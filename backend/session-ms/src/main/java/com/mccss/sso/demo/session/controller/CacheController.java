package com.mccss.sso.demo.session.controller;


import com.mccss.sso.demo.commonlib.model.UserSession;
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
    @PostMapping("/user-session")
    public Mono<UserSession> cacheUserSession(@AuthenticationPrincipal Jwt jwt, @RequestBody UserSession request) {
        UserSession userSession = cacheService.cacheUserSession(jwt, request);
        return Mono.just(userSession);
    }

    /** Read current cached authz (also ensures cache is populated). */
    @GetMapping("/user-session")
    public Mono<UserSession> getUserSession(@AuthenticationPrincipal Jwt jwt,
                                            @RequestParam(value = "app", required = true) String app) {
        UserSession userSession = cacheService.getUserSession(jwt, app);
        return Mono.just(userSession);
    }
}
