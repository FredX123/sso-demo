package com.mccss.sso.demo.auth.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

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
