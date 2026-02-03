package com.gustavoreinaldi.dev_utils.model.dtos.export;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MockConfigExportDTO {
    private String path;
    private String httpMethod;
    private Integer statusCode;
    private String responseBody;
    private Boolean isActive;
}
