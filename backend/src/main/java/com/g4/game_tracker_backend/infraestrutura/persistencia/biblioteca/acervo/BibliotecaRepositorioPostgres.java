package com.g4.game_tracker_backend.infraestrutura.persistencia.biblioteca.acervo;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.g4.game_tracker_backend.dominio.biblioteca.acervo.BibliotecaRepositorio;
import com.g4.game_tracker_backend.dominio.biblioteca.acervo.ItemBiblioteca;

interface ItemBibliotecaSpringDataRepository extends JpaRepository<ItemBibliotecaJpa, UUID> {

    boolean existsByUsuarioIdAndJogoId(UUID usuarioId, UUID jogoId);

    List<ItemBibliotecaJpa> findByUsuarioIdOrderByAdicionadoEmDesc(UUID usuarioId);

    void deleteByUsuarioIdAndJogoId(UUID usuarioId, UUID jogoId);
}

@Repository
public class BibliotecaRepositorioPostgres implements BibliotecaRepositorio {

    private final ItemBibliotecaSpringDataRepository springData;

    public BibliotecaRepositorioPostgres(ItemBibliotecaSpringDataRepository springData) {
        this.springData = springData;
    }

    @Override
    public ItemBiblioteca salvar(ItemBiblioteca item) {
        return springData.save(ItemBibliotecaJpa.deDominio(item)).paraDominio();
    }

    @Override
    public boolean existe(UUID usuarioId, UUID jogoId) {
        return springData.existsByUsuarioIdAndJogoId(usuarioId, jogoId);
    }

    @Override
    @Transactional
    public void remover(UUID usuarioId, UUID jogoId) {
        springData.deleteByUsuarioIdAndJogoId(usuarioId, jogoId);
    }

    @Override
    public List<ItemBiblioteca> listarPorUsuario(UUID usuarioId) {
        return springData.findByUsuarioIdOrderByAdicionadoEmDesc(usuarioId).stream()
                .map(ItemBibliotecaJpa::paraDominio).toList();
    }
}