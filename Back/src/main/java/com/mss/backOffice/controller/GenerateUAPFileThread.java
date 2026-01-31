package com.mss.backOffice.controller;

import com.google.common.base.Throwables;
import com.google.gson.Gson;
import com.mss.backOffice.request.Datas;
import com.mss.backOffice.request.UapData;
import com.mss.backOffice.services.FormulaInterpreterService;
import com.mss.unified.entities.Account;
import com.mss.unified.entities.AtmTerminal;
import com.mss.unified.entities.BkmvtiFransaBank;
import com.mss.unified.entities.DayOperationFransaBank;
import com.mss.unified.entities.Merchant;
import com.mss.unified.entities.MvbkConf;
import com.mss.unified.entities.MvbkSettlement;
import com.mss.unified.entities.UAPFransaBank;
import com.mss.unified.entities.User;
import com.mss.unified.repositories.BkmvtiFransaBankRepository;
import com.mss.unified.repositories.CodeBankBCRepository;
import com.mss.unified.repositories.DayOperationFransaBankRepository;
import com.mss.unified.repositories.MerchantRepository;
import com.mss.unified.repositories.MvbkConfigRepository;
import com.mss.unified.repositories.MvbkSettlementRepository;
import com.mss.unified.repositories.UAPFransaBankRepository;
import com.mss.unified.repositories.UserRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public  strictfp class GenerateUAPFileThread implements Runnable {
	private static final Gson gson = new Gson();
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
	public String paramUAP;
	@Autowired
	MerchantRepository mr;
	@Autowired
	MvbkSettlementRepository mvbkSettlementRepository;
	@Autowired
	BkmvtiFransaBankRepository bkmvtiFransaBankRepository;
	private static final Logger logger = LoggerFactory.getLogger(GenerateUAPFileThread.class);

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

	@Autowired
	public MvbkConfigRepository mvbkConfigR;

	public GenerateUAPFileThread(DayOperationFransaBankRepository dayOperationFransaBankRepository, String paramUAP,
			UAPFransaBankRepository uAPFransaBankRepository, MvbkSettlementRepository mvbkSettlementRepository,
			BkmvtiFransaBankRepository bkmvtiFransaBankRepository, String name, String fileIntegration,
			MvbkConfigRepository mvbkConfigR) {
		super();
		this.dayOperationFransaBankRepository = dayOperationFransaBankRepository;
		this.paramUAP = paramUAP;
		this.uAPFransaBankRepository = uAPFransaBankRepository;
		this.mvbkSettlementRepository = mvbkSettlementRepository;
		this.bkmvtiFransaBankRepository = bkmvtiFransaBankRepository;
		this.mvbkConfigR = mvbkConfigR;
		this.name = name;
		this.fileIntegration = fileIntegration;

	}

	public void writeUAP040() {

		writeUAP040Retrait();
		writeUAP040Consultation();
		writeUAP040RetraitAlgeriePoste();
//		generationLotRetrait();
//		generationLotConsultation();
//		generationLotAlgeriePoste();

	}

	public void writeUAP040Retrait() {
		try {
			logger.info("writeUAP040Retrait");

			List<Object[]> dayOperations = dayOperationFransaBankRepository.getListRetraitUAP();
			logger.info("dayOperations size=>{}", dayOperations.size());
			List<UAPFransaBank> uAPFransaBanks = new ArrayList<UAPFransaBank>();
			for (Object o[] : dayOperations) {
				UAPFransaBank UAPFransaBank = new UAPFransaBank();
				UAPFransaBank.setNumtransaction(String.valueOf(o[14]) + getSpace(12 - String.valueOf(o[14]).length()));
				String RefArchivage = UAPFransaBank.getNumtransaction() + String.valueOf(o[0])
						.substring(String.valueOf(o[0]).length() - 6, String.valueOf(o[0]).length());

				UAPFransaBank.setTypeTransaction("040");
				UAPFransaBank
						.setReferenceArchivage(RefArchivage + getSpace(18 - String.valueOf(RefArchivage).length()));
				UAPFransaBank.setCodeBankAcquereur(String.valueOf(o[5]) + getSpace(3 - String.valueOf(o[5]).length()));
				UAPFransaBank.setCodeAgence(String.valueOf(o[3]) + getSpace(5 - String.valueOf(o[3]).length()));
				UAPFransaBank.setTypeRetrait("001");
				UAPFransaBank
						.setCodeBin(String.valueOf(o[1]).substring(0, 6) + getSpace(6 - String.valueOf(o[1]).length()));
				UAPFransaBank.setTypeCarteDebit(
						String.valueOf(o[1]).substring(6, 8) + getSpace(2 - String.valueOf(o[1]).length()));
				UAPFransaBank
						.setNumSeq(String.valueOf(o[1]).substring(8, 14) + getSpace(6 - String.valueOf(o[1]).length()));
				UAPFransaBank.setNumOrdre(
						String.valueOf(o[1]).substring(14, 15) + getSpace(1 - String.valueOf(o[1]).length()));
				int length = String.valueOf(o[1]).length();
				String numCarte = String.valueOf(o[1]).substring(length - 1, length);
				UAPFransaBank.setCle(numCarte);
				UAPFransaBank.setFileDate(String.valueOf(o[13]) + getSpace(6 - String.valueOf(o[13]).length()));
				String montatnAcompenser = String.valueOf(o[12]).replace(".0", "");

				int lengthMC = montatnAcompenser.length();
				UAPFransaBank.setMontantAComponser(getZero(15 - lengthMC) + montatnAcompenser);

				String amount = String.valueOf(o[2]).replace(".", "");
				UAPFransaBank.setMontantRetrait(getZero(15 - amount.length()) + amount);

				UAPFransaBank
						.setCodeDebitCommercant(String.valueOf(o[8]) + getSpace(1 - String.valueOf(o[8]).length()));

				String commissions = dayOperationFransaBankRepository.getListRetraitCommission(String.valueOf(o[0]),
						String.valueOf(o[14]), String.valueOf(o[1]));
				UAPFransaBank.setMontantCommission(getZero(7 - commissions.length()) + commissions);

				UAPFransaBank.setDateTransaction(String.valueOf(o[10]) + getSpace(8 - String.valueOf(o[10]).length()));
				UAPFransaBank.setHeureTransaction(String.valueOf(o[11]) + getSpace(6 - String.valueOf(o[11]).length()));
				UAPFransaBank.setIdentifSystem("00SATIMDAB");
				UAPFransaBank
						.setIdentifPointRetrait(String.valueOf(o[15]) + getSpace(10 - String.valueOf(o[15]).length()));
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
				UAPFransaBank.setFileIntegrationDate(fileIntegration);
				UAPFransaBank.setPieceComptableBKM(String.valueOf(o[16]));
				UAPFransaBank.setTagRetrait("1");
				UAPFransaBank.setMontantTranasction(Integer.parseInt(String.valueOf(o[2]).replace(".", "")));
				UAPFransaBank.setMontantCommissionFSBKHt((int) dayOperationFransaBankRepository
						.findGByTransaction("G009", String.valueOf(o[0]), String.valueOf(o[14]), String.valueOf(o[1]))
						.get().getMontantSettlement());
				UAPFransaBank.setMontantCommissionTVA((int) dayOperationFransaBankRepository
						.findGByTransaction("G010", String.valueOf(o[0]), String.valueOf(o[14]), String.valueOf(o[1]))
						.get().getMontantSettlement());

				uAPFransaBanks.add(UAPFransaBank);
			}
			logger.info("end writeUAP040Retrait =>{}", uAPFransaBanks.size());

			uAPFransaBankRepository.saveAll(uAPFransaBanks);
		} catch (Exception e) {
			String stackTrace = Throwables.getStackTraceAsString(e);
			logger.info("Exception is=>{}", stackTrace);
		}

	}

	public void writeUAP040Consultation() {
		logger.info("writeUAP040Consultation");

		List<Object[]> dayOperationsConsultationSolde = dayOperationFransaBankRepository.getListConsultationSoldeUAP();
		List<UAPFransaBank> uAPFransaBanks = new ArrayList<UAPFransaBank>();

		for (Object o[] : dayOperationsConsultationSolde) {

			UAPFransaBank UAPFransaBank = new UAPFransaBank();
			UAPFransaBank.setTypeTransaction("040");
			UAPFransaBank.setNumtransaction(String.valueOf(o[14]) + getSpace(12 - String.valueOf(o[14]).length()));

			String RefArchivage = UAPFransaBank.getNumtransaction()
					+ String.valueOf(o[0]).substring(String.valueOf(o[0]).length() - 6, String.valueOf(o[0]).length());
			UAPFransaBank.setReferenceArchivage(RefArchivage + getSpace(18 - String.valueOf(RefArchivage).length()));
			UAPFransaBank.setCodeBankAcquereur(String.valueOf(o[5]) + getSpace(3 - String.valueOf(o[5]).length()));
			UAPFransaBank.setCodeAgence(String.valueOf(o[3]) + getSpace(5 - String.valueOf(o[3]).length()));
			UAPFransaBank.setTypeRetrait("002");
			UAPFransaBank
					.setCodeBin(String.valueOf(o[1]).substring(0, 6) + getSpace(6 - String.valueOf(o[1]).length()));
			int commissions = Integer.parseInt(String.valueOf(o[12]).replace(".0", ""));
			UAPFransaBank.setMontantCommission(getZero(7 - String.valueOf(Math.round(commissions)).length())
					+ String.valueOf(Math.round(commissions)));

			UAPFransaBank.setMontantAComponser(getZero(15 - String.valueOf(Math.round(commissions)).length())
					+ String.valueOf(Math.round(commissions)));

			UAPFransaBank.setTypeCarteDebit(
					String.valueOf(o[1]).substring(6, 8) + getSpace(2 - String.valueOf(o[1]).length()));
			UAPFransaBank
					.setNumSeq(String.valueOf(o[1]).substring(8, 14) + getSpace(6 - String.valueOf(o[1]).length()));
			UAPFransaBank
					.setNumOrdre(String.valueOf(o[1]).substring(14, 15) + getSpace(1 - String.valueOf(o[1]).length()));
			int length = String.valueOf(o[1]).length();
			String numCarte = String.valueOf(o[1]).substring(length - 1, length);
			UAPFransaBank.setCle(numCarte);

			UAPFransaBank.setFileDate(String.valueOf(o[13]) + getSpace(6 - String.valueOf(o[13]).length()));
			UAPFransaBank.setMontantRetrait("000000000000000");

			UAPFransaBank.setCodeDebitCommercant(String.valueOf(o[8]) + getSpace(1 - String.valueOf(o[8]).length()));

			UAPFransaBank.setDateTransaction(String.valueOf(o[10]) + getSpace(8 - String.valueOf(o[10]).length()));
			UAPFransaBank.setHeureTransaction(String.valueOf(o[11]) + getSpace(6 - String.valueOf(o[11]).length()));
			UAPFransaBank.setIdentifSystem("00SATIMDAB");
			UAPFransaBank.setIdentifPointRetrait(String.valueOf(o[15]) + getSpace(10 - String.valueOf(o[15]).length()));
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
			UAPFransaBank.setFileIntegrationDate(fileIntegration);
			UAPFransaBank.setPieceComptableBKM(String.valueOf(o[16]));
			UAPFransaBank.setMontantTranasction(Integer.parseInt(String.valueOf(o[2]).replace(".", "")));
			UAPFransaBank.setMontantCommissionFSBKHt((int) dayOperationFransaBankRepository
					.findGByTransaction("G122", String.valueOf(o[0]), String.valueOf(o[14]), String.valueOf(o[1])).get()
					.getMontantSettlement());
			UAPFransaBank.setMontantCommissionTVA((int) dayOperationFransaBankRepository
					.findGByTransaction("G123", String.valueOf(o[0]), String.valueOf(o[14]), String.valueOf(o[1])).get()
					.getMontantSettlement());

			uAPFransaBanks.add(UAPFransaBank);

		}
		logger.info("end writeUAP040Consultation =>{}", uAPFransaBanks.size());

		uAPFransaBankRepository.saveAll(uAPFransaBanks);

	}

	public void writeUAP040RetraitAlgeriePoste() {
		logger.info("writeUAP040RetraitAlgeriePoste");

		List<Object[]> dayOperationsAlgeriePoste = dayOperationFransaBankRepository.getListRetraitUAPAlgeriePoste();
		List<UAPFransaBank> uAPFransaBanks = new ArrayList<UAPFransaBank>();

		for (Object o[] : dayOperationsAlgeriePoste) {
			UAPFransaBank UAPFransaBank = new UAPFransaBank();
			UAPFransaBank.setNumtransaction(String.valueOf(o[14]) + getSpace(12 - String.valueOf(o[14]).length()));

			String RefArchivage = UAPFransaBank.getNumtransaction()
					+ String.valueOf(o[0]).substring(String.valueOf(o[0]).length() - 6, String.valueOf(o[0]).length());

			UAPFransaBank.setTypeTransaction("040");
			UAPFransaBank.setReferenceArchivage(RefArchivage + getSpace(18 - String.valueOf(RefArchivage).length()));
			UAPFransaBank.setCodeBankAcquereur(String.valueOf(o[5]) + getSpace(3 - String.valueOf(o[5]).length()));
			UAPFransaBank.setCodeAgence(String.valueOf(o[3]) + getSpace(5 - String.valueOf(o[3]).length()));
			UAPFransaBank.setTypeRetrait("001");
			UAPFransaBank
					.setCodeBin(String.valueOf(o[1]).substring(0, 6) + getSpace(6 - String.valueOf(o[1]).length()));
			UAPFransaBank.setTypeCarteDebit(
					String.valueOf(o[1]).substring(6, 8) + getSpace(2 - String.valueOf(o[1]).length()));
			UAPFransaBank
					.setNumSeq(String.valueOf(o[1]).substring(8, 14) + getSpace(6 - String.valueOf(o[1]).length()));
			UAPFransaBank
					.setNumOrdre(String.valueOf(o[1]).substring(14, 15) + getSpace(1 - String.valueOf(o[1]).length()));
			int length = String.valueOf(o[1]).length();
			String numCarte = String.valueOf(o[1]).substring(length - 1, length);
			UAPFransaBank.setCle(numCarte);

			UAPFransaBank.setFileDate(String.valueOf(o[13]) + getSpace(6 - String.valueOf(o[13]).length()));
			String montatnAcompenser = String.valueOf(o[12]).replace(".0", "");

			int lengthMC = montatnAcompenser.length();
			UAPFransaBank.setMontantAComponser(getZero(15 - lengthMC) + montatnAcompenser);
			String amount = String.valueOf(o[2]).replace(".", "");
			UAPFransaBank.setMontantRetrait(getZero(15 - amount.length()) + amount);

			UAPFransaBank.setCodeDebitCommercant(String.valueOf(o[8]) + getSpace(1 - String.valueOf(o[8]).length()));
			String commissionsAlgeriePoste = dayOperationFransaBankRepository.getListRetraitCommissionAlgeriePoste(
					String.valueOf(o[0]), String.valueOf(o[14]), String.valueOf(o[1]));
			UAPFransaBank.setMontantCommission(getZero(7 - commissionsAlgeriePoste.length()) + commissionsAlgeriePoste);

			UAPFransaBank.setNumtransaction(String.valueOf(o[14]) + getSpace(12 - String.valueOf(o[14]).length()));
			UAPFransaBank.setDateTransaction(String.valueOf(o[10]) + getSpace(8 - String.valueOf(o[10]).length()));
			UAPFransaBank.setHeureTransaction(String.valueOf(o[11]) + getSpace(6 - String.valueOf(o[11]).length()));
			UAPFransaBank.setIdentifSystem("00SATIMDAB");
			UAPFransaBank.setIdentifPointRetrait(String.valueOf(o[15]) + getSpace(10 - String.valueOf(o[15]).length()));
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
			UAPFransaBank.setFileIntegrationDate(fileIntegration);
			UAPFransaBank.setPieceComptableBKM(String.valueOf(o[16]));
			UAPFransaBank.setTagRetrait("2");
			UAPFransaBank.setMontantTranasction(Integer.parseInt(String.valueOf(o[2]).replace(".", "")));
			UAPFransaBank.setMontantCommissionFSBKHt((int) dayOperationFransaBankRepository
					.findGByTransaction("G171", String.valueOf(o[0]), String.valueOf(o[14]), String.valueOf(o[1])).get()
					.getMontantSettlement());
			UAPFransaBank.setMontantCommissionTVA((int) dayOperationFransaBankRepository
					.findGByTransaction("G172", String.valueOf(o[0]), String.valueOf(o[14]), String.valueOf(o[1])).get()
					.getMontantSettlement());

			uAPFransaBanks.add(UAPFransaBank);

		}
		logger.info("end writeUAP040RetraitAlgeriePoste =>{}", uAPFransaBanks.size());

		uAPFransaBankRepository.saveAll(uAPFransaBanks);

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

	public String getAmountFormat(double amount) {
		float m = Math.round(amount);
		int a = (int) m;

		String amountFormat = String.format("%018d,%02d", a / 100, a % 100);
		return amountFormat;
	}

	public int testMethode(int method) {
		int acq = 0;
		if (method != 1 && method != 3 && method != 4 && method != 6 && method != 8 && method != 9 && method != 11
				&& method != 12 && method != 14) {
			acq = 1;
		} else if (method != 1 || method != 3 || method != 4 || method != 6 || method != 8 || method != 9
				|| method != 11 || method != 12 || method != 14) {
			acq = 2;
		}
		return acq;
	}

	@Override
	public void run() {
		switch (paramUAP) {
		case "1":
			writeUAP040();
			break;
		default:
			System.out.println("C tt ");
			break;
		}
		System.out.println("Param" + paramUAP);
	}
}
