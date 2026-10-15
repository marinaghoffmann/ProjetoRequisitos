# language: pt
Funcionalidade: F-08 Gerenciamento de jogos favoritos e personalização do perfil público
  Como jogador, quero fixar meus jogos favoritos e personalizar biografia e capa do meu
  perfil público.

  Contexto:
    Dado o jogador "marina" autenticado

  Cenário: Adicionar um favorito em um slot
    Quando o jogador fixa o jogo "Hollow Knight" no slot 1
    Então o slot 1 do perfil deve conter "Hollow Knight"

  # RN 1 - limite e reordenação dos slots
  Cenário: Não é possível ter mais de 4 favoritos
    Dado que o jogador tem os 4 slots de favoritos ocupados
    Quando o jogador fixa o jogo "Celeste" como favorito sem escolher slot
    Então a operação deve ser recusada com a mensagem "Você já possui 4 favoritos"

  Cenário: Mover um favorito reordena os demais
    Dado que o jogador tem os favoritos "A" no slot 1, "B" no slot 2, "C" no slot 3 e "D" no slot 4
    Quando o jogador move o favorito "D" para o slot 1
    Então os favoritos devem ficar na ordem "D", "A", "B", "C"

  Cenário: Remover um favorito
    Dado que o jogador tem os favoritos "A" no slot 1, "B" no slot 2, "C" no slot 3 e "D" no slot 4
    Quando o jogador remove o favorito "B"
    Então os favoritos devem ficar na ordem "A", "C", "D"

  # RN 2 - unicidade de jogo por slot
  Cenário: O mesmo jogo não pode ocupar dois slots
    Dado que o jogador tem o jogo "Hollow Knight" no slot 1
    Quando o jogador fixa o jogo "Hollow Knight" no slot 2
    Então a operação deve ser recusada com a mensagem "Este jogo já está entre os seus favoritos"

  # RN 3 - sanitização da biografia e mídias do perfil
  Cenário: Biografia com até 500 caracteres é salva
    Quando o jogador edita a biografia com um texto de 500 caracteres
    Então a biografia deve ser salva no perfil

  Cenário: Biografia acima de 500 caracteres é recusada
    Quando o jogador edita a biografia com um texto de 501 caracteres
    Então a operação deve ser recusada com a mensagem "A biografia pode ter no máximo 500 caracteres"

  Esquema do Cenário: Validação da extensão da capa do perfil
    Quando o jogador define a capa do perfil com a URL "<url>"
    Então a capa deve ser <resultado>

    Exemplos:
      | url                              | resultado |
      | https://cdn.exemplo.com/capa.png  | aceita    |
      | https://cdn.exemplo.com/capa.jpg  | aceita    |
      | https://cdn.exemplo.com/capa.webp | aceita    |
      | https://cdn.exemplo.com/capa.gif  | recusada  |
      | capa-sem-url.png                  | recusada  |

  # Consulta
  Cenário: Consultar o perfil público com favoritos ordenados
    Dado que o jogador tem os favoritos "A" no slot 2 e "B" no slot 1
    Quando outro usuário consulta o perfil público do jogador
    Então os favoritos devem ficar na ordem "B", "A"
