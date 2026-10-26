package com.g4.game_tracker_backend.aplicacao.identidade;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.g4.game_tracker_backend.dominio.identidade.Papel;
import com.g4.game_tracker_backend.dominio.identidade.Usuario;
import com.g4.game_tracker_backend.dominio.identidade.UsuarioRepositorio;

@Service
public class ProvisionarAdministradorInicial {

    private final UsuarioRepositorio usuarios;
    private final CodificadorDeSenha codificadorDeSenha;

    public ProvisionarAdministradorInicial(UsuarioRepositorio usuarios, CodificadorDeSenha codificadorDeSenha) {
        this.usuarios = usuarios;
        this.codificadorDeSenha = codificadorDeSenha;
    }

    @Transactional
    public void executar(String nome, String email, String senha) {
        boolean emailConfigurado = email != null && !email.isBlank();
        boolean senhaConfigurada = senha != null && !senha.isBlank();
        if (!emailConfigurado && !senhaConfigurada) {
            return;
        }
        if (!emailConfigurado || !senhaConfigurada) {
            throw new IllegalStateException(
                    "Configure email e senha juntos para provisionar o administrador inicial");
        }
        if (senha.getBytes(StandardCharsets.UTF_8).length < 8
                || senha.getBytes(StandardCharsets.UTF_8).length > 72) {
            throw new IllegalStateException("A senha do administrador inicial deve ter entre 8 e 72 bytes em UTF-8");
        }
        usuarios.buscarPorEmail(email)
                .ifPresentOrElse(usuario -> {
                    if (usuario.papel() != Papel.ADMINISTRADOR) {
                        throw new IllegalStateException(
                                "O email configurado para o administrador inicial já pertence a uma conta sem papel Admin");
                    }
                }, () -> usuarios.salvar(Usuario.reconstituir(UUID.randomUUID(), nome, email,
                        codificadorDeSenha.codificar(senha), Papel.ADMINISTRADOR, true)));
    }
}
