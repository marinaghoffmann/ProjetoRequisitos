package com.g4.game_tracker_backend.apresentacao.biblioteca.listas;

import java.net.URI;
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
import org.springframework.web.bind.annotation.RestController;

import com.g4.game_tracker_backend.aplicacao.biblioteca.listas.ConsultarListas;
import com.g4.game_tracker_backend.aplicacao.biblioteca.listas.ConsultarListas.JogoDaLista;
import com.g4.game_tracker_backend.aplicacao.biblioteca.listas.GerenciarItensDaLista;
import com.g4.game_tracker_backend.aplicacao.biblioteca.listas.GerenciarListas;
import com.g4.game_tracker_backend.dominio.biblioteca.listas.ListaPersonalizada;
import com.g4.game_tracker_backend.aplicacao.moderacao.EstadoPublico;
import com.g4.game_tracker_backend.dominio.moderacao.TipoConteudo;


/** F-04: listas personalizadas e os seus itens. */
@RestController
@RequestMapping("/api/biblioteca")
public class ListaController {

    /**
     * As regras (título obrigatório e os limites de 80 e 500 caracteres) ficam no domínio, para a mensagem
     * chegar ao usuário. O dono nunca vem daqui, e sim do usuário autenticado.
     */
    public record ListaRequest(String titulo, String descricao) {
    }

    public record ItemRequest(UUID jogoId, String notaExplicativa) {
    }

    public record NotaRequest(String notaExplicativa) {
    }

    private final GerenciarListas gerenciarListas;
    private final GerenciarItensDaLista gerenciarItens;
    private final ConsultarListas consultarListas;
    private final EstadoPublico moderacao;

    public ListaController(GerenciarListas gerenciarListas, GerenciarItensDaLista gerenciarItens,
            ConsultarListas consultarListas, EstadoPublico moderacao) {
        this.gerenciarListas = gerenciarListas;
        this.gerenciarItens = gerenciarItens;
        this.consultarListas = consultarListas;
        this.moderacao = moderacao;
    }

    @GetMapping("/listas")
    public List<ListaResponse> minhas() {
        return detalhar(consultarListas.minhas());
    }

    @GetMapping("/usuarios/{usuarioId}/listas")
    public List<ListaResponse> doUsuario(@PathVariable UUID usuarioId) {
        return detalhar(consultarListas.doUsuario(usuarioId).stream().filter(l -> moderacao.visivel(TipoConteudo.LISTA,l.id())).toList());
    }

    @GetMapping("/listas/{id}")
    public ListaResponse consultar(@PathVariable UUID id) {
        moderacao.exigirVisivel(TipoConteudo.LISTA,id);
        return detalhar(consultarListas.buscar(id));
    }

    @PostMapping("/listas")
    public ResponseEntity<ListaResponse> criar(@RequestBody ListaRequest request) {
        ListaPersonalizada lista = gerenciarListas.criar(request.titulo(), request.descricao());
        return ResponseEntity.created(URI.create("/api/biblioteca/listas/" + lista.id())).body(detalhar(lista));
    }

    @PutMapping("/listas/{id}")
    public ListaResponse editar(@PathVariable UUID id, @RequestBody ListaRequest request) {
        return detalhar(gerenciarListas.editar(id, request.titulo(), request.descricao()));
    }

    /** Exclusão física: a lista e os seus itens deixam de existir. */
    @DeleteMapping("/listas/{id}")
    public ResponseEntity<Void> excluir(@PathVariable UUID id) {
        gerenciarListas.excluir(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/listas/{id}/itens")
    public ResponseEntity<ListaResponse> adicionar(@PathVariable UUID id, @RequestBody ItemRequest request) {
        return ResponseEntity.status(201)
                .body(detalhar(gerenciarItens.adicionar(id, request.jogoId(), request.notaExplicativa())));
    }

    @PatchMapping("/listas/{id}/itens/{jogoId}")
    public ListaResponse editarNota(@PathVariable UUID id, @PathVariable UUID jogoId,
            @RequestBody NotaRequest request) {
        return detalhar(gerenciarItens.editarNota(id, jogoId, request.notaExplicativa()));
    }

    @DeleteMapping("/listas/{id}/itens/{jogoId}")
    public ListaResponse remover(@PathVariable UUID id, @PathVariable UUID jogoId) {
        return detalhar(gerenciarItens.remover(id, jogoId));
    }

    private ListaResponse detalhar(ListaPersonalizada lista) {
        return detalhar(List.of(lista)).getFirst();
    }

    private List<ListaResponse> detalhar(List<ListaPersonalizada> listas) {
        Map<UUID, String> nomes = consultarListas.nomesDosDonos(listas.stream().map(ListaPersonalizada::donoId)
                .toList());
        Map<UUID, JogoDaLista> jogos = consultarListas.jogosDosItens(listas);
        return listas.stream().map(lista -> ListaResponse.de(lista, nomes, jogos)).toList();
    }
}
