package com.g4.game_tracker_backend.infraestrutura.persistencia.biblioteca.acervo;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.g4.game_tracker_backend.dominio.biblioteca.acervo.CatalogoDeJogos;
import com.g4.game_tracker_backend.dominio.catalogo.JogoRepositorio;

/** Adapta o catálogo (F-01) à porta da biblioteca. */
@Component
public class CatalogoDeJogosAdapter implements CatalogoDeJogos {

    private final JogoRepositorio jogos;

    public CatalogoDeJogosAdapter(JogoRepositorio jogos) {
        this.jogos = jogos;
    }

    @Override
    public Optional<JogoResumo> buscarResumo(UUID jogoId) {
        return jogos.buscarPorId(jogoId)
                .map(j -> new JogoResumo(j.id(), j.titulo(), j.capaUrl()));
    }

    @Override
    public List<JogoResumo> buscarResumos(Collection<UUID> jogoIds) {
        return jogoIds.stream().map(this::buscarResumo).flatMap(Optional::stream).toList();
    }
}