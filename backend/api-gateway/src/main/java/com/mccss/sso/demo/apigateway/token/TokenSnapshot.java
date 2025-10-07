package com.mccss.sso.demo.apigateway.token;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.Set;

@Getter
@Setter
@Builder
public class TokenSnapshot {
    private final String clientRegistrationId;
    private final String accessToken;
    private final String tokenType;
    private final Set<String> scopes;
    private final Instant issuedAt;
    private final Instant expiresAt;
    private final Instant refreshedAt;
}
