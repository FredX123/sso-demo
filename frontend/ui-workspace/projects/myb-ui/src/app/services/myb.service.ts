import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { environment } from '../../environments/environment';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class MyBService {

  env = environment;
  me: any;
  data: any;

  constructor(private http: HttpClient) { }

  callMyb(): Observable<any> {
    return this.http.get(`${this.env.apiBaseUrl}/whoami`, { withCredentials: true });
  }

  callSadaViaBackend(): Observable<any> {
    return this.http.get(`${this.env.apiBaseUrl}/whoami-from-sada`, { withCredentials: true });
  }

  callSadaViaBackendNoToken(): Observable<any> {
    return this.http.get(`${this.env.apiBaseUrl}/whoami-from-sada-no-token`, { withCredentials: true });
  }
}
