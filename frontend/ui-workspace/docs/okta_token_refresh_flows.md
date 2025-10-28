# Okta Token Refresh Flows – Mermaid Diagrams (Corrected)

This document contains all sequence and flow diagrams in Mermaid format. Diagrams **#3** and **#7** have been adjusted to avoid Mermaid parser issues (removed special characters like commas/brackets inside message lines, avoided Unicode arrows, and simplified note text).

---

## 1) First Login (OIDC Authorization Code w/ PKCE)
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
  Note over Gateway: Store tokens in server session (authorized client)
  Gateway-->>Browser: Set session cookie (HttpOnly Secure SameSite) + 200 app shell
```

---

## 2) Normal API Call (AT still valid)
```mermaid
sequenceDiagram
  participant Browser
  participant Gateway as Gateway (BFF)
  participant API as Downstream API

  Browser->>Gateway: GET /api/data (session cookie)
  Note over Gateway: Resolve authorized client from session
  Gateway->>API: GET /api/data with Authorization Bearer access token
  API-->>Gateway: 200 JSON
  Gateway-->>Browser: 200 JSON
```

---

## 3) Auto-Refresh on Backend (AT expired -> refresh with RT)  **(Corrected)**
```mermaid
sequenceDiagram
  participant Browser
  participant Gateway as Gateway (BFF)
  participant Okta as Okta (IdP)
  participant API as Downstream API

  Browser->>Gateway: GET /api/data (session cookie)
  Note over Gateway: Access token expired. Try refresh using refresh token.
  Gateway->>Okta: POST /v1/token grant_type=refresh_token
  Okta-->>Gateway: new access token and maybe new refresh token
  Note over Gateway: Save new access token. If rotation then replace refresh token in session.
  Gateway->>API: GET /api/data with Authorization Bearer new access token
  API-->>Gateway: 200 JSON
  Gateway-->>Browser: 200 JSON
```

---

## 4) Optional Manual Refresh Endpoint (Keep-alive)
```mermaid
sequenceDiagram
  participant Angular
  participant Gateway as Gateway (BFF)
  participant Okta as Okta (IdP)

  Angular->>Gateway: POST /api/session/refresh
  alt Access token still valid
    Gateway-->>Angular: 200 status ok
  else Access token expired
    Gateway->>Okta: POST /v1/token with refresh token
    Okta-->>Gateway: new access token (and new refresh token if rotation)
    Gateway-->>Angular: 200 status ok
  end
```

---

## 5) Frontend Error Interceptor (401 -> refresh -> retry)
```mermaid
sequenceDiagram
  participant Angular
  participant Gateway as Gateway (BFF)

  Angular->>Gateway: GET /api/data
  Gateway-->>Angular: 401 session expired
  Angular->>Gateway: POST /api/session/refresh
  alt Refresh succeeds
    Gateway-->>Angular: 200 ok
    Angular->>Gateway: RETRY GET /api/data
    Gateway-->>Angular: 200 JSON
  else Refresh fails
    Gateway-->>Angular: 401
    Angular->>Gateway: Redirect to /login (start OIDC)
  end
```

---

## 6) Rotation vs Persistent (Refresh behavior)
```mermaid
flowchart LR
  A[Okta /v1/token using refresh token] --> B{Refresh behavior}
  B -- Persistent --> C[Return new access token only]
  B -- Rotate --> D[Return new access token and new refresh token]
  C --> E[Gateway updates access token in session]
  D --> F[Gateway updates access token and replaces refresh token in session]
```

---

## 7) Alternative Model — HttpOnly Cookies for AT/RT  **(Corrected)**
```mermaid
sequenceDiagram
  participant Browser
  participant Gateway as Gateway (BFF)
  participant Okta

  Browser->>Gateway: GET /api/data (cookies auto-attached)
  Note over Gateway: Access token expired. Use refresh token cookie to refresh.
  Gateway->>Okta: POST /v1/token using refresh token
  Okta-->>Gateway: new access token and maybe new refresh token
  Gateway-->>Browser: Set-Cookie access_token HttpOnly
  alt Rotation enabled
    Gateway-->>Browser: Set-Cookie refresh_token HttpOnly (replaced)
  else No rotation
    Gateway-->>Browser: Keep existing refresh_token cookie
  end
  Note over Browser: Cookies updated automatically. JavaScript cannot read HttpOnly cookies.
```
