package com.g4.game_tracker_backend.infraestrutura.persistencia.catalogo;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.g4.game_tracker_backend.dominio.catalogo.FiltroCatalogo;
import com.g4.game_tracker_backend.dominio.catalogo.Jogo;
import com.g4.game_tracker_backend.dominio.catalogo.JogoRepositorio;

@Repository
@Transactional(readOnly = true)
public class JogoRepositorioPostgres implements JogoRepositorio {

    private final JogoSpringDataRepository springData;

    public JogoRepositorioPostgres(JogoSpringDataRepository springData) {
        this.springData = springData;
    }

    @Override
    @Transactional
    public Jogo salvar(Jogo jogo) {
        return springData.save(JogoJpa.deDominio(jogo)).paraDominio();
    }

    @Override
    public Optional<Jogo> buscarPorId(UUID id) {
        return springData.findById(id).map(JogoJpa::paraDominio);
    }

    @Override
    public Optional<Jogo> buscarPorIdentificadorExterno(String identificadorExterno) {
        return springData.findByIdentificadorExterno(identificadorExterno).map(JogoJpa::paraDominio);
    }

    @Override
    public boolean existePorTituloEDesenvolvedora(String titulo, UUID desenvolvedoraId) {
        return springData.existsByTituloIgnoreCaseAndDesenvolvedoraId(titulo, desenvolvedoraId);
    }

    @Override
    public boolean existePorTituloEDesenvolvedoraEIdDiferente(String titulo, UUID desenvolvedoraId, UUID jogoId) {
        return springData.existsByTituloIgnoreCaseAndDesenvolvedoraIdAndIdNot(titulo, desenvolvedoraId, jogoId);
    }

    @Override
    public List<Jogo> consultar(FiltroCatalogo filtro) {
        String titulo = filtro.titulo() == null ? "" : filtro.titulo();
        return springData.consultar(titulo, filtro.generoId(), filtro.desenvolvedoraId(),
                filtro.origem(), filtro.status()).stream().map(JogoJpa::paraDominio).toList();
    }
}