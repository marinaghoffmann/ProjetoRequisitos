# Domínio de Negócio: Ecossistema Game Tracker

O ecossistema **Game Tracker** organiza-se em torno do registro da jornada do Jogador, da confiabilidade do Catálogo e de uma comunidade que conversa, avalia e negocia jogos. Inspirado em redes de registro cultural como o Letterboxd, o fluxo integra a formação do Catálogo (importação de Base Externa ou cadastro pelo próprio Criador), a avaliação por categorias, a organização da biblioteca pessoal, o Journal de jogatina, o fórum moderado e um Marketplace entre Jogadores com negociação, pagamento em Custódia e reputação. Em todo o domínio valem três princípios: o dado do jogo é único e rastreável até sua Origem; somente o dono de um registro pode alterá-lo; e toda ação relevante do Jogador deixa rastro na sua Timeline e nas Estatísticas do Jogo.

---

## Persona - Desenvolvedor (Criador de Jogos) e Administrador

**F-01 Importação, Cadastramento Manual e Gerenciamento do Catálogo de Jogos e Desenvolvedoras:** O Catálogo é alimentado por duas Origens. Na Origem **API**, o sistema busca jogos em uma Base Externa confiável (IGDB ou RAWG) e, antes de gravar, verifica se o Identificador Externo já existe no banco local: se existir, atualiza o registro em vez de inserir um novo. Na Origem **Manual/Criador**, um usuário com papel de Desenvolvedor cadastra o jogo diretamente, sem depender da Base Externa; para isso ele precisa estar vinculado a um Perfil de Desenvolvedora ativo, ao qual o jogo fica automaticamente associado, e o sistema garante que não exista outro jogo com o mesmo título para a mesma Desenvolvedora. O Desenvolvedor também mantém o Perfil Institucional da sua Desenvolvedora (biografia, site, redes sociais e país). O Administrador pode editar metadados ou remover jogos e Desenvolvedoras; sempre que houver dados vinculados (Reviews, Entradas de Journal ou presença em Listas), a remoção é obrigatoriamente uma Exclusão Lógica, preservando a integridade referencial. A consulta filtra o Catálogo local por título, gênero, Desenvolvedora, Origem (API ou Criador) e Status de Ativação.

**F-12 Gerenciamento de Lançamentos Futuros, Agenda de Alertas e Pré-cadastros:** Administradores e Criadores cadastram, editam e excluem Lançamentos Futuros. A inclusão exige uma Data Prevista de Lançamento posterior à data atual; quando o sistema atinge essa data, o status muda automaticamente de "Futuro Lançamento" para "Lançado". O Jogador marca ou desmarca a qualquer momento o Interesse em Lançamento ("Quero jogar no lançamento") e acompanha a contagem regressiva. Para os inscritos, o sistema agenda no banco uma Notificação de Lançamento a ser disparada 24 horas antes da data oficial. Se a data for adiada ou alterada, o registro é atualizado e gera uma entrada no Histórico de Alterações de Cronograma, que avisa os Jogadores interessados. A consulta lista os Lançamentos Futuros em ordem de data, destacando o status de alerta do Jogador autenticado.

---

## Persona - Jogador

**F-02 Gerenciamento Completo de Reviews e Avaliações Categoriais:** O Jogador publica uma Review de um jogo do Catálogo com uma Nota Geral de 1 a 5, cinco Notas por Categoria (Gráficos, Som, Jogabilidade, Diversão e Inovação) e um texto descritivo. Cada Jogador possui uma única Review por jogo: ao tentar avaliar novamente um jogo já avaliado, ele é redirecionado ao fluxo de edição. Somente o autor pode editar as notas e o texto ou excluir a Review, e o sistema valida no backend que o identificador do usuário autenticado é exatamente o do autor antes de qualquer alteração. Na exclusão, todas as Notas por Categoria vinculadas são removidas em cascata. A consulta apresenta as Reviews de um jogo com as médias por categoria, ou as Reviews criadas pelo próprio Jogador.

