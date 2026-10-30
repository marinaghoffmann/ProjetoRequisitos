package com.g4.game_tracker_backend.dominio.forum;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

import com.g4.game_tracker_backend.dominio.compartilhado.RegraDeNegocioException;

/**
 * Resposta dentro de um tópico (F-13). Pode responder diretamente ao tópico (respostaPaiId nulo)
 * ou a outra resposta. Só é alterada pelo agregado TopicoDeForum, que aplica as regras de status,
 * propriedade e prazo de edição. A exclusão é lógica, para manter a árvore de respostas.
 */
public final class RespostaForum {

    public static final int LIMITE_DO_CONTEUDO = 5000;

    private final UUID id;
    private final UUID autorId;
    private final UUID respostaPaiId;
    private final String conteudo;
    private final boolean removida;
    private final Instant publicadaEm;
    private final Instant editadaEm;

    private RespostaForum(UUID id, UUID autorId, UUID respostaPaiId, String conteudo, boolean removida,
            Instant publicadaEm, Instant editadaEm) {
        this.id = Objects.requireNonNull(id, "O identificador da resposta é obrigatório");
        this.autorId = Objects.requireNonNull(autorId, "O autor da resposta é obrigatório");
        this.respostaPaiId = respostaPaiId;
        this.conteudo = validarConteudo(conteudo);
        this.removida = removida;
        this.publicadaEm = Objects.requireNonNull(publicadaEm, "A data de publicação é obrigatória");
        this.editadaEm = editadaEm;
    }

    static RespostaForum criar(UUID autorId, UUID respostaPaiId, String conteudo, Instant agora) {
        return new RespostaForum(UUID.randomUUID(), autorId, respostaPaiId, conteudo, false, agora, null);
    }

    public static RespostaForum reconstituir(UUID id, UUID autorId, UUID respostaPaiId, String conteudo,
            boolean removida, Instant publicadaEm, Instant editadaEm) {
        return new RespostaForum(id, autorId, respostaPaiId, conteudo, removida, publicadaEm, editadaEm);
    }

    RespostaForum editar(String novoConteudo, Instant agora) {
        return new RespostaForum(id, autorId, respostaPaiId, novoConteudo, removida, publicadaEm, agora);
    }

    RespostaForum remover() {
        return new RespostaForum(id, autorId, respostaPaiId, conteudo, true, publicadaEm, editadaEm);
    }

    public boolean pertenceA(UUID usuarioId) {
        return autorId.equals(usuarioId);
    }

    private static String validarConteudo(String conteudo) {
        if (conteudo == null || conteudo.isBlank()) {
            throw new RegraDeNegocioException("A resposta não pode ficar vazia");
        }
        String normalizado = conteudo.strip();
        if (normalizado.length() > LIMITE_DO_CONTEUDO) {
            throw new RegraDeNegocioException("A resposta deve ter no máximo " + LIMITE_DO_CONTEUDO + " caracteres");
        }
        return normalizado;
    }

    public UUID id() { return id; }
    public UUID autorId() { return autorId; }
    public UUID respostaPaiId() { return respostaPaiId; }
    public String conteudo() { return conteudo; }
    public boolean removida() { return removida; }
    public Instant publicadaEm() { return publicadaEm; }
    public Instant editadaEm() { return editadaEm; }
}
