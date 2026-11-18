# Journal individual — F12 do grupo / F-15 dos entregáveis

Branch de implementação: `feat/f12-journal`. A numeração oficial continua F-15 no CML e no Cucumber;
F-12 oficial é lançamentos, de outra pessoa.

## Como validar pela interface

1. Entre na conta e clique em **Journal** no menu. Também há a aba Journal no próprio perfil.
2. Em **Nova entrada**, escolha um jogo ou deixe sem associação, informe a data, título opcional e texto.
   Adicione tags separadas por vírgula e até três imagens. Salve e atualize a página.
3. Edite título, jogo, data, texto e tags. Remova uma imagem existente e acrescente outra. As alterações
   só são aplicadas quando clicar em **Salvar alterações**; Cancelar preserva a entrada anterior.
4. Teste uma quarta imagem, imagem maior que 2 MB, arquivo que não seja PNG/JPG/WebP e uma sétima tag.
   O frontend orienta o jogador e o domínio também recusa as violações.
5. Filtre por origem (manual/automática), jogo, tag e datas. O intervalo inclui as duas datas;
   um início posterior ao fim é recusado. **Limpar** restaura a consulta completa.
6. Marque uma anotação com spoiler. No próprio Journal o dono vê o conteúdo. Abra o perfil público
   (`/perfil/{id}`), inclusive em janela anônima: texto e imagens só aparecem após **Clique para mostrar**.
   **Ocultar spoiler** restaura a máscara. A resposta inicial da API não contém texto nem anexos ocultos.
7. Confira a timeline: as entradas manuais aparecem junto às automáticas. As automáticas são somente leitura.
8. Exclua uma anotação manual com imagens: ela some do Journal, do perfil público e da timeline;
   os registros dos anexos, seu conteúdo binário e as tags são removidos junto à entrada.
9. Uma segunda conta pode consultar o perfil público, mas não editar, excluir ou consultar o endpoint
   privado da entrada de outra conta. Os cenários BDD verificam as regras de propriedade.

No perfil H2 os dados somem ao encerrar o backend. No PostgreSQL ficam persistidos no volume do banco.

## Implementação

- Domínio Java puro: `EntradaDeJournal` (manual e automática), tags normalizadas, `AnexoJournal` e
  `ImagemDoJournal`. O limite do protótipo é seis tags, três imagens de até 2 MB, PNG/JPG/WebP;
  texto até 5.000 e título opcional até 120 caracteres.
- `GerenciarJournal` usa `ExecutorDeTransacao`. A edição bloqueia a linha da entrada, impedindo que
  inclusões simultâneas ultrapassem três imagens. Uma falha desfaz texto, tags e imagens juntos.
- JPA: `journal_entradas`, `journal_tags` e `journal_anexos`. Anexos são entidades internas ao agregado,
  com exclusão em cascata/orphan removal. Os bytes ficam no banco (`BYTEA` no PostgreSQL), sem uploads
  temporários ou arquivos órfãos no disco.
- O formulário envia os dados JSON e os novos arquivos num único multipart. `anexosMantidos` identifica
  as imagens existentes preservadas na edição; IDs de outra entrada são recusados.
- O Decorator `SpoilerMascaradoDecorator` envolve `ConteudoOriginal`, sem mudar a entidade.
  A consulta pública oculta também os metadados das imagens. O endpoint binário exige revelação para spoilers.
- A data informada no formulário é uma data de calendário, armazenada à meia-noite UTC para compatibilidade
  com a timeline existente. A tela exibe a data das anotações sem convertê-la para o dia anterior.

## Testes

`f15_journal.feature` usa os casos de uso e a persistência reais, incluindo remoção dos anexos, rollback,
filtros, limite de imagens e consulta pública com spoilers. `JournalRegrasTest` verifica o Decorator,
normalização de tags e rejeição de conteúdo/tamanho de mídia inválido. Os cenários existentes da F-16
continuam verificando a integração por eventos.

Com Java 21, em `backend/`: `./mvnw verify`. Em `frontend/`: `npm ci`, `npm test -- --watch=false`
e `npm run build -- --configuration development`.
