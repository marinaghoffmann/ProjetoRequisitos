package com.g4.game_tracker_backend.apresentacao.marketplace.anuncios;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.g4.game_tracker_backend.aplicacao.marketplace.anuncios.AlterarStatusAnuncio;
import com.g4.game_tracker_backend.aplicacao.marketplace.anuncios.ArmazenarFotoAnuncio;
import com.g4.game_tracker_backend.aplicacao.marketplace.anuncios.ConsultarAnuncios;
import com.g4.game_tracker_backend.aplicacao.marketplace.anuncios.CriarAnuncio;
import com.g4.game_tracker_backend.aplicacao.marketplace.anuncios.EditarAnuncio;
import com.g4.game_tracker_backend.dominio.marketplace.anuncios.EstadoConservacao;
import com.g4.game_tracker_backend.dominio.marketplace.anuncios.FiltroAnuncios;
import com.g4.game_tracker_backend.dominio.marketplace.anuncios.StatusAnuncio;
import com.g4.game_tracker_backend.dominio.marketplace.anuncios.TipoNegociacao;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/marketplace")
public class AnuncioController {

    private final CriarAnuncio criarAnuncio;
    private final EditarAnuncio editarAnuncio;
    private final AlterarStatusAnuncio alterarStatusAnuncio;
    private final ConsultarAnuncios consultarAnuncios;
    private final ArmazenarFotoAnuncio armazenarFotoAnuncio;

    public AnuncioController(CriarAnuncio criarAnuncio, EditarAnuncio editarAnuncio,
            AlterarStatusAnuncio alterarStatusAnuncio, ConsultarAnuncios consultarAnuncios,
            ArmazenarFotoAnuncio armazenarFotoAnuncio) {
        this.criarAnuncio = criarAnuncio;
        this.editarAnuncio = editarAnuncio;
        this.alterarStatusAnuncio = alterarStatusAnuncio;
        this.consultarAnuncios = consultarAnuncios;
        this.armazenarFotoAnuncio = armazenarFotoAnuncio;
    }

    @GetMapping("/anuncios")
    public List<AnuncioResponse> buscar(@RequestParam(required = false) UUID jogoId,
            @RequestParam(required = false) String titulo,
            @RequestParam(required = false) BigDecimal precoMinimo,
            @RequestParam(required = false) BigDecimal precoMaximo,
            @RequestParam(required = false) EstadoConservacao estado,
            @RequestParam(required = false) TipoNegociacao tipoNegociacao,
            @RequestParam(required = false) String regiao) {
        FiltroAnuncios filtro = new FiltroAnuncios(jogoId, titulo, precoMinimo, precoMaximo, estado,
                tipoNegociacao, regiao);
        return consultarAnuncios.buscar(filtro).stream().map(AnuncioResponse::de).toList();
    }

    @GetMapping("/anuncios/meus")
    public List<AnuncioResponse> meusAnuncios() {
        return consultarAnuncios.meusAnuncios().stream().map(AnuncioResponse::de).toList();
    }

    @GetMapping("/anuncios/{id}")
    public AnuncioResponse consultar(@PathVariable UUID id) {
        return AnuncioResponse.de(consultarAnuncios.buscarPorId(id));
    }

    @PostMapping("/anuncios")
    public ResponseEntity<AnuncioResponse> criar(@Valid @RequestBody AnuncioRequest request) {
        var anuncio = criarAnuncio.executar(request.jogoId(), request.titulo(), request.estado(), request.preco(),
                request.tipoNegociacao(), request.midia(), request.localidade(), request.fotos());
        return ResponseEntity.created(URI.create("/api/marketplace/anuncios/" + anuncio.id()))
                .body(AnuncioResponse.de(anuncio));
    }

    @PutMapping("/anuncios/{id}")
    public AnuncioResponse editar(@PathVariable UUID id, @Valid @RequestBody AnuncioRequest request) {
        return AnuncioResponse.de(editarAnuncio.executar(id, request.titulo(), request.estado(), request.preco(),
                request.tipoNegociacao(), request.midia(), request.localidade(), request.fotos()));
    }

    @PatchMapping("/anuncios/{id}/status")
    public AnuncioResponse alterarStatus(@PathVariable UUID id,
            @Valid @RequestBody AlterarStatusAnuncioRequest request) {
        return AnuncioResponse.de(alterarStatusAnuncio.executar(id, request.status()));
    }

    /** A exclusão é lógica: o anúncio passa a CANCELADO e sai da busca pública. */
    @DeleteMapping("/anuncios/{id}")
    public AnuncioResponse excluir(@PathVariable UUID id) {
        return AnuncioResponse.de(alterarStatusAnuncio.executar(id, StatusAnuncio.CANCELADO));
    }

    @PostMapping(path = "/fotos", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Map<String, String> enviarFoto(@RequestPart("imagem") MultipartFile imagem) {
        try {
            String caminho = armazenarFotoAnuncio.executar(imagem.getBytes(), imagem.getOriginalFilename(),
                    imagem.getContentType());
            return Map.of("caminho", caminho);
        } catch (IOException e) {
            throw new IllegalStateException("Não foi possível ler a foto enviada", e);
        }
    }
}
