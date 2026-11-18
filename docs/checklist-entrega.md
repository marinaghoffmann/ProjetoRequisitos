# Checklist da entrega final

Atualizado em 17/11/2026, na branch `chore/elevar-entrega`. Legenda: ✅ cumprido · 🟡 parcial · ❌ falta ·
`TODO(grupo)` depende de pessoas.

## 1. Requisitos do professor

| # | Requisito | Status | Evidência |
|---|---|---|---|
| 1 | Artefatos publicados no GitHub; repositório público ou com acesso para @profsauloaraujo | 🟡 `TODO(grupo)` | Repositório `marinaghoffmann/ProjetoRequisitos`. Conferir a visibilidade ou o convite ao professor. |
| 2 | Java, com Spring Boot, JPA e Angular | ✅ | `backend/pom.xml` (Java 21, Spring Boot 4.1), `frontend/` (Angular 21) |
| 3a | Descrição do domínio com a linguagem onipresente | 🟡 | `entregaveis/dominio/descricao_do_dominio.md`, com o glossário revisado contra o código. `TODO(grupo)`: regerar o `.pdf` a partir do `.md`. |
| 3b | Mapa de histórias do usuário | ✅ | `entregaveis/mapa/` |
| 3c | Protótipos de alta fidelidade | 🟡 `TODO(grupo)` | `entregaveis/prototipo/prototipo.md`: falta o link **público** `*.figma.site` |
| 3d | Modelo dos subdomínios no Context Mapper (`.cml`) | ✅ | `entregaveis/cml/game_tracker.cml`: 0 erros e 0 avisos no parser do Context Mapper 6.12 |
| 3e | Cenários de teste BDD | 🟡 | 20 `.feature` em `backend/src/test/resources/features/`; 7 funcionalidades com `@pendente` (seção 2) |
| 3f | Automação dos cenários com Cucumber | 🟡 | `backend/src/test/java/.../bdd/`: 157 de 211 cenários rodando e passando; relatório em `backend/target/cucumber-relatorio.html` |
| 4 | DDD preliminar, estratégico, tático e operacional; arquitetura limpa | ✅ | Seção "DDD nos quatro níveis" do README; `ArquiteturaTest` com 5 regras |
| 5 | 4 ou mais padrões de projeto (Iterator, Decorator, Observer, Proxy, Strategy, Template Method) | ✅ 4 de 6 | Strategy, Observer, Proxy e Template Method em código e testados (seção 3). Meta de 6: faltam Decorator e Iterator. |
| 6 | Persistência com mapeamento objeto-relacional | ✅ | `infraestrutura/persistencia/**` (`XxxJpa` + adapters), PostgreSQL no `docker-compose.yml` |
| 7 | Camada de apresentação web | 🟡 | Angular em `frontend/`; F-06, F-12, F-14 e F-15 ainda sem tela |
| 8 | Declaração de uso de IA e interações | 🟡 `TODO(grupo)` | `entregaveis/ia/declaracao_uso_ia.md` e `entregaveis/ia/interacoes/`: cada integrante anexa as suas |

## 2. Matriz funcionalidade × camada

✅ completo · 🟡 parcial · ❌ ausente. "Cenários" = executados e passando / total no `.feature`.

| ID | Funcionalidade | Resp. | Domínio | Caso de uso | JPA | Controller | Tela | `.feature` | Steps | Cenários |
|---|---|---|---|---|---|---|---|---|---|---|
| F-01 | Catálogo e desenvolvedoras | Caio | ✅ | 🟡 RN 3 | ✅ | ✅ | 🟡 | ✅ | ✅ | 11/13 |
| F-02 | Reviews | Lavínia | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | 8/8 |
| F-04 | Listas personalizadas | Lavínia | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | 9/9 |
| F-06 | Wishlist | Caio | ❌ | ❌ | ❌ | ❌ | ❌ | ✅ | ❌ | 0/6 |
| F-07 | Currently Playing | Caio | 🟡 | 🟡 | ✅ | ✅ | ✅ | ✅ | ❌ | 0/6 |
| F-08 | Favoritos e perfil público | Bruno | 🟡 | 🟡 | 🟡 | 🟡 | 🟡 | ✅ | ❌ | 0/9 |
| F-10 | Similaridade | Lavínia | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | 10/10 |
| F-11 | Registros de "zerado" | Bruno | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | 13/13 |
| F-12 | Lançamentos futuros | Henrique | ❌ | ❌ | ❌ | ❌ | ❌ | ✅ | ❌ | 0/9 |
| F-13 | Fórum | Henrique | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | 10/10 |
| F-14 | Moderação | Henrique | ❌ | ❌ | ❌ | ❌ | ❌ | ✅ | ❌ | 0/9 |
| F-15 | Journal manual | Bruno | 🟡 | ❌ | 🟡 | ❌ | 🟡 | ✅ | ❌ | 0/9 |
| F-16 | Timeline automática | Davi | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | 5/5 |
| F-17 | Estatísticas | Davi | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | 8/8 |
| F-18 | Anúncios | Caio | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | 8/8 |
| F-19 | Chat e propostas | Marina | ✅ | ✅ (sem Spring) | ✅ | ✅ | ✅ | ✅ | ✅ | 25/25 |
| F-20 | Pedido e checkout | Marina | ✅ | ✅ (sem Spring) | ✅ | ✅ | ✅ | ✅ | ✅ | 26/26 |
| F-21 | Reputação | Davi | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | 7/7 |
| T02 | Autenticação | grupo | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | 6/6 |

