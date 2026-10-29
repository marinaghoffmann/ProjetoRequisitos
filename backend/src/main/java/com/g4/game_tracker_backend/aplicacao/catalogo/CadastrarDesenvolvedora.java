package com.g4.game_tracker_backend.aplicacao.catalogo;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.g4.game_tracker_backend.dominio.catalogo.Desenvolvedora;
import com.g4.game_tracker_backend.dominio.catalogo.DesenvolvedoraRepositorio;

@Service
public class CadastrarDesenvolvedora {

    private final DesenvolvedoraRepositorio desenvolvedoras;

    public CadastrarDesenvolvedora(DesenvolvedoraRepositorio desenvolvedoras) {
        this.desenvolvedoras = desenvolvedoras;
    }

    @Transactional
    public Desenvolvedora executar(String nome, String biografia, String site, String redesSociais, String pais) {
        return desenvolvedoras.salvar(Desenvolvedora.criar(nome, biografia, site, redesSociais, pais));
    }

    @Transactional(readOnly = true)
    public List<Desenvolvedora> listar() {
        return desenvolvedoras.listar();
    }
}
