package com.mss.backOffice.services;

import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mss.unified.entities.BatchesFC;
import com.mss.unified.entities.BatchesHistory;
import com.mss.unified.repositories.BatchesFFCRepository;
import com.mss.unified.repositories.BatchesHistoryRepository;

/**
 * Service responsible for syncing BatchesFC records to BatchesHistory.
 * Runs a scheduled job every minute to check for new or updated batches
 * and creates corresponding history records.
 */
@Service
public class batchHistoryService {

    private static final Logger logger = LoggerFactory.getLogger(batchHistoryService.class);

    @Autowired
    private BatchesFFCRepository batchesFCRepository;

    @Autowired
    private BatchesHistoryRepository batchesHistoryRepository;

    /**
     * Scheduled job that runs every minute to sync BatchesFC to BatchesHistory.
     * Checks for new records in BatchesFC and creates history entries if they don't exist.
     */
    @Scheduled(cron = "0 * * * * *") // Every minute at second 0
    @Transactional
    public void syncBatchesToHistory() {
        logger.info("Starting batch history sync job at {}", new Date());
        
        try {
            List<BatchesFC> allBatches = batchesFCRepository.findAll();
            int syncedCount = 0;
            
            for (BatchesFC batch : allBatches) {
                if (shouldCreateHistoryRecord(batch)) {
                    createHistoryRecord(batch);
                    syncedCount++;
                }
            }
            
            if (syncedCount > 0) {
                logger.info("Synced {} new batch records to history", syncedCount);
            } else {
                logger.debug("No new batch records to sync");
            }
            
        } catch (Exception e) {
            logger.error("Error during batch history sync: {}", e.getMessage(), e);
        }
        
        logger.info("Batch history sync job completed at {}", new Date());
    }

    /**
     * Determines if a history record should be created for the given batch.
     * A record is created if no history exists for this batch with the same lastExecution date.
     */
    private boolean shouldCreateHistoryRecord(BatchesFC batch) {
        if (batch.getBatchLastExcution() == null) {
            return false;
        }
        
        return !batchesHistoryRepository.existsByBatchIdAndLastExecution(
                batch.getBatchId(), 
                batch.getBatchLastExcution()
        );
    }

    /**
     * Creates a new BatchesHistory record from a BatchesFC record.
     * Syncs: batchName, batchId (via relation), batchLastExecution, batchType
     * Initializes: batchHStartDate, batchHEndDate, status, exception, erreur, bypassStatus
     */
    private void createHistoryRecord(BatchesFC batch) {
        BatchesHistory history = new BatchesHistory();
        
        // Sync from BatchesFC
        history.setBatch(batch);
        history.setBatchName(batch.getBatchName());
        history.setBatchType(batch.getBatchType());
        history.setBatchLastExecution(batch.getBatchLastExcution());
        
        // Set execution-specific data from BatchesFC
        history.setBatchHStartDate(batch.getBatchDate());
        history.setBatchHEndDate(batch.getBatchEndDate());
        history.setStatus(batch.getBatchStatus());
        history.setErreur(batch.getError());
        history.setException(batch.getErrorStackTrace());
        history.setBypassStatus(0); // Default bypass status
        
        batchesHistoryRepository.save(history);
        
        logger.debug("Created history record for batch: {} (ID: {})", 
                batch.getBatchName(), batch.getBatchId());
    }

    /**
     * Manually trigger sync for a specific batch by its key.
     * Useful for on-demand syncing.
     */
    @Transactional
    public void syncBatchByKey(String key) {
        batchesFCRepository.findByKey(key).ifPresent(batch -> {
            if (shouldCreateHistoryRecord(batch)) {
                createHistoryRecord(batch);
                logger.info("Manually synced batch with key: {}", key);
            }
        });
    }

    /**
     * Get all history records for a specific batch.
     */
    public List<BatchesHistory> getHistoryByBatchId(int batchId) {
        return batchesHistoryRepository.findByBatchId(batchId);
    }

    /**
     * Get all history records with a specific status.
     */
    public List<BatchesHistory> getHistoryByStatus(Integer status) {
        return batchesHistoryRepository.findByStatus(status);
    }

    /**
     * Get all history records.
     */
    public List<BatchesHistory> getAllHistory() {
        return batchesHistoryRepository.findAll();
    }

    /**
     * Update bypass status for a history record.
     */
    @Transactional
    public void updateBypassStatus(Long historyId, Integer bypassStatus) {
        batchesHistoryRepository.findById(historyId).ifPresent(history -> {
            history.setBypassStatus(bypassStatus);
            batchesHistoryRepository.save(history);
            logger.info("Updated bypass status to {} for history ID: {}", bypassStatus, historyId);
        });
    }
}