**F-04 Gerenciamento Completo de Listas Personalizadas e seus Itens:** O Jogador cria, edita e exclui Listas Personalizadas temáticas, como "Melhores RPGs", e gerencia seus Itens: adiciona jogos, remove jogos e escreve uma Nota Explicativa para cada Item. O título da Lista é único por Jogador, sem diferenciar maiúsculas de minúsculas; um mesmo jogo não pode aparecer duas vezes na mesma Lista; e cada Lista comporta no máximo 100 jogos. A consulta exibe todas as Listas de um Jogador com o total e a relação detalhada de jogos de cada uma.

**F-06 Gerenciamento da Wishlist:** Cada Jogador possui exatamente uma Wishlist, criada automaticamente na primeira tentativa de adicionar um jogo. Nela ele registra os jogos que deseja jogar, define a Prioridade de cada um (Alta, Média ou Baixa) e remove jogos quando quiser. Quando nenhuma Prioridade é informada, o sistema assume Média, e o mesmo jogo nunca aparece duas vezes na Wishlist do mesmo Jogador. A consulta exibe a Wishlist do Jogador autenticado ou de outro Jogador, ordenada por Prioridade.

**F-07 Marcação de Currently Playing:** O Jogador destaca no perfil os jogos que está jogando no momento e atualiza o Progresso de cada um ("No início", "Na metade" ou "Perto do fim"). Ao marcar um jogo, o sistema registra a data e a hora de início. São permitidos no máximo 5 jogos simultâneos: ao tentar marcar um sexto, o sistema exige que o Jogador remova ou substitua um dos existentes antes de gravar. Ao concluir o jogo, ele o retira do destaque. A consulta apresenta os jogos em andamento no Perfil Público.

**F-08 Gerenciamento de Jogos Favoritos e Personalização do Perfil Público:** O Jogador fixa até 4 Favoritos no Perfil Público, organizados em Slots de 1 a 4, e pode adicioná-los, removê-los, substituí-los e reordená-los a qualquer momento; ao mover ou adicionar um Favorito, o sistema grava a posição exata e reordena os demais automaticamente. Um mesmo jogo nunca ocupa mais de um Slot. O Jogador também personaliza o perfil com uma Biografia de até 500 caracteres e uma Capa de Perfil, cuja URL é validada quanto ao formato e à extensão permitida (.png, .jpg ou .webp). A consulta apresenta os dados públicos do perfil, com Biografia, Capa e Favoritos do Slot 1 ao 4.

**F-11 Gerenciamento de Registros de "Zerado" e Contagem de Conclusões:** O Jogador registra que concluiu um jogo informando a Data de Conclusão, a Plataforma, as horas jogadas e a marcação "100%/Platina", e pode editar ou excluir esses Registros de Conclusão. A data é obrigatória e não pode ser posterior à data e hora atual do servidor. As horas jogadas são opcionais, mas, quando informadas, devem ser um número positivo, e a marcação de Platina só é aceita se a conclusão principal estiver ativa. Somente o dono do registro pode alterá-lo ou excluí-lo; a cada inclusão ou exclusão o sistema atualiza o Contador de Conclusões do Jogador para aquele jogo. A consulta apresenta o histórico de conclusões de um jogo por um Jogador, da mais recente para a mais antiga.

**F-15 Gerenciamento Completo do Journal Individual:** O Journal é o diário de bordo do Jogador. Ele cria, consulta, edita e exclui Entradas de Journal, podendo associá-las a um jogo, definir a data da sessão, atribuir Tags personalizadas, anexar imagens e marcar a presença de Spoiler. Apenas o dono do Journal pode alterar ou excluir suas Entradas. Cada Entrada aceita no máximo 3 Anexos de imagem, e ao editar ou excluir a Entrada o sistema atualiza ou apaga os Anexos vinculados. Entradas marcadas com Spoiler ficam mascaradas no Perfil Público e só são exibidas após um clique explícito do leitor. A consulta filtra as Entradas por jogo, Tag ou intervalo de datas.

