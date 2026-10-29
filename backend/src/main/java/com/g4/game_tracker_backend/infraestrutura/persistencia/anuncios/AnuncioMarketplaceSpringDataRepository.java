package com.g4.game_tracker_backend.infraestrutura.persistencia.anuncios;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.g4.game_tracker_backend.dominio.marketplace.anuncios.EstadoConservacao;
import com.g4.game_tracker_backend.dominio.marketplace.anuncios.StatusAnuncio;
import com.g4.game_tracker_backend.dominio.marketplace.anuncios.TipoNegociacao;

public interface AnuncioMarketplaceSpringDataRepository extends JpaRepository<AnuncioMarketplaceJpa, UUID> {

    List<AnuncioMarketplaceJpa> findByVendedorIdOrderByAtualizadoEmDesc(UUID vendedorId);

    @Query("""
            select a from AnuncioMarketplaceJpa a
            where a.status in :status
              and a.tipoNegociacao in :tipos
              and (:jogoId is null or a.jogoId = :jogoId)
              and lower(a.titulo) like lower(concat('%', :titulo, '%'))
              and lower(a.localidade) like lower(concat('%', :regiao, '%'))
              and (:precoMinimo is null or a.preco >= :precoMinimo)
              and (:precoMaximo is null or a.preco <= :precoMaximo)
              and (:estado is null or a.estado = :estado)
            order by a.atualizadoEm desc
            """)
    List<AnuncioMarketplaceJpa> buscarPublicos(@Param("status") Collection<StatusAnuncio> status,
            @Param("tipos") Collection<TipoNegociacao> tipos, @Param("jogoId") UUID jogoId,
            @Param("titulo") String titulo, @Param("regiao") String regiao,
            @Param("precoMinimo") BigDecimal precoMinimo, @Param("precoMaximo") BigDecimal precoMaximo,
            @Param("estado") EstadoConservacao estado);
}
