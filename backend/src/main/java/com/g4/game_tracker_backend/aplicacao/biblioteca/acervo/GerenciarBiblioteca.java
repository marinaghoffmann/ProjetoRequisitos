package com.g4.game_tracker_backend.aplicacao.biblioteca.acervo;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.g4.game_tracker_backend.aplicacao.compartilhado.ExecutorDeTransacao;
import com.g4.game_tracker_backend.aplicacao.identidade.ConsultarUsuarioAtual;
import com.g4.game_tracker_backend.dominio.biblioteca.perfil.PerfilFavoritosRepositorio;
import com.g4.game_tracker_backend.dominio.biblioteca.acervo.BibliotecaRepositorio;
import com.g4.game_tracker_backend.dominio.biblioteca.acervo.CatalogoDeJogos;
import com.g4.game_tracker_backend.dominio.biblioteca.acervo.CatalogoDeJogos.JogoResumo;
import com.g4.game_tracker_backend.dominio.biblioteca.acervo.ItemBiblioteca;
import com.g4.game_tracker_backend.dominio.compartilhado.RegraDeNegocioException;

/**
 * Casos de uso da biblioteca pessoal: listar, adicionar e remover jogos que o
 * usuário já tem.
 */
public class GerenciarBiblioteca {

    public record Item(UUID jogoId, String titulo, String capaUrl, Instant adicionadoEm) {
    }

    private final BibliotecaRepositorio repositorio;
    private final CatalogoDeJogos catalogo;
    private final ConsultarUsuarioAtual usuarioAtual;
    private final PerfilFavoritosRepositorio favoritos;
    private final ExecutorDeTransacao transacao;

    public GerenciarBiblioteca(BibliotecaRepositorio repositorio, CatalogoDeJogos catalogo,
            ConsultarUsuarioAtual usuarioAtual, PerfilFavoritosRepositorio favoritos, ExecutorDeTransacao transacao) {
        this.repositorio = repositorio;
        this.catalogo = catalogo;
        this.usuarioAtual = usuarioAtual;
        this.favoritos = favoritos;
        this.transacao = transacao;
    }

    public List<Item> listar() {
        UUID usuarioId = usuarioAtual.executar().id();
        List<ItemBiblioteca> itens = repositorio.listarPorUsuario(usuarioId);
        Map<UUID, JogoResumo> jogos = catalogo.buscarResumos(itens.stream().map(ItemBiblioteca::jogoId).toList())
                .stream().collect(Collectors.toMap(JogoResumo::id, Function.identity()));
        return itens.stream().filter(i -> jogos.containsKey(i.jogoId()))
                .map(i -> paraItem(i, jogos.get(i.jogoId()))).toList();
    }

    public Item adicionar(UUID jogoId) {
        UUID usuarioId = usuarioAtual.executar().id();
        JogoResumo jogo = catalogo.buscarResumo(jogoId)
                .orElseThrow(() -> new RegraDeNegocioException("O jogo não está cadastrado no catálogo"));
        if (repositorio.existe(usuarioId, jogoId)) {
            throw new RegraDeNegocioException("Este jogo já está na sua biblioteca");
        }
        return paraItem(repositorio.salvar(ItemBiblioteca.adicionar(usuarioId, jogoId)), jogo);
    }

    /** Remove da biblioteca e, na mesma transação, tira o jogo dos favoritos do usuário. */
    public void remover(UUID jogoId) {
        UUID usuarioId = usuarioAtual.executar().id();
        transacao.executarSemRetorno(() -> {
            repositorio.remover(usuarioId, jogoId);
            favoritos.salvar(favoritos.buscarParaAlterar(usuarioId).remover(jogoId));
        });
    }

    private static Item paraItem(ItemBiblioteca item, JogoResumo jogo) {
        return new Item(jogo.id(), jogo.titulo(), jogo.capaUrl(), item.adicionadoEm());
    }
}
