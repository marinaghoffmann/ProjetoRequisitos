package com.g4.game_tracker_backend.forum;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import com.g4.game_tracker_backend.dominio.compartilhado.RegraDeNegocioException;
import com.g4.game_tracker_backend.dominio.forum.ImagensDoForum;

class ImagensDoForumTest {
    @Test
    void identificaAssinaturaRealEmVezDoNomeOuContentType() {
        assertEquals("png", ImagensDoForum.validarArquivo(new byte[] {(byte) 0x89, 'P', 'N', 'G', 13, 10, 26, 10}));
        assertEquals("jpg", ImagensDoForum.validarArquivo(new byte[] {(byte) 0xff, (byte) 0xd8, (byte) 0xff}));
        assertEquals("webp", ImagensDoForum.validarArquivo(new byte[] {'R','I','F','F',0,0,0,0,'W','E','B','P'}));
        assertEquals("Envie uma imagem PNG, JPG ou WEBP", assertThrows(RegraDeNegocioException.class,
                () -> ImagensDoForum.validarArquivo("arquivo.pdf".getBytes())).getMessage());
    }

    @Test
    void recusaArquivoVazioEOQueExcedeCincoMegabytes() {
        assertEquals("Selecione uma imagem para enviar", assertThrows(RegraDeNegocioException.class,
                () -> ImagensDoForum.validarArquivo(new byte[0])).getMessage());
        assertEquals("A imagem deve ter no máximo 5 MB", assertThrows(RegraDeNegocioException.class,
                () -> ImagensDoForum.validarArquivo(new byte[(int) ImagensDoForum.TAMANHO_MAXIMO_ARQUIVO + 1])).getMessage());
    }
}
