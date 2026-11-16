package com.g4.game_tracker_backend.infraestrutura.persistencia.notificacao;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.g4.game_tracker_backend.dominio.notificacao.*;
interface NotificacaoSpringData extends JpaRepository<NotificacaoJpa,UUID> {
    List<NotificacaoJpa> findByDestinatarioIdOrderByGeradaEmDesc(UUID usuarioId);
    boolean existsByContextoOrigem(String origem);
}
@Repository
public class NotificacaoRepositorioJpa implements NotificacaoRepositorio {
    private final NotificacaoSpringData dados;
    public NotificacaoRepositorioJpa(NotificacaoSpringData dados) { this.dados=dados; }
    public Notificacao salvar(Notificacao n) { dados.save(new NotificacaoJpa(n)); return n; }
    public Optional<Notificacao> buscar(UUID id) { return dados.findById(id).map(NotificacaoJpa::dominio); }
    public List<Notificacao> listarPorUsuario(UUID id) {
        return dados.findByDestinatarioIdOrderByGeradaEmDesc(id).stream().map(NotificacaoJpa::dominio).toList();
    }
    public boolean existePorOrigem(String origem) { return dados.existsByContextoOrigem(origem); }
}
