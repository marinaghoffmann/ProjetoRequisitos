# Declaração de uso de Inteligência Artificial

**Projeto:** Game Tracker. Disciplina de Requisitos, Projeto de Software e Validação, CESAR School.

O grupo declara que **usou ferramentas de IA generativa** no desenvolvimento do projeto, nas condições
descritas abaixo. As interações registradas ficam em [`interacoes/`](interacoes/).

> `TODO(grupo)`: cada integrante revisa e completa a sua linha nas tabelas. Não deixem nenhuma linha
> genérica: o professor pediu as interações reais.

## 1. Ferramentas usadas

| Ferramenta | Quem usou | Para quê |
|---|---|---|
| Claude Code (Anthropic) | Marina Hoffmann | Diagnóstico da entrega, refatoração da camada de aplicação da F-19 e da F-20, padrões Proxy e Template Method, correção do CML, Docker e documentação (ver [registro de 17/11/2026](interacoes/marina-claude-code-2026-11-17.md)) |
| `TODO(grupo)` | Lavínia Maranhão | `TODO(grupo)` |
| Claude (Anthropic), pelo navegador | Caio Ferreira | F-01 (catálogo e importação), F-06 (wishlist), F-07 (Currently Playing), biblioteca no perfil e ajustes de CSS (ver [registro](interacoes/caio-claude-2026-11-22.md)) |
| Claude (Anthropic) e Claude Code | Davi Marques | Backend, steps BDD, telas e documentação da F-18 (anúncios), da F-13 (fórum), da F-17 (estatísticas), da F-16 (journal automático) e da F-21 (reputação); ligação da wishlist (F-06) às estatísticas (ver [registro](interacoes/davi-claude-2026-10-a-11.md)) |
| Figma e Figma Make | Davi Marques | Protótipo e exportação do protótipo funcional |
| `TODO(grupo)` | Henrique Brito | `TODO(grupo)` |
| `TODO(grupo)` | Bruno Mayer | `TODO(grupo)` |

## 2. Em quais artefatos e fases

Marque com X onde houve uso de IA. Deixe em branco onde não houve.

| Artefato / fase | Uso de IA | Observação |
|---|---|---|
| Histórias de usuário e regras de negócio | `TODO(grupo)` | |
| Descrição do domínio (linguagem onipresente) | `TODO(grupo)` | |
| Mapa de histórias | `TODO(grupo)` | |
| Protótipo (Figma Make) | X | O Figma Make é uma ferramenta de geração assistida por IA. Davi: protótipo no Figma e exportação do protótipo funcional com o Figma Make |
| Modelo CML (Context Mapper) | X | Correção de sintaxe e do Context Map em 17/11/2026 |
| Cenários BDD (`.feature`) | X | Davi: steps da F-18, F-13, F-16, F-17 e F-21 (os `.feature` vieram das histórias do grupo); `TODO(grupo)` para as demais |
| Código do backend | X | F-19 e F-20 (Marina); F-18, F-13, F-16, F-17 e F-21 (Davi); `TODO(grupo)` para as demais |
| Código do frontend | X | Davi: telas do marketplace (F-18), fórum (F-13), timeline (F-16), ficha de estatísticas (F-17) e perfil do vendedor (F-21); `TODO(grupo)` para as demais |
| Infraestrutura (Docker) | X | `docker-compose.yml` e Dockerfiles em 17/11/2026 |
| README e documentação | X | README, `docs/` e esta pasta em 17/11/2026; documentação da API e das regras da F-18, F-13, F-16, F-17 e F-21 (Davi) |

## 3. Como o resultado foi revisado

- Todo código gerado ou alterado com IA passou pelo build completo (`./mvnw verify`, com JUnit, ArchUnit e os
  cenários Cucumber, além de `npm test` e `npm run build`) antes do commit.
- As regras de negócio vêm das histórias do grupo (`entregaveis/historias/`) e dos `.feature`. A IA não
  definiu regras novas: quando houve dúvida, o grupo decidiu e o texto foi alinhado ao código.
- As mudanças entram por Pull Request e passam pela revisão de pelo menos um colega (`CONTRIBUTING.md`).
- Davi: cada funcionalidade foi conferida contra as regras de negócio da história e os cenários do `.feature`, com
  `./mvnw verify` verde antes de cada commit; o código entrou por Pull Request e o diff foi lido antes de abrir cada um.
- `TODO(grupo)`: cada integrante descreve aqui, em uma frase, como revisou o que a IA produziu para ele.

## 4. Responsáveis

| Integrante | Assinatura (nome completo) |
|---|---|
| Marina Hoffmann | `TODO(grupo)` |
| Lavínia Maranhão | `TODO(grupo)` |
| Caio Ferreira | `TODO(grupo)` |
| Davi Marques | Davi Marques Oliveira |
| Henrique Brito | `TODO(grupo)` |
| Bruno Mayer | `TODO(grupo)` |
