package com.mccss.sso.demo.auth.controller;

import com.mccss.sso.demo.auth.service.AuthzService;
import com.mccss.sso.demo.commonlib.model.AuthMe;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/auth/authorizations")
public class AuthController {

    private final AuthzService authzService;

    /**
     * Loads the authentication and authorization information for the user identified by the provided JWT.
     *
     * @param jwt the JSON Web Token containing user identity and claims
     * @return a {@code Mono} emitting a {@code ResponseEntity} containing an {@code AuthMe} object
     *         with the user's authentication and authorization details
     */
    @GetMapping("/load")
    public Mono<ResponseEntity<AuthMe>> loadUserInfo(@AuthenticationPrincipal Jwt jwt,
                                                     @RequestHeader(name = "X-App", required = false) String app) {
        log.info("Load and cache user authentication/authorization data for app: {}", app);

        return authzService.loadUserInfo(jwt, app)
                .map(ResponseEntity::ok);
    }

    /**
     * Retrieves user authentication and authorization data from the cache.
     *
     * @return a {@code Mono} emitting a {@code ResponseEntity} containing an {@code AuthMe} object
     *         with the user's authentication and authorization details.
     */
    @GetMapping()
    public Mono<ResponseEntity<AuthMe>> getUserInfo(@AuthenticationPrincipal Jwt jwt,
                                                    @RequestHeader(name = "X-App", required = false) String app) {
        log.info("Get user authentication/authorization data from cache for app: {}", app);

        return authzService.getUserInfo(jwt, app)
                .map(ResponseEntity::ok);
    }

    @GetMapping("/touch")
    public Mono<ResponseEntity<AuthMe>> touch(@AuthenticationPrincipal Jwt jwt,
                                              @RequestHeader(name = "X-App", required = false) String app) {
        return authzService.touchSession(jwt, app)
                .map(ResponseEntity::ok);
    }
}
