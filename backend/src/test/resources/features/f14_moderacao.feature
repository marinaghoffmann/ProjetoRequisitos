# language: pt
@pendente
Funcionalidade: F-14 Central de moderação, auditoria e denúncias
  Como moderador, quero analisar denúncias e aplicar ações moderativas com registro de
  auditoria, mantendo o fórum saudável.

  Cenário: Jogador denuncia uma resposta
    Dado o jogador "marina" autenticado
    E uma resposta ofensiva publicada por "troll"
    Quando o jogador denuncia a resposta com o motivo "Linguagem ofensiva"
    Então a fila de denúncias pendentes deve conter 1 denúncia

  # RN 1 - permissão exclusiva e justificativa obrigatória
  Cenário: Jogador comum não acessa a central de moderação
    Dado o jogador "marina" autenticado
    Quando o usuário tenta acessar a central de moderação
    Então o acesso deve ser recusado com a mensagem "Acesso restrito a moderadores e administradores"

  Cenário: Ocultar conteúdo sem justificativa é recusado
    Dado um usuário com papel "MODERADOR"
    E uma denúncia pendente sobre uma resposta
    Quando o moderador oculta a resposta sem justificativa
    Então a ação deve ser recusada com a mensagem "A justificativa é obrigatória"

  # RN 2 - auditoria e registro imutável em log
  Cenário: Toda ação moderativa gera registro no log
    Dado um usuário com papel "MODERADOR"
    E uma denúncia pendente sobre uma resposta
    Quando o moderador oculta a resposta com a justificativa "Discurso de ódio"
    Então o log de moderação deve conter 1 registro com a ação "OCULTAR"
    E o registro deve conter o moderador, o conteúdo afetado, a justificativa e a data e hora

  Cenário: Registro do log não pode ser alterado
    Dado um registro existente no log de moderação
    Quando alguém tenta alterar o registro do log
    Então a operação deve ser recusada com a mensagem "Registros de moderação são imutáveis"

  Cenário: Moderador registra advertência a um usuário
    Dado um usuário com papel "MODERADOR"
    Quando o moderador registra uma advertência a "troll" com o motivo "Reincidência"
    Então o usuário "troll" deve ter 1 advertência
    E o log de moderação deve conter 1 registro com a ação "ADVERTIR_USUARIO"

  # RN 3 - exclusão lógica com preservação do fluxo
  Cenário: Remover postagem com respostas mantém a árvore
    Dado um usuário com papel "MODERADOR"
    E uma resposta que possui 2 respostas filhas
    Quando o moderador exclui a resposta com a justificativa "Spam"
    Então o texto da resposta deve ser "Esta postagem foi removida por violar as diretrizes"
    E as 2 respostas filhas devem continuar visíveis

  Cenário: Moderador encerra uma discussão
    Dado um usuário com papel "MODERADOR"
    E um tópico com status "ABERTO"
    Quando o moderador encerra o tópico com a justificativa "Discussão resolvida"
    Então o tópico deve ter status "ENCERRADO"

  # Consulta
  Cenário: Consultar a fila de denúncias pendentes
    Dado um usuário com papel "MODERADOR"
    E 3 denúncias pendentes e 2 já analisadas
    Quando o moderador consulta a fila de denúncias
    Então a consulta deve retornar 3 denúncias
