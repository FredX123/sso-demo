import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { catchError, map, Observable, of, tap } from 'rxjs';
import { API_AUTH_BASE_URL, GATEWAY_BASE_URL } from '../core/tokens';
import { AuthMe } from '../model/auth.models';
import { AuthStateService } from './auth-state.service';

@Injectable({
  providedIn: 'root'
})
export class AuthService {

  private readonly http = inject(HttpClient);
  private readonly authBase = inject<string>(API_AUTH_BASE_URL);
  private readonly gatewayBase = inject<string>(GATEWAY_BASE_URL);
  private readonly authState = inject(AuthStateService);

  /** Pull latest auth state from server and update shared state */
  me() {
    return this.http.get<AuthMe>(`${this.authBase}/me`, { withCredentials: true })
      .pipe(
        tap(me => this.authState.update(me)),
        map(me => me),
        catchError(() => of({ authenticated: false } as AuthMe))
      );
  }

  refresh() {
    return this.http.get(`${this.gatewayBase}/api/token/refresh`, { withCredentials: true });
  }

  logout() {
    // Spring Security default logout at the gateway; Okta RP-logout is handled server-side.
    return this.http.post(`${this.gatewayBase}/logout`, {}, { withCredentials: true, observe: 'response' })
    .pipe(
      tap(() => this.authState.clear())
    );
  }
}
