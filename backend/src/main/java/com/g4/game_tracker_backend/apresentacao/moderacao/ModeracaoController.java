package com.g4.game_tracker_backend.apresentacao.moderacao;

import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import com.g4.game_tracker_backend.aplicacao.moderacao.ModeracaoOperacoes;
import com.g4.game_tracker_backend.dominio.moderacao.*;


@RestController
public class ModeracaoController {
    public record DenunciaRequest(TipoConteudo tipoConteudo,UUID conteudoId,String motivo) {}
    public record AcaoRequest(AcaoModeracao acao,TipoConteudo tipoConteudo,UUID conteudoId,String justificativa,
            String novoTitulo,String novoConteudo,String novaCategoria,UUID novaRespostaPaiId) {}
    public record AdvertenciaRequest(UUID usuarioId,String motivo) {}
    public record ArquivarRequest(String justificativa) {}
    private final ModeracaoOperacoes service;
    private final com.g4.game_tracker_backend.aplicacao.moderacao.EstadoPublico estados;
    public ModeracaoController(ModeracaoOperacoes service, com.g4.game_tracker_backend.aplicacao.moderacao.EstadoPublico estados) { this.service=service; this.estados=estados; }

    @GetMapping("/api/moderacao/conteudos/{tipo}/{id}/estado")
    public java.util.Map<String,Boolean> estado(@PathVariable TipoConteudo tipo,@PathVariable UUID id) {
        return java.util.Map.of("aviso18",estados.aviso18(tipo,id),"oculto",estados.oculto(tipo,id),"removido",estados.removido(tipo,id));
    }
    @PostMapping("/api/denuncias") @ResponseStatus(HttpStatus.CREATED)
    public ModeracaoOperacoes.Denuncia denunciar(@RequestBody DenunciaRequest r) { return service.denunciar(r.tipoConteudo(),r.conteudoId(),r.motivo()); }
    @GetMapping("/api/moderacao/denuncias")
    public Page<ModeracaoOperacoes.Denuncia> fila(@RequestParam(defaultValue="1") int pagina,@RequestParam(defaultValue="10") int tamanho) { return service.fila(pagina,tamanho); }
    @PostMapping("/api/moderacao/denuncias/{id}/arquivar")
    public ModeracaoOperacoes.Log arquivar(@PathVariable UUID id,@RequestBody ArquivarRequest r) { return service.arquivar(id,r.justificativa()); }
    @PostMapping("/api/moderacao/acoes")
    public ModeracaoOperacoes.Log agir(@RequestBody AcaoRequest r) { return service.agir(r.acao(),r.tipoConteudo(),r.conteudoId(),r.justificativa(),r.novoTitulo(),r.novoConteudo(),r.novaCategoria(),r.novaRespostaPaiId()); }
    @GetMapping("/api/moderacao/logs")
    public Page<ModeracaoOperacoes.Log> logs(@RequestParam(defaultValue="1") int pagina,@RequestParam(defaultValue="10") int tamanho) { return service.historico(pagina,tamanho); }
    @GetMapping("/api/moderacao/conteudos/{tipo}/{id}/previa")
    public ModeracaoOperacoes.Alvo previa(@PathVariable TipoConteudo tipo,@PathVariable UUID id) { return service.previa(tipo,id); }
    @PostMapping("/api/moderacao/advertencias")
    public ModeracaoOperacoes.Advertencia advertir(@RequestBody AdvertenciaRequest r) { return service.advertir(r.usuarioId(),r.motivo()); }
    @GetMapping("/api/moderacao/usuarios/{id}/advertencias")
    public java.util.List<ModeracaoOperacoes.Advertencia> advertencias(@PathVariable UUID id) { return service.advertencias(id); }
    @GetMapping("/api/moderacao/usuarios/{id}/advertencias/total")
    public java.util.Map<String,Long> totalAdvertencias(@PathVariable UUID id) { return java.util.Map.of("total",service.totalAdvertencias(id)); }
}
