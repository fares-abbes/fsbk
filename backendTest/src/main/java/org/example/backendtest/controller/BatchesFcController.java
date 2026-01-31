package org.example.backendtest.controller;

import org.example.backendtest.entity.BatchesFc;
import org.example.backendtest.service.BatchesFcService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/batchesfc")
public class BatchesFcController {

    private final BatchesFcService batchesFcService;

    @Autowired
    public BatchesFcController(BatchesFcService batchesFcService) {
        this.batchesFcService = batchesFcService;
    }

    @PostMapping("/add")
    public ResponseEntity<BatchesFc> addBatch(@RequestBody BatchesFc batch) {
        BatchesFc savedBatch = batchesFcService.addBatch(batch);
        return ResponseEntity.ok(savedBatch);
    }
}
