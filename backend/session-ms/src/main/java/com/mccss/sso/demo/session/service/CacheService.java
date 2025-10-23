package com.mccss.sso.demo.session.service;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.mccss.sso.demo.commonlib.dto.AuthMe;
import com.mccss.sso.demo.session.cache.AuthzCacheKey;
import com.mccss.sso.demo.session.config.AuthzCacheProps;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import java.util.Collections;

@RequiredArgsConstructor
@Service
public class CacheService {

    private final StringRedisTemplate redis;
    private final ObjectMapper mapper;
    private final AuthzCacheKey keyHelper;
    private final AuthzCacheProps authzCacheProps;

    /**
     * Caches user authorization data
     *
     * @param jwt  the JSON Web Token used to identify the user and their context; the issuer and subject
     *             are extracted for use as cache keys
     * @param data the user authorization data, containing roles and permissions, to be cached
     */
    public AuthMe cacheAuthz(Jwt jwt, AuthMe data) {
        String iss = jwt.getIssuer() != null ? jwt.getIssuer().toString() : "unknown";
        String sub = jwt.getSubject();

        this.put(iss, sub, data);

        return data;
    }

    /**
     * Retrieves the user authorization data associated with the given JSON Web Token (JWT)
     * by extracting the issuer (`iss`) and subject (`sub`) from the token and using them
     * to query the cache.
     *
     * @param jwt the JSON Web Token that contains the issuer and subject information,
     *            used to locate the corresponding authorization data in the cache
     * @return the user authorization data if it exists and can be successfully resolved,
     *         or null if no corresponding data is found or if the data is invalid
     */
    public AuthMe findAuthz(Jwt jwt) {
        String iss = jwt.getIssuer() != null ? jwt.getIssuer().toString() : "unknown";
        String sub = jwt.getSubject();

        return this.get(iss, sub);
    }

    /**
     * Retrieves a user authorization record from the cache, if available. The record
     * is identified using a combination of the issuer (`iss`) and subject (`sub`).
     * The method handles cases where the data is unavailable or corrupted by returning
     * `null` and cleaning up invalid cache entries.
     *
     * @param iss the issuer identifier, representing the origin of the token
     * @param sub the subject identifier, representing the user or entity for which
     *            the authorization is being retrieved
     * @return the user authorization data if it exists in the cache and is unmarshalled
     *         successfully, or `null` if no valid data is available
     */
    public AuthMe get(String iss, String sub) {
        String key = keyHelper.forIssSub(iss, sub);
        String json = redis.opsForValue().get(key);
        if (json == null) return null;
        try {
            return mapper.readValue(json, AuthMe.class);
        } catch (Exception e) {
            // corrupted entry: evict and miss
            redis.delete(key);
            return null;
        }
    }

    /**
     * Stores user authorization data in the cache. The data is serialized to JSON and
     * saved using a key derived from the issuer (`iss`) and subject (`sub`).
     *
     * @param iss  the issuer identifier, representing the origin of the token
     * @param sub  the subject identifier, representing the user or entity for which
     *             the authorization data is being stored
     * @param data the user authorization data, containing roles and permissions,
     *             that needs to be cached
     * @throws RuntimeException if the data cannot be serialized to JSON
     */
    public void put(String iss, String sub, AuthMe data) {
        String key = keyHelper.forIssSub(iss, sub);
        try {
            String json = mapper.writeValueAsString(data);
            redis.opsForValue().set(key, json, authzCacheProps.getTtl());
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize authz data", e);
        }
    }

    /**
     * Removes the cached user authorization data associated with the specified issuer (`iss`)
     * and subject (`sub`) from the cache.
     *
     * @param iss the issuer identifier, representing the origin of the token
     * @param sub the subject identifier, representing the user or entity for which
     *            the authorization cache entry is being removed
     * @return true if the cache entry was successfully removed, false otherwise
     */
    public boolean evict(String iss, String sub) {
        String key = keyHelper.forIssSub(iss, sub);
        Long n = redis.delete(Collections.singleton(key));
        return n > 0;
    }
}
