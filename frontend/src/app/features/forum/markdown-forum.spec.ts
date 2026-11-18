import { describe, expect, it } from 'vitest';
import { formatarMarkdownForum } from './markdown-forum';

describe('formatação do fórum', () => {
  it('formata Markdown e preserva quebras de linha', () => {
    const html = formatarMarkdownForum('**destaque**\nsegunda linha\n\n- item');
    expect(html).toContain('<strong>destaque</strong>');
    expect(html).toContain('<br>');
    expect(html).toContain('<li>item</li>');
  });

  it('não cria HTML bruto, imagens embutidas ou links javascript', () => {
    const html = formatarMarkdownForum('<script>alert(1)</script> ![foto](https://exemplo.com/a.png) [abrir](javascript:alert(1))');
    expect(html).not.toContain('<script>');
    expect(html).not.toContain('<img');
    expect(html).not.toContain('href="javascript:');
  });
});
