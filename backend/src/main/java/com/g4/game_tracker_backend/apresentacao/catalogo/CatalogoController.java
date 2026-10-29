package com.g4.game_tracker_backend.apresentacao.catalogo;

import java.net.URI;
import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.g4.game_tracker_backend.aplicacao.catalogo.CadastrarDesenvolvedora;
import com.g4.game_tracker_backend.aplicacao.catalogo.CadastrarGenero;
import com.g4.game_tracker_backend.aplicacao.catalogo.CadastrarJogoManual;
import com.g4.game_tracker_backend.aplicacao.catalogo.ConsultarCatalogo;
import com.g4.game_tracker_backend.aplicacao.catalogo.AtualizarJogoManual;
import com.g4.game_tracker_backend.aplicacao.catalogo.DesativarJogo;
import com.g4.game_tracker_backend.aplicacao.catalogo.AtualizarDesenvolvedora;
import com.g4.game_tracker_backend.aplicacao.catalogo.DesativarDesenvolvedora;
import com.g4.game_tracker_backend.aplicacao.catalogo.ImportarJogoSteam;
import com.g4.game_tracker_backend.aplicacao.catalogo.ImportarJogo;
import com.g4.game_tracker_backend.dominio.catalogo.FiltroCatalogo;
import com.g4.game_tracker_backend.dominio.catalogo.OrigemJogo;
import com.g4.game_tracker_backend.dominio.catalogo.StatusCatalogo;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/catalogo")
public class CatalogoController {

    private final ConsultarCatalogo consultarCatalogo;
    private final CadastrarJogoManual cadastrarJogoManual;
    private final ImportarJogo importarJogo;
    private final CadastrarDesenvolvedora cadastrarDesenvolvedora;
    private final CadastrarGenero cadastrarGenero;
    private final ObjectMapper objectMapper;
    private final AtualizarJogoManual atualizarJogoManual;
    private final DesativarJogo desativarJogo;
    private final AtualizarDesenvolvedora atualizarDesenvolvedora;
    private final DesativarDesenvolvedora desativarDesenvolvedora;
    private final ImportarJogoSteam importarJogoSteam;

    public CatalogoController(ConsultarCatalogo consultarCatalogo, CadastrarJogoManual cadastrarJogoManual,
            ImportarJogo importarJogo, CadastrarDesenvolvedora cadastrarDesenvolvedora,
            CadastrarGenero cadastrarGenero, ObjectMapper objectMapper, AtualizarJogoManual atualizarJogoManual,
            DesativarJogo desativarJogo, AtualizarDesenvolvedora atualizarDesenvolvedora,
            DesativarDesenvolvedora desativarDesenvolvedora, ImportarJogoSteam importarJogoSteam) {
        this.consultarCatalogo = consultarCatalogo;
        this.cadastrarJogoManual = cadastrarJogoManual;
        this.importarJogo = importarJogo;
        this.cadastrarDesenvolvedora = cadastrarDesenvolvedora;
        this.cadastrarGenero = cadastrarGenero;
        this.objectMapper = objectMapper;
        this.atualizarJogoManual = atualizarJogoManual;
        this.desativarJogo = desativarJogo;
        this.atualizarDesenvolvedora = atualizarDesenvolvedora;
        this.desativarDesenvolvedora = desativarDesenvolvedora;
        this.importarJogoSteam = importarJogoSteam;
    }

    @GetMapping("/jogos/{id}")
    public JogoResponse consultarJogo(@PathVariable UUID id) {
        return JogoResponse.de(consultarCatalogo.buscarPorId(id));
    }

    @GetMapping("/jogos")
    public List<JogoResponse> consultarJogos(@RequestParam(required = false) String titulo,
            @RequestParam(required = false) UUID generoId,
            @RequestParam(required = false) UUID desenvolvedoraId,
            @RequestParam(required = false) OrigemJogo origem,
            @RequestParam(required = false) StatusCatalogo status) {
        FiltroCatalogo filtro = new FiltroCatalogo(titulo, generoId, desenvolvedoraId, origem,
                status == null ? StatusCatalogo.ATIVO : status);
        return consultarCatalogo.executar(filtro).stream().map(JogoResponse::de).toList();
    }

