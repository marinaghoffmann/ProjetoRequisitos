# Game Tracker

Sistema web no estilo "Letterboxd de jogos": catálogo, reviews por categoria, biblioteca pessoal, journal de
jogatina, fórum moderado e um marketplace entre jogadores com negociação, pagamento em custódia e reputação.

Projeto em grupo da disciplina de Requisitos, Projeto de Software e Validação da CESAR School. Foi construído
com **Domain-Driven Design** nos quatro níveis e **arquitetura limpa**, com **Java 21 + Spring Boot** no backend,
**Angular** no frontend, **JPA + PostgreSQL** na persistência e **BDD com Cucumber** em português.

- [Entregáveis](#entregáveis)
- [Funcionalidades por contexto](#funcionalidades-por-contexto)
- [Como rodar](#como-rodar)
- [Arquitetura](#arquitetura)
- [DDD nos quatro níveis](#ddd-nos-quatro-níveis)
- [Padrões de projeto](#padrões-de-projeto)
- [Testes e BDD](#testes-e-bdd)
- [Integrantes](#integrantes)

Endpoints e regras de cada funcionalidade: [docs/api.md](docs/api.md). Como contribuir (onde fica cada classe, steps
do Cucumber, branches e commits): [CONTRIBUTING.md](CONTRIBUTING.md). Situação da entrega: [docs/checklist-entrega.md](docs/checklist-entrega.md).

## Entregáveis

| Requisito do professor | Artefato |
|---|---|
| Descrição do domínio com a linguagem onipresente | [entregaveis/dominio/descricao_do_dominio.md](entregaveis/dominio/descricao_do_dominio.md) (e `.pdf`) |
| Mapa de histórias do usuário | [entregaveis/mapa/](entregaveis/mapa/) (`.png` e `.pdf`) |
| Histórias e regras de negócio | [entregaveis/historias/historias_game_tracker.txt](entregaveis/historias/historias_game_tracker.txt) |
| Protótipos de alta fidelidade | [entregaveis/prototipo/prototipo.md](entregaveis/prototipo/prototipo.md) |
| Modelo dos subdomínios (Context Mapper) | [entregaveis/cml/game_tracker.cml](entregaveis/cml/game_tracker.cml) |
| Cenários de teste BDD | [backend/src/test/resources/features/](backend/src/test/resources/features/) |
| Automação dos cenários com Cucumber | [backend/src/test/java/com/g4/game_tracker_backend/bdd/](backend/src/test/java/com/g4/game_tracker_backend/bdd/) |
| Declaração e interações de IA | [entregaveis/ia/](entregaveis/ia/) |
| Contrato do anúncio do marketplace | [entregaveis/contratos/contrato_anuncio.md](entregaveis/contratos/contrato_anuncio.md) |
| Slides da apresentação | [entregaveis/apresentacao/](entregaveis/apresentacao/) |

## Funcionalidades por contexto

A numeração é a oficial, a de `entregaveis/historias/`. F-03, F-05 e F-09 não existem. O grupo também usa uma
numeração interna de 1 a 18, mostrada na coluna "Nº grupo".

| Bounded context (CML) | Pacote (`com.g4.game_tracker_backend.<camada>.…`) | Funcionalidade | Nº grupo | Responsável | Situação |
|---|---|---|---|---|---|
| ContextoIdentidadeUsuario | `identidade` | T02 Cadastro, login (JWT) e papéis | — | grupo | ✅ |
| ContextoCatalogoJogos | `catalogo` | F-01 Catálogo, importação e desenvolvedoras | 1 | Caio | 🟡 RN 3 pendente |
| ContextoLancamentos | `lancamentos` | F-12 Lançamentos futuros e alertas | 9 | Henrique | ✅ |
| ContextoAvaliacao | `avaliacao.reviews` | F-02 Reviews e notas por categoria | 2 | Lavínia | ✅ |
| ContextoEstatisticas | `avaliacao.estatisticas` | F-17 Estatísticas da comunidade | 14 | Davi | ✅ |
| ContextoListasPersonalizadas | `biblioteca.listas` | F-04 Listas personalizadas | 3 | Lavínia | ✅ |
| ContextoWishlist | — | F-06 Wishlist | 4 | Caio | ❌ |
| ContextoPerfilJogador | `biblioteca` e `identidade` | F-07 Currently Playing | 5 | Caio | 🟡 sem BDD |
| ContextoPerfilJogador | `biblioteca.perfil` e `identidade` | F-08 Favoritos e perfil público | 6 | Bruno | ✅ |
| ContextoConclusoes | `biblioteca.conclusoes` | F-11 Registros de "zerado" | 8 | Bruno | ✅ |
| ContextoSimilaridade | `descoberta.similaridade` | F-10 Similares e feedback | 7 | Lavínia | ✅ |
| ContextoDiarioJogatina | `journal` | F-15 Journal manual | 12 | Bruno | ✅ |
| ContextoDiarioJogatina | `journal` | F-16 Timeline automática | 13 | Davi | ✅ |
| ContextoForum | `forum` | F-13 Fórum | 10 | Henrique | ✅ |
| ContextoModeracao | `moderacao` | F-14 Moderação e denúncias | 11 | Henrique | ✅ |
| ContextoAnuncios | `marketplace.anuncios` | F-18 Anúncios | 15 | Caio | ✅ |
| ContextoNegociacao | `marketplace.negociacao` | F-19 Chat e propostas | 16 | Marina | ✅ |
| ContextoPedidos | `marketplace.pedidos` | F-20 Pedido, checkout e entrega | 17 | Marina | ✅ |
| ContextoReputacao | `marketplace.reputacao` | F-21 Reputação | 18 | Davi | ✅ |
| ContextoNotificacao | `notificacao` | Notificações internas (apoio da F-12 e da F-14) | — | Henrique | ✅ usada pela F-12 |

`compartilhado` é o shared kernel: exceções de domínio, `Pagina` e a porta `PublicadorDeEventos`. `status` é técnico
(saúde da API). O detalhe por camada está na matriz de [docs/checklist-entrega.md](docs/checklist-entrega.md).

## Como rodar

Requisitos: Docker. Para rodar fora do Docker: Java 21 e Node 20.19+ ou 22.12+.

### Com Docker (tudo com um comando)

```bash
cp .env.example .env
# no .env: defina POSTGRES_PASSWORD e APP_SECURITY_JWT_SECRET (openssl rand -base64 32)
# e, se quiser um administrador, APP_BOOTSTRAP_ADMIN_EMAIL e APP_BOOTSTRAP_ADMIN_PASSWORD
docker compose up --build
```

| Serviço | Endereço |
|---|---|
| Frontend (Nginx, com proxy de `/api` para o backend) | http://localhost:4200 |
| API | http://localhost:8080/api/status |
| PostgreSQL | `localhost:5432` |

`docker compose down` derruba tudo e `docker compose down -v` apaga também o banco e os uploads.

### Local, sem Docker (banco H2 em memória)

```bash
cd backend && ./mvnw spring-boot:run -Dspring-boot.run.profiles=h2
cd frontend && npm ci && npm start          # em outro terminal
```

Frontend em http://localhost:4200 (o `ng serve` repassa `/api` para o backend). Para usar o Postgres em vez do H2:
`docker compose up -d postgres` e `./mvnw spring-boot:run`, com `APP_SECURITY_JWT_SECRET` exportada.

### Contas de teste

| Perfil | Conta | Senha | Papel |
|---|---|---|---|
| `h2` | `admin@gametracker.local` | `admin12345` | ADMINISTRADOR |
| `h2` | `dev@gametracker.local` | `dev12345` | DESENVOLVEDOR, vinculado à "Umbra Interactive" |
| Docker / Postgres | o que estiver em `APP_BOOTSTRAP_ADMIN_EMAIL` / `APP_BOOTSTRAP_ADMIN_PASSWORD` | | ADMINISTRADOR |

Jogadores comuns se cadastram pela tela `/cadastro`. O catálogo já sobe com gêneros, desenvolvedoras e jogos de
exemplo (`CargaInicialCatalogo`).

## Arquitetura

O backend é um módulo Maven organizado em **camadas** e, dentro de cada camada, em **contextos delimitados**:
`com.g4.game_tracker_backend.<camada>.<contexto>`.

```
          ┌──────────────────────────────────────────────┐
          │ apresentacao   controllers REST + DTOs record │──┐
          └──────────────────────┬───────────────────────┘  │
                                 ▼                          │
          ┌──────────────────────────────────────────────┐  │
          │ aplicacao      casos de uso                   │  │
          └──────────────────────┬───────────────────────┘  │
                                 ▼                          ▼
          ┌──────────────────────────────────────────────────────┐
          │ dominio        agregados, VOs, eventos, portas         │  Java puro
          └──────────────────────────────────────────────────────┘
                                 ▲
          ┌──────────────────────┴───────────────────────┐
          │ infraestrutura JPA, adapters, Spring Security, │  implementa as portas
          │                @Configuration (wiring)         │  do domínio e da aplicação
          └──────────────────────────────────────────────┘
```

**Regra de dependência:** as setas apontam para dentro. O domínio não conhece Spring, JPA nem as outras camadas;
a aplicação não conhece JPA, controllers nem a infraestrutura; os controllers não acessam repositórios. Quem
garante é o [`ArquiteturaTest`](backend/src/test/java/com/g4/game_tracker_backend/arquitetura/ArquiteturaTest.java)
(ArchUnit), que roda em todo `./mvnw verify`: uma importação proibida quebra o build.

- **Persistência:** cada agregado tem uma interface `XxxRepositorio` no domínio e um adapter
  `XxxRepositorioPostgres` na infraestrutura, que converte entre o agregado e a entidade `XxxJpa`. O Id nasce no
  domínio (`UUID`), sem `@GeneratedValue`. Agregados se referenciam só por Id; `@OneToMany` só dentro do agregado.
- **Erros:** violação de regra vira `RegraDeNegocioException`, e o `TratadorGlobalDeErros` devolve **422** com
  `ProblemDetail` (mensagem em `detail`). `RecursoNaoEncontradoException` vira 404 e `AcessoNegadoException`, 403.
  No frontend, o interceptor `ErroApi` mostra a mensagem ao usuário.
- **Aplicação sem framework (em migração):** a negociação (F-19) e os pedidos (F-20) já usam só portas:
  `ExecutorDeTransacao` para a transação e `PublicadorDeEventos` para os eventos. Os beans são registrados em
  `infraestrutura/configuracao/NegociacaoConfig` e `PedidosConfig`, e o ArchUnit garante esses pacotes livres de
  Spring. Os outros contextos ainda usam `@Service` e `@Transactional`. Para migrar um contexto, siga o mesmo
  modelo e acrescente o pacote à regra `aplicacaoMigradaNaoDependeDoSpring`.

**Por que um módulo Maven só, e não um por camada (como no PetCollar)?** A separação por módulos faria o compilador
barrar dependências proibidas. Aqui quem faz isso é o ArchUnit, a cada build, com a mesma força. Dividir agora
mexeria no código de todos os integrantes perto da entrega, sem ganho funcional.

**Convenções do grupo que diferem do PetCollar:** Ids são `UUID` (não VOs `XxxId`), repositórios não têm o prefixo
`I` e a reconstituição usa a fábrica estática `reconstituir(...)` em vez de um construtor comentado. São usadas de
forma consistente em todos os agregados.

## DDD nos quatro níveis

| Nível | Onde aparece |
|---|---|
| **Preliminar**: linguagem onipresente | Glossário em [descricao_do_dominio.md](entregaveis/dominio/descricao_do_dominio.md). Código, mensagens de erro e cenários em português, com os termos do glossário (`AnuncioMarketplace`, `PropostaNegociacao`, `Custódia`, `RegistroDeConclusao`...). |
| **Estratégico**: subdomínios, contextos e mapa | [game_tracker.cml](entregaveis/cml/game_tracker.cml): 9 subdomínios, 18 bounded contexts e o Context Map (U/D, OHS/PL, ACL e Conformist). Cada contexto vira um pacote (tabela acima). Exemplos de ACL no código: `PerfilJogoGateway` (similaridade sobre o catálogo) e `AnunciosDaNegociacao` (negociação sobre os anúncios). O CML abre sem erros no Context Mapper 6.12. |
| **Tático**: agregados, VOs, eventos, repositórios | `backend/.../dominio/**`: agregados imutáveis com regras nos métodos (`ConversaChat`, `PedidoMarketplace`, `Review`...), objetos de valor (`EnderecoEntrega`, `CartaoSimulado`, `NotaAvaliacao`), eventos de domínio (`CompraConcluida`, `ReviewPublicada`, `AnuncioStatusAlterado`) e repositórios como interfaces. |
| **Operacional**: casos de uso e arquitetura | `backend/.../aplicacao/**`: um caso de uso por fluxo (`GerarPedido`, `IniciarConversa`, `PublicarReview`...), orquestrando o domínio, com as camadas verificadas pelo ArchUnit. |

## Padrões de projeto

| Padrão | Problema que resolve | Classes principais | Funcionalidade | Responsável |
|---|---|---|---|---|
| **Strategy** | Calcular a similaridade por critérios intercambiáveis (gênero, tag, estúdio) e combiná-los | [`SimilaridadeStrategy`](backend/src/main/java/com/g4/game_tracker_backend/dominio/descoberta/similaridade/SimilaridadeStrategy.java) e as implementações `SimilaridadePorGeneroStrategy`, `SimilaridadePorTagStrategy` e `SimilaridadePorEstudioStrategy` | F-10 | Lavínia |
| **Strategy** | Cobrar e reembolsar por forma de pagamento sem `if` por tipo; uma forma nova é um `@Bean` a mais | [`EstrategiaDePagamento`](backend/src/main/java/com/g4/game_tracker_backend/dominio/marketplace/pedidos/EstrategiaDePagamento.java), `PagamentoCartao`, `PagamentoPix`, `PagamentoSaldoCarteira` e `FormasDePagamento` | F-20 | Marina |
| **Observer** | Gerar a timeline do journal e recalcular estatísticas quando outras funcionalidades agem, sem que elas conheçam quem reage | [`ObservadorEventosPlataforma`](backend/src/main/java/com/g4/game_tracker_backend/aplicacao/journal/ObservadorEventosPlataforma.java), [`ObservadorEstatisticaJogo`](backend/src/main/java/com/g4/game_tracker_backend/aplicacao/avaliacao/estatisticas/ObservadorEstatisticaJogo.java) e a porta [`PublicadorDeEventos`](backend/src/main/java/com/g4/game_tracker_backend/dominio/compartilhado/PublicadorDeEventos.java) | F-16, F-17 | Davi |
| **Proxy** (de proteção) | Garantir em um só lugar que a conversa só chega ao comprador e ao vendedor (RN 3), sem repetir a checagem em cada caso de uso | [`ConversasDoParticipante`](backend/src/main/java/com/g4/game_tracker_backend/aplicacao/marketplace/negociacao/ConversasDoParticipante.java), com a mesma interface de `ConversaChatRepositorio` | F-19 | Marina |
| **Template Method** | Toda ação moderativa segue a mesma sequência: validar, aplicar, registrar o log e notificar o autor | [`FluxoAcaoModerativa`](backend/src/main/java/com/g4/game_tracker_backend/aplicacao/moderacao/FluxoAcaoModerativa.java), estendida por `GerenciarModeracao` | F-14 | Henrique |
| **Template Method** | Três rotinas de prazo repetiam o mesmo laço (buscar os vencidos, uma transação por item, registrar a falha sem parar as outras) | [`RotinaDePrazo`](backend/src/main/java/com/g4/game_tracker_backend/aplicacao/compartilhado/RotinaDePrazo.java), com as subclasses `ExpirarReservasVencidas`, `CancelarPedidosSemEnvio` e `LiberarCustodiaPorTolerancia` | F-19, F-20 | Marina |
| **Decorator** | Muda o texto exibido sem mexer na entidade: oculta o spoiler do Journal até o clique do leitor e troca o conteúdo removido pela moderação pela mensagem de sistema | `IConteudoExibivel` e `ConteudoOriginal` (em `dominio.compartilhado`), `SpoilerMascaradoDecorator`, `ConteudoRemovidoDecorator` | F-15 (F12 do grupo), F-14 | Bruno, Henrique |
| **Iterator** | `TODO(grupo)`. O CML prevê a paginação de tópicos e a navegação da timeline | — | F-13 / F-16 | `TODO(grupo)` |

Testes dos padrões: `SimilaridadeTest`, os cenários de pagamento da F-20, `ConversasDoParticipanteTest`,
`RotinaDePrazoTest`, `JournalRegrasTest` e os cenários da F-15, da F-16 e da F-17.

## Testes e BDD

```bash
cd backend && ./mvnw verify      # JUnit, ArchUnit e todos os cenários Cucumber
cd frontend && npm test          # Vitest
```

- Os cenários ficam em [backend/src/test/resources/features/](backend/src/test/resources/features/), um `.feature`
  por funcionalidade, em português (`# language: pt`), com o número da RN em comentário acima de cada grupo.
- Os steps ficam em `backend/src/test/java/com/g4/game_tracker_backend/bdd/` (`PassosXxx`) e sobem o Spring com H2
  (`ConfiguracaoSpringCucumber`), chamando os casos de uso de verdade.
- **Relatório HTML:** `backend/target/cucumber-relatorio.html`, gerado a cada `./mvnw verify`.
- Funcionalidades marcadas com `@pendente` ainda não têm implementação e são puladas. Veja a matriz em
  [docs/checklist-entrega.md](docs/checklist-entrega.md).

## Integrantes

| Integrante | Funcionalidades (numeração oficial) |
|---|---|
| Marina Hoffmann | F-19 Chat e propostas · F-20 Pedido e checkout |
| Lavínia Maranhão | F-02 Reviews · F-04 Listas · F-10 Similaridade |
| Caio Ferreira | F-01 Catálogo · F-06 Wishlist · F-07 Currently Playing · F-18 Anúncios |
| Bruno Mayer | F-08 Favoritos e perfil · F-11 Zerados · F-15 Journal |
| Henrique Brito | F-12 Lançamentos · F-13 Fórum · F-14 Moderação |
| Davi Marques | F-16 Timeline automática · F-17 Estatísticas · F-21 Reputação |

## Lançamentos futuros e alertas (F-12)

Os lançamentos são cadastrados **explicitamente** para jogos ativos do catálogo. Uma data futura
no cadastro do jogo, por si só, não cria uma inscrição na agenda.

| Método | Rota | Objetivo | Quem pode |
|---|---|---|---|
| `GET` | `/api/lancamentos` | Listar futuros por data, com contagem regressiva, total de interessados e alerta do usuário; `incluirLancados=true` acrescenta os já lançados | qualquer um |
| `GET` | `/api/lancamentos/{id}` | Consultar lançamento e histórico de datas | qualquer um |
| `POST` | `/api/lancamentos` | Cadastrar `{ "jogoId": "...", "dataPrevista": "AAAA-MM-DD", "tipoData": "ESTIMADA" }` | admin ou desenvolvedor vinculado ao jogo |
| `PUT` | `/api/lancamentos/{id}` | Alterar `{ "dataPrevista": "AAAA-MM-DD", "tipoData": "CONFIRMADA" }` | admin ou desenvolvedor vinculado ao jogo |
| `DELETE` | `/api/lancamentos/{id}` | Remover a agenda e cancelar alertas, mantendo o jogo | admin ou desenvolvedor vinculado ao jogo |
| `PUT` / `DELETE` | `/api/lancamentos/{id}/interesse` | Ativar/desativar o interesse (operações idempotentes) | autenticado |
| `GET` | `/api/notificacoes` | Caixa de notificações do próprio usuário | autenticado |
| `PATCH` | `/api/notificacoes/{id}/leitura` | Marcar como lida | destinatário |

A data é `LocalDate` no fuso `America/Sao_Paulo`. `tipoData` pode ser `ESTIMADA`
(data possível, ainda sem confirmação) ou `CONFIRMADA`. O formulário inicia em `ESTIMADA`;
a API mantém `CONFIRMADA` como padrão para clientes anteriores. Uma estimativa é exibida
publicamente mesmo após passar, sem mudar automaticamente para `LANCADO` nem criar
alerta. O usuário pode acompanhar o jogo; ao confirmar a data, os alertas dos interessados
são agendados. O alerta confirmado vence à meia-noite do dia anterior (24 horas antes do
início do dia oficial). O campo representa a classificação informada por quem cadastra;
o sistema não verifica a fonte automaticamente. O status `LANCADO` pertence ao
lançamento; o jogo continua `ATIVO` no catálogo. Edições de data nas rotas do catálogo e
importações de jogos já inscritos sincronizam o histórico e reagendam os alertas na mesma
transação. Avisos de data alterada e de lançamento próximo são internos ao aplicativo.

`RotinasDosLancamentos` processa alertas e transições vencidas a cada minuto. Defina
`APP_LANCAMENTOS_ROTINAS=false` para desativá-la; os testes a desativam e invocam os
casos de uso com um instante fixo. Frontend: a seção “Em breve” de `/inicio` mostra os três primeiros lançamentos reais e o link “Todos” abre `/lancamentos`; essa página mostra a agenda e, ao final, os já lançados. Os formulários de cadastro/edição e `/notificacoes` continuam disponíveis. Cenários BDD: `f12_lancamentos.feature`.

## Fórum (F-13)

| Método | Rota | Objetivo | Quem pode |
|---|---|---|---|
| `GET` | `/api/forum/topicos` | Tópicos paginados (`jogoId`, `categoria`, `apenasGerais=true`, `status`, `ordem=recentes|emAlta`, `pagina` a partir de 1, `tamanho` até 50) | qualquer um |
| `GET` | `/api/forum/topicos/{id}` | Tópico com todas as respostas (o encadeamento vem em `respostaPaiId`) | qualquer um |
| `POST` | `/api/forum/topicos` | Criar tópico geral (`jogoId` nulo) ou de um jogo `ATIVO` do catálogo; nasce `ABERTO` | autenticado |
| `PUT` | `/api/forum/topicos/{id}` | Editar título, mensagem e categoria (até 48 horas após a publicação) | autor |
| `PATCH` | `/api/forum/topicos/{id}/status` | Marcar como `RESOLVIDO`, reabrir (`ABERTO`) ou encerrar (`ENCERRADO`) com `{ "status": "..." }` | autor |
| `DELETE` | `/api/forum/topicos/{id}` | Excluir o tópico (exclusão lógica: ele some das consultas) | autor |
| `POST` | `/api/forum/topicos/{id}/respostas` | Responder ao tópico, ou a outra resposta com `respostaPaiId` | autenticado |
| `PUT` | `/api/forum/topicos/{id}/respostas/{respostaId}` | Editar a resposta (até 48 horas após a publicação) | autor |
| `DELETE` | `/api/forum/topicos/{id}/respostas/{respostaId}` | Excluir a resposta | autor |
| `PUT` / `DELETE` | `/api/forum/topicos/{id}/curtida` | Curtir / descurtir o tópico, sem duplicar em requisições repetidas | autenticado |
| `PUT` / `DELETE` | `/api/forum/topicos/{id}/respostas/{respostaId}/curtida` | Curtir / descurtir uma resposta visível | autenticado |

- RN 1: tópico `ENCERRADO` não aceita novas respostas, edições nem exclusões, e não pode ser reaberto pelo autor.
- RN 2: só o autor edita ou exclui a sua postagem (403 "Apenas o autor pode alterar esta postagem"); a edição vale por 48 horas.
- RN 3: criar, editar ou excluir uma resposta atualiza o contador do tópico e a data da última atividade. Os dois ficam
  gravados no próprio tópico (`forum_topicos`), então a listagem não carrega as respostas. Escritas no mesmo tópico
  são serializadas por bloqueio da linha raiz dentro da transação.
- `ordem=recentes` (padrão) usa a última atividade; `ordem=emAlta` usa a soma de respostas ativas e curtidas,
  com última atividade e ID para desempate. `status` aceita `ABERTO`, `RESOLVIDO` ou `ENCERRADO`. A categoria
  `Discussão geral` também inclui tópicos antigos salvos como `Geral`. A resposta da API traz `totalCurtidas`
  e `totalInteracoes` (respostas ativas + curtidas); no detalhe, cada resposta traz sua contagem e o estado
  da curtida do usuário autenticado. Curtidas de respostas removidas pelo autor são apagadas; curtidas de conteúdo moderado saem dos totais públicos e voltam com a reversão.
- `apenasGerais=true` seleciona tópicos sem jogo vinculado, independentemente da categoria. Pode ser combinado
  com `categoria`, mas não com `jogoId`; essa combinação retorna erro de regra de negócio.
- O upload em `/api/forum/imagens` aceita PNG, JPG ou WEBP de até 5 MB. A assinatura e o tamanho são
  validados por `ImagensDoForum` no domínio; `ArmazenarImagemForum` autentica e grava o arquivo.
- Conteúdo de tópicos e respostas é Markdown com negrito, itálico, listas, código e links. As telas oferecem prévia;
  HTML bruto e imagens embutidas via Markdown não são renderizados. Imagens continuam no fluxo de anexos.
- A exclusão de resposta é lógica: ela deixa de contar, volta sem conteúdo (`removida: true`) e continua na árvore
  para as respostas filhas não sumirem. Não é possível responder a uma resposta removida.
- Toda resposta nova publica um `RespostaPublicada` pelo `ApplicationEventPublisher`, até o barramento da T04 existir.
- Frontend: `/forum` é a Comunidade, com editor na própria página, filtros por categoria, status e jogo,
  abas Recentes/Em alta e paginação de 5 tópicos. `/forum/{id}` traz cartões de autor, curtidas e respostas
  encadeadas com editor inline. `/forum/novo` e `/forum/{id}/editar` permanecem para compatibilidade.
  As telas seguem a [referência visual da Comunidade no Figma Make](https://www.figma.com/make/Hbid18iRVvTGOqfX2p4UNW/Game-Tracker-MVP-Development--Copy-?t=zTCKnJrVlLNDiCpq-1).
- Cenários BDD em `f13_forum.feature`, com steps em `PassosForum`. Jogadores e jogos de apoio ficam em `DadosDoCenario`,
  compartilhado entre as classes de steps; o step `o jogador "<nome>" autenticado` agora fica em `PassosComuns`.

A moderação do fórum, as denúncias e os avisos 18+ são descritos na F-14 abaixo. A listagem ainda mostra
  o nome do jogo procurando no catálogo carregado pela tela; não há busca por título do tópico.

## Moderação da plataforma (F-14)

A moderação cobre `TOPICO`, `RESPOSTA`, `REVIEW`, `LISTA`, `AVALIACAO_MARKETPLACE`, `ANUNCIO`, `PERFIL` e `JOURNAL`.
Jogos do catálogo e lançamentos futuros seguem os fluxos de gestão próprios. Denúncias **não** ocultam conteúdo:
a postagem permanece pública até um moderador decidir.

| Método | Rota | Objetivo | Quem pode |
|---|---|---|---|
| `POST` | `/api/denuncias` | Denunciar com `{ "tipoConteudo", "conteudoId", "motivo" }`; repetir enquanto pendente devolve a mesma denúncia | autenticado |
| `GET` | `/api/moderacao/denuncias?pagina=1&tamanho=10` | Fila pendente por data, paginada (até 50 por página) | `MODERADOR` ou `ADMINISTRADOR` |
| `POST` | `/api/moderacao/denuncias/{id}/arquivar` | Marcar uma denúncia improcedente, com `{ "justificativa" }`, e registrar log | `MODERADOR` ou `ADMINISTRADOR` |
| `POST` | `/api/moderacao/acoes` | Aplicar `{ "acao", "tipoConteudo", "conteudoId", "justificativa" }`; edição do fórum também usa `novoTitulo` e `novoConteudo`; movimentação de tópico usa `novaCategoria` e movimentação de resposta usa `novaRespostaPaiId` (nulo para a raiz) | `MODERADOR` ou `ADMINISTRADOR` |
| `GET` | `/api/moderacao/logs?pagina=1&tamanho=10` | Histórico imutável e paginado das ações | `MODERADOR` ou `ADMINISTRADOR` |
| `GET` | `/api/moderacao/conteudos/{tipo}/{id}/previa` | Prévia privada e ID do autor | `MODERADOR` ou `ADMINISTRADOR` |
| `GET` | `/api/moderacao/conteudos/{tipo}/{id}/estado` | Estado do aviso 18+ e moderação para a interface | qualquer um |
| `POST` | `/api/moderacao/advertencias` | Registrar `{ "usuarioId", "motivo" }` sem suspender a conta | `MODERADOR` ou `ADMINISTRADOR` |
| `GET` | `/api/moderacao/usuarios/{id}/advertencias` e `/total` | Advertências e contagem do usuário | `MODERADOR` ou `ADMINISTRADOR` |

Ações aceitas: `OCULTAR`, `EXCLUIR`, `MARCAR_18`, `RETIRAR_18`, `REVERTER_OCULTACAO` e
`REVERTER_EXCLUSAO` para todos os tipos; `EDITAR` para tópicos e respostas; `MOVER` e
`ENCERRAR_TOPICO` só para tópicos; `MOVER` reorganiza a categoria de um tópico ou o pai de uma resposta no mesmo tópico. `OCULTAR` e `EXCLUIR` exigem justificativa. Uma ação procedente
resolve todas as denúncias pendentes do alvo. Cada decisão gera um registro novo em `moderacao_logs` e
notificação interna ao autor; a reversão acrescenta outro registro. A advertência também notifica o usuário.
O `FluxoAcaoModerativa` implementa Template Method: validar permissão e justificativa, aplicar a ação,
registrar o log e notificar o autor, sempre na transação do caso de uso.

A remoção moderativa do tópico mantém seu endereço e as respostas, mostra uma mensagem de sistema e encerra
a conversa. Respostas removidas mantêm as filhas. Conteúdo oculto ou removido não aparece nas consultas
públicas nem em seus agregados; URLs de mídias anexadas são bloqueadas, inclusive as antigas após edição
do autor. Anúncios moderados não aceitam novas conversas, propostas ou compras diretas. Pedidos existentes
continuam registrados. O estado de moderação é separado dos dados editáveis pelo autor.

`MARCAR_18` aplica um aviso visual e desfoque ao texto e às imagens. Qualquer visitante pode revelar o
item na sessão atual, sem verificação de idade e sem restrição de acesso à API. Na interface, “Denunciar”
aparece nas áreas públicas cobertas e `/moderacao` oferece fila, prévia, decisão, histórico e contagem de
advertências somente para moderadores e administradores. Os cenários BDD estão em
`f14_moderacao.feature`, os testes de integração em `ModeracaoIntegracaoTest` e os das regras dos agregados em
`ModeracaoDominioTest`.

Entradas do journal (F-15) oculta ou removida saem do journal público do autor (inclusive da paginação) e
deixam de aceitar edição e de servir imagens. O dono continua vendo a entrada, marcada como moderada; a removida
mostra a mensagem de sistema pelo `ConteudoRemovidoDecorator`.

Organização em camadas: os agregados `DenunciaConteudo` e `EstadoConteudo`, o `LogDeModeracao`, a `Advertencia`
e as portas dos repositórios ficam em `dominio.moderacao`, sem framework. O caso de uso `GerenciarModeracao`
(aplicação) aplica as regras pelos agregados e acessa o conteúdo dos outros contextos pela porta
`ConteudosModeraveis`, implementada em SQL por `ConteudosModeraveisJdbc` (infraestrutura).
