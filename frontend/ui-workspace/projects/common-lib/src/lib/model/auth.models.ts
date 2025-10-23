
export interface UserAuthorization {
  roles: string[];
  permissions: string[];
}

export interface AuthMe {
  authenticated: boolean;
  subject?: string;
  firstName?: string;
  lastName?: string;
  issuer?: string;
  issuedAt?: string;
  expiresAt?: string;
  email?: string;
  authz?: UserAuthorization;
}
