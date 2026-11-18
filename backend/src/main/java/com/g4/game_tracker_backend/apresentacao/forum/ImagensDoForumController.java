package com.g4.game_tracker_backend.apresentacao.forum;

import java.io.IOException;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.g4.game_tracker_backend.aplicacao.forum.ArmazenarImagemForum;

/** Endpoint multipart para imagens do fórum; regras de arquivo ficam no domínio. */
@RestController
@RequestMapping("/api/forum")
public class ImagensDoForumController {
    private final ArmazenarImagemForum imagens;

    public ImagensDoForumController(ArmazenarImagemForum imagens) {
        this.imagens = imagens;
    }

    public record ImagemEnviada(String url) {}

    @PostMapping(value = "/imagens", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ImagemEnviada> enviar(@RequestParam("imagem") MultipartFile imagem) throws IOException {
        return ResponseEntity.status(201).body(new ImagemEnviada(imagens.executar(imagem.getBytes())));
    }
}
