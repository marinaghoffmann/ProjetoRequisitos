package com.g4.game_tracker_backend.aplicacao.marketplace.negociacao;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.g4.game_tracker_backend.aplicacao.identidade.VerificadorDeAcesso;
import com.g4.game_tracker_backend.dominio.identidade.Usuario;
import com.g4.game_tracker_backend.dominio.identidade.UsuarioRepositorio;
import com.g4.game_tracker_backend.dominio.marketplace.anuncios.AnuncioMarketplace;
import com.g4.game_tracker_backend.dominio.marketplace.anuncios.AnuncioMarketplaceRepositorio;
import com.g4.game_tracker_backend.dominio.marketplace.anuncios.FotoAnuncio;
import com.g4.game_tracker_backend.dominio.marketplace.anuncios.StatusAnuncio;
import com.g4.game_tracker_backend.dominio.marketplace.negociacao.ConversaChat;
import com.g4.game_tracker_backend.dominio.marketplace.negociacao.ConversaChatRepositorio;
import com.g4.game_tracker_backend.dominio.marketplace.negociacao.MensagemChat;
import com.g4.game_tracker_backend.dominio.marketplace.negociacao.PropostaNegociacao;
import com.g4.game_tracker_backend.dominio.marketplace.negociacao.StatusProposta;

/**
 * Consultas da F-19: a caixa de mensagens (com o status da última proposta e as não lidas), o total de não
 * lidas do menu e uma conversa aberta, sempre só para as partes (RN 3).
 */
@Service
public class ConsultarConversas {

    private final ConversaChatRepositorio conversas;
    private final AnuncioMarketplaceRepositorio anuncios;
    private final UsuarioRepositorio usuarios;
    private final VerificadorDeAcesso verificadorDeAcesso;

    public ConsultarConversas(ConversaChatRepositorio conversas, AnuncioMarketplaceRepositorio anuncios,
            UsuarioRepositorio usuarios, VerificadorDeAcesso verificadorDeAcesso) {
        this.conversas = conversas;
        this.anuncios = anuncios;
        this.usuarios = usuarios;
        this.verificadorDeAcesso = verificadorDeAcesso;
    }

    /** Conversas do usuário como comprador ou vendedor, da atividade mais recente para a mais antiga. */
    @Transactional(readOnly = true)
    public List<ResumoConversa> listar() {
        UUID usuarioId = verificadorDeAcesso.usuarioAtualId();
        Map<UUID, String> nomes = new HashMap<>();
        Map<UUID, Optional<AnuncioMarketplace>> anunciosCarregados = new HashMap<>();
        return conversas.listarPorParticipante(usuarioId).stream()
                .map(conversa -> {
                    UUID outraParte = conversa.outraParte(usuarioId);
                    Optional<AnuncioMarketplace> anuncio = anunciosCarregados.computeIfAbsent(conversa.anuncioId(),
                            anuncios::buscarPorId);
                    return new ResumoConversa(conversa.id(), conversa.anuncioId(),
                            anuncio.map(AnuncioMarketplace::titulo).orElse(null),
                            anuncio.map(ConsultarConversas::primeiraFoto).orElse(null),
                            anuncio.map(AnuncioMarketplace::status).orElse(null),
                            conversa.compradorId().equals(usuarioId) ? "COMPRADOR" : "VENDEDOR", outraParte,
                            nomes.computeIfAbsent(outraParte, this::nome),
                            conversa.ultimaMensagem().map(MensagemChat::texto).orElse(null),
                            conversa.ultimaProposta().map(PropostaNegociacao::status).orElse(null),
                            conversa.naoLidasPor(usuarioId), conversa.ultimaAtividadeEm());
                })
                .toList();
    }

    /** RN 11: total de mensagens recebidas e não lidas, para o menu. */
    @Transactional(readOnly = true)
    public int contarNaoLidas() {
        UUID usuarioId = verificadorDeAcesso.usuarioAtualId();
        return conversas.listarPorParticipante(usuarioId).stream()
                .mapToInt(conversa -> conversa.naoLidasPor(usuarioId))
                .sum();
    }

    @Transactional(readOnly = true)
    public DetalheConversa detalhar(UUID conversaId) {
        UUID usuarioId = verificadorDeAcesso.usuarioAtualId();
        ConversaChat conversa = AcessoAConversa.doParticipante(conversas, conversaId, usuarioId);
        Optional<AnuncioMarketplace> anuncio = anuncios.buscarPorId(conversa.anuncioId());
        StatusAnuncio status = anuncio.map(AnuncioMarketplace::status).orElse(null);
        return new DetalheConversa(conversa,
                anuncio.map(AnuncioMarketplace::titulo).orElse(null),
                anuncio.map(ConsultarConversas::primeiraFoto).orElse(null),
                anuncio.map(AnuncioMarketplace::preco).orElse(null),
                anuncio.map(a -> a.tipoNegociacao().name()).orElse(null),
                status,
                nome(conversa.compradorId()), nome(conversa.vendedorId()),
                status == StatusAnuncio.VENDIDO || status == StatusAnuncio.CANCELADO,
                conversa.naoLidasPor(usuarioId));
    }

    private String nome(UUID usuarioId) {
        return usuarios.buscarPorId(usuarioId).map(Usuario::nome).orElse(null);
    }

    private static String primeiraFoto(AnuncioMarketplace anuncio) {
        return anuncio.fotos().stream().findFirst().map(FotoAnuncio::urlFoto).orElse(null);
    }

    /** papel é o do usuário que consulta na conversa: COMPRADOR ou VENDEDOR. */
    public record ResumoConversa(UUID conversaId, UUID anuncioId, String tituloAnuncio, String fotoAnuncio,
            StatusAnuncio statusAnuncio, String papel, UUID outraParteId, String nomeOutraParte,
            String ultimaMensagem, StatusProposta statusUltimaProposta, int naoLidas, Instant ultimaAtividadeEm) {
    }

    /** somenteLeitura vem de RN 5: anúncio vendido ou cancelado. */
    public record DetalheConversa(ConversaChat conversa, String tituloAnuncio, String fotoAnuncio,
            BigDecimal precoAnuncio, String tipoNegociacao, StatusAnuncio statusAnuncio, String nomeComprador,
            String nomeVendedor, boolean somenteLeitura, int naoLidas) {
    }
}
