package com.mss.backOffice.controller;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.beanutils.PropertyUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.google.gson.Gson;
import com.mss.backOffice.Response.TotalControlDisplay;
import com.mss.backOffice.Response.TpControlDates;
import com.mss.backOffice.Response.TpControlDisplay;
import com.mss.backOffice.Response.UapNotAcceptedDisplay;
import com.mss.backOffice.request.AddFileRequest;
import com.mss.backOffice.request.BkmvtiFransaBankRequest;
import com.mss.backOffice.request.CraControlDisplay;
import com.mss.backOffice.request.CraData;
import com.mss.backOffice.request.CraDataList;
import com.mss.backOffice.request.CroControlDisplay;
import com.mss.backOffice.request.CroControlFilter;
import com.mss.backOffice.request.CroData;
import com.mss.backOffice.request.CroDataList;
import com.mss.backOffice.request.DateFilter;

import com.mss.backOffice.request.FileContentRequest;
import com.mss.backOffice.request.SummaryFileContent;
import com.mss.backOffice.request.TpControlFilter;
import com.mss.unified.dto.FileContentProjection;
import com.mss.unified.entities.Account;
import com.mss.unified.entities.BankFransaBank;
import com.mss.unified.entities.BatchesFC;
import com.mss.unified.entities.BkmHistory;
import com.mss.unified.entities.BkmvtiFransaBank;
import com.mss.unified.entities.Card;
import com.mss.unified.entities.CommissionAchatFransaBank;
import com.mss.unified.entities.CommissionAchatInternetFB;
import com.mss.unified.entities.CommissionFransaBank;
import com.mss.unified.entities.CommissionFransabankOnUs;
import com.mss.unified.entities.ConfCategories;
import com.mss.unified.entities.CraControl;
import com.mss.unified.entities.CraRejetControl;
import com.mss.unified.entities.CroControl;
import com.mss.unified.entities.CroRejetControl;
import com.mss.unified.entities.FileContent;
import com.mss.unified.entities.FileContentT;
import com.mss.unified.entities.FileHeader;
import com.mss.unified.entities.FileHeaderT;
import com.mss.unified.entities.MotifRejet;
import com.mss.unified.entities.MvbkConf;
import com.mss.unified.entities.MvbkSettlement;
import com.mss.unified.entities.OpeningDay;
import com.mss.unified.entities.TpControl;
import com.mss.unified.entities.TransactionType;
import com.mss.unified.entities.UAP040FransaBankNotAccepted;
import com.mss.unified.entities.UAP040INNotAccepted;
import com.mss.unified.entities.UAP050FransaBankNotAccepted;
import com.mss.unified.entities.UAP050INNotAccepted;
import com.mss.unified.entities.UAP051FransaBankNotAccepted;
import com.mss.unified.entities.UAP051INNotAccepted;
import com.mss.unified.entities.OpeningDay;

import com.mss.unified.repositories.AccountRepository;
import com.mss.unified.repositories.BankFransaRepository;
import com.mss.unified.repositories.BatchesFFCRepository;
import com.mss.unified.repositories.BkmHistoryRepository;
import com.mss.unified.repositories.BkmvtiFransaBankRepository;
import com.mss.unified.repositories.CardRepository;
import com.mss.unified.repositories.CodeBankBCRepository;
import com.mss.unified.repositories.CommissionAchatFransaBankRepository;
import com.mss.unified.repositories.CommissionFransaBankInternetRepository;
import com.mss.unified.repositories.CommissionFransaBankRepository;
import com.mss.unified.repositories.CommissionFransabankOnUsRepository;
import com.mss.unified.repositories.ConfCategoriesRepository;
import com.mss.unified.repositories.CraControlRepository;
import com.mss.unified.repositories.FILECONTENTRepository;
import com.mss.unified.repositories.FILECONTENTTRepository;
import com.mss.unified.repositories.FileProcessedRepository;
import com.mss.unified.repositories.FileRepository;
import com.mss.unified.repositories.FileTRepository;
import com.mss.unified.repositories.MotifRejetRepository;
import com.mss.unified.repositories.MvbkConfigRepository;
import com.mss.unified.repositories.MvbkSettlementRepository;
import com.mss.unified.repositories.OpeningDayRepository;
import com.mss.unified.repositories.StatusFileContentRepository;
import com.mss.unified.repositories.TpControlRespository;
import com.mss.unified.repositories.TransactionTypesRepository;
import com.mss.unified.repositories.UAP040FransaBankNotAcceptedRepository;
import com.mss.unified.repositories.UAP050FransaBankNotAcceptedRepository;
import com.mss.unified.repositories.UAP050InNotAcceptedRepository;
import com.mss.unified.repositories.UAP051FransaBankNotAcceptedRepository;
import com.mss.unified.repositories.UAP051InNotAcceptedRepository;
import com.mss.unified.repositories.CraRejetControlRepository;
import com.mss.unified.repositories.CroControlRepository;
import com.mss.unified.repositories.CroRejetControlRepository;
import com.mss.unified.repositories.UAP040InNotAcceptedRepository;

@RestController
@RequestMapping("FileRequest")
public class FileRequest {
	@Autowired
	IntegrationFilesInternational integrationFilesInternational;
	// public String Path = "C://Users//Lenovo IdeaPad L3//Desktop//Files26";
	@Autowired
	MvbkConfigRepository mvbCongR;
	@Autowired
	ConfCategoriesRepository mvbConfCategoriesRep;
	@Autowired
	FileTRepository fileSummaryTRepository;
	@Autowired
	FILECONTENTRepository filecontentRepository;
	@Autowired
	FileRepository fileSummaryRepository;
	@Autowired
	FILECONTENTTRepository filecontentTRepository;
	@Autowired
	BkmvtiFransaBankRepository bkmvtiFransaBankRepository;
	@Autowired
	CodeBankBCRepository codeBankBCRepository;
	@Autowired
	BatchesFFCRepository batchesFFCRepository;
	@Autowired
	FileProcessedRepository fileProcessedRepository;
	@Autowired
	StatusFileContentRepository statusFileContent;
	@Autowired
	TransactionTypesRepository transactionTypesRepository;
	@Autowired
	MvbkSettlementRepository mvbkSettlementRepository;
	@Autowired
	CommissionAchatFransaBankRepository commissionAchatFransaBankRepository;
	@Autowired
	CommissionFransaBankRepository commissionFransaBankRepository;
	@Autowired
	CommissionFransaBankInternetRepository commissionAchatInternetFBRepository;
	@Autowired
	BankFransaRepository bankFransaRepository;
	@Autowired
	CommissionFransabankOnUsRepository commissionOnUsFransaBankRepository;
	@Autowired
	OpeningDayRepository openingDRepository;
	@Autowired
	private MotifRejetRepository motifRejetRepository;
	@Autowired
	private TpControlRespository tpControlRespository;

	@Autowired
	private CroControlRepository croControlRespository;
	@Autowired
	private CraControlRepository craControlRespository;

	@Autowired
	private CardRepository cardRepository;
	@Autowired
	private AccountRepository accountRepository;
	@Autowired
	private UAP040FransaBankNotAcceptedRepository uAP040FransaBankNotAcceptedRepository;
	@Autowired
	private UAP050FransaBankNotAcceptedRepository uAP050FransaBankNotAcceptedRepository;
	@Autowired
	private UAP051FransaBankNotAcceptedRepository uAP051FransaBankNotAcceptedRepository;

	@Autowired
	private CraRejetControlRepository craRejetControlRespository;

	@Autowired
	private UAP040InNotAcceptedRepository uAP040InNotAcceptedRepository;
	@Autowired
	private UAP050InNotAcceptedRepository uAP050InNotAcceptedRepository;
	@Autowired
	private UAP051InNotAcceptedRepository uAP051InNotAcceptedRepository;
	@Autowired
	private CroRejetControlRepository croRejetControlRepository;
	@Autowired
	private BkmHistoryRepository bkmHistoryRepository;
	@Autowired
	private com.mss.unified.services.DynamicQueryService dynamicQueryService;
	private static final Gson gson = new Gson();
	private static final int pendingProgressBatch = -1;

	private static final int inProgressBatch = 0;
	private static final int doneProgressBatch = 1;
	private static final int errorProgressBatch = 2;
	private static final int waintingValidation = 3;
	private static final Logger logger = LoggerFactory.getLogger(FileRequest.class);

	public static String getLineNumber() {
		return Thread.currentThread().getStackTrace()[2].getClassName() + ": "
				+ String.valueOf(Thread.currentThread().getStackTrace()[2].getLineNumber());
	}

	public static void print(String x, String y) {
		logger.info(y + "  " + x + " at date " + LocalDateTime.now());
	}

