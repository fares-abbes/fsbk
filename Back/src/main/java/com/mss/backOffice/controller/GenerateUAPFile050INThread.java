package com.mss.backOffice.controller;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import com.mss.unified.entities.UAP050IN;
import com.mss.unified.repositories.BkmvtiFransaBankRepository;
import com.mss.unified.repositories.CodeBankBCRepository;
import com.mss.unified.repositories.DayOperationFransaBankRepository;
import com.mss.unified.repositories.MvbkSettlementRepository;

import com.mss.unified.repositories.UAP050INFransaBankRepository;

public strictfp  class GenerateUAPFile050INThread implements Runnable {
	String fileIntegration;
	@Autowired
	DayOperationFransaBankRepository dayOperationFransaBankRepository;
	@Autowired
	CodeBankBCRepository codeBankBCRepository;

	@Autowired
	UAP050INFransaBankRepository uAP050INFransaBankRepository;

	@Autowired
	MvbkSettlementRepository mvbkSettlementRepository;
	@Autowired
	BkmvtiFransaBankRepository bkmvtiFransaBankRepository;
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

	public String paramUAP50;

	public GenerateUAPFile050INThread(DayOperationFransaBankRepository dayOperationFransaBankRepository,
			String paramUAP50, UAP050INFransaBankRepository uAP050INFransaBankRepository,
			MvbkSettlementRepository mvbkSettlementRepository, BkmvtiFransaBankRepository bkmvtiFransaBankRepository,
			String fileIntegration) {
		super();
		this.dayOperationFransaBankRepository = dayOperationFransaBankRepository;
		this.paramUAP50 = paramUAP50;
		this.uAP050INFransaBankRepository = uAP050INFransaBankRepository;
		this.mvbkSettlementRepository = mvbkSettlementRepository;
		this.bkmvtiFransaBankRepository = bkmvtiFransaBankRepository;
		this.fileIntegration = fileIntegration;
	}

	public void generationPaiementTpeIN() {

		List<Object[]> dayOperations = dayOperationFransaBankRepository.getListPaiementSurTpeUAPIN();
		List<UAP050IN> uAPFransaBanks = new ArrayList<UAP050IN>();

		for (Object o[] : dayOperations) {
			UAP050IN UAPFransaBank = new UAP050IN();
			UAPFransaBank.setTypeTransaction("050");
			UAPFransaBank.setTypePaiement("01");
			UAPFransaBank.setNumTransaction(String.valueOf(o[16]) + getSpace(12 - String.valueOf(o[16]).length()));

			String RefArchivage = 	UAPFransaBank.getNumTransaction()
					+ String.valueOf(o[0]).substring(String.valueOf(o[0]).length() - 6, String.valueOf(o[0]).length());
			UAPFransaBank.setReferenceArchivage(RefArchivage + getSpace(18 - String.valueOf(RefArchivage).length()));
			UAPFransaBank.setIndicateur("1");
			UAPFransaBank.setCodeBankAcquereur(String.valueOf(o[5]) + getSpace(3 - String.valueOf(o[5]).length()));
			UAPFransaBank.setNumRIBcommercant(String.valueOf(o[13]) + getSpace(20 - String.valueOf(o[13]).length()));
			UAPFransaBank.setPrefixeIBAN("    ");
			UAPFransaBank.setLibelleCommercant(String.valueOf(o[14]) + getSpace(50 - String.valueOf(o[14]).length()));
			UAPFransaBank.setAdresseCommercant(getSpace(70));
			UAPFransaBank.setTelephoneCommercant(getSpace(10));
			UAPFransaBank.setNumContratAccepteur(getStar(15));
			UAPFransaBank.setCodeActivite(getStar(6));
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
			UAPFransaBank.setFileDate(String.valueOf(o[15]) + getSpace(6 - String.valueOf(o[15]).length()));

			String amount = String.valueOf(o[2]).replace(".", "");
			UAPFransaBank.setMontantRetrait(getZero(15 - amount.length()) + amount);

			UAPFransaBank.setCodeDebitPorteur(String.valueOf(o[7]) + getSpace(1 - String.valueOf(o[7]).length()));
			UAPFransaBank.setCodeDebitCommercant(String.valueOf(o[8]) + getSpace(1 - String.valueOf(o[8]).length()));

			BigDecimal combg = new BigDecimal(String.valueOf(o[12]));
//			BigDecimal multip = new BigDecimal(100);

			int commissions = combg.intValue();
			UAPFransaBank.setMontantCommission(getZero(7 - String.valueOf(Math.round(commissions)).length())
					+ String.valueOf(Math.round(commissions)));

			UAPFransaBank.setMontantCommission(getZero(7 - String.valueOf(Math.round(commissions)).length())
					+ String.valueOf(Math.round(commissions)));
			int montant_compensee = (int) (Integer.parseInt(amount) - Math.round(commissions));
			UAPFransaBank.setMontantAComponser(
					getZero(15 - String.valueOf(montant_compensee).length()) + String.valueOf(montant_compensee));
			UAPFransaBank.setDateTransaction(String.valueOf(o[10]) + getSpace(8 - String.valueOf(o[10]).length()));
			UAPFransaBank.setHeureTransaction(String.valueOf(o[11]) + getSpace(6 - String.valueOf(o[11]).length()));
			UAPFransaBank.setIdentifSystem("00SATIMTPE");
			UAPFransaBank.setIdentifPointRetrait(String.valueOf(o[17]) + getSpace(10 - String.valueOf(o[17]).length()));
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
			UAPFransaBank.setPieceComptableBKM(String.valueOf(o[18]));
			UAPFransaBank.setCodeAgence(String.valueOf(o[3]) + getSpace(5 - String.valueOf(o[3]).length()));
			UAPFransaBank.setMontantTransaction(Integer.valueOf(String.valueOf(o[2]).replace(".", "")));
			UAPFransaBank.setMontantCommissionTTC(commissions);
			uAPFransaBanks.add(UAPFransaBank);
		}

		uAP050INFransaBankRepository.saveAll(uAPFransaBanks);
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

	public void generationAchatInternetUAPIN() {

		List<Object[]> dayOperationsAchatInternet = dayOperationFransaBankRepository.getListAchatInternetUAPIN();
		List<UAP050IN> uAPFransaBanksAchatInternet = new ArrayList<UAP050IN>();

		// List<Object[]> dayOperationAmountInternetAchat =
		// dayOperationFransaBankRepository.getListAmountAchatInternetIN();
		for (Object o[] : dayOperationsAchatInternet) {
			UAP050IN UAP050IN = new UAP050IN();

			UAP050IN.setTypeTransaction("050");
			String typePaiement = getTypePaiement(String.valueOf(o[4]));
			UAP050IN.setNumTransaction(String.valueOf(o[16]) + getSpace(12 - String.valueOf(o[16]).length()));

			UAP050IN.setTypePaiement(typePaiement);
			String RefArchivage = UAP050IN.getNumTransaction()
					+ String.valueOf(o[0]).substring(String.valueOf(o[0]).length() - 6, String.valueOf(o[0]).length());
			UAP050IN.setReferenceArchivage(RefArchivage + getSpace(18 - String.valueOf(RefArchivage).length()));
			UAP050IN.setIndicateur("1");
			UAP050IN.setNumRIBcommercant(String.valueOf(o[13]) + getSpace(20 - String.valueOf(o[13]).length()));
			UAP050IN.setPrefixeIBAN("    ");
			UAP050IN.setLibelleCommercant(String.valueOf(o[14]) + getSpace(50 - String.valueOf(o[14]).length()));
			UAP050IN.setAdresseCommercant(getSpace(70));
			UAP050IN.setTelephoneCommercant(getStar(10));
			UAP050IN.setNumContratAccepteur(getStar(15));
			UAP050IN.setCodeActivite(getStar(6));
			UAP050IN.setCodeBankAcquereur(String.valueOf(o[5]) + getSpace(3 - String.valueOf(o[5]).length()));
			UAP050IN.setReserved("000");
			UAP050IN.setCodeBin(String.valueOf(o[1]).substring(0, 6) + getSpace(6 - String.valueOf(o[1]).length()));
			UAP050IN.setTypeCarte(String.valueOf(o[1]).substring(6, 8) + getSpace(2 - String.valueOf(o[1]).length()));
			UAP050IN.setNumSeq(String.valueOf(o[1]).substring(8, 14) + getSpace(6 - String.valueOf(o[1]).length()));
			UAP050IN.setNumOrdre(String.valueOf(o[1]).substring(14, 15) + getSpace(1 - String.valueOf(o[1]).length()));
			int length = String.valueOf(o[1]).length();
			String numCarte = String.valueOf(o[1]).substring(length - 1, length);
			UAP050IN.setCle(numCarte);

			UAP050IN.setFileDate(String.valueOf(o[15]) + getSpace(6 - String.valueOf(o[15]).length()));

			String amount = String.valueOf(o[2]).replace(".", "");
			UAP050IN.setMontantRetrait(getZero(15 - amount.length()) + amount);
			/*
			 * for (Object d[] : dayOperationAmountInternetAchat) { if(d[0].equals(o[0])){
			 * UAP050IN.setMontantRetrait(getZero(15 -
			 * String.valueOf(d[1]).length())+String.valueOf(d[1])); }
			 * 
			 * 
			 * }
			 */

			UAP050IN.setCodeDebitPorteur(String.valueOf(o[7]) + getSpace(1 - String.valueOf(o[7]).length()));
			UAP050IN.setCodeDebitCommercant(String.valueOf(o[8]) + getSpace(1 - String.valueOf(o[8]).length()));
			// String getListAchatInternetCommission =
			// dayOperationFransaBankRepository.getListAchatInternetCommissionIN(String.valueOf(o[0]),String.valueOf(o[16]),String.valueOf(o[1]));

			Integer commissions = Integer.parseInt(String.valueOf(o[12]).replace(".0", ""));
			UAP050IN.setMontantCommission(getZero(7 - String.valueOf(Math.round(commissions)).length())
					+ String.valueOf(Math.round(commissions)));
			int montant_compensee = (int) (Integer.parseInt(amount) - Math.round(commissions));
			UAP050IN.setMontantAComponser(
					getZero(15 - String.valueOf(montant_compensee).length()) + String.valueOf(montant_compensee));
			UAP050IN.setMontantCommission(getZero(7 - String.valueOf(Math.round(commissions)).length())
					+ String.valueOf(Math.round(commissions)));

			/*
			 * for (Object c[] : getListAchatInternetCommission) { if(c[0].equals(o[0])) {
			 * 
			 * } }
			 */
			UAP050IN.setDateTransaction(String.valueOf(o[10]) + getSpace(8 - String.valueOf(o[10]).length()));
			UAP050IN.setHeureTransaction(String.valueOf(o[11]) + getSpace(6 - String.valueOf(o[11]).length()));
			UAP050IN.setIdentifSystem("00SATIMTPE");
			UAP050IN.setIdentifPointRetrait(String.valueOf(o[17]) + getSpace(10 - String.valueOf(o[17]).length()));

			UAP050IN.setModeLectureCarte("2");
			UAP050IN.setMethAuthPorteur("1");

			UAP050IN.setNumAutorisation(String.valueOf(o[0]) + getSpace(6 - String.valueOf(o[0]).length()));
			String dateBefore = String.valueOf(o[10]);
			DateTimeFormatter formater = DateTimeFormatter.ofPattern("yyyyMMdd");
			LocalDate date = LocalDate.parse(dateBefore, formater);

			LocalDate updatedDate = date.plusDays(1);

			String updatedDateString = updatedDate.format(DateTimeFormatter.ofPattern("yyyyMMdd"));

			UAP050IN.setDateDebutValiditeCarte(updatedDateString + getSpace(8 - updatedDateString.length()));
			UAP050IN.setDateFinValiditeCarte(updatedDateString);
			UAP050IN.setCryptogramData("0");
			UAP050IN.setAtc("00");
			UAP050IN.setTvr("00000");
			UAP050IN.setDateRemise(String.valueOf(o[10]) + getSpace(8 - String.valueOf(o[10]).length()));
			UAP050IN.setFileIntegrationDate(fileIntegration);
			UAP050IN.setPieceComptableBKM(String.valueOf(o[18]));
			UAP050IN.setCodeAgence(String.valueOf(o[3]) + getSpace(5 - String.valueOf(o[3]).length()));
			UAP050IN.setTagPaiement("1");
			UAP050IN.setMontantTransaction(Integer.valueOf(String.valueOf(o[2]).replace(".", "")));
			UAP050IN.setMontantCommissionTTC(Integer.valueOf(UAP050IN.getMontantCommission()));
			uAPFransaBanksAchatInternet.add(UAP050IN);
		}

		uAP050INFransaBankRepository.saveAll(uAPFransaBanksAchatInternet);

	}

	public void generationAchatInternetExceptionnelMerchantUAPIN() {

		List<Object[]> dayOperationsAchatInternet = dayOperationFransaBankRepository
				.getListAchatInternetExceptionnelMerchantUAPIN();
		List<UAP050IN> uAPFransaBanksAchatInternet = new ArrayList<UAP050IN>();
		for (Object o[] : dayOperationsAchatInternet) {
			UAP050IN UAP050IN = new UAP050IN();
			UAP050IN.setNumTransaction(String.valueOf(o[16]) + getSpace(12 - String.valueOf(o[16]).length()));
			UAP050IN.setTypeTransaction("050");
			UAP050IN.setTypePaiement("03");
			String RefArchivage =UAP050IN.getNumTransaction()
					+ String.valueOf(o[0]).substring(String.valueOf(o[0]).length() - 6, String.valueOf(o[0]).length());
			UAP050IN.setReferenceArchivage(RefArchivage + getSpace(18 - String.valueOf(RefArchivage).length()));
			UAP050IN.setIndicateur("1");
			UAP050IN.setNumRIBcommercant(String.valueOf(o[13]) + getSpace(20 - String.valueOf(o[13]).length()));
			UAP050IN.setPrefixeIBAN("    ");
			UAP050IN.setCodeBankAcquereur(String.valueOf(o[5]) + getSpace(3 - String.valueOf(o[5]).length()));

			UAP050IN.setLibelleCommercant(String.valueOf(o[14]) + getSpace(50 - String.valueOf(o[14]).length()));
			UAP050IN.setAdresseCommercant(getSpace(70));
			UAP050IN.setTelephoneCommercant(getStar(10));
			UAP050IN.setNumContratAccepteur(getStar(15));
			UAP050IN.setCodeActivite(getStar(6));
			UAP050IN.setReserved("000");
			UAP050IN.setCodeBin(String.valueOf(o[1]).substring(0, 6) + getSpace(6 - String.valueOf(o[1]).length()));
			UAP050IN.setTypeCarte(String.valueOf(o[1]).substring(6, 8) + getSpace(2 - String.valueOf(o[1]).length()));
			UAP050IN.setNumSeq(String.valueOf(o[1]).substring(8, 14) + getSpace(6 - String.valueOf(o[1]).length()));
			UAP050IN.setNumOrdre(String.valueOf(o[1]).substring(14, 15) + getSpace(1 - String.valueOf(o[1]).length()));
			int length = String.valueOf(o[1]).length();
			String numCarte = String.valueOf(o[1]).substring(length - 1, length);
			UAP050IN.setCle(numCarte);
			String montatnAcompenser = String.valueOf(o[12]).replace(".0", "");
			UAP050IN.setMontantAComponser(getZero(15 - montatnAcompenser.length()) + montatnAcompenser);
			UAP050IN.setFileDate(String.valueOf(o[15]) + getSpace(6 - String.valueOf(o[15]).length()));
			String amount = String.valueOf(o[2]).replace(".", "");
			UAP050IN.setMontantRetrait(getZero(15 - amount.length()) + amount);
			/*
			 * for (Object d[] : dayOperationAmountInternetAchat) { if(d[0].equals(o[0])){
			 * UAP050IN.setMontantRetrait(getZero(15 -
			 * String.valueOf(d[1]).length())+String.valueOf(d[1])); }
			 * 
			 * 
			 * }
			 */

			UAP050IN.setCodeDebitPorteur(String.valueOf(o[7]) + getSpace(1 - String.valueOf(o[7]).length()));
			UAP050IN.setCodeDebitCommercant(String.valueOf(o[8]) + getSpace(1 - String.valueOf(o[8]).length()));
			String getListAchatInternetCommission = dayOperationFransaBankRepository
					.getListAchatInternetCommissionExceptionnelMerchantIN(String.valueOf(o[0]), String.valueOf(o[16]),
							String.valueOf(o[1]))
					.replace(".0", "");
			UAP050IN.setMontantCommission(
					getZero(7 - getListAchatInternetCommission.length()) + getListAchatInternetCommission);
			/*
			 * for (Object c[] : getListAchatInternetCommission) { if(c[0].equals(o[0])) {
			 * UAP050IN.setMontantCommission(getZero(7 -
			 * String.valueOf(c[1]).length())+String.valueOf(c[1])); } }
			 */
			FileRequest.print(UAP050IN.getMontantCommission(), FileRequest.getLineNumber());

			UAP050IN.setDateTransaction(String.valueOf(o[10]) + getSpace(8 - String.valueOf(o[10]).length()));
			UAP050IN.setHeureTransaction(String.valueOf(o[11]) + getSpace(6 - String.valueOf(o[11]).length()));
			UAP050IN.setIdentifSystem("00SATIMTPE");
			UAP050IN.setIdentifPointRetrait(String.valueOf(o[17]) + getSpace(10 - String.valueOf(o[17]).length()));

			UAP050IN.setModeLectureCarte("2");
			UAP050IN.setMethAuthPorteur("1");

			UAP050IN.setNumAutorisation(String.valueOf(o[0]) + getSpace(6 - String.valueOf(o[0]).length()));
			String dateBefore = String.valueOf(o[10]);
			DateTimeFormatter formater = DateTimeFormatter.ofPattern("yyyyMMdd");
			LocalDate date = LocalDate.parse(dateBefore, formater);

			LocalDate updatedDate = date.plusDays(1);

			String updatedDateString = updatedDate.format(DateTimeFormatter.ofPattern("yyyyMMdd"));

			UAP050IN.setDateDebutValiditeCarte(updatedDateString + getSpace(8 - updatedDateString.length()));
			UAP050IN.setDateFinValiditeCarte(updatedDateString);
			UAP050IN.setCryptogramData("0");
			UAP050IN.setAtc("00");
			UAP050IN.setTvr("00000");
			UAP050IN.setDateRemise(String.valueOf(o[10]) + getSpace(8 - String.valueOf(o[10]).length()));
			UAP050IN.setFileIntegrationDate(fileIntegration);
			UAP050IN.setPieceComptableBKM(String.valueOf(o[18]));
			UAP050IN.setCodeAgence(String.valueOf(o[3]) + getSpace(5 - String.valueOf(o[3]).length()));
			UAP050IN.setTagPaiement("2");
			UAP050IN.setMontantTransaction(Integer.valueOf(String.valueOf(o[2]).replace(".", "")));
			UAP050IN.setMontantCommissionTTC(Integer.valueOf(UAP050IN.getMontantCommission()));
			uAPFransaBanksAchatInternet.add(UAP050IN);
		}

		uAP050INFransaBankRepository.saveAll(uAPFransaBanksAchatInternet);

	}

	public void generationAchatInternetAlgeriePosteUAPIN() {

		List<Object[]> dayOperationsAchatInternet = dayOperationFransaBankRepository
				.getListAchatInternetAlgeriePosteUAPIN();
		List<UAP050IN> uAPFransaBanksAchatInternet = new ArrayList<UAP050IN>();
		// List<Object[]> dayOperationAmountInternetAchat =
		// dayOperationFransaBankRepository.getListAmountAchatInternetAlgeriePosteIN();
		for (Object o[] : dayOperationsAchatInternet) {
			UAP050IN UAP050IN = new UAP050IN();

			UAP050IN.setTypeTransaction("050");
			UAP050IN.setTypePaiement("03");
			UAP050IN.setNumTransaction(String.valueOf(o[16]) + getSpace(12 - String.valueOf(o[16]).length()));

			String RefArchivage = UAP050IN.getNumTransaction()
					+ String.valueOf(o[0]).substring(String.valueOf(o[0]).length() - 6, String.valueOf(o[0]).length());
			UAP050IN.setReferenceArchivage(RefArchivage + getSpace(18 - String.valueOf(RefArchivage).length()));
			UAP050IN.setIndicateur("1");
			UAP050IN.setCodeBankAcquereur(String.valueOf(o[5]) + getSpace(3 - String.valueOf(o[5]).length()));
			UAP050IN.setNumRIBcommercant(String.valueOf(o[13]) + getSpace(20 - String.valueOf(o[13]).length()));
			UAP050IN.setPrefixeIBAN("    ");
			UAP050IN.setLibelleCommercant(String.valueOf(o[14]) + getSpace(50 - String.valueOf(o[14]).length()));
			UAP050IN.setAdresseCommercant(getSpace(70));
			UAP050IN.setTelephoneCommercant(getStar(10));
			UAP050IN.setNumContratAccepteur(getStar(15));
			UAP050IN.setCodeActivite(getStar(6));
			UAP050IN.setReserved("000");
			UAP050IN.setCodeBin(String.valueOf(o[1]).substring(0, 6) + getSpace(6 - String.valueOf(o[1]).length()));
			UAP050IN.setTypeCarte(String.valueOf(o[1]).substring(6, 8) + getSpace(2 - String.valueOf(o[1]).length()));
			UAP050IN.setNumSeq(String.valueOf(o[1]).substring(8, 14) + getSpace(6 - String.valueOf(o[1]).length()));
			UAP050IN.setNumOrdre(String.valueOf(o[1]).substring(14, 15) + getSpace(1 - String.valueOf(o[1]).length()));
			int length = String.valueOf(o[1]).length();
			String numCarte = String.valueOf(o[1]).substring(length - 1, length);
			UAP050IN.setCle(numCarte);
			UAP050IN.setFileDate(String.valueOf(o[15]) + getSpace(6 - String.valueOf(o[15]).length()));

			String amount = String.valueOf(o[2]).replace(".", "");
			UAP050IN.setMontantRetrait(getZero(15 - amount.length()) + amount);
			/*
			 * for (Object d[] : dayOperationAmountInternetAchat) { if(d[0].equals(o[0])){
			 * UAP050IN.setMontantRetrait(getZero(15 -
			 * String.valueOf(d[1]).length())+String.valueOf(d[1])); }
			 * 
			 * 
			 * }
			 */

			UAP050IN.setCodeDebitPorteur(String.valueOf(o[7]) + getSpace(1 - String.valueOf(o[7]).length()));
			UAP050IN.setCodeDebitCommercant(String.valueOf(o[8]) + getSpace(1 - String.valueOf(o[8]).length()));
			String getListAchatInternetCommission = dayOperationFransaBankRepository
					.getListAchatInternetAlgeriePosteCommissionIN(String.valueOf(o[0]), String.valueOf(o[16]),
							String.valueOf(o[1]))
					.replace(".0", "");
			;
			UAP050IN.setMontantCommission(
					getZero(7 - getListAchatInternetCommission.length()) + getListAchatInternetCommission);
			/*
			 * for (Object c[] : getListAchatInternetCommission) { if(c[0].equals(o[0])) {
			 * UAP050IN.setMontantCommission(getZero(7 -
			 * String.valueOf(c[1]).length())+String.valueOf(c[1])); } }
			 */
			UAP050IN.setDateTransaction(String.valueOf(o[10]) + getSpace(8 - String.valueOf(o[10]).length()));
			UAP050IN.setHeureTransaction(String.valueOf(o[11]) + getSpace(6 - String.valueOf(o[11]).length()));
			UAP050IN.setIdentifSystem("00SATIMTPE");
			UAP050IN.setIdentifPointRetrait(String.valueOf(o[17]) + getSpace(10 - String.valueOf(o[17]).length()));

			UAP050IN.setModeLectureCarte("2");
			UAP050IN.setMethAuthPorteur("1");

			UAP050IN.setNumAutorisation(String.valueOf(o[0]) + getSpace(6 - String.valueOf(o[0]).length()));
			String dateBefore = String.valueOf(o[10]);
			DateTimeFormatter formater = DateTimeFormatter.ofPattern("yyyyMMdd");
			LocalDate date = LocalDate.parse(dateBefore, formater);

			LocalDate updatedDate = date.plusDays(1);

			String updatedDateString = updatedDate.format(DateTimeFormatter.ofPattern("yyyyMMdd"));

			UAP050IN.setDateDebutValiditeCarte(updatedDateString + getSpace(8 - updatedDateString.length()));
			UAP050IN.setDateFinValiditeCarte(updatedDateString);
			UAP050IN.setCryptogramData("0");
			UAP050IN.setAtc("00");
			UAP050IN.setTvr("00000");
			UAP050IN.setDateRemise(String.valueOf(o[10]) + getSpace(8 - String.valueOf(o[10]).length()));
			UAP050IN.setFileIntegrationDate(fileIntegration);
			UAP050IN.setPieceComptableBKM(String.valueOf(o[18]));
			UAP050IN.setCodeAgence(String.valueOf(o[3]) + getSpace(5 - String.valueOf(o[3]).length()));
			UAP050IN.setTagPaiement("3");
			UAP050IN.setMontantTransaction(Integer.valueOf(String.valueOf(o[2]).replace(".", "")));
			UAP050IN.setMontantCommissionTTC(Integer.valueOf(UAP050IN.getMontantCommission()));
			int montant_compensee = (int) (Integer.parseInt(UAP050IN.getMontantRetrait()) - Math.round(UAP050IN.getMontantCommissionTTC()));

  			UAP050IN.setMontantAComponser(
					getZero(15 - String.valueOf(montant_compensee).length()) + String.valueOf(montant_compensee));
 
			uAPFransaBanksAchatInternet.add(UAP050IN);
		}

		uAP050INFransaBankRepository.saveAll(uAPFransaBanksAchatInternet);

	}

	public void writeUAP050IN() {

		generationPaiementTpeIN();

		generationAchatInternetUAPIN();
		generationAchatInternetAlgeriePosteUAPIN();
		generationAchatInternetExceptionnelMerchantUAPIN();
	}

	public String getZero(int count)

	{

		String zero = "";

		for (int i = 0; i < count; i++)

			zero += "0";

		return zero;

	}

	public String getStar(int count)

	{

		String star = "";

		for (int i = 0; i < count; i++)

			star += "*";

		return star;

	}
 
	public String getSpace(int count)

	{

		String Space = "";

		for (int i = 0; i < count; i++)

			Space += " ";

		return Space;

	}

	@Override
	public void run() {
		switch (paramUAP50) {
		case "1":
			writeUAP050IN();
			break;
		default:
			System.out.println("C tt ");
			break;
		}
		System.out.println("Param050" + paramUAP50);
	}
}
