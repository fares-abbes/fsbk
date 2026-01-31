package com.mss.backOffice.controller;

import static com.mss.backOffice.controller.ExecutorThreadFransaBank.daysOperationsAll;
import static com.mss.backOffice.controller.ExecutorThreadFransaBank.tpList;
import static com.mss.backOffice.controller.ExecutorThreadFransaBank.stopThreads;
import com.google.common.base.Throwables;
import com.mss.backOffice.enumType.ChargebackStatus;
import com.mss.backOffice.services.DayOperationCardSequenceService;
import com.mss.unified.entities.*;
import com.mss.unified.repositories.*;
import com.sleepycat.je.rep.impl.networkRestore.Protocol.FileReq;
import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

import org.apache.commons.beanutils.PropertyUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

public strictfp class DayOpFransaBankThread implements Runnable {
	public static List<Integer> Test = new ArrayList<Integer>();

	@Autowired
	FileRepository fileRepository;
	@Autowired
	SettelementFransaBankRepository settlementRepo;
	@Autowired
	CommissionFransaBankRepository commissionFransaBank;
	@Autowired
	DayOperationFransaBankRepository operationRepo;
	@Autowired
	FILECONTENTRepository tp_detail;
	@Autowired
	TvaCommissionFransaBankRepository tvaRepo;
	@Autowired
	BatchesFFCRepository batchRepo;
	@Autowired
	CommissionAchatFransaBankRepository commissionAchatFransaBankRepository;
	@Autowired
	CommissionFransabankOnUsRepository commissionFransabankOnUsRepository;
	@Autowired
	DayOperationCardSequenceService dayService;
	@Autowired
	PourcentageCommFSBKRepository pourcentageCommFSBKRepository;
	@Autowired
	CommissionFransaBankInternetRepository commissionFransaBankInternetRepository;
	@Autowired
	MargeCommissionRepository margeCommissionRepository;
	@Autowired
	BankFransaRepository bankFransaRepository;

	private TvaCommissionFransaBank tvaCommissionFransaBank;

	private List<String> codeBanks;
	private List<String> codeBanksInternet;
	private List<AtmTerminal> atmList;
	private List<Account> accountList;
	float fff = 100;
	public String paramDay;
	private String fileDate;
	private MerchantRepository mr;
	public static boolean allowEdit;
	public static int index1 = 0;
	public static int index2 = 0;
	private static final Logger logger = LoggerFactory.getLogger(DayOpFransaBankThread.class);
	public static Integer eve1 = 0;

	public int getEveIndex1() {
		synchronized (eve1) {
			eve1 = eve1 + 1;
		}

		return eve1;
	}

	public DayOpFransaBankThread(MerchantRepository merchantRepository,
			CommissionFransaBankRepository commissionFransaBank, SettelementFransaBankRepository settlementRepo,
			DayOperationFransaBankRepository operationRepo, FILECONTENTRepository tp_detail,

			String paramDay, String fileDate, FileRepository fileRepository, TvaCommissionFransaBankRepository tvaRepo,
			BatchesFFCRepository batchRepo, CommissionAchatFransaBankRepository commissionAchatFransaBankRepository,
			CommissionFransabankOnUsRepository commissionFransabankOnUsRepository,
			PourcentageCommFSBKRepository pourcentageCommFSBKRepository,
			CommissionFransaBankInternetRepository commissionFransaBankInternetRepository,
			MargeCommissionRepository margeCommissionRepository, BankFransaRepository bankFransaRepository,
			List<AtmTerminal> atmList, List<Account> merchant) {
		super();
		this.allowEdit = true;

		this.settlementRepo = settlementRepo;
		this.operationRepo = operationRepo;
		this.tp_detail = tp_detail;
		this.paramDay = paramDay;
		this.fileDate = fileDate;
		this.fileRepository = fileRepository;

		this.commissionFransaBank = commissionFransaBank;
		this.tvaRepo = tvaRepo;
		this.batchRepo = batchRepo;
		this.commissionAchatFransaBankRepository = commissionAchatFransaBankRepository;
		this.commissionFransabankOnUsRepository = commissionFransabankOnUsRepository;
		this.pourcentageCommFSBKRepository = pourcentageCommFSBKRepository;
		this.commissionFransaBankInternetRepository = commissionFransaBankInternetRepository;
		this.margeCommissionRepository = margeCommissionRepository;
		this.bankFransaRepository = bankFransaRepository;
		tvaCommissionFransaBank = tvaRepo.findTva();
		this.atmList = atmList;
		this.accountList = merchant;
		codeBanks = settlementRepo.getAutreCodeBank();
		this.mr = merchantRepository;
		List<CommissionAchatInternetFB> comms = commissionFransaBankInternetRepository.findAll();
		HashSet<String> x = new HashSet<>();
		for (CommissionAchatInternetFB com : comms) {
			if (com.getBankIssuer() != null) {
				x.add(com.getBankIssuer());
			}
		}
		codeBanksInternet = new ArrayList<>(x);

	}
	/////////// *********************** CONSULTATION SOLDE///////////
	/////////// ***********************************////////////

	private void catchError(Exception e, int threadNB) {
		e.printStackTrace();

		String stackTrace = Throwables.getStackTraceAsString(e);
		logger.info("Exception");
		logger.info(stackTrace);

		if (stackTrace.length() > 4000) {
			stackTrace = stackTrace.substring(0, 3999);

		}

		batchRepo.updateFinishBatch("TH" + threadNB, 2, new Date());
		stopSavingData();
		batchRepo.updateStatusAndErrorBatch("Execute", 2, e.getMessage() == null ? e.toString() : e.getMessage(),
				new Date(), stackTrace);
	}

	//////// ******************************* CASH OUT
	//////// ***********************/////////////////////////////////////////////

	public void chargebackonUSMA(String fileDate, int threadNB) {

		try {
			int index = 0;
			FileHeader summary = new FileHeader();
			int indexPieceComptable = getEveIndex1();

			if (fileRepository.findByfileDate(fileDate).isPresent()) {
				index = index + 1;
				summary = fileRepository.findByfileDate(fileDate).get();
				String destinationBank = summary.getDestinationBankIdentification();
				List<FileContent> dayOperation = settlementRepo.chargebackOnUsMA(	String.valueOf(summary.getId()));
				List<DayOperationFransaBank> dayOperations = new ArrayList<DayOperationFransaBank>();
				List<FileContent> contents = new ArrayList<FileContent>();

				for (FileContent element : dayOperation) {
					if (ExecutorThreadFransaBank.stopThreads) {
						return;
					}
					if (element.getTransactionOrigine().equals("040")) {
						DayOperationFransaBank G206 = new DayOperationFransaBank();
						dayopCreation(G206, element, "G206");
						float montantTransaction = Float.parseFloat(element.getMontantTransaction()) / fff;

						G206.setMontantSettlement(montantTransaction);
						G206.setPieceComptable("DB" + "161" + String.format("%06d", indexPieceComptable));
						dayOperations.add(G206);
						element.setChargebackStatus(ChargebackStatus.DONE.getCode());

					}else  {
						DayOperationFransaBank G207= new DayOperationFransaBank();

						dayopCreation(G207, element, "G207");
						float montantTransaction = Float.parseFloat(element.getMontantTransaction()) / fff;
						G207.setMontantSettlement(montantTransaction);
						G207.setPieceComptable("PA" + "171" + String.format("%06d", indexPieceComptable));
						dayOperations.add(G207);

						element.setChargebackStatus(ChargebackStatus.DONE.getCode());

					}
					contents.add(element);


				}
				tpList.addAll(contents);

				daysOperationsAll.addAll(dayOperations);

				logger.info("chargebackonUS =>{}", dayOperations.size());

				batchRepo.updateFinishBatch("TH" + threadNB, 1, new Date());
			}

		}	catch (Exception e) {
						catchError(e, threadNB);

					}
	}
		public void chargebackonUSAA(String fileDate, int threadNB) {

		try {
			int index = 0;
			FileHeader summary = new FileHeader();
			int indexPieceComptable = getEveIndex1();

			if (fileRepository.findByfileDate(fileDate).isPresent()) {
				index = index + 1;
				summary = fileRepository.findByfileDate(fileDate).get();
				String destinationBank = summary.getDestinationBankIdentification();
				List<FileContent> dayOperation = settlementRepo.chargebackOnUsAA(	String.valueOf(summary.getId()));
				List<DayOperationFransaBank> dayOperations = new ArrayList<DayOperationFransaBank>();
				List<FileContent> contents = new ArrayList<FileContent>();

				for (FileContent element : dayOperation) {
					if (ExecutorThreadFransaBank.stopThreads) {
						return;
					}
					if (element.getTransactionOrigine().equals("040")) {
						DayOperationFransaBank G200 = new DayOperationFransaBank();
						dayopCreation(G200, element, "G200");
						float montantTransaction = Float.parseFloat(element.getMontantTransaction()) / fff;

						G200.setMontantSettlement(montantTransaction);
						G200.setPieceComptable("DB" + "161" + String.format("%06d", indexPieceComptable));
						dayOperations.add(G200);
						element.setChargebackStatus(ChargebackStatus.DONE.getCode());

					}else  {
						DayOperationFransaBank G201= new DayOperationFransaBank();

						dayopCreation(G201, element, "G201");
						float montantTransaction = Float.parseFloat(element.getMontantTransaction()) / fff;
						G201.setMontantSettlement(montantTransaction);
						G201.setPieceComptable("PA" + "171" + String.format("%06d", indexPieceComptable));
						dayOperations.add(G201);

						element.setChargebackStatus(ChargebackStatus.DONE.getCode());

					}
					contents.add(element);


				}
				tpList.addAll(contents);

				daysOperationsAll.addAll(dayOperations);

				logger.info("chargebackonUS =>{}", dayOperations.size());

				batchRepo.updateFinishBatch("TH" + threadNB, 1, new Date());
			}

		}	catch (Exception e) {
						catchError(e, threadNB);

					}
	}


	public void chargebackOffUSIssuer(String fileDate, int threadNB) {

		try {
			int index = 0;
			FileHeader summary = new FileHeader();
			int indexPieceComptable = getEveIndex1();
			if (fileRepository.findByfileDate(fileDate).isPresent()) {
				index = index + 1;
				summary = fileRepository.findByfileDate(fileDate).get();
				String destinationBank = summary.getDestinationBankIdentification();
				List<FileContent> dayOperation = settlementRepo.chargebackIssuer(String.valueOf(summary.getId()),
						destinationBank);
				List<DayOperationFransaBank> dayOperations = new ArrayList<DayOperationFransaBank>();
				List<FileContent> contents = new ArrayList<FileContent>();

				for (FileContent element : dayOperation) {
					if (ExecutorThreadFransaBank.stopThreads) {
						return;
					}
					List<FileContent> list = tp_detail.findByNumAutorisationAndNumCartePorteur(
							element.getNumAutorisationInitial(), element.getNumCartePorteur());
					if (list == null || list.isEmpty()) continue;
					FileContent transorigine = list.get(0);
					if (element.getTransactionOrigine().equals("040")) {
						DayOperationFransaBank G202 = new DayOperationFransaBank();
						dayopCreation(G202, element, "G202");
						float montantTransaction = Float.parseFloat(element.getMontantTransaction()) / fff;
						G202.setMontantSettlement(montantTransaction);
						G202.setPieceComptable("DB" + "161" + String.format("%06d", indexPieceComptable));
						element.setChargebackStatus(ChargebackStatus.DONE.getCode());
 						dayOperations.add(G202);
						contents.add(element);
					}else {
						DayOperationFransaBank G203= new DayOperationFransaBank();
						dayopCreation(G203, element, "G203");
						G203.setPieceComptable("PA" + "171" + String.format("%06d", indexPieceComptable));
						dayOperations.add(G203);
						float montantTransaction = Float.parseFloat(element.getMontantTransaction()) / fff;
						G203.setMontantSettlement(montantTransaction);
						element.setChargebackStatus(ChargebackStatus.DONE.getCode());
						Map<String, DayOperationFransaBank> days = new HashMap<>();
						dayOperations.add(G203);
						contents.add(element);
					}

				}
				tpList.addAll(contents);
				daysOperationsAll.addAll(dayOperations);
				logger.info("chargebackonUS =>{}", dayOperations.size());
				batchRepo.updateFinishBatch("TH" + threadNB, 1, new Date());
			}
		}	catch (Exception e) {
			catchError(e, threadNB);
		}
	}
	public void chargebackOffUSAcq(String fileDate, int threadNB) {
		try {
			int index = 0;
			FileHeader summary = new FileHeader();
			int indexPieceComptable = getEveIndex1();

			if (fileRepository.findByfileDate(fileDate).isPresent()) {
				index = index + 1;
				summary = fileRepository.findByfileDate(fileDate).get();
				String destinationBank = summary.getDestinationBankIdentification();
				List<FileContent> dayOperation = settlementRepo.chargebackAcq(String.valueOf(summary.getId()),
						destinationBank);
				List<DayOperationFransaBank> dayOperations = new ArrayList<DayOperationFransaBank>();
				List<FileContent> contents = new ArrayList<FileContent>();

				for (FileContent element : dayOperation) {
					if (ExecutorThreadFransaBank.stopThreads) {
						return;
					}
					List<FileContent> list = tp_detail.findByNumAutorisationAndNumCartePorteur(
							element.getNumAutorisationInitial(), element.getNumCartePorteur());
					if (list == null || list.isEmpty()) continue;
					FileContent transorigine = list.get(0);
					if (element.getTransactionOrigine().equals("040")) {
						DayOperationFransaBank G204 = new DayOperationFransaBank();
						dayopCreation(G204, element, "G204");
						G204.setPieceComptable("DB" + "161" + String.format("%06d", indexPieceComptable));
								dayOperations.add(G204);//comm
						float montantTransaction = Float.parseFloat(element.getMontantTransaction()) / fff;
						G204.setMontantSettlement(montantTransaction);
						element.setChargebackStatus(ChargebackStatus.DONE.getCode());
						contents.add(element);
					}else  {
						DayOperationFransaBank G205= new DayOperationFransaBank();
						dayopCreation(G205, element, "G205");
						float montantTransaction = Float.parseFloat(element.getMontantTransaction()) / fff;
						G205.setMontantSettlement(montantTransaction);
						G205.setPieceComptable("PA" + "171" + String.format("%06d", indexPieceComptable));
						Map<String, DayOperationFransaBank> days = new HashMap<>();
						dayOperations.add(G205);
						element.setChargebackStatus(ChargebackStatus.DONE.getCode());
					}

				}
				tpList.addAll(contents);
				daysOperationsAll.addAll(dayOperations);
				logger.info("chargebackonUS =>{}", dayOperations.size());
				batchRepo.updateFinishBatch("TH" + threadNB, 1, new Date());
			}

		}	catch (Exception e) {
			catchError(e, threadNB);

		}
	}

	public void addG001_G003_cash_out_onUs(String fileDate, int threadNB) {
		batchRepo.updateFinishBatch("TH" + threadNB, 0, new Date());
		try {
			int index = 0;
			FileHeader summary = new FileHeader();
			FileRequest.print(fileDate, FileRequest.getLineNumber());
			FileRequest.print(" " + fileRepository.findByfileDate(fileDate).isPresent(), FileRequest.getLineNumber());

			if (fileRepository.findByfileDate(fileDate).isPresent()) {
				index = index + 1;
				summary = fileRepository.findByfileDate(fileDate).get();
				String destinationBank = summary.getDestinationBankIdentification();
				List<FileContent> dayOperation = settlementRepo.getG001_G002_G003(String.valueOf(summary.getId()),
						destinationBank);

				List<DayOperationFransaBank> dayOperations = new ArrayList<DayOperationFransaBank>();
				List<FileContent> contents = new ArrayList<FileContent>();
				for (FileContent element : dayOperation) {
					if (ExecutorThreadFransaBank.stopThreads ) {
						return;
					}
					int indexPieceComptable = getEveIndex1();
					DayOperationFransaBank G001 = new DayOperationFransaBank();
					DayOperationFransaBank G002 = new DayOperationFransaBank();
					DayOperationFransaBank G003 = new DayOperationFransaBank();
					DayOperationFransaBank G054 = new DayOperationFransaBank();
					DayOperationFransaBank G055 = new DayOperationFransaBank();
					DayOperationFransaBank G064 = new DayOperationFransaBank();

					dayopCreation(G001, element, "G001");
					dayopCreation(G002, element, "G002");
					dayopCreation(G003, element, "G003");
					dayopCreation(G054, element, "G054");
					dayopCreation(G055, element, "G055");
					dayopCreation(G064, element, "G064");
					/////////////////// ********START G001**********///////////////////////////

					//////////////////////////////////////////////////////////////////////////
					G001.setPieceComptable("DB" + "161" + String.format("%06d", indexPieceComptable));
					G002.setPieceComptable("DB" + "161" + String.format("%06d", indexPieceComptable));
					G003.setPieceComptable("DB" + "161" + String.format("%06d", indexPieceComptable));
					G054.setPieceComptable("DB" + "161" + String.format("%06d", indexPieceComptable));
					G055.setPieceComptable("DB" + "161" + String.format("%06d", indexPieceComptable));
					G064.setPieceComptable("DB" + "161" + String.format("%06d", indexPieceComptable));

					/////////////////////////////////////////////////////////////////////////

					////////////////////////////////////////////////////////////////////////

					//////////////////////////////////////////////////////////////////////////
					// Float montantTransaction =Float.parseFloat(element.getMontantTransaction());

					float montantTransaction = Float.parseFloat(element.getMontantTransaction()) * fff;
					CommissionFransaBank com = commissionFransaBank.findByType(element.getTypeTransaction());
					float commission = (Float.parseFloat(com.getCommissionAcq())
							+ Float.parseFloat(com.getCommissionIssuer()));

					int tva = Integer.parseInt(tvaCommissionFransaBank.getTva());
					float commssionTvaArr = (commission * tva) / fff;

					float commission_satim = Float.parseFloat(com.getCmi());

					float tva_cmi = (commission_satim * tva) / fff;

					float montant_satim = tva_cmi + commission_satim;

					/////////////////////////////////////////////////////////////////////

					G001.setMontantSettlement(montantTransaction);
					G002.setMontantSettlement(commission);
					G002.setMontantSettlement(commission);
					G003.setMontantSettlement(commssionTvaArr);
					G054.setMontantSettlement(commission_satim);
					G055.setMontantSettlement(tva_cmi);
					G064.setMontantSettlement(montant_satim);

					Map<String, DayOperationFransaBank> days = new HashMap<>();
					days.put("SATIM", G054);
					days.put("SATIMTVA", G055);

					correctiondayOperation(element, days);

					contents.add(element);
					dayOperations.add(G001);
					dayOperations.add(G002);
					dayOperations.add(G003);
					dayOperations.add(G054);
					dayOperations.add(G055);
					dayOperations.add(G064);
					index = index + 1;

				}
				;
				// saveInBatches(dayOperations, threadNB, contents);
				tpList.addAll(contents);

				daysOperationsAll.addAll(dayOperations);

				logger.info("addG001_G003_cash_out_onUs =>{}", dayOperations.size());

				batchRepo.updateFinishBatch("TH" + threadNB, 1, new Date());
			}

		} catch (Exception e) {
			catchError(e, threadNB);

		}

	}

	public void addG004_G007_cash_out_onUs_AutreAgence(String fileDate, int threadNB) {
		batchRepo.updateFinishBatch("TH" + threadNB, 0, new Date());

		try {
			int index = 0;
			FileHeader summary = new FileHeader();
			if (fileRepository.findByfileDate(fileDate).isPresent()) {
				index = index + 1;
				summary = fileRepository.findByfileDate(fileDate).get();
				String destinationBank = summary.getDestinationBankIdentification();
				List<FileContent> dayOperation = settlementRepo.getG004_G005_G006_G007(String.valueOf(summary.getId()),
						destinationBank);
				List<DayOperationFransaBank> dayOperations = new ArrayList<DayOperationFransaBank>();
				List<FileContent> contents = new ArrayList<FileContent>();

				for (FileContent element : dayOperation) {
					if (ExecutorThreadFransaBank.stopThreads ) {
						return;
					}
					int indexPieceComptable = getEveIndex1();
					DayOperationFransaBank G004 = new DayOperationFransaBank();
					DayOperationFransaBank G005 = new DayOperationFransaBank();
					DayOperationFransaBank G006 = new DayOperationFransaBank();
					DayOperationFransaBank G007 = new DayOperationFransaBank();
					DayOperationFransaBank G056 = new DayOperationFransaBank();
					DayOperationFransaBank G057 = new DayOperationFransaBank();
					DayOperationFransaBank G058 = new DayOperationFransaBank();

					dayopCreation(G004, element, "G004");
					dayopCreation(G005, element, "G005");
					dayopCreation(G006, element, "G006");
					dayopCreation(G007, element, "G007");
					dayopCreation(G056, element, "G056");
					dayopCreation(G057, element, "G057");
					dayopCreation(G058, element, "G058");

					//////////////////////////////////////////////

					//////////////////////////////////////////////////////////////////////////
					float montantTransaction = Float.parseFloat(element.getMontantTransaction()) * fff;

					G004.setMontantSettlement(montantTransaction);

					G004.setPieceComptable("DB" + "161" + String.format("%06d", indexPieceComptable));

					///////////////// *********** END G004********////////////////////////////

					//////////////// **********START G005***********//////////////////////////

					CommissionFransaBank com = commissionFransaBank.findByType(element.getTypeTransaction());

					TvaCommissionFransaBank tvaCommissionFransaBank = tvaRepo.findTva();
					float commission = Float.parseFloat((com.getCommissionAcq()))
							+ Float.parseFloat((com.getCommissionIssuer()));

					G005.setMontantSettlement((commission));

					G005.setPieceComptable("DB" + "161" + String.format("%06d", indexPieceComptable)); // G005.setPieceComptable("DB"+element.getTypeTransaction()+String.format("%06d",index));

					/////////////// **********END G005*********///////////////////////////////

					//////////////// **********START G006***********//////////////////////////
					int tva = Integer.parseInt(tvaCommissionFransaBank.getTva());
					float fff = 100;
					float commssionTvaArr = ((commission * tva) / fff);

					G006.setMontantSettlement(commssionTvaArr);

					G006.setPieceComptable("DB" + "161" + String.format("%06d", indexPieceComptable));

					/////////////// **********END G006*********///////////////////////////////

					///////////// ***********START G007 ********/////////////////////////

					G007.setMontantSettlement(montantTransaction);

					G007.setPieceComptable("DB" + "161" + String.format("%06d", indexPieceComptable));

					///////////// ***********END G010 ********/////////////////////////

					//////////////// **********START G054***********//////////////////////////

					float commission_satim = Float.parseFloat(com.getCmi());

					G056.setMontantSettlement(commission_satim);

					G056.setPieceComptable("DB" + "161" + String.format("%06d", indexPieceComptable));

					/////////////// **********END G056 *********///////////////////////////////

					//////////////// **********START G057***********//////////////////////////

					float tva_cmi = (commission_satim * tva) / fff;

					G057.setMontantSettlement(tva_cmi);

					G057.setPieceComptable("DB" + "161" + String.format("%06d", indexPieceComptable));

					/////////////// **********END G057 *********///////////////////////////////

					//////////////// **********START G058***********//////////////////////////

					float montant_satim = tva_cmi + commission_satim;

					G058.setMontantSettlement((montant_satim));

					G058.setPieceComptable("DB" + "161" + String.format("%06d", indexPieceComptable));

					/////////////// **********END G057 *********///////////////////////////////

					// ************valid commission******//
					Map<String, DayOperationFransaBank> days = new HashMap<>();
					days.put("SATIM", G056);
					days.put("SATIMTVA", G057);

					correctiondayOperation(element, days);
					contents.add(element);
					tpList.addAll(contents);

					dayOperations.add(G004);
					dayOperations.add(G005);
					dayOperations.add(G006);
					dayOperations.add(G007);
					dayOperations.add(G056);
					dayOperations.add(G057);
					dayOperations.add(G058);

				}
				;

				// saveInBatches(dayOperations, threadNB, contents);
				tpList.addAll(contents);

				daysOperationsAll.addAll(dayOperations);

				logger.info("addG004_G007_cash_out_onUs_AutreAgence =>{}", dayOperations.size());

				batchRepo.updateFinishBatch("TH" + threadNB, 1, new Date());
			}

		} catch (Exception e) {
			catchError(e, threadNB);

		}

	}

	public void addG014_G025_cash_out_offUs_Issuer(String fileDate, int threadNB) {
		batchRepo.updateFinishBatch("TH" + threadNB, 0, new Date());

		try {
			int index = 0;
			FileHeader summary = new FileHeader();
			if (fileRepository.findByfileDate(fileDate).isPresent()) {
				index = index + 1;
				summary = fileRepository.findByfileDate(fileDate).get();
				String destinationBank = summary.getDestinationBankIdentification();
				List<FileContent> fileContentList = new ArrayList<FileContent>();
				List<FileContent> dayOperation = settlementRepo.getG014_G015_G016_G018_G019_G020_G021_G022_G23_G024(
						String.valueOf(summary.getId()), destinationBank, codeBanks);
				List<DayOperationFransaBank> dayOperations = new ArrayList<DayOperationFransaBank>();
				List<FileContent> contents = new ArrayList<FileContent>();

				for (FileContent element : dayOperation) {
					if (ExecutorThreadFransaBank.stopThreads ) {
						return;
					}
					int indexPieceComptable = getEveIndex1();
					DayOperationFransaBank G014 = new DayOperationFransaBank();
					DayOperationFransaBank G015 = new DayOperationFransaBank();
					DayOperationFransaBank G016 = new DayOperationFransaBank();
					DayOperationFransaBank G018 = new DayOperationFransaBank();
					DayOperationFransaBank G019 = new DayOperationFransaBank();
					DayOperationFransaBank G020 = new DayOperationFransaBank();
					DayOperationFransaBank G021 = new DayOperationFransaBank();
					DayOperationFransaBank G024 = new DayOperationFransaBank();
					DayOperationFransaBank G025 = new DayOperationFransaBank();
					dayopCreation(G014, element, "G014");
					dayopCreation(G015, element, "G015");
					dayopCreation(G016, element, "G016");
					dayopCreation(G018, element, "G018");
					dayopCreation(G019, element, "G019");
					dayopCreation(G020, element, "G020");
					dayopCreation(G021, element, "G021");
					dayopCreation(G024, element, "G024");
					dayopCreation(G025, element, "G025");
					/////////////////// ********START G014**********///////////////////////////
					G014.setCodeAgence(element.getNumRIBEmetteur().substring(3, 8));

					float montantTransaction = Float.parseFloat(element.getMontantTransaction()) * fff;

					G014.setMontantSettlement(montantTransaction);

					G014.setPieceComptable("DB" + "162" + String.format("%06d", indexPieceComptable)); // G014.setPieceComptable("DB"+element.getTypeTransaction()+String.format("%06d",index));

					///////////////// *********** END G014********////////////////////////////

					//////////////// **********START G015***********//////////////////////////
					G015.setCodeAgence(element.getNumRIBEmetteur().substring(3, 8));

					CommissionFransaBank com = commissionFransaBank.findByCodeIssuer(element.getTypeTransaction());

					TvaCommissionFransaBank tvaCommissionFransaBank = tvaRepo.findTva();
					float commission = Float.parseFloat(com.getCommissionAcq())
							+ Float.parseFloat(com.getCommissionIssuer());

					G015.setMontantSettlement(commission);

					G015.setPieceComptable("DB" + "162" + String.format("%06d", indexPieceComptable));

					/////////////// **********END G015*********///////////////////////////////

					//////////////// **********START G016***********//////////////////////////
					G016.setCodeAgence(element.getNumRIBEmetteur().substring(3, 8));

					int tva = Integer.parseInt(tvaCommissionFransaBank.getTva());
					float fff = 100;

					float commssionTvaArr = Math.round((commission * tva) / fff);

					// DayOperationFransaBank emp = new DayOperationFransaBank();

					G016.setMontantSettlement(Float.parseFloat("" + commssionTvaArr));

					G016.setPieceComptable("DB" + "162" + String.format("%06d", indexPieceComptable));

					/////////////// **********END G016*********///////////////////////////////

					///////////// ***********START G018 ********/////////////////////////

					G018.setCodeAgence(element.getNumRIBEmetteur().substring(3, 8));

					float commission_acq = Float.parseFloat(com.getCommissionAcq());

					G018.setMontantSettlement((commission_acq));

					G018.setPieceComptable("DB" + "162" + String.format("%06d", indexPieceComptable));

					///////////// ***********END G018 ********/////////////////////////

					///////////// ***********START G019 ********/////////////////////////

					G019.setCodeAgence(element.getNumRIBEmetteur().substring(3, 8));

					float tva_acq = Math.round((commission_acq * tva) / fff);

					G019.setMontantSettlement((tva_acq));

					G019.setPieceComptable("DB" + "162" + String.format("%06d", indexPieceComptable));

					///////////// ***********END G019 ********/////////////////////////

					///////////// ***********START G020 ********/////////////////////////

					G020.setCodeAgence(element.getNumRIBEmetteur().substring(3, 8));

					float commission_satim = Float.parseFloat(com.getCmi());

					G020.setMontantSettlement((commission_satim));

					G020.setPieceComptable("DB" + "162" + String.format("%06d", indexPieceComptable));

					///////////// ***********END G020 ********/////////////////////////

					/////////////////// ****START G021 **************//////////////////////
					G021.setCodeAgence(element.getNumRIBEmetteur().substring(3, 8));
					float tva_satim = Math.round((commission_satim * tva) / fff);
					G021.setMontantSettlement((tva_satim));
					G021.setPieceComptable("DB" + "162" + String.format("%06d", indexPieceComptable));

					////////////// ******* END G021 ****************/////////////////////////////

					/////////// ********* START G024 **************////////////////////////
					G024.setCodeAgence(element.getNumRIBEmetteur().substring(3, 8));

					float montant = montantTransaction + commission_acq + tva_acq;

					G024.setMontantSettlement((montant));

					G024.setPieceComptable("DB" + "162" + String.format("%06d", indexPieceComptable));

					////////////// ******* END G024 ****************/////////////////////////////

					/////////// ********* START G025 **************////////////////////////
					G025.setCodeAgence(element.getNumRIBEmetteur().substring(3, 8));

					float montant_satim = commission_satim + tva_satim;

					G025.setMontantSettlement((montant_satim));

					G025.setPieceComptable("DB" + "162" + String.format("%06d", indexPieceComptable));

					////////////// ******* END G024 ****************/////////////////////////////

					// ***********valid commission**///
					float interchange = commission_acq + tva_acq;
					Map<String, DayOperationFransaBank> days = new HashMap<>();
					days.put("SATIM", G020);
					days.put("SATIMTVA", G021);
					days.put("Interchange", G018);
					days.put("tvaInterchange", G019);

					correctiondayOperation(element, days);
					contents.add(element);
					dayOperations.add(G014);
					dayOperations.add(G015);
					dayOperations.add(G016);
					dayOperations.add(G018);
					dayOperations.add(G019);
					dayOperations.add(G020);
					dayOperations.add(G021);

					dayOperations.add(G024);
					dayOperations.add(G025);
				}
				;

				// saveInBatches(dayOperations, threadNB, contents);
				tpList.addAll(contents);

				daysOperationsAll.addAll(dayOperations);
				logger.info("addG014_G025_cash_out_offUs_Issuer=>{}", dayOperations.size());
				batchRepo.updateFinishBatch("TH" + threadNB, 1, new Date());

			}

		} catch (Exception e) {
			catchError(e, threadNB);

		}

	}

	public void addG008_G013_cash_out_offUs_Acq(String fileDate, int threadNB) {
		batchRepo.updateFinishBatch("TH" + threadNB, 0, new Date());
		try {
			int index = 0;
			FileHeader summary = new FileHeader();
			if (fileRepository.findByfileDate(fileDate).isPresent()) {
				index = index + 1;
				summary = fileRepository.findByfileDate(fileDate).get();
				String destinationBank = summary.getDestinationBankIdentification();
				List<FileContent> fileContentList = new ArrayList<FileContent>();
				List<FileContent> dayOperation = settlementRepo
						.getG008_G009_G010_G011_G012_G013(String.valueOf(summary.getId()), destinationBank, codeBanks);
				List<DayOperationFransaBank> dayOperations = new ArrayList<DayOperationFransaBank>();
				List<FileContent> contents = new ArrayList<FileContent>();
				for (FileContent element : dayOperation) {
					if (ExecutorThreadFransaBank.stopThreads ) {
						return;
					}
					int indexPieceComptable = getEveIndex1();

					DayOperationFransaBank G008 = new DayOperationFransaBank();
					DayOperationFransaBank G009 = new DayOperationFransaBank();
					DayOperationFransaBank G010 = new DayOperationFransaBank();
					DayOperationFransaBank G011 = new DayOperationFransaBank();
					DayOperationFransaBank G012 = new DayOperationFransaBank();
					DayOperationFransaBank G013 = new DayOperationFransaBank();

					dayopCreation(G008, element, "G008");
					dayopCreation(G009, element, "G009");
					dayopCreation(G010, element, "G010");
					dayopCreation(G011, element, "G011");
					dayopCreation(G012, element, "G012");
					dayopCreation(G013, element, "G013");
					/////////////////// ********START G014**********///////////////////////////

					float montantTransaction = Float.parseFloat(element.getMontantTransaction()) * fff;

					G008.setMontantSettlement(montantTransaction);

					G008.setPieceComptable("DB" + "170" + String.format("%06d", indexPieceComptable)); // G008.setPieceComptable("DB"+element.getTypeTransaction()+String.format("%06d",index));

					///////////////// *********** END G014********////////////////////////////

					//////////////// **********START G015***********//////////////////////////

					CommissionFransaBank com = commissionFransaBank.findByCodeAcq(element.getTypeTransaction());

					TvaCommissionFransaBank tvaCommissionFransaBank = tvaRepo.findTva();
					float commission = Float.parseFloat(com.getCommissionAcq());
					G009.setMontantSettlement((commission));

					G009.setPieceComptable("DB" + "170" + String.format("%06d", indexPieceComptable)); // G009.setPieceComptable("DB"+element.getTypeTransaction()+String.format("%06d",index));

					/////////////// **********END G015*********///////////////////////////////

					//////////////// **********START G016***********//////////////////////////

					int tva = Integer.parseInt(tvaCommissionFransaBank.getTva());
					float fff = 100;
					float commssionTvaArr = Math.round((commission * tva) / fff);
					G010.setMontantSettlement((commssionTvaArr));
					G010.setPieceComptable("DB" + "170" + String.format("%06d", indexPieceComptable));

					/////////////// **********END G016*********///////////////////////////////

					///////////// ***********START G018 ********/////////////////////////

					float commission_cpi = Float.parseFloat(com.getCpi());

					G011.setMontantSettlement((commission_cpi));
					G011.setPieceComptable("DB" + "170" + String.format("%06d", indexPieceComptable));

					///////////// ***********END G018 ********/////////////////////////

					///////////// ***********START G019 ********/////////////////////////

					float tva_acq = Math.round((commission_cpi * tva) / fff);

					G012.setMontantSettlement((tva_acq));
					G012.setPieceComptable("DB" + "170" + String.format("%06d", indexPieceComptable));

					///////////// ***********END G019 ********/////////////////////////

					///////////// ***********START G020 ********/////////////////////////

					float MONTANT = (montantTransaction) + commssionTvaArr + commission;

					G013.setMontantSettlement((MONTANT));

					G013.setPieceComptable("DB" + "170" + String.format("%06d", indexPieceComptable));

					///////////// ***********END G020 ********/////////////////////////

					// *********valid commission****//
					Float interchange = commssionTvaArr + commission;

					Map<String, DayOperationFransaBank> days = new HashMap<>();
					days.put("Interchange", G009);
					days.put("tvaInterchange", G010);
					correctiondayOperation(element, days);
					contents.add(element);
					dayOperations.add(G008);
					dayOperations.add(G009);
					dayOperations.add(G010);
					dayOperations.add(G011);
					dayOperations.add(G012);
					dayOperations.add(G013);

				}
				;

				// saveInBatches(dayOperations, threadNB, contents);
				tpList.addAll(contents);

				daysOperationsAll.addAll(dayOperations);
				logger.info("addG008_G013_cash_out_offUs_Acq =>{}", dayOperations.size());
				batchRepo.updateFinishBatch("TH" + threadNB, 1, new Date());

			}
			synchronized (Test) {

				Test.add(1);
			}

		} catch (Exception e) {
			catchError(e, threadNB);

		}

	}

	/////////////// ****************** ACHAT
	/////////////// TPE***********************/////////////////////

	public void addG026_G033_Achat_offus_Issuer_MinMontant(String fileDate, int threadNB) {
		batchRepo.updateFinishBatch("TH" + threadNB, 0, new Date());
		try {
			int index = 0;
			FileHeader summary = new FileHeader();
			if (fileRepository.findByfileDate(fileDate).isPresent()) {
				index = index + 1;
				summary = fileRepository.findByfileDate(fileDate).get();
				String destinationBank = summary.getDestinationBankIdentification();
				List<FileContent> fileContentList = new ArrayList<FileContent>();
				List<FileContent> dayOperation = settlementRepo
						.getG026_G027_G028_G029_G030_G031_G032_G033(String.valueOf(summary.getId()), destinationBank);
				List<DayOperationFransaBank> dayOperations = new ArrayList<DayOperationFransaBank>();
				List<FileContent> contents = new ArrayList<FileContent>();
				for (FileContent element : dayOperation) {
					if (ExecutorThreadFransaBank.stopThreads ) {
						return;
					}
					int indexPieceComptable = getEveIndex1();
					DayOperationFransaBank G026 = new DayOperationFransaBank();
					DayOperationFransaBank G027 = new DayOperationFransaBank();
					DayOperationFransaBank G028 = new DayOperationFransaBank();
					DayOperationFransaBank G029 = new DayOperationFransaBank();
					DayOperationFransaBank G030 = new DayOperationFransaBank();
					DayOperationFransaBank G031 = new DayOperationFransaBank();
					DayOperationFransaBank G032 = new DayOperationFransaBank();
					DayOperationFransaBank G033 = new DayOperationFransaBank();
					dayopCreation(G026, element, "G026");
					dayopCreation(G027, element, "G027");
					dayopCreation(G028, element, "G028");
					dayopCreation(G029, element, "G029");
					dayopCreation(G030, element, "G030");
					dayopCreation(G031, element, "G031");
					dayopCreation(G032, element, "G032");
					dayopCreation(G033, element, "G033");
					String montantTransaction_DA = element.getMontantTransaction().replace(".", "");
					String montantTransaction_ML = montantTransaction_DA;
					int montant = Integer.parseInt(montantTransaction_ML);

					CommissionFransaBank com = commissionFransaBank.findByType(element.getTypeTransaction());

					MargeCommission margeCom = margeCommissionRepository.findByMargekey("MIN").get();
					if (montant > Integer.parseInt(margeCom.getValeurMin())
							&& montant <= Integer.parseInt(margeCom.getValeurMax())) {

						/////////////////// ********START G026 **********///////////////////////////

						float montantTransaction = Float.parseFloat(element.getMontantTransaction()) * fff;
						G026.setMontantSettlement(montantTransaction);

						G026.setPieceComptable("PA" + "171" + String.format("%06d", indexPieceComptable));

						///////////////// *********** END G026 ********////////////////////////////

						//////////////// **********START G027 ***********//////////////////////////

						TvaCommissionFransaBank tvaCommissionFransaBank = tvaRepo.findTva();
						float commission = Float.parseFloat(com.getCommissionAcq())
								+ Float.parseFloat(com.getCommissionIssuer());
						G027.setMontantSettlement((commission));

						G027.setPieceComptable("PA" + "171" + String.format("%06d", indexPieceComptable));

						/////////////// **********END G027 *********///////////////////////////////

						//////////////// **********START G028 ***********//////////////////////////

						int tva = Integer.parseInt(tvaCommissionFransaBank.getTva());

						float fff = 100;

						float commssionTvaArr = Math.round((commission * tva) / fff);

						G028.setMontantSettlement((commssionTvaArr));
						G028.setPieceComptable("PA" + "171" + String.format("%06d", indexPieceComptable));

						/////////////// **********END G028 *********///////////////////////////////

						///////////// ***********START G029 ********/////////////////////////

						float commission_cpi = Float.parseFloat(com.getCpi());
						G029.setMontantSettlement((commission_cpi));
						G029.setPieceComptable("PA" + "171" + String.format("%06d", indexPieceComptable));

						///////////// ***********END G029 ********/////////////////////////

						/////////////////// ****START G030 **************//////////////////////

						float tva_satim = Math.round((commission_cpi * tva) / fff);

						G030.setMontantSettlement((tva_satim));

						G030.setPieceComptable("PA" + "171" + String.format("%06d", indexPieceComptable));

						////////////// ******* END G030 ****************/////////////////////////////

						/////////// ********* START G031 **************////////////////////////

						float interchange = Float.parseFloat(com.getCommissionIssuer());

						G031.setMontantSettlement((interchange));

						G031.setPieceComptable("PA" + "171" + String.format("%06d", indexPieceComptable));

						////////////// ******* END G031 ****************/////////////////////////////

						/////////// ********* START G032 **************////////////////////////

						float tva_interchange = Math.round((interchange * tva) / fff);

						G032.setMontantSettlement((tva_interchange));

						G032.setPieceComptable("PA" + "171" + String.format("%06d", indexPieceComptable));

						////////////// ******* END G032 ****************/////////////////////////////

						/////////// ********* START G033 **************////////////////////////

						float montant_a_verser = montant - interchange - tva_interchange;

						G033.setMontantSettlement((montant_a_verser));

						G033.setPieceComptable("PA" + "171" + String.format("%06d", indexPieceComptable));

						////////////// ******* END G033 ****************/////////////////////////////

						Map<String, DayOperationFransaBank> days = new HashMap<>();
						days.put("Interchange", G031);
						days.put("tvaInterchange", G032);
						correctiondayOperation(element, days);
						float interchangeCommission = interchange + tva_interchange;

						contents.add(element);
						dayOperations.add(G026);
						dayOperations.add(G027);
						dayOperations.add(G028);
						dayOperations.add(G029);
						dayOperations.add(G030);
						dayOperations.add(G031);
						dayOperations.add(G032);
						dayOperations.add(G033);
					}
				}

				// saveInBatches(dayOperations, threadNB, contents);
				tpList.addAll(contents);

				daysOperationsAll.addAll(dayOperations);
				logger.info("addG026_G033_Achat_offus_Issuer_MinMontant =>{}", dayOperations.size());
				batchRepo.updateFinishBatch("TH" + threadNB, 1, new Date());
			}

		} catch (Exception e) {
			catchError(e, threadNB);

		}

	}

	public void addG026_G033_Achat_offus_Issuer_MaxMontant(String fileDate, int threadNB) {
		batchRepo.updateFinishBatch("TH" + threadNB, 0, new Date());

		try {
			int index = 0;
			FileHeader summary = new FileHeader();
			if (fileRepository.findByfileDate(fileDate).isPresent()) {
				index = index + 1;
				summary = fileRepository.findByfileDate(fileDate).get();
				String destinationBank = summary.getDestinationBankIdentification();
				List<FileContent> fileContentList = new ArrayList<FileContent>();
				List<FileContent> dayOperation = settlementRepo
						.getG026_G027_G028_G029_G030_G031_G032_G033(String.valueOf(summary.getId()), destinationBank);
				List<DayOperationFransaBank> dayOperations = new ArrayList<DayOperationFransaBank>();
				List<FileContent> contents = new ArrayList<FileContent>();
				for (FileContent element : dayOperation) {
					if (ExecutorThreadFransaBank.stopThreads ) {
						return;
					}
					int indexPieceComptable = getEveIndex1();
					DayOperationFransaBank G026 = new DayOperationFransaBank();
					DayOperationFransaBank G027 = new DayOperationFransaBank();
					DayOperationFransaBank G028 = new DayOperationFransaBank();
					DayOperationFransaBank G029 = new DayOperationFransaBank();
					DayOperationFransaBank G030 = new DayOperationFransaBank();
					DayOperationFransaBank G031 = new DayOperationFransaBank();
					DayOperationFransaBank G032 = new DayOperationFransaBank();
					DayOperationFransaBank G033 = new DayOperationFransaBank();
					dayopCreation(G026, element, "G026");
					dayopCreation(G027, element, "G027");
					dayopCreation(G028, element, "G028");
					dayopCreation(G029, element, "G029");
					dayopCreation(G030, element, "G030");
					dayopCreation(G031, element, "G031");
					dayopCreation(G032, element, "G032");
					dayopCreation(G033, element, "G033");
					boolean stopSave = true;
					String montantTransaction_DA = element.getMontantTransaction().replace(".", "");
					String montantTransaction_ML = montantTransaction_DA;
					int amount = Integer.parseInt(montantTransaction_ML);

					List<CommissionAchatFransaBank> CommissionAchatFransaBank = commissionAchatFransaBankRepository
							.findAll();
					for (CommissionAchatFransaBank com : CommissionAchatFransaBank) {
						if (amount > Integer.parseInt(com.getValeurMin()) && (com.getValeurMax().equals("20000000")
								|| amount <= Integer.parseInt(com.getValeurMax()))) {
							stopSave = false;
							float ff = 100;
							float commission_commercant_achat = 0;
							float cmi_commercant = 0;
							float cpi_commercant = 0;
							float tv_cmi = 0;
							float tv_cpi = 0;

							commission_commercant_achat = (Float.parseFloat(com.getValeurFix())
									+ ((amount * Float.parseFloat(com.getValeurVarivable())) / 100));

							TvaCommissionFransaBank tvaCommissionFransaBank = tvaRepo.findTva();
							int tva = Integer.parseInt(tvaCommissionFransaBank.getTva());
//							FileRequest.print("tva" + tva, FileRequest.getLineNumber());
							int commissionTTC = Math.round(commission_commercant_achat * (1 + tva / ff));
//							FileRequest.print("commissionTTC" + commissionTTC, FileRequest.getLineNumber());

							int tvaCommercant = Math.round(commission_commercant_achat * tva / ff);
//							FileRequest.print("tvaCommercant" + tvaCommercant, FileRequest.getLineNumber());

							int commissionHT = commissionTTC - tvaCommercant;

//							FileRequest.print("commissionHT" + commissionHT, FileRequest.getLineNumber());

//							tva_commercant = (commission_commercant_achat * tva) / ff;
							cmi_commercant = Float.parseFloat(com.getCmi());
							tv_cmi = ((cmi_commercant * tva) / ff);
							cpi_commercant = Float.parseFloat(com.getCpi());
							tv_cpi = ((cpi_commercant * tva) / ff);
							int interchangeTTC = Math.round(commissionTTC / 2);
							float interchangeHT = ((float) interchangeTTC) / (1 + tva / ff);
							int interchangeTVA = Math.round(interchangeTTC - interchangeHT);

							int interchangeHTTVA = interchangeTTC - interchangeTVA;
//							FileRequest.print("interchangeTTC" + interchangeTTC, FileRequest.getLineNumber());
//							FileRequest.print("interchangeTVA" + interchangeTVA, FileRequest.getLineNumber());
//							FileRequest.print("interchangeHTTVA" + interchangeHTTVA, FileRequest.getLineNumber());

							/////////////////// ********START G026 **********///////////////////////////

							float montantTransaction = Float.parseFloat(element.getMontantTransaction()) * fff;
							G026.setMontantSettlement(montantTransaction);

							G026.setPieceComptable("PA" + "171" + String.format("%06d", indexPieceComptable));

							///////////////// *********** END G026 ********////////////////////////////

							G027.setMontantSettlement((commissionHT));

							G027.setPieceComptable("PA" + "171" + String.format("%06d", indexPieceComptable));

							/////////////// **********END G027 *********///////////////////////////////

							//////////////// **********START G028 ***********//////////////////////////

							G028.setMontantSettlement((tvaCommercant));
							G028.setPieceComptable("PA" + "171" + String.format("%06d", indexPieceComptable));

							/////////////// **********END G028*********///////////////////////////////

							///////////// ***********START G029 ********/////////////////////////

							G029.setMontantSettlement((cpi_commercant));

							int lengPieceComptable3 = G028.getPieceComptable().length();

							G029.setPieceComptable("PA" + "171" + String.format("%06d", indexPieceComptable));

							///////////// ***********END G029 ********/////////////////////////

							/////////////////// ****START G030 **************//////////////////////

							G030.setMontantSettlement((tv_cpi));

							G030.setPieceComptable("PA" + "171" + String.format("%06d", indexPieceComptable));

							////////////// ******* END G030 ****************/////////////////////////////

							/////////// ********* START G031 **************////////////////////////

							G031.setMontantSettlement((interchangeHTTVA));

							G031.setPieceComptable("PA" + "171" + String.format("%06d", indexPieceComptable));

							////////////// ******* END G031 ****************/////////////////////////////

							/////////// ********* START G032 **************////////////////////////

							G032.setMontantSettlement((interchangeTVA));

							G032.setPieceComptable("PA" + "171" + String.format("%06d", indexPieceComptable));

							////////////// ******* END G032 ****************/////////////////////////////

							/////////// ********* START G033 **************////////////////////////

							float amount_verser = amount - interchangeHTTVA - interchangeTVA;

							G033.setMontantSettlement((amount_verser));

							G033.setPieceComptable("PA" + "171" + String.format("%06d", indexPieceComptable));

							////////////// ******* END G033 ****************/////////////////////////////
							contents.add(element);

						}
						MargeCommission margeCom = margeCommissionRepository.findByMargekey("MAX").get();

						if (amount > Integer.parseInt(margeCom.getValeurMin())
								&& amount <= Integer.parseInt(margeCom.getValeurMax())) {
							stopSave = false;
							float ff = 100;
							float commission_commercant_achat = 0;
							float tva_commercant = 0;
							float cmi_commercant = 0;
							float cpi_commercant = 0;
							float tv_cmi = 0;
							float tv_cpi = 0;
							float interchange = 0;
							float tva_interchange = 0;
							// commission commercant bien recu
							commission_commercant_achat = Float.parseFloat(com.getValeurFix());

							TvaCommissionFransaBank tvaCommissionFransaBank = tvaRepo.findTva();
							int tva = Integer.parseInt(tvaCommissionFransaBank.getTva());
							tva_commercant = ((commission_commercant_achat * tva) / ff);

							cmi_commercant = Float.parseFloat(com.getCmi());
							tv_cmi = ((cmi_commercant * tva) / ff);
							cpi_commercant = Float.parseFloat(com.getCpi());
							tv_cpi = ((cpi_commercant * tva) / ff);
							float CommissionAymen = Math.round(commission_commercant_achat + tva_commercant);
							float interchangewissal = Math.round(CommissionAymen / 2);
							float interchangebefore = interchangewissal / (1 + (tva / ff));
							tva_interchange = Math.round(interchangewissal - interchangebefore);
							interchange = Math.round(interchangewissal - tva_interchange);

							/*
							 * interchange = (commission_commercant_achat / 2); tva_interchange =
							 * ((interchange * tva) / ff);
							 */

							/////////////////// ********START G026 **********///////////////////////////

							float montantTransaction = Float.parseFloat(element.getMontantTransaction()) * fff;
							G026.setMontantSettlement(montantTransaction);

							G026.setPieceComptable("PA" + "171" + String.format("%06d", indexPieceComptable));

							///////////////// *********** END G026 ********////////////////////////////

							/////////////// **********END G028*********///////////////////////////////

							///////////// ***********START G029 ********/////////////////////////

							G029.setMontantSettlement((cpi_commercant));

							G029.setPieceComptable("PA" + "171" + String.format("%06d", indexPieceComptable));

							///////////// ***********END G029 ********/////////////////////////

							/////////////////// ****START G030 **************//////////////////////

							G030.setMontantSettlement((tv_cpi));

							G030.setPieceComptable("PA" + "171" + String.format("%06d", indexPieceComptable));

							////////////// ******* END G030 ****************/////////////////////////////

							/////////// ********* START G031 **************////////////////////////

							G031.setMontantSettlement((interchange));

							G031.setPieceComptable("PA" + "171" + String.format("%06d", indexPieceComptable));

							////////////// ******* END G031 ****************/////////////////////////////

							/////////// ********* START G032 **************////////////////////////

							G032.setMontantSettlement((tva_interchange));

							G032.setPieceComptable("PA" + "171" + String.format("%06d", indexPieceComptable));

							//////////////// **********START G027 ***********//////////////////////////

							G027.setMontantSettlement((commission_commercant_achat));

							G027.setPieceComptable("PA" + "171" + String.format("%06d", indexPieceComptable));

							/////////////// **********END G027 *********///////////////////////////////

							//////////////// **********START G028 ***********//////////////////////////

							G028.setMontantSettlement((tva_commercant));

							G028.setPieceComptable("PA" + "171" + String.format("%06d", indexPieceComptable));

							////////////// ******* END G032 ****************/////////////////////////////

							/////////// ********* START G033 **************////////////////////////

							float amount_verser = amount - interchange - tva_interchange;
							G033.setMontantSettlement((amount_verser));
							G033.setPieceComptable("PA" + "171" + String.format("%06d", indexPieceComptable));

							////////////// ******* END G033 ****************/////////////////////////////

							contents.add(element);

						}
					}
					;

					Map<String, DayOperationFransaBank> days = new HashMap<>();
					days.put("Interchange", G031);
					days.put("tvaInterchange", G032);
					correctiondayOperation(element, days);
					if (stopSave == false) {
						dayOperations.add(G026);
						dayOperations.add(G027);
						dayOperations.add(G028);
						dayOperations.add(G029);
						dayOperations.add(G030);
						dayOperations.add(G031);
						dayOperations.add(G032);
						dayOperations.add(G033);
					}
				}
				;

				// saveInBatches(dayOperations, threadNB, contents);
				tpList.addAll(contents);

				daysOperationsAll.addAll(dayOperations);
				logger.info("addG026_G033_Achat_offus_Issuer_MaxMontant =>{}", dayOperations.size());

				batchRepo.updateFinishBatch("TH" + threadNB, 1, new Date());

			}

		} catch (Exception e) {
			catchError(e, threadNB);

		}

	}

	public void addG034_G040_Achat_offus_Acq_MinMontant(String fileDate, int threadNB) {
		batchRepo.updateFinishBatch("TH" + threadNB, 0, new Date());

		try {
			int index = 0;
			FileHeader summary = new FileHeader();
			if (fileRepository.findByfileDate(fileDate).isPresent()) {
				index = index + 1;
				summary = fileRepository.findByfileDate(fileDate).get();
				String destinationBank = summary.getDestinationBankIdentification();
				List<FileContent> fileContentList = new ArrayList<FileContent>();
				List<FileContent> dayOperation = settlementRepo
						.getG034_G035_G036_G037_G038_G039_G040(String.valueOf(summary.getId()), destinationBank);
				List<DayOperationFransaBank> dayOperations = new ArrayList<DayOperationFransaBank>();
				List<FileContent> contents = new ArrayList<FileContent>();
				for (FileContent element : dayOperation) {
					if (ExecutorThreadFransaBank.stopThreads ) {
						return;
					}
					int indexPieceComptable = getEveIndex1();
					DayOperationFransaBank G034 = new DayOperationFransaBank();
					DayOperationFransaBank G035 = new DayOperationFransaBank();
					DayOperationFransaBank G036 = new DayOperationFransaBank();
					DayOperationFransaBank G037 = new DayOperationFransaBank();
					DayOperationFransaBank G038 = new DayOperationFransaBank();
					DayOperationFransaBank G039 = new DayOperationFransaBank();
					DayOperationFransaBank G040 = new DayOperationFransaBank();
					dayopCreation(G034, element, "G034");
					dayopCreation(G035, element, "G035");
					dayopCreation(G036, element, "G036");
					dayopCreation(G037, element, "G037");
					dayopCreation(G038, element, "G038");
					dayopCreation(G039, element, "G039");
					dayopCreation(G040, element, "G040");
					String montantTransaction_DA = element.getMontantTransaction().replace(".", "");
					String montantTransaction_ML = montantTransaction_DA;
					int montant = Integer.parseInt(montantTransaction_ML);
					MargeCommission margeCom = margeCommissionRepository.findByMargekey("MIN").get();
					FileRequest.print("" + montant, FileRequest.getLineNumber());
					FileRequest.print("" + margeCom.getValeurMin(), FileRequest.getLineNumber());
					FileRequest.print("" + margeCom.getValeurMax(), FileRequest.getLineNumber());

					if (montant > Integer.parseInt(margeCom.getValeurMin())
							&& montant <= Integer.parseInt(margeCom.getValeurMax())) {

						/////////////////// ********START G034 **********///////////////////////////
						G034.setCodeAgence(element.getNumRIBEmetteur().substring(3, 8));

						float montantTransaction = Float.parseFloat(element.getMontantTransaction()) * fff;
						G034.setMontantSettlement(montantTransaction);

						G034.setPieceComptable("PA" + "164" + String.format("%06d", indexPieceComptable));

						///////////////// *********** END G034 ********////////////////////////////

						//////////////// **********START G035 ***********//////////////////////////
						G035.setCodeAgence(element.getNumRIBEmetteur().substring(3, 8));

						CommissionFransaBank com = commissionFransaBank.findByType(element.getTypeTransaction());

						TvaCommissionFransaBank tvaCommissionFransaBank = tvaRepo.findTva();
						float commission = Float.parseFloat(com.getCommissionIssuer());

						G035.setMontantSettlement((commission));

						G035.setPieceComptable("PA" + "164" + String.format("%06d", indexPieceComptable));

						/////////////// **********END G035 *********///////////////////////////////

						//////////////// **********START G036 ***********//////////////////////////
						G036.setCodeAgence(element.getNumRIBEmetteur().substring(3, 8));

						int tva = Integer.parseInt(tvaCommissionFransaBank.getTva());

						float fff = 100;

						float commssionTvaArr = Math.round((commission * tva) / fff);

						G036.setMontantSettlement((commssionTvaArr));

						G036.setPieceComptable("PA" + "164" + String.format("%06d", indexPieceComptable));

						/////////////// **********END G036 *********///////////////////////////////

						///////////// ***********START G037 ********/////////////////////////

						G037.setCodeAgence(element.getNumRIBEmetteur().substring(3, 8));

						float commission_cmi = Float.parseFloat(com.getCmi());

						G037.setMontantSettlement((commission_cmi));

						G037.setPieceComptable("PA" + "164" + String.format("%06d", indexPieceComptable));

						///////////// ***********END G037 ********/////////////////////////

						/////////////////// ****START G038 **************//////////////////////
						G038.setCodeAgence(element.getNumRIBEmetteur().substring(3, 8));

						float tva_satim = ((commission_cmi * tva) / fff);

						G038.setMontantSettlement((tva_satim));

						G038.setPieceComptable("PA" + "164" + String.format("%06d", indexPieceComptable));

						////////////// ******* END G038 ****************/////////////////////////////

						/////////// ********* START G039 **************////////////////////////
						G039.setCodeAgence(element.getNumRIBEmetteur().substring(3, 8));

						float montant_a_verser = montant - commission - commssionTvaArr;

						G039.setMontantSettlement((montant_a_verser));

						G039.setPieceComptable("PA" + "164" + String.format("%06d", indexPieceComptable));

						////////////// ******* END G039 ****************/////////////////////////////

						/////////// ********* START G040 **************////////////////////////
						G040.setCodeAgence(element.getNumRIBEmetteur().substring(3, 8));

						float montant_satim_a_verser = commission_cmi + tva_satim;

						G040.setMontantSettlement((montant_satim_a_verser));

						G040.setPieceComptable("PA" + "164" + String.format("%06d", indexPieceComptable));

						////////////// ******* END G040 ****************/////////////////////////////

						float interchange = commission + commssionTvaArr;
						contents.add(element);
						Map<String, DayOperationFransaBank> days = new HashMap<>();
						days.put("Interchange", G035);
						days.put("tvaInterchange", G036);

						correctiondayOperation(element, days);
						dayOperations.add(G034);
						dayOperations.add(G035);
						dayOperations.add(G036);
						dayOperations.add(G037);
						dayOperations.add(G038);
						dayOperations.add(G039);
						dayOperations.add(G040);

					}
				}
				;
				FileRequest.print("" + dayOperations.toString(), FileRequest.getLineNumber());
				tpList.addAll(contents);

				daysOperationsAll.addAll(dayOperations);
				logger.info("addG034_G040_Achat_offus_Acq_MinMontant =>{}", dayOperations.size());

				// saveInBatches(dayOperations, threadNB, contents);
				batchRepo.updateFinishBatch("TH" + threadNB, 1, new Date());

				//
			}

		} catch (Exception e) {
			catchError(e, threadNB);

		}

	}

	public void addG034_G040_Achat_offus_Acq_MaxMontant(String fileDate, int threadNB) {
		batchRepo.updateFinishBatch("TH" + threadNB, 0, new Date());
		try {
			int index = 0;
			FileHeader summary = new FileHeader();
			if (fileRepository.findByfileDate(fileDate).isPresent()) {
				index = index + 1;
				summary = fileRepository.findByfileDate(fileDate).get();
				String destinationBank = summary.getDestinationBankIdentification();
				List<FileContent> fileContentList = new ArrayList<FileContent>();
				List<FileContent> dayOperation = settlementRepo
						.getG034_G035_G036_G037_G038_G039_G040(String.valueOf(summary.getId()), destinationBank);
				List<DayOperationFransaBank> dayOperations = new ArrayList<DayOperationFransaBank>();
				List<FileContent> contents = new ArrayList<FileContent>();
				for (FileContent element : dayOperation) {
					if (ExecutorThreadFransaBank.stopThreads ) {
						return;
					}
					int indexPieceComptable = getEveIndex1();
					DayOperationFransaBank G034 = new DayOperationFransaBank();
					DayOperationFransaBank G035 = new DayOperationFransaBank();
					DayOperationFransaBank G036 = new DayOperationFransaBank();
					DayOperationFransaBank G037 = new DayOperationFransaBank();
					DayOperationFransaBank G038 = new DayOperationFransaBank();
					DayOperationFransaBank G039 = new DayOperationFransaBank();
					DayOperationFransaBank G040 = new DayOperationFransaBank();
					dayopCreation(G034, element, "G034");
					dayopCreation(G035, element, "G035");
					dayopCreation(G036, element, "G036");
					dayopCreation(G037, element, "G037");
					dayopCreation(G038, element, "G038");
					dayopCreation(G039, element, "G039");
					dayopCreation(G040, element, "G040");
					boolean stopSave = true;
					String montantTransaction_DA = element.getMontantTransaction().replace(".", "");
					String montantTransaction_ML = montantTransaction_DA;
					int amount = Integer.parseInt(montantTransaction_ML);

					List<CommissionAchatFransaBank> CommissionAchatFransaBank = commissionAchatFransaBankRepository
							.findAll();
					FileRequest.print("" + amount, FileRequest.getLineNumber());

					for (CommissionAchatFransaBank com : CommissionAchatFransaBank) {
						FileRequest.print("" + com.getValeurMin(), FileRequest.getLineNumber());
						FileRequest.print("" + com.getValeurMax(), FileRequest.getLineNumber());
						if (amount > Integer.parseInt(com.getValeurMin()) && (com.getValeurMax().equals("20000000")
								|| amount <= Integer.parseInt(com.getValeurMax()))) {
							stopSave = false;
							float ff = 100;
							float commission_commercant_achat = 0;
							float tva_commercant = 0;
							float cmi_commercant = 0;
							int cpi_commercant = 0;
							float tv_cmi = 0;
							float tv_cpi = 0;
							float interchange = 0;
							float tva_interchange = 0;
							commission_commercant_achat = (Float.parseFloat(com.getValeurFix())
									+ ((amount * Float.parseFloat(com.getValeurVarivable())) / 100));

							TvaCommissionFransaBank tvaCommissionFransaBank = tvaRepo.findTva();
							int tva = Integer.parseInt(tvaCommissionFransaBank.getTva());
							tva_commercant = ((commission_commercant_achat * tva) / ff);

							cmi_commercant = Float.parseFloat(com.getCmi());
							tv_cmi = ((cmi_commercant * tva) / ff);
							cpi_commercant = Integer.parseInt(com.getCpi());
							tv_cpi = ((cpi_commercant * tva) / ff);
							/*
							 * interchange = commission_commercant_achat / 2; tva_interchange =
							 * Math.round((interchange * tva) / ff);
							 */
							float CommissionAymen = Math.round(commission_commercant_achat + tva_commercant);
							float interchangewissal = Math.round(CommissionAymen / 2);
							float interchangebefore = interchangewissal / (1 + (tva / ff));
							tva_interchange = Math.round(interchangewissal - interchangebefore);
							interchange = Math.round(interchangewissal - tva_interchange);

							float amount_verser = amount - interchange - tva_interchange;
							float amount_cmi_a_verser = cmi_commercant + tv_cmi;
							/////////////////// ********START G026 **********///////////////////////////
							G034.setCodeAgence(element.getNumRIBEmetteur().substring(3, 8));

							float montantTransaction = Float.parseFloat(element.getMontantTransaction()) * fff;
							G034.setMontantSettlement(montantTransaction);

							G034.setPieceComptable("PA" + "164" + String.format("%06d", indexPieceComptable));

							///////////////// *********** END G034 ********////////////////////////////

							//////////////// **********START G035 ***********//////////////////////////
							G035.setCodeAgence(element.getNumRIBEmetteur().substring(3, 8));

							G035.setMontantSettlement((interchange));

							G035.setPieceComptable("PA" + "164" + String.format("%06d", indexPieceComptable));

							/////////////// **********END G035 *********///////////////////////////////

							//////////////// **********START G036 ***********//////////////////////////
							G036.setCodeAgence(element.getNumRIBEmetteur().substring(3, 8));

							G036.setMontantSettlement((tva_interchange));

							int lengPieceComptable2 = G035.getPieceComptable().length();

							G036.setPieceComptable("PA" + "164" + String.format("%06d", indexPieceComptable));

							/////////////// **********END G036*********///////////////////////////////

							///////////// ***********START G037 ********/////////////////////////
							G037.setCodeAgence(element.getNumRIBEmetteur().substring(3, 8));

							G037.setMontantSettlement((cmi_commercant));

							int lengPieceComptable3 = G036.getPieceComptable().length();

							G037.setPieceComptable("PA" + "164" + String.format("%06d", indexPieceComptable));

							///////////// ***********END G037 ********/////////////////////////

							/////////////////// ****START G038 **************//////////////////////
							G038.setCodeAgence(element.getNumRIBEmetteur().substring(3, 8));

							G038.setMontantSettlement((tv_cmi));

							G038.setPieceComptable("PA" + "164" + String.format("%06d", indexPieceComptable));

							////////////// ******* END G038 ****************/////////////////////////////

							/////////// ********* START G039 **************////////////////////////
							G039.setCodeAgence(element.getNumRIBEmetteur().substring(3, 8));

							G039.setMontantSettlement((amount_verser));

							G039.setPieceComptable("PA" + "164" + String.format("%06d", indexPieceComptable));

							////////////// ******* END G039 ****************/////////////////////////////

							/////////// ********* START G040 **************////////////////////////
							G040.setCodeAgence(element.getNumRIBEmetteur().substring(3, 8));

							int lenghMontantINTER = String.valueOf(amount_cmi_a_verser).length();
							G040.setMontantSettlement((amount_cmi_a_verser));

							G040.setPieceComptable("PA" + "164" + String.format("%06d", indexPieceComptable));

							////////////// ******* END G040 ****************/////////////////////////////
							float interchangeCommission = interchange + tva_interchange;
							contents.add(element);

						}
						MargeCommission margeCom = margeCommissionRepository.findByMargekey("MAX").get();
						if (amount > Integer.parseInt(margeCom.getValeurMin())
								&& amount <= Integer.parseInt(margeCom.getValeurMax())) {
							stopSave = false;
							float ff = 100;
							int commission_commercant_achat = 0;
							float tva_commercant = 0;
							float cmi_commercant = 0;
							int cpi_commercant = 0;
							float tv_cmi = 0;
							float tv_cpi = 0;
							float interchange = 0;
							float tva_interchange = 0;
							commission_commercant_achat = Integer.parseInt(com.getValeurFix());

							TvaCommissionFransaBank tvaCommissionFransaBank = tvaRepo.findTva();
							int tva = Integer.parseInt(tvaCommissionFransaBank.getTva());
							tva_commercant = ((commission_commercant_achat * tva) / ff);

							cmi_commercant = Float.parseFloat(com.getCmi());
							tv_cmi = ((cmi_commercant * tva) / ff);
							cpi_commercant = Integer.parseInt(com.getCpi());
							tv_cpi = ((cpi_commercant * tva) / ff);

							float CommissionAymen = Math.round(commission_commercant_achat + tva_commercant);
							float interchangewissal = Math.round(CommissionAymen / 2);
							float interchangebefore = interchangewissal / (1 + (tva / ff));
							tva_interchange = Math.round(interchangewissal - interchangebefore);
							interchange = Math.round(interchangewissal - tva_interchange);

							float amount_verser = amount - interchange - tva_interchange;
							float amount_cmi_a_verser = cmi_commercant + tv_cmi;
							/////////////////// ********START G026 **********///////////////////////////
							G034.setCodeAgence(element.getNumRIBEmetteur().substring(3, 8));

							float montantTransaction = Float.parseFloat(element.getMontantTransaction()) * fff;
							G034.setMontantSettlement(montantTransaction);

							G034.setPieceComptable("PA" + "164" + String.format("%06d", indexPieceComptable));

							///////////////// *********** END G034 ********////////////////////////////

							//////////////// **********START G035 ***********//////////////////////////
							G035.setCodeAgence(element.getNumRIBEmetteur().substring(3, 8));

							G035.setMontantSettlement((interchange));

							int lengPieceComptable1 = G034.getPieceComptable().length();
							G035.setPieceComptable("PA" + "164" + String.format("%06d", indexPieceComptable));

							/////////////// **********END G035 *********///////////////////////////////

							//////////////// **********START G036 ***********//////////////////////////
							G036.setCodeAgence(element.getNumRIBEmetteur().substring(3, 8));

							G036.setMontantSettlement((tva_interchange));

							G036.setPieceComptable("PA" + "164" + String.format("%06d", indexPieceComptable));

							/////////////// **********END G036*********///////////////////////////////

							///////////// ***********START G037 ********/////////////////////////

							G037.setCodeAgence(element.getNumRIBEmetteur().substring(3, 8));

							G037.setMontantSettlement((cmi_commercant));
							G037.setPieceComptable("PA" + "164" + String.format("%06d", indexPieceComptable));

							///////////// ***********END G037 ********/////////////////////////

							/////////////////// ****START G038 **************//////////////////////
							G038.setCodeAgence(element.getNumRIBEmetteur().substring(3, 8));

							G038.setMontantSettlement((tv_cmi));

							G038.setPieceComptable("PA" + "164" + String.format("%06d", indexPieceComptable));

							////////////// ******* END G038 ****************/////////////////////////////

							/////////// ********* START G039 **************////////////////////////
							G039.setCodeAgence(element.getNumRIBEmetteur().substring(3, 8));

							G039.setMontantSettlement((amount_verser));

							G039.setPieceComptable("PA" + "164" + String.format("%06d", indexPieceComptable));

							////////////// ******* END G039 ****************/////////////////////////////

							/////////// ********* START G040 **************////////////////////////
							G040.setCodeAgence(element.getNumRIBEmetteur().substring(3, 8));

							G040.setMontantSettlement((amount_cmi_a_verser));

							G040.setPieceComptable("PA" + "164" + String.format("%06d", indexPieceComptable));

							////////////// ******* END G040 ****************/////////////////////////////
							float interchangeCommission = interchange + tva_interchange;

							contents.add(element);
						}
					}
					Map<String, DayOperationFransaBank> days = new HashMap<>();
					days.put("Interchange", G035);
					days.put("tvaInterchange", G036);
					days.put("SATIM", G037);
					days.put("SATIMTVA", G038);
					correctiondayOperation(element, days);

					if (stopSave == false) {
						dayOperations.add(G034);
						dayOperations.add(G035);
						dayOperations.add(G036);
						dayOperations.add(G037);
						dayOperations.add(G038);
						dayOperations.add(G039);
						dayOperations.add(G040);
					}

				}
				;
				FileRequest.print("" + dayOperations.toString(), FileRequest.getLineNumber());
				tpList.addAll(contents);

				// saveInBatches(dayOperations, threadNB, contents);
				daysOperationsAll.addAll(dayOperations);
				logger.info("addG034_G040_Achat_offus_Acq_MaxMontant =>{}", dayOperations.size());

				batchRepo.updateFinishBatch("TH" + threadNB, 1, new Date());
			}

		} catch (Exception e) {
			catchError(e, threadNB);

		}

	}

///*/ok///
	public void addG041_G043_Achat_Onus_MemeAgence_MinMontant(String fileDate, int threadNB) {
		batchRepo.updateFinishBatch("TH" + threadNB, 0, new Date());
		try {
			int index = 0;
			FileHeader summary = new FileHeader();
			if (fileRepository.findByfileDate(fileDate).isPresent()) {
				index = index + 1;
				summary = fileRepository.findByfileDate(fileDate).get();
				String destinationBank = summary.getDestinationBankIdentification();
				List<FileContent> fileContentList = new ArrayList<FileContent>();
				List<FileContent> dayOperation = settlementRepo.getG041_G042_G043(String.valueOf(summary.getId()),
						destinationBank);
				List<DayOperationFransaBank> dayOperations = new ArrayList<DayOperationFransaBank>();
				TvaCommissionFransaBank tvaCommissionFransaBank = tvaRepo.findTva();
				List<FileContent> contents = new ArrayList<FileContent>();
				for (FileContent element : dayOperation) {
					if (ExecutorThreadFransaBank.stopThreads ) {
						return;
					}
					int indexPieceComptable = getEveIndex1();
					DayOperationFransaBank G041 = new DayOperationFransaBank();
					DayOperationFransaBank G042 = new DayOperationFransaBank();
					DayOperationFransaBank G043 = new DayOperationFransaBank();
					DayOperationFransaBank G059 = new DayOperationFransaBank();
					DayOperationFransaBank G060 = new DayOperationFransaBank();
					DayOperationFransaBank G065 = new DayOperationFransaBank();
					dayopCreation(G041, element, "G041");
					dayopCreation(G042, element, "G042");
					dayopCreation(G043, element, "G043");
					dayopCreation(G059, element, "G059");
					dayopCreation(G060, element, "G060");
					dayopCreation(G065, element, "G065");
					String montantTransaction_DA = element.getMontantTransaction().replace(".", "");
					String montantTransaction_ML = montantTransaction_DA;
					int montant = Integer.parseInt(montantTransaction_ML);

					/////////////////// ********START G041 **********///////////////////////////

					float montantTransaction = Float.parseFloat(element.getMontantTransaction()) * fff;
					G041.setMontantSettlement(montantTransaction);

					G041.setPieceComptable("PA" + "163" + String.format("%06d", indexPieceComptable));

					///////////////// *********** END G041 ********////////////////////////////

					//////////////// **********START G042 ***********//////////////////////////

					CommissionFransabankOnUs com = commissionFransabankOnUsRepository
							.findByType(element.getTypeTransaction());

					float commission = Float.parseFloat(com.getCommissionFixe());

					G042.setMontantSettlement((commission));

					G042.setPieceComptable("PA" + "163" + String.format("%06d", indexPieceComptable));

					/////////////// **********END G042 *********///////////////////////////////

					//////////////// **********START G043 ***********//////////////////////////

					int tva = Integer.parseInt(tvaCommissionFransaBank.getTva());

					float fff = 100;

					float commssionTvaArr = ((commission * tva) / fff);

					G043.setMontantSettlement((commssionTvaArr));

					G043.setPieceComptable("PA" + "163" + String.format("%06d", indexPieceComptable));

					/////////////// **********END G043 *********///////////////////////////////

					//////////////// **********START G059***********//////////////////////////

					float commission_satim = Float.parseFloat(com.getCmi());

					G059.setMontantSettlement((commission_satim));

					G059.setPieceComptable("PA" + "163" + String.format("%06d", indexPieceComptable));

					/////////////// **********END G059 *********///////////////////////////////

					//////////////// **********START G057***********//////////////////////////

					float tva_cmi = ((commission_satim * tva) / fff);

					G060.setMontantSettlement((tva_cmi));

					G060.setPieceComptable("PA" + "163" + String.format("%06d", indexPieceComptable));

					/////////////// **********END G060 *********///////////////////////////////

					//////////////// **********START G065***********//////////////////////////

					float montant_satim = tva_cmi + commission_satim;

					G065.setMontantSettlement((montant_satim));

					G065.setPieceComptable("PA" + "163" + String.format("%06d", indexPieceComptable));

					/////////////// **********END G065 *********///////////////////////////////

					// *********VALID COMMISSION*****//
					Map<String, DayOperationFransaBank> days = new HashMap<>();
					days.put("SATIM", G059);
					days.put("SATIMTVA", G060);
					correctiondayOperation(element, days);
					contents.add(element);
					dayOperations.add(G041);
					dayOperations.add(G042);
					dayOperations.add(G043);
					dayOperations.add(G059);
					dayOperations.add(G060);
					dayOperations.add(G065);

				}
				;
				tpList.addAll(contents);

				// saveInBatches(dayOperations, threadNB, contents);
				daysOperationsAll.addAll(dayOperations);
				logger.info("addG041_G043_Achat_Onus_MemeAgence_MinMontant =>{}", dayOperations.size());

				batchRepo.updateFinishBatch("TH" + threadNB, 1, new Date());
			}

		} catch (Exception e) {
			catchError(e, threadNB);

		}

	}

	public void addG044_G047_Achat_Onus_AutreAgence_MinMontant(String fileDate, int threadNB) {
		batchRepo.updateFinishBatch("TH" + threadNB, 0, new Date());
		try {

			int index = 0;
			FileHeader summary = new FileHeader();
			if (fileRepository.findByfileDate(fileDate).isPresent()) {
				index = index + 1;
				summary = fileRepository.findByfileDate(fileDate).get();
				String destinationBank = summary.getDestinationBankIdentification();
				List<FileContent> fileContentList = new ArrayList<FileContent>();
				List<FileContent> dayOperation = settlementRepo.getG044_G045_G046_G047(String.valueOf(summary.getId()),
						destinationBank);
				List<DayOperationFransaBank> dayOperations = new ArrayList<DayOperationFransaBank>();
				List<FileContent> contents = new ArrayList<FileContent>();
				for (FileContent element : dayOperation) {
					if (ExecutorThreadFransaBank.stopThreads ) {
						return;
					}
					int indexPieceComptable = getEveIndex1();
					DayOperationFransaBank G044 = new DayOperationFransaBank();
					DayOperationFransaBank G045 = new DayOperationFransaBank();
					DayOperationFransaBank G046 = new DayOperationFransaBank();
					DayOperationFransaBank G047 = new DayOperationFransaBank();
					DayOperationFransaBank G061 = new DayOperationFransaBank();
					DayOperationFransaBank G062 = new DayOperationFransaBank();
					DayOperationFransaBank G063 = new DayOperationFransaBank();
					dayopCreation(G044, element, "G044");
					dayopCreation(G045, element, "G045");
					dayopCreation(G046, element, "G046");
					dayopCreation(G047, element, "G047");
					dayopCreation(G061, element, "G061");
					dayopCreation(G062, element, "G062");
					dayopCreation(G063, element, "G063");
					String montantTransaction_DA = element.getMontantTransaction().replace(".", "");
					String montantTransaction_ML = montantTransaction_DA;
					float montant = Float.parseFloat(montantTransaction_ML);

					/////////////////// ********START G044 **********///////////////////////////

					float montantTransaction = Float.parseFloat(element.getMontantTransaction()) * fff;
					G044.setMontantSettlement(montantTransaction);

					G044.setPieceComptable("PA" + "163" + String.format("%06d", indexPieceComptable));

					///////////////// *********** END G044 ********////////////////////////////

					//////////////// **********START G045 ***********//////////////////////////

					CommissionFransabankOnUs com = commissionFransabankOnUsRepository
							.findByType(element.getTypeTransaction());

					TvaCommissionFransaBank tvaCommissionFransaBank = tvaRepo.findTva();
					float commission = Float.parseFloat(com.getCommissionFixe());

					G045.setMontantSettlement((commission));

					int lengPieceComptable1 = G044.getPieceComptable().length();
					G045.setPieceComptable("PA" + "163" + String.format("%06d", indexPieceComptable));

					/////////////// **********END G045 *********///////////////////////////////

					//////////////// **********START G046 ***********//////////////////////////

					int tva = Integer.parseInt(tvaCommissionFransaBank.getTva());

					float fff = 100;

					float commssionTvaArr = ((commission * tva) / fff);

					G046.setMontantSettlement((commssionTvaArr));

					int lengPieceComptable2 = G045.getPieceComptable().length();

					G046.setPieceComptable("PA" + "163" + String.format("%06d", indexPieceComptable));

					/////////////// **********END G046 *********///////////////////////////////

					//////////////// **********START G047 ***********//////////////////////////

					G047.setMontantSettlement((montant));

					int lengPieceComptable3 = G046.getPieceComptable().length();

					G047.setPieceComptable("PA" + "163" + String.format("%06d", indexPieceComptable));

					/////////////// **********END G047 *********///////////////////////////////

					//////////////// **********START G061***********//////////////////////////

					float commission_satim = Float.parseFloat(com.getCmi());

					G061.setMontantSettlement((commission_satim));

					int lengPieceComptable54 = G047.getPieceComptable().length();
					G061.setPieceComptable("PA" + "163" + String.format("%06d", indexPieceComptable));

					/////////////// **********END G061 *********///////////////////////////////

					//////////////// **********START G062***********//////////////////////////

					float tva_cmi = ((commission_satim * tva) / fff);

					G062.setMontantSettlement((tva_cmi));

					int lengPieceComptable55 = G061.getPieceComptable().length();
					G062.setPieceComptable("PA" + "163" + String.format("%06d", indexPieceComptable));

					/////////////// **********END G062 *********///////////////////////////////

					//////////////// **********START G063***********//////////////////////////

					float montant_satim = tva_cmi + commission_satim;

					G063.setMontantSettlement((montant_satim));
					int lengPieceComptable31 = G062.getPieceComptable().length();
					G063.setPieceComptable("PA" + "163" + String.format("%06d", indexPieceComptable));

					/////////////// **********END G063 *********///////////////////////////////

					Map<String, DayOperationFransaBank> days = new HashMap<>();
					days.put("SATIM", G061);
					days.put("SATIMTVA", G062);
					correctiondayOperation(element, days);
					contents.add(element);
					dayOperations.add(G044);
					dayOperations.add(G045);
					dayOperations.add(G046);
					dayOperations.add(G047);
					dayOperations.add(G061);
					dayOperations.add(G062);
					dayOperations.add(G063);
				}
				;
				tpList.addAll(contents);

				// saveInBatches(dayOperations, threadNB, contents);
				daysOperationsAll.addAll(dayOperations);
				logger.info("addG044_G047_Achat_Onus_AutreAgence_MinMontant =>{}", dayOperations.size());

				batchRepo.updateFinishBatch("TH" + threadNB, 1, new Date());
			}

		} catch (Exception e) {
			catchError(e, threadNB);

		}

	}

	///////////////////// ************* ACHAT INTERNET

	//////////////// new achat internet ////////////////////////////////

	public void addG081_G086_AchatInternet_Onus_MemeAgence_MinMontant(String fileDate, int threadNB) {
		batchRepo.updateFinishBatch("TH" + threadNB, 0, new Date());
		try {
			int index = 0;
			FileHeader summary = new FileHeader();
			if (fileRepository.findByfileDate(fileDate).isPresent()) {
				index = index + 1;
				summary = fileRepository.findByfileDate(fileDate).get();
				String destinationBank = summary.getDestinationBankIdentification();
				List<FileContent> fileContentList = new ArrayList<FileContent>();
				List<FileContent> dayOperation = settlementRepo
						.getG081_G082_G083_G084_G085_G086(String.valueOf(summary.getId()), destinationBank);
				List<DayOperationFransaBank> dayOperations = new ArrayList<DayOperationFransaBank>();
				TvaCommissionFransaBank tvaCommissionFransaBank = tvaRepo.findTva();
				List<FileContent> contents = new ArrayList<FileContent>();
				for (FileContent element : dayOperation) {
					if (ExecutorThreadFransaBank.stopThreads ) {
						return;
					}
					int indexPieceComptable = getEveIndex1();
					DayOperationFransaBank G081 = new DayOperationFransaBank();
					DayOperationFransaBank G082 = new DayOperationFransaBank();
					DayOperationFransaBank G083 = new DayOperationFransaBank();
					DayOperationFransaBank G084 = new DayOperationFransaBank();
					DayOperationFransaBank G085 = new DayOperationFransaBank();
					DayOperationFransaBank G086 = new DayOperationFransaBank();
					dayopCreation(G081, element, "G081");
					dayopCreation(G082, element, "G082");
					dayopCreation(G083, element, "G083");
					dayopCreation(G084, element, "G084");
					dayopCreation(G085, element, "G085");
					dayopCreation(G086, element, "G086");
					String montantTransaction_DA = element.getMontantTransaction().replace(".", "");
					String montantTransaction_ML = montantTransaction_DA;
					int montant = Integer.parseInt(montantTransaction_ML);
					CommissionFransabankOnUs com = commissionFransabankOnUsRepository
							.findByTypeAchatInternetMin(element.getTypeTransaction());
					if (montant < Integer.parseInt(com.getMax())) {
						/////////////////// ********START G041 **********///////////////////////////

						float montantTransaction = Float.parseFloat(element.getMontantTransaction()) * fff;
						G081.setMontantSettlement(montantTransaction);

						G081.setPieceComptable("PA" + "163" + String.format("%06d", indexPieceComptable));

						///////////////// *********** END G081 ********////////////////////////////

						//////////////// **********START G042 ***********//////////////////////////

						float commission = Float.parseFloat(com.getCommissionFixe());

						G082.setMontantSettlement((commission));

						G082.setPieceComptable("PA" + "163" + String.format("%06d", indexPieceComptable));

						/////////////// **********END G082 *********///////////////////////////////

						//////////////// **********START G083 ***********//////////////////////////

						int tva = Integer.parseInt(tvaCommissionFransaBank.getTva());

						float fff = 100;

						float commssionTvaArr = ((commission * tva) / fff);

						int lenghTva = String.valueOf(commssionTvaArr).length();
						G083.setMontantSettlement((commssionTvaArr));

						G083.setPieceComptable("PA" + "163" + String.format("%06d", indexPieceComptable));

						/////////////// **********END G083 *********///////////////////////////////

						//////////////// **********START G084***********//////////////////////////

						float commission_satim = Float.parseFloat(com.getCmi());

						G084.setMontantSettlement((commission_satim));

						G084.setPieceComptable("PA" + "163" + String.format("%06d", indexPieceComptable));

						/////////////// **********END G084 *********///////////////////////////////

						//////////////// **********START G085***********//////////////////////////

						float tva_cmi = ((commission_satim * tva) / fff);

						int lenghTvaCmi = String.valueOf(tva_cmi).length();
						G085.setMontantSettlement((tva_cmi));

						G085.setPieceComptable("PA" + "163" + String.format("%06d", indexPieceComptable));

						/////////////// **********END G085 *********///////////////////////////////

						//////////////// **********START G086***********//////////////////////////

						float montant_satim = tva_cmi + commission_satim;

						G086.setMontantSettlement((montant_satim));

						G086.setPieceComptable("PA" + "163" + String.format("%06d", indexPieceComptable));

						/////////////// **********END G065 *********///////////////////////////////

						Map<String, DayOperationFransaBank> days = new HashMap<>();
						days.put("SATIM", G084);
						days.put("SATIMTVA", G085);
						correctiondayOperation(element, days);

						contents.add(element);
						dayOperations.add(G081);
						dayOperations.add(G082);
						dayOperations.add(G083);
						dayOperations.add(G084);
						dayOperations.add(G085);
						dayOperations.add(G086);
					}
				}
				;
				tpList.addAll(contents);

				// saveInBatches(dayOperations, threadNB, contents);
				daysOperationsAll.addAll(dayOperations);
				logger.info("addG081_G086_AchatInternet_Onus_MemeAgence_MinMontant =>{}", dayOperations.size());

				batchRepo.updateFinishBatch("TH" + threadNB, 1, new Date());
			}

		} catch (Exception e) {

			catchError(e, threadNB);

		}

	}

	public void addG081_G086_AchatInternet_Onus_MemeAgence_MaxMontant(String fileDate, int threadNB) {
		batchRepo.updateFinishBatch("TH" + threadNB, 0, new Date());
		try {
			int index = 0;
			FileHeader summary = new FileHeader();
			if (fileRepository.findByfileDate(fileDate).isPresent()) {
				index = index + 1;
				summary = fileRepository.findByfileDate(fileDate).get();
				String destinationBank = summary.getDestinationBankIdentification();
				List<FileContent> fileContentList = new ArrayList<FileContent>();
				List<FileContent> dayOperation = settlementRepo
						.getG081_G082_G083_G084_G085_G086(String.valueOf(summary.getId()), destinationBank);
				List<DayOperationFransaBank> dayOperations = new ArrayList<DayOperationFransaBank>();
				TvaCommissionFransaBank tvaCommissionFransaBank = tvaRepo.findTva();
				List<FileContent> contents = new ArrayList<FileContent>();
				for (FileContent element : dayOperation) {
					if (ExecutorThreadFransaBank.stopThreads ) {
						return;
					}
					int indexPieceComptable = getEveIndex1();
					DayOperationFransaBank G081 = new DayOperationFransaBank();
					DayOperationFransaBank G082 = new DayOperationFransaBank();
					DayOperationFransaBank G083 = new DayOperationFransaBank();
					DayOperationFransaBank G084 = new DayOperationFransaBank();
					DayOperationFransaBank G085 = new DayOperationFransaBank();
					DayOperationFransaBank G086 = new DayOperationFransaBank();
					dayopCreation(G081, element, "G081");
					dayopCreation(G082, element, "G082");
					dayopCreation(G083, element, "G083");
					dayopCreation(G084, element, "G084");
					dayopCreation(G085, element, "G085");
					dayopCreation(G086, element, "G086");
					String montantTransaction_DA = element.getMontantTransaction().replace(".", "");
					String montantTransaction_ML = montantTransaction_DA;
					int montant = Integer.parseInt(montantTransaction_ML);
					CommissionFransabankOnUs com = commissionFransabankOnUsRepository
							.findByTypeAchatInternetMax(element.getTypeTransaction());
					if (montant >= Integer.parseInt(com.getMin())) {
						/////////////////// ********START G041 **********///////////////////////////

						float montantTransaction = Float.parseFloat(element.getMontantTransaction()) * fff;
						G081.setMontantSettlement(montantTransaction);

						G081.setPieceComptable("PA" + "163" + String.format("%06d", indexPieceComptable));

						///////////////// *********** END G081 ********////////////////////////////

						//////////////// **********START G042 ***********//////////////////////////

						float commission = Float.parseFloat(com.getCommissionFixe());

						G082.setMontantSettlement((commission));

						G082.setPieceComptable("PA" + "163" + String.format("%06d", indexPieceComptable));

						/////////////// **********END G082 *********///////////////////////////////

						//////////////// **********START G083 ***********//////////////////////////

						int tva = Integer.parseInt(tvaCommissionFransaBank.getTva());

						float fff = 100;

						float commssionTvaArr = ((commission * tva) / fff);

						G083.setMontantSettlement((commssionTvaArr));

						G083.setPieceComptable("PA" + "163" + String.format("%06d", indexPieceComptable));

						/////////////// **********END G083 *********///////////////////////////////

						//////////////// **********START G084***********//////////////////////////

						float commission_satim = Integer.parseInt(com.getCmi());

						G084.setMontantSettlement((commission_satim));

						G084.setPieceComptable("PA" + "163" + String.format("%06d", indexPieceComptable));

						/////////////// **********END G084 *********///////////////////////////////

						//////////////// **********START G085***********//////////////////////////

						float tva_cmi = ((commission_satim * tva) / fff);

						G085.setMontantSettlement((tva_cmi));

						G085.setPieceComptable("PA" + "163" + String.format("%06d", indexPieceComptable));

						/////////////// **********END G085 *********///////////////////////////////

						//////////////// **********START G086***********//////////////////////////

						float montant_satim = tva_cmi + commission_satim;

						G086.setMontantSettlement((montant_satim));

						G086.setPieceComptable("PA" + "163" + String.format("%06d", indexPieceComptable));

						/////////////// **********END G065 *********///////////////////////////////

						Map<String, DayOperationFransaBank> days = new HashMap<>();
						days.put("SATIM", G084);
						days.put("SATIMTVA", G085);
						correctiondayOperation(element, days);
						contents.add(element);

						dayOperations.add(G081);
						dayOperations.add(G082);
						dayOperations.add(G083);
						dayOperations.add(G084);
						dayOperations.add(G085);
						dayOperations.add(G086);
					}
				}
				;
				tpList.addAll(contents);

				// saveInBatches(dayOperations, threadNB, contents);
				daysOperationsAll.addAll(dayOperations);
				logger.info("addG081_G086_AchatInternet_Onus_MemeAgence_MaxMontant =>{}", dayOperations.size());

				batchRepo.updateFinishBatch("TH" + threadNB, 1, new Date());
			}

		} catch (Exception e) {
			catchError(e, threadNB);

		}

	}

	public void addG087_G093_AchatInternet_Onus_AutreAgence_MaxMontant(String fileDate, int threadNB) {
		batchRepo.updateFinishBatch("TH" + threadNB, 0, new Date());
		try {
			int index = 0;
			FileHeader summary = new FileHeader();
			if (fileRepository.findByfileDate(fileDate).isPresent()) {
				index = index + 1;
				summary = fileRepository.findByfileDate(fileDate).get();
				String destinationBank = summary.getDestinationBankIdentification();
				List<FileContent> fileContentList = new ArrayList<FileContent>();
				List<FileContent> dayOperation = settlementRepo
						.getG087_G088_G089_G090_G091_G092_G093(String.valueOf(summary.getId()), destinationBank);
				List<DayOperationFransaBank> dayOperations = new ArrayList<DayOperationFransaBank>();
				List<FileContent> contents = new ArrayList<FileContent>();
				for (FileContent element : dayOperation) {
					if (ExecutorThreadFransaBank.stopThreads ) {
						return;
					}
					int indexPieceComptable = getEveIndex1();
					DayOperationFransaBank G087 = new DayOperationFransaBank();
					DayOperationFransaBank G088 = new DayOperationFransaBank();
					DayOperationFransaBank G089 = new DayOperationFransaBank();
					DayOperationFransaBank G090 = new DayOperationFransaBank();
					DayOperationFransaBank G091 = new DayOperationFransaBank();
					DayOperationFransaBank G092 = new DayOperationFransaBank();
					DayOperationFransaBank G093 = new DayOperationFransaBank();
					dayopCreation(G087, element, "G087");
					dayopCreation(G088, element, "G088");
					dayopCreation(G089, element, "G089");
					dayopCreation(G090, element, "G090");
					dayopCreation(G091, element, "G091");
					dayopCreation(G092, element, "G092");
					dayopCreation(G093, element, "G093");

					String montantTransaction_DA = element.getMontantTransaction().replace(".", "");
					String montantTransaction_ML = montantTransaction_DA;
					float montant = Float.parseFloat(montantTransaction_ML);
					CommissionFransabankOnUs com = commissionFransabankOnUsRepository
							.findByTypeAchatInternetMax(element.getTypeTransaction());
					if (montant >= Integer.parseInt(com.getMin())) {
						/////////////////// ********START G044 **********///////////////////////////

						float montantTransaction = Float.parseFloat(element.getMontantTransaction()) * fff;
						G087.setMontantSettlement(montantTransaction);

						G087.setPieceComptable("PA" + "163" + String.format("%06d", indexPieceComptable));

						///////////////// *********** END G087 ********////////////////////////////

						//////////////// **********START G088 ***********//////////////////////////

						TvaCommissionFransaBank tvaCommissionFransaBank = tvaRepo.findTva();
						float commission = Float.parseFloat(com.getCommissionFixe());

						G088.setMontantSettlement((commission));

						G088.setPieceComptable("PA" + "163" + String.format("%06d", indexPieceComptable));

						/////////////// **********END G088 *********///////////////////////////////

						//////////////// **********START G089 ***********//////////////////////////

						int tva = Integer.parseInt(tvaCommissionFransaBank.getTva());

						float fff = 100;

						float commssionTvaArr = ((commission * tva) / fff);

						G089.setMontantSettlement((commssionTvaArr));

						G089.setPieceComptable("PA" + "163" + String.format("%06d", indexPieceComptable));

						/////////////// **********END G089 *********///////////////////////////////

						//////////////// **********START G090 ***********//////////////////////////

						G090.setMontantSettlement((montant));

						G090.setPieceComptable("PA" + "163" + String.format("%06d", indexPieceComptable));

						/////////////// **********END G090 *********///////////////////////////////

						//////////////// **********START G091***********//////////////////////////

						float commission_satim = Float.parseFloat(com.getCmi());

						G091.setMontantSettlement((commission_satim));

						G091.setPieceComptable("PA" + "163" + String.format("%06d", indexPieceComptable));

						/////////////// **********END G091 *********///////////////////////////////

						//////////////// **********START G092***********//////////////////////////

						float tva_cmi = ((commission_satim * tva) / fff);

						G092.setMontantSettlement((tva_cmi));

						G092.setPieceComptable("PA" + "163" + String.format("%06d", indexPieceComptable));

						/////////////// **********END G092 *********///////////////////////////////

						//////////////// **********START G093***********//////////////////////////

						float montant_satim = tva_cmi + commission_satim;

						G093.setMontantSettlement((montant_satim));

						G093.setPieceComptable("PA" + "163" + String.format("%06d", indexPieceComptable));

						/////////////// **********END G063 *********///////////////////////////////

						Map<String, DayOperationFransaBank> days = new HashMap<>();
						days.put("SATIM", G091);
						days.put("SATIMTVA", G092);
						correctiondayOperation(element, days);
						contents.add(element);

						dayOperations.add(G087);
						dayOperations.add(G088);
						dayOperations.add(G089);
						dayOperations.add(G090);
						dayOperations.add(G091);
						dayOperations.add(G092);
						dayOperations.add(G093);
					}
				}
				;
				tpList.addAll(contents);

				// saveInBatches(dayOperations, threadNB, contents);
				daysOperationsAll.addAll(dayOperations);
				logger.info("addG087_G093_AchatInternet_Onus_AutreAgence_MaxMontant =>{}", dayOperations.size());

				batchRepo.updateFinishBatch("TH" + threadNB, 1, new Date());
			}

		} catch (Exception e) {
			catchError(e, threadNB);

		}

	}

	public void addG044_G047_AchatInternet_Onus_AutreAgence_MinMontant(String fileDate, int threadNB) {
		batchRepo.updateFinishBatch("TH" + threadNB, 0, new Date());
		try {
			int index = 0;
			FileHeader summary = new FileHeader();
			if (fileRepository.findByfileDate(fileDate).isPresent()) {
				index = index + 1;
				summary = fileRepository.findByfileDate(fileDate).get();
				String destinationBank = summary.getDestinationBankIdentification();
				List<FileContent> fileContentList = new ArrayList<FileContent>();
				List<FileContent> dayOperation = settlementRepo
						.getG087_G088_G089_G090_G091_G092_G093(String.valueOf(summary.getId()), destinationBank);
				List<DayOperationFransaBank> dayOperations = new ArrayList<DayOperationFransaBank>();
				List<FileContent> contents = new ArrayList<FileContent>();
				for (FileContent element : dayOperation) {
					if (ExecutorThreadFransaBank.stopThreads ) {
						return;
					}
					int indexPieceComptable = getEveIndex1();
					DayOperationFransaBank G087 = new DayOperationFransaBank();
					DayOperationFransaBank G088 = new DayOperationFransaBank();
					DayOperationFransaBank G089 = new DayOperationFransaBank();
					DayOperationFransaBank G090 = new DayOperationFransaBank();
					DayOperationFransaBank G091 = new DayOperationFransaBank();
					DayOperationFransaBank G092 = new DayOperationFransaBank();
					DayOperationFransaBank G093 = new DayOperationFransaBank();

					dayopCreation(G087, element, "G087");
					dayopCreation(G088, element, "G088");
					dayopCreation(G089, element, "G089");
					dayopCreation(G090, element, "G090");
					dayopCreation(G091, element, "G091");
					dayopCreation(G092, element, "G092");
					dayopCreation(G093, element, "G093");
					String montantTransaction_DA = element.getMontantTransaction().replace(".", "");
					String montantTransaction_ML = montantTransaction_DA;
					float montant = Float.parseFloat(montantTransaction_ML);
					CommissionFransabankOnUs com = commissionFransabankOnUsRepository
							.findByTypeAchatInternetMin(element.getTypeTransaction());
					if (montant < Integer.parseInt(com.getMax())) {

						/////////////////// ********START G087 **********///////////////////////////

						float montantTransaction = Float.parseFloat(element.getMontantTransaction()) * fff;
						G087.setMontantSettlement(montantTransaction);

						G087.setPieceComptable("PA" + "163" + String.format("%06d", indexPieceComptable));

						///////////////// *********** END G087 ********////////////////////////////

						//////////////// **********START G088 ***********//////////////////////////

						TvaCommissionFransaBank tvaCommissionFransaBank = tvaRepo.findTva();
						float commission = Float.parseFloat(com.getCommissionFixe());

						G088.setMontantSettlement((commission));

						G088.setPieceComptable("PA" + "163" + String.format("%06d", indexPieceComptable));

						/////////////// **********END G088 *********///////////////////////////////

						//////////////// **********START G089 ***********//////////////////////////

						int tva = Integer.parseInt(tvaCommissionFransaBank.getTva());
						float fff = 100;
						float commssionTvaArr = ((commission * tva) / fff);

						G089.setMontantSettlement((commssionTvaArr));

						G089.setPieceComptable("PA" + "163" + String.format("%06d", indexPieceComptable));

						/////////////// **********END G089 *********///////////////////////////////

						///////////// **********START G090 ***********//////////////////////////

						G090.setMontantSettlement((montant));

						G090.setPieceComptable("PA" + "163" + String.format("%06d", indexPieceComptable));

						/////////////// **********END G090 *********///////////////////////////////

						//////////////// **********START G091***********//////////////////////////

						float commission_satim = Float.parseFloat(com.getCmi());

						G091.setMontantSettlement((commission_satim));

						G091.setPieceComptable("PA" + "163" + String.format("%06d", indexPieceComptable));

						/////////////// **********END G091 *********///////////////////////////////

						//////////////// **********START G092***********//////////////////////////

						float tva_cmi = ((commission_satim * tva) / fff);

						G092.setMontantSettlement((tva_cmi));

						G092.setPieceComptable("PA" + "163" + String.format("%06d", indexPieceComptable));

						/////////////// **********END G092 *********///////////////////////////////

						//////////////// **********START G093***********//////////////////////////

						float montant_satim = tva_cmi + commission_satim;

						G093.setMontantSettlement((montant_satim));

						G093.setPieceComptable("PA" + "163" + String.format("%06d", indexPieceComptable));

						/////////////// **********END G093 *********///////////////////////////////

						Map<String, DayOperationFransaBank> days = new HashMap<>();
						days.put("SATIM", G091);
						days.put("SATIMTVA", G092);
						correctiondayOperation(element, days);
						contents.add(element);

						dayOperations.add(G087);
						dayOperations.add(G088);
						dayOperations.add(G089);
						dayOperations.add(G090);
						dayOperations.add(G091);
						dayOperations.add(G092);
						dayOperations.add(G093);
					}
				}
				;
				tpList.addAll(contents);

				// saveInBatches(dayOperations, threadNB, contents);
				daysOperationsAll.addAll(dayOperations);
				logger.info("addG044_G047_AchatInternet_Onus_AutreAgence_MinMontant =>{}", dayOperations.size());

				batchRepo.updateFinishBatch("TH" + threadNB, 1, new Date());
			}

		} catch (Exception e) {
			catchError(e, threadNB);

		}

	}

	public void addG066_G073_Achat_Internet_offus_Issuer(String fileDate, int threadNB) {
		batchRepo.updateFinishBatch("TH" + threadNB, 0, new Date());
		try {
			int index = 0;
			FileHeader summary = new FileHeader();
			if (fileRepository.findByfileDate(fileDate).isPresent()) {
				index = index + 1;
				summary = fileRepository.findByfileDate(fileDate).get();
				String destinationBank = summary.getDestinationBankIdentification();
				List<FileContent> fileContentList = new ArrayList<FileContent>();
				List<FileContent> dayOperation = settlementRepo.getG066_G067_G068_G069_G070_G071_G072_G073(
						String.valueOf(summary.getId()), destinationBank, codeBanksInternet);
				List<DayOperationFransaBank> dayOperations = new ArrayList<DayOperationFransaBank>();
				List<FileContent> contents = new ArrayList<FileContent>();
				for (FileContent element : dayOperation) {
					if (ExecutorThreadFransaBank.stopThreads ) {
						return;
					}
					int indexPieceComptable = getEveIndex1();
					DayOperationFransaBank G066 = new DayOperationFransaBank();
					DayOperationFransaBank G067 = new DayOperationFransaBank();
					DayOperationFransaBank G068 = new DayOperationFransaBank();
					DayOperationFransaBank G069 = new DayOperationFransaBank();
					DayOperationFransaBank G070 = new DayOperationFransaBank();
					DayOperationFransaBank G071 = new DayOperationFransaBank();
					DayOperationFransaBank G072 = new DayOperationFransaBank();
					DayOperationFransaBank G073 = new DayOperationFransaBank();
					dayopCreation(G066, element, "G066");
					dayopCreation(G067, element, "G067");
					dayopCreation(G068, element, "G068");
					dayopCreation(G069, element, "G069");
					dayopCreation(G070, element, "G070");
					dayopCreation(G071, element, "G071");
					dayopCreation(G072, element, "G072");
					dayopCreation(G073, element, "G073");
					String pComptable = "PA" + "171" + String.format("%06d", indexPieceComptable);
					G066.setPieceComptable(pComptable);
					G067.setPieceComptable(pComptable);
					G070.setPieceComptable(pComptable);
					G071.setPieceComptable(pComptable);
					G068.setPieceComptable(pComptable);
					G072.setPieceComptable(pComptable);
					G073.setPieceComptable(pComptable);
					String montantTransaction_DA = element.getMontantTransaction().replace(".", "");
					String montantTransaction_ML = montantTransaction_DA;
					int montant = Integer.parseInt(montantTransaction_ML);

					CommissionAchatInternetFB com = commissionFransaBankInternetRepository
							.findByType(element.getTypeTransaction());

					float fff = 100;
					float pourcentage_com = Float.parseFloat(com.getValeurVarivable());
					float commission = ((montant * pourcentage_com) / fff);

					TvaCommissionFransaBank tvaCommissionFransaBank = tvaRepo.findTva();

					float commissionInf = Float.parseFloat(com.getValeurMin());
					float commissionSup = Float.parseFloat(com.getValeurMax());

					int tva = Integer.parseInt(tvaCommissionFransaBank.getTva());

					float commssionTvaInf = (((commissionInf) * tva) / fff);
					float commssionTvaSup = (((commissionSup) * tva) / fff);
					float commssionTva = ((commission * tva) / fff);

					float pourcenatgeCmi = Integer.parseInt(com.getVariableCmi()) / fff;
					float cmi = (pourcenatgeCmi * (commission));
					float interchange = (pourcenatgeCmi * (commission))
							+ ((commission * Float.parseFloat(com.getVariableCommissionAcq())) / fff);
					float interchangeCommission = 0;
					float pourcentageAcq = Integer.parseInt(com.getVariableCommissionAcq()) / fff;

					/////// for inf ////////////////
					float commissionAcqInf = ((pourcentageAcq * (commissionInf) + pourcenatgeCmi * (commissionInf)));
					float commssionTvaAccqMin = ((commissionAcqInf) * tva) / fff;
					float montant_a_verserInf = montant - commissionAcqInf - commssionTvaAccqMin;

					////// for sup ////////////////

					float commissionAcqSup = ((pourcentageAcq * (commissionSup) + pourcenatgeCmi * (commissionSup)));
					float commssionTvaAccqSup = ((commissionAcqSup) * tva / fff);
					float montant_a_verserSup = montant - commissionAcqSup - commssionTvaAccqSup;

					////// for between /////////////
					int commissionTTC = Math.round(commission * (1 + tva));
					int tvaCommercant = Math.round(commission * tva / fff);

					int commissionHT = commissionTTC - tvaCommercant;
					float commissionAcq = (pourcentageAcq * (commission) + pourcenatgeCmi * (commission));
					float commssionTvaAccq = (((commissionAcq) * tva) / fff);

					float montant_a_verser = montant - commissionAcq - commssionTvaAccq;

					if (commission <= Integer.parseInt(com.getValeurMin())) {

						G067.setMontantSettlement((commissionInf));

						G068.setMontantSettlement((commssionTvaInf));
						G071.setMontantSettlement((commissionAcqInf));
						G072.setMontantSettlement((commssionTvaAccqMin));
						G073.setMontantSettlement((montant_a_verserInf));
						interchangeCommission = commissionAcqInf + commssionTvaAccqMin;

					} else if (commission >= Integer.parseInt(com.getValeurMax())) {

						G067.setMontantSettlement((commissionSup));
						G068.setMontantSettlement((commssionTvaSup));
						G071.setMontantSettlement((commissionAcqSup));
						G072.setMontantSettlement((commssionTvaAccqSup));
						G073.setMontantSettlement((montant_a_verserSup));
						interchangeCommission = commissionAcqSup + commssionTvaAccqSup;

					} else if (commission > Integer.parseInt(com.getValeurMin())
							&& commission < Integer.parseInt(com.getValeurMax())) {

						G067.setMontantSettlement((commissionHT));
						G068.setMontantSettlement((tvaCommercant));
						G071.setMontantSettlement((commissionAcq));
						G072.setMontantSettlement((commssionTvaAccq));
						G073.setMontantSettlement((montant_a_verser));
						Map<String, DayOperationFransaBank> days = new HashMap<>();

						interchangeCommission = commissionAcq + commssionTvaAccq;

					}

					//////////////// ************ VARIABLES
					//////////////// *************************//////////////////////////////
					float montantTransaction = Float.parseFloat(element.getMontantTransaction()) * fff;
					float commission_cpi = Float.parseFloat(com.getFixCpi());
					float tva_cpi = ((commission_cpi * tva) / fff);

					////////////// ****** START MONTANT SETTLEMNT
					////////////// *********************/////////////////////////
					G066.setMontantSettlement((montantTransaction));
					G069.setMontantSettlement((commission_cpi));
					G070.setMontantSettlement((tva_cpi));

					contents.add(element);
					Map<String, DayOperationFransaBank> days = new HashMap<>();
					days.put("Interchange", G071);
					days.put("tvaInterchange", G072);

					correctiondayOperation(element, days);

					dayOperations.add(G066);
					dayOperations.add(G067);
					dayOperations.add(G068);
					dayOperations.add(G069);
					dayOperations.add(G070);
					dayOperations.add(G071);
					dayOperations.add(G072);
					dayOperations.add(G073);
				}
				;
				tpList.addAll(contents);

				// saveInBatches(dayOperations, threadNB, contents);
				daysOperationsAll.addAll(dayOperations);
				logger.info("addG066_G073_Achat_Internet_offus_Issuer =>{}", dayOperations.size());

				batchRepo.updateFinishBatch("TH" + threadNB, 1, new Date());

			}

		} catch (Exception e) {
			catchError(e, threadNB);

		}

	}

	public void addG074_G080_AchatInternet_offus_Acq(String fileDate, int threadNB) {
		batchRepo.updateFinishBatch("TH" + threadNB, 0, new Date());
		try {
			int index = 0;
			FileHeader summary = new FileHeader();
			if (fileRepository.findByfileDate(fileDate).isPresent()) {
				index = index + 1;
				summary = fileRepository.findByfileDate(fileDate).get();
				String destinationBank = summary.getDestinationBankIdentification();
				List<FileContent> fileContentList = new ArrayList<FileContent>();
				List<FileContent> dayOperation = settlementRepo.getG074_G075_G076_G077_G078_G079_G080(
						String.valueOf(summary.getId()), destinationBank, codeBanksInternet);
				List<DayOperationFransaBank> dayOperations = new ArrayList<DayOperationFransaBank>();
				List<FileContent> contents = new ArrayList<FileContent>();
				for (FileContent element : dayOperation) {
					if (ExecutorThreadFransaBank.stopThreads ) {
						return;
					}
					int indexPieceComptable = getEveIndex1();
					DayOperationFransaBank G074 = new DayOperationFransaBank();
					DayOperationFransaBank G075 = new DayOperationFransaBank();
					DayOperationFransaBank G076 = new DayOperationFransaBank();
					DayOperationFransaBank G077 = new DayOperationFransaBank();
					DayOperationFransaBank G078 = new DayOperationFransaBank();
					DayOperationFransaBank G079 = new DayOperationFransaBank();
					DayOperationFransaBank G080 = new DayOperationFransaBank();
					dayopCreation(G074, element, "G074");
					dayopCreation(G075, element, "G075");
					dayopCreation(G076, element, "G076");
					dayopCreation(G077, element, "G077");
					dayopCreation(G078, element, "G078");
					dayopCreation(G079, element, "G079");
					dayopCreation(G080, element, "G080");
					String pComptable = "PA" + "164" + String.format("%06d", indexPieceComptable);
					G075.setPieceComptable(pComptable);
					G074.setPieceComptable(pComptable);
					G076.setPieceComptable(pComptable);
					G077.setPieceComptable(pComptable);
					G078.setPieceComptable(pComptable);
					G079.setPieceComptable(pComptable);
					G080.setPieceComptable(pComptable);

					String montantTransaction_DA = element.getMontantTransaction().replace(".", "");
					String montantTransaction_ML = montantTransaction_DA;
					int montant = Integer.parseInt(montantTransaction_ML);

					/////////////////// ********START G074 **********///////////////////////////
					float montantTransaction = new BigDecimal(element.getMontantTransaction()).multiply(new BigDecimal(fff)).floatValue() ;
					G074.setMontantSettlement((montantTransaction));
					///////////////// *********** END G074 ********////////////////////////////

					//////////////// **********START G075 ***********//////////////////////////
					FileRequest.print(" " + element.getTypeTransaction(), FileRequest.getLineNumber());
					CommissionAchatInternetFB com = commissionFransaBankInternetRepository
							.findByType(element.getTypeTransaction());
					float fff = 100;
					float pourcentage = Float.parseFloat(com.getValeurVarivable());
					float commission = ((montant * pourcentage) / fff);
					float commissionAcq = ((commission * Float.parseFloat(com.getVariableCommissionAcq())) / fff);
					float pourcenatgeCmi = Integer.parseInt(com.getVariableCmi()) / fff;
					float interchange = (pourcenatgeCmi * (commission)) + commissionAcq;

					TvaCommissionFransaBank tvaCommissionFransaBank = tvaRepo.findTva();
					String commissionInf = com.getValeurMin();
					String commissionSup = com.getValeurMax();
					int tva = Integer.parseInt(tvaCommissionFransaBank.getTva());
					float commssionTvaInf = ((Integer.valueOf(commissionInf) * tva) / fff);
					float commssionTvaSup = ((Integer.valueOf(commissionSup) * tva) / fff);

					float commssionTva = ((commission * tva) / fff);
					float commssionTvaAcq = ((interchange * tva) / fff);
					//////////// new Inf////////////////
					float commissionAcqInf = ((Integer.parseInt(commissionInf)
							* Float.parseFloat(com.getVariableCommissionAcq())) / fff);
					float interchangeInf = (pourcenatgeCmi * Integer.valueOf(commissionInf)) + commissionAcqInf;
					float commssionTvaInterchangeInf = (((interchangeInf) * tva) / fff);
					float cmiInf = ((pourcenatgeCmi * Integer.parseInt(commissionInf)));
					float tvaCmiInf = (((cmiInf) * tva) / fff);
					float montant_a_verserInf = montant - interchangeInf - commssionTvaInterchangeInf;
					float montant_satim_a_verserInf = cmiInf + tvaCmiInf;

					///////////// new sup ///////////
					float commissionAcqSup = ((Integer.parseInt(commissionSup)
							* Float.parseFloat(com.getVariableCommissionAcq())) / fff);
					float interchangeSup = (pourcenatgeCmi * Integer.valueOf(commissionSup)) + commissionAcqSup;
					float commssionTvaInterchangeSup = (((interchangeSup) * tva) / fff);
					float cmiSup = ((pourcenatgeCmi * Integer.parseInt(commissionSup)));

					float tvaCmiSup = (((cmiSup) * tva) / fff);
					float montant_a_verserSup = montant - interchangeSup - commssionTvaInterchangeSup;
					float montant_satim_a_verserSup = cmiSup + tvaCmiSup;
					/// new between ///////////////////
					float cmi = (pourcenatgeCmi * (commission));
					float tvaCmi = (((cmi) * tva) / fff);
					float montant_a_verser = montant - interchange - commssionTvaAcq;
					float montant_satim_a_verser = cmi + tvaCmi;
					float interchangeCommission = 0;
					float satimCommission = 0;
					if (commission <= Integer.parseInt(com.getValeurMin())) {
						G075.setMontantSettlement((interchangeInf));
						G076.setMontantSettlement((commssionTvaInterchangeInf));
						G077.setMontantSettlement((cmiInf));
						G078.setMontantSettlement((tvaCmiInf));
						G079.setMontantSettlement((montant_a_verserInf));
						G080.setMontantSettlement((montant_satim_a_verserInf));
						Map<String, DayOperationFransaBank> days = new HashMap<>();
						days.put("SATIM", G077);
						days.put("SATIMTVA", G078);
						days.put("Interchange", G075);
						days.put("tvaInterchange", G076);

						correctiondayOperation(element, days);
						interchangeCommission = interchangeInf + commssionTvaInterchangeInf;
						satimCommission = montant_satim_a_verserInf;
					} else if (commission >= Integer.parseInt(com.getValeurMax())) {

						int lengh = String.valueOf(interchangeSup).length();
						G075.setMontantSettlement((interchangeSup));
						G076.setMontantSettlement((commssionTvaInterchangeSup));
						G077.setMontantSettlement((cmiSup));
						G078.setMontantSettlement((tvaCmiSup));
						G079.setMontantSettlement((montant_a_verserSup));
						G080.setMontantSettlement((montant_satim_a_verserSup));
						Map<String, DayOperationFransaBank> days = new HashMap<>();
						days.put("SATIM", G077);
						days.put("SATIMTVA", G078);
						days.put("Interchange", G075);
						days.put("tvaInterchange", G076);

						correctiondayOperation(element, days);
						interchangeCommission = interchangeSup + commssionTvaInterchangeSup;
						satimCommission = montant_satim_a_verserSup;
					} else if (commission > Integer.parseInt(com.getValeurMin())
							&& commission < Integer.parseInt(com.getValeurMax())) {

						G075.setMontantSettlement((interchange));
						G076.setMontantSettlement((commssionTvaAcq));
						G077.setMontantSettlement((cmi));
						G078.setMontantSettlement((tvaCmi));
						G079.setMontantSettlement((montant_a_verser));
						G080.setMontantSettlement((montant_satim_a_verser));
						Map<String, DayOperationFransaBank> days = new HashMap<>();
						days.put("SATIM", G077);
						days.put("SATIMTVA", G078);
						days.put("Interchange", G075);
						days.put("tvaInterchange", G076);

						correctiondayOperation(element, days);
						interchangeCommission = interchange + commssionTvaAcq;
						satimCommission = montant_satim_a_verser;

					}

					G074.setCodeAgence(element.getNumRIBEmetteur().substring(3, 8));
					G075.setCodeAgence(element.getNumRIBEmetteur().substring(3, 8));
					G076.setCodeAgence(element.getNumRIBEmetteur().substring(3, 8));
					G077.setCodeAgence(element.getNumRIBEmetteur().substring(3, 8));
					G078.setCodeAgence(element.getNumRIBEmetteur().substring(3, 8));
					G079.setCodeAgence(element.getNumRIBEmetteur().substring(3, 8));
					G080.setCodeAgence(element.getNumRIBEmetteur().substring(3, 8));

					////////////// ******* END G080 ****************/////////////////////////////

					contents.add(element);

					dayOperations.add(G074);
					dayOperations.add(G075);
					dayOperations.add(G076);
					dayOperations.add(G077);
					dayOperations.add(G078);
					dayOperations.add(G079);
					dayOperations.add(G080);
				}
				;
				tpList.addAll(contents);

				// saveInBatches(dayOperations, threadNB, contents);
				daysOperationsAll.addAll(dayOperations);
				logger.info("addG074_G080_AchatInternet_offus_Acq =>{}", dayOperations.size());

				batchRepo.updateFinishBatch("TH" + threadNB, 1, new Date());
			}

		} catch (Exception e) {
			catchError(e, threadNB);

		}

	}

	public void addG066_G073_Achat_Internet_offus_IssuerBanqueException(String fileDate, int threadNB) {
		batchRepo.updateFinishBatch("TH" + threadNB, 0, new Date());
		try {
			int index = 0;
			FileHeader summary = new FileHeader();
			if (fileRepository.findByfileDate(fileDate).isPresent()) {
				index = index + 1;
				summary = fileRepository.findByfileDate(fileDate).get();
				String destinationBank = summary.getDestinationBankIdentification();
				List<FileContent> fileContentList = new ArrayList<FileContent>();

				List<FileContent> dayOperation = settlementRepo.getG178_G179_G180_G181_G182_G183_G184_G185(
						String.valueOf(summary.getId()), destinationBank, codeBanksInternet);
				List<DayOperationFransaBank> dayOperations = new ArrayList<DayOperationFransaBank>();
				List<FileContent> contents = new ArrayList<FileContent>();
				for (FileContent element : dayOperation) {
					if (ExecutorThreadFransaBank.stopThreads ) {
						return;
					}
					int indexPieceComptable = getEveIndex1();
					DayOperationFransaBank G178 = new DayOperationFransaBank();
					DayOperationFransaBank G179 = new DayOperationFransaBank();
					DayOperationFransaBank G180 = new DayOperationFransaBank();
					DayOperationFransaBank G181 = new DayOperationFransaBank();
					DayOperationFransaBank G182 = new DayOperationFransaBank();
					DayOperationFransaBank G183 = new DayOperationFransaBank();
					DayOperationFransaBank G184 = new DayOperationFransaBank();
					DayOperationFransaBank G185 = new DayOperationFransaBank();
					dayopCreation(G178, element, "G178");
					dayopCreation(G179, element, "G179");
					dayopCreation(G180, element, "G180");
					dayopCreation(G181, element, "G181");
					dayopCreation(G182, element, "G182");
					dayopCreation(G183, element, "G183");
					dayopCreation(G184, element, "G184");
					dayopCreation(G185, element, "G185");
					String montantTransaction_DA = element.getMontantTransaction().replace(".", "");
					String montantTransaction_ML = montantTransaction_DA;
					int montant = Integer.parseInt(montantTransaction_ML);
/////////////////// ALP/FSBK ////////////
					CommissionAchatInternetFB com = commissionFransaBankInternetRepository
							.findByTypeIssuer(element.getTypeTransaction(), element.getCodeBank());

					/////// ********* START PIECE COMPTABLE **************/////////////////////////
					G178.setPieceComptable("PA" + "171" + String.format("%06d", indexPieceComptable));
					G179.setPieceComptable("PA" + "171" + String.format("%06d", indexPieceComptable));
					G180.setPieceComptable("PA" + "171" + String.format("%06d", indexPieceComptable));
					G181.setPieceComptable("PA" + "171" + String.format("%06d", indexPieceComptable));
					G182.setPieceComptable("PA" + "171" + String.format("%06d", indexPieceComptable));
					G183.setPieceComptable("PA" + "171" + String.format("%06d", indexPieceComptable));
					G184.setPieceComptable("PA" + "171" + String.format("%06d", indexPieceComptable));
					G185.setPieceComptable("PA" + "171" + String.format("%06d", indexPieceComptable));

					////// ***** VARIABLES *************** /////////

					float montantTransaction = Float.parseFloat(element.getMontantTransaction()) * fff;
					float fff = 100;
					float pourcentage_com = Float.parseFloat(com.getValeurVarivable());
					float commission = ((montant * pourcentage_com) / fff);

					TvaCommissionFransaBank tvaCommissionFransaBank = tvaRepo.findTva();

					float commissionInf = Float.parseFloat(com.getValeurMin());
					float commissionSup = Float.parseFloat(com.getValeurMax());
					int tva = Integer.parseInt(tvaCommissionFransaBank.getTva());

					float commssionTvaInf = (((commissionInf) * tva) / fff);
					float commssionTvaSup = (((commissionSup) * tva) / fff);
					float commssionTva = ((commission * tva) / fff);
					int pourcentageAcq = Integer.parseInt(com.getVariableCommissionAcq());

					float pourcenatgeCmi = Integer.parseInt(com.getVariableCmi()) / fff;
					float cmi = (pourcenatgeCmi * (commission));
					float interchange = (pourcenatgeCmi * (commission))
							+ ((commission * Float.parseFloat(com.getVariableCommissionAcq())) / fff);
					////////// for min ///////////////
					float commissionAcqInf = ((pourcentageAcq * (commissionInf) / fff)
							+ (pourcenatgeCmi * (commissionInf)));
					float commssionTvaAccqMin = (((commissionAcqInf) * tva) / fff);
					float montant_a_verserInf = montant - commissionAcqInf - commssionTvaAccqMin;
					///////// for max ///////////////
					float commissionAcqSup = ((pourcentageAcq * (commissionSup) / fff)
							+ (pourcenatgeCmi * (commissionSup)));
					float commssionTvaAccqSup = (((commissionAcqSup) * tva) / fff);
					float montant_a_verserSup = montant - commissionAcqSup - commssionTvaAccqSup;
					///////// for betwwen ////////////////
					float commissionAcq = (pourcentageAcq * (commission) / fff) + (pourcenatgeCmi * (commission));
					;
					float commssionTvaAccq = (((commissionAcq) * tva) / fff);
					float montant_a_verser = montant - commissionAcq - commssionTvaAccqMin;

					float interchangeCommission = 0;
					float commission_cpi = Float.parseFloat(com.getFixCpi());
					float tva_cpi = ((commission_cpi * tva) / fff);
					////// *********** START MONTANT SETTLEMENT *****************/////////
					G178.setMontantSettlement((montantTransaction));

					G181.setMontantSettlement((commission_cpi));

					G182.setMontantSettlement((tva_cpi));

					///////////////// *********** END G178 ********////////////////////////////

					//////////////// **********START G067 ***********//////////////////////////

					if (commission <= Integer.parseInt(com.getValeurMin())) {
						G179.setMontantSettlement((commissionInf));
						G180.setMontantSettlement((commssionTvaInf));
						G183.setMontantSettlement((commissionAcqInf));
						G184.setMontantSettlement((commssionTvaAccqMin));
						G185.setMontantSettlement((montant_a_verserInf));
						interchangeCommission = commissionAcqInf + commssionTvaAccqMin;
					} else if (commission >= Integer.parseInt(com.getValeurMax())) {

						G179.setMontantSettlement((commissionSup));
						G180.setMontantSettlement((commssionTvaSup));
						G183.setMontantSettlement((commissionAcqSup));
						G184.setMontantSettlement((commssionTvaAccqSup));
						G185.setMontantSettlement((montant_a_verserSup));
						interchangeCommission = commissionAcqSup + commssionTvaAccqSup;
					} else if (commission > Integer.parseInt(com.getValeurMin())
							&& commission < Integer.parseInt(com.getValeurMax())) {

						G179.setMontantSettlement((commission));
						G180.setMontantSettlement((commssionTva));
						G183.setMontantSettlement((commissionAcq));
						G184.setMontantSettlement((commssionTvaAccq));
						G185.setMontantSettlement((montant_a_verser));

						interchangeCommission = commissionAcq + commssionTvaAccq;

					}

					Map<String, DayOperationFransaBank> days = new HashMap<>();
					days.put("Interchange", G183);
					days.put("tvaInterchange", G184);

					correctiondayOperation(element, days);
					contents.add(element);
					dayOperations.add(G178);
					dayOperations.add(G179);
					dayOperations.add(G180);
					dayOperations.add(G181);
					dayOperations.add(G182);
					dayOperations.add(G183);
					dayOperations.add(G184);
					dayOperations.add(G185);
				}
				;
				tpList.addAll(contents);

				// saveInBatches(dayOperations, threadNB, contents);
				daysOperationsAll.addAll(dayOperations);
				logger.info("addG066_G073_Achat_Internet_offus_IssuerBanqueException =>{}", dayOperations.size());

				batchRepo.updateFinishBatch("TH" + threadNB, 1, new Date());
			}

		} catch (Exception e) {
			catchError(e, threadNB);

		}

	}

	public void addG074_G080_AchatInternet_offus_AcqBanqueException(String fileDate, int threadNB) {
		batchRepo.updateFinishBatch("TH" + threadNB, 0, new Date());
		try {
			int index = 0;
			FileHeader summary = new FileHeader();
			if (fileRepository.findByfileDate(fileDate).isPresent()) {
				index = index + 1;
				summary = fileRepository.findByfileDate(fileDate).get();
				String destinationBank = summary.getDestinationBankIdentification();
				List<FileContent> fileContentList = new ArrayList<FileContent>();
				List<FileContent> dayOperation = settlementRepo.getG186_G187_G188_G189_G190_G191_G192(
						String.valueOf(summary.getId()), destinationBank, codeBanksInternet);
				List<DayOperationFransaBank> dayOperations = new ArrayList<DayOperationFransaBank>();
				List<FileContent> contents = new ArrayList<FileContent>();
				for (FileContent element : dayOperation) {
					if (ExecutorThreadFransaBank.stopThreads ) {
						return;
					}
					int indexPieceComptable = getEveIndex1();
					DayOperationFransaBank G186 = new DayOperationFransaBank();
					DayOperationFransaBank G187 = new DayOperationFransaBank();
					DayOperationFransaBank G188 = new DayOperationFransaBank();
					DayOperationFransaBank G189 = new DayOperationFransaBank();
					DayOperationFransaBank G190 = new DayOperationFransaBank();
					DayOperationFransaBank G191 = new DayOperationFransaBank();
					DayOperationFransaBank G192 = new DayOperationFransaBank();
					dayopCreation(G186, element, "G186");
					dayopCreation(G187, element, "G187");
					dayopCreation(G188, element, "G188");
					dayopCreation(G189, element, "G189");
					dayopCreation(G190, element, "G190");
					dayopCreation(G191, element, "G191");
					dayopCreation(G192, element, "G192");
					//////////////// ********** START PIECE COMPTABLE ***********/////////////////

					G186.setPieceComptable("PA" + "164" + String.format("%06d", indexPieceComptable));
					G187.setPieceComptable("PA" + "164" + String.format("%06d", indexPieceComptable));
					G188.setPieceComptable("PA" + "164" + String.format("%06d", indexPieceComptable));
					G189.setPieceComptable("PA" + "164" + String.format("%06d", indexPieceComptable));
					G190.setPieceComptable("PA" + "164" + String.format("%06d", indexPieceComptable));
					G191.setPieceComptable("PA" + "164" + String.format("%06d", indexPieceComptable));
					G192.setPieceComptable("PA" + "164" + String.format("%06d", indexPieceComptable));
					///////////////// ********** START CODE AGENCE *************/////////////////
					G186.setCodeAgence(element.getNumRIBEmetteur().substring(3, 8));
					G187.setCodeAgence(element.getNumRIBEmetteur().substring(3, 8));
					G188.setCodeAgence(element.getNumRIBEmetteur().substring(3, 8));
					G189.setCodeAgence(element.getNumRIBEmetteur().substring(3, 8));
					G190.setCodeAgence(element.getNumRIBEmetteur().substring(3, 8));
					G191.setCodeAgence(element.getNumRIBEmetteur().substring(3, 8));
					G192.setCodeAgence(element.getNumRIBEmetteur().substring(3, 8));

					String montantTransaction_DA = element.getMontantTransaction().replace(".", "");
					String montantTransaction_ML = montantTransaction_DA;
					int montant = Integer.parseInt(montantTransaction_ML);

					/////////// ********* VARIABLES AND MONTANT SETTLEMENT *************////////////

					float montantTransaction = Float.parseFloat(element.getMontantTransaction()) * fff;
/////////////// FSBK/ALP//////////////////////
					CommissionAchatInternetFB com = commissionFransaBankInternetRepository
							.findByTypeAcq(element.getTypeTransaction(), element.getCodeBankAcquereur());
					float fff = 100;
					float pourcentage = Float.parseFloat(com.getValeurVarivable());
					float commission = ((montant * pourcentage) / fff);
					float commissionAcq = ((commission * Float.parseFloat(com.getVariableCommissionIssuer())) / fff);
					TvaCommissionFransaBank tvaCommissionFransaBank = tvaRepo.findTva();
					float pourcenatgeCmi = Integer.parseInt(com.getVariableCmi()) / fff;
					float interchange = (pourcenatgeCmi * (commission)) + commissionAcq;
					String commissionInf = com.getValeurMin();
					String commissionSup = com.getValeurMax();
					int tva = Integer.parseInt(tvaCommissionFransaBank.getTva());
					float commssionTvaInf = ((Integer.valueOf(commissionInf) * tva) / fff);
					float commssionTvaSup = ((Integer.valueOf(commissionSup) * tva) / fff);
					float commssionTva = ((commission * tva) / fff);
					float commssionTvaAcq = ((interchange * tva) / fff);
					float commissionAcqInf = ((Integer.parseInt(commissionInf)
							* Float.parseFloat(com.getVariableCommissionIssuer())) / fff);
					float interchangeInf = (pourcenatgeCmi * Integer.valueOf(commissionInf)) + commissionAcqInf;
					float commssionTvaInterchangeInf = (((interchangeInf) * tva) / fff);
					float commissionAcqSup = ((Integer.parseInt(commissionSup)
							* Float.parseFloat(com.getVariableCommissionIssuer())) / fff);
					float interchangeSup = (pourcenatgeCmi * Integer.valueOf(commissionSup)) + commissionAcqSup;
					float commssionTvaInterchangeSup = (((interchangeSup) * tva) / fff);
					////////// for inf //////////////////////
					float cmiInf = (pourcenatgeCmi * Integer.parseInt(commissionInf));
					float tvaCmiInf = (((cmiInf) * tva) / fff);
					float montant_a_verserInf = montant - interchangeInf - commssionTvaInterchangeInf;
					float montant_satim_a_verserInf = cmiInf + tvaCmiInf;
					////////// for sup ///////////
					float cmiSup = (pourcenatgeCmi * Integer.parseInt(commissionSup));
					float tvaCmiSup = (((cmiSup) * tva) / fff);
					float montant_a_verserSup = montant - interchangeSup - commssionTvaInterchangeSup;
					float montant_satim_a_verserSup = tvaCmiSup + cmiSup;
					////////// for between //////////////

					float cmi = (pourcenatgeCmi * (commission));
					float tvaCmi = (((cmi) * tva) / fff);
					float montant_a_verser = montant - interchange - commssionTvaAcq;
					float montant_satim_a_verser = cmi + tvaCmi;
					float interchangeCommission = 0;
					float satimCommission = 0;
					if (commission <= Integer.parseInt(com.getValeurMin())) {

						G187.setMontantSettlement((interchangeInf));
						G188.setMontantSettlement((commssionTvaInterchangeInf));
						G189.setMontantSettlement((cmiInf));
						G190.setMontantSettlement((tvaCmiInf));
						G191.setMontantSettlement((montant_a_verserInf));
						G192.setMontantSettlement((montant_satim_a_verserInf));
						interchangeCommission = interchangeInf + commssionTvaInterchangeInf;
						satimCommission = montant_satim_a_verserInf;

					} else if (commission >= Integer.parseInt(com.getValeurMax())) {

						G189.setMontantSettlement((cmiSup));

						G187.setMontantSettlement((interchangeSup));
						G188.setMontantSettlement((commssionTvaInterchangeSup));
						G190.setMontantSettlement((tvaCmiSup));
						G191.setMontantSettlement((montant_a_verserSup));
						G192.setMontantSettlement((montant_satim_a_verserSup));
						interchangeCommission = interchangeSup + commssionTvaInterchangeSup;
						satimCommission = montant_satim_a_verserSup;
					} else if (commission > Integer.parseInt(com.getValeurMin())
							&& commission < Integer.parseInt(com.getValeurMax())) {

						G187.setMontantSettlement((interchange));
						G188.setMontantSettlement(commssionTvaAcq);
						G189.setMontantSettlement((cmi));
						G190.setMontantSettlement((tvaCmi));
						G191.setMontantSettlement((montant_a_verser));
						G192.setMontantSettlement((montant_satim_a_verser));

						interchangeCommission = interchange + commssionTvaAcq;
						satimCommission = montant_satim_a_verser;

					}

					G186.setMontantSettlement((montantTransaction));
					Map<String, DayOperationFransaBank> days = new HashMap<>();
					days.put("SATIM", G189);
					days.put("SATIMTVA", G190);
					days.put("Interchange", G187);
					days.put("tvaInterchange", G188);
					correctiondayOperation(element, days);
					contents.add(element);
					dayOperations.add(G186);
					dayOperations.add(G187);
					dayOperations.add(G188);
					dayOperations.add(G189);
					dayOperations.add(G190);
					dayOperations.add(G191);
					dayOperations.add(G192);

				}
				;
				tpList.addAll(contents);

				// saveInBatches(dayOperations, threadNB, contents);
				daysOperationsAll.addAll(dayOperations);
				logger.info("addG074_G080_AchatInternet_offus_AcqBanqueException =>{}", dayOperations.size());

				batchRepo.updateFinishBatch("TH" + threadNB, 1, new Date());

			}

		} catch (Exception e) {
			catchError(e, threadNB);

		}

	}
	////////////// *************************** REMBOURSSEMENT SUR TPE
	////////////// ******************///////////////////////////////

	public void addG103_G107_rembourssement_onUs_MemeAgence(String fileDate, int threadNB) {
		batchRepo.updateFinishBatch("TH" + threadNB, 0, new Date());
		try {
			int index = 0;
			FileHeader summary = new FileHeader();
			if (fileRepository.findByfileDate(fileDate).isPresent()) {
				index = index + 1;
				summary = fileRepository.findByfileDate(fileDate).get();
				String destinationBank = summary.getDestinationBankIdentification();
				List<FileContent> fileContentList = new ArrayList<FileContent>();
				List<FileContent> dayOperation = settlementRepo
						.getG103_G104_G105_G106_G107(String.valueOf(summary.getId()), destinationBank);
				List<DayOperationFransaBank> dayOperations = new ArrayList<DayOperationFransaBank>();
				List<FileContent> contents = new ArrayList<FileContent>();
				for (FileContent element : dayOperation) {
					if (ExecutorThreadFransaBank.stopThreads ) {
						return;
					}
					int indexPieceComptable = getEveIndex1();
					DayOperationFransaBank G103 = new DayOperationFransaBank();
					DayOperationFransaBank G104 = new DayOperationFransaBank();
					DayOperationFransaBank G105 = new DayOperationFransaBank();
					DayOperationFransaBank G106 = new DayOperationFransaBank();
					DayOperationFransaBank G107 = new DayOperationFransaBank();
					dayopCreation(G103, element, "G103");
					dayopCreation(G104, element, "G104");
					dayopCreation(G105, element, "G105");
					dayopCreation(G106, element, "G106");
					dayopCreation(G107, element, "G107");
					/////////////////// ********START G103**********///////////////////////////

					float montantTransaction = Float.parseFloat(element.getMontantTransaction()) * fff;

					G103.setMontantSettlement(montantTransaction);
					G103.setPieceComptable("RB" + "***" + String.format("%06d", indexPieceComptable));

					///////////////// *********** END G103********////////////////////////////

					//////////////// **********START G104***********//////////////////////////

					CommissionFransabankOnUs com = commissionFransabankOnUsRepository
							.findByType(element.getTypeTransaction());

					TvaCommissionFransaBank tvaCommissionFransaBank = tvaRepo.findTva();
					float commission = Float.parseFloat(com.getCommissionFixe());

					G104.setMontantSettlement((commission));

					G104.setPieceComptable("RB" + "***" + String.format("%06d", indexPieceComptable));

					/////////////// **********END G104*********///////////////////////////////

					//////////////// **********START G105***********//////////////////////////

					int tva = Integer.parseInt(tvaCommissionFransaBank.getTva());
					float fff = 100;
					float commssionTvaArr = ((commission * tva) / fff);

					G105.setMontantSettlement((commssionTvaArr));

					G105.setPieceComptable("RB" + "***" + String.format("%06d", indexPieceComptable));

					/////////////// **********END G105*********///////////////////////////////

					//////////////// **********START G106***********//////////////////////////

					float commission_satim = Float.parseFloat(com.getCmi());

					G106.setMontantSettlement((commission_satim));

					G106.setPieceComptable("RB" + "***" + String.format("%06d", indexPieceComptable));

					/////////////// **********END G106*********///////////////////////////////

					//////////////// **********START G107***********//////////////////////////

					float tva_cmi = ((commission_satim * tva) / fff);

					G107.setMontantSettlement((tva_cmi));

					G107.setPieceComptable("RB" + "***" + String.format("%06d", indexPieceComptable));

					/////////////// **********END G107 *********///////////////////////////////

					float satim = commission_satim + tva_cmi;
					Map<String, DayOperationFransaBank> days = new HashMap<>();
					days.put("SATIM", G106);
					days.put("SATIMTVA", G107);
					correctiondayOperation(element, days);
					contents.add(element);
					contents.add(element);
					dayOperations.add(G103);
					dayOperations.add(G104);
					dayOperations.add(G105);
					dayOperations.add(G106);
					dayOperations.add(G107);

				}
				;
				tpList.addAll(contents);

				daysOperationsAll.addAll(dayOperations);
				logger.info("addG103_G107_rembourssement_onUs_MemeAgence =>{}", dayOperations.size());

				// saveInBatches(dayOperations, threadNB, contents);

				batchRepo.updateFinishBatch("TH" + threadNB, 1, new Date());

			}

		} catch (Exception e) {
			catchError(e, threadNB);

		}

	}

	public void addG108_G113_rembourssement_onUs_AutreAgence(String fileDate, int threadNB) {
		batchRepo.updateFinishBatch("TH" + threadNB, 0, new Date());
		try {
			int index = 0;
			FileHeader summary = new FileHeader();
			if (fileRepository.findByfileDate(fileDate).isPresent()) {
				index = index + 1;
				summary = fileRepository.findByfileDate(fileDate).get();
				String destinationBank = summary.getDestinationBankIdentification();
				List<FileContent> fileContentList = new ArrayList<FileContent>();
				List<FileContent> dayOperation = settlementRepo
						.getG108_G109_G110_G111_G112_G113(String.valueOf(summary.getId()), destinationBank);
				List<DayOperationFransaBank> dayOperations = new ArrayList<DayOperationFransaBank>();
				List<FileContent> contents = new ArrayList<FileContent>();
				for (FileContent element : dayOperation) {
					if (ExecutorThreadFransaBank.stopThreads ) {
						return;
					}
					int indexPieceComptable = getEveIndex1();
					DayOperationFransaBank G108 = new DayOperationFransaBank();
					DayOperationFransaBank G109 = new DayOperationFransaBank();
					DayOperationFransaBank G110 = new DayOperationFransaBank();
					DayOperationFransaBank G111 = new DayOperationFransaBank();
					DayOperationFransaBank G112 = new DayOperationFransaBank();
					DayOperationFransaBank G113 = new DayOperationFransaBank();
					dayopCreation(G108, element, "G108");
					dayopCreation(G109, element, "G109");
					dayopCreation(G110, element, "G110");
					dayopCreation(G111, element, "G111");
					dayopCreation(G112, element, "G112");
					dayopCreation(G113, element, "G113");
					/////////////////// ********START G108**********///////////////////////////

					float montantTransaction = Float.parseFloat(element.getMontantTransaction()) * fff;

					G108.setMontantSettlement(montantTransaction);

					G108.setPieceComptable("RB" + "***" + String.format("%06d", indexPieceComptable));

					///////////////// *********** END G108********////////////////////////////

					//////////////// **********START G109***********//////////////////////////

					CommissionFransabankOnUs com = commissionFransabankOnUsRepository
							.findByType(element.getTypeTransaction());

					TvaCommissionFransaBank tvaCommissionFransaBank = tvaRepo.findTva();
					float commission = Float.parseFloat(com.getCommissionFixe());

					G109.setMontantSettlement((commission));

					G109.setPieceComptable("RB" + "***" + String.format("%06d", indexPieceComptable));

					/////////////// **********END G109*********///////////////////////////////

					//////////////// **********START G110***********//////////////////////////

					int tva = Integer.parseInt(tvaCommissionFransaBank.getTva());
					float fff = 100;

					float commssionTvaArr = ((commission * tva) / fff);

					G110.setMontantSettlement((commssionTvaArr));

					G110.setPieceComptable("RB" + "***" + String.format("%06d", indexPieceComptable));

					/////////////// **********END G110*********///////////////////////////////

					//////////////// **********START G111***********//////////////////////////

					float commission_satim = Float.parseFloat(com.getCmi());

					G111.setMontantSettlement((commission_satim));

					G111.setPieceComptable("RB" + "***" + String.format("%06d", indexPieceComptable));

					/////////////// **********END G111*********///////////////////////////////

					//////////////// **********START G112***********//////////////////////////

					float tva_cmi = ((commission_satim * tva) / fff);

					G112.setMontantSettlement((tva_cmi));

					G112.setPieceComptable("RB" + "***" + String.format("%06d", indexPieceComptable));

					/////////////// **********END G112 *********///////////////////////////////

					//////////////// **********START G113***********//////////////////////////

					G113.setMontantSettlement((montantTransaction));

					G113.setPieceComptable("RB" + "***" + String.format("%06d", indexPieceComptable));

					/////////////// **********END G113 *********///////////////////////////////

					float satim = commission_satim + tva_cmi;
					Map<String, DayOperationFransaBank> days = new HashMap<>();
					days.put("SATIM", G111);
					days.put("SATIMTVA", G112);
					correctiondayOperation(element, days);

					contents.add(element);

					dayOperations.add(G108);
					dayOperations.add(G109);
					dayOperations.add(G110);
					dayOperations.add(G111);
					dayOperations.add(G112);
					dayOperations.add(G113);

				}
				;
				tpList.addAll(contents);

				// saveInBatches(dayOperations, threadNB, contents);
				daysOperationsAll.addAll(dayOperations);
				logger.info("addG108_G113_rembourssement_onUs_AutreAgence =>{}", dayOperations.size());

				batchRepo.updateFinishBatch("TH" + threadNB, 1, new Date());

			}

		} catch (Exception e) {
			catchError(e, threadNB);

		}

	}

	public void addG097_G102Rembourssement_offUs_Issuer(String fileDate, int threadNB) {
		batchRepo.updateFinishBatch("TH" + threadNB, 0, new Date());
		try {
			int index = 0;
			FileHeader summary = new FileHeader();
			if (fileRepository.findByfileDate(fileDate).isPresent()) {
				index = index + 1;
				summary = fileRepository.findByfileDate(fileDate).get();
				String destinationBank = summary.getDestinationBankIdentification();
				List<FileContent> fileContentList = new ArrayList<FileContent>();
				List<FileContent> dayOperation = settlementRepo
						.getG097_G098_G099_G100_G101_G102(String.valueOf(summary.getId()), destinationBank);
				List<DayOperationFransaBank> dayOperations = new ArrayList<DayOperationFransaBank>();
				List<FileContent> contents = new ArrayList<FileContent>();
				for (FileContent element : dayOperation) {
					if (ExecutorThreadFransaBank.stopThreads ) {
						return;
					}
					int indexPieceComptable = getEveIndex1();
					DayOperationFransaBank G097 = new DayOperationFransaBank();
					DayOperationFransaBank G098 = new DayOperationFransaBank();
					DayOperationFransaBank G099 = new DayOperationFransaBank();
					DayOperationFransaBank G100 = new DayOperationFransaBank();
					DayOperationFransaBank G101 = new DayOperationFransaBank();
					DayOperationFransaBank G102 = new DayOperationFransaBank();
					dayopCreation(G097, element, "G097");
					dayopCreation(G098, element, "G098");
					dayopCreation(G099, element, "G099");
					dayopCreation(G100, element, "G100");
					dayopCreation(G101, element, "G101");
					dayopCreation(G102, element, "G102");
					/////////////////// ********START G097**********///////////////////////////
					G097.setCodeAgence(element.getNumRIBEmetteur().substring(3, 8));

					float montantTransaction = Float.parseFloat(element.getMontantTransaction()) * fff;

					G097.setMontantSettlement(montantTransaction);

					G097.setPieceComptable("RB" + "***" + String.format("%06d", indexPieceComptable));

					///////////////// *********** END G097********////////////////////////////

					//////////////// **********START G098***********//////////////////////////
					G098.setCodeAgence(element.getNumRIBEmetteur().substring(3, 8));

					CommissionFransaBank com = commissionFransaBank.findByType(element.getTypeTransaction());

					TvaCommissionFransaBank tvaCommissionFransaBank = tvaRepo.findTva();
					float commission = Float.parseFloat(com.getCommissionAcq());

					G098.setMontantSettlement((commission));

					G098.setPieceComptable("RB" + "***" + String.format("%06d", indexPieceComptable));

					/////////////// **********END G098*********///////////////////////////////

					//////////////// **********START G099***********//////////////////////////
					G099.setCodeAgence(element.getNumRIBEmetteur().substring(3, 8));

					int tva = Integer.parseInt(tvaCommissionFransaBank.getTva());
					float fff = 100;

					float commssionTvaArr = ((commission * tva) / fff);

					G099.setMontantSettlement((commssionTvaArr));

					G099.setPieceComptable("RB" + "***" + String.format("%06d", indexPieceComptable));

					/////////////// **********END G099*********///////////////////////////////

					///////////// ***********START G100 ********/////////////////////////

					G100.setCodeAgence(element.getNumRIBEmetteur().substring(3, 8));

					float commission_cmi = Float.parseFloat(com.getCmi());

					G100.setMontantSettlement((commission_cmi));

					G100.setPieceComptable("RB" + "***" + String.format("%06d", indexPieceComptable));

					///////////// ***********END G100 ********/////////////////////////

					///////////// ***********START G101 ********/////////////////////////

					G101.setCodeAgence(element.getNumRIBEmetteur().substring(3, 8));

					float tva_emm = ((commission_cmi * tva) / fff);
					G101.setMontantSettlement((tva_emm));

					G101.setPieceComptable("RB" + "***" + String.format("%06d", indexPieceComptable));

					///////////// ***********END G101 ********/////////////////////////

					///////////// ***********START G102 ********/////////////////////////

					G102.setCodeAgence(element.getNumRIBEmetteur().substring(3, 8));

					float MONTANT = (montantTransaction) + commssionTvaArr + commission;

					G102.setMontantSettlement((MONTANT));

					G102.setPieceComptable("RB" + "***" + String.format("%06d", indexPieceComptable));

					///////////// ***********END G102 ********/////////////////////////

					Map<String, DayOperationFransaBank> days = new HashMap<>();
					days.put("SATIM", G100);
					days.put("SATIMTVA", G101);
					days.put("Interchange", G098);
					days.put("tvaInterchange", G099);
					correctiondayOperation(element, days);

					contents.add(element);
					dayOperations.add(G097);
					dayOperations.add(G098);
					dayOperations.add(G099);
					dayOperations.add(G100);
					dayOperations.add(G101);
					dayOperations.add(G102);

				}
				;
				tpList.addAll(contents);
				daysOperationsAll.addAll(dayOperations);
				logger.info("addG097_G102Rembourssement_offUs_Issuer =>{}", dayOperations.size());

				// saveInBatches(dayOperations, threadNB, contents);
				batchRepo.updateFinishBatch("TH" + threadNB, 1, new Date());

			}
			synchronized (Test) {
				logger.info("addG097_G102Rembourssement_offUs_Issuer");
				Test.add(1);
			}

		} catch (Exception e) {
			catchError(e, threadNB);

		}

	}

	public void addG048_G052_G094_G096Rembourssement_offUs_Acq(String fileDate, int threadNB) {
		batchRepo.updateFinishBatch("TH" + threadNB, 0, new Date());
		try {
			int index = 0;
			FileHeader summary = new FileHeader();
			if (fileRepository.findByfileDate(fileDate).isPresent()) {
				index = index + 1;
				summary = fileRepository.findByfileDate(fileDate).get();
				String destinationBank = summary.getDestinationBankIdentification();
				List<FileContent> fileContentList = new ArrayList<FileContent>();
				List<FileContent> dayOperation = settlementRepo
						.getG048_G049_G050_G051_G052_G094_G095_G096(String.valueOf(summary.getId()), destinationBank);
				List<DayOperationFransaBank> dayOperations = new ArrayList<DayOperationFransaBank>();
				List<FileContent> contents = new ArrayList<FileContent>();
				for (FileContent element : dayOperation) {
					if (ExecutorThreadFransaBank.stopThreads ) {
						return;
					}
					int indexPieceComptable = getEveIndex1();
					DayOperationFransaBank G048 = new DayOperationFransaBank();
					DayOperationFransaBank G049 = new DayOperationFransaBank();
					DayOperationFransaBank G050 = new DayOperationFransaBank();
					DayOperationFransaBank G051 = new DayOperationFransaBank();
					DayOperationFransaBank G052 = new DayOperationFransaBank();
					DayOperationFransaBank G094 = new DayOperationFransaBank();
					DayOperationFransaBank G095 = new DayOperationFransaBank();
					DayOperationFransaBank G096 = new DayOperationFransaBank();
					dayopCreation(G048, element, "G048");
					dayopCreation(G049, element, "G049");
					dayopCreation(G050, element, "G050");
					dayopCreation(G051, element, "G051");
					dayopCreation(G052, element, "G052");
					dayopCreation(G094, element, "G094");
					dayopCreation(G095, element, "G095");
					dayopCreation(G096, element, "G096");
					/////////////////// ********START G048 **********///////////////////////////

					float montantTransaction = Float.parseFloat(element.getMontantTransaction()) * fff;

					G048.setMontantSettlement(montantTransaction);

					G048.setPieceComptable("RB" + "***" + String.format("%06d", indexPieceComptable));

					///////////////// *********** END G048********////////////////////////////

					//////////////// **********START G049***********//////////////////////////

					CommissionFransaBank com = commissionFransaBank.findByType(element.getTypeTransaction());

					TvaCommissionFransaBank tvaCommissionFransaBank = tvaRepo.findTva();
					float commission = Float.parseFloat(com.getCommissionAcq())
							+ Float.parseFloat(com.getCommissionIssuer());
					G049.setMontantSettlement((commission));

					G049.setPieceComptable("RB" + "***" + String.format("%06d", indexPieceComptable));

					/////////////// **********END G049*********///////////////////////////////

					//////////////// **********START G050***********//////////////////////////

					int tva = Integer.parseInt(tvaCommissionFransaBank.getTva());
					float fff = 100;

					float commssionTvaArr = Math.round((commission * tva) / fff);

					G050.setMontantSettlement((commssionTvaArr));

					G050.setPieceComptable("RB" + "***" + String.format("%06d", indexPieceComptable));

					/////////////// **********END G050*********///////////////////////////////

					///////////// ***********START G051 ********/////////////////////////

					float commission_cpi = Float.parseFloat(com.getCpi());

					G051.setMontantSettlement((commission_cpi));

					G051.setPieceComptable("RB" + "***" + String.format("%06d", indexPieceComptable));

					///////////// ***********END G051 ********/////////////////////////

					///////////// ***********START G052 ********/////////////////////////

					float tva_cpi = Math.round((commission_cpi * tva) / fff);

					G052.setMontantSettlement((tva_cpi));

					G052.setPieceComptable("RB" + "***" + String.format("%06d", indexPieceComptable));

					///////////// ***********END G052 ********/////////////////////////

					///////////// ***********START G094 ********/////////////////////////

					float commission_emm = Float.parseFloat(com.getCommissionIssuer());

					G094.setMontantSettlement((commission_emm));

					G094.setPieceComptable("RB" + "***" + String.format("%06d", indexPieceComptable));

					///////////// ***********END G094 ********/////////////////////////

					///////////// ***********START G095 ********/////////////////////////

					float tva_emm = ((commission_emm * tva) / fff);

					G095.setMontantSettlement((tva_emm));

					G095.setPieceComptable("RB" + "***" + String.format("%06d", indexPieceComptable));

					///////////// ***********END G052 ********/////////////////////////

					///////////// ***********START G096 ********/////////////////////////

					float montant = ((montantTransaction) + commission + commssionTvaArr) - commission_cpi - tva_cpi;

					G096.setMontantSettlement((montant));

					G096.setPieceComptable("RB" + "***" + String.format("%06d", indexPieceComptable));

					///////////// ***********END G096 ********/////////////////////////

					Map<String, DayOperationFransaBank> days = new HashMap<>();
					days.put("Interchange", G094);
					days.put("tvaInterchange", G095);
					correctiondayOperation(element, days);
					contents.add(element);

					dayOperations.add(G048);
					dayOperations.add(G049);
					dayOperations.add(G050);
					dayOperations.add(G051);
					dayOperations.add(G052);
					dayOperations.add(G094);
					dayOperations.add(G095);
					dayOperations.add(G096);

				}
				;
				tpList.addAll(contents);
				daysOperationsAll.addAll(dayOperations);
				logger.info("addG048_G052_G094_G096Rembourssement_offUs_Acq =>{}", dayOperations.size());

				// saveInBatches(dayOperations, threadNB, contents);
				batchRepo.updateFinishBatch("TH" + threadNB, 1, new Date());

			}
			synchronized (Test) {
				logger.info("addG048_G052_G094_G096Rembourssement_offUs_Acq");
				Test.add(1);
			}

		} catch (Exception e) {
			catchError(e, threadNB);

		}

	}

	////////////// *************************** REMBOURSSEMENT SUR INTERNET
	////////////// ******************/////////////////////////

	public void addG148_G152_rembourssement_onUs_Internet_MemeAgence(String fileDate, int threadNB) {
		batchRepo.updateFinishBatch("TH" + threadNB, 0, new Date());
		try {
			int index = 0;
			FileHeader summary = new FileHeader();
			if (fileRepository.findByfileDate(fileDate).isPresent()) {
				index = index + 1;
				summary = fileRepository.findByfileDate(fileDate).get();
				String destinationBank = summary.getDestinationBankIdentification();
				List<FileContent> fileContentList = new ArrayList<FileContent>();
				List<FileContent> dayOperation = settlementRepo
						.getG148_G149_G150_G151_G152(String.valueOf(summary.getId()), destinationBank);
				List<DayOperationFransaBank> dayOperations = new ArrayList<DayOperationFransaBank>();
				List<FileContent> contents = new ArrayList<FileContent>();
				for (FileContent element : dayOperation) {
					if (ExecutorThreadFransaBank.stopThreads ) {
						return;
					}
					int indexPieceComptable = getEveIndex1();
					DayOperationFransaBank G148 = new DayOperationFransaBank();
					DayOperationFransaBank G149 = new DayOperationFransaBank();
					DayOperationFransaBank G150 = new DayOperationFransaBank();
					DayOperationFransaBank G151 = new DayOperationFransaBank();
					DayOperationFransaBank G152 = new DayOperationFransaBank();

					dayopCreation(G148, element, "G148");
					dayopCreation(G149, element, "G149");
					dayopCreation(G150, element, "G150");
					dayopCreation(G151, element, "G151");
					dayopCreation(G152, element, "G152");
					/////////////////// ********START G148**********///////////////////////////

					float montantTransaction = Float.parseFloat(element.getMontantTransaction()) * fff;

					G148.setMontantSettlement(montantTransaction);

					G148.setPieceComptable("RB" + "***" + String.format("%06d", indexPieceComptable));

					///////////////// *********** END G148********////////////////////////////

					//////////////// **********START G149***********//////////////////////////

					CommissionFransabankOnUs com = commissionFransabankOnUsRepository
							.findByType(element.getTypeTransaction());

					TvaCommissionFransaBank tvaCommissionFransaBank = tvaRepo.findTva();
					float commission = Float.parseFloat(com.getCommissionFixe());

					G149.setMontantSettlement((commission));

					G149.setPieceComptable("RB" + "***" + String.format("%06d", indexPieceComptable));

					/////////////// **********END G149*********///////////////////////////////

					//////////////// **********START G150***********//////////////////////////

					int tva = Integer.parseInt(tvaCommissionFransaBank.getTva());
					float fff = 100;

					float commssionTvaArr = ((commission * tva) / fff);

					G150.setMontantSettlement((commssionTvaArr));

					G150.setPieceComptable("RB" + "***" + String.format("%06d", indexPieceComptable));

					/////////////// **********END G150*********///////////////////////////////

					//////////////// **********START G151***********//////////////////////////

					float commission_satim = Float.parseFloat(com.getCmi());

					G151.setMontantSettlement((commission_satim));

					G151.setPieceComptable("RB" + "***" + String.format("%06d", indexPieceComptable));

					/////////////// **********END G151*********///////////////////////////////

					//////////////// **********START G152***********//////////////////////////

					float tva_cmi = ((commission_satim * tva) / fff);

					G152.setMontantSettlement((tva_cmi));

					G152.setPieceComptable("RB" + "***" + String.format("%06d", indexPieceComptable));

					/////////////// **********END G152 *********///////////////////////////////
					Map<String, DayOperationFransaBank> days = new HashMap<>();
					days.put("SATIM", G151);
					days.put("SATIMTVA", G152);
					correctiondayOperation(element, days);
					contents.add(element);
					dayOperations.add(G148);
					dayOperations.add(G149);
					dayOperations.add(G150);
					dayOperations.add(G151);
					dayOperations.add(G152);

				}
				;
				tpList.addAll(contents);
				daysOperationsAll.addAll(dayOperations);
				logger.info("addG148_G152_rembourssement_onUs_Internet_MemeAgence =>{}", dayOperations.size());

				// saveInBatches(dayOperations, threadNB, contents);

			}
			batchRepo.updateFinishBatch("TH" + threadNB, 1, new Date());

		} catch (Exception e) {
			catchError(e, threadNB);

		}

	}

	public void addG108_G113_rembourssement_Internet_onUs_AutreAgence(String fileDate, int threadNB) {
		batchRepo.updateFinishBatch("TH" + threadNB, 0, new Date());
		try {
			int index = 0;
			FileHeader summary = new FileHeader();
			if (fileRepository.findByfileDate(fileDate).isPresent()) {
				index = index + 1;
				summary = fileRepository.findByfileDate(fileDate).get();
				String destinationBank = summary.getDestinationBankIdentification();
				List<FileContent> fileContentList = new ArrayList<FileContent>();
				List<FileContent> dayOperation = settlementRepo
						.getG153_G154_G155_G156_G157_G158(String.valueOf(summary.getId()), destinationBank);
				List<DayOperationFransaBank> dayOperations = new ArrayList<DayOperationFransaBank>();
				List<FileContent> contents = new ArrayList<FileContent>();
				for (FileContent element : dayOperation) {
					if (ExecutorThreadFransaBank.stopThreads ) {
						return;
					}
					int indexPieceComptable = getEveIndex1();
					DayOperationFransaBank G153 = new DayOperationFransaBank();
					DayOperationFransaBank G154 = new DayOperationFransaBank();
					DayOperationFransaBank G155 = new DayOperationFransaBank();
					DayOperationFransaBank G156 = new DayOperationFransaBank();
					DayOperationFransaBank G157 = new DayOperationFransaBank();
					DayOperationFransaBank G158 = new DayOperationFransaBank();
					dayopCreation(G153, element, "G153");
					dayopCreation(G154, element, "G154");
					dayopCreation(G155, element, "G155");
					dayopCreation(G156, element, "G156");
					dayopCreation(G157, element, "G157");
					dayopCreation(G158, element, "G158");
					/////////////////// ********START G153**********///////////////////////////

					float montantTransaction = Float.parseFloat(element.getMontantTransaction()) * fff;

					G153.setMontantSettlement(montantTransaction);

					G153.setPieceComptable("RB" + "***" + String.format("%06d", indexPieceComptable));

					///////////////// *********** END G153********////////////////////////////

					//////////////// **********START G154***********//////////////////////////

					CommissionFransabankOnUs com = commissionFransabankOnUsRepository
							.findByType(element.getTypeTransaction());

					TvaCommissionFransaBank tvaCommissionFransaBank = tvaRepo.findTva();
					float commission = Float.parseFloat(com.getCommissionFixe());

					G154.setMontantSettlement((commission));

					G154.setPieceComptable("RB" + "***" + String.format("%06d", indexPieceComptable));

					/////////////// **********END G154*********///////////////////////////////

					//////////////// **********START G155***********//////////////////////////

					int tva = Integer.parseInt(tvaCommissionFransaBank.getTva());
					float fff = 100;

					float commssionTvaArr = ((commission * tva) / fff);
					G155.setMontantSettlement((commssionTvaArr));

					G155.setPieceComptable("RB" + "***" + String.format("%06d", indexPieceComptable));

					/////////////// **********END G155*********///////////////////////////////

					//////////////// **********START G156***********//////////////////////////

					float commission_satim = Float.parseFloat(com.getCmi());

					G156.setMontantSettlement((commission_satim));

					G156.setPieceComptable("RB" + "***" + String.format("%06d", indexPieceComptable));

					/////////////// **********END G156*********///////////////////////////////

					//////////////// **********START G157***********//////////////////////////

					float tva_cmi = ((commission_satim * tva) / fff);
					G157.setMontantSettlement((tva_cmi));
					G157.setPieceComptable("RB" + "***" + String.format("%06d", indexPieceComptable));

					/////////////// **********END G157 *********///////////////////////////////

					//////////////// **********START G158***********//////////////////////////

					G158.setMontantSettlement((montantTransaction));

					G158.setPieceComptable("RB" + "***" + String.format("%06d", indexPieceComptable));

					/////////////// **********END G158 *********///////////////////////////////

					Map<String, DayOperationFransaBank> days = new HashMap<>();
					days.put("SATIM", G156);
					days.put("SATIMTVA", G157);
					correctiondayOperation(element, days);
					contents.add(element);
					dayOperations.add(G153);
					dayOperations.add(G154);
					dayOperations.add(G155);
					dayOperations.add(G156);
					dayOperations.add(G157);
					dayOperations.add(G158);

				}
				;
				tpList.addAll(contents);
				daysOperationsAll.addAll(dayOperations);
				logger.info("addG108_G113_rembourssement_Internet_onUs_AutreAgence =>{}", dayOperations.size());

				// saveInBatches(dayOperations, threadNB, contents);

				batchRepo.updateFinishBatch("TH" + threadNB, 1, new Date());

			}

		} catch (Exception e) {
			catchError(e, threadNB);

		}

	}

	public void addG142_G147Rembourssement_Internet_offUs_Issuer(String fileDate, int threadNB) {
		batchRepo.updateFinishBatch("TH" + threadNB, 0, new Date());
		try {
			int index = 0;
			FileHeader summary = new FileHeader();
			if (fileRepository.findByfileDate(fileDate).isPresent()) {
				index = index + 1;
				summary = fileRepository.findByfileDate(fileDate).get();
				String destinationBank = summary.getDestinationBankIdentification();
				List<FileContent> fileContentList = new ArrayList<FileContent>();
				List<FileContent> dayOperation = settlementRepo
						.getG142_G143_G144_G145_G146_G147(String.valueOf(summary.getId()), destinationBank);
				List<DayOperationFransaBank> dayOperations = new ArrayList<DayOperationFransaBank>();
				List<FileContent> contents = new ArrayList<FileContent>();
				for (FileContent element : dayOperation) {
					if (ExecutorThreadFransaBank.stopThreads ) {
						return;
					}
					int indexPieceComptable = getEveIndex1();
					DayOperationFransaBank G142 = new DayOperationFransaBank();
					DayOperationFransaBank G143 = new DayOperationFransaBank();
					DayOperationFransaBank G144 = new DayOperationFransaBank();
					DayOperationFransaBank G145 = new DayOperationFransaBank();
					DayOperationFransaBank G146 = new DayOperationFransaBank();
					DayOperationFransaBank G147 = new DayOperationFransaBank();
					dayopCreation(G142, element, "G142");
					dayopCreation(G143, element, "G143");
					dayopCreation(G144, element, "G144");
					dayopCreation(G145, element, "G145");
					dayopCreation(G146, element, "G146");
					dayopCreation(G147, element, "G147");
					/////////////////// ********START G097**********///////////////////////////
					G142.setCodeAgence(element.getNumRIBEmetteur().substring(3, 8));

					float montantTransaction = Float.parseFloat(element.getMontantTransaction()) * fff;

					G142.setMontantSettlement(montantTransaction);

					G142.setPieceComptable("RB" + "***" + String.format("%06d", indexPieceComptable));

					///////////////// *********** END G142********////////////////////////////

					//////////////// **********START G143***********//////////////////////////
					G143.setCodeAgence(element.getNumRIBEmetteur().substring(3, 8));

					CommissionFransaBank com = commissionFransaBank.findByType(element.getTypeTransaction());

					TvaCommissionFransaBank tvaCommissionFransaBank = tvaRepo.findTva();
					float commission = Float.parseFloat(com.getCommissionAcq());

					G143.setMontantSettlement((commission));

					G143.setPieceComptable("RB" + "***" + String.format("%06d", indexPieceComptable));

					/////////////// **********END G143*********///////////////////////////////

					//////////////// **********START G144***********//////////////////////////
					G144.setCodeAgence(element.getNumRIBEmetteur().substring(3, 8));

					int tva = Integer.parseInt(tvaCommissionFransaBank.getTva());
					float fff = 100;

					float commssionTvaArr = ((commission * tva) / fff);

					G144.setMontantSettlement((commssionTvaArr));

					G144.setPieceComptable("RB" + "***" + String.format("%06d", indexPieceComptable));

					/////////////// **********END G144*********///////////////////////////////

					///////////// ***********START G145 ********/////////////////////////

					G145.setCodeAgence(element.getNumRIBEmetteur().substring(3, 8));

					float commission_cmi = Float.parseFloat(com.getCmi());

					G145.setMontantSettlement((commission_cmi));

					G145.setPieceComptable("RB" + "***" + String.format("%06d", indexPieceComptable));

					///////////// ***********END G145 ********/////////////////////////

					///////////// ***********START G146 ********/////////////////////////

					G146.setCodeAgence(element.getNumRIBEmetteur().substring(3, 8));

					float tva_emm = ((commission_cmi * tva) / fff);

					G146.setMontantSettlement((tva_emm));

					G146.setPieceComptable("RB" + "***" + String.format("%06d", indexPieceComptable));

					///////////// ***********END G146 ********/////////////////////////

					///////////// ***********START G147 ********/////////////////////////

					G147.setCodeAgence(element.getNumRIBEmetteur().substring(3, 8));

					float MONTANT = (montantTransaction) + commssionTvaArr + commission;

					G147.setMontantSettlement((MONTANT));

					G147.setPieceComptable("RB" + "***" + String.format("%06d", indexPieceComptable));

					///////////// ***********END G147 ********/////////////////////////
					Map<String, DayOperationFransaBank> days = new HashMap<>();
					days.put("SATIM", G146);
					days.put("SATIMTVA", G145);
					days.put("Interchange", G143);
					days.put("tvaInterchange", G144);
					correctiondayOperation(element, days);
					contents.add(element);
					dayOperations.add(G142);
					dayOperations.add(G143);
					dayOperations.add(G144);
					dayOperations.add(G145);
					dayOperations.add(G146);
					dayOperations.add(G147);

				}
				;
				tpList.addAll(contents);
				daysOperationsAll.addAll(dayOperations);
				logger.info("addG142_G147Rembourssement_Internet_offUs_Issuer =>{}", dayOperations.size());

				// saveInBatches(dayOperations, threadNB, contents);
				batchRepo.updateFinishBatch("TH" + threadNB, 1, new Date());

			}
			synchronized (Test) {
				Test.add(1);
			}

		} catch (Exception e) {
			catchError(e, threadNB);

		}

	}

	public void addG134_G141Rembourssement_Internet_offUs_Acq(String fileDate, int threadNB) {
		batchRepo.updateFinishBatch("TH" + threadNB, 0, new Date());
		try {
			int index = 0;
			FileHeader summary = new FileHeader();
			if (fileRepository.findByfileDate(fileDate).isPresent()) {
				index = index + 1;
				summary = fileRepository.findByfileDate(fileDate).get();
				String destinationBank = summary.getDestinationBankIdentification();
				List<FileContent> fileContentList = new ArrayList<FileContent>();
				List<FileContent> dayOperation = settlementRepo
						.getG134_G135_G136_G137_G138_G139_G140_G141(String.valueOf(summary.getId()), destinationBank);

				List<DayOperationFransaBank> dayOperations = new ArrayList<DayOperationFransaBank>();
				List<FileContent> contents = new ArrayList<FileContent>();
				for (FileContent element : dayOperation) {
					if (ExecutorThreadFransaBank.stopThreads ) {
						return;
					}
					int indexPieceComptable = getEveIndex1();
					DayOperationFransaBank G134 = new DayOperationFransaBank();
					DayOperationFransaBank G135 = new DayOperationFransaBank();
					DayOperationFransaBank G136 = new DayOperationFransaBank();
					DayOperationFransaBank G137 = new DayOperationFransaBank();
					DayOperationFransaBank G138 = new DayOperationFransaBank();
					DayOperationFransaBank G139 = new DayOperationFransaBank();
					DayOperationFransaBank G140 = new DayOperationFransaBank();
					DayOperationFransaBank G141 = new DayOperationFransaBank();
					dayopCreation(G134, element, "G134");
					dayopCreation(G135, element, "G135");
					dayopCreation(G136, element, "G136");
					dayopCreation(G137, element, "G137");
					dayopCreation(G138, element, "G138");
					dayopCreation(G139, element, "G139");
					dayopCreation(G140, element, "G140");
					dayopCreation(G141, element, "G141");
					/////////////////// ********START G134 **********///////////////////////////

					float montantTransaction = Float.parseFloat(element.getMontantTransaction()) * fff;

					G134.setMontantSettlement(montantTransaction);

					G134.setPieceComptable("RB" + "***" + String.format("%06d", indexPieceComptable));

					///////////////// *********** END G134********////////////////////////////

					//////////////// **********START G135***********//////////////////////////

					CommissionFransaBank com = commissionFransaBank.findByType(element.getTypeTransaction());

					TvaCommissionFransaBank tvaCommissionFransaBank = tvaRepo.findTva();
					float commission = Float.parseFloat(com.getCommissionAcq())
							+ Float.parseFloat(com.getCommissionIssuer());

					G135.setMontantSettlement((commission));

					G135.setPieceComptable("RB" + "***" + String.format("%06d", indexPieceComptable));

					/////////////// **********END G135*********///////////////////////////////

					//////////////// **********START G136***********//////////////////////////

					int tva = Integer.parseInt(tvaCommissionFransaBank.getTva());
					float fff = 100;

					float commssionTvaArr = ((commission * tva) / fff);

					int lenghTva = String.valueOf(commssionTvaArr).length();
					G136.setMontantSettlement((commssionTvaArr));

					G136.setPieceComptable("RB" + "***" + String.format("%06d", indexPieceComptable));

					/////////////// **********END G136*********///////////////////////////////

					///////////// ***********START G137 ********/////////////////////////

					float commission_cpi = Float.parseFloat(com.getCpi());

					G137.setMontantSettlement((commission_cpi));

					G137.setPieceComptable("RB" + "***" + String.format("%06d", indexPieceComptable));

					///////////// ***********END G137 ********/////////////////////////

					///////////// ***********START G138 ********/////////////////////////

					float tva_cpi = ((commission_cpi * tva) / fff);

					G138.setMontantSettlement((tva_cpi));

					G138.setPieceComptable("RB" + "***" + String.format("%06d", indexPieceComptable));

					///////////// ***********END G138 ********/////////////////////////

					///////////// ***********START G139 ********/////////////////////////

					float commission_emm = Float.parseFloat(com.getCommissionIssuer());

					G139.setMontantSettlement((commission_emm));

					G139.setPieceComptable("RB" + "***" + String.format("%06d", indexPieceComptable));

					///////////// ***********END G139 ********/////////////////////////

					///////////// ***********START G140 ********/////////////////////////

					float tva_emm = ((commission_emm * tva) / fff);

					G140.setMontantSettlement((tva_emm));

					G140.setPieceComptable("RB" + "***" + String.format("%06d", indexPieceComptable));

					///////////// ***********END G140 ********/////////////////////////

					///////////// ***********START G141 ********/////////////////////////

					float montant = ((montantTransaction) + commission + commssionTvaArr) - commission_cpi - tva_cpi;

					G141.setMontantSettlement((montant));

					G141.setPieceComptable("RB" + "***" + String.format("%06d", indexPieceComptable));

					///////////// ***********END G141 ********/////////////////////////

					float interchange = commission_emm + tva_emm;
					float satim = commission_cpi + tva_cpi;
					Map<String, DayOperationFransaBank> days = new HashMap<>();
					days.put("Interchange", G139);
					days.put("tvaInterchange", G140);
					correctiondayOperation(element, days);
					contents.add(element);
					dayOperations.add(G134);
					dayOperations.add(G135);
					dayOperations.add(G136);
					dayOperations.add(G137);
					dayOperations.add(G138);
					dayOperations.add(G139);
					dayOperations.add(G140);
					dayOperations.add(G141);

				}
				;
				tpList.addAll(contents);
				daysOperationsAll.addAll(dayOperations);
				logger.info("addG134_G141Rembourssement_Internet_offUs_Acq =>{}", dayOperations.size());

				// saveInBatches(dayOperations, threadNB, contents);
				batchRepo.updateFinishBatch("TH" + threadNB, 1, new Date());

			}
			synchronized (Test) {
				Test.add(1);
			}

		} catch (Exception e) {
			catchError(e, threadNB);

		}

	}

	///////////////// ******** CONSULTATION DE SOLDE
	///////////////// **************////////////////////////////////

	public void addG114_G117_ConsultationSolde_onUs_MemeAgence(String fileDate, int threadNB) {
		batchRepo.updateFinishBatch("TH" + threadNB, 0, new Date());
		try {
			int index = 0;
			FileHeader summary = new FileHeader();
			if (fileRepository.findByfileDate(fileDate).isPresent()) {
				index = index + 1;
				summary = fileRepository.findByfileDate(fileDate).get();
				String destinationBank = summary.getDestinationBankIdentification();
				List<FileContent> fileContentList = new ArrayList<FileContent>();
				List<FileContent> dayOperation = settlementRepo.getG114_G115_G116_G117(String.valueOf(summary.getId()),
						destinationBank);
				List<DayOperationFransaBank> dayOperations = new ArrayList<DayOperationFransaBank>();
				List<FileContent> contents = new ArrayList<FileContent>();
				for (FileContent element : dayOperation) {
					if (ExecutorThreadFransaBank.stopThreads ) {
						return;
					}
					int indexPieceComptable = getEveIndex1();
					DayOperationFransaBank G114 = new DayOperationFransaBank();
					DayOperationFransaBank G115 = new DayOperationFransaBank();
					DayOperationFransaBank G116 = new DayOperationFransaBank();
					DayOperationFransaBank G117 = new DayOperationFransaBank();
					dayopCreation(G114, element, "G114");
					dayopCreation(G115, element, "G115");
					dayopCreation(G116, element, "G116");
					dayopCreation(G117, element, "G117");
					//////////////// **********START G114***********//////////////////////////
					String montantTransaction = element.getMontantTransaction().replace(".", "");
					G114.setCodeAgence(element.getNumRIBEmetteur().substring(3, 8));

					CommissionFransabankOnUs com = commissionFransabankOnUsRepository
							.findByType(element.getTypeTransaction());
					TvaCommissionFransaBank tvaCommissionFransaBank = tvaRepo.findTva();
					float commission = Float.parseFloat(com.getCommissionFixe());
					G114.setMontantSettlement((commission));

					G114.setPieceComptable("DB" + "187" + String.format("%06d", indexPieceComptable));

					/////////////// **********END G114*********///////////////////////////////

					//////////////// **********START G115***********//////////////////////////
					G115.setCodeAgence(element.getNumRIBEmetteur().substring(3, 8));

					int tva = Integer.parseInt(tvaCommissionFransaBank.getTva());
					float fff = 100;
					float commssionTvaArr = ((commission * tva) / fff);

					G115.setMontantSettlement((commssionTvaArr));

					G115.setPieceComptable("DB" + "187" + String.format("%06d", indexPieceComptable));

					/////////////// **********END G115*********///////////////////////////////

					//////////////// **********START G116***********//////////////////////////
					G116.setCodeAgence(element.getNumRIBEmetteur().substring(3, 8));

					float commission_satim = Float.parseFloat(com.getCmi());
					G116.setMontantSettlement((commission_satim));

					G116.setPieceComptable("DB" + "187" + String.format("%06d", indexPieceComptable));

					/////////////// **********END G116*********///////////////////////////////

					//////////////// **********START G117***********//////////////////////////
					G117.setCodeAgence(element.getNumRIBEmetteur().substring(3, 8));

					float tva_cmi = ((commission_satim * tva) / fff);

					G117.setMontantSettlement((tva_cmi));

					G117.setPieceComptable("DB" + "187" + String.format("%06d", indexPieceComptable));

					/////////////// **********END G117 *********///////////////////////////////

					Map<String, DayOperationFransaBank> days = new HashMap<>();
					days.put("SATIM", G116);
					days.put("SATIMTVA", G117);
					correctiondayOperation(element, days);
					contents.add(element);
					dayOperations.add(G114);
					dayOperations.add(G115);
					dayOperations.add(G116);
					dayOperations.add(G117);

				}
				;
				tpList.addAll(contents);
				daysOperationsAll.addAll(dayOperations);
				logger.info("addG114_G117_ConsultationSolde_onUs_MemeAgence =>{}", dayOperations.size());

				// saveInBatches(dayOperations, threadNB, contents);
				batchRepo.updateFinishBatch("TH" + threadNB, 1, new Date());

			}

		} catch (Exception e) {
			catchError(e, threadNB);

		}

	}

	public void addG118_G121_ConsultationSolde_AutreAgence(String fileDate, int threadNB) {
		batchRepo.updateFinishBatch("TH" + threadNB, 0, new Date());
		try {
			int index = 0;
			FileHeader summary = new FileHeader();
			if (fileRepository.findByfileDate(fileDate).isPresent()) {
				index = index + 1;
				summary = fileRepository.findByfileDate(fileDate).get();
				String destinationBank = summary.getDestinationBankIdentification();
				List<FileContent> fileContentList = new ArrayList<FileContent>();
				List<FileContent> dayOperation = settlementRepo.getG118_G119_G120_G121(String.valueOf(summary.getId()),
						destinationBank);
				List<DayOperationFransaBank> dayOperations = new ArrayList<DayOperationFransaBank>();
				List<FileContent> contents = new ArrayList<FileContent>();
				for (FileContent element : dayOperation) {
					if (ExecutorThreadFransaBank.stopThreads ) {
						return;
					}
					int indexPieceComptable = getEveIndex1();
					DayOperationFransaBank G118 = new DayOperationFransaBank();
					DayOperationFransaBank G119 = new DayOperationFransaBank();
					DayOperationFransaBank G120 = new DayOperationFransaBank();
					DayOperationFransaBank G121 = new DayOperationFransaBank();
					dayopCreation(G118, element, "G118");
					dayopCreation(G119, element, "G119");
					dayopCreation(G120, element, "G120");
					dayopCreation(G121, element, "G121");
					//////////////// **********START G118***********//////////////////////////
					CommissionFransabankOnUs com = commissionFransabankOnUsRepository
							.findByType(element.getTypeTransaction());
					TvaCommissionFransaBank tvaCommissionFransaBank = tvaRepo.findTva();
					float commission = Float.parseFloat(com.getCommissionFixe());

					G118.setMontantSettlement((commission));

					G118.setPieceComptable("DB" + "187" + String.format("%06d", indexPieceComptable));

					/////////////// **********END G118*********///////////////////////////////

					//////////////// **********START G119***********//////////////////////////

					int tva = Integer.parseInt(tvaCommissionFransaBank.getTva());
					float fff = 100;
					float commssionTvaArr = ((commission * tva) / fff);

					G119.setMontantSettlement((commssionTvaArr));

					G119.setPieceComptable("DB" + "187" + String.format("%06d", indexPieceComptable));

					/////////////// **********END G119*********///////////////////////////////

					///////////// ***********START G120 ********/////////////////////////

					float commission_satim = Float.parseFloat(com.getCmi());

					G120.setMontantSettlement((commission_satim));

					G120.setPieceComptable("DB" + "187" + String.format("%06d", indexPieceComptable));

					///////////// ***********END G120 ********/////////////////////////

					//////////////// **********START G121***********//////////////////////////

					float tva_cmi = ((commission_satim * tva) / fff);

					G121.setMontantSettlement((tva_cmi));

					G121.setPieceComptable("DB" + "187" + String.format("%06d", indexPieceComptable));

					/////////////// **********END G121 *********///////////////////////////////

					float satim = commission_satim + tva_cmi;
					Map<String, DayOperationFransaBank> days = new HashMap<>();
					days.put("SATIM", G120);
					days.put("SATIMTVA", G121);
					correctiondayOperation(element, days);
					contents.add(element);
					dayOperations.add(G118);
					dayOperations.add(G119);
					dayOperations.add(G120);
					dayOperations.add(G121);

				}
				;
				tpList.addAll(contents);
				daysOperationsAll.addAll(dayOperations);
				logger.info("addG118_G121_ConsultationSolde_AutreAgence =>{}", dayOperations.size());

				// saveInBatches(dayOperations, threadNB, contents);

				batchRepo.updateFinishBatch("TH" + threadNB, 1, new Date());

			}

		} catch (Exception e) {
			catchError(e, threadNB);

		}

	}

	public void addG127_G133_ConsultationSolde_offUs_Issuer(String fileDate, int threadNB) {
		batchRepo.updateFinishBatch("TH" + threadNB, 0, new Date());
		try {
			int index = 0;
			FileHeader summary = new FileHeader();

			if (fileRepository.findByfileDate(fileDate).isPresent()) {
				index = index + 1;
				summary = fileRepository.findByfileDate(fileDate).get();

				String destinationBank = summary.getDestinationBankIdentification();
				List<FileContent> fileContentList = new ArrayList<FileContent>();
				List<FileContent> dayOperation = settlementRepo
						.getG127_G128_G129_G130_G131_G132_G133(String.valueOf(summary.getId()), destinationBank);

				List<DayOperationFransaBank> dayOperations = new ArrayList<DayOperationFransaBank>();
				List<FileContent> contents = new ArrayList<FileContent>();
				for (FileContent element : dayOperation) {
					if (ExecutorThreadFransaBank.stopThreads ) {
						return;
					}
					int indexPieceComptable = getEveIndex1();
					DayOperationFransaBank G127 = new DayOperationFransaBank();
					DayOperationFransaBank G128 = new DayOperationFransaBank();
					DayOperationFransaBank G129 = new DayOperationFransaBank();
					DayOperationFransaBank G130 = new DayOperationFransaBank();
					DayOperationFransaBank G131 = new DayOperationFransaBank();
					DayOperationFransaBank G132 = new DayOperationFransaBank();
					DayOperationFransaBank G133 = new DayOperationFransaBank();
					dayopCreation(G127, element, "G127");
					dayopCreation(G128, element, "G128");
					dayopCreation(G129, element, "G129");
					dayopCreation(G130, element, "G130");
					dayopCreation(G131, element, "G131");
					dayopCreation(G132, element, "G132");
					dayopCreation(G133, element, "G133");
					BankFransaBank codeBanque = bankFransaRepository.findByCodeBanque(element.getCodeBank()).get();
//					if (codeBanque.getStatus().equals("1")) {

					//////////////// **********START G127 ***********//////////////////////////
					G127.setCodeAgence(element.getNumRIBEmetteur().substring(3, 8));

//						CommissionFransaBank com = commissionFransaBank
//								.findByTypeForConsultationSolde(element.getTypeTransaction());
					CommissionFransaBank com = commissionFransaBank.findByTypeSwitch(element.getTypeTransaction());

					TvaCommissionFransaBank tvaCommissionFransaBank = tvaRepo.findTva();
					float commission = Float.parseFloat(com.getCommissionAcq())
							+ Float.parseFloat(com.getCommissionIssuer());

					G127.setMontantSettlement((commission));

					G127.setPieceComptable("DB" + "***" + String.format("%06d", indexPieceComptable));

					/////////////// **********END G127*********///////////////////////////////

					//////////////// **********START G128***********//////////////////////////
					G128.setCodeAgence(element.getNumRIBEmetteur().substring(3, 8));

					int tva = Integer.parseInt(tvaCommissionFransaBank.getTva());
					float fff = 100;

					float commssionTvaArr = Math.round((commission * tva) / fff);

					G128.setMontantSettlement((commssionTvaArr));

					G128.setPieceComptable("DB" + "***" + String.format("%06d", indexPieceComptable));

					/////////////// **********END G128*********///////////////////////////////

					///////////// ***********START G129 ********/////////////////////////

					G129.setCodeAgence(element.getNumRIBEmetteur().substring(3, 8));

					float commission_emm = Float.parseFloat(com.getCommissionIssuer());

					G129.setMontantSettlement((commission_emm));

					G129.setPieceComptable("DB" + "***" + String.format("%06d", indexPieceComptable));

					///////////// ***********END G129 ********/////////////////////////

					///////////// ***********START G130 ********/////////////////////////

					G130.setCodeAgence(element.getNumRIBEmetteur().substring(3, 8));

					float tva_emm = Math.round((commission_emm * tva) / fff);

					G130.setMontantSettlement((tva_emm));

					G130.setPieceComptable("DB" + "***" + String.format("%06d", indexPieceComptable));

					///////////// ***********END G130 ********/////////////////////////

					///////////// ***********START G131 ********/////////////////////////

					G131.setCodeAgence(element.getNumRIBEmetteur().substring(3, 8));

					float commission_satim = Float.parseFloat(com.getCmi());

					G131.setMontantSettlement((commission_satim));

					G131.setPieceComptable("DB" + "***" + String.format("%06d", indexPieceComptable));

					///////////// ***********END G131 ********/////////////////////////

					/////////////////// ****START G132 **************//////////////////////
					G132.setCodeAgence(element.getNumRIBEmetteur().substring(3, 8));

					float tva_satim = Math.round((commission_satim * tva) / fff);

					G132.setMontantSettlement((tva_satim));

					G132.setPieceComptable("DB" + "***" + String.format("%06d", indexPieceComptable));

					////////////// ******* END G132 ****************/////////////////////////////

					/////////// ********* START G133 **************////////////////////////
					G133.setCodeAgence(element.getNumRIBEmetteur().substring(3, 8));
					FileRequest.print(com.getCommissionAcq(), FileRequest.getLineNumber());
					Double montant = Double.parseDouble(com.getCommissionAcq())
							+ ((Double.parseDouble(com.getCommissionAcq()) * tva) / fff);
					montant = (double) Math.round(montant);
					FileRequest.print(montant + "", FileRequest.getLineNumber());

					G133.setMontantSettlement((montant.floatValue()));

					G133.setPieceComptable("DB" + "***" + String.format("%06d", indexPieceComptable));

					////////////// ******* END G133 ****************/////////////////////////////

					// ***********valid commission*******//
					float satim = commission_satim + tva_satim;
					float interchange = commission_emm + tva_emm;

					contents.add(element);

					dayOperations.add(G127);
					dayOperations.add(G128);
					dayOperations.add(G129);
					dayOperations.add(G130);
					dayOperations.add(G131);
					dayOperations.add(G132);
					dayOperations.add(G133);
					;

				}
				tpList.addAll(contents);
				daysOperationsAll.addAll(dayOperations);
				logger.info("addG127_G133_ConsultationSolde_offUs_Issuer =>{}", dayOperations.size());

				// saveInBatches(dayOperations, threadNB, contents);
				batchRepo.updateFinishBatch("TH" + threadNB, 1, new Date());

			}
		} catch (Exception e) {
			catchError(e, threadNB);

		}

	}

	public void addG122_G126_ConsultationSolde_offUs_Acq(String fileDate, int threadNB) {
		batchRepo.updateFinishBatch("TH" + threadNB, 0, new Date());
		try {
			int index = 0;
			FileHeader summary = new FileHeader();
			if (fileRepository.findByfileDate(fileDate).isPresent()) {
				index = index + 1;
				summary = fileRepository.findByfileDate(fileDate).get();
				String destinationBank = summary.getDestinationBankIdentification();
				List<FileContent> fileContentList = new ArrayList<FileContent>();
				List<FileContent> dayOperation = settlementRepo
						.getG122_G123_G124_G125_G126(String.valueOf(summary.getId()), destinationBank);
				List<DayOperationFransaBank> dayOperations = new ArrayList<DayOperationFransaBank>();
				List<FileContent> contents = new ArrayList<FileContent>();
				for (FileContent element : dayOperation) {
					if (ExecutorThreadFransaBank.stopThreads ) {
						return;
					}
					// find in table bank with the code bank in the eleent
					int indexPieceComptable = getEveIndex1();
					DayOperationFransaBank G122 = new DayOperationFransaBank();
					DayOperationFransaBank G123 = new DayOperationFransaBank();
					DayOperationFransaBank G124 = new DayOperationFransaBank();
					DayOperationFransaBank G125 = new DayOperationFransaBank();
					DayOperationFransaBank G126 = new DayOperationFransaBank();
					dayopCreation(G122, element, "G122");
					dayopCreation(G123, element, "G123");
					dayopCreation(G124, element, "G124");
					dayopCreation(G125, element, "G125");
					dayopCreation(G126, element, "G126");
					//////////////// **********START G122***********//////////////////////////
					BankFransaBank codeBanque = bankFransaRepository.findByCodeBanque(element.getCodeBank()).get();
					if (codeBanque.getStatus().equals("1")) {
						CommissionFransaBank com = commissionFransaBank.findByTypeSwitch(element.getTypeTransaction());
						TvaCommissionFransaBank tvaCommissionFransaBank = tvaRepo.findTva();
						float commission = Float.parseFloat(com.getCommissionAcq());
						G122.setMontantSettlement((commission));
						G122.setPieceComptable("DB" + "***" + String.format("%06d", indexPieceComptable));
						/////////////// **********END G122*********///////////////////////////////
						//////////////// **********START G123***********//////////////////////////
						int tva = Integer.parseInt(tvaCommissionFransaBank.getTva());
						float fff = 100;
						float commssionTvaArr = Math.round((commission * tva) / fff);
						G123.setMontantSettlement((commssionTvaArr));
						G123.setPieceComptable("DB" + "***" + String.format("%06d", indexPieceComptable));

						/////////////// **********END G123*********///////////////////////////////

						///////////// ***********START G124 ********/////////////////////////

						float commission_cpi = Float.parseFloat(com.getCpi());

						G124.setMontantSettlement((commission_cpi));

						G124.setPieceComptable("DB" + "***" + String.format("%06d", indexPieceComptable));

						///////////// ***********END G124 ********/////////////////////////

						///////////// ***********START G125 ********/////////////////////////

						float tva_cpi = Math.round((commission_cpi * tva) / fff);

						G125.setMontantSettlement((tva_cpi));

						G125.setPieceComptable("DB" + "***" + String.format("%06d", indexPieceComptable));

						///////////// ***********END G125 ********/////////////////////////

						///////////// ***********START G126 ********/////////////////////////

						float MONTANT = commssionTvaArr + commission;

						G126.setMontantSettlement((MONTANT));

						G126.setPieceComptable("DB" + "***" + String.format("%06d", indexPieceComptable));

						///////////// ***********END G126 ********/////////////////////////

					} else {
						CommissionFransaBank com = commissionFransaBank.findByTypeHost(element.getTypeTransaction());

						TvaCommissionFransaBank tvaCommissionFransaBank = tvaRepo.findTva();
						float commission = Float.parseFloat(com.getCommissionAcq());

						G122.setMontantSettlement((commission));

						G122.setPieceComptable("DB" + "***" + String.format("%06d", indexPieceComptable));

						/////////////// **********END G122*********///////////////////////////////

						//////////////// **********START G123***********//////////////////////////

						int tva = Integer.parseInt(tvaCommissionFransaBank.getTva());
						float fff = 100;

						float commssionTvaArr = Math.round((commission * tva) / fff);

						G123.setMontantSettlement((commssionTvaArr));

						G123.setPieceComptable("DB" + "***" + String.format("%06d", indexPieceComptable));

						/////////////// **********END G123*********///////////////////////////////

						///////////// ***********START G124 ********/////////////////////////

						float commission_cpi = Float.parseFloat(com.getCpi());

						G124.setMontantSettlement((commission_cpi));

						G124.setPieceComptable("DB" + "***" + String.format("%06d", indexPieceComptable));

						///////////// ***********END G124 ********/////////////////////////

						///////////// ***********START G125 ********/////////////////////////

						float tva_cpi = Math.round((commission_cpi * tva) / fff);

						G125.setMontantSettlement((tva_cpi));

						G125.setPieceComptable("DB" + "***" + String.format("%06d", indexPieceComptable));

						///////////// ***********END G125 ********/////////////////////////

						///////////// ***********START G126 ********/////////////////////////

						float MONTANT = commssionTvaArr + commission;

						G126.setMontantSettlement((MONTANT));

						G126.setPieceComptable("DB" + "***" + String.format("%06d", indexPieceComptable));

						///////////// ***********END G126 ********/////////////////////////

						float interchange = commssionTvaArr + commission;
					}
					Map<String, DayOperationFransaBank> days = new HashMap<>();
					days.put("Interchange", G122);
					days.put("tvaInterchange", G123);
					correctiondayOperation(element, days);
					contents.add(element);
					dayOperations.add(G122);
					dayOperations.add(G123);
					dayOperations.add(G124);
					dayOperations.add(G125);
					dayOperations.add(G126);

				}
				;
				tpList.addAll(contents);
				daysOperationsAll.addAll(dayOperations);
				logger.info("addG122_G126_ConsultationSolde_offUs_Acq =>{}", dayOperations.size());

				// saveInBatches(dayOperations, threadNB, contents);
				batchRepo.updateFinishBatch("TH" + threadNB, 1, new Date());

			}
			synchronized (Test) {

				Test.add(1);
			}

		} catch (Exception e) {
			catchError(e, threadNB);

		}

	}

///////////////////// ******* ALGERIE POSTE **********/////////////////////////////////////

	public void addG159_G169_cash_out_offUs_IssuerBanqueException(String fileDate, int threadNB) {
		batchRepo.updateFinishBatch("TH" + threadNB, 0, new Date());
		try {
			int index = 0;
			FileHeader summary = new FileHeader();
			if (fileRepository.findByfileDate(fileDate).isPresent()) {
				index = index + 1;
				summary = fileRepository.findByfileDate(fileDate).get();
				String destinationBank = summary.getDestinationBankIdentification();
				List<FileContent> fileContentList = new ArrayList<FileContent>();
				List<FileContent> dayOperation = settlementRepo
						.getG159_G160_G161_G162_G163_G164_G165_G166_G167_G168_G169(String.valueOf(summary.getId()),
								destinationBank, codeBanks);
				List<DayOperationFransaBank> dayOperations = new ArrayList<DayOperationFransaBank>();
				List<FileContent> contents = new ArrayList<FileContent>();
				for (FileContent element : dayOperation) {
					if (ExecutorThreadFransaBank.stopThreads ) {
						return;
					}
					int indexPieceComptable = getEveIndex1();

					DayOperationFransaBank G159 = new DayOperationFransaBank();
					DayOperationFransaBank G160 = new DayOperationFransaBank();
					DayOperationFransaBank G161 = new DayOperationFransaBank();
					DayOperationFransaBank G164 = new DayOperationFransaBank();
					DayOperationFransaBank G165 = new DayOperationFransaBank();
					DayOperationFransaBank G166 = new DayOperationFransaBank();
					DayOperationFransaBank G167 = new DayOperationFransaBank();
					DayOperationFransaBank G168 = new DayOperationFransaBank();
					DayOperationFransaBank G169 = new DayOperationFransaBank();
					dayopCreation(G159, element, "G159");
					dayopCreation(G160, element, "G160");
					dayopCreation(G161, element, "G161");
					dayopCreation(G164, element, "G164");
					dayopCreation(G165, element, "G165");
					dayopCreation(G166, element, "G166");
					dayopCreation(G167, element, "G167");
					dayopCreation(G168, element, "G168");
					dayopCreation(G169, element, "G169");
					/////////////////// ********START G159**********///////////////////////////
					G159.setCodeAgence(element.getNumRIBEmetteur().substring(3, 8));

					float montantTransaction = Float.parseFloat(element.getMontantTransaction()) * fff;

					G159.setMontantSettlement(montantTransaction);

					G159.setPieceComptable("DB" + "162" + String.format("%06d", indexPieceComptable));

					///////////////// *********** END G159********////////////////////////////

					//////////////// **********START G160***********//////////////////////////
					G160.setCodeAgence(element.getNumRIBEmetteur().substring(3, 8));
////////// commission FSBK/ALP ////////////////
					CommissionFransaBank com = commissionFransaBank.findByTypeIssuer(element.getTypeTransaction(),
							element.getCodeBankAcquereur());

					TvaCommissionFransaBank tvaCommissionFransaBank = tvaRepo.findTva();
					float commission = Float.parseFloat(com.getCommissionAcq())
							+ Float.parseFloat(com.getCommissionIssuer());

					G160.setMontantSettlement((commission));

					G160.setPieceComptable("DB" + "162" + String.format("%06d", indexPieceComptable));

					/////////////// **********END G160*********///////////////////////////////

					//////////////// **********START G161***********//////////////////////////
					G161.setCodeAgence(element.getNumRIBEmetteur().substring(3, 8));

					int tva = Integer.parseInt(tvaCommissionFransaBank.getTva());
					float fff = 100;

					float commssionTvaArr = Math.round((commission * tva) / fff);

					G161.setMontantSettlement((commssionTvaArr));

					int lengPieceComptable160 = G160.getPieceComptable().length();
					G161.setPieceComptable("DB" + "162" + String.format("%06d", indexPieceComptable));

					/////////////// **********END G161*********///////////////////////////////

					///////////// ***********START G162 ********/////////////////////////

					///////////// ***********START G164 ********/////////////////////////

					G164.setCodeAgence(element.getNumRIBEmetteur().substring(3, 8));

					float commission_satim = Float.parseFloat(com.getCmi());

					G164.setMontantSettlement((commission_satim));

					G164.setPieceComptable("DB" + "162" + String.format("%06d", indexPieceComptable));

					///////////// ***********END G164 ********/////////////////////////

					/////////////////// ****START G165 **************//////////////////////
					G165.setCodeAgence(element.getNumRIBEmetteur().substring(3, 8));

					float tva_satim = Math.round((commission_satim * tva) / fff);

					G165.setMontantSettlement((tva_satim));

					G165.setPieceComptable("DB" + "162" + String.format("%06d", indexPieceComptable));

					///////////// ***********START G018 ********/////////////////////////

					G166.setCodeAgence(element.getNumRIBEmetteur().substring(3, 8));

					float commission_acq = Float.parseFloat(com.getCommissionAcq());

					G166.setMontantSettlement((commission_acq));

					G166.setPieceComptable("DB" + "162" + String.format("%06d", indexPieceComptable));

					///////////// ***********END G018 ********/////////////////////////

					///////////// ***********START G019 ********/////////////////////////

					G167.setCodeAgence(element.getNumRIBEmetteur().substring(3, 8));

					float tva_acq = Math.round((commission_acq * tva) / fff);

					G167.setMontantSettlement((tva_acq));

					G167.setPieceComptable("DB" + "162" + String.format("%06d", indexPieceComptable));

					///////////// ***********END G167 ********/////////////////////////

					/////////////////// ****START G022 **************//////////////////////

					/////////// ********* START G168 **************////////////////////////
					G168.setCodeAgence(element.getNumRIBEmetteur().substring(3, 8));

					float montant = ((montantTransaction) + commission_acq + tva_acq);

					G168.setMontantSettlement((montant));

					G168.setPieceComptable("DB" + "162" + String.format("%06d", indexPieceComptable));

					////////////// ******* END G168 ****************/////////////////////////////

					/////////// ********* START G169 **************////////////////////////
					G169.setCodeAgence(element.getNumRIBEmetteur().substring(3, 8));

					float montant_satim = commission_satim + tva_satim;

					G169.setMontantSettlement((montant_satim));

					G169.setPieceComptable("DB" + "162" + String.format("%06d", indexPieceComptable));

					////////////// ******* END G169 ****************/////////////////////////////
					Map<String, DayOperationFransaBank> days = new HashMap<>();
					days.put("SATIM", G164);
					days.put("SATIMTVA", G165);
					days.put("Interchange", G166);
					days.put("tvaInterchange", G167);
					correctiondayOperation(element, days);
					contents.add(element);
					dayOperations.add(G159);
					dayOperations.add(G160);
					dayOperations.add(G161);
					dayOperations.add(G166);
					dayOperations.add(G167);
					dayOperations.add(G164);
					dayOperations.add(G165);

					dayOperations.add(G168);
					dayOperations.add(G169);
				}
				;
				tpList.addAll(contents);
				daysOperationsAll.addAll(dayOperations);
				logger.info("addG159_G169_cash_out_offUs_IssuerBanqueException =>{}", dayOperations.size());

				// saveInBatches(dayOperations, threadNB, contents);
				batchRepo.updateFinishBatch("TH" + threadNB, 1, new Date());

			}

		} catch (Exception e) {
			catchError(e, threadNB);

		}

	}

	public void addG170_G175_cash_out_offUs_AcqBanqueException(String fileDate, int threadNB) {
		batchRepo.updateFinishBatch("TH" + threadNB, 0, new Date());
		try {
			int index = 0;
			FileHeader summary = new FileHeader();
			if (fileRepository.findByfileDate(fileDate).isPresent()) {
				index = index + 1;
				summary = fileRepository.findByfileDate(fileDate).get();
				String destinationBank = summary.getDestinationBankIdentification();
				List<FileContent> fileContentList = new ArrayList<FileContent>();
				List<FileContent> dayOperation = settlementRepo
						.getG170_G171_G172_G173_G174_G175(String.valueOf(summary.getId()), destinationBank, codeBanks);
				List<DayOperationFransaBank> dayOperations = new ArrayList<DayOperationFransaBank>();
				List<FileContent> contents = new ArrayList<FileContent>();
				for (FileContent element : dayOperation) {
					if (ExecutorThreadFransaBank.stopThreads ) {
						return;
					}
					int indexPieceComptable = getEveIndex1();
					DayOperationFransaBank G170 = new DayOperationFransaBank();
					DayOperationFransaBank G171 = new DayOperationFransaBank();
					DayOperationFransaBank G172 = new DayOperationFransaBank();
					DayOperationFransaBank G173 = new DayOperationFransaBank();
					DayOperationFransaBank G174 = new DayOperationFransaBank();
					DayOperationFransaBank G175 = new DayOperationFransaBank();
					DayOperationFransaBank G176 = new DayOperationFransaBank();
					DayOperationFransaBank G177 = new DayOperationFransaBank();
					dayopCreation(G170, element, "G170");
					dayopCreation(G171, element, "G171");
					dayopCreation(G172, element, "G172");
					dayopCreation(G173, element, "G173");
					dayopCreation(G174, element, "G174");
					dayopCreation(G175, element, "G175");
					dayopCreation(G176, element, "G176");
					dayopCreation(G177, element, "G177");
					/////////////////// ********START G170**********///////////////////////////

					float montantTransaction = Float.parseFloat(element.getMontantTransaction()) * fff;

					G170.setMontantSettlement(montantTransaction);

					G170.setPieceComptable("DB" + "170" + String.format("%06d", indexPieceComptable)); // G170.setPieceComptable("DB"+element.getTypeTransaction()+String.format("%06d",index));

					///////////////// *********** END G170********////////////////////////////

					//////////////// **********START G171***********//////////////////////////
////////// ALP/FSBK //////////////////
					CommissionFransaBank com = commissionFransaBank.findByTypeAcq(element.getTypeTransaction(),
							element.getCodeBank());

					TvaCommissionFransaBank tvaCommissionFransaBank = tvaRepo.findTva();
					float commission = Float.parseFloat(com.getCommissionAcq());

					G171.setMontantSettlement((commission));
					G171.setPieceComptable("DB" + "170" + String.format("%06d", indexPieceComptable));

					/////////////// **********END G171*********///////////////////////////////

					//////////////// **********START G172***********//////////////////////////

					int tva = Integer.parseInt(tvaCommissionFransaBank.getTva());
					float fff = 100;

					float commssionTvaArr = Math.round((commission * tva) / fff);

					G172.setMontantSettlement((commssionTvaArr));

					int lengPieceComptable171 = G171.getPieceComptable().length();
					G172.setPieceComptable("DB" + "170" + String.format("%06d", indexPieceComptable));

					/////////////// **********END G172*********///////////////////////////////

					///////////// ***********START G173 ********/////////////////////////

					float commission_cpi = Float.parseFloat(com.getCpi());

					G173.setMontantSettlement((commission_cpi));

					G173.setPieceComptable("DB" + "170" + String.format("%06d", indexPieceComptable));

					///////////// ***********END G173 ********/////////////////////////

					///////////// ***********START G174 ********/////////////////////////

					float tva_acq = Math.round((commission_cpi * tva) / fff);

					G174.setMontantSettlement((tva_acq));

					G174.setPieceComptable("DB" + "170" + String.format("%06d", indexPieceComptable));

					///////////// ***********END G174 ********/////////////////////////

					///////////// ***********START G175 ********/////////////////////////

					float MONTANT = (montantTransaction) + commssionTvaArr + commission;

					G175.setMontantSettlement((MONTANT));

					int lengPieceComptable174 = G174.getPieceComptable().length();
					G175.setPieceComptable("DB" + "170" + String.format("%06d", indexPieceComptable));

					///////////// ***********END G175 ********/////////////////////////

					/////////// *************START G176*********///////////////////////

					float commission_cmi = Float.parseFloat(com.getCmi());

					G176.setMontantSettlement((commission_cmi));

					G176.setPieceComptable("DB" + "170" + String.format("%06d", indexPieceComptable));

					/////////////////////////// ***************END
					/////////////////////////// G176*********//////////////////////////////////////

					////////////////////// **************** START G177
					////////////////////// ********///////////////////////////////

					float tvA = ((commission_cmi * tva) / fff);

					G177.setMontantSettlement((tvA));

					G177.setPieceComptable("DB" + "170" + String.format("%06d", indexPieceComptable));

					//////////////////////////////

					Map<String, DayOperationFransaBank> days = new HashMap<>();
					days.put("SATIM", G176);
					days.put("SATIMTVA", G177);
					days.put("Interchange", G171);
					days.put("tvaInterchange", G172);
					correctiondayOperation(element, days);
					contents.add(element);
					dayOperations.add(G170);
					dayOperations.add(G171);
					dayOperations.add(G172);
					dayOperations.add(G173);
					dayOperations.add(G174);
					dayOperations.add(G175);
					dayOperations.add(G176);
					dayOperations.add(G177);
				}
				tpList.addAll(contents);
				daysOperationsAll.addAll(dayOperations);
				logger.info("addG170_G175_cash_out_offUs_AcqAlgeriePoste =>{}", dayOperations.size());

				// saveInBatches(dayOperations, threadNB, contents);
				batchRepo.updateFinishBatch("TH" + threadNB, 1, new Date());

			}
			synchronized (Test) {
				Test.add(1);
			}

		} catch (Exception e) {
			catchError(e, threadNB);

		}

	}

	///////////////////////// ACHAT INTERNET FOR EXCEPTIONNEL MERCHANT
	///////////////////////// //////////////////////////////////////

	public void addG193_G199_AchatInternet_offus_AcqExceptionnelMerchant(String fileDate, int threadNB) {
		batchRepo.updateFinishBatch("TH" + threadNB, 0, new Date());
		try {
			int index = 0;
			FileHeader summary = new FileHeader();
			FileRequest.print("" + fileRepository.findByfileDate(fileDate).isPresent(), FileRequest.getLineNumber());
			if (fileRepository.findByfileDate(fileDate).isPresent()) {
				index = index + 1;
				summary = fileRepository.findByfileDate(fileDate).get();
				String destinationBank = summary.getDestinationBankIdentification();
				List<FileContent> fileContentList = new ArrayList<FileContent>();
				List<FileContent> dayOperation = settlementRepo.getG193_G194_G195_G196_G197_G198_G199(
						String.valueOf(summary.getId()), destinationBank, codeBanksInternet);
//				FileRequest.print(String.valueOf(summary.getId()), FileRequest.getLineNumber());
//				FileRequest.print(destinationBank, FileRequest.getLineNumber());
//				FileRequest.print("" + codeBanksInternet, FileRequest.getLineNumber());
//				FileRequest.print("" + dayOperation.size(), FileRequest.getLineNumber());

				List<DayOperationFransaBank> dayOperations = new ArrayList<DayOperationFransaBank>();
				List<FileContent> contents = new ArrayList<FileContent>();
				for (FileContent element : dayOperation) {
					if (ExecutorThreadFransaBank.stopThreads ) {
						return;
					}
					int indexPieceComptable = getEveIndex1();
					DayOperationFransaBank G193 = new DayOperationFransaBank();
					DayOperationFransaBank G194 = new DayOperationFransaBank();
					DayOperationFransaBank G195 = new DayOperationFransaBank();
					DayOperationFransaBank G196 = new DayOperationFransaBank();
					DayOperationFransaBank G197 = new DayOperationFransaBank();
					DayOperationFransaBank G198 = new DayOperationFransaBank();
					DayOperationFransaBank G199 = new DayOperationFransaBank();
					dayopCreation(G193, element, "G193");
					dayopCreation(G194, element, "G194");
					dayopCreation(G195, element, "G195");
					dayopCreation(G196, element, "G196");
					dayopCreation(G197, element, "G197");
					dayopCreation(G198, element, "G198");
					dayopCreation(G199, element, "G199");
					String pComptable = "PA" + "164" + String.format("%06d", indexPieceComptable);
					G193.setPieceComptable(pComptable);
					G194.setPieceComptable(pComptable);
					G195.setPieceComptable(pComptable);
					G196.setPieceComptable(pComptable);
					G197.setPieceComptable(pComptable);
					G198.setPieceComptable(pComptable);
					G199.setPieceComptable(pComptable);

					String montantTransaction_DA = element.getMontantTransaction().replace(".", "");
					String montantTransaction_ML = montantTransaction_DA;
					int montant = Integer.parseInt(montantTransaction_ML);

					/////////////////// ********START G193 **********///////////////////////////
					float montantTransaction = Float.parseFloat(element.getMontantTransaction()) * fff;
					G193.setMontantSettlement((montantTransaction));

					//////////////// **********START ***********//////////////////////////

					CommissionAchatInternetFB com = commissionFransaBankInternetRepository
							.findByType(element.getTypeTransaction());
					float fff = 100;
					float pourcentage = Float.parseFloat(com.getValeurVarivable());
					float commission = ((montant * pourcentage) / fff);
					float commissionAcq = ((commission * Float.parseFloat(com.getVariableCommissionIssuer())) / fff);
					float pourcenatgeCmi = Integer.parseInt(com.getVariableCmi()) / fff;
					float interchange = commissionAcq;

					TvaCommissionFransaBank tvaCommissionFransaBank = tvaRepo.findTva();
					float commissionInf = Float.parseFloat(com.getValeurMin());
					float commissionSup = Float.parseFloat(com.getValeurMax());
					int tva = Integer.parseInt(tvaCommissionFransaBank.getTva());
					float commssionTvaInf = (((commissionInf) * tva) / fff);
					float commssionTvaSup = (((commissionSup) * tva) / fff);

					float commssionTva = ((commission * tva) / fff);
					float commssionTvaAcq = ((commissionAcq * tva) / fff);
					//////////// new Inf////////////////
					float commissionAcqInf = (((commissionInf) * Float.parseFloat(com.getVariableCommissionIssuer()))
							/ fff);
					float interchangeInf = commissionAcqInf;
					float commssionTvaInterchangeInf = (((commissionAcqInf) * tva) / fff);
					float cmiInf = ((pourcenatgeCmi * (commissionInf)));
					float tvaCmiInf = (((cmiInf) * tva) / fff);
					float montant_a_verserInf = montant - commissionAcqInf - commssionTvaInterchangeInf;
					float montant_satim_a_verserInf = cmiInf + tvaCmiInf;

					///////////// new sup ///////////
					float commissionAcqSup = (((commissionSup) * Float.parseFloat(com.getVariableCommissionIssuer()))
							/ fff);
					float interchangeSup = commissionAcqSup;
					float commssionTvaInterchangeSup = (((interchangeSup) * tva) / fff);
					float cmiSup = ((pourcenatgeCmi * (commissionSup)));

					float tvaCmiSup = (((cmiSup) * tva) / fff);
					float montant_a_verserSup = montant - interchangeSup - commssionTvaInterchangeSup;
					float montant_satim_a_verserSup = cmiSup + tvaCmiSup;
					/// new between ///////////////////
					float cmi = (pourcenatgeCmi * (commission));
					float tvaCmi = (((cmi) * tva) / fff);
					float montant_a_verser = montant - interchange - commssionTvaAcq;
					float montant_satim_a_verser = cmi + tvaCmi;
					float interchangeCommission = 0;
					float satimCommission = 0;
					if (commission <= Integer.parseInt(com.getValeurMin())) {
						G194.setMontantSettlement((commissionInf));
						G195.setMontantSettlement((commssionTvaInf));

						G196.setMontantSettlement((interchangeInf));
						G197.setMontantSettlement((commssionTvaInterchangeInf));
						G198.setMontantSettlement((cmiInf));
						G199.setMontantSettlement((tvaCmiInf));
						Map<String, DayOperationFransaBank> days = new HashMap<>();
						days.put("COMMPOR", G194);
						days.put("COMMPORTTVA", G195);
						days.put("SATIM", G198);
						days.put("SATIMTVA", G199);
						days.put("Interchange", G196);
						days.put("tvaInterchange", G197);

						correctiondayOperation(element, days);
						interchangeCommission = interchangeInf + commssionTvaInterchangeInf;
						satimCommission = montant_satim_a_verserInf;
					} else if (commission >= Integer.parseInt(com.getValeurMax())) {

						G194.setMontantSettlement((commissionSup));
						G195.setMontantSettlement((commssionTvaSup));
						G196.setMontantSettlement((interchangeSup));
						G197.setMontantSettlement((commssionTvaInterchangeSup));
						G198.setMontantSettlement((cmiSup));
						G199.setMontantSettlement((tvaCmiSup));
						Map<String, DayOperationFransaBank> days = new HashMap<>();
						days.put("COMMPOR", G194);
						days.put("COMMPORTTVA", G195);
						days.put("SATIM", G198);
						days.put("SATIMTVA", G199);
						days.put("Interchange", G196);
						days.put("tvaInterchange", G197);

						correctiondayOperation(element, days);
						interchangeCommission = interchangeSup + commssionTvaInterchangeSup;
						satimCommission = montant_satim_a_verserSup;
					} else if (commission > Integer.parseInt(com.getValeurMin())
							&& commission < Integer.parseInt(com.getValeurMax())) {
						G194.setMontantSettlement((commission));
						G195.setMontantSettlement((commssionTva));
						G196.setMontantSettlement((interchange));
						G197.setMontantSettlement((commssionTvaAcq));
						G198.setMontantSettlement((cmi));
						G199.setMontantSettlement((tvaCmi));
						Map<String, DayOperationFransaBank> days = new HashMap<>();
						days.put("COMMPOR", G194);
						days.put("COMMPORTTVA", G195);
						days.put("SATIM", G198);
						days.put("SATIMTVA", G199);
						days.put("Interchange", G196);
						days.put("tvaInterchange", G197);

						correctiondayOperation(element, days);
//						FileRequest.print(days.toString(), FileRequest.getLineNumber());
//						FileRequest.print(G196.toString(), FileRequest.getLineNumber());
//						FileRequest.print(G197.toString(), FileRequest.getLineNumber());

						interchangeCommission = interchange + commssionTvaAcq;
						satimCommission = montant_satim_a_verser;

					}

					G193.setCodeAgence(element.getNumRIBEmetteur().substring(3, 8));
					G194.setCodeAgence(element.getNumRIBEmetteur().substring(3, 8));
					G195.setCodeAgence(element.getNumRIBEmetteur().substring(3, 8));
					G196.setCodeAgence(element.getNumRIBEmetteur().substring(3, 8));
					G197.setCodeAgence(element.getNumRIBEmetteur().substring(3, 8));
					G198.setCodeAgence(element.getNumRIBEmetteur().substring(3, 8));
					G199.setCodeAgence(element.getNumRIBEmetteur().substring(3, 8));

					////////////// ******* END G080 ****************/////////////////////////////

					contents.add(element);

					dayOperations.add(G193);
					dayOperations.add(G194);
					dayOperations.add(G195);
					dayOperations.add(G196);
					dayOperations.add(G197);
					dayOperations.add(G198);
					dayOperations.add(G199);
				}
				;
				daysOperationsAll.addAll(dayOperations);
				logger.info("addG193_G199_AchatInternet_offus_AcqExceptionnelMerchant =>{}", dayOperations.size());

				// saveInBatches(dayOperations, threadNB, contents);

				batchRepo.updateFinishBatch("TH" + threadNB, 1, new Date());
			}

		} catch (Exception e) {
			catchError(e, threadNB);

		}

	}

//////////////////////////////////////////////////////////////////////////////////////
	public String getDebit(String identi, FileContent porteur) {
		String debit_G = new String();
		SettelementFransaBank sett = settlementRepo.findByIdentificationbh(identi);
		switch (sett.getDebitAccount()) {

		// ********Consultation solde ONUS *******//
		case "PORTEUR":
			debit_G = porteur.getNumRIBEmetteur();
			break;
		case "AGENCE":
			debit_G = "373103" + porteur.getCodeAgence();
			break;
		case "PRODUIT":
			debit_G = "702150";
			break;
		case "PRODUIT TVA":
			debit_G = "341120";
			break;
		case "C NATIONAL":
			debit_G = "365AAA";
			break;
		case "373103****":
			int i = sett.getDebitAccount().indexOf("*");

			String credit = sett.getDebitAccount().substring(0, i);

			int lengh = porteur.getCodeAgence().length();
			String code = porteur.getCodeAgence().substring(1, lengh);

			debit_G = credit + code;
			break;
		case "COMMERCANT":
			debit_G = porteur.getNumRIBcommercant();
			break;
		default:
			debit_G = sett.getDebitAccount();
			break;
		}
		return debit_G;

	}

	public String getCredit(String identi, FileContent porteur) {
		String credit_G = new String();
		SettelementFransaBank sett = settlementRepo.findByIdentificationbh(identi);

		switch (sett.getCreditAccount()) {

		// ********Consultation solde ONUS *******//
		case "PORTEUR":
			credit_G = porteur.getNumRIBEmetteur();
			break;
		case "AGENCE":
			credit_G = "373103" + porteur.getCodeAgence();
			break;
		case "PRODUIT":
			credit_G = "702150";
			break;
		case "PRODUIT TVA":
			credit_G = "341120";
			break;
		case "C NATIONAL":
			credit_G = "4123545";
			break;
		case "CMI":
			credit_G = "341793";
			break;
		case "CPI":
			credit_G = "225469";
			break;
		case "373103****":
			int i = sett.getCreditAccount().indexOf("*");
			String credit = sett.getCreditAccount().substring(0, i);
			int lengh = porteur.getCodeAgence().length();
			String code = porteur.getCodeAgence().substring(1, lengh);
			credit_G = credit + code;

			break;
		case "COMMERCANT":
			credit_G = porteur.getNumRIBcommercant();
			break;
		case "ATM":
			credit_G = getAccountAtm(porteur.getIdCommercant());
			break;

		default:
			credit_G = sett.getCreditAccount();
			break;
		}

		return credit_G;

	}

	public String getAccountAtm(String idTerminal) {
//		FileRequest.print("idTerminal= " + idTerminal, FileRequest.getLineNumber());
		String account = "";
		for (AtmTerminal element : ExecutorThreadFransaBank.atmTerminal) {

			if ((element.getAteId().trim()).equals(idTerminal)) {
				Merchant merchant = mr.findById(element.getMerchantCode()).get();
//				FileRequest.print(" "+merchant.getAccount(), FileRequest.getLineNumber());
				for (Account e : ExecutorThreadFransaBank.account) {
//					FileRequest.print("in account", FileRequest.getLineNumber());
					if (e.getAccountCode() == merchant.getAccount()) {

						account = "0" + e.getAccountNum();
//						FileRequest.print("account switch", FileRequest.getLineNumber());
						break;
					}

				}
				;
			}
		}
		;
//		FileRequest.print("account= " + account, FileRequest.getLineNumber());

		return account;
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


	
	public void correctiondayOperation(FileContent tp, Map<String, DayOperationFransaBank> days) {
		 
		DayOperationFransaBank commpor = days.get("COMMPOR");
		DayOperationFransaBank commporTva = days.get("COMMPORTTVA");
		DayOperationFransaBank satimD = days.get("SATIM");
		DayOperationFransaBank tvaSatimD = days.get("SATIMTVA");
		DayOperationFransaBank interchangeD = days.get("Interchange");
		DayOperationFransaBank tvaInterchangeD = days.get("tvaInterchange");
		DayOperationFransaBank profilHTD = days.get("profilHT");
		DayOperationFransaBank tvaprofilD = days.get("tvaprofil");
		DayOperationFransaBank commissionHTD = days.get("commission");
		DayOperationFransaBank commissionTVAD = days.get("commissionTVA");
		if (commpor != null) {
			commpor.setMontantSettlement(Math.round(commpor.getMontantSettlement()));
		}
		if (commporTva != null) {
			commporTva.setMontantSettlement(Math.round(commporTva.getMontantSettlement()));
		}
		if (satimD != null) {
			satimD.setMontantSettlement(Math.round(satimD.getMontantSettlement()));
		}
		if (tvaSatimD != null) {
			tvaSatimD.setMontantSettlement(Math.round(tvaSatimD.getMontantSettlement()));
		}
		if (interchangeD != null) {
			interchangeD.setMontantSettlement(Math.round(interchangeD.getMontantSettlement()));
		}
		if (tvaInterchangeD != null) {
			tvaInterchangeD.setMontantSettlement(Math.round(tvaInterchangeD.getMontantSettlement()));
		}
		if (profilHTD != null) {
			profilHTD.setMontantSettlement(Math.round(profilHTD.getMontantSettlement()));
		}
		if (tvaprofilD != null) {
			tvaprofilD.setMontantSettlement(Math.round(tvaprofilD.getMontantSettlement()));
		}
		if (commissionHTD != null) {
			commissionHTD.setMontantSettlement(Math.round(commissionHTD.getMontantSettlement()));
		}
		if (commissionTVAD != null) {
			commissionTVAD.setMontantSettlement(Math.round(commissionTVAD.getMontantSettlement()));
		}
 
		BigDecimal interchangeTTC = new BigDecimal(tp.getCommisionInterchange().replace(".", ""));
		BigDecimal satimTTC = new BigDecimal(tp.getFraisOperateurTechnique().replace(".", ""));
		BigDecimal tva = new BigDecimal(tvaRepo.findTva().getTva());
		tva = tva.divide(new BigDecimal(100));
		BigDecimal tvaDevided = tva.add(new BigDecimal(1));
		BigDecimal interchange = interchangeTTC.divide(tvaDevided, 10, RoundingMode.HALF_UP);
		BigDecimal satim = satimTTC.divide(tvaDevided, 10, RoundingMode.HALF_UP);
		BigDecimal tvaSatim = satimTTC.subtract(satim).setScale(0, RoundingMode.HALF_UP);
		satim = satimTTC.subtract(tvaSatim).setScale(0, RoundingMode.HALF_UP);
		BigDecimal interchangeTva = interchangeTTC.subtract(interchange).setScale(0, RoundingMode.HALF_UP);
		interchange = interchangeTTC.subtract(interchangeTva).setScale(0, RoundingMode.HALF_UP);
//		calcule profil 
		BigDecimal profilTTC = new BigDecimal(tp.getCommisionCommercant().replace(".", ""));
//		BigDecimal profilTTC = profilcommercant.subtract(interchangeTTC);
		BigDecimal profilHT = profilTTC.divide(tvaDevided, 10, RoundingMode.HALF_UP);
		BigDecimal tvaprofil = profilTTC.subtract(profilHT).setScale(0, RoundingMode.HALF_UP);
		profilHT = profilTTC.subtract(tvaprofil);
 
		// calculecommission
		BigDecimal commissionTTc = new BigDecimal(tp.getCommisionCommercant().replace(".", ""));
//		BigDecimal profilTTC = profilcommercant.subtract(interchangeTTC);
		BigDecimal commissionHT = commissionTTc.divide(tvaDevided, 10, RoundingMode.HALF_UP);
		BigDecimal commissionTVA = commissionTTc.subtract(commissionHT).setScale(0, RoundingMode.HALF_UP);
		commissionHT = commissionTTc.subtract(commissionTVA);
//		commissionPorteur
		BigDecimal commissionPorteurTTc = new BigDecimal(tp.getCommisionPorteur().replace(".", ""));
		BigDecimal commissionPorteurHT = commissionPorteurTTc.divide(tvaDevided, 10, RoundingMode.HALF_UP);
		BigDecimal commissionPorteurTVA = commissionPorteurTTc.subtract(commissionPorteurHT).setScale(0,
				RoundingMode.HALF_UP);
		commissionPorteurHT = commissionPorteurTTc.subtract(commissionTVA);
 
		tp.setValidCommission("1");
 
		if (commpor != null && commpor.getMontantSettlement() != commissionPorteurHT.floatValue()) {
			satimD.setAmountCalculated(satimD.getMontantSettlement());
			satimD.setMontantSettlement(commissionPorteurHT.floatValue());
//			tp.setValidCommission("4");
		}
		if (commporTva != null && commporTva.getMontantSettlement() != commissionPorteurTVA.floatValue()) {
			commporTva.setAmountCalculated(commporTva.getMontantSettlement());
 
			commporTva.setMontantSettlement(commissionPorteurTVA.floatValue());
//			tp.setValidCommission("4");
 
		}
		if (satimD != null && satimD.getMontantSettlement() != satim.floatValue()) {
			satimD.setAmountCalculated(satimD.getMontantSettlement());
			satimD.setMontantSettlement(satim.floatValue());
			tp.setSatimCalculated(Double.toString(satimD.getAmountCalculated() + tvaSatimD.getMontantSettlement()));
 
			tp.setValidCommission("2");
		}
		if (tvaSatimD != null && tvaSatimD.getMontantSettlement() != tvaSatim.floatValue()) {
			tvaSatimD.setAmountCalculated(tvaSatimD.getMontantSettlement());
			tvaSatimD.setMontantSettlement(tvaSatim.floatValue());
			tp.setSatimCalculated(Double.toString(satimD.getAmountCalculated() + tvaSatimD.getAmountCalculated()));
 
			tp.setValidCommission("2");
 
		}
		if (interchangeD != null && interchangeD.getMontantSettlement() != interchange.floatValue()) {
			FileRequest.print(interchange.floatValue() + "", FileRequest.getLineNumber());
			interchangeD.setAmountCalculated(interchangeD.getMontantSettlement());
			interchangeD.setMontantSettlement(interchange.floatValue());
			tp.setInterchangeCalculated(
					Double.toString(interchangeD.getAmountCalculated() + tvaInterchangeD.getMontantSettlement()));
 
			tp.setValidCommission("3");
 
		}
		if (tvaInterchangeD != null && tvaInterchangeD.getMontantSettlement() != interchangeTva.floatValue()) {
			FileRequest.print(interchangeTva.floatValue() + "", FileRequest.getLineNumber());
			tvaInterchangeD.setAmountCalculated(tvaInterchangeD.getMontantSettlement());
			tvaInterchangeD.setMontantSettlement(interchangeTva.floatValue());
			tp.setInterchangeCalculated(
					Double.toString(interchangeD.getAmountCalculated() + tvaInterchangeD.getAmountCalculated()));
 
			tp.setValidCommission("3");
 
		}
		if (commissionHTD != null && commissionHTD.getMontantSettlement() != commissionHT.floatValue()) {
			commissionHTD.setAmountCalculated(commissionHTD.getMontantSettlement());
//			tp.setValidCommission("4");
 
			commissionHTD.setMontantSettlement(commissionHT.floatValue());
		}
		if (commissionTVAD != null && commissionTVAD.getMontantSettlement() != commissionTVA.floatValue()) {
//			tp.setValidCommission("4");
 
			commissionTVAD.setAmountCalculated(commissionTVAD.getMontantSettlement());
 
			commissionTVAD.setMontantSettlement(commissionTVA.floatValue());
		}

 
	}
	
	@Override
	public void run() {

		switch (paramDay) {
		case "1":
			try {
				addG001_G003_cash_out_onUs(fileDate, 1);
				break;
			} catch (Exception e) {
				ExecutorThreadFransaBank.stopThreads  = true;
				FileRequest.print(e.getMessage(), FileRequest.getLineNumber());
				throw e;
			}
		case "2":
			try {
				addG004_G007_cash_out_onUs_AutreAgence(fileDate, 2);
				break;
			} catch (Exception e) {
				ExecutorThreadFransaBank.stopThreads  = true;
				FileRequest.print(e.getMessage(), FileRequest.getLineNumber());
				throw e;
			}
		case "3":
			try {
				addG014_G025_cash_out_offUs_Issuer(fileDate, 3);
				break;
			} catch (Exception e) {
				ExecutorThreadFransaBank.stopThreads  = true;
				FileRequest.print(e.getMessage(), FileRequest.getLineNumber());
				throw e;
			}
		case "4":
			try {
				addG008_G013_cash_out_offUs_Acq(fileDate, 4);
				break;
			} catch (Exception e) {
				ExecutorThreadFransaBank.stopThreads  = true;
				FileRequest.print(e.getMessage(), FileRequest.getLineNumber());
				throw e;
			}
		case "5":
			try {
				addG026_G033_Achat_offus_Issuer_MinMontant(fileDate, 5);
				break;
			} catch (Exception e) {
				ExecutorThreadFransaBank.stopThreads  = true;
				FileRequest.print(e.getMessage(), FileRequest.getLineNumber());
				throw e;
			}
		case "6":
			try {
				addG026_G033_Achat_offus_Issuer_MaxMontant(fileDate, 6);
				break;
			} catch (Exception e) {
				ExecutorThreadFransaBank.stopThreads  = true;
				FileRequest.print(e.getMessage(), FileRequest.getLineNumber());
				throw e;
			}
		case "7":
			try {
				addG034_G040_Achat_offus_Acq_MinMontant(fileDate, 7);
				break;
			} catch (Exception e) {
				ExecutorThreadFransaBank.stopThreads  = true;
				FileRequest.print(e.getMessage(), FileRequest.getLineNumber());
				throw e;
			}
		case "8":
			try {
				addG034_G040_Achat_offus_Acq_MaxMontant(fileDate, 8);
				break;
			} catch (Exception e) {
				ExecutorThreadFransaBank.stopThreads  = true;
				FileRequest.print(e.getMessage(), FileRequest.getLineNumber());
				throw e;
			}
		case "9":
			try {
				addG041_G043_Achat_Onus_MemeAgence_MinMontant(fileDate, 9);
				break;
			} catch (Exception e) {
				ExecutorThreadFransaBank.stopThreads  = true;
				FileRequest.print(e.getMessage(), FileRequest.getLineNumber());
				throw e;
			}

		case "11":
			try {
				addG044_G047_Achat_Onus_AutreAgence_MinMontant(fileDate, 11);
				break;
			} catch (Exception e) {
				ExecutorThreadFransaBank.stopThreads  = true;
				FileRequest.print(e.getMessage(), FileRequest.getLineNumber());
				throw e;
			}
		case "13":
			try {

				addG103_G107_rembourssement_onUs_MemeAgence(fileDate, 13);
				break;
			} catch (Exception e) {
				ExecutorThreadFransaBank.stopThreads  = true;
				FileRequest.print(e.getMessage(), FileRequest.getLineNumber());
				throw e;
			}
		case "14":
			try {
				addG108_G113_rembourssement_onUs_AutreAgence(fileDate, 14);
				break;
			} catch (Exception e) {
				ExecutorThreadFransaBank.stopThreads  = true;
				FileRequest.print(e.getMessage(), FileRequest.getLineNumber());
				throw e;
			}
		case "15":
			try {
				addG097_G102Rembourssement_offUs_Issuer(fileDate, 15);
				break;
			} catch (Exception e) {
				ExecutorThreadFransaBank.stopThreads  = true;
				FileRequest.print(e.getMessage(), FileRequest.getLineNumber());
				throw e;
			}
		case "16":
			try {
				addG048_G052_G094_G096Rembourssement_offUs_Acq(fileDate, 16);
				break;
			} catch (Exception e) {
				ExecutorThreadFransaBank.stopThreads  = true;
				FileRequest.print(e.getMessage(), FileRequest.getLineNumber());
				throw e;
			}
		case "17":
			try {
				addG148_G152_rembourssement_onUs_Internet_MemeAgence(fileDate, 17);
				break;
			} catch (Exception e) {
				ExecutorThreadFransaBank.stopThreads  = true;
				FileRequest.print(e.getMessage(), FileRequest.getLineNumber());
				throw e;
			}
		case "18":
			try {
				addG108_G113_rembourssement_Internet_onUs_AutreAgence(fileDate, 18);
				break;
			} catch (Exception e) {
				ExecutorThreadFransaBank.stopThreads  = true;
				FileRequest.print(e.getMessage(), FileRequest.getLineNumber());
				throw e;
			}
		case "19":
			try {
				addG134_G141Rembourssement_Internet_offUs_Acq(fileDate, 19);
				break;
			} catch (Exception e) {
				ExecutorThreadFransaBank.stopThreads  = true;
				FileRequest.print(e.getMessage(), FileRequest.getLineNumber());
				throw e;
			}
		case "20":
			try {
				addG142_G147Rembourssement_Internet_offUs_Issuer(fileDate, 20);
				break;
			} catch (Exception e) {
				ExecutorThreadFransaBank.stopThreads  = true;
				FileRequest.print(e.getMessage(), FileRequest.getLineNumber());
				throw e;
			}
		case "21":
			try {
				addG114_G117_ConsultationSolde_onUs_MemeAgence(fileDate, 21);
				break;
			} catch (Exception e) {
				ExecutorThreadFransaBank.stopThreads  = true;
				FileRequest.print(e.getMessage(), FileRequest.getLineNumber());
				throw e;
			}
		case "22":
			try {
				addG118_G121_ConsultationSolde_AutreAgence(fileDate, 22);
				break;
			} catch (Exception e) {
				ExecutorThreadFransaBank.stopThreads  = true;
				FileRequest.print(e.getMessage(), FileRequest.getLineNumber());
				throw e;
			}
		case "23":
			try {
				addG127_G133_ConsultationSolde_offUs_Issuer(fileDate, 23);
				break;
			} catch (Exception e) {
				ExecutorThreadFransaBank.stopThreads  = true;
				FileRequest.print(e.getMessage(), FileRequest.getLineNumber());
				throw e;
			}
		case "24":
			try {
				addG122_G126_ConsultationSolde_offUs_Acq(fileDate, 24);
				break;
			} catch (Exception e) {
				ExecutorThreadFransaBank.stopThreads  = true;
				FileRequest.print(e.getMessage(), FileRequest.getLineNumber());
				throw e;
			}
		case "25":
			try {
				addG159_G169_cash_out_offUs_IssuerBanqueException(fileDate, 25);
				break;
			} catch (Exception e) {
				ExecutorThreadFransaBank.stopThreads  = true;
				FileRequest.print(e.getMessage(), FileRequest.getLineNumber());
				throw e;
			}
		case "26":
			try {
				addG170_G175_cash_out_offUs_AcqBanqueException(fileDate, 26);
				break;
			} catch (Exception e) {
				ExecutorThreadFransaBank.stopThreads  = true;
				FileRequest.print(e.getMessage(), FileRequest.getLineNumber());
				throw e;
			}

		case "27":
			try {
				addG081_G086_AchatInternet_Onus_MemeAgence_MinMontant(fileDate, 27);
				break;
			} catch (Exception e) {
				ExecutorThreadFransaBank.stopThreads  = true;
				FileRequest.print(e.getMessage(), FileRequest.getLineNumber());
				throw e;
			}
		case "28":
			try {
				addG081_G086_AchatInternet_Onus_MemeAgence_MaxMontant(fileDate, 28);
				break;
			} catch (Exception e) {
				ExecutorThreadFransaBank.stopThreads  = true;
				FileRequest.print(e.getMessage(), FileRequest.getLineNumber());
				throw e;
			}
		case "29":
			try {
				addG066_G073_Achat_Internet_offus_Issuer(fileDate, 29);
				break;
			} catch (Exception e) {
				ExecutorThreadFransaBank.stopThreads  = true;
				FileRequest.print(e.getMessage(), FileRequest.getLineNumber());
				throw e;
			}
		case "30":
			try {
				addG074_G080_AchatInternet_offus_Acq(fileDate, 30);
				break;
			} catch (Exception e) {
				ExecutorThreadFransaBank.stopThreads  = true;
				FileRequest.print(e.getMessage(), FileRequest.getLineNumber());
				throw e;
			}

		case "31":
			try {
				addG066_G073_Achat_Internet_offus_IssuerBanqueException(fileDate, 31);
				break;
			} catch (Exception e) {
				ExecutorThreadFransaBank.stopThreads  = true;
				FileRequest.print(e.getMessage(), FileRequest.getLineNumber());
				throw e;
			}
		case "32":
			try {
				addG074_G080_AchatInternet_offus_AcqBanqueException(fileDate, 32);
				break;
			} catch (Exception e) {
				ExecutorThreadFransaBank.stopThreads  = true;
				FileRequest.print(e.getMessage(), FileRequest.getLineNumber());
				throw e;
			}

		case "33":
			try {
				addG087_G093_AchatInternet_Onus_AutreAgence_MaxMontant(fileDate, 33);
				break;
			} catch (Exception e) {
				ExecutorThreadFransaBank.stopThreads  = true;
				FileRequest.print(e.getMessage(), FileRequest.getLineNumber());
				throw e;
			}
		case "34":
			try {
				addG044_G047_AchatInternet_Onus_AutreAgence_MinMontant(fileDate, 34);
				break;
			} catch (Exception e) {
				ExecutorThreadFransaBank.stopThreads  = true;
				FileRequest.print(e.getMessage(), FileRequest.getLineNumber());
				throw e;
			}

		case "35":
			try {
				addG193_G199_AchatInternet_offus_AcqExceptionnelMerchant(fileDate, 35);
				break;
			} catch (Exception e) {
				ExecutorThreadFransaBank.stopThreads  = true;
				FileRequest.print(e.getMessage(), FileRequest.getLineNumber());

				throw e;
			}

		case "36":
			try {
				 chargebackonUSAA(fileDate, 36);
				break;
			} catch (Exception e) {
				ExecutorThreadFransaBank.stopThreads  = true;
				FileRequest.print(e.getMessage(), FileRequest.getLineNumber());

				throw e;
			}

		case "37":
			try {
				chargebackonUSMA(fileDate, 37);
				break;
			} catch (Exception e) {
				ExecutorThreadFransaBank.stopThreads  = true;
				FileRequest.print(e.getMessage(), FileRequest.getLineNumber());

				throw e;
			}

		case "38":
			try {
				chargebackOffUSIssuer(fileDate, 38);
				break;
			} catch (Exception e) {
				ExecutorThreadFransaBank.stopThreads  = true;
				FileRequest.print(e.getMessage(), FileRequest.getLineNumber());

				throw e;
			}

		case "39":
			try {
				chargebackOffUSAcq(fileDate, 39);
				break;
			} catch (Exception e) {
				ExecutorThreadFransaBank.stopThreads  = true;
				FileRequest.print(e.getMessage(), FileRequest.getLineNumber());

				throw e;
			}

		default:
			FileRequest.print("done", FileRequest.getLineNumber());

			break;

		}
	}

	public void dayopCreation(DayOperationFransaBank op, FileContent element, String identification) {
		try {
			PropertyUtils.copyProperties(op, element);
		} catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
			// TODO Auto-generated catch block
			String stackTrace = Throwables.getStackTraceAsString(e);
			logger.info("Exception is=>{}", stackTrace);
			// e.printStackTrace();
			stopSavingData();
		}
		op.setRefernceLettrage(element.getNumRefTransaction() + element.getNumtransaction()
				.substring(element.getNumtransaction().length() - 6, element.getNumtransaction().length())
				+ element.getDateTransaction());
		op.setIdenfication(identification);
		op.setFileDate(this.fileDate);
		op.setCompteDebit(getDebit(identification, element));
		op.setCompteCredit(getCredit(identification, element));

	}

	private void stopSavingData() {
		allowEdit = false;
		stopThreads = true;

	}

}
