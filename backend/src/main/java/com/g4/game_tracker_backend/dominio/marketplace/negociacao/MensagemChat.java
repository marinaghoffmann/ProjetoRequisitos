package com.g4.game_tracker_backend.dominio.marketplace.negociacao;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

import com.g4.game_tracker_backend.dominio.compartilhado.RegraDeNegocioException;

/** Mensagem da conversa (F-19). Imutável; lidaEm é preenchida quando o destinatário abre a conversa. */
public final class MensagemChat {

    public static final int LIMITE_DO_TEXTO = 1000;

    private final UUID id;
    private final UUID remetenteId;
    private final String texto;
    private final Instant enviadaEm;
    private final Instant lidaEm;

    private MensagemChat(UUID id, UUID remetenteId, String texto, Instant enviadaEm, Instant lidaEm) {
        this.id = Objects.requireNonNull(id, "O identificador da mensagem é obrigatório");
        this.remetenteId = Objects.requireNonNull(remetenteId, "O remetente é obrigatório");
        this.texto = Objects.requireNonNull(texto, "O texto da mensagem é obrigatório");
        this.enviadaEm = Objects.requireNonNull(enviadaEm, "A data de envio é obrigatória");
        this.lidaEm = lidaEm;
    }

    /** RN 4: a mensagem não pode ser vazia e tem no máximo 1.000 caracteres. */
    static MensagemChat nova(UUID remetenteId, String texto, Instant agora) {
        if (texto == null || texto.isBlank()) {
            throw new RegraDeNegocioException("A mensagem não pode ser vazia");
        }
        String normalizado = texto.strip();
        if (normalizado.length() > LIMITE_DO_TEXTO) {
            throw new RegraDeNegocioException("A mensagem deve ter no máximo " + LIMITE_DO_TEXTO + " caracteres");
        }
        return new MensagemChat(UUID.randomUUID(), remetenteId, normalizado, agora, null);
    }

    public static MensagemChat reconstituir(UUID id, UUID remetenteId, String texto, Instant enviadaEm,
            Instant lidaEm) {
        return new MensagemChat(id, remetenteId, texto, enviadaEm, lidaEm);
    }

    /** RN 11: só conta como não lida para quem a recebeu. */
    boolean naoLidaPor(UUID usuarioId) {
        return lidaEm == null && !remetenteId.equals(usuarioId);
    }

    MensagemChat lida(Instant agora) {
        return new MensagemChat(id, remetenteId, texto, enviadaEm, agora);
    }

    public UUID id() { return id; }
    public UUID remetenteId() { return remetenteId; }
    public String texto() { return texto; }
    public Instant enviadaEm() { return enviadaEm; }
    public Instant lidaEm() { return lidaEm; }
}
