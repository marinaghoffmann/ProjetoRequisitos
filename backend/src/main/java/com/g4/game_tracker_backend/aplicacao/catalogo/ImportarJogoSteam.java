package com.g4.game_tracker_backend.aplicacao.catalogo;

import java.util.LinkedHashSet;
import java.util.Set;

import com.g4.game_tracker_backend.aplicacao.identidade.VerificadorDeAcesso;
import com.g4.game_tracker_backend.dominio.catalogo.Desenvolvedora;
import com.g4.game_tracker_backend.dominio.catalogo.DesenvolvedoraRepositorio;
import com.g4.game_tracker_backend.dominio.catalogo.Genero;
import com.g4.game_tracker_backend.dominio.catalogo.GeneroRepositorio;
import com.g4.game_tracker_backend.dominio.catalogo.Jogo;
import com.g4.game_tracker_backend.dominio.catalogo.JogoRepositorio;
import com.g4.game_tracker_backend.dominio.identidade.Papel;
import com.g4.game_tracker_backend.aplicacao.compartilhado.CasoDeUso;
import com.g4.game_tracker_backend.aplicacao.compartilhado.Transacional;

@CasoDeUso
public class ImportarJogoSteam {

    private final ImportadorJogoExterno importador;
    private final JogoRepositorio jogos;
    private final SalvarJogoComEvento salvarComEvento;
    private final DesenvolvedoraRepositorio desenvolvedoras;
    private final GeneroRepositorio generos;
    private final VerificadorDeAcesso verificadorDeAcesso;

    public ImportarJogoSteam(ImportadorJogoExterno importador, JogoRepositorio jogos,
            DesenvolvedoraRepositorio desenvolvedoras, GeneroRepositorio generos,
            VerificadorDeAcesso verificadorDeAcesso, SalvarJogoComEvento salvarComEvento) {
        this.importador = importador;
        this.jogos = jogos;
        this.salvarComEvento = salvarComEvento;
        this.desenvolvedoras = desenvolvedoras;
        this.generos = generos;
        this.verificadorDeAcesso = verificadorDeAcesso;
    }

    @Transacional
    public Jogo executar(String termo) {
        verificadorDeAcesso.exigirPapel(Papel.ADMINISTRADOR);
        var dados = importador.buscar(termo);
        Desenvolvedora desenvolvedora = dados.desenvolvedoras().stream().findFirst()
                .flatMap(desenvolvedoras::buscarPorNome)
                .orElseGet(() -> desenvolvedoras.salvar(Desenvolvedora.criar(
                        dados.desenvolvedoras().stream().findFirst().orElse("Desenvolvedora desconhecida"),
                        "", "", "", "")));
        Set<java.util.UUID> generoIds = new LinkedHashSet<>();
        for (String nome : dados.generos()) {
            Genero genero = generos.buscarPorNome(nome)
                    .orElseGet(() -> generos.salvar(Genero.criar(nome)));
            generoIds.add(genero.id());
        }
        Jogo existente = jogos.buscarPorIdentificadorExterno("STEAM-" + dados.identificador()).orElse(null);
        if (existente != null) {
            return salvarComEvento.salvar(existente, existente.atualizarDados(dados.titulo(), dados.tituloOriginal(), dados.resumo(),
                    desenvolvedora.id(), generoIds, dados.dataLancamento() == null ? existente.dataLancamento() : dados.dataLancamento(),
                    dados.capaUrl(), ""));
        }
        return jogos.salvar(Jogo.importar(dados.titulo(), dados.tituloOriginal(), dados.resumo(),
                "STEAM-" + dados.identificador(), desenvolvedora.id(), generoIds, dados.dataLancamento(),
                dados.capaUrl(), ""));
    }
}
