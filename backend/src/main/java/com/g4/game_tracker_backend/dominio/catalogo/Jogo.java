package com.g4.game_tracker_backend.dominio.catalogo;

import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

import com.g4.game_tracker_backend.dominio.compartilhado.RegraDeNegocioException;

public final class Jogo {

    private final UUID id;
    private final String titulo;
    private final String tituloOriginal;
    private final String slug;
    private final String resumo;
    private final String identificadorExterno;
    private final OrigemJogo origem;
    private final StatusCatalogo status;
    private final UUID desenvolvedoraId;
    private final Set<UUID> generosIds;
    private final LocalDate dataLancamento;
    private final String capaUrl;
    private final String trailerUrl;
    private final String metadadosJson;

    private Jogo(UUID id, String titulo, String tituloOriginal, String slug, String resumo,
            String identificadorExterno, OrigemJogo origem, StatusCatalogo status, UUID desenvolvedoraId,
            Set<UUID> generosIds, LocalDate dataLancamento, String capaUrl, String trailerUrl) {
        this(id, titulo, tituloOriginal, slug, resumo, identificadorExterno, origem, status, desenvolvedoraId,
                generosIds, dataLancamento, capaUrl, trailerUrl, "");
    }

    private Jogo(UUID id, String titulo, String tituloOriginal, String slug, String resumo,
            String identificadorExterno, OrigemJogo origem, StatusCatalogo status, UUID desenvolvedoraId,
            Set<UUID> generosIds, LocalDate dataLancamento, String capaUrl, String trailerUrl, String metadadosJson) {
        this.id = Objects.requireNonNull(id, "O identificador do jogo é obrigatório");
        this.titulo = texto(titulo, "O título do jogo é obrigatório");
        this.tituloOriginal = opcional(tituloOriginal);
        this.slug = texto(slug, "O slug do jogo é obrigatório");
        this.resumo = limitar(resumo, 600, "O resumo deve ter no máximo 600 caracteres");
        this.identificadorExterno = opcional(identificadorExterno);
        this.origem = Objects.requireNonNull(origem, "A origem do jogo é obrigatória");
        this.status = Objects.requireNonNull(status, "O status do jogo é obrigatório");
        this.desenvolvedoraId = Objects.requireNonNull(desenvolvedoraId,
                "O jogo deve estar vinculado a uma desenvolvedora");
        this.generosIds = Set.copyOf(generosIds == null ? Set.of() : new LinkedHashSet<>(generosIds));
        this.dataLancamento = dataLancamento;
        this.capaUrl = opcional(capaUrl);
        this.trailerUrl = opcional(trailerUrl);
        this.metadadosJson = opcional(metadadosJson);
    }

    public static Jogo criarManual(String titulo, String tituloOriginal, String resumo, UUID desenvolvedoraId,
            Set<UUID> generosIds, LocalDate dataLancamento, String capaUrl, String trailerUrl) {
        return novo(titulo, tituloOriginal, resumo, "", OrigemJogo.MANUAL_CRIADOR, desenvolvedoraId,
                generosIds, dataLancamento, capaUrl, trailerUrl);
    }

    public static Jogo importar(String titulo, String tituloOriginal, String resumo, String identificadorExterno,
            UUID desenvolvedoraId, Set<UUID> generosIds, LocalDate dataLancamento, String capaUrl,
            String trailerUrl) {
        if (identificadorExterno == null || identificadorExterno.isBlank()) {
            throw new RegraDeNegocioException("O identificador externo é obrigatório para importação");
        }
        return novo(titulo, tituloOriginal, resumo, identificadorExterno, OrigemJogo.API, desenvolvedoraId,
                generosIds, dataLancamento, capaUrl, trailerUrl);
    }

    private static Jogo novo(String titulo, String tituloOriginal, String resumo, String identificadorExterno,
            OrigemJogo origem, UUID desenvolvedoraId, Set<UUID> generosIds, LocalDate dataLancamento,
            String capaUrl, String trailerUrl) {
        return new Jogo(UUID.randomUUID(), titulo, tituloOriginal, slugificar(titulo), resumo,
                identificadorExterno, origem, StatusCatalogo.ATIVO, desenvolvedoraId, generosIds, dataLancamento,
                capaUrl, trailerUrl);
    }

    public static Jogo reconstituir(UUID id, String titulo, String tituloOriginal, String slug, String resumo,
            String identificadorExterno, OrigemJogo origem, StatusCatalogo status, UUID desenvolvedoraId,
            Set<UUID> generosIds, LocalDate dataLancamento, String capaUrl, String trailerUrl) {
        return new Jogo(id, titulo, tituloOriginal, slug, resumo, identificadorExterno, origem, status,
                desenvolvedoraId, generosIds, dataLancamento, capaUrl, trailerUrl);
    }

    public Jogo atualizarDados(String titulo, String tituloOriginal, String resumo, UUID desenvolvedoraId,
            Set<UUID> generosIds, LocalDate dataLancamento, String capaUrl, String trailerUrl) {
        return new Jogo(id, titulo, tituloOriginal, slugificar(titulo), resumo, identificadorExterno, origem,
                status, desenvolvedoraId, generosIds, dataLancamento, capaUrl, trailerUrl)
                .comMetadados(metadadosJson);
    }

    public Jogo comMetadados(String metadadosJson) {
        return new Jogo(id, titulo, tituloOriginal, slug, resumo, identificadorExterno, origem, status,
                desenvolvedoraId, generosIds, dataLancamento, capaUrl, trailerUrl, metadadosJson);
    }

    public Jogo desativar() {
        return new Jogo(id, titulo, tituloOriginal, slug, resumo, identificadorExterno, origem,
                StatusCatalogo.INATIVO, desenvolvedoraId, generosIds, dataLancamento, capaUrl, trailerUrl)
                .comMetadados(metadadosJson);
    }

    private static String slugificar(String valor) {
        return texto(valor, "O título do jogo é obrigatório").toLowerCase()
                .replaceAll("[^a-z0-9]+", "-").replaceAll("(^-|-$)", "");
    }

    private static String texto(String valor, String mensagem) {
        if (valor == null || valor.isBlank()) {
            throw new RegraDeNegocioException(mensagem);
        }
        return valor.trim();
    }

    private static String opcional(String valor) {
        return valor == null ? "" : valor.trim();
    }

    private static String limitar(String valor, int limite, String mensagem) {
        String normalizado = opcional(valor);
        if (normalizado.length() > limite) {
            throw new RegraDeNegocioException(mensagem);
        }
        return normalizado;
    }

    public UUID id() { return id; }
    public String titulo() { return titulo; }
    public String tituloOriginal() { return tituloOriginal; }
    public String slug() { return slug; }
    public String resumo() { return resumo; }
    public String identificadorExterno() { return identificadorExterno; }
    public OrigemJogo origem() { return origem; }
    public StatusCatalogo status() { return status; }
    public UUID desenvolvedoraId() { return desenvolvedoraId; }
    public Set<UUID> generosIds() { return generosIds; }
    public LocalDate dataLancamento() { return dataLancamento; }
    public String capaUrl() { return capaUrl; }
    public String trailerUrl() { return trailerUrl; }
    public String metadadosJson() { return metadadosJson; }
}
