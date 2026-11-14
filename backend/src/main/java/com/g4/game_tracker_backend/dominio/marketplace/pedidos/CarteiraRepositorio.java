package com.g4.game_tracker_backend.dominio.marketplace.pedidos;

import java.util.Optional;
import java.util.UUID;

public interface CarteiraRepositorio {

    Carteira salvar(Carteira carteira);

    Optional<Carteira> buscarPorDono(UUID donoId);

    /** Trava a linha até o fim da transação, para débitos e créditos simultâneos não se sobrescreverem. */
    Optional<Carteira> buscarPorDonoParaAtualizar(UUID donoId);
}
