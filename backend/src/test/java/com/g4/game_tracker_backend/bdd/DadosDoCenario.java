package com.g4.game_tracker_backend.bdd;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.g4.game_tracker_backend.aplicacao.identidade.CodificadorDeSenha;
import com.g4.game_tracker_backend.dominio.catalogo.Desenvolvedora;
import com.g4.game_tracker_backend.dominio.catalogo.DesenvolvedoraRepositorio;
import com.g4.game_tracker_backend.dominio.catalogo.Jogo;
import com.g4.game_tracker_backend.dominio.catalogo.JogoRepositorio;
import com.g4.game_tracker_backend.dominio.identidade.Papel;
import com.g4.game_tracker_backend.dominio.identidade.Usuario;
import com.g4.game_tracker_backend.dominio.identidade.UsuarioRepositorio;

import io.cucumber.spring.ScenarioScope;

/**
 * Jogadores e jogos de apoio de UM cenário, compartilhados entre as classes de steps. O banco H2 é
 * o mesmo para todos os cenários, então cada nome vira um registro novo (com sufixo aleatório no
 * caso dos jogos) e as consultas filtradas por ele não contam dados de outros cenários.
 */
@Component
@ScenarioScope
public class DadosDoCenario {

    private static final String ESTUDIO_DE_APOIO = "Estúdio BDD";

    private final UsuarioRepositorio usuarios;
    private final CodificadorDeSenha codificadorDeSenha;
    private final JogoRepositorio jogos;
    private final DesenvolvedoraRepositorio desenvolvedoras;

    private final Map<String, Usuario> jogadores = new HashMap<>();
    private final Map<String, Jogo> jogosDoCenario = new HashMap<>();
    private Usuario jogadorAtual;

    public DadosDoCenario(UsuarioRepositorio usuarios, CodificadorDeSenha codificadorDeSenha, JogoRepositorio jogos,
            DesenvolvedoraRepositorio desenvolvedoras) {
        this.usuarios = usuarios;
        this.codificadorDeSenha = codificadorDeSenha;
        this.jogos = jogos;
        this.desenvolvedoras = desenvolvedoras;
    }

    /** Autentica o jogador e passa a tratá-lo como "o jogador" dos steps. */
    public Usuario autenticar(String nome) {
        jogadorAtual = jogador(nome);
        SessaoDeTeste.autenticarComo(jogadorAtual);
        return jogadorAtual;
    }

    public Usuario jogadorAtual() {
        return jogadorAtual;
    }

    public Usuario jogador(String nome) {
        return jogadores.computeIfAbsent(nome, apelido -> usuarios.salvar(Usuario.reconstituir(UUID.randomUUID(),
                apelido, UUID.randomUUID() + "@bdd.example", codificadorDeSenha.codificar("senha-segura-123"),
                Papel.COMUM, true, null)));
    }

    public Jogo jogo(String titulo) {
        return jogosDoCenario.computeIfAbsent(titulo, nome -> jogos.salvar(Jogo.criarManual(
                nome + " " + UUID.randomUUID(), null, null, estudioDeApoio().id(), Set.of(), null, null, null)));
    }

    private Desenvolvedora estudioDeApoio() {
        return desenvolvedoras.buscarPorNome(ESTUDIO_DE_APOIO)
                .orElseGet(() -> desenvolvedoras.salvar(Desenvolvedora.criar(ESTUDIO_DE_APOIO, null, null, null,
                        null)));
    }
}
