package com.mss.backOffice.controller;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.google.common.base.Throwables;
import com.mss.backOffice.request.AddFileRequest;
import com.mss.unified.entities.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mss.unified.dto.TpDetailsControl;
import com.mss.unified.dto.UapDetailsControl;
import com.mss.unified.repositories.BatchesFFCRepository;
import com.mss.unified.repositories.BkmvtiFransaBankRepository;
import com.mss.unified.repositories.CodeBankBCRepository;
import com.mss.unified.repositories.DayOperationFransaBankRepository;
import com.mss.unified.repositories.FILECONTENTRepository;
import com.mss.unified.repositories.FileRepository;
import com.mss.unified.repositories.MvbkSettlementRepository;
import com.mss.unified.repositories.OpeningDayRepository;
import com.mss.unified.repositories.TpControlRespository;
import com.mss.unified.repositories.UAP050FransaBankRepository;
import com.mss.unified.repositories.UAP050INFransaBankRepository;
import com.mss.unified.repositories.UAP051FransaBankRepository;
import com.mss.unified.repositories.UAP051INFransaBankRepository;
import com.mss.unified.repositories.UAPFransaBankRepository;

@RestController
@RequestMapping("executorThreadUAP051INFileBC")
public class ExecutorThreadUAP051INFileBC {
	public static Authentication auth;

	@Autowired
	BatchesFFCRepository batchRepo;
	@Autowired
	DayOperationFransaBankRepository dayOperationFransaBankRepository;
	@Autowired
	CodeBankBCRepository codeBankBCRepository;
	private static final Logger logger = LoggerFactory.getLogger(ExecutorThreadUAP050FileBC.class);
	public static boolean catchException;

	@Autowired
	UAP051INFransaBankRepository uAP050INFransaBankRepository;
	@Autowired
	MvbkSettlementRepository mvbkSettlementRepository;
	@Autowired
	BkmvtiFransaBankRepository bkmvtiFransaBankRepository;
	@Autowired
	DownloadFileORD_UAP040 ord040;
	@Autowired
	DownloadFileORD_UAP050 ord050;
	@Autowired
	DownloadFileORD_UAP051 ord051;
	@Autowired
	DownloadFileUAP_040_FC lot040;
	@Autowired
	DownloadFileUAP_050_FC lot050;
	@Autowired
	DownloadFileUAP_051_FC lot051;
	@Autowired
	UAPFransaBankRepository up040;
	@Autowired
	UAP050FransaBankRepository up050;
	@Autowired
	UAP051FransaBankRepository up051;
	@Autowired
	OpeningDayRepository opdr;
	@Autowired
	FileRepository fileRepository;
	@Autowired
	FILECONTENTRepository tpDetailsRepository;
	@Autowired
	TpControlRespository tpControlRespository;
	@Autowired
	ChargeBacksController chargeBacksController;
	private String fileIntegration;

	@GetMapping("executeUAP050")
	public void parallelUAP(String name, String fileIntegration) throws ParseException {
		logger.info("executeUAP050");
		catchException = false;

		if (!catchException) {
			this.fileIntegration=fileIntegration;
			Runnable thread1 = new GenerateUAPFile051INThread(dayOperationFransaBankRepository, "1",
					uAP050INFransaBankRepository, mvbkSettlementRepository, bkmvtiFransaBankRepository,
					fileIntegration);
			Thread x = new Thread(thread1);
			x.start();
			doAfterEnd(x);

		}
	}

