# language: pt
Funcionalidade: F-17 Engine de agregação de estatísticas e métricas da comunidade
  Como jogador, quero ver na página do jogo as métricas consolidadas da comunidade.

  # RN 1 - recálculo disparado por eventos
  Cenário: Nova review recalcula as médias do jogo
    Dado que o jogo "Hades" possui reviews com notas gerais 4 e 5
    Quando um jogador publica uma review de "Hades" com nota geral 3
    Então a média geral de "Hades" deve ser 4.00

  Cenário: Excluir review recalcula as médias do jogo
    Dado que o jogo "Hades" possui reviews com notas gerais 3, 4 e 5
    Quando a review com nota geral 3 é excluída
    Então a média geral de "Hades" deve ser 4.50

  Cenário: Novo registro de conclusão atualiza o total de conclusões
    Dado que o jogo "Celeste" possui 10 conclusões
    Quando um jogador registra uma conclusão de "Celeste"
    Então o total de conclusões de "Celeste" deve ser 11

  Cenário: Pedido concluído no marketplace atualiza o volume de negociações
    Dado que o jogo "Hades" possui 2 negociações concluídas
    Quando um pedido de "Hades" é concluído no marketplace
    Então o total de negociações de "Hades" deve ser 3

  # RN 2 - precisão e arredondamento
  Cenário: Médias são gravadas com duas casas decimais
    Dado que o jogo "Hades" possui reviews com notas gerais 5, 4 e 4
    Quando as estatísticas de "Hades" são recalculadas
    Então a média geral de "Hades" deve ser gravada como 4.33

  # RN 3 - elegibilidade para amostragem pública
  Cenário: Estatísticas não são exibidas com menos de 3 reviews
    Dado que o jogo "Hades" possui 2 reviews ativas
    Quando o usuário consulta a ficha de estatísticas de "Hades"
    Então as estatísticas públicas não devem ser exibidas

  Cenário: Estatísticas são exibidas a partir de 3 reviews
    Dado que o jogo "Hades" possui 3 reviews ativas
    Quando o usuário consulta a ficha de estatísticas de "Hades"
    Então as estatísticas públicas devem ser exibidas
