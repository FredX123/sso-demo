package com.mccss.sso.demo.auth.service;


import com.mccss.sso.demo.auth.config.AuthzProps;
import com.mccss.sso.demo.commonlib.dto.AuthMe;
import com.mccss.sso.demo.commonlib.dto.UserAuthorization;
import com.mccss.sso.demo.commonlib.integration.CacheWebClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Slf4j
@RequiredArgsConstructor
@Service
public class AuthzService {

    private final CacheWebClient cacheWebClient;
    private final AuthzProps authzProps;

    public Mono<AuthMe> cacheAuthz(Jwt jwt) {
        log.info("Load and cache user authentication/authorization data");

        if (jwt == null) {
            return Mono.just(AuthMe.builder().authenticated(false).build());
        }

        AuthMe authMe = buildAuthMeFromJwt(jwt);

        return cacheWebClient.cacheAuthz(authMe);
    }

    public Mono<AuthMe> getAuthz() {
        log.info("Get user authentication/authorization data from cache");
        return cacheWebClient.getAuthz();
    }

    private AuthMe buildAuthMeFromJwt(Jwt jwt) {
        UserAuthorization userAuthorization = UserAuthorization.builder()
                .roles(authzProps.getMock().getClaims().getRoles())
                .permissions(authzProps.getMock().getClaims().getPermissions())
                .build();

        AuthMe authMe = AuthMe.builder()
                .authenticated(true)
                .subject(jwt.getSubject())
                .firstName(jwt.getClaimAsString("firstName"))
                .lastName(jwt.getClaimAsString("lastName"))
                .email(jwt.getClaimAsString("email"))
                .issuer(jwt.getIssuer() != null ? jwt.getIssuer().toString() : null)
                .issuedAt(jwt.getIssuedAt())
                .expiresAt(jwt.getExpiresAt())
                .authz(userAuthorization)
                .build();
        return authMe;
    }
}
