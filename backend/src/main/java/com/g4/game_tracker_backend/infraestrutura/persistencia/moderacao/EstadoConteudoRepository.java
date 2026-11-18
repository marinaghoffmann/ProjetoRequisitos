package com.g4.game_tracker_backend.infraestrutura.persistencia.moderacao;
import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;
import com.g4.game_tracker_backend.dominio.moderacao.TipoConteudo;
public interface EstadoConteudoRepository extends JpaRepository<EstadoConteudoJpa,UUID> { Optional<EstadoConteudoJpa> findByTipoAndConteudoId(TipoConteudo tipo,UUID conteudoId);
 long countByTipoAndConteudoIdInAndOcultoTrueOrTipoAndConteudoIdInAndRemovidoTrue(TipoConteudo tipo,java.util.Collection<UUID> ids,TipoConteudo tipo2,java.util.Collection<UUID> ids2); }
