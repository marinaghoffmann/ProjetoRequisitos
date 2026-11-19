package com.g4.game_tracker_backend.infraestrutura.configuracao;

import java.time.Clock;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.g4.game_tracker_backend.aplicacao.compartilhado.ExecutorDeTransacao;
import com.g4.game_tracker_backend.aplicacao.identidade.VerificadorDeAcesso;
import com.g4.game_tracker_backend.aplicacao.journal.ConsultarJournal;
import com.g4.game_tracker_backend.aplicacao.journal.GerenciarJournal;
import com.g4.game_tracker_backend.aplicacao.moderacao.EstadoPublico;
import com.g4.game_tracker_backend.dominio.catalogo.JogoRepositorio;
import com.g4.game_tracker_backend.dominio.identidade.UsuarioRepositorio;
import com.g4.game_tracker_backend.dominio.journal.AnexoJournalRepositorio;
import com.g4.game_tracker_backend.dominio.journal.EntradaDeJournalRepositorio;

@Configuration
public class JournalConfig {
    @Bean
    GerenciarJournal gerenciarJournal(EntradaDeJournalRepositorio entradas, AnexoJournalRepositorio anexos,
            JogoRepositorio jogos, VerificadorDeAcesso acesso, ExecutorDeTransacao transacao,
            EstadoPublico moderacao) {
        return new GerenciarJournal(entradas, anexos, jogos, acesso, transacao, Clock.systemUTC(), moderacao);
    }

    @Bean
    ConsultarJournal consultarJournal(EntradaDeJournalRepositorio entradas, AnexoJournalRepositorio anexos,
            JogoRepositorio jogos, UsuarioRepositorio usuarios, VerificadorDeAcesso acesso,
            ExecutorDeTransacao transacao, EstadoPublico moderacao) {
        return new ConsultarJournal(entradas, anexos, jogos, usuarios, acesso, transacao, moderacao);
    }
}
