package com.g4.game_tracker_backend.dominio.moderacao;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.g4.game_tracker_backend.dominio.compartilhado.Pagina;

public interface DenunciaRepositorio {
    DenunciaConteudo salvar(DenunciaConteudo denuncia);
    Optional<DenunciaConteudo> buscarPorId(UUID id);
    /** A denúncia pendente do jogador para o alvo, se houver (uma só por vez). */
    Optional<DenunciaConteudo> buscarPendente(UUID denuncianteId, TipoConteudo tipo, UUID conteudoId);
    List<DenunciaConteudo> listarPendentesDoAlvo(TipoConteudo tipo, UUID conteudoId);
    /** Fila da central, da mais antiga para a mais recente. Página a partir de 1. */
    Pagina<DenunciaConteudo> filaDePendentes(int pagina, int tamanho);
}
