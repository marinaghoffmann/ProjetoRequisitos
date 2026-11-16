package com.g4.game_tracker_backend.dominio.descoberta.similaridade;

/**
 * RN 3: uma conexão que o administrador removeu fica REMOVIDA, e não é apagada, para o recálculo automático
 * não a criar de novo. Só uma nova conexão manual a reativa.
 */
public enum StatusSimilaridade {
    ATIVA,
    REMOVIDA
}
