package com.g4.game_tracker_backend.aplicacao.identidade;

import java.util.UUID;

import com.g4.game_tracker_backend.dominio.catalogo.DesenvolvedoraRepositorio;
import com.g4.game_tracker_backend.dominio.compartilhado.RecursoNaoEncontradoException;
import com.g4.game_tracker_backend.dominio.compartilhado.RegraDeNegocioException;
import com.g4.game_tracker_backend.dominio.identidade.Papel;
import com.g4.game_tracker_backend.dominio.identidade.Usuario;
import com.g4.game_tracker_backend.dominio.identidade.UsuarioRepositorio;
import com.g4.game_tracker_backend.aplicacao.compartilhado.CasoDeUso;
import com.g4.game_tracker_backend.aplicacao.compartilhado.Transacional;

@CasoDeUso
public class VincularDesenvolvedora {

    private final UsuarioRepositorio usuarios;
    private final DesenvolvedoraRepositorio desenvolvedoras;
    private final VerificadorDeAcesso verificadorDeAcesso;

    public VincularDesenvolvedora(UsuarioRepositorio usuarios, DesenvolvedoraRepositorio desenvolvedoras,
            VerificadorDeAcesso verificadorDeAcesso) {
        this.usuarios = usuarios;
        this.desenvolvedoras = desenvolvedoras;
        this.verificadorDeAcesso = verificadorDeAcesso;
    }

    @Transacional
    public Usuario executar(UUID usuarioId, UUID desenvolvedoraId) {
        verificadorDeAcesso.exigirPapel(Papel.ADMINISTRADOR);
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
