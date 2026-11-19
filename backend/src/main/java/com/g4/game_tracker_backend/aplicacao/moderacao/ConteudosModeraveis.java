package com.g4.game_tracker_backend.aplicacao.moderacao;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.g4.game_tracker_backend.aplicacao.moderacao.ModeracaoOperacoes.Alvo;
import com.g4.game_tracker_backend.dominio.moderacao.TipoConteudo;

/**
 * Porta para o conteúdo dos outros contextos (fórum, reviews, listas, marketplace, perfis e journal).
 * A moderação lê o autor e o texto e, no fórum, faz as alterações moderativas.
 */
public interface ConteudosModeraveis {

    /** bloquear trava a linha até o fim da transação, para a decisão não concorrer com o autor. */
    Optional<Alvo> buscar(TipoConteudo tipo, UUID id, boolean bloquear);

    Optional<UUID> topicoDaResposta(UUID respostaId);

    /** Trava o tópico antes da resposta, na mesma ordem que o fórum usa. */
    void bloquearTopico(UUID topicoId);

    String statusDoTopico(UUID topicoId);

    void alterarStatusDoTopico(UUID topicoId, String status);

    void encerrarTopico(UUID topicoId, Instant agora);

    void editarTopico(UUID topicoId, String titulo, String conteudo, Instant agora);

    void editarResposta(UUID respostaId, String conteudo, Instant agora);

    void moverTopico(UUID topicoId, String categoria, Instant agora);

    /** Se a resposta existe, pertence ao tópico e não foi removida pelo autor. */
    boolean respostaAtivaNoTopico(UUID respostaId, UUID topicoId);

    /** O pai da resposta, ou vazio se ela responde direto ao tópico. */
    Optional<UUID> paiDaResposta(UUID respostaId);

    void definirPaiDaResposta(UUID respostaId, UUID novoPaiId, UUID topicoId, Instant agora);

    /** Recontagem que desconsidera respostas removidas pelo autor ou ocultas/removidas pela moderação. */
    void recalcularTotalDeRespostas(UUID topicoId, Instant agora);

    void recalcularTotalDeCurtidas(UUID topicoId);

    /** Arquivos enviados que pertencem ao conteúdo (imagens, avatar, banner, fotos do anúncio). */
    List<String> urlsDeMidia(TipoConteudo tipo, UUID id);

    boolean usuarioExiste(UUID usuarioId);
}
