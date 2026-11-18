import { Component } from '@angular/core';
import { JournalEntradasComponent } from './journal-entradas.component';

@Component({
  selector: 'app-journal-page',
  imports: [JournalEntradasComponent],
  template: '<main><app-journal-entradas /></main>',
  styles: 'main { width: min(1060px, calc(100% - 48px)); margin: 0 auto; padding: 48px 0 80px; }',
})
export class JournalPage { }
