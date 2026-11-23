# Protótipo de alta fidelidade: Game Tracker

| | Link |
|---|---|
| **Protótipo publicado (público, sem login)** | https://pearl-spent-94260914.figma.site/ |
| Arquivo de edição no Figma Make (exige login) | https://www.figma.com/make/Hbid18iRVvTGOqfX2p4UNW/Game-Tracker-MVP-Development--Copy-?p=f&t=fS3NxPLv3Ov3KmNI-0 |

> O link publicado abre sem conta no Figma e é o que vale para a entrega. O de edição fica só para o grupo.

## Capturas das telas

As 27 telas de referência estão em [`telas/`](telas/), capturadas da prévia do Figma Make em 21/11/2026
(1487 × 764, sem a barra do Figma). A numeração é a mesma de [`docs/front-paridade.md`](../../docs/front-paridade.md),
o que permite comparar lado a lado com as capturas do Angular em `docs/front-paridade/`.

| Nº | Tela | Nº | Tela |
|---|---|---|---|
| 01–03 | [Início](telas/01-inicio.jpg), [em alta](telas/02-inicio-em-alta.jpg), [comunidade e em breve](telas/03-inicio-comunidade.jpg) | 16 | [Comunidade](telas/16-comunidade.jpg) |
| 04 | [Descobrir](telas/04-descobrir.jpg) | 17 | [Tópico](telas/17-topico.jpg) |
| 05 | [Jogos](telas/05-jogos.jpg) | 18 | [Lançamentos](telas/18-lancamentos.jpg) |
| 06 | [Detalhe do jogo](telas/06-detalhe-jogo.jpg) | 19, 20 | [Marketplace](telas/19-marketplace.jpg), [anúncios](telas/20-marketplace-anuncios.jpg) |
| 07 | [Detalhe: reviews e ficha](telas/07-detalhe-reviews-ficha.jpg) | 21 | [Anúncio](telas/21-anuncio.jpg) |
| 08 | [Detalhe: similares](telas/08-detalhe-similares.jpg) | 22 | [Checkout](telas/22-checkout.jpg) |
| 09 | [Listas](telas/09-listas.jpg) | 23 | [Mensagens](telas/23-mensagens.jpg) |
| 10 | [Wishlist](telas/10-wishlist.jpg) | 24 | [Meus pedidos](telas/24-pedidos.jpg) |
| 11 | [Em andamento](telas/11-em-andamento.jpg) | 25 | [Estatísticas](telas/25-estatisticas.jpg) |
| 12 | [Zerados](telas/12-zerados.jpg) | 26 | [Notificações](telas/26-notificacoes.jpg) |
| 13, 14 | [Journal](telas/13-journal.jpg), [Timeline](telas/14-timeline.jpg) | 27 | [Busca](telas/27-busca.jpg) |
| 15 | [Nova entrada](telas/15-nova-entrada.jpg) | | |

## Telas por funcionalidade

A numeração é a oficial de `entregaveis/historias/historias_game_tracker.txt`.

### 1. Catálogo e lançamentos

| Funcionalidade | Tela |
|---|---|
| F-01 | Catálogo com filtros (título, gênero, desenvolvedora, origem API/Criador, status) |
| F-01 | Importar jogo da API externa (busca, resultado, aviso "jogo já existe, atualizado") |
| F-01 | Cadastro manual de jogo pelo Criador (vinculado à desenvolvedora; erro de título duplicado) |
| F-01 | Perfil institucional da desenvolvedora (bio, site, redes, país) e edição |
| F-01 | Painel do Administrador: editar metadados e remover jogo (aviso de exclusão lógica) |
| F-12 | Lista de lançamentos futuros com contagem regressiva e botão "Quero jogar no lançamento" |
| F-12 | Formulário de cadastro e edição de lançamento (data no passado bloqueada; histórico de alterações) |

### 2. Avaliação e comunidade

| Funcionalidade | Tela |
|---|---|
| F-02 | Página do jogo com reviews e médias por categoria |
| F-02 | Modal de review (nota geral, 5 categorias, texto), edição e exclusão com confirmação |
| F-10 | Bloco "Jogos similares" com feedback Útil / Não relevante |
| F-10 | Admin: criar e remover similaridade manual |
| F-17 | Ficha de estatísticas do jogo (e o estado "estatísticas disponíveis a partir de 3 reviews") |

### 3. Biblioteca pessoal

| Funcionalidade | Tela |
|---|---|
| F-04 | Minhas listas (total de jogos), criar e editar lista, itens com nota explicativa (limite de 100) |
| F-06 | Wishlist ordenada por prioridade (Alta, Média, Baixa) |
| F-11 | Registrar "zerado" (data, plataforma, horas, 100%), histórico, editar e excluir |

### 4. Perfil e diário de jogatina

| Funcionalidade | Tela |
|---|---|
| F-07 | Currently Playing no perfil e modal de substituição ao tentar o 6º jogo |
| F-08 | Perfil público: capa, biografia (contador de 500), favoritos em 4 slots com reordenação |
| F-15 | Journal: criar e editar entrada (tags, até 3 imagens, spoiler) e filtro por jogo, tag e período |
| F-16 | Timeline unificada (entradas manuais e automáticas; as automáticas sem botão de editar) |

### 5. Fórum e moderação

| Funcionalidade | Tela |
|---|---|
| F-13 | Lista de tópicos paginada, tópico com respostas encadeadas, status Aberto/Resolvido/Encerrado |
| F-14 | Botão "Denunciar" e Central de Moderação (fila de denúncias, ação com justificativa, log) |

### 6. Marketplace

| Funcionalidade | Tela |
|---|---|
| F-18 | Busca do marketplace com filtros; criar e editar anúncio (fotos, estado, preço, tipo) |
| F-19 | Chat da negociação com proposta (Pendente, Aceita, Recusada) |
| F-20 | Checkout (endereço, Cartão/PIX/Carteira), acompanhamento do pedido, código de rastreio |
| F-21 | Avaliar negociação (estrelas e tags) e perfil de vendedor com reputação |
