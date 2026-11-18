package com.g4.game_tracker_backend.dominio.biblioteca.perfil;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.g4.game_tracker_backend.dominio.compartilhado.RegraDeNegocioException;

public record PerfilFavoritos(UUID donoId, List<UUID> jogos) {

    public PerfilFavoritos {
        jogos = List.copyOf(jogos);
        if (jogos.size() > 4 || jogos.stream().distinct().count() != jogos.size()) {
            throw new RegraDeNegocioException("O perfil deve ter até 4 favoritos sem jogos repetidos");
        }
    }

    public PerfilFavoritos adicionar(UUID jogoId, Integer slot) {
        exigirNovoJogo(jogoId);
        if (jogos.size() == 4) {
            throw new RegraDeNegocioException("Você já possui 4 favoritos");
        }
        int destino = slot == null ? jogos.size() + 1 : slot;
        exigirSlot(destino);
        var novos = new ArrayList<>(jogos);
        novos.add(Math.min(destino - 1, novos.size()), jogoId);
        return new PerfilFavoritos(donoId, novos);
    }

    public PerfilFavoritos mover(UUID jogoId, int slot) {
        exigirSlot(slot);
        if (!jogos.contains(jogoId)) {
            throw new RegraDeNegocioException("Este jogo não está entre os seus favoritos");
        }
        var novos = new ArrayList<>(jogos);
        novos.remove(jogoId);
        novos.add(Math.min(slot - 1, novos.size()), jogoId);
        return new PerfilFavoritos(donoId, novos);
    }

    public PerfilFavoritos substituir(int slot, UUID jogoId) {
        exigirSlot(slot);
        if (slot > jogos.size()) {
            throw new RegraDeNegocioException("Este slot está vazio");
        }
        if (jogos.get(slot - 1).equals(jogoId)) return this;
        exigirNovoJogo(jogoId);
        var novos = new ArrayList<>(jogos);
        novos.set(slot - 1, jogoId);
        return new PerfilFavoritos(donoId, novos);
    }

    public PerfilFavoritos remover(UUID jogoId) {
        var novos = new ArrayList<>(jogos);
        novos.remove(jogoId);
        return new PerfilFavoritos(donoId, novos);
    }

    private void exigirNovoJogo(UUID jogoId) {
        if (jogoId == null) throw new RegraDeNegocioException("O jogo é obrigatório");
        if (jogos.contains(jogoId)) {
            throw new RegraDeNegocioException("Este jogo já está entre os seus favoritos");
        }
    }

    private static void exigirSlot(int slot) {
        if (slot < 1 || slot > 4) {
            throw new RegraDeNegocioException("O slot deve estar entre 1 e 4");
        }
    }
}
