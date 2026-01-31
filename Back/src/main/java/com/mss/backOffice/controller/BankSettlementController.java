package com.mss.backOffice.controller;


import com.mss.backOffice.services.FormulaInterpreterService;
import com.mss.unified.entities.*;
import com.mss.unified.repositories.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("BankSettlementController")
public class BankSettlementController extends Thread {
	@Autowired
	UAP070FransaBankRepository uap070FransaBankRepo;

	@Autowired
	UAP070INRepository uap070INRepo;
	String porteur = "PORTEUR";
	String atm = "ATM";
	String merchant = "MERCHANT";
	@Autowired
	TvaCommissionFransaBankRepository tvaRepo;
	@Autowired
	AtmTerminalRepository atmTerminalRepo;

	@Autowired
	CardRepository cardRepository;

	@Autowired
	AccountRepository accountRepo;

	@Autowired
	MerchantRepository merchantRepo;

	@Autowired
	FormulaInterpreterService fIPService;
	@Autowired
	DayOperationMPRepository dayOperationRepo;
	@Autowired
	DayOperationInternationalRepository  dayOperationInterRepo;
	@Autowired
	MvbkConfigRepository mvbkConfigRepo;


	@Autowired
	SettelementFransaBankRepository SettelementFransaBankRepo;
	@Autowired
	BkmvtiMPFransaBankRepository BkmvtiMPFransaBankRepository;

	@Autowired
	UserRepository userRepository;

	private static final int startRange = 5000;
    private static final int EndRange = 6000;
    @Autowired
    private BkmvtiVisaRepository bkmvtiVisaRepository;

	public static boolean isNumeric(String str) {
		if (str == null || str.isEmpty()) {
			return false;
		}
		try {
			// Try parsing the string as a double
			Double.parseDouble(str);
			return true;
		} catch (NumberFormatException e) {
			// If an exception is caught, the string is not numeric
			return false;
		}
	}

	private static final Logger logger = LoggerFactory.getLogger(BankSettlementController.class);

 	private int recordNumber;
 	private static String tvaGlob = "TVA";
	public static Integer eve1 = 0;
	public static int eve = 0;

	public synchronized int getEveIndex() {
		return (++eve);
	}

	public static int getEveIndex1() {
		synchronized (eve1) {
			eve1 = eve1 + 1;
		}

		return eve1;
	}






	@PostMapping("/generateBankSettCode")
	public String generateBankSettCode() throws Exception {
		DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.FRENCH);
		symbols.setDecimalSeparator('.');
		DecimalFormat decimalFormat = new DecimalFormat("0.###", symbols);
		logger.info("debut extraction credit debit account all settlements");


		String type="DAYOPMP";
		List<Integer> categories = mvbkConfigRepo.findDistinctByStatusLevel(type);
		logger.info("car size " + categories.size());


		String initialTva = tvaRepo.findTva().getTva();
		Double tva = Double.parseDouble(initialTva) / 100.0;
		// FOR TEST
//		List<String> categories = bankSettelementRepository.findDistinctCategorie_03();
// 		List<String> categories = bankSettelementRepository.findDistinctCategorie_01_02_03();
//		List<String> categories = new ArrayList<String>();
//		categories.add("8");
		recordNumber = 0;


			for (Integer categorie : categories) {
//				for (String categorie : Arrays.asList("24")) {

				List<String> identifications = new ArrayList<>();
				System.out.println("categorie " + categorie);
				logger.info("categorie " + categorie);
				List<MvbkConf> listmvbkConf = mvbkConfigRepo.findByCategorie(categorie);

				for (MvbkConf mvbkConf : listmvbkConf) {
					if (mvbkConf.getCodeSettlement() != null
							&& "" != mvbkConf.getCodeSettlement().trim()) {
						List<String> elements = fIPService.extractElementsFromFormula(mvbkConf.getCodeSettlement());
						identifications.addAll(elements);
					}
				}

				if (identifications.isEmpty()) {
					continue;
				}

				List<MvbkConf> listMvbkConf = mvbkConfigRepo.findByCategorie(categorie);

				List<DayOperationMP> listDayOperation = new ArrayList<>();
				if (!identifications.isEmpty()) {
					listDayOperation = dayOperationRepo.findByIdentificationsCode(identifications);
				}

				if (!listDayOperation.isEmpty()) {
					generateBKMS(listDayOperation, listMvbkConf);
				}


			}
			//bSCC.generate_file();

		logger.info(">>>>>>>>>>>>>>>>> Last Save DONE");
		logger.info("fin extraction credit debit account all settlements");
		return "Done";

	}

	@PostMapping("/generateBankSettCodeInter")
	public String generateBankSettCodeInter() throws Exception {
		DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.FRENCH);
		symbols.setDecimalSeparator('.');
		DecimalFormat decimalFormat = new DecimalFormat("0.###", symbols);
		logger.info("debut extraction credit debit account all settlements");


		String type="Inter";
		List<Integer> categories = mvbkConfigRepo.findDistinctByStatusLevel(type);
		logger.info("car size " + categories.size());


		String initialTva = tvaRepo.findTva().getTva();
		Double tva = Double.parseDouble(initialTva) / 100.0;
		// FOR TEST
//		List<String> categories = bankSettelementRepository.findDistinctCategorie_03();
// 		List<String> categories = bankSettelementRepository.findDistinctCategorie_01_02_03();
//		List<String> categories = new ArrayList<String>();
//		categories.add("8");
		recordNumber = 0;


		for (Integer categorie : categories) {
//				for (String categorie : Arrays.asList("24")) {

			List<String> identifications = new ArrayList<>();
			System.out.println("categorie " + categorie);
			logger.info("categorie " + categorie);
			List<MvbkConf> listmvbkConf = mvbkConfigRepo.findByCategorie(categorie);

			for (MvbkConf mvbkConf : listmvbkConf) {
				if (mvbkConf.getCodeSettlement() != null
						&& "" != mvbkConf.getCodeSettlement().trim()) {
					List<String> elements = fIPService.extractElementsFromFormula(mvbkConf.getCodeSettlement());
					identifications.addAll(elements);
				}
			}

			if (identifications.isEmpty()) {
				continue;
			}

			List<MvbkConf> listMvbkConf = mvbkConfigRepo.findByCategorie(categorie);

			List<DayOperationInternational> listDayOperation = new ArrayList<>();
			if (!identifications.isEmpty()) {
				listDayOperation = dayOperationInterRepo.findByIdentificationsCode(identifications);
			}

			if (!listDayOperation.isEmpty()) {
				generateBKMSInter(listDayOperation, listMvbkConf);
			}


		}
		//bSCC.generate_file();

		logger.info(">>>>>>>>>>>>>>>>> Last Save DONE");
		logger.info("fin extraction credit debit account all settlements");
		return "Done";

	}

	//		type="UAP070IN";
//		type="UAP070OUT";
//type=UAP070INREG
//		type=UAP070OUTREG
	@PostMapping("/generateBankSettCodeUAP070")
	public String generateBankSettCodeUAP070(@RequestParam String type, @RequestParam(required = false) List<TransactionEntity> transactionLists) throws Exception {
		DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.FRENCH);
		symbols.setDecimalSeparator('.');
		DecimalFormat decimalFormat = new DecimalFormat("0.###", symbols);
		logger.info("debut extraction credit debit account all settlements");
