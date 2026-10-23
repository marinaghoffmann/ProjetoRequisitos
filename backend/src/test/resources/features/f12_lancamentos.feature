# language: pt
@pendente
Funcionalidade: F-12 Gerenciamento de lançamentos futuros, agenda de alertas e pré-cadastros
  Como administrador ou criador, quero cadastrar lançamentos futuros; como jogador, quero
  ser avisado antes do lançamento dos jogos que me interessam.

  # RN 1 - validação de data e transição automática de status
  Cenário: Cadastrar lançamento com data futura
    Dado um usuário com papel "ADMINISTRADOR"
    Quando o administrador cadastra o lançamento de "GTA VI" para daqui a 30 dias
    Então o lançamento de "GTA VI" deve ter status "FUTURO_LANCAMENTO"

  Cenário: Lançamento com data no passado é recusado
    Dado um usuário com papel "ADMINISTRADOR"
    Quando o administrador cadastra o lançamento de "GTA VI" para ontem
    Então o cadastro deve ser recusado com a mensagem "A data de lançamento deve ser futura"

  Cenário: Status muda para Lançado quando a data é atingida
    Dado que o lançamento de "GTA VI" está previsto para hoje
    Quando a rotina de transição de lançamentos é executada
    Então o lançamento de "GTA VI" deve ter status "LANCADO"

  # RN 2 - gestão do interesse e envio de alertas
  Cenário: Marcar interesse agenda notificação 24 horas antes
    Dado o jogador "marina" autenticado
    E que o lançamento de "GTA VI" está previsto para daqui a 10 dias
    Quando o jogador marca "Quero jogar no lançamento" em "GTA VI"
    Então deve existir uma notificação agendada para 24 horas antes do lançamento de "GTA VI"

  Cenário: Desmarcar o interesse cancela o alerta
    Dado o jogador "marina" autenticado
    E que o jogador marcou interesse no lançamento de "GTA VI"
    Quando o jogador desmarca "Quero jogar no lançamento" em "GTA VI"
    Então o jogador não deve ter alerta ativo para "GTA VI"

  Cenário: Notificações são disparadas aos inscritos
    Dado que 3 jogadores marcaram interesse no lançamento de "GTA VI"
    E que faltam 24 horas para o lançamento de "GTA VI"
    Quando a rotina de notificações é executada
    Então 3 notificações de lançamento devem ser enviadas

  # RN 3 - edição de cronograma por administradores ou criadores
  Cenário: Adiar o lançamento registra histórico e avisa os interessados
    Dado um usuário com papel "ADMINISTRADOR"
    E que 2 jogadores marcaram interesse no lançamento de "GTA VI"
    Quando o administrador altera a data de "GTA VI" para daqui a 60 dias
    Então o histórico de alterações de "GTA VI" deve ter 1 registro
    E 2 jogadores devem ser avisados da nova data

  Cenário: Jogador comum não altera o cronograma
    Dado o jogador "marina" autenticado
    Quando o usuário tenta alterar a data de "GTA VI"
    Então a operação deve ser recusada com a mensagem "Apenas administradores ou criadores podem alterar lançamentos"

  # Consulta
  Cenário: Lançamentos listados por data com destaque do alerta do usuário
    Dado o jogador "marina" autenticado
    E os lançamentos "Jogo X" em 20 dias, "Jogo Y" em 5 dias e "Jogo Z" em 12 dias
    E que o jogador marcou interesse em "Jogo Z"
    Quando o jogador consulta os lançamentos futuros
    Então os lançamentos devem aparecer na ordem "Jogo Y", "Jogo Z", "Jogo X"
    E apenas "Jogo Z" deve estar destacado com alerta ativo
