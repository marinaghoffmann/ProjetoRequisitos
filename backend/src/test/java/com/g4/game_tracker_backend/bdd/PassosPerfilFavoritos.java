package com.g4.game_tracker_backend.bdd;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.List;
import java.util.UUID;

import org.springframework.security.core.context.SecurityContextHolder;

import com.g4.game_tracker_backend.aplicacao.biblioteca.perfil.ConsultarPerfilPublico;
import com.g4.game_tracker_backend.aplicacao.biblioteca.perfil.GerenciarFavoritos;
import com.g4.game_tracker_backend.aplicacao.identidade.EditarPerfil;
import com.g4.game_tracker_backend.dominio.identidade.UsuarioRepositorio;

import io.cucumber.java.pt.Dado;
import io.cucumber.java.pt.Entao;
import io.cucumber.java.pt.Quando;

public class PassosPerfilFavoritos {
    private final DadosDoCenario dados;
    private final ContextoDoCenario contexto;
    private final GerenciarFavoritos favoritos;
    private final ConsultarPerfilPublico consultar;
    private final EditarPerfil editarPerfil;
    private final UsuarioRepositorio usuarios;
    private ConsultarPerfilPublico.PerfilPublico consultado;
    private String bioEsperada;
    private String capaEsperada;

    public PassosPerfilFavoritos(DadosDoCenario dados, ContextoDoCenario contexto,
            GerenciarFavoritos favoritos, ConsultarPerfilPublico consultar,
            EditarPerfil editarPerfil, UsuarioRepositorio usuarios) {
        this.dados = dados;
        this.contexto = contexto;
        this.favoritos = favoritos;
        this.consultar = consultar;
        this.editarPerfil = editarPerfil;
        this.usuarios = usuarios;
    }

    @Quando("o jogador fixa o jogo {string} no slot {int}")
    public void fixaJogo(String jogo, int slot) {
        contexto.executar(() -> favoritos.adicionar(dados.jogo(jogo).id(), slot));
    }

    @Dado("que o jogador tem os 4 slots de favoritos ocupados")
    public void possuiQuatroFavoritos() {
        prepararQuatro("A", "B", "C", "D");
    }

    @Quando("o jogador fixa o jogo {string} como favorito sem escolher slot")
    public void fixaSemSlot(String jogo) {
        contexto.executar(() -> favoritos.adicionar(dados.jogo(jogo).id(), null));
    }

    @Dado("que o jogador tem os favoritos {string} no slot 1, {string} no slot 2, {string} no slot 3 e {string} no slot 4")
    public void prepararQuatro(String a, String b, String c, String d) {
        for (String jogo : List.of(a, b, c, d)) {
            favoritos.adicionar(dados.jogo(jogo).id(), null);
        }
    }

    @Dado("que o jogador tem os favoritos {string} no slot 2 e {string} no slot 1")
    public void prepararDois(String segundo, String primeiro) {
        favoritos.adicionar(dados.jogo(primeiro).id(), 1);
        favoritos.adicionar(dados.jogo(segundo).id(), 2);
    }

    @Dado("que o jogador tem o jogo {string} no slot {int}")
    public void possuiJogo(String jogo, int slot) {
        favoritos.adicionar(dados.jogo(jogo).id(), slot);
    }

    @Quando("o jogador move o favorito {string} para o slot {int}")
    public void move(String jogo, int slot) {
        contexto.executar(() -> favoritos.mover(dados.jogo(jogo).id(), slot));
    }

    @Quando("o jogador remove o favorito {string}")
    public void remove(String jogo) {
        contexto.executar(() -> favoritos.remover(dados.jogo(jogo).id()));
    }

    @Quando("o jogador substitui o slot {int} pelo jogo {string}")
    public void substitui(int slot, String jogo) {
        contexto.executar(() -> favoritos.substituir(slot, dados.jogo(jogo).id()));
    }

    @Quando("o jogador edita a biografia com um texto de {int} caracteres")
    public void editaBio(int tamanho) {
        bioEsperada = "a".repeat(tamanho);
        contexto.executar(() -> editarPerfil.executar(dados.jogadorAtual().nome(), bioEsperada, null));
    }

    @Quando("o jogador define a capa do perfil com a URL {string}")
    public void defineCapa(String url) {
        capaEsperada = url;
        contexto.executar(() -> editarPerfil.executar(dados.jogadorAtual().nome(), null, null, url));
    }

    @Quando("outro usuário consulta o perfil público do jogador")
    public void consultaOutro() {
        UUID dono = dados.jogadorAtual().id();
        SessaoDeTeste.autenticarComo(dados.jogador("visitante"));
        consultado = consultar.consultar(dono);
    }

    @Quando("um visitante sem login consulta o perfil público do jogador")
    public void consultaAnonima() {
        SecurityContextHolder.clearContext();
        consultado = consultar.consultar(dados.jogadorAtual().id());
    }

    @Entao("o slot {int} do perfil deve conter {string}")
    public void confereSlot(int slot, String jogo) {
        assertNull(contexto.erro());
        var item = perfil().favoritos().get(slot - 1);
        assertEquals(slot, item.slot());
        assertEquals(dados.jogo(jogo).id(), item.jogoId());
    }

    @Entao("os favoritos devem ficar na ordem {string}, {string}, {string}, {string}")
    public void confereQuatro(String a, String b, String c, String d) {
        conferirOrdem(List.of(a, b, c, d));
    }

    @Entao("os favoritos devem ficar na ordem {string}, {string}, {string}")
    public void confereTres(String a, String b, String c) {
        conferirOrdem(List.of(a, b, c));
    }

    @Entao("os favoritos devem ficar na ordem {string}, {string}")
    public void confereDois(String a, String b) {
        conferirOrdem(List.of(a, b));
    }

    @Entao("a biografia deve ser salva no perfil")
    public void confereBio() {
        assertNull(contexto.erro());
        assertEquals(bioEsperada, usuarios.buscarPorId(dados.jogadorAtual().id()).orElseThrow().bio());
        assertEquals(bioEsperada, perfil().bio());
    }

    @Entao("a capa deve ser aceita")
    public void capaAceita() {
        assertNull(contexto.erro());
        assertEquals(capaEsperada, perfil().bannerUrl());
    }

    @Entao("a capa deve ser recusada")
    public void capaRecusada() {
        assertNotNull(contexto.erro());
        assertNull(perfil().bannerUrl());
    }

    private ConsultarPerfilPublico.PerfilPublico perfil() {
        return consultado == null ? consultar.consultar(dados.jogadorAtual().id()) : consultado;
    }

    private void conferirOrdem(List<String> nomes) {
        assertNull(contexto.erro());
        assertEquals(nomes.stream().map(nome -> dados.jogo(nome).id()).toList(),
                perfil().favoritos().stream().map(ConsultarPerfilPublico.Favorito::jogoId).toList());
        for (int i = 0; i < nomes.size(); i++) assertEquals(i + 1, perfil().favoritos().get(i).slot());
    }
}

