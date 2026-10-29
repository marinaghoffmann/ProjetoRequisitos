package com.g4.game_tracker_backend.dominio.marketplace.anuncios;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import com.g4.game_tracker_backend.dominio.compartilhado.RegraDeNegocioException;

/**
 * Agregado do anúncio do marketplace (F-18), conforme o contrato da T07. É imutável: cada
 * alteração devolve uma nova instância. As mudanças de status passam por CicloDeVidaAnuncio.
 */
public final class AnuncioMarketplace {

    public static final int MAXIMO_DE_FOTOS = 10;

    private final UUID id;
    private final UUID vendedorId;
    private final UUID jogoId;
    private final String titulo;
    private final EstadoConservacao estado;
    private final BigDecimal preco;
    private final TipoNegociacao tipoNegociacao;
    private final MidiaJogo midia;
    private final String localidade;
    private final List<FotoAnuncio> fotos;
    private final StatusAnuncio status;
    private final Instant criadoEm;
    private final Instant atualizadoEm;

    private AnuncioMarketplace(UUID id, UUID vendedorId, UUID jogoId, String titulo, EstadoConservacao estado,
            BigDecimal preco, TipoNegociacao tipoNegociacao, MidiaJogo midia, String localidade,
            List<FotoAnuncio> fotos, StatusAnuncio status, Instant criadoEm, Instant atualizadoEm) {
        this.id = Objects.requireNonNull(id, "O identificador do anúncio é obrigatório");
        this.vendedorId = Objects.requireNonNull(vendedorId, "O vendedor é obrigatório");
        this.jogoId = Objects.requireNonNull(jogoId, "O jogo precisa estar cadastrado no catálogo");
        this.titulo = texto(titulo, 180, "O título do anúncio é obrigatório",
                "O título do anúncio deve ter no máximo 180 caracteres");
        this.estado = obrigatorio(estado, "O estado de conservação é obrigatório");
        this.preco = validarPreco(preco);
        this.tipoNegociacao = obrigatorio(tipoNegociacao, "O tipo de negociação é obrigatório");
        this.midia = obrigatorio(midia, "A mídia do jogo é obrigatória");
        this.localidade = texto(localidade, 120, "A localidade para envio é obrigatória",
                "A localidade deve ter no máximo 120 caracteres");
        this.fotos = validarFotos(fotos);
        this.status = Objects.requireNonNull(status, "O status do anúncio é obrigatório");
        this.criadoEm = Objects.requireNonNull(criadoEm, "A data de criação é obrigatória");
        this.atualizadoEm = Objects.requireNonNull(atualizadoEm, "A data de atualização é obrigatória");
    }

    /** F-18 RN 1 e RN 3: todo anúncio nasce ATIVO, com jogo do catálogo, ao menos 1 foto e preço positivo. */
    public static AnuncioMarketplace criar(UUID vendedorId, UUID jogoId, String titulo, EstadoConservacao estado,
            BigDecimal preco, TipoNegociacao tipoNegociacao, MidiaJogo midia, String localidade,
            List<String> urlsDasFotos, Instant agora) {
        return new AnuncioMarketplace(UUID.randomUUID(), vendedorId, jogoId, titulo, estado, preco,
                tipoNegociacao, midia, localidade, ordenar(urlsDasFotos), StatusAnuncio.ATIVO, agora, agora);
    }

    public static AnuncioMarketplace reconstituir(UUID id, UUID vendedorId, UUID jogoId, String titulo,
            EstadoConservacao estado, BigDecimal preco, TipoNegociacao tipoNegociacao, MidiaJogo midia,
            String localidade, List<FotoAnuncio> fotos, StatusAnuncio status, Instant criadoEm,
            Instant atualizadoEm) {
        return new AnuncioMarketplace(id, vendedorId, jogoId, titulo, estado, preco, tipoNegociacao, midia,
                localidade, fotos, status, criadoEm, atualizadoEm);
    }

