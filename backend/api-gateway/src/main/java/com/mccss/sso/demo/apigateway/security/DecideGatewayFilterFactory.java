package com.mccss.sso.demo.apigateway.security;

import com.mccss.sso.demo.commonlib.integration.SessionClient;
import com.mccss.sso.demo.commonlib.model.AuthorizationBundle;
import com.mccss.sso.demo.commonlib.model.Decision;
import com.mccss.sso.demo.commonlib.model.UserSession;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
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

    private final SessionClient sessionClient;
    private final ReactiveOAuth2AuthorizedClientService authorizedClientService;
    private final PathPatternParser parser = new PathPatternParser();

    // ✅ IMPORTANT: call super(Config.class)
    public DecideGatewayFilterFactory(SessionClient sessionClient,
                                      ReactiveOAuth2AuthorizedClientService authorizedClientService) {
        super(Config.class); // <-- this prevents the ClassCastException
        this.sessionClient = sessionClient;
        this.authorizedClientService = authorizedClientService;
    }

    @Override
    public GatewayFilter apply(Config cfg) {
        return (exchange, chain) ->
                exchange.getPrincipal().flatMap(p -> {
                            if (!(p instanceof Authentication auth)) {
                                exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                                return exchange.getResponse().setComplete();
                            }

                            final String app = cfg.getApp();
                            if (app == null || app.isBlank()) {
                                exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
                                return exchange.getResponse().setComplete();
                            }

                            var req = exchange.getRequest();
                            String action = req.getMethod() != null ? req.getMethod().name() : "GET";
                            String resource = req.getPath().value();

                            return resolveBearer(exchange, auth)
                                    .flatMap(bearer -> sessionClient.getUserSession(bearer)
                                            .defaultIfEmpty(new UserSession(
                                                    null, new AuthorizationBundle(null, null, app, List.of(), List.of(), 0)))
                                            .flatMap(us -> {
                                                var bundle = us.getAuthz();
                                                var decisions = (bundle != null && bundle.decisions() != null)
                                                        ? bundle.decisions() : List.<Decision>of();

                                                if (!match(decisions, action, resource)) {
                                                    exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
                                                    return exchange.getResponse().setComplete();
                                                }

                                                var mutated = req.mutate().headers(h -> h.set("X-App", app)).build();
                                                return chain.filter(exchange.mutate().request(mutated).build());
                                            })
                                    )
                                    .switchIfEmpty(Mono.defer(() -> {
                                        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                                        return exchange.getResponse().setComplete();
                                    }));
                        })
                        .switchIfEmpty(Mono.defer(() -> {
                            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                            return exchange.getResponse().setComplete();
                        }));
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
        for (Decision d : decisions) {
            if (!d.allowed()) continue;
            if (!d.action().equalsIgnoreCase(action)) continue;
            PathPattern pat = parser.parse(d.resource());
            if (pat.matches(path)) return true;
        }
        return false;
    }

    @Getter
    @Setter
    public static class Config {
        private String app; // set per route
    }
}

