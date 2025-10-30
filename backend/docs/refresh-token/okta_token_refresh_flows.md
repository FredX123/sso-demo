# Okta Token Refresh Flows - Mermaid Diagrams

This document describes how the session-aware Angular frontends interact with the Spring Cloud Gateway (BFF) and Okta. All flows reflect the current `common-lib` implementation (refresh endpoint, silent header, retry logic).

---

## 1) First Login (OIDC Authorization Code with PKCE)
```mermaid
sequenceDiagram
  participant Browser
  participant Gateway as Gateway (BFF)
  participant Okta as Okta (IdP)

  Browser->>Gateway: GET /
  Gateway-->>Browser: 302 redirect to Okta /authorize
  Browser->>Okta: /authorize (PKCE)
  Okta-->>Browser: 302 back to Gateway with code
  Browser->>Gateway: /login/oauth2/code/my-bff?code=...
  Gateway->>Okta: POST /v1/token (exchange code)
  Okta-->>Gateway: access token, refresh token, id token
  Note over Gateway: Persist tokens inside the authorized client session
  Gateway-->>Browser: Set session cookie + 200 app shell
```

---

## 2) Normal API Call (access token still valid)
```mermaid
sequenceDiagram
  participant Browser
  participant Gateway as Gateway (BFF)
  participant API as Downstream API

  Browser->>Gateway: GET /api/data (session cookie)
  Note over Gateway: Resolve authorized client from session
  Gateway->>API: GET /api/data with Authorization: Bearer <access token>
  API-->>Gateway: 200 JSON
  Gateway-->>Browser: 200 JSON
```

---

## 3) Automatic Refresh on Gateway (access token expired)
```mermaid
sequenceDiagram
  participant Browser
  participant Gateway as Gateway (BFF)
  participant Okta as Okta (IdP)
  participant API as Downstream API

  Browser->>Gateway: GET /api/data (session cookie)
  Note over Gateway: Access token expired, use refresh token
  Gateway->>Okta: POST /v1/token grant_type=refresh_token
  Okta-->>Gateway: new access token (+ optional refresh token)
  Gateway->>API: GET /api/data with new access token
  API-->>Gateway: 200 JSON
  Gateway-->>Browser: 200 JSON
```

---

## 4) Angular Keep-Alive (activity or session timer)
```mermaid
sequenceDiagram
  participant Angular
  participant Gateway as Gateway (BFF)
  participant Okta as Okta (IdP)
  participant AuthMS as Auth-MS (PDP)

  Angular->>Gateway: GET /api/token/refresh
  alt Refresh succeeds
    Gateway->>Okta: POST /v1/token grant_type=refresh_token
    Okta-->>Gateway: new access token (+ optional refresh token)
    Gateway-->>Angular: 200 OK
    Angular->>Gateway: GET /api/auth/authorizations/touch
    Note over Angular,Gateway: Header X-Silent-Auth=true prevents nested refresh attempts
    Gateway->>AuthMS: Forward touch request with TokenRelay
    AuthMS-->>Gateway: 200 AuthMe
    Gateway-->>Angular: 200 AuthMe
  else Refresh fails
    Gateway-->>Angular: 401
    Angular->>Angular: Trigger expiry dialog or logout
  end
```

---

## 5) Http Interceptor (401 -> refresh -> touch -> retry)
```mermaid
sequenceDiagram
  participant Angular
  participant Gateway as Gateway (BFF)
  participant API as Downstream API
  participant AuthMS as Auth-MS (PDP)
  participant Dialog as DialogService

  Angular->>Gateway: GET /api/data (withCredentials)
  Gateway-->>Angular: 401 Unauthorized
  Angular->>Gateway: GET /api/token/refresh
  alt Refresh succeeds
    Gateway-->>Angular: 200 OK
    Angular->>Gateway: GET /api/auth/authorizations/touch
    Note over Angular,Gateway: X-Silent-Auth=true header suppresses interceptor retries
    Gateway->>AuthMS: Forward touch request with TokenRelay
    AuthMS-->>Gateway: 200 AuthMe
    Gateway-->>Angular: 200 AuthMe
    Angular->>Gateway: RETRY GET /api/data
    Gateway->>API: GET /api/data with refreshed access token
    API-->>Gateway: 200 JSON
    Gateway-->>Angular: 200 JSON
  else Refresh fails
    Gateway-->>Angular: 401 Unauthorized
    Angular->>Dialog: open("Authentication Required")
  end
```

---

## 6) Refresh Token Rotation vs Persistent
```mermaid
flowchart LR
  A[Okta /v1/token using refresh token] --> B{Refresh behavior}
  B -- Persistent --> C[Return new access token only]
  B -- Rotate --> D[Return new access token and refresh token]
  C --> E[Gateway updates access token in session]
  D --> F[Gateway updates access and refresh token in session]
```

---

## 7) Alternative Model - HttpOnly cookies for access/refresh tokens
```mermaid
sequenceDiagram
  participant Browser
  participant Gateway as Gateway (BFF)
  participant Okta as Okta (IdP)

  Browser->>Gateway: GET /api/data (cookies auto-attached)
  Note over Gateway: Access token expired, rely on refresh token cookie
  Gateway->>Okta: POST /v1/token using refresh token
  Okta-->>Gateway: new access token (+ optional refresh token)
  Gateway-->>Browser: Set-Cookie access_token HttpOnly
  alt Rotation enabled
    Gateway-->>Browser: Set-Cookie refresh_token HttpOnly (replace)
  else No rotation
    Gateway-->>Browser: Keep existing refresh_token cookie
  end
  Note over Browser: HttpOnly cookies cannot be read from JavaScript
```
