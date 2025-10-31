## Authorization Flow — Modular Plugin Architecture

auth-ms functions purely as a PDP (Policy Decision Point) and dynamically loads pluggable AppAuthAdapters 
from domain-specific JARs (e.g. frontoffice-plugin, backoffice-plugin).
Each adapter defines how to fetch user roles (via MockExternalSvcClient) and how to interpret permissions for its own domain.
This preserves a clean separation between shared security infrastructure and domain-specific logic.

### 1) Activity Diagram — Gateway (PEP) ↔ auth-ms (PDP) with Plugin Adapters

```mermaid
flowchart TD
    A["Browser request to /api/<app>/..."] --> B["API Gateway (PEP)"]
    B --> C["Validate access token (JWT)"]
    C -- "invalid" --> X["401 Unauthorized or redirect"]
    C -- "valid" --> D["Extract iss, sub"]
    D --> E["Determine appKey from route (frontoffice / backoffice)"]
    E --> F{"UserSession present in Redis cache (via session-ms)?"}
    F -- "yes" --> G["Use cached decisions (allow/deny)"]
    F -- "no" --> H["UI triggers auth-ms /api/auth/authorizations/load (once per app)"]
    H --> I["auth-ms resolves appKey via cid/azp and selects corresponding plugin adapter"]
    I --> J["Adapter fetches rules from permission-ms and user roles from mock-external-ms"]
    J --> K["Adapter builds AuthorizationBundle (roles + decisions + ttl)"]
    K --> L["auth-ms builds AuthMe and caches UserSession (AuthMe + AuthorizationBundle) in Redis via session-ms"]
    G --> M{"Allow?"}
    L --> M
    M -- "no" --> N["403 Forbidden"]
    M -- "yes" --> O["(Optional) Add header X-App"]
    O --> P["Forward to target microservice (e.g., backoffice-ms / frontoffice-ms)"]
    P --> Q["Service executes business logic"]
    Q --> R["Response returned to browser"]
```

### 2) Sequence Diagram — Per-Request Authorization with Cached Decisions

```mermaid
sequenceDiagram
    autonumber
    participant Br as Browser / UI
    participant GW as API Gateway (PEP)
    participant SM as SessionClient (in Gateway)
    participant SESS as session-ms (Redis Cache)
    participant APP as Domain Service (frontoffice-ms / backoffice-ms)

    Note over Br,GW: User already authenticated&#59 Gateway validates JWT and routes based on /api/<app>.

Br->>GW: GET /api/backoffice/orders (Bearer AT)
GW->>GW: Validate JWT (iss, sub)
GW->>GW: Resolve appKey = "backoffice"
GW->>SM: getUserSession(bearer, appKey)
SM->>SESS: GET roles:{iss}:{sub}:{appKey}:{version}
alt Cache hit
SESS-->>SM: UserSession (AuthMe + AuthorizationBundle)
else Cache miss
Note over GW,AUTH: UI should have triggered /api/auth/authorizations/load for this app
SESS-->>SM: empty
end
SM-->>GW: UserSession or empty

alt Decision.allow == false OR no UserSession
GW-->>Br: 403 Forbidden
else Decision.allow == true
GW->>GW: Add header X-App: backoffice
GW->>APP: Forward authorized request
APP-->>GW: 200 OK (payload)
GW-->>Br: 200 OK (payload)
end
```

### 3) Sequence Diagram — Login / Bootstrap & Roles Caching (iss + sub + app + version)

```mermaid
sequenceDiagram
    autonumber
    participant Br as Browser / UI
    participant GW as API Gateway
    participant AUTH as auth-ms (PDP)
    participant PLG as AppAuthAdapter Plugin (e.g., FrontofficeAdapter / BackofficeAdapter)
    participant PERM as permission-ms
    participant EXT as mock-external-ms
    participant SESS as session-ms (Redis Cache)

    Note over Br,AUTH: After login and landing on an app, UI initializes authorization context.

    Br->>GW: GET /api/auth/authorizations/load
    GW->>AUTH: Forward with Bearer AT (TokenRelay)
    AUTH->>AUTH: Extract iss, sub, cid/azp → resolve appKey
    AUTH->>PLG: Select registered AppAuthAdapter (via AppAdapterRegistry)
    PLG->>PERM: GET /api/permission/{appKey}
    PLG->>EXT: GET /api/mock-external/user-roles/{sub}
    PLG->>PLG: Build AuthorizationBundle (roles + decisions + ttl + version)
    AUTH->>AUTH: Build AuthMe from JWT + roles
    AUTH->>SESS: POST UserSession{AuthMe, AuthorizationBundle} with key roles:{iss}:{sub}:{appKey}:{version}
    AUTH-->>GW: 200 OK (AuthMe)
    GW-->>Br: 200 OK (AuthMe)
```

####  Design Highlights

