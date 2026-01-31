package org.example.backendtest.repo;

import org.example.backendtest.entity.BatchesFc;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource(collectionResourceRel = "batchesfc", path = "batchesfc")
public interface BatchesFcRepository extends JpaRepository<BatchesFc, Long> {
}
