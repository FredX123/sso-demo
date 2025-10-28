package com.mccss.sso.demo.commonlib.integration;

import com.mccss.sso.demo.commonlib.config.IntegrationProps;
import com.mccss.sso.demo.commonlib.dto.BoAppDto;
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
public class BackOfficeClient {

    private final SecurityUtil securityUtil;
    private final WebClient.Builder boClientBuilder;
    private final IntegrationProps integrationProps;

    public BackOfficeClient(@Qualifier("boClientBuilder") WebClient.Builder boClientBuilder,
                            SecurityUtil securityUtil,
                            IntegrationProps integrationProps) {
        this.boClientBuilder = boClientBuilder;
        this.securityUtil = securityUtil;
        this.integrationProps = integrationProps;
    }

    public Mono<BoAppDto> whoamiFromBo() {
        return boClientBuilder.build()
                .get()
                .uri(getBoWhoAmIApi())
                .header(HttpHeaders.AUTHORIZATION, securityUtil.getAuthHeader())
                .retrieve()
                .onStatus(status -> status.value() == HttpStatus.UNAUTHORIZED.value(),
                        r -> Mono.error(new ApplicationException(
                                HttpStatus.UNAUTHORIZED.value(), "Unauthorized (401) from Backoffice API")))
                .bodyToMono(BoAppDto.class);
    }

    public Mono<BoAppDto> callBoNoToken() {
        return boClientBuilder.build()
                .get()
                .uri(getBoWhoAmIApi())
                .retrieve()
                .bodyToMono(BoAppDto.class);
    }

    private String getBoWhoAmIApi() {
        return integrationProps.getBackofficeMs().getBaseUrl() + "/whoami";
    }
}
