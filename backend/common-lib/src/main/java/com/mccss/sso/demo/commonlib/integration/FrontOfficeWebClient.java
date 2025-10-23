package com.mccss.sso.demo.commonlib.integration;

import com.mccss.sso.demo.commonlib.dto.FoAppDto;
import com.mccss.sso.demo.commonlib.exception.ApplicationException;
import com.mccss.sso.demo.commonlib.util.SecurityUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class FrontOfficeWebClient {

    @Value("${url.frontoffice.api.whoami:}")
    private String foWhoAmIApi;

    private final SecurityUtil securityUtil;
    private final WebClient.Builder foWebClientBuilder;

    public FrontOfficeWebClient(@Qualifier("foWebClientBuilder") WebClient.Builder foWebClientBuilder,
                                SecurityUtil securityUtil) {
        this.foWebClientBuilder = foWebClientBuilder;
        this.securityUtil = securityUtil;
    }

    public Mono<FoAppDto> whoamiFromFo() {
        return foWebClientBuilder.build()
                .get()
                .uri(foWhoAmIApi)
                .header(HttpHeaders.AUTHORIZATION, securityUtil.getAuthHeader())
                .retrieve()
                .bodyToMono(FoAppDto.class);
    }

    public Mono<FoAppDto> callFoNoToken() {
        return foWebClientBuilder.build()
                .get()
                .uri(foWhoAmIApi)
                .retrieve()
                .onStatus(status -> status.value() == HttpStatus.UNAUTHORIZED.value(),
                        r -> Mono.error(new ApplicationException(
                                HttpStatus.UNAUTHORIZED.value(), "Unauthorized (401) from Frontoffice API")))
                .bodyToMono(FoAppDto.class);
    }
}
