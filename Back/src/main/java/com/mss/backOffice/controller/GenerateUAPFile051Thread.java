package com.mss.backOffice.controller;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.mss.backOffice.services.FormulaInterpreterService;
import com.mss.unified.entities.Account;
import com.mss.unified.entities.AtmTerminal;
import com.mss.unified.entities.BkmvtiFransaBank;
import com.mss.unified.entities.DayOperationFransaBank;
import com.mss.unified.entities.Merchant;
import com.mss.unified.entities.MvbkConf;
import com.mss.unified.entities.MvbkSettlement;
import com.mss.unified.entities.UAP050FransaBank;
import com.mss.unified.entities.UAP051FransaBank;
import com.mss.unified.repositories.BkmvtiFransaBankRepository;
import com.mss.unified.repositories.CodeBankBCRepository;
import com.mss.unified.repositories.DayOperationFransaBankRepository;
import com.mss.unified.repositories.MerchantRepository;
import com.mss.unified.repositories.MvbkConfigRepository;
import com.mss.unified.repositories.MvbkSettlementRepository;
import com.mss.unified.repositories.UAP050FransaBankRepository;
import com.mss.unified.repositories.UAP051FransaBankRepository;
import com.mss.unified.repositories.UAPFransaBankRepository;

public strictfp  class GenerateUAPFile051Thread implements Runnable {

	public String fileIntegration;
	private static final Logger logger = LoggerFactory.getLogger(GenerateUAPFile050Thread.class);
	public String name;
	@Autowired
	DayOperationFransaBankRepository dayOperationFransaBankRepository;
	@Autowired
	CodeBankBCRepository codeBankBCRepository;
	@Autowired
	UAPFransaBankRepository uAPFransaBankRepository;
	@Autowired
	FormulaInterpreterService fIPService;
	@Autowired
	UAP051FransaBankRepository uAP050FransaBankRepository;
	@Autowired
	MerchantRepository mr;
	@Autowired
	MvbkSettlementRepository mvbkSettlementRepository;
	@Autowired
	BkmvtiFransaBankRepository bkmvtiFransaBankRepository;
	@Autowired
	public MvbkConfigRepository mvbkConfigR;
	public static Integer eve1 = 0;
	public static int eve = 0;

	public synchronized int getEveIndex() {
		return (++eve);
	}

	public int getEveIndex1() {
		synchronized (eve1) {
			eve1 = eve1 + 1;
		}

		return eve1;
	}

	public String paramUAP51;

	public GenerateUAPFile051Thread(DayOperationFransaBankRepository dayOperationFransaBankRepository,
			String paramUAP51, UAP051FransaBankRepository uAP050FransaBankRepository,
			MvbkSettlementRepository mvbkSettlementRepository, BkmvtiFransaBankRepository bkmvtiFransaBankRepository,
			String name, String fileIntegration, MvbkConfigRepository mvbkConfigR) {
		super();
		this.dayOperationFransaBankRepository = dayOperationFransaBankRepository;
		this.paramUAP51 = paramUAP51;
		this.uAP050FransaBankRepository = uAP050FransaBankRepository;
		this.mvbkSettlementRepository = mvbkSettlementRepository;
		this.bkmvtiFransaBankRepository = bkmvtiFransaBankRepository;
		this.name = name;
		this.fileIntegration = fileIntegration;
		this.mvbkConfigR = mvbkConfigR;

	}

	public void writeUAP051() {

		writeUAP051RembourssementTpe();
		writeUAP051RembourssementInternet();
//		generationUAP051RembourssementTpe();
//		generationUAP051RembourssementInternet();

	}

	public String getTypePaiement(String typeRetrait) {
		String typePaeiment = "";
		switch (typeRetrait) {

		case "051":
			typePaeiment = "01";
			break;
		case "056":
			typePaeiment = "03";

			break;
		case "055":
			typePaeiment = "04";

			break;

		case "054":
			typePaeiment = "05";
			break;

		default:
			System.out.println("nothing");
		}
		return typePaeiment;
	}

	public void writeUAP051RembourssementTpe() {
		logger.info("writeUAP050RembourssementTpe");
		List<Object[]> dayOperationsRembourssement = dayOperationFransaBankRepository.getListRembourssementTpeUAP();
		List<UAP051FransaBank> uAPFransaBanksRembourssementTPE = new ArrayList<UAP051FransaBank>();

		// List<Object[]> dayOperationAmountRembourssement =
		// dayOperationFransaBankRepository.getListAmountRembourssement();
		List<Merchant> merchants = dayOperationFransaBankRepository.getListMerchantRembourssementTpe();
		for (Object o[] : dayOperationsRembourssement) {
			UAP051FransaBank uAP050FransaBank = new UAP051FransaBank();
			uAP050FransaBank.setCodeAgence(String.valueOf(o[3]) + getSpace(5 - String.valueOf(o[3]).length()));
			uAP050FransaBank.setNumTransaction(String.valueOf(o[16]) + getSpace(12 - String.valueOf(o[16]).length()));

			uAP050FransaBank.setTypeTransaction("051");
			uAP050FransaBank.setTypePaiement("01");
			String RefArchivage = uAP050FransaBank.getNumTransaction()
					+ String.valueOf(o[0]).substring(String.valueOf(o[0]).length() - 6, String.valueOf(o[0]).length());
			uAP050FransaBank.setReferenceArchivage(RefArchivage + getSpace(18 - String.valueOf(RefArchivage).length()));
			uAP050FransaBank.setIndicateur("1");
			uAP050FransaBank.setNumRIBcommercant(String.valueOf(o[13]) + getSpace(20 - String.valueOf(o[13]).length()));
			uAP050FransaBank.setPrefixeIBAN("    ");
			uAP050FransaBank
					.setLibelleCommercant(String.valueOf(o[14]) + getSpace(50 - String.valueOf(o[14]).length()));
			for (Merchant merchant : merchants) {
				int lengthAdresse = merchant.getAddress().length();
				int lengthTelephone = merchant.getPhone().length();
				int lengthContrat = merchant.getIdContrat().length();
				if (merchant.getMerchantId().equals(o[17])) {
					uAP050FransaBank.setAdresseCommercant(
							merchant.getAddress().replaceAll("[^a-zA-Z0-9]", "") + getSpace(70 - lengthAdresse));
					uAP050FransaBank.setTelephoneCommercant(merchant.getPhone() + getSpace(10 - lengthTelephone));
					uAP050FransaBank.setNumContratAccepteur(merchant.getIdContrat() + getSpace(15 - lengthContrat));
				}
			}
			uAP050FransaBank.setCodeActivite("000035");
			uAP050FransaBank.setReserved("000");
			uAP050FransaBank
					.setCodeBin(String.valueOf(o[1]).substring(0, 6) + getSpace(6 - String.valueOf(o[1]).length()));
			uAP050FransaBank
					.setTypeCarte(String.valueOf(o[1]).substring(6, 8) + getSpace(2 - String.valueOf(o[1]).length()));
			uAP050FransaBank
					.setNumSeq(String.valueOf(o[1]).substring(8, 14) + getSpace(6 - String.valueOf(o[1]).length()));
			uAP050FransaBank
					.setNumOrdre(String.valueOf(o[1]).substring(14, 15) + getSpace(1 - String.valueOf(o[1]).length()));
			int length = String.valueOf(o[1]).length();
			String numCarte = String.valueOf(o[1]).substring(length - 1, length);
			uAP050FransaBank.setCle(numCarte);
			String montatnAcompenser = String.valueOf(o[12]).replace(".0", "");

			uAP050FransaBank.setMontantAComponser(getZero(15 - montatnAcompenser.length()) + montatnAcompenser);
			uAP050FransaBank.setFileDate(String.valueOf(o[15]) + getSpace(6 - String.valueOf(o[15]).length()));

			String amount = String.valueOf(o[2]).replace(".", "");
			uAP050FransaBank.setMontantRetrait(getZero(15 - amount.length()) + amount);

			uAP050FransaBank.setCodeDebitPorteur(String.valueOf(o[7]) + getSpace(1 - String.valueOf(o[7]).length()));
			uAP050FransaBank.setCodeDebitCommercant(String.valueOf(o[8]) + getSpace(1 - String.valueOf(o[8]).length()));
			String getListRembourssementCommission = dayOperationFransaBankRepository.getListRembourssemntTpeCommission(
					String.valueOf(o[0]), String.valueOf(o[16]), String.valueOf(o[1]));
			uAP050FransaBank.setMontantCommission(
					getZero(7 - getListRembourssementCommission.length()) + getListRembourssementCommission);

			uAP050FransaBank.setNumTransaction(String.valueOf(o[16]) + getSpace(12 - String.valueOf(o[16]).length()));
			uAP050FransaBank.setDateTransaction(String.valueOf(o[10]) + getSpace(8 - String.valueOf(o[10]).length()));
			uAP050FransaBank.setHeureTransaction(String.valueOf(o[11]) + getSpace(6 - String.valueOf(o[11]).length()));
			uAP050FransaBank.setIdentifSystem("00SATIMTPE");
			uAP050FransaBank
					.setIdentifPointRetrait(String.valueOf(o[18]) + getSpace(10 - String.valueOf(o[18]).length()));

			uAP050FransaBank.setModeLectureCarte("2");
			uAP050FransaBank.setMethAuthPorteur("1");

			uAP050FransaBank.setNumAutorisation(String.valueOf(o[0]) + getSpace(6 - String.valueOf(o[0]).length()));

			String dateBefore = String.valueOf(o[10]);
			DateTimeFormatter formater = DateTimeFormatter.ofPattern("yyyyMMdd");
			LocalDate date = LocalDate.parse(dateBefore, formater);

			LocalDate updatedDate = date.plusDays(1);

			String updatedDateString = updatedDate.format(DateTimeFormatter.ofPattern("yyyyMMdd"));

			uAP050FransaBank.setDateDebutValiditeCarte(updatedDateString + getSpace(8 - updatedDateString.length()));
			uAP050FransaBank.setDateFinValiditeCarte(updatedDateString + getSpace(8 - updatedDateString.length()));
			uAP050FransaBank.setCryptogramData("0");
			uAP050FransaBank.setAtc("00");
			uAP050FransaBank.setTvr("00000");
			uAP050FransaBank.setDateRemise(String.valueOf(o[10]) + getSpace(8 - String.valueOf(o[10]).length()));
			uAP050FransaBank.setFileIntegrationDate(fileIntegration);
			uAP050FransaBank.setPieceComptableBKM(String.valueOf(o[19]));
			uAP050FransaBank.setMontantTranasction(Integer.parseInt(uAP050FransaBank.getMontantRetrait()));
			uAP050FransaBank.setMontantCommissionConf(Integer.parseInt(uAP050FransaBank.getMontantCommission()));

			uAPFransaBanksRembourssementTPE.add(uAP050FransaBank);
		}
		logger.info("writeUAP051RembourssementTpe=>{}", uAPFransaBanksRembourssementTPE.size());
		uAP050FransaBankRepository.saveAll(uAPFransaBanksRembourssementTPE);

	}

	public void writeUAP051RembourssementInternet() {
		logger.info("writeUAP050RembourssementInternet");

		List<Object[]> dayOperationsRembourssementInternet = dayOperationFransaBankRepository
				.getListRembourssementInternetUAP();
		List<UAP051FransaBank> uAPFransaBanksRembourssementInternet = new ArrayList<UAP051FransaBank>();
		// List<Object[]> dayOperationAmountInternetRembourssement =
		// dayOperationFransaBankRepository.getListAmountRembourssementInternet();
		List<Merchant> merchants = dayOperationFransaBankRepository.getListMerchantRembourssementInternet();
		for (Object o[] : dayOperationsRembourssementInternet) {
			UAP051FransaBank uAP050FransaBank = new UAP051FransaBank();
			uAP050FransaBank.setCodeAgence(String.valueOf(o[3]) + getSpace(5 - String.valueOf(o[3]).length()));
			uAP050FransaBank.setTypeTransaction("051");
			String typePaiement = getTypePaiement(String.valueOf(o[4]));
			uAP050FransaBank.setTypePaiement(typePaiement);
			uAP050FransaBank.setNumTransaction(String.valueOf(o[16]) + getSpace(12 - String.valueOf(o[16]).length()));

			String RefArchivage = uAP050FransaBank.getNumTransaction()
					+ String.valueOf(o[0]).substring(String.valueOf(o[0]).length() - 6, String.valueOf(o[0]).length());
			uAP050FransaBank.setReferenceArchivage(RefArchivage + getSpace(18 - String.valueOf(RefArchivage).length()));
			uAP050FransaBank.setIndicateur("1");
			uAP050FransaBank.setNumRIBcommercant(String.valueOf(o[13]) + getSpace(20 - String.valueOf(o[13]).length()));
			uAP050FransaBank.setPrefixeIBAN("    ");
			uAP050FransaBank
					.setLibelleCommercant(String.valueOf(o[14]) + getSpace(50 - String.valueOf(o[14]).length()));
			for (Merchant merchant : merchants) {
				int lengthAdresse = merchant.getAddress().length();
				int lengthTelephone = merchant.getPhone().length();
				int lengthContrat = merchant.getIdContrat().length();
				if (merchant.getMerchantId().equals(o[17])) {
					uAP050FransaBank.setAdresseCommercant(
							merchant.getAddress().replaceAll("[^a-zA-Z0-9]", "") + getSpace(70 - lengthAdresse));
					uAP050FransaBank.setTelephoneCommercant(merchant.getPhone() + getSpace(10 - lengthTelephone));
					uAP050FransaBank.setNumContratAccepteur(merchant.getIdContrat() + getSpace(15 - lengthContrat));
				}
			}
			uAP050FransaBank.setCodeActivite("000035");
			uAP050FransaBank.setReserved("000");
			uAP050FransaBank
					.setCodeBin(String.valueOf(o[1]).substring(0, 6) + getSpace(6 - String.valueOf(o[1]).length()));
			uAP050FransaBank
					.setTypeCarte(String.valueOf(o[1]).substring(6, 8) + getSpace(2 - String.valueOf(o[1]).length()));
			uAP050FransaBank
					.setNumSeq(String.valueOf(o[1]).substring(8, 14) + getSpace(6 - String.valueOf(o[1]).length()));
			uAP050FransaBank
					.setNumOrdre(String.valueOf(o[1]).substring(14, 15) + getSpace(1 - String.valueOf(o[1]).length()));
			int length = String.valueOf(o[1]).length();
			String numCarte = String.valueOf(o[1]).substring(length - 1, length);
			uAP050FransaBank.setCle(numCarte);
			String montatnAcompenser = String.valueOf(o[12]).replace(".0", "");

			uAP050FransaBank.setMontantAComponser(getZero(15 - montatnAcompenser.length()) + montatnAcompenser);
			uAP050FransaBank.setFileDate(String.valueOf(o[15]) + getSpace(6 - String.valueOf(o[15]).length()));

			String amount = String.valueOf(o[2]).replace(".", "");
			uAP050FransaBank.setMontantRetrait(getZero(15 - amount.length()) + amount);

			uAP050FransaBank.setCodeDebitPorteur(String.valueOf(o[7]) + getSpace(1 - String.valueOf(o[7]).length()));
			uAP050FransaBank.setCodeDebitCommercant(String.valueOf(o[8]) + getSpace(1 - String.valueOf(o[8]).length()));
			String getListRembourssementInternetCommission = dayOperationFransaBankRepository
					.getListRembourssemntInternetCommission(String.valueOf(o[0]), String.valueOf(o[16]),
							String.valueOf(o[1]));
			uAP050FransaBank
					.setMontantCommission(getZero(7 - String.valueOf(getListRembourssementInternetCommission).length())
							+ String.valueOf(getListRembourssementInternetCommission));

			uAP050FransaBank.setDateTransaction(String.valueOf(o[10]) + getSpace(8 - String.valueOf(o[10]).length()));
			uAP050FransaBank.setHeureTransaction(String.valueOf(o[11]) + getSpace(6 - String.valueOf(o[11]).length()));
			uAP050FransaBank.setIdentifSystem("00SATIMTVP");
			uAP050FransaBank
					.setIdentifPointRetrait(String.valueOf(o[18]) + getSpace(10 - String.valueOf(o[18]).length()));

			uAP050FransaBank.setModeLectureCarte("2");
			uAP050FransaBank.setMethAuthPorteur("3");

			uAP050FransaBank.setNumAutorisation(String.valueOf(o[0]) + getSpace(6 - String.valueOf(o[0]).length()));
			String dateBefore = String.valueOf(o[10]);
			DateTimeFormatter formater = DateTimeFormatter.ofPattern("yyyyMMdd");
			LocalDate date = LocalDate.parse(dateBefore, formater);

			LocalDate updatedDate = date.plusDays(1);

			String updatedDateString = updatedDate.format(DateTimeFormatter.ofPattern("yyyyMMdd"));

			uAP050FransaBank.setDateDebutValiditeCarte(updatedDateString + getSpace(8 - updatedDateString.length()));
			uAP050FransaBank.setDateFinValiditeCarte(updatedDateString + getSpace(8 - updatedDateString.length()));
			uAP050FransaBank.setCryptogramData("0");
			uAP050FransaBank.setAtc("00");
			uAP050FransaBank.setTvr("00000");
			uAP050FransaBank.setDateRemise(String.valueOf(o[10]) + getSpace(8 - String.valueOf(o[10]).length()));
			uAP050FransaBank.setFileIntegrationDate(fileIntegration);
			uAP050FransaBank.setPieceComptableBKM(String.valueOf(o[19]));
			uAP050FransaBank.setMontantTranasction(Integer.parseInt(uAP050FransaBank.getMontantRetrait()));
			uAP050FransaBank.setMontantCommissionConf(Integer.parseInt(uAP050FransaBank.getMontantCommission()));
			uAPFransaBanksRembourssementInternet.add(uAP050FransaBank);
		}
		logger.info("writeUAP051RembourssementInternet=>{}", uAPFransaBanksRembourssementInternet.size());
		uAP050FransaBankRepository.saveAll(uAPFransaBanksRembourssementInternet);

	}

	public List<BkmvtiFransaBank> TestSigne(int methode, MvbkConf mvk,
			List<DayOperationFransaBank> dayOperationFransaBanksByNumTransaction,
			List<BkmvtiFransaBank> BkmvtiFransaBanks, int indexPieceComptable, int index2, String codeOperation) {
		BkmvtiFransaBank bkmvtiFransaBank2 = new BkmvtiFransaBank();
		Map<String, Float> data = new HashMap<>();

		for (DayOperationFransaBank op : dayOperationFransaBanksByNumTransaction) {
			data.put(op.getIdenfication(), op.getMontantSettlement());

		}
		DayOperationFransaBank op = dayOperationFransaBanksByNumTransaction
				.get(dayOperationFransaBanksByNumTransaction.size() - 1);

		if (AccountSigne(mvk.getAccount(), mvk.getSigne()) == 1) {
			bkmvtiFransaBank2 = TestAccountAndSigne(methode, index2, indexPieceComptable, (data.size() > 1 ? 2 : 1),
					op.getCompteCredit(), op.getCompteDebit().length(), mvk.getSigne(), mvk, op);

		}

		else if (AccountSigne(mvk.getAccount(), mvk.getSigne()) == 2) {
			bkmvtiFransaBank2 = TestAccountAndSigne(methode, index2, indexPieceComptable, (data.size() > 1 ? 2 : 1),
					op.getCompteDebit(), op.getCompteDebit().length(), mvk.getSigne(), mvk, op);

		} else if (AccountSigne(mvk.getAccount(), mvk.getSigne()) == 3) {

			String accountATM = getAccountAtm(op.getIdTerminal());
			bkmvtiFransaBank2 = TestAccountAndSigne(methode, index2, indexPieceComptable, (data.size() > 1 ? 2 : 1),
					accountATM, accountATM.length(), mvk.getSigne(), mvk, op);

		}

		else if (AccountSigne(mvk.getAccount(), mvk.getSigne()) == 4) {

			if ("BANK_ACQ".equals(mvk.getEntity())) {

				String codeBank = op.getCodeBankAcquereur();
				String mvbaccount = mvk.getAccount().substring(0, mvk.getAccount().length() - 3) + codeBank;

				bkmvtiFransaBank2 = TestAccountAndSigne(methode, index2, indexPieceComptable, (data.size() > 1 ? 2 : 1),
						mvbaccount, op.getCompteDebit().length(), mvk.getSigne(), mvk, op);
			} else if ("BANK_ISSUER".equals(mvk.getEntity())) {

				String codeBank = op.getCodeBank();
				String mvbaccount = mvk.getAccount().substring(0, mvk.getAccount().length() - 3) + codeBank;

				bkmvtiFransaBank2 = TestAccountAndSigne(methode, index2, indexPieceComptable, (data.size() > 1 ? 2 : 1),
						mvbaccount, op.getCompteDebit().length(), mvk.getSigne(), mvk, op);
			} else {
				bkmvtiFransaBank2 = TestAccountAndSigne(methode, index2, indexPieceComptable, (data.size() > 1 ? 2 : 1),
						mvk.getAccount(), op.getCompteDebit().length(), mvk.getSigne(), mvk, op);
			}

		}
		bkmvtiFransaBank2.setMontant(getAmountFormat(fIPService.evaluateWithElements(codeOperation, data)));
		try {

			if (Double.valueOf(bkmvtiFransaBank2.getMontant().replace(",", ".")) <= 0) {
				throw new Exception("amount lower than expected");
			}
		} catch (Exception e) {
			FileRequest.print("amount negatif", FileRequest.getLineNumber());
			return BkmvtiFransaBanks;
		}
		if ("AGENCE_C".equals(mvk.getEntity())) {
			bkmvtiFransaBank2.setNumCompte(
					bkmvtiFransaBank2.getNumCompte().substring(0, bkmvtiFransaBank2.getNumCompte().length() - 4)
							+ op.getCodeAgence().substring(1, op.getCodeAgence().length()));

		} else if ("AGENCE_P".equals(mvk.getEntity())) {
			bkmvtiFransaBank2.setNumCompte(
					bkmvtiFransaBank2.getNumCompte().substring(0, bkmvtiFransaBank2.getNumCompte().length() - 4)
							+ op.getNumRIBEmetteur().substring(4, 8).length());

		}
		BkmvtiFransaBanks.add(bkmvtiFransaBank2);

		return BkmvtiFransaBanks;
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

	public int AccountSigne(String account, String signe) {
		int test = 0;
		if (account.equals("PORTEUR")) {
			test = 1;
		} else if (account.equals("COMMERCANT")) {
			test = 2;
		} else if (account.equals("ATM")) {
			test = 3;
		} else if ((!account.equals("PORTEUR") && !account.equals("COMMERCANT") && !account.equals("ATM"))) {
			test = 4;
		}

		return test;

	}

	public BkmvtiFransaBank TestAccountAndSigne(int methode, int index2, int indexPieceComptable, int test,
			String account, int accountDebit, String signe, MvbkConf mvk, DayOperationFransaBank op) {
		BkmvtiFransaBank bkmvtiFransaBank = new BkmvtiFransaBank();
		TestAccountLength(account.length(), accountDebit, account, mvk, bkmvtiFransaBank, op);
		bkmvtiFransaBank.setCodeDevice("208");

		if (name.length() > 10) {
			name = name.substring(0, 10);
		}
		bkmvtiFransaBank.setCodeUtilisateur(name + getSpace(10 - name.length()));
		// bkmvtiFransaBank.setCodeUtilisateur("UTI1"+getSpace(7));
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

		TestCodeAgence(mvk.getCodeAgence(), mvk, op, bkmvtiFransaBank);

		getTransactionDate(op.getDateTransaction(), bkmvtiFransaBank);

		int lengthReferanceLettrage = op.getNumAutorisation().length();

		bkmvtiFransaBank.setReferanceLettrage(op.getDateTransaction().substring(6, op.getDateTransaction().length())
				+ op.getNumAutorisation().substring(lengthReferanceLettrage - 6, lengthReferanceLettrage));
		if (mvk.getType().equals("1")) {
			getAmount(op.getMontantSettlement(), bkmvtiFransaBank);
		}

		bkmvtiFransaBank.setCodeDeviceOrigine("208");
		bkmvtiFransaBank.setIdentification(mvk.getIdentification());
		bkmvtiFransaBank.setNumRefTransactions(op.getNumtransaction());

		setSameData(methode, bkmvtiFransaBank, index2, indexPieceComptable, op, mvk);
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

	public BkmvtiFransaBank setSameData(int methode, BkmvtiFransaBank bkmvtiFransaBank, int index2,
			int indexPieceComptable, DayOperationFransaBank op, MvbkConf mvk) {

		String lib = "";
		String cp = " ";
		String type = " ";
		bkmvtiFransaBank.setCodeOperation(mvk.getCodeOperation());
		lib = mvk.getLibelle_operation();
		bkmvtiFransaBank.setLibelle(lib + getSpace(40 - lib.length()));
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

			bkmvtiFransaBank.setPieceComptable(op.getPieceComptableBkm());

			bkmvtiFransaBank.setNumEvenement(op.getPieceComptableBkm().substring(op.getPieceComptableBkm().length() - 6,
					op.getPieceComptableBkm().length()));

			break;
		case 2:

			bkmvtiFransaBank.setPieceComptable(op.getPieceComptableBkm());

			bkmvtiFransaBank.setNumEvenement(op.getPieceComptableBkm().substring(op.getPieceComptableBkm().length() - 6,
					op.getPieceComptableBkm().length()));
			bkmvtiFransaBank.setNumEvenement(String.format("%06d", indexPieceComptable));

			break;
		case 3:

			bkmvtiFransaBank.setPieceComptable(op.getPieceComptableBkm());

			bkmvtiFransaBank.setNumEvenement(op.getPieceComptableBkm().substring(op.getPieceComptableBkm().length() - 6,
					op.getPieceComptableBkm().length()));

			break;

		case 4:

			bkmvtiFransaBank.setPieceComptable(op.getPieceComptableBkm());

			bkmvtiFransaBank.setNumEvenement(op.getPieceComptableBkm().substring(op.getPieceComptableBkm().length() - 6,
					op.getPieceComptableBkm().length()));

			break;

		case 5:

			bkmvtiFransaBank.setPieceComptable(op.getPieceComptableBkm());

			bkmvtiFransaBank.setNumEvenement(op.getPieceComptableBkm().substring(op.getPieceComptableBkm().length() - 6,
					op.getPieceComptableBkm().length()));

			break;
		case 6:

			bkmvtiFransaBank.setPieceComptable(op.getPieceComptableBkm());

			bkmvtiFransaBank.setNumEvenement(op.getPieceComptableBkm().substring(op.getPieceComptableBkm().length() - 6,
					op.getPieceComptableBkm().length()));

			break;

		case 7:

			bkmvtiFransaBank.setPieceComptable(op.getPieceComptableBkm());

			bkmvtiFransaBank.setNumEvenement(op.getPieceComptableBkm().substring(op.getPieceComptableBkm().length() - 6,
					op.getPieceComptableBkm().length()));

			break;
		case 8:

			bkmvtiFransaBank.setPieceComptable(op.getPieceComptableBkm());

			bkmvtiFransaBank.setNumEvenement(op.getPieceComptableBkm().substring(op.getPieceComptableBkm().length() - 6,
					op.getPieceComptableBkm().length()));

			break;
		case 9:

			bkmvtiFransaBank.setPieceComptable(op.getPieceComptableBkm());

			bkmvtiFransaBank.setNumEvenement(op.getPieceComptableBkm().substring(op.getPieceComptableBkm().length() - 6,
					op.getPieceComptableBkm().length()));

			break;
		case 10:

			bkmvtiFransaBank.setPieceComptable(op.getPieceComptableBkm());

			bkmvtiFransaBank.setNumEvenement(op.getPieceComptableBkm().substring(op.getPieceComptableBkm().length() - 6,
					op.getPieceComptableBkm().length()));

			break;
		case 11:

			bkmvtiFransaBank.setPieceComptable(op.getPieceComptableBkm());

			bkmvtiFransaBank.setNumEvenement(op.getPieceComptableBkm().substring(op.getPieceComptableBkm().length() - 6,
					op.getPieceComptableBkm().length()));

			break;
		case 12:

			bkmvtiFransaBank.setPieceComptable(op.getPieceComptableBkm());

			bkmvtiFransaBank.setNumEvenement(op.getPieceComptableBkm().substring(op.getPieceComptableBkm().length() - 6,
					op.getPieceComptableBkm().length()));

			break;
		case 13:

			bkmvtiFransaBank.setPieceComptable(op.getPieceComptableBkm());

			bkmvtiFransaBank.setNumEvenement(op.getPieceComptableBkm().substring(op.getPieceComptableBkm().length() - 6,
					op.getPieceComptableBkm().length()));

			break;
		case 14:

			bkmvtiFransaBank.setPieceComptable(op.getPieceComptableBkm());

			bkmvtiFransaBank.setNumEvenement(op.getPieceComptableBkm().substring(op.getPieceComptableBkm().length() - 6,
					op.getPieceComptableBkm().length()));

			break;
		///////////// NEW ***********////////////////////////

		case 15:

			bkmvtiFransaBank.setPieceComptable(op.getPieceComptableBkm());

			bkmvtiFransaBank.setNumEvenement(op.getPieceComptableBkm().substring(op.getPieceComptableBkm().length() - 6,
					op.getPieceComptableBkm().length()));

			break;

		case 16:

			bkmvtiFransaBank.setPieceComptable(op.getPieceComptableBkm());

			bkmvtiFransaBank.setNumEvenement(op.getPieceComptableBkm().substring(op.getPieceComptableBkm().length() - 6,
					op.getPieceComptableBkm().length()));

			break;

		case 17:

			bkmvtiFransaBank.setPieceComptable(op.getPieceComptableBkm());

			bkmvtiFransaBank.setNumEvenement(op.getPieceComptableBkm().substring(op.getPieceComptableBkm().length() - 6,
					op.getPieceComptableBkm().length()));

			break;

		case 18:

			bkmvtiFransaBank.setPieceComptable(op.getPieceComptableBkm());

			bkmvtiFransaBank.setNumEvenement(op.getPieceComptableBkm().substring(op.getPieceComptableBkm().length() - 6,
					op.getPieceComptableBkm().length()));

			break;

		case 19:

			bkmvtiFransaBank.setPieceComptable(op.getPieceComptableBkm());

			bkmvtiFransaBank.setNumEvenement(op.getPieceComptableBkm().substring(op.getPieceComptableBkm().length() - 6,
					op.getPieceComptableBkm().length()));

			break;
		case 20:

			bkmvtiFransaBank.setPieceComptable(op.getPieceComptableBkm());

			bkmvtiFransaBank.setNumEvenement(op.getPieceComptableBkm().substring(op.getPieceComptableBkm().length() - 6,
					op.getPieceComptableBkm().length()));

			break;

		case 21:

			bkmvtiFransaBank.setPieceComptable(op.getPieceComptableBkm());

			bkmvtiFransaBank.setNumEvenement(op.getPieceComptableBkm().substring(op.getPieceComptableBkm().length() - 6,
					op.getPieceComptableBkm().length()));

			break;

		case 22:

			bkmvtiFransaBank.setPieceComptable(op.getPieceComptableBkm());

			bkmvtiFransaBank.setNumEvenement(op.getPieceComptableBkm().substring(op.getPieceComptableBkm().length() - 6,
					op.getPieceComptableBkm().length()));

			break;
		case 23:

			bkmvtiFransaBank.setPieceComptable(op.getPieceComptableBkm());

			bkmvtiFransaBank.setNumEvenement(op.getPieceComptableBkm().substring(op.getPieceComptableBkm().length() - 6,
					op.getPieceComptableBkm().length()));

			break;
		case 24:

			bkmvtiFransaBank.setPieceComptable(op.getPieceComptableBkm());

			bkmvtiFransaBank.setNumEvenement(op.getPieceComptableBkm().substring(op.getPieceComptableBkm().length() - 6,
					op.getPieceComptableBkm().length()));

			break;
		default:
			bkmvtiFransaBank.setPieceComptable(op.getPieceComptableBkm());

			bkmvtiFransaBank.setNumEvenement(op.getPieceComptableBkm().substring(op.getPieceComptableBkm().length() - 6,
					op.getPieceComptableBkm().length()));
		}
		return bkmvtiFransaBank;
	}

	public BkmvtiFransaBank TestAccountLength(int lengAccount, int lengAccountDebit, String Account, MvbkConf mvk,
			BkmvtiFransaBank bkmvtiFransaBank, DayOperationFransaBank op) {

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

	public BkmvtiFransaBank TestCodeAgence(String codeAgence, MvbkConf mvk, DayOperationFransaBank op,
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

	public BkmvtiFransaBank getAmount(float amountSettlement, BkmvtiFransaBank bkmvtiFransaBank) {
		int amountRound = Math.round(amountSettlement);
		String amountString = String.valueOf(amountRound);
		int AmountLengh1 = amountString.length();
		String amount = amountString.substring(0, AmountLengh1 - 2) + "," + amountString.substring(AmountLengh1 - 2);

		bkmvtiFransaBank.setMontant(getZero(20 - AmountLengh1) + amount);
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

	public String getAmountFormat(float amount) {
		float m = Math.round(amount);
		int a = (int) m;

		String amountFormat = String.format("%018d,%02d", a / 100, a % 100);
		return amountFormat;
	}

	@Override
	public void run() {
		switch (paramUAP51) {
		case "1":
			writeUAP051();
			break;
		default:
			System.out.println("C tt ");
			break;
		}
		System.out.println("Param051" + paramUAP51);
	}

}
