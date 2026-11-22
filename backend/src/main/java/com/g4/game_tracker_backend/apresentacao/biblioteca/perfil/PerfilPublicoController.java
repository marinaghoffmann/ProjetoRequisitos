package com.g4.game_tracker_backend.apresentacao.biblioteca.perfil;

import java.util.List;
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

import com.g4.game_tracker_backend.aplicacao.biblioteca.perfil.ConsultarPerfilPublico;
import com.g4.game_tracker_backend.aplicacao.biblioteca.perfil.GerenciarFavoritos;
import com.g4.game_tracker_backend.dominio.biblioteca.conclusoes.ResumoJogoZerado;
import com.g4.game_tracker_backend.dominio.compartilhado.RegraDeNegocioException;

@RestController
@RequestMapping("/api/perfis")
public class PerfilPublicoController {
    public record FavoritoRequest(UUID jogoId, Integer slot) {
    }
    public record PosicaoRequest(Integer slot) {
    }

    private final ConsultarPerfilPublico consultar;
    private final GerenciarFavoritos gerenciar;

    public PerfilPublicoController(ConsultarPerfilPublico consultar, GerenciarFavoritos gerenciar) {
        this.consultar = consultar;
        this.gerenciar = gerenciar;
    }

    @GetMapping("/{id}")
    public ConsultarPerfilPublico.PerfilPublico perfil(@PathVariable UUID id) {
        return consultar.consultar(id);
    }

    @GetMapping("/{id}/biblioteca")
    public List<ConsultarPerfilPublico.JogoPublico> biblioteca(@PathVariable UUID id) {
        return consultar.biblioteca(id);
    }

    @GetMapping("/{id}/zerados")
    public List<ResumoJogoZerado> zerados(@PathVariable UUID id) {
        return consultar.zerados(id);
    }

    @PostMapping("/me/favoritos")
    public ResponseEntity<Void> adicionar(@RequestBody FavoritoRequest request) {
        gerenciar.adicionar(request.jogoId(), request.slot());
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/me/favoritos/{jogoId}/posicao")
    public ResponseEntity<Void> mover(@PathVariable UUID jogoId, @RequestBody PosicaoRequest request) {
        if (request.slot() == null) throw new RegraDeNegocioException("O slot é obrigatório");
        gerenciar.mover(jogoId, request.slot());
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/me/favoritos/slots/{slot}")
    public ResponseEntity<Void> substituir(@PathVariable int slot, @RequestBody FavoritoRequest request) {
        gerenciar.substituir(slot, request.jogoId());
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/me/favoritos/{jogoId}")
    public ResponseEntity<Void> remover(@PathVariable UUID jogoId) {
        gerenciar.remover(jogoId);
        return ResponseEntity.noContent().build();
    }
}
