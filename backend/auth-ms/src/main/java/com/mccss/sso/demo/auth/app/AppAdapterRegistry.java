package com.mccss.sso.demo.auth.app;

import com.mccss.sso.demo.commonlib.spi.AppAuthAdapter;
import com.mccss.sso.demo.commonlib.spi.AppKey;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A registry for managing and providing {@link AppAuthAdapter} instances in the application.
 * The registry maps adapters to their corresponding application keys for efficient retrieval and usage.
 *
 * This class is designed to support multiple application integrations, where each integration may have
 * specific authentication and authorization requirements encapsulated in its respective adapter.
 *
 * The registry is initialized by registering all available {@link AppAuthAdapter} beans, using their
 * unique application keys as identifiers.
 *
 * Thread-safety: Instances of this class are expected to be used as singletons within the Spring context
 * and are immutable after initialization.
 */
@Component
public class AppAdapterRegistry {

    private final Map<String, AppAuthAdapter> adapters = new HashMap<>();

    public AppAdapterRegistry(List<AppAuthAdapter> discovered) {
        // Each plugin will annotate with @Component and maybe @Qualifier("frontoffice")
        for (AppAuthAdapter adapter : discovered) {
            String appKey = resolveAppKey(adapter);
            adapters.put(appKey, adapter);
        }
    }

    /**
     * Retrieves the {@link AppAuthAdapter} associated with the given application key.
     * The returned adapter can be used to handle authentication and authorization
     * specifically for the requested application.
     *
     * @param app the unique application key for which to retrieve the adapter
     * @return the {@link AppAuthAdapter} instance linked to the provided application key,
     *         or null if no adapter is found for the specified key
     */
    public AppAuthAdapter get(String app) {
        return adapters.get(app);
    }

    private String resolveAppKey(AppAuthAdapter adapter) {
        // simplest: adapter class has @AppKey("frontoffice")
        AppKey ann = adapter.getClass().getAnnotation(AppKey.class);
        if (ann != null) return ann.value();
        // fallback: bean name, or throw
        throw new IllegalStateException("Adapter " + adapter + " has no @AppKey");
    }
}
