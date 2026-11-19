package com.g4.game_tracker_backend.aplicacao.notificacao;
import java.time.Instant;
import java.util.UUID;
import com.g4.game_tracker_backend.dominio.notificacao.*;
import com.g4.game_tracker_backend.aplicacao.compartilhado.CasoDeUso;
import com.g4.game_tracker_backend.aplicacao.compartilhado.Transacional;
@CasoDeUso
public class DespacharNotificacao {
    private final NotificacaoRepositorio notificacoes;
    public DespacharNotificacao(NotificacaoRepositorio notificacoes) { this.notificacoes=notificacoes; }
    @Transacional
    public void enviar(UUID usuario, TipoNotificacao tipo, String titulo, String mensagem,
            String origem, Instant agora) {
        if (!notificacoes.existePorOrigem(origem))
            notificacoes.salvar(new Notificacao(UUID.randomUUID(),usuario,tipo,titulo,mensagem,origem,agora,null));
    }
}
