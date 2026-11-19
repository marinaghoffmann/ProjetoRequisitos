package com.g4.game_tracker_backend.infraestrutura.persistencia.moderacao;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;

import com.g4.game_tracker_backend.dominio.compartilhado.Pagina;
import com.g4.game_tracker_backend.dominio.moderacao.DenunciaConteudo;
import com.g4.game_tracker_backend.dominio.moderacao.DenunciaRepositorio;
import com.g4.game_tracker_backend.dominio.moderacao.StatusDenuncia;
import com.g4.game_tracker_backend.dominio.moderacao.TipoConteudo;

@Repository
class DenunciaRepositorioPostgres implements DenunciaRepositorio {

    private final DenunciaRepository springData;

    DenunciaRepositorioPostgres(DenunciaRepository springData) {
        this.springData = springData;
    }

    @Override
    public DenunciaConteudo salvar(DenunciaConteudo denuncia) {
        DenunciaJpa jpa = springData.findById(denuncia.id()).orElse(null);
        if (jpa == null) {
            jpa = DenunciaJpa.deDominio(denuncia);
        } else {
            jpa.atualizar(denuncia);
        }
        return springData.save(jpa).paraDominio();
    }

    @Override
    public Optional<DenunciaConteudo> buscarPorId(UUID id) {
        return springData.findById(id).map(DenunciaJpa::paraDominio);
    }

    @Override
    public Optional<DenunciaConteudo> buscarPendente(UUID denuncianteId, TipoConteudo tipo, UUID conteudoId) {
        return springData.findByDenuncianteIdAndTipoAndConteudoIdAndStatus(denuncianteId, tipo, conteudoId,
                StatusDenuncia.PENDENTE).map(DenunciaJpa::paraDominio);
    }

    @Override
    public List<DenunciaConteudo> listarPendentesDoAlvo(TipoConteudo tipo, UUID conteudoId) {
        return springData.findByTipoAndConteudoIdAndStatus(tipo, conteudoId, StatusDenuncia.PENDENTE).stream()
                .map(DenunciaJpa::paraDominio).toList();
    }

    @Override
    public Pagina<DenunciaConteudo> filaDePendentes(int pagina, int tamanho) {
        Page<DenunciaJpa> resultado = springData.findByStatusOrderByRegistradaEmAsc(StatusDenuncia.PENDENTE,
                PageRequest.of(pagina - 1, tamanho));
        return new Pagina<>(resultado.getContent().stream().map(DenunciaJpa::paraDominio).toList(), pagina,
                tamanho, resultado.getTotalElements());
    }
}
