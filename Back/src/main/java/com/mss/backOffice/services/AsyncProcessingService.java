package com.mss.backOffice.services;

import com.mss.backOffice.controller.ExecutorMobileThreads;
import com.mss.backOffice.controller.FileRequest;
import com.mss.unified.entities.*;
import com.mss.unified.repositories.BankFransaRepository;
import com.mss.unified.repositories.CodeBankBCRepository;
import com.mss.unified.repositories.TvaCommissionFransaBankRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.mss.unified.repositories.UAP051INFransaBankRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import static com.mss.backOffice.controller.ExecutorMobileThreads.*;

@Service
public class AsyncProcessingService {
    private static final Logger logger = LoggerFactory.getLogger(AsyncProcessingService.class);
    @Autowired
    TvaCommissionFransaBankRepository tvaRepo;
    @Autowired
    private UAP051INFransaBankRepository repository;
    @Autowired
    public CodeBankBCRepository bankRepo;


    @Async("taskExecutor")
    public CompletableFuture<String> processUAP052() {
        try {
            // Your processing logic here
            logger.info("Processing UAP052 in thread: {}", Thread.currentThread().getName());
            // Simulate processing
            Thread.sleep(2000);
            return CompletableFuture.completedFuture("SUCCESS");
        } catch (Exception e) {
            logger.error("Error processing UAP052", e);
            return CompletableFuture.completedFuture("ERROR");
        }
    }

    @Async("taskExecutor")
    public CompletableFuture<String> processUAP052IN() {
        try {
            logger.info("Processing UAP052IN in thread: {}", Thread.currentThread().getName());
            // Your processing logic here
            Thread.sleep(2000);
            return CompletableFuture.completedFuture("SUCCESS");
        } catch (Exception e) {
            logger.error("Error processing UAP052IN", e);
            return CompletableFuture.completedFuture("ERROR");
        }
    }

    // ================= Mobile flows with cooperative cancellation =================

    private String runWithChecks(String flow, String role, AtomicBoolean stopSignal) {
        try {
            logger.info("Start {}-{}", flow, role);
            // TODO: replace simulation with real business logic for this flow-role
            for (int i = 0; i < 50; i++) {
                if (stopSignal.get() || Thread.currentThread().isInterrupted()) {
                    logger.warn("Stop requested for {}-{} at step {}", flow, role, i);
                    throw new InterruptedException("Stop signal received");
                }
                Thread.sleep(100); // simulate chunk of work
            }
            logger.info("Done {}-{}", flow, role);
            return "SUCCESS";
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            logger.warn("Cancelled {}-{} due to interruption", flow, role);
            throw new RuntimeException("CANCELLED: " + flow + "-" + role, ie);
        } catch (Exception e) {
            logger.error("Error in {}-{}", flow, role, e);
            throw new RuntimeException("ERROR: " + flow + "-" + role, e);
        }
    }

    private String processFlowWithData(String flow, String role, AtomicBoolean stopSignal, List<String> transactionTypes) {
        try {
            logger.info("Start processing {}-{} with transaction types: {}", flow, role, transactionTypes);

            // Access the prepared data from ExecutorMobileThreads static map
            Map<Integer, List<FileContentM>> originalFileContentMap = ExecutorMobileThreads.fileContentMap;

            if (originalFileContentMap.isEmpty()) {
                logger.warn("No file content data available for {}-{}", flow, role);
                return "NO_DATA";
            }

            // Create filtered map containing only transactions of specified types
            Map<Integer, List<FileContentM>> filteredFileContentMap = new HashMap<>();

            for (Map.Entry<Integer, List<FileContentM>> entry : originalFileContentMap.entrySet()) {
                Integer fileHeaderId = entry.getKey();
                List<FileContentM> allFileContents = entry.getValue();

                // Filter by transaction types for this flow
                List<FileContentM> filteredContents = allFileContents.stream()
                        .filter(fc -> transactionTypes.contains(fc.getTypeTransaction()))
                        .collect(Collectors.toList());

                // Only add to filtered map if there are matching transactions
                if (!filteredContents.isEmpty()) {
                    filteredFileContentMap.put(fileHeaderId, filteredContents);
                }
            }

            logger.info("{}-{}: Created filtered map with {} FileHeaders containing {} total transactions",
                    flow, role, filteredFileContentMap.size(),
                    filteredFileContentMap.values().stream().mapToInt(List::size).sum());

            int totalProcessed = 0;

            // Process each FileHeaderM and its filtered FileContentM list
            for (Map.Entry<Integer, List<FileContentM>> entry : filteredFileContentMap.entrySet()) {
                Integer fileHeaderId = entry.getKey();
                List<FileContentM> filteredContents = entry.getValue();

                logger.info("{}-{}: Processing FileHeaderM id: {} with {} filtered transactions",
                        flow, role, fileHeaderId, filteredContents.size());

                // Process each filtered transaction
                for (FileContentM fileContent : filteredContents) {
                    // Check stop signal periodically
                    if (stopSignal.get() || Thread.currentThread().isInterrupted()) {
                        logger.warn("Stop requested for {}-{} at transaction {}", flow, role, fileContent.getId());
                        throw new InterruptedException("Stop signal received");
                    }

                    // TODO: Replace with actual business logic for this flow-role-transaction
                    // Example: process fileContent based on flow and role
                    logger.debug("Processing transaction {} of type {} for {}-{}",
                            fileContent.getId(), fileContent.getTypeTransaction(), flow, role);

                    totalProcessed++;

                    // Simulate some processing time
                    Thread.sleep(10);
                }
            }

            logger.info("Completed {}-{}: processed {} transactions from {} FileHeaders",
                    flow, role, totalProcessed, filteredFileContentMap.size());
            return "SUCCESS";

        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            logger.warn("Cancelled {}-{} due to interruption", flow, role);
            throw new RuntimeException("CANCELLED: " + flow + "-" + role, ie);
        } catch (Exception e) {
            logger.error("Error in {}-{}", flow, role, e);
            throw new RuntimeException("ERROR: " + flow + "-" + role, e);
        }
    }


    //070 073
    @Async("taskExecutor")
    public CompletableFuture<String> p2bAcquirer(AtomicBoolean stopSignal) {        String methodName = getCurrentMethodName();
        // P2B transactions: 070 (Paiement mobile auprès des commerçants), 073 (Paiement de facture par mobile)
        List<String> transactionTypes = Arrays.asList("070", "073");
        String flow = "P2B";
        String role = "ACQUIRER";
        String result = "ERROR";
        try {
            logger.info("Start processing {}-{} with transaction types: {}", flow, role, transactionTypes);

            // Access the prepared data from ExecutorMobileThreads static map
            Map<Integer, List<FileContentM>> originalFileContentMap = ExecutorMobileThreads.fileContentMap;

            if (originalFileContentMap.isEmpty()) {
                logger.warn("No file content data available for {}-{}", flow, role);
                return CompletableFuture.completedFuture("NO_DATA");
            }


            for (Map.Entry<Integer, List<FileContentM>> entry : originalFileContentMap.entrySet()) {
                Integer fileHeaderId = entry.getKey();
                List<FileContentM> allFileContents = entry.getValue();

                // Filter by transaction types for this flow
                List<FileContentM> filteredContents = allFileContents.stream()
                        .filter(fc -> transactionTypes.contains(fc.getTypeTransaction())
                                && fc.getRibRipBeneficiaire().substring(0,3).equals(bankRepo.findIdentifiant().getIdentifiant() ))
                        .collect(Collectors.toList());
                for (FileContentM fileContent : filteredContents) {
                    if (fileContent.getTypeTransaction().equals("070")){
                        //settelement
                        DayOperationMP M011 = new DayOperationMP(fileContent, ExecutorMobileThreads.getSettlementByIdentification("M011"),methodName);
                        //commission
                        DayOperationMP M012 = new DayOperationMP(fileContent, ExecutorMobileThreads.getSettlementByIdentification("M012"),methodName);
                        //tva commission
                        DayOperationMP M013 = new DayOperationMP(fileContent, ExecutorMobileThreads.getSettlementByIdentification("M013"),methodName);
                        //interchange
                        DayOperationMP M014 = new DayOperationMP(fileContent, ExecutorMobileThreads.getSettlementByIdentification("M014"),methodName);
                        //tva interchange
                        DayOperationMP M015 = new DayOperationMP(fileContent, ExecutorMobileThreads.getSettlementByIdentification("M015"),methodName);
                        Map<String,DayOperationMP> list=new HashMap<>();
                        list.put("sett", M011);
                        list.put("commission", M012);
                        list.put("commissionTVA", M013);
                        list.put("Interchange", M014);
                        list.put("tvaInterchange", M015);
//                    if ("donneur".equals(sens)) {

//                    }else if ("benifeciaire".equals(sens)) {
                        correctiondayOperation(fileContent,list,"B", commissionByTypeMap.get("070"));
                        dayOperations.addAll(list.values());

                        if (stopSignal.get() || Thread.currentThread().isInterrupted()) {
                            logger.warn("Stop requested for {}-{} at transaction {}", flow, role, fileContent.getId());
                            throw new InterruptedException("Stop signal received");
                        }
                    }
                    else if (fileContent.getTypeTransaction().equals("073")){
                        //settelement
                        DayOperationMP M016 = new DayOperationMP(fileContent, ExecutorMobileThreads.getSettlementByIdentification("M016"),methodName);
                        //commission
                        DayOperationMP M017 = new DayOperationMP(fileContent, ExecutorMobileThreads.getSettlementByIdentification("M017"),methodName);
                        //tva commission
                        DayOperationMP M018 = new DayOperationMP(fileContent, ExecutorMobileThreads.getSettlementByIdentification("M018"),methodName);
                        //interchange
                        DayOperationMP M019 = new DayOperationMP(fileContent, ExecutorMobileThreads.getSettlementByIdentification("M019"),methodName);
                        //tva interchange
                        DayOperationMP M020 = new DayOperationMP(fileContent, ExecutorMobileThreads.getSettlementByIdentification("M020"),methodName);
                        Map<String,DayOperationMP> list=new HashMap<>();
                        list.put("sett", M016);
                        list.put("commission", M017);
                        list.put("commissionTVA", M018);
                        list.put("Interchange", M019);
                        list.put("tvaInterchange", M020);
//                    if ("donneur".equals(sens)) {

//                    }else if ("benifeciaire".equals(sens)) {
                        correctiondayOperation(fileContent,list,"B", commissionByTypeMap.get("073"));
                        if (stopSignal.get() || Thread.currentThread().isInterrupted()) {
                            logger.warn("Stop requested for {}-{} at transaction {}", flow, role, fileContent.getId());
                            throw new InterruptedException("Stop signal received");
                        }
                        dayOperations.addAll(list.values());

                    }
                }
            }
            int totalProcessed = 0;
            result = "SUCCESS";
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            logger.warn("Cancelled {}-{} due to interruption", flow, role);
            throw new RuntimeException("CANCELLED: " + flow + "-" + role, ie);
        } catch (Exception e) {
            logger.error("Error in {}-{}", flow, role, e);
            throw new RuntimeException("ERROR: " + flow + "-" + role, e);
        }
        return CompletableFuture.completedFuture(result);
    }


//070 073

    @Async("taskExecutor")
    public CompletableFuture<String> p2bEmitter(AtomicBoolean stopSignal) {        String methodName = getCurrentMethodName();
        // P2B transactions: 070 (Paiement mobile auprès des commerçants), 073 (Paiement de facture par mobile)
        List<String> transactionTypes = Arrays.asList("070", "073");
        String flow = "P2B";
        String role = "EMITTER";
        String result = "ERROR";
        try {
            logger.info("Start processing {}-{} with transaction types: {}", flow, role, transactionTypes);

            // Access the prepared data from ExecutorMobileThreads static map
            Map<Integer, List<FileContentM>> originalFileContentMap = ExecutorMobileThreads.fileContentMap;

            if (originalFileContentMap.isEmpty()) {
                logger.warn("No file content data available for {}-{}", flow, role);
                return CompletableFuture.completedFuture("NO_DATA");
            }

            // Create filtered map containing only transactions of specified types
            Map<Integer, List<FileContentM>> filteredFileContentMap = new HashMap<>();

            for (Map.Entry<Integer, List<FileContentM>> entry : originalFileContentMap.entrySet()) {
                Integer fileHeaderId = entry.getKey();
                List<FileContentM> allFileContents = entry.getValue();

                // Filter by transaction types for this flow
                List<FileContentM> filteredContents = allFileContents.stream()
                        .filter(fc -> transactionTypes.contains(fc.getTypeTransaction())
                                && merchantAccountribMap.get(fc.getRibRipDonneur().substring(1))==null
                                && fc.getRibRipDonneur().substring(0,3).equals(bankRepo.findIdentifiant().getIdentifiant().replace("'", ""))
                        )
                        .collect(Collectors.toList());

                for (FileContentM fileContent : filteredContents) {
                    if (fileContent.getTypeTransaction().equals("070")){
                        //settelement
                        DayOperationMP M021 = new DayOperationMP(fileContent, ExecutorMobileThreads.getSettlementByIdentification("M021"),methodName);
                        //commission
                        DayOperationMP M022 = new DayOperationMP(fileContent, ExecutorMobileThreads.getSettlementByIdentification("M022"),methodName);
                        //tva commission
                        DayOperationMP M023 = new DayOperationMP(fileContent, ExecutorMobileThreads.getSettlementByIdentification("M023"),methodName);
                        //interchange
                        DayOperationMP M024 = new DayOperationMP(fileContent, ExecutorMobileThreads.getSettlementByIdentification("M024"),methodName);
                        //tva interchange
                        DayOperationMP M025 = new DayOperationMP(fileContent, ExecutorMobileThreads.getSettlementByIdentification("M025"),methodName);
                        Map<String,DayOperationMP> list=new HashMap<>();
                        list.put("sett", M021);
                        list.put("commission", M022);
                        list.put("commissionTVA", M023);
                        list.put("Interchange", M024);
                        list.put("tvaInterchange", M025);
//                    if ("donneur".equals(sens)) {

//                    }else if ("benifeciaire".equals(sens)) {
                        correctiondayOperation(fileContent,list,"B",commissionByTypeMap.get("070"));
                        dayOperations.addAll(list.values());
                        if (stopSignal.get() || Thread.currentThread().isInterrupted()) {
                            logger.warn("Stop requested for {}-{} at transaction {}", flow, role, fileContent.getId());
                            throw new InterruptedException("Stop signal received");
                        }
                    }
                    else if (fileContent.getTypeTransaction().equals("073")){
                        //settelement
                        DayOperationMP M026 = new DayOperationMP(fileContent, ExecutorMobileThreads.getSettlementByIdentification("M026"),methodName);
                        //commission
                        DayOperationMP M027 = new DayOperationMP(fileContent, ExecutorMobileThreads.getSettlementByIdentification("M027"),methodName);
                        //tva commission
                        DayOperationMP M028 = new DayOperationMP(fileContent, ExecutorMobileThreads.getSettlementByIdentification("M028"),methodName);
                        //interchange
                        DayOperationMP M029 = new DayOperationMP(fileContent, ExecutorMobileThreads.getSettlementByIdentification("M029"),methodName);
                        //tva interchange
                        DayOperationMP M030 = new DayOperationMP(fileContent, ExecutorMobileThreads.getSettlementByIdentification("M030"),methodName);
                        Map<String,DayOperationMP> list=new HashMap<>();
                        list.put("sett", M026);
                        list.put("commission", M027);
                        list.put("commissionTVA", M028);
                        list.put("Interchange", M029);
                        list.put("tvaInterchange", M030);
//                    if ("donneur".equals(sens)) {

//                    }else if ("benifeciaire".equals(sens)) {
                        correctiondayOperation(fileContent,list,"B",commissionByTypeMap.get("073"));
                        dayOperations.addAll(list.values());

                        if (stopSignal.get() || Thread.currentThread().isInterrupted()) {
                            logger.warn("Stop requested for {}-{} at transaction {}", flow, role, fileContent.getId());
                            throw new InterruptedException("Stop signal received");
                        }
                    }
                }

            }

            result = "SUCCESS";

        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            logger.warn("Cancelled {}-{} due to interruption", flow, role);
            throw new RuntimeException("CANCELLED: " + flow + "-" + role, ie);
        } catch (Exception e) {
            logger.error("Error in {}-{}", flow, role, e);
            throw new RuntimeException("ERROR: " + flow + "-" + role, e);
        }
        return CompletableFuture.completedFuture(result);
    }



//070 073

