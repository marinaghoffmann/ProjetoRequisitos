package com.g4.game_tracker_backend.aplicacao.identidade;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.g4.game_tracker_backend.dominio.catalogo.DesenvolvedoraRepositorio;
import com.g4.game_tracker_backend.dominio.compartilhado.RecursoNaoEncontradoException;
import com.g4.game_tracker_backend.dominio.compartilhado.RegraDeNegocioException;
import com.g4.game_tracker_backend.dominio.identidade.Papel;
import com.g4.game_tracker_backend.dominio.identidade.Usuario;
import com.g4.game_tracker_backend.dominio.identidade.UsuarioRepositorio;

@Service
public class VincularDesenvolvedora {

    private final UsuarioRepositorio usuarios;
    private final DesenvolvedoraRepositorio desenvolvedoras;

    public VincularDesenvolvedora(UsuarioRepositorio usuarios, DesenvolvedoraRepositorio desenvolvedoras) {
        this.usuarios = usuarios;
        this.desenvolvedoras = desenvolvedoras;
    }

    @Transactional
    public Usuario executar(UUID usuarioId, UUID desenvolvedoraId) {
        Usuario usuario = usuarios.buscarPorId(usuarioId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuário não encontrado"));
        if (usuario.papel() != Papel.DESENVOLVEDOR) {
            throw new RegraDeNegocioException("Apenas usuários desenvolvedores podem ser vinculados");
        }
        var desenvolvedora = desenvolvedoras.buscarPorId(desenvolvedoraId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Desenvolvedora não encontrada"));
        if (!desenvolvedora.ativa()) {
            throw new RegraDeNegocioException("A desenvolvedora precisa estar ativa");
        }
        return usuarios.salvar(usuario.vincularDesenvolvedora(desenvolvedora.id()));
    }
}
