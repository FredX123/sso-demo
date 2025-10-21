import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { environment } from '../../environments/environment';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class SadaService {

  env = environment;
  me: any;
  data: any;

  constructor(private http: HttpClient) { }

  callSada(): Observable<any> {
    return this.http.get(`${this.env.apiBaseUrl}/whoami`, { withCredentials: true });
  }

  callMybViaBackend(): Observable<any> {
    return this.http.get(`${this.env.apiBaseUrl}/whoami-from-myb`, { withCredentials: true });
  }

  callMybViaBackendNoToken(): Observable<any> {
    return this.http.get(`${this.env.apiBaseUrl}/whoami-from-myb-no-token`, { withCredentials: true });
  }
}