    @Async("taskExecutor")
    public CompletableFuture<String> b2bEmitter(AtomicBoolean stopSignal) {
        String methodName = getCurrentMethodName();
        // P2B transactions: 070 (Paiement mobile auprès des commerçants), 073 (Paiement de facture par mobile)
        List<String> transactionTypes = Arrays.asList("070", "073");
        String flow = "P2B";
        String role = "EMITTER";
        String result = "ERROR";
        try {
            logger.info("Start processing {}-{} with transaction types: {}", flow, role, transactionTypes);

            // Access the prepared data from ExecutorMobileThreads static map
            Map<Integer, List<FileContentM>> originalFileContentMap = ExecutorMobileThreads.fileContentMap;

            if (originalFileContentMap.isEmpty()) {
                logger.warn("No file content data available for {}-{}", flow, role);
                return CompletableFuture.completedFuture("NO_DATA");
            }

            // Create filtered map containing only transactions of specified types
            Map<Integer, List<FileContentM>> filteredFileContentMap = new HashMap<>();

            for (Map.Entry<Integer, List<FileContentM>> entry : originalFileContentMap.entrySet()) {
                Integer fileHeaderId = entry.getKey();
                List<FileContentM> allFileContents = entry.getValue();

                // Filter by transaction types for this flow
                List<FileContentM> filteredContents = allFileContents.stream()
                        .filter(fc -> transactionTypes.contains(fc.getTypeTransaction())
                                && merchantAccountribMap.get(fc.getRibRipDonneur().substring(1))!=null

                                && fc.getRibRipDonneur().substring(0,3).equals(bankRepo.findIdentifiant().getIdentifiant().replace("'", ""))
                        )
                        .collect(Collectors.toList());

                for (FileContentM fileContent : filteredContents) {
                    if (fileContent.getTypeTransaction().equals("070")){
                        //settelement
                        DayOperationMP M111 = new DayOperationMP(fileContent, ExecutorMobileThreads.getSettlementByIdentification("M111"),methodName);
                        //commission
                        DayOperationMP M112 = new DayOperationMP(fileContent, ExecutorMobileThreads.getSettlementByIdentification("M112"),methodName);
                        //tva commission
                        DayOperationMP M113 = new DayOperationMP(fileContent, ExecutorMobileThreads.getSettlementByIdentification("M113"),methodName);
                        //interchange
                        DayOperationMP M114 = new DayOperationMP(fileContent, ExecutorMobileThreads.getSettlementByIdentification("M114"),methodName);
                        //tva interchange
                        DayOperationMP M115 = new DayOperationMP(fileContent, ExecutorMobileThreads.getSettlementByIdentification("M115"),methodName);
                        Map<String,DayOperationMP> list=new HashMap<>();
                        list.put("sett", M111);
                        list.put("commission", M112);
                        list.put("commissionTVA", M113);
                        list.put("Interchange", M114);
                        list.put("tvaInterchange", M115);
//                    if ("donneur".equals(sens)) {

//                    }else if ("benifeciaire".equals(sens)) {
                        correctiondayOperation(fileContent,list,"D",commissionByTypeMap.get("070"));
                        dayOperations.addAll(list.values());
                        if (stopSignal.get() || Thread.currentThread().isInterrupted()) {
                            logger.warn("Stop requested for {}-{} at transaction {}", flow, role, fileContent.getId());
                            throw new InterruptedException("Stop signal received");
                        }
                    }
                    else if (fileContent.getTypeTransaction().equals("073")){
                        //settelement
                        DayOperationMP M116 = new DayOperationMP(fileContent, ExecutorMobileThreads.getSettlementByIdentification("M116"),methodName);
                        //commission
                        DayOperationMP M117 = new DayOperationMP(fileContent, ExecutorMobileThreads.getSettlementByIdentification("M117"),methodName);
                        //tva commission
                        DayOperationMP M118 = new DayOperationMP(fileContent, ExecutorMobileThreads.getSettlementByIdentification("M118"),methodName);
                        //interchange
                        DayOperationMP M119 = new DayOperationMP(fileContent, ExecutorMobileThreads.getSettlementByIdentification("M119"),methodName);
                        //tva interchange
                        DayOperationMP M120 = new DayOperationMP(fileContent, ExecutorMobileThreads.getSettlementByIdentification("M120"),methodName);
                        Map<String,DayOperationMP> list=new HashMap<>();
                        list.put("sett", M116);
                        list.put("commission", M117);
                        list.put("commissionTVA", M118);
                        list.put("Interchange", M119);
                        list.put("tvaInterchange", M120);
//                    if ("donneur".equals(sens)) {

//                    }else if ("benifeciaire".equals(sens)) {
                        correctiondayOperation(fileContent,list,"D",commissionByTypeMap.get("073"));
                        dayOperations.addAll(list.values());

                        if (stopSignal.get() || Thread.currentThread().isInterrupted()) {
                            logger.warn("Stop requested for {}-{} at transaction {}", flow, role, fileContent.getId());
                            throw new InterruptedException("Stop signal received");
                        }
                    }
                }

            }

            result = "SUCCESS";

        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            logger.warn("Cancelled {}-{} due to interruption", flow, role);
            throw new RuntimeException("CANCELLED: " + flow + "-" + role, ie);
        } catch (Exception e) {
            logger.error("Error in {}-{}", flow, role, e);
            throw new RuntimeException("ERROR: " + flow + "-" + role, e);
        }
        return CompletableFuture.completedFuture(result);
    }
    @Async("taskExecutor")
    public CompletableFuture<String> b2bAcquireur(AtomicBoolean stopSignal) {        String methodName = getCurrentMethodName();
        // P2B transactions: 070 (Paiement mobile auprès des commerçants), 073 (Paiement de facture par mobile)
        List<String> transactionTypes = Arrays.asList("071","073");
        String flow = "B2B";
        String role = "ACQUIREUR";
        String result = "ERROR";
        try {
            logger.info("Start processing {}-{} with transaction types: {}", flow, role, transactionTypes);

            // Access the prepared data from ExecutorMobileThreads static map
            Map<Integer, List<FileContentM>> originalFileContentMap = ExecutorMobileThreads.fileContentMap;

            if (originalFileContentMap.isEmpty()) {
                logger.warn("No file content data available for {}-{}", flow, role);
                return CompletableFuture.completedFuture("NO_DATA");
            }

            // Create filtered map containing only transactions of specified types
            Map<Integer, List<FileContentM>> filteredFileContentMap = new HashMap<>();

            for (Map.Entry<Integer, List<FileContentM>> entry : originalFileContentMap.entrySet()) {
                Integer fileHeaderId = entry.getKey();
                List<FileContentM> allFileContents = entry.getValue();

                // Filter by transaction types for this flow
                List<FileContentM> filteredContents = allFileContents.stream()
                        .filter(fc -> transactionTypes.contains(fc.getTypeTransaction())
                                && merchantAccountribMap.get(fc.getRibRipBeneficiaire())!=null

                                && fc.getRibRipBeneficiaire().substring(0,3).equals(bankRepo.findIdentifiant().getIdentifiant().replace("'", ""))
                        )
                        .collect(Collectors.toList());

                for (FileContentM fileContent : filteredContents) {
                    if (fileContent.getTypeTransaction().equals("071")){
                        //settelement
                        DayOperationMP M121 = new DayOperationMP(fileContent, ExecutorMobileThreads.getSettlementByIdentification("M121"),methodName);
                        //commission
                        DayOperationMP M122 = new DayOperationMP(fileContent, ExecutorMobileThreads.getSettlementByIdentification("M122"),methodName);
                        //tva commission
                        DayOperationMP M123 = new DayOperationMP(fileContent, ExecutorMobileThreads.getSettlementByIdentification("M123"),methodName);
                        //interchange
                        DayOperationMP M124 = new DayOperationMP(fileContent, ExecutorMobileThreads.getSettlementByIdentification("M124"),methodName);
                        //tva interchange
                        DayOperationMP M125 = new DayOperationMP(fileContent, ExecutorMobileThreads.getSettlementByIdentification("M125"),methodName);
                        Map<String,DayOperationMP> list=new HashMap<>();
                        list.put("sett", M121);
                        list.put("commission", M122);
                        list.put("commissionTVA", M123);
                        list.put("Interchange", M124);
                        list.put("tvaInterchange", M125);
//                    if ("donneur".equals(sens)) {

//                    }else if ("benifeciaire".equals(sens)) {
                        correctiondayOperation(fileContent,list,"B",commissionByTypeMap.get("071"));
                        dayOperations.addAll(list.values());
                        if (stopSignal.get() || Thread.currentThread().isInterrupted()) {
                            logger.warn("Stop requested for {}-{} at transaction {}", flow, role, fileContent.getId());
                            throw new InterruptedException("Stop signal received");
                        }
                    }                    else if (fileContent.getTypeTransaction().equals("073")){
                        //settelement
                        DayOperationMP M126 = new DayOperationMP(fileContent, ExecutorMobileThreads.getSettlementByIdentification("M126"),methodName);
                        //commission
                        DayOperationMP M127 = new DayOperationMP(fileContent, ExecutorMobileThreads.getSettlementByIdentification("M127"),methodName);
                        //tva commission
                        DayOperationMP M128 = new DayOperationMP(fileContent, ExecutorMobileThreads.getSettlementByIdentification("M128"),methodName);
                        //interchange
                        DayOperationMP M129 = new DayOperationMP(fileContent, ExecutorMobileThreads.getSettlementByIdentification("M129"),methodName);
                        //tva interchange
                        DayOperationMP M130 = new DayOperationMP(fileContent, ExecutorMobileThreads.getSettlementByIdentification("M130"),methodName);
                        Map<String,DayOperationMP> list=new HashMap<>();
                        list.put("sett", M126);
                        list.put("commission", M127);
                        list.put("commissionTVA", M128);
                        list.put("Interchange", M129);
                        list.put("tvaInterchange", M130);
//                    if ("donneur".equals(sens)) {

//                    }else if ("benifeciaire".equals(sens)) {
                        correctiondayOperation(fileContent,list,"B",commissionByTypeMap.get("073"));
                        dayOperations.addAll(list.values());

                        if (stopSignal.get() || Thread.currentThread().isInterrupted()) {
                            logger.warn("Stop requested for {}-{} at transaction {}", flow, role, fileContent.getId());
                            throw new InterruptedException("Stop signal received");
                        }
                    }
                }

            }

            result = "SUCCESS";

        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            logger.warn("Cancelled {}-{} due to interruption", flow, role);
            throw new RuntimeException("CANCELLED: " + flow + "-" + role, ie);
        } catch (Exception e) {
            logger.error("Error in {}-{}", flow, role, e);
            throw new RuntimeException("ERROR: " + flow + "-" + role, e);
        }
        return CompletableFuture.completedFuture(result);
    }

    @Async("taskExecutor")
    public CompletableFuture<String> b2pAcquirer(AtomicBoolean stopSignal) {
        String methodName = getCurrentMethodName();
        // B2B transactions: 070 (Paiement mobile auprès des commerçants), 073 (Paiement de facture par mobile), 071 (Remboursement)
        List<String> transactionTypes = Arrays.asList("071");
        String flow = "B2P";
        String role = "ACQUIRER";
        String result = "ERROR";
        try {
            logger.info("Start processing {}-{} with transaction types: {}", flow, role, transactionTypes);

            // Access the prepared data from ExecutorMobileThreads static map
            Map<Integer, List<FileContentM>> originalFileContentMap = ExecutorMobileThreads.fileContentMap;

            if (originalFileContentMap.isEmpty()) {
                logger.warn("No file content data available for {}-{}", flow, role);
                return CompletableFuture.completedFuture("NO_DATA");
            }

            // Create filtered map containing only transactions of specified types
            Map<Integer, List<FileContentM>> filteredFileContentMap = new HashMap<>();

            for (Map.Entry<Integer, List<FileContentM>> entry : originalFileContentMap.entrySet()) {
                Integer fileHeaderId = entry.getKey();
                List<FileContentM> allFileContents = entry.getValue();

                // Filter by transaction types for this flow
                List<FileContentM> filteredContents = allFileContents.stream()
                        .filter(fc -> transactionTypes.contains(fc.getTypeTransaction())
                                && merchantAccountribMap.get(fc.getRibRipBeneficiaire())==null

                                && fc.getRibRipBeneficiaire().substring(0,3).equals(bankRepo.findIdentifiant().getIdentifiant().replace("'", ""))
                        )
                        .collect(Collectors.toList());

                for (FileContentM fileContent : filteredContents) {
                    if (fileContent.getTypeTransaction().equals("071")){
                        //settelement
                        DayOperationMP M036 = new DayOperationMP(fileContent, ExecutorMobileThreads.getSettlementByIdentification("M036"),methodName);
                        //commission
                        DayOperationMP M037 = new DayOperationMP(fileContent, ExecutorMobileThreads.getSettlementByIdentification("M037"),methodName);
                        //tva commission
                        DayOperationMP M038 = new DayOperationMP(fileContent, ExecutorMobileThreads.getSettlementByIdentification("M038"),methodName);
                        //interchange
                        DayOperationMP M039 = new DayOperationMP(fileContent, ExecutorMobileThreads.getSettlementByIdentification("M039"),methodName);
                        //tva interchange
                        DayOperationMP M040 = new DayOperationMP(fileContent, ExecutorMobileThreads.getSettlementByIdentification("M040"),methodName);
                        Map<String,DayOperationMP> list=new HashMap<>();
                        list.put("sett", M036);
                        list.put("commission", M037);
                        list.put("commissionTVA", M038);
                        list.put("Interchange", M039);
                        list.put("tvaInterchange", M040);
//                    if ("donneur".equals(sens)) {

//                    }else if ("benifeciaire".equals(sens)) {
                        correctiondayOperation(fileContent,list,"B", commissionByTypeMap.get("071"));
                        if (stopSignal.get() || Thread.currentThread().isInterrupted()) {
                            logger.warn("Stop requested for {}-{} at transaction {}", flow, role, fileContent.getId());
                            throw new InterruptedException("Stop signal received");
                        }
                        dayOperations.addAll(list.values());

                    }
                }

            }

            logger.info("{}-{}: Created filtered map with {} FileHeaders containing {} total transactions",
                    flow, role, filteredFileContentMap.size(),
                    filteredFileContentMap.values().stream().mapToInt(List::size).sum());

            int totalProcessed = 0;

            // Process each FileHeaderM and its filtered FileContentM list

            logger.info("Completed {}-{}: processed {} transactions from {} FileHeaders",
                    flow, role, totalProcessed, filteredFileContentMap.size());
            result = "SUCCESS";

        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            logger.warn("Cancelled {}-{} due to interruption", flow, role);
            throw new RuntimeException("CANCELLED: " + flow + "-" + role, ie);
        } catch (Exception e) {
            logger.error("Error in {}-{}", flow, role, e);
            throw new RuntimeException("ERROR: " + flow + "-" + role, e);
        }
        return CompletableFuture.completedFuture(result);
    }

