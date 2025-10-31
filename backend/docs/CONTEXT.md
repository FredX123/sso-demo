
- OKTA: CURRENTLY using **two** Web Apps (frontoffice + backoffice)
- NEXT STEP: consolidate to **one** OKTA Web App shared by frontoffice and backoffice
- GATEWAY: still responsible for resolving appKey from route (/api/frontoffice/**, /api/backoffice/**) and NOT from OKTA client id

# 🧩 SSO Demo – Context Summary

This repository demonstrates a **modular SSO architecture** built with **Spring Boot 3.5.x** and **Angular 19**, using **Okta** for authentication and **Spring Cloud Gateway + Redis** for authorization caching.

---

## 🏗️ Core Microservices (Shared Layer)

| Service | Role | Notes |
|----------|------|-------|
| **api-gateway** | Acts as **PEP (Policy Enforcement Point)** using `DecideGatewayFilterFactory` | Validates JWT, enforces authorization via cached UserSessions |
| **auth-ms** | Serves as **PDP (Policy Decision Point)** | Builds authorization decisions using per-app plugin adapters |
| **permission-ms** | Provides app-specific permission rules | Queried by adapters during decision build |
| **session-ms** | Central **Redis cache** for `UserSession` (AuthMe + AuthorizationBundle) | Keyed by `iss:sub:app:version` |
| **mock-external-ms** | Mock identity/role provider | Returns user roles by subject; unsecured for demo |

---

## 🧱 Plugin Architecture

- **auth-ms** dynamically loads **AppAuthAdapter** implementations discovered via Spring scan.
- Each plugin (e.g. `frontoffice-plugin`, `backoffice-plugin`) implements:
    - `AppAuthAdapter`
    - its own `MockExternalSvcClient`
    - optionally custom `PermissionSvcClient` logic
- Plugins are discovered by annotation `@AppKey("<app>")` and registered in `AppAdapterRegistry`.

### Example
frontoffice-plugin/  
├─ FrontofficeAdapter.java (@AppKey("frontoffice"))  
└─ FrontofficeMockExternalSvcClient.java


---

## 🔑 Redis Caching Model

| Key Pattern | Purpose |
|--------------|----------|
| `roles:{iss}:{sub}:{appKey}:{ver}` | Cached `UserSession` (AuthMe + AuthorizationBundle) |
| `policyVer:{appKey}` | Incremented on policy updates in `permission-ms` |
| `userVer:{iss}:{sub}` | Incremented on role updates in `mock-external-ms` |

`ver = policyVer + '.' + userVer`

- Old cache keys expire naturally.
- TTL ≈ 15 min–1 h (configurable in `auth-ms.yml`).

---

## 🔄 Request Flow Summary

1. **UI → Gateway**  
   `/api/<app>/…` request with valid Bearer token.
2. **Gateway (PEP)**
    - Validates JWT
    - Resolves `appKey` from route
    - Calls `session-ms` for `UserSession`
3. **Cache hit →** apply decision  
   **Cache miss →** UI triggers `/api/auth/authorizations/load`
4. **auth-ms (PDP)**
    - Selects plugin adapter via `AppAdapterRegistry`
    - Fetches rules from `permission-ms`
    - Fetches user roles via plugin’s `MockExternalSvcClient`
    - Builds & caches `UserSession`
5. **Gateway (Decide Filter)**
    - Evaluates decision
    - Adds `X-App` header
    - Forwards request to target service

---

## 🧩 Example Key Services and Ports

| Service | Port | Description |
|----------|------|-------------|
| api-gateway | 6000 | entry point, TokenRelay, Decide filter |
| auth-ms | 6002 | PDP logic and plugin loading |
| session-ms | 6005 | Redis cache API |
| permission-ms | 6004 | permission repository |
| mock-external-ms | 6006 | mock identity provider |
| frontoffice-ms / backoffice-ms | 6010+ | business microservices |

---

## ✅ Design Principles

- **Shared modules** never depend on domain/business code.
- **Plugins** depend only on `common-lib` and define their own domain logic.
- **Auth decisions** are centralized; **policies & roles** remain per-domain.
- **Extensible:** new apps can be onboarded by adding a plugin JAR with minimal coupling.

---

_This `CONTEXT.md` serves as a bootstrap reference for Codex or ChatGPT sessions to quickly rehydrate the project’s architecture without loading full conversation history._
