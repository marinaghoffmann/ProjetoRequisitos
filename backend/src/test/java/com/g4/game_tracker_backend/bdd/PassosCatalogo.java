package com.g4.game_tracker_backend.bdd;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import com.g4.game_tracker_backend.aplicacao.catalogo.AtualizarDesenvolvedora;
import com.g4.game_tracker_backend.aplicacao.catalogo.AtualizarJogoManual;
import com.g4.game_tracker_backend.aplicacao.catalogo.CadastrarJogoManual;
import com.g4.game_tracker_backend.aplicacao.catalogo.ConsultarCatalogo;
import com.g4.game_tracker_backend.aplicacao.catalogo.DesativarJogo;
import com.g4.game_tracker_backend.aplicacao.catalogo.ImportarJogo;
import com.g4.game_tracker_backend.aplicacao.identidade.CodificadorDeSenha;
import com.g4.game_tracker_backend.dominio.catalogo.Desenvolvedora;
import com.g4.game_tracker_backend.dominio.catalogo.DesenvolvedoraRepositorio;
import com.g4.game_tracker_backend.dominio.catalogo.FiltroCatalogo;
import com.g4.game_tracker_backend.dominio.catalogo.Jogo;
import com.g4.game_tracker_backend.dominio.catalogo.JogoRepositorio;
import com.g4.game_tracker_backend.dominio.catalogo.OrigemJogo;
import com.g4.game_tracker_backend.dominio.identidade.Papel;
import com.g4.game_tracker_backend.dominio.identidade.Usuario;
import com.g4.game_tracker_backend.dominio.identidade.UsuarioRepositorio;

import io.cucumber.java.pt.Dado;
import io.cucumber.java.pt.Entao;
import io.cucumber.java.pt.Quando;

/**
 * Steps da F-01 (catálogo). O banco H2 é compartilhado entre os cenários, por isso os dados
 * de apoio são buscados pelo nome antes de serem criados.
 */
public class PassosCatalogo {

    private static final String ESTUDIO_DE_APOIO = "Estúdio BDD";

    private final ImportarJogo importarJogo;
    private final CadastrarJogoManual cadastrarJogoManual;
    private final AtualizarJogoManual atualizarJogoManual;
    private final AtualizarDesenvolvedora atualizarDesenvolvedora;
    private final DesativarJogo desativarJogo;
    private final ConsultarCatalogo consultarCatalogo;
    private final JogoRepositorio jogos;
    private final DesenvolvedoraRepositorio desenvolvedoras;
    private final UsuarioRepositorio usuarios;
    private final CodificadorDeSenha codificadorDeSenha;
    private final ContextoDoCenario contexto;

    private Usuario usuarioAtual;
    private Jogo jogoAtual;
    private UUID desenvolvedoraDoFiltro;
    private List<Jogo> resultadoDaConsulta;

    public PassosCatalogo(ImportarJogo importarJogo, CadastrarJogoManual cadastrarJogoManual,
            AtualizarJogoManual atualizarJogoManual, AtualizarDesenvolvedora atualizarDesenvolvedora,
            DesativarJogo desativarJogo, ConsultarCatalogo consultarCatalogo, JogoRepositorio jogos,
            DesenvolvedoraRepositorio desenvolvedoras, UsuarioRepositorio usuarios,
            CodificadorDeSenha codificadorDeSenha, ContextoDoCenario contexto) {
        this.importarJogo = importarJogo;
        this.cadastrarJogoManual = cadastrarJogoManual;
        this.atualizarJogoManual = atualizarJogoManual;
        this.atualizarDesenvolvedora = atualizarDesenvolvedora;
        this.desativarJogo = desativarJogo;
        this.consultarCatalogo = consultarCatalogo;
        this.jogos = jogos;
        this.desenvolvedoras = desenvolvedoras;
        this.usuarios = usuarios;
        this.codificadorDeSenha = codificadorDeSenha;
        this.contexto = contexto;
    }

    // ---- Usuários e desenvolvedoras ----

    @Dado("um usuário com papel {string}")
    public void umUsuarioComPapel(String papel) {
        usuarioAtual = autenticarNovoUsuario(Papel.valueOf(papel), null);
    }

    @Dado("um usuário com papel {string} vinculado à desenvolvedora ativa {string}")
    public void usuarioVinculadoADesenvolvedoraAtiva(String papel, String nome) {
        usuarioAtual = autenticarNovoUsuario(Papel.valueOf(papel), desenvolvedora(nome, true).id());
    }

    @Dado("um usuário com papel {string} vinculado à desenvolvedora inativa {string}")
    public void usuarioVinculadoADesenvolvedoraInativa(String papel, String nome) {
        usuarioAtual = autenticarNovoUsuario(Papel.valueOf(papel), desenvolvedora(nome, false).id());
    }

