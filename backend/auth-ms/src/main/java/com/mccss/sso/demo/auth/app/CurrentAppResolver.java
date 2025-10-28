package com.mccss.sso.demo.auth.app;

import com.mccss.sso.demo.auth.config.AppClientsProps;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;

@Component
public class CurrentAppResolver {

    private final Map<String, AppClientsProps.Client> clients;

    public CurrentAppResolver(AppClientsProps props) {
        this.clients = props.getClients();
    }

    /**
     * Resolve appKey from JWT's client id (cid or azp)
     */
    public String resolveApp(Jwt jwt) {
        String clientId = Optional.ofNullable(jwt.getClaimAsString("cid"))
                .orElse(jwt.getClaimAsString("azp"));
        if (clientId == null) return null;

        return clients.entrySet().stream()
                .filter(e -> clientId.equals(e.getValue().getClientId()))
                .map(Map.Entry::getKey)
                .findFirst().orElse(null);
    }
}
