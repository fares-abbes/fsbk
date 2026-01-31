package com.mss.backOffice.controller;

import java.io.File;
import java.io.FileWriter;
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
import java.time.Period;
import java.time.ZoneId;
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
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
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
import com.mss.unified.entities.CroRejetControl;
import com.mss.unified.entities.DayOperationFransaBank;
import com.mss.unified.entities.FileHeaderT;
import com.mss.unified.entities.MvbkConf;
import com.mss.unified.entities.MvbkSettlement;
import com.mss.unified.entities.UAP050FransaBank;
import com.mss.unified.entities.UAPFransaBank;
import com.mss.unified.entities.UAPFransaBankHistory;
import com.mss.unified.entities.UAPFransaBankR;
import com.mss.unified.entities.User;
import com.mss.unified.entities.dayOperationReglement;
import com.mss.unified.repositories.BatchesFFCRepository;
import com.mss.unified.repositories.BkmvtiFransaBankRepository;
import com.mss.unified.repositories.CodeBankBCRepository;
import com.mss.unified.repositories.DayOperationFransaBankRepository;
import com.mss.unified.repositories.FileTRepository;
import com.mss.unified.repositories.MvbkConfigRepository;
import com.mss.unified.repositories.MvbkSettlementRepository;
import com.mss.unified.repositories.UAP050FransaBankRepository;
import com.mss.unified.repositories.UAPFransaBankRRepository;
import com.mss.unified.repositories.UAPFransaBankRepository;
import com.mss.unified.repositories.UserRepository;
import com.mss.unified.repositories.dayOperationReglementRepository;
import com.mss.unified.repositories.OpeningDayRepository;
import com.mss.unified.repositories.CroRejetControlRepository;

