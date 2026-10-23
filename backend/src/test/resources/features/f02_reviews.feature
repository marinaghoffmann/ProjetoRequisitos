# language: pt
@pendente
Funcionalidade: F-02 Gerenciamento completo de reviews e avaliações categoriais
  Como jogador, quero publicar, editar e excluir minha review de um jogo com nota geral e
  notas por categoria, para compartilhar minha opinião com a comunidade.

  Contexto:
    Dado o jogador "marina" autenticado
    E que existe o jogo "Hades" no catálogo

  Cenário: Publicar review com nota geral, notas por categoria e texto
    Quando o jogador publica uma review de "Hades" com nota geral 5 e notas por categoria:
      | categoria    | nota |
      | GRAFICOS     | 5    |
      | SOM          | 5    |
      | JOGABILIDADE | 5    |
      | DIVERSAO     | 4    |
      | INOVACAO     | 4    |
    E o texto "Combate viciante e história que avança a cada tentativa"
    Então a review deve ser registrada com 5 notas por categoria

  # RN 1 - unicidade de review ativa por usuário e jogo
  Cenário: Tentar avaliar de novo um jogo já avaliado redireciona para a edição
    Dado que o jogador "marina" já possui uma review de "Hades"
    Quando o jogador tenta publicar uma nova review de "Hades"
    Então nenhuma nova review deve ser criada
    E o sistema deve indicar o redirecionamento para a edição da review existente

  Cenário: Nota fora do intervalo de 1 a 5 é recusada
    Quando o jogador publica uma review de "Hades" com nota geral 6
    Então a publicação deve ser recusada com a mensagem "A nota deve estar entre 1 e 5"

  # RN 2 - restrição estrita de propriedade
  Cenário: Autor edita as notas e o texto da sua review
    Dado que o jogador "marina" já possui uma review de "Hades" com nota geral 4
    Quando o jogador "marina" altera a nota geral da sua review para 5
    Então a review deve ter nota geral 5

  Cenário: Outro usuário não pode editar a review
    Dado que o jogador "joao" possui uma review de "Hades"
    Quando o jogador "marina" tenta editar a review de "joao"
    Então a operação deve ser recusada com a mensagem "Apenas o autor pode alterar a review"

  Cenário: Outro usuário não pode excluir a review
    Dado que o jogador "joao" possui uma review de "Hades"
    Quando o jogador "marina" tenta excluir a review de "joao"
    Então a operação deve ser recusada com a mensagem "Apenas o autor pode alterar a review"

  # RN 3 - exclusão em cascata das notas categoriais
  Cenário: Excluir a review remove as notas por categoria em cascata
    Dado que o jogador "marina" já possui uma review de "Hades" com 5 notas por categoria
    Quando o jogador "marina" exclui a sua review de "Hades"
    Então a review não deve mais existir
    E não deve existir nenhuma nota por categoria vinculada a ela

  # Consulta
  Cenário: Consultar as reviews de um jogo com médias por categoria
    Dado que o jogo "Hades" possui reviews com nota de "GRAFICOS" 4 e 5
    Quando o usuário consulta as reviews de "Hades"
    Então a média de "GRAFICOS" deve ser 4.50
