package com.g4.game_tracker_backend.infraestrutura.persistencia.wishlist;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.g4.game_tracker_backend.dominio.wishlist.WishlistRepositorio;

@Repository
public class WishlistRepositorioPostgres implements WishlistRepositorio {

    private final WishlistSpringDataRepository springData;

    public WishlistRepositorioPostgres(WishlistSpringDataRepository springData) {
        this.springData = springData;
    }

    @Override
    public List<UUID> jogoIdsDoUsuario(UUID usuarioId) {
        return springData.jogoIdsDoUsuario(usuarioId);
    }

    @Override
    public boolean contem(UUID usuarioId, UUID jogoId) {
        return springData.existsByUsuarioIdAndJogoId(usuarioId, jogoId);
    }

    @Override
    @Transactional
    public void adicionar(UUID usuarioId, UUID jogoId) {
        if (!contem(usuarioId, jogoId)) {
            springData.save(new WishlistJpa(usuarioId, jogoId));
        }
    }

    @Override
    @Transactional
    public void remover(UUID usuarioId, UUID jogoId) {
        springData.deleteByUsuarioIdAndJogoId(usuarioId, jogoId);
    }
}