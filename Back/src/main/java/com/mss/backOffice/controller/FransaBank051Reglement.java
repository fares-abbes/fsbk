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
import java.text.ParseException;
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
import com.mss.unified.entities.BatchesFC;
import com.mss.unified.entities.BkmvtiFransaBank;
import com.mss.unified.entities.CroRejetControl;
import com.mss.unified.entities.DayOperationFransaBank;
import com.mss.unified.entities.MvbkConf;
import com.mss.unified.entities.MvbkSettlement;
import com.mss.unified.entities.UAP050FransaBank;
import com.mss.unified.entities.UAP050RFransaBank;
import com.mss.unified.entities.UAP051FransaBank;
import com.mss.unified.entities.UAP051RFransaBank;
import com.mss.unified.entities.UAPFransaBank;
import com.mss.unified.entities.User;
import com.mss.unified.entities.dayOperationReglement;
import com.mss.unified.repositories.BatchesFFCRepository;
import com.mss.unified.repositories.BkmvtiFransaBankRepository;
import com.mss.unified.repositories.CodeBankBCRepository;
import com.mss.unified.repositories.DayOperationFransaBankRepository;
import com.mss.unified.repositories.FileTRepository;
import com.mss.unified.repositories.MvbkConfigRepository;
import com.mss.unified.repositories.MvbkSettlementRepository;
import com.mss.unified.repositories.UAP051FransaBankRepository;
import com.mss.unified.repositories.UAP051RFransaBankRepository;
import com.mss.unified.repositories.UAPFransaBankRepository;
import com.mss.unified.repositories.UserRepository;
import com.mss.unified.repositories.dayOperationReglementRepository;
import com.mss.unified.repositories.OpeningDayRepository;
import com.mss.unified.repositories.CroRejetControlRepository;

