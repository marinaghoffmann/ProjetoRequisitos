package com.g4.game_tracker_backend.aplicacao.moderacao;

import java.time.Instant;
import java.util.List;
import java.util.UUID;


import com.g4.game_tracker_backend.dominio.compartilhado.Pagina;
import com.g4.game_tracker_backend.dominio.moderacao.AcaoModeracao;
import com.g4.game_tracker_backend.dominio.moderacao.ConteudoRemovidoDecorator;
import com.g4.game_tracker_backend.dominio.moderacao.StatusDenuncia;
import com.g4.game_tracker_backend.dominio.moderacao.TipoConteudo;

/** Casos de uso da central de moderação (F-14), como a apresentação e os outros contextos os enxergam. */
public interface ModeracaoOperacoes {

    String MENSAGEM_REMOCAO = ConteudoRemovidoDecorator.MENSAGEM;

    /** O conteúdo denunciado: quem é o autor, o tópico a que pertence (no fórum) e um trecho do texto. */
    record Alvo(UUID autorId, UUID topicoId, String previa) {
    }

    record Denuncia(UUID id, UUID denuncianteId, TipoConteudo tipo, UUID conteudoId, String motivo,
            StatusDenuncia status, Instant registradaEm, Instant analisadaEm) {
    }

    record Log(UUID id, UUID moderadorId, AcaoModeracao acao, TipoConteudo tipo, UUID conteudoId,
            String justificativa, Instant registradoEm) {
    }

    record Advertencia(UUID id, UUID usuarioId, UUID moderadorId, String motivo, Instant registradaEm) {
    }

    Alvo previa(TipoConteudo tipo, UUID id);

    Denuncia denunciar(TipoConteudo tipo, UUID id, String motivo);

    Pagina<Denuncia> fila(int pagina, int tamanho);

    Pagina<Log> historico(int pagina, int tamanho);

    List<Advertencia> advertencias(UUID usuarioId);

    long totalAdvertencias(UUID usuarioId);

    Log arquivar(UUID denunciaId, String justificativa);

    Log agir(AcaoModeracao acao, TipoConteudo tipo, UUID id, String justificativa, String novoTitulo,
            String novoConteudo, String novaCategoria);

    Log agir(AcaoModeracao acao, TipoConteudo tipo, UUID id, String justificativa, String novoTitulo,
            String novoConteudo, String novaCategoria, UUID novaRespostaPaiId);

    Advertencia advertir(UUID usuarioId, String motivo);
}
