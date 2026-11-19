package com.g4.game_tracker_backend.aplicacao.avaliacao.reviews;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import com.g4.game_tracker_backend.aplicacao.identidade.VerificadorDeAcesso;
import com.g4.game_tracker_backend.dominio.avaliacao.reviews.ResumoReviewsJogo;
import com.g4.game_tracker_backend.dominio.avaliacao.reviews.Review;
import com.g4.game_tracker_backend.dominio.avaliacao.reviews.ReviewRepositorio;
import com.g4.game_tracker_backend.dominio.catalogo.JogoRepositorio;
import com.g4.game_tracker_backend.dominio.compartilhado.RegraDeNegocioException;
import com.g4.game_tracker_backend.dominio.identidade.UsuarioRepositorio;
import com.g4.game_tracker_backend.aplicacao.moderacao.EstadoPublico;
import com.g4.game_tracker_backend.dominio.moderacao.TipoConteudo;
import com.g4.game_tracker_backend.aplicacao.compartilhado.CasoDeUso;
import com.g4.game_tracker_backend.aplicacao.compartilhado.Transacional;

/**
 * Consultas da F-02: as reviews de um jogo, paginadas e com as médias geral e por categoria de todas
 * elas (pública), e as reviews do usuário autenticado.
 */
@CasoDeUso
public class ConsultarReviews {

    public static final int TAMANHO_MAXIMO_DA_PAGINA = 50;

    /** Título e capa do jogo avaliado, para as telas que listam reviews de vários jogos. */
    public record JogoAvaliado(UUID id, String titulo, String capaUrl) {
    }

    private final ReviewRepositorio reviews;
    private final JogoRepositorio jogos;
    private final UsuarioRepositorio usuarios;
    private final VerificadorDeAcesso verificadorDeAcesso;
    private final EstadoPublico moderacao;

    public ConsultarReviews(ReviewRepositorio reviews, JogoRepositorio jogos, UsuarioRepositorio usuarios,
            VerificadorDeAcesso verificadorDeAcesso, EstadoPublico moderacao) {
        this.reviews = reviews;
        this.jogos = jogos;
        this.usuarios = usuarios;
        this.verificadorDeAcesso = verificadorDeAcesso;
        this.moderacao = moderacao;
    }

    @Transacional(somenteLeitura = true)
    public ResumoReviewsJogo porJogo(UUID jogoId, int pagina, int tamanho) {
        if (pagina < 1) {
            throw new RegraDeNegocioException("A página deve ser maior ou igual a 1");
        }
        if (tamanho < 1 || tamanho > TAMANHO_MAXIMO_DA_PAGINA) {
            throw new RegraDeNegocioException("O tamanho da página deve estar entre 1 e " + TAMANHO_MAXIMO_DA_PAGINA);
        }
        JogosAvaliaveis.exigirExistente(jogos, jogoId);
        return ResumoReviewsJogo.de(jogoId, reviews.resumirNotas(jogoId), reviews.listarPorJogo(jogoId, pagina,
                tamanho));
    }

    @Transacional(somenteLeitura = true)
    public List<Review> doUsuarioAtual() {
        return reviews.listarPorAutor(verificadorDeAcesso.usuarioAtualId());
    }

    /** A review do usuário autenticado para o jogo, se houver: a tela abre o modal já em modo de edição. */
    @Transacional(somenteLeitura = true)
    public Optional<Review> doUsuarioAtualParaOJogo(UUID jogoId) {
        return reviews.buscarPorAutorEJogo(verificadorDeAcesso.usuarioAtualId(), jogoId);
    }

    /** Nome de exibição de cada autor, para as telas não mostrarem apenas identificadores. */
    @Transacional(somenteLeitura = true)
    public Map<UUID, String> nomesDosAutores(Collection<UUID> autorIds) {
        Map<UUID, String> nomes = new HashMap<>();
        for (UUID autorId : new HashSet<>(autorIds)) {
            usuarios.buscarPorId(autorId).ifPresent(usuario -> nomes.put(autorId,
                    moderacao.visivel(TipoConteudo.PERFIL, autorId) ? usuario.nome() : "Perfil indisponível"));
        }
        return nomes;
    }

    @Transacional(somenteLeitura = true)
    public Map<UUID, JogoAvaliado> jogosAvaliados(Collection<UUID> jogoIds) {
        Map<UUID, JogoAvaliado> resultado = new HashMap<>();
        for (UUID jogoId : new HashSet<>(jogoIds)) {
            jogos.buscarPorId(jogoId).ifPresent(jogo -> resultado.put(jogoId,
                    new JogoAvaliado(jogo.id(), jogo.titulo(), jogo.capaUrl())));
        }
        return resultado;
    }
}
