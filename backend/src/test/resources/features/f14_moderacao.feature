# language: pt
Funcionalidade: F-14 Central de moderação, auditoria e denúncias
  Conteúdo denunciado permanece público até decisão. Toda decisão é auditada.

  Esquema do Cenário: Jogador denuncia qualquer conteúdo público existente
    Dado que há um jogador autenticado para moderação
    E que existe conteúdo moderável do tipo "<tipo>"
    Quando ele denuncia o conteúdo com motivo "Conteúdo inadequado"
    Então existe uma denúncia pendente e o conteúdo continua público

    Exemplos:
      | tipo                    |
      | TOPICO                  |
      | RESPOSTA                |
      | REVIEW                  |
      | LISTA                   |
      | AVALIACAO_MARKETPLACE  |
      | ANUNCIO                 |
      | PERFIL                  |
      | JOURNAL                 |

  Cenário: Denúncia repetida mantém uma só pendência
    Dado que há um jogador autenticado para moderação
    E que existe conteúdo moderável do tipo "TOPICO"
    Quando ele denuncia o conteúdo duas vezes
    Então há uma única denúncia pendente do jogador para o alvo

  Cenário: Jogador comum não acessa a central
    Dado que há um jogador autenticado para moderação
    Quando tenta consultar a fila da moderação
    Então o acesso à moderação é recusado

  Cenário: Ocultar exige justificativa
    Dado que há um moderador autenticado para moderação
    E que existe conteúdo moderável do tipo "RESPOSTA"
    Quando tenta ocultar sem justificativa
    Então a ação deve ser recusada com a mensagem "A justificativa é obrigatória"

  Cenário: Decisão procedente gera log e resolve denúncias
    Dado que há um jogador autenticado para moderação
    E que existe conteúdo moderável do tipo "REVIEW"
    E que ele denunciou o conteúdo
    Quando um moderador oculta o conteúdo com justificativa "Discurso de ódio"
    Então as denúncias do alvo ficam procedentes e há um log imutável
    E o autor recebe a notificação da ação

  Cenário: Arquivamento improcedente é auditado
    Dado que há um jogador autenticado para moderação
    E que existe conteúdo moderável do tipo "LISTA"
    E que ele denunciou o conteúdo
    Quando um moderador arquiva a denúncia
    Então a denúncia fica improcedente e há um log de arquivamento

  Cenário: Remover resposta mantém filhas e contagem
    Dado que há um moderador autenticado para moderação
    E que existe resposta com duas filhas para moderação
    Quando o moderador remove a resposta com justificativa "Spam"
    Então as duas filhas continuam visíveis e o contador exclui a resposta

  Cenário: Remover tópico preserva endereço e encerra conversa
    Dado que há um moderador autenticado para moderação
    E que existe conteúdo moderável do tipo "TOPICO"
    Quando o moderador remove o tópico com justificativa "Spam"
    Então o endereço do tópico mostra a mensagem de sistema e status "ENCERRADO"

  Cenário: Reversão cria novo log e restaura visibilidade
    Dado que há um moderador autenticado para moderação
    E que existe conteúdo moderável do tipo "ANUNCIO"
    Quando oculta e reverte a ocultação do conteúdo
    Então o conteúdo volta a ser público e existem dois logs

  Cenário: Aviso 18+ não restringe a leitura
    Dado que há um moderador autenticado para moderação
    E que existe conteúdo moderável do tipo "PERFIL"
    Quando aplica aviso 18+ ao conteúdo
    Então o conteúdo permanece público com aviso 18+

  Cenário: Advertência incrementa contador e notifica usuário
    Dado que há um moderador autenticado para moderação
    E que existe conteúdo moderável do tipo "PERFIL"
    Quando adverte o autor por "Reincidência"
    Então a contagem de advertências é 1 e o autor é notificado

  # Entradas do journal (F-15) são públicas no perfil e também podem ser moderadas.
  Cenário: Entrada do journal oculta sai do perfil público e não pode ser editada
    Dado que há um jogador autenticado para moderação
    E que existe conteúdo moderável do tipo "JOURNAL"
    Quando um moderador oculta o conteúdo com justificativa "Exposição de dados pessoais"
    Então a entrada não aparece no journal público do autor
    E o autor não consegue mais editar a entrada

  Cenário: Entrada do journal removida mostra a mensagem de sistema ao autor
    Dado que há um moderador autenticado para moderação
    E que existe conteúdo moderável do tipo "JOURNAL"
    Quando o moderador remove o conteúdo com justificativa "Spam"
    Então o autor vê a entrada com a mensagem de sistema
    E a entrada não aparece no journal público do autor
