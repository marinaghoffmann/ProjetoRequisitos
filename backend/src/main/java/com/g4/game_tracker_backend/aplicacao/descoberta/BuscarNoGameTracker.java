package com.g4.game_tracker_backend.aplicacao.descoberta;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.g4.game_tracker_backend.aplicacao.compartilhado.CasoDeUso;
import com.g4.game_tracker_backend.aplicacao.compartilhado.Transacional;
import com.g4.game_tracker_backend.aplicacao.moderacao.EstadoPublico;
import com.g4.game_tracker_backend.dominio.biblioteca.listas.ListaPersonalizada;
import com.g4.game_tracker_backend.dominio.biblioteca.listas.ListaRepositorio;
import com.g4.game_tracker_backend.dominio.catalogo.FiltroCatalogo;
import com.g4.game_tracker_backend.dominio.catalogo.Jogo;
import com.g4.game_tracker_backend.dominio.catalogo.JogoRepositorio;
import com.g4.game_tracker_backend.dominio.catalogo.StatusCatalogo;
import com.g4.game_tracker_backend.dominio.identidade.Usuario;
import com.g4.game_tracker_backend.dominio.identidade.UsuarioRepositorio;
import com.g4.game_tracker_backend.dominio.moderacao.TipoConteudo;

/**
 * Busca global da barra superior: jogos ativos do catálogo (F-01) e listas personalizadas (F-04), que já são
 * públicas. Só entra o que qualquer visitante já poderia ver:
 * <ul>
 * <li>listas ocultas ou removidas pela moderação (F-14) ficam de fora;</li>
 * <li>o dono de perfil oculto aparece como "Perfil indisponível";</li>
 * <li>não há busca de pessoas, porque o sistema não tem consulta pública de usuários.</li>
 * </ul>
 */
@CasoDeUso
public class BuscarNoGameTracker {

    public static final int TAMANHO_MINIMO_DO_TERMO = 2;
    static final int LIMITE_DE_JOGOS = 12;
    static final int LIMITE_DE_LISTAS = 8;
    static final String PERFIL_INDISPONIVEL = "Perfil indisponível";

    public record ListaEncontrada(ListaPersonalizada lista, String nomeDoDono) {
    }

    public record Resultado(String termo, List<Jogo> jogos, List<ListaEncontrada> listas) {
        public static Resultado vazio(String termo) {
            return new Resultado(termo, List.of(), List.of());
        }
    }

    private final JogoRepositorio jogos;
    private final ListaRepositorio listas;
    private final UsuarioRepositorio usuarios;
    private final EstadoPublico moderacao;

    public BuscarNoGameTracker(JogoRepositorio jogos, ListaRepositorio listas, UsuarioRepositorio usuarios,
            EstadoPublico moderacao) {
        this.jogos = jogos;
        this.listas = listas;
        this.usuarios = usuarios;
        this.moderacao = moderacao;
    }

    @Transacional(somenteLeitura = true)
    public Resultado executar(String termo) {
        String limpo = termo == null ? "" : termo.strip();
        if (limpo.length() < TAMANHO_MINIMO_DO_TERMO) {
            return Resultado.vazio(limpo);
        }
        List<Jogo> jogosEncontrados = jogos.consultar(new FiltroCatalogo(limpo, null, null, null, StatusCatalogo.ATIVO))
                .stream().limit(LIMITE_DE_JOGOS).toList();
        List<ListaPersonalizada> listasVisiveis = listas.buscarPorTitulo(limpo, LIMITE_DE_LISTAS * 2).stream()
                .filter(lista -> moderacao.visivel(TipoConteudo.LISTA, lista.id()))
                .limit(LIMITE_DE_LISTAS)
                .toList();
        Map<UUID, Usuario> donos = donos(listasVisiveis.stream().map(ListaPersonalizada::donoId)
                .collect(Collectors.toSet()));
        return new Resultado(limpo, jogosEncontrados, listasVisiveis.stream()
                .map(lista -> new ListaEncontrada(lista, nomeDoDono(donos.get(lista.donoId()))))
                .toList());
    }

    private Map<UUID, Usuario> donos(Set<UUID> ids) {
        return ids.stream().flatMap(id -> usuarios.buscarPorId(id).stream())
                .collect(Collectors.toMap(Usuario::id, Function.identity()));
    }

    private String nomeDoDono(Usuario dono) {
        if (dono == null) {
            return null;
        }
        return moderacao.visivel(TipoConteudo.PERFIL, dono.id()) ? dono.nome() : PERFIL_INDISPONIVEL;
    }
}
