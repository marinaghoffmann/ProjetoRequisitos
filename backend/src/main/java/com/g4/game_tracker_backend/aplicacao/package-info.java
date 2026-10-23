/**
 * Camada de APLICAÇÃO: casos de uso que orquestram o domínio (um serviço por fluxo,
 * por exemplo PublicarReview, IniciarConversa). Abre a transação, carrega agregados
 * pelos repositórios do domínio e publica eventos. Pode usar anotações de estereótipo
 * e transação do Spring, mas não conhece controllers nem JPA.
 */
package com.g4.game_tracker_backend.aplicacao;
