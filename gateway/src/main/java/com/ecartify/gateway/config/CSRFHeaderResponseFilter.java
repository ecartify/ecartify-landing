package com.ecartify.gateway.config;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.filter.OncePerRequestFilter;

public class CSRFHeaderResponseFilter extends OncePerRequestFilter
{
    @Override
    protected void doFilterInternal(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse,
                                    FilterChain filterChain) throws ServletException, IOException
    {
        CsrfToken csrf = (CsrfToken) httpServletRequest.getAttribute(CsrfToken.class.getName());
        if (csrf != null)
        {
            Cookie cookie = new Cookie(ConfigConstants.CSRF_COOKIE_NAME, csrf.getToken());
            cookie.setPath("/");
            cookie.setSecure(true);
            httpServletResponse.addCookie(cookie);
            httpServletResponse.setHeader(ConfigConstants.CSRF_HEADER_NAME, csrf.getToken());
        }
        filterChain.doFilter(httpServletRequest, httpServletResponse);
    }
}
