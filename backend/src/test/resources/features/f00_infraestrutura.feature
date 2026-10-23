# language: pt
Funcionalidade: F-00 Infraestrutura de testes BDD
  Cenários de exemplo que confirmam que o Cucumber, o contexto Spring e o step comum
  de recusa estão funcionando. Sirvam de modelo para as demais funcionalidades.

  Cenário: A aplicação responde que está no ar
    Quando consulto o status do sistema
    Então o status deve ser "UP"
    E a operação deve ser concluída sem erros

  Cenário: Uma regra de negócio violada é capturada pelo step comum
    Quando uma regra de negócio é violada com a mensagem "Exemplo de regra violada"
    Então a operação deve ser recusada com a mensagem "Exemplo de regra violada"
