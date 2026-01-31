package com.mss.backOffice.controller;

import com.google.common.base.Throwables;
import com.mss.backOffice.services.FormulaInterpreterService;
import com.mss.unified.entities.BkmvtiFransaBank;
import com.mss.unified.entities.DayOperationFransaBank;
import com.mss.unified.entities.FileContentT;
import com.mss.unified.entities.MvbkSettlement;
import com.mss.unified.entities.SettelementFransaBank;
import com.mss.unified.entities.User;
import com.mss.unified.repositories.BatchesFFCRepository;
import com.mss.unified.repositories.BkmvtiFransaBankRepository;
import com.mss.unified.repositories.CodeBankBCRepository;
import com.mss.unified.repositories.DayOperationCardFransaBankRepository;
import com.mss.unified.repositories.DayOperationFransaBankRepository;
import com.mss.unified.repositories.MerchantRepository;
import com.mss.unified.repositories.MvbkConfigRepository;
import com.mss.unified.repositories.MvbkSettlementRepository;
import com.mss.unified.repositories.SettelementFransaBankRepository;
import com.mss.unified.repositories.SummaryCompensationOnUsRepository;
import com.mss.unified.repositories.UserRepository;

import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("BKMTable")
public class ExecutorBKMMouvement {
	public static List<DayOperationFransaBank> days = new ArrayList<DayOperationFransaBank>();
	@Autowired
	public MvbkConfigRepository mvbkConfigR;
	@Autowired
	BatchesFFCRepository batchRepo;
	@Autowired
	CodeBankBCRepository codeBankBCRepository;
	@Autowired
	DayOperationFransaBankRepository dayOperationFransaBankRepository;
	@Autowired
	MvbkSettlementRepository mvbkSettlementRepository;
	@Autowired
	BkmvtiFransaBankRepository bkmvtiFransaBankRepository;
	@Autowired
	ExecutorThreadUAPFileBC uapfilBc;
	public String paramGlobalBancaire;
	List<String[]> ListcommisionIntenational;

	@Autowired
	MerchantRepository mr;
	@Autowired
	FormulaInterpreterService fIPService;
	@Autowired
	SettelementFransaBankRepository settlemnts;
	public static int index = 0;
	public static int index2 = 0;
	private static final Logger logger = LoggerFactory.getLogger(ExecutorBKMMouvement.class);
	public static boolean catchException;

	@GetMapping("get")
	public void parallelGBanciare(String name, String fileIntegration) throws ParseException {
		logger.info("parallelGBanciare");
		logger.info("days size=>{}", days.size());
		catchException = false;
		index2 = 0;

		if (!catchException) {

			Thread[] threads = new Thread[37];
			Runnable[] runn = new Runnable[37];
			for (int i = 0; i < threads.length; i++) {
				runn[i] = new BKMMouvementThread(mr, settlemnts, fIPService, mvbkConfigR, codeBankBCRepository,
						dayOperationFransaBankRepository,

						mvbkSettlementRepository, bkmvtiFransaBankRepository, Integer.toString(i + 1), name);

				threads[i] = new Thread(runn[i]);
				threads[i].setName("BKMVTI: " + Integer.toString(i + 1));
				threads[i].start();

			}
			// Wait for the threads to finish
			Thread waitThread = new Thread(() -> {
				try {
					for (int i = 0; i < threads.length; i++) {

						try {
							System.out.println("i " + i);
							threads[i].join();
						} catch (InterruptedException e) {
							String stackTrace = Throwables.getStackTraceAsString(e);
							logger.info("Exception is=>{}", stackTrace);
						}
					}
					FileRequest.print("Day Operation ended with sucess ", FileRequest.getLineNumber());
					logger.info("days length=>{}", days.size());
					// CollectionUtils.filter(days, x -> x != null);
					/*
					 * dayOperationFransaBankRepository.saveAll(days); days.clear();
					 */
					uapfilBc.parallelUAP(name, fileIntegration);

				} catch (Exception e) {
					batchRepo.updateFinishBatch("Execute", 2, new Date());
					String stackTrace = Throwables.getStackTraceAsString(e);
					logger.info("Exception is=>{}", stackTrace);
				}
			});
			waitThread.setName("status bkmvti");
			waitThread.start();

		}
	}

	@Autowired
	SummaryCompensationOnUsRepository summaryCompensationOnUsRepository;

	public String getZero(int count) {
		String zero = "";
		for (int i = 0; i < count; i++)
			zero += "0";
		return zero;
	}

	public synchronized int getEveIndex() {
		return (++eve);
	}

	public synchronized int getEveIndex1() {
		return (++eve1);
	}

	public String getSpace(int count) {
		String Space = "";
		for (int i = 0; i < count; i++)
			Space += " ";
		return Space;
	}

	public static int eve1 = 0;
	public static int eve = 0;

	public String toString(Object c) {
		return String.valueOf(c);
	}

}
