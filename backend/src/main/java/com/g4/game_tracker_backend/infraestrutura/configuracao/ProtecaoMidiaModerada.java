package com.g4.game_tracker_backend.infraestrutura.configuracao;

import java.io.IOException;
import java.util.List;
import java.util.UUID;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import com.g4.game_tracker_backend.aplicacao.moderacao.EstadoPublico;
import com.g4.game_tracker_backend.dominio.moderacao.TipoConteudo;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/** Blocks uploaded files attached to hidden or removed user content even by direct URL. */
@Component @Order(1)
public class ProtecaoMidiaModerada extends OncePerRequestFilter {
    private final JdbcTemplate jdbc;
    private final EstadoPublico estados;
    private final com.g4.game_tracker_backend.infraestrutura.persistencia.moderacao.MidiaModeradaRepository midias;
    public ProtecaoMidiaModerada(JdbcTemplate jdbc, EstadoPublico estados,
            com.g4.game_tracker_backend.infraestrutura.persistencia.moderacao.MidiaModeradaRepository midias) { this.jdbc=jdbc; this.estados=estados; this.midias=midias; }
    @Override protected boolean shouldNotFilter(HttpServletRequest request) {
        String path=request.getRequestURI();
        return !path.startsWith("/uploads/forum/") && !path.startsWith("/uploads/perfil/")
                && !path.startsWith("/uploads/catalogo/");
    }
    @Override protected void doFilterInternal(HttpServletRequest request,HttpServletResponse response,FilterChain chain)
            throws ServletException,IOException {
        String path=request.getRequestURI();
        if (!"GET".equals(request.getMethod()) && !"HEAD".equals(request.getMethod())) { chain.doFilter(request,response); return; }
        if (bloqueado(path)) { response.sendError(HttpServletResponse.SC_NOT_FOUND); return; }
        chain.doFilter(request,response);
    }
    private boolean bloqueado(String path) {
        // A catalog cover remains public when a seller reuses its URL in an ad.
        if (path.startsWith("/uploads/catalogo/") && jdbc.queryForObject(
                "select count(*) from jogos where capa_url = ?",Integer.class,path)>0) return false;
        if (midias.findByUrl(path).stream().anyMatch(m -> estados.oculto(m.tipo,m.conteudoId) || estados.removido(m.tipo,m.conteudoId))) return true;
        if (path.startsWith("/uploads/forum/")) {
            if (algumBloqueado("select topico_id from forum_topico_imagens where url = ?",path,TipoConteudo.TOPICO)) return true;
            return algumBloqueado("select resposta_id from forum_resposta_imagens where url = ?",path,TipoConteudo.RESPOSTA);
        }
        if (path.startsWith("/uploads/perfil/"))
            return algumBloqueado("select id from usuarios where avatar_url = ? or banner_url = ?",path,TipoConteudo.PERFIL,true);
        if (path.startsWith("/uploads/catalogo/"))
            return algumBloqueado("select anuncio_id from anuncios_fotos where url_foto = ?",path,TipoConteudo.ANUNCIO);
        return false;
    }
    private boolean algumBloqueado(String sql,String path,TipoConteudo tipo) { return algumBloqueado(sql,path,tipo,false); }
    private boolean algumBloqueado(String sql,String path,TipoConteudo tipo,boolean duasVezes) {
        List<UUID> ids=jdbc.query(sql,(rs,row)->rs.getObject(1,UUID.class),duasVezes ? new Object[]{path,path} : new Object[]{path});
        return ids.stream().anyMatch(id -> estados.oculto(tipo,id) || estados.removido(tipo,id));
    }
}
