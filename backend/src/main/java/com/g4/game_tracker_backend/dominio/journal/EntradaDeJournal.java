package com.g4.game_tracker_backend.dominio.journal;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;

import com.g4.game_tracker_backend.dominio.compartilhado.AcessoNegadoException;
import com.g4.game_tracker_backend.dominio.compartilhado.RegraDeNegocioException;

/**
 * Entrada do journal do jogador, manual (F-15) ou gerada pelo sistema a partir de um evento de
 * plataforma (F-16). É imutável: cada alteração devolve uma nova instância.
 *
 * Base compartilhada pelas duas funcionalidades, com tags e anexos pertencentes à entrada.
 * Toda edição de texto deve passar por editar(...), que aplica as regras abaixo.
 *
 * F-15 RN 1: só o dono altera ou exclui a entrada.
 * F-16 RN 3: entradas AUTOMATICO não têm o texto alterado pelo jogador.
 */
public final class EntradaDeJournal {

    public static final int LIMITE_DO_TEXTO = 5000;
    public static final int LIMITE_DE_ANEXOS = 3;
    public static final int LIMITE_DE_TAGS = 6;

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
    private final String titulo;
    private final List<String> tags;
    private final List<AnexoJournal> anexos;

    private EntradaDeJournal(UUID id, UUID donoId, UUID jogoId, Instant dataSessao, String texto,
            boolean contemSpoiler, TipoOrigem tipoOrigem, TipoEventoPlataforma tipoEvento, String origemId,
            Instant criadaEm, Instant atualizadaEm) {
        this(id, donoId, jogoId, dataSessao, texto, contemSpoiler, tipoOrigem, tipoEvento, origemId,
                criadaEm, atualizadaEm, null, List.of(), List.of());
    }

    private EntradaDeJournal(UUID id, UUID donoId, UUID jogoId, Instant dataSessao, String texto,
            boolean contemSpoiler, TipoOrigem tipoOrigem, TipoEventoPlataforma tipoEvento, String origemId,
            Instant criadaEm, Instant atualizadaEm, String titulo, List<String> tags, List<AnexoJournal> anexos) {
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
        this.titulo = titulo == null || titulo.isBlank() ? null : titulo.strip();
        if (this.titulo != null && this.titulo.length() > 120) {
            throw new RegraDeNegocioException("O título deve ter no máximo 120 caracteres");
        }
        var normalizadas = new LinkedHashSet<String>();
        for (String tag : tags == null ? List.<String>of() : tags) {
            if (tag == null || tag.isBlank()) continue;
            String valor = tag.strip().replaceFirst("^#", "").strip().toLowerCase(Locale.ROOT);
            if (valor.isBlank()) continue;
            if (valor.length() > 40) throw new RegraDeNegocioException("Cada tag deve ter no máximo 40 caracteres");
            normalizadas.add(valor);
        }
        if (normalizadas.size() > LIMITE_DE_TAGS) throw new RegraDeNegocioException("Cada entrada aceita no máximo 6 tags");
        this.tags = List.copyOf(normalizadas);
        this.anexos = List.copyOf(anexos);
        if (this.anexos.size() > LIMITE_DE_ANEXOS) {
            throw new RegraDeNegocioException("Cada entrada aceita no máximo 3 imagens");
        }
    }

    /** Entrada escrita pelo jogador (F-15). jogoId é opcional. */
    public static EntradaDeJournal criarManual(UUID donoId, UUID jogoId, Instant dataSessao, String texto,
            boolean contemSpoiler, Instant agora) {
        return new EntradaDeJournal(UUID.randomUUID(), donoId, jogoId, dataSessao, texto, contemSpoiler,
                TipoOrigem.MANUAL, null, null, agora, agora);
    }

    public static EntradaDeJournal criarManual(UUID donoId, UUID jogoId, Instant dataSessao, String titulo,
            String texto, List<String> tags, boolean contemSpoiler, Instant agora) {
        return new EntradaDeJournal(UUID.randomUUID(), donoId, jogoId, dataSessao, texto, contemSpoiler,
                TipoOrigem.MANUAL, null, null, agora, agora, titulo, tags, List.of());
    }

