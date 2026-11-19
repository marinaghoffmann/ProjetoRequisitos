package com.g4.game_tracker_backend.dominio.moderacao;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

import com.g4.game_tracker_backend.dominio.compartilhado.RegraDeNegocioException;

/**
 * Situação de moderação de um conteúdo. Um conteúdo sem estado gravado é público e sem aviso.
 * Ocultar tira o conteúdo do ar; remover troca o texto pela mensagem de sistema (o tópico removido
 * continua no endereço para preservar a conversa). O aviso 18+ não restringe a leitura.
 */
public final class EstadoConteudo {

    private final UUID id;
    private final TipoConteudo tipo;
    private final UUID conteudoId;
    private final boolean oculto;
    private final boolean removido;
    private final boolean aviso18;
    private final String statusAnterior;
    private final Instant atualizadoEm;

    private EstadoConteudo(UUID id, TipoConteudo tipo, UUID conteudoId, boolean oculto, boolean removido,
            boolean aviso18, String statusAnterior, Instant atualizadoEm) {
        this.id = Objects.requireNonNull(id);
        this.tipo = Objects.requireNonNull(tipo);
        this.conteudoId = Objects.requireNonNull(conteudoId);
        this.oculto = oculto;
        this.removido = removido;
        this.aviso18 = aviso18;
        this.statusAnterior = statusAnterior;
        this.atualizadoEm = Objects.requireNonNull(atualizadoEm);
    }

    public static EstadoConteudo publico(TipoConteudo tipo, UUID conteudoId, Instant agora) {
        return new EstadoConteudo(UUID.randomUUID(), tipo, conteudoId, false, false, false, null, agora);
    }

    public static EstadoConteudo reconstituir(UUID id, TipoConteudo tipo, UUID conteudoId, boolean oculto,
            boolean removido, boolean aviso18, String statusAnterior, Instant atualizadoEm) {
        return new EstadoConteudo(id, tipo, conteudoId, oculto, removido, aviso18, statusAnterior, atualizadoEm);
    }

    public EstadoConteudo ocultar(Instant agora) {
        if (oculto) throw new RegraDeNegocioException("Conteúdo já está oculto");
        return com(true, removido, aviso18, statusAnterior, agora);
    }

    public EstadoConteudo reverterOcultacao(Instant agora) {
        if (!oculto) throw new RegraDeNegocioException("Conteúdo não está oculto");
        return com(false, removido, aviso18, statusAnterior, agora);
    }

    /** statusDoTopico guarda o status do tópico removido, para a reversão restaurá-lo. */
    public EstadoConteudo remover(String statusDoTopico, Instant agora) {
        if (removido) throw new RegraDeNegocioException("Conteúdo já foi removido");
        return com(oculto, true, aviso18, tipo == TipoConteudo.TOPICO ? statusDoTopico : null, agora);
    }

    public EstadoConteudo reverterRemocao(Instant agora) {
        if (!removido) throw new RegraDeNegocioException("Conteúdo não está removido");
        return com(oculto, false, aviso18, null, agora);
    }

    public EstadoConteudo marcar18(Instant agora) {
        if (aviso18) throw new RegraDeNegocioException("Conteúdo já tem aviso 18+");
        return com(oculto, removido, true, statusAnterior, agora);
    }

    public EstadoConteudo retirar18(Instant agora) {
        if (!aviso18) throw new RegraDeNegocioException("Conteúdo não tem aviso 18+");
        return com(oculto, removido, false, statusAnterior, agora);
    }

    /** Ações que mexem no conteúdo (editar, mover, encerrar) sem mudar a visibilidade. */
    public EstadoConteudo tocar(Instant agora) {
        return com(oculto, removido, aviso18, statusAnterior, agora);
    }

    private EstadoConteudo com(boolean novoOculto, boolean novoRemovido, boolean novoAviso18, String novoStatus,
            Instant agora) {
        return new EstadoConteudo(id, tipo, conteudoId, novoOculto, novoRemovido, novoAviso18, novoStatus, agora);
    }

    /** Nem oculto nem removido. */
    public boolean ativo() {
        return !oculto && !removido;
    }

    /** O que o público ainda alcança: o tópico removido (e não oculto) segue no endereço com a mensagem. */
    public boolean visivelPublicamente() {
        return ativo() || (tipo == TipoConteudo.TOPICO && removido && !oculto);
    }

    public UUID id() { return id; }
    public TipoConteudo tipo() { return tipo; }
    public UUID conteudoId() { return conteudoId; }
    public boolean oculto() { return oculto; }
    public boolean removido() { return removido; }
    public boolean aviso18() { return aviso18; }
    public String statusAnterior() { return statusAnterior; }
    public Instant atualizadoEm() { return atualizadoEm; }
}
