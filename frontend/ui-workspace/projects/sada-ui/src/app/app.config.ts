import { ApplicationConfig } from '@angular/core';
import { provideRouter } from '@angular/router';
import { routes } from './app.routes';
import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { API_AUTH_BASE_URL, authInterceptor, GATEWAY_BASE_URL, APP_URL } from 'common-lib';
import { environment } from '../environments/environment';

export const appConfig: ApplicationConfig = {
  providers: [
    provideRouter(routes),
    { provide: GATEWAY_BASE_URL, useValue: environment.gatewayBaseUrl },
    { provide: API_AUTH_BASE_URL,  useValue: environment.apiAuthUrl },
        { provide: APP_URL, useValue: environment.appUrl },
    provideHttpClient(withInterceptors([authInterceptor])),
  ],
};
