# Diagnóstico do Game Tracker (Fase 0)

> Levantamento feito em 17/11/2026 sobre o commit `e875693` (igual à `origin/main`), antes de qualquer
> alteração de código. Referência de comparação: projeto PetCollar (`CLAUDE.md`, `Checklist.md`,
> `pom.xml`, `docker-compose.yml`, `README.md`).

## 1. Builds de partida

| Comando | Resultado |
|---|---|
| `./mvnw verify` (backend) | **BUILD SUCCESS**. 305 testes, 0 falhas, 0 erros. |
| Cucumber (`ExecutarCenariosBddTest`) | 211 cenários: **157 executados e passando**, **54 pulados** por `@pendente`. |
| ArchUnit (`ArquiteturaTest`) | 4 regras, todas verdes. |
| `npm test` (frontend, Vitest) | 5 arquivos, 10 testes, todos passando. |
| `npm run build` (frontend) | Sucesso, com 5 avisos de orçamento de SCSS (`inicio`, `perfil`, `detalhe-jogo`, `marketplace`, `app`). |

Observação: a máquina usada roda JDK 25; o `pom.xml` mira Java 21 e compila sem problemas.

## 2. Numeração oficial das funcionalidades

O arquivo `entregaveis/historias_game_tracker.txt` define **18 funcionalidades**: F-01, F-02, F-04, F-06,
F-07, F-08, F-10 a F-21. **F-03, F-05 e F-09 não existem** (foram renumeradas em T-xx ou descartadas).
T02 (autenticação) e o `f00_infraestrutura.feature` são cenários técnicos.

Divergências encontradas:
- O commit `0f1dab6` chama de `feat(f8)` os registros de "zerado", mas pela numeração oficial isso é a
  **F-11** (o `.feature` e o README já usam F-11). A F-08 é "favoritos e perfil público".
- O README usa "T03" e "T07" para catálogo e contrato do anúncio; a numeração oficial é F-01 e F-18.

### Responsáveis (numeração interna do grupo → numeração oficial)

| Grupo | Oficial | Funcionalidade | Responsável |
|---|---|---|---|
| 1 | F-01 | Catálogo e desenvolvedoras | Caio |
| 2 | F-02 | Reviews | Lavínia |
| 3 | F-04 | Listas personalizadas | Lavínia |
| 4 | F-06 | Wishlist | Caio |
| 5 | F-07 | Currently Playing | Caio |
| 6 | F-08 | Favoritos e perfil público | Bruno |
| 7 | F-10 | Similaridade | Lavínia |
| 8 | F-11 | Registros de "zerado" | Bruno |
| 9 | F-12 | Lançamentos futuros | Henrique |
| 10 | F-13 | Fórum | Henrique |
| 11 | F-14 | Moderação | Henrique |
| 12 | F-15 | Journal manual | Bruno |
| 13 | F-16 | Timeline automática | Davi |
| 14 | F-17 | Estatísticas | Davi |
| 15 | F-18 | Anúncios | Caio |
| 16 | F-19 | Chat e propostas | Marina |
| 17 | F-20 | Pedido e checkout | Marina |
| 18 | F-21 | Reputação | Davi |

A numeração "do grupo" (1 a 18) explica o commit `feat(f8)`: é a funcionalidade 8 do grupo, a F-11 oficial.

## 3. Matriz funcionalidade × camada

Legenda: ✅ completo · 🟡 parcial · ❌ ausente. "Cenários" = executados/total no `.feature`.

