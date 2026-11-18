package com.g4.game_tracker_backend.infraestrutura.persistencia.moderacao;
import java.util.UUID;
import org.springframework.stereotype.Service;
import com.g4.game_tracker_backend.aplicacao.moderacao.EstadoPublico;
import com.g4.game_tracker_backend.dominio.compartilhado.RecursoNaoEncontradoException;
import com.g4.game_tracker_backend.dominio.moderacao.TipoConteudo;
@Service
public class EstadoPublicoJpa implements EstadoPublico {
 private final EstadoConteudoRepository estados;
 public EstadoPublicoJpa(EstadoConteudoRepository estados) { this.estados=estados; }
 public boolean visivel(TipoConteudo tipo,UUID id) { return estados.findByTipoAndConteudoId(tipo,id).map(e -> e.publico() || (tipo==TipoConteudo.TOPICO && e.removido && !e.oculto)).orElse(true); }
 public boolean removido(TipoConteudo tipo,UUID id) { return estados.findByTipoAndConteudoId(tipo,id).map(e -> e.removido).orElse(false); }
 public boolean oculto(TipoConteudo tipo,UUID id) { return estados.findByTipoAndConteudoId(tipo,id).map(e -> e.oculto).orElse(false); }
 public boolean aviso18(TipoConteudo tipo,UUID id) { return estados.findByTipoAndConteudoId(tipo,id).map(e -> e.aviso18).orElse(false); }
 public void exigirVisivel(TipoConteudo tipo,UUID id) { if (!visivel(tipo,id)) throw new RecursoNaoEncontradoException("Conteúdo não encontrado"); }
 public void exigirAtivo(TipoConteudo tipo,UUID id) { if (oculto(tipo,id) || removido(tipo,id)) throw new RecursoNaoEncontradoException("Conteúdo indisponível"); }
}