@RestController
@RequestMapping("FransaBank051Reglement")
public strictfp  class FransaBank051Reglement {
	@Autowired
	FormulaInterpreterService fIPService;
	@Autowired
	OpeningDayRepository openedDayRepo;
	@Autowired
	dayOperationReglementRepository dayReglementRepo;

	@Autowired
	UAPFransaBankRepository uAPFransaBankRepository;
	@Autowired
	UAP051RFransaBankRepository uAP051RFransaBankRepository;
	@Autowired
	UAP051FransaBankRepository uAP050FransaBankRepository;
	@Autowired
	MvbkSettlementRepository mvbkSettlementRepository;
	@Autowired
	DayOperationFransaBankRepository dayOperationFransaBankRepository;
	@Autowired
	BkmvtiFransaBankRepository bkmvtiFransaBankRepository;
	@Autowired
	public MvbkConfigRepository mvbkConfigR;
	@Autowired
	BatchesFFCRepository batchesFFCRepository;
	@Autowired
	FileTRepository fileSummaryTRepository;
	@Autowired
	CodeBankBCRepository codeBankBCRepository;
	@Autowired
	BatchesFFCRepository batchRepo;

	@Autowired
	UserRepository userRepository;
	@Autowired
	PropertyService propertyService;
	@Autowired
	CroRejetControlRepository croRejetControlRepository;
	private static final Logger logger = LoggerFactory.getLogger(FransaBank050Reglement.class);

	public synchronized int getEveIndex() {
		return (++ExecutorThreadUAPINFileBC.eve);
	}

	public synchronized int getEveIndex1() {
		return (++ExecutorThreadUAPINFileBC.eve1);
	}

	@PutMapping("matchingUAP051Reglement")
	public void validFile() throws IOException, ParseException {
		BatchesFC batch = batchesFFCRepository.findByKey("CRAUAP51Reglement").get();

		batch.setBatchLastExcution(batch.getBatchEndDate());
		batch.setBatchStatus(0);
		batch.setBatchDate(new Date());
		batch.setError(null);
		batch.setErrorStackTrace(null);
		batchRepo.saveAndFlush(batch);
		for (int i = 1; i < 10; i++) {
			SimpleDateFormat newFormat = new SimpleDateFormat("yyyyMMdd");
			String today = newFormat.format(new Date());

			String name = batch.getFileName().replace("X", i + "");
			String fileName = batch.getFileLocation() + "/" + name + ".CRO";
			try {
				if (Files.exists(Paths.get(fileName))) {

					Stream<String> stream = Files.lines(Paths.get(fileName), StandardCharsets.ISO_8859_1);
					List<String> list = new ArrayList<>();
					list = stream.filter(element -> !element.startsWith("ECRO")).map(String::toUpperCase)
							.collect(Collectors.toList());

					if (list.size() == 0) {

						emptyFileOrNotExist(today);
						moveFilesByStartingName(batch.getFileLocation(), propertyService.getCompensationfilePathCro(),
								name);

						batchRepo.updateFinishBatch("CRAUAP51Reglement", 1, new Date());

					} else {

						boolean test = validTransaction(list, today, name + ".CRO");
						if (test == true) {

							moveFilesByStartingName(batch.getFileLocation(),
									propertyService.getCompensationfilePathCro(),name);
							batchRepo.updateFinishBatch("CRAUAP51Reglement", 1, new Date());
						} else {
							batchRepo.updateFinishBatch("CRAUAP51Reglement", 1, new Date());

						}
					}

				} else if (!Files.exists(Paths.get(fileName))) {
					if (i == 1) {

						emptyFileOrNotExist(today);

						batchRepo.updateFinishBatch("CRAUAP51Reglement", 1, new Date());

					} else {

						batchRepo.updateFinishBatch("CRAUAP51Reglement", 1, new Date());

					}
				}
			} catch (Exception e) {
				String stackTrace = Throwables.getStackTraceAsString(e);
				logger.info("Exception is=>{}", stackTrace);
				if (stackTrace.length() > 4000)
					stackTrace = stackTrace.substring(0, 3999);
				String error = e.getMessage() == null ? e.toString() : e.getMessage();
				if (error.length() > 255)
					error = error.substring(0, 254);

				batchRepo.updateStatusAndErrorBatch("CRAUAP51Reglement", 2, error, new Date(), stackTrace);

			}
		}
	}

	public void emptyFileOrNotExist(String dateReg) {

		List<UAP051FransaBank> ListUAPFransaBanksFiltred = uAP050FransaBankRepository.getListUAPByStatus(dateReg);
		logger.info("ListUAPFransaBanksFiltred =>{}", ListUAPFransaBanksFiltred.size());

		for (UAP051FransaBank uap : ListUAPFransaBanksFiltred) {

			if (uap.getTypeTransaction().equals("051") && uap.getTypePaiement().equals("01")) {
				remboursementTpaWithoutDayOperation(uap);

			}

			else if ((uap.getTypeTransaction().equals("051") && uap.getTypePaiement().equals("04"))
					|| (uap.getTypeTransaction().equals("051") && uap.getTypePaiement().equals("03"))) {
				remboursementInternetWithoutDayOperation(uap);

			}
		}

		List<UAP051RFransaBank> uapsReject = uAP051RFransaBankRepository.getListUAPByStatusAcceptedReject(dateReg);
		for (UAP051RFransaBank uap : uapsReject) {
			UAP051FransaBank uapcopy = new UAP051FransaBank();
			try {
				PropertyUtils.copyProperties(uapcopy, uap);
			} catch (Exception ex) {
				ex.printStackTrace();
			}
			if (uap.getTypeTransaction().equals("051") && uap.getTypePaiement().equals("01")) {
				remboursementTpaRejectWithoutDayOperation(uapcopy);

			}

			else if ((uap.getTypeTransaction().equals("051") && uap.getTypePaiement().equals("04"))
					|| (uap.getTypeTransaction().equals("051") && uap.getTypePaiement().equals("03"))) {
				remboursementInternetRejectWithoutDayOperation(uapcopy);

			}
		}

	}

	public boolean validTransaction(List<String> list, String fileDate, String fileName) {
		logger.info("date reglement 051=>{}", fileDate);
		logger.info("list rio size=>{}", list.size());
		List<UAP051FransaBank> ListUAPFransaBanksFiltred = uAP050FransaBankRepository.getListUAPByStatuswithoutDay( );
		List<UAP051FransaBank> uAPFransaBank = new ArrayList<>();
		List<UAP051RFransaBank> rejectedUap = new ArrayList<>();

		boolean test = false;
		int nbTotalAccepted = 0;
		for (UAP051FransaBank u : ListUAPFransaBanksFiltred) {

			for (String e : list) {
				if (e != null || e.length() > 115) {

					String rio = e.substring(78, 116);

					if ((u.getUapRio().equals(rio))) {
						u.setAccepted("1");
						UAP051RFransaBank uaprejected = new UAP051RFransaBank();
						try {
							PropertyUtils.copyProperties(uaprejected, u);
						} catch (Exception ex) {
							ex.printStackTrace();
						}
						if (!"00000000".equals(e.substring(38, 46))) {
							uaprejected.setDateReglement(e.substring(38, 46));
						}
						rejectedUap.add(uaprejected);
						uAPFransaBank.add(u);
						test = true;

						nbTotalAccepted++;
					}
				}
			}
		}
		;
		uAP051RFransaBankRepository.saveAll(rejectedUap);

		uAP050FransaBankRepository.saveAll(uAPFransaBank);

		List<UAP051FransaBank> uaps = uAP050FransaBankRepository.getListUAPByStatus(fileDate);
		for (UAP051FransaBank uap : uaps) {

			if (uap.getTypeTransaction().equals("051") && uap.getTypePaiement().equals("01")) {
				remboursementTpaWithoutDayOperation(uap);

			}

			else if ((uap.getTypeTransaction().equals("051") && uap.getTypePaiement().equals("04"))
					|| (uap.getTypeTransaction().equals("051") && uap.getTypePaiement().equals("03"))) {
				remboursementInternetWithoutDayOperation(uap);

			}
		}

		List<UAP051RFransaBank> uapsReject = uAP051RFransaBankRepository.getListUAPByStatusAcceptedReject(fileDate);
		for (UAP051RFransaBank uap : uapsReject) {
			UAP051FransaBank uapcopy = new UAP051FransaBank();
			try {
				PropertyUtils.copyProperties(uapcopy, uap);
			} catch (Exception ex) {
				ex.printStackTrace();
			}
			if (uap.getTypeTransaction().equals("051") && uap.getTypePaiement().equals("01")) {
				remboursementTpaRejectWithoutDayOperation(uapcopy);

			}

			else if ((uap.getTypeTransaction().equals("051") && uap.getTypePaiement().equals("04"))
					|| (uap.getTypeTransaction().equals("051") && uap.getTypePaiement().equals("03"))) {
				remboursementInternetRejectWithoutDayOperation(uapcopy);

			}
		}
		/////////// CRO control/////////////

		saveCroControl(fileName, list, nbTotalAccepted);

		/////////////////////////////////////
		return test;
	}

	private void saveCroControl(String fileName, List<String> list, int nbAccepted) {

		logger.info("fileName => {}", fileName);

		CroRejetControl croControl = new CroRejetControl();
		croControl.setFileName(fileName);
		croControl.setProcessingDate(new Date());
		croControl.setTypeCro("151");

		croControl.setNbTotalFromFile(list.size());
		logger.info("nbFromFile => {}", croControl.getNbTotalFromFile());

		// croControl.setSumValidated(sumAccepted);
		croControl.setNbTotalValidated(nbAccepted);

		logger.info("Nb rejet accepted => {}", nbAccepted);

		croRejetControlRepository.save(croControl);
	}

	public void remboursementTpaWithoutDayOperation(UAP051FransaBank uap) {
		List<MvbkSettlement> allMvbkSettelemnts = mvbkSettlementRepository
				.findByIdentificationOffUsAcqRembourssementTpeReglement();
		List<MvbkConf> allMvbkSettelemntsC = mvbkConfigR.findByCategorie(32);

		List<BkmvtiFransaBank> BkmvtiFransaBanks = new ArrayList<BkmvtiFransaBank>();
		int indexPieceComptable = getEveIndex1();
		for (MvbkConf mvk : allMvbkSettelemntsC) {
			int index2 = getEveIndex();
			BkmvtiFransaBanks = TestSigneWithoutDayOperation(uap, 14, mvk, BkmvtiFransaBanks, indexPieceComptable,
					index2, mvk.getCodeSettlement());

		}

		logger.info("remboursementTpa=>{}", BkmvtiFransaBanks.size());

		if (BkmvtiFransaBanks != null && !BkmvtiFransaBanks.isEmpty()) {
		    // Filter out null elements from the list
		    List<BkmvtiFransaBank> nonNullBkmvtiFransaBanks = BkmvtiFransaBanks.stream()
		            .filter(Objects::nonNull)
		            .collect(Collectors.toList());

		    if (!nonNullBkmvtiFransaBanks.isEmpty()) {
		        bkmvtiFransaBankRepository.saveAll(nonNullBkmvtiFransaBanks);
		    }
		    
		}
	}

	public void remboursementInternetWithoutDayOperation(UAP051FransaBank uap) {
		List<MvbkSettlement> allMvbkSettelemnts = mvbkSettlementRepository
				.findByIdentificationOffUsAcqRembourssementInternetReglement();
		List<MvbkConf> allMvbkSettelemntsC = mvbkConfigR.findByCategorie(60);

		List<BkmvtiFransaBank> BkmvtiFransaBanks = new ArrayList<BkmvtiFransaBank>();

		int indexPieceComptable = getEveIndex1();
		for (MvbkConf mvk : allMvbkSettelemntsC) {
			int index2 = getEveIndex();
			BkmvtiFransaBanks = TestSigneWithoutDayOperation(uap, 24, mvk, BkmvtiFransaBanks, indexPieceComptable,
					index2, mvk.getCodeSettlement());

		}

		logger.info("remboursementInternet=>{}", BkmvtiFransaBanks.size());

		if (BkmvtiFransaBanks != null && !BkmvtiFransaBanks.isEmpty()) {
		    // Filter out null elements from the list
		    List<BkmvtiFransaBank> nonNullBkmvtiFransaBanks = BkmvtiFransaBanks.stream()
		            .filter(Objects::nonNull)
		            .collect(Collectors.toList());

		    if (!nonNullBkmvtiFransaBanks.isEmpty()) {
		        bkmvtiFransaBankRepository.saveAll(nonNullBkmvtiFransaBanks);
		    }
		    
		}	}

	public void remboursementTpaRejectWithoutDayOperation(UAP051FransaBank uap) {
		List<MvbkSettlement> allMvbkSettelemnts = mvbkSettlementRepository
				.findByIdentificationOffUsAcqRejectRembourssement();
		List<MvbkConf> allMvbkSettelemntsC = mvbkConfigR.findByCategorie(543);

		List<BkmvtiFransaBank> BkmvtiFransaBanks = new ArrayList<BkmvtiFransaBank>();

		int indexPieceComptable = getEveIndex1();
		for (MvbkConf mvk : allMvbkSettelemntsC) {
			int index2 = getEveIndex();
			BkmvtiFransaBanks = TestSigneWithoutDayOperation(uap, 25, mvk, BkmvtiFransaBanks, indexPieceComptable,
					index2, mvk.getCodeSettlement());

		}

		logger.info("remboursementTpa=>{}", BkmvtiFransaBanks.size());

		if (BkmvtiFransaBanks != null && !BkmvtiFransaBanks.isEmpty()) {
		    // Filter out null elements from the list
		    List<BkmvtiFransaBank> nonNullBkmvtiFransaBanks = BkmvtiFransaBanks.stream()
		            .filter(Objects::nonNull)
		            .collect(Collectors.toList());

		    if (!nonNullBkmvtiFransaBanks.isEmpty()) {
		        bkmvtiFransaBankRepository.saveAll(nonNullBkmvtiFransaBanks);
		    }
		    
		}
	}

	public void remboursementInternetRejectWithoutDayOperation(UAP051FransaBank uap) {
		List<MvbkSettlement> allMvbkSettelemnts = mvbkSettlementRepository
				.findByIdentificationOffUsAcqRejctRembourssementInternet();
		List<MvbkConf> allMvbkSettelemntsC = mvbkConfigR.findByCategorie(544);

		List<BkmvtiFransaBank> BkmvtiFransaBanks = new ArrayList<BkmvtiFransaBank>();

		int indexPieceComptable = getEveIndex1();
		for (MvbkConf mvk : allMvbkSettelemntsC) {
			int index2 = getEveIndex();
			BkmvtiFransaBanks = TestSigneWithoutDayOperation(uap, 25, mvk, BkmvtiFransaBanks, indexPieceComptable,
					index2, mvk.getCodeSettlement());

		}

		logger.info("remboursementInternet=>{}", BkmvtiFransaBanks.size());

		if (BkmvtiFransaBanks != null && !BkmvtiFransaBanks.isEmpty()) {
		    // Filter out null elements from the list
		    List<BkmvtiFransaBank> nonNullBkmvtiFransaBanks = BkmvtiFransaBanks.stream()
		            .filter(Objects::nonNull)
		            .collect(Collectors.toList());

		    if (!nonNullBkmvtiFransaBanks.isEmpty()) {
		        bkmvtiFransaBankRepository.saveAll(nonNullBkmvtiFransaBanks);
		    }
		    
		}	}

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

//		getAmount(Float.parseFloat(uap.getMontantAComponser()), bkmvtiFransaBank);
//		Gson gson = new Gson();
//		HashMap<String, Float> data = gson.fromJson(uap.getDatasJson(), FransaBankController.typeJsonData);
//
//		getAmount(fIPService.evaluateWithElements(mvk.getCodeSettlement(), data), bkmvtiFransaBank);


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
		if (mvb.getLibGenerique() != null && mvb.getLibGenerique().trim() != "") {
			String libgenerique = mvb.getLibGenerique();
			libgenerique = libgenerique.replaceAll("dateTransation", uap.getDateTransaction());
			String aut = uap.getNumAutorisation();
			int lengthAuth = aut.length();
			libgenerique = libgenerique.replaceAll("numAutorisation", aut.substring(lengthAuth - 6));
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
		case 25:
			String piece = uap.getPieceComptableBKM();
			piece = piece.replaceFirst("DB", "RG");
			bkmvtiFransaBank.setPieceComptable(piece);

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

	public BkmvtiFransaBank TestCodeAgenceWithoutDayOperation(UAP051FransaBank uap, String codeAgence, MvbkConf mvk,
			BkmvtiFransaBank bkmvtiFransaBank) {
		if (mvk.getCodeAgence() != null) {
			if (mvk.getCodeAgence().equals("00002")) {
				bkmvtiFransaBank.setAgence(mvk.getCodeAgence());
			} else {
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
			String movingDate = "" + timer.getYear() + timer.getMonth() + timer.getDayOfMonth() + "_" + timer.getHour()
					+ timer.getMinute() + timer.getSecond();

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
