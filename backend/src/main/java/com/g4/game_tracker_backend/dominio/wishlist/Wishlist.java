package com.g4.game_tracker_backend.dominio.wishlist;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import com.g4.game_tracker_backend.dominio.compartilhado.RecursoNaoEncontradoException;
import com.g4.game_tracker_backend.dominio.compartilhado.RegraDeNegocioException;

public final class Wishlist {

  private final UUID donoId;
  private final Instant criadaEm;
  private final List<ItemWishlist> itens;

  private Wishlist(UUID donoId, Instant criadaEm, List<ItemWishlist> itens) {
    this.donoId = Objects.requireNonNull(donoId, "O dono da wishlist é obrigatório");
    this.criadaEm = Objects.requireNonNull(criadaEm, "A data de criação é obrigatória");
    this.itens = List.copyOf(itens);
  }

  /** RN 1: a wishlist nasce vazia, na primeira adição do usuário. */
  public static Wishlist criar(UUID donoId, Instant agora) {
    return new Wishlist(donoId, agora, List.of());
  }

  public static Wishlist reconstituir(UUID donoId, Instant criadaEm, List<ItemWishlist> itens) {
    return new Wishlist(donoId, criadaEm, itens);
  }

  /**
   * RN 2 (sem repetidos) e RN 3 (prioridade padrão, aplicada no ItemWishlist).
   */
  public Wishlist adicionar(UUID jogoId, PrioridadeWishlist prioridade, Instant agora) {
    if (contem(jogoId)) {
      throw new RegraDeNegocioException("Este jogo já está na sua wishlist");
    }
    List<ItemWishlist> novos = new ArrayList<>(itens);
    novos.add(new ItemWishlist(jogoId, prioridade, agora));
    return new Wishlist(donoId, criadaEm, novos);
  }

  /** Idempotente: remover um jogo que não está na lista não é erro. */
  public Wishlist remover(UUID jogoId) {
    return new Wishlist(donoId, criadaEm,
        itens.stream().filter(i -> !i.jogoId().equals(jogoId)).toList());
  }

  public Wishlist alterarPrioridade(UUID jogoId, PrioridadeWishlist nova) {
    Objects.requireNonNull(nova, "A prioridade é obrigatória");
    if (!contem(jogoId)) {
      throw new RecursoNaoEncontradoException("Este jogo não está na sua wishlist");
    }
    return new Wishlist(donoId, criadaEm, itens.stream()
        .map(i -> i.jogoId().equals(jogoId) ? i.comPrioridade(nova) : i).toList());
  }

  /** ALTA, MEDIA, BAIXA; dentro da mesma prioridade, o mais recente primeiro. */
  public List<ItemWishlist> itensOrdenados() {
    return itens.stream()
        .sorted(Comparator.comparing(ItemWishlist::prioridade)
            .thenComparing(ItemWishlist::adicionadoEm, Comparator.reverseOrder()))
        .toList();
  }

  public boolean contem(UUID jogoId) {
    return itens.stream().anyMatch(i -> i.jogoId().equals(jogoId));
  }

  public UUID donoId() {
    return donoId;
  }

  public Instant criadaEm() {
    return criadaEm;
  }

  public List<ItemWishlist> itens() {
    return itens;
  }
}