| ID | Funcionalidade | Domínio | Caso de uso | JPA | Controller | Tela Angular | `.feature` | Steps | Cenários |
|---|---|---|---|---|---|---|---|---|---|
| F-01 | Catálogo, importação, desenvolvedoras | ✅ | ✅ | ✅ | ✅ | 🟡 (sem tela de perfil da desenvolvedora; rotas `catalogo/novo` e `:id/editar` sem guarda de papel) | ✅ | ✅ `PassosCatalogo` | 11/13 (RN 3 exclusão lógica `@pendente`) |
| F-02 | Reviews e notas por categoria | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | 8/8 |
| F-04 | Listas personalizadas | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | 9/9 |
| F-06 | Wishlist | ❌ (só a porta `FonteDeWishlists` da F-17) | ❌ | ❌ | ❌ | ❌ | ✅ | ❌ | 0/6 |
| F-07 | Currently Playing | 🟡 `JogandoAgora` guarda só a data (RN 3 pede data **e hora**) | 🟡 mensagem de recusa diferente do `.feature`; sem consulta de perfil público | ✅ | ✅ | ✅ `jogando-agora` | ✅ | ❌ | 0/6 |
| F-08 | Favoritos e perfil público | 🟡 bio, avatar, banner em `Usuario`; **sem favoritos (slots 1–4)** nem limite de 500 caracteres conferido | 🟡 `EditarPerfil`, `AtualizarImagemPerfil` | 🟡 | 🟡 | 🟡 `perfil`, `editar-perfil` (sem favoritos, sem perfil público de outro usuário) | ✅ | ❌ | 0/9 |
| F-10 | Similaridade e feedback | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | 10/10 |
| F-11 | Registros de "zerado" | ✅ | ✅ | ✅ | ✅ | ✅ `zerados` | ✅ | ✅ | 13/13 |
| F-12 | Lançamentos futuros | ❌ | ❌ | ❌ | ❌ | ❌ | ✅ | ❌ | 0/9 |
| F-13 | Fórum | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | 10/10 |
| F-14 | Moderação e denúncias | ❌ | ❌ | ❌ | ❌ | ❌ | ✅ | ❌ | 0/9 |
| F-15 | Journal manual | 🟡 `EntradaDeJournal.criarManual/editar` existem | ❌ | 🟡 (tabela já existe) | ❌ (só `GET /api/journal/timeline`) | 🟡 (timeline só exibe) | ✅ | ❌ | 0/9 |
| F-16 | Timeline automática | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | 5/5 |
| F-17 | Estatísticas | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | 8/8 |
| F-18 | Anúncios | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | 8/8 |
| F-19 | Chat e propostas | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | 25/25 |
| F-20 | Pedido e checkout | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | 26/26 |
| F-21 | Reputação | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | 7/7 |
| T02 | Autenticação | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | 6/6 |

Achados adicionais em funcionalidades marcadas como completas:
- **F-01, RN 3:** `DesativarJogo` sempre faz exclusão lógica, sem verificar vínculos. A RN pede exclusão
  definitiva quando não há reviews, journal nem listas. É isso que os 2 cenários `@pendente` cobrem.
- **F-08, RN 3:** `Usuario.BIO_MAXIMA = 160`, mas a RN e o `.feature` pedem **500** caracteres.

Resumo: **13 de 18 funcionalidades completas**. Faltam F-06, F-12 e F-14 inteiras (contextos novos, sem
nenhuma linha de código), F-15 e F-08 em boa parte, e acertos na F-07 e na RN 3 da F-01.

Todos os `.feature` já têm 3 cenários ou mais, exceto `f00_infraestrutura` (2, técnico).

## 4. Arquitetura: o que o ArchUnit já garante

- Domínio sem Spring, JPA ou Hibernate (zero ocorrências) e sem depender das outras camadas.
- Aplicação sem depender de infraestrutura, apresentação ou JPA.
- Apresentação sem acesso à infraestrutura (nenhum controller importa repositório).

## 5. Violações que o ArchUnit **não** pega

