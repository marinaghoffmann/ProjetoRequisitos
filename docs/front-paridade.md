# Paridade do frontend com o protótipo

Comparação das telas Angular com o protótipo de alta fidelidade do Figma Make, feita em 18 e 19/11/2026 na
branch `chore/entrega-final`.

**Referência usada.** As capturas do protótipo (`prototipo-telas.zip`, que iria para
`entregaveis/prototipo/telas/`) **não estavam no repositório nem na máquina** durante o trabalho. O link do
Figma Make exige login. A paridade foi feita pela descrição tela a tela do roteiro da entrega (B1 a B3), que
cita as medidas, os textos e os componentes de cada captura. `TODO(grupo)`: exportar as 27 telas do protótipo
para `entregaveis/prototipo/telas/` e conferir lado a lado com `docs/front-paridade/`.

**Capturas do Angular:** [`docs/front-paridade/`](front-paridade/), em 1440 × 900 (mais `celular-*` em 390 px,
`tablet-*` em 900 px e `visitante-*` sem login). Os nomes seguem a numeração das capturas do protótipo. Dados
de teste criados pela própria API no perfil `h2`.

Status: **igual** (estrutura, textos e estados como descritos), **adaptado** (difere por um motivo registrado)
ou **pendente**.

## Shell e componentes (B1)

| Item | Status | Observações |
|---|---|---|
| Menu lateral: ordem, "Minha biblioteca", itens por papel, chip do usuário | adaptado | Igual ao descrito. Comunidade sem o badge de novidades: a API não informa "novidades" do fórum. Os itens por papel ficam sob o rótulo "Gestão". |
| Nenhum item desabilitado; Lançamentos e Notificações fora do menu | igual | As rotas continuam; Lançamentos pelo "Todos" da Início, Notificações pelo sino. |
| Contadores da biblioteca | igual | Reais, de `GET /api/perfis/me/resumo`. |
| Barra superior em todas as telas (busca ⌘K, sino, "+ Registrar", Entrar/Criar conta) | igual | O ponto do sino aparece só com notificação não lida. "+ Registrar" abre o editor da F-15. "Sair" está no menu do chip. |
| Cabeçalho de página `gt-cabecalho-pagina` | igual | Eyebrow com o traço de 18 px, título em Space Grotesk `clamp(28px, 3.4vw, 44px)`, subtítulo de 12 px e ação à direita. |
| Larguras (larga e estreita ~850 px) | adaptado | Estreita em Journal, Timeline, Zerados, Em andamento, Wishlist, Notificações, Pedidos e Busca. O tópico continua largo (layout da F-13 com a coluna do autor). |
| Navegação de volta só em detalhe | igual | "← Voltar ao marketplace" no anúncio e "← Voltar à comunidade" no tópico. |
| Chips, linha de filtros inline, abas sublinhadas, card de pôster, linha de biblioteca, badges, estados, toast e confirmação | igual | Em `src/app/shared/ui/`. Filtros aplicados na hora. Jogo sem capa usa o gradiente da Início. |
| Responsivo (ícones abaixo de 1000 px; barra inferior e 2 colunas abaixo de 700 px) | igual | Ver `tablet-descobrir.png`, `celular-inicio.png` e `celular-mensagens.png`. |

## Telas (B3)

