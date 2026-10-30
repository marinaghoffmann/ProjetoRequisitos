package com.g4.game_tracker_backend.apresentacao.forum;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.g4.game_tracker_backend.aplicacao.forum.ConsultarForum;
import com.g4.game_tracker_backend.aplicacao.forum.CriarTopico;
import com.g4.game_tracker_backend.aplicacao.forum.EditarTopico;
import com.g4.game_tracker_backend.aplicacao.forum.GerenciarRespostas;
import com.g4.game_tracker_backend.dominio.compartilhado.Pagina;
import com.g4.game_tracker_backend.dominio.forum.FiltroTopicos;
import com.g4.game_tracker_backend.dominio.forum.ResumoTopico;
import com.g4.game_tracker_backend.dominio.forum.TopicoDeForum;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/forum")
public class ForumController {

    private final CriarTopico criarTopico;
    private final EditarTopico editarTopico;
    private final GerenciarRespostas gerenciarRespostas;
    private final ConsultarForum consultarForum;

    public ForumController(CriarTopico criarTopico, EditarTopico editarTopico,
            GerenciarRespostas gerenciarRespostas, ConsultarForum consultarForum) {
        this.criarTopico = criarTopico;
        this.editarTopico = editarTopico;
        this.gerenciarRespostas = gerenciarRespostas;
        this.consultarForum = consultarForum;
    }

    @GetMapping("/topicos")
    public PaginaResponse<TopicoResumoResponse> listar(@RequestParam(required = false) UUID jogoId,
            @RequestParam(required = false) String categoria,
            @RequestParam(defaultValue = "1") int pagina,
            @RequestParam(defaultValue = "10") int tamanho) {
        Pagina<ResumoTopico> resultado = consultarForum.listar(new FiltroTopicos(jogoId, categoria), pagina, tamanho);
        Map<UUID, String> nomes = consultarForum.nomesDosAutores(
                resultado.itens().stream().map(ResumoTopico::autorId).toList());
        return PaginaResponse.de(resultado.map(resumo -> TopicoResumoResponse.de(resumo, nomes)));
    }

    @GetMapping("/topicos/{id}")
    public TopicoResponse consultar(@PathVariable UUID id) {
        return detalhar(consultarForum.buscarPorId(id));
    }

    @PostMapping("/topicos")
    public ResponseEntity<TopicoResponse> criar(@Valid @RequestBody TopicoRequest request) {
        TopicoDeForum topico = criarTopico.executar(request.jogoId(), request.categoria(), request.titulo(),
                request.conteudo());
        return ResponseEntity.created(URI.create("/api/forum/topicos/" + topico.id())).body(detalhar(topico));
    }

    /** Na edição, jogoId é ignorado: o tópico continua no mesmo jogo. */
    @PutMapping("/topicos/{id}")
    public TopicoResponse editar(@PathVariable UUID id, @Valid @RequestBody TopicoRequest request) {
        return detalhar(editarTopico.executar(id, request.titulo(), request.conteudo(), request.categoria()));
    }

    @PatchMapping("/topicos/{id}/status")
    public TopicoResponse alterarStatus(@PathVariable UUID id,
            @Valid @RequestBody AlterarStatusTopicoRequest request) {
        return detalhar(editarTopico.alterarStatus(id, request.status()));
    }

    /** Exclusão lógica: o tópico some das consultas. */
    @DeleteMapping("/topicos/{id}")
    public ResponseEntity<Void> excluir(@PathVariable UUID id) {
        editarTopico.excluir(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/topicos/{id}/respostas")
    public ResponseEntity<TopicoResponse> responder(@PathVariable UUID id,
            @Valid @RequestBody RespostaRequest request) {
        gerenciarRespostas.responder(id, request.respostaPaiId(), request.conteudo());
        return ResponseEntity.status(201).body(detalhar(consultarForum.buscarPorId(id)));
    }

    @PutMapping("/topicos/{id}/respostas/{respostaId}")
    public TopicoResponse editarResposta(@PathVariable UUID id, @PathVariable UUID respostaId,
            @Valid @RequestBody RespostaRequest request) {
        gerenciarRespostas.editar(id, respostaId, request.conteudo());
        return detalhar(consultarForum.buscarPorId(id));
    }

    @DeleteMapping("/topicos/{id}/respostas/{respostaId}")
    public TopicoResponse excluirResposta(@PathVariable UUID id, @PathVariable UUID respostaId) {
        gerenciarRespostas.excluir(id, respostaId);
        return detalhar(consultarForum.buscarPorId(id));
    }

    private TopicoResponse detalhar(TopicoDeForum topico) {
        List<UUID> autores = new ArrayList<>();
        autores.add(topico.autorId());
        topico.respostas().forEach(resposta -> autores.add(resposta.autorId()));
        return TopicoResponse.de(topico, consultarForum.nomesDosAutores(autores));
    }
}
