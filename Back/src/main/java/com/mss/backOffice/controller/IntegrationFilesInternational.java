package com.mss.backOffice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Throwables;
import com.mss.backOffice.Response.FileIntegrationClearing;
 import com.mss.backOffice.request.AddFileRequest;
 import com.mss.unified.entities.*;
import com.mss.unified.enumeration.ChargeBackType;
import com.mss.unified.enumeration.TypeDossier;
import com.mss.unified.repositories.*;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.util.ReplacingInputStream;
import org.apache.tika.io.IOUtils;
import org.jpos.iso.ISOException;
import org.jpos.iso.ISOMsg;
import org.jpos.iso.packager.GenericPackager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Order;
import org.springframework.scheduling.annotation.Async;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.bind.annotation.*;

import javax.xml.bind.DatatypeConverter;
import java.io.*;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

 import static java.util.Arrays.asList;

@RestController
@RequestMapping("IntegrationFileInternational")
public class IntegrationFilesInternational {
	public static Charset c = Charset.forName("ISO-8859-15");
	private static final Logger logger = LoggerFactory.getLogger(IntegrationFilesInternational.class);

	@Autowired
	FraisVisaIncomingRepository fraisVisaIncomingRepo;
	@Autowired

	BatchesFFCRepository batchesFFCRepository;
	@Autowired
	VisaRaportRepository visaRaportRepository;
	@Autowired
	VisaDisputeStatusAdviceRepository visaDisputeStatusAdviceRepository;
	
	@Autowired
	private VisaIncomingRepository visaIncomingRepository;

	@Autowired
	private VisaOutComingAtmRepository visaOutComingAtmRepository;
	@Autowired

	private Tc45Repository tc45Repository;
	@Autowired
	TotalVisaRepository totalVisaRepository;
	@Autowired
	private VisaSummaryRepository visaSummaryRepository;

	@Autowired
	private BatchesFFCRepository batchesRepository;
	@Autowired
	VisaSummaryRepository visaSummaryRepo;
	private String methode;
	@Autowired
	private TransactionTemplate transactionTemplate;
 	private String lineUsed;
	@Autowired
	public TotalVisaRepository totalVisaRepo;
	@Autowired
	private ChargebackVisaRepository chargebackVisaRepo;
	@Autowired
	public VisaV2Repository visaV2Repo;
	@Autowired
	public Tc04Repository tc04Repo;

	@Autowired
	private DayOperationInternationalRepository dayOperationInternationalRepository;

	@Autowired
	public OperationCodeRepository operationCodeRep;
	@Autowired
	public ChargeBackHistoryInterRepository chargeBackHistoryInterRepository;
    @Autowired
    private ChargebacksInternationalRepository chargebacksInternationalRepository;
    ChargebackVisa internationalChargeback;
	public IntegrationFilesInternational() {
		super();
	}

	// new code by RM
	public String transformDateFromFile(@RequestBody BatchesFC addFileRequest) throws IOException {
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
		LocalDate localDate;
		
		if (addFileRequest.getDateReg() != null && !addFileRequest.getDateReg().trim().equals("")) {
			localDate = LocalDate.parse(addFileRequest.getDateReg(), formatter);
		} else {
			localDate = LocalDate.now();
		}
		
		int year = localDate.getYear();
		int month = localDate.getMonthValue();
		int day = localDate.getDayOfMonth();
		LocalDate today = LocalDate.of(year, month, day);
		String ordinalDateString = today.format(DateTimeFormatter.ISO_ORDINAL_DATE);

		switch (addFileRequest.getFileName().substring(0, 1)) {
		case "F":
			ordinalDateString = ordinalDateString.substring(5, 8);
			break;
		case "I":
			String yearString = ordinalDateString.substring(2, 4);
			ordinalDateString = yearString + "." + ordinalDateString.substring(5, 8);
			break;
		case "C":
			break;
		}
		logger.info("ordinalDateString " + ordinalDateString);
		return ordinalDateString;
	}

	@GetMapping("AllSummaryVisa")
	public List<String> findAllSummaryVISA() {
		List<VisaSummary> ListcpSummary = visaSummaryRepository.findAll();
		List<String> processingCode = new ArrayList<String>();
		for (VisaSummary summary : ListcpSummary) {
			processingCode.add(summary.getSummary_date());
		}
		return processingCode;
	}