    @Quando("o desenvolvedor altera a biografia para {string} e o país para {string}")
    public void desenvolvedorAlteraBiografia(String biografia, String pais) {
        Desenvolvedora atual = desenvolvedoras.buscarPorId(usuarioAtual.desenvolvedoraId()).orElseThrow();
        contexto.obter(() -> atualizarDesenvolvedora.executar(atual.id(), biografia, atual.site(),
                atual.redesSociais(), pais));
    }

    @Entao("o perfil da desenvolvedora {string} deve exibir a biografia {string}")
    public void perfilDeveExibirBiografia(String nome, String biografia) {
        assertEquals(biografia, desenvolvedoras.buscarPorNome(nome).orElseThrow().biografia());
    }

    // ---- RN 1: importação e duplicidade ----

    @Dado("que não existe jogo com identificador externo {string} no catálogo")
    public void naoExisteJogoComIdentificador(String identificador) {
        assertTrue(jogos.buscarPorIdentificadorExterno(identificador).isEmpty(),
                "O identificador " + identificador + " já foi usado por outro cenário");
    }

    @Dado("que existe o jogo {string} com identificador externo {string} no catálogo")
    public void existeJogoComIdentificador(String titulo, String identificador) {
        importarComoAdministrador(titulo, identificador);
    }

    @Quando("o sistema importa o jogo {string} com identificador externo {string}")
    public void sistemaImportaJogo(String titulo, String identificador) {
        jogoAtual = contexto.obter(() -> importarComoAdministrador(titulo, identificador));
    }

    @Entao("o catálogo deve conter {int} jogo com identificador externo {string}")
    public void catalogoDeveConterJogoComIdentificador(int quantidade, String identificador) {
        long encontrados = consultarCatalogo.executar(new FiltroCatalogo(null, null, null, null, null)).stream()
                .filter(jogo -> identificador.equals(jogo.identificadorExterno()))
                .count();
        assertEquals(quantidade, encontrados);
    }

    @Entao("a origem do jogo deve ser {string}")
    public void origemDoJogoDeveSer(String origem) {
        assertNotNull(jogoAtual, () -> "A ação foi recusada: " + contexto.erro().getMessage());
        assertEquals(OrigemJogo.valueOf(origem), jogoAtual.origem());
    }

    @Entao("o título do jogo deve ser {string}")
    public void tituloDoJogoDeveSer(String titulo) {
        assertEquals(titulo, jogoAtual.titulo());
    }

    // ---- RN 1 e RN 2: cadastro manual ----

    @Dado("que a desenvolvedora {string} já possui o jogo {string}")
    public void desenvolvedoraJaPossuiJogo(String nomeDesenvolvedora, String titulo) {
        Desenvolvedora estudio = desenvolvedora(nomeDesenvolvedora, true);
        if (!jogos.existePorTituloEDesenvolvedora(titulo, estudio.id())) {
            jogos.salvar(Jogo.criarManual(titulo, null, null, estudio.id(), Set.of(), null, null, null));
        }
    }

    @Quando("o desenvolvedor cadastra manualmente o jogo {string}")
    public void desenvolvedorCadastraJogo(String titulo) {
        cadastrarManualmente(titulo);
    }

    @Quando("o usuário cadastra manualmente o jogo {string}")
    public void usuarioCadastraJogo(String titulo) {
        cadastrarManualmente(titulo);
    }

    @Entao("o jogo {string} deve estar vinculado à desenvolvedora {string}")
    public void jogoDeveEstarVinculado(String titulo, String nomeDesenvolvedora) {
        UUID esperada = desenvolvedoras.buscarPorNome(nomeDesenvolvedora).orElseThrow().id();
        assertNotNull(jogoAtual, () -> "O cadastro foi recusado: " + contexto.erro().getMessage());
        assertEquals(titulo, jogoAtual.titulo());
        assertEquals(esperada, jogoAtual.desenvolvedoraId());
    }

    // ---- RN 3 e edição pelo administrador ----

    @Dado("que existe o jogo {string} no catálogo")
    public void existeJogoNoCatalogo(String titulo) {
        jogoAtual = jogoDeApoio(titulo);
    }

    @Quando("o administrador remove o jogo {string}")
    public void administradorRemoveJogo(String titulo) {
        contexto.obter(() -> desativarJogo.executar(jogoDeApoio(titulo).id()));
    }

    @Quando("o usuário tenta remover o jogo {string}")
    public void usuarioTentaRemoverJogo(String titulo) {
        contexto.obter(() -> desativarJogo.executar(jogoDeApoio(titulo).id()));
    }

    @Entao("o jogo {string} deve continuar no banco com status {string}")
    public void jogoDeveContinuarComStatus(String titulo, String status) {
        Jogo persistido = jogos.buscarPorId(jogoDeApoio(titulo).id()).orElseThrow();
        assertEquals(status, persistido.status().name());
    }

