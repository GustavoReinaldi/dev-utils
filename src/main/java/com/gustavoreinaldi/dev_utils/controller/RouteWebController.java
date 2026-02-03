package com.gustavoreinaldi.dev_utils.controller;

import com.gustavoreinaldi.dev_utils.model.entities.ProjectCollection;
import com.gustavoreinaldi.dev_utils.model.entities.RouteConfig;
import com.gustavoreinaldi.dev_utils.repository.ProjectCollectionRepository;
import com.gustavoreinaldi.dev_utils.repository.RouteConfigRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import com.gustavoreinaldi.dev_utils.model.dtos.RouteForm;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/routes")
@RequiredArgsConstructor
public class RouteWebController {

        private final RouteConfigRepository routeConfigRepository;
        private final ProjectCollectionRepository projectCollectionRepository;

        @PostMapping("/collection/{collectionId}")
        public String addRoute(@PathVariable Long collectionId,
                        @jakarta.validation.Valid @ModelAttribute RouteForm form,
                        org.springframework.validation.BindingResult bindingResult) {
                if (bindingResult.hasErrors()) {
                        return "redirect:/collections/" + collectionId; // Redirect back on error
                }
                ProjectCollection collection = projectCollectionRepository.findById(collectionId)
                                .orElseThrow(() -> new IllegalArgumentException(
                                                "Invalid collection Id:" + collectionId));

                RouteConfig routeConfig = RouteConfig.builder()
                                .pathOrigem(form.getPathOrigem())
                                .targetHost(form.getTargetHost())
                                .projectCollection(collection)
                                .isActive(true)
                                .build();
                routeConfigRepository.save(routeConfig);

                return "redirect:/collections/" + collectionId;
        }

        @DeleteMapping("/{id}")
        public String deleteRoute(@PathVariable Long id) {
                RouteConfig route = routeConfigRepository.findById(id)
                                .orElseThrow(() -> new IllegalArgumentException("Invalid route Id:" + id));
                Long collectionId = route.getProjectCollection().getId();

                routeConfigRepository.deleteById(id);

                return "redirect:/collections/" + collectionId;
        }

        @PatchMapping("/{id}/toggle")
        public String toggleRoute(@PathVariable Long id, Model model) {
                RouteConfig route = routeConfigRepository.findById(id)
                                .orElseThrow(() -> new IllegalArgumentException("Invalid route Id:" + id));

                route.setIsActive(!route.getIsActive());
                routeConfigRepository.save(route);

                // Return the updated route row or just the button?
                // For simplicity, let's assume we reload the whole list or just redirect for
                // now if not using fragments effectively yet.
                // Actually, let's return a "fragments/route-list :: route-row"
                // But for this step, let's just validly update and redirect, allowing HTMX to
                // handle the redirect or I can implement the fragment return later when I have
                // the template.
                // Let's assume we'll use hx-target="closest tr" and return a fragment.

                model.addAttribute("route", route);
                return "fragments/route-list :: route-row(route=${route})";
        }
}
