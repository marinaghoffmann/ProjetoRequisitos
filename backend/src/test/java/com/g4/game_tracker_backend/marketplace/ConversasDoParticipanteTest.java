package com.g4.game_tracker_backend.marketplace;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.g4.game_tracker_backend.aplicacao.identidade.IdentidadeAutenticada;
import com.g4.game_tracker_backend.aplicacao.identidade.VerificadorDeAcesso;
import com.g4.game_tracker_backend.aplicacao.marketplace.negociacao.ConversasDoParticipante;
import com.g4.game_tracker_backend.dominio.compartilhado.AcessoNegadoException;
import com.g4.game_tracker_backend.dominio.identidade.Papel;
import com.g4.game_tracker_backend.dominio.marketplace.anuncios.StatusAnuncio;
import com.g4.game_tracker_backend.dominio.marketplace.anuncios.TipoNegociacao;
import com.g4.game_tracker_backend.dominio.marketplace.negociacao.AnuncioNegociavel;
import com.g4.game_tracker_backend.dominio.marketplace.negociacao.ConversaChat;
import com.g4.game_tracker_backend.dominio.marketplace.negociacao.ConversaChatRepositorio;

/** Padrão Proxy: o proxy de proteção aplica a F-19 RN 3 antes de entregar a conversa. */
class ConversasDoParticipanteTest {

    private static final Instant AGORA = Instant.parse("2026-11-10T12:00:00Z");

    private final UUID vendedor = UUID.randomUUID();
    private final UUID comprador = UUID.randomUUID();
    private final UUID intruso = UUID.randomUUID();
    private final RepositorioEmMemoria real = new RepositorioEmMemoria();
    private final ConversaChat conversa = real.salvar(ConversaChat.iniciar(new AnuncioNegociavel(UUID.randomUUID(),
            vendedor, UUID.randomUUID(), "Hollow Knight", new BigDecimal("150.00"), StatusAnuncio.ATIVO,
            TipoNegociacao.AMBOS), comprador, AGORA));

    @Test
    void comprador_e_vendedor_leem_a_conversa() {
        assertEquals(conversa.id(), proxyComo(comprador).buscarPorId(conversa.id()).orElseThrow().id());
        assertEquals(conversa.id(), proxyComo(vendedor).buscarPorId(conversa.id()).orElseThrow().id());
    }

    @Test
    void outro_usuario_e_barrado_antes_de_receber_a_conversa() {
        AcessoNegadoException erro = assertThrows(AcessoNegadoException.class,
                () -> proxyComo(intruso).buscarPorId(conversa.id()));
        assertEquals(ConversaChat.RESTRITA, erro.getMessage());
    }

    @Test
    void conversa_inexistente_continua_vazia_para_o_caso_de_uso_responder_404() {
        assertTrue(proxyComo(intruso).buscarPorId(UUID.randomUUID()).isEmpty());
    }

    @Test
    void ninguem_lista_as_conversas_de_outra_pessoa() {
        assertEquals(1, proxyComo(comprador).listarPorParticipante(comprador).size());
        assertThrows(AcessoNegadoException.class, () -> proxyComo(intruso).listarPorParticipante(comprador));
    }

    @Test
    void operacoes_do_sistema_passam_direto_para_o_repositorio_real() {
        assertEquals(1, proxyComo(intruso).listarPorAnuncio(conversa.anuncioId()).size());
    }

    private ConversasDoParticipante proxyComo(UUID usuarioId) {
        IdentidadeAutenticada identidade = new IdentidadeAutenticada() {
            @Override
            public UUID usuarioId() {
                return usuarioId;
            }

            @Override
            public Papel papel() {
                return Papel.COMUM;
            }
        };
        return new ConversasDoParticipante(real, new VerificadorDeAcesso(identidade));
    }

    private static final class RepositorioEmMemoria implements ConversaChatRepositorio {

        private final List<ConversaChat> conversas = new ArrayList<>();

        @Override
        public ConversaChat salvar(ConversaChat conversa) {
            conversas.removeIf(c -> c.id().equals(conversa.id()));
            conversas.add(conversa);
            return conversa;
        }

        @Override
        public Optional<ConversaChat> buscarPorId(UUID id) {
            return conversas.stream().filter(c -> c.id().equals(id)).findFirst();
        }

        @Override
        public Optional<ConversaChat> buscarPorAnuncioEComprador(UUID anuncioId, UUID compradorId) {
            return conversas.stream()
                    .filter(c -> c.anuncioId().equals(anuncioId) && c.compradorId().equals(compradorId))
                    .findFirst();
        }

        @Override
        public Optional<ConversaChat> buscarPorProposta(UUID propostaId) {
            return Optional.empty();
        }

        @Override
        public List<ConversaChat> listarPorParticipante(UUID usuarioId) {
            return conversas.stream().filter(c -> c.participante(usuarioId)).toList();
        }

        @Override
        public List<ConversaChat> listarPorAnuncio(UUID anuncioId) {
            return conversas.stream().filter(c -> c.anuncioId().equals(anuncioId)).toList();
        }

        @Override
        public List<ConversaChat> listarComReservaAceitaAte(Instant limite) {
            return List.of();
        }
    }
}
