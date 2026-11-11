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
- Cenários BDD em `f18_anuncios_marketplace.feature`, com steps em `PassosAnunciosMarketplace`. O step
  `o jogador "<nome>" autenticado` fica nessa classe e pode ser reutilizado por outras funcionalidades.
- Detalhes para quem vai integrar (F-19, F-20, F-16, F-17): seção "Implementação" do
  [contrato do anúncio](entregaveis/contratos/contrato_anuncio.md).

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
- RN 3: só o comprador e o vendedor leem e escrevem (403 "Conversa restrita aos participantes").
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
  e liberação da custódia, com uma transação por pedido. Desligue com `APP_MARKETPLACE_ROTINAS=false`; nos testes elas
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

Como ligar as funcionalidades que ainda não existem:

| Funcionalidade | Implementar (bean Spring) | Publicar ao criar, editar ou excluir |
|---|---|---|
| F-02 Reviews | `FonteDeReviews` | `MetricasDoJogoAlteradas(jogoId)` |
| F-11 Conclusões | `FonteDeConclusoes` | `MetricasDoJogoAlteradas(jogoId)` |
| F-06 Wishlist | `FonteDeWishlists` | `MetricasDoJogoAlteradas(jogoId)` |

Ao ligar uma fonte real, remova a porta correspondente de `MetricasDeApoioDosCenarios` (test) e faça os steps usarem a
funcionalidade de verdade: as duas implementações juntas falham na subida dos testes. O marketplace já está ligado: o
volume de negociações conta os anúncios `VENDIDO` e é recalculado pelo `AnuncioStatusAlterado`, que a F-20 publica.

Pendências conhecidas da F-17:

- Até F-02, F-06 e F-11 chegarem, nenhum jogo atinge 3 reviews em produção, então a página sempre mostra o aviso de
  estatísticas indisponíveis.
- Os cenários de review, conclusão e wishlist usam dados de apoio (`MetricasDeApoioDosCenarios`), não as funcionalidades reais.
- Se o recálculo falhar depois do commit, o erro só vai para o log e a ficha fica desatualizada até o próximo evento.

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

Como ligar as funcionalidades que ainda não existem (publicar com o `ApplicationEventPublisher`, **dentro** do método
`@Transactional` do caso de uso, depois de gravar):

| Funcionalidade | Ao criar | Ao excluir |
|---|---|---|
| F-02 Reviews | `ReviewPublicada(autorId, jogoId, reviewId, publicadaEm)` | `ReviewExcluida(reviewId)` |
| F-11 Conclusões | `JogoZerado(jogadorId, jogoId, registroId, zeradoEm)` | `ConclusaoExcluida(registroId)` |
| F-20 Pedidos | `CompraConcluida(compradorId, jogoId, pedidoId, concluidaEm)` ao confirmar o recebimento | — |

Os eventos ficam em `dominio/journal/eventos`. Ao ligar uma funcionalidade real, troque nos steps da F-16 a ação
correspondente de `AcoesDeApoioDaTimeline` (test) pelo caso de uso de verdade e apague-a de lá.

Para a F-15: a edição de entradas precisa passar por `EntradaDeJournal.editar(...)`, que já aplica a RN 1 da F-15
(só o dono) e a RN 3 da F-16. A exclusão de uma entrada automática pelo jogador não está definida nas histórias; combine
antes de liberar.

Pendências conhecidas da F-16:

- Até F-02 e F-11 chegarem, só as compras concluídas no marketplace (F-20) geram entradas automáticas em produção.
- Editar uma review não atualiza o texto da entrada, que só cita o jogo.
- Não há visão pública da timeline; o perfil público é da F-06/F-08.

## Fórum (F-13)

| Método | Rota | Objetivo | Quem pode |
|---|---|---|---|
| `GET` | `/api/forum/topicos` | Tópicos paginados, do mais recentemente ativo para o menos (`jogoId`, `categoria`, `pagina` a partir de 1, `tamanho` até 50) | qualquer um |
| `GET` | `/api/forum/topicos/{id}` | Tópico com todas as respostas (o encadeamento vem em `respostaPaiId`) | qualquer um |
| `POST` | `/api/forum/topicos` | Criar tópico geral (`jogoId` nulo) ou de um jogo `ATIVO` do catálogo; nasce `ABERTO` | autenticado |
| `PUT` | `/api/forum/topicos/{id}` | Editar título, mensagem e categoria (até 48 horas após a publicação) | autor |
| `PATCH` | `/api/forum/topicos/{id}/status` | Marcar como `RESOLVIDO`, reabrir (`ABERTO`) ou encerrar (`ENCERRADO`) com `{ "status": "..." }` | autor |
| `DELETE` | `/api/forum/topicos/{id}` | Excluir o tópico (exclusão lógica: ele some das consultas) | autor |
| `POST` | `/api/forum/topicos/{id}/respostas` | Responder ao tópico, ou a outra resposta com `respostaPaiId` | autenticado |
| `PUT` | `/api/forum/topicos/{id}/respostas/{respostaId}` | Editar a resposta (até 48 horas após a publicação) | autor |
| `DELETE` | `/api/forum/topicos/{id}/respostas/{respostaId}` | Excluir a resposta | autor |

- RN 1: tópico `ENCERRADO` não aceita novas respostas, edições nem exclusões, e não pode ser reaberto pelo autor.
- RN 2: só o autor edita ou exclui a sua postagem (403 "Apenas o autor pode alterar esta postagem"); a edição vale por 48 horas.
- RN 3: criar, editar ou excluir uma resposta atualiza o contador do tópico e a data da última atividade. Os dois ficam
  gravados no próprio tópico (`forum_topicos`), então a listagem não carrega as respostas.
- A exclusão de resposta é lógica: ela deixa de contar, volta sem conteúdo (`removida: true`) e continua na árvore
  para as respostas filhas não sumirem. Não é possível responder a uma resposta removida.
- Toda resposta nova publica um `RespostaPublicada` pelo `ApplicationEventPublisher`, até o barramento da T04 existir.
- Frontend: `/forum` (lista com filtros e paginação), `/forum/novo`, `/forum/{id}` (tópico com respostas em árvore)
  e `/forum/{id}/editar`.
- Cenários BDD em `f13_forum.feature`, com steps em `PassosForum`. Jogadores e jogos de apoio ficam em `DadosDoCenario`,
  compartilhado entre as classes de steps; o step `o jogador "<nome>" autenticado` agora fica em `PassosComuns`.

Pendências conhecidas da F-13 (algumas são escopo da F-14):

- Moderadores e administradores ainda não têm poderes no fórum: encerrar, reabrir, ocultar e remover conteúdo de
  outros usuários fica para a central de moderação (F-14), que pode reaproveitar os campos `removido` e `removida`.
- A "formatação de texto" é só a preservação de quebras de linha; não há Markdown nem editor rico.
- A listagem mostra o nome do jogo procurando no catálogo carregado pela tela; não há busca por título do tópico.

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
