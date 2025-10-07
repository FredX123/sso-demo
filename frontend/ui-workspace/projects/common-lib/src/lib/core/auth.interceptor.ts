import { HttpErrorResponse, HttpInterceptorFn, HttpRequest } from '@angular/common/http';
import { HttpClient } from '@angular/common/http';
import { inject, InjectionToken } from '@angular/core';
import { catchError, switchMap, throwError } from 'rxjs';

// ✅ Use an InjectionToken (not a string) for DI
export const GATEWAY_BASE_URL = new InjectionToken<string>('GATEWAY_BASE_URL');

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const http = inject(HttpClient);
  const gatewayBaseUrl = inject(GATEWAY_BASE_URL);

  // Always send cookies to the gateway (session-based auth)
  const withCreds = req.clone({ withCredentials: true });

  // Prevent infinite refresh loops: retry at most once per request
  const alreadyRetried = withCreds.headers.has('X-Retry');

  return next(withCreds).pipe(
    catchError((err: HttpErrorResponse) => {
      if (err.status === 401 && !alreadyRetried) {
        const retryReq: HttpRequest<any> = withCreds.clone({
          setHeaders: { 'X-Retry': '1' },
        });
        // Ask gateway to refresh (it uses the session's refresh token), then retry once
        return http.get(`${gatewayBaseUrl}/api/token/refresh`, { withCredentials: true }).pipe(
          switchMap(() => next(retryReq)),
          catchError(() => throwError(() => err)) // if refresh fails, surface original 401
        );
      }
      return throwError(() => err);
    })
  );
};
