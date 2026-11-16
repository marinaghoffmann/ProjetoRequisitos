package com.g4.game_tracker_backend.apresentacao.forum;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.g4.game_tracker_backend.aplicacao.identidade.VerificadorDeAcesso;
import com.g4.game_tracker_backend.dominio.compartilhado.RegraDeNegocioException;
import com.g4.game_tracker_backend.dominio.forum.ImagensDoForum;

/**
 * Envio de imagens para o fórum (multipart, campo "imagem"; PNG, JPG ou WEBP até 5 MB). O arquivo é
 * gravado em uploads/forum/ e a URL devolvida (/uploads/forum/...) é a que vai em "imagens" do tópico
 * ou da resposta. O tipo é conferido pelo conteúdo do arquivo, não pelo nome nem pelo Content-Type.
 */
@RestController
@RequestMapping("/api/forum")
public class ImagensDoForumController {

    private static final long TAMANHO_MAXIMO = 5L * 1024 * 1024;
    private static final Path DIRETORIO = Path.of("uploads", "forum");

    private final VerificadorDeAcesso verificadorDeAcesso;

    public ImagensDoForumController(VerificadorDeAcesso verificadorDeAcesso) {
        this.verificadorDeAcesso = verificadorDeAcesso;
    }

    public record ImagemEnviada(String url) {
    }

    @PostMapping(value = "/imagens", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ImagemEnviada> enviar(@RequestParam("imagem") MultipartFile imagem) throws IOException {
        verificadorDeAcesso.usuarioAtualId();
        if (imagem.isEmpty()) {
            throw new RegraDeNegocioException("Selecione uma imagem para enviar");
        }
        if (imagem.getSize() > TAMANHO_MAXIMO) {
            throw new RegraDeNegocioException("A imagem deve ter no máximo 5 MB");
        }
        byte[] conteudo = imagem.getBytes();
        String extensao = extensaoPelaAssinatura(conteudo);
        if (extensao == null) {
            throw new RegraDeNegocioException("Envie uma imagem PNG, JPG ou WEBP");
        }

        Files.createDirectories(DIRETORIO);
        String nome = UUID.randomUUID() + "." + extensao;
        Files.write(DIRETORIO.resolve(nome), conteudo);
        return ResponseEntity.status(201).body(new ImagemEnviada(ImagensDoForum.PREFIXO + nome));
    }

    private static String extensaoPelaAssinatura(byte[] b) {
        if (b.length >= 8 && (b[0] & 0xFF) == 0x89 && b[1] == 'P' && b[2] == 'N' && b[3] == 'G'
                && b[4] == 0x0D && b[5] == 0x0A && b[6] == 0x1A && b[7] == 0x0A) {
            return "png";
        }
        if (b.length >= 3 && (b[0] & 0xFF) == 0xFF && (b[1] & 0xFF) == 0xD8 && (b[2] & 0xFF) == 0xFF) {
            return "jpg";
        }
        if (b.length >= 12 && b[0] == 'R' && b[1] == 'I' && b[2] == 'F' && b[3] == 'F'
                && b[8] == 'W' && b[9] == 'E' && b[10] == 'B' && b[11] == 'P') {
            return "webp";
        }
        return null;
    }
}