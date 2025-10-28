flowchart TD
    A[Browser request to /api/{app}/...] --> B[API Gateway (PEP)]
    B --> C{Access token valid?}
    C -- No --> C1[Return 401/redirect to login] --> Z[End]
    C -- Yes --> D[Extract iss, sub, clientId (cid/azp)]
    D --> E[Resolve appKey (e.g., myb/sada)]
    E --> F{Decision in Redis cache?}
    F -- Yes --> G[Use cached allow/deny + obligations]
    F -- No --> H[Build DecisionRequest(iss, sub, app, method, path)]
    H --> I[auth-ms (PDP) /api/decision]
    I --> J[Select AppAuthAdapter by appKey]
    J --> K[Build AppAuthContext (roles/permissions) -- mock/DB/Redis]
    K --> L[Evaluate policy (RBAC/ABAC/OPA)]
    L --> M[DecisionResponse(allow, obligations, ttl)]
    M --> N[Gateway caches decision (ttl)]
    G --> O{allow?}
    N --> O
    O -- No --> O1[Return 403 Forbidden] --> Z
    O -- Yes --> P[Add normalized headers: X-App, X-Roles, X-Permissions]
    P --> Q[Forward to target service (myb-ms / sada-ms)]
    Q --> R[Service handles business logic]
    R --> S[Response back to Browser]
    S --> Z[End]
