package com.g4.game_tracker_backend.infraestrutura.persistencia.lancamentos;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.g4.game_tracker_backend.dominio.lancamentos.*;
interface InteresseSpringData extends JpaRepository<InteresseJpa,UUID> {
    Optional<InteresseJpa> findByUsuarioIdAndJogoId(UUID usuarioId,UUID jogoId);
    List<InteresseJpa> findByJogoIdAndAtivoTrue(UUID jogoId);
    List<InteresseJpa> findByJogoId(UUID jogoId);
}
@Repository
public class InteresseRepositorioJpa implements InteresseLancamentoRepositorio {
    private final InteresseSpringData dados;
    public InteresseRepositorioJpa(InteresseSpringData dados) { this.dados=dados; }
    public InteresseEmLancamento salvar(InteresseEmLancamento i) { dados.save(new InteresseJpa(i)); return i; }
    public Optional<InteresseEmLancamento> buscar(UUID u, UUID j) {
        return dados.findByUsuarioIdAndJogoId(u,j).map(InteresseJpa::dominio);
    }
    public Optional<InteresseEmLancamento> buscarPorId(UUID id) { return dados.findById(id).map(InteresseJpa::dominio); }
    public List<InteresseEmLancamento> ativosPorJogo(UUID j) {
        return dados.findByJogoIdAndAtivoTrue(j).stream().map(InteresseJpa::dominio).toList();
    }
    public List<InteresseEmLancamento> todosPorJogo(UUID j) {
        return dados.findByJogoId(j).stream().map(InteresseJpa::dominio).toList();
    }
    public void excluirPorJogo(UUID j) { dados.deleteAll(dados.findByJogoId(j)); }
}
