package com.mccss.sso.demo.application.sada.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/sada")
public class SadaController {

    @GetMapping("/hello")
    public Map<String, Object> hello(@AuthenticationPrincipal Jwt jwt) {
        return Map.of(
                "service", "sada-ms",
                "message", "Hello from SADA",
                "user", jwt != null ? jwt.getSubject() : null
        );
    }
}

