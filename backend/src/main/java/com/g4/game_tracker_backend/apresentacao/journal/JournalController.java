package com.g4.game_tracker_backend.apresentacao.journal;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.g4.game_tracker_backend.aplicacao.journal.ConsultarJournal;
import com.g4.game_tracker_backend.aplicacao.journal.GerenciarJournal;
import com.g4.game_tracker_backend.aplicacao.journal.GerenciarJournal.DadosEntrada;
import com.g4.game_tracker_backend.apresentacao.compartilhado.PaginaResponse;
import com.g4.game_tracker_backend.dominio.journal.FiltroJournal;
import com.g4.game_tracker_backend.dominio.journal.ImagemDoJournal;
import com.g4.game_tracker_backend.dominio.journal.TipoOrigem;

@RestController
@RequestMapping("/api/journal/entradas")
public class JournalController {
    public record EntradaRequest(UUID jogoId, LocalDate dataSessao, String titulo, String texto,
            List<String> tags, boolean contemSpoiler, List<UUID> anexosMantidos) {
        DadosEntrada dados() { return new DadosEntrada(jogoId, dataSessao, titulo, texto, tags, contemSpoiler); }
    }

    private final GerenciarJournal gerenciar;
    private final ConsultarJournal consultar;

    public JournalController(GerenciarJournal gerenciar, ConsultarJournal consultar) {
        this.gerenciar = gerenciar;
        this.consultar = consultar;
    }

    @GetMapping("/tags")
    public List<String> tags() { return consultar.tags(null); }

    @GetMapping
    public PaginaResponse<ConsultarJournal.Entrada> listar(@RequestParam(required = false) UUID jogoId,
            @RequestParam(required = false) String tag, @RequestParam(required = false) LocalDate inicio,
            @RequestParam(required = false) LocalDate fim, @RequestParam(required = false) TipoOrigem origem,
            @RequestParam(defaultValue = "1") int pagina, @RequestParam(defaultValue = "20") int tamanho) {
        return PaginaResponse.de(consultar.minhas(new FiltroJournal(jogoId, tag, inicio, fim, origem), pagina, tamanho));
    }

    @GetMapping("/{id}")
    public ConsultarJournal.Entrada obter(@PathVariable UUID id) { return consultar.minha(id); }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ConsultarJournal.Entrada> criar(@RequestPart("entrada") EntradaRequest entrada,
            @RequestPart(value = "imagens", required = false) List<MultipartFile> imagens) throws IOException {
        var criada = gerenciar.criar(entrada.dados(), imagens(imagens));
        return ResponseEntity.status(201).body(consultar.minha(criada.id()));
    }

    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ConsultarJournal.Entrada editar(@PathVariable UUID id, @RequestPart("entrada") EntradaRequest entrada,
            @RequestPart(value = "imagens", required = false) List<MultipartFile> imagens) throws IOException {
        gerenciar.editar(id, entrada.dados(), entrada.anexosMantidos(), imagens(imagens));
        return consultar.minha(id);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable UUID id) {
        gerenciar.excluir(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/anexos/{anexoId}")
    public ResponseEntity<byte[]> imagem(@PathVariable UUID id, @PathVariable UUID anexoId,
            @RequestParam(defaultValue = "false") boolean revelarSpoiler) {
        var imagem = consultar.imagem(id, anexoId, revelarSpoiler);
        return ResponseEntity.ok().contentType(MediaType.parseMediaType(imagem.tipoMidia()))
                .header(HttpHeaders.CACHE_CONTROL, "no-store")
                .header("X-Content-Type-Options", "nosniff").body(imagem.conteudo());
    }

    private static List<ImagemDoJournal> imagens(List<MultipartFile> arquivos) throws IOException {
        var resultado = new ArrayList<ImagemDoJournal>();
        if (arquivos != null) for (var arquivo : arquivos) {
            resultado.add(new ImagemDoJournal(arquivo.getOriginalFilename(), arquivo.getContentType(), arquivo.getBytes()));
        }
        return resultado;
    }
}