	@Transactional
	public FileIntegrationClearing readFileVISA(BatchesFC addFileRequest) throws Exception {
		List<ChargeBackHistoryInternational> chargeBackHistoryInternationals = new ArrayList<>();
		FileIntegrationClearing response = new FileIntegrationClearing();

		String ordinalDateString = transformDateFromFile(addFileRequest);
		String fileName = addFileRequest.getFileLocation() + File.separator + addFileRequest.getFileName() + "." + ordinalDateString;
		logger.info(fileName, Files.exists(Paths.get(fileName)));

		List<List<String>> finalString33SubList = new ArrayList<>();
		List<List<String>> finalString05SubList = new ArrayList<>();
		List<List<String>> finalString45SubList = new ArrayList<>();
		List<List<String>> finalString46SubList = new ArrayList<>();
		List<List<String>> finalString15SubList = new ArrayList<>();
		List<List<String>> finalString04SubList = new ArrayList<>();
//		int summaryId = -1;

		Path filePath = Paths.get(fileName);
		boolean fileExists = Files.exists(filePath);
		if (fileExists) {
			int lineNumber = 0;
			Map<String, Integer> filLines = new HashMap<>();
			String li = null;
			
			try (Stream<String> stream = Files.lines(Paths.get(fileName))) {
				List<String> lines = stream.map(String::toUpperCase).collect(Collectors.toList());
				int j = 0;
				for (String lig : lines) {
					filLines.put(lig, j++);
				}
				String header = lines.stream().filter(line -> line.startsWith("90")).map(String::toUpperCase)
						.findFirst().orElse(null);

				List<String> listData = lines.stream()
						.filter(line -> line.startsWith("05") || line.startsWith("07") || line.startsWith("06")
								|| line.startsWith("27") || line.startsWith("25") || line.startsWith("35"))
						.map(String::toUpperCase).collect(Collectors.toList());

				List<String> finalString33 = lines.stream()
						.filter(line -> line.startsWith("33") && line.contains("SMSRAWDATA")).map(String::toUpperCase)
						.collect(Collectors.toList());
				
				List<List<String>> finalString33VCR = new ArrayList<>();
		        for (int i = 0; i < lines.size(); i++) {
		            String line = lines.get(i);

		            if (line.startsWith("33") && line.substring(16,19).equals("VCR") ) {
		            	 List<String> transaction=new ArrayList<>();
		            	 transaction.add(line);
		            	 int sequence=0;
		            	   for ( int k = i+1; k < lines.size(); k++) {
		            		   String subligne=lines.get(k);
		            		if(   parseSequenceNumber(subligne)>sequence) {
		            			transaction.add(subligne);
		            		}else {
		            			i=k-1;
		            			break;
		            		}
		            	   }
		                finalString33VCR.add(transaction);
		            }
		        }


				
				List<String> finalString45 = lines.stream().filter(line -> line.startsWith("45"))
						.map(String::toUpperCase).collect(Collectors.toList());

				List<String> listData46 = lines.stream().filter(line -> line.startsWith("46")).map(String::toUpperCase)
						.collect(Collectors.toList());

				List<String> listData46V2 = lines.stream().filter(line -> line.startsWith("46") && line.contains("V2")).map(String::toUpperCase)
						.collect(Collectors.toList());

				List<String> finalString10 = lines.stream().filter(line -> line.startsWith("10"))
						.map(String::toUpperCase).collect(Collectors.toList());
//				List<String> finalString20 = lines.stream().filter(line -> line.startsWith("20"))
//						.map(String::toUpperCase).collect(Collectors.toList());
				List<List<String>> finalString20Data = new ArrayList<>();
		        for (int i = 0; i < lines.size(); i++) {
		            String line = lines.get(i);

		            if (line.startsWith("20")   ) {
		            	 List<String> transaction=new ArrayList<>();
		            	 transaction.add(line);
		            	 int sequence=0;
		            	   for ( int k = i+1; k < lines.size(); k++) {
		            		   String subligne=lines.get(k);
		            		if(   parseSequenceNumber(subligne)>sequence) {
		            			transaction.add(subligne);
		            		}else {
		            			i=k-1;
		            			break;
		            		}
		            	   }
		            	   finalString20Data.add(transaction);
		            }
		        }


				List<String> chargebacks = lines.stream().filter(line -> line.startsWith("15") || line.startsWith("17"))
						.map(String::toUpperCase).collect(Collectors.toList());

				List<String> listData04 = lines.stream().filter(line -> line.startsWith("04")).map(String::toUpperCase)
						.collect(Collectors.toList());
				int i = 0;
				logger.info("listData.size() =>{}", listData.size());
				while (i < listData.size()) {
					// int j = i + 1;
					li = listData.get(i);
					//logger.info("i =>{}",i);

					List<String> finalString05sub = new ArrayList<>();
					// finalString33sub.add(listData.get(i));
					while ((i < listData.size() - 1) && (Integer.parseInt(listData.get(i).substring(2, 4)) < Integer.parseInt(listData.get(i + 1).substring(2, 4)))
							&& (Integer.parseInt(listData.get(i).substring(0, 2)) == Integer.parseInt(listData.get(i + 1).substring(0, 2))) && (!listData.get(i + 1).startsWith("0510")) && (!listData.get(i + 1).startsWith("0520"))) {

						finalString05sub.add(listData.get(i));
						i = i + 1;

					}
					finalString05sub.add(listData.get(i));
					finalString05SubList.add(finalString05sub);
					i = i + 1;
				}
				int chargeback = 0;
				while (chargeback < chargebacks.size()) {
					// int j = i + 1;
					li = chargebacks.get(chargeback);

					List<String> finalString15sub = new ArrayList<>();
					// finalString33sub.add(listData.get(i));
					while ((chargeback < chargebacks.size() - 1)
							&& (Integer.parseInt(chargebacks.get(chargeback).substring(2, 4)) < Integer
									.parseInt(chargebacks.get(chargeback + 1).substring(2, 4)))) {

						finalString15sub.add(chargebacks.get(chargeback));
						chargeback = chargeback + 1;
					//	logger.info("isecond =>{}",chargeback);

					}
					finalString15sub.add(chargebacks.get(chargeback));
					finalString15SubList.add(finalString15sub);
					chargeback = chargeback + 1;
					logger.info("ithord =>{}", chargeback);

				}

				int total = 0;

				while (total < listData46.size()) {
					li = listData46.get(total);
					List<String> finalString46sub = new ArrayList<>();
					while ((total < listData46.size() - 1)
							&& (Integer.parseInt(listData46.get(total).substring(2, 4)) < Integer
									.parseInt(listData46.get(total + 1).substring(2, 4)))) {

						finalString46sub.add(listData46.get(total));
						total = total + 1;
					}

					finalString46sub.add(listData46.get(total));
					finalString46SubList.add(finalString46sub);
					total = total + 1;

				}
				int k = 0;

				List<List<String>> finalGroups = new ArrayList<>();
				List<String> currentGroup = null;
				List<String> currentSubGroup = null;

				for (; k < finalString33.size(); k++) {
				    String line = finalString33.get(k);
				    
				    // Check if line is long enough for our substrings
				    if (line.length() < 41) continue;  // Skip malformed lines
				    
				    // Check for 2282 marker (positions 36-40)
				    if (line.substring(36, 40).equals("2282")) {
				        // Finalize previous groups
				        if (currentSubGroup != null && currentSubGroup.size()>0) {
						     logger.info(currentSubGroup.toString() );

				            finalGroups.add(currentSubGroup);
				            currentSubGroup = null;
				        }
				        if (currentGroup != null &&currentGroup.size()>0 ) {
							logger.info(currentGroup.toString( ));

				            finalGroups.add(currentGroup);
				        }
				        
				        // Start new main group
				        currentGroup = new ArrayList<>();
//				        currentGroup.add(line);
						continue;
				    }

					// Check for 2212 marker (positions 35-39)
					if (line.substring(35, 39).equals("2212")) {
						// Start new sub-group when 22200A is found
						currentSubGroup = new ArrayList<>();
//				        currentSubGroup.add(line);
						continue;
					}
					if (line.substring(35, 41).equals("22200A")) {
				            if (currentSubGroup != null && currentSubGroup.size()>0) {
							   logger.info(currentSubGroup.toString());

				                finalGroups.add(currentSubGroup);
				            }
				            currentSubGroup = new ArrayList<>();
				        }
				        if (currentSubGroup != null) {
				            currentSubGroup.add(line);
				        }

				}

				// Add remaining groups
				if (currentSubGroup != null &&currentSubGroup.size()>0 ) {
					logger.info((currentSubGroup.toString()));

				    finalGroups.add(currentSubGroup);
				}
				if (currentGroup != null && currentGroup.size()>0) {
					logger.info((currentGroup.toString()));

					
				    finalGroups.add(currentGroup);
					
				}
				finalString33SubList.addAll(finalGroups);
				int l = 0;

				while (l < finalString45.size() - 8) {
					
					li = finalString45.get(l);

					// int j = i + 1;
					// int indexSub = 0;

					List<String> finalString45sub = new ArrayList<>();
					int n = l;// 45004126184000820
					while (finalString45.get(l + 1).substring(0, 17)
							.equals("4500" + finalString45.get(l + 1).substring(4, 10) + "4000820") == false) {// 412618//45004550404000820
						try {
	
 						// l-finalString45sub.size()
						if (asList("0200", "0420","0422","0220").indexOf(finalString45.get(n).substring(88, 92)) == -1) {

							break;
						}

						finalString45sub.add(finalString45.get(l));
						l = l + 1;
						
						}
						catch (IndexOutOfBoundsException exception)
						{
							throw new Exception ("erreur dans la structure du fichier ligne "+lines.indexOf(finalString45.get(l + 1)),exception);
						}
						catch (Exception exception)
						{
							throw new Exception (exception.getMessage()+lines.indexOf(finalString45.get(l + 1)),exception);
							
						}
						}
				

					if (!finalString45sub.isEmpty()) {
						finalString45sub.add(finalString45.get(l));
						finalString45SubList.add(finalString45sub);
					}

					l = l + 1;
				}
				
				VisaSummary summary =null;
				try {
				summary= new VisaSummary(header, addFileRequest.getDateReg());
				logger.info("start saving summary visa");
				summary=visaSummaryRepository.save(summary);
				logger.info("summary code "+ summary.getSummaryCode());
				}
				catch (IndexOutOfBoundsException e)
				{
					throw new Exception ("erreur dans entete du fichier ",e);
				}
				catch (Exception e)
				{
					throw e;
				}
				List<VisaDisputeStatusAdvice> visaDlist=new ArrayList<>();
				List<ChargebacksInternational> updatedchargebackEmis= new ArrayList<>();
				for(List<String> transaction:finalString33VCR) {
 
 				        VisaDisputeStatusAdvice vds = new VisaDisputeStatusAdvice(transaction,summary.getSummaryCode());
					Optional<ChargebacksInternational> chargebackintenational = chargebacksInternationalRepository.findByRrn(vds.getAcquirerReferenceNumber());
					if (chargebackintenational.isPresent() && Arrays.asList("F1", "L1", "L2", "L3").indexOf(vds.getDisputeStatus()) >= 0) {
						ChargebacksInternational chargebacElement=chargebackintenational.get();
						chargebacElement.setStatusChargeback(1);

						chargebacElement.setDateConfirmation(summary.getSummary_date());
						updatedchargebackEmis.add(chargebacElement);
					}else if(chargebackintenational.isPresent() && Arrays.asList("P1", "R1", "R2", "R3").indexOf(vds.getDisputeStatus()) >= 0) {
						ChargebacksInternational chargebacElement=chargebackintenational.get();
						chargebacElement.setStatusChargeback(4);
						chargebacElement.setDateConfirmation(summary.getSummary_date());
						updatedchargebackEmis.add(chargebacElement);
					}
				        visaDlist.add(vds);
				    
				}
			
				double baseCurrencyToDestinationCurrencyExchangeRate = 1;

				outerLoop: for (List<String> list : finalString05SubList) {

					for (String e : list) {
						li = e;

						if (e.substring(0, 4).equals("0505") && !e.substring(115, 123).equals("00000000")) {
							baseCurrencyToDestinationCurrencyExchangeRate = changeRate(e.substring(115, 123));
							break outerLoop;
						}

					}
				}

				List<TotalVisa> totalVisaV4 = new ArrayList<TotalVisa>();

				for (List<String> list : finalString46SubList) {


					if (list.get(0).contains("V4")) {

						TotalVisa totalVisa = new TotalVisa();
						for (String e : list) {
							li = e;
							try {
								totalVisa.SetV4(summary.getSummaryCode(), e);//, baseCurrency
							} catch (IndexOutOfBoundsException exception) {
								throw new Exception("erreur dans la structure du fichier " + filLines.get(e) + 1, exception);
							} catch (Exception exception) {
								throw new Exception(exception.getMessage() + filLines.get(e) + 1, exception);

							}

						}

						totalVisaV4.add(totalVisa);
					}
				}
				logger.info("start saving visa " + totalVisaV4.size());
				List<TotalVisa> purchaseRecord = totalVisaV4.stream()
						.filter(tv -> "100".equals(tv.getBusinessTransactionType()) &&
								"2".equals(tv.getBusinessMode()) &&
								"120".equals(tv.getReportIdentificationNumber()) &&
								"840".equals(tv.getSettlementCurrencyCode()) &&
								tv.getFundsTransferSreIdentifier().equals(tv.getReportingForSreIdentifier()) &&
								"788".equals(tv.getClearingCurrencyCode()) &&
								//"1".equals(tv.getBusinessTransactionCycle()) &&
//								("07".equals(tv.getSummaryLevel()) || "10".equals(tv.getSummaryLevel())) &&
								("05".equals(tv.getSummaryLevel())) &&
								Double.parseDouble(tv.getThirdAmount()) != 0).collect(Collectors.toList());
				List<TotalVisa> purchaseRecordCasiCash = totalVisaV4.stream()
						.filter(tv -> "100".equals(tv.getBusinessTransactionType()) &&
								"2".equals(tv.getBusinessMode()) &&
								"120".equals(tv.getReportIdentificationNumber()) &&
								"840".equals(tv.getSettlementCurrencyCode()) &&
								tv.getFundsTransferSreIdentifier().equals(tv.getReportingForSreIdentifier()) &&
								"788".equals(tv.getClearingCurrencyCode()) &&
								//"1".equals(tv.getBusinessTransactionCycle()) &&
//								("07".equals(tv.getSummaryLevel()) || "10".equals(tv.getSummaryLevel())) &&
								("05".equals(tv.getSummaryLevel())) &&
								Double.parseDouble(tv.getThirdAmount()) != 0).collect(Collectors.toList());

				List<TotalVisa> retraitRecord = totalVisaV4.stream()
						.filter(tv -> "310".equals(tv.getBusinessTransactionType()) &&
								"2".equals(tv.getBusinessMode()) &&
								"120".equals(tv.getReportIdentificationNumber()) &&
								"840".equals(tv.getSettlementCurrencyCode()) &&
								tv.getFundsTransferSreIdentifier().equals(tv.getReportingForSreIdentifier()) &&
								"788".equals(tv.getClearingCurrencyCode()) &&
								//"1".equals(tv.getBusinessTransactionCycle()) &&
//								("07".equals(tv.getSummaryLevel()) || "10".equals(tv.getSummaryLevel())) &&
								("05".equals(tv.getSummaryLevel())) &&
								Double.parseDouble(tv.getThirdAmount()) != 0).collect(Collectors.toList());
				double valueRate = 0.0;
				HashMap<String, Double> mapValueRate = new HashMap<String, Double>();

				if (purchaseRecord.size() > 0) {
					TotalVisa tv = purchaseRecord.get(0);
					Double thirdAmount = Double.parseDouble(tv.getThirdAmount()) - Double.parseDouble(tv.getSecondAmount());
					Double firstAmount = Double.parseDouble(tv.getFirstAmount()) / 10;
					logger.info("Purchase firstAmount" + firstAmount);
					 logger.info("Purchase secondAmount" + thirdAmount);
					if (purchaseRecordCasiCash.size() > 0) {
						TotalVisa casicash = purchaseRecordCasiCash.get(0);
						thirdAmount = thirdAmount + Double.parseDouble(casicash.getThirdAmount());
						firstAmount = firstAmount + Double.parseDouble(casicash.getFirstAmount()) / 10;
						logger.info("casiCash firstAmount" + firstAmount);
						logger.info("casiCash thirdAmount" + thirdAmount);
					}
					valueRate = thirdAmount / firstAmount;


					logger.info("Purchase rate : " + valueRate);
					mapValueRate.put("Purchase", valueRate);

				} else {
					mapValueRate.put("Purchase", baseCurrencyToDestinationCurrencyExchangeRate);

				}
				if (retraitRecord.size() > 0) {
					TotalVisa tv = retraitRecord.get(0);
					valueRate = (Double.parseDouble(tv.getThirdAmount()) - Double.parseDouble(tv.getSecondAmount())) / (Double.parseDouble(tv.getFirstAmount()) / 10);
					 logger.info("Retrait firstAmount" + Double.parseDouble(tv.getFirstAmount()));
					 logger.info("Retrait thirdAmount" + Double.parseDouble(tv.getThirdAmount()));
					 logger.info("Retrait rate : " + valueRate);

					mapValueRate.put("Withdraw", valueRate);

				} else {
					mapValueRate.put("Withdraw", baseCurrencyToDestinationCurrencyExchangeRate);

				}
				mapValueRate.put("All", baseCurrencyToDestinationCurrencyExchangeRate);


				List<VisaIncoming> visaIncomingList = new ArrayList<VisaIncoming>();
				for (List<String> list : finalString05SubList) {
					 logger.info("new trans");

					VisaIncoming visaIncoming = new VisaIncoming();
					for (String e : list) {
						li = e;


						try {
							 logger.info(e);
							visaIncoming.visaIncomingSetter(e, summary.getSummaryCode(), mapValueRate);//, baseCurrency

						}
						catch (IndexOutOfBoundsException exception )
						{
							throw new Exception ("erreur dans la structure du fichier données visa incoming " + (filLines.get(e)+1) ,exception);
						}
						catch (Exception exception ) {
							throw new Exception (exception.getMessage() + (filLines.get(e)+1) ,exception);

					}
					}

					visaIncomingList.add(visaIncoming);
				}

				logger.info("end saving visa incoming ");
				List<ChargebackVisa> chargebackVisaList = new ArrayList<ChargebackVisa>();
				List<ChargebacksInternational> chargebacklist = new ArrayList<ChargebacksInternational>();
				for (List<String> list : finalString15SubList) {
					ChargebackVisa chargebackVisa = new ChargebackVisa();
					for (String e : list) {
						li = e;
						try {
						chargebackVisa.visaIncomingSetter(e, summary.getSummaryCode(),baseCurrencyToDestinationCurrencyExchangeRate);//, baseCurrency
						ChargeBackHistoryInternational  historiqueInter = new ChargeBackHistoryInternational(chargebackVisa);
						historiqueInter.setChargeBackType(ChargeBackType.VISA);
						Optional<ChargebacksInternational> chargeabckEmisVisa = chargebacksInternationalRepository.findByRrn(chargebackVisa.getAcquirerReferenceNumber());
						if(chargeabckEmisVisa.isPresent()){
							ChargebacksInternational chargebacktoUpdate=chargeabckEmisVisa.get();
							if(chargeabckEmisVisa.get().getTypeDossier() != null)
								chargebacktoUpdate.setTypeDossier(chargeabckEmisVisa.get().getTypeDossier());
							else
								chargebacktoUpdate.setTypeDossier(TypeDossier.EMIS);
							chargebacktoUpdate.setStatusChargeback(1);
							chargebacktoUpdate.setDateConfirmation(summary.getSummary_date());

							chargebacklist.add(chargebacktoUpdate);
						}else{
							historiqueInter.setTypeDossier(TypeDossier.RECU);
						}
						chargeBackHistoryInternationals.add(historiqueInter);

						}
						catch (IndexOutOfBoundsException exception )
						{
							throw new Exception ("erreur dans la structure du fichier ligne "+(filLines.get(e)+1),exception);
						}
						catch (Exception exception )
						{
							throw new Exception (exception.getMessage()+(filLines.get(e)+1),exception);

						}
					}
					chargebackVisaList.add(chargebackVisa);
				}
				 logger.info("start saving chargebacklist"+ chargebacklist.size());
				chargebacksInternationalRepository.saveAll(chargebacklist);

				logger.info("start saving charge back visa  "+chargebackVisaList.size());

				logger.info("end saving charge back visa ");


				logger.info("end saving visa ");
				 
				List<VisaOutGoingAtm> visaOutGoingAtmList = new ArrayList<VisaOutGoingAtm>();
				for (List<String> list:finalString33SubList)  {
					VisaOutGoingAtm visaOutGoingAtm = new VisaOutGoingAtm(summary.getSummaryCode());
					for (String e:list)
					{
						try {
						visaOutGoingAtm.visaOutGoingAtmSetter(e);
						}
						catch (IndexOutOfBoundsException exception)
						{
							throw new Exception("erreur dans la structure du fichier ligne"+(filLines.get(e)+1),exception);
						}
						catch (Exception exception)
						{
							throw new Exception (exception.getMessage()+(filLines.get(e)+1),exception);

						}
						}
					if(visaOutGoingAtm.getCardNumber()!=null) {
					visaOutGoingAtmList.add(visaOutGoingAtm);}
				}
				logger.info("start saving visa atm de taille "+visaOutGoingAtmList.size());
				
				
				logger.info("end saving visa atm ");
		
				List<Tc45> visaOutComingAtmList = new ArrayList<Tc45>();
				for (List<String> list :finalString45SubList) {
					Tc45 visaOutComingAtm = new Tc45();
					for (String e:list ) {
						try {
							 
						visaOutComingAtm.tc45Setter(e, summary.getSummaryCode());
					 
						}
						catch (IndexOutOfBoundsException exception)
						{
							throw new Exception ("erreur dans la structure du fichier "+(filLines.get(e)+1),exception);

						}
						catch (Exception exception)
						{
							throw new Exception (exception.getMessage()+(filLines.get(e)+1),exception);

						}
						}
					visaOutComingAtmList.add(visaOutComingAtm);

				};
				logger.info("start saving tc45"+visaOutComingAtmList.size());

				logger.info("end saving tc45");
				List<FraisVisaIncoming> ListfraisVisa = new ArrayList<FraisVisaIncoming>();
				for (String fraisVisa : finalString10) {
                       
					 	try { 
					 		FraisVisaIncoming frais=new FraisVisaIncoming(fraisVisa,summary.getSummaryCode());
					 	

                       ListfraisVisa.add(frais);
					 	}
					 	catch (IndexOutOfBoundsException e)
					 	{
							throw new Exception ("erreur dans la structure du fichier '10' "+(filLines.get(fraisVisa)+1),e);

					 	}
					 	catch (Exception e)
					 	{
							throw new Exception (e.getMessage()+(filLines.get(fraisVisa)+1),e);

					 	}
					 	
					 	}
				for (List<String> fraisVisa : finalString20Data) {
					
					try { 
						FraisVisaIncoming frais=new FraisVisaIncoming(fraisVisa,summary.getSummaryCode());
						
						
						ListfraisVisa.add(frais);
					}
					catch (IndexOutOfBoundsException e)
					{
						throw new Exception ("erreur dans la structure du fichier '20' "+(filLines.get(fraisVisa)+1),e);
						
					}
					catch (Exception e)
					{
						throw new Exception (e.getMessage()+(filLines.get(fraisVisa)+1),e);
						
					}
					
				}

				List<VisaV2> listV2 = new ArrayList<VisaV2>();
				
				for (String Visa : listData46V2) {
					VisaV2 v2=new VisaV2();
					 	try {
					 	    v2.visaSetter(Visa,summary.getSummaryCode());


					 	   listV2.add(v2);
					 	}
					 	catch (IndexOutOfBoundsException e)
					 	{
							throw new Exception ("erreur dans la structure du fichier "+(filLines.get(v2)+1),e);

					 	}
					 	catch (Exception e)
					 	{
							throw new Exception (e.getMessage()+(filLines.get(v2)+1),e);

					 	}

					 	}


				logger.info("start saving frais visa incoming " +ListfraisVisa.size());
				int tc04 = 0;
				while (tc04 < listData04.size()) {
					// int j = i + 1;
					li = listData04.get(tc04);

					List<String> finalString04sub = new ArrayList<>();
					// finalString33sub.add(listData.get(i));
					while ((tc04 < listData04.size() - 1)
							&& (Integer.parseInt(listData04.get(tc04).substring(2, 4)) < Integer
									.parseInt(listData04.get(tc04 + 1).substring(2, 4)))) {

						finalString04sub.add(listData04.get(tc04));
						tc04 = tc04 + 1;
					//	logger.info("isecond =>{}",chargeback);

					}
					finalString04sub.add(listData04.get(tc04));
					finalString04SubList.add(finalString04sub);
					tc04 = tc04 + 1;
					logger.info("ithord =>{}", tc04);

				}
				List<Tc04> tc04List = new ArrayList<Tc04>();
				for (List<String> list : finalString04SubList) {
					Tc04 tc = new Tc04();
					for (String e : list) {
						li = e;
						try {
							tc.visaIncomingSetter(summary.getSummaryCode(),e);//, baseCurrency
						}
						catch (IndexOutOfBoundsException exception )
						{
							throw new Exception ("erreur dans la structure du fichier ligne "+(filLines.get(e)+1),exception);
						}
						catch (Exception exception )
						{
							throw new Exception (exception.getMessage()+(filLines.get(e)+1),exception);

						}
					}
					tc04List.add(tc);
				}
				List<VisaOutGoingAtm> listchargebackAtm=visaOutGoingAtmList.stream()
		                .filter(item -> "0422".equals(item.getRequestMessageType()) || "0220".equals(item.getRequestMessageType()))
		                .collect(Collectors.toList());
				List<ChargeBackVisaOutgoing>  ch=new ArrayList<>()	;
				for (VisaOutGoingAtm element:listchargebackAtm) {
 					// archive day operation

						ChargeBackVisaOutgoing  historique = new ChargeBackVisaOutgoing();
						try {
							PropertyUtils.copyProperties(historique, element);
							List<Tc45> tc45Filtered = visaOutComingAtmList.stream().filter(el ->
									el.getRetrievalRefNumber().equals(historique.getRetrRefNumber()) &&
									el.getTranType().equals(historique.getRequestMessageType()) &&
									el.getCardNumber().equals(historique.getCardNumber())).collect(Collectors.toList());
							if(tc45Filtered.size() > 0){
								Tc45 tc45 = tc45Filtered.get(0);
								ChargeBackHistoryInternational  historiqueInter = new ChargeBackHistoryInternational(historique,tc45,2);
								historiqueInter.setChargeBackType(ChargeBackType.VISA);
								historiqueInter.setTypeDossier(TypeDossier.RECU);
								chargeBackHistoryInternationals.add(historiqueInter);
							}
						} catch (Exception ex) {
							String stackTrace = Throwables.getStackTraceAsString(ex);
							throw new Exception ("erreur dans la structure du   "+stackTrace);
						}
						 ch.add(historique);
					
				}

				traitementVisaDisputeCharge(totalVisaV4, visaDlist);
				traitementVisaDisputeRep(totalVisaV4, visaDlist);
				 logger.info("" + visaDlist.size());


				 logger.info("start saving");
				chargebackVisaList.forEach(visaCb -> visaCb.setFileDate(addFileRequest.getDateReg()));
				visaDlist.forEach(visaCb -> visaCb.setFileDate(addFileRequest.getDateReg()));
				visaOutGoingAtmList.forEach(visaCb -> visaCb.setFileDate(addFileRequest.getDateReg()));

				visaV2Repo.saveAll(listV2);
				visaIncomingRepository.saveAll(visaIncomingList);
				chargebackVisaRepo.saveAll(chargebackVisaList);
				totalVisaRepo.saveAll(totalVisaV4);
				visaOutComingAtmRepository.saveAll(visaOutGoingAtmList);
				tc45Repository.saveAll(visaOutComingAtmList);
				fraisVisaIncomingRepo.saveAll(ListfraisVisa);
				tc04Repo.saveAll(tc04List);
				visaDisputeStatusAdviceRepository.saveAll(visaDlist);
				chargeBackHistoryInterRepository.saveAll(chargeBackHistoryInternationals);
				totalVisaRepo.flush();
				visaSummaryRepository.flush();


				//li
				if(summary!=null) {
					 logger.info("start calc "+summary.getSummary_date());
 				 generatedataEUR(summary.getSummary_date());

				}
				response.setStatus(true);
				logger.info("End visa.");
				
				}

			} 

			
		 else {
			response.setStatus(false);
			response.setMessage("File not found!");
			logger.info("File not found!");
		}
		return response;
		}


