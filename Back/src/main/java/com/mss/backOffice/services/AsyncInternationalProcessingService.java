package com.mss.backOffice.services;

import com.google.common.base.Throwables;
import com.mss.unified.dto.TotalVisaDto;
import com.mss.unified.entities.*;
import com.mss.unified.repositories.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Async service for international operations (VISA, MasterCard)
 * Follows Spring Boot best practices with @Async annotation
 * Compatible with Java 8
 */
@Service
public class AsyncInternationalProcessingService {
    private static final Logger logger = LoggerFactory.getLogger(AsyncInternationalProcessingService.class);
    private static final Logger LOG = LoggerFactory.getLogger(AsyncInternationalProcessingService.class);

    // Constants
    private static final String sourceVisa = "VISA";
    @Autowired
    private TvaCommissionFransaBankRepository tvaRepo;
    // Thread-safe date formatters
    public static ThreadLocal<SimpleDateFormat> inputDateFormat = ThreadLocal.withInitial(() -> new SimpleDateFormat("yyyy-MM-dd"));
    public static ThreadLocal<SimpleDateFormat> inputDateFormatVisa = ThreadLocal.withInitial(() -> new SimpleDateFormat("yyMMdd"));
    public static ThreadLocal<SimpleDateFormat> outputDateFormat = ThreadLocal.withInitial(() -> new SimpleDateFormat("ddMMyy"));
    public static ThreadLocal<SimpleDateFormat> outputDateFormatTransactionDate = ThreadLocal.withInitial(() -> new SimpleDateFormat("yyyyMMdd"));
    
    // Shared data structures
    public static List<DayOperationInternational> dayOperationsL = Collections.synchronizedList(new ArrayList<>());
    public static List<DayOperationInternational> dayOperationsChargebackL = Collections.synchronizedList(new ArrayList<>());
    public static List<VisaIncoming> updatedVisaInL = Collections.synchronizedList(new ArrayList<>());
    public static Map<String,String> accountDevise= new HashMap<>();
    public static Map<String,String> accountDinars= new HashMap<>();
    // Identification mapper
    public static Map<String, Integer> identificationMapper = new HashMap<>();
    
    // Repository dependencies
    @Autowired
    public VisaSummaryRepository visaSummaryRepository;
    @Autowired

    public TotalVisaRepository totalVisaRepository;

    @Autowired
    public DayOperationInternationalRepository operationRepo;
    
    @Autowired
    public OperationCodeRepository operationCodeRep;
    @Autowired
    public CommissionInternationalRepository commissionInternationalRepository;
    
    /**
     * Initialize the identification mapper
     */
    private void initializeIdentificationMapper() {
        if (identificationMapper.isEmpty()) {
            List<OperationCodeCommision> operationCodes = operationCodeRep.findAll();
            logger.info("operationCodes size: " + operationCodes.size());
            for (OperationCodeCommision operationCode : operationCodes) {
                identificationMapper.put(operationCode.getIdentification(), operationCode.getCode());
            }
        }
    }
    public static String getaccountByType(String cardNum, String type) {
        if ("DEVISE".equals(type)) {
            return accountDevise.get(cardNum);
        } else if ("DINARS".equals(type)) {
            return accountDinars.get(cardNum);
        }
        return null;
    }


    
    /**
     * Get current method name for origin tracking
     */
    private String getCurrentMethodName() {
        return Thread.currentThread().getStackTrace()[2].getMethodName();
    }
    
    /**
     * Process VISA incoming purchase transactions
     */
    @Async("taskExecutor")
    public CompletableFuture<String> visaIncomingAchat(AtomicBoolean stopSignal) {
        String methodName = getCurrentMethodName();
        String result = "ERROR";
        
        try {
            logger.info("Start processing visaIncomingAchat");
            
            List<VisaSummary> summaries = visaSummaryRepository.findByTreatM();
            if (summaries == null || summaries.isEmpty()) {
                logger.warn("No visa summaries found");
                return CompletableFuture.completedFuture("NO_DATA");
            }
            
            int totalProcessed = 0;
            
            for (VisaSummary visaSummary : summaries) {
                if (stopSignal.get() || Thread.currentThread().isInterrupted()) {
                    throw new InterruptedException("Stop signal received");
                }
                
                List<VisaIncoming> visaIncoming = operationRepo.VisaIncomingPOS(visaSummary.getSummaryCode());
                List<DayOperationInternational> dayOperations = new ArrayList<>();
                List<VisaIncoming> updatedVisaIn = new ArrayList<>();
                
                for (VisaIncoming element : visaIncoming) {
                    if (stopSignal.get() || Thread.currentThread().isInterrupted()) {
                        throw new InterruptedException("Stop signal received");
                    }
                    
                    Date date = null;
                    try {
                        date = inputDateFormat.get().parse(visaSummary.getSummary_date());
                    } catch (ParseException ex) {
                        logger.error("Date parsing error: {}", Throwables.getStackTraceAsString(ex));
                        continue;
                    }
                    
                    String outputDateString = outputDateFormat.get().format(date);
                    String transactionDate = element.getTerminalTransactionDate();
                    String transactionDateOutput = "";
                    
                    if ((transactionDate == null) || (transactionDate.equals("000000"))) {
                        transactionDate = element.getPurchaseDate();
                        int processingDateMonth = Integer.parseInt(outputDateString.substring(2, 4));
                        int processingDateDay = Integer.parseInt(outputDateString.substring(0, 2));
                        int processingDateYear = Integer.parseInt(outputDateString.substring(4));
                        int purchaseDateMonth = Integer.parseInt(transactionDate.substring(0, 2));
                        int purchaseDateDay = Integer.parseInt(transactionDate.substring(2));
                        
                        if (purchaseDateMonth > processingDateMonth) {
                            processingDateYear--;
                        } else if ((purchaseDateMonth == processingDateMonth) && ((purchaseDateDay > processingDateDay))) {
                            processingDateYear--;
                        }
                        
                        transactionDate = processingDateYear + "" + transactionDate;
                    }
                    
                    try {
                        Date transactionDateInput = inputDateFormatVisa.get().parse(transactionDate);
                        transactionDateOutput = outputDateFormatTransactionDate.get().format(transactionDateInput);
                    } catch (ParseException e) {
                        logger.error("Exception: {}", Throwables.getStackTraceAsString(e));
                        continue;
                    }
                    
                    // Create operations: G096, G097, G544, G545
                    DayOperationInternational G096 = new DayOperationInternational(findIdentification("G096"),
                            getFullAccountNumber(element), element.getTerminalId(), element.getSourceAmount(),
                            element.getSourceCurrencyCode(), getSourceToBaseExchangeRateInverse(element),
                            element.getBaseCurrency(), String.valueOf(element.getIncomingCode()), new Date(),
                            transactionDateOutput, element.getAuthorisationCode(), element.getAcquirerReferenceNumber(),
                            element.getBaseAmount(), element.getTransactionIdentifier(), outputDateString,
                            element.getSummaryCode(), element.getCardAcceptorId(), element.getTransactionIdentifier(),
                            element.getDe37(), element.getDe11(), element.getMatchingSwitch(),
                            element.getBaseCurrencyToDestinationCurrencyExchangeRate(), element.getDestinationAmount(),
                            element.getMerchantName(),"976","G096", sourceVisa);

                    DayOperationInternational G097 = new DayOperationInternational(findIdentification("G097"),
                            getFullAccountNumber(element), element.getTerminalId(), element.getSourceAmount(),
                            element.getSourceCurrencyCode(), getSourceToBaseExchangeRateInverse(element),
                            element.getBaseCurrency(), String.valueOf(element.getIncomingCode()), new Date(),
                            transactionDateOutput, element.getAuthorisationCode(), element.getAcquirerReferenceNumber(),
                            element.getInterchangeFeeAmount(), element.getTransactionIdentifier(), outputDateString,
                            element.getSummaryCode(), element.getCardAcceptorId(), element.getTransactionIdentifier(),
                            element.getDe37(), element.getDe11(), element.getMatchingSwitch(),
                            element.getBaseCurrencyToDestinationCurrencyExchangeRate(), element.getDestinationAmount(),
                            element.getMerchantName(), "976","G097",sourceVisa);

                    // TODO: Calculate markupNet and markupTva (currently undefined in original code)
                    double markupNet = 0.0;
                    double markupTva = 0.0;
                    CommissionInternational commossionIntern =commissionInternationalRepository.findByType("Achat").get(0);
                    BigDecimal commissionTTC = commossionIntern.getCommVariable().multiply(BigDecimal.valueOf(element.getBaseAmount()));
                     commissionTTC=commissionTTC.multiply(BigDecimal.valueOf(getdestinationToBaseExchangeRateInverse(element)));

                    BigDecimal commissionTTCEUR = commossionIntern.getCommVariableDevise().multiply(BigDecimal.valueOf(element.getBaseAmount()));
                    TvaCommissionFransaBank tvaCommissionFransaBank = tvaRepo.findTva();
                    int tva = Integer.parseInt(tvaCommissionFransaBank.getTva());


                    BigDecimal tvaRate = BigDecimal.valueOf(tva).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
                    BigDecimal tvaCommission = commissionTTC.multiply(tvaRate).setScale(2, RoundingMode.HALF_UP);
                    BigDecimal commissionHT = commissionTTC.subtract(tvaCommission).setScale(2, RoundingMode.HALF_UP);

//                    BigDecimal commissionNetDevise =
//                    BigDecimal tvaCommissionDevise =

                    DayOperationInternational G544 = new DayOperationInternational(findIdentification("G544"),
                            getFullAccountNumber(element), element.getTerminalId(), element.getSourceAmount(),
                            element.getSourceCurrencyCode(), getSourceToBaseExchangeRateInverse(element),
                            "788", String.valueOf(element.getIncomingCode()), new Date(),
                            transactionDateOutput, element.getAuthorisationCode(), element.getAcquirerReferenceNumber(),
                            commissionHT.doubleValue(), element.getTransactionIdentifier(), outputDateString,
                            element.getSummaryCode(), element.getCardAcceptorId(), element.getTransactionIdentifier(),
                            element.getDe37(), element.getDe11(), element.getMatchingSwitch(),
                            element.getBaseCurrencyToDestinationCurrencyExchangeRate(), element.getDestinationAmount(),
                            element.getMerchantName(), "208","G544",sourceVisa);

                    DayOperationInternational G545 = new DayOperationInternational(findIdentification("G545"),
                            getFullAccountNumber(element), element.getTerminalId(), element.getSourceAmount(),
                            element.getSourceCurrencyCode(), getSourceToBaseExchangeRateInverse(element),
                            "788", String.valueOf(element.getIncomingCode()), new Date(),
                            transactionDateOutput, element.getAuthorisationCode(), element.getAcquirerReferenceNumber(),
                            tvaCommission.doubleValue(), element.getTransactionIdentifier(), outputDateString,
                            element.getSummaryCode(), element.getCardAcceptorId(), element.getTransactionIdentifier(),
                            element.getDe37(), element.getDe11(), element.getMatchingSwitch(),
                            element.getBaseCurrencyToDestinationCurrencyExchangeRate(), element.getDestinationAmount(),
                            element.getMerchantName(),"208","G545", sourceVisa);
                    DayOperationInternational G546 = new DayOperationInternational(findIdentification("G546"),
                            getFullAccountNumber(element), element.getTerminalId(), element.getSourceAmount(),
                            element.getSourceCurrencyCode(), getSourceToBaseExchangeRateInverse(element),
                            element.getBaseCurrency(), String.valueOf(element.getIncomingCode()), new Date(),
                            transactionDateOutput, element.getAuthorisationCode(), element.getAcquirerReferenceNumber(),
                            commissionTTCEUR.doubleValue(), element.getTransactionIdentifier(), outputDateString,
                            element.getSummaryCode(), element.getCardAcceptorId(), element.getTransactionIdentifier(),
                            element.getDe37(), element.getDe11(), element.getMatchingSwitch(),
                            element.getBaseCurrencyToDestinationCurrencyExchangeRate(), element.getDestinationAmount(),
                            element.getMerchantName(), "976","G546",sourceVisa);
                    setNumRIBPorteurByType(G096, getFullAccountNumber(element), "DEVISE");
                    setNumRIBPorteurByType(G097, getFullAccountNumber(element), "DEVISE");
                    setNumRIBPorteurByType(G544, getFullAccountNumber(element), "DINARS");
                    setNumRIBPorteurByType(G545, getFullAccountNumber(element), "DINARS");
                    setNumRIBPorteurByType(G546, getFullAccountNumber(element), "DEVISE");
                    dayOperations.add(G096);
                    dayOperations.add(G097);
                    dayOperations.add(G544);
                    dayOperations.add(G545);
                    dayOperations.add(G546);

                    element.setTreat("1");
                    updatedVisaIn.add(element);
                    totalProcessed++;
                }

                dayOperationsL.addAll(dayOperations);
                updatedVisaInL.addAll(updatedVisaIn);
                logger.info("Processed summary {} with {} operations", visaSummary.getSummaryCode(), dayOperations.size());
            }

            logger.info("Completed visaIncomingAchat: {} operations", totalProcessed);
            result = "SUCCESS";

        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            logger.warn("Cancelled visaIncomingAchat");
            throw new RuntimeException("CANCELLED: visaIncomingAchat", ie);
        } catch (Exception e) {
            logger.error("Error in visaIncomingAchat: {}", Throwables.getStackTraceAsString(e));
            throw new RuntimeException("ERROR: visaIncomingAchat", e);
        }

        return CompletableFuture.completedFuture(result);
    }

