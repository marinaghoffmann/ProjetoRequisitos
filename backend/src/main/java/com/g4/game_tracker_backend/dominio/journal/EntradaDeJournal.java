package com.g4.game_tracker_backend.dominio.journal;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

import com.g4.game_tracker_backend.dominio.compartilhado.AcessoNegadoException;
import com.g4.game_tracker_backend.dominio.compartilhado.RegraDeNegocioException;

/**
 * Entrada do journal do jogador, manual (F-15) ou gerada pelo sistema a partir de um evento de
 * plataforma (F-16). É imutável: cada alteração devolve uma nova instância.
 *
 * Esta é a base compartilhada pelas duas funcionalidades. A F-15 acrescenta tags, anexos e o restante
 * do CRUD; toda edição de texto deve passar por editar(...), que aplica as regras abaixo.
 *
 * F-15 RN 1: só o dono altera ou exclui a entrada.
 * F-16 RN 3: entradas AUTOMATICO não têm o texto alterado pelo jogador.
 */
public final class EntradaDeJournal {

    public static final int LIMITE_DO_TEXTO = 5000;

    private static final String APENAS_O_DONO = "Apenas o dono pode alterar esta entrada";
    private static final String AUTOMATICA_NAO_EDITAVEL = "Entradas automáticas não podem ser editadas";

    private final UUID id;
    private final UUID donoId;
    private final UUID jogoId;
    private final Instant dataSessao;
    private final String texto;
    private final boolean contemSpoiler;
    private final TipoOrigem tipoOrigem;
    private final TipoEventoPlataforma tipoEvento;
    private final String origemId;
    private final Instant criadaEm;
    private final Instant atualizadaEm;

    private EntradaDeJournal(UUID id, UUID donoId, UUID jogoId, Instant dataSessao, String texto,
            boolean contemSpoiler, TipoOrigem tipoOrigem, TipoEventoPlataforma tipoEvento, String origemId,
            Instant criadaEm, Instant atualizadaEm) {
        this.id = Objects.requireNonNull(id, "O identificador da entrada é obrigatório");
        this.donoId = Objects.requireNonNull(donoId, "O dono da entrada é obrigatório");
        this.jogoId = jogoId;
        this.dataSessao = Objects.requireNonNull(dataSessao, "A data da sessão é obrigatória");
        this.texto = texto(texto);
        this.contemSpoiler = contemSpoiler;
        this.tipoOrigem = Objects.requireNonNull(tipoOrigem, "O tipo de origem é obrigatório");
        if (tipoOrigem == TipoOrigem.AUTOMATICO && (tipoEvento == null || origemId == null || origemId.isBlank())) {
            throw new IllegalArgumentException("Entrada automática precisa do evento e da ação de origem");
        }
        this.tipoEvento = tipoOrigem == TipoOrigem.AUTOMATICO ? tipoEvento : null;
        this.origemId = tipoOrigem == TipoOrigem.AUTOMATICO ? origemId : null;
        this.criadaEm = Objects.requireNonNull(criadaEm, "A data de criação é obrigatória");
        this.atualizadaEm = Objects.requireNonNull(atualizadaEm, "A data de atualização é obrigatória");
    }

    /** Entrada escrita pelo jogador (F-15). jogoId é opcional. */
    public static EntradaDeJournal criarManual(UUID donoId, UUID jogoId, Instant dataSessao, String texto,
            boolean contemSpoiler, Instant agora) {
        return new EntradaDeJournal(UUID.randomUUID(), donoId, jogoId, dataSessao, texto, contemSpoiler,
                TipoOrigem.MANUAL, null, null, agora, agora);
    }

    /**
     * Entrada gerada por um evento de plataforma (F-16). A data da sessão é a da ação de origem, e
     * origemId guarda o id da review, do registro de conclusão ou do pedido, para a sincronização (RN 2).
     */
    public static EntradaDeJournal criarAutomatica(UUID donoId, UUID jogoId, TipoEventoPlataforma evento,
            String origemId, String texto, Instant ocorridoEm, Instant agora) {
        return new EntradaDeJournal(UUID.randomUUID(), donoId, jogoId, ocorridoEm, texto, false,
                TipoOrigem.AUTOMATICO, evento, origemId, agora, agora);
    }

    public static EntradaDeJournal reconstituir(UUID id, UUID donoId, UUID jogoId, Instant dataSessao, String texto,
            boolean contemSpoiler, TipoOrigem tipoOrigem, TipoEventoPlataforma tipoEvento, String origemId,
            Instant criadaEm, Instant atualizadaEm) {
        return new EntradaDeJournal(id, donoId, jogoId, dataSessao, texto, contemSpoiler, tipoOrigem, tipoEvento,
                origemId, criadaEm, atualizadaEm);
    }

    /**
     * O dono altera o texto e a marcação de spoiler. Em entradas automáticas, o texto principal é do
     * sistema e não pode mudar (F-16 RN 3).
     */
    public EntradaDeJournal editar(UUID solicitanteId, String novoTexto, boolean novoContemSpoiler, Instant agora) {
        exigirDono(solicitanteId);
        if (tipoOrigem == TipoOrigem.AUTOMATICO) {
            throw new RegraDeNegocioException(AUTOMATICA_NAO_EDITAVEL);
        }
        return new EntradaDeJournal(id, donoId, jogoId, dataSessao, novoTexto, novoContemSpoiler, tipoOrigem,
                tipoEvento, origemId, criadaEm, agora);
    }

    public void exigirDono(UUID solicitanteId) {
        if (!donoId.equals(solicitanteId)) {
            throw new AcessoNegadoException(APENAS_O_DONO);
        }
    }

    public boolean automatica() {
        return tipoOrigem == TipoOrigem.AUTOMATICO;
    }

    private static String texto(String valor) {
        if (valor == null || valor.isBlank()) {
            throw new RegraDeNegocioException("O texto da entrada é obrigatório");
        }
        String normalizado = valor.strip();
        if (normalizado.length() > LIMITE_DO_TEXTO) {
            throw new RegraDeNegocioException("O texto da entrada deve ter no máximo " + LIMITE_DO_TEXTO
                    + " caracteres");
        }
        return normalizado;
    }

    public UUID id() { return id; }
    public UUID donoId() { return donoId; }
    public UUID jogoId() { return jogoId; }
    public Instant dataSessao() { return dataSessao; }
    public String texto() { return texto; }
    public boolean contemSpoiler() { return contemSpoiler; }
    public TipoOrigem tipoOrigem() { return tipoOrigem; }
    public TipoEventoPlataforma tipoEvento() { return tipoEvento; }
    public String origemId() { return origemId; }
    public Instant criadaEm() { return criadaEm; }
    public Instant atualizadaEm() { return atualizadaEm; }
}
