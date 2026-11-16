package com.g4.game_tracker_backend.dominio.identidade;

import java.util.Locale;
import java.util.Objects;
import java.util.UUID;

import com.g4.game_tracker_backend.dominio.compartilhado.RegraDeNegocioException;

/** Usuário da plataforma; a senha armazenada aqui deve ser sempre um hash. */
public final class Usuario {

    public static final int NOME_MAXIMO = 80;
    public static final int BIO_MAXIMA = 160;
    public static final int LOCALIZACAO_MAXIMA = 80;
    public static final int URL_IMAGEM_MAXIMA = 255;

    private final UUID id;
    private final String nome;
    private final String email;
    private final String senhaHash;
    private final Papel papel;
    private final boolean ativo;
    private final UUID desenvolvedoraId;
    private final String bio;
    private final String localizacao;
    private final String avatarUrl;
    private final String bannerUrl;

    private Usuario(UUID id, String nome, String email, String senhaHash, Papel papel, boolean ativo,
            UUID desenvolvedoraId, String bio, String localizacao, String avatarUrl, String bannerUrl) {
        this.id = Objects.requireNonNull(id, "O identificador do usuário é obrigatório");
        this.nome = exigirTexto(nome, "O nome é obrigatório");
        this.email = exigirTexto(normalizarEmail(email), "O email é obrigatório");
        this.senhaHash = exigirTexto(senhaHash, "A senha deve ser armazenada como hash");
        this.papel = Objects.requireNonNull(papel, "O papel do usuário é obrigatório");
        this.ativo = ativo;
        this.desenvolvedoraId = desenvolvedoraId;
        this.bio = opcional(bio);
        this.localizacao = opcional(localizacao);
        this.avatarUrl = opcional(avatarUrl);
        this.bannerUrl = opcional(bannerUrl);
    }

    private static String exigirTexto(String valor, String mensagem) {
        if (valor == null || valor.isBlank()) {
            throw new RegraDeNegocioException(mensagem);
        }
        return valor.trim();
    }

    /** Texto opcional: vazio ou só espaços vira nulo. */
    private static String opcional(String valor) {
        return valor == null || valor.isBlank() ? null : valor.trim();
    }

    private static void limitar(String valor, int maximo, String mensagem) {
        if (valor != null && valor.length() > maximo) {
            throw new RegraDeNegocioException(mensagem);
        }
    }

    /** Cria uma conta pública sempre com o papel básico, sem permitir autoelevação. */
    public static Usuario registrar(String nome, String email, String senhaHash) {
        return new Usuario(UUID.randomUUID(), nome, email, senhaHash, Papel.COMUM, true, null, null, null, null,
                null);
    }

    /** Reconstrói um usuário persistido sem alterar seu identificador ou papel. */
    public static Usuario reconstituir(UUID id, String nome, String email, String senhaHash,
            Papel papel, boolean ativo) {
        return new Usuario(id, nome, email, senhaHash, papel, ativo, null, null, null, null, null);
    }

    public static Usuario reconstituir(UUID id, String nome, String email, String senhaHash,
            Papel papel, boolean ativo, UUID desenvolvedoraId) {
        return new Usuario(id, nome, email, senhaHash, papel, ativo, desenvolvedoraId, null, null, null, null);
    }

    public static Usuario reconstituir(UUID id, String nome, String email, String senhaHash,
            Papel papel, boolean ativo, UUID desenvolvedoraId, String bio, String localizacao) {
        return new Usuario(id, nome, email, senhaHash, papel, ativo, desenvolvedoraId, bio, localizacao, null,
                null);
    }

    public static Usuario reconstituir(UUID id, String nome, String email, String senhaHash,
            Papel papel, boolean ativo, UUID desenvolvedoraId, String bio, String localizacao,
            String avatarUrl, String bannerUrl) {
        return new Usuario(id, nome, email, senhaHash, papel, ativo, desenvolvedoraId, bio, localizacao,
                avatarUrl, bannerUrl);
    }

    public Usuario comPapel(Papel novoPapel) {
        return new Usuario(id, nome, email, senhaHash, novoPapel, ativo, desenvolvedoraId, bio, localizacao,
                avatarUrl, bannerUrl);
    }

    public Usuario vincularDesenvolvedora(UUID novaDesenvolvedoraId) {
        return new Usuario(id, nome, email, senhaHash, papel, ativo, novaDesenvolvedoraId, bio, localizacao,
                avatarUrl, bannerUrl);
    }

    /** O próprio usuário altera nome, bio e localização; bio e localização vazias viram nulas. */
    public Usuario editarPerfil(String novoNome, String novaBio, String novaLocalizacao) {
        String nomeValido = exigirTexto(novoNome, "O nome é obrigatório");
        limitar(nomeValido, NOME_MAXIMO, "O nome deve ter no máximo " + NOME_MAXIMO + " caracteres");
        String bioValida = opcional(novaBio);
        limitar(bioValida, BIO_MAXIMA, "A bio deve ter no máximo " + BIO_MAXIMA + " caracteres");
        String localizacaoValida = opcional(novaLocalizacao);
        limitar(localizacaoValida, LOCALIZACAO_MAXIMA,
                "A localização deve ter no máximo " + LOCALIZACAO_MAXIMA + " caracteres");
        return new Usuario(id, nomeValido, email, senhaHash, papel, ativo, desenvolvedoraId, bioValida,
                localizacaoValida, avatarUrl, bannerUrl);
    }

    /** Define a foto de perfil; o valor nulo remove a foto. */
    public Usuario comAvatar(String novaUrl) {
        String url = opcional(novaUrl);
        limitar(url, URL_IMAGEM_MAXIMA, "A URL da foto é longa demais");
        return new Usuario(id, nome, email, senhaHash, papel, ativo, desenvolvedoraId, bio, localizacao, url,
                bannerUrl);
    }

    /** Define o banner do perfil; o valor nulo remove o banner. */
    public Usuario comBanner(String novaUrl) {
        String url = opcional(novaUrl);
        limitar(url, URL_IMAGEM_MAXIMA, "A URL do banner é longa demais");
        return new Usuario(id, nome, email, senhaHash, papel, ativo, desenvolvedoraId, bio, localizacao,
                avatarUrl, url);
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

    public String bio() {
        return bio;
    }

    public String localizacao() {
        return localizacao;
    }

    public String avatarUrl() {
        return avatarUrl;
    }

    public String bannerUrl() {
        return bannerUrl;
    }
}