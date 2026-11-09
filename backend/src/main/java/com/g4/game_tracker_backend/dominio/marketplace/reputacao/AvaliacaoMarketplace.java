package com.g4.game_tracker_backend.dominio.marketplace.reputacao;

import java.time.Duration;
import java.time.Instant;
import java.util.EnumSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

import com.g4.game_tracker_backend.dominio.compartilhado.AcessoNegadoException;
import com.g4.game_tracker_backend.dominio.compartilhado.RegraDeNegocioException;

/**
 * Avaliação de uma das partes de um pedido pela outra (F-21). Imutável.
 *
 * RN 1: só existe para um pedido CONCLUIDO, e só comprador e vendedor desse pedido avaliam.
 * RN 2: até 15 dias após a confirmação da entrega. A unicidade por parte e pedido é checada no caso de
 * uso e garantida pela restrição única no banco.
 */
public final class AvaliacaoMarketplace {

    public static final Duration PRAZO_PARA_AVALIAR = Duration.ofDays(15);
    public static final int LIMITE_DO_COMENTARIO = 1000;

    private final UUID id;
    private final UUID pedidoId;
    private final UUID avaliadorId;
    private final UUID avaliadoId;
    private final PapelNaNegociacao papelAvaliado;
    private final int estrelas;
    private final Set<TagComportamento> tags;
    private final String comentario;
    private final Instant criadaEm;

    private AvaliacaoMarketplace(UUID id, UUID pedidoId, UUID avaliadorId, UUID avaliadoId,
            PapelNaNegociacao papelAvaliado, int estrelas, Set<TagComportamento> tags, String comentario,
            Instant criadaEm) {
        this.id = Objects.requireNonNull(id, "O identificador da avaliação é obrigatório");
        this.pedidoId = Objects.requireNonNull(pedidoId, "O pedido é obrigatório");
        this.avaliadorId = Objects.requireNonNull(avaliadorId, "O avaliador é obrigatório");
        this.avaliadoId = Objects.requireNonNull(avaliadoId, "O avaliado é obrigatório");
        this.papelAvaliado = Objects.requireNonNull(papelAvaliado, "O papel do avaliado é obrigatório");
        if (estrelas < 1 || estrelas > 5) {
            throw new RegraDeNegocioException("A nota deve ser de 1 a 5 estrelas");
        }
        this.estrelas = estrelas;
        this.tags = tags == null || tags.isEmpty() ? Set.of() : Set.copyOf(EnumSet.copyOf(tags));
        this.comentario = comentario(comentario);
        this.criadaEm = Objects.requireNonNull(criadaEm, "A data da avaliação é obrigatória");
    }

    /** O avaliador é quem está logado; o avaliado é a outra parte do pedido. */
    public static AvaliacaoMarketplace criar(PedidoAvaliavel pedido, UUID avaliadorId, int estrelas,
            Set<TagComportamento> tags, String comentario, Instant agora) {
        UUID avaliadoId;
        PapelNaNegociacao papelAvaliado;
        if (pedido.compradorId().equals(avaliadorId)) {
            avaliadoId = pedido.vendedorId();
            papelAvaliado = PapelNaNegociacao.VENDEDOR;
        } else if (pedido.vendedorId().equals(avaliadorId)) {
            avaliadoId = pedido.compradorId();
            papelAvaliado = PapelNaNegociacao.COMPRADOR;
        } else {
            throw new AcessoNegadoException("Apenas o comprador e o vendedor do pedido podem avaliar");
        }
        if (!pedido.concluido()) {
            throw new RegraDeNegocioException("Só é possível avaliar pedidos concluídos");
        }
        if (agora.isAfter(prazoFinal(pedido))) {
            throw new RegraDeNegocioException("O prazo de 15 dias para avaliar terminou");
        }
        return new AvaliacaoMarketplace(UUID.randomUUID(), pedido.pedidoId(), avaliadorId, avaliadoId, papelAvaliado,
                estrelas, tags, comentario, agora);
    }

    public static AvaliacaoMarketplace reconstituir(UUID id, UUID pedidoId, UUID avaliadorId, UUID avaliadoId,
            PapelNaNegociacao papelAvaliado, int estrelas, Set<TagComportamento> tags, String comentario,
            Instant criadaEm) {
        return new AvaliacaoMarketplace(id, pedidoId, avaliadorId, avaliadoId, papelAvaliado, estrelas, tags,
                comentario, criadaEm);
    }

    /** Até quando as partes de um pedido concluído podem avaliar. */
    public static Instant prazoFinal(PedidoAvaliavel pedido) {
        return pedido.entregaConfirmadaEm().plus(PRAZO_PARA_AVALIAR);
    }

    private static String comentario(String valor) {
        if (valor == null || valor.isBlank()) {
            return null;
        }
        String normalizado = valor.strip();
        if (normalizado.length() > LIMITE_DO_COMENTARIO) {
            throw new RegraDeNegocioException("O comentário deve ter no máximo " + LIMITE_DO_COMENTARIO
                    + " caracteres");
        }
        return normalizado;
    }

    public UUID id() { return id; }
    public UUID pedidoId() { return pedidoId; }
    public UUID avaliadorId() { return avaliadorId; }
    public UUID avaliadoId() { return avaliadoId; }
    public PapelNaNegociacao papelAvaliado() { return papelAvaliado; }
    public int estrelas() { return estrelas; }
    public Set<TagComportamento> tags() { return tags; }
    public String comentario() { return comentario; }
    public Instant criadaEm() { return criadaEm; }
}
