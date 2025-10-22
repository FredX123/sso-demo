package com.mccss.sso.demo.commonlib.integration;

import com.mccss.sso.demo.commonlib.dto.FoAppDto;
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

    public FoAppDto whoamiFromFo() {
        return foWebClientBuilder.build()
                .get()
                .uri(foWhoAmIApi)
                .header(HttpHeaders.AUTHORIZATION, securityUtil.getAuthHeader())
                .retrieve()
                .bodyToMono(FoAppDto.class)
                .block();
    }

    public FoAppDto callFoNoToken() {
        return foWebClientBuilder.build()
                .get()
                .uri(foWhoAmIApi)
                .retrieve()
                .onStatus(status -> status.value() == 401,
                        r -> Mono.error(new ApplicationException(
                                HttpStatus.UNAUTHORIZED.value(), "Unauthorized (401) from Frontoffice API")))
                .bodyToMono(FoAppDto.class)
                .block();
    }
}
