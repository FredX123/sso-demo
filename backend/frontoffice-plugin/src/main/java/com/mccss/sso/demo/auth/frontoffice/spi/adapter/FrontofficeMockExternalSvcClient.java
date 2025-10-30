package com.mccss.sso.demo.auth.frontoffice.spi.adapter;

import com.mccss.sso.demo.auth.frontoffice.spi.config.FrontofficeMockExternalProps;
import com.mccss.sso.demo.commonlib.model.UserRoles;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class FrontofficeMockExternalSvcClient {

    private final WebClient mockExternalClient;

    public FrontofficeMockExternalSvcClient(FrontofficeMockExternalProps props) {
        this.mockExternalClient = WebClient.builder().baseUrl(props.getBaseUrl()).build();
    }

    public Mono<UserRoles> getUserRoles(String subject) {
        log.info("Getting user roles for subject: {} from Mock External Service", subject);
        return mockExternalClient
                .get()
                .uri("/user-roles/" + subject)
                .retrieve()
                .bodyToMono(UserRoles.class);
    }
}