---

## Persona - Jogador no Fórum e Moderador

**F-13 Gerenciamento Completo do Fórum de Discussões:** O Jogador cria, edita e exclui seus Tópicos, gerais ou atrelados a um jogo, e publica, edita ou remove Respostas encadeadas dentro deles, com suporte a formatação de texto. Cada Tópico tem um Status (Aberto, Resolvido ou Encerrado); um Tópico Encerrado não aceita novas Respostas, edições nem exclusões por usuários comuns. Somente o autor pode editar ou excluir sua postagem, e a edição só é permitida até 48 horas após a publicação. Toda criação, alteração ou exclusão de Resposta atualiza o Contador de Respostas do Tópico e a Data da Última Atividade. A consulta lista os Tópicos de forma paginada, por jogo ou categoria geral, com o número de respostas e a última interação.

**F-14 Central de Moderação, Auditoria e Denúncias:** Qualquer Jogador pode registrar uma Denúncia sobre um Tópico, uma Resposta ou um comentário. Apenas usuários com papel de Moderador ou Administrador acessam a Central de Moderação, onde analisam a Fila de Denúncias e aplicam Ações Moderativas: ocultar, editar, mover ou excluir conteúdo, encerrar discussões e registrar Advertências a usuários. Toda remoção ou ocultação exige uma Justificativa textual. Cada ação gera um registro imutável no Log de Moderação, com o identificador do moderador, o Tipo de Ação, o conteúdo afetado, a justificativa e o carimbo de data e hora. A remoção de postagens é uma Exclusão Lógica: o texto é substituído por uma Mensagem de Sistema ("Esta postagem foi removida por violar as diretrizes"), mantendo a árvore de Respostas coesa. A consulta apresenta a Fila de Denúncias pendentes e o histórico do Log de Moderação.

---

## Persona - Comprador e Vendedor (Marketplace)

**F-18 Gerenciamento Completo de Anúncios do Marketplace:** O Vendedor cadastra jogos físicos ou digitais da sua coleção para venda ou troca, informando o jogo do Catálogo, o Estado de Conservação (Novo, Excelente, Usado ou Com marcas), o preço, as fotos do produto real, o Tipo de Negociação (Venda, Troca ou Ambos) e a localidade de envio. Todo Anúncio precisa estar vinculado a um jogo do Catálogo e conter pelo menos 1 Foto do Anúncio. O preço deve ser numérico e maior que zero. O Anúncio percorre o Ciclo de Vida Ativo, Pausado, Em Negociação, Vendido ou Cancelado, e somente o proprietário pode alterar seu preço ou status. Anúncios Vendidos deixam de aparecer na busca pública. A consulta filtra os Anúncios por jogo, faixa de preço, Estado de Conservação, Tipo de Negociação e região.

**F-19 Chat Interno de Negociação e Propostas:** A partir de um Anúncio Ativo, o Comprador abre uma Conversa com o Vendedor; o sistema impede Conversas duplicadas entre os mesmos dois usuários para o mesmo Anúncio. Na Conversa, as partes trocam Mensagens e o Comprador pode enviar Propostas formais de valor ou de troca ("Ofereço R$ 120,00" ou "Troco pelo jogo X"). Toda Proposta nasce Pendente; se o Vendedor a aceita, ela passa a Aceita, o item é reservado e o Anúncio muda para Em Negociação; se recusa, passa a Recusada. O conteúdo da Conversa é visível exclusivamente para o Comprador e o Vendedor envolvidos. A consulta lista as Conversas ativas do usuário, como comprador ou vendedor, com o status da última Proposta e as mensagens não lidas.

