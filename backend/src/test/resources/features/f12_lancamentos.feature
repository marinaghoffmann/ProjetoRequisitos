# language: pt
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
    E o lançamento de "GTA VI" deve aparecer na consulta que inclui lançados

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
    E "Jogo Z" deve mostrar 1 interessado na consulta

  # Casos de integração e idempotência
  Cenário: Desenvolvedor vinculado pode alterar o cronograma
    Dado que o lançamento de "Jogo do estúdio" está previsto para daqui a 10 dias
    E um desenvolvedor vinculado ao jogo "Jogo do estúdio" autenticado
    Quando o responsável altera o lançamento para daqui a 20 dias
    Então o histórico de alterações de "Jogo do estúdio" deve ter 1 registro
    E o jogo "Jogo do estúdio" deve ter a mesma data do lançamento

  Cenário: Marcar interesse duas vezes não duplica o alerta
    Dado o jogador "marina" autenticado
    E que o lançamento de "Jogo repetido" está previsto para daqui a 10 dias
    Quando o jogador marca interesse duas vezes em "Jogo repetido"
    Então deve haver apenas um alerta pendente para "Jogo repetido"

  Cenário: Reexecutar rotina não duplica avisos
    Dado que 3 jogadores marcaram interesse no lançamento de "Jogo idempotente"
    E que faltam 24 horas para o lançamento de "Jogo idempotente"
    Quando a rotina de notificações é executada duas vezes
    Então cada jogador recebe apenas um aviso de lançamento

  Cenário: Alteração pelo catálogo sincroniza histórico e alertas
    Dado um usuário com papel "ADMINISTRADOR"
    E que o lançamento de "Jogo editado" está previsto para daqui a 10 dias
    E que 2 jogadores marcaram interesse no lançamento de "Jogo editado"
    Quando o catálogo altera a data de "Jogo editado" para daqui a 20 dias
    Então o histórico de alterações de "Jogo editado" deve ter 1 registro
    E o jogo "Jogo editado" deve ter a mesma data do lançamento
    E os alertas dos interessados devem seguir a nova data

  Cenário: Importação de jogo existente sincroniza o lançamento
    Dado um usuário com papel "ADMINISTRADOR"
    E um jogo importado com lançamento inscrito para daqui a 10 dias
    Quando a importação atualiza a data do jogo para daqui a 20 dias
    Então o histórico de alterações do jogo importado deve ter 1 registro
    E o jogo importado deve ter a mesma data do lançamento

  Cenário: Data já atingida pelo catálogo encerra a agenda
    Dado um usuário com papel "ADMINISTRADOR"
    E que o lançamento de "Jogo lançado" está previsto para daqui a 10 dias
    E que 2 jogadores marcaram interesse no lançamento de "Jogo lançado"
    Quando o catálogo altera a data de "Jogo lançado" para hoje
    Então o lançamento de "Jogo lançado" deve ter status "LANCADO"
    E não deve haver alertas pendentes para os interessados

  Cenário: Excluir lançamento preserva o jogo e cancela a agenda
    Dado um usuário com papel "ADMINISTRADOR"
    E que o lançamento de "Jogo preservado" está previsto para daqui a 10 dias
    E que 2 jogadores marcaram interesse no lançamento de "Jogo preservado"
    Quando o administrador exclui o lançamento de "Jogo preservado"
    Então o jogo "Jogo preservado" continua no catálogo
    E não deve haver alertas pendentes para os interessados

  Cenário: Desativar jogo cancela alertas do lançamento
    Dado um usuário com papel "ADMINISTRADOR"
    E que o lançamento de "Jogo inativo" está previsto para daqui a 10 dias
    E que 2 jogadores marcaram interesse no lançamento de "Jogo inativo"
    Quando o catálogo desativa o jogo "Jogo inativo"
    Então não deve haver alertas pendentes para os interessados
    E o lançamento de "Jogo inativo" não deve aparecer na agenda pública

  # Datas estimadas informadas por criadores ou administradores
  Cenário: Jogo sem data no catálogo recebe uma possível data na agenda pública
    Dado um usuário com papel "ADMINISTRADOR"
    Quando o administrador cadastra uma data estimada para "Jogo incerto" daqui a 10 dias
    Então "Jogo incerto" aparece na agenda pública com data estimada
    E o jogo "Jogo incerto" deve ter a mesma data do lançamento

  Cenário: Data estimada não envia alerta nem encerra o lançamento automaticamente
    Dado o jogador "marina" autenticado
    E que o lançamento de "Jogo incerto" tem data estimada para ontem
    Quando o jogador marca "Quero jogar no lançamento" em "Jogo incerto"
    E a rotina de transição de lançamentos é executada
    Então o lançamento de "Jogo incerto" deve ter status "FUTURO_LANCAMENTO"
    E o interesse em "Jogo incerto" não deve ter alerta pendente
    E "Jogo incerto" aparece na agenda pública com data estimada

  Cenário: Confirmar uma data estimada agenda os alertas dos interessados
    Dado um usuário com papel "ADMINISTRADOR"
    E que o lançamento de "Jogo por confirmar" tem data estimada para daqui a 10 dias
    E que 2 jogadores marcaram interesse sem alerta em "Jogo por confirmar"
    Quando o administrador confirma a data de "Jogo por confirmar"
    Então os alertas dos interessados devem seguir a nova data

  Cenário: Edição do catálogo preserva a estimativa e não agenda alerta
    Dado um usuário com papel "ADMINISTRADOR"
    E que o lançamento de "Jogo incerto" tem data estimada para daqui a 10 dias
    E que 2 jogadores marcaram interesse sem alerta em "Jogo incerto"
    Quando o catálogo altera a data de "Jogo incerto" para daqui a 20 dias
    Então o lançamento de "Jogo incerto" mantém a data estimada
    E não deve haver alertas pendentes para os interessados

  Cenário: Reclassificar data confirmada como estimativa cancela alerta pendente
    Dado um usuário com papel "ADMINISTRADOR"
    E que o lançamento de "Jogo reavaliado" está previsto para daqui a 10 dias
    E que 2 jogadores marcaram interesse no lançamento de "Jogo reavaliado"
    Quando o administrador reclassifica a data de "Jogo reavaliado" como estimada
    Então não deve haver alertas pendentes para os interessados
    E o lançamento de "Jogo reavaliado" mantém a data estimada
