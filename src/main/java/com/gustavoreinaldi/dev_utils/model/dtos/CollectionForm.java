package com.gustavoreinaldi.dev_utils.model.dtos;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class CollectionForm {
    @NotBlank(message = "Name is required")
    private String name;
    private String description;
    private String fallbackUrl;

    // Explicit setters
    public void setName(String name) { this.name = name; }
    public void setDescription(String description) { this.description = description; }
    public void setFallbackUrl(String fallbackUrl) { this.fallbackUrl = fallbackUrl; }

    public String getName() { return name; }
    public String getDescription() { return description; }
    public String getFallbackUrl() { return fallbackUrl; }
}
