package com.g4.game_tracker_backend.infraestrutura.persistencia.catalogo;

import java.util.List;
import java.util.UUID;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import com.g4.game_tracker_backend.dominio.catalogo.VinculosDoJogo;

/**
 * Implementa a porta da F-01 RN 3 consultando, por SQL, as tabelas dos contextos que guardam o Id do jogo.
 * Fica na infraestrutura para que o domínio e a aplicação do catálogo não dependam desses contextos.
 *
 * A ficha de estatísticas (F-17) não conta como vínculo: ela é derivada dos outros dados.
 */
@Component
class VinculosDoJogoNoBanco implements VinculosDoJogo {

    /** Tabela e coluna de cada vínculo. Ao criar um contexto que referencia jogos, inclua-o aqui. */
    private static final List<String[]> VINCULOS = List.of(
            new String[] { "reviews", "jogo_id" },                    // F-02
            new String[] { "listas_itens", "jogo_id" },               // F-04
            new String[] { "itens_wishlist", "jogo_id" },             // F-06
            new String[] { "jogando_agora", "jogo_id" },              // F-07
            new String[] { "perfil_jogos_favoritos", "jogo_id" },     // F-08
            new String[] { "similaridades_jogos", "jogo_a_id" },      // F-10
            new String[] { "similaridades_jogos", "jogo_b_id" },      // F-10
            new String[] { "registros_conclusao", "jogo_id" },        // F-11
            new String[] { "lancamentos_futuros", "jogo_id" },        // F-12
            new String[] { "lancamentos_interesses", "jogo_id" },     // F-12
            new String[] { "forum_topicos", "jogo_id" },              // F-13
            new String[] { "journal_entradas", "jogo_id" },           // F-15 e F-16
            new String[] { "anuncios_marketplace", "jogo_id" },       // F-18
            new String[] { "marketplace_pedidos", "jogo_id" });       // F-20

    private final JdbcTemplate jdbc;

    VinculosDoJogoNoBanco(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public boolean possuiVinculos(UUID jogoId) {
        return VINCULOS.stream().anyMatch(v -> existe(v[0], v[1], jogoId));
    }

    private boolean existe(String tabela, String coluna, UUID jogoId) {
        // Tabela e coluna vêm da constante acima, nunca do usuário.
        List<Integer> achou = jdbc.queryForList(
                "SELECT 1 FROM " + tabela + " WHERE " + coluna + " = ? FETCH FIRST 1 ROWS ONLY", Integer.class,
                jogoId);
        return !achou.isEmpty();
    }
}
