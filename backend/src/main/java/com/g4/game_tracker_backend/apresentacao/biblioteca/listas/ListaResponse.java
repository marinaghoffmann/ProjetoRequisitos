package com.g4.game_tracker_backend.apresentacao.biblioteca.listas;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.g4.game_tracker_backend.aplicacao.biblioteca.listas.ConsultarListas.JogoDaLista;
import com.g4.game_tracker_backend.dominio.biblioteca.listas.ItemDaLista;
import com.g4.game_tracker_backend.dominio.biblioteca.listas.ListaPersonalizada;

/**
 * Lista com o total de jogos e os itens em ordem de inclusão. limiteDeJogos é o máximo da RN 3, para a
 * tela mostrar o contador "N/100".
 */
public record ListaResponse(UUID id, UUID donoId, String nomeDono, String titulo, String descricao, int totalJogos,
        int limiteDeJogos, Instant criadaEm, Instant atualizadaEm, List<Item> itens) {

    public record Item(UUID itemId, UUID jogoId, String jogoTitulo, String jogoCapaUrl, String jogoStatus,
            String notaExplicativa, Instant incluidoEm) {

        static Item de(ItemDaLista item, Map<UUID, JogoDaLista> jogos) {
            JogoDaLista jogo = jogos.get(item.jogoId());
            return new Item(item.id(), item.jogoId(), jogo == null ? null : jogo.titulo(),
                    jogo == null ? null : jogo.capaUrl(), jogo == null ? null : jogo.status(),
                    item.notaExplicativa(), item.incluidoEm());
        }
    }

    static ListaResponse de(ListaPersonalizada lista, Map<UUID, String> nomes, Map<UUID, JogoDaLista> jogos) {
        return new ListaResponse(lista.id(), lista.donoId(), nomes.get(lista.donoId()), lista.titulo(),
                lista.descricao(), lista.totalJogos(), ListaPersonalizada.LIMITE_DE_JOGOS, lista.criadaEm(),
                lista.atualizadaEm(), lista.itens().stream().map(item -> Item.de(item, jogos)).toList());
    }
}
