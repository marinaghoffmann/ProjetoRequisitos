package com.g4.game_tracker_backend.wishlist;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.g4.game_tracker_backend.dominio.compartilhado.RecursoNaoEncontradoException;
import com.g4.game_tracker_backend.dominio.compartilhado.RegraDeNegocioException;
import com.g4.game_tracker_backend.dominio.wishlist.ItemWishlist;
import com.g4.game_tracker_backend.dominio.wishlist.PrioridadeWishlist;
import com.g4.game_tracker_backend.dominio.wishlist.Wishlist;

class WishlistTest {

    private static final Instant T0 = Instant.parse("2026-01-01T10:00:00Z");

    private final UUID dono = UUID.randomUUID();
    private final UUID jogoA = UUID.randomUUID();
    private final UUID jogoB = UUID.randomUUID();
    private final UUID jogoC = UUID.randomUUID();

    private Wishlist vazia() {
        return Wishlist.criar(dono, T0);
    }

    @Test
    void rn1_wishlist_nasce_vazia_para_o_dono() {
        Wishlist wishlist = vazia();
        assertEquals(dono, wishlist.donoId());
        assertTrue(wishlist.itens().isEmpty());
    }

    @Test
    void rn2_recusa_jogo_repetido_com_a_mensagem_do_cml() {
        Wishlist wishlist = vazia().adicionar(jogoA, PrioridadeWishlist.ALTA, T0);

        RegraDeNegocioException erro = assertThrows(RegraDeNegocioException.class,
                () -> wishlist.adicionar(jogoA, PrioridadeWishlist.MEDIA, T0.plusSeconds(1)));

        assertEquals("Este jogo já está na sua wishlist", erro.getMessage());
    }

    @Test
    void rn3_sem_prioridade_usa_media() {
        Wishlist wishlist = vazia().adicionar(jogoA, null, T0);
        assertEquals(PrioridadeWishlist.MEDIA, wishlist.itens().get(0).prioridade());
    }

    @Test
    void adicionar_com_prioridade_informada_mantem_a_prioridade() {
        Wishlist wishlist = vazia().adicionar(jogoA, PrioridadeWishlist.ALTA, T0);
        assertEquals(PrioridadeWishlist.ALTA, wishlist.itens().get(0).prioridade());
    }

    @Test
    void lista_ordenada_por_prioridade_alta_media_baixa() {
        Wishlist wishlist = vazia()
                .adicionar(jogoA, PrioridadeWishlist.BAIXA, T0)
                .adicionar(jogoB, PrioridadeWishlist.ALTA, T0.plusSeconds(1))
                .adicionar(jogoC, PrioridadeWishlist.MEDIA, T0.plusSeconds(2));

        List<UUID> ordem = wishlist.itensOrdenados().stream().map(ItemWishlist::jogoId).toList();

        assertEquals(List.of(jogoB, jogoC, jogoA), ordem);
    }

    @Test
    void na_mesma_prioridade_o_mais_recente_vem_primeiro() {
        Wishlist wishlist = vazia()
                .adicionar(jogoA, PrioridadeWishlist.ALTA, T0)
                .adicionar(jogoB, PrioridadeWishlist.ALTA, T0.plusSeconds(5));

        assertEquals(jogoB, wishlist.itensOrdenados().get(0).jogoId());
    }

    @Test
    void altera_a_prioridade_sem_mudar_a_data_de_adicao() {
        Wishlist wishlist = vazia().adicionar(jogoA, PrioridadeWishlist.BAIXA, T0)
                .alterarPrioridade(jogoA, PrioridadeWishlist.ALTA);

        ItemWishlist item = wishlist.itens().get(0);
        assertEquals(PrioridadeWishlist.ALTA, item.prioridade());
        assertEquals(T0, item.adicionadoEm());
    }

    @Test
    void alterar_prioridade_de_jogo_fora_da_lista_falha() {
        assertThrows(RecursoNaoEncontradoException.class,
                () -> vazia().alterarPrioridade(jogoA, PrioridadeWishlist.ALTA));
    }

    @Test
    void remove_o_jogo_e_e_idempotente() {
        Wishlist wishlist = vazia().adicionar(jogoA, PrioridadeWishlist.MEDIA, T0).remover(jogoA);

        assertFalse(wishlist.contem(jogoA));
        assertTrue(wishlist.remover(jogoA).itens().isEmpty());
    }

    @Test
    void o_agregado_e_imutavel() {
        Wishlist original = vazia();
        original.adicionar(jogoA, PrioridadeWishlist.ALTA, T0);
        assertTrue(original.itens().isEmpty());
    }
}