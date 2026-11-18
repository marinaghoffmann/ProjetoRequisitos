package com.g4.game_tracker_backend.aplicacao.biblioteca.perfil;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.g4.game_tracker_backend.dominio.biblioteca.perfil.PerfilFavoritosRepositorio;
import com.g4.game_tracker_backend.aplicacao.moderacao.EstadoPublico;
import com.g4.game_tracker_backend.dominio.moderacao.TipoConteudo;
import com.g4.game_tracker_backend.dominio.catalogo.JogoRepositorio;
import com.g4.game_tracker_backend.dominio.compartilhado.RecursoNaoEncontradoException;
import com.g4.game_tracker_backend.dominio.identidade.Usuario;
import com.g4.game_tracker_backend.dominio.identidade.UsuarioRepositorio;

@Service
public class ConsultarPerfilPublico {
    public record Favorito(int slot, UUID jogoId, String titulo, String capaUrl) {
    }

    public record PerfilPublico(UUID id, String nome, String bio, String localizacao,
            String avatarUrl, String bannerUrl, List<Favorito> favoritos) {
    }

    private final UsuarioRepositorio usuarios;
    private final PerfilFavoritosRepositorio favoritos;
    private final JogoRepositorio jogos;
    private final EstadoPublico moderacao;

    public ConsultarPerfilPublico(UsuarioRepositorio usuarios, PerfilFavoritosRepositorio favoritos, JogoRepositorio jogos,
            EstadoPublico moderacao) {
        this.usuarios = usuarios;
        this.favoritos = favoritos;
        this.jogos = jogos;
        this.moderacao = moderacao;
    }

    @Transactional(readOnly = true)
    public PerfilPublico consultar(UUID usuarioId) {
        var usuario = usuarios.buscarPorId(usuarioId).filter(Usuario::ativo)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Perfil não encontrado"));
        var lista = favoritos.buscar(usuarioId).jogos();
        List<Favorito> resultado = new ArrayList<>();
        for (int i = 0; i < lista.size(); i++) {
            UUID jogoId = lista.get(i);
            var jogo = jogos.buscarPorId(jogoId);
            resultado.add(new Favorito(i + 1, jogoId, jogo.map(j -> j.titulo()).orElse("Jogo indisponível"),
                    jogo.map(j -> j.capaUrl()).orElse(null)));
        }
        if (!moderacao.visivel(TipoConteudo.PERFIL, usuarioId)) {
            return new PerfilPublico(usuario.id(), "Perfil indisponível", null, null, null, null, List.copyOf(resultado));
        }
        return new PerfilPublico(usuario.id(), usuario.nome(), usuario.bio(), usuario.localizacao(),
                usuario.avatarUrl(), usuario.bannerUrl(), List.copyOf(resultado));
    }
}
