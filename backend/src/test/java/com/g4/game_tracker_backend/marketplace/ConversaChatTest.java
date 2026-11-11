package com.g4.game_tracker_backend.marketplace;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.g4.game_tracker_backend.dominio.compartilhado.AcessoNegadoException;
import com.g4.game_tracker_backend.dominio.compartilhado.RegraDeNegocioException;
import com.g4.game_tracker_backend.dominio.marketplace.anuncios.StatusAnuncio;
import com.g4.game_tracker_backend.dominio.marketplace.anuncios.TipoNegociacao;
import com.g4.game_tracker_backend.dominio.marketplace.negociacao.AnuncioNegociavel;
import com.g4.game_tracker_backend.dominio.marketplace.negociacao.ConversaChat;
import com.g4.game_tracker_backend.dominio.marketplace.negociacao.PropostaNegociacao;
import com.g4.game_tracker_backend.dominio.marketplace.negociacao.StatusProposta;
import com.g4.game_tracker_backend.dominio.marketplace.negociacao.TipoProposta;

class ConversaChatTest {

    private static final Instant AGORA = Instant.parse("2026-11-10T12:00:00Z");

    private final UUID vendedor = UUID.randomUUID();
    private final UUID comprador = UUID.randomUUID();
    private final UUID anuncioId = UUID.randomUUID();

    @Test
    void naoNegociaOProprioAnuncioNemAnuncioPausado() {
        RegraDeNegocioException proprio = assertThrows(RegraDeNegocioException.class,
                () -> ConversaChat.iniciar(anuncio(StatusAnuncio.ATIVO), vendedor, AGORA));
        assertEquals("Você não pode negociar o próprio anúncio", proprio.getMessage());

        RegraDeNegocioException pausado = assertThrows(RegraDeNegocioException.class,
                () -> ConversaChat.iniciar(anuncio(StatusAnuncio.PAUSADO), comprador, AGORA));
        assertEquals("Só é possível negociar anúncios ativos", pausado.getMessage());
    }

    @Test
    void terceiroNaoEnviaNemLeMensagens() {
        ConversaChat conversa = conversa();
        UUID terceiro = UUID.randomUUID();

        AcessoNegadoException erro = assertThrows(AcessoNegadoException.class,
                () -> conversa.enviarMensagem(terceiro, "Oi", anuncio(StatusAnuncio.ATIVO), AGORA));
        assertEquals(ConversaChat.RESTRITA, erro.getMessage());
        assertThrows(AcessoNegadoException.class, () -> conversa.exigirParticipante(terceiro));
    }

    @Test
    void mensagemPrecisaDeTextoDeAte1000Caracteres() {
        ConversaChat conversa = conversa();

        assertEquals("A mensagem não pode ser vazia", assertThrows(RegraDeNegocioException.class,
                () -> conversa.enviarMensagem(comprador, "  ", anuncio(StatusAnuncio.ATIVO), AGORA)).getMessage());
        assertEquals("A mensagem deve ter no máximo 1000 caracteres", assertThrows(RegraDeNegocioException.class,
                () -> conversa.enviarMensagem(comprador, "a".repeat(1001), anuncio(StatusAnuncio.ATIVO), AGORA))
                .getMessage());
        assertEquals(1, conversa.enviarMensagem(comprador, "a".repeat(1000), anuncio(StatusAnuncio.ATIVO), AGORA)
                .mensagens().size());
    }

    @Test
    void conversaDeAnuncioVendidoFicaSomenteLeitura() {
        RegraDeNegocioException erro = assertThrows(RegraDeNegocioException.class,
                () -> conversa().enviarMensagem(comprador, "Ainda tem?", anuncio(StatusAnuncio.VENDIDO), AGORA));
        assertEquals(ConversaChat.SOMENTE_LEITURA, erro.getMessage());
    }

