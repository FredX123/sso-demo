package com.mccss.sso.demo.commonlib.dto;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MybAppDto {
    private String service;
    private String message;
    private String user;
}
