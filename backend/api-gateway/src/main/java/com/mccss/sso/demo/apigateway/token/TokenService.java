package com.mccss.sso.demo.apigateway.token;

import org.springframework.security.oauth2.client.OAuth2AuthorizeRequest;
import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientManager;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.Collections;

@Service
public class TokenService {

    private final ReactiveOAuth2AuthorizedClientManager manager;

    public TokenService(ReactiveOAuth2AuthorizedClientManager manager) {
        this.manager = manager;
    }

    public Mono<TokenSnapshot> authorize(OAuth2AuthorizeRequest authorizeRequest) {
        return manager.authorize(authorizeRequest)
                .switchIfEmpty(Mono.error(new IllegalStateException("Authorization failed")))
                .map(ac -> {
                    var reg = ac.getClientRegistration();
                    var at  = ac.getAccessToken();

                    return TokenSnapshot.builder()
                            .clientRegistrationId(reg != null ? reg.getRegistrationId() : null)
                            .accessToken(at != null ? at.getTokenValue() : null)
                            .tokenType(at != null ? at.getTokenType().getValue() : null)
                            .scopes(at != null ? at.getScopes() : Collections.emptySet())
                            .issuedAt(at != null ? at.getIssuedAt() : null)
                            .expiresAt(at != null ? at.getExpiresAt() : null)
                            .refreshedAt(Instant.now())
                            .build();
                });
    }
}
