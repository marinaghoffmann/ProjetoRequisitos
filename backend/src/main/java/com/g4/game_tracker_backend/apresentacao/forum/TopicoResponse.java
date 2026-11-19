package com.g4.game_tracker_backend.apresentacao.forum;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.g4.game_tracker_backend.aplicacao.forum.CurtidasForum;
import com.g4.game_tracker_backend.aplicacao.moderacao.EstadoPublico;
import com.g4.game_tracker_backend.dominio.compartilhado.ConteudoOriginal;
import com.g4.game_tracker_backend.dominio.forum.IteradorDeRespostas.RespostaNaArvore;
import com.g4.game_tracker_backend.dominio.forum.RespostaForum;
import com.g4.game_tracker_backend.dominio.forum.TopicoDeForum;
import com.g4.game_tracker_backend.dominio.identidade.Usuario;
import com.g4.game_tracker_backend.dominio.moderacao.ConteudoRemovidoDecorator;
import com.g4.game_tracker_backend.dominio.moderacao.TipoConteudo;

/**
 * Tópico com as respostas na ordem da conversa, percorridas pelo IteradorDeRespostas (padrão Iterator): cada
 * resposta vem antes das suas filhas, as irmãs por ordem de publicação, e nivel diz o recuo. O encadeamento
 * continua em respostaPaiId. Respostas removidas continuam na lista (sem conteúdo) para as respostas filhas não sumirem.
 * editavelAte indica até quando o autor ainda pode editar (RN 2: 48 horas).
 * F-14: tópico e resposta removidos pela moderação mostram a mensagem de sistema; a resposta oculta, um aviso.
 */
public record TopicoResponse(UUID id, UUID autorId, String nomeAutor, UUID jogoId, String categoria, String titulo,
        String conteudo, List<String> imagens, String status, int totalRespostas, int totalCurtidas,
        int totalInteracoes, boolean curtidoPeloUsuario, String papelAutor, String avatarAutorUrl,
        Instant publicadoEm, Instant editadoEm, Instant editavelAte, Instant dataUltimaAtividade, List<Resposta> respostas) {

    static final String RESPOSTA_OCULTA = "Resposta oculta pela moderação";
    static final String PERFIL_INDISPONIVEL = "Perfil indisponível";

    public record Resposta(UUID id, UUID autorId, String nomeAutor, String papelAutor, String avatarAutorUrl,
            UUID respostaPaiId, String conteudo, List<String> imagens, boolean removida, boolean removidaPelaModeracao,
            int totalCurtidas, boolean curtidaPeloUsuario, Instant publicadaEm, Instant editadaEm,
            Instant editavelAte, int nivel) {

        static Resposta de(RespostaNaArvore naArvore, Map<UUID, Usuario> autores, CurtidasForum.Resumo curtidas,
                EstadoPublico moderacao) {
            RespostaForum resposta = naArvore.resposta();
            Usuario autor = autores.get(resposta.autorId());
            boolean removidaPelaModeracao = moderacao.removido(TipoConteudo.RESPOSTA, resposta.id());
            boolean oculta = moderacao.oculto(TipoConteudo.RESPOSTA, resposta.id());
            boolean moderada = removidaPelaModeracao || oculta;
            String conteudo;
            if (resposta.removida()) {
                conteudo = null;
            } else if (oculta && !removidaPelaModeracao) {
                conteudo = RESPOSTA_OCULTA;
            } else {
                conteudo = new ConteudoRemovidoDecorator(new ConteudoOriginal(resposta.conteudo()),
                        removidaPelaModeracao).exibir();
            }
            return new Resposta(resposta.id(), resposta.autorId(), nomeDoAutor(autor, moderacao),
                    autor == null ? null : autor.papel().name(), avatarDoAutor(autor, moderacao),
                    resposta.respostaPaiId(), conteudo,
                    resposta.removida() || moderada ? List.of() : resposta.imagens(),
                    resposta.removida() || moderada, moderada,
                    curtidas.porResposta().getOrDefault(resposta.id(), 0),
                    curtidas.curtidasDoUsuario().contains(resposta.id()),
                    resposta.publicadaEm(), resposta.editadaEm(), TopicoDeForum.editavelAte(resposta.publicadaEm()),
                    naArvore.nivel());
        }
    }

    static TopicoResponse de(TopicoDeForum topico, Map<UUID, Usuario> autores, CurtidasForum.Resumo curtidas,
            EstadoPublico moderacao) {
        Usuario autor = autores.get(topico.autorId());
        boolean removido = moderacao.removido(TipoConteudo.TOPICO, topico.id());
        List<Resposta> respostas = new ArrayList<>();
        int respostasVisiveis = 0;
        for (RespostaNaArvore naArvore : topico.arvoreDeRespostas()) {
            RespostaForum r = naArvore.resposta();
            respostas.add(Resposta.de(naArvore, autores, curtidas, moderacao));
            if (!r.removida() && !moderacao.removido(TipoConteudo.RESPOSTA, r.id())
                    && !moderacao.oculto(TipoConteudo.RESPOSTA, r.id())) {
                respostasVisiveis++;
            }
        }
        return new TopicoResponse(topico.id(), topico.autorId(), nomeDoAutor(autor, moderacao), topico.jogoId(),
                topico.categoria(),
                new ConteudoRemovidoDecorator(new ConteudoOriginal(topico.titulo()), removido).exibir(),
                new ConteudoRemovidoDecorator(new ConteudoOriginal(topico.conteudo()), removido).exibir(),
                removido ? List.of() : topico.imagens(), topico.status().name(),
                respostasVisiveis, curtidas.totalTopico(), respostasVisiveis + curtidas.total(),
                curtidas.curtidasDoUsuario().contains(topico.id()),
                autor == null ? null : autor.papel().name(), avatarDoAutor(autor, moderacao),
                topico.publicadoEm(), topico.editadoEm(),
                TopicoDeForum.editavelAte(topico.publicadoEm()), topico.dataUltimaAtividade(),
                respostas);
    }

    /** Perfil oculto ou removido pela moderação não expõe nome nem avatar. */
    static String nomeDoAutor(Usuario autor, EstadoPublico moderacao) {
        if (autor == null) {
            return null;
        }
        return moderacao.visivel(TipoConteudo.PERFIL, autor.id()) ? autor.nome() : PERFIL_INDISPONIVEL;
    }

    static String avatarDoAutor(Usuario autor, EstadoPublico moderacao) {
        return autor == null || !moderacao.visivel(TipoConteudo.PERFIL, autor.id()) ? null : autor.avatarUrl();
    }
}