// Calculate today's date in YYYYMMdd format for comparison


		List<Integer> categories = mvbkConfigRepo.findDistinctByStatusLevel(type);
		logger.info("car size " + categories.size());


		String initialTva = tvaRepo.findTva().getTva();
		Double tva = Double.parseDouble(initialTva) / 100.0;

		recordNumber = 0;


			for (Integer categorie : categories) {
//				for (String categorie : Arrays.asList("24")) {

 				System.out.println("categorie " + categorie);
				logger.info("categorie " + categorie);
				List<MvbkConf> listmvbkConf = mvbkConfigRepo.findByCategorie(categorie);



				List<MvbkConf> listMvbkConf = mvbkConfigRepo.findByCategorie(categorie);

				List<TransactionEntity> uaps = new ArrayList<>();
				if (transactionLists!= null){
					uaps=transactionLists;
				}else {
				if (  type.contains("UAP070IN")) {
					uaps = new ArrayList<>(uap070INRepo.findByOriginContainingIgnoreCase(listmvbkConf.get(0).getTypeOp()));
				}
				if (  type.contains("OUT")) {
					uaps = new ArrayList<>(uap070FransaBankRepo.findByOriginContainingIgnoreCase(listmvbkConf.get(0).getTypeOp() ));
				}
				}

				if (!uaps.isEmpty()) {
					generateBKMSUAP070(uaps, listMvbkConf);
				}


			}
			//bSCC.generate_file();

		logger.info(">>>>>>>>>>>>>>>>> Last Save DONE");
		logger.info("fin extraction credit debit account all settlements");
		return "Done";

	}


	private List<String> identificaitonExtractor(List<DayOperationMP> days) {
		List<String> dataList = new ArrayList<>();
		for (DayOperationMP elemnt : days) {

			String identification = elemnt.getIdentification();
			dataList.add(identification);
		}
		return dataList;
		// TODO Auto-generated method stub

	}

public String getZero(int count)

{

	String zero = "";

	for (int i = 0; i < count; i++)

		zero += "0";

	return zero;

}

	public int getIndex(int indextoadd) {

		indextoadd += 1;

		return indextoadd;
	}

	public String getSpace(int count)

	{

		String Space = "";

		for (int i = 0; i < count; i++)

			Space += " ";

		return Space;

	}
	public String getAmountFormat(double amount) {
		float m = Math.round(Math.abs(amount));
		int a = (int) m;

		String amountFormat = String.format("%018d,%02d", a / 100, a % 100);

		return amountFormat;
	}

