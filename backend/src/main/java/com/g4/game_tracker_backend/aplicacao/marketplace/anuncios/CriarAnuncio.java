package com.g4.game_tracker_backend.aplicacao.marketplace.anuncios;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.g4.game_tracker_backend.aplicacao.identidade.VerificadorDeAcesso;
import com.g4.game_tracker_backend.dominio.catalogo.Jogo;
import com.g4.game_tracker_backend.dominio.catalogo.JogoRepositorio;
import com.g4.game_tracker_backend.dominio.catalogo.StatusCatalogo;
import com.g4.game_tracker_backend.dominio.compartilhado.RegraDeNegocioException;
import com.g4.game_tracker_backend.dominio.marketplace.anuncios.AnuncioMarketplace;
import com.g4.game_tracker_backend.dominio.marketplace.anuncios.AnuncioMarketplaceRepositorio;
import com.g4.game_tracker_backend.dominio.marketplace.anuncios.EstadoConservacao;
import com.g4.game_tracker_backend.dominio.marketplace.anuncios.MidiaJogo;
import com.g4.game_tracker_backend.dominio.marketplace.anuncios.TipoNegociacao;

/** F-18 RN 1: qualquer usuário autenticado anuncia um jogo ATIVO do catálogo, com ao menos 1 foto. */
@Service
public class CriarAnuncio {

    private final AnuncioMarketplaceRepositorio anuncios;
    private final JogoRepositorio jogos;
    private final VerificadorDeAcesso verificadorDeAcesso;

    public CriarAnuncio(AnuncioMarketplaceRepositorio anuncios, JogoRepositorio jogos,
            VerificadorDeAcesso verificadorDeAcesso) {
        this.anuncios = anuncios;
        this.jogos = jogos;
        this.verificadorDeAcesso = verificadorDeAcesso;
    }

    @Transactional
    public AnuncioMarketplace executar(UUID jogoId, String titulo, EstadoConservacao estado, BigDecimal preco,
            TipoNegociacao tipoNegociacao, MidiaJogo midia, String localidade, List<String> fotos) {
        UUID vendedorId = verificadorDeAcesso.usuarioAtualId();
        Jogo jogo = (jogoId == null ? Optional.<Jogo>empty() : jogos.buscarPorId(jogoId))
                .filter(encontrado -> encontrado.status() == StatusCatalogo.ATIVO)
                .orElseThrow(() -> new RegraDeNegocioException("O jogo precisa estar cadastrado no catálogo"));
        String tituloDoAnuncio = titulo == null || titulo.isBlank() ? jogo.titulo() : titulo;
        return anuncios.salvar(AnuncioMarketplace.criar(vendedorId, jogo.id(), tituloDoAnuncio, estado, preco,
                tipoNegociacao, midia, localidade, fotos, Instant.now()));
    }
}
