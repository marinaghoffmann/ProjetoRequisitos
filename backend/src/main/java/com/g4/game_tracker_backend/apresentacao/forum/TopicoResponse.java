package com.g4.game_tracker_backend.apresentacao.forum;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.g4.game_tracker_backend.dominio.forum.RespostaForum;
import com.g4.game_tracker_backend.dominio.forum.TopicoDeForum;
import com.g4.game_tracker_backend.dominio.identidade.Usuario;
import com.g4.game_tracker_backend.aplicacao.forum.CurtidasForum;

/**
 * Tópico com as respostas em ordem de publicação. O encadeamento vem em respostaPaiId; a tela monta
 * a árvore. Respostas removidas continuam na lista (sem conteúdo) para as respostas filhas não sumirem.
 * editavelAte indica até quando o autor ainda pode editar (RN 2: 48 horas).
 */
public record TopicoResponse(UUID id, UUID autorId, String nomeAutor, UUID jogoId, String categoria, String titulo,
        String conteudo, List<String> imagens, String status, int totalRespostas, int totalCurtidas,
        int totalInteracoes, boolean curtidoPeloUsuario, String papelAutor, String avatarAutorUrl,
        Instant publicadoEm, Instant editadoEm, Instant editavelAte, Instant dataUltimaAtividade, List<Resposta> respostas) {

    public record Resposta(UUID id, UUID autorId, String nomeAutor, String papelAutor, String avatarAutorUrl,
            UUID respostaPaiId, String conteudo, List<String> imagens, boolean removida, int totalCurtidas,
            boolean curtidaPeloUsuario, Instant publicadaEm, Instant editadaEm, Instant editavelAte) {

        static Resposta de(RespostaForum resposta, Map<UUID, Usuario> autores, CurtidasForum.Resumo curtidas) {
            Usuario autor = autores.get(resposta.autorId());
            return new Resposta(resposta.id(), resposta.autorId(), autor == null ? null : autor.nome(),
                    autor == null ? null : autor.papel().name(), autor == null ? null : autor.avatarUrl(),
                    resposta.respostaPaiId(), resposta.removida() ? null : resposta.conteudo(),
                    resposta.removida() ? List.of() : resposta.imagens(), resposta.removida(),
                    curtidas.porResposta().getOrDefault(resposta.id(), 0),
                    curtidas.curtidasDoUsuario().contains(resposta.id()),
                    resposta.publicadaEm(), resposta.editadaEm(), TopicoDeForum.editavelAte(resposta.publicadaEm()));
        }
    }

    static TopicoResponse de(TopicoDeForum topico, Map<UUID, Usuario> autores, CurtidasForum.Resumo curtidas) {
        Usuario autor = autores.get(topico.autorId());
        return new TopicoResponse(topico.id(), topico.autorId(), autor == null ? null : autor.nome(), topico.jogoId(),
                topico.categoria(), topico.titulo(), topico.conteudo(), topico.imagens(), topico.status().name(),
                topico.totalRespostas(), curtidas.totalTopico(), topico.totalRespostas() + curtidas.total(),
                curtidas.curtidasDoUsuario().contains(topico.id()),
                autor == null ? null : autor.papel().name(), autor == null ? null : autor.avatarUrl(),
                topico.publicadoEm(), topico.editadoEm(),
                TopicoDeForum.editavelAte(topico.publicadoEm()), topico.dataUltimaAtividade(),
                topico.respostas().stream().map(resposta -> Resposta.de(resposta, autores, curtidas)).toList());
    }
}