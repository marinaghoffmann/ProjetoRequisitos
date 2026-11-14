package com.g4.game_tracker_backend.aplicacao.marketplace.pedidos;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.g4.game_tracker_backend.aplicacao.identidade.VerificadorDeAcesso;
import com.g4.game_tracker_backend.dominio.compartilhado.RecursoNaoEncontradoException;
import com.g4.game_tracker_backend.dominio.identidade.Usuario;
import com.g4.game_tracker_backend.dominio.identidade.UsuarioRepositorio;
import com.g4.game_tracker_backend.dominio.marketplace.pedidos.PedidoMarketplace;
import com.g4.game_tracker_backend.dominio.marketplace.pedidos.PedidoMarketplaceRepositorio;

/**
 * Consultas da F-20: os pedidos do usuário (compras e vendas, do mais recente para o mais antigo, com o status
 * da entrega e do pagamento), um pedido só para as partes e o saldo da carteira.
 */
@Service
public class ConsultarPedidos {

    private final PedidoMarketplaceRepositorio pedidos;
    private final MovimentarCarteira carteira;
    private final UsuarioRepositorio usuarios;
    private final VerificadorDeAcesso verificadorDeAcesso;

    public ConsultarPedidos(PedidoMarketplaceRepositorio pedidos, MovimentarCarteira carteira,
            UsuarioRepositorio usuarios, VerificadorDeAcesso verificadorDeAcesso) {
        this.pedidos = pedidos;
        this.carteira = carteira;
        this.usuarios = usuarios;
        this.verificadorDeAcesso = verificadorDeAcesso;
    }

    @Transactional(readOnly = true)
    public List<PedidoDoUsuario> listar() {
        UUID usuarioId = verificadorDeAcesso.usuarioAtualId();
        Map<UUID, String> nomes = new HashMap<>();
        return pedidos.listarPorParticipante(usuarioId).stream()
                .map(pedido -> doPontoDeVista(pedido, usuarioId, nomes))
                .toList();
    }

    @Transactional(readOnly = true)
    public PedidoDoUsuario detalhar(UUID pedidoId) {
        UUID usuarioId = verificadorDeAcesso.usuarioAtualId();
        PedidoMarketplace pedido = pedidos.buscarPorId(pedidoId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Pedido não encontrado"));
        pedido.exigirParte(usuarioId);
        return doPontoDeVista(pedido, usuarioId, new HashMap<>());
    }

    @Transactional(readOnly = true)
    public BigDecimal saldoDaCarteira() {
        return carteira.saldo(verificadorDeAcesso.usuarioAtualId());
    }

    private PedidoDoUsuario doPontoDeVista(PedidoMarketplace pedido, UUID usuarioId, Map<UUID, String> nomes) {
        boolean compra = pedido.compradorId().equals(usuarioId);
        UUID outraParte = compra ? pedido.vendedorId() : pedido.compradorId();
        return new PedidoDoUsuario(pedido, compra ? "COMPRA" : "VENDA", outraParte,
                nomes.computeIfAbsent(outraParte, id -> usuarios.buscarPorId(id).map(Usuario::nome).orElse(null)));
    }

    /** papel é COMPRA ou VENDA, do ponto de vista de quem consulta. */
    public record PedidoDoUsuario(PedidoMarketplace pedido, String papel, UUID outraParteId, String nomeOutraParte) {
    }
}
