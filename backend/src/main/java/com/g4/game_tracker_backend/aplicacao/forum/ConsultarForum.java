package com.g4.game_tracker_backend.aplicacao.forum;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.g4.game_tracker_backend.dominio.compartilhado.Pagina;
import com.g4.game_tracker_backend.dominio.compartilhado.RegraDeNegocioException;
import com.g4.game_tracker_backend.dominio.forum.FiltroTopicos;
import com.g4.game_tracker_backend.dominio.forum.ResumoTopico;
import com.g4.game_tracker_backend.dominio.forum.TopicoDeForum;
import com.g4.game_tracker_backend.dominio.forum.TopicoDeForumRepositorio;
import com.g4.game_tracker_backend.dominio.identidade.UsuarioRepositorio;

/** Consultas públicas da F-13: tópicos paginados por jogo ou categoria e um tópico com as suas respostas. */
@Service
public class ConsultarForum {

    public static final int TAMANHO_MAXIMO_DA_PAGINA = 50;

    private final TopicoDeForumRepositorio topicos;
    private final UsuarioRepositorio usuarios;

    public ConsultarForum(TopicoDeForumRepositorio topicos, UsuarioRepositorio usuarios) {
        this.topicos = topicos;
        this.usuarios = usuarios;
    }

    @Transactional(readOnly = true)
    public Pagina<ResumoTopico> listar(FiltroTopicos filtro, int pagina, int tamanho) {
        if (pagina < 1) {
            throw new RegraDeNegocioException("A página deve ser maior ou igual a 1");
        }
        if (tamanho < 1 || tamanho > TAMANHO_MAXIMO_DA_PAGINA) {
            throw new RegraDeNegocioException(
                    "O tamanho da página deve estar entre 1 e " + TAMANHO_MAXIMO_DA_PAGINA);
        }
        return topicos.listar(filtro == null ? FiltroTopicos.todos() : filtro, pagina, tamanho);
    }

    @Transactional(readOnly = true)
    public TopicoDeForum buscarPorId(UUID id) {
        return TopicosDoForum.visivel(topicos, id);
    }

    /** Nome de exibição de cada autor, para as telas não mostrarem apenas identificadores. */
    @Transactional(readOnly = true)
    public Map<UUID, String> nomesDosAutores(Collection<UUID> autorIds) {
        Map<UUID, String> nomes = new HashMap<>();
        for (UUID autorId : new HashSet<>(autorIds)) {
            usuarios.buscarPorId(autorId).ifPresent(usuario -> nomes.put(autorId, usuario.nome()));
        }
        return nomes;
    }
}
