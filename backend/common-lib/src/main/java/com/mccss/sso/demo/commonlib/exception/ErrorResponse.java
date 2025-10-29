package com.mccss.sso.demo.commonlib.exception;

import java.time.Instant;

public record ErrorResponse(
        int statusCode,
        String error,
        Instant timestamp
) {}
