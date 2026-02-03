package com.gustavoreinaldi.dev_utils.controller;

import com.gustavoreinaldi.dev_utils.model.dtos.CollectionForm;
import com.gustavoreinaldi.dev_utils.model.entities.GlobalConfig;
import com.gustavoreinaldi.dev_utils.model.entities.ProjectCollection;
import com.gustavoreinaldi.dev_utils.repository.GlobalConfigRepository;
import com.gustavoreinaldi.dev_utils.repository.ProjectCollectionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/collections")
@RequiredArgsConstructor
public class CollectionWebController {

    private final ProjectCollectionRepository projectCollectionRepository;
    private final GlobalConfigRepository globalConfigRepository;

    @GetMapping
    public String index() {
        List<ProjectCollection> collections = projectCollectionRepository.findAll();
        if (collections.isEmpty()) {
            // If no collections, stay on a landing page or create one
            return "redirect:/collections/new";
        }
        // Redirect to the first collection's dashboard
        return "redirect:/collections/" + collections.get(0).getId();
    }

    @GetMapping("/new")
    public String newCollection(Model model) {
        // Just show the layout with sidebar (needing collections list) and the welcome
        // page
        List<ProjectCollection> collections = projectCollectionRepository.findAll();
        model.addAttribute("collections", collections);
        model.addAttribute("newCollectionForm", new CollectionForm());
        return "welcome";
    }

    // Using a separate mapping for the root to handle the initial load
    // This methods allows us to redirect to /collections from /
    // See RootController below (or we can add it here if @RequestMapping is not
    // strict)

    @GetMapping("/{id}")
    public String dashboard(
            @PathVariable Long id,
            @RequestParam(required = false) List<String> mockStatus,
            @RequestParam(required = false) List<String> routeStatus,
            @RequestParam(defaultValue = "routes") String tab,
            Model model) {
        ProjectCollection collection = projectCollectionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid collection Id:" + id));

        model.addAttribute("currentCollection", collection);
        // model.addAttribute("collections", allCollections); // Handled by
        // GlobalControllerAdvice

        // Set defaults if null
        if (mockStatus == null) {
            mockStatus = Arrays.asList("active", "inactive");
        }
        if (routeStatus == null) {
            routeStatus = Arrays.asList("active", "inactive");
        }

        List<com.gustavoreinaldi.dev_utils.model.entities.RouteConfig> routes = collection.getRouteConfigs();
        if (routeStatus.size() < 2 && !routeStatus.isEmpty()) {
            boolean activeOnly = routeStatus.contains("active");
            routes = routes.stream()
                    .filter(r -> r.getIsActive() == activeOnly)
                    .collect(Collectors.toList());
        }

        List<com.gustavoreinaldi.dev_utils.model.entities.MockConfig> mocks = collection.getMockConfigs();
        if (mockStatus.size() < 2 && !mockStatus.isEmpty()) {
            boolean activeOnly = mockStatus.contains("active");
            mocks = mocks.stream()
                    .filter(m -> m.getIsActive() == activeOnly)
                    .collect(Collectors.toList());
        }

        model.addAttribute("routes", routes);
        model.addAttribute("mocks", mocks);
        model.addAttribute("mockStatus", mockStatus);
        model.addAttribute("routeStatus", routeStatus);
        model.addAttribute("activeTab", tab);

        // Forms for editing and creating children
        CollectionForm collectionForm = new CollectionForm();
        collectionForm.setName(collection.getName());
        collectionForm.setDescription(collection.getDescription());

        model.addAttribute("collectionForm", collectionForm);

        String fallbackUrl = globalConfigRepository.findById(1L)
                .map(GlobalConfig::getFallbackUrl)
                .orElse("");
        model.addAttribute("globalFallbackUrl", fallbackUrl);
        model.addAttribute("routeForm", new com.gustavoreinaldi.dev_utils.model.dtos.RouteForm());
        model.addAttribute("mockForm", new com.gustavoreinaldi.dev_utils.model.dtos.MockForm());

        return "dashboard";
    }

    @PostMapping
    public String create(@ModelAttribute("submitCollectionForm") CollectionForm form) {
        ProjectCollection collection = ProjectCollection.builder()
                .name(form.getName())
                .description(form.getDescription())
                .build();
        projectCollectionRepository.save(collection);
        return "redirect:/collections/" + collection.getId();
    }

    @PostMapping("/{id}")
    public String update(@PathVariable Long id, @jakarta.validation.Valid @ModelAttribute CollectionForm form,
            @RequestParam(value = "globalFallbackUrl", required = false) String globalFallbackUrl,
            org.springframework.validation.BindingResult bindingResult) {

        // Update global config
        GlobalConfig config = globalConfigRepository.findById(1L)
                .orElse(GlobalConfig.builder().id(1L).build());
        config.setFallbackUrl(globalFallbackUrl);
        globalConfigRepository.save(config);

        if (bindingResult.hasErrors()) {
            return "redirect:/collections/" + id;
        }

        ProjectCollection collection = projectCollectionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid collection Id:" + id));

        // Update fields
        collection.setName(form.getName());
        collection.setDescription(form.getDescription());

        projectCollectionRepository.save(collection);

        return "redirect:/collections/" + id;
    }

    @DeleteMapping("/{id}")
    public String delete(@PathVariable Long id) {
        projectCollectionRepository.deleteById(id);
        return "redirect:/collections";
    }
}