| Captura | Rota Angular | Status | Diferenças que ficaram e motivo |
|---|---|---|---|
| 01–03 Início | `/inicio` | adaptado | "Da sua comunidade" mostra os **tópicos recentes do fórum**: não existe timeline pública entre usuários (a da F-16 é de cada jogador). "Desafio do mês" omitido: não há dado real. "Em breve" com dia, mês, miniatura e "gênero · plataformas". |
| 04 Descobrir | `/descobrir` | igual | Chips de gêneros e plataformas que existem no catálogo. Ordenação inclui "Melhor avaliados ↓". |
| 05 Jogos | `/catalogo` | igual | Filtros da F-01: título, gênero (chips), desenvolvedora, origem e status (só administrador). "Cadastrar jogo" no cabeçalho; Editar e Remover sob o pôster para quem pode. `?gerenciar=1` (menu "Gerenciar catálogo") troca o título. |
| 06 Detalhe (topo) | `/catalogo/:id` | adaptado | Sem as **barras de distribuição** de notas: a API não fornece a distribuição por estrelas, só a média e o total. A capa desfocada ao fundo já existia. As abas Journal, Listas e Fórum levam às telas correspondentes (o fórum filtrado pelo jogo), porque não há consultas de journal e listas por jogo. |
| 07 Detalhe: reviews e ficha | `/catalogo/:id` | adaptado | Usa os componentes de reviews (F-02) e de estatísticas (F-17) existentes, com o estado "dados insuficientes" abaixo de 3 reviews. Sem curtidas em reviews: a F-02 não tem curtida. |
| 08 Detalhe: similares | `/catalogo/:id` | adaptado | Componente da F-10 mantido (relevância, motivo, rebaixamento e Útil / Não relevante). |
| 09 Listas | `/biblioteca/listas` | adaptado | Título "Minhas listas": não há listagem pública de todas as listas. Criar e renomear em modal; leque de até 4 capas. |
| 10 Wishlist | `/biblioteca/wishlist` | igual | Seletor de prioridade Alta/Média/Baixa (F-06), lista reordenada pela prioridade. |
| 11 Em andamento | `/biblioteca/jogando-agora` | adaptado | "iniciado em 18 de nov., 22:27" (data e hora, F-07 RN 3). "✓ Concluir" e "Remover" chamam a mesma API (a F-07 só tira do destaque); o Concluir avisa para registrar o zerado. O modal de substituição do 6º jogo continua no detalhe do jogo. |
| 12 Zerados | `/biblioteca/zerados` | igual | "N conclusões em M jogos", chips "1x zerado" e "100% / Platina", "Excluir" vermelho translúcido. |
| 13, 14 Journal e Timeline | `/journal`, `/timeline` | igual | Filtros inline aplicados na hora; linha do tempo vertical agrupada por data; entradas automáticas sem Editar; spoiler oculto até o clique. |
| 15 Nova entrada | modal | adaptado | Mesmos campos. O contador diz **"de 6 tags"**, não "de 5": o limite da regra é 6 (`EntradaDeJournal.LIMITE_DE_TAGS`). |
| 16 Comunidade | `/forum` | igual | Removido o texto "Mostrando x–y de n". |
| 17 Tópico | `/forum/:id` | adaptado | Já seguia o protótipo; mantido em largura larga. |
| 18 Lançamentos | `/lancamentos` | adaptado | Sem alteração estrutural nesta rodada; não tinha link de volta. |
| 19, 20 Marketplace | `/marketplace` | adaptado | Hero, "Compra protegida", chip "Jogos", "Meus pedidos", filtros inline (incluindo Título) e cards em 4 colunas. O "por @usuário" usa o nome público do perfil. "Meus anúncios" fica no menu do chip. |
| 21 Anúncio | `/marketplace/:id` | adaptado | Reputação "★ nota · N negociações" da F-21 (sem nota enquanto não há avaliações). "Denunciar" vem do componente da F-14. |
| 22 Checkout | `/marketplace/checkout` | adaptado | Continua como página, não como modal sobre o anúncio. Mudar exigiria mover a F-20 para dentro do anúncio; o fluxo e os campos são os mesmos. |
| 23 Mensagens | `/marketplace/mensagens/:id` | igual | Duas colunas (rota filha `:id`); no celular, uma coluna por vez. |
| 24 Meus pedidos | `/marketplace/pedidos` | igual | Saldo no subtítulo, abas Compras/Vendas/Concluídos/Reputação, eyebrow do status e stepper. |
| 25 Estatísticas | `/estatisticas` | adaptado | Conclusões, wishlists e negociações mostram **só o total**, sem "%" e sem "R$": a RN 2 da F-17 não define a base do percentual e a ficha guarda a contagem de negociações, não o valor (pendência **P9**). Fichas ocultas mostram "—" (RN 3). |
| 26 Notificações | `/notificacoes` | igual | "Marcar como lidas" marca uma a uma (não há rota em lote). |
| 27 Busca | `/busca` | adaptado | Subtítulo "Encontre jogos do catálogo e listas da comunidade": não há busca de reviews nem de pessoas (sem consulta pública de usuários). "Buscas em alta" são atalhos para os gêneros do catálogo. |
| Perfil, Login/Cadastro, Moderação, formulários | — | adaptado | Shell e barra superior aplicados (o perfil perdeu a barra própria). Os formulários mantêm o padrão global (rótulo acima, campo escuro). Não houve captura de referência. |

## Riscos e pontos de atenção

- Sem as capturas do protótipo, medidas finas (espaçamentos, tamanhos de fonte de cada card) podem divergir.
- O `app.scss` (shell) passou de 4,6 kB para 6,2 kB e continua acima do orçamento de 4 kB por componente, como
  já estava. Nenhum arquivo novo entrou na lista de avisos (eram 7, agora são 6).
