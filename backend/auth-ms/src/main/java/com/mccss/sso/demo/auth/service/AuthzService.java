package com.mccss.sso.demo.auth.service;


import com.mccss.sso.demo.auth.app.AppAdapterRegistry;
import com.mccss.sso.demo.auth.app.AppAuthAdapter;
import com.mccss.sso.demo.commonlib.exception.ApplicationException;
import com.mccss.sso.demo.commonlib.integration.SessionSvcClient;
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

    private final AppAdapterRegistry registry;
    private final SessionSvcClient sessionSvcClient;

    /**
     * Loads and caches user authentication and authorization data based on the provided JWT.
     * This method retrieves the app key from the JWT, determines the corresponding application
     * adapter, and builds the authorization bundle. The user session is then cached, and the
     * user's authentication details are returned.
     *
     * @param jwt the JWT containing claims and information required for authentication
     * @param appKey the application key used to resolve the corresponding application
     * @return a {@code Mono<AuthMe>} object containing the authenticated user's details, or
     *         an error if the application key is missing or invalid
     */
    public Mono<AuthMe> loadUserInfo(Jwt jwt, String appKey) {
        log.info("Load and cache user authentication/authorization data. Application: {}", appKey);

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
                    return sessionSvcClient.cacheUserSession(new UserSession(authMe, authzBundle), buildBearerToken(jwt))
                            .thenReturn(authMe);
                });
    }

    /**
     * Updates and caches the user's session based on the provided JWT and application key.
     * This method retrieves the user's session, updates the authentication data, and caches
     * the updated session before returning the updated authentication details.
     *
     * @param jwt    the JWT containing claims and authentication data
     * @param appKey the application key used to resolve the corresponding application and session
     * @return a {@code Mono<Object>} containing the user's updated authentication details,
     *         or an empty Mono if the session does not exist or has no authentication data
     */
    public Mono<AuthMe> touchSession(Jwt jwt, String appKey) {
        log.info("Touch user session. Application: {}", appKey);
        String bearer = buildBearerToken(jwt);
        return sessionSvcClient.getUserSession(bearer, appKey)
                .flatMap(userSession -> {
                    if (userSession == null || userSession.getAuthMe() ==null) {
                        return Mono.empty();
                    }

                    AuthMe old = userSession.getAuthMe();
                    AuthMe updated = buildAuthMeFromJwt(jwt, old.getRoles());

                    UserSession updatedUs = new UserSession(updated, userSession.getAuthz());
                    return sessionSvcClient.cacheUserSession(updatedUs, bearer).thenReturn(updated);
                });
    }


    /**
     * Retrieves user authentication and authorization data from the cache based on the provided JWT.
     * This method uses the specified application key to resolve the corresponding app and fetch
     * the cached user session, extracting authentication details from it.
     *
     * @param jwt    the JWT containing claims and authentication data
     * @param appKey the application key used to resolve the corresponding application
     * @return a {@code Mono<AuthMe>} containing the user's authentication details, or an error
     *         if the session cannot be retrieved
     */
    public Mono<AuthMe> getUserInfo(Jwt jwt, String appKey) {
        log.info("Get user authentication/authorization data from cache. Application: {}", appKey);
        return sessionSvcClient.getUserSession(buildBearerToken(jwt), appKey)
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
