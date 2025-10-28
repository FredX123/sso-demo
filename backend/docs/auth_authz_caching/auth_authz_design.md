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

4) Decision cache key composition (per-app)

```mermaid
flowchart LR
    subgraph Inputs
        A["iss\nissuer URL"]
        B["sub\nuser id (subject)"]
        C["app\nappKey (frontoffice/backoffice)"]
        D["action\nHTTP method"]
        E["routeId\ngateway route id"]
        F["path\nfull path"]
        V["version\npermission config version"]
    end

    F --> H["pathHash = hex(hash(path))"]
    A --> I
    B --> I
    C --> I
    D --> I
    E --> I
    H --> I
    V --> I

    I["Build key"] --> J["dec:<iss>:<sub>:<app>:<action>:<routeId>:<pathHash>:v<version>"]
```

5) Eviction & versioning flows (roles-only)

```mermaid
flowchart TD
    A["Gateway request"] --> B{"UserSession in cache?"}
    B -- "yes" --> C["Use stored decisions"]
    C --> Z["Forward to service"]
    B -- "no" --> D["UI must call /api/auth/authorizations/load"]
    D --> E["auth-ms builds AuthorizationBundle (roles, decisions)"]
    E --> F["Store UserSession under dec:{iss}:{sub}:{app}:v{version} (TTL)"]
    F --> Z

    subgraph "Role change (user)"
        R1["Roles updated in external system"]
        R2["Increment userVer or bump app 'version' in permission-ms"]
        R3["Next load stores under new :v{version}; old becomes cold"]
    end
    R1 --> R2 --> R3

    subgraph "Policy change (app)"
        P1["Permission rules updated"]
        P2["permission-ms returns higher 'version'"]
        P3["All future loads use new :v{version}"]
    end
    P1 --> P2 --> P3

    subgraph "Explicit purge (optional)"
        E1["Admin purge"]
        E2["DEL dec:*:v* for scope or FLUSHDB in non-prod"]
    end
    E1 --> E2
```


