package com.g4.game_tracker_backend.aplicacao.marketplace.pedidos;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.g4.game_tracker_backend.aplicacao.identidade.VerificadorDeAcesso;
import com.g4.game_tracker_backend.dominio.marketplace.anuncios.AnuncioMarketplace;
import com.g4.game_tracker_backend.dominio.marketplace.anuncios.FotoAnuncio;
import com.g4.game_tracker_backend.dominio.marketplace.pedidos.OrigemPedido;

/**
 * Prévia do checkout: confere a origem do pedido (RN 1 e RN 2) sem cobrar nada e devolve o que a tela precisa
 * para montar os dois passos: item, valor, prazo da reserva e saldo da carteira.
 */
@Service
public class PrepararCheckout {

    private final OrigemDoPedido origens;
    private final MovimentarCarteira carteira;
    private final VerificadorDeAcesso verificadorDeAcesso;

    public PrepararCheckout(OrigemDoPedido origens, MovimentarCarteira carteira,
            VerificadorDeAcesso verificadorDeAcesso) {
        this.origens = origens;
        this.carteira = carteira;
        this.verificadorDeAcesso = verificadorDeAcesso;
    }

    @Transactional
    public ResumoCheckout executar(UUID anuncioId, UUID propostaId) {
        UUID compradorId = verificadorDeAcesso.usuarioAtualId();
        OrigemDoPedido.Resolvida origem = origens.resolver(anuncioId, propostaId, compradorId, Instant.now());
        AnuncioMarketplace anuncio = origem.anuncio();
        return new ResumoCheckout(origem.origem(), anuncio.id(), origem.propostaId(), anuncio.titulo(),
                anuncio.fotos().stream().findFirst().map(FotoAnuncio::urlFoto).orElse(null), origem.valorItem(),
                origem.jogoOferecidoEmTroca(), origem.prazoParaPagar(), carteira.saldo(compradorId));
    }

    public record ResumoCheckout(OrigemPedido origem, UUID anuncioId, UUID propostaId, String tituloAnuncio,
            String fotoAnuncio, BigDecimal valorItem, String jogoOferecidoEmTroca, Instant prazoParaPagar,
            BigDecimal saldoCarteira) {
    }
}
