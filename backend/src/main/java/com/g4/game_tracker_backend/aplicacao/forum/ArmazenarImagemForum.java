package com.g4.game_tracker_backend.aplicacao.forum;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

import com.g4.game_tracker_backend.aplicacao.identidade.VerificadorDeAcesso;
import com.g4.game_tracker_backend.dominio.compartilhado.RegraDeNegocioException;
import com.g4.game_tracker_backend.dominio.forum.ImagensDoForum;
import com.g4.game_tracker_backend.aplicacao.compartilhado.CasoDeUso;

/** Aplica as regras de upload do fórum antes de gravar o arquivo. */
@CasoDeUso
public class ArmazenarImagemForum {
    private static final Path DIRETORIO = Path.of("uploads", "forum");
    private final VerificadorDeAcesso acesso;

    public ArmazenarImagemForum(VerificadorDeAcesso acesso) {
        this.acesso = acesso;
    }

    public String executar(byte[] conteudo) throws IOException {
        acesso.usuarioAtualId();
        String extensao = ImagensDoForum.validarArquivo(conteudo);
        Files.createDirectories(DIRETORIO);
        String nome = UUID.randomUUID() + "." + extensao;
        Files.write(DIRETORIO.resolve(nome), conteudo);
        return ImagensDoForum.PREFIXO + nome;
    }
}
