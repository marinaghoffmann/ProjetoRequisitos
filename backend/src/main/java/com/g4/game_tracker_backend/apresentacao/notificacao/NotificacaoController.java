package com.g4.game_tracker_backend.apresentacao.notificacao;
import java.util.List;
import java.util.UUID;
import org.springframework.web.bind.annotation.*;
import com.g4.game_tracker_backend.aplicacao.notificacao.ConsultarNotificacoes;
import com.g4.game_tracker_backend.dominio.notificacao.Notificacao;
@RestController
@RequestMapping("/api/notificacoes")
public class NotificacaoController {
    private final ConsultarNotificacoes consulta;
    public NotificacaoController(ConsultarNotificacoes consulta) { this.consulta=consulta; }
    @GetMapping
    public List<Notificacao> listar() { return consulta.listar(); }
    @PatchMapping("/{id}/leitura")
    public Notificacao marcarLida(@PathVariable UUID id) { return consulta.marcarLida(id); }
}
