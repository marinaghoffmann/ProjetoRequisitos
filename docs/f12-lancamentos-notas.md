# Handoff — F9 / F-12: lançamentos futuros

**Responsável:** Henrique Brito
**Branch:** feat/HenriqueBrito
**Estado:** entregue no PR #25 (branch `feat/HenriqueBrito`). Notas de implementação da F-12; a referência da API está em [api.md](api.md).

## O que foi entregue

O módulo de lançamentos usa as quatro camadas e os nomes em português do Game Tracker. Um administrador ou desenvolvedor vinculado cadastra explicitamente um lançamento para um jogo ativo que já existe no catálogo. A API pública e a tela /lancamentos mostram a agenda por data. Usuários autenticados podem acompanhar jogos e usar a caixa de notificações internas em /notificacoes.

A data pode ser **CONFIRMADA** ou **ESTIMADA**. O formulário começa como ESTIMADA para não apresentar uma possível data como oficial. Uma estimativa continua visível se o dia passar; não muda o status para LANCADO nem dispara o aviso de 24 horas. Ao confirmar uma data futura, os alertas dos interessados são agendados. Reclassificar uma data confirmada como estimada cancela os alertas pendentes. A classificação é informada por quem cadastra: não há verificação automática de fontes. Jogos que não foram inscritos na agenda podem continuar sem data no catálogo.

## Regras e integrações

- Datas trafegam em ISO AAAA-MM-DD e são calculadas como LocalDate no fuso America/Sao_Paulo. O aviso confirmado vence à meia-noite do dia anterior.
- Um jogo tem no máximo um lançamento; um usuário tem no máximo um interesse por jogo, com restrições no banco.
- Escrita em lançamentos exige ADMINISTRADOR ou DESENVOLVEDOR vinculado à desenvolvedora do jogo ativo. GET de lista/detalhe é público; interesse e notificações exigem autenticação.
- A F9 exige data futura em cadastro e edição. Alterações pelo catálogo ou importação sincronizam jogo, lançamento, histórico e alertas na mesma transação. Se uma data confirmada já foi alcançada, a rotina marca LANCADO.
- Exclusão da agenda remove interesses/alertas, sem excluir o jogo do catálogo. Desativar o jogo cancela alertas e o retira da agenda pública.
- Notificações internas avisam mudança de data e proximidade do lançamento; não há email ou push. Entrega e marcação de alerta como enviado ocorrem na mesma transação, com proteção contra duplicação.

## API e pontos de entrada

| Rota | Uso |
|---|---|
| GET /api/lancamentos e GET /api/lancamentos/{id} | Lista pública ordenada e detalhe com histórico, tipoData e estado do interesse |
| POST /api/lancamentos | Cadastro com jogoId, dataPrevista e tipoData |
| PUT /api/lancamentos/{id} e DELETE /api/lancamentos/{id} | Edição e exclusão da agenda |
| PUT e DELETE /api/lancamentos/{id}/interesse | Marcar e desmarcar interesse |
| GET /api/notificacoes e PATCH /api/notificacoes/{id}/leitura | Caixa do próprio usuário e leitura |

Contrato completo no README.md. Implementação principal em backend/src/main/java/com/g4/game_tracker_backend/{dominio,aplicacao,apresentacao,infraestrutura/persistencia}/lancamentos, aplicação de notificações no contexto notificacao, e frontend/src/app/features/lancamentos. A sincronização do catálogo entra por SalvarJogoComEvento e SincronizarLancamentosComCatalogo. Cenários: backend/src/test/resources/features/f12_lancamentos.feature.

## Validação após atualizar main

- ./mvnw verify: passou com JDK 21. Inclui 22 cenários F-12 ativos; os 5 mais recentes cobrem datas estimadas.
- npm test -- --watch=false: 2 arquivos e 3 testes passaram com Node 24.
- npm run build -- --configuration development: passou.
- npm run build (produção): ainda falha nos limites de tamanho SCSS de inicio.page.scss e perfil.page.scss. Os arquivos são da interface comum e não fazem parte da implementação F9.
- Prévia local com perfil H2: POST de jogo fictício sem data e lançamento ESTIMADA retornou 201; GET público retornou o lançamento e a tela mostrou “Possível lançamento”. Dados H2 são temporários.

## Para a próxima pessoa

O branch e o main local já apontam para os 9 novos commits de origin/main por fast-forward. A atualização trouxe mudanças nos modelos, controladores e telas do fórum; revisar essas interfaces antes de iniciar F10/F11. As alterações locais da F9 foram preservadas. Há um stash local de segurança chamado “F9 local work before syncing origin/main”; não o reaplicar sobre a árvore atual.

Antes de abrir PR, revisar o diff, commitar os arquivos da F9 e decidir com a equipe como tratar o orçamento SCSS do build de produção. O JDK 21 usado nesta validação está em uma pasta temporária local; cada desenvolvedor precisa preparar seu próprio JDK 21. O plano de checkpoints PLANO_F9.md fica apenas nesta máquina e está no .gitignore; este handoff é o documento compartilhável.
