package com.mccss.sso.demo.session.controller;


import com.mccss.sso.demo.commonlib.dto.AuthMe;
import com.mccss.sso.demo.session.service.CacheService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/session/cache")
public class CacheController {

    private final CacheService cacheService;

    /** Call this once after login (or lazily when roles first needed). */
    @PostMapping("/authorizations")
    public Mono<AuthMe> bootstrap(@AuthenticationPrincipal Jwt jwt, @RequestBody AuthMe authz) {
        AuthMe me = cacheService.cacheAuthz(jwt, authz);
        return Mono.just(me);
    }

    /** Read current cached authz (also ensures cache is populated). */
    @GetMapping("/authorizations")
    public Mono<AuthMe> authorizations(@AuthenticationPrincipal Jwt jwt) {
        AuthMe me = cacheService.findAuthz(jwt);
        return Mono.just(me);
    }
}
