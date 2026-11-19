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
| ContextoModeracao | — | F-14 Moderação e denúncias | 11 | Henrique | ❌ |
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
| **Template Method** | Três rotinas de prazo repetiam o mesmo laço (buscar os vencidos, uma transação por item, registrar a falha sem parar as outras) | [`RotinaDePrazo`](backend/src/main/java/com/g4/game_tracker_backend/aplicacao/compartilhado/RotinaDePrazo.java), com as subclasses `ExpirarReservasVencidas`, `CancelarPedidosSemEnvio` e `LiberarCustodiaPorTolerancia` | F-19, F-20 | Marina |
| **Decorator** | Oculta o conteúdo do Journal até o clique explícito do leitor | `IConteudoExibivel`, `ConteudoOriginal`, `SpoilerMascaradoDecorator` | F-15 (F12 do grupo); moderação ainda prevista | Bruno |
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
