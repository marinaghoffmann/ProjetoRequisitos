package com.g4.game_tracker_backend.aplicacao.catalogo;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.g4.game_tracker_backend.aplicacao.identidade.VerificadorDeAcesso;
import com.g4.game_tracker_backend.dominio.catalogo.Desenvolvedora;
import com.g4.game_tracker_backend.dominio.catalogo.DesenvolvedoraRepositorio;
import com.g4.game_tracker_backend.dominio.compartilhado.AcessoNegadoException;
import com.g4.game_tracker_backend.dominio.compartilhado.RecursoNaoEncontradoException;
import com.g4.game_tracker_backend.dominio.identidade.Papel;
import com.g4.game_tracker_backend.dominio.identidade.UsuarioRepositorio;

/** Perfil institucional: alterado pelo administrador ou pelo desenvolvedor vinculado à desenvolvedora. */
@Service
public class AtualizarDesenvolvedora {

    private final DesenvolvedoraRepositorio desenvolvedoras;
    private final UsuarioRepositorio usuarios;
    private final VerificadorDeAcesso verificadorDeAcesso;

    public AtualizarDesenvolvedora(DesenvolvedoraRepositorio desenvolvedoras, UsuarioRepositorio usuarios,
            VerificadorDeAcesso verificadorDeAcesso) {
        this.desenvolvedoras = desenvolvedoras;
        this.usuarios = usuarios;
        this.verificadorDeAcesso = verificadorDeAcesso;
    }

    @Transactional
    public Desenvolvedora executar(UUID id, String biografia, String site, String redesSociais, String pais) {
        exigirPermissao(id);
        Desenvolvedora atual = desenvolvedoras.buscarPorId(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Desenvolvedora não encontrada"));
        return desenvolvedoras.salvar(atual.atualizarPerfil(biografia, site, redesSociais, pais));
    }

    private void exigirPermissao(UUID desenvolvedoraId) {
        if (verificadorDeAcesso.usuarioAtualTemAlgumPapel(Papel.ADMINISTRADOR)) {
            return;
        }
        verificadorDeAcesso.exigirPapel(Papel.DESENVOLVEDOR);
        boolean vinculado = usuarios.buscarPorId(verificadorDeAcesso.usuarioAtualId())
                .map(usuario -> desenvolvedoraId.equals(usuario.desenvolvedoraId()))
                .orElse(false);
        if (!vinculado) {
            throw new AcessoNegadoException("O desenvolvedor só pode alterar o perfil da própria desenvolvedora");
        }
    }
}