	public void doAfterEnd(Thread x) {
		// Wait for the threads to finish
		Thread waitThread = new Thread(() -> {
			try {
				x.join();
				ExecutorService executorService = Executors.newFixedThreadPool(10);
				auth = SecurityContextHolder.getContext().getAuthentication();
				CompletableFuture<ResponseEntity<?>> result1 = generateUAP052(executorService,fileIntegration);
				CompletableFuture<ResponseEntity<?>> result2 = generateUAP052IN( executorService,fileIntegration);

				// Wait for all CompletableFuture to complete
				CompletableFuture<Void> allOf = CompletableFuture.allOf(result1,result2 );
				allOf.join();
				// Check results and throw if any signaled an error
				ResponseEntity<?> r1 = result1.join();
				ResponseEntity<?> r2 = result2.join();
				if (!r1.getStatusCode().is2xxSuccessful() || !r2.getStatusCode().is2xxSuccessful()) {
					throw new RuntimeException("One or more generation tasks failed: r1=" + r1.getStatusCode() + ", r2=" + r2.getStatusCode());
				}
				FileRequest.print("all ended with sucess ", FileRequest.getLineNumber());
				FileRequest.print("starting generating lot & ord", FileRequest.getLineNumber());
				if (up040.getListUAPByStatusTechniqueNV(opdr.findByStatus040().get().getFileIntegration()) != null
						&& up040.getListUAPByStatusTechniqueNV(opdr.findByStatus040().get().getFileIntegration())
								.size() > 0) {
//					ord040.writeInORD040File();
					lot040.writeInUAPFile();
					Thread.sleep(1000);
				}
				if (up050.getListUAPByStatusTechniqueNV(opdr.findByStatus050().get().getFileIntegration()) != null
						&& up050.getListUAPByStatusTechniqueNV(opdr.findByStatus050().get().getFileIntegration())
								.size() > 0) {

//					ord050.writeInORD050File();
					lot050.writeInUAP050File();
					Thread.sleep(1000);
				}
				if (up051.getListUAPByStatusTechniqueNV(opdr.findByStatus051().get().getFileIntegration()) != null
						&& up051.getListUAPByStatusTechniqueNV(opdr.findByStatus051().get().getFileIntegration())
								.size() > 0) {

					ord051.writeInORD051File();
					lot051.writeInUAP051File();
				}
				
				// add control on TP 
				buildControlTp();
				
				FileRequest.print("files generated ", FileRequest.getLineNumber());
				BatchesFC batchS = batchRepo.findByKey("execStatus").get();
				FileRequest.print("updateStatus", FileRequest.getLineNumber());
				batchS.setBatchStatus(0);
				batchS.setBatchNumber(2);
				batchS.setBatchDate(new Date());
				batchS.setError(null);
				batchS.setErrorStackTrace(null);
				batchRepo.save(batchS);

				FileRequest.print("updateStatus" + batchS, FileRequest.getLineNumber());
				batchRepo.updateFinishBatch("Execute", 1, new Date());
				Thread.sleep(1000);
				batchRepo.updateFinishBatch("SENDLOT", -1, new Date());

			} catch (Exception e) {
				batchRepo.updateFinishBatch("Execute", 2, new Date());
				e.printStackTrace();
			}
		});
		waitThread.setName("status end everything");
		waitThread.start();
	}
	@Async
	public CompletableFuture<ResponseEntity<?>> generateUAP052(ExecutorService executorService,String fileIntegration) {

		FileRequest.print("strating", FileRequest.getLineNumber());
		CompletableFuture<ResponseEntity<?>> future = CompletableFuture.supplyAsync(() -> {
			// Call the fsbk040.Matching() method
            return chargeBacksController.generateUap052out(fileIntegration);
        }, executorService);
		return future;

	}
	@Async
	public CompletableFuture<ResponseEntity<?>> generateUAP052IN( ExecutorService executorService,String fileIntegration) {

		FileRequest.print("strating", FileRequest.getLineNumber());
		CompletableFuture<ResponseEntity<?>> future = CompletableFuture.supplyAsync(() -> {
			// Call the fsbk040.Matching() method
            return chargeBacksController.generateUap052IN(  fileIntegration );
        }, executorService);
		return future;

	}
//	@GetMapping("buildControlTp")
	private void buildControlTp() throws ParseException {
		logger.info("****begin buildControlTp******");
		ArrayList<String> transactionTypeUap040 = new ArrayList<String>(
				Arrays.asList("014", "040"));
		ArrayList<String> transactionTypeUap050 = new ArrayList<String>(
				Arrays.asList("050", "052", "053"));
		ArrayList<String> transactionTypeUap051 = new ArrayList<String>(
				Arrays.asList("051", "054", "055", "056"));
		
		String integrationDate =	opdr.findByStatus040().get().getFileIntegration();
		// Parse the integrationDate date string
		SimpleDateFormat inputFormat = new SimpleDateFormat("MMddyyyy");
         Date date = inputFormat.parse(integrationDate);
		
         // Format the date to the desired output format
         SimpleDateFormat outputFormat = new SimpleDateFormat("yyyy-MM-dd");
         String dateInSummaryFormat = outputFormat.format(date);
         
         List<FileHeader> tpSummaries=  fileRepository.findByFileprocessingDate(dateInSummaryFormat);
         List<TpControl> listTosave=new ArrayList<TpControl>();
        for (FileHeader summary:tpSummaries ) {
        	if (!tpControlRespository.findByFileName(summary.getFileName()+summary.getFileDate()).isPresent()) {
        		

            	List<TpDetailsControl> details040=  tpDetailsRepository.findTpDetailsForUap040(String.valueOf (summary.getId()),transactionTypeUap040); 
            	List<TpDetailsControl> details050=  tpDetailsRepository.findTpDetailsForUap050(String.valueOf (summary.getId()),transactionTypeUap050); 
            	List<TpDetailsControl> details051=  tpDetailsRepository.findTpDetailsForUap051(String.valueOf (summary.getId()),transactionTypeUap051); 
            	
            	
            	
            	TpControl tpControl = new TpControl();
            	tpControl.setProcessingDate(dateInSummaryFormat);
            	tpControl.setFileName(summary.getFileName()+summary.getFileDate());
            	long sumTp=0;
            	for (TpDetailsControl el:details040) {
        
            		sumTp=sumTp+(
            				Long.valueOf(el.getMontantTransaction().replace(".", "")) +
            				Long.valueOf(el.getCommisionInterchange().replace(".", ""))
            				
            			) ;
            		
            	}
            	
    			for (TpDetailsControl el : details050) {
    				sumTp=sumTp+(
            				Long.valueOf(el.getMontantTransaction().replace(".", "")) -
            				Long.valueOf(el.getCommisionInterchange().replace(".", ""))
            				
            			) ;
    			}

    			for (TpDetailsControl el : details051) {
    				sumTp=sumTp+(
            				Long.valueOf(el.getMontantTransaction().replace(".", "")) +
            				Long.valueOf(el.getCommisionInterchange().replace(".", ""))
            				
            			) ;
    			}
       	
            	long nbTotalTp= details040.size() + details050.size() + details051.size();
            	
            
            	tpControl.setTpTransactionsNb(nbTotalTp);
            	
//            	long sumTp =(details040!=null?details040.getMontant():0) +
//            			(details050!=null?details050.getMontant():0) +
//            			(details051!=null?details051.getMontant():0);
            	tpControl.setSumTp(sumTp);
            	
            	List<UapDetailsControl>  upa40= up040.getListUAP40ForControl(summary.getFileDate());
            	List<UapDetailsControl>  upa50= up050.getListUAP50ForControl(summary.getFileDate());
            	List<UapDetailsControl>  upa51= up051.getListUAP51ForControl(summary.getFileDate());
            	
            	long sumUap040=0;
            	long sumUap050=0;
            	long sumUap051=0;

            	for (UapDetailsControl el : upa40) {
            		sumUap040+=
            				Long.valueOf(el.getMontantAComponser()   .replace(".", "")) ;
    			}
            	
            	for (UapDetailsControl el : upa50) {
            		sumUap050+=
            				Long.valueOf(el.getMontantAComponser()   .replace(".", "")) ;
    			}
            	
            	for (UapDetailsControl el : upa51) {
            		sumUap051+=
            				Long.valueOf(el.getMontantAComponser()   .replace(".", "")) ;
    			}

            	tpControl.setUap40TransactionsNb(upa40.size());
            	tpControl.setUap50TransactionsNb(upa50.size());
            	tpControl.setUap51TransactionsNb(upa51.size());
            	
            	tpControl.setNbTotalPres(tpControl.getUap40TransactionsNb()+tpControl.getUap50TransactionsNb()+tpControl.getUap51TransactionsNb());

            	tpControl.setSumUap40(sumUap040);
            	tpControl.setSumUap50(sumUap050);
            	tpControl.setSumUap51(sumUap051);
            	
            	tpControl.setSumTotalPres(tpControl.getSumUap40()+tpControl.getSumUap50()+tpControl.getSumUap51());
            	listTosave.add(tpControl);
        		
        		
        	}	
        }
        if (listTosave.size()>0)
        	tpControlRespository.saveAll(listTosave);
		logger.info("****end buildControlTp******");
 
         
	}

}
