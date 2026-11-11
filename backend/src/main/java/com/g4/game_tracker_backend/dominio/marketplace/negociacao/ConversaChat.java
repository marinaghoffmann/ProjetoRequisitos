package com.g4.game_tracker_backend.dominio.marketplace.negociacao;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.UnaryOperator;

import com.g4.game_tracker_backend.dominio.compartilhado.AcessoNegadoException;
import com.g4.game_tracker_backend.dominio.compartilhado.RecursoNaoEncontradoException;
import com.g4.game_tracker_backend.dominio.compartilhado.RegraDeNegocioException;

/**
 * Agregado da conversa de negociação (F-19), com as mensagens e as propostas. É imutável: cada
 * alteração devolve uma nova instância.
 *
 * RN 1: só começa a partir de anúncio ATIVO, e o dono do anúncio não negocia no próprio anúncio.
 * RN 3: só o comprador e o vendedor leem e enviam mensagens.
 * RN 5: anúncio vendido ou cancelado deixa a conversa somente leitura.
 * RN 6 a 8: só o comprador propõe e cancela; só o vendedor aceita ou recusa; uma pendente por vez.
 * RN 11: abrir a conversa marca como lidas as mensagens recebidas.
 *
 * A unicidade da conversa por anúncio e comprador (RN 2) e a recusa das propostas de outras conversas
 * (RN 9) envolvem mais de um agregado e ficam nos casos de uso.
 */
public final class ConversaChat {

    public static final String RESTRITA = "Conversa restrita aos participantes";
    public static final String SOMENTE_LEITURA = "Anúncio encerrado: a conversa é somente leitura";

    private final UUID id;
    private final UUID anuncioId;
    private final UUID compradorId;
    private final UUID vendedorId;
    private final Instant iniciadaEm;
    private final Instant ultimaAtividadeEm;
    private final List<MensagemChat> mensagens;
    private final List<PropostaNegociacao> propostas;

    private ConversaChat(UUID id, UUID anuncioId, UUID compradorId, UUID vendedorId, Instant iniciadaEm,
            Instant ultimaAtividadeEm, List<MensagemChat> mensagens, List<PropostaNegociacao> propostas) {
        this.id = Objects.requireNonNull(id, "O identificador da conversa é obrigatório");
        this.anuncioId = Objects.requireNonNull(anuncioId, "O anúncio é obrigatório");
        this.compradorId = Objects.requireNonNull(compradorId, "O comprador é obrigatório");
        this.vendedorId = Objects.requireNonNull(vendedorId, "O vendedor é obrigatório");
        this.iniciadaEm = Objects.requireNonNull(iniciadaEm, "A data de início é obrigatória");
        this.ultimaAtividadeEm = Objects.requireNonNull(ultimaAtividadeEm, "A data da última atividade é obrigatória");
        this.mensagens = List.copyOf(mensagens == null ? List.of() : mensagens);
        this.propostas = List.copyOf(propostas == null ? List.of() : propostas);
    }

    /** RN 1: o comprador abre a conversa a partir de um anúncio ATIVO de outra pessoa. */
    public static ConversaChat iniciar(AnuncioNegociavel anuncio, UUID compradorId, Instant agora) {
        if (anuncio.vendedorId().equals(compradorId)) {
            throw new RegraDeNegocioException("Você não pode negociar o próprio anúncio");
        }
        if (!anuncio.aceitaNegociacao()) {
            throw new RegraDeNegocioException("Só é possível negociar anúncios ativos");
        }
        return new ConversaChat(UUID.randomUUID(), anuncio.anuncioId(), compradorId, anuncio.vendedorId(), agora,
                agora, List.of(), List.of());
    }

    public static ConversaChat reconstituir(UUID id, UUID anuncioId, UUID compradorId, UUID vendedorId,
            Instant iniciadaEm, Instant ultimaAtividadeEm, List<MensagemChat> mensagens,
            List<PropostaNegociacao> propostas) {
        return new ConversaChat(id, anuncioId, compradorId, vendedorId, iniciadaEm, ultimaAtividadeEm, mensagens,
                propostas);
    }

    // ---- Participantes (RN 3) ----

    public boolean participante(UUID usuarioId) {
        return compradorId.equals(usuarioId) || vendedorId.equals(usuarioId);
    }

    public void exigirParticipante(UUID usuarioId) {
        if (!participante(usuarioId)) {
            throw new AcessoNegadoException(RESTRITA);
        }
    }

