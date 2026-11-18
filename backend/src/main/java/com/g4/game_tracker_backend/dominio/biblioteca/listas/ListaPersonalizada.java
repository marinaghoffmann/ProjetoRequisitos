package com.g4.game_tracker_backend.dominio.biblioteca.listas;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import com.g4.game_tracker_backend.dominio.compartilhado.AcessoNegadoException;
import com.g4.game_tracker_backend.dominio.compartilhado.RecursoNaoEncontradoException;
import com.g4.game_tracker_backend.dominio.compartilhado.RegraDeNegocioException;

/**
 * Agregado da lista personalizada (F-04), com os seus itens. É imutável: cada alteração devolve uma nova
 * instância.
 *
 * RN 1: o título é único por dono, sem diferenciar maiúsculas e minúsculas e sem os espaços das pontas. A
 * checagem precisa do repositório (caso de uso), e o banco garante pela chave do título (chaveDoTitulo).
 * RN 2: um jogo aparece no máximo uma vez na lista ("Este jogo já está na lista").
 * RN 3: no máximo 100 jogos por lista ("A lista atingiu o limite de 100 jogos").
 * Só o dono edita, exclui ou mexe nos itens.
 */
public final class ListaPersonalizada {

    public static final int LIMITE_DE_JOGOS = 100;
    public static final int LIMITE_DO_TITULO = 80;
    public static final int LIMITE_DA_DESCRICAO = 500;
    public static final int LIMITE_DA_NOTA = 500;

    public static final String TITULO_REPETIDO = "Você já possui uma lista com este título";
    static final String APENAS_O_DONO = "Apenas o dono pode alterar esta lista";

    private final UUID id;
    private final UUID donoId;
    private final String titulo;
    private final String descricao;
    private final List<ItemDaLista> itens;
    private final Instant criadaEm;
    private final Instant atualizadaEm;

    private ListaPersonalizada(UUID id, UUID donoId, String titulo, String descricao, List<ItemDaLista> itens,
            Instant criadaEm, Instant atualizadaEm) {
        this.id = Objects.requireNonNull(id, "O identificador da lista é obrigatório");
        this.donoId = Objects.requireNonNull(donoId, "O dono da lista é obrigatório");
        this.titulo = titulo(titulo);
        this.descricao = opcional(descricao, LIMITE_DA_DESCRICAO,
                "A descrição da lista deve ter no máximo " + LIMITE_DA_DESCRICAO + " caracteres");
        this.itens = List.copyOf(itens == null ? List.of() : itens);
        if (this.itens.size() > LIMITE_DE_JOGOS) {
            throw new RegraDeNegocioException("A lista atingiu o limite de " + LIMITE_DE_JOGOS + " jogos");
        }
        this.criadaEm = Objects.requireNonNull(criadaEm, "A data de criação é obrigatória");
        this.atualizadaEm = Objects.requireNonNull(atualizadaEm, "A data de atualização é obrigatória");
    }

    public static ListaPersonalizada criar(UUID donoId, String titulo, String descricao, Instant agora) {
        return new ListaPersonalizada(UUID.randomUUID(), donoId, titulo, descricao, List.of(), agora, agora);
    }

    public static ListaPersonalizada reconstituir(UUID id, UUID donoId, String titulo, String descricao,
            List<ItemDaLista> itens, Instant criadaEm, Instant atualizadaEm) {
        return new ListaPersonalizada(id, donoId, titulo, descricao, itens, criadaEm, atualizadaEm);
    }

    /** Forma do título usada na RN 1: sem os espaços das pontas e em minúsculas. */
    public static String chaveDoTitulo(String titulo) {
        return titulo == null ? "" : titulo.strip().toLowerCase(Locale.ROOT);
    }

    // ---- Lista ----

    public ListaPersonalizada editar(UUID solicitanteId, String novoTitulo, String novaDescricao, Instant agora) {
        exigirDono(solicitanteId);
        return new ListaPersonalizada(id, donoId, novoTitulo, novaDescricao, itens, criadaEm, agora);
    }

