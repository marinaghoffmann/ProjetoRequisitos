package com.g4.game_tracker_backend.moderacao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.g4.game_tracker_backend.dominio.compartilhado.ConteudoOriginal;
import com.g4.game_tracker_backend.dominio.compartilhado.RegraDeNegocioException;
import com.g4.game_tracker_backend.dominio.moderacao.Advertencia;
import com.g4.game_tracker_backend.dominio.moderacao.ConteudoRemovidoDecorator;
import com.g4.game_tracker_backend.dominio.moderacao.DenunciaConteudo;
import com.g4.game_tracker_backend.dominio.moderacao.EstadoConteudo;
import com.g4.game_tracker_backend.dominio.moderacao.StatusDenuncia;
import com.g4.game_tracker_backend.dominio.moderacao.TipoConteudo;

class ModeracaoDominioTest {

    private static final Instant AGORA = Instant.parse("2026-11-18T12:00:00Z");

    @Test
    void denunciaNascePendenteESoPodeSerAnalisadaUmaVez() {
        var denuncia = DenunciaConteudo.registrar(UUID.randomUUID(), TipoConteudo.REVIEW, UUID.randomUUID(),
                "  Ofensivo  ", AGORA);
        assertEquals(StatusDenuncia.PENDENTE, denuncia.status());
        assertEquals("Ofensivo", denuncia.motivo());

        var procedente = denuncia.julgarProcedente(AGORA.plusSeconds(60));
        assertEquals(StatusDenuncia.PROCEDENTE, procedente.status());
        assertEquals(AGORA.plusSeconds(60), procedente.analisadaEm());
        var erro = assertThrows(RegraDeNegocioException.class, () -> procedente.arquivar(AGORA));
        assertEquals("Denúncia já analisada", erro.getMessage());
    }

    @Test
    void denunciaExigeAlvoEMotivoDentroDoLimite() {
        UUID usuario = UUID.randomUUID();
        assertEquals("Tipo e identificador do conteúdo são obrigatórios", assertThrows(RegraDeNegocioException.class,
                () -> DenunciaConteudo.registrar(usuario, null, UUID.randomUUID(), "x", AGORA)).getMessage());
        assertEquals("O motivo é obrigatório", assertThrows(RegraDeNegocioException.class,
                () -> DenunciaConteudo.registrar(usuario, TipoConteudo.TOPICO, UUID.randomUUID(), " ", AGORA))
                .getMessage());
        assertThrows(RegraDeNegocioException.class, () -> DenunciaConteudo.registrar(usuario, TipoConteudo.TOPICO,
                UUID.randomUUID(), "a".repeat(DenunciaConteudo.LIMITE_DO_MOTIVO + 1), AGORA));
    }

    @Test
    void ocultarEReverterAlternamAVisibilidade() {
        var estado = EstadoConteudo.publico(TipoConteudo.RESPOSTA, UUID.randomUUID(), AGORA);
        var oculto = estado.ocultar(AGORA);
        assertFalse(oculto.ativo());
        assertFalse(oculto.visivelPublicamente());
        assertEquals("Conteúdo já está oculto",
                assertThrows(RegraDeNegocioException.class, () -> oculto.ocultar(AGORA)).getMessage());
        assertTrue(oculto.reverterOcultacao(AGORA).visivelPublicamente());
        assertEquals("Conteúdo não está oculto",
                assertThrows(RegraDeNegocioException.class, () -> estado.reverterOcultacao(AGORA)).getMessage());
    }

    @Test
    void topicoRemovidoContinuaNoEnderecoEGuardaOStatusAnterior() {
        var topico = EstadoConteudo.publico(TipoConteudo.TOPICO, UUID.randomUUID(), AGORA).remover("ABERTO", AGORA);
        assertFalse(topico.ativo());
        assertTrue(topico.visivelPublicamente());
        assertEquals("ABERTO", topico.statusAnterior());
        assertNull(topico.reverterRemocao(AGORA).statusAnterior());

        var review = EstadoConteudo.publico(TipoConteudo.REVIEW, UUID.randomUUID(), AGORA).remover("ABERTO", AGORA);
        assertFalse(review.visivelPublicamente());
        assertNull(review.statusAnterior());
    }

    @Test
    void aviso18NaoRestringeALeitura() {
        var marcado = EstadoConteudo.publico(TipoConteudo.PERFIL, UUID.randomUUID(), AGORA).marcar18(AGORA);
        assertTrue(marcado.aviso18());
        assertTrue(marcado.visivelPublicamente());
        assertThrows(RegraDeNegocioException.class, () -> marcado.marcar18(AGORA));
        assertFalse(marcado.retirar18(AGORA).aviso18());
    }

    @Test
    void decoratorTrocaSoOTextoRemovido() {
        assertEquals(ConteudoRemovidoDecorator.MENSAGEM,
                new ConteudoRemovidoDecorator(new ConteudoOriginal("texto"), true).exibir());
        assertEquals("texto", new ConteudoRemovidoDecorator(new ConteudoOriginal("texto"), false).exibir());
    }

    @Test
    void advertenciaExigeMotivo() {
        assertEquals("O motivo é obrigatório", assertThrows(RegraDeNegocioException.class,
                () -> Advertencia.emitir(UUID.randomUUID(), UUID.randomUUID(), "", AGORA)).getMessage());
        assertEquals("Reincidência",
                Advertencia.emitir(UUID.randomUUID(), UUID.randomUUID(), " Reincidência ", AGORA).motivo());
    }
}
