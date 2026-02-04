package com.gustavoreinaldi.dev_utils;

import com.gustavoreinaldi.dev_utils.model.entities.MockConfig;
import com.gustavoreinaldi.dev_utils.model.entities.ProjectCollection;
import com.gustavoreinaldi.dev_utils.repository.MockConfigRepository;
import com.gustavoreinaldi.dev_utils.repository.ProjectCollectionRepository;
import com.gustavoreinaldi.dev_utils.repository.RouteConfigRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class CollectionStatusIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ProjectCollectionRepository collectionRepository;

    @Autowired
    private MockConfigRepository mockRepository;

    @Autowired
    private RouteConfigRepository routeRepository;

    private ProjectCollection disabledCollection;
    private ProjectCollection enabledCollection;

    @BeforeEach
    void setUp() {
        mockRepository.deleteAll();
        routeRepository.deleteAll();
        collectionRepository.deleteAll();

        disabledCollection = ProjectCollection.builder()
                .name("Disabled")
                .isActive(false)
                .build();
        collectionRepository.save(disabledCollection);

        enabledCollection = ProjectCollection.builder()
                .name("Enabled")
                .isActive(true)
                .build();
        collectionRepository.save(enabledCollection);

        // Mock in disabled collection
        MockConfig mockDisabled = MockConfig.builder()
                .path("/api/disabled")
                .httpMethod("GET")
                .statusCode(200)
                .responseBody("{\"status\":\"disabled\"}")
                .isActive(true)
                .projectCollection(disabledCollection)
                .build();
        mockRepository.save(mockDisabled);

        // Mock in enabled collection
        MockConfig mockEnabled = MockConfig.builder()
                .path("/api/enabled")
                .httpMethod("GET")
                .statusCode(200)
                .responseBody("{\"status\":\"enabled\"}")
                .isActive(true)
                .projectCollection(enabledCollection)
                .build();
        mockRepository.save(mockEnabled);
    }

    @Test
    void whenCollectionIsDisabled_mockShouldNotBeFound() throws Exception {
        mockMvc.perform(get("/api/disabled"))
                .andExpect(status().isNotFound());
    }

    @Test
    void whenCollectionIsEnabled_mockShouldBeFound() throws Exception {
        mockMvc.perform(get("/api/enabled"))
                .andExpect(status().isOk())
                .andExpect(content().json("{\"status\":\"enabled\"}"));
    }
}