// ****** ACHAT INTERNET ALGERIE POSTE Wissal
public void generateBKMSUAP070(List<TransactionEntity> uaps, List<MvbkConf> mvbks ) {
	logger.info("begin Generation");
	String name = ExecutorMobileThreads.name;
	List<BkmvtiMPFransaBank> BkmvtiMPFransaBanks = new ArrayList<BkmvtiMPFransaBank>();
	Map<String, List<DayOperationMP>> groupedByTransactionKey = new HashMap<>();

	for (TransactionEntity uap : uaps) {

 		int indexPieceComptable = getEveIndex1();
		for (MvbkConf mvk : mvbks) {
			int index2 = getEveIndex();
			BkmvtiMPFransaBanks = TestSigneUAP070(mvk, uap, BkmvtiMPFransaBanks,
					indexPieceComptable, index2);

		}

	}
	logger.info("end Generation =>{}", BkmvtiMPFransaBanks.size());
	if (BkmvtiMPFransaBanks != null && !BkmvtiMPFransaBanks.isEmpty()) {
		// Filter out null elements from the list
		List<BkmvtiMPFransaBank> nonNullBkmvtiMPFransaBanks = BkmvtiMPFransaBanks.stream()
				.filter(Objects::nonNull)
				.collect(Collectors.toList());

		if (!nonNullBkmvtiMPFransaBanks.isEmpty()) {
			BkmvtiMPFransaBankRepository.saveAll(nonNullBkmvtiMPFransaBanks);
		}

	}
}


	public void generateBKMS( List<DayOperationMP>dayOperationFransaBanks,List<MvbkConf> mvbks ) {
		logger.info("begin Generation");
		String name = ExecutorMobileThreads.name;
		List<BkmvtiMPFransaBank> BkmvtiMPFransaBanks = new ArrayList<BkmvtiMPFransaBank>();
		Map<String, List<DayOperationMP>> groupedByTransactionKey = new HashMap<>();

		for (DayOperationMP day : dayOperationFransaBanks) {
			String transactionKey = day.getNumRefTransaction() + "_" + day.getNumAutorisation();
			groupedByTransactionKey.computeIfAbsent(transactionKey, k -> new ArrayList<>()).add(day);
		}

		// Loop through the grouped keys
		for (Map.Entry<String, List<DayOperationMP>> entry : groupedByTransactionKey.entrySet()) {
			String transactionKey = entry.getKey();
			List<DayOperationMP> dayOperationFransaBanksByNumTransaction = entry.getValue();
			int indexPieceComptable = getEveIndex1();
			for (MvbkConf mvk : mvbks) {
				int index2 = getEveIndex();
				BkmvtiMPFransaBanks = TestSigne(mvk, dayOperationFransaBanksByNumTransaction, BkmvtiMPFransaBanks,
						indexPieceComptable, index2);

			}
			updateDays(20, indexPieceComptable, dayOperationFransaBanksByNumTransaction, mvbks.get(0));

		}
		logger.info("end Generation =>{}", BkmvtiMPFransaBanks.size());
		if (BkmvtiMPFransaBanks != null && !BkmvtiMPFransaBanks.isEmpty()) {
			// Filter out null elements from the list
			List<BkmvtiMPFransaBank> nonNullBkmvtiMPFransaBanks = BkmvtiMPFransaBanks.stream()
					.filter(Objects::nonNull)
					.collect(Collectors.toList());

			if (!nonNullBkmvtiMPFransaBanks.isEmpty()) {
				BkmvtiMPFransaBankRepository.saveAll(nonNullBkmvtiMPFransaBanks);
			}

		}
	}
	public void generateBKMSInter( List<DayOperationInternational>dayOperationFransaBanks,List<MvbkConf> mvbks ) {
		logger.info("begin Generation");
		String name = ExecutorMobileThreads.name;
		List<BkmvtiVisa> BkmvtiMPFransaBanks = new ArrayList<BkmvtiVisa>();
		Map<String, List<DayOperationInternational>> groupedByTransactionKey = new HashMap<>();

		for (DayOperationInternational day : dayOperationFransaBanks) {
			String transactionKey = day.getReferenceNumber() ;
			groupedByTransactionKey.computeIfAbsent(transactionKey, k -> new ArrayList<>()).add(day);
		}

		// Loop through the grouped keys
		for (Map.Entry<String, List<DayOperationInternational>> entry : groupedByTransactionKey.entrySet()) {
			String transactionKey = entry.getKey();
			List<DayOperationInternational> dayOperationFransaBanksByNumTransaction = entry.getValue();
			int indexPieceComptable = getEveIndex1();
			for (MvbkConf mvk : mvbks) {

				int index2 = getEveIndex();
				BkmvtiMPFransaBanks = TestSigneInter(mvk, dayOperationFransaBanksByNumTransaction, BkmvtiMPFransaBanks,
						indexPieceComptable, index2);
				if (dayOperationFransaBanksByNumTransaction.get(0).getPieceComptable()!=null && !!dayOperationFransaBanksByNumTransaction.get(0).getPieceComptable().isEmpty()) {
					BkmvtiMPFransaBanks.forEach(bkmvtiMPFransaBank -> {bkmvtiMPFransaBank.setPieceComptable(dayOperationFransaBanksByNumTransaction.get(0).getPieceComptable());});
				}else{
					updateDaysInter(dayOperationFransaBanksByNumTransaction,BkmvtiMPFransaBanks.get(0));
				}

			}

		}
		logger.info("end Generation =>{}", BkmvtiMPFransaBanks.size());
		if (BkmvtiMPFransaBanks != null && !BkmvtiMPFransaBanks.isEmpty()) {
			// Filter out null elements from the list
			List<BkmvtiVisa> nonNullBkmvtiMPFransaBanks = BkmvtiMPFransaBanks.stream()
					.filter(Objects::nonNull)
					.collect(Collectors.toList());

			if (!nonNullBkmvtiMPFransaBanks.isEmpty()) {
				bkmvtiVisaRepository.saveAll(nonNullBkmvtiMPFransaBanks);
			}

		}
	}

	public List<BkmvtiMPFransaBank> TestSigne(  MvbkConf mvk,
											List<DayOperationMP> dayOperationFransaBanksByNumTransaction,
											List<BkmvtiMPFransaBank> BkmvtiMPFransaBanks, int indexPieceComptable, int index2 ) {
		BkmvtiMPFransaBank BkmvtiMPFransaBank2 = new BkmvtiMPFransaBank();
		Map<String, BigDecimal> data = new HashMap<>();
		Map<String, String> ComptC = new HashMap<>();
		Map<String, String> ComptD = new HashMap<>();
		for (DayOperationMP op : dayOperationFransaBanksByNumTransaction) {
			data.put(op.getIdenficationCode(), op.getMontantSettlement().multiply(new BigDecimal(100)));
		}
		DayOperationMP op = dayOperationFransaBanksByNumTransaction
				.get(dayOperationFransaBanksByNumTransaction.size() - 1);
		try {
			if(Double.valueOf( fIPService.evaluateWithElementswithBigDecimal(mvk.getCodeSettlement(), data).floatValue())<=0) {
				throw new Exception("amount lower than expected");
			}
//					String codeBank = op.getCodeBank();
//					String mvbaccount = mvk.getAccount().substring(0, mvk.getAccount().length() - 3) + codeBank;
					BkmvtiMPFransaBank2 = TestAccountAndSigne( index2, indexPieceComptable, mvk,
							op);
			BkmvtiMPFransaBank2.setMontant(getAmountFormat(fIPService.evaluateWithElementswithBigDecimal(mvk.getCodeSettlement(), data).doubleValue()));
			if ("AGENCE_C".equals(mvk.getEntity())) {
				BkmvtiMPFransaBank2.setNumCompte(
						BkmvtiMPFransaBank2.getNumCompte().substring(0, BkmvtiMPFransaBank2.getNumCompte().length() - 4)
								+ op.getCodeAgence().substring(1, op.getCodeAgence().length()));
			} else if ("AGENCE_P".equals(mvk.getEntity())) {
				BkmvtiMPFransaBank2.setNumCompte(
						BkmvtiMPFransaBank2.getNumCompte().substring(0, BkmvtiMPFransaBank2.getNumCompte().length() - 4)
								+ op.getNumRIBEmetteur().substring(4, 8).length());
			}
			BkmvtiMPFransaBanks.add(BkmvtiMPFransaBank2);

			return BkmvtiMPFransaBanks;
		} catch (Exception e) {
			logger.error(e.getMessage());
			return BkmvtiMPFransaBanks;

		}
	}
	public List<BkmvtiVisa> TestSigneInter(  MvbkConf mvk,
												List<DayOperationInternational> dayOperationFransaBanksByNumTransaction,
												List<BkmvtiVisa> BkmvtiMPFransaBanks, int indexPieceComptable, int index2 ) {
		BkmvtiVisa BkmvtiMPFransaBank2 = new BkmvtiVisa();
		Map<String, BigDecimal> data = new HashMap<>();
		Map<String, String> ComptC = new HashMap<>();
		Map<String, String> ComptD = new HashMap<>();
		DayOperationInternational op = dayOperationFransaBanksByNumTransaction
				.get(dayOperationFransaBanksByNumTransaction.size() - 1);
		for (DayOperationInternational opp : dayOperationFransaBanksByNumTransaction) {
			data.put(opp.getIdenficationCode(),BigDecimal.valueOf(opp.getCompletedAmtSettlement()).multiply(new BigDecimal(100)));
			if (mvk.getCodeSettlement().contains(opp.getIdenficationCode())){
				op=opp;
			}
		}

		try {
//			if(Double.valueOf( fIPService.evaluateWithElementswithBigDecimal(mvk.getCodeSettlement(), data).floatValue())<=0) {
//				throw new Exception("amount lower than expected");
//			}
//					String codeBank = op.getCodeBank();
//					String mvbaccount = mvk.getAccount().substring(0, mvk.getAccount().length() - 3) + codeBank;
			BkmvtiMPFransaBank2 = TestAccountAndSigneInter( index2, indexPieceComptable, mvk,
					op);
			BkmvtiMPFransaBank2.setMontant(getAmountFormat(fIPService.evaluateWithElementswithBigDecimal(mvk.getCodeSettlement(), data).doubleValue()));
			if ("AGENCE_C".equals(mvk.getEntity())) {
				BkmvtiMPFransaBank2.setNumCompte(
						BkmvtiMPFransaBank2.getNumCompte().substring(0, BkmvtiMPFransaBank2.getNumCompte().length() - 4)
								+ op.getCodeAgence().substring(1, op.getCodeAgence().length()));
			} else if ("AGENCE_P".equals(mvk.getEntity())) {
				BkmvtiMPFransaBank2.setNumCompte(
						BkmvtiMPFransaBank2.getNumCompte().substring(0, BkmvtiMPFransaBank2.getNumCompte().length() - 4)
								+ op.getNumRIBPorteur().substring(4, 8).length());
			}
			BkmvtiMPFransaBanks.add(BkmvtiMPFransaBank2);

			return BkmvtiMPFransaBanks;
		} catch (Exception e) {
			logger.error(e.getMessage());
			return BkmvtiMPFransaBanks;

		}
	}

	public List<BkmvtiMPFransaBank> TestSigneUAP070(MvbkConf mvk,
												  TransactionEntity uap,
												  List<BkmvtiMPFransaBank> BkmvtiMPFransaBanks, int indexPieceComptable, int index2 ) {
		BkmvtiMPFransaBank BkmvtiMPFransaBank2 = new BkmvtiMPFransaBank();
		Map<String, BigDecimal> data = new HashMap<>();
		Map<String, String> ComptC = new HashMap<>();
		Map<String, String> ComptD = new HashMap<>();
			data.put("montantTransaction", uap.getMontantTransaction());

			data.put("montantInterchange", uap.getMontantInterchange());


		try {
			if(Double.valueOf( fIPService.evaluateWithElementswithBigDecimal(mvk.getCodeSettlement(), data).floatValue())<=0) {
				throw new Exception("amount lower than expected");
			}
//					String codeBank = op.getCodeBank();
//					String mvbaccount = mvk.getAccount().substring(0, mvk.getAccount().length() - 3) + codeBank;
			BkmvtiMPFransaBank2 = TestAccountAndSigneBKM( index2, indexPieceComptable, mvk,
					uap);
			BkmvtiMPFransaBank2.setMontant(getAmountFormat(fIPService.evaluateWithElementswithBigDecimal(mvk.getCodeSettlement(), data).doubleValue()));
			if ("AGENCE_C".equals(mvk.getEntity())) {
				BkmvtiMPFransaBank2.setNumCompte(
						BkmvtiMPFransaBank2.getNumCompte().substring(0, BkmvtiMPFransaBank2.getNumCompte().length() - 4)
								+ uap.getCodeAgence().substring(1, uap.getCodeAgence().length()));
			} else if ("AGENCE_P".equals(mvk.getEntity())) {
				BkmvtiMPFransaBank2.setNumCompte(
						BkmvtiMPFransaBank2.getNumCompte().substring(0, BkmvtiMPFransaBank2.getNumCompte().length() - 4)
								+ uap.getNumRIBEmetteur().substring(4, 8).length());
			}
			BkmvtiMPFransaBanks.add(BkmvtiMPFransaBank2);

			return BkmvtiMPFransaBanks;
		} catch (Exception e) {
			logger.error(e.getMessage());
			return BkmvtiMPFransaBanks;

		}
	}



	public void updateDays(int methode, int indexPieceComptable, List<DayOperationMP> days, MvbkConf mvb) {
		String lib = mvb.getTypeOp() + days.get(0).getTypeTransaction()+ String.format("%05d", indexPieceComptable);
		days.forEach(element -> {
			element.setPieceComptableBkm(lib);
		});
		dayOperationRepo.saveAll(days);
	}
	public void updateDaysInter(  List<DayOperationInternational> days, BkmvtiVisa mvb) {
 		days.forEach(element -> {
			element.setPieceComptable(mvb.getPieceComptable());
		});
		dayOperationInterRepo.saveAll(days);
	}


	public BkmvtiMPFransaBank TestAccountAndSigne( int index2, int indexPieceComptable, MvbkConf mvk, DayOperationMP op) {
		BkmvtiMPFransaBank BkmvtiMPFransaBank = new BkmvtiMPFransaBank();
		TestAccountLength(mvk, BkmvtiMPFransaBank, op);
		BkmvtiMPFransaBank.setCodeDevice("208");
		String name = ExecutorMobileThreads.name;
		if (name.length() > 10) {
			name = name.substring(0, 10);
		}

		BkmvtiMPFransaBank.setCodeUtilisateur(name + getSpace(10 - name.length()));
		BkmvtiMPFransaBank.setCodeService("0000");
		BkmvtiMPFransaBank.setSens(mvk.getSigne());
		BkmvtiMPFransaBank.setExonerationcommission("O");
		int lengthNumPiece = op.getNumAutorisation().length();
		BkmvtiMPFransaBank.setNumPiece(op.getNumAutorisation().substring(lengthNumPiece - 10, lengthNumPiece));
		BkmvtiMPFransaBank.setTauxChange("1" + getSpace(6));
		BkmvtiMPFransaBank.setCalculmouvementInteragence("N");
		BkmvtiMPFransaBank.setMouvementAgence("N");
		//// add new fileds //////
		int lengthNumAuth = op.getNumAutorisation().length();
		int lengthRefDossier = (op.getNumRefTransaction() + op.getDateTransaction()
				+ op.getNumAutorisation().substring(lengthNumAuth - 6, lengthNumAuth)).length();
		BkmvtiMPFransaBank.setRefDossier(op.getNumRefTransaction() + op.getDateTransaction()
				+ op.getNumAutorisation().substring(lengthNumAuth - 6, lengthNumAuth)
				+ getSpace(50 - lengthRefDossier));

		TestCodeAgence(  mvk, op, BkmvtiMPFransaBank);

		getTransactionDate(op.getDateTransaction(), BkmvtiMPFransaBank);

		int lengthReferanceLettrage = op.getNumAutorisation().length();

		BkmvtiMPFransaBank.setReferanceLettrage(op.getDateTransaction().substring(6, op.getDateTransaction().length())
				+ op.getNumAutorisation().substring(lengthReferanceLettrage - 6, lengthReferanceLettrage));
// 		if (mvk.getType().equals("1")) {
//			getAmount(op.getMontantSettlement(), BkmvtiMPFransaBank);
//		}

		BkmvtiMPFransaBank.setCodeDeviceOrigine("208");
		BkmvtiMPFransaBank.setIdentification(mvk.getIdentification());
		BkmvtiMPFransaBank.setNumRefTransactions(op.getNumRefTransaction());

		setSameData(  BkmvtiMPFransaBank, index2, indexPieceComptable, op, mvk);

		return BkmvtiMPFransaBank;

	}
	public BkmvtiVisa TestAccountAndSigneInter( int index2, int indexPieceComptable, MvbkConf mvk, DayOperationInternational op) {
		BkmvtiVisa BkmvtiMPFransaBank = new BkmvtiVisa();
		TestAccountLengthInter(mvk, BkmvtiMPFransaBank, op);
		String formule=mvk.getCodeSettlement();
		String codeDev=op.getCodeDevise();
		BkmvtiMPFransaBank.setCodeDevice(op.getCodeDevise());
		String name = ExecutorMobileThreads.name;

		if (name.length() > 10) {
			name = name.substring(0, 10);
		}

		BkmvtiMPFransaBank.setCodeUtilisateur(name + getSpace(10 - name.length()));
		BkmvtiMPFransaBank.setCodeService("0000");
		BkmvtiMPFransaBank.setSens(mvk.getSigne());
		BkmvtiMPFransaBank.setExonerationcommission("O");
		String referenceNumber = op.getReferenceNumber();
		int lengthNumPiece = referenceNumber.length();
		String numPiece;
		if (lengthNumPiece >= 10) {
			numPiece = referenceNumber.substring(lengthNumPiece - 10, lengthNumPiece);
		} else {
			// Pad with leading zeros if length < 10 (e.g., "12hh" becomes "00000012hh")
			numPiece = getZero(10 - lengthNumPiece) + referenceNumber;
		}
		BkmvtiMPFransaBank.setNumPiece(numPiece);
		BkmvtiMPFransaBank.setTauxChange("1" + getSpace(6));
		BkmvtiMPFransaBank.setCalculmouvementInteragence("N");
		BkmvtiMPFransaBank.setMouvementAgence("N");
		//// add new fileds //////
		int lengthNumAuth = op.getDe38().length();
		int lengthRefDossier = (op.getReferenceNumber() ).length();
		BkmvtiMPFransaBank.setRefDossier(op.getReferenceNumber()
				+ getSpace(50 - lengthRefDossier));

		TestCodeAgenceInter(  mvk, op, BkmvtiMPFransaBank);

		getTransactionDateInter(op.getTransactionDate(), BkmvtiMPFransaBank);

		int lengthReferanceLettrage = op.getDe38().length();

		BkmvtiMPFransaBank.setReferanceLettrage(op.getTransactionDate().substring(6, op.getTransactionDate().length())
				+ op.getDe38().substring(lengthReferanceLettrage - 6, lengthReferanceLettrage));
// 		if (mvk.getType().equals("1")) {
//			getAmount(op.getMontantSettlement(), BkmvtiMPFransaBank);
//		}

		BkmvtiMPFransaBank.setCodeDeviceOrigine("208");
		BkmvtiMPFransaBank.setIdentification(mvk.getIdentification());
		BkmvtiMPFransaBank.setNumRefTransactions(op.getTransactionIdentification());

		setSameDataInter(  BkmvtiMPFransaBank, index2, indexPieceComptable, op, mvk);

		return BkmvtiMPFransaBank;

	}
	public BkmvtiMPFransaBank TestAccountAndSigneBKM( int index2, int indexPieceComptable, MvbkConf mvk, TransactionEntity op) {
		BkmvtiMPFransaBank BkmvtiMPFransaBank = new BkmvtiMPFransaBank();
		TestAccountLengthBKM(mvk, BkmvtiMPFransaBank, op);
		BkmvtiMPFransaBank.setCodeDevice("208");
		String name = ExecutorMobileThreads.name;

		if (name.length() > 10) {
			name = name.substring(0, 10);
		}

		BkmvtiMPFransaBank.setCodeUtilisateur(name + getSpace(10 - name.length()));
		BkmvtiMPFransaBank.setCodeService("0000");
		BkmvtiMPFransaBank.setSens(mvk.getSigne());
		BkmvtiMPFransaBank.setExonerationcommission("O");
		int lengthNumPiece = op.getNumAutorisation().length();
		BkmvtiMPFransaBank.setNumPiece(op.getNumAutorisation().substring(lengthNumPiece - 10, lengthNumPiece));
		BkmvtiMPFransaBank.setTauxChange("1" + getSpace(6));
		BkmvtiMPFransaBank.setCalculmouvementInteragence("N");
		BkmvtiMPFransaBank.setMouvementAgence("N");
		//// add new fileds //////
		int lengthNumAuth = op.getNumAutorisation().length();
		int lengthRefDossier = (op.getNumRefTransaction() + op.getDateTransaction()
				+ op.getNumAutorisation().substring(lengthNumAuth - 6, lengthNumAuth)).length();
		BkmvtiMPFransaBank.setRefDossier(op.getNumRefTransaction() + op.getDateTransaction()
				+ op.getNumAutorisation().substring(lengthNumAuth - 6, lengthNumAuth)
				+ getSpace(50 - lengthRefDossier));

		TestCodeAgenceBKM(  mvk, op, BkmvtiMPFransaBank);

		getTransactionDate(op.getDateTransaction(), BkmvtiMPFransaBank);

		int lengthReferanceLettrage = op.getNumAutorisation().length();

		BkmvtiMPFransaBank.setReferanceLettrage(op.getDateTransaction().substring(6, op.getDateTransaction().length())
				+ op.getNumAutorisation().substring(lengthReferanceLettrage - 6, lengthReferanceLettrage));

		BkmvtiMPFransaBank.setCodeDeviceOrigine("208");
		BkmvtiMPFransaBank.setIdentification(mvk.getIdentification());
		BkmvtiMPFransaBank.setNumRefTransactions(op.getNumRefTransaction());
		setSameDataBKM(  BkmvtiMPFransaBank, index2, indexPieceComptable, op, mvk);

		return BkmvtiMPFransaBank;

	}


	public BkmvtiMPFransaBank setSameData(  BkmvtiMPFransaBank BkmvtiMPFransaBank, int index2,
										int indexPieceComptable, DayOperationMP op, MvbkConf mvk) {
		String lib = "";
		BkmvtiMPFransaBank.setCodeOperation(mvk.getCodeOperation());
		lib = mvk.getLibelle_operation();
		BkmvtiMPFransaBank.setLibelle(lib + getSpace(40 - lib.length()));
		if (mvk.getLibGenerique() != null && mvk.getLibGenerique().trim() != "") {
			String libgenerique = mvk.getLibGenerique();
			libgenerique = libgenerique.replaceAll("dateTransation", op.getDateTransaction());
			String aut = op.getNumAutorisation();
			int lengthAuth = aut.length();
			libgenerique = libgenerique.replaceAll("numAutorisation", aut.substring(lengthAuth - 6));
			BkmvtiMPFransaBank.setLibelle(libgenerique + getSpace(40 - lib.length()));
		}
				BkmvtiMPFransaBank
						.setPieceComptable( mvk.getTypeOp()+op.getTypeTransaction()+ String.format("%05d", indexPieceComptable));
				BkmvtiMPFransaBank.setNumEvenement(String.format("%06d", indexPieceComptable));
		return BkmvtiMPFransaBank;
	}
	public BkmvtiVisa setSameDataInter(  BkmvtiVisa BkmvtiMPFransaBank, int index2,
										int indexPieceComptable, DayOperationInternational op, MvbkConf mvk) {
		String lib = "";
		BkmvtiMPFransaBank.setCodeOperation(mvk.getCodeOperation());
		lib = mvk.getLibelle_operation();
		BkmvtiMPFransaBank.setLibelle(lib + getSpace(40 - lib.length()));
		if (mvk.getLibGenerique() != null && mvk.getLibGenerique().trim() != "") {
			String libgenerique = mvk.getLibGenerique();
			libgenerique = libgenerique.replaceAll("dateTransation", op.getTransactionDate());
			String aut = op.getDe38();
			int lengthAuth = aut.length();
			libgenerique = libgenerique.replaceAll("numAutorisation", aut.substring(lengthAuth - 6));
			BkmvtiMPFransaBank.setLibelle(libgenerique + getSpace(40 - lib.length()));
		}
				BkmvtiMPFransaBank
						.setPieceComptable( mvk.getTypeOp()+mvk.getTypeOp()+ String.format("%05d", indexPieceComptable));
				BkmvtiMPFransaBank.setNumEvenement(String.format("%06d", indexPieceComptable));
		return BkmvtiMPFransaBank;
	}

	public BkmvtiMPFransaBank setSameDataBKM(  BkmvtiMPFransaBank BkmvtiMPFransaBank, int index2,
										  int indexPieceComptable, TransactionEntity op, MvbkConf mvk) {
		String lib = "";
		BkmvtiMPFransaBank.setCodeOperation(mvk.getCodeOperation());
		lib = mvk.getLibelle_operation();
		BkmvtiMPFransaBank.setLibelle(lib + getSpace(40 - lib.length()));
		if (mvk.getLibGenerique() != null && mvk.getLibGenerique().trim() != "") {
			String libgenerique = mvk.getLibGenerique();
			libgenerique = libgenerique.replaceAll("dateTransation", op.getDateTransaction());
			String aut = op.getNumAutorisation();
			int lengthAuth = aut.length();
			libgenerique = libgenerique.replaceAll("numAutorisation", aut.substring(lengthAuth - 6));
			BkmvtiMPFransaBank.setLibelle(libgenerique + getSpace(40 - lib.length()));
		}
		BkmvtiMPFransaBank
				.setPieceComptable( mvk.getTypeOp()+op.getTypeTransaction()+ String.format("%05d", indexPieceComptable));
		BkmvtiMPFransaBank.setNumEvenement(String.format("%06d", indexPieceComptable));
		return BkmvtiMPFransaBank;
	}


	public BkmvtiVisa TestAccountLengthInter(MvbkConf mvk,
													 BkmvtiVisa BkmvtiMPFransaBank, DayOperationInternational op) {
		String accountConfig= mvk.getAccount();
		String account= "" ;
		if("PORTEUR".equals(accountConfig)){
			account=op.getNumRIBPorteur();}
		else{account=accountConfig;

		}
		int lengAccount=account.length();
		if (lengAccount > 18) {
			String credit = account.substring(8, 18);
			String chapitreCompta = account.substring(8, 14);
			BkmvtiMPFransaBank.setChapitreComptable(chapitreCompta + getZero(6 - chapitreCompta.length()));
			BkmvtiMPFransaBank.setNumCompte(credit);
			BkmvtiMPFransaBank.setCleControleCompte(getSpace(2));
			String codeDes = account.substring(3, 8);
			BkmvtiMPFransaBank.setAgenceDestinatrice(codeDes);
			BkmvtiMPFransaBank.setAgenceEmettrice(codeDes);
			BkmvtiMPFransaBank.setCodeAgenceSaisie(codeDes);
			String codeId = account.substring(3, 8);
			BkmvtiMPFransaBank.setCodeID("S" + codeId);
		}

		else if (lengAccount >= 6 && lengAccount < 10) {
			BkmvtiMPFransaBank.setNumCompte(account + getZero(10 - (account.length())));
			BkmvtiMPFransaBank.setChapitreComptable(account.substring(0, 6) + getZero(6 - account.length()));

			BkmvtiMPFransaBank.setCleControleCompte(getSpace(2));
			BkmvtiMPFransaBank.setAgenceDestinatrice(op.getCodeAgence());
			BkmvtiMPFransaBank.setAgenceEmettrice(op.getCodeAgence());
			BkmvtiMPFransaBank.setCodeAgenceSaisie(op.getCodeAgence());
			BkmvtiMPFransaBank.setCodeID("S" + op.getCodeAgence());
		}

		else if (lengAccount < 6) {
			BkmvtiMPFransaBank.setNumCompte(account + getZero(10 - (account.length())));
			BkmvtiMPFransaBank.setChapitreComptable(account + getZero(6 - account.length()));
			BkmvtiMPFransaBank.setCleControleCompte(getSpace(2));

		}

		else if (lengAccount == 10) {
			BkmvtiMPFransaBank.setNumCompte(account);
			BkmvtiMPFransaBank.setChapitreComptable(account.substring(0, 6));
			BkmvtiMPFransaBank.setCleControleCompte(getSpace(2));
			BkmvtiMPFransaBank.setAgenceDestinatrice(op.getCodeAgence());
			BkmvtiMPFransaBank.setAgenceEmettrice(op.getCodeAgence());
			BkmvtiMPFransaBank.setCodeAgenceSaisie(op.getCodeAgence());
			BkmvtiMPFransaBank.setCodeID("S" + op.getCodeAgence());
		}

		else {
			BkmvtiMPFransaBank.setNumCompte(account.substring(0, 10));
			BkmvtiMPFransaBank.setChapitreComptable(account.substring(0, 6));
			BkmvtiMPFransaBank.setCleControleCompte(getSpace(2));
			BkmvtiMPFransaBank.setAgenceEmettrice(op.getCodeAgence());
			BkmvtiMPFransaBank.setCodeAgenceSaisie(op.getCodeAgence());
			BkmvtiMPFransaBank.setCodeID("S" + op.getCodeAgence());
		}

		return BkmvtiMPFransaBank;
	}

	public BkmvtiMPFransaBank TestAccountLength(MvbkConf mvk,
											  BkmvtiMPFransaBank BkmvtiMPFransaBank, DayOperationMP op) {
	    String accountConfig= mvk.getAccount();
	    String account= "" ;
		if("PorteurB".equals(accountConfig)){
			account=op.getNumRIBAcquereur();}
		else if("PorteurD".equals(accountConfig)){
			account=op.getNumRIBEmetteur();}
		else if("COMMERCANTB".equals(accountConfig)){
			account=op.getNumRIBAcquereur();}
		else if("COMMERCANTD".equals(accountConfig)){
			account=op.getNumRIBEmetteur();}
		else{account=accountConfig;

		}
		int lengAccount=account.length();
		if (lengAccount > 18) {
			String credit = account.substring(8, 18);
			String chapitreCompta = account.substring(8, 14);
			BkmvtiMPFransaBank.setChapitreComptable(chapitreCompta + getZero(6 - chapitreCompta.length()));
			BkmvtiMPFransaBank.setNumCompte(credit);
			BkmvtiMPFransaBank.setCleControleCompte(getSpace(2));
			String codeDes = account.substring(3, 8);
			BkmvtiMPFransaBank.setAgenceDestinatrice(codeDes);
			BkmvtiMPFransaBank.setAgenceEmettrice(codeDes);
			BkmvtiMPFransaBank.setCodeAgenceSaisie(codeDes);
			String codeId = account.substring(3, 8);
			BkmvtiMPFransaBank.setCodeID("S" + codeId);
		}

		else if (lengAccount >= 6 && lengAccount < 10) {
			BkmvtiMPFransaBank.setNumCompte(account + getZero(10 - (account.length())));
			BkmvtiMPFransaBank.setChapitreComptable(account.substring(0, 6) + getZero(6 - account.length()));

			BkmvtiMPFransaBank.setCleControleCompte(getSpace(2));
			BkmvtiMPFransaBank.setAgenceDestinatrice(op.getCodeAgence());
			BkmvtiMPFransaBank.setAgenceEmettrice(op.getCodeAgence());
			BkmvtiMPFransaBank.setCodeAgenceSaisie(op.getCodeAgence());
			BkmvtiMPFransaBank.setCodeID("S" + op.getCodeAgence());
		}

		else if (lengAccount < 6) {
			BkmvtiMPFransaBank.setNumCompte(account + getZero(10 - (account.length())));
			BkmvtiMPFransaBank.setChapitreComptable(account + getZero(6 - account.length()));
			BkmvtiMPFransaBank.setCleControleCompte(getSpace(2));

		}

		else if (lengAccount == 10) {
			BkmvtiMPFransaBank.setNumCompte(account);
			BkmvtiMPFransaBank.setChapitreComptable(account.substring(0, 6));
			BkmvtiMPFransaBank.setCleControleCompte(getSpace(2));
			BkmvtiMPFransaBank.setAgenceDestinatrice(op.getCodeAgence());
			BkmvtiMPFransaBank.setAgenceEmettrice(op.getCodeAgence());
			BkmvtiMPFransaBank.setCodeAgenceSaisie(op.getCodeAgence());
			BkmvtiMPFransaBank.setCodeID("S" + op.getCodeAgence());
		}

		else {
			BkmvtiMPFransaBank.setNumCompte(account.substring(0, 10));
			BkmvtiMPFransaBank.setChapitreComptable(account.substring(0, 6));
			BkmvtiMPFransaBank.setCleControleCompte(getSpace(2));
			BkmvtiMPFransaBank.setAgenceEmettrice(op.getCodeAgence());
			BkmvtiMPFransaBank.setCodeAgenceSaisie(op.getCodeAgence());
			BkmvtiMPFransaBank.setCodeID("S" + op.getCodeAgence());
		}

		return BkmvtiMPFransaBank;
	}
	public BkmvtiMPFransaBank TestAccountLengthBKM(MvbkConf mvk,
											  BkmvtiMPFransaBank BkmvtiMPFransaBank, TransactionEntity op) {
		String accountConfig= mvk.getAccount();
		String account= "" ;
		if("PorteurB".equals(accountConfig)){account=op.getNumRIBAcquereur();}
		else if("PorteurD".equals(accountConfig)){account=op.getNumRIBEmetteur();}
		else if("COMMERCANTB".equals(accountConfig)){account=op.getNumRIBAcquereur();}
		else if("COMMERCANTD".equals(accountConfig)){account=op.getNumRIBEmetteur();}
		else{account=accountConfig;

		}
		int lengAccount=account.length();
		if (lengAccount > 18) {
			String credit = account.substring(8, 18);
			String chapitreCompta = account.substring(8, 14);
			BkmvtiMPFransaBank.setChapitreComptable(chapitreCompta + getZero(6 - chapitreCompta.length()));
			BkmvtiMPFransaBank.setNumCompte(credit);
			BkmvtiMPFransaBank.setCleControleCompte(getSpace(2));
			String codeDes = account.substring(3, 8);
			BkmvtiMPFransaBank.setAgenceDestinatrice(codeDes);
			BkmvtiMPFransaBank.setAgenceEmettrice(codeDes);
			BkmvtiMPFransaBank.setCodeAgenceSaisie(codeDes);
			String codeId = account.substring(3, 8);
			BkmvtiMPFransaBank.setCodeID("S" + codeId);
		}

		else if (lengAccount >= 6 && lengAccount < 10) {
			BkmvtiMPFransaBank.setNumCompte(account + getZero(10 - (account.length())));
			BkmvtiMPFransaBank.setChapitreComptable(account.substring(0, 6) + getZero(6 - account.length()));

			BkmvtiMPFransaBank.setCleControleCompte(getSpace(2));
			BkmvtiMPFransaBank.setAgenceDestinatrice(op.getCodeAgence());
			BkmvtiMPFransaBank.setAgenceEmettrice(op.getCodeAgence());
			BkmvtiMPFransaBank.setCodeAgenceSaisie(op.getCodeAgence());
			BkmvtiMPFransaBank.setCodeID("S" + op.getCodeAgence());
		}

		else if (lengAccount < 6) {
			BkmvtiMPFransaBank.setNumCompte(account + getZero(10 - (account.length())));
			BkmvtiMPFransaBank.setChapitreComptable(account + getZero(6 - account.length()));
			BkmvtiMPFransaBank.setCleControleCompte(getSpace(2));

		}

		else if (lengAccount == 10) {
			BkmvtiMPFransaBank.setNumCompte(account);
			BkmvtiMPFransaBank.setChapitreComptable(account.substring(0, 6));
			BkmvtiMPFransaBank.setCleControleCompte(getSpace(2));
			BkmvtiMPFransaBank.setAgenceDestinatrice(op.getCodeAgence());
			BkmvtiMPFransaBank.setAgenceEmettrice(op.getCodeAgence());
			BkmvtiMPFransaBank.setCodeAgenceSaisie(op.getCodeAgence());
			BkmvtiMPFransaBank.setCodeID("S" + op.getCodeAgence());
		}

		else {
			BkmvtiMPFransaBank.setNumCompte(account.substring(0, 10));
			BkmvtiMPFransaBank.setChapitreComptable(account.substring(0, 6));
			BkmvtiMPFransaBank.setCleControleCompte(getSpace(2));
			BkmvtiMPFransaBank.setAgenceEmettrice(op.getCodeAgence());
			BkmvtiMPFransaBank.setCodeAgenceSaisie(op.getCodeAgence());
			BkmvtiMPFransaBank.setCodeID("S" + op.getCodeAgence());
		}

		return BkmvtiMPFransaBank;
	}

	public BkmvtiVisa TestCodeAgenceInter( MvbkConf mvk,
											  DayOperationInternational op, BkmvtiVisa BkmvtiMPFransaBank) {

		if ("P".equals(mvk.getCodeAgence())) {

			BkmvtiMPFransaBank.setAgence(op.getNumRIBPorteur().substring(3, 8));
			BkmvtiMPFransaBank.setAgenceEmettrice(BkmvtiMPFransaBank.getAgence());
			BkmvtiMPFransaBank.setAgenceDestinatrice(BkmvtiMPFransaBank.getAgence());
			BkmvtiMPFransaBank.setCodeAgenceSaisie(BkmvtiMPFransaBank.getAgence());
			BkmvtiMPFransaBank.setCodeID("S" + BkmvtiMPFransaBank.getAgence());
		}  else {

			BkmvtiMPFransaBank.setAgence(mvk.getCodeAgence());
			BkmvtiMPFransaBank.setAgenceEmettrice(BkmvtiMPFransaBank.getAgence());
			BkmvtiMPFransaBank.setAgenceDestinatrice(BkmvtiMPFransaBank.getAgence());
			BkmvtiMPFransaBank.setCodeAgenceSaisie(BkmvtiMPFransaBank.getAgence());
			BkmvtiMPFransaBank.setCodeID("S" + BkmvtiMPFransaBank.getAgence());
		}

		return BkmvtiMPFransaBank;
	}


	public BkmvtiMPFransaBank TestCodeAgence( MvbkConf mvk,
										   DayOperationMP op, BkmvtiMPFransaBank BkmvtiMPFransaBank) {

		if ("P".equals(mvk.getCodeAgence())) {

			BkmvtiMPFransaBank.setAgence(op.getNumRIBEmetteur().substring(3, 8));
			BkmvtiMPFransaBank.setAgenceEmettrice(BkmvtiMPFransaBank.getAgence());
			BkmvtiMPFransaBank.setAgenceDestinatrice(BkmvtiMPFransaBank.getAgence());
			BkmvtiMPFransaBank.setCodeAgenceSaisie(BkmvtiMPFransaBank.getAgence());
			BkmvtiMPFransaBank.setCodeID("S" + BkmvtiMPFransaBank.getAgence());
		} else if ("C".equals(mvk.getCodeAgence())) {

			BkmvtiMPFransaBank.setAgence(op.getCodeAgence());
			BkmvtiMPFransaBank.setAgenceEmettrice(BkmvtiMPFransaBank.getAgence());
			BkmvtiMPFransaBank.setAgenceDestinatrice(BkmvtiMPFransaBank.getAgence());
			BkmvtiMPFransaBank.setCodeAgenceSaisie(BkmvtiMPFransaBank.getAgence());
			BkmvtiMPFransaBank.setCodeID("S" + BkmvtiMPFransaBank.getAgence());
		} 		if ("B".equals(mvk.getCodeAgence())) {

			BkmvtiMPFransaBank.setAgence(op.getNumRIBAcquereur().substring(3, 8));
			BkmvtiMPFransaBank.setAgenceEmettrice(BkmvtiMPFransaBank.getAgence());
			BkmvtiMPFransaBank.setAgenceDestinatrice(BkmvtiMPFransaBank.getAgence());
			BkmvtiMPFransaBank.setCodeAgenceSaisie(BkmvtiMPFransaBank.getAgence());
			BkmvtiMPFransaBank.setCodeID("S" + BkmvtiMPFransaBank.getAgence());
		} else if ("D".equals(mvk.getCodeAgence())) {

			BkmvtiMPFransaBank.setAgence(op.getNumRIBEmetteur().substring(3, 8));
			BkmvtiMPFransaBank.setAgenceEmettrice(BkmvtiMPFransaBank.getAgence());
			BkmvtiMPFransaBank.setAgenceDestinatrice(BkmvtiMPFransaBank.getAgence());
			BkmvtiMPFransaBank.setCodeAgenceSaisie(BkmvtiMPFransaBank.getAgence());
			BkmvtiMPFransaBank.setCodeID("S" + BkmvtiMPFransaBank.getAgence());
		} else {

			BkmvtiMPFransaBank.setAgence(mvk.getCodeAgence());
			BkmvtiMPFransaBank.setAgenceEmettrice(BkmvtiMPFransaBank.getAgence());
			BkmvtiMPFransaBank.setAgenceDestinatrice(BkmvtiMPFransaBank.getAgence());
			BkmvtiMPFransaBank.setCodeAgenceSaisie(BkmvtiMPFransaBank.getAgence());
			BkmvtiMPFransaBank.setCodeID("S" + BkmvtiMPFransaBank.getAgence());
		}

		return BkmvtiMPFransaBank;
	}
	public BkmvtiMPFransaBank TestCodeAgenceBKM( MvbkConf mvk,
										   TransactionEntity op, BkmvtiMPFransaBank BkmvtiMPFransaBank) {

		if ("P".equals(mvk.getCodeAgence())) {

			BkmvtiMPFransaBank.setAgence(op.getNumRIBEmetteur().substring(3, 8));
			BkmvtiMPFransaBank.setAgenceEmettrice(BkmvtiMPFransaBank.getAgence());
			BkmvtiMPFransaBank.setAgenceDestinatrice(BkmvtiMPFransaBank.getAgence());
			BkmvtiMPFransaBank.setCodeAgenceSaisie(BkmvtiMPFransaBank.getAgence());
			BkmvtiMPFransaBank.setCodeID("S" + BkmvtiMPFransaBank.getAgence());
		} else if ("C".equals(mvk.getCodeAgence())) {

			BkmvtiMPFransaBank.setAgence(op.getCodeAgence());
			BkmvtiMPFransaBank.setAgenceEmettrice(BkmvtiMPFransaBank.getAgence());
			BkmvtiMPFransaBank.setAgenceDestinatrice(BkmvtiMPFransaBank.getAgence());
			BkmvtiMPFransaBank.setCodeAgenceSaisie(BkmvtiMPFransaBank.getAgence());
			BkmvtiMPFransaBank.setCodeID("S" + BkmvtiMPFransaBank.getAgence());
		} 		if ("B".equals(mvk.getCodeAgence())) {

			BkmvtiMPFransaBank.setAgence(op.getNumRIBAcquereur().substring(3, 8));
			BkmvtiMPFransaBank.setAgenceEmettrice(BkmvtiMPFransaBank.getAgence());
			BkmvtiMPFransaBank.setAgenceDestinatrice(BkmvtiMPFransaBank.getAgence());
			BkmvtiMPFransaBank.setCodeAgenceSaisie(BkmvtiMPFransaBank.getAgence());
			BkmvtiMPFransaBank.setCodeID("S" + BkmvtiMPFransaBank.getAgence());
		} else if ("D".equals(mvk.getCodeAgence())) {

			BkmvtiMPFransaBank.setAgence(op.getCodeAgence());
			BkmvtiMPFransaBank.setAgenceEmettrice(BkmvtiMPFransaBank.getAgence());
			BkmvtiMPFransaBank.setAgenceDestinatrice(BkmvtiMPFransaBank.getAgence());
			BkmvtiMPFransaBank.setCodeAgenceSaisie(BkmvtiMPFransaBank.getAgence());
			BkmvtiMPFransaBank.setCodeID("S" + BkmvtiMPFransaBank.getAgence());
		} else {

			BkmvtiMPFransaBank.setAgence(mvk.getCodeAgence());
			BkmvtiMPFransaBank.setAgenceEmettrice(BkmvtiMPFransaBank.getAgence());
			BkmvtiMPFransaBank.setAgenceDestinatrice(BkmvtiMPFransaBank.getAgence());
			BkmvtiMPFransaBank.setCodeAgenceSaisie(BkmvtiMPFransaBank.getAgence());
			BkmvtiMPFransaBank.setCodeID("S" + BkmvtiMPFransaBank.getAgence());
		}

		return BkmvtiMPFransaBank;
	}
	public BkmvtiMPFransaBank getTransactionDate(String TransactionDate, BkmvtiMPFransaBank BkmvtiMPFransaBank) {
		String year = TransactionDate.substring(0, 4);
		String month = TransactionDate.substring(4, 6);
		String dayy = TransactionDate.substring(6);
		// BkmvtiMPFransaBank.setDateComptable(dayy + "/" + month + "/" + year);

		LocalDate date = LocalDate.now();
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
		BkmvtiMPFransaBank.setDateComptable(date.format(formatter));

		BkmvtiMPFransaBank.setDateValeur(dayy + "/" + month + "/" + year);
		return BkmvtiMPFransaBank;

	}

	public BkmvtiVisa getTransactionDateInter(String TransactionDate, BkmvtiVisa BkmvtiMPFransaBank) {
		String year = TransactionDate.substring(0, 4);
		String month = TransactionDate.substring(4, 6);
		String dayy = TransactionDate.substring(6);
		// BkmvtiMPFransaBank.setDateComptable(dayy + "/" + month + "/" + year);

		LocalDate date = LocalDate.now();
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
		BkmvtiMPFransaBank.setDateComptable(date.format(formatter));

		BkmvtiMPFransaBank.setDateValeur(dayy + "/" + month + "/" + year);
		return BkmvtiMPFransaBank;

	}

	@PostMapping("/processComprehensiveBankSettlement")
	public String processComprehensiveBankSettlement() throws Exception {
		logger.info("Starting comprehensive bank settlement processing");
		
		StringBuilder resultLog = new StringBuilder();
		resultLog.append("=== Comprehensive Bank Settlement Processing Results ===\n");
		
		try {
			// Step 1: Call generateBankSettCode()
			logger.info("Step 1: Executing generateBankSettCode()");
			resultLog.append("Step 1: Processing standard bank settlement codes...\n");
			
			String step1Result = generateBankSettCode();
			resultLog.append("Step 1 Result: ").append(step1Result).append("\n");
			logger.info("Step 1 completed successfully: {}", step1Result);
			
			// Step 2: Call generateBankSettCodeUAP070() with each type
			String[] uap070Types = {"DAYOPMP","UAP070IN", "UAP070OUT", "UAP070INREG", "UAP070OUTREG"};
			
			for (String type : uap070Types) {
				try {
					logger.info("Step 2.{}: Executing generateBankSettCodeUAP070() with type: {}", 
							java.util.Arrays.asList(uap070Types).indexOf(type) + 1, type);
					resultLog.append("Step 2.").append(java.util.Arrays.asList(uap070Types).indexOf(type) + 1)
							.append(": Processing UAP070 type: ").append(type).append("...\n");
					
					String stepResult = generateBankSettCodeUAP070(type,null);
					resultLog.append("Step 2.").append(java.util.Arrays.asList(uap070Types).indexOf(type) + 1)
							.append(" Result: ").append(stepResult).append("\n");
					logger.info("Step 2.{} completed successfully for type {}: {}", 
							java.util.Arrays.asList(uap070Types).indexOf(type) + 1, type, stepResult);
					
				} catch (Exception e) {
					String errorMsg = "Error processing UAP070 type " + type + ": " + e.getMessage();
					logger.error(errorMsg, e);
					resultLog.append("Step 2.").append(java.util.Arrays.asList(uap070Types).indexOf(type) + 1)
							.append(" Error: ").append(errorMsg).append("\n");
					// Continue processing other types even if one fails
				}
			}
			
			resultLog.append("=== Comprehensive Bank Settlement Processing Completed ===\n");
			logger.info("Comprehensive bank settlement processing completed successfully");
			
			return resultLog.toString();
			
		} catch (Exception e) {
			String errorMsg = "Critical error during comprehensive bank settlement processing: " + e.getMessage();
			logger.error(errorMsg, e);
			resultLog.append("CRITICAL ERROR: ").append(errorMsg).append("\n");
			throw new Exception(errorMsg, e);
		}
	}

}