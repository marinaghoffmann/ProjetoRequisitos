package com.g4.game_tracker_backend.apresentacao.wishlist;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.g4.game_tracker_backend.aplicacao.wishlist.AdicionarNaWishlist;
import com.g4.game_tracker_backend.aplicacao.wishlist.ConsultarWishlist;
import com.g4.game_tracker_backend.aplicacao.wishlist.RemoverDaWishlist;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/perfis/me/wishlist")
public class WishlistController {

    private final ConsultarWishlist consultarWishlist;
    private final AdicionarNaWishlist adicionarNaWishlist;
    private final RemoverDaWishlist removerDaWishlist;

    public WishlistController(ConsultarWishlist consultarWishlist, AdicionarNaWishlist adicionarNaWishlist,
            RemoverDaWishlist removerDaWishlist) {
        this.consultarWishlist = consultarWishlist;
        this.adicionarNaWishlist = adicionarNaWishlist;
        this.removerDaWishlist = removerDaWishlist;
    }

    @GetMapping
    public List<ItemWishlistResponse> listar() {
        return consultarWishlist.executar().stream().map(ItemWishlistResponse::de).toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void adicionar(@Valid @RequestBody AdicionarWishlistRequest request) {
        adicionarNaWishlist.executar(request.jogoId());
    }

    @DeleteMapping("/{jogoId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void remover(@PathVariable UUID jogoId) {
        removerDaWishlist.executar(jogoId);
    }
}