package com.gustavoreinaldi.dev_utils.controller;

import com.gustavoreinaldi.dev_utils.model.dtos.CollectionForm;
import com.gustavoreinaldi.dev_utils.model.entities.ProjectCollection;
import com.gustavoreinaldi.dev_utils.repository.ProjectCollectionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.List;

@ControllerAdvice
@RequiredArgsConstructor
public class GlobalControllerAdvice {

    private final ProjectCollectionRepository projectCollectionRepository;

    @ModelAttribute
    public void addAttributes(Model model) {
        // Ensure 'collections' is always available for the sidebar
        List<ProjectCollection> collections = projectCollectionRepository.findAll();
        model.addAttribute("collections", collections);
        if (!model.containsAttribute("newCollectionForm")) {
            model.addAttribute("newCollectionForm", new CollectionForm());
        }
    }
}
