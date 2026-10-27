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

## Entregáveis

| Artefato | Local |
|---|---|
| Histórias de usuário (funcionalidades e regras de negócio) | `entregaveis/historias_game_tracker.txt` |
| Descrição do domínio com linguagem onipresente | `entregaveis/dominio/` |
| Mapa de histórias do usuário | `entregaveis/mapa/` |
| Protótipos de alta fidelidade | `entregaveis/prototipo/prototipo.txt` |
| Modelo dos subdomínios (Context Mapper) | `entregaveis/cml/game_tracker.cml` |
| Cenários de teste BDD (Gherkin, pt) | `backend/src/test/resources/features/` |
| Automação dos cenários BDD (Cucumber) | `backend/src/test/java/com/g4/game_tracker_backend/bdd/` |
