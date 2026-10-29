package com.g4.game_tracker_backend.infraestrutura.persistencia.catalogo;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Repository;

import com.g4.game_tracker_backend.dominio.catalogo.FiltroCatalogo;
import com.g4.game_tracker_backend.dominio.catalogo.Jogo;
import com.g4.game_tracker_backend.dominio.catalogo.JogoRepositorio;

@Repository
public class JogoRepositorioPostgres implements JogoRepositorio {

    private final JogoSpringDataRepository springData;
    private final DesenvolvedoraSpringDataRepository desenvolvedoras;
    private final GeneroSpringDataRepository generos;

    public JogoRepositorioPostgres(JogoSpringDataRepository springData,
            DesenvolvedoraSpringDataRepository desenvolvedoras, GeneroSpringDataRepository generos) {
        this.springData = springData;
        this.desenvolvedoras = desenvolvedoras;
        this.generos = generos;
    }

    @Override
    public Jogo salvar(Jogo jogo) {
        DesenvolvedoraJpa desenvolvedora = desenvolvedoras.findById(jogo.desenvolvedoraId())
                .orElseThrow(() -> new IllegalStateException("Desenvolvedora não encontrada: "
                        + jogo.desenvolvedoraId()));
        var generosJpa = jogo.generosIds().stream()
                .map(id -> generos.findById(id)
                        .orElseThrow(() -> new IllegalStateException("Gênero não encontrado: " + id)))
                .collect(Collectors.toCollection(LinkedHashSet::new));
        return springData.save(JogoJpa.deDominio(jogo, desenvolvedora, generosJpa)).paraDominio();
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
        return springData.existsByTituloIgnoreCaseAndDesenvolvedora_Id(titulo, desenvolvedoraId);
    }

    @Override
    public boolean existePorTituloEDesenvolvedoraEIdDiferente(String titulo, UUID desenvolvedoraId, UUID jogoId) {
        return springData.existsByTituloIgnoreCaseAndDesenvolvedora_IdAndIdNot(titulo, desenvolvedoraId, jogoId);
    }

    @Override
    public List<Jogo> consultar(FiltroCatalogo filtro) {
        String titulo = filtro.titulo() == null ? "" : filtro.titulo();
        return springData.consultar(titulo, filtro.generoId(), filtro.desenvolvedoraId(),
                filtro.origem(), filtro.status()).stream().map(JogoJpa::paraDominio).toList();
    }
}
