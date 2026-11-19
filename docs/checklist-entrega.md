# Checklist da entrega final

Atualizado em 18/11/2026, na branch `chore/entrega-final`. Legenda: ✅ cumprido · 🟡 parcial · ❌ falta ·
`TODO(grupo)` depende de pessoas.

## 1. Requisitos do professor

| # | Requisito | Status | Evidência |
|---|---|---|---|
| 1 | Artefatos publicados no GitHub; repositório público ou com acesso para @profsauloaraujo | 🟡 `TODO(grupo)` | Repositório `marinaghoffmann/ProjetoRequisitos`. Conferir a visibilidade ou o convite ao professor. |
| 2 | Java, com Spring Boot, JPA e Angular | ✅ | `backend/pom.xml` (Java 21, Spring Boot 4.1), `frontend/` (Angular 21) |
| 3a | Descrição do domínio com a linguagem onipresente | ✅ | `entregaveis/dominio/descricao_do_dominio.md` e o `.pdf` regerado dele em 18/11 (markdown-it + Chrome headless). Propostas de glossário para P5 e P7 em `pendencias.md` (`TODO(grupo)`). |
| 3b | Mapa de histórias do usuário | ✅ | `entregaveis/mapa/` |
| 3c | Protótipos de alta fidelidade | 🟡 `TODO(grupo)` | `entregaveis/prototipo/prototipo.md`: falta o link **público** `*.figma.site` |
| 3d | Modelo dos subdomínios no Context Mapper (`.cml`) | ✅ | `entregaveis/cml/game_tracker.cml`: 0 erros e 0 avisos no servidor de linguagem do Context Mapper 6.12 (só 5 sugestões informativas de Value Object). Corrigidos 3 repositórios da F-14 declarados fora da entidade raiz. |
| 3e | Cenários de teste BDD | ✅ | 20 `.feature` em `backend/src/test/resources/features/`: 257 cenários, nenhum `@pendente` |
| 3f | Automação dos cenários com Cucumber | ✅ | `backend/src/test/java/.../bdd/` (`PassosXxx`, com os novos `PassosWishlist` e `PassosCurrentlyPlaying`): os 257 cenários executam e passam. Relatório em `backend/target/cucumber-relatorio.html` |
| 4 | DDD preliminar, estratégico, tático e operacional; arquitetura limpa | ✅ | Seção "DDD nos quatro níveis" do README; `ArquiteturaTest` com 7 regras, incluindo a aplicação inteira sem Spring |
| 5 | 4 ou mais padrões de projeto (Iterator, Decorator, Observer, Proxy, Strategy, Template Method) | ✅ 6 de 6 | Os seis em código e testados (seção 3) |
| 6 | Persistência com mapeamento objeto-relacional | ✅ | `infraestrutura/persistencia/**` (`XxxJpa` + adapters), PostgreSQL no `docker-compose.yml` |
| 7 | Camada de apresentação web | ✅ | Angular em `frontend/`, com tela para todas as funcionalidades. Paridade com o protótipo em [front-paridade.md](front-paridade.md) |
| 8 | Declaração de uso de IA e interações | 🟡 `TODO(grupo)` | `entregaveis/ia/declaracao_uso_ia.md` e `entregaveis/ia/interacoes/`: cada integrante anexa as suas |

## 2. Matriz funcionalidade × camada

✅ completo · 🟡 parcial · ❌ ausente. "Cenários" = executados e passando / total no `.feature`.

| ID | Funcionalidade | Resp. | Domínio | Caso de uso | JPA | Controller | Tela | `.feature` | Steps | Cenários |
|---|---|---|---|---|---|---|---|---|---|---|
| F-01 | Catálogo e desenvolvedoras | Caio | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | 13/13 |
| F-02 | Reviews | Lavínia | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | 8/8 |
| F-04 | Listas personalizadas | Lavínia | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | 9/9 |
| F-06 | Wishlist | Caio | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | 6/6 |
| F-07 | Currently Playing | Caio | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | 6/6 |
| F-08 | Favoritos e perfil público | Bruno | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | 19/19 |
| F-10 | Similaridade | Lavínia | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | 10/10 |
| F-11 | Registros de "zerado" | Bruno | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | 15/15 |
| F-12 | Lançamentos futuros | Henrique | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | 22/22 |
| F-13 | Fórum | Henrique | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | 15/15 |
| F-14 | Moderação | Henrique | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | 20/20 |
| F-15 | Journal manual | Bruno | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | 19/19 |
| F-16 | Timeline automática | Davi | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | 7/7 |
| F-17 | Estatísticas | Davi | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | 9/9 |
| F-18 | Anúncios | Caio | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | 10/10 |
| F-19 | Chat e propostas | Marina | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | 25/25 |
| F-20 | Pedido e checkout | Marina | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | 29/29 |
| F-21 | Reputação | Davi | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | 7/7 |
| T02 | Autenticação | grupo | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | 6/6 |

Total: 257 cenários executados e nenhum `@pendente` (cada linha de `Exemplos` conta como um cenário). Dúvidas de regra em [pendencias.md](pendencias.md). O [diagnostico.md](diagnostico.md) é o retrato de partida (17/11) e não é mais atualizado.

