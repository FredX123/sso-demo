package com.mccss.sso.demo.application.myb.controller;

import com.mccss.sso.demo.application.myb.service.MybService;
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
@RequestMapping("/api/myb")
public class MybController {

    private final MybService service;

    @GetMapping("/whoami")
    public ResponseEntity<MybAppDto> hello(@AuthenticationPrincipal Jwt jwt) {
        String user = jwt != null ? jwt.getSubject() : null;
        String accessToken = jwt != null ? jwt.getTokenValue() : null;
        return ResponseEntity.ok(service.whoami(user, accessToken));
    }

    @GetMapping("/whoami-from-sada")
    public ResponseEntity<SadaAppDto> whoamiFromSada() {
        return ResponseEntity.ok(service.whoamiFromSada());
    }

    @GetMapping("/whoami-from-sada-no-token")
    public ResponseEntity<SadaAppDto> helloFromMybNoToken() {
        return ResponseEntity.ok(service.helloFromMybNoToken());
    }
}
