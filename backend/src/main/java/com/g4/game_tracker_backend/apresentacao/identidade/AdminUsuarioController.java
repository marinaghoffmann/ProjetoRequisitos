package com.g4.game_tracker_backend.apresentacao.identidade;

import java.net.URI;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.g4.game_tracker_backend.aplicacao.identidade.CadastrarUsuarioPrivilegiado;
import com.g4.game_tracker_backend.dominio.identidade.Usuario;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/admin/usuarios")
public class AdminUsuarioController {

    private final CadastrarUsuarioPrivilegiado cadastrar;

    public AdminUsuarioController(CadastrarUsuarioPrivilegiado cadastrar) {
        this.cadastrar = cadastrar;
    }

    @PostMapping
    public ResponseEntity<UsuarioResponse> cadastrar(@Valid @RequestBody CadastroPrivilegiadoRequest r) {
        Usuario u = cadastrar.executar(r.nome(), r.email(), r.senha(), r.papel());
        return ResponseEntity.created(URI.create("/api/usuarios/" + u.id())).body(UsuarioResponse.de(u));
    }
}