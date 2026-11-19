package com.g4.game_tracker_backend.infraestrutura.persistencia.wishlist;

import java.util.Optional;
import java.util.UUID;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.g4.game_tracker_backend.dominio.compartilhado.RegraDeNegocioException;
import com.g4.game_tracker_backend.dominio.wishlist.Wishlist;
import com.g4.game_tracker_backend.dominio.wishlist.WishlistRepositorio;

@Repository
@Transactional(readOnly = true)
public class WishlistRepositorioPostgres implements WishlistRepositorio {

    private final WishlistSpringDataRepository springData;

    public WishlistRepositorioPostgres(WishlistSpringDataRepository springData) {
        this.springData = springData;
    }

    @Override
    public Optional<Wishlist> buscarPorDono(UUID donoId) {
        return springData.findById(donoId).map(WishlistJpa::paraDominio);
    }

    @Override
    @Transactional
    public Wishlist salvar(Wishlist wishlist) {
        WishlistJpa jpa = springData.findById(wishlist.donoId())
                .orElseGet(() -> new WishlistJpa(wishlist.donoId(), wishlist.criadaEm()));
        jpa.sincronizar(wishlist.itens());
        try {
            // saveAndFlush força o INSERT aqui dentro, para a violação de unicidade cair neste catch
            return springData.saveAndFlush(jpa).paraDominio();
        } catch (DataIntegrityViolationException e) {
            // dois cliques rápidos: o domínio não viu o duplicado, mas o banco barrou (RN 2)
            throw new RegraDeNegocioException("Este jogo já está na sua wishlist");
        }
    }

    @Override
    public int contarWishlistsComJogo(UUID jogoId) {
        return Math.toIntExact(springData.contarComOJogo(jogoId));
    }
}