	private double changeRate(String change) {
		int number = Integer.parseInt(change.substring(0, 2));
		if (number == 0)
			return 1.0;

		String changeRate = change.substring(2);
		double res = Double.parseDouble(changeRate) / Math.pow(10, number);
		return res;
	}

	public BigDecimal sumOfSettlementsDispute(List<VisaDisputeStatusAdvice> octfs) {
	    return octfs.stream()
	                .map(octf -> BigDecimal.valueOf(octf.getSettlementCalculate()))
	                .reduce(BigDecimal.ZERO, BigDecimal::add);
	}

 	public BigDecimal sumOfInterchangesDispute(List<VisaDisputeStatusAdvice> octfs) {
	    return octfs.stream()
	                .map(octf -> BigDecimal.valueOf(octf.getInterchangeCalculat()))
	                .reduce(BigDecimal.ZERO, BigDecimal::add);
	}

    private Optional<TotalVisa> filterTotalVisa(List<TotalVisa> totalVisa, String currencyCode) {
    	 List<TotalVisa> filteredVisas = totalVisa.stream()
                .filter(e -> e.getBusinessMode().equals("1") &&
                        e.getClearingCurrencyCode().equals(currencyCode) &&
                        e.getSummaryLevel().equals("10") &&
                        e.getReportingForSreIdentifier().equals("0008456894")
                        && e.getFundsTransferSreIdentifier().equals("0008456894") 
                        && e.getBusinessTransactionType().equals("100")
                        && e.getReversalIndicator().equals("N")
       					&& (!e.getBusinessTransactionCycle().equals("8") && !e.getBusinessTransactionCycle().equals("3") )
                        && e.getSettlementCurrencyCode().equals("840")
                        && e.getFirstAmountSign().equals("CR")
                        )
                .collect(Collectors.toList());
        
    	 Optional<TotalVisa> total = filteredVisas.stream().map(visa ->
         new TotalVisa(visa.getFirstCount(),visa.getFirstAmount(),visa.getSecondAmount(),visa.getThirdAmount())
                  
         ).reduce((acc, curr) -> {
     // Accumulate the necessary values
     acc.setFirstCount(String.valueOf(Double.parseDouble(acc.getFirstCount()) + Double.parseDouble(curr.getFirstCount())));
     acc.setFirstAmount(String.valueOf(Double.parseDouble(acc.getFirstAmount()) + Double.parseDouble(curr.getFirstAmount())));
     acc.setSecondAmount(String.valueOf(Double.parseDouble(acc.getSecondAmount()) + Double.parseDouble(curr.getSecondAmount())));
     return acc;
             });
 
          return total;
    }
    
    
    private Optional<TotalVisa> filterTotalVisaReturned(List<TotalVisa> totalVisa, String currencyCode) {
   	 List<TotalVisa> filteredVisas = totalVisa.stream()
               .filter(e -> e.getBusinessMode().equals("1") &&
                       e.getClearingCurrencyCode().equals(currencyCode) &&
                       e.getSummaryLevel().equals("10") &&
                       e.getReportingForSreIdentifier().equals("0008456894")
                       && e.getFundsTransferSreIdentifier().equals("0008456894") 
                       && e.getBusinessTransactionType().equals("100")
                       && e.getReversalIndicator().equals("N")
                       && e.getSettlementCurrencyCode().equals("840")
                       && e.getFirstAmountSign().equals("DB")
                       && e.getReturnIndicator().equals("Y")
                       
                       )
               .collect(Collectors.toList());
       
   	 Optional<TotalVisa> total = filteredVisas.stream().map(visa ->
     new TotalVisa(visa.getFirstCount(),visa.getFirstAmount(),visa.getSecondAmount(),visa.getThirdAmount())
              
     ).reduce((acc, curr) -> {
 // Accumulate the necessary values
 acc.setFirstCount(String.valueOf(Double.parseDouble(acc.getFirstCount()) + Double.parseDouble(curr.getFirstCount())));
 acc.setFirstAmount(String.valueOf(Double.parseDouble(acc.getFirstAmount()) + Double.parseDouble(curr.getFirstAmount())));
 acc.setThirdAmount(String.valueOf(Double.parseDouble(acc.getThirdAmount()) + Double.parseDouble(curr.getThirdAmount())));

 return acc;
         });
   	   if(total.isPresent()) {
   		 logger.info("totalpresent=>{}",total.get().getFirstAmount());
   		logger.info("totalpresent=>{}",total.get().getThirdAmount());
   	   }
         
         return total;
   }
    
