package com.gustavoreinaldi.dev_utils.model.entities;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Table(name = "project_collection")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProjectCollection {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private String description;

    @Builder.Default
    private Boolean isActive = true;

    @OneToMany(mappedBy = "projectCollection", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RouteConfig> routeConfigs;

    @OneToMany(mappedBy = "projectCollection", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MockConfig> mockConfigs;
}
