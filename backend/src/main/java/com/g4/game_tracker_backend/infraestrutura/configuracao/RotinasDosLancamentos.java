package com.g4.game_tracker_backend.infraestrutura.configuracao;
import java.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import com.g4.game_tracker_backend.aplicacao.lancamentos.ProcessarLancamentos;
/** Rotina recuperável: processa o que venceu desde a última execução. */
@Configuration
@EnableScheduling
@ConditionalOnProperty(name="app.lancamentos.rotinas.habilitadas",havingValue="true",matchIfMissing=true)
public class RotinasDosLancamentos {
    private static final Logger LOG=LoggerFactory.getLogger(RotinasDosLancamentos.class);
    private final ProcessarLancamentos processamento;
    public RotinasDosLancamentos(ProcessarLancamentos processamento) { this.processamento=processamento; }
    @Scheduled(initialDelayString="${app.lancamentos.rotinas.atraso-inicial:PT1M}",
            fixedDelayString="${app.lancamentos.rotinas.intervalo:PT1M}")
    public void executar() {
        Instant agora=Instant.now();
        int enviados=processamento.dispararAlertas(agora);
        int lancados=processamento.transicionar(agora);
        if (enviados+lancados>0) LOG.info("Lançamentos: {} alertas, {} transições",enviados,lancados);
    }
}
