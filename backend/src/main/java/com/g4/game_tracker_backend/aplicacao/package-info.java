/**
 * Camada de APLICAÇÃO: casos de uso que orquestram o domínio (um serviço por fluxo,
 * por exemplo PublicarReview, IniciarConversa). Abre a transação, carrega agregados
 * pelos repositórios do domínio e publica eventos. Não conhece controllers nem JPA.
 *
 * <p>Meta: Java puro, com transação e eventos pelas portas ExecutorDeTransacao e
 * PublicadorDeEventos e wiring em infraestrutura.configuracao (já assim em
 * marketplace.negociacao e marketplace.pedidos, verificado pelo ArquiteturaTest).
 * Os demais contextos ainda usam as anotações de estereótipo e transação do Spring.
 */
package com.g4.game_tracker_backend.aplicacao;
