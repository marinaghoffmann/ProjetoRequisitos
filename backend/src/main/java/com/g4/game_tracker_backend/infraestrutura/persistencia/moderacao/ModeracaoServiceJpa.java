package com.g4.game_tracker_backend.infraestrutura.persistencia.moderacao;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.PageRequest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.g4.game_tracker_backend.aplicacao.identidade.VerificadorDeAcesso;
import com.g4.game_tracker_backend.aplicacao.moderacao.ModeracaoOperacoes;
import com.g4.game_tracker_backend.aplicacao.notificacao.DespacharNotificacao;
import com.g4.game_tracker_backend.dominio.compartilhado.RecursoNaoEncontradoException;
import com.g4.game_tracker_backend.dominio.compartilhado.RegraDeNegocioException;
import com.g4.game_tracker_backend.dominio.identidade.Papel;
import com.g4.game_tracker_backend.dominio.moderacao.*;
import com.g4.game_tracker_backend.dominio.notificacao.TipoNotificacao;
import com.g4.game_tracker_backend.infraestrutura.persistencia.moderacao.*;

@Service
public class ModeracaoServiceJpa implements ModeracaoOperacoes {
    private final VerificadorDeAcesso acesso;
    private final JdbcTemplate jdbc;
    private final DenunciaRepository denuncias;
    private final EstadoConteudoRepository estados;
    private final LogModeracaoRepository logs;
    private final AdvertenciaRepository advertencias;
    private final DespacharNotificacao notificacoes;
    private final MidiaModeradaRepository midias;

    public ModeracaoServiceJpa(VerificadorDeAcesso acesso, JdbcTemplate jdbc, DenunciaRepository denuncias,
            EstadoConteudoRepository estados, LogModeracaoRepository logs, AdvertenciaRepository advertencias,
            DespacharNotificacao notificacoes, MidiaModeradaRepository midias) {
        this.acesso = acesso; this.jdbc = jdbc; this.denuncias = denuncias; this.estados = estados;
        this.logs = logs; this.advertencias = advertencias; this.notificacoes = notificacoes; this.midias = midias;
    }

    private void exigirModerador() {
        if (!acesso.usuarioAtualTemAlgumPapel(Papel.MODERADOR, Papel.ADMINISTRADOR))
            throw new com.g4.game_tracker_backend.dominio.compartilhado.AcessoNegadoException("Acesso restrito a moderadores e administradores");
    }


    /** Table names are a fixed whitelist; no client input is interpolated into SQL. */
    private Alvo alvo(TipoConteudo tipo, UUID id, boolean bloquear) {
        String sql = switch (tipo) {
            case TOPICO -> "select autor_id, id, conteudo from forum_topicos where id = ?";
            case RESPOSTA -> "select autor_id, topico_id, conteudo from forum_respostas where id = ?";
            case REVIEW -> "select autor_id, id, texto from reviews where id = ?";
            case LISTA -> "select dono_id, id, descricao from listas_personalizadas where id = ?";
            case AVALIACAO_MARKETPLACE -> "select avaliador_id, id, comentario from marketplace_avaliacoes where id = ?";
            case ANUNCIO -> "select vendedor_id, id, titulo from anuncios_marketplace where id = ?";
            case PERFIL -> "select id, id, bio from usuarios where id = ?";
        };
        List<Alvo> encontrados = jdbc.query(sql + (bloquear ? " for update" : ""), (rs, row) ->
                new Alvo(rs.getObject(1, UUID.class), rs.getObject(2, UUID.class), rs.getString(3)), id);
        if (encontrados.isEmpty()) throw new RecursoNaoEncontradoException("Conteúdo não encontrado");
        return encontrados.getFirst();
    }

    public Alvo previa(TipoConteudo tipo, UUID id) { exigirModerador(); return alvo(tipo,id,false); }

