package com.g4.game_tracker_backend.apresentacao.forum;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.g4.game_tracker_backend.dominio.forum.RespostaForum;
import com.g4.game_tracker_backend.dominio.forum.TopicoDeForum;

/**
 * Tópico com as respostas em ordem de publicação. O encadeamento vem em respostaPaiId; a tela monta
 * a árvore. Respostas removidas continuam na lista (sem conteúdo) para as respostas filhas não sumirem.
 * editavelAte indica até quando o autor ainda pode editar (RN 2: 48 horas).
 */
public record TopicoResponse(UUID id, UUID autorId, String nomeAutor, UUID jogoId, String categoria, String titulo,
        String conteudo, String status, int totalRespostas, Instant publicadoEm, Instant editadoEm,
        Instant editavelAte, Instant dataUltimaAtividade, List<Resposta> respostas) {

    public record Resposta(UUID id, UUID autorId, String nomeAutor, UUID respostaPaiId, String conteudo,
            boolean removida, Instant publicadaEm, Instant editadaEm, Instant editavelAte) {

        static Resposta de(RespostaForum resposta, Map<UUID, String> nomes) {
            return new Resposta(resposta.id(), resposta.autorId(), nomes.get(resposta.autorId()),
                    resposta.respostaPaiId(), resposta.removida() ? null : resposta.conteudo(), resposta.removida(),
                    resposta.publicadaEm(), resposta.editadaEm(), TopicoDeForum.editavelAte(resposta.publicadaEm()));
        }
    }

    static TopicoResponse de(TopicoDeForum topico, Map<UUID, String> nomes) {
        return new TopicoResponse(topico.id(), topico.autorId(), nomes.get(topico.autorId()), topico.jogoId(),
                topico.categoria(), topico.titulo(), topico.conteudo(), topico.status().name(),
                topico.totalRespostas(), topico.publicadoEm(), topico.editadoEm(),
                TopicoDeForum.editavelAte(topico.publicadoEm()), topico.dataUltimaAtividade(),
                topico.respostas().stream().map(resposta -> Resposta.de(resposta, nomes)).toList());
    }
}
