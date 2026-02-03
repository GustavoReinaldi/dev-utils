package com.gustavoreinaldi.dev_utils.model.dtos.export;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RouteConfigExportDTO {
    private String pathOrigem;
    private String targetHost;
    private Boolean isActive;
}