    @Async("taskExecutor")
    public CompletableFuture<String> b2pEmitter(AtomicBoolean stopSignal) {        String methodName = getCurrentMethodName();
        // B2B transactions: 070 (Paiement mobile auprès des commerçants), 073 (Paiement de facture par mobile), 071 (Remboursement)
        List<String> transactionTypes = Arrays.asList("071");
        String flow = "B2P";
        String role = "EMITTER";
        String result = "ERROR";
        try {
            logger.info("Start processing {}-{} with transaction types: {}", flow, role, transactionTypes);

            // Access the prepared data from ExecutorMobileThreads static map
            Map<Integer, List<FileContentM>> originalFileContentMap = ExecutorMobileThreads.fileContentMap;

            if (originalFileContentMap.isEmpty()) {
                logger.warn("No file content data available for {}-{}", flow, role);
                return CompletableFuture.completedFuture("NO_DATA");
            }

            // Create filtered map containing only transactions of specified types
            Map<Integer, List<FileContentM>> filteredFileContentMap = new HashMap<>();

            for (Map.Entry<Integer, List<FileContentM>> entry : originalFileContentMap.entrySet()) {
                Integer fileHeaderId = entry.getKey();
                List<FileContentM> allFileContents = entry.getValue();

                // Filter by transaction types for this flow
                List<FileContentM> filteredContents = allFileContents.stream()
                        .filter(fc -> transactionTypes.contains(fc.getTypeTransaction())

                                && fc.getRibRipDonneur().substring(0,3).equals(bankRepo.findIdentifiant().getIdentifiant().replace("'", "")))
                        .collect(Collectors.toList());

                for (FileContentM fileContent : filteredContents) {
                    if (fileContent.getTypeTransaction().equals("071")){
                        //settelement
                        DayOperationMP M051 = new DayOperationMP(fileContent, ExecutorMobileThreads.getSettlementByIdentification("M051"),methodName);
                        //commission
                        DayOperationMP M052 = new DayOperationMP(fileContent, ExecutorMobileThreads.getSettlementByIdentification("M052"),methodName);
                        //tva commission
                        DayOperationMP M053 = new DayOperationMP(fileContent, ExecutorMobileThreads.getSettlementByIdentification("M053"),methodName);
                        //interchange
                        DayOperationMP M054 = new DayOperationMP(fileContent, ExecutorMobileThreads.getSettlementByIdentification("M054"),methodName);
                        //tva interchange
                        DayOperationMP M055 = new DayOperationMP(fileContent, ExecutorMobileThreads.getSettlementByIdentification("M055"),methodName);
                        Map<String,DayOperationMP> list=new HashMap<>();
                        list.put("sett", M051);
                        list.put("commission", M052);
                        list.put("commissionTVA", M053);
                        list.put("Interchange", M054);
                        list.put("tvaInterchange", M055);
//                    if ("donneur".equals(sens)) {

//                    }else if ("benifeciaire".equals(sens)) {
                        correctiondayOperation(fileContent,list,"B", commissionByTypeMap.get("071"));
                        if (stopSignal.get() || Thread.currentThread().isInterrupted()) {
                            logger.warn("Stop requested for {}-{} at transaction {}", flow, role, fileContent.getId());
                            throw new InterruptedException("Stop signal received");
                        }
                        dayOperations.addAll(list.values());

                    }
                }

            }

            logger.info("{}-{}: Created filtered map with {} FileHeaders containing {} total transactions",
                    flow, role, filteredFileContentMap.size(),
                    filteredFileContentMap.values().stream().mapToInt(List::size).sum());

            int totalProcessed = 0;

            // Process each FileHeaderM and its filtered FileContentM list

            logger.info("Completed {}-{}: processed {} transactions from {} FileHeaders",
                    flow, role, totalProcessed, filteredFileContentMap.size());
            result = "SUCCESS";

        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            logger.warn("Cancelled {}-{} due to interruption", flow, role);
            throw new RuntimeException("CANCELLED: " + flow + "-" + role, ie);
        } catch (Exception e) {
            logger.error("Error in {}-{}", flow, role, e);
            throw new RuntimeException("ERROR: " + flow + "-" + role, e);
        }
        return CompletableFuture.completedFuture(result);
    }

    @Async("taskExecutor")
    public CompletableFuture<String> p2pAcquirer(AtomicBoolean stopSignal) {
        String methodName = getCurrentMethodName();
        // P2P transactions: 072 (Transfert d'argent entre particuliers)
        List<String> transactionTypes = Arrays.asList("072");
        String flow = "P2P";
        String role = "ACQUIRER";
        String result = "ERROR";
        try {
            logger.info("Start processing {}-{} with transaction types: {}", flow, role, transactionTypes);

            // Access the prepared data from ExecutorMobileThreads static map
            Map<Integer, List<FileContentM>> originalFileContentMap = ExecutorMobileThreads.fileContentMap;

            if (originalFileContentMap.isEmpty()) {
                logger.warn("No file content data available for {}-{}", flow, role);
                return CompletableFuture.completedFuture("NO_DATA");
            }

            // Create filtered map containing only transactions of specified types
            Map<Integer, List<FileContentM>> filteredFileContentMap = new HashMap<>();

            for (Map.Entry<Integer, List<FileContentM>> entry : originalFileContentMap.entrySet()) {
                Integer fileHeaderId = entry.getKey();
                List<FileContentM> allFileContents = entry.getValue();

                // Filter by transaction types for this flow
                List<FileContentM> filteredContents = allFileContents.stream()
                        .filter(fc -> transactionTypes.contains(fc.getTypeTransaction())
                                && fc.getRibRipBeneficiaire().substring(0,3).equals(bankRepo.findIdentifiant().getIdentifiant().replace("'", ""))

                        )
                        .collect(Collectors.toList());
                for (FileContentM fileContent : filteredContents) {
                    if (fileContent.getTypeTransaction().equals("072")){
                        //settelement
                        DayOperationMP M061 = new DayOperationMP(fileContent, ExecutorMobileThreads.getSettlementByIdentification("M061"),methodName);
                        //commission
                        DayOperationMP M062 = new DayOperationMP(fileContent, ExecutorMobileThreads.getSettlementByIdentification("M062"),methodName);
                        //tva commission
                        DayOperationMP M063 = new DayOperationMP(fileContent, ExecutorMobileThreads.getSettlementByIdentification("M063"),methodName);
                        //interchange
                        DayOperationMP M064 = new DayOperationMP(fileContent, ExecutorMobileThreads.getSettlementByIdentification("M064"),methodName);
                        //tva interchange
                        DayOperationMP M065 = new DayOperationMP(fileContent, ExecutorMobileThreads.getSettlementByIdentification("M065"),methodName);
                        Map<String,DayOperationMP> list=new HashMap<>();
                        list.put("sett", M061);
                        list.put("commission", M062);
                        list.put("commissionTVA", M063);
                        list.put("Interchange", M064);
                        list.put("tvaInterchange", M065);
//                    if ("donneur".equals(sens)) {

//                    }else if ("benifeciaire".equals(sens)) {
                        correctiondayOperation(fileContent,list,"B",commissionByTypeMap.get("072"));
                        dayOperations.addAll(list.values());
                        if (stopSignal.get() || Thread.currentThread().isInterrupted()) {
                            logger.warn("Stop requested for {}-{} at transaction {}", flow, role, fileContent.getId());
                            throw new InterruptedException("Stop signal received");
                        }
                    }
                }

            }

            logger.info("{}-{}: Created filtered map with {} FileHeaders containing {} total transactions",
                    flow, role, filteredFileContentMap.size(),
                    filteredFileContentMap.values().stream().mapToInt(List::size).sum());

            int totalProcessed = 0;

            // Process each FileHeaderM and its filtered FileContentM list

            logger.info("Completed {}-{}: processed {} transactions from {} FileHeaders",
                    flow, role, totalProcessed, filteredFileContentMap.size());
            result = "SUCCESS";

        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            logger.warn("Cancelled {}-{} due to interruption", flow, role);
            throw new RuntimeException("CANCELLED: " + flow + "-" + role, ie);
        } catch (Exception e) {
            logger.error("Error in {}-{}", flow, role, e);
            throw new RuntimeException("ERROR: " + flow + "-" + role, e);
        }
        return CompletableFuture.completedFuture(result);
    }

    @Async("taskExecutor")
    public CompletableFuture<String> p2pEmitter(AtomicBoolean stopSignal) {        String methodName = getCurrentMethodName();
        // P2P transactions: 072 (Transfert d'argent entre particuliers)
        List<String> transactionTypes = Arrays.asList("072");
        String flow = "P2P";
        String role = "EMITTER";
        String result = "ERROR";
        try {
            logger.info("Start processing {}-{} with transaction types: {}", flow, role, transactionTypes);

            // Access the prepared data from ExecutorMobileThreads static map
            Map<Integer, List<FileContentM>> originalFileContentMap = ExecutorMobileThreads.fileContentMap;

            if (originalFileContentMap.isEmpty()) {
                logger.warn("No file content data available for {}-{}", flow, role);
                return CompletableFuture.completedFuture("NO_DATA");
            }

            // Create filtered map containing only transactions of specified types
            Map<Integer, List<FileContentM>> filteredFileContentMap = new HashMap<>();

            for (Map.Entry<Integer, List<FileContentM>> entry : originalFileContentMap.entrySet()) {
                Integer fileHeaderId = entry.getKey();
                List<FileContentM> allFileContents = entry.getValue();

                // Filter by transaction types for this flow
                List<FileContentM> filteredContents = allFileContents.stream()
                        .filter(fc -> transactionTypes.contains(fc.getTypeTransaction())

                                && fc.getRibRipDonneur().substring(0,3).equals(bankRepo.findIdentifiant().getIdentifiant().replace("'", ""))
                        )
                        .collect(Collectors.toList());

                for (FileContentM fileContent : filteredContents) {
                    if (fileContent.getTypeTransaction().equals("072")){
                        //settelement
                        DayOperationMP M066 = new DayOperationMP(fileContent, ExecutorMobileThreads.getSettlementByIdentification("M066"),methodName);
                        //commission
                        DayOperationMP M067 = new DayOperationMP(fileContent, ExecutorMobileThreads.getSettlementByIdentification("M067"),methodName);
                        //tva commission
                        DayOperationMP M068 = new DayOperationMP(fileContent, ExecutorMobileThreads.getSettlementByIdentification("M068"),methodName);
                        //interchange
                        DayOperationMP M069 = new DayOperationMP(fileContent, ExecutorMobileThreads.getSettlementByIdentification("M069"),methodName);
                        //tva interchange
                        DayOperationMP M070 = new DayOperationMP(fileContent, ExecutorMobileThreads.getSettlementByIdentification("M070"),methodName);
                        Map<String,DayOperationMP> list=new HashMap<>();
                        list.put("sett", M066);
                        list.put("commission", M067);
                        list.put("commissionTVA", M068);
                        list.put("Interchange", M069);
                        list.put("tvaInterchange", M070);
//                    if ("donneur".equals(sens)) {

//                    }else if ("benifeciaire".equals(sens)) {
                        correctiondayOperation(fileContent,list,"D",commissionByTypeMap.get("072"));
                        dayOperations.addAll(list.values());
                        if (stopSignal.get() || Thread.currentThread().isInterrupted()) {
                            logger.warn("Stop requested for {}-{} at transaction {}", flow, role, fileContent.getId());
                            throw new InterruptedException("Stop signal received");
                        }
                    }
                }
            }

            logger.info("{}-{}: Created filtered map with {} FileHeaders containing {} total transactions",
                    flow, role, filteredFileContentMap.size(),
                    filteredFileContentMap.values().stream().mapToInt(List::size).sum());

            int totalProcessed = 0;

            logger.info("Completed {}-{}: processed {} transactions from {} FileHeaders",
                    flow, role, totalProcessed, filteredFileContentMap.size());
            result = "SUCCESS";

        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            logger.warn("Cancelled {}-{} due to interruption", flow, role);
            throw new RuntimeException("CANCELLED: " + flow + "-" + role, ie);
        } catch (Exception e) {
            logger.error("Error in {}-{}", flow, role, e);
            throw new RuntimeException("ERROR: " + flow + "-" + role, e);
        }
        return CompletableFuture.completedFuture(result);
    }

