package com.g4.game_tracker_backend.aplicacao.biblioteca;

import java.util.UUID;

import com.g4.game_tracker_backend.aplicacao.compartilhado.CasoDeUso;
import com.g4.game_tracker_backend.aplicacao.compartilhado.Transacional;
import com.g4.game_tracker_backend.aplicacao.identidade.VerificadorDeAcesso;
import com.g4.game_tracker_backend.dominio.biblioteca.JogandoAgoraRepositorio;
import com.g4.game_tracker_backend.dominio.biblioteca.conclusoes.RegistroDeConclusaoRepositorio;
import com.g4.game_tracker_backend.dominio.wishlist.WishlistRepositorio;

/**
 * Totais da "Minha biblioteca" do menu lateral para o usuário autenticado: jogos em andamento (F-07), jogos na
 * wishlist (F-06) e jogos zerados (F-11, cada jogo uma vez, mesmo com várias conclusões).
 */
@CasoDeUso
public class ResumirBiblioteca {

    public record Resumo(long emAndamento, int naWishlist, int jogosZerados) {
    }

    private final JogandoAgoraRepositorio jogandoAgora;
    private final WishlistRepositorio wishlists;
    private final RegistroDeConclusaoRepositorio conclusoes;
    private final VerificadorDeAcesso acesso;

    public ResumirBiblioteca(JogandoAgoraRepositorio jogandoAgora, WishlistRepositorio wishlists,
            RegistroDeConclusaoRepositorio conclusoes, VerificadorDeAcesso acesso) {
        this.jogandoAgora = jogandoAgora;
        this.wishlists = wishlists;
        this.conclusoes = conclusoes;
        this.acesso = acesso;
    }

    @Transacional(somenteLeitura = true)
    public Resumo executar() {
        UUID usuarioId = acesso.usuarioAtualId();
        return new Resumo(jogandoAgora.contarAtivos(usuarioId),
                wishlists.buscarPorDono(usuarioId).map(w -> w.itens().size()).orElse(0),
                conclusoes.jogosZerados(usuarioId).size());
    }
}
