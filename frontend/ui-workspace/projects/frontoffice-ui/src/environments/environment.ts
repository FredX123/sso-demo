export const baseUrl = 'http://localhost:6001';

export const environment = {
  production: false,
  gatewayBaseUrl:  `${baseUrl}`,
  apiBaseUrl: `${baseUrl}/api/frontoffice`,      // routes to Front Office backend via gateway
  apiAuthUrl: `${baseUrl}/api/frontoffice/auth`, // identity probe per app lives in auth-ms
  loginUrl: `${baseUrl}/oauth2/authorization/frontoffice-app?redirectTo=/dashboard`,
  appUrl: 'http://localhost:4200',
  crossAppUrl: 'http://localhost:4201',     // Back Office UI
};
