package com.mccss.sso.demo.commonlib.integration;

import com.mccss.sso.demo.commonlib.dto.SadaAppDto;
import com.mccss.sso.demo.commonlib.exception.ApplicationException;
import com.mccss.sso.demo.commonlib.util.SecurityUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Slf4j
@Service
public class SadaWebClient {
    @Value("${url.sada.api.hello:}")
    private String sadaHelloApi;

    private final SecurityUtil securityUtil;
    private final WebClient.Builder sadaWebClientBuilder;

    public SadaWebClient(@Qualifier("sadaWebClientBuilder") WebClient.Builder sadaWebClientBuilder,
                        SecurityUtil securityUtil) {
        this.sadaWebClientBuilder = sadaWebClientBuilder;
        this.securityUtil = securityUtil;
    }

    public SadaAppDto heloFromSada() {
        return sadaWebClientBuilder.build()
                .get()
                .uri(sadaHelloApi)
                .header(HttpHeaders.AUTHORIZATION, securityUtil.getAuthHeader())
                .retrieve()
                .onStatus(status -> status.value() == 401,
                        r -> Mono.error(new ApplicationException(
                                HttpStatus.UNAUTHORIZED.value(), "Unauthorized (401) from SADA API")))
                .bodyToMono(SadaAppDto.class)
                .block();
    }

    public SadaAppDto helloFromSadaNoToken() {
        return sadaWebClientBuilder.build()
                .get()
                .uri(sadaHelloApi)
                .retrieve()
                .bodyToMono(SadaAppDto.class)
                .block();
    }
}
