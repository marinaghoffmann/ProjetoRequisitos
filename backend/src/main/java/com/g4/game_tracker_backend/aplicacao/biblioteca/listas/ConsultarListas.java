package com.g4.game_tracker_backend.aplicacao.biblioteca.listas;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.g4.game_tracker_backend.aplicacao.identidade.VerificadorDeAcesso;
import com.g4.game_tracker_backend.dominio.biblioteca.listas.ListaPersonalizada;
import com.g4.game_tracker_backend.dominio.biblioteca.listas.ListaRepositorio;
import com.g4.game_tracker_backend.dominio.catalogo.JogoRepositorio;
import com.g4.game_tracker_backend.dominio.compartilhado.RecursoNaoEncontradoException;
import com.g4.game_tracker_backend.dominio.identidade.Usuario;
import com.g4.game_tracker_backend.dominio.identidade.UsuarioRepositorio;
import com.g4.game_tracker_backend.aplicacao.moderacao.EstadoPublico;
import com.g4.game_tracker_backend.dominio.moderacao.TipoConteudo;

/**
 * Consultas da F-04: as listas do usuário autenticado, as listas públicas de um usuário e uma lista com os
 * itens. Cada lista traz o total de jogos; os dados dos jogos (título e capa) vêm do catálogo.
 */
@Service
public class ConsultarListas {

    /** Título e capa do jogo de um item, como o catálogo os informa. */
    public record JogoDaLista(UUID id, String titulo, String capaUrl, String status) {
    }

    private final ListaRepositorio listas;
    private final JogoRepositorio jogos;
    private final UsuarioRepositorio usuarios;
    private final VerificadorDeAcesso verificadorDeAcesso;
    private final EstadoPublico moderacao;

    public ConsultarListas(ListaRepositorio listas, JogoRepositorio jogos, UsuarioRepositorio usuarios,
            VerificadorDeAcesso verificadorDeAcesso, EstadoPublico moderacao) {
        this.listas = listas;
        this.jogos = jogos;
        this.usuarios = usuarios;
        this.verificadorDeAcesso = verificadorDeAcesso;
        this.moderacao = moderacao;
    }

    @Transactional(readOnly = true)
    public List<ListaPersonalizada> minhas() {
        return listas.listarPorDono(verificadorDeAcesso.usuarioAtualId());
    }

    @Transactional(readOnly = true)
    public List<ListaPersonalizada> doUsuario(UUID usuarioId) {
        if (usuarios.buscarPorId(usuarioId).filter(Usuario::ativo).isEmpty()) {
            throw new RecursoNaoEncontradoException("Usuário não encontrado");
        }
        return listas.listarPorDono(usuarioId);
    }

    @Transactional(readOnly = true)
    public ListaPersonalizada buscar(UUID listaId) {
        return ListasDoUsuario.existente(listas, listaId);
    }

    @Transactional(readOnly = true)
    public Map<UUID, JogoDaLista> jogosDosItens(Collection<ListaPersonalizada> consultadas) {
        Map<UUID, JogoDaLista> resultado = new HashMap<>();
        HashSet<UUID> ids = new HashSet<>();
        consultadas.forEach(lista -> lista.itens().forEach(item -> ids.add(item.jogoId())));
        for (UUID jogoId : ids) {
            jogos.buscarPorId(jogoId).ifPresent(jogo -> resultado.put(jogoId,
                    new JogoDaLista(jogo.id(), jogo.titulo(), jogo.capaUrl(), jogo.status().name())));
        }
        return resultado;
    }

    @Transactional(readOnly = true)
    public Map<UUID, String> nomesDosDonos(Collection<UUID> donoIds) {
        Map<UUID, String> nomes = new HashMap<>();
        for (UUID donoId : new HashSet<>(donoIds)) {
            usuarios.buscarPorId(donoId).ifPresent(usuario -> nomes.put(donoId,
                    moderacao.visivel(TipoConteudo.PERFIL, donoId) ? usuario.nome() : "Perfil indisponível"));
        }
        return nomes;
    }
}
