package com.mss.backOffice.controller;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.google.gson.Gson;
import com.mss.unified.entities.DayOperationFransaBank;
import com.mss.unified.entities.UAPFransaBank;
import com.mss.unified.entities.User;
import com.mss.unified.repositories.BatchesFFCRepository;
import com.mss.unified.repositories.BkmvtiFransaBankRepository;
import com.mss.unified.repositories.CodeBankBCRepository;
import com.mss.unified.repositories.DayOperationFransaBankRepository;
import com.mss.unified.repositories.MvbkConfigRepository;
import com.mss.unified.repositories.MvbkSettlementRepository;
import com.mss.unified.repositories.UAPFransaBankRepository;
import com.mss.unified.repositories.UserRepository;

import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
@RestController
@RequestMapping("executorThreadUAPFileBC")
public class ExecutorThreadUAPFileBC {

	@Autowired
	public MvbkConfigRepository mvbkConfigR;
	@Autowired
    DayOperationFransaBankRepository dayOperationFransaBankRepository;
    @Autowired
    CodeBankBCRepository codeBankBCRepository;
	@Autowired
	BatchesFFCRepository batchRepo;
	@Autowired
	ExecutorThreadUAPINFileBC executorThreadUAPINFileBC;
	 @Autowired
	    UserRepository userRepository;
    private static final Logger logger = LoggerFactory.getLogger(ExecutorThreadUAPFileBC.class);
    public static boolean catchException;
    
    @Autowired
    UAPFransaBankRepository uAPFransaBankRepository;
    @Autowired
    MvbkSettlementRepository mvbkSettlementRepository;
    @Autowired
    BkmvtiFransaBankRepository bkmvtiFransaBankRepository;
    @GetMapping("executeUAP")
    public void parallelUAP(String name,String fileIntegration) throws ParseException {
        logger.info("executeUAP");
        catchException=false;


        if (!catchException) {
           
            Runnable thread1 = new GenerateUAPFileThread(dayOperationFransaBankRepository,"1",uAPFransaBankRepository,mvbkSettlementRepository,bkmvtiFransaBankRepository,name,fileIntegration,mvbkConfigR);
           
            Thread x=   new Thread(thread1) ;
            x.start();
            doAfterEnd(x,name,fileIntegration);
          
         

        }}
	public  void doAfterEnd (Thread x,String name,String fileIntegration) {
        // Wait for the threads to finish
        Thread waitThread = new Thread(() -> {
            try {
            	x.join();
//                for (int i = 0; i < threads.length; i++) {
//
//                    try {
//                        System.out.println("i " + i);
//                        threads[i].join();
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                    }
//                }
                FileRequest.print("ExecutorThreadUAPFileBC ended with sucess ", FileRequest.getLineNumber());
                executorThreadUAPINFileBC.parallelUAPIN(name,fileIntegration);

            } catch (Exception e) {
                batchRepo.updateFinishBatch("Execute", 2, new Date());
                e.printStackTrace();
            }
        });
        waitThread.setName("status executorThreadUAPINFileBC");
        waitThread.start();
	}
}