    public void exigirDono(UUID solicitanteId) {
        if (!donoId.equals(solicitanteId)) {
            throw new AcessoNegadoException(APENAS_O_DONO);
        }
    }

    // ---- Itens ----

    public ListaPersonalizada adicionarJogo(UUID solicitanteId, UUID jogoId, String notaExplicativa, Instant agora) {
        exigirDono(solicitanteId);
        Objects.requireNonNull(jogoId, "O jogo é obrigatório");
        if (item(jogoId).isPresent()) {
            throw new RegraDeNegocioException("Este jogo já está na lista");
        }
        if (itens.size() >= LIMITE_DE_JOGOS) {
            throw new RegraDeNegocioException("A lista atingiu o limite de " + LIMITE_DE_JOGOS + " jogos");
        }
        List<ItemDaLista> novos = new ArrayList<>(itens);
        novos.add(new ItemDaLista(UUID.randomUUID(), jogoId, nota(notaExplicativa), agora));
        return new ListaPersonalizada(id, donoId, titulo, descricao, novos, criadaEm, agora);
    }

    public ListaPersonalizada editarNotaExplicativa(UUID solicitanteId, UUID jogoId, String novaNota, Instant agora) {
        exigirDono(solicitanteId);
        ItemDaLista atual = itemExistente(jogoId);
        List<ItemDaLista> novos = itens.stream()
                .map(item -> item.equals(atual) ? item.comNota(nota(novaNota)) : item)
                .toList();
        return new ListaPersonalizada(id, donoId, titulo, descricao, novos, criadaEm, agora);
    }

    public ListaPersonalizada removerJogo(UUID solicitanteId, UUID jogoId, Instant agora) {
        exigirDono(solicitanteId);
        ItemDaLista atual = itemExistente(jogoId);
        List<ItemDaLista> novos = itens.stream().filter(item -> !item.equals(atual)).toList();
        return new ListaPersonalizada(id, donoId, titulo, descricao, novos, criadaEm, agora);
    }

    public Optional<ItemDaLista> item(UUID jogoId) {
        return itens.stream().filter(item -> item.jogoId().equals(jogoId)).findFirst();
    }

    public int totalJogos() {
        return itens.size();
    }

    public boolean pertenceA(UUID usuarioId) {
        return donoId.equals(usuarioId);
    }

    public String chaveDoTitulo() {
        return chaveDoTitulo(titulo);
    }

    private ItemDaLista itemExistente(UUID jogoId) {
        return item(jogoId).orElseThrow(() -> new RecursoNaoEncontradoException("Este jogo não está na lista"));
    }

    private static String titulo(String valor) {
        if (valor == null || valor.isBlank()) {
            throw new RegraDeNegocioException("O título da lista é obrigatório");
        }
        String normalizado = valor.strip();
        if (normalizado.length() > LIMITE_DO_TITULO) {
            throw new RegraDeNegocioException("O título da lista deve ter no máximo " + LIMITE_DO_TITULO
                    + " caracteres");
        }
        return normalizado;
    }

    private static String nota(String valor) {
        return opcional(valor, LIMITE_DA_NOTA,
                "A nota explicativa deve ter no máximo " + LIMITE_DA_NOTA + " caracteres");
    }

    private static String opcional(String valor, int limite, String mensagem) {
        String normalizado = valor == null ? "" : valor.strip();
        if (normalizado.length() > limite) {
            throw new RegraDeNegocioException(mensagem);
        }
        return normalizado;
    }

    public UUID id() { return id; }
    public UUID donoId() { return donoId; }
    public String titulo() { return titulo; }
    public String descricao() { return descricao; }
    public List<ItemDaLista> itens() { return itens; }
    public Instant criadaEm() { return criadaEm; }
    public Instant atualizadaEm() { return atualizadaEm; }
}
