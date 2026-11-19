package com.g4.game_tracker_backend.aplicacao.marketplace.anuncios;

import java.util.List;
import java.util.UUID;

import com.g4.game_tracker_backend.aplicacao.identidade.VerificadorDeAcesso;
import com.g4.game_tracker_backend.dominio.compartilhado.RecursoNaoEncontradoException;
import com.g4.game_tracker_backend.dominio.marketplace.anuncios.AnuncioMarketplace;
import com.g4.game_tracker_backend.dominio.marketplace.anuncios.AnuncioMarketplaceRepositorio;
import com.g4.game_tracker_backend.dominio.marketplace.anuncios.FiltroAnuncios;
import com.g4.game_tracker_backend.aplicacao.compartilhado.CasoDeUso;
import com.g4.game_tracker_backend.aplicacao.compartilhado.Transacional;

/** Consultas da F-18: busca pública com filtros e listagem dos anúncios do próprio vendedor. */
@CasoDeUso
public class ConsultarAnuncios {

    private final AnuncioMarketplaceRepositorio anuncios;
    private final VerificadorDeAcesso verificadorDeAcesso;

    public ConsultarAnuncios(AnuncioMarketplaceRepositorio anuncios, VerificadorDeAcesso verificadorDeAcesso) {
        this.anuncios = anuncios;
        this.verificadorDeAcesso = verificadorDeAcesso;
    }

    @Transacional(somenteLeitura = true)
    public List<AnuncioMarketplace> buscar(FiltroAnuncios filtro) {
        return anuncios.buscarPublicos(filtro == null ? FiltroAnuncios.todos() : filtro);
    }

    @Transacional(somenteLeitura = true)
    public AnuncioMarketplace buscarPorId(UUID id) {
        return anuncios.buscarPorId(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Anúncio não encontrado"));
    }

    @Transacional(somenteLeitura = true)
    public List<AnuncioMarketplace> meusAnuncios() {
        return anuncios.listarPorVendedor(verificadorDeAcesso.usuarioAtualId());
    }
}
