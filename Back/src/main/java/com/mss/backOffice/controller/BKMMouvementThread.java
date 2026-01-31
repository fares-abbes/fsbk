package com.mss.backOffice.controller;

import static com.mss.backOffice.controller.ExecutorBKMMouvement.days;

import com.mss.backOffice.services.FormulaInterpreterService;
import com.mss.unified.entities.Account;
import com.mss.unified.entities.AtmTerminal;
import com.mss.unified.entities.BkmvtiFransaBank;
import com.mss.unified.entities.DayOperationCardFransaBank;
import com.mss.unified.entities.DayOperationFransaBank;
import com.mss.unified.entities.Merchant;
import com.mss.unified.entities.MvbkConf;
import com.mss.unified.entities.MvbkSettlement;
import com.mss.unified.entities.SettelementFransaBank;
import com.mss.unified.entities.User;
import com.mss.unified.repositories.*;

import org.apache.commons.beanutils.PropertyUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;

import java.lang.reflect.InvocationTargetException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.Optional;

public strictfp class BKMMouvementThread implements Runnable {

	FormulaInterpreterService fIPService;
	public String name;
	@Autowired
	CodeBankBCRepository codeBankBCRepository;
	@Autowired
	DayOperationFransaBankRepository dayOperationFransaBankRepository;
	@Autowired
	MvbkSettlementRepository mvbkSettlementRepository;
	@Autowired
	BkmvtiFransaBankRepository bkmvtiFransaBankRepository;
	public MvbkConfigRepository mvbkConfigR;
	public String paramGlobalBancaire;
	public static Integer eve1 = 0;
	public static int eve = 0;
	MerchantRepository mr;

	private SettelementFransaBankRepository settlementRepo;

	private static final Logger logger = LoggerFactory.getLogger(BKMMouvementThread.class);

	public synchronized int getEveIndex() {
		return (++eve);
	}

	public static int getEveIndex1() {
		synchronized (eve1) {
			eve1 = eve1 + 1;
		}

		return eve1;
	}

	public BKMMouvementThread(CodeBankBCRepository codeBankBCRepository,
			DayOperationFransaBankRepository dayOperationFransaBankRepository,
			MvbkSettlementRepository mvbkSettlementRepository,

			BkmvtiFransaBankRepository bkmvtiFransaBankRepository, String paramGlobalBancaire, String name) {

		super();

		this.codeBankBCRepository = codeBankBCRepository;
		this.dayOperationFransaBankRepository = dayOperationFransaBankRepository;
		this.mvbkSettlementRepository = mvbkSettlementRepository;
		this.bkmvtiFransaBankRepository = bkmvtiFransaBankRepository;
		this.paramGlobalBancaire = paramGlobalBancaire;
		this.name = name;

	}

	public BKMMouvementThread(MerchantRepository mr, SettelementFransaBankRepository settlementRepo,
			FormulaInterpreterService fIPService, MvbkConfigRepository mvbkConfigR2,
			CodeBankBCRepository codeBankBCRepository,
			DayOperationFransaBankRepository dayOperationFransaBankRepository,
			MvbkSettlementRepository mvbkSettlementRepository, BkmvtiFransaBankRepository bkmvtiFransaBankRepository,
			String paramGlobalBancaire, String name) {

		super();
		this.fIPService = fIPService;
		this.codeBankBCRepository = codeBankBCRepository;
		this.dayOperationFransaBankRepository = dayOperationFransaBankRepository;
		this.mvbkSettlementRepository = mvbkSettlementRepository;
		this.bkmvtiFransaBankRepository = bkmvtiFransaBankRepository;
		this.paramGlobalBancaire = paramGlobalBancaire;
		this.name = name;
		this.mvbkConfigR = mvbkConfigR2;
		this.mr = mr;
		this.settlementRepo = settlementRepo;
	}

	// ***************Retrait Wissal**********//
	public void mvbkOnUsMemeAgenceWissal() {
		logger.info("begin mvbkOnUsMemeAgence");
		List<DayOperationFransaBank> dayOperationFransaBanks = dayOperationFransaBankRepository.getListOnUsMemeAgence();
		List<BkmvtiFransaBank> BkmvtiFransaBanks = new ArrayList<BkmvtiFransaBank>();
		List<MvbkConf> allMvbkSettelemntsC = mvbkConfigR.findByCategorie(1);

		for (DayOperationFransaBank day : dayOperationFransaBanks) {
			int indexPieceComptable = getEveIndex1();

			List<DayOperationFransaBank> dayOperationFransaBanksByNumTransaction = dayOperationFransaBankRepository
					.getListOnUsMemeAgenceByNumTransaction(day.getNumRefTransaction(), day.getNumAutorisation(),
							day.getNumCartePorteur());
			for (MvbkConf mvk : allMvbkSettelemntsC) {
				int index2 = getEveIndex();
				BkmvtiFransaBanks = TestSigne(1, mvk, dayOperationFransaBanksByNumTransaction, BkmvtiFransaBanks,
						indexPieceComptable, index2, mvk.getCodeSettlement());

			}
			updateDays(1, indexPieceComptable, dayOperationFransaBanksByNumTransaction, allMvbkSettelemntsC.get(0));

		}
		logger.info("end mvbkOnUsMemeAgence =>{}", BkmvtiFransaBanks.size());
		if (BkmvtiFransaBanks != null && !BkmvtiFransaBanks.isEmpty()) {
		    // Filter out null elements from the list
		    List<BkmvtiFransaBank> nonNullBkmvtiFransaBanks = BkmvtiFransaBanks.stream()
		            .filter(Objects::nonNull)
		            .collect(Collectors.toList());

		    if (!nonNullBkmvtiFransaBanks.isEmpty()) {
		        bkmvtiFransaBankRepository.saveAll(nonNullBkmvtiFransaBanks);
		    }
		    
		}
	}

	public void mvbkOnUsAutreAgenceWissal() {
		logger.info("begin mvbkOnUsAutreAgence");
		List<DayOperationFransaBank> dayOperationFransaBanks = dayOperationFransaBankRepository
				.getListOnUsAutreAgence();
		List<BkmvtiFransaBank> BkmvtiFransaBanks = new ArrayList<BkmvtiFransaBank>();

		List<MvbkConf> mvbks = mvbkConfigR.findByCategorie(2);

		for (DayOperationFransaBank day : dayOperationFransaBanks) {
			int indexPieceComptable = getEveIndex1();

			List<DayOperationFransaBank> dayOperationFransaBanksByNumTransaction = dayOperationFransaBankRepository
					.getListOnUsMemeAgenceByNumTransaction(day.getNumRefTransaction(), day.getNumAutorisation(),
							day.getNumCartePorteur());

			for (MvbkConf mvk : mvbks) {
				int index2 = getEveIndex();
				BkmvtiFransaBanks = TestSigne(1, mvk, dayOperationFransaBanksByNumTransaction, BkmvtiFransaBanks,
						indexPieceComptable, index2, mvk.getCodeSettlement());

			}
			updateDays(1, indexPieceComptable, dayOperationFransaBanksByNumTransaction, mvbks.get(0));

		}

		if (BkmvtiFransaBanks != null && !BkmvtiFransaBanks.isEmpty()) {
		    // Filter out null elements from the list
		    List<BkmvtiFransaBank> nonNullBkmvtiFransaBanks = BkmvtiFransaBanks.stream()
		            .filter(Objects::nonNull)
		            .collect(Collectors.toList());

		    if (!nonNullBkmvtiFransaBanks.isEmpty()) {
		        bkmvtiFransaBankRepository.saveAll(nonNullBkmvtiFransaBanks);
		    }
		    
		}		logger.info("end mvbkOnUsAutreAgence =>{}", BkmvtiFransaBanks.size());

	}

	public void mvbkOffUsIssuerWissal() {
		logger.info("begin mvbkOffUsIssuer");

		List<DayOperationFransaBank> dayOperationFransaBanks = dayOperationFransaBankRepository.getListOffUsIssuer();
		List<BkmvtiFransaBank> BkmvtiFransaBanks = new ArrayList<BkmvtiFransaBank>();

		List<MvbkConf> allMvbkSettelemntsC = mvbkConfigR.findByCategorie(3);

		for (DayOperationFransaBank day : dayOperationFransaBanks) {
			int indexPieceComptable = getEveIndex1();
			List<DayOperationFransaBank> dayOperationFransaBanksByNumTransaction = dayOperationFransaBankRepository
					.getListOnUsMemeAgenceByNumTransaction(day.getNumRefTransaction(), day.getNumAutorisation(),
							day.getNumCartePorteur());

			for (MvbkConf mvk : allMvbkSettelemntsC) {

				int index2 = getEveIndex();

				BkmvtiFransaBanks = TestSigne(2, mvk, dayOperationFransaBanksByNumTransaction, BkmvtiFransaBanks,
						indexPieceComptable, index2, mvk.getCodeSettlement());

			}
			updateDays(2, indexPieceComptable, dayOperationFransaBanksByNumTransaction, allMvbkSettelemntsC.get(0));

		}
		logger.info("end mvbkOffUsIssuer =>{}", BkmvtiFransaBanks.size());
		if (BkmvtiFransaBanks != null && !BkmvtiFransaBanks.isEmpty()) {
		    // Filter out null elements from the list
		    List<BkmvtiFransaBank> nonNullBkmvtiFransaBanks = BkmvtiFransaBanks.stream()
		            .filter(Objects::nonNull)
		            .collect(Collectors.toList());

		    if (!nonNullBkmvtiFransaBanks.isEmpty()) {
		        bkmvtiFransaBankRepository.saveAll(nonNullBkmvtiFransaBanks);
		    }
		    
		}
	}

	public void mvbkOffUsAcqWissal() {
		logger.info("begin mvbkOffUsAcq");
		List<DayOperationFransaBank> dayOperationFransaBanks = dayOperationFransaBankRepository.getListOffUsAcq();
		List<BkmvtiFransaBank> BkmvtiFransaBanks = new ArrayList<BkmvtiFransaBank>();
		List<DayOperationFransaBank> daysSaved = new ArrayList<DayOperationFransaBank>();
		List<MvbkConf> mvbks = mvbkConfigR.findByCategorie(4);

		for (DayOperationFransaBank day : dayOperationFransaBanks) {
			int indexPieceComptable = getEveIndex1();

			List<DayOperationFransaBank> dayOperationFransaBanksByNumTransaction = dayOperationFransaBankRepository
					.getListOnUsMemeAgenceByNumTransaction(day.getNumRefTransaction(), day.getNumAutorisation(),
							day.getNumCartePorteur());

			for (MvbkConf mvk : mvbks) {
				int index2 = getEveIndex();
				BkmvtiFransaBanks = TestSigne(3, mvk, dayOperationFransaBanksByNumTransaction, BkmvtiFransaBanks,
						indexPieceComptable, index2, mvk.getCodeSettlement());

			}
			updateDays(3, indexPieceComptable, dayOperationFransaBanksByNumTransaction, mvbks.get(0));

		}
		logger.info("end mvbkOffUsAcq=>{}", BkmvtiFransaBanks.size());
		if (BkmvtiFransaBanks != null && !BkmvtiFransaBanks.isEmpty()) {
		    // Filter out null elements from the list
		    List<BkmvtiFransaBank> nonNullBkmvtiFransaBanks = BkmvtiFransaBanks.stream()
		            .filter(Objects::nonNull)
		            .collect(Collectors.toList());

		    if (!nonNullBkmvtiFransaBanks.isEmpty()) {
		        bkmvtiFransaBankRepository.saveAll(nonNullBkmvtiFransaBanks);
		    }
		    
		}
	}

	public void mvbkOffUsAcqAlgeriePosteWissal() {
		logger.info("begin mvbkOffUsAcqAlgeriePoste");
		List<DayOperationFransaBank> dayOperationFransaBanks = dayOperationFransaBankRepository
				.getListOffUsAcqAlgeriePoste();
		List<BkmvtiFransaBank> BkmvtiFransaBanks = new ArrayList<BkmvtiFransaBank>();

		List<MvbkConf> mvbks = mvbkConfigR.findByCategorie(14);

		for (DayOperationFransaBank day : dayOperationFransaBanks) {
			int indexPieceComptable = getEveIndex1();
			List<DayOperationFransaBank> dayOperationFransaBanksByNumTransaction = dayOperationFransaBankRepository
					.getListOnUsMemeAgenceByNumTransaction(day.getNumRefTransaction(), day.getNumAutorisation(),
							day.getNumCartePorteur());

			for (MvbkConf mvk : mvbks) {
				int index2 = getEveIndex();
				BkmvtiFransaBanks = TestSigne(15, mvk, dayOperationFransaBanksByNumTransaction, BkmvtiFransaBanks,
						indexPieceComptable, index2, mvk.getCodeSettlement());

			}
			updateDays(15, indexPieceComptable, dayOperationFransaBanksByNumTransaction, mvbks.get(0));

		}
		logger.info("end mvbkOffUsAcqAlgeriePoste =>{}", BkmvtiFransaBanks.size());
		if (BkmvtiFransaBanks != null && !BkmvtiFransaBanks.isEmpty()) {
		    // Filter out null elements from the list
		    List<BkmvtiFransaBank> nonNullBkmvtiFransaBanks = BkmvtiFransaBanks.stream()
		            .filter(Objects::nonNull)
		            .collect(Collectors.toList());

		    if (!nonNullBkmvtiFransaBanks.isEmpty()) {
		        bkmvtiFransaBankRepository.saveAll(nonNullBkmvtiFransaBanks);
		    }
		    
		}	}

	public void mvbkOffUsIssuerAlgeriePosteWissal() {
		logger.info("begin mvbkOffUsIssuerAlgeriePoste");
		List<DayOperationFransaBank> dayOperationFransaBanks = dayOperationFransaBankRepository
				.getListOffUsIssuerAlgeriePoste();
		List<BkmvtiFransaBank> BkmvtiFransaBanks = new ArrayList<BkmvtiFransaBank>();
		List<MvbkConf> mvbks = mvbkConfigR.findByCategorie(13);

		for (DayOperationFransaBank day : dayOperationFransaBanks) {
			int indexPieceComptable = getEveIndex1();
			List<DayOperationFransaBank> dayOperationFransaBanksByNumTransaction = dayOperationFransaBankRepository
					.getListOnUsMemeAgenceByNumTransaction(day.getNumRefTransaction(), day.getNumAutorisation(),
							day.getNumCartePorteur());

			for (MvbkConf mvk : mvbks) {
				int index2 = getEveIndex();
				BkmvtiFransaBanks = TestSigne(16, mvk, dayOperationFransaBanksByNumTransaction, BkmvtiFransaBanks,
						indexPieceComptable, index2, mvk.getCodeSettlement());

			}
			updateDays(16, indexPieceComptable, dayOperationFransaBanksByNumTransaction, mvbks.get(0));

		}
		logger.info("end mvbkOffUsIssuerAlgeriePoste =>{}", BkmvtiFransaBanks.size());
		if (BkmvtiFransaBanks != null && !BkmvtiFransaBanks.isEmpty()) {
		    // Filter out null elements from the list
		    List<BkmvtiFransaBank> nonNullBkmvtiFransaBanks = BkmvtiFransaBanks.stream()
		            .filter(Objects::nonNull)
		            .collect(Collectors.toList());

		    if (!nonNullBkmvtiFransaBanks.isEmpty()) {
		        bkmvtiFransaBankRepository.saveAll(nonNullBkmvtiFransaBanks);
		    }
		    
		}	}

//***************Achat Wissal*******//
	public void mvbkOnUsMemeAgenceAchatWissal() {
		logger.info("begin mvbkOnUsMemeAgenceAchat =>{}");
		List<DayOperationFransaBank> dayOperationFransaBanks = dayOperationFransaBankRepository
				.getListOnUsMemeAgenceAchat();
		List<BkmvtiFransaBank> BkmvtiFransaBanks = new ArrayList<BkmvtiFransaBank>();
		List<MvbkConf> mvbks = mvbkConfigR.findByCategorie(5);

		for (DayOperationFransaBank day : dayOperationFransaBanks) {
			int indexPieceComptable = getEveIndex1();
			List<DayOperationFransaBank> dayOperationFransaBanksByNumTransaction = dayOperationFransaBankRepository
					.getListOnUsMemeAgenceByNumTransaction(day.getNumRefTransaction(), day.getNumAutorisation(),
							day.getNumCartePorteur());

			for (MvbkConf mvk : mvbks) {
				int index2 = getEveIndex();
				BkmvtiFransaBanks = TestSigne(6, mvk, dayOperationFransaBanksByNumTransaction, BkmvtiFransaBanks,
						indexPieceComptable, index2, mvk.getCodeSettlement());

			}
			updateDays(6, indexPieceComptable, dayOperationFransaBanksByNumTransaction, mvbks.get(0));

		}
		logger.info("end mvbkOnUsMemeAgenceAchat=>{}", BkmvtiFransaBanks.size());
		if (BkmvtiFransaBanks != null && !BkmvtiFransaBanks.isEmpty()) {
		    // Filter out null elements from the list
		    List<BkmvtiFransaBank> nonNullBkmvtiFransaBanks = BkmvtiFransaBanks.stream()
		            .filter(Objects::nonNull)
		            .collect(Collectors.toList());

		    if (!nonNullBkmvtiFransaBanks.isEmpty()) {
		        bkmvtiFransaBankRepository.saveAll(nonNullBkmvtiFransaBanks);
		    }
		    
		}	}

	public void mvbkOnUsAutreAgenceAchatWissal() {
		logger.info(" begin mvbkOnUsAutreAgenceAchat");
		List<DayOperationFransaBank> dayOperationFransaBanks = dayOperationFransaBankRepository
				.getListOnUsAutreAgenceAchat();
		List<BkmvtiFransaBank> BkmvtiFransaBanks = new ArrayList<BkmvtiFransaBank>();
		List<MvbkConf> mvbks = mvbkConfigR.findByCategorie(6);

		for (DayOperationFransaBank day : dayOperationFransaBanks) {
			int indexPieceComptable = getEveIndex1();
			List<DayOperationFransaBank> dayOperationFransaBanksByNumTransaction = dayOperationFransaBankRepository
					.getListOnUsMemeAgenceByNumTransaction(day.getNumRefTransaction(), day.getNumAutorisation(),
							day.getNumCartePorteur());

			for (MvbkConf mvk : mvbks) {
				int index2 = getEveIndex();
				BkmvtiFransaBanks = TestSigne(6, mvk, dayOperationFransaBanksByNumTransaction, BkmvtiFransaBanks,
						indexPieceComptable, index2, mvk.getCodeSettlement());

			}
			updateDays(6, indexPieceComptable, dayOperationFransaBanksByNumTransaction, mvbks.get(0));

		}
		logger.info("end mvbkOnUsAutreAgenceAchat=>{}", BkmvtiFransaBanks.size());
		if (BkmvtiFransaBanks != null && !BkmvtiFransaBanks.isEmpty()) {
		    // Filter out null elements from the list
		    List<BkmvtiFransaBank> nonNullBkmvtiFransaBanks = BkmvtiFransaBanks.stream()
		            .filter(Objects::nonNull)
		            .collect(Collectors.toList());

		    if (!nonNullBkmvtiFransaBanks.isEmpty()) {
		        bkmvtiFransaBankRepository.saveAll(nonNullBkmvtiFransaBanks);
		    }
		    
		}	}

	public void mvbkOffUsIssuerAchatWissal() {
		logger.info("begin mvbkOffUsIssuerAchat");
		List<DayOperationFransaBank> dayOperationFransaBanks = dayOperationFransaBankRepository
				.getListOffUsIssuerAchat();
		List<BkmvtiFransaBank> BkmvtiFransaBanks = new ArrayList<BkmvtiFransaBank>();
		List<MvbkConf> mvbks = mvbkConfigR.findByCategorie(7);

		for (DayOperationFransaBank day : dayOperationFransaBanks) {
			int indexPieceComptable = getEveIndex1();
			List<DayOperationFransaBank> dayOperationFransaBanksByNumTransaction = dayOperationFransaBankRepository
					.getListOnUsMemeAgenceByNumTransaction(day.getNumRefTransaction(), day.getNumAutorisation(),
							day.getNumCartePorteur());

			for (MvbkConf mvk : mvbks) {
				int index2 = getEveIndex();
				BkmvtiFransaBanks = TestSigne(7, mvk, dayOperationFransaBanksByNumTransaction, BkmvtiFransaBanks,
						indexPieceComptable, index2, mvk.getCodeSettlement());

			}
			updateDays(7, indexPieceComptable, dayOperationFransaBanksByNumTransaction, mvbks.get(0));

		}
		logger.info("end mvbkOffUsIssuerAchat =>{}", BkmvtiFransaBanks.size());
		if (BkmvtiFransaBanks != null && !BkmvtiFransaBanks.isEmpty()) {
		    // Filter out null elements from the list
		    List<BkmvtiFransaBank> nonNullBkmvtiFransaBanks = BkmvtiFransaBanks.stream()
		            .filter(Objects::nonNull)
		            .collect(Collectors.toList());

		    if (!nonNullBkmvtiFransaBanks.isEmpty()) {
		        bkmvtiFransaBankRepository.saveAll(nonNullBkmvtiFransaBanks);
		    }
		    
		}	}

	public void mvbkOffUsAcqAchatWissal() {
		logger.info("begin mvbkOffUsAcqAchat");
		List<DayOperationFransaBank> dayOperationFransaBanks = dayOperationFransaBankRepository.getListOffUsAcqAchat();

		List<BkmvtiFransaBank> BkmvtiFransaBanks = new ArrayList<BkmvtiFransaBank>();
		List<MvbkConf> mvbks = mvbkConfigR.findByCategorie(8);

		for (DayOperationFransaBank day : dayOperationFransaBanks) {
			int indexPieceComptable = getEveIndex1();
			List<DayOperationFransaBank> dayOperationFransaBanksByNumTransaction = dayOperationFransaBankRepository
					.getListOnUsMemeAgenceByNumTransaction(day.getNumRefTransaction(), day.getNumAutorisation(),
							day.getNumCartePorteur());

			for (MvbkConf mvk : mvbks) {
				int index2 = getEveIndex();
				BkmvtiFransaBanks = TestSigne(8, mvk, dayOperationFransaBanksByNumTransaction, BkmvtiFransaBanks,
						indexPieceComptable, index2, mvk.getCodeSettlement());

			}
			updateDays(8, indexPieceComptable, dayOperationFransaBanksByNumTransaction, mvbks.get(0));

		}
		logger.info("end mvbkOffUsAcqAchat =>{}", BkmvtiFransaBanks.size());
		if (BkmvtiFransaBanks != null && !BkmvtiFransaBanks.isEmpty()) {
		    // Filter out null elements from the list
		    List<BkmvtiFransaBank> nonNullBkmvtiFransaBanks = BkmvtiFransaBanks.stream()
		            .filter(Objects::nonNull)
		            .collect(Collectors.toList());

		    if (!nonNullBkmvtiFransaBanks.isEmpty()) {
		        bkmvtiFransaBankRepository.saveAll(nonNullBkmvtiFransaBanks);
		    }
		    
		}	}

//**************Achat Wissal********//

//*************Consultation solde*****//
	public void mvbkONUdMemeAgenceConsultationSoldeWissal() {
		logger.info("begin mvbkONUdMemeAgenceConsultationSolde");
		List<DayOperationFransaBank> dayOperationFransaBanks = dayOperationFransaBankRepository
				.getListOnUsConsultationSolde();

		List<BkmvtiFransaBank> BkmvtiFransaBanks = new ArrayList<BkmvtiFransaBank>();
		List<MvbkConf> mvbks = mvbkConfigR.findByCategorie(9);

		for (DayOperationFransaBank day : dayOperationFransaBanks) {
			int indexPieceComptable = getEveIndex1();
			List<DayOperationFransaBank> dayOperationFransaBanksByNumTransaction = dayOperationFransaBankRepository
					.getListOnUsMemeAgenceByNumTransaction(day.getNumRefTransaction(), day.getNumAutorisation(),
							day.getNumCartePorteur());

			for (MvbkConf mvk : mvbks) {
				int index2 = getEveIndex();
				BkmvtiFransaBanks = TestSigne(9, mvk, dayOperationFransaBanksByNumTransaction, BkmvtiFransaBanks,
						indexPieceComptable, index2, mvk.getCodeSettlement());

			}
			updateDays(9, indexPieceComptable, dayOperationFransaBanksByNumTransaction, mvbks.get(0));

		}
		logger.info("end mvbkONUdMemeAgenceConsultationSolde =>{}", BkmvtiFransaBanks.size());
		if (BkmvtiFransaBanks != null && !BkmvtiFransaBanks.isEmpty()) {
		    // Filter out null elements from the list
		    List<BkmvtiFransaBank> nonNullBkmvtiFransaBanks = BkmvtiFransaBanks.stream()
		            .filter(Objects::nonNull)
		            .collect(Collectors.toList());

		    if (!nonNullBkmvtiFransaBanks.isEmpty()) {
		        bkmvtiFransaBankRepository.saveAll(nonNullBkmvtiFransaBanks);
		    }
		    
		}	}

	public void mvbkONUdAutreAgenceConsultationSoldeWissal() {
		logger.info("begin mvbkONUdAutreAgenceConsultationSolde");
		List<DayOperationFransaBank> dayOperationFransaBanks = dayOperationFransaBankRepository
				.getListOnUsAutreAgenceConsultationSolde();

		List<BkmvtiFransaBank> BkmvtiFransaBanks = new ArrayList<BkmvtiFransaBank>();
		List<MvbkConf> mvbks = mvbkConfigR.findByCategorie(10);

		for (DayOperationFransaBank day : dayOperationFransaBanks) {
			int indexPieceComptable = getEveIndex1();
			List<DayOperationFransaBank> dayOperationFransaBanksByNumTransaction = dayOperationFransaBankRepository
					.getListOnUsMemeAgenceByNumTransaction(day.getNumRefTransaction(), day.getNumAutorisation(),
							day.getNumCartePorteur());

			for (MvbkConf mvk : mvbks) {
				int index2 = getEveIndex();
				BkmvtiFransaBanks = TestSigne(9, mvk, dayOperationFransaBanksByNumTransaction, BkmvtiFransaBanks,
						indexPieceComptable, index2, mvk.getCodeSettlement());

			}
			updateDays(9, indexPieceComptable, dayOperationFransaBanksByNumTransaction, mvbks.get(0));

		}
		logger.info("end mvbkONUdAutreAgenceConsultationSolde =>{}", BkmvtiFransaBanks.size());
		if (BkmvtiFransaBanks != null && !BkmvtiFransaBanks.isEmpty()) {
		    // Filter out null elements from the list
		    List<BkmvtiFransaBank> nonNullBkmvtiFransaBanks = BkmvtiFransaBanks.stream()
		            .filter(Objects::nonNull)
		            .collect(Collectors.toList());

		    if (!nonNullBkmvtiFransaBanks.isEmpty()) {
		        bkmvtiFransaBankRepository.saveAll(nonNullBkmvtiFransaBanks);
		    }
		    
		}
	}

	public void mvbkOffUsIssuerConsultationSoldeWissal() {
		logger.info("begin mvbkOffUsIssuerConsultationSolde");
		List<DayOperationFransaBank> dayOperationFransaBanks = dayOperationFransaBankRepository
				.getListOffUsIssuerConsultationSolde();

		List<BkmvtiFransaBank> BkmvtiFransaBanks = new ArrayList<BkmvtiFransaBank>();
		List<MvbkConf> mvbks = mvbkConfigR.findByCategorie(11);
//		FileRequest.print(dayOperationFransaBanks.size() + "", FileRequest.getLineNumber());
//		FileRequest.print(mvbks.size() + "", FileRequest.getLineNumber());

		for (DayOperationFransaBank day : dayOperationFransaBanks) {
			int indexPieceComptable = getEveIndex1();
			List<DayOperationFransaBank> dayOperationFransaBanksByNumTransaction = dayOperationFransaBankRepository
					.getListOnUsMemeAgenceByNumTransaction(day.getNumRefTransaction(), day.getNumAutorisation(),
							day.getNumCartePorteur());
//			if (dayOperationFransaBanksByNumTransaction.size() > 7) {
//				FileRequest.print(dayOperationFransaBanksByNumTransaction.size() + "", FileRequest.getLineNumber());
//				FileRequest.print(day.toString() + "", FileRequest.getLineNumber());
//
//			}

			for (MvbkConf mvk : mvbks) {
				int index2 = getEveIndex();
				BkmvtiFransaBanks = TestSigne(10, mvk, dayOperationFransaBanksByNumTransaction, BkmvtiFransaBanks,
						indexPieceComptable, index2, mvk.getCodeSettlement());

			}
			updateDays(10, indexPieceComptable, dayOperationFransaBanksByNumTransaction, mvbks.get(0));

		}
		logger.info("end mvbkOffUsIssuerConsultationSolde =>{}", BkmvtiFransaBanks.size());
		if (BkmvtiFransaBanks != null && !BkmvtiFransaBanks.isEmpty()) {
		    // Filter out null elements from the list
		    List<BkmvtiFransaBank> nonNullBkmvtiFransaBanks = BkmvtiFransaBanks.stream()
		            .filter(Objects::nonNull)
		            .collect(Collectors.toList());

		    if (!nonNullBkmvtiFransaBanks.isEmpty()) {
		        bkmvtiFransaBankRepository.saveAll(nonNullBkmvtiFransaBanks);
		    }
		    
		}	}

	public void mvbkOffUsAcqConsultationSoldeWissal() {
		logger.info("begin mvbkOffUsAcqConsultationSolde");
		List<DayOperationFransaBank> dayOperationFransaBanks = dayOperationFransaBankRepository
				.getListOffUsAcqConsultationSolde();

		List<BkmvtiFransaBank> BkmvtiFransaBanks = new ArrayList<BkmvtiFransaBank>();

		List<MvbkSettlement> allMvbkSettelemnts = mvbkSettlementRepository.findByIdentificationOffUsAcqCSolde();
		List<MvbkConf> mvbks = mvbkConfigR.findByCategorie(12);

		for (DayOperationFransaBank day : dayOperationFransaBanks) {
			int indexPieceComptable = getEveIndex1();
			List<DayOperationFransaBank> dayOperationFransaBanksByNumTransaction = dayOperationFransaBankRepository
					.getListOnUsMemeAgenceByNumTransaction(day.getNumRefTransaction(), day.getNumAutorisation(),
							day.getNumCartePorteur());

			for (MvbkConf mvk : mvbks) {
				int index2 = getEveIndex();
				BkmvtiFransaBanks = TestSigne(11, mvk, dayOperationFransaBanksByNumTransaction, BkmvtiFransaBanks,
						indexPieceComptable, index2, mvk.getCodeSettlement());

			}
			updateDays(11, indexPieceComptable, dayOperationFransaBanksByNumTransaction, mvbks.get(0));

		}
		logger.info("end mvbkOffUsAcqConsultationSolde =>{}", BkmvtiFransaBanks.size());
		if (BkmvtiFransaBanks != null && !BkmvtiFransaBanks.isEmpty()) {
		    // Filter out null elements from the list
		    List<BkmvtiFransaBank> nonNullBkmvtiFransaBanks = BkmvtiFransaBanks.stream()
		            .filter(Objects::nonNull)
		            .collect(Collectors.toList());

		    if (!nonNullBkmvtiFransaBanks.isEmpty()) {
		        bkmvtiFransaBankRepository.saveAll(nonNullBkmvtiFransaBanks);
		    }
		    
		}	}

	// ******** REMBOURSSEMENT TPE Wissal *****//
	public void mvbkONUdMemeAgenceCHRet() {
		logger.info("begin mvbkONUdMemeAgenceCHRet");
		List<DayOperationFransaBank> dayOperationFransaBanks = dayOperationFransaBankRepository
				.findByIdenficationIn(Arrays.asList("G206"));

		List<BkmvtiFransaBank> BkmvtiFransaBanks = new ArrayList<BkmvtiFransaBank>();
		List<MvbkConf> mvbks = mvbkConfigR.findByCategorie(90);

		for (DayOperationFransaBank day : dayOperationFransaBanks) {
			int indexPieceComptable = getEveIndex1();
			List<DayOperationFransaBank> dayOperationFransaBanksByNumTransaction = dayOperationFransaBankRepository
					.getListOnUsMemeAgenceByNumTransaction(day.getNumRefTransaction(), day.getNumAutorisation(),
							day.getNumCartePorteur());

			for (MvbkConf mvk : mvbks) {
				int index2 = getEveIndex();
				BkmvtiFransaBanks = TestSigne(12, mvk, dayOperationFransaBanksByNumTransaction, BkmvtiFransaBanks,
						indexPieceComptable, index2, mvk.getCodeSettlement());

			}
			updateDays(12, indexPieceComptable, dayOperationFransaBanksByNumTransaction, mvbks.get(0));

		}
		logger.info("end mvbkONUdMemeAgenceCHRet =>{}", BkmvtiFransaBanks.size());
		if (BkmvtiFransaBanks != null && !BkmvtiFransaBanks.isEmpty()) {
		    // Filter out null elements from the list
		    List<BkmvtiFransaBank> nonNullBkmvtiFransaBanks = BkmvtiFransaBanks.stream()
		            .filter(Objects::nonNull)
		            .collect(Collectors.toList());

		    if (!nonNullBkmvtiFransaBanks.isEmpty()) {
		        bkmvtiFransaBankRepository.saveAll(nonNullBkmvtiFransaBanks);
		    }
		    
		}	}
	// ******** REMBOURSSEMENT TPE Wissal *****//
	public void mvbkONUdAutreAgenceCHRet() {
		logger.info("begin mvbkONUdAutreAgenceCHRet");
		List<DayOperationFransaBank> dayOperationFransaBanks = dayOperationFransaBankRepository
				.findByIdenficationIn(Arrays.asList("G200"));

		List<BkmvtiFransaBank> BkmvtiFransaBanks = new ArrayList<BkmvtiFransaBank>();
		List<MvbkConf> mvbks = mvbkConfigR.findByCategorie(91);

		for (DayOperationFransaBank day : dayOperationFransaBanks) {
			int indexPieceComptable = getEveIndex1();
			List<DayOperationFransaBank> dayOperationFransaBanksByNumTransaction = dayOperationFransaBankRepository
					.getListOnUsMemeAgenceByNumTransaction(day.getNumRefTransaction(), day.getNumAutorisation(),
							day.getNumCartePorteur());

			for (MvbkConf mvk : mvbks) {
				int index2 = getEveIndex();
				BkmvtiFransaBanks = TestSigne(12, mvk, dayOperationFransaBanksByNumTransaction, BkmvtiFransaBanks,
						indexPieceComptable, index2, mvk.getCodeSettlement());

			}
			updateDays(12, indexPieceComptable, dayOperationFransaBanksByNumTransaction, mvbks.get(0));

		}
		logger.info("end mvbkONUdAutreAgenceCHRet =>{}", BkmvtiFransaBanks.size());
		if (BkmvtiFransaBanks != null && !BkmvtiFransaBanks.isEmpty()) {
		    // Filter out null elements from the list
		    List<BkmvtiFransaBank> nonNullBkmvtiFransaBanks = BkmvtiFransaBanks.stream()
		            .filter(Objects::nonNull)
		            .collect(Collectors.toList());

		    if (!nonNullBkmvtiFransaBanks.isEmpty()) {
		        bkmvtiFransaBankRepository.saveAll(nonNullBkmvtiFransaBanks);
		    }

		}	}
	// ******** REMBOURSSEMENT TPE Wissal *****//

	// ******** REMBOURSSEMENT TPE Wissal *****//
	public void mvbkONUdMemeAgenceCHACHAT() {
		logger.info("begin mvbkONUdMemeAgenceCHACHAT");
		List<DayOperationFransaBank> dayOperationFransaBanks = dayOperationFransaBankRepository
				.findByIdenficationIn(Arrays.asList("G207"));

		List<BkmvtiFransaBank> BkmvtiFransaBanks = new ArrayList<BkmvtiFransaBank>();
		List<MvbkConf> mvbks = mvbkConfigR.findByCategorie(92);

		for (DayOperationFransaBank day : dayOperationFransaBanks) {
			int indexPieceComptable = getEveIndex1();
			List<DayOperationFransaBank> dayOperationFransaBanksByNumTransaction = dayOperationFransaBankRepository
					.getListOnUsMemeAgenceByNumTransaction(day.getNumRefTransaction(), day.getNumAutorisation(),
							day.getNumCartePorteur());

			for (MvbkConf mvk : mvbks) {
				int index2 = getEveIndex();
				BkmvtiFransaBanks = TestSigne(12, mvk, dayOperationFransaBanksByNumTransaction, BkmvtiFransaBanks,
						indexPieceComptable, index2, mvk.getCodeSettlement());

			}
			updateDays(12, indexPieceComptable, dayOperationFransaBanksByNumTransaction, mvbks.get(0));

		}
		logger.info("end mvbkONUdMemeAgenceCHACHAT =>{}", BkmvtiFransaBanks.size());
		if (BkmvtiFransaBanks != null && !BkmvtiFransaBanks.isEmpty()) {
		    // Filter out null elements from the list
		    List<BkmvtiFransaBank> nonNullBkmvtiFransaBanks = BkmvtiFransaBanks.stream()
		            .filter(Objects::nonNull)
		            .collect(Collectors.toList());

		    if (!nonNullBkmvtiFransaBanks.isEmpty()) {
		        bkmvtiFransaBankRepository.saveAll(nonNullBkmvtiFransaBanks);
		    }

		}	}
	// ******** REMBOURSSEMENT TPE Wissal *****//
	public void mvbkONUdAutreAgenceCHACHAT() {
		logger.info("begin mvbkONUdAutreAgenceCHACHAT");
		List<DayOperationFransaBank> dayOperationFransaBanks = dayOperationFransaBankRepository
				.findByIdenficationIn(Arrays.asList("G201"));

		List<BkmvtiFransaBank> BkmvtiFransaBanks = new ArrayList<BkmvtiFransaBank>();
		List<MvbkConf> mvbks = mvbkConfigR.findByCategorie(93);

		for (DayOperationFransaBank day : dayOperationFransaBanks) {
			int indexPieceComptable = getEveIndex1();
			List<DayOperationFransaBank> dayOperationFransaBanksByNumTransaction = dayOperationFransaBankRepository
					.getListOnUsMemeAgenceByNumTransaction(day.getNumRefTransaction(), day.getNumAutorisation(),
							day.getNumCartePorteur());

			for (MvbkConf mvk : mvbks) {
				int index2 = getEveIndex();
				BkmvtiFransaBanks = TestSigne(12, mvk, dayOperationFransaBanksByNumTransaction, BkmvtiFransaBanks,
						indexPieceComptable, index2, mvk.getCodeSettlement());

			}
			updateDays(12, indexPieceComptable, dayOperationFransaBanksByNumTransaction, mvbks.get(0));

		}
		logger.info("end mvbkONUdAutreAgenceCHACHAT =>{}", BkmvtiFransaBanks.size());
		if (BkmvtiFransaBanks != null && !BkmvtiFransaBanks.isEmpty()) {
		    // Filter out null elements from the list
		    List<BkmvtiFransaBank> nonNullBkmvtiFransaBanks = BkmvtiFransaBanks.stream()
		            .filter(Objects::nonNull)
		            .collect(Collectors.toList());

		    if (!nonNullBkmvtiFransaBanks.isEmpty()) {
		        bkmvtiFransaBankRepository.saveAll(nonNullBkmvtiFransaBanks);
		    }

		}	}
	// ******** REMBOURSSEMENT TPE Wissal *****//

	// ******** REMBOURSSEMENT TPE Wissal *****//
	public void mvbkIssuerCHRET() {
		logger.info("begin mvbkIssuerCHRET");
		List<DayOperationFransaBank> dayOperationFransaBanks = dayOperationFransaBankRepository
				.findByIdenficationIn(Arrays.asList("G202"));

		List<BkmvtiFransaBank> BkmvtiFransaBanks = new ArrayList<BkmvtiFransaBank>();
		List<MvbkConf> mvbks = mvbkConfigR.findByCategorie(96);

		for (DayOperationFransaBank day : dayOperationFransaBanks) {
			int indexPieceComptable = getEveIndex1();
			List<DayOperationFransaBank> dayOperationFransaBanksByNumTransaction = dayOperationFransaBankRepository
					.getListOnUsMemeAgenceByNumTransaction(day.getNumRefTransaction(), day.getNumAutorisation(),
							day.getNumCartePorteur());

			for (MvbkConf mvk : mvbks) {
				int index2 = getEveIndex();
				BkmvtiFransaBanks = TestSigne(12, mvk, dayOperationFransaBanksByNumTransaction, BkmvtiFransaBanks,
						indexPieceComptable, index2, mvk.getCodeSettlement());

			}
			updateDays(12, indexPieceComptable, dayOperationFransaBanksByNumTransaction, mvbks.get(0));

		}
		logger.info("end mvbkIssuerCHRET =>{}", BkmvtiFransaBanks.size());
		if (BkmvtiFransaBanks != null && !BkmvtiFransaBanks.isEmpty()) {
		    // Filter out null elements from the list
		    List<BkmvtiFransaBank> nonNullBkmvtiFransaBanks = BkmvtiFransaBanks.stream()
		            .filter(Objects::nonNull)
		            .collect(Collectors.toList());

		    if (!nonNullBkmvtiFransaBanks.isEmpty()) {
		        bkmvtiFransaBankRepository.saveAll(nonNullBkmvtiFransaBanks);
		    }

		}	}
	// ******** REMBOURSSEMENT TPE Wissal *****//
	public void mvbkACQCHRET() {
		logger.info("begin mvbkACQCHRET");
		List<DayOperationFransaBank> dayOperationFransaBanks = dayOperationFransaBankRepository
				.findByIdenficationIn(Arrays.asList("G204"));

		List<BkmvtiFransaBank> BkmvtiFransaBanks = new ArrayList<BkmvtiFransaBank>();
		List<MvbkConf> mvbks = mvbkConfigR.findByCategorie(94);

		for (DayOperationFransaBank day : dayOperationFransaBanks) {
			int indexPieceComptable = getEveIndex1();
			List<DayOperationFransaBank> dayOperationFransaBanksByNumTransaction = dayOperationFransaBankRepository
					.getListOnUsMemeAgenceByNumTransaction(day.getNumRefTransaction(), day.getNumAutorisation(),
							day.getNumCartePorteur());

			for (MvbkConf mvk : mvbks) {
				int index2 = getEveIndex();
				BkmvtiFransaBanks = TestSigne(12, mvk, dayOperationFransaBanksByNumTransaction, BkmvtiFransaBanks,
						indexPieceComptable, index2, mvk.getCodeSettlement());

			}
			updateDays(12, indexPieceComptable, dayOperationFransaBanksByNumTransaction, mvbks.get(0));

		}
		logger.info("end mvbkACQCHRET =>{}", BkmvtiFransaBanks.size());
		if (BkmvtiFransaBanks != null && !BkmvtiFransaBanks.isEmpty()) {
		    // Filter out null elements from the list
		    List<BkmvtiFransaBank> nonNullBkmvtiFransaBanks = BkmvtiFransaBanks.stream()
		            .filter(Objects::nonNull)
		            .collect(Collectors.toList());

		    if (!nonNullBkmvtiFransaBanks.isEmpty()) {
		        bkmvtiFransaBankRepository.saveAll(nonNullBkmvtiFransaBanks);
		    }

		}	}
	// ******** REMBOURSSEMENT TPE Wissal *****//

	// ******** REMBOURSSEMENT TPE Wissal *****//
	public void mvbkIssuerCHACHAT() {
		logger.info("begin mvbkIssuerCHACHAT");
		List<DayOperationFransaBank> dayOperationFransaBanks = dayOperationFransaBankRepository
				.findByIdenficationIn(Arrays.asList("G203"));

		List<BkmvtiFransaBank> BkmvtiFransaBanks = new ArrayList<BkmvtiFransaBank>();
		List<MvbkConf> mvbks = mvbkConfigR.findByCategorie(180);

		for (DayOperationFransaBank day : dayOperationFransaBanks) {
			int indexPieceComptable = getEveIndex1();
			List<DayOperationFransaBank> dayOperationFransaBanksByNumTransaction = dayOperationFransaBankRepository
					.getListOnUsMemeAgenceByNumTransaction(day.getNumRefTransaction(), day.getNumAutorisation(),
							day.getNumCartePorteur());

			for (MvbkConf mvk : mvbks) {
				int index2 = getEveIndex();
				BkmvtiFransaBanks = TestSigne(12, mvk, dayOperationFransaBanksByNumTransaction, BkmvtiFransaBanks,
						indexPieceComptable, index2, mvk.getCodeSettlement());

			}
			updateDays(12, indexPieceComptable, dayOperationFransaBanksByNumTransaction, mvbks.get(0));

		}
		logger.info("end mvbkIssuerCHACHAT =>{}", BkmvtiFransaBanks.size());
		if (BkmvtiFransaBanks != null && !BkmvtiFransaBanks.isEmpty()) {
		    // Filter out null elements from the list
		    List<BkmvtiFransaBank> nonNullBkmvtiFransaBanks = BkmvtiFransaBanks.stream()
		            .filter(Objects::nonNull)
		            .collect(Collectors.toList());

		    if (!nonNullBkmvtiFransaBanks.isEmpty()) {
		        bkmvtiFransaBankRepository.saveAll(nonNullBkmvtiFransaBanks);
		    }

		}	}
	// ******** REMBOURSSEMENT TPE Wissal *****//
	public void mvbkACQCHACHAT() {
		logger.info("begin mvbkACQCHACHAT");
		List<DayOperationFransaBank> dayOperationFransaBanks = dayOperationFransaBankRepository
				.findByIdenficationIn(Arrays.asList("G205"));

		List<BkmvtiFransaBank> BkmvtiFransaBanks = new ArrayList<BkmvtiFransaBank>();
		List<MvbkConf> mvbks = mvbkConfigR.findByCategorie(98);

		for (DayOperationFransaBank day : dayOperationFransaBanks) {
			int indexPieceComptable = getEveIndex1();
			List<DayOperationFransaBank> dayOperationFransaBanksByNumTransaction = dayOperationFransaBankRepository
					.getListOnUsMemeAgenceByNumTransaction(day.getNumRefTransaction(), day.getNumAutorisation(),
							day.getNumCartePorteur());

			for (MvbkConf mvk : mvbks) {
				int index2 = getEveIndex();
				BkmvtiFransaBanks = TestSigne(12, mvk, dayOperationFransaBanksByNumTransaction, BkmvtiFransaBanks,
						indexPieceComptable, index2, mvk.getCodeSettlement());

			}
			updateDays(12, indexPieceComptable, dayOperationFransaBanksByNumTransaction, mvbks.get(0));

		}
		logger.info("end mvbkACQCHACHAT =>{}", BkmvtiFransaBanks.size());
		if (BkmvtiFransaBanks != null && !BkmvtiFransaBanks.isEmpty()) {
		    // Filter out null elements from the list
		    List<BkmvtiFransaBank> nonNullBkmvtiFransaBanks = BkmvtiFransaBanks.stream()
		            .filter(Objects::nonNull)
		            .collect(Collectors.toList());

		    if (!nonNullBkmvtiFransaBanks.isEmpty()) {
		        bkmvtiFransaBankRepository.saveAll(nonNullBkmvtiFransaBanks);
		    }

		}	}
	// ******** REMBOURSSEMENT TPE Wissal *****//

	// ******** REMBOURSSEMENT TPE Wissal *****//
	public void mvbkONUdMemeAgenceRembourssementTpeWissal() {
		logger.info("begin mvbkONUdMemeAgenceRembourssementTpe");
		List<DayOperationFransaBank> dayOperationFransaBanks = dayOperationFransaBankRepository
				.getListOnusRembourssementTpe();

		List<BkmvtiFransaBank> BkmvtiFransaBanks = new ArrayList<BkmvtiFransaBank>();
		List<MvbkConf> mvbks = mvbkConfigR.findByCategorie(15);

		for (DayOperationFransaBank day : dayOperationFransaBanks) {
			int indexPieceComptable = getEveIndex1();
			List<DayOperationFransaBank> dayOperationFransaBanksByNumTransaction = dayOperationFransaBankRepository
					.getListOnUsMemeAgenceByNumTransaction(day.getNumRefTransaction(), day.getNumAutorisation(),
							day.getNumCartePorteur());

			for (MvbkConf mvk : mvbks) {
				int index2 = getEveIndex();
				BkmvtiFransaBanks = TestSigne(12, mvk, dayOperationFransaBanksByNumTransaction, BkmvtiFransaBanks,
						indexPieceComptable, index2, mvk.getCodeSettlement());

			}
			updateDays(12, indexPieceComptable, dayOperationFransaBanksByNumTransaction, mvbks.get(0));

		}
		logger.info("end mvbkONUdMemeAgenceRembourssementTpe =>{}", BkmvtiFransaBanks.size());
		if (BkmvtiFransaBanks != null && !BkmvtiFransaBanks.isEmpty()) {
		    // Filter out null elements from the list
		    List<BkmvtiFransaBank> nonNullBkmvtiFransaBanks = BkmvtiFransaBanks.stream()
		            .filter(Objects::nonNull)
		            .collect(Collectors.toList());

		    if (!nonNullBkmvtiFransaBanks.isEmpty()) {
		        bkmvtiFransaBankRepository.saveAll(nonNullBkmvtiFransaBanks);
		    }

		}	}
	// ******** REMBOURSSEMENT TPE Wissal *****//

	public void mvbkONUdAutreAgenceRembourssementTpeWissal() {
		logger.info("begin mvbkONUdAutreAgenceRembourssementTpe");
		List<DayOperationFransaBank> dayOperationFransaBanks = dayOperationFransaBankRepository
				.getListOnusAutreAgenceRembourssementTpe();

		List<BkmvtiFransaBank> BkmvtiFransaBanks = new ArrayList<BkmvtiFransaBank>();

		List<MvbkConf> mvbks = mvbkConfigR.findByCategorie(16);

		for (DayOperationFransaBank day : dayOperationFransaBanks) {
			int indexPieceComptable = getEveIndex1();
			List<DayOperationFransaBank> dayOperationFransaBanksByNumTransaction = dayOperationFransaBankRepository
					.getListOnUsMemeAgenceByNumTransaction(day.getNumRefTransaction(), day.getNumAutorisation(),
							day.getNumCartePorteur());

			for (MvbkConf mvk : mvbks) {
				int index2 = getEveIndex();
				BkmvtiFransaBanks = TestSigne(12, mvk, dayOperationFransaBanksByNumTransaction, BkmvtiFransaBanks,
						indexPieceComptable, index2, mvk.getCodeSettlement());

			}
			updateDays(12, indexPieceComptable, dayOperationFransaBanksByNumTransaction, mvbks.get(0));

		}
		logger.info("end mvbkONUdAutreAgenceRembourssementTpe=>{}", BkmvtiFransaBanks.size());
		if (BkmvtiFransaBanks != null && !BkmvtiFransaBanks.isEmpty()) {
		    // Filter out null elements from the list
		    List<BkmvtiFransaBank> nonNullBkmvtiFransaBanks = BkmvtiFransaBanks.stream()
		            .filter(Objects::nonNull)
		            .collect(Collectors.toList());

		    if (!nonNullBkmvtiFransaBanks.isEmpty()) {
		        bkmvtiFransaBankRepository.saveAll(nonNullBkmvtiFransaBanks);
		    }
		    
		}	}

	public void mvbkOffUsIssuerRembourssementTpeWissal() {
		logger.info("begin mvbkOffUsIssuerRembourssementTpe");
		List<DayOperationFransaBank> dayOperationFransaBanks = dayOperationFransaBankRepository
				.getListOffUsIssuerRembourssementTpe();

		List<BkmvtiFransaBank> BkmvtiFransaBanks = new ArrayList<BkmvtiFransaBank>();

		List<MvbkConf> mvbks = mvbkConfigR.findByCategorie(17);

		for (DayOperationFransaBank day : dayOperationFransaBanks) {
			int indexPieceComptable = getEveIndex1();
			List<DayOperationFransaBank> dayOperationFransaBanksByNumTransaction = dayOperationFransaBankRepository
					.getListOnUsMemeAgenceByNumTransaction(day.getNumRefTransaction(), day.getNumAutorisation(),
							day.getNumCartePorteur());

			for (MvbkConf mvk : mvbks) {
				int index2 = getEveIndex();
				BkmvtiFransaBanks = TestSigne(13, mvk, dayOperationFransaBanksByNumTransaction, BkmvtiFransaBanks,
						indexPieceComptable, index2, mvk.getCodeSettlement());

			}
			updateDays(13, indexPieceComptable, dayOperationFransaBanksByNumTransaction, mvbks.get(0));

		}
		logger.info("end mvbkOffUsIssuerRembourssementTpe =>{}", BkmvtiFransaBanks.size());
		if (BkmvtiFransaBanks != null && !BkmvtiFransaBanks.isEmpty()) {
		    // Filter out null elements from the list
		    List<BkmvtiFransaBank> nonNullBkmvtiFransaBanks = BkmvtiFransaBanks.stream()
		            .filter(Objects::nonNull)
		            .collect(Collectors.toList());

		    if (!nonNullBkmvtiFransaBanks.isEmpty()) {
		        bkmvtiFransaBankRepository.saveAll(nonNullBkmvtiFransaBanks);
		    }
		    
		}	}

	public void mvbkOffUsAcqRembourssementTpeWissal() {
		logger.info("begin mvbkOffUsAcqRembourssementTpe");
		List<DayOperationFransaBank> dayOperationFransaBanks = dayOperationFransaBankRepository
				.getListOffUsAcqRembourssementTpe();

		List<BkmvtiFransaBank> BkmvtiFransaBanks = new ArrayList<BkmvtiFransaBank>();

		List<MvbkConf> mvbks = mvbkConfigR.findByCategorie(18);

		for (DayOperationFransaBank day : dayOperationFransaBanks) {
			int indexPieceComptable = getEveIndex1();
			List<DayOperationFransaBank> dayOperationFransaBanksByNumTransaction = dayOperationFransaBankRepository
					.getListOnUsMemeAgenceByNumTransaction(day.getNumRefTransaction(), day.getNumAutorisation(),
							day.getNumCartePorteur());

			for (MvbkConf mvk : mvbks) {
				int index2 = getEveIndex();
				BkmvtiFransaBanks = TestSigne(14, mvk, dayOperationFransaBanksByNumTransaction, BkmvtiFransaBanks,
						indexPieceComptable, index2, mvk.getCodeSettlement());

			}
			updateDays(14, indexPieceComptable, dayOperationFransaBanksByNumTransaction, mvbks.get(0));

		}
		logger.info("end mvbkOffUsAcqRembourssementTpe =>{}", BkmvtiFransaBanks.size());
		if (BkmvtiFransaBanks != null && !BkmvtiFransaBanks.isEmpty()) {
		    // Filter out null elements from the list
		    List<BkmvtiFransaBank> nonNullBkmvtiFransaBanks = BkmvtiFransaBanks.stream()
		            .filter(Objects::nonNull)
		            .collect(Collectors.toList());

		    if (!nonNullBkmvtiFransaBanks.isEmpty()) {
		        bkmvtiFransaBankRepository.saveAll(nonNullBkmvtiFransaBanks);
		    }
		    
		}	}

	// ****** REmbourssement Internet Wissal ****//
	public void mvbkONUdMemeAgenceRembourssementInternetWissal() {
		logger.info("mvbkONUdMemeAgenceRembourssementInternet");
		List<DayOperationFransaBank> dayOperationFransaBanks = dayOperationFransaBankRepository
				.getListOnusRembourssementInternet();

		List<BkmvtiFransaBank> BkmvtiFransaBanks = new ArrayList<BkmvtiFransaBank>();

		List<MvbkConf> mvbks = mvbkConfigR.findByCategorie(2015);

		for (DayOperationFransaBank day : dayOperationFransaBanks) {
			int indexPieceComptable = getEveIndex1();
			List<DayOperationFransaBank> dayOperationFransaBanksByNumTransaction = dayOperationFransaBankRepository
					.getListOnUsMemeAgenceByNumTransaction(day.getNumRefTransaction(), day.getNumAutorisation(),
							day.getNumCartePorteur());

			for (MvbkConf mvk : mvbks) {
				int index2 = getEveIndex();
				BkmvtiFransaBanks = TestSigne(22, mvk, dayOperationFransaBanksByNumTransaction, BkmvtiFransaBanks,
						indexPieceComptable, index2, mvk.getCodeSettlement());

			}
			updateDays(22, indexPieceComptable, dayOperationFransaBanksByNumTransaction, mvbks.get(0));

		}
		logger.info("end mvbkONUdMemeAgenceRembourssementInternet =>{}", BkmvtiFransaBanks.size());
		if (BkmvtiFransaBanks != null && !BkmvtiFransaBanks.isEmpty()) {
		    // Filter out null elements from the list
		    List<BkmvtiFransaBank> nonNullBkmvtiFransaBanks = BkmvtiFransaBanks.stream()
		            .filter(Objects::nonNull)
		            .collect(Collectors.toList());

		    if (!nonNullBkmvtiFransaBanks.isEmpty()) {
		        bkmvtiFransaBankRepository.saveAll(nonNullBkmvtiFransaBanks);
		    }
		    
		}	}

	public void mvbkONUdAutreAgenceRembourssementInternetWissal() {
		logger.info("mvbkONUdAutreAgenceRembourssementInternet");
		List<DayOperationFransaBank> dayOperationFransaBanks = dayOperationFransaBankRepository
				.getListOnusAutreAgenceRembourssementInternet();

		List<BkmvtiFransaBank> BkmvtiFransaBanks = new ArrayList<BkmvtiFransaBank>();

		List<MvbkConf> mvbks = mvbkConfigR.findByCategorie(2016);

		for (DayOperationFransaBank day : dayOperationFransaBanks) {
			int indexPieceComptable = getEveIndex1();
			List<DayOperationFransaBank> dayOperationFransaBanksByNumTransaction = dayOperationFransaBankRepository
					.getListOnUsMemeAgenceByNumTransaction(day.getNumRefTransaction(), day.getNumAutorisation(),
							day.getNumCartePorteur());

			for (MvbkConf mvk : mvbks) {
				int index2 = getEveIndex();
				BkmvtiFransaBanks = TestSigne(22, mvk, dayOperationFransaBanksByNumTransaction, BkmvtiFransaBanks,
						indexPieceComptable, index2, mvk.getCodeSettlement());

			}
			updateDays(22, indexPieceComptable, dayOperationFransaBanksByNumTransaction, mvbks.get(0));

		}
		logger.info("end mvbkONUdAutreAgenceRembourssementInternet =>{}", BkmvtiFransaBanks.size());
		if (BkmvtiFransaBanks != null && !BkmvtiFransaBanks.isEmpty()) {
		    // Filter out null elements from the list
		    List<BkmvtiFransaBank> nonNullBkmvtiFransaBanks = BkmvtiFransaBanks.stream()
		            .filter(Objects::nonNull)
		            .collect(Collectors.toList());

		    if (!nonNullBkmvtiFransaBanks.isEmpty()) {
		        bkmvtiFransaBankRepository.saveAll(nonNullBkmvtiFransaBanks);
		    }
		    
		}	}

	public void mvbkOffUsIssuerRembourssementInternetWissal() {
		logger.info("mvbkOffUsIssuerRembourssementInternet");
		List<DayOperationFransaBank> dayOperationFransaBanks = dayOperationFransaBankRepository
				.getListOffUsIssuerRembourssementInternet();

		List<BkmvtiFransaBank> BkmvtiFransaBanks = new ArrayList<BkmvtiFransaBank>();

		List<MvbkConf> mvbks = mvbkConfigR.findByCategorie(2017);

		for (DayOperationFransaBank day : dayOperationFransaBanks) {
			int indexPieceComptable = getEveIndex1();
			List<DayOperationFransaBank> dayOperationFransaBanksByNumTransaction = dayOperationFransaBankRepository
					.getListOnUsMemeAgenceByNumTransaction(day.getNumRefTransaction(), day.getNumAutorisation(),
							day.getNumCartePorteur());

			for (MvbkConf mvk : mvbks) {
				int index2 = getEveIndex();
				BkmvtiFransaBanks = TestSigne(23, mvk, dayOperationFransaBanksByNumTransaction, BkmvtiFransaBanks,
						indexPieceComptable, index2, mvk.getCodeSettlement());

			}
			updateDays(23, indexPieceComptable, dayOperationFransaBanksByNumTransaction, mvbks.get(0));

		}
		logger.info("end mvbkOffUsIssuerRembourssementInternet =>{}", BkmvtiFransaBanks.size());
		if (BkmvtiFransaBanks != null && !BkmvtiFransaBanks.isEmpty()) {
		    // Filter out null elements from the list
		    List<BkmvtiFransaBank> nonNullBkmvtiFransaBanks = BkmvtiFransaBanks.stream()
		            .filter(Objects::nonNull)
		            .collect(Collectors.toList());

		    if (!nonNullBkmvtiFransaBanks.isEmpty()) {
		        bkmvtiFransaBankRepository.saveAll(nonNullBkmvtiFransaBanks);
		    }
		    
		}	}

	public void mvbkOffUsAcqRembourssementInternetWissal() {
		logger.info("mvbkOffUsAcqRembourssementInternet");
		List<DayOperationFransaBank> dayOperationFransaBanks = dayOperationFransaBankRepository
				.getListOffUsAcqRembourssementInternet();

		List<BkmvtiFransaBank> BkmvtiFransaBanks = new ArrayList<BkmvtiFransaBank>();

		List<MvbkConf> mvbks = mvbkConfigR.findByCategorie(2018);
		for (DayOperationFransaBank day : dayOperationFransaBanks) {
			int indexPieceComptable = getEveIndex1();
			List<DayOperationFransaBank> dayOperationFransaBanksByNumTransaction = dayOperationFransaBankRepository
					.getListOnUsMemeAgenceByNumTransaction(day.getNumRefTransaction(), day.getNumAutorisation(),
							day.getNumCartePorteur());

			for (MvbkConf mvk : mvbks) {
				int index2 = getEveIndex();
				BkmvtiFransaBanks = TestSigne(24, mvk, dayOperationFransaBanksByNumTransaction, BkmvtiFransaBanks,
						indexPieceComptable, index2, mvk.getCodeSettlement());

			}
			updateDays(24, indexPieceComptable, dayOperationFransaBanksByNumTransaction, mvbks.get(0));

		}
		logger.info("end mvbkOffUsAcqRembourssementInternet=>{}", BkmvtiFransaBanks.size());
		if (BkmvtiFransaBanks != null && !BkmvtiFransaBanks.isEmpty()) {
		    // Filter out null elements from the list
		    List<BkmvtiFransaBank> nonNullBkmvtiFransaBanks = BkmvtiFransaBanks.stream()
		            .filter(Objects::nonNull)
		            .collect(Collectors.toList());

		    if (!nonNullBkmvtiFransaBanks.isEmpty()) {
		        bkmvtiFransaBankRepository.saveAll(nonNullBkmvtiFransaBanks);
		    }
		    
		}	}

	// ****** Achat internet wissal ******//

	public void mvbkOnUsMemeAgenceAchatInternetWissal() {
		logger.info("begin mvbkOnUsMemeAgenceAchatInternet");
		List<DayOperationFransaBank> dayOperationFransaBanks = dayOperationFransaBankRepository
				.getListOnUsMemeAgenceAchatInternet();
		List<BkmvtiFransaBank> BkmvtiFransaBanks = new ArrayList<BkmvtiFransaBank>();
		List<MvbkConf> mvbks = mvbkConfigR.findByCategorie(1005);
		for (DayOperationFransaBank day : dayOperationFransaBanks) {
			int indexPieceComptable = getEveIndex1();
			List<DayOperationFransaBank> dayOperationFransaBanksByNumTransaction = dayOperationFransaBankRepository
					.getListOnUsMemeAgenceByNumTransaction(day.getNumRefTransaction(), day.getNumAutorisation(),
							day.getNumCartePorteur());

			for (MvbkConf mvk : mvbks) {
				int index2 = getEveIndex();
				BkmvtiFransaBanks = TestSigne(17, mvk, dayOperationFransaBanksByNumTransaction, BkmvtiFransaBanks,
						indexPieceComptable, index2, mvk.getCodeSettlement());

			}
			updateDays(17, indexPieceComptable, dayOperationFransaBanksByNumTransaction, mvbks.get(0));

		}
		logger.info("end mvbkOnUsMemeAgenceAchatInternet =>{}", BkmvtiFransaBanks.size());
		if (BkmvtiFransaBanks != null && !BkmvtiFransaBanks.isEmpty()) {
		    // Filter out null elements from the list
		    List<BkmvtiFransaBank> nonNullBkmvtiFransaBanks = BkmvtiFransaBanks.stream()
		            .filter(Objects::nonNull)
		            .collect(Collectors.toList());

		    if (!nonNullBkmvtiFransaBanks.isEmpty()) {
		        bkmvtiFransaBankRepository.saveAll(nonNullBkmvtiFransaBanks);
		    }
		    
		}	}

	public void mvbkOnUsAutreAgenceAchatInternetWissal() {
		logger.info("begin mvbkOnUsAutreAgenceAchatInternet");
		List<DayOperationFransaBank> dayOperationFransaBanks = dayOperationFransaBankRepository
				.getListOnUsAutreAgenceAchatInternet();
		List<BkmvtiFransaBank> BkmvtiFransaBanks = new ArrayList<BkmvtiFransaBank>();

		List<MvbkConf> mvbks = mvbkConfigR.findByCategorie(1006);

		for (DayOperationFransaBank day : dayOperationFransaBanks) {
			int indexPieceComptable = getEveIndex1();
			List<DayOperationFransaBank> dayOperationFransaBanksByNumTransaction = dayOperationFransaBankRepository
					.getListOnUsMemeAgenceByNumTransaction(day.getNumRefTransaction(), day.getNumAutorisation(),
							day.getNumCartePorteur());

			for (MvbkConf mvk : mvbks) {
				int index2 = getEveIndex();
				BkmvtiFransaBanks = TestSigne(17, mvk, dayOperationFransaBanksByNumTransaction, BkmvtiFransaBanks,
						indexPieceComptable, index2, mvk.getCodeSettlement());

			}
			updateDays(17, indexPieceComptable, dayOperationFransaBanksByNumTransaction, mvbks.get(0));

		}
		logger.info("end mvbkOnUsAutreAgenceAchatInternet=>{}", BkmvtiFransaBanks.size());
		if (BkmvtiFransaBanks != null && !BkmvtiFransaBanks.isEmpty()) {
		    // Filter out null elements from the list
		    List<BkmvtiFransaBank> nonNullBkmvtiFransaBanks = BkmvtiFransaBanks.stream()
		            .filter(Objects::nonNull)
		            .collect(Collectors.toList());

		    if (!nonNullBkmvtiFransaBanks.isEmpty()) {
		        bkmvtiFransaBankRepository.saveAll(nonNullBkmvtiFransaBanks);
		    }
		    
		}	}

	public void mvbkOffUsIssuerAchatInternetWissal() {
		logger.info("begin mvbkOffUsIssuerAchatInternet");
		List<DayOperationFransaBank> dayOperationFransaBanks = dayOperationFransaBankRepository
				.getListOffUsIssuerAchatInternet();
		List<BkmvtiFransaBank> BkmvtiFransaBanks = new ArrayList<BkmvtiFransaBank>();

		List<MvbkConf> mvbks = mvbkConfigR.findByCategorie(1007);

		for (DayOperationFransaBank day : dayOperationFransaBanks) {
			int indexPieceComptable = getEveIndex1();
			List<DayOperationFransaBank> dayOperationFransaBanksByNumTransaction = dayOperationFransaBankRepository
					.getListOnUsMemeAgenceByNumTransaction(day.getNumRefTransaction(), day.getNumAutorisation(),
							day.getNumCartePorteur());

			for (MvbkConf mvk : mvbks) {
				int index2 = getEveIndex();
				BkmvtiFransaBanks = TestSigne(18, mvk, dayOperationFransaBanksByNumTransaction, BkmvtiFransaBanks,
						indexPieceComptable, index2, mvk.getCodeSettlement());

			}
			updateDays(18, indexPieceComptable, dayOperationFransaBanksByNumTransaction, mvbks.get(0));

		}
		FileRequest.print("end mvbkOffUsIssuerAchatInternet => "+ BkmvtiFransaBanks.size(), FileRequest.getLineNumber());
		logger.info("end mvbkOffUsIssuerAchatInternet =>{}", BkmvtiFransaBanks.size());
		if (BkmvtiFransaBanks != null && !BkmvtiFransaBanks.isEmpty()) {
		    // Filter out null elements from the list
		    List<BkmvtiFransaBank> nonNullBkmvtiFransaBanks = BkmvtiFransaBanks.stream()
		            .filter(Objects::nonNull)
		            .collect(Collectors.toList());

		    if (!nonNullBkmvtiFransaBanks.isEmpty()) {
		        bkmvtiFransaBankRepository.saveAll(nonNullBkmvtiFransaBanks);
		    }
		    
		}	
		}

	public void mvbkOffUsIssuerAchatInternetWissalExceptionnelMerchant() {
		logger.info("begin mvbkOffUsIssuerAchatInternetExceptionnelMerchant");
		List<DayOperationFransaBank> dayOperationFransaBanks = dayOperationFransaBankRepository
				.getListOffUsIssuerAchatInternetExceptionnelMerchant();
		List<BkmvtiFransaBank> BkmvtiFransaBanks = new ArrayList<BkmvtiFransaBank>();
		List<MvbkConf> mvbks = mvbkConfigR.findByCategorie(100);

		for (DayOperationFransaBank day : dayOperationFransaBanks) {
			int indexPieceComptable = getEveIndex1();
			List<DayOperationFransaBank> dayOperationFransaBanksByNumTransaction = dayOperationFransaBankRepository
					.getListOnUsMemeAgenceByNumTransaction(day.getNumRefTransaction(), day.getNumAutorisation(),
							day.getNumCartePorteur());
//			FileRequest.print("" + dayOperationFransaBanksByNumTransaction.size(), FileRequest.getLineNumber());
			for (MvbkConf mvk : mvbks) {
				int index2 = getEveIndex();
				BkmvtiFransaBanks = TestSigne(18, mvk, dayOperationFransaBanksByNumTransaction, BkmvtiFransaBanks,
						indexPieceComptable, index2, mvk.getCodeSettlement());

			}
			updateDays(18, indexPieceComptable, dayOperationFransaBanksByNumTransaction, mvbks.get(0));

		}
		logger.info("end mvbkOffUsIssuerAchatInternet =>{}", BkmvtiFransaBanks.size());
		if (BkmvtiFransaBanks != null && !BkmvtiFransaBanks.isEmpty()) {
		    // Filter out null elements from the list
		    List<BkmvtiFransaBank> nonNullBkmvtiFransaBanks = BkmvtiFransaBanks.stream()
		            .filter(Objects::nonNull)
		            .collect(Collectors.toList());

		    if (!nonNullBkmvtiFransaBanks.isEmpty()) {
		        bkmvtiFransaBankRepository.saveAll(nonNullBkmvtiFransaBanks);
		    }
		    
		}	}

	public void mvbkOffUsAcqAchatInternetWissal() {
		logger.info("mvbkOffUsAcqAchatInternetWissal =>{}");
		List<DayOperationFransaBank> dayOperationFransaBanks = dayOperationFransaBankRepository
				.getListOffUsAcqAchatInternet();

		List<BkmvtiFransaBank> BkmvtiFransaBanks = new ArrayList<BkmvtiFransaBank>();

		List<MvbkConf> mvbks = mvbkConfigR.findByCategorie(1008);

		for (DayOperationFransaBank day : dayOperationFransaBanks) {
			int indexPieceComptable = getEveIndex1();
			List<DayOperationFransaBank> dayOperationFransaBanksByNumTransaction = dayOperationFransaBankRepository
					.getListOnUsMemeAgenceByNumTransaction(day.getNumRefTransaction(), day.getNumAutorisation(),
							day.getNumCartePorteur());

			for (MvbkConf mvk : mvbks) {
				int index2 = getEveIndex();
				BkmvtiFransaBanks = TestSigne(19, mvk, dayOperationFransaBanksByNumTransaction, BkmvtiFransaBanks,
						indexPieceComptable, index2, mvk.getCodeSettlement());

			}
			updateDays(19, indexPieceComptable, dayOperationFransaBanksByNumTransaction, mvbks.get(0));

		}
		logger.info("end mvbkOffUsAcqAchatInternet=>{}", BkmvtiFransaBanks.size());
		if (BkmvtiFransaBanks != null && !BkmvtiFransaBanks.isEmpty()) {
		    // Filter out null elements from the list
		    List<BkmvtiFransaBank> nonNullBkmvtiFransaBanks = BkmvtiFransaBanks.stream()
		            .filter(Objects::nonNull)
		            .collect(Collectors.toList());

		    if (!nonNullBkmvtiFransaBanks.isEmpty()) {
		        bkmvtiFransaBankRepository.saveAll(nonNullBkmvtiFransaBanks);
		    }
		    
		}	}

	// ****** ACHAT INTERNET ALGERIE POSTE Wissal
	public void mvbkOffUsIssuerAchatInternetAlgeriePosteWissal() {
		logger.info("begin mvbkOffUsIssuerAchatInternetAlgeriePoste");
		List<DayOperationFransaBank> dayOperationFransaBanks = dayOperationFransaBankRepository
				.getListOffUsIssuerAchatInternetAlgeriePoste();
		List<BkmvtiFransaBank> BkmvtiFransaBanks = new ArrayList<BkmvtiFransaBank>();

		List<MvbkConf> mvbks = mvbkConfigR.findByCategorie(3007);

		for (DayOperationFransaBank day : dayOperationFransaBanks) {
			int indexPieceComptable = getEveIndex1();
			List<DayOperationFransaBank> dayOperationFransaBanksByNumTransaction = dayOperationFransaBankRepository
					.getListOnUsMemeAgenceByNumTransaction(day.getNumRefTransaction(), day.getNumAutorisation(),
							day.getNumCartePorteur());

			for (MvbkConf mvk : mvbks) {
				int index2 = getEveIndex();
				BkmvtiFransaBanks = TestSigne(20, mvk, dayOperationFransaBanksByNumTransaction, BkmvtiFransaBanks,
						indexPieceComptable, index2, mvk.getCodeSettlement());

			}
			updateDays(20, indexPieceComptable, dayOperationFransaBanksByNumTransaction, mvbks.get(0));

		}
		logger.info("end mvbkOffUsIssuerAchatInternetAlgeriePoste =>{}", BkmvtiFransaBanks.size());
		if (BkmvtiFransaBanks != null && !BkmvtiFransaBanks.isEmpty()) {
		    // Filter out null elements from the list
		    List<BkmvtiFransaBank> nonNullBkmvtiFransaBanks = BkmvtiFransaBanks.stream()
		            .filter(Objects::nonNull)
		            .collect(Collectors.toList());

		    if (!nonNullBkmvtiFransaBanks.isEmpty()) {
		        bkmvtiFransaBankRepository.saveAll(nonNullBkmvtiFransaBanks);
		    }
		    
		}
	}

	public void mvbkOffUsAcqAchatInternetAlgeriePosteWissal() {
		logger.info("begin mvbkOffUsAcqAchatInternetAlgeriePoste");
		List<DayOperationFransaBank> dayOperationFransaBanks = dayOperationFransaBankRepository
				.getListOffUsAcqAchatInternetAlgeriePoste();

		List<BkmvtiFransaBank> BkmvtiFransaBanks = new ArrayList<BkmvtiFransaBank>();
		List<MvbkConf> mvbks = mvbkConfigR.findByCategorie(3008);
		for (DayOperationFransaBank day : dayOperationFransaBanks) {
			int indexPieceComptable = getEveIndex1();
			List<DayOperationFransaBank> dayOperationFransaBanksByNumTransaction = dayOperationFransaBankRepository
					.getListOnUsMemeAgenceByNumTransaction(day.getNumRefTransaction(), day.getNumAutorisation(),
							day.getNumCartePorteur());

			for (MvbkConf mvk : mvbks) {
				int index2 = getEveIndex();
				BkmvtiFransaBanks = TestSigne(21, mvk, dayOperationFransaBanksByNumTransaction, BkmvtiFransaBanks,
						indexPieceComptable, index2, mvk.getCodeSettlement());

			}
			updateDays(21, indexPieceComptable, dayOperationFransaBanksByNumTransaction, mvbks.get(0));

		}
		logger.info("end mvbkOffUsAcqAchatInternetAlgeriePoste =>{}", BkmvtiFransaBanks.size());
		if (BkmvtiFransaBanks != null && !BkmvtiFransaBanks.isEmpty()) {
		    // Filter out null elements from the list
		    List<BkmvtiFransaBank> nonNullBkmvtiFransaBanks = BkmvtiFransaBanks.stream()
		            .filter(Objects::nonNull)
		            .collect(Collectors.toList());

		    if (!nonNullBkmvtiFransaBanks.isEmpty()) {
		        bkmvtiFransaBankRepository.saveAll(nonNullBkmvtiFransaBanks);
		    }
		    
		}	}

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

	public void updateDays(int methode, int indexPieceComptable, List<DayOperationFransaBank> days, MvbkConf mvb) {
		String lib = getPieceComptable(methode, indexPieceComptable, mvb);
		days.forEach(element -> {
			element.setPieceComptableBkm(lib);

			if (element.getPieceComptableBkm() == null) {
				FileRequest.print(methode + " " + indexPieceComptable + " " + mvb, lib);
			}
		});
		dayOperationFransaBankRepository.saveAll(days);
	}

	public String getPieceComptable(int methode, int indexPieceComptable, MvbkConf mvb) {
		String lib = "";
		switch (methode) {

		case 1:
			lib = "DB" + mvb.getCodeOperation() + String.format("%06d", indexPieceComptable);
			break;
		case 2:
			lib = "DB" + mvb.getCodeOperation() + String.format("%06d", indexPieceComptable);

			break;
		case 3:
			lib = "DB" + mvb.getCodeOperation() + String.format("%06d", indexPieceComptable);

			break;

		case 4:
			lib = "DB" + mvb.getCodeOperation() + String.format("%06d", indexPieceComptable);
			break;

		case 5:
			lib = "DB" + mvb.getCodeOperation() + String.format("%06d", indexPieceComptable);

			break;
		case 6:
			lib = "PA" + mvb.getCodeOperation() + String.format("%06d", indexPieceComptable);

			break;

		case 7:
			lib = "PA" + mvb.getCodeOperation() + String.format("%06d", indexPieceComptable);

			break;
		case 8:
			lib = "PA" + mvb.getCodeOperation() + String.format("%06d", indexPieceComptable);

			break;
		case 9:
			lib = "DB" + mvb.getCodeOperation() + String.format("%06d", indexPieceComptable);

			break;
		case 10:
			lib = "DB" + mvb.getCodeOperation() + String.format("%06d", indexPieceComptable);

			break;
		case 11:
			lib = "DB" + mvb.getCodeOperation() + String.format("%06d", indexPieceComptable);

			break;
		case 12:
			lib = "RB" + mvb.getCodeOperation() + String.format("%06d", indexPieceComptable);

			break;
		case 13:
			lib = "RB" + mvb.getCodeOperation() + String.format("%06d", indexPieceComptable);

			break;
		case 14:
			lib = "RB" + mvb.getCodeOperation() + String.format("%06d", indexPieceComptable);

			break;

		case 15:
			lib = "DB" + mvb.getCodeOperation() + String.format("%06d", indexPieceComptable);

			break;

		case 16:
			lib = "DB" + mvb.getCodeOperation() + String.format("%06d", indexPieceComptable);

			break;
		case 17:
			lib = "PA" + mvb.getCodeOperation() + String.format("%06d", indexPieceComptable);

			break;
		case 18:
			lib = "PA" + mvb.getCodeOperation() + String.format("%06d", indexPieceComptable);

			break;
		case 19:
			lib = "PA" + mvb.getCodeOperation() + String.format("%06d", indexPieceComptable);

			break;
		case 20:
			lib = "PA" + mvb.getCodeOperation() + String.format("%06d", indexPieceComptable);

			break;

		case 21:
			lib = "PA" + mvb.getCodeOperation() + String.format("%06d", indexPieceComptable);

			break;

		case 22:
			lib = "RB" + mvb.getCodeOperation() + String.format("%06d", indexPieceComptable);

			break;
		case 23:
			lib = "RB" + mvb.getCodeOperation() + String.format("%06d", indexPieceComptable);

			break;
		case 24:
			lib = "RB" + mvb.getCodeOperation() + String.format("%06d", indexPieceComptable);

			break;
		default:
			System.out.println("nothing");
		}
		return lib;

	}

	public BkmvtiFransaBank setSameData(int methode, BkmvtiFransaBank bkmvtiFransaBank, int index2,
			int indexPieceComptable, DayOperationFransaBank op, MvbkConf mvk) {
		String lib = "";
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

			bkmvtiFransaBank
					.setPieceComptable("DB" + mvk.getCodeOperation() + String.format("%06d", indexPieceComptable));
			bkmvtiFransaBank.setNumEvenement(String.format("%06d", indexPieceComptable));

			break;
		case 2:
			bkmvtiFransaBank
					.setPieceComptable("DB" + mvk.getCodeOperation() + String.format("%06d", indexPieceComptable));
			// bkmvtiFransaBank.setNumEvenement(op.getPieceComptableBkm().substring(op.getPieceComptableBkm().length()-6,op.getPieceComptableBkm().length()));
			bkmvtiFransaBank.setNumEvenement(String.format("%06d", indexPieceComptable));
			break;
		case 3:

			bkmvtiFransaBank
					.setPieceComptable("DB" + mvk.getCodeOperation() + String.format("%06d", indexPieceComptable));
			bkmvtiFransaBank.setNumEvenement(String.format("%06d", indexPieceComptable));
			break;

		case 4:

			bkmvtiFransaBank
					.setPieceComptable("DB" + mvk.getCodeOperation() + String.format("%06d", indexPieceComptable));
			bkmvtiFransaBank.setNumEvenement(String.format("%06d", indexPieceComptable));
			break;

		case 5:

			bkmvtiFransaBank
					.setPieceComptable("DB" + mvk.getCodeOperation() + String.format("%06d", indexPieceComptable));
			bkmvtiFransaBank.setNumEvenement(String.format("%06d", indexPieceComptable));
			break;
		case 6:

			bkmvtiFransaBank
					.setPieceComptable("PA" + mvk.getCodeOperation() + String.format("%06d", indexPieceComptable));
			bkmvtiFransaBank.setNumEvenement(String.format("%06d", indexPieceComptable));
			break;

		case 7:

			bkmvtiFransaBank
					.setPieceComptable("PA" + mvk.getCodeOperation() + String.format("%06d", indexPieceComptable));
			bkmvtiFransaBank.setNumEvenement(String.format("%06d", indexPieceComptable));
			break;
		case 8:

			bkmvtiFransaBank
					.setPieceComptable("PA" + mvk.getCodeOperation() + String.format("%06d", indexPieceComptable));
			bkmvtiFransaBank.setNumEvenement(String.format("%06d", indexPieceComptable));
			break;
		case 9:

			bkmvtiFransaBank
					.setPieceComptable("DB" + mvk.getCodeOperation() + String.format("%06d", indexPieceComptable));
			bkmvtiFransaBank.setNumEvenement(String.format("%06d", indexPieceComptable));
			break;
		case 10:

			bkmvtiFransaBank
					.setPieceComptable("DB" + mvk.getCodeOperation() + String.format("%06d", indexPieceComptable));
			bkmvtiFransaBank.setNumEvenement(String.format("%06d", indexPieceComptable));
			break;
		case 11:

			bkmvtiFransaBank
					.setPieceComptable("DB" + mvk.getCodeOperation() + String.format("%06d", indexPieceComptable));
			bkmvtiFransaBank.setNumEvenement(String.format("%06d", indexPieceComptable));
			break;
		case 12:

			bkmvtiFransaBank
					.setPieceComptable("RB" + mvk.getCodeOperation() + String.format("%06d", indexPieceComptable));
			bkmvtiFransaBank.setNumEvenement(String.format("%06d", indexPieceComptable));
			break;
		case 13:

			bkmvtiFransaBank
					.setPieceComptable("RB" + mvk.getCodeOperation() + String.format("%06d", indexPieceComptable));
			bkmvtiFransaBank.setNumEvenement(String.format("%06d", indexPieceComptable));
			break;
		case 14:

			bkmvtiFransaBank
					.setPieceComptable("RB" + mvk.getCodeOperation() + String.format("%06d", indexPieceComptable));
			bkmvtiFransaBank.setNumEvenement(String.format("%06d", indexPieceComptable));
			break;
		///////////// NEW ***********////////////////////////

		case 15:

			bkmvtiFransaBank
					.setPieceComptable("DB" + mvk.getCodeOperation() + String.format("%06d", indexPieceComptable));
			bkmvtiFransaBank.setNumEvenement(String.format("%06d", indexPieceComptable));
			break;

		case 16:

			bkmvtiFransaBank
					.setPieceComptable("DB" + mvk.getCodeOperation() + String.format("%06d", indexPieceComptable));
			bkmvtiFransaBank.setNumEvenement(String.format("%06d", indexPieceComptable));
			break;

		case 17:

			bkmvtiFransaBank
					.setPieceComptable("PA" + mvk.getCodeOperation() + String.format("%06d", indexPieceComptable));
			bkmvtiFransaBank.setNumEvenement(String.format("%06d", indexPieceComptable));
			break;

		case 18:

			bkmvtiFransaBank
					.setPieceComptable("PA" + mvk.getCodeOperation() + String.format("%06d", indexPieceComptable));
			bkmvtiFransaBank.setNumEvenement(String.format("%06d", indexPieceComptable));
			break;

		case 19:

			bkmvtiFransaBank
					.setPieceComptable("PA" + mvk.getCodeOperation() + String.format("%06d", indexPieceComptable));
			bkmvtiFransaBank.setNumEvenement(String.format("%06d", indexPieceComptable));
			break;
		case 20:

			bkmvtiFransaBank
					.setPieceComptable("PA" + mvk.getCodeOperation() + String.format("%06d", indexPieceComptable));
			bkmvtiFransaBank.setNumEvenement(String.format("%06d", indexPieceComptable));
			break;

		case 21:

			bkmvtiFransaBank
					.setPieceComptable("PA" + mvk.getCodeOperation() + String.format("%06d", indexPieceComptable));
			bkmvtiFransaBank.setNumEvenement(String.format("%06d", indexPieceComptable));
			break;

		case 22:

			bkmvtiFransaBank
					.setPieceComptable("RB" + mvk.getCodeOperation() + String.format("%06d", indexPieceComptable));
			bkmvtiFransaBank.setNumEvenement(String.format("%06d", indexPieceComptable));
			break;
		case 23:

			bkmvtiFransaBank
					.setPieceComptable("RB" + mvk.getCodeOperation() + String.format("%06d", indexPieceComptable));
			bkmvtiFransaBank.setNumEvenement(String.format("%06d", indexPieceComptable));
			break;
		case 24:

			bkmvtiFransaBank
					.setPieceComptable("RB" + mvk.getCodeOperation() + String.format("%06d", indexPieceComptable));
			bkmvtiFransaBank.setNumEvenement(String.format("%06d", indexPieceComptable));
			break;
		default:

		}
		return bkmvtiFransaBank;
	}

	public List<BkmvtiFransaBank> TestSigne(int methode, MvbkConf mvk,
			List<DayOperationFransaBank> dayOperationFransaBanksByNumTransaction,
			List<BkmvtiFransaBank> BkmvtiFransaBanks, int indexPieceComptable, int index2, String codeOperation) {

		BkmvtiFransaBank bkmvtiFransaBank2 = new BkmvtiFransaBank();
		Map<String, Float> data = new HashMap<>();
		Map<String, String> ComptC = new HashMap<>();
		Map<String, String> ComptD = new HashMap<>();

		for (DayOperationFransaBank op : dayOperationFransaBanksByNumTransaction) {
			data.put(op.getIdenfication(), op.getMontantSettlement());
			SettelementFransaBank sett = settlementRepo.findByIdentificationbh(op.getIdenfication());
			ComptC.put(sett.getCreditAccount(), op.getCompteCredit());
			ComptD.put(sett.getDebitAccount(), op.getCompteDebit());

		}
//		FileRequest.print(ComptC.toString(), FileRequest.getLineNumber());
//		FileRequest.print(ComptD.toString(), FileRequest.getLineNumber());
		DayOperationFransaBank op = dayOperationFransaBanksByNumTransaction
				.get(dayOperationFransaBanksByNumTransaction.size() - 1);
		try {
  			if(Double.valueOf( fIPService.evaluateWithElements(codeOperation, data))<=0) {
				throw new Exception("amount lower than expected");
			}
			if (AccountSigne(mvk.getAccount(), mvk.getSigne()) == 1) {
				bkmvtiFransaBank2 = TestAccountAndSigne(methode, index2, indexPieceComptable, (data.size() > 1 ? 2 : 1),
						("C".equals(mvk.getSigne()) ? ComptC.get(mvk.getAccount()) : ComptD.get(mvk.getAccount())),
						op.getCompteDebit().length(), mvk.getSigne(), mvk, op);

			}

			else if (AccountSigne(mvk.getAccount(), mvk.getSigne()) == 2) {
				bkmvtiFransaBank2 = TestAccountAndSigne(methode, index2, indexPieceComptable, (data.size() > 1 ? 2 : 1),
						("C".equals(mvk.getSigne()) ? ComptC.get(mvk.getAccount()) : ComptD.get(mvk.getAccount())),
						op.getCompteDebit().length(), mvk.getSigne(), mvk, op);

			} else if (AccountSigne(mvk.getAccount(), mvk.getSigne()) == 3) {

//			String accountATM = getAccountAtm(op.getIdCommercant());
				bkmvtiFransaBank2 = TestAccountAndSigne(methode, index2, indexPieceComptable, (data.size() > 1 ? 2 : 1),
						("C".equals(mvk.getSigne()) ? ComptC.get(mvk.getAccount()) : ComptD.get(mvk.getAccount())),
						op.getCompteDebit().length(), mvk.getSigne(), mvk, op);

			}

			else if (AccountSigne(mvk.getAccount(), mvk.getSigne()) == 4) {

				if ("BANK_ACQ".equals(mvk.getEntity())) {

					String codeBank = op.getCodeBankAcquereur();
					String mvbaccount = mvk.getAccount().substring(0, mvk.getAccount().length() - 3) + codeBank;

					bkmvtiFransaBank2 = TestAccountAndSigne(methode, index2, indexPieceComptable,
							(data.size() > 1 ? 2 : 1), mvbaccount, op.getCompteDebit().length(), mvk.getSigne(), mvk,
							op);
				} else if ("BANK_ISSUER".equals(mvk.getEntity())) {

					String codeBank = op.getCodeBank();
					String mvbaccount = mvk.getAccount().substring(0, mvk.getAccount().length() - 3) + codeBank;

					bkmvtiFransaBank2 = TestAccountAndSigne(methode, index2, indexPieceComptable,
							(data.size() > 1 ? 2 : 1), mvbaccount, op.getCompteDebit().length(), mvk.getSigne(), mvk,
							op);
				} else {
					bkmvtiFransaBank2 = TestAccountAndSigne(methode, index2, indexPieceComptable,
							(data.size() > 1 ? 2 : 1), mvk.getAccount(), op.getCompteDebit().length(), mvk.getSigne(),
							mvk, op);
				}

			}
			bkmvtiFransaBank2.setMontant(getAmountFormat(fIPService.evaluateWithElements(codeOperation, data)));

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
		} catch (Exception e) {
			FileRequest.print(e.getMessage(), FileRequest.getLineNumber());
			FileRequest.print(op.toString(), codeOperation);
			FileRequest.print(mvk.toString(), codeOperation);
				return BkmvtiFransaBanks;

		}
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
		TestAccountLength(account.length(), accountDebit, account, bkmvtiFransaBank, op);
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
// 		if (mvk.getType().equals("1")) {
//			getAmount(op.getMontantSettlement(), bkmvtiFransaBank);
//		}

		bkmvtiFransaBank.setCodeDeviceOrigine("208");
		bkmvtiFransaBank.setIdentification(mvk.getIdentification());
		bkmvtiFransaBank.setNumRefTransactions(op.getNumRefTransaction());

		setSameData(methode, bkmvtiFransaBank, index2, indexPieceComptable, op, mvk);

		return bkmvtiFransaBank;

	}

	public BkmvtiFransaBank TestAccountLength(int lengAccount, int lengAccountDebit, String account,
			BkmvtiFransaBank bkmvtiFransaBank, DayOperationFransaBank op) {
		if (lengAccount > 18) {
			String credit = account.substring(8, 18);
			String chapitreCompta = account.substring(8, 14);
			bkmvtiFransaBank.setChapitreComptable(chapitreCompta + getZero(6 - chapitreCompta.length()));
			bkmvtiFransaBank.setNumCompte(credit);
			bkmvtiFransaBank.setCleControleCompte(getSpace(2));
			String codeDes = account.substring(3, 8);
			bkmvtiFransaBank.setAgenceDestinatrice(codeDes);
			bkmvtiFransaBank.setAgenceEmettrice(codeDes);
			bkmvtiFransaBank.setCodeAgenceSaisie(codeDes);
			String codeId = account.substring(3, 8);
			bkmvtiFransaBank.setCodeID("S" + codeId);
		}

		else if (lengAccount >= 6 && lengAccount < 10) {
			bkmvtiFransaBank.setNumCompte(account + getZero(10 - (account.length())));
			bkmvtiFransaBank.setChapitreComptable(account.substring(0, 6) + getZero(6 - account.length()));

			bkmvtiFransaBank.setCleControleCompte(getSpace(2));
			bkmvtiFransaBank.setAgenceDestinatrice(op.getCodeAgence());
			bkmvtiFransaBank.setAgenceEmettrice(op.getCodeAgence());
			bkmvtiFransaBank.setCodeAgenceSaisie(op.getCodeAgence());
			bkmvtiFransaBank.setCodeID("S" + op.getCodeAgence());
		}

		else if (lengAccount < 6) {
			bkmvtiFransaBank.setNumCompte(account + getZero(10 - (account.length())));
			bkmvtiFransaBank.setChapitreComptable(account + getZero(6 - account.length()));
			bkmvtiFransaBank.setCleControleCompte(getSpace(2));

		}

		else if (lengAccount == 10) {
			bkmvtiFransaBank.setNumCompte(account);
			bkmvtiFransaBank.setChapitreComptable(account.substring(0, 6));
			bkmvtiFransaBank.setCleControleCompte(getSpace(2));
			bkmvtiFransaBank.setAgenceDestinatrice(op.getCodeAgence());
			bkmvtiFransaBank.setAgenceEmettrice(op.getCodeAgence());
			bkmvtiFransaBank.setCodeAgenceSaisie(op.getCodeAgence());
			bkmvtiFransaBank.setCodeID("S" + op.getCodeAgence());
		}

		else {
			bkmvtiFransaBank.setNumCompte(account.substring(0, 10));
			bkmvtiFransaBank.setChapitreComptable(account.substring(0, 6));
			bkmvtiFransaBank.setCleControleCompte(getSpace(2));
			bkmvtiFransaBank.setAgenceEmettrice(op.getCodeAgence());
			bkmvtiFransaBank.setCodeAgenceSaisie(op.getCodeAgence());
			bkmvtiFransaBank.setCodeID("S" + op.getCodeAgence());
		}

		return bkmvtiFransaBank;
	}

	public BkmvtiFransaBank TestCodeAgence(int methode, int test, String codeAgence, MvbkConf mvk,
			DayOperationFransaBank op, BkmvtiFransaBank bkmvtiFransaBank) {

		if ("P".equals(mvk.getCodeAgence())) {

			bkmvtiFransaBank.setAgence(op.getNumRIBEmetteur().substring(3, 8));
			bkmvtiFransaBank.setAgenceEmettrice(bkmvtiFransaBank.getAgence());
			bkmvtiFransaBank.setAgenceDestinatrice(bkmvtiFransaBank.getAgence());
			bkmvtiFransaBank.setCodeAgenceSaisie(bkmvtiFransaBank.getAgence());
			bkmvtiFransaBank.setCodeID("S" + bkmvtiFransaBank.getAgence());
		} else if ("C".equals(mvk.getCodeAgence())) {

			bkmvtiFransaBank.setAgence(op.getCodeAgence());
			bkmvtiFransaBank.setAgenceEmettrice(bkmvtiFransaBank.getAgence());
			bkmvtiFransaBank.setAgenceDestinatrice(bkmvtiFransaBank.getAgence());
			bkmvtiFransaBank.setCodeAgenceSaisie(bkmvtiFransaBank.getAgence());
			bkmvtiFransaBank.setCodeID("S" + bkmvtiFransaBank.getAgence());
		} else {

			bkmvtiFransaBank.setAgence(mvk.getCodeAgence());
		}

		return bkmvtiFransaBank;
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

	public String getAmountFormat(double amount) {
		float m = Math.round(amount);
		int a = (int) m;

		String amountFormat = String.format("%018d,%02d", a / 100, a % 100);

		return amountFormat;
	}

	@Override
	public void run() {
		switch (paramGlobalBancaire) {

		/* done */

		case "1":
			mvbkOnUsMemeAgenceWissal();
			break;
		case "2":
			mvbkOnUsAutreAgenceWissal();
			break;
		case "3":
			mvbkOffUsIssuerWissal();
			break;
		case "4":
			mvbkOffUsAcqWissal();
			break;
		case "5":
			mvbkOffUsAcqAlgeriePosteWissal();
			break;
		case "6":
			mvbkOffUsIssuerAlgeriePosteWissal();
			break;

		case "7":
			mvbkOnUsMemeAgenceAchatWissal();
			break;
		case "8":
			mvbkOnUsAutreAgenceAchatWissal();
			break;
		case "9":
			mvbkOffUsIssuerAchatWissal();
			break;
		case "10":
			mvbkOffUsAcqAchatWissal();
			break;

		case "11":
			mvbkONUdMemeAgenceConsultationSoldeWissal();
			break;
		case "12":
			mvbkONUdAutreAgenceConsultationSoldeWissal();
			break;
		case "13":
			mvbkOffUsIssuerConsultationSoldeWissal();
			break;
		case "14":
			mvbkOffUsAcqConsultationSoldeWissal();
			break;

		case "15":
			mvbkONUdMemeAgenceRembourssementTpeWissal();
			break;
		case "16":
			mvbkONUdAutreAgenceRembourssementTpeWissal();
			break;
		case "17":
			mvbkOffUsIssuerRembourssementTpeWissal();
			break;
		case "18":
			mvbkOffUsAcqRembourssementTpeWissal();
			break;

		case "19":
			mvbkONUdMemeAgenceRembourssementInternetWissal();
			break;
		case "20":
			mvbkONUdAutreAgenceRembourssementInternetWissal();
			break;
		case "21":
			mvbkOffUsIssuerRembourssementInternetWissal();
			break;
		case "22":
			mvbkOffUsAcqRembourssementInternetWissal();
			break;

		case "23":
			mvbkOnUsMemeAgenceAchatInternetWissal();
			break;
		case "24":
			mvbkOnUsAutreAgenceAchatInternetWissal();
			break;
		case "25":
			mvbkOffUsIssuerAchatInternetWissal();
			break;
		case "26":
			mvbkOffUsAcqAchatInternetWissal();
			break;

		case "27":
			mvbkOffUsIssuerAchatInternetAlgeriePosteWissal();
			break;
		case "28":
			mvbkOffUsAcqAchatInternetAlgeriePosteWissal();
			break;
		case "29":
			mvbkOffUsIssuerAchatInternetWissalExceptionnelMerchant();
			break;
		case "30":
			mvbkONUdMemeAgenceCHACHAT();
			break;
		case "31":
			mvbkONUdAutreAgenceCHACHAT();
			break;
		case "32":
			mvbkONUdMemeAgenceCHRet();
			break;
		case "33":
			mvbkONUdAutreAgenceCHRet();
			break;
		case "34":
			mvbkIssuerCHACHAT();
			break;
		case "35":
			mvbkIssuerCHRET();
			break;
		case "36":
			mvbkACQCHACHAT();
			break;
		case "37":
			mvbkACQCHRET();
			break;

		default:
			System.out.println("c bn ");
			break;
		}
		System.out.println("Param" + paramGlobalBancaire);
	}
}
