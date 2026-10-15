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