    /**
     * Entrada gerada por um evento de plataforma (F-16). A data da sessão é a da ação de origem, e
     * origemId guarda o id da review, do registro de conclusão ou do pedido, para a sincronização (RN 2).
     */
    public static EntradaDeJournal criarAutomatica(UUID donoId, UUID jogoId, TipoEventoPlataforma evento,
            String origemId, String texto, Instant ocorridoEm, Instant agora) {
        return new EntradaDeJournal(UUID.randomUUID(), donoId, jogoId, ocorridoEm, texto, false,
                TipoOrigem.AUTOMATICO, evento, origemId, agora, agora, null,
                List.of(switch (evento) {
                    case REVIEW_PUBLICADA -> "review";
                    case JOGO_ZERADO -> "zerado";
                    case COMPRA_CONCLUIDA -> "compra";
                }), List.of());
    }

    public static EntradaDeJournal reconstituir(UUID id, UUID donoId, UUID jogoId, Instant dataSessao, String texto,
            boolean contemSpoiler, TipoOrigem tipoOrigem, TipoEventoPlataforma tipoEvento, String origemId,
            Instant criadaEm, Instant atualizadaEm) {
        return new EntradaDeJournal(id, donoId, jogoId, dataSessao, texto, contemSpoiler, tipoOrigem, tipoEvento,
                origemId, criadaEm, atualizadaEm);
    }

    public static EntradaDeJournal reconstituir(UUID id, UUID donoId, UUID jogoId, Instant dataSessao, String texto,
            boolean contemSpoiler, TipoOrigem tipoOrigem, TipoEventoPlataforma tipoEvento, String origemId,
            Instant criadaEm, Instant atualizadaEm, String titulo, List<String> tags, List<AnexoJournal> anexos) {
        return new EntradaDeJournal(id, donoId, jogoId, dataSessao, texto, contemSpoiler, tipoOrigem, tipoEvento,
                origemId, criadaEm, atualizadaEm, titulo, tags, anexos);
    }

    /**
     * O dono altera o texto e a marcação de spoiler. Em entradas automáticas, o texto principal é do
     * sistema e não pode mudar (F-16 RN 3).
     */
    public EntradaDeJournal editar(UUID solicitanteId, String novoTexto, boolean novoContemSpoiler, Instant agora) {
        return editar(solicitanteId, jogoId, dataSessao, titulo, novoTexto, tags, novoContemSpoiler, agora);
    }

    public EntradaDeJournal editar(UUID solicitanteId, UUID novoJogo, Instant novaData, String novoTitulo,
            String novoTexto, List<String> novasTags, boolean spoiler, Instant agora) {
        exigirEdicaoManual(solicitanteId);
        return new EntradaDeJournal(id, donoId, novoJogo, novaData, novoTexto, spoiler, tipoOrigem,
                tipoEvento, origemId, criadaEm, agora, novoTitulo, novasTags, anexos);
    }

    public EntradaDeJournal anexar(UUID solicitante, AnexoJournal anexo, Instant agora) {
        exigirEdicaoManual(solicitante);
        var novos = new ArrayList<>(anexos);
        novos.add(anexo);
        return comAnexos(novos, agora);
    }

    public EntradaDeJournal manterAnexos(UUID solicitante, Collection<UUID> ids, Instant agora) {
        exigirEdicaoManual(solicitante);
        if (ids == null || ids.stream().anyMatch(id -> anexos.stream().noneMatch(anexo -> anexo.id().equals(id)))) {
            throw new RegraDeNegocioException("O anexo não pertence a esta entrada");
        }
        return comAnexos(anexos.stream().filter(anexo -> ids.contains(anexo.id())).toList(), agora);
    }

    public void exigirEdicaoManual(UUID solicitante) {
        exigirDono(solicitante);
        if (automatica()) throw new RegraDeNegocioException(AUTOMATICA_NAO_EDITAVEL);
    }

    private EntradaDeJournal comAnexos(List<AnexoJournal> novos, Instant agora) {
        return new EntradaDeJournal(id, donoId, jogoId, dataSessao, texto, contemSpoiler, tipoOrigem,
                tipoEvento, origemId, criadaEm, agora, titulo, tags, novos);
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
    public String titulo() { return titulo; }
    public List<String> tags() { return tags; }
    public List<AnexoJournal> anexos() { return anexos; }
}
