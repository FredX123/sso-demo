package com.mccss.sso.demo.backoffice.controller;

import com.mccss.sso.demo.backoffice.service.BackofficeService;
import com.mccss.sso.demo.commonlib.dto.BoAppDto;
import com.mccss.sso.demo.commonlib.dto.FoAppDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/backoffice")
public class BackofficeController {

    private final BackofficeService service;

    @GetMapping("/whoami")
    public Mono<ResponseEntity<BoAppDto>> hello(@AuthenticationPrincipal Jwt jwt) {
        log.info("whoami");
        String user = jwt != null ? jwt.getSubject() : null;
        String accessToken = jwt != null ? jwt.getTokenValue() : null;

        return service.whoami(user, accessToken).map(ResponseEntity::ok);
    }

    @GetMapping("/whoami-from-fo")
    public Mono<ResponseEntity<FoAppDto>> whoamiFromFo() {
        log.info("whoami-from-fo");
        return service.whoamiFromFo().map(ResponseEntity::ok);
    }

    @GetMapping("/whoami-from-fo-no-token")
    public Mono<ResponseEntity<FoAppDto>> whoamiFromFoNoToken() {
        log.info("whoami-from-fo-no-token");
        return service.whoamiFromFoNoToken().map(ResponseEntity::ok);
    }
}

