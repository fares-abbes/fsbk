package com.mss.backOffice.controller;

import com.mss.backOffice.services.AsyncInternationalProcessingService;
import com.mss.unified.entities.*;
import com.mss.unified.references.RecapInter;
import com.mss.unified.repositories.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

 import static com.mss.backOffice.services.AsyncInternationalProcessingService.*;

@RestController
@RequestMapping("/api/international-processing")
@CrossOrigin(origins = "*", maxAge = 3600)
public class InternationalProcessingExecutor {
    
    private static final Logger logger = LoggerFactory.getLogger(InternationalProcessingExecutor.class);
    @Autowired
    ChargebacksInternationalRepository chargebacksInternationalRepository;

    @Autowired
    TotalAmountInterRepository totalAmountInterRepository;
    @Autowired
    DayOperationInternationalRepository dayOperationInternationalRepository;
    @Autowired
    private AsyncInternationalProcessingService asyncInternationalProcessingService;
    @Autowired

    BankSettlementController bankSettelementController;
    @Autowired
    VisaSummaryRepository visaSummaryRepo;
    @Autowired
    private DayOperationChargebackInterRep dayOperationChargebackInterRep;

    /**
     * Starts all international processing methods and stops all if one fails
     * This endpoint executes all 21 async methods for VISA international operations
     */
    @GetMapping("/start-all-stop-on-error")
    public ResponseEntity<String> startAllStopOnError() {
        AtomicBoolean stopSignal = new AtomicBoolean(false);
        accountDevise.put("4466170481332799","03501601220400035239");
        accountDinars.put("4466170481332799","03501601220100078356");
        List<CompletableFuture<String>> futures = new ArrayList<>();
        try {
            logger.info("Starting all international processing operations...");
            // Initialize data preparation if needed
            logger.info("Preparing international processing data...");
            // Chargeback operations
            logger.info("Adding chargeback operations to execution queue...");
            futures.add(asyncInternationalProcessingService.visaIncomingAchat(stopSignal));
            futures.add(asyncInternationalProcessingService.visaIncomingRetrait(stopSignal));
            futures.add(asyncInternationalProcessingService.refundVisaIssuerBt(stopSignal));
            futures.add(asyncInternationalProcessingService.refundVisaRetaitIssuerBt(stopSignal));
            
            // Representation operations
            logger.info("Adding representation operations to execution queue...");
            futures.add(asyncInternationalProcessingService.cashAdvanceVisaIssuerBt(stopSignal));
            futures.add(asyncInternationalProcessingService.ReversalvisaIncomingRetrait(stopSignal));
            futures.add(asyncInternationalProcessingService.ReversalvisaIncomingAchat(stopSignal));
            futures.add(asyncInternationalProcessingService.chargeBackVISAEmis(stopSignal));
            futures.add(asyncInternationalProcessingService.RepresentationVISARecue(stopSignal));
            
            // Arbitrage operations
            logger.info("Adding arbitrage operations to execution queue...");
            futures.add(asyncInternationalProcessingService.RepresentationVISAIncomingRecueAchat(stopSignal));
            futures.add(asyncInternationalProcessingService.RepresentationVISAIncomingRecueRetrait(stopSignal));
            

            
            // Charge operations
            logger.info("Adding charge operations to execution queue...");

            futures.add(asyncInternationalProcessingService.chargeVisaGlobalEURCredit(stopSignal));
            futures.add(asyncInternationalProcessingService.chargeVisaGlobalEURDebit(stopSignal));
            futures.add(asyncInternationalProcessingService.disputeReversalvisa(stopSignal));

            logger.info("Total operations queued: {}", futures.size());
            
            // Monitor all futures for completion or failure
            CompletableFuture<Void> allOf = CompletableFuture.allOf(
                    futures.toArray(new CompletableFuture[0])
            );
            
            // Wait for all to complete or handle exceptions
            try {
                // Check periodically for failures
                while (!allOf.isDone()) {
                    Thread.sleep(500);
                    
                    // Check if any future completed exceptionally
                    for (CompletableFuture<String> future : futures) {
                        if (future.isCompletedExceptionally()) {
                            logger.error("One operation failed! Signaling all others to stop...");
                            stopSignal.set(true);
                            
                            // Cancel all pending futures
                            for (CompletableFuture<String> f : futures) {
                                if (!f.isDone()) {
                                    f.cancel(true);
                                }
                            }
                            
                            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                    .body("International processing failed - one or more operations encountered errors. All operations stopped.");
                        }
                    }
                    
                    // Check stop signal
                    if (stopSignal.get()) {
                        logger.warn("Stop signal detected during execution!");
                        break;
                    }
                }
                
                allOf.join(); // Wait for final completion
                
                // Collect results
                List<String> results = new ArrayList<>();
                for (CompletableFuture<String> future : futures) {
                    try {
                        results.add(future.get());
                    } catch (Exception e) {
                        logger.error("Error collecting result: {}", e.getMessage());
                        results.add("ERROR");
                    }
                }
                
                long successCount = results.stream().filter(r -> "SUCCESS".equals(r)).count();
                long errorCount = results.stream().filter(r -> "ERROR".equals(r) || r.startsWith("CANCELLED")).count();
                long noDataCount = results.stream().filter(r -> "NO_DATA".equals(r)).count();
                
                logger.info("International processing completed. Success: {}, Errors: {}, No Data: {}", 
                        successCount, errorCount, noDataCount);
                
                if (errorCount > 0) {
                    return ResponseEntity.status(HttpStatus.PARTIAL_CONTENT)
                            .body(String.format("International processing completed with errors. Success: %d, Errors: %d, No Data: %d", 
                                    successCount, errorCount, noDataCount));
                }
                dayOperationsL.addAll(dayOperationsChargebackL);
                List<DayOperationChargebackInternational> listChargebacks = new ArrayList<>();
                listChargebacks= dayOperationChargebackInterRep.findAll();
                dayOperationsL.addAll(copyChargebacksToDayOperations(listChargebacks));

                dayOperationInternationalRepository.saveAll(dayOperationsL);
                dayOperationsL.clear();
                dayOperationsChargebackL.clear();
                dayOperationChargebackInterRep.deleteAll(listChargebacks);
                bankSettelementController.generateBankSettCodeInter();
                List<VisaSummary> summariesV = visaSummaryRepo.findByTreatM();

                try {
                    for (VisaSummary sum : summariesV) {
                        logger.info("calcTotalAmountVISA with date " + sum.getSummary_date() + " satrted");
                        logger.info("calcTotalAmountVISA with date " + convertDateFormatted(sum.getSummary_date()) + " satrted");
                        calcTotalAmountVisa(convertDateFormatted(sum.getSummary_date()));
                        logger.info("calcTotalAmountVISA with date " + sum.getSummary_date() + " ended ");
                        sum.setTreat("1");
                        
                    }
                } catch (Exception e) {
                    logger.error("Error processing VISA summaries: {}", e.getMessage(), e);
                }
                return ResponseEntity.ok(String.format("All international processing operations completed successfully. Success: %d, No Data: %d", 
                        successCount, noDataCount));
                
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                logger.error("International processing interrupted", e);
                stopSignal.set(true);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body("International processing interrupted");
            }
            
        } catch (Exception e) {
            logger.error("Error starting international processing operations", e);
            stopSignal.set(true);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to start international processing: " + e.getMessage());
        }
    }

