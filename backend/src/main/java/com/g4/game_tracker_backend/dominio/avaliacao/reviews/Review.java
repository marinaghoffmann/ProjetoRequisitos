package com.g4.game_tracker_backend.dominio.avaliacao.reviews;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import com.g4.game_tracker_backend.dominio.compartilhado.AcessoNegadoException;
import com.g4.game_tracker_backend.dominio.compartilhado.RegraDeNegocioException;

/**
 * Agregado da review de um jogo (F-02), com a nota geral, as notas por categoria e o texto. É imutável:
 * cada alteração devolve uma nova instância.
 *
 * RN 1: uma review por usuário e jogo. A unicidade é checada no caso de uso (que precisa consultar o
 * repositório) e garantida no banco pela restrição única (autor, jogo).
 * RN 2: só o autor edita ou exclui a review ("Apenas o autor pode alterar a review").
 * RN 3: as notas por categoria pertencem à review e são removidas junto com ela.
 *
 * Toda nota fica entre 1 e 5 e cada categoria aparece no máximo uma vez. As notas por categoria são
 * opcionais: o jogador pode publicar só a nota geral.
 */
public final class Review {

    public static final int LIMITE_DO_TEXTO = 5000;

    static final String APENAS_O_AUTOR = "Apenas o autor pode alterar a review";

    private final UUID id;
    private final UUID autorId;
    private final UUID jogoId;
    private final NotaAvaliacao notaGeral;
    private final String texto;
    private final List<NotaPorCategoria> notasPorCategoria;
    private final Instant criadaEm;
    private final Instant atualizadaEm;

    private Review(UUID id, UUID autorId, UUID jogoId, NotaAvaliacao notaGeral, String texto,
            List<NotaPorCategoria> notasPorCategoria, Instant criadaEm, Instant atualizadaEm) {
        this.id = Objects.requireNonNull(id, "O identificador da review é obrigatório");
        this.autorId = Objects.requireNonNull(autorId, "O autor da review é obrigatório");
        this.jogoId = Objects.requireNonNull(jogoId, "O jogo da review é obrigatório");
        this.notaGeral = Objects.requireNonNull(notaGeral, "A nota geral é obrigatória");
        this.texto = texto(texto);
        this.notasPorCategoria = ordenadas(notasPorCategoria);
        this.criadaEm = Objects.requireNonNull(criadaEm, "A data de criação é obrigatória");
        this.atualizadaEm = Objects.requireNonNull(atualizadaEm, "A data de atualização é obrigatória");
    }

    public static Review publicar(UUID autorId, UUID jogoId, Integer notaGeral, List<NotaInformada> notas,
            String texto, Instant agora) {
        return new Review(UUID.randomUUID(), autorId, jogoId, NotaAvaliacao.de(notaGeral), texto,
                montarNotas(notas, Map.of()), agora, agora);
    }

    public static Review reconstituir(UUID id, UUID autorId, UUID jogoId, int notaGeral, String texto,
            List<NotaPorCategoria> notasPorCategoria, Instant criadaEm, Instant atualizadaEm) {
        return new Review(id, autorId, jogoId, new NotaAvaliacao(notaGeral), texto, notasPorCategoria, criadaEm,
                atualizadaEm);
    }

    /**
     * RN 2: só o autor altera as notas e o texto. As notas de categorias que continuam na review mantêm o
     * identificador; as que saem da lista deixam de existir.
     */
    public Review editar(UUID solicitanteId, Integer novaNotaGeral, List<NotaInformada> novasNotas, String novoTexto,
            Instant agora) {
        exigirAutor(solicitanteId);
        Map<CategoriaReview, UUID> idsAtuais = new EnumMap<>(CategoriaReview.class);
        notasPorCategoria.forEach(nota -> idsAtuais.put(nota.categoria(), nota.id()));
        return new Review(id, autorId, jogoId, NotaAvaliacao.de(novaNotaGeral), novoTexto,
                montarNotas(novasNotas, idsAtuais), criadaEm, agora);
    }

    /** RN 2: a exclusão também é exclusiva do autor. */
    public void exigirAutor(UUID solicitanteId) {
        if (!autorId.equals(solicitanteId)) {
            throw new AcessoNegadoException(APENAS_O_AUTOR);
        }
    }

    public boolean pertenceA(UUID usuarioId) {
        return autorId.equals(usuarioId);
    }

    public Optional<NotaAvaliacao> notaDe(CategoriaReview categoria) {
        return notasPorCategoria.stream()
                .filter(nota -> nota.categoria() == categoria)
                .map(NotaPorCategoria::nota)
                .findFirst();
    }

    private static List<NotaPorCategoria> montarNotas(List<NotaInformada> notas, Map<CategoriaReview, UUID> ids) {
        if (notas == null) {
            return List.of();
        }
        Map<CategoriaReview, NotaPorCategoria> porCategoria = new EnumMap<>(CategoriaReview.class);
        for (NotaInformada informada : notas) {
            if (informada == null || informada.categoria() == null) {
                throw new RegraDeNegocioException("A categoria da nota é obrigatória");
            }
            NotaAvaliacao nota = NotaAvaliacao.de(informada.nota());
            UUID notaId = ids.getOrDefault(informada.categoria(), UUID.randomUUID());
            if (porCategoria.put(informada.categoria(), new NotaPorCategoria(notaId, informada.categoria(), nota))
                    != null) {
                throw new RegraDeNegocioException("Cada categoria só pode receber uma nota");
            }
        }
        return new ArrayList<>(porCategoria.values());
    }

    private static List<NotaPorCategoria> ordenadas(List<NotaPorCategoria> notas) {
        List<NotaPorCategoria> copia = new ArrayList<>(notas == null ? List.of() : notas);
        copia.sort(Comparator.comparing(NotaPorCategoria::categoria));
        for (int i = 1; i < copia.size(); i++) {
            if (copia.get(i).categoria() == copia.get(i - 1).categoria()) {
                throw new RegraDeNegocioException("Cada categoria só pode receber uma nota");
            }
        }
        return List.copyOf(copia);
    }

    private static String texto(String valor) {
        String normalizado = valor == null ? "" : valor.strip();
        if (normalizado.length() > LIMITE_DO_TEXTO) {
            throw new RegraDeNegocioException("O texto da review deve ter no máximo " + LIMITE_DO_TEXTO
                    + " caracteres");
        }
        return normalizado;
    }

    public UUID id() { return id; }
    public UUID autorId() { return autorId; }
    public UUID jogoId() { return jogoId; }
    public NotaAvaliacao notaGeral() { return notaGeral; }
    public String texto() { return texto; }
    public List<NotaPorCategoria> notasPorCategoria() { return notasPorCategoria; }
    public Instant criadaEm() { return criadaEm; }
    public Instant atualizadaEm() { return atualizadaEm; }
}
