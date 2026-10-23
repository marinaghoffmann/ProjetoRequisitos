# Como contribuir

## Rodando o projeto

| O quê | Comando | Onde |
|---|---|---|
| Banco PostgreSQL | `docker compose up -d` | raiz |
| Backend (Postgres) | `./mvnw spring-boot:run` | `backend/` |
| Backend sem Docker (H2 em memória) | `./mvnw spring-boot:run -Dspring-boot.run.profiles=h2` | `backend/` |
| Frontend | `npm ci` e depois `npm start` | `frontend/` |
| Todos os testes do backend (JUnit, ArchUnit e Cucumber) | `./mvnw verify` | `backend/` |
| Testes do frontend | `npm test` | `frontend/` |

O backend sobe em `http://localhost:8080` e o frontend em `http://localhost:4200`. Em desenvolvimento,
o Angular repassa tudo que começa com `/api` para o backend (`frontend/proxy.conf.json`).
Requisitos: Java 21, Node 20.19+ ou 22.12+.

## Backend: onde cada coisa fica

Pacote base `com.g4.game_tracker_backend`, dividido em quatro camadas. Dentro de cada camada há um
subpacote por contexto delimitado do modelo CML (`catalogo`, `avaliacao`, `biblioteca`,
`marketplace.negociacao`...).

| Camada | Contém | Pode depender de |
|---|---|---|
| `dominio` | Entidades, objetos de valor, regras de negócio, eventos e **interfaces** de repositório. Java puro. | nada |
| `aplicacao` | Casos de uso (um serviço por fluxo), transações | `dominio` |
| `infraestrutura` | Entidades JPA, repositórios Spring Data, mapeadores, clientes de API externa, configurações | `dominio`, `aplicacao` |
| `apresentacao` | Controllers REST (`/api/...`) e DTOs (records) | `aplicacao`, `dominio` |

Essas regras são checadas pelo `ArquiteturaTest`: se você importar JPA no domínio, ou um repositório
num controller, o build quebra.

Exemplo para uma funcionalidade do contexto de negociação:

```
dominio/marketplace/negociacao/ConversaChat.java              agregado com as RNs
dominio/marketplace/negociacao/ConversaChatRepositorio.java   interface (porta)
aplicacao/marketplace/negociacao/IniciarConversa.java         caso de uso
infraestrutura/persistencia/negociacao/ConversaChatJpa.java   @Entity
infraestrutura/persistencia/negociacao/ConversaChatRepositorioJpa.java  implementa a porta
apresentacao/marketplace/negociacao/ConversaController.java  POST /api/conversas
```

Regras violadas viram `RegraDeNegocioException("mensagem")`. O `TratadorGlobalDeErros` devolve 422
com a mensagem no campo `detail`, e o frontend mostra esse texto. Também existem
`RecursoNaoEncontradoException` (404) e `AcessoNegadoException` (403).

## Cenários BDD (Cucumber)

1. Os cenários ficam em `backend/src/test/resources/features/`, em português (`# language: pt`).
2. Funcionalidades com a tag `@pendente` não rodam. Ao começar a sua, **remova a tag** do topo do arquivo.
3. Crie os steps em `backend/src/test/java/com/g4/game_tracker_backend/bdd/`, uma classe por funcionalidade
   (`PassosChatNegociacao`, `PassosReviews`...). Use `PassosInfraestrutura` como modelo.
4. Nos steps `Quando`, chame o caso de uso por `contexto.executar(...)` ou `contexto.obter(...)`. Assim a
   exceção de regra violada é guardada e o step comum confere a mensagem.
5. O step "`o/a <algo> deve ser recusado(a) com a mensagem "..."`" já existe em `PassosComuns` e cobre
   todas as variações escritas nos arquivos. Não o reimplemente.
6. Não crie outra classe com `@CucumberContextConfiguration`; só pode existir a `ConfiguracaoSpringCucumber`.

Relatório HTML após `./mvnw test`: `backend/target/cucumber-relatorio.html`.

## Frontend: onde cada coisa fica

```
src/app/core/api/        um serviço por recurso da API (HttpClient)
src/app/core/http/       interceptor que converte erros da API em ErroApi { status, titulo, mensagem }
src/app/shared/          componentes reutilizados por várias features
src/app/features/<ctx>/  páginas (*.page.ts) e rotas (<ctx>.routes.ts) de cada contexto
```

## Git

- `main` sempre compila e passa nos testes. Ninguém commita direto nela.
- Uma branch por task: `feat/f16-chat-negociacao`, `fix/f02-media-categorias`, `docs/cml-marketplace`,
  `chore/t01-esqueleto`.
- Commits no formato `tipo(escopo): descrição`, por exemplo `feat(f16): bloqueia conversa duplicada no anúncio`.
- Abra um Pull Request para a `main` e peça revisão de pelo menos 1 colega. Antes de abrir, rode
  `./mvnw verify` e `npm test`.
