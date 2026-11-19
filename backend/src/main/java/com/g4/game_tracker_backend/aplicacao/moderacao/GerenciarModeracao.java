package com.g4.game_tracker_backend.aplicacao.moderacao;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;


import com.g4.game_tracker_backend.aplicacao.identidade.VerificadorDeAcesso;
import com.g4.game_tracker_backend.aplicacao.notificacao.DespacharNotificacao;
import com.g4.game_tracker_backend.dominio.compartilhado.AcessoNegadoException;
import com.g4.game_tracker_backend.dominio.compartilhado.Pagina;
import com.g4.game_tracker_backend.dominio.compartilhado.RecursoNaoEncontradoException;
import com.g4.game_tracker_backend.dominio.compartilhado.RegraDeNegocioException;
import com.g4.game_tracker_backend.dominio.identidade.Papel;
import com.g4.game_tracker_backend.dominio.moderacao.AcaoModeracao;
import com.g4.game_tracker_backend.dominio.moderacao.AdvertenciaRepositorio;
import com.g4.game_tracker_backend.dominio.moderacao.DenunciaConteudo;
import com.g4.game_tracker_backend.dominio.moderacao.DenunciaRepositorio;
import com.g4.game_tracker_backend.dominio.moderacao.EstadoConteudo;
import com.g4.game_tracker_backend.dominio.moderacao.EstadoConteudoRepositorio;
import com.g4.game_tracker_backend.dominio.moderacao.LogDeModeracao;
import com.g4.game_tracker_backend.dominio.moderacao.LogModeracaoRepositorio;
import com.g4.game_tracker_backend.dominio.moderacao.TextoModerado;
import com.g4.game_tracker_backend.dominio.moderacao.TipoConteudo;
import com.g4.game_tracker_backend.dominio.notificacao.TipoNotificacao;
import com.g4.game_tracker_backend.aplicacao.compartilhado.CasoDeUso;
import com.g4.game_tracker_backend.aplicacao.compartilhado.Transacional;

/**
 * Central de moderação (F-14). Denunciar é aberto a qualquer usuário autenticado; as demais operações
 * exigem moderador ou administrador. Toda decisão gera um log e notifica o autor do conteúdo.
 */
@CasoDeUso
public class GerenciarModeracao implements ModeracaoOperacoes {

    private static final int LIMITE_DA_JUSTIFICATIVA = 2000;
    private static final int LIMITE_DO_TOPICO = 10000;
    private static final int LIMITE_DA_RESPOSTA = 5000;
    private static final int LIMITE_DO_TITULO = 150;
    private static final int LIMITE_DA_CATEGORIA = 60;

    private final VerificadorDeAcesso acesso;
    private final DenunciaRepositorio denuncias;
    private final EstadoConteudoRepositorio estados;
    private final LogModeracaoRepositorio logs;
    private final AdvertenciaRepositorio advertencias;
    private final ConteudosModeraveis conteudos;
    private final MidiasModeradas midias;
    private final DespacharNotificacao notificacoes;

    public GerenciarModeracao(VerificadorDeAcesso acesso, DenunciaRepositorio denuncias,
            EstadoConteudoRepositorio estados, LogModeracaoRepositorio logs, AdvertenciaRepositorio advertencias,
            ConteudosModeraveis conteudos, MidiasModeradas midias, DespacharNotificacao notificacoes) {
        this.acesso = acesso;
        this.denuncias = denuncias;
        this.estados = estados;
        this.logs = logs;
        this.advertencias = advertencias;
        this.conteudos = conteudos;
        this.midias = midias;
        this.notificacoes = notificacoes;
    }

    // ---- Denúncias ----

    @Override
    public Alvo previa(TipoConteudo tipo, UUID id) {
        exigirModerador();
        return alvo(tipo, id, false);
    }

    /** Uma pendência por jogador e alvo: denunciar de novo devolve a denúncia já aberta. */
    @Override
    @Transacional
    public Denuncia denunciar(TipoConteudo tipo, UUID id, String motivo) {
        UUID denunciante = acesso.usuarioAtualId();
        DenunciaConteudo nova = DenunciaConteudo.registrar(denunciante, tipo, id, motivo, Instant.now());
        alvo(tipo, id, true);
        DenunciaConteudo denuncia = denuncias.buscarPendente(denunciante, tipo, id)
                .orElseGet(() -> denuncias.salvar(nova));
        return dto(denuncia);
    }

