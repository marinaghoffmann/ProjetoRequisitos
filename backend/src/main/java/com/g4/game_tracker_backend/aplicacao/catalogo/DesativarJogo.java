package com.g4.game_tracker_backend.aplicacao.catalogo;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.g4.game_tracker_backend.aplicacao.identidade.VerificadorDeAcesso;
import com.g4.game_tracker_backend.dominio.catalogo.Jogo;
import com.g4.game_tracker_backend.dominio.catalogo.JogoDesativado;
import com.g4.game_tracker_backend.dominio.catalogo.JogoRepositorio;
import com.g4.game_tracker_backend.dominio.compartilhado.PublicadorDeEventos;
import com.g4.game_tracker_backend.dominio.compartilhado.RecursoNaoEncontradoException;
import com.g4.game_tracker_backend.dominio.identidade.Papel;

/**
 * F-01 RN 3: a remoção feita pelo administrador é lógica, preservando os vínculos do jogo. Publica
 * {@link JogoDesativado} na mesma transação; o catálogo não conhece quem reage (a F-12 cancela os alertas).
 */
@Service
public class DesativarJogo {

    private final JogoRepositorio jogos;
    private final VerificadorDeAcesso verificadorDeAcesso;
    private final PublicadorDeEventos eventos;

    public DesativarJogo(JogoRepositorio jogos, VerificadorDeAcesso verificadorDeAcesso,
            PublicadorDeEventos eventos) {
        this.jogos = jogos;
        this.verificadorDeAcesso = verificadorDeAcesso;
        this.eventos = eventos;
    }

    @Transactional
    public Jogo executar(UUID id) {
        verificadorDeAcesso.exigirPapel(Papel.ADMINISTRADOR);
        Jogo jogo = jogos.buscarPorId(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Jogo não encontrado"));
        Jogo desativado = jogos.salvar(jogo.desativar());
        eventos.publicar(new JogoDesativado(desativado.id()));
        return desativado;
    }
}
