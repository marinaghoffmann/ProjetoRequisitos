# language: pt
@pendente
Funcionalidade: F-21 Sistema de reputação e avaliações de vendedores e compradores
  Como participante de uma negociação, quero avaliar a outra parte, construindo a
  reputação dos usuários no marketplace.

  # RN 1 - vínculo exclusivo com pedido concluído
  Cenário: Comprador avalia o vendedor após pedido concluído
    Dado um pedido "CONCLUIDO" entre o comprador "joao" e o vendedor "marina"
    Quando "joao" avalia "marina" com 5 estrelas e as tags "ENVIO_RAPIDO, PRODUTO_COMO_DESCRITO"
    Então "marina" deve ter 1 avaliação recebida

  Cenário: Pedido não concluído não pode ser avaliado
    Dado um pedido "ENVIADO" entre o comprador "joao" e o vendedor "marina"
    Quando "joao" avalia "marina" com 5 estrelas
    Então a avaliação deve ser recusada com a mensagem "Só é possível avaliar pedidos concluídos"

  # RN 2 - avaliação única e prazo limite
  Cenário: Segunda avaliação da mesma parte é recusada
    Dado um pedido "CONCLUIDO" entre o comprador "joao" e o vendedor "marina"
    E que "joao" já avaliou "marina" neste pedido
    Quando "joao" avalia "marina" com 3 estrelas
    Então a avaliação deve ser recusada com a mensagem "Você já avaliou este pedido"

  Cenário: Comprador e vendedor avaliam um ao outro no mesmo pedido
    Dado um pedido "CONCLUIDO" entre o comprador "joao" e o vendedor "marina"
    Quando "joao" avalia "marina" com 5 estrelas
    E "marina" avalia "joao" com 4 estrelas
    Então o pedido deve ter 2 avaliações

  Cenário: Avaliação após 15 dias da entrega é recusada
    Dado um pedido "CONCLUIDO" entregue há 16 dias entre "joao" e "marina"
    Quando "joao" avalia "marina" com 4 estrelas
    Então a avaliação deve ser recusada com a mensagem "O prazo de 15 dias para avaliar terminou"

  # RN 3 - recálculo da nota de reputação
  Cenário: Nova avaliação recalcula a reputação e soma negociações
    Dado que "marina" tem reputação 4.00 com 2 avaliações e 2 negociações bem-sucedidas
    E um pedido "CONCLUIDO" entre o comprador "joao" e o vendedor "marina"
    Quando "joao" avalia "marina" com 5 estrelas
    Então a reputação de "marina" deve ser 4.33
    E "marina" deve ter 3 negociações bem-sucedidas

  # Consulta
  Cenário: Consultar avaliações recebidas por um vendedor
    Dado que "marina" recebeu avaliações de 5, 4 e 3 estrelas
    Quando um usuário consulta o perfil de vendedor de "marina"
    Então a consulta deve retornar 3 avaliações e a nota média 4.00