    private Optional<TotalVisa> filterTotalVisaReturnedReversal(List<TotalVisa> totalVisa, String currencyCode) {
   	 List<TotalVisa> filteredVisas = totalVisa.stream()
               .filter(e -> e.getBusinessMode().equals("1") &&
                       e.getClearingCurrencyCode().equals(currencyCode) &&
                       e.getSummaryLevel().equals("10") &&
                       e.getReportingForSreIdentifier().equals("0008456894")
                       && e.getFundsTransferSreIdentifier().equals("0008456894") 
                       && e.getBusinessTransactionType().equals("100")
                       && e.getReversalIndicator().equals("Y")
                       && e.getSettlementCurrencyCode().equals("840")
                       && e.getFirstAmountSign().equals("CR")
                       && e.getReturnIndicator().equals("Y")
                       
                       )
               .collect(Collectors.toList());
       
   	 Optional<TotalVisa> total = filteredVisas.stream().map(visa ->
     new TotalVisa(visa.getFirstCount(),visa.getFirstAmount(),visa.getSecondAmount(),visa.getThirdAmount())
              
     ).reduce((acc, curr) -> {
 // Accumulate the necessary values
 acc.setFirstCount(String.valueOf(Integer.parseInt(acc.getFirstCount()) + Integer.parseInt(curr.getFirstCount())));
 acc.setFirstAmount(String.valueOf(Integer.parseInt(acc.getFirstAmount()) + Integer.parseInt(curr.getFirstAmount())));
 acc.setThirdAmount(String.valueOf(Integer.parseInt(acc.getThirdAmount()) + Integer.parseInt(curr.getThirdAmount())));

 return acc;
         });
   	   if(total.isPresent()) {
   		 logger.info("totalpresent=>{}",total.get().getFirstAmount());
   		logger.info("totalpresent=>{}",total.get().getThirdAmount());
   	   }
         
         return total;
   }
    private List<TotalVisa> filterInterchangeReversalVisa(List<TotalVisa> totalVisa, String currencyCode) {
        return totalVisa.stream()
                .filter(e -> e.getReportingForSreIdentifier().equals(e.getFundsTransferSreIdentifier())
                		&& e.getBusinessMode().equals("1") 
                		&& e.getClearingCurrencyCode().equals(currencyCode) 
                        && e.getBusinessTransactionType().equals("100")
                        && e.getReversalIndicator().equals("Y")
                        && e.getReturnIndicator().contentEquals("N")
                        && e.getFundsTransferSreIdentifier().equals(e.getReportingForSreIdentifier())
                        && e.getBusinessMode().equals("1")
                        && e.getSettlementCurrencyCode().equals("840")
                        && e.getBusinessTransactionCycle().equals("1")
                        ).collect(Collectors.toList());
    }
	private Optional<TotalVisa> filterInterchangeRefundVisa(List<TotalVisa> totalVisa) {
       return totalVisa.stream()
                .filter(e ->
                        e.getReportIdentificationNumber().equals("130")
                		&& e.getBusinessMode().equals("1")
                        && e.getBusinessTransactionType().equals("200")
                        && e.getFundsTransferSreIdentifier().equals(e.getReportingForSreIdentifier())
                        && e.getBusinessMode().equals("1")
                        && e.getSettlementCurrencyCode().equals("840")
                        &&e.getSummaryLevel().equals("05")
                        ). findFirst();
    }

    private Double calculateTotalAmount(List<TotalVisa> totalVisa) {
        return totalVisa.stream()
                .filter(e -> e.getBusinessMode().equals("1") &&
                        (e.getClearingCurrencyCode().equals("840") ||
                         e.getClearingCurrencyCode().equals("978") ||
                         e.getClearingCurrencyCode().equals("788")) &&
                        e.getSummaryLevel().equals("10") &&
                        e.getReportingForSreIdentifier().equals("0008456894")
                        && e.getFundsTransferSreIdentifier().equals("0008456894")
                        && e.getBusinessTransactionType().equals("100")
                        && e.getReversalIndicator().equals("N")
                        && e.getSettlementCurrencyCode().equals("840")
                        && e.getFirstAmountSign().equals("CR")
                        )
                .mapToDouble(e -> Double.parseDouble(e.getSecondAmount()))
                .sum();
    }
    
    private Double calculateTotalAmountReturned(List<TotalVisa> totalVisa) {
        return totalVisa.stream()
                .filter(e-> e.getBusinessMode().equals("1") &&
                		 (e.getClearingCurrencyCode().equals("840") ||
                          e.getClearingCurrencyCode().equals("978") ||
                          e.getClearingCurrencyCode().equals("788")) &&
                        e.getSummaryLevel().equals("10") &&
                        e.getReportingForSreIdentifier().equals("0008456894")
                        && e.getFundsTransferSreIdentifier().equals("0008456894")
                        && e.getBusinessTransactionType().equals("100")
                        && e.getReversalIndicator().equals("N")
                        && e.getSettlementCurrencyCode().equals("840")
                        && e.getFirstAmountSign().equals("DB")
                        && e.getReturnIndicator().equals("Y")
                        )
              
                .mapToDouble(e -> Double.parseDouble(e.getThirdAmount()))
                .sum();
    }

    private Double calculateTotalAmountInterchange(List<TotalVisa> totalVisa) {
		Double total = totalVisa.stream()
                .filter(e -> e.getReportingForSreIdentifier().equals(e.getFundsTransferSreIdentifier())
                		&& e.getBusinessMode().equals("1") &&
						(e.getSummaryLevel().equals("07")) &&
						e.getBusinessTransactionCycle().equals("1") &&
                        e.getReportIdentificationNumber().equals("120")
                        && e.getBusinessTransactionType().equals("100")
                        && e.getReversalIndicator().equals("Y")
                        && e.getSettlementCurrencyCode().equals("840"))
                .mapToDouble(e -> Double.parseDouble(e.getThirdAmount()))
                .sum();
		if (total == 0) {
			total = totalVisa.stream()
					.filter(e -> e.getReportingForSreIdentifier().equals(e.getFundsTransferSreIdentifier())
							&& e.getBusinessMode().equals("1") &&
							(e.getSummaryLevel().equals("10")) &&
							e.getBusinessTransactionCycle().equals("1") &&
							e.getReportIdentificationNumber().equals("120")
							&& e.getBusinessTransactionType().equals("100")
							&& e.getReversalIndicator().equals("Y")
							&& e.getSettlementCurrencyCode().equals("840"))
					.mapToDouble(e -> Double.parseDouble(e.getThirdAmount()))
					.findFirst()
					.orElse(0.0);
		}
		return total;
    }

	private Double calculateTotalAmountInterchangebyC(List<TotalVisa> totalVisa) {
		Double total = totalVisa.stream()
				.filter(e -> e.getReportingForSreIdentifier().equals(e.getFundsTransferSreIdentifier())
						&& e.getBusinessMode().equals("1") &&
						(e.getSummaryLevel().equals("10")) &&
						e.getBusinessTransactionCycle().equals("1") &&
						e.getReportIdentificationNumber().equals("120")
						&& e.getBusinessTransactionType().equals("100")
						&& e.getReversalIndicator().equals("Y")
						&& e.getSettlementCurrencyCode().equals("840"))
				.mapToDouble(e -> Double.parseDouble(e.getThirdAmount()))
				.sum();
		if (total == 0) {
			total = totalVisa.stream()
					.filter(e -> e.getReportingForSreIdentifier().equals(e.getFundsTransferSreIdentifier())
							&& e.getBusinessMode().equals("1") &&
							(e.getSummaryLevel().equals("10")) &&
							e.getBusinessTransactionCycle().equals("1") &&
							e.getReportIdentificationNumber().equals("120")
							&& e.getBusinessTransactionType().equals("100")
							&& e.getReversalIndicator().equals("Y")
							&& e.getSettlementCurrencyCode().equals("840"))
					.mapToDouble(e -> Double.parseDouble(e.getThirdAmount()))
					.findFirst()
					.orElse(0.0);
		}
		return total;
	}


	private Double calculateTotalAmountInterchangeR(List<TotalVisa> totalVisa) {

		Double total = totalVisa.stream()
				.filter(e -> e.getReportingForSreIdentifier().equals(e.getFundsTransferSreIdentifier())
						&& e.getBusinessMode().equals("1") &&
						e.getReportIdentificationNumber().equals("130")
						&& e.getBusinessTransactionType().equals("100")
						&& e.getReversalIndicator().equals("Y")
						&& e.getSettlementCurrencyCode().equals("840")
						&& e.getBusinessTransactionCycle().equals("1"))
				.mapToDouble(e -> Double.parseDouble(e.getSecondAmount()))
				.sum();

		return total;
	}

    private Optional<TotalVisa> filterInterchangeVisa(List<TotalVisa> totalVisa) {
        return totalVisa.stream()
                .filter(e -> e.getBusinessMode().equals("1") &&
                        e.getSummaryLevel().equals("05") &&
                        e.getFundsTransferSreIdentifier().equals("0008456894")
                       && e.getReportingForSreIdentifier().equals("0006456894")
                        && e.getBusinessTransactionType().equals("100")
                        && e.getReportIdentificationNumber().equals("130")
                        && e.getSettlementCurrencyCode().equals("840")
                        )
                .findFirst();
    }

	private Double filterReversalInterchange(List<TotalVisa> totalVisa) {
		//modif vue avec wisal  summary_level='10' , reversal_indicator='Y' ;
		// 
        return totalVisa.stream()
                .filter(e -> e.getReportingForSreIdentifier().equals(e.getFundsTransferSreIdentifier())
                		&& e.getBusinessMode().equals("1") &&
								e.getReversalIndicator().equals("Y")
								&& e.getSummaryLevel().equals("10") &&
                        e.getReportIdentificationNumber().equals("130")
                        && e.getBusinessTransactionType().equals("100")
                        && e.getSettlementCurrencyCode().equals("840")
                        )

				.mapToDouble(e -> Double.parseDouble(e.getSecondAmount()))
				.sum();
    }


	private double logTotalAndParseCount(Optional<TotalVisa> totalAcq, String currency) {
        double totalCount = totalAcq.map(e -> Double.parseDouble(e.getFirstCount())).orElse(0.0);
    
        logger.info("totalCount=>{}", totalCount);
       
        return totalCount;
    }
	
	

    private double logTotalAndParseCount(List<TotalVisa> totalAcq, String currency) {
        double totalCount = totalAcq.stream()
                                 .mapToDouble(e -> Double.parseDouble(e.getFirstCount())) // Convert each getFirstCount() to int
                                 .sum(); // Sum all values

        logger.info("Currency: {} | Total Count: {}", currency, totalCount);

        return totalCount;
    }
    private void traitementVisaDisputeRep(List<TotalVisa> totalVisa,List<VisaDisputeStatusAdvice> disputes) throws Exception{
    	if (disputes.size()>0) {
    	List<VisaDisputeStatusAdvice> disputeA=disputes.stream().filter(e->(!"6011".equals(e.getMerchantCategoryCode()) && !"6010".equals(e.getMerchantCategoryCode()) && !"F1".equals(e.getDisputeStatus())  )).collect(Collectors.toList());
    	List<VisaDisputeStatusAdvice> disputeR=disputes.stream().filter(e->(("6011".equals(e.getMerchantCategoryCode()) || "6010".equals(e.getMerchantCategoryCode())) && !"F1".equals(e.getDisputeStatus())  )).collect(Collectors.toList());
    	if(disputeA.size()>0)
    		handleRepAchat(totalVisa,disputeA);
    	if(disputeR.size()>0)
    		handleRepRetrait(totalVisa,disputeR);
    	}
    	logger.info("disputes size =>{}",disputes.size());
    }
    
