package com.g4.game_tracker_backend.infraestrutura.persistencia.identidade;

import java.util.UUID;

import com.g4.game_tracker_backend.dominio.identidade.Papel;
import com.g4.game_tracker_backend.dominio.identidade.Usuario;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "usuarios", uniqueConstraints = @UniqueConstraint(name = "uk_usuarios_email", columnNames = "email"))
public class UsuarioJpa {

    @Id
    private UUID id;

    @Column(nullable = false, length = 120)
    private String nome;

    @Column(nullable = false, length = 254)
    private String email;

    @Column(name = "senha_hash", nullable = false, length = 100)
    private String senhaHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 24)
    private Papel papel;

    @Column(nullable = false)
    private boolean ativo;

    protected UsuarioJpa() {
    }

    private UsuarioJpa(Usuario usuario) {
        this.id = usuario.id();
        this.nome = usuario.nome();
        this.email = usuario.email();
        this.senhaHash = usuario.senhaHash();
        this.papel = usuario.papel();
        this.ativo = usuario.ativo();
    }

    static UsuarioJpa deDominio(Usuario usuario) {
        return new UsuarioJpa(usuario);
    }

    Usuario paraDominio() {
        return Usuario.reconstituir(id, nome, email, senhaHash, papel, ativo);
    }
}
