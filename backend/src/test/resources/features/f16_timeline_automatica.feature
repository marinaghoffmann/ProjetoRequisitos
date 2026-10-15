# language: pt
Funcionalidade: F-16 Automação da linha do tempo e integração de eventos no journal
  Como jogador, quero que minhas ações na plataforma apareçam automaticamente no meu
  journal, formando uma linha do tempo unificada.

  Contexto:
    Dado o jogador "marina" autenticado

  Esquema do Cenário: Ação na plataforma gera entrada automática
    Quando o jogador <acao>
    Então o journal deve ter 1 entrada automática do tipo "<evento>"

    Exemplos:
      | acao                                          | evento           |
      | publica uma review de "Hades"                 | REVIEW_PUBLICADA |
      | registra uma conclusão de "Celeste"           | JOGO_ZERADO      |
      | confirma o recebimento de um pedido de "Hades" | COMPRA_CONCLUIDA |

  # RN 1 - atomicidade na gravação de eventos
  Cenário: Falha na ação principal desfaz a entrada automática
    Dado que a gravação da review vai falhar
    Quando o jogador publica uma review de "Hades"
    Então a review não deve ser registrada
    E o journal deve ter 0 entradas automáticas

  # RN 2 - sincronização de alterações e exclusões
  Cenário: Excluir a ação de origem remove a entrada automática
    Dado que o jogador registrou uma conclusão de "Celeste" que gerou entrada automática
    Quando o jogador exclui o registro de conclusão de "Celeste"
    Então o journal deve ter 0 entradas automáticas do tipo "JOGO_ZERADO"

  # RN 3 - bloqueio de edição manual em entradas de sistema
  Cenário: Texto de entrada automática não pode ser editado
    Dado que o jogador possui uma entrada automática do tipo "REVIEW_PUBLICADA"
    Quando o jogador tenta alterar o texto da entrada automática
    Então a operação deve ser recusada com a mensagem "Entradas automáticas não podem ser editadas"

  # Consulta
  Cenário: Timeline unificada com entradas manuais e automáticas
    Dado que o jogador possui 2 entradas manuais e 3 entradas automáticas
    Quando o jogador consulta a sua timeline
    Então a timeline deve conter 5 entradas ordenadas por data
