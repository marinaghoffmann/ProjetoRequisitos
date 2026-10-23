# language: pt
@pendente
Funcionalidade: F-01 Importação, cadastramento manual e gerenciamento do catálogo de jogos e desenvolvedoras
  Como desenvolvedor ou administrador, quero alimentar o catálogo pela API externa ou por
  cadastro manual e gerenciar jogos e desenvolvedoras, mantendo o catálogo sem duplicidades
  e preservando os dados vinculados.

  # RN 1 - evitar duplicidade por API e vínculo de origem
  Cenário: Jogo importado da API é inserido quando o identificador externo é novo
    Dado que não existe jogo com identificador externo "IGDB-1942" no catálogo
    Quando o sistema importa o jogo "The Witcher 3" com identificador externo "IGDB-1942"
    Então o catálogo deve conter 1 jogo com identificador externo "IGDB-1942"
    E a origem do jogo deve ser "API"

  Cenário: Jogo importado da API é atualizado quando o identificador externo já existe
    Dado que existe o jogo "Witcher 3" com identificador externo "IGDB-1942" no catálogo
    Quando o sistema importa o jogo "The Witcher 3: Wild Hunt" com identificador externo "IGDB-1942"
    Então o catálogo deve conter 1 jogo com identificador externo "IGDB-1942"
    E o título do jogo deve ser "The Witcher 3: Wild Hunt"

  Cenário: Cadastro manual com título repetido na mesma desenvolvedora é recusado
    Dado um usuário com papel "DESENVOLVEDOR" vinculado à desenvolvedora ativa "Studio Recife"
    E que a desenvolvedora "Studio Recife" já possui o jogo "Frevo Quest"
    Quando o desenvolvedor cadastra manualmente o jogo "frevo quest"
    Então o cadastro deve ser recusado com a mensagem "Já existe um jogo com este título para a desenvolvedora"

  # RN 2 - vínculo obrigatório com perfil de desenvolvedora existente
  Cenário: Desenvolvedor com desenvolvedora ativa cadastra jogo manualmente
    Dado um usuário com papel "DESENVOLVEDOR" vinculado à desenvolvedora ativa "Studio Recife"
    Quando o desenvolvedor cadastra manualmente o jogo "Mangue Runner"
    Então o jogo "Mangue Runner" deve estar vinculado à desenvolvedora "Studio Recife"
    E a origem do jogo deve ser "MANUAL_CRIADOR"

  Cenário: Usuário sem papel de desenvolvedor não cadastra jogo manualmente
    Dado um usuário com papel "JOGADOR"
    Quando o usuário cadastra manualmente o jogo "Mangue Runner"
    Então o cadastro deve ser recusado com a mensagem "Apenas desenvolvedores podem cadastrar jogos manualmente"

  Cenário: Desenvolvedor vinculado a desenvolvedora inativa não cadastra jogo
    Dado um usuário com papel "DESENVOLVEDOR" vinculado à desenvolvedora inativa "Studio Antigo"
    Quando o desenvolvedor cadastra manualmente o jogo "Mangue Runner"
    Então o cadastro deve ser recusado com a mensagem "O desenvolvedor precisa estar vinculado a uma desenvolvedora ativa"

  Cenário: Desenvolvedor atualiza o perfil institucional da sua desenvolvedora
    Dado um usuário com papel "DESENVOLVEDOR" vinculado à desenvolvedora ativa "Studio Recife"
    Quando o desenvolvedor altera a biografia para "Estúdio indie pernambucano" e o país para "Brasil"
    Então o perfil da desenvolvedora "Studio Recife" deve exibir a biografia "Estúdio indie pernambucano"

  # RN 3 - exclusão lógica e manutenção da integridade de vínculos
  Cenário: Administrador remove jogo com reviews vinculadas por exclusão lógica
    Dado um usuário com papel "ADMINISTRADOR"
    E que o jogo "Hollow Knight" possui 3 reviews
    Quando o administrador remove o jogo "Hollow Knight"
    Então o jogo "Hollow Knight" deve continuar no banco com status "INATIVO"
    E as 3 reviews do jogo devem ser preservadas

  Cenário: Administrador remove jogo sem vínculos definitivamente
    Dado um usuário com papel "ADMINISTRADOR"
    E que o jogo "Jogo Teste" não possui reviews, entradas de journal nem itens de lista
    Quando o administrador remove o jogo "Jogo Teste"
    Então o jogo "Jogo Teste" não deve mais existir no catálogo

  Cenário: Administrador edita os metadados de um jogo
    Dado um usuário com papel "ADMINISTRADOR"
    E que existe o jogo "Celeste" no catálogo
    Quando o administrador altera o resumo do jogo "Celeste" para "Plataforma sobre superar desafios"
    Então o jogo "Celeste" deve ter o resumo "Plataforma sobre superar desafios"

  # Consulta
  Cenário: Consultar o catálogo filtrando por origem
    Dado que o catálogo possui 4 jogos de origem "API" e 2 de origem "MANUAL_CRIADOR"
    Quando o usuário filtra o catálogo pela origem "MANUAL_CRIADOR"
    Então a consulta deve retornar 2 jogos
