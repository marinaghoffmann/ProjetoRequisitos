package com.g4.game_tracker_backend.aplicacao.marketplace.anuncios;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.g4.game_tracker_backend.aplicacao.identidade.VerificadorDeAcesso;
import com.g4.game_tracker_backend.dominio.compartilhado.RecursoNaoEncontradoException;
import com.g4.game_tracker_backend.dominio.marketplace.anuncios.AnuncioMarketplace;
import com.g4.game_tracker_backend.dominio.marketplace.anuncios.AnuncioMarketplaceRepositorio;
import com.g4.game_tracker_backend.dominio.marketplace.anuncios.FiltroAnuncios;

/** Consultas da F-18: busca pública com filtros e listagem dos anúncios do próprio vendedor. */
@Service
public class ConsultarAnuncios {

    private final AnuncioMarketplaceRepositorio anuncios;
    private final VerificadorDeAcesso verificadorDeAcesso;

    public ConsultarAnuncios(AnuncioMarketplaceRepositorio anuncios, VerificadorDeAcesso verificadorDeAcesso) {
        this.anuncios = anuncios;
        this.verificadorDeAcesso = verificadorDeAcesso;
    }

    @Transactional(readOnly = true)
    public List<AnuncioMarketplace> buscar(FiltroAnuncios filtro) {
        return anuncios.buscarPublicos(filtro == null ? FiltroAnuncios.todos() : filtro);
    }

    @Transactional(readOnly = true)
    public AnuncioMarketplace buscarPorId(UUID id) {
        return anuncios.buscarPorId(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Anúncio não encontrado"));
    }

    @Transactional(readOnly = true)
    public List<AnuncioMarketplace> meusAnuncios() {
        return anuncios.listarPorVendedor(verificadorDeAcesso.usuarioAtualId());
    }
}
