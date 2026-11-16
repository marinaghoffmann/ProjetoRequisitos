package com.g4.game_tracker_backend.aplicacao.notificacao;
import java.time.Instant;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.g4.game_tracker_backend.dominio.notificacao.*;
@Service
public class DespacharNotificacao {
    private final NotificacaoRepositorio notificacoes;
    public DespacharNotificacao(NotificacaoRepositorio notificacoes) { this.notificacoes=notificacoes; }
    @Transactional
    public void enviar(UUID usuario, TipoNotificacao tipo, String titulo, String mensagem,
            String origem, Instant agora) {
        if (!notificacoes.existePorOrigem(origem))
            notificacoes.salvar(new Notificacao(UUID.randomUUID(),usuario,tipo,titulo,mensagem,origem,agora,null));
    }
}
