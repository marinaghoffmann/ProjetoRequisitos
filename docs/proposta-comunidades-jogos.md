# Proposta para a equipe — Comunidades de jogos

Responsável pela proposta: Henrique Brito. Estado: **proposta; ainda não integra a F10/F-13 nem a F11/F-14**.

## Decisões alinhadas na conversa

- O fórum atual permanece como espaço global de discussão. Tópicos gerais e tópicos vinculados a jogos continuam funcionando sem associação ou inscrição em uma comunidade.
- A navegação atual chama `/forum` de “Comunidade” por causa do protótipo da F10; conceitualmente essa tela continua sendo o fórum global. A equipe deverá decidir o rótulo da navegação quando existir uma página própria para comunidades de jogos.
- Uma comunidade é um espaço próprio de **um jogo do catálogo**, com identidade, participantes e discussões no estilo do fórum. Para a primeira entrega, a criação de comunidades por usuários fica de fora; comunidades criadas pelos próprios usuários poderão ser estudadas depois.
- O usuário escolhe em quais comunidades de jogos participa. Um tópico criado dentro de uma comunidade aparece no espaço dela; ele não é copiado automaticamente para o fórum global.
- Uma comunidade pode ser pública ou privada. Em comunidades privadas, **pedido de ingresso aprovado** e **convite aceito** são duas formas de entrada, não dois tipos separados de comunidade.
- Tópico `ENCERRADO` é uma regra da discussão. Comunidade privada é uma regra de acesso ao espaço inteiro; os conceitos são independentes.

## Experiência proposta

1. A página de um jogo apresenta sua comunidade, quando existir. Uma lista de comunidades permite descobrir comunidades de jogos; **Minhas comunidades** reúne as que o usuário escolheu acompanhar.
2. A página da comunidade mostra o jogo, descrição, regras, participantes e uma lista de tópicos com as interações já conhecidas do fórum: criação, respostas encadeadas, Markdown, curtidas, filtros, paginação e ordenação.
3. No fórum global (`/forum`), o fluxo e as rotas existentes continuam. O usuário escolhe explicitamente se cria um tópico no fórum global ou dentro de uma comunidade.
4. Visitantes podem descobrir e ler comunidades públicas. Para participar, o usuário autenticado entra na comunidade. Uma comunidade privada só revela suas discussões aos membros.
5. Em comunidade privada visível, o usuário solicita ingresso e acompanha o estado do pedido. Um gestor aprova ou rejeita. Um convite direcionado ao usuário pode ser aceito ou recusado. Uma comunidade privada oculta não aparece na descoberta pública e depende de convite.

## Regras e dados sugeridos para o planejamento técnico

| Conceito | Papel |
|---|---|
| `ComunidadeJogo` | Identidade, jogo vinculado, descrição, regras e visibilidade pública/privada. |
| `ParticipacaoComunidade` | Vínculo único usuário/comunidade, papel (gestor, moderador ou membro) e data de ingresso. |
| `PedidoIngresso` | Solicitação pendente, aprovada ou rejeitada para uma comunidade privada. |
| `ConviteComunidade` | Convite para um usuário específico, com estado de aceite/recusa. |
| `TopicoDeForum.comunidadeId` | Vínculo opcional: nulo para o fórum global; preenchido para uma discussão da comunidade. O `jogoId` continua representando o jogo citado pelo tópico. |

Recomendações a validar com a equipe:

- Na primeira entrega, permitir **uma comunidade oficial por jogo** e criação/gestão inicial pela administração da plataforma. Definir depois se desenvolvedores verificados também poderão geri-la.
- Em comunidades públicas, exigir ingresso para publicar e responder; leitura continua aberta. Em comunidades privadas, exigir participação aprovada também para consultas de tópicos e respostas.
- Fazer as verificações de visibilidade no backend, incluindo listagens, detalhe e contagens, para evitar vazamento de conteúdo privado por ID ou pela busca do fórum global.
- Reutilizar o modelo de tópicos e respostas da F10 e o serviço de notificações existente para pedidos e convites. Integrar denúncias, punições e auditoria da F11 quando essa funcionalidade estiver pronta.
- Preservar os tópicos existentes como tópicos globais na migração. Nenhum tópico antigo deve entrar automaticamente em uma comunidade.

## Limite entre entregas

- **F10/F-13:** fórum global, tópicos e respostas, Markdown, curtidas e ordenação. Não inclui participação em comunidades.
- **F11/F-14:** denúncias, ações moderativas e auditoria do conteúdo, conforme contrato existente.
- **Nova funcionalidade a priorizar pela equipe:** comunidades de jogos, participação, privacidade, pedidos e convites. O contrato CML, os cenários de aceitação, as rotas e a numeração precisam de aprovação conjunta antes da implementação.
- **Etapa posterior:** criação de comunidades por usuários e comunidades de assuntos não vinculados a um jogo.

## Critérios de aceite propostos

- Um usuário encontra a comunidade de um jogo, entra e vê essa comunidade em **Minhas comunidades**; pode sair dela.
- O fórum global continua acessível e mantém seus tópicos atuais. Um tópico da comunidade aparece apenas no espaço daquela comunidade.
- Uma comunidade pública pode ser lida sem ingresso; somente participantes publicam.
- Em uma comunidade privada, não membros não conseguem ler tópicos nem respostas, mesmo usando a API diretamente.
- Um pedido de ingresso só concede acesso após aprovação. Convites só concedem acesso após aceite do destinatário. Requisições repetidas não criam participações duplicadas.
- Um gestor consegue consultar e decidir pedidos; as regras de tópico encerrado, autoria e edição da F10 seguem válidas dentro da comunidade.

## Decisões pendentes para a equipe

1. Quem pode criar e administrar a primeira comunidade de cada jogo: somente administração ou também desenvolvedores verificados?
2. Comunidades privadas poderão ser encontradas na busca, com nome e descrição públicos, ou cada gestor escolherá entre visível e oculta?
3. Qual será a identificação/numeração da nova funcionalidade no roadmap e quem será responsável por ela?