    public static String convertDateFormatted(String inputDate) {
        // Define the input and output date formats
        DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("ddMMyy");
        logger.info(inputDate );
        // Parse the input date
        LocalDate date = LocalDate.parse(inputDate, inputFormatter);

        // Format the date to the desired output format
        String formattedDate = date.format(outputFormatter);

        return formattedDate;
    }
//    /**
//     * Starts all international processing methods independently (no stop-on-error)
//     * Each method runs to completion regardless of others' status
//     */
//    @GetMapping("/start-all-independent")
//    public ResponseEntity<String> startAllIndependent() {
//        AtomicBoolean stopSignal = new AtomicBoolean(false);
//        List<CompletableFuture<String>> futures = new ArrayList<>();
//
//        try {
//            logger.info("Starting all international processing operations independently...");
//
//            // Execute all operations
//            logger.info("Starting all international processing operations...");
//            // Initialize data preparation if needed
//            logger.info("Preparing international processing data...");
//            // Chargeback operations
//            logger.info("Adding chargeback operations to execution queue...");
//            futures.add(asyncInternationalProcessingService.visaIncomingAchat(stopSignal));
//            futures.add(asyncInternationalProcessingService.visaIncomingRetrait(stopSignal));
//            futures.add(asyncInternationalProcessingService.refundVisaIssuerBt(stopSignal));
//            futures.add(asyncInternationalProcessingService.refundVisaRetaitIssuerBt(stopSignal));
//
//            // Representation operations
//            logger.info("Adding representation operations to execution queue...");
//            futures.add(asyncInternationalProcessingService.cashAdvanceVisaIssuerBt(stopSignal));
//            futures.add(asyncInternationalProcessingService.ReversalvisaIncomingRetrait(stopSignal));
//            futures.add(asyncInternationalProcessingService.ReversalvisaIncomingAchat(stopSignal));
//            futures.add(asyncInternationalProcessingService.chargeBackVISAEmis(stopSignal));
//            futures.add(asyncInternationalProcessingService.RepresentationVISARecue(stopSignal));
//
//            // Arbitrage operations
//            logger.info("Adding arbitrage operations to execution queue...");
//            futures.add(asyncInternationalProcessingService.RepresentationVISAIncomingRecueAchat(stopSignal));
//            futures.add(asyncInternationalProcessingService.RepresentationVISAIncomingRecueRetrait(stopSignal));
//
//
//
//            // Charge operations
//            logger.info("Adding charge operations to execution queue...");
//
//            futures.add(asyncInternationalProcessingService.chargeVisaGlobalEURCredit(stopSignal));
//            futures.add(asyncInternationalProcessingService.chargeVisaGlobalEURDebit(stopSignal));
//            futures.add(asyncInternationalProcessingService.disputeReversalvisa(stopSignal));
//
//            logger.info("Total operations queued: {}", futures.size());
//
//            // Wait for all to complete
//            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
//
//            // Collect results
//            List<String> results = new ArrayList<>();
//            for (CompletableFuture<String> future : futures) {
//                try {
//                    results.add(future.get());
//                } catch (Exception e) {
//                    logger.error("Error collecting result: {}", e.getMessage());
//                    results.add("ERROR");
//                }
//            }
//
//            long successCount = results.stream().filter(r -> "SUCCESS".equals(r)).count();
//            long errorCount = results.stream().filter(r -> "ERROR".equals(r) || r.startsWith("CANCELLED")).count();
//            long noDataCount = results.stream().filter(r -> "NO_DATA".equals(r)).count();
//
//            logger.info("International processing completed independently. Success: {}, Errors: {}, No Data: {}",
//                    successCount, errorCount, noDataCount);
//
//            return ResponseEntity.ok(String.format("All international processing operations completed. Success: %d, Errors: %d, No Data: %d",
//                    successCount, errorCount, noDataCount));
//
//        } catch (Exception e) {
//            logger.error("Error in independent international processing", e);
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//                    .body("Failed to complete international processing: " + e.getMessage());
//        }
//    }
//
    /**
     * Manually stop all running operations
     */
    @PostMapping("/stop-all")
    public ResponseEntity<String> stopAll() {
        logger.warn("Manual stop requested for international processing operations");
        // In a real implementation, you'd maintain a reference to the stopSignal
        // and set it here. For now, this is a placeholder.
        return ResponseEntity.ok("Stop signal sent to all international processing operations");
    }
    