    /** Edição de valores e dados do item; anúncios vendidos ou cancelados não mudam mais. */
    public AnuncioMarketplace editar(String titulo, EstadoConservacao estado, BigDecimal preco,
            TipoNegociacao tipoNegociacao, MidiaJogo midia, String localidade, List<String> urlsDasFotos,
            Instant agora) {
        if (status.finalizado()) {
            throw new RegraDeNegocioException("Anúncios vendidos ou cancelados não podem ser alterados");
        }
        return new AnuncioMarketplace(id, vendedorId, jogoId, titulo, estado, preco, tipoNegociacao, midia,
                localidade, ordenar(urlsDasFotos), status, criadoEm, agora);
    }

    /** F-18 RN 2: delega a regra de transição ao contrato e devolve o evento a ser publicado. */
    public MudancaDeStatusAnuncio mudarStatus(StatusAnuncio novo, Instant agora) {
        AnuncioStatusAlterado evento = CicloDeVidaAnuncio.transicionar(id, jogoId, vendedorId, status, novo, agora);
        AnuncioMarketplace atualizado = new AnuncioMarketplace(id, vendedorId, jogoId, titulo, estado, preco,
                tipoNegociacao, midia, localidade, fotos, novo, criadoEm, agora);
        return new MudancaDeStatusAnuncio(atualizado, evento);
    }

    public boolean pertenceA(UUID usuarioId) {
        return vendedorId.equals(usuarioId);
    }

    private static List<FotoAnuncio> ordenar(List<String> urlsDasFotos) {
        List<FotoAnuncio> fotos = new ArrayList<>();
        if (urlsDasFotos != null) {
            for (String url : urlsDasFotos) {
                fotos.add(new FotoAnuncio(url, fotos.size() + 1));
            }
        }
        return fotos;
    }

    private static List<FotoAnuncio> validarFotos(List<FotoAnuncio> fotos) {
        if (fotos == null || fotos.isEmpty()) {
            throw new RegraDeNegocioException("O anúncio precisa de pelo menos 1 foto");
        }
        if (fotos.size() > MAXIMO_DE_FOTOS) {
            throw new RegraDeNegocioException("O anúncio pode ter no máximo " + MAXIMO_DE_FOTOS + " fotos");
        }
        return List.copyOf(fotos);
    }

    private static BigDecimal validarPreco(BigDecimal preco) {
        if (preco == null || preco.signum() <= 0) {
            throw new RegraDeNegocioException("O preço deve ser maior que zero");
        }
        if (preco.scale() > 2) {
            throw new RegraDeNegocioException("O preço deve ter no máximo 2 casas decimais");
        }
        return preco.setScale(2, RoundingMode.UNNECESSARY);
    }

    private static <T> T obrigatorio(T valor, String mensagem) {
        if (valor == null) {
            throw new RegraDeNegocioException(mensagem);
        }
        return valor;
    }

    private static String texto(String valor, int limite, String mensagemObrigatorio, String mensagemLimite) {
        if (valor == null || valor.isBlank()) {
            throw new RegraDeNegocioException(mensagemObrigatorio);
        }
        String normalizado = valor.trim();
        if (normalizado.length() > limite) {
            throw new RegraDeNegocioException(mensagemLimite);
        }
        return normalizado;
    }

    public UUID id() { return id; }
    public UUID vendedorId() { return vendedorId; }
    public UUID jogoId() { return jogoId; }
    public String titulo() { return titulo; }
    public EstadoConservacao estado() { return estado; }
    public BigDecimal preco() { return preco; }
    public TipoNegociacao tipoNegociacao() { return tipoNegociacao; }
    public MidiaJogo midia() { return midia; }
    public String localidade() { return localidade; }
    public List<FotoAnuncio> fotos() { return fotos; }
    public StatusAnuncio status() { return status; }
    public Instant criadoEm() { return criadoEm; }
    public Instant atualizadoEm() { return atualizadoEm; }
}
