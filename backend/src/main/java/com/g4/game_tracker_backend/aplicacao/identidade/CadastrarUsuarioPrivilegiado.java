package com.g4.game_tracker_backend.aplicacao.identidade;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.g4.game_tracker_backend.dominio.compartilhado.RegraDeNegocioException;
import com.g4.game_tracker_backend.dominio.identidade.Papel;
import com.g4.game_tracker_backend.dominio.identidade.Usuario;
import com.g4.game_tracker_backend.dominio.identidade.UsuarioRepositorio;

@Service
public class CadastrarUsuarioPrivilegiado {

    private final RegistrarUsuario registrarUsuario;
    private final UsuarioRepositorio usuarios;
    private final VerificadorDeAcesso verificadorDeAcesso;

    public CadastrarUsuarioPrivilegiado(RegistrarUsuario registrarUsuario, UsuarioRepositorio usuarios,
            VerificadorDeAcesso verificadorDeAcesso) {
        this.registrarUsuario = registrarUsuario;
        this.usuarios = usuarios;
        this.verificadorDeAcesso = verificadorDeAcesso;
    }

    @Transactional
    public Usuario executar(String nome, String email, String senha, Papel papel) {
        verificadorDeAcesso.exigirPapel(Papel.ADMINISTRADOR);
        if (papel != Papel.ADMINISTRADOR && papel != Papel.DESENVOLVEDOR) {
            throw new RegraDeNegocioException("Só é possível criar contas de administrador ou desenvolvedor");
        }
        Usuario criado = registrarUsuario.executar(nome, email, senha);
        return usuarios.salvar(criado.comPapel(papel));
    }
}