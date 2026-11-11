package com.g4.game_tracker_backend.aplicacao.journal;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.g4.game_tracker_backend.aplicacao.identidade.VerificadorDeAcesso;
import com.g4.game_tracker_backend.dominio.catalogo.JogoRepositorio;
import com.g4.game_tracker_backend.dominio.compartilhado.Pagina;
import com.g4.game_tracker_backend.dominio.compartilhado.RegraDeNegocioException;
import com.g4.game_tracker_backend.dominio.journal.EntradaDeJournal;
import com.g4.game_tracker_backend.dominio.journal.EntradaDeJournalRepositorio;

/** Consulta da F-16: a timeline unificada do jogador autenticado, com entradas manuais e automáticas. */
@Service
public class ConsultarTimeline {

    public static final int TAMANHO_MAXIMO_DA_PAGINA = 50;

    private final EntradaDeJournalRepositorio entradas;
    private final JogoRepositorio jogos;
    private final VerificadorDeAcesso verificadorDeAcesso;

    public ConsultarTimeline(EntradaDeJournalRepositorio entradas, JogoRepositorio jogos,
            VerificadorDeAcesso verificadorDeAcesso) {
        this.entradas = entradas;
        this.jogos = jogos;
        this.verificadorDeAcesso = verificadorDeAcesso;
    }

    /** Da sessão mais recente para a mais antiga. A página começa em 1. */
    @Transactional(readOnly = true)
    public Pagina<EntradaDeJournal> consultar(int pagina, int tamanho) {
        if (pagina < 1) {
            throw new RegraDeNegocioException("A página deve ser maior ou igual a 1");
        }
        if (tamanho < 1 || tamanho > TAMANHO_MAXIMO_DA_PAGINA) {
            throw new RegraDeNegocioException(
                    "O tamanho da página deve estar entre 1 e " + TAMANHO_MAXIMO_DA_PAGINA);
        }
        return entradas.listarTimeline(verificadorDeAcesso.usuarioAtualId(), pagina, tamanho);
    }

    /** Título de cada jogo citado, para a tela não mostrar apenas identificadores. */
    @Transactional(readOnly = true)
    public Map<UUID, String> titulosDosJogos(Collection<UUID> jogoIds) {
        Map<UUID, String> titulos = new HashMap<>();
        for (UUID jogoId : new HashSet<>(jogoIds)) {
            if (jogoId != null) {
                jogos.buscarPorId(jogoId).ifPresent(jogo -> titulos.put(jogoId, jogo.titulo()));
            }
        }
        return titulos;
    }
}
