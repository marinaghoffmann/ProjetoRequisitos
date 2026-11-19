package com.g4.game_tracker_backend.apresentacao.forum;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import com.g4.game_tracker_backend.aplicacao.moderacao.EstadoPublico;
import com.g4.game_tracker_backend.dominio.compartilhado.ConteudoOriginal;
import com.g4.game_tracker_backend.dominio.forum.ResumoTopico;
import com.g4.game_tracker_backend.dominio.identidade.Usuario;
import com.g4.game_tracker_backend.dominio.moderacao.ConteudoRemovidoDecorator;
import com.g4.game_tracker_backend.dominio.moderacao.TipoConteudo;

public record TopicoResumoResponse(UUID id, UUID autorId, String nomeAutor, UUID jogoId, String categoria,
        String titulo, String status, int totalRespostas, int totalCurtidas, int totalInteracoes,
        String papelAutor, String avatarAutorUrl, Instant dataUltimaAtividade, Instant publicadoEm) {

    static TopicoResumoResponse de(ResumoTopico resumo, Map<UUID, Usuario> autores, EstadoPublico moderacao) {
        Usuario autor = autores.get(resumo.autorId());
        boolean removido = moderacao.removido(TipoConteudo.TOPICO, resumo.id());
        return new TopicoResumoResponse(resumo.id(), resumo.autorId(), TopicoResponse.nomeDoAutor(autor, moderacao),
                resumo.jogoId(), resumo.categoria(),
                new ConteudoRemovidoDecorator(new ConteudoOriginal(resumo.titulo()), removido).exibir(),
                resumo.status().name(), resumo.totalRespostas(), resumo.totalCurtidas(),
                resumo.totalRespostas() + resumo.totalCurtidas(),
                autor == null ? null : autor.papel().name(), TopicoResponse.avatarDoAutor(autor, moderacao),
                resumo.dataUltimaAtividade(), resumo.publicadoEm());
    }
}
