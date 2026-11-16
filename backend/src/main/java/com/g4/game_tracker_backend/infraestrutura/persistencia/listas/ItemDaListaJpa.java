package com.g4.game_tracker_backend.infraestrutura.persistencia.listas;

import java.time.Instant;
import java.util.UUID;

import com.g4.game_tracker_backend.dominio.biblioteca.listas.ItemDaLista;
import com.g4.game_tracker_backend.dominio.biblioteca.listas.ListaPersonalizada;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

/**
 * Item da lista. A coluna lista_id é preenchida pela lista (dona da associação) e aparece aqui só para
 * leitura e para a restrição única da RN 2: o mesmo jogo uma vez por lista.
 */
@Entity
@Table(name = "listas_itens", uniqueConstraints = @UniqueConstraint(name = "uk_listas_itens_lista_jogo",
        columnNames = { "lista_id", "jogo_id" }), indexes = @Index(name = "ix_listas_itens_jogo",
                columnList = "jogo_id"))
public class ItemDaListaJpa {

    @Id
    private UUID id;

    @Column(name = "lista_id", nullable = false, insertable = false, updatable = false)
    private UUID listaId;

    @Column(name = "jogo_id", nullable = false)
    private UUID jogoId;

    @Column(name = "nota_explicativa", nullable = false, length = ListaPersonalizada.LIMITE_DA_NOTA)
    private String notaExplicativa;

    @Column(name = "incluido_em", nullable = false)
    private Instant incluidoEm;

    protected ItemDaListaJpa() {
    }

    private ItemDaListaJpa(ItemDaLista item) {
        this.id = item.id();
        this.jogoId = item.jogoId();
        this.notaExplicativa = item.notaExplicativa();
        this.incluidoEm = item.incluidoEm();
    }

    static ItemDaListaJpa deDominio(ItemDaLista item) {
        return new ItemDaListaJpa(item);
    }

    ItemDaLista paraDominio() {
        return new ItemDaLista(id, jogoId, notaExplicativa, incluidoEm);
    }

    UUID jogoId() {
        return jogoId;
    }

    Instant incluidoEm() {
        return incluidoEm;
    }

    void alterarNota(String novaNota) {
        this.notaExplicativa = novaNota;
    }
}