| # | Achado | Onde | Gravidade |
|---|---|---|---|
| A1 | **Camada de aplicação depende do Spring**: 63 `@Service`, 13 `@Component`, 58 `@Transactional`, 9 `ApplicationEventPublisher`, `@EventListener`/`@TransactionalEventListener`, `ObjectProvider`, `TransactionTemplate`. São 93 arquivos em `aplicacao`. | `aplicacao/**` | Média (o PetCollar exige aplicação sem framework) |
| A2 | **`@ManyToOne` e `@ManyToMany` cruzando agregados**: `JogoJpa` → `DesenvolvedoraJpa` e `JogoJpa` ↔ `GeneroJpa`. No domínio, `Jogo` já guarda `desenvolvedoraId` e `generosIds` por Id; o desvio está só na persistência. | `infraestrutura/persistencia/catalogo/JogoJpa.java:60-68` | Média |
| A3 | **Regra de negócio no controller**: limite de 5 MB e validação de tipo de imagem por *magic bytes* do fórum. | `apresentacao/forum/ImagensDoForumController.java:44-72` | Média |
| A4 | Validação de entrada no controller/DTO (aceitável, mas lança `RegraDeNegocioException` de dentro de DTO). | `apresentacao/marketplace/pedidos/PedidoRequest.java:19`, `apresentacao/identidade/UsuarioController.java:110` | Baixa |
| A5 | Enums persistidos com `@Enumerated(EnumType.STRING)` em 12 entidades JPA (o PetCollar grava `name()` como `String`). O efeito no banco é o mesmo. | `infraestrutura/persistencia/**` | Baixa, só convenção |
| A6 | Alguns DTOs de saída usam enums do domínio como tipo de campo (ex.: `ProgressoJogatina` em `JogandoAgoraController`). Nenhum DTO expõe entidade ou agregado. | `apresentacao/**` | Baixa |
| A7 | Arquivo `JogandoAgoraRepositorio.java` na infraestrutura contém a classe `JogandoAgoraRepositorioJpa` (nome do arquivo ≠ nome da classe, e igual ao da porta do domínio). | `infraestrutura/persistencia/biblioteca/` | Baixa |

Pontos verificados **sem** desvio:
- Nenhum setter público em classes do domínio. As entidades são imutáveis (métodos de negócio devolvem nova instância) e usam `reconstituir(...)` como fábrica de reconstituição.
- Agregados se referenciam só por `UUID` no domínio.
- `@OneToMany` só dentro do próprio agregado (`ListaPersonalizada`→itens, `TopicoDeForum`→respostas, `ConversaChat`→mensagens/propostas, `Review`→notas).
- Nenhum `@GeneratedValue`: o Id nasce no domínio.

### Convenções diferentes do PetCollar, mas consistentes no projeto inteiro (manter)

- Identidade por `UUID` direto, não por VO `XxxId` com `gerar()`/`de()`. Usado em todos os 18+ agregados.
  Trocar seria reescrever domínio, JPA, DTOs e steps de todos os integrantes.
- Repositórios `XxxRepositorio` (sem prefixo `I`) e adapters `XxxRepositorioPostgres`.
- Reconstituição por fábrica estática `reconstituir(...)` em vez de construtor comentado.
- Erros de regra como `RegraDeNegocioException` → 422 com `ProblemDetail` (melhor que 409 genérico).

## 6. DDD estratégico: CML × código

O `game_tracker.cml` tem 18 bounded contexts. Correspondência com os pacotes:

| BoundedContext (CML) | Pacote no código | Situação |
|---|---|---|
| ContextoIdentidadeUsuario | `identidade` | ✅ |
| ContextoCatalogoJogos | `catalogo` | ✅ |
| ContextoLancamentos | — | ❌ sem código (F-12) |
| ContextoAvaliacao | `avaliacao.reviews` | ✅ |
| ContextoEstatisticas | `avaliacao.estatisticas` | ✅ |
| ContextoListasPersonalizadas | `biblioteca.listas` | ✅ |
| ContextoWishlist | — | ❌ sem código (F-06) |
| ContextoPerfilJogador | `biblioteca` (`JogandoAgora`) + `identidade` (bio/capa) | 🟡 nome e lugar divergem |
| ContextoConclusoes | `biblioteca.conclusoes` | ✅ |
| ContextoSimilaridade | `descoberta.similaridade` | ✅ |
| ContextoDiarioJogatina | `journal` | ✅ |
| ContextoForum | `forum` | ✅ |
| ContextoModeracao | — | ❌ sem código (F-14) |
| ContextoAnuncios | `marketplace.anuncios` | ✅ |
| ContextoNegociacao | `marketplace.negociacao` | ✅ |
| ContextoPedidos | `marketplace.pedidos` | ✅ |
| ContextoReputacao | `marketplace.reputacao` | ✅ |
| ContextoNotificacao | — | ❌ sem código e sem funcionalidade dona |

