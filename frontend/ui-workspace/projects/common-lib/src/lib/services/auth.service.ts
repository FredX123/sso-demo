import { HttpClient } from '@angular/common/http';
import { inject, Injectable, InjectionToken } from '@angular/core';
import { GATEWAY_BASE_URL } from 'common-lib';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class AuthService {

  gatewayBaseUrl = inject(GATEWAY_BASE_URL);

  constructor(private http: HttpClient) { }

  whoAmI(): Observable<any> {
    return this.http.get(`${this.gatewayBaseUrl}/api/auth/me`, { withCredentials: true });
  }

  logout(logoutUrl: string): Observable<any> {
    return this.http.get(`${logoutUrl}`, { withCredentials: true });
  }
}
