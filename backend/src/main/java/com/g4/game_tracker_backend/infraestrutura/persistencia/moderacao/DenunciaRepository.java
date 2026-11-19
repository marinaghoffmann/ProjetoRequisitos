package com.g4.game_tracker_backend.infraestrutura.persistencia.moderacao;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.g4.game_tracker_backend.dominio.moderacao.StatusDenuncia;
import com.g4.game_tracker_backend.dominio.moderacao.TipoConteudo;

interface DenunciaRepository extends JpaRepository<DenunciaJpa, UUID> {

    Optional<DenunciaJpa> findByDenuncianteIdAndTipoAndConteudoIdAndStatus(UUID denuncianteId, TipoConteudo tipo,
            UUID conteudoId, StatusDenuncia status);

    List<DenunciaJpa> findByTipoAndConteudoIdAndStatus(TipoConteudo tipo, UUID conteudoId, StatusDenuncia status);

    Page<DenunciaJpa> findByStatusOrderByRegistradaEmAsc(StatusDenuncia status, Pageable pagina);
}
