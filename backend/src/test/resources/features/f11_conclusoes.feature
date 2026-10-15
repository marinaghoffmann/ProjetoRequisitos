# language: pt
Funcionalidade: F-11 Gerenciamento de registros de "zerado" e contagem de conclusões
  Como jogador, quero registrar, editar e excluir as vezes em que zerei um jogo, para manter
  o histórico e a contagem de conclusões.

  Contexto:
    Dado o jogador "marina" autenticado
    E que existe o jogo "Celeste" no catálogo

  Cenário: Registrar uma conclusão incrementa o contador
    Dado que o jogador zerou "Celeste" 1 vez
    Quando o jogador registra uma conclusão de "Celeste" em "2026-09-10" na plataforma "PC" com 12 horas
    Então o contador de conclusões de "Celeste" do jogador deve ser 2

  # RN 1 - obrigatoriedade de data válida e limite temporal
  Cenário: Conclusão com data futura é recusada
    Quando o jogador registra uma conclusão de "Celeste" com data futura
    Então o registro deve ser recusado com a mensagem "A data de conclusão não pode ser futura"

  Cenário: Conclusão sem data é recusada
    Quando o jogador registra uma conclusão de "Celeste" sem data
    Então o registro deve ser recusado com a mensagem "A data de conclusão é obrigatória"

  # RN 2 - restrição de propriedade para alteração e exclusão
  Cenário: Excluir um registro decrementa o contador
    Dado que o jogador zerou "Celeste" 2 vezes
    Quando o jogador exclui um dos seus registros de conclusão de "Celeste"
    Então o contador de conclusões de "Celeste" do jogador deve ser 1

  Cenário: Jogador não edita registro de outro usuário
    Dado que o jogador "joao" possui um registro de conclusão de "Celeste"
    Quando o jogador "marina" tenta editar o registro de "joao"
    Então a operação deve ser recusada com a mensagem "Apenas o dono pode alterar este registro"

  Cenário: Editar a plataforma de um registro próprio
    Dado que o jogador possui um registro de conclusão de "Celeste" na plataforma "PC"
    Quando o jogador altera a plataforma do registro para "Nintendo Switch"
    Então o registro deve ter a plataforma "Nintendo Switch"

  # RN 3 - validação do tempo de jogo e modo de conclusão
  Esquema do Cenário: Validação das horas jogadas
    Quando o jogador registra uma conclusão de "Celeste" em "2026-09-10" com <horas> horas
    Então o registro deve ser <resultado>

    Exemplos:
      | horas | resultado |
      | 12.5  | aceito    |
      | 0     | recusado  |
      | -3    | recusado  |

  Cenário: Conclusão sem horas jogadas é aceita
    Quando o jogador registra uma conclusão de "Celeste" em "2026-09-10" sem informar horas
    Então o registro deve ser aceito

  Cenário: Platina sem conclusão principal é recusada
    Quando o jogador registra "Celeste" com platina marcada e conclusão principal desmarcada
    Então o registro deve ser recusado com a mensagem "A platina exige que o jogo esteja concluído"

  # Consulta
  Cenário: Histórico de conclusões ordenado da mais recente para a mais antiga
    Dado que o jogador zerou "Celeste" em "2024-01-10", "2026-03-02" e "2025-06-20"
    Quando o jogador consulta o histórico de conclusões de "Celeste"
    Então as conclusões devem aparecer na ordem "2026-03-02", "2025-06-20", "2024-01-10"