    @Async("taskExecutor")
    public CompletableFuture<String> p2gEmitter(AtomicBoolean stopSignal) {        String methodName = getCurrentMethodName();
        // P2G transactions: 074 (Paiement de charges fiscales et parafiscales)
        List<String> transactionTypes = Arrays.asList("074");
        String flow = "P2G";
        String role = "EMITTER";
        String result = "ERROR";
        try {
            logger.info("Start processing {}-{} with transaction types: {}", flow, role, transactionTypes);

            // Access the prepared data from ExecutorMobileThreads static map
            Map<Integer, List<FileContentM>> originalFileContentMap = ExecutorMobileThreads.fileContentMap;

            if (originalFileContentMap.isEmpty()) {
                logger.warn("No file content data available for {}-{}", flow, role);
                return CompletableFuture.completedFuture("NO_DATA");
            }

            // Create filtered map containing only transactions of specified types
            Map<Integer, List<FileContentM>> filteredFileContentMap = new HashMap<>();

            for (Map.Entry<Integer, List<FileContentM>> entry : originalFileContentMap.entrySet()) {
                Integer fileHeaderId = entry.getKey();
                List<FileContentM> allFileContents = entry.getValue();

                // Filter by transaction types for this flow
                List<FileContentM> filteredContents = allFileContents.stream()
                        .filter(fc -> transactionTypes.contains(fc.getTypeTransaction())
                                && merchantAccountribMap.get(fc.getRibRipDonneur().substring(1))==null
                                && fc.getRibRipDonneur().substring(0,3).equals(bankRepo.findIdentifiant().getIdentifiant().replace("'", ""))
                        )
                        .collect(Collectors.toList());
                for (FileContentM fileContent : filteredContents) {
                    if (fileContent.getTypeTransaction().equals("074")){
                        //settelement
                        DayOperationMP M076 = new DayOperationMP(fileContent, ExecutorMobileThreads.getSettlementByIdentification("M076"),methodName);
                        //commission
                        DayOperationMP M077 = new DayOperationMP(fileContent, ExecutorMobileThreads.getSettlementByIdentification("M077"),methodName);
                        //tva commission
                        DayOperationMP M078 = new DayOperationMP(fileContent, ExecutorMobileThreads.getSettlementByIdentification("M078"),methodName);
                        //interchange
                        DayOperationMP M079 = new DayOperationMP(fileContent, ExecutorMobileThreads.getSettlementByIdentification("M079"),methodName);
                        //tva interchange
                        DayOperationMP M080 = new DayOperationMP(fileContent, ExecutorMobileThreads.getSettlementByIdentification("M080"),methodName);
                        Map<String,DayOperationMP> list=new HashMap<>();
                        list.put("sett", M076);
                        list.put("commission", M077);
                        list.put("commissionTVA", M078);
                        list.put("Interchange", M079);
                        list.put("tvaInterchange", M080);
//                    if ("donneur".equals(sens)) {

//                    }else if ("benifeciaire".equals(sens)) {
                        correctiondayOperation(fileContent,list,"D",commissionByTypeMap.get("074"));
                        dayOperations.addAll(list.values());
                        if (stopSignal.get() || Thread.currentThread().isInterrupted()) {
                            logger.warn("Stop requested for {}-{} at transaction {}", flow, role, fileContent.getId());
                            throw new InterruptedException("Stop signal received");
                        }
                    }
                }

            }

            logger.info("{}-{}: Created filtered map with {} FileHeaders containing {} total transactions",
                    flow, role, filteredFileContentMap.size(),
                    filteredFileContentMap.values().stream().mapToInt(List::size).sum());

            int totalProcessed = 0;


            logger.info("Completed {}-{}: processed {} transactions from {} FileHeaders",
                    flow, role, totalProcessed, filteredFileContentMap.size());
            result = "SUCCESS";

        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            logger.warn("Cancelled {}-{} due to interruption", flow, role);
            throw new RuntimeException("CANCELLED: " + flow + "-" + role, ie);
        } catch (Exception e) {
            logger.error("Error in {}-{}", flow, role, e);
            throw new RuntimeException("ERROR: " + flow + "-" + role, e);
        }
        return CompletableFuture.completedFuture(result);
    }

    @Async("taskExecutor")
    public CompletableFuture<String> b2gAcquirer(AtomicBoolean stopSignal) {        String methodName = getCurrentMethodName();
        // B2G transactions: 074 (Paiement de charges fiscales et parafiscales)
        List<String> transactionTypes = Arrays.asList("074");
        String flow = "B2G";
        String role = "ACQUIRER";
        String result = "ERROR";
        try {
            logger.info("Start processing {}-{} with transaction types: {}", flow, role, transactionTypes);

            // Access the prepared data from ExecutorMobileThreads static map
            Map<Integer, List<FileContentM>> originalFileContentMap = ExecutorMobileThreads.fileContentMap;

            if (originalFileContentMap.isEmpty()) {
                logger.warn("No file content data available for {}-{}", flow, role);
                return CompletableFuture.completedFuture("NO_DATA");
            }

            // Create filtered map containing only transactions of specified types
            Map<Integer, List<FileContentM>> filteredFileContentMap = new HashMap<>();

            for (Map.Entry<Integer, List<FileContentM>> entry : originalFileContentMap.entrySet()) {
                Integer fileHeaderId = entry.getKey();
                List<FileContentM> allFileContents = entry.getValue();

                // Filter by transaction types for this flow
                List<FileContentM> filteredContents = allFileContents.stream()
                        .filter(fc -> transactionTypes.contains(fc.getTypeTransaction())
                                && merchantAccountribMap.get(fc.getRibRipDonneur().substring(1))!=null

                                && fc.getRibRipBeneficiaire().substring(0,3).equals(bankRepo.findIdentifiant().getIdentifiant().replace("'", ""))
                        )
                        .collect(Collectors.toList());

                for (FileContentM fileContent : filteredContents) {
                    if (fileContent.getTypeTransaction().equals("074")){
                        //settelement
                        DayOperationMP M081 = new DayOperationMP(fileContent, ExecutorMobileThreads.getSettlementByIdentification("M081"),methodName);
                        //commission
                        DayOperationMP M082 = new DayOperationMP(fileContent, ExecutorMobileThreads.getSettlementByIdentification("M082"),methodName);
                        //tva commission
                        DayOperationMP M083 = new DayOperationMP(fileContent, ExecutorMobileThreads.getSettlementByIdentification("M083"),methodName);
                        //interchange
                        DayOperationMP M084 = new DayOperationMP(fileContent, ExecutorMobileThreads.getSettlementByIdentification("M084"),methodName);
                        //tva interchange
                        DayOperationMP M085 = new DayOperationMP(fileContent, ExecutorMobileThreads.getSettlementByIdentification("M085"),methodName);
                        Map<String,DayOperationMP> list=new HashMap<>();
                        list.put("sett", M081);
                        list.put("commission", M082);
                        list.put("commissionTVA", M083);
                        list.put("Interchange", M084);
                        list.put("tvaInterchange", M085);
//                    if ("donneur".equals(sens)) {

//                    }else if ("benifeciaire".equals(sens)) {
                        correctiondayOperation(fileContent,list,"D",commissionByTypeMap.get("074"));
                        dayOperations.addAll(list.values());
                        if (stopSignal.get() || Thread.currentThread().isInterrupted()) {
                            logger.warn("Stop requested for {}-{} at transaction {}", flow, role, fileContent.getId());
                            throw new InterruptedException("Stop signal received");
                        }
                    }
                }

            }

            logger.info("{}-{}: Created filtered map with {} FileHeaders containing {} total transactions",
                    flow, role, filteredFileContentMap.size(),
                    filteredFileContentMap.values().stream().mapToInt(List::size).sum());

            int totalProcessed = 0;

            // Process each FileHeaderM and its filtered FileContentM list

            logger.info("Completed {}-{}: processed {} transactions from {} FileHeaders",
                    flow, role, totalProcessed, filteredFileContentMap.size());
            result = "SUCCESS";

        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            logger.warn("Cancelled {}-{} due to interruption", flow, role);
            throw new RuntimeException("CANCELLED: " + flow + "-" + role, ie);
        } catch (Exception e) {
            logger.error("Error in {}-{}", flow, role, e);
            throw new RuntimeException("ERROR: " + flow + "-" + role, e);
        }
        return CompletableFuture.completedFuture(result);
    }

    @Async("taskExecutor")
    public CompletableFuture<String> b2gEmitter(AtomicBoolean stopSignal) {        String methodName = getCurrentMethodName();
        // B2G transactions: 074 (Paiement de charges fiscales et parafiscales)
        List<String> transactionTypes = Arrays.asList("074");
        String flow = "B2G";
        String role = "EMITTER";
        String result = "ERROR";
        try {
            logger.info("Start processing {}-{} with transaction types: {}", flow, role, transactionTypes);

            // Access the prepared data from ExecutorMobileThreads static map
            Map<Integer, List<FileContentM>> originalFileContentMap = ExecutorMobileThreads.fileContentMap;

            if (originalFileContentMap.isEmpty()) {
                logger.warn("No file content data available for {}-{}", flow, role);
                return CompletableFuture.completedFuture("NO_DATA");
            }

            // Create filtered map containing only transactions of specified types
            Map<Integer, List<FileContentM>> filteredFileContentMap = new HashMap<>();

            for (Map.Entry<Integer, List<FileContentM>> entry : originalFileContentMap.entrySet()) {
                Integer fileHeaderId = entry.getKey();
                List<FileContentM> allFileContents = entry.getValue();

                // Filter by transaction types for this flow
                List<FileContentM> filteredContents = allFileContents.stream()
                        .filter(fc -> transactionTypes.contains(fc.getTypeTransaction())
                                && merchantAccountribMap.get(fc.getRibRipDonneur().substring(1))!=null

                                && fc.getRibRipDonneur().substring(0,3).equals(bankRepo.findIdentifiant().getIdentifiant().replace("'", "")))
                        .collect(Collectors.toList());

                for (FileContentM fileContent : filteredContents) {
                    if (fileContent.getTypeTransaction().equals("074")){
                        //settelement
                        DayOperationMP M086 = new DayOperationMP(fileContent, ExecutorMobileThreads.getSettlementByIdentification("M086"),methodName);
                        //commission
                        DayOperationMP M087 = new DayOperationMP(fileContent, ExecutorMobileThreads.getSettlementByIdentification("M087"),methodName);
                        //tva commission
                        DayOperationMP M088 = new DayOperationMP(fileContent, ExecutorMobileThreads.getSettlementByIdentification("M088"),methodName);
                        //interchange
                        DayOperationMP M089 = new DayOperationMP(fileContent, ExecutorMobileThreads.getSettlementByIdentification("M089"),methodName);
                        //tva interchange
                        DayOperationMP M090 = new DayOperationMP(fileContent, ExecutorMobileThreads.getSettlementByIdentification("M090"),methodName);
                        Map<String,DayOperationMP> list=new HashMap<>();
                        list.put("sett", M086);
                        list.put("commission", M087);
                        list.put("commissionTVA", M088);
                        list.put("Interchange", M089);
                        list.put("tvaInterchange", M090);
//                    if ("donneur".equals(sens)) {

//                    }else if ("benifeciaire".equals(sens)) {
                        correctiondayOperation(fileContent,list,"D",commissionByTypeMap.get("074"));
                        dayOperations.addAll(list.values());
                        if (stopSignal.get() || Thread.currentThread().isInterrupted()) {
                            logger.warn("Stop requested for {}-{} at transaction {}", flow, role, fileContent.getId());
                            throw new InterruptedException("Stop signal received");
                        }
                    }
                }

            }

            logger.info("{}-{}: Created filtered map with {} FileHeaders containing {} total transactions",
                    flow, role, filteredFileContentMap.size(),
                    filteredFileContentMap.values().stream().mapToInt(List::size).sum());

            int totalProcessed = 0;

            // Process each FileHeaderM and its filtered FileContentM list

            logger.info("Completed {}-{}: processed {} transactions from {} FileHeaders",
                    flow, role, totalProcessed, filteredFileContentMap.size());
            result = "SUCCESS";

        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            logger.warn("Cancelled {}-{} due to interruption", flow, role);
            throw new RuntimeException("CANCELLED: " + flow + "-" + role, ie);
        } catch (Exception e) {
            logger.error("Error in {}-{}", flow, role, e);
            throw new RuntimeException("ERROR: " + flow + "-" + role, e);
        }
        return CompletableFuture.completedFuture(result);
    }

    @Async("taskExecutor")
    public CompletableFuture<String> g2pAcquirer(AtomicBoolean stopSignal) {        String methodName = getCurrentMethodName();
        // G2P transactions: 075 (Transferts sociaux et subventions)
        List<String> transactionTypes = Arrays.asList("075");
        String flow = "G2P";
        String role = "ACQUIRER";
        String result = "ERROR";
        try {
            logger.info("Start processing {}-{} with transaction types: {}", flow, role, transactionTypes);

            // Access the prepared data from ExecutorMobileThreads static map
            Map<Integer, List<FileContentM>> originalFileContentMap = ExecutorMobileThreads.fileContentMap;

            if (originalFileContentMap.isEmpty()) {
                logger.warn("No file content data available for {}-{}", flow, role);
                return CompletableFuture.completedFuture("NO_DATA");
            }

            // Create filtered map containing only transactions of specified types
            Map<Integer, List<FileContentM>> filteredFileContentMap = new HashMap<>();

            for (Map.Entry<Integer, List<FileContentM>> entry : originalFileContentMap.entrySet()) {
                Integer fileHeaderId = entry.getKey();
                List<FileContentM> allFileContents = entry.getValue();

                // Filter by transaction types for this flow
                List<FileContentM> filteredContents = allFileContents.stream()
                        .filter(fc -> transactionTypes.contains(fc.getTypeTransaction())
                                && fc.getRibRipBeneficiaire().substring(0,3).equals(bankRepo.findIdentifiant().getIdentifiant().replace("'", ""))                         )
                        .collect(Collectors.toList());

                for (FileContentM fileContent : filteredContents) {
                    if (fileContent.getTypeTransaction().equals("075")){
                        //settelement
                        DayOperationMP M091 = new DayOperationMP(fileContent, ExecutorMobileThreads.getSettlementByIdentification("M091"),methodName);
                        //commission
                        DayOperationMP M092 = new DayOperationMP(fileContent, ExecutorMobileThreads.getSettlementByIdentification("M092"),methodName);
                        //tva commission
                        DayOperationMP M093 = new DayOperationMP(fileContent, ExecutorMobileThreads.getSettlementByIdentification("M093"),methodName);
                        //interchange
                        DayOperationMP M094 = new DayOperationMP(fileContent, ExecutorMobileThreads.getSettlementByIdentification("M094"),methodName);
                        //tva interchange
                        DayOperationMP M095 = new DayOperationMP(fileContent, ExecutorMobileThreads.getSettlementByIdentification("M095"),methodName);
                        Map<String,DayOperationMP> list=new HashMap<>();
                        list.put("sett", M091);
                        list.put("commission", M092);
                        list.put("commissionTVA", M093);
                        list.put("Interchange", M094);
                        list.put("tvaInterchange", M095);
//                    if ("donneur".equals(sens)) {

//                    }else if ("benifeciaire".equals(sens)) {
                        correctiondayOperation(fileContent,list,"B",commissionByTypeMap.get("075"));
                        dayOperations.addAll(list.values());
                        if (stopSignal.get() || Thread.currentThread().isInterrupted()) {
                            logger.warn("Stop requested for {}-{} at transaction {}", flow, role, fileContent.getId());
                            throw new InterruptedException("Stop signal received");
                        }
                    }
                }

            }

            logger.info("{}-{}: Created filtered map with {} FileHeaders containing {} total transactions",
                    flow, role, filteredFileContentMap.size(),
                    filteredFileContentMap.values().stream().mapToInt(List::size).sum());

            int totalProcessed = 0;

            // Process each FileHeaderM and its filtered FileContentM list
            for (Map.Entry<Integer, List<FileContentM>> entry : filteredFileContentMap.entrySet()) {
                Integer fileHeaderId = entry.getKey();
                List<FileContentM> filteredContents = entry.getValue();

                logger.info("{}-{}: Processing FileHeaderM id: {} with {} filtered transactions",
                        flow, role, fileHeaderId, filteredContents.size());

                // Process each filtered transaction
            }

            logger.info("Completed {}-{}: processed {} transactions from {} FileHeaders",
                    flow, role, totalProcessed, filteredFileContentMap.size());
            result = "SUCCESS";

        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            logger.warn("Cancelled {}-{} due to interruption", flow, role);
            throw new RuntimeException("CANCELLED: " + flow + "-" + role, ie);
        } catch (Exception e) {
            logger.error("Error in {}-{}", flow, role, e);
            throw new RuntimeException("ERROR: " + flow + "-" + role, e);
        }

        return CompletableFuture.completedFuture(result);
    }

