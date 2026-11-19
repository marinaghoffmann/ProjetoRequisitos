package com.g4.game_tracker_backend.bdd;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.g4.game_tracker_backend.aplicacao.biblioteca.perfil.ConsultarPerfilPublico;
import com.g4.game_tracker_backend.aplicacao.moderacao.EstadoPublico;
import com.g4.game_tracker_backend.dominio.biblioteca.perfil.PerfilFavoritos;
import com.g4.game_tracker_backend.dominio.biblioteca.perfil.PerfilFavoritosRepositorio;
import com.g4.game_tracker_backend.dominio.catalogo.JogoRepositorio;
import com.g4.game_tracker_backend.dominio.identidade.Papel;
import com.g4.game_tracker_backend.dominio.identidade.Usuario;
import com.g4.game_tracker_backend.dominio.identidade.UsuarioRepositorio;
import com.g4.game_tracker_backend.dominio.moderacao.TipoConteudo;

class PerfilPublicoModeracaoTest {
    @Test
    void mascaraDadosPessoaisDoPerfilModeradoEReverteSemApagarFavoritos() {
        UUID id = UUID.randomUUID();
        var usuarios = mock(UsuarioRepositorio.class);
        var favoritos = mock(PerfilFavoritosRepositorio.class);
        var jogos = mock(JogoRepositorio.class);
        var moderacao = mock(EstadoPublico.class);
        var usuario = Usuario.reconstituir(id, "Maria", "maria@example.com", "hash", Papel.COMUM, true,
                null, "Biografia", "Recife", "/uploads/perfil/avatar/a.jpg", "/uploads/perfil/banner/b.jpg");
        when(usuarios.buscarPorId(id)).thenReturn(Optional.of(usuario));
        when(favoritos.buscar(id)).thenReturn(new PerfilFavoritos(id, List.of()));
        var consulta = new ConsultarPerfilPublico(usuarios, favoritos, jogos, moderacao);

        when(moderacao.visivel(TipoConteudo.PERFIL, id)).thenReturn(false, true);
        var oculto = consulta.consultar(id);
        assertEquals("Perfil indisponível", oculto.nome());
        assertNull(oculto.bio());
        assertNull(oculto.localizacao());
        assertNull(oculto.avatarUrl());
        assertNull(oculto.bannerUrl());
        assertTrue(oculto.favoritos().isEmpty());

        var restaurado = consulta.consultar(id);
        assertEquals("Maria", restaurado.nome());
        assertEquals("Biografia", restaurado.bio());
        assertEquals("/uploads/perfil/avatar/a.jpg", restaurado.avatarUrl());
    }
}
