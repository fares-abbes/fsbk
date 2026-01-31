package com.mss.backOffice.controller;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.beanutils.PropertyUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.google.common.base.Throwables;
import com.google.gson.Gson;
 import com.mss.backOffice.request.AddFileRequest;
import com.mss.backOffice.services.BkmService;
import com.mss.backOffice.services.FormulaInterpreterService;
import com.mss.backOffice.services.PropertyService;
import com.mss.unified.dto.UapDetailsControl;
import com.mss.unified.entities.BatchesFC;
import com.mss.unified.entities.BkmvtiFransaBank;
import com.mss.unified.entities.CraControl;
import com.mss.unified.entities.DayOperationFransaBank;
import com.mss.unified.entities.MvbkConf;
import com.mss.unified.entities.MvbkSettlement;
import com.mss.unified.entities.UAP050FransaBank;
import com.mss.unified.entities.UAP050FransaBankNotAccepted;
import com.mss.unified.entities.UAPFransaBank;
import com.mss.unified.entities.User;
import com.mss.unified.entities.OpeningDay;
import com.mss.unified.entities.UAP040FransaBankNotAccepted;
import com.mss.unified.repositories.BatchesFFCRepository;
import com.mss.unified.repositories.BkmvtiFransaBankRepository;
import com.mss.unified.repositories.CodeBankBCRepository;
import com.mss.unified.repositories.CraControlRepository;
import com.mss.unified.repositories.DayOperationFransaBankRepository;
import com.mss.unified.repositories.FileTRepository;
import com.mss.unified.repositories.MvbkConfigRepository;
import com.mss.unified.repositories.MvbkSettlementRepository;
import com.mss.unified.repositories.UAP050FransaBankRepository;
import com.mss.unified.repositories.UAPFransaBankRepository;
import com.mss.unified.repositories.UserRepository;
import com.mss.unified.repositories.OpeningDayRepository;
import com.mss.unified.repositories.UAP050FransaBankNotAcceptedRepository;
import org.apache.commons.beanutils.PropertyUtils;

