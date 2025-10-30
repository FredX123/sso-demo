package com.mccss.sso.demo.commonlib.spi;

import com.mccss.sso.demo.commonlib.model.AuthorizationBundle;
import com.mccss.sso.demo.commonlib.model.UserRoles;
import reactor.core.publisher.Mono;

import java.util.Map;

public interface AppAuthAdapter {

    /**
     * Returns the unique application key associated with the adapter.
     * The application key is used to identify the type of application integration
     * (e.g., "frontoffice", "backoffice") this adapter serves.
     *
     * @return a string representing the unique application key for the adapter
     */
    String appKey();

    Mono<AuthorizationBundle> buildDecisions(String issuer, String subject);

    Mono<UserRoles> getUserRoles(String sub);

    Mono<Map<String, Object>> getAttributes(String sub);
}
