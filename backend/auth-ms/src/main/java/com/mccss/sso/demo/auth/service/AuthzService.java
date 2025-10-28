package com.mccss.sso.demo.auth.service;


import com.mccss.sso.demo.auth.app.AppAdapterRegistry;
import com.mccss.sso.demo.auth.app.AppAuthAdapter;
import com.mccss.sso.demo.auth.app.CurrentAppResolver;
import com.mccss.sso.demo.commonlib.exception.ApplicationException;
import com.mccss.sso.demo.commonlib.integration.SessionClient;
import com.mccss.sso.demo.commonlib.model.AuthMe;
import com.mccss.sso.demo.commonlib.model.UserSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class AuthzService {

    private final CurrentAppResolver resolver;
    private final AppAdapterRegistry registry;
    private final SessionClient sessionClient;

    /**
     * Loads and caches user authentication and authorization data based on the provided JWT.
     * This method retrieves the app key from the JWT, determines the corresponding application
     * adapter, and builds the authorization bundle. The user session is then cached, and the
     * user's authentication details are returned.
     *
     * @param jwt the JWT containing claims and information required for authentication
     * @return a {@code Mono<AuthMe>} object containing the authenticated user's details, or
     *         an error if the application key is missing or invalid
     */
    public Mono<AuthMe> loadUserInfo(Jwt jwt) {
        log.info("Load and cache user authentication/authorization data");

        String appKey = resolver.resolveApp(jwt);
        if (appKey == null) {
            return Mono.error(new ApplicationException(HttpStatus.BAD_REQUEST.value(),
                    "Bad request: no app key found in JWT"));
        }

        AppAuthAdapter adapter = registry.get(appKey);
        if (adapter == null) {
            return Mono.error(new ApplicationException(HttpStatus.BAD_REQUEST.value(),
                    "Bad request: no adapter found for app key: " + appKey));
        }

        // 1) Build decisions
        return adapter.buildDecisions(jwt)
                // 2) Cache User Session which includes the role, decisions, etc
                .flatMap(authzBundle -> {
                    AuthMe authMe = buildAuthMeFromJwt(jwt, authzBundle.roles());
                    return sessionClient.cacheUserSession(new UserSession(authMe, authzBundle), buildBearerToken(jwt))
                            .thenReturn(authMe);
                });
    }

    /**
     * Retrieves the authenticated user's data from the cached user session.
     *
     * @return a {@code Mono<AuthMe>} containing the authentication and authorization details of the user
     *         retrieved from the cache, or an empty Mono if no user session is available.
     */
    public Mono<AuthMe> getUserInfo(Jwt jwt) {
        log.info("Get user authentication/authorization data from cache");
        return sessionClient.getUserSession(buildBearerToken(jwt))
                .map(UserSession::getAuthMe);
    }

    /**
     * Constructs and returns an instance of {@code AuthMe} populated with authentication
     * and authorization details derived from the provided JWT and roles.
     *
     * @param jwt   the JWT containing claims and authentication data
     * @param roles the list of roles assigned to the authenticated user
     * @return an {@code AuthMe} object containing the user's authentication and authorization information
     */
    private AuthMe buildAuthMeFromJwt(Jwt jwt, List<String> roles) {
        return AuthMe.builder()
                .authenticated(true)
                .subject(jwt.getSubject())
                .firstName(jwt.getClaimAsString("firstName"))
                .lastName(jwt.getClaimAsString("lastName"))
                .email(jwt.getClaimAsString("email"))
                .issuer(jwt.getIssuer() != null ? jwt.getIssuer().toString() : null)
                .issuedAt(jwt.getIssuedAt())
                .expiresAt(jwt.getExpiresAt())
                .roles(roles)
                .build();
    }

    private String buildBearerToken(Jwt jwt) {
        return "Bearer " + jwt.getTokenValue();
    }
}