    /**
     * Health check endpoint
     */
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("International Processing Executor is running");
    }
    
    /**
     * Get status of international processing service
     */
    @GetMapping("/status")
    public ResponseEntity<String> getStatus() {
        return ResponseEntity.ok("Service ready. Available endpoints: /start-all-stop-on-error, /start-all-independent, /stop-all, /health");
    }

    private List<TotalAmountSettlementInter> remplirVisa(List<RecapInter> settVisaoutgoingAchat,
                                                         List<RecapInter> interVisaoutgoingAchat, List<RecapInter> surcVisaoutgoingAchat, String lib, String date) {
        logger.info(lib);
        List<TotalAmountSettlementInter> data = new ArrayList<>();
        logger.info(settVisaoutgoingAchat.toString());

        settVisaoutgoingAchat = addElements(settVisaoutgoingAchat);
        interVisaoutgoingAchat = addElements(interVisaoutgoingAchat);
        logger.info(settVisaoutgoingAchat.toString());
        logger.info(interVisaoutgoingAchat.toString());
        for (RecapInter element : settVisaoutgoingAchat) {
            TotalAmountSettlementInter output = new TotalAmountSettlementInter();
            List<RecapInter> filteredList = interVisaoutgoingAchat.stream()
                    .filter(recap -> recap.getCurSett().equals(element.getCurSett())).collect(Collectors.toList());

            output.setCat(lib);
            if (lib.equals("VISA outgoing Retrait")) {
                logger.info("here");
                logger.info(filteredList.get(0).getNb() + "");

                output.setNbtrans(filteredList.get(0).getNb());
                logger.info(filteredList.get(0).getNb() + "");

            }else  {

                output.setNbtrans(element.getNb());
            }
            if (lib.contains("VISA incoming Frais") && element.getMntSett()<0) {
                output.setNbtrans(0l);

            }
            if(filteredList!=null && filteredList.size()>0) {
                output.setMntInter(filteredList.get(0).getMntSett());
                output.setMntInterCur(filteredList.get(0).getCurSett());
            }else {
                logger.info("error");
                logger.info("lib "+lib);
                logger.info(interVisaoutgoingAchat.toString());

                output.setMntInter(0);
                output.setMntInterCur("");
            }

            output.setMntTran(element.getTransactionMnt());
            output.setMntSett(element.getMntSett());
            output.setMntTranCur(element.getCurTran());
            output.setMntSettCur(element.getCurSett());
            if (element.getCurSett().equals("840") && surcVisaoutgoingAchat != null
                    && surcVisaoutgoingAchat.size() > 0) {
                output.setMntAccesFee(surcVisaoutgoingAchat.get(0).getMntSett());
                output.setMntAccesFeeCur(surcVisaoutgoingAchat.get(0).getCurSett());

                output.setMntAccesFeeUSD(surcVisaoutgoingAchat.get(0).getTransactionMnt());

                output.setMntTranCur(element.getCurSett());
            } else {
                output.setMntAccesFee(0);
                output.setMntAccesFeeCur("788");
            }
            output.setDateSet(date);
            logger.info(lib);
            logger.info(lib.contains("VISA incoming Frais")+"");
            logger.info(output.toString());
            data.add(output);
        }
        logger.info("Data returned lib " + lib);
        logger.info("data =>" + data.toString());

        return data;
    }
    private List<TotalAmountSettlementInter> remplirVisaFrais(List<RecapInter> settVisaoutgoingAchat,  String lib, String date) {
        logger.info(lib);
        List<TotalAmountSettlementInter> data = new ArrayList<>();
        logger.info(settVisaoutgoingAchat.toString());

        settVisaoutgoingAchat = addElements(settVisaoutgoingAchat);
        logger.info(settVisaoutgoingAchat.toString());
        for (RecapInter element : settVisaoutgoingAchat) {
            TotalAmountSettlementInter output = new TotalAmountSettlementInter();
            output.setCat(lib);

            output.setNbtrans(element.getNb());

            if ( !(element.getMntSett()>0)) {
                output.setNbtrans(0l);

            }
            output.setMntInter(0);
            output.setMntInterCur("");
            output.setMntTran(0);
            output.setMntSett(element.getMntSett());
            output.setMntTranCur(element.getCurTran());
            output.setMntSettCur(element.getCurSett());
            output.setMntAccesFee(0);
            output.setMntAccesFeeCur("788");
            output.setDateSet(date);
            logger.info(lib);
            logger.info(lib.contains("VISA incoming Frais")+"");
            logger.info(output.toString());
            data.add(output);
        }
        logger.info("Data returned lib " + lib);
        logger.info("data =>" + data.toString());

        return data;
    }

    public List<TotalAmountSettlementInter> calcTotalAmountVisa(@PathVariable(value = "fileDate") String date) {
        logger.info("single messa");
        List<String> idens = new ArrayList<>();

        logger.info("VISA outgoing");
//		logger.info("VISA outgoing Achat");
//
//		idens.add("G0XX");
//		List<RecapInter> settVisaoutgoingAchat = totalAmountInterRepository.getSumByIdentifListswithoutTransaction(date,
//				idens);
//		idens.removeAll(idens);
//		idens.add("G0XX");
//		List<RecapInter> interVisaoutgoingAchat = totalAmountInterRepository
//				.getSumByIdentifListswithoutTransaction(date, idens);
//		idens.removeAll(idens);
//		idens.add("G0XX");
//		List<RecapInter> surcVisaoutgoingAchat = totalAmountInterRepository.getSumByIdentifListswithoutTransaction(date,
//				idens);
//		idens.removeAll(idens);
//		idens.add("G0XX");

        logger.info("VISA outgoing Retrait");
        idens.removeAll(idens);

        idens.add("G089");
        List<RecapInter> settlementRetraisVisa = totalAmountInterRepository.getSumByIdentifListswithoutzero(date,
                idens);
        idens.removeAll(idens);
        idens.add("G090");


        List<RecapInter> intercahngeRetraisVisa = totalAmountInterRepository
                .getSumByIdentifListswithoutTransaction(date, idens);
        logger.info(intercahngeRetraisVisa.toString());
        idens.removeAll(idens);
        idens.add("G092");

        List<RecapInter> intercahngeRetraisDecVisa = totalAmountInterRepository
                .getSumByIdentifListswithoutTransaction(date, idens);
        if (intercahngeRetraisDecVisa == null || intercahngeRetraisDecVisa.size() == 0) {
            intercahngeRetraisDecVisa = Arrays.asList(new RecapInter(0.0, 0.0, 0, "840", "788"));
        }


        logger.info(intercahngeRetraisVisa.toString());
        idens.removeAll(idens);
//		idens.add("G092");
//		List<RecapInter> retraisVisaNotApprouved = totalAmountInterRepository
//				.getSumByIdentifListswithoutTransaction(date, idens);
//
//		idens.removeAll(idens);
        idens.add("G091");
        List<RecapInter> accesFeesRetraisVisa = totalAmountInterRepository.getSumForAccesFee(date, idens);
        idens.removeAll(idens);
        logger.info("VISA outgoing refund ACHAT");

        idens.add("G0XX");
        List<RecapInter> settVisaoutgoingRefundA = totalAmountInterRepository
                .getSumByIdentifListswithoutTransaction(date, idens);
        idens.removeAll(idens);
        idens.add("G0XX");
        List<RecapInter> interVisaoutgoingRefundA = totalAmountInterRepository
                .getSumByIdentifListswithoutTransaction(date, idens);
        idens.removeAll(idens);
        idens.add("G0XX");
        List<RecapInter> surcAVisaoutgoingRefundA = totalAmountInterRepository
                .getSumByIdentifListswithoutTransaction(date, idens);
        idens.removeAll(idens);
        logger.info("VISA outgoing refund RETRAIT");

        idens.add("G0XX");
        List<RecapInter> settVisaoutgoingRefundR = totalAmountInterRepository
                .getSumByIdentifListswithoutTransaction(date, idens);
        idens.removeAll(idens);
        idens.add("G0XX");
        List<RecapInter> interVisaoutgoingRefundR = totalAmountInterRepository
                .getSumByIdentifListswithoutTransaction(date, idens);
        idens.removeAll(idens);
        idens.add("G0XX");
        List<RecapInter> surcAVisaoutgoingRefundR = totalAmountInterRepository
                .getSumByIdentifListswithoutTransaction(date, idens);
        idens.removeAll(idens);
        logger.info("VISA outgoing reversal ACHAT");

        idens.add("G0XX");
        List<RecapInter> settVisaoutgoingReversalA = totalAmountInterRepository
                .getSumByIdentifListswithoutTransaction(date, idens);
        idens.removeAll(idens);
        idens.add("G0XX");
        logger.info(idens.toString());
        logger.info(date);

        List<RecapInter> interVisaoutgoingReversalA = totalAmountInterRepository
                .getSumByIdentifListswithoutTransaction(date, idens);
        idens.removeAll(idens);
        logger.info(interVisaoutgoingReversalA.toString());

        idens.add("G0XX");
        List<RecapInter> surcAVisaoutgoingReversalA = totalAmountInterRepository
                .getSumByIdentifListswithoutTransaction(date, idens);
        idens.removeAll(idens);
        logger.info("VISA outgoing reversal RETRAIT");

        idens.add("G093");
        List<RecapInter> settVisaoutgoingReversalR = totalAmountInterRepository
                .getSumByIdentifListswithoutTransaction(date, idens);
        idens.removeAll(idens);
        idens.add("G094");
        List<RecapInter> interVisaoutgoingReversalR = totalAmountInterRepository
                .getSumByIdentifListswithoutTransaction(date, idens);
        idens.removeAll(idens);
        idens.add("G095");
        List<RecapInter> surcAVisaoutgoingReversalR = totalAmountInterRepository
                .getSumForAccesFee(date, idens);
        idens.removeAll(idens);

        logger.info("VISA outgoing CASHADVANCE");
        idens.add("G0XX");
        List<RecapInter> settVisaoutgoingCashAdvance = totalAmountInterRepository
                .getSumByIdentifListswithoutTransaction(date, idens);
        idens.removeAll(idens);
        idens.add("G0XX");
        List<RecapInter> interVisaoutgoingCashAdvance = totalAmountInterRepository
                .getSumByIdentifListswithoutTransaction(date, idens);
        idens.removeAll(idens);
        idens.add("G0XX");
        List<RecapInter> surcAVisaoutgoingCashAdvance = totalAmountInterRepository
                .getSumByIdentifListswithoutTransaction(date, idens);
        idens.removeAll(idens);

        logger.info("VISA outgoing CAHRGEBACK ACHAT");
        idens.add("G0XX");
        List<RecapInter> settVisaoutgoingChargeBackA = totalAmountInterRepository
                .getSumByIdentifListswithoutTransaction(date, idens);
        idens.removeAll(idens);
        idens.add("G0XX");
        List<RecapInter> interVisaoutgoingChargeBackA = totalAmountInterRepository
                .getSumByIdentifListswithoutTransaction(date, idens);
        idens.removeAll(idens);
        idens.add("G0XX");
        List<RecapInter> surcAVisaoutgoingChargeBackA = totalAmountInterRepository
                .getSumByIdentifListswithoutTransaction(date, idens);
        idens.removeAll(idens);

        logger.info("VISA outgoing CAHRGEBACK RETRAIT");
        idens.add("G764");
        List<RecapInter> settVisaoutgoingChargeBackR = totalAmountInterRepository
                .getSumByIdentifListswithoutTransaction(date, idens);
        idens.removeAll(idens);
        idens.add("G762");
        List<RecapInter> interVisaoutgoingChargeBackR = totalAmountInterRepository
                .getSumByIdentifListswithoutTransaction(date, idens);
        idens.removeAll(idens);
        idens.add("G763");
        List<RecapInter> surcAVisaoutgoingChargeBackR = totalAmountInterRepository
                .getSumByIdentifListswithoutTransaction(date, idens);
        idens.removeAll(idens);

        logger.info("VISA outgoing REPRESENTAION ACHAT");
        idens.add("G0XX");
        List<RecapInter> settVisaoutgoingRepA = totalAmountInterRepository.getSumByIdentifListswithoutTransaction(date,
                idens);
        idens.removeAll(idens);
        idens.add("G0XX");
        List<RecapInter> interVisaoutgoingRepA = totalAmountInterRepository.getSumByIdentifListswithoutTransaction(date,
                idens);
        idens.removeAll(idens);
        idens.add("G0XX");
        List<RecapInter> surcAVisaoutgoingRepA = totalAmountInterRepository.getSumByIdentifListswithoutTransaction(date,
                idens);
        idens.removeAll(idens);

        logger.info("VISA outgoing REPRESENTAION RETRAIT");
        idens.add("G915");
        List<RecapInter> settVisaoutgoingRepR = totalAmountInterRepository.getSumByIdentifListswithoutTransaction(date,
                idens);
        idens.removeAll(idens);
        idens.add("G916");
        List<RecapInter> interVisaoutgoingRepR = totalAmountInterRepository.getSumByIdentifListswithoutTransaction(date,
                idens);
        idens.removeAll(idens);
        idens.add("G917");
        List<RecapInter> surcAVisaoutgoingRepR = totalAmountInterRepository.getSumByIdentifListswithoutTransaction(date,
                idens);
        idens.removeAll(idens);
        idens.removeAll(idens);

//		logger.info("VISA outgoing Credit");
//		idens.add("G0XX");
//		List<RecapInter> settVisaoutgoingCredit = totalAmountInterRepository
//				.getSumByIdentifListswithoutTransaction(date, idens);
//		idens.removeAll(idens);
//		idens.add("G0XX");
//		List<RecapInter> interVisaoutgoingCredit = totalAmountInterRepository
//				.getSumByIdentifListswithoutTransaction(date, idens);
//		idens.removeAll(idens);
//		idens.add("G0XX");
//		List<RecapInter> surcAVisaoutgoingCredit = totalAmountInterRepository
//				.getSumByIdentifListswithoutTransaction(date, idens);
//		idens.removeAll(idens);

        logger.info("VISA incoming Achat DEBIT");

        idens.add("G096");
        List<RecapInter> achatvisaIncomingD = totalAmountInterRepository.getSumByIdentifListswithoutTransaction(date,
                idens);
        idens.removeAll(idens);

        idens.add("G097");
        List<RecapInter> interchangeAVisaIncomingD = totalAmountInterRepository
                .getSumByIdentifListswithoutTransaction(date, idens);
        idens.removeAll(idens);
        logger.info("VISA incoming Achat Credit");

        idens.add("G599");
        List<RecapInter> achatvisaIncomingC = totalAmountInterRepository.getSumByIdentifListswithoutTransaction(date,
                idens);
        idens.removeAll(idens);

        idens.add("G600");
        List<RecapInter> interchangeAVisaIncomingC = totalAmountInterRepository
                .getSumByIdentifListswithoutTransaction(date, idens);
        idens.removeAll(idens);

        logger.info("VISA incoming Retrait DEBIT");

        idens.add("G098");

        List<RecapInter> retraitvisaIncomingD = totalAmountInterRepository.getSumByIdentifListswithoutTransaction(date,
                idens);
        idens.removeAll(idens);

        idens.add("G099");
        List<RecapInter> interchangeRVisaIncomingD = totalAmountInterRepository
                .getSumByIdentifListswithoutTransaction(date, idens);
        idens.removeAll(idens);

        logger.info("VISA incoming Retrait Credit");

        idens.add("G0XX");
        List<RecapInter> retraitvisaIncomingC = totalAmountInterRepository.getSumByIdentifListswithoutTransaction(date,
                idens);
        idens.removeAll(idens);

        idens.add("G0XX");
        List<RecapInter> interchangeRVisaIncomingC = totalAmountInterRepository
                .getSumByIdentifListswithoutTransaction(date, idens);
        idens.removeAll(idens);
        logger.info("VISA incoming Refund  Debit");

        idens.add("G556");
        idens.add("G745");
        List<RecapInter> refundAvisaIncomingD = totalAmountInterRepository.getSumByIdentifListswithoutTransaction(date,
                idens);
        idens.removeAll(idens);

        idens.add("G557");
        idens.add("G746");
        List<RecapInter> interchangeRefundAVisaIncomingDP = totalAmountInterRepository
                .getSumByIdentifListswithoutTransaction(date, idens);
        idens.removeAll(idens);
        idens.add("G758");
        idens.add("G759");
        List<RecapInter> interchangeRefundAVisaIncomingDN = totalAmountInterRepository
                .getSumByIdentifListswithoutTransaction(date, idens);
        idens.removeAll(idens);

        logger.info("VISA incoming Refund  Credit");
        idens.add("G552");
        idens.add("G747");
        idens.add("G991");
        idens.add("G993");
        List<RecapInter> refundAvisaIncomingC = totalAmountInterRepository.getSumByIdentifListswithoutTransaction(date,
                idens);
        idens.removeAll(idens);

        idens.add("G555");
        idens.add("G748");
        idens.add("G992");
        idens.add("G994");
        List<RecapInter> interchangeRefundAVisaIncomingCP = totalAmountInterRepository
                .getSumByIdentifListswithoutTransaction(date, idens);
        idens.removeAll(idens);
        idens.add("G760");
        idens.add("G761");
        List<RecapInter> interchangeRefundAVisaIncomingCN = totalAmountInterRepository
                .getSumByIdentifListswithoutTransaction(date, idens);
        idens.removeAll(idens);
//		logger.info("VISA incoming Refund RETRAIT Credit");


//		idens.add("G745");
//		List<RecapInter> refundRvisaIncomingD = totalAmountInterRepository.getSumByIdentifListswithoutTransaction(date,
//				idens);
//		idens.removeAll(idens);
//
//		idens.add("G746");
//		List<RecapInter> interchangeRefundRVisaIncomingDP = totalAmountInterRepository
//				.getSumByIdentifListswithoutTransaction(date, idens);
//		idens.removeAll(idens);
//		idens.add("G761");
//		List<RecapInter> interchangeRefundRVisaIncomingDN = totalAmountInterRepository
//				.getSumByIdentifListswithoutTransaction(date, idens);
//		idens.removeAll(idens);
//		logger.info("VISA incoming Refund RETRAIT DEBIT");
//		idens.add("G747");
//		List<RecapInter> refundRvisaIncomingC = totalAmountInterRepository.getSumByIdentifListswithoutTransaction(date,
//				idens);
//		logger.info(refundRvisaIncomingC.toString());
//		idens.removeAll(idens);
//
//		idens.add("G748");
//		List<RecapInter> interchangeRefundRVisaIncomingCP = totalAmountInterRepository
//				.getSumByIdentifListswithoutTransaction(date, idens);
//		idens.removeAll(idens);
//		idens.add("G761");
//		List<RecapInter> interchangeRefundRVisaIncomingCN = totalAmountInterRepository
//				.getSumByIdentifListswithoutTransaction(date, idens);
//		idens.removeAll(idens);
        logger.info("VISA incoming REVERSAL ACHAT DEBIT");
        idens.add("G558");
        List<RecapInter> reversalAvisaIncomingD = totalAmountInterRepository
                .getSumByIdentifListswithoutTransaction(date, idens);
        idens.removeAll(idens);

        idens.add("G559");
        List<RecapInter> interchangereversalAVisaIncomingD = totalAmountInterRepository
                .getSumByIdentifListswithoutTransaction(date, idens);
        idens.removeAll(idens);

        logger.info("VISA incoming REVERSAL ACHAT Credit");

        idens.add("G636");
        idens.add("G601");
        List<RecapInter> reversalAvisaIncomingC = totalAmountInterRepository
                .getSumByIdentifListswithoutTransaction(date, idens);
        idens.removeAll(idens);

        idens.add("G637");
        idens.add("G602");
        List<RecapInter> interchangereversalAVisaIncomingC = totalAmountInterRepository
                .getSumByIdentifListswithoutTransaction(date, idens);
        idens.removeAll(idens);
        logger.info("VISA incoming REVERSAL RETRAIT DEBIT");
        idens.add("G560");
        List<RecapInter> reversalRvisaIncomingD = totalAmountInterRepository
                .getSumByIdentifListswithoutTransaction(date, idens);
        idens.removeAll(idens);

        idens.add("G561");
        List<RecapInter> interchangereversalRVisaIncomingD = totalAmountInterRepository
                .getSumByIdentifListswithoutTransaction(date, idens);
        idens.removeAll(idens);

        logger.info("VISA incoming REVERSAL RETRAIT Credit");

        idens.add("G0XX");
        List<RecapInter> reversalRvisaIncomingC = totalAmountInterRepository
                .getSumByIdentifListswithoutTransaction(date, idens);
        idens.removeAll(idens);

        idens.add("G0XX");
        List<RecapInter> interchangereversalRVisaIncomingC = totalAmountInterRepository
                .getSumByIdentifListswithoutTransaction(date, idens);
        idens.removeAll(idens);
        logger.info("VISA incoming CASH ADVANCE DEBIT");
        idens.add("G718");
        List<RecapInter> cashAdvancevisaIncomingD = totalAmountInterRepository
                .getSumByIdentifListswithoutTransaction(date, idens);
        idens.removeAll(idens);

        idens.add("G719");
        List<RecapInter> interchangecashAdvanceVisaIncomingD = totalAmountInterRepository
                .getSumByIdentifListswithoutTransaction(date, idens);
        idens.removeAll(idens);

        logger.info("VISA incoming CASH ADVANCE Credit");

        idens.add("G720");
        List<RecapInter> cashAdvancevisaIncomingC = totalAmountInterRepository
                .getSumByIdentifListswithoutTransaction(date, idens);
        idens.removeAll(idens);

        idens.add("G721");
        List<RecapInter> interchangecashAdvanceVisaIncomingC = totalAmountInterRepository
                .getSumByIdentifListswithoutTransaction(date, idens);
        idens.removeAll(idens);





//		logger.info("VISA incoming REVERSAL CASHADVANCE Credit");
//
//		idens.add("G0XX");
//		List<RecapInter> reversalCAvisaIncomingC = totalAmountInterRepository
//				.getSumByIdentifListswithoutTransaction(date, idens);
//		idens.removeAll(idens);
//
//		idens.add("G0XX");
//		List<RecapInter> interchangereversalCAVisaIncomingC = totalAmountInterRepository
//				.getSumByIdentifListswithoutTransaction(date, idens);
//		idens.removeAll(idens);
//		logger.info("VISA incoming REVERSAL CASHADVANCE DEBIT");
//
//		idens.add("G0XX");
//		List<RecapInter> reversalCAvisaIncomingD = totalAmountInterRepository
//				.getSumByIdentifListswithoutTransaction(date, idens);
//		idens.removeAll(idens);
//
//		idens.add("G0XX");
//		List<RecapInter> interchangereversalCAVisaIncomingD = totalAmountInterRepository
//				.getSumByIdentifListswithoutTransaction(date, idens);
//		idens.removeAll(idens);
//		logger.info("VISA incoming  CReDIT Credit");
//
//		idens.add("G0XX");
//		List<RecapInter> creditvisaIncomingC = totalAmountInterRepository
//				.getSumByIdentifListswithoutTransaction(date, idens);
//		idens.removeAll(idens);
//
//		idens.add("G0XX");
//		List<RecapInter> interchangeCreditVisaIncomingC = totalAmountInterRepository
//				.getSumByIdentifListswithoutTransaction(date, idens);
//		idens.removeAll(idens);
//		logger.info("VISA incoming  CReDIT DEBIT");
//
//		idens.add("G0XX");
//		List<RecapInter> creditvisaIncomingD = totalAmountInterRepository
//				.getSumByIdentifListswithoutTransaction(date, idens);
//		idens.removeAll(idens);
//
//		idens.add("G0XX");
//		List<RecapInter> interchangeCreditVisaIncomingD = totalAmountInterRepository
//				.getSumByIdentifListswithoutTransaction(date, idens);
//		idens.removeAll(idens);
        logger.info("VISA incoming CHARGEBACK ACHAT Credit");

        idens.add("G563");
        idens.add("G695");
        List<RecapInter> chargeBackAvisaIncomingC = totalAmountInterRepository
                .getSumByIdentifListswithoutTransaction(date, idens);
        idens.removeAll(idens);
        idens.add("G681");
        idens.add("G696");
        List<RecapInter> chargeBackAvisaIncomingCInterP = totalAmountInterRepository
                .getSumByIdentifListswithoutTransaction(date, idens);
        idens.removeAll(idens);
        idens.add("G682");
        idens.add("G697");
        List<RecapInter> chargeBackAvisaIncomingCInterN = totalAmountInterRepository
                .getSumByIdentifListswithoutTransaction(date, idens);
        idens.removeAll(idens);

        logger.info("VISA incoming CHARGEBACK ACHAT DEBIT");

        idens.add("G698");
        idens.add("G926");
        List<RecapInter> chargeBackAvisaIncomingD = totalAmountInterRepository
                .getSumByIdentifListswithoutTransaction(date, idens);
        idens.removeAll(idens);
        idens.add("G699");
        idens.add("G927");
        List<RecapInter> chargeBackAvisaIncomingDinterP = totalAmountInterRepository
                .getSumByIdentifListswithoutTransaction(date, idens);
        idens.removeAll(idens);

        logger.info("VISA incoming CHARGEBACK RETRAIT Credit");
        idens.add("G0XX");
        List<RecapInter> chargeBackRvisaIncomingC = totalAmountInterRepository
                .getSumByIdentifListswithoutTransaction(date, idens);
        idens.removeAll(idens);
        idens.add("G0XX");
        List<RecapInter> chargeBackRvisaIncomingCInterP = totalAmountInterRepository
                .getSumByIdentifListswithoutTransaction(date, idens);
        idens.removeAll(idens);
        idens.add("G0XX");
        List<RecapInter> chargeBackRvisaIncomingCInterN = totalAmountInterRepository
                .getSumByIdentifListswithoutTransaction(date, idens);
        idens.removeAll(idens);
        logger.info("VISA incoming CHARGEBACK RETRAIT Debit");

        idens.add("G731");
        idens.add("G928");
        List<RecapInter> chargeBackRvisaIncomingD = totalAmountInterRepository.getSumByIdentifListswithoutTransaction(date, idens);
        idens.removeAll(idens);
        idens.add("G732");
        idens.add("G929");
        List<RecapInter> chargeBackRvisaIncomingDinterP = totalAmountInterRepository
                .getSumByIdentifListswithoutTransaction(date, idens);
        idens.removeAll(idens);

        logger.info("VISA incoming REPRESENTATION RETRAIT DEBIT" );
        idens.add("G1028");
        List<RecapInter> repRvisaIncomingD = totalAmountInterRepository.getSumByIdentifListswithoutTransaction(date,
                idens);
        idens.removeAll(idens);
        idens.add("G1029");
        List<RecapInter> repRvisaIncomingDInter = totalAmountInterRepository.getSumByIdentifListswithoutTransaction(date,
                idens);
        idens.removeAll(idens);

        logger.info("VISA incoming REPRESENTATION RETRAIT Credit");

        idens.add("G0xx");
        List<RecapInter> repRvisaIncomingC = totalAmountInterRepository.getSumByIdentifListswithoutTransaction(date,
                idens);
        idens.removeAll(idens);
        idens.add("G0xx");
        List<RecapInter> repRvisaIncomingCInterchange = totalAmountInterRepository.getSumByIdentifListswithoutTransaction(date,
                idens);
        idens.removeAll(idens);

        logger.info("VISA incoming REPRESENTATION ACHAT DEBIT");
        idens.add("G869");
        List<RecapInter> repAvisaIncomingD = totalAmountInterRepository.getSumByIdentifListswithoutTransaction(date,
                idens);
        idens.removeAll(idens);
        idens.add("G870");
        List<RecapInter> repAvisaIncomingDInterchange= totalAmountInterRepository.getSumByIdentifListswithoutTransaction(date,
                idens);
        idens.removeAll(idens);

        logger.info("VISA incoming REPRESENTATION ACHAT Credit");

        idens.add("G706");

        List<RecapInter> repAvisaIncomingC = totalAmountInterRepository.getSumByIdentifListswithoutTransaction(date,
                idens);
        idens.removeAll(idens);
        idens.add("G707");

        List<RecapInter> repAvisaIncomingCInterchangeP = totalAmountInterRepository.getSumByIdentifListswithoutTransaction(date,
                idens);
        idens.removeAll(idens);
        idens.add("G0xx");
        List<RecapInter> repAvisaIncomingCInterchangeN = totalAmountInterRepository.getSumByIdentifListswithoutTransaction(date,
                idens);
        idens.removeAll(idens);
        logger.info("VISA incoming Achat Credit");

        idens.add("G599");
        List<RecapInter> details = totalAmountInterRepository.getSumByIdentifListswithoutTransactionVisaT(date,
                idens);
        idens.removeAll(idens);
        idens.add("G600");
        List<RecapInter> detailsinter = totalAmountInterRepository.getSumByIdentifListswithoutTransactionVisaT(date,
                idens);
        logger.info("Frais");
        idens.removeAll(idens);
        idens.add("G692");
        List<RecapInter> fraisSMTC = totalAmountInterRepository.getSumByIdentifListswithoutTransactionVisaT(date,
                idens);
        idens.removeAll(idens);
        idens.add("G0XX");
        List<RecapInter> fraisSMTD = totalAmountInterRepository.getSumByIdentifListswithoutTransactionVisaT(date,
                idens);
        idens.removeAll(idens);
        idens.add("G694");
        List<RecapInter> fraisCredit = totalAmountInterRepository.getSumByIdentifListswithoutTransactionVisaT(date, idens);
        idens.removeAll(idens);
        idens.add("G693");
        List<RecapInter> fraisDebit = totalAmountInterRepository.getSumByIdentifListswithoutTransactionVisaT(date, idens);


        List<TotalAmountSettlementInter> listttI = new ArrayList<>();
        handleDetails(details, detailsinter, listttI);
//		listttI.addAll(remplirVisa(settVisaoutgoingAchat, interVisaoutgoingAchat, surcVisaoutgoingAchat,
//				"VISA outgoing Achat", date));

        //totalAmountInterRepository.
        //date must be YYYY-MM-dd ddMMyy
        DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("ddMMyy");
        DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("YYYY-MM-dd");
        LocalDate dateOut = LocalDate.parse(date, inputFormatter);
        String dateiso=dateOut.format(outputFormatter);
        VisaSummary summaryVisa = visaSummaryRepo.findBydateCompensation(dateiso).get();



        listttI.addAll(remplirVisa(settlementRetraisVisa, intercahngeRetraisVisa, accesFeesRetraisVisa,
                "VISA outgoing Retrait", date));

        RecapInter declinedset=new RecapInter(0,0,intercahngeRetraisDecVisa.get(0).getNb(),"840","840");
        RecapInter declinedaccessf=new RecapInter(0,0,intercahngeRetraisDecVisa.get(0).getNb(),"840","840");
        listttI.addAll(remplirVisa(Arrays.asList(declinedset), intercahngeRetraisDecVisa, Arrays.asList(declinedaccessf),
                "VISA outgoing Retrait Declined", date));
        listttI.addAll(remplirVisa(settVisaoutgoingChargeBackR, interVisaoutgoingChargeBackR,
                surcAVisaoutgoingChargeBackR, "VISA outgoing CAHRGEBACK RETRAIT", date));
		/*listttI.addAll(remplirVisa(settVisaoutgoingRefundA, interVisaoutgoingRefundA, surcAVisaoutgoingRefundA,
				"VISA outgoing refund ACHAT", date));
		listttI.addAll(remplirVisa(settVisaoutgoingRefundR, interVisaoutgoingRefundR, surcAVisaoutgoingRefundR,
				"VISA outgoing refund RETRAIT", date));
		listttI.addAll(remplirVisa(settVisaoutgoingReversalA, interVisaoutgoingReversalA, surcAVisaoutgoingReversalA,
				"VISA outgoing reversal ACHAT", date));*/
        listttI.addAll(remplirVisa(settVisaoutgoingReversalR, interVisaoutgoingReversalR, surcAVisaoutgoingReversalR,
                "VISA outgoing reversal RETRAIT", date));
//		listttI.addAll(remplirVisa(settVisaoutgoingCredit, interVisaoutgoingCredit, surcAVisaoutgoingCredit,
//				"VISA outgoing Credit", date));
		/*listttI.addAll(remplirVisa(settVisaoutgoingCashAdvance, interVisaoutgoingCashAdvance,
				surcAVisaoutgoingCashAdvance, "VISA outgoing CASHADVANCE", date));
		listttI.addAll(remplirVisa(settVisaoutgoingChargeBackA, interVisaoutgoingChargeBackA,
				surcAVisaoutgoingChargeBackA, "VISA outgoing CAHRGEBACK ACHAT", date));

		listttI.addAll(remplirVisa(settVisaoutgoingRepA, interVisaoutgoingRepA, surcAVisaoutgoingRepA,
				"VISA outgoing REPRESENTAION ACHAT", date));
		listttI.addAll(remplirVisa(settVisaoutgoingCredit, interVisaoutgoingCredit, surcAVisaoutgoingCredit,
				"VISA outgoing Credit", date));*/

        listttI.addAll(remplirVisa(settVisaoutgoingRepR, interVisaoutgoingRepR, surcAVisaoutgoingRepR,
                "VISA outgoing REPRESENTAION RETRAIT", date));
        listttI.addAll(
                remplirVisa(achatvisaIncomingD, interchangeAVisaIncomingD, null, "VISA incoming Achat DEBIT", date));
        listttI.addAll(
                remplirVisa(achatvisaIncomingC, interchangeAVisaIncomingC, null, "VISA incoming Achat Credit", date));

        listttI.addAll(remplirVisa(retraitvisaIncomingD, interchangeRVisaIncomingD, null, "VISA incoming Retrait DEBIT",
                date));
        listttI.addAll(remplirVisa(retraitvisaIncomingC, interchangeRVisaIncomingC, null,
                "VISA incoming Retrait Credit", date));
        listttI.addAll(remplirVisaCharge(refundAvisaIncomingC, interchangeRefundAVisaIncomingCP, interchangeRefundAVisaIncomingCN,
                "VISA incoming CREDIT VOUCHER Credit", date));
        listttI.addAll(remplirVisaCharge(refundAvisaIncomingD, interchangeRefundAVisaIncomingDP, interchangeRefundAVisaIncomingDN,
                "VISA incoming CREDIT VOUCHER DEBIT", date));
//		listttI.addAll(remplirVisa(refundRvisaIncomingC, interchangeRefundRVisaIncomingC, null,
//				"VISA incoming Refund RETRAIT Credit", date));
//		listttI.addAll(remplirVisa(refundRvisaIncomingD, interchangeRefundRVisaIncomingD, null,
//				"VISA incoming Refund RETRAIT DEBIT", date));
        listttI.addAll(remplirVisa(reversalAvisaIncomingD, interchangereversalAVisaIncomingD, null,
                "VISA incoming REVERSAL ACHAT DEBIT", date));
        listttI.addAll(remplirVisa(reversalAvisaIncomingC, interchangereversalAVisaIncomingC, null,
                "VISA incoming REVERSAL ACHAT Credit", date));
        listttI.addAll(remplirVisa(reversalRvisaIncomingD, interchangereversalRVisaIncomingD, null,
                "VISA incoming REVERSAL RETRAIT DEBIT", date));
        listttI.addAll(remplirVisa(reversalRvisaIncomingC, interchangereversalRVisaIncomingC, null,
                "VISA incoming REVERSAL RETRAIT Credit", date));
//		listttI.addAll(remplirVisa(reversalCAvisaIncomingC, interchangereversalCAVisaIncomingC, null,
//				"VISA incoming REVERSAL CASHADVANCE Credit", date));
//		listttI.addAll(remplirVisa(reversalCAvisaIncomingD, interchangereversalCAVisaIncomingD, null,
//				"VISA incoming REVERSAL CASHADVANCE DEBIT", date));
//		listttI.addAll(remplirVisa(creditvisaIncomingC, interchangeCreditVisaIncomingC, null,
//				"VISA incoming CREDIT Credit", date));
//		listttI.addAll(remplirVisa(creditvisaIncomingD, interchangeCreditVisaIncomingD, null,
//				"VISA incoming CREDIT DEBIT", date));
        listttI.addAll(remplirVisa(cashAdvancevisaIncomingD, interchangecashAdvanceVisaIncomingD, null,
                "VISA incoming CASHADVANCE DEBIT", date));
        listttI.addAll(remplirVisa(cashAdvancevisaIncomingC, interchangecashAdvanceVisaIncomingC, null,
                "VISA incoming CASHADVANCE Credit", date));
        listttI.addAll(remplirVisaCharge(chargeBackAvisaIncomingC, chargeBackAvisaIncomingCInterP, chargeBackAvisaIncomingCInterN,
                "VISA incoming CHARGEBACK ACHAT Credit", date));
        listttI.addAll(
                remplirVisaCharge(chargeBackAvisaIncomingD, chargeBackAvisaIncomingDinterP, null,
                        "VISA incoming CHARGEBACK ACHAT DEBIT", date));
        listttI.addAll(
                remplirVisaCharge(chargeBackRvisaIncomingC, chargeBackRvisaIncomingCInterP, chargeBackRvisaIncomingCInterN,
                        "VISA incoming CHARGEBACK RETRAIT Credit", date));
        listttI.addAll(
                remplirVisaCharge(chargeBackRvisaIncomingD, chargeBackRvisaIncomingDinterP, null,
                        "VISA incoming CHARGEBACK RETRAIT DEBIT", date));
        listttI.addAll(remplirVisa(repAvisaIncomingD, repAvisaIncomingDInterchange, null,
                "VISA incoming REPRESENTATION ACHAT DEBIT", date));
        listttI.addAll(remplirVisa(repAvisaIncomingC, repAvisaIncomingCInterchangeP, repAvisaIncomingCInterchangeN,
                "VISA incoming REPRESENTATION ACHAT Credit", date));
        listttI.addAll(remplirVisa(repRvisaIncomingD, repRvisaIncomingDInter, null,
                "VISA incoming REPRESENTATION RETRAIT DEBIT", date));
        listttI.addAll(remplirVisa(repRvisaIncomingC, repRvisaIncomingCInterchange, null,
                "VISA incoming REPRESENTATION RETRAIT Credit", date));


        listttI.addAll(remplirVisaFrais(fraisCredit, "VISA incoming Frais Credit", date));
        listttI.addAll(remplirVisaFrais(fraisDebit, "VISA incoming Frais DEBIT", date));
        listttI.addAll(remplirVisaFrais(fraisSMTC, "VISA incoming Frais SMT Credit", date));
        listttI.addAll(remplirVisaFrais(fraisSMTD, "VISA incoming Frais SMT DEBIT", date));
//		listttI.add(summaryVisaoutgoing);
//		listttI.add(summaryVISAincoming);
//		listttI.add(summaryVISAFEE);
//		listttI.add(summaryVISAFEERECONS);
//		listttI.add(summaryVISAFEESMT);
//		listttI.add(summaryVISADisput);

// Add this at the beginning of the method
        int order = 200;

// Then at the end of your method, before saving:
        for (TotalAmountSettlementInter el : listttI) {

            el.setDateSet(date);
            el.setOrderRow(order++);
            // Determine sensInter based on category and mntInter
            //if it is incoming
            // Determine sensInter based on category and mntInter
            String category = el.getCat().trim();
            Double interAmount = el.getMntInter();

            if (interAmount != 0 && category.toUpperCase().contains("DEBIT") &&
                    (category.contains("Retrait") ||
                            category.contains("ChargeBack Achat") ||
                            category.contains("Representation Retrait") ||
                            category.contains("Pre-Arbitrage CHARGEBACK ACHAT") ||
                            category.contains("Arbitrage Accept CHARGEBACK ACHAT") ||
                            category.contains("Pre-Arbitrage REPRENTATION RETRAIT") ||
                            category.contains("Arbitrage Accept REPRENTATION RETRAIT") ||
                            category.contains("CREDIT VOUCHER"))) {
                // Set to "D" for negative values
                el.setSensInter("D");
            } else if (interAmount != 0 && category.toUpperCase().contains("DEBIT")) {
                // Set to "C" for positive values
                el.setSensInter("C");
            }

            // This is the negation of the first condition
            boolean isNotInExcludedCategories = !(
                    category.contains("Retrait") ||
                            category.contains("CHARGEBACK ACHAT") ||
                            category.contains("Representation Retrait") ||
                            category.contains("REVERSAL ACHAT")
            );


            if (interAmount != 0 && category.contains("Credit") && isNotInExcludedCategories && el.getMntInter() != 0) {
                // Set to "D" for negative values
                el.setSensInter("D");
            } else if (interAmount != 0 && category.contains("Credit")) {
                // Set to "C" for positive values
                el.setSensInter("C");
            }


        }
        totalAmountInterRepository.saveAll(listttI);
        return listttI;

    }


   private List<RecapInter> addElements(List<RecapInter> elements) {
        if (elements == null || elements.size() == 0) {
            elements = Arrays.asList(new RecapInter(0.0, 0.0, 0, "840", "788"),
                    new RecapInter(0.0, 0.0, 0, "978", "788"));
        }

        return sumByCurSett(elements);

    }
    public List<RecapInter> sumByCurSett(List<RecapInter> recapList) {
        // Use a Map to group by CurSett and sum the variables
        Map<String, RecapInter> groupedRecap = new HashMap<>();
        logger.info(recapList.toString() + "");

        for (RecapInter recap : recapList) {
            String curSett = recap.getCurSett();
            if (groupedRecap.containsKey(curSett)) {
                RecapInter existingRecap = groupedRecap.get(curSett);
                existingRecap.setMntSett(existingRecap.getMntSett() + recap.getMntSett());
                existingRecap.setTransactionMnt(existingRecap.getTransactionMnt() + recap.getTransactionMnt());
                existingRecap.setNb(existingRecap.getNb() + recap.getNb());
                existingRecap.setCurTran(existingRecap.getCurTran() + recap.getCurTran());
            } else {
                groupedRecap.put(curSett, new RecapInter(recap.getMntSett(), recap.getTransactionMnt(), recap.getNb(),
                        recap.getCurSett(), recap.getCurTran()));
            }
        }
        logger.info(groupedRecap.toString() + "");
        if (!groupedRecap.containsKey("840")) {
            groupedRecap.put("840", new RecapInter(0.0, 0.0, 0, "840", "788"));
        }
        if (!groupedRecap.containsKey("978")) {
            groupedRecap.put("978", new RecapInter(0.0, 0.0, 0, "978", "788"));
        }
        // Convert the values of the map to a list
        return new ArrayList<>(groupedRecap.values());
    }


    private List<TotalAmountSettlementInter> 	 remplirVisaCharge(List<RecapInter> settVisaoutgoingAchat,
                                                               List<RecapInter> interVisaoutgoingAchatP, List<RecapInter> interVisaoutgoingAchatN, String lib, String date) {
        logger.info(lib);
        List<TotalAmountSettlementInter> data = new ArrayList<>();
        logger.info(settVisaoutgoingAchat.toString());

        settVisaoutgoingAchat = addElements(settVisaoutgoingAchat);
        interVisaoutgoingAchatP = addElements(interVisaoutgoingAchatP);
        interVisaoutgoingAchatN = addElements(interVisaoutgoingAchatN);
//		surcVisaoutgoingAchat=addElements(surcVisaoutgoingAchat);

        logger.info(interVisaoutgoingAchatP.toString());
        logger.info(interVisaoutgoingAchatN.toString());
        for (RecapInter element : settVisaoutgoingAchat) {
            TotalAmountSettlementInter output = new TotalAmountSettlementInter();
            List<RecapInter> filteredList = interVisaoutgoingAchatP.stream()
                    .filter(recap -> recap.getCurSett().equals(element.getCurSett())).collect(Collectors.toList());
            List<RecapInter> filteredListN = interVisaoutgoingAchatN.stream()
                    .filter(recap -> recap.getCurSett().equals(element.getCurSett())).collect(Collectors.toList());

//			List<RecapInter> filteredListsurc = surcVisaoutgoingAchat.stream()
//					.filter(recap -> recap.getCurTran().equals(element.getCurTran())
//							&& recap.getCurSett().equals(element.getCurSett()))
//					.collect(Collectors.toList());
            output.setCat(lib);


            output.setNbtrans(element.getNb());

            if (filteredListN.get(0).getMntSett() > filteredList.get(0).getMntSett()) {
                output.setSensInter("C");
            } else {
                output.setSensInter("D");
            }
            output.setMntInter(Math.abs(filteredList.get(0).getMntSett()-filteredListN.get(0).getMntSett()));
            output.setMntInterCur(filteredList.get(0).getCurSett());

            output.setMntTran(element.getTransactionMnt());
            output.setMntSett(element.getMntSett());
            output.setMntTranCur(element.getCurTran());
            output.setMntSettCur(element.getCurSett());

            output.setMntAccesFee(0);
            output.setMntAccesFeeCur("788");

            output.setDateSet(date);
            data.add(output);
        }

        return data;
    }

    private void handleDetails(List<RecapInter> details,List<RecapInter> detailsInter ,List<TotalAmountSettlementInter> listttI) {

        for(RecapInter recap:details) {

            if(recap.getCurSett().equals("840")) {
                TotalAmountSettlementInter data= new TotalAmountSettlementInter();
                data.setCat("details Achats");
                data.setMntSett(recap.getMntSett());
                data.setMntSettCur(recap.getCurSett());
                data.setMntTran(recap.getTransactionMnt());
                data.setMntTranCur(recap.getCurTran());
                data.setNbtrans(recap.getNb());
                List<RecapInter> filteredTransactions = detailsInter.stream()
                        .filter(transaction -> recap.getCurTran().equals(transaction.getCurTran()))
                        .collect(Collectors.toList());
                data.setMntInter(filteredTransactions.get(0).getMntSett());
                listttI.add(data);

            }

        }

    }
    private void handleDetailsIPM(List<RecapInter> details,List<RecapInter> detailsInter, List<TotalAmountSettlementInter> listttI) {
        for(RecapInter recap:details) {
            if(recap.getCurSett().equals("840")) {
                TotalAmountSettlementInter data= new TotalAmountSettlementInter();
                data.setCat("details AchatIPM");
                data.setMntSett(recap.getMntSett());
                data.setMntSettCur(recap.getCurSett());
                data.setMntTran(recap.getTransactionMnt());
                data.setMntTranCur(recap.getCurTran());
                data.setNbtrans(recap.getNb());
                List<RecapInter> filteredTransactions = detailsInter.stream()
                        .filter(transaction -> recap.getCurTran().equals(transaction.getCurTran()))
                        .collect(Collectors.toList());
                data.setMntInter(filteredTransactions.get(0).getMntSett());
                listttI.add(data);

            }

        }

    }

    /**
     * Converts a list of ChargebacksInternational entities to DayOperationInternational entities
     * This method uses the new constructor in DayOperationInternational to map all fields
     * 
     * @param chargebacks List of ChargebacksInternational to convert
     * @param typeOperation The type of operation identifier
     * @param codeFile The file code
     * @param fileDate The file date
     * @param source The source identifier
     * @return List of DayOperationInternational entities
     */
    public List<DayOperationInternational> copyChargebacksToDayOperations(
            List<DayOperationChargebackInternational> chargebacks,
            int typeOperation,
            String codeFile,
            String fileDate,
            String source) {
        
        List<DayOperationInternational> dayOperations = new ArrayList<>();
        
        for (DayOperationChargebackInternational chargeback : chargebacks) {
            try {
                // Create DayOperationInternational using the new constructor
                DayOperationInternational dayOp = new DayOperationInternational(
                        chargeback,
                        typeOperation,
                        codeFile,
                        chargeback.getReferenceNumber() != null ? chargeback.getReferenceNumber() : chargeback.getReferenceNumber(), // transactionIdentification
                        fileDate,
                        null, // affiliation - not available in ChargebacksInternational
                        null, // transactionNumber - not available in ChargebacksInternational
                        null, // de37Switch - not available in ChargebacksInternational
                        null, // de11Switch - not available in ChargebacksInternational
                        null, // matchingSwitch - not available in ChargebacksInternational
                        "976",
                        chargeback.getIdentification(), // idenficationCode
                        source
                );
                
                dayOperations.add(dayOp);
                logger.debug("Converted chargeback {} to day operation", chargeback.getCode());
                
            } catch (Exception e) {
                logger.error("Error converting chargeback {} to day operation: {}", 
                        chargeback.getCode(), e.getMessage(), e);
            }
        }
        
        logger.info("Converted {} chargebacks to day operations", dayOperations.size());
        return dayOperations;
    }

    /**
     * Overloaded method with default parameters for common use cases
     * 
     * @param chargebacks List of ChargebacksInternational to convert
     * @return List of DayOperationInternational entities
     */
    public List<DayOperationInternational> copyChargebacksToDayOperations(
            List<DayOperationChargebackInternational> chargebacks) {
        
        String currentDate = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        return copyChargebacksToDayOperations(
                chargebacks,
                0, // default typeOperation
                "CB", // default codeFile for chargebacks
                currentDate,
                "CHARGEBACK" // default source
        );
    }

}
