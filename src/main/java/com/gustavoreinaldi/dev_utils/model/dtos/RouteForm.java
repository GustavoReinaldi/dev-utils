package com.gustavoreinaldi.dev_utils.model.dtos;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RouteForm {
        @NotBlank(message = "Path is required")
        private String pathOrigem;
        @NotBlank(message = "Target Host is required")
        private String targetHost;
}
