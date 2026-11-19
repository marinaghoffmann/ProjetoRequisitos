# language: pt
Funcionalidade: F-07 Marcação de Currently Playing
  Como jogador, quero destacar no perfil os jogos que estou jogando e o meu progresso
  em cada um.

  Contexto:
    Dado o jogador "marina" autenticado

  # RN 3 - registro de data de início da jogatina
  Cenário: Marcar um jogo registra a data e a hora de início
    Quando o jogador marca o jogo "Elden Ring" como Currently Playing
    Então o jogo "Elden Ring" deve aparecer no Currently Playing do jogador
    E o registro deve ter a data e a hora de início preenchidas

  Cenário: Atualizar o progresso de um jogo em andamento
    Dado que o jogador tem o jogo "Elden Ring" em Currently Playing com progresso "NO_INICIO"
    Quando o jogador altera o progresso de "Elden Ring" para "NA_METADE"
    Então o progresso de "Elden Ring" deve ser "NA_METADE"

  # RN 1 - limite de jogos simultâneos
  # RN 2 - pedir remoção ou substituição ao atingir o limite
  Cenário: Marcar o sexto jogo é bloqueado e exige substituição
    Dado que o jogador tem 5 jogos em Currently Playing
    Quando o jogador marca o jogo "Celeste" como Currently Playing
    Então a marcação deve ser recusada com a mensagem "Remova ou substitua um jogo para continuar"
    E o jogador deve continuar com 5 jogos em Currently Playing

  Cenário: Substituir um jogo quando o limite foi atingido
    Dado que o jogador tem 5 jogos em Currently Playing, entre eles "Elden Ring"
    Quando o jogador substitui "Elden Ring" por "Celeste"
    Então o jogador deve continuar com 5 jogos em Currently Playing
    E o jogo "Celeste" deve aparecer no Currently Playing do jogador

  Cenário: Remover do destaque ao concluir o jogo
    Dado que o jogador tem o jogo "Elden Ring" em Currently Playing com progresso "PERTO_DO_FIM"
    Quando o jogador remove "Elden Ring" do Currently Playing
    Então o jogo "Elden Ring" não deve aparecer no Currently Playing do jogador

  # Consulta
  Cenário: Consultar os jogos em andamento no perfil público
    Dado que o jogador tem 3 jogos em Currently Playing
    Quando outro usuário consulta o perfil público do jogador
    Então o perfil deve exibir 3 jogos em Currently Playing
