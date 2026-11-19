package com.g4.game_tracker_backend.infraestrutura.persistencia.moderacao;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.g4.game_tracker_backend.dominio.moderacao.TipoConteudo;

public interface MidiaModeradaRepository extends JpaRepository<MidiaModeradaJpa, UUID> {

    List<MidiaModeradaJpa> findByUrl(String url);

    boolean existsByTipoAndConteudoIdAndUrl(TipoConteudo tipo, UUID conteudoId, String url);
}
