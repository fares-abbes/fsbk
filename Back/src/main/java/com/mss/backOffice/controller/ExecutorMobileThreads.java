package com.mss.backOffice.controller;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.*;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import com.mss.backOffice.exception.ExceptionMethod;
import com.mss.backOffice.exception.ResourceNotFoundException;
import com.mss.backOffice.request.FileUAPRequest;
import com.mss.backOffice.services.PropertyService;
import com.mss.unified.entities.*;
import com.mss.unified.projection.MerchantAccountribDto;
import com.mss.unified.repositories.*;
import org.apache.commons.beanutils.PropertyUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import com.mss.backOffice.services.AsyncProcessingService;
import com.mss.backOffice.enumType.FileStatusEnum;

import java.nio.charset.StandardCharsets;
import java.nio.charset.Charset;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.CharsetDecoder;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.FileInputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.stream.Stream;

import static com.mss.backOffice.controller.CheckCroController040.moveFilesByStartingName;
import static com.mss.backOffice.controller.ReglementGlobalController.convertDate;

@RestController
@RequestMapping("executorMobileThreads")
public class ExecutorMobileThreads {
    @Autowired
    DownloadFileBc write;
    private static final Logger logger = LoggerFactory.getLogger(ExecutorMobileThreads.class);
    @Autowired
    MvbkConfigRepository mvbkConfigRepo;
    @Autowired
    BkmvtiMPFransaBankRepository bkmRepo;
    @Autowired
    BankSettlementController bankSettlementController;

    @Autowired
    private AsyncProcessingService asyncProcessingService;
    // Static map to store file content data for async tasks
    public static Map<Integer, List<FileContentM>> fileContentMap = new HashMap<>();
    // Static map to store MerchantAccountribDto keyed by accountrib (thread-safe for async usage)
    public static Map<String, MerchantAccountribDto> merchantAccountribMap = new ConcurrentHashMap<String, MerchantAccountribDto>();
    public static Map<String, List<CommissionMobile>> commissionByTypeMap = new ConcurrentHashMap<String, List<CommissionMobile>>();
    public static List<DayOperationMP> dayOperations = new ArrayList<>();
    @Autowired
    private FileHeaderMRepository fileHeaderMRepository;
    public static List<SettelementFransaBank> settlementFransaBankList = new ArrayList<>();
    @Autowired
    private DayOperationMPRepository dayOperationMPRepository;
    @Autowired
    PropertyService propertyService;
    @Autowired
    private UAP070FransaBankRepository uap070FransaBankRepository;

    @Autowired
    private FileContentMRepository fileContentMRepository;
    public static final int minimumAmount = 2000;

    @Autowired
    MerchantRepository merchantRepository;
    @Autowired
    CodeBankBCRepository codeBankBCRepository;

    @Autowired
    CommissionMobileRepository commissionMobileRepository ;
    @Autowired
      SettelementFransaBankRepository settelementFransaBankRepository;
    @Autowired
    UAP070INRepository uap070INRepository;
    @Autowired
    UAP070FransaBankHistoryRepository uap070FransaBankHistoryRepository;
    @Autowired
    UAP070INHistoryRepository uap070INHistoryRepository;
    @Autowired
    BkmvtiMPFransaBankHRepository bkmvtiMPFransaBankHRepository;
    @Autowired
    DayOperationMPHRepository dayOperationMPHRepository;
    @Autowired
      BatchesFFCRepository batchesFFCRepository;
    @Autowired
    DownloadFileBc  downloadFileBC;
    public static String name;
    // Start all mobile flows (P2B,B2B,B2P,P2G,B2G,G2P) for both roles and stop others if one fails
    @GetMapping("/start-all-stop-on-error")
    public ResponseEntity<String> startAllStopOnError() {
          name = SecurityContextHolder.getContext().getAuthentication().getName();

        AtomicBoolean stopSignal = new AtomicBoolean(false);
        List<CompletableFuture<String>> futures = new ArrayList<>();
        BatchesFC batche = batchesFFCRepository.findByKey("ExecuteM").get();
        batche.setBatchStatus(0);
        batchesFFCRepository.save(batche);
        BatchesFC batcheStatus = batchesFFCRepository.findByKey("execStatusM").get();
        batcheStatus.setBatchNumber(1);
        batchesFFCRepository.save(batcheStatus);
        try{
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
            LocalDate localDate = LocalDate.now();
            LocalTime currentTime = LocalTime.now();
            LocalTime cutoffTime = LocalTime.of(11, 59);

            // If time is before 11:59 AM, use yesterday's date
            if (currentTime.isBefore(cutoffTime)) {
                localDate = localDate.minusDays(1);
            }

            String regDate = localDate.format(formatter);
            reglement070Out(regDate);
            reglement070In(regDate);
            reglementGlobal(regDate);
            archiveMobile(regDate);
        } catch (Exception e) {
             batche.setBatchStatus(2);
            batchesFFCRepository.save(batche);
            throw new RuntimeException(e);
        }

        try {
            // Step 1: Prepare data - get FileHeaderM with status INTEGRATED and build the map
            logger.info("Preparing file content data...");
            List<FileHeaderM> integratedFiles = fileHeaderMRepository.findByStatusFile(FileStatusEnum.INTEGRATED.getValue());

            if (integratedFiles.isEmpty()) {
                logger.info("No integrated files found. Stopping process.");
                batche.setBatchStatus(1);
                batchesFFCRepository.save(batche);
                return ResponseEntity.ok("No integrated files found.");
            }

            List<MerchantAccountribDto> merchant = merchantRepository.findAllMerchantAccountribDto();

            // Clear previous data
            fileContentMap.clear();

            // Build the map: FileHeaderM.id -> List<FileContentM>
            for (FileHeaderM fileHeader : integratedFiles) {
                List<FileContentM> fileContents = fileContentMRepository.findBySummaryCode(fileHeader.getId());
                fileContentMap.put(fileHeader.getId(), fileContents);
                logger.info("Loaded {} file contents for FileHeaderM id: {}", fileContents.size(), fileHeader.getId());
            }

            // Load all the settlements
            logger.info("Loading all the settlements...");
            settlementFransaBankList = settelementFransaBankRepository.findAll();

            // Build/refresh the merchant map: accountrib -> MerchantAccountribDto
            merchantAccountribMap.clear();
            for (MerchantAccountribDto m : merchant) {
                // Assuming MerchantAccountribDto exposes getAccountrib(); adjust if different
                if (m != null && m.getAccountrib() != null) {
                    merchantAccountribMap.put(m.getAccountrib(), m);
                }
            }
            List<CommissionMobile>  commMP=commissionMobileRepository.findAll();
            
            // Group CommissionMobile objects by their type value
            // Note: Multiple CommissionMobile objects can have the same type value
              commissionByTypeMap = commMP.stream()
                .collect(Collectors.groupingBy(CommissionMobile::getType));
            
            logger.info("Grouped {} CommissionMobile objects into {} different types", 
                commMP.size(), commissionByTypeMap.size());

            logger.info("Loaded {} merchants into merchantAccountribMap", merchantAccountribMap.size());

            logger.info("Data preparation complete. Total FileHeaderM records: {}, Map size: {}",
                    integratedFiles.size(), fileContentMap.size());

            // 15 tasks: various flows with acquirer/emitter roles
            futures.add(asyncProcessingService.p2bOnUsAA(stopSignal));
            futures.add(asyncProcessingService.p2bOnUsMA(stopSignal));
            futures.add(asyncProcessingService.p2bAcquirer(stopSignal));
            futures.add(asyncProcessingService.p2bEmitter(stopSignal));

            futures.add(asyncProcessingService.p2pOnUsAA(stopSignal));
            futures.add(asyncProcessingService.p2pOnUsMA(stopSignal));
            futures.add(asyncProcessingService.p2pAcquirer(stopSignal));
            futures.add(asyncProcessingService.p2pEmitter(stopSignal));

            futures.add(asyncProcessingService.b2bOnUsAA(stopSignal));
            futures.add(asyncProcessingService.b2bOnUsMA(stopSignal));
            futures.add(asyncProcessingService.b2bEmitter(stopSignal));
            futures.add(asyncProcessingService.b2bAcquireur(stopSignal));

            futures.add(asyncProcessingService.b2pOnUsAA(stopSignal));
            futures.add(asyncProcessingService.b2pOnUsMA(stopSignal));
            futures.add(asyncProcessingService.b2pAcquirer(stopSignal));
            futures.add(asyncProcessingService.b2pEmitter(stopSignal));

//            futures.add(asyncProcessingService.p2gAcquirer(stopSignal));
            futures.add(asyncProcessingService.p2gEmitter(stopSignal));

//            futures.add(asyncProcessingService.b2gAcquirer(stopSignal));
            futures.add(asyncProcessingService.b2gEmitter(stopSignal));

            futures.add(asyncProcessingService.g2pAcquirer(stopSignal));
//            futures.add(asyncProcessingService.g2pEmitter(stopSignal));
            futures.add(asyncProcessingService.chargeBacksAcquirer(stopSignal));
            futures.add(asyncProcessingService.chargeBacksEmitter(stopSignal));

            // Attach error handler to each future: on error -> signal stop and cancel others
            for (CompletableFuture<String> f : futures) {
                f.whenComplete((val, ex) -> {
                    if (ex != null) {
                        logger.error("A task failed; signalling others to stop", ex);
                        stopSignal.set(true);

                        // attempt to cancel all
                        futures.forEach(ff -> ff.cancel(true));
                        batche.setBatchStatus(2);
                        batche.setError(ex.getLocalizedMessage());
                        batchesFFCRepository.save(batche);
                    }
                });
            }

            // Setup completion handler - this runs after ALL threads complete
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                    .whenCompleteAsync((v, ex) -> {
                        if (ex != null) {
                            logger.error("At least one task failed", ex);
                            batche.setBatchStatus(2);
                            batche.setError(ex.getLocalizedMessage());

                            batchesFFCRepository.save(batche);
                        } else {
                            logger.info("All {} tasks completed successfully", futures.size());
                        dayOperationMPRepository.saveAll(dayOperations) ;
                        }

                        
                        // Start the new thread after all processing threads complete
                        logger.info("Starting post-processing thread...");
                        // For now, just log the thread start as requested
                        CompletableFuture.runAsync(() -> {
                            logger.info("Post-processing thread started - ready for additional operations");
                            try {
                                bankSettlementController.generateBankSettCode();
                                processSettlementOperationsUap070();
                                processSettlementOperationsUap070In();
                                generateCompensationFile("CRA");
                                batche.setBatchStatus(1);
                                batchesFFCRepository.save(batche);
                            } catch (Exception e) {
                                batche.setBatchStatus(2);
                                batche.setError(e.getLocalizedMessage());
                                logger.info("Error.....");

                                batchesFFCRepository.save(batche);
                                throw new RuntimeException(e);
                            }

                            // Future implementation will go here
                        });
                    });

            logger.info("Successfully submitted {} tasks (fire-and-forget mode with stop-on-error enabled)", futures.size());
            return ResponseEntity.ok("Submitted " + futures.size() + " tasks in fire-and-forget mode");
            
        } catch (Exception e) {
            stopSignal.set(true);
            futures.forEach(f -> f.cancel(true));
            logger.error("Failed to submit all tasks", e);
            batche.setBatchStatus(2);
            batchesFFCRepository.save(batche);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to start all tasks");
        }
    }
    @GetMapping(value = "writeInORD")
    public @ResponseBody ResponseEntity<FileUAPRequest> writeInORD( Path filePath, String sequenceNumber)
            throws ResourceNotFoundException, ExceptionMethod, IOException {

        CodeBankBC codeBankBC = codeBankBCRepository.findIdentifiant();
        StringBuilder str = new StringBuilder("");
        FileUAPRequest fileRequest = new FileUAPRequest();
        String destinationBank = codeBankBC.getIdentifiant();
        String Entete = "ORD" + sequenceNumber + "INLOT" + "000" + sequenceNumber + "070" + "DZD" + getSpace(41) + "\n";
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
        String Date = dateFormat.format(new Date());
        String fileName = destinationBank + "." + sequenceNumber + "." + Date + "." + "ORD";
        str.append(Entete);
        fileRequest.setData(str.toString());
        fileRequest.setNameTitle(fileName);
        // Before line 813:
        if (Files.exists(filePath)) {
            Files.delete(filePath);
            logger.info("Deleted existing file: {}", filePath);
        }
        Files.write(filePath, str.toString().getBytes(StandardCharsets.UTF_8), StandardOpenOption.CREATE_NEW);

         return ResponseEntity.ok().body(fileRequest);
    }



