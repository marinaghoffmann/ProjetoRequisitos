package com.g4.game_tracker_backend.infraestrutura.persistencia.wishlist;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface WishlistSpringDataRepository extends JpaRepository<WishlistJpa, UUID> {

    @Query("select count(w) from WishlistJpa w join w.itens i where i.jogoId = :jogoId")
    long contarComOJogo(@Param("jogoId") UUID jogoId);
}