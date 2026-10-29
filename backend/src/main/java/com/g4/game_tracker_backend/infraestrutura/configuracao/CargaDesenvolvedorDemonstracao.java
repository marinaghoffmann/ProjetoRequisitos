package com.g4.game_tracker_backend.infraestrutura.configuracao;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import com.g4.game_tracker_backend.aplicacao.identidade.CodificadorDeSenha;
import com.g4.game_tracker_backend.dominio.catalogo.DesenvolvedoraRepositorio;
import com.g4.game_tracker_backend.dominio.identidade.Papel;
import com.g4.game_tracker_backend.dominio.identidade.Usuario;
import com.g4.game_tracker_backend.dominio.identidade.UsuarioRepositorio;

/**
 * Só no perfil h2: cria uma conta de desenvolvedor já vinculada à desenvolvedora da carga
 * inicial, para experimentar o cadastro manual de jogos sem passar pelo administrador.
 */
@Component
@Order(2)
@ConditionalOnProperty("app.demo.desenvolvedor.email")
public class CargaDesenvolvedorDemonstracao implements CommandLineRunner {

    private final UsuarioRepositorio usuarios;
    private final DesenvolvedoraRepositorio desenvolvedoras;
    private final CodificadorDeSenha codificadorDeSenha;
    private final String email;
    private final String senha;

    public CargaDesenvolvedorDemonstracao(UsuarioRepositorio usuarios, DesenvolvedoraRepositorio desenvolvedoras,
            CodificadorDeSenha codificadorDeSenha, @Value("${app.demo.desenvolvedor.email}") String email,
            @Value("${app.demo.desenvolvedor.password}") String senha) {
        this.usuarios = usuarios;
        this.desenvolvedoras = desenvolvedoras;
        this.codificadorDeSenha = codificadorDeSenha;
        this.email = email;
        this.senha = senha;
    }

    @Override
    public void run(String... args) {
        if (usuarios.existePorEmail(email)) {
            return;
        }
        desenvolvedoras.buscarPorNome("Umbra Interactive").ifPresent(estudio -> usuarios.salvar(
                Usuario.reconstituir(UUID.randomUUID(), "Dev Umbra", email, codificadorDeSenha.codificar(senha),
                        Papel.DESENVOLVEDOR, true, estudio.id())));
    }
}
