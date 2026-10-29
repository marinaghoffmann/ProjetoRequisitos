package com.g4.game_tracker_backend.apresentacao.catalogo;

import java.util.UUID;

import com.g4.game_tracker_backend.dominio.catalogo.Desenvolvedora;

public record DesenvolvedoraResponse(UUID id, String nome, String biografia, String site, String redesSociais,
        String pais, boolean ativa) {

    static DesenvolvedoraResponse de(Desenvolvedora desenvolvedora) {
        return new DesenvolvedoraResponse(desenvolvedora.id(), desenvolvedora.nome(), desenvolvedora.biografia(),
                desenvolvedora.site(), desenvolvedora.redesSociais(), desenvolvedora.pais(), desenvolvedora.ativa());
    }
}