    @Test
    void abrirAConversaMarcaComoLidasSoAsRecebidas() {
        ConversaChat conversa = conversa()
                .enviarMensagem(comprador, "Oi", anuncio(StatusAnuncio.ATIVO), AGORA)
                .enviarMensagem(comprador, "Ainda disponível?", anuncio(StatusAnuncio.ATIVO), AGORA.plusSeconds(1))
                .enviarMensagem(vendedor, "Sim", anuncio(StatusAnuncio.ATIVO), AGORA.plusSeconds(2));

        assertEquals(2, conversa.naoLidasPor(vendedor));
        assertEquals(1, conversa.naoLidasPor(comprador));

        ConversaChat lida = conversa.marcarComoLidas(vendedor, AGORA.plusSeconds(3));
        assertEquals(0, lida.naoLidasPor(vendedor));
        assertEquals(1, lida.naoLidasPor(comprador));
    }

    @Test
    void propostaNascePendenteEPermiteSoUmaPorVez() {
        ConversaChat conversa = proporValor(conversa(), "120.00");

        assertEquals(StatusProposta.PENDENTE, conversa.ultimaProposta().orElseThrow().status());
        RegraDeNegocioException erro = assertThrows(RegraDeNegocioException.class, () -> proporValor(conversa, "130.00"));
        assertEquals("Já existe uma proposta pendente nesta conversa", erro.getMessage());
    }

    @Test
    void validaValorETrocaDaProposta() {
        assertEquals("O valor da proposta deve ser maior que zero",
                assertThrows(RegraDeNegocioException.class, () -> proporValor(conversa(), "0")).getMessage());
        assertEquals("O jogo oferecido na troca deve ter de 2 a 80 caracteres",
                assertThrows(RegraDeNegocioException.class, () -> conversa().enviarProposta(comprador,
                        TipoProposta.TROCA, null, "X", null, anuncio(StatusAnuncio.ATIVO), AGORA)).getMessage());

        ConversaChat troca = conversa().enviarProposta(comprador, TipoProposta.TROCA, null, "Celeste", "Topa?",
                anuncio(StatusAnuncio.ATIVO), AGORA);
        PropostaNegociacao proposta = troca.ultimaProposta().orElseThrow();
        assertEquals(TipoProposta.TROCA, proposta.tipo());
        assertEquals("Celeste", proposta.jogoOferecido());
        assertEquals("Topa?", troca.ultimaMensagem().orElseThrow().texto());
    }

    @Test
    void anuncioSoDeVendaNaoAceitaTroca() {
        AnuncioNegociavel soVenda = new AnuncioNegociavel(anuncioId, vendedor, UUID.randomUUID(), "Hades",
                new BigDecimal("150.00"), StatusAnuncio.ATIVO, TipoNegociacao.VENDA);
        RegraDeNegocioException erro = assertThrows(RegraDeNegocioException.class, () -> conversa()
                .enviarProposta(comprador, TipoProposta.TROCA, null, "Celeste", null, soVenda, AGORA));
        assertEquals("Este anúncio não aceita trocas", erro.getMessage());
    }

    @Test
    void soOVendedorRespondeESoOCompradorCancela() {
        ConversaChat conversa = proporValor(conversa(), "120.00");
        UUID propostaId = conversa.ultimaProposta().orElseThrow().id();

        assertEquals("Apenas o vendedor pode aceitar ou recusar a proposta", assertThrows(RegraDeNegocioException.class,
                () -> conversa.aceitarProposta(comprador, propostaId, anuncio(StatusAnuncio.ATIVO), AGORA)).getMessage());
        assertEquals("Apenas o comprador pode cancelar a proposta", assertThrows(RegraDeNegocioException.class,
                () -> conversa.cancelarProposta(vendedor, propostaId, AGORA)).getMessage());
        assertEquals("Apenas o comprador envia propostas", assertThrows(RegraDeNegocioException.class,
                () -> conversa().enviarProposta(vendedor, TipoProposta.VALOR, new BigDecimal("10.00"), null, null,
                        anuncio(StatusAnuncio.ATIVO), AGORA)).getMessage());

        ConversaChat cancelada = conversa.cancelarProposta(comprador, propostaId, AGORA);
        assertEquals(StatusProposta.CANCELADA, cancelada.proposta(propostaId).status());
    }

