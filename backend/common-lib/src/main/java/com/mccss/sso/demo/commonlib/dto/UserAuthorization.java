package com.mccss.sso.demo.commonlib.dto;

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
