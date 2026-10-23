/**
 * Camada de DOMÍNIO: entidades, objetos de valor, agregados, eventos de domínio e
 * interfaces de repositório (portas). É Java puro: não pode depender de Spring, JPA
 * nem das outras camadas (verificado em ArquiteturaTest).
 *
 * Um subpacote por contexto delimitado do modelo CML, por exemplo:
 * catalogo, avaliacao, estatisticas, biblioteca, similaridade, diario, forum,
 * moderacao, marketplace.anuncios, marketplace.negociacao, marketplace.pedidos,
 * marketplace.reputacao, identidade, notificacao. Código usado por vários contextos
 * fica em compartilhado.
 */
package com.g4.game_tracker_backend.dominio;
