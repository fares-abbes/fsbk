package com.mss.backOffice.controller;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import com.mss.unified.entities.UAP040IN;
import com.mss.unified.repositories.BkmvtiFransaBankRepository;
import com.mss.unified.repositories.CodeBankBCRepository;
import com.mss.unified.repositories.DayOperationFransaBankRepository;
import com.mss.unified.repositories.MvbkSettlementRepository;
import com.mss.unified.repositories.UAP040INFransaBankRepository;

public strictfp  class GenerateUAPINFileThread implements Runnable {
	String fileIntegration;
	String name;
	@Autowired
	DayOperationFransaBankRepository dayOperationFransaBankRepository;
	@Autowired
	CodeBankBCRepository codeBankBCRepository;
	@Autowired
	UAP040INFransaBankRepository uAP040INFransaBankRepository;

	public String paramUAP;
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

	public GenerateUAPINFileThread(DayOperationFransaBankRepository dayOperationFransaBankRepository, String paramUAP,
			UAP040INFransaBankRepository uAP040INFransaBankRepository,
			MvbkSettlementRepository mvbkSettlementRepository, BkmvtiFransaBankRepository bkmvtiFransaBankRepository,
			String name, String fileIntegration) {
		super();
		this.dayOperationFransaBankRepository = dayOperationFransaBankRepository;
		this.paramUAP = paramUAP;
		this.uAP040INFransaBankRepository = uAP040INFransaBankRepository;
		this.mvbkSettlementRepository = mvbkSettlementRepository;
		this.bkmvtiFransaBankRepository = bkmvtiFransaBankRepository;
		this.name = name;
		this.fileIntegration = fileIntegration;
	}

	public void writeUAP040IN() {

		writeRetraitIN();
		writeConsultationSoldeIN();
		writeUAP040RetraitAlgeriePosteIN();
	}

	public void writeRetraitIN() {

		List<Object[]> dayOperations = dayOperationFransaBankRepository.getListRetraitUAP40IN();

		List<UAP040IN> UAP040INs = new ArrayList<UAP040IN>();

		for (Object o[] : dayOperations) {

			UAP040IN UAP040IN = new UAP040IN();
			UAP040IN.setNumTransaction(String.valueOf(o[14]) + getSpace(12 - String.valueOf(o[14]).length()));

			String RefArchivage = UAP040IN.getNumTransaction() 
					+ String.valueOf(o[0]).substring(String.valueOf(o[0]).length() - 6, String.valueOf(o[0]).length());
			UAP040IN.setFileDate(String.valueOf(o[13]) + getSpace(6 - String.valueOf(o[13]).length()));
			UAP040IN.setTypeTransaction("040");
			UAP040IN.setReferenceArchivage(RefArchivage + getSpace(18 - String.valueOf(RefArchivage).length()));
			UAP040IN.setCodeBankAcquereur(String.valueOf(o[5]) + getSpace(3 - String.valueOf(o[5]).length()));
			UAP040IN.setCodeAgence(String.valueOf(o[3]) + getSpace(5 - String.valueOf(o[3]).length()));
			UAP040IN.setTypeRetrait("001");
			UAP040IN.setCodeBin(String.valueOf(o[1]).substring(0, 6) + getSpace(6 - String.valueOf(o[1]).length()));
			UAP040IN.setTypeCarteDebit(
					String.valueOf(o[1]).substring(6, 8) + getSpace(2 - String.valueOf(o[1]).length()));
			UAP040IN.setNumSeq(String.valueOf(o[1]).substring(8, 14) + getSpace(6 - String.valueOf(o[1]).length()));
			UAP040IN.setNumOrdre(String.valueOf(o[1]).substring(14, 15) + getSpace(1 - String.valueOf(o[1]).length()));
			int length = String.valueOf(o[1]).length();
			String numCarte = String.valueOf(o[1]).substring(length - 1, length);
			UAP040IN.setCle(numCarte);
			UAP040IN.setFileDate(String.valueOf(o[13]) + getSpace(6 - String.valueOf(o[13]).length()));
			String montatnAcompenser = String.valueOf(o[12]).replace(".0", "");

			int lengthMC = montatnAcompenser.length();
			UAP040IN.setMontantAComponser(getZero(15 - lengthMC) + montatnAcompenser);

			String amount = String.valueOf(o[2]).replace(".", "");

			UAP040IN.setMontantRetrait(getZero(15 - amount.length()) + amount);

			UAP040IN.setCodeDebitCommercant(String.valueOf(o[8]) + getSpace(1 - String.valueOf(o[8]).length()));
			String commissions = dayOperationFransaBankRepository.getListRetraitCommission040IN(String.valueOf(o[0]),
					String.valueOf(o[14]), String.valueOf(o[1]));
			UAP040IN.setMontantCommission(
					getZero(7 - String.valueOf(commissions).length()) + String.valueOf(commissions));

			UAP040IN.setDateTransaction(String.valueOf(o[10]) + getSpace(8 - String.valueOf(o[10]).length()));
			UAP040IN.setHeureTransaction(String.valueOf(o[11]) + getSpace(6 - String.valueOf(o[11]).length()));
			UAP040IN.setIdentifSystem("00FSBKADAB");
			UAP040IN.setIdentifPointRetrait(String.valueOf(o[15]) + getSpace(10 - String.valueOf(o[15]).length()));
			UAP040IN.setModeLectureCarte("2");
			UAP040IN.setMethAuthPorteur("1");
			UAP040IN.setNumAutorisation(String.valueOf(o[0]) + getSpace(6 - String.valueOf(o[0]).length()));
			String dateBefore = String.valueOf(o[10]);
			DateTimeFormatter formater = DateTimeFormatter.ofPattern("yyyyMMdd");
			LocalDate date = LocalDate.parse(dateBefore, formater);

			LocalDate updatedDate = date.plusDays(1);

			String updatedDateString = updatedDate.format(DateTimeFormatter.ofPattern("yyyyMMdd"));

			UAP040IN.setDateDebutValiditeCarte(updatedDateString + getSpace(8 - updatedDateString.length()));
			UAP040IN.setDateFinValiditeCarte(updatedDateString + getSpace(8 - updatedDateString.length()));
			UAP040IN.setCryptogramData("0");
			UAP040IN.setAtc("00");
			UAP040IN.setTvr("00000");
			UAP040IN.setFileIntegrationDate(fileIntegration);
			UAP040IN.setPieceComptableBKM(String.valueOf(o[16]));
			UAP040IN.setTagRetrait("1");
			UAP040IN.setMontantTransaction(Integer.valueOf(String.valueOf(o[2]).replace(".", "")));
			UAP040IN.setMontantCommissionTTC(Integer.valueOf(UAP040IN.getMontantCommission()));
			UAP040INs.add(UAP040IN);

		}

		uAP040INFransaBankRepository.saveAll(UAP040INs);
	}

	public void writeConsultationSoldeIN() {

		List<Object[]> dayOperationsConsultationSolde = dayOperationFransaBankRepository
				.getListConsultationSoldeUAP040IN();
		List<UAP040IN> UAP040INsConsultationSolde = new ArrayList<UAP040IN>();

		for (Object o[] : dayOperationsConsultationSolde) {
			UAP040IN UAP040IN = new UAP040IN();
			UAP040IN.setNumTransaction(String.valueOf(o[14]) + getSpace(12 - String.valueOf(o[14]).length()));

			String RefArchivage =  UAP040IN.getNumTransaction() 
					+ String.valueOf(o[0]).substring(String.valueOf(o[0]).length() - 6, String.valueOf(o[0]).length());
			UAP040IN.setTypeTransaction("040");
			UAP040IN.setReferenceArchivage(RefArchivage + getSpace(18 - String.valueOf(RefArchivage).length()));
			UAP040IN.setCodeBankAcquereur(String.valueOf(o[5]) + getSpace(3 - String.valueOf(o[5]).length()));
			UAP040IN.setCodeAgence(String.valueOf(o[3]) + getSpace(5 - String.valueOf(o[3]).length()));
			UAP040IN.setTypeRetrait("002");
			UAP040IN.setCodeBin(String.valueOf(o[1]).substring(0, 6) + getSpace(6 - String.valueOf(o[1]).length()));
			UAP040IN.setTypeCarteDebit(
					String.valueOf(o[1]).substring(6, 8) + getSpace(2 - String.valueOf(o[1]).length()));
			UAP040IN.setNumSeq(String.valueOf(o[1]).substring(8, 14) + getSpace(6 - String.valueOf(o[1]).length()));
			UAP040IN.setNumOrdre(String.valueOf(o[1]).substring(14, 15) + getSpace(1 - String.valueOf(o[1]).length()));
			int length = String.valueOf(o[1]).length();
			String numCarte = String.valueOf(o[1]).substring(length - 1, length);
			UAP040IN.setCle(numCarte);
			UAP040IN.setFileDate(String.valueOf(o[13]) + getSpace(6 - String.valueOf(o[13]).length()));

			UAP040IN.setMontantRetrait("000000000000000");

			UAP040IN.setCodeDebitCommercant(String.valueOf(o[8]) + getSpace(1 - String.valueOf(o[8]).length()));
			BigDecimal combg = new BigDecimal(String.valueOf(o[12]));
//BigDecimal multip=new BigDecimal(100 );

			int commissions = combg.intValue();
			UAP040IN.setMontantCommission(getZero(7 - String.valueOf(Math.round(commissions)).length())
					+ String.valueOf(Math.round(commissions)));

			UAP040IN.setMontantAComponser(getZero(15 - String.valueOf(Math.round(commissions)).length())
					+ String.valueOf(Math.round(commissions)));

			UAP040IN.setDateTransaction(String.valueOf(o[10]) + getSpace(8 - String.valueOf(o[10]).length()));
			UAP040IN.setHeureTransaction(String.valueOf(o[11]) + getSpace(6 - String.valueOf(o[11]).length()));
			UAP040IN.setIdentifSystem("00SATIMDAB");
			UAP040IN.setIdentifPointRetrait(String.valueOf(o[15]) + getSpace(10 - String.valueOf(o[15]).length()));
			UAP040IN.setModeLectureCarte("2");
			UAP040IN.setMethAuthPorteur("1");
			UAP040IN.setNumAutorisation(String.valueOf(o[0]) + getSpace(6 - String.valueOf(o[0]).length()));
			String dateBefore = String.valueOf(o[10]);
			DateTimeFormatter formater = DateTimeFormatter.ofPattern("yyyyMMdd");
			LocalDate date = LocalDate.parse(dateBefore, formater);

			LocalDate updatedDate = date.plusDays(1);

			String updatedDateString = updatedDate.format(DateTimeFormatter.ofPattern("yyyyMMdd"));

			UAP040IN.setDateDebutValiditeCarte(updatedDateString + getSpace(8 - updatedDateString.length()));
			UAP040IN.setDateFinValiditeCarte(updatedDateString + getSpace(8 - updatedDateString.length()));
			UAP040IN.setCryptogramData("0");
			UAP040IN.setAtc("00");
			UAP040IN.setTvr("00000");
			UAP040IN.setFileIntegrationDate(fileIntegration);
			UAP040IN.setPieceComptableBKM(String.valueOf(o[16]));
			UAP040IN.setMontantTransaction(Integer.valueOf(0));
			UAP040IN.setMontantCommissionTTC(commissions);
			UAP040INsConsultationSolde.add(UAP040IN);

		}
		uAP040INFransaBankRepository.saveAll(UAP040INsConsultationSolde);

	}

	public void writeUAP040RetraitAlgeriePosteIN() {

		List<Object[]> dayOperationsAlgeriePoste = dayOperationFransaBankRepository
				.getListRetraitUAPAlgeriePoste040IN();
		List<UAP040IN> UAP040INsAlgeriePoste = new ArrayList<UAP040IN>();
		for (Object o[] : dayOperationsAlgeriePoste) {
			UAP040IN UAP040IN = new UAP040IN();
			UAP040IN.setNumTransaction(String.valueOf(o[14]) + getSpace(12 - String.valueOf(o[14]).length()));

			String RefArchivage =  UAP040IN.getNumTransaction() 
					+ String.valueOf(o[0]).substring(String.valueOf(o[0]).length() - 6, String.valueOf(o[0]).length());
			UAP040IN.setTypeTransaction("040");
			UAP040IN.setReferenceArchivage(RefArchivage + getSpace(18 - String.valueOf(RefArchivage).length()));
			UAP040IN.setCodeBankAcquereur(String.valueOf(o[5]) + getSpace(3 - String.valueOf(o[5]).length()));
			UAP040IN.setCodeAgence(String.valueOf(o[3]) + getSpace(5 - String.valueOf(o[3]).length()));
			UAP040IN.setTypeRetrait("001");
			UAP040IN.setCodeBin(String.valueOf(o[1]).substring(0, 6) + getSpace(6 - String.valueOf(o[1]).length()));
			UAP040IN.setTypeCarteDebit(
					String.valueOf(o[1]).substring(6, 8) + getSpace(2 - String.valueOf(o[1]).length()));
			UAP040IN.setNumSeq(String.valueOf(o[1]).substring(8, 14) + getSpace(6 - String.valueOf(o[1]).length()));
			UAP040IN.setNumOrdre(String.valueOf(o[1]).substring(14, 15) + getSpace(1 - String.valueOf(o[1]).length()));
			int length = String.valueOf(o[1]).length();
			String numCarte = String.valueOf(o[1]).substring(length - 1, length);
			UAP040IN.setCle(numCarte);

			UAP040IN.setFileDate(String.valueOf(o[13]) + getSpace(6 - String.valueOf(o[13]).length()));
			String montatnAcompenser = String.valueOf(o[12]).replace(".0", "");

			int lengthMC = montatnAcompenser.length();
			UAP040IN.setMontantAComponser(getZero(15 - lengthMC) + montatnAcompenser);

			String amount = String.valueOf(o[2]).replace(".", "");

			UAP040IN.setMontantRetrait(getZero(15 - amount.length()) + amount);

			UAP040IN.setCodeDebitCommercant(String.valueOf(o[8]) + getSpace(1 - String.valueOf(o[8]).length()));
			String commissionsAlgeriePoste = dayOperationFransaBankRepository.getListRetraitCommissionAlgeriePoste040IN(
					String.valueOf(o[0]), String.valueOf(o[14]), String.valueOf(o[1]));
			UAP040IN.setMontantCommission(getZero(7 - commissionsAlgeriePoste.length()) + commissionsAlgeriePoste);

			UAP040IN.setDateTransaction(String.valueOf(o[10]) + getSpace(8 - String.valueOf(o[10]).length()));
			UAP040IN.setHeureTransaction(String.valueOf(o[11]) + getSpace(6 - String.valueOf(o[11]).length()));
			UAP040IN.setIdentifSystem("00SATIMDAB");
			UAP040IN.setIdentifPointRetrait(String.valueOf(o[15]) + getSpace(10 - String.valueOf(o[15]).length()));
			UAP040IN.setModeLectureCarte("2");
			UAP040IN.setMethAuthPorteur("1");
			UAP040IN.setNumAutorisation(String.valueOf(o[0]) + getSpace(6 - String.valueOf(o[0]).length()));
			String dateBefore = String.valueOf(o[10]);
			DateTimeFormatter formater = DateTimeFormatter.ofPattern("yyyyMMdd");
			LocalDate date = LocalDate.parse(dateBefore, formater);

			LocalDate updatedDate = date.plusDays(1);

			String updatedDateString = updatedDate.format(DateTimeFormatter.ofPattern("yyyyMMdd"));

			UAP040IN.setDateDebutValiditeCarte(updatedDateString + getSpace(8 - updatedDateString.length()));
			UAP040IN.setDateFinValiditeCarte(updatedDateString);
			UAP040IN.setCryptogramData("0");
			UAP040IN.setAtc("00");
			UAP040IN.setTvr("00000");
			UAP040IN.setFileIntegrationDate(fileIntegration);
			UAP040IN.setPieceComptableBKM(String.valueOf(o[16]));
			UAP040IN.setTagRetrait("2");
			UAP040IN.setMontantTransaction(Integer.valueOf(String.valueOf(o[2]).replace(".", "")));
			UAP040IN.setMontantCommissionTTC(Integer.valueOf(UAP040IN.getMontantCommission()));
			UAP040INsAlgeriePoste.add(UAP040IN);

		}
		uAP040INFransaBankRepository.saveAll(UAP040INsAlgeriePoste);

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

	@Override
	public void run() {
		switch (paramUAP) {
		case "1":
			writeUAP040IN();
			break;
		default:
			System.out.println("C tt ");
			break;
		}
		System.out.println("Param" + paramUAP);
	}
}
