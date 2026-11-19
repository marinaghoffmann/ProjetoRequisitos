package com.g4.game_tracker_backend.aplicacao.moderacao;

import java.util.Collection;
import java.util.UUID;

import com.g4.game_tracker_backend.dominio.moderacao.TipoConteudo;

/**
 * Guarda os endereços dos arquivos do conteúdo moderado, para continuarem bloqueados mesmo que o autor
 * edite o conteúdo depois e o arquivo deixe de estar vinculado a ele.
 */
public interface MidiasModeradas {
    void proteger(TipoConteudo tipo, UUID conteudoId, Collection<String> urls);
}
