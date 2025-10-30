package com.mccss.sso.demo.commonlib.model;

import java.util.List;

public record UserRoles(
        String subject,
        List<String> roles
) { }
