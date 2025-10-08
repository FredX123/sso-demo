export interface AuthMe {
  authenticated: boolean;
  name?: string;
  subject?: string;
  email?: string;
  issuer?: string;
  issuedAt?: string;
  expiresAt?: string;
  roles?: string[];
  claims?: Record<string, any>;
}
