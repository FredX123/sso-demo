package com.mccss.sso.demo.frontoffice.service;

import com.mccss.sso.demo.commonlib.dto.FoAppDto;
import com.mccss.sso.demo.commonlib.dto.BoAppDto;
import com.mccss.sso.demo.commonlib.integration.BackOfficeClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Slf4j
@RequiredArgsConstructor
@Service
public class FrontOfficeService {

    private final BackOfficeClient backOfficeClient;

    public Mono<FoAppDto> whoami(String user, String accessToken) {
        return Mono.fromSupplier(() -> FoAppDto.builder()
                .service("frontoffice-ms")
                .message("Hello from Frontoffice")
                .user(user)
                .accessToken(accessToken)
                .build());
    }

    public Mono<BoAppDto> whoamiFromBo() {
        return backOfficeClient.whoamiFromBo();
    }

    public Mono<BoAppDto> helloFromBoNoToken() {
        return backOfficeClient.callBoNoToken();
    }
}
