package com.gustavoreinaldi.dev_utils.service;

import com.gustavoreinaldi.dev_utils.model.entities.MockConfig;
import com.gustavoreinaldi.dev_utils.model.entities.ProjectCollection;
import com.gustavoreinaldi.dev_utils.model.entities.RouteConfig;
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
    private final RestOperations restTemplate;

    public ResponseEntity<Object> processRequest(HttpServletRequest request, RequestEntity<byte[]> requestEntity,
            Long collectionId) {
        String path = request.getRequestURI();
        String method = request.getMethod();

        log.info("Processing request: {} {}", method, path);

        // 0. Resolve Collection
        ProjectCollection collection = resolveCollection(collectionId);
        if (collection == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("No Project Collection found or selected.");
        }

        // 1. Check Mock
        Optional<MockConfig> mockOpt = mockRepository.findActiveMockConfig(collection.getId(), path, method);
        if (mockOpt.isPresent()) {
            log.info("Mock found for {} {}", method, path);
            MockConfig mock = mockOpt.get();
            return ResponseEntity.status(mock.getStatusCode())
                    .header(HttpHeaders.CONTENT_TYPE, "application/json")
                    .body(mock.getResponseBody());
        }

        // 2. Check Route (Proxy)
        List<RouteConfig> routes = routeRepository.findActiveRoutesByCollection(collection.getId());
        for (RouteConfig route : routes) {
            if (path.startsWith(route.getPathOrigem())) {
                String targetUrl = route.getTargetHost() + path; // Simple append, can be improved to strip prefix if
                                                                 // needed
                log.info("Route matched. Proxying to: {}", targetUrl);
                return forwardRequest(targetUrl, requestEntity);
            }
        }

        // 3. Fallback
        if (collection.getFallbackUrl() != null && !collection.getFallbackUrl().isEmpty()) {
            String fallbackUrl = collection.getFallbackUrl() + path;
            log.info("No mock/route found. Fallback to: {}", fallbackUrl);
            return forwardRequest(fallbackUrl, requestEntity);
        }

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No mock, route or fallback configured for this path.");
    }

    private ProjectCollection resolveCollection(Long collectionId) {
        if (collectionId != null) {
            return projectCollectionRepository.findById(collectionId).orElse(null);
        }
        // Default to first one for now if not specified - to be improved
        List<ProjectCollection> all = projectCollectionRepository.findAll();
        return all.isEmpty() ? null : all.get(0);
    }

    private ResponseEntity<Object> forwardRequest(String targetUrl, RequestEntity<byte[]> originalEntity) {
        try {
            URI uri = new URI(targetUrl);
            RequestEntity<byte[]> forwardEntity = new RequestEntity<>(
                    originalEntity.getBody(),
                    originalEntity.getHeaders(),
                    originalEntity.getMethod(),
                    uri);
            ResponseEntity<byte[]> response = restTemplate.exchange(forwardEntity, byte[].class);
            return ResponseEntity.status(response.getStatusCode())
                    .headers(response.getHeaders())
                    .body(response.getBody());
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            return ResponseEntity.status(e.getStatusCode())
                    .headers(e.getResponseHeaders())
                    .body(e.getResponseBodyAsByteArray());
        } catch (URISyntaxException e) {
            log.error("Invalid Target URL: {}", targetUrl, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Invalid Target URL Configuration");
        } catch (Exception e) {
            log.error("Proxy Error", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Proxy Error: " + e.getMessage());
        }
    }
}
