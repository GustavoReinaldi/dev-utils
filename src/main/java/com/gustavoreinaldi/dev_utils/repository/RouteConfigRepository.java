package com.gustavoreinaldi.dev_utils.repository;

import com.gustavoreinaldi.dev_utils.model.entities.RouteConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RouteConfigRepository extends JpaRepository<RouteConfig, Long> {

    @Query("SELECT r FROM RouteConfig r WHERE r.projectCollection.id = :collectionId AND r.isActive = true")
    List<RouteConfig> findActiveRoutesByCollection(Long collectionId);

    @Query("SELECT r FROM RouteConfig r WHERE r.isActive = true")
    List<RouteConfig> findAllActiveRoutes();
}
