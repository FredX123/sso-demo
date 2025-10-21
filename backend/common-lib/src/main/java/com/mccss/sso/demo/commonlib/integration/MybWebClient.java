package com.mccss.sso.demo.commonlib.integration;

import com.mccss.sso.demo.commonlib.dto.MybAppDto;
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
public class MybWebClient {

    @Value("${url.myb.api.whoami:}")
    private String mybWhoAmIApi;

    private final SecurityUtil securityUtil;
    private final WebClient.Builder mybWebClientBuilder;

    public MybWebClient(@Qualifier("mybWebClientBuilder") WebClient.Builder mybWebClientBuilder,
                        SecurityUtil securityUtil) {
        this.mybWebClientBuilder = mybWebClientBuilder;
        this.securityUtil = securityUtil;
    }

    public MybAppDto whoamiFromMyb() {
        return mybWebClientBuilder.build()
                .get()
                .uri(mybWhoAmIApi)
                .header(HttpHeaders.AUTHORIZATION, securityUtil.getAuthHeader())
                .retrieve()
                .bodyToMono(MybAppDto.class)
                .block();
    }

    public MybAppDto callMybNoToken() {
        return mybWebClientBuilder.build()
                .get()
                .uri(mybWhoAmIApi)
                .retrieve()
                .onStatus(status -> status.value() == 401,
                        r -> Mono.error(new ApplicationException(
                                HttpStatus.UNAUTHORIZED.value(), "Unauthorized (401) from MyB API")))
                .bodyToMono(MybAppDto.class)
                .block();
    }
}
