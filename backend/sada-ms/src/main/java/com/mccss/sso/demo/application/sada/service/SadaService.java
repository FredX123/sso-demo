package com.mccss.sso.demo.application.sada.service;

import com.mccss.sso.demo.commonlib.dto.MybAppDto;
import com.mccss.sso.demo.commonlib.dto.SadaAppDto;
import com.mccss.sso.demo.commonlib.integration.MybWebClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class SadaService {

    private final MybWebClient mybWebClient;

    public SadaAppDto whoami(String user, String accessToken) {
        return SadaAppDto.builder()
                .service("sada-ms")
                .message("Hello from SADA")
                .user(user)
                .accessToken(accessToken)
                .build();
    }

    public MybAppDto whoamiFromMyb() {
        return mybWebClient.whoamiFromMyb();
    }

    public MybAppDto whoamiFromMybNoToken() {
        return mybWebClient.callMybNoToken();
    }
}
