package com.g4.game_tracker_backend.catalogo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.g4.game_tracker_backend.dominio.catalogo.Desenvolvedora;
import com.g4.game_tracker_backend.dominio.catalogo.Jogo;
import com.g4.game_tracker_backend.dominio.catalogo.OrigemJogo;
import com.g4.game_tracker_backend.dominio.catalogo.StatusCatalogo;
import com.g4.game_tracker_backend.dominio.compartilhado.RegraDeNegocioException;

class CatalogoDominioTest {

    @Test
    void jogoManualRegistraOrigemEStatusAtivo() {
        UUID desenvolvedoraId = UUID.randomUUID();

        Jogo jogo = Jogo.criarManual("Mangue Runner", null, "Corrida em Recife.", desenvolvedoraId,
                Set.of(), null, null, null);

        assertEquals(OrigemJogo.MANUAL_CRIADOR, jogo.origem());
        assertEquals(StatusCatalogo.ATIVO, jogo.status());
        assertEquals(desenvolvedoraId, jogo.desenvolvedoraId());
    }

    @Test
    void jogoImportadoExigeIdentificadorExterno() {
        assertThrows(RegraDeNegocioException.class,
                () -> Jogo.importar("Jogo", null, null, " ", UUID.randomUUID(), Set.of(),
                        null, null, null));
    }

    @Test
    void desenvolvedoraPodeSerDesativadaSemPerderDados() {
        Desenvolvedora desenvolvedora = Desenvolvedora.criar("Studio Recife",
                "Estúdio independente.", null, null, "Brasil");

        Desenvolvedora desativada = desenvolvedora.desativar();

        assertEquals(desenvolvedora.id(), desativada.id());
        assertEquals("Studio Recife", desativada.nome());
        assertFalse(desativada.ativa());
    }
}