	public boolean isValidDateFormat(String inputDate) {
		try {
			LocalDate.parse(inputDate);
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	@GetMapping("getAllCompensationDates")
	public HashSet<String> getAllCompensationDates() {
		List<FileHeader> elements = fileSummaryRepository.findAll();
		HashSet<String> dates = new HashSet();
		for (FileHeader fh : elements) {
			try {
				DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyMMdd");
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

	@GetMapping("getAllReglemntDates")
	public List<OpeningDay> getAllReglemntDates() {
		return openingDRepository.findAll();
	}

	@GetMapping("checklength")
	public ResponseEntity<?> checklength() throws IOException {

		return ResponseEntity.ok().body(gson.toJson(filecontentTRepository.count()));

	}

	@GetMapping("validateFile")
	public ResponseEntity<?> validateFile() throws IOException {
		BatchesFC batch = batchesFFCRepository.findByBatchType("Integration").get(0);
		print("starting", getLineNumber());
		FileHeaderT s = fileSummaryTRepository.findAll().get(0);
		FileHeader des = new FileHeader();
		try {
			PropertyUtils.copyProperties(des, s);

		} catch (Exception ex) {
			ex.printStackTrace();
		}
		List<FileContentT> a = filecontentTRepository.findAll();
		des = fileSummaryRepository.saveAndFlush(des);
		FileRequest.print(String.valueOf(des.getId()), getLineNumber());
		List<FileContent> b = new ArrayList<>();
		for (FileContentT ft : a) {
			FileContent tocopy = new FileContent();
			try {
				PropertyUtils.copyProperties(tocopy, ft);
				tocopy.setIdHeder(String.valueOf(des.getId()));

				b.add(tocopy);
			} catch (Exception ex) {
				ex.printStackTrace();
			}
			if("080".equals(tocopy.getTypeTransaction())){
				List<FileContent> matchingEntities = filecontentRepository.findByNumCartePorteurAndNumAutorisationAndIdCommercant(
					tocopy.getNumCartePorteur(), 
					tocopy.getNumAutorisationOperationInitiale(),
					tocopy.getIdCommercant()
				);
				// Process the matching entities as needed
				if (matchingEntities != null && !matchingEntities.isEmpty()) {
					tocopy.setTransactionOrigine(matchingEntities.get(0).getTypeTransaction());
					tocopy.setIdTransactionOrigine(matchingEntities.get(0).getId());

					logger.info("Found " + matchingEntities.size() + " matching entities for transaction 080");
					// Add your logic here to handle the found entities
				}else{

				}
			}

		}

		print("saving", getLineNumber());
		filecontentRepository.saveAll(b);

		print("deleting", getLineNumber());
		batch.setBatchStatus(inProgressBatch);
		batchesFFCRepository.saveAndFlush(batch);
		fileSummaryTRepository.deleteAll();
		filecontentTRepository.deleteAll();
		print("updating status", getLineNumber());

		batch.setBatchEndDate(new Date());
		batch.setBatchStatus(doneProgressBatch);
		batchesFFCRepository.saveAndFlush(batch);

		batch = batchesFFCRepository.findByKey("execStatus").get();
		logger.info("updateStatus");
		batch.setBatchStatus(0);
		ZoneId defaultZoneId = ZoneId.systemDefault();

		DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("yyMMdd");
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
		return ResponseEntity.ok().body(gson.toJson(filecontentTRepository.count()));

	}

	@GetMapping("cancelFile")
	public ResponseEntity<?> cancelFile() throws IOException {
		fileSummaryTRepository.deleteAll();
		filecontentTRepository.deleteAll();
		BatchesFC execstatus = batchesFFCRepository.findByKey("execStatus").get();
		execstatus.setBatchNumber(0);
		batchesFFCRepository.save(execstatus);
		batchesFFCRepository.updateFinishBatch("TP", -1, new Date());

		return ResponseEntity.ok().body(gson.toJson(filecontentTRepository.count()));

	}

	@PutMapping("checkDateTP")
	public ResponseEntity<?> checkDateTP(@RequestBody AddFileRequest addFileRequest) throws IOException {
		fileSummaryTRepository.findByfileDate(getLineNumber());
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

		DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("yyMMdd");
		LocalDate extractedDatelastFile = LocalDate.parse(addFileRequest.getFileDate(), formatter);
		LocalDate previousDay = extractedDatelastFile.minusDays(1);
		;

		LocalDate extractedDate = LocalDate.parse(addFileRequest.getFileDate(), formatter);

		LocalDate localDate = LocalDate.parse(addFileRequest.getFileDate(), formatter);
		print(localDate.format(formatter), getLineNumber());
		DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("yyMMdd");
		String ordinalDateString = localDate.format(outputFormatter);
		String fileName = addFileRequest.getFilePath() + "/" + addFileRequest.getFileName() + ordinalDateString
				+ ".txt";
		String fileNameprev = addFileRequest.getFilePath() + "/" + addFileRequest.getFileName()
				+ previousDay.format(inputFormatter) + ".txt";
		print(previousDay.format(inputFormatter), getLineNumber());
		print(fileSummaryRepository.findAll() + "", getLineNumber());
		print(fileName, getLineNumber());
		if (!Files.exists(Paths.get(fileName))) {
			return ResponseEntity.badRequest().body(gson.toJson(
					"ERROR 102 : La date du fichier sélectionné est introuvable voulez vous demarrer le processus de generation tp sans fichier ?"));
		}
		if (!fileSummaryRepository.findByfileDate(previousDay.format(inputFormatter)).isPresent()) {
			return ResponseEntity.badRequest().body(gson.toJson(
					"ERROR 101 : Il existe un gap dans la liste des fichiers intégrer voulez vous continuer ? "));
		}

		return ResponseEntity.ok().body(gson.toJson("OK"));

	}

	@PutMapping("addFileTP")
	public ResponseEntity<?> addFileTP(@RequestBody AddFileRequest addFileRequest) throws IOException {
		BatchesFC batch = batchesFFCRepository.findByBatchType("Integration").get(0);

		try {
			openingDRepository.deleteAll();
		} catch (Exception e) {
			logger.info("no openinday to delete");
		}
		int size = 0;
		try {
			if (!isValidDateFormat(addFileRequest.getFileDate())) {
				return ResponseEntity.status(HttpStatus.BAD_REQUEST)
						.body(gson.toJson("please verify that format date is YYYY-MM-dd"));

			}
			BatchesFC batche = batchesFFCRepository.findByKey("INCTF").get();
 			batche.setDateReg(addFileRequest.getFileDate());
			batchesFFCRepository.save(batche);
			integrationFilesInternational.runvisa(batche);
			List<BatchesFC> batches = batchesFFCRepository.findAll();
			batches.forEach(v -> {
				if (v.getKey().equals("TP")) {
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
			batchesFFCRepository.updateFinishBatch("SENDLOT", 10, new Date());
			batchesFFCRepository.updateFinishBatch("SENDORD", 10, new Date());
			FileRequest.print(batchesFFCRepository.findByKey("SENDLOT").toString(), getLineNumber());
			batchesFFCRepository.saveAll(batches);
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
			LocalDate localDate = LocalDate.parse(addFileRequest.getFileDate(), formatter);
			print(getLineNumber(), localDate.format(formatter));
			DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("yyMMdd");
			String ordinalDateString = localDate.format(outputFormatter);
			print(ordinalDateString, getLineNumber());
			String yearString = ordinalDateString.substring(2, 4);
			print(yearString, getLineNumber());
			print(addFileRequest.getFilePath(), getLineNumber());
			print(addFileRequest.getFileName(), getLineNumber());
			print(ordinalDateString, getLineNumber());
			String fileName = addFileRequest.getFilePath() + "/" + addFileRequest.getFileName() + ordinalDateString
					+ ".txt";
			// +

//Add header
			if (!(fileSummaryTRepository
					.findByFileNameAndFileDate(addFileRequest.getFileName(), addFileRequest.getFileDate())
					.isPresent())) {
				List<String> listDetails = new ArrayList<>();
				FileHeaderT f = new FileHeaderT();
				print(String.valueOf(fileSummaryTRepository
						.findByFileNameAndFileDate(addFileRequest.getFileName(), addFileRequest.getFileDate())
						.isPresent()), getLineNumber());
				System.out.println("this file existe" + addFileRequest.getFileDate());
				if (Files.exists(Paths.get(fileName))) {
					List<String> stream = Files.readAllLines(Paths.get(fileName), StandardCharsets.ISO_8859_1);

					f.setFileName(addFileRequest.getFileName());
					f.setFileDate(ordinalDateString);
					f.setFileprocessingDate(LocalDate.now().format(formatter));
					f.setDestinationBankIdentification(codeBankBCRepository.findAll().get(0).getIdentifiant());
					f = fileSummaryTRepository.save(f);
					int idHeader = f.getId();
					List<FileContentT> lists = new ArrayList<FileContentT>();

					stream.forEach(e -> {
						FileContentT fileContent = new FileContentT();
						fileContent.setCodeDebit(e.substring(0, 1).trim());
						fileContent.setCodeBin(e.substring(1, 7).trim());
						fileContent.setCodeBank(e.substring(7, 10).trim());
//<<<<<<< HEAD
//						
//						fileContent.setNumCartePorteur(e.substring(30, 49).trim());
//						
//						fileContent.setNumRIBEmetteur(e.substring(10, 30).trim());
//						
//						fileContent.setCodeAgence(e.substring(79, 84).trim());
//						
//						print("Code bank is"+fileContent.getCodeBank(),getLineNumber());
//						
//						if (fileContent.getCodeBank().equals("035")) {
//							// if bank FSBK go find the card
//							Optional<Card> card = cardRepository.findByCardNum(fileContent.getNumCartePorteur());
//							if (card.isPresent()) {
//								print("card founded " ,getLineNumber());
//
//								 Optional<Account> foundedAccount = accountRepository.findByAccountCode(card.get().getAccCode());
//								 if (foundedAccount.isPresent()) {
//										print("account founded " ,getLineNumber());
//										print("account num is "+foundedAccount.get().getAccountNum() ,getLineNumber());
//										//print("branch is "+foundedAccount.get().getAccountNum().substring(2,7),getLineNumber());
//									 fileContent.setNumRIBEmetteur("0"+foundedAccount.get().getAccountNum());
//								//	 fileContent.setCodeAgence(foundedAccount.get().getAccountNum().substring(2,7));
//
//								 }
//								
//							}
//							
//						}
//						
//						
//
//						
//						
//
//=======

						fileContent.setNumCartePorteur(e.substring(30, 49).trim());

						fileContent.setNumRIBEmetteur(e.substring(10, 30).trim());

						fileContent.setCodeAgence(e.substring(79, 84).trim());

						if (fileContent.getCodeBank().equals("035")) {
							// if bank FSBK go find the card
							Optional<Card> card = cardRepository.findByCardNum(fileContent.getNumCartePorteur());
							if (card.isPresent()) {
								print("card founded ", getLineNumber());

								Optional<Account> foundedAccount = accountRepository
										.findByAccountCode(card.get().getAccCode());
								if (foundedAccount.isPresent()) {
									print("account founded ", getLineNumber());
									print("account num is " + foundedAccount.get().getAccountNum(), getLineNumber());
									// print("branch is
									// "+foundedAccount.get().getAccountNum().substring(2,7),getLineNumber());
									fileContent.setNumRIBEmetteur("0" + foundedAccount.get().getAccountNum());
									// fileContent.setCodeAgence(foundedAccount.get().getAccountNum().substring(2,7));

								}

							}

						}

						fileContent.setCodeDebitCommercant(safeSub(e,49, 50).trim());
						fileContent.setNumRIBcommercant(safeSub(e,50, 70).trim());
						fileContent.setBinAcquereur(safeSub(e,70, 76).trim());
						fileContent.setCodeBankAcquereur(safeSub(e,76, 79).trim());

						fileContent.setIdTerminal(safeSub(e,84, 99).trim());
						fileContent.setIdCommercant(safeSub(e,99, 114).trim());

						fileContent.setTypeTransaction(safeSub(e,114, 117).trim());
						fileContent.setDateTransaction(safeSub(e,117, 125).trim());
						fileContent.setHeureTransaction(safeSub(e,125, 131).trim());
						fileContent.setMontantTransaction(safeSub(e,131, 146).trim());
						fileContent.setNumFacture(safeSub(e,146, 161).trim());
						fileContent.setEmetteurFacture(safeSub(e,161, 201).trim());
						fileContent.setNumRefTransaction(safeSub(e,201, 213).trim());
						fileContent.setNumAutorisation(safeSub(e,213, 228).trim());
						fileContent.setCodeDebitPorteur(safeSub(e,228, 229).trim());
						fileContent.setCommisionPorteur(safeSub(e,229, 241).trim());

						fileContent.setCodeDebitCommisionCommercant(safeSub(e,241, 242).trim());
						fileContent.setCommisionCommercant(safeSub(e,242, 254).trim());
						fileContent.setCommisionInterchange(safeSub(e,254, 266).trim());
						fileContent.setFraisOperateurTechnique(safeSub(e,266, 278).trim());

						fileContent.setAppCryptogram(safeSub(e,278, 294).trim());
						fileContent.setCryptogramInfoData(safeSub(e,294, 296).trim());
						fileContent.setAtc(safeSub(e,296, 300).trim());

						fileContent.setTerminalVerificationResult(safeSub(e,300, 310).trim());
						fileContent.setLibelleCommercant(safeSub(e,310, 350).trim());

//                        fileTP.setRuf(safeSub(e,350, 410).trim());
						//nouvelle partie
						fileContent.setModeLectureCarte(safeSub(e,350, 351).trim());
						fileContent.setMethodeVerificationPorteur(safeSub(e,351, 352).trim());
						fileContent.setTypeDispositifSansContract(safeSub(e,352, 353).trim());
						fileContent.setcommissionsGestionaireATM(safeSub(e,353, 365).trim());
						//partie chargeback
						fileContent.setIdChargeback(safeSub(e, 365,381));
						fileContent.setCodeMotifChargeback(safeSub(e, 381,385));
						fileContent.setNumAutorisationInitial(safeSub(e, 385,400));
						fileContent.setdateOPInitial(safeSub(e, 400,408));
						//supression de 2 caractère
						fileContent.setNumtransaction(safeSub(e,408, 420).trim());

						fileContent.setUdf1(safeSub(e,420, 440).trim());
						fileContent.setRufAcquereur(safeSub(e,440, 458).trim());
						//cas Retrait
						if(fileContent.getTypeTransaction().equals("040")){
						fileContent.setLocalisationATM(safeSub(e, 458, 478));
						fileContent.setReseau(safeSub(e, 458, 478));
						fileContent.setTrackId(safeSub(e, 458, 478));
						fileContent.setRufRetrait(safeSub(e, 458, 478));}
						//cas achat
						else{
						fileContent.setNumTransactionPaiementInternet(safeSub(e, 458, 478));
						fileContent.setTrackId(safeSub(e, 478, 488));
						fileContent.setIdOriginTransaction(safeSub(e, 488, 508));
						fileContent.setRufpaiement(safeSub(e, 508, 526).trim());}
						fileContent.setIdHeder(idHeader + "");
						lists.add(fileContent);
//
//						fileContent.setRuf(e.substring(350, 410).trim());
//						fileContent.setNumtransaction(e.substring(410, 422).trim());
//
//						fileContent.setUdf1(e.substring(422, 442).trim());
//						fileContent.setRufAcquereur(e.substring(442, 460).trim());
//						fileContent.setNumTransactionPaiementInternet(e.substring(460, 480).trim());
//						fileContent.setTrackId(e.substring(480, 490).trim());
//						fileContent.setIdOriginTransaction(e.substring(490, 510).trim());
//						fileContent.setRufpaiement(e.substring(510, 528).trim());
//						fileContent.setIdHeder(idHeader + "");
//						lists.add(fileContent);

					});
					size = lists.size();
					filecontentTRepository.saveAll(lists);

					FileRequest.print(batchesFFCRepository.findAll().toString(), FileRequest.getLineNumber());
				} else {
					batchesFFCRepository.updateStatusAndErrorBatch("TP", 2, "file not found", new Date(),
							"file not found");
					return ResponseEntity.badRequest().body(gson.toJson("file not found"));

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

	public class CheckUp {
		BigDecimal montantTransaction;
		String typeTransaction;
		String dateCompensation;
		int nb;
	}
	private static String safeSub(String s, int start, int end) {
		if (start >= s.length()) return "";
		if (end > s.length()) end = s.length();
		if (start < 0 || start > end) return "";
		return s.substring(start, end).trim();
	}
	@GetMapping("getCheckTP")
	public ResponseEntity<?> getCheckTP() {
		List<FileContentT> fileContents = filecontentTRepository.findAll();
		FileHeaderT header = fileSummaryTRepository.findAll().get(0);
		HashMap<String, CheckUp> list = new HashMap<>();
		for (FileContentT fc : fileContents) {
			if (!list.containsKey(fc.getTypeTransaction())) {
				CheckUp cp = new CheckUp();
				cp.montantTransaction = new BigDecimal(fc.getMontantTransaction());
				cp.dateCompensation = header.getFileDate();
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


	@DeleteMapping("deleteDuplicate/{id}")
	public ResponseEntity<?> deleteDuplicate(@PathVariable(value = "id") Integer id) {

		filecontentTRepository.deleteById(id);
		return ResponseEntity.ok().body(gson.toJson("element deleted  successfully!"));

	}

	@GetMapping("/countDuplicate")
	public ResponseEntity<?> countDuplicate() {
		List<Integer> x = filecontentTRepository.countDuplicate();
		Integer sum = x.stream().collect(Collectors.summingInt(Integer::intValue));
		return ResponseEntity.ok().body(gson.toJson(String.valueOf(sum)));

	}

	@PostMapping("/findDuplicate")
	public Page<FileContent> findDuplicate(@RequestParam(name = "page", defaultValue = "0") int page,
			@RequestParam(name = "size", defaultValue = "10") int size,
			@RequestParam(name = "sortOn", defaultValue = "id") String sortOn,
			@RequestParam(name = "dir", defaultValue = "asc") String dir) {

		sortOn = sortOn.substring(0, 1).toLowerCase() + sortOn.substring(1);

		List<Order> orders = new ArrayList<Order>();
		Order order1 = null;
		if (dir.equals("desc"))
			order1 = new Order(Sort.Direction.DESC, sortOn);
		else
			order1 = new Order(Sort.Direction.ASC, sortOn);
		orders.add(order1);
		return filecontentRepository.findDuplicate(PageRequest.of(page, size, Sort.by(orders)));

	}

	@PostMapping("/findDuplicateT")
	public Page<FileContentT> findDuplicateT(@RequestParam(name = "page", defaultValue = "0") int page,
			@RequestParam(name = "size", defaultValue = "10") int size,
			@RequestParam(name = "sortOn", defaultValue = "id") String sortOn,
			@RequestParam(name = "dir", defaultValue = "asc") String dir) {
		sortOn = sortOn.substring(0, 1).toLowerCase() + sortOn.substring(1);

		List<Order> orders = new ArrayList<Order>();
		Order order1 = null;
		if (dir.equals("desc"))
			order1 = new Order(Sort.Direction.DESC, sortOn);
		else
			order1 = new Order(Sort.Direction.ASC, sortOn);
		orders.add(order1);
		return filecontentTRepository.findDuplicate(PageRequest.of(page, size, Sort.by(orders)));

	}

	@PostMapping("/findByFilters")
	public Page<FileContent> findByFilters(@RequestParam(name = "page", defaultValue = "0") int page,
			@RequestParam(name = "size", defaultValue = "10") int size,
			@RequestParam(name = "sortOn", defaultValue = "id") String sortOn,
			@RequestParam(name = "dir", defaultValue = "asc") String dir, @RequestBody FileContentRequest fileContent) {
		print(sortOn, getLineNumber());
		sortOn = sortOn.substring(0, 1).toLowerCase() + sortOn.substring(1);
		print(sortOn, getLineNumber());

		List<Order> orders = new ArrayList<Order>();
		Order order1 = null;
		if (dir.equals("desc"))
			order1 = new Order(Sort.Direction.DESC, sortOn);
		else
			order1 = new Order(Sort.Direction.ASC, sortOn);
		orders.add(order1);
//		List<Integer> idheaders = fileProcessedRepository
//				.findIdByDateTakenBetween(fileContent.getDateCompensationStart(), fileContent.getDateCompensationEnd());

		List<Integer> idheaders = fileProcessedRepository.findIdByDate(fileContent.getDateCompensationStart());

		List<String> tpsummaries = null;
		if (!idheaders.isEmpty()) {
			tpsummaries = idheaders.stream().map(Object::toString).collect(Collectors.toList());
		}
		System.out.println("idheaders" + idheaders);
		System.out.println("tpsummaries" + tpsummaries);

		Page<FileContent> filesContent = filecontentRepository.findByFilters(
				PageRequest.of(page, size, Sort.by(orders)), tpsummaries, fileContent.getDateTransactionStart(),
				fileContent.getDateTransactionEnd(), fileContent.getCodeDebit(), fileContent.getCodeBin(),
				fileContent.getCodeBank(), fileContent.getNumRIBEmetteur(), fileContent.getNumCartePorteur(),
				fileContent.getCodeDebitCommercant(), fileContent.getNumRIBcommercant(), fileContent.getBinAcquereur(),
				fileContent.getCodeBankAcquereur(), fileContent.getCodeAgence(), fileContent.getIdTerminal(),
				fileContent.getIdCommercant(), fileContent.getTypeTransaction(), fileContent.getDateTransaction(),
				fileContent.getHeureTransaction(), fileContent.getMontantTransaction(), fileContent.getNumFacture(),
				fileContent.getEmetteurFacture(), fileContent.getNumRefTransaction(), fileContent.getNumAutorisation(),
				fileContent.getCodeDebitPorteur(), fileContent.getCommisionPorteur(),
				fileContent.getCodeDebitCommisionCommercant(), fileContent.getCommisionCommercant(),
				fileContent.getCommisionInterchange(), fileContent.getFraisOperateurTechnique(),
				fileContent.getAppCryptogram(), fileContent.getCryptogramInfoData(), fileContent.getAtc(),
				fileContent.getTerminalVerificationResult(), fileContent.getLibelleCommercant(), fileContent.getRuf(),
				fileContent.getNumtransaction(), fileContent.getValidCommission(), null);
		List<FileContent> list = filesContent.getContent();
		for (FileContent f : list) {
			f.setDateCompensation(
					fileProcessedRepository.findById(Integer.valueOf(f.getIdHeder())).get().getFileDate());
		}
		return filesContent;
	}

	@PostMapping("/findByFiltersToExport")
	public List<FileContent> findByFiltersToExport(@RequestParam(name = "page", defaultValue = "0") int page,
			@RequestParam(name = "size", defaultValue = "10") int size, @RequestBody FileContentRequest fileContent) {
		System.out.println("starting");
		List<Integer> idheaders = fileProcessedRepository
				.findIdByDateTakenBetween(fileContent.getDateCompensationStart(), fileContent.getDateCompensationEnd());
		List<String> tpsummaries = null;
		if (!idheaders.isEmpty()) {
			tpsummaries = idheaders.stream().map(Object::toString).collect(Collectors.toList());
		}
		System.out.println(idheaders);
		List<FileContent> filesContent = filecontentRepository.findByFilters(tpsummaries,
				fileContent.getDateTransactionStart(), fileContent.getDateTransactionEnd(), fileContent.getCodeDebit(),
				fileContent.getCodeBin(), fileContent.getCodeBank(), fileContent.getNumRIBEmetteur(),
				fileContent.getNumCartePorteur(), fileContent.getCodeDebitCommercant(),
				fileContent.getNumRIBcommercant(), fileContent.getBinAcquereur(), fileContent.getCodeBankAcquereur(),
				fileContent.getCodeAgence(), fileContent.getIdTerminal(), fileContent.getIdCommercant(),
				fileContent.getTypeTransaction(), fileContent.getDateTransaction(), fileContent.getHeureTransaction(),
				fileContent.getMontantTransaction(), fileContent.getNumFacture(), fileContent.getEmetteurFacture(),
				fileContent.getNumRefTransaction(), fileContent.getNumAutorisation(), fileContent.getCodeDebitPorteur(),
				fileContent.getCommisionPorteur(), fileContent.getCodeDebitCommisionCommercant(),
				fileContent.getCommisionCommercant(), fileContent.getCommisionInterchange(),
				fileContent.getFraisOperateurTechnique(), fileContent.getAppCryptogram(),
				fileContent.getCryptogramInfoData(), fileContent.getAtc(), fileContent.getTerminalVerificationResult(),
				fileContent.getLibelleCommercant(), fileContent.getRuf(), fileContent.getNumtransaction(),
				fileContent.getValidCommission(), null);
		for (FileContent f : filesContent) {
			f.setDateCompensation(
					fileProcessedRepository.findById(Integer.valueOf(f.getIdHeder())).get().getFileDate());
		}
		return filesContent;
	}

	public String correctDateFormat(String x) {
		if (x == null)
			return null;
		String input = x; // or "11/3/2023" or "15/10/2023"

		String[] parts = input.split("/");
		String day = parts[0].length() == 1 ? "0" + parts[0] : parts[0];
		String month = parts[1].length() == 1 ? "0" + parts[1] : parts[1];
		String year = parts[2];
		return day + "/" + month + "/" + year;
	}

	@PostMapping("/ListOfmovements")
	public Page<BkmvtiFransaBank> findAllMovements(@RequestParam(name = "page", defaultValue = "0") int page,
			@RequestParam(name = "size", defaultValue = "10") int size,
			@RequestParam(name = "sortOn", defaultValue = "id") String sortOn,
			@RequestParam(name = "dir", defaultValue = "asc") String dir, @RequestBody BkmvtiFransaBankRequest filtre) {

		sortOn = sortOn.substring(0, 1).toLowerCase() + sortOn.substring(1);

		List<Order> orders = new ArrayList<Order>();
		Order order1 = dir.equals("desc") 
			? new Order(Sort.Direction.DESC, sortOn) 
			: new Order(Sort.Direction.ASC, sortOn);
		orders.add(order1);

		filtre.setDvaStart(correctDateFormat(filtre.getDvaStart()));
		filtre.setDvaEnd(correctDateFormat(filtre.getDvaEnd()));
		filtre.setDcoStart(correctDateFormat(filtre.getDcoStart()));
		filtre.setDcoEnd(correctDateFormat(filtre.getDcoEnd()));


		Specification<BkmvtiFransaBank> spec = dynamicQueryService.buildBkmvtiSpecification(
				filtre.getDcoStart(), filtre.getDcoEnd(), filtre.getDvaStart(), filtre.getDvaEnd(),
				filtre.getCodeOperation(), filtre.getAgence(), filtre.getNumCompte(), filtre.getNumPiece(),
				filtre.getPieceComptable(), filtre.getRefDossier());

		// Debug: test spec query count
		long specCount = bkmvtiFransaBankRepository.count(spec);
		logger.info("DEBUG Specification query found {} records", specCount);

		if (page < 0) {
			return bkmvtiFransaBankRepository.findAll(spec,
					PageRequest.of(Long.valueOf(bkmvtiFransaBankRepository.count()).intValue(), size, Sort.by(orders)));
		}

		return bkmvtiFransaBankRepository.findAll(spec, PageRequest.of(page, size, Sort.by(orders)));
	}

	@PostMapping("/ListOfmovementsArchive")
	public Page<BkmHistory> findAllMovementsArchive(@RequestParam(name = "page", defaultValue = "0") int page,
			@RequestParam(name = "size", defaultValue = "10") int size,
			@RequestParam(name = "sortOn", defaultValue = "id") String sortOn,
			@RequestParam(name = "dir", defaultValue = "asc") String dir, @RequestBody BkmvtiFransaBankRequest filtre) {

		sortOn = sortOn.substring(0, 1).toLowerCase() + sortOn.substring(1);

		List<Order> orders = new ArrayList<Order>();
		Order order1 = dir.equals("desc") 
			? new Order(Sort.Direction.DESC, sortOn) 
			: new Order(Sort.Direction.ASC, sortOn);
		orders.add(order1);

		filtre.setDvaStart(correctDateFormat(filtre.getDvaStart()));
		filtre.setDvaEnd(correctDateFormat(filtre.getDvaEnd()));
		filtre.setDcoStart(correctDateFormat(filtre.getDcoStart()));
		filtre.setDcoEnd(correctDateFormat(filtre.getDcoEnd()));

		Specification<BkmHistory> spec = dynamicQueryService.buildBkmHistorySpecification(
				filtre.getDcoStart(), filtre.getDcoEnd(), filtre.getDvaStart(), filtre.getDvaEnd(),
				filtre.getCodeOperation(), filtre.getAgence(), filtre.getNumCompte(), filtre.getNumPiece(),
				filtre.getPieceComptable(), filtre.getRefDossier());

		if (page < 0) {
			return bkmHistoryRepository.findAll(spec,
					PageRequest.of(Long.valueOf(bkmHistoryRepository.count()).intValue(), size, Sort.by(orders)));
		}

		return bkmHistoryRepository.findAll(spec, PageRequest.of(page, size, Sort.by(orders)));
	}

	@GetMapping("/ListOfTransactionTypes")
	public List<TransactionType> findAllTransactionTypes() {

		return transactionTypesRepository.findAll();

	}

	@GetMapping("/ListOfTransactionTypesPaged")
	public Page<TransactionType> findAllTransactionTypesPaged(@RequestParam(name = "page", defaultValue = "0") int page,
			@RequestParam(name = "size", defaultValue = "10") int size,
			@RequestParam(name = "sortOn", defaultValue = "code") String sortOn,
			@RequestParam(name = "dir", defaultValue = "asc") String dir) {

		List<Order> orders = new ArrayList<Order>();
		Order order1 = null;
		if (dir.equals("desc"))
			order1 = new Order(Sort.Direction.DESC, sortOn);
		else
			order1 = new Order(Sort.Direction.ASC, sortOn);
		orders.add(order1);
		if (page < 0)
			return transactionTypesRepository.findAll(
					PageRequest.of(Long.valueOf(transactionTypesRepository.count()).intValue(), size, Sort.by(orders)));

		return transactionTypesRepository.findAll(PageRequest.of(page, size, Sort.by(orders)));
	}

	@GetMapping("/ListOflibCat")
	public List<ConfCategories> getListOflibCat() {
		return mvbConfCategoriesRep.findAll();
	}

	@GetMapping("/Listoflibs")
	public List<String> Listoflibs() {

		return mvbCongR.findDistinctBylibs();

	}

	@GetMapping("/ListOfMvbkSettlement")
	public Page<MvbkConf> findAllMvbkSettlementsPaged(@RequestParam(name = "page", defaultValue = "0") int page,
			@RequestParam(name = "size", defaultValue = "10") int size,
			@RequestParam(name = "sortOn", defaultValue = "code") String sortOn,
			@RequestParam(name = "dir", defaultValue = "asc") String dir,
			@RequestParam(name = "category", defaultValue = " ") String libCat) {
		List<Order> orders = new ArrayList<Order>();
		Order order1 = null;

		if (dir.equals("desc"))
			order1 = new Order(Sort.Direction.DESC, sortOn);
		else
			order1 = new Order(Sort.Direction.ASC, sortOn);
		orders.add(order1);
		if (page < 0 && " ".equals(libCat)) {
			FileRequest.print("error " + libCat, FileRequest.getLineNumber());

			return mvbCongR.findAll(PageRequest.of(Long.valueOf(mvbCongR.count()).intValue(), size, Sort.by(orders)));
		}
		if (page < 0 && !" ".equals(libCat)) {
			FileRequest.print("  " + libCat, FileRequest.getLineNumber());

			return mvbCongR.findBylibelleOperation(
					PageRequest.of(Long.valueOf(mvbCongR.count()).intValue(), size, Sort.by(orders)), libCat);
		}
		if (page >= 0 && !" ".equals(libCat)) {
			FileRequest.print("  " + libCat, FileRequest.getLineNumber());

			return mvbCongR.findBylibelleOperation(PageRequest.of(page, size, Sort.by(orders)), libCat);
		}
		return mvbCongR.findAll(PageRequest.of(page, size, Sort.by(orders)));
	}

	@GetMapping("/ListOfMvbkSettlementList")
	public List<MvbkConf> findAllMvbkSettlementsList() {

		return mvbCongR.findAll();
	}

	@PutMapping("/updateMvbkConf")
	public ResponseEntity<?> updateMvbkConf(@RequestBody MvbkConf element) {

		if (mvbCongR.existsById(element.getCode()))
			return ResponseEntity.ok().body(mvbCongR.save(element));

		else
			return ResponseEntity.badRequest().body("entity not found");
	}

	private String prepareResponse(String value) {
		try {
			double doubleValue = Double.parseDouble(value);
			if (doubleValue < 0) {
				return "0";
			} else {
				return value;
			}
		} catch (NumberFormatException e) {
			return value;
		}
	}

	@PutMapping("/updateInternetCommission")
	public ResponseEntity<?> updateInternetCommission(@RequestBody CommissionAchatInternetFB element) {

		if (element.getValeurMin() != null) {
			element.setValeurMin(prepareResponse(element.getValeurMin()));
		}

		if (element.getValeurMax() != null) {
			element.setValeurMax(prepareResponse(element.getValeurMax()));
		}

		if (element.getValeurFix() != null) {
			element.setValeurFix(prepareResponse(element.getValeurFix()));
		}

		if (element.getValeurVarivable() != null) {
			element.setValeurVarivable(prepareResponse(element.getValeurVarivable()));
		}

		if (element.getVariableCmi() != null) {
			element.setVariableCmi(prepareResponse(element.getVariableCmi()));
		}

		if (element.getFixCmi() != null) {
			element.setFixCmi(prepareResponse(element.getFixCmi()));
		}

		if (element.getVariableCpi() != null) {
			element.setVariableCpi(prepareResponse(element.getVariableCpi()));
		}

		if (element.getFixCpi() != null) {
			element.setFixCpi(prepareResponse(element.getFixCpi()));
		}

		if (element.getVariableCommissionAcq() != null) {
			element.setVariableCommissionAcq(prepareResponse(element.getVariableCommissionAcq()));
		}

		if (element.getFixCommissionAcq() != null) {
			element.setFixCommissionAcq(prepareResponse(element.getFixCommissionAcq()));
		}

		if (element.getVariableCommissionIssuer() != null) {
			element.setVariableCommissionIssuer(prepareResponse(element.getVariableCommissionIssuer()));
		}

		if (element.getFixCommissionIssuer() != null) {
			element.setFixCommissionIssuer(prepareResponse(element.getFixCommissionIssuer()));
		}

		if (element.getBankIssuer() != null) {
			element.setBankIssuer(prepareResponse(element.getBankIssuer()));
		}

		if (element.getBankAck() != null) {
			element.setBankAck(prepareResponse(element.getBankAck()));
		}

		// Repeat for other attributes as needed

		System.out.println(element);

		System.out.println(commissionAchatInternetFBRepository.existsById(element.getCode()));
		if (commissionAchatInternetFBRepository.existsById(element.getCode()))
			return ResponseEntity.ok().body(commissionAchatInternetFBRepository.save(element));

		else
			return ResponseEntity.badRequest().body("entity not found");
	}

	@DeleteMapping("/deleteInternetCommission/{id}")
	public ResponseEntity<?> deleteInternetCommission(@PathVariable(value = "id") Integer id) {
		if (commissionAchatInternetFBRepository.existsById(id)) {
			commissionAchatInternetFBRepository.delete(commissionAchatInternetFBRepository.findById(id).get());
			return ResponseEntity.ok().body(gson.toJson("element deleted"));
		} else

			return ResponseEntity.badRequest().body(gson.toJson("entity not found"));
	}

	@PostMapping("/addCsInternetCommission/{id}")
	public ResponseEntity<?> addCsInternetCommission(@PathVariable(value = "id") Integer id) {
		String m = org.apache.commons.lang3.StringUtils.leftPad(String.valueOf(id), 3, '0');

		CommissionAchatInternetFB csI52 = gson.fromJson(
				gson.toJson(commissionAchatInternetFBRepository.findById(1).get()), CommissionAchatInternetFB.class);
		CommissionAchatInternetFB csI53 = gson.fromJson(
				gson.toJson(commissionAchatInternetFBRepository.findById(4).get()), CommissionAchatInternetFB.class);
		CommissionAchatInternetFB csI62 = gson.fromJson(
				gson.toJson(commissionAchatInternetFBRepository.findById(5).get()), CommissionAchatInternetFB.class);
		csI52.setCode(0);
		csI52.setBankAck(m);
		commissionAchatInternetFBRepository.save(csI52);
		csI53.setCode(0);
		csI53.setBankAck(m);
		commissionAchatInternetFBRepository.save(csI53);
		csI62.setCode(0);
		csI62.setBankAck(m);
		commissionAchatInternetFBRepository.save(csI62);
		csI52 = gson.fromJson(gson.toJson(commissionAchatInternetFBRepository.findById(1).get()),
				CommissionAchatInternetFB.class);
		csI53 = gson.fromJson(gson.toJson(commissionAchatInternetFBRepository.findById(4).get()),
				CommissionAchatInternetFB.class);
		csI62 = gson.fromJson(gson.toJson(commissionAchatInternetFBRepository.findById(5).get()),
				CommissionAchatInternetFB.class);
		csI52.setCode(0);
		csI52.setBankAck(null);
		csI52.setBankIssuer(m);
		commissionAchatInternetFBRepository.save(csI52);
		csI53.setCode(0);
		csI53.setBankAck(null);
		csI53.setBankIssuer(m);
		commissionAchatInternetFBRepository.save(csI53);
		csI62.setCode(0);
		csI62.setBankAck(null);
		csI62.setBankIssuer(m);
		commissionAchatInternetFBRepository.save(csI62);
		return ResponseEntity.ok().body(gson.toJson("element added"));

	}

	@GetMapping("/CommisionAchat")
	public Page<CommissionAchatFransaBank> findAllCommisionAchat(
			@RequestParam(name = "page", defaultValue = "0") int page,
			@RequestParam(name = "size", defaultValue = "10") int size,
			@RequestParam(name = "sortOn", defaultValue = "code") String sortOn,
			@RequestParam(name = "dir", defaultValue = "asc") String dir) {
		List<Order> orders = new ArrayList<Order>();
		Order order1 = null;
		if (dir.equals("desc"))
			order1 = new Order(Sort.Direction.DESC, sortOn);
		else
			order1 = new Order(Sort.Direction.ASC, sortOn);
		orders.add(order1);
		if (page < 0)
			return commissionAchatFransaBankRepository.findAll(PageRequest
					.of(Long.valueOf(commissionAchatFransaBankRepository.count()).intValue(), size, Sort.by(orders)));

		return commissionAchatFransaBankRepository.findAll(PageRequest.of(page, size, Sort.by(orders)));
	}

	@GetMapping("/CommissionRetrait")
	public Page<CommissionFransaBank> findAllCommissionRetrait(
			@RequestParam(name = "page", defaultValue = "0") int page,
			@RequestParam(name = "size", defaultValue = "10") int size,
			@RequestParam(name = "sortOn", defaultValue = "code") String sortOn,
			@RequestParam(name = "dir", defaultValue = "asc") String dir) {
		List<Order> orders = new ArrayList<Order>();
		Order order1 = null;
		if (dir.equals("desc"))
			order1 = new Order(Sort.Direction.DESC, sortOn);
		else
			order1 = new Order(Sort.Direction.ASC, sortOn);
		orders.add(order1);
		if (page < 0)
			return commissionFransaBankRepository.findAll(PageRequest
					.of(Long.valueOf(commissionFransaBankRepository.count()).intValue(), size, Sort.by(orders)));

		return commissionFransaBankRepository.findAll(PageRequest.of(page, size, Sort.by(orders)));
	}

	@GetMapping("/CommisionAchatInternet")
	public Page<CommissionAchatInternetFB> findAllCommisionAchatInternet(
			@RequestParam(name = "page", defaultValue = "0") int page,
			@RequestParam(name = "size", defaultValue = "10") int size,
			@RequestParam(name = "sortOn", defaultValue = "code") String sortOn,
			@RequestParam(name = "dir", defaultValue = "asc") String dir) {
		List<Order> orders = new ArrayList<Order>();
		Order order1 = null;
		if (dir.equals("desc"))
			order1 = new Order(Sort.Direction.DESC, sortOn);
		else
			order1 = new Order(Sort.Direction.ASC, sortOn);
		orders.add(order1);
		System.out.println(page);
		Page<CommissionAchatInternetFB> data;
		if (size > 5000) {
			System.out.println(Long.valueOf(commissionAchatInternetFBRepository.count()).intValue());
			data = commissionAchatInternetFBRepository.findAll(PageRequest.of(0,
					Long.valueOf(commissionAchatInternetFBRepository.count()).intValue(), Sort.by(orders)));
		}
		data = commissionAchatInternetFBRepository.findAll(PageRequest.of(page, size, Sort.by(orders)));
		return data;
	}

	@PutMapping("/updateCommissionRetrait")
	public ResponseEntity<?> updateCommissionRetrait(@RequestBody CommissionFransaBank element) {
		if (element.getType() != null) {
			element.setType(prepareResponse(element.getType()));
		}

		if (element.getTypeAcq() != null) {
			element.setTypeAcq(prepareResponse(element.getTypeAcq()));
		}

		if (element.getTypeIssuer() != null) {
			element.setTypeIssuer(prepareResponse(element.getTypeIssuer()));
		}

		if (element.getCommissionAcq() != null) {
			element.setCommissionAcq(prepareResponse(element.getCommissionAcq()));
		}

		if (element.getCommissionIssuer() != null) {
			element.setCommissionIssuer(prepareResponse(element.getCommissionIssuer()));
		}

		if (element.getCmi() != null) {
			element.setCmi(prepareResponse(element.getCmi()));
		}

		if (element.getCpi() != null) {
			element.setCpi(prepareResponse(element.getCpi()));
		}

		if (element.getCommissionFixe() != null) {
			element.setCommissionFixe(prepareResponse(element.getCommissionFixe()));
		}

		if (element.getCommissionVariable() != null) {
			element.setCommissionVariable(prepareResponse(element.getCommissionVariable()));
		}

		if (element.getTypeAcqIssuer() != null) {
			element.setTypeAcqIssuer(prepareResponse(element.getTypeAcqIssuer()));
		}

		if (commissionFransaBankRepository.existsById(element.getCode()))
			return ResponseEntity.ok().body(commissionFransaBankRepository.save(element));

		else
			return ResponseEntity.badRequest().body("entity not found");

	}

	@PostMapping("/addCommissionRetrait")
	public ResponseEntity<?> addCommissionRetrait(@RequestBody CommissionFransaBank crb) {

		crb.setCode(null);
		FileRequest.print(crb.toString(), getLineNumber());
		commissionFransaBankRepository.save(crb);
		return ResponseEntity.ok().body(gson.toJson("element added"));

	}

	@DeleteMapping("/deleteCommissionRetrait/{id}")
	public ResponseEntity<?> deleteCommissionRetrait(@PathVariable Integer id) {
		commissionFransaBankRepository.deleteById(id);

		return ResponseEntity.ok().body(gson.toJson("element deleted"));

	}

	@GetMapping("/CommissionRetraitFsbkOB")
	public CommissionFransaBank findCommissionRetraitACQOB() {
		return commissionFransaBankRepository.findByCodeIssuer("040");

	}

	@GetMapping("/CommissionRetraitOBFSBK")
	public CommissionFransaBank findCommissionRetraitIssuerOB() {
		return commissionFransaBankRepository.findByCodeAcq("040");

	}

	@GetMapping("/CommissionConsultationSoldeOB")
	public CommissionFransaBank findCommissionConsultationSoldeOB() {
		return commissionFransaBankRepository.findByCodeAcq("014");

	}

	@GetMapping("/CommissionRetraitByOnUs")
	public List<CommissionFransaBank> findCommissionRetraitByOnUs(
			@RequestParam(name = "codeBin", defaultValue = "014") String codeBin) {
		return commissionFransaBankRepository.findByTypeOnUsIssuerNull(codeBin);

	}

	@GetMapping("/findAllCommissionRetraitHavingTypeAcqIssuer")
	public List<CommissionFransaBank> findAllCommissionRetrait() {
		return commissionFransaBankRepository.findByTypeAcqIssuerIsNotNull();
	}

	@GetMapping("/fransaBanksLIst")
	public List<BankFransaBank> findAllFransaBankList() {
		return bankFransaRepository.findAll();
	}

	@PostMapping("/addfransaBanks")
	public BankFransaBank addfransaBanks(@RequestBody BankFransaBank bfb) {
		return bankFransaRepository.save(bfb);
	}

	@PutMapping("/updateCommissionAchat")
	public ResponseEntity<?> updateCommissionAchat(@RequestBody CommissionAchatFransaBank element) {
		if (element.getValeurMin() != null) {
			element.setValeurMin(prepareResponse(element.getValeurMin()));
		}
		if (element.getValeurMax() != null) {
			element.setValeurMax(prepareResponse(element.getValeurMax()));
		}
		if (element.getValeurFix() != null) {
			element.setValeurFix(prepareResponse(element.getValeurFix()));
		}
		if (element.getValeurVarivable() != null) {
			element.setValeurVarivable(prepareResponse(element.getValeurVarivable()));
		}
		if (element.getCmi() != null) {
			element.setCmi(prepareResponse(element.getCmi()));
		}
		if (element.getCpi() != null) {
			element.setCpi(prepareResponse(element.getCpi()));
		}

		if (commissionAchatFransaBankRepository.existsById(element.getCode()))
			return ResponseEntity.ok().body(commissionAchatFransaBankRepository.save(element));

		else
			return ResponseEntity.badRequest().body(gson.toJson("entity not found"));

	}

	@PostMapping("/addCommissionAchat")
	public ResponseEntity<?> addCommissionAchat(@RequestBody CommissionAchatFransaBank crb) {

		crb.setCode(null);
		return ResponseEntity.ok().body(commissionAchatFransaBankRepository.save(crb));

	}

	@DeleteMapping("/deleteCommissionAchat/{id}")
	public ResponseEntity<?> deleteCommissionAchat(@PathVariable Integer id) {
		commissionAchatFransaBankRepository.deleteById(id);

		return ResponseEntity.ok().body(gson.toJson("element deleted"));

	}

	@GetMapping("/CommisionAchatList")
	public List<CommissionAchatFransaBank> findAllCommisionAchatList() {
		return commissionAchatFransaBankRepository.findAll();
	}

	@GetMapping("/getCommissionOnUsFransaBank")
	public List<CommissionFransabankOnUs> getCommissionOnUsFransaBank() {
		return commissionOnUsFransaBankRepository.findAll();
	}

	@PutMapping("/updateCommissionOnUsFransaBank")
	public ResponseEntity<?> updateCommissionOnUsFransaBank(@RequestBody CommissionFransabankOnUs element) {
		if (element.getCmi() != null) {
			element.setCmi(prepareResponse(element.getCmi()));
		}

		if (element.getCommissionFixe() != null) {
			element.setCommissionFixe(prepareResponse(element.getCommissionFixe()));
		}

		if (element.getCommissionVariable() != null) {
			element.setCommissionVariable(prepareResponse(element.getCommissionVariable()));
		}

		if (element.getMax() != null) {
			element.setMax(prepareResponse(element.getMax()));
		}

		if (element.getMin() != null) {
			element.setMin(prepareResponse(element.getMin()));
		}

		if (commissionOnUsFransaBankRepository.existsById(element.getCode()))
			return ResponseEntity.ok().body(commissionOnUsFransaBankRepository.save(element));
		else
			return ResponseEntity.badRequest().body(gson.toJson("entity not found"));
	}

	@GetMapping("getAllBatchTP")
	public List<BatchesFC> getAllBatchPos() {

		return batchesFFCRepository.findAll();
	}

	@PostMapping("/Overview")
	public ResponseEntity<?> overview(@RequestBody FileContentRequest fc) throws IOException {
		List<Integer> idheaders = fileProcessedRepository.findIdByDateTakenBetween(fc.getDateCompensationStart(),
				fc.getDateCompensationEnd());
		print(idheaders.toString(), getLineNumber());

		if (idheaders.size() == 1)
			return ResponseEntity.ok().body(gson.toJson(findbyList(idheaders.get(0))));
		else
			return ResponseEntity.ok().body(gson.toJson(findbyList(0)));

	}

	public List<SummaryFileContent> findbyList(int idHeader) {
		List<SummaryFileContent> elements = new ArrayList<>();

		try {
			List<Object[]> queryResult = filecontentRepository.summary(idHeader);

			for (Object[] obj : queryResult) {

				if (obj[0] != null && obj[0] != null) {
					SummaryFileContent sm = new SummaryFileContent();
					long total = (long) obj[0];
					sm.total = total;
					sm.validCommission = (String) obj[1];
					sm.type_transaction = (String) obj[2];
					elements.add(sm);
				}
			}

		} catch (Exception e) {

			print("error", getLineNumber());
			e.printStackTrace();
		}
		return elements;

	}

	@GetMapping("/AllStatusFileRequest")
	public ResponseEntity<?> findAllStatusFileRequest() throws IOException {

		return ResponseEntity.ok().body(gson.toJson(statusFileContent.findAll()));

	}

	@PostMapping("/getByTransactionType")
	public Page<FileContent> findByTransactionType(@RequestParam(name = "page", defaultValue = "0") int page,
			@RequestParam(name = "size", defaultValue = "10") int size,
			@RequestParam(name = "sortOn", defaultValue = "code") String sortOn,
			@RequestParam(name = "dir", defaultValue = "asc") String dir, @RequestBody String transactionType) {

		List<Order> orders = new ArrayList<Order>();
		Order order1 = null;
		if (dir.equals("desc"))
			order1 = new Order(Sort.Direction.DESC, sortOn);
		else
			order1 = new Order(Sort.Direction.ASC, sortOn);
		orders.add(order1);
		if (page < 0)
			return filecontentRepository.findByTypeTransaction(
					PageRequest.of(Long.valueOf(transactionTypesRepository.count()).intValue(), size, Sort.by(orders)),
					transactionType);
		return filecontentRepository.findByTypeTransaction(PageRequest.of(page, size, Sort.by(orders)),
				transactionType);

	}

	@PostMapping("/getByValidCommission")
	public Page<FileContent> findByValidCommission(@RequestParam(name = "page", defaultValue = "0") int page,
			@RequestParam(name = "size", defaultValue = "10") int size,
			@RequestParam(name = "sortOn", defaultValue = "code") String sortOn,
			@RequestParam(name = "dir", defaultValue = "asc") String dir, @RequestBody String validCommission) {

		List<Order> orders = new ArrayList<Order>();
		Order order1 = null;
		if (dir.equals("desc"))
			order1 = new Order(Sort.Direction.DESC, sortOn);
		else
			order1 = new Order(Sort.Direction.ASC, sortOn);
		orders.add(order1);
		if (page < 0)
			return filecontentRepository.findByTypeTransaction(
					PageRequest.of(Long.valueOf(transactionTypesRepository.count()).intValue(), size, Sort.by(orders)),
					validCommission);
		return filecontentRepository.findByTypeTransaction(PageRequest.of(page, size, Sort.by(orders)),
				validCommission);

	}

	@PostMapping("/getByValidCommissionAndTypeTransaction")
	public Page<FileContent> findByValidCommissionAndTypeTransaction(
			@RequestParam(name = "page", defaultValue = "0") int page,
			@RequestParam(name = "size", defaultValue = "10") int size,
			@RequestParam(name = "sortOn", defaultValue = "code") String sortOn,
			@RequestParam(name = "dir", defaultValue = "asc") String dir,
			@RequestParam(name = "transactionType", defaultValue = "code") String transactionType,
			@RequestParam(name = "validCommission", defaultValue = "asc") String validCommission) {

		List<Order> orders = new ArrayList<Order>();
		Order order1 = null;
		if (dir.equals("desc"))
			order1 = new Order(Sort.Direction.DESC, sortOn);
		else
			order1 = new Order(Sort.Direction.ASC, sortOn);
		orders.add(order1);
		if (page < 0)
			return filecontentRepository.findByValidCommissionAndTypeTransaction(
					PageRequest.of(0, Long.valueOf(filecontentRepository.count()).intValue(), Sort.by(orders)),
					validCommission, transactionType);
		return filecontentRepository.findByValidCommissionAndTypeTransaction(
				PageRequest.of(page, size, Sort.by(orders)), validCommission, transactionType);

	}

	@GetMapping("/findAllRejetMotifs")
	List<MotifRejet> getAllRejetMotifs() {

		return motifRejetRepository.findAll();
	}

	@PostMapping("/findmatchedwithSwitch")
	public Page<FileContentProjection> findmatchedwithSwitch(@RequestParam(name = "page", defaultValue = "0") int page,
			@RequestParam(name = "size", defaultValue = "10") int size,
			@RequestParam(name = "sortOn", defaultValue = "code") String sortOn,
			@RequestParam(name = "dir", defaultValue = "asc") String dir) {
		List<Order> orders = new ArrayList<Order>();
		Order order1 = null;
		if (dir.equals("desc"))
			order1 = new Order(Sort.Direction.DESC, sortOn);
		else
			order1 = new Order(Sort.Direction.ASC, sortOn);
		orders.add(order1);
		Page<FileContentProjection> elemnts;
		if (page < 0)
			elemnts = filecontentRepository.findMatchingSwitch(
					PageRequest.of(page, Long.valueOf(filecontentRepository.count()).intValue(), Sort.by(orders)));
		else
			elemnts = filecontentRepository.findMatchingSwitch(PageRequest.of(page, size, Sort.by(orders)));

		return elemnts;
	}

	@PostMapping("/findnotmatchedwithSwitch")
	public Page<FileContent> findnotmatchedwithSwitch(@RequestParam(name = "page", defaultValue = "0") int page,
			@RequestParam(name = "size", defaultValue = "10") int size,
			@RequestParam(name = "sortOn", defaultValue = "code") String sortOn,
			@RequestParam(name = "dir", defaultValue = "asc") String dir) {
		List<Order> orders = new ArrayList<Order>();
		Order order1 = null;
		if (dir.equals("desc"))
			order1 = new Order(Sort.Direction.DESC, sortOn);
		else
			order1 = new Order(Sort.Direction.ASC, sortOn);
		orders.add(order1);
		if (page < 0)
			return filecontentRepository.findnotMatchingSwitch(
					PageRequest.of(page, Long.valueOf(filecontentRepository.count()).intValue(), Sort.by(orders)));
		return filecontentRepository.findnotMatchingSwitch(PageRequest.of(page, size, Sort.by(orders)));

	}

	@PostMapping("/tpcontrol")
	public TotalControlDisplay findAllTpControl(@RequestBody TpControlFilter filter,
			@RequestParam(name = "page", defaultValue = "0") int page,
			@RequestParam(name = "size", defaultValue = "10") int size,
			@RequestParam(name = "sortOn", defaultValue = "code") String sortOn,
			@RequestParam(name = "dir", defaultValue = "asc") String dir) {
		List<Order> orders = new ArrayList<Order>();
		Order order1 = null;
		if (dir.equals("desc"))
			order1 = new Order(Sort.Direction.DESC, sortOn);
		else
			order1 = new Order(Sort.Direction.ASC, sortOn);
		orders.add(order1);
		// Page<TpControlDisplay> data=null;
		List<TpControlDisplay> list = new ArrayList<TpControlDisplay>();

		Page<TpControl> listTps = tpControlRespository.findByProcessingDate(PageRequest.of(page, size, Sort.by(orders)),
				filter.getStartProcessingDate(), filter.getEndProcessingDate()
//				,filter.getStartFileDate(),
//				filter.getEndFileDate()
		);

		long totalGenTpTransactionsNb = 0;
		long totalGenSumTp = 0;

		long totalGenNbPres = 0;
		long totalGensumPres = 0;

		long totalGenNbNonPres = 0;
		long totalGensumNonPres = 0;

		for (TpControl tpControl : listTps) {
			TpControlDisplay tpControlDisplay = new TpControlDisplay();
			tpControlDisplay.setFileName(tpControl.getFileName());
			tpControlDisplay.setProcessingDate(tpControl.getProcessingDate());

			// Nombre trx par TP
			tpControlDisplay.setTpTransactionsNb(tpControl.getTpTransactionsNb());
			totalGenTpTransactionsNb = totalGenTpTransactionsNb + tpControlDisplay.getTpTransactionsNb();

			// Somme (montant à compenser) par TP
			tpControlDisplay.setSumTp(tpControl.getSumTp());
			totalGenSumTp = totalGenSumTp + tpControlDisplay.getSumTp();

			// Nombre trx présentées
			tpControlDisplay.setNbTotalPres(tpControl.getNbTotalPres());
			totalGenNbPres = totalGenNbPres + tpControlDisplay.getNbTotalPres();

			// Montant trx présentées
			tpControlDisplay.setSumTotalPres(tpControl.getSumTotalPres());
			totalGensumPres = totalGensumPres + tpControlDisplay.getSumTotalPres();

			// Nombre trx non présentées
			tpControlDisplay.setNbTotalNonPres(tpControl.getTpTransactionsNb() - tpControl.getNbTotalPres());
			totalGenNbNonPres = totalGenNbNonPres + tpControlDisplay.getNbTotalNonPres();

			// Montant trx non présentées
			tpControlDisplay.setSumTotalNomPres(tpControl.getSumTp() - tpControl.getSumTotalPres());
			totalGensumNonPres = totalGensumNonPres + tpControlDisplay.getSumTotalNomPres();

			list.add(tpControlDisplay);

		}

		Page<TpControlDisplay> data = new PageImpl<TpControlDisplay>(list, PageRequest.of(page, size, Sort.by(orders)),
				listTps.getTotalElements());

		TotalControlDisplay dataToReturn = new TotalControlDisplay();
		dataToReturn.setDataControl(data);
		dataToReturn.setTotalGenTpTransactionsNb(totalGenTpTransactionsNb);
		dataToReturn.setTotalGenSumTp(totalGenSumTp);

		dataToReturn.setTotalGenNbPres(totalGenNbPres);
		dataToReturn.setTotalGensumPres(totalGensumPres);

		dataToReturn.setTotalGenNbNonPres(totalGenNbNonPres);

		dataToReturn.setTotalGensumNonPres(totalGensumNonPres);

		return dataToReturn;

	}

	@GetMapping("/getMaxTpControlDate")
	public TpControlDates getMaxTpControlDate() {

		List<Date> processigDate = tpControlRespository.findMaxProcessingDate();
//		List<Date> fileDate=tpControlRespository.findMaxFileDate();
		TpControlDates dates = new TpControlDates();

		if (processigDate.size() > 0)
			dates.setProcessingDate(processigDate.get(0));

//		if (fileDate.size()>0)
//			dates.setFileDate(fileDate.get(0));

		return dates;

	}

	@PostMapping("/croControl")
	public List<CroControlDisplay> findAllCroControl(@RequestBody CroControlFilter filter
	// ,
//			@RequestParam(name = "page", defaultValue = "0") int page,
//			@RequestParam(name = "size", defaultValue = "10") int size,
//			@RequestParam(name = "sortOn", defaultValue = "id") String sortOn,
//			@RequestParam(name = "dir", defaultValue = "asc") String dir
	) throws ParseException {
//		List<Order> orders = new ArrayList<Order>();
//		Order order1 = null;
//		if (dir.equals("desc"))
//			order1 = new Order(Sort.Direction.DESC, sortOn);
//		else
//			order1 = new Order(Sort.Direction.ASC, sortOn);
//		orders.add(order1);
		// Page<TpControlDisplay> data=null;
		List<CroControl> data = croControlRespository.findByProcessingDate(filter.getStartDateReg(),
				filter.getEndDateReg());
		// Page<CroControl> data=
		// croControlRespository.findByDateReg(PageRequest.of(page, size,
		// Sort.by(orders)),filter.getDateReg());
//		 Map<String, Map<Object, List<CroControl>>> map= 
//				 data.collect(Collectors.groupingBy(CroControl::getStud_name)));
		List<CroControlDisplay> croControlDisplayList = new ArrayList<CroControlDisplay>();

		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");

		// Map<String, List<CroControl>> dataPerDateReg =
		// data.stream().collect(Collectors.groupingBy(CroControl::getDateReg));

		Map<String, List<CroControl>> dataPerDateReg = data.stream().collect(Collectors.groupingBy(

				e -> dateFormat.format(e.getProcessingDate())));

		for (String dateReg : dataPerDateReg.keySet()) {
			logger.info("dateReg is {}", dateReg);

			CroControlDisplay croControlDisplay = new CroControlDisplay();
			croControlDisplay.setDateReg(dateFormat.parse(dateReg));
			List<CroDataList> croDataList = new ArrayList<CroDataList>();

			Map<String, List<CroControl>> dataPerCroType = dataPerDateReg.get(dateReg).stream()
					.collect(Collectors.groupingBy(CroControl::getTypeCro));

			for (String croType : dataPerCroType.keySet()) {
				logger.info("croType is {}", croType);
				CroDataList croDataInList = new CroDataList();
				croDataInList.setTypeCro(croType);
				List<CroData> croData = new ArrayList<CroData>();

				long sumValidated = 0;
				long nbTotalValidated = 0;

				long sumRejected = 0;
				long nbTotalRejected = 0;

				long sumExtra = 0;
				long nbTotalExtra = 0;

				for (CroControl item : dataPerCroType.get(croType)) {
					CroData croInList = new CroData();
					croInList.setFileName(item.getFileName());
					croInList.setProcessingDate(item.getProcessingDate());
					croInList.setNbTotalFromFile(item.getNbTotalFromFile());
					croInList.setSumFromFile(item.getSumFromFile());

					croData.add(croInList);

					sumValidated = item.getSumValidated();
					sumRejected = item.getSumRejected();
					sumExtra = item.getSumExtra();

					nbTotalValidated = item.getNbTotalValidated();
					nbTotalRejected = item.getNbTotalRejected();
					nbTotalExtra = item.getNbTotalExtra();

				}

				croDataInList.setNbTotalExtra(nbTotalExtra);
				croDataInList.setNbTotalRejected(nbTotalRejected);
				croDataInList.setNbTotalValidated(nbTotalValidated);

				croDataInList.setSumExtra(sumExtra);
				croDataInList.setSumRejected(sumRejected);
				croDataInList.setSumValidated(sumValidated);

				croDataInList.setCroData(croData);
				croDataList.add(croDataInList);
			}

			croControlDisplay.setCroDataList(croDataList);

			croControlDisplayList.add(croControlDisplay);
		}

		return croControlDisplayList;

	}

	@PostMapping("/croRejetControl")
	public List<CroControlDisplay> findAllCroRejetControl(@RequestBody CroControlFilter filter) throws ParseException {

		List<CroRejetControl> data = croRejetControlRepository.findByProcessingDate(filter.getStartDateReg(),
				filter.getEndDateReg());

		List<CroControlDisplay> croControlDisplayList = new ArrayList<CroControlDisplay>();

		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");

		Map<String, List<CroRejetControl>> dataPerDateReg = data.stream().collect(Collectors.groupingBy(

				e -> dateFormat.format(e.getProcessingDate())));

		for (String dateReg : dataPerDateReg.keySet()) {
			logger.info("dateProcessing is {}", dateReg);

			CroControlDisplay croControlDisplay = new CroControlDisplay();
			croControlDisplay.setDateReg(dateFormat.parse(dateReg));
			List<CroDataList> croDataList = new ArrayList<CroDataList>();

			Map<String, List<CroRejetControl>> dataPerCroType = dataPerDateReg.get(dateReg).stream()
					.collect(Collectors.groupingBy(CroRejetControl::getTypeCro));

			for (String croType : dataPerCroType.keySet()) {
				logger.info("croType is {}", croType);
				CroDataList croDataInList = new CroDataList();
				croDataInList.setTypeCro(croType);
				List<CroData> croData = new ArrayList<CroData>();

				long sumValidated = 0;
				long nbTotalValidated = 0;

//					long sumRejected = 0;
//					long nbTotalRejected = 0;
//
//					long sumExtra = 0;
//					long nbTotalExtra = 0;

				for (CroRejetControl item : dataPerCroType.get(croType)) {
					CroData croInList = new CroData();
					croInList.setFileName(item.getFileName());
					croInList.setProcessingDate(item.getProcessingDate());
					croInList.setNbTotalFromFile(item.getNbTotalFromFile());
					// croInList.setSumFromFile(item.getSumFromFile());

					croData.add(croInList);

					// sumValidated=item.getSumValidated();
//    	    		 sumRejected=item.getSumRejected();
//    	    		 sumExtra=item.getSumExtra();

					nbTotalValidated = item.getNbTotalValidated();
//    	    		 nbTotalRejected=item.getNbTotalRejected();
//    	    		 nbTotalExtra=item.getNbTotalExtra();

				}

//    	    	 croDataInList.setNbTotalExtra(nbTotalExtra);
//    	    	 croDataInList.setNbTotalRejected(nbTotalRejected);
				croDataInList.setNbTotalValidated(nbTotalValidated);

//    	    	 croDataInList.setSumExtra(sumExtra);
//    	    	 croDataInList.setSumRejected(sumRejected);
//    	    	 croDataInList.setSumValidated(sumValidated);

				croDataInList.setCroData(croData);
				croDataList.add(croDataInList);
			}

			croControlDisplay.setCroDataList(croDataList);

			croControlDisplayList.add(croControlDisplay);
		}

		return croControlDisplayList;

	}

	@GetMapping("/getMaxCroControlDate")
	public TpControlDates getMaxCroControlDate() {

		List<Date> processigDate = croControlRespository.findMaxDateReg();
//		List<Date> fileDate=tpControlRespository.findMaxFileDate();
		TpControlDates dates = new TpControlDates();

		if (processigDate.size() > 0)
			dates.setProcessingDate(processigDate.get(0));

//		if (fileDate.size()>0)
//			dates.setFileDate(fileDate.get(0));

		return dates;
	}

	@GetMapping("/getMaxCroRejetControlDate")
	public TpControlDates getMaxCroRejetControlDate() {

		List<Date> processigDate = croRejetControlRepository.findMaxDateReg();
//		List<Date> fileDate=tpControlRespository.findMaxFileDate();
		TpControlDates dates = new TpControlDates();

		if (processigDate.size() > 0)
			dates.setProcessingDate(processigDate.get(0));

//		if (fileDate.size()>0)
//			dates.setFileDate(fileDate.get(0));

		return dates;
	}

	@PostMapping("/cracontrol")
	public List<CraControlDisplay> findAllCraControl(@RequestBody TpControlFilter filter

	) throws ParseException {

		List<CraControl> data = craControlRespository.findByProcessingDate(filter.getStartProcessingDate(),
				filter.getEndProcessingDate());

		List<CraControlDisplay> craControlDisplayList = new ArrayList<CraControlDisplay>();

		Map<String, List<CraControl>> dataPerProcessingDate = data.stream()
				.collect(Collectors.groupingBy(CraControl::getProcessingDate));
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
		for (String dateReg : dataPerProcessingDate.keySet()) {
			logger.info("dateReg is {}", dateReg);

			CraControlDisplay craControlDisplay = new CraControlDisplay();
			craControlDisplay.setFileIntegrationDate(dateReg);

			List<CraDataList> craDataList = new ArrayList<CraDataList>();

			Map<String, List<CraControl>> dataPerCraType = dataPerProcessingDate.get(dateReg).stream()
					.collect(Collectors.groupingBy(CraControl::getLotType));

			for (String craType : dataPerCraType.keySet()) {
				logger.info("craType is {}", craType);
				CraDataList craDataInList = new CraDataList();
				craDataInList.setTypeCra(craType);
				List<CraData> craData = new ArrayList<CraData>();

				long sumValidated = 0;
				long nbTotalValidated = 0;

				long sumRejected = 0;
				long nbTotalRejected = 0;

//					long sumExtra = 0;
//					long nbTotalExtra = 0;

				for (CraControl item : dataPerCraType.get(craType)) {
					CraData croInList = new CraData();
					croInList.setFileName(item.getLotFileName());
					// croInList.setProcessingDate(item.getProcessingDate());
					croInList.setNbTotalFromFile(item.getNbTotalInFile());
					croInList.setSumFromFile(item.getSumInFile());

					craData.add(croInList);

					sumValidated = item.getSumAccepted();
					sumRejected = item.getSumNotAccepted();

					nbTotalValidated = item.getNbAccepted();
					nbTotalRejected = item.getNbNotAccepted();

				}

				craDataInList.setNbTotalRejected(nbTotalRejected);
				craDataInList.setNbTotalValidated(nbTotalValidated);

				craDataInList.setSumRejected(sumRejected);
				craDataInList.setSumValidated(sumValidated);

				craDataInList.setCraData(craData);
				craDataList.add(craDataInList);
			}

			craControlDisplay.setCraDataList(craDataList);

			craControlDisplayList.add(craControlDisplay);
		}

		return craControlDisplayList;

	}

	@PostMapping("/craRejetcontrol")
	public List<CraControlDisplay> findAllCraRejetControl(@RequestBody TpControlFilter filter

	) throws ParseException {

		List<CraRejetControl> data = craRejetControlRespository.findByProcessingDate(filter.getStartProcessingDate(),
				filter.getEndProcessingDate());

		List<CraControlDisplay> craControlDisplayList = new ArrayList<CraControlDisplay>();

		Map<String, List<CraRejetControl>> dataPerProcessingDate = data.stream()
				.collect(Collectors.groupingBy(CraRejetControl::getCraRejetDate));
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
		for (String dateReg : dataPerProcessingDate.keySet()) {
			logger.info("dateReg is {}", dateReg);

			CraControlDisplay craControlDisplay = new CraControlDisplay();
			craControlDisplay.setFileIntegrationDate(dateReg);

			List<CraDataList> craDataList = new ArrayList<CraDataList>();

			Map<String, List<CraRejetControl>> dataPerCraType = dataPerProcessingDate.get(dateReg).stream()
					.collect(Collectors.groupingBy(CraRejetControl::getLotType));

			for (String craType : dataPerCraType.keySet()) {
				logger.info("craType is {}", craType);
				CraDataList craDataInList = new CraDataList();
				craDataInList.setTypeCra(craType);
				List<CraData> craData = new ArrayList<CraData>();

				long sumValidated = 0;
				long nbTotalValidated = 0;

				long sumRejected = 0;
				long nbTotalRejected = 0;
				String processingDate = "";
//					long sumExtra = 0;
//					long nbTotalExtra = 0;

				for (CraRejetControl item : dataPerCraType.get(craType)) {
					CraData croInList = new CraData();
					croInList.setFileName(item.getLotFileName());
					// croInList.setProcessingDate(item.getProcessingDate());
					croInList.setNbTotalFromFile(item.getNbTotalInFile());
					croInList.setSumFromFile(item.getSumInFile());

					craData.add(croInList);

					sumValidated = item.getSumAccepted();
					sumRejected = item.getSumNotAccepted();

					nbTotalValidated = item.getNbAccepted();
					nbTotalRejected = item.getNbNotAccepted();
					processingDate = item.getProcessingDate();

				}

				craDataInList.setNbTotalRejected(nbTotalRejected);
				craDataInList.setNbTotalValidated(nbTotalValidated);

				craDataInList.setSumRejected(sumRejected);
				craDataInList.setSumValidated(sumValidated);

				craDataInList.setCraData(craData);

				craDataInList.setProcessingDateOpeningDay(processingDate);
				craDataList.add(craDataInList);
			}

			craControlDisplay.setCraDataList(craDataList);

			craControlDisplayList.add(craControlDisplay);
		}

		return craControlDisplayList;

	}

	@GetMapping("/getUap040NotAccepted/{type}/{dateUap}")
	public List<UapNotAcceptedDisplay> getUap040NotAccepted(@PathVariable String type, @PathVariable String dateUap) {

		List<UapNotAcceptedDisplay> list = new ArrayList<UapNotAcceptedDisplay>();

		if (type.equals("040")) {

			List<UAP040FransaBankNotAccepted> uAP040FransaBankNotAccepted = uAP040FransaBankNotAcceptedRepository
					.findByFileIntegrationDate(dateUap);

			for (UAP040FransaBankNotAccepted el : uAP040FransaBankNotAccepted) {
				UapNotAcceptedDisplay data = new UapNotAcceptedDisplay();
				data.setIdentifPointRetrait(el.getIdentifPointRetrait());
				data.setMontantAComponser(el.getMontantAComponser());
				data.setNumAutorisation(el.getNumAutorisation());
				data.setNumtransaction(el.getNumtransaction());
				data.setPieceComptableBKM(el.getPieceComptableBKM());
				data.setReferenceArchivage(el.getReferenceArchivage());
//				String PAN = el.getCodeBin().trim() + el.getTypeCarteDebit().trim() +el.getNumSeq().trim() + el.getNumOrdre().trim()
//						+el.getCle().trim();
				data.setPan(el.getCodeBin().trim() + el.getTypeCarteDebit().trim() + el.getNumSeq().trim()
						+ el.getNumOrdre().trim() + el.getCle().trim());

				list.add(data);
			}
		} else if (type.equals("050")) {
			List<UAP050FransaBankNotAccepted> uAP050FransaBankNotAccepted = uAP050FransaBankNotAcceptedRepository
					.findByFileIntegrationDate(dateUap);

			for (UAP050FransaBankNotAccepted el : uAP050FransaBankNotAccepted) {
				UapNotAcceptedDisplay data = new UapNotAcceptedDisplay();
				data.setIdentifPointRetrait(el.getIdentifPointRetrait());
				data.setMontantAComponser(el.getMontantAComponser());
				data.setNumAutorisation(el.getNumAutorisation());
				data.setNumtransaction(el.getNumTransaction());
				data.setPieceComptableBKM(el.getPieceComptableBKM());
				data.setReferenceArchivage(el.getReferenceArchivage());
//				String PAN = el.getCodeBin().trim() + el.getTypeCarteDebit().trim() +el.getNumSeq().trim() + el.getNumOrdre().trim()
//						+el.getCle().trim();
				data.setPan(el.getCodeBin().trim() + el.getTypeCarte().trim() + el.getNumSeq().trim()
						+ el.getNumOrdre().trim() + el.getCle().trim());

				list.add(data);
			}
		} else if (type.equals("051")) {
			List<UAP051FransaBankNotAccepted> uAP051FransaBankNotAccepted = uAP051FransaBankNotAcceptedRepository
					.findByFileIntegrationDate(dateUap);

			for (UAP051FransaBankNotAccepted el : uAP051FransaBankNotAccepted) {
				UapNotAcceptedDisplay data = new UapNotAcceptedDisplay();
				data.setIdentifPointRetrait(el.getIdentifPointRetrait());
				data.setMontantAComponser(el.getMontantAComponser());
				data.setNumAutorisation(el.getNumAutorisation());
				data.setNumtransaction(el.getNumTransaction());
				data.setPieceComptableBKM(el.getPieceComptableBKM());
				data.setReferenceArchivage(el.getReferenceArchivage());
//				String PAN = el.getCodeBin().trim() + el.getTypeCarteDebit().trim() +el.getNumSeq().trim() + el.getNumOrdre().trim()
//						+el.getCle().trim();
				data.setPan(el.getCodeBin().trim() + el.getTypeCarte().trim() + el.getNumSeq().trim()
						+ el.getNumOrdre().trim() + el.getCle().trim());

				list.add(data);
			}
		}

		return list;
	}

	@GetMapping("/getMaxCraControlDate")
	public TpControlDates getMaxCraControlDate() {

		List<String> processigDate = craControlRespository.findMaxProcessingDate();
//		List<Date> fileDate=tpControlRespository.findMaxFileDate();
		TpControlDates dates = new TpControlDates();

		if (processigDate.size() > 0)
			dates.setFileDate(processigDate.get(0));

//		if (fileDate.size()>0)
//			dates.setFileDate(fileDate.get(0));

		return dates;
	}

	@GetMapping("/getMaxCraRejetControlDate")
	public TpControlDates getMaxCraRejetControlDate() {

		List<String> processigDate = craRejetControlRespository.findMaxProcessingDate();
//		List<Date> fileDate=tpControlRespository.findMaxFileDate();
		TpControlDates dates = new TpControlDates();

		if (processigDate.size() > 0)
			dates.setFileDate(processigDate.get(0));

//		if (fileDate.size()>0)
//			dates.setFileDate(fileDate.get(0));

		return dates;
	}

	@GetMapping("/getUap140NotAccepted/{type}/{dateUap}")
	public List<UapNotAcceptedDisplay> getUap140NotAccepted(@PathVariable String type, @PathVariable String dateUap) {

		List<UapNotAcceptedDisplay> list = new ArrayList<UapNotAcceptedDisplay>();

		if (type.equals("140")) {

			List<UAP040INNotAccepted> uAP040FransaBankNotAccepted = uAP040InNotAcceptedRepository
					.findByControlId(dateUap);

			for (UAP040INNotAccepted el : uAP040FransaBankNotAccepted) {
				UapNotAcceptedDisplay data = new UapNotAcceptedDisplay();
				data.setIdentifPointRetrait(el.getIdentifPointRetrait());
				data.setMontantAComponser(el.getMontantAComponser());
				data.setNumAutorisation(el.getNumAutorisation());
				data.setNumtransaction(el.getNumTransaction());
				data.setPieceComptableBKM(el.getPieceComptableBKM());
				data.setReferenceArchivage(el.getReferenceArchivage());
//				String PAN = el.getCodeBin().trim() + el.getTypeCarteDebit().trim() +el.getNumSeq().trim() + el.getNumOrdre().trim()
//						+el.getCle().trim();
				data.setPan(el.getCodeBin().trim() + el.getTypeCarteDebit().trim() + el.getNumSeq().trim()
						+ el.getNumOrdre().trim() + el.getCle().trim());

				list.add(data);
			}
		} else if (type.equals("150")) {
			List<UAP050INNotAccepted> uAP050FransaBankNotAccepted = uAP050InNotAcceptedRepository
					.findByControlId(dateUap);

			for (UAP050INNotAccepted el : uAP050FransaBankNotAccepted) {
				UapNotAcceptedDisplay data = new UapNotAcceptedDisplay();
				data.setIdentifPointRetrait(el.getIdentifPointRetrait());
				data.setMontantAComponser(el.getMontantAComponser());
				data.setNumAutorisation(el.getNumAutorisation());
				data.setNumtransaction(el.getNumTransaction());
				data.setPieceComptableBKM(el.getPieceComptableBKM());
				data.setReferenceArchivage(el.getReferenceArchivage());
//				String PAN = el.getCodeBin().trim() + el.getTypeCarteDebit().trim() +el.getNumSeq().trim() + el.getNumOrdre().trim()
//						+el.getCle().trim();
				data.setPan(el.getCodeBin().trim() + el.getTypeCarte().trim() + el.getNumSeq().trim()
						+ el.getNumOrdre().trim() + el.getCle().trim());

				list.add(data);
			}
		} else if (type.equals("151")) {
			List<UAP051INNotAccepted> uAP051FransaBankNotAccepted = uAP051InNotAcceptedRepository
					.findByControlId(dateUap);

			for (UAP051INNotAccepted el : uAP051FransaBankNotAccepted) {
				UapNotAcceptedDisplay data = new UapNotAcceptedDisplay();
				data.setIdentifPointRetrait(el.getIdentifPointRetrait());
				data.setMontantAComponser(el.getMontantAComponser());
				data.setNumAutorisation(el.getNumAutorisation());
				data.setNumtransaction(el.getNumTransaction());
				data.setPieceComptableBKM(el.getPieceComptableBKM());
				data.setReferenceArchivage(el.getReferenceArchivage());
//				String PAN = el.getCodeBin().trim() + el.getTypeCarteDebit().trim() +el.getNumSeq().trim() + el.getNumOrdre().trim()
//						+el.getCle().trim();
				data.setPan(el.getCodeBin().trim() + el.getTypeCarte().trim() + el.getNumSeq().trim()
						+ el.getNumOrdre().trim() + el.getCle().trim());

				list.add(data);
			}
		}

		return list;
	}
}
