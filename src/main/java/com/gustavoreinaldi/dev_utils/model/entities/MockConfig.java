package com.gustavoreinaldi.dev_utils.model.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "mock_config")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MockConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String path;

    private String httpMethod;

    private Integer statusCode;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String responseBody;

    private Boolean isActive;

    @ManyToOne
    @JoinColumn(name = "project_collection_id")
    private ProjectCollection projectCollection;
}
