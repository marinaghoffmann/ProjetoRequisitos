package com.g4.game_tracker_backend.bdd;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;

import com.g4.game_tracker_backend.aplicacao.biblioteca.MarcarJogandoAgora;
import com.g4.game_tracker_backend.aplicacao.biblioteca.ResumirBiblioteca;
import com.g4.game_tracker_backend.aplicacao.biblioteca.listas.GerenciarListas;
import com.g4.game_tracker_backend.aplicacao.descoberta.BuscarNoGameTracker;
import com.g4.game_tracker_backend.aplicacao.wishlist.AdicionarNaWishlist;
import com.g4.game_tracker_backend.dominio.catalogo.Desenvolvedora;
import com.g4.game_tracker_backend.dominio.catalogo.DesenvolvedoraRepositorio;
import com.g4.game_tracker_backend.dominio.catalogo.Jogo;
import com.g4.game_tracker_backend.dominio.catalogo.JogoRepositorio;
import com.g4.game_tracker_backend.dominio.identidade.Papel;
import com.g4.game_tracker_backend.dominio.identidade.Usuario;
import com.g4.game_tracker_backend.dominio.identidade.UsuarioRepositorio;

/** Consultas criadas para as telas do frontend: busca global e contadores do menu. */
@SpringBootTest
@ActiveProfiles("test")
class ConsultasDasTelasTest {

    @Autowired BuscarNoGameTracker buscar;
    @Autowired ResumirBiblioteca resumir;
    @Autowired MarcarJogandoAgora marcarJogandoAgora;
    @Autowired AdicionarNaWishlist adicionarNaWishlist;
    @Autowired GerenciarListas listas;
    @Autowired JogoRepositorio jogos;
    @Autowired DesenvolvedoraRepositorio desenvolvedoras;
    @Autowired UsuarioRepositorio usuarios;

    @AfterEach
    void sair() {
        SecurityContextHolder.clearContext();
    }

    private Usuario jogador() {
        return usuarios.salvar(Usuario.reconstituir(UUID.randomUUID(), "Jogadora", UUID.randomUUID() + "@teste.local",
                "hash", Papel.COMUM, true));
    }

    private Jogo jogo(String titulo) {
        Desenvolvedora estudio = desenvolvedoras.salvar(Desenvolvedora.criar("Estúdio " + UUID.randomUUID(), null,
                null, null, null));
        return jogos.salvar(Jogo.criarManual(titulo, null, null, estudio.id(), Set.of(), null, null, null));
    }

    @Test
    void buscaEncontraJogosAtivosEListasPeloTitulo() {
        String marca = UUID.randomUUID().toString().substring(0, 8);
        Jogo ativo = jogo("Busca " + marca + " Ativo");
        Jogo inativo = jogos.salvar(jogo("Busca " + marca + " Inativo").desativar());
        Usuario dona = jogador();
        SessaoDeTeste.autenticarComo(dona);
        var lista = listas.criar("Favoritos busca " + marca, "Lista de teste");

        var resultado = buscar.executar("  busca " + marca + " ");
        assertTrue(resultado.jogos().stream().anyMatch(j -> j.id().equals(ativo.id())));
        assertFalse(resultado.jogos().stream().anyMatch(j -> j.id().equals(inativo.id())));

        var listasEncontradas = buscar.executar("favoritos busca " + marca).listas();
        assertEquals(1, listasEncontradas.size());
        assertEquals(lista.id(), listasEncontradas.get(0).lista().id());
        assertEquals("Jogadora", listasEncontradas.get(0).nomeDoDono());
    }

    @Test
    void termoCurtoNaoBuscaNada() {
        var resultado = buscar.executar(" a ");
        assertTrue(resultado.jogos().isEmpty());
        assertTrue(resultado.listas().isEmpty());
    }

    @Test
    void resumoContaOsJogosDaBibliotecaDoUsuario() {
        Usuario jogadora = jogador();
        SessaoDeTeste.autenticarComo(jogadora);
        assertEquals(new ResumirBiblioteca.Resumo(0, 0, 0), resumir.executar());

        Jogo a = jogo("Resumo A " + UUID.randomUUID());
        Jogo b = jogo("Resumo B " + UUID.randomUUID());
        marcarJogandoAgora.executar(a.id(), null);
        adicionarNaWishlist.executar(a.id(), null);
        adicionarNaWishlist.executar(b.id(), null);

        var resumo = resumir.executar();
        assertEquals(1, resumo.emAndamento());
        assertEquals(2, resumo.naWishlist());
        assertEquals(0, resumo.jogosZerados());
    }
}
