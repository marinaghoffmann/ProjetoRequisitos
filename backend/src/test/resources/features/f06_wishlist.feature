# language: pt
@pendente
Funcionalidade: F-06 Gerenciamento da wishlist
  Como jogador, quero manter uma wishlist com prioridades, para lembrar quais jogos
  quero jogar primeiro.

  Contexto:
    Dado o jogador "marina" autenticado

  # RN 1 - unicidade e criação automática da wishlist
  Cenário: A wishlist é criada automaticamente na primeira adição
    Dado que o jogador ainda não possui wishlist
    Quando o jogador adiciona o jogo "Silksong" à wishlist com prioridade "ALTA"
    Então o jogador deve possuir exatamente 1 wishlist
    E a wishlist deve conter o jogo "Silksong" com prioridade "ALTA"

  Cenário: Adições seguintes usam a mesma wishlist
    Dado que o jogador já possui uma wishlist com o jogo "Silksong"
    Quando o jogador adiciona o jogo "Hades II" à wishlist com prioridade "BAIXA"
    Então o jogador deve possuir exatamente 1 wishlist
    E a wishlist deve conter 2 jogos

  # RN 2 - bloqueio de jogo duplicado na wishlist
  Cenário: Jogo repetido na wishlist é recusado
    Dado que o jogador já possui uma wishlist com o jogo "Silksong"
    Quando o jogador adiciona o jogo "Silksong" à wishlist com prioridade "MEDIA"
    Então a adição deve ser recusada com a mensagem "Este jogo já está na sua wishlist"

  # RN 3 - atribuição de prioridade padrão
  Cenário: Jogo adicionado sem prioridade recebe prioridade Média
    Quando o jogador adiciona o jogo "Hollow Knight" à wishlist sem informar prioridade
    Então a wishlist deve conter o jogo "Hollow Knight" com prioridade "MEDIA"

  Cenário: Remover jogo da wishlist
    Dado que o jogador já possui uma wishlist com o jogo "Silksong"
    Quando o jogador remove o jogo "Silksong" da wishlist
    Então a wishlist deve conter 0 jogos

  # Consulta
  Cenário: Wishlist de outro jogador é exibida ordenada por prioridade
    Dado que o jogador "joao" possui na wishlist "Jogo A" com prioridade "BAIXA", "Jogo B" com prioridade "ALTA" e "Jogo C" com prioridade "MEDIA"
    Quando o jogador consulta a wishlist de "joao"
    Então os jogos devem aparecer na ordem "Jogo B", "Jogo C", "Jogo A"
