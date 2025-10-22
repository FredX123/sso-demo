package com.mccss.sso.demo.application.bo.controller;

import com.mccss.sso.demo.application.bo.service.BackofficeService;
import com.mccss.sso.demo.commonlib.dto.FoAppDto;
import com.mccss.sso.demo.commonlib.dto.BoAppDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/backoffice")
public class BackofficeController {

    private final BackofficeService service;

    @GetMapping("/whoami")
    public ResponseEntity<BoAppDto> hello(@AuthenticationPrincipal Jwt jwt) {
        String user = jwt != null ? jwt.getSubject() : null;
        String accessToken = jwt != null ? jwt.getTokenValue() : null;

        return ResponseEntity.ok(service.whoami(user, accessToken));
    }

    @GetMapping("/whoami-from-fo")
    public ResponseEntity<FoAppDto> whoamiFromFo() {
        return ResponseEntity.ok(service.whoamiFromFo());
    }

    @GetMapping("/whoami-from-fo-no-token")
    public ResponseEntity<FoAppDto> whoamiFromFoNoToken() {
        return ResponseEntity.ok(service.whoamiFromFoNoToken());
    }
}

