# Domínio de Negócio: Ecossistema Game Tracker

O ecossistema **Game Tracker** organiza-se em torno do registro da jornada do Jogador, da confiabilidade do Catálogo e da construção de uma comunidade que conversa sobre jogos. Inspirado em redes de registro cultural como o Letterboxd, o fluxo integra desde a importação de jogos de uma base externa e a avaliação por categorias até a organização da biblioteca pessoal, o diário de jogatina e o fórum, garantindo que o dado do jogo seja confiável, que as regras de propriedade e de limite sejam sempre respeitadas e que cada Jogador tenha um perfil público que reflita o que ele joga, quer jogar e já zerou.

---

## Persona - Administrador
**F-09 Cadastro e Gerenciamento de Perfil de Desenvolvedora:** O Administrador, ou o Representante Verificado do estúdio, mantém o perfil institucional de cada Desenvolvedora, cadastrando biografia, ano de fundação, país de origem e links oficiais, além de vincular ao estúdio os jogos existentes no Catálogo. O sistema impede o cadastro de duas Desenvolvedoras com o mesmo nome e o mesmo país de origem, e restringe a alteração dos dados institucionais a quem possui o papel de Administrador ou de Representante Verificado. Ao remover um perfil, os jogos vinculados nunca são apagados: eles passam a apontar para o registro genérico "Desenvolvedora Desconhecida / Indie", preservando a integridade do Catálogo. A consulta apresenta os detalhes da Desenvolvedora e lista seus jogos ordenados por ano de lançamento.

---

## Persona - Jogador
**F-02 Criação de Review Geral e por Categoria:** A jornada de avaliação inicia-se quando o Jogador registrado publica sua opinião sobre um jogo do Catálogo, atribuindo uma Nota Geral de 1 a 5 estrelas e cinco Notas por Categoria: Gráficos, Som, Jogabilidade, Diversão e Inovação. Todas as notas são obrigatórias e devem estar dentro do intervalo permitido; o comentário descritivo é opcional, mas, quando preenchido, deve ter entre 20 e 2.000 caracteres para evitar conteúdo de spam. Cada Jogador possui uma única Review por jogo: ao tentar avaliar novamente um jogo já avaliado, ele é redirecionado ao fluxo de edição. A consulta reúne todas as Reviews do jogo, com o cálculo das médias por categoria e a listagem dos textos dos usuários.

**F-03 Edição e Exclusão de Review:** Permite que o Jogador corrija as notas e o texto de uma Review publicada por ele ou a exclua definitivamente. O sistema valida no backend que o Jogador autenticado é exatamente o autor da Review antes de autorizar qualquer alteração ou remoção. A cada edição, registra a data e a hora da última modificação e sinaliza a Review com a Tag "Editado". Na exclusão, as Notas por Categoria dependentes são removidas em cascata, evitando dados órfãos. O Jogador consulta o histórico das Reviews que criou.

**F-04 Criação e Gerenciamento de Listas Personalizadas:** O Jogador organiza sua biblioteca em Listas temáticas, como "Meus RPGs Favoritos" ou "Jogos para Zerar nas Férias", definindo título, descrição e Visibilidade. Toda nova Lista nasce Pública, e o Jogador pode torná-la Privada a qualquer momento durante a edição, além de excluí-la. O sistema não permite dois títulos idênticos para o mesmo Jogador, sem diferenciar maiúsculas de minúsculas, e limita cada conta a 30 Listas ativas para evitar sobrecarga. A consulta exibe todas as Listas de um Jogador com a contagem de jogos de cada uma.

**F-05 Adição e Remoção de Jogos em uma Lista Personalizada:** Permite ao Jogador inserir jogos em uma de suas Listas, com uma Nota Explicativa opcional sobre o motivo da inclusão, ou removê-los. O sistema barra qualquer tentativa de adicionar um jogo que já esteja na Lista, permite a alteração apenas em Listas cujo criador seja o Jogador autenticado e limita cada Lista a 100 jogos. A consulta apresenta os itens de uma Lista ordenados pela data de inclusão.

