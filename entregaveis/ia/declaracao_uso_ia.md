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
| `TODO(grupo)` | Caio Ferreira | `TODO(grupo)` |
| `TODO(grupo)` | Davi Marques | `TODO(grupo)` |
| `TODO(grupo)` | Henrique Brito | `TODO(grupo)` |
| `TODO(grupo)` | Bruno Mayer | `TODO(grupo)` |

## 2. Em quais artefatos e fases

Marque com X onde houve uso de IA. Deixe em branco onde não houve.

| Artefato / fase | Uso de IA | Observação |
|---|---|---|
| Histórias de usuário e regras de negócio | `TODO(grupo)` | |
| Descrição do domínio (linguagem onipresente) | `TODO(grupo)` | |
| Mapa de histórias | `TODO(grupo)` | |
| Protótipo (Figma Make) | `TODO(grupo)` | O Figma Make é uma ferramenta de geração assistida por IA |
| Modelo CML (Context Mapper) | X | Correção de sintaxe e do Context Map em 17/11/2026 |
| Cenários BDD (`.feature`) | `TODO(grupo)` | |
| Código do backend | X | F-19 e F-20 (Marina); `TODO(grupo)` para as demais |
| Código do frontend | `TODO(grupo)` | |
| Infraestrutura (Docker) | X | `docker-compose.yml` e Dockerfiles em 17/11/2026 |
| README e documentação | X | README, `docs/` e esta pasta em 17/11/2026 |

## 3. Como o resultado foi revisado

- Todo código gerado ou alterado com IA passou pelo build completo (`./mvnw verify`, com JUnit, ArchUnit e os
  cenários Cucumber, além de `npm test` e `npm run build`) antes do commit.
- As regras de negócio vêm das histórias do grupo (`entregaveis/historias/`) e dos `.feature`. A IA não
  definiu regras novas: quando houve dúvida, ela ficou registrada em `docs/pendencias.md`.
- As mudanças entram por Pull Request e passam pela revisão de pelo menos um colega (`CONTRIBUTING.md`).
- `TODO(grupo)`: cada integrante descreve aqui, em uma frase, como revisou o que a IA produziu para ele.

## 4. Responsáveis

| Integrante | Assinatura (nome completo) |
|---|---|
| Marina Hoffmann | `TODO(grupo)` |
| Lavínia Maranhão | `TODO(grupo)` |
| Caio Ferreira | `TODO(grupo)` |
| Davi Marques | `TODO(grupo)` |
| Henrique Brito | `TODO(grupo)` |
| Bruno Mayer | `TODO(grupo)` |
