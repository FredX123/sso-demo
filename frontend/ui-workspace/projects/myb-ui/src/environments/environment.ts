export const baseUrl = 'http://localhost:6001';

export const environment = {
  production: false,
  gatewayBaseUrl:  `${baseUrl}`,
  apiAuthUrl: `${baseUrl}/api/auth`, // identity probe lives in auth-ms
  apiBaseUrl: `${baseUrl}/api/myb`, // routes to MYB backend via gateway
  loginUrl: `${baseUrl}/oauth2/authorization/myb-app?redirectTo=/dashboard`,
  appUrl: 'http://localhost:4200',
  crossAppUrl: 'http://localhost:4201',     // SADA UI
};
