package com.mss.backOffice.controller;

import com.google.common.base.Throwables;
import com.google.gson.Gson;
import com.mss.backOffice.services.DayOperationCardSequenceService;
import com.mss.backOffice.services.FormulaInterpreterService;
import com.mss.backOffice.services.PropertyService;
import com.mss.unified.entities.*;
import com.mss.unified.repositories.*;

import org.apache.any23.plugin.Author;
import org.apache.commons.beanutils.PropertyUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.transaction.TransactionScoped;

//import static com.mss.backOffice.controller.ExecutorThreadMp.Test;

@RestController
@RequestMapping("executorSwitchDayOP")
public class ExecutorSwitch {
	private static final Logger logger = LoggerFactory.getLogger(ExecutorSwitch.class);
	@Autowired
	UserRepository userRepository;
	@Autowired
	SettelementFransaBankRepository settlementRepo;
	@Autowired
	DayOperationFransaBankRepository operationRepo;
	@Autowired
	DownloadFileBc fbc;
	@Autowired
	TvaCommissionFransaBankRepository tvaRepo;
	@Autowired
	BatchesFFCRepository batchRepo;
	@Autowired
	MvbkConfigRepository mvbkConfR;
	@Autowired
	PourcentageCommFSBKRepository pourcentageCommFSBKRepository;

	@Autowired
	AgenceAdministrationRepository ag;
	@Autowired
	MerchantRepository mr;
	@Autowired
	AccountRepository accR;
	@Autowired
	CodeBankBCRepository codeBankBCRepository;
	@Autowired
	DayOperationFransaBankRepository dayOperationFransaBankRepository;
	@Autowired
	MvbkSettlementRepository mvbkSettlementRepository;
	@Autowired
	BkmvtiFransaBankRepository bkmvtiFransaBankRepository;
	@Autowired
	public CommissionFransaBankRepository commissionFransaRepo;
	@Autowired
	SummaryCompensationOnUsRepository summaryCompensationOnUsRepository;

	@Autowired
	BinOnUsRepository binOnUsRepository;
	@Autowired
	FormulaInterpreterService fIPService;
	@Autowired
	SwitchTransactionRepository switchTransactionRepository;
	@Autowired
	SwitchTransactionTRepository switchTransactionTRepository;
	@Autowired
	SummaryCompensationOnUsRepository summaryCompensationOnUsR;
	@Autowired
	TvaCommissionFransaBankRepository tvaCommissionFransaBankRepository;
	@Autowired
	CommissionSwitchRepository cSR;
	@Autowired
	AtmTerminalRepository atmRepo;
	@Autowired
	AccountRepository accountRepo;

	static LocalDate dateExc;
	public static List<AtmTerminal> atmTerminal;
	public static List<Account> account;
	private static final Gson gson = new Gson();
	public static HashMap<String, String> merchantAccountList;

	public HashMap<String, String> findMerchantsaccount() {
		HashMap<String, String> elements = new HashMap<>();

		try {
			List<Object[]> queryResult = switchTransactionTRepository.getMerchantAccount();
			for (Object[] obj : queryResult) {

				if (obj[0] != null && obj[0] != null) {
					String merchant = (String) obj[0];
					String account = (String) obj[1];
					if (elements.get(merchant) == null) {
						elements.put(merchant, account);
					} else
						elements.put(merchant, account);
				}
			}

		} catch (Exception e) {

			System.out.println(e);
		}
		return elements;

	}

	public void copyElements(SwitchTransactionT op, SwitchTransaction element) {

		try {
			PropertyUtils.copyProperties(op, element);
		} catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		op.setAccountCommercant(merchantAccountList.get(element.getSwitchAcceptorMerchantCode()));
	}

