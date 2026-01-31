package com.mss.backOffice.services;

import java.lang.reflect.InvocationTargetException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.common.base.Throwables;
import org.apache.commons.beanutils.PropertyUtils;

import com.mss.unified.entities.SwitchTransaction;
import com.mss.unified.entities.SwitchTransactionArchive;
import com.mss.unified.repositories.SwitchRepository;
import com.mss.unified.repositories.SwitchTransactionArchiveRepository;

@Service
public class TransactionArchiveService {
	private static final Logger logger = LoggerFactory.getLogger(TransactionArchiveService.class);

	@Autowired
	SwitchRepository switchRepository;
	@Autowired
	SwitchTransactionArchiveRepository switchTransactionArchiveRepository;


	@Transactional
	@Scheduled(cron = "0 0 3 * * ?")
	//@Scheduled(cron = "0 0/1 * * * ?")
	public void transactionArchive() {
	    try {
	        logger.info("******* Begin add switch archive");
	        processTransactions();
	        logger.info("******* End add switch archive");    
	    } catch (Exception e) {
	    	String stackTrace = Throwables.getStackTraceAsString(e);
	        logger.error("Exception occurred in transactionArchive method =>{}", stackTrace);
	    }
	}
	private void processTransactions() {

	
	    List<SwitchTransaction> listSwitchTransaction = switchRepository.getAllForSwitchArchive();
	    List<SwitchTransactionArchive> listSwitchTransactionArchive = new ArrayList<>();
        logger.info("switch transaction size =>{}",listSwitchTransaction.size());

	    for (SwitchTransaction switchTransaction : listSwitchTransaction) {
	        try {
	            SwitchTransactionArchive archive = convertToArchive(switchTransaction);
	            listSwitchTransactionArchive.add(archive);
	        } catch (Exception e) {
	        	String stackTrace = Throwables.getStackTraceAsString(e);
		        logger.error("Error processing transaction =>{}", stackTrace);
	        }
	    }
        logger.info("listSwitchTransactionArchive size =>{}", listSwitchTransactionArchive.size());

	    switchTransactionArchiveRepository.saveAll(listSwitchTransactionArchive);
	    switchRepository.deleteAll(listSwitchTransaction);
	}

	private SwitchTransactionArchive convertToArchive(SwitchTransaction switchTransaction) throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
	    SwitchTransactionArchive archive = new SwitchTransactionArchive();
	    LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String formattedDateTime = now.format(formatter);
	    archive.setDateArchive(formattedDateTime);
	    PropertyUtils.copyProperties(archive, switchTransaction);
	    return archive;
	}


}
