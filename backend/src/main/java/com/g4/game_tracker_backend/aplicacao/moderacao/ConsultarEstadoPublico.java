package com.g4.game_tracker_backend.aplicacao.moderacao;

import java.util.Optional;
import java.util.UUID;

import com.g4.game_tracker_backend.dominio.compartilhado.RecursoNaoEncontradoException;
import com.g4.game_tracker_backend.dominio.moderacao.EstadoConteudo;
import com.g4.game_tracker_backend.dominio.moderacao.EstadoConteudoRepositorio;
import com.g4.game_tracker_backend.dominio.moderacao.TipoConteudo;
import com.g4.game_tracker_backend.aplicacao.compartilhado.CasoDeUso;

/** Conteúdo sem estado gravado nunca foi moderado: é público e sem aviso. */
@CasoDeUso
public class ConsultarEstadoPublico implements EstadoPublico {

    private final EstadoConteudoRepositorio estados;

    public ConsultarEstadoPublico(EstadoConteudoRepositorio estados) {
        this.estados = estados;
    }

    @Override
    public boolean visivel(TipoConteudo tipo, UUID id) {
        return estado(tipo, id).map(EstadoConteudo::visivelPublicamente).orElse(true);
    }

    @Override
    public boolean removido(TipoConteudo tipo, UUID id) {
        return estado(tipo, id).map(EstadoConteudo::removido).orElse(false);
    }

    @Override
    public boolean oculto(TipoConteudo tipo, UUID id) {
        return estado(tipo, id).map(EstadoConteudo::oculto).orElse(false);
    }

    @Override
    public boolean aviso18(TipoConteudo tipo, UUID id) {
        return estado(tipo, id).map(EstadoConteudo::aviso18).orElse(false);
    }

    @Override
    public void exigirVisivel(TipoConteudo tipo, UUID id) {
        if (!visivel(tipo, id)) {
            throw new RecursoNaoEncontradoException("Conteúdo não encontrado");
        }
    }

    @Override
    public void exigirAtivo(TipoConteudo tipo, UUID id) {
        if (!estado(tipo, id).map(EstadoConteudo::ativo).orElse(true)) {
            throw new RecursoNaoEncontradoException("Conteúdo indisponível");
        }
    }

    private Optional<EstadoConteudo> estado(TipoConteudo tipo, UUID id) {
        return estados.buscar(tipo, id);
    }
}