	@GetMapping("DayOperationfromSwitch/")
	public ResponseEntity<?> dayOP() {
		String name = SecurityContextHolder.getContext().getAuthentication().getName();

		Optional<User> user = userRepository.findByUserNameOrUserEmail(name, name);
		if (!user.isPresent()) {
			throw new RuntimeException("Error saving status !");
		}
		BatchesFC batch = batchRepo.findByKey("ExecuteSwitch").get();
		batch.setBatchDate(new Date());
		batch.setBatchStatus(0);
		batchRepo.save(batch);
//		dayOperationFransaBankRepository.deleteAll();
		FileRequest.print("starting DayOperationfromSwitch ", FileRequest.getLineNumber());
		TvaCommissionFransaBank tva = tvaCommissionFransaBankRepository.findById(1).get();
		System.out.println(tvaCommissionFransaBankRepository.findAll());
		ArrayList<DayOperationFransaBank> listToSave = new ArrayList<>();
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("YYYY-MM-dd");

		merchantAccountList = findMerchantsaccount();
		dateExc = LocalDate.now();
		List<SwitchTransaction> or = switchTransactionRepository.getAllDayOPTransaction();
		List<SwitchTransaction> todelete = new ArrayList<>();
		FileRequest.print(or.toString(), FileRequest.getLineNumber());

		ArrayList<SwitchTransactionT> lswt = new ArrayList<>();
		Date yesterday = new Date(System.currentTimeMillis() - 24 * 60 * 60 * 1000);
		Date yesterdayAt2359 = new Date(yesterday.getYear(), yesterday.getMonth(), yesterday.getDate(), 23, 59, 0);
		for (SwitchTransaction el : or) {

			Date currentDate = new Date();
			Date transactionDate = el.getSwitchRequestDate();

//			long timeDifference = currentDate.getTime() - el.getSwitchRequestDate().getTime();
//			long minutesDifference = timeDifference / (60 * 1000);

			if (transactionDate.before(yesterdayAt2359)) {
				SwitchTransactionT element = new SwitchTransactionT();
				copyElements(element, el);
				el.setSwitchDE156("Done");
				lswt.add(element);
			} else {
				todelete.add(el);
			}

//			SwitchTransactionT element = new SwitchTransactionT();
//			copyElements(element, el);
//			el.setSwitchDE156("Done");
//			lswt.add(element);
		}

		or.removeAll(todelete);
		FileRequest.print(lswt.toString(), FileRequest.getLineNumber());
		switchTransactionTRepository.saveAll(lswt);
		switchTransactionRepository.saveAll(or);

//
//		addRetrais(listToSave, id, tva);
//		addConsultationSolde_onUs(listToSave, id, tva);
//		addAchatTPE(listToSave, id, tva);
//
//		// operationRepo.deleteAll();
//		List<DayOperationFransaBank> saved = operationRepo.saveAll(listToSave);

		Thread[] threads = new Thread[34];
		Runnable[] runn = new Runnable[34];
		atmTerminal = atmRepo.findAll();
		account = accountRepo.findAll();
		FileRequest.print("atm list" + atmTerminal.size(), FileRequest.getLineNumber());
		FileRequest.print("account lis" + account.size(), FileRequest.getLineNumber());

		for (int i = 0; i < 7; i++) {
			try {
				runn[i] = new DayOpSwitchFSBKThread(Integer.toString(i + 1), fIPService,binOnUsRepository,
						switchTransactionTRepository, settlementRepo, dayOperationFransaBankRepository,
						tvaCommissionFransaBankRepository, batchRepo, pourcentageCommFSBKRepository,
						codeBankBCRepository, mvbkSettlementRepository, bkmvtiFransaBankRepository, ag, mr, accR, cSR,
						mvbkConfR, atmTerminal, account, user.get().getUserName());
				threads[i] = new Thread(runn[i]);
				threads[i].setName("DayOPN: " + Integer.toString(i + 1));
				threads[i].start();
			} catch (Exception e) {
				ExecutorThreadFransaBank.stopThreads = true;
				batchRepo.updateStatusAndErrorBatch("Execute", 2, e.getMessage(), new Date(),
						e.getStackTrace()[1].toString());
				throw e;
			}
			if (ExecutorThreadFransaBank.stopThreads) {
				break;
			}

		}

		// Wait for the threads to finish

		Thread waitThread = new Thread(() -> {
			try {
				for (int i = 0; i < 7; i++) {
					try {
						checkerrors();
						threads[i].join();

					} catch (Exception e) {

						throw new Exception("error in dayOp");
					}
				}
				for (int i = 7; i < 14; i++) {
					try {
						runn[i] = new DayOpSwitchFSBKThread(Integer.toString(i + 1),fIPService, binOnUsRepository,
								switchTransactionTRepository, settlementRepo, dayOperationFransaBankRepository,
								tvaCommissionFransaBankRepository, batchRepo, pourcentageCommFSBKRepository,
								codeBankBCRepository, mvbkSettlementRepository, bkmvtiFransaBankRepository, ag, mr,
								accR, cSR, mvbkConfR, atmTerminal, account, user.get().getUserName());
						threads[i] = new Thread(runn[i]);
						threads[i].setName("Bkmvti: " + Integer.toString(i + 1));
						threads[i].start();
					} catch (Exception e) {
						ExecutorThreadFransaBank.stopThreads = true;
						batchRepo.updateStatusAndErrorBatch("Execute", 2, e.getMessage(), new Date(),
								e.getStackTrace()[1].toString());
						throw e;
					}
					if (ExecutorThreadFransaBank.stopThreads) {
						
						break;
					}

				}
				FileRequest.print("Day Operation ended with sucess "+ExecutorThreadFransaBank.stopThreads, FileRequest.getLineNumber());
				FileRequest.print("ends DayOperationfromSwitch ", FileRequest.getLineNumber());
				FileRequest.print("starting bkmintegration ", FileRequest.getLineNumber());
				FileRequest.print("starting", FileRequest.getLineNumber());

				for (int i = 0; i < 14; i++) {
					try {
						threads[i].join();
					} catch (InterruptedException e) {
						batchRepo.updateStatusAndErrorBatch("Execute", 2, e.getMessage(), new Date(),
								e.getStackTrace()[1].toString());
						e.printStackTrace();
					}
				}
				FileRequest.print("BKM Switch ended with sucess ", FileRequest.getLineNumber());
				FileRequest.print("ends BKM Switch ", FileRequest.getLineNumber());

				batchRepo.updateFinishBatch("ExecuteSwitch", 1, new Date());
				if (batchRepo.findByKey("ByPassTP").get().getBatchStatus() == 0) {
					BatchesFC execstatus = batchRepo.findByKey("execStatus").get();
					FileRequest.print("swith ended ", FileRequest.getLineNumber());
					fbc.writeInFile();
					execstatus.setBatchNumber(2);
					batchRepo.save(execstatus);
					batchRepo.updateFinishBatch("ByPassTP", 1, new Date());
					batchRepo.updateFinishBatch("CRAUAP40", 1, new Date());
					batchRepo.updateFinishBatch("CRAUAP50", 1, new Date());
					batchRepo.updateFinishBatch("CRAUAP51", 1, new Date());
				}
			} catch (Exception e) {
				batchRepo.updateStatusAndErrorBatch("Execute", 2, e.getMessage(), new Date(),
						e.getStackTrace()[1].toString());
				e.printStackTrace();
			}
			FileRequest.print("exiting method ", FileRequest.getLineNumber());

		});
		waitThread.setName("replyStatus");
		waitThread.start();

		return ResponseEntity.ok().body(gson.toJson("starting processes"));
	}

	private void checkerrors() throws Exception {
		if (ExecutorThreadFransaBank.stopThreads) {
			throw new Exception("error in dayOp");
		}
	}

}
