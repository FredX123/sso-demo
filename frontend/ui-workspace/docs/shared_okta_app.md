# Shared Okta Application Migration Guide

This note captures the reasoning, Okta configuration, and code changes required to consolidate the Frontoffice and Backoffice experiences onto a single Okta Web Application while preserving per-app routing, authorization plugins, and UI behavior.

---

## 1. Design: From Two Apps To One

- **Single trust, multi-app behavior** – Spring Cloud Gateway keeps resolving the app key from `/api/{app}/**` and still stamps `X-App` headers; auth-ms plugins remain untouched. We simply swapped the two Okta client registrations for one shared registration that is cloned per `appKey` at runtime.
- **Derived client registrations** – `app.oauth2` (see `backend/config-repo/api-gateway.yml`) defines a template (`shared-okta`) plus the per-app metadata (registration id, redirect URI, Angular landing page, logout target). A dedicated `ClientRegistrationConfig` turns those entries into real `ClientRegistration`s at startup.
- **UI parity** – Each Angular shell still points to `/oauth2/authorization/{app-registration}?redirectTo=…`, so bookmarks and cross-links continue to work even though Okta only sees one underlying application.
- **Token & logout handling** – Gateway login success and logout handlers look up the Angular redirect/logout URLs from `AppOAuthProperties`, so the correct UI receives control regardless of which app triggered the login.

> ✅ Result: Operations only manage one Okta app (one client ID/secret), while developers keep the lightweight per-app surface that already exists in the gateway and plugins.

---

## 2. Okta Configuration Steps

1. **Create / reuse a Web Application**
   - Type: *OIDC – Web*
   - Grants: *Authorization Code* and *Refresh Token*
   - Client authentication: *Client secret basic* (default)
2. **Credentials**  
   - Record the `clientId` and `clientSecret`; place them under `spring.security.oauth2.client.registration.shared-okta` in `backend/config-repo/api-gateway.yml`.
3. **Sign-in redirect URIs**  
   Add both per-app callbacks (plus optional `http://` variants for non-TLS dev runs):
   ```
   http://localhost:6001/login/oauth2/code/frontoffice-app
   http://localhost:6001/login/oauth2/code/backoffice-app
   ```
4. **Post-logout redirect URIs**
   ```
   http://localhost:4200
   http://localhost:4201
   ```
5. **Assignments** – Assign every test user (or group) to the shared app so Okta can issue tokens.
6. **Custom claims (optional but recommended)** – If downstream services key off email, add a reusable claim (e.g., `userEmail = user.email`) under *Security → API → Authorization Servers → default → Claims* and expose it in both ID and Access tokens. The Spring services can then bind to that claim if required.

---

## 3. Implementation Walkthrough

### 3.1 Config Repository

`backend/config-repo/api-gateway.yml` holds the shared template and the front/back derived registrations:

```yaml
spring:
  security:
    oauth2:
      client:
        registration:
          shared-okta:
            provider: okta
            client-id: 0oar9oovewJd7YCav5d7
            client-secret: TPlUksnDr4vgeTrw1Z_394flkINBxAkDHoexCRuwbn0aW8I3YC7uxABIr8hXR-Pf
            scope: openid, profile, email, offline_access
            authorization-grant-type: authorization_code

app:
  oauth2:
    base-registration-id: shared-okta
    default-registration-id: frontoffice-app
    apps:
      frontoffice:
        registration-id: frontoffice-app
        client-name: Frontoffice
        redirect-uri: "{baseUrl}/login/oauth2/code/frontoffice-app"
        angular-redirect: http://localhost:4200
        logout-redirect: http://localhost:4200
      backoffice:
        registration-id: backoffice-app
        client-name: Backoffice
        redirect-uri: "{baseUrl}/login/oauth2/code/backoffice-app"
        angular-redirect: http://localhost:4201
        logout-redirect: http://localhost:4201
```

Changing (or adding) an app is now a matter of editing this block—no extra Okta application is needed.

### 3.2 Gateway runtime wiring

1. **AppOAuthProperties** – binds the YAML structure and enforces that every app entry has the required metadata.

