package com.mccss.sso.demo.commonlib.model;

import lombok.*;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class UserAuthorization {
    private List<String> roles;
    private List<String> permissions;
}
