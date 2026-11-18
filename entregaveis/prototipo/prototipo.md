# Protótipo de alta fidelidade: Game Tracker

| | Link |
|---|---|
| **Protótipo publicado (público, sem login)** | `TODO(grupo)`: publicar o Figma Make e colar aqui o link `https://<nome>.figma.site/` |
| Arquivo de edição no Figma Make (exige login) | https://www.figma.com/make/Hbid18iRVvTGOqfX2p4UNW/Game-Tracker-MVP-Development--Copy-?p=f&t=fS3NxPLv3Ov3KmNI-0 |

> Para publicar: no Figma Make, **Publish** → copiar o endereço `*.figma.site`. O professor precisa abrir o
> protótipo sem conta no Figma, então o link de edição não serve como entrega.

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
