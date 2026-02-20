package com.gustavoreinaldi.dev_utils.config;

import jakarta.servlet.http.HttpServletRequest;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.util.Timeout;
import org.springframework.boot.web.servlet.filter.OrderedHiddenHttpMethodFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;

@Configuration
public class AppConfig {

    @Bean
    public OrderedHiddenHttpMethodFilter hiddenHttpMethodFilter() {
        return new OrderedHiddenHttpMethodFilter() {
            @Override
            protected boolean shouldNotFilter(HttpServletRequest request) {
                String path = request.getRequestURI();
                List<String> localPrefixes = Arrays.asList(
                        "/collections", "/routes", "/mocks", "/welcome", "/css", "/js", "/images", "/webjars", "/favicon.ico", "/error"
                );

                if (path.equals("/") || path.isEmpty()) {
                    return false; // Do not skip for root
                }

                for (String prefix : localPrefixes) {
                    if (path.startsWith(prefix)) {
                        return false; // Do not skip for local paths
                    }
                }

                return true; // Skip for everything else (proxy requests)
            }
        };
    }

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
