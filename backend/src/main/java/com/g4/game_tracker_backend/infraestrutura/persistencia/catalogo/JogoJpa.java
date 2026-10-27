package com.g4.game_tracker_backend.infraestrutura.persistencia.catalogo;

import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import com.g4.game_tracker_backend.dominio.catalogo.Jogo;
import com.g4.game_tracker_backend.dominio.catalogo.OrigemJogo;
import com.g4.game_tracker_backend.dominio.catalogo.StatusCatalogo;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "jogos", uniqueConstraints = {
        @UniqueConstraint(name = "uk_jogos_identificador_externo", columnNames = "identificador_externo"),
        @UniqueConstraint(name = "uk_jogos_titulo_desenvolvedora",
                columnNames = { "titulo", "desenvolvedora_id" })
})
public class JogoJpa {

    @Id
    private UUID id;

    @Column(nullable = false, length = 180)
    private String titulo;

    @Column(name = "titulo_original", length = 180)
    private String tituloOriginal;

    @Column(nullable = false, length = 220)
    private String slug;

    @Column(length = 600)
    private String resumo;

    @Column(name = "identificador_externo", length = 160)
    private String identificadorExterno;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 24)
    private OrigemJogo origem;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 24)
    private StatusCatalogo status;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "desenvolvedora_id", nullable = false)
    private DesenvolvedoraJpa desenvolvedora;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "jogos_generos",
            joinColumns = @JoinColumn(name = "jogo_id"),
            inverseJoinColumns = @JoinColumn(name = "genero_id"))
    private Set<GeneroJpa> generos = new LinkedHashSet<>();

    @Column(name = "data_lancamento")
    private LocalDate dataLancamento;

    @Column(name = "capa_url", length = 1000)
    private String capaUrl;

    @Column(name = "trailer_url", length = 1000)
    private String trailerUrl;

    @Column(name = "metadados_json", columnDefinition = "TEXT")
    private String metadadosJson;

    protected JogoJpa() {
    }

    private JogoJpa(Jogo jogo, DesenvolvedoraJpa desenvolvedora, Set<GeneroJpa> generos) {
        this.id = jogo.id();
        this.titulo = jogo.titulo();
        this.tituloOriginal = jogo.tituloOriginal();
        this.slug = jogo.slug();
        this.resumo = jogo.resumo();
        this.identificadorExterno = vazioParaNulo(jogo.identificadorExterno());
        this.origem = jogo.origem();
        this.status = jogo.status();
        this.desenvolvedora = desenvolvedora;
        this.generos = new LinkedHashSet<>(generos);
        this.dataLancamento = jogo.dataLancamento();
        this.capaUrl = vazioParaNulo(jogo.capaUrl());
        this.trailerUrl = vazioParaNulo(jogo.trailerUrl());
        this.metadadosJson = vazioParaNulo(jogo.metadadosJson());
    }

    static JogoJpa deDominio(Jogo jogo, DesenvolvedoraJpa desenvolvedora, Set<GeneroJpa> generos) {
        return new JogoJpa(jogo, desenvolvedora, generos);
    }

    Jogo paraDominio() {
        return Jogo.reconstituir(id, titulo, tituloOriginal, slug, resumo, identificadorExterno,
                origem, status, desenvolvedora.getId(), generos.stream().map(GeneroJpa::id)
                        .collect(Collectors.toCollection(LinkedHashSet::new)),
                dataLancamento, capaUrl, trailerUrl).comMetadados(metadadosJson);
    }

    private static String vazioParaNulo(String valor) {
        return valor == null || valor.isBlank() ? null : valor;
    }
}
