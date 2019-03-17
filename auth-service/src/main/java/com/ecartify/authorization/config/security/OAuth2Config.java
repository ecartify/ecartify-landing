package com.ecartify.authorization.config.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;
import org.springframework.security.oauth2.provider.token.store.KeyStoreKeyFactory;

@Configuration
public class OAuth2Config extends AuthorizationServerConfigurerAdapter
{
    @Autowired
    @Qualifier("authenticationManagerBean")
    private AuthenticationManager authenticationManager;

    @Autowired
    private MongoUserDetailsService mongoUserDetailsService;

    @Value("${ecartify.gateway.oauth2.clientId}")
    private String apiGatewayOauthClientID;

    @Value("${ecartify.gateway.oauth2.clientSecret}")
    private String apiGatewayOauthClientSecret;

    @Value("${ecartify.gateway.oauth2.scope}")
    private String apiGatewayOauthClientScope;

    @Value("${ecartify.gateway.oauth2.accessTokenValidityInSeconds}")
    private int accessTokenValiditiyInSeconds;

    @Value("${ecartify.gateway.oauth2.refreshTokenValidityInSeconds}")
    private int refreshTokenValiditiyInSeconds;

    @Value("${ecartify.gateway.oauth2.jwt.keystore.name}")
    private String jwtKeyStoreName;

    @Value("${ecartify.gateway.oauth2.jwt.keystore.password}")
    private String jwtKeyStorePass;

    @Value("${ecartify.gateway.oauth2.jwt.keyName}")
    private String jwtKeyName;


    @Override
    public void configure(AuthorizationServerSecurityConfigurer security) throws Exception
    {
        security.tokenKeyAccess("isAuthenticated()")
                .passwordEncoder(NoOpPasswordEncoder.getInstance());
    }

    @Override
    public void configure(ClientDetailsServiceConfigurer clients) throws Exception
    {
        clients
                .inMemory()
                .withClient(apiGatewayOauthClientID)
                .secret(apiGatewayOauthClientSecret)
                .scopes(apiGatewayOauthClientScope)
                .resourceIds()
                .authorizedGrantTypes("password", "refresh_token")
                .autoApprove(true)
                .accessTokenValiditySeconds(accessTokenValiditiyInSeconds)
                .refreshTokenValiditySeconds(refreshTokenValiditiyInSeconds);

    }


    @Bean
    public JwtAccessTokenConverter accessTokenConverter()
    {
        JwtAccessTokenConverter converter = new JwtAccessTokenConverter();
        KeyStoreKeyFactory keyStoreKeyFactory = new KeyStoreKeyFactory(new ClassPathResource(jwtKeyStoreName),
                jwtKeyStorePass.toCharArray());
        converter.setKeyPair(keyStoreKeyFactory.getKeyPair(jwtKeyName));
        return converter;
    }

    @Override
    public void configure(AuthorizationServerEndpointsConfigurer endpoints) throws Exception {
        endpoints.authenticationManager(authenticationManager)
                .userDetailsService(mongoUserDetailsService)
                .reuseRefreshTokens(false) // is to use non-reusable refresh token
                .accessTokenConverter(accessTokenConverter());
    }

}
