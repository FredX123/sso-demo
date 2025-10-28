package com.mccss.sso.demo.commonlib.model;

import java.util.List;

/** Permission set returned by permission-ms */
public record PermissionSet(
        String app,
        List<Rule> rules)
{

    public record Rule(
            String action,
            String resource,
            List<String> roles
    ) {}
}
