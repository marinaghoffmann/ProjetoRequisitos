# language: pt
Funcionalidade: F-04 Gerenciamento completo de listas personalizadas e seus itens
  Como jogador, quero criar listas temáticas e gerenciar os jogos dentro delas, com notas
  explicativas, para organizar e compartilhar minha biblioteca.

  Contexto:
    Dado o jogador "marina" autenticado

  Cenário: Criar uma lista personalizada
    Quando o jogador cria a lista "Melhores RPGs" com descrição "Meus favoritos do gênero"
    Então o jogador deve possuir a lista "Melhores RPGs"

  # RN 1 - nome de lista único por usuário
  Cenário: Título repetido ignorando maiúsculas e minúsculas é recusado
    Dado que o jogador já possui a lista "Melhores RPGs"
    Quando o jogador cria a lista "melhores rpgs"
    Então a criação deve ser recusada com a mensagem "Você já possui uma lista com este título"

  Cenário: Outro jogador pode usar o mesmo título
    Dado que o jogador "joao" possui a lista "Melhores RPGs"
    Quando o jogador cria a lista "Melhores RPGs"
    Então o jogador deve possuir a lista "Melhores RPGs"

  Cenário: Editar e excluir uma lista
    Dado que o jogador já possui a lista "Zerar nas férias"
    Quando o jogador renomeia a lista "Zerar nas férias" para "Zerar em dezembro"
    E o jogador exclui a lista "Zerar em dezembro"
    Então o jogador não deve possuir nenhuma lista

  # RN 2 - impedir duplicação de jogo na mesma lista
  Cenário: Adicionar jogo com nota explicativa
    Dado que o jogador já possui a lista "Melhores RPGs"
    Quando o jogador adiciona o jogo "Persona 5" à lista com a nota "Trilha sonora perfeita"
    Então a lista "Melhores RPGs" deve conter 1 jogo
    E o item "Persona 5" deve ter a nota explicativa "Trilha sonora perfeita"

  Cenário: Adicionar o mesmo jogo duas vezes é recusado
    Dado que a lista "Melhores RPGs" do jogador já contém o jogo "Persona 5"
    Quando o jogador adiciona o jogo "Persona 5" à lista
    Então a adição deve ser recusada com a mensagem "Este jogo já está na lista"

  Cenário: Remover jogo da lista
    Dado que a lista "Melhores RPGs" do jogador já contém o jogo "Persona 5"
    Quando o jogador remove o jogo "Persona 5" da lista
    Então a lista "Melhores RPGs" deve conter 0 jogos

  # RN 3 - limite máximo de itens por lista
  Cenário: Lista com 100 jogos não aceita mais itens
    Dado que a lista "Melhores RPGs" do jogador já contém 100 jogos
    Quando o jogador adiciona o jogo "Chrono Trigger" à lista
    Então a adição deve ser recusada com a mensagem "A lista atingiu o limite de 100 jogos"

  # Consulta
  Cenário: Consultar as listas do jogador com o total de jogos
    Dado que o jogador possui a lista "RPGs" com 3 jogos e a lista "Indies" com 2 jogos
    Quando o jogador consulta suas listas
    Então a consulta deve retornar 2 listas
    E a lista "RPGs" deve informar o total de 3 jogos
