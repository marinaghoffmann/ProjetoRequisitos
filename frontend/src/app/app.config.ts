import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { ApplicationConfig, provideBrowserGlobalErrorListeners } from '@angular/core';
import { provideRouter } from '@angular/router';

import { routes } from './app.routes';
import { autenticacaoInterceptor } from './core/auth/autenticacao.interceptor';
import { erroApiInterceptor } from './core/http/erro-api.interceptor';

export const appConfig: ApplicationConfig = {
  providers: [
    provideBrowserGlobalErrorListeners(),
    provideRouter(routes),
    provideHttpClient(withInterceptors([autenticacaoInterceptor, erroApiInterceptor])),
  ],
};
