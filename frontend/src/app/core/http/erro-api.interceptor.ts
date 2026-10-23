import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { catchError, throwError } from 'rxjs';

import { paraErroApi } from './erro-api';

/** Converte toda resposta de erro da API em ErroApi, para os componentes tratarem igual. */
export const erroApiInterceptor: HttpInterceptorFn = (req, next) =>
  next(req).pipe(
    catchError((erro: unknown) =>
      throwError(() => (erro instanceof HttpErrorResponse ? paraErroApi(erro) : erro)),
    ),
  );
