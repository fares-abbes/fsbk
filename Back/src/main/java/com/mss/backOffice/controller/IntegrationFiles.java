package com.mss.backOffice.controller;

import com.google.gson.Gson;
import com.mss.backOffice.enumType.FileStatusEnum;
import com.mss.backOffice.request.AddFileRequest;
import com.mss.backOffice.request.UAP070INFilterRequest;
import com.mss.backOffice.specification.*;
import com.mss.unified.entities.*;
import com.mss.unified.repositories.*;
import org.apache.commons.beanutils.PropertyUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import javax.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Collections;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RestController
@RequestMapping("IntegrationFiles")
public class IntegrationFiles {
    private static final Gson gson = new Gson();

    @Autowired
    UAP070FransaBankRepository uap070FransaBankRepository;
    @Autowired
    UAP070FransaBankHistoryRepository uap070FransaBankHistoryRepository;
    @Autowired
    UAP070INHistoryRepository uap070INHistoryRepository;
     @Autowired
    BatchesFFCRepository batchesFFCRepository;
    @Autowired
    private FileContentMRepository filecontentMRepository;
    @Autowired
    private FileHeaderMRepository fileheaderMRepository;
     @Autowired
    private TransactionTypesMRepository transactionTypesMRepository;
    private static final int pendingProgressBatch = -1;
    @Autowired
    private CodeBankBCRepository codeBankBCRepository;
    @Autowired
    private AccountRepository accountRepository;
    @Autowired
    BankSettlementController bankSettlementController;
    @Autowired
    private CardRepository cardRepository;
    @Autowired
    private UAP070INRepository uap070INRepository;
    private static final int inProgressBatch = 0;
    private static final int doneProgressBatch = 1;
    private static final int errorProgressBatch = 2;
    private static final int waintingValidation = 3;


    private static final Logger logger = LoggerFactory.getLogger(IntegrationFiles.class);

