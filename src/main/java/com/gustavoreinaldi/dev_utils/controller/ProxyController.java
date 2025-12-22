package com.gustavoreinaldi.dev_utils.controller;

import com.gustavoreinaldi.dev_utils.service.ProxyService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class ProxyController {

    private final ProxyService proxyService;

    @RequestMapping("/**")
    public ResponseEntity<Object> splitAll(
            HttpServletRequest request,
            RequestEntity<byte[]> requestEntity,
            @RequestHeader(value = "X-Project-Collection-ID", required = false) Long collectionId) {

        // Exclude specific internal API paths if necessary, but "/**" catches
        // everything.
        // We assume /api/v1/projects etc are handled by other controllers.
        // Spring matches more specific mappings first, so real API controllers should
        // take precedence naturally.

        return proxyService.processRequest(request, requestEntity, collectionId);
    }
}
