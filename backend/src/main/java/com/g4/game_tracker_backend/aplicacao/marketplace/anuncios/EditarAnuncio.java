package com.g4.game_tracker_backend.aplicacao.marketplace.anuncios;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.g4.game_tracker_backend.aplicacao.identidade.VerificadorDeAcesso;
import com.g4.game_tracker_backend.dominio.marketplace.anuncios.AnuncioMarketplace;
import com.g4.game_tracker_backend.dominio.marketplace.anuncios.AnuncioMarketplaceRepositorio;
import com.g4.game_tracker_backend.dominio.marketplace.anuncios.EstadoConservacao;
import com.g4.game_tracker_backend.dominio.marketplace.anuncios.MidiaJogo;
import com.g4.game_tracker_backend.dominio.marketplace.anuncios.TipoNegociacao;

/** F-18 RN 2 e RN 3: só o proprietário edita o anúncio, e o preço continua precisando ser positivo. */
@Service
public class EditarAnuncio {

    private final AnuncioMarketplaceRepositorio anuncios;
    private final VerificadorDeAcesso verificadorDeAcesso;

    public EditarAnuncio(AnuncioMarketplaceRepositorio anuncios, VerificadorDeAcesso verificadorDeAcesso) {
        this.anuncios = anuncios;
        this.verificadorDeAcesso = verificadorDeAcesso;
    }

    @Transactional
    public AnuncioMarketplace executar(UUID id, String titulo, EstadoConservacao estado, BigDecimal preco,
            TipoNegociacao tipoNegociacao, MidiaJogo midia, String localidade, List<String> fotos) {
        AnuncioMarketplace atual = AcessoAoAnuncio.doProprietario(anuncios, verificadorDeAcesso, id);
        return anuncios.salvar(atual.editar(titulo, estado, preco, tipoNegociacao, midia, localidade, fotos,
                Instant.now()));
    }

    /** Atalho para alterar apenas o preço, mantendo os demais dados. */
    @Transactional
    public AnuncioMarketplace alterarPreco(UUID id, BigDecimal preco) {
        AnuncioMarketplace atual = AcessoAoAnuncio.doProprietario(anuncios, verificadorDeAcesso, id);
        return anuncios.salvar(atual.editar(atual.titulo(), atual.estado(), preco, atual.tipoNegociacao(),
                atual.midia(), atual.localidade(), atual.fotos().stream().map(foto -> foto.urlFoto()).toList(),
                Instant.now()));
    }
}