    /**
     * Process VISA incoming ATM withdrawal transactions
     */
    @Async("taskExecutor")
    public CompletableFuture<String> visaIncomingRetrait(AtomicBoolean stopSignal) {
        String methodName = getCurrentMethodName();
        String result = "ERROR";

        try {
            logger.info("Start processing visaIncomingRetrait");

            List<VisaSummary> summaries = visaSummaryRepository.findByTreatM();
            if (summaries == null || summaries.isEmpty()) {
                return CompletableFuture.completedFuture("NO_DATA");
            }

            for (VisaSummary visaSummary : summaries) {
                if (stopSignal.get() || Thread.currentThread().isInterrupted()) {
                    throw new InterruptedException("Stop signal received");
                }

                List<VisaIncoming> visaIncoming = operationRepo.VisaIncomingATM(visaSummary.getSummaryCode());
                List<DayOperationInternational> dayOperations = new ArrayList<>();
                List<VisaIncoming> updatedVisaIn = new ArrayList<>();

                for (VisaIncoming element : visaIncoming) {
                    if (stopSignal.get() || Thread.currentThread().isInterrupted()) {
                        throw new InterruptedException("Stop signal received");
                    }

                    Date date = null;
                    try {
                        date = inputDateFormat.get().parse(visaSummary.getSummary_date());
                    } catch (Exception ex) {
                        logger.error("Exception: {}", Throwables.getStackTraceAsString(ex));
                        continue;
                    }

                    String outputDateString = outputDateFormat.get().format(date);
                    String transactionDate = element.getTerminalTransactionDate();
                    String transactionDateOutput = "";

                    if ((transactionDate == null) || ("000000".equals(transactionDate))) {
                        transactionDate = element.getPurchaseDate();
                        int processingDateMonth = Integer.parseInt(outputDateString.substring(2, 4));
                        int processingDateDay = Integer.parseInt(outputDateString.substring(0, 2));
                        int processingDateYear = Integer.parseInt(outputDateString.substring(4));
                        int purchaseDateMonth = Integer.parseInt(transactionDate.substring(0, 2));
                        int purchaseDateDay = Integer.parseInt(transactionDate.substring(2));

                        if (purchaseDateMonth > processingDateMonth) {
                            processingDateYear--;
                        } else if ((purchaseDateMonth == processingDateMonth) && ((purchaseDateDay > processingDateDay))) {
                            processingDateYear--;
                        }

                        transactionDate = processingDateYear + "" + transactionDate;
                    }

                    try {
                        Date transactionDateInput = inputDateFormatVisa.get().parse(transactionDate);
                        transactionDateOutput = outputDateFormatTransactionDate.get().format(transactionDateInput);
                    } catch (ParseException e) {
                        logger.error("Exception: {}", Throwables.getStackTraceAsString(e));
                        continue;
                    }
                    CommissionInternational commossionIntern =commissionInternationalRepository.findByType("Retrait").get(0);
                    BigDecimal commissionTTC = commossionIntern.getCommVariable().multiply(BigDecimal.valueOf(element.getBaseAmount()));
                    commissionTTC=commissionTTC.multiply(BigDecimal.valueOf(getdestinationToBaseExchangeRateInverse(element)));
                    BigDecimal commissionTTCEUR = commossionIntern.getCommVariableDevise().multiply(BigDecimal.valueOf(element.getBaseAmount()));
                    TvaCommissionFransaBank tvaCommissionFransaBank = tvaRepo.findTva();
                    int tva = Integer.parseInt(tvaCommissionFransaBank.getTva());


                    BigDecimal tvaRate = BigDecimal.valueOf(tva).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
                    BigDecimal tvaCommission = commissionTTC.multiply(tvaRate).setScale(2, RoundingMode.HALF_UP);
                    BigDecimal commissionHT = commissionTTC.subtract(tvaCommission).setScale(2, RoundingMode.HALF_UP);
                    // Create operations: G098, G099, G540, G541
                    DayOperationInternational G098 = new DayOperationInternational(findIdentification("G098"),
                            getFullAccountNumber(element), element.getTerminalId(), element.getSourceAmount(),
                            element.getSourceCurrencyCode(), getSourceToBaseExchangeRateInverse(element),
                            element.getBaseCurrency(), String.valueOf(element.getIncomingCode()), new Date(),
                            transactionDateOutput, element.getAuthorisationCode(), element.getAcquirerReferenceNumber(),
                            element.getBaseAmount(), element.getTransactionIdentifier(), outputDateString,
                            element.getSummaryCode(), element.getCardAcceptorId(), element.getTransactionIdentifier(),
                            element.getDe37(), element.getDe11(), element.getMatchingSwitch(),
                            element.getBaseCurrencyToDestinationCurrencyExchangeRate(), element.getDestinationAmount(),
                            element.getMerchantName(),"976","G098", sourceVisa);

                    DayOperationInternational G099 = new DayOperationInternational(findIdentification("G099"),
                            getFullAccountNumber(element), element.getTerminalId(), element.getSourceAmount(),
                            element.getSourceCurrencyCode(), getSourceToBaseExchangeRateInverse(element),
                            element.getBaseCurrency(), String.valueOf(element.getIncomingCode()), new Date(),
                            transactionDateOutput, element.getAuthorisationCode(), element.getAcquirerReferenceNumber(),
                            element.getInterchangeFeeAmount(), element.getTransactionIdentifier(), outputDateString,
                            element.getSummaryCode(), element.getCardAcceptorId(), element.getTransactionIdentifier(),
                            element.getDe37(), element.getDe11(), element.getMatchingSwitch(),
                            element.getBaseCurrencyToDestinationCurrencyExchangeRate(), element.getDestinationAmount(),
                            element.getMerchantName(), "976","G099",sourceVisa);



                    DayOperationInternational G540 = new DayOperationInternational(findIdentification("G540"),
                            getFullAccountNumber(element), element.getTerminalId(), element.getSourceAmount(),
                            element.getSourceCurrencyCode(), getSourceToBaseExchangeRateInverse(element),
                            "788", String.valueOf(element.getIncomingCode()), new Date(),
                            transactionDateOutput, element.getAuthorisationCode(), element.getAcquirerReferenceNumber(),
                            commissionHT.doubleValue(), element.getTransactionIdentifier(), outputDateString,
                            element.getSummaryCode(), element.getCardAcceptorId(), element.getTransactionIdentifier(),
                            element.getDe37(), element.getDe11(), element.getMatchingSwitch(),
                            element.getBaseCurrencyToDestinationCurrencyExchangeRate(), element.getDestinationAmount(),
                            element.getMerchantName(), "208","G540",sourceVisa);

                    DayOperationInternational G541 = new DayOperationInternational(findIdentification("G541"),
                            getFullAccountNumber(element), element.getTerminalId(), element.getSourceAmount(),
                            element.getSourceCurrencyCode(), getSourceToBaseExchangeRateInverse(element),
                            "788", String.valueOf(element.getIncomingCode()), new Date(),
                            transactionDateOutput, element.getAuthorisationCode(), element.getAcquirerReferenceNumber(),
                            tvaCommission.doubleValue(), element.getTransactionIdentifier(), outputDateString,
                            element.getSummaryCode(), element.getCardAcceptorId(), element.getTransactionIdentifier(),
                            element.getDe37(), element.getDe11(), element.getMatchingSwitch(),
                            element.getBaseCurrencyToDestinationCurrencyExchangeRate(), element.getDestinationAmount(),
                            element.getMerchantName(), "208","G541",sourceVisa);
                     DayOperationInternational G542 = new DayOperationInternational(findIdentification("G542"),
                            getFullAccountNumber(element), element.getTerminalId(), element.getSourceAmount(),
                            element.getSourceCurrencyCode(), getSourceToBaseExchangeRateInverse(element),
                            element.getBaseCurrency(), String.valueOf(element.getIncomingCode()), new Date(),
                            transactionDateOutput, element.getAuthorisationCode(), element.getAcquirerReferenceNumber(),
                             commissionTTCEUR.doubleValue(), element.getTransactionIdentifier(), outputDateString,
                            element.getSummaryCode(), element.getCardAcceptorId(), element.getTransactionIdentifier(),
                            element.getDe37(), element.getDe11(), element.getMatchingSwitch(),
                            element.getBaseCurrencyToDestinationCurrencyExchangeRate(), element.getDestinationAmount(),
                            element.getMerchantName(), "976","G542",sourceVisa);
                    setNumRIBPorteurByType(G098, getFullAccountNumber(element), "DEVISE");
                    setNumRIBPorteurByType(G099, getFullAccountNumber(element), "DEVISE");
                    setNumRIBPorteurByType(G540, getFullAccountNumber(element), "DINARS");
                    setNumRIBPorteurByType(G541, getFullAccountNumber(element), "DINARS");
                    setNumRIBPorteurByType(G542, getFullAccountNumber(element), "DEVISE");
                    dayOperations.add(G098);
                    dayOperations.add(G099);
                    dayOperations.add(G540);
                    dayOperations.add(G541);
                    dayOperations.add(G542);

                    element.setTreat("1");
                    updatedVisaIn.add(element);
                }

                dayOperationsL.addAll(dayOperations);
                updatedVisaInL.addAll(updatedVisaIn);
            }

            result = "SUCCESS";

        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("CANCELLED: visaIncomingRetrait", ie);
        } catch (Exception e) {
            logger.error("Error in visaIncomingRetrait: {}", Throwables.getStackTraceAsString(e));
            throw new RuntimeException("ERROR: visaIncomingRetrait", e);
        }

        return CompletableFuture.completedFuture(result);
    }

