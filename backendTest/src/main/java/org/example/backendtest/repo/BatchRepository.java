package org.example.backendtest.repo;

import org.example.backendtest.entity.Batch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BatchRepository extends JpaRepository<Batch, Long> {

    @Query("SELECT b.sourceId FROM Batch b WHERE b.sourceId IN :sourceIds")
    List<Long> findProcessedSourceIds(@Param("sourceIds") List<Long> sourceIds);
}