O código também tem `status` (saúde do sistema, técnico) e `compartilhado` (shared kernel: exceções,
`Pagina`), que não aparecem no CML.

Divergências no conteúdo do CML:
- Descreve um **Template Method** `CadastroJogoTemplate` e um **Proxy** `ProxyBaseExternaJogos` /
  `IgdbGateway` para a F-01, mas **nenhum dos dois existe** no código. A importação real usa a Steam
  (`SteamImportador`, `ImportarJogoSteam`).
- Relação `ContextoPedidos -> ContextoEstatisticas`: correta. A conclusão do pedido marca o anúncio como
  VENDIDO e o `ObservadorEstatisticaJogo` recalcula o volume de negociações pelo `AnuncioStatusAlterado`.
  (Na primeira leitura deste diagnóstico ela foi apontada, por engano, como inexistente.)
- `ContextoCatalogoJogos -> ContextoSimilaridade` como ACL está correto (`PerfilJogoGateway` /
  `PerfilJogoDoCatalogo`).
- Validado com o parser do Context Mapper 6.12 (o da extensão do VS Code): **14 erros** de sintaxe e de
  referência. O arquivo não abria sem erros. Corrigido na Fase 2: 0 erros, 0 avisos.

## 7. Padrões de projeto

| Padrão | Situação | Onde |
|---|---|---|
| Strategy | ✅ código | `SimilaridadeStrategy` (+3 implementações, F-10); `EstrategiaDePagamento` (+3, F-20) |
| Observer | ✅ código, mas acoplado ao Spring | `ObservadorEventosPlataforma` (F-16) e `ObservadorEstatisticaJogo` (F-17) via `@EventListener` |
| Template Method | ❌ só no CML | `CadastroJogoTemplate` (F-01) |
| Proxy | ❌ só no CML | proxy da base externa (F-01) |
| Decorator | ❌ | — |
| Iterator | ❌ (existe `Pagina`, mas é um record de lista) | — |

Hoje: **2 padrões distintos** em código. O professor exige 4; a meta do checklist é 6.

## 8. Entregáveis e repositório

| Item | Situação |
|---|---|
| Descrição do domínio (`.md` + `.pdf`) | ✅ existe; falta conferir glossário × código (Fase 6) |
| Mapa de histórias (`.png` + `.pdf`) | ✅ |
| Protótipo | 🟡 só link de **edição** do Figma Make; falta o link público `*.figma.site` |
| CML | ✅ (com as divergências da seção 6) |
| Cenários BDD + automação Cucumber | 🟡 54 cenários pulados |
| Declaração/interações de IA | ❌ não existe |
| Slides de apresentação | ❌ não existe pasta |
| `.env.backup` versionado | ❌ **contém `APP_SECURITY_JWT_SECRET` e senha de admin reais** |
| `.env.example` | ❌ vazio |
| `backend/uploads/` | ❌ 24 imagens versionadas (forum, perfil). São uploads de teste manual, **não** usados por seed. |
| `docker-compose.yml` | 🟡 só Postgres; sem healthcheck, backend e frontend |
| README | 🟡 bom detalhamento por funcionalidade, mas sem seções de arquitetura, DDD por nível, padrões, contas, integrantes |
| `pom.xml` | 🟡 e-mails placeholder (`email@exemplo.com`) para Davi, Henrique e Caio |

Commits por autor no histórico: Marina 38, Lavínia 39, Davi 24, Caio 28 (duas identidades), Bruno 3,
**Henrique 0**. Vale conferir se a identidade de Git do Henrique está configurada, porque o checklist
pede commits identificáveis por integrante.
