package com.g4.game_tracker_backend.arquitetura;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.IllegalTransactionStateException;
import org.springframework.transaction.support.TransactionTemplate;

import com.g4.game_tracker_backend.aplicacao.journal.GerarEntradaAutomatica;
import com.g4.game_tracker_backend.aplicacao.wishlist.AdicionarNaWishlist;
import com.g4.game_tracker_backend.dominio.journal.TipoEventoPlataforma;

/**
 * Confere que as anotações Java puras da aplicação ({@code @CasoDeUso} e {@code @Transacional}) têm o mesmo
 * efeito que as do Spring tinham: o caso de uso vira bean, ganha o proxy de transação e respeita a propagação.
 */
@SpringBootTest
@ActiveProfiles("test")
class TransacaoDosCasosDeUsoTest {

    @Autowired
    AdicionarNaWishlist adicionarNaWishlist;

    @Autowired
    GerarEntradaAutomatica gerarEntradaAutomatica;

    @Autowired
    TransactionTemplate transacao;

    @Test
    void casoDeUsoAnotadoViraBeanComProxyDeTransacao() {
        assertTrue(AopUtils.isAopProxy(adicionarNaWishlist));
    }

    /** EXIGIR_EXISTENTE equivale ao MANDATORY: fora da ação de outro caso de uso, falha. */
    @Test
    void exigirTransacaoExistenteFalhaSemTransacaoAberta() {
        assertThrows(IllegalTransactionStateException.class, () -> gerarEntradaAutomatica
                .sincronizarExclusao(TipoEventoPlataforma.REVIEW_PUBLICADA, UUID.randomUUID().toString()));
    }

    @Test
    void exigirTransacaoExistenteParticipaDaTransacaoAberta() {
        String origem = UUID.randomUUID().toString();
        var entrada = transacao.execute(status -> gerarEntradaAutomatica.gerar(UUID.randomUUID(),
                UUID.randomUUID(), TipoEventoPlataforma.REVIEW_PUBLICADA, origem, Instant.now()));
        assertEquals(origem, entrada.origemId());
    }
}
