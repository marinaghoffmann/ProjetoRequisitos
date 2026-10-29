package com.g4.game_tracker_backend.infraestrutura.persistencia.anuncios;

import com.g4.game_tracker_backend.dominio.marketplace.anuncios.FotoAnuncio;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public class FotoAnuncioJpa {

    @Column(name = "url_foto", nullable = false, length = 1000)
    private String urlFoto;

    @Column(nullable = false)
    private int ordem;

    protected FotoAnuncioJpa() {
    }

    private FotoAnuncioJpa(String urlFoto, int ordem) {
        this.urlFoto = urlFoto;
        this.ordem = ordem;
    }

    static FotoAnuncioJpa deDominio(FotoAnuncio foto) {
        return new FotoAnuncioJpa(foto.urlFoto(), foto.ordem());
    }

    FotoAnuncio paraDominio() {
        return new FotoAnuncio(urlFoto, ordem);
    }

    int ordem() {
        return ordem;
    }
}
