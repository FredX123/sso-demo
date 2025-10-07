package com.mccss.sso.demo.apigateway.token;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.OAuth2AuthorizeRequest;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.web.server.ServerOAuth2AuthorizedClientRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/token")
public class TokenController {

    private final TokenService tokenService;
    private final ServerOAuth2AuthorizedClientRepository authorizedClientRepo;
    private final List<String> knownClientIds;

    public TokenController(
            TokenService tokenService,
            ServerOAuth2AuthorizedClientRepository authorizedClientRepo,
            @Value("#{'${app.oauth2.client-ids:myb-app,sada-app}'.split(',')}") List<String> knownClientIds) {
        this.tokenService = tokenService;
        this.authorizedClientRepo = authorizedClientRepo;
        // trim spaces just in case
        this.knownClientIds = knownClientIds.stream().map(String::trim).collect(Collectors.toList());
    }

    @GetMapping("/refresh")
    public Mono<Map<String, Object>> refresh(ServerWebExchange exchange,
                                             Authentication auth,
                                             @RequestParam(name = "client", required = false) String clientParam) {
        if (auth == null) {
            return Mono.error(new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Not authenticated"));
        }

        return resolveClientId(exchange, auth, clientParam)
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.UNAUTHORIZED, "No authorized client found")))
                .flatMap(clientId -> {
                    // Build the authorize request (web concern: add the exchange attribute)
                    OAuth2AuthorizeRequest req = OAuth2AuthorizeRequest
                            .withClientRegistrationId(clientId)
                            .principal(auth)
                            .attribute(ServerWebExchange.class.getName(), exchange)
                            .build();

                    return tokenService.authorize(req)
                            .map(this::toBody);
                });
    }

    /**
     * Web-only concern: decide which clientRegistrationId to use.
     * 1) If ?client= provided (and known) -> use it.
     * 2) Else probe session to find an already-authorized client among known ids.
     */
    private Mono<String> resolveClientId(ServerWebExchange exchange, Authentication auth, String clientParam) {
        if (clientParam != null && !clientParam.isBlank()) {
            if (!knownClientIds.contains(clientParam)) {
                return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unknown client: " + clientParam));
            }
            return Mono.just(clientParam);
        }

        // Probe the session for an existing authorized client (first match wins)
        List<Mono<OAuth2AuthorizedClient>> probes = new ArrayList<>();
        for (String id : knownClientIds) {
            probes.add(authorizedClientRepo.loadAuthorizedClient(id, auth, exchange));
        }

        return Mono.firstWithValue(
                        probes.stream().map(m -> m.filter(Objects::nonNull)).toList()
                                  )
                .map(ac -> ac.getClientRegistration().getRegistrationId())
                .switchIfEmpty(Mono.empty());
    }

    private Map<String, Object> toBody(TokenSnapshot s) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("client",       s.getClientRegistrationId());
        m.put("access_token", s.getAccessToken());
        m.put("token_type",   s.getTokenType());
        m.put("scopes",       s.getScopes());
        m.put("issued_at",    s.getIssuedAt());
        m.put("expires_at",   s.getExpiresAt());
        m.put("refreshed_at", s.getRefreshedAt());
        return m;
    }
}