import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { environment } from '../../environments/environment';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class BackOfficeService {

  env = environment;
  me: any;
  data: any;

  constructor(private http: HttpClient) { }

  callBo(): Observable<any> {
    return this.http.get(`${this.env.apiBaseUrl}/whoami`, { withCredentials: true });
  }

  callFoViaBackend(): Observable<any> {
    return this.http.get(`${this.env.apiBaseUrl}/whoami-from-fo`, { withCredentials: true });
  }

  callFoViaBackendNoToken(): Observable<any> {
    return this.http.get(`${this.env.apiBaseUrl}/whoami-from-fo-no-token`, { withCredentials: true });
  }
}