    @Async("taskExecutor")
    public CompletableFuture<String> refundVisaIssuerBt(AtomicBoolean stopSignal){
        logger.info("begin RefundVisaIssuerBt");
        String methodName = getCurrentMethodName();
        String result = "ERROR";
        try {
            List<VisaSummary> summaries = visaSummaryRepository.findByTreatM();
            if (summaries == null || summaries.isEmpty()) {
                return CompletableFuture.completedFuture("NO_DATA");
            }

            for(VisaSummary visaSummary : summaries){
                if (stopSignal.get() || Thread.currentThread().isInterrupted()) {
                    throw new InterruptedException("Stop signal received");
                }

                List<VisaIncoming> visaIncoming = operationRepo.RefundVisaIssuerBt(visaSummary.getSummaryCode());
                List<DayOperationInternational> dayOperations = new ArrayList<DayOperationInternational>();
                List<VisaIncoming> updatedVisaIn = new ArrayList<VisaIncoming>();

                for (VisaIncoming element : visaIncoming) {
                    if (stopSignal.get() || Thread.currentThread().isInterrupted()) {
                        throw new InterruptedException("Stop signal received");
                    }
                    Date date = null;
                    try {
                        date = inputDateFormat.get().parse(visaSummary.getSummary_date());

                    } catch (Exception ex) {

                        LOG.info(ex.getMessage());
                        String stackTrace = Throwables.getStackTraceAsString(ex);
                        LOG.info("Exception is=>{}", stackTrace);

                    }
                    String outputDateString = outputDateFormat.get().format(date);
                    String transactionDate=element.getTerminalTransactionDate();
                    String transactionDateOutput="";
                    if ((transactionDate==null) || ("000000".equals(transactionDate)))
                    {
                        transactionDate=element.getPurchaseDate();
//						LOG.info("date du fichier"+outputDateString);
//						LOG.info("date de transaction sans annee"+transactionDate);

                        int processingDateMonth=Integer.parseInt (outputDateString.substring(2,4));
                        int processingDateDay=Integer.parseInt( outputDateString.substring(0,2));
                        int processingDateYear=Integer.parseInt(outputDateString.substring(4));

                        int purchaseDateMonth=Integer.parseInt(transactionDate.substring(0,2));
                        int purchaseDateDay=Integer.parseInt(transactionDate.substring(2));
                        if (purchaseDateMonth>processingDateMonth)
                        {
                            processingDateYear--;
                        }
                        else if ((purchaseDateMonth==processingDateMonth) && ((purchaseDateDay>processingDateDay)))
                        {
                            processingDateYear--;
                        }

                        // Calculate the current century

                        String transactionDateResult=processingDateYear+""+transactionDate;
                        transactionDate =transactionDateResult;
                        LOG.info("transaction date � partir de purchase date "+transactionDate);
                    }

                    Date transactionDateInput = null;
                    try {
                        transactionDateInput=inputDateFormatVisa.get().parse(transactionDate);
                        transactionDateOutput=outputDateFormatTransactionDate.get().format(transactionDateInput);

                    } catch (ParseException e) {
                        String stackTrace = Throwables.getStackTraceAsString(e);
                        LOG.info("Exception is=>{}", stackTrace);
                    }
                    LOG.info("visa incoming retrait transaction date "+transactionDateOutput);

                    DayOperationInternational G556 = new DayOperationInternational(findIdentification("G556"),
                            getFullAccountNumber(element), element.getTerminalId(), element.getSourceAmount(),
                            element.getSourceCurrencyCode(), getSourceToBaseExchangeRateInverse(element),
                            element.getBaseCurrency(), String.valueOf(element.getIncomingCode()), new Date(),
                            transactionDateOutput, element.getAuthorisationCode(), element.getAcquirerReferenceNumber(),
                            element.getBaseAmount(), element.getTransactionIdentifier(), outputDateString,
                            element.getSummaryCode(), element.getCardAcceptorId(), element.getTransactionIdentifier(),
                            element.getDe37(),element.getDe11(),element.getMatchingSwitch(),
                            element.getBaseCurrencyToDestinationCurrencyExchangeRate(), element.getDestinationAmount(),
                            element.getMerchantName(),"976","G556",this.sourceVisa);
                    setNumRIBPorteurByType(G556, getFullAccountNumber(element), "DEVISE");
                    if (element.getInterchangeFeeSign().equals("C")) {
                        DayOperationInternational G557 = new DayOperationInternational(findIdentification("G557"),
                                getFullAccountNumber(element), element.getTerminalId(), element.getSourceAmount(),
                                element.getSourceCurrencyCode(), getSourceToBaseExchangeRateInverse(element),
                                element.getBaseCurrency(), String.valueOf(element.getIncomingCode()), new Date(),
                                transactionDateOutput, element.getAuthorisationCode(), element.getAcquirerReferenceNumber(),
                                element.getInterchangeFeeAmount(), element.getTransactionIdentifier(), outputDateString,
                                element.getSummaryCode(), element.getCardAcceptorId(), element.getTransactionIdentifier(),
                                element.getDe37(),element.getDe11(),element.getMatchingSwitch(),
                                element.getBaseCurrencyToDestinationCurrencyExchangeRate(), element.getDestinationAmount(),
                                element.getMerchantName(),"976","G557",this.sourceVisa);
                        setNumRIBPorteurByType(G557, getFullAccountNumber(element), "DEVISE");
                        dayOperations.add(G557);
                        DayOperationInternational G758 = new DayOperationInternational(findIdentification("G758"),
                                getFullAccountNumber(element), element.getTerminalId(), element.getSourceAmount(),
                                element.getSourceCurrencyCode(), getSourceToBaseExchangeRateInverse(element),
                                element.getBaseCurrency(), String.valueOf(element.getIncomingCode()), new Date(),
                                transactionDateOutput, element.getAuthorisationCode(), element.getAcquirerReferenceNumber(),
                                0.0, element.getTransactionIdentifier(), outputDateString,
                                element.getSummaryCode(), element.getCardAcceptorId(), element.getTransactionIdentifier(),
                                element.getDe37(),element.getDe11(),element.getMatchingSwitch(),
                                element.getBaseCurrencyToDestinationCurrencyExchangeRate(), element.getDestinationAmount(),
                                element.getMerchantName(),"976","G758",this.sourceVisa);
                        setNumRIBPorteurByType(G758, getFullAccountNumber(element), "DEVISE");
                        dayOperations.add(G758);

                    }else if (element.getInterchangeFeeSign().equals("D")) {
                        DayOperationInternational G557 = new DayOperationInternational(findIdentification("G557"),
                                getFullAccountNumber(element), element.getTerminalId(), element.getSourceAmount(),
                                element.getSourceCurrencyCode(), getSourceToBaseExchangeRateInverse(element),
                                element.getBaseCurrency(), String.valueOf(element.getIncomingCode()), new Date(),
                                transactionDateOutput, element.getAuthorisationCode(), element.getAcquirerReferenceNumber(),
                                0.0, element.getTransactionIdentifier(), outputDateString,
                                element.getSummaryCode(), element.getCardAcceptorId(), element.getTransactionIdentifier(),
                                element.getDe37(),element.getDe11(),element.getMatchingSwitch(),
                                element.getBaseCurrencyToDestinationCurrencyExchangeRate(),
                                element.getDestinationAmount(), element.getMerchantName(),"976","G557",this.sourceVisa);
                        setNumRIBPorteurByType(G557, getFullAccountNumber(element), "DEVISE");

                        dayOperations.add(G557);
                        DayOperationInternational G758 = new DayOperationInternational(findIdentification("G758"),
                                getFullAccountNumber(element), element.getTerminalId(), element.getSourceAmount(),
                                element.getSourceCurrencyCode(), getSourceToBaseExchangeRateInverse(element),
                                element.getBaseCurrency(), String.valueOf(element.getIncomingCode()), new Date(),
                                transactionDateOutput, element.getAuthorisationCode(), element.getAcquirerReferenceNumber(),
                                element.getInterchangeFeeAmount(), element.getTransactionIdentifier(), outputDateString,
                                element.getSummaryCode(), element.getCardAcceptorId(), element.getTransactionIdentifier(),
                                element.getDe37(),element.getDe11(),element.getMatchingSwitch(),
                                element.getBaseCurrencyToDestinationCurrencyExchangeRate(), element.getDestinationAmount(),
                                element.getMerchantName(),"976","G758",this.sourceVisa);
                        setNumRIBPorteurByType(G758, getFullAccountNumber(element), "DEVISE");

                        dayOperations.add(G758);

                    }


                    dayOperations.add(G556);

                    element.setTreat("1");
                    updatedVisaIn.add(element);
                }

                dayOperationsL.addAll(dayOperations);
                updatedVisaInL.addAll(updatedVisaIn);

                LOG.info("end RefundVisaIssuerBt for summary {} => {}", visaSummary.getSummaryCode(), dayOperations.size());
            }

            result = "SUCCESS";
            logger.info("Completed RefundVisaIssuerBt");

        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            logger.warn("Cancelled RefundVisaIssuerBt");
            throw new RuntimeException("CANCELLED: RefundVisaIssuerBt", ie);
        } catch (Exception e) {
            logger.error("Error in RefundVisaIssuerBt: {}", Throwables.getStackTraceAsString(e));
            throw new RuntimeException("ERROR: RefundVisaIssuerBt", e);
        }

        return CompletableFuture.completedFuture(result);
    }

