package com.mccss.sso.demo.session.cache;

import com.mccss.sso.demo.session.config.AuthzCacheProps;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

/**
 * This class is responsible for generating cache keys for user authorization data.
 * It supports optional hashing of keys using the SHA-256 algorithm.
 * The cache keys are derived from the issuer and subject identifiers.
 */
@Component
public class AuthzCacheKey {
    private final boolean hash;

    public AuthzCacheKey(AuthzCacheProps authzCacheProps) {
        this.hash = authzCacheProps.isHashKey();
    }

    /**
     * Generates a cache key based on the provided issuer (`iss`) and subject (`sub`).
     * If hashing is enabled, the raw key is hashed using SHA-256.
     *
     * @param iss the issuer identifier, representing the origin of the token
     * @param sub the subject identifier, representing the user or entity for which
     *            the cache key is being generated
     * @return a string representing the generated cache key; if hashing is enabled,
     *         the SHA-256 hash of the raw key is returned, otherwise the raw key is returned
     */
    public String forIssSub(String iss, String sub) {
        String raw = "roles:" + iss + ":" + sub;
        if (!hash) return raw;
        return "roles:" + sha256(raw);
    }

    /**
     * Computes the SHA-256 hash of the provided input string and returns the hash as a hexadecimal string.
     *
     * @param input the input string to be hashed
     * @return the SHA-256 hash of the input string represented as a hexadecimal string
     * @throws IllegalStateException if the hashing process encounters an error
     */
    private static String sha256(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] d = md.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : d) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }
}
