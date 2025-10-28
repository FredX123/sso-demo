package com.mccss.sso.demo.commonlib.model;

import lombok.*;

import java.time.Instant;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class AuthMe {

    private boolean authenticated;
    private String subject;
    private String firstName;
    private String lastName;
    private String email;
    private String issuer;
    private Instant issuedAt;
    private Instant expiresAt;

    private List<String> roles;
}
