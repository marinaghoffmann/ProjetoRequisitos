import MarkdownIt from 'markdown-it';

// O HTML fornecido pelo usuário permanece texto. Angular sanitiza novamente a saída
// ao vinculá-la com [innerHTML]. Imagens entram apenas pelo fluxo de anexos do fórum.
const parser = new MarkdownIt({ html: false, breaks: true, linkify: false }).disable('image');

export function formatarMarkdownForum(conteudo: string | null): string {
  return parser.render(conteudo ?? '');
}
