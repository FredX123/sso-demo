1) Activity diagram — Gateway (PEP) ↔ auth-ms (PDP) decision flow

```mermaid
flowchart TD
    A["Browser request to /api/<app>/..."] --> B["API Gateway (PEP)"]
    B --> C["Validate access token (JWT)"]
    C -- "invalid" --> X["401 Unauthorized or redirect"]
    C -- "valid" --> D["Extract iss, sub"]
    D --> E["Determine appKey from route (e.g., frontoffice / backoffice)"]
    E --> F{"UserSession present in cache?"}
    F -- "yes" --> G["Use cached decisions (allow/deny)"]
    F -- "no" --> H["UI calls auth-ms /api/auth/authorizations/load (once per app)"]
    H --> I["auth-ms resolves app via cid/azp; selects app adapter"]
    I --> J["Fetch rules from permission-ms + roles from mock-external-ms"]
    J --> K["Build AuthorizationBundle (roles + decisions)"]
    K --> L["Cache UserSession (AuthMe + AuthorizationBundle) in Redis via session-ms"]
    G --> M{"Allow?"}
    L --> M
    M -- "no" --> N["403 Forbidden"]
    M -- "yes" --> O["(Optional) Add header X-App"]
    O --> P["Forward to target service"]
    P --> Q["Service handles business logic"]
    Q --> R["Response to browser"]
```

2) Sequence diagram — per-request authorization with caching

```mermaid
sequenceDiagram
    autonumber
    participant Br as Browser / UI
    participant GW as API Gateway (PEP)
    participant SM as SessionClient (in Gateway)
    participant SESS as session-ms (Redis cache)
    participant APP as backoffice-ms/frontoffice-ms

    Note over Br,GW: User already authenticated&#59 Gateway validates JWT.

Br->>GW: GET /api/backoffice/hello (Bearer AT)
GW->>GW: Validate JWT (iss/sub)
GW->>GW: appKey = from route ("backoffice")
GW->>SM: getUserSession()
SM->>SESS: GET dec:{iss}:{sub}:{app}:{version}  (stored as UserSession)
alt Cache hit
SESS-->>SM: UserSession (AuthMe + AuthorizationBundle)
else Cache miss
Note over Br,GW: UI should have called /api/auth/authorizations/load earlier
SESS-->>SM: 404 / empty
end
SM-->>GW: UserSession or empty

alt decisions allow == false OR cache missing
GW-->>Br: 403 Forbidden
else allow == true
GW->>GW: (optional) add X-App: backoffice
GW->>APP: Forward request
APP-->>GW: 200 OK
GW-->>Br: 200 OK
end
```

3) Sequence diagram — login/bootstrap & roles caching (iss + sub + app + version)

```mermaid
sequenceDiagram
    autonumber
    participant Br as Browser / UI
    participant GW as API Gateway
    participant AUTH as auth-ms (PDP)
    participant PERM as permission-ms
    participant EXT as mock-external-ms
    participant SESS as session-ms (Redis)

    Note over Br,AUTH: After login and landing on an app, UI initializes authorization context.

    Br->>GW: GET /api/auth/authorizations/load
    GW->>AUTH: Forward with Bearer AT (TokenRelay)
    AUTH->>AUTH: Extract iss, sub, cid/azp → appKey
    AUTH->>PERM: GET /api/permission/{appKey}
    AUTH->>EXT: GET /api/external-mock/user-info/{sub}
    AUTH->>AUTH: Build AuthorizationBundle (roles, decisions, version, ttl)
    AUTH->>AUTH: Build AuthMe from JWT (+ roles)
    AUTH->>SESS: POST UserSession{ AuthMe, AuthorizationBundle } with key dec:{iss}:{sub}:{app}:{version}
    AUTH-->>GW: 200 OK (AuthMe)
    GW-->>Br: 200 OK (AuthMe)
```
