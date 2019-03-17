package com.ecartify.gateway.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.security.oauth2.client.OAuth2SsoProperties;
import org.springframework.boot.autoconfigure.security.oauth2.resource.ResourceServerProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.client.loadbalancer.LoadBalancerInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.annotation.web.configurers.ExceptionHandlingConfigurer;
import org.springframework.security.oauth2.client.OAuth2ClientContext;
import org.springframework.security.oauth2.client.OAuth2RestOperations;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.security.oauth2.client.filter.OAuth2ClientAuthenticationProcessingFilter;
import org.springframework.security.oauth2.client.token.AccessTokenProviderChain;
import org.springframework.security.oauth2.client.token.grant.password.ResourceOwnerPasswordAccessTokenProvider;
import org.springframework.security.oauth2.client.token.grant.password.ResourceOwnerPasswordResourceDetails;
import org.springframework.security.oauth2.provider.token.DefaultTokenServices;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;
import org.springframework.security.oauth2.provider.token.store.JwtTokenStore;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.security.web.csrf.CsrfFilter;
import org.springframework.security.web.csrf.CsrfTokenRepository;
import org.springframework.security.web.csrf.HttpSessionCsrfTokenRepository;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.MediaTypeRequestMatcher;
import org.springframework.security.web.util.matcher.RequestHeaderRequestMatcher;
import org.springframework.web.accept.ContentNegotiationStrategy;
import org.springframework.web.accept.HeaderContentNegotiationStrategy;

import javax.servlet.Filter;

import java.util.Collections;

@Configuration
public class SecurityConfig extends WebSecurityConfigurerAdapter
{

    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @Autowired
    private OAuth2ClientContext oauth2ClientContext;

    @Value("${zuul.auth-free-endpoints}")
    private String[] authFreeEndPoints;

    @Autowired
    private LoadBalancerInterceptor loadBalancerInterceptor;

    @Bean
    @ConfigurationProperties(prefix = "security.oauth2.sso")
    public OAuth2SsoProperties sso()
    {
        return new OAuth2SsoProperties();
    }

    @Bean
    @ConfigurationProperties(prefix = "security.oauth2.resource")
    public ResourceServerProperties resourceDetails()
    {
        return new ResourceServerProperties();
    }

    @Bean
    @ConfigurationProperties("security.oauth2.client")
    public ResourceOwnerPasswordResourceDetails clientDetails()
    {
        return new ResourceOwnerPasswordResourceDetails();
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception
    {
        http.authorizeRequests()
                .antMatchers(sso().getLoginPath())
                .permitAll()//permit all request to login path
                .antMatchers(authFreeEndPoints)
                .permitAll() // allow all auth free endpoints
                .anyRequest()
                .authenticated()//any other request is authenticated
                .and()
                .httpBasic().disable()
                .csrf().requireCsrfProtectionMatcher(new CSRFRequestMatcher(sso(),authFreeEndPoints))
                .csrfTokenRepository(csrfTokenRepository())
                .and()
                .addFilterBefore(ssoFilter(), BasicAuthenticationFilter.class)
                .addFilterAfter(new CSRFHeaderResponseFilter(), CsrfFilter.class)
                .logout()
                .permitAll()
                .logoutSuccessUrl(sso().getLoginPath());
        addAuthenticationEntryPoint(http);

    }

    @Bean
    public JwtAccessTokenConverter tokenEnhancer()
    {
        JwtAccessTokenConverter converter = new JwtAccessTokenConverter();
        converter.setVerifierKey(resourceDetails().getJwt().getKeyValue());
        return converter;
    }

    @Bean
    public TokenStore tokenStore()
    {
        return new JwtTokenStore(tokenEnhancer());
    }

    @Bean
    public DefaultTokenServices tokenServices()
    {
        DefaultTokenServices tokenServices = new DefaultTokenServices();
        tokenServices.setTokenStore(tokenStore());
        tokenServices.setSupportRefreshToken(true);
        tokenServices.setReuseRefreshToken(false);
        return tokenServices;
    }

    @Bean
    public OAuth2RestOperations oAuth2RestTemplateBean()
    {
        ResourceOwnerPasswordAccessTokenProvider tokenProvider = new ResourceOwnerPasswordAccessTokenProvider();
        tokenProvider.setInterceptors(Collections.singletonList(loadBalancerInterceptor));

        OAuth2RestTemplate oauth2Template = new OAuth2RestTemplate(clientDetails(), oauth2ClientContext);
        oauth2Template.setInterceptors(Collections.singletonList(loadBalancerInterceptor));
        oauth2Template.setAccessTokenProvider(new AccessTokenProviderChain(Collections.singletonList(tokenProvider)));

        return oauth2Template;
    }

    private Filter ssoFilter()
    {
        OAuth2ClientAuthenticationProcessingFilter oAuth2ClientAuthenticationProcessingFilter = new OAuth2ClientAuthenticationProcessingFilter(sso().getLoginPath());
        oAuth2ClientAuthenticationProcessingFilter.setRequiresAuthenticationRequestMatcher(new AntPathRequestMatcher(sso().getLoginPath(), HttpMethod.POST.name()));
        oAuth2ClientAuthenticationProcessingFilter.setAuthenticationSuccessHandler(new SimpleUrlAuthenticationSuccessHandler("/index"));

        oAuth2ClientAuthenticationProcessingFilter.setRestTemplate(oAuth2RestTemplateBean());

        oAuth2ClientAuthenticationProcessingFilter.setTokenServices(tokenServices());
        return oAuth2ClientAuthenticationProcessingFilter;
    }

    private static CsrfTokenRepository csrfTokenRepository()
    {
        HttpSessionCsrfTokenRepository repository = new HttpSessionCsrfTokenRepository();
        repository.setHeaderName(ConfigConstants.CSRF_HEADER_NAME);
        return repository;
    }

    private void addAuthenticationEntryPoint(HttpSecurity http)
            throws Exception {
        ExceptionHandlingConfigurer<HttpSecurity> exceptions = http.exceptionHandling();
        ContentNegotiationStrategy contentNegotiationStrategy = http
                .getSharedObject(ContentNegotiationStrategy.class);
        if (contentNegotiationStrategy == null) {
            contentNegotiationStrategy = new HeaderContentNegotiationStrategy();
        }
        MediaTypeRequestMatcher preferredMatcher = new MediaTypeRequestMatcher(
                contentNegotiationStrategy, MediaType.APPLICATION_XHTML_XML,
                new MediaType("image", "*"), MediaType.TEXT_HTML, MediaType.TEXT_PLAIN);
        preferredMatcher.setIgnoredMediaTypes(Collections.singleton(MediaType.ALL));
        exceptions.defaultAuthenticationEntryPointFor(
                new LoginUrlAuthenticationEntryPoint(sso().getLoginPath()),
                preferredMatcher);
        // When multiple entry points are provided the default is the first one
        exceptions.defaultAuthenticationEntryPointFor(
                new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED),
                new RequestHeaderRequestMatcher("X-Requested-With", "XMLHttpRequest"));
    }
}
