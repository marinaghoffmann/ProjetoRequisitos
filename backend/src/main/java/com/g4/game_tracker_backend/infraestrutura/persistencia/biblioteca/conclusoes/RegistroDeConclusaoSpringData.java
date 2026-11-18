package com.g4.game_tracker_backend.infraestrutura.persistencia.biblioteca.conclusoes;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.g4.game_tracker_backend.dominio.biblioteca.conclusoes.ResumoJogoZerado;
import com.g4.game_tracker_backend.dominio.biblioteca.conclusoes.RegistroZeradoNaBiblioteca;

import jakarta.persistence.LockModeType;

interface RegistroDeConclusaoSpringData extends JpaRepository<RegistroDeConclusaoJpa, UUID> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<RegistroDeConclusaoJpa> findRegistroById(UUID id);

    List<RegistroDeConclusaoJpa> findByJogadorIdAndJogoIdOrderByDataConclusaoDescRegistradoEmDescIdDesc(
            UUID jogadorId, UUID jogoId);

    int countByJogoId(UUID jogoId);

    @Query("""
            select new com.g4.game_tracker_backend.dominio.biblioteca.conclusoes.ResumoJogoZerado(
                jogo.id, jogo.titulo, count(registro.id), max(registro.dataConclusao))
            from RegistroDeConclusaoJpa registro
            join JogoJpa jogo on jogo.id = registro.jogoId
            where registro.jogadorId = :jogadorId
            group by jogo.id, jogo.titulo
            order by max(registro.dataConclusao) desc, jogo.titulo, jogo.id
            """)
    List<ResumoJogoZerado> resumirPorJogador(@Param("jogadorId") UUID jogadorId);

    @Query("""
            select new com.g4.game_tracker_backend.dominio.biblioteca.conclusoes.RegistroZeradoNaBiblioteca(
                registro.id, jogo.id, jogo.titulo, jogo.capaUrl, registro.dataConclusao,
                registro.plataforma, registro.horasJogadas, registro.concluido, registro.platina)
            from RegistroDeConclusaoJpa registro
            join JogoJpa jogo on jogo.id = registro.jogoId
            where registro.jogadorId = :jogadorId
            order by registro.dataConclusao desc, registro.registradoEm desc, registro.id desc
            """)
    List<RegistroZeradoNaBiblioteca> detalharPorJogador(@Param("jogadorId") UUID jogadorId);
}
