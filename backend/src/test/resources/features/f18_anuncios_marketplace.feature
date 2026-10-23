# language: pt
@pendente
Funcionalidade: F-18 Gerenciamento completo de anúncios do marketplace
  Como vendedor, quero anunciar jogos da minha coleção para venda ou troca e gerenciar
  meus anúncios.

  Contexto:
    Dado o jogador "marina" autenticado

  # RN 1 - vínculo com o catálogo e fotos do item
  Cenário: Criar anúncio com jogo do catálogo e foto
    Quando o jogador anuncia "Zelda: Tears of the Kingdom" usado por R$ 200,00 para "VENDA" com 2 fotos
    Então o anúncio deve ser criado com status "ATIVO"

  Cenário: Anúncio sem foto é recusado
    Quando o jogador anuncia "Zelda: Tears of the Kingdom" por R$ 200,00 sem fotos
    Então o anúncio deve ser recusado com a mensagem "O anúncio precisa de pelo menos 1 foto"

  Cenário: Anúncio de jogo fora do catálogo é recusado
    Quando o jogador anuncia um jogo que não existe no catálogo
    Então o anúncio deve ser recusado com a mensagem "O jogo precisa estar cadastrado no catálogo"

  # RN 2 - gestão do ciclo de vida do anúncio
  Cenário: Proprietário pausa e reativa o anúncio
    Dado que o jogador possui um anúncio com status "ATIVO"
    Quando o jogador pausa o anúncio
    Então o anúncio deve ter status "PAUSADO"

  Cenário: Outro usuário não altera o preço do anúncio
    Dado que o jogador "joao" possui um anúncio com status "ATIVO"
    Quando o jogador "marina" tenta alterar o preço do anúncio de "joao"
    Então a operação deve ser recusada com a mensagem "Apenas o proprietário pode alterar o anúncio"

  # RN 3 - validação de preço e retirada de anúncios vendidos
  Esquema do Cenário: Validação do preço
    Quando o jogador anuncia "Hades" por <preco> com 1 foto
    Então o anúncio deve ser <resultado>

    Exemplos:
      | preco  | resultado |
      | 59.90  | criado    |
      | 0      | recusado  |
      | -10.00 | recusado  |

  Cenário: Anúncio vendido não aparece na busca pública
    Dado que existem 2 anúncios de "Hades" com status "ATIVO" e 1 com status "VENDIDO"
    Quando um usuário busca anúncios de "Hades"
    Então a busca deve retornar 2 anúncios

  # Consulta
  Cenário: Filtrar anúncios por faixa de preço e estado de conservação
    Dado anúncios de "Hades" "NOVO" por R$ 150,00, "USADO" por R$ 80,00 e "USADO" por R$ 60,00
    Quando um usuário busca anúncios "USADO" entre R$ 50,00 e R$ 70,00
    Então a busca deve retornar 1 anúncio
