package com.g4.game_tracker_backend.infraestrutura.persistencia.forum;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

import com.g4.game_tracker_backend.dominio.forum.ResumoTopico;
import com.g4.game_tracker_backend.dominio.forum.StatusTopico;
import com.g4.game_tracker_backend.dominio.forum.TopicoDeForum;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

/**
 * Tópico com as respostas. O contador e a data da última atividade são gravados no próprio tópico
 * para a listagem paginada não precisar carregar as respostas.
 */
@Entity
@Table(name = "forum_topicos", indexes = {
        @Index(name = "ix_forum_topicos_jogo", columnList = "jogo_id"),
        @Index(name = "ix_forum_topicos_atividade", columnList = "data_ultima_atividade")
})
public class TopicoForumJpa {

    @Id
    private UUID id;

    @Column(name = "autor_id", nullable = false)
    private UUID autorId;

    @Column(name = "jogo_id")
    private UUID jogoId;

    @Column(nullable = false, length = 60)
    private String categoria;

    @Column(nullable = false, length = 150)
    private String titulo;

    @Column(nullable = false, length = 10000)
    private String conteudo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private StatusTopico status;

    @Column(nullable = false)
    private boolean removido;

    @Column(name = "total_respostas", nullable = false)
    private int totalRespostas;

    @Column(name = "publicado_em", nullable = false)
    private Instant publicadoEm;

    @Column(name = "editado_em")
    private Instant editadoEm;

    @Column(name = "data_ultima_atividade", nullable = false)
    private Instant dataUltimaAtividade;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "topico_id", nullable = false)
    private List<RespostaForumJpa> respostas = new ArrayList<>();

    protected TopicoForumJpa() {
    }

    private TopicoForumJpa(TopicoDeForum topico) {
        this.id = topico.id();
        this.autorId = topico.autorId();
        this.jogoId = topico.jogoId();
        this.categoria = topico.categoria();
        this.titulo = topico.titulo();
        this.conteudo = topico.conteudo();
        this.status = topico.status();
        this.removido = topico.removido();
        this.totalRespostas = topico.totalRespostas();
        this.publicadoEm = topico.publicadoEm();
        this.editadoEm = topico.editadoEm();
        this.dataUltimaAtividade = topico.dataUltimaAtividade();
        this.respostas = new ArrayList<>(topico.respostas().stream().map(RespostaForumJpa::deDominio).toList());
    }

    static TopicoForumJpa deDominio(TopicoDeForum topico) {
        return new TopicoForumJpa(topico);
    }

    /** Exige as respostas carregadas (consulta com join fetch). */
    TopicoDeForum paraDominio() {
        return TopicoDeForum.reconstituir(id, autorId, jogoId, categoria, titulo, conteudo, status, removido,
                publicadoEm, editadoEm, dataUltimaAtividade, respostas.stream()
                        .sorted(Comparator.comparing(RespostaForumJpa::publicadaEm))
                        .map(RespostaForumJpa::paraDominio)
                        .toList());
    }

    ResumoTopico paraResumo() {
        return new ResumoTopico(id, autorId, jogoId, categoria, titulo, status, totalRespostas, dataUltimaAtividade,
                publicadoEm);
    }
}
