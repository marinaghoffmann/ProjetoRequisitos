package com.g4.game_tracker_backend.apresentacao.marketplace.anuncios;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import com.g4.game_tracker_backend.dominio.marketplace.anuncios.EstadoConservacao;
import com.g4.game_tracker_backend.dominio.marketplace.anuncios.MidiaJogo;
import com.g4.game_tracker_backend.dominio.marketplace.anuncios.TipoNegociacao;

import jakarta.validation.constraints.Size;

/**
 * Dados do anúncio. Na edição, jogoId é ignorado: o anúncio continua vinculado ao mesmo jogo.
 * Quando o título vem vazio, o anúncio usa o título do jogo no catálogo.
 * As regras (preço positivo, ao menos 1 foto...) ficam no domínio, para a mensagem chegar ao usuário.
 */
public record AnuncioRequest(
        UUID jogoId,
        @Size(max = 180) String titulo,
        EstadoConservacao estado,
        BigDecimal preco,
        TipoNegociacao tipoNegociacao,
        MidiaJogo midia,
        @Size(max = 120) String localidade,
        List<String> fotos) {
}
