package com.gustavoreinaldi.dev_utils.service;

import com.gustavoreinaldi.dev_utils.model.entities.GlobalConfig;
import com.gustavoreinaldi.dev_utils.model.entities.MockConfig;
import com.gustavoreinaldi.dev_utils.model.entities.ProjectCollection;
import com.gustavoreinaldi.dev_utils.model.entities.RouteConfig;
import com.gustavoreinaldi.dev_utils.repository.GlobalConfigRepository;
import com.gustavoreinaldi.dev_utils.repository.MockConfigRepository;
import com.gustavoreinaldi.dev_utils.repository.ProjectCollectionRepository;
import com.gustavoreinaldi.dev_utils.repository.RouteConfigRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestOperations;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProxyService {

    private final MockConfigRepository mockRepository;
    private final RouteConfigRepository routeRepository;
    private final ProjectCollectionRepository projectCollectionRepository;
    private final GlobalConfigRepository globalConfigRepository;
    private final RestOperations restTemplate;

    public ResponseEntity<Object> processRequest(HttpServletRequest request, RequestEntity<byte[]> requestEntity) {
        String path = request.getRequestURI();
        String method = request.getMethod();

        log.info("Processing request: {} {}", method, path);

        // 1. Check Mock (Global)
        List<MockConfig> mocks = mockRepository.findActiveMockConfigs(path, method);
        if (mocks.size() > 1) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Conflict: Multiple mocks found for path " + path);
        }
        if (!mocks.isEmpty()) {
            MockConfig mock = mocks.get(0);
            log.info("Mock found for {} {}", method, path);
            return ResponseEntity.status(mock.getStatusCode())
                    .header(HttpHeaders.CONTENT_TYPE, "application/json")
                    .body(mock.getResponseBody());
        }

        // 2. Check Route (Global)
        List<RouteConfig> allRoutes = routeRepository.findAllActiveRoutes();
        java.util.List<RouteConfig> matchingRoutes = new java.util.ArrayList<>();
        for (RouteConfig route : allRoutes) {
            if (path.startsWith(route.getPathOrigem())) {
                matchingRoutes.add(route);
            }
        }

        if (matchingRoutes.size() > 1) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Conflict: Multiple routes matched path " + path);
        }

        String queryString = request.getQueryString();
        String pathWithQuery = path + (queryString != null ? "?" + queryString : "");

        if (!matchingRoutes.isEmpty()) {
            RouteConfig route = matchingRoutes.get(0);
            String targetUrl = route.getTargetHost() + pathWithQuery;
            log.info("Route matched. Proxying to: {}", targetUrl);
            return forwardRequest(targetUrl, requestEntity);
        }

        // 3. Fallback (Global Config)
        Optional<GlobalConfig> configOpt = globalConfigRepository.findById(1L);
        if (configOpt.isPresent() && configOpt.get().getFallbackUrl() != null && !configOpt.get().getFallbackUrl().isEmpty()) {
            String fallbackUrl = configOpt.get().getFallbackUrl() + pathWithQuery;
            log.info("No mock/route found. Fallback to: {}", fallbackUrl);
            return forwardRequest(fallbackUrl, requestEntity);
        }

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No mock, route or fallback configured for this path.");
    }

    private ResponseEntity<Object> forwardRequest(String targetUrl, RequestEntity<byte[]> originalEntity) {
        int maxRedirects = 5;
        int redirectCount = 0;
        String currentUrl = targetUrl;

        while (true) {
            try {
                URI uri = new URI(currentUrl);

                // Copy headers and remove Host header to let the proxy set it correctly
                HttpHeaders headers = new HttpHeaders();
                headers.putAll(originalEntity.getHeaders());
                headers.remove(HttpHeaders.HOST);

                RequestEntity<byte[]> forwardEntity = new RequestEntity<>(
                        originalEntity.getBody(),
                        headers,
                        originalEntity.getMethod(),
                        uri);

                log.info("Forwarding request (attempt {}): {} {}", redirectCount + 1, originalEntity.getMethod(), currentUrl);
                ResponseEntity<byte[]> response = restTemplate.exchange(forwardEntity, byte[].class);

                // Manually handle redirects to preserve the original HTTP method
                if (response.getStatusCode().is3xxRedirection() && redirectCount < maxRedirects) {
                    String location = response.getHeaders().getFirst(HttpHeaders.LOCATION);
                    if (location != null) {
                        currentUrl = uri.resolve(location).toString();
                        redirectCount++;
                        log.info("Following redirect to: {}", currentUrl);
                        continue;
                    }
                }

                return ResponseEntity.status(response.getStatusCode())
                        .headers(response.getHeaders())
                        .body(response.getBody());

            } catch (HttpClientErrorException | HttpServerErrorException e) {
                // Also handle redirects that might be reported as exceptions by RestTemplate
                if (e.getStatusCode().is3xxRedirection() && redirectCount < maxRedirects) {
                    String location = e.getResponseHeaders().getFirst(HttpHeaders.LOCATION);
                    if (location != null) {
                        try {
                            currentUrl = new URI(currentUrl).resolve(location).toString();
                            redirectCount++;
                            log.info("Following redirect (from exception) to: {}", currentUrl);
                            continue;
                        } catch (Exception ex) {
                            log.error("Error resolving redirect URI", ex);
                        }
                    }
                }
                return ResponseEntity.status(e.getStatusCode())
                        .headers(e.getResponseHeaders())
                        .body(e.getResponseBodyAsByteArray());
            } catch (URISyntaxException e) {
                log.error("Invalid Target URL: {}", currentUrl, e);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Invalid Target URL Configuration");
            } catch (Exception e) {
                log.error("Proxy Error", e);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Proxy Error: " + e.getMessage());
            }
        }
    }
}
