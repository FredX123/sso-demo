package com.mccss.sso.demo.frontoffice.controller;

import com.mccss.sso.demo.frontoffice.service.FrontOfficeService;
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
@RequestMapping("/api/frontoffice")
public class FrontOfficeController {

    private final FrontOfficeService service;

    @GetMapping("/whoami")
    public ResponseEntity<FoAppDto> hello(@AuthenticationPrincipal Jwt jwt) {
        String user = jwt != null ? jwt.getSubject() : null;
        String accessToken = jwt != null ? jwt.getTokenValue() : null;
        return ResponseEntity.ok(service.whoami(user, accessToken));
    }

    @GetMapping("/whoami-from-bo")
    public ResponseEntity<BoAppDto> whoamiFromBo() {
        return ResponseEntity.ok(service.whoamiFromBo());
    }

    @GetMapping("/whoami-from-bo-no-token")
    public ResponseEntity<BoAppDto> helloFromBoNoToken() {
        return ResponseEntity.ok(service.helloFromBoNoToken());
    }
}
