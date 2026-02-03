package com.gustavoreinaldi.dev_utils;

import com.gustavoreinaldi.dev_utils.model.entities.GlobalConfig;
import com.gustavoreinaldi.dev_utils.model.entities.MockConfig;
import com.gustavoreinaldi.dev_utils.model.entities.ProjectCollection;
import com.gustavoreinaldi.dev_utils.model.entities.RouteConfig;
import com.gustavoreinaldi.dev_utils.repository.GlobalConfigRepository;
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

        @Autowired
        private GlobalConfigRepository globalConfigRepo;

        @MockBean
        private RestOperations restTemplate;

        private ProjectCollection savedCollection;

        @BeforeEach
        void setup() {
                mockRepo.deleteAll();
                routeRepo.deleteAll();
                collectionRepo.deleteAll();
                globalConfigRepo.deleteAll();

                ProjectCollection collection = ProjectCollection.builder()
                                .name("Default Collection")
                                .description("Test Collection")
                                .build();
                savedCollection = collectionRepo.save(collection);

                GlobalConfig globalConfig = GlobalConfig.builder()
                                .id(1L)
                                .fallbackUrl("http://fallback.com")
                                .build();
                globalConfigRepo.save(globalConfig);
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
                byte[] responseBytes = "proxied_response".getBytes(java.nio.charset.StandardCharsets.UTF_8);
                when(restTemplate.exchange(any(RequestEntity.class), eq(byte[].class)))
                                .thenReturn(new ResponseEntity<>(responseBytes, HttpStatus.OK));

                mockMvc.perform(post("/api/service/users")
                                .content("test body"))
                                .andExpect(status().isOk())
                                .andExpect(content().string("proxied_response"));

                // Verify RestTemplate called with correct URL
                ArgumentCaptor<RequestEntity> captor = ArgumentCaptor.forClass(RequestEntity.class);
                verify(restTemplate).exchange(captor.capture(), eq(byte[].class));

                RequestEntity capturedRequest = captor.getValue();
                assertEquals(new URI("http://backend-service.com/api/service/users"), capturedRequest.getUrl());
                assertEquals(HttpMethod.POST, capturedRequest.getMethod());
        }

        @Test
        void testFallback() throws Exception {
                // No mock, No route
                // Should hit fallbackUrl configured in collection: http://fallback.com

                byte[] responseBytes = "fallback_response".getBytes(java.nio.charset.StandardCharsets.UTF_8);
                when(restTemplate.exchange(any(RequestEntity.class), eq(byte[].class)))
                                .thenReturn(new ResponseEntity<>(responseBytes, HttpStatus.OK));

                mockMvc.perform(get("/unknown/path"))
                                .andExpect(status().isOk())
                                .andExpect(content().string("fallback_response"));

                // Verify RestTemplate called with fallback URL
                ArgumentCaptor<RequestEntity> captor = ArgumentCaptor.forClass(RequestEntity.class);
                verify(restTemplate).exchange(captor.capture(), eq(byte[].class));
                assertEquals(new URI("http://fallback.com/unknown/path"), captor.getValue().getUrl());
        }

        @Test
        void testMockCollision() throws Exception {
                // Create a second collection
                ProjectCollection collection2 = ProjectCollection.builder()
                                .name("Collection 2")
                                .build();
                collectionRepo.save(collection2);

                // Create same mock in both collections
                MockConfig mock1 = MockConfig.builder()
                                .projectCollection(savedCollection)
                                .path("/api/collision")
                                .httpMethod("GET")
                                .statusCode(200)
                                .responseBody("Mock 1")
                                .isActive(true)
                                .build();
                mockRepo.save(mock1);

                MockConfig mock2 = MockConfig.builder()
                                .projectCollection(collection2)
                                .path("/api/collision")
                                .httpMethod("GET")
                                .statusCode(200)
                                .responseBody("Mock 2")
                                .isActive(true)
                                .build();
                mockRepo.save(mock2);

                // Expect 409 Conflict
                mockMvc.perform(get("/api/collision"))
                                .andExpect(status().isConflict());
        }

        @Test
        void testRouteCollision() throws Exception {
                // Create a second collection
                ProjectCollection collection2 = ProjectCollection.builder()
                                .name("Collection 2")
                                .build();
                collectionRepo.save(collection2);

                // Create same route in both collections
                RouteConfig route1 = RouteConfig.builder()
                                .projectCollection(savedCollection)
                                .pathOrigem("/api/conflict")
                                .targetHost("http://host1")
                                .isActive(true)
                                .build();
                routeRepo.save(route1);

                RouteConfig route2 = RouteConfig.builder()
                                .projectCollection(collection2)
                                .pathOrigem("/api/conflict")
                                .targetHost("http://host2")
                                .isActive(true)
                                .build();
                routeRepo.save(route2);

                // Expect 409 Conflict
                mockMvc.perform(get("/api/conflict/something"))
                                .andExpect(status().isConflict());
        }

        @Test
        void testCrossCollectionHit() throws Exception {
                // Ensure we can hit a mock in a second collection without any header
                ProjectCollection collection2 = ProjectCollection.builder().name("C2").build();
                collectionRepo.save(collection2);

                MockConfig mock = MockConfig.builder()
                                .projectCollection(collection2)
                                .path("/api/c2")
                                .httpMethod("GET")
                                .statusCode(202)
                                .responseBody("c2")
                                .isActive(true)
                                .build();
                mockRepo.save(mock);

                mockMvc.perform(get("/api/c2"))
                                .andExpect(status().isAccepted())
                                .andExpect(content().string("c2"));
        }
}
