export const baseUrl = 'http://localhost:6001';

export const environment = {
  production: false,
  gatewayBaseUrl:  `${baseUrl}`,
  apiAuthUrl: `${baseUrl}/api/auth`, // identity probe lives in auth-ms
  apiBaseUrl: `${baseUrl}/api/sada`, // routes to SADA backend via gateway
  loginUrl: `${baseUrl}/oauth2/authorization/sada-app?redirectTo=/my-applications`,
  appUrl: 'http://localhost:4201',
  crossAppUrl: 'http://localhost:4200',     // MyB UI
};
