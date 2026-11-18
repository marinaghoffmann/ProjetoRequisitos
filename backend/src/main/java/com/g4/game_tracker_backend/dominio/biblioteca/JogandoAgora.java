package com.g4.game_tracker_backend.dominio.biblioteca;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public final class JogandoAgora {

    public static final int LIMITE = 5;

    private final UUID id;
    private final UUID usuarioId;
    private final UUID jogoId;
    private final ProgressoJogatina progresso;
    private final Instant iniciadoEm;
    private final boolean ativo;

    private JogandoAgora(UUID id, UUID usuarioId, UUID jogoId, ProgressoJogatina progresso,
            Instant iniciadoEm, boolean ativo) {
        this.id = Objects.requireNonNull(id);
        this.usuarioId = Objects.requireNonNull(usuarioId);
        this.jogoId = Objects.requireNonNull(jogoId);
        this.progresso = Objects.requireNonNull(progresso);
        this.iniciadoEm = Objects.requireNonNull(iniciadoEm);
        this.ativo = ativo;
    }

    public static JogandoAgora iniciar(UUID usuarioId, UUID jogoId, Instant agora) {
        return new JogandoAgora(UUID.randomUUID(), usuarioId, jogoId, ProgressoJogatina.NO_INICIO, agora, true);
    }

    public static JogandoAgora reconstituir(UUID id, UUID usuarioId, UUID jogoId, ProgressoJogatina progresso,
            Instant iniciadoEm, boolean ativo) {
        return new JogandoAgora(id, usuarioId, jogoId, progresso, iniciadoEm, ativo);
    }

    public JogandoAgora comProgresso(ProgressoJogatina novo) {
        return new JogandoAgora(id, usuarioId, jogoId, novo, iniciadoEm, ativo);
    }

    public JogandoAgora encerrar() {
        return new JogandoAgora(id, usuarioId, jogoId, progresso, iniciadoEm, false);
    }

    public UUID id() { return id; }
    public UUID usuarioId() { return usuarioId; }
    public UUID jogoId() { return jogoId; }
    public ProgressoJogatina progresso() { return progresso; }
    public Instant iniciadoEm() { return iniciadoEm; }
    public boolean ativo() { return ativo; }
}