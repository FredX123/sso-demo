package com.mccss.sso.demo.permission.controller;

import com.mccss.sso.demo.commonlib.model.PermissionSet;
import com.mccss.sso.demo.permission.service.PermissionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/permission")
public class PermissionController {

    private final PermissionService service;

    @GetMapping("/{app}")
    public  Mono<ResponseEntity<PermissionSet>> findPermissionsByApp(@PathVariable("app") String app) {
        log.info("get permission for app {}", app);

        return Mono.fromCallable(() -> service.findByApp(app))
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }
}
