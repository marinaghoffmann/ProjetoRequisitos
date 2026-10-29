# Game Tracker

Sistema web estilo "Letterboxd de jogos" (Java 21 / Spring Boot, Angular, DDD, arquitetura limpa).

## Estrutura

| Pasta | Conteúdo |
|---|---|
| `backend/` | API Spring Boot em camadas (`dominio`, `aplicacao`, `infraestrutura`, `apresentacao`) |
| `frontend/` | Aplicação Angular |
| `entregaveis/` | Documentação do projeto |
| `docker-compose.yml` | PostgreSQL para desenvolvimento |

Como rodar, onde colocar cada classe, como escrever os steps do Cucumber e o fluxo de branches:
veja [CONTRIBUTING.md](CONTRIBUTING.md).

## Catálogo (T03)

O núcleo do catálogo está disponível na API Spring Boot:

| Método | Rota | Objetivo |
|---|---|---|
| `GET` | `/api/catalogo/jogos` | Consultar jogos ativos com filtros opcionais (`titulo`, `generoId`, `desenvolvedoraId`, `origem`, `status`) |
| `GET` | `/api/catalogo/generos` | Listar gêneros cadastrados |
| `GET` | `/api/catalogo/desenvolvedoras` | Listar desenvolvedoras cadastradas |
| `POST` | `/api/catalogo/jogos` | Cadastrar jogo manualmente |
| `POST` | `/api/catalogo/jogos/importacao` | Importar ou atualizar jogo pelo identificador externo |
| `POST` | `/api/catalogo/generos` | Cadastrar gênero |
| `POST` | `/api/catalogo/desenvolvedoras` | Cadastrar desenvolvedora |

O banco recebe uma carga inicial idempotente com gêneros, uma desenvolvedora e jogos de exemplo.
Imagens de capa podem ser enviadas por `POST /api/catalogo/imagens` como multipart no campo `imagem`.
Os formatos aceitos são PNG, JPG e WEBP, com limite de 5 MB; os arquivos são salvos em
`backend/uploads/catalogo/` e servidos em `/uploads/catalogo/...`.
O catálogo também oferece CRUD de jogos: `GET /api/catalogo/jogos/{id}`, `PUT /api/catalogo/jogos/{id}`
e `DELETE /api/catalogo/jogos/{id}`. A exclusão desativa o jogo, preservando seus dados.
Desenvolvedores podem ser vinculados a uma desenvolvedora por `PATCH /api/usuarios/{id}/desenvolvedora`
com `{ "desenvolvedoraId": "..." }`; o cadastro manual usa automaticamente esse vínculo.
Para importar da Steam Store, não é necessária chave de API. Envie o título ou termo de busca em
`POST /api/catalogo/jogos/importacao/steam` com `{ "termo": "Elden Ring" }`.
O perfil de desenvolvedora pode ser atualizado por `PUT /api/catalogo/desenvolvedoras/{id}` e desativado
por `DELETE /api/catalogo/desenvolvedoras/{id}`.

Consultas (`GET`) são públicas. As demais operações exigem login e o papel certo, checado nos casos de uso:

| Operação | Quem pode |
|---|---|
| Cadastrar jogo manualmente | `DESENVOLVEDOR` vinculado a uma desenvolvedora ativa |
| Editar jogo | `ADMINISTRADOR`, ou `DESENVOLVEDOR` da desenvolvedora do jogo |
| Desativar jogo, importar (API/Steam), cadastrar gênero | `ADMINISTRADOR` |
| Cadastrar ou desativar desenvolvedora | `ADMINISTRADOR` |
| Editar perfil da desenvolvedora | `ADMINISTRADOR`, ou `DESENVOLVEDOR` vinculado a ela |
| Enviar imagem de capa | `ADMINISTRADOR` ou `DESENVOLVEDOR` |
| Alterar papel e vincular desenvolvedor (`PATCH /api/usuarios/{id}/...`) | `ADMINISTRADOR` |

## Marketplace: contrato do anúncio (T07)

Status, campos e evento de mudança de status do anúncio estão em
[entregaveis/contratos/contrato_anuncio.md](entregaveis/contratos/contrato_anuncio.md).

## Marketplace: anúncios (F-18)

| Método | Rota | Objetivo | Quem pode |
|---|---|---|---|
| `GET` | `/api/marketplace/anuncios` | Busca pública com filtros opcionais (`jogoId`, `titulo`, `precoMinimo`, `precoMaximo`, `estado`, `tipoNegociacao`, `regiao`) | qualquer um |
| `GET` | `/api/marketplace/anuncios/{id}` | Consultar um anúncio | qualquer um |
| `GET` | `/api/marketplace/anuncios/meus` | Anúncios do usuário autenticado, em qualquer status | autenticado |
| `POST` | `/api/marketplace/anuncios` | Criar anúncio (nasce `ATIVO`) | autenticado |
| `PUT` | `/api/marketplace/anuncios/{id}` | Editar dados e preço | proprietário |
| `PATCH` | `/api/marketplace/anuncios/{id}/status` | Pausar (`PAUSADO`) ou reativar (`ATIVO`) com `{ "status": "..." }` | proprietário |
| `DELETE` | `/api/marketplace/anuncios/{id}` | Excluir: o anúncio passa a `CANCELADO` (exclusão lógica) | proprietário |
| `POST` | `/api/marketplace/fotos` | Enviar foto do item (multipart, campo `imagem`; PNG, JPG ou WEBP até 5 MB) | autenticado |

- O jogo precisa estar `ATIVO` no catálogo e o anúncio precisa de pelo menos 1 foto. O preço deve ser maior que zero.
- A busca pública mostra apenas anúncios `ATIVO` e `EM_NEGOCIACAO`. Buscar por `VENDA` ou `TROCA` também traz os anúncios `AMBOS`.
- `EM_NEGOCIACAO` e `VENDIDO` não são definidos pelo proprietário: ficam a cargo da F-19 (negociação) e da F-20 (pedido).
- Toda mudança de status publica um `AnuncioStatusAlterado` pelo `ApplicationEventPublisher` do Spring, até o barramento da T04 existir.
- Frontend: `/marketplace` (busca), `/marketplace/novo`, `/marketplace/meus` e `/marketplace/{id}/editar`.

## Entregáveis

| Artefato | Local |
|---|---|
| Histórias de usuário (funcionalidades e regras de negócio) | `entregaveis/historias_game_tracker.txt` |
| Descrição do domínio com linguagem onipresente | `entregaveis/dominio/` |
| Mapa de histórias do usuário | `entregaveis/mapa/` |
| Protótipos de alta fidelidade | `entregaveis/prototipo/prototipo.txt` |
| Modelo dos subdomínios (Context Mapper) | `entregaveis/cml/game_tracker.cml` |
| Contrato do anúncio do marketplace (T07) | `entregaveis/contratos/contrato_anuncio.md` |
| Cenários de teste BDD (Gherkin, pt) | `backend/src/test/resources/features/` |
| Automação dos cenários BDD (Cucumber) | `backend/src/test/java/com/g4/game_tracker_backend/bdd/` |
