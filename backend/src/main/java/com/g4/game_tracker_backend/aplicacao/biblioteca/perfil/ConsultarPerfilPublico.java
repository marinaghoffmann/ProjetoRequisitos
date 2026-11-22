package com.g4.game_tracker_backend.aplicacao.biblioteca.perfil;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.List;
import java.util.UUID;

import com.g4.game_tracker_backend.dominio.biblioteca.acervo.BibliotecaRepositorio;
import com.g4.game_tracker_backend.dominio.biblioteca.acervo.CatalogoDeJogos;
import com.g4.game_tracker_backend.dominio.biblioteca.acervo.ItemBiblioteca;
import com.g4.game_tracker_backend.dominio.biblioteca.conclusoes.RegistroDeConclusaoRepositorio;
import com.g4.game_tracker_backend.dominio.biblioteca.conclusoes.ResumoJogoZerado;
import com.g4.game_tracker_backend.dominio.biblioteca.perfil.PerfilFavoritosRepositorio;
import com.g4.game_tracker_backend.aplicacao.moderacao.EstadoPublico;
import com.g4.game_tracker_backend.dominio.moderacao.TipoConteudo;
import com.g4.game_tracker_backend.dominio.catalogo.JogoRepositorio;
import com.g4.game_tracker_backend.dominio.compartilhado.RecursoNaoEncontradoException;
import com.g4.game_tracker_backend.dominio.identidade.Usuario;
import com.g4.game_tracker_backend.dominio.identidade.UsuarioRepositorio;
import com.g4.game_tracker_backend.aplicacao.compartilhado.CasoDeUso;
import com.g4.game_tracker_backend.aplicacao.compartilhado.Transacional;

@CasoDeUso
public class ConsultarPerfilPublico {
    public record Favorito(int slot, UUID jogoId, String titulo, String capaUrl) {
    }

    /** Jogo da biblioteca de outro usuário (só dados públicos). */
    public record JogoPublico(UUID jogoId, String titulo, String capaUrl) {
    }

    public record PerfilPublico(UUID id, String nome, String bio, String localizacao,
            String avatarUrl, String bannerUrl, List<Favorito> favoritos, int totalJogos, int totalZerados) {
    }

    private final UsuarioRepositorio usuarios;
    private final PerfilFavoritosRepositorio favoritos;
    private final JogoRepositorio jogos;
    private final EstadoPublico moderacao;
    private final BibliotecaRepositorio biblioteca;
    private final CatalogoDeJogos catalogo;
    private final RegistroDeConclusaoRepositorio conclusoes;

    public ConsultarPerfilPublico(UsuarioRepositorio usuarios, PerfilFavoritosRepositorio favoritos, JogoRepositorio jogos,
            EstadoPublico moderacao, BibliotecaRepositorio biblioteca, CatalogoDeJogos catalogo,
            RegistroDeConclusaoRepositorio conclusoes) {
        this.usuarios = usuarios;
        this.favoritos = favoritos;
        this.jogos = jogos;
        this.moderacao = moderacao;
        this.biblioteca = biblioteca;
        this.catalogo = catalogo;
        this.conclusoes = conclusoes;
    }

    @Transacional(somenteLeitura = true)
    public PerfilPublico consultar(UUID usuarioId) {
        var usuario = usuarioAtivo(usuarioId);
        var lista = favoritos.buscar(usuarioId).jogos();
        List<Favorito> resultado = new ArrayList<>();
        for (int i = 0; i < lista.size(); i++) {
            UUID jogoId = lista.get(i);
            var jogo = jogos.buscarPorId(jogoId);
            resultado.add(new Favorito(i + 1, jogoId, jogo.map(j -> j.titulo()).orElse("Jogo indisponível"),
                    jogo.map(j -> j.capaUrl()).orElse(null)));
        }
        if (!moderacao.visivel(TipoConteudo.PERFIL, usuarioId)) {
            return new PerfilPublico(usuario.id(), "Perfil indisponível", null, null, null, null, List.copyOf(resultado), 0, 0);
        }
        return new PerfilPublico(usuario.id(), usuario.nome(), usuario.bio(), usuario.localizacao(),
                usuario.avatarUrl(), usuario.bannerUrl(), List.copyOf(resultado),
                biblioteca.listarPorUsuario(usuarioId).size(), conclusoes.jogosZerados(usuarioId).size());
    }

    /** Biblioteca pública: só jogos que ainda existem no catálogo. Perfil oculto por moderação não mostra nada. */
    @Transacional(somenteLeitura = true)
    public List<JogoPublico> biblioteca(UUID usuarioId) {
        usuarioAtivo(usuarioId);
        if (!moderacao.visivel(TipoConteudo.PERFIL, usuarioId)) return List.of();
        List<ItemBiblioteca> itens = biblioteca.listarPorUsuario(usuarioId);
        Map<UUID, CatalogoDeJogos.JogoResumo> resumos = catalogo
                .buscarResumos(itens.stream().map(ItemBiblioteca::jogoId).toList())
                .stream().collect(Collectors.toMap(CatalogoDeJogos.JogoResumo::id, Function.identity()));
        return itens.stream().filter(i -> resumos.containsKey(i.jogoId()))
                .map(i -> new JogoPublico(i.jogoId(), resumos.get(i.jogoId()).titulo(), resumos.get(i.jogoId()).capaUrl()))
                .toList();
    }

    /** Jogos zerados do usuário, com a contagem de conclusões e a última data. */
    @Transacional(somenteLeitura = true)
    public List<ResumoJogoZerado> zerados(UUID usuarioId) {
        usuarioAtivo(usuarioId);
        if (!moderacao.visivel(TipoConteudo.PERFIL, usuarioId)) return List.of();
        return conclusoes.jogosZerados(usuarioId);
    }

    private Usuario usuarioAtivo(UUID usuarioId) {
        return usuarios.buscarPorId(usuarioId).filter(Usuario::ativo)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Perfil não encontrado"));
    }
}