```java
// backend/api-gateway/.../AppOAuthProperties.java
@ConfigurationProperties(prefix = "app.oauth2")
public class AppOAuthProperties {
    private String baseRegistrationId;
    private String defaultRegistrationId;
    private Map<String, AppRegistration> apps = new LinkedHashMap<>();
    // helper lookups + validation omitted for brevity
}
```

2. **ClientRegistrationConfig** – clones the base registration into one `ClientRegistration` per app so the standard Spring OAuth machinery can keep using `frontoffice-app` / `backoffice-app`.

```java
// backend/api-gateway/.../ClientRegistrationConfig.java
@Bean
public ReactiveClientRegistrationRepository reactiveClientRegistrationRepository(
        OAuth2ClientProperties clientProperties,
        AppOAuthProperties appOAuthProperties) {

    Map<String, ClientRegistration> mapped =
        new OAuth2ClientPropertiesMapper(clientProperties).asClientRegistrations();
    ClientRegistration base = mapped.get(appOAuthProperties.getBaseRegistrationId());

    List<ClientRegistration> derived = new ArrayList<>();
    appOAuthProperties.getApps().forEach((appKey, props) -> {
        derived.add(ClientRegistration.withClientRegistration(base)
                .registrationId(props.getRegistrationId())
                .redirectUri(props.getRedirectUri())
                .clientName(props.getClientName())
                .build());
    });

    return new InMemoryReactiveClientRegistrationRepository(derived);
}
```

3. **SecurityConfig** – uses the same properties to decide which login page, Angular redirect, and logout URL to use.

```java
// backend/api-gateway/.../SecurityConfig.java
return http
    .oauth2Login(login -> login.authenticationSuccessHandler(redirectToAngular()))
    .logout(logout -> logout
        .logoutSuccessHandler(oidcLogoutSuccessHandler(clientRegistrationRepository)))
    .build();

private String getClientRedirectUrl(OAuth2AuthenticationToken authentication) {
    return oAuthProperties.findByRegistrationId(authentication.getAuthorizedClientRegistrationId())
            .map(AppOAuthProperties.AppRegistration::getAngularRedirect)
            .orElseGet(this::defaultAngularRedirect);
}
```

4. **TokenController** – keeps token refresh working by pulling the allowed registration IDs from `AppOAuthProperties` instead of a hard-coded list.

```java
// backend/api-gateway/.../TokenController.java
this.knownClientIds = appOAuthProperties.getRegistrationIds().stream()
        .map(String::trim)
        .collect(Collectors.toList());
```

### 3.3 Frontend changes

Both Angular shells continue to reference their own authorization endpoints; no UI logic changes were required beyond ensuring the login URLs point to the shared gateway host:

```ts
// projects/frontoffice-ui/src/environments/environment.ts
loginUrl: `${baseUrl}/oauth2/authorization/frontoffice-app?redirectTo=/dashboard`,

// projects/backoffice-ui/src/environments/environment.ts
loginUrl: `${baseUrl}/oauth2/authorization/backoffice-app?redirectTo=/my-applications`,
```

### 3.4 Validation checklist

1. Restart Config Server + gateway after editing `config-repo`.
2. Hit `/oauth2/authorization/frontoffice-app?redirectTo=/dashboard` – expect Okta to redirect back to `/login/oauth2/code/frontoffice-app` and then to `http://localhost:4200/dashboard`.
3. Repeat for backoffice; confirm logout flows bring you back to the correct Angular SPA.
4. Exercise `/api/token/refresh?client=frontoffice-app` to verify refresh tokens are reused independent of the shared Okta registration.

---

### Frequently Asked Questions

| Question | Answer |
| --- | --- |
| *Do we still need two Okta apps for new domains?* | No. Add an entry under `app.oauth2.apps` with its own registration id + redirect URLs. |
| *Where do I rotate client secrets?* | Only once in `spring.security.oauth2.client.registration.shared-okta`. Derived registrations pick up the change automatically. |
| *Can we change the default landing page?* | Update `app.oauth2.default-registration-id` (and optionally the Angular redirect) – the gateway’s entry point uses that value whenever a request does not specify `redirectTo`. |

This guide should provide enough context for onboarding new contributors or revisiting the configuration later. Feel free to extend it with environment-specific tips as the shared Okta footprint evolves.
