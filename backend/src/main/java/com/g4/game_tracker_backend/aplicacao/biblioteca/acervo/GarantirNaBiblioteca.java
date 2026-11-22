package com.g4.game_tracker_backend.aplicacao.biblioteca.acervo;

import java.util.UUID;

import com.g4.game_tracker_backend.aplicacao.compartilhado.CasoDeUso;
import com.g4.game_tracker_backend.aplicacao.compartilhado.Transacional;
import com.g4.game_tracker_backend.dominio.biblioteca.acervo.BibliotecaRepositorio;
import com.g4.game_tracker_backend.dominio.biblioteca.acervo.ItemBiblioteca;

/**
 * Garante que o jogo esteja na biblioteca do usuário. Usado por "Jogar agora" e "Marcar zerado":
 * se o jogo já estiver na biblioteca, nada acontece; senão, ele é adicionado.
 */
@CasoDeUso
public class GarantirNaBiblioteca {

    private final BibliotecaRepositorio repositorio;

    public GarantirNaBiblioteca(BibliotecaRepositorio repositorio) {
        this.repositorio = repositorio;
    }

    @Transacional
    public void executar(UUID usuarioId, UUID jogoId) {
        if (!repositorio.existe(usuarioId, jogoId)) {
            repositorio.salvar(ItemBiblioteca.adicionar(usuarioId, jogoId));
        }
    }
}