    @Async("taskExecutor")
    public CompletableFuture<String> g2pEmitter(AtomicBoolean stopSignal) {        String methodName = getCurrentMethodName();
        // G2P transactions: 075 (Transferts sociaux et subventions)
        List<String> transactionTypes = Arrays.asList("075");
        String flow = "G2P";
        String role = "EMITTER";
        String result = "ERROR";
        try {
            logger.info("Start processing {}-{} with transaction types: {}", flow, role, transactionTypes);

            // Access the prepared data from ExecutorMobileThreads static map
            Map<Integer, List<FileContentM>> originalFileContentMap = ExecutorMobileThreads.fileContentMap;

            if (originalFileContentMap.isEmpty()) {
                logger.warn("No file content data available for {}-{}", flow, role);
                return CompletableFuture.completedFuture("NO_DATA");
            }

            // Create filtered map containing only transactions of specified types
            Map<Integer, List<FileContentM>> filteredFileContentMap = new HashMap<>();

            for (Map.Entry<Integer, List<FileContentM>> entry : originalFileContentMap.entrySet()) {
                Integer fileHeaderId = entry.getKey();
                List<FileContentM> allFileContents = entry.getValue();

                // Filter by transaction types for this flow
                List<FileContentM> filteredContents = allFileContents.stream()
                        .filter(fc -> transactionTypes.contains(fc.getTypeTransaction())
                                && fc.getRibRipDonneur().substring(0,3).equals(bankRepo.findIdentifiant().getIdentifiant().replace("'", ""))
                        )
                        .collect(Collectors.toList());

                for (FileContentM fileContent : filteredContents) {
                    if (fileContent.getTypeTransaction().equals("075")){
                        //settelement
                        DayOperationMP M096 = new DayOperationMP(fileContent, ExecutorMobileThreads.getSettlementByIdentification("M096"),methodName);
                        //commission
                        DayOperationMP M097 = new DayOperationMP(fileContent, ExecutorMobileThreads.getSettlementByIdentification("M097"),methodName);
                        //tva commission
                        DayOperationMP M098 = new DayOperationMP(fileContent, ExecutorMobileThreads.getSettlementByIdentification("M098"),methodName);
                        //interchange
                        DayOperationMP M099 = new DayOperationMP(fileContent, ExecutorMobileThreads.getSettlementByIdentification("M099"),methodName);
                        //tva interchange
                        DayOperationMP M100 = new DayOperationMP(fileContent, ExecutorMobileThreads.getSettlementByIdentification("M100"),methodName);
                        Map<String,DayOperationMP> list=new HashMap<>();
                        list.put("sett", M096);
                        list.put("commission", M097);
                        list.put("commissionTVA", M098);
                        list.put("Interchange", M099);
                        list.put("tvaInterchange", M100);
//                    if ("donneur".equals(sens)) {

//                    }else if ("benifeciaire".equals(sens)) {
                        correctiondayOperation(fileContent,list,"B",commissionByTypeMap.get("075"));
                        dayOperations.addAll(list.values());
                        if (stopSignal.get() || Thread.currentThread().isInterrupted()) {
                            logger.warn("Stop requested for {}-{} at transaction {}", flow, role, fileContent.getId());
                            throw new InterruptedException("Stop signal received");
                        }
                    }
                }

            }

            logger.info("{}-{}: Created filtered map with {} FileHeaders containing {} total transactions",
                    flow, role, filteredFileContentMap.size(),
                    filteredFileContentMap.values().stream().mapToInt(List::size).sum());

            int totalProcessed = 0;

            // Process each FileHeaderM and its filtered FileContentM list

            logger.info("Completed {}-{}: processed {} transactions from {} FileHeaders",
                    flow, role, totalProcessed, filteredFileContentMap.size());
            result = "SUCCESS";

        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            logger.warn("Cancelled {}-{} due to interruption", flow, role);
            throw new RuntimeException("CANCELLED: " + flow + "-" + role, ie);
        } catch (Exception e) {
            logger.error("Error in {}-{}", flow, role, e);
            throw new RuntimeException("ERROR: " + flow + "-" + role, e);
        }

        return CompletableFuture.completedFuture(result);
    }

    @Async("taskExecutor")
    public CompletableFuture<String> chargeBacksAcquirer(AtomicBoolean stopSignal) {        String methodName = getCurrentMethodName();
        // Chargeback transactions: 080 (Chargeback)
        List<String> transactionTypes = Arrays.asList("080");
        String flow = "ChargeBacks";
        String role = "ACQUIRER";
        String result = "ERROR";
        try {
            logger.info("Start processing {}-{} with transaction types: {}", flow, role, transactionTypes);

            // Access the prepared data from ExecutorMobileThreads static map
            Map<Integer, List<FileContentM>> originalFileContentMap = ExecutorMobileThreads.fileContentMap;

            if (originalFileContentMap.isEmpty()) {
                logger.warn("No file content data available for {}-{}", flow, role);
                return CompletableFuture.completedFuture("NO_DATA");
            }

            // Create filtered map containing only transactions of specified types
            Map<Integer, List<FileContentM>> filteredFileContentMap = new HashMap<>();

            for (Map.Entry<Integer, List<FileContentM>> entry : originalFileContentMap.entrySet()) {
                Integer fileHeaderId = entry.getKey();
                List<FileContentM> allFileContents = entry.getValue();

                // Filter by transaction types for this flow
                List<FileContentM> filteredContents = allFileContents.stream()
                        .filter(fc -> transactionTypes.contains(fc.getTypeTransaction())

                                && fc.getRibRipDonneur().substring(0,3).equals(bankRepo.findIdentifiant().getIdentifiant().replace("'", "")))
                        .collect(Collectors.toList());

                for (FileContentM fileContent : filteredContents) {
                    if (fileContent.getTypeTransaction().equals("080")){
                        //settelement
                        DayOperationMP M101 = new DayOperationMP(fileContent, ExecutorMobileThreads.getSettlementByIdentification("M101"),methodName);
                        //commission
                        DayOperationMP M102 = new DayOperationMP(fileContent, ExecutorMobileThreads.getSettlementByIdentification("M102"),methodName);
                        //tva commission
                        DayOperationMP M103 = new DayOperationMP(fileContent, ExecutorMobileThreads.getSettlementByIdentification("M103"),methodName);
                        //interchange
                        DayOperationMP M104 = new DayOperationMP(fileContent, ExecutorMobileThreads.getSettlementByIdentification("M104"),methodName);
                        //tva interchange
                        DayOperationMP M105 = new DayOperationMP(fileContent, ExecutorMobileThreads.getSettlementByIdentification("M105"),methodName);
                        Map<String,DayOperationMP> list=new HashMap<>();
                        list.put("sett", M101);
                        list.put("commission", M102);
                        list.put("commissionTVA", M103);
                        list.put("Interchange", M104);
                        list.put("tvaInterchange", M105);
//                    if ("donneur".equals(sens)) {

//                    }else if ("benifeciaire".equals(sens)) {
                        correctiondayOperation(fileContent,list,"benifeciaire",commissionByTypeMap.get("080"));
                        dayOperations.addAll(list.values());
                        if (stopSignal.get() || Thread.currentThread().isInterrupted()) {
                            logger.warn("Stop requested for {}-{} at transaction {}", flow, role, fileContent.getId());
                            throw new InterruptedException("Stop signal received");
                        }
                    }
                }

            }

            logger.info("{}-{}: Created filtered map with {} FileHeaders containing {} total transactions",
                    flow, role, filteredFileContentMap.size(),
                    filteredFileContentMap.values().stream().mapToInt(List::size).sum());

            int totalProcessed = 0;


            logger.info("Completed {}-{}: processed {} transactions from {} FileHeaders",
                    flow, role, totalProcessed, filteredFileContentMap.size());
            result = "SUCCESS";

        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            logger.warn("Cancelled {}-{} due to interruption", flow, role);
            throw new RuntimeException("CANCELLED: " + flow + "-" + role, ie);
        } catch (Exception e) {
            logger.error("Error in {}-{}", flow, role, e);
            throw new RuntimeException("ERROR: " + flow + "-" + role, e);
        }

        return CompletableFuture.completedFuture(result);
    }

    @Async("taskExecutor")
    public CompletableFuture<String> chargeBacksEmitter(AtomicBoolean stopSignal) {        String methodName = getCurrentMethodName();
        // Chargeback transactions: 080 (Chargeback)
        List<String> transactionTypes = Arrays.asList("080");
        String flow = "ChargeBacks";
        String role = "EMITTER";
        String result = "ERROR";
        try {
            logger.info("Start processing {}-{} with transaction types: {}", flow, role, transactionTypes);

            // Access the prepared data from ExecutorMobileThreads static map
            Map<Integer, List<FileContentM>> originalFileContentMap = ExecutorMobileThreads.fileContentMap;

            if (originalFileContentMap.isEmpty()) {
                logger.warn("No file content data available for {}-{}", flow, role);
                return CompletableFuture.completedFuture("NO_DATA");
            }

            // Create filtered map containing only transactions of specified types
            Map<Integer, List<FileContentM>> filteredFileContentMap = new HashMap<>();

            for (Map.Entry<Integer, List<FileContentM>> entry : originalFileContentMap.entrySet()) {
                Integer fileHeaderId = entry.getKey();
                List<FileContentM> allFileContents = entry.getValue();

                // Filter by transaction types for this flow
                List<FileContentM> filteredContents = allFileContents.stream()
                        .filter(fc -> transactionTypes.contains(fc.getTypeTransaction())
                                && fc.getRibRipBeneficiaire().substring(0,3).equals(bankRepo.findIdentifiant().getIdentifiant().replace("'", ""))
                        )
                        .collect(Collectors.toList());
                for (FileContentM fileContent : filteredContents) {
                    if (fileContent.getTypeTransaction().equals("080")){
                        //settelement
                        DayOperationMP M106 = new DayOperationMP(fileContent, ExecutorMobileThreads.getSettlementByIdentification("M106"),methodName);
                        //commission
                        DayOperationMP M107 = new DayOperationMP(fileContent, ExecutorMobileThreads.getSettlementByIdentification("M107"),methodName);
                        //tva commission
                        DayOperationMP M108 = new DayOperationMP(fileContent, ExecutorMobileThreads.getSettlementByIdentification("M108"),methodName);
                        //interchange
                        DayOperationMP M109 = new DayOperationMP(fileContent, ExecutorMobileThreads.getSettlementByIdentification("M109"),methodName);
                        //tva interchange
                        DayOperationMP M110 = new DayOperationMP(fileContent, ExecutorMobileThreads.getSettlementByIdentification("M110"),methodName);
                        Map<String,DayOperationMP> list=new HashMap<>();
                        list.put("sett", M106);
                        list.put("commission", M107);
                        list.put("commissionTVA", M108);
                        list.put("Interchange", M109);
                        list.put("tvaInterchange", M110);
//                    if ("donneur".equals(sens)) {

//                    }else if ("benifeciaire".equals(sens)) {
                        correctiondayOperation(fileContent,list,"donneur",commissionByTypeMap.get("080"));
                        dayOperations.addAll(list.values());
                        if (stopSignal.get() || Thread.currentThread().isInterrupted()) {
                            logger.warn("Stop requested for {}-{} at transaction {}", flow, role, fileContent.getId());
                            throw new InterruptedException("Stop signal received");
                        }
                    }
                }

            }

            logger.info("{}-{}: Created filtered map with {} FileHeaders containing {} total transactions",
                    flow, role, filteredFileContentMap.size(),
                    filteredFileContentMap.values().stream().mapToInt(List::size).sum());

            int totalProcessed = 0;


            logger.info("Completed {}-{}: processed {} transactions from {} FileHeaders",
                    flow, role, totalProcessed, filteredFileContentMap.size());
            result = "SUCCESS";

        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            logger.warn("Cancelled {}-{} due to interruption", flow, role);
            throw new RuntimeException("CANCELLED: " + flow + "-" + role, ie);
        } catch (Exception e) {
            logger.error("Error in {}-{}", flow, role, e);
            throw new RuntimeException("ERROR: " + flow + "-" + role, e);
        }

        return CompletableFuture.completedFuture(result);
    }

