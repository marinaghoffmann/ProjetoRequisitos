package com.g4.game_tracker_backend.dominio.forum;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.UUID;

/**
 * Padrão <b>Iterator</b> (GoF) da F-13: percorre as respostas encadeadas de um {@link TopicoDeForum} em
 * profundidade, com cada resposta antes das suas filhas e as irmãs na ordem de publicação.
 *
 * Quem percorre (a API que monta o tópico, a moderação que conta o que está visível) recebe as respostas já na
 * ordem da conversa e com o nível de recuo, sem conhecer como o agregado guarda o encadeamento
 * ({@code respostaPaiId} numa lista plana). A pilha guarda só os próximos ramos a visitar.
 *
 * Uma resposta cujo pai não está no tópico é tratada como resposta direta ao tópico, para nunca sumir.
 */
public final class IteradorDeRespostas implements Iterator<IteradorDeRespostas.RespostaNaArvore> {

    /** Uma resposta e a sua profundidade: 0 responde ao tópico, 1 responde a uma resposta de nível 0 etc. */
    public record RespostaNaArvore(RespostaForum resposta, int nivel) {
    }

    private static final Comparator<RespostaForum> ORDEM_DE_PUBLICACAO = Comparator
            .comparing(RespostaForum::publicadaEm).thenComparing(RespostaForum::id);

    private final Map<UUID, List<RespostaForum>> filhasPorPai = new HashMap<>();
    private final Deque<RespostaNaArvore> pendentes = new ArrayDeque<>();

    IteradorDeRespostas(List<RespostaForum> respostas) {
        Set<UUID> ids = new HashSet<>();
        respostas.forEach(r -> ids.add(r.id()));
        List<RespostaForum> raizes = respostas.stream()
                .filter(r -> r.respostaPaiId() == null || !ids.contains(r.respostaPaiId()))
                .sorted(ORDEM_DE_PUBLICACAO)
                .toList();
        respostas.stream()
                .filter(r -> r.respostaPaiId() != null && ids.contains(r.respostaPaiId()))
                .forEach(r -> filhasPorPai.computeIfAbsent(r.respostaPaiId(), pai -> new ArrayList<>()).add(r));
        filhasPorPai.values().forEach(filhas -> filhas.sort(ORDEM_DE_PUBLICACAO));
        empilhar(raizes, 0);
    }

    @Override
    public boolean hasNext() {
        return !pendentes.isEmpty();
    }

    @Override
    public RespostaNaArvore next() {
        if (pendentes.isEmpty()) {
            throw new NoSuchElementException("Não há mais respostas neste tópico");
        }
        RespostaNaArvore atual = pendentes.pop();
        empilhar(filhasPorPai.getOrDefault(atual.resposta().id(), List.of()), atual.nivel() + 1);
        return atual;
    }

    /** Empilha de trás para a frente para que a primeira publicada seja a próxima visitada. */
    private void empilhar(List<RespostaForum> irmas, int nivel) {
        for (int i = irmas.size() - 1; i >= 0; i--) {
            pendentes.push(new RespostaNaArvore(irmas.get(i), nivel));
        }
    }
}
