# Caio Ferreira · Claude · 27/10 a 22/11/2026

| | |
|---|---|
| Integrante | Caio Ferreira |
| Ferramentas | Claude (Anthropic), pelo navegador |
| Datas | 27/10, 08/11, 16/11, 18/11, 19/11 e 22/11/2026 |
| Branches | `feat/f01-catalogo-api`, `feat/f05-jogando-agora`, `feat/f4-wishlist`, `fix/correcoes-wishlist`, `fix/caio-correcoes`, `improvements`, `style/melhorias`, `feat/biblioteca-perfil`, `feat/f15-anuncios` |
| Conversa completa | `TODO(Caio)`: exportar a conversa e salvar ao lado deste arquivo como `caio-claude-2026-11-22.txt` |

## O que foi pedido

Implementar as funcionalidades atribuídas ao Caio no PDF da atividade: F-01 (catálogo e importação), F-06 (wishlist), F-07 (Currently Playing) e F-18 (anúncios do Marketplace). Além disso, corrigir bugs apontados no perfil público, na biblioteca e na sessão de login.

## O que foi feito e aproveitado

| Data | Funcionalidade | Resultado | Commits |
|---|---|---|---|
| 27/10 | F-01 | Catálogo com importação via API e cadastro manual | 1f3c7f2 |
| 08/11 | F-07 | Marcação de "Currently Playing" com limite de 5 jogos | ab502cc, 079e0e4, d5f85cb |
| 16/11 | Ajustes | Melhorias gerais de interface | 7b714a0 |
| 18/11 | F-06 | Wishlist com prioridade e correções | d0f6b89, 76905a5 |
| 18/11 | Correções | Ajustes na wishlist e no perfil do Caio | 2093c2e, 5bed580, 2fe14dc |
| 19/11 | Estilo | Melhorias visuais e de layout | 5e9e7d0, 147fb87 |
| 22/11 | Perfil público | Biblioteca e zerados no perfil público; sessão mantida entre abas | 0db2db3, 8071105 |
| 22/11 | F-18 | Branch criada para anúncios do Marketplace; implementação ainda não iniciada | sem commits |

Com apoio da IA, foram corrigidas funcionalidades do backend e ajustado o CSS do frontend. As mudanças de regra de negócio (biblioteca obrigatória para favoritos e entradas do journal, e registro de zerado com inclusão automática na biblioteca) foram propostas nesta branch e registradas como RN 4 da F-08 e RN 4 da F-15 nas histórias, no CML e na descrição do domínio.

## Como foi revisado

- `mvn verify` no backend: BUILD SUCCESS, com 424 testes, 0 falhas, 0 erros e 0 ignorados.
- As regras novas da biblioteca foram documentadas nas histórias, no CML e na descrição do domínio antes do merge.
- Os diffs foram lidos antes de cada commit.
