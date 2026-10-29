package com.g4.game_tracker_backend.aplicacao.catalogo;

import java.util.LinkedHashSet;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.g4.game_tracker_backend.aplicacao.identidade.VerificadorDeAcesso;
import com.g4.game_tracker_backend.dominio.catalogo.Desenvolvedora;
import com.g4.game_tracker_backend.dominio.catalogo.DesenvolvedoraRepositorio;
import com.g4.game_tracker_backend.dominio.catalogo.Genero;
import com.g4.game_tracker_backend.dominio.catalogo.GeneroRepositorio;
import com.g4.game_tracker_backend.dominio.catalogo.Jogo;
import com.g4.game_tracker_backend.dominio.catalogo.JogoRepositorio;
import com.g4.game_tracker_backend.dominio.identidade.Papel;

@Service
public class ImportarJogoSteam {

    private final ImportadorJogoExterno importador;
    private final JogoRepositorio jogos;
    private final DesenvolvedoraRepositorio desenvolvedoras;
    private final GeneroRepositorio generos;
    private final VerificadorDeAcesso verificadorDeAcesso;

    public ImportarJogoSteam(ImportadorJogoExterno importador, JogoRepositorio jogos,
            DesenvolvedoraRepositorio desenvolvedoras, GeneroRepositorio generos,
            VerificadorDeAcesso verificadorDeAcesso) {
        this.importador = importador;
        this.jogos = jogos;
        this.desenvolvedoras = desenvolvedoras;
        this.generos = generos;
        this.verificadorDeAcesso = verificadorDeAcesso;
    }

    @Transactional
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
            return jogos.salvar(existente.atualizarDados(dados.titulo(), dados.tituloOriginal(), dados.resumo(),
                    desenvolvedora.id(), generoIds, dados.dataLancamento(), dados.capaUrl(), ""));
        }
        return jogos.salvar(Jogo.importar(dados.titulo(), dados.tituloOriginal(), dados.resumo(),
                "STEAM-" + dados.identificador(), desenvolvedora.id(), generoIds, dados.dataLancamento(),
                dados.capaUrl(), ""));
    }
}
