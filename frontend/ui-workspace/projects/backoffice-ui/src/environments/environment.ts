export const baseUrl = 'http://localhost:6001';

export const environment = {
  production: false,
  gatewayBaseUrl:  `${baseUrl}`,
  apiBaseUrl: `${baseUrl}/api/backoffice`, // routes to Back Office backend via gateway
  apiAuthUrl: `${baseUrl}/api/backoffice/auth`, // identity probe per app lives in auth-ms
  loginUrl: `${baseUrl}/oauth2/authorization/backoffice-app?redirectTo=/my-applications`,
  appUrl: 'http://localhost:4201',
  crossAppUrl: 'http://localhost:4200',     // Front Office UI
};