    public UUID outraParte(UUID usuarioId) {
        exigirParticipante(usuarioId);
        return compradorId.equals(usuarioId) ? vendedorId : compradorId;
    }

    // ---- Mensagens (RN 3, 4, 5 e 11) ----

    public ConversaChat enviarMensagem(UUID remetenteId, String texto, AnuncioNegociavel anuncio, Instant agora) {
        exigirParticipante(remetenteId);
        exigirAberta(anuncio);
        List<MensagemChat> novas = new ArrayList<>(mensagens);
        novas.add(MensagemChat.nova(remetenteId, texto, agora));
        return new ConversaChat(id, anuncioId, compradorId, vendedorId, iniciadaEm, agora, novas, propostas);
    }

    /** RN 11: marca como lidas as mensagens que o leitor recebeu. */
    public ConversaChat marcarComoLidas(UUID leitorId, Instant agora) {
        exigirParticipante(leitorId);
        if (naoLidasPor(leitorId) == 0) {
            return this;
        }
        List<MensagemChat> atualizadas = mensagens.stream()
                .map(mensagem -> mensagem.naoLidaPor(leitorId) ? mensagem.lida(agora) : mensagem)
                .toList();
        return new ConversaChat(id, anuncioId, compradorId, vendedorId, iniciadaEm, ultimaAtividadeEm, atualizadas,
                propostas);
    }

    public int naoLidasPor(UUID usuarioId) {
        return (int) mensagens.stream().filter(mensagem -> mensagem.naoLidaPor(usuarioId)).count();
    }

    // ---- Propostas (RN 6 a 10) ----

    /** RN 6 e 7. A mensagem que acompanha a proposta é opcional e entra no chat junto com ela. */
    public ConversaChat enviarProposta(UUID remetenteId, TipoProposta tipo, BigDecimal valor, String jogoOferecido,
            String mensagem, AnuncioNegociavel anuncio, Instant agora) {
        exigirParticipante(remetenteId);
        if (!compradorId.equals(remetenteId)) {
            throw new RegraDeNegocioException("Apenas o comprador envia propostas");
        }
        exigirAberta(anuncio);
        if (!anuncio.aceitaNegociacao()) {
            throw new RegraDeNegocioException("Só é possível negociar anúncios ativos");
        }
        if (tipo != null && !anuncio.aceitaProposta(tipo)) {
            throw new RegraDeNegocioException(tipo == TipoProposta.TROCA
                    ? "Este anúncio não aceita trocas"
                    : "Este anúncio só aceita propostas de troca");
        }
        if (propostaPendente().isPresent()) {
            throw new RegraDeNegocioException("Já existe uma proposta pendente nesta conversa");
        }
        List<PropostaNegociacao> novasPropostas = new ArrayList<>(propostas);
        novasPropostas.add(PropostaNegociacao.nova(tipo, valor, jogoOferecido, agora));
        List<MensagemChat> novasMensagens = new ArrayList<>(mensagens);
        if (mensagem != null && !mensagem.isBlank()) {
            novasMensagens.add(MensagemChat.nova(remetenteId, mensagem, agora));
        }
        return new ConversaChat(id, anuncioId, compradorId, vendedorId, iniciadaEm, agora, novasMensagens,
                novasPropostas);
    }

    /** RN 8 e 9: o vendedor aceita; reservar o anúncio e recusar as outras conversas fica no caso de uso. */
    public ConversaChat aceitarProposta(UUID usuarioId, UUID propostaId, AnuncioNegociavel anuncio, Instant agora) {
        exigirVendedorParaResponder(usuarioId);
        if (!anuncio.aceitaNegociacao()) {
            throw new RegraDeNegocioException("Só é possível negociar anúncios ativos");
        }
        return alterarProposta(propostaId, proposta -> proposta.aceitar(agora), agora);
    }

    public ConversaChat recusarProposta(UUID usuarioId, UUID propostaId, Instant agora) {
        exigirVendedorParaResponder(usuarioId);
        return alterarProposta(propostaId, proposta -> proposta.recusar(null, agora), agora);
    }

    public ConversaChat cancelarProposta(UUID usuarioId, UUID propostaId, Instant agora) {
        exigirParticipante(usuarioId);
        if (!compradorId.equals(usuarioId)) {
            throw new RegraDeNegocioException("Apenas o comprador pode cancelar a proposta");
        }
        return alterarProposta(propostaId, proposta -> proposta.cancelar(agora), agora);
    }