    @Async("taskExecutor")
    public CompletableFuture<String> refundVisaRetaitIssuerBt(AtomicBoolean stopSignal) {
        logger.info("begin RefundVisaRetaitIssuerBt");
        String methodName = getCurrentMethodName();
        String result = "ERROR";

        try {
            List<VisaSummary> summaries = visaSummaryRepository.findByTreatM();
            if (summaries == null || summaries.isEmpty()) {
                return CompletableFuture.completedFuture("NO_DATA");
            }

            for (VisaSummary visaSummary : summaries) {
                if (stopSignal.get() || Thread.currentThread().isInterrupted()) {
                    throw new InterruptedException("Stop signal received");
                }

                List<VisaIncoming> visaIncoming = operationRepo.RefundVisaRetraitIssuerBt(visaSummary.getSummaryCode());
                List<DayOperationInternational> dayOperations = new ArrayList<DayOperationInternational>();
                List<VisaIncoming> updatedVisaIn = new ArrayList<VisaIncoming>();

                for (VisaIncoming element : visaIncoming) {
                    if (stopSignal.get() || Thread.currentThread().isInterrupted()) {
                        throw new InterruptedException("Stop signal received");
                    }
                    Date date = null;
                    try {
                        date = inputDateFormat.get().parse(visaSummary.getSummary_date());

                    } catch (Exception ex) {

                        LOG.info(ex.getMessage());
                        String stackTrace = Throwables.getStackTraceAsString(ex);
                        LOG.info("Exception is=>{}", stackTrace);

                    }
                    String outputDateString = outputDateFormat.get().format(date);
                    String transactionDate = element.getTerminalTransactionDate();
                    String transactionDateOutput = "";
                    if (transactionDate == null) {
                        transactionDate = element.getPurchaseDate();
//							LOG.info("date du fichier"+outputDateString);
//							LOG.info("date de transaction sans annee"+transactionDate);

                        int processingDateMonth = Integer.parseInt(outputDateString.substring(2, 4));
                        int processingDateDay = Integer.parseInt(outputDateString.substring(0, 2));
                        int processingDateYear = Integer.parseInt(outputDateString.substring(4));

                        int purchaseDateMonth = Integer.parseInt(transactionDate.substring(0, 2));
                        int purchaseDateDay = Integer.parseInt(transactionDate.substring(2));
                        if (purchaseDateMonth > processingDateMonth) {
                            processingDateYear--;
                        } else if ((purchaseDateMonth == processingDateMonth)
                                && ((purchaseDateDay > processingDateDay))) {
                            processingDateYear--;
                        }

                        // Calculate the current century

                        String transactionDateResult = processingDateYear + "" + transactionDate;
                        transactionDate = transactionDateResult;
                        LOG.info("transaction date � partir de purchase date " + transactionDate);
                    }

                    Date transactionDateInput = null;
                    try {
                        transactionDateInput = inputDateFormatVisa.get().parse(transactionDate);
                        transactionDateOutput = outputDateFormatTransactionDate.get().format(transactionDateInput);

                    } catch (ParseException e) {
                        String stackTrace = Throwables.getStackTraceAsString(e);
                        LOG.info("Exception is=>{}", stackTrace);
                    }
                    LOG.info("visa incoming retrait transaction date " + transactionDateOutput);

                    DayOperationInternational G745 = new DayOperationInternational(findIdentification("G745"),
                            getFullAccountNumber(element), element.getTerminalId(), element.getSourceAmount(),
                            element.getSourceCurrencyCode(), getSourceToBaseExchangeRateInverse(element),
                            element.getBaseCurrency(), String.valueOf(element.getIncomingCode()), new Date(),
                            transactionDateOutput, element.getAuthorisationCode(), element.getAcquirerReferenceNumber(),
                            element.getBaseAmount(), element.getTransactionIdentifier(), outputDateString,
                            element.getSummaryCode(), element.getCardAcceptorId(), element.getTransactionIdentifier(),
                            element.getDe37(), element.getDe11(), element.getMatchingSwitch(),
                            element.getBaseCurrencyToDestinationCurrencyExchangeRate(), element.getDestinationAmount(),
                            element.getMerchantName(),"976","G745",sourceVisa);
                    setNumRIBPorteurByType(G745, getFullAccountNumber(element), "DEVISE");
                    if(element.getInterchangeFeeSign().equals("C")){
                        DayOperationInternational G746 = new DayOperationInternational(findIdentification("G746"),
                                getFullAccountNumber(element), element.getTerminalId(), element.getSourceAmount(),
                                element.getSourceCurrencyCode(), getSourceToBaseExchangeRateInverse(element),
                                element.getBaseCurrency(), String.valueOf(element.getIncomingCode()), new Date(),
                                transactionDateOutput, element.getAuthorisationCode(), element.getAcquirerReferenceNumber(),
                                element.getInterchangeFeeAmount(), element.getTransactionIdentifier(), outputDateString,
                                element.getSummaryCode(), element.getCardAcceptorId(), element.getTransactionIdentifier(),
                                element.getDe37(), element.getDe11(), element.getMatchingSwitch(),
                                element.getBaseCurrencyToDestinationCurrencyExchangeRate(), element.getDestinationAmount(),
                                element.getMerchantName(),"976","G746",sourceVisa);
                        setNumRIBPorteurByType(G746, getFullAccountNumber(element), "DEVISE");
                        dayOperations.add(G746);
                        DayOperationInternational G759 = new DayOperationInternational(findIdentification("G759"),
                                getFullAccountNumber(element), element.getTerminalId(), element.getSourceAmount(),
                                element.getSourceCurrencyCode(), getSourceToBaseExchangeRateInverse(element),
                                element.getBaseCurrency(), String.valueOf(element.getIncomingCode()), new Date(),
                                transactionDateOutput, element.getAuthorisationCode(), element.getAcquirerReferenceNumber(),
                                0.0, element.getTransactionIdentifier(), outputDateString,
                                element.getSummaryCode(), element.getCardAcceptorId(), element.getTransactionIdentifier(),
                                element.getDe37(), element.getDe11(), element.getMatchingSwitch(),
                                element.getBaseCurrencyToDestinationCurrencyExchangeRate(), element.getDestinationAmount(),
                                element.getMerchantName(),"976","G759",sourceVisa);
                        setNumRIBPorteurByType(G759, getFullAccountNumber(element), "DEVISE");
                        dayOperations.add(G759);}
                    else if(element.getInterchangeFeeSign().equals("D")){
                        DayOperationInternational G746 = new DayOperationInternational(findIdentification("G746"),
                                getFullAccountNumber(element), element.getTerminalId(), element.getSourceAmount(),
                                element.getSourceCurrencyCode(), getSourceToBaseExchangeRateInverse(element),
                                element.getBaseCurrency(), String.valueOf(element.getIncomingCode()), new Date(),
                                transactionDateOutput, element.getAuthorisationCode(), element.getAcquirerReferenceNumber(),
                                0.0, element.getTransactionIdentifier(), outputDateString,
                                element.getSummaryCode(), element.getCardAcceptorId(), element.getTransactionIdentifier(),
                                element.getDe37(), element.getDe11(), element.getMatchingSwitch(),
                                element.getBaseCurrencyToDestinationCurrencyExchangeRate(), element.getDestinationAmount(),
                                element.getMerchantName(),"976","G746",sourceVisa);
                        setNumRIBPorteurByType(G746, getFullAccountNumber(element), "DEVISE");
                        dayOperations.add(G746);
                        DayOperationInternational G759 = new DayOperationInternational(findIdentification("G759"),
                                getFullAccountNumber(element), element.getTerminalId(), element.getSourceAmount(),
                                element.getSourceCurrencyCode(), getSourceToBaseExchangeRateInverse(element),
                                element.getBaseCurrency(), String.valueOf(element.getIncomingCode()), new Date(),
                                transactionDateOutput, element.getAuthorisationCode(), element.getAcquirerReferenceNumber(),
                                element.getInterchangeFeeAmount(), element.getTransactionIdentifier(), outputDateString,
                                element.getSummaryCode(),element.getCardAcceptorId(), element.getTransactionIdentifier(),
                                element.getDe37(), element.getDe11(), element.getMatchingSwitch(),
                                element.getBaseCurrencyToDestinationCurrencyExchangeRate(), element.getDestinationAmount(),
                                element.getMerchantName(),"976","G759",sourceVisa);
                        setNumRIBPorteurByType(G759, getFullAccountNumber(element), "DEVISE");
                        dayOperations.add(G759);}

                    dayOperations.add(G745);

                    element.setTreat("1");
                    updatedVisaIn.add(element);
                }

                dayOperationsL.addAll(dayOperations);
                updatedVisaInL.addAll(updatedVisaIn);

                LOG.info("end RefundVisaRetaitIssuerBt for summary {} => {}", visaSummary.getSummaryCode(), dayOperations.size());
            }

            result = "SUCCESS";
            logger.info("Completed RefundVisaRetaitIssuerBt");

        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            logger.warn("Cancelled RefundVisaRetaitIssuerBt");
            throw new RuntimeException("CANCELLED: RefundVisaRetaitIssuerBt", ie);
        } catch (Exception e) {
            logger.error("Error in RefundVisaRetaitIssuerBt: {}", Throwables.getStackTraceAsString(e));
            throw new RuntimeException("ERROR: RefundVisaRetaitIssuerBt", e);
        }

        return CompletableFuture.completedFuture(result);
    }
    @Async("taskExecutor")
    public CompletableFuture<String> cashAdvanceVisaIssuerBt(AtomicBoolean stopSignal) {
        logger.info("begin cashAdvanceVisaIssuerBt");
        String methodName = getCurrentMethodName();
        String result = "ERROR";

        try {
            List<VisaSummary> summaries = visaSummaryRepository.findByTreatM();
            if (summaries == null || summaries.isEmpty()) {
                return CompletableFuture.completedFuture("NO_DATA");
            }

            for (VisaSummary visaSummary : summaries) {
                if (stopSignal.get() || Thread.currentThread().isInterrupted()) {
                    throw new InterruptedException("Stop signal received");
                }

                List<VisaIncoming> visaIncoming = operationRepo.cashAdvanceVisaIssuerBt(visaSummary.getSummaryCode());
                List<DayOperationInternational> dayOperations = new ArrayList<DayOperationInternational>();
                List<VisaIncoming> updatedVisaIn = new ArrayList<VisaIncoming>();

                for (VisaIncoming element : visaIncoming) {
                    if (stopSignal.get() || Thread.currentThread().isInterrupted()) {
                        throw new InterruptedException("Stop signal received");
                    }
                    Date date = null;
                    try {
                        date = inputDateFormat.get().parse(visaSummary.getSummary_date());

                    } catch (Exception ex) {

                        LOG.info(ex.getMessage());
                        String stackTrace = Throwables.getStackTraceAsString(ex);
                        LOG.info("Exception is=>{}", stackTrace);

                    }
                    String outputDateString = outputDateFormat.get().format(date);
                    String transactionDate = element.getTerminalTransactionDate();
                    String transactionDateOutput = "";
                    if ((transactionDate == null) ||("000000".equals(transactionDate))) {
                        transactionDate = element.getPurchaseDate();
//							LOG.info("date du fichier"+outputDateString);
//							LOG.info("date de transaction sans annee"+transactionDate);

                        int processingDateMonth = Integer.parseInt(outputDateString.substring(2, 4));
                        int processingDateDay = Integer.parseInt(outputDateString.substring(0, 2));
                        int processingDateYear = Integer.parseInt(outputDateString.substring(4));

                        int purchaseDateMonth = Integer.parseInt(transactionDate.substring(0, 2));
                        int purchaseDateDay = Integer.parseInt(transactionDate.substring(2));
                        if (purchaseDateMonth > processingDateMonth) {
                            processingDateYear--;
                        } else if ((purchaseDateMonth == processingDateMonth)
                                && ((purchaseDateDay > processingDateDay))) {
                            processingDateYear--;
                        }

                        // Calculate the current century

                        String transactionDateResult = processingDateYear + "" + transactionDate;
                        transactionDate = transactionDateResult;
                        LOG.info("transaction date � partir de purchase date " + transactionDate);
                    }

                    Date transactionDateInput = null;
                    try {
                        transactionDateInput = inputDateFormatVisa.get().parse(transactionDate);
                        transactionDateOutput = outputDateFormatTransactionDate.get().format(transactionDateInput);

                    } catch (ParseException e) {
                        String stackTrace = Throwables.getStackTraceAsString(e);
                        LOG.info("Exception is=>{}", stackTrace);
                    }
                    LOG.info("visa incoming retrait transaction date " + transactionDateOutput);

                    DayOperationInternational G718 = new DayOperationInternational(findIdentification("G718"),
                            getFullAccountNumber(element), element.getTerminalId(), element.getSourceAmount(),
                            element.getSourceCurrencyCode(), getSourceToBaseExchangeRateInverse(element),
                            element.getBaseCurrency(), String.valueOf(element.getIncomingCode()), new Date(),
                            transactionDateOutput, element.getAuthorisationCode(), element.getAcquirerReferenceNumber(),
                            element.getBaseAmount(), element.getTransactionIdentifier(), outputDateString,
                            element.getSummaryCode(), element.getCardAcceptorId(), element.getTransactionIdentifier(),
                            element.getDe37(), element.getDe11(), element.getMatchingSwitch(),
                            element.getBaseCurrencyToDestinationCurrencyExchangeRate(), element.getDestinationAmount(),
                            element.getMerchantName(),"976","G718",sourceVisa);
                    DayOperationInternational G719 = new DayOperationInternational(findIdentification("G719"),
                            getFullAccountNumber(element), element.getTerminalId(), element.getSourceAmount(),
                            element.getSourceCurrencyCode(), getSourceToBaseExchangeRateInverse(element),
                            element.getBaseCurrency(), String.valueOf(element.getIncomingCode()), new Date(),
                            transactionDateOutput, element.getAuthorisationCode(), element.getAcquirerReferenceNumber(),
                            element.getInterchangeFeeAmount(), element.getTransactionIdentifier(), outputDateString,
                            element.getSummaryCode(), element.getCardAcceptorId(), element.getTransactionIdentifier(),
                            element.getDe37(), element.getDe11(), element.getMatchingSwitch(),
                            element.getBaseCurrencyToDestinationCurrencyExchangeRate(), element.getDestinationAmount(),
                            element.getMerchantName(),"976","G719",sourceVisa);



                    DayOperationInternational G640 = new DayOperationInternational(findIdentification("G640"),
                            getFullAccountNumber(element), element.getTerminalId(), element.getSourceAmount(),
                            element.getSourceCurrencyCode(), getSourceToBaseExchangeRateInverse(element),
                            element.getBaseCurrency(), String.valueOf(element.getIncomingCode()), new Date(),
                            transactionDateOutput, element.getAuthorisationCode(), element.getAcquirerReferenceNumber(),
                            0, element.getTransactionIdentifier(), outputDateString,
                            element.getSummaryCode(), element.getCardAcceptorId(), element.getTransactionIdentifier(),
                            element.getDe37(), element.getDe11(), element.getMatchingSwitch(),
                            element.getBaseCurrencyToDestinationCurrencyExchangeRate(), element.getDestinationAmount(),
                            element.getMerchantName(),"976","G640",sourceVisa);
                    DayOperationInternational G641 = new DayOperationInternational(findIdentification("G641"),
                            getFullAccountNumber(element), element.getTerminalId(), element.getSourceAmount(),
                            element.getSourceCurrencyCode(), getSourceToBaseExchangeRateInverse(element),
                            element.getBaseCurrency(), String.valueOf(element.getIncomingCode()), new Date(),
                            transactionDateOutput, element.getAuthorisationCode(), element.getAcquirerReferenceNumber(),
                            0, element.getTransactionIdentifier(), outputDateString,
                            element.getSummaryCode(), element.getCardAcceptorId(), element.getTransactionIdentifier(),
                            element.getDe37(), element.getDe11(), element.getMatchingSwitch(),
                            element.getBaseCurrencyToDestinationCurrencyExchangeRate(), element.getDestinationAmount(),
                            element.getMerchantName(),"976","G641",sourceVisa);

                    setNumRIBPorteurByType(G718, getFullAccountNumber(element), "DEVISE");
                    setNumRIBPorteurByType(G719, getFullAccountNumber(element), "DEVISE");
                    setNumRIBPorteurByType(G640, getFullAccountNumber(element), "DEVISE");
                    setNumRIBPorteurByType(G641, getFullAccountNumber(element), "DEVISE");
                    dayOperations.add(G718);
                    dayOperations.add(G719);
                    dayOperations.add(G640);
                    dayOperations.add(G641);

                    element.setTreat("1");
                    updatedVisaIn.add(element);
                }

                dayOperationsL.addAll(dayOperations);
                updatedVisaInL.addAll(updatedVisaIn);

                LOG.info("end cashAdvanceVisaIssuerBt for summary {} => {}", visaSummary.getSummaryCode(), dayOperations.size());
            }

            result = "SUCCESS";
            logger.info("Completed cashAdvanceVisaIssuerBt");

        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            logger.warn("Cancelled cashAdvanceVisaIssuerBt");
            throw new RuntimeException("CANCELLED: cashAdvanceVisaIssuerBt", ie);
        } catch (Exception e) {
            logger.error("Error in cashAdvanceVisaIssuerBt: {}", Throwables.getStackTraceAsString(e));
            throw new RuntimeException("ERROR: cashAdvanceVisaIssuerBt", e);
        }

        return CompletableFuture.completedFuture(result);
    }

    @Async("taskExecutor")
    public CompletableFuture<String> ReversalvisaIncomingRetrait(AtomicBoolean stopSignal) {
        logger.info("begin ReversalvisaIncomingRetrait");
        String methodName = getCurrentMethodName();
        String result = "ERROR";

        try {
            List<VisaSummary> summaries = visaSummaryRepository.findByTreatM();
            if (summaries == null || summaries.isEmpty()) {
                return CompletableFuture.completedFuture("NO_DATA");
            }

            for(VisaSummary visaSummary : summaries){
                if (stopSignal.get() || Thread.currentThread().isInterrupted()) {
                    throw new InterruptedException("Stop signal received");
                }

                List<VisaIncoming> visaIncoming = operationRepo.ResevsalDabVisa(visaSummary.getSummaryCode());
                List<DayOperationInternational> dayOperations = new ArrayList<DayOperationInternational>();
                List<VisaIncoming> updatedVisaIn = new ArrayList<VisaIncoming>();

                for (VisaIncoming element : visaIncoming) {
                    if (stopSignal.get() || Thread.currentThread().isInterrupted()) {
                        throw new InterruptedException("Stop signal received");
                    }
                    Date date = null;
                    try {
//						LOG.info ("date du fichier "+fileDate);
                        date = inputDateFormat.get().parse(visaSummary.getSummary_date());
                    } catch (ParseException ex) {
                        LOG.info(ex.getMessage());
                        String stackTrace = Throwables.getStackTraceAsString(ex);
                        LOG.info("Exception is=>{}", stackTrace);
                    }
                    String outputDateString = outputDateFormat.get().format(date);
                    String transactionDate=element.getTerminalTransactionDate();
                    String transactionDateOutput="";
                    if ((transactionDate==null)||("000000".equals(transactionDate)))
                    {
                        transactionDate=element.getPurchaseDate();
//						LOG.info("date du fichier achat incoming visa"+outputDateString);
//						LOG.info("date de transaction sans annee"+transactionDate);

                        int processingDateMonth=Integer.parseInt (outputDateString.substring(2,4));
                        int processingDateDay=Integer.parseInt( outputDateString.substring(0,2));
                        int processingDateYear=Integer.parseInt(outputDateString.substring(4));

                        int purchaseDateMonth=Integer.parseInt(transactionDate.substring(0,2));
                        int purchaseDateDay=Integer.parseInt(transactionDate.substring(2));
                        if (purchaseDateMonth>processingDateMonth)
                        {
                            processingDateYear--;
                        }
                        else if ((purchaseDateMonth==processingDateMonth) && ((purchaseDateDay>processingDateDay)))
                        {
                            processingDateYear--;
                        }

                        // Calculate the current century

                        String transactionDateResult=processingDateYear+""+transactionDate;
                        transactionDate =transactionDateResult;
                        LOG.info("transaction date � partir de purchase date "+transactionDate);
                    }


                    Date transactionDateInput = null;
                    try {
                        transactionDateInput=inputDateFormatVisa.get().parse(transactionDate);
                        transactionDateOutput=outputDateFormatTransactionDate.get().format(transactionDateInput);

                    } catch (ParseException e) {
                        String stackTrace = Throwables.getStackTraceAsString(e);
                        LOG.info("Exception is=>{}", stackTrace);
                    }
                    LOG.info("transaction date visa incoming achat "+transactionDateOutput);

                    DayOperationInternational G560 = new DayOperationInternational(findIdentification("G560"),
                            getFullAccountNumber(element), element.getTerminalId(), element.getSourceAmount(),
                            element.getSourceCurrencyCode(), getSourceToBaseExchangeRateInverse(element),
                            element.getBaseCurrency(), String.valueOf(element.getIncomingCode()), new Date(),
                            transactionDateOutput, element.getAuthorisationCode(), element.getAcquirerReferenceNumber(),
                            element.getBaseAmount(), element.getTransactionIdentifier(), outputDateString,
                            element.getSummaryCode(), element.getCardAcceptorId(), element.getTransactionIdentifier(),
                            element.getDe37(),element.getDe11(),element.getMatchingSwitch(),
                            element.getBaseCurrencyToDestinationCurrencyExchangeRate(), element.getDestinationAmount(),
                            element.getMerchantName(),"976","G560",sourceVisa);
                    DayOperationInternational G561 = new DayOperationInternational(findIdentification("G561"),
                            getFullAccountNumber(element), element.getTerminalId(), element.getSourceAmount(),
                            element.getSourceCurrencyCode(), getSourceToBaseExchangeRateInverse(element),
                            element.getBaseCurrency(), String.valueOf(element.getIncomingCode()), new Date(),
                            transactionDateOutput, element.getAuthorisationCode(), element.getAcquirerReferenceNumber(),
                            element.getInterchangeFeeAmount(), element.getTransactionIdentifier(), outputDateString,
                            element.getSummaryCode(), element.getCardAcceptorId(), element.getTransactionIdentifier(),
                            element.getDe37(),element.getDe11(),element.getMatchingSwitch(),
                            element.getBaseCurrencyToDestinationCurrencyExchangeRate(), element.getDestinationAmount(),
                            element.getMerchantName(),"976","G561",sourceVisa);
//					setCompteDebitCreditVisaIncoming(G560, element, "G560");
//					setCompteDebitCreditVisaIncoming(G561, element, "G561");
                    setNumRIBPorteurByType(G560, getFullAccountNumber(element), "DEVISE");
                    setNumRIBPorteurByType(G561, getFullAccountNumber(element), "DEVISE");
                    dayOperations.add(G560);
                    dayOperations.add(G561);
                    element.setTreat("1");
                    updatedVisaIn.add(element);
                }

                dayOperationsL.addAll(dayOperations);
                updatedVisaInL.addAll(updatedVisaIn);

                LOG.info("end ReversalvisaIncomingRetrait for summary {} => {}", visaSummary.getSummaryCode(), dayOperations.size());
            }

            result = "SUCCESS";
            logger.info("Completed ReversalvisaIncomingRetrait");

        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            logger.warn("Cancelled ReversalvisaIncomingRetrait");
            throw new RuntimeException("CANCELLED: ReversalvisaIncomingRetrait", ie);
        } catch (Exception e) {
            logger.error("Error in ReversalvisaIncomingRetrait: {}", Throwables.getStackTraceAsString(e));
            throw new RuntimeException("ERROR: ReversalvisaIncomingRetrait", e);
        }

        return CompletableFuture.completedFuture(result);
    }

