package com.g4.game_tracker_backend.apresentacao.wishlist;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.g4.game_tracker_backend.aplicacao.wishlist.AdicionarNaWishlist;
import com.g4.game_tracker_backend.aplicacao.wishlist.AlterarPrioridadeNaWishlist;
import com.g4.game_tracker_backend.aplicacao.wishlist.ConsultarWishlist;
import com.g4.game_tracker_backend.aplicacao.wishlist.RemoverDaWishlist;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/perfis")
public class WishlistController {

    private final ConsultarWishlist consultarWishlist;
    private final AdicionarNaWishlist adicionarNaWishlist;
    private final AlterarPrioridadeNaWishlist alterarPrioridade;
    private final RemoverDaWishlist removerDaWishlist;

    public WishlistController(ConsultarWishlist consultarWishlist, AdicionarNaWishlist adicionarNaWishlist,
            AlterarPrioridadeNaWishlist alterarPrioridade, RemoverDaWishlist removerDaWishlist) {
        this.consultarWishlist = consultarWishlist;
        this.adicionarNaWishlist = adicionarNaWishlist;
        this.alterarPrioridade = alterarPrioridade;
        this.removerDaWishlist = removerDaWishlist;
    }

    @GetMapping("/me/wishlist")
    public List<ItemWishlistResponse> listarMinha() {
        return consultarWishlist.doUsuarioAtual().stream().map(ItemWishlistResponse::de).toList();
    }

    @GetMapping("/{donoId}/wishlist")
    public List<ItemWishlistResponse> listarDe(@PathVariable UUID donoId) {
        return consultarWishlist.executar(donoId).stream().map(ItemWishlistResponse::de).toList();
    }

    @PostMapping("/me/wishlist")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void adicionar(@Valid @RequestBody AdicionarWishlistRequest request) {
        adicionarNaWishlist.executar(request.jogoId(), request.prioridade());
    }

    @PatchMapping("/me/wishlist/{jogoId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void alterarPrioridade(@PathVariable UUID jogoId,
            @Valid @RequestBody AlterarPrioridadeWishlistRequest request) {
        alterarPrioridade.executar(jogoId, request.prioridade());
    }

    @DeleteMapping("/me/wishlist/{jogoId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void remover(@PathVariable UUID jogoId) {
        removerDaWishlist.executar(jogoId);
    }
}