package com.mss.backOffice.services;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.google.common.base.Throwables;
import com.mss.backOffice.controller.BankSettlementController;
import com.mss.unified.entities.*;
import com.mss.unified.repositories.ChargebacksInternationalRepository;
import com.mss.unified.repositories.DayOperationChargebackInterRep;
import com.mss.unified.repositories.VisaIncomingRepository;
import com.mss.unified.repositories.VisaSummaryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ChargeBackService {
	@Autowired
	DayOperationChargebackInterRep dayOperationChargebackInternationalRepository;
	@Autowired
	VisaIncomingRepository visaIncomingRepository;
	@Autowired
	VisaSummaryRepository visaSummaryRepository;
	public static ThreadLocal<SimpleDateFormat> inputDateFormat =ThreadLocal.withInitial(() -> new SimpleDateFormat("yyyy-MM-dd"));
	public static ThreadLocal<SimpleDateFormat> inputDateFormatVisa = ThreadLocal.withInitial(()->new SimpleDateFormat("yyMMdd"));
	public static ThreadLocal<SimpleDateFormat> outputDateFormat = ThreadLocal.withInitial(()->new SimpleDateFormat("ddMMyy"));
	public static ThreadLocal<SimpleDateFormat> outputDateFormatTransactionDate = ThreadLocal.withInitial(()->new SimpleDateFormat("yyyyMMdd"));
	public static ThreadLocal<SimpleDateFormat> inputDateFormatTransactionDateToCorrect = ThreadLocal.withInitial(()->new SimpleDateFormat("ddMMyyyy"));
	String sourceVisa="VISA";
	public static List<ChargebacksInternational> acceptedChargebackVISA = new ArrayList<ChargebacksInternational>();

	List<DayOperationChargebackInternational> AcceptChargebackEmisVISA = new ArrayList<>();
	private static final Logger LOG = LoggerFactory.getLogger(ChargeBackService.class);
    @Autowired
    private ChargebacksInternationalRepository chargebacksInternationalRepository;

	public void AppurementchargebackEmisVISA(List<ChargebacksInternational> ipms) throws InterruptedException {
		
		LOG.info("start AppurementchargebackEmisVISA");
		ipms.forEach(ele -> {
			try {
				VisaIncoming e = visaIncomingRepository.findFirstByAuthorisationCodeAndAcquirerReferenceNumberAndSummaryCode(ele.getAuthCode(), ele.getRrn(), ele.getSummaryCode());
				Date date = null;
				try {
					VisaSummary visaSummary = visaSummaryRepository.findById(e.getSummaryCode()).get();
					date = inputDateFormat.get().parse(visaSummary.getSummary_date());
				} catch (Exception ex) {

					LOG.info(ex.getMessage() );
					String stackTrace = Throwables.getStackTraceAsString(ex);
					LOG.info("Exception is=>{}", stackTrace);

				}
				String outputDateString = outputDateFormat.get().format(date);
				String transactionDate=e.getPurchaseDate();
				String transactionDateOutput="";
				transactionDate=e.getPurchaseDate();
				LOG.info("date du fichier"+outputDateString);
				LOG.info("date de transaction sans annee"+transactionDate);

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
				LOG.info("transaction date a partir de purchase date "+transactionDate);

				Date transactionDateInput = null;
				try {
					transactionDateInput=inputDateFormatVisa.get().parse(transactionDate);
					transactionDateOutput=outputDateFormatTransactionDate.get().format(transactionDateInput);

				} catch (ParseException exception) {
					String stackTrace = Throwables.getStackTraceAsString(exception);
					LOG.info("Exception is=>{}", stackTrace);
				}
				LOG.info("start generation vias chargeback" );
				LOG.info(e.getTransactionCode() );
				//last check, only baseToCurrency of visaIncomming is under 1
				Double conversionRateToTND = ele.getConversionRateToTND() != null ? Math.round((ele.getConversionRateToTND() >= 1 ?
						ele.getConversionRateToTND() : 1 / ele.getConversionRateToTND()) * Math.pow(10, 6)) / Math.pow(10, 6) : null;
				if (e.getTransactionCode().equals("07")) {
					//RETRAIT

					DayOperationChargebackInternational G620 = new DayOperationChargebackInternational(
							"G620", e.getAccountNumber().trim(), e.getCardAcceptorId(), e.getSourceAmount(), e.getBaseCurrency(),
							ele.getConversionRate(), e.getSourceCurrencyCode(), String.valueOf(e.getIncomingCode()), new Date(), transactionDateOutput, ele.getAuthCode(),
							e.getAcquirerReferenceNumber(), ele.getSettlementAmount(), e.getTransactionIdentifier(), conversionRateToTND, e.getDestinationAmount(), e.getCardAcceptorId(), null, e.getSummaryCode(), this.sourceVisa);

					DayOperationChargebackInternational G621 = new DayOperationChargebackInternational(
							"G621", e.getAccountNumber().trim(), e.getCardAcceptorId(), e.getSourceAmount(), e.getBaseCurrency(),
							ele.getConversionRate(), e.getSourceCurrencyCode(), String.valueOf(e.getIncomingCode()), new Date(), transactionDateOutput,ele.getAuthCode(),
							e.getAcquirerReferenceNumber(), ele.getInterchange(), e.getTransactionIdentifier(), conversionRateToTND, e.getDestinationAmount(), e.getCardAcceptorId(), null, e.getSummaryCode(), this.sourceVisa);

					ele.setStatusChargeback(3);

					G620.setCompteDebit("appurement");
					G621.setCompteDebit("appurement");


					AcceptChargebackEmisVISA.add(G620);
					AcceptChargebackEmisVISA.add(G621);
					acceptedChargebackVISA.add(ele);

					LOG.info(AcceptChargebackEmisVISA.toString() );
					LOG.info(acceptedChargebackVISA.toString() );

				} else if (e.getTransactionCode().equals("05")) {
					//ACHAT
					LOG.info("entred" );
					DayOperationChargebackInternational G618 = new DayOperationChargebackInternational(
							"G618", e.getAccountNumber().trim(), e.getCardAcceptorId(), e.getSourceAmount(), e.getBaseCurrency(),
							ele.getConversionRate(), e.getSourceCurrencyCode(), String.valueOf(e.getIncomingCode()), new Date(), transactionDateOutput,ele.getAuthCode(),
							e.getAcquirerReferenceNumber(), ele.getSettlementAmount(), e.getTransactionIdentifier(), conversionRateToTND, e.getDestinationAmount(), e.getCardAcceptorId(), null, e.getSummaryCode(), this.sourceVisa);

					DayOperationChargebackInternational G619 = new DayOperationChargebackInternational(
							"G619", e.getAccountNumber().trim(), e.getCardAcceptorId(), e.getSourceAmount(), e.getBaseCurrency(),
							ele.getConversionRate(), e.getSourceCurrencyCode(), String.valueOf(e.getIncomingCode()), new Date(), transactionDateOutput,ele.getAuthCode(),
							e.getAcquirerReferenceNumber(), ele.getInterchange(), e.getTransactionIdentifier(), conversionRateToTND, e.getDestinationAmount(), e.getCardAcceptorId(), null, e.getSummaryCode(), this.sourceVisa);




					ele.setStatusChargeback(3);
					G618.setCompteDebit("appurement");
					G619.setCompteDebit("appurement");

					AcceptChargebackEmisVISA.add(G618);
					AcceptChargebackEmisVISA.add(G619);


					LOG.info(AcceptChargebackEmisVISA.toString() );
					LOG.info(acceptedChargebackVISA.toString() );
				}
			}
			catch (Exception ex) {
				String stackTrace = Throwables.getStackTraceAsString(ex);
				LOG.info("Exception is=>{}", stackTrace);

			}

		});
	chargebacksInternationalRepository.saveAll(acceptedChargebackVISA);
		dayOperationChargebackInternationalRepository.saveAll(AcceptChargebackEmisVISA);

		LOG.info("end AppurementchargebackEmisVISA=>{}",AcceptChargebackEmisVISA.size());
		
	}
	
	
	public void AppurementchargebackRecuVisa(List<VisaDisputeStatusAdvice> visaAccepted, List<ChargebackVisa> chargebackVisa, List<ChargeBackVisaOutgoing> chargebackVisaOutgoing) throws InterruptedException {
		LOG.info("start AppurementchargebackRecuVISA Achat");
//		Thread[] threads = new Thread[1];
//		Runnable[] runn = new Runnable[1];
//		runn[0] = new ThreadChargebackRepresentation(12, visaAccepted,chargebackVisa,chargebackVisaOutgoing, settlementOperationRepo,
//				dayOperationInternationalRepository, tc45Repository, historicDayRepo, visaSummaryRepository,operationRepo,visaIncomingRepo,visaRepo,chargeBackVisaOutgoingRepository,chargebackVisaRepository);
//
//		threads[0] = new Thread(runn[0]);
//		threads[0].setName("DayOPN: " + Integer.toString(0));
//		threads[0].start();
//
//		try {
//			threads[0].join();
//			if (threadExceptionVisa != null) {
//				throw new RuntimeException("Le thread a échoué : " + threadExceptionVisa.getMessage(), threadExceptionVisa);
//			}
//		} catch (InterruptedException e) {
//			Thread.currentThread().interrupt();
//		}
//
//		LOG.info("Appurement chargeback Recu VISA terminé => {}", updatedChargebackL.size());
//
//		// Sauvegarde de l'Appurement (Mise à jour des statusChargeback = 3)
//		dayOperationInterRepo.saveAll(dayOperationsChargebackL);
//		chargebackVisaRepository.saveAll(updatedChargebackL);
//
//		// Création de l'historique après la mise à jour
//		List<ChargeBackHistoryInternational> chargeBackHistoryList = updatedChargebackL.stream().map(e -> {
//			ChargeBackHistoryInternational chargebackHistory = new ChargeBackHistoryInternational(e);
//			chargebackHistory.setStatusChargeback(e.getStatusChargeback());  // Devrait être 3
//			return chargebackHistory;
//		}).collect(Collectors.toList());
//
//		// Sauvegarde de l'historique
//		chargeBackHistoryInterRepository.saveAll(chargeBackHistoryList);
//
//		// Nettoyage des listes après sauvegarde
//		updatedChargebackL.clear();
//		dayOperationsChargebackL.clear();
//		chargeBackHistoryList.clear();
//
		LOG.info("End Appurement chargeback Recu VISA ACHAT ");
	}
	
	public void AppurementchargebackRecuVisaOutgoingAtm(List<VisaDisputeStatusAdvice> visaAccepted, List<ChargebackVisa> chargebackVisa, List<ChargeBackVisaOutgoing> chargebackVisaOutgoing) throws InterruptedException {
		LOG.info("start AppurementchargebackRecuVISA Retrait ");
//		Thread[] threads = new Thread[1];
//		Runnable[] runn = new Runnable[1];
//		runn[0] = new ThreadChargebackRepresentation(14, visaAccepted,chargebackVisa,chargebackVisaOutgoing, settlementOperationRepo,
//				dayOperationInternationalRepository, tc45Repository, historicDayRepo, visaSummaryRepository,operationRepo,visaIncomingRepo,visaRepo,chargeBackVisaOutgoingRepository,chargebackVisaRepository);
//		threads[0] = new Thread(runn[0]);
//		threads[0].setName("DayOPN: " + Integer.toString(0));
//		threads[0].start();
//
//
//		try {
//			threads[0].join();
//			if (threadExceptionVisa != null) {
//				throw new RuntimeException("Le thread a échoué : " + threadExceptionVisa.getMessage(), threadExceptionVisa);
//			}
//		} catch (InterruptedException e) {
//			Thread.currentThread().interrupt();
//		}
//
//		LOG.info("Appurement chargeback Recu VISA terminé => {}", updatedChargebackL.size());
//
//		// Sauvegarde de l'Appurement (Mise à jour des statusChargeback = 3)
//		dayOperationInterRepo.saveAll(dayOperationsChargebackL);
//		chargeBackVisaOutgoingRepository.saveAll(updatedChargeBackVisaOutgoing);
//
//		// Création de l'historique après la mise à jour
//		List<ChargeBackHistoryInternational> chargeBackHistoryList = updatedChargeBackVisaOutgoing.stream().map(e -> {
//			ChargeBackHistoryInternational chargebackHistory = new ChargeBackHistoryInternational(e);
//			chargebackHistory.setStatusChargeback(e.getStatusChargeback());  // Devrait être 3
//			return chargebackHistory;
//		}).collect(Collectors.toList());
//
//		// Sauvegarde de l'historique
//		chargeBackHistoryInterRepository.saveAll(chargeBackHistoryList);
//
//		// Nettoyage des listes après sauvegarde
//		updatedChargeBackVisaOutgoing.clear();
//		dayOperationsChargebackL.clear();
//		chargeBackHistoryList.clear();
//
//		LOG.info("End Appurement chargeback Recu VISA ACHAT ");
	}
}
