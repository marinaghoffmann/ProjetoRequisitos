# language: pt
Funcionalidade: F-19 Chat interno para negociação e propostas entre comprador e vendedor
  Como comprador, quero conversar com o vendedor e enviar propostas formais; como vendedor,
  quero aceitar ou recusar essas propostas.

  # RN 1 - conversa só a partir de anúncio ativo
  Cenário: Comprador inicia conversa a partir de anúncio ativo
    Dado o jogador "joao" autenticado
    E um anúncio "ATIVO" do vendedor "marina"
    Quando o comprador inicia uma conversa sobre o anúncio
    Então deve existir 1 conversa entre "joao" e "marina" para o anúncio

  Cenário: Não é possível iniciar conversa em anúncio pausado
    Dado o jogador "joao" autenticado
    E um anúncio "PAUSADO" do vendedor "marina"
    Quando o comprador inicia uma conversa sobre o anúncio
    Então a operação deve ser recusada com a mensagem "Só é possível negociar anúncios ativos"

  Cenário: Vendedor não negocia no próprio anúncio
    Dado o jogador "marina" autenticado
    E um anúncio "ATIVO" do vendedor "marina"
    Quando o comprador inicia uma conversa sobre o anúncio
    Então a operação deve ser recusada com a mensagem "Você não pode negociar o próprio anúncio"

  # RN 2 - sem conversas duplicadas
  Cenário: Abrir de novo a conversa reabre a existente
    Dado o jogador "joao" autenticado
    E que já existe uma conversa entre "joao" e "marina" para o anúncio
    Quando o comprador inicia uma conversa sobre o anúncio
    Então deve existir 1 conversa entre "joao" e "marina" para o anúncio
    E a conversa aberta deve ser a que já existia

  # RN 3 - mensagens restritas aos participantes
  Cenário: Terceiro não visualiza as mensagens da conversa
    Dado uma conversa entre "joao" e "marina" com 4 mensagens
    E o jogador "lucas" autenticado
    Quando o usuário tenta ler as mensagens da conversa
    Então o acesso deve ser recusado com a mensagem "Conversa restrita aos participantes"

  Cenário: Terceiro não envia mensagens na conversa
    Dado uma conversa entre "joao" e "marina" para um anúncio ativo
    Quando "lucas" envia a mensagem "Também tenho interesse"
    Então o acesso deve ser recusado com a mensagem "Conversa restrita aos participantes"

  # RN 4 - mensagem válida
  Cenário: Mensagem vazia é recusada
    Dado uma conversa entre "joao" e "marina" para um anúncio ativo
    Quando "joao" envia a mensagem "   "
    Então a mensagem deve ser recusada com a mensagem "A mensagem não pode ser vazia"

  Cenário: Mensagem acima de 1000 caracteres é recusada
    Dado uma conversa entre "joao" e "marina" para um anúncio ativo
    Quando "joao" envia uma mensagem com 1001 caracteres
    Então a mensagem deve ser recusada com a mensagem "A mensagem deve ter no máximo 1000 caracteres"

  # RN 5 - conversa somente leitura após encerramento
  Cenário: Conversa de anúncio vendido fica somente leitura
    Dado uma conversa entre "joao" e "marina" para um anúncio ativo
    E que o anúncio da conversa foi vendido
    Quando "joao" envia a mensagem "Ainda está disponível?"
    Então a mensagem deve ser recusada com a mensagem "Anúncio encerrado: a conversa é somente leitura"

  # RN 6 - proposta formal válida
  Cenário: Proposta enviada fica pendente
    Dado uma conversa entre "joao" e "marina" para um anúncio ativo
    Quando "joao" envia uma proposta de R$ 120,00
    Então a proposta deve ter status "PENDENTE"

  Cenário: Comprador oferece troca por outro jogo
    Dado uma conversa entre "joao" e "marina" para um anúncio ativo
    Quando "joao" envia uma proposta de troca pelo jogo "Celeste"
    Então a proposta deve ser do tipo "TROCA" com status "PENDENTE"

  Cenário: Proposta de valor zero é recusada
    Dado uma conversa entre "joao" e "marina" para um anúncio ativo
    Quando "joao" envia uma proposta de R$ 0,00
    Então a proposta deve ser recusada com a mensagem "O valor da proposta deve ser maior que zero"

  Cenário: Vendedor não envia propostas
    Dado uma conversa entre "joao" e "marina" para um anúncio ativo
    Quando "marina" envia uma proposta de R$ 120,00
    Então a proposta deve ser recusada com a mensagem "Apenas o comprador envia propostas"

  # RN 7 - uma proposta pendente por conversa
  Cenário: Segunda proposta pendente é recusada
    Dado uma conversa entre "joao" e "marina" com uma proposta pendente de R$ 120,00
    Quando "joao" envia uma proposta de R$ 130,00
    Então a proposta deve ser recusada com a mensagem "Já existe uma proposta pendente nesta conversa"

  Cenário: Comprador cancela a pendente e envia outra
    Dado uma conversa entre "joao" e "marina" com uma proposta pendente de R$ 120,00
    Quando "joao" cancela a proposta
    E "joao" envia uma proposta de R$ 130,00
    Então a proposta anterior deve ter status "CANCELADA"
    E a proposta deve ter status "PENDENTE"

  # RN 8 - quem responde a proposta
  Cenário: Comprador não aceita a própria proposta
    Dado uma conversa entre "joao" e "marina" com uma proposta pendente de R$ 120,00
    Quando "joao" aceita a proposta
    Então a operação deve ser recusada com a mensagem "Apenas o vendedor pode aceitar ou recusar a proposta"

  Cenário: Vendedor recusa a proposta
    Dado uma conversa entre "joao" e "marina" com uma proposta pendente de R$ 120,00
    Quando "marina" recusa a proposta
    Então a proposta deve ter status "RECUSADA"
    E o anúncio negociado deve ficar "ATIVO"

  Cenário: Proposta já respondida não muda mais de status
    Dado uma conversa entre "joao" e "marina" com uma proposta pendente de R$ 120,00
    E que "marina" recusou a proposta
    Quando "marina" aceita a proposta
    Então a operação deve ser recusada com a mensagem "A proposta não está mais pendente"

  # RN 9 - aceite reserva o item
  Cenário: Vendedor aceita a proposta e o anúncio é reservado
    Dado uma conversa entre "joao" e "marina" com uma proposta pendente de R$ 120,00
    Quando "marina" aceita a proposta
    Então a proposta deve ter status "ACEITA"
    E o anúncio negociado deve ficar "EM_NEGOCIACAO"

  Cenário: Aceite recusa as propostas pendentes de outros compradores
    Dado uma conversa entre "joao" e "marina" com uma proposta pendente de R$ 120,00
    E que "lucas" também enviou uma proposta de R$ 110,00 para o mesmo anúncio
    Quando "marina" aceita a proposta
    Então a proposta de "lucas" deve ter status "RECUSADA" com o aviso "item reservado para outro comprador"

  # RN 10 - prazo de 48 horas para pagar
  Cenário: Reserva sem pedido expira após 48 horas
    Dado uma conversa entre "joao" e "marina" com uma proposta aceita há 49 horas
    Quando a rotina de expiração de reservas é executada
    Então a proposta deve ter status "EXPIRADA"
    E o anúncio negociado deve ficar "ATIVO"

  Cenário: Reserva dentro do prazo continua valendo
    Dado uma conversa entre "joao" e "marina" com uma proposta aceita há 47 horas
    Quando a rotina de expiração de reservas é executada
    Então a proposta deve ter status "ACEITA"
    E o anúncio negociado deve ficar "EM_NEGOCIACAO"

  Cenário: Vendedor libera a reserva antes do prazo
    Dado uma conversa entre "joao" e "marina" com uma proposta aceita há 2 horas
    Quando "marina" libera a reserva
    Então a proposta deve ter status "EXPIRADA"
    E o anúncio negociado deve ficar "ATIVO"

  # RN 11 - controle de mensagens não lidas
  Cenário: Abrir a conversa marca as mensagens recebidas como lidas
    Dado uma conversa entre "joao" e "marina" com 3 mensagens de "joao" não lidas
    Quando "marina" abre a conversa
    Então "marina" deve ter 0 mensagens não lidas
    E "joao" deve ter 0 mensagens não lidas

  # Consulta
  Cenário: Listar conversas com status da última proposta e não lidas
    Dado o jogador "marina" autenticado
    E que "marina" participa de 2 conversas, uma com 3 mensagens não lidas
    Quando o jogador consulta suas conversas ativas
    Então a consulta deve retornar 2 conversas com o status da última proposta
    E uma conversa deve indicar 3 mensagens não lidas
    E "marina" deve ter 3 mensagens não lidas
