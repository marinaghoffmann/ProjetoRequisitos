package com.g4.game_tracker_backend.aplicacao.moderacao;
import java.util.UUID;
import com.g4.game_tracker_backend.dominio.moderacao.TipoConteudo;
public interface EstadoPublico {
 boolean visivel(TipoConteudo tipo,UUID id);
 boolean removido(TipoConteudo tipo,UUID id);
 boolean oculto(TipoConteudo tipo,UUID id);
 boolean aviso18(TipoConteudo tipo,UUID id);
 void exigirVisivel(TipoConteudo tipo,UUID id);
 void exigirAtivo(TipoConteudo tipo,UUID id);
}