    @Async("taskExecutor")
    public CompletableFuture<String> ReversalvisaIncomingAchat(AtomicBoolean stopSignal) {
        logger.info("begin ReversalvisaIncomingAchat");
        String methodName = getCurrentMethodName();
        String result = "ERROR";

        try {
            List<VisaSummary> summaries = visaSummaryRepository.findByTreatM();
            if (summaries == null || summaries.isEmpty()) {
                return CompletableFuture.completedFuture("NO_DATA");
            }

            for(VisaSummary visaSummary : summaries){
                if (stopSignal.get() || Thread.currentThread().isInterrupted()) {
                    throw new InterruptedException("Stop signal received");
                }

                List<VisaIncoming> visaIncoming = operationRepo.ResevsalPOSVisa(visaSummary.getSummaryCode());
                List<DayOperationInternational> dayOperations = new ArrayList<DayOperationInternational>();
                List<VisaIncoming> updatedVisaIn = new ArrayList<VisaIncoming>();

                for (VisaIncoming element : visaIncoming) {
                    if (stopSignal.get() || Thread.currentThread().isInterrupted()) {
                        throw new InterruptedException("Stop signal received");
                    }

                    Date date = null;
                    try {
                        //LOG.info ("date du fichier "+fileDate);
                        date = inputDateFormat.get().parse(visaSummary.getSummary_date());
                    } catch (ParseException ex) {
                        LOG.info(ex.getMessage());
                        String stackTrace = Throwables.getStackTraceAsString(ex);
                        LOG.info("Exception is=>{}", stackTrace);
                    }
                    String outputDateString = outputDateFormat.get().format(date);
                    String transactionDate=element.getTerminalTransactionDate();
                    String transactionDateOutput="";
                    if ((transactionDate==null) ||("000000".equals(transactionDate)))
                    {
                        transactionDate=element.getPurchaseDate();
                        //	LOG.info("date du fichier achat incoming visa"+outputDateString);
                        //	LOG.info("date de transaction sans annee"+transactionDate);

                        int processingDateMonth=Integer.parseInt (outputDateString.substring(2,4));
                        int processingDateDay=Integer.parseInt( outputDateString.substring(0,2));
                        int processingDateYear=Integer.parseInt(outputDateString.substring(4));

                        int purchaseDateMonth=Integer.parseInt(transactionDate.substring(0,2));
                        int purchaseDateDay=Integer.parseInt(transactionDate.substring(2));
                        if (purchaseDateMonth>processingDateMonth)
                        {
                            processingDateYear--;
                        }
                        else if ((purchaseDateMonth==processingDateMonth) && ((purchaseDateDay>processingDateDay)))
                        {
                            processingDateYear--;
                        }

                        // Calculate the current century

                        String transactionDateResult=processingDateYear+""+transactionDate;
                        transactionDate =transactionDateResult;
                        LOG.info("transaction date � partir de purchase date "+transactionDate);
                    }


                    Date transactionDateInput = null;
                    try {
                        transactionDateInput=inputDateFormatVisa.get().parse(transactionDate);
                        transactionDateOutput=outputDateFormatTransactionDate.get().format(transactionDateInput);

                    } catch (ParseException e) {
                        String stackTrace = Throwables.getStackTraceAsString(e);
                        LOG.info("Exception is=>{}", stackTrace);
                    }
                    LOG.info("transaction date visa incoming achat "+transactionDateOutput);

                    DayOperationInternational G558 = new DayOperationInternational(findIdentification("G558"),
                            getFullAccountNumber(element), element.getTerminalId(), element.getSourceAmount(),
                            element.getSourceCurrencyCode(), getSourceToBaseExchangeRateInverse(element),
                            element.getBaseCurrency(), String.valueOf(element.getIncomingCode()), new Date(),
                            transactionDateOutput, element.getAuthorisationCode(), element.getAcquirerReferenceNumber(),
                            element.getBaseAmount(), element.getTransactionIdentifier(), outputDateString,
                            element.getSummaryCode(), element.getCardAcceptorId(), element.getTransactionIdentifier(),
                            element.getDe37(),element.getDe11(),element.getMatchingSwitch(),
                            element.getBaseCurrencyToDestinationCurrencyExchangeRate(), element.getDestinationAmount(),
                            element.getMerchantName(),"976","G558",sourceVisa);
                    DayOperationInternational G559 = new DayOperationInternational(findIdentification("G559"),
                            getFullAccountNumber(element), element.getTerminalId(), element.getSourceAmount(),
                            element.getSourceCurrencyCode(), getSourceToBaseExchangeRateInverse(element),
                            element.getBaseCurrency(), String.valueOf(element.getIncomingCode()), new Date(),
                            transactionDateOutput, element.getAuthorisationCode(), element.getAcquirerReferenceNumber(),
                            element.getInterchangeFeeAmount(), element.getTransactionIdentifier(), outputDateString,
                            element.getSummaryCode(), element.getCardAcceptorId(), element.getTransactionIdentifier(),
                            element.getDe37(),element.getDe11(),element.getMatchingSwitch(),
                            element.getBaseCurrencyToDestinationCurrencyExchangeRate(), element.getDestinationAmount(),
                            element.getMerchantName(),"976","G559",sourceVisa);
//				setCompteDebitCreditVisaIncoming(G558, element, "G558");
//				setCompteDebitCreditVisaIncoming(G559, element, "G559");
                    setNumRIBPorteurByType(G558, getFullAccountNumber(element), "DEVISE");
                    setNumRIBPorteurByType(G559, getFullAccountNumber(element), "DEVISE");
                    dayOperations.add(G558);
                    dayOperations.add(G559);
                    element.setTreat("1");
                    updatedVisaIn.add(element);
                }

                dayOperationsL.addAll(dayOperations);
                updatedVisaInL.addAll(updatedVisaIn);

                LOG.info("end ReversalvisaIncomingAchat for summary {} => {}", visaSummary.getSummaryCode(), dayOperations.size());
            }

            result = "SUCCESS";
            logger.info("Completed ReversalvisaIncomingAchat");

        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            logger.warn("Cancelled ReversalvisaIncomingAchat");
            throw new RuntimeException("CANCELLED: ReversalvisaIncomingAchat", ie);
        } catch (Exception e) {
            logger.error("Error in ReversalvisaIncomingAchat: {}", Throwables.getStackTraceAsString(e));
            throw new RuntimeException("ERROR: ReversalvisaIncomingAchat", e);
        }

        return CompletableFuture.completedFuture(result);
    }

