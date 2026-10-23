# language: pt
@pendente
Funcionalidade: F-10 Engine de sugestão de similares e central de feedback de relevância
  Como jogador, quero ver jogos parecidos com os que gosto e avaliar se as sugestões são
  relevantes; como administrador, quero ajustar manualmente as conexões.

  # RN 1 - impedir auto-similaridade e simetria
  Cenário: A similaridade é gravada nos dois sentidos
    Dado os jogos "Hades" e "Dead Cells" com gêneros e tags em comum
    Quando o sistema calcula a similaridade de "Hades"
    Então "Dead Cells" deve aparecer como similar a "Hades"
    E "Hades" deve aparecer como similar a "Dead Cells" com o mesmo grau

  Cenário: Um jogo nunca é similar a ele mesmo
    Quando o sistema calcula a similaridade de "Hades"
    Então "Hades" não deve aparecer na lista de similares de "Hades"

  # RN 2 - recálculo dinâmico pelo feedback da comunidade
  Cenário: Jogador registra feedback de uma sugestão
    Dado o jogador "marina" autenticado
    E que "Dead Cells" é sugerido como similar a "Hades"
    Quando o jogador marca a sugestão "Hades" -> "Dead Cells" como "UTIL"
    Então a sugestão deve ter 1 feedback "UTIL"

  Cenário: Segundo feedback do mesmo jogador para o par é recusado
    Dado o jogador "marina" autenticado
    E que o jogador já deu feedback "UTIL" para a sugestão "Hades" -> "Dead Cells"
    Quando o jogador marca a sugestão "Hades" -> "Dead Cells" como "NAO_RELEVANTE"
    Então o feedback deve ser recusado com a mensagem "Você já avaliou esta sugestão"

  Cenário: Jogador edita e remove seu feedback
    Dado o jogador "marina" autenticado
    E que o jogador já deu feedback "UTIL" para a sugestão "Hades" -> "Dead Cells"
    Quando o jogador altera o feedback para "NAO_RELEVANTE"
    E o jogador remove o seu feedback
    Então a sugestão deve ter 0 feedbacks do jogador

  Cenário: Mais de 15 feedbacks negativos rebaixam a relevância
    Dado que a sugestão "Hades" -> "Tetris" tem grau de relevância 0.80 e 15 feedbacks "NAO_RELEVANTE"
    Quando outro jogador marca a sugestão "Hades" -> "Tetris" como "NAO_RELEVANTE"
    Então o grau de relevância da sugestão deve ser menor que 0.80
    E a sugestão deve estar marcada como rebaixada

  Cenário: Exatamente 15 feedbacks negativos não rebaixam a relevância
    Dado que a sugestão "Hades" -> "Tetris" tem grau de relevância 0.80 e 14 feedbacks "NAO_RELEVANTE"
    Quando outro jogador marca a sugestão "Hades" -> "Tetris" como "NAO_RELEVANTE"
    Então o grau de relevância da sugestão deve ser 0.80

  # RN 3 - moderação e override manual
  Cenário: Administrador cria uma similaridade manual
    Dado um usuário com papel "ADMINISTRADOR"
    Quando o administrador cria uma similaridade manual entre "Hades" e "Bastion" com grau 0.95
    Então "Bastion" deve aparecer como similar a "Hades" com origem "MANUAL"

  Cenário: Jogador comum não cria similaridade manual
    Dado o jogador "marina" autenticado
    Quando o usuário tenta criar uma similaridade manual entre "Hades" e "Bastion"
    Então a operação deve ser recusada com a mensagem "Apenas administradores podem ajustar similaridades"

  # Consulta
  Cenário: Consultar similares ordenados por relevância
    Dado que "Hades" possui similares "A" com grau 0.60, "B" com grau 0.90 e "C" com grau 0.75
    Quando o usuário consulta os similares de "Hades"
    Então os similares devem aparecer na ordem "B", "C", "A"