    public void correctiondayOperation(FileContentM tp, Map<String, DayOperationMP> days, String sens, List<CommissionMobile> commissionMobiles) {
        DayOperationMP interchangeD = days.get("Interchange");
        DayOperationMP tvaInterchangeD = days.get("tvaInterchange");
        DayOperationMP commissionHTD = days.get("commission");
        DayOperationMP commissionTVAD = days.get("commissionTVA");
        BigDecimal tva = new BigDecimal(tvaRepo.findTva().getTva());
        tva = tva.divide(new BigDecimal(100));
        BigDecimal tvaDevided = tva.add(new BigDecimal(1));
        for (CommissionMobile commission : commissionMobiles) {
            BigDecimal mntTransaction = new BigDecimal(interchangeD.getMontantTransaction());
            BigDecimal valeurMax = new BigDecimal(commission.getValeurMax());
            BigDecimal valeurMin = new BigDecimal(commission.getValeurMin());

            if (valeurMax.compareTo(mntTransaction) >= 0 && valeurMin.compareTo(mntTransaction) < 0) {
                BigDecimal valeurFix = new BigDecimal(commission.getValeurFix());
                BigDecimal valeurVariable = new BigDecimal(commission.getValeurVarivable());
                BigDecimal hundred = new BigDecimal(100);

                // Calculate commission: valeurFix + ((valeurVariable * mntTransaction) / 100)
                BigDecimal commissionHT = valeurFix.add(
                        valeurVariable.multiply(mntTransaction).divide(hundred, RoundingMode.HALF_UP)
                );
                commissionHTD.setMontantSettlement(commissionHT);

                // Calculate TVA commission: commissionHT * tva
                BigDecimal tvacomm = commissionHT.multiply(tva);
                commissionTVAD.setMontantSettlement(tvacomm);
                break;
            }
        }
        if (interchangeD != null) {
            interchangeD.setMontantSettlement(interchangeD.getMontantSettlement());
        }
        if (tvaInterchangeD != null) {
            tvaInterchangeD.setMontantSettlement((tvaInterchangeD.getMontantSettlement()));
        }

        if (commissionHTD != null) {
            commissionHTD.setMontantSettlement(commissionHTD.getMontantSettlement());
        }
        if (commissionTVAD != null) {
            commissionTVAD.setMontantSettlement(commissionTVAD.getMontantSettlement());
        }
        BigDecimal interchangeTTC =null;
        BigDecimal commissionTTc =null;
        interchangeTTC= new BigDecimal(tp.getCommissionInterchange().replace(".", ""));
        interchangeTTC=interchangeTTC.divide(new BigDecimal(100));
//        commissionTTc= new BigDecimal(tp.getCommissionDonneurOrdre().replace(".", ""));
//
//        if ("D".equals(sens)) {
//            commissionTTc=commissionTTc.divide(new BigDecimal(100));
//
//        }else if ("B".equals(sens)) {
//             commissionTTc= new BigDecimal(tp.getCommissionDestinataire().replace(".", ""));
//        }
        commissionTTc= new BigDecimal(tp.getCommissionDonneurOrdre().replace(".", ""));

        if (commissionTTc.compareTo(new BigDecimal(0)) == 0) {
             commissionTTc= new BigDecimal(tp.getCommissionDestinataire().replace(".", ""));
        }

        commissionTTc=commissionTTc.divide(new BigDecimal(100));

        BigDecimal interchange = interchangeTTC.divide(tvaDevided, 2, RoundingMode.HALF_UP);
        BigDecimal interchangeTva = interchangeTTC.subtract(interchange).setScale(2, RoundingMode.HALF_UP);
        interchange = interchangeTTC.subtract(interchangeTva).setScale(2, RoundingMode.HALF_UP);
        interchangeTva = interchangeTTC.subtract(interchange).setScale(2, RoundingMode.HALF_UP);
        // calculecommission
        BigDecimal commissionHT = commissionTTc.divide(tvaDevided, 2, RoundingMode.HALF_UP);
        BigDecimal commissionTVA = commissionTTc.subtract(commissionHT).setScale(2, RoundingMode.HALF_UP);
        commissionHT = commissionTTc.subtract(commissionTVA).setScale(2, RoundingMode.HALF_UP);
        commissionTVA = commissionTTc.subtract(commissionHT).setScale(2, RoundingMode.HALF_UP);

        if (interchangeD != null && interchangeD.getMontantSettlement().compareTo(interchange)!=0) {
            interchangeD.setAmountCalculated(interchangeD.getMontantSettlement().floatValue());
            interchangeD.setMontantSettlement(interchange);
        }
        if (tvaInterchangeD != null && tvaInterchangeD.getMontantSettlement().compareTo(interchangeTva )!=0) {
            tvaInterchangeD.setAmountCalculated(tvaInterchangeD.getMontantSettlement().floatValue());
            tvaInterchangeD.setMontantSettlement(interchangeTva );
        }
        if (commissionHTD != null && commissionHTD.getMontantSettlement().compareTo(commissionHT )!=0) {
            commissionHTD.setAmountCalculated(commissionHTD.getMontantSettlement().floatValue());
            commissionHTD.setMontantSettlement(commissionHT );
        }
        if (commissionTVAD != null && commissionTVAD.getMontantSettlement().compareTo(commissionTVA )!=0) {
            commissionTVAD.setAmountCalculated(commissionTVAD.getMontantSettlement().floatValue());
            commissionTVAD.setMontantSettlement(commissionTVA );
        }
        logger.info(interchangeD.getNumRefTransaction());
        logger.info(interchangeD.getNumRefTransaction());
    }
    /**
     * Gets the current method name dynamically
     * @return the name of the calling method
     */
    private String getCurrentMethodName() {
        return Thread.currentThread().getStackTrace()[2].getMethodName();
    }
    @Async("taskExecutor")
    public CompletableFuture<String> p2bOnUsMA(AtomicBoolean stopSignal) {        String methodName = getCurrentMethodName();
        // P2B transactions: 070 (Paiement mobile auprès des commerçants), 073 (Paiement de facture par mobile)
        List<String> transactionTypes = Arrays.asList("070", "073");
        String flow = "P2B";
        String role = "ACQUIRER";
        String result = "ERROR";
        try {
            logger.info("Start processing {}-{} with transaction types: {}", flow, role, transactionTypes);

            // Access the prepared data from ExecutorMobileThreads static map
            Map<Integer, List<FileContentM>> originalFileContentMap = ExecutorMobileThreads.fileContentMap;

            if (originalFileContentMap.isEmpty()) {
                logger.warn("No file content data available for {}-{}", flow, role);
                return CompletableFuture.completedFuture("NO_DATA");
            }

            // Create filtered map containing only transactions of specified types
//            Map<Integer, List<FileContentM>> filteredFileContentMap = new HashMap<>();

            for (Map.Entry<Integer, List<FileContentM>> entry : originalFileContentMap.entrySet()) {
                Integer fileHeaderId = entry.getKey();
                List<FileContentM> allFileContents = entry.getValue();

                // Filter by transaction types for this flow
                List<FileContentM> filteredContents = allFileContents.stream()
                        .filter(fc -> transactionTypes.contains(fc.getTypeTransaction())
                                && fc.getRibRipBeneficiaire().substring(3,8).equals(fc.getRibRipBeneficiaire().substring(3,8))
                                && fc.getRibRipBeneficiaire().substring(0,3).equals(bankRepo.findIdentifiant().getIdentifiant().replace("'", ""))
                                && fc.getRibRipDonneur().substring(0,3).equals(bankRepo.findIdentifiant().getIdentifiant().replace("'", ""))
                        )
                        .collect(Collectors.toList());
                for (FileContentM fileContent : filteredContents) {
                    if (fileContent.getTypeTransaction().equals("070")){
                        //settelement
                        DayOperationMP M001 = new DayOperationMP(fileContent, ExecutorMobileThreads.getSettlementByIdentification("M001"),methodName);
                        //commission
                        DayOperationMP M002 = new DayOperationMP(fileContent, ExecutorMobileThreads.getSettlementByIdentification("M002"),methodName);
                        //tva commission
                        DayOperationMP M003 = new DayOperationMP(fileContent, ExecutorMobileThreads.getSettlementByIdentification("M003"),methodName);
                        //interchange
                        DayOperationMP M004 = new DayOperationMP(fileContent, ExecutorMobileThreads.getSettlementByIdentification("M004"),methodName);
                        //tva interchange
                        DayOperationMP M005 = new DayOperationMP(fileContent, ExecutorMobileThreads.getSettlementByIdentification("M005"),methodName);
                        Map<String,DayOperationMP> list=new HashMap<>();
                        list.put("sett", M001);
                        list.put("commission", M002);
                        list.put("commissionTVA", M003);
                        list.put("Interchange", M004);
                        list.put("tvaInterchange", M005);
//                    if ("donneur".equals(sens)) {

//                    }else if ("benifeciaire".equals(sens)) {
                        correctiondayOperation(fileContent,list,"B",commissionByTypeMap.get("070"));
                        dayOperations.addAll(list.values());
                        if (stopSignal.get() || Thread.currentThread().isInterrupted()) {
                            logger.warn("Stop requested for {}-{} at transaction {}", flow, role, fileContent.getId());
                            throw new InterruptedException("Stop signal received");
                        }
                    }
                    else if (fileContent.getTypeTransaction().equals("073")){
                        //settelement
                        DayOperationMP M006 = new DayOperationMP(fileContent, ExecutorMobileThreads.getSettlementByIdentification("M006"),methodName);
                        //commission
                        DayOperationMP M007 = new DayOperationMP(fileContent, ExecutorMobileThreads.getSettlementByIdentification("M007"),methodName);
                        //tva commission
                        DayOperationMP M008 = new DayOperationMP(fileContent, ExecutorMobileThreads.getSettlementByIdentification("M008"),methodName);
                        //interchange
                        DayOperationMP M009 = new DayOperationMP(fileContent, ExecutorMobileThreads.getSettlementByIdentification("M009"),methodName);
                        //tva interchange
                        DayOperationMP M010 = new DayOperationMP(fileContent, ExecutorMobileThreads.getSettlementByIdentification("M010"),methodName);
                        Map<String,DayOperationMP> list=new HashMap<>();
                        list.put("sett", M006);
                        list.put("commission", M007);
                        list.put("commissionTVA", M008);
                        list.put("Interchange", M009);
                        list.put("tvaInterchange", M010);
//                    if ("donneur".equals(sens)) {

//                    }else if ("benifeciaire".equals(sens)) {
                        correctiondayOperation(fileContent,list,"B",commissionByTypeMap.get("073"));
                        dayOperations.addAll(list.values());

                        if (stopSignal.get() || Thread.currentThread().isInterrupted()) {
                            logger.warn("Stop requested for {}-{} at transaction {}", flow, role, fileContent.getId());
                            throw new InterruptedException("Stop signal received");
                        }
                    }
                }


            }



            result = "SUCCESS";

        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            logger.warn("Cancelled {}-{} due to interruption", flow, role);
            throw new RuntimeException("CANCELLED: " + flow + "-" + role, ie);
        } catch (Exception e) {
            logger.error("Error in {}-{}", flow, role, e);
            throw new RuntimeException("ERROR: " + flow + "-" + role, e);
        }
        return CompletableFuture.completedFuture(result);
    }
    @Async("taskExecutor")
    public CompletableFuture<String> b2pOnUsMA(AtomicBoolean stopSignal) {
        String methodName = getCurrentMethodName();
        // B2B transactions: 070 (Paiement mobile auprès des commerçants), 073 (Paiement de facture par mobile), 071 (Remboursement)
        List<String> transactionTypes = Arrays.asList("071");
        String flow = "B2P";
        String role = "ONUS";
        String result = "ERROR";
        try {
            logger.info("Start processing {}-{} with transaction types: {}", flow, role, transactionTypes);

            // Access the prepared data from ExecutorMobileThreads static map
            Map<Integer, List<FileContentM>> originalFileContentMap = ExecutorMobileThreads.fileContentMap;

            if (originalFileContentMap.isEmpty()) {
                logger.warn("No file content data available for {}-{}", flow, role);
                return CompletableFuture.completedFuture("NO_DATA");
            }

            // Create filtered map containing only transactions of specified types
            Map<Integer, List<FileContentM>> filteredFileContentMap = new HashMap<>();

            for (Map.Entry<Integer, List<FileContentM>> entry : originalFileContentMap.entrySet()) {
                Integer fileHeaderId = entry.getKey();
                List<FileContentM> allFileContents = entry.getValue();

                // Filter by transaction types for this flow
                List<FileContentM> filteredContents = allFileContents.stream()
                        .filter(fc -> transactionTypes.contains(fc.getTypeTransaction())
                                && fc.getRibRipBeneficiaire().substring(3,8).equals(fc.getRibRipBeneficiaire().substring(3,8))

                                && fc.getRibRipDonneur().substring(0,3).equals(bankRepo.findIdentifiant().getIdentifiant().replace("'", ""))
                                && fc.getRibRipBeneficiaire().substring(0,3).equals(bankRepo.findIdentifiant().getIdentifiant().replace("'", ""))
                        )
                        .collect(Collectors.toList());

                for (FileContentM fileContent : filteredContents) {
                    if (fileContent.getTypeTransaction().equals("071")){
                        //settelement
                        DayOperationMP M135= new DayOperationMP(fileContent, ExecutorMobileThreads.getSettlementByIdentification("M135"),methodName);
                        //commission
                        DayOperationMP M136 = new DayOperationMP(fileContent, ExecutorMobileThreads.getSettlementByIdentification("M136"),methodName);
                        //tva commission
                        DayOperationMP M137 = new DayOperationMP(fileContent, ExecutorMobileThreads.getSettlementByIdentification("M137"),methodName);
                        //interchange
                        DayOperationMP M138 = new DayOperationMP(fileContent, ExecutorMobileThreads.getSettlementByIdentification("M138"),methodName);
                        //tva interchange
                        DayOperationMP M139 = new DayOperationMP(fileContent, ExecutorMobileThreads.getSettlementByIdentification("M139"),methodName);
                        Map<String,DayOperationMP> list=new HashMap<>();
                        list.put("sett", M135);
                        list.put("commission", M136);
                        list.put("commissionTVA", M137);
                        list.put("Interchange", M138);
                        list.put("tvaInterchange", M139);
//                    if ("donneur".equals(sens)) {

//                    }else if ("benifeciaire".equals(sens)) {
                        correctiondayOperation(fileContent,list,"B", commissionByTypeMap.get("071"));
                        if (stopSignal.get() || Thread.currentThread().isInterrupted()) {
                            logger.warn("Stop requested for {}-{} at transaction {}", flow, role, fileContent.getId());
                            throw new InterruptedException("Stop signal received");
                        }
                        dayOperations.addAll(list.values());

                    }
                }

            }

            logger.info("{}-{}: Created filtered map with {} FileHeaders containing {} total transactions",
                    flow, role, filteredFileContentMap.size(),
                    filteredFileContentMap.values().stream().mapToInt(List::size).sum());

            int totalProcessed = 0;

            // Process each FileHeaderM and its filtered FileContentM list

            logger.info("Completed {}-{}: processed {} transactions from {} FileHeaders",
                    flow, role, totalProcessed, filteredFileContentMap.size());
            result = "SUCCESS";

        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            logger.warn("Cancelled {}-{} due to interruption", flow, role);
            throw new RuntimeException("CANCELLED: " + flow + "-" + role, ie);
        } catch (Exception e) {
            logger.error("Error in {}-{}", flow, role, e);
            throw new RuntimeException("ERROR: " + flow + "-" + role, e);
        }
        return CompletableFuture.completedFuture(result);
    }
    @Async("taskExecutor")
    public CompletableFuture<String> b2bOnUsMA(AtomicBoolean stopSignal) {
        String methodName = getCurrentMethodName();
        // P2B transactions: 070 (Paiement mobile auprès des commerçants), 073 (Paiement de facture par mobile)
        List<String> transactionTypes = Arrays.asList("070", "073");
        String flow = "P2B";
        String role = "EMITTER";
        String result = "ERROR";
        try {
            logger.info("Start processing {}-{} with transaction types: {}", flow, role, transactionTypes);

            // Access the prepared data from ExecutorMobileThreads static map
            Map<Integer, List<FileContentM>> originalFileContentMap = ExecutorMobileThreads.fileContentMap;

            if (originalFileContentMap.isEmpty()) {
                logger.warn("No file content data available for {}-{}", flow, role);
                return CompletableFuture.completedFuture("NO_DATA");
            }

            // Create filtered map containing only transactions of specified types
            Map<Integer, List<FileContentM>> filteredFileContentMap = new HashMap<>();

            for (Map.Entry<Integer, List<FileContentM>> entry : originalFileContentMap.entrySet()) {
                Integer fileHeaderId = entry.getKey();
                List<FileContentM> allFileContents = entry.getValue();

                // Filter by transaction types for this flow
                List<FileContentM> filteredContents = allFileContents.stream()
                        .filter(fc -> transactionTypes.contains(fc.getTypeTransaction())
                                && fc.getRibRipBeneficiaire().substring(3,8).equals(fc.getRibRipBeneficiaire().substring(3,8))

                                && fc.getRibRipDonneur().substring(0,3).equals(bankRepo.findIdentifiant().getIdentifiant().replace("'", ""))
                                && fc.getRibRipBeneficiaire().substring(0,3).equals(bankRepo.findIdentifiant().getIdentifiant().replace("'", ""))
                        )
                        .collect(Collectors.toList());

                for (FileContentM fileContent : filteredContents) {
                    if (fileContent.getTypeTransaction().equals("070")){
                        //settelement
                        DayOperationMP M145 = new DayOperationMP(fileContent, ExecutorMobileThreads.getSettlementByIdentification("M145"),methodName);
                        //commission
                        DayOperationMP M146 = new DayOperationMP(fileContent, ExecutorMobileThreads.getSettlementByIdentification("M146"),methodName);
                        //tva commission
                        DayOperationMP M147 = new DayOperationMP(fileContent, ExecutorMobileThreads.getSettlementByIdentification("M147"),methodName);
                        //interchange
                        DayOperationMP M148 = new DayOperationMP(fileContent, ExecutorMobileThreads.getSettlementByIdentification("M148"),methodName);
                        //tva interchange
                        DayOperationMP M149 = new DayOperationMP(fileContent, ExecutorMobileThreads.getSettlementByIdentification("M149"),methodName);
                        Map<String,DayOperationMP> list=new HashMap<>();
                        list.put("sett", M145);
                        list.put("commission", M146);
                        list.put("commissionTVA", M147);
                        list.put("Interchange", M148);
                        list.put("tvaInterchange", M149);
//                    if ("donneur".equals(sens)) {

//                    }else if ("benifeciaire".equals(sens)) {
                        correctiondayOperation(fileContent,list,"D",commissionByTypeMap.get("070"));
                        dayOperations.addAll(list.values());
                        if (stopSignal.get() || Thread.currentThread().isInterrupted()) {
                            logger.warn("Stop requested for {}-{} at transaction {}", flow, role, fileContent.getId());
                            throw new InterruptedException("Stop signal received");
                        }
                    }
                    else if (fileContent.getTypeTransaction().equals("073")){
                        //settelement
                        DayOperationMP M140 = new DayOperationMP(fileContent, ExecutorMobileThreads.getSettlementByIdentification("M140"),methodName);
                        //commission
                        DayOperationMP M141 = new DayOperationMP(fileContent, ExecutorMobileThreads.getSettlementByIdentification("M141"),methodName);
                        //tva commission
                        DayOperationMP M142 = new DayOperationMP(fileContent, ExecutorMobileThreads.getSettlementByIdentification("M142"),methodName);
                        //interchange
                        DayOperationMP M143 = new DayOperationMP(fileContent, ExecutorMobileThreads.getSettlementByIdentification("M143"),methodName);
                        //tva interchange
                        DayOperationMP M144 = new DayOperationMP(fileContent, ExecutorMobileThreads.getSettlementByIdentification("M144"),methodName);
                        Map<String,DayOperationMP> list=new HashMap<>();
                        list.put("sett", M140);
                        list.put("commission", M141);
                        list.put("commissionTVA", M142);
                        list.put("Interchange", M143);
                        list.put("tvaInterchange", M144);
//                    if ("donneur".equals(sens)) {

//                    }else if ("benifeciaire".equals(sens)) {
                        correctiondayOperation(fileContent,list,"D",commissionByTypeMap.get("073"));
                        dayOperations.addAll(list.values());

                        if (stopSignal.get() || Thread.currentThread().isInterrupted()) {
                            logger.warn("Stop requested for {}-{} at transaction {}", flow, role, fileContent.getId());
                            throw new InterruptedException("Stop signal received");
                        }
                    }
                }

            }

            result = "SUCCESS";

        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            logger.warn("Cancelled {}-{} due to interruption", flow, role);
            throw new RuntimeException("CANCELLED: " + flow + "-" + role, ie);
        } catch (Exception e) {
            logger.error("Error in {}-{}", flow, role, e);
            throw new RuntimeException("ERROR: " + flow + "-" + role, e);
        }
        return CompletableFuture.completedFuture(result);
    }
    @Async("taskExecutor")
    public CompletableFuture<String> p2pOnUsMA(AtomicBoolean stopSignal) {
        String methodName = getCurrentMethodName();

        // P2P transactions: 072 (Transfert d'argent entre particuliers)
        List<String> transactionTypes = Arrays.asList("072");
        String flow = "P2P";
        String role = "ONUS";
        String result = "ERROR";
        try {
            logger.info("Start processing {}-{} with transaction types: {}", flow, role, transactionTypes);

            // Access the prepared data from ExecutorMobileThreads static map
            Map<Integer, List<FileContentM>> originalFileContentMap = ExecutorMobileThreads.fileContentMap;

            if (originalFileContentMap.isEmpty()) {
                logger.warn("No file content data available for {}-{}", flow, role);
                return CompletableFuture.completedFuture("NO_DATA");
            }

            // Create filtered map containing only transactions of specified types
            Map<Integer, List<FileContentM>> filteredFileContentMap = new HashMap<>();

            for (Map.Entry<Integer, List<FileContentM>> entry : originalFileContentMap.entrySet()) {
                Integer fileHeaderId = entry.getKey();
                List<FileContentM> allFileContents = entry.getValue();

                // Filter by transaction types for this flow
                List<FileContentM> filteredContents = allFileContents.stream()
                        .filter(fc -> transactionTypes.contains(fc.getTypeTransaction())
                                && fc.getRibRipBeneficiaire().substring(3,8).equals(fc.getRibRipBeneficiaire().substring(3,8))

                                && fc.getRibRipDonneur().substring(0,3).equals(bankRepo.findIdentifiant().getIdentifiant().replace("'", ""))
                                && fc.getRibRipBeneficiaire().substring(0,3).equals(bankRepo.findIdentifiant().getIdentifiant().replace("'", ""))

                        )
                        .collect(Collectors.toList());
                for (FileContentM fileContent : filteredContents) {
                    if (fileContent.getTypeTransaction().equals("072")){
                        //settelement
                        DayOperationMP M130 = new DayOperationMP(fileContent, ExecutorMobileThreads.getSettlementByIdentification("M130"),methodName);
                        //commission
                        DayOperationMP M131 = new DayOperationMP(fileContent, ExecutorMobileThreads.getSettlementByIdentification("M131"),methodName);
                        //tva commission
                        DayOperationMP M132 = new DayOperationMP(fileContent, ExecutorMobileThreads.getSettlementByIdentification("M132"),methodName);
                        //interchange
                        DayOperationMP M133 = new DayOperationMP(fileContent, ExecutorMobileThreads.getSettlementByIdentification("M133"),methodName);
                        //tva interchange
                        DayOperationMP M134 = new DayOperationMP(fileContent, ExecutorMobileThreads.getSettlementByIdentification("M134"),methodName);
                        Map<String,DayOperationMP> list=new HashMap<>();
                        list.put("sett", M130);
                        list.put("commission", M131);
                        list.put("commissionTVA", M132);
                        list.put("Interchange", M133);
                        list.put("tvaInterchange", M134);
//                    if ("donneur".equals(sens)) {

//                    }else if ("benifeciaire".equals(sens)) {
                        correctiondayOperation(fileContent,list,"B",commissionByTypeMap.get("072"));
                        dayOperations.addAll(list.values());
                        if (stopSignal.get() || Thread.currentThread().isInterrupted()) {
                            logger.warn("Stop requested for {}-{} at transaction {}", flow, role, fileContent.getId());
                            throw new InterruptedException("Stop signal received");
                        }
                    }
                }

            }

            logger.info("{}-{}: Created filtered map with {} FileHeaders containing {} total transactions",
                    flow, role, filteredFileContentMap.size(),
                    filteredFileContentMap.values().stream().mapToInt(List::size).sum());

            int totalProcessed = 0;

            // Process each FileHeaderM and its filtered FileContentM list

            logger.info("Completed {}-{}: processed {} transactions from {} FileHeaders",
                    flow, role, totalProcessed, filteredFileContentMap.size());
            result = "SUCCESS";

        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            logger.warn("Cancelled {}-{} due to interruption", flow, role);
            throw new RuntimeException("CANCELLED: " + flow + "-" + role, ie);
        } catch (Exception e) {
            logger.error("Error in {}-{}", flow, role, e);
            throw new RuntimeException("ERROR: " + flow + "-" + role, e);
        }
        return CompletableFuture.completedFuture(result);
    }
    @Async("taskExecutor")
    public CompletableFuture<String> p2bOnUsAA(AtomicBoolean stopSignal) {        String methodName = getCurrentMethodName();
        // P2B transactions: 070 (Paiement mobile auprès des commerçants), 073 (Paiement de facture par mobile)
        List<String> transactionTypes = Arrays.asList("070", "073");
        String flow = "P2B";
        String role = "ACQUIRER";
        String result = "ERROR";
        try {
            logger.info("Start processing {}-{} with transaction types: {}", flow, role, transactionTypes);

            // Access the prepared data from ExecutorMobileThreads static map
            Map<Integer, List<FileContentM>> originalFileContentMap = ExecutorMobileThreads.fileContentMap;

            if (originalFileContentMap.isEmpty()) {
                logger.warn("No file content data available for {}-{}", flow, role);
                return CompletableFuture.completedFuture("NO_DATA");
            }

            // Create filtered map containing only transactions of specified types
//            Map<Integer, List<FileContentM>> filteredFileContentMap = new HashMap<>();

            for (Map.Entry<Integer, List<FileContentM>> entry : originalFileContentMap.entrySet()) {
                Integer fileHeaderId = entry.getKey();
                List<FileContentM> allFileContents = entry.getValue();

                // Filter by transaction types for this flow
                List<FileContentM> filteredContents = allFileContents.stream()
                        .filter(fc -> transactionTypes.contains(fc.getTypeTransaction())
                                && !fc.getRibRipBeneficiaire().substring(3,8).equals(fc.getRibRipBeneficiaire().substring(3,8))

                                && fc.getRibRipBeneficiaire().substring(0,3).equals(bankRepo.findIdentifiant().getIdentifiant().replace("'", ""))
                                && fc.getRibRipDonneur().substring(0,3).equals(bankRepo.findIdentifiant().getIdentifiant().replace("'", ""))
                        )
                        .collect(Collectors.toList());
                for (FileContentM fileContent : filteredContents) {
                    if (fileContent.getTypeTransaction().equals("070")){
                        //settelement
                        DayOperationMP M170 = new DayOperationMP(fileContent, ExecutorMobileThreads.getSettlementByIdentification("M170"),methodName);
                        //commission
                        DayOperationMP M171 = new DayOperationMP(fileContent, ExecutorMobileThreads.getSettlementByIdentification("M171"),methodName);
                        //tva commission
                        DayOperationMP M172 = new DayOperationMP(fileContent, ExecutorMobileThreads.getSettlementByIdentification("M172"),methodName);
                        //interchange
                        DayOperationMP M173 = new DayOperationMP(fileContent, ExecutorMobileThreads.getSettlementByIdentification("M173"),methodName);
                        //tva interchange
                        DayOperationMP M174 = new DayOperationMP(fileContent, ExecutorMobileThreads.getSettlementByIdentification("M174"),methodName);
                        Map<String,DayOperationMP> list=new HashMap<>();
                        list.put("sett", M170);
                        list.put("commission", M171);
                        list.put("commissionTVA", M172);
                        list.put("Interchange", M173);
                        list.put("tvaInterchange", M174);
//                    if ("donneur".equals(sens)) {

//                    }else if ("benifeciaire".equals(sens)) {
                        correctiondayOperation(fileContent,list,"B",commissionByTypeMap.get("070"));
                        dayOperations.addAll(list.values());
                        if (stopSignal.get() || Thread.currentThread().isInterrupted()) {
                            logger.warn("Stop requested for {}-{} at transaction {}", flow, role, fileContent.getId());
                            throw new InterruptedException("Stop signal received");
                        }
                    }
                    else if (fileContent.getTypeTransaction().equals("073")){
                        //settelement
                        DayOperationMP M175 = new DayOperationMP(fileContent, ExecutorMobileThreads.getSettlementByIdentification("M175"),methodName);
                        //commission
                        DayOperationMP M176 = new DayOperationMP(fileContent, ExecutorMobileThreads.getSettlementByIdentification("M176"),methodName);
                        //tva commission
                        DayOperationMP M177 = new DayOperationMP(fileContent, ExecutorMobileThreads.getSettlementByIdentification("M177"),methodName);
                        //interchange
                        DayOperationMP M178 = new DayOperationMP(fileContent, ExecutorMobileThreads.getSettlementByIdentification("M178"),methodName);
                        //tva interchange
                        DayOperationMP M179 = new DayOperationMP(fileContent, ExecutorMobileThreads.getSettlementByIdentification("M179"),methodName);
                        Map<String,DayOperationMP> list=new HashMap<>();
                        list.put("sett", M175);
                        list.put("commission", M176);
                        list.put("commissionTVA", M177);
                        list.put("Interchange", M178);
                        list.put("tvaInterchange", M179);
//                    if ("donneur".equals(sens)) {

//                    }else if ("benifeciaire".equals(sens)) {
                        correctiondayOperation(fileContent,list,"B",commissionByTypeMap.get("073"));
                        dayOperations.addAll(list.values());

                        if (stopSignal.get() || Thread.currentThread().isInterrupted()) {
                            logger.warn("Stop requested for {}-{} at transaction {}", flow, role, fileContent.getId());
                            throw new InterruptedException("Stop signal received");
                        }
                    }
                }


            }



            result = "SUCCESS";

        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            logger.warn("Cancelled {}-{} due to interruption", flow, role);
            throw new RuntimeException("CANCELLED: " + flow + "-" + role, ie);
        } catch (Exception e) {
            logger.error("Error in {}-{}", flow, role, e);
            throw new RuntimeException("ERROR: " + flow + "-" + role, e);
        }
        return CompletableFuture.completedFuture(result);
    }
    @Async("taskExecutor")
    public CompletableFuture<String> b2pOnUsAA(AtomicBoolean stopSignal) {
        String methodName = getCurrentMethodName();
        // B2B transactions: 070 (Paiement mobile auprès des commerçants), 073 (Paiement de facture par mobile), 071 (Remboursement)
        List<String> transactionTypes = Arrays.asList("071");
        String flow = "B2P";
        String role = "ONUS";
        String result = "ERROR";
        try {
            logger.info("Start processing {}-{} with transaction types: {}", flow, role, transactionTypes);

            // Access the prepared data from ExecutorMobileThreads static map
            Map<Integer, List<FileContentM>> originalFileContentMap = ExecutorMobileThreads.fileContentMap;

            if (originalFileContentMap.isEmpty()) {
                logger.warn("No file content data available for {}-{}", flow, role);
                return CompletableFuture.completedFuture("NO_DATA");
            }

            // Create filtered map containing only transactions of specified types
            Map<Integer, List<FileContentM>> filteredFileContentMap = new HashMap<>();

            for (Map.Entry<Integer, List<FileContentM>> entry : originalFileContentMap.entrySet()) {
                Integer fileHeaderId = entry.getKey();
                List<FileContentM> allFileContents = entry.getValue();

                // Filter by transaction types for this flow
                List<FileContentM> filteredContents = allFileContents.stream()
                        .filter(fc -> transactionTypes.contains(fc.getTypeTransaction())
                                && !fc.getRibRipBeneficiaire().substring(3,8).equals(fc.getRibRipBeneficiaire().substring(3,8))

                                && fc.getRibRipDonneur().substring(0,3).equals(bankRepo.findIdentifiant().getIdentifiant().replace("'", ""))
                                && fc.getRibRipBeneficiaire().substring(0,3).equals(bankRepo.findIdentifiant().getIdentifiant().replace("'", ""))
                        )
                        .collect(Collectors.toList());

                for (FileContentM fileContent : filteredContents) {
                    if (fileContent.getTypeTransaction().equals("071")){
                        //settelement
                        DayOperationMP M155= new DayOperationMP(fileContent, ExecutorMobileThreads.getSettlementByIdentification("M155"),methodName);
                        //commission
                        DayOperationMP M156 = new DayOperationMP(fileContent, ExecutorMobileThreads.getSettlementByIdentification("M156"),methodName);
                        //tva commission
                        DayOperationMP M157 = new DayOperationMP(fileContent, ExecutorMobileThreads.getSettlementByIdentification("M157"),methodName);
                        //interchange
                        DayOperationMP M158 = new DayOperationMP(fileContent, ExecutorMobileThreads.getSettlementByIdentification("M158"),methodName);
                        //tva interchange
                        DayOperationMP M159 = new DayOperationMP(fileContent, ExecutorMobileThreads.getSettlementByIdentification("M159"),methodName);
                        Map<String,DayOperationMP> list=new HashMap<>();
                        list.put("sett", M155);
                        list.put("commission", M156);
                        list.put("commissionTVA", M157);
                        list.put("Interchange", M158);
                        list.put("tvaInterchange", M159);
//                    if ("donneur".equals(sens)) {

//                    }else if ("benifeciaire".equals(sens)) {
                        correctiondayOperation(fileContent,list,"B", commissionByTypeMap.get("071"));
                        if (stopSignal.get() || Thread.currentThread().isInterrupted()) {
                            logger.warn("Stop requested for {}-{} at transaction {}", flow, role, fileContent.getId());
                            throw new InterruptedException("Stop signal received");
                        }
                        dayOperations.addAll(list.values());

                    }
                }

            }

            logger.info("{}-{}: Created filtered map with {} FileHeaders containing {} total transactions",
                    flow, role, filteredFileContentMap.size(),
                    filteredFileContentMap.values().stream().mapToInt(List::size).sum());

            int totalProcessed = 0;

            // Process each FileHeaderM and its filtered FileContentM list

            logger.info("Completed {}-{}: processed {} transactions from {} FileHeaders",
                    flow, role, totalProcessed, filteredFileContentMap.size());
            result = "SUCCESS";

        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            logger.warn("Cancelled {}-{} due to interruption", flow, role);
            throw new RuntimeException("CANCELLED: " + flow + "-" + role, ie);
        } catch (Exception e) {
            logger.error("Error in {}-{}", flow, role, e);
            throw new RuntimeException("ERROR: " + flow + "-" + role, e);
        }
        return CompletableFuture.completedFuture(result);
    }
    @Async("taskExecutor")
    public CompletableFuture<String> b2bOnUsAA(AtomicBoolean stopSignal) {
        String methodName = getCurrentMethodName();
        // P2B transactions: 070 (Paiement mobile auprès des commerçants), 073 (Paiement de facture par mobile)
        List<String> transactionTypes = Arrays.asList("070", "073");
        String flow = "P2B";
        String role = "ONUS";
        String result = "ERROR";
        try {
            logger.info("Start processing {}-{} with transaction types: {}", flow, role, transactionTypes);

            // Access the prepared data from ExecutorMobileThreads static map
            Map<Integer, List<FileContentM>> originalFileContentMap = ExecutorMobileThreads.fileContentMap;

            if (originalFileContentMap.isEmpty()) {
                logger.warn("No file content data available for {}-{}", flow, role);
                return CompletableFuture.completedFuture("NO_DATA");
            }

            // Create filtered map containing only transactions of specified types
            Map<Integer, List<FileContentM>> filteredFileContentMap = new HashMap<>();

            for (Map.Entry<Integer, List<FileContentM>> entry : originalFileContentMap.entrySet()) {
                Integer fileHeaderId = entry.getKey();
                List<FileContentM> allFileContents = entry.getValue();

                // Filter by transaction types for this flow
                List<FileContentM> filteredContents = allFileContents.stream()
                        .filter(fc -> transactionTypes.contains(fc.getTypeTransaction())
                                && !fc.getRibRipBeneficiaire().substring(3,8).equals(fc.getRibRipBeneficiaire().substring(3,8))

                                && fc.getRibRipDonneur().substring(0,3).equals(bankRepo.findIdentifiant().getIdentifiant().replace("'", ""))
                                && fc.getRibRipBeneficiaire().substring(0,3).equals(bankRepo.findIdentifiant().getIdentifiant().replace("'", ""))
                        )
                        .collect(Collectors.toList());

                for (FileContentM fileContent : filteredContents) {
                    if (fileContent.getTypeTransaction().equals("070")){
                        //settelement
                        DayOperationMP M165 = new DayOperationMP(fileContent, ExecutorMobileThreads.getSettlementByIdentification("M165"),methodName);
                        //commission
                        DayOperationMP M166 = new DayOperationMP(fileContent, ExecutorMobileThreads.getSettlementByIdentification("M166"),methodName);
                        //tva commission
                        DayOperationMP M167 = new DayOperationMP(fileContent, ExecutorMobileThreads.getSettlementByIdentification("M167"),methodName);
                        //interchange
                        DayOperationMP M168 = new DayOperationMP(fileContent, ExecutorMobileThreads.getSettlementByIdentification("M168"),methodName);
                        //tva interchange
                        DayOperationMP M169 = new DayOperationMP(fileContent, ExecutorMobileThreads.getSettlementByIdentification("M169"),methodName);
                        Map<String,DayOperationMP> list=new HashMap<>();
                        list.put("sett", M165);
                        list.put("commission", M166);
                        list.put("commissionTVA", M167);
                        list.put("Interchange", M168);
                        list.put("tvaInterchange", M169);
//                    if ("donneur".equals(sens)) {

//                    }else if ("benifeciaire".equals(sens)) {
                        correctiondayOperation(fileContent,list,"D",commissionByTypeMap.get("070"));
                        dayOperations.addAll(list.values());
                        if (stopSignal.get() || Thread.currentThread().isInterrupted()) {
                            logger.warn("Stop requested for {}-{} at transaction {}", flow, role, fileContent.getId());
                            throw new InterruptedException("Stop signal received");
                        }
                    }
                    else if (fileContent.getTypeTransaction().equals("073")){
                        //settelement
                        DayOperationMP M160 = new DayOperationMP(fileContent, ExecutorMobileThreads.getSettlementByIdentification("M160"),methodName);
                        //commission
                        DayOperationMP M161 = new DayOperationMP(fileContent, ExecutorMobileThreads.getSettlementByIdentification("M161"),methodName);
                        //tva commission
                        DayOperationMP M162 = new DayOperationMP(fileContent, ExecutorMobileThreads.getSettlementByIdentification("M162"),methodName);
                        //interchange
                        DayOperationMP M163 = new DayOperationMP(fileContent, ExecutorMobileThreads.getSettlementByIdentification("M163"),methodName);
                        //tva interchange
                        DayOperationMP M164 = new DayOperationMP(fileContent, ExecutorMobileThreads.getSettlementByIdentification("M164"),methodName);
                        Map<String,DayOperationMP> list=new HashMap<>();
                        list.put("sett", M160);
                        list.put("commission", M161);
                        list.put("commissionTVA", M162);
                        list.put("Interchange", M163);
                        list.put("tvaInterchange", M164);
//                    if ("donneur".equals(sens)) {

//                    }else if ("benifeciaire".equals(sens)) {
                        correctiondayOperation(fileContent,list,"D",commissionByTypeMap.get("073"));
                        dayOperations.addAll(list.values());

                        if (stopSignal.get() || Thread.currentThread().isInterrupted()) {
                            logger.warn("Stop requested for {}-{} at transaction {}", flow, role, fileContent.getId());
                            throw new InterruptedException("Stop signal received");
                        }
                    }
                }

            }

            result = "SUCCESS";

        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            logger.warn("Cancelled {}-{} due to interruption", flow, role);
            throw new RuntimeException("CANCELLED: " + flow + "-" + role, ie);
        } catch (Exception e) {
            logger.error("Error in {}-{}", flow, role, e);
            throw new RuntimeException("ERROR: " + flow + "-" + role, e);
        }
        return CompletableFuture.completedFuture(result);
    }
    @Async("taskExecutor")
    public CompletableFuture<String> p2pOnUsAA(AtomicBoolean stopSignal) {
        String methodName = getCurrentMethodName();

        // P2P transactions: 072 (Transfert d'argent entre particuliers)
        List<String> transactionTypes = Arrays.asList("072");
        String flow = "P2P";
        String role = "ONUS";
        String result = "ERROR";
        try {
            logger.info("Start processing {}-{} with transaction types: {}", flow, role, transactionTypes);

            // Access the prepared data from ExecutorMobileThreads static map
            Map<Integer, List<FileContentM>> originalFileContentMap = ExecutorMobileThreads.fileContentMap;

            if (originalFileContentMap.isEmpty()) {
                logger.warn("No file content data available for {}-{}", flow, role);
                return CompletableFuture.completedFuture("NO_DATA");
            }

            // Create filtered map containing only transactions of specified types
            Map<Integer, List<FileContentM>> filteredFileContentMap = new HashMap<>();

            for (Map.Entry<Integer, List<FileContentM>> entry : originalFileContentMap.entrySet()) {
                Integer fileHeaderId = entry.getKey();
                List<FileContentM> allFileContents = entry.getValue();

                // Filter by transaction types for this flow
                List<FileContentM> filteredContents = allFileContents.stream()
                        .filter(fc -> transactionTypes.contains(fc.getTypeTransaction())
                                && !fc.getRibRipBeneficiaire().substring(3,8).equals(fc.getRibRipBeneficiaire().substring(3,8))

                                && fc.getRibRipDonneur().substring(0,3).equals(bankRepo.findIdentifiant().getIdentifiant().replace("'", ""))
                                && fc.getRibRipBeneficiaire().substring(0,3).equals(bankRepo.findIdentifiant().getIdentifiant().replace("'", ""))

                        )
                        .collect(Collectors.toList());
                for (FileContentM fileContent : filteredContents) {
                    if (fileContent.getTypeTransaction().equals("072")){
                        //settelement
                        DayOperationMP M150 = new DayOperationMP(fileContent, ExecutorMobileThreads.getSettlementByIdentification("M150"),methodName);
                        //commission
                        DayOperationMP M151 = new DayOperationMP(fileContent, ExecutorMobileThreads.getSettlementByIdentification("M151"),methodName);
                        //tva commission
                        DayOperationMP M152 = new DayOperationMP(fileContent, ExecutorMobileThreads.getSettlementByIdentification("M152"),methodName);
                        //interchange
                        DayOperationMP M153 = new DayOperationMP(fileContent, ExecutorMobileThreads.getSettlementByIdentification("M153"),methodName);
                        //tva interchange
                        DayOperationMP M154 = new DayOperationMP(fileContent, ExecutorMobileThreads.getSettlementByIdentification("M154"),methodName);
                        Map<String,DayOperationMP> list=new HashMap<>();
                        list.put("sett", M150);
                        list.put("commission", M151);
                        list.put("commissionTVA", M152);
                        list.put("Interchange", M153);
                        list.put("tvaInterchange", M154);
//                    if ("donneur".equals(sens)) {

//                    }else if ("benifeciaire".equals(sens)) {
                        correctiondayOperation(fileContent,list,"B",commissionByTypeMap.get("072"));
                        dayOperations.addAll(list.values());
                        if (stopSignal.get() || Thread.currentThread().isInterrupted()) {
                            logger.warn("Stop requested for {}-{} at transaction {}", flow, role, fileContent.getId());
                            throw new InterruptedException("Stop signal received");
                        }
                    }
                }

            }

            logger.info("{}-{}: Created filtered map with {} FileHeaders containing {} total transactions",
                    flow, role, filteredFileContentMap.size(),
                    filteredFileContentMap.values().stream().mapToInt(List::size).sum());

            int totalProcessed = 0;

            // Process each FileHeaderM and its filtered FileContentM list

            logger.info("Completed {}-{}: processed {} transactions from {} FileHeaders",
                    flow, role, totalProcessed, filteredFileContentMap.size());
            result = "SUCCESS";

        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            logger.warn("Cancelled {}-{} due to interruption", flow, role);
            throw new RuntimeException("CANCELLED: " + flow + "-" + role, ie);
        } catch (Exception e) {
            logger.error("Error in {}-{}", flow, role, e);
            throw new RuntimeException("ERROR: " + flow + "-" + role, e);
        }
        return CompletableFuture.completedFuture(result);
    }