    @Transactional
    public Denuncia denunciar(TipoConteudo tipo, UUID id, String motivo) {
        UUID usuario = acesso.usuarioAtualId();
        if (tipo == null || id == null) throw new RegraDeNegocioException("Tipo e identificador do conteúdo são obrigatórios");
        validarTexto(motivo, 1000, "O motivo é obrigatório");
        alvo(tipo, id, true);
        return dto(denuncias.findByDenuncianteIdAndTipoAndConteudoIdAndStatus(usuario,tipo,id,StatusDenuncia.PENDENTE)
                .orElseGet(() -> denuncias.save(new DenunciaJpa(usuario,tipo,id,motivo.strip(),Instant.now()))));
    }

    @Transactional(readOnly=true)
    public org.springframework.data.domain.Page<Denuncia> fila(int pagina,int tamanho) {
        exigirModerador(); validarPagina(pagina,tamanho);
        return denuncias.findByStatusOrderByRegistradaEmAsc(StatusDenuncia.PENDENTE,PageRequest.of(pagina-1,tamanho)).map(ModeracaoServiceJpa::dto);
    }

    @Transactional(readOnly=true)
    public org.springframework.data.domain.Page<Log> historico(int pagina,int tamanho) {
        exigirModerador(); validarPagina(pagina,tamanho);
        return logs.findAllByOrderByRegistradoEmDesc(PageRequest.of(pagina-1,tamanho)).map(ModeracaoServiceJpa::dto);
    }

    @Transactional(readOnly=true)
    public List<Advertencia> advertencias(UUID usuarioId) {
        exigirModerador(); return advertencias.findByUsuarioIdOrderByRegistradaEmDesc(usuarioId).stream().map(ModeracaoServiceJpa::dto).toList();
    }

    @Transactional(readOnly=true)
    public long totalAdvertencias(UUID usuarioId) {
        exigirModerador(); return advertencias.countByUsuarioId(usuarioId);
    }