| Layer                      | Responsibility                                                    | Notes                                                                     |
| -------------------------- | ----------------------------------------------------------------- | ------------------------------------------------------------------------- |
| **API Gateway (PEP)**      | Enforces access via the `Decide` filter using cached UserSessions | Uses `SessionClient` to talk to `session-ms`                              |
| **auth-ms (PDP)**          | Centralized decision point; loads per-app adapters dynamically    | No dependency on domain/business microservices                            |
| **AppAuthAdapter Plugins** | Domain-aware adapters providing custom decision logic             | Each implements `AppAuthAdapter` and is registered via `@AppKey("<app>")` |
| **permission-ms**          | Stores app-level permission rules                                 | Independent of auth-ms                                                    |
| **mock-external-ms**       | Provides user role data per subject (mock external identity)      | Not secured; pluggable for future identity sources                        |
| **session-ms**             | Caches `UserSession` (AuthMe + AuthorizationBundle)               | Keyed by `iss:sub:app:version`                                            |

#### Benefits of the Plugin-Based PDP Design
 * **Decoupled architecture**: auth-ms is generic and agnostic of any business domain.
 * **Pluggable onboarding**: new app teams (e.g., trainingoffice, analytics) just build their own adapter JAR implementing AppAuthAdapter.
 * **Centralized decision caching**: unified Redis structure for all apps.
 * **No circular dependencies**: shared modules never depend on business modules.
 * **Independent deployment**: new plugins can be deployed by updating the auth-ms container image with the new adapter JAR.

### 4) Redis Key Schema & Versioning Flow (Plugin-Based PDP) - Not Fully Implemented Yet

#### Cache Key Composition — per app, user, and version

```mermaid
flowchart LR
    subgraph Inputs
        A["iss – Issuer URL"]:::input
        B["sub – Subject (User ID)"]:::input
        C["appKey – from AppAuthAdapter<br/>(e.g., frontoffice, backoffice)"]:::input
        D["version – composite of policyVer + userVer"]:::input
    end

    A --> E
    B --> E
    C --> E
    D --> E

    E["Build Redis key"]:::process --> F["roles:{iss}:{sub}:{appKey}:{version}"]:::key

    classDef input fill:#eef,stroke:#66f,stroke-width:1px;
    classDef process fill:#efe,stroke:#393,stroke-width:1px;
    classDef key fill:#ffd,stroke:#cc0,stroke-width:1px;
```
 * **Purpose**: uniquely identifies a cached UserSession (AuthMe + AuthorizationBundle).
 * **Namespace prefix**: roles: or dec: depending on context (role set vs. fine-grained decision).
 * **Version**: avoids collisions when either user roles or policy rules change.

#### Version Composition

```mermaid
flowchart TB
    subgraph "Version Components"
        V1["policyVer:{appKey}<br/>→ incremented when permission-ms policy changes"]
        V2["userVer:{iss}:{sub}<br/>→ incremented when user roles change in mock-external-ms"]
    end
    V1 --> J["ver = policyVer + '.' + userVer"]
    V2 --> J
    J --> K["Used as {version} in Redis key"]
```
Both policyVer and userVer are small integers stored separately in Redis or a DB; combining them ensures 
that any change in either policy or user state invalidates cached sessions automatically.

#### Eviction / Invalidation Flow

```mermaid
flowchart TD
    subgraph CacheLookup
        A["Gateway receives request"] --> B{"roles:{iss}:{sub}:{app}:{ver} in Redis?"}
        B -- "yes" --> C["Use cached UserSession"]
        B -- "no" --> D["Call auth-ms /authorizations/load"]
        D --> E["Adapter builds AuthMe + AuthorizationBundle"]
        E --> F["Cache roles:{iss}:{sub}:{app}:{ver} with TTL"]
        C --> G["Forward to target microservice"]
        F --> G
    end

    subgraph RoleChange["User Role Change (External)"]
        RC1["mock-external-ms updates user roles"]
        RC2["Increment userVer:{iss}:{sub}"]
        RC3["Old cache keys (with previous ver) expire naturally"]
    end
    RC1 --> RC2 --> RC3

    subgraph PolicyDeploy["Policy Change (Per App)"]
        PC1["permission-ms deploys new rules"]
        PC2["Increment policyVer:{app}"]
        PC3["Next auth-ms load creates fresh ver"]
    end
    PC1 --> PC2 --> PC3

    subgraph ManualEvict["Explicit Eviction (Optional)"]
        EV1["Admin triggers cache purge"]
        EV2["DEL roles:*:{app}:* or FLUSHDB (non-prod)"]
    end
    EV1 --> EV2
```

#### Design Highlights

| Aspect                  | Description                                                                         |
| ----------------------- | ----------------------------------------------------------------------------------- |
| **Scope**               | Cached per `iss + sub + appKey + version`                                           |
| **Storage**             | Central Redis (`session-ms`)                                                        |
| **Update triggers**     | Role changes (mock-external-ms) or policy updates (permission-ms)                   |
| **TTL**                 | Typically 15 min – 1 h (depending on `auth-ms.yml`)                                 |
| **Eviction safety**     | Old keys naturally expire; new `ver` ensures soft invalidation without flush        |
| **Plugin independence** | Each AppAuthAdapter manages its own policyVer namespace → no cross-app interference |

#### Result:
This key-version scheme guarantees that all cached authorization decisions are both per-application and time-bounded, 
while still allowing independent evolution of user data and policy rules—no shared-module ↔ domain-module coupling.