    @Async("taskExecutor")
    public CompletableFuture<String> chargeBackVISAEmis(AtomicBoolean stopSignal) {
        logger.info("begin chargeBackVISAEmis");
        String methodName = getCurrentMethodName();
        String result = "ERROR";

        try {
            List<VisaSummary> summaries = visaSummaryRepository.findByTreatM();
            if (summaries == null || summaries.isEmpty()) {
                return CompletableFuture.completedFuture("NO_DATA");
            }

            for (VisaSummary visaSummary : summaries) {
                if (stopSignal.get() || Thread.currentThread().isInterrupted()) {
                    throw new InterruptedException("Stop signal received");
                }

                List<VisaDisputeStatusAdvice> VisaDispute = operationRepo.VisaDisputeChEmis(visaSummary.getSummaryCode());
                List<DayOperationInternational> dayOperations = new ArrayList<DayOperationInternational>();
                List<VisaDisputeStatusAdvice> updatedVisaOut = new ArrayList<VisaDisputeStatusAdvice>();

                for (VisaDisputeStatusAdvice e : VisaDispute) {
                    if (stopSignal.get() || Thread.currentThread().isInterrupted()) {
                        throw new InterruptedException("Stop signal received");
                    }
                    Date date = null;
                    try {
                        date = inputDateFormat.get().parse(visaSummary.getSummary_date());
                    } catch (ParseException ex) {
                        LOG.info(ex.getMessage());
                        String stackTrace = Throwables.getStackTraceAsString(ex);
                        LOG.info("Exception is=>{}", stackTrace);

                    }
                    String outputDateString = outputDateFormat.get().format(date);

                    String anneeTransaction = outputDateString.substring(4);
                    String transactionDate = anneeTransaction + e.getPurchaseDate();
                    try {
                        date = inputDateFormatVisa.get().parse(transactionDate);
                    } catch (ParseException e1) {
                        String stackTrace = Throwables.getStackTraceAsString(e1);
                        LOG.info("Exception is=>{}", stackTrace);
                    }
                    transactionDate = outputDateFormatTransactionDate.get().format(date);
                    if(!"P".equals(e.getSpecialChargebackIndicator())){
                        if(!"6011".equals(e.getMerchantCategoryCode()) && !"6010".equals(e.getMerchantCategoryCode()))  {
                            DayOperationInternational G698 = new DayOperationInternational(findIdentification("G698"),
                                    getFullAccountNumber(e), e.getCardAcceptorId(), e.getSourceAmount(),
                                    e.getSourceCurrencyCode(), e.getConversionRate(),
                                    e.getSourceCurrencyCode(), String.valueOf(e.getIncomingCode()), new Date(),
                                    transactionDate, e.getAuthorizationCode(), e.getAcquirerReferenceNumber(),
                                    e.getSettlementCalculate(), e.getTransactionIdentifier(), outputDateString,
                                    e.getSummaryCode(), e.getCardAcceptorId(), e.getTransactionIdentifier(),
                                    null,null
                                    ,null,
                                    e.getConversionRateTnd(), e.getDestinationAmount(),
                                    e.getMerchantName(),"976","G698",sourceVisa);
                            DayOperationInternational G699 = new DayOperationInternational(findIdentification("G699"),
                                    getFullAccountNumber(e), e.getCardAcceptorId(), e.getSourceAmount(),
                                    e.getSourceCurrencyCode(), e.getConversionRate(),
                                    e.getSourceCurrencyCode(), String.valueOf(e.getIncomingCode()), new Date(),
                                    transactionDate, e.getAuthorizationCode(), e.getAcquirerReferenceNumber(),
                                    e.getInterchangeCalculat(), e.getTransactionIdentifier(), outputDateString,
                                    e.getSummaryCode(), e.getCardAcceptorId(), e.getTransactionIdentifier(),
                                    null,null
                                    ,null,
                                    e.getConversionRateTnd(), e.getDestinationAmount(),
                                    e.getMerchantName(),"976","G699",sourceVisa);
                           setNumRIBPorteurByType(G698, getFullAccountNumber(e), "DEVISE");
                           setNumRIBPorteurByType(G699, getFullAccountNumber(e), "DEVISE");
                           dayOperations.add(G698);
                            dayOperations.add(G699);
                        }else {
                            DayOperationInternational G731 = new DayOperationInternational(findIdentification("G731"),
                                    getFullAccountNumber(e), e.getCardAcceptorId(), e.getSourceAmount(),
                                    e.getSourceCurrencyCode(), e.getConversionRate(),
                                    e.getSourceCurrencyCode(), String.valueOf(e.getIncomingCode()), new Date(),
                                    transactionDate, e.getAuthorizationCode(), e.getAcquirerReferenceNumber(),
                                    e.getSettlementCalculate(), e.getTransactionIdentifier(), outputDateString,
                                    e.getSummaryCode(), e.getCardAcceptorId(), e.getTransactionIdentifier(),
                                    null,null
                                    ,null,
                                    e.getConversionRateTnd(), e.getDestinationAmount(),
                                    e.getMerchantName(),"976","G731",sourceVisa);
                            DayOperationInternational G732 = new DayOperationInternational(findIdentification("G732"),
                                    getFullAccountNumber(e), e.getCardAcceptorId(), e.getSourceAmount(),
                                    e.getSourceCurrencyCode(), e.getConversionRate(),
                                    e.getSourceCurrencyCode(), String.valueOf(e.getIncomingCode()), new Date(),
                                    transactionDate, e.getAuthorizationCode(), e.getAcquirerReferenceNumber(),
                                    e.getInterchangeCalculat(), e.getTransactionIdentifier(), outputDateString,
                                    e.getSummaryCode(), e.getCardAcceptorId(), e.getTransactionIdentifier(),
                                    null,null
                                    ,null,
                                    e.getConversionRateTnd(), e.getDestinationAmount(),
                                    e.getMerchantName(),"976","G732",sourceVisa);
                            setNumRIBPorteurByType(G731, getFullAccountNumber(e), "DEVISE");
                            setNumRIBPorteurByType(G732, getFullAccountNumber(e), "DEVISE");
                            dayOperations.add(G731);
                            dayOperations.add(G732);

                        }}else{
                        if(!"6011".equals(e.getMerchantCategoryCode()) && !"6010".equals(e.getMerchantCategoryCode()))  {
                            DayOperationInternational G926 =  new DayOperationInternational(findIdentification("G926"),
                                    getFullAccountNumber(e), e.getCardAcceptorId(), e.getSourceAmount(),
                                    e.getSourceCurrencyCode(), e.getConversionRate(),
                                    e.getSourceCurrencyCode(), String.valueOf(e.getIncomingCode()), new Date(),
                                    transactionDate, e.getAuthorizationCode(), e.getAcquirerReferenceNumber(),
                                    e.getSettlementCalculate(), e.getTransactionIdentifier(), outputDateString,
                                    e.getSummaryCode(), e.getCardAcceptorId(), e.getTransactionIdentifier(),
                                    null,null
                                    ,null,
                                    e.getConversionRateTnd(), e.getDestinationAmount(),
                                    e.getMerchantName(),"976","G926",sourceVisa);
                           DayOperationInternational G927 =  new DayOperationInternational(findIdentification("G927"),
                                    getFullAccountNumber(e), e.getCardAcceptorId(), e.getSourceAmount(),
                                    e.getSourceCurrencyCode(), e.getConversionRate(),
                                    e.getSourceCurrencyCode(), String.valueOf(e.getIncomingCode()), new Date(),
                                    transactionDate, e.getAuthorizationCode(), e.getAcquirerReferenceNumber(),
                                    e.getInterchangeCalculat(), e.getTransactionIdentifier(), outputDateString,
                                    e.getSummaryCode(), e.getCardAcceptorId(), e.getTransactionIdentifier(),
                                    null,null
                                    ,null,
                                    e.getConversionRateTnd(), e.getDestinationAmount(),
                                    e.getMerchantName(),"976","G927",sourceVisa);
                            setNumRIBPorteurByType(G926, getFullAccountNumber(e), "DEVISE");
                            setNumRIBPorteurByType(G927, getFullAccountNumber(e), "DEVISE");
                            dayOperations.add(G926);
                            dayOperations.add(G927);
                        }else {
                            DayOperationInternational G928 = new DayOperationInternational(findIdentification("G928"),
                                    getFullAccountNumber(e), e.getCardAcceptorId(), e.getSourceAmount(),
                                    e.getSourceCurrencyCode(), e.getConversionRate(),
                                    e.getSourceCurrencyCode(), String.valueOf(e.getIncomingCode()), new Date(),
                                    transactionDate, e.getAuthorizationCode(), e.getAcquirerReferenceNumber(),
                                    e.getSettlementCalculate(), e.getTransactionIdentifier(), outputDateString,
                                    e.getSummaryCode(), e.getCardAcceptorId(), e.getTransactionIdentifier(),
                                    null,null
                                    ,null,
                                    e.getConversionRateTnd(), e.getDestinationAmount(),
                                    e.getMerchantName(),"976","G928",sourceVisa);
                            DayOperationInternational G929 = new DayOperationInternational(findIdentification("G929"),
                                    getFullAccountNumber(e), e.getCardAcceptorId(), e.getSourceAmount(),
                                    e.getSourceCurrencyCode(), e.getConversionRate(),
                                    e.getSourceCurrencyCode(), String.valueOf(e.getIncomingCode()), new Date(),
                                    transactionDate, e.getAuthorizationCode(), e.getAcquirerReferenceNumber(),
                                    e.getInterchangeCalculat(), e.getTransactionIdentifier(), outputDateString,
                                    e.getSummaryCode(), e.getCardAcceptorId(), e.getTransactionIdentifier(),
                                    null,null
                                    ,null,
                                    e.getConversionRateTnd(), e.getDestinationAmount(),
                                    e.getMerchantName(),"976","G929",sourceVisa);
                            setNumRIBPorteurByType(G928, getFullAccountNumber(e), "DEVISE");
                            setNumRIBPorteurByType(G929, getFullAccountNumber(e), "DEVISE");
                            dayOperations.add(G928);
                            dayOperations.add(G929);

                        }
                    }


                }

                dayOperationsChargebackL.addAll(dayOperations);

                LOG.info("end chargeBackVISAEmis for summary {} => {}", visaSummary.getSummaryCode(), dayOperations.size());
            }

            result = "SUCCESS";
            logger.info("Completed chargeBackVISAEmis");

        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            logger.warn("Cancelled chargeBackVISAEmis");
            throw new RuntimeException("CANCELLED: chargeBackVISAEmis", ie);
        } catch (Exception e) {
            logger.error("Error in chargeBackVISAEmis: {}", Throwables.getStackTraceAsString(e));
            throw new RuntimeException("ERROR: chargeBackVISAEmis", e);
        }

        return CompletableFuture.completedFuture(result);
    }
    @Async("taskExecutor")
    public CompletableFuture<String> RepresentationVISARecue(AtomicBoolean stopSignal) {
        logger.info("begin RepresentationVISARecue");
        String methodName = getCurrentMethodName();
        String result = "ERROR";

        try {
            List<VisaSummary> summaries = visaSummaryRepository.findByTreatM();
            if (summaries == null || summaries.isEmpty()) {
                return CompletableFuture.completedFuture("NO_DATA");
            }

            for (VisaSummary visaSummary : summaries) {
                if (stopSignal.get() || Thread.currentThread().isInterrupted()) {
                    throw new InterruptedException("Stop signal received");
                }

                List<VisaDisputeStatusAdvice> VisaDispute = operationRepo.VisaDisputeRepRecue(visaSummary.getSummaryCode());
                List<DayOperationInternational> dayOperations = new ArrayList<DayOperationInternational>();
                List<VisaDisputeStatusAdvice> updatedVisaOut = new ArrayList<VisaDisputeStatusAdvice>();

                for (VisaDisputeStatusAdvice e : VisaDispute) {
                    if (stopSignal.get() || Thread.currentThread().isInterrupted()) {
                        throw new InterruptedException("Stop signal received");
                    }
                    Date date = null;
                    try {
                        date = inputDateFormat.get().parse(visaSummary.getSummary_date());
                    } catch (ParseException ex) {
                        LOG.info(ex.getMessage());
                        String stackTrace = Throwables.getStackTraceAsString(ex);
                        LOG.info("Exception is=>{}", stackTrace);

                    }
                    String outputDateString = outputDateFormat.get().format(date);

                    String anneeTransaction = outputDateString.substring(4);
                    String transactionDate = anneeTransaction + e.getPurchaseDate();
                    try {
                        date = inputDateFormatVisa.get().parse(transactionDate);
                    } catch (ParseException e1) {
                        String stackTrace = Throwables.getStackTraceAsString(e1);
                        LOG.info("Exception is=>{}", stackTrace);
                    }
                    transactionDate = outputDateFormatTransactionDate.get().format(date);
                    double commission=0.0;
                    double tva=0.0;
                    if(!"6011".equals(e.getMerchantCategoryCode()) && !"6010".equals(e.getMerchantCategoryCode())) {

                        DayOperationInternational G869 = new DayOperationInternational(findIdentification("G869"),
                                getFullAccountNumber(e), e.getCardAcceptorId(), e.getSourceAmount(),
                                e.getSourceCurrencyCode(), e.getConversionRate(),
                                e.getSourceCurrencyCode(), String.valueOf(e.getIncomingCode()), new Date(),
                                transactionDate, e.getAuthorizationCode(), e.getAcquirerReferenceNumber(),
                                e.getSettlementCalculate(), e.getTransactionIdentifier(), outputDateString,
                                e.getSummaryCode(), e.getCardAcceptorId(), e.getTransactionIdentifier(),
                                null,null
                                ,null,
                                e.getConversionRateTnd(), e.getDestinationAmount(),
                                e.getMerchantName(),"976","G869",sourceVisa);
                        DayOperationInternational G870 = new DayOperationInternational(findIdentification("G870"),
                                getFullAccountNumber(e), e.getCardAcceptorId(), e.getSourceAmount(),
                                e.getSourceCurrencyCode(), e.getConversionRate(),
                                e.getSourceCurrencyCode(), String.valueOf(e.getIncomingCode()), new Date(),
                                transactionDate, e.getAuthorizationCode(), e.getAcquirerReferenceNumber(),
                                e.getInterchangeCalculat(), e.getTransactionIdentifier(), outputDateString,
                                e.getSummaryCode(), e.getCardAcceptorId(), e.getTransactionIdentifier(),
                                null,null
                                ,null,
                                e.getConversionRateTnd(), e.getDestinationAmount(),
                                e.getMerchantName(),"976","G870",sourceVisa);
                        setNumRIBPorteurByType(G869, getFullAccountNumber(e), "DEVISE");
                        setNumRIBPorteurByType(G870, getFullAccountNumber(e), "DEVISE");
                        dayOperations.add(G869);
                        dayOperations.add(G870);

                    }else{
                        DayOperationInternational G1028 = new DayOperationInternational(findIdentification("G1028"),
                                getFullAccountNumber(e), e.getCardAcceptorId(), e.getSourceAmount(),
                                e.getSourceCurrencyCode(), e.getConversionRate(),
                                e.getSourceCurrencyCode(), String.valueOf(e.getIncomingCode()), new Date(),
                                transactionDate, e.getAuthorizationCode(), e.getAcquirerReferenceNumber(),
                                e.getSettlementCalculate(), e.getTransactionIdentifier(), outputDateString,
                                e.getSummaryCode(), e.getCardAcceptorId(), e.getTransactionIdentifier(),
                                null,null
                                ,null,
                                e.getConversionRateTnd(), e.getDestinationAmount(),
                                e.getMerchantName(),"976","G1028",sourceVisa);
                        DayOperationInternational G1029 = new DayOperationInternational(findIdentification("G1029"),
                                getFullAccountNumber(e), e.getCardAcceptorId(), e.getSourceAmount(),
                                e.getSourceCurrencyCode(), e.getConversionRate(),
                                e.getSourceCurrencyCode(), String.valueOf(e.getIncomingCode()), new Date(),
                                transactionDate, e.getAuthorizationCode(), e.getAcquirerReferenceNumber(),
                                e.getInterchangeCalculat(), e.getTransactionIdentifier(), outputDateString,
                                e.getSummaryCode(), e.getCardAcceptorId(), e.getTransactionIdentifier(),
                                null,null
                                ,null,
                                e.getConversionRateTnd(), e.getDestinationAmount(),
                                e.getMerchantName(),"976","G1029",sourceVisa);
                        setNumRIBPorteurByType(G1028, getFullAccountNumber(e), "DEVISE");
                        setNumRIBPorteurByType(G1029, getFullAccountNumber(e), "DEVISE");
                        dayOperations.add(G1028);
                        dayOperations.add(G1029);
                    }


                }

                dayOperationsChargebackL.addAll(dayOperations);

                LOG.info("end RepresentationVISARecue for summary {} => {}", visaSummary.getSummaryCode(), dayOperations.size());
            }

            result = "SUCCESS";
            logger.info("Completed RepresentationVISARecue");

        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            logger.warn("Cancelled RepresentationVISARecue");
            throw new RuntimeException("CANCELLED: RepresentationVISARecue", ie);
        } catch (Exception e) {
            logger.error("Error in RepresentationVISARecue: {}", Throwables.getStackTraceAsString(e));
            throw new RuntimeException("ERROR: RepresentationVISARecue", e);
        }

        return CompletableFuture.completedFuture(result);
    }
    @Async("taskExecutor")
    public CompletableFuture<String> RepresentationVISAIncomingRecueAchat(AtomicBoolean stopSignal) {
        logger.info("begin RepresentationVISAIncomingRecueAchat");
        String methodName = getCurrentMethodName();
        String result = "ERROR";

        try {
            List<VisaSummary> summaries = visaSummaryRepository.findByTreatM();
            if (summaries == null || summaries.isEmpty()) {
                return CompletableFuture.completedFuture("NO_DATA");
            }

            for (VisaSummary visaSummary : summaries) {
                if (stopSignal.get() || Thread.currentThread().isInterrupted()) {
                    throw new InterruptedException("Stop signal received");
                }

                List<VisaIncoming> VisaDispute = operationRepo.VisaIncomingPOSDISP(visaSummary.getSummaryCode());
                List<DayOperationInternational> dayOperations = new ArrayList<DayOperationInternational>();
                List<VisaDisputeStatusAdvice> updatedVisaOut = new ArrayList<VisaDisputeStatusAdvice>();

                for (VisaIncoming e : VisaDispute) {
                    if (stopSignal.get() || Thread.currentThread().isInterrupted()) {
                        throw new InterruptedException("Stop signal received");
                    }
                    Date date = null;
                    try {
                        date = inputDateFormat.get().parse(visaSummary.getSummary_date());
                    } catch (ParseException ex) {
                        LOG.info(ex.getMessage());
                        String stackTrace = Throwables.getStackTraceAsString(ex);
                        LOG.info("Exception is=>{}", stackTrace);

                    }
                    String outputDateString = outputDateFormat.get().format(date);

                    String anneeTransaction = outputDateString.substring(4);
                    String transactionDate = anneeTransaction + e.getPurchaseDate();
                    try {
                        date = inputDateFormatVisa.get().parse(transactionDate);
                    } catch (ParseException e1) {
                        String stackTrace = Throwables.getStackTraceAsString(e1);
                        LOG.info("Exception is=>{}", stackTrace);
                    }
                    transactionDate = outputDateFormatTransactionDate.get().format(date);
                    double commission = 0.0;
                    double tva = 0.0;
                    double taux= e.getBaseCurrencyToDestinationCurrencyExchangeRate();
                    if (taux==0)
                    {
                        taux=1;

                    }
                    else {
                        taux=convertAmount(1/e.getBaseCurrencyToDestinationCurrencyExchangeRate(),3);

                    }
                    DayOperationInternational G869 = new DayOperationInternational(findIdentification("G869"),
                            getFullAccountNumber(e), e.getTerminalId(), e.getSourceAmount(),
                            e.getSourceCurrencyCode(), getSourceToBaseExchangeRateInverse(e),
                            e.getBaseCurrency(), String.valueOf(e.getIncomingCode()), new Date(),
                            transactionDate, e.getAuthorisationCode(), e.getAcquirerReferenceNumber(),
                            e.getInterchangeFeeAmount(), e.getTransactionIdentifier(), outputDateString,
                            e.getSummaryCode(), e.getCardAcceptorId(), e.getTransactionIdentifier(),
                            e.getDe37(),e.getDe11(),e.getMatchingSwitch(),
                            e.getBaseCurrencyToDestinationCurrencyExchangeRate(), e.getDestinationAmount(),
                            e.getMerchantName(),"976","G869",sourceVisa);
                    DayOperationInternational G870 =new DayOperationInternational(findIdentification("G870"),
                            getFullAccountNumber(e), e.getTerminalId(), e.getSourceAmount(),
                            e.getSourceCurrencyCode(), getSourceToBaseExchangeRateInverse(e),
                            e.getBaseCurrency(), String.valueOf(e.getIncomingCode()), new Date(),
                            transactionDate, e.getAuthorisationCode(), e.getAcquirerReferenceNumber(),
                            e.getInterchangeFeeAmount(), e.getTransactionIdentifier(), outputDateString,
                            e.getSummaryCode(), e.getCardAcceptorId(), e.getTransactionIdentifier(),
                            e.getDe37(),e.getDe11(),e.getMatchingSwitch(),
                            e.getBaseCurrencyToDestinationCurrencyExchangeRate(), e.getDestinationAmount(),
                            e.getMerchantName(),"976","G870",sourceVisa);
                    setNumRIBPorteurByType(G869, getFullAccountNumber(e), "DEVISE");
                    setNumRIBPorteurByType(G870, getFullAccountNumber(e), "DEVISE");
                    dayOperations.add(G869);
                    dayOperations.add(G870);
                }

                dayOperationsChargebackL.addAll(dayOperations);

                LOG.info("end RepresentationVISAIncomingRecueAchat for summary {} => {}", visaSummary.getSummaryCode(), dayOperations.size());
            }

            result = "SUCCESS";
            logger.info("Completed RepresentationVISAIncomingRecueAchat");

        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            logger.warn("Cancelled RepresentationVISAIncomingRecueAchat");
            throw new RuntimeException("CANCELLED: RepresentationVISAIncomingRecueAchat", ie);
        } catch (Exception e) {
            logger.error("Error in RepresentationVISAIncomingRecueAchat: {}", Throwables.getStackTraceAsString(e));
            throw new RuntimeException("ERROR: RepresentationVISAIncomingRecueAchat", e);
        }

        return CompletableFuture.completedFuture(result);
    }
    @Async("taskExecutor")
    public CompletableFuture<String> RepresentationVISAIncomingRecueRetrait(AtomicBoolean stopSignal) {
        logger.info("begin RepresentationVISAIncomingRecueRetrait");
        String methodName = getCurrentMethodName();
        String result = "ERROR";

        try {
            List<VisaSummary> summaries = visaSummaryRepository.findByTreatM();
            if (summaries == null || summaries.isEmpty()) {
                return CompletableFuture.completedFuture("NO_DATA");
            }

            for (VisaSummary visaSummary : summaries) {
                if (stopSignal.get() || Thread.currentThread().isInterrupted()) {
                    throw new InterruptedException("Stop signal received");
                }

                List<VisaIncoming> VisaDispute = operationRepo.VisaIncomingATMDisp(visaSummary.getSummaryCode());
                List<DayOperationInternational> dayOperations = new ArrayList<DayOperationInternational>();
                List<VisaDisputeStatusAdvice> updatedVisaOut = new ArrayList<VisaDisputeStatusAdvice>();

                for (VisaIncoming e : VisaDispute) {
                    if (stopSignal.get() || Thread.currentThread().isInterrupted()) {
                        throw new InterruptedException("Stop signal received");
                    }
                    Date date = null;
                    try {
                        date = inputDateFormat.get().parse(visaSummary.getSummary_date());
                    } catch (ParseException ex) {
                        LOG.info(ex.getMessage());
                        String stackTrace = Throwables.getStackTraceAsString(ex);
                        LOG.info("Exception is=>{}", stackTrace);

                    }
                    String outputDateString = outputDateFormat.get().format(date);

                    String anneeTransaction = outputDateString.substring(4);
                    String transactionDate = anneeTransaction + e.getPurchaseDate();
                    try {
                        date = inputDateFormatVisa.get().parse(transactionDate);
                    } catch (ParseException e1) {
                        String stackTrace = Throwables.getStackTraceAsString(e1);
                        LOG.info("Exception is=>{}", stackTrace);
                    }
                    transactionDate = outputDateFormatTransactionDate.get().format(date);
                    double commission = 0.0;
                    double tva = 0.0;
                    double taux= e.getBaseCurrencyToDestinationCurrencyExchangeRate();
                    if (taux==0)
                    {
                        taux=1;

                    }
                    else {
                        taux=convertAmount(1/e.getBaseCurrencyToDestinationCurrencyExchangeRate(),3);

                    }
                    DayOperationInternational G1028 = new DayOperationInternational(findIdentification("G1028"),
                            getFullAccountNumber(e), e.getTerminalId(), e.getSourceAmount(),
                            e.getSourceCurrencyCode(), getSourceToBaseExchangeRateInverse(e),
                            e.getBaseCurrency(), String.valueOf(e.getIncomingCode()), new Date(),
                            transactionDate, e.getAuthorisationCode(), e.getAcquirerReferenceNumber(),
                            e.getInterchangeFeeAmount(), e.getTransactionIdentifier(), outputDateString,
                            e.getSummaryCode(), e.getCardAcceptorId(), e.getTransactionIdentifier(),
                            e.getDe37(),e.getDe11(),e.getMatchingSwitch(),
                            e.getBaseCurrencyToDestinationCurrencyExchangeRate(), e.getDestinationAmount(),
                            e.getMerchantName(),"976","G1028",sourceVisa);
                    DayOperationInternational G1029 = new DayOperationInternational(findIdentification("G559"),
                            getFullAccountNumber(e), e.getTerminalId(), e.getSourceAmount(),
                            e.getSourceCurrencyCode(), getSourceToBaseExchangeRateInverse(e),
                            e.getBaseCurrency(), String.valueOf(e.getIncomingCode()), new Date(),
                            transactionDate, e.getAuthorisationCode(), e.getAcquirerReferenceNumber(),
                            e.getInterchangeFeeAmount(), e.getTransactionIdentifier(), outputDateString,
                            e.getSummaryCode(), e.getCardAcceptorId(), e.getTransactionIdentifier(),
                            e.getDe37(),e.getDe11(),e.getMatchingSwitch(),
                            e.getBaseCurrencyToDestinationCurrencyExchangeRate(), e.getDestinationAmount(),
                            e.getMerchantName(),"976","G1029",sourceVisa);
                    setNumRIBPorteurByType(G1028, getFullAccountNumber(e), "DEVISE");
                    setNumRIBPorteurByType(G1029, getFullAccountNumber(e), "DEVISE");
                    dayOperations.add(G1028);
                    dayOperations.add(G1029);


                }

                dayOperationsChargebackL.addAll(dayOperations);

                LOG.info("end RepresentationVISAIncomingRecueRetrait for summary {} => {}", visaSummary.getSummaryCode(), dayOperations.size());
            }

            result = "SUCCESS";
            logger.info("Completed RepresentationVISAIncomingRecueRetrait");

        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            logger.warn("Cancelled RepresentationVISAIncomingRecueRetrait");
            throw new RuntimeException("CANCELLED: RepresentationVISAIncomingRecueRetrait", ie);
        } catch (Exception e) {
            logger.error("Error in RepresentationVISAIncomingRecueRetrait: {}", Throwables.getStackTraceAsString(e));
            throw new RuntimeException("ERROR: RepresentationVISAIncomingRecueRetrait", e);
        }

        return CompletableFuture.completedFuture(result);
    }

