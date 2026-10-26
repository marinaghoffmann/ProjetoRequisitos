package com.g4.game_tracker_backend.aplicacao.identidade;

import java.nio.charset.StandardCharsets;
import java.util.Locale;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.g4.game_tracker_backend.dominio.compartilhado.RegraDeNegocioException;
import com.g4.game_tracker_backend.dominio.identidade.Usuario;
import com.g4.game_tracker_backend.dominio.identidade.UsuarioRepositorio;

@Service
public class RegistrarUsuario {

    private static final int SENHA_MINIMA_BYTES = 8;
    private static final int SENHA_MAXIMA_BYTES_BCRYPT = 72;

    private final UsuarioRepositorio usuarios;
    private final CodificadorDeSenha codificadorDeSenha;

    public RegistrarUsuario(UsuarioRepositorio usuarios, CodificadorDeSenha codificadorDeSenha) {
        this.usuarios = usuarios;
        this.codificadorDeSenha = codificadorDeSenha;
    }

    @Transactional
    public Usuario executar(String nome, String email, String senha) {
        String emailNormalizado = email.trim().toLowerCase(Locale.ROOT);
        validarSenha(senha);
        if (usuarios.existePorEmail(emailNormalizado)) {
            throw new RegraDeNegocioException("Já existe uma conta com este email");
        }
        String senhaHash = codificadorDeSenha.codificar(senha);
        return usuarios.salvar(Usuario.registrar(nome, emailNormalizado, senhaHash));
    }

    private void validarSenha(String senha) {
        int tamanhoEmBytes = senha.getBytes(StandardCharsets.UTF_8).length;
        if (tamanhoEmBytes < SENHA_MINIMA_BYTES || tamanhoEmBytes > SENHA_MAXIMA_BYTES_BCRYPT) {
            throw new RegraDeNegocioException("A senha deve ter entre 8 e 72 bytes em UTF-8");
        }
    }
}
