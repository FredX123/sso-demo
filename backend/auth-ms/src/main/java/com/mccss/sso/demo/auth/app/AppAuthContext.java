package com.mccss.sso.demo.auth.app;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class AppAuthContext {

    public String iss;
    public String sub;
    public List<String> roles = List.of();
    public List<String> permissions = List.of();

}
