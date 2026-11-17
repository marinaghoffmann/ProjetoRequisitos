package com.g4.game_tracker_backend.dominio.biblioteca.conclusoes;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RegistroDeConclusaoRepositorio {

    void inserir(RegistroDeConclusao registro);

    Optional<RegistroDeConclusao> buscarParaAlterar(UUID id);

    void atualizar(RegistroDeConclusao registro);

    void excluir(UUID id);

    List<RegistroDeConclusao> historico(UUID jogadorId, UUID jogoId);

    List<ResumoJogoZerado> jogosZerados(UUID jogadorId);

    List<RegistroZeradoNaBiblioteca> registrosDoJogador(UUID jogadorId);

    int contarPorJogo(UUID jogoId);
}
