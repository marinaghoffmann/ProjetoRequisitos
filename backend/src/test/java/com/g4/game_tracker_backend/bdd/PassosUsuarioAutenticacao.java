package com.g4.game_tracker_backend.bdd;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.UUID;

import org.springframework.security.core.context.SecurityContextHolder;

import com.g4.game_tracker_backend.aplicacao.identidade.AlterarPapelUsuario;
import com.g4.game_tracker_backend.aplicacao.identidade.AutenticarUsuario;
import com.g4.game_tracker_backend.aplicacao.identidade.ProvisionarAdministradorInicial;
import com.g4.game_tracker_backend.aplicacao.identidade.RegistrarUsuario;
import com.g4.game_tracker_backend.aplicacao.identidade.SessaoAutenticada;
import com.g4.game_tracker_backend.aplicacao.identidade.VerificadorDeAcesso;
import com.g4.game_tracker_backend.dominio.identidade.Papel;
import com.g4.game_tracker_backend.dominio.identidade.Usuario;
import com.g4.game_tracker_backend.dominio.identidade.UsuarioRepositorio;

import io.cucumber.java.After;
import io.cucumber.java.pt.Dado;
import io.cucumber.java.pt.Entao;
import io.cucumber.java.pt.Quando;

/** Cenários de cadastro, login e autorização da T02. */
public class PassosUsuarioAutenticacao {

    private static final String SENHA = "senha-segura-123";

    private final RegistrarUsuario registrarUsuario;
    private final AutenticarUsuario autenticarUsuario;
    private final ProvisionarAdministradorInicial provisionarAdministrador;
    private final AlterarPapelUsuario alterarPapelUsuario;
    private final VerificadorDeAcesso verificadorDeAcesso;
    private final UsuarioRepositorio usuarios;
    private final ContextoDoCenario contexto;

    private Usuario contaAtual;
    private Usuario contaAlvo;
    private Usuario contaCadastrada;
    private SessaoAutenticada sessao;

    public PassosUsuarioAutenticacao(RegistrarUsuario registrarUsuario, AutenticarUsuario autenticarUsuario,
            ProvisionarAdministradorInicial provisionarAdministrador, AlterarPapelUsuario alterarPapelUsuario,
            VerificadorDeAcesso verificadorDeAcesso, UsuarioRepositorio usuarios, ContextoDoCenario contexto) {
        this.registrarUsuario = registrarUsuario;
        this.autenticarUsuario = autenticarUsuario;
        this.provisionarAdministrador = provisionarAdministrador;
        this.alterarPapelUsuario = alterarPapelUsuario;
        this.verificadorDeAcesso = verificadorDeAcesso;
        this.usuarios = usuarios;
        this.contexto = contexto;
    }

    @Quando("cadastro um usuário com nome {string} e senha {string}")
    public void cadastroUsuario(String nome, String senha) {
        contaCadastrada = contexto.obter(() -> registrarUsuario.executar(nome, novoEmail(), senha));
    }

    @Dado("que existe uma conta comum cadastrada")
    public void existeContaComum() {
        contaAlvo = criarContaComum("Conta alvo");
    }

    @Quando("faço login com a senha correta")
    public void facoLoginComSenhaCorreta() {
        sessao = contexto.obter(() -> autenticarUsuario.executar(contaAlvo.email(), SENHA));
    }

    @Quando("faço login com uma senha incorreta")
    public void facoLoginComSenhaIncorreta() {
        contexto.obter(() -> autenticarUsuario.executar(contaAlvo.email(), "senha-errada"));
    }

    @Dado("que estou autenticado como usuário comum")
    public void estouAutenticadoComoUsuarioComum() {
        contaAtual = criarContaComum("Jogador comum");
        autenticarComo(contaAtual);
    }

    @Dado("que estou autenticado como administrador")
    public void estouAutenticadoComoAdministrador() {
        String email = novoEmail();
        provisionarAdministrador.executar("Admin BDD", email, SENHA);
        contaAtual = usuarios.buscarPorEmail(email).orElseThrow();
        autenticarComo(contaAtual);
    }

    @Quando("tento alterar meu papel para {string}")
    public void tentoAlterarMeuPapel(String papel) {
        contexto.obter(() -> alterarPapelUsuario.executar(contaAtual.id(), Papel.valueOf(papel)));
    }

    @Quando("o administrador altera o papel dessa conta para {string}")
    public void administradorAlteraPapel(String papel) {
        contexto.obter(() -> alterarPapelUsuario.executar(contaAlvo.id(), Papel.valueOf(papel)));
    }

    @Quando("tento acessar um recurso pertencente a outra conta")
    public void tentoAcessarRecursoDeOutraConta() {
        contaAlvo = criarContaComum("Outro jogador");
        contexto.executar(() -> verificadorDeAcesso.exigirDono(contaAlvo.id()));
    }

    @Entao("o papel da conta cadastrada deve ser {string}")
    public void papelDaContaCadastrada(String papel) {
        assertNotNull(contaCadastrada);
        assertEquals(Papel.valueOf(papel), contaCadastrada.papel());
    }

    @Entao("a sessão deve conter um token Bearer")
    public void sessaoDeveConterTokenBearer() {
        assertNotNull(sessao);
        assertNotNull(sessao.token().valor());
        assertEquals(Papel.COMUM, sessao.usuario().papel());
    }

    @Entao("o papel persistido da conta deve ser {string}")
    public void papelPersistidoDaConta(String papel) {
        Usuario atualizado = usuarios.buscarPorId(contaAlvo.id()).orElseThrow();
        assertEquals(Papel.valueOf(papel), atualizado.papel());
    }

    @After
    public void limparAutenticacaoDoCenario() {
        SecurityContextHolder.clearContext();
    }

    private Usuario criarContaComum(String nome) {
        return registrarUsuario.executar(nome, novoEmail(), SENHA);
    }

    private String novoEmail() {
        return UUID.randomUUID() + "@bdd.example";
    }

    private void autenticarComo(Usuario usuario) {
        SessaoDeTeste.autenticarComo(usuario);
    }
}
