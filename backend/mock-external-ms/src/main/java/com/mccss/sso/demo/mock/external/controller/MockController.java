package com.mccss.sso.demo.mock.external.controller;

import com.mccss.sso.demo.commonlib.model.UserRoles;
import com.mccss.sso.demo.mock.external.service.MockService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/mock-external")
public class MockController {

    private final MockService mockService;

    @GetMapping("/user-roles/{sub}")
    public ResponseEntity<UserRoles> getUserRoles(@PathVariable("sub") String sub) {
        return ResponseEntity.ok(mockService.getRolesByUser(sub));
    }

}
