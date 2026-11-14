package com.g4.game_tracker_backend.aplicacao.marketplace.reputacao;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.g4.game_tracker_backend.dominio.compartilhado.RecursoNaoEncontradoException;
import com.g4.game_tracker_backend.dominio.identidade.Usuario;
import com.g4.game_tracker_backend.dominio.identidade.UsuarioRepositorio;
import com.g4.game_tracker_backend.dominio.marketplace.reputacao.AvaliacaoMarketplace;
import com.g4.game_tracker_backend.dominio.marketplace.reputacao.AvaliacaoMarketplaceRepositorio;
import com.g4.game_tracker_backend.dominio.marketplace.reputacao.ReputacaoUsuario;
import com.g4.game_tracker_backend.dominio.marketplace.reputacao.ReputacaoUsuarioRepositorio;

/** Consulta pública da F-21: o perfil de vendedor, com a reputação e as avaliações recebidas. */
@Service
public class ConsultarReputacao {

    private final ReputacaoUsuarioRepositorio reputacoes;
    private final AvaliacaoMarketplaceRepositorio avaliacoes;
    private final UsuarioRepositorio usuarios;

    public ConsultarReputacao(ReputacaoUsuarioRepositorio reputacoes, AvaliacaoMarketplaceRepositorio avaliacoes,
            UsuarioRepositorio usuarios) {
        this.reputacoes = reputacoes;
        this.avaliacoes = avaliacoes;
        this.usuarios = usuarios;
    }

    @Transactional(readOnly = true)
    public PerfilDeVendedor perfil(UUID usuarioId) {
        Usuario usuario = usuarios.buscarPorId(usuarioId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuário não encontrado"));
        ReputacaoUsuario reputacao = reputacoes.buscarPorUsuario(usuarioId)
                .orElseGet(() -> ReputacaoUsuario.semAvaliacoes(usuarioId));
        List<AvaliacaoMarketplace> recebidas = avaliacoes.listarRecebidas(usuarioId);
        return new PerfilDeVendedor(usuario.id(), usuario.nome(), reputacao, recebidas,
                nomes(recebidas.stream().map(AvaliacaoMarketplace::avaliadorId).toList()));
    }

    private Map<UUID, String> nomes(List<UUID> ids) {
        Map<UUID, String> nomes = new HashMap<>();
        for (UUID id : new HashSet<>(ids)) {
            usuarios.buscarPorId(id).ifPresent(usuario -> nomes.put(id, usuario.nome()));
        }
        return nomes;
    }

    /** nomesDosAvaliadores traz o nome de exibição de quem avaliou, para a tela não mostrar só ids. */
    public record PerfilDeVendedor(UUID usuarioId, String nome, ReputacaoUsuario reputacao,
            List<AvaliacaoMarketplace> avaliacoesRecebidas, Map<UUID, String> nomesDosAvaliadores) {
    }
}
