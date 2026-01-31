package com.mss.backOffice.controller;

import java.text.ParseException;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mss.unified.repositories.BatchesFFCRepository;
import com.mss.unified.repositories.BkmvtiFransaBankRepository;
import com.mss.unified.repositories.CodeBankBCRepository;
import com.mss.unified.repositories.DayOperationFransaBankRepository;
import com.mss.unified.repositories.MvbkSettlementRepository;
import com.mss.unified.repositories.UAP050FransaBankRepository;
import com.mss.unified.repositories.UAP050INFransaBankRepository;

@RestController
@RequestMapping("executorThreadUAP050INFileBC")
public class ExecutorThreadUAP050INFileBC {
	@Autowired
	BatchesFFCRepository batchRepo;
	@Autowired
	DayOperationFransaBankRepository dayOperationFransaBankRepository;
	@Autowired
	CodeBankBCRepository codeBankBCRepository;
	private static final Logger logger = LoggerFactory.getLogger(ExecutorThreadUAP050FileBC.class);
	public static boolean catchException;

	@Autowired
	UAP050INFransaBankRepository uAP050INFransaBankRepository;
	@Autowired
	MvbkSettlementRepository mvbkSettlementRepository;
	@Autowired
	BkmvtiFransaBankRepository bkmvtiFransaBankRepository;
	@Autowired
	ExecutorThreadUAP051FileBC uAP051FransaBankRepository;

	@GetMapping("executeUAP050")
	public void parallelUAP(String name,String fileIntegration) throws ParseException {
		logger.info("executeUAP050");
		catchException = false;

		if (!catchException) {

			Runnable thread1 = new GenerateUAPFile050INThread(dayOperationFransaBankRepository, "1",
					uAP050INFransaBankRepository, mvbkSettlementRepository, bkmvtiFransaBankRepository,fileIntegration);
			Thread x = new Thread(thread1);
			x.start();
			doAfterEnd(x,name,fileIntegration);

		}
	}
	
	
	public void doAfterEnd(Thread x,String name,String fileIntegration) {
		// Wait for the threads to finish
		Thread waitThread = new Thread(() -> {
			try {
				x.join();
				FileRequest.print("uAP051FransaBankRepository ended with sucess ", FileRequest.getLineNumber());
				uAP051FransaBankRepository.parallelUAP(name,fileIntegration);

			} catch (Exception e) {
				batchRepo.updateFinishBatch("Execute", 2, new Date());
				e.printStackTrace();
			}
		});
		waitThread.setName("status uAP051FransaBankRepository");
		waitThread.start();
	}
	
	

/*	public void doAfterEnd(Thread x) {
		// Wait for the threads to finish
		Thread waitThread = new Thread(() -> {
			try {
				x.join();
				FileRequest.print("all ended with sucess ", FileRequest.getLineNumber());
				batchRepo.updateFinishBatch("Execute", 1, new Date());

			} catch (Exception e) {
				batchRepo.updateFinishBatch("Execute", 2, new Date());
				e.printStackTrace();
			}
		});
		waitThread.setName("status end everything");
		waitThread.start();
	}
*/
}