**F-20 Processamento do Pedido, Checkout e Confirmação de Entrega:** Após o aceite da Proposta, o Comprador gera o Pedido escolhendo a Forma de Pagamento (Cartão simulado, PIX ou Saldo da Carteira) e informando o Endereço de Entrega. O valor pago fica retido Em Custódia e só é liberado para a Carteira do Vendedor quando o Comprador confirma o recebimento ou quando expira o Prazo de Tolerância de 7 dias após a entrega. Depois da confirmação do pagamento, o Vendedor tem até 3 dias úteis para registrar o Código de Rastreio ou o método de entrega; se o prazo expirar, o Pedido é cancelado automaticamente e o Comprador é reembolsado. Com o Pedido Concluído, o Anúncio passa a Vendido e o sistema publica um evento para que a compra seja registrada no Journal do Comprador (F-16). A consulta lista os Pedidos do usuário, compras e vendas, com o status da entrega e do pagamento.

**F-21 Reputação e Avaliações de Vendedores e Compradores:** Ao final de um Pedido Concluído, Comprador e Vendedor podem avaliar a experiência com uma nota de 1 a 5 estrelas, Tags de Comportamento ("Envio rápido", "Produto como descrito", "Bom comunicador") e um comentário. A avaliação só pode ser criada se existir um Pedido Concluído entre os dois usuários; cada parte avalia uma única vez por Pedido, em até 15 dias após a confirmação de entrega. Cada nova avaliação recalcula imediatamente a Reputação do usuário avaliado e incrementa seu total de Negociações Bem-sucedidas. A consulta exibe o histórico de avaliações recebidas por um Vendedor com sua nota média de Reputação.

---

## Persona - Sistema (Motores Automáticos)

**F-10 Engine de Sugestão de Similares e Central de Feedback de Relevância:** O sistema calcula e grava relações de Similaridade entre jogos a partir de gêneros, tags e estúdios. Um jogo nunca é sugerido a ele mesmo e a relação é bidirecional: se A é similar a B, B também é similar a A com o mesmo Grau de Relevância. Os Jogadores registram, editam e removem um Feedback de Sugestão ("Útil" ou "Não relevante"), limitado a um por usuário por par de jogos; quando um par ultrapassa 15 Feedbacks negativos, o sistema rebaixa automaticamente seu Grau de Relevância. Administradores podem criar ou remover manualmente uma Similaridade Manual (Override) entre dois jogos, que prevalece sobre o cálculo automático. A consulta lista os similares de um jogo ordenados pelo Grau de Relevância, com filtro por avaliação da comunidade.

**F-16 Automação da Linha do Tempo e Integração de Eventos no Journal:** O sistema atua como um barramento interno de Eventos de Plataforma: quando o Jogador publica uma Review, registra um jogo zerado ou conclui uma compra no Marketplace, uma Entrada Automática é gerada no seu Journal. A gravação ocorre na mesma transação da ação de origem; se a ação falhar, a Entrada sofre rollback. Quando a ação de origem é excluída (por exemplo, um Registro de Conclusão), a Entrada Automática correspondente é removida ou atualizada em cascata. Entradas com Tipo de Origem AUTOMATICO não podem ter o texto principal alterado manualmente pelo Jogador. A consulta apresenta a Timeline unificada, com Entradas manuais e automáticas.

**F-17 Engine de Estatísticas e Métricas da Comunidade:** O sistema consolida em uma tabela agregada as Estatísticas do Jogo: Média Geral, médias por categoria (Gráficos, Som, Jogabilidade, Diversão e Inovação), total de conclusões, presença em Wishlists e volume de negociações no Marketplace. Sempre que uma Review, um Registro de Conclusão ou uma transação do Marketplace é criada, editada ou excluída, os valores do jogo correspondente são recalculados. Todas as médias e percentuais são gravados com precisão fixa de duas casas decimais. As Estatísticas Públicas só são exibidas quando o jogo possui pelo menos 3 Reviews ativas. A consulta retorna a ficha consolidada para a página principal do jogo.

---

## Glossário da Linguagem Onipresente

