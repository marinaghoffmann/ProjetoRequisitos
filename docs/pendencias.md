# Pendências e dúvidas de regra de negócio

Registro das ambiguidades encontradas ao comparar histórias, `.feature`, CML, glossário e código. Para cada
uma: a leitura adotada (a mais conservadora) e quem decide.

| # | Funcionalidade | Dúvida | Leitura adotada | Responsável |
|---|---|---|---|---|
| P1 | F-20 RN 7 | ~~A história dizia "7 dias pós-entrega", mas a plataforma não sabe quando o item chegou: a única confirmação de entrega é a do próprio comprador.~~ **Resolvida.** | O prazo conta a partir do **envio** (registro do código de rastreio). História, glossário, CML, `.feature` e código estão alinhados. | Marina |
| P2 | F-08 RN 3 | O código limita a biografia a 160 caracteres (`Usuario.BIO_MAXIMA`), mas a RN, o glossário e o `.feature` pedem 500. | Vale a RN (500). O código precisa ser ajustado. | Bruno |
| P3 | F-07 RN 3 | A RN pede data **e hora** de início; o código guarda só a data (`LocalDate`). | Vale a RN. O código precisa ser ajustado. | Caio |
| P4 | F-01 RN 3 | A RN pede exclusão definitiva quando o jogo não tem vínculos e lógica quando tem; o código sempre faz a lógica. | Vale a RN. Os 2 cenários `@pendente` do `f01_catalogo_jogos.feature` cobrem isso. | Caio |
| P5 | F-01 | O glossário e o CML falam em base externa "IGDB ou RAWG", e o código importa da Steam. | `TODO(grupo)`: decidir se o glossário e o CML passam a citar a Steam ou se a integração muda. | Caio |
| P6 | Numeração | O grupo usa uma numeração interna de 1 a 18; os entregáveis usam a oficial (F-01 a F-21, sem F-03, F-05 e F-09). | Todos os artefatos usam a **oficial**. A tabela de conversão está no README. | Grupo |
| P7 | F-13 | O fórum aceita imagens nos tópicos (`ImagensDoForum`), mas o glossário e as histórias não citam imagens no fórum. | `TODO(grupo)`: incluir o termo no glossário e a regra (5 MB, PNG/JPG/WEBP) na história, ou remover a função. | Henrique |