    private void handleRepRetrait(List<TotalVisa> totalVisa, List<VisaDisputeStatusAdvice> disputes) throws Exception {
    	Optional<TotalVisa> disputeAmountE=totalVisa.stream()
    			.filter(e -> e.getBusinessMode().equals("2") &&
    					e.getReportingForSreIdentifier().equals(e.getFundsTransferSreIdentifier())&&
    					e.getReversalIndicator().equals("Y")&&
    					e.getReturnIndicator().equals("N")
    					&& (e.getBusinessTransactionCycle().equals("8") || e.getBusinessTransactionCycle().equals("3") )

    					&& e.getBusinessTransactionType().equals("310")
    					)
    			.findFirst();
    	
    	Optional<TotalVisa> disputeInterchangeE=totalVisa.stream()
    			.filter(e -> e.getBusinessMode().equals("2") &&
    					e.getReportingForSreIdentifier().equals(e.getFundsTransferSreIdentifier())&&
    					e.getReversalIndicator().equals("Y")&&
    					e.getReportIdentificationNumber().equals("130")
    					&& (e.getBusinessTransactionCycle().equals("8") || e.getBusinessTransactionCycle().equals("3") )

    					&& e.getBusinessTransactionType().equals("310")
    					
    					)
    			.findFirst();
    	Optional<TotalVisa> disputeAmountR=totalVisa.stream()
    			.filter(e -> e.getBusinessMode().equals("1") &&
    					e.getReportingForSreIdentifier().equals(e.getFundsTransferSreIdentifier())&&
    					e.getReversalIndicator().equals("N")&&
    					e.getReturnIndicator().equals("N")
    					&& e.getBusinessTransactionType().equals("310")
    					&& e.getSettlementCurrencyCode().equals("840")
    					&& (e.getBusinessTransactionCycle().equals("8") || e.getBusinessTransactionCycle().equals("3") )
    					&& e.getReportIdentificationNumber().equals("120")
    					
    					)
    			.findFirst();
    	
    	Optional<TotalVisa> disputeInterchangeR=totalVisa.stream()
    			.filter(e -> e.getBusinessMode().equals("1") &&
    					e.getReportingForSreIdentifier().equals(e.getFundsTransferSreIdentifier())
    					&& (e.getBusinessTransactionCycle().equals("8") || e.getBusinessTransactionCycle().equals("3") )
    					&& e.getSettlementCurrencyCode().equals("840")
    					&& e.getReversalIndicator().equals("N")
    					&& e.getReportIdentificationNumber().equals("130")
    					&& e.getBusinessTransactionType().equals("310")
						&& (e.getSummaryLevel().equals("08") || e.getSummaryLevel().equals("10"))) // Ensure only "08" and "10" are considered
        				.sorted(Comparator.comparing(TotalVisa::getSummaryLevel)) // Sort by summary level
				.findFirst();
    	 logger.info(disputes.toString());
    	
    	List<VisaDisputeStatusAdvice> disputeE=disputes.stream().filter(e->(!e.getCardAcceptorId().startsWith("05")  )).collect(Collectors.toList());
    	List<VisaDisputeStatusAdvice> disputeR=disputes.stream().filter(e->(e.getCardAcceptorId().startsWith("05")    )).collect(Collectors.toList());
    	 logger.info(disputeE.toString());
    	 logger.info(disputeR.toString());
    	 logger.info(disputeAmountE.toString());
    	 logger.info(disputeInterchangeE.toString());
    	 logger.info(disputeAmountR.toString());
    	 logger.info(disputeInterchangeR.toString());
    
    	caulsuleVisaDsiputeE(disputeAmountE,disputeInterchangeE,disputeE,"REP");
    	caulsuleVisaDsiputeR(disputeAmountR,disputeInterchangeR,disputeR,"REP");
		
	}

	private void handleRepAchat(List<TotalVisa> totalVisa, List<VisaDisputeStatusAdvice> disputes) throws Exception {
		Optional<TotalVisa> disputeAmountE =totalVisa.stream()
    			.filter(e -> e.getBusinessMode().equals("2") &&
    					e.getReportingForSreIdentifier().equals(e.getFundsTransferSreIdentifier())&&
    					e.getReversalIndicator().equals("Y")&&
    					e.getReturnIndicator().equals("N")
								&& (e.getBusinessTransactionCycle().equals("8") || e.getBusinessTransactionCycle().equals("3") )

    					&& e.getBusinessTransactionType().equals("100")
    					)
    			.findFirst();
    	
    	Optional<TotalVisa> disputeInterchangeE=totalVisa.stream()
    			.filter(e -> e.getBusinessMode().equals("2") &&
    					e.getReportingForSreIdentifier().equals(e.getFundsTransferSreIdentifier())&&
    					e.getReversalIndicator().equals("Y")&&
    					e.getReportIdentificationNumber().equals("130")
								&& (e.getBusinessTransactionCycle().equals("8") || e.getBusinessTransactionCycle().equals("3") )
    					&& e.getBusinessTransactionType().equals("100")
    					
    					)
    			.findFirst();
		Optional<TotalVisa> disputeAmountER2 =totalVisa.stream()
    			.filter(e -> e.getBusinessMode().equals("2") &&
    					e.getReportingForSreIdentifier().equals(e.getFundsTransferSreIdentifier())&&
    					e.getReversalIndicator().equals("Y")&&
    					e.getReturnIndicator().equals("N")
								&& (e.getBusinessTransactionCycle().equals("7") || e.getBusinessTransactionCycle().equals("2") )

    					&& e.getBusinessTransactionType().equals("100")
    					)
    			.findFirst();

    	Optional<TotalVisa> disputeInterchangeER2=totalVisa.stream()
    			.filter(e -> e.getBusinessMode().equals("2") &&
    					e.getReportingForSreIdentifier().equals(e.getFundsTransferSreIdentifier())&&
    					e.getReversalIndicator().equals("Y")&&
    					e.getReportIdentificationNumber().equals("130")
								&& (e.getBusinessTransactionCycle().equals("7") || e.getBusinessTransactionCycle().equals("2") )
    					&& e.getBusinessTransactionType().equals("100")

    					)
    			.findFirst();


    	Optional<TotalVisa> disputeAmountR=totalVisa.stream()
    			.filter(e -> e.getBusinessMode().equals("1") &&
    					e.getReportingForSreIdentifier().equals(e.getFundsTransferSreIdentifier())&&
    					e.getReversalIndicator().equals("N")&&
    					e.getReturnIndicator().equals("N")
    					&& e.getBusinessTransactionType().equals("100")
    					&& e.getSettlementCurrencyCode().equals("840")
    					&& (e.getBusinessTransactionCycle().equals("8") || e.getBusinessTransactionCycle().equals("3") )
    					&& e.getReportIdentificationNumber().equals("120")
    					
    					)
    			.findFirst();
    	
    	Optional<TotalVisa> disputeInterchangeR=totalVisa.stream()
    			.filter(e -> e.getBusinessMode().equals("1") &&
    					e.getReportingForSreIdentifier().equals(e.getFundsTransferSreIdentifier())
    					&& (e.getBusinessTransactionCycle().equals("8") || e.getBusinessTransactionCycle().equals("3") )
    					&& e.getSettlementCurrencyCode().equals("840")
    					&& e.getReversalIndicator().equals("N")
    					&& e.getReportIdentificationNumber().equals("130")
    					&& e.getBusinessTransactionType().equals("100")
						&& (e.getSummaryLevel().equals("08") || e.getSummaryLevel().equals("10"))) // Ensure only "08" and "10" are considered
				.sorted(Comparator.comparing(TotalVisa::getSummaryLevel)) // Sort by summary level
				.findFirst();
    	Optional<TotalVisa> disputeAmountRR2=totalVisa.stream()
    			.filter(e -> e.getBusinessMode().equals("1") &&
    					e.getReportingForSreIdentifier().equals(e.getFundsTransferSreIdentifier())&&
    					e.getReversalIndicator().equals("N")&&
    					e.getReturnIndicator().equals("N")
    					&& e.getBusinessTransactionType().equals("100")
    					&& e.getSettlementCurrencyCode().equals("840")
    					&& (e.getBusinessTransactionCycle().equals("7") || e.getBusinessTransactionCycle().equals("2") )
    					&& e.getReportIdentificationNumber().equals("120")

    					)
    			.findFirst();

    	Optional<TotalVisa> disputeInterchangeRR2=totalVisa.stream()
    			.filter(e -> e.getBusinessMode().equals("1") &&
    					e.getReportingForSreIdentifier().equals(e.getFundsTransferSreIdentifier())
    					&& (e.getBusinessTransactionCycle().equals("7") || e.getBusinessTransactionCycle().equals("2") )
    					&& e.getSettlementCurrencyCode().equals("840")
    					&& e.getReversalIndicator().equals("N")
    					&& e.getReportIdentificationNumber().equals("130")
    					&& e.getBusinessTransactionType().equals("100")
						&& (e.getSummaryLevel().equals("08") || e.getSummaryLevel().equals("10"))) // Ensure only "08" and "10" are considered
				.sorted(Comparator.comparing(TotalVisa::getSummaryLevel)) // Sort by summary level
				.findFirst();
		Optional<TotalVisa> disputeAmountRL2=totalVisa.stream()
    			.filter(e -> e.getBusinessMode().equals("1") &&
    					e.getReportingForSreIdentifier().equals(e.getFundsTransferSreIdentifier())&&
    					e.getReversalIndicator().equals("Y")&&
    					e.getReturnIndicator().equals("N")
    					&& e.getBusinessTransactionType().equals("100")
    					&& e.getSettlementCurrencyCode().equals("840")
    					&& (e.getBusinessTransactionCycle().equals("8")   )
    					&& e.getReportIdentificationNumber().equals("120")

    					)
    			.findFirst();

    	Optional<TotalVisa> disputeInterchangeRL2=totalVisa.stream()
    			.filter(e -> e.getBusinessMode().equals("1") &&
    					e.getReportingForSreIdentifier().equals(e.getFundsTransferSreIdentifier())
    					&& (e.getBusinessTransactionCycle().equals("8")   )
    					&& e.getSettlementCurrencyCode().equals("840")
    					&& e.getReversalIndicator().equals("Y")
    					&& e.getReportIdentificationNumber().equals("130")
    					&& e.getBusinessTransactionType().equals("100")
						&& (e.getSummaryLevel().equals("08") || e.getSummaryLevel().equals("10"))) // Ensure only "08" and "10" are considered
				.sorted(Comparator.comparing(TotalVisa::getSummaryLevel)) // Sort by summary level
				.findFirst();
    	 logger.info(disputes.toString());
    	
    	List<VisaDisputeStatusAdvice> disputeE=disputes.stream().filter(e->(!e.getCardAcceptorId().startsWith("05")  )).collect(Collectors.toList());
    	List<VisaDisputeStatusAdvice> disputeR=disputes.stream().filter(e->(e.getCardAcceptorId().startsWith("05")  )).collect(Collectors.toList());
    	 logger.info(disputeE.toString());
		List<VisaDisputeStatusAdvice> disputeE_R2 = disputeE.stream()
				.filter(e -> "R2".equals(e.getDisputeStatus()))
				.collect(Collectors.toList());

		List<VisaDisputeStatusAdvice> disputeE_not_R2 = disputeE.stream()
				.filter(e -> !"R2".equals(e.getDisputeStatus()))
				.collect(Collectors.toList());

		List<VisaDisputeStatusAdvice> disputeR_R2 = disputeR.stream()
				.filter(e -> "R2".equals(e.getDisputeStatus()))
				.collect(Collectors.toList());

		List<VisaDisputeStatusAdvice> disputeR_not_R2 = disputeR.stream()
				.filter(e -> !"R2".equals(e.getDisputeStatus()))
				.collect(Collectors.toList());
		List<VisaDisputeStatusAdvice> disputeR_L2 = disputeR.stream()
				.filter(e -> "L2".equals(e.getDisputeStatus()))
				.collect(Collectors.toList());
		List<VisaDisputeStatusAdvice> disputeR_not_R2_L2 = disputeR_not_R2.stream()
				.filter(e -> !"L2".equals(e.getDisputeStatus()))
				.collect(Collectors.toList());
    	 logger.info(disputeR.toString());
    	 logger.info(disputeAmountE.toString());
    	 logger.info(disputeInterchangeE.toString());
    	 logger.info(disputeAmountR.toString());
    	 logger.info(disputeInterchangeR.toString());
		 logger.info(disputeAmountRL2.toString());
    	 logger.info(disputeInterchangeRL2.toString());

    	caulsuleVisaDsiputeE(disputeAmountE,disputeInterchangeE,disputeE_not_R2,"REP");
    	caulsuleVisaDsiputeR(disputeAmountR,disputeInterchangeR,disputeR_not_R2_L2,"REP");
		caulsuleVisaDsiputeE(disputeAmountER2,disputeInterchangeER2,disputeE_R2,"REP");
    	caulsuleVisaDsiputeR(disputeAmountRR2,disputeInterchangeRR2,disputeR_R2,"REP");
    	caulsuleVisaDsiputeR(disputeAmountRL2,disputeInterchangeRL2,disputeR_L2,"L2");

	}

