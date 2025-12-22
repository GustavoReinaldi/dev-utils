package com.gustavoreinaldi.dev_utils.repository;

import com.gustavoreinaldi.dev_utils.model.entities.ProjectCollection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProjectCollectionRepository extends JpaRepository<ProjectCollection, Long> {
}
