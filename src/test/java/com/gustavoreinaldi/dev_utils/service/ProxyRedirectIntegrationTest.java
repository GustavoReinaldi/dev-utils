package com.gustavoreinaldi.dev_utils.service;

import com.gustavoreinaldi.dev_utils.model.entities.GlobalConfig;
import com.gustavoreinaldi.dev_utils.repository.GlobalConfigRepository;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class ProxyRedirectIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private GlobalConfigRepository globalConfigRepo;

    private HttpServer server;
    private int port;
    private AtomicReference<String> lastMethod = new AtomicReference<>();
    private AtomicInteger requestCount = new AtomicInteger(0);

    @BeforeEach
    public void setup() throws IOException {
        server = HttpServer.create(new InetSocketAddress(0), 0);
        port = server.getAddress().getPort();

        server.createContext("/source", exchange -> {
            requestCount.incrementAndGet();
            lastMethod.set(exchange.getRequestMethod());
            exchange.getResponseHeaders().set("Location", "http://localhost:" + port + "/target");
            exchange.sendResponseHeaders(302, -1);
            exchange.close();
        });

        server.createContext("/target", exchange -> {
            requestCount.incrementAndGet();
            lastMethod.set(exchange.getRequestMethod());
            String response = "Final Success";
            exchange.sendResponseHeaders(200, response.length());
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(response.getBytes());
            }
            exchange.close();
        });

        server.start();

        // Configure fallback to point to our test server
        GlobalConfig config = GlobalConfig.builder()
                .id(1L)
                .fallbackUrl("http://localhost:" + port)
                .build();
        globalConfigRepo.save(config);

        requestCount.set(0);
        lastMethod.set(null);
    }

    @AfterEach
    public void tearDown() {
        if (server != null) {
            server.stop(0);
        }
    }

    @Test
    public void testProxyFollowsRedirectAndPreservesPostMethod() throws Exception {
        mockMvc.perform(post("/source")
                .content("test body")
                .contentType(MediaType.TEXT_PLAIN))
                .andExpect(status().isOk())
                .andExpect(content().string("Final Success"));

        assertEquals(2, requestCount.get());
        assertEquals("POST", lastMethod.get(), "Method should be preserved as POST");
    }

    @Test
    public void testProxyFollowsRedirectForGetMethod() throws Exception {
        mockMvc.perform(get("/source"))
                .andExpect(status().isOk())
                .andExpect(content().string("Final Success"));

        assertEquals(2, requestCount.get());
        assertEquals("GET", lastMethod.get());
    }
}
