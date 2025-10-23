package com.mccss.sso.demo.auth.controller;

import com.mccss.sso.demo.auth.service.AuthzService;
import com.mccss.sso.demo.commonlib.dto.AuthMe;
import lombok.RequiredArgsConstructor;
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

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/auth/authorizations")
public class AuthController {

    private final AuthzService authzService;

    /**
     * Load the user authorization data and cache it.
     *
     * @return a {@code Mono} emitting a {@code ResponseEntity} containing the {@code UserAuthorization} object
     *         with the user's roles and permissions after caching the authorization data.
     */
    @GetMapping("/load")
    public Mono<ResponseEntity<AuthMe>> loadAndCacheAuthz(@AuthenticationPrincipal Jwt jwt) {
        return authzService.cacheAuthz(jwt)
                .map(ResponseEntity::ok);
    }

    /**
     * Retrieves the currently authorized user's roles and permissions.
     *
     * @return a {@code Mono} emitting a {@code ResponseEntity} containing the {@code UserAuthorization} object
     *         with the user's roles and permissions retrieved from the authorization cache.
     */
    @GetMapping()
    public Mono<ResponseEntity<AuthMe>> getAuthorizations() {
        return authzService.getAuthz()
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
