package com.g4.game_tracker_backend.apresentacao.descoberta.similaridade;

import java.math.BigDecimal;
import java.net.URI;
import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.g4.game_tracker_backend.aplicacao.descoberta.similaridade.AjustarSimilaridadeManual;
import com.g4.game_tracker_backend.aplicacao.descoberta.similaridade.ConsultarSimilares;
import com.g4.game_tracker_backend.aplicacao.descoberta.similaridade.GerenciarFeedback;
import com.g4.game_tracker_backend.aplicacao.descoberta.similaridade.RecalcularSimilaridades;
import com.g4.game_tracker_backend.dominio.descoberta.similaridade.SimilaridadeJogo;
import com.g4.game_tracker_backend.dominio.descoberta.similaridade.TipoFeedback;

/** F-10: jogos similares, feedback de relevância e ajustes manuais do administrador. */
@RestController
@RequestMapping("/api/descoberta")
public class SimilaridadeController {

    public record FeedbackRequest(TipoFeedback tipo) {
    }

    public record ConexaoManualRequest(UUID jogoAId, UUID jogoBId, BigDecimal grau) {
    }

    private final ConsultarSimilares consultarSimilares;
    private final RecalcularSimilaridades recalcularSimilaridades;
    private final GerenciarFeedback gerenciarFeedback;
    private final AjustarSimilaridadeManual ajustarSimilaridadeManual;

    public SimilaridadeController(ConsultarSimilares consultarSimilares,
            RecalcularSimilaridades recalcularSimilaridades, GerenciarFeedback gerenciarFeedback,
            AjustarSimilaridadeManual ajustarSimilaridadeManual) {
        this.consultarSimilares = consultarSimilares;
        this.recalcularSimilaridades = recalcularSimilaridades;
        this.gerenciarFeedback = gerenciarFeedback;
        this.ajustarSimilaridadeManual = ajustarSimilaridadeManual;
    }

    @GetMapping("/jogos/{jogoId}/similares")
    public List<SimilarResponse> similares(@PathVariable UUID jogoId,
            @RequestParam(required = false) BigDecimal notaMinima,
            @RequestParam(required = false) Integer limite) {
        return consultarSimilares.consultar(jogoId, notaMinima, limite).stream().map(SimilarResponse::de).toList();
    }

    @PostMapping("/jogos/{jogoId}/similares/recalcular")
    public List<SimilaridadeResponse> recalcular(@PathVariable UUID jogoId) {
        return respostas(recalcularSimilaridades.executarComoAdministrador(jogoId));
    }

    @PostMapping("/similaridades")
    public ResponseEntity<SimilaridadeResponse> criarManual(@RequestBody ConexaoManualRequest request) {
        SimilaridadeJogo conexao = ajustarSimilaridadeManual.criarManual(request.jogoAId(), request.jogoBId(),
                request.grau());
        return ResponseEntity.created(URI.create("/api/descoberta/similaridades/" + conexao.id()))
                .body(SimilaridadeResponse.de(conexao));
    }

    /** A conexão fica REMOVIDA (bloqueada para o recálculo automático) e some das sugestões. */
    @DeleteMapping("/similaridades/{id}")
    public ResponseEntity<Void> remover(@PathVariable UUID id) {
        ajustarSimilaridadeManual.removerConexao(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/similaridades/{id}/feedback")
    public ResponseEntity<SimilaridadeResponse> registrarFeedback(@PathVariable UUID id,
            @RequestBody FeedbackRequest request) {
        return ResponseEntity.status(201)
                .body(SimilaridadeResponse.de(gerenciarFeedback.registrar(id, request.tipo())));
    }

    @PutMapping("/similaridades/{id}/feedback")
    public SimilaridadeResponse editarFeedback(@PathVariable UUID id, @RequestBody FeedbackRequest request) {
        return SimilaridadeResponse.de(gerenciarFeedback.editar(id, request.tipo()));
    }

    @DeleteMapping("/similaridades/{id}/feedback")
    public SimilaridadeResponse removerFeedback(@PathVariable UUID id) {
        return SimilaridadeResponse.de(gerenciarFeedback.remover(id));
    }

    private static List<SimilaridadeResponse> respostas(List<SimilaridadeJogo> conexoes) {
        return conexoes.stream().map(SimilaridadeResponse::de).toList();
    }
}
