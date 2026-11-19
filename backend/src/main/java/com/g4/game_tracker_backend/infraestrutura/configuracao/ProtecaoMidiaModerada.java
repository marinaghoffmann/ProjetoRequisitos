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
import com.g4.game_tracker_backend.infraestrutura.persistencia.moderacao.MidiaModeradaRepository;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/** Bloqueia, mesmo pelo endereço direto, os arquivos enviados de conteúdo oculto ou removido. */
@Component
@Order(1)
public class ProtecaoMidiaModerada extends OncePerRequestFilter {

    private final JdbcTemplate jdbc;
    private final EstadoPublico estados;
    private final MidiaModeradaRepository midias;

    public ProtecaoMidiaModerada(JdbcTemplate jdbc, EstadoPublico estados, MidiaModeradaRepository midias) {
        this.jdbc = jdbc;
        this.estados = estados;
        this.midias = midias;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest requisicao) {
        String caminho = requisicao.getRequestURI();
        return !caminho.startsWith("/uploads/forum/") && !caminho.startsWith("/uploads/perfil/")
                && !caminho.startsWith("/uploads/catalogo/");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest requisicao, HttpServletResponse resposta, FilterChain cadeia)
            throws ServletException, IOException {
        boolean leitura = "GET".equals(requisicao.getMethod()) || "HEAD".equals(requisicao.getMethod());
        if (leitura && bloqueado(requisicao.getRequestURI())) {
            resposta.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        cadeia.doFilter(requisicao, resposta);
    }

    private boolean bloqueado(String caminho) {
        // A capa do catálogo continua pública quando um vendedor reaproveita o endereço no anúncio.
        if (caminho.startsWith("/uploads/catalogo/") && jdbc.queryForObject(
                "select count(*) from jogos where capa_url = ?", Integer.class, caminho) > 0) {
            return false;
        }
        if (midias.findByUrl(caminho).stream().anyMatch(m -> indisponivel(m.tipo(), m.conteudoId()))) {
            return true;
        }
        if (caminho.startsWith("/uploads/forum/")) {
            return algumIndisponivel("select topico_id from forum_topico_imagens where url = ?", TipoConteudo.TOPICO,
                    caminho)
                    || algumIndisponivel("select resposta_id from forum_resposta_imagens where url = ?",
                            TipoConteudo.RESPOSTA, caminho);
        }
        if (caminho.startsWith("/uploads/perfil/")) {
            return algumIndisponivel("select id from usuarios where avatar_url = ? or banner_url = ?",
                    TipoConteudo.PERFIL, caminho, caminho);
        }
        if (caminho.startsWith("/uploads/catalogo/")) {
            return algumIndisponivel("select anuncio_id from anuncios_fotos where url_foto = ?", TipoConteudo.ANUNCIO,
                    caminho);
        }
        return false;
    }

    private boolean algumIndisponivel(String sql, TipoConteudo tipo, Object... parametros) {
        List<UUID> ids = jdbc.query(sql, (rs, linha) -> rs.getObject(1, UUID.class), parametros);
        return ids.stream().anyMatch(id -> indisponivel(tipo, id));
    }

    private boolean indisponivel(TipoConteudo tipo, UUID id) {
        return estados.oculto(tipo, id) || estados.removido(tipo, id);
    }
}