    /**
     * Async method to process CRA matching
     * Calls the processCRAMatching method from ExecutorMobileThreads
     */
    @Async("taskExecutor")
    public CompletableFuture<String> processCRAMatchingAsync(ExecutorMobileThreads executorMobileThreads) {
        try {
            logger.info("Starting async CRA matching process in thread: {}", Thread.currentThread().getName());
            executorMobileThreads.processCRAMatching();
            logger.info("Async CRA matching process completed successfully");
            return CompletableFuture.completedFuture("SUCCESS");
        } catch (Exception e) {
            logger.error("Error in async CRA matching process", e);
            return CompletableFuture.completedFuture("ERROR: " + e.getMessage());
        }
    }

    /**
     * Async method to process CRO matching
     * Calls the processCROMatching method from ExecutorMobileThreads
     */
    @Async("taskExecutor")
    public CompletableFuture<String> processCROMatchingAsync(ExecutorMobileThreads executorMobileThreads) {
        try {
            logger.info("Starting async CRO matching process in thread: {}", Thread.currentThread().getName());
            executorMobileThreads.processCROMatching();
            logger.info("Async CRO matching process completed successfully");
            return CompletableFuture.completedFuture("SUCCESS");
        } catch (Exception e) {
            logger.error("Error in async CRO matching process", e);
            return CompletableFuture.completedFuture("ERROR: " + e.getMessage());
        }
    }
}