	private void traitementVisaDisputeCharge(List<TotalVisa> totalVisa,List<VisaDisputeStatusAdvice> disputes) throws Exception{
    	logger.info("disputes size =>{}",disputes.size());
    	if (disputes.size()>0) {
    	List<VisaDisputeStatusAdvice> disputeA=disputes.stream().filter(e->(!"6011".equals(e.getMerchantCategoryCode()) && !"6010".equals(e.getMerchantCategoryCode()) && "F1".equals(e.getDisputeStatus())  )).collect(Collectors.toList());
    	List<VisaDisputeStatusAdvice> disputeR=disputes.stream().filter(e->(("6011".equals(e.getMerchantCategoryCode()) || "6010".equals(e.getMerchantCategoryCode())) && "F1".equals(e.getDisputeStatus())  )).collect(Collectors.toList());
    	if(disputeA.size()>0)
    	handleChargeAchat(totalVisa,disputeA);
    	if(disputeR.size()>0)
    	handleChargeRetrait(totalVisa,disputeR);
    	}
    }
    
    private void handleChargeRetrait(List<TotalVisa> totalVisa, List<VisaDisputeStatusAdvice> disputes) throws Exception{
  	  Optional<TotalVisa> disputeAmountE=totalVisa.stream()
    			.filter(e -> e.getBusinessMode().equals("2") &&
    					e.getReportingForSreIdentifier().equals(e.getFundsTransferSreIdentifier())&&
								e.getReversalIndicator().equals("N") &&
    					e.getReturnIndicator().equals("N")
								&& e.getReportIdentificationNumber().equals("120")

								&& (e.getBusinessTransactionCycle().equals("2") || e.getBusinessTransactionCycle().equals("7"))
    					&& e.getBusinessTransactionType().equals("310")
    					)
    			.findFirst();
    	
    	Optional<TotalVisa> disputeInterchangeE=totalVisa.stream()
    			.filter(e -> e.getBusinessMode().equals("2") &&
    					e.getReportingForSreIdentifier().equals(e.getFundsTransferSreIdentifier())&&
    					e.getReportIdentificationNumber().equals("130")
    					&& (e.getBusinessTransactionCycle().equals("2") || e.getBusinessTransactionCycle().equals("7") )
    					&& e.getBusinessTransactionType().equals("310")
    					
    					)
    			.findFirst();
    	Optional<TotalVisa> disputeAmountR=totalVisa.stream()
    			.filter(e -> e.getBusinessMode().equals("1") &&
    					e.getReportingForSreIdentifier().equals(e.getFundsTransferSreIdentifier())&&
    					e.getReversalIndicator().equals("N")&&
    					e.getReturnIndicator().equals("N")
    					&& e.getBusinessTransactionType().equals("310")
    					&& e.getSettlementCurrencyCode().equals("840")
    					&& (e.getBusinessTransactionCycle().equals("2") || e.getBusinessTransactionCycle().equals("7") )
    					&& e.getReportIdentificationNumber().equals("120")
    					
    					)
    			.findFirst();
    	
    	Optional<TotalVisa> disputeInterchangeR=totalVisa.stream()
    			.filter(e -> e.getBusinessMode().equals("1") &&
    					e.getReportingForSreIdentifier().equals(e.getFundsTransferSreIdentifier())
    					&& (e.getBusinessTransactionCycle().equals("2") || e.getBusinessTransactionCycle().equals("7") )
    					&& e.getSettlementCurrencyCode().equals("840")
    					&& e.getReversalIndicator().equals("N")
    					&& e.getReportIdentificationNumber().equals("130")
    					&& e.getBusinessTransactionType().equals("310")
    					
    					)
    			.findFirst();
    	 logger.info(disputes.toString());
    	
    	List<VisaDisputeStatusAdvice> disputeE=disputes.stream().filter(e->(!e.getCardAcceptorId().startsWith("05") && e.getDisputeStatus().equals("F1") )).collect(Collectors.toList());
    	List<VisaDisputeStatusAdvice> disputeR=disputes.stream().filter(e->(e.getCardAcceptorId().startsWith("05")  && e.getDisputeStatus().equals("F1") )).collect(Collectors.toList());
/*    	 logger.info(disputeE.toString());
    	 logger.info(disputeR.toString());
    	 logger.info(disputeAmountR.get().toString());
    	 logger.info(disputeInterchangeR.get().toString());*/
		caulsuleVisaDsiputeE(disputeAmountE, disputeInterchangeE, disputeE, "CHARGEBACKR");
    	caulsuleVisaDsiputeR(disputeAmountR,disputeInterchangeR,disputeR,"CHARGEBACK");
		
	}

	private void handleChargeAchat(List<TotalVisa> totalVisa, List<VisaDisputeStatusAdvice> disputes)throws Exception {
		Optional<TotalVisa> disputeAmountE=totalVisa.stream()
    			.filter(e -> e.getBusinessMode().equals("2") &&
    					e.getReportingForSreIdentifier().equals(e.getFundsTransferSreIdentifier())&&
    					e.getReportIdentificationNumber().equals("120")&&
    					e.getReversalIndicator().equals("N")&&
    					e.getReturnIndicator().equals("N")
    					&& (e.getBusinessTransactionCycle().equals("2") || e.getBusinessTransactionCycle().equals("7") )
    					&& e.getBusinessTransactionType().equals("100")
    					)
    			.findFirst();
    	
    	Optional<TotalVisa> disputeInterchangeE=totalVisa.stream()
    			.filter(e -> e.getBusinessMode().equals("2") &&
    					e.getReportingForSreIdentifier().equals(e.getFundsTransferSreIdentifier())&&
    					e.getReportIdentificationNumber().equals("130")
    					&& e.getReversalIndicator().equals("N")

    					&& (e.getBusinessTransactionCycle().equals("2") || e.getBusinessTransactionCycle().equals("7") )
    					&& e.getBusinessTransactionType().equals("100")
    					
    					)
    			.findFirst();
    	Optional<TotalVisa> disputeAmountR=totalVisa.stream()
    			.filter(e -> e.getBusinessMode().equals("1") &&
    					e.getReportingForSreIdentifier().equals(e.getFundsTransferSreIdentifier())&&
    					e.getReversalIndicator().equals("N")&&
    					e.getReturnIndicator().equals("N")
    					&& e.getBusinessTransactionType().equals("100")
    					&& e.getSettlementCurrencyCode().equals("840")
    					&& (e.getBusinessTransactionCycle().equals("2") || e.getBusinessTransactionCycle().equals("7") )
    					&& e.getReportIdentificationNumber().equals("120")
    					
    					)
    			.findFirst();
    	
    	Optional<TotalVisa> disputeInterchangeR=totalVisa.stream()
    			.filter(e -> e.getBusinessMode().equals("1") &&
    					e.getReportingForSreIdentifier().equals(e.getFundsTransferSreIdentifier())
    					&& (e.getBusinessTransactionCycle().equals("2") || e.getBusinessTransactionCycle().equals("7") )
    					&& e.getSettlementCurrencyCode().equals("840")
    					&& e.getReversalIndicator().equals("N")
    					&& e.getReportIdentificationNumber().equals("130")
    					&& e.getBusinessTransactionType().equals("100")
						&& (e.getSummaryLevel().equals("08") || e.getSummaryLevel().equals("10"))) // Ensure only "08" and "10" are considered
				.sorted(Comparator.comparing(TotalVisa::getSummaryLevel)) // Sort by summary level
				.findFirst();
    	 logger.info(disputes.toString());
    	
    	List<VisaDisputeStatusAdvice> disputeE=disputes.stream().filter(e->(!e.getCardAcceptorId().startsWith("05") && e.getDisputeStatus().equals("F1") )).collect(Collectors.toList());
    	List<VisaDisputeStatusAdvice> disputeR=disputes.stream().filter(e->(e.getCardAcceptorId().startsWith("05")  && e.getDisputeStatus().equals("F1") )).collect(Collectors.toList());
    	 logger.info(disputeE.toString());
    	 logger.info(disputeR.toString());
    	 logger.info(disputeAmountE.toString());
    	 logger.info(disputeInterchangeE.toString());
    	 logger.info(disputeAmountR.toString());
    	 logger.info(disputeInterchangeR.toString());
    	
    	caulsuleVisaDsiputeE(disputeAmountE,disputeInterchangeE,disputeE,"CHARGEBACK");
    	caulsuleVisaDsiputeR(disputeAmountR,disputeInterchangeR,disputeR,"CHARGEBACK");
		
	}