| Termo do Negócio | Significado / Contexto dentro do Sistema |
|---|---|
| **Jogador** | Usuário registrado da plataforma. Avalia jogos, organiza sua biblioteca, mantém o Journal, participa do fórum, negocia no Marketplace e possui um Perfil Público. |
| **Desenvolvedor (Criador de Jogos)** | Papel de usuário vinculado a um Perfil de Desenvolvedora ativo. Pode cadastrar jogos manualmente no Catálogo e manter o perfil institucional do estúdio. |
| **Administrador** | Papel de gestão. Edita metadados e remove jogos e Desenvolvedoras, gerencia Lançamentos Futuros, cria Similaridades Manuais e acessa a Central de Moderação. |
| **Moderador** | Papel que acessa a Central de Moderação para analisar Denúncias e aplicar Ações Moderativas no fórum e nos comentários. |
| **Comprador / Vendedor** | Papéis contextuais que um Jogador assume em uma negociação do Marketplace. Não são perfis fixos: o mesmo Jogador pode vender um jogo e comprar outro. |
| **Perfil Público** | Página do Jogador com Biografia, Capa de Perfil, Favoritos, jogos em Currently Playing e Entradas públicas do Journal. |
| **Biografia** | Texto de apresentação do Perfil Público, limitado a 500 caracteres. |
| **Capa de Perfil** | Imagem de destaque do Perfil Público. A URL só é aceita com extensão .png, .jpg ou .webp. |
| **Jogo** | Título do Catálogo. Entidade central do sistema, em torno da qual se organizam Reviews, Listas, Wishlist, Journal, conclusões, Tópicos e Anúncios. |
| **Catálogo** | Conjunto de jogos armazenados localmente, alimentado pela Base Externa ou pelo cadastro manual de Criadores. |
| **Base Externa** | Fonte de dados confiável e atualizada (IGDB ou RAWG) da qual o sistema importa jogos. |
| **Identificador Externo** | Código do jogo na Base Externa. Se já existe no banco local, a importação atualiza o registro em vez de inserir outro. |
| **Origem do Cadastro** | Indica como o jogo entrou no Catálogo: API (Base Externa) ou Manual/Criador. |
| **Status de Ativação** | Estado do jogo ou da Desenvolvedora no Catálogo: Ativo ou Inativo (excluído logicamente). |
| **Desenvolvedora** | Estúdio ou desenvolvedor independente responsável por jogos, com Perfil Institucional (biografia, site, redes e país). |
| **Gênero** | Classificação temática do jogo (RPG, Aventura, Estratégia etc.), usada em filtros e no cálculo de Similaridade. |
| **Plataforma** | Sistema em que o jogo pode ser jogado (PC, console, mobile), usada em filtros e no Registro de Conclusão. |
| **Exclusão Lógica (Soft Delete)** | Remoção que marca o registro como inativo ou oculto sem apagá-lo do banco, preservando os dados vinculados. |
| **Review** | Avaliação de um jogo feita por um Jogador, com Nota Geral, Notas por Categoria e texto. Existe no máximo uma por Jogador e jogo. |
| **Nota Geral** | Nota de 1 a 5 que resume a opinião do Jogador sobre o jogo. |
| **Nota por Categoria** | Nota atribuída a cada categoria da Review: Gráficos, Som, Jogabilidade, Diversão e Inovação. É removida em cascata com a Review. |
| **Estatística do Jogo** | Registro agregado com médias da comunidade, total de conclusões, presença em Wishlists e volume de negociações de um jogo. |
| **Estatísticas Públicas** | Ficha de métricas exibida na página do jogo. Só é liberada quando há pelo menos 3 Reviews ativas. |
| **Lista Personalizada** | Coleção temática de jogos criada pelo Jogador, com título único por usuário e no máximo 100 jogos. |
| **Item da Lista** | Vínculo entre um jogo e uma Lista, com Nota Explicativa opcional. Um jogo aparece uma única vez por Lista. |
| **Nota Explicativa** | Texto curto que justifica a presença de um jogo em uma Lista. |
| **Wishlist** | Lista oficial de desejos do Jogador. Existe exatamente uma por conta e é criada automaticamente na primeira adição. |
| **Prioridade** | Nível de interesse em um jogo da Wishlist: Alta, Média (padrão) ou Baixa. Define a ordem de exibição. |
| **Currently Playing** | Destaque do perfil com os jogos que o Jogador está jogando agora, limitado a 5 simultâneos. |
| **Progresso** | Estágio de avanço em um jogo de Currently Playing: "No início", "Na metade" ou "Perto do fim". |
| **Favorito** | Jogo fixado no Perfil Público como um dos preferidos do Jogador. |
| **Slot** | Posição de 1 a 4 ocupada por um Favorito. Um jogo nunca ocupa dois Slots. |
| **Registro de Conclusão ("Zerado")** | Registro de que o Jogador terminou um jogo, com data, Plataforma, horas jogadas e marcação de Platina. |
| **Platina (100%)** | Marcação de conclusão completa de um jogo. Só é aceita quando a conclusão principal está ativa. |
| **Contador de Conclusões** | Total de vezes que um Jogador zerou um jogo. Aumenta a cada novo registro e diminui a cada exclusão. |
| **Lançamento Futuro** | Jogo ainda não lançado, com Data Prevista de Lançamento posterior à data atual. Passa a "Lançado" quando a data chega. |
| **Interesse em Lançamento** | Marcação "Quero jogar no lançamento" feita pelo Jogador; pode ser ativada e desativada a qualquer momento. |
| **Notificação de Lançamento** | Alerta agendado no banco para ser disparado 24 horas antes do lançamento aos Jogadores interessados. |
| **Histórico de Alterações de Cronograma** | Registro de cada mudança na Data Prevista de Lançamento, usado para avisar os interessados. |
| **Similaridade** | Relação bidirecional entre dois jogos calculada por gêneros, tags e estúdio. Nunca liga um jogo a ele mesmo. |
| **Grau de Relevância** | Pontuação de uma Similaridade, usada para ordenar as sugestões. É rebaixado quando o par ultrapassa 15 Feedbacks negativos. |
| **Feedback de Sugestão** | Avaliação de relevância de uma sugestão ("Útil" ou "Não relevante"). Um por Jogador e por par de jogos. |
| **Similaridade Manual (Override)** | Conexão de similaridade criada ou removida por um Administrador, que prevalece sobre o cálculo automático. |
| **Journal** | Diário de bordo pessoal do Jogador, com Entradas manuais e automáticas. |
| **Entrada de Journal** | Registro do Journal com texto, data da sessão, jogo opcional, Tags, Anexos e indicação de Spoiler. |
| **Anexo de Journal** | Imagem vinculada a uma Entrada. Cada Entrada aceita no máximo 3. |
| **Spoiler** | Marcação de uma Entrada que revela partes da história; o texto fica mascarado até o leitor clicar para exibi-lo. |
| **Tipo de Origem** | Indica se a Entrada foi escrita pelo Jogador (MANUAL) ou gerada pelo sistema (AUTOMATICO). |
| **Evento de Plataforma** | Fato relevante publicado por outra funcionalidade (Review publicada, jogo zerado, compra concluída) que gera uma Entrada Automática. |
| **Entrada Automática** | Entrada criada pelo sistema a partir de um Evento de Plataforma. Seu texto principal não pode ser editado pelo Jogador. |
| **Timeline** | Linha do tempo unificada do Jogador, com Entradas manuais e automáticas em ordem cronológica. |
| **Tópico** | Discussão do fórum, geral ou atrelada a um jogo, com Status Aberto, Resolvido ou Encerrado. |
| **Resposta** | Mensagem publicada dentro de um Tópico, podendo responder a outra Resposta (encadeamento). |
| **Tópico Encerrado** | Tópico que não aceita novas Respostas, edições nem exclusões por usuários comuns. |
| **Janela de Edição** | Prazo de 48 horas após a publicação dentro do qual o autor pode editar um Tópico ou uma Resposta. |
| **Contador de Respostas / Data da Última Atividade** | Indicadores do Tópico atualizados a cada criação, edição ou exclusão de Resposta. |
| **Denúncia** | Sinalização feita por um Jogador sobre um conteúdo que possivelmente viola as diretrizes. Entra na Fila de Denúncias. |
| **Fila de Denúncias** | Conjunto de Denúncias pendentes de análise na Central de Moderação. |
| **Central de Moderação** | Área restrita a Moderadores e Administradores para analisar Denúncias e aplicar Ações Moderativas. |
| **Ação Moderativa** | Intervenção sobre um conteúdo ou usuário: ocultar, editar, mover, excluir, encerrar discussão ou advertir. |
| **Justificativa** | Texto obrigatório que explica toda remoção ou ocultação feita pela moderação. |
| **Advertência** | Punição registrada contra um usuário por violação das diretrizes. |
| **Log de Moderação** | Registro imutável de cada Ação Moderativa, com moderador, tipo de ação, conteúdo afetado, justificativa e data e hora. |
| **Mensagem de Sistema** | Texto que substitui o conteúdo de uma postagem removida pela moderação, mantendo a árvore de Respostas. |
| **Marketplace** | Área de compra, venda e troca de jogos físicos ou digitais entre Jogadores. |
| **Anúncio** | Oferta de um jogo do Catálogo feita por um Vendedor, com preço, fotos, Estado de Conservação, Tipo de Negociação e localidade. |
| **Estado de Conservação** | Condição do item anunciado: Novo, Excelente, Usado ou Com marcas. |
| **Tipo de Negociação** | Modalidade aceita no Anúncio: Venda, Troca ou Ambos. |
| **Foto do Anúncio** | Imagem do produto real. Todo Anúncio exige pelo menos uma. |
| **Ciclo de Vida do Anúncio** | Sequência de status do Anúncio: Ativo, Pausado, Em Negociação, Vendido ou Cancelado. |
| **Conversa** | Canal privado entre Comprador e Vendedor aberto a partir de um Anúncio Ativo. Existe no máximo uma por par de usuários e Anúncio. |
| **Proposta** | Oferta formal de valor ou troca enviada na Conversa. Status: Pendente, Aceita ou Recusada. |
| **Reserva** | Efeito do aceite de uma Proposta: o Anúncio passa a Em Negociação e sai da disputa de outros compradores. |
| **Pedido** | Registro da transação gerado após o aceite da Proposta, com Forma de Pagamento, Endereço de Entrega e status. |
| **Forma de Pagamento** | Meio escolhido no checkout: Cartão (simulado), PIX ou Saldo da Carteira. |
| **Carteira** | Saldo interno do usuário, criado automaticamente na primeira transação e usado como Forma de Pagamento e como destino dos valores liberados ao Vendedor. |
| **Custódia (Escrow)** | Retenção do valor pago até a confirmação de recebimento pelo Comprador ou o fim do Prazo de Tolerância. |
| **Prazo de Tolerância** | Período de 7 dias após a entrega ao fim do qual o valor em Custódia é liberado automaticamente ao Vendedor. |
| **Código de Rastreio** | Identificador de envio que o Vendedor deve registrar em até 3 dias úteis após a confirmação do pagamento. |
| **Avaliação de Negociação** | Nota de 1 a 5, Tags de Comportamento e comentário que uma parte dá à outra após um Pedido Concluído. |
| **Tags de Comportamento** | Rótulos da Avaliação de Negociação, como "Envio rápido", "Produto como descrito" e "Bom comunicador". |
| **Reputação** | Média das Avaliações de Negociação recebidas por um usuário, com o total de Negociações Bem-sucedidas. |
