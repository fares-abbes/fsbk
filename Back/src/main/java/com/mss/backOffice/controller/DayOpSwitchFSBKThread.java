package com.mss.backOffice.controller;

import com.google.common.base.Throwables;
import com.google.gson.Gson;
import com.mss.backOffice.services.FormulaInterpreterService;
import com.mss.unified.entities.*;
import com.mss.unified.repositories.*;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.ContentCachingRequestWrapper;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

//import static com.mss.backOffice.controller.ExecutorThreadMp.Test;

public strictfp class DayOpSwitchFSBKThread implements Runnable {
	public static List<Integer> Test = new ArrayList<Integer>();
	FormulaInterpreterService fIPService;

	public String name;
	private boolean allowEdit;
	private TvaCommissionFransaBank tvaCommissionFransaBank;
	private List<String> codeBanks;
	public static final String CONSULTATIONSOLDE = "014";
	public static final String RETRAIT = "040";
	public static final String RETRAITSC = "040SC";
	public static final String CHANGEMENTPIN = "040SC";

	public static final String ACHATTPE = "050";
	public static final String ACHATInternet = "052";
	public static final String REMBOURSEMENTTPE = "051";
	public static final String REMBOURSEMENTINTERNET = "055";
	public static final String DB = "DB";
	public static final String PA = "PA";
	public static final String RB = "RB";
	public MvbkConfigRepository mvbkConfigR;

	/*
	 * iinternet de22_pos_entry_mode position 7 V de03_processing_code==00 premier
	 * achat de03_processing_code==20 premier REMBOURSEMENT
	 */
	public synchronized int getEveIndex() {
		return (++eve);
	}

	public String getSpace(int count) {
		String Space = "";
		for (int i = 0; i < count; i++)
			Space += " ";
		return Space;
	}

	public static int eve = 0;

	public String toString(Object c) {
		return String.valueOf(c);
	}

	private static final Gson gson = new Gson();

	public String paramDay;
	public String fileDate;

	private SettelementFransaBankRepository settlementRepo;

	private DayOperationFransaBankRepository operationRepo;

	private TvaCommissionFransaBankRepository tvaRepo;

	private BatchesFFCRepository batchRepo;

	private PourcentageCommFSBKRepository pourcentageCommFSBKRepository;

	private SwitchTransactionTRepository switchTransactionTRepository;

	private BinOnUsRepository binOnUsRepository;

	private CodeBankBCRepository codeBankBCRepository;
	private List<AtmTerminal> atmList;
	private List<Account> accountList;
	private MvbkSettlementRepository mvbkSettlementRepository;
	private AgenceAdministrationRepository ag;
	private BkmvtiFransaBankRepository bkmvtiFransaBankRepository;

	private CommissionSwitchRepository cSR;
	public static int index1 = 0;
	public static int index2 = 0;
	private static final Logger logger = LoggerFactory.getLogger(DayOpSwitchFSBKThread.class);

	public DayOpSwitchFSBKThread() {
		super();

	}

	public DayOpSwitchFSBKThread(String paramDay, FormulaInterpreterService fIPService,
			BinOnUsRepository binOnUsRepository, SwitchTransactionTRepository switchTransactionTRepository,
			SettelementFransaBankRepository settlementRepo, DayOperationFransaBankRepository operationRepo,
			TvaCommissionFransaBankRepository tvaRepo, BatchesFFCRepository batchRepo,
			PourcentageCommFSBKRepository pourcentageCommFSBKRepository, CodeBankBCRepository codeBankBCRepository,
			MvbkSettlementRepository mvbkSettlementRepository, BkmvtiFransaBankRepository bkmvtiFransaBankRepository,
			AgenceAdministrationRepository ag, MerchantRepository mr, AccountRepository accR,
			CommissionSwitchRepository cSR, MvbkConfigRepository mvbkConfigR, List<AtmTerminal> atmList,
			List<Account> merchant, String name) {
		super();
		this.allowEdit = true;
		this.settlementRepo = settlementRepo;
		this.operationRepo = operationRepo;
		this.paramDay = paramDay;
		this.tvaRepo = tvaRepo;
		this.batchRepo = batchRepo;
		this.pourcentageCommFSBKRepository = pourcentageCommFSBKRepository;
		this.switchTransactionTRepository = switchTransactionTRepository;
		this.tvaCommissionFransaBank = tvaRepo.findTva();
		this.binOnUsRepository = binOnUsRepository;
		this.codeBankBCRepository = codeBankBCRepository;
		this.mvbkSettlementRepository = mvbkSettlementRepository;
		this.ag = ag;
		this.mr = mr;
		this.accR = accR;
		this.cSR = cSR;
		this.bkmvtiFransaBankRepository = bkmvtiFransaBankRepository;
		this.atmList = atmList;
		this.accountList = merchant;
		this.name = name;
		this.mvbkConfigR = mvbkConfigR;
		this.fIPService = fIPService;

	}

	public String getZero(int count)

	{

		String zero = "";

		for (int i = 0; i < count; i++)

			zero += "0";

		return zero;

	}

	MerchantRepository mr;
	AccountRepository accR;
	float fff = 100;
	public static Integer eve1 = 0;

	public int getEveIndex1() {
		synchronized (eve1) {
			eve1 = eve1 + 1;
		}

		return eve1;
	}

	public void getDebitEtCredit(SettelementFransaBank sp, DayOperationFransaBank el) {

		if ("PORTEUR".equals(sp.getCreditAccount())) {
		} else if ("COMMERCANT".equals(sp.getCreditAccount())) {
			el.setCompteCredit(el.getNumRIBcommercant());
		} else if ("ATM".equals(sp.getCreditAccount())) {
			el.setCompteCredit(getAccountAtm(el.getIdTerminal().trim()));
			// el.setCompteCredit(el.getNumRIBcommercant());
		} else {

			el.setCompteCredit(sp.getCreditAccount());
		}
		if ("PORTEUR".equals(sp.getDebitAccount())) {
		} else if ("COMMERCANT".equals(sp.getDebitAccount())) {

			el.setCompteDebit(el.getNumRIBcommercant());

		} else {
			el.setCompteDebit(sp.getDebitAccount());
		}
	}

	public void getCredit(SettelementFransaBank sp, DayOperationFransaBank el) {
		if ("COMMERCANT".equals(sp.getCreditAccount())) {
			el.setCompteCredit(el.getNumRIBcommercant());
		} else if ("ATM".equals(sp.getCreditAccount())) {
			el.setCompteCredit(getAccountAtm(el.getIdCommercant().trim()));
			// el.setCompteCredit(el.getNumRIBcommercant());
		} else if ("PORTEUR".equals(sp.getDebitAccount())) {
			el.setCompteCredit(el.getNumRIBEmetteur());
		} else {

			el.setCompteCredit(sp.getCreditAccount());
		}
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

	public void fromSwitchtoDayOp(DayOperationFransaBank op, SwitchTransactionT element, int header,
			String typeTransaction, String identifier, String Agence) {
		FileRequest.print("start copy", FileRequest.getLineNumber());
		String codeAgenceAtm = element.getSwitchAcceptorTerminalId().replace("FSB", "").replace("NC", "");
		String codeAgenceClient = element.getSwitchAccountId1().substring(2, 7);
//		FileRequest.print("  copy", FileRequest.getLineNumber());

		// Parse the input date as LocalDate with two-digit year
		DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("yyMMdd");
		LocalDate localDate = LocalDate.parse(element.getSwitchDateLocalTransaction(), inputFormatter);
		DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("yyyyMMdd");
//		FileRequest.print("  copy", FileRequest.getLineNumber());

		op.setDateTransaction(localDate.format(outputFormatter));
		op.setIdHeder(String.valueOf(header));
		op.setNumAutorisation(String.format("%015d", Integer.valueOf(element.getSwitchAuthNumber())));
//		FileRequest.print("  copy", FileRequest.getLineNumber());

		if (element.getSwitchAmountTransaction() != null
				&& Integer.valueOf(element.getSwitchAmountTransaction()) != 0) {

			if (element.getSwitchAmountTransaction().contains(".")) {
				int value = Integer.parseInt(element.getSwitchAmountTransaction().replace(".", ""));
				op.setMontantTransaction(String.format("%014d", value));

			} else {
				int value = Integer.parseInt(element.getSwitchAmountTransaction());
				op.setMontantTransaction(String.format("%014d", value));
			}

		} else {
			op.setMontantTransaction("00000000000000");

		}
//		FileRequest.print("  copy", FileRequest.getLineNumber());

		op.setNumRefTransaction(element.getSwitchRRN());
		op.setNumtransaction(element.getSwitchRRN());
		String switchAuthNumber = element.getSwitchAuthNumber();
//		FileRequest.print("  copy", FileRequest.getLineNumber());

		op.setNumAutorisation(StringUtils.leftPad(switchAuthNumber, 15, '0'));
		op.setLibelleCommercant(element.getSwitchAcceptorAcceptorName());
		op.setPieceComptable(DB + "161" + String.format("%06d", getEveIndex1()));
		op.setNumCartePorteur(element.getSwitchPan());
		op.setHeureTransaction(String.format("%02d", element.getSwitchResponseDate().getHours())
				+ String.format("%02d", element.getSwitchResponseDate().getMinutes())
				+ String.format("%02d", element.getSwitchResponseDate().getSeconds()));
		op.setTypeTransaction(typeTransaction);
//		FileRequest.print("  copy", FileRequest.getLineNumber());

		String banque = element.getSwitchAcquirerIdenCode();
		op.setCodeBankAcquereur(String.format("%03d", Integer.parseInt(banque)));
		op.setIdCommercant(element.getSwitchAcceptorMerchantCode());
		op.setIdTerminal(element.getSwitchAcceptorTerminalId());
//		FileRequest.print("  copy", FileRequest.getLineNumber());

		if (element.getSwitchAccountId1().length() == 19) {
			String account = "0" + element.getSwitchAccountId1();
			op.setNumRIBEmetteur(account);
			// op.setCompteCredit(account);
			op.setCompteDebit(account);
		} else {
			op.setNumRIBEmetteur(element.getSwitchAccountId1());
			// op.setCompteCredit(element.getSwitchAccountId1());
			op.setCompteDebit(element.getSwitchAccountId1());
		}
//		element.getSwitchAtm
//		FileRequest.print("  copy", FileRequest.getLineNumber());

		op.setCodeBin(op.getNumCartePorteur().substring(0, 6));
		op.setCodeBank("035");
		op.setCodeDebitCommercant("C");
		op.setCodeDebitPorteur("D");
//		FileRequest.print("  copy", FileRequest.getLineNumber());

		op.setRefernceLettrage(
				element.getSwitchRRN() + op.getNumAutorisation().substring(op.getNumAutorisation().length() - 6,
						op.getNumAutorisation().length()) + element.getSwitchDateLocalTransaction());
		List<BinOnUs> ListofBins = binOnUsRepository.findAll();
		op.setCodeBin(op.getNumCartePorteur().substring(0, 6));
		op.setIdenfication(identifier);
		op.setNumRIBcommercant(element.getAccountCommercant());

		op.setCodeAgence(element.getAccountCommercant().substring(2, 7));

		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyMMdd");
		String fileDate = LocalDate.now().format(formatter);
		op.setFileDate(fileDate);
		element.setScriptExcDate(ExecutorSwitch.dateExc.toString());

	}

	public void cash_out_onUs_A_AgenceSC(int id) {
		try {
			List<DayOperationFransaBank> dayOperations = new ArrayList<DayOperationFransaBank>();
			TvaCommissionFransaBank tvaCommissionFransaBank = tvaRepo.findTva();
			List<SwitchTransactionT> switchList = switchTransactionTRepository.getTransactionsRetraitOnUSAASC();
			FileRequest.print(" " + switchList.size(), FileRequest.getLineNumber());

			for (SwitchTransactionT element : switchList) {
				if (ExecutorThreadFransaBank.stopThreads) {
					return;
				}
				int indexPieceComptable = getEveIndex1();
				DayOperationFransaBank S1004 = new DayOperationFransaBank();
				DayOperationFransaBank S1005 = new DayOperationFransaBank();
				DayOperationFransaBank S1006 = new DayOperationFransaBank();

				String typeTransaction = RETRAIT;

				fromSwitchtoDayOp(S1004, element, id, typeTransaction, "S1004", "agence");
				getCredit(settlementRepo.findByIdentificationbh("S1004"), S1004);

				fromSwitchtoDayOp(S1005, element, id, typeTransaction, "S1005", "client");
				getCredit(settlementRepo.findByIdentificationbh("S1005"), S1005);

				fromSwitchtoDayOp(S1006, element, id, typeTransaction, "S1006", "client");
				getCredit(settlementRepo.findByIdentificationbh("S1006"), S1006);



				int tva = Integer.parseInt(tvaCommissionFransaBank.getTva());

				//////////////////////////////////////////////

				//////////////////////////////////////////////////////////////////////////

				float montantTransaction = Float.parseFloat(S1004.getMontantTransaction());


				///////////////// *********** END G004********////////////////////////////

				//////////////// **********START G005***********//////////////////////////
				S1004.setMontantSettlement(montantTransaction);

				int lengPieceComptable6 = S1006.getPieceComptable().length();
				S1004.setPieceComptable(DB + "161" + String.format("%06d", indexPieceComptable));

				CommissionSwitch com = cSR.findByTypeTransaction(RETRAITSC);

				/////////////// **********END G005*********///////////////////////////////

				//////////////// **********START G006***********//////////////////////////
				float commission = Float.parseFloat(com.getCommissionFix());

				S1005.setMontantSettlement((commission));

				int lengPieceComptable4 = S1004.getPieceComptable().length();
				S1005.setPieceComptable(DB + "161" + String.format("%06d", indexPieceComptable)); // G005.setPieceComptable(DB+element.getTypeTransaction()+String.format("%06d",index));

				/////////////// **********END G006*********///////////////////////////////

				///////////// ***********START G007 ********/////////////////////////
				float fff = 100;
//				new commission *tva
				float commssionTvaArr = ((commission * tva) / fff);

				S1006.setMontantSettlement((commssionTvaArr));

				S1006.setPieceComptable(DB + "161" + String.format("%06d", indexPieceComptable));

				///////////// ***********END G010 ********/////////////////////////

				dayOperations.add(S1004);
				dayOperations.add(S1005);
				dayOperations.add(S1006);

			}

			saveInBatches(dayOperations, id, switchList);
			logger.info("cash_out_onUs_A_Agence");

//				batchRepo.updateFinishBatch("TH" + threadNB, 1, new Date());

		} catch (Exception e) {

			System.out.println("----------");
			System.out.println(e);
			e.printStackTrace();
			FileRequest.print(e.toString(), FileRequest.getLineNumber());

			logger.error("some exception message", e);
			FileRequest.print(e.getStackTrace().toString(), FileRequest.getLineNumber());
			catchError(e, id);

		}

	}

	public void cash_out_onUs_M_AgenceSC(int id) {
		try {
			List<DayOperationFransaBank> dayOperations = new ArrayList<DayOperationFransaBank>();
			TvaCommissionFransaBank tvaCommissionFransaBank = tvaRepo.findTva();
			List<SwitchTransactionT> switchList = switchTransactionTRepository.getTransactionsRetraitOnUSMASC();
			FileRequest.print(" " + switchList.size(), FileRequest.getLineNumber());

			for (SwitchTransactionT element : switchList) {
				if (ExecutorThreadFransaBank.stopThreads) {
					return;
				}
				int indexPieceComptable = getEveIndex1();
				DayOperationFransaBank S1001 = new DayOperationFransaBank();
				DayOperationFransaBank S1002 = new DayOperationFransaBank();
				DayOperationFransaBank S1003 = new DayOperationFransaBank();
				String typeTransaction = RETRAIT;
				FileRequest.print("  copy", FileRequest.getLineNumber());

				fromSwitchtoDayOp(S1001, element, id, typeTransaction, "S1001", "client");
				getCredit(settlementRepo.findByIdentificationbh("S1001"), S1001);

				fromSwitchtoDayOp(S1002, element, id, typeTransaction, "S1002", "client");
				getCredit(settlementRepo.findByIdentificationbh("S1002"), S1002);

				fromSwitchtoDayOp(S1003, element, id, typeTransaction, "S1003", "client");
				getCredit(settlementRepo.findByIdentificationbh("S1003"), S1003);

				/////////////////// ********START G001**********///////////////////////////

				//////////////////////////////////////////////////////////////////////////
				S1001.setPieceComptable(DB + "161" + String.format("%06d", indexPieceComptable));
				S1002.setPieceComptable(DB + "161" + String.format("%06d", indexPieceComptable));
				S1003.setPieceComptable(DB + "161" + String.format("%06d", indexPieceComptable));

				/////////////////////////////////////////////////////////////////////////

				////////////////////////////////////////////////////////////////////////

				//////////////////////////////////////////////////////////////////////////
				// float montantTransaction = S001.getMontantTransaction().replace(".", "");
				float montantTransaction = Float.parseFloat(S1001.getMontantTransaction());
				int tva = Integer.parseInt(tvaCommissionFransaBank.getTva());

				CommissionSwitch com = cSR.findByTypeTransaction(RETRAITSC);
				float commission = Float.parseFloat(com.getCommissionFix());

				float commssionTvaArr = ((commission * tva) / fff);

				/////////////////////////////////////////////////////////////////////

				S1001.setMontantSettlement(montantTransaction);

				S1002.setMontantSettlement((commission));
				S1003.setMontantSettlement((commssionTvaArr));

				dayOperations.add(S1001);
				dayOperations.add(S1002);
				dayOperations.add(S1003);
			}
			saveInBatches(dayOperations, id, switchList);

			logger.info("cash_out_onUs_M_Agence");

		} catch (Exception e) {
			catchError(e, id);

		}

	}
///---retrais---------//
	/////////////// ******** retrais ******** /////////////////////
	public void addRetrais(int id) {
		cash_out_onUs_M_Agence(id);
		cash_out_onUs_A_Agence(id);
	}
	public void addRetraisSC(int id) {
		cash_out_onUs_M_AgenceSC(id);
		cash_out_onUs_A_AgenceSC(id);
	}
	public void changeCardPIN(int id){
		try {
			List<DayOperationFransaBank> dayOperations = new ArrayList<DayOperationFransaBank>();
			TvaCommissionFransaBank tvaCommissionFransaBank = tvaRepo.findTva();
			List<SwitchTransactionT> switchList = switchTransactionTRepository.getChangementPIN();
			FileRequest.print(" " + switchList.size(), FileRequest.getLineNumber());

			for (SwitchTransactionT element : switchList) {
				if (ExecutorThreadFransaBank.stopThreads) {
					return;
				}
				int indexPieceComptable = getEveIndex1();
				DayOperationFransaBank S1020 = new DayOperationFransaBank();
				DayOperationFransaBank S1030 = new DayOperationFransaBank();
				String typeTransaction = CHANGEMENTPIN;
				FileRequest.print("  copy", FileRequest.getLineNumber());

				fromSwitchtoDayOp(S1020, element, id, typeTransaction, "S1020", "client");
				getCredit(settlementRepo.findByIdentificationbh("S1020"), S1020);

				fromSwitchtoDayOp(S1030, element, id, typeTransaction, "S1030", "client");
				getCredit(settlementRepo.findByIdentificationbh("S1030"), S1030);




				/////////////////// ********START G001**********///////////////////////////

				//////////////////////////////////////////////////////////////////////////
				S1020.setPieceComptable(DB + "161" + String.format("%06d", indexPieceComptable));
				S1030.setPieceComptable(DB + "161" + String.format("%06d", indexPieceComptable));

				/////////////////////////////////////////////////////////////////////////

				////////////////////////////////////////////////////////////////////////

				//////////////////////////////////////////////////////////////////////////
				// float montantTransaction = S001.getMontantTransaction().replace(".", "");
				float montantTransaction = Float.parseFloat(S1020.getMontantTransaction());
				int tva = Integer.parseInt(tvaCommissionFransaBank.getTva());

				CommissionSwitch com = cSR.findByTypeTransaction(S1030.getTypeTransaction());
				float commission = Float.parseFloat(com.getCommissionFix());

				float commssionTvaArr = ((commission * tva) / fff);

				/////////////////////////////////////////////////////////////////////


				S1020.setMontantSettlement((commission));
				S1030.setMontantSettlement((commssionTvaArr));

				dayOperations.add(S1020);
				dayOperations.add(S1030);
 			}
			saveInBatches(dayOperations, id, switchList);

			logger.info("changeCardPIN");

		} catch (Exception e) {
			catchError(e, id);

		}

	}

	public void addChargeBack(int id) {
		chargeback_out_onUs_M_Agence(id);
		chargeback_onUs_A_Agence(id);
	}

	public void cash_out_onUs_M_Agence(int id) {
		try {
			List<DayOperationFransaBank> dayOperations = new ArrayList<DayOperationFransaBank>();
			TvaCommissionFransaBank tvaCommissionFransaBank = tvaRepo.findTva();
			List<SwitchTransactionT> switchList = switchTransactionTRepository.getTransactionsRetraitOnUSMA();
			FileRequest.print(" " + switchList.size(), FileRequest.getLineNumber());

			for (SwitchTransactionT element : switchList) {
				if (ExecutorThreadFransaBank.stopThreads) {
					return;
				}
				int indexPieceComptable = getEveIndex1();
				DayOperationFransaBank S108 = new DayOperationFransaBank();
				DayOperationFransaBank S109 = new DayOperationFransaBank();
				DayOperationFransaBank S110 = new DayOperationFransaBank();
				String typeTransaction = RETRAIT;
				FileRequest.print("  copy", FileRequest.getLineNumber());

				fromSwitchtoDayOp(S108, element, id, typeTransaction, "S108", "client");
				getCredit(settlementRepo.findByIdentificationbh("S108"), S108);

				fromSwitchtoDayOp(S109, element, id, typeTransaction, "S109", "client");
				getCredit(settlementRepo.findByIdentificationbh("S109"), S109);

				fromSwitchtoDayOp(S110, element, id, typeTransaction, "S110", "client");
				getCredit(settlementRepo.findByIdentificationbh("S110"), S110);

				/////////////////// ********START G001**********///////////////////////////

				//////////////////////////////////////////////////////////////////////////
				S108.setPieceComptable(DB + "161" + String.format("%06d", indexPieceComptable));
				S109.setPieceComptable(DB + "161" + String.format("%06d", indexPieceComptable));
				S110.setPieceComptable(DB + "161" + String.format("%06d", indexPieceComptable));

				/////////////////////////////////////////////////////////////////////////

				////////////////////////////////////////////////////////////////////////

				//////////////////////////////////////////////////////////////////////////
				// float montantTransaction = S001.getMontantTransaction().replace(".", "");
				float montantTransaction = Float.parseFloat(S108.getMontantTransaction());
				int tva = Integer.parseInt(tvaCommissionFransaBank.getTva());

				CommissionSwitch com = cSR.findByTypeTransaction(S108.getTypeTransaction());
				float commission = Float.parseFloat(com.getCommissionFix());

				float commssionTvaArr = ((commission * tva) / fff);

				/////////////////////////////////////////////////////////////////////

				S108.setMontantSettlement(montantTransaction);

				S109.setMontantSettlement((commission));
				S110.setMontantSettlement((commssionTvaArr));

				dayOperations.add(S108);
				dayOperations.add(S109);
				dayOperations.add(S110);
			}
			saveInBatches(dayOperations, id, switchList);

			logger.info("cash_out_onUs_M_Agence");

		} catch (Exception e) {
			catchError(e, id);

		}

	}


	public void cash_out_onUs_A_Agence(int id) {
		try {
			List<DayOperationFransaBank> dayOperations = new ArrayList<DayOperationFransaBank>();
			TvaCommissionFransaBank tvaCommissionFransaBank = tvaRepo.findTva();
			List<SwitchTransactionT> switchList = switchTransactionTRepository.getTransactionsRetraitOnUSAA();
			FileRequest.print(" " + switchList.size(), FileRequest.getLineNumber());

			for (SwitchTransactionT element : switchList) {
				if (ExecutorThreadFransaBank.stopThreads) {
					return;
				}
				int indexPieceComptable = getEveIndex1();
				DayOperationFransaBank S004 = new DayOperationFransaBank();
				DayOperationFransaBank S005 = new DayOperationFransaBank();
				DayOperationFransaBank S006 = new DayOperationFransaBank();
				DayOperationFransaBank S007 = new DayOperationFransaBank();

				String typeTransaction = RETRAIT;

				fromSwitchtoDayOp(S004, element, id, typeTransaction, "S004", "agence");
				getCredit(settlementRepo.findByIdentificationbh("S004"), S004);

				fromSwitchtoDayOp(S005, element, id, typeTransaction, "S005", "client");
				getCredit(settlementRepo.findByIdentificationbh("S005"), S005);

				fromSwitchtoDayOp(S006, element, id, typeTransaction, "S006", "client");
				getCredit(settlementRepo.findByIdentificationbh("S006"), S006);

				fromSwitchtoDayOp(S007, element, id, typeTransaction, "S007", "client");
				getCredit(settlementRepo.findByIdentificationbh("S007"), S007);

				int tva = Integer.parseInt(tvaCommissionFransaBank.getTva());

				//////////////////////////////////////////////

				//////////////////////////////////////////////////////////////////////////

				float montantTransaction = Float.parseFloat(S004.getMontantTransaction());
				S004.setMontantSettlement(montantTransaction);

				S004.setPieceComptable(DB + "161" + String.format("%06d", indexPieceComptable));

				///////////////// *********** END G004********////////////////////////////

				//////////////// **********START G005***********//////////////////////////
				S005.setMontantSettlement(montantTransaction);

				int lengPieceComptable6 = S006.getPieceComptable().length();
				S005.setPieceComptable(DB + "161" + String.format("%06d", indexPieceComptable));

				CommissionSwitch com = cSR.findByTypeTransaction(S004.getTypeTransaction());

				/////////////// **********END G005*********///////////////////////////////

				//////////////// **********START G006***********//////////////////////////
				float commission = Float.parseFloat(com.getCommissionFix());

				S006.setMontantSettlement((commission));

				int lengPieceComptable4 = S004.getPieceComptable().length();
				S006.setPieceComptable(DB + "161" + String.format("%06d", indexPieceComptable)); // G005.setPieceComptable(DB+element.getTypeTransaction()+String.format("%06d",index));

				/////////////// **********END G006*********///////////////////////////////

				///////////// ***********START G007 ********/////////////////////////
				float fff = 100;
//				new commission *tva
				float commssionTvaArr = ((commission * tva) / fff);

				S007.setMontantSettlement((commssionTvaArr));

				S007.setPieceComptable(DB + "161" + String.format("%06d", indexPieceComptable));

				///////////// ***********END G010 ********/////////////////////////

				dayOperations.add(S004);
				dayOperations.add(S005);
				dayOperations.add(S006);
				dayOperations.add(S007);

			}

			saveInBatches(dayOperations, id, switchList);
			logger.info("cash_out_onUs_A_Agence");

//				batchRepo.updateFinishBatch("TH" + threadNB, 1, new Date());

		} catch (Exception e) {

			System.out.println("----------");
			System.out.println(e);
			e.printStackTrace();
			FileRequest.print(e.toString(), FileRequest.getLineNumber());

			logger.error("some exception message", e);
			FileRequest.print(e.getStackTrace().toString(), FileRequest.getLineNumber());
			catchError(e, id);

		}

	}


	public void chargeback_onUs_A_Agence(int id) {
		try {
			List<DayOperationFransaBank> dayOperations = new ArrayList<DayOperationFransaBank>();
			TvaCommissionFransaBank tvaCommissionFransaBank = tvaRepo.findTva();
			List<SwitchTransactionT> switchList = switchTransactionTRepository.getTransactionsCHRetraitOnUSAA();
			FileRequest.print(" " + switchList.size(), FileRequest.getLineNumber());

			for (SwitchTransactionT element : switchList) {
				if (ExecutorThreadFransaBank.stopThreads) {
					return;
				}
				int indexPieceComptable = getEveIndex1();
				DayOperationFransaBank S101 = new DayOperationFransaBank();


				String typeTransaction = RETRAIT;

				fromSwitchtoDayOp(S101, element, id, typeTransaction, "S101", "agence");
				getCredit(settlementRepo.findByIdentificationbh("S101"), S101);


				//////////////////////////////////////////////

				float montantTransaction = Float.parseFloat(S101.getMontantTransaction());
				S101.setMontantSettlement(montantTransaction);

				S101.setPieceComptable(DB + "950" + String.format("%06d", indexPieceComptable));

				///////////////// *********** END G004********////////////////////////////


				CommissionSwitch com = cSR.findByTypeTransaction(S101.getTypeTransaction());

				/////////////// **********END G005*********///////////////////////////////

				//////////////// **********START G006***********//////////////////////////
				int lengPieceComptable4 = S101.getPieceComptable().length();
				///////////// ***********START G007 ********/////////////////////////
				float fff = 100;
//				new commission *tva
				///////////// ***********END G010 ********/////////////////////////
				dayOperations.add(S101);

			}

			saveInBatches(dayOperations, id, switchList);
			logger.info("cash_out_onUs_A_Agence");

//				batchRepo.updateFinishBatch("TH" + threadNB, 1, new Date());

		} catch (Exception e) {

			System.out.println("----------");
			System.out.println(e);
			e.printStackTrace();
			FileRequest.print(e.toString(), FileRequest.getLineNumber());

			logger.error("some exception message", e);
			FileRequest.print(e.getStackTrace().toString(), FileRequest.getLineNumber());
			catchError(e, id);

		}

	}

	public void chargeback_out_onUs_M_Agence(int id) {
		try {
			List<DayOperationFransaBank> dayOperations = new ArrayList<DayOperationFransaBank>();
			TvaCommissionFransaBank tvaCommissionFransaBank = tvaRepo.findTva();
			List<SwitchTransactionT> switchList = switchTransactionTRepository.getTransactionsCHRetraitOnUSMA();
			FileRequest.print(" " + switchList.size(), FileRequest.getLineNumber());

			for (SwitchTransactionT element : switchList) {
				if (ExecutorThreadFransaBank.stopThreads) {
					return;
				}
				int indexPieceComptable = getEveIndex1();
				DayOperationFransaBank S100 = new DayOperationFransaBank();
				String typeTransaction = RETRAIT;
				FileRequest.print("  copy", FileRequest.getLineNumber());
				fromSwitchtoDayOp(S100, element, id, typeTransaction, "S100", "client");
				getCredit(settlementRepo.findByIdentificationbh("S100"), S100);
				/////////////////// ********START G001**********///////////////////////////
				S100.setPieceComptable(DB + "951" + String.format("%06d", indexPieceComptable));
				//////////////////////////////////////////////////////////////////////////
				// float montantTransaction = S001.getMontantTransaction().replace(".", "");
				float montantTransaction = Float.parseFloat(S100.getMontantTransaction());
				/////////////////////////////////////////////////////////////////////
				S100.setMontantSettlement(montantTransaction);
				dayOperations.add(S100);
			}
			saveInBatches(dayOperations, id, switchList);

			logger.info("cash_out_onUs_M_Agence");

		} catch (Exception e) {
			catchError(e, id);

		}

	}
	/////////////// ******** CS ******** /////////////////////

	public void addConsultationSolde_onUs(int id) {
		onUsCSAutreAgence(id);
		onUsCSMAgence(id);
	}

	private void onUsCSAutreAgence(int id) {
		try {
			List<DayOperationFransaBank> dayOperations = new ArrayList<DayOperationFransaBank>();
			TvaCommissionFransaBank tvaCommissionFransaBank = tvaRepo.findTva();
			List<SwitchTransactionT> switchList = switchTransactionTRepository.getTransactionsCSONUSAA();
			FileRequest.print(" " + switchList.size(), FileRequest.getLineNumber());

			for (SwitchTransactionT element : switchList) {
				if (ExecutorThreadFransaBank.stopThreads) {
					return;
				}
				DayOperationFransaBank s010 = new DayOperationFransaBank();
				DayOperationFransaBank s011 = new DayOperationFransaBank();

				fromSwitchtoDayOp(s010, element, id, CONSULTATIONSOLDE, "S010", "ATM");
				CommissionSwitch com = cSR.findByTypeTransaction(s010.getTypeTransaction());

				double montantCommission = Double.valueOf(com.getCommissionFix());
				s010.setMontantSettlement((float) montantCommission);
				s011.setIdenfication("S011");
				fromSwitchtoDayOp(s011, element, id, CONSULTATIONSOLDE, "S011", "ATM");

				double tvaval = Double.valueOf(tvaCommissionFransaBank.getTva()) / 100;
				double somme = montantCommission * tvaval;
				s011.setMontantSettlement((float) (somme));
				getDebitEtCredit(settlementRepo.findByIdentificationbh("S010"), s010);
				getDebitEtCredit(settlementRepo.findByIdentificationbh("S011"), s011);
//				s010.setCodeAgence(element.getSwitchAccountId1().substring(2, 7));
//				s011.setCodeAgence(element.getSwitchAccountId1().substring(2, 7));
				getCredit(settlementRepo.findByIdentificationbh("S010"), s010);
				getCredit(settlementRepo.findByIdentificationbh("S011"), s011);

				dayOperations.add(s010);
				dayOperations.add(s011);
			}

			saveInBatches(dayOperations, id, switchList);
			logger.info("onUsCSAutreAgence");

//			batchRepo.updateFinishBatch("TH" + threadNB, 1, new Date());

		} catch (Exception e) {
			catchError(e, id);

		}

	}

	private void onUsCSMAgence(int id) {
		try {
			List<DayOperationFransaBank> dayOperations = new ArrayList<DayOperationFransaBank>();
			TvaCommissionFransaBank tvaCommissionFransaBank = tvaRepo.findTva();
			List<SwitchTransactionT> switchList = switchTransactionTRepository.getTransactionsCSONUSMA();
			FileRequest.print(" " + switchList.size(), FileRequest.getLineNumber());

			for (SwitchTransactionT element : switchList) {
				if (ExecutorThreadFransaBank.stopThreads) {
					return;
				}
				DayOperationFransaBank s009 = new DayOperationFransaBank();
				DayOperationFransaBank s008 = new DayOperationFransaBank();

				fromSwitchtoDayOp(s008, element, id, CONSULTATIONSOLDE, "S008", "client");
				CommissionSwitch com = cSR.findByTypeTransaction(s008.getTypeTransaction());

				double montantCommission = Double.valueOf(com.getCommissionFix());

				s008.setMontantSettlement((float) (montantCommission));
				fromSwitchtoDayOp(s009, element, id, CONSULTATIONSOLDE, "S009", "client");
				double tvaval = Double.valueOf(tvaCommissionFransaBank.getTva()) / 100;
				double somme = montantCommission * tvaval;
				s009.setMontantSettlement((float) (somme));
				getDebitEtCredit(settlementRepo.findByIdentificationbh("S008"), s008);
				getDebitEtCredit(settlementRepo.findByIdentificationbh("S009"), s009);
				s008.setCodeAgence(element.getSwitchAccountId1().substring(2, 7));
				s009.setCodeAgence(element.getSwitchAccountId1().substring(2, 7));
				getCredit(settlementRepo.findByIdentificationbh("S008"), s008);
				getCredit(settlementRepo.findByIdentificationbh("S009"), s009);
				dayOperations.add(s008);
				dayOperations.add(s009);
			}

			saveInBatches(dayOperations, id, switchList);
			logger.info("onUsCSAutreAgence");

		} catch (Exception e) {
			catchError(e, id);

		}

	}
	/////////////// ****************** ACHAT TPE/////////////////////

	public void addAchatTPE(int id) {

		Achat_Onus_MemeAgence(id);
		Achat_Onus_AutreAgence(id);

	}

	public void Achat_Onus_MemeAgence(int id) {
		try {

			List<DayOperationFransaBank> dayOperations = new ArrayList<DayOperationFransaBank>();
			TvaCommissionFransaBank tvaCommissionFransaBank = tvaRepo.findTva();
			List<SwitchTransactionT> switchList = switchTransactionTRepository.getTransactionsAchatTPEOnUSMA();
			FileRequest.print(" " + switchList.size(), FileRequest.getLineNumber());

			for (SwitchTransactionT element : switchList) {
				if (ExecutorThreadFransaBank.stopThreads) {
					return;
				}
				int indexPieceComptable = getEveIndex1();
				DayOperationFransaBank S012 = new DayOperationFransaBank();
				DayOperationFransaBank S013 = new DayOperationFransaBank();
				DayOperationFransaBank S014 = new DayOperationFransaBank();
				String typeTransaction = ACHATTPE;

				fromSwitchtoDayOp(S012, element, id, typeTransaction, "S012", "client");
				fromSwitchtoDayOp(S013, element, id, typeTransaction, "S013", "client");
				fromSwitchtoDayOp(S014, element, id, typeTransaction, "S014", "client");

				String montantTransaction_DA = S012.getMontantTransaction();

				String montantTransaction_ML = montantTransaction_DA;
				int montant = Integer.parseInt(montantTransaction_ML);
				/////////////////// ********START G041 **********///////////////////////////

				float montantTransaction = Float.parseFloat(S012.getMontantTransaction());

				S012.setMontantSettlement(montantTransaction);
				S012.setPieceComptable(PA + "163" + String.format("%06d", indexPieceComptable));
				///////////////// *********** END G041 ********////////////////////////////

				//////////////// **********START G042 ***********//////////////////////////

				CommissionSwitch com = cSR.findByTypeTransaction(S012.getTypeTransaction());

				int commission = Integer.parseInt(com.getCommissionFix());

				S013.setMontantSettlement(commission);

				int lengPieceComptable1 = S012.getPieceComptable().length();
				S013.setPieceComptable(PA + "163" + String.format("%06d", indexPieceComptable));
				/////////////// **********END G042 *********///////////////////////////////

				//////////////// **********START G043 ***********//////////////////////////
				int tVA = Integer.parseInt(tvaCommissionFransaBank.getTva());
				float fff = 100;

				float commssionTvaArr = (commission * tVA / fff);

				S014.setMontantSettlement(commssionTvaArr);

				int lengPieceComptable2 = S013.getPieceComptable().length();
				S014.setPieceComptable(PA + "163" + String.format("%06d", indexPieceComptable));
				getCredit(settlementRepo.findByIdentificationbh("S012"), S012);
				getCredit(settlementRepo.findByIdentificationbh("S013"), S013);
				getCredit(settlementRepo.findByIdentificationbh("S014"), S014);
				/////////////// **********END G043 *********///////////////////////////////
				dayOperations.add(S012);
				dayOperations.add(S013);
				dayOperations.add(S014);

			}

			saveInBatches(dayOperations, id, switchList);

			// batchRepo.updateFinishBatch("TH" + 0, 1, new Date());

		} catch (Exception e) {
			// catchError(e, threadNB);

		}

	}

	public void Achat_Onus_AutreAgence(int id) {
		try {
			List<DayOperationFransaBank> dayOperations = new ArrayList<DayOperationFransaBank>();
			TvaCommissionFransaBank tvaCommissionFransaBank = tvaRepo.findTva();
			List<SwitchTransactionT> switchList = switchTransactionTRepository.getTransactionsAchatTPEOnUSAA();
			FileRequest.print(" " + switchList.size(), FileRequest.getLineNumber());

			for (SwitchTransactionT element : switchList) {
				if (ExecutorThreadFransaBank.stopThreads) {
					return;
				}
				int indexPieceComptable = getEveIndex1();
				DayOperationFransaBank S015 = new DayOperationFransaBank();
				DayOperationFransaBank S016 = new DayOperationFransaBank();
				DayOperationFransaBank S017 = new DayOperationFransaBank();
				DayOperationFransaBank S018 = new DayOperationFransaBank();
				String typeTransaction = ACHATTPE;

				fromSwitchtoDayOp(S015, element, id, typeTransaction, "S015", "agence");
				fromSwitchtoDayOp(S016, element, id, typeTransaction, "S016", "agence");
				fromSwitchtoDayOp(S017, element, id, typeTransaction, "S017", "agence");
				fromSwitchtoDayOp(S018, element, id, typeTransaction, "S018", "agence");

				String montantTransaction_DA = element.getSwitchAmountTransaction().replace(".", "");

				String montantTransaction_ML = montantTransaction_DA;
				int montant = Integer.parseInt(montantTransaction_ML);

				/////////////////// ********START G044 **********///////////////////////////

				float montantTransaction = Float.parseFloat(element.getSwitchAmountTransaction()) * fff;

				S015.setMontantSettlement(montantTransaction);

				S015.setPieceComptable(PA + "163" + String.format("%06d", indexPieceComptable));

				///////////////// *********** END G044 ********////////////////////////////

				//////////////// **********START G045 ***********//////////////////////////

				CommissionSwitch com = cSR.findByTypeTransaction(S016.getTypeTransaction());

				int commission = Integer.parseInt(com.getCommissionFix());

				S016.setMontantSettlement(commission);

				int lengPieceComptable1 = S015.getPieceComptable().length();
				S016.setPieceComptable(PA + "163" + String.format("%06d", indexPieceComptable));

				/////////////// **********END G045 *********///////////////////////////////

				//////////////// **********START G046 ***********//////////////////////////

				int tvaVar = Integer.parseInt(tvaCommissionFransaBank.getTva());

				float fff = 100;

				float commssionTvaArr = ((commission * tvaVar) / fff);

				S017.setMontantSettlement(commssionTvaArr);

				int lengPieceComptable2 = S016.getPieceComptable().length();

				S017.setPieceComptable(PA + "163" + String.format("%06d", indexPieceComptable));

				/////////////// **********END G046 *********///////////////////////////////

				//////////////// **********START G047 ***********//////////////////////////

				S018.setMontantSettlement(montant);

				int lengPieceComptable3 = S017.getPieceComptable().length();

				S018.setPieceComptable(PA + "163" + String.format("%06d", indexPieceComptable));

				/////////////// **********END G047 *********///////////////////////////////
				getCredit(settlementRepo.findByIdentificationbh("S015"), S015);
				getCredit(settlementRepo.findByIdentificationbh("S016"), S016);
				getCredit(settlementRepo.findByIdentificationbh("S017"), S017);
				getCredit(settlementRepo.findByIdentificationbh("S018"), S018);
				dayOperations.add(S015);
				dayOperations.add(S016);
				dayOperations.add(S017);
				dayOperations.add(S018);

			}
			;

			saveInBatches(dayOperations, id, switchList);

//				batchRepo.updateFinishBatch("TH" + threadNB, 1, new Date());

		} catch (Exception e) {
			catchError(e, id);

		}

	}

//	///////////////////// ************* ACHAT INTERNET  *************////////////////////////////
	public void addAchatInternet(int id) {

		AchatInternet_Onus_MemeAgence_MinMontant(id);
		AchatInternet_Onus_MemeAgence_MaxMontant(id);
		AchatInternet_Onus_AutreAgence_MinMontant(id);
		AchatInternet_Onus_AutreAgence_MaxMontant(id);
	}

	public void AchatInternet_Onus_MemeAgence_MinMontant(int id) {
		try {
			List<DayOperationFransaBank> dayOperations = new ArrayList<DayOperationFransaBank>();
			TvaCommissionFransaBank tvaCommissionFransaBank = tvaRepo.findTva();
			List<SwitchTransactionT> switchList = switchTransactionTRepository.getTransactionsAchatInternetOnUSMA();
			FileRequest.print(" " + switchList.size(), FileRequest.getLineNumber());

			for (SwitchTransactionT element : switchList) {
				if (ExecutorThreadFransaBank.stopThreads) {
					return;
				}
				int indexPieceComptable = getEveIndex1();
				DayOperationFransaBank S019 = new DayOperationFransaBank();
				DayOperationFransaBank S020 = new DayOperationFransaBank();
				DayOperationFransaBank S021 = new DayOperationFransaBank();
				String typeTransaction = ACHATInternet;
				fromSwitchtoDayOp(S019, element, id, typeTransaction, "S019", "client");
				fromSwitchtoDayOp(S020, element, id, typeTransaction, "S020", "client");
				fromSwitchtoDayOp(S021, element, id, typeTransaction, "S021", "client");

				String montantTransaction_DA = S019.getMontantTransaction();
				String montantTransaction_ML = montantTransaction_DA;
				int montant = Integer.parseInt(montantTransaction_ML);

				CommissionSwitch com = cSR.findByTypeTransactionAndMinVal(S019.getTypeTransaction(), "0");

				if (montant < Integer.parseInt(com.getMaxVal())) {
					/////////////////// ********START G041 **********///////////////////////////

					float montantTransaction = Float.parseFloat(S019.getMontantTransaction());
					S019.setMontantSettlement(montantTransaction);

					S019.setPieceComptable(PA + "163" + String.format("%06d", indexPieceComptable));

					///////////////// *********** END G081 ********////////////////////////////

					//////////////// **********START G042 ***********//////////////////////////

					int commission = Integer.parseInt(com.getCommissionFix());

					S020.setMontantSettlement(commission);

					S020.setPieceComptable(PA + "163" + String.format("%06d", indexPieceComptable));

					/////////////// **********END G082 *********///////////////////////////////

					//////////////// **********START G083 ***********//////////////////////////

					int tvaR = Integer.parseInt(tvaCommissionFransaBank.getTva());

					float fff = 100;

					float commssionTvaArr = ((commission * tvaR) / fff);

					S021.setMontantSettlement(commssionTvaArr);

					S021.setPieceComptable(PA + "163" + String.format("%06d", indexPieceComptable));

					/////////////// **********END G083 *********///////////////////////////////
					getCredit(settlementRepo.findByIdentificationbh("S019"), S019);
					getCredit(settlementRepo.findByIdentificationbh("S020"), S020);
					getCredit(settlementRepo.findByIdentificationbh("S021"), S021);
					dayOperations.add(S019);
					dayOperations.add(S020);
					dayOperations.add(S021);
				} else {
					element.setScriptExcDate(null);
				}
			}

			saveInBatches(dayOperations, id, switchList);

		} catch (Exception e) {
			catchError(e, id);

		}

	}

	public void AchatInternet_Onus_MemeAgence_MaxMontant(int id) {
		try {

			List<DayOperationFransaBank> dayOperations = new ArrayList<DayOperationFransaBank>();
			TvaCommissionFransaBank tvaCommissionFransaBank = tvaRepo.findTva();
			List<SwitchTransactionT> switchList = switchTransactionTRepository.getTransactionsAchatInternetOnUSMA();
			FileRequest.print(" " + switchList.size(), FileRequest.getLineNumber());

			for (SwitchTransactionT element : switchList) {
				if (ExecutorThreadFransaBank.stopThreads) {
					return;
				}
				int indexPieceComptable = getEveIndex1();
				DayOperationFransaBank S019 = new DayOperationFransaBank();
				DayOperationFransaBank S020 = new DayOperationFransaBank();
				DayOperationFransaBank S021 = new DayOperationFransaBank();

				String typeTransaction = ACHATInternet;
				fromSwitchtoDayOp(S019, element, id, typeTransaction, "S019", "client");
				fromSwitchtoDayOp(S020, element, id, typeTransaction, "S020", "client");
				fromSwitchtoDayOp(S021, element, id, typeTransaction, "S021", "client");
				String montantTransaction_DA = S019.getMontantTransaction();
				String montantTransaction_ML = montantTransaction_DA;
				int montant = Integer.parseInt(montantTransaction_ML);

				CommissionSwitch com = cSR.findByTypeTransactionAndMinVal(S019.getTypeTransaction(), "500000");

				if (montant >= Integer.parseInt(com.getMinVal())) {
					/////////////////// ********START G041 **********///////////////////////////

					float montantTransaction = Float.parseFloat(S019.getMontantTransaction());
					S019.setMontantSettlement(montantTransaction);

					S019.setPieceComptable(PA + "163" + String.format("%06d", indexPieceComptable));

					///////////////// *********** END G081 ********////////////////////////////

					//////////////// **********START G042 ***********//////////////////////////

					int commission = Integer.parseInt(com.getCommissionFix());

					S020.setMontantSettlement(commission);

					S020.setPieceComptable(PA + "163" + String.format("%06d", indexPieceComptable));

					/////////////// **********END G082 *********///////////////////////////////

					//////////////// **********START G083 ***********//////////////////////////

					int tva = Integer.parseInt(tvaCommissionFransaBank.getTva());

					float fff = 100;

					float commssionTvaArr = ((commission * tva) / fff);

					S021.setMontantSettlement(commssionTvaArr);

					S021.setPieceComptable(PA + "163" + String.format("%06d", indexPieceComptable));

					/////////////// **********END G083 *********///////////////////////////////
					getCredit(settlementRepo.findByIdentificationbh("S019"), S019);
					getCredit(settlementRepo.findByIdentificationbh("S020"), S020);
					getCredit(settlementRepo.findByIdentificationbh("S021"), S021);
					dayOperations.add(S019);
					dayOperations.add(S020);
					dayOperations.add(S021);

				} else {
					element.setScriptExcDate(null);
				}
			}

			saveInBatches(dayOperations, id, switchList);

//				batchRepo.updateFinishBatch("TH" + threadNB, 1, new Date());

		} catch (Exception e) {
			catchError(e, id);

		}

	}

	public void AchatInternet_Onus_AutreAgence_MinMontant(int id) {
		try {

			List<DayOperationFransaBank> dayOperations = new ArrayList<DayOperationFransaBank>();
			TvaCommissionFransaBank tvaCommissionFransaBank = tvaRepo.findTva();
			List<SwitchTransactionT> switchList = switchTransactionTRepository.getTransactionsAchatInternetOnUSAA();
			FileRequest.print(" " + switchList.size(), FileRequest.getLineNumber());

			for (SwitchTransactionT element : switchList) {
				if (ExecutorThreadFransaBank.stopThreads) {
					return;
				}
				int indexPieceComptable = getEveIndex1();
				DayOperationFransaBank S022 = new DayOperationFransaBank();
				DayOperationFransaBank S023 = new DayOperationFransaBank();
				DayOperationFransaBank S024 = new DayOperationFransaBank();
				DayOperationFransaBank S025 = new DayOperationFransaBank();
				String typeTransaction = ACHATInternet;
				fromSwitchtoDayOp(S022, element, id, typeTransaction, "S022", "agence");
				fromSwitchtoDayOp(S023, element, id, typeTransaction, "S023", "client");
				fromSwitchtoDayOp(S024, element, id, typeTransaction, "S024", "client");
				fromSwitchtoDayOp(S025, element, id, typeTransaction, "S025", "client");
				String montantTransaction_DA = S022.getMontantTransaction();
				String montantTransaction_ML = montantTransaction_DA;
				int montant = Integer.parseInt(montantTransaction_ML);
				CommissionSwitch com = cSR.findByTypeTransactionAndMinVal(S022.getTypeTransaction(), "0");

				if (montant < Integer.parseInt(com.getMaxVal())) {

					/////////////////// ********START G087 **********///////////////////////////

					float montantTransaction = Float.parseFloat(S022.getMontantTransaction());
					S022.setMontantSettlement(montantTransaction);

					S022.setPieceComptable(PA + "163" + String.format("%06d", indexPieceComptable));

					///////////////// *********** END G087 ********////////////////////////////

					//////////////// **********START G088 ***********//////////////////////////

					int commission = Integer.parseInt(com.getCommissionFix());

					S023.setMontantSettlement(commission);

					S023.setPieceComptable(PA + "163" + String.format("%06d", indexPieceComptable));

					/////////////// **********END G088 *********///////////////////////////////

					//////////////// **********START G089 ***********//////////////////////////

					int tvaVar = Integer.parseInt(tvaCommissionFransaBank.getTva());
					float fff = 100;
					float commssionTvaArr = ((commission * tvaVar) / fff);

					S024.setMontantSettlement(commssionTvaArr);

					S024.setPieceComptable(PA + "163" + String.format("%06d", indexPieceComptable));

					/////////////// **********END G089 *********///////////////////////////////

					///////////// **********START G090 ***********//////////////////////////

					S025.setMontantSettlement(montant);

					S025.setPieceComptable(PA + "163" + String.format("%06d", indexPieceComptable));

					/////////////// **********END G090 *********///////////////////////////////
					getCredit(settlementRepo.findByIdentificationbh("S022"), S022);
					getCredit(settlementRepo.findByIdentificationbh("S023"), S023);
					getCredit(settlementRepo.findByIdentificationbh("S024"), S024);
					getCredit(settlementRepo.findByIdentificationbh("S025"), S025);
					dayOperations.add(S022);
					dayOperations.add(S023);
					dayOperations.add(S024);
					dayOperations.add(S025);

				} else {
					element.setScriptExcDate(null);
				}
			}

			saveInBatches(dayOperations, id, switchList);

		} catch (Exception e) {
			catchError(e, id);

		}

	}

	public void AchatInternet_Onus_AutreAgence_MaxMontant(int id) {
		try {

			List<DayOperationFransaBank> dayOperations = new ArrayList<DayOperationFransaBank>();
			TvaCommissionFransaBank tvaCommissionFransaBank = tvaRepo.findTva();
			List<SwitchTransactionT> switchList = switchTransactionTRepository.getTransactionsAchatInternetOnUSAA();
			FileRequest.print(" " + switchList.size(), FileRequest.getLineNumber());

			for (SwitchTransactionT element : switchList) {
				if (ExecutorThreadFransaBank.stopThreads) {
					return;
				}
				int indexPieceComptable = getEveIndex1();
				DayOperationFransaBank S022 = new DayOperationFransaBank();
				DayOperationFransaBank S023 = new DayOperationFransaBank();
				DayOperationFransaBank S024 = new DayOperationFransaBank();
				DayOperationFransaBank S025 = new DayOperationFransaBank();
				String typeTransaction = ACHATInternet;

				fromSwitchtoDayOp(S022, element, id, typeTransaction, "S022", "client");
				fromSwitchtoDayOp(S023, element, id, typeTransaction, "S023", "client");
				fromSwitchtoDayOp(S024, element, id, typeTransaction, "S024", "client");
				fromSwitchtoDayOp(S025, element, id, typeTransaction, "S025", "agence");

				String montantTransaction_DA = S022.getMontantTransaction();
				String montantTransaction_ML = montantTransaction_DA;
				int montant = Integer.parseInt(montantTransaction_ML);

				CommissionSwitch com = cSR.findByTypeTransactionAndMinVal(S022.getTypeTransaction(), "500000");

				if (montant >= Integer.parseInt(com.getMinVal())) {
					/////////////////// ********START G044 **********///////////////////////////

					float montantTransaction = Float.parseFloat(S022.getMontantTransaction());
					S022.setMontantSettlement(montantTransaction);

					S022.setPieceComptable(PA + "163" + String.format("%06d", indexPieceComptable));

					///////////////// *********** END G087 ********////////////////////////////

					//////////////// **********START G088 ***********//////////////////////////

					int commission = Integer.parseInt(com.getCommissionFix());

					S023.setMontantSettlement(commission);

					S023.setPieceComptable(PA + "163" + String.format("%06d", indexPieceComptable));

					/////////////// **********END G088 *********///////////////////////////////

					//////////////// **********START G089 ***********//////////////////////////

					int tva = Integer.parseInt(tvaCommissionFransaBank.getTva());

					float fff = 100;

					float commssionTvaArr = ((commission * tva) / fff);

					S024.setMontantSettlement(commssionTvaArr);

					S024.setPieceComptable(PA + "163" + String.format("%06d", indexPieceComptable));

					/////////////// **********END G089 *********///////////////////////////////

					//////////////// **********START G090 ***********//////////////////////////

					S025.setMontantSettlement(montant);

					S025.setPieceComptable(PA + "163" + String.format("%06d", indexPieceComptable));

					/////////////// **********END G090 *********///////////////////////////////
					getCredit(settlementRepo.findByIdentificationbh("S022"), S022);
					getCredit(settlementRepo.findByIdentificationbh("S023"), S023);
					getCredit(settlementRepo.findByIdentificationbh("S024"), S024);
					getCredit(settlementRepo.findByIdentificationbh("S025"), S025);
					dayOperations.add(S022);
					dayOperations.add(S023);
					dayOperations.add(S024);
					dayOperations.add(S025);
				} else {
					element.setScriptExcDate(null);
				}
			}
			;

			saveInBatches(dayOperations, id, switchList);

//				batchRepo.updateFinishBatch("TH" + threadNB, 1, new Date());

		} catch (Exception e) {
			catchError(e, id);

		}

	}

//	////////////// *************************** REMBOURSSEMENT SUR TPE ******************///////////////////////////////
	public void addRemboursementTpe(int id) {
		rembourssement_onUs_MemeAgence(id);
		rembourssement_onUs_AutreAgence(id);

	}

	public void rembourssement_onUs_MemeAgence(int id) {
		try {

			List<DayOperationFransaBank> dayOperations = new ArrayList<DayOperationFransaBank>();
			TvaCommissionFransaBank tvaCommissionFransaBank = tvaRepo.findTva();
			List<SwitchTransactionT> switchList = switchTransactionTRepository.getTransactionsRemboursementTPEOnUSMA();
			FileRequest.print(" " + switchList.size(), FileRequest.getLineNumber());

			for (SwitchTransactionT element : switchList) {
				if (ExecutorThreadFransaBank.stopThreads) {
					return;
				}
				int indexPieceComptable = getEveIndex1();
				DayOperationFransaBank S026 = new DayOperationFransaBank();
				DayOperationFransaBank S027 = new DayOperationFransaBank();
				DayOperationFransaBank S028 = new DayOperationFransaBank();

				String typeTransaction = REMBOURSEMENTTPE;
				fromSwitchtoDayOp(S026, element, id, typeTransaction, "S026", "client");
				fromSwitchtoDayOp(S027, element, id, typeTransaction, "S027", "client");
				fromSwitchtoDayOp(S028, element, id, typeTransaction, "S028", "client");
				/////////////////// ********START G103**********///////////////////////////

				float montantTransaction = Float.parseFloat(S026.getMontantTransaction());

				S026.setMontantSettlement(montantTransaction);
				S026.setPieceComptable(RB + "***" + String.format("%06d", indexPieceComptable));

				///////////////// *********** END G103********////////////////////////////

				//////////////// **********START G104***********//////////////////////////

				CommissionSwitch com = cSR.findByTypeTransaction(S026.getTypeTransaction());

				int commission = Integer.parseInt(com.getCommissionFix());

				S027.setMontantSettlement(commission);

				S027.setPieceComptable(RB + "***" + String.format("%06d", indexPieceComptable));

				/////////////// **********END G104*********///////////////////////////////

				//////////////// **********START G105***********//////////////////////////

				int tvarVar = Integer.parseInt(tvaCommissionFransaBank.getTva());
				float fff = 100;
				int commssionTvaArr = Math.round((commission * tvarVar) / fff);

				S028.setMontantSettlement(commssionTvaArr);

				S028.setPieceComptable(RB + "***" + String.format("%06d", indexPieceComptable));

				/////////////// **********END G105*********///////////////////////////////
				getCredit(settlementRepo.findByIdentificationbh("S026"), S026);
				getCredit(settlementRepo.findByIdentificationbh("S027"), S027);
				getCredit(settlementRepo.findByIdentificationbh("S028"), S028);
				dayOperations.add(S026);
				dayOperations.add(S027);
				dayOperations.add(S028);

			}
			;

			saveInBatches(dayOperations, id, switchList);

//				batchRepo.updateFinishBatch("TH" + threadNB, 1, new Date());

		} catch (Exception e) {
			catchError(e, id);

		}

	}

	public void rembourssement_onUs_AutreAgence(int id) {
		try {

			List<DayOperationFransaBank> dayOperations = new ArrayList<DayOperationFransaBank>();
			TvaCommissionFransaBank tvaCommissionFransaBank = tvaRepo.findTva();
			List<SwitchTransactionT> switchList = switchTransactionTRepository.getTransactionsRemboursementTPEOnUSAA();
			FileRequest.print(" " + switchList.size(), FileRequest.getLineNumber());

			for (SwitchTransactionT element : switchList) {
				if (ExecutorThreadFransaBank.stopThreads) {
					return;
				}
				int indexPieceComptable = getEveIndex1();
				DayOperationFransaBank S029 = new DayOperationFransaBank();
				DayOperationFransaBank S030 = new DayOperationFransaBank();
				DayOperationFransaBank S031 = new DayOperationFransaBank();
				DayOperationFransaBank S032 = new DayOperationFransaBank();
				String typeTransaction = REMBOURSEMENTTPE;
				fromSwitchtoDayOp(S029, element, id, typeTransaction, "S029", "client");
				fromSwitchtoDayOp(S030, element, id, typeTransaction, "S030", "client");
				fromSwitchtoDayOp(S031, element, id, typeTransaction, "S031", "client");
				fromSwitchtoDayOp(S032, element, id, typeTransaction, "S032", "agence");

				/////////////////// ********START G108**********///////////////////////////

				float montantTransaction = Float.parseFloat(S029.getMontantTransaction());

				S029.setMontantSettlement(montantTransaction);

				S029.setPieceComptable(RB + "***" + String.format("%06d", indexPieceComptable));

				///////////////// *********** END G108********////////////////////////////

				//////////////// **********START G109***********//////////////////////////

				CommissionSwitch com = cSR.findByTypeTransaction(S029.getTypeTransaction());

				int commission = Integer.parseInt(com.getCommissionFix());

				S030.setMontantSettlement(commission);

				S030.setPieceComptable(RB + "***" + String.format("%06d", indexPieceComptable));

				/////////////// **********END G109*********///////////////////////////////

				//////////////// **********START G110***********//////////////////////////

				int tvaR = Integer.parseInt(tvaCommissionFransaBank.getTva());
				float fff = 100;

				float commssionTvaArr = ((commission * tvaR) / fff);

				S031.setMontantSettlement(commssionTvaArr);

				S031.setPieceComptable(RB + "***" + String.format("%06d", indexPieceComptable));

				/////////////// **********END G110*********///////////////////////////////

				//////////////// **********START G113***********//////////////////////////

				S032.setMontantSettlement((montantTransaction));

				S032.setPieceComptable(RB + "***" + String.format("%06d", indexPieceComptable));

				/////////////// **********END G113 *********///////////////////////////////
				getCredit(settlementRepo.findByIdentificationbh("S029"), S029);
				getCredit(settlementRepo.findByIdentificationbh("S030"), S030);
				getCredit(settlementRepo.findByIdentificationbh("S031"), S031);
				getCredit(settlementRepo.findByIdentificationbh("S032"), S032);
				dayOperations.add(S029);
				dayOperations.add(S030);
				dayOperations.add(S031);
				dayOperations.add(S032);

			}
			;
			saveInBatches(dayOperations, id, switchList);

//				batchRepo.updateFinishBatch("TH" + threadNB, 1, new Date());

		} catch (Exception e) {
			catchError(e, id);

		}

	}

//	////////////// *************************** REMBOURSSEMENT SUR INTERNET  ******************/////////////////////////
	public void addRemboursementInternet(int id) {
		rembourssement_onUs_Internet_MemeAgence(id);
		rembourssement_Internet_onUs_AutreAgence(id);
	}

	public void rembourssement_onUs_Internet_MemeAgence(int id) {
		try {

			List<DayOperationFransaBank> dayOperations = new ArrayList<DayOperationFransaBank>();
			TvaCommissionFransaBank tvaCommissionFransaBank = tvaRepo.findTva();
			List<SwitchTransactionT> switchList = switchTransactionTRepository
					.getTransactionsRemboursementInternetOnUSMA();
			FileRequest.print(" " + switchList.size(), FileRequest.getLineNumber());

			for (SwitchTransactionT element : switchList) {
				if (ExecutorThreadFransaBank.stopThreads) {
					return;
				}
				int indexPieceComptable = getEveIndex1();
				DayOperationFransaBank S033 = new DayOperationFransaBank();
				DayOperationFransaBank S034 = new DayOperationFransaBank();
				DayOperationFransaBank S035 = new DayOperationFransaBank();

				String typeTransaction = REMBOURSEMENTINTERNET;
				fromSwitchtoDayOp(S033, element, id, typeTransaction, "S033", "client");
				fromSwitchtoDayOp(S034, element, id, typeTransaction, "S034", "client");
				fromSwitchtoDayOp(S035, element, id, typeTransaction, "S035", "client");
				/////////////////// ********START G148**********///////////////////////////

				float montantTransaction = Float.parseFloat(S033.getMontantTransaction());

				S033.setMontantSettlement(montantTransaction);

				S033.setPieceComptable(RB + "***" + String.format("%06d", indexPieceComptable));

				///////////////// *********** END G148********////////////////////////////

				//////////////// **********START G149***********//////////////////////////

				CommissionSwitch com = cSR.findByTypeTransaction(S033.getTypeTransaction());

				int commission = Integer.parseInt(com.getCommissionFix());

				S034.setMontantSettlement(commission);

				S034.setPieceComptable(RB + "***" + String.format("%06d", indexPieceComptable));

				/////////////// **********END G149*********///////////////////////////////

				//////////////// **********START G150***********//////////////////////////

				int tvaVar = Integer.parseInt(tvaCommissionFransaBank.getTva());
				float fff = 100;
				float commssionTvaArr = ((commission * tvaVar) / fff);

				S035.setMontantSettlement(commssionTvaArr);
				S035.setPieceComptable(RB + "***" + String.format("%06d", indexPieceComptable));

				/////////////// **********END G150*********///////////////////////////////
				getCredit(settlementRepo.findByIdentificationbh("S033"), S033);
				getCredit(settlementRepo.findByIdentificationbh("S034"), S034);
				getCredit(settlementRepo.findByIdentificationbh("S035"), S035);
				dayOperations.add(S033);
				dayOperations.add(S034);
				dayOperations.add(S035);

			}
			;

			saveInBatches(dayOperations, id, switchList);

//			batchRepo.updateFinishBatch("TH" + threadNB, 1, new Date());

		} catch (Exception e) {
			catchError(e, id);

		}

	}

	public void rembourssement_Internet_onUs_AutreAgence(int id) {
		try {

			List<DayOperationFransaBank> dayOperations = new ArrayList<DayOperationFransaBank>();
			TvaCommissionFransaBank tvaCommissionFransaBank = tvaRepo.findTva();
			List<SwitchTransactionT> switchList = switchTransactionTRepository
					.getTransactionsRemboursementInternetOnUSAA();
			FileRequest.print(" " + switchList.size(), FileRequest.getLineNumber());

			for (SwitchTransactionT element : switchList) {
				if (ExecutorThreadFransaBank.stopThreads) {
					return;
				}
				int indexPieceComptable = getEveIndex1();
				DayOperationFransaBank S036 = new DayOperationFransaBank();
				DayOperationFransaBank S037 = new DayOperationFransaBank();
				DayOperationFransaBank S038 = new DayOperationFransaBank();
				DayOperationFransaBank S039 = new DayOperationFransaBank();

				String typeTransaction = REMBOURSEMENTINTERNET;
				fromSwitchtoDayOp(S036, element, id, typeTransaction, "S036", "client");
				fromSwitchtoDayOp(S037, element, id, typeTransaction, "S037", "client");
				fromSwitchtoDayOp(S038, element, id, typeTransaction, "S038", "client");
				fromSwitchtoDayOp(S039, element, id, typeTransaction, "S039", "agence");
				/////////////////// ********START G153**********///////////////////////////

				float montantTransaction = Float.parseFloat(S036.getMontantTransaction());

				S036.setMontantSettlement(montantTransaction);

				S036.setPieceComptable(RB + "***" + String.format("%06d", indexPieceComptable));

				///////////////// *********** END G153********////////////////////////////

				//////////////// **********START G154***********//////////////////////////

				CommissionSwitch com = cSR.findByTypeTransaction(S036.getTypeTransaction());

				int commission = Integer.parseInt(com.getCommissionFix());

				S037.setMontantSettlement(commission);

				S037.setPieceComptable(RB + "***" + String.format("%06d", indexPieceComptable));

				/////////////// **********END G154*********///////////////////////////////

				//////////////// **********START G155***********//////////////////////////

				int tvaVar = Integer.parseInt(tvaCommissionFransaBank.getTva());
				float fff = 100;

				float commssionTvaArr = ((commission * tvaVar) / fff);

				S038.setMontantSettlement(commssionTvaArr);

				S038.setPieceComptable(RB + "***" + String.format("%06d", indexPieceComptable));

				/////////////// **********END G155*********///////////////////////////////

				//////////////// **********START G158***********//////////////////////////

				int lenghMontantSatim = String.valueOf(montantTransaction).length();
				S039.setMontantSettlement((float) montantTransaction);

				S039.setPieceComptable(RB + "***" + String.format("%06d", indexPieceComptable));

				/////////////// **********END G158 *********///////////////////////////////
				getCredit(settlementRepo.findByIdentificationbh("S036"), S036);
				getCredit(settlementRepo.findByIdentificationbh("S037"), S037);
				getCredit(settlementRepo.findByIdentificationbh("S038"), S038);
				getCredit(settlementRepo.findByIdentificationbh("S039"), S039);
				dayOperations.add(S036);
				dayOperations.add(S037);
				dayOperations.add(S038);
				dayOperations.add(S039);

			}
			;

			saveInBatches(dayOperations, id, switchList);

//				batchRepo.updateFinishBatch("TH" + threadNB, 1, new Date());

		} catch (Exception e) {
			catchError(e, id);

		}

	}

//	// ===========retrais onus=============//
	public void mvbkOnUsMemeAgenceSwitchCH() {

		logger.info("begin mvbkOnUsMemeAgence");
		List<DayOperationFransaBank> dayOperationFransaBanks = operationRepo.findByIdentification("S100");
		int size = 0;
		if (dayOperationFransaBanks.size() > 0) {
			List<BkmvtiFransaBank> BkmvtiFransaBanks = new ArrayList<BkmvtiFransaBank>();

			List<MvbkConf> allMvbkSettelemntsC = mvbkConfigR.findByCategorie(88);

			for (DayOperationFransaBank day : dayOperationFransaBanks) {
				if (ExecutorThreadFransaBank.stopThreads) {
					return;
				}
				int indexPieceComptable = getEveIndex1();

				List<DayOperationFransaBank> dayOperationFransaBanksByNumTransaction = operationRepo
						.getListOnUsMemeAgenceByNumTransaction(day.getNumRefTransaction(), day.getNumAutorisation(),
								day.getNumCartePorteur());
				for (MvbkConf mvk : allMvbkSettelemntsC) {
					int index2 = getEveIndex();
					BkmvtiFransaBanks = TestSigne(1, mvk, dayOperationFransaBanksByNumTransaction, BkmvtiFransaBanks,
							indexPieceComptable, index2, mvk.getCodeSettlement());
				}
			}

			FileRequest.print(BkmvtiFransaBanks.size() + "", FileRequest.getLineNumber());
			FileRequest.print(BkmvtiFransaBanks.toString() + "", FileRequest.getLineNumber());
			if (BkmvtiFransaBanks != null && !BkmvtiFransaBanks.isEmpty()) {
				// Filter out null elements from the list
				List<BkmvtiFransaBank> nonNullBkmvtiFransaBanks = BkmvtiFransaBanks.stream().filter(Objects::nonNull)
						.collect(Collectors.toList());

				if (!nonNullBkmvtiFransaBanks.isEmpty()) {
					bkmvtiFransaBankRepository.saveAll(nonNullBkmvtiFransaBanks);
				}

				size = BkmvtiFransaBanks.size();
			}
		}
		logger.info("end mvbkOnUsMemeAgenceAchatInternet =>{}", size);

	}
	public void mvbkPINChangeMVBK() {

		logger.info("begin mvbkOnUsMemeAgence");
		List<DayOperationFransaBank> dayOperationFransaBanks = operationRepo.findByIdentification("S100");
		int size = 0;
		if (dayOperationFransaBanks.size() > 0) {
			List<BkmvtiFransaBank> BkmvtiFransaBanks = new ArrayList<BkmvtiFransaBank>();

			List<MvbkConf> allMvbkSettelemntsC = mvbkConfigR.findByCategorie(1088);

			for (DayOperationFransaBank day : dayOperationFransaBanks) {
				if (ExecutorThreadFransaBank.stopThreads) {
					return;
				}
				int indexPieceComptable = getEveIndex1();

				List<DayOperationFransaBank> dayOperationFransaBanksByNumTransaction = operationRepo
						.getListOnUsMemeAgenceByNumTransaction(day.getNumRefTransaction(), day.getNumAutorisation(),
								day.getNumCartePorteur());
				for (MvbkConf mvk : allMvbkSettelemntsC) {
					int index2 = getEveIndex();
					BkmvtiFransaBanks = TestSigne(1, mvk, dayOperationFransaBanksByNumTransaction, BkmvtiFransaBanks,
							indexPieceComptable, index2, mvk.getCodeSettlement());
				}
			}

			FileRequest.print(BkmvtiFransaBanks.size() + "", FileRequest.getLineNumber());
			FileRequest.print(BkmvtiFransaBanks.toString() + "", FileRequest.getLineNumber());
			if (BkmvtiFransaBanks != null && !BkmvtiFransaBanks.isEmpty()) {
				// Filter out null elements from the list
				List<BkmvtiFransaBank> nonNullBkmvtiFransaBanks = BkmvtiFransaBanks.stream().filter(Objects::nonNull)
						.collect(Collectors.toList());

				if (!nonNullBkmvtiFransaBanks.isEmpty()) {
					bkmvtiFransaBankRepository.saveAll(nonNullBkmvtiFransaBanks);
				}

				size = BkmvtiFransaBanks.size();
			}
		}
		logger.info("end mvbkOnUsMemeAgenceAchatInternet =>{}", size);

	}

	public void mvbkOnUsAutreAgenceSwitchCH() {

		logger.info("begin mvbkOnUsAutreAgence");
		List<DayOperationFransaBank> dayOperationFransaBanks = operationRepo.findByIdentification("S101");
		int size = 0;
		if (dayOperationFransaBanks.size() > 0) {

			List<BkmvtiFransaBank> BkmvtiFransaBanks = new ArrayList<BkmvtiFransaBank>();
			List<MvbkConf> allMvbkSettelemntsC = mvbkConfigR.findByCategorie(89);

			for (DayOperationFransaBank day : dayOperationFransaBanks) {
				if (ExecutorThreadFransaBank.stopThreads) {
					return;
				}
				int indexPieceComptable = getEveIndex1();

				List<DayOperationFransaBank> dayOperationFransaBanksByNumTransaction = operationRepo
						.getListOnUsMemeAgenceByNumTransaction(day.getNumRefTransaction(), day.getNumAutorisation(),
								day.getNumCartePorteur());
				for (MvbkConf mvk : allMvbkSettelemntsC) {
					int index2 = getEveIndex();
					BkmvtiFransaBanks = TestSigne(1, mvk, dayOperationFransaBanksByNumTransaction, BkmvtiFransaBanks,
							indexPieceComptable, index2, mvk.getCodeSettlement());


				}

			}

			if (BkmvtiFransaBanks != null && !BkmvtiFransaBanks.isEmpty()) {
				// Filter out null elements from the list
				List<BkmvtiFransaBank> nonNullBkmvtiFransaBanks = BkmvtiFransaBanks.stream().filter(Objects::nonNull)
						.collect(Collectors.toList());

				if (!nonNullBkmvtiFransaBanks.isEmpty()) {
					bkmvtiFransaBankRepository.saveAll(nonNullBkmvtiFransaBanks);
				}

				size = BkmvtiFransaBanks.size();
			}
		}
		logger.info("end mvbkOnUsMemeAgenceAchatInternet =>{}", size);

	}
	public void mvbkOnUsMemeAgenceSwitch() {

		logger.info("begin mvbkOnUsMemeAgence");
		List<DayOperationFransaBank> dayOperationFransaBanks = operationRepo.findByIdentification("S001");
		int size = 0;
		if (dayOperationFransaBanks.size() > 0) {
			List<BkmvtiFransaBank> BkmvtiFransaBanks = new ArrayList<BkmvtiFransaBank>();

			List<MvbkConf> allMvbkSettelemntsC = mvbkConfigR.findByCategorie(76);

			for (DayOperationFransaBank day : dayOperationFransaBanks) {
				if (ExecutorThreadFransaBank.stopThreads) {
					return;
				}
				int indexPieceComptable = getEveIndex1();

				List<DayOperationFransaBank> dayOperationFransaBanksByNumTransaction = operationRepo
						.getListOnUsMemeAgenceByNumTransaction(day.getNumRefTransaction(), day.getNumAutorisation(),
								day.getNumCartePorteur());
				for (MvbkConf mvk : allMvbkSettelemntsC) {
					int index2 = getEveIndex();
					BkmvtiFransaBanks = TestSigne(1, mvk, dayOperationFransaBanksByNumTransaction, BkmvtiFransaBanks,
							indexPieceComptable, index2, mvk.getCodeSettlement());
				}
			}
			FileRequest.print(BkmvtiFransaBanks.size() + "", FileRequest.getLineNumber());
			FileRequest.print(BkmvtiFransaBanks.toString() + "", FileRequest.getLineNumber());
			if (BkmvtiFransaBanks != null && !BkmvtiFransaBanks.isEmpty()) {
				// Filter out null elements from the list
				List<BkmvtiFransaBank> nonNullBkmvtiFransaBanks = BkmvtiFransaBanks.stream().filter(Objects::nonNull)
						.collect(Collectors.toList());

				if (!nonNullBkmvtiFransaBanks.isEmpty()) {
					bkmvtiFransaBankRepository.saveAll(nonNullBkmvtiFransaBanks);
				}

				size = BkmvtiFransaBanks.size();
			}
		}
		logger.info("end mvbkOnUsMemeAgenceAchatInternet =>{}", size);

	}

	public void mvbkOnUsAutreAgenceSwitch() {

		logger.info("begin mvbkOnUsAutreAgence");
		List<DayOperationFransaBank> dayOperationFransaBanks = operationRepo.findByIdentification("S004");
		int size = 0;
		if (dayOperationFransaBanks.size() > 0) {

			List<BkmvtiFransaBank> BkmvtiFransaBanks = new ArrayList<BkmvtiFransaBank>();
			List<MvbkConf> allMvbkSettelemntsC = mvbkConfigR.findByCategorie(77);

			for (DayOperationFransaBank day : dayOperationFransaBanks) {
				if (ExecutorThreadFransaBank.stopThreads) {
					return;
				}
				int indexPieceComptable = getEveIndex1();

				List<DayOperationFransaBank> dayOperationFransaBanksByNumTransaction = operationRepo
						.getListOnUsMemeAgenceByNumTransaction(day.getNumRefTransaction(), day.getNumAutorisation(),
								day.getNumCartePorteur());
				for (MvbkConf mvk : allMvbkSettelemntsC) {
					int index2 = getEveIndex();
					BkmvtiFransaBanks = TestSigne(1, mvk, dayOperationFransaBanksByNumTransaction, BkmvtiFransaBanks,
							indexPieceComptable, index2, mvk.getCodeSettlement());

				}

			}

			if (BkmvtiFransaBanks != null && !BkmvtiFransaBanks.isEmpty()) {
				// Filter out null elements from the list
				List<BkmvtiFransaBank> nonNullBkmvtiFransaBanks = BkmvtiFransaBanks.stream().filter(Objects::nonNull)
						.collect(Collectors.toList());

				if (!nonNullBkmvtiFransaBanks.isEmpty()) {
					bkmvtiFransaBankRepository.saveAll(nonNullBkmvtiFransaBanks);
				}

				size = BkmvtiFransaBanks.size();
			}
		}
		logger.info("end mvbkOnUsMemeAgenceAchatInternet =>{}", size);

	}
	public void mvbkOnUsMemeAgenceSwitchCS() {

		logger.info("begin mvbkOnUsMemeAgence");
		List<DayOperationFransaBank> dayOperationFransaBanks = operationRepo.findByIdentification("S1001");
		int size = 0;
		if (dayOperationFransaBanks.size() > 0) {
			List<BkmvtiFransaBank> BkmvtiFransaBanks = new ArrayList<BkmvtiFransaBank>();

			List<MvbkConf> allMvbkSettelemntsC = mvbkConfigR.findByCategorie(1076);

			for (DayOperationFransaBank day : dayOperationFransaBanks) {
				if (ExecutorThreadFransaBank.stopThreads) {
					return;
				}
				int indexPieceComptable = getEveIndex1();

				List<DayOperationFransaBank> dayOperationFransaBanksByNumTransaction = operationRepo
						.getListOnUsMemeAgenceByNumTransaction(day.getNumRefTransaction(), day.getNumAutorisation(),
								day.getNumCartePorteur());
				for (MvbkConf mvk : allMvbkSettelemntsC) {
					int index2 = getEveIndex();
					BkmvtiFransaBanks = TestSigne(1, mvk, dayOperationFransaBanksByNumTransaction, BkmvtiFransaBanks,
							indexPieceComptable, index2, mvk.getCodeSettlement());
				}
			}
			FileRequest.print(BkmvtiFransaBanks.size() + "", FileRequest.getLineNumber());
			FileRequest.print(BkmvtiFransaBanks.toString() + "", FileRequest.getLineNumber());
			if (BkmvtiFransaBanks != null && !BkmvtiFransaBanks.isEmpty()) {
				// Filter out null elements from the list
				List<BkmvtiFransaBank> nonNullBkmvtiFransaBanks = BkmvtiFransaBanks.stream().filter(Objects::nonNull)
						.collect(Collectors.toList());

				if (!nonNullBkmvtiFransaBanks.isEmpty()) {
					bkmvtiFransaBankRepository.saveAll(nonNullBkmvtiFransaBanks);
				}

				size = BkmvtiFransaBanks.size();
			}
		}
		logger.info("end mvbkOnUsMemeAgenceAchatInternet =>{}", size);

	}

	public void mvbkOnUsAutreAgenceSwitchCS() {

		logger.info("begin mvbkOnUsAutreAgence");
		List<DayOperationFransaBank> dayOperationFransaBanks = operationRepo.findByIdentification("S1004");
		int size = 0;
		if (dayOperationFransaBanks.size() > 0) {

			List<BkmvtiFransaBank> BkmvtiFransaBanks = new ArrayList<BkmvtiFransaBank>();
			List<MvbkConf> allMvbkSettelemntsC = mvbkConfigR.findByCategorie(1077);

			for (DayOperationFransaBank day : dayOperationFransaBanks) {
				if (ExecutorThreadFransaBank.stopThreads) {
					return;
				}
				int indexPieceComptable = getEveIndex1();

				List<DayOperationFransaBank> dayOperationFransaBanksByNumTransaction = operationRepo
						.getListOnUsMemeAgenceByNumTransaction(day.getNumRefTransaction(), day.getNumAutorisation(),
								day.getNumCartePorteur());
				for (MvbkConf mvk : allMvbkSettelemntsC) {
					int index2 = getEveIndex();
					BkmvtiFransaBanks = TestSigne(1, mvk, dayOperationFransaBanksByNumTransaction, BkmvtiFransaBanks,
							indexPieceComptable, index2, mvk.getCodeSettlement());

				}

			}

			if (BkmvtiFransaBanks != null && !BkmvtiFransaBanks.isEmpty()) {
				// Filter out null elements from the list
				List<BkmvtiFransaBank> nonNullBkmvtiFransaBanks = BkmvtiFransaBanks.stream().filter(Objects::nonNull)
						.collect(Collectors.toList());

				if (!nonNullBkmvtiFransaBanks.isEmpty()) {
					bkmvtiFransaBankRepository.saveAll(nonNullBkmvtiFransaBanks);
				}

				size = BkmvtiFransaBanks.size();
			}
		}
		logger.info("end mvbkOnUsMemeAgenceAchatInternet =>{}", size);

	}

	// *************Consultation solde*****//
	public void mvbkONUdMemeAgenceConsultationSoldeSwitch() {

		logger.info("begin mvbkONUdMemeAgenceConsultationSolde");
		List<DayOperationFransaBank> dayOperationFransaBanks = operationRepo.findByIdentification("S008");
		int size = 0;
		if (dayOperationFransaBanks.size() > 0) {

			List<BkmvtiFransaBank> BkmvtiFransaBanks = new ArrayList<BkmvtiFransaBank>();
			List<MvbkConf> allMvbkSettelemntsC = mvbkConfigR.findByCategorie(78);

			for (DayOperationFransaBank day : dayOperationFransaBanks) {
				if (ExecutorThreadFransaBank.stopThreads) {
					return;
				}
				int indexPieceComptable = getEveIndex1();
				List<DayOperationFransaBank> dayOperationFransaBanksByNumTransaction = operationRepo
						.getListOnUsMemeAgenceByNumTransaction(day.getNumRefTransaction(), day.getNumAutorisation(),
								day.getNumCartePorteur());
				for (MvbkConf mvk : allMvbkSettelemntsC) {
					int index2 = getEveIndex();
					BkmvtiFransaBanks = TestSigne(9, mvk, dayOperationFransaBanksByNumTransaction, BkmvtiFransaBanks,
							indexPieceComptable, index2, mvk.getCodeSettlement());

				}
			}

			if (BkmvtiFransaBanks != null && !BkmvtiFransaBanks.isEmpty()) {
				// Filter out null elements from the list
				List<BkmvtiFransaBank> nonNullBkmvtiFransaBanks = BkmvtiFransaBanks.stream().filter(Objects::nonNull)
						.collect(Collectors.toList());

				if (!nonNullBkmvtiFransaBanks.isEmpty()) {
					bkmvtiFransaBankRepository.saveAll(nonNullBkmvtiFransaBanks);
				}

				size = BkmvtiFransaBanks.size();
			}
		}
		logger.info("end mvbkOnUsMemeAgenceAchatInternet =>{}", size);

	}

	public void mvbkONUdAutreAgenceConsultationSoldeSwitch() {
		logger.info("begin mvbkONUdAutreAgenceConsultationSolde");

		List<DayOperationFransaBank> dayOperationFransaBanks = operationRepo.findByIdentification("S010");
		int size = 0;
		if (dayOperationFransaBanks.size() > 0) {

			List<BkmvtiFransaBank> BkmvtiFransaBanks = new ArrayList<BkmvtiFransaBank>();

			List<MvbkConf> allMvbkSettelemntsC = mvbkConfigR.findByCategorie(79);

			for (DayOperationFransaBank day : dayOperationFransaBanks) {
				if (ExecutorThreadFransaBank.stopThreads) {
					return;
				}
				int indexPieceComptable = getEveIndex1();
				List<DayOperationFransaBank> dayOperationFransaBanksByNumTransaction = operationRepo
						.getListOnUsMemeAgenceByNumTransaction(day.getNumRefTransaction(), day.getNumAutorisation(),
								day.getNumCartePorteur());
				for (MvbkConf mvk : allMvbkSettelemntsC) {

					int index2 = getEveIndex();
					BkmvtiFransaBanks = TestSigne(9, mvk, dayOperationFransaBanksByNumTransaction, BkmvtiFransaBanks,
							indexPieceComptable, index2, mvk.getCodeSettlement());

				}
			}

			if (BkmvtiFransaBanks != null && !BkmvtiFransaBanks.isEmpty()) {
				// Filter out null elements from the list
				List<BkmvtiFransaBank> nonNullBkmvtiFransaBanks = BkmvtiFransaBanks.stream().filter(Objects::nonNull)
						.collect(Collectors.toList());

				if (!nonNullBkmvtiFransaBanks.isEmpty()) {
					bkmvtiFransaBankRepository.saveAll(nonNullBkmvtiFransaBanks);
				}

				size = BkmvtiFransaBanks.size();
			}
		}
		logger.info("end mvbkOnUsMemeAgenceAchatInternet =>{}", size);

	}

	// *************Achat Tpe *****//
	public void mvbkOnUsMemeAgenceAchatWissal() {
		logger.info("begin mvbkOnUsMemeAgenceAchat =>{}");
		List<DayOperationFransaBank> dayOperationFransaBanks = operationRepo.findByIdentification("S012");
		int size = 0;
		if (dayOperationFransaBanks.size() > 0) {

			List<BkmvtiFransaBank> BkmvtiFransaBanks = new ArrayList<BkmvtiFransaBank>();
			List<MvbkConf> allMvbkSettelemntsC = mvbkConfigR.findByCategorie(81);
			for (DayOperationFransaBank day : dayOperationFransaBanks) {
				if (ExecutorThreadFransaBank.stopThreads) {
					return;
				}
				int indexPieceComptable = getEveIndex1();
				List<DayOperationFransaBank> dayOperationFransaBanksByNumTransaction = operationRepo
						.getListOnUsMemeAgenceByNumTransaction(day.getNumRefTransaction(), day.getNumAutorisation(),
								day.getNumCartePorteur());

				for (MvbkConf mvk : allMvbkSettelemntsC) {
					int index2 = getEveIndex();
					BkmvtiFransaBanks = TestSigne(6, mvk, dayOperationFransaBanksByNumTransaction, BkmvtiFransaBanks,
							indexPieceComptable, index2, mvk.getCodeSettlement());

				}
				updateDays(6, indexPieceComptable, dayOperationFransaBanksByNumTransaction);

			}

			if (BkmvtiFransaBanks != null && !BkmvtiFransaBanks.isEmpty()) {
				// Filter out null elements from the list
				List<BkmvtiFransaBank> nonNullBkmvtiFransaBanks = BkmvtiFransaBanks.stream().filter(Objects::nonNull)
						.collect(Collectors.toList());

				if (!nonNullBkmvtiFransaBanks.isEmpty()) {
					bkmvtiFransaBankRepository.saveAll(nonNullBkmvtiFransaBanks);
				}

				size = BkmvtiFransaBanks.size();
			}
		}
		logger.info("end mvbkOnUsMemeAgenceAchatInternet =>{}", size);

	}

	public void mvbkOnUsAutreAgenceAchatWissal() {
		logger.info(" begin mvbkOnUsAutreAgenceAchat");
		List<DayOperationFransaBank> dayOperationFransaBanks = operationRepo.findByIdentification("S015");
		int size = 0;
		if (dayOperationFransaBanks.size() > 0) {

			List<BkmvtiFransaBank> BkmvtiFransaBanks = new ArrayList<BkmvtiFransaBank>();

			List<MvbkConf> allMvbkSettelemntsC = mvbkConfigR.findByCategorie(80);

			for (DayOperationFransaBank day : dayOperationFransaBanks) {
				if (ExecutorThreadFransaBank.stopThreads) {
					return;
				}
				int indexPieceComptable = getEveIndex1();
				List<DayOperationFransaBank> dayOperationFransaBanksByNumTransaction = operationRepo
						.getListOnUsMemeAgenceByNumTransaction(day.getNumRefTransaction(), day.getNumAutorisation(),
								day.getNumCartePorteur());

				for (MvbkConf mvk : allMvbkSettelemntsC) {
					int index2 = getEveIndex();
					BkmvtiFransaBanks = TestSigne(6, mvk, dayOperationFransaBanksByNumTransaction, BkmvtiFransaBanks,
							indexPieceComptable, index2, mvk.getCodeSettlement());

				}
				updateDays(6, indexPieceComptable, dayOperationFransaBanksByNumTransaction);

			}
			if (BkmvtiFransaBanks != null && !BkmvtiFransaBanks.isEmpty()) {
				// Filter out null elements from the list
				List<BkmvtiFransaBank> nonNullBkmvtiFransaBanks = BkmvtiFransaBanks.stream().filter(Objects::nonNull)
						.collect(Collectors.toList());

				if (!nonNullBkmvtiFransaBanks.isEmpty()) {
					bkmvtiFransaBankRepository.saveAll(nonNullBkmvtiFransaBanks);
				}

				size = BkmvtiFransaBanks.size();
			}
		}
		logger.info("end mvbkOnUsMemeAgenceAchatInternet =>{}", size);

	}

	// *************Achat internet *****//
	public void mvbkOnUsMemeAgenceAchatInternetWissal() {
		logger.info("begin mvbkOnUsMemeAgenceAchatInternet");
		List<DayOperationFransaBank> dayOperationFransaBanks = operationRepo.findByIdentification("S019");

		int size = 0;
		if (dayOperationFransaBanks.size() > 0) {
			List<BkmvtiFransaBank> BkmvtiFransaBanks = new ArrayList<BkmvtiFransaBank>();
			List<MvbkConf> allMvbkSettelemntsC = mvbkConfigR.findByCategorie(84);
			for (DayOperationFransaBank day : dayOperationFransaBanks) {
				if (ExecutorThreadFransaBank.stopThreads) {
					return;
				}
				int indexPieceComptable = getEveIndex1();
				List<DayOperationFransaBank> dayOperationFransaBanksByNumTransaction = operationRepo
						.getListOnUsMemeAgenceByNumTransaction(day.getNumRefTransaction(), day.getNumAutorisation(),
								day.getNumCartePorteur());

				for (MvbkConf mvk : allMvbkSettelemntsC) {
					int index2 = getEveIndex();
					BkmvtiFransaBanks = TestSigne(17, mvk, dayOperationFransaBanksByNumTransaction, BkmvtiFransaBanks,
							indexPieceComptable, index2, mvk.getCodeSettlement());

				}
				updateDays(17, indexPieceComptable, dayOperationFransaBanksByNumTransaction);

			}
			if (BkmvtiFransaBanks != null && !BkmvtiFransaBanks.isEmpty()) {
				// Filter out null elements from the list
				List<BkmvtiFransaBank> nonNullBkmvtiFransaBanks = BkmvtiFransaBanks.stream().filter(Objects::nonNull)
						.collect(Collectors.toList());

				if (!nonNullBkmvtiFransaBanks.isEmpty()) {
					bkmvtiFransaBankRepository.saveAll(nonNullBkmvtiFransaBanks);
				}

				size = BkmvtiFransaBanks.size();
			}
		}
		logger.info("end mvbkOnUsMemeAgenceAchatInternet =>{}", size);

	}

	public void mvbkOnUsAutreAgenceAchatInternetWissal() {
		logger.info("begin mvbkOnUsAutreAgenceAchatInternet");
		List<DayOperationFransaBank> dayOperationFransaBanks = operationRepo.findByIdentification("S022");
		int size = 0;
		if (dayOperationFransaBanks.size() > 0) {

			List<BkmvtiFransaBank> BkmvtiFransaBanks = new ArrayList<BkmvtiFransaBank>();

			List<MvbkConf> allMvbkSettelemntsC = mvbkConfigR.findByCategorie(85);

			for (DayOperationFransaBank day : dayOperationFransaBanks) {
				if (ExecutorThreadFransaBank.stopThreads) {
					return;
				}
				int indexPieceComptable = getEveIndex1();
				List<DayOperationFransaBank> dayOperationFransaBanksByNumTransaction = operationRepo
						.getListOnUsMemeAgenceByNumTransaction(day.getNumRefTransaction(), day.getNumAutorisation(),
								day.getNumCartePorteur());

				for (MvbkConf mvk : allMvbkSettelemntsC) {
					int index2 = getEveIndex();
					BkmvtiFransaBanks = TestSigne(17, mvk, dayOperationFransaBanksByNumTransaction, BkmvtiFransaBanks,
							indexPieceComptable, index2, mvk.getCodeSettlement());

				}
				updateDays(17, indexPieceComptable, dayOperationFransaBanksByNumTransaction);

			}
			if (BkmvtiFransaBanks != null && !BkmvtiFransaBanks.isEmpty()) {
				// Filter out null elements from the list
				List<BkmvtiFransaBank> nonNullBkmvtiFransaBanks = BkmvtiFransaBanks.stream().filter(Objects::nonNull)
						.collect(Collectors.toList());

				if (!nonNullBkmvtiFransaBanks.isEmpty()) {
					bkmvtiFransaBankRepository.saveAll(nonNullBkmvtiFransaBanks);
				}

				size = BkmvtiFransaBanks.size();
			}
		}
		logger.info("end mvbkOnUsMemeAgenceAchatInternet =>{}", size);

	}

	// *************remboursement TPE*****//
	public void mvbkONUdMemeAgenceRembourssementTpeWissal() {
		logger.info("begin mvbkONUdMemeAgenceRembourssementTpe");
		List<DayOperationFransaBank> dayOperationFransaBanks = operationRepo.findByIdentification("S026");
		int size = 0;
		if (dayOperationFransaBanks.size() > 0) {

			List<BkmvtiFransaBank> BkmvtiFransaBanks = new ArrayList<BkmvtiFransaBank>();

			List<MvbkConf> allMvbkSettelemntsC = mvbkConfigR.findByCategorie(82);

			for (DayOperationFransaBank day : dayOperationFransaBanks) {
				if (ExecutorThreadFransaBank.stopThreads) {
					return;
				}
				int indexPieceComptable = getEveIndex1();
				List<DayOperationFransaBank> dayOperationFransaBanksByNumTransaction = operationRepo
						.getListOnUsMemeAgenceByNumTransaction(day.getNumRefTransaction(), day.getNumAutorisation(),
								day.getNumCartePorteur());

				for (MvbkConf mvk : allMvbkSettelemntsC) {
					int index2 = getEveIndex();
					BkmvtiFransaBanks = TestSigne(12, mvk, dayOperationFransaBanksByNumTransaction, BkmvtiFransaBanks,
							indexPieceComptable, index2, mvk.getCodeSettlement());

				}
				updateDays(12, indexPieceComptable, dayOperationFransaBanksByNumTransaction);

			}
			if (BkmvtiFransaBanks != null && !BkmvtiFransaBanks.isEmpty()) {
				// Filter out null elements from the list
				List<BkmvtiFransaBank> nonNullBkmvtiFransaBanks = BkmvtiFransaBanks.stream().filter(Objects::nonNull)
						.collect(Collectors.toList());

				if (!nonNullBkmvtiFransaBanks.isEmpty()) {
					bkmvtiFransaBankRepository.saveAll(nonNullBkmvtiFransaBanks);
				}

				size = BkmvtiFransaBanks.size();
			}
		}
		logger.info("end mvbkOnUsMemeAgenceAchatInternet =>{}", size);

	}

	public void mvbkONUdAutreAgenceRembourssementTpeWissal() {
		logger.info("begin mvbkONUdAutreAgenceRembourssementTpe");
		List<DayOperationFransaBank> dayOperationFransaBanks = operationRepo.findByIdentification("S029");
		int size = 0;
		if (dayOperationFransaBanks.size() > 0) {

			List<BkmvtiFransaBank> BkmvtiFransaBanks = new ArrayList<BkmvtiFransaBank>();

			List<MvbkConf> allMvbkSettelemntsC = mvbkConfigR.findByCategorie(83);

			for (DayOperationFransaBank day : dayOperationFransaBanks) {
				if (ExecutorThreadFransaBank.stopThreads) {
					return;
				}
				int indexPieceComptable = getEveIndex1();
				List<DayOperationFransaBank> dayOperationFransaBanksByNumTransaction = operationRepo
						.getListOnUsMemeAgenceByNumTransaction(day.getNumRefTransaction(), day.getNumAutorisation(),
								day.getNumCartePorteur());

				for (MvbkConf mvk : allMvbkSettelemntsC) {
					int index2 = getEveIndex();
					BkmvtiFransaBanks = TestSigne(12, mvk, dayOperationFransaBanksByNumTransaction, BkmvtiFransaBanks,
							indexPieceComptable, index2, mvk.getCodeSettlement());

				}
				updateDays(12, indexPieceComptable, dayOperationFransaBanksByNumTransaction);

			}
			if (BkmvtiFransaBanks != null && !BkmvtiFransaBanks.isEmpty()) {
				// Filter out null elements from the list
				List<BkmvtiFransaBank> nonNullBkmvtiFransaBanks = BkmvtiFransaBanks.stream().filter(Objects::nonNull)
						.collect(Collectors.toList());

				if (!nonNullBkmvtiFransaBanks.isEmpty()) {
					bkmvtiFransaBankRepository.saveAll(nonNullBkmvtiFransaBanks);
				}

				size = BkmvtiFransaBanks.size();
			}
		}
		logger.info("end mvbkOnUsMemeAgenceAchatInternet =>{}", size);

	}

	// *************remboursement Internet*****//
	public void mvbkONUdMemeAgenceRembourssementInternetWissal() {
		logger.info("mvbkONUdMemeAgenceRembourssementInternet");
		List<DayOperationFransaBank> dayOperationFransaBanks = operationRepo.findByIdentification("S033");
		int size = 0;
		if (dayOperationFransaBanks.size() > 0) {

			List<BkmvtiFransaBank> BkmvtiFransaBanks = new ArrayList<BkmvtiFransaBank>();

//S026 et S033 on le mème mvbk settelemnt
			List<MvbkConf> allMvbkSettelemntsC = mvbkConfigR.findByCategorie(86);

			for (DayOperationFransaBank day : dayOperationFransaBanks) {
				if (ExecutorThreadFransaBank.stopThreads) {
					return;
				}
				int indexPieceComptable = getEveIndex1();
				List<DayOperationFransaBank> dayOperationFransaBanksByNumTransaction = operationRepo
						.getListOnUsMemeAgenceByNumTransaction(day.getNumRefTransaction(), day.getNumAutorisation(),
								day.getNumCartePorteur());

				for (MvbkConf mvk : allMvbkSettelemntsC) {
					int index2 = getEveIndex();
					BkmvtiFransaBanks = TestSigne(22, mvk, dayOperationFransaBanksByNumTransaction, BkmvtiFransaBanks,
							indexPieceComptable, index2, mvk.getCodeSettlement());

				}
				updateDays(22, indexPieceComptable, dayOperationFransaBanksByNumTransaction);

			}
			if (BkmvtiFransaBanks != null && !BkmvtiFransaBanks.isEmpty()) {
				// Filter out null elements from the list
				List<BkmvtiFransaBank> nonNullBkmvtiFransaBanks = BkmvtiFransaBanks.stream().filter(Objects::nonNull)
						.collect(Collectors.toList());

				if (!nonNullBkmvtiFransaBanks.isEmpty()) {
					bkmvtiFransaBankRepository.saveAll(nonNullBkmvtiFransaBanks);
				}

				size = BkmvtiFransaBanks.size();
			}
		}
		logger.info("end mvbkOnUsMemeAgenceAchatInternet =>{}", size);

	}

	public void mvbkONUdAutreAgenceRembourssementInternetWissal() {
		logger.info("mvbkONUdAutreAgenceRembourssementInternet");
		List<DayOperationFransaBank> dayOperationFransaBanks = operationRepo.findByIdentification("S036");
		int size = 0;
		if (dayOperationFransaBanks.size() > 0) {

			List<BkmvtiFransaBank> BkmvtiFransaBanks = new ArrayList<BkmvtiFransaBank>();

			List<MvbkConf> allMvbkSettelemntsC = mvbkConfigR.findByCategorie(87);

			for (DayOperationFransaBank day : dayOperationFransaBanks) {
				if (ExecutorThreadFransaBank.stopThreads) {
					return;
				}
				int indexPieceComptable = getEveIndex1();
				List<DayOperationFransaBank> dayOperationFransaBanksByNumTransaction = operationRepo
						.getListOnUsMemeAgenceByNumTransaction(day.getNumRefTransaction(), day.getNumAutorisation(),
								day.getNumCartePorteur());

				for (MvbkConf mvk : allMvbkSettelemntsC) {
					int index2 = getEveIndex();
					BkmvtiFransaBanks = TestSigne(22, mvk, dayOperationFransaBanksByNumTransaction, BkmvtiFransaBanks,
							indexPieceComptable, index2, mvk.getCodeSettlement());

				}
				updateDays(22, indexPieceComptable, dayOperationFransaBanksByNumTransaction);

			}
			if (BkmvtiFransaBanks != null && !BkmvtiFransaBanks.isEmpty()) {
				// Filter out null elements from the list
				List<BkmvtiFransaBank> nonNullBkmvtiFransaBanks = BkmvtiFransaBanks.stream().filter(Objects::nonNull)
						.collect(Collectors.toList());

				if (!nonNullBkmvtiFransaBanks.isEmpty()) {
					bkmvtiFransaBankRepository.saveAll(nonNullBkmvtiFransaBanks);
				}

				size = BkmvtiFransaBanks.size();
			}
		}
		logger.info("end mvbkOnUsMemeAgenceAchatInternet =>{}", size);

	}

	@Override
	public void run() {
		System.out.println("==>Param" + paramDay);
		FileRequest.print(paramDay, FileRequest.getLineNumber());
		int i =Integer.valueOf(paramDay);
		switch (paramDay) {
		case "1":
			try {
				addRetrais(i);
				break;
			} catch (Exception e) {
				ExecutorThreadFransaBank.stopThreads = true;
				logger.error("some exception message", e);
				throw e;
			}
		case "2":
			try {
				addRetraisSC(i);
				break;
			} catch (Exception e) {
				ExecutorThreadFransaBank.stopThreads = true;
				logger.error("some exception message", e);
				throw e;
			}
		case "3":
			try {
				addConsultationSolde_onUs(i);
				break;
			} catch (Exception e) {
				ExecutorThreadFransaBank.stopThreads = true;
				logger.error("some exception message", e);
				throw e;
			}
		case "4":
			try {
				addAchatTPE(i);
				break;
			} catch (Exception e) {
				ExecutorThreadFransaBank.stopThreads = true;
				logger.error("some exception message", e);
				throw e;
			}
		case "5":
			try {
				addAchatInternet(i);
				break;
			} catch (Exception e) {
				ExecutorThreadFransaBank.stopThreads = true;
				logger.error("some exception message", e);
				throw e;
			}
		case "6":
			try {
				addRemboursementTpe(i);
				break;
			} catch (Exception e) {
				ExecutorThreadFransaBank.stopThreads = true;
				logger.error("some exception message", e);
				throw e;
			}
		case "7":
			try {
				addRemboursementInternet(i);
				break;
			} catch (Exception e) {
				ExecutorThreadFransaBank.stopThreads = true;
				logger.error("some exception message", e);
				throw e;
			}
		case "8":
			try {
				addChargeBack(i);
				break;
			} catch (Exception e) {
				ExecutorThreadFransaBank.stopThreads = true;
				logger.error("some exception message", e);
				throw e;
			}
		case "9":
			try {
				changeCardPIN(i);
				break;
			} catch (Exception e) {
				ExecutorThreadFransaBank.stopThreads = true;
				logger.error("some exception message", e);
				throw e;
			}
		case "10":
			try {
				addRetraisMVBK(i);
				break;
			} catch (Exception e) {
				ExecutorThreadFransaBank.stopThreads = true;
				logger.error("some exception message", e);
				throw e;
			}
		case "11":
			try {
				addConsultationSolde_onUsMVBK(i);
				break;
			} catch (Exception e) {
				ExecutorThreadFransaBank.stopThreads = true;
				logger.error("some exception message", e);
				throw e;
			}
		case "12":
			try {
				addAchatTPEMVBK(i);
				break;
			} catch (Exception e) {
				ExecutorThreadFransaBank.stopThreads = true;
				logger.error("some exception message", e);
				throw e;
			}
		case "13":
			try {
				addAchatInternetMVBK(i);
				break;
			} catch (Exception e) {
				ExecutorThreadFransaBank.stopThreads = true;
				logger.error("some exception message", e);
				throw e;
			}
		case "14":
			try {
				addRemboursementTpeMVBK(i);
				break;
			} catch (Exception e) {
				ExecutorThreadFransaBank.stopThreads = true;
				logger.error("some exception message", e);
				throw e;
			}
		case "15":
			try {
				addRemboursementInternetMVBK(i);
				break;
			} catch (Exception e) {
				ExecutorThreadFransaBank.stopThreads = true;
				logger.error("some exception message", e);
				throw e;
			}
		case "16":
			try {
				addCHMVBK(i);
				break;
			} catch (Exception e) {
				ExecutorThreadFransaBank.stopThreads = true;
				logger.error("some exception message", e);
				throw e;
			}
		case "17":
			try {
				addRetraitCSMVBK(i);
				break;
			} catch (Exception e) {
				ExecutorThreadFransaBank.stopThreads = true;
				logger.error("some exception message", e);
				throw e;
			}
		case "18":
			try {
				addPINChangeMVBK(i);
				break;
			} catch (Exception e) {
				ExecutorThreadFransaBank.stopThreads = true;
				logger.error("some exception message", e);
				throw e;
			}

		}
	}

	private void addRemboursementInternetMVBK(int i) {
		mvbkONUdMemeAgenceRembourssementInternetWissal();
		mvbkONUdAutreAgenceRembourssementInternetWissal();
	}

	private void addRemboursementTpeMVBK(int i) {
		mvbkONUdMemeAgenceRembourssementTpeWissal();
		mvbkONUdAutreAgenceRembourssementTpeWissal();
	}

	private void addAchatInternetMVBK(int i) {
		mvbkOnUsMemeAgenceAchatInternetWissal();
		mvbkOnUsAutreAgenceAchatInternetWissal();
	}

	private void addAchatTPEMVBK(int i) {
		mvbkOnUsMemeAgenceAchatWissal();
		mvbkOnUsAutreAgenceAchatWissal();
	}

	private void addConsultationSolde_onUsMVBK(int i) {
		mvbkONUdMemeAgenceConsultationSoldeSwitch();
		mvbkONUdAutreAgenceConsultationSoldeSwitch();

	}

	private void addRetraisMVBK(int i) {
		mvbkOnUsMemeAgenceSwitch();
		mvbkOnUsAutreAgenceSwitch();
	}
	private void addRetraitCSMVBK(int i) {
		mvbkOnUsMemeAgenceSwitchCS();
		mvbkOnUsAutreAgenceSwitchCS();
	}
	private void addCHMVBK(int i) {
		mvbkOnUsMemeAgenceSwitchCH();
		mvbkOnUsAutreAgenceSwitchCH();
	}

	private void addPINChangeMVBK(int i) {
		mvbkPINChangeMVBK();
 	}

	public void saveInBatches(List<DayOperationFransaBank> dayOperations, int i, List<SwitchTransactionT> elements) {
		switchTransactionTRepository.saveAll(elements);

//			for (DayOperationFransaBank op : dayOperations) {
		//
//				op.setFileDate("" + i + "");
//			}

		if (allowEdit) {
//			tp_detail.saveAll(contents);

			operationRepo.saveAll(dayOperations);
		}

	}

	private void catchError(Exception e, int threadNB) {
		e.printStackTrace();
		logger.info("Exception is=>{}", e.getMessage());
		logger.info("Exception is=>{}", e.fillInStackTrace());

		String stackTrace = Throwables.getStackTraceAsString(e);

		if (stackTrace.length() > 4000) {
			stackTrace = stackTrace.substring(0, 3999);

		}

//		batchRepo.updateFinishBatch("TH" + threadNB, 2, new Date());
		stopSavingData();
		batchRepo.updateStatusAndErrorBatch("ExecuteSwitch", 2, e.getMessage() == null ? e.toString() : e.getMessage(),
				new Date(), stackTrace);
	}

	private void stopSavingData() {
		ExecutorThreadFransaBank.stopThreads = true;

	}

	public int getIndex(int indextoadd) {

		indextoadd += 1;

		return indextoadd;
	}

	public void updateDays(int methode, int indexPieceComptable, List<DayOperationFransaBank> days) {
		String lib = getPieceComptable(methode, indexPieceComptable);
		days.forEach(element -> {
			element.setPieceComptableBkm(lib);
		});
		operationRepo.saveAll(days);
	}

	public String getPieceComptable(int methode, int indexPieceComptable) {
		String lib = "";
		switch (methode) {

		case 1:
			lib = DB + "161" + String.format("%06d", indexPieceComptable);
			break;
		case 2:
			lib = DB + "162" + String.format("%06d", indexPieceComptable);

			break;
		case 3:
			lib = DB + "170" + String.format("%06d", indexPieceComptable);

			break;

		case 4:
			lib = DB + "170" + String.format("%06d", indexPieceComptable);
			break;

		case 5:
			lib = DB + "162" + String.format("%06d", indexPieceComptable);

			break;
		case 6:
			lib = PA + "163" + String.format("%06d", indexPieceComptable);

			break;

		case 7:
			lib = PA + "164" + String.format("%06d", indexPieceComptable);

			break;
		case 8:
			lib = PA + "171" + String.format("%06d", indexPieceComptable);

			break;
		case 9:
			lib = DB + "187" + String.format("%06d", indexPieceComptable);

			break;
		case 10:
			lib = DB + "106" + String.format("%06d", indexPieceComptable);

			break;
		case 11:
			lib = DB + "107" + String.format("%06d", indexPieceComptable);

			break;
		case 12:
			lib = RB + "***" + String.format("%06d", indexPieceComptable);

			break;
		case 13:
			lib = RB + "169" + String.format("%06d", indexPieceComptable);

			break;
		case 14:
			lib = RB + "173" + String.format("%06d", indexPieceComptable);

			break;

		case 15:
			lib = DB + "170" + String.format("%06d", indexPieceComptable);

			break;

		case 16:
			lib = DB + "162" + String.format("%06d", indexPieceComptable);

			break;
		case 17:
			lib = PA + "163" + String.format("%06d", indexPieceComptable);

			break;
		case 18:
			lib = PA + "164" + String.format("%06d", indexPieceComptable);

			break;
		case 19:
			lib = PA + "171" + String.format("%06d", indexPieceComptable);

			break;
		case 20:
			lib = PA + "164" + String.format("%06d", indexPieceComptable);

			break;

		case 21:
			lib = PA + "171" + String.format("%06d", indexPieceComptable);

			break;

		case 22:
			lib = RB + "***" + String.format("%06d", indexPieceComptable);

			break;
		case 23:
			lib = RB + "972" + String.format("%06d", indexPieceComptable);

			break;
		case 24:
			lib = RB + "***" + String.format("%06d", indexPieceComptable);

			break;
		default:
			System.out.println("nothing");
		}
		return lib;

	}

	public BkmvtiFransaBank setSameData(int methode, BkmvtiFransaBank bkmvtiFransaBank, int index2,
			int indexPieceComptable, DayOperationFransaBank op, MvbkConf mvk) {
		String lib = "";
		bkmvtiFransaBank.setNumEvenement(String.format("%06d", indexPieceComptable));
		bkmvtiFransaBank.setCodeOperation(mvk.getCodeOperation());
		bkmvtiFransaBank.setLibelle(mvk.getLibelle_operation() + getSpace(40 - lib.length()));
		if (mvk.getLibGenerique() != null && mvk.getLibGenerique().trim() != "") {
			String libgenerique = mvk.getLibGenerique();
			libgenerique = libgenerique.replaceAll("dateTransation", op.getDateTransaction());
			String aut = op.getNumAutorisation();
			int lengthAuth = aut.length();
			libgenerique = libgenerique.replaceAll("numAutorisation", aut.substring(lengthAuth - 6));
			bkmvtiFransaBank.setLibelle(libgenerique + getSpace(40 - lib.length()));
		}
		switch (methode) {

		case 1:

			bkmvtiFransaBank
					.setPieceComptable(DB + mvk.getCodeOperation() + String.format("%06d", indexPieceComptable));

			break;
		case 6:

			bkmvtiFransaBank
					.setPieceComptable(PA + mvk.getCodeOperation() + String.format("%06d", indexPieceComptable));
			break;

		case 9:

			bkmvtiFransaBank
					.setPieceComptable(DB + mvk.getCodeOperation() + String.format("%06d", indexPieceComptable));
			break;
		case 12:

			bkmvtiFransaBank
					.setPieceComptable(RB + mvk.getCodeOperation() + String.format("%06d", indexPieceComptable));
			break;

		case 17:

			bkmvtiFransaBank
					.setPieceComptable(PA + mvk.getCodeOperation() + String.format("%06d", indexPieceComptable));
			break;
		case 22:

			bkmvtiFransaBank
					.setPieceComptable(RB + mvk.getCodeOperation() + String.format("%06d", indexPieceComptable));
			break;
		default:

		}
		return bkmvtiFransaBank;
	}

	public List<BkmvtiFransaBank> TestSigne(int methode, MvbkConf mvk,
			List<DayOperationFransaBank> dayOperationFransaBanksByNumTransaction,
			List<BkmvtiFransaBank> BkmvtiFransaBanks, int indexPieceComptable, int index2, String codeOperation) {
		Map<String, Float> data = new HashMap<>();
		Map<String, String> ComptC = new HashMap<>();
		Map<String, String> ComptD = new HashMap<>();

		for (DayOperationFransaBank op : dayOperationFransaBanksByNumTransaction) {
			data.put(op.getIdenfication(), op.getMontantSettlement());
			SettelementFransaBank sett = settlementRepo.findByIdentificationbh(op.getIdenfication());
			ComptC.put(sett.getCreditAccount(), op.getCompteCredit());
			ComptD.put(sett.getDebitAccount(), op.getCompteDebit());

		}
		FileRequest.print(codeOperation, FileRequest.getLineNumber());
		FileRequest.print(fIPService.evaluateWithElements(codeOperation, data) + "", FileRequest.getLineNumber());

		if (fIPService.evaluateWithElements(codeOperation, data) <= 0) {
			FileRequest.print("cancelled", FileRequest.getLineNumber());
			return BkmvtiFransaBanks;
		}
		switch (mvk.getType()) {
		case "1":
//    	FileRequest.print("enterToMethode", FileRequest.getLineNumber());
			for (DayOperationFransaBank op : dayOperationFransaBanksByNumTransaction) {

				if (op.getIdenfication().equals(codeOperation)) {
					BkmvtiFransaBank bkmvtiFransaBank = new BkmvtiFransaBank();

					// ****Account porteur and signe C OR D****////
					if (AccountSigne(mvk.getAccount(), mvk.getSigne()) == 1) {
//                 	FileRequest.print("enterFirst", FileRequest.getLineNumber());
						bkmvtiFransaBank = TestAccountAndSigne(methode, index2, indexPieceComptable, 1,
								op.getCompteCredit(), op.getCompteDebit().length(), mvk.getSigne(), mvk, op,
								op.getCompteDebit());
						BkmvtiFransaBanks.add(bkmvtiFransaBank);

					} else if (AccountSigne(mvk.getAccount(), mvk.getSigne()) == 2) {
//                	FileRequest.print("enterSecond", FileRequest.getLineNumber());
						bkmvtiFransaBank = TestAccountAndSigne(methode, index2, indexPieceComptable, 1,
								op.getCompteDebit(), op.getCompteDebit().length(), mvk.getSigne(), mvk, op,
								op.getCompteDebit());
						BkmvtiFransaBanks.add(bkmvtiFransaBank);

					}

					// ****Account not porteur and signe C OR D****///
					else if (AccountSigne(mvk.getAccount(), mvk.getSigne()) == 3) {
//                	FileRequest.print("enterThreart", FileRequest.getLineNumber());
						bkmvtiFransaBank = TestAccountAndSigne(methode, index2, indexPieceComptable, 1,
								mvk.getAccount(), mvk.getAccount().length(), mvk.getSigne(), mvk, op, mvk.getAccount());

						BkmvtiFransaBanks.add(bkmvtiFransaBank);
					}

				}
			}
			break;
		case "2":

			String code = codeOperation;
			String code1 = code.substring(0, 4);
			String code2 = code.substring(5, 9);
			String operation = code.substring(4, 5);
			float montant = 0;
			// *** same data ***//
			BkmvtiFransaBank bkmvtiFransaBank = new BkmvtiFransaBank();

			for (DayOperationFransaBank op : dayOperationFransaBanksByNumTransaction) {
				if (op.getIdenfication().equals(code1) || op.getIdenfication().equals(code2)) {

					if (AccountSigne(mvk.getAccount(), mvk.getSigne()) == 1) {
						bkmvtiFransaBank = TestAccountAndSigne(methode, index2, indexPieceComptable, 1,
								op.getCompteCredit(), op.getCompteDebit().length(), mvk.getSigne(), mvk, op,
								op.getCompteDebit());

						bkmvtiFransaBank
								.setMontant(getAmountFormat(fIPService.evaluateWithElements(codeOperation, data)));
					}

					else if (AccountSigne(mvk.getAccount(), mvk.getSigne()) == 2) {
						bkmvtiFransaBank = TestAccountAndSigne(methode, index2, indexPieceComptable, 1,
								op.getCompteDebit(), op.getCompteDebit().length(), mvk.getSigne(), mvk, op,
								op.getCompteDebit());
						FileRequest.print(codeOperation, FileRequest.getLineNumber());
						FileRequest.print(fIPService.evaluateWithElements(codeOperation, data) + "",
								FileRequest.getLineNumber());
						FileRequest.print(getAmountFormat(fIPService.evaluateWithElements(codeOperation, data)),
								FileRequest.getLineNumber());
						bkmvtiFransaBank
								.setMontant(getAmountFormat(fIPService.evaluateWithElements(codeOperation, data)));
					}

					else if (AccountSigne(mvk.getAccount(), mvk.getSigne()) == 3) {
						bkmvtiFransaBank = TestAccountAndSigne(methode, index2, indexPieceComptable, 1,
								mvk.getAccount(), op.getCompteDebit().length(), mvk.getSigne(), mvk, op,
								mvk.getAccount());

						bkmvtiFransaBank
								.setMontant(getAmountFormat(fIPService.evaluateWithElements(codeOperation, data)));
					}
				}

			}
			BkmvtiFransaBanks.add(bkmvtiFransaBank);

			break;
		case "3":

			String codeSettlement = codeOperation;
			String codeSettlement1 = codeSettlement.substring(0, 4);
			String codeSettlement2 = codeSettlement.substring(5, 9);
			String codeSettlement3 = codeSettlement.substring(10, 14);
			String operationSettlement = codeSettlement.substring(4, 5);
			String operation2 = codeSettlement.substring(9, 10);
//			float montantSettlement = 0;
			BkmvtiFransaBank bkmvtiFransaBank2 = new BkmvtiFransaBank();

			for (DayOperationFransaBank op : dayOperationFransaBanksByNumTransaction) {
				if (op.getIdenfication().equals(codeSettlement1) || op.getIdenfication().equals(codeSettlement2)
						|| op.getIdenfication().equals(codeSettlement3)) {
//					if (operationSettlement.equals("+") && operation2.equals("+")) {
//						montantSettlement = (montantSettlement + (op.getMontantSettlement()));
//					} else if (operationSettlement.equals("-") && operation2.equals("-")) {
//						montantSettlement = (float) ((Math.abs((op.getMontantSettlement()) - montantSettlement) * 100.0)
//								/ 100.0);
//					}

					if (AccountSigne(mvk.getAccount(), mvk.getSigne()) == 1) {
						bkmvtiFransaBank2 = TestAccountAndSigne(methode, index2, indexPieceComptable, 2,
								op.getCompteCredit(), op.getCompteDebit().length(), mvk.getSigne(), mvk, op,
								op.getCompteDebit());
						bkmvtiFransaBank2
								.setMontant(getAmountFormat(fIPService.evaluateWithElements(codeOperation, data)));

					}

					else if (AccountSigne(mvk.getAccount(), mvk.getSigne()) == 2) {
						bkmvtiFransaBank2 = TestAccountAndSigne(methode, index2, indexPieceComptable, 2,
								op.getCompteDebit(), op.getCompteDebit().length(), mvk.getSigne(), mvk, op,
								op.getCompteDebit());

						bkmvtiFransaBank2
								.setMontant(getAmountFormat(fIPService.evaluateWithElements(codeOperation, data)));
					}

					else if (AccountSigne(mvk.getAccount(), mvk.getSigne()) == 3) {
						bkmvtiFransaBank2 = TestAccountAndSigne(methode, index2, indexPieceComptable, 2,
								mvk.getAccount(), op.getCompteDebit().length(), mvk.getSigne(), mvk, op,
								mvk.getAccount());

						bkmvtiFransaBank2
								.setMontant(getAmountFormat(fIPService.evaluateWithElements(codeOperation, data)));

					}
				}

			}
			BkmvtiFransaBanks.add(bkmvtiFransaBank2);

			break;
		default:

		}
		return BkmvtiFransaBanks;
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

	public BkmvtiFransaBank TestAccountAndSigne(int methode, int index2, int indexPieceComptable, int test,
			String account, int accountDebit, String signe, MvbkConf mvk, DayOperationFransaBank op, String cle) {
		BkmvtiFransaBank bkmvtiFransaBank = new BkmvtiFransaBank();
		TestAccountLength(account.length(), accountDebit, account, mvk, bkmvtiFransaBank, op, cle);
		bkmvtiFransaBank.setCodeDevice("208");
		if (name.length() > 10) {
			name = name.substring(0, 10);
		}

		bkmvtiFransaBank.setCodeUtilisateur(name + getSpace(10 - name.length()));
		bkmvtiFransaBank.setCodeService("0000");
		bkmvtiFransaBank.setSens(mvk.getSigne());
		bkmvtiFransaBank.setExonerationcommission("O");
		int lengthNumPiece = op.getNumAutorisation().length();
		bkmvtiFransaBank.setNumPiece(op.getNumAutorisation().substring(lengthNumPiece - 11, lengthNumPiece));
		bkmvtiFransaBank.setTauxChange("1" + getSpace(6));
		bkmvtiFransaBank.setCalculmouvementInteragence("N");
		bkmvtiFransaBank.setMouvementAgence("N");
		//// add new fileds //////
		int lengthNumAuth = op.getNumAutorisation().length();
		int lengthRefDossier = (op.getNumRefTransaction() + op.getDateTransaction()
				+ op.getNumAutorisation().substring(lengthNumAuth - 6, lengthNumAuth)).length();
		bkmvtiFransaBank.setRefDossier(op.getNumRefTransaction() + op.getDateTransaction()
				+ op.getNumAutorisation().substring(lengthNumAuth - 6, lengthNumAuth)
				+ getSpace(50 - lengthRefDossier));

		TestCodeAgence(methode, test, mvk.getCodeAgence(), mvk, op, bkmvtiFransaBank);

		getTransactionDate(op.getDateTransaction(), bkmvtiFransaBank);

		int lengthReferanceLettrage = op.getNumAutorisation().length();

		bkmvtiFransaBank.setReferanceLettrage(op.getDateTransaction().substring(6, op.getDateTransaction().length())
				+ op.getNumAutorisation().substring(lengthReferanceLettrage - 6, lengthReferanceLettrage));
		if (mvk.getType().equals("1")) {
			bkmvtiFransaBank.setMontant(getAmountFormat(op.getMontantSettlement()));
		}

		bkmvtiFransaBank.setCodeDeviceOrigine("208");
		bkmvtiFransaBank.setIdentification(mvk.getIdentification());
		bkmvtiFransaBank.setNumRefTransactions(op.getNumRefTransaction());

		setSameData(methode, bkmvtiFransaBank, index2, indexPieceComptable, op, mvk);
			if ("AGENCE_C".equals(mvk.getEntity())) {
				bkmvtiFransaBank.setNumCompte(
						bkmvtiFransaBank.getNumCompte().substring(0, bkmvtiFransaBank.getNumCompte().length() - 4)
								+ op.getCodeAgence().substring(1, op.getCodeAgence().length()));
			} else if ("AGENCE_P".equals(mvk.getEntity())) {
				bkmvtiFransaBank.setNumCompte(
						bkmvtiFransaBank.getNumCompte().substring(0, bkmvtiFransaBank.getNumCompte().length() - 4)
								+ op.getNumRIBEmetteur().substring(4, 8).length());
			}

//		try {
//
//			if (Double.valueOf(bkmvtiFransaBank.getMontant().replace(",", ".")) <= 0) {
//				throw new Exception("amount lower than expected");
//			}
//		} catch (Exception e) {
//			return null;
//		}
		return bkmvtiFransaBank;

	}

	public BkmvtiFransaBank TestAccountLength(int lengAccount, int lengAccountDebit, String Account, MvbkConf mvk,
			BkmvtiFransaBank bkmvtiFransaBank, DayOperationFransaBank op, String cle) {

		if (lengAccount > 18) {

			String credit = Account.substring(8, 18);
			String chapitreCompta = Account.substring(8, 14);
			bkmvtiFransaBank.setChapitreComptable(chapitreCompta + getZero(6 - chapitreCompta.length()));
			bkmvtiFransaBank.setNumCompte(credit);
			String index = cle.substring(cle.length() - 2, cle.length());
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
			bkmvtiFransaBank.setAgenceDestinatrice(op.getCodeAgence());
			bkmvtiFransaBank.setAgenceEmettrice(op.getCodeAgence());
			bkmvtiFransaBank.setCodeAgenceSaisie(op.getCodeAgence());
			bkmvtiFransaBank.setCodeID("S" + op.getCodeAgence());
		}

		else if (lengAccount < 6) {
			bkmvtiFransaBank.setNumCompte(Account + getZero(10 - (Account.length())));
			bkmvtiFransaBank.setChapitreComptable(Account + getZero(6 - Account.length()));
			bkmvtiFransaBank.setCleControleCompte(getSpace(2));
			bkmvtiFransaBank.setAgenceDestinatrice(op.getCodeAgence());
			bkmvtiFransaBank.setAgenceEmettrice(op.getCodeAgence());
			bkmvtiFransaBank.setCodeAgenceSaisie(op.getCodeAgence());
			bkmvtiFransaBank.setCodeID("S" + op.getCodeAgence());
		}

		else if (lengAccount == 10) {
			bkmvtiFransaBank.setNumCompte(Account);
			bkmvtiFransaBank.setChapitreComptable(Account.substring(0, 6));
			String index = cle.substring(cle.length() - 2, cle.length());
			bkmvtiFransaBank.setCleControleCompte(getSpace(2));
			bkmvtiFransaBank.setAgenceDestinatrice(op.getCodeAgence());
			bkmvtiFransaBank.setAgenceEmettrice(op.getCodeAgence());
			bkmvtiFransaBank.setCodeAgenceSaisie(op.getCodeAgence());
			bkmvtiFransaBank.setCodeID("S" + op.getCodeAgence());
		}

		else {
			bkmvtiFransaBank.setNumCompte(Account.substring(0, 10));
			bkmvtiFransaBank.setChapitreComptable(Account.substring(0, 6));
			bkmvtiFransaBank.setCleControleCompte(getSpace(2));
			bkmvtiFransaBank.setAgenceEmettrice(op.getCodeAgence());
			bkmvtiFransaBank.setCodeAgenceSaisie(op.getCodeAgence());
			bkmvtiFransaBank.setCodeID("S" + op.getCodeAgence());
		}

		return bkmvtiFransaBank;
	}

	public int testMethode(int method) {
		int acq = 0;
		if (method != 3 && method != 4 && method != 8 && method != 11 && method != 14) {
			acq = 1;
		} else if (method != 3 || method != 4 || method != 8 || method != 11 || method != 14) {
			acq = 2;
		}
		return acq;
	}

	public String testCleController(String account, String index) {
		String cle = getSpace(2);
		if (!account.equals("PORTEUR") && !account.equals("COMMERCANT") && !account.equals("ATM")) {
			cle = index;
		}
		return cle;
	}

	public BkmvtiFransaBank TestCodeAgence(int methode, int test, String codeAgence, MvbkConf mvk,
			DayOperationFransaBank op, BkmvtiFransaBank bkmvtiFransaBank) {
		if (testMethode(methode) == 1) {

			if (test != 3 && test != 4) {
				if (codeAgence == null) {

					if (test == 1) {

						bkmvtiFransaBank.setAgence(TestCompteCredit(op.getCompteCredit(), op));
					} else {
						bkmvtiFransaBank.setAgence(op.getCodeAgence());

					}
				} else if (mvk.getCodeAgence().equals("00002")) {
					bkmvtiFransaBank.setAgence(mvk.getCodeAgence());
				}

				else if (mvk.getCodeAgence().equals("MERCH")) {
					bkmvtiFransaBank.setAgence(op.getCodeAgence());
				} else if (mvk.getCodeAgence().equals("CARD") || mvk.getCodeAgence().equals("P")) {
					bkmvtiFransaBank.setAgence(op.getNumRIBEmetteur().substring(3, 8));
				}

				else if (mvk.getCodeAgence().equals("C")) {
					bkmvtiFransaBank.setAgence(op.getCodeAgence());

				}

			}

			else if (test == 3) {

				String code = op.getNumRIBEmetteur().substring(3, 8);
				bkmvtiFransaBank.setAgence(code);

			} else if (test == 4) {
				String code = op.getIdCommercant().substring(3, 8);
				bkmvtiFransaBank.setAgence(code);
			}

		} else if (testMethode(methode) == 2) {
			if (mvk.getCodeAgence() != null) {
				if (mvk.getCodeAgence().equals("00002")) {
					bkmvtiFransaBank.setAgence(mvk.getCodeAgence());
				}
			} else {
				bkmvtiFransaBank.setAgence(op.getCodeAgence());
			}

		}

		return bkmvtiFransaBank;
	}

	public String TestCompteCredit(String compteCredit, DayOperationFransaBank op) {
		String codeAgence = "";
		if (op.getCompteCredit().length() < 18) {
			codeAgence = op.getCodeAgence();

		} else {

			codeAgence = op.getCompteCredit().substring(3, 8);

		}
		return codeAgence;

	}

	public BkmvtiFransaBank getTransactionDate(String TransactionDate, BkmvtiFransaBank bkmvtiFransaBank) {
		String year = TransactionDate.substring(0, 4);
		String month = TransactionDate.substring(4, 6);
		String dayy = TransactionDate.substring(6);
		// bkmvtiFransaBank.setDateComptable(dayy + "/" + month + "/" + year);

		LocalDate date = LocalDate.now();
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
		bkmvtiFransaBank.setDateComptable(date.format(formatter));

		bkmvtiFransaBank.setDateValeur(dayy + "/" + month + "/" + year);
		return bkmvtiFransaBank;

	}

	public BkmvtiFransaBank getAmount(float amountSettlement, BkmvtiFransaBank bkmvtiFransaBank) {
		int amountRound = Math.round(amountSettlement);
		String amountString = String.valueOf(amountRound);
		int AmountLengh1 = amountString.length();
		String amount = "00,00";
		try {
			amount = amountString.substring(0, AmountLengh1 - 2) + "," + amountString.substring(AmountLengh1 - 2);
		} catch (Exception ex) {
			FileRequest.print("data empty identification  :" + bkmvtiFransaBank.getIdentification(),
					FileRequest.getLineNumber());
		}

		bkmvtiFransaBank.setMontant(getZero(20 - AmountLengh1) + amount);
		return bkmvtiFransaBank;
	}

	public int AmountType2(String codeSettlement, String identification, String MontantSettlement) {

		String code1 = codeSettlement.substring(0, 4);
		String code2 = codeSettlement.substring(5, 9);
		String operation = codeSettlement.substring(4, 5);
		int montant = 0;

		if (identification.equals(code1) || identification.equals(code2)) {
			if (operation.equals("+")) {
				montant = montant + Integer.parseInt(MontantSettlement);
			} else if (operation.equals("-")) {
				montant = Math.abs(Integer.parseInt(MontantSettlement) - montant);
			}

		}

		return montant;

	}

	public String getAmountFormat(float amount) {
		float m = Math.round(amount);
		int a = (int) m;

		String amountFormat = String.format("%018d,%02d", a / 100, a % 100);

		return amountFormat;
	}

}