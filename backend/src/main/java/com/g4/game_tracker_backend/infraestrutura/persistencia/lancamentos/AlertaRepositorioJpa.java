package com.g4.game_tracker_backend.infraestrutura.persistencia.lancamentos;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.g4.game_tracker_backend.dominio.lancamentos.*;
interface AlertaSpringData extends JpaRepository<AlertaJpa,UUID> {
    Optional<AlertaJpa> findFirstByInteresseIdAndEnviadaFalse(UUID interesseId);
    List<AlertaJpa> findByEnviadaFalseAndDispararEmLessThanEqual(Instant agora);
    List<AlertaJpa> findByInteresseIdAndEnviadaFalse(UUID interesseId);
    List<AlertaJpa> findByInteresseId(UUID interesseId);
}
@Repository
public class AlertaRepositorioJpa implements AlertaLancamentoRepositorio {
    private final AlertaSpringData dados;
    public AlertaRepositorioJpa(AlertaSpringData dados) { this.dados=dados; }
    public NotificacaoLancamento salvar(NotificacaoLancamento a) { dados.save(new AlertaJpa(a)); return a; }
    public Optional<NotificacaoLancamento> buscar(UUID id) { return dados.findById(id).map(AlertaJpa::dominio); }
    public Optional<NotificacaoLancamento> pendentePorInteresse(UUID id) {
        return dados.findFirstByInteresseIdAndEnviadaFalse(id).map(AlertaJpa::dominio);
    }
    public List<NotificacaoLancamento> vencidos(Instant agora) {
        return dados.findByEnviadaFalseAndDispararEmLessThanEqual(agora).stream().map(AlertaJpa::dominio).toList();
    }
    public void cancelarPendentes(UUID id) { dados.deleteAll(dados.findByInteresseIdAndEnviadaFalse(id)); }
    public void excluirPorInteresse(UUID id) { dados.deleteAll(dados.findByInteresseId(id)); }
}
