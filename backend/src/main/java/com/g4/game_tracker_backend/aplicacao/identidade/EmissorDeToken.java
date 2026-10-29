package com.g4.game_tracker_backend.aplicacao.identidade;

import com.g4.game_tracker_backend.dominio.identidade.Usuario;

/** Porta para emissão de credenciais de acesso assinadas. */
public interface EmissorDeToken {

    TokenDeAcesso emitir(Usuario usuario);
}