    @Test
    void propostaRespondidaNaoMudaMais() {
        ConversaChat conversa = proporValor(conversa(), "120.00");
        UUID propostaId = conversa.ultimaProposta().orElseThrow().id();
        ConversaChat recusada = conversa.recusarProposta(vendedor, propostaId, AGORA);

        RegraDeNegocioException erro = assertThrows(RegraDeNegocioException.class,
                () -> recusada.aceitarProposta(vendedor, propostaId, anuncio(StatusAnuncio.ATIVO), AGORA));
        assertEquals("A proposta não está mais pendente", erro.getMessage());
    }

    @Test
    void reservaExpiraDepoisDe48HorasSemPedido() {
        ConversaChat aceita = aceitar(proporValor(conversa(), "120.00"));
        PropostaNegociacao proposta = aceita.ultimaProposta().orElseThrow();
        assertEquals(AGORA.plus(Duration.ofHours(48)), proposta.prazoParaPagar());

        assertSame(aceita, aceita.expirarReservaVencida(AGORA.plus(Duration.ofHours(47))));
        PropostaNegociacao expirada = aceita.expirarReservaVencida(AGORA.plus(Duration.ofHours(49)))
                .proposta(proposta.id());
        assertEquals(StatusProposta.EXPIRADA, expirada.status());
        assertEquals(PropostaNegociacao.PRAZO_ENCERRADO, expirada.motivo());
    }

    @Test
    void pedidoGeradoImpedeExpiracaoELiberacao() {
        ConversaChat aceita = aceitar(proporValor(conversa(), "120.00"));
        UUID propostaId = aceita.ultimaProposta().orElseThrow().id();
        ConversaChat comPedido = aceita.vincularPedido(propostaId, UUID.randomUUID());

        assertSame(comPedido, comPedido.expirarReservaVencida(AGORA.plus(Duration.ofDays(5))));
        RegraDeNegocioException erro = assertThrows(RegraDeNegocioException.class,
                () -> comPedido.liberarReserva(vendedor, propostaId, AGORA));
        assertEquals("O pedido desta proposta já foi gerado", erro.getMessage());
    }

    @Test
    void vendedorLiberaAReservaAntesDoPrazo() {
        ConversaChat aceita = aceitar(proporValor(conversa(), "120.00"));
        UUID propostaId = aceita.ultimaProposta().orElseThrow().id();

        PropostaNegociacao liberada = aceita.liberarReserva(vendedor, propostaId, AGORA).proposta(propostaId);
        assertEquals(StatusProposta.EXPIRADA, liberada.status());
        assertEquals(PropostaNegociacao.RESERVA_LIBERADA, liberada.motivo());
    }

    @Test
    void reservaDeOutroCompradorRecusaAPendenteComAviso() {
        ConversaChat conversa = proporValor(conversa(), "100.00");
        PropostaNegociacao recusada = conversa.recusarPorReservaDeOutroComprador(AGORA).ultimaProposta().orElseThrow();

        assertEquals(StatusProposta.RECUSADA, recusada.status());
        assertEquals(PropostaNegociacao.RESERVADO_PARA_OUTRO, recusada.motivo());
        assertTrue(conversa().recusarPorReservaDeOutroComprador(AGORA).propostas().isEmpty());
    }

    private ConversaChat conversa() {
        return ConversaChat.iniciar(anuncio(StatusAnuncio.ATIVO), comprador, AGORA);
    }

    private ConversaChat proporValor(ConversaChat conversa, String valor) {
        return conversa.enviarProposta(comprador, TipoProposta.VALOR, new BigDecimal(valor), null, null,
                anuncio(StatusAnuncio.ATIVO), AGORA);
    }

    private ConversaChat aceitar(ConversaChat conversa) {
        return conversa.aceitarProposta(vendedor, conversa.ultimaProposta().orElseThrow().id(),
                anuncio(StatusAnuncio.ATIVO), AGORA);
    }

    private AnuncioNegociavel anuncio(StatusAnuncio status) {
        return new AnuncioNegociavel(anuncioId, vendedor, UUID.randomUUID(), "Hollow Knight", new BigDecimal("150.00"),
                status, TipoNegociacao.AMBOS);
    }
}
