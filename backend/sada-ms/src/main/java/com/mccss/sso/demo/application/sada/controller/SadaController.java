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

    @GetMapping("/hello")
    public ResponseEntity<SadaAppDto> hello(@AuthenticationPrincipal Jwt jwt) {
        String user = jwt != null ? jwt.getSubject() : null;
        return ResponseEntity.ok(service.hello(user));
    }

    @GetMapping("/hello-from-myb")
    public ResponseEntity<MybAppDto> helloFromMyb() {
        return ResponseEntity.ok(service.helloFromMyb());
    }

    @GetMapping("/hello-from-myb-no-token")
    public ResponseEntity<MybAppDto> helloFromMybNoToken() {
        return ResponseEntity.ok(service.helloFromMybNoToken());
    }
}

