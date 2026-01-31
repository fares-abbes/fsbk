package com.mss.backOffice.services;

import org.springframework.stereotype.Service;

import com.google.common.base.Throwables;
import com.mss.unified.entities.Account;
import com.mss.unified.entities.DayOperationFeesPosSequence;
import com.mss.unified.entities.DayOperationPosFransaBank;
import com.mss.unified.entities.PosTerminal;
import com.mss.unified.entities.SettelementFransaBank;
import com.mss.unified.entities.TvaCommissionFransaBank;
import com.mss.unified.repositories.AccountRepository;
import com.mss.unified.repositories.DayOperationPosFransaBankRepository;
import com.mss.unified.repositories.PosTerminalRepository;
import com.mss.unified.repositories.SettelementFransaBankRepository;
import com.mss.unified.repositories.TvaCommissionFransaBankRepository;

import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;

@Service
public class AnniversaryPosService {
	@Autowired
	PosTerminalRepository posTerminalRepository;
	@Autowired
	SettelementFransaBankRepository settlementRepo;
	@Autowired
	DayOperationPosSequenceService dayOperationPosSequenceService;
	@Autowired
	private TvaCommissionFransaBankRepository tvaRepo;
	@Autowired
	private DayOperationPosFransaBankRepository operationRepo;
	@Autowired
	private AccountRepository accountRepository;
	private static final Logger logger = LoggerFactory.getLogger(AnniversaryPosService.class);

	private SimpleDateFormat dayFormat = new SimpleDateFormat("dd");
	private SimpleDateFormat dayAndMonthFormat = new SimpleDateFormat("dd/MM");
	private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
	private SimpleDateFormat timeFormat = new SimpleDateFormat("HHmmss");
	private SimpleDateFormat filedateFormat = new SimpleDateFormat("yyMMdd");
	private SimpleDateFormat datePieceComptableFormat = new SimpleDateFormat("ddMMyy");

	LocalDateTime now = LocalDateTime.now();
	LocalDateTime target = LocalDateTime.of(now.getYear(), now.getMonth(), now.getDayOfMonth(), 15, 30); // 3:30 PM
	long delay = Duration.between(now, target).toMillis();

	private List<DayOperationPosFransaBank> saveDayOperations(PosTerminal p, Date date, long amount,
			SettelementFransaBank settlt003, SettelementFransaBank settlt004, Account account,
			List<DayOperationPosFransaBank> dayOperations) {
		String transactionDate = dateFormat.format(date);
		String transactionTime = timeFormat.format(date);
		TvaCommissionFransaBank tvaCommissionFransaBank = tvaRepo.findTva();
		float tvaBase = Float.parseFloat(tvaCommissionFransaBank.getTva()) / 100;

		float tva = tvaBase + 1;
		logger.info("TVA is => {}", tva);

		String agence = p.getAgence();
		Account a = accountRepository.findByAccountCode(p.getMerchantCode().getAccount()).get();

		long settlementAmountHt = Math.round(Long.parseLong(p.getMontantLoyer()) / tva);
		logger.info("settlementAmountHt is => {}", settlementAmountHt);

		long settlementAmountTva = Long.parseLong(p.getMontantLoyer()) - settlementAmountHt;
		logger.info("settlementAmountTva is => {}", settlementAmountTva);

		long transactionAmount = Integer.parseInt(p.getMontantLoyer());
		logger.info("transactionAmount is => {}", transactionAmount);

		String montantTrans = String.format("%014d", transactionAmount);
		montantTrans = montantTrans.substring(0, 12) + "." + montantTrans.substring(12);

		DayOperationFeesPosSequence seq = dayOperationPosSequenceService.getSequence();
		DayOperationFeesPosSequence seqPieceComptable = dayOperationPosSequenceService.getSequencePieceComptable();

		DayOperationPosFransaBank T003 = new DayOperationPosFransaBank();

		T003.setCodeAgence(agence);
		T003.setCodeBankAcquereur("035");
		T003.setCodeBank("035");
		T003.setCompteDebit("0" + a.getAccountNum());
		T003.setDateTransaction(transactionDate);
		T003.setHeureTransaction(transactionTime);
		T003.setCompteCredit(settlt003.getCreditAccount());
		T003.setNumCartePorteur(p.getPosNum());
		T003.setNumtransaction(String.format("%012d", seq.getSequence()));
		T003.setIdenfication(settlt003.getIdentificationbh());
		T003.setMontantSettlement(String.format("%014d", settlementAmountHt));
		T003.setMontantTransaction(montantTrans);
		T003.setLibelleCommercant("REFABRICATION CARTE");

		T003.setPieceComptable(
				"FC" + datePieceComptableFormat.format(date) + String.format("%03d", seqPieceComptable.getSequence()));
		T003.setRefernceLettrage(datePieceComptableFormat.format(date) + p.getPosNum());
		T003.setFileDate(filedateFormat.format(date));
		T003.setNumAutorisation(T003.getNumtransaction());
		T003.setNumRefTransaction(T003.getNumtransaction());

		dayOperations.add(T003);

		DayOperationPosFransaBank T004 = new DayOperationPosFransaBank();

		T004.setCodeAgence(agence);
		T004.setCodeBankAcquereur("035");
		T004.setCodeBank("035");
		T004.setCompteDebit("0" + a.getAccountNum());
		T004.setDateTransaction(transactionDate);
		T004.setHeureTransaction(transactionTime);
		T004.setCompteCredit(settlt004.getCreditAccount());
		T004.setNumCartePorteur(p.getPosNum());
		T004.setNumtransaction(String.format("%012d", seq.getSequence()));
		T004.setIdenfication(settlt004.getIdentificationbh());
		T004.setMontantSettlement(String.format("%014d", settlementAmountTva));
		T004.setMontantTransaction(montantTrans);
		T004.setLibelleCommercant("REFABRICATION CARTE");

		T004.setPieceComptable(
				"FC" + datePieceComptableFormat.format(date) + String.format("%03d", seqPieceComptable.getSequence()));
		T004.setRefernceLettrage(datePieceComptableFormat.format(date) + p.getPosNum());
		T004.setFileDate(filedateFormat.format(date));
		T004.setNumAutorisation(T004.getNumtransaction());
		T004.setNumRefTransaction(T004.getNumtransaction());

		dayOperations.add(T004);
		seq = dayOperationPosSequenceService.incrementSequence(seq);
		seqPieceComptable = dayOperationPosSequenceService.incrementSequencePieceComptable(seqPieceComptable);
		return dayOperations;

	}

