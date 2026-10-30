package com.g4.game_tracker_backend.dominio.forum;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import com.g4.game_tracker_backend.dominio.compartilhado.AcessoNegadoException;
import com.g4.game_tracker_backend.dominio.compartilhado.RecursoNaoEncontradoException;
import com.g4.game_tracker_backend.dominio.compartilhado.RegraDeNegocioException;

/**
 * Agregado do tópico do fórum (F-13), com as suas respostas encadeadas. É imutável: cada
 * alteração devolve uma nova instância.
 *
 * RN 1: tópico ENCERRADO não aceita novas respostas, edições nem exclusões.
 * RN 2: só o autor edita ou exclui a sua postagem, e a edição só vale até 48 horas após a publicação.
 * RN 3: toda criação, edição ou exclusão de resposta atualiza o contador e a última atividade.
 */
public final class TopicoDeForum {

    public static final Duration PRAZO_DE_EDICAO = Duration.ofHours(48);
    public static final String CATEGORIA_GERAL = "Geral";

    private static final String ENCERRADO = "Este tópico está encerrado";
    private static final String APENAS_O_AUTOR = "Apenas o autor pode alterar esta postagem";
    private static final String PRAZO_ESGOTADO = "O prazo de 48 horas para edição terminou";

    private final UUID id;
    private final UUID autorId;
    private final UUID jogoId;
    private final String categoria;
    private final String titulo;
    private final String conteudo;
    private final StatusTopico status;
    private final boolean removido;
    private final Instant publicadoEm;
    private final Instant editadoEm;
    private final Instant dataUltimaAtividade;
    private final List<RespostaForum> respostas;

    private TopicoDeForum(UUID id, UUID autorId, UUID jogoId, String categoria, String titulo, String conteudo,
            StatusTopico status, boolean removido, Instant publicadoEm, Instant editadoEm,
            Instant dataUltimaAtividade, List<RespostaForum> respostas) {
        this.id = Objects.requireNonNull(id, "O identificador do tópico é obrigatório");
        this.autorId = Objects.requireNonNull(autorId, "O autor do tópico é obrigatório");
        this.jogoId = jogoId;
        this.categoria = categoria == null || categoria.isBlank()
                ? CATEGORIA_GERAL
                : texto(categoria, 60, "", "A categoria deve ter no máximo 60 caracteres");
        this.titulo = texto(titulo, 150, "O título do tópico é obrigatório",
                "O título do tópico deve ter no máximo 150 caracteres");
        this.conteudo = texto(conteudo, 10000, "O conteúdo do tópico é obrigatório",
                "O conteúdo do tópico deve ter no máximo 10000 caracteres");
        this.status = Objects.requireNonNull(status, "O status do tópico é obrigatório");
        this.removido = removido;
        this.publicadoEm = Objects.requireNonNull(publicadoEm, "A data de publicação é obrigatória");
        this.editadoEm = editadoEm;
        this.dataUltimaAtividade = Objects.requireNonNull(dataUltimaAtividade,
                "A data da última atividade é obrigatória");
        this.respostas = List.copyOf(respostas == null ? List.of() : respostas);
    }

    /** Todo tópico nasce ABERTO e sem respostas. jogoId nulo indica um tópico geral. */
    public static TopicoDeForum criar(UUID autorId, UUID jogoId, String categoria, String titulo, String conteudo,
            Instant agora) {
        return new TopicoDeForum(UUID.randomUUID(), autorId, jogoId, categoria, titulo, conteudo,
                StatusTopico.ABERTO, false, agora, null, agora, List.of());
    }

    public static TopicoDeForum reconstituir(UUID id, UUID autorId, UUID jogoId, String categoria, String titulo,
            String conteudo, StatusTopico status, boolean removido, Instant publicadoEm, Instant editadoEm,
            Instant dataUltimaAtividade, List<RespostaForum> respostas) {
        return new TopicoDeForum(id, autorId, jogoId, categoria, titulo, conteudo, status, removido, publicadoEm,
                editadoEm, dataUltimaAtividade, respostas);
    }

    // ---- Tópico ----

    public TopicoDeForum editar(UUID solicitanteId, String novoTitulo, String novoConteudo, String novaCategoria,
            Instant agora) {
        exigirNaoEncerrado();
        exigirAutor(autorId, solicitanteId);
        exigirDentroDoPrazo(publicadoEm, agora);
        return new TopicoDeForum(id, autorId, jogoId, novaCategoria, novoTitulo, novoConteudo, status, removido,
                publicadoEm, agora, agora, respostas);
    }

    /** O autor marca o tópico como resolvido, reabre ou encerra; um tópico encerrado não volta atrás. */
    public TopicoDeForum alterarStatus(UUID solicitanteId, StatusTopico novoStatus, Instant agora) {
        Objects.requireNonNull(novoStatus, "O status do tópico é obrigatório");
        exigirNaoEncerrado();
        exigirAutor(autorId, solicitanteId);
        return new TopicoDeForum(id, autorId, jogoId, categoria, titulo, conteudo, novoStatus, removido,
                publicadoEm, editadoEm, agora, respostas);
    }

