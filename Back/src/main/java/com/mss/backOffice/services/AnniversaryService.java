package com.mss.backOffice.services;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.google.common.base.Throwables;
import com.mss.unified.entities.Account;
import com.mss.unified.entities.Card;
import com.mss.unified.entities.DayOperationCardFransaBank;
import com.mss.unified.entities.DayOperationFeesCardSequence;
import com.mss.unified.entities.OperationCodeCommision;
import com.mss.unified.entities.Product;
import com.mss.unified.entities.SettelementFransaBank;
import com.mss.unified.entities.TvaCommissionFransaBank;
import com.mss.unified.repositories.AccountRepository;
import com.mss.unified.repositories.CardRepository;
import com.mss.unified.repositories.DayOperationCardFransaBankRepository;
import com.mss.unified.repositories.OperationCodeRepository;
import com.mss.unified.repositories.ProductRepositpry;
import com.mss.unified.repositories.SettelementFransaBankRepository;
import com.mss.unified.repositories.TvaCommissionFransaBankRepository;

@Service
public class AnniversaryService {
	@Autowired
	private CardRepository cardRepository;
	@Autowired
	private ProductRepositpry productRepository;
	@Autowired
	private DayOperationCardSequenceService dayOperationCardSequenceService;
	@Autowired
	private TvaCommissionFransaBankRepository tvaRepo;
	@Autowired
	private SettelementFransaBankRepository settlementRepo;
	@Autowired
	private DayOperationCardFransaBankRepository operationRepo;
    @Autowired
    private AccountRepository accountRepository;
    @Autowired
	private OperationCodeRepository operationCodeInternationalRepository;
	
    
	private static final Logger logger = LoggerFactory.getLogger(AnniversaryService.class);
	
