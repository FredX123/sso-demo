import { HttpInterceptorFn, HttpErrorResponse } from '@angular/common/http';
import { inject } from '@angular/core';
import { GATEWAY_BASE_URL } from './tokens';
import { catchError, switchMap, throwError } from 'rxjs';
import { AuthService, DialogService } from 'common-lib';
import { HDR_SILENT_AUTH } from './constants';
import { Router } from '@angular/router';

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const router = inject(Router);
  const gatewayBaseUrl = inject(GATEWAY_BASE_URL);
  const dialog = inject(DialogService);
  const auth = inject(AuthService);

  // Endpoints we must NOT try to refresh for (to avoid loops)
  const REFRESH_URL = `${gatewayBaseUrl}/api/token/refresh`;
  const EXCLUDED_PREFIXES = [
    REFRESH_URL,
    `${gatewayBaseUrl}/logout`,
    `${gatewayBaseUrl}/oauth2/authorization/`, // any OIDC start
  ];

  const isExcluded = EXCLUDED_PREFIXES.some(u => req.url.startsWith(u));
  const isSilent   = req.headers.has(HDR_SILENT_AUTH);

  // Always send cookies (gateway session)
  const withCreds = req.clone({ withCredentials: true });
  let triedRefresh = false;

  // If this request is to an excluded URL, just pass it through unchanged.
  if (isExcluded) {
    return next(withCreds);
  }

  return next(withCreds).pipe(
    catchError((err: HttpErrorResponse) => {
      // 1) NEW: handle 403 → just navigate
      if (err.status === 403) {
        router.navigate(['/access-denied']);
        return throwError(() => err);
      }

      // If this request opted into silent behavior, do NOT try refresh and do NOT show dialog.
      if (isSilent) {
        return throwError(() => err);
      }

      // Only handle 401 once, and never for excluded targets
      if (err.status === 401 && !triedRefresh) {
        triedRefresh = true;
        console.log('refresh token - auth interceptor');

        // Silent refresh only updates tokens
        return auth.refresh().pipe(
          // refresh succeeded → pull /authorizations (to update header) → retry original request
          switchMap(() => auth.touch({ silent: true })),
          // Retry original request with session cookie
          switchMap(() => next(withCreds)),
          // refresh failed (likely not logged in) → show dialog and stop
          catchError(() => {
            dialog.open(
              'Your session has expired or you are not logged in. Please sign in again.',
              'Authentication Required'
            );
            return throwError(() => err);
          })
        );
      }

      // Any other error (or second 401) → propagate
      return throwError(() => err);
    })
  );
};
