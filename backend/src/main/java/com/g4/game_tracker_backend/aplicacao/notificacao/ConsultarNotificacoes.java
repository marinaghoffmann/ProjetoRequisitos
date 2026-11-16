package com.g4.game_tracker_backend.aplicacao.notificacao;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.g4.game_tracker_backend.aplicacao.identidade.VerificadorDeAcesso;
import com.g4.game_tracker_backend.dominio.compartilhado.AcessoNegadoException;
import com.g4.game_tracker_backend.dominio.compartilhado.RecursoNaoEncontradoException;
import com.g4.game_tracker_backend.dominio.notificacao.*;
@Service
public class ConsultarNotificacoes {
    private final NotificacaoRepositorio notificacoes;
    private final VerificadorDeAcesso acesso;
    public ConsultarNotificacoes(NotificacaoRepositorio notificacoes,VerificadorDeAcesso acesso) {
        this.notificacoes=notificacoes; this.acesso=acesso;
    }
    @Transactional(readOnly=true)
    public List<Notificacao> listar() { return notificacoes.listarPorUsuario(acesso.usuarioAtualId()); }
    @Transactional
    public Notificacao marcarLida(UUID id) {
        Notificacao n=notificacoes.buscar(id)
                .orElseThrow(()->new RecursoNaoEncontradoException("Notificação não encontrada"));
        if (!n.destinatarioId().equals(acesso.usuarioAtualId()))
            throw new AcessoNegadoException("Esta notificação pertence a outro usuário");
        return notificacoes.salvar(n.marcarLida(Instant.now()));
    }
}
