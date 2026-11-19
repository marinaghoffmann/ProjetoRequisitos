# API do Game Tracker por funcionalidade

Detalhamento dos endpoints, regras e decisões de cada funcionalidade. Todas as rotas ficam sob `/api`. Erros de regra de negócio voltam como **422** com `ProblemDetail` (mensagem no campo `detail`), recurso inexistente como **404** e acesso negado como **403**.

> Os títulos de seção preservam a numeração usada quando cada parte foi escrita. **T03** é a F-01 (catálogo) e **T07** é o contrato do anúncio da F-18. A tabela de conversão está no [README](../README.md#funcionalidades-por-contexto).

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
e `DELETE /api/catalogo/jogos/{id}`. RN 3: sem vínculos em outros contextos (reviews, journal, listas, wishlist,
conclusões, anúncios, pedidos, fórum, lançamentos, favoritos, jogos em andamento, similaridades) a exclusão é
**definitiva**; com vínculos, é **lógica** (status `INATIVO`) e preserva os dados. A resposta traz
`{ "id", "titulo", "exclusao": "DEFINITIVA" | "LOGICA" }`. Os vínculos chegam pela porta `VinculosDoJogo`.
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

## Jogos similares e feedback de relevância (F-10)

| Método | Rota | Objetivo | Quem pode |
|---|---|---|---|
| `GET` | `/api/descoberta/jogos/{jogoId}/similares?notaMinima=&limite=` | Similares do jogo, do maior para o menor grau (`limite` de 1 a 50, padrão 10), com origem, totais `UTIL` e `NAO_RELEVANTE` e `meuFeedback` de quem está logado | qualquer um |
| `POST` | `/api/descoberta/similaridades/{id}/feedback` | Registrar `{ "tipo": "UTIL" }` ou `NAO_RELEVANTE` | autenticado |
| `PUT` | `/api/descoberta/similaridades/{id}/feedback` | Trocar o próprio feedback | autor do feedback |
| `DELETE` | `/api/descoberta/similaridades/{id}/feedback` | Remover o próprio feedback | autor do feedback |
| `POST` | `/api/descoberta/similaridades` | Conexão manual `{ "jogoAId", "jogoBId", "grau" }` (grau de 0 a 1) | `ADMINISTRADOR` |
| `DELETE` | `/api/descoberta/similaridades/{id}` | Remover a conexão (fica bloqueada para o recálculo) | `ADMINISTRADOR` |
| `POST` | `/api/descoberta/jogos/{jogoId}/similares/recalcular` | Recalcular os similares do jogo | `ADMINISTRADOR` |

- Padrão Strategy (GoF): `SimilaridadeStrategy` com `SimilaridadePorGeneroStrategy` e `SimilaridadePorTagStrategy` (índice
  de Jaccard) e `SimilaridadePorEstudioStrategy` (1 para a mesma desenvolvedora, 0 se não). `CalculoSimilaridade` combina
  as estratégias com pesos 0,5 / 0,3 / 0,2 e devolve o grau de 0 a 1 com duas casas.
- Tags: o `Jogo` do catálogo não tem campo de tags, então a `PerfilJogoGateway` (ACL, `PerfilJogoDoCatalogo`) usa os
  descritores que o cadastro e a edição já gravam no `metadadosJson`: `tags` (se uma importação ou carga informar),
  `subgeneros`, `temas`, `modosJogo` e `generoPrincipal`. Gêneros são os gêneros cadastrados e o estúdio é a
  desenvolvedora. O catálogo não foi alterado.
- Quando calcula: o jogo é comparado com os demais jogos `ATIVO` e só os pares com grau acima de 0,20 são guardados.
  Roda pelo endpoint de administrador ou sob demanda, na primeira consulta de um jogo sem similares calculados, sem
  ligação com o cadastro da F-01.
- RN 1: um jogo nunca é similar a ele mesmo ("Um jogo não pode ser similar a ele mesmo"). Cada par é uma linha só em
  `similaridades_jogos` (menor UUID em `jogo_a_id`, restrição única no par) e é consultado pelos dois lados, então o
  grau de (A, B) é o de (B, A).
- RN 2: um feedback por usuário e sugestão ("Você já avaliou esta sugestão"; restrição única `(usuario_id,
  similaridade_id)`), que o autor pode trocar ou remover. Os totais são recontados a cada mudança. O `grauBase` nunca
  muda pelo feedback: com mais de 15 `NAO_RELEVANTE` (a partir do 16º), a sugestão automática fica `rebaixada` e o
  grau efetivo cai pela metade; com 15 ou menos, nada muda. Editar ou remover feedbacks desfaz o rebaixamento.
- RN 3: só `ADMINISTRADOR` cria, remove ou recalcula ("Apenas administradores podem ajustar similaridades"). A conexão
  manual substitui a automática do mesmo par e o recálculo nunca a altera; a removida fica `REMOVIDA` e não volta.
- Filtro `notaMinima`: usa a média geral da ficha de estatísticas (F-17) do jogo sugerido, e só jogos elegíveis (3 ou
  mais reviews) passam.
- Frontend: bloco "Jogos similares" em `/catalogo/{id}` (cards com capa, título e grau, filtro pela avaliação, "Útil" e
  "Não relevante" para quem está logado; para administradores, conexão manual, remoção e recálculo).
- Cenários BDD em `f10_similaridade.feature`, com steps em `PassosSimilaridade`.

Pendências conhecidas da F-10:

- O rebaixamento vale só para sugestões automáticas: a conexão manual é a palavra final do administrador, que pode
  removê-la.
- Um jogo sem nenhum par acima de 0,20 é recalculado a cada consulta (não há marca de "já calculado sem resultados").
- Jogos novos só entram nas sugestões de jogos já calculados quando eles são recalculados (endpoint de administrador).
- A importação da Steam ainda não grava tags no `metadadosJson`; jogos importados são comparados só por gênero e estúdio.
- Remover uma conexão mantém os feedbacks já dados a ela (deixam de aparecer junto com a sugestão).

## Marketplace: contrato do anúncio (T07)

Status, campos e evento de mudança de status do anúncio estão em
[entregaveis/contratos/contrato_anuncio.md](../entregaveis/contratos/contrato_anuncio.md).

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
- Cenários BDD em `f18_anuncios_marketplace.feature`, com steps em `PassosAnunciosMarketplace`. O step
  `o jogador "<nome>" autenticado` fica nessa classe e pode ser reutilizado por outras funcionalidades.
- Detalhes para quem vai integrar (F-19, F-20, F-16, F-17): seção "Implementação" do
  [contrato do anúncio](../entregaveis/contratos/contrato_anuncio.md).

Pendências conhecidas da F-18:

- A resposta do anúncio não traz o nome do vendedor nem a capa do jogo; os cards usam a primeira foto.
- A região é um texto livre (`localidade`) buscado por trecho, sem UF ou cidade estruturadas.
- A busca não é paginada.
- As URLs das fotos não são restritas aos arquivos enviados por `/api/marketplace/fotos`, e fotos removidas de
  um anúncio continuam no disco.
- `GET /api/marketplace/anuncios/{id}` devolve o anúncio em qualquer status, inclusive cancelado.

## Chat de negociação e propostas (F-19)

| Método | Rota | Objetivo | Quem pode |
|---|---|---|---|
| `POST` | `/api/marketplace/anuncios/{anuncioId}/conversas` | Abrir a conversa com o vendedor, ou reabrir a que já existe | autenticado (não o dono do anúncio) |
| `GET` | `/api/marketplace/conversas` | Caixa de mensagens: anúncio, outra parte, última mensagem, status da última proposta e não lidas, da atividade mais recente para a mais antiga | autenticado (só as próprias) |
| `GET` | `/api/marketplace/conversas/nao-lidas` | Total de mensagens recebidas e não lidas, para o menu (`{ "total": n }`) | autenticado |
| `GET` | `/api/marketplace/conversas/{id}` | Conversa com mensagens e propostas em ordem cronológica | comprador ou vendedor da conversa |
| `POST` | `/api/marketplace/conversas/{id}/leitura` | Marcar como lidas as mensagens recebidas (ao abrir a conversa) | comprador ou vendedor da conversa |
| `POST` | `/api/marketplace/conversas/{id}/mensagens` | Enviar mensagem `{ "texto": "..." }` | comprador ou vendedor da conversa |
| `POST` | `/api/marketplace/conversas/{id}/propostas` | Proposta `{ "tipo": "VALOR", "valor": 120.00 }` ou `{ "tipo": "TROCA", "jogoOferecido": "..." }`, com `mensagem` opcional | comprador |
| `POST` | `/api/marketplace/conversas/{id}/propostas/{propostaId}/aceite` e `/recusa` | Responder a proposta pendente | vendedor |
| `POST` | `/api/marketplace/conversas/{id}/propostas/{propostaId}/cancelamento` | Cancelar a proposta pendente | comprador |
| `POST` | `/api/marketplace/conversas/{id}/propostas/{propostaId}/liberacao` | Liberar a reserva antes das 48 horas | vendedor |

As regras seguem o documento das telas (11 RNs):

- RN 1 e 2: conversa só a partir de anúncio `ATIVO`, nunca no próprio anúncio, e uma por anúncio e comprador
  (restrição única `(anuncio_id, comprador_id)`); abrir de novo reabre a existente.
- RN 3: só o comprador e o vendedor leem e escrevem (403 "Conversa restrita aos participantes"). Quem garante é o
  proxy de proteção `ConversasDoParticipante` (padrão Proxy), que os casos de uso recebem no lugar do repositório.
- RN 4 e 5: mensagem de 1 a 1.000 caracteres; com o anúncio `VENDIDO` ou `CANCELADO` a conversa fica somente leitura.
- RN 6 a 8: proposta de valor maior que zero ou de troca com o nome do jogo (2 a 80 caracteres), uma pendente por
  conversa; só o vendedor aceita ou recusa e só o comprador cancela. Proposta de valor exige anúncio de venda ou
  "ambos"; de troca, anúncio de troca ou "ambos".
- RN 9: o aceite muda o anúncio para `EM_NEGOCIACAO` e recusa as pendentes das outras conversas do anúncio com o aviso
  "item reservado para outro comprador", tudo na mesma transação.
- RN 10: sem pedido em 48 horas, a proposta aceita fica `EXPIRADA` e o anúncio volta a `ATIVO` (rotina agendada); o
  vendedor pode liberar a reserva antes. Com o pedido gerado, a reserva não expira mais.
- RN 11: cada mensagem guarda quando foi lida; abrir a conversa marca as recebidas.
- O chat é assíncrono: a conversa aberta consulta a API a cada 5 segundos e o menu, a cada 30 segundos (sem WebSocket).
- Frontend: `/marketplace/{id}` (página do anúncio com Conversar, Fazer proposta e Comprar), `/marketplace/mensagens`
  e `/marketplace/mensagens/{id}`; "Mensagens" no menu mostra as não lidas.
- Cenários BDD em `f19_chat_negociacao.feature`, com steps em `PassosChatNegociacao`. Anúncio, conversa e proposta do
  cenário ficam em `CenarioDoMarketplace`, reaproveitado pela F-20.

Pendências conhecidas da F-19:

- Não há notificação fora do chat (e-mail ou push): o aviso é a contagem de não lidas.
- A lista de conversas não é paginada.

## Pedido, checkout e entrega (F-20)

| Método | Rota | Objetivo | Quem pode |
|---|---|---|---|
| `GET` | `/api/marketplace/checkout?anuncioId=` ou `?propostaId=` | Prévia sem cobrança: item, valor, opções de frete, prazo da reserva e saldo da carteira | autenticado |
| `POST` | `/api/marketplace/pedidos` | Gerar o pedido (ver corpo abaixo); o pedido nasce pago, aguardando envio | comprador |
| `GET` | `/api/marketplace/pedidos` | Compras e vendas do usuário, do mais recente para o mais antigo | autenticado (só os próprios) |
| `GET` | `/api/marketplace/pedidos/{id}` | Um pedido, com endereço, pagamento e prazos | comprador ou vendedor |
| `POST` | `/api/marketplace/pedidos/{id}/envio` | Registrar o rastreio `{ "codigoRastreio": "..." }` | vendedor |
| `POST` | `/api/marketplace/pedidos/{id}/recebimento` | Confirmar o recebimento e liberar o valor ao vendedor | comprador |
| `GET` | `/api/marketplace/carteira` | Saldo da carteira (`{ "saldo": 500.00 }`) | autenticado |

Corpo do `POST /api/marketplace/pedidos`: `anuncioId` (compra direta) **ou** `propostaId` (proposta aceita), `frete`
(`ENVIO_SEGURO` ou `EXPRESSO`), `formaPagamento` (`CARTAO`, `PIX` ou `SALDO_CARTEIRA`), `endereco` (`destinatario`, `cep`,
`rua`, `numero`, `complemento`, `cidade`, `uf`) e, só no cartão, `cartao` (`numero`, `nomeImpresso`, `validade`, `cvv`).

- RN 1 e 2: o pedido vem da proposta aceita do próprio comprador, pelo valor da proposta, ou da compra direta de
  anúncio `ATIVO` pelo preço anunciado (que reserva o anúncio e recusa as propostas pendentes). Anúncio só de troca não
  tem compra direta, e um anúncio tem no máximo um pedido não cancelado. Proposta de troca gera o pedido com o item a
  R$ 0,00: o comprador paga só o frete.
- RN 3 e 4: endereço com destinatário (3 a 80), CEP `00000-000`, rua, número, cidade e UF; total = item + frete
  (R$ 18,90 ou R$ 32,90), em duas casas.
- RN 5 e 6, pagamento simulado (padrão Strategy, `EstrategiaDePagamento`), sem gateway real:
  - **Cartão**: número de 13 a 19 dígitos válido pelo algoritmo de Luhn (ex.: `4111 1111 1111 1111`), validade MM/AA
    não vencida e CVV de 3 ou 4 dígitos. Só os 4 últimos dígitos ficam gravados. Reembolso: estorno simulado.
  - **PIX**: aprovado na confirmação do checkout. Reembolso: devolução simulada.
  - **Saldo da carteira**: debita a carteira, criada na primeira transação com R$ 500,00 de simulação ("Saldo
    insuficiente na carteira" quando não cobre o total). Reembolso: crédito de volta na carteira.
- RN 7: o valor fica `EM_CUSTODIA` (fora da carteira de todos) até o comprador confirmar o recebimento ou passarem 7 dias
  do envio; então vai para a carteira do vendedor (`LIBERADO`).
- RN 8 e 9: o vendedor tem 3 dias úteis (segunda a sexta, fuso de Brasília, sem feriados) para registrar o rastreio
  (3 a 60 caracteres); vencido o prazo, o pedido é `CANCELADO`, o pagamento `REEMBOLSADO` e o anúncio volta a `ATIVO`.
- RN 10 e 11: só o comprador confirma, e só com o pedido `ENVIADO`. Na conclusão, na mesma transação, o anúncio passa a
  `VENDIDO` (a F-17 recalcula as estatísticas) e a F-16 recebe `CompraConcluida` para o journal do comprador.
- Rotinas agendadas (`RotinasDoMarketplace`, de hora em hora): expiração das reservas (F-19), cancelamento sem rastreio
  e liberação da custódia, com uma transação por item. As três herdam de `RotinaDePrazo` (padrão Template Method).
  Desligue com `APP_MARKETPLACE_ROTINAS=false`; nos testes elas
  ficam desligadas e os cenários chamam os casos de uso com o instante atual sobre datas recuadas.
- A F-21 está ligada: `PedidosParaReputacao` implementa `FonteDePedidos`, e a conclusão do pedido é o início dos 15 dias
  para avaliar.
- Frontend: `/marketplace/checkout?proposta=` ou `?anuncio=` (dois passos) e `/marketplace/pedidos` (abas Compras, Vendas
  e Concluídos, com rastreio, confirmação de recebimento e avaliação da outra parte).
- Cenários BDD em `f20_pedido_checkout.feature`, com steps em `PassosPedidoCheckout`; pedidos reais dos cenários ficam em
  `PedidosDoCenario`, usado também pela F-21.

Pendências conhecidas da F-20:

- Não há extrato da carteira, só o saldo, nem saque (as histórias não pedem).
- O "estoque" é o próprio anúncio: cada anúncio é um item único e sai da busca ao ficar `VENDIDO`.
- Na troca, só o envio do item do anúncio é rastreado; o jogo oferecido pelo comprador é combinado no chat.
- Os steps da F-16 (`AcoesDeApoioDaTimeline.confirmarRecebimento`) e da F-17 (`venderAnuncio`) continuam usando os apoios
  de teste; a integração real com as duas está coberta pelo cenário da RN 11 da F-20.

## Reputação no marketplace (F-21)

| Método | Rota | Objetivo | Quem pode |
|---|---|---|---|
| `POST` | `/api/marketplace/pedidos/{pedidoId}/avaliacoes` | Avaliar a outra parte do pedido com `{ "estrelas": 1-5, "tags": [...], "comentario": "..." }` | comprador ou vendedor do pedido |
| `GET` | `/api/marketplace/usuarios/{usuarioId}/reputacao` | Perfil de vendedor: nota média, totais e avaliações recebidas (da mais recente para a mais antiga) | qualquer um |

- Tags: `ENVIO_RAPIDO`, `PRODUTO_COMO_DESCRITO`, `BOM_COMUNICADOR` e `PAGAMENTO_RAPIDO`. O comentário é opcional (até 1000 caracteres).
- RN 1: só para pedido concluído ("Só é possível avaliar pedidos concluídos"), e só pelas duas partes. O avaliado é a
  outra parte do pedido, e não um campo da requisição.
- RN 2: uma avaliação por parte e pedido ("Você já avaliou este pedido"), com restrição única
  `(pedido_id, avaliador_id)`, em até 15 dias após a confirmação da entrega ("O prazo de 15 dias para avaliar terminou").
- RN 3: a reputação (`marketplace_reputacoes`) é recalculada na mesma transação, com a linha travada. A média vem da soma
  das estrelas (duas casas, `HALF_UP`), para não acumular erro de arredondamento. Cada avaliação soma 1 negociação bem-sucedida.
- Frontend: `/marketplace/vendedores/{id}` (perfil de vendedor), acessado por "Ver reputação do vendedor" no card do
  anúncio. A avaliação é feita em "Meus pedidos" (F-20), nos pedidos concluídos.
- Cenários BDD em `f21_reputacao.feature`, com steps em `PassosReputacao`.

Ligação com a F-20: `PedidosParaReputacao` implementa `FonteDePedidos`; `entregaConfirmadaEm` é a data de conclusão do
pedido (confirmação do recebimento ou fim da tolerância de 7 dias). Os steps criam pedidos reais por `PedidosDoCenario`.

Pendências conhecidas da F-21:

- Não há como editar nem excluir uma avaliação (as histórias não pedem).
- O perfil lista todas as avaliações de uma vez, sem paginação.

## Reviews e avaliações categoriais (F-02)

| Método | Rota | Objetivo | Quem pode |
|---|---|---|---|
| `GET` | `/api/avaliacao/jogos/{jogoId}/reviews` | Reviews do jogo, da mais recente para a mais antiga (`pagina` a partir de 1, `tamanho` até 50), com `totalReviews`, `mediaGeral` e `mediasPorCategoria` de todas as reviews | qualquer um |
| `GET` | `/api/avaliacao/jogos/{jogoId}/reviews/minha` | A review do usuário para o jogo (200) ou nenhuma (204) | autenticado |
| `GET` | `/api/avaliacao/reviews/minhas` | Reviews do usuário, com título e capa de cada jogo | autenticado |
| `POST` | `/api/avaliacao/reviews` | Publicar `{ "jogoId", "notaGeral", "notasPorCategoria": [{ "categoria", "nota" }], "texto" }` | autenticado |
| `PUT` | `/api/avaliacao/reviews/{id}` | Editar as notas e o texto (mesmo corpo; `jogoId` é ignorado) | autor |
| `DELETE` | `/api/avaliacao/reviews/{id}` | Excluir de vez a review e as notas por categoria | autor |

- Categorias: `GRAFICOS`, `SOM`, `JOGABILIDADE`, `DIVERSAO` e `INOVACAO`. São opcionais e aparecem no máximo uma vez
  ("Cada categoria só pode receber uma nota"). Toda nota vai de 1 a 5 ("A nota deve estar entre 1 e 5"); o texto é
  opcional, com até 5000 caracteres. O jogo precisa existir (404) e estar `ATIVO` ("O jogo precisa estar ativo no catálogo").
- RN 1: uma review por usuário e jogo. Quem tenta publicar de novo recebe 409 com
  `{ "detail": "Você já publicou uma review deste jogo", "reviewExistenteId": "..." }`, e a tela abre a edição da
  review existente. A restrição única `(autor_id, jogo_id)` em `reviews` cobre requisições simultâneas.
- RN 2: o autor é sempre o usuário autenticado; editar ou excluir a review de outra pessoa dá 403 "Apenas o autor pode
  alterar a review" (checado no agregado `Review`).
- RN 3: a exclusão é física. As notas (`reviews_notas_categoria`) saem junto, pelo JPA (`cascade = ALL` e
  `orphanRemoval`) e pelo banco (FK com `ON DELETE CASCADE`).
- As médias (geral e por categoria) usam todas as reviews do jogo, com duas casas e `HALF_UP`; categoria sem nota vem `null`.
- F-16 ligada: `PublicarReview` publica `ReviewPublicada` e `GerenciarReview.excluir` publica `ReviewExcluida`, dentro
  da transação. Os steps da F-16 usam o caso de uso real, inclusive no cenário de falha, que roda a publicação numa
  transação que quebra logo depois (`AcoesDeApoioDaTimeline.executarEFalharDepois`).
- F-17 ligada: criar, editar e excluir publicam `MetricasDoJogoAlteradas`, e `ReviewsParaEstatisticas` implementa
  `FonteDeReviews`. A porta saiu de `MetricasDeApoioDosCenarios`, e `PassosEstatisticas` publica reviews de verdade.
- Frontend: `/catalogo/{id}` mostra as médias, as reviews paginadas e o botão "Escrever review" (ou "Editar minha
  review"), que abre o modal com a nota geral, as 5 categorias e o texto; o autor vê Editar e Excluir (com
  confirmação), e a aba Reviews leva à seção. Em `/perfil`, a aba Reviews lista as reviews do usuário.
- Cenários BDD em `f02_reviews.feature`, com steps em `PassosReviews`.

Pendências conhecidas da F-02:

- Editar a review não atualiza o texto da entrada automática da timeline (pendência já registrada na F-16).
- Duas publicações exatamente simultâneas do mesmo usuário para o mesmo jogo: a segunda esbarra na restrição única e
  volta como erro genérico, e não como o 409 da RN 1.
- A ficha de estatísticas da página do jogo só mostra as novas médias ao recarregar a página.
- Não há curtidas, comentários nem moderação de reviews (fora do escopo da F-02).

## Estatísticas dos jogos (F-17)

| Método | Rota | Objetivo | Quem pode |
|---|---|---|---|
| `GET` | `/api/catalogo/jogos/{id}/estatisticas` | Ficha de estatísticas consolidadas do jogo | qualquer um |

- RN 1: a ficha é refeita por inteiro a cada evento, sempre a partir das fontes. O recálculo roda depois do commit da
  ação, em transação própria, para enxergar os dados gravados e não desfazer a ação do usuário se falhar.
- RN 2: as médias são gravadas com duas casas decimais (`estatisticas_jogo`), com arredondamento `HALF_UP`.
- RN 3: com menos de 3 reviews ativas, a API devolve `elegivel: false` e só o total de reviews e o mínimo exigido. Os
  demais indicadores vão nulos.
- Frontend: `/catalogo/{id}` (página do jogo com a ficha), acessada pelo título do jogo no catálogo.
- Cenários BDD em `f17_estatisticas.feature`, com steps em `PassosEstatisticas`.

Fontes de dados da ficha:

| Funcionalidade | Implementar (bean Spring) | Publicar ao criar, editar ou excluir |
|---|---|---|
| F-02 Reviews | `FonteDeReviews` | `MetricasDoJogoAlteradas(jogoId)` |
| F-11 Conclusões | `FonteDeConclusoes` | `MetricasDoJogoAlteradas(jogoId)` |
| F-06 Wishlist | `FonteDeWishlists` | `MetricasDoJogoAlteradas(jogoId)` |

Reviews, conclusões e wishlist usam fontes reais. O volume de negociações conta os anúncios `VENDIDO` e é recalculado pelo
`AnuncioStatusAlterado`, que a F-20 publica.

Pendências conhecidas da F-17:

- Se o recálculo falhar depois do commit, o erro só vai para o log e a ficha fica desatualizada até o próximo evento.

## Journal individual (F-15 / F12 do grupo)

| Método | Rota | Objetivo | Quem pode |
|---|---|---|---|
| `GET` | `/api/journal/entradas` | Consultar entradas com `origem`, `jogoId`, `tag`, `inicio`, `fim`, `pagina`, `tamanho` | autenticado, somente próprias |
| `GET` | `/api/journal/entradas/tags` | Tags disponíveis para o filtro | autenticado, somente próprias |
| `GET` | `/api/journal/entradas/{id}` | Dados completos para edição | dono |
| `POST` | `/api/journal/entradas` | Criar anotação e imagens em um multipart | autenticado |
| `PUT` | `/api/journal/entradas/{id}` | Editar anotação e anexos no mesmo multipart | dono de entrada manual |
| `DELETE` | `/api/journal/entradas/{id}` | Excluir entrada manual, tags e imagens | dono |
| `GET` | `/api/perfis/{donoId}/journal` | Journal público paginado, com os mesmos filtros e spoilers ocultos | público |
| `GET` | `/api/perfis/{donoId}/journal/tags` | Tags públicas do perfil | público |
| `GET` | `/api/perfis/{donoId}/journal/{id}?revelarSpoiler=true` | Revelar anotação após clique explícito | público |
| `GET` | `/api/journal/entradas/{id}/anexos/{anexoId}` | Imagem vinculada à entrada; spoiler exige `revelarSpoiler=true` | público |

POST/PUT recebem a parte JSON `entrada` (`Content-Type: application/json`):

```json
{"jogoId":null,"dataSessao":"2026-11-18","titulo":"Uma descoberta","texto":"Explorei uma nova região.","tags":["exploração"],"contemSpoiler":false,"anexosMantidos":[]}
```

Cada arquivo novo vem na parte `imagens`. Na edição, `anexosMantidos` preserva os IDs listados;
omitir o campo preserva todos os existentes. A data da sessão é obrigatória. Jogo e título são opcionais.
Tags são normalizadas e únicas (até seis); imagens PNG/JPG/WebP (até três de 2 MB cada).
Entradas automáticas aparecem nas consultas, mas não podem ser editadas ou excluídas pelo CRUD manual.

A consulta pública devolve `texto: null`, `mascarada: true` e `anexos: []` nos spoilers até a revelação.
Os dados ocultos não ficam apenas borrados no HTML. A mídia binária usa `Cache-Control: no-store`.
O próprio Journal e a timeline do dono exibem o texto completo. Datas dos filtros são inclusivas.

Persistência: `journal_entradas`, `journal_tags` e `journal_anexos` (conteúdo binário no banco).
Gravação e exclusão das imagens participam da mesma transação da entrada. Frontend: `/journal` e aba
Journal do perfil; roteiro completo em [f12-journal.md](f12-journal.md).

## Timeline automática do journal (F-16)

| Método | Rota | Objetivo | Quem pode |
|---|---|---|---|
| `GET` | `/api/journal/timeline` | Timeline do jogador, com entradas manuais e automáticas, da sessão mais recente para a mais antiga (`pagina` a partir de 1, `tamanho` até 50) | autenticado (só as próprias entradas) |

- A entrada do journal é uma só (`EntradaDeJournal`, tabela `journal_entradas`), com `tipoOrigem` `MANUAL` ou `AUTOMATICO`,
  como no CML. A F-15 acrescenta tags, anexos e o CRUD das manuais sobre essa base.
- RN 1: o `ObservadorEventosPlataforma` usa `@EventListener` e roda na transação de quem publicou o evento. Se a ação
  falhar, a entrada sofre rollback junto. `GerarEntradaAutomatica` usa `Propagation.MANDATORY`: publicar o evento fora
  de uma transação dá erro na hora, em vez de gravar uma entrada solta.
- RN 2: excluir a ação de origem remove a entrada automática. O vínculo é `(tipo_evento, origem_id)`, com restrição
  única, e o mesmo evento entregue duas vezes não duplica a entrada.
- RN 3: `EntradaDeJournal.editar(...)` recusa entradas automáticas com "Entradas automáticas não podem ser editadas".
- Frontend: `/timeline` ("Minha timeline" no menu, para quem está logado).
- Cenários BDD em `f16_timeline_automatica.feature`, com steps em `PassosTimelineAutomatica`.

Eventos publicados **dentro** da transação do caso de uso, depois de gravar. A F-02 e a F-11 usam o
`ApplicationEventPublisher` num método `@Transactional`. A F-20 usa a porta `PublicadorDeEventos` dentro do
`ExecutorDeTransacao`, e o efeito é o mesmo:

| Funcionalidade | Ao criar | Ao excluir |
|---|---|---|
| F-02 Reviews | `ReviewPublicada(autorId, jogoId, reviewId, publicadaEm)` | `ReviewExcluida(reviewId)` |
| F-11 Conclusões | `JogoZerado(jogadorId, jogoId, registroId, zeradoEm)` | `ConclusaoExcluida(registroId)` |
| F-20 Pedidos | `CompraConcluida(compradorId, jogoId, pedidoId, concluidaEm)` ao confirmar o recebimento | — |

Os eventos ficam em `dominio/journal/eventos`. Ao ligar uma funcionalidade real, troque nos steps da F-16 a ação
correspondente de `AcoesDeApoioDaTimeline` (test) pelo caso de uso de verdade e apague-a de lá.

O CRUD manual da F-15 passa por `EntradaDeJournal.editar(...)`, aplicando propriedade e bloqueio de alterações
nas automáticas. A exclusão de entradas automáticas continua vinculada à ação de origem, sem botão no CRUD manual.

Pendências conhecidas da F-16:

- A entrada automática guarda a data do evento original. Editar a conclusão não altera a data da entrada no journal.

## Registros de conclusão (F-11)

Na página do jogo, o jogador autenticado pode usar **Marcar zerado** para registrar uma conclusão e **Minhas conclusões**
para consultar, editar ou excluir seus registros. O histórico mostra o total pessoal e ordena pela data da conclusão,
da mais recente à mais antiga.
Em **Minha biblioteca → Zerados**, cada jogo aparece com o registro mais recente. O filtro por jogo revela todos os
registros daquele jogo. A tela permite registrar, editar e excluir diretamente.

| Método | Rota | Objetivo |
|---|---|---|
| `GET` | `/api/biblioteca/zerados` | Listar os jogos zerados pelo jogador autenticado |
| `GET` | `/api/biblioteca/zerados/registros` | Listar os registros próprios com título e capa do jogo |
| `GET` | `/api/biblioteca/jogos/{jogoId}/conclusoes` | Consultar o histórico do jogador autenticado |
| `POST` | `/api/biblioteca/jogos/{jogoId}/conclusoes` | Registrar uma conclusão |
| `PUT` | `/api/biblioteca/conclusoes/{id}` | Editar uma conclusão própria |
| `DELETE` | `/api/biblioteca/conclusoes/{id}` | Excluir uma conclusão própria |

A API recebe `dataConclusao` (AAAA-MM-DD), `plataforma`, `horasJogadas`, `concluido` e `platina`.
A data é obrigatória e não pode ser futura. Horas são opcionais e, se informadas, devem ser positivas.
Platina exige conclusão principal. As estatísticas contam os registros de todos os jogadores para o jogo.
Na edição, `jogoId` é opcional. Ao trocar o jogo, as duas fichas de estatísticas são recalculadas e a entrada
automática da timeline passa a apontar para o jogo correto.
Os cenários de aceitação estão em `f11_conclusoes.feature`.
- Não há visão pública da timeline; o perfil público é da F-06/F-08.

## Favoritos e perfil público (F-08)

O próprio perfil (`/perfil`) permite adicionar até quatro favoritos, substituir, remover e mudar a posição.
Os slots ficam compactados na ordem de exibição. Também é possível favoritar pela página de um jogo.
O endereço `/perfil/{id}` pode ser compartilhado e consultado sem login, com bio, avatar, capa e favoritos ordenados.
O link "Ver perfil público" abre essa visualização em uma nova aba, sempre sem controles de edição, inclusive para o dono.
A edição do perfil aceita bio de até 500 caracteres e capa por upload ou URL HTTP(S) terminada em PNG, JPG ou WebP.
Consultas públicas não incluem email, senha ou identificadores de autenticação.

| Método | Rota | Objetivo |
|---|---|---|
| `GET` | `/api/perfis/{id}` | Perfil público e favoritos em ordem |
| `POST` | `/api/perfis/me/favoritos` | Adicionar `{ jogoId, slot? }` |
| `PATCH` | `/api/perfis/me/favoritos/{jogoId}/posicao` | Mover com `{ slot }` |
| `PUT` | `/api/perfis/me/favoritos/slots/{slot}` | Substituir com `{ jogoId }` |
| `DELETE` | `/api/perfis/me/favoritos/{jogoId}` | Remover e compactar posições |

As escritas operam exclusivamente sobre o usuário autenticado. O bloqueio da linha do dono serializa
alterações simultâneas, inclusive na primeira inclusão. As referências a jogos são UUIDs.
Os cenários de aceitação estão em `f08_perfil_favoritos.feature`, usando os casos de uso e persistência reais.

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
| `GET` | `/api/forum/topicos/{id}` | Tópico com todas as respostas na ordem da conversa (padrão Iterator: cada resposta antes das filhas, irmãs por publicação), com `respostaPaiId` e o `nivel` de recuo | qualquer um |
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
- Toda resposta nova publica um `RespostaPublicada` pela porta `PublicadorDeEventos`.
- Frontend: `/forum` é a Comunidade, com editor na própria página, filtros por categoria, status e jogo,
  abas Recentes/Em alta e paginação de 5 tópicos. `/forum/{id}` traz cartões de autor, curtidas e respostas
  encadeadas com editor inline. `/forum/novo` e `/forum/{id}/editar` permanecem para compatibilidade.
  As telas seguem a [referência visual da Comunidade no Figma Make](https://www.figma.com/make/Hbid18iRVvTGOqfX2p4UNW/Game-Tracker-MVP-Development--Copy-?t=zTCKnJrVlLNDiCpq-1).
- Cenários BDD em `f13_forum.feature`, com steps em `PassosForum`. Jogadores e jogos de apoio ficam em `DadosDoCenario`,
  compartilhado entre as classes de steps; o step `o jogador "<nome>" autenticado` agora fica em `PassosComuns`.

A moderação do fórum, as denúncias e os avisos 18+ são descritos na F-14 abaixo. A listagem ainda mostra
  o nome do jogo procurando no catálogo carregado pela tela; não há busca por título do tópico.

## Listas personalizadas (F-04)

| Método | Rota | Objetivo | Quem pode |
|---|---|---|---|
| `GET` | `/api/biblioteca/listas` | Minhas listas, da atualizada mais recentemente para a mais antiga, com `totalJogos` e os itens | autenticado |
| `POST` | `/api/biblioteca/listas` | Criar `{ "titulo", "descricao" }` | autenticado |
| `GET` | `/api/biblioteca/listas/{id}` | Uma lista com os itens (título e capa do jogo, nota explicativa, data de inclusão) | qualquer um |
| `PUT` | `/api/biblioteca/listas/{id}` | Renomear: título e descrição | dono |
| `DELETE` | `/api/biblioteca/listas/{id}` | Excluir a lista e os itens (exclusão física) | dono |
| `POST` | `/api/biblioteca/listas/{id}/itens` | Adicionar `{ "jogoId", "notaExplicativa" }` (nota opcional) | dono |
| `PATCH` | `/api/biblioteca/listas/{id}/itens/{jogoId}` | Editar a nota explicativa `{ "notaExplicativa" }` | dono |
| `DELETE` | `/api/biblioteca/listas/{id}/itens/{jogoId}` | Remover o jogo da lista | dono |
| `GET` | `/api/biblioteca/usuarios/{usuarioId}/listas` | Listas públicas de um usuário | qualquer um |

- Título obrigatório, com até 80 caracteres e sem os espaços das pontas; descrição e nota explicativa com até 500.
  O jogo precisa existir (404) e estar `ATIVO` ("O jogo precisa estar ativo no catálogo"). Quem não é o dono recebe
  403 "Apenas o dono pode alterar esta lista"; o dono é sempre o usuário autenticado.
- RN 1: título único por usuário, sem diferenciar maiúsculas e minúsculas e também ao renomear ("Você já possui uma
  lista com este título"). Outro usuário pode usar o mesmo título. No banco, `titulo_chave` (minúsculas, sem os espaços
  das pontas) tem restrição única com `dono_id`.
- RN 2: um jogo uma vez por lista ("Este jogo já está na lista"), com restrição única `(lista_id, jogo_id)` em `listas_itens`.
- RN 3: até 100 jogos por lista, no agregado `ListaPersonalizada` ("A lista atingiu o limite de 100 jogos"). A lista tem
  versão (bloqueio otimista), para duas adições simultâneas não passarem do limite.
- Excluir a lista remove os itens pelo JPA (`cascade = ALL` e `orphanRemoval`) e pelo banco (FK com `ON DELETE CASCADE`).
- Frontend: `/biblioteca/listas` ("Minhas listas", com `papelGuard`: criar, renomear, excluir e o total de cada lista),
  `/biblioteca/listas/{id}` (itens com capa e nota editável, remover item e o contador "N/100"; quem não é dono só
  vê), o botão "Adicionar à lista" na página do jogo, o item "Listas" do menu e a aba Listas do perfil.
- Cenários BDD em `f04_listas_personalizadas.feature`, com steps em `PassosListasPersonalizadas`.

Pendências conhecidas da F-04:

- Não há como reordenar os itens: a lista segue a ordem de inclusão.
- A exclusão lógica de um jogo do catálogo (F-01) não mexe nas listas: o item continua, com o status do jogo na resposta.
- Ainda não existe perfil público para ver as listas de outra pessoa; a API já oferece a rota pública.

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

## Wishlist (F-06)

| Método | Rota | Objetivo | Quem pode |
|---|---|---|---|
| `GET` | `/api/perfis/me/wishlist` | Wishlist do usuário, ordenada por prioridade (ALTA, MEDIA, BAIXA) e, na mesma prioridade, da mais recente | autenticado |
| `GET` | `/api/perfis/{id}/wishlist` | Wishlist de outro jogador, na mesma ordem | qualquer um |
| `POST` | `/api/perfis/me/wishlist` | Adicionar `{ "jogoId", "prioridade" }`; sem prioridade, vale `MEDIA` (RN 3) | autenticado |
| `PATCH` | `/api/perfis/me/wishlist/{jogoId}` | Alterar a prioridade | autenticado |
| `DELETE` | `/api/perfis/me/wishlist/{jogoId}` | Remover (idempotente) | autenticado |

- RN 1: a wishlist nasce na primeira adição; o banco garante uma por usuário (a chave é o dono).
- RN 2: jogo repetido é recusado com "Este jogo já está na sua wishlist".
- Adicionar e remover publicam `MetricasDoJogoAlteradas`, e a ficha da F-17 recalcula a presença em wishlists.
- Cenários BDD em `f06_wishlist.feature`, com steps em `PassosWishlist`.

## Currently Playing (F-07)

| Método | Rota | Objetivo | Quem pode |
|---|---|---|---|
| `GET` | `/api/biblioteca/jogando-agora` | Jogos em andamento do usuário, com progresso e `iniciadoEm` (data e hora, RN 3) | autenticado |
| `GET` | `/api/perfis/{id}/jogando-agora` | Jogos em andamento de outro jogador, para o perfil público | qualquer um |
| `POST` | `/api/biblioteca/jogando-agora` | Marcar `{ "jogoId", "substituirJogoId" }`; no 6º jogo sem `substituirJogoId`, recusa com "Remova ou substitua um jogo para continuar" (RN 1 e 2) | autenticado |
| `PATCH` | `/api/biblioteca/jogando-agora/{jogoId}` | Alterar o progresso (`NO_INICIO`, `NA_METADE`, `PERTO_DO_FIM`) | autenticado |
| `DELETE` | `/api/biblioteca/jogando-agora/{jogoId}` | Remover do destaque | autenticado |

Cenários BDD em `f07_currently_playing.feature`, com steps em `PassosCurrentlyPlaying`.

## Consultas das telas

| Método | Rota | Objetivo | Quem pode |
|---|---|---|---|
| `GET` | `/api/estatisticas?visibilidade=TODOS\|PUBLICAS\|OCULTAS` | Fichas da F-17 de todos os jogos ativos (tela "Estatísticas da comunidade"). Nas ocultas, só seguem `totalReviewsAtivas` e `minimoDeReviews` (RN 3) | qualquer um |
| `GET` | `/api/busca?termo=` | Busca global: até 12 jogos ativos e 8 listas pelo título. Termo com menos de 2 letras devolve listas vazias. Listas ocultas pela moderação ficam de fora; não há busca de pessoas | qualquer um |
| `GET` | `/api/perfis/me/resumo` | Totais do menu: `emAndamento`, `naWishlist` e `jogosZerados` | autenticado |