	private SimpleDateFormat dayFormat = new SimpleDateFormat("dd");
	private SimpleDateFormat dayAndMonthFormat = new SimpleDateFormat("dd/MM");
	private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
	private SimpleDateFormat timeFormat = new SimpleDateFormat("HHmmss");
	private SimpleDateFormat filedateFormat = new SimpleDateFormat("yyMMdd");
	private SimpleDateFormat datePieceComptableFormat = new SimpleDateFormat("ddMMyy");
	
	
	private List<DayOperationCardFransaBank> saveDayOperations(Card card,Date date,int amount,
			Account account,List<DayOperationCardFransaBank> dayOperations,
			Product product,SettelementFransaBank settlc011,SettelementFransaBank settlc012,SettelementFransaBank settlc019,SettelementFransaBank settlc020,OperationCodeCommision settlc021,OperationCodeCommision settlc022) {
		String transactionDate=dateFormat.format(date);
		String transactionTime=timeFormat.format(date);
		
		
	
		TvaCommissionFransaBank tvaCommissionFransaBank = tvaRepo.findTva();
		float tvaBase=Float.parseFloat(tvaCommissionFransaBank.getTva())  /100;
		
		float tva=tvaBase+1;
		logger.info("TVA is => {}",tva);
		
		
		int settlementAmountHt=Math.round(amount/tva);
		logger.info("settlementAmountHt is => {}",settlementAmountHt);
		
		int settlementAmountTva=amount-settlementAmountHt;
		
		logger.info("settlementAmountTva is => {}",settlementAmountTva);		
	
		logger.info("transactionAmount is => {}",amount);
		
		String montantTrans=String.format("%014d",amount);
		montantTrans=montantTrans.substring(0,12)+"."+montantTrans.substring(12);
		
		DayOperationFeesCardSequence seq=dayOperationCardSequenceService.getSequence();
		DayOperationFeesCardSequence  seqPieceComptable=dayOperationCardSequenceService.getSequencePieceComptable();
		if (product.getLibelle().toLowerCase().contains("visa")) {
			DayOperationCardFransaBank C021 = new DayOperationCardFransaBank();
			C021.setCodeAgence(card.getAgencyCode());
			C021.setCodeBankAcquereur("035");
			C021.setCodeBank("035");
			C021.setCompteDebit("0"+(account.getCurrency().equals("012")? account.getAccountNum(): account.getAccountNumAttached()));
			C021.setDateTransaction(transactionDate);
			C021.setHeureTransaction(transactionTime);
			C021.setCompteCredit(settlc021.getCreditAccount());
			C021.setNumCartePorteur(card.getCardNum());
			C021.setNumtransaction(	String.format("%012d", seq.getSequence()));
			C021.setIdenfication(settlc021.getIdentification());
			C021.setMontantSettlement(settlementAmountHt);
			C021.setMontantTransaction(montantTrans);
			C021.setLibelleCommercant("FRAIS FAB CARTE RET/PAIEMENT");
			
			C021.setPieceComptable(String.format("%06d", seqPieceComptable.getSequence()));
			C021.setRefernceLettrage(datePieceComptableFormat.format(date)+card.getCardNum());
			C021.setFileDate(filedateFormat.format(date));
			C021.setNumAutorisation(C021.getNumtransaction());
			C021.setNumRefTransaction(C021.getNumtransaction());
			C021.setCardProduct("visa");
			
			DayOperationCardFransaBank C022 = new DayOperationCardFransaBank();
			
			C022.setCodeAgence(card.getAgencyCode());
			C022.setCodeBankAcquereur("035");
			C022.setCodeBank("035");
			C022.setCompteDebit("0"+(account.getCurrency().equals("022")? account.getAccountNum(): account.getAccountNumAttached()));
			C022.setDateTransaction(transactionDate);
			C022.setHeureTransaction(transactionTime);
			C022.setCompteCredit(settlc022.getCreditAccount());
			C022.setNumCartePorteur(card.getCardNum());
			C022.setNumtransaction(	String.format("%012d", seq.getSequence()));
			C022.setIdenfication(settlc022.getIdentification());
			C022.setMontantSettlement(settlementAmountTva);
			C022.setMontantTransaction(montantTrans);
			C022.setLibelleCommercant("FRAIS FAB CARTE RET/PAIEMENT");
			
			C022.setPieceComptable(String.format("%06d", seqPieceComptable.getSequence()));
			C022.setRefernceLettrage(datePieceComptableFormat.format(date)+card.getCardNum());
			C022.setFileDate(filedateFormat.format(date));
			C022.setNumAutorisation(C022.getNumtransaction());
			C022.setNumRefTransaction(C022.getNumtransaction());
			
			 C022.setCardProduct("visa");
			 C021.setCodeAgence(card.getAgencyCode());
			 C022.setCodeAgence(card.getAgencyCode());
			 dayOperations.add(C021);
		    dayOperations.add(C022);
		}
		else if (product.getLibelle().toLowerCase().contains("gold")) {
		
			DayOperationCardFransaBank C011 = new DayOperationCardFransaBank();
			
			C011.setCodeAgence(card.getAgencyCode());
			C011.setCodeBankAcquereur("035");
			C011.setCodeBank("035");
			C011.setCompteDebit("0"+(account.getCurrency().equals("012")? account.getAccountNum(): account.getAccountNumAttached()));
			C011.setDateTransaction(transactionDate);
			C011.setHeureTransaction(transactionTime);
			C011.setCompteCredit(settlc011.getCreditAccount());
			C011.setNumCartePorteur(card.getCardNum());
			C011.setNumtransaction(	String.format("%012d", seq.getSequence()));
			C011.setIdenfication(settlc011.getIdentificationbh());
			C011.setMontantSettlement(settlementAmountHt);
			C011.setMontantTransaction(montantTrans);
			C011.setLibelleCommercant("FRAIS FAB CARTE RET/PAIEMENT");
			
			C011.setPieceComptable(String.format("%06d", seqPieceComptable.getSequence()));
			C011.setRefernceLettrage(datePieceComptableFormat.format(date)+card.getCardNum());
			C011.setFileDate(filedateFormat.format(date));
			C011.setNumAutorisation(C011.getNumtransaction());
			C011.setNumRefTransaction(C011.getNumtransaction());
			 C011.setCardProduct("gold");
				
			
		//	seq=dayOperationCardSequenceService.incrementSequence(seq);
			
			DayOperationCardFransaBank C012 = new DayOperationCardFransaBank();
			
			C012.setCodeAgence(card.getAgencyCode());
			C012.setCodeBankAcquereur("035");
			C012.setCodeBank("035");
			C012.setCompteDebit("0"+(account.getCurrency().equals("012")? account.getAccountNum(): account.getAccountNumAttached()));
			C012.setDateTransaction(transactionDate);
			C012.setHeureTransaction(transactionTime);
			C012.setCompteCredit(settlc012.getCreditAccount());
			C012.setNumCartePorteur(card.getCardNum());
			C012.setNumtransaction(	String.format("%012d", seq.getSequence()));
			C012.setIdenfication(settlc012.getIdentificationbh());
			C012.setMontantSettlement(settlementAmountTva);
			C012.setMontantTransaction(montantTrans);
			C012.setLibelleCommercant("FRAIS FAB CARTE RET/PAIEMENT");
			
			C012.setPieceComptable(String.format("%06d", seqPieceComptable.getSequence()));
			C012.setRefernceLettrage(datePieceComptableFormat.format(date)+card.getCardNum());
			C012.setFileDate(filedateFormat.format(date));
			C012.setNumAutorisation(C012.getNumtransaction());
			C012.setNumRefTransaction(C012.getNumtransaction());
			
			 C012.setCardProduct("gold");
			 C011.setCodeAgence(card.getAgencyCode());
			 C012.setCodeAgence(card.getAgencyCode());
			 dayOperations.add(C011);
		    dayOperations.add(C012);
		}else {
		
			DayOperationCardFransaBank C019 = new DayOperationCardFransaBank();
			
			C019.setCodeAgence(card.getAgencyCode());
			C019.setCodeBankAcquereur("035");
			C019.setCodeBank("035");
			C019.setCompteDebit("0"+(account.getCurrency().equals("012")? account.getAccountNum(): account.getAccountNumAttached()));
			C019.setDateTransaction(transactionDate);
			C019.setHeureTransaction(transactionTime);
			C019.setCompteCredit(settlc019.getCreditAccount());
			C019.setNumCartePorteur(card.getCardNum());
			C019.setNumtransaction(	String.format("%012d", seq.getSequence()));
			C019.setIdenfication(settlc019.getIdentificationbh());
			C019.setMontantSettlement(settlementAmountHt);
			C019.setMontantTransaction(montantTrans);
			C019.setLibelleCommercant("FRAIS FAB CARTE RET/PAIEMENT");
			
			C019.setPieceComptable(String.format("%06d", seqPieceComptable.getSequence()));
			C019.setRefernceLettrage(datePieceComptableFormat.format(date)+card.getCardNum());
			C019.setFileDate(filedateFormat.format(date));
			C019.setNumAutorisation(C019.getNumtransaction());
			C019.setNumRefTransaction(C019.getNumtransaction());
			 C019.setCardProduct("classic");
			
			
		//	seq=dayOperationCardSequenceService.incrementSequence(seq);
			
			DayOperationCardFransaBank C020 = new DayOperationCardFransaBank();
			
			C020.setCodeAgence(card.getAgencyCode());
			C020.setCodeBankAcquereur("035");
			C020.setCodeBank("035");
			C020.setCompteDebit("0"+(account.getCurrency().equals("012")? account.getAccountNum(): account.getAccountNumAttached()));
			C020.setDateTransaction(transactionDate);
			C020.setHeureTransaction(transactionTime);
			C020.setCompteCredit(settlc020.getCreditAccount());
			C020.setNumCartePorteur(card.getCardNum());
			C020.setNumtransaction(	String.format("%012d", seq.getSequence()));
			C020.setIdenfication(settlc020.getIdentificationbh());
			C020.setMontantSettlement(settlementAmountTva);
			C020.setMontantTransaction(montantTrans);
			C020.setLibelleCommercant("FRAIS FAB CARTE RET/PAIEMENT");
			
			C020.setPieceComptable(String.format("%06d", seqPieceComptable.getSequence()));
			C020.setRefernceLettrage(datePieceComptableFormat.format(date)+card.getCardNum());
			C020.setFileDate(filedateFormat.format(date));
			C020.setNumAutorisation(C020.getNumtransaction());
			C020.setNumRefTransaction(C020.getNumtransaction());
			
			 C020.setCardProduct("classic");
			 C019.setCodeAgence(card.getAgencyCode());
				C020.setCodeAgence(card.getAgencyCode());
				dayOperations.add(C019);
				dayOperations.add(C020);
			
			
		
		}
	
		seq=dayOperationCardSequenceService.incrementSequence(seq);
		seqPieceComptable=dayOperationCardSequenceService.incrementSequencePieceComptable(seqPieceComptable);

		return dayOperations;
	}
	
	
	@Scheduled(cron = "0 1 0 * * ?")
	public void anniversaryCron() throws Exception {
		logger.info("Begin anniversary cron");
		
		try {
			Date currentDay=new Date();
			Calendar cal = Calendar.getInstance();
			cal.setTime(currentDay);
			int lastDay = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
        	LocalDate localDate = currentDay.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        	
        	logger.info("month is => {}",localDate.getMonthValue());
        	logger.info("day is => {}",localDate.getDayOfMonth());
        	List<Card> cardsMonthly= new ArrayList<Card>();
        	List<Card> cardsAnnual= new ArrayList<Card>();
        	List<String> days=new ArrayList<String>();
        	List<String> daysAndMonth=new ArrayList<String>();

        	SettelementFransaBank settlc011 =settlementRepo.findByIdentificationbh("C011");
			SettelementFransaBank settlc012 =settlementRepo.findByIdentificationbh("C012");
			
			SettelementFransaBank settlc019 =settlementRepo.findByIdentificationbh("C019");
			SettelementFransaBank settlc020 =settlementRepo.findByIdentificationbh("C020");
			
			OperationCodeCommision settlc021 = operationCodeInternationalRepository.findByIdentification("C021").get();
			OperationCodeCommision settlc022 = operationCodeInternationalRepository.findByIdentification("C022").get();
			
			
        	if (localDate.getMonthValue()==2 && localDate.getDayOfMonth()==28 && localDate.getDayOfMonth()==lastDay) {
        		
        		days=new ArrayList<String>(
        					Arrays.asList("28", "29", "30", "31"));
        		
        		daysAndMonth=new ArrayList<String>(
    					Arrays.asList("28/02", "29/02"));
        		

        		
        	}else if (localDate.getMonthValue()==2 && localDate.getDayOfMonth()==29 && localDate.getDayOfMonth()==lastDay) {
        		days=new ArrayList<String>(
    					Arrays.asList("29", "30", "31"));
        		
        		daysAndMonth=new ArrayList<String>(
    					Arrays.asList("29/02"));
        		
        	}else if (localDate.getDayOfMonth()==30 && localDate.getDayOfMonth()==lastDay)  {
        		days=new ArrayList<String>(
    					Arrays.asList("30", "31"));
        		
        		daysAndMonth=new ArrayList<String>(
    					Arrays.asList(  "30/"+String.format("%02d",localDate.getMonthValue()),
    							"31/"+String.format("%02d",localDate.getMonthValue())
    							
    							));
        		
        	}else {
        		days=new ArrayList<String>(
    					Arrays.asList(dayFormat.format(currentDay)));
        		
        		daysAndMonth=new ArrayList<String>(
    					Arrays.asList( dayAndMonthFormat.format(currentDay)
    							
    							));
        		
        	}
		

        	cardsMonthly=cardRepository.getCardsWithStartDateByDay(days);       	
//			    cardsAnnual=cardRepository.getCardsWithStartDateByDayAndMonth(dayAndMonthFormat.format(currentDay));
        	cardsAnnual=cardRepository.getCardsWithStartDateByDayAndMonth(daysAndMonth);
			
			logger.info("cardsMonthly size => {}",cardsMonthly.size());
			logger.info("cardsAnnual size => {}",cardsAnnual.size());

			List<DayOperationCardFransaBank> dayOperations = new ArrayList<DayOperationCardFransaBank>();
			
			
	
			for (Card card : cardsMonthly ) {
				if (localDate.isBefore(card.getExpiryDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate())) {
					Optional<Product> product =productRepository.findByProductCode(card.getProductCode());
					Optional<Account> account = accountRepository.findByAccountCode(card.getAccCode());
					if (product.isPresent() && account.isPresent() ) {
						if (product.get().getCpCommissionAnniversaryAndCreation()>0)
						dayOperations=saveDayOperations(card, currentDay, product.get().getCpCommissionAnniversaryAndCreation(),
								account.get(), dayOperations,product.get(),settlc011,settlc012,settlc019,settlc020,settlc021,settlc022);
					}
				}
				
			}

			for (Card card : cardsAnnual ) {
				if (localDate.isBefore(card.getExpiryDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate())) {
					Optional<Product> product =productRepository.findByProductCode(card.getProductCode());
					Optional<Account> account = accountRepository.findByAccountCode(card.getAccCode());
					if (product.isPresent() && account.isPresent()) {
						
						if (product.get().getCpCommissionCreation()>0)
							
						dayOperations=saveDayOperations(card, currentDay, product.get().getCpCommissionCreation(),
								account.get(), dayOperations,product.get(),settlc011,settlc012,settlc019,settlc020,settlc021,settlc022);

					}
				}
				
			}
			
			  operationRepo.saveAll(dayOperations);
		}catch(Exception e) {
			
	
			logger.info("Exception is=>{}", Throwables.getStackTraceAsString(e));
		}
		
	
		  logger.info("end anniversary cron");
	}

}
