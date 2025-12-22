package com.gustavoreinaldi.dev_utils.model.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MockForm {
        @NotBlank
        private String path;
        @NotBlank
        private String httpMethod;
        @NotNull
        private Integer statusCode;
        private String responseBody;
}
