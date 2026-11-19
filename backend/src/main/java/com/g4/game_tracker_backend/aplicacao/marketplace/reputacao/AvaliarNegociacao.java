package com.g4.game_tracker_backend.aplicacao.marketplace.reputacao;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;


import com.g4.game_tracker_backend.aplicacao.identidade.VerificadorDeAcesso;
import com.g4.game_tracker_backend.dominio.compartilhado.RecursoNaoEncontradoException;
import com.g4.game_tracker_backend.dominio.compartilhado.RegraDeNegocioException;
import com.g4.game_tracker_backend.dominio.marketplace.reputacao.AvaliacaoMarketplace;
import com.g4.game_tracker_backend.dominio.marketplace.reputacao.AvaliacaoMarketplaceCriada;
import com.g4.game_tracker_backend.dominio.marketplace.reputacao.AvaliacaoMarketplaceRepositorio;
import com.g4.game_tracker_backend.dominio.marketplace.reputacao.FonteDePedidos;
import com.g4.game_tracker_backend.dominio.marketplace.reputacao.PedidoAvaliavel;
import com.g4.game_tracker_backend.dominio.marketplace.reputacao.ReputacaoUsuario;
import com.g4.game_tracker_backend.dominio.marketplace.reputacao.ReputacaoUsuarioRepositorio;
import com.g4.game_tracker_backend.dominio.marketplace.reputacao.TagComportamento;
import com.g4.game_tracker_backend.aplicacao.compartilhado.CasoDeUso;
import com.g4.game_tracker_backend.aplicacao.compartilhado.Transacional;
import com.g4.game_tracker_backend.dominio.compartilhado.PublicadorDeEventos;

/**
 * F-21: o comprador ou o vendedor de um pedido concluído avalia a outra parte.
 *
 * RN 2: uma avaliação por parte e pedido.
 * RN 3: a reputação do avaliado é recalculada na mesma transação, com a linha travada para que duas
 * avaliações simultâneas não se sobrescrevam.
 *
 * Os pedidos vêm da F-20 pela porta FonteDePedidos.
 */
@CasoDeUso
public class AvaliarNegociacao {

    private final FonteDePedidos pedidos;
    private final AvaliacaoMarketplaceRepositorio avaliacoes;
    private final ReputacaoUsuarioRepositorio reputacoes;
    private final VerificadorDeAcesso verificadorDeAcesso;
    private final PublicadorDeEventos eventos;

    public AvaliarNegociacao(FonteDePedidos pedidos, AvaliacaoMarketplaceRepositorio avaliacoes,
            ReputacaoUsuarioRepositorio reputacoes, VerificadorDeAcesso verificadorDeAcesso,
            PublicadorDeEventos eventos) {
        this.pedidos = pedidos;
        this.avaliacoes = avaliacoes;
        this.reputacoes = reputacoes;
        this.verificadorDeAcesso = verificadorDeAcesso;
        this.eventos = eventos;
    }

    @Transacional
    public AvaliacaoMarketplace executar(UUID pedidoId, int estrelas, Set<TagComportamento> tags, String comentario) {
        UUID avaliadorId = verificadorDeAcesso.usuarioAtualId();
        PedidoAvaliavel pedido = buscarPedido(pedidoId);
        if (avaliacoes.existePorPedidoEAvaliador(pedidoId, avaliadorId)) {
            throw new RegraDeNegocioException("Você já avaliou este pedido");
        }
        Instant agora = Instant.now();
        AvaliacaoMarketplace avaliacao = avaliacoes.salvar(
                AvaliacaoMarketplace.criar(pedido, avaliadorId, estrelas, tags, comentario, agora));

        ReputacaoUsuario atual = reputacoes.buscarPorUsuarioParaAtualizar(avaliacao.avaliadoId())
                .orElseGet(() -> ReputacaoUsuario.semAvaliacoes(avaliacao.avaliadoId()));
        reputacoes.salvar(atual.registrarAvaliacao(avaliacao.estrelas(), agora));

        eventos.publicar(new AvaliacaoMarketplaceCriada(avaliacao.id(), pedidoId, avaliacao.avaliadoId(),
                avaliacao.estrelas()));
        return avaliacao;
    }

    private PedidoAvaliavel buscarPedido(UUID pedidoId) {
        return pedidos.buscarPedido(pedidoId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Pedido não encontrado"));
    }
}
