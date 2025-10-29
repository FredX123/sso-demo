package com.mccss.sso.demo.commonlib.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Slf4j
@Component
public class SecurityUtil {

    /**
     * Get the Authorization header from the request headers
     * @return the Authorization header
     */
    public String getAuthHeader() {
        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attrs == null) return null;

        String authHeader = attrs.getRequest().getHeader(HttpHeaders.AUTHORIZATION);
        return authHeader == null || !authHeader.startsWith("Bearer ") ? null : authHeader;
    }


}
