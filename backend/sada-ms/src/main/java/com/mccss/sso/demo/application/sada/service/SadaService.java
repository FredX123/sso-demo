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

    public SadaAppDto hello(String user) {
        return SadaAppDto.builder()
                .service("sada-ms")
                .message("Hello from SADA")
                .user(user)
                .build();
    }

    public MybAppDto helloFromMyb() {
        return mybWebClient.heloFromMyb();
    }

    public MybAppDto helloFromMybNoToken() {
        return mybWebClient.heloFromMybNoToken();
    }
}
