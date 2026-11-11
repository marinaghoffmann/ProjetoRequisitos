package com.g4.game_tracker_backend.infraestrutura.configuracao;

import java.time.Instant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import com.g4.game_tracker_backend.aplicacao.marketplace.negociacao.ExpirarReservasVencidas;
import com.g4.game_tracker_backend.aplicacao.marketplace.pedidos.ProcessarPrazosDosPedidos;

/**
 * Agenda as rotinas de prazo do marketplace: reservas de 48 horas (F-19 RN 10), envio em 3 dias úteis
 * (F-20 RN 9) e liberação da custódia 7 dias após o envio (F-20 RN 7). Rodam de hora em hora, a primeira um
 * minuto depois da subida. Os testes desligam com app.marketplace.rotinas.habilitadas=false e chamam os
 * casos de uso direto, com um instante fixo.
 */
@Configuration
@EnableScheduling
@ConditionalOnProperty(name = "app.marketplace.rotinas.habilitadas", havingValue = "true", matchIfMissing = true)
public class RotinasDoMarketplace {

    private static final Logger LOG = LoggerFactory.getLogger(RotinasDoMarketplace.class);

    private final ExpirarReservasVencidas expirarReservas;
    private final ProcessarPrazosDosPedidos prazosDosPedidos;

    public RotinasDoMarketplace(ExpirarReservasVencidas expirarReservas, ProcessarPrazosDosPedidos prazosDosPedidos) {
        this.expirarReservas = expirarReservas;
        this.prazosDosPedidos = prazosDosPedidos;
    }

    @Scheduled(initialDelayString = "${app.marketplace.rotinas.atraso-inicial:PT1M}",
            fixedDelayString = "${app.marketplace.rotinas.intervalo:PT1H}")
    public void executar() {
        Instant agora = Instant.now();
        int reservas = expirarReservas.executar(agora);
        int cancelados = prazosDosPedidos.cancelarPedidosSemEnvio(agora);
        int liberados = prazosDosPedidos.liberarCustodiaPorTolerancia(agora);
        if (reservas + cancelados + liberados > 0) {
            LOG.info("Rotinas do marketplace: {} reservas expiradas, {} pedidos cancelados, {} custódias liberadas",
                    reservas, cancelados, liberados);
        }
    }
}
