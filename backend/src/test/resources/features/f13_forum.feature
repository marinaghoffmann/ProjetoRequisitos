# language: pt
Funcionalidade: F-13 Gerenciamento completo do fórum de discussões
  Como jogador, quero criar tópicos e responder discussões, editando ou removendo o que eu
  publiquei.

  Contexto:
    Dado o jogador "marina" autenticado

  Cenário: Criar tópico atrelado a um jogo
    Quando o jogador cria o tópico "Melhor build para iniciantes" no jogo "Elden Ring"
    Então o tópico deve ser criado com status "ABERTO" e 0 respostas

  Cenário: Responder a outra resposta (encadeamento)
    Dado um tópico aberto com uma resposta de "joao"
    Quando o jogador responde à resposta de "joao" com "Concordo, foi assim que zerei"
    Então a nova resposta deve estar vinculada à resposta de "joao"

  # RN 1 - bloqueio de alterações em tópicos encerrados
  Cenário: Tópico encerrado não aceita novas respostas
    Dado um tópico com status "ENCERRADO"
    Quando o jogador responde ao tópico com "Alguém ainda joga?"
    Então a resposta deve ser recusada com a mensagem "Este tópico está encerrado"

  Cenário: Tópico encerrado não permite editar respostas
    Dado um tópico com status "ENCERRADO" com uma resposta do jogador
    Quando o jogador edita a sua resposta
    Então a operação deve ser recusada com a mensagem "Este tópico está encerrado"

  # RN 2 - restrição de propriedade e janela de 48 horas
  Cenário: Autor edita sua resposta dentro de 48 horas
    Dado um tópico aberto com uma resposta do jogador publicada há 2 horas
    Quando o jogador edita a sua resposta para "Texto corrigido"
    Então a resposta deve ter o conteúdo "Texto corrigido"

  Cenário: Edição após 48 horas é recusada
    Dado um tópico aberto com uma resposta do jogador publicada há 49 horas
    Quando o jogador edita a sua resposta
    Então a operação deve ser recusada com a mensagem "O prazo de 48 horas para edição terminou"

  Cenário: Usuário não edita postagem de outro autor
    Dado um tópico aberto com uma resposta de "joao"
    Quando o jogador edita a resposta de "joao"
    Então a operação deve ser recusada com a mensagem "Apenas o autor pode alterar esta postagem"

  # RN 3 - atualização do histórico de atividade
  Cenário: Nova resposta atualiza contador e última atividade
    Dado um tópico aberto com 2 respostas
    Quando o jogador responde ao tópico com "Boa discussão"
    Então o tópico deve ter 3 respostas
    E a data da última atividade deve ser a da nova resposta

  Cenário: Excluir resposta atualiza o contador
    Dado um tópico aberto com 3 respostas, sendo uma do jogador
    Quando o jogador exclui a sua resposta
    Então o tópico deve ter 2 respostas

  # Consulta
  Cenário: Listar tópicos de um jogo com paginação
    Dado que o jogo "Elden Ring" possui 25 tópicos
    Quando o usuário consulta a página 1 de tópicos de "Elden Ring" com 10 itens por página
    Então a consulta deve retornar 10 tópicos com número de respostas e última interação
