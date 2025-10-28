package com.mccss.sso.demo.permission.service;

import com.mccss.sso.demo.commonlib.model.PermissionSet;
import com.mccss.sso.demo.permission.config.MockPermissions;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@Service
public class PermissionService {

    private final MockPermissions mockPermissions;

    private Map<String, PermissionSet> byApp;

    @PostConstruct
    void init() {
        Map<String, PermissionSet> tmp = new HashMap<>();
        if (mockPermissions.getPermissions() != null) {
            for (PermissionSet ps : mockPermissions.getPermissions()) {
                if (ps.app() != null) {
                    tmp.put(ps.app().toLowerCase(), ps);
                }
            }
        }
        byApp = Collections.unmodifiableMap(tmp);
    }

    public PermissionSet findByApp(String appKey) {
        if (appKey == null) return null;
        return byApp.get(appKey.toLowerCase());
    }

    public List<PermissionSet.Rule> rulesFor(String appKey) {
        PermissionSet ps = findByApp(appKey);
        return ps == null ? List.of() : ps.rules();
    }
}
