package com.mss.backOffice.controller;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.google.common.base.Throwables;
import com.mss.backOffice.services.PropertyService;
import com.mss.unified.entities.Account;
import com.mss.unified.repositories.AccountRepository;
import com.sleepycat.je.utilint.Timestamp;
import static com.mss.backOffice.BackOfficeApplication.occupied;
@RestController
@RequestMapping("balance")
public class IntegrationBalanceFile {
	@Autowired
	private PropertyService propertyService;
	@Autowired
	private AccountRepository accRepository;
	private static final Logger logger = LoggerFactory.getLogger(IntegrationBalanceFile.class);
	
	
	@Scheduled(cron = "0 0/1 * * * ?")
	public void readFile() throws IOException {
	
		if (!occupied) {
			
			
 			occupied=true;
			
			String archFileName="";
			try {
					File[] fileList = getFileList(propertyService.getSoldeFilePath());
					if (fileList!=null) {
						if (fileList.length>0) {
							logger.info("************ begin reading balance file**************");
				            for(File file : fileList) {
				            	logger.info("file name =>",file.getName());
				              
				            	
				                String filePath = propertyService.getSoldeFilePath() + "/" + file.getName();

				    			logger.info("filePath is    " + filePath);
				    			Timestamp timestamp = new Timestamp(System.currentTimeMillis());
				    			
				    			
				    			if (Files.exists(Paths.get(filePath))) {
				    				List<String> fileContent = Files.readAllLines(Paths.get(filePath));
				    				
				    				archFileName = propertyService.getSoldeArchiveFilePath() + "/AMP_SOLDE." + timestamp;
				    				logger.info("****archiving file***");
				    				//String archFileName = propertyService.getSoldeArchiveFilePath() + "/AMP_SOLDE." + timestamp;
				    				logger.info("archive name => {}",archFileName);
				    				Files.move(Paths.get(filePath), Paths.get(archFileName), StandardCopyOption.REPLACE_EXISTING);
				    				    				
				    				List<Account> accounts = new ArrayList<Account>();
				    				for (String line : fileContent) {
				    					String accountNum = line.substring(1, 20);
				    					logger.info("accountNum from file is    " + accountNum);
				    					Account account = accRepository.findByAccountNum(accountNum);
				    					if (account != null) {
				    						logger.info("account exist");
				    						String balanceFromFile = line.substring(40, 60);
				    						logger.info("balance From File is    " + balanceFromFile);
				    						String balance = balanceFromFile.substring(0, (balanceFromFile.length() - 1)).replace(".", "");
				    						logger.info("old acc available is    " + account.getAccountAvailable());
				    						if (balanceFromFile.charAt(balanceFromFile.length() - 1) == 'C')
				    							account.setAccountAvailable(new BigInteger(balance));
				    						else
				    							account.setAccountAvailable(new BigInteger(balance).multiply(new BigInteger("-1")));
				    						
				    						logger.info("new account available    " + account.getAccountAvailable());
				    						accounts.add(account);

				    					}else
				    						logger.info("account not found");

				    				}
				    				accRepository.saveAll(accounts);
				    				logger.info("account list to save size   => {} " , accounts.size());
				    				
				    				//return ResponseEntity.ok().body(gson.toJson("OPÉRATION TERMINÉE AVEC SUCCÈS "));
				    			} else
				    				logger.info("No file => {}",new Date());
				                
				                
				            }	
							
						}

						
					}
	
			} catch (Exception e) {
				String stackTrace = Throwables.getStackTraceAsString(e);
				logger.info("Exception is=>{}", stackTrace);
				if (!archFileName.equals("")) {
					if (Files.exists(Paths.get(archFileName))) {
						String exceptionName = propertyService.getSoldeArchiveFilePath() + "/"+propertyService.getSoldeFileName()+".TXT" ;
						logger.info("exceptionName => {}",exceptionName);
	    				Files.move(Paths.get(archFileName), Paths.get(exceptionName), StandardCopyOption.REPLACE_EXISTING);
					}
					
				}
			}
			occupied=false;
		}

	}

    private File[] getFileList(String dirPath) {
        File dir = new File(dirPath);   

        File[] fileList = dir.listFiles(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return name.startsWith(propertyService.getSoldeFileName());
                		
            }
        });
        return fileList;
    }

}
