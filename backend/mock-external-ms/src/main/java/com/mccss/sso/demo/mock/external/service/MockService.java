package com.mccss.sso.demo.mock.external.service;

import com.mccss.sso.demo.commonlib.model.UserRoles;
import com.mccss.sso.demo.mock.external.config.MockUsers;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class MockService {

    private final MockUsers mockUsers;

    public UserRoles getRolesByUser(String sub) {
        return mockUsers.getUsers().stream()
                .filter(ur -> sub.equals(ur.subject()))
                .findFirst()
                .orElse(new UserRoles(sub, List.of("USER")));
    }
}
