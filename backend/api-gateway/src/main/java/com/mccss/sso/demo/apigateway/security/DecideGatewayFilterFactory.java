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
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.util.pattern.PathPatternParser;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.util.pattern.PathPattern;
import org.springframework.http.server.PathContainer;

import java.util.*;

@Slf4j
@Component("Decide")
public class DecideGatewayFilterFactory extends AbstractGatewayFilterFactory<DecideGatewayFilterFactory.Config> {

    private final SessionClient sessionClient;
    private final PathPatternParser parser;

    public DecideGatewayFilterFactory(SessionClient sessionClient) {
        super(Config.class);
        this.sessionClient = sessionClient;
        this.parser = new PathPatternParser();
    }

    @Override
    public GatewayFilter apply(Config cfg) {
        return (exchange, chain) -> exchange.getPrincipal().flatMap(principal -> {
            if (!(principal instanceof Authentication authz) || !(authz instanceof JwtAuthenticationToken tok)) {
                exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                return exchange.getResponse().setComplete();
            }

            var req = exchange.getRequest();
            String action = req.getMethod().name();
            String resource = req.getPath().value();

            // appKey comes from route mapping (configured per route below)
            String app = cfg.getApp();
            if (app == null || app.isBlank()) {
                exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
                return exchange.getResponse().setComplete();
            }

            // Load cached decisions from session-ms
            return sessionClient.getUserSession()
                    .defaultIfEmpty(new UserSession(null /*AuthMe*/,
                            new AuthorizationBundle(null, null, app, List.of(), List.of(), 0)))
                    .flatMap(userSession -> {
                        var bundle = userSession.getAuthz();
                        var decisions = (bundle != null && bundle.decisions() != null) ? bundle.decisions() : List.<Decision>of();
                        // Check permission
                        boolean allowed = match(decisions, action, resource);
                        if (!allowed) {
                            exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
                            return exchange.getResponse().setComplete();
                        }
                        ServerHttpRequest mutated = req.mutate().headers(h -> h.set("X-App", app)).build();
                        return chain.filter(exchange.mutate().request(mutated).build());
                    });
        }).switchIfEmpty(chain.filter(exchange));
    }

    private boolean match(List<Decision> decisions, String action, String resource) {
        for (Decision d : decisions) {
            if (!d.allowed()) continue;
            if (!d.action().equalsIgnoreCase(action)) continue;

            PathPattern pat = parser.parse(d.resource()); // allow patterns like /api/backoffice/orders/**
            if (pat.matches(PathContainer.parsePath(resource))) {
                return true;
            }
        }
        return false;
    }

    @Getter
    @Setter
    public static class Config {
        private String app; // set per route
    }
}

