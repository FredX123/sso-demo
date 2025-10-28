package com.mccss.sso.demo.commonlib.integration;

import com.mccss.sso.demo.commonlib.config.IntegrationProps;
import com.mccss.sso.demo.commonlib.dto.FoAppDto;
import com.mccss.sso.demo.commonlib.exception.ApplicationException;
import com.mccss.sso.demo.commonlib.util.SecurityUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class FrontOfficeClient {

    private final SecurityUtil securityUtil;
    private final WebClient.Builder foClientBuilder;
    private final IntegrationProps integrationProps;

    public FrontOfficeClient(@Qualifier("foClientBuilder") WebClient.Builder foClientBuilder,
                             SecurityUtil securityUtil,
                             IntegrationProps integrationProps) {
        this.foClientBuilder = foClientBuilder;
        this.securityUtil = securityUtil;
        this.integrationProps = integrationProps;
    }

    public Mono<FoAppDto> whoamiFromFo() {
        return foClientBuilder.build()
                .get()
                .uri(getFoWhoAmIApi())
                .header(HttpHeaders.AUTHORIZATION, securityUtil.getAuthHeader())
                .retrieve()
                .bodyToMono(FoAppDto.class);
    }

    public Mono<FoAppDto> callFoNoToken() {
        return foClientBuilder.build()
                .get()
                .uri(getFoWhoAmIApi())
                .retrieve()
                .onStatus(status -> status.value() == HttpStatus.UNAUTHORIZED.value(),
                        r -> Mono.error(new ApplicationException(
                                HttpStatus.UNAUTHORIZED.value(), "Unauthorized (401) from Frontoffice API")))
                .bodyToMono(FoAppDto.class);
    }

    private String getFoWhoAmIApi() {
        return integrationProps.getFrontofficeMs().getBaseUrl() + "/whoami";
    }
}
