import { Component } from '@angular/core';
import { JournalEntradasComponent } from './journal-entradas.component';

@Component({
  selector: 'app-journal-page',
  imports: [JournalEntradasComponent],
  template: '<main class="pagina pagina--estreita"><app-journal-entradas /></main>',
})
export class JournalPage { }