**F-06 Gerenciamento da Wishlist:** Cada Jogador possui exatamente uma Wishlist, criada automaticamente na primeira tentativa de adicionar um jogo. Nela, ele registra os jogos que deseja jogar e define uma Prioridade (Alta, Média ou Baixa) para cada um; quando nenhuma é informada, o sistema assume Média. O mesmo jogo não pode aparecer duas vezes na Wishlist do mesmo Jogador, e ele pode removê-lo quando quiser. A consulta exibe a Wishlist do Jogador autenticado, ou a de outro Jogador, ordenada por Prioridade.

**F-07 Marcação de Currently Playing:** O Jogador destaca no perfil os jogos que está jogando no momento e acompanha seu Progresso em três estágios: "No início", "Na metade" e "Perto do fim". O sistema salva a data e a hora em que o jogo foi marcado e limita a 5 os jogos simultâneos. Ao tentar marcar um sexto jogo, o Jogador precisa remover ou substituir um dos existentes antes da gravação. Ao concluir o jogo, ele o retira do destaque. A lista de jogos em andamento é exibida no perfil público.

**F-08 Marcação de Jogos Favoritos no Perfil:** Permite ao Jogador fixar no topo do perfil público os jogos preferidos de sua vida, em até 4 Slots numerados de 1 a 4. Ao adicionar um novo Favorito, ele ocupa automaticamente o menor Slot vago, e um mesmo jogo nunca ocupa mais de um Slot. O Jogador pode reordenar os Favoritos ou desafixá-los, e a consulta os apresenta ordenados do Slot 1 ao Slot 4.

**F-11 Registro de "Zerado" e Contagem de Conclusões:** O Jogador registra que concluiu ("zerou") um jogo, informando a data de conclusão, a plataforma, as horas jogadas e se alcançou 100% das conquistas (platina). Cada Registro de Conclusão exige uma data que não seja futura, e as horas jogadas, quando informadas, devem ser maiores que zero e menores que 10.000. É permitido registrar várias conclusões do mesmo jogo, as chamadas replays, e a cada novo registro o sistema recalcula o Contador de Conclusões daquele Jogador para aquele jogo. A consulta apresenta o histórico completo de conclusões de um jogo, com datas e horas registradas.

**F-12 Cadastro e Acompanhamento de Lançamentos Futuros:** O Catálogo aceita jogos que ainda não chegaram ao mercado, desde que a data prevista de lançamento seja posterior à data atual do servidor, e permite que o Jogador demonstre interesse por meio do botão "Quero jogar no lançamento", ativando notificações. O interesse é único por Jogador e por jogo: ao clicar novamente, o sistema remove o interesse, funcionando como um toggle de marcar e desmarcar. Quando a data atual ultrapassa a data de lançamento cadastrada, o status do jogo passa automaticamente de "Futuro Lançamento" para "Lançado". A consulta lista os jogos não lançados em ordem cronológica, destacando aqueles nos quais o Jogador autenticado registrou interesse.

**F-13 Criação de Tópicos e Respostas no Fórum:** O Jogador autenticado abre Tópicos de discussão, gerais ou associados a um jogo específico, e outros Jogadores publicam Respostas encadeadas dentro deles. Todo Tópico exige um título com no mínimo 10 caracteres e uma mensagem inicial com no mínimo 20. Quando um Tópico está Encerrado, o sistema recusa qualquer nova Resposta. A cada Resposta gravada, o sistema incrementa a contagem de respostas do Tópico e atualiza a Data da Última Atividade. A consulta lista os Tópicos de um jogo com paginação, nome do autor e contagem de respostas.

**F-15 Registro Manual de Entradas no Journal:** O Journal é o diário de bordo pessoal do Jogador: ele escreve anotações sobre suas sessões, vinculando cada Entrada a um jogo já existente no Catálogo e informando a data da sessão. Se a anotação contiver Spoiler, o Jogador marca essa opção e o texto passa a exigir confirmação de leitura antes de ser exibido a outros usuários. Para evitar spam, cada Jogador pode registrar no máximo 10 Entradas manuais por jogo no mesmo dia civil. A consulta apresenta as Entradas do Jogador autenticado, da mais recente para a mais antiga.

