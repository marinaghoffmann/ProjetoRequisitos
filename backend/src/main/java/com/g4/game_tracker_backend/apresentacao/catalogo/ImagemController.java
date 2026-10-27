package com.g4.game_tracker_backend.apresentacao.catalogo;

import java.io.IOException;
import java.util.Map;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.g4.game_tracker_backend.aplicacao.catalogo.ArmazenarImagem;

@RestController
@RequestMapping("/api/catalogo/imagens")
public class ImagemController {

    private final ArmazenarImagem armazenarImagem;

    public ImagemController(ArmazenarImagem armazenarImagem) {
        this.armazenarImagem = armazenarImagem;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Map<String, String> enviar(@RequestPart("imagem") MultipartFile imagem) {
        try {
            String caminho = armazenarImagem.executar(imagem.getBytes(), imagem.getOriginalFilename(),
                    imagem.getContentType());
            return Map.of("caminho", caminho);
        } catch (IOException e) {
            throw new IllegalStateException("Não foi possível ler a imagem enviada", e);
        }
    }
}