	void caulsuleVisaDsiputeE(Optional<TotalVisa> disputeAmount, Optional<TotalVisa> disputeInterchange, List<VisaDisputeStatusAdvice> dispute,String type) throws Exception {
  	  if(dispute== null || dispute.size()==0) {
  		  return;
  	  }
  	  
        
    	if(disputeAmount.isPresent() && disputeInterchange.isPresent()) {
    		if(Integer.parseInt(disputeAmount.get().getFirstCount())!=dispute.size()){
    			throw new Exception();
    		}
    		else {
    		String sommeMontantTransactions=disputeAmount.get().getFirstAmount();
    		String sommeMontantSettelement=disputeInterchange.get().getFirstAmount();
    		String interchangeAmount=disputeInterchange.get().getSecondAmount();
	 
			  
			  if ("CHARGEBACK".equals(type)) {
				sommeMontantSettelement=disputeAmount.get().getSecondAmount();
  			    interchangeAmount=disputeInterchange.get().getThirdAmount();
			  }
    		double interchange=Double.parseDouble(interchangeAmount)/100;
    		double tauxInterchange=0.0;
//    		double convertMontantTransactions=0.0;
    		double convertMontantSettelement=0.0;
    		double taux=0.0;
    		double sommeCalculated=0.0;
    		double sommeInterchangeCalculated=0.0;
    		double  convertMontantTransactions=Double.parseDouble(sommeMontantTransactions)/1000;
            double convertMontantTransactionsTND = dispute.stream()
                    .mapToDouble(e -> {
                        if ("788".equals(e.getSourceCurrencyCode())) {
                            return e.getSourceAmount();
                        } else if ("788".equals(e.getOriginalTransactionCurrencyCode())) {
                            return e.getOriginalTransactionAmount();
                        } else {
                            throw new RuntimeException("Invalid currency code for transaction.");
                        }
                    })
                    .sum();
	            logger.info(""+convertMontantTransactionsTND);
	            logger.info(""+convertMontantTransactionsTND);

			  convertMontantSettelement=Double.parseDouble(sommeMontantSettelement)/100;
			 double tauxInterchangeG=interchange/convertMontantSettelement;
			  taux=convertMontantTransactions/convertMontantSettelement;
	            logger.info(""+taux);
	            logger.info(""+convertMontantTransactions);
	            logger.info(""+convertMontantSettelement);

    		    logger.info("size R2=> {}",dispute.size());
                for(VisaDisputeStatusAdvice e:dispute) {
                  double tndAmount = 0.0;

  			      if ("788".equals(e.getSourceCurrencyCode())) {
  			    	tndAmount = e.getSourceAmount();
                  } else if ("788".equals(e.getOriginalTransactionCurrencyCode())) {
                	  tndAmount = e.getOriginalTransactionAmount();
                  } else {
                      throw new Exception("Invalid currency code for transaction.");
                  }
	                  // Calculate transaction proportionally
	    	            logger.info(""+tndAmount);

	                  double proportion = tndAmount / convertMontantTransactionsTND;
	    	            logger.info(""+proportion);

   				  double trans =roundToTwoDecimalPlaces(proportion*convertMontantSettelement);
   	            logger.info(""+trans);

  				  double interchangeTrans=roundToTwoDecimalPlaces(tauxInterchangeG*trans);
  				  e.setSettlementCalculate(trans);
  				  e.setInterchangeCalculat(interchangeTrans);
  				  e.setConversionRate(taux);
  				  e.setConversionRateTnd(taux);
  				  e.setAmountTnd(e.getSourceAmount());
  				  sommeCalculated+=e.getSettlementCalculate();
  				  sommeInterchangeCalculated+=e.getInterchangeCalculat();
    	             
    		    }
    		    ajustementVisaDispute(dispute, sommeCalculated,convertMontantSettelement, 2, dispute.size());
    		    ajustementVisaDisputeInterchange(dispute, sommeInterchangeCalculated, interchange, 2, dispute.size());
    		}
       
    	}		
	}
      void caulsuleVisaDsiputeR(Optional<TotalVisa> disputeAmount, Optional<TotalVisa> disputeInterchange, List<VisaDisputeStatusAdvice> dispute,String type) throws Exception {


    	  if(dispute== null || dispute.size()==0) {
    		  return;
    	  }
    	  
    	  if(disputeAmount.isPresent() && disputeInterchange.isPresent()) {
        	   logger.info(disputeAmount.get().toString());
        	   logger.info(disputeInterchange.get().toString());
        	   logger.info(dispute.toString());
    		  if(Integer.parseInt(disputeAmount.get().getFirstCount())!=dispute.size()){
    			  throw new Exception();
    		  }
    		  else {
    			  String sommeMontantTransactions=disputeAmount.get().getFirstAmount();
    			  
    			  String sommeMontantSettelement=disputeAmount.get().getSecondAmount();
    			  String interchangeAmount=disputeInterchange.get().getThirdAmount();
    			  
    			  if ("CHARGEBACK".equals(type)) {
    				    sommeMontantSettelement=disputeAmount.get().getFirstAmount();
     			  }
				  if("L2".equals(type)){
					    sommeMontantSettelement=disputeAmount.get().getFirstAmount();
					    interchangeAmount=disputeInterchange.get().getSecondAmount();
				  }
    	    	   logger.info(sommeMontantTransactions);
    	    	   logger.info(sommeMontantSettelement);
    	    	   logger.info(interchangeAmount);

    			  double interchange=Double.parseDouble(interchangeAmount)/100;
    			  double tauxInterchange=0.0;
    			  double convertMontantTransactions=0.0;
    			  double convertMontantSettelement=0.0;
    			  double taux=0.0;
    			  double sommeCalculated=0.0;
    			  double sommeInterchangeCalculated=0.0;
    	    		   convertMontantTransactions=Double.parseDouble(sommeMontantTransactions)/100;

    	         double  convertMontantTransactionsTND = dispute.stream()
    	                    .mapToDouble(e -> {
    	                        if ("788".equals(e.getSourceCurrencyCode())) {
    	                            return e.getSourceAmount();
    	                        } else if ("788".equals(e.getOriginalTransactionCurrencyCode())) {
    	                            return e.getOriginalTransactionAmount();
    	                        } else {
    	                            throw new RuntimeException("Invalid currency code for transaction.");
    	                        }
    	                    })
    	                    .sum();
    	            logger.info(""+convertMontantTransactionsTND);
    				  convertMontantSettelement=Double.parseDouble(sommeMontantSettelement)/100;
    				 double tauxInterchangeG=interchange/convertMontantSettelement;
    				  taux=convertMontantTransactions/convertMontantSettelement;
    	    	
    	    		    logger.info("size R2=> {}",dispute.size());
    	                for(VisaDisputeStatusAdvice e:dispute) {
    	                  double tndAmount = 0.0;

    	  			      if ("788".equals(e.getSourceCurrencyCode())) {
    	  			    	tndAmount = e.getSourceAmount();
    	                  } else if ("788".equals(e.getOriginalTransactionCurrencyCode())) {
    	                	  tndAmount = e.getOriginalTransactionAmount();
    	                  } else {
    	                      throw new Exception("Invalid currency code for transaction.");
    	                  }

    	                  // Calculate transaction proportionally
    	    	            logger.info(""+tndAmount);

    	                  double proportion = tndAmount / convertMontantTransactionsTND;
   	    	            logger.info(""+proportion);

   	    	        double trans =roundToTwoDecimalPlaces(proportion*convertMontantSettelement);
   	    	        double interchangeTrans=roundToTwoDecimalPlaces(tauxInterchangeG*trans);
    	  				  e.setSettlementCalculate(trans);
    	  				  e.setInterchangeCalculat(interchangeTrans);
    	  				  e.setConversionRate(taux);
    	  				  e.setConversionRateTnd(taux);
    	  				  e.setAmountTnd(e.getSourceAmount());
    	  				  sommeCalculated+=e.getSettlementCalculate();
    	  				  sommeInterchangeCalculated+=e.getInterchangeCalculat();
    	    	             
    	    		    }
    	    		    ajustementVisaDispute(dispute, sommeCalculated,convertMontantSettelement, 2, dispute.size());
    	    		    ajustementVisaDisputeInterchange(dispute, sommeInterchangeCalculated, interchange, 2, dispute.size());
    		  }
    		  
    	  }		
      }

    
    
	public Map<String, Integer> calculateDifference(
            Map<String, Optional<TotalVisa>> totalAcqMap,
            Map<String, Optional<TotalVisa>> totalAcqReturnedMap) {
        
        Map<String, Integer> differenceMap = new HashMap<>();

        for (String currency : totalAcqMap.keySet()) {
            Optional<TotalVisa> totalAcq = totalAcqMap.get(currency);
            Optional<TotalVisa> totalReturned = totalAcqReturnedMap.get(currency);
           
            int difference = totalAcq.map(acq -> 
            totalReturned.map(returned -> 
                Integer.parseInt(acq.getFirstCount()) /*- Integer.parseInt(returned.getFirstCount())*/
            ).orElse(Integer.parseInt(acq.getFirstCount())) // If no returned value, take the totalAcq
         ).orElse(0); // If no totalAcq value, consider the difference as 0

            differenceMap.put(currency, difference);
        }

        return differenceMap;
    }


	public static String formatHexDump(byte[] array, int offset, int length) {

		final int width = 16;

		StringBuilder builder = new StringBuilder();

		for (int rowOffset = offset; rowOffset < offset + length; rowOffset += width) {

			builder.append(String.format("%06d:  ", rowOffset));

			for (int index = 0; index < width; index++) {

				if (rowOffset + index < array.length) {

					builder.append(String.format("%02x ", array[rowOffset + index]));

				} else {

					builder.append("   ");

				}

			}

			if (rowOffset < array.length) {

				int asciiWidth = Math.min(width, array.length - rowOffset);

				builder.append("  |  ");

				builder.append(new String(array, rowOffset, asciiWidth, c).replaceAll("\r\n", " ")

						.replaceAll("\n", " "));

			}

			builder.append(String.format("%n"));

		}

		return builder.toString();

	}


	public String toHexadecimal(String ascii) throws UnsupportedEncodingException {

		char[] ch = ascii.toCharArray();

		StringBuilder builder = new StringBuilder();
		for (char c : ch) {
			// Step-2 Use %H to format character to Hex
			String hexCode = String.format("%H", c);
			builder.append(hexCode);
		}

		return builder.toString();
	}


	private double extractAmount(String value,int position) {
		return Double.parseDouble(value.substring(position));
	}



	public static String convertDate(String dateString, String formatInput, String formatOutput) throws ParseException {
		SimpleDateFormat inputFormat = new SimpleDateFormat(formatInput);
		Date date = inputFormat.parse(dateString);
		SimpleDateFormat outputFormat = new SimpleDateFormat(formatOutput);
		return outputFormat.format(date);
	}


private Integer parseSequenceNumber(String line) {
    try {
        // Ensure line is long enough to check the 4th character
        if (line.length() >= 4 && Character.isDigit(line.charAt(3))) {
            return Character.getNumericValue(line.charAt(3));
        }
    } catch (NumberFormatException e) {
        // Handle invalid number format
    }
    return null; // Return null if not a valid sequence number
}


	  public List<VisaDisputeStatusAdvice> ajustementVisaDispute(List<VisaDisputeStatusAdvice> octfs, double totalActuel, double totalDesire, int precision, int len) {
		    double sommeActuelle = totalActuel;
		    BigDecimal totalDesireBigDecimal = BigDecimal.valueOf(totalDesire);
		    BigDecimal totalActuelBigDecimal = BigDecimal.valueOf(totalActuel);

		    BigDecimal sommeActuelleBigDecimal = BigDecimal.valueOf(sommeActuelle);
		    BigDecimal ajustementTotalBigDecimal = totalDesireBigDecimal.subtract(sommeActuelleBigDecimal);
		    ajustementTotalBigDecimal = ajustementTotalBigDecimal.setScale(2, RoundingMode.HALF_UP);
	    	   logger.info(""+totalDesireBigDecimal);
	    	   logger.info(""+octfs.toString());
	    	   logger.info(""+sumOfSettlementsDispute(octfs));

		    // Boucler tant que l'ajustement total n'est pas égal à zéro
        while(sumOfSettlementsDispute(octfs).compareTo(totalDesireBigDecimal) != 0){
        	boolean test=false;
		        // Calculer l'ajustement par valeur
		    	BigDecimal ajustementParValeur = BigDecimal.valueOf(0.01);

		        // Réinitialiser la somme actuelle à chaque itération
		    	sommeActuelleBigDecimal = sumOfSettlementsDispute(octfs);

		        // Parcourir la liste octfs jusqu'à ce que l'ajustement total devienne nul
		       int i=0;
		    	while ((i < octfs.size()) && !test) {

		        	BigDecimal signeAjustement = BigDecimal.valueOf(Math.signum(ajustementTotalBigDecimal.signum()));
		        	BigDecimal ajustement = ajustementParValeur.multiply(signeAjustement);
		        	BigDecimal calculateBigDecimal = BigDecimal.valueOf(octfs.get(i).getSettlementCalculate()).add(ajustement);

		        	double calculate = calculateBigDecimal.setScale(2, RoundingMode.HALF_EVEN).doubleValue();
		        	octfs.get(i).setSettlementCalculate((calculate));

		        	sommeActuelleBigDecimal = sommeActuelleBigDecimal.add(ajustement);
		        	sommeActuelleBigDecimal = sommeActuelleBigDecimal.setScale(2, RoundingMode.HALF_EVEN);

		            // Vérifier si l'ajustement total est devenu nul
		        	ajustementTotalBigDecimal = totalDesireBigDecimal.subtract(sommeActuelleBigDecimal);

		        	ajustementTotalBigDecimal = ajustementTotalBigDecimal.setScale(2, RoundingMode.HALF_EVEN);
		        	 logger.info(octfs.toString());
		        	 logger.info(totalDesireBigDecimal.toString());
		        	if (sumOfSettlementsDispute(octfs).compareTo(totalDesireBigDecimal) ==0) {

		                test=true; // Sortir de la boucle for si l'ajustement total est nul
		            }
		        	else {
		        		i++;
		        	}
		        }
		    }
		    return octfs;
		}


	  public List<VisaDisputeStatusAdvice> ajustementVisaDisputeInterchange(List<VisaDisputeStatusAdvice> octfs, double totalActuel, double totalDesire, double precision,
			int len) {
		 double sommeActuelle = totalActuel;
		    BigDecimal totalDesireBigDecimal = BigDecimal.valueOf(totalDesire);
		    BigDecimal totalActuelBigDecimal = BigDecimal.valueOf(totalActuel);

		    BigDecimal sommeActuelleBigDecimal = BigDecimal.valueOf(sommeActuelle);
		    BigDecimal ajustementTotalBigDecimal = totalDesireBigDecimal.subtract(sommeActuelleBigDecimal);
		    ajustementTotalBigDecimal = ajustementTotalBigDecimal.setScale(2, RoundingMode.HALF_UP);
	    	   logger.info(""+totalDesireBigDecimal);
	    	   logger.info(""+octfs.toString());
		    // Boucler tant que l'ajustement total n'est pas égal à zéro
         while(sumOfInterchangesDispute(octfs).compareTo(totalDesireBigDecimal) != 0){
         	boolean test=false;
		        // Calculer l'ajustement par valeur
		    	BigDecimal ajustementParValeur = BigDecimal.valueOf(0.01);

		        // Réinitialiser la somme actuelle à chaque itération
		    	sommeActuelleBigDecimal = totalActuelBigDecimal;

		        // Parcourir la liste octfs jusqu'à ce que l'ajustement total devienne nul
		       int i=0;
		    	while ((i < octfs.size()) && !test) {
		        	
		        	BigDecimal signeAjustement = BigDecimal.valueOf(Math.signum(ajustementTotalBigDecimal.signum()));
		        	BigDecimal ajustement = ajustementParValeur.multiply(signeAjustement);
		        	BigDecimal calculateBigDecimal = BigDecimal.valueOf(octfs.get(i).getInterchangeCalculat()).add(ajustement);

		        	double calculate = calculateBigDecimal.setScale(2, RoundingMode.HALF_EVEN).doubleValue();
		        	octfs.get(i).setInterchangeCalculat((calculate));

		        	sommeActuelleBigDecimal = sommeActuelleBigDecimal.add(ajustement);
		        	sommeActuelleBigDecimal = sommeActuelleBigDecimal.setScale(2, RoundingMode.HALF_EVEN);

		            // Vérifier si l'ajustement total est devenu nul
		        	ajustementTotalBigDecimal = totalDesireBigDecimal.subtract(sommeActuelleBigDecimal).abs();

		        	ajustementTotalBigDecimal = ajustementTotalBigDecimal.setScale(2, RoundingMode.HALF_EVEN);
		        	 logger.info(octfs.toString());
		        	 logger.info(totalDesireBigDecimal.toString());
		        	if (sumOfInterchangesDispute(octfs).compareTo(totalDesireBigDecimal) ==0) {

		                test=true; // Sortir de la boucle for si l'ajustement total est nul
		            }
		        	else {
		        		i++;
		        	}
		        }
		    }
		    return octfs;

	}

