# T07 — Contrato do anúncio do marketplace

Contrato combinado entre anúncios (F-18), chat e propostas (F-19), pedidos (F-20), estatísticas (F-17)
e linha do tempo do journal (F-16). Segue o `ContextoAnuncios` de `entregaveis/cml/game_tracker.cml`.
O código fica em `backend/src/main/java/com/g4/game_tracker_backend/dominio/marketplace/anuncios/`
e é verificado por `ContratoAnuncioTest`.

## Campos do anúncio (`AnuncioMarketplace`)

| Campo | Tipo | Regra |
|---|---|---|
| `anuncioId` | UUID | identificador |
| `vendedorId` | UUID (Usuário) | só o proprietário altera preço e status (RN 2) |
| `jogoId` | UUID (Jogo do catálogo) | obrigatório, jogo ATIVO do catálogo (RN 1) |
| `titulo` | texto | |
| `estado` | `EstadoConservacao`: `NOVO`, `EXCELENTE`, `USADO`, `COM_MARCAS` | obrigatório |
| `preco` | decimal (BRL) | maior que zero (RN 3) |
| `tipoNegociacao` | `TipoNegociacao`: `VENDA`, `TROCA`, `AMBOS` | obrigatório |
| `midia` | `MidiaJogo`: `FISICO`, `DIGITAL` | obrigatório |
| `localidade` | texto | usada no filtro por região |
| `fotos` | lista de `FotoAnuncio` (`urlFoto`, `ordem`) | pelo menos 1 (RN 1) |
| `status` | `StatusAnuncio` | ciclo de vida abaixo |
| `criadoEm`, `atualizadoEm` | data e hora | |

## Ciclo de vida (`StatusAnuncio`)

| De | Para | Quem dispara |
|---|---|---|
| `ATIVO` | `PAUSADO` | proprietário (F-18) |
| `PAUSADO` | `ATIVO` | proprietário (F-18) |
| `ATIVO`, `PAUSADO` | `CANCELADO` | proprietário, ao excluir (F-18) |
| `ATIVO` | `EM_NEGOCIACAO` | aceite de proposta (F-19) |
| `EM_NEGOCIACAO` | `ATIVO` | reserva liberada: pedido cancelado ou expirado (F-20) |
| `EM_NEGOCIACAO` | `VENDIDO` | pedido concluído (F-20) |

`VENDIDO` e `CANCELADO` são finais. Qualquer outra transição é recusada com
`"O anúncio não pode passar de <ATUAL> para <NOVO>"`.

- Busca pública (F-18 RN 3): mostra apenas `ATIVO` e `EM_NEGOCIACAO` (`visivelNaBuscaPublica()`).
- Nova conversa ou proposta (F-19 RN 1): somente `ATIVO` (`aceitaNegociacao()`).

## Evento de mudança de status

Toda transição válida produz um `AnuncioStatusAlterado`:

| Campo | Descrição |
|---|---|
| `anuncioId`, `jogoId`, `vendedorId` | identificam o anúncio, o jogo e o vendedor |
| `statusAnterior`, `novoStatus` | a transição realizada |
| `ocorridoEm` | instante da mudança |

`vendido()` indica o caso `novoStatus == VENDIDO`, equivalente ao `AnuncioVendidoEvent` do CML.
Quem consome:

- **F-17**: recalcula o volume de negociações do jogo.
- **F-16**: com `vendido()`, gera a entrada automática no journal do comprador, usando o `PedidoConcluidoEvent` da F-20.
- **F-19**: deixa de aceitar propostas quando o anúncio sai de `ATIVO`.

A publicação na mesma transação da ação, como `EventoPlataforma`, é responsabilidade do barramento da **T04**.
O contrato só define o formato do evento e a regra que o gera (`CicloDeVidaAnuncio.transicionar`).

## Implementação (F-18)

O agregado `AnuncioMarketplace` implementa este contrato. As peças que F-19, F-20, F-16 e F-17 vão usar:

| Peça | Onde | Uso |
|---|---|---|
| `AnuncioMarketplace` | `dominio/marketplace/anuncios/` | agregado imutável; `mudarStatus(novo, agora)` delega ao `CicloDeVidaAnuncio` |
| `MudancaDeStatusAnuncio` | `dominio/marketplace/anuncios/` | resultado de `mudarStatus`: o anúncio já atualizado e o `AnuncioStatusAlterado` |
| `AnuncioMarketplaceRepositorio` | `dominio/marketplace/anuncios/` | `salvar`, `buscarPorId`, `buscarPublicos(FiltroAnuncios)`, `listarPorVendedor` |
| `FiltroAnuncios` | `dominio/marketplace/anuncios/` | filtros da busca pública; `statusVisiveis()` aplica a RN 3 |
| `AlterarStatusAnuncio` | `aplicacao/marketplace/anuncios/` | transições do proprietário: `PAUSADO`, `ATIVO` (vindo de `PAUSADO`) e `CANCELADO` |

- O proprietário **não** define `EM_NEGOCIACAO` nem `VENDIDO`: a F-19 e a F-20 devem criar os próprios casos de uso,
  carregar o anúncio pelo repositório, chamar `mudarStatus` e publicar o evento devolvido.
- Enquanto a T04 não existe, o evento é publicado com o `ApplicationEventPublisher` do Spring. Quem consumir
  pode usar `@EventListener` ou `@TransactionalEventListener` para `AnuncioStatusAlterado`.
- O título do anúncio é opcional na criação: quando vem vazio, usa o título do jogo no catálogo.
- Um anúncio aceita no máximo 10 fotos. Anúncios `VENDIDO` ou `CANCELADO` não podem mais ser editados.
