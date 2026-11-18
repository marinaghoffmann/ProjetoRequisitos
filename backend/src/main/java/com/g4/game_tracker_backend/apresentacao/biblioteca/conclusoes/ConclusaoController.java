package com.g4.game_tracker_backend.apresentacao.biblioteca.conclusoes;

import java.net.URI;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.g4.game_tracker_backend.aplicacao.biblioteca.conclusoes.GerenciarConclusoes;
import com.g4.game_tracker_backend.dominio.biblioteca.conclusoes.RegistroDeConclusao;
import com.g4.game_tracker_backend.dominio.biblioteca.conclusoes.RegistroDeConclusao.Dados;
import com.g4.game_tracker_backend.dominio.biblioteca.conclusoes.RegistroZeradoNaBiblioteca;
import com.g4.game_tracker_backend.dominio.biblioteca.conclusoes.ResumoJogoZerado;

@RestController
@RequestMapping("/api/biblioteca")
public class ConclusaoController {

    public record EdicaoRequest(UUID jogoId, LocalDate dataConclusao, String plataforma,
            BigDecimal horasJogadas, boolean concluido, boolean platina) {

        Dados dados() {
            return new Dados(dataConclusao, plataforma, horasJogadas, concluido, platina);
        }
    }

    private final GerenciarConclusoes conclusoes;

    public ConclusaoController(GerenciarConclusoes conclusoes) {
        this.conclusoes = conclusoes;
    }

    @GetMapping("/zerados")
    public List<ResumoJogoZerado> jogosZerados() {
        return conclusoes.meusJogosZerados();
    }

    @GetMapping("/zerados/registros")
    public List<RegistroZeradoNaBiblioteca> registros() {
        return conclusoes.meusRegistros();
    }

    @GetMapping("/jogos/{jogoId}/conclusoes")
    public List<RegistroDeConclusao> historico(@PathVariable UUID jogoId) {
        return conclusoes.meuHistorico(jogoId);
    }

    @PostMapping("/jogos/{jogoId}/conclusoes")
    public ResponseEntity<RegistroDeConclusao> registrar(@PathVariable UUID jogoId, @RequestBody Dados dados) {
        RegistroDeConclusao registro = conclusoes.registrar(jogoId, dados);
        return ResponseEntity.created(URI.create("/api/biblioteca/conclusoes/" + registro.id())).body(registro);
    }

    @PutMapping("/conclusoes/{id}")
    public RegistroDeConclusao editar(@PathVariable UUID id, @RequestBody EdicaoRequest request) {
        return conclusoes.editar(id, request.jogoId(), request.dados());
    }

    @DeleteMapping("/conclusoes/{id}")
    public ResponseEntity<Void> excluir(@PathVariable UUID id) {
        conclusoes.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
