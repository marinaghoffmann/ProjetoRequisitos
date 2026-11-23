package com.g4.game_tracker_backend.dominio.biblioteca.acervo;

import java.util.List;
import java.util.UUID;

public interface BibliotecaRepositorio {

    ItemBiblioteca salvar(ItemBiblioteca item);

    boolean existe(UUID usuarioId, UUID jogoId);

    void remover(UUID usuarioId, UUID jogoId);

    List<ItemBiblioteca> listarPorUsuario(UUID usuarioId);
}
