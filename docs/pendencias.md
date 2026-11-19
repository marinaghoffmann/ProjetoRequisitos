# Pendências e dúvidas de regra de negócio

Registro das ambiguidades encontradas ao comparar histórias, `.feature`, CML, glossário e código. Para cada
uma: a leitura adotada (a mais conservadora) e quem decide.

| # | Funcionalidade | Dúvida | Leitura adotada | Responsável |
|---|---|---|---|---|
| P1 | F-20 RN 7 | ~~A história dizia "7 dias pós-entrega", mas a plataforma não sabe quando o item chegou: a única confirmação de entrega é a do próprio comprador.~~ **Resolvida.** | O prazo conta a partir do **envio** (registro do código de rastreio). História, glossário, CML, `.feature` e código estão alinhados. | Marina |
| P2 | F-08 RN 3 | ~~O código limitava a biografia a 160 caracteres, mas a RN, o glossário e o `.feature` pedem 500.~~ **Resolvida** no PR #26: `Usuario.BIO_MAXIMA = 500`. | Vale a RN (500). | Bruno |
| P3 | F-07 RN 3 | ~~A RN pede data **e hora** de início; o código guardava só a data (`LocalDate`).~~ **Resolvida.** | O início é um `Instant` (domínio, JPA `TIMESTAMPTZ` e DTO), com o ajuste da coluna em `db/ajustes-postgresql.sql`. A tela mostra data e hora. O cenário "Marcar um jogo registra a data e a hora de início" confere. | Caio |
| P4 | F-01 RN 3 | ~~O código sempre fazia a exclusão lógica.~~ **Resolvida.** | `RemoverJogo` exclui de vez o jogo sem vínculos e desativa o que tem, consultando a porta `VinculosDoJogo`. O cenário "Administrador desativa um jogo sem apagar seus dados" passou a dar ao jogo um vínculo (uma lista), porque sem vínculo a RN pede a exclusão definitiva. | Caio |
| P5 | F-01 | O glossário e o CML falam em base externa "IGDB ou RAWG", e o código importa da Steam. | `TODO(grupo)`: decidir. Proposta abaixo. | Caio |
| P6 | Numeração | O grupo usa uma numeração interna de 1 a 18; os entregáveis usam a oficial (F-01 a F-21, sem F-03, F-05 e F-09). | Todos os artefatos usam a **oficial**. A tabela de conversão está no README. | Grupo |
| P7 | F-13 | O fórum aceita imagens nos tópicos (`ImagensDoForum`), mas o glossário e as histórias não citam imagens no fórum. | `TODO(grupo)`: decidir. Proposta abaixo. | Henrique |
| P8 | F-12 RN 1 e 2 | O código criou a data **Estimada**, que não vira "Lançado" nem gera alerta, mas a história só falava em uma data. | **Resolvida.** A data Confirmada é a "data oficial" da RN 2; a Estimada é só previsão. História, glossário e CML atualizados. O grupo confirma na revisão do PR #25. | Henrique |
| P9 | F-17 RN 2 | A RN fala em "percentuais" com duas casas, mas não diz a base (percentual de quê?). O protótipo mostra conclusões e wishlists como "n · %" e negociações como "n · R$", e a ficha só guarda a **contagem** de negociações, não o valor. | A tela "Estatísticas da comunidade" mostra os totais e a média, sem % e sem valor. `TODO(grupo)`: definir a base do percentual (por exemplo, % dos jogadores que avaliaram o jogo) e se o valor negociado entra na ficha. | Davi |
| P10 | F-01 RN 3 | A RN vale também para o perfil de desenvolvedora ("remoção de um jogo ou de um perfil de desenvolvedora"). `DesativarDesenvolvedora` sempre faz a exclusão lógica, e não há cenário para isso. | Mantida a exclusão lógica, a mais conservadora: uma desenvolvedora quase sempre tem jogos vinculados. `TODO(grupo)`: confirmar. | Caio |

## Propostas de texto (decisão `TODO(grupo)`)

### P5: base externa da F-01

**Opção A, alinhar o texto ao código (recomendada, sem mudar código):**

- História, descrição da F-01: "via busca/importação automatizada de uma API externa (a loja **Steam**)".
- Glossário, novo termo: "**Base externa de jogos**: catálogo público de terceiros de onde o sistema importa
  metadados (título, resumo, capa, capturas, data de lançamento). No Game Tracker é a loja Steam, identificada
  pelo `identificadorExterno` (o *appid*). A importação nunca duplica: um identificador já existente vira
  atualização (F-01 RN 1)."
- CML: trocar `IgdbGateway` por `SteamGateway` (ou o nome neutro `BaseExternaJogosGateway`).

**Opção B:** manter "IGDB ou RAWG" no texto e trocar a integração, o que exige código novo e chave de API.

### P7: imagens no fórum (F-13)

- Glossário, novo termo: "**Imagem do fórum**: arquivo anexado a um tópico ou resposta. Aceita PNG, JPG ou WEBP
  de até 5 MB, conferidos pelo conteúdo do arquivo e não só pela extensão. Some das consultas públicas quando a
  postagem é ocultada ou removida pela moderação (F-14)."
- História da F-13, nova RN 4, "anexos de imagem": "Tópicos e respostas podem anexar imagens PNG, JPG ou WEBP de
  até 5 MB cada. O sistema recusa arquivos de outro tipo ou maiores, com a mensagem correspondente."
- `.feature`: um cenário de recusa por tipo e outro por tamanho, reaproveitando `ImagensDoForumTest`.