    @GetMapping("/desenvolvedoras")
    public List<DesenvolvedoraResponse> consultarDesenvolvedoras() {
        return consultarCatalogoDesenvolvedoras();
    }

    @GetMapping("/generos")
    public List<GeneroResponse> consultarGeneros() {
        return cadastrarGenero.listar().stream().map(GeneroResponse::de).toList();
    }

    @PostMapping("/jogos")
    public ResponseEntity<JogoResponse> cadastrar(@Valid @RequestBody JogoRequest request) {
        var jogo = cadastrarJogoManual.executarParaUsuario(request.titulo(), request.tituloOriginal(),
                request.resumo(), request.generosIds(), request.dataLancamento(), request.capaUrl(),
                request.trailerUrl(), serializarMetadados(request.metadados()));
        return ResponseEntity.created(URI.create("/api/catalogo/jogos/" + jogo.id()))
                .body(JogoResponse.de(jogo));
    }

    @PutMapping("/jogos/{id}")
    public JogoResponse atualizar(@PathVariable UUID id, @Valid @RequestBody JogoRequest request) {
        return JogoResponse.de(atualizarJogoManual.executar(id, request.titulo(), request.tituloOriginal(),
                request.resumo(), request.desenvolvedoraId(), request.generosIds(), request.dataLancamento(),
                request.capaUrl(), request.trailerUrl(), serializarMetadados(request.metadados())));
    }

    @DeleteMapping("/jogos/{id}")
    public JogoResponse excluir(@PathVariable UUID id) {
        return JogoResponse.de(desativarJogo.executar(id));
    }

    private String serializarMetadados(java.util.Map<String, Object> metadados) {
        if (metadados == null || metadados.isEmpty()) return "";
        try {
            return objectMapper.writeValueAsString(metadados);
        } catch (JacksonException e) {
            throw new IllegalArgumentException("Os metadados do jogo são inválidos", e);
        }
    }

    @PostMapping("/jogos/importacao")
    public JogoResponse importar(@Valid @RequestBody ImportarJogoRequest request) {
        return JogoResponse.de(importarJogo.executar(request.titulo(), request.tituloOriginal(), request.resumo(),
                request.identificadorExterno(), request.desenvolvedoraId(), request.generosIds(),
                request.dataLancamento(), request.capaUrl(), request.trailerUrl()));
    }

    @PostMapping("/jogos/importacao/steam")
    public JogoResponse importarSteam(@Valid @RequestBody SteamImportacaoRequest request) {
        return JogoResponse.de(importarJogoSteam.executar(request.termo()));
    }

    @PostMapping("/desenvolvedoras")
    public DesenvolvedoraResponse cadastrarDesenvolvedora(@Valid @RequestBody DesenvolvedoraRequest request) {
        return DesenvolvedoraResponse.de(cadastrarDesenvolvedora.executar(request.nome(), request.biografia(),
                request.site(), request.redesSociais(), request.pais()));
    }

    @PutMapping("/desenvolvedoras/{id}")
    public DesenvolvedoraResponse atualizarDesenvolvedora(@PathVariable UUID id,
            @Valid @RequestBody DesenvolvedoraRequest request) {
        return DesenvolvedoraResponse.de(atualizarDesenvolvedora.executar(id, request.biografia(),
                request.site(), request.redesSociais(), request.pais()));
    }

    @DeleteMapping("/desenvolvedoras/{id}")
    public DesenvolvedoraResponse excluirDesenvolvedora(@PathVariable UUID id) {
        return DesenvolvedoraResponse.de(desativarDesenvolvedora.executar(id));
    }

    @PostMapping("/generos")
    public GeneroResponse cadastrarGenero(@Valid @RequestBody GeneroRequest request) {
        return GeneroResponse.de(cadastrarGenero.executar(request.nome()));
    }

    private List<DesenvolvedoraResponse> consultarCatalogoDesenvolvedoras() {
        return cadastrarDesenvolvedora.listar().stream().map(DesenvolvedoraResponse::de).toList();
    }
}
