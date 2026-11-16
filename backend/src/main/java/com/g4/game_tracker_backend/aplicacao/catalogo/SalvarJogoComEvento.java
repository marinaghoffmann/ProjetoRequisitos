package com.g4.game_tracker_backend.aplicacao.catalogo;
import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.g4.game_tracker_backend.aplicacao.identidade.VerificadorDeAcesso;
import com.g4.game_tracker_backend.dominio.catalogo.DataJogoAlterada;
import com.g4.game_tracker_backend.dominio.catalogo.Jogo;
import com.g4.game_tracker_backend.dominio.catalogo.JogoRepositorio;
import com.g4.game_tracker_backend.dominio.compartilhado.RecursoNaoEncontradoException;

/** Ponto único de gravação para alterações da data de lançamento de jogos existentes. */
@Service
public class SalvarJogoComEvento {
    private final JogoRepositorio jogos;
    private final ApplicationEventPublisher eventos;
    private final VerificadorDeAcesso acesso;
    public SalvarJogoComEvento(JogoRepositorio jogos, ApplicationEventPublisher eventos, VerificadorDeAcesso acesso) {
        this.jogos = jogos; this.eventos = eventos; this.acesso = acesso;
    }
    @Transactional
    public Jogo salvar(Jogo anterior, Jogo atualizado) {
        Jogo salvo = jogos.salvar(atualizado);
        if (!Objects.equals(anterior.dataLancamento(), salvo.dataLancamento()))
            eventos.publishEvent(new DataJogoAlterada(salvo.id(), anterior.dataLancamento(),
                    salvo.dataLancamento(), acesso.usuarioAtualId()));
        return salvo;
    }
    @Transactional
    public Jogo atualizarData(UUID jogoId, LocalDate novaData) {
        Jogo anterior = jogos.buscarPorId(jogoId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Jogo não encontrado"));
        if (Objects.equals(anterior.dataLancamento(), novaData)) return anterior;
        return salvar(anterior, anterior.comDataLancamento(novaData));
    }
}
