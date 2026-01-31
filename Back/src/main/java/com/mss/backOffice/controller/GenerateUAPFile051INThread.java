package com.mss.backOffice.controller;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import com.mss.unified.entities.UAP051IN;
import com.mss.unified.entities.UAP051IN;
import com.mss.unified.repositories.BkmvtiFransaBankRepository;
import com.mss.unified.repositories.CodeBankBCRepository;
import com.mss.unified.repositories.DayOperationFransaBankRepository;
import com.mss.unified.repositories.MvbkSettlementRepository;
import com.mss.unified.repositories.UAP051INFransaBankRepository;
import com.mss.unified.repositories.UAP051INFransaBankRepository;

public strictfp  class GenerateUAPFile051INThread implements Runnable {

	String fileIntegration;
	@Autowired
	DayOperationFransaBankRepository dayOperationFransaBankRepository;
	@Autowired
	CodeBankBCRepository codeBankBCRepository;

	@Autowired
	UAP051INFransaBankRepository UAP051INFransaBankRepository;

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

	public String paramUAP51;

	public GenerateUAPFile051INThread(DayOperationFransaBankRepository dayOperationFransaBankRepository,
			String paramUAP51, UAP051INFransaBankRepository UAP051INFransaBankRepository,
			MvbkSettlementRepository mvbkSettlementRepository, BkmvtiFransaBankRepository bkmvtiFransaBankRepository,
			String fileIntegration) {
		super();
		this.dayOperationFransaBankRepository = dayOperationFransaBankRepository;
		this.paramUAP51 = paramUAP51;
		this.UAP051INFransaBankRepository = UAP051INFransaBankRepository;
		this.mvbkSettlementRepository = mvbkSettlementRepository;
		this.bkmvtiFransaBankRepository = bkmvtiFransaBankRepository;
		this.fileIntegration = fileIntegration;
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

	public void writeUAP051IN() {

		generationRembourssementInternetUAPIN();
		generationRembourssementTpeIN();

//			  generationAchatInternetAlgeriePosteUAPIN();

	}

	public void generationRembourssementInternetUAPIN() {

		List<Object[]> dayOperationsRembourssementInternet = dayOperationFransaBankRepository
				.getListRembourssementInternetUAPIN();
		List<UAP051IN> uAPFransaBanksRembourssementInternet = new ArrayList<UAP051IN>();
		// List<Object[]> dayOperationAmountInternetRembourssement =
		// dayOperationFransaBankRepository.getListAmountRembourssementInternetIN();
		for (Object o[] : dayOperationsRembourssementInternet) {
			UAP051IN UAP051IN = new UAP051IN();

			UAP051IN.setTypeTransaction("051");
			String typePaiement = getTypePaiement(String.valueOf(o[4]));
			UAP051IN.setTypePaiement(typePaiement);
			UAP051IN.setNumTransaction(String.valueOf(o[16]) + getSpace(12 - String.valueOf(o[16]).length()));

			String RefArchivage = UAP051IN.getNumTransaction()
					+ String.valueOf(o[0]).substring(String.valueOf(o[0]).length() - 6, String.valueOf(o[0]).length());
			UAP051IN.setReferenceArchivage(RefArchivage + getSpace(18 - String.valueOf(RefArchivage).length()));
			UAP051IN.setIndicateur("1");
			UAP051IN.setNumRIBcommercant(String.valueOf(o[13]) + getSpace(20 - String.valueOf(o[13]).length()));
			UAP051IN.setPrefixeIBAN("    ");
			UAP051IN.setCodeBankAcquereur(String.valueOf(o[5]) + getSpace(3 - String.valueOf(o[5]).length()));

			UAP051IN.setLibelleCommercant(String.valueOf(o[14]) + getSpace(50 - String.valueOf(o[14]).length()));
			UAP051IN.setAdresseCommercant(getSpace(70));
			UAP051IN.setTelephoneCommercant(getStar(10));
			UAP051IN.setNumContratAccepteur(getStar(15));
			UAP051IN.setCodeActivite(getStar(6));
			UAP051IN.setReserved("000");
			UAP051IN.setCodeBin(String.valueOf(o[1]).substring(0, 6) + getSpace(6 - String.valueOf(o[1]).length()));
			UAP051IN.setTypeCarte(String.valueOf(o[1]).substring(6, 8) + getSpace(2 - String.valueOf(o[1]).length()));
			UAP051IN.setNumSeq(String.valueOf(o[1]).substring(8, 14) + getSpace(6 - String.valueOf(o[1]).length()));
			UAP051IN.setNumOrdre(String.valueOf(o[1]).substring(14, 15) + getSpace(1 - String.valueOf(o[1]).length()));
			int length = String.valueOf(o[1]).length();
			String numCarte = String.valueOf(o[1]).substring(length - 1, length);
			UAP051IN.setCle(numCarte);
			String montatnAcompenser = String.valueOf(o[12]).replace(".0", "");

			UAP051IN.setMontantAComponser(getZero(15 - montatnAcompenser.length()) + montatnAcompenser);
			UAP051IN.setFileDate(String.valueOf(o[15]) + getSpace(6 - String.valueOf(o[15]).length()));
			String amount = String.valueOf(o[2]).replace(".", "");
			UAP051IN.setMontantRetrait(getZero(15 - amount.length()) + amount);

			UAP051IN.setCodeDebitPorteur(String.valueOf(o[7]) + getSpace(1 - String.valueOf(o[7]).length()));
			UAP051IN.setCodeDebitCommercant(String.valueOf(o[8]) + getSpace(1 - String.valueOf(o[8]).length()));
			String getListRembourssementInternetCommission = dayOperationFransaBankRepository
					.getListRembourssemntInternetCommissionIN(String.valueOf(o[0]), String.valueOf(o[16]),
							String.valueOf(o[1]));
			getListRembourssementInternetCommission=getListRembourssementInternetCommission.replace(".0", "").replace(",0", "");
			UAP051IN.setMontantCommission(getZero(7 - getListRembourssementInternetCommission.length())
					+ String.valueOf(Math.round(Integer.parseInt(getListRembourssementInternetCommission))));

			UAP051IN.setDateTransaction(String.valueOf(o[10]) + getSpace(8 - String.valueOf(o[10]).length()));
			UAP051IN.setHeureTransaction(String.valueOf(o[11]) + getSpace(6 - String.valueOf(o[11]).length()));
			UAP051IN.setIdentifSystem("00SATIMTPE");
			UAP051IN.setIdentifPointRetrait(String.valueOf(o[17]) + getSpace(10 - String.valueOf(o[17]).length()));

			UAP051IN.setModeLectureCarte("2");
			UAP051IN.setMethAuthPorteur("1");

			UAP051IN.setNumAutorisation(String.valueOf(o[0]) + getSpace(6 - String.valueOf(o[0]).length()));
			String dateBefore = String.valueOf(o[10]);
			DateTimeFormatter formater = DateTimeFormatter.ofPattern("yyyyMMdd");
			LocalDate date = LocalDate.parse(dateBefore, formater);

			LocalDate updatedDate = date.plusDays(1);

			String updatedDateString = updatedDate.format(DateTimeFormatter.ofPattern("yyyyMMdd"));

			UAP051IN.setDateDebutValiditeCarte(updatedDateString + getSpace(8 - updatedDateString.length()));
			UAP051IN.setDateFinValiditeCarte(updatedDateString + getSpace(8 - updatedDateString.length()));
			UAP051IN.setCryptogramData("0");
			UAP051IN.setAtc("00");
			UAP051IN.setTvr("00000");
			UAP051IN.setDateRemise(String.valueOf(o[10]) + getSpace(8 - String.valueOf(o[10]).length()));
			UAP051IN.setFileIntegrationDate(fileIntegration);
			UAP051IN.setPieceComptableBKM(String.valueOf(o[18]));
			UAP051IN.setCodeAgence(String.valueOf(o[3]) + getSpace(5 - String.valueOf(o[3]).length()));
			UAP051IN.setMontantTransaction(Integer.valueOf(amount));
			UAP051IN.setMontantCommissionFSBKTTC(Math.round(Integer.valueOf(UAP051IN.getMontantCommission())));
			uAPFransaBanksRembourssementInternet.add(UAP051IN);
		}

		UAP051INFransaBankRepository.saveAll(uAPFransaBanksRembourssementInternet);

	}

	public void generationRembourssementTpeIN() {

		List<Object[]> dayOperationsRembourssement = dayOperationFransaBankRepository.getListRembourssementTpeUAPIN();
		List<UAP051IN> uAPFransaBanksRembourssementTPE = new ArrayList<UAP051IN>();
		for (Object o[] : dayOperationsRembourssement) {
			UAP051IN UAP051IN = new UAP051IN();

			UAP051IN.setTypeTransaction("051");
			UAP051IN.setTypePaiement("01");
			UAP051IN.setNumTransaction(String.valueOf(o[16]) + getSpace(12 - String.valueOf(o[16]).length()));

			String RefArchivage = UAP051IN.getNumTransaction()
					+ String.valueOf(o[0]).substring(String.valueOf(o[0]).length() - 6, String.valueOf(o[0]).length());
			UAP051IN.setReferenceArchivage(RefArchivage + getSpace(18 - String.valueOf(RefArchivage).length()));
			UAP051IN.setIndicateur("1");
			UAP051IN.setCodeBankAcquereur(String.valueOf(o[5]) + getSpace(3 - String.valueOf(o[5]).length()));

			UAP051IN.setNumRIBcommercant(String.valueOf(o[13]) + getSpace(20 - String.valueOf(o[13]).length()));
			UAP051IN.setPrefixeIBAN("    ");
			UAP051IN.setLibelleCommercant(String.valueOf(o[14]) + getSpace(50 - String.valueOf(o[14]).length()));
			UAP051IN.setAdresseCommercant(getSpace(70));
			UAP051IN.setTelephoneCommercant(getStar(10));
			UAP051IN.setNumContratAccepteur(getStar(15));
			UAP051IN.setCodeActivite(getStar(15));
			UAP051IN.setReserved("000");
			UAP051IN.setCodeBin(String.valueOf(o[1]).substring(0, 6) + getSpace(6 - String.valueOf(o[1]).length()));
			UAP051IN.setTypeCarte(String.valueOf(o[1]).substring(6, 8) + getSpace(2 - String.valueOf(o[1]).length()));
			UAP051IN.setNumSeq(String.valueOf(o[1]).substring(8, 14) + getSpace(6 - String.valueOf(o[1]).length()));
			UAP051IN.setNumOrdre(String.valueOf(o[1]).substring(14, 15) + getSpace(1 - String.valueOf(o[1]).length()));
			int length = String.valueOf(o[1]).length();
			String numCarte = String.valueOf(o[1]).substring(length - 1, length);
			UAP051IN.setCle(numCarte);
			String montatnAcompenser = String.valueOf(o[12]).replace(".0", "");

			UAP051IN.setMontantAComponser(getZero(15 - montatnAcompenser.length()) + montatnAcompenser);
			UAP051IN.setFileDate(String.valueOf(o[15]) + getSpace(6 - String.valueOf(o[15]).length()));

			UAP051IN.setFileDate(String.valueOf(o[15]) + getSpace(6 - String.valueOf(o[15]).length()));
			String amount = String.valueOf(o[2]).replace(".", "");
			UAP051IN.setMontantRetrait(getZero(15 - amount.length()) + amount);

			UAP051IN.setCodeDebitPorteur(String.valueOf(o[7]) + getSpace(1 - String.valueOf(o[7]).length()));
			UAP051IN.setCodeDebitCommercant(String.valueOf(o[8]) + getSpace(1 - String.valueOf(o[8]).length()));
			String getListRembourssementCommission = dayOperationFransaBankRepository
					.getListRembourssemntTpeCommissionIN(String.valueOf(o[0]), String.valueOf(o[16]),
							String.valueOf(o[1]));
			getListRembourssementCommission=getListRembourssementCommission.replace(".0", "").replace(",0", "");
			UAP051IN.setMontantCommission( getZero(7 - getListRembourssementCommission.length()) 
					+ String.valueOf(Math.round(Integer.parseInt(getListRembourssementCommission ))));

			UAP051IN.setNumTransaction(String.valueOf(o[16]) + getSpace(12 - String.valueOf(o[16]).length()));
			UAP051IN.setDateTransaction(String.valueOf(o[10]) + getSpace(8 - String.valueOf(o[10]).length()));
			UAP051IN.setHeureTransaction(String.valueOf(o[11]) + getSpace(6 - String.valueOf(o[11]).length()));
			UAP051IN.setIdentifSystem("00SATIMTPE");
			UAP051IN.setIdentifPointRetrait(String.valueOf(o[17]) + getSpace(10 - String.valueOf(o[17]).length()));

			UAP051IN.setModeLectureCarte("2");
			UAP051IN.setMethAuthPorteur("1");

			UAP051IN.setNumAutorisation(String.valueOf(o[0]) + getSpace(6 - String.valueOf(o[0]).length()));
			String dateBefore = String.valueOf(o[10]);
			DateTimeFormatter formater = DateTimeFormatter.ofPattern("yyyyMMdd");
			LocalDate date = LocalDate.parse(dateBefore, formater);

			LocalDate updatedDate = date.plusDays(1);

			String updatedDateString = updatedDate.format(DateTimeFormatter.ofPattern("yyyyMMdd"));

			UAP051IN.setDateDebutValiditeCarte(updatedDateString + getSpace(8 - updatedDateString.length()));
			UAP051IN.setDateFinValiditeCarte(updatedDateString + getSpace(8 - updatedDateString.length()));
			UAP051IN.setCryptogramData("0");
			UAP051IN.setAtc("00");
			UAP051IN.setTvr("00000");
			UAP051IN.setDateRemise(String.valueOf(o[10]) + getSpace(8 - String.valueOf(o[10]).length()));
			UAP051IN.setFileIntegrationDate(fileIntegration);
			UAP051IN.setPieceComptableBKM(String.valueOf(o[18]));
			UAP051IN.setCodeAgence(String.valueOf(o[3]) + getSpace(5 - String.valueOf(o[3]).length()));
			UAP051IN.setMontantTransaction(Integer.valueOf(amount));
			UAP051IN.setMontantCommissionFSBKTTC((int) Math.round(Double.parseDouble(UAP051IN.getMontantCommission())));
			uAPFransaBanksRembourssementTPE.add(UAP051IN);

		}
		UAP051INFransaBankRepository.saveAll(uAPFransaBanksRembourssementTPE);

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
		switch (paramUAP51) {
		case "1":
			writeUAP051IN();
			break;
		default:
			System.out.println("C tt ");
			break;
		}
		System.out.println("Param051" + paramUAP51);
	}

}
