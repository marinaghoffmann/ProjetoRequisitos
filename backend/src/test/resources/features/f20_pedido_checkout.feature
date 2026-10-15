# language: pt
Funcionalidade: F-20 Processamento do pedido, checkout de pagamento e confirmação de entrega
  Como comprador, quero pagar com segurança e só liberar o valor ao receber o produto; como
  vendedor, quero receber após a entrega confirmada.

  Contexto:
    Dado uma proposta aceita de R$ 120,00 entre o comprador "joao" e o vendedor "marina"

  Esquema do Cenário: Gerar pedido com cada forma de pagamento
    Quando "joao" gera o pedido com pagamento "<forma>" e endereço de entrega válido
    E o pagamento é confirmado
    Então o pedido deve ter status "PAGO"
    E o valor de R$ 120,00 deve ficar "EM_CUSTODIA"

    Exemplos:
      | forma           |
      | CARTAO_SIMULADO |
      | PIX             |
      | SALDO_CARTEIRA  |

  Cenário: Pagamento com saldo insuficiente na carteira é recusado
    Dado que "joao" tem R$ 50,00 na carteira
    Quando "joao" gera o pedido com pagamento "SALDO_CARTEIRA" e endereço de entrega válido
    Então o pagamento deve ser recusado com a mensagem "Saldo insuficiente na carteira"

  # RN 1 - retenção do valor em custódia
  Cenário: Confirmação de recebimento libera o valor ao vendedor
    Dado um pedido "ENTREGUE" de R$ 120,00 com valor em custódia
    Quando "joao" confirma o recebimento
    Então o valor deve ser creditado na carteira de "marina"
    E o pedido deve ter status "CONCLUIDO"

  Cenário: Valor é liberado automaticamente após 7 dias da entrega
    Dado um pedido "ENTREGUE" há 8 dias com valor em custódia
    Quando a rotina de liberação de custódia é executada
    Então o valor deve ser creditado na carteira de "marina"

  Cenário: Valor continua em custódia antes de 7 dias
    Dado um pedido "ENTREGUE" há 3 dias com valor em custódia
    Quando a rotina de liberação de custódia é executada
    Então o valor deve continuar "EM_CUSTODIA"

  # RN 2 - obrigatoriedade do código de rastreamento
  Cenário: Vendedor registra o código de rastreio dentro do prazo
    Dado um pedido "PAGO" há 1 dia útil
    Quando "marina" registra o código de rastreio "BR123456789"
    Então o pedido deve ter status "ENVIADO"

  Cenário: Pedido sem rastreio em 3 dias úteis é cancelado e reembolsado
    Dado um pedido "PAGO" há 4 dias úteis sem código de rastreio
    Quando a rotina de prazos de envio é executada
    Então o pedido deve ter status "CANCELADO"
    E o valor deve ser reembolsado a "joao"

  # RN 3 - atualização do anúncio e do journal
  Cenário: Conclusão do pedido marca o anúncio como vendido e registra no journal
    Dado um pedido "ENTREGUE" de R$ 120,00 com valor em custódia
    Quando "joao" confirma o recebimento
    Então o anúncio deve ter status "VENDIDO"
    E o journal de "joao" deve ter 1 entrada automática do tipo "COMPRA_CONCLUIDA"

  # Consulta
  Cenário: Consultar compras e vendas com status
    Dado que "marina" possui 2 vendas e 1 compra
    Quando "marina" consulta os seus pedidos
    Então a consulta deve retornar 2 vendas e 1 compra com status de entrega e pagamento
