package com.g4.game_tracker_backend.infraestrutura.persistencia.catalogo;

import java.util.UUID;

import com.g4.game_tracker_backend.dominio.catalogo.Desenvolvedora;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "desenvolvedoras", uniqueConstraints = @UniqueConstraint(name = "uk_desenvolvedoras_nome",
        columnNames = "nome"))
public class DesenvolvedoraJpa {

    @Id
    private UUID id;

    @Column(nullable = false, length = 160)
    private String nome;

    @Column(length = 2000)
    private String biografia;

    @Column(length = 500)
    private String site;

    @Column(name = "redes_sociais", length = 1000)
    private String redesSociais;

    @Column(length = 120)
    private String pais;

    @Column(nullable = false)
    private boolean ativa;

    protected DesenvolvedoraJpa() {
    }

    private DesenvolvedoraJpa(Desenvolvedora desenvolvedora) {
        this.id = desenvolvedora.id();
        this.nome = desenvolvedora.nome();
        this.biografia = desenvolvedora.biografia();
        this.site = desenvolvedora.site();
        this.redesSociais = desenvolvedora.redesSociais();
        this.pais = desenvolvedora.pais();
        this.ativa = desenvolvedora.ativa();
    }

    static DesenvolvedoraJpa deDominio(Desenvolvedora desenvolvedora) {
        return new DesenvolvedoraJpa(desenvolvedora);
    }

    Desenvolvedora paraDominio() {
        return Desenvolvedora.reconstituir(id, nome, biografia, site, redesSociais, pais, ativa);
    }

    UUID getId() {
        return id;
    }
}
