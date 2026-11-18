package com.g4.game_tracker_backend.infraestrutura.persistencia.wishlist;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface WishlistSpringDataRepository extends JpaRepository<WishlistJpa, UUID> {

    @Query("select w.jogoId from WishlistJpa w where w.usuarioId = :usuarioId order by w.adicionadoEm desc")
    List<UUID> jogoIdsDoUsuario(@Param("usuarioId") UUID usuarioId);

    boolean existsByUsuarioIdAndJogoId(UUID usuarioId, UUID jogoId);

    void deleteByUsuarioIdAndJogoId(UUID usuarioId, UUID jogoId);

    long countByJogoId(UUID jogoId);
}