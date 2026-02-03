package com.gustavoreinaldi.dev_utils.controller;

import com.gustavoreinaldi.dev_utils.model.entities.MockConfig;
import com.gustavoreinaldi.dev_utils.model.entities.ProjectCollection;
import com.gustavoreinaldi.dev_utils.repository.MockConfigRepository;
import com.gustavoreinaldi.dev_utils.repository.ProjectCollectionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import com.gustavoreinaldi.dev_utils.model.dtos.MockForm;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/mocks")
@RequiredArgsConstructor
public class MockWebController {

        private final MockConfigRepository mockConfigRepository;
        private final ProjectCollectionRepository projectCollectionRepository;

        @PostMapping("/collection/{collectionId}")
        public String addMock(@PathVariable Long collectionId, @jakarta.validation.Valid @ModelAttribute MockForm form,
                        org.springframework.validation.BindingResult bindingResult) {
                if (bindingResult.hasErrors()) {
                        return "redirect:/collections/" + collectionId;
                }
                ProjectCollection collection = projectCollectionRepository.findById(collectionId)
                                .orElseThrow(() -> new IllegalArgumentException(
                                                "Invalid collection Id:" + collectionId));

                MockConfig mockConfig = MockConfig.builder()
                                .path(form.getPath())
                                .httpMethod(form.getHttpMethod())
                                .statusCode(form.getStatusCode())
                                .responseBody(form.getResponseBody())
                                .projectCollection(collection)
                                .isActive(true)
                                .build();
                mockConfigRepository.save(mockConfig);

                return "redirect:/collections/" + collectionId;
        }

        @DeleteMapping("/{id}")
        public String deleteMock(@PathVariable Long id) {
                MockConfig mock = mockConfigRepository.findById(id)
                                .orElseThrow(() -> new IllegalArgumentException("Invalid mock Id:" + id));
                Long collectionId = mock.getProjectCollection().getId();

                mockConfigRepository.deleteById(id);

                return "redirect:/collections/" + collectionId;
        }

        @PatchMapping("/{id}/toggle")
        public String toggleMock(@PathVariable Long id, Model model) {
                MockConfig mock = mockConfigRepository.findById(id)
                                .orElseThrow(() -> new IllegalArgumentException("Invalid mock Id:" + id));

                mock.setIsActive(!mock.getIsActive());
                mockConfigRepository.save(mock);

                model.addAttribute("mock", mock);
                return "fragments/mock-list :: mock-row(mock=${mock})";
        }
}