@RestController
@RequestMapping("FransaBank040Reglement")
public strictfp class FransaBank040Reglement {
	private static final Logger logger = LoggerFactory.getLogger(FransaBank040Reglement.class);
	@Autowired
	OpeningDayRepository openedDayRepo;
	@Autowired
	dayOperationReglementRepository dayReglementRepo;
	@Autowired
	UAPFransaBankRepository uAPFransaBankRepository;
	@Autowired
	UAPFransaBankRRepository uAPFransaBankRRepository;

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
	FormulaInterpreterService fIPService;
	@Autowired
	CroRejetControlRepository croRejetControlRepository;
	public static List<BkmvtiFransaBank> listToSave;

	public synchronized int getEveIndex() {
		return (++ExecutorThreadUAPINFileBC.eve);
	}

	public synchronized int getEveIndex1() {
		return (++ExecutorThreadUAPINFileBC.eve1);
	}

	@PutMapping("matchingUAP040Reglement")
	public void validFile() throws IOException, ParseException {
		BatchesFC batch = batchesFFCRepository.findByKey("CRAUAP40Reglement").get();
		batch.setBatchLastExcution(batch.getBatchEndDate());
		batch.setBatchStatus(0);
		batch.setBatchDate(new Date());
		batch.setError(null);
		batch.setErrorStackTrace(null);
		batchRepo.saveAndFlush(batch);
		listToSave = new ArrayList<>();
		for (int i = 1; i < 10; i++) {
			SimpleDateFormat newFormat = new SimpleDateFormat("yyyyMMdd");
			String today = newFormat.format(new Date());
			String Name = batch.getFileName().replace("X", i + "");
			String fileName = batch.getFileLocation() + "/" + Name + ".CRO";
			try {
				if (Files.exists(Paths.get(fileName))) {
					logger.info("***** file reglement 040 exist =>{}", fileName);
					Stream<String> stream = Files.lines(Paths.get(fileName), StandardCharsets.ISO_8859_1);
					List<String> list = new ArrayList<>();
					list = stream.filter(element -> !element.startsWith("ECRO")).map(String::toUpperCase)
							.collect(Collectors.toList());

					if (list.size() != 0) {
						logger.info("******* file reglement exist and not empty *****");

						boolean test = validTransaction(list, today, Name + ".CRO");
						if (test == true) {
							moveFilesByStartingName(batch.getFileLocation(),
									propertyService.getCompensationfilePathCro(), Name);
							batchRepo.updateFinishBatch("CRAUAP40Reglement", 1, new Date());

						} else {
							batchRepo.updateFinishBatch("CRAUAP40Reglement", 1, new Date());

						}

					}

				}

				else if (!Files.exists(Paths.get(fileName))) {
					if (i == 1) {
						emptyFileOrNotExist(today);
						batchRepo.updateFinishBatch("CRAUAP40Reglement", 1, new Date());

					}

					else {

						batchRepo.updateFinishBatch("CRAUAP40Reglement", 1, new Date());

					}

				}
				bkmvtiFransaBankRepository.saveAll(listToSave);

			}

			catch (Exception e) {
				String stackTrace = Throwables.getStackTraceAsString(e);
				logger.info("Exception is=>{}", stackTrace);
				if (stackTrace.length() > 4000)
					stackTrace = stackTrace.substring(0, 3999);
				String error = e.getMessage() == null ? e.toString() : e.getMessage();
				if (error.length() > 255)
					error = error.substring(0, 254);

				batchRepo.updateStatusAndErrorBatch("CRAUAP40Reglement", 2, error, new Date(), stackTrace);

			}
		}
	}

	public void emptyFileOrNotExist(String reg) {
		logger.info("date reglement 040 =>{}", reg);
		List<UAPFransaBank> ListUAPFransaBanksFiltred = uAPFransaBankRepository.getListUAPByStatus(reg);
		logger.info("******ListUAPFransaBanksFiltred*****=>{}", ListUAPFransaBanksFiltred.size());
		logger.info("******emptyFileOrNotExist*****");

		for (UAPFransaBank uap : ListUAPFransaBanksFiltred) {

			if (uap.getTypeTransaction().equals("040") && uap.getTypeRetrait().equals("001")) {
				if (uap.getTagRetrait().equals("1")) {
					ValidRetraitWithoutDayOperation(uap);

				} else if (uap.getTagRetrait().equals("2")) {
					ValidRetraitAlgeriePosteWithoutDayOperation(uap);
				}

			} else if (uap.getTypeTransaction().equals("040") && uap.getTypeRetrait().equals("002")) {
				ValidConsultationSoldeWithoutDayOperation(uap);

			}

		}
		FileRequest.print("start hahndling reject", FileRequest.getLineNumber());
		List<UAPFransaBankR> uapsReject = uAPFransaBankRRepository.getListUAPByStatusAcceptedReject(reg);
		FileRequest.print(uapsReject.size() + "", FileRequest.getLineNumber());
		for (UAPFransaBankR uap : uapsReject) {
			UAPFransaBank uapcopy = new UAPFransaBank();
			try {
				PropertyUtils.copyProperties(uapcopy, uap);
			} catch (Exception ex) {
				ex.printStackTrace();
			}
			if (uap.getTypeTransaction().equals("040") && uap.getTypeRetrait().equals("001")) {
				if (uap.getTagRetrait().equals("1")) {
					ValidRetraitRejectWithoutDayOperation(uapcopy);
				} else if (uap.getTagRetrait().equals("2")) {
					ValidRetraitAlgeriePosteRejectWithoutDayOperation(uapcopy);
				}

			} else if (uap.getTypeTransaction().equals("040") && uap.getTypeRetrait().equals("002")) {
				ValidConsultationSoldeRejectWithoutDayOperation(uapcopy);

			}

		}

		logger.info("*****end emptyFileOrNotExist******");

	}

	public boolean validTransaction(List<String> list, String fileDate, String fileName) {

		logger.info("date reglement 040=>{}", fileDate);
		logger.info("list rio size=>{}", list.size());

		List<UAPFransaBank> ListUAPFransaBanksFiltred = uAPFransaBankRepository.getListUAPByStatuswithoutDay();
		Map<String, UAPFransaBank> fileUap = new HashMap<>();

		ListUAPFransaBanksFiltred.forEach(u -> {
			fileUap.put(u.getUapRio(), u);

		});
		logger.info("ListUAPFransaBanksFiltred size=>{}", ListUAPFransaBanksFiltred.size());
		List<UAPFransaBank> uAPFransaBank = new ArrayList<>();
		List<UAPFransaBankR> rejectedUap = new ArrayList<>();
		boolean test = false;

		int nbTotalAccepted = 0;
		for (String e : list) {
			if (e != null || e.length() > 115) {
				String rio = e.substring(78, 116);
				if (fileUap.containsKey(rio)) {
					UAPFransaBank uap = fileUap.get(rio);

					uap.setAccepted("1");
					logger.info("valid rio =>{}", rio);
					test = true;
					UAPFransaBankR uaprejected = new UAPFransaBankR();
					try {
						PropertyUtils.copyProperties(uaprejected, uap);
					} catch (Exception ex) {
						ex.printStackTrace();
					}
					if (!"00000000".equals(e.substring(38, 46))) {
						uaprejected.setDateReglement(e.substring(38, 46));
					}
					rejectedUap.add(uaprejected);
					uAPFransaBank.add(uap);

					nbTotalAccepted++;

				}
			}
		}

		FileRequest.print("start saving reject " + uAPFransaBank.size(), FileRequest.getLineNumber());
		uAPFransaBankRRepository.saveAll(rejectedUap);
		uAPFransaBankRepository.saveAll(uAPFransaBank);
		FileRequest.print("start hahndling valid", FileRequest.getLineNumber());
		List<UAPFransaBank> uaps = uAPFransaBankRepository.getListUAPByStatus(fileDate);
		for (UAPFransaBank uap : uaps) {
			if (uap.getTypeTransaction().equals("040") && uap.getTypeRetrait().equals("001")) {
				if (uap.getTagRetrait().equals("1")) {
					ValidRetraitWithoutDayOperation(uap);
				} else if (uap.getTagRetrait().equals("2")) {
					ValidRetraitAlgeriePosteWithoutDayOperation(uap);
				}

			} else if (uap.getTypeTransaction().equals("040") && uap.getTypeRetrait().equals("002")) {
				ValidConsultationSoldeWithoutDayOperation(uap);

			}
		}
		FileRequest.print("start hahndling reject", FileRequest.getLineNumber());
		List<UAPFransaBankR> uapsReject = uAPFransaBankRRepository.getListUAPByStatusAcceptedReject(fileDate);
		FileRequest.print(uapsReject.size() + "", FileRequest.getLineNumber());
		for (UAPFransaBankR uap : uapsReject) {
			UAPFransaBank uapcopy = new UAPFransaBank();
			try {
				PropertyUtils.copyProperties(uapcopy, uap);
			} catch (Exception ex) {
				ex.printStackTrace();
			}
			if (uap.getTypeTransaction().equals("040") && uap.getTypeRetrait().equals("001")) {
				if (uap.getTagRetrait().equals("1")) {
					ValidRetraitRejectWithoutDayOperation(uapcopy);
				} else if (uap.getTagRetrait().equals("2")) {
					ValidRetraitAlgeriePosteRejectWithoutDayOperation(uapcopy);
				}

			} else if (uap.getTypeTransaction().equals("040") && uap.getTypeRetrait().equals("002")) {
				ValidConsultationSoldeRejectWithoutDayOperation(uapcopy);

			}

		}

		/////////// CRO control/////////////

		saveCroControl(fileName, list, nbTotalAccepted);

		/////////////////////////////////////

		FileRequest.print("******end valid Transaction reglement*****", FileRequest.getLineNumber());
		return test;
	}

	private void saveCroControl(String fileName, List<String> list, int nbAccepted) {

		logger.info("fileName => {}", fileName);

		CroRejetControl croControl = new CroRejetControl();
		croControl.setFileName(fileName);
		croControl.setProcessingDate(new Date());
		croControl.setTypeCro("140");

		croControl.setNbTotalFromFile(list.size());
		logger.info("nbFromFile => {}", croControl.getNbTotalFromFile());

		// croControl.setSumValidated(sumAccepted);
		croControl.setNbTotalValidated(nbAccepted);

		logger.info("Nb rejet accepted => {}", nbAccepted);

		croRejetControlRepository.save(croControl);
	}

	public void ValidRetraitWithoutDayOperation(UAPFransaBank uap) {
		List<MvbkSettlement> allMvbkSettelemnts = mvbkSettlementRepository.findByIdentificationReglementRetrait();
		List<BkmvtiFransaBank> BkmvtiFransaBanks = new ArrayList<BkmvtiFransaBank>();
		List<MvbkConf> allMvbkSettelemntsC = mvbkConfigR.findByCategorie(30);

		int indexPieceComptable = getEveIndex1();
		for (MvbkConf mvk : allMvbkSettelemntsC) {
			int index2 = getEveIndex();
			BkmvtiFransaBanks = TestSigneWithoutDayOperation(uap, 3, mvk, BkmvtiFransaBanks, indexPieceComptable,
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
			listToSave.addAll(BkmvtiFransaBanks);
		}
//		bkmvtiFransaBankRepository.saveAll(BkmvtiFransaBanks);
	}

	public void ValidRetraitAlgeriePosteWithoutDayOperation(UAPFransaBank uap) {
		List<MvbkSettlement> allMvbkSettelemntsALP = mvbkSettlementRepository.findByIdentificationReglementRetraitALP();
		List<MvbkConf> allMvbkSettelemntsC = mvbkConfigR.findByCategorie(50);

		List<BkmvtiFransaBank> BkmvtiFransaBanksALP = new ArrayList<BkmvtiFransaBank>();

		int indexPieceComptable = getEveIndex1();
		for (MvbkConf mvk : allMvbkSettelemntsC) {
			int index2 = getEveIndex();
			BkmvtiFransaBanksALP = TestSigneWithoutDayOperation(uap, 15, mvk, BkmvtiFransaBanksALP, indexPieceComptable,
					index2, mvk.getCodeSettlement());

		}
		listToSave.addAll(BkmvtiFransaBanksALP);
//		bkmvtiFransaBankRepository.saveAll(BkmvtiFransaBanksALP);
	}

	public void ValidConsultationSoldeWithoutDayOperation(UAPFransaBank uap) {
		List<MvbkSettlement> allMvbkSettelemnts = mvbkSettlementRepository
				.findByIdentificationConsultationSoldeReglement();
		List<MvbkConf> allMvbkSettelemntsC = mvbkConfigR.findByCategorie(70);

		List<BkmvtiFransaBank> BkmvtiFransaBanksC = new ArrayList<BkmvtiFransaBank>();

		int indexPieceComptable = getEveIndex1();
		for (MvbkConf mvk : allMvbkSettelemntsC) {
			int index2 = getEveIndex();
			BkmvtiFransaBanksC = TestSigneWithoutDayOperation(uap, 11, mvk, BkmvtiFransaBanksC, indexPieceComptable,
					index2, mvk.getCodeSettlement());

		}
		listToSave.addAll(BkmvtiFransaBanksC);

//		bkmvtiFransaBankRepository.saveAll(BkmvtiFransaBanksC);
	}

	public void ValidRetraitRejectWithoutDayOperation(UAPFransaBank uap) {
		List<MvbkSettlement> allMvbkSettelemnts = mvbkSettlementRepository.findByIdentificationRetraitRejet();
		List<MvbkConf> allMvbkSettelemntsC = mvbkConfigR.findByCategorie(539);

		List<BkmvtiFransaBank> BkmvtiFransaBanks = new ArrayList<BkmvtiFransaBank>();

		int indexPieceComptable = getEveIndex1();
		for (MvbkConf mvk : allMvbkSettelemntsC) {
			int index2 = getEveIndex();
			BkmvtiFransaBanks = TestSigneWithoutDayOperation(uap, 25, mvk, BkmvtiFransaBanks, indexPieceComptable,
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
			listToSave.addAll(BkmvtiFransaBanks);
		}

//		bkmvtiFransaBankRepository.saveAll(BkmvtiFransaBanks);
	}

	public void ValidConsultationSoldeRejectWithoutDayOperation(UAPFransaBank uap) {
		List<MvbkSettlement> allMvbkSettelemnts = mvbkSettlementRepository.findByIdentificationRetraitRejet();
		List<MvbkConf> allMvbkSettelemntsC = mvbkConfigR.findByCategorie(538);

		List<BkmvtiFransaBank> BkmvtiFransaBanks = new ArrayList<BkmvtiFransaBank>();

		int indexPieceComptable = getEveIndex1();
		for (MvbkConf mvk : allMvbkSettelemntsC) {
			int index2 = getEveIndex();
			BkmvtiFransaBanks = TestSigneWithoutDayOperation(uap, 25, mvk, BkmvtiFransaBanks, indexPieceComptable,
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
			listToSave.addAll(BkmvtiFransaBanks);
		}
//		bkmvtiFransaBankRepository.saveAll(BkmvtiFransaBanks);
	}

	public void ValidRetraitAlgeriePosteRejectWithoutDayOperation(UAPFransaBank uap) {
		List<MvbkSettlement> allMvbkSettelemntsALP = mvbkSettlementRepository.findByIdentificationRejetRetraitALP();
		List<MvbkConf> allMvbkSettelemntsC = mvbkConfigR.findByCategorie(540);

		List<BkmvtiFransaBank> BkmvtiFransaBanksALP = new ArrayList<BkmvtiFransaBank>();

		int indexPieceComptable = getEveIndex1();
		for (MvbkConf mvk : allMvbkSettelemntsC) {
			int index2 = getEveIndex();
			BkmvtiFransaBanksALP = TestSigneWithoutDayOperation(uap, 25, mvk, BkmvtiFransaBanksALP, indexPieceComptable,
					index2, mvk.getCodeSettlement());

		}
		listToSave.addAll(BkmvtiFransaBanksALP);

//		bkmvtiFransaBankRepository.saveAll(BkmvtiFransaBanksALP);
	}

	public List<BkmvtiFransaBank> TestSigneWithoutDayOperation(UAPFransaBank uap, int methode, MvbkConf mvk,
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

	public BkmvtiFransaBank TestAccountAndSigneWithoutSayOperation(UAPFransaBank uap, int methode, int index2,
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
		int lengthRefDossier = (uap.getNumtransaction() + uap.getDateTransaction()
				+ uap.getNumAutorisation().substring(lengthNumAuth - 6, lengthNumAuth)).length();

		bkmvtiFransaBank.setRefDossier(uap.getNumtransaction() + uap.getDateTransaction()
				+ uap.getNumAutorisation().substring(lengthNumAuth - 6, lengthNumAuth)
				+ getSpace(50 - lengthRefDossier));

		TestCodeAgenceWithoutDayOperation(uap, mvk.getCodeAgence(), mvk, bkmvtiFransaBank);

		getTransactionDate(uap.getDateTransaction(), bkmvtiFransaBank);

		int lengthReferanceLettrage = uap.getNumAutorisation().length();

		bkmvtiFransaBank.setReferanceLettrage(uap.getDateTransaction().substring(6, uap.getDateTransaction().length())
				+ uap.getNumAutorisation().substring(lengthReferanceLettrage - 6, lengthReferanceLettrage));

 
		HashMap<String, BigDecimal> data = new HashMap<String, BigDecimal>();
		BigDecimal mntRet= new BigDecimal( uap.getMontantTranasction());
		BigDecimal commConf= new BigDecimal(  uap.getMontantCommissionFSBKHt());
		BigDecimal commTVA= new BigDecimal(  uap.getMontantCommissionTVA());
  
		data.put("MntRet", mntRet);
		data.put("commFSBKHT", commConf);
		data.put("commTVA", commTVA);
        BigDecimal roundedValue = fIPService.evaluateWithElementswithBigDecimal(mvk.getCodeSettlement(), data).divide(new BigDecimal(100));

        DecimalFormat decimalFormat = new DecimalFormat("000000000000000000.00");
        String formattedValue = decimalFormat.format(roundedValue);

        // Replace the decimal point with a comma
        formattedValue = formattedValue.replace('.', ',');

        // Print the formatted value
        bkmvtiFransaBank.setMontant(formattedValue);
		
		
		bkmvtiFransaBank.setCodeDeviceOrigine("208");
		bkmvtiFransaBank.setIdentification(mvk.getIdentification());
		bkmvtiFransaBank.setNumRefTransactions(uap.getNumtransaction());

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

	public BkmvtiFransaBank TestAccountLengthWithoutDayOperation(UAPFransaBank uap, int lengAccount, String Account,
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

	public BkmvtiFransaBank setSameDataWithoutDayOperation(UAPFransaBank uap, int methode,
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
		bkmvtiFransaBank.setPieceComptable(uap.getPieceComptableBKM());

		bkmvtiFransaBank.setNumEvenement(uap.getPieceComptableBKM().substring(uap.getPieceComptableBKM().length() - 6,
				uap.getPieceComptableBKM().length()));
		switch (methode) {

		case 25:
			String piece = uap.getPieceComptableBKM();
			piece = piece.replaceFirst("DB", "RG");
			bkmvtiFransaBank.setPieceComptable(piece);
			break;

		default:
			bkmvtiFransaBank.setPieceComptable(uap.getPieceComptableBKM());

			bkmvtiFransaBank.setNumEvenement(uap.getPieceComptableBKM()
					.substring(uap.getPieceComptableBKM().length() - 6, uap.getPieceComptableBKM().length()));
		}
		return bkmvtiFransaBank;
	}

	public BkmvtiFransaBank TestCodeAgenceWithoutDayOperation(UAPFransaBank uap, String codeAgence, MvbkConf mvk,
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