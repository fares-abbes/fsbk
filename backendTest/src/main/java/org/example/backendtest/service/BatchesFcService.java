package org.example.backendtest.service;

import org.example.backendtest.entity.BatchesFc;
import org.example.backendtest.repo.BatchesFcRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class BatchesFcService {

    private final BatchesFcRepository batchesFcRepository;

    @Autowired
    public BatchesFcService(BatchesFcRepository batchesFcRepository) {
        this.batchesFcRepository = batchesFcRepository;
    }

    public BatchesFc addBatch(BatchesFc batch) {
        return batchesFcRepository.save(batch);
    }
}
