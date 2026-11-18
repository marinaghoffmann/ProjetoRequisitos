# Davi Marques · Claude · 29/10 a 18/11/2026

| | |
|---|---|
| Integrante | Davi Marques Oliveira |
| Ferramentas | Claude (Anthropic) e Claude Code, no terminal; Figma e Figma Make no protótipo |
| Datas | 29/10, 30/10, 06/11, 09/11 e 18/11/2026 |
| Branches | `Davi`, `feat/f16-timeline-automatica`, `feat/f21-reputacao`, `feat/f17-wishlist-estatisticas` |
| Conversa completa | `TODO(Davi)`: exportar com `/export` no Claude Code e salvar ao lado deste arquivo como `davi-claude-code-2026-11-18.txt` |

## O que foi pedido

Implementar as funcionalidades atribuídas ao Davi, do BDD às telas: F-18 (anúncios do marketplace) e F-13 (fórum)
na primeira etapa; F-17 (estatísticas), F-16 (automação do journal) e F-21 (reputação no marketplace) no plano de
2 semanas; e ligar as fontes de dados dos colegas à F-17 conforme ficassem prontas.

## O que foi feito e aproveitado

| Data | Funcionalidade | Resultado | Commits |
|---|---|---|---|
| 29/10 | F-18 | Anúncios com ciclo de vida, fotos e busca pública, cenários BDD, telas do marketplace, docs | 524cc02, f3fea11, 3740efd, c2904c3 |
| 30/10 | F-13 | Fórum com tópicos, respostas encadeadas e consulta paginada, cenários BDD, telas, docs | 6246166, 11cfe1b, 502200e, b7083d0 |
| 06/11 | F-17 | Engine de estatísticas com recálculo por eventos, ficha pública, cenários BDD, página do jogo, docs | ad847f0, d066af0, f7a3c38, 72e3ae6 |
| 06/11 | F-13 | Fórum no tema escuro e link no menu | c230568 |
| 09/11 | F-16 | Entradas automáticas do journal por eventos e timeline unificada, cenários BDD, página, docs | 3fcfe86, 26fa8d9, f0cddbc, f0a8745 |
| 09/11 | F-21 | Avaliação das partes do pedido e reputação, cenários BDD, perfil do vendedor, docs | d442958, 6b5888f, 822cf40, e3966d4, 6969430 |
| 18/11 | F-17 + F-06 | Wishlist real alimentando as estatísticas; sai a fonte falsa dos testes | ea13373 (PR #30) |

No protótipo, o Figma foi usado para desenhar as telas e o Figma Make para exportar o protótipo funcional.

## Como foi revisado

- `./mvnw verify` verde (JUnit, ArchUnit e Cucumber) antes de cada commit.
- As regras vieram das histórias e dos `.feature` do grupo; a IA não criou regra nova.
- Tudo entrou por Pull Request, com o diff lido antes de abrir.
