package com.mccss.sso.demo.auth.app;

import com.mccss.sso.demo.commonlib.model.AuthorizationBundle;
import com.mccss.sso.demo.commonlib.model.DecisionResponse;
import org.springframework.security.oauth2.jwt.Jwt;
import reactor.core.publisher.Mono;

public interface AppAuthAdapter {

    /**
     * Returns the unique application key associated with the adapter.
     * The application key is used to identify the type of application integration
     * (e.g., "frontoffice", "backoffice") this adapter serves.
     *
     * @return a string representing the unique application key for the adapter
     */
    String appKey();

    Mono<AuthorizationBundle> buildDecisions(Jwt jwt);
}
