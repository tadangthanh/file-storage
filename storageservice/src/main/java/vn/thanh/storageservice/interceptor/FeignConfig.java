package vn.thanh.storageservice.interceptor;


import feign.RequestInterceptor;
import feign.codec.ErrorDecoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.security.oauth2.client.*;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizedClientRepository;

@Configuration
public class FeignConfig {

    @Bean
    public OAuth2AuthorizedClientManager authorizedClientManager(
            ClientRegistrationRepository clientRegistrations,
            OAuth2AuthorizedClientRepository authorizedClients) {

        OAuth2AuthorizedClientProvider provider = OAuth2AuthorizedClientProviderBuilder.builder()
                .clientCredentials()
                .build();

        DefaultOAuth2AuthorizedClientManager manager =
                new DefaultOAuth2AuthorizedClientManager(clientRegistrations, authorizedClients);
        manager.setAuthorizedClientProvider(provider);
        return manager;
    }

    @Bean
    public RequestInterceptor oauth2FeignRequestInterceptor(OAuth2AuthorizedClientManager manager) {
        return requestTemplate -> {
            // Lấy registrationId đúng với name của FeignClient
            String registrationId = requestTemplate.feignTarget().name();

            OAuth2AuthorizeRequest authRequest = OAuth2AuthorizeRequest.withClientRegistrationId(registrationId)
                    .principal("storage-service")    // bất kỳ username đại diện cho client
                    .build();

            OAuth2AuthorizedClient client = manager.authorize(authRequest);
            if (client == null || client.getAccessToken() == null) {
                throw new IllegalStateException("Không lấy được access token cho " + registrationId);
            }

            String token = client.getAccessToken().getTokenValue();
            requestTemplate.header(HttpHeaders.AUTHORIZATION, "Bearer " + token);
        };
    }
    @Bean
    public ErrorDecoder errorDecoder() {
        return new CustomErrorDecoder();
    }
}
