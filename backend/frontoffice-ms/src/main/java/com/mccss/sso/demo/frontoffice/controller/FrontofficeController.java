package com.mccss.sso.demo.frontoffice.controller;

import com.mccss.sso.demo.commonlib.dto.BoAppDto;
import com.mccss.sso.demo.commonlib.dto.FoAppDto;
import com.mccss.sso.demo.frontoffice.service.FrontofficeService;
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
@RequestMapping("/api/frontoffice")
public class FrontofficeController {

    private final FrontofficeService service;

    @GetMapping("/whoami")
    public Mono<ResponseEntity<FoAppDto>> whoami(@AuthenticationPrincipal Jwt jwt) {
        log.info("whoami");
        String user = jwt != null ? jwt.getSubject() : null;
        String accessToken = jwt != null ? jwt.getTokenValue() : null;

        return service.whoami(user, accessToken).map(ResponseEntity::ok);
    }

    @GetMapping("/whoami-from-bo")
    public Mono<ResponseEntity<BoAppDto>> whoamiFromBo() {
        log.info("whoami-from-bo");
        return service.whoamiFromBo().map(ResponseEntity::ok);
    }

    @GetMapping("/whoami-from-bo-no-token")
    public Mono<ResponseEntity<BoAppDto>> helloFromBoNoToken() {
        log.info("whoami-from-bo-no-token");
        return service.helloFromBoNoToken().map(ResponseEntity::ok);
    }
}
