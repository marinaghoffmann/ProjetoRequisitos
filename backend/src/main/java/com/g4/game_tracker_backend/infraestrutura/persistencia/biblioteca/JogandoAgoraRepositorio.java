package com.g4.game_tracker_backend.infraestrutura.persistencia.biblioteca;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Repository;

import com.g4.game_tracker_backend.dominio.biblioteca.JogandoAgora;
import com.g4.game_tracker_backend.dominio.biblioteca.JogandoAgoraRepositorio;

@Repository
class JogandoAgoraRepositorioJpa implements JogandoAgoraRepositorio {

    private final JogandoAgoraSpringData dados;

    JogandoAgoraRepositorioJpa(JogandoAgoraSpringData dados) {
        this.dados = dados;
    }

    @Override
    public JogandoAgora salvar(JogandoAgora registro) {
        return dados.save(JogandoAgoraJpa.de(registro)).paraDominio();
    }

    @Override
    public List<JogandoAgora> buscarAtivos(UUID usuarioId) {
        return dados.findByUsuarioIdAndAtivoTrueOrderByDataInicioDesc(usuarioId).stream()
                .map(JogandoAgoraJpa::paraDominio).toList();
    }

    @Override
    public Optional<JogandoAgora> buscarAtivo(UUID usuarioId, UUID jogoId) {
        return dados.findByUsuarioIdAndJogoIdAndAtivoTrue(usuarioId, jogoId).map(JogandoAgoraJpa::paraDominio);
    }

    @Override
    public long contarAtivos(UUID usuarioId) {
        return dados.countByUsuarioIdAndAtivoTrue(usuarioId);
    }
}