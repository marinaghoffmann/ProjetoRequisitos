# language: pt
Funcionalidade: F-15 Gerenciamento completo do journal individual
  Como jogador, quero registrar anotações das minhas sessões de jogo com tags, imagens e
  aviso de spoiler, para manter meu diário de bordo.

  Contexto:
    Dado o jogador "marina" autenticado

  Cenário: Criar entrada associada a um jogo com tags
    Quando o jogador cria uma entrada no journal sobre "Hollow Knight" com as tags "chefes, exploração"
    Então o journal do jogador deve ter 1 entrada
    E a entrada deve ter as tags "chefes" e "exploração"

  # RN 1 - restrição de propriedade do diário
  Cenário: Jogador edita a própria entrada
    Dado que o jogador possui uma entrada no journal com o texto "Derrotei o Hornet"
    Quando o jogador altera o texto da entrada para "Derrotei o Hornet na 3ª tentativa"
    Então a entrada deve ter o texto "Derrotei o Hornet na 3ª tentativa"

  Cenário: Jogador não altera entrada de outro usuário
    Dado que o jogador "joao" possui uma entrada no journal
    Quando o jogador "marina" tenta excluir a entrada de "joao"
    Então a operação deve ser recusada com a mensagem "Apenas o dono pode alterar esta entrada"

  # RN 2 - gerenciamento de anexos e mídias
  Cenário: Entrada aceita até 3 imagens
    Dado que o jogador possui uma entrada no journal com 2 imagens
    Quando o jogador anexa mais 1 imagem à entrada
    Então a entrada deve ter 3 imagens

  Cenário: Quarta imagem é recusada
    Dado que o jogador possui uma entrada no journal com 3 imagens
    Quando o jogador anexa mais 1 imagem à entrada
    Então a operação deve ser recusada com a mensagem "Cada entrada aceita no máximo 3 imagens"

  Cenário: Excluir a entrada apaga os anexos vinculados
    Dado que o jogador possui uma entrada no journal com 2 imagens
    Quando o jogador exclui a entrada
    Então não deve existir nenhum anexo vinculado à entrada

  # RN 3 - controle e mascaramento de spoilers
  Cenário: Entrada com spoiler fica mascarada no perfil público
    Dado que o jogador possui uma entrada pública marcada com spoiler
    Quando outro usuário visualiza o perfil público do jogador
    Então o texto da entrada deve estar mascarado

  Cenário: Leitor revela o spoiler com clique explícito
    Dado que o jogador possui uma entrada pública marcada com spoiler
    Quando outro usuário confirma que deseja ver o spoiler
    Então o texto completo da entrada deve ser exibido

  # Consulta
  Cenário: Filtrar entradas por intervalo de datas
    Dado que o jogador possui entradas em "2026-08-01", "2026-09-15" e "2026-10-05"
    Quando o jogador filtra o journal de "2026-09-01" a "2026-10-31"
    Então a consulta deve retornar 2 entradas

  Cenário: Criar anotação sem jogo associado
    Quando o jogador cria uma anotação sem jogo com o texto "Memórias da semana"
    Então a entrada deve ter o texto "Memórias da semana"
    E a entrada não deve ter jogo associado

  Cenário: Filtrar por jogo e tag
    Dado entradas de journal sobre jogos e tags diferentes
    Quando o jogador filtra o journal pelo jogo "Hades" e pela tag "exploração"
    Então a consulta deve retornar 1 entrada

  Cenário: Intervalo invertido é recusado
    Quando o jogador filtra o journal de "2026-11-01" a "2026-10-01"
    Então a operação deve ser recusada com a mensagem "A data inicial não pode ser posterior à data final"

  Cenário: Editar remove apenas as imagens descartadas
    Dado que o jogador possui uma entrada no journal com 2 imagens
    Quando o jogador edita a entrada mantendo apenas a primeira imagem
    Então a entrada deve ter 1 imagem
    E os dados da imagem descartada devem ser apagados

  Cenário: Edição com imagens demais não altera a entrada
    Dado que o jogador possui uma entrada no journal com 3 imagens
    Quando o jogador tenta editar a entrada adicionando uma quarta imagem
    Então a operação deve ser recusada com a mensagem "Cada entrada aceita no máximo 3 imagens"
    E a entrada deve ter 3 imagens
    E o texto original do journal deve ser preservado

  Cenário: Publicação com spoiler não entrega imagens antes da revelação
    Dado que o jogador possui uma entrada pública marcada com spoiler
    E o jogador anexa uma imagem ao spoiler
    Quando outro usuário visualiza o perfil público do jogador
    Então o texto da entrada deve estar mascarado
    E a resposta pública não deve incluir os anexos do spoiler

  Cenário: Entradas e imagens sofrem rollback juntas
    Quando a transação falha após criar uma entrada com imagem
    Então nem a entrada nem a imagem devem ser gravadas

  Cenário: As anotações manuais também aparecem na timeline
    Quando o jogador cria uma anotação sem jogo com o texto "Minha sessão"
    Então a anotação criada deve aparecer na timeline unificada

  Cenário: Filtro inclui o dia final e não inclui o dia seguinte
    Dado que o jogador possui entradas em "2026-10-30", "2026-10-31" e "2026-11-01"
    Quando o jogador filtra o journal de "2026-10-31" a "2026-10-31"
    Então a consulta deve retornar 1 entrada

  Cenário: Jogador não edita entrada de outro usuário
    Dado que o jogador "joao" possui uma entrada no journal
    Quando o jogador "marina" tenta editar a entrada de "joao"
    Então a operação deve ser recusada com a mensagem "Apenas o dono pode alterar esta entrada"
