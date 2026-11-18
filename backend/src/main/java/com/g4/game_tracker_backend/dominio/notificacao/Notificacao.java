package com.g4.game_tracker_backend.dominio.notificacao;
import java.time.Instant;
import java.util.UUID;
public record Notificacao(UUID id, UUID destinatarioId, TipoNotificacao tipo, String titulo,
        String mensagem, String contextoOrigem, Instant geradaEm, Instant lidaEm) {
    public Notificacao marcarLida(Instant agora) {
        return lidaEm == null ? new Notificacao(id, destinatarioId, tipo, titulo, mensagem,
                contextoOrigem, geradaEm, agora) : this;
    }
}
