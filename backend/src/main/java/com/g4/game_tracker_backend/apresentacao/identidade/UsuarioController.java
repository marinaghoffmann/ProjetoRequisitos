package com.g4.game_tracker_backend.apresentacao.identidade;

import java.io.IOException;
import java.net.URI;
import java.util.Map;
import java.util.UUID;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.g4.game_tracker_backend.aplicacao.identidade.AutenticarUsuario;
import com.g4.game_tracker_backend.aplicacao.identidade.AlterarPapelUsuario;
import com.g4.game_tracker_backend.aplicacao.identidade.AtualizarImagemPerfil;
import com.g4.game_tracker_backend.aplicacao.identidade.EditarPerfil;
import com.g4.game_tracker_backend.aplicacao.identidade.VincularDesenvolvedora;
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
    private final AlterarPapelUsuario alterarPapelUsuario;
    private final VincularDesenvolvedora vincularDesenvolvedora;
    private final EditarPerfil editarPerfil;
    private final AtualizarImagemPerfil atualizarImagemPerfil;

    public UsuarioController(RegistrarUsuario registrarUsuario, AutenticarUsuario autenticarUsuario,
            ConsultarUsuarioAtual consultarUsuarioAtual, AlterarPapelUsuario alterarPapelUsuario,
            VincularDesenvolvedora vincularDesenvolvedora, EditarPerfil editarPerfil,
            AtualizarImagemPerfil atualizarImagemPerfil) {
        this.registrarUsuario = registrarUsuario;
        this.autenticarUsuario = autenticarUsuario;
        this.consultarUsuarioAtual = consultarUsuarioAtual;
        this.alterarPapelUsuario = alterarPapelUsuario;
        this.vincularDesenvolvedora = vincularDesenvolvedora;
        this.editarPerfil = editarPerfil;
        this.atualizarImagemPerfil = atualizarImagemPerfil;
    }

    @PatchMapping("/usuarios/{id}/desenvolvedora")
    public UsuarioResponse vincularDesenvolvedora(@PathVariable UUID id,
            @Valid @RequestBody VincularDesenvolvedoraRequest requisicao) {
        return UsuarioResponse.de(vincularDesenvolvedora.executar(id, requisicao.desenvolvedoraId()));
    }

    @PostMapping("/usuarios")
    public ResponseEntity<UsuarioResponse> cadastrar(@Valid @RequestBody CadastroUsuarioRequest requisicao) {
        Usuario usuario = registrarUsuario.executar(requisicao.nome(), requisicao.email(), requisicao.senha());
        return ResponseEntity.created(URI.create("/api/usuarios/" + usuario.id()))
                .body(UsuarioResponse.de(usuario));
    }

    /** O usuário autenticado edita o próprio perfil (nome, bio e localização). */
    @PutMapping("/usuarios/me")
    public UsuarioResponse editarPerfil(@Valid @RequestBody EditarPerfilRequest requisicao) {
        return UsuarioResponse.de(editarPerfil.executar(requisicao.nome(), requisicao.bio(), requisicao.localizacao()));
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

    @PatchMapping("/usuarios/{id}/papel")
    public UsuarioResponse alterarPapel(@PathVariable UUID id,
            @Valid @RequestBody AlterarPapelUsuarioRequest requisicao) {
        return UsuarioResponse.de(alterarPapelUsuario.executar(id, requisicao.papel()));
    }

    private static final Map<String, String> TIPOS = Map.of(
            "image/jpeg", "jpg", "image/png", "png", "image/webp", "webp");

    @PostMapping(value = "/usuarios/me/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public UsuarioResponse avatar(@RequestParam("arquivo") MultipartFile arquivo) throws IOException {
        return atualizar(AtualizarImagemPerfil.Tipo.AVATAR, arquivo);
    }

    @PostMapping(value = "/usuarios/me/banner", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public UsuarioResponse banner(@RequestParam("arquivo") MultipartFile arquivo) throws IOException {
        return atualizar(AtualizarImagemPerfil.Tipo.BANNER, arquivo);
    }

    private UsuarioResponse atualizar(AtualizarImagemPerfil.Tipo tipo, MultipartFile arquivo) throws IOException {
        String extensao = TIPOS.get(arquivo.getContentType());
        if (arquivo.isEmpty() || extensao == null) {
            throw new IllegalArgumentException("Envie uma imagem JPG, PNG ou WebP.");
        }
        return UsuarioResponse.de(atualizarImagemPerfil.executar(tipo, arquivo.getBytes(), extensao));
    }
}