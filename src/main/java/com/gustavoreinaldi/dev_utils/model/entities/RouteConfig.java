package com.gustavoreinaldi.dev_utils.model.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "route_config")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RouteConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String pathOrigem;

    private String targetHost;

    private Boolean isActive;

    @ManyToOne
    @JoinColumn(name = "project_collection_id")
    private ProjectCollection projectCollection;
}