    @Async("taskExecutor")
    public CompletableFuture<String> chargeVisaGlobalEURCredit(AtomicBoolean stopSignal) {
        logger.info("begin chargeVisaGlobalEURCredit");
        String methodName = getCurrentMethodName();
        String result = "ERROR";

        try {
            List<VisaSummary> summaries = visaSummaryRepository.findByTreatM();
            if (summaries == null || summaries.isEmpty()) {
                return CompletableFuture.completedFuture("NO_DATA");
            }

            List<DayOperationInternational> dayOperations = new ArrayList<DayOperationInternational>();

            for(VisaSummary visaSummary : summaries){
                if (stopSignal.get() || Thread.currentThread().isInterrupted()) {
                    throw new InterruptedException("Stop signal received");
                }

                Date date = null;
                try {
                    date = inputDateFormat.get().parse(visaSummary.getSummary_date());

                } catch (Exception ex) {

                    LOG.info(ex.getMessage());
                    String stackTrace = Throwables.getStackTraceAsString(ex);
                    LOG.info("Exception is=>{}", stackTrace);

                }
                String outputDateString = outputDateFormat.get().format(date);
                VisaV2 chargeEmissionv = totalVisaRepository.getTotalVisaChargeEUR(visaSummary.getSummaryCode()).orElse(null);

                DayOperationInternational G772 = new DayOperationInternational();
                G772.setCompletedAmtSettlement(Double.valueOf(chargeEmissionv.getCreditAmount())/100);
                G772.setCurrencyCodeSettlement("978");
                G772.setTypeOperation( findIdentification("G772"));
                G772.setCurrencyCodeTransaction("978");
                G772.setCodeFile(visaSummary.getSummaryCode()+"978"+"C");
                G772.setReferenceNumber(visaSummary.getSummaryCode()+"978"+"C");
                G772.setTransactionAmount(G772.getCompletedAmtSettlement());
                double conversionRateSettlementToTnd = 1.0;
                double tndAmount= Math.round(G772.getCompletedAmtSettlement()*conversionRateSettlementToTnd*1000.0)/1000.0;
                G772.setConversionRateSettlement(conversionRateSettlementToTnd);
                G772.setConversionRateSettlementToTnd(conversionRateSettlementToTnd);
                G772.setTndAmount(tndAmount);
                G772.setFileDate(outputDateString);
                G772.setOperationDate(new Date());
                G772.setSource(this.sourceVisa);
                dayOperations.add(G772);
            }

            dayOperationsL.addAll(dayOperations);
            LOG.info("end chargeVisaGlobalEURCredit => {}", dayOperations.size());

            result = "SUCCESS";
            logger.info("Completed chargeVisaGlobalEURCredit");

        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            logger.warn("Cancelled chargeVisaGlobalEURCredit");
            throw new RuntimeException("CANCELLED: chargeVisaGlobalEURCredit", ie);
        } catch (Exception e) {
            logger.error("Error in chargeVisaGlobalEURCredit: {}", Throwables.getStackTraceAsString(e));
            throw new RuntimeException("ERROR: chargeVisaGlobalEURCredit", e);
        }

        return CompletableFuture.completedFuture(result);
    }
    @Async("taskExecutor")
    public CompletableFuture<String> chargeVisaGlobalEURDebit(AtomicBoolean stopSignal) {
        logger.info("begin chargeVisaGlobalEURDebit");
        String methodName = getCurrentMethodName();
        String result = "ERROR";

        try {
            List<VisaSummary> summaries = visaSummaryRepository.findByTreatM();
            if (summaries == null || summaries.isEmpty()) {
                return CompletableFuture.completedFuture("NO_DATA");
            }

            List<DayOperationInternational> dayOperations = new ArrayList<DayOperationInternational>();

            for(VisaSummary visaSummary : summaries){
                if (stopSignal.get() || Thread.currentThread().isInterrupted()) {
                    throw new InterruptedException("Stop signal received");
                }

                Date date = null;
                try {
                    date = inputDateFormat.get().parse(visaSummary.getSummary_date());

                } catch (Exception ex) {

                    LOG.info(ex.getMessage());
                    String stackTrace = Throwables.getStackTraceAsString(ex);
                    LOG.info("Exception is=>{}", stackTrace);

                }
                String outputDateString = outputDateFormat.get().format(date);
                VisaV2 chargeEmissionv = totalVisaRepository.getTotalVisaChargeEUR(visaSummary.getSummaryCode()).orElse(null);
                DayOperationInternational G834 = new DayOperationInternational();
                G834.setCompletedAmtSettlement(Double.valueOf(chargeEmissionv.getDebitAmount())/100);
                G834.setCurrencyCodeSettlement("978");
                G834.setTypeOperation( findIdentification("G834"));
                G834.setCurrencyCodeTransaction("978");
                G834.setCodeFile(visaSummary.getSummaryCode()+"978"+"D");
                G834.setReferenceNumber(visaSummary.getSummaryCode()+"978"+"D");
                G834.setTransactionAmount(G834.getCompletedAmtSettlement());
                double conversionRateSettlementToTnd = 1.0;
                double tndAmount= Math.round(G834.getCompletedAmtSettlement()*conversionRateSettlementToTnd*1000.0)/1000.0;
                G834.setConversionRateSettlement(conversionRateSettlementToTnd);
                G834.setConversionRateSettlementToTnd(conversionRateSettlementToTnd);
                G834.setTndAmount(tndAmount);
                G834.setFileDate(outputDateString);
                G834.setOperationDate(new Date());
                G834.setSource(this.sourceVisa);
                dayOperations.add(G834);
            }

            dayOperationsL.addAll(dayOperations);
            LOG.info("end chargeVisaGlobalEURDebit => {}", dayOperations.size());

            result = "SUCCESS";
            logger.info("Completed chargeVisaGlobalEURDebit");

        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            logger.warn("Cancelled chargeVisaGlobalEURDebit");
            throw new RuntimeException("CANCELLED: chargeVisaGlobalEURDebit", ie);
        } catch (Exception e) {
            logger.error("Error in chargeVisaGlobalEURDebit: {}", Throwables.getStackTraceAsString(e));
            throw new RuntimeException("ERROR: chargeVisaGlobalEURDebit", e);
        }

        return CompletableFuture.completedFuture(result);
    }
    @Async("taskExecutor")
    public CompletableFuture<String> disputeReversalvisa(AtomicBoolean stopSignal) {
        logger.info("begin disputeReversalvisa");
        String methodName = getCurrentMethodName();
        String result = "ERROR";

        try {
            List<VisaSummary> summaries = visaSummaryRepository.findByTreatM();
            if (summaries == null || summaries.isEmpty()) {
                return CompletableFuture.completedFuture("NO_DATA");
            }

            for(VisaSummary visaSummary : summaries){
                if (stopSignal.get() || Thread.currentThread().isInterrupted()) {
                    throw new InterruptedException("Stop signal received");
                }

                List<VisaIncoming> visaIncoming = operationRepo.disputeReversal(visaSummary.getSummaryCode());
                List<DayOperationInternational> dayOperations = new ArrayList<DayOperationInternational>();
                List<VisaIncoming> updatedVisaIn = new ArrayList<VisaIncoming>();

                for (VisaIncoming element : visaIncoming) {
                    if (stopSignal.get() || Thread.currentThread().isInterrupted()) {
                        throw new InterruptedException("Stop signal received");
                    }

                    Date date = null;
                    try {
                        //LOG.info ("date du fichier "+fileDate);
                        date = inputDateFormat.get().parse(visaSummary.getSummary_date());
                    } catch (ParseException ex) {
                        LOG.info(ex.getMessage());
                        String stackTrace = Throwables.getStackTraceAsString(ex);
                        LOG.info("Exception is=>{}", stackTrace);
                    }
                    String outputDateString = outputDateFormat.get().format(date);
                    String transactionDate=element.getTerminalTransactionDate();
                    String transactionDateOutput="";
                    if ((transactionDate==null) ||("000000".equals(transactionDate)))
                    {
                        transactionDate=element.getPurchaseDate();
                        //	LOG.info("date du fichier achat incoming visa"+outputDateString);
                        //	LOG.info("date de transaction sans annee"+transactionDate);

                        int processingDateMonth=Integer.parseInt (outputDateString.substring(2,4));
                        int processingDateDay=Integer.parseInt( outputDateString.substring(0,2));
                        int processingDateYear=Integer.parseInt(outputDateString.substring(4));

                        int purchaseDateMonth=Integer.parseInt(transactionDate.substring(0,2));
                        int purchaseDateDay=Integer.parseInt(transactionDate.substring(2));
                        if (purchaseDateMonth>processingDateMonth)
                        {
                            processingDateYear--;
                        }
                        else if ((purchaseDateMonth==processingDateMonth) && ((purchaseDateDay>processingDateDay)))
                        {
                            processingDateYear--;
                        }

                        // Calculate the current century

                        String transactionDateResult=processingDateYear+""+transactionDate;
                        transactionDate =transactionDateResult;
                        LOG.info("transaction date � partir de purchase date "+transactionDate);
                    }


                    Date transactionDateInput = null;
                    try {
                        transactionDateInput=inputDateFormatVisa.get().parse(transactionDate);
                        transactionDateOutput=outputDateFormatTransactionDate.get().format(transactionDateInput);

                    } catch (ParseException e) {
                        String stackTrace = Throwables.getStackTraceAsString(e);
                        LOG.info("Exception is=>{}", stackTrace);
                    }
                    LOG.info("transaction date visa incoming achat "+transactionDateOutput);

                    DayOperationInternational G1018 = new DayOperationInternational(findIdentification("G1018"),
                            getFullAccountNumber(element), element.getTerminalId(), element.getSourceAmount(),
                            element.getSourceCurrencyCode(), getSourceToBaseExchangeRateInverse(element),
                            element.getBaseCurrency(), String.valueOf(element.getIncomingCode()), new Date(),
                            transactionDateOutput, element.getAuthorisationCode(), element.getAcquirerReferenceNumber(),
                            element.getBaseAmount(), element.getTransactionIdentifier(), outputDateString,
                            element.getSummaryCode(), element.getCardAcceptorId(), element.getTransactionIdentifier(),
                            element.getDe37(),element.getDe11(),element.getMatchingSwitch(),
                            element.getBaseCurrencyToDestinationCurrencyExchangeRate(), element.getDestinationAmount(),
                            element.getMerchantName(),"976","G1018",sourceVisa);
                    DayOperationInternational G1019 = new DayOperationInternational(findIdentification("G1019"),
                            getFullAccountNumber(element), element.getTerminalId(), element.getSourceAmount(),
                            element.getSourceCurrencyCode(), getSourceToBaseExchangeRateInverse(element),
                            element.getBaseCurrency(), String.valueOf(element.getIncomingCode()), new Date(),
                            transactionDateOutput, element.getAuthorisationCode(), element.getAcquirerReferenceNumber(),
                            element.getInterchangeFeeAmount(), element.getTransactionIdentifier(), outputDateString,
                            element.getSummaryCode(), element.getCardAcceptorId(), element.getTransactionIdentifier(),
                            element.getDe37(),element.getDe11(),element.getMatchingSwitch(),
                            element.getBaseCurrencyToDestinationCurrencyExchangeRate(), element.getDestinationAmount(),
                            element.getMerchantName(),"976","G1018",sourceVisa);
//				setCompteDebitCreditVisaIncoming(G558, element, "G558");
//				setCompteDebitCreditVisaIncoming(G559, element, "G559");
                    dayOperations.add(G1018);
                    dayOperations.add(G1019);
                    element.setTreat("1");
                    updatedVisaIn.add(element);

                }
                
                dayOperationsL.addAll(dayOperations);
                updatedVisaInL.addAll(updatedVisaIn);
                LOG.info("end disputeReversalvisa for summary {} => {}", visaSummary.getSummaryCode(), dayOperations.size());
            }
            
            result = "SUCCESS";
            logger.info("Completed disputeReversalvisa");
            
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            logger.warn("Cancelled disputeReversalvisa");
            throw new RuntimeException("CANCELLED: disputeReversalvisa", ie);
        } catch (Exception e) {
            logger.error("Error in disputeReversalvisa: {}", Throwables.getStackTraceAsString(e));
            throw new RuntimeException("ERROR: disputeReversalvisa", e);
        }
        
        return CompletableFuture.completedFuture(result);
    }

