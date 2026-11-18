package com.g4.game_tracker_backend.marketplace;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import org.junit.jupiter.api.Test;

import com.g4.game_tracker_backend.aplicacao.compartilhado.ExecutorDeTransacao;
import com.g4.game_tracker_backend.aplicacao.compartilhado.RotinaDePrazo;

/** Padrão Template Method: o esqueleto fixo das rotinas de prazo do marketplace. */
class RotinaDePrazoTest {

    private static final Instant AGORA = Instant.parse("2026-11-10T12:00:00Z");

    private final TransacoesRegistradas transacoes = new TransacoesRegistradas();

    @Test
    void busca_numa_transacao_de_leitura_e_trata_cada_item_na_sua_propria_transacao() {
        RotinaDeTeste rotina = new RotinaDeTeste(List.of("a", "b", "c"), List.of());

        assertEquals(3, rotina.executar(AGORA));
        assertEquals(List.of("leitura", "escrita", "escrita", "escrita"), transacoes.abertas);
        assertEquals(List.of("a", "b", "c"), rotina.tratados);
    }

    @Test
    void falha_em_um_item_nao_interrompe_os_demais_nem_entra_na_contagem() {
        RotinaDeTeste rotina = new RotinaDeTeste(List.of("a", "quebra", "c"), List.of("quebra"));

        assertEquals(2, rotina.executar(AGORA));
        assertEquals(List.of("a", "c"), rotina.tratados);
    }

    @Test
    void item_que_ja_nao_precisava_ser_tratado_nao_conta() {
        RotinaDeTeste rotina = new RotinaDeTeste(List.of("a", "ignorar"), List.of());

        assertEquals(1, rotina.executar(AGORA));
    }

    @Test
    void sem_itens_vencidos_nao_abre_transacao_de_escrita() {
        assertEquals(0, new RotinaDeTeste(List.of(), List.of()).executar(AGORA));
        assertEquals(List.of("leitura"), transacoes.abertas);
    }

    private final class RotinaDeTeste extends RotinaDePrazo<String> {

        private final List<String> vencidos;
        private final List<String> quebram;
        private final List<String> tratados = new ArrayList<>();

        RotinaDeTeste(List<String> vencidos, List<String> quebram) {
            super(transacoes);
            this.vencidos = vencidos;
            this.quebram = quebram;
        }

        @Override
        protected List<String> vencidos(Instant agora) {
            return vencidos;
        }

        @Override
        protected boolean tratar(String item, Instant agora) {
            if (quebram.contains(item)) {
                throw new IllegalStateException("falha simulada");
            }
            if (item.equals("ignorar")) {
                return false;
            }
            tratados.add(item);
            return true;
        }

        @Override
        protected String descrever(String item) {
            return item;
        }
    }

    /** Executa o bloco direto e registra o tipo de cada transação aberta. */
    private static final class TransacoesRegistradas implements ExecutorDeTransacao {

        private final List<String> abertas = new ArrayList<>();

        @Override
        public <T> T executar(Supplier<T> bloco) {
            abertas.add("escrita");
            return bloco.get();
        }

        @Override
        public void executarSemRetorno(Runnable bloco) {
            abertas.add("escrita");
            bloco.run();
        }

        @Override
        public <T> T consultar(Supplier<T> bloco) {
            abertas.add("leitura");
            return bloco.get();
        }

        @Override
        public void exigirTransacaoAtiva() {
        }
    }
}
