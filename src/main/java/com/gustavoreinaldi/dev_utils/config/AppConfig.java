package com.gustavoreinaldi.dev_utils.config;

import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.util.Timeout;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.net.URI;

@Configuration
public class AppConfig {

    @Bean
    public RestTemplate restTemplate() {
        // We disable automatic redirect handling to handle it manually in ProxyService.
        // This allows us to preserve the original HTTP method (POST, etc.) during redirects.
        CloseableHttpClient httpClient = HttpClients.custom()
                .disableRedirectHandling()
                .setDefaultRequestConfig(RequestConfig.custom()
                        .setConnectTimeout(Timeout.ofSeconds(5))
                        .setResponseTimeout(Timeout.ofSeconds(30))
                        .build())
                .build();

        HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory(httpClient);
        return new RestTemplate(factory);
    }
}
