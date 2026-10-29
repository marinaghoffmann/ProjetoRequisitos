# language: pt
Funcionalidade: T02 Usuários, autenticação e papéis
  Cadastro público cria apenas jogadores comuns; login autentica credenciais;
  operações administrativas verificam o papel atual e a propriedade do recurso.

  Cenário: Cadastro público recebe o papel comum
    Quando cadastro um usuário com nome "Bruno" e senha "senha-segura-123"
    Então o papel da conta cadastrada deve ser "COMUM"
    E a operação deve ser concluída sem erros

  Cenário: Login correto retorna uma sessão com token Bearer
    Dado que existe uma conta comum cadastrada
    Quando faço login com a senha correta
    Então a sessão deve conter um token Bearer
    E a operação deve ser concluída sem erros

  Cenário: Senha incorreta é recusada sem revelar qual credencial falhou
    Dado que existe uma conta comum cadastrada
    Quando faço login com uma senha incorreta
    Então o login deve ser recusado com a mensagem "Email ou senha inválidos"

  Cenário: Papel comum não pode alterar papéis
    Dado que estou autenticado como usuário comum
    Quando tento alterar meu papel para "ADMINISTRADOR"
    Então o acesso deve ser recusado com a mensagem "Você não tem permissão para esta operação"

  Cenário: Administrador pode atribuir um papel permitido
    Dado que estou autenticado como administrador
    E que existe uma conta comum cadastrada
    Quando o administrador altera o papel dessa conta para "MODERADOR"
    Então o papel persistido da conta deve ser "MODERADOR"
    E a operação deve ser concluída sem erros

  Cenário: Usuário só é dono do próprio recurso
    Dado que estou autenticado como usuário comum
    Quando tento acessar um recurso pertencente a outra conta
    Então o acesso deve ser recusado com a mensagem "Apenas o dono pode alterar este recurso"
