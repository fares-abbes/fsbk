
package com.mss.backOffice.controller;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;
import com.google.gson.Gson;
import com.mss.backOffice.request.BranchFilter;
import com.mss.backOffice.request.CardReportingRequest;
import com.mss.backOffice.request.TransactionReportingRequest;
import com.mss.unified.entities.NationalAcquirerFiid;
import com.mss.unified.repositories.AccountRepository;
import com.mss.unified.repositories.CardRepository;
import com.mss.unified.repositories.NationalAcquirerFiidRepository;
import com.mss.unified.repositories.SwitchRepository;
import com.mss.unified.repositories.SwitchTransactionArchiveRepository;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("readFile")
public class ReadFileController {
	@Autowired
	SwitchTransactionArchiveRepository switchTransactionArchiveRepository;
    @Autowired
    CardRepository cardRepository;

    @Autowired
    AccountRepository accountRepository;
    
    @Autowired
    SwitchRepository switchRepository;
    @Autowired
    NationalAcquirerFiidRepository nationalAcquirerFiidRepository;
    private static final Logger logger = LoggerFactory.getLogger(ReadFileController.class);
    private static final Gson gson = new Gson();
    
    
    @GetMapping(value ="getAllBankCodes")
    public List<NationalAcquirerFiid> getAllBankCodes(){
    	return nationalAcquirerFiidRepository.findAll();
    }


    @PostMapping(value ="reportingCard")
    public @ResponseBody List<Object[]> reportingCard(@RequestBody CardReportingRequest cardReportingRequest) {
    	logger.info("******************begin reporting cards******************");
    	  List<Object[]> card =new ArrayList<Object[]>();
    	if (!cardReportingRequest.getStartDate().equals("") && !cardReportingRequest.getEndDate().equals("")) {
    		card = cardRepository.reportinWithInerval(
            		cardReportingRequest.getName(),
            		cardReportingRequest.getPan(),
            		cardReportingRequest.getCardStatus(),
            		cardReportingRequest.getAddress(),
            		cardReportingRequest.getAgencyCode(),
            		cardReportingRequest.getAccountNum(),
            		cardReportingRequest.getStartDate(),
            		cardReportingRequest.getExpiryDate(),
            		cardReportingRequest.getModifDate(),
            		cardReportingRequest.getEndDate(),
            		cardReportingRequest.getRadical(),
            		cardReportingRequest.getIsFromMobile()
            		);
    		
    	}else {
    		
    		card = cardRepository.reporting(
            		cardReportingRequest.getName(),
            		cardReportingRequest.getPan(),
            		cardReportingRequest.getCardStatus(),
            		cardReportingRequest.getAddress(),
            		cardReportingRequest.getAgencyCode(),
            		cardReportingRequest.getAccountNum(),
            		cardReportingRequest.getStartDate(),
            		cardReportingRequest.getExpiryDate(),
            		cardReportingRequest.getModifDate(),
            		cardReportingRequest.getRadical(),
            		cardReportingRequest.getIsFromMobile()
            		);
    	}
    	
    	if(card.size()>0) {
		
    		card.forEach(element ->{
			
			element[9] = String.valueOf(element[9]).equals("true") ? "YES": "NO";

			});
			}
    	logger.info("******************end reporting cards******************");
        return card;
    }
    
    
    @PostMapping(value ="reportingCardToCancel")
    public @ResponseBody List<Object[]> reportingCardToCancel(@RequestBody BranchFilter cardReportingRequest) {
    	logger.info("******************begin reporting cards to cancel******************");

    	return cardRepository.reportingCardsToDelete(cardReportingRequest.getBranch());
       
    }
    
    
    private String convert(String amount, String currency) {
        if (amount==null || amount.equals("null") || amount.equals("NULL"))
            return "";
        int number = Integer.parseInt(amount);
        String str = String.valueOf(number);
        if (str.length() < 3) {
            int i = str.length();
            while (i < 3) {
                str = "0" + str;
                i++;
            }
        }
        String result = "";
        if (currency.equals("788")) {
            result = str.substring(0, (str.length()) - 3) + "." + str.substring(str.length() - 3);
        } else {
            result = str.substring(0, str.length() - 2) + "." + str.substring(str.length() - 2);
        }
        if (result.substring(0, 1).equals("."))
            result = "0" + result;
 
        return result;
    }
	
