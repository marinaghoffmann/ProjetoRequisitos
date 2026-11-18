package com.g4.game_tracker_backend.dominio.compartilhado;

/**
 * Porta para publicar eventos de domínio (padrão Observer: quem publica não conhece os observadores).
 *
 * <p>A implementação fica na infraestrutura. Os observadores recebem o evento na mesma thread e na mesma
 * transação de quem publicou, então uma falha no observador desfaz também a ação que gerou o evento
 * (F-16 RN 1).
 */
public interface PublicadorDeEventos {

    void publicar(Object evento);
}
