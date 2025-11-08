package com.mccss.sso.demo.apigateway.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.security.oauth2.client.OAuth2ClientProperties;
import org.springframework.boot.autoconfigure.security.oauth2.client.OAuth2ClientPropertiesMapper;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.InMemoryReactiveClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Configuration
@EnableConfigurationProperties({AppOAuthProperties.class, OAuth2ClientProperties.class})
public class ClientRegistrationConfig {

    /**
     * Creates a {@link ReactiveClientRegistrationRepository} that registers OAuth2 client configurations
     * derived from the base configuration and additional application-specific properties.
     * Validates the provided application OAuth properties and ensures that derived client registrations
     * are properly created based on the configured base registration.
     *
     * @param clientProperties the {@link OAuth2ClientProperties} containing base client configurations
     *                          as defined in the Spring `spring.security.oauth2.client` configuration
     * @param appOAuthProperties the {@link AppOAuthProperties} containing application-specific
     *                            extensions to the base configuration and additional app definitions
     * @return a {@link ReactiveClientRegistrationRepository} with the derived client registrations
     * @throws IllegalStateException if the base registration is not found or no derived
     *                                client registrations are created
     */
    @Bean
    public ReactiveClientRegistrationRepository reactiveClientRegistrationRepository(
            OAuth2ClientProperties clientProperties,
            AppOAuthProperties appOAuthProperties) {

        appOAuthProperties.validate();

        Map<String, ClientRegistration> mappedRegistrations =
                new OAuth2ClientPropertiesMapper(clientProperties).asClientRegistrations();

        ClientRegistration baseRegistration = mappedRegistrations.get(appOAuthProperties.getBaseRegistrationId());
        if (baseRegistration == null) {
            throw new IllegalStateException("Base registration '%s' not found in spring.security.oauth2.client configuration"
                    .formatted(appOAuthProperties.getBaseRegistrationId()));
        }

        List<ClientRegistration> derived = getClientRegistrations(appOAuthProperties, baseRegistration);

        if (derived.isEmpty()) {
            throw new IllegalStateException("No OAuth2 client registrations derived from app configuration");
        }

        return new InMemoryReactiveClientRegistrationRepository(derived);
    }

    /**
     * Generates a list of client registrations based on the provided application OAuth properties
     * and a base registration template. Each derived registration is configured with unique
     * registration properties specified for individual apps.
     *
     * @param appOAuthProperties the application-specific OAuth properties containing app configurations
     * @param baseRegistration   the base client registration template used as a reference for deriving new registrations
     * @return a list of derived {@code ClientRegistration} objects created for each app specified in the provided properties
     */
    private static List<ClientRegistration> getClientRegistrations(AppOAuthProperties appOAuthProperties,
                                                                   ClientRegistration baseRegistration) {
        List<ClientRegistration> derived = new ArrayList<>();
        appOAuthProperties.getApps().forEach((appKey, registrationProps) -> {
            ClientRegistration.Builder builder = ClientRegistration.withClientRegistration(baseRegistration)
                    .registrationId(registrationProps.getRegistrationId())
                    .redirectUri(registrationProps.getRedirectUri());

            if (StringUtils.hasText(registrationProps.getClientName())) {
                builder.clientName(registrationProps.getClientName());
            }

            ClientRegistration derivedRegistration = builder.build();
            derived.add(derivedRegistration);

            if (log.isDebugEnabled()) {
                log.debug("Registered OAuth2 client for appKey='{}' with registrationId='{}'",
                        appKey, derivedRegistration.getRegistrationId());
            }
        });
        return derived;
    }
}