@RestController
@RequestMapping("FransaBank050")
public  strictfp class FransaBank050Controller {
	@Autowired
	FormulaInterpreterService fIPService;
	@Autowired
	OpeningDayRepository openedDayRepo;
 
	@Autowired
	UAP050FransaBankRepository uAP050FransaBankRepository;
	@Autowired
	MvbkSettlementRepository mvbkSettlementRepository;
	@Autowired
	DayOperationFransaBankRepository dayOperationFransaBankRepository;
	@Autowired
	BkmvtiFransaBankRepository bkmvtiFransaBankRepository;
	@Autowired
	DayOperationFransaBankRepository dayRepo;
	@Autowired
	BatchesFFCRepository batchesFFCRepository;
	@Autowired
	FileTRepository fileSummaryTRepository;
	@Autowired
	CodeBankBCRepository codeBankBCRepository;
	@Autowired
	BatchesFFCRepository batchRepo;
	@Autowired
	public MvbkConfigRepository mvbkConfigR;
 
	@Autowired
	UserRepository userRepository;
	@Autowired
	PropertyService propertyService;
	@Autowired
	BkmService bkmService;
	@Autowired
	private CraControlRepository craControlRepository;
	@Autowired
	private UAP050FransaBankNotAcceptedRepository UAP050NotAcceptedRepository;
	private static final Logger logger = LoggerFactory.getLogger(FransaBank050Controller.class);

	public synchronized int getEveIndex() {
		return (++ExecutorThreadUAPINFileBC.eve);
	}

	public synchronized int getEveIndex1() {
		return (++ExecutorThreadUAPINFileBC.eve1);
	}

	public static String getLineNumber() {
		return Thread.currentThread().getStackTrace()[2].getClassName() + ": "
				+ String.valueOf(Thread.currentThread().getStackTrace()[2].getLineNumber());
	}


	@PutMapping("matchingUAP050")
	public String Matching(@RequestBody AddFileRequest addFileRequest) throws IOException {
		logger.info("*****begin matching cra 050*****");
		FileRequest.print("thread nb " + Thread.currentThread().getName(), FileRequest.getLineNumber());
		SecurityContextHolder.getContext().setAuthentication(OrshesterController.auth);

		String name = SecurityContextHolder.getContext().getAuthentication().getName();

		try {
			if (openedDayRepo.findByStatus050().isPresent()) {

				OpeningDay openDay = openedDayRepo.findByStatus050().get();
				openDay.setLotIncrementNb(1);
				openDay.setLotIncrementNbCra(1);
				 openedDayRepo.saveAndFlush(openDay);
				FileRequest.print(name, FileRequest.getLineNumber());
				BatchesFC batch = batchesFFCRepository.findByKey("CRAUAP50").get();
				batch.setBatchLastExcution(batch.getBatchEndDate());
				batch.setBatchStatus(0);
				batch.setBatchDate(new Date());
				batch.setError(null);
				batch.setErrorStackTrace(null);
				batchRepo.saveAndFlush(batch);

				int etat = validTransaction(addFileRequest, openDay.getFileIntegration());

				if (etat == 1) {
					List<UAP050FransaBank> ListUAPFransaBanksFiltred = uAP050FransaBankRepository
							.getListUAPByStatusTechnique(openDay.getFileIntegration());
					for (UAP050FransaBank uap : ListUAPFransaBanksFiltred) {

						if (uap.getTypeTransaction().equals("050") && uap.getTypePaiement().equals("01")) {
							ValidPaiementTpeWithoutDayOperation(uap);
						}

						if (uap.getTypeTransaction().equals("050") && uap.getTypePaiement().equals("04")
								|| uap.getTypeTransaction().equals("050") && uap.getTypePaiement().equals("03")) {
							if (uap.getTagPaiement().equals("1")) {
								ValidAchatInternetWithouDayOperation(uap);
							} else if (uap.getTagPaiement().equals("2")) {
								ValidAchatInternetAlgeriePosteWithouDayOperation(uap);
							}

						}

					}

//					moveFilesByStartingName(addFileRequest.getFilePath(), propertyService.getCompensationfilePath(),
//							addFileRequest.getFileName());

					List<UAP050FransaBank> data = uAP050FransaBankRepository
							.getListUAPByStatusTechniqueNV(openDay.getFileIntegration());
					if (data.size() > 0) {
						openedDayRepo.updateStatus050(openDay.getFileIntegration(), "doneSortCra");
						batchRepo.updateFinishBatch("CRAUAP50", 5, new Date());
						logger.info("****** end matching cra 050 with rejection ******");
					} else {
						OrshesterController.endedOk=true;

						openedDayRepo.updateStatus050(openDay.getFileIntegration(), "doneCra");
						batchRepo.updateFinishBatch("CRAUAP50", 1, new Date());
						logger.info("****** end matching cra 050 ******");
					}
				}

			}
			return "Done";
		} catch (Exception e) {
			String stackTrace = Throwables.getStackTraceAsString(e);
			logger.info("Exception is=>{}", stackTrace);
			if (stackTrace.length() > 4000)
				stackTrace = stackTrace.substring(0, 3999);
			String error = e.getMessage() == null ? e.toString() : e.getMessage();
			if (error.length() > 255)
				error = error.substring(0, 254);

			batchRepo.updateStatusAndErrorBatch("CRAUAP50", 2, error, new Date(), stackTrace);
			return "Error";
		}

	}

	public int validTransaction(AddFileRequest addFileRequest, String fileIntegration) throws IOException {
		logger.info("*****valid Transaction cra 050*****");
		String fileName = addFileRequest.getFilePath() + "/" + addFileRequest.getFileName() + ".CRA";

		FileRequest.print(fileName, FileRequest.getLineNumber());
		if (Files.exists(Paths.get(fileName))) {
			logger.info("file exist=>{}", fileName);
			logger.info("file integration=>{}", fileIntegration);
			List<UAP050FransaBank> UAPFransaBankS = uAP050FransaBankRepository.findByIntegrationDate(fileIntegration);
			List<UAP050FransaBank> uAPFransaBank = new ArrayList<>();
			List<String> cras = new ArrayList<>();
			Stream<String> stream = extractData(addFileRequest);

//			Stream<String> stream = Files.lines(Paths.get(fileName), StandardCharsets.ISO_8859_1);

			cras = stream
					.filter(element -> element.startsWith("035")
							&& element.substring(element.length() - 3, element.length()).equals("000"))
					.map(String::toUpperCase).collect(Collectors.toList());

			String dateReg = "";
			if (cras.size() != 0) {
				dateReg = cras.get(0).substring(38, 46);

				batchesFFCRepository.updateDateReg(dateReg, "GNR02");
				batchesFFCRepository.updateDateReg(dateReg, "CRAUAP50Reglement");

			}

			Map<String, UAP050FransaBank> fileUap = new HashMap<>();
			UAPFransaBankS.forEach(u -> {

				int lenNumAutorisationUap = u.getNumAutorisation().length();
				String numAutorisationUap = u.getNumAutorisation().substring(lenNumAutorisationUap - 6,
						lenNumAutorisationUap);
				String PAN = u.getCodeBin().trim() + u.getTypeCarte().trim() + u.getNumSeq().trim()
						+ u.getNumOrdre().trim() + u.getCle().trim();

				fileUap.put(numAutorisationUap + PAN+u.getNumTransaction(), u);

			});
			cras.forEach(e -> {
				String dateReglement = e.substring(38, 46);
				String crasTransaction = e.substring(46, e.length());
				String numAutorisationCra = crasTransaction.substring(305, 311);
				String numTransaction = crasTransaction.substring(257, 269);

				
				String PanCra = crasTransaction.substring(202, 218);

				if (fileUap.containsKey(numAutorisationCra + PanCra+numTransaction)) {
//					logger.info("transaction existe 050=>{}", numAutorisationCra + PanCra);
					UAP050FransaBank uap = fileUap.get(numAutorisationCra + PanCra+numTransaction);
					String dateTransactionCra = crasTransaction.substring(269, 277);
					String MontantRetrait = crasTransaction.substring(218, 233);
					String MontantComission = crasTransaction.substring(250, 257);
					String MontantCompenser = crasTransaction.substring(234, 249);

					if ((uap.getDateTransaction().trim()).equals(dateTransactionCra)

							&& (uap.getMontantRetrait().trim()).equals(MontantRetrait)

							&& (uap.getMontantCommission().trim()).equals(MontantComission)
							&& (uap.getMontantAComponser().trim()).equals(MontantCompenser)) {
//						logger.info("transaction matchee 050 =>{}", numAutorisationCra + PanCra);
						uap.setUapRio(e.substring(0, 38));
						uap.setStatusTechnique("000");
						uap.setDateReglement(dateReglement);
						uAPFransaBank.add(uap);
					}
				}

			});

			uAP050FransaBankRepository.saveAll(uAPFransaBank);
			/////////// save cra LOT control /////////////
			saveCraControl(fileIntegration);
		    ///////////////////////////////////////////
			logger.info("****end validTransaction 050****");
			return 1;
		}

		else {
			batchRepo.updateStatusAndErrorBatch("CRAUAP50", 4, "Missing file!", new Date(), "");
			return 0;
		}
	}
	
	
	private void saveCraControl(String fileIntegration) {
		List<CraControl> craControlList=craControlRepository.findByProcessingDateAndLotType(fileIntegration,"050");
		
		if (craControlList.size()>0) {
			
			List<UapDetailsControl>  acceptedUp= uAP050FransaBankRepository.getListUAP50AcceptedForControl(fileIntegration);
			long sumAcceptedUp=0;
			for (UapDetailsControl el : acceptedUp) {
	    		sumAcceptedUp+=
	    				Long.valueOf(el.getMontantAComponser().replace(".", "")) ;
	    		
	    		
			}
			
			long sumNotAccepted=0;
			List<UAP050FransaBank>  notAccepted= uAP050FransaBankRepository.getListUAP50NotAcceptedForControl(fileIntegration);
			List<UAP050FransaBankNotAccepted> notAcceptedList= new ArrayList<UAP050FransaBankNotAccepted>();
			for (UAP050FransaBank el : notAccepted) {
				sumNotAccepted+=
	    				Long.valueOf(el.getMontantAComponser().replace(".", "")) ;
				
				
				UAP050FransaBankNotAccepted notAcceptedUap50 = new UAP050FransaBankNotAccepted();
				try {
					PropertyUtils.copyProperties(notAcceptedUap50, el);
				} catch (Exception ex) {
					logger.info("Exception");
					logger.info( Throwables.getStackTraceAsString(ex));
				}
				notAcceptedList.add(notAcceptedUap50);
	    		
			}

			for(CraControl craControl :craControlList) {
				logger.info("cra control is found");
				
				craControl.setSumAccepted(sumAcceptedUp);
				craControl.setNbAccepted(acceptedUp.size());
				
				
				craControl.setSumNotAccepted(sumNotAccepted);
				craControl.setNbNotAccepted(notAccepted.size());
	
				
				craControlRepository.save(craControl);
			}
			UAP050NotAcceptedRepository.saveAll(notAcceptedList);
		}
	}


	public Stream<String> extractData(AddFileRequest addFileRequest) throws IOException {
		logger.info("extractData");

		Stream<String> fileRows = null;
		List<String> filesNames = listAndFilterFiles(addFileRequest.getFilePath(), "050.DZD.CRA");
		FileRequest.print(filesNames.toString(), FileRequest.getLineNumber());
		FileRequest.print(addFileRequest.getFilePath(), FileRequest.getLineNumber());

		fileRows = readFiles(filesNames, addFileRequest.getFilePath());
		List<String> rows = fileRows.map(String::toUpperCase).collect(Collectors.toList());
		logger.info("extractData size = " + rows.size());
		for (String file : filesNames) {
			moveFilesByStartingName(addFileRequest.getFilePath(), propertyService.getCompensationfilePath(), file);
		}
		return rows.stream();
	}

	public List<String> listAndFilterFiles(String folderPath, String filter) {
		List<String> filteredFiles = new ArrayList<>();

		// Create a File object for the specified folder path
		File folder = new File(folderPath);

		// Check if the folder exists and is a directory
		if (folder.exists() && folder.isDirectory()) {
			// List all files in the folder
			File[] files = folder.listFiles();

			if (files != null) {
				for (File file : files) {
					// Check if the file matches the filter criteria
					if (file.isFile() && file.getName().contains(filter)) {
						// Add the file name to the list of filtered files
						filteredFiles.add(file.getName());
					}
				}
			}
		}

		return filteredFiles;
	}

	public Stream<String> readFiles(List<String> filteredFiles, String folderPath) {
		return filteredFiles.stream() // Stream the filtered file names
				.map(fileName -> Paths.get(folderPath, fileName)) // Map to file paths
				.flatMap(filePath -> {
					try {
						return Files.lines(filePath, StandardCharsets.ISO_8859_1); // Open each file as a stream
					} catch (IOException e) {
						throw new UncheckedIOException(e); // Wrap IOException as UncheckedIOException
					}
				});
	}

	public void ValidPaiementTpeWithoutDayOperation(UAP050FransaBank uap) {
//		logger.info("ValidPaiementTpe");
		List<MvbkSettlement> allMvbkSettelemnts = mvbkSettlementRepository.findByIdentificationCRAPaiementTpe();
		List<MvbkConf> allMvbkSettelemntsC = mvbkConfigR.findByCategorie(23);

		List<BkmvtiFransaBank> BkmvtiFransaBanks = new ArrayList<BkmvtiFransaBank>();

		int indexPieceComptable = getEveIndex1();
		for (MvbkConf mvk : allMvbkSettelemntsC) {
			int index2 = getEveIndex();
			BkmvtiFransaBanks = TestSigneWithoutDayOperation(uap, 8, mvk, BkmvtiFransaBanks, indexPieceComptable,
					index2, mvk.getCodeSettlement());

		}

		if (BkmvtiFransaBanks != null && !BkmvtiFransaBanks.isEmpty()) {
		    // Filter out null elements from the list
		    List<BkmvtiFransaBank> nonNullBkmvtiFransaBanks = BkmvtiFransaBanks.stream()
		            .filter(Objects::nonNull)
		            .collect(Collectors.toList());

		    if (!nonNullBkmvtiFransaBanks.isEmpty()) {
		        bkmvtiFransaBankRepository.saveAll(nonNullBkmvtiFransaBanks);
		    }
		    
		}//		logger.info("end ValidPaiementTpe");
	}

	public void ValidAchatInternetAlgeriePosteWithouDayOperation(UAP050FransaBank uap) {
//		logger.info("ValidAchatInternetAlgeriePoste");
		List<MvbkSettlement> allMvbkSettelemnts = mvbkSettlementRepository
				.findByIdentificationOffUsAcqAchatInternetCRA();
		List<MvbkConf> allMvbkSettelemntsC = mvbkConfigR.findByCategorie(1054);

		List<BkmvtiFransaBank> BkmvtiFransaBanks = new ArrayList<BkmvtiFransaBank>();
		int indexPieceComptable = getEveIndex1();
		for (MvbkConf mvk : allMvbkSettelemntsC) {
			int index2 = getEveIndex();
			BkmvtiFransaBanks = TestSigneWithoutDayOperation(uap, 21, mvk, BkmvtiFransaBanks, indexPieceComptable,
					index2, mvk.getCodeSettlement());

		}

		if (BkmvtiFransaBanks != null && !BkmvtiFransaBanks.isEmpty()) {
		    // Filter out null elements from the list
		    List<BkmvtiFransaBank> nonNullBkmvtiFransaBanks = BkmvtiFransaBanks.stream()
		            .filter(Objects::nonNull)
		            .collect(Collectors.toList());

		    if (!nonNullBkmvtiFransaBanks.isEmpty()) {
		        bkmvtiFransaBankRepository.saveAll(nonNullBkmvtiFransaBanks);
		    }
		    
		}
//		logger.info("end ValidAchatInternetAlgeriePoste");
	}

	public void ValidAchatInternetWithouDayOperation(UAP050FransaBank uap) {
//		logger.info("ValidAchatInternetCRA");
		List<MvbkSettlement> allMvbkSettelemnts = mvbkSettlementRepository
				.findByIdentificationOffUsAcqAchatInternetCRA();
		List<MvbkConf> allMvbkSettelemntsC = mvbkConfigR.findByCategorie(54);

		List<BkmvtiFransaBank> BkmvtiFransaBanks = new ArrayList<BkmvtiFransaBank>();

		int indexPieceComptable = getEveIndex1();
		for (MvbkConf mvk : allMvbkSettelemntsC) {
			int index2 = getEveIndex();
			BkmvtiFransaBanks = TestSigneWithoutDayOperation(uap, 19, mvk, BkmvtiFransaBanks, indexPieceComptable,
					index2, mvk.getCodeSettlement());

		}

		if (BkmvtiFransaBanks != null && !BkmvtiFransaBanks.isEmpty()) {
		    // Filter out null elements from the list
		    List<BkmvtiFransaBank> nonNullBkmvtiFransaBanks = BkmvtiFransaBanks.stream()
		            .filter(Objects::nonNull)
		            .collect(Collectors.toList());

		    if (!nonNullBkmvtiFransaBanks.isEmpty()) {
		        bkmvtiFransaBankRepository.saveAll(nonNullBkmvtiFransaBanks);
		    }
		    
		}
//		logger.info("end ValidAchatInternetCRA");

	}

	public List<BkmvtiFransaBank> TestSigneWithoutDayOperation(UAP050FransaBank uap, int methode, MvbkConf mvk,
			List<BkmvtiFransaBank> BkmvtiFransaBanks, int indexPieceComptable, int index2, String codeOperation) {

		BkmvtiFransaBank bkmvtiFransaBank = new BkmvtiFransaBank();

		if (AccountSigne(mvk.getAccount(), mvk.getSigne()) == 1 || AccountSigne(mvk.getAccount(), mvk.getSigne()) == 2
				|| AccountSigne(mvk.getAccount(), mvk.getSigne()) == 3) {
			bkmvtiFransaBank = TestAccountAndSigneWithoutSayOperation(uap, methode, index2, indexPieceComptable, 1,
					mvk.getAccount(), mvk.getSigne(), mvk, mvk.getAccount());
			BkmvtiFransaBanks.add(bkmvtiFransaBank);

		}

		return BkmvtiFransaBanks;
	}

	public BkmvtiFransaBank TestAccountAndSigneWithoutSayOperation(UAP050FransaBank uap, int methode, int index2,
			int indexPieceComptable, int test, String account, String signe, MvbkConf mvk, String cle) {
		BkmvtiFransaBank bkmvtiFransaBank = new BkmvtiFransaBank();
		TestAccountLengthWithoutDayOperation(uap, account.length(), account, mvk, bkmvtiFransaBank, cle);
		bkmvtiFransaBank.setCodeDevice("208");
		String name = SecurityContextHolder.getContext().getAuthentication().getName();
		Optional<User> user = userRepository.findByUserNameOrUserEmail(name, name);
		if (!user.isPresent()) {
			throw new RuntimeException("Error saving status !");
		}
		String nameUser = user.get().getUserName();
		if (nameUser.length() > 10) {

			nameUser = nameUser.substring(0, 10);
		}
		bkmvtiFransaBank.setCodeUtilisateur(nameUser + getSpace(10 - user.get().getUserName().length()));
		bkmvtiFransaBank.setCodeService("0000");
		bkmvtiFransaBank.setSens(mvk.getSigne());
		bkmvtiFransaBank.setExonerationcommission("O");
		int lengthNumPiece = uap.getNumAutorisation().length();
		bkmvtiFransaBank.setNumPiece(uap.getNumAutorisation().substring(lengthNumPiece - 11, lengthNumPiece));
		bkmvtiFransaBank.setTauxChange("1" + getSpace(6));
		bkmvtiFransaBank.setCalculmouvementInteragence("N");
		bkmvtiFransaBank.setMouvementAgence("N");
		int lengthNumAuth = uap.getNumAutorisation().length();
		int lengthRefDossier = (uap.getNumTransaction() + uap.getDateTransaction()
				+ uap.getNumAutorisation().substring(lengthNumAuth - 6, lengthNumAuth)).length();

		bkmvtiFransaBank.setRefDossier(uap.getNumTransaction() + uap.getDateTransaction()
				+ uap.getNumAutorisation().substring(lengthNumAuth - 6, lengthNumAuth)
				+ getSpace(50 - lengthRefDossier));

		TestCodeAgenceWithoutDayOperation(uap, mvk.getCodeAgence(), mvk, bkmvtiFransaBank);

		getTransactionDate(uap.getDateTransaction(), bkmvtiFransaBank);

		int lengthReferanceLettrage = uap.getNumAutorisation().length();

		bkmvtiFransaBank.setReferanceLettrage(uap.getDateTransaction().substring(6, uap.getDateTransaction().length())
				+ uap.getNumAutorisation().substring(lengthReferanceLettrage - 6, lengthReferanceLettrage));

 		HashMap<String, BigDecimal> data = new HashMap<String, BigDecimal>();
		BigDecimal mntRet= new BigDecimal( uap.getMontantTranasction());
		BigDecimal commConf= new BigDecimal( uap.getMontantCommissionConf());
  
		data.put("MntTrans", mntRet);
		data.put("commConf", commConf);
        BigDecimal roundedValue = fIPService.evaluateWithElementswithBigDecimal(mvk.getCodeSettlement(), data).divide(new BigDecimal(100));

        DecimalFormat decimalFormat = new DecimalFormat("000000000000000000.00");
        String formattedValue = decimalFormat.format(roundedValue);

        // Replace the decimal point with a comma
        formattedValue = formattedValue.replace('.', ',');

        // Print the formatted value
        bkmvtiFransaBank.setMontant(formattedValue);


 
		bkmvtiFransaBank.setCodeDeviceOrigine("208");
		bkmvtiFransaBank.setIdentification(mvk.getIdentification());
		bkmvtiFransaBank.setNumRefTransactions(uap.getNumTransaction());

		setSameDataWithoutDayOperation(uap, methode, bkmvtiFransaBank, index2, indexPieceComptable, mvk);
		try {

			if (Double.valueOf(bkmvtiFransaBank.getMontant().replace(",", ".")) <= 0) {
				throw new Exception("amount lower than expected");
			}
		} catch (Exception e) {
			return null;
		}
		return bkmvtiFransaBank;

	}

	public BkmvtiFransaBank TestAccountLengthWithoutDayOperation(UAP050FransaBank uap, int lengAccount, String Account,
			MvbkConf mvk, BkmvtiFransaBank bkmvtiFransaBank, String cle) {

		if (lengAccount > 18) {

			String credit = Account.substring(8, 18);
			String chapitreCompta = Account.substring(8, 14);
			bkmvtiFransaBank.setChapitreComptable(chapitreCompta + getZero(6 - chapitreCompta.length()));
			bkmvtiFransaBank.setNumCompte(credit);
			bkmvtiFransaBank.setCleControleCompte(getSpace(2));
			String codeDes = Account.substring(3, 8);
			bkmvtiFransaBank.setAgenceDestinatrice(codeDes);
			bkmvtiFransaBank.setAgenceEmettrice(codeDes);
			bkmvtiFransaBank.setCodeAgenceSaisie(codeDes);
			String codeId = Account.substring(3, 8);
			bkmvtiFransaBank.setCodeID("S" + codeId);
		}

		else if (lengAccount >= 6 && lengAccount < 10) {
			bkmvtiFransaBank.setNumCompte(Account + getZero(10 - (Account.length())));
			bkmvtiFransaBank.setChapitreComptable(Account.substring(0, 6) + getZero(6 - Account.length()));

			bkmvtiFransaBank.setCleControleCompte(getSpace(2));
			bkmvtiFransaBank.setAgenceDestinatrice(uap.getCodeAgence());
			bkmvtiFransaBank.setAgenceEmettrice(uap.getCodeAgence());
			bkmvtiFransaBank.setCodeAgenceSaisie(uap.getCodeAgence());
			bkmvtiFransaBank.setCodeID("S" + uap.getCodeAgence());
		}

		else if (lengAccount < 6) {
			bkmvtiFransaBank.setNumCompte(Account + getZero(10 - (Account.length())));
			bkmvtiFransaBank.setChapitreComptable(Account + getZero(6 - Account.length()));
			bkmvtiFransaBank.setCleControleCompte(getSpace(2));
			bkmvtiFransaBank.setAgenceDestinatrice(uap.getCodeAgence());
			bkmvtiFransaBank.setAgenceEmettrice(uap.getCodeAgence());
			bkmvtiFransaBank.setCodeAgenceSaisie(uap.getCodeAgence());
			bkmvtiFransaBank.setCodeID("S" + uap.getCodeAgence());
		}

		else if (lengAccount == 10) {
			bkmvtiFransaBank.setNumCompte(Account);
			bkmvtiFransaBank.setChapitreComptable(Account.substring(0, 6));
			bkmvtiFransaBank.setCleControleCompte(getSpace(2));
			bkmvtiFransaBank.setAgenceDestinatrice(uap.getCodeAgence());
			bkmvtiFransaBank.setAgenceEmettrice(uap.getCodeAgence());
			bkmvtiFransaBank.setCodeAgenceSaisie(uap.getCodeAgence());
			bkmvtiFransaBank.setCodeID("S" + uap.getCodeAgence());
		}

		else {
			bkmvtiFransaBank.setNumCompte(Account.substring(0, 10));
			bkmvtiFransaBank.setChapitreComptable(Account.substring(0, 6));
			bkmvtiFransaBank.setCleControleCompte(getSpace(2));
			bkmvtiFransaBank.setAgenceEmettrice(uap.getCodeAgence());
			bkmvtiFransaBank.setCodeAgenceSaisie(uap.getCodeAgence());
			bkmvtiFransaBank.setCodeID("S" + uap.getCodeAgence());
		}

		return bkmvtiFransaBank;
	}

	public BkmvtiFransaBank setSameDataWithoutDayOperation(UAP050FransaBank uap, int methode,
			BkmvtiFransaBank bkmvtiFransaBank, int index2, int indexPieceComptable, MvbkConf mvb) {
		String lib = "";
		bkmvtiFransaBank.setCodeOperation(mvb.getCodeOperation());
		lib = mvb.getLibelle_operation();
		bkmvtiFransaBank.setLibelle(lib + getSpace(40 - lib.length()));
		if (mvb.getLibGenerique()!= null &&  mvb.getLibGenerique().trim()!="") {
			String libgenerique =mvb.getLibGenerique();
			libgenerique=libgenerique.replaceAll("dateTransation", uap.getDateTransaction());
			String aut=uap.getNumAutorisation();
			int lengthAuth=aut.length();
			libgenerique=libgenerique.replaceAll("numAutorisation", aut.substring(lengthAuth-6));
			bkmvtiFransaBank.setLibelle(libgenerique + getSpace(40 - lib.length()));
		}
		switch (methode) {

		case 8:

			bkmvtiFransaBank.setPieceComptable(uap.getPieceComptableBKM());

			bkmvtiFransaBank.setNumEvenement(uap.getPieceComptableBKM()
					.substring(uap.getPieceComptableBKM().length() - 6, uap.getPieceComptableBKM().length()));
			break;

		case 19:

			bkmvtiFransaBank.setPieceComptable(uap.getPieceComptableBKM());

			bkmvtiFransaBank.setNumEvenement(uap.getPieceComptableBKM()
					.substring(uap.getPieceComptableBKM().length() - 6, uap.getPieceComptableBKM().length()));
			break;

		case 21:

			bkmvtiFransaBank.setPieceComptable(uap.getPieceComptableBKM());

			bkmvtiFransaBank.setNumEvenement(uap.getPieceComptableBKM()
					.substring(uap.getPieceComptableBKM().length() - 6, uap.getPieceComptableBKM().length()));
			break;

		default:
			bkmvtiFransaBank.setPieceComptable(uap.getPieceComptableBKM());

			bkmvtiFransaBank.setNumEvenement(uap.getPieceComptableBKM()
					.substring(uap.getPieceComptableBKM().length() - 6, uap.getPieceComptableBKM().length()));
		}
		return bkmvtiFransaBank;
	}

	public BkmvtiFransaBank TestCodeAgenceWithoutDayOperation(UAP050FransaBank uap, String codeAgence, MvbkConf mvk,
			BkmvtiFransaBank bkmvtiFransaBank) {
		if (mvk.getCodeAgence() != null) {
			if (mvk.getCodeAgence().equals("00002")) {
				bkmvtiFransaBank.setAgence(mvk.getCodeAgence());
			}else {
				bkmvtiFransaBank.setAgence(uap.getCodeAgence());
			}
		}

		else {
			bkmvtiFransaBank.setAgence(uap.getCodeAgence());
		}
		return bkmvtiFransaBank;
	}

	public int AccountSigne(String account, String signe) {
		int test = 0;
		if ((account.equals("PORTEUR") || (account.equals("COMMERCANT")) || (account.equals("ATM")))
				&& signe.equals("C")) {
			test = 1;
		} else if ((account.equals("PORTEUR") || (account.equals("COMMERCANT")) || (account.equals("ATM")))
				&& signe.equals("D")) {
			test = 2;
		} else if ((!account.equals("PORTEUR") && !account.equals("COMMERCANT") && !account.equals("ATM"))
				&& (signe.equals("C") || signe.equals("D"))) {
			test = 3;
		}

		return test;
	}

	

	
	
	public BkmvtiFransaBank getTransactionDate(String TransactionDate, BkmvtiFransaBank bkmvtiFransaBank) {
		String year = TransactionDate.substring(0, 4);
		String month = TransactionDate.substring(4, 6);
		String dayy = TransactionDate.substring(6);
		LocalDate date = LocalDate.now();
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
		bkmvtiFransaBank.setDateComptable(date.format(formatter));

		bkmvtiFransaBank.setDateValeur(dayy + "/" + month + "/" + year);
		return bkmvtiFransaBank;

	}



	public String getZero(int count)

	{

		String zero = "";

		for (int i = 0; i < count; i++)

			zero += "0";

		return zero;

	}

	public String getSpace(int count)

	{

		String Space = "";

		for (int i = 0; i < count; i++)

			Space += " ";

		return Space;

	}



	public static void moveFilesByStartingName(String sourceDirectory, String destinationDirectory, String startingName)
			throws IOException {
		File sourceDir = new File(sourceDirectory);
		File[] files = sourceDir.listFiles((dir, name) -> name.startsWith(startingName));

		if (files != null) {
			LocalDateTime timer = LocalDateTime.now();
	        String movingDate =  ""+timer.getYear()+timer.getMonth()+timer.getDayOfMonth()+"_"+timer.getHour()+timer.getMinute()+timer.getSecond();


			for (File file : files) {
				String originalFileName = file.getName();
				String newFileName = originalFileName + movingDate;

				Path sourcePath = file.toPath();
				Path destinationPath = new File(destinationDirectory, newFileName).toPath();

				Files.move(sourcePath, destinationPath, StandardCopyOption.REPLACE_EXISTING);
			}
		}
	}

}
