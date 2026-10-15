# language: pt
Funcionalidade: F-19 Chat interno para negociação e propostas entre comprador e vendedor
  Como comprador, quero conversar com o vendedor e enviar propostas formais; como vendedor,
  quero aceitar ou recusar essas propostas.

  # RN 1 - vinculação da conversa ao anúncio ativo
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

  Cenário: Conversa duplicada para o mesmo anúncio é bloqueada
    Dado o jogador "joao" autenticado
    E que já existe uma conversa entre "joao" e "marina" para o anúncio
    Quando o comprador inicia uma conversa sobre o anúncio
    Então deve existir 1 conversa entre "joao" e "marina" para o anúncio

  # RN 2 - formalização e bloqueio de propostas
  Cenário: Proposta enviada fica pendente
    Dado uma conversa entre "joao" e "marina" para um anúncio ativo
    Quando "joao" envia uma proposta de R$ 120,00
    Então a proposta deve ter status "PENDENTE"

  Cenário: Vendedor aceita a proposta e o anúncio é reservado
    Dado uma conversa entre "joao" e "marina" com uma proposta pendente de R$ 120,00
    Quando "marina" aceita a proposta
    Então a proposta deve ter status "ACEITA"
    E o anúncio deve ter status "EM_NEGOCIACAO"

  Cenário: Vendedor recusa a proposta
    Dado uma conversa entre "joao" e "marina" com uma proposta pendente de R$ 120,00
    Quando "marina" recusa a proposta
    Então a proposta deve ter status "RECUSADA"
    E o anúncio deve ter status "ATIVO"

  Cenário: Comprador oferece troca por outro jogo
    Dado uma conversa entre "joao" e "marina" para um anúncio ativo
    Quando "joao" envia uma proposta de troca pelo jogo "Celeste"
    Então a proposta deve ser do tipo "TROCA" com status "PENDENTE"

  # RN 3 - restrição de visualização das mensagens
  Cenário: Terceiro não visualiza as mensagens da conversa
    Dado uma conversa entre "joao" e "marina" com 4 mensagens
    E o jogador "lucas" autenticado
    Quando o usuário tenta ler as mensagens da conversa
    Então o acesso deve ser recusado com a mensagem "Conversa restrita aos participantes"

  # Consulta
  Cenário: Listar conversas com status da última proposta e não lidas
    Dado o jogador "marina" autenticado
    E que "marina" participa de 2 conversas, uma com 3 mensagens não lidas
    Quando o jogador consulta suas conversas ativas
    Então a consulta deve retornar 2 conversas com o status da última proposta
    E uma conversa deve indicar 3 mensagens não lidas
