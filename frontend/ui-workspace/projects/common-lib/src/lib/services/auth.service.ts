import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { catchError, of, tap } from 'rxjs';
import { API_AUTH_BASE_URL, GATEWAY_BASE_URL } from '../core/tokens';
import { AuthMe } from '../model/auth.models';
import { AuthStateService } from './auth-state.service';
import { HDR_SILENT_AUTH } from '../core/constants';

@Injectable({
  providedIn: 'root'
})
export class AuthService {

  private readonly http = inject(HttpClient);
  private readonly authBase = inject<string>(API_AUTH_BASE_URL);
  private readonly gatewayBase = inject<string>(GATEWAY_BASE_URL);
  private readonly authState = inject(AuthStateService);

  autuzUrl = `${this.authBase}/authorizations`;

  /** Pull latest auth state from server and update shared state */
  /** set {silent:true} to avoid refresh/dialog on 401 */
  initAppAuth(opts?: { silent?: boolean }) {
    const headers = opts?.silent ? { [HDR_SILENT_AUTH]: 'true' } : undefined;
    // NOTE: path is per app: /api/<frontoffice|backoffice>/auth/authorizations/load
    const url = `${this.autuzUrl}/load`;

    return this.http.get<AuthMe>(url, { withCredentials: true, headers })
      .pipe(
        tap(me => this.authState.update(me)),
        catchError(() => of({ authenticated: false } as AuthMe))
      );
  }

  /**
   * Updates and caches the user's session based on the provided JWT
   */
  touch(opts?: { silent?: boolean }) {
    const headers = opts?.silent ? { [HDR_SILENT_AUTH]: 'true' } : undefined;
    return this.http.get<AuthMe>(`${this.autuzUrl}/touch`, { withCredentials: true, headers })
      .pipe(
        tap(me => this.authState.update(me)),
        catchError(() => of({ authenticated: false } as AuthMe))
      );
  }

  refresh() {
    return this.http.get(`${this.gatewayBase}/api/token/refresh`, { withCredentials: true });
  }

  logout(): void {
    this.authState.clear();

    const target = `${this.gatewayBase}/logout`;

    if (typeof window !== 'undefined' && window.document?.body) {
      const form = window.document.createElement('form');
      form.method = 'POST';
      form.action = target;
      form.style.display = 'none';

      window.document.body.appendChild(form);
      form.submit();
      return;
    }

    throw new Error('logout() requires a browser environment to redirect.');
  }

  /** TODO Demo Only */
  me(opts?: { silent?: boolean }) {
    const headers = opts?.silent ? { [HDR_SILENT_AUTH]: 'true' } : undefined;
    
    return this.http.get<AuthMe>(this.autuzUrl, { withCredentials: true, headers })
      .pipe(
        tap(me => this.authState.update(me)),
        catchError(() => of({ authenticated: false } as AuthMe))
      );
  }

}
