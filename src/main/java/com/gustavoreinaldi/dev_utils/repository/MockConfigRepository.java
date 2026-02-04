package com.gustavoreinaldi.dev_utils.repository;

import com.gustavoreinaldi.dev_utils.model.entities.MockConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MockConfigRepository extends JpaRepository<MockConfig, Long> {

    @Query("SELECT m FROM MockConfig m WHERE m.projectCollection.id = :collectionId AND m.path = :path AND m.httpMethod = :method AND m.isActive = true")
    Optional<MockConfig> findActiveMockConfig(Long collectionId, String path, String method);

    @Query("SELECT m FROM MockConfig m WHERE m.path = :path AND m.httpMethod = :method AND m.isActive = true AND m.projectCollection.isActive = true")
    java.util.List<MockConfig> findActiveMockConfigs(String path, String method);
}