    /**
     * Static method to find a SettelementFransaBank by identification
     * @param identification The identification string to search for
     * @return The matching SettelementFransaBank object, or null if not found
     */
    public static SettelementFransaBank getSettlementByIdentification(String identification) {
        if (identification == null || settlementFransaBankList == null) {
            return null;
        }
        
        return settlementFransaBankList.stream()
            .filter(settlement -> identification.equals(settlement.getIdentificationbh()))
            .findFirst()
            .orElse(null);
    }




    /**
     * Create a list of DayOperation070IN objects from a list of DayOperationMP objects
     * @param dayOperationMPList The list of DayOperationMP objects to create from
     * @return The list of DayOperation070IN objects
     */
    public static Map<String, UAP070FransaBank> createDayOperation070List(List<DayOperationMP> dayOperationMPList) {
        return dayOperationMPList.stream()
                .filter(dayOperation -> dayOperation.getNumRefTransaction() != null)
                .collect(Collectors.toMap(
                        DayOperationMP::getNumRefTransaction,
                        developer -> {
                            UAP070FransaBank historique = new UAP070FransaBank();
                            
                            // Handle problematic fields separately
                            String originalMontantTransaction = developer.getMontantTransaction();
                            Integer originalSummaryCode = developer.getSummaryCode();
                            
                            // Temporarily set problematic fields to null to avoid type conversion issues
                            developer.setMontantTransaction(null);
                            developer.setSummaryCode(null);
                            
                            try {
                                // Copy all other properties that are compatible
                                PropertyUtils.copyProperties(historique, developer);
                            } catch (Exception ex) {
                                logger.warn("Failed to copy some properties: {}", ex.getMessage());
                            } finally {
                                // Restore the original values
                                developer.setMontantTransaction(originalMontantTransaction);
                                developer.setSummaryCode(originalSummaryCode);
                            }
                            
                            // Handle field conversions separately
                            try {
                                if (originalMontantTransaction != null) {
                                    historique.setMontantTransaction(new BigDecimal(originalMontantTransaction));
                                }
                                if (originalSummaryCode != null) {
                                    historique.setSummaryCode(String.valueOf(originalSummaryCode));
                                }
                            } catch (Exception ex) {
                                logger.warn("Failed to convert fields: {}", ex.getMessage());
                            }
                            String concatenated = historique.getNumRefTransaction() + historique.getNumAutorisation();
                            String numRefArchivage = String.format("%-18s", concatenated.length() > 18 ? concatenated.substring(0, 18) : concatenated);
                            historique.setNumRefArchivage(numRefArchivage);
                            return historique;
                        },
                        (existing, replacement) -> existing
                ));
    }
    public static Map<String, UAP070IN> createDayOperation070INList(List<DayOperationMP> dayOperationMPList) {
        return dayOperationMPList.stream()
                .filter(dayOperation -> dayOperation.getNumRefTransaction() != null)
                .collect(Collectors.toMap(
                        DayOperationMP::getNumRefTransaction,
                        developer -> {
                            UAP070IN historique = new UAP070IN();
                            
                            // Handle problematic fields separately
                            String originalMontantTransaction = developer.getMontantTransaction();
                            Integer originalSummaryCode = developer.getSummaryCode();
                            
                            // Temporarily set problematic fields to null to avoid type conversion issues
                            developer.setMontantTransaction(null);
                            developer.setSummaryCode(null);
                            
                            try {
                                // Copy all other properties that are compatible
                                PropertyUtils.copyProperties(historique, developer);
                            } catch (Exception ex) {
                                logger.warn("Failed to copy some properties: {}", ex.getMessage());
                            } finally {
                                // Restore the original values
                                developer.setMontantTransaction(originalMontantTransaction);
                                developer.setSummaryCode(originalSummaryCode);
                            }
                            
                            // Handle field conversions separately
                            try {
                                if (originalMontantTransaction != null) {
                                    historique.setMontantTransaction(new BigDecimal(originalMontantTransaction));
                                }
                                if (originalSummaryCode != null) {
                                    historique.setSummaryCode(String.valueOf(originalSummaryCode));
                                }
                            } catch (Exception ex) {
                                logger.warn("Failed to convert fields: {}", ex.getMessage());
                            }
                            
                            String concatenated = historique.getNumRefTransaction() + historique.getNumAutorisation();
                            String numRefArchivage = String.format("%-18s", concatenated.length() > 18 ? concatenated.substring(0, 18) : concatenated);
                            historique.setNumRefArchivage(numRefArchivage);
                            historique.setTypeTransaction(developer.getTypeTransaction());
                            return historique;
                        },
                        (existing, replacement) -> existing
                ));
    }

