package com.gustavoreinaldi.dev_utils.repository;

import com.gustavoreinaldi.dev_utils.model.entities.GlobalConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GlobalConfigRepository extends JpaRepository<GlobalConfig, Long> {
}
