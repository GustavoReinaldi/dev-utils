package com.gustavoreinaldi.dev_utils.model.dtos.export;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProjectCollectionExportDTO {
    private String name;
    private String description;
    private List<RouteConfigExportDTO> routeConfigs;
    private List<MockConfigExportDTO> mockConfigs;
}
