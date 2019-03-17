package com.ecartify.gateway.config;

import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.oauth2.client.OAuth2SsoProperties;
import org.springframework.http.HttpMethod;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.stereotype.Component;

public class CSRFRequestMatcher implements RequestMatcher
{


    // Always allow the HTTP GET method
    private final Pattern allowedMethods;

    // Disable CSFR protection on the following urls:
    private final AntPathRequestMatcher[] requestMatchers;

    public CSRFRequestMatcher(OAuth2SsoProperties ssoProperties,String[] authFreeEndpoints)
    {

        allowedMethods = Pattern.compile("^(GET|HEAD|OPTIONS|TRACE)$");
        requestMatchers = new AntPathRequestMatcher[authFreeEndpoints.length+1];
        requestMatchers[0]=new AntPathRequestMatcher(ssoProperties.getLoginPath(),
                HttpMethod.POST.name());
        for(int i=0;i<authFreeEndpoints.length;i++){
            requestMatchers[i+1]=new AntPathRequestMatcher(authFreeEndpoints[i],
                    HttpMethod.POST.name());
        }
    }

    @Override
    public boolean matches(HttpServletRequest httpServletRequest)
    {
        if (allowedMethods.matcher(httpServletRequest.getMethod()).matches()) {
            return false;
        }

        for (AntPathRequestMatcher matcher : requestMatchers) {
            if (matcher.matches(httpServletRequest)) {
                return false;
            }
        }
        return true;
    }
}