    /**
     * Process settlement operations by getting DayOperationMP records,
     * creating UAP070FransaBank objects, and saving them
     * @return ResponseEntity with the result message
     */
    @GetMapping("/process-settlement-operations-uap070")
    public ResponseEntity<String> processSettlementOperationsUap070() {
        try {
            logger.info("Starting settlement operations processing...");

            // Step 1: Get list of DayOperationMP using the specified method
            CodeBankBC codeBankBC = codeBankBCRepository.findById(1)
                    .orElseThrow(() -> new RuntimeException("CodeBankBC with id 1 not found"));
            
            List<DayOperationMP> dayOperationMPList = dayOperationMPRepository
                    .findDayOperationMPByTypeOperationPatternEM("settelement", codeBankBC.getIdentifiant()
                    );

            logger.info("Found {} DayOperationMP records for settlement operations", dayOperationMPList.size());

            if (dayOperationMPList.isEmpty()) {
                logger.info("No DayOperationMP records found for settlement operations");
                return ResponseEntity.ok("No settlement operations to process");
            }

            // Step 1.1: Create 4 additional lists using different typeOperationPattern values
            String bankIdentifiant = codeBankBC.getIdentifiant();
            
            List<DayOperationMP> commissionList = dayOperationMPRepository
                    .findDayOperationMPByTypeOperationPatternEM("commission", bankIdentifiant);
            logger.info("Found {} DayOperationMP records for commission operations", commissionList.size());
            
            List<DayOperationMP> tvaCommList = dayOperationMPRepository
                    .findDayOperationMPByTypeOperationPatternEM("TVA comm", bankIdentifiant);
            logger.info("Found {} DayOperationMP records for TVA comm operations", tvaCommList.size());
            
            List<DayOperationMP> interchangeList = dayOperationMPRepository
                    .findDayOperationMPByTypeOperationPatternEM("interchange", bankIdentifiant);
            logger.info("Found {} DayOperationMP records for interchange operations", interchangeList.size());
            
            List<DayOperationMP> tvaInterList = dayOperationMPRepository
                    .findDayOperationMPByTypeOperationPatternEM("TVA INTER", bankIdentifiant);
            logger.info("Found {} DayOperationMP records for TVA INTER operations", tvaInterList.size());

            // Step 2: Create list of UAP070FransaBank using the existing method
            Map<String, UAP070FransaBank> uap070Map = createDayOperation070List(dayOperationMPList);

            logger.info("Created {} UAP070FransaBank objects from DayOperationMP records", uap070Map.size());

            // Step 2.1: For each UAP070FransaBank, find associated DayOperationMP records from the 4 lists
            // Create maps for efficient lookup from the 4 lists
            Map<String, DayOperationMP> commissionMap = commissionList.stream()
                    .filter(commission -> commission.getNumRefTransaction() != null)
                    .collect(Collectors.toMap(
                            DayOperationMP::getNumRefTransaction,
                            commission -> commission,
                            (existing, replacement) -> existing
                    ));
            
            Map<String, DayOperationMP> tvaCommMap = tvaCommList.stream()
                    .filter(tvaComm -> tvaComm.getNumRefTransaction() != null)
                    .collect(Collectors.toMap(
                            DayOperationMP::getNumRefTransaction,
                            tvaComm -> tvaComm,
                            (existing, replacement) -> existing
                    ));
            
            Map<String, DayOperationMP> interchangeMap = interchangeList.stream()
                    .filter(interchange -> interchange.getNumRefTransaction() != null)
                    .collect(Collectors.toMap(
                            DayOperationMP::getNumRefTransaction,
                            interchange -> interchange,
                            (existing, replacement) -> existing
                    ));
            
            Map<String, DayOperationMP> tvaInterMap = tvaInterList.stream()
                    .filter(tvaInter -> tvaInter.getNumRefTransaction() != null)
                    .collect(Collectors.toMap(
                            DayOperationMP::getNumRefTransaction,
                            tvaInter -> tvaInter,
                            (existing, replacement) -> existing
                    ));

            // Loop through each UAP070FransaBank and find associated DayOperationMP records
            int totalAssociations = 0;
            for (Map.Entry<String, UAP070FransaBank> entry : uap070Map.entrySet()) {
                String numRefTransaction = entry.getKey();
                UAP070FransaBank uap070 = entry.getValue();
                
                logger.info("Processing UAP070FransaBank with numRefTransaction: {}", numRefTransaction);

                uap070.setMontantCommission(commissionMap.get(uap070.getNumRefTransaction()).getMontantSettlement().add(tvaCommMap.get(uap070.getNumRefTransaction()).getMontantSettlement()));
                uap070.setMontantInterchange(interchangeMap.get(uap070.getNumRefTransaction()).getMontantSettlement().add(tvaInterMap.get(uap070.getNumRefTransaction()).getMontantSettlement()));

                // Here you can process the UAP070FransaBank with its associated DayOperationMP records
                // For example, you could update fields, perform calculations, etc.
            }
            
            logger.info("Total associations found: {}", totalAssociations);

            // Step 3: Convert map values to list and save them
            List<UAP070FransaBank> uap070List = new ArrayList<>(uap070Map.values());
            List<UAP070FransaBank> savedRecords = uap070FransaBankRepository.saveAll(uap070List);

            logger.info("Successfully saved {} UAP070FransaBank records", savedRecords.size());

            return ResponseEntity.ok(String.format(
                    "Settlement operations processed successfully. " +
                            "Processed %d DayOperationMP records and saved %d UAP070FransaBank records. " +
                            "Found matching elements: Commission=%d, TVA Comm=%d, Interchange=%d, TVA INTER=%d",
                    dayOperationMPList.size(), savedRecords.size(),
                    commissionMap.size(), tvaCommMap.size(),
                    interchangeMap.size(), tvaInterMap.size()));

        } catch (Exception e) {
            logger.error("Failed to process settlement operations", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to process settlement operations: " + e.getMessage());
        }
    }
    @GetMapping("/process-settlement-operations-uap070In")
    public ResponseEntity<String> processSettlementOperationsUap070In() {
        try {
            logger.info("Starting settlement operations processing...");

            // Step 1: Get list of DayOperationMP using the specified method
            CodeBankBC codeBankBC = codeBankBCRepository.findById(1)
                    .orElseThrow(() -> new RuntimeException("CodeBankBC with id 1 not found"));
            
            List<DayOperationMP> dayOperationMPList = dayOperationMPRepository
                    .findDayOperationMPByTypeOperationPatternACQ("settelement", codeBankBC.getIdentifiant()
                    );

            logger.info("Found {} DayOperationMP records for settlement operations", dayOperationMPList.size());

            if (dayOperationMPList.isEmpty()) {
                logger.info("No DayOperationMP records found for settlement operations");
                return ResponseEntity.ok("No settlement operations to process");
            }

            // Step 1.1: Create 4 additional lists using different typeOperationPattern values
            String bankIdentifiant = codeBankBC.getIdentifiant();
            
            List<DayOperationMP> commissionList = dayOperationMPRepository
                    .findDayOperationMPByTypeOperationPatternACQ("commission", bankIdentifiant);
            logger.info("Found {} DayOperationMP records for commission operations", commissionList.size());
            
            List<DayOperationMP> tvaCommList = dayOperationMPRepository
                    .findDayOperationMPByTypeOperationPatternACQ("TVA comm", bankIdentifiant);
            logger.info("Found {} DayOperationMP records for TVA comm operations", tvaCommList.size());
            
            List<DayOperationMP> interchangeList = dayOperationMPRepository
                    .findDayOperationMPByTypeOperationPatternACQ("interchange", bankIdentifiant);
            logger.info("Found {} DayOperationMP records for interchange operations", interchangeList.size());
            
            List<DayOperationMP> tvaInterList = dayOperationMPRepository
                    .findDayOperationMPByTypeOperationPatternACQ("TVA INTER", bankIdentifiant);
            logger.info("Found {} DayOperationMP records for TVA INTER operations", tvaInterList.size());

            // Step 2: Create list of UAP070FransaBank using the existing method
            Map<String, UAP070IN> uap070Map = createDayOperation070INList(dayOperationMPList);

            logger.info("Created {} UAP070FransaBank objects from DayOperationMP records", uap070Map.size());

            // Step 2.1: For each UAP070FransaBank, find associated DayOperationMP records from the 4 lists
            // Create maps for efficient lookup from the 4 lists
            Map<String, DayOperationMP> commissionMap = commissionList.stream()
                    .filter(commission -> commission.getNumRefTransaction() != null)
                    .collect(Collectors.toMap(
                            DayOperationMP::getNumRefTransaction,
                            commission -> commission,
                            (existing, replacement) -> existing
                    ));
            
            Map<String, DayOperationMP> tvaCommMap = tvaCommList.stream()
                    .filter(tvaComm -> tvaComm.getNumRefTransaction() != null)
                    .collect(Collectors.toMap(
                            DayOperationMP::getNumRefTransaction,
                            tvaComm -> tvaComm,
                            (existing, replacement) -> existing
                    ));
            
            Map<String, DayOperationMP> interchangeMap = interchangeList.stream()
                    .filter(interchange -> interchange.getNumRefTransaction() != null)
                    .collect(Collectors.toMap(
                            DayOperationMP::getNumRefTransaction,
                            interchange -> interchange,
                            (existing, replacement) -> existing
                    ));
            
            Map<String, DayOperationMP> tvaInterMap = tvaInterList.stream()
                    .filter(tvaInter -> tvaInter.getNumRefTransaction() != null)
                    .collect(Collectors.toMap(
                            DayOperationMP::getNumRefTransaction,
                            tvaInter -> tvaInter,
                            (existing, replacement) -> existing
                    ));

            // Loop through each UAP070FransaBank and find associated DayOperationMP records
            int totalAssociations = 0;
            for (Map.Entry<String, UAP070IN> entry : uap070Map.entrySet()) {
                String numRefTransaction = entry.getKey();
                UAP070IN uap070In = entry.getValue();
                String origin = uap070In.getOrigin() != null ? uap070In.getOrigin().toUpperCase() : "";

                logger.info("Processing UAP070FransaBank with numRefTransaction: {}", numRefTransaction);

                uap070In.setMontantCommission(commissionMap.get(uap070In.getNumRefTransaction()).getMontantSettlement().add(tvaCommMap.get(uap070In.getNumRefTransaction()).getMontantSettlement()));
                uap070In.setMontantInterchange(interchangeMap.get(uap070In.getNumRefTransaction()).getMontantSettlement().add(tvaInterMap.get(uap070In.getNumRefTransaction()).getMontantSettlement()));

                BigDecimal montantComp = (origin.contains("P2P") || origin.contains("G2P") ) ?
                        uap070In.getMontantTransaction().subtract(uap070In.getMontantInterchange()) :
                        uap070In.getMontantTransaction().add(uap070In.getMontantInterchange());
                uap070In.setMontantCompensee(montantComp);
                uap070In.setMontantSettlement(montantComp);
                // Here you can process the UAP070FransaBank with its associated DayOperationMP records
                // For example, you could update fields, perform calculations, etc.
            }

            logger.info("Total associations found: {}", totalAssociations);

            // Step 3: Convert map values to list and save them
            List<UAP070IN> uap070INList = new ArrayList<>(uap070Map.values());
            List<UAP070IN> savedRecords = uap070INRepository.saveAll(uap070INList);

            logger.info("Successfully saved {} UAP070FransaBank records", savedRecords.size());

            return ResponseEntity.ok(String.format(
                    "Settlement operations processed successfully. " +
                            "Processed %d DayOperationMP records and saved %d UAP070FransaBank records. " +
                            "Found matching elements: Commission=%d, TVA Comm=%d, Interchange=%d, TVA INTER=%d",
                    dayOperationMPList.size(), savedRecords.size(),
                    commissionMap.size(), tvaCommMap.size(),
                    interchangeMap.size(), tvaInterMap.size()));

        } catch (Exception e) {
            logger.error("Failed to process settlement operations", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to process settlement operations: " + e.getMessage());
        }
    }
    public String getSpace(int count){
        String Space = "";
        for (int i = 0; i < count; i++)
            Space += " ";
        return Space;
    }
    /**
     * Generate formatted string content from UAP070IN list based on specification
     * @param uap070INList List of UAP070IN records to format
     * @return Formatted string content for the file
     */

    private String generateUAP070FileContent(List<UAP070FransaBank> uap070INList) {
        StringBuilder content = new StringBuilder();
        CodeBankBC codeBankBC = codeBankBCRepository.findIdentifiant();
        String destinationBank = codeBankBC.getIdentifiant();
        BigDecimal totalAmountFinal = new BigDecimal(0);
        int nbreLigne = uap070INList.size();
        String seqNumber = "001";


        for (UAP070FransaBank uap070 : uap070INList) {
            StringBuilder line = new StringBuilder();

            // Code operation (3N, O) - 070 for mobile payment
            line.append("070");

            // Type d'operation (2N, O) - Map type transaction codes to operation type codes
            String typeOperation = mapTypeTransaction(uap070.getOrigin());
            // Ensure 2-digit zero-padding for numeric type operation
            try {
                int typeOpNum = Integer.parseInt(typeOperation);
                line.append(String.format("%02d", typeOpNum));
            } catch (NumberFormatException e) {
                // If not numeric, pad with spaces and truncate to 2 chars
                line.append(String.format("%-2s", typeOperation.length() > 2 ? typeOperation.substring(0, 2) : typeOperation));
            }

            // Reference de l'operation (18AN, O)
            String refOperation = uap070.getNumRefArchivage() != null ? uap070.getNumRefArchivage() : "";
            line.append(String.format("%-18s", refOperation.length() > 18 ? refOperation.substring(0, 18) : refOperation));

            // Indicateur RIB (1N, O) - Default to 1
            line.append("1");

            // RIB Donneur (20AN, O) - Use numRIBEmetteur
            String ribD = uap070.getNumRIBEmetteur() != null ? uap070.getNumRIBEmetteur() : "";
            line.append(String.format("%-20s", ribD.length() > 20 ? ribD.substring(0, 20) : ribD));

            // Identifiant plateforme Donneur (10AN, O)
            String platformIdD = uap070.getIdPlateformeMobileDonneur() != null ? uap070.getIdPlateformeMobileDonneur() : "";
            line.append(String.format("%-10s", platformIdD.length() > 10 ? platformIdD.substring(0, 10) : platformIdD));

            // Date de remise (8N, O) - Format YYYYMMDD
            String dateRemise = DateTimeFormatter.ofPattern("yyyyMMdd").format(LocalDateTime.now());
            line.append(dateRemise);

            // Numero d'autorisation (15AN, O) - Padded with zeros on the left
            String numAuth = uap070.getNumAutorisation() != null ? uap070.getNumAutorisation() : "";
            // Handle numeric authorization numbers with zero-padding
            try {
                long authNum = Long.parseLong(numAuth.isEmpty() ? "0" : numAuth);
                String paddedNumAuth = String.format("%015d", authNum);
                line.append(paddedNumAuth.length() > 15 ? paddedNumAuth.substring(0, 15) : paddedNumAuth);
            } catch (NumberFormatException e) {
                // If not numeric, treat as alphanumeric and pad with spaces
                line.append(String.format("%-15s", numAuth.length() > 15 ? numAuth.substring(0, 15) : numAuth));
            }

            // Indicateur RIB Beneficiaire (1N, O) - Default to 1
            line.append("1");

            // RIB Beneficiaire (20AN, O) - Use numRIBAcquereur
            String ribB = uap070.getNumRIBAcquereur() != null ? uap070.getNumRIBAcquereur() : "";
            line.append(String.format("%-20s", ribB.length() > 20 ? ribB.substring(0, 20) : ribB));

            // Identifiant plateforme Beneficiaire (10AN, O)
            String platformIdB = uap070.getIdPlateformeMobileBeneficiaire() != null ? uap070.getIdPlateformeMobileBeneficiaire() : "";
            line.append(String.format("%-10s", platformIdB.length() > 10 ? platformIdB.substring(0, 10) : platformIdB));

            // Reference Transaction (12AN, O)
            String refTransaction = uap070.getNumRefTransaction() != null ? uap070.getNumRefTransaction() : "";
            line.append(String.format("%-12s", refTransaction.length() > 12 ? refTransaction.substring(0, 12) : refTransaction));

            // Montant operation (15N, O) - Format with decimals in cents
            BigDecimal montantOp = uap070.getMontantTransaction() != null ? uap070.getMontantTransaction() : BigDecimal.ZERO;
            line.append(String.format("%015d", montantOp.multiply(new BigDecimal(100)).longValue()));

            // Sens operation (1AN, O) - Always D for this context
            String sensOperation = "D";
            line.append(sensOperation);

            // Calculate compensation based on origin
            String origin = uap070.getOrigin() != null ? uap070.getOrigin().toUpperCase() : "";
// Determine sensCompensation based on typeOperation
            String sensCompensation;
            if (typeOperation.equals("01") || typeOperation.equals("04") || typeOperation.equals("03")) {
                sensCompensation = "C"; // Credit
            } else if (typeOperation.equals("02") || typeOperation.equals("05")) {
                sensCompensation = "D"; // Debit
            } else {
                sensCompensation = "D"; // Default to Debit for unknown types
            }
            // Calculate montant compensation based on origin
//            BigDecimal montantComm = uap070.getMontantCommission() != null ? uap070.getMontantCommission() : BigDecimal.ZERO;
            BigDecimal montantInter = uap070.getMontantInterchange() != null ? uap070.getMontantInterchange() : BigDecimal.ZERO;
            BigDecimal montantComp = (sensCompensation.equals("C")) ?
                    uap070.getMontantTransaction().add(montantInter) :
                    uap070.getMontantTransaction().subtract(montantInter);
            totalAmountFinal = totalAmountFinal.add(montantComp);
            // Update the entity with calculated compensation amount
            uap070.setMontantSettlement(montantComp);
            // Montant compensation (15N, O) - Format with decimals in cents
            line.append(String.format("%015d", montantComp.multiply(new BigDecimal(100)).longValue()));

            // Sens compensation (1AN, O) - C if origin contains P2B or B2B, else D
            line.append(sensCompensation);

            // Montant Commission (7N, O) - Format with decimals in cents
            line.append(String.format("%07d", montantInter.multiply(new BigDecimal(100)).longValue()));

            // Libelle (70AN, F) - Free text
            String libelle = "COMPENSATION MOBILE PAYMENT";
            line.append(String.format("%-70s", libelle.length() > 70 ? libelle.substring(0, 70) : libelle));

            // FILLER (421AN) - Free zone filled with spaces
            line.append(String.format("%-421s", ""));

            content.append(line.toString()).append("\n");
        }


        // Build header line: ELOT + Bank ID + Padding + Seq + Currency + Line Count + Total Amount + Filler
        StringBuilder header = new StringBuilder();
        header.append("ELOT");                                                          // File type identifier
        header.append(destinationBank);                                                 // Destination bank code
        header.append("000");                                                           // Fixed padding
        header.append(seqNumber);                                                       // Sequence number
        header.append("DZD");                                                           // Currency code
        header.append(String.format("%04d", nbreLigne));                               // Number of lines (5 digits)
        header.append(String.format("%016d", totalAmountFinal.multiply(new BigDecimal(100)).longValue())); // Total amount in cents (15 digits)
        header.append(getSpace(28));                                                    // Filler spaces

        return header.toString() + "\n" + content.toString();
    }
    /**
     * Map type transaction codes to operation type codes
     * @param origin The type transaction from UAP070IN
     * @return The mapped operation type code
     */
//    private String mapTypeTransaction(String typeTransaction,String origin) {
//
//        if (typeTransaction == null) {
//            return "01"; // Default value
//        }
//
//        switch (typeTransaction) {
//            case "070": return "01";
//            case "071": return "03";
//            case "072": return "02";
//            case "073": return "04";
//            case "074": return "05";
//            case "075": return "05";
//            case "080": return "03";
//            default: return "01"; // Default for unknown codes
//        }
//    }
    private String mapTypeTransaction( String origin) {
        // Map based on origin field
        if (origin == null || origin.trim().isEmpty()) {
            return "01"; // Default value
        }

        String originUpper = origin.toUpperCase();

        // Check for different transaction types based on origin
        if (originUpper.contains("P2B")) {
            return "01"; // Person to Business
        } else if (originUpper.contains("P2P") || originUpper.contains("G2P")) {
            return "02"; // Person to Person or Government to Person
        } else if (originUpper.contains("B2P")) {
            return "03"; // Business to Person
        } else if (originUpper.contains("B2B")) {
            return "04"; // Business to Business
        } else if (originUpper.contains("P2G") || originUpper.contains("B2G")) {
            return "05"; // Person to Government or Business to Government
        } else {
            return "01"; // Default for unknown origins
        }
    }

    /**
     * Parse a CRA file line and create a UAP070FransaBank entity
     * This is the reverse operation of generateUAP070FileContent
     * @param line The CRA file line string to parse
     * @return UAP070FransaBank entity populated with parsed data
     * @throws IllegalArgumentException if line is too short or invalid
     */
    public UAP070IN parseCROLineToUAP070(String line) {
        if (line == null || line.length() < 150) {
            throw new IllegalArgumentException("CRO line is too short. Expected at least 600 characters, got: " +
                    (line != null ? line.length() : 0));
        }

        UAP070IN uap070 = new UAP070IN();
        
        try {
            int pos = 0;
            
            // Code operation (3N) - Position 0-2
            String codeOperation = line.substring(pos, pos + 3).trim();
            pos += 3;
            
            // Type d'operation (2N) - Position 3-4
            String typeOperation = line.substring(pos, pos + 2).trim();
            uap070.setTypeTransaction(typeOperation);
            // Map type operation back to origin
            String origin = mapTypeOperationToOrigin(typeOperation);
            uap070.setOrigin(origin);
            pos += 2;
            
            // Reference de l'operation (18AN) - Position 5-22
            String refOperation = line.substring(pos, pos + 18).trim();
            uap070.setNumRefArchivage(refOperation);
            pos += 18;
            
            // Indicateur RIB Donneur (1N) - Position 23
            String indRibD = line.substring(pos, pos + 1).trim();
            pos += 1;
            
            // RIB Donneur (20AN) - Position 24-43
            String ribDonneur = line.substring(pos, pos + 20).trim();
            uap070.setNumRIBEmetteur(ribDonneur);
            pos += 20;
            
            // Identifiant plateforme Donneur (10AN) - Position 44-53
            String platformIdD = line.substring(pos, pos + 10).trim();
            uap070.setIdPlateformeMobileDonneur(platformIdD);
            pos += 10;
            
            // Date de remise (8N) - Position 54-61
            String dateRemise = line.substring(pos, pos + 8).trim();
            uap070.setDateRemise(dateRemise);
            pos += 8;
            
            // Numero d'autorisation (15AN) - Position 62-76
            String numAutorisation = line.substring(pos, pos + 15).trim();
            // Remove leading zeros if numeric
            try {
                long authNum = Long.parseLong(numAutorisation);
                uap070.setNumAutorisation(String.valueOf(authNum));
            } catch (NumberFormatException e) {
                uap070.setNumAutorisation(numAutorisation);
            }
            pos += 15;
            
            // Indicateur RIB Beneficiaire (1N) - Position 77
            String indRibB = line.substring(pos, pos + 1).trim();
            pos += 1;
            
            // RIB Beneficiaire (20AN) - Position 78-97
            String ribBeneficiaire = line.substring(pos, pos + 20).trim();
            uap070.setNumRIBAcquereur(ribBeneficiaire);
            pos += 20;
            
            // Identifiant plateforme Beneficiaire (10AN) - Position 98-107
            String platformIdB = line.substring(pos, pos + 10).trim();
            uap070.setIdPlateformeMobileBeneficiaire(platformIdB);
            pos += 10;
            
            // Reference Transaction (12AN) - Position 108-119
            String refTransaction = line.substring(pos, pos + 12).trim();
            uap070.setNumRefTransaction(refTransaction);
            pos += 12;
            
            // Montant operation (15N) - Position 120-134
            String montantOpStr = line.substring(pos, pos + 15).trim().replaceAll("\\s+", "");
            try {
                long montantCents = Long.parseLong(montantOpStr);
                BigDecimal montantOp = new BigDecimal(montantCents).divide(new BigDecimal(100));
                uap070.setMontantTransaction(montantOp);
            } catch (NumberFormatException e) {
                logger.warn("Failed to parse montant operation: {}", montantOpStr);
                uap070.setMontantTransaction(BigDecimal.ZERO);
            }
            pos += 15;
            
            // Sens operation (1AN) - Position 135
            String sensOperation = line.substring(pos, pos + 1).trim();
            pos += 1;
            
            // Montant compensation (15N) - Position 136-150
            String montantCompStr = line.substring(pos, pos + 15).trim().replaceAll("\\s+", "");
            try {
                long montantCompCents = Long.parseLong(montantCompStr);
                BigDecimal montantComp = new BigDecimal(montantCompCents).divide(new BigDecimal(100));
                uap070.setMontantSettlement(montantComp);
            } catch (NumberFormatException e) {
                logger.warn("Failed to parse montant compensation: {}", montantCompStr);
                uap070.setMontantSettlement(BigDecimal.ZERO);
            }
            pos += 15;
            
            // Sens compensation (1AN) - Position 151
            String sensCompensation = line.substring(pos, pos + 1).trim();
            pos += 1;
            
            // Montant Commission (7N) - Position 152-158
            String montantInterStr = line.substring(pos, pos + 7).trim().replaceAll("\\s+", "");
            try {
                long montantInterCents = Long.parseLong(montantInterStr);
                BigDecimal montantInter = new BigDecimal(montantInterCents).divide(new BigDecimal(100));
                uap070.setMontantInterchange(montantInter);
            } catch (NumberFormatException e) {
                logger.warn("Failed to parse montant commission: {}", montantInterStr);
                uap070.setMontantInterchange(BigDecimal.ZERO);
            }
            pos += 7;
            
            // Libelle (70AN) - Position 159-228
//            String libelle = line.substring(pos, pos + 70).trim();
//            uap070.setLibelleCommercant(libelle);
//            pos += 70;
            

            logger.debug("Successfully parsed CRA line to UAP070FransaBank - Ref: {}, Amount: {}", 
                    refTransaction, uap070.getMontantTransaction());
            
        } catch (StringIndexOutOfBoundsException e) {
            logger.error("Error parsing CRA line at position - line length: {}", line.length(), e);
            throw new IllegalArgumentException("Invalid CRA line format: " + e.getMessage(), e);
        }
        uap070.setFlag("20");
        uap070.setAccepted("2");
        return uap070;
    }
    
    /**
     * Map type operation code back to origin
     * This is the reverse of mapTypeTransaction
     * @param typeOperation The operation type code (01-05)
     * @return The origin string
     */
    private String mapTypeOperationToOrigin(String typeOperation) {
        if (typeOperation == null || typeOperation.trim().isEmpty()) {
            return "UNKNOWN";
        }
        
        switch (typeOperation.trim()) {
            case "01": return "P2B"; // Person to Business
            case "02": return "P2P"; // Person to Person
            case "03": return "B2P"; // Business to Person
            case "04": return "B2B"; // Business to Business
            case "05": return "P2G"; // Person to Government
            default: return "UNKNOWN";
        }
    }

    // Generate a compensation file into a configured destination (CRA or LOT)
    @GetMapping("/generate-lot")
    public ResponseEntity<String> generateCompensationFile(
            @RequestParam(name = "target", defaultValue = "CRA") String target) {
        try {
            String baseDir;
            baseDir=propertyService.getCompensationfilePathLOT();

            // Fetch list of UAP070IN records

            List<UAP070FransaBank> uap070List = uap070FransaBankRepository.findByDateReglementIsNullOrDateReglementEquals("");
            logger.info("Loaded {} UAP070IN records for file generation", uap070List.size());

            if (baseDir == null || baseDir.trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body("Destination path is not configured for target: " + target);
            }

            // Ensure directory exists
            Path dirPath = Paths.get(baseDir);
            Files.createDirectories(dirPath);

            // Build filename
            String timestamp = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss").format(LocalDateTime.now());
            String fileName = "035.000.001.070.DZD.LOT";
            Path filePath = dirPath.resolve(fileName);

            // Generate formatted content from UAP070IN records
            String content = generateUAP070FileContent(uap070List);

            Files.write(filePath, content.getBytes(StandardCharsets.UTF_8), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            String fileNameORD = "035.000.001.070.DZD.ORD" ;

            Path filePathORD = dirPath.resolve(fileNameORD);

            writeInORD(filePathORD,"001");
            uap070FransaBankRepository.saveAll(uap070List);
            logger.info("Generated compensation file with {} records at: {}", uap070List.size(), filePath.toString());
            return ResponseEntity.ok(String.format("Generated file with %d UAP070IN records at: %s",
                    uap070List.size(), filePath.toString()));
        } catch (Exception ex) {
            logger.error("Failed to generate compensation file", ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to generate file: " + ex.getMessage());
        }
    }
    
    //matching CRA - Async API endpoints
    
    /**
     * Async API endpoint to start CRA matching process in background
     * Returns immediately while processing continues asynchronously
     * @return ResponseEntity with status message
     */
    @GetMapping("/process-cra-matching-async")
    public ResponseEntity<?> processCRAMatchingAsync() {
        try {
            logger.info("Received request to start async CRA matching process");
            name = SecurityContextHolder.getContext().getAuthentication().getName();

            // Check if batch is already running
            BatchesFC batche = batchesFFCRepository.findByKey("CRAUAP70").get();
            if (batche.getBatchStatus() == 0) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body("CRA matching process is already running");
            }
            
            // Start async processing
            asyncProcessingService.processCRAMatchingAsync(this);
            
            logger.info("CRA matching process started asynchronously");
            return ResponseEntity.ok("CRA matching process started successfully. Check batch status for progress.");
            
        } catch (Exception ex) {
            logger.error("Failed to start async CRA matching process", ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to start CRA matching process: " + ex.getMessage());
        }
    }
    
    /**
     * Async API endpoint to start CRO matching process in background
     * Returns immediately while processing continues asynchronously
     * @return ResponseEntity with status message
     */
    @GetMapping("/process-cro-matching-async")
    public ResponseEntity<?> processCROMatchingAsync() {
        try {
            logger.info("Received request to start async CRO matching process");
            name = SecurityContextHolder.getContext().getAuthentication().getName();

            // Check if batch is already running
            BatchesFC batche = batchesFFCRepository.findByKey("CROUAP70IN").get();
            if (batche.getBatchStatus() == 0) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body("CRO matching process is already running");
            }
            
            // Start async processing
            asyncProcessingService.processCROMatchingAsync(this);
            
            logger.info("CRO matching process started asynchronously");
            return ResponseEntity.ok("CRO matching process started successfully. Check batch status for progress.");
            
        } catch (Exception ex) {
            logger.error("Failed to start async CRO matching process", ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to start CRO matching process: " + ex.getMessage());
        }
    }
    
    //matching CRA - Synchronous methods
    
    /**
     * Reads CRA files from the compensation folder and retrieves UAP070FransaBank records
     * with null or empty dateReglement for matching
     * @return ResponseEntity with list of CRA files and matched records count
     */
    @GetMapping("/process-cra-matching")
    public ResponseEntity<?> processCRAMatching() {
        try {
            // Initialize batch status
            BatchesFC batche = batchesFFCRepository.findByKey("CRAUAP70").get();
            batche.setBatchStatus(0);
            batchesFFCRepository.save(batche);
            
            logger.info("Starting CRA matching process...");
            
            // Step 1: Get the CRA folder path and unmatched records
            String craFolderPath = propertyService.getCompensationfilePathCRA();
            logger.info("CRA folder path: {}", craFolderPath);
            
            List<UAP070FransaBank> uapList = uap070FransaBankRepository.findByDateReglementIsNullOrDateReglementEquals("");
            logger.info("Found {} UAP070FransaBank records with null or empty dateReglement", uapList.size());
            
            // Step 2: Create HashMap with numRefTransaction as key for efficient lookup
            Map<String, UAP070FransaBank> uapMap = uapList.stream()
                    .filter(uap -> uap.getNumRefTransaction() != null && !uap.getNumRefTransaction().trim().isEmpty())
                    .collect(Collectors.toMap(
                            uap -> uap.getNumRefTransaction().trim(),
                            uap -> uap,
                            (existing, replacement) -> existing
                    ));
            logger.info("Created HashMap with {} UAP070FransaBank records", uapMap.size());
            
            // Step 3: List all files in the folder
            Path folderPath = Paths.get(craFolderPath);
            if (!Files.exists(folderPath) || !Files.isDirectory(folderPath)) {
                logger.error("CRA folder does not exist or is not a directory: {}", craFolderPath);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("CRA folder does not exist or is not a directory: " + craFolderPath);
            }
            
            // Step 4: Filter files ending with ".070.CRA"
            List<Path> craFilePaths = Files.list(folderPath)
                    .filter(Files::isRegularFile)
                    .filter(path -> path.getFileName().toString().endsWith(".070.DZD.CRA"))
                    .collect(Collectors.toList());
            
            logger.info("Found {} CRA files ending with .070.DZD.CRA", craFilePaths.size());
            
            // Step 5: Process each CRA file
            int totalLinesProcessed = 0;
            int totalMatchedRecords = 0;
            List<String> processedFiles = new ArrayList<>();
            
            for (Path craFilePath : craFilePaths) {
                String fileName = craFilePath.getFileName().toString();
                logger.info("Processing CRA file: {}", fileName);
                
                // Read file with UTF-8 and ignore/replace malformed characters
                List<String> lines = readFileWithErrorHandling(craFilePath);
                logger.info("Read {} lines from file: {}", lines.size(), fileName);
                
                int matchedInFile = 0;
                for (String line : lines) {
                    totalLinesProcessed++;
                    
                    // Skip if line is too short (minimum 166 chars to include transaction reference)
                    if (line.length() < 166) {
                        logger.warn("Skipping short line (length: {})", line.length());
                        continue;
                    }
                    
                    try {
                        // Parse the CRA line structure
                        // Position 0-37: UAP RIO (38 chars)
                        String uapRio = line.substring(0, 38);
                        
                        // Position 38-45: Date reglement (8 chars, YYYYMMDD format)
                        String dateReglement = line.substring(38, 46);
                        
                        // Position 154-165: Reference transaction (12 chars) - after the initial 46 chars + 108 chars of transaction data
                        String numRefTransaction = line.substring(154, 166).trim();
                        String statusTechnique = line.length() >= 3 ? line.substring(line.length() - 3) : "";

                        logger.debug("Parsed CRA line - UAP RIO: {}, Date: {}, Ref: {} ,staus technique {}",
                                uapRio, dateReglement, numRefTransaction,statusTechnique);
                        
                        // Step 6: Match with UAP070FransaBank record using numRefTransaction
                        UAP070FransaBank matchedRecord = uapMap.get(numRefTransaction);
                        
                        if (matchedRecord != null) {
                            // Update the matched record with RIO and dateReglement
                            matchedRecord.setUapRio(uapRio);
                            matchedRecord.setDateReglement(dateReglement);
                            matchedRecord.setStatusTechnique(statusTechnique);
                            matchedInFile++;
                            if ("000".equals(statusTechnique)) {
                                totalMatchedRecords++;
                            }
                            logger.debug("Matched and updated record for transaction: {}", numRefTransaction);
                        } else {
                            logger.debug("No matching UAP070FransaBank record found for transaction: {}", numRefTransaction);
                        }
                        
                    } catch (Exception ex) {
                        logger.error("Error parsing CRA line: {}", ex.getMessage());
                    }
                }
                
                processedFiles.add(fileName + " (" + matchedInFile + " matched)");
                logger.info("Completed processing file: {} - Matched {} records", fileName, matchedInFile);
            }
            
            // Step 7: Save all updated records
            List<UAP070FransaBank> updatedRecords = uapMap.values().stream()
                    .filter(uap -> uap.getDateReglement() != null && !uap.getDateReglement().isEmpty() && "000".equals(uap.getStatusTechnique()))
                    .collect(Collectors.toList());
            
            if (!updatedRecords.isEmpty()) {
                uap070FransaBankRepository.saveAll(updatedRecords);
                logger.info("Saved {} updated UAP070FransaBank records", updatedRecords.size());
            }
            // Step 8: Check and log matching status
            int totalUnmatchedRecords = uapList.size() - totalMatchedRecords;
            if (totalUnmatchedRecords == 0) {
                logger.info("✓ SUCCESS: All {} UAP070FransaBank records were successfully matched with CRA files",
                        uapList.size());
                ArrayList<TransactionEntity> uaps = new ArrayList<>(updatedRecords);

                bankSettlementController.generateBankSettCodeUAP070("UAP070OUT", uaps);
                batche.setBatchStatus(1);
                batchesFFCRepository.save(batche);
                BatchesFC batcheError = batchesFFCRepository.findByKey("execStatusM").get();
                batcheError.setBatchNumber(2);
                batchesFFCRepository.save(batcheError);
                downloadFileBC.writeInFileMP();
                // Move matched CRA files to archive/processed folder
                logger.info("Moving matched CRA files to archive/processed folder");
                moveMatchedFiles(craFilePaths, propertyService.getCompensationfilePath(), "CRA");
            } else {
                logger.warn("⚠ PARTIAL MATCH: {} out of {} UAP070FransaBank records were matched. {} records remain unmatched",
                        totalMatchedRecords, uapList.size(), totalUnmatchedRecords);

                // Log unmatched transaction references
                List<String> unmatchedRefs = uapList.stream()
                        .filter(uap -> (uap.getDateReglement() == null || uap.getDateReglement().isEmpty())    && !"000".equals(uap.getStatusTechnique()))
                        .map(UAP070FransaBank::getNumRefTransaction)
                        .collect(Collectors.toList());
                ArrayList<TransactionEntity> uaps = new ArrayList<>(updatedRecords);

                bankSettlementController.generateBankSettCodeUAP070("UAP070OUT", uaps);
                batche.setBatchStatus(2);
                batchesFFCRepository.save(batche);
                logger.warn("Unmatched or rejected transaction references: {}", unmatchedRefs);
            }
            // Prepare response
            Map<String, Object> response = new HashMap<>();
            response.put("processedFiles", processedFiles);
            response.put("totalFilesProcessed", craFilePaths.size());
            response.put("totalLinesProcessed", totalLinesProcessed);
            response.put("totalMatchedRecords", totalMatchedRecords);
            response.put("totalSavedRecords", updatedRecords.size());
            
            logger.info("CRA matching process completed - Files: {}, Lines: {}, Matched: {}, Saved: {}", 
                    craFilePaths.size(), totalLinesProcessed, totalMatchedRecords, updatedRecords.size());
            
//            // Set batch status to success
//            batche.setBatchStatus(1);
//            batchesFFCRepository.save(batche);
//
            return ResponseEntity.ok(response);
            
        } catch (IOException ex) {
            logger.error("IO error while processing CRA files", ex);
            // Set batch status to error
            BatchesFC batcheError = batchesFFCRepository.findByKey("CRAUAP70").get();
            batcheError.setBatchStatus(2);
            batchesFFCRepository.save(batcheError);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("IO error while processing CRA files: " + ex.getMessage());
        } catch (Exception ex) {
            logger.error("Error during CRA matching process", ex);
            // Set batch status to error
            BatchesFC batcheError = batchesFFCRepository.findByKey("CRAUAP70").get();
            batcheError.setBatchStatus(2);
            batchesFFCRepository.save(batcheError);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error during CRA matching process: " + ex.getMessage());
        }
    }
    @GetMapping("/process-cro-matching")
    public ResponseEntity<?> processCROMatching() {
        try {
            // Initialize batch status
            BatchesFC batche = batchesFFCRepository.findByKey("CROUAP70IN").get();
            batche.setBatchStatus(0);
            batchesFFCRepository.save(batche);
            
            logger.info("Starting CRO matching process...");

            // Step 1: Get the CRO folder path and unmatched records
//            String croFolderPath = propertyService.getCompensationfilePathCro();
            String croFolderPath = batche.getFileLocation();
            logger.info("CRO folder path: {}", croFolderPath);

            List<UAP070IN> uapList = uap070INRepository.findByDateReglementIsNullOrDateReglementEquals("");
            logger.info("Found {} UAP070IN records with null or empty dateReglement", uapList.size());

            // Step 2: Create HashMap with numRefTransaction as key for efficient lookup
            Map<String, UAP070IN> uapMap = uapList.stream()
                    .filter(uap -> uap.getNumRefTransaction() != null && !uap.getNumRefTransaction().trim().isEmpty())
                    .collect(Collectors.toMap(
                            uap -> uap.getNumRefTransaction().trim(),
                            uap -> uap,
                            (existing, replacement) -> existing
                    ));
            logger.info("Created HashMap with {} UAP070IN records", uapMap.size());

            // Step 3: List all files in the folder
            Path folderPath = Paths.get(croFolderPath);
            if (!Files.exists(folderPath) || !Files.isDirectory(folderPath)) {
                logger.error("CRO folder does not exist or is not a directory: {}", croFolderPath);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("CRO folder does not exist or is not a directory: " + croFolderPath);
            }

            // Step 4: Filter files ending with ".070.CRO"
            List<Path> croFilePaths = Files.list(folderPath)
                    .filter(Files::isRegularFile)
                    .filter(path -> path.getFileName().toString().endsWith(".070.DZD.CRO"))
                    .collect(Collectors.toList());

            logger.info("Found {} CRO files ending with .070.DZD.CRO", croFilePaths.size());

            // Step 5: Process each CRO file
            int totalLinesProcessed = 0;
            int totalMatchedRecords = 0;
            List<String> processedFiles = new ArrayList<>();
            List<UAP070IN> croRecords=new ArrayList<>();

            for (Path croFilePath : croFilePaths) {
                String fileName = croFilePath.getFileName().toString();
                logger.info("Processing CRO file: {}", fileName);

                // Read file with UTF-8 and ignore/replace malformed characters
                List<String> lines = readFileWithErrorHandling(croFilePath);

                logger.info("Read {} lines from file: {}", lines.size(), fileName);
                int matchedInFile = 0;
                for (String line : lines) {
                    totalLinesProcessed++;
                    logger.info("Read before {}  : {}", line, fileName);

                    // Clean the line: remove null characters, non-ASCII characters and ALL whitespace (file has spaces between every character)
                    line = line.replaceAll("\\x00", "").replaceAll("[^\\x00-\\x7F]", "").replaceAll("\\s+", "");
                    logger.info("Read {} after  : {}", line, fileName);

                    // Skip if line is too short (minimum 166 chars to include transaction reference)
                    if (line.length() < 166) {
                        logger.warn("Skipping short line (length: {})", line.length());
                        continue;
                    }

                    try {
                        // Parse the CRO line structure
                        // Position 0-37: UAP RIO (38 chars)
                        String uapRio = line.substring(0, 38);

                        // Position 38-45: Date reglement (8 chars, YYYYMMDD format)
                        String dateReglement = line.substring(38, 46);
                        UAP070IN croEntity = parseCROLineToUAP070(line.substring(  46));
                        // Position 154-165: Reference transaction (12 chars) - after the initial 46 chars + 108 chars of transaction data
                        String numRefTransaction = line.substring(154, 166).trim();
                        croEntity.setUapRio(uapRio);
                        croEntity.setDateReglement(dateReglement);
                        logger.debug("Parsed CRO line - UAP RIO: {}, Date: {}, Ref: {}",
                                uapRio, dateReglement, numRefTransaction);

                        // Step 6: Match with UAP070IN record using numRefTransaction
                        UAP070IN matchedRecord = uapMap.get(numRefTransaction);

                        if (matchedRecord != null) {
                            // Update the matched record with RIO and dateReglement
                            matchedRecord.setUapRio(uapRio);
                            matchedRecord.setDateReglement(dateReglement);
                            matchedRecord.setMontantCompensee(croEntity.getMontantCompensee());
                            matchedInFile++;
                            totalMatchedRecords++;
                            logger.debug("Matched and updated record for transaction: {}", numRefTransaction);
                        } else {
                            croRecords.add(croEntity);

                            logger.debug("No matching UAP070IN record found for transaction: {}", numRefTransaction);
                        }

                    } catch (Exception ex) {
                        logger.error("Error parsing CRO line: {}", ex.getMessage());
                    }
                }

                processedFiles.add(fileName + " (" + matchedInFile + " matched)");
                uap070INRepository.saveAll(croRecords);
                logger.info("Completed processing file: {} - Matched {} records", fileName, matchedInFile);
            }

            // Step 7: Save all updated records
            List<UAP070IN> updatedRecords = uapMap.values().stream()
                    .filter(uap -> uap.getDateReglement() != null && !uap.getDateReglement().isEmpty())
                    .collect(Collectors.toList());

            if (!updatedRecords.isEmpty()) {
                uap070INRepository.saveAll(updatedRecords);
                logger.info("Saved {} updated UAP070IN records", updatedRecords.size());
            }

            // Step 8: Check and log matching status
            int totalUnmatchedRecords = uapList.size() - totalMatchedRecords;
            if (totalUnmatchedRecords == 0) {
                logger.info("✓ SUCCESS: All {} UAP070IN records were successfully matched with CRO files",
                        uapList.size());
                ArrayList<TransactionEntity> uaps = new ArrayList<>(updatedRecords);
                bankSettlementController.generateBankSettCodeUAP070("UAP070IN", uaps);

                batche.setBatchStatus(1);
                batchesFFCRepository.save(batche);
                BatchesFC batcheStatus = batchesFFCRepository.findByKey("execStatusM").get();
                batcheStatus.setBatchNumber(0);
                batchesFFCRepository.save(batcheStatus);
                moveMatchedFiles(croFilePaths, propertyService.getCompensationfilePathCro(), "CRO");

            }  else if (croRecords.size()!=0) {
                logger.warn("⚠ PARTIAL MATCH: {} out of {} UAP070IN records were matched. {} records remain extra",
                        totalMatchedRecords, uapList.size(), croRecords.size());
                batche.setBatchStatus(5);
                batchesFFCRepository.save(batche);
                moveMatchedFiles(croFilePaths, propertyService.getCompensationfilePathCro(), "CRO");
            }else {
                logger.warn("⚠ PARTIAL MATCH: {} out of {} UAP070IN records were matched. {} records remain unmatched",
                        totalMatchedRecords, uapList.size(), totalUnmatchedRecords);

                // Log unmatched transaction references
                List<String> unmatchedRefs = uapList.stream()
                        .filter(uap -> uap.getDateReglement() == null || uap.getDateReglement().isEmpty())
                        .map(UAP070IN::getNumRefTransaction)
                        .collect(Collectors.toList());
                batche.setBatchStatus(1);
                batchesFFCRepository.save(batche);
                logger.warn("Unmatched transaction references: {}", unmatchedRefs);
            }

            // Prepare response
            Map<String, Object> response = new HashMap<>();
            response.put("processedFiles", processedFiles);
            response.put("totalFilesProcessed", croFilePaths.size());
            response.put("totalLinesProcessed", totalLinesProcessed);
            response.put("totalMatchedRecords", totalMatchedRecords);
            response.put("totalUnmatchedRecords", totalUnmatchedRecords);
            response.put("totalSavedRecords", updatedRecords.size());
            response.put("allMatched", totalUnmatchedRecords == 0);

            logger.info("CRO matching process completed - Files: {}, Lines: {}, Matched: {}, Saved: {}",
                    croFilePaths.size(), totalLinesProcessed, totalMatchedRecords, updatedRecords.size());

            // Set batch status to success
            batche.setBatchStatus(1);
            batchesFFCRepository.save(batche);
            
            return ResponseEntity.ok(response);

        } catch (IOException ex) {
            logger.error("IO error while processing CRO files", ex);
            // Set batch status to error
            BatchesFC batcheError = batchesFFCRepository.findByKey("CROUAP70IN").get();
            batcheError.setBatchStatus(2);
            batchesFFCRepository.save(batcheError);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("IO error while processing CRO files: " + ex.getMessage());
        } catch (Exception ex) {
            BatchesFC batcheError = batchesFFCRepository.findByKey("CROUAP70IN").get();
            batcheError.setBatchStatus(2);
            batchesFFCRepository.save(batcheError);
            logger.error("Error during CRO matching process", ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error during CRO matching process: " + ex.getMessage());
        }
    }



    /**
     * Process settlement operations for OUT transactions with reglementDate <= regDate
     * @param regDate The regulation date in yyyyMMdd format
     */
    private void reglement070Out(String regDate) {
        try {
            logger.info("Processing reglement070Out for regDate: {}", regDate);
            

            
            // Fetch all UAP070FransaBank records where reglementDate <= regDate
            // Assuming there's a method in the repository or we need to fetch all and filter
            List<TransactionEntity> transactions = new ArrayList<>(uap070FransaBankRepository.findByOriginContainingIgnoreCaseAndDateReglementLessThanOrEqual(regDate));
            bankSettlementController.generateBankSettCodeUAP070("UAP070OUTREG", transactions);
             logger.info("Found {} OUT transactions for reglement with regDate: {}", transactions.size(), regDate);
            

        } catch (Exception e) {
            logger.error("Error processing reglement070Out for regDate {}: {}", regDate, e.getMessage(), e);
        }
    }


    /**
     * Process settlement operations for IN transactions with reglementDate <= regDate
     * @param regDate The regulation date in yyyyMMdd format
     */
    private void reglement070In(String regDate) {
        try {
            logger.info("Processing reglement070In for regDate: {}", regDate);
            List<TransactionEntity> transactions = new ArrayList<>(uap070INRepository.findByOriginContainingIgnoreCaseAndDateReglementLessThanOrEqual(regDate));
            bankSettlementController.generateBankSettCodeUAP070("UAP070INREG", transactions);

            
            logger.info("Found {} IN transactions for reglement with regDate: {}", transactions.size(), regDate);
            

            
        } catch (Exception e) {
            logger.error("Error processing reglement070In for regDate {}: {}", regDate, e.getMessage(), e);
        }
    }
    public String getAmountFormat(BigDecimal amount) {
        long amountInCents = amount.multiply(new BigDecimal(100)).longValue();
        String amountFormat = String.format("%018d,%02d", amountInCents / 100, Math.abs(amountInCents % 100));
        return amountFormat;
    }
    public String getAmountFormat(long amount) {

        String amountFormat = String.format("%018d,%02d", amount / 100, amount % 100);
        return amountFormat;
    }
    /**
     * Process global settlement operations with reglementDate <= regDate
     * @param regDate The regulation date in yyyyMMdd format
     */
    private void reglementGlobal(String regDate) {
        try {

            List<MvbkConf> categories = mvbkConfigRepo.findByStatusLevel("REGGLOBAL");

            String sequenceNumber = bkmRepo.getLastNumIndex() == null ? "0" : bkmRepo.getLastNumIndex();
            HashSet<String> dates = new HashSet<String>();
            List<String> elementUap070Out = uap070FransaBankRepository.findDistinctRegDates(regDate);
            List<String> elementUap070IN = uap070INRepository.findDistinctRegDates(regDate);
            dates.addAll(elementUap070Out);
            dates.addAll(elementUap070IN);
            List<BkmvtiMPFransaBank> bkms = new ArrayList<BkmvtiMPFransaBank>();

            for (String thedate : dates) {
                String dateFormat = convertDate(thedate, "yyyyMMdd", "ddMMyy");
                logger.info("date reg" + dateFormat);
                for (MvbkConf mvk : categories) {


                    BkmvtiMPFransaBank bkmvtiFransaBank = new BkmvtiMPFransaBank();
                    bkmvtiFransaBank.setNumEvenement(String.format("%06d", Integer.parseInt(sequenceNumber)));
                    BigDecimal amountnegatif070 = uap070FransaBankRepository.getTotalAmount(thedate) == null ? BigDecimal.ZERO
                            : uap070FransaBankRepository.getTotalAmount(thedate);
                    logger.info("amount 070 out=>{}", amountnegatif070);

                    BigDecimal amountPositif070In = uap070INRepository.getTotalAmount(thedate) == null ? BigDecimal.ZERO
                            : uap070INRepository.getTotalAmount(thedate);
                    logger.info("amount 070 In=>{}", amountPositif070In);
                    BigDecimal differenceAllerRetour = amountPositif070In.subtract(amountnegatif070);

                    if (differenceAllerRetour.compareTo(BigDecimal.ZERO) > 0) {
                        bkmvtiFransaBank.setMontant(getAmountFormat(differenceAllerRetour));
                        bkmvtiFransaBank.setSens(mvk.getSigne());
                    } else {
                        bkmvtiFransaBank.setMontant(getAmountFormat(differenceAllerRetour.abs()));
                        bkmvtiFransaBank.setSens(mvk.getSigne().equals("D") ? "C" : "D");
                    }
                    bkmvtiFransaBank.setNumCompte(mvk.getAccount());
                    bkmvtiFransaBank.setAgenceEmettrice(mvk.getCodeAgence());
                    bkmvtiFransaBank.setCodeAgenceSaisie(mvk.getCodeAgence());
                    bkmvtiFransaBank.setAgenceDestinatrice(mvk.getCodeAgence());
                    bkmvtiFransaBank.setAgence(mvk.getCodeAgence());
                    bkmvtiFransaBank.setChapitreComptable(mvk.getAccount().substring(0, 6));
                    String lib = mvk.getLibelle() + dateFormat;
                    bkmvtiFransaBank.setLibelle(lib + getSpace(40 - lib.length()));
                    bkmvtiFransaBank.setIdentification(mvk.getIdentification());
                    sameData(bkmvtiFransaBank, thedate, mvk);
                    bkms.add(bkmvtiFransaBank);

                }
            }
            bkmRepo.saveAll(bkms);

        } catch (Exception e) {
            logger.error("Error processing reglementGlobal for regDate {}: {}", regDate, e.getMessage(), e);
        }
    }
    public BkmvtiMPFransaBank sameData(BkmvtiMPFransaBank bkmvtiFransaBank, String date, MvbkConf mvk) {
        FileRequest.print("filling data", FileRequest.getLineNumber());

        String dateformat = convertDate(date, "yyyyMMdd", "dd/MM/yyyy");
        String dateformat2 = convertDate(date, "yyyyMMdd", "yyyyMMdd");

        bkmvtiFransaBank
                .setCodeUtilisateur(name + getSpace(10 - name.length()));
        bkmvtiFransaBank.setCodeDevice("208");
        bkmvtiFransaBank.setCodeOperation(mvk.getCodeOperation());
        bkmvtiFransaBank.setCodeService("9999");
        bkmvtiFransaBank.setExonerationcommission("N");
        bkmvtiFransaBank.setCodeDeviceOrigine("208");
        bkmvtiFransaBank.setCodeID("99000S");
        bkmvtiFransaBank.setCalculmouvementInteragence("N");
        bkmvtiFransaBank.setMouvementAgence("N");
        bkmvtiFransaBank.setTauxChange("1" + getSpace(6));
        bkmvtiFransaBank.setDateComptable(dateformat);
        bkmvtiFransaBank.setNatureTransaction("REGMON");
        bkmvtiFransaBank.setMontantOrigine(getSpace(19));
        bkmvtiFransaBank.setCleControleCompte(getSpace(2));
        bkmvtiFransaBank.setCodeEtat("VA");
        bkmvtiFransaBank.setRefDossier(getSpace(50));
        bkmvtiFransaBank.setPieceComptable("RB" + bkmvtiFransaBank.getCodeOperation() + dateformat2.substring(2));
        bkmvtiFransaBank.setIdentification(mvk.getIdentification());
        bkmvtiFransaBank.setDateValeur(dateformat);
        bkmvtiFransaBank.setReferanceLettrage(dateformat2);
        bkmvtiFransaBank.setNumPiece("RB" + dateformat2);
        return bkmvtiFransaBank;
    }

    /**
     * Move matched files to a destination folder
     * @param filePaths List of file paths to move
     * @param destinationPath Destination folder path
     * @param fileType Type of files being moved (for logging, e.g., "CRA", "CRO")
     * @return Number of files successfully moved
     */
    public static int moveMatchedFiles(List<Path> filePaths, String destinationPath, String fileType) {
        int movedCount = 0;

        try {
            Path destinationDir = Paths.get(destinationPath);

            // Ensure destination directory exists
            Files.createDirectories(destinationDir);

            for (Path filePath : filePaths) {
                try {
                    String fileName = filePath.getFileName().toString();
                    Path targetPath = destinationDir.resolve(fileName);

                    Files.move(filePath, targetPath, StandardCopyOption.REPLACE_EXISTING);

                    logger.info("Moved {} file {} to {}", fileType, fileName, destinationPath);
                    movedCount++;
                } catch (IOException e) {
                    logger.error("Failed to move {} file {}: {}", fileType, filePath.getFileName(), e.getMessage());
                }
            }

            logger.info("Successfully moved {} out of {} {} files", movedCount, filePaths.size(), fileType);

        } catch (IOException e) {
            logger.error("Failed to create destination directory {}: {}", destinationPath, e.getMessage());
        }

        return movedCount;
    }
    
    /**
     * Read file with UTF-8 encoding and handle malformed input by replacing invalid characters
     * @param filePath Path to the file to read
     * @return List of lines from the file
     * @throws IOException if file cannot be read
     */
    private static List<String> readFileWithErrorHandling(Path filePath) throws IOException {
        List<String> lines = new ArrayList<>();
        
        // Create a UTF-8 decoder that replaces malformed input instead of throwing exceptions
        CharsetDecoder decoder = StandardCharsets.UTF_8.newDecoder()
                .onMalformedInput(CodingErrorAction.REPLACE)
                .onUnmappableCharacter(CodingErrorAction.REPLACE)
                .replaceWith("?");
        
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(new FileInputStream(filePath.toFile()), decoder))) {
            String line;
            while ((line = reader.readLine()) != null) {
                lines.add(line);
            }
        }
        
        return lines;
    }

    /**
     * Archive mobile payment data from operational tables to history tables
     * Orchestrates the archiving of all mobile payment entities
     */
    private void archiveMobile(String regDate) {
        try {
            logger.info("Starting archiveMobile process...");
            
            archiveUAP070IN(regDate);
            archiveUAP070FransaBank(regDate);
//            archiveBkmvtiMPFransaBank(regDate);
            archiveDayOperationMP( );
            
            logger.info("archiveMobile process completed successfully");
            
        } catch (Exception e) {
            logger.error("Error during archiveMobile process: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to archive mobile data", e);
        }
    }

    /**
     * Archive UAP070IN records to UAP070INHistory
     */
    private void archiveUAP070IN(String regDate) {
        try {
            logger.info("Archiving UAP070IN to UAP070INHistory...");
            List<UAP070IN> uap070INList = uap070INRepository.findByOriginContainingIgnoreCaseAndDateReglementLessThanOrEqual(regDate);
            
            if (uap070INList.isEmpty()) {
                logger.info("No UAP070IN records to archive");
                return;
            }
            
            List<UAP070INHistory> uap070INHistoryList = uap070INList.stream()
                .map(source -> {
                    UAP070INHistory history = new UAP070INHistory();
                    try {
                        PropertyUtils.copyProperties(history, source);
                    } catch (Exception e) {
                        logger.warn("Failed to copy properties for UAP070IN id {}: {}", source.getNumRefTransaction(), e.getMessage());
                    }
                    return history;
                })
                .collect(Collectors.toList());
            
            uap070INHistoryRepository.saveAll(uap070INHistoryList);
            uap070INRepository.deleteAll(uap070INList);
            logger.info("Successfully archived {} UAP070IN records to UAP070INHistory", uap070INHistoryList.size());
            
        } catch (Exception e) {
            logger.error("Error archiving UAP070IN: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to archive UAP070IN", e);
        }
    }

    /**
     * Archive UAP070FransaBank records to UAP070FransaBankHistory
     */
    private void archiveUAP070FransaBank(String regDate) {
        try {
            logger.info("Archiving UAP070FransaBank to UAP070FransaBankHistory...");
            List<UAP070FransaBank> uap070FransaBankList = uap070FransaBankRepository.findByOriginContainingIgnoreCaseAndDateReglementLessThanOrEqual(regDate);
            
            if (uap070FransaBankList.isEmpty()) {
                logger.info("No UAP070FransaBank records to archive");
                return;
            }
            
            List<UAP070FransaBankHistory> uap070FransaBankHistoryList = uap070FransaBankList.stream()
                .map(source -> {
                    UAP070FransaBankHistory history = new UAP070FransaBankHistory();
                    try {
                        PropertyUtils.copyProperties(history, source);
                    } catch (Exception e) {
                        logger.warn("Failed to copy properties for UAP070FransaBank id {}: {}", source.getNumRefTransaction(), e.getMessage());
                    }
                    return history;
                })
                .collect(Collectors.toList());
            
            uap070FransaBankHistoryRepository.saveAll(uap070FransaBankHistoryList);
            uap070FransaBankRepository.deleteAll(uap070FransaBankList);
            logger.info("Successfully archived {} UAP070FransaBank records to UAP070FransaBankHistory", uap070FransaBankHistoryList.size());
            
        } catch (Exception e) {
            logger.error("Error archiving UAP070FransaBank: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to archive UAP070FransaBank", e);
        }
    }

    /**
     * Archive BkmvtiMPFransaBank records to BkmvtiMPFransaBankH
     */
    private void archiveBkmvtiMPFransaBank(String regDate) {
        try {
            logger.info("Archiving BkmvtiMPFransaBank to BkmvtiMPFransaBankH...");
            List<BkmvtiMPFransaBank> bkmvtiMPFransaBankList = bkmRepo.findAll();
            
            if (bkmvtiMPFransaBankList.isEmpty()) {
                logger.info("No BkmvtiMPFransaBank records to archive");
                return;
            }
            
            List<BkmvtiMPFransaBankH> bkmvtiMPFransaBankHList = bkmvtiMPFransaBankList.stream()
                .map(source -> {
                    BkmvtiMPFransaBankH history = new BkmvtiMPFransaBankH();
                    try {
                        PropertyUtils.copyProperties(history, source);
                    } catch (Exception e) {
                        logger.warn("Failed to copy properties for BkmvtiMPFransaBank id {}: {}", source.getNumRefTransactions() , e.getMessage());
                    }
                    return history;
                })
                .collect(Collectors.toList());
            
            bkmvtiMPFransaBankHRepository.saveAll(bkmvtiMPFransaBankHList);
            logger.info("Successfully archived {} BkmvtiMPFransaBank records to BkmvtiMPFransaBankH", bkmvtiMPFransaBankHList.size());
            
        } catch (Exception e) {
            logger.error("Error archiving BkmvtiMPFransaBank: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to archive BkmvtiMPFransaBank", e);
        }
    }

    /**
     * Archive DayOperationMP records to DayOperationMPH
     */
    public void archiveDayOperationMP( ) {
        try {
            logger.info("Archiving DayOperationMP to DayOperationMPH...");
            List<DayOperationMP> dayOperationMPList = dayOperationMPRepository.findAll();
            
            if (dayOperationMPList.isEmpty()) {
                logger.info("No DayOperationMP records to archive");
                return;
            }
            
            List<DayOperationMPH> dayOperationMPHList = dayOperationMPList.stream()
                .map(source -> {
                    DayOperationMPH history = new DayOperationMPH();
                    try {
                        PropertyUtils.copyProperties(history, source);
                    } catch (Exception e) {
                        logger.warn("Failed to copy properties for DayOperationMP id {}: {}", source.getIddays(), e.getMessage());
                    }
                    return history;
                })
                .collect(Collectors.toList());
            
            dayOperationMPHRepository.saveAll(dayOperationMPHList);
            dayOperationMPRepository.deleteAll();
            logger.info("Successfully archived {} DayOperationMP records to DayOperationMPH", dayOperationMPHList.size());
            
        } catch (Exception e) {
            logger.error("Error archiving DayOperationMP: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to archive DayOperationMP", e);
        }
    }
}
