package com.g4.game_tracker_backend.apresentacao.forum;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
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
import com.g4.game_tracker_backend.aplicacao.forum.CurtidasForum;
import com.g4.game_tracker_backend.aplicacao.forum.CriarTopico;
import com.g4.game_tracker_backend.aplicacao.forum.EditarTopico;
import com.g4.game_tracker_backend.aplicacao.forum.GerenciarRespostas;
import com.g4.game_tracker_backend.apresentacao.compartilhado.PaginaResponse;
import com.g4.game_tracker_backend.dominio.compartilhado.Pagina;
import com.g4.game_tracker_backend.dominio.forum.FiltroTopicos;
import com.g4.game_tracker_backend.dominio.forum.ResumoTopico;
import com.g4.game_tracker_backend.dominio.forum.TopicoDeForum;
import com.g4.game_tracker_backend.dominio.forum.StatusTopico;
import com.g4.game_tracker_backend.dominio.identidade.Usuario;
import com.g4.game_tracker_backend.dominio.compartilhado.RegraDeNegocioException;

import jakarta.validation.Valid;
import com.g4.game_tracker_backend.aplicacao.moderacao.EstadoPublico;

@RestController
@RequestMapping("/api/forum")
public class ForumController {

    private final CriarTopico criarTopico;
    private final EditarTopico editarTopico;
    private final GerenciarRespostas gerenciarRespostas;
    private final ConsultarForum consultarForum;
    private final CurtidasForum curtidas;
    private final EstadoPublico moderacao;

    public ForumController(CriarTopico criarTopico, EditarTopico editarTopico,
            GerenciarRespostas gerenciarRespostas, ConsultarForum consultarForum, CurtidasForum curtidas,
            EstadoPublico moderacao) {
        this.criarTopico = criarTopico;
        this.editarTopico = editarTopico;
        this.gerenciarRespostas = gerenciarRespostas;
        this.consultarForum = consultarForum;
        this.curtidas = curtidas;
        this.moderacao = moderacao;
    }

    @GetMapping("/topicos")
    public PaginaResponse<TopicoResumoResponse> listar(@RequestParam(required = false) UUID jogoId,
            @RequestParam(required = false) String categoria,
            @RequestParam(defaultValue = "false") boolean apenasGerais,
            @RequestParam(required = false) StatusTopico status,
            @RequestParam(defaultValue = "recentes") String ordem,
            @RequestParam(defaultValue = "1") int pagina,
            @RequestParam(defaultValue = "10") int tamanho) {
        if (!ordem.equalsIgnoreCase("recentes") && !ordem.equalsIgnoreCase("emAlta")) {
            throw new RegraDeNegocioException("Ordem inválida");
        }
        Pagina<ResumoTopico> resultado = consultarForum.listar(
                new FiltroTopicos(jogoId, categoria, apenasGerais, status, ordem.equalsIgnoreCase("emAlta")),
                pagina, tamanho);
        Map<UUID, Usuario> autores = consultarForum.autores(
                resultado.itens().stream().map(ResumoTopico::autorId).toList());
        return PaginaResponse.de(resultado.map(resumo -> TopicoResumoResponse.de(resumo, autores, moderacao)));
    }

    @GetMapping("/topicos/{id}")
    public TopicoResponse consultar(@PathVariable UUID id) {
        return detalhar(consultarForum.buscarPorId(id));
    }

    @PostMapping("/topicos")
    public ResponseEntity<TopicoResponse> criar(@Valid @RequestBody TopicoRequest request) {
        TopicoDeForum topico = criarTopico.executar(request.jogoId(), request.categoria(), request.titulo(),
                request.conteudo(), request.imagens());
        return ResponseEntity.created(URI.create("/api/forum/topicos/" + topico.id())).body(detalhar(topico));
    }

    /** Na edição, jogoId é ignorado: o tópico continua no mesmo jogo. imagens nulo mantém as atuais. */
    @PutMapping("/topicos/{id}")
    public TopicoResponse editar(@PathVariable UUID id, @Valid @RequestBody TopicoRequest request) {
        return detalhar(editarTopico.executar(id, request.titulo(), request.conteudo(), request.categoria(),
                request.imagens()));
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
        gerenciarRespostas.responder(id, request.respostaPaiId(), request.conteudo(), request.imagens());
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

    @PutMapping("/topicos/{id}/curtida")
    public ResponseEntity<Void> curtirTopico(@PathVariable UUID id) {
        curtidas.definir(id, null, true);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/topicos/{id}/curtida")
    public ResponseEntity<Void> descurtirTopico(@PathVariable UUID id) {
        curtidas.definir(id, null, false);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/topicos/{id}/respostas/{respostaId}/curtida")
    public ResponseEntity<Void> curtirResposta(@PathVariable UUID id, @PathVariable UUID respostaId) {
        curtidas.definir(id, respostaId, true);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/topicos/{id}/respostas/{respostaId}/curtida")
    public ResponseEntity<Void> descurtirResposta(@PathVariable UUID id, @PathVariable UUID respostaId) {
        curtidas.definir(id, respostaId, false);
        return ResponseEntity.noContent().build();
    }

    private static UUID usuarioAtualIdOuNull() {
        var autenticacao = SecurityContextHolder.getContext().getAuthentication();
        return autenticacao instanceof JwtAuthenticationToken token && token.isAuthenticated()
                ? UUID.fromString(token.getToken().getSubject()) : null;
    }

    private TopicoResponse detalhar(TopicoDeForum topico) {
        List<UUID> autores = new ArrayList<>();
        autores.add(topico.autorId());
        topico.respostas().forEach(resposta -> autores.add(resposta.autorId()));
        return TopicoResponse.de(topico, consultarForum.autores(autores),
                curtidas.resumo(topico.id(), usuarioAtualIdOuNull()), moderacao);
    }
}