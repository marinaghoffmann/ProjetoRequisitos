package com.g4.game_tracker_backend.apresentacao.moderacao;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.data.domain.Page;
import com.g4.game_tracker_backend.dominio.compartilhado.Pagina;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.g4.game_tracker_backend.aplicacao.moderacao.EstadoPublico;
import com.g4.game_tracker_backend.aplicacao.moderacao.ModeracaoOperacoes;
import com.g4.game_tracker_backend.aplicacao.moderacao.ModeracaoOperacoes.Advertencia;
import com.g4.game_tracker_backend.aplicacao.moderacao.ModeracaoOperacoes.Alvo;
import com.g4.game_tracker_backend.aplicacao.moderacao.ModeracaoOperacoes.Denuncia;
import com.g4.game_tracker_backend.aplicacao.moderacao.ModeracaoOperacoes.Log;
import com.g4.game_tracker_backend.dominio.moderacao.AcaoModeracao;
import com.g4.game_tracker_backend.dominio.moderacao.TipoConteudo;

@RestController
public class ModeracaoController {

    public record DenunciaRequest(TipoConteudo tipoConteudo, UUID conteudoId, String motivo) {
    }

    public record AcaoRequest(AcaoModeracao acao, TipoConteudo tipoConteudo, UUID conteudoId, String justificativa,
            String novoTitulo, String novoConteudo, String novaCategoria, UUID novaRespostaPaiId) {
    }

    public record AdvertenciaRequest(UUID usuarioId, String motivo) {
    }

    public record ArquivarRequest(String justificativa) {
    }

    private final ModeracaoOperacoes moderacao;
    private final EstadoPublico estados;

    public ModeracaoController(ModeracaoOperacoes moderacao, EstadoPublico estados) {
        this.moderacao = moderacao;
        this.estados = estados;
    }

    /** Público: a tela usa para exibir o aviso 18+ e a situação do conteúdo. */
    @GetMapping("/api/moderacao/conteudos/{tipo}/{id}/estado")
    public Map<String, Boolean> estado(@PathVariable TipoConteudo tipo, @PathVariable UUID id) {
        return Map.of("aviso18", estados.aviso18(tipo, id), "oculto", estados.oculto(tipo, id),
                "removido", estados.removido(tipo, id));
    }

    @PostMapping("/api/denuncias")
    @ResponseStatus(HttpStatus.CREATED)
    public Denuncia denunciar(@RequestBody DenunciaRequest r) {
        return moderacao.denunciar(r.tipoConteudo(), r.conteudoId(), r.motivo());
    }

    @GetMapping("/api/moderacao/denuncias")
    public Page<Denuncia> fila(@RequestParam(defaultValue = "1") int pagina,
            @RequestParam(defaultValue = "10") int tamanho) {
        return paraPage(moderacao.fila(pagina, tamanho));
    }

    @PostMapping("/api/moderacao/denuncias/{id}/arquivar")
    public Log arquivar(@PathVariable UUID id, @RequestBody ArquivarRequest r) {
        return moderacao.arquivar(id, r.justificativa());
    }

    @PostMapping("/api/moderacao/acoes")
    public Log agir(@RequestBody AcaoRequest r) {
        return moderacao.agir(r.acao(), r.tipoConteudo(), r.conteudoId(), r.justificativa(), r.novoTitulo(),
                r.novoConteudo(), r.novaCategoria(), r.novaRespostaPaiId());
    }

    @GetMapping("/api/moderacao/logs")
    public Page<Log> logs(@RequestParam(defaultValue = "1") int pagina,
            @RequestParam(defaultValue = "10") int tamanho) {
        return paraPage(moderacao.historico(pagina, tamanho));
    }

    @GetMapping("/api/moderacao/conteudos/{tipo}/{id}/previa")
    public Alvo previa(@PathVariable TipoConteudo tipo, @PathVariable UUID id) {
        return moderacao.previa(tipo, id);
    }

    @PostMapping("/api/moderacao/advertencias")
    public Advertencia advertir(@RequestBody AdvertenciaRequest r) {
        return moderacao.advertir(r.usuarioId(), r.motivo());
    }

    @GetMapping("/api/moderacao/usuarios/{id}/advertencias")
    public List<Advertencia> advertencias(@PathVariable UUID id) {
        return moderacao.advertencias(id);
    }

    @GetMapping("/api/moderacao/usuarios/{id}/advertencias/total")
    public Map<String, Long> totalAdvertencias(@PathVariable UUID id) {
        return Map.of("total", moderacao.totalAdvertencias(id));
    }

    /** A API da central mantém o formato de página do Spring; a aplicação devolve a Pagina do domínio. */
    private static <T> Page<T> paraPage(Pagina<T> pagina) {
        return new PageImpl<>(pagina.itens(), PageRequest.of(pagina.pagina() - 1, pagina.tamanho()),
                pagina.totalItens());
    }
}