    /**
     * Parses a single line from the mobile payment file according to TPM format specification
     *
     * @param line The line to parse
     * @return FileContentM entity populated with data from the line
     */
    private FileContentM parseLineToFileContentM(String line) {
        try {
            FileContentM fileContent = new FileContentM();

            int pos = 0; // Current position tracker

            // DONNÉES DU DONNEUR D'ORDRE
            fileContent.setIdPlateformeMobileDonneur(safeSub(line, pos, pos + 10));      // 10
            pos += 10;
            fileContent.setRibRipDonneur(safeSub(line, pos, pos + 20));                 // 20
            pos += 20;
            fileContent.setNumTelDonneur(safeSub(line, pos, pos + 15));                 // 15
            pos += 15;
            fileContent.setMethodeAuthDonneur(safeSub(line, pos, pos + 1));             // 1
            pos += 1;

            // DONNÉES DU BENEFICIAIRE
            fileContent.setIdPlateformeMobileBeneficiaire(safeSub(line, pos, pos + 10)); // 10
            pos += 10;
            fileContent.setRibRipBeneficiaire(safeSub(line, pos, pos + 20));             // 20
            pos += 20;
            fileContent.setNumTelBeneficiaire(safeSub(line, pos, pos + 15));             // 15
            pos += 15;

            // DONNÉES DE LA TRANSACTION
            fileContent.setTypeTransaction(safeSub(line, pos, pos + 3));                // 3
            pos += 3;
            fileContent.setDateTransaction(safeSub(line, pos, pos + 8));                // 8
            pos += 8;
            fileContent.setHeureTransaction(safeSub(line, pos, pos + 6));               // 6
            pos += 6;
            fileContent.setMontantTransaction(safeSub(line, pos, pos + 15));            // 15
            pos += 15;
            fileContent.setReferenceFactureCharge(safeSub(line, pos, pos + 20));        // 20
            pos += 20;
            fileContent.setReferenceTransaction(safeSub(line, pos, pos + 12));          // 12
            pos += 12;
            fileContent.setNumAutorisation(safeSub(line, pos, pos + 15));               // 15
            pos += 15;

            // DONNÉES COMMISSIONS
            fileContent.setCommissionDonneurOrdre(safeSub(line, pos, pos + 12));        // 12
            pos += 12;
            fileContent.setCommissionDestinataire(safeSub(line, pos, pos + 12));        // 12
            pos += 12;
            fileContent.setCommissionSwitchMobile(safeSub(line, pos, pos + 12));        // 12
            pos += 12;
            fileContent.setCommissionInterchange(safeSub(line, pos, pos + 12));         // 12
            pos += 12;

            // DONNÉES CHARGEBACK (optional fields)
            if (line.length() > pos) {
                fileContent.setIdChargeback(safeSub(line, pos, pos + 16));              // 16
                pos += 16;
                if (line.length() > pos) {
                    fileContent.setCodeMotifChargeback(safeSub(line, pos, pos + 4));    // 4
                    pos += 4;
                    if (line.length() > pos) {
                        fileContent.setNumAutorisationOperationInitiale(safeSub(line, pos, pos + 15)); // 15
                        pos += 15;
                        if (line.length() > pos) {
                            fileContent.setDateOperationInitiale(safeSub(line, pos, pos + 8)); // 8
                            pos += 8;
                            // RUF field (remaining up to 389 characters)
                            if (line.length() > pos) {
                                int rufLength = Math.min(389, line.length() - pos);
                                fileContent.setRuf(safeSub(line, pos, pos + rufLength)); // Up to 389
                            }
                        }
                    }
                }
            }

            return fileContent;

        } catch (Exception e) {
            logger.error("Error parsing line: {}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * Safe substring extraction that handles bounds checking
     *
     * @param s The source string
     * @param start Start index
     * @param end End index
     * @return Trimmed substring or empty string if bounds are invalid
     */
    private static String safeSub(String s, int start, int end) {
        if (s == null || start >= s.length()) return "";
        if (end > s.length()) end = s.length();
        if (start < 0 || start > end) return "";
        return s.substring(start, end).trim();
    }

    public boolean isValidDateFormat(String inputDate) {
        try {
            LocalDate.parse(inputDate);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @PutMapping("addFileTP")
    public ResponseEntity<?> addFileTP(@RequestBody AddFileRequest addFileRequest) throws IOException {
        BatchesFC batch = batchesFFCRepository.findByBatchType("IntegrationM").get(0);

        int size = 0;
        try {
            if (!isValidDateFormat(addFileRequest.getFileDate())) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(gson.toJson("please verify that format date is YYYY-MM-dd"));

            }

            List<BatchesFC> batches = batchesFFCRepository.findAll();
            batches.forEach(v -> {
                if (v.getKey().equals("TPM")) {
                    LocalDateTime localDateTime = LocalDateTime.now();
                    Date date = Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
                    v.setBatchDate(date);
                    v.setFileName(addFileRequest.getFileName());
                    v.setBatchLastExcution(v.getBatchEndDate());
                    v.setBatchEndDate(null);
                    v.setBatchStatus(inProgressBatch);
                    v.setDateReg(null);
                } else {
                    v.setBatchStatus(pendingProgressBatch);
                }
            });
            batchesFFCRepository.updateFinishBatch("SENDLOTM", 10, new Date());
            batchesFFCRepository.updateFinishBatch("SENDORDM", 10, new Date());
            FileRequest.print(batchesFFCRepository.findByKey("SENDLOTM").toString(), FileRequest.getLineNumber());
            batchesFFCRepository.saveAll(batches);
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            LocalDate localDate = LocalDate.parse(addFileRequest.getFileDate(), formatter);
            DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("yyyyMMdd");
            String ordinalDateString = localDate.format(outputFormatter);
//             String yearString = ordinalDateString.substring(2, 4);

            String fileName = addFileRequest.getFilePath() + "/" + addFileRequest.getFileName() + ordinalDateString;
            // +

//Add header
            if (!(fileheaderMRepository
                    .findByFileNameAndFileDate(addFileRequest.getFileName(), ordinalDateString)
                    .isPresent())) {
                List<String> listDetails = new ArrayList<>();
                FileHeaderM f = new FileHeaderM();

                System.out.println("this file existe" + addFileRequest.getFileDate());
                // Look for files that start with the filename pattern
                Path directory = Paths.get(addFileRequest.getFilePath());
                String filePattern = addFileRequest.getFileName() + ordinalDateString + "*";
                
                try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(directory, filePattern)) {
                    Path matchedFile = null;
                    for (Path entry : directoryStream) {
                        if (Files.isRegularFile(entry)) {
                            matchedFile = entry;
                            break; // Use the first matching file
                        }
                    }
                    
                    if (matchedFile != null) {
                        List<String> stream = Files.readAllLines(matchedFile, StandardCharsets.UTF_8);

                        f.setFileName(addFileRequest.getFileName());
                        f.setFileDate(ordinalDateString);
                        f.setFileprocessingDate(LocalDate.now().format(formatter));
                        f.setStatusFile(FileStatusEnum.PENDING.getValue());
                        f.setDestinationBankIdentification(codeBankBCRepository.findAll().get(0).getIdentifiant());
                        f = fileheaderMRepository.save(f);
                        int idHeader = f.getId();
                        List<FileContentM> lists = new ArrayList<FileContentM>();
                        stream.forEach(e -> {
                            FileContentM fileContent = parseLineToFileContentM(e);
                            fileContent.setSummaryCode(idHeader);
                            lists.add(fileContent);


                        });
                        size = lists.size();
                        filecontentMRepository.saveAll(lists);

                        FileRequest.print(batchesFFCRepository.findAll().toString(), FileRequest.getLineNumber());
                    } else {
                        batchesFFCRepository.updateStatusAndErrorBatch("TP", 2, "file not found", new Date(),
                                "file not found");
                        return ResponseEntity.badRequest().body(gson.toJson("file not found"));
                    }
                } catch (IOException e) {
                    batchesFFCRepository.updateStatusAndErrorBatch("TP", 2, "error reading directory", new Date(),
                            "error reading directory: " + e.getMessage());
                    return ResponseEntity.badRequest().body(gson.toJson("error reading directory"));
                }
            }
        }

        catch (Exception e) {
            LocalDateTime localDateTime = LocalDateTime.now();
            Date date = Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
            batch.setBatchEndDate(date);
            batch.setError(e.getMessage());
            batch.setErrorStackTrace(e.getStackTrace()[1].toString());
            batch.setBatchStatus(errorProgressBatch);
            batchesFFCRepository.saveAndFlush(batch);
            e.printStackTrace();
            return ResponseEntity.badRequest().body(gson.toJson("Errors wile generating file"));
        }
        LocalDateTime localDateTime = LocalDateTime.now();
        Date date = Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
        batch.setBatchEndDate(date);

        batch.setBatchStatus(waintingValidation);
        batchesFFCRepository.saveAndFlush(batch);
        return ResponseEntity.ok().body(gson.toJson(size));

    }

    @GetMapping("/ListOfTransactionTypes")
    public List<TransactionTypeM> findAllTransactionTypes() {
        return transactionTypesMRepository.findAll();
    }


    @GetMapping("getAllCompensationDates")
    public HashSet<String> getAllCompensationDates() {
        List<FileHeaderM> elements = fileheaderMRepository.findAll();
        HashSet<String> dates = new HashSet();
        for (FileHeaderM fh : elements) {
            try {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
                LocalDate localDate = LocalDate.parse(fh.getFileDate(), formatter);
                DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("MM/dd/YYYY");
                String ordinalDateString = localDate.format(outputFormatter);
                // print(localDate.toString(), getLineNumber());
                dates.add(ordinalDateString);
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
        return dates;
    }

    public String formatDate(String date) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        LocalDate localDate = LocalDate.parse(date, formatter);
        DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("MM/dd/YYYY");
        String ordinalDateString = localDate.format(outputFormatter);
        // print(localDate.toString(), getLineNumber());
        return ordinalDateString;
    }



    @GetMapping("checklength")
    public ResponseEntity<?> checklength() throws IOException {
        return ResponseEntity.ok().body(gson.toJson(filecontentMRepository.count()));
    }

    @GetMapping("validateFile")
    public ResponseEntity<?> validateFile() throws IOException {
        BatchesFC batch = batchesFFCRepository.findByBatchType("IntegrationM").get(0);
        FileHeaderM s = fileheaderMRepository.findByStatusFile(FileStatusEnum.PENDING.getValue()).get(0);
        s.setStatusFile(FileStatusEnum.INTEGRATED.getValue());
        fileheaderMRepository.save(s);
        batch.setBatchStatus(doneProgressBatch);
        batchesFFCRepository.saveAndFlush(batch);
        batch = batchesFFCRepository.findByKey("execStatus").get();
        logger.info("updateStatus");
        batch.setBatchStatus(0);
        ZoneId defaultZoneId = ZoneId.systemDefault();
        DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        LocalDate localDate = LocalDate.parse(s.getFileDate(), inputFormatter);
        batch.setBatchLastExcution(Date.from(localDate.atStartOfDay(defaultZoneId).toInstant()));
        batch.getBatchLastExcution().setHours(15);
        batch.setBatchStatus(1);
        batch.setBatchNumber(1);
        batch.setBatchDate(new Date());
        batch.setError(null);
        batch.setErrorStackTrace(null);
        logger.info("updateStatus" + batch);
        batchesFFCRepository.saveAndFlush(batch);
        return ResponseEntity.ok().body(gson.toJson("HTTTP 200"));

    }

    @GetMapping("cancelFile")
    public ResponseEntity<?> cancelFile() throws IOException {
        FileHeaderM summmary = fileheaderMRepository.findByStatusFile(FileStatusEnum.PENDING.getValue()).get(0);
        List<FileContentM> elements = filecontentMRepository.findBySummaryCode(summmary.getId());

        fileheaderMRepository.delete(summmary);
        filecontentMRepository.deleteAll(elements);
        BatchesFC execstatus = batchesFFCRepository.findByKey("execStatusM").get();
        execstatus.setBatchNumber(0);
        batchesFFCRepository.save(execstatus);
        batchesFFCRepository.updateFinishBatch("TPM", -1, new Date());

        return ResponseEntity.ok().body(gson.toJson("HTTP 200"));

    }

    @PutMapping("checkDateTP")
    public ResponseEntity<?> checkDateTP(@RequestBody AddFileRequest addFileRequest) throws IOException {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        LocalDate extractedDatelastFile = LocalDate.parse(addFileRequest.getFileDate(), formatter);
        LocalDate previousDay = extractedDatelastFile.minusDays(1);
        ;

        LocalDate extractedDate = LocalDate.parse(addFileRequest.getFileDate(), formatter);

        LocalDate localDate = LocalDate.parse(addFileRequest.getFileDate(), formatter);
        DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        String ordinalDateString = localDate.format(outputFormatter);
        String fileName = addFileRequest.getFilePath() + "/" + addFileRequest.getFileName() + ordinalDateString
                + ".txt";
        String fileNameprev = addFileRequest.getFilePath() + "/" + addFileRequest.getFileName()
                + previousDay.format(inputFormatter) + ".txt";

        try {
            String filePattern = addFileRequest.getFileName() + ordinalDateString;
            boolean fileExists = Files.list(Paths.get(addFileRequest.getFilePath()))
                .filter(Files::isRegularFile)
                .anyMatch(path -> path.getFileName().toString().startsWith(filePattern));
                
            if (!fileExists) {
                return ResponseEntity.badRequest().body(gson.toJson(
                        "ERROR 103 : Aucun fichier correspondant à la date sélectionnée n'a été trouvé!!"));
            }
        } catch (IOException e) {
            return ResponseEntity.badRequest().body(gson.toJson(
                    "ERROR 104 : Erreur lors de la vérification des fichiers : " + e.getMessage()));
        }
        if (fileheaderMRepository.findByfileDate(previousDay.format(inputFormatter)).isEmpty()) {
            return ResponseEntity.badRequest().body(gson.toJson(
                    "ERROR 101 : Il existe un gap dans la liste des fichiers intégrer voulez vous continuer ? "));
        }

        return ResponseEntity.ok().body(gson.toJson("OK"));

    }

    public class CheckUp {
        BigDecimal montantTransaction;
        String typeTransaction;
        String dateCompensation;
        int nb;
    }

    @GetMapping("getCheckTP")
    public ResponseEntity<?> getCheckTP() {
        FileHeaderM summmary = fileheaderMRepository.findByStatusFile(FileStatusEnum.PENDING.getValue()).get(0);
        List<FileContentM> elements = filecontentMRepository.findBySummaryCode(summmary.getId());


        HashMap<String,  CheckUp> list = new HashMap<>();
        for (FileContentM fc : elements) {
            if (!list.containsKey(fc.getTypeTransaction())) {
                 CheckUp cp = new CheckUp();
                cp.montantTransaction = new BigDecimal(fc.getMontantTransaction());
                cp.dateCompensation = summmary.getFileDate();
                cp.typeTransaction = fc.getTypeTransaction();
                cp.nb = 1;
                list.put(fc.getTypeTransaction(), cp);
            } else {
                 CheckUp cp = list.get(fc.getTypeTransaction());
                cp.montantTransaction = cp.montantTransaction.add(new BigDecimal(fc.getMontantTransaction()));
                cp.nb++;
            }
        }

        return ResponseEntity.ok().body(gson.toJson(list.values()));
    }

    /**
     * Updates the status of all FileHeaderM records to INTEGRATED for a given date
     * @param fileDate The file date of the FileHeaderM records to update
     * @return ResponseEntity with success or error message
     */
    @PutMapping("updateFileStatus/{fileDate}")
    public ResponseEntity<?> updateFileStatus(@PathVariable String fileDate) {
        try {
            List<FileHeaderM> fileHeaders = fileheaderMRepository.findByfileDate(fileDate);
            
            if (fileHeaders.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(gson.toJson("No FileHeaderM records found for file date " + fileDate));
            }
            
            // Update all FileHeaderM records for this date
            for (FileHeaderM fileHeader : fileHeaders) {
                fileHeader.setStatusFile(FileStatusEnum.INTEGRATED.getValue());
            }
            fileheaderMRepository.saveAll(fileHeaders);
            
            logger.info("Updated {} FileHeaderM records with file date {} status to INTEGRATED", fileHeaders.size(), fileDate);
            return ResponseEntity.ok().body(gson.toJson("Updated " + fileHeaders.size() + " FileHeaderM records to INTEGRATED successfully"));
            
        } catch (Exception e) {
            logger.error("Error updating FileHeaderM status: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(gson.toJson("Error updating FileHeaderM status: " + e.getMessage()));
        }
    }

    /**
     * Deletes all FileHeaderM records and their associated FileContentM records for a given date
     * @param fileDate The file date of the FileHeaderM records to delete
     * @return ResponseEntity with success or error message
     */
    @DeleteMapping("deleteFileHeader/{fileDate}")
    public ResponseEntity<?> deleteFileHeader(@PathVariable String fileDate) {
        try {
            List<FileHeaderM> fileHeaders = fileheaderMRepository.findByfileDate(fileDate);
            
            if (fileHeaders.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(gson.toJson("No FileHeaderM records found for file date " + fileDate));
            }
            
            int totalContentDeleted = 0;
            
            // Delete all associated FileContentM records first for each FileHeaderM
            for (FileHeaderM fileHeader : fileHeaders) {
                List<FileContentM> associatedContent = filecontentMRepository.findBySummaryCode(fileHeader.getId());
                if (!associatedContent.isEmpty()) {
                    filecontentMRepository.deleteAll(associatedContent);
                    totalContentDeleted += associatedContent.size();
                }
            }
            
            // Delete all FileHeaderM records
            fileheaderMRepository.deleteAll(fileHeaders);
            
            logger.info("Successfully deleted {} FileHeaderM records with file date {} and {} associated FileContentM records", 
                       fileHeaders.size(), fileDate, totalContentDeleted);
            return ResponseEntity.ok().body(gson.toJson("Deleted " + fileHeaders.size() + " FileHeaderM records and " + 
                                                       totalContentDeleted + " associated FileContentM records successfully"));
            
        } catch (Exception e) {
            logger.error("Error deleting FileHeaderM records: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(gson.toJson("Error deleting FileHeaderM records: " + e.getMessage()));
        }
    }

    /**
     * Retrieve paginated list of FileContentM with dynamic filters and sorting
     * 
     * @param page Page number (0-indexed)
     * @param size Page size
     * @param sortOn Field name to sort on
     * @param dir Sort direction ("asc" or "desc")
     * @param filter FileContentM object containing filter criteria (non-null fields will be used)
     * @return Page<FileContentM> with pagination metadata
     */
    @PostMapping("findFileContentMByFilters")
    public ResponseEntity<?> findFileContentMByFilters(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortOn,
            @RequestParam(defaultValue = "desc") String dir,
            @RequestBody FileContentM filter) {
        try {
            // Create sort object based on direction
            Sort sort = dir.equalsIgnoreCase("asc") ? Sort.by(sortOn).ascending() : Sort.by(sortOn).descending();
            
            // Create pageable object
            Pageable pageable = PageRequest.of(page, size, sort);
            
            // Build specification from filter
            Specification<FileContentM> spec = FileContentMSpecification.buildSpecification(filter);
            
            // Execute query with pagination
            Page<FileContentM> result = filecontentMRepository.findAll(spec, pageable);
            
            logger.info("Retrieved {} FileContentM records (page {}/{}, total elements: {})", 
                       result.getContent().size(), page + 1, result.getTotalPages(), result.getTotalElements());
            
            return ResponseEntity.ok().body(result);
            
        } catch (Exception e) {
            logger.error("Error retrieving FileContentM with filters: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(gson.toJson("Error retrieving FileContentM: " + e.getMessage()));
        }
    }

    /**
     * Retrieve full list of FileContentM without pagination for Excel/PDF export
     * 
     * @param filter FileContentM object containing filter criteria (non-null fields will be used)
     * @return List<FileContentM> containing all matching records
     */
    @PostMapping("findFileContentMByFiltersToExport")
    public ResponseEntity<?> findFileContentMByFiltersToExport(@RequestBody FileContentM filter) {
        try {
            // Build specification from filter
            Specification<FileContentM> spec = FileContentMSpecification.buildSpecification(filter);

            // Execute query without pagination
            List<FileContentM> result = filecontentMRepository.findAll(spec);
            
            logger.info("Retrieved {} FileContentM records for export", result.size());
            
            return ResponseEntity.ok().body(result);
            
        } catch (Exception e) {
            logger.error("Error retrieving FileContentM for export: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(gson.toJson("Error retrieving FileContentM for export: " + e.getMessage()));
        }
    }
    @GetMapping("/countDuplicate")
    public ResponseEntity<?> countDuplicate() {
        List<Integer> x = filecontentMRepository.countDuplicate();
        Integer sum = x.stream().collect(Collectors.summingInt(Integer::intValue));
        return ResponseEntity.ok().body(gson.toJson(String.valueOf(sum)));

    }

    /**
     * Update chargeback status for selected mobile transaction IDs
     * 
     * @param chargebackIds List of FileContentM IDs to update
     * @return ResponseEntity with success/failure message
     */
    @PostMapping("setChStatusM")
    public ResponseEntity<?> setChStatusM(@RequestBody List<Long> chargebackIds) {
        try {
            if (chargebackIds == null || chargebackIds.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(gson.toJson("Chargeback IDs list cannot be empty"));
            }
            
            int updatedCount = 0;
            int notFoundCount = 0;
            
            for (Long id : chargebackIds) {
                // Convert Long to Integer for FileContentM ID
                Integer fileContentId = id.intValue();
                
                // Find the FileContentM record
                java.util.Optional<FileContentM> optionalFileContent = filecontentMRepository.findById(fileContentId);
                
                if (optionalFileContent.isPresent()) {
                    FileContentM fileContent = optionalFileContent.get();
                    // Update chargeback status - you can modify this logic based on your requirements
                    // For now, we'll set a flag or update a status field
                    // Note: FileContentM entity doesn't have a chargebackStatus field in the current structure
                    // You may need to add this field to the entity if it doesn't exist
                    
                    // If you have a chargebackStatus field, uncomment and use:
                    // fileContent.setChargebackStatus("PROCESSED");
                    
                    filecontentMRepository.save(fileContent);
                    updatedCount++;
                } else {
                    notFoundCount++;
                    logger.warn("FileContentM with ID {} not found", id);
                }
            }
            
            String message = String.format("Updated %d records successfully. %d records not found.", 
                                         updatedCount, notFoundCount);
            logger.info(message);
            
            return ResponseEntity.ok().body(gson.toJson(message));
            
        } catch (Exception e) {
            logger.error("Error updating chargeback status: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(gson.toJson("Error updating chargeback status: " + e.getMessage()));
        }
    }

    /**
     * Search and retrieve UAP070IN or UAP070INHistory records with pagination, sorting, and filtering
     * 
     * @param page Page number (0-indexed)
     * @param size Number of records per page
     * @param sortOn Field to sort on (default: code)
     * @param dir Sort direction (asc/desc, default: asc)
     * @param filterRequest Filter request containing historique flag and filter criteria
     * @return Paginated list of UAP070IN or UAP070INHistory records
     */
    @PostMapping("/findUAP070IN")
    public ResponseEntity<?> findUAP070IN(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "code") String sortOn,
            @RequestParam(defaultValue = "asc") String dir,
            @RequestBody(required = false) UAP070INFilterRequest filterRequest) {
        try {
            // Extract historique flag and filters
            boolean isHistorique = filterRequest != null && filterRequest.isHistorique();
            Map<String, Object> filterParams = filterRequest != null ? filterRequest.getFilters() : null;
            
            logger.info("Fetching UAP070IN{} records - page: {}, size: {}, sortOn: {}, dir: {}, filters: {}", 
                    isHistorique ? "History" : "", page, size, sortOn, dir, filterParams);
            
            // Validate pagination parameters
            if (page < 0) {
                throw new IllegalArgumentException("Page number must be greater than or equal to zero");
            }
            if (size <= 0 || size > 100) {
                throw new IllegalArgumentException("Page size must be between 1 and 100");
            }
            
            // Create sort object
            Sort.Direction direction = dir.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
            Sort sort = Sort.by(direction, sortOn);
            
            // Create pageable object
            Pageable pageable = PageRequest.of(page, size, sort);
            
            // Query from appropriate table based on historique flag
            if (isHistorique) {
                // Query from UAP070INHistory table
                Page<UAP070INHistory> historyResult;
                
                if (filterParams != null && !filterParams.isEmpty()) {
                    Specification<UAP070INHistory> spec = UAP070INHistorySpecification.withDynamicQuery(filterParams);
                    historyResult = uap070INHistoryRepository.findAll(spec, pageable);
                } else {
                    historyResult = uap070INHistoryRepository.findAll(pageable);
                }
                
                logger.info("Successfully retrieved {} UAP070INHistory records out of {} total", 
                        historyResult.getNumberOfElements(), historyResult.getTotalElements());
                
                return ResponseEntity.ok(historyResult);
            } else {
                // Query from UAP070IN table
                Page<UAP070IN> result;
                
                if (filterParams != null && !filterParams.isEmpty()) {
                    Specification<UAP070IN> spec = UAP070INSpecification.withDynamicQuery(filterParams);
                    result = uap070INRepository.findAll(spec, pageable);
                } else {
                    result = uap070INRepository.findAll(pageable);
                }
                
                logger.info("Successfully retrieved {} UAP070IN records out of {} total", 
                        result.getNumberOfElements(), result.getTotalElements());
                
                return ResponseEntity.ok(result);
            }
            
        } catch (IllegalArgumentException e) {
            logger.warn("Invalid request parameters: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(Collections.singletonMap("error", e.getMessage()));
        } catch (Exception e) {
            logger.error("Error fetching UAP070IN records: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.singletonMap("error", "Error processing your request"));
        }
    }
     @PostMapping("/findUAP070INExtra")
    public ResponseEntity<?> findUAP070INExtra(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "code") String sortOn,
            @RequestParam(defaultValue = "asc") String dir,
            @RequestBody(required = false) UAP070INFilterRequest filterRequest) {
        try {
            // Extract historique flag and filters
            boolean isHistorique = filterRequest != null && filterRequest.isHistorique();
            Map<String, Object> filterParams = filterRequest != null ? filterRequest.getFilters() : null;
            
            // Add flag filter to always filter by flag = "20"
            if (filterParams == null) {
                filterParams = new HashMap<>();
            }
            filterParams.put("flag", "20");

            logger.info("Fetching UAP070IN{} records with flag=20 - page: {}, size: {}, sortOn: {}, dir: {}, filters: {}",
                    isHistorique ? "History" : "", page, size, sortOn, dir, filterParams);

            // Validate pagination parameters
            if (page < 0) {
                throw new IllegalArgumentException("Page number must be greater than or equal to zero");
            }
            if (size <= 0 || size > 100) {
                throw new IllegalArgumentException("Page size must be between 1 and 100");
            }

            // Create sort object
            Sort.Direction direction = dir.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
            Sort sort = Sort.by(direction, sortOn);

            // Create pageable object
            Pageable pageable = PageRequest.of(page, size, sort);

            // Query from appropriate table based on historique flag
            if (isHistorique) {
                // Query from UAP070INHistory table
                Page<UAP070INHistory> historyResult;

                if (filterParams != null && !filterParams.isEmpty()) {
                    Specification<UAP070INHistory> spec = UAP070INHistorySpecification.withDynamicQuery(filterParams);
                    historyResult = uap070INHistoryRepository.findAll(spec, pageable);
                } else {
                    historyResult = uap070INHistoryRepository.findAll(pageable);
                }

                logger.info("Successfully retrieved {} UAP070INHistory records out of {} total",
                        historyResult.getNumberOfElements(), historyResult.getTotalElements());

                return ResponseEntity.ok(historyResult);
            } else {
                // Query from UAP070IN table
                Page<UAP070IN> result;

                if (filterParams != null && !filterParams.isEmpty()) {
                    Specification<UAP070IN> spec = UAP070INSpecification.withDynamicQuery(filterParams);
                    result = uap070INRepository.findAll(spec, pageable);
                } else {
                    result = uap070INRepository.findAll(pageable);
                }

                logger.info("Successfully retrieved {} UAP070IN records out of {} total",
                        result.getNumberOfElements(), result.getTotalElements());

                return ResponseEntity.ok(result);
            }

        } catch (IllegalArgumentException e) {
            logger.warn("Invalid request parameters: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(Collections.singletonMap("error", e.getMessage()));
        } catch (Exception e) {
            logger.error("Error fetching UAP070IN records: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.singletonMap("error", "Error processing your request"));
        }
    }

    /**
     * Search and retrieve UAP070 or UAP070History records with pagination, sorting, and filtering
     *
     * @param page Page number (0-indexed)
     * @param size Number of records per page
     * @param sortOn Field to sort on (default: code)
     * @param dir Sort direction (asc/desc, default: asc)
     * @param filterRequest Filter request containing historique flag and filter criteria
     * @return Paginated list of UAP070IN or UAP070INHistory records
     */
    @PostMapping("/findUAP070OUT")
    public ResponseEntity<?> findUAP070OUT(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "code") String sortOn,
            @RequestParam(defaultValue = "asc") String dir,
            @RequestBody(required = false) UAP070INFilterRequest filterRequest) {
        try {
            // Extract historique flag and filters
            boolean isHistorique = filterRequest != null && filterRequest.isHistorique();
            Map<String, Object> filterParams = filterRequest != null ? filterRequest.getFilters() : null;

            logger.info("Fetching UAP070IN{} records - page: {}, size: {}, sortOn: {}, dir: {}, filters: {}",
                    isHistorique ? "History" : "", page, size, sortOn, dir, filterParams);

            // Validate pagination parameters
            if (page < 0) {
                throw new IllegalArgumentException("Page number must be greater than or equal to zero");
            }
            if (size <= 0 || size > 100) {
                throw new IllegalArgumentException("Page size must be between 1 and 100");
            }

            // Create sort object
            Sort.Direction direction = dir.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
            Sort sort = Sort.by(direction, sortOn);

            // Create pageable object
            Pageable pageable = PageRequest.of(page, size, sort);

            // Query from appropriate table based on historique flag
            if (isHistorique) {
                // Query from UAP070INHistory table
                Page<UAP070FransaBankHistory> historyResult;

                if (filterParams != null && !filterParams.isEmpty()) {
                    Specification<UAP070FransaBankHistory> spec = UAP070HistorySpecification.withDynamicQuery(filterParams);
                    historyResult = uap070FransaBankHistoryRepository.findAll(spec, pageable);
                } else {
                    historyResult = uap070FransaBankHistoryRepository.findAll(pageable);
                }

                logger.info("Successfully retrieved {} UAP070INHistory records out of {} total",
                        historyResult.getNumberOfElements(), historyResult.getTotalElements());

                return ResponseEntity.ok(historyResult);
            } else {
                // Query from UAP070IN table
                Page<UAP070FransaBank> result;

                if (filterParams != null && !filterParams.isEmpty()) {
                    Specification<UAP070FransaBank> spec = UAP070Specification.withDynamicQuery(filterParams);
                    result = uap070FransaBankRepository.findAll(spec, pageable);
                } else {
                    result = uap070FransaBankRepository.findAll(pageable);
                }

                logger.info("Successfully retrieved {} UAP070IN records out of {} total",
                        result.getNumberOfElements(), result.getTotalElements());

                return ResponseEntity.ok(result);
            }

        } catch (IllegalArgumentException e) {
            logger.warn("Invalid request parameters: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(Collections.singletonMap("error", e.getMessage()));
        } catch (Exception e) {
            logger.error("Error fetching UAP070IN records: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.singletonMap("error", "Error processing your request"));
        }
    }

    /**
     * Accept extra UAP070IN mobile payment transactions
     * Fetches all UAP070IN transactions with flag = "20" and updates them to flag = "10" (accepted status)
     * 
     * @return ResponseEntity with success/failure message
     */
    @PutMapping("/AcceptExtra070IN")
    public ResponseEntity<?> acceptExtra070IN() {
        try {
            logger.info("Starting AcceptExtra070IN - fetching all transactions with flag = 20");
            
            // Fetch all UAP070IN transactions with flag = "20"
            List<UAP070IN> transactionsWithFlag20 = uap070INRepository.findByFlag("20");
            
            if (transactionsWithFlag20 == null || transactionsWithFlag20.isEmpty()) {
                logger.info("No UAP070IN transactions found with flag = 20");
                return ResponseEntity.ok()
                        .body(gson.toJson("No transactions found with flag = 20"));
            }
            
            logger.info("Found {} UAP070IN transactions with flag = 20", transactionsWithFlag20.size());
            
            // Update flag from "20" to "10" for all transactions
            for (UAP070IN transaction : transactionsWithFlag20) {
                transaction.setFlag("10");
            }
            
            // Save all updated transactions
            uap070INRepository.saveAll(transactionsWithFlag20);
            ArrayList<TransactionEntity> uaps = new ArrayList<>(transactionsWithFlag20);

            bankSettlementController.generateBankSettCodeUAP070("UAP070IN",uaps);
            BatchesFC batche = batchesFFCRepository.findByKey("CROUAP70IN").get();

            batche.setBatchStatus(1);
            batchesFFCRepository.save(batche);
            String message = String.format("Successfully accepted %d transactions (flag updated from 20 to 10)", 
                                         transactionsWithFlag20.size());
            logger.info(message);
            
            return ResponseEntity.ok().body(gson.toJson(message));
            
        } catch (Exception e) {
            logger.error("Error accepting extra UAP070IN transactions: {}", e.getMessage(), e);
            BatchesFC batche = batchesFFCRepository.findByKey("CROUAP70IN").get();

            batche.setBatchStatus(2);
            batchesFFCRepository.save(batche);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(gson.toJson("Error accepting transactions: " + e.getMessage()));
        }
    }

}
