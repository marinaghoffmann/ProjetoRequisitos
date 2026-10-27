package com.g4.game_tracker_backend.aplicacao.catalogo;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.g4.game_tracker_backend.dominio.catalogo.Desenvolvedora;
import com.g4.game_tracker_backend.dominio.catalogo.DesenvolvedoraRepositorio;
import com.g4.game_tracker_backend.dominio.compartilhado.RecursoNaoEncontradoException;

@Service
public class AtualizarDesenvolvedora {

    private final DesenvolvedoraRepositorio desenvolvedoras;

    public AtualizarDesenvolvedora(DesenvolvedoraRepositorio desenvolvedoras) {
        this.desenvolvedoras = desenvolvedoras;
    }

    @Transactional
    public Desenvolvedora executar(UUID id, String biografia, String site, String redesSociais, String pais) {
        Desenvolvedora atual = desenvolvedoras.buscarPorId(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Desenvolvedora não encontrada"));
        return desenvolvedoras.salvar(atual.atualizarPerfil(biografia, site, redesSociais, pais));
    }
}
