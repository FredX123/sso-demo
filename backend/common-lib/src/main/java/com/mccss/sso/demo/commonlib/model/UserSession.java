package com.mccss.sso.demo.commonlib.model;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class UserSession {
    private AuthMe authMe;
    private AuthorizationBundle authz;
}
