# language: pt
Funcionalidade: F-20 Processamento do pedido, checkout de pagamento e confirmação de entrega
  Como comprador, quero pagar com segurança e só liberar o valor ao receber o produto; como
  vendedor, quero receber após a entrega confirmada.

  # RN 1 - origem válida do pedido
  Esquema do Cenário: Gerar o pedido da proposta aceita com cada forma de pagamento
    Dado uma proposta aceita de R$ 120,00 entre o comprador "joao" e o vendedor "marina"
    Quando "joao" gera o pedido da proposta com frete "ENVIO_SEGURO" e pagamento "<forma>"
    Então o pedido deve ter status "AGUARDANDO_ENVIO"
    E o pagamento de R$ 138,90 deve ficar "EM_CUSTODIA"

    Exemplos:
      | forma          |
      | CARTAO         |
      | PIX            |
      | SALDO_CARTEIRA |

  Cenário: Compra direta pelo preço anunciado reserva o anúncio
    Dado um anúncio "ATIVO" de R$ 150,00 do vendedor "marina"
    Quando "joao" compra o anúncio direto com frete "EXPRESSO" e pagamento "PIX"
    Então o pedido deve ter status "AGUARDANDO_ENVIO"
    E o pagamento de R$ 182,90 deve ficar "EM_CUSTODIA"
    E o anúncio negociado deve ficar "EM_NEGOCIACAO"

  Cenário: Compra direta recusa as propostas pendentes do anúncio
    Dado uma conversa entre "lucas" e "marina" com uma proposta pendente de R$ 100,00
    Quando "joao" compra o anúncio direto com frete "ENVIO_SEGURO" e pagamento "PIX"
    Então a proposta deve ter status "RECUSADA"

  Cenário: Anúncio só de troca não tem compra direta
    Dado um anúncio só de troca do vendedor "marina"
    Quando "joao" compra o anúncio direto com frete "ENVIO_SEGURO" e pagamento "PIX"
    Então o pedido deve ser recusado com a mensagem "Anúncios só de troca não aceitam compra direta"

  Cenário: Vendedor não compra o próprio anúncio
    Dado um anúncio "ATIVO" de R$ 150,00 do vendedor "marina"
    Quando "marina" compra o anúncio direto com frete "ENVIO_SEGURO" e pagamento "PIX"
    Então o pedido deve ser recusado com a mensagem "Você não pode comprar o próprio anúncio"

  Cenário: Só o comprador da proposta gera o pedido
    Dado uma proposta aceita de R$ 120,00 entre o comprador "joao" e o vendedor "marina"
    Quando "lucas" gera o pedido da proposta com frete "ENVIO_SEGURO" e pagamento "PIX"
    Então o acesso deve ser recusado com a mensagem "Apenas o comprador da proposta pode gerar o pedido"

  Cenário: Proposta de troca aceita gera o pedido pagando só o frete
    Dado uma proposta de troca pelo jogo "Celeste" aceita entre o comprador "joao" e o vendedor "marina"
    Quando "joao" gera o pedido da proposta com frete "ENVIO_SEGURO" e pagamento "PIX"
    Então o pedido deve ter status "AGUARDANDO_ENVIO"
    E o pagamento de R$ 18,90 deve ficar "EM_CUSTODIA"

  # RN 2 - um pedido ativo por anúncio
  Cenário: Anúncio com pedido em andamento não aceita outro pedido
    Dado uma proposta aceita de R$ 120,00 entre o comprador "joao" e o vendedor "marina"
    E que "joao" já gerou o pedido da proposta
    Quando "joao" gera o pedido da proposta com frete "ENVIO_SEGURO" e pagamento "PIX"
    Então o pedido deve ser recusado com a mensagem "Este anúncio já tem um pedido em andamento"

  # RN 3 - endereço de entrega completo
  Cenário: CEP fora do formato é recusado
    Dado uma proposta aceita de R$ 120,00 entre o comprador "joao" e o vendedor "marina"
    Quando "joao" gera o pedido da proposta com o CEP "50000000"
    Então o pedido deve ser recusado com a mensagem "O CEP deve estar no formato 00000-000"

  # RN 4 - cálculo do total com frete
  Esquema do Cenário: Total soma o valor do item e o frete escolhido
    Dado uma proposta aceita de R$ <valor> entre o comprador "joao" e o vendedor "marina"
    Quando "joao" gera o pedido da proposta com frete "<frete>" e pagamento "PIX"
    Então o total do pedido deve ser R$ <total>

    Exemplos:
      | valor  | frete        | total  |
      | 120,00 | ENVIO_SEGURO | 138,90 |
      | 99,99  | EXPRESSO     | 132,89 |

  # RN 5 - validação da forma de pagamento
  Cenário: Cartão com número inválido é recusado
    Dado uma proposta aceita de R$ 120,00 entre o comprador "joao" e o vendedor "marina"
    Quando "joao" paga o pedido da proposta com o cartão "4111111111111112"
    Então o pagamento deve ser recusado com a mensagem "Número do cartão inválido"

  Cenário: Pagamento com saldo insuficiente na carteira é recusado
    Dado uma proposta aceita de R$ 120,00 entre o comprador "joao" e o vendedor "marina"
    E que "joao" tem R$ 50,00 na carteira
    Quando "joao" gera o pedido da proposta com frete "ENVIO_SEGURO" e pagamento "SALDO_CARTEIRA"
    Então o pagamento deve ser recusado com a mensagem "Saldo insuficiente na carteira"
    E o saldo da carteira de "joao" deve ser R$ 50,00

  # RN 6 - carteira criada automaticamente
  Cenário: Carteira nasce com o saldo de simulação e paga a compra
    Dado uma proposta aceita de R$ 120,00 entre o comprador "joao" e o vendedor "marina"
    Quando "joao" gera o pedido da proposta com frete "ENVIO_SEGURO" e pagamento "SALDO_CARTEIRA"
    Então o saldo da carteira de "joao" deve ser R$ 361,10

  # RN 7 - retenção do valor em custódia
  Cenário: Confirmação de recebimento libera o valor ao vendedor
    Dado um pedido "ENVIADO" de R$ 120,00 entre o comprador "joao" e o vendedor "marina"
    Quando "joao" confirma o recebimento
    Então o pedido deve ter status "CONCLUIDO"
    E o pagamento deve ficar "LIBERADO"
    E o saldo da carteira de "marina" deve ser R$ 638,90

  Cenário: Valor é liberado automaticamente 7 dias após o envio
    Dado um pedido "ENVIADO" de R$ 120,00 entre o comprador "joao" e o vendedor "marina"
    E que o pedido foi enviado há 8 dias
    Quando a rotina de liberação de custódia é executada
    Então o pedido deve ter status "CONCLUIDO"
    E o pagamento deve ficar "LIBERADO"

  Cenário: Valor continua em custódia antes de 7 dias
    Dado um pedido "ENVIADO" de R$ 120,00 entre o comprador "joao" e o vendedor "marina"
    E que o pedido foi enviado há 3 dias
    Quando a rotina de liberação de custódia é executada
    Então o pedido deve ter status "ENVIADO"
    E o pagamento deve ficar "EM_CUSTODIA"

  # RN 8 - prazo de 3 dias úteis para envio
  Cenário: Vendedor registra o código de rastreio dentro do prazo
    Dado um pedido "AGUARDANDO_ENVIO" de R$ 120,00 entre o comprador "joao" e o vendedor "marina"
    Quando "marina" registra o código de rastreio "BR123456789"
    Então o pedido deve ter status "ENVIADO"

  Cenário: Só o vendedor registra o envio
    Dado um pedido "AGUARDANDO_ENVIO" de R$ 120,00 entre o comprador "joao" e o vendedor "marina"
    Quando "joao" registra o código de rastreio "BR123456789"
    Então o envio deve ser recusado com a mensagem "Apenas o vendedor registra o envio"

  Cenário: Código de rastreio curto demais é recusado
    Dado um pedido "AGUARDANDO_ENVIO" de R$ 120,00 entre o comprador "joao" e o vendedor "marina"
    Quando "marina" registra o código de rastreio "BR"
    Então o envio deve ser recusado com a mensagem "O código de rastreio deve ter de 3 a 60 caracteres"

  # RN 9 - cancelamento automático com reembolso
  Cenário: Pedido sem rastreio em 3 dias úteis é cancelado e reembolsado
    Dado um pedido "AGUARDANDO_ENVIO" de R$ 120,00 entre o comprador "joao" e o vendedor "marina" pago com "SALDO_CARTEIRA"
    E que o pedido foi pago há 6 dias
    Quando a rotina de prazos de envio é executada
    Então o pedido deve ter status "CANCELADO"
    E o pagamento deve ficar "REEMBOLSADO"
    E o saldo da carteira de "joao" deve ser R$ 500,00
    E o anúncio negociado deve ficar "ATIVO"

  Cenário: Pedido dentro do prazo de envio não é cancelado
    Dado um pedido "AGUARDANDO_ENVIO" de R$ 120,00 entre o comprador "joao" e o vendedor "marina"
    Quando a rotina de prazos de envio é executada
    Então o pedido deve ter status "AGUARDANDO_ENVIO"

  # RN 10 - confirmação de recebimento
  Cenário: Recebimento só pode ser confirmado com o pedido enviado
    Dado um pedido "AGUARDANDO_ENVIO" de R$ 120,00 entre o comprador "joao" e o vendedor "marina"
    Quando "joao" confirma o recebimento
    Então a confirmação deve ser recusada com a mensagem "Só é possível confirmar o recebimento de um pedido enviado"

  Cenário: Só o comprador confirma o recebimento
    Dado um pedido "ENVIADO" de R$ 120,00 entre o comprador "joao" e o vendedor "marina"
    Quando "marina" confirma o recebimento
    Então a confirmação deve ser recusada com a mensagem "Apenas o comprador confirma o recebimento"

  # RN 11 - conclusão atualiza anúncio, journal e estatísticas
  Cenário: Conclusão marca o anúncio como vendido, registra no journal e atualiza as estatísticas
    Dado um pedido "ENVIADO" de R$ 120,00 entre o comprador "joao" e o vendedor "marina"
    Quando "joao" confirma o recebimento
    Então o anúncio negociado deve ficar "VENDIDO"
    E o journal de "joao" deve ter 1 entrada automática do tipo "COMPRA_CONCLUIDA"
    E as estatísticas do jogo devem contar 1 negociação

  # Consulta
  Cenário: Consultar compras e vendas com status
    Dado que "marina" possui 2 vendas e 1 compra
    Quando "marina" consulta os seus pedidos
    Então a consulta deve retornar 2 vendas e 1 compra com status de entrega e pagamento

  Cenário: Pedido restrito ao comprador e ao vendedor
    Dado um pedido "AGUARDANDO_ENVIO" de R$ 120,00 entre o comprador "joao" e o vendedor "marina"
    Quando "lucas" consulta o pedido
    Então o acesso deve ser recusado com a mensagem "Pedido restrito ao comprador e ao vendedor"
