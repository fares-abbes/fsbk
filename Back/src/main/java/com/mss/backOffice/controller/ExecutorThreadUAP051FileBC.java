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
import com.mss.unified.repositories.MvbkConfigRepository;
import com.mss.unified.repositories.MvbkSettlementRepository;
import com.mss.unified.repositories.UAP040INFransaBankRepository;
import com.mss.unified.repositories.UAP051FransaBankRepository;
import com.mss.unified.repositories.UserRepository;
@RestController
@RequestMapping("executorThreadUAP051FileBC")
public class ExecutorThreadUAP051FileBC {

	@Autowired
	public MvbkConfigRepository mvbkConfigR;

	@Autowired
	DayOperationFransaBankRepository dayOperationFransaBankRepository;
	@Autowired
	CodeBankBCRepository codeBankBCRepository;
	private static final Logger logger = LoggerFactory.getLogger(ExecutorThreadUAPFileBC.class);
	public static boolean catchException;
	public static int eve1 = 0;
	public static int eve = 0;
	@Autowired
    UserRepository userRepository;
	@Autowired
	UAP040INFransaBankRepository uAP040INFransaBankRepository;
	@Autowired
	MvbkSettlementRepository mvbkSettlementRepository;
	@Autowired
	BkmvtiFransaBankRepository bkmvtiFransaBankRepository;
	@Autowired
	ExecutorThreadUAP051INFileBC uAP051INFransaBankRepository;
	@Autowired
	UAP051FransaBankRepository uAP051FransaBankRepository;
	@Autowired
	BatchesFFCRepository batchRepo;
	@GetMapping("executeUAP051")
	public void parallelUAP(String name,String fileIntegration) throws ParseException {
		logger.info("executeUAP");
		catchException = false;

		if (!catchException) {
		
			Runnable thread1 = new GenerateUAPFile051Thread(dayOperationFransaBankRepository, "1",
					uAP051FransaBankRepository, mvbkSettlementRepository, bkmvtiFransaBankRepository,name,fileIntegration,mvbkConfigR);
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
				FileRequest.print("uAP050FransaBankRepository ended with sucess ", FileRequest.getLineNumber());
				uAP051INFransaBankRepository.parallelUAP(name,fileIntegration);

			} catch (Exception e) {
				batchRepo.updateFinishBatch("Execute", 2, new Date());
				e.printStackTrace();
			}
		});
		waitThread.setName("status uAP050FransaBankRepository");
		waitThread.start();
	}

}