    /** RN 9: a pendente desta conversa perde a vez porque o item foi reservado para outro comprador. */
    public ConversaChat recusarPorReservaDeOutroComprador(Instant agora) {
        return propostaPendente()
                .map(pendente -> alterarProposta(pendente.id(),
                        proposta -> proposta.recusar(PropostaNegociacao.RESERVADO_PARA_OUTRO, agora), agora))
                .orElse(this);
    }

    /** RN 10: o vendedor libera a reserva antes do prazo. */
    public ConversaChat liberarReserva(UUID usuarioId, UUID propostaId, Instant agora) {
        exigirParticipante(usuarioId);
        if (!vendedorId.equals(usuarioId)) {
            throw new RegraDeNegocioException("Apenas o vendedor pode liberar a reserva");
        }
        return alterarProposta(propostaId, proposta -> proposta.expirar(PropostaNegociacao.RESERVA_LIBERADA), agora);
    }

    /** RN 10: rotina agendada; a aceita sem pedido há mais de 48 horas expira. */
    public ConversaChat expirarReservaVencida(Instant agora) {
        return propostas.stream()
                .filter(proposta -> proposta.reservaVencida(agora))
                .findFirst()
                .map(vencida -> alterarProposta(vencida.id(),
                        proposta -> proposta.expirar(PropostaNegociacao.PRAZO_ENCERRADO), agora))
                .orElse(this);
    }

    /** F-20: o pedido gerado a partir da proposta aceita encerra a contagem das 48 horas. */
    public ConversaChat vincularPedido(UUID propostaId, UUID pedidoId) {
        PropostaNegociacao proposta = proposta(propostaId);
        if (!proposta.reservaVigente()) {
            throw new RegraDeNegocioException("A proposta não está aceita ou o pedido já foi gerado");
        }
        return comProposta(proposta.vincularPedido(pedidoId), ultimaAtividadeEm);
    }

    // ---- Consultas ----

    public Optional<PropostaNegociacao> propostaPendente() {
        return propostas.stream().filter(PropostaNegociacao::pendente).findFirst();
    }

    public Optional<PropostaNegociacao> reservaVigente() {
        return propostas.stream().filter(PropostaNegociacao::reservaVigente).findFirst();
    }

    public Optional<PropostaNegociacao> ultimaProposta() {
        return propostas.stream().max(Comparator.comparing(PropostaNegociacao::enviadaEm));
    }

    public Optional<MensagemChat> ultimaMensagem() {
        return mensagens.stream().max(Comparator.comparing(MensagemChat::enviadaEm));
    }

    public PropostaNegociacao proposta(UUID propostaId) {
        return propostas.stream()
                .filter(proposta -> proposta.id().equals(propostaId))
                .findFirst()
                .orElseThrow(() -> new RecursoNaoEncontradoException("Proposta não encontrada"));
    }

    // ---- Apoio ----

    private void exigirAberta(AnuncioNegociavel anuncio) {
        if (anuncio.encerrado()) {
            throw new RegraDeNegocioException(SOMENTE_LEITURA);
        }
    }

    private void exigirVendedorParaResponder(UUID usuarioId) {
        exigirParticipante(usuarioId);
        if (!vendedorId.equals(usuarioId)) {
            throw new RegraDeNegocioException("Apenas o vendedor pode aceitar ou recusar a proposta");
        }
    }

    private ConversaChat alterarProposta(UUID propostaId, UnaryOperator<PropostaNegociacao> alteracao, Instant agora) {
        return comProposta(alteracao.apply(proposta(propostaId)), agora);
    }

    private ConversaChat comProposta(PropostaNegociacao alterada, Instant atividade) {
        List<PropostaNegociacao> novas = propostas.stream()
                .map(proposta -> proposta.id().equals(alterada.id()) ? alterada : proposta)
                .toList();
        return new ConversaChat(id, anuncioId, compradorId, vendedorId, iniciadaEm, atividade, mensagens, novas);
    }

    public UUID id() { return id; }
    public UUID anuncioId() { return anuncioId; }
    public UUID compradorId() { return compradorId; }
    public UUID vendedorId() { return vendedorId; }
    public Instant iniciadaEm() { return iniciadaEm; }
    public Instant ultimaAtividadeEm() { return ultimaAtividadeEm; }
    public List<MensagemChat> mensagens() { return mensagens; }
    public List<PropostaNegociacao> propostas() { return propostas; }
}
