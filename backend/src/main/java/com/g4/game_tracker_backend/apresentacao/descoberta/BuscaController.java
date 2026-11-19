package com.g4.game_tracker_backend.apresentacao.descoberta;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.g4.game_tracker_backend.aplicacao.descoberta.BuscarNoGameTracker;
import com.g4.game_tracker_backend.aplicacao.descoberta.BuscarNoGameTracker.ListaEncontrada;
import com.g4.game_tracker_backend.aplicacao.descoberta.BuscarNoGameTracker.Resultado;
import com.g4.game_tracker_backend.dominio.catalogo.Jogo;

/** Busca global da barra superior (pública): jogos e listas, agrupados por tipo. */
@RestController
public class BuscaController {

    private final BuscarNoGameTracker buscar;

    public BuscaController(BuscarNoGameTracker buscar) {
        this.buscar = buscar;
    }

    @GetMapping("/api/busca")
    public BuscaResponse buscar(@RequestParam(defaultValue = "") String termo) {
        return BuscaResponse.de(buscar.executar(termo));
    }

    public record BuscaResponse(String termo, List<JogoEncontrado> jogos, List<ListaEncontradaResponse> listas) {

        static BuscaResponse de(Resultado resultado) {
            return new BuscaResponse(resultado.termo(),
                    resultado.jogos().stream().map(JogoEncontrado::de).toList(),
                    resultado.listas().stream().map(ListaEncontradaResponse::de).toList());
        }
    }

    public record JogoEncontrado(UUID id, String titulo, String capaUrl, LocalDate dataLancamento) {
        static JogoEncontrado de(Jogo jogo) {
            return new JogoEncontrado(jogo.id(), jogo.titulo(), jogo.capaUrl(), jogo.dataLancamento());
        }
    }

    public record ListaEncontradaResponse(UUID id, String titulo, String descricao, int totalJogos, UUID donoId,
            String nomeDoDono) {
        static ListaEncontradaResponse de(ListaEncontrada encontrada) {
            var lista = encontrada.lista();
            return new ListaEncontradaResponse(lista.id(), lista.titulo(), lista.descricao(), lista.totalJogos(),
                    lista.donoId(), encontrada.nomeDoDono());
        }
    }
}