	// @Scheduled(cron = "*/20 * * * * *")
	// @Scheduled(cron = "0 1 0 * * ?")
	@Scheduled(cron = "0 1 0 * * ?")
	public void anniversaryCron() throws Exception {
		logger.info("Begin anniversary cron");

		try {

			Date currentDay = new Date();
			Calendar cal = Calendar.getInstance();
			cal.setTime(currentDay);
			int lastDay = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
			LocalDate localDate = currentDay.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

			List<String> days = new ArrayList<String>();

			if (localDate.getMonthValue() == 2 && localDate.getDayOfMonth() == 28
					&& localDate.getDayOfMonth() == lastDay) {

				days = new ArrayList<String>(Arrays.asList("28", "29", "30", "31"));

			} else if (localDate.getMonthValue() == 2 && localDate.getDayOfMonth() == 29
					&& localDate.getDayOfMonth() == lastDay) {
				days = new ArrayList<String>(Arrays.asList("29", "30", "31"));

			} else if (localDate.getDayOfMonth() == 30 && localDate.getDayOfMonth() == lastDay) {
				days = new ArrayList<String>(Arrays.asList("30", "31"));

			} else {
				days = new ArrayList<String>(Arrays.asList(dayFormat.format(currentDay)));

			}

			logger.info("currentDay => {}", dayFormat.format(currentDay));
			logger.info("days ", days);
			List<PosTerminal> pt = posTerminalRepository.getPosTerminalWithStartDateByDay(days);
			logger.info("PosTerminal size => {}", pt.size());

			List<DayOperationPosFransaBank> dayOperations = new ArrayList<DayOperationPosFransaBank>();

			SettelementFransaBank settlt003 = settlementRepo.findByIdentificationbh("T003");
			SettelementFransaBank settlt004 = settlementRepo.findByIdentificationbh("T004");

			LocalDate posCreationDate = LocalDate.of(2022, 1, 31);
			LocalDate currentDate = LocalDate.of(2022, 2, 28);
			long monthsElapsed = ChronoUnit.MONTHS.between(posCreationDate, currentDate);
			System.out.println("monthsElapsed " + monthsElapsed);
			for (PosTerminal p : pt) {

				Account a = accountRepository.findByAccountCode(p.getMerchantCode().getAccount()).get();

				dayOperations = saveDayOperations(p, currentDay, Long.parseLong(p.getMontantLoyer()), settlt003,
						settlt004, a, dayOperations);

			}

			operationRepo.saveAll(dayOperations);
		} catch (Exception e) {

			logger.info("Exception is=>{}", Throwables.getStackTraceAsString(e));
		}

		logger.info("end anniversary cron");
	}

}
