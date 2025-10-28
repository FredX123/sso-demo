package com.mccss.sso.demo.auth.controller;

import com.mccss.sso.demo.auth.service.AuthzService;
import com.mccss.sso.demo.commonlib.model.AuthMe;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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
    public Mono<ResponseEntity<AuthMe>> loadUserInfo(@AuthenticationPrincipal Jwt jwt) {
        log.info("Load and cache user authentication/authorization data");

        return authzService.loadUserInfo(jwt)
                .map(ResponseEntity::ok);
    }

    /**
     * Retrieves user authentication and authorization data from the cache.
     *
     * @return a {@code Mono} emitting a {@code ResponseEntity} containing an {@code AuthMe} object
     *         with the user's authentication and authorization details.
     */
    @GetMapping()
    public Mono<ResponseEntity<AuthMe>> getUserInfo(@AuthenticationPrincipal Jwt jwt) {
        log.info("Get user authentication/authorization data from cache");

        return authzService.getUserInfo(jwt)
                .map(ResponseEntity::ok);
    }

    @GetMapping("/me")
    public Map<String, Object> me(@AuthenticationPrincipal Jwt jwt) {
        Map<String, Object> m = new LinkedHashMap<>();
        if (jwt == null) {
            m.put("authenticated", false);
            return m;
        }
        m.put("authenticated", true);
        m.put("subject", jwt.getSubject());
        m.put("name", jwt.getClaimAsString("name"));
        m.put("email", jwt.getClaimAsString("email"));
        m.put("issuer", jwt.getIssuer() != null ? jwt.getIssuer().toString() : null);
        m.put("issuedAt", jwt.getIssuedAt());
        m.put("expiresAt", jwt.getExpiresAt());
        m.put("roles", Optional.ofNullable(jwt.getClaimAsStringList("groups")).orElse(List.of()));
        // Include all claims for debugging SSO (remove in prod if noisy)
        m.put("claims", jwt.getClaims());
        return m;
    }
}
