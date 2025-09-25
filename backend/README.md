# SSO Demo Backend

This project is a Spring Boot multi–module backend for demonstrating Single Sign-On (SSO) with
API Gateway, Config Server, Service Discovery, and microservices.

## Project Structure

```
backend/
│  pom.xml                 # Parent aggregator POM
│
├─ config-repo/            # Centralized configuration (native/Git backend for Config Server)
│
├─ config-server/          # Spring Cloud Config Server
├─ discovery-server/       # Eureka service registry
├─ api-gateway/            # Spring Cloud Gateway (WebFlux)
├─ permission-ms/          # Example microservice
├─ auth-ms/                # Example microservice
├─ myb-ms/          # Example microservice
├─ sada-ms/                # Example microservice
└─ common-lib/             # Shared DTOs, utils, mappers
```

## Prerequisites

- JDK 17+
- Maven 3.9+
- Git (for config-repo if using Git backend)
- IntelliJ IDEA Ultimate (recommended for development)

## Build

From the project root (`backend/`):

```bash
mvn clean install
```

This builds all modules and installs `common-lib` into the local Maven repo for use by services.

## Run Order

The system should be started in the following sequence:

1. **Discovery Server**  
   ```bash
   cd discovery-server
   mvn spring-boot:run
   ```  
   Runs on port **6761**.

2. **Config Server**  
   ```bash
   cd config-server
   mvn spring-boot:run
   ```  
   Runs on port **6002**. Loads configuration from `config-repo/`.

3. **API Gateway**  
   ```bash
   cd api-gateway
   mvn spring-boot:run
   ```  
   Runs on port **6001**. Forwards requests to downstream services.

4. **Microservices (permission-ms, auth-ms, myb-ms, sada-ms, etc.)**  
   Start each service. They register with Eureka and fetch configuration from Config Server.  
   ```bash
   cd permission-ms
   mvn spring-boot:run
   ```

   ```bash
   cd auth-ms
   mvn spring-boot:run
   ```

## Configuration Repository

- Config files are stored in `config-repo/`.  
- Filenames must match the Spring application name (`spring.application.name`).  
  - `permission-ms.yml` → for permission service
  - `auth-ms.yml` → for auth service
  - `myb-ms.yml` → for MyB Service  
  - `sada-ms.yml` → for SADA Service
- Global defaults go into `application.yml`.

## Verify Setup

- **Eureka Dashboard**: [http://localhost:6761](http://localhost:8761)  
- **Config Server**: [http://localhost:6002/permission-ms/default](http://localhost:6002/user-service/default)  
- **Gateway**: [http://localhost:6001/api/**](http://localhost:8080/api/**)

## Development Tips

- Use IntelliJ **Run Dashboard** to manage all Spring Boot applications.  
- You can create a **Compound Run Configuration** to start Discovery, Config Server, Gateway, and services together.  
- For config-repo:  
  - Local dev: use native mode (`file:./config-repo`)  
  - Production: use Git-backed repo

---

© 2025 MathCode Lab
