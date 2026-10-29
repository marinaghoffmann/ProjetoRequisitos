package com.g4.game_tracker_backend.infraestrutura.seguranca;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import com.g4.game_tracker_backend.aplicacao.identidade.ProvisionarAdministradorInicial;

@Component
public class ProvisionamentoAdminInicial implements ApplicationRunner {

    private final ProvisionarAdministradorInicial provisionarAdministrador;
    private final String nome;
    private final String email;
    private final String senha;

    public ProvisionamentoAdminInicial(ProvisionarAdministradorInicial provisionarAdministrador,
            @Value("${app.security.bootstrap-admin.name:Administrador inicial}") String nome,
            @Value("${app.security.bootstrap-admin.email:}") String email,
            @Value("${app.security.bootstrap-admin.password:}") String senha) {
        this.provisionarAdministrador = provisionarAdministrador;
        this.nome = nome;
        this.email = email;
        this.senha = senha;
    }

    @Override
    public void run(ApplicationArguments args) {
        provisionarAdministrador.executar(nome, email, senha);
    }
}
