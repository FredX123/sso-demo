import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { catchError, map, Observable, of } from 'rxjs';
import { API_AUTH_BASE_URL, GATEWAY_BASE_URL } from '../core/tokens';
import { AuthMe } from '../model/auth.models';

@Injectable({
  providedIn: 'root'
})
export class AuthService {

  private readonly http = inject(HttpClient);
  private readonly authBase = inject<string>(API_AUTH_BASE_URL);
  private readonly gatewayBase = inject<string>(GATEWAY_BASE_URL);

/** GET /api/auth/me via the gateway; includes credentials (session cookie). */
  me() {
    return this.http.get<AuthMe>(`${this.authBase}/me`, { withCredentials: true })
      .pipe(
        map(r => r),
        catchError(() => of({ authenticated: false } as AuthMe))
      );
  }

  refresh() {
    return this.http.get(`${this.gatewayBase}/api/token/refresh`, { withCredentials: true });
  }

  logout() {
    // Spring Security default logout at the gateway; Okta RP-logout is handled server-side.
    return this.http.post(`${this.gatewayBase}/logout`, {}, { withCredentials: true, observe: 'response' });
  }
}
