package com.mccss.sso.demo.application.sada.controller;

import com.mccss.sso.demo.application.sada.service.SadaService;
import com.mccss.sso.demo.commonlib.dto.MybAppDto;
import com.mccss.sso.demo.commonlib.dto.SadaAppDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/sada")
public class SadaController {

    private final SadaService service;

    @GetMapping("/whoami")
    public ResponseEntity<SadaAppDto> hello(@AuthenticationPrincipal Jwt jwt) {
        String user = jwt != null ? jwt.getSubject() : null;
        String accessToken = jwt != null ? jwt.getTokenValue() : null;

        return ResponseEntity.ok(service.whoami(user, accessToken));
    }

    @GetMapping("/whoami-from-myb")
    public ResponseEntity<MybAppDto> whoamiFromMyb() {
        return ResponseEntity.ok(service.whoamiFromMyb());
    }

    @GetMapping("/whoami-from-myb-no-token")
    public ResponseEntity<MybAppDto> whoamiFromMybNoToken() {
        return ResponseEntity.ok(service.whoamiFromMybNoToken());
    }
}

