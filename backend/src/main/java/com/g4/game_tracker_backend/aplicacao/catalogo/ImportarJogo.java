package com.g4.game_tracker_backend.aplicacao.catalogo;

import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.g4.game_tracker_backend.aplicacao.identidade.VerificadorDeAcesso;
import com.g4.game_tracker_backend.dominio.catalogo.Desenvolvedora;
import com.g4.game_tracker_backend.dominio.catalogo.DesenvolvedoraRepositorio;
import com.g4.game_tracker_backend.dominio.catalogo.GeneroRepositorio;
import com.g4.game_tracker_backend.dominio.catalogo.Jogo;
import com.g4.game_tracker_backend.dominio.catalogo.JogoRepositorio;
import com.g4.game_tracker_backend.dominio.compartilhado.RecursoNaoEncontradoException;
import com.g4.game_tracker_backend.dominio.identidade.Papel;

/** F-01 RN 1: importação pela API externa; se o identificador externo já existe, atualiza o jogo. */
@Service
public class ImportarJogo {

    private final JogoRepositorio jogos;
    private final DesenvolvedoraRepositorio desenvolvedoras;
    private final GeneroRepositorio generos;
    private final VerificadorDeAcesso verificadorDeAcesso;

    public ImportarJogo(JogoRepositorio jogos, DesenvolvedoraRepositorio desenvolvedoras,
            GeneroRepositorio generos, VerificadorDeAcesso verificadorDeAcesso) {
        this.jogos = jogos;
        this.desenvolvedoras = desenvolvedoras;
        this.generos = generos;
        this.verificadorDeAcesso = verificadorDeAcesso;
    }

    @Transactional
    public Jogo executar(String titulo, String tituloOriginal, String resumo, String identificadorExterno,
            UUID desenvolvedoraId, Set<UUID> generosIds, LocalDate dataLancamento, String capaUrl,
            String trailerUrl) {
        verificadorDeAcesso.exigirPapel(Papel.ADMINISTRADOR);
        Desenvolvedora desenvolvedora = desenvolvedoras.buscarPorId(desenvolvedoraId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Desenvolvedora não encontrada"));
        ValidacaoCatalogo.exigirGeneros(generos, generosIds);

        Jogo existente = jogos.buscarPorIdentificadorExterno(identificadorExterno).orElse(null);
        if (existente != null) {
            return jogos.salvar(existente.atualizarDados(titulo, tituloOriginal, resumo,
                    desenvolvedora.id(), generosIds, dataLancamento, capaUrl, trailerUrl));
        }
        return jogos.salvar(Jogo.importar(titulo, tituloOriginal, resumo, identificadorExterno,
                desenvolvedora.id(), generosIds, dataLancamento, capaUrl, trailerUrl));
    }
}
