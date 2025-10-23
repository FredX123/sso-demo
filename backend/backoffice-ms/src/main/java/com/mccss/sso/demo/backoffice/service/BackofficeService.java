package com.mccss.sso.demo.backoffice.service;

import com.mccss.sso.demo.commonlib.dto.FoAppDto;
import com.mccss.sso.demo.commonlib.dto.BoAppDto;
import com.mccss.sso.demo.commonlib.integration.FrontOfficeWebClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class BackofficeService {

    private final FrontOfficeWebClient frontOfficeWebClient;

    public BoAppDto whoami(String user, String accessToken) {
        return BoAppDto.builder()
                .service("backoffice-ms")
                .message("Hello from Backoffice")
                .user(user)
                .accessToken(accessToken)
                .build();
    }

    public FoAppDto whoamiFromFo() {
        return frontOfficeWebClient.whoamiFromFo();
    }

    public FoAppDto whoamiFromFoNoToken() {
        return frontOfficeWebClient.callFoNoToken();
    }
}
