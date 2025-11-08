package com.mccss.sso.demo.apigateway.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientProviderBuilder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.client.oidc.web.server.logout.OidcClientInitiatedServerLogoutSuccessHandler;
import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.DefaultReactiveOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.web.server.ServerOAuth2AuthorizedClientRepository;
import org.springframework.security.oauth2.client.web.server.WebSessionServerOAuth2AuthorizedClientRepository;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authentication.RedirectServerAuthenticationEntryPoint;
import org.springframework.security.web.server.authentication.RedirectServerAuthenticationSuccessHandler;
import org.springframework.security.web.server.authentication.ServerAuthenticationSuccessHandler;
import org.springframework.security.web.server.authentication.logout.ServerLogoutSuccessHandler;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.time.Duration;
import java.util.List;

import static org.springframework.security.config.Customizer.withDefaults;

@Slf4j
@RequiredArgsConstructor
@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    private final AppOAuthProperties oAuthProperties;

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
                        // APIs: 401 JSON (no redirect)
                        .authenticationEntryPoint(this::handleUnauthorized)
                        .accessDeniedHandler((exchange, exDenied) -> {
                            return handleForbidden(exchange, Mono.error(exDenied));
                        })
                )
                .oauth2Login(login -> login
                        .authenticationSuccessHandler(redirectToAngular())) // redirects to Angular after login
                .oauth2Client(withDefaults())
                .logout(logout -> logout
                        .logoutSuccessHandler(oidcLogoutSuccessHandler(clientRegistrationRepository)))
                .build(); // ✅ TokenRelay is configured via application.yml route filters
    }

    private static Mono<Void> handleForbidden(ServerWebExchange exchange, Mono<Void> exDenied) {
        // APIs: 403 JSON
        var path = exchange.getRequest().getPath().value();
        if (path.startsWith("/api/")) {
            exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
            return exchange.getResponse().setComplete();
        }
        return exDenied;
    }

    private Mono<Void> handleUnauthorized(ServerWebExchange exchange, AuthenticationException exAuth) {
        var path = exchange.getRequest().getPath().value();
        if (path.startsWith("/api/")) {
            exchange.getResponse().setStatusCode(org.springframework.http.HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }
        // Pages: fall back to default redirect-to-login (handled by oauth2Login())
        return new RedirectServerAuthenticationEntryPoint(
                "/oauth2/authorization/" + oAuthProperties.getDefaultRegistrationId()
        ).commence(exchange, exAuth);
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
                .refreshToken(builder -> builder.clockSkew(Duration.ofMinutes(4))) // refresh when <=4 min remain
                .build();

        var manager =
                new DefaultReactiveOAuth2AuthorizedClientManager(registrations, authorizedClients);
        manager.setAuthorizedClientProvider(provider);

        return manager;
    }

    /**
     * For links like /oauth2/authorization/frontoffice-app?redirectTo=/dashboard
     */
    @Bean
    public WebFilter captureRedirectTo() {
        return (exchange, chain) -> {
            var req = exchange.getRequest();
            var path = req.getPath().value();

            // Only intercept the OAuth2 authorization endpoints
            if (path.startsWith("/oauth2/authorization/")) {
                var redirectTo = req.getQueryParams().getFirst("redirectTo");
                if (redirectTo != null && !redirectTo.isBlank()) {
                    return exchange.getSession().flatMap(session -> {
                        session.getAttributes().put("redirectTo", redirectTo);
                        return chain.filter(exchange);
                    });
                }
            }
            return chain.filter(exchange);
        };
    }

    @Bean
    public ServerOAuth2AuthorizedClientRepository authorizedClientRepository() {
        return new WebSessionServerOAuth2AuthorizedClientRepository();
    }

    private ServerAuthenticationSuccessHandler redirectToAngular() {
        return (exchange, authentication) -> {
            var webExchange = exchange.getExchange();

            var  defaultRedirect = getClientRedirectUrl((OAuth2AuthenticationToken) authentication); // e.g. http://localhost:4200, http://localhost:4201

            return webExchange.getSession().flatMap(session -> {
                String redirectTo = (String) session.getAttributes().get("redirectTo");  // Read session attribute "redirectTo"
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
            String redirectUri = getClientLogoutRedirectUrl(
                    authentication instanceof OAuth2AuthenticationToken o ? o : null);

            var handler = new OidcClientInitiatedServerLogoutSuccessHandler(clientRegistrationRepository);
            handler.setPostLogoutRedirectUri(String.valueOf(URI.create(redirectUri)));

            return handler.onLogoutSuccess(exchange, authentication);
        };
    }

    private String getClientRedirectUrl(OAuth2AuthenticationToken authentication) {
        String regId = (authentication != null) ? authentication.getAuthorizedClientRegistrationId() : null;
        if (regId != null) {
            return oAuthProperties.findByRegistrationId(regId)
                    .map(AppOAuthProperties.AppRegistration::getAngularRedirect)
                    .orElseGet(this::defaultAngularRedirect);
        }
        return defaultAngularRedirect();
    }

    private String getClientLogoutRedirectUrl(OAuth2AuthenticationToken authentication) {
        String regId = (authentication != null) ? authentication.getAuthorizedClientRegistrationId() : null;
        if (regId != null) {
            return oAuthProperties.findByRegistrationId(regId)
                    .map(AppOAuthProperties.AppRegistration::getLogoutRedirect)
                    .orElseGet(this::defaultLogoutRedirect);
        }
        return defaultLogoutRedirect();
    }

    private String defaultAngularRedirect() {
        return oAuthProperties.requireByRegistrationId(oAuthProperties.getDefaultRegistrationId())
                .getAngularRedirect();
    }

    private String defaultLogoutRedirect() {
        return oAuthProperties.requireByRegistrationId(oAuthProperties.getDefaultRegistrationId())
                .getLogoutRedirect();
    }

    @Bean
    public UrlBasedCorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of(
                "http://localhost:4200",
                "http://localhost:4201"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true); // ✅ important for cookies/session-based auth

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
