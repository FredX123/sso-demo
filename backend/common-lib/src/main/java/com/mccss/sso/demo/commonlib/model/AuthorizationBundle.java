package com.mccss.sso.demo.commonlib.model;

import java.util.List;

/** Bundle cached in session-ms */
public record AuthorizationBundle(
        String iss,
        String sub,
        String app,
        List<Decision> decisions,
        List<String> roles,
        int ttlSec
) {}
