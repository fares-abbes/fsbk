package org.example.backendtest.controller;

import org.example.backendtest.dto.BatchDto;
import org.example.backendtest.entity.Batch;
import org.example.backendtest.repo.BatchRepository;
import org.example.backendtest.service.BatchProcessorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/batches")
public class BatchController {

    @Autowired
    private BatchRepository batchRepository;

    @Autowired
    private BatchProcessorService batchProcessorService;

    @GetMapping
    public ResponseEntity<List<BatchDto>> getAllBatches() {
        List<Batch> batches = batchRepository.findAll();
        List<BatchDto> batchDtos = batches.stream()
            .map(batchProcessorService::convertToDto)
            .collect(Collectors.toList());
        return ResponseEntity.ok(batchDtos);
    }

    @GetMapping("/{id}")
    public ResponseEntity<BatchDto> getBatchById(@PathVariable Long id) {
        return batchRepository.findById(id)
            .map(batch -> ResponseEntity.ok(batchProcessorService.convertToDto(batch)))
            .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/process")
    public ResponseEntity<String> triggerManualProcessing() {
        try {
            batchProcessorService.processNewBatches();
            return ResponseEntity.ok("Batch processing triggered successfully");
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body("Error during batch processing: " + e.getMessage());
        }
    }
}
