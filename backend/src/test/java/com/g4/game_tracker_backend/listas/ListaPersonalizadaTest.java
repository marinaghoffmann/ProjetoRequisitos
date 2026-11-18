package com.g4.game_tracker_backend.listas;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.g4.game_tracker_backend.dominio.biblioteca.listas.ListaPersonalizada;
import com.g4.game_tracker_backend.dominio.compartilhado.AcessoNegadoException;
import com.g4.game_tracker_backend.dominio.compartilhado.RecursoNaoEncontradoException;
import com.g4.game_tracker_backend.dominio.compartilhado.RegraDeNegocioException;

class ListaPersonalizadaTest {

    private static final Instant AGORA = Instant.parse("2026-11-16T12:00:00Z");
    private final UUID dono = UUID.randomUUID();
    private final UUID outro = UUID.randomUUID();

    private ListaPersonalizada lista() {
        return ListaPersonalizada.criar(dono, "  Melhores RPGs  ", "Meus favoritos", AGORA);
    }

    @Test
    void tituloSemEspacosNasPontasEChaveEmMinusculas() {
        ListaPersonalizada lista = lista();
        assertEquals("Melhores RPGs", lista.titulo());
        assertEquals("melhores rpgs", lista.chaveDoTitulo());
        assertEquals(lista.chaveDoTitulo(), ListaPersonalizada.chaveDoTitulo(" MELHORES rpgs "));
    }

    @Test
    void tituloObrigatorioEComLimite() {
        assertThrows(RegraDeNegocioException.class, () -> ListaPersonalizada.criar(dono, "  ", null, AGORA));
        assertThrows(RegraDeNegocioException.class, () -> ListaPersonalizada.criar(dono,
                "a".repeat(ListaPersonalizada.LIMITE_DO_TITULO + 1), null, AGORA));
    }

    @Test
    void adicionaJogoComNotaExplicativa() {
        UUID jogo = UUID.randomUUID();
        ListaPersonalizada lista = lista().adicionarJogo(dono, jogo, " Trilha sonora perfeita ", AGORA);
        assertEquals(1, lista.totalJogos());
        assertEquals("Trilha sonora perfeita", lista.item(jogo).orElseThrow().notaExplicativa());
    }

    @Test
    void mesmoJogoDuasVezesERecusado() {
        UUID jogo = UUID.randomUUID();
        ListaPersonalizada lista = lista().adicionarJogo(dono, jogo, null, AGORA);
        RegraDeNegocioException erro = assertThrows(RegraDeNegocioException.class,
                () -> lista.adicionarJogo(dono, jogo, null, AGORA));
        assertEquals("Este jogo já está na lista", erro.getMessage());
    }

    @Test
    void centesimoPrimeiroJogoERecusado() {
        ListaPersonalizada lista = lista();
        for (int i = 0; i < ListaPersonalizada.LIMITE_DE_JOGOS; i++) {
            lista = lista.adicionarJogo(dono, UUID.randomUUID(), null, AGORA);
        }
        ListaPersonalizada cheia = lista;
        RegraDeNegocioException erro = assertThrows(RegraDeNegocioException.class,
                () -> cheia.adicionarJogo(dono, UUID.randomUUID(), null, AGORA));
        assertEquals("A lista atingiu o limite de 100 jogos", erro.getMessage());
    }

    @Test
    void editaNotaERemoveJogo() {
        UUID jogo = UUID.randomUUID();
        ListaPersonalizada lista = lista().adicionarJogo(dono, jogo, "antes", AGORA)
                .editarNotaExplicativa(dono, jogo, "depois", AGORA);
        assertEquals("depois", lista.item(jogo).orElseThrow().notaExplicativa());
        assertTrue(lista.removerJogo(dono, jogo, AGORA).itens().isEmpty());
        assertThrows(RecursoNaoEncontradoException.class, () -> lista().removerJogo(dono, jogo, AGORA));
    }

    @Test
    void apenasODonoAlteraALista() {
        ListaPersonalizada lista = lista();
        assertThrows(AcessoNegadoException.class, () -> lista.editar(outro, "Outro título", null, AGORA));
        assertThrows(AcessoNegadoException.class, () -> lista.adicionarJogo(outro, UUID.randomUUID(), null, AGORA));
        assertThrows(AcessoNegadoException.class, () -> lista.exigirDono(outro));
    }
}
