package com.gustavoreinaldi.dev_utils;

import com.gustavoreinaldi.dev_utils.model.entities.MockConfig;
import com.gustavoreinaldi.dev_utils.model.entities.ProjectCollection;
import com.gustavoreinaldi.dev_utils.model.entities.RouteConfig;
import com.gustavoreinaldi.dev_utils.repository.MockConfigRepository;
import com.gustavoreinaldi.dev_utils.repository.ProjectCollectionRepository;
import com.gustavoreinaldi.dev_utils.repository.RouteConfigRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.client.RestOperations;

import java.net.URI;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class ProxyIntegrationTest {

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private ProjectCollectionRepository collectionRepo;

        @Autowired
        private MockConfigRepository mockRepo;

        @Autowired
        private RouteConfigRepository routeRepo;

        @MockBean
        private RestOperations restTemplate;

        private ProjectCollection savedCollection;

        @BeforeEach
        void setup() {
                mockRepo.deleteAll();
                routeRepo.deleteAll();
                collectionRepo.deleteAll();

                ProjectCollection collection = ProjectCollection.builder()
                                .name("Default Collection")
                                .description("Test Collection")
                                .fallbackUrl("http://fallback.com")
                                .build();
                savedCollection = collectionRepo.save(collection);
        }

        @Test
        void testMockHit() throws Exception {
                MockConfig mock = MockConfig.builder()
                                .projectCollection(savedCollection)
                                .path("/api/mocked")
                                .httpMethod("GET")
                                .statusCode(200)
                                .responseBody("{\"message\": \"mocked\"}")
                                .isActive(true)
                                .build();
                mockRepo.save(mock);

                mockMvc.perform(get("/api/mocked")
                                .header("Origin", "http://localhost:3000"))
                                .andExpect(status().isOk())
                                .andExpect(content().json("{\"message\": \"mocked\"}"))
                                .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:3000"))
                                .andExpect(header().string("Access-Control-Allow-Credentials", "true"));
        }

        @Test
        void testRouteProxyHit() throws Exception {
                RouteConfig route = RouteConfig.builder()
                                .projectCollection(savedCollection)
                                .pathOrigem("/api/service")
                                .targetHost("http://backend-service.com")
                                .isActive(true)
                                .build();
                routeRepo.save(route);

                // Mock RestTemplate response
                when(restTemplate.exchange(any(RequestEntity.class), eq(Object.class)))
                                .thenReturn(new ResponseEntity<>("proxied_response", HttpStatus.OK));

                mockMvc.perform(post("/api/service/users")
                                .content("test body"))
                                .andExpect(status().isOk())
                                .andExpect(content().string("proxied_response"));

                // Verify RestTemplate called with correct URL
                ArgumentCaptor<RequestEntity> captor = ArgumentCaptor.forClass(RequestEntity.class);
                verify(restTemplate).exchange(captor.capture(), eq(Object.class));

                RequestEntity capturedRequest = captor.getValue();
                assertEquals(new URI("http://backend-service.com/api/service/users"), capturedRequest.getUrl());
                assertEquals(HttpMethod.POST, capturedRequest.getMethod());
        }

        @Test
        void testFallback() throws Exception {
                // No mock, No route
                // Should hit fallbackUrl configured in collection: http://fallback.com

                when(restTemplate.exchange(any(RequestEntity.class), eq(Object.class)))
                                .thenReturn(new ResponseEntity<>("fallback_response", HttpStatus.OK));

                mockMvc.perform(get("/unknown/path"))
                                .andExpect(status().isOk())
                                .andExpect(content().string("fallback_response"));

                // Verify RestTemplate called with fallback URL
                ArgumentCaptor<RequestEntity> captor = ArgumentCaptor.forClass(RequestEntity.class);
                verify(restTemplate).exchange(captor.capture(), eq(Object.class));
                assertEquals(new URI("http://fallback.com/unknown/path"), captor.getValue().getUrl());
        }
}