---

## Persona - Moderador
**F-14 Moderação de Postagens do Fórum:** O autor de uma postagem ou um usuário com papel de Moderador ou Admin pode editar o conteúdo, marcar um Tópico como resolvido ou Encerrado e excluir Respostas e Tópicos que violem as regras do fórum. O sistema autoriza a alteração ou a remoção apenas quando o Jogador autenticado é o autor da postagem ou possui um desses papéis. Quando um Moderador que não seja o dono da postagem oculta ou remove um Tópico ou uma Resposta, o sistema cria obrigatoriamente um registro de Log de Moderação com a justificativa, o identificador do Moderador e a data. Ao excluir um Tópico que já possui Respostas, a exclusão é lógica (Soft Delete): o texto original é ocultado, mas a estrutura permanece no banco para não deixar Respostas órfãs. A consulta apresenta o histórico do Log de Moderação de um Tópico ou de um usuário específico.

---

## Persona - Sistema (Motores Automáticos)
**F-01 Importação e Catalogação de Jogos a partir de Base Externa:** O Catálogo do Game Tracker é alimentado por uma Base Externa confiável, como IGDB ou RAWG, da qual o sistema busca jogos e os cadastra ou atualiza localmente, salvando título, Desenvolvedora, imagem de capa, Gêneros, Plataformas e resumo. Antes de gravar um novo jogo, o sistema verifica se o Identificador Externo já existe no banco local e, se existir, atualiza o registro em vez de inserir um novo. Quando a Base Externa não retorna a imagem de capa ou a data de lançamento, o sistema aplica uma imagem padrão (Placeholder) e define o status "Sem data definida", impedindo falhas de integridade. Se a Desenvolvedora retornada ainda não existe, ela é cadastrada automaticamente antes de ser associada ao jogo. A consulta permite filtrar o Catálogo local por título, gênero, desenvolvedora ou plataforma.

**F-10 Geração de Sugestões de Jogos Similares:** O sistema calcula e grava relações de Similaridade entre jogos a partir de Tags de gênero, mecânicas e Desenvolvedora, e os Jogadores avaliam a relevância de cada sugestão com um Feedback de Sugestão ("Sugestão útil" ou "Não tem nada a ver"). Um jogo nunca pode ser similar a ele mesmo, e a relação é simétrica: se A é similar a B com um Grau de Similaridade X, B também sugere A na mesma proporção. Cada Jogador registra um único Feedback por par de sugestão (A → B) e, quando um par acumula mais de 10 Feedbacks negativos, o sistema rebaixa automaticamente o seu Grau de Similaridade. A consulta lista os jogos similares a um jogo, do maior para o menor grau.

**F-16 Geração Automática de Entradas no Journal:** Sempre que o Jogador realiza uma ação relevante, como criar uma Review, marcar um jogo como zerado ou adicioná-lo à Wishlist, o sistema gera uma Entrada automática no Journal, unificando a linha do tempo do Jogador. A inserção ocorre na mesma transação da ação principal: se ela falhar, a Entrada também sofre rollback. Cada Entrada recebe um Tipo de Evento (EVENTO_REVIEW, EVENTO_ZEROU ou EVENTO_WISHLIST) e um texto no formato fixo daquele tipo. Entradas marcadas como geradas automaticamente não podem ser editadas diretamente; o texto só muda se a entidade de origem, como a Review, for atualizada. A consulta apresenta a Timeline consolidada, com Entradas manuais e automáticas ordenadas por data.

**F-17 Estatísticas Agregadas de Jogos com Base na Comunidade:** O sistema consolida em uma tabela agregada as Estatísticas do Jogo: Nota Média Geral, médias por categoria, percentual de recomendação e total de Jogadores que zeraram ou têm o jogo na Wishlist. Sempre que uma Review é inserida, atualizada ou removida, as médias são recalculadas e regravadas. Todas as médias são armazenadas com exatamente duas casas decimais (por exemplo, 4.35). As Estatísticas são sempre calculadas, mas a Média Pública só é exibida quando o jogo possui pelo menos 3 Reviews ativas. A consulta retorna o registro consolidado para renderização rápida na página de detalhes do jogo.