## 3. Padrões de projeto

| Padrão | Funcionalidade | Responsável | Em código | Testado |
|---|---|---|---|---|
| Strategy | F-10 `SimilaridadeStrategy` | Lavínia | ✅ | ✅ `SimilaridadeTest`, `f10_similaridade.feature` |
| Strategy | F-20 `EstrategiaDePagamento` | Marina | ✅ | ✅ `f20_pedido_checkout.feature` (RN 5 e 6) |
| Observer | F-16 e F-17, observadores de eventos | Davi | ✅ | ✅ `f16_timeline_automatica.feature`, `f17_estatisticas.feature` |
| Proxy | F-19 `ConversasDoParticipante` | Marina | ✅ | ✅ `ConversasDoParticipanteTest`, `f19_chat_negociacao.feature` (RN 3) |
| Template Method | F-19/F-20 `RotinaDePrazo` | Marina | ✅ | ✅ `RotinaDePrazoTest`, cenários da RN 10 (F-19) e das RN 7 e 9 (F-20) |
| Template Method | F-14 `FluxoAcaoModerativa` | Henrique | ✅ | ✅ `f14_moderacao.feature`, `ModeracaoIntegracaoTest` |
| Decorator | F-15 `SpoilerMascaradoDecorator`; F-14 `ConteudoRemovidoDecorator` | Bruno, Henrique | ✅ | ✅ `JournalRegrasTest`, `ModeracaoDominioTest`, `f15_journal.feature` (RN 3), `f14_moderacao.feature` |
| Iterator | F-13 `IteradorDeRespostas` (respostas encadeadas em profundidade) | `TODO(grupo)` | ✅ | ✅ `IteradorDeRespostasTest`, `f13_forum.feature` ("Respostas encadeadas aparecem na ordem da conversa") |

## 4. Arquitetura e repositório

| Item | Status | Evidência |
|---|---|---|
| Domínio sem framework | ✅ | `ArquiteturaTest.dominioNaoDependeDeFrameworks` |
| Aplicação sem Spring | ✅ | Todos os contextos: `ArquiteturaTest.aplicacaoNaoDependeDoSpring` vale para `..aplicacao..` inteiro. `@CasoDeUso` e `@Transacional` (Java puro) são ligados ao Spring na infraestrutura (`CasosDeUsoConfig`, `TransacaoDosCasosDeUsoConfig`, `OuvintesDeEventosSpring`); o `TransacaoDosCasosDeUsoTest` confere o proxy e a propagação |
| Agregados ligados só por Id | 🟡 | No domínio, ✅. Na persistência, `JogoJpa` ainda usa `@ManyToOne`/`@ManyToMany` para desenvolvedora e gênero (F-01) |
| Sem regra de negócio em controller | ✅ | A validação de imagens do fórum (F-13) foi para `ImagensDoForum`, no domínio |
| `docker compose up --build` sobe banco, API e frontend | ✅ | `docker-compose.yml`, `backend/Dockerfile`, `frontend/Dockerfile`, `frontend/nginx.conf` (testado em 17/11/2026) |
| Segredos fora do Git | 🟡 `TODO(grupo)` | `.env.backup` removido e `.env*` ignorado, mas os valores continuam no histórico: **trocar** a chave JWT e a senha de admin |
| Uploads de usuários fora do Git | ✅ | `backend/uploads/` só com `.gitkeep` |
| Catálogo upstream dos contextos que o referenciam | ✅ | `ArquiteturaTest.catalogoNaoDependeDosContextosQueOReferenciam` (F-01 RN 3 pela porta `VinculosDoJogo`) |
| Builds verdes | ✅ | `./mvnw verify`, `npm test` e `npm run build` |

## 5. Antes de entregar (`TODO(grupo)`)

- [ ] Conferir se o repositório está **público** ou com acesso para **@profsauloaraujo**.
- [ ] **Publicar o Figma Make** e colar o link `*.figma.site` em `entregaveis/prototipo/prototipo.md`.
- [ ] **Trocar os segredos vazados**: gerar outra `APP_SECURITY_JWT_SECRET` (`openssl rand -base64 32`) e outra senha
      de admin em todo ambiente que usou os valores do antigo `.env.backup`.
- [ ] Cada integrante anexa as suas interações de IA em `entregaveis/ia/interacoes/` e completa a declaração.
- [ ] Exportar os slides para `entregaveis/apresentacao/`.
- [ ] Definir o **responsável** pelo Iterator (F-13) nas tabelas de padrões do README e deste checklist.
- [ ] Decidir as pendências **P5** (Steam × IGDB/RAWG), **P7** (imagens no fórum), **P9** (base dos percentuais da F-17) e **P10** (exclusão de desenvolvedora). Há propostas de texto em [pendencias.md](pendencias.md).
- [ ] Exportar as telas do protótipo do Figma Make para `entregaveis/prototipo/telas/` (referência da paridade; ver [front-paridade.md](front-paridade.md)).
