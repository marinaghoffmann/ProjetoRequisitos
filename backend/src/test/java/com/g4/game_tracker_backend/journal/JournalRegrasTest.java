package com.g4.game_tracker_backend.journal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.g4.game_tracker_backend.dominio.compartilhado.RegraDeNegocioException;
import com.g4.game_tracker_backend.dominio.compartilhado.ConteudoOriginal;
import com.g4.game_tracker_backend.dominio.journal.EntradaDeJournal;
import com.g4.game_tracker_backend.dominio.journal.ImagemDoJournal;
import com.g4.game_tracker_backend.dominio.journal.SpoilerMascaradoDecorator;

class JournalRegrasTest {
    @Test
    void spoilerNaoEntregaTextoAntesDaRevelacao() {
        var original = new ConteudoOriginal("Texto secreto");
        assertNull(new SpoilerMascaradoDecorator(original, false).exibir());
        assertEquals("Texto secreto", new SpoilerMascaradoDecorator(original, true).exibir());
        assertEquals("Texto secreto", original.exibir());
    }

    @Test
    void tagsSaoNormalizadasSemRepeticoes() {
        var entrada = manual(List.of(" #Chefe ", "chefe", "Exploração", " "));
        assertEquals(List.of("chefe", "exploração"), entrada.tags());
    }

    @Test
    void setimaTagERecusada() {
        assertThrows(RegraDeNegocioException.class, () -> manual(List.of("a", "b", "c", "d", "e", "f", "g")));
    }

    @Test
    void conteudoHtmlNaoPodeSePassarPorPng() {
        assertThrows(RegraDeNegocioException.class, () -> new ImagemDoJournal("imagem.png", "image/png",
                "<script>alert(1)</script>".getBytes(StandardCharsets.UTF_8)));
    }

    @Test
    void imagemMaiorQueDoisMbERecusada() {
        assertThrows(RegraDeNegocioException.class, () -> new ImagemDoJournal("grande.png", "image/png",
                new byte[ImagemDoJournal.TAMANHO_MAXIMO + 1]));
    }

    private EntradaDeJournal manual(List<String> tags) {
        return EntradaDeJournal.criarManual(UUID.randomUUID(), null, Instant.now(), "Sessão", "Texto", tags,
                false, Instant.now());
    }
}
