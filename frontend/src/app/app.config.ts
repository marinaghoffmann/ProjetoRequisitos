import { registerLocaleData } from '@angular/common';
import { provideHttpClient, withInterceptors } from '@angular/common/http';
import localePt from '@angular/common/locales/pt';
import { ApplicationConfig, LOCALE_ID, provideBrowserGlobalErrorListeners } from '@angular/core';
import { provideRouter } from '@angular/router';

import { routes } from './app.routes';
import { autenticacaoInterceptor } from './core/auth/autenticacao.interceptor';
import { erroApiInterceptor } from './core/http/erro-api.interceptor';

// Datas, números e moeda no formato brasileiro (R$ 1.234,56; 18 de nov. de 2026).
registerLocaleData(localePt, 'pt-BR');

export const appConfig: ApplicationConfig = {
  providers: [
    provideBrowserGlobalErrorListeners(),
    { provide: LOCALE_ID, useValue: 'pt-BR' },
    provideRouter(routes),
    provideHttpClient(withInterceptors([autenticacaoInterceptor, erroApiInterceptor])),
  ],
};
