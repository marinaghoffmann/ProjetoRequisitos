package com.g4.game_tracker_backend.dominio.identidade;

import java.util.Locale;
import java.util.Objects;
import java.util.UUID;

import com.g4.game_tracker_backend.dominio.compartilhado.RegraDeNegocioException;

/** Usuário da plataforma; a senha armazenada aqui deve ser sempre um hash. */
public final class Usuario {

    private final UUID id;
    private final String nome;
    private final String email;
    private final String senhaHash;
    private final Papel papel;
    private final boolean ativo;
    private final UUID desenvolvedoraId;

    private Usuario(UUID id, String nome, String email, String senhaHash, Papel papel, boolean ativo,
            UUID desenvolvedoraId) {
        this.id = Objects.requireNonNull(id, "O identificador do usuário é obrigatório");
        this.nome = exigirTexto(nome, "O nome é obrigatório");
        this.email = exigirTexto(normalizarEmail(email), "O email é obrigatório");
        this.senhaHash = exigirTexto(senhaHash, "A senha deve ser armazenada como hash");
        this.papel = Objects.requireNonNull(papel, "O papel do usuário é obrigatório");
        this.ativo = ativo;
        this.desenvolvedoraId = desenvolvedoraId;
    }

    private static String exigirTexto(String valor, String mensagem) {
        if (valor == null || valor.isBlank()) {
            throw new RegraDeNegocioException(mensagem);
        }
        return valor.trim();
    }

    /** Cria uma conta pública sempre com o papel básico, sem permitir autoelevação. */
    public static Usuario registrar(String nome, String email, String senhaHash) {
        return new Usuario(UUID.randomUUID(), nome, email, senhaHash, Papel.COMUM, true, null);
    }

    /** Reconstrói um usuário persistido sem alterar seu identificador ou papel. */
    public static Usuario reconstituir(UUID id, String nome, String email, String senhaHash,
            Papel papel, boolean ativo) {
        return new Usuario(id, nome, email, senhaHash, papel, ativo, null);
    }

    public static Usuario reconstituir(UUID id, String nome, String email, String senhaHash,
            Papel papel, boolean ativo, UUID desenvolvedoraId) {
        return new Usuario(id, nome, email, senhaHash, papel, ativo, desenvolvedoraId);
    }

    public Usuario comPapel(Papel novoPapel) {
        return new Usuario(id, nome, email, senhaHash, novoPapel, ativo, desenvolvedoraId);
    }

    public Usuario vincularDesenvolvedora(UUID novaDesenvolvedoraId) {
        return new Usuario(id, nome, email, senhaHash, papel, ativo, novaDesenvolvedoraId);
    }

    private static String normalizarEmail(String email) {
        return email == null ? null : email.trim().toLowerCase(Locale.ROOT);
    }

    public UUID id() {
        return id;
    }

    public String nome() {
        return nome;
    }

    public String email() {
        return email;
    }

    public String senhaHash() {
        return senhaHash;
    }

    public Papel papel() {
        return papel;
    }

    public boolean ativo() {
        return ativo;
    }

    public UUID desenvolvedoraId() {
        return desenvolvedoraId;
    }
}
