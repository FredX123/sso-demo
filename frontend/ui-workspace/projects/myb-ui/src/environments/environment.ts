export const baseUrl = 'http://localhost:6001';

export const environment = {
  production: false,
  gatewayBaseUrl:  `${baseUrl}`,
  apiAuthBaseUrl: `${baseUrl}/api/auth`, // identity probe lives in auth-ms
  apiBaseUrl: `${baseUrl}/myb`, // routes to MYB backend via gateway
  loginUrl: `${baseUrl}/oauth2/authorization/myb-app?redirectTo=/dashboard`,
  crossAppUrl: 'http://localhost:4201',     // SADA UI
};
