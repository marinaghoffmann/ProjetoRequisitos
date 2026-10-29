package com.g4.game_tracker_backend.infraestrutura.persistencia.catalogo;

import java.util.UUID;

import com.g4.game_tracker_backend.dominio.catalogo.Genero;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "generos", uniqueConstraints = @UniqueConstraint(name = "uk_generos_nome", columnNames = "nome"))
public class GeneroJpa {

    @Id
    private UUID id;

    @Column(nullable = false, length = 80)
    private String nome;

    protected GeneroJpa() {
    }

    private GeneroJpa(Genero genero) {
        this.id = genero.id();
        this.nome = genero.nome();
    }

    static GeneroJpa deDominio(Genero genero) {
        return new GeneroJpa(genero);
    }

    Genero paraDominio() {
        return Genero.reconstituir(id, nome);
    }

    UUID id() {
        return id;
    }
}