O que falta em cada parcial está em [diagnostico.md](diagnostico.md) (seção 3) e em [pendencias.md](pendencias.md).

## 3. Padrões de projeto

| Padrão | Funcionalidade | Responsável | Em código | Testado |
|---|---|---|---|---|
| Strategy | F-10 `SimilaridadeStrategy` | Lavínia | ✅ | ✅ `SimilaridadeTest`, `f10_similaridade.feature` |
| Strategy | F-20 `EstrategiaDePagamento` | Marina | ✅ | ✅ `f20_pedido_checkout.feature` (RN 5 e 6) |
| Observer | F-16 e F-17, observadores de eventos | Davi | ✅ | ✅ `f16_timeline_automatica.feature`, `f17_estatisticas.feature` |
| Proxy | F-19 `ConversasDoParticipante` | Marina | ✅ | ✅ `ConversasDoParticipanteTest`, `f19_chat_negociacao.feature` (RN 3) |
| Template Method | F-19/F-20 `RotinaDePrazo` | Marina | ✅ | ✅ `RotinaDePrazoTest`, cenários da RN 10 (F-19) e das RN 7 e 9 (F-20) |
| Decorator | previsto no CML para F-14/F-15 | `TODO(grupo)` | ❌ | ❌ |
| Iterator | previsto no CML para F-13/F-16 | `TODO(grupo)` | ❌ | ❌ |

## 4. Arquitetura e repositório

| Item | Status | Evidência |
|---|---|---|
| Domínio sem framework | ✅ | `ArquiteturaTest.dominioNaoDependeDeFrameworks` |
| Aplicação sem Spring | 🟡 | Negociação e pedidos migrados (`ArquiteturaTest.aplicacaoMigradaNaoDependeDoSpring`); os outros contextos ainda usam `@Service`/`@Transactional` |
| Agregados ligados só por Id | 🟡 | No domínio, ✅. Na persistência, `JogoJpa` ainda usa `@ManyToOne`/`@ManyToMany` para desenvolvedora e gênero (F-01) |
| Sem regra de negócio em controller | 🟡 | `ImagensDoForumController` valida tamanho e tipo de imagem (F-13) |
| `docker compose up --build` sobe banco, API e frontend | ✅ | `docker-compose.yml`, `backend/Dockerfile`, `frontend/Dockerfile`, `frontend/nginx.conf` (testado em 17/11/2026) |
| Segredos fora do Git | 🟡 `TODO(grupo)` | `.env.backup` removido e `.env*` ignorado, mas os valores continuam no histórico: **trocar** a chave JWT e a senha de admin |
| Uploads de usuários fora do Git | ✅ | `backend/uploads/` só com `.gitkeep` |
| Builds verdes | ✅ | `./mvnw verify`, `npm test` e `npm run build` |

## 5. Antes de entregar (`TODO(grupo)`)

- [ ] Conferir se o repositório está **público** ou com acesso para **@profsauloaraujo**.
- [ ] **Publicar o Figma Make** e colar o link `*.figma.site` em `entregaveis/prototipo/prototipo.md`.
- [ ] **Trocar os segredos vazados**: gerar outra `APP_SECURITY_JWT_SECRET` (`openssl rand -base64 32`) e outra senha
      de admin em todo ambiente que usou os valores do antigo `.env.backup`.
- [ ] Cada integrante anexa as suas interações de IA em `entregaveis/ia/interacoes/` e completa a declaração.
- [ ] Regerar `entregaveis/dominio/descricao_do_dominio.pdf` a partir do `.md` atualizado.
- [ ] Exportar os slides para `entregaveis/apresentacao/`.
- [ ] Remover todos os `@pendente` (F-01, F-06, F-07, F-08, F-12, F-14, F-15) com os cenários passando.
- [ ] Implementar Decorator e Iterator para chegar a 6 padrões (um por integrante).
- [ ] Corrigir os e-mails `email@exemplo.com` no `backend/pom.xml` (Davi, Henrique, Caio).
