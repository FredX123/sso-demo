package com.mccss.sso.demo.commonlib.model;

/** A single decision */
public record Decision(
        String action,
        String resource,
        boolean allowed
) {}
