package com.g4.game_tracker_backend.journal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.g4.game_tracker_backend.dominio.compartilhado.AcessoNegadoException;
import com.g4.game_tracker_backend.dominio.compartilhado.RegraDeNegocioException;
import com.g4.game_tracker_backend.dominio.journal.EntradaDeJournal;
import com.g4.game_tracker_backend.dominio.journal.TipoEventoPlataforma;
import com.g4.game_tracker_backend.dominio.journal.TipoOrigem;

class EntradaDeJournalTest {

    private static final Instant AGORA = Instant.parse("2026-11-09T12:00:00Z");
    private final UUID dono = UUID.randomUUID();

    @Test
    void entradaAutomaticaGuardaOEventoEAAcaoDeOrigem() {
        Instant zeradoEm = AGORA.minusSeconds(3600);
        EntradaDeJournal entrada = EntradaDeJournal.criarAutomatica(dono, UUID.randomUUID(),
                TipoEventoPlataforma.JOGO_ZERADO, "registro-1", "Zerou Celeste", zeradoEm, AGORA);

        assertEquals(TipoOrigem.AUTOMATICO, entrada.tipoOrigem());
        assertEquals(TipoEventoPlataforma.JOGO_ZERADO, entrada.tipoEvento());
        assertEquals("registro-1", entrada.origemId());
        assertEquals(zeradoEm, entrada.dataSessao());
        assertFalse(entrada.contemSpoiler());
    }

    @Test
    void entradaAutomaticaSemAcaoDeOrigemERecusada() {
        assertThrows(IllegalArgumentException.class, () -> EntradaDeJournal.criarAutomatica(dono, null,
                TipoEventoPlataforma.REVIEW_PUBLICADA, " ", "Publicou uma review", AGORA, AGORA));
    }

    @Test
    void entradaManualNaoTemEventoNemOrigem() {
        EntradaDeJournal entrada = EntradaDeJournal.criarManual(dono, null, AGORA, "  Primeira sessão  ", true,
                AGORA);

        assertEquals(TipoOrigem.MANUAL, entrada.tipoOrigem());
        assertNull(entrada.tipoEvento());
        assertNull(entrada.origemId());
        assertEquals("Primeira sessão", entrada.texto());
    }

    @Test
    void donoEditaOTextoDaEntradaManual() {
        EntradaDeJournal editada = EntradaDeJournal.criarManual(dono, null, AGORA, "Derrotei o Hornet", false, AGORA)
                .editar(dono, "Derrotei o Hornet na 3ª tentativa", true, AGORA.plusSeconds(60));

        assertEquals("Derrotei o Hornet na 3ª tentativa", editada.texto());
        assertTrue(editada.contemSpoiler());
        assertEquals(AGORA.plusSeconds(60), editada.atualizadaEm());
    }

    @Test
    void textoDaEntradaAutomaticaNaoPodeSerEditado() {
        EntradaDeJournal entrada = EntradaDeJournal.criarAutomatica(dono, null, TipoEventoPlataforma.REVIEW_PUBLICADA,
                "review-1", "Publicou uma review de Hades", AGORA, AGORA);

        RegraDeNegocioException erro = assertThrows(RegraDeNegocioException.class,
                () -> entrada.editar(dono, "Outro texto", false, AGORA));
        assertEquals("Entradas automáticas não podem ser editadas", erro.getMessage());
    }

    @Test
    void outroUsuarioNaoEditaAEntrada() {
        EntradaDeJournal entrada = EntradaDeJournal.criarManual(dono, null, AGORA, "Texto", false, AGORA);

        AcessoNegadoException erro = assertThrows(AcessoNegadoException.class,
                () -> entrada.editar(UUID.randomUUID(), "Invasão", false, AGORA));
        assertEquals("Apenas o dono pode alterar esta entrada", erro.getMessage());
    }

    @Test
    void textoEmBrancoERecusado() {
        assertThrows(RegraDeNegocioException.class,
                () -> EntradaDeJournal.criarManual(dono, null, AGORA, "   ", false, AGORA));
    }
}
