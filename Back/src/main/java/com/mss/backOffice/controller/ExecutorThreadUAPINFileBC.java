package com.mss.backOffice.controller;

import java.text.ParseException;
import java.util.Date;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mss.unified.entities.User;
import com.mss.unified.repositories.BatchesFFCRepository;
import com.mss.unified.repositories.BkmvtiFransaBankRepository;
import com.mss.unified.repositories.CodeBankBCRepository;
import com.mss.unified.repositories.DayOperationFransaBankRepository;
import com.mss.unified.repositories.MvbkSettlementRepository;
import com.mss.unified.repositories.UAP040INFransaBankRepository;
import com.mss.unified.repositories.UAPFransaBankRepository;
import com.mss.unified.repositories.UserRepository;

@RestController
@RequestMapping("executorThreadUAPINFileBC")
public class ExecutorThreadUAPINFileBC {

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
	ExecutorThreadUAP050FileBC uAP050FransaBankRepository;
	@Autowired
	BatchesFFCRepository batchRepo;
	@GetMapping("executeUAPIN")
	public void parallelUAPIN(String name,String fileIntegration) throws ParseException {
		logger.info("executeUAP");
		catchException = false;

		if (!catchException) {
		
			Runnable thread1 = new GenerateUAPINFileThread(dayOperationFransaBankRepository, "1",
					uAP040INFransaBankRepository, mvbkSettlementRepository, bkmvtiFransaBankRepository,name,fileIntegration);
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
				uAP050FransaBankRepository.parallelUAP(name,fileIntegration);

			} catch (Exception e) {
				batchRepo.updateFinishBatch("Execute", 2, new Date());
				e.printStackTrace();
			}
		});
		waitThread.setName("status uAP050FransaBankRepository");
		waitThread.start();
	}
}
