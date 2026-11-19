package com.g4.game_tracker_backend.dominio.catalogo;

import java.util.UUID;

/**
 * Porta da F-01 RN 3: diz se algum outro contexto guarda dados ligados ao jogo (reviews, journal, listas,
 * wishlist, conclusões, anúncios, pedidos, fórum, lançamentos, favoritos, jogos em andamento, similaridades).
 *
 * O catálogo é upstream no Context Map e não conhece esses contextos: a implementação fica na infraestrutura,
 * que consulta as tabelas de cada um.
 */
public interface VinculosDoJogo {

    boolean possuiVinculos(UUID jogoId);
}
