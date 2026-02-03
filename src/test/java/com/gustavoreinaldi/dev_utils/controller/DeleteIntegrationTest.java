package com.gustavoreinaldi.dev_utils.controller;

import com.gustavoreinaldi.dev_utils.model.entities.MockConfig;
import com.gustavoreinaldi.dev_utils.model.entities.ProjectCollection;
import com.gustavoreinaldi.dev_utils.model.entities.RouteConfig;
import com.gustavoreinaldi.dev_utils.repository.MockConfigRepository;
import com.gustavoreinaldi.dev_utils.repository.ProjectCollectionRepository;
import com.gustavoreinaldi.dev_utils.repository.RouteConfigRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.filter.HiddenHttpMethodFilter;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class DeleteIntegrationTest {

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private ProjectCollectionRepository collectionRepo;

    @Autowired
    private MockConfigRepository mockRepo;

    @Autowired
    private RouteConfigRepository routeRepo;

    private ProjectCollection savedCollection;

    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .addFilters(new HiddenHttpMethodFilter())
                .build();

        mockRepo.deleteAll();
        routeRepo.deleteAll();
        collectionRepo.deleteAll();

        ProjectCollection collection = ProjectCollection.builder()
                .name("Default Collection")
                .description("Test Collection")
                .build();
        savedCollection = collectionRepo.save(collection);
    }

    @Test
    void testDeleteRouteViaHiddenMethod() throws Exception {
        RouteConfig route = RouteConfig.builder()
                .projectCollection(savedCollection)
                .pathOrigem("/api/service")
                .targetHost("http://backend-service.com")
                .isActive(true)
                .build();
        route = routeRepo.save(route);
        Long id = route.getId();

        mockMvc.perform(post("/routes/" + id)
                .param("_method", "DELETE"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/collections/" + savedCollection.getId()));

        assertFalse(routeRepo.findById(id).isPresent());
    }

    @Test
    void testDeleteMockViaHiddenMethod() throws Exception {
        MockConfig mock = MockConfig.builder()
                .projectCollection(savedCollection)
                .path("/api/mocked")
                .httpMethod("GET")
                .statusCode(200)
                .responseBody("{\"message\": \"mocked\"}")
                .isActive(true)
                .build();
        mock = mockRepo.save(mock);
        Long id = mock.getId();

        mockMvc.perform(post("/mocks/" + id)
                .param("_method", "DELETE"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/collections/" + savedCollection.getId()));

        assertFalse(mockRepo.findById(id).isPresent());
    }

    @Test
    void testDeleteCollectionViaHiddenMethod() throws Exception {
        Long id = savedCollection.getId();

        mockMvc.perform(post("/collections/" + id)
                .param("_method", "DELETE"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/collections"));

        assertFalse(collectionRepo.findById(id).isPresent());
    }
}
