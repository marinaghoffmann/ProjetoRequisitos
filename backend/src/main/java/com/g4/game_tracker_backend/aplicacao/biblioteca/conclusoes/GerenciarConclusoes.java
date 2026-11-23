package com.g4.game_tracker_backend.aplicacao.biblioteca.conclusoes;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;

import com.g4.game_tracker_backend.aplicacao.biblioteca.acervo.GarantirNaBiblioteca;
import com.g4.game_tracker_backend.aplicacao.identidade.VerificadorDeAcesso;
import com.g4.game_tracker_backend.dominio.avaliacao.estatisticas.MetricasDoJogoAlteradas;
import com.g4.game_tracker_backend.dominio.biblioteca.conclusoes.RegistroDeConclusao;
import com.g4.game_tracker_backend.dominio.biblioteca.conclusoes.RegistroDeConclusao.Dados;
import com.g4.game_tracker_backend.dominio.biblioteca.conclusoes.RegistroDeConclusaoRepositorio;
import com.g4.game_tracker_backend.dominio.biblioteca.conclusoes.RegistroZeradoNaBiblioteca;
import com.g4.game_tracker_backend.dominio.biblioteca.conclusoes.ResumoJogoZerado;
import com.g4.game_tracker_backend.dominio.catalogo.JogoRepositorio;
import com.g4.game_tracker_backend.dominio.compartilhado.RecursoNaoEncontradoException;
import com.g4.game_tracker_backend.dominio.journal.eventos.ConclusaoExcluida;
import com.g4.game_tracker_backend.dominio.journal.eventos.JogoZerado;
import com.g4.game_tracker_backend.aplicacao.compartilhado.CasoDeUso;
import com.g4.game_tracker_backend.aplicacao.compartilhado.Transacional;
import com.g4.game_tracker_backend.dominio.compartilhado.PublicadorDeEventos;

@CasoDeUso
public class GerenciarConclusoes {

    private final RegistroDeConclusaoRepositorio registros;
    private final JogoRepositorio jogos;
    private final VerificadorDeAcesso acesso;
    private final PublicadorDeEventos eventos;
    private final GarantirNaBiblioteca garantirNaBiblioteca;

    public GerenciarConclusoes(RegistroDeConclusaoRepositorio registros, JogoRepositorio jogos,
            VerificadorDeAcesso acesso, PublicadorDeEventos eventos, GarantirNaBiblioteca garantirNaBiblioteca) {
        this.registros = registros;
        this.jogos = jogos;
        this.acesso = acesso;
        this.eventos = eventos;
        this.garantirNaBiblioteca = garantirNaBiblioteca;
    }

    @Transacional
    public RegistroDeConclusao registrar(UUID jogoId, Dados dados) {
        UUID jogadorId = acesso.usuarioAtualId();
        exigirJogoExistente(jogoId);
        // Marcar zerado implica ter o jogo na biblioteca.
        garantirNaBiblioteca.executar(jogadorId, jogoId);

        Instant agora = Instant.now();
        ZoneId fusoServidor = ZoneId.systemDefault();
        RegistroDeConclusao registro = RegistroDeConclusao.registrar(jogadorId, jogoId, dados,
                LocalDate.ofInstant(agora, fusoServidor), agora);
        registros.inserir(registro);
        publicarJogoZerado(registro, fusoServidor);
        eventos.publicar(new MetricasDoJogoAlteradas(jogoId));
        return registro;
    }

    @Transacional
    public RegistroDeConclusao editar(UUID id, Dados dados) {
        return editar(id, null, dados);
    }

    @Transacional
    public RegistroDeConclusao editar(UUID id, UUID jogoId, Dados dados) {
        RegistroDeConclusao atual = registroDoJogador(id);
        UUID destino = jogoId == null ? atual.jogoId() : jogoId;
        boolean mudouJogo = !atual.jogoId().equals(destino);
        if (mudouJogo) {
            exigirJogoExistente(destino);
        }
        ZoneId fusoServidor = ZoneId.systemDefault();
        RegistroDeConclusao registro = atual.editar(destino, dados, LocalDate.now(fusoServidor));
        registros.atualizar(registro);
        if (mudouJogo) {
            eventos.publicar(new ConclusaoExcluida(id.toString()));
            publicarJogoZerado(registro, fusoServidor);
            eventos.publicar(new MetricasDoJogoAlteradas(atual.jogoId()));
        }
        eventos.publicar(new MetricasDoJogoAlteradas(destino));
        return registro;
    }

    @Transacional
    public void excluir(UUID id) {
        RegistroDeConclusao registro = registroDoJogador(id);
        registros.excluir(id);
        eventos.publicar(new ConclusaoExcluida(id.toString()));
        eventos.publicar(new MetricasDoJogoAlteradas(registro.jogoId()));
    }

    @Transacional(somenteLeitura = true)
    public List<RegistroDeConclusao> meuHistorico(UUID jogoId) {
        return registros.historico(acesso.usuarioAtualId(), jogoId);
    }

    @Transacional(somenteLeitura = true)
    public List<ResumoJogoZerado> meusJogosZerados() {
        return registros.jogosZerados(acesso.usuarioAtualId());
    }

    @Transacional(somenteLeitura = true)
    public List<RegistroZeradoNaBiblioteca> meusRegistros() {
        return registros.registrosDoJogador(acesso.usuarioAtualId());
    }

    private void exigirJogoExistente(UUID jogoId) {
        jogos.buscarPorId(jogoId).orElseThrow(() -> new RecursoNaoEncontradoException("Jogo não encontrado"));
    }

    private void publicarJogoZerado(RegistroDeConclusao registro, ZoneId fusoServidor) {
        eventos.publicar(new JogoZerado(registro.jogadorId(), registro.jogoId(), registro.id().toString(),
                registro.dataConclusao().atStartOfDay(fusoServidor).toInstant()));
    }

    private RegistroDeConclusao registroDoJogador(UUID id) {
        RegistroDeConclusao registro = registros.buscarParaAlterar(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Registro de conclusão não encontrado"));
        acesso.exigirDono(registro.jogadorId(), "Apenas o dono pode alterar este registro");
        return registro;
    }
}
