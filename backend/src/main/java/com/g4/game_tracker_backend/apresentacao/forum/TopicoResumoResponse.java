package com.g4.game_tracker_backend.apresentacao.forum;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import com.g4.game_tracker_backend.dominio.forum.ResumoTopico;
import com.g4.game_tracker_backend.dominio.identidade.Usuario;

public record TopicoResumoResponse(UUID id, UUID autorId, String nomeAutor, UUID jogoId, String categoria,
        String titulo, String status, int totalRespostas, int totalCurtidas, int totalInteracoes,
        String papelAutor, String avatarAutorUrl, Instant dataUltimaAtividade, Instant publicadoEm) {

    static TopicoResumoResponse de(ResumoTopico resumo, Map<UUID, Usuario> autores, com.g4.game_tracker_backend.aplicacao.moderacao.EstadoPublico moderacao) {
        Usuario autor = autores.get(resumo.autorId());
        return new TopicoResumoResponse(resumo.id(), resumo.autorId(), autor == null ? null : (moderacao.visivel(com.g4.game_tracker_backend.dominio.moderacao.TipoConteudo.PERFIL,autor.id()) ? autor.nome() : "Perfil indisponível"), resumo.jogoId(),
                resumo.categoria(), moderacao.removido(com.g4.game_tracker_backend.dominio.moderacao.TipoConteudo.TOPICO,resumo.id()) ? com.g4.game_tracker_backend.aplicacao.moderacao.ModeracaoOperacoes.MENSAGEM_REMOCAO : resumo.titulo(), resumo.status().name(), resumo.totalRespostas(),
                resumo.totalCurtidas(), resumo.totalRespostas() + resumo.totalCurtidas(),
                autor == null ? null : autor.papel().name(), autor == null || !moderacao.visivel(com.g4.game_tracker_backend.dominio.moderacao.TipoConteudo.PERFIL,autor.id()) ? null : autor.avatarUrl(),
                resumo.dataUltimaAtividade(), resumo.publicadoEm());
    }
}
