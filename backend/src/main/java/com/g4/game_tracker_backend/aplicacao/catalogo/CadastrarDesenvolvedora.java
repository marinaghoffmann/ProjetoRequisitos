package com.g4.game_tracker_backend.aplicacao.catalogo;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.g4.game_tracker_backend.aplicacao.identidade.VerificadorDeAcesso;
import com.g4.game_tracker_backend.dominio.catalogo.Desenvolvedora;
import com.g4.game_tracker_backend.dominio.catalogo.DesenvolvedoraRepositorio;
import com.g4.game_tracker_backend.dominio.compartilhado.RegraDeNegocioException;
import com.g4.game_tracker_backend.dominio.identidade.Papel;

@Service
public class CadastrarDesenvolvedora {

    private final DesenvolvedoraRepositorio desenvolvedoras;
    private final VerificadorDeAcesso verificadorDeAcesso;

    public CadastrarDesenvolvedora(DesenvolvedoraRepositorio desenvolvedoras,
            VerificadorDeAcesso verificadorDeAcesso) {
        this.desenvolvedoras = desenvolvedoras;
        this.verificadorDeAcesso = verificadorDeAcesso;
    }

    @Transactional
    public Desenvolvedora executar(String nome, String biografia, String site, String redesSociais, String pais) {
        verificadorDeAcesso.exigirPapel(Papel.ADMINISTRADOR);
        if (nome != null && desenvolvedoras.buscarPorNome(nome).isPresent()) {
            throw new RegraDeNegocioException("Já existe uma desenvolvedora com este nome");
        }
        return desenvolvedoras.salvar(Desenvolvedora.criar(nome, biografia, site, redesSociais, pais));
    }

    @Transactional(readOnly = true)
    public List<Desenvolvedora> listar() {
        return desenvolvedoras.listar();
    }
}
