package com.g4.game_tracker_backend.apresentacao.marketplace.reputacao;

import java.util.Map;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.g4.game_tracker_backend.aplicacao.marketplace.reputacao.AvaliarNegociacao;
import com.g4.game_tracker_backend.aplicacao.marketplace.reputacao.ConsultarReputacao;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/marketplace")
public class ReputacaoController {

    private final AvaliarNegociacao avaliarNegociacao;
    private final ConsultarReputacao consultarReputacao;

    public ReputacaoController(AvaliarNegociacao avaliarNegociacao, ConsultarReputacao consultarReputacao) {
        this.avaliarNegociacao = avaliarNegociacao;
        this.consultarReputacao = consultarReputacao;
    }

    /** O avaliador é o usuário autenticado; o avaliado é a outra parte do pedido. */
    @PostMapping("/pedidos/{pedidoId}/avaliacoes")
    @ResponseStatus(HttpStatus.CREATED)
    public AvaliacaoResponse avaliar(@PathVariable UUID pedidoId, @Valid @RequestBody AvaliacaoRequest request) {
        return AvaliacaoResponse.de(avaliarNegociacao.executar(pedidoId, request.estrelas(), request.tags(),
                request.comentario()), Map.of());
    }

    /** Perfil público de vendedor: reputação e avaliações recebidas. */
    @GetMapping("/usuarios/{usuarioId}/reputacao")
    public PerfilVendedorResponse perfil(@PathVariable UUID usuarioId) {
        return PerfilVendedorResponse.de(consultarReputacao.perfil(usuarioId));
    }
}