    /** Exclusão lógica: o tópico sai das consultas, mas as respostas continuam guardadas. */
    public TopicoDeForum remover(UUID solicitanteId) {
        exigirNaoEncerrado();
        exigirAutor(autorId, solicitanteId);
        return new TopicoDeForum(id, autorId, jogoId, categoria, titulo, conteudo, status, true, publicadoEm,
                editadoEm, dataUltimaAtividade, respostas);
    }

    // ---- Respostas ----

    /** Publica uma resposta ao tópico ou, com respostaPaiId, a outra resposta do mesmo tópico. */
    public TopicoDeForum responder(UUID autorDaResposta, UUID respostaPaiId, String texto, Instant agora) {
        exigirNaoEncerrado();
        if (respostaPaiId != null && resposta(respostaPaiId).removida()) {
            throw new RegraDeNegocioException("Não é possível responder a uma resposta removida");
        }
        List<RespostaForum> novas = new ArrayList<>(respostas);
        novas.add(RespostaForum.criar(autorDaResposta, respostaPaiId, texto, agora));
        return comRespostas(novas, agora);
    }

    public TopicoDeForum editarResposta(UUID respostaId, UUID solicitanteId, String novoConteudo, Instant agora) {
        exigirNaoEncerrado();
        RespostaForum atual = resposta(respostaId);
        exigirAutor(atual.autorId(), solicitanteId);
        if (atual.removida()) {
            throw new RegraDeNegocioException("Esta resposta foi removida");
        }
        exigirDentroDoPrazo(atual.publicadaEm(), agora);
        return comRespostas(substituir(atual.editar(novoConteudo, agora)), agora);
    }

    /** Exclusão lógica: a resposta deixa de contar, mas continua na árvore para as respostas filhas. */
    public TopicoDeForum excluirResposta(UUID respostaId, UUID solicitanteId, Instant agora) {
        exigirNaoEncerrado();
        RespostaForum atual = resposta(respostaId);
        exigirAutor(atual.autorId(), solicitanteId);
        if (atual.removida()) {
            throw new RegraDeNegocioException("Esta resposta foi removida");
        }
        return comRespostas(substituir(atual.remover()), agora);
    }

    public RespostaForum resposta(UUID respostaId) {
        return respostas.stream()
                .filter(resposta -> resposta.id().equals(respostaId))
                .findFirst()
                .orElseThrow(() -> new RecursoNaoEncontradoException("Resposta não encontrada neste tópico"));
    }

    /** A resposta publicada por último; útil logo depois de responder(...). */
    public RespostaForum ultimaResposta() {
        return respostas.getLast();
    }

    /** RN 3: respostas removidas não entram no contador. */
    public int totalRespostas() {
        return (int) respostas.stream().filter(resposta -> !resposta.removida()).count();
    }

    public boolean pertenceA(UUID usuarioId) {
        return autorId.equals(usuarioId);
    }

    /** Até quando uma postagem publicada em publicadaEm pode ser editada pelo autor. */
    public static Instant editavelAte(Instant publicadaEm) {
        return publicadaEm.plus(PRAZO_DE_EDICAO);
    }

    private TopicoDeForum comRespostas(List<RespostaForum> novas, Instant agora) {
        return new TopicoDeForum(id, autorId, jogoId, categoria, titulo, conteudo, status, removido, publicadoEm,
                editadoEm, agora, novas);
    }

    private List<RespostaForum> substituir(RespostaForum alterada) {
        return respostas.stream()
                .map(resposta -> resposta.id().equals(alterada.id()) ? alterada : resposta)
                .toList();
    }

    private void exigirNaoEncerrado() {
        if (status == StatusTopico.ENCERRADO) {
            throw new RegraDeNegocioException(ENCERRADO);
        }
    }

    private static void exigirAutor(UUID dono, UUID solicitanteId) {
        if (!dono.equals(solicitanteId)) {
            throw new AcessoNegadoException(APENAS_O_AUTOR);
        }
    }

    private static void exigirDentroDoPrazo(Instant publicadaEm, Instant agora) {
        if (agora.isAfter(editavelAte(publicadaEm))) {
            throw new RegraDeNegocioException(PRAZO_ESGOTADO);
        }
    }

    private static String texto(String valor, int limite, String mensagemObrigatorio, String mensagemLimite) {
        if (valor == null || valor.isBlank()) {
            throw new RegraDeNegocioException(mensagemObrigatorio);
        }
        String normalizado = valor.strip();
        if (normalizado.length() > limite) {
            throw new RegraDeNegocioException(mensagemLimite);
        }
        return normalizado;
    }

    public UUID id() { return id; }
    public UUID autorId() { return autorId; }
    public UUID jogoId() { return jogoId; }
    public String categoria() { return categoria; }
    public String titulo() { return titulo; }
    public String conteudo() { return conteudo; }
    public StatusTopico status() { return status; }
    public boolean removido() { return removido; }
    public Instant publicadoEm() { return publicadoEm; }
    public Instant editadoEm() { return editadoEm; }
    public Instant dataUltimaAtividade() { return dataUltimaAtividade; }
    public List<RespostaForum> respostas() { return respostas; }
}
