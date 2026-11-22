package com.g4.game_tracker_backend.infraestrutura.persistencia.biblioteca.acervo;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.g4.game_tracker_backend.aplicacao.biblioteca.acervo.GerenciarBiblioteca;
import com.g4.game_tracker_backend.aplicacao.compartilhado.ExecutorDeTransacao;
import com.g4.game_tracker_backend.aplicacao.identidade.ConsultarUsuarioAtual;
import com.g4.game_tracker_backend.dominio.biblioteca.perfil.PerfilFavoritosRepositorio;
import com.g4.game_tracker_backend.dominio.biblioteca.acervo.BibliotecaRepositorio;
import com.g4.game_tracker_backend.dominio.biblioteca.acervo.CatalogoDeJogos;

@Configuration
public class BibliotecaConfig {

    @Bean
    GerenciarBiblioteca gerenciarBiblioteca(BibliotecaRepositorio repositorio, CatalogoDeJogos catalogo,
            ConsultarUsuarioAtual usuarioAtual, PerfilFavoritosRepositorio favoritos,
            ExecutorDeTransacao transacao) {
        return new GerenciarBiblioteca(repositorio, catalogo, usuarioAtual, favoritos, transacao);
    }
}