package com.mss.backOffice.controller;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
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
import com.mss.unified.entities.UAP051FransaBank;
import com.mss.unified.entities.UAP051FransaBankNotAccepted;
import com.mss.unified.entities.UAPFransaBank;
import com.mss.unified.entities.User;
import com.mss.unified.entities.OpeningDay;
import com.mss.unified.repositories.BatchesFFCRepository;
import com.mss.unified.repositories.BkmvtiFransaBankRepository;
import com.mss.unified.repositories.CodeBankBCRepository;
import com.mss.unified.repositories.CraControlRepository;
import com.mss.unified.repositories.DayOperationFransaBankRepository;
import com.mss.unified.repositories.FileTRepository;
import com.mss.unified.repositories.MvbkConfigRepository;
import com.mss.unified.repositories.MvbkSettlementRepository;
import com.mss.unified.repositories.UAP051FransaBankRepository;
import com.mss.unified.repositories.UAPFransaBankRepository;
import com.mss.unified.repositories.UserRepository;
import com.mss.unified.repositories.OpeningDayRepository;
import com.mss.unified.repositories.UAP051FransaBankNotAcceptedRepository;

@RestController
@RequestMapping("FransaBank051")
public  strictfp class FransaBank051Controller {
	@Autowired
	FormulaInterpreterService fIPService;
	@Autowired
	OpeningDayRepository openedDayRepo;
	@Autowired
	DayOperationFransaBankRepository dayRepo;
	@Autowired
	UAPFransaBankRepository uAPFransaBankRepository;
	@Autowired
	UAP051FransaBankRepository uAP051FransaBankRepository;
	@Autowired
	MvbkSettlementRepository mvbkSettlementRepository;
	@Autowired
	DayOperationFransaBankRepository dayOperationFransaBankRepository;
	@Autowired
	BkmvtiFransaBankRepository bkmvtiFransaBankRepository;

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
	private CraControlRepository craControlRepository;
	@Autowired
	private UAP051FransaBankNotAcceptedRepository UAP051NotAcceptedRepository;
	
	private static final Logger logger = LoggerFactory.getLogger(FransaBank051Controller.class);

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


	@PutMapping("matchingUAP051")
	public String Matching(@RequestBody AddFileRequest addFileRequest) throws IOException {
		logger.info("matchingUAP051");
		FileRequest.print("thread nb " + Thread.currentThread().getName(), FileRequest.getLineNumber());
		SecurityContextHolder.getContext().setAuthentication(OrshesterController.auth);

		String name = SecurityContextHolder.getContext().getAuthentication().getName();
		FileRequest.print(name, FileRequest.getLineNumber());
		BatchesFC batch = batchesFFCRepository.findByKey("CRAUAP51").get();

		try {
			if (openedDayRepo.findByStatus051().isPresent()) {
				OpeningDay openDay = openedDayRepo.findByStatus051().get();
				openDay.setLotIncrementNb(1);
				openDay.setLotIncrementNbCra(1);
				 openedDayRepo.saveAndFlush(openDay);
				batch.setBatchLastExcution(batch.getBatchEndDate());
				batch.setBatchStatus(0);
				batch.setBatchDate(new Date());
				batch.setError(null);
				batch.setErrorStackTrace(null);
				batchRepo.saveAndFlush(batch);

				int etat = validTransaction(addFileRequest, openDay.getFileIntegration());

				if (etat == 1) {

					List<UAP051FransaBank> ListUAPFransaBanksFiltred = uAP051FransaBankRepository
							.getListUAPByStatusTechnique(openDay.getFileIntegration());
					for (UAP051FransaBank uap : ListUAPFransaBanksFiltred) {

						if (uap.getTypeTransaction().equals("051") && uap.getTypePaiement().equals("01")
								|| uap.getTypeTransaction().equals("051") && uap.getTypePaiement().equals("05")) {
							ValidRembourssementTpeWithoutDayOperation(uap);
						} else if ((uap.getTypeTransaction().equals("055") && uap.getTypePaiement().equals("04"))
								|| (uap.getTypeTransaction().equals("056") && uap.getTypePaiement().equals("03"))) {
							ValidRembourssementInternetWithoutDayOperation(uap);
						}

					}
					moveFilesByStartingName(addFileRequest.getFilePath(), propertyService.getCompensationfilePath(),
							addFileRequest.getFileName());

					List<UAP051FransaBank> data = uAP051FransaBankRepository
							.getListUAPByStatusTechniqueNV(openDay.getFileIntegration());
					if (data.size() > 0) {
						openedDayRepo.updateStatus051(openDay.getFileIntegration(), "doneSortCra");
						batchRepo.updateFinishBatch("CRAUAP51", 5, new Date());
						logger.info("end matching cra 051 with rejection");
					} else {
						OrshesterController.endedOk=true;

						openedDayRepo.updateStatus051(openDay.getFileIntegration(), "doneCra");
						batchRepo.updateFinishBatch("CRAUAP51", 1, new Date());
						logger.info("end matching cra 051");
					}
				}
			}
			return "Done";
		}

		catch (Exception e) {
			String stackTrace = Throwables.getStackTraceAsString(e);
			logger.info("Exception is=>{}", stackTrace);
			if (stackTrace.length() > 4000)
				stackTrace = stackTrace.substring(0, 3999);
			String error = e.getMessage() == null ? e.toString() : e.getMessage();
			if (error.length() > 255)
				error = error.substring(0, 254);

			batchRepo.updateStatusAndErrorBatch("CRAUAP51", 2, error, new Date(), stackTrace);

		}
		return "error";
	}

	public int validTransaction(AddFileRequest addFileRequest, String integrationFile) throws IOException {
		logger.info("validTransaction");
		String fileName = addFileRequest.getFilePath() + "/" + addFileRequest.getFileName() + ".CRA";
		if (Files.exists(Paths.get(fileName))) {
			// ****update batch****//

			// *****list for adding***//
			List<UAP051FransaBank> UAPFransaBankS = uAP051FransaBankRepository.findByIntegrationFile(integrationFile);
			List<UAP051FransaBank> uAPFransaBank = new ArrayList<>();
			List<String> cras = new ArrayList<>();

			// ****summary to matching with cra***//
			Stream<String> stream = Files.lines(Paths.get(fileName), StandardCharsets.ISO_8859_1);

			cras = stream.filter(element -> element.startsWith("035")).map(String::toUpperCase)
					.collect(Collectors.toList());

			String dateReg = "";
			if (cras.size() != 0) {
				dateReg = cras.get(0).substring(38, 46);


				batchesFFCRepository.updateDateReg(dateReg, "GNR03");
				batchesFFCRepository.updateDateReg(dateReg, "CRAUAP51Reglement");

			}

			///////// code ////////////////

			cras.forEach(e -> {

				for (UAP051FransaBank u : UAPFransaBankS) {
					String dateReglement = e.substring(38, 46);
					String crasTransaction = e.substring(46, e.length());
					String status = crasTransaction.substring(crasTransaction.length() - 3, crasTransaction.length());

					if (status.equals("000")) {
						String dateTransactionCra = crasTransaction.substring(269, 277);
						String numAutorisationCra = crasTransaction.substring(305, 311);
						String ReferenceArchivageCra = crasTransaction.substring(5, 23);
						String MontantRetrait = crasTransaction.substring(218, 233);
						String MontantCompenser = crasTransaction.substring(234, 249);
						String MontantComission = crasTransaction.substring(250, 257);
						String PanCra = crasTransaction.substring(202, 218);
						String numTransaction = crasTransaction.substring(257, 269);

						int lenNumAutorisationUap = u.getNumAutorisation().length();
						String numAutorisationUap = u.getNumAutorisation().substring(lenNumAutorisationUap - 6,
								lenNumAutorisationUap);
						String PAN = u.getCodeBin().trim() + u.getTypeCarte().trim() + u.getNumSeq().trim()
								+ u.getNumOrdre().trim() + u.getCle().trim();

						if ((u.getDateTransaction().trim()).equals(dateTransactionCra)
								&& (numAutorisationUap.trim()).equals(numAutorisationCra)
								&& (u.getMontantRetrait().trim()).equals(MontantRetrait) && (PAN.equals(PanCra))
								&& (u.getMontantAComponser().trim()).equals(MontantCompenser)
								&& (u.getMontantCommission().trim()).equals(MontantComission) && u.getNumTransaction().trim().equals(numTransaction)) {
							logger.info("matching=>{}");
							u.setUapRio(e.substring(0, 38));
							u.setStatusTechnique("000");
							u.setDateReglement(dateReglement);
							uAPFransaBank.add(u);
						}
					}
				}

			});

			uAP051FransaBankRepository.saveAll(uAPFransaBank);
			/////////// save cra LOT control /////////////
			saveCraControl(integrationFile);
		    ///////////////////////////////////////////
			return 1;

		} else {
			batchRepo.updateStatusAndErrorBatch("CRAUAP51", 4, "Missing file!", new Date(), "");
			return 0;
		}
	}
	
	private void saveCraControl(String fileIntegration) {
		List<CraControl> craControlList=craControlRepository.findByProcessingDateAndLotType(fileIntegration,"051");
		
		if (craControlList.size()>0) {
			List<UapDetailsControl>  acceptedUp= uAP051FransaBankRepository.getListUAP51AcceptedForControl(fileIntegration);
			long sumAcceptedUp=0;
			for (UapDetailsControl el : acceptedUp) {
	    		sumAcceptedUp+=
	    				Long.valueOf(el.getMontantAComponser().replace(".", "")) ;
	    		
	    		
			}
			long sumNotAccepted=0;
			List<UAP051FransaBank>  notAccepted= uAP051FransaBankRepository.getListUAP51NotAcceptedForControl(fileIntegration);
			List<UAP051FransaBankNotAccepted> notAcceptedList= new ArrayList<UAP051FransaBankNotAccepted>();
			for (UAP051FransaBank el : notAccepted) {
				sumNotAccepted+=
	    				Long.valueOf(el.getMontantAComponser().replace(".", "")) ;
				
				
				UAP051FransaBankNotAccepted notAcceptedUap51 = new UAP051FransaBankNotAccepted();
				try {
					PropertyUtils.copyProperties(notAcceptedUap51, el);
				} catch (Exception ex) {
					logger.info("Exception");
					logger.info( Throwables.getStackTraceAsString(ex));
				}
				notAcceptedList.add(notAcceptedUap51);
	    		
			}
			
			for(CraControl craControl :craControlList) {
				logger.info("cra control is found");
			
				craControl.setSumAccepted(sumAcceptedUp);
				craControl.setNbAccepted(acceptedUp.size());
				
				
				craControl.setSumNotAccepted(sumNotAccepted);
				craControl.setNbNotAccepted(notAccepted.size());

				
				craControlRepository.save(craControl);
			}
			UAP051NotAcceptedRepository.saveAll(notAcceptedList);
			
		}
		
		
		

		
	}

	public void ValidRembourssementTpeWithoutDayOperation(UAP051FransaBank uap) {
//		logger.info("ValidRembourssementTpe");
		List<MvbkSettlement> allMvbkSettelemnts = mvbkSettlementRepository
				.findByIdentificationOffUsAcqRembourssementTpeRCRA();
		List<MvbkConf> allMvbkSettelemntsC = mvbkConfigR.findByCategorie(33);

		List<BkmvtiFransaBank> BkmvtiFransaBanks = new ArrayList<BkmvtiFransaBank>();

		int indexPieceComptable = getEveIndex1();
		for (MvbkConf mvk : allMvbkSettelemntsC) {
			int index2 = getEveIndex();
			BkmvtiFransaBanks = TestSigneWithoutDayOperation(uap, 14, mvk, BkmvtiFransaBanks, indexPieceComptable,
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
		    
		}//		logger.info("end ValidRembourssementTpe");
	}

	public void ValidRembourssementInternetWithoutDayOperation(UAP051FransaBank uap) {
//		logger.info("ValidRembourssementInternetCRA");
		List<MvbkSettlement> allMvbkSettelemnts = mvbkSettlementRepository
				.findByIdentificationOffUsAcqRembourssementInternetCRA();
		List<MvbkConf> allMvbkSettelemntsC = mvbkConfigR.findByCategorie(59);

		List<BkmvtiFransaBank> BkmvtiFransaBanks = new ArrayList<BkmvtiFransaBank>();

		int indexPieceComptable = getEveIndex1();
		for (MvbkConf mvk : allMvbkSettelemntsC) {
			int index2 = getEveIndex();
			BkmvtiFransaBanks = TestSigneWithoutDayOperation(uap, 24, mvk, BkmvtiFransaBanks, indexPieceComptable,
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
		    
		}//		logger.info("end ValidRembourssementInternetCRA");
	}

	public List<BkmvtiFransaBank> TestSigneWithoutDayOperation(UAP051FransaBank uap, int methode, MvbkConf mvk,
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

	public BkmvtiFransaBank TestAccountAndSigneWithoutSayOperation(UAP051FransaBank uap, int methode, int index2,
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
  
		data.put("MntRemb", mntRet);
		data.put("CommRemb", commConf);
        BigDecimal roundedValue = fIPService.evaluateWithElementswithBigDecimal(mvk.getCodeSettlement(), data).divide(new BigDecimal(100));

        DecimalFormat decimalFormat = new DecimalFormat("000000000000000000.00");
        String formattedValue = decimalFormat.format(roundedValue);

        // Replace the decimal point with a comma
        formattedValue = formattedValue.replace('.', ',');

        // save the formatted value
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

	public BkmvtiFransaBank TestAccountLengthWithoutDayOperation(UAP051FransaBank uap, int lengAccount, String Account,
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

	public BkmvtiFransaBank setSameDataWithoutDayOperation(UAP051FransaBank uap, int methode,
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

		case 14:

			bkmvtiFransaBank.setPieceComptable(uap.getPieceComptableBKM());

			bkmvtiFransaBank.setNumEvenement(uap.getPieceComptableBKM()
					.substring(uap.getPieceComptableBKM().length() - 6, uap.getPieceComptableBKM().length()));

			break;

		case 24:

			bkmvtiFransaBank.setPieceComptable(uap.getPieceComptableBKM());

			bkmvtiFransaBank.setNumEvenement(uap.getPieceComptableBKM()
					.substring(uap.getPieceComptableBKM().length() - 6, uap.getPieceComptableBKM().length()));

			break;
		default:
			System.out.println("nothing");
		}
		return bkmvtiFransaBank;
	}

	public BkmvtiFransaBank TestCodeAgenceWithoutDayOperation(UAP051FransaBank uap, String codeAgence, MvbkConf mvk,
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

	public BkmvtiFransaBank TestCodeAgence(String codeAgence, MvbkSettlement mvk, DayOperationFransaBank op,
			BkmvtiFransaBank bkmvtiFransaBank) {
		if (mvk.getCodeAgence() != null) {
			if (mvk.getCodeAgence().equals("00002")) {
				bkmvtiFransaBank.setAgence(mvk.getCodeAgence());
			}
		} else {
			bkmvtiFransaBank.setAgence(op.getCodeAgence());
		}
		return bkmvtiFransaBank;
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
