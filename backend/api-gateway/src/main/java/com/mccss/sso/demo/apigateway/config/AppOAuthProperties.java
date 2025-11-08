package com.mccss.sso.demo.apigateway.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.Assert;

import java.util.*;

@Getter
@Setter
@ConfigurationProperties(prefix = "app.oauth2")
public class AppOAuthProperties {

    /**
     * Registration id defined under spring.security.oauth2.client.registration
     * that serves as the template for all derived registrations.
     */
    private String baseRegistrationId;

    /**
     * Registration id that should be used when no app is specified
     * (e.g. default login redirect).
     */
    private String defaultRegistrationId;

    /**
     * Map of appKey -> registration definition.
     */
    private Map<String, AppRegistration> apps = new LinkedHashMap<>();

    public void setApps(Map<String, AppRegistration> apps) {
        this.apps = apps != null ? apps : new LinkedHashMap<>();
        this.apps.forEach((key, registration) -> registration.setAppKey(key));
    }

    public List<String> getRegistrationIds() {
        List<String> ids = new ArrayList<>();
        apps.values().stream()
                .map(AppRegistration::getRegistrationId)
                .filter(Objects::nonNull)
                .forEach(ids::add);
        return ids;
    }

    public Optional<AppRegistration> findByRegistrationId(String registrationId) {
        return apps.values().stream()
                .filter(app -> Objects.equals(app.getRegistrationId(), registrationId))
                .findFirst();
    }

    public AppRegistration requireByRegistrationId(String registrationId) {
        return findByRegistrationId(registrationId)
                .orElseThrow(() -> new IllegalArgumentException("Unknown registrationId: " + registrationId));
    }

    public Optional<AppRegistration> findByAppKey(String appKey) {
        return Optional.ofNullable(apps.get(appKey));
    }

    public void validate() {
        Assert.hasText(baseRegistrationId, "app.oauth2.base-registration-id must be provided");
        Assert.hasText(defaultRegistrationId, "app.oauth2.default-registration-id must be provided");
        Assert.state(!apps.isEmpty(), "app.oauth2.apps must define at least one app");
        apps.forEach((key, registration) -> {
            Assert.hasText(registration.getRegistrationId(),
                    () -> "registration-id is required for app '" + key + "'");
            Assert.hasText(registration.getRedirectUri(),
                    () -> "redirect-uri is required for app '" + key + "'");
            Assert.hasText(registration.getAngularRedirect(),
                    () -> "angular-redirect is required for app '" + key + "'");
            Assert.hasText(registration.getLogoutRedirect(),
                    () -> "logout-redirect is required for app '" + key + "'");
        });
        findByRegistrationId(defaultRegistrationId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "default-registration-id must match one of app registrations"));
    }

    @Getter
    @Setter
    public static class AppRegistration {
        private String appKey;
        private String registrationId;
        private String clientName;
        private String redirectUri;
        private String angularRedirect;
        private String logoutRedirect;
    }
}
