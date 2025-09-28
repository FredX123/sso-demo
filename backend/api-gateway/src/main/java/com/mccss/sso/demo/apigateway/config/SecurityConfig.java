package com.mccss.sso.demo.apigateway.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientProviderBuilder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.client.oidc.web.server.logout.OidcClientInitiatedServerLogoutSuccessHandler;
import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.DefaultReactiveOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.web.server.ServerOAuth2AuthorizedClientRepository;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authentication.RedirectServerAuthenticationEntryPoint;
import org.springframework.security.web.server.authentication.RedirectServerAuthenticationSuccessHandler;
import org.springframework.security.web.server.authentication.ServerAuthenticationSuccessHandler;
import org.springframework.security.web.server.authentication.logout.ServerLogoutSuccessHandler;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;
import org.springframework.web.server.ServerWebExchange;

import java.net.URI;
import java.util.List;

import static org.springframework.security.config.Customizer.withDefaults;

@Slf4j
@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    @Value("${client.redirect-url.myb-app:}")
    private String mybAppUrl;

    @Value("${client.redirect-url.sada-app:}")
    private String sadaAppUrl;

    @Bean
    public SecurityWebFilterChain springSecurityFilterChain(
            ServerHttpSecurity http,
            ReactiveClientRegistrationRepository clientRegistrationRepository) {

        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .authorizeExchange(exchanges -> exchanges
                        .pathMatchers("/actuator/**", "/public/**").permitAll()
                        .anyExchange().authenticated())
                .exceptionHandling(e -> e
                                // For APIs, return 401 instead of redirecting
                                .authenticationEntryPoint((exchange, ex) -> {
                                    if (exchange.getRequest().getPath().toString().startsWith("/api/")) {
                                        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                                        return exchange.getResponse().setComplete();
                                    }
                                    // For pages, keep default redirect to login
                                    return new RedirectServerAuthenticationEntryPoint("/oauth2/authorization/myb-app")
                                            .commence(exchange, ex);
                                }))
                .oauth2Login(login -> login
                        .authenticationSuccessHandler(redirectToAngular())) // redirects to Angular after login
                .oauth2Client(withDefaults())
                .logout(logout -> logout
                        .logoutSuccessHandler(oidcLogoutSuccessHandler(clientRegistrationRepository)))
                .build(); // ✅ TokenRelay is configured via application.yml route filters
    }

    /**
     * ReactiveOAuth2AuthorizedClientManager automatically refreshes access tokens using the stored
     * refresh token—including rotation. If the refresh token is rotated, Spring will persist the new one via
     * the ReactiveOAuth2AuthorizedClientService/Repository.
     */
    @Bean
    public ReactiveOAuth2AuthorizedClientManager authorizedClientManager(
            ReactiveClientRegistrationRepository registrations,
            ServerOAuth2AuthorizedClientRepository authorizedClients) {

        var provider = ReactiveOAuth2AuthorizedClientProviderBuilder.builder()
                .authorizationCode()
                .refreshToken()  // <- enables refresh on expiry; supports rotation transparently
                .build();

        var manager =
                new DefaultReactiveOAuth2AuthorizedClientManager(registrations, authorizedClients);
        manager.setAuthorizedClientProvider(provider);

        return manager;
    }

    private ServerAuthenticationSuccessHandler redirectToAngular() {
        return (exchange, authentication) -> {
            ServerWebExchange webExchange = exchange.getExchange();

            String defaultRedirect = getClientRedirectUrl((OAuth2AuthenticationToken) authentication);

            return webExchange.getSession().flatMap(session -> {
                String redirectTo = (String) session.getAttributes().get("redirectTo");
                log.info("Redirecting to {}", redirectTo);

                // Clear it from session after use
                session.getAttributes().remove("redirectTo");

                String finalRedirect = (redirectTo != null && !redirectTo.isBlank())
                        ? defaultRedirect + redirectTo
                        : defaultRedirect;

                RedirectServerAuthenticationSuccessHandler handler =
                        new RedirectServerAuthenticationSuccessHandler(finalRedirect);

                return handler.onAuthenticationSuccess(exchange, authentication);
            });
        };
    }

    private ServerLogoutSuccessHandler oidcLogoutSuccessHandler(
            ReactiveClientRegistrationRepository clientRegistrationRepository) {

        return (exchange, authentication) -> {
            String redirectUri = getClientRedirectUrl((OAuth2AuthenticationToken) authentication);

            OidcClientInitiatedServerLogoutSuccessHandler handler =
                    new OidcClientInitiatedServerLogoutSuccessHandler(clientRegistrationRepository);
            handler.setPostLogoutRedirectUri(String.valueOf(URI.create(redirectUri)));

            return handler.onLogoutSuccess(exchange, authentication);
        };
    }

    private String getClientRedirectUrl(OAuth2AuthenticationToken authentication) {
        String client = authentication.getAuthorizedClientRegistrationId();

        return switch (client) {
            case "myb-app" -> mybAppUrl;
            case "sada-app" -> sadaAppUrl;
            default -> mybAppUrl; // fallback
        };
    }

    @Bean
    public UrlBasedCorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of("http://localhost:4200", "http://localhost:4201"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true); // ✅ important for cookies/session-based auth

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}

