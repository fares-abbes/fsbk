package org.example.backendtest.service;

import org.example.backendtest.dto.BatchDto;
import org.example.backendtest.entity.Batch;
import org.example.backendtest.entity.BatchesFc;
import org.example.backendtest.repo.BatchRepository;
import org.example.backendtest.repo.BatchesFcRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class BatchProcessorService {

    private static final Logger logger = LoggerFactory.getLogger(BatchProcessorService.class);

    @Autowired
    private BatchesFcRepository batchesFcRepository;

    @Autowired
    private BatchRepository batchRepository;

    @Scheduled(fixedRate = 60000) // Run every 60 seconds
    @Transactional
    public void processNewBatches() {
        logger.info("Starting batch processing job at {}", LocalDateTime.now());

        try {
            // Get all records from batches_fc
            List<BatchesFc> allBatchesFc = batchesFcRepository.findAll();
            
            if (allBatchesFc.isEmpty()) {
                logger.info("No records found in batches_fc table");
                return;
            }

            // Get already processed source IDs
            List<Long> processedSourceIds = batchRepository.findProcessedSourceIds(
                allBatchesFc.stream()
                    .map(BatchesFc::getId)
                    .collect(Collectors.toList())
            );

            // Filter only new records
            List<BatchesFc> newBatches = allBatchesFc.stream()
                .filter(batchesFc -> !processedSourceIds.contains(batchesFc.getId()))
                .collect(Collectors.toList());

            if (newBatches.isEmpty()) {
                logger.info("No new records to process");
                return;
            }

            logger.info("Found {} new records to process", newBatches.size());

            // Convert and save new records
            List<Batch> batchesToSave = newBatches.stream()
                .map(this::convertToBatch)
                .collect(Collectors.toList());

            batchRepository.saveAll(batchesToSave);
            
            logger.info("Successfully processed {} new records", newBatches.size());

        } catch (Exception e) {
            logger.error("Error occurred during batch processing", e);
        }
    }

    private Batch convertToBatch(BatchesFc batchesFc) {
        Batch batch = new Batch();
        batch.setBatchId(batchesFc.getBatchId());
        batch.setBatchDate(batchesFc.getBatchDate());
        batch.setBatchFunction(batchesFc.getBatchFunction());
        batch.setBatchName(batchesFc.getBatchName());
        batch.setFileLocation(batchesFc.getFileLocation());
        batch.setProcessedDate(LocalDateTime.now());
        batch.setSourceId(batchesFc.getId());
        return batch;
    }

    public BatchDto convertToDto(Batch batch) {
        BatchDto dto = new BatchDto();
        dto.setBatchId(batch.getBatchId());
        dto.setBatchDate(batch.getBatchDate());
        dto.setBatchFunction(batch.getBatchFunction());
        dto.setBatchName(batch.getBatchName());
        dto.setFileLocation(batch.getFileLocation());
        return dto;
    }
}
