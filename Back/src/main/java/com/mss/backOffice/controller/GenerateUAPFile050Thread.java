package com.mss.backOffice.controller;

import com.mss.backOffice.services.FormulaInterpreterService;
import com.mss.unified.entities.Account;
import com.mss.unified.entities.AtmTerminal;
import com.mss.unified.entities.BkmvtiFransaBank;
import com.mss.unified.entities.DayOperationFransaBank;
import com.mss.unified.entities.Merchant;
import com.mss.unified.entities.MvbkConf;
import com.mss.unified.entities.MvbkSettlement;
import com.mss.unified.entities.UAP050FransaBank;
import com.mss.unified.entities.User;
import com.mss.unified.repositories.BkmvtiFransaBankRepository;
import com.mss.unified.repositories.CodeBankBCRepository;
import com.mss.unified.repositories.DayOperationFransaBankRepository;
import com.mss.unified.repositories.MvbkConfigRepository;
import com.mss.unified.repositories.MvbkSettlementRepository;
import com.mss.unified.repositories.UAP050FransaBankRepository;
import com.mss.unified.repositories.UAPFransaBankRepository;
import com.mss.unified.repositories.UserRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public  strictfp class GenerateUAPFile050Thread implements Runnable {
	public String fileIntegration;
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
	UAP050FransaBankRepository uAP050FransaBankRepository;

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

	private static final Logger logger = LoggerFactory.getLogger(DayOpFransaBankThread.class);
	public String paramUAP50;

	public GenerateUAPFile050Thread(DayOperationFransaBankRepository dayOperationFransaBankRepository,
			String paramUAP50, UAP050FransaBankRepository uAP050FransaBankRepository,
			MvbkSettlementRepository mvbkSettlementRepository, BkmvtiFransaBankRepository bkmvtiFransaBankRepository,
			String name, String fileIntegration, MvbkConfigRepository mvbkConfigR) {
		super();
		this.dayOperationFransaBankRepository = dayOperationFransaBankRepository;
		this.paramUAP50 = paramUAP50;
		this.uAP050FransaBankRepository = uAP050FransaBankRepository;
		this.mvbkSettlementRepository = mvbkSettlementRepository;
		this.bkmvtiFransaBankRepository = bkmvtiFransaBankRepository;
		this.name = name;
		this.fileIntegration = fileIntegration;
		this.mvbkConfigR = mvbkConfigR;

	}

	public void writeUAP050() {
		writeUAP050PaiementSurTpe();

		writeUAP050AchatInternet();
		writeUAP050AchatInternetAlgeriePoste();
//		generationUAP050PaiementSurTpe();
//
//		generationUAP050AchatInternet();
//		generationUAP050AchatInternetAlgeriePoste();
	}

	public String getTypePaiement(String typeRetrait) {
		String typePaeiment = "";
		switch (typeRetrait) {

		case "050":
			typePaeiment = "01";
			break;
		case "053":
			typePaeiment = "03";

			break;
		case "005":
			typePaeiment = "05";

			break;

		case "042":
			typePaeiment = "06";
			break;

		case "041":
			typePaeiment = "07";

			break;
		case "052":
			typePaeiment = "04";

			break;

		default:
			System.out.println("nothing");
		}
		return typePaeiment;
	}

	public void writeUAP050PaiementSurTpe() {
		logger.info("writeUAP050PaiementSurTpe");

		List<Object[]> dayOperations = dayOperationFransaBankRepository.getListPaiementSurTpeUAP();
		List<UAP050FransaBank> uAPFransaBanks = new ArrayList<UAP050FransaBank>();
		List<Merchant> merchants = dayOperationFransaBankRepository.getListMerchantPaiementTpe();
		for (Object o[] : dayOperations) {

			UAP050FransaBank UAPFransaBank = new UAP050FransaBank();
			String typePaiement = getTypePaiement(String.valueOf(o[4]));
			UAPFransaBank.setTypeTransaction("050");
			UAPFransaBank.setTypePaiement(typePaiement);
			UAPFransaBank.setNumTransaction(String.valueOf(o[16]) + getSpace(12 - String.valueOf(o[16]).length()));

			String RefArchivage = UAPFransaBank.getNumTransaction()
					+ String.valueOf(o[0]).substring(String.valueOf(o[0]).length() - 6, String.valueOf(o[0]).length());
			UAPFransaBank.setCodeAgence(String.valueOf(o[3]) + getSpace(5 - String.valueOf(o[3]).length()));

			UAPFransaBank.setReferenceArchivage(RefArchivage + getSpace(18 - String.valueOf(RefArchivage).length()));
			UAPFransaBank.setIndicateur("1");
			UAPFransaBank.setNumRIBcommercant(String.valueOf(o[13]) + getSpace(20 - String.valueOf(o[13]).length()));
			UAPFransaBank.setPrefixeIBAN("    ");
			UAPFransaBank.setLibelleCommercant(String.valueOf(o[14]) + getSpace(50 - String.valueOf(o[14]).length()));
			for (Merchant merchant : merchants) {
				int lengthAdresse = merchant.getAddress().length();
				int lengthTelephone = merchant.getPhone().length();
				int lengthContrat = merchant.getIdContrat().length();
				if (merchant.getMerchantId().equals(o[17])) {
					UAPFransaBank.setAdresseCommercant(
							merchant.getAddress().replaceAll("[^a-zA-Z0-9]", "") + getSpace(70 - lengthAdresse));
					UAPFransaBank.setTelephoneCommercant(merchant.getPhone() + getSpace(10 - lengthTelephone));
					UAPFransaBank.setNumContratAccepteur(merchant.getIdContrat() + getSpace(15 - lengthContrat));
				}
			}
			UAPFransaBank.setCodeActivite("000035");
			UAPFransaBank.setReserved("000");
			UAPFransaBank
					.setCodeBin(String.valueOf(o[1]).substring(0, 6) + getSpace(6 - String.valueOf(o[1]).length()));
			UAPFransaBank
					.setTypeCarte(String.valueOf(o[1]).substring(6, 8) + getSpace(2 - String.valueOf(o[1]).length()));
			UAPFransaBank
					.setNumSeq(String.valueOf(o[1]).substring(8, 14) + getSpace(6 - String.valueOf(o[1]).length()));
			UAPFransaBank
					.setNumOrdre(String.valueOf(o[1]).substring(14, 15) + getSpace(1 - String.valueOf(o[1]).length()));
			int length = String.valueOf(o[1]).length();
			String numCarte = String.valueOf(o[1]).substring(length - 1, length);
			UAPFransaBank.setCle(numCarte);
			String amount = String.valueOf(o[2]).replace(".", "");
			UAPFransaBank.setMontantRetrait(getZero(15 - amount.length()) + amount);

			UAPFransaBank.setFileDate(String.valueOf(o[15]) + getSpace(6 - String.valueOf(o[15]).length()));
			BigDecimal combg = new BigDecimal(String.valueOf(o[12]));
//			BigDecimal multip = new BigDecimal(100);

			int commissions = combg.intValue();
			UAPFransaBank.setMontantCommission(getZero(7 - String.valueOf(Math.round(commissions)).length())
					+ String.valueOf(Math.round(commissions)));
			int montant_compensee = (int) (Integer.parseInt(amount) - Math.round(commissions));
			UAPFransaBank.setMontantAComponser(
					getZero(15 - String.valueOf(montant_compensee).length()) + String.valueOf(montant_compensee));

			UAPFransaBank.setCodeDebitPorteur(String.valueOf(o[7]) + getSpace(1 - String.valueOf(o[7]).length()));
			UAPFransaBank.setCodeDebitCommercant(String.valueOf(o[8]) + getSpace(1 - String.valueOf(o[8]).length()));

			UAPFransaBank.setDateTransaction(String.valueOf(o[10]) + getSpace(8 - String.valueOf(o[10]).length()));
			UAPFransaBank.setHeureTransaction(String.valueOf(o[11]) + getSpace(6 - String.valueOf(o[11]).length()));
			UAPFransaBank.setIdentifSystem("00SATIMTPE");
			UAPFransaBank.setIdentifPointRetrait(String.valueOf(o[18]) + getSpace(10 - String.valueOf(o[18]).length()));

			UAPFransaBank.setModeLectureCarte("2");
			UAPFransaBank.setMethAuthPorteur("1");

			UAPFransaBank.setNumAutorisation(String.valueOf(o[0]) + getSpace(6 - String.valueOf(o[0]).length()));
			String dateBefore = String.valueOf(o[10]);
			DateTimeFormatter formater = DateTimeFormatter.ofPattern("yyyyMMdd");
			LocalDate date = LocalDate.parse(dateBefore, formater);

			LocalDate updatedDate = date.plusDays(1);

			String updatedDateString = updatedDate.format(DateTimeFormatter.ofPattern("yyyyMMdd"));

			UAPFransaBank.setDateDebutValiditeCarte(updatedDateString + getSpace(8 - updatedDateString.length()));
			UAPFransaBank.setDateFinValiditeCarte(updatedDateString + getSpace(8 - updatedDateString.length()));
			UAPFransaBank.setCryptogramData("0");
			UAPFransaBank.setAtc("00");
			UAPFransaBank.setTvr("00000");
			UAPFransaBank.setDateRemise(String.valueOf(o[10]) + getSpace(8 - String.valueOf(o[10]).length()));
			UAPFransaBank.setFileIntegrationDate(fileIntegration);
			UAPFransaBank.setPieceComptableBKM(String.valueOf(o[19]));
			UAPFransaBank.setMontantTranasction(Integer.parseInt(String.valueOf(o[2]).replace(".", "")));
			UAPFransaBank.setMontantCommissionConf(commissions);
			uAPFransaBanks.add(UAPFransaBank);
		}
		logger.info("writeUAP050PaiementSurTpe=>{}", uAPFransaBanks.size());
		uAP050FransaBankRepository.saveAll(uAPFransaBanks);
	}

	public void writeUAP050AchatInternet() {
		logger.info("writeUAP050AchatInternet");

		List<Object[]> dayOperationsAchatInternet = dayOperationFransaBankRepository.getListAchatInternetUAP();
		List<UAP050FransaBank> uAPFransaBanksAchatInternet = new ArrayList<UAP050FransaBank>();
		List<Merchant> merchants = dayOperationFransaBankRepository.getListMerchantAchatInternet();
		for (Object o[] : dayOperationsAchatInternet) {
			UAP050FransaBank uAP050FransaBank = new UAP050FransaBank();
			uAP050FransaBank.setNumTransaction(String.valueOf(o[16]) + getSpace(12 - String.valueOf(o[16]).length()));

			uAP050FransaBank.setTypeTransaction("050");
			String typePaiement = getTypePaiement(String.valueOf(o[4]));
			uAP050FransaBank.setCodeAgence(String.valueOf(o[3]) + getSpace(5 - String.valueOf(o[3]).length()));

			uAP050FransaBank.setTypePaiement(typePaiement);
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
			uAP050FransaBank.setFileDate(String.valueOf(o[15]) + getSpace(6 - String.valueOf(o[15]).length()));
			int length = String.valueOf(o[1]).length();
			String numCarte = String.valueOf(o[1]).substring(length - 1, length);
			uAP050FransaBank.setCle(numCarte);

			String amount = String.valueOf(o[2]).replace(".", "");
			uAP050FransaBank.setMontantRetrait(getZero(15 - amount.length()) + amount);

			uAP050FransaBank.setCodeDebitPorteur(String.valueOf(o[7]) + getSpace(1 - String.valueOf(o[7]).length()));
			uAP050FransaBank.setCodeDebitCommercant(String.valueOf(o[8]) + getSpace(1 - String.valueOf(o[8]).length()));

			BigDecimal combg = new BigDecimal(String.valueOf(o[12]));
//			BigDecimal multip = new BigDecimal(100);

			int commissions = combg.intValue();
			uAP050FransaBank.setMontantCommission(getZero(7 - String.valueOf(Math.round(commissions)).length())
					+ String.valueOf(Math.round(commissions)));

			uAP050FransaBank.setMontantCommission(getZero(7 - String.valueOf(Math.round(commissions)).length())
					+ String.valueOf(Math.round(commissions)));
			int montant_compensee = (Integer.parseInt(amount) - Math.round(commissions));
			uAP050FransaBank.setMontantAComponser(
					getZero(15 - String.valueOf(montant_compensee).length()) + String.valueOf(montant_compensee));

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
			uAP050FransaBank.setDateFinValiditeCarte(updatedDateString);
			uAP050FransaBank.setCryptogramData("0");
			uAP050FransaBank.setAtc("00");
			uAP050FransaBank.setTvr("00000");
			uAP050FransaBank.setDateRemise(String.valueOf(o[10]) + getSpace(8 - String.valueOf(o[10]).length()));
			uAP050FransaBank.setFileIntegrationDate(fileIntegration);
			uAP050FransaBank.setPieceComptableBKM(String.valueOf(o[19]));
			uAP050FransaBank.setTagPaiement("1");
			uAP050FransaBank.setMontantTranasction(Integer.parseInt(String.valueOf(o[2]).replace(".", "")));
			uAP050FransaBank.setMontantCommissionConf(commissions);
			uAPFransaBanksAchatInternet.add(uAP050FransaBank);
		}
		uAP050FransaBankRepository.saveAll(uAPFransaBanksAchatInternet);

	}

	public void writeUAP050AchatInternetAlgeriePoste() {
		logger.info("writeUAP050AchatInternetAlgeriePoste");

		List<Object[]> dayOperationsAchatInternet = dayOperationFransaBankRepository
				.getListAchatInternetAlgeriePosteUAP();
		List<UAP050FransaBank> uAPFransaBanksAchatInternet = new ArrayList<UAP050FransaBank>();
		List<Merchant> merchants = dayOperationFransaBankRepository.getListMerchantAchatInternetAlgeriePoste();
		for (Object o[] : dayOperationsAchatInternet) {
			UAP050FransaBank uAP050FransaBank = new UAP050FransaBank();
			uAP050FransaBank.setCodeAgence(String.valueOf(o[3]) + getSpace(5 - String.valueOf(o[3]).length()));
			uAP050FransaBank.setNumTransaction(String.valueOf(o[16]) + getSpace(12 - String.valueOf(o[16]).length()));

			uAP050FransaBank.setTypeTransaction("050");
			String typePaiement = getTypePaiement(String.valueOf(o[4]));
			uAP050FransaBank.setTypePaiement(typePaiement);
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
			uAP050FransaBank.setFileDate(String.valueOf(o[15]) + getSpace(6 - String.valueOf(o[15]).length()));
			int length = String.valueOf(o[1]).length();
			String numCarte = String.valueOf(o[1]).substring(length - 1, length);
			uAP050FransaBank.setCle(numCarte);

			String amount = String.valueOf(o[2]).replace(".", "");
			uAP050FransaBank.setMontantRetrait(getZero(15 - amount.length()) + amount);

			uAP050FransaBank.setCodeDebitPorteur(String.valueOf(o[7]) + getSpace(1 - String.valueOf(o[7]).length()));
			uAP050FransaBank.setCodeDebitCommercant(String.valueOf(o[8]) + getSpace(1 - String.valueOf(o[8]).length()));
			BigDecimal combg = new BigDecimal(String.valueOf(o[12]));
//			BigDecimal multip = new BigDecimal(100);

			int commissions = combg.intValue();
			uAP050FransaBank.setMontantCommission(getZero(7 - String.valueOf(Math.round(commissions)).length())
					+ String.valueOf(Math.round(commissions)));
			int montant_compensee = (int) (Integer.parseInt(amount) - Math.round(commissions));
			uAP050FransaBank.setMontantAComponser(
					getZero(15 - String.valueOf(montant_compensee).length()) + String.valueOf(montant_compensee));
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

			uAP050FransaBank.setDateDebutValiditeCarte(updatedDateString);
			uAP050FransaBank.setDateFinValiditeCarte(updatedDateString);
			uAP050FransaBank.setCryptogramData("0");
			uAP050FransaBank.setAtc("00");
			uAP050FransaBank.setTvr("00000");
			uAP050FransaBank.setDateRemise(String.valueOf(o[10]) + getSpace(8 - String.valueOf(o[10]).length()));
			uAP050FransaBank.setFileIntegrationDate(fileIntegration);
			uAP050FransaBank.setPieceComptableBKM(String.valueOf(o[19]));
			uAP050FransaBank.setTagPaiement("2");
			uAP050FransaBank.setMontantTranasction(Integer.parseInt(String.valueOf(o[2]).replace(".", "")));
			uAP050FransaBank.setMontantCommissionConf(commissions);
			uAPFransaBanksAchatInternet.add(uAP050FransaBank);
		}
		uAP050FransaBankRepository.saveAll(uAPFransaBanksAchatInternet);

	}

	public BkmvtiFransaBank setSameData(int methode, BkmvtiFransaBank bkmvtiFransaBank, int index2,
			int indexPieceComptable, DayOperationFransaBank op, MvbkConf mvk) {
		String lib = "";
		String cp = " ";
		String type = " ";
		bkmvtiFransaBank.setCodeOperation(mvk.getCodeOperation());
		lib = mvk.getLibelle_operation();
		bkmvtiFransaBank.setLibelle(lib + getSpace(40 - lib.length()));
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
			bkmvtiFransaBank.setCodeOperation("171");
			lib = "PAIEMENT PORTEUR CONF/TPE FSBK";
			bkmvtiFransaBank.setLibelle(lib + getSpace(40 - lib.length()));

			break;
		case 9:

			bkmvtiFransaBank.setPieceComptable(op.getPieceComptableBkm());

			bkmvtiFransaBank.setNumEvenement(op.getPieceComptableBkm().substring(op.getPieceComptableBkm().length() - 6,
					op.getPieceComptableBkm().length()));
			bkmvtiFransaBank.setCodeOperation("187");
			lib = "CONSULTATION SOLDE PORTEUR FSBK/ATM FSBK";
			bkmvtiFransaBank.setLibelle(lib + getSpace(40 - lib.length()));

			break;
		case 10:

			bkmvtiFransaBank.setPieceComptable(op.getPieceComptableBkm());

			bkmvtiFransaBank.setNumEvenement(op.getPieceComptableBkm().substring(op.getPieceComptableBkm().length() - 6,
					op.getPieceComptableBkm().length()));

			bkmvtiFransaBank.setCodeOperation("***");
			lib = "CONSULTATION SOLDE PORTEUR FSBK/ATM CONF";
			bkmvtiFransaBank.setLibelle(lib + getSpace(40 - lib.length()));

			break;
		case 11:

			bkmvtiFransaBank.setPieceComptable(op.getPieceComptableBkm());

			bkmvtiFransaBank.setNumEvenement(op.getPieceComptableBkm().substring(op.getPieceComptableBkm().length() - 6,
					op.getPieceComptableBkm().length()));

			bkmvtiFransaBank.setCodeOperation("***");
			lib = "CONSULTATION SOLDE PORTEUR CONF/ATM FSBK";
			bkmvtiFransaBank.setLibelle(lib + getSpace(40 - lib.length()));

			break;
		case 12:

			bkmvtiFransaBank.setPieceComptable(op.getPieceComptableBkm());

			bkmvtiFransaBank.setNumEvenement(op.getPieceComptableBkm().substring(op.getPieceComptableBkm().length() - 6,
					op.getPieceComptableBkm().length()));

			bkmvtiFransaBank.setCodeOperation("***");
			lib = "REMBOURSSEMENT PORTEUR FSBK/TPE FSBK";
			bkmvtiFransaBank.setLibelle(lib + getSpace(40 - lib.length()));

			break;
		case 13:

			bkmvtiFransaBank.setPieceComptable(op.getPieceComptableBkm());

			bkmvtiFransaBank.setNumEvenement(op.getPieceComptableBkm().substring(op.getPieceComptableBkm().length() - 6,
					op.getPieceComptableBkm().length()));

			bkmvtiFransaBank.setCodeOperation("***");
			lib = "REMBOURSSEMENT PORTEUR FSBK/TPE CONF";
			bkmvtiFransaBank.setLibelle(lib + getSpace(40 - lib.length()));

			break;
		case 14:

			bkmvtiFransaBank.setPieceComptable(op.getPieceComptableBkm());

			bkmvtiFransaBank.setNumEvenement(op.getPieceComptableBkm().substring(op.getPieceComptableBkm().length() - 6,
					op.getPieceComptableBkm().length()));

			bkmvtiFransaBank.setCodeOperation("***");
			lib = "REMBOURSSEMENT PORTEUR CONF/TPE FSBK";
			bkmvtiFransaBank.setLibelle(lib + getSpace(40 - lib.length()));

			break;
		///////////// NEW ***********////////////////////////

		case 15:

			bkmvtiFransaBank.setPieceComptable(op.getPieceComptableBkm());

			bkmvtiFransaBank.setNumEvenement(op.getPieceComptableBkm().substring(op.getPieceComptableBkm().length() - 6,
					op.getPieceComptableBkm().length()));
			bkmvtiFransaBank.setCodeOperation("170");
			lib = "RETRAIT PORTEUR ALP/ATM FSBK";
			bkmvtiFransaBank.setLibelle(lib + getSpace(40 - lib.length()));

			break;

		case 16:

			bkmvtiFransaBank.setPieceComptable(op.getPieceComptableBkm());

			bkmvtiFransaBank.setNumEvenement(op.getPieceComptableBkm().substring(op.getPieceComptableBkm().length() - 6,
					op.getPieceComptableBkm().length()));
			bkmvtiFransaBank.setCodeOperation("170");
			lib = "RETRAIT PORTEUR FSBK/ATM ALP";
			bkmvtiFransaBank.setLibelle(lib + getSpace(40 - lib.length()));

			break;

		case 17:

			bkmvtiFransaBank.setPieceComptable(op.getPieceComptableBkm());

			bkmvtiFransaBank.setNumEvenement(op.getPieceComptableBkm().substring(op.getPieceComptableBkm().length() - 6,
					op.getPieceComptableBkm().length()));
			bkmvtiFransaBank.setCodeOperation("170");
			lib = "PAIEMENT PORTEUR FSBK/TVP FSBK";
			bkmvtiFransaBank.setLibelle(lib + getSpace(40 - lib.length()));

			break;

		case 18:

			bkmvtiFransaBank.setPieceComptable(op.getPieceComptableBkm());

			bkmvtiFransaBank.setNumEvenement(op.getPieceComptableBkm().substring(op.getPieceComptableBkm().length() - 6,
					op.getPieceComptableBkm().length()));
			bkmvtiFransaBank.setCodeOperation("170");
			lib = "PAIEMENT PORTEUR FSBK/TVP CONF";
			bkmvtiFransaBank.setLibelle(lib + getSpace(40 - lib.length()));

			break;

		case 19:

			bkmvtiFransaBank.setPieceComptable(op.getPieceComptableBkm());

			bkmvtiFransaBank.setNumEvenement(op.getPieceComptableBkm().substring(op.getPieceComptableBkm().length() - 6,
					op.getPieceComptableBkm().length()));
			bkmvtiFransaBank.setCodeOperation("170");
			lib = "PAIEMENT PORTEUR CONF/TVP FSBK";
			bkmvtiFransaBank.setLibelle(lib + getSpace(40 - lib.length()));

			break;
		case 20:

			bkmvtiFransaBank.setPieceComptable(op.getPieceComptableBkm());

			bkmvtiFransaBank.setNumEvenement(op.getPieceComptableBkm().substring(op.getPieceComptableBkm().length() - 6,
					op.getPieceComptableBkm().length()));
			bkmvtiFransaBank.setCodeOperation("170");
			lib = "PAIEMENT PORTEUR FSBK/TVP ALP";
			bkmvtiFransaBank.setLibelle(lib + getSpace(40 - lib.length()));

			break;

		case 21:

			bkmvtiFransaBank.setPieceComptable(op.getPieceComptableBkm());

			bkmvtiFransaBank.setNumEvenement(op.getPieceComptableBkm().substring(op.getPieceComptableBkm().length() - 6,
					op.getPieceComptableBkm().length()));
			bkmvtiFransaBank.setCodeOperation("170");
			lib = "PAIEMENT PORTEUR ALP/TVP FSBK";
			bkmvtiFransaBank.setLibelle(lib + getSpace(40 - lib.length()));

			break;

		case 22:

			bkmvtiFransaBank.setPieceComptable(op.getPieceComptableBkm());

			bkmvtiFransaBank.setNumEvenement(op.getPieceComptableBkm().substring(op.getPieceComptableBkm().length() - 6,
					op.getPieceComptableBkm().length()));
			bkmvtiFransaBank.setCodeOperation("170");
			lib = "REMBOURSSEMENT PORTEUR FSBK/TVP FSBK";
			bkmvtiFransaBank.setLibelle(lib + getSpace(40 - lib.length()));

			break;
		case 23:

			bkmvtiFransaBank.setPieceComptable(op.getPieceComptableBkm());

			bkmvtiFransaBank.setNumEvenement(op.getPieceComptableBkm().substring(op.getPieceComptableBkm().length() - 6,
					op.getPieceComptableBkm().length()));
			bkmvtiFransaBank.setCodeOperation("170");
			lib = "Remboursement porteur FSBK/ TVP CONF";
			bkmvtiFransaBank.setLibelle(lib + getSpace(40 - lib.length()));

			break;
		case 24:

			bkmvtiFransaBank.setPieceComptable(op.getPieceComptableBkm());

			bkmvtiFransaBank.setNumEvenement(op.getPieceComptableBkm().substring(op.getPieceComptableBkm().length() - 6,
					op.getPieceComptableBkm().length()));
			bkmvtiFransaBank.setCodeOperation("170");
			lib = "Remboursement porteur CONF/ TVP FSBK";
			bkmvtiFransaBank.setLibelle(lib + getSpace(40 - lib.length()));
			break;
		default:
			System.out.println("nothing");
		}
		if (mvk.getLibGenerique() != null && mvk.getLibGenerique().trim() != "") {
			String libgenerique = mvk.getLibGenerique();
			libgenerique = libgenerique.replaceAll("dateTransation", op.getDateTransaction());
			String aut = op.getNumAutorisation();
			int lengthAuth = aut.length();
			libgenerique = libgenerique.replaceAll("numAutorisation", aut.substring(lengthAuth - 6));
			bkmvtiFransaBank.setLibelle(libgenerique + getSpace(40 - lib.length()));
		}
		return bkmvtiFransaBank;
	}

	 
	public String getAccountAtm(String idTerminal) {

		String account = "";
		for (AtmTerminal element : ExecutorThreadFransaBank.atmTerminal) {
			if ((element.getAteId().substring(0, idTerminal.length()).trim()).equals(idTerminal.trim())) {
				for (Account e : ExecutorThreadFransaBank.account) {
					if (e.getCustomerCode().equals(String.valueOf(element.getMerchantCode())))
						account = "0" + e.getAccountNum();

				}
				;
			}
		}
		;
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

//	public BkmvtiFransaBank TestAccountAndSigne(int methode, int index2, int indexPieceComptable, int test,
//			String account, int accountDebit, String signe, MvbkConf mvk, DayOperationFransaBank op) {
//		BkmvtiFransaBank bkmvtiFransaBank = new BkmvtiFransaBank();
//		TestAccountLength(account.length(), accountDebit, account, mvk, bkmvtiFransaBank, op);
//		bkmvtiFransaBank.setCodeDevice("208");
//		if (name.length() > 10) {
//			name = name.substring(0, 10);
//		}
//
//		bkmvtiFransaBank.setCodeUtilisateur(name + getSpace(10 - name.length()));
//		// bkmvtiFransaBank.setCodeUtilisateur("UTI1"+getSpace(7));
//		bkmvtiFransaBank.setCodeService("0000");
//		bkmvtiFransaBank.setSens(mvk.getSigne());
//		bkmvtiFransaBank.setExonerationcommission("O");
//		int lengthNumPiece = op.getNumAutorisation().length();
//		bkmvtiFransaBank.setNumPiece(op.getNumAutorisation().substring(lengthNumPiece - 11, lengthNumPiece));
//		bkmvtiFransaBank.setTauxChange("1" + getSpace(6));
//		bkmvtiFransaBank.setCalculmouvementInteragence("N");
//		bkmvtiFransaBank.setMouvementAgence("N");
//		//// add new fileds //////
//		int lengthNumAuth = op.getNumAutorisation().length();
//		int lengthRefDossier = (op.getNumRefTransaction() + op.getDateTransaction()
//				+ op.getNumAutorisation().substring(lengthNumAuth - 6, lengthNumAuth)).length();
//		bkmvtiFransaBank.setRefDossier(op.getNumRefTransaction() + op.getDateTransaction()
//				+ op.getNumAutorisation().substring(lengthNumAuth - 6, lengthNumAuth)
//				+ getSpace(50 - lengthRefDossier));
//
//		TestCodeAgence(mvk.getCodeAgence(), mvk, op, bkmvtiFransaBank);
//
//		getTransactionDate(op.getDateTransaction(), bkmvtiFransaBank);
//
//		int lengthReferanceLettrage = op.getNumAutorisation().length();
//
//		bkmvtiFransaBank.setReferanceLettrage(op.getDateTransaction().substring(6, op.getDateTransaction().length())
//				+ op.getNumAutorisation().substring(lengthReferanceLettrage - 6, lengthReferanceLettrage));
//		if (mvk.getType().equals("1")) {
//			getAmount(op.getMontantSettlement(), bkmvtiFransaBank);
//		}
//
//		bkmvtiFransaBank.setCodeDeviceOrigine("208");
//		bkmvtiFransaBank.setIdentification(mvk.getIdentification());
//		bkmvtiFransaBank.setNumRefTransactions(op.getNumtransaction());
//
//		setSameData(methode, bkmvtiFransaBank, index2, indexPieceComptable, op, mvk);
//		try {
//			bkmvtiFransaBank.setMontant(signe);
//			if (Double.valueOf(bkmvtiFransaBank.getMontant().replace(",", ".")) <= 0) {
//				throw new Exception("amount lower than expected");
//			}
//		} catch (Exception e) {
//			return null;
//		}
//		return bkmvtiFransaBank;
//
//	}

//	public BkmvtiFransaBank TestAccountLength(int lengAccount, int lengAccountDebit, String Account, MvbkConf mvk,
//			BkmvtiFransaBank bkmvtiFransaBank, DayOperationFransaBank op) {
//
//		if (lengAccount > 18) {
//
//			String credit = Account.substring(8, 18);
//			String chapitreCompta = Account.substring(8, 14);
//			bkmvtiFransaBank.setChapitreComptable(chapitreCompta + getZero(6 - chapitreCompta.length()));
//			bkmvtiFransaBank.setNumCompte(credit);
//			bkmvtiFransaBank.setCleControleCompte(getSpace(2));
//			String codeDes = Account.substring(3, 8);
//			bkmvtiFransaBank.setAgenceDestinatrice(codeDes);
//			bkmvtiFransaBank.setAgenceEmettrice(codeDes);
//			bkmvtiFransaBank.setCodeAgenceSaisie(codeDes);
//			String codeId = Account.substring(3, 8);
//			bkmvtiFransaBank.setCodeID("S" + codeId);
//		}
//
//		else if (lengAccount >= 6 && lengAccount < 10) {
//			bkmvtiFransaBank.setNumCompte(Account + getZero(10 - (Account.length())));
//			bkmvtiFransaBank.setChapitreComptable(Account.substring(0, 6) + getZero(6 - Account.length()));
//
//			bkmvtiFransaBank.setCleControleCompte(getSpace(2));
//			bkmvtiFransaBank.setAgenceDestinatrice(op.getCodeAgence());
//			bkmvtiFransaBank.setAgenceEmettrice(op.getCodeAgence());
//			bkmvtiFransaBank.setCodeAgenceSaisie(op.getCodeAgence());
//			bkmvtiFransaBank.setCodeID("S" + op.getCodeAgence());
//		}
//
//		else if (lengAccount < 6) {
//			bkmvtiFransaBank.setNumCompte(Account + getZero(10 - (Account.length())));
//			bkmvtiFransaBank.setChapitreComptable(Account + getZero(6 - Account.length()));
//			bkmvtiFransaBank.setCleControleCompte(getSpace(2));
//			bkmvtiFransaBank.setAgenceDestinatrice(op.getCodeAgence());
//			bkmvtiFransaBank.setAgenceEmettrice(op.getCodeAgence());
//			bkmvtiFransaBank.setCodeAgenceSaisie(op.getCodeAgence());
//			bkmvtiFransaBank.setCodeID("S" + op.getCodeAgence());
//		}
//
//		else if (lengAccount == 10) {
//			bkmvtiFransaBank.setNumCompte(Account);
//			bkmvtiFransaBank.setChapitreComptable(Account.substring(0, 6));
//			bkmvtiFransaBank.setCleControleCompte(getSpace(2));
//			bkmvtiFransaBank.setAgenceDestinatrice(op.getCodeAgence());
//			bkmvtiFransaBank.setAgenceEmettrice(op.getCodeAgence());
//			bkmvtiFransaBank.setCodeAgenceSaisie(op.getCodeAgence());
//			bkmvtiFransaBank.setCodeID("S" + op.getCodeAgence());
//		}
//
//		else {
//			bkmvtiFransaBank.setNumCompte(Account.substring(0, 10));
//			bkmvtiFransaBank.setChapitreComptable(Account.substring(0, 6));
//			bkmvtiFransaBank.setCleControleCompte(getSpace(2));
//			bkmvtiFransaBank.setAgenceEmettrice(op.getCodeAgence());
//			bkmvtiFransaBank.setCodeAgenceSaisie(op.getCodeAgence());
//			bkmvtiFransaBank.setCodeID("S" + op.getCodeAgence());
//		}
//
//		return bkmvtiFransaBank;
//	}
//
//	public BkmvtiFransaBank TestCodeAgence(String codeAgence, MvbkConf mvk, DayOperationFransaBank op,
//			BkmvtiFransaBank bkmvtiFransaBank) {
//		if (mvk.getCodeAgence() != null) {
//			if (mvk.getCodeAgence().equals("00002")) {
//				bkmvtiFransaBank.setAgence(mvk.getCodeAgence());
//			}
//		} else {
//
//			bkmvtiFransaBank.setAgence(op.getCodeAgence());
//		}
//		return bkmvtiFransaBank;
//	}

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
		switch (paramUAP50) {
		case "1":
			writeUAP050();
			break;
		default:
			System.out.println("C tt ");
			break;
		}
		System.out.println("Param050" + paramUAP50);
	}
}
