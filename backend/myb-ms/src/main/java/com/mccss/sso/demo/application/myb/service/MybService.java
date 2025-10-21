package com.mccss.sso.demo.application.myb.service;

import com.mccss.sso.demo.commonlib.dto.MybAppDto;
import com.mccss.sso.demo.commonlib.dto.SadaAppDto;
import com.mccss.sso.demo.commonlib.integration.SadaWebClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class MybService {

    private final SadaWebClient sadaWebClient;

    public MybAppDto whoami(String user, String accessToken) {
        return MybAppDto.builder()
                .service("myb-ms")
                .message("Hello from MyB")
                .user(user)
                .accessToken(accessToken)
                .build();
    }

    public SadaAppDto whoamiFromSada() {
        return sadaWebClient.whoamiFromSada();
    }

    public SadaAppDto helloFromMybNoToken() {
        return sadaWebClient.callSadaNoToken();
    }
}
