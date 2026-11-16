package com.g4.game_tracker_backend.apresentacao.lancamentos;
import java.net.URI;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import com.g4.game_tracker_backend.aplicacao.lancamentos.*;
import com.g4.game_tracker_backend.dominio.lancamentos.TipoDataLancamento;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
@RestController
@RequestMapping("/api/lancamentos")
public class LancamentoController {
    public record LancamentoRequest(@NotNull UUID jogoId,@NotNull LocalDate dataPrevista,
            TipoDataLancamento tipoData) {}
    public record AlterarDataRequest(@NotNull LocalDate dataPrevista,TipoDataLancamento tipoData) {}
    private final GerenciarLancamentos gestao;
    private final GerenciarInteresseLancamento interesse;
    private final ConsultarLancamentos consulta;
    public LancamentoController(GerenciarLancamentos gestao,GerenciarInteresseLancamento interesse,
            ConsultarLancamentos consulta) {
        this.gestao=gestao;this.interesse=interesse;this.consulta=consulta;
    }
    private UUID usuario(Jwt jwt) { return jwt==null?null:UUID.fromString(jwt.getSubject()); }
    @GetMapping
    public List<ConsultarLancamentos.Visao> listar(@AuthenticationPrincipal Jwt jwt) {
        return consulta.listar(usuario(jwt));
    }
    @GetMapping("/{id}")
    public ConsultarLancamentos.Visao buscar(@PathVariable UUID id,@AuthenticationPrincipal Jwt jwt) {
        return consulta.buscar(id,usuario(jwt));
    }
    @PostMapping
    public ResponseEntity<ConsultarLancamentos.Visao> cadastrar(@Valid @RequestBody LancamentoRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        var l=gestao.cadastrar(request.jogoId(),request.dataPrevista(),request.tipoData());
        return ResponseEntity.created(URI.create("/api/lancamentos/"+l.id()))
                .body(consulta.buscar(l.id(),usuario(jwt)));
    }
    @PutMapping("/{id}")
    public ConsultarLancamentos.Visao alterar(@PathVariable UUID id,@Valid @RequestBody AlterarDataRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        gestao.alterarData(id,request.dataPrevista(),request.tipoData());
        return consulta.buscar(id,usuario(jwt));
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable UUID id) {
        gestao.excluir(id);
        return ResponseEntity.noContent().build();
    }
    @PutMapping("/{id}/interesse")
    public ResponseEntity<Void> marcar(@PathVariable UUID id) {
        interesse.marcar(id); return ResponseEntity.noContent().build();
    }
    @DeleteMapping("/{id}/interesse")
    public ResponseEntity<Void> desmarcar(@PathVariable UUID id) {
        interesse.desmarcar(id); return ResponseEntity.noContent().build();
    }
}
