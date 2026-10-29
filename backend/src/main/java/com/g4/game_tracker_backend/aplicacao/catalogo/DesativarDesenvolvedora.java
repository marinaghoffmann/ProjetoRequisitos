package com.g4.game_tracker_backend.aplicacao.catalogo;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.g4.game_tracker_backend.aplicacao.identidade.VerificadorDeAcesso;
import com.g4.game_tracker_backend.dominio.catalogo.Desenvolvedora;
import com.g4.game_tracker_backend.dominio.catalogo.DesenvolvedoraRepositorio;
import com.g4.game_tracker_backend.dominio.compartilhado.RecursoNaoEncontradoException;
import com.g4.game_tracker_backend.dominio.identidade.Papel;

@Service
public class DesativarDesenvolvedora {

    private final DesenvolvedoraRepositorio desenvolvedoras;
    private final VerificadorDeAcesso verificadorDeAcesso;

    public DesativarDesenvolvedora(DesenvolvedoraRepositorio desenvolvedoras,
            VerificadorDeAcesso verificadorDeAcesso) {
        this.desenvolvedoras = desenvolvedoras;
        this.verificadorDeAcesso = verificadorDeAcesso;
    }

    @Transactional
    public Desenvolvedora executar(UUID id) {
        verificadorDeAcesso.exigirPapel(Papel.ADMINISTRADOR);
        Desenvolvedora atual = desenvolvedoras.buscarPorId(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Desenvolvedora não encontrada"));
        return desenvolvedoras.salvar(atual.desativar());
    }
}
