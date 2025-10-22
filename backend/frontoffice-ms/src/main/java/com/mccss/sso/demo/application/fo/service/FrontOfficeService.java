package com.mccss.sso.demo.application.fo.service;

import com.mccss.sso.demo.commonlib.dto.FoAppDto;
import com.mccss.sso.demo.commonlib.dto.BoAppDto;
import com.mccss.sso.demo.commonlib.integration.BackOfficeWebClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class FrontOfficeService {

    private final BackOfficeWebClient backOfficeWebClient;

    public FoAppDto whoami(String user, String accessToken) {
        return FoAppDto.builder()
                .service("frontoffice-ms")
                .message("Hello from Frontoffice")
                .user(user)
                .accessToken(accessToken)
                .build();
    }

    public BoAppDto whoamiFromBo() {
        return backOfficeWebClient.whoamiFromBo();
    }

    public BoAppDto helloFromBoNoToken() {
        return backOfficeWebClient.callBoNoToken();
    }
}
