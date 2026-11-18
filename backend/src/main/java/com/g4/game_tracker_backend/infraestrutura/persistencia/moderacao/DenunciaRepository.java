package com.g4.game_tracker_backend.infraestrutura.persistencia.moderacao;
import java.util.*;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.JpaRepository;
import com.g4.game_tracker_backend.dominio.moderacao.*;
public interface DenunciaRepository extends JpaRepository<DenunciaJpa,UUID> {
 Optional<DenunciaJpa> findByDenuncianteIdAndTipoAndConteudoIdAndStatus(UUID denunciante,TipoConteudo tipo,UUID conteudoId,StatusDenuncia status);
 List<DenunciaJpa> findByTipoAndConteudoIdAndStatus(TipoConteudo tipo,UUID conteudoId,StatusDenuncia status);
 Page<DenunciaJpa> findByStatusOrderByRegistradaEmAsc(StatusDenuncia status,Pageable page);
}