    @PostMapping(value ="reportingTransactions/{docType}")
	public @ResponseBody List<Object[]> reportingTransactions(
			@PathVariable(value = "docType" ) String docType,
			@RequestBody TransactionReportingRequest transReportingRequest) {
		logger.info("******************begin reporting transactions******************");
		List<Object[]> trans = new ArrayList<Object[]>();

		if (!transReportingRequest.getDate().equals("") && !transReportingRequest.getEndDate().equals("")) {
			// with date interval
			if (transReportingRequest.getReversal().equals("oui")) {
				trans = switchRepository.reportingWtihStartDateAndReversal(transReportingRequest.getPan(),
						transReportingRequest.getAmount(), transReportingRequest.getResponseCode(),
						transReportingRequest.getMerchantId(), transReportingRequest.getAuthCode(),
						transReportingRequest.getTerminal(), transReportingRequest.getDate(),
						transReportingRequest.getEndDate(), "R", transReportingRequest.getFiid(),
						transReportingRequest.getTransactionCode(),
						transReportingRequest.getPosEntry(),
						transReportingRequest.getResponseCodeIso());
				trans.addAll(switchTransactionArchiveRepository.reportingWtihStartDateAndReversal(transReportingRequest.getPan(),
						transReportingRequest.getAmount(), transReportingRequest.getResponseCode(),
						transReportingRequest.getMerchantId(), transReportingRequest.getAuthCode(),
						transReportingRequest.getTerminal(), transReportingRequest.getDate(),
						transReportingRequest.getEndDate(), "R", transReportingRequest.getFiid(),
						transReportingRequest.getTransactionCode(),
						transReportingRequest.getPosEntry(),
						transReportingRequest.getResponseCodeIso(),
						Sort.by("switchRequestDate").descending()));

			} else if (transReportingRequest.getReversal().equals("non")) {
				trans = switchRepository.reportingWtihStartDateAndNoReversal(transReportingRequest.getPan(),
						transReportingRequest.getAmount(), transReportingRequest.getResponseCode(),
						transReportingRequest.getMerchantId(), transReportingRequest.getAuthCode(),
						transReportingRequest.getTerminal(), transReportingRequest.getDate(),
						transReportingRequest.getEndDate(), "R", transReportingRequest.getFiid(),
						transReportingRequest.getTransactionCode(),
						transReportingRequest.getPosEntry(),
						transReportingRequest.getResponseCodeIso() );
				
				trans.addAll(switchTransactionArchiveRepository.reportingWtihStartDateAndNoReversal(transReportingRequest.getPan(),
						transReportingRequest.getAmount(), transReportingRequest.getResponseCode(),
						transReportingRequest.getMerchantId(), transReportingRequest.getAuthCode(),
						transReportingRequest.getTerminal(), transReportingRequest.getDate(),
						transReportingRequest.getEndDate(), "R", transReportingRequest.getFiid(),
						transReportingRequest.getTransactionCode(),
						transReportingRequest.getPosEntry(),
						transReportingRequest.getResponseCodeIso(),
						Sort.by("switchRequestDate").descending()));
			} else {
				trans = switchRepository.reportingWtihStartDateAndReversal(transReportingRequest.getPan(),
						transReportingRequest.getAmount(), transReportingRequest.getResponseCode(),
						transReportingRequest.getMerchantId(), transReportingRequest.getAuthCode(),
						transReportingRequest.getTerminal(), transReportingRequest.getDate(),
						transReportingRequest.getEndDate(), "", transReportingRequest.getFiid(),
						transReportingRequest.getTransactionCode(),
						transReportingRequest.getPosEntry(),
						transReportingRequest.getResponseCodeIso() );
				trans.addAll(switchTransactionArchiveRepository.reportingWtihStartDateAndReversal(transReportingRequest.getPan(),
						transReportingRequest.getAmount(), transReportingRequest.getResponseCode(),
						transReportingRequest.getMerchantId(), transReportingRequest.getAuthCode(),
						transReportingRequest.getTerminal(), transReportingRequest.getDate(),
						transReportingRequest.getEndDate(), "", transReportingRequest.getFiid(),
						transReportingRequest.getTransactionCode(),
						transReportingRequest.getPosEntry(),
						transReportingRequest.getResponseCodeIso(),
						Sort.by("switchRequestDate").descending()));
			}

		} else {
			// with no date interval
			if (transReportingRequest.getReversal().equals("oui")) {
				trans = switchRepository.reportingWtihReversal(transReportingRequest.getPan(),
						transReportingRequest.getAmount(), transReportingRequest.getResponseCode(),
						transReportingRequest.getMerchantId(), transReportingRequest.getAuthCode(),
						transReportingRequest.getTerminal(), "R", transReportingRequest.getFiid(),
						transReportingRequest.getTransactionCode(),
						transReportingRequest.getPosEntry(),
						transReportingRequest.getResponseCodeIso() 

				);
				trans.addAll(switchTransactionArchiveRepository.reportingWtihReversal(transReportingRequest.getPan(),
						transReportingRequest.getAmount(), transReportingRequest.getResponseCode(),
						transReportingRequest.getMerchantId(), transReportingRequest.getAuthCode(),
						transReportingRequest.getTerminal(), "R", transReportingRequest.getFiid(),
						transReportingRequest.getTransactionCode(),
						transReportingRequest.getPosEntry(),
						transReportingRequest.getResponseCodeIso(),
						Sort.by("switchRequestDate").descending()

				));

			} else if (transReportingRequest.getReversal().equals("non")) {

				trans = switchRepository.reportingWtihNoReversal(transReportingRequest.getPan(),
						transReportingRequest.getAmount(), transReportingRequest.getResponseCode(),
						transReportingRequest.getMerchantId(), transReportingRequest.getAuthCode(),
						transReportingRequest.getTerminal(), "R",
						transReportingRequest.getFiid(),
						transReportingRequest.getTransactionCode(),
						transReportingRequest.getPosEntry(),
						transReportingRequest.getResponseCodeIso() );
				trans.addAll(switchTransactionArchiveRepository.reportingWtihNoReversal(transReportingRequest.getPan(),
						transReportingRequest.getAmount(), transReportingRequest.getResponseCode(),
						transReportingRequest.getMerchantId(), transReportingRequest.getAuthCode(),
						transReportingRequest.getTerminal(), "R",
						transReportingRequest.getFiid(),
						transReportingRequest.getTransactionCode(),
						transReportingRequest.getPosEntry(),
						transReportingRequest.getResponseCodeIso(),
						Sort.by("switchRequestDate").descending()));
			} else {
				trans = switchRepository.reportingWtihReversal(transReportingRequest.getPan(),
						transReportingRequest.getAmount(), transReportingRequest.getResponseCode(),
						transReportingRequest.getMerchantId(), transReportingRequest.getAuthCode(),
						transReportingRequest.getTerminal(), "", transReportingRequest.getFiid(),
						transReportingRequest.getTransactionCode(),
						transReportingRequest.getPosEntry(),
						transReportingRequest.getResponseCodeIso() 

				);
				trans.addAll(switchTransactionArchiveRepository.reportingWtihReversal(transReportingRequest.getPan(),
						transReportingRequest.getAmount(), transReportingRequest.getResponseCode(),
						transReportingRequest.getMerchantId(), transReportingRequest.getAuthCode(),
						transReportingRequest.getTerminal(), "", transReportingRequest.getFiid(),
						transReportingRequest.getTransactionCode(),
						transReportingRequest.getPosEntry(),
						transReportingRequest.getResponseCodeIso(),
						Sort.by("switchRequestDate").descending()

				));
			}

		}
		
		if(trans.size()>0) {
			double i = trans.get(0)[3]=="788" ? 1000.0: 100.0 ;
			DecimalFormat decimalFormat = new DecimalFormat("#0.00");
			trans.forEach(element ->{
			element[2] = docType.equals("EXCEL") ? 
			String.valueOf(element[2]) : 
			decimalFormat.format(Double.parseDouble(String.valueOf(element[2] ))/i).replace(".", ",");
			element[9] = String.valueOf(element[9]).substring(0,2);

			});
			}
		logger.info("******************end reporting transactions******************");

		return trans;
	}

}
