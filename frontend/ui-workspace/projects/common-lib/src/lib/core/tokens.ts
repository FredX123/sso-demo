import { InjectionToken } from "@angular/core";

export const GATEWAY_BASE_URL = new InjectionToken<string>('GATEWAY_BASE_URL');   // already used by your interceptor
export const API_AUTH_BASE_URL = new InjectionToken<string>('API_AUTH_BASE_URL'); // new: e.g., http://localhost:6001/api/auth
export const APP_URL         = new InjectionToken<string>('APP_URL');
