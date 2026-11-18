# Marina Hoffmann · Claude Code · 17/11/2026

| | |
|---|---|
| Integrante | Marina Hoffmann |
| Ferramenta | Claude Code (Anthropic), no VS Code |
| Data | 17/11/2026 |
| Branch | `chore/elevar-entrega` |
| Conversa completa | `TODO(grupo)`: exportar com `/export` no Claude Code e salvar ao lado deste arquivo como `marina-claude-code-2026-11-17.txt` |

## O que foi pedido

Um roteiro em fases para "elevar e organizar o projeto para a entrega final", usando o PetCollar como
referência: diagnóstico, higiene do repositório, arquitetura limpa e DDD, funcionalidades pendentes,
padrões de projeto, apresentação web e entregáveis.

No meio da sessão, Marina restringiu o escopo: **só as funcionalidades dela (F-19 e F-20)** podem receber código.
Das funcionalidades dos colegas, a IA só levantaria o que falta, para Marina repassar ao grupo. As tarefas
que não são funcionalidade (repositório, Docker, documentação, CML, entregáveis) foram liberadas.

## O que foi feito e aproveitado

| Fase | Resultado | Commits |
|---|---|---|
| 0. Diagnóstico | `docs/diagnostico.md`: builds, matriz funcionalidade × camada, desvios de arquitetura, CML × código, padrões e entregáveis. Também um resumo do que falta por integrante, para o grupo. | `docs: adiciona o diagnóstico da entrega` |
| 1. Repositório | `.env.backup` (com segredos) fora do Git; `.env.example` documentado; uploads de teste fora do Git; `docker compose up --build` subindo banco, backend e frontend (testado). | `chore(seguranca)…`, `chore: tira os uploads…`, `chore(docker)…` |
| 2. Arquitetura (F-19/F-20) | Portas `ExecutorDeTransacao` e `PublicadorDeEventos`; aplicação da negociação e dos pedidos sem Spring, com wiring em `NegociacaoConfig`/`PedidosConfig`; nova regra no ArchUnit. | `refactor(arquitetura)…`, `refactor(f19,f20)…` |
| 4. Padrões | Proxy de proteção `ConversasDoParticipante` (F-19 RN 3) e Template Method `RotinaDePrazo` (F-19 RN 10, F-20 RN 7 e 9), com testes. | `refactor(f19,f20)…` |
| 2/6. CML | Validado com o parser do Context Mapper: de 14 erros para 0. Context Map completado. | `docs(cml)…` |
| 6. Entregáveis | Estrutura de `entregaveis/` (histórias, protótipo, IA, apresentação); glossário alinhado ao código do marketplace; README novo; `docs/api.md`, `docs/pendencias.md`, `docs/checklist-entrega.md`. | `docs: …` |

## Como foi revisado

- Cada fase terminou com `./mvnw verify` verde (JUnit, ArchUnit e Cucumber). Os 51 cenários da F-19 e da F-20
  continuaram passando sem nenhuma alteração nos `.feature` nem nos steps.
- `npm test` e `npm run build` verdes no frontend.
- O Docker foi testado de ponta a ponta: frontend, rota do Angular, API e login pelo Nginx.
- A IA não criou regras de negócio. A única divergência de regra na F-20 (tolerância de 7 dias contada do envio, e
  não da entrega) ficou registrada em `docs/pendencias.md` (P1).
- `TODO(grupo)`: Marina revisa o diff da branch antes de abrir o Pull Request.
