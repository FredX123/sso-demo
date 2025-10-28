import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { catchError, map, of, tap } from 'rxjs';
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
    const separator = this.autuzUrl.includes('?') ? '&' : '?';
    const url = `${this.autuzUrl}/load${separator}_=${Date.now()}`;

    return this.http.get<AuthMe>(url, { withCredentials: true, headers })
      .pipe(
        tap(me => this.authState.update(me)),
        map(me => me),
        catchError(() => of({ authenticated: false } as AuthMe))
      );
  }

  getAuthCache(opts?: { silent?: boolean }) {
    const headers = opts?.silent ? { [HDR_SILENT_AUTH]: 'true' } : undefined;
    const separator = this.autuzUrl.includes('?') ? '&' : '?';
    const url = `${this.autuzUrl}${separator}_=${Date.now()}`;
    
    return this.http.get<AuthMe>(url, { withCredentials: true, headers })
      .pipe(
        tap(me => this.authState.update(me)),
        map(me => me),
        catchError(() => of({ authenticated: false } as AuthMe))
      );
  }

  refresh() {
    console.log('Refreshinggggggggggggggggg token ......................');
    
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
}
