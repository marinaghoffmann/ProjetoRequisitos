package com.g4.game_tracker_backend.apresentacao.identidade;

import java.net.URI;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.g4.game_tracker_backend.aplicacao.identidade.AutenticarUsuario;
import com.g4.game_tracker_backend.aplicacao.identidade.ConsultarUsuarioAtual;
import com.g4.game_tracker_backend.aplicacao.identidade.RegistrarUsuario;
import com.g4.game_tracker_backend.aplicacao.identidade.SessaoAutenticada;
import com.g4.game_tracker_backend.dominio.identidade.Usuario;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api")
public class UsuarioController {

    private final RegistrarUsuario registrarUsuario;
    private final AutenticarUsuario autenticarUsuario;
    private final ConsultarUsuarioAtual consultarUsuarioAtual;

    public UsuarioController(RegistrarUsuario registrarUsuario, AutenticarUsuario autenticarUsuario,
            ConsultarUsuarioAtual consultarUsuarioAtual) {
        this.registrarUsuario = registrarUsuario;
        this.autenticarUsuario = autenticarUsuario;
        this.consultarUsuarioAtual = consultarUsuarioAtual;
    }

    @PostMapping("/usuarios")
    public ResponseEntity<UsuarioResponse> cadastrar(@Valid @RequestBody CadastroUsuarioRequest requisicao) {
        Usuario usuario = registrarUsuario.executar(requisicao.nome(), requisicao.email(), requisicao.senha());
        return ResponseEntity.created(URI.create("/api/usuarios/" + usuario.id()))
                .body(UsuarioResponse.de(usuario));
    }

    @PostMapping("/auth/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest requisicao) {
        SessaoAutenticada sessao = autenticarUsuario.executar(requisicao.email(), requisicao.senha());
        return new LoginResponse(sessao.token().valor(), "Bearer", sessao.token().expiraEm(),
                UsuarioResponse.de(sessao.usuario()));
    }

    @GetMapping("/auth/me")
    public UsuarioResponse usuarioAtual() {
        return UsuarioResponse.de(consultarUsuarioAtual.executar());
    }
}
