package com.g4.game_tracker_backend.infraestrutura.persistencia.listas;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import com.g4.game_tracker_backend.dominio.biblioteca.listas.ItemDaLista;
import com.g4.game_tracker_backend.dominio.biblioteca.listas.ListaPersonalizada;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Version;

/**
 * Lista com os itens.
 *
 * RN 1: titulo_chave guarda o título sem espaços nas pontas e em minúsculas, com restrição única por dono.
 * RN 3: a versão (bloqueio otimista) impede que duas adições simultâneas passem de 100 itens.
 * Excluir a lista remove os itens, pelo JPA (cascade e orphanRemoval) e pelo banco (ON DELETE CASCADE).
 */
@Entity
@Table(name = "listas_personalizadas", uniqueConstraints = @UniqueConstraint(name = "uk_listas_dono_titulo",
        columnNames = { "dono_id", "titulo_chave" }), indexes = @Index(name = "ix_listas_dono",
                columnList = "dono_id"))
public class ListaPersonalizadaJpa {

    @Id
    private UUID id;

    /** null enquanto a lista não foi gravada: assim o Spring Data a persiste como nova. */
    @Version
    private Long versao;

    @Column(name = "dono_id", nullable = false)
    private UUID donoId;

    @Column(nullable = false, length = ListaPersonalizada.LIMITE_DO_TITULO)
    private String titulo;

    @Column(name = "titulo_chave", nullable = false, length = ListaPersonalizada.LIMITE_DO_TITULO)
    private String tituloChave;

    @Column(nullable = false, length = ListaPersonalizada.LIMITE_DA_DESCRICAO)
    private String descricao;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "lista_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @BatchSize(size = 50)
    private List<ItemDaListaJpa> itens = new ArrayList<>();

    @Column(name = "criada_em", nullable = false)
    private Instant criadaEm;

    @Column(name = "atualizada_em", nullable = false)
    private Instant atualizadaEm;

    protected ListaPersonalizadaJpa() {
    }

    private ListaPersonalizadaJpa(ListaPersonalizada lista) {
        this.id = lista.id();
        this.donoId = lista.donoId();
        this.criadaEm = lista.criadaEm();
        atualizarCom(lista);
    }

    static ListaPersonalizadaJpa deDominio(ListaPersonalizada lista) {
        return new ListaPersonalizadaJpa(lista);
    }

    /** Atualiza a entidade gerenciada no lugar; os itens são casados pelo jogo. */
    void atualizarCom(ListaPersonalizada lista) {
        this.titulo = lista.titulo();
        this.tituloChave = lista.chaveDoTitulo();
        this.descricao = lista.descricao();
        this.atualizadaEm = lista.atualizadaEm();

        Map<UUID, ItemDaLista> novos = lista.itens().stream()
                .collect(Collectors.toMap(ItemDaLista::jogoId, Function.identity()));
        itens.removeIf(item -> !novos.containsKey(item.jogoId()));
        for (ItemDaLista novo : novos.values()) {
            itens.stream()
                    .filter(item -> item.jogoId().equals(novo.jogoId()))
                    .findFirst()
                    .ifPresentOrElse(item -> item.alterarNota(novo.notaExplicativa()),
                            () -> itens.add(ItemDaListaJpa.deDominio(novo)));
        }
    }

    ListaPersonalizada paraDominio() {
        return ListaPersonalizada.reconstituir(id, donoId, titulo, descricao, itens.stream()
                .sorted(Comparator.comparing(ItemDaListaJpa::incluidoEm))
                .map(ItemDaListaJpa::paraDominio)
                .toList(), criadaEm, atualizadaEm);
    }
}
