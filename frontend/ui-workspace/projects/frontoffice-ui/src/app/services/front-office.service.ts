import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { environment } from '../../environments/environment';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class FrontOfficeService {

  env = environment;
  me: any;
  data: any;

  constructor(private http: HttpClient) { }

  callFo(): Observable<any> {
    return this.http.get(`${this.env.apiBaseUrl}/whoami`, { withCredentials: true });
  }

  callBoViaBackend(): Observable<any> {
    return this.http.get(`${this.env.apiBaseUrl}/whoami-from-bo`, { withCredentials: true });
  }

  callBoViaBackendNoToken(): Observable<any> {
    return this.http.get(`${this.env.apiBaseUrl}/whoami-from-bo-no-token`, { withCredentials: true });
  }
}
