package com.mccss.sso.demo.apigateway.security;

import com.mccss.sso.demo.commonlib.exception.UnAuthorizedException;
import com.mccss.sso.demo.commonlib.integration.SessionSvcClient;
import com.mccss.sso.demo.commonlib.model.AuthorizationBundle;
import com.mccss.sso.demo.commonlib.model.Decision;
import com.mccss.sso.demo.commonlib.model.UserSession;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.PathContainer;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.util.pattern.PathPattern;
import org.springframework.web.util.pattern.PathPatternParser;
import reactor.core.publisher.Mono;

import java.util.List;

@Slf4j
@Component("Decide")
public class DecideGatewayFilterFactory extends AbstractGatewayFilterFactory<DecideGatewayFilterFactory.Config> {

    private final SessionSvcClient sessionSvcClient;
    private final ReactiveOAuth2AuthorizedClientService authorizedClientService;
    private final PathPatternParser parser = new PathPatternParser();

    // IMPORTANT: call super(Config.class)
    public DecideGatewayFilterFactory(SessionSvcClient sessionSvcClient,
                                      ReactiveOAuth2AuthorizedClientService authorizedClientService) {
        super(Config.class);
        this.sessionSvcClient = sessionSvcClient;
        this.authorizedClientService = authorizedClientService;
    }

    /**
     * Applies the filter logic to ensure authentication and handle the request accordingly.
     * This method ensures that the user is authenticated and then processes the request
     * by invoking the downstream chain and performing authorization checks based on the provided configuration.
     * If an UnAuthorizedException is encountered, an unauthorized response is sent.
     *
     * @param cfg the configuration object containing application-specific settings
     * @return the {@link GatewayFilter} that applies authentication and authorization logic to the request
     */
    @Override
    public GatewayFilter apply(Config cfg) {
        return (exchange, chain) ->
                ensureAuthentication(exchange)
                        .flatMap(auth -> handleWithAuthentication(exchange, chain, cfg, auth))
                        .onErrorResume(UnAuthorizedException.class, e -> unauthorized(exchange));
    }

    private Mono<Authentication> ensureAuthentication(ServerWebExchange exchange) {
        return exchange.getPrincipal()
                .switchIfEmpty(Mono.error(unauthorizedError("Principal is missing")))
                .flatMap(principal -> {
                    if (principal instanceof Authentication auth) {
                        return Mono.just(auth);
                    }
                    return Mono.error(unauthorizedError("Principal is missing"));
                });
    }

    /**
     * Handles the request with authentication by verifying the application's configuration,
     * resolving the Bearer token, fetching the user session, and forwarding the request if authorized.
     * In cases where the application configuration is invalid or the authorization fails,
     * appropriate error responses are generated.
     *
     * @param exchange the current server web exchange containing request and response details
     * @param chain the gateway filter chain to handle the next stage of processing
     * @param cfg the configuration object containing application-specific settings
     * @param auth the authentication object representing the authenticated user or request context
     * @return a {@link Mono} that completes when the request has been processed,
     *         or emits an error if authentication or authorization fails
     */
    private Mono<Void> handleWithAuthentication(ServerWebExchange exchange,
                                                GatewayFilterChain chain,
                                                Config cfg,
                                                Authentication auth) {
        String app = cfg.getApp();
        log.info("Handle request with authentication for application: {}", app);
        if (app == null || app.isBlank()) {
            return forbidden(exchange);
        }

        var request = exchange.getRequest();
        String action = request.getMethod().name();
        String resource = request.getPath().value();

        return resolveBearer(exchange, auth)
                .switchIfEmpty(Mono.error(unauthorizedError("Bearer token is missing")))
                .flatMap(bearer -> fetchUserSession(bearer, app)
                        .flatMap(session -> forwardIfAuthorized(session, action, resource, app, exchange, chain)));
    }

    private Mono<UserSession> fetchUserSession(String bearer, String app) {
        return sessionSvcClient.getUserSession(bearer, app)
                .defaultIfEmpty(new UserSession(
                        null, new AuthorizationBundle(null, null, app, List.of(), List.of(), 0)));
    }

    private Mono<Void> forwardIfAuthorized(UserSession session,
                                           String action,
                                           String resource,
                                           String app,
                                           ServerWebExchange exchange,
                                           GatewayFilterChain chain) {
        var bundle = session.getAuthz();
        var decisions = (bundle != null && bundle.decisions() != null)
                ? bundle.decisions()
                : List.<Decision>of();

        if (!match(decisions, action, resource)) {
            return forbidden(exchange);
        }

        var mutatedRequest = exchange.getRequest()
                .mutate()
                .headers(headers -> headers.set("X-App", app))
                .build();
        var mutatedExchange = exchange.mutate().request(mutatedRequest).build();
        return chain.filter(mutatedExchange);
    }

    // Response helpers
    private Mono<Void> unauthorized(ServerWebExchange exchange) {
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        return exchange.getResponse().setComplete();
    }

    private Mono<Void> forbidden(ServerWebExchange exchange) {
        exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
        return exchange.getResponse().setComplete();
    }

    private Mono<String> resolveBearer(ServerWebExchange exchange, Authentication auth) {
        String hdr = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (hdr != null && !hdr.isBlank()) {
            return Mono.just(hdr.startsWith("Bearer ") ? hdr : "Bearer " + hdr);
        }
        if (auth instanceof JwtAuthenticationToken jat) {
            return Mono.just("Bearer " + jat.getToken().getTokenValue());
        }
        if (auth instanceof OAuth2AuthenticationToken oat) {
            return authorizedClientService
                    .loadAuthorizedClient(oat.getAuthorizedClientRegistrationId(), oat.getName())
                    .map(c -> "Bearer " + c.getAccessToken().getTokenValue());
        }
        return Mono.empty();
    }

    private boolean match(List<Decision> decisions, String action, String resource) {
        PathContainer path = PathContainer.parsePath(resource);
        for (Decision decision : decisions) {
            if (!decision.allowed()) {
                continue;
            }
            if (!decision.action().equalsIgnoreCase(action)) {
                continue;
            }
            PathPattern pattern = parser.parse(decision.resource());
            if (pattern.matches(path)) {
                return true;
            }
        }
        return false;
    }

    private UnAuthorizedException unauthorizedError(String message) {
        return new UnAuthorizedException(HttpStatus.UNAUTHORIZED.value(), message);
    }

    @Getter
    @Setter
    public static class Config {
        private String app; // set per route
    }
}