    @Override
    @Transacional(somenteLeitura = true)
    public Pagina<Denuncia> fila(int pagina, int tamanho) {
        exigirModerador();
        validarPagina(pagina, tamanho);
        return denuncias.filaDePendentes(pagina, tamanho).map(GerenciarModeracao::dto);
    }

    @Override
    @Transacional
    public Log arquivar(UUID denunciaId, String justificativa) {
        exigirModerador();
        DenunciaConteudo denuncia = denuncias.buscarPorId(denunciaId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Denúncia não encontrada"));
        Instant agora = Instant.now();
        DenunciaConteudo arquivada = denuncia.arquivar(agora);
        alvo(denuncia.tipo(), denuncia.conteudoId(), true);
        denuncias.salvar(arquivada);
        return dto(registrar(AcaoModeracao.ARQUIVAR_DENUNCIA, denuncia.tipo(), denuncia.conteudoId(),
                justificativa, agora));
    }

    // ---- Ações sobre o conteúdo ----

    @Override
    @Transacional
    public Log agir(AcaoModeracao acao, TipoConteudo tipo, UUID id, String justificativa, String novoTitulo,
            String novoConteudo, String novaCategoria) {
        return agir(acao, tipo, id, justificativa, novoTitulo, novoConteudo, novaCategoria, null);
    }

    @Override
    @Transacional
    public Log agir(AcaoModeracao acao, TipoConteudo tipo, UUID id, String justificativa, String novoTitulo,
            String novoConteudo, String novaCategoria, UUID novaRespostaPaiId) {
        return new AcaoSobreConteudo(acao, tipo, id, justificativa, novoTitulo, novoConteudo, novaCategoria,
                novaRespostaPaiId).executar();
    }

    /** Template Method: validar, aplicar, registrar e notificar, nessa ordem, para qualquer ação. */
    private final class AcaoSobreConteudo extends FluxoAcaoModerativa<Log> {

        private final AcaoModeracao acao;
        private final TipoConteudo tipo;
        private final UUID id;
        private final String justificativa;
        private final String novoTitulo;
        private final String novoConteudo;
        private final String novaCategoria;
        private final UUID novaRespostaPaiId;
        private Alvo alvo;
        private EstadoConteudo estado;
        private Instant agora;

        private AcaoSobreConteudo(AcaoModeracao acao, TipoConteudo tipo, UUID id, String justificativa,
                String novoTitulo, String novoConteudo, String novaCategoria, UUID novaRespostaPaiId) {
            this.acao = acao;
            this.tipo = tipo;
            this.id = id;
            this.justificativa = justificativa;
            this.novoTitulo = novoTitulo;
            this.novoConteudo = novoConteudo;
            this.novaCategoria = novaCategoria;
            this.novaRespostaPaiId = novaRespostaPaiId;
        }

        @Override
        protected void validar() {
            exigirModerador();
            if (acao == null || tipo == null || id == null) {
                throw new RegraDeNegocioException("Ação e conteúdo são obrigatórios");
            }
            if (acao == AcaoModeracao.ARQUIVAR_DENUNCIA || acao == AcaoModeracao.ADVERTIR_USUARIO) {
                throw new RegraDeNegocioException("Use a operação específica para esta ação");
            }
            if (acao.exigeJustificativa()) {
                TextoModerado.exigir(justificativa, LIMITE_DA_JUSTIFICATIVA, "A justificativa é obrigatória");
            }
            if (tipo == TipoConteudo.RESPOSTA) {
                UUID topicoId = conteudos.topicoDaResposta(id)
                        .orElseThrow(() -> new RecursoNaoEncontradoException("Conteúdo não encontrado"));
                conteudos.bloquearTopico(topicoId);
            }
            alvo = alvo(tipo, id, true);
            agora = Instant.now();
            estado = estados.buscar(tipo, id).orElseGet(() -> EstadoConteudo.publico(tipo, id, agora));
        }

        @Override
        protected void aplicar() {
            estado = switch (acao) {
                case OCULTAR -> estado.ocultar(agora);
                case REVERTER_OCULTACAO -> estado.reverterOcultacao(agora);
                case EXCLUIR -> remover();
                case REVERTER_EXCLUSAO -> reverterRemocao();
                case MARCAR_18 -> estado.marcar18(agora);
                case RETIRAR_18 -> estado.retirar18(agora);
                case EDITAR -> {
                    editar();
                    yield estado.tocar(agora);
                }
                case MOVER -> {
                    mover();
                    yield estado.tocar(agora);
                }
                case ENCERRAR_TOPICO -> {
                    if (tipo != TipoConteudo.TOPICO) {
                        throw new RegraDeNegocioException("Apenas tópicos podem ser encerrados");
                    }
                    conteudos.encerrarTopico(id, agora);
                    yield estado.tocar(agora);
                }
                default -> throw new RegraDeNegocioException("Ação inválida");
            };
            estados.salvar(estado);
            if (acao == AcaoModeracao.OCULTAR || acao == AcaoModeracao.EXCLUIR) {
                midias.proteger(tipo, id, conteudos.urlsDeMidia(tipo, id));
            }
            if (acao.alteraVisibilidade()) {
                atualizarContadoresDoForum();
            }
            if (acao.resolveDenuncias()) {
                for (DenunciaConteudo pendente : denuncias.listarPendentesDoAlvo(tipo, id)) {
                    denuncias.salvar(pendente.julgarProcedente(agora));
                }
            }
        }

        @Override
        protected Log registrar() {
            return dto(GerenciarModeracao.this.registrar(acao, tipo, id, justificativa, agora));
        }

        @Override
        protected void notificar(Log registro) {
            String complemento = justificativa == null ? "" : justificativa;
            notificacoes.enviar(alvo.autorId(), TipoNotificacao.MODERACAO, "Ação de moderação",
                    "Seu conteúdo recebeu a ação " + acao.name() + ". " + complemento,
                    "moderacao:" + registro.id(), agora);
        }

        /** O tópico removido é encerrado; o status anterior fica guardado para a reversão. */
        private EstadoConteudo remover() {
            String statusAnterior = null;
            if (tipo == TipoConteudo.TOPICO) {
                statusAnterior = conteudos.statusDoTopico(id);
            }
            EstadoConteudo removido = estado.remover(statusAnterior, agora);
            if (tipo == TipoConteudo.TOPICO) {
                conteudos.alterarStatusDoTopico(id, "ENCERRADO");
            }
            return removido;
        }

        private EstadoConteudo reverterRemocao() {
            String statusAnterior = estado.statusAnterior();
            EstadoConteudo restaurado = estado.reverterRemocao(agora);
            if (tipo == TipoConteudo.TOPICO && statusAnterior != null) {
                conteudos.alterarStatusDoTopico(id, statusAnterior);
            }
            return restaurado;
        }

        private void editar() {
            if (tipo == TipoConteudo.TOPICO) {
                String conteudo = TextoModerado.exigir(novoConteudo, LIMITE_DO_TOPICO, "O conteúdo é obrigatório");
                String titulo = TextoModerado.exigir(novoTitulo, LIMITE_DO_TITULO, "O título é obrigatório");
                conteudos.editarTopico(id, titulo, conteudo, agora);
            } else if (tipo == TipoConteudo.RESPOSTA) {
                String conteudo = TextoModerado.exigir(novoConteudo, LIMITE_DA_RESPOSTA,
                        "O conteúdo é obrigatório");
                conteudos.editarResposta(id, conteudo, agora);
            } else {
                throw new RegraDeNegocioException("Edição moderativa disponível apenas no fórum");
            }
        }

        private void mover() {
            if (tipo == TipoConteudo.TOPICO) {
                String categoria = TextoModerado.exigir(novaCategoria, LIMITE_DA_CATEGORIA,
                        "A categoria é obrigatória");
                conteudos.moverTopico(id, categoria, agora);
            } else if (tipo == TipoConteudo.RESPOSTA) {
                moverResposta();
            } else {
                throw new RegraDeNegocioException("Movimentação disponível apenas no fórum");
            }
        }

        /** A nova resposta pai precisa ser do mesmo tópico, estar ativa e não descender da resposta movida. */
        private void moverResposta() {
            if (id.equals(novaRespostaPaiId)) {
                throw new RegraDeNegocioException("A resposta não pode ser pai de si mesma");
            }
            if (novaRespostaPaiId != null) {
                if (!conteudos.respostaAtivaNoTopico(novaRespostaPaiId, alvo.topicoId())) {
                    throw new RegraDeNegocioException("A nova resposta pai não pertence ao tópico");
                }
                boolean paiModerado = estados.buscar(TipoConteudo.RESPOSTA, novaRespostaPaiId)
                        .map(e -> !e.ativo()).orElse(false);
                if (paiModerado) {
                    throw new RegraDeNegocioException("A nova resposta pai está moderada");
                }
                Optional<UUID> cursor = Optional.of(novaRespostaPaiId);
                while (cursor.isPresent()) {
                    if (cursor.get().equals(id)) {
                        throw new RegraDeNegocioException("A movimentação criaria um ciclo de respostas");
                    }
                    cursor = conteudos.paiDaResposta(cursor.get());
                }
            }
            conteudos.definirPaiDaResposta(id, novaRespostaPaiId, alvo.topicoId(), agora);
        }

        private void atualizarContadoresDoForum() {
            if (tipo == TipoConteudo.RESPOSTA) {
                conteudos.recalcularTotalDeRespostas(alvo.topicoId(), agora);
            }
            if (tipo == TipoConteudo.TOPICO || tipo == TipoConteudo.RESPOSTA) {
                conteudos.recalcularTotalDeCurtidas(tipo == TipoConteudo.TOPICO ? id : alvo.topicoId());
            }
        }
    }

    // ---- Advertências e auditoria ----

    @Override
    @Transacional
    public Advertencia advertir(UUID usuarioId, String motivo) {
        exigirModerador();
        Instant agora = Instant.now();
        // O agregado tem o mesmo nome do record de resposta herdado de ModeracaoOperacoes.
        var advertencia = com.g4.game_tracker_backend.dominio.moderacao.Advertencia.emitir(usuarioId,
                acesso.usuarioAtualId(), motivo, agora);
        if (!conteudos.usuarioExiste(usuarioId)) {
            throw new RecursoNaoEncontradoException("Usuário não encontrado");
        }
        advertencias.salvar(advertencia);
        registrar(AcaoModeracao.ADVERTIR_USUARIO, TipoConteudo.PERFIL, usuarioId, advertencia.motivo(), agora);
        notificacoes.enviar(usuarioId, TipoNotificacao.ADVERTENCIA, "Advertência recebida", advertencia.motivo(),
                "moderacao:advertencia:" + advertencia.id(), agora);
        return dto(advertencia);
    }

    @Override
    @Transacional(somenteLeitura = true)
    public List<Advertencia> advertencias(UUID usuarioId) {
        exigirModerador();
        return advertencias.listarPorUsuario(usuarioId).stream().map(GerenciarModeracao::dto).toList();
    }

    @Override
    @Transacional(somenteLeitura = true)
    public long totalAdvertencias(UUID usuarioId) {
        exigirModerador();
        return advertencias.contarPorUsuario(usuarioId);
    }

    @Override
    @Transacional(somenteLeitura = true)
    public Pagina<Log> historico(int pagina, int tamanho) {
        exigirModerador();
        validarPagina(pagina, tamanho);
        return logs.historico(pagina, tamanho).map(GerenciarModeracao::dto);
    }

    // ---- Apoio ----

    private void exigirModerador() {
        if (!acesso.usuarioAtualTemAlgumPapel(Papel.MODERADOR, Papel.ADMINISTRADOR)) {
            throw new AcessoNegadoException("Acesso restrito a moderadores e administradores");
        }
    }

    private Alvo alvo(TipoConteudo tipo, UUID id, boolean bloquear) {
        return conteudos.buscar(tipo, id, bloquear)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Conteúdo não encontrado"));
    }

    private LogDeModeracao registrar(AcaoModeracao acao, TipoConteudo tipo, UUID id, String justificativa,
            Instant agora) {
        return logs.registrar(LogDeModeracao.registrar(acesso.usuarioAtualId(), acao, tipo, id, justificativa,
                agora));
    }

    private static void validarPagina(int pagina, int tamanho) {
        if (pagina < 1 || tamanho < 1 || tamanho > 50) {
            throw new RegraDeNegocioException("Página ou tamanho inválido");
        }
    }

    private static Denuncia dto(DenunciaConteudo d) {
        return new Denuncia(d.id(), d.denuncianteId(), d.tipo(), d.conteudoId(), d.motivo(), d.status(),
                d.registradaEm(), d.analisadaEm());
    }

    private static Log dto(LogDeModeracao l) {
        return new Log(l.id(), l.moderadorId(), l.acao(), l.tipo(), l.conteudoId(), l.justificativa(),
                l.registradoEm());
    }

    private static Advertencia dto(com.g4.game_tracker_backend.dominio.moderacao.Advertencia a) {
        return new Advertencia(a.id(), a.usuarioId(), a.moderadorId(), a.motivo(), a.registradaEm());
    }
}
