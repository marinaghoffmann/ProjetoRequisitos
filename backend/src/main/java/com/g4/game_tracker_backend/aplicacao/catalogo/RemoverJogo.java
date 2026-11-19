package com.g4.game_tracker_backend.aplicacao.catalogo;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.g4.game_tracker_backend.aplicacao.identidade.VerificadorDeAcesso;
import com.g4.game_tracker_backend.dominio.catalogo.Jogo;
import com.g4.game_tracker_backend.dominio.catalogo.JogoDesativado;
import com.g4.game_tracker_backend.dominio.catalogo.JogoRepositorio;
import com.g4.game_tracker_backend.dominio.catalogo.VinculosDoJogo;
import com.g4.game_tracker_backend.dominio.compartilhado.PublicadorDeEventos;
import com.g4.game_tracker_backend.dominio.compartilhado.RecursoNaoEncontradoException;
import com.g4.game_tracker_backend.dominio.identidade.Papel;

/**
 * F-01 RN 3: o administrador remove o jogo do catálogo.
 * <ul>
 * <li>Sem vínculos em outros contextos, a exclusão é <b>definitiva</b>.</li>
 * <li>Com vínculos, a exclusão é <b>lógica</b> (status INATIVO), preservando reviews, journal, listas etc., e
 * publica {@link JogoDesativado} na mesma transação. O catálogo não conhece quem reage (a F-12 cancela os
 * alertas do lançamento).</li>
 * </ul>
 */
@Service
public class RemoverJogo {

    /** O que aconteceu com o jogo: {@code definitiva} indica que ele não existe mais no banco. */
    public record Resultado(Jogo jogo, boolean definitiva) {
    }

    private final JogoRepositorio jogos;
    private final VinculosDoJogo vinculos;
    private final VerificadorDeAcesso verificadorDeAcesso;
    private final PublicadorDeEventos eventos;

    public RemoverJogo(JogoRepositorio jogos, VinculosDoJogo vinculos, VerificadorDeAcesso verificadorDeAcesso,
            PublicadorDeEventos eventos) {
        this.jogos = jogos;
        this.vinculos = vinculos;
        this.verificadorDeAcesso = verificadorDeAcesso;
        this.eventos = eventos;
    }

    @Transactional
    public Resultado executar(UUID id) {
        verificadorDeAcesso.exigirPapel(Papel.ADMINISTRADOR);
        Jogo jogo = jogos.buscarPorId(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Jogo não encontrado"));
        if (!vinculos.possuiVinculos(id)) {
            jogos.excluir(id);
            return new Resultado(jogo, true);
        }
        Jogo desativado = jogos.salvar(jogo.desativar());
        eventos.publicar(new JogoDesativado(desativado.id()));
        return new Resultado(desativado, false);
    }
}
