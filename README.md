# 🔐 SSO Demo Backend

This project demonstrates a **modular, plugin-based Single Sign-On (SSO) architecture** using **Spring Boot 3.5.x**, **Spring Cloud Gateway**, **Spring Security (OAuth2)**, and **Redis caching**.

It showcases a **shared security foundation** (PEP/PDP/Cache pattern) with extensible **domain adapters** that allow new business applications to integrate authorization logic dynamically without changing core services.

---

## 🏗️ Project Structure

```
backend/
│  pom.xml
│
├─ config-repo/              # Centralized configuration (native/Git backend for Config Server)
│
├─ config-server/            # Spring Cloud Config Server
├─ discovery-server/         # Eureka service registry
├─ api-gateway/              # Gateway (PEP) + Decide filter
├─ auth-ms/                  # Authorization service (PDP) + plugin adapter registry
├─ permission-ms/            # Policy / permission repository
├─ session-ms/               # UserSession + decision cache (Redis)
├─ mock-external-ms/         # Mock identity & role provider
│
├─ frontoffice-plugin/       # AppAuthAdapter for frontoffice domain
├─ backoffice-plugin/        # AppAuthAdapter for backoffice domain
│
├─ frontoffice-ms/           # Example business microservice
└─ backoffice-ms/            # Example business microservice
```

---

## ⚙️ Architecture Overview

| Component | Role | Description |
|------------|------|-------------|
| **API Gateway** | **PEP** | Validates JWT, enforces decisions via `DecideGatewayFilterFactory` |
| **auth-ms** | **PDP** | Builds authorization decisions using plugin adapters |
| **permission-ms** | Policy store | Provides per-app permission rules |
| **session-ms** | Cache | Caches `UserSession` (AuthMe + AuthorizationBundle) in Redis |
| **mock-external-ms** | Mock external data source | Returns mock user roles per subject |
| **frontoffice-/backoffice-plugin** | Plugins | Implement `AppAuthAdapter` to integrate domain-specific role & rule retrieval |

**Key principle:**  
Core shared modules (gateway, auth-ms, session-ms, permission-ms) must **never depend** on business or domain logic.  
Instead, new business domains (e.g. `frontoffice`, `backoffice`, `trainingoffice`) provide plugin JARs implementing generic interfaces defined in `common-lib`.

---

## 🧩 Redis Caching Model

| Key Pattern | Purpose |
|--------------|----------|
| `roles:{iss}:{sub}:{app}:{ver}` | Cached `UserSession` |
| `policyVer:{app}` | Incremented when policies change |
| `userVer:{iss}:{sub}` | Incremented when roles change |

Composite version:  
`ver = policyVer + '.' + userVer`

Old entries naturally expire via TTL.

---

## 🚀 Prerequisites

- **JDK 17+**
- **Maven 3.9+**
- **Redis 8.2+** (Docker recommended)
- **Git** (for config-repo if using Git backend)
- **IntelliJ IDEA Ultimate** (recommended)

---

## 🧰 Build

From the project root (`backend/`):

```bash
mvn clean install
```

This builds all modules and installs `common-lib` and plugin JARs locally for dependency resolution.

---

## ▶️ Run Order

1. **Discovery Server**  
   ```bash
   cd discovery-server
   mvn spring-boot:run
   ```  
   → http://localhost:6761

2. **Config Server**  
   ```bash
   cd config-server
   mvn spring-boot:run
   ```  
   → http://localhost:6002

3. **Session Service (Redis)**  
   ```bash
   cd session-ms
   mvn spring-boot:run
   ```  
   → http://localhost:6005

4. **Permission Service**  
   ```bash
   cd permission-ms
   mvn spring-boot:run
   ```  
   → http://localhost:6004

5. **Mock External Service**  
   ```bash
   cd mock-external-ms
   mvn spring-boot:run
   ```  
   → http://localhost:6006

6. **Auth Service (PDP)**  
   ```bash
   cd auth-ms
   mvn spring-boot:run
   ```  
   → http://localhost:6003

7. **API Gateway (PEP)**  
   ```bash
   cd api-gateway
   mvn spring-boot:run
   ```  
   → http://localhost:6001

8. **Business Microservices**  
   ```bash
   cd frontoffice-ms
   mvn spring-boot:run
   ```  
   ```bash
   cd backoffice-ms
   mvn spring-boot:run
   ```

---

## 🗂️ Configuration Repository

Located in `config-repo/`.

- Each YAML file matches the service name:
  - `api-gateway.yml`
  - `auth-ms.yml`
  - `permission-ms.yml`
  - `session-ms.yml`
  - `frontoffice-ms.yml`
  - `backoffice-ms.yml`
- Shared/global config:
  - `application.yml` (common settings)

---

## 🔍 Verification

- **Eureka Dashboard:** http://localhost:6761  
- **Config Server:** http://localhost:6002/auth-ms/default  
- **Gateway routes:** http://localhost:6000/api/**  
- **Redis UI (optional):** run RedisInsight or `redis-cli monitor`

---

## 💡 Development Tips

- Use IntelliJ **Run Dashboard** or **Compound Run Configurations** for multi-service startup.
- For local development:
  - Config Server: `spring.profiles.active=native`
  - Redis: run via Docker Compose
- When adding new domain services:
  1. Create a new plugin JAR implementing `AppAuthAdapter`
  2. Annotate with `@AppKey("<your-app>")`
  3. Add to `auth-ms` dependencies
  4. Provide mock data or live integration for roles

---

## Okta Setup (Shared Web App)

Both Front Office and Back Office now authenticate through a **single** Okta Web Application.

1. Create (or reuse) one Web Application with **Authorization Code** + **Refresh Token** grants.
2. Configure its credentials in `config-repo/api-gateway.yml` under `spring.security.oauth2.client.registration.shared-okta`.
3. Add the following **sign-in redirect URIs**:
   - `https://localhost:6001/login/oauth2/code/frontoffice-app`
   - `https://localhost:6001/login/oauth2/code/backoffice-app`
   - *(optional)* add the `http://` variants if TLS termination is not enabled locally.
4. Add these **post-logout redirect URIs** so Angular apps receive control after logout:
   - `http://localhost:4200`
   - `http://localhost:4201`
5. No additional Okta apps are needed. Onboarding another UI only requires extending the `app.oauth2.apps` map; runtime registrations are derived automatically.

---

## 🧠 AI & Developer Assistance

For AI assistants (ChatGPT / Codex) or new contributors working on the **SSO Demo** project:

➡️ Please refer to [`docs/CONTEXT.md`](./docs/CONTEXT.md)  
for a complete, up-to-date description of the architecture, microservice interactions,  
plugin-based authorization design, and Redis caching model.

That file serves as the canonical context for intelligent tooling or code analysis  
to understand how modules like **api-gateway**, **auth-ms**, **session-ms**, and  
**plugin adapters** integrate within the SSO platform.

---

© 2025 MathCode Lab
