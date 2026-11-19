package com.g4.game_tracker_backend.aplicacao.notificacao;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import com.g4.game_tracker_backend.aplicacao.identidade.VerificadorDeAcesso;
import com.g4.game_tracker_backend.dominio.compartilhado.AcessoNegadoException;
import com.g4.game_tracker_backend.dominio.compartilhado.RecursoNaoEncontradoException;
import com.g4.game_tracker_backend.dominio.notificacao.*;
import com.g4.game_tracker_backend.aplicacao.compartilhado.CasoDeUso;
import com.g4.game_tracker_backend.aplicacao.compartilhado.Transacional;
@CasoDeUso
public class ConsultarNotificacoes {
    private final NotificacaoRepositorio notificacoes;
    private final VerificadorDeAcesso acesso;
    public ConsultarNotificacoes(NotificacaoRepositorio notificacoes,VerificadorDeAcesso acesso) {
        this.notificacoes=notificacoes; this.acesso=acesso;
    }
    @Transacional(somenteLeitura = true)
    public List<Notificacao> listar() { return notificacoes.listarPorUsuario(acesso.usuarioAtualId()); }
    @Transacional
    public Notificacao marcarLida(UUID id) {
        Notificacao n=notificacoes.buscar(id)
                .orElseThrow(()->new RecursoNaoEncontradoException("Notificação não encontrada"));
        if (!n.destinatarioId().equals(acesso.usuarioAtualId()))
            throw new AcessoNegadoException("Esta notificação pertence a outro usuário");
        return notificacoes.salvar(n.marcarLida(Instant.now()));
    }
}
