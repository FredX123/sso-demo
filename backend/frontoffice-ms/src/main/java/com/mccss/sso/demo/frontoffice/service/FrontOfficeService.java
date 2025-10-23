package com.mccss.sso.demo.frontoffice.service;

import com.mccss.sso.demo.commonlib.dto.FoAppDto;
import com.mccss.sso.demo.commonlib.dto.BoAppDto;
import com.mccss.sso.demo.commonlib.integration.BackOfficeWebClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Slf4j
@RequiredArgsConstructor
@Service
public class FrontOfficeService {

    private final BackOfficeWebClient backOfficeWebClient;

    public Mono<FoAppDto> whoami(String user, String accessToken) {
        return Mono.fromSupplier(() -> FoAppDto.builder()
                .service("frontoffice-ms")
                .message("Hello from Frontoffice")
                .user(user)
                .accessToken(accessToken)
                .build());
    }

    public Mono<BoAppDto> whoamiFromBo() {
        return backOfficeWebClient.whoamiFromBo();
    }

    public Mono<BoAppDto> helloFromBoNoToken() {
        return backOfficeWebClient.callBoNoToken();
    }
}
