package com.mccss.sso.demo.commonlib.model;

import java.util.List;

/** User roles returned by mock-external-ms */
public record UserRoles(
        String subject,
        List<String> roles
) { }
