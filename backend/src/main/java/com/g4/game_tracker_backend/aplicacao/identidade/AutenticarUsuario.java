package com.g4.game_tracker_backend.aplicacao.identidade;

import java.util.Locale;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.g4.game_tracker_backend.dominio.compartilhado.CredenciaisInvalidasException;
import com.g4.game_tracker_backend.dominio.identidade.Usuario;
import com.g4.game_tracker_backend.dominio.identidade.UsuarioRepositorio;

@Service
public class AutenticarUsuario {

    private final UsuarioRepositorio usuarios;
    private final AutenticadorDeCredenciais autenticador;
    private final EmissorDeToken emissorDeToken;

    public AutenticarUsuario(UsuarioRepositorio usuarios, AutenticadorDeCredenciais autenticador,
            EmissorDeToken emissorDeToken) {
        this.usuarios = usuarios;
        this.autenticador = autenticador;
        this.emissorDeToken = emissorDeToken;
    }

    @Transactional(readOnly = true)
    public SessaoAutenticada executar(String email, String senha) {
        String emailNormalizado = email.trim().toLowerCase(Locale.ROOT);
        autenticador.autenticar(emailNormalizado, senha);
        Usuario usuario = usuarios.buscarPorEmail(emailNormalizado)
                .filter(Usuario::ativo)
                .orElseThrow(CredenciaisInvalidasException::new);
        return new SessaoAutenticada(usuario, emissorDeToken.emitir(usuario));
    }
}
