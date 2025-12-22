package com.gustavoreinaldi.dev_utils.model.dtos;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class RouteForm {
    @NotBlank(message = "Path is required")
    private String pathOrigem;
    @NotBlank(message = "Target Host is required")
    private String targetHost;

    public void setPathOrigem(String pathOrigem) {
        this.pathOrigem = pathOrigem;
    }

    public void setTargetHost(String targetHost) {
        this.targetHost = targetHost;
    }

    public String getPathOrigem() {
        return pathOrigem;
    }

    public String getTargetHost() {
        return targetHost;
    }
}
