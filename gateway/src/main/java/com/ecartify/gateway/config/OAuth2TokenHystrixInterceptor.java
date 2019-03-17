package com.ecartify.gateway.config;

import java.io.IOException;

import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;

import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;

@Component
public class OAuth2TokenHystrixInterceptor implements ClientHttpRequestInterceptor
{

    @Override
    @HystrixCommand(groupKey = "OAUTH2",commandKey = "TOKEN_REQUEST")
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException
    {
        return execution.execute(request,body);
    }


}