    // All async methods have been successfully converted to CompletableFuture pattern:
    // ✓ All methods now use @Async("taskExecutor") annotation
    // ✓ All methods return CompletableFuture<String>
    // ✓ All methods accept AtomicBoolean stopSignal for cancellation
    // ✓ All methods have proper InterruptedException handling
    // ✓ All methods have consistent exception handling and logging


    public int findIdentification(String identification) {
        if (identificationMapper==null || identificationMapper.size()==0) {
            initializeIdentificationMapper();
        }
        Integer settlementCode = identificationMapper.get(identification);
//        LOG.info("findIdentification="+settlementCode);
        return (settlementCode != null) ? settlementCode : 0;
    }
    public static double convertAmount(double amount, int nb) {
        return Math.round(amount * Math.pow(10, nb)) / Math.pow(10, nb);
    }

    private Double getSourceToBaseExchangeRateInverse(VisaIncoming v) {
        if (v.getSourceCurrencyToBaseCurrencyExchangeRate() == 0) {
            LOG.info("SourceCurrencyToBaseCurrencyExchangeRate is zero");
            return null;
        }
        double exRate=v.getSourceCurrencyToBaseCurrencyExchangeRate();
        //exchange rate SourceCurrencyToBaseCurrencyExchangeRate fixxxx
        if(("788".equals(v.getSourceCurrencyCode()) && v.getSourceCurrencyToBaseCurrencyExchangeRate()==1) &&
                !Arrays.asList(0.0,1.0).contains(v.getBaseCurrencyToDestinationCurrencyExchangeRate())){
            exRate=v.getBaseCurrencyToDestinationCurrencyExchangeRate();
        }
        return Math.round((1 / exRate) * 1000000) / 1000000.0;
    }
    private Double getdestinationToBaseExchangeRateInverse(VisaIncoming v) {
        if (v.getBaseCurrencyToDestinationCurrencyExchangeRate() == 0) {
            LOG.info("SourceCurrencyToBaseCurrencyExchangeRate is zero");
            return null;
        }
        double exRate=v.getBaseCurrencyToDestinationCurrencyExchangeRate();
        //exchange rate SourceCurrencyToBaseCurrencyExchangeRate fixxxx

        return Math.round((1 / exRate) * 1000000) / 1000000.0;
    }

    /**
     * Gets the full account number by concatenating account number with extension if applicable.
     * Returns accountNumber + accountNumberExtension if extension is not null and not "000".
     * Otherwise returns just the accountNumber.
     * 
     * @param visaIncoming The VisaIncoming object containing account information
     * @return The full account number with extension if applicable
     */
    private String getFullAccountNumber(VisaIncoming visaIncoming) {
        if (visaIncoming == null) {
            return null;
        }
        
        String accountNumber = visaIncoming.getAccountNumber();
        String accountNumberExtension = visaIncoming.getAccountNumberExtension();
        
        // Check if extension exists and is not "000"
        if (accountNumberExtension != null && !"000".equals(accountNumberExtension)) {
            return accountNumber + accountNumberExtension;
        }
        
        return accountNumber;
    }
    /**
     * Gets the full account number by concatenating account number with extension if applicable.
     * Returns accountNumber + accountNumberExtension if extension is not null and not "000".
     * Otherwise returns just the accountNumber.
     *
     * @param dispute The VisaIncoming object containing account information
     * @return The full account number with extension if applicable
     */
    private String getFullAccountNumber(VisaDisputeStatusAdvice dispute) {
        if (dispute == null) {
            return null;
        }

        String accountNumber = dispute.getAccountNumber();
        String accountNumberExtension = dispute.getAccountNumberExtension();

        // Check if extension exists and is not "000"
        if (accountNumberExtension != null && !"000".equals(accountNumberExtension)) {
            return accountNumber + accountNumberExtension;
        }

        return accountNumber;
    }

    /**
     * Sets the NumRIBPorteur and CodeAgence for a DayOperationInternational based on the card number and account type.
     * This method centralizes the logic for setting RIB and extracting agency code, allowing for easier future modifications.
     * 
     * The agency code is extracted from the RIB as a 5-character substring starting at position 3.
     * Example: RIB "03501601220100078356" -> Agency "01601"
     * 
     * @param operation The DayOperationInternational object to update
     * @param cardNumber The card number to look up the account
     * @param accountType The type of account ("DEVISE" or "DINARS")
     */
    private void setNumRIBPorteurByType(DayOperationInternational operation, String cardNumber, String accountType) {
        if (operation == null) {
            logger.warn("Cannot set NumRIBPorteur: operation is null");
            return;
        }
        
        String ribPorteur = getaccountByType(cardNumber, accountType);
        
        // Set the RIB
        operation.setNumRIBPorteur(ribPorteur);
        
        // Extract and set the code agence from the RIB
        if (ribPorteur != null && ribPorteur.length() >= 8) {
            // Agency code is characters 4-8 (positions 3-7, 5 characters)
            String codeAgence = ribPorteur.substring(3, 8);
            operation.setCodeAgence(codeAgence);
            logger.debug("Set CodeAgence: {} from RIB: {}", codeAgence, ribPorteur);
        } else {
            logger.debug("Cannot extract CodeAgence: RIB is null or too short for cardNumber: {} and accountType: {}", 
                        cardNumber, accountType);
        }
    }


}
