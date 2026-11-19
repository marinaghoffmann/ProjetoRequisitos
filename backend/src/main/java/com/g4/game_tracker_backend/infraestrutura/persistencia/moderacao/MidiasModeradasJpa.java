package com.g4.game_tracker_backend.infraestrutura.persistencia.moderacao;

import java.util.Collection;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.g4.game_tracker_backend.aplicacao.moderacao.MidiasModeradas;
import com.g4.game_tracker_backend.dominio.moderacao.TipoConteudo;

@Component
class MidiasModeradasJpa implements MidiasModeradas {

    private final MidiaModeradaRepository springData;

    MidiasModeradasJpa(MidiaModeradaRepository springData) {
        this.springData = springData;
    }

    @Override
    public void proteger(TipoConteudo tipo, UUID conteudoId, Collection<String> urls) {
        for (String url : urls) {
            if (url != null && !url.isBlank() && !springData.existsByTipoAndConteudoIdAndUrl(tipo, conteudoId, url)) {
                springData.save(new MidiaModeradaJpa(tipo, conteudoId, url));
            }
        }
    }
}
