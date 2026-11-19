package com.g4.game_tracker_backend.infraestrutura.persistencia.moderacao;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import com.g4.game_tracker_backend.aplicacao.moderacao.ConteudosModeraveis;
import com.g4.game_tracker_backend.aplicacao.moderacao.ModeracaoOperacoes.Alvo;
import com.g4.game_tracker_backend.dominio.moderacao.TipoConteudo;

/**
 * Acesso, em SQL, às tabelas dos contextos moderados. Os nomes de tabela vêm de uma lista fixa por tipo;
 * nenhum valor do cliente é concatenado ao SQL.
 */
@Component
class ConteudosModeraveisJdbc implements ConteudosModeraveis {

    private final JdbcTemplate jdbc;

    ConteudosModeraveisJdbc(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    /** Colunas: autor, tópico (o próprio id fora do fórum) e o texto mostrado na prévia. */
    @Override
    public Optional<Alvo> buscar(TipoConteudo tipo, UUID id, boolean bloquear) {
        String sql = switch (tipo) {
            case TOPICO -> "select autor_id, id, conteudo from forum_topicos where id = ?";
            case RESPOSTA -> "select autor_id, topico_id, conteudo from forum_respostas where id = ?";
            case REVIEW -> "select autor_id, id, texto from reviews where id = ?";
            case LISTA -> "select dono_id, id, descricao from listas_personalizadas where id = ?";
            case AVALIACAO_MARKETPLACE -> "select avaliador_id, id, comentario from marketplace_avaliacoes where id = ?";
            case ANUNCIO -> "select vendedor_id, id, titulo from anuncios_marketplace where id = ?";
            case PERFIL -> "select id, id, bio from usuarios where id = ?";
            case JOURNAL -> "select dono_id, id, texto from journal_entradas where id = ?";
        };
        return jdbc.query(sql + (bloquear ? " for update" : ""),
                (rs, linha) -> new Alvo(rs.getObject(1, UUID.class), rs.getObject(2, UUID.class), rs.getString(3)),
                id).stream().findFirst();
    }

    @Override
    public Optional<UUID> topicoDaResposta(UUID respostaId) {
        return jdbc.query("select topico_id from forum_respostas where id = ?",
                (rs, linha) -> rs.getObject(1, UUID.class), respostaId).stream().findFirst();
    }

    @Override
    public void bloquearTopico(UUID topicoId) {
        jdbc.queryForObject("select id from forum_topicos where id = ? for update", UUID.class, topicoId);
    }

    @Override
    public String statusDoTopico(UUID topicoId) {
        return jdbc.queryForObject("select status from forum_topicos where id = ?", String.class, topicoId);
    }

    @Override
    public void alterarStatusDoTopico(UUID topicoId, String status) {
        jdbc.update("update forum_topicos set status = ? where id = ?", status, topicoId);
    }

    @Override
    public void encerrarTopico(UUID topicoId, Instant agora) {
        jdbc.update("update forum_topicos set status = 'ENCERRADO', data_ultima_atividade = ? where id = ?",
                agora, topicoId);
    }

    @Override
    public void editarTopico(UUID topicoId, String titulo, String conteudo, Instant agora) {
        jdbc.update("update forum_topicos set titulo = ?, conteudo = ?, editado_em = ?, data_ultima_atividade = ?"
                + " where id = ?", titulo, conteudo, agora, agora, topicoId);
    }

    @Override
    public void editarResposta(UUID respostaId, String conteudo, Instant agora) {
        jdbc.update("update forum_respostas set conteudo = ?, editada_em = ? where id = ?", conteudo, agora,
                respostaId);
        jdbc.update("update forum_topicos set data_ultima_atividade = ?"
                + " where id = (select topico_id from forum_respostas where id = ?)", agora, respostaId);
    }

    @Override
    public void moverTopico(UUID topicoId, String categoria, Instant agora) {
        jdbc.update("update forum_topicos set categoria = ?, data_ultima_atividade = ? where id = ?", categoria,
                agora, topicoId);
    }

    @Override
    public boolean respostaAtivaNoTopico(UUID respostaId, UUID topicoId) {
        Integer total = jdbc.queryForObject("select count(*) from forum_respostas"
                + " where id = ? and topico_id = ? and removida = false", Integer.class, respostaId, topicoId);
        return total != null && total > 0;
    }

    @Override
    public Optional<UUID> paiDaResposta(UUID respostaId) {
        return Optional.ofNullable(jdbc.queryForObject("select resposta_pai_id from forum_respostas where id = ?",
                UUID.class, respostaId));
    }

    @Override
    public void definirPaiDaResposta(UUID respostaId, UUID novoPaiId, UUID topicoId, Instant agora) {
        jdbc.update("update forum_respostas set resposta_pai_id = ?, editada_em = ? where id = ?", novoPaiId, agora,
                respostaId);
        jdbc.update("update forum_topicos set data_ultima_atividade = ? where id = ?", agora, topicoId);
    }

    @Override
    public void recalcularTotalDeRespostas(UUID topicoId, Instant agora) {
        jdbc.update("""
                update forum_topicos set total_respostas = (
                    select count(*) from forum_respostas r
                    where r.topico_id = ? and r.removida = false
                      and not exists (select 1 from moderacao_estados m
                                      where m.tipo = 'RESPOSTA' and m.conteudo_id = r.id
                                        and (m.removido = true or m.oculto = true))),
                    data_ultima_atividade = ?
                where id = ?""", topicoId, agora, topicoId);
    }

    @Override
    public void recalcularTotalDeCurtidas(UUID topicoId) {
        jdbc.update("""
                update forum_topicos set total_curtidas = (
                    select count(*) from forum_curtidas c
                    where c.topico_id = ?
                      and not exists (select 1 from moderacao_estados m
                                      where m.conteudo_id = c.conteudo_id and m.tipo = c.tipo
                                        and (m.oculto = true or m.removido = true)))
                where id = ?""", topicoId, topicoId);
    }

    @Override
    public List<String> urlsDeMidia(TipoConteudo tipo, UUID id) {
        return switch (tipo) {
            case TOPICO -> jdbc.queryForList("select url from forum_topico_imagens where topico_id = ?",
                    String.class, id);
            case RESPOSTA -> jdbc.queryForList("select url from forum_resposta_imagens where resposta_id = ?",
                    String.class, id);
            case ANUNCIO -> jdbc.queryForList("select url_foto from anuncios_fotos where anuncio_id = ?",
                    String.class, id);
            case PERFIL -> jdbc.query("select avatar_url, banner_url from usuarios where id = ?", (rs, linha) -> {
                List<String> urls = new ArrayList<>();
                urls.add(rs.getString(1));
                urls.add(rs.getString(2));
                return urls;
            }, id).stream().flatMap(List::stream).toList();
            // As imagens do journal ficam no banco e são servidas pela API, que já consulta o estado.
            default -> List.of();
        };
    }

    @Override
    public boolean usuarioExiste(UUID usuarioId) {
        Integer total = jdbc.queryForObject("select count(*) from usuarios where id = ?", Integer.class, usuarioId);
        return total != null && total > 0;
    }
}