	public static double roundToTwoDecimalPlaces(double value) {
		// Multiplier par 100 et tronquer en un entier
		int temp = (int) (value * 100);
		// Diviser par 100 pour remettre la virgule
		return temp / 100.0;
	}
	public static double roundToTwoDecimalPlacesVisa(double value) {
		// Multiplier par 100 et tronquer en un entier
		int temp = (int) (value * 1000000);
		// Diviser par 100 pour remettre la virgule
		return temp / 1000000.0;
	}

	public static String maskCardNumber(String cardNumber) {

		if(Objects.isNull(cardNumber) || cardNumber.length()<15){
			return cardNumber;
		}
		int replacingStringLength = cardNumber.length() - 10;
		String replacingString = "";
		int index = 0;
		while (index < replacingStringLength) {
			replacingString = replacingString + "*";
			index++;
		}
		return maskString(cardNumber, replacingString, 6, cardNumber.length() - 4);
	}

	public static String maskString(String str, String replacingStr, int start, int end) {
		return StringUtils.overlay(str, replacingStr, start, end);
	}

	public void runvisa(BatchesFC batch) {
		logger.info("====>" + methode);
		// Creating a wrapper class to hold the response
		class ResponseWrapper {
			FileIntegrationClearing response;
		}

		ResponseWrapper responseWrapper = new ResponseWrapper();
		LocalDateTime currentDateTime = LocalDateTime.now();
		// Initialize batch status
		BatchesFC batche = batchesFFCRepository.findByKey("INCTF").get();
		batche.setBatchStatus(0);
		batchesFFCRepository.save(batche);

		Instant instant = currentDateTime.atZone(ZoneId.systemDefault()).toInstant();
		Date startDate = Date.from(instant);
try{

				transactionTemplate.execute(status -> {

					try {
						responseWrapper.response = readFileVISA(batch);
					} catch (Exception e) {

						String stackTrace = Throwables.getStackTraceAsString(e);
						logger.info("Exception is=>{}", stackTrace);
						FileIntegrationClearing response = new FileIntegrationClearing();
						LocalDate localDate;


						String ordinalDateString="";
						try {
							ordinalDateString = transformDateFromFile(batch);
						} catch (IOException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
						String fileName =   batch.getFileName() + "." + ordinalDateString;

						StackTraceElement[] stackTraceDetail = e.getStackTrace();
			            if (stackTraceDetail.length >= 2) {
			                StackTraceElement[] stack2= {stackTraceDetail[0],stackTraceDetail[1]};    
			                response.setErrorStack(stack2);
			            }		
			            else {
						response.setErrorStack(e.getStackTrace());
			            }
						 
						response.setStatus(false);
						response.setMessage(e.getMessage());
					 

						
						responseWrapper.response=response;
						status.setRollbackOnly();
					}

					return null;
				});


		} catch (Exception e) {

			responseWrapper.response.setStatus(false);
			responseWrapper.response.setMessage(e.getMessage());
//			bh.updateStatusAndErrorBatch(batch.getBatchId(), new Date(), e.getMessage(),
//					e.getStackTrace()[0].toString(), 2);
			String stackTrace = Throwables.getStackTraceAsString(e);
			logger.info("Exception is=>{}", stackTrace);
		}
		if (responseWrapper.response.isStatus()) {
			  batche = batchesFFCRepository.findByKey("CRAUAP70").get();
			batche.setBatchStatus(1);
			batchesFFCRepository.save(batche);
		} else {
			currentDateTime = LocalDateTime.now();
			batche.setBatchStatus(2);
			batche.setError(responseWrapper.response.getMessage());
			
			if ((responseWrapper.response.getMessage()!=null)&&(responseWrapper.response.getMessage().length()>255))
			{
				batche.setError(responseWrapper.response.getMessage().substring(0,255));
			}
			else {
				batche.setError(responseWrapper.response.getMessage());
			}
			Date endDate = java.sql.Timestamp.valueOf(currentDateTime);
			batche.setBatchEndDate(endDate);
			if (responseWrapper.response!=null  &&  responseWrapper.response.getNameComplet()!=null)

			if (responseWrapper.response.getErrorStack() != null){
				if (responseWrapper.response.getErrorStack().length==1)
				{
					batche.setErrorStackTrace(responseWrapper.response.getErrorStack()[0].toString());
				}	
				else if (responseWrapper.response.getErrorStack().length>=2)
				{
					batche.setErrorStackTrace(responseWrapper.response.getErrorStack()[0].toString());
				}
			}
			batchesFFCRepository.saveAndFlush(batche);
		}
	}



	@GetMapping("generatedataEUR/{fileDate}")
	public List<VisaRaport> generatedataEUR(@PathVariable(value = "fileDate") String fileDate) {
		VisaSummary summariesV = visaSummaryRepo.findByDate(fileDate);

		Map<String, TotalVisa> listReturn = new HashMap<>();
		if(summariesV!=null) {
			TotalVisa purchaseEmission = totalVisaRepository.getTotalPurchaseEmissionEUR(summariesV.getSummaryCode()).orElse(new TotalVisa());
			TotalVisa purchaseEmissionCasiCsh = totalVisaRepository.getTotalEmissionCASICASHEUR(summariesV.getSummaryCode()).orElse(new TotalVisa());

			TotalVisa retraitEmission = totalVisaRepository.getTotalWithdrawalEmissionEUR(summariesV.getSummaryCode()).orElse(new TotalVisa());
			TotalVisa intechangeretraitEmission = totalVisaRepository.getTotalEmissionWithdrawalInterchangeEUR(summariesV.getSummaryCode()).orElse(new TotalVisa());
			TotalVisa refundRetraitEmission = totalVisaRepository.getTotalRefundEmissionOriginalCreditEUR(summariesV.getSummaryCode()).orElse(new TotalVisa());
			TotalVisa refundAchatEmission = totalVisaRepository.getTotalRefundEmissionMerchandiseReturnEUR(summariesV.getSummaryCode()).orElse(new TotalVisa());
			TotalVisa demandeSoldeEmission = totalVisaRepository.getTotalBalanceInquiryEmissionEUR(summariesV.getSummaryCode()).orElse(new TotalVisa());
			TotalVisa visaNotApprouvedEmission = totalVisaRepository.getTotalAtmDeclineEmissionEUR(summariesV.getSummaryCode()).orElse(new TotalVisa());


			VisaV2 chargeEmissionv = totalVisaRepository.getTotalVisaChargeEUR(summariesV.getSummaryCode()).orElse(null);
			List<VisaRaport> raportslist= new ArrayList<>();
			TotalVisa	chargeEmission=new TotalVisa();
			chargeEmission.setThirdAmount(chargeEmissionv.getNetAmount());
			VisaRaport achatISSUER= new VisaRaport();
			achatISSUER.setLibelle("Achat");
			achatISSUER.setType("ISSUER");
			achatISSUER.setCurrency("EUR");

			achatISSUER.setRaportDate(fileDate);
//		achatISSUER.setSettAmt(Double.valueOf(purchaseEmission.getFirstAmount()) );
//		achatISSUER.setNbTrans(Integer.valueOf(purchaseEmission.getFirstCount()) );
//		achatISSUER.setInterAmt(Double.valueOf(purchaseEmission.getSecondAmount()));
			achatISSUER.setSettAmt(Double.valueOf(purchaseEmission.getFirstAmount())+Double.valueOf(purchaseEmissionCasiCsh.getFirstAmount()) );
			achatISSUER.setNbTrans(Integer.valueOf(purchaseEmission.getFirstCount())+Integer.valueOf(purchaseEmissionCasiCsh.getFirstCount()) );
			achatISSUER.setInterAmt(Double.valueOf(purchaseEmission.getSecondAmount())+Double.valueOf(purchaseEmissionCasiCsh.getSecondAmount()) );
			raportslist.add(achatISSUER );


			VisaRaport retraitISSUER= new VisaRaport();
			retraitISSUER.setLibelle("retrait");
			retraitISSUER.setType("ISSUER");
			retraitISSUER.setCurrency("EUR");

			retraitISSUER.setRaportDate(fileDate);
			retraitISSUER.setSettAmt(Double.valueOf(retraitEmission.getThirdAmount())-Double.valueOf(retraitEmission.getSecondAmount()));
			retraitISSUER.setNbTrans(Integer.valueOf(retraitEmission.getFirstCount())+Integer.valueOf(demandeSoldeEmission.getFirstCount())+Integer.valueOf(visaNotApprouvedEmission.getFirstCount() ));
			retraitISSUER.setInterAmt(-Double.valueOf(intechangeretraitEmission.getSecondAmount())+Double.valueOf(intechangeretraitEmission.getThirdAmount())-Double.valueOf(demandeSoldeEmission.getSecondAmount())+Double.valueOf(demandeSoldeEmission.getThirdAmount())-Double.valueOf(visaNotApprouvedEmission.getSecondAmount())+Double.valueOf(visaNotApprouvedEmission.getThirdAmount()));
			raportslist.add(retraitISSUER );

			VisaRaport refundISSUER= new VisaRaport();
			refundISSUER.setLibelle("refund");
			refundISSUER.setType("ISSUER");
			refundISSUER.setCurrency("EUR");
			refundISSUER.setRaportDate(fileDate);
			refundISSUER.setSettAmt(Double.valueOf(refundRetraitEmission.getFirstAmount())+Double.valueOf(refundAchatEmission.getFirstAmount()));
			refundISSUER.setNbTrans(Integer.valueOf(refundRetraitEmission.getFirstCount())+Integer.valueOf(refundAchatEmission.getFirstCount()));
			refundISSUER.setInterAmt(Double.valueOf(refundRetraitEmission.getSecondAmount())+Double.valueOf(refundRetraitEmission.getThirdAmount())-Double.valueOf(refundAchatEmission.getSecondAmount())-Double.valueOf(refundAchatEmission.getThirdAmount()) );

			raportslist.add(refundISSUER );




			//charge
			VisaRaport charge= new VisaRaport();
			charge.setLibelle("charge");
			charge.setType("ISSUER");
			charge.setCurrency("EUR");
			charge.setRaportDate(fileDate);
			charge.setSettAmt(Double.valueOf(chargeEmissionv.getNetAmount()));
			raportslist.add(charge );

			visaRaportRepository.saveAll(raportslist);
//
//		listReturn.put("Achat", purchaseEmission != null ? purchaseEmission : new TotalVisa());
//		listReturn.put("Retrait", retraitEmission != null ? retraitEmission : new TotalVisa());
//		listReturn.put("intechangeretraitEmission", intechangeretraitEmission != null ? intechangeretraitEmission : new TotalVisa());
//		listReturn.put("Refund RETRAIT", refundRetraitEmission != null ? refundRetraitEmission : new TotalVisa());
//		listReturn.put("CREDIT VOUCHER", refundAchatEmission != null ? refundAchatEmission : new TotalVisa());
//		listReturn.put("demandeSoldeEmission", demandeSoldeEmission != null ? demandeSoldeEmission : new TotalVisa());
//		listReturn.put("visaNotApprouvedEmission",
//				visaNotApprouvedEmission != null ? visaNotApprouvedEmission : new TotalVisa());
//		listReturn.put("CHARGEBACK", chargeEmission != null ? chargeEmission : new TotalVisa());

			return raportslist;}else {
			return null;
		}

	}


}