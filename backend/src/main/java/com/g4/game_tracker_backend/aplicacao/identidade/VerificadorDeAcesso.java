package com.g4.game_tracker_backend.aplicacao.identidade;

import java.util.Arrays;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.g4.game_tracker_backend.dominio.compartilhado.AcessoNegadoException;
import com.g4.game_tracker_backend.dominio.identidade.Papel;

/** Regras reutilizáveis para autorização por papel e propriedade de recursos. */
@Component
public class VerificadorDeAcesso {

    private final IdentidadeAutenticada identidade;

    public VerificadorDeAcesso(IdentidadeAutenticada identidade) {
        this.identidade = identidade;
    }

    public UUID usuarioAtualId() {
        return identidade.usuarioId();
    }

    public boolean usuarioAtualTemAlgumPapel(Papel... papeis) {
        return Arrays.asList(papeis).contains(identidade.papel());
    }

    public void exigirPapel(Papel... papeis) {
        if (!usuarioAtualTemAlgumPapel(papeis)) {
            throw new AcessoNegadoException("Você não tem permissão para esta operação");
        }
    }

    public boolean usuarioAtualEDono(UUID donoId) {
        return identidade.usuarioId().equals(donoId);
    }

    public void exigirDono(UUID donoId) {
        exigirDono(donoId, "Apenas o dono pode alterar este recurso");
    }

    public void exigirDono(UUID donoId, String mensagem) {
        if (!usuarioAtualEDono(donoId)) {
            throw new AcessoNegadoException(mensagem);
        }
    }
}