---

## Glossário da Linguagem Onipresente

| Termo do Negócio | Significado / Contexto dentro do Sistema |
|---|---|
| **Jogador** | Usuário registrado da plataforma. É quem avalia jogos, organiza sua biblioteca, mantém o Journal, participa do fórum e possui um perfil público. |
| **Administrador** | Usuário com papel de gestão do Catálogo e das Desenvolvedoras. Também pode alterar ou remover postagens do fórum. |
| **Representante Verificado** | Usuário reconhecido como responsável por um estúdio, com permissão para editar os dados institucionais da sua Desenvolvedora. |
| **Moderador** | Usuário com papel para editar, encerrar ou remover postagens do fórum que violem as regras. Toda ação sobre postagem alheia deixa um Log de Moderação. |
| **Perfil Público** | Página do Jogador que exibe seus Favoritos, os jogos em Currently Playing e sua atividade, visível a outros usuários. |
| **Jogo** | Título do Catálogo. É a entidade central do sistema, em torno da qual se organizam Reviews, Listas, Wishlist, Journal, conclusões e Tópicos do fórum. |
| **Catálogo** | Conjunto de jogos armazenados localmente, alimentado pela Base Externa e disponível para consulta por título, gênero, desenvolvedora ou plataforma. |
| **Base Externa** | Fonte de dados confiável e atualizada, como IGDB ou RAWG, da qual o sistema importa as informações dos jogos. |
| **Identificador Externo** | Código único do jogo na Base Externa. É a chave que evita duplicidade no Catálogo: se já existe, o jogo é atualizado, não inserido de novo. |
| **Placeholder** | Imagem padrão atribuída ao jogo quando a Base Externa não fornece a capa. |
| **Sem data definida** | Status do jogo importado sem data de lançamento informada pela Base Externa. |
| **Gênero** | Classificação temática do jogo (RPG, Aventura, Estratégia etc.), usada em filtros e no cálculo de Similaridade. |
| **Plataforma** | Sistema em que o jogo pode ser jogado (PC, console, mobile etc.), usada em filtros e no Registro de Conclusão. |
| **Desenvolvedora** | Estúdio ou desenvolvedor independente responsável pelo jogo. É única pela combinação de nome e país de origem. |
| **Desenvolvedora Desconhecida / Indie** | Registro genérico para o qual os jogos são redirecionados quando o perfil da Desenvolvedora é removido. |
| **Review** | Avaliação completa de um jogo feita por um Jogador, com Nota Geral, Notas por Categoria e comentário opcional. Cada Jogador possui uma Review por jogo. |
| **Nota Geral** | Nota de 1 a 5 estrelas que resume a opinião do Jogador sobre o jogo. |
| **Nota por Categoria** | Nota de 1 a 5 atribuída a cada uma das cinco categorias da Review: Gráficos, Som, Jogabilidade, Diversão e Inovação. |
| **Tag "Editado"** | Sinalização exibida em uma Review que foi alterada após a publicação, acompanhada da data da última modificação. |
| **Estatística do Jogo** | Registro agregado com as médias da comunidade, o percentual de recomendação e os totais de zerados e de Wishlist de um jogo. |
| **Média Pública** | Nota média exibida na página do jogo. Só é liberada quando há pelo menos 3 Reviews ativas. |
| **Lista Personalizada** | Coleção temática de jogos criada pelo Jogador, com título único por usuário, descrição e Visibilidade. Limitada a 30 por conta. |
| **Visibilidade** | Estado de privacidade de uma Lista: Pública (padrão) ou Privada. |
| **Item da Lista** | Vínculo entre um jogo e uma Lista, com data de inclusão e Nota Explicativa opcional. Cada Lista comporta no máximo 100 itens. |
| **Nota Explicativa** | Texto curto em que o Jogador registra o motivo de ter incluído um jogo na Lista. |
| **Wishlist** | Lista oficial de desejos do Jogador. Existe exatamente uma por conta e é criada automaticamente na primeira adição. |
| **Prioridade** | Nível de interesse de um jogo na Wishlist: Alta, Média (padrão) ou Baixa. Define a ordem de exibição. |
| **Currently Playing** | Destaque do perfil com os jogos que o Jogador está jogando agora. Limitado a 5 jogos simultâneos. |
| **Progresso** | Estágio de avanço em um jogo de Currently Playing: "No início", "Na metade" ou "Perto do fim". |
| **Favorito** | Jogo fixado no topo do Perfil Público como um dos preferidos da vida do Jogador. |
| **Slot** | Posição de 1 a 4 ocupada por um Favorito. Um novo Favorito assume o menor Slot vago, e um jogo nunca ocupa dois Slots. |
| **Registro de Conclusão ("Zerado")** | Registro de que o Jogador terminou um jogo, com data, plataforma, horas jogadas e indicação de 100% (platina). Pode haver vários para o mesmo jogo (replays). |
| **Contador de Conclusões** | Total de vezes que um Jogador zerou um mesmo jogo, recalculado a cada novo Registro de Conclusão. |
| **Lançamento Futuro** | Jogo ainda não lançado, cadastrado com data prevista posterior à data atual do servidor. Seu status é "Futuro Lançamento" até a data chegar. |
| **Interesse em Lançamento** | Marcação "Quero jogar no lançamento" feita pelo Jogador, que ativa notificações. Funciona como toggle: clicar de novo remove o interesse. |
| **Similaridade** | Relação entre dois jogos calculada por gênero, mecânicas e desenvolvedora. É simétrica e nunca liga um jogo a ele mesmo. |
| **Grau de Similaridade** | Pontuação da Similaridade entre dois jogos, usada para ordenar as sugestões. É rebaixada quando o par recebe mais de 10 Feedbacks negativos. |
| **Feedback de Sugestão** | Avaliação de relevância de uma sugestão de jogo similar ("Sugestão útil" ou "Não tem nada a ver"). Cada Jogador registra um por par. |
| **Tópico** | Discussão aberta no fórum, geral ou associada a um jogo. Exige título com no mínimo 10 caracteres e mensagem com no mínimo 20. |
| **Resposta** | Mensagem publicada dentro de um Tópico. Não é aceita quando o Tópico está Encerrado. |
| **Tópico Encerrado** | Tópico marcado como resolvido ou finalizado, que deixa de receber novas Respostas. |
| **Data da Última Atividade** | Marca de tempo do Tópico atualizada a cada nova Resposta, junto com a contagem de respostas. |
| **Log de Moderação** | Registro obrigatório, com justificativa, moderador e data, criado quando alguém que não é o autor oculta ou remove uma postagem. |
| **Soft Delete (Exclusão Lógica)** | Exclusão que oculta o texto de um Tópico com Respostas, mas mantém sua estrutura no banco para preservar a consistência. |
| **Journal** | Diário de bordo pessoal do Jogador, com Entradas manuais e automáticas vinculadas a jogos do Catálogo. |
| **Entrada de Journal** | Registro do Journal com texto, jogo vinculado e data. Pode ser manual (limite de 10 por jogo por dia) ou automática. |
| **Spoiler** | Flag de uma Entrada manual que revela partes da história do jogo e exige confirmação de leitura antes da exibição a outros usuários. |
| **Entrada Automática** | Entrada gerada pelo sistema a partir de uma ação do Jogador, marcada como gerada automaticamente e não editável diretamente. |
| **Tipo de Evento** | Categoria da Entrada Automática: EVENTO_REVIEW, EVENTO_ZEROU ou EVENTO_WISHLIST. Define o formato fixo do texto gerado. |
| **Timeline** | Linha do tempo consolidada do Jogador, unindo Entradas manuais e automáticas do Journal em ordem de data. |