    @Quando("o administrador altera o resumo do jogo {string} para {string}")
    public void administradorAlteraResumo(String titulo, String resumo) {
        Jogo atual = jogoDeApoio(titulo);
        contexto.obter(() -> atualizarJogoManual.executar(atual.id(), atual.titulo(), atual.tituloOriginal(),
                resumo, null, atual.generosIds(), atual.dataLancamento(), atual.capaUrl(), atual.trailerUrl(),
                atual.metadadosJson()));
    }

    @Entao("o jogo {string} deve ter o resumo {string}")
    public void jogoDeveTerResumo(String titulo, String resumo) {
        assertEquals(resumo, jogoDeApoio(titulo).resumo());
    }

    // ---- Consulta ----

    @Dado("que o catálogo possui {int} jogos de origem {string} e {int} de origem {string}")
    public void catalogoPossuiJogosPorOrigem(int qtdA, String origemA, int qtdB, String origemB) {
        // Desenvolvedora exclusiva do cenário, para não contar jogos criados por outros cenários.
        Desenvolvedora estudio = desenvolvedoras.salvar(Desenvolvedora.criar("Filtro " + UUID.randomUUID(),
                null, null, null, null));
        desenvolvedoraDoFiltro = estudio.id();
        criarJogos(estudio.id(), OrigemJogo.valueOf(origemA), qtdA);
        criarJogos(estudio.id(), OrigemJogo.valueOf(origemB), qtdB);
    }

    @Quando("o usuário filtra o catálogo pela origem {string}")
    public void usuarioFiltraPorOrigem(String origem) {
        resultadoDaConsulta = contexto.obter(() -> consultarCatalogo.executar(
                new FiltroCatalogo(null, null, desenvolvedoraDoFiltro, OrigemJogo.valueOf(origem), null)));
    }

    @Entao("a consulta deve retornar {int} jogos")
    public void consultaDeveRetornar(int quantidade) {
        assertEquals(quantidade, resultadoDaConsulta.size());
    }

    // ---- Apoio ----

    private void cadastrarManualmente(String titulo) {
        jogoAtual = contexto.obter(() -> cadastrarJogoManual.executar(titulo, null, "Cadastrado no BDD",
                Set.of(), LocalDate.of(2026, 1, 1), null, null, null));
    }

    private Jogo importarComoAdministrador(String titulo, String identificador) {
        Usuario anterior = usuarioAtual;
        autenticarNovoUsuario(Papel.ADMINISTRADOR, null);
        try {
            return importarJogo.executar(titulo, null, null, identificador,
                    desenvolvedora(ESTUDIO_DE_APOIO, true).id(), Set.of(), null, null, null);
        } finally {
            if (anterior != null) {
                SessaoDeTeste.autenticarComo(anterior);
            }
        }
    }

    private Jogo jogoDeApoio(String titulo) {
        UUID estudio = desenvolvedora(ESTUDIO_DE_APOIO, true).id();
        return consultarCatalogo.executar(new FiltroCatalogo(titulo, null, estudio, null, null)).stream()
                .filter(jogo -> jogo.titulo().equalsIgnoreCase(titulo))
                .findFirst()
                .orElseGet(() -> jogos.salvar(Jogo.criarManual(titulo, null, null, estudio, Set.of(), null,
                        null, null)));
    }

    private void criarJogos(UUID desenvolvedoraId, OrigemJogo origem, int quantidade) {
        for (int i = 1; i <= quantidade; i++) {
            String titulo = origem + " " + i;
            jogos.salvar(origem == OrigemJogo.API
                    ? Jogo.importar(titulo, null, null, "BDD-" + UUID.randomUUID(), desenvolvedoraId, Set.of(),
                            null, null, null)
                    : Jogo.criarManual(titulo, null, null, desenvolvedoraId, Set.of(), null, null, null));
        }
    }

    private Desenvolvedora desenvolvedora(String nome, boolean ativa) {
        Desenvolvedora estudio = desenvolvedoras.buscarPorNome(nome)
                .orElseGet(() -> desenvolvedoras.salvar(Desenvolvedora.criar(nome, null, null, null, null)));
        if (estudio.ativa() != ativa) {
            estudio = desenvolvedoras.salvar(ativa
                    ? Desenvolvedora.reconstituir(estudio.id(), estudio.nome(), estudio.biografia(), estudio.site(),
                            estudio.redesSociais(), estudio.pais(), true)
                    : estudio.desativar());
        }
        return estudio;
    }

    private Usuario autenticarNovoUsuario(Papel papel, UUID desenvolvedoraId) {
        Usuario usuario = usuarios.salvar(Usuario.reconstituir(UUID.randomUUID(), "Usuário BDD",
                UUID.randomUUID() + "@bdd.example", codificadorDeSenha.codificar("senha-segura-123"), papel, true,
                desenvolvedoraId));
        SessaoDeTeste.autenticarComo(usuario);
        return usuario;
    }
}
