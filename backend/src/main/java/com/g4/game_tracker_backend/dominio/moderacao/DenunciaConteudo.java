package com.g4.game_tracker_backend.dominio.moderacao;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

import com.g4.game_tracker_backend.dominio.compartilhado.RegraDeNegocioException;

/**
 * Denúncia de um jogador sobre um conteúdo público. O conteúdo continua público enquanto a denúncia
 * estiver pendente; a decisão do moderador a torna procedente ou improcedente, uma única vez.
 */
public final class DenunciaConteudo {

    public static final int LIMITE_DO_MOTIVO = 1000;

    private final UUID id;
    private final UUID denuncianteId;
    private final TipoConteudo tipo;
    private final UUID conteudoId;
    private final String motivo;
    private final StatusDenuncia status;
    private final Instant registradaEm;
    private final Instant analisadaEm;

    private DenunciaConteudo(UUID id, UUID denuncianteId, TipoConteudo tipo, UUID conteudoId, String motivo,
            StatusDenuncia status, Instant registradaEm, Instant analisadaEm) {
        this.id = Objects.requireNonNull(id);
        this.denuncianteId = Objects.requireNonNull(denuncianteId);
        this.tipo = tipo;
        this.conteudoId = conteudoId;
        this.motivo = motivo;
        this.status = Objects.requireNonNull(status);
        this.registradaEm = Objects.requireNonNull(registradaEm);
        this.analisadaEm = analisadaEm;
    }

    public static DenunciaConteudo registrar(UUID denuncianteId, TipoConteudo tipo, UUID conteudoId, String motivo,
            Instant agora) {
        if (tipo == null || conteudoId == null) {
            throw new RegraDeNegocioException("Tipo e identificador do conteúdo são obrigatórios");
        }
        String texto = TextoModerado.exigir(motivo, LIMITE_DO_MOTIVO, "O motivo é obrigatório");
        return new DenunciaConteudo(UUID.randomUUID(), denuncianteId, tipo, conteudoId, texto,
                StatusDenuncia.PENDENTE, agora, null);
    }

    public static DenunciaConteudo reconstituir(UUID id, UUID denuncianteId, TipoConteudo tipo, UUID conteudoId,
            String motivo, StatusDenuncia status, Instant registradaEm, Instant analisadaEm) {
        return new DenunciaConteudo(id, denuncianteId, tipo, conteudoId, motivo, status, registradaEm, analisadaEm);
    }

    /** O moderador agiu sobre o conteúdo: a denúncia tinha fundamento. */
    public DenunciaConteudo julgarProcedente(Instant agora) {
        return analisar(StatusDenuncia.PROCEDENTE, agora);
    }

    /** O moderador arquivou: a denúncia não tinha fundamento. */
    public DenunciaConteudo arquivar(Instant agora) {
        return analisar(StatusDenuncia.IMPROCEDENTE, agora);
    }

    private DenunciaConteudo analisar(StatusDenuncia decisao, Instant agora) {
        if (!pendente()) {
            throw new RegraDeNegocioException("Denúncia já analisada");
        }
        return new DenunciaConteudo(id, denuncianteId, tipo, conteudoId, motivo, decisao, registradaEm,
                Objects.requireNonNull(agora));
    }

    public boolean pendente() {
        return status == StatusDenuncia.PENDENTE;
    }

    public UUID id() { return id; }
    public UUID denuncianteId() { return denuncianteId; }
    public TipoConteudo tipo() { return tipo; }
    public UUID conteudoId() { return conteudoId; }
    public String motivo() { return motivo; }
    public StatusDenuncia status() { return status; }
    public Instant registradaEm() { return registradaEm; }
    public Instant analisadaEm() { return analisadaEm; }
}
