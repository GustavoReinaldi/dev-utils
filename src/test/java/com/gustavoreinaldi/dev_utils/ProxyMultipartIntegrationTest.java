package com.gustavoreinaldi.dev_utils;

import com.gustavoreinaldi.dev_utils.model.entities.ProjectCollection;
import com.gustavoreinaldi.dev_utils.model.entities.RouteConfig;
import com.gustavoreinaldi.dev_utils.repository.ProjectCollectionRepository;
import com.gustavoreinaldi.dev_utils.repository.RouteConfigRepository;
import org.apache.hc.client5.http.entity.mime.MultipartEntityBuilder;
import org.apache.hc.core5.http.HttpEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.client.RestOperations;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class ProxyMultipartIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ProjectCollectionRepository collectionRepo;

    @Autowired
    private RouteConfigRepository routeRepo;

    @MockBean
    private RestOperations restTemplate;

    private ProjectCollection savedCollection;

    @BeforeEach
    void setup() {
        routeRepo.deleteAll();
        collectionRepo.deleteAll();

        ProjectCollection collection = ProjectCollection.builder()
                .name("Default Collection")
                .description("Test Collection")
                .build();
        savedCollection = collectionRepo.save(collection);
    }

    @Test
    void testMultipartFileForwarding() throws Exception {
        RouteConfig route = RouteConfig.builder()
                .projectCollection(savedCollection)
                .pathOrigem("/api/upload")
                .targetHost("http://backend-service.com")
                .isActive(true)
                .build();
        routeRepo.save(route);

        // Build a real multipart body
        HttpEntity multipartEntity = MultipartEntityBuilder.create()
                .addBinaryBody("file", "Hello, World!".getBytes(StandardCharsets.UTF_8),
                               org.apache.hc.core5.http.ContentType.TEXT_PLAIN, "test.txt")
                .addTextBody("description", "test file")
                .build();

        ByteArrayOutputStream os = new ByteArrayOutputStream();
        multipartEntity.writeTo(os);
        byte[] multipartBody = os.toByteArray();
        String contentType = multipartEntity.getContentType();

        when(restTemplate.exchange(any(RequestEntity.class), eq(byte[].class)))
                .thenReturn(new ResponseEntity<>("success".getBytes(), HttpStatus.OK));

        mockMvc.perform(post("/api/upload")
                        .contentType(contentType)
                        .content(multipartBody))
                .andExpect(status().isOk());

        ArgumentCaptor<RequestEntity> captor = ArgumentCaptor.forClass(RequestEntity.class);
        verify(restTemplate).exchange(captor.capture(), eq(byte[].class));

        RequestEntity capturedRequest = captor.getValue();
        byte[] body = (byte[]) capturedRequest.getBody();
        assertNotNull(body, "Body should not be null");
        String bodyString = new String(body, StandardCharsets.UTF_8);

        // Check if both the file content and the parameter are in the body
        assertTrue(bodyString.contains("Hello, World!"), "Body should contain file content");
        assertTrue(bodyString.contains("test file"), "Body should contain parameter value");
        assertTrue(bodyString.contains("description"), "Body should contain parameter name");

        // Verify Content-Type header was preserved (with boundary)
        assertTrue(capturedRequest.getHeaders().getContentType().toString().contains("boundary="));
    }

    @Test
    void testHiddenMethodStillWorksForLocalPaths() throws Exception {
        // Verify that HiddenHttpMethodFilter is NOT skipped for local paths
        mockMvc.perform(post("/collections/" + savedCollection.getId())
                        .param("_method", "DELETE"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/collections"));
    }
}