    @Transactional
    public Log arquivar(UUID denunciaId, String justificativa) {
        exigirModerador();
        DenunciaJpa denuncia = denuncias.findById(denunciaId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Denúncia não encontrada"));
        if (denuncia.status != StatusDenuncia.PENDENTE) throw new RegraDeNegocioException("Denúncia já analisada");
        alvo(denuncia.tipo,denuncia.conteudoId,true);
        denuncia.status=StatusDenuncia.IMPROCEDENTE; denuncia.chavePendente=null; denuncia.analisadaEm=Instant.now(); denuncias.save(denuncia);
        return dto(registrar(AcaoModeracao.ARQUIVAR_DENUNCIA,denuncia.tipo,denuncia.conteudoId,justificativa,Instant.now()));
    }

    @Transactional
    public Log agir(AcaoModeracao acao, TipoConteudo tipo, UUID id, String justificativa,
            String novoTitulo, String novoConteudo, String novaCategoria) {
        return agir(acao,tipo,id,justificativa,novoTitulo,novoConteudo,novaCategoria,null);
    }

    @Transactional
    public Log agir(AcaoModeracao acao, TipoConteudo tipo, UUID id, String justificativa,
            String novoTitulo, String novoConteudo, String novaCategoria, UUID novaRespostaPaiId) {
        return new com.g4.game_tracker_backend.aplicacao.moderacao.FluxoAcaoModerativa<Log>() {
            private Alvo alvo;
            private Instant agora;
            private EstadoConteudoJpa estado;

            @Override protected void validar() {
        exigirModerador();
        if (acao == null || tipo == null || id == null) throw new RegraDeNegocioException("Ação e conteúdo são obrigatórios");
        if (acao == AcaoModeracao.ARQUIVAR_DENUNCIA || acao == AcaoModeracao.ADVERTIR_USUARIO)
            throw new RegraDeNegocioException("Use a operação específica para esta ação");
        if ((acao == AcaoModeracao.OCULTAR || acao == AcaoModeracao.EXCLUIR))
            validarTexto(justificativa,2000,"A justificativa é obrigatória");
        // Match forum's root-first lock order before changing a reply or its counters.
        if (tipo == TipoConteudo.RESPOSTA) {
            UUID topicoId = jdbc.query("select topico_id from forum_respostas where id = ?",
                    (rs,row) -> rs.getObject(1,UUID.class),id).stream().findFirst()
                    .orElseThrow(() -> new RecursoNaoEncontradoException("Conteúdo não encontrado"));
            jdbc.queryForObject("select id from forum_topicos where id = ? for update",UUID.class,topicoId);
        }
        alvo = alvo(tipo,id,true);
        agora = Instant.now();
        estado = estados.findByTipoAndConteudoId(tipo,id)
                .orElseGet(() -> new EstadoConteudoJpa(tipo,id,agora));
            }

            @Override protected void aplicar() {
        switch (acao) {
            case OCULTAR -> {
                if (estado.oculto) throw new RegraDeNegocioException("Conteúdo já está oculto");
                estado.oculto=true;
            }
            case EXCLUIR -> {
                if (estado.removido) throw new RegraDeNegocioException("Conteúdo já foi removido");
                estado.removido=true;
                if (tipo == TipoConteudo.TOPICO) {
                    estado.statusAnterior=jdbc.queryForObject("select status from forum_topicos where id = ?",String.class,id);
                    jdbc.update("update forum_topicos set status = 'ENCERRADO' where id = ?",id);
                }

            }
            case REVERTER_OCULTACAO -> {
                if (!estado.oculto) throw new RegraDeNegocioException("Conteúdo não está oculto");
                estado.oculto=false;
            }
            case REVERTER_EXCLUSAO -> {
                if (!estado.removido) throw new RegraDeNegocioException("Conteúdo não está removido");
                estado.removido=false;
                if (tipo == TipoConteudo.TOPICO && estado.statusAnterior != null) {
                    jdbc.update("update forum_topicos set status = ? where id = ?",estado.statusAnterior,id);
                    estado.statusAnterior=null;
                }
            }
            case MARCAR_18 -> {
                if (estado.aviso18) throw new RegraDeNegocioException("Conteúdo já tem aviso 18+");
                estado.aviso18=true;
            }
            case RETIRAR_18 -> {
                if (!estado.aviso18) throw new RegraDeNegocioException("Conteúdo não tem aviso 18+");
                estado.aviso18=false;
            }
            case EDITAR -> editarForum(tipo,id,novoTitulo,novoConteudo,agora);
            case MOVER -> {
                if (tipo == TipoConteudo.TOPICO) {
                    validarTexto(novaCategoria,60,"A categoria é obrigatória");
                    jdbc.update("update forum_topicos set categoria = ?, data_ultima_atividade = ? where id = ?",novaCategoria.strip(),agora,id);
                } else if (tipo == TipoConteudo.RESPOSTA) {
                    moverResposta(id,alvo.topicoId(),novaRespostaPaiId,agora);
                } else throw new RegraDeNegocioException("Movimentação disponível apenas no fórum");
            }
            case ENCERRAR_TOPICO -> {
                if (tipo != TipoConteudo.TOPICO) throw new RegraDeNegocioException("Apenas tópicos podem ser encerrados");
                jdbc.update("update forum_topicos set status = 'ENCERRADO', data_ultima_atividade = ? where id = ?",agora,id);
            }
            default -> throw new RegraDeNegocioException("Ação inválida");
        }
        estado.atualizadoEm=agora; estados.saveAndFlush(estado);
        if (acao == AcaoModeracao.OCULTAR || acao == AcaoModeracao.EXCLUIR) registrarMidias(tipo,id);
        if (tipo == TipoConteudo.RESPOSTA && (acao == AcaoModeracao.EXCLUIR || acao == AcaoModeracao.REVERTER_EXCLUSAO || acao == AcaoModeracao.OCULTAR || acao == AcaoModeracao.REVERTER_OCULTACAO)) {
            jdbc.update("update forum_topicos set total_respostas = (select count(*) from forum_respostas r where r.topico_id = ? and r.removida = false and not exists (select 1 from moderacao_estados m where m.tipo = 'RESPOSTA' and m.conteudo_id = r.id and (m.removido = true or m.oculto = true))), data_ultima_atividade = ? where id = ?",alvo.topicoId(),agora,alvo.topicoId());
        }
        if ((tipo == TipoConteudo.TOPICO || tipo == TipoConteudo.RESPOSTA) &&
                (acao == AcaoModeracao.OCULTAR || acao == AcaoModeracao.EXCLUIR ||
                 acao == AcaoModeracao.REVERTER_OCULTACAO || acao == AcaoModeracao.REVERTER_EXCLUSAO)) {
            UUID topicoId = tipo == TipoConteudo.TOPICO ? id : alvo.topicoId();
            jdbc.update("update forum_topicos set total_curtidas = (select count(*) from forum_curtidas c where c.topico_id = ? and not exists (select 1 from moderacao_estados m where m.conteudo_id = c.conteudo_id and m.tipo = c.tipo and (m.oculto = true or m.removido = true))) where id = ?",topicoId,topicoId);
        }
        if (acao != AcaoModeracao.REVERTER_OCULTACAO && acao != AcaoModeracao.REVERTER_EXCLUSAO && acao != AcaoModeracao.RETIRAR_18)
            resolverDenuncias(tipo,id,agora);
            }

            @Override protected Log registrar() {
                return dto(ModeracaoServiceJpa.this.registrar(acao,tipo,id,justificativa,agora));
            }

            @Override protected void notificar(Log registro) {
                notificacoes.enviar(alvo.autorId(),TipoNotificacao.MODERACAO,"Ação de moderação",
                        "Seu conteúdo recebeu a ação " + acao.name() + ". " + (justificativa == null ? "" : justificativa),
                        "moderacao:" + registro.id(),agora);
            }
        }.executar();
    }

    private void moverResposta(UUID id,UUID topicoId,UUID novaPaiId,Instant agora) {
        if (id.equals(novaPaiId)) throw new RegraDeNegocioException("A resposta não pode ser pai de si mesma");
        if (novaPaiId != null) {
            java.util.List<UUID> pais = jdbc.query("select resposta_pai_id from forum_respostas where id = ? and topico_id = ? and removida = false",
                    (rs,row) -> rs.getObject(1,UUID.class),novaPaiId,topicoId);
            if (pais.isEmpty()) throw new RegraDeNegocioException("A nova resposta pai não pertence ao tópico");
            if (estados.findByTipoAndConteudoId(TipoConteudo.RESPOSTA,novaPaiId).map(e -> !e.publico()).orElse(false))
                throw new RegraDeNegocioException("A nova resposta pai está moderada");
            UUID cursor=novaPaiId;
            while (cursor != null) {
                if (cursor.equals(id)) throw new RegraDeNegocioException("A movimentação criaria um ciclo de respostas");
                UUID atual=cursor;
                cursor=jdbc.queryForObject("select resposta_pai_id from forum_respostas where id = ?",UUID.class,atual);
            }
        }
        jdbc.update("update forum_respostas set resposta_pai_id = ?, editada_em = ? where id = ?",novaPaiId,agora,id);
        jdbc.update("update forum_topicos set data_ultima_atividade = ? where id = ?",agora,topicoId);
    }

    private void registrarMidias(TipoConteudo tipo,UUID id) {
        java.util.List<String> urls = switch (tipo) {
            case TOPICO -> jdbc.query("select url from forum_topico_imagens where topico_id = ?",(rs,row)->rs.getString(1),id);
            case RESPOSTA -> jdbc.query("select url from forum_resposta_imagens where resposta_id = ?",(rs,row)->rs.getString(1),id);
            case ANUNCIO -> jdbc.query("select url_foto from anuncios_fotos where anuncio_id = ?",(rs,row)->rs.getString(1),id);
            case PERFIL -> jdbc.query("select avatar_url,banner_url from usuarios where id = ?",(rs,row)->java.util.List.of(rs.getString(1)==null ? "" : rs.getString(1),rs.getString(2)==null ? "" : rs.getString(2)),id).stream().flatMap(java.util.List::stream).toList();
            default -> java.util.List.of();
        };
        for (String url: urls) if (url != null && !url.isBlank() && !midias.existsByTipoAndConteudoIdAndUrl(tipo,id,url))
            midias.save(new MidiaModeradaJpa(tipo,id,url));
    }

    private void editarForum(TipoConteudo tipo,UUID id,String titulo,String conteudo,Instant agora) {
        if (tipo != TipoConteudo.TOPICO && tipo != TipoConteudo.RESPOSTA)
            throw new RegraDeNegocioException("Edição moderativa disponível apenas no fórum");
        validarTexto(conteudo,tipo==TipoConteudo.TOPICO?10000:5000,"O conteúdo é obrigatório");
        if (tipo==TipoConteudo.TOPICO) {
            validarTexto(titulo,150,"O título é obrigatório");
            jdbc.update("update forum_topicos set titulo = ?, conteudo = ?, editado_em = ?, data_ultima_atividade = ? where id = ?",titulo.strip(),conteudo.strip(),agora,agora,id);
        } else {
            jdbc.update("update forum_respostas set conteudo = ?, editada_em = ? where id = ?",conteudo.strip(),agora,id);
            jdbc.update("update forum_topicos set data_ultima_atividade = ? where id = (select topico_id from forum_respostas where id = ?)",agora,id);
        }
    }

    @Transactional
    public Advertencia advertir(UUID usuarioId,String motivo) {
        exigirModerador(); validarTexto(motivo,1000,"O motivo é obrigatório");
        if (jdbc.queryForObject("select count(*) from usuarios where id = ?",Integer.class,usuarioId)==0)
            throw new RecursoNaoEncontradoException("Usuário não encontrado");
        Instant agora=Instant.now();
        AdvertenciaJpa advertencia=advertencias.save(new AdvertenciaJpa(usuarioId,acesso.usuarioAtualId(),motivo.strip(),agora));
        registrar(AcaoModeracao.ADVERTIR_USUARIO,TipoConteudo.PERFIL,usuarioId,motivo,agora);
        notificacoes.enviar(usuarioId,TipoNotificacao.ADVERTENCIA,"Advertência recebida",motivo,
                "moderacao:advertencia:"+advertencia.id,agora);
        return dto(advertencia);
    }

    private LogModeracaoJpa registrar(AcaoModeracao acao,TipoConteudo tipo,UUID id,String justificativa,Instant agora) {
        return logs.save(new LogModeracaoJpa(acesso.usuarioAtualId(),acao,tipo,id,justificativa,agora));
    }
    private void resolverDenuncias(TipoConteudo tipo,UUID id,Instant agora) {
        for (DenunciaJpa denuncia: denuncias.findByTipoAndConteudoIdAndStatus(tipo,id,StatusDenuncia.PENDENTE)) {
            denuncia.status=StatusDenuncia.PROCEDENTE; denuncia.chavePendente=null; denuncia.analisadaEm=agora; denuncias.save(denuncia);
        }
    }
    private static Denuncia dto(DenunciaJpa e) { return new Denuncia(e.id,e.denuncianteId,e.tipo,e.conteudoId,e.motivo,e.status,e.registradaEm,e.analisadaEm); }
    private static Log dto(LogModeracaoJpa e) { return new Log(e.id,e.moderadorId,e.acao,e.tipo,e.conteudoId,e.justificativa,e.registradoEm); }
    private static Advertencia dto(AdvertenciaJpa e) { return new Advertencia(e.id,e.usuarioId,e.moderadorId,e.motivo,e.registradaEm); }
    private static void validarTexto(String valor,int limite,String mensagem) {
        if (valor==null || valor.isBlank()) throw new RegraDeNegocioException(mensagem);
        if (valor.strip().length()>limite) throw new RegraDeNegocioException("Texto acima do limite de " + limite + " caracteres");
    }
    private static void validarPagina(int pagina,int tamanho) {
        if (pagina<1 || tamanho<1 || tamanho>50) throw new RegraDeNegocioException("Página ou tamanho inválido");
    }
}
