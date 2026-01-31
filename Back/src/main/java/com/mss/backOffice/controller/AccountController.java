package com.mss.backOffice.controller;



import com.google.common.base.Strings;
import com.google.common.base.Throwables;
import com.google.gson.Gson;
import com.mss.backOffice.exception.ResourceNotFoundException;
import com.mss.backOffice.request.*;
import com.mss.backOffice.services.AccountService;
import com.mss.backOffice.services.DeletedMerchantService;
import com.mss.backOffice.services.GetTokenAmplitudeService;
import com.mss.unified.repositories.*;
import com.mss.unified.entities.*;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import com.mss.unified.services.GenerateCardService;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("account")
public
class AccountController {
 @Autowired
 UserRepository userRepository;
  @Autowired
  GenerateCardService generateCard;
  @Autowired
  BinOnUsRepository binOnUsRepository;
  @Autowired
	DeletedMerchantService deletedMerchantService;
  
  @Autowired
  AccountRepository accountRepository;
  @Autowired
  GlobalRiskManagementRepository globalRepository;
  @Autowired
  DetailRiskManagmentRepository detailRepository;
  @Autowired
  CustomerRepository customerRepository;
  @Autowired
  MerchantRepository merchantRepository;
 
  @Autowired
  DetailRiskManagmentRepository detailrepository;
  @Autowired
  private ProductPrepRepository prepRepository;
  @Autowired
  CardRepository cardRepository;
  @Autowired
  PeriodicityTypeRepository periodicityRepository;
  @Autowired
  AccountStatusRepository accountStatusRepository;
  @Autowired
  EmvServiceValuesRepository emvServiceValuesRepository;


  @Autowired
  ProgramReposiroty programRepos;
  @Autowired
  CardHistoryRepository cardHistoryRepository;
  @Autowired
  private AgenceAdministrationRepository agenceAdministrationRepository;
  @Autowired
  private ZoneRepository zoneRepository;
  @Autowired
  private RegionRepository regionRepository;
  @Autowired
  GetTokenAmplitudeService getTokenAmplitudeService;
  @Autowired
  AccountService accountService;
  private static final Gson gson = new Gson();
  private static final Logger logger = LoggerFactory.getLogger(AccountController.class);

  @GetMapping("getAccountDetails/{rib}")
  public AccountDetails getAccountDetails(@PathVariable(value = "rib" ) String rib) {
	  
	 
		  try {
			
//			  AccountDetails test=new AccountDetails();
//			  test.setStatutCompte("");
//			  test.setStatutClient("");
//			  test.setTypeIdentite("CNI");
//			  test.setDevise("208");
//			  test.setTel("0558851489");
//			  test.setAgence("01601");
//			  test.setZipCode("16606");
//			  test.setPays("ALGERIE");
//			  test.setCite("EL BIAR");
//			  test.setPrenom("AYMEN");
//			  test.setNom("DAMMAK");
//			  test.setAddresse("20 RUE JEAN JAURES");
////			  
//			  test.setNomCompte("CPTES CHEQUES DES PARTICULIERS");
//			  test.setIdentite("0000000140");
//			  test.setTypeIdentite("CNI");
//			  test.setNjf("");
//			  test.setDns("20/04/1968");
//			  test.setViln("ALGER");
//			  test.setEmail("test@gmail.com");
//			  test.setRadical("59999778");
//			 
//			  
//			  AccountDetails test2=new AccountDetails();
//			  test2.setStatutCompte("");
//			  test2.setStatutClient("");
//			  test2.setTypeIdentite("CNI");
//			  test2.setDevise("208");
//			  test2.setTel("0558851489");
//			  test2.setAgence("01601");
//			  test2.setZipCode("16000");
//			  test2.setPays("ALGERIE");
//			  test2.setCite("ALGER");
//			  test2.setPrenom("Seif eddine");
//			  test2.setNom("Salah");
//			  test2.setAddresse("CITE 200 LOGEMENTS BT G CAGE 2 N° 06 EUCALYPTUS");
//			  
//	  
//			  test2.setNomCompte("CPTES CHEQUES DES PARTICULIERS");
//			  test2.setIdentite("0000000141");
//			  test2.setTypeIdentite("CNI");
//			  test2.setNjf("JEUNF0000000142");
//			  test2.setDns("20/04/1968");
//			  test2.setViln("ALGER");
//			  test2.setEmail("");
//			  test2.setRadical("59999799");

//			  
//			  
//			  AccountDetails test3=new AccountDetails();
//			  test3.setStatutCompte("");
//			  test3.setStatutClient("");
//			  test3.setTypeIdentite("CNI");
//			  test3.setDevise("208");
//			  test3.setTel("0558851489");
//			  test3.setAgence("01601");
//			  test3.setZipCode("16208");
//			  test3.setPays("ALGERIE");
//			  test3.setCite("SIDI M'HAMED");
//			  test3.setPrenom("Maher");
//			  test3.setNom("Elmanoubi");
//			  test3.setAddresse("2 RUE VIGNARD B DES MARTYS CITE DES FONCTIONNAIRE BATIMENT A");
//			  
//			  test3.setNomCompte("CPTES CHEQUES DES PARTICULIERS");
//			  test3.setIdentite("0000000142");
//			  test3.setTypeIdentite("CNI");
//			  test3.setNjf("JEUNF0000000142");
//			  test3.setDns("20/04/1968");
//			  test3.setViln("ALGER");
//			  test3.setEmail("");
//			  
//			  
//			  
//			  AccountDetails test4=new AccountDetails();
//			  test4.setStatutCompte("");
//			  test4.setStatutClient("");
//			  test4.setTypeIdentite("CNI");
//			  test4.setDevise("976");
//			  test4.setTel("0558851489");
//			  test4.setAgence("01601");
//			  test4.setZipCode("16208");
//			  test4.setPays("ALGERIE");
//			  test4.setCite("SIDI M'HAMED");
//			  test4.setPrenom("SEIF");
//			  test4.setNom("SALAH");
//			  test4.setAddresse("2 RUE VIGNARD B DES MARTYS CITE DES FONCTIONNAIRE BATIMENT A");
//			  
//			  test4.setNomCompte("CPTES CHEQUES DES PARTICULIERS");
//			  test4.setIdentite("0000000145");
//			  test4.setTypeIdentite("CNI");
//			  test4.setNjf("JEUNF0000000142");
//			  test4.setDns("20/04/1968");
//			  test4.setViln("ALGER");
//			  test4.setEmail("");
//			  test4.setCompteAttacheDZD("03501601220100003085");
//			  test4.setDeviseCompteAttache("208");
//			  test4.setRadical("89656933");
			 // if (rib.equals("03501601220100003085"))
			//  if(rib.equals("03501601220200001144"))
			 // return test;
//			  if (rib.equals("03501601220100003085"))
//				  return test2;
//			  if (rib.equals("03501601220110000971"))
//				  return test3;
//			  
//			  if (rib.equals("03501601220400000874"))
//				  return test4;
			 return accountService.getAccountDetails(rib, getTokenAmplitudeService.getToken().getToken());
			 
		} catch (Exception e) {
			
		
			logger.info(Throwables.getStackTraceAsString ( e ));
		}
		return null; 
  }

  @PostMapping("ValidationCard")
  public ResponseEntity<String> GenerateCard(@RequestBody GenerateCardPrep validationRequest) {

	ProductPrepayer productPrep=prepRepository.findById(validationRequest.getProduct()).get();

	Program prog=programRepos.findById(productPrep.getProgram()).get();
	System.out.println("codeprog"+prog.getProgramCode());
	Set<Range> ranges = new HashSet<>();
    BinOnUs bin=new BinOnUs();
    String numberCard=new String();

	if(binOnUsRepository.findById(prog.getBinOnUsCode()).isPresent()) {
		  bin=binOnUsRepository.findById(prog.getBinOnUsCode()).get();
		  ranges=bin.getRanges();
		  String LowBin=bin.getBouLowBin();
		  int CarteLength=bin.getBinLength();
		  for(Range rang:ranges) {
				if(rang.getRangeStatusCode()==1) {

				  numberCard=generateCard.completed_number(LowBin, CarteLength,rang.getBouLowRange(),rang.getBouHighRange());
				  System.out.println(numberCard);
				  while(cardRepository.findByCardNum(numberCard).isPresent()) {

					  numberCard=generateCard.completed_number(LowBin, CarteLength,rang.getBouLowRange(),rang.getBouHighRange());

		    	}

				  break;
					 }


			}
  	    Card card=new Card();
    	card.setCardNum(numberCard);
    	card.setFirtPositionCode(prog.getFirstPosition());
    	card.setSecondPositionCode(prog.getSecondPosition());
    	card.setThirdPositionCode(prog.getThirdPosition());
	    card.setAccCode(validationRequest.getAccount());
    	card.setProductCode(productPrep.getProductCode());
    	int years=prog.getCprLifeCycle();
    	Date date = new Date();
    	Calendar c = Calendar.getInstance();
    	c.setTime(date);
    	c.add(Calendar.YEAR, years);
    	Date currentDatePlusYears = c.getTime();
    	card.setExpiryDate(currentDatePlusYears);
    	//card.setCardStatusCode(4);
    	card.setCurrencyCode(788);
    	if(prog.isCprRenewl()==false) {
    		card.setPreDate(null);
    	}
    	if(prog.isCprRenewl()==true)
    	{
    		Calendar calRenewl = Calendar.getInstance();
    		calRenewl.setTime(card.getExpiryDate());
    		calRenewl.add(Calendar.MONTH, -1);

    		card.setPreDate(calRenewl.getTime());
    	}
    	card.setStartDate(new Date());

    	//GlobalRisq
    	System.out.println("prog code"+prog.getProgramCode());
    	GlobalRiskManagement globaManagement=new GlobalRiskManagement();
    	int typePeriodicity= prog.getRiskPeriodicity();
    	  globaManagement.setPeriodicityType(typePeriodicity);
		  globaManagement.setGlCurrencyAmount(0);
		  globaManagement.setGlCurrencyNumber("0");
		  globaManagement.setMaxAmount(prog.getCprRiskAmountMax());
		  globaManagement.setMinAmount(prog.getCprRiskAmountMin());
		  globaManagement.setGolbalAmount(String.valueOf(prog.getCprRiskAmount()));
		  globaManagement.setGlStartDate(prog.getStartDate());
		  Calendar cal = Calendar.getInstance();
		  Date endDate=new Date();
		  switch(typePeriodicity) {

		  case 24:

	    	c.setTime(globaManagement.getGlStartDate());
	    	c.add(Calendar.MONTH, 1);
		    endDate= c.getTime();
	    	break;
		  case 22:
		      c.setTime(globaManagement.getGlStartDate());
		      c.add(Calendar.YEAR, 1);
		      endDate= c.getTime();
		    break;

		  case 21:
		      c.setTime(globaManagement.getGlStartDate());
		      c.add(Calendar.DAY_OF_WEEK, 7);
		      endDate=c.getTime() ;
		      System.out.println(endDate);
		    break;
		  case 23:
		      c.setTime(globaManagement.getGlStartDate());
		      c.add(Calendar.DAY_OF_WEEK, 1);
		      endDate=c.getTime();
		    break;

		  }
          globaManagement.setGlEndDate(endDate);
          globaManagement.setGlobalNumber(prog.glNumber);
		  GlobalRiskManagement globalSaved=globalRepository.save(globaManagement);
		  card.setGlobalRiskCode(String.valueOf(globalSaved.getGlobalRiskManagCode()));
		  cardRepository.save(card);
		 /* List<RiskPeriodicity> Listrisq=riskPeriodicityRepository.findByProgCode(prog.getProgramCode());




			 for(RiskPeriodicity risq:Listrisq) {
				 Set<EmvServiceValues> Listemv=risq.getEmvServiceValues();
				 for(EmvServiceValues trans:Listemv)
				 {


					 DetailRiskManagment detailRisque=new DetailRiskManagment();

					  detailRisque.setGlobalCode(globalSaved.getGlobalRiskManagCode());
					  detailRisque.transactionCode=trans.getCodeTransaction();
					  Date start=risq.riskPeriodicityStartDate;
					  detailRisque.setDrStartDate(risq.riskPeriodicityStartDate);

					  Date end=new Date();
					  int typePer=risq.getTypeCode();
					  Calendar cal2 = Calendar.getInstance();
					  cal2.setTime(start);
					  switch(typePer) {
					  case 23:
						  cal2.add(Calendar.DAY_OF_WEEK, 1);
						  end=cal2.getTime();

						  break;
					  case 21:
						  cal2.add(Calendar.DAY_OF_WEEK, 7);
						  end=cal2.getTime();
						  break;
					  case 24:

						  cal2.setTime(start);
						  cal2.add(Calendar.MONTH, 1);
						  end=cal2.getTime();
					  }

					  detailRisque.setDrEndDate(end);
					  detailRisque.setDrCurrencyAmount(globalSaved.getGlCurrencyAmount());
					  detailRisque.setDrCurrencyNumber(Long.parseLong(globalSaved.getGlCurrencyNumber()));
					  detailRisque.setMinAmount(Long.parseLong(risq.amountMinPeriod));
					  detailRisque.setMaxAmount(Long.parseLong(risq.amountMaxPeriod));
					  detailRisque.setDrNumberLimit(Integer.parseInt(risq.numberLimitPeriod));
					  detailRisque.setDrAmountLimit(risq.amountLimitPeriod);
					  detailRisque.typePeriodicity=risq.getTypeCode();

					  detailrepository.save(detailRisque);
				 }

				 }*/


    }


	return ResponseEntity.ok().body("card created");



  }



  @PostMapping("addaccount")
  public ResponseEntity<Account> addAccount(@RequestBody Account account) {
    accountRepository.save(account);
    return ResponseEntity.ok().body(account);
  }
  @GetMapping("/getUserConnect")
  public ResponseEntity<User> getUserConnect(HttpServletRequest request) {

    String name = SecurityContextHolder.getContext().getAuthentication().getName();
    System.out.println(" nammmmmeee " + name);
    //  String id = userPrincipal.getId();
    if (!userRepository.findByUserNameOrUserEmail(name, name).isPresent()) {
      return ResponseEntity.ok(new User());

    }
    User user = userRepository.findByUserNameOrUserEmail(name, name).get();
    return ResponseEntity.ok(user);
  }
    @GetMapping("accounts")
    public List<AccountDisplay> getAllAtm() {
        String name = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUserNameOrUserEmail(name, name).get();
        List<Account> accounts=new ArrayList<Account>();
        if(user.getUserType()==1) {
            accounts=accountRepository.findAllByAgence(user.getIdAgence());
        }
        if(user.getUserType()==3) {
            accounts=accountRepository.findAllByZone(user.getIdAgence());
        }
        if(user.getUserType()==4) {
            accounts=accountRepository.findAllByRegion(user.getIdAgence());
        }
        if(user.getUserType()==5) {
            accounts=accountRepository.findAll();
        }
        //List<Account> accounts=accountRepository.findAll();
        List<AccountDisplay> customerDisplays = new ArrayList<>();
        for (Account c : accounts
        ) {
            AccountStatus accountStatus = accountStatusRepository.findByAstCode(String.valueOf(c.getAstCode()));
            AccountDisplay accountDisplay = new AccountDisplay(c.getAccountCode(),c.getAccountNum(),
                    c.getAccountNumAttached(), c.getAccountAuthorize(), c.getAccountAvailable(),
                    c.getAccountBilling(),c.getAccountRevolvingLimit(),c.getAccountName(),c.getAstCode(),c.getCurrency(),accountStatus.getAstLibelle(),c.getIdAgence(),
                    c.getAccountExceeding(),c.getAccountBalance(),c.getPreauthAmount(),c.getRefundAmount());
            customerDisplays.add(accountDisplay);
        }
        return  customerDisplays;
    }

    @PostMapping("accountsFilter")
	public Page<AccountDisplay> getAllAtmFilter(@RequestBody AccountDisplayFilter accountDisplayFilter,
			@RequestParam(name = "page", defaultValue = "0") int page,
			@RequestParam(name = "size", defaultValue = "10") int size, @RequestParam(name = "sortOn") String sortOn,
			@RequestParam(name = "dir") String dir) {
		String name = SecurityContextHolder.getContext().getAuthentication().getName();
		String numAccount = "";
		int status = 0;
		Order order=null;
	  	if (dir.equals("desc"))
	  		order = new Order(Sort.Direction.DESC, sortOn);
	  	else 
	  		order = new Order(Sort.Direction.ASC, sortOn);

        if(accountDisplayFilter.getAccountNumber() !=null){
            numAccount = accountDisplayFilter.getAccountNumber().trim();
        }
        if(accountDisplayFilter.getStatus() !=0){
            status = accountDisplayFilter.getStatus();
        }
        User user = userRepository.findByUserNameOrUserEmail(name, name).get();
        Page<Account> accounts= null;
        Page<AccountDisplay> page2 = null;
//        if(user.getUserType()==1) {
//        	 Optional<AgenceAdministration> agence=  agenceAdministrationRepository.findByCodeAgence(user.getIdAgence());
//             if (agence.isPresent()) {
//          	   agencyCode.add(agence.get().getInitial());
//
//             }
//        	
//        	
//            if(accountDisplayFilter.getStatus() ==0){
//                accounts=accountRepository.findAllByAgenceAndWithoutStatusAndNum(PageRequest.of(page, size, Sort.by(order)),user.getIdAgence(),numAccount);
//            } else{
//                accounts=accountRepository.findAllByAgenceAndStatusAndNum(PageRequest.of(page, size, Sort.by(order)),user.getIdAgence(),status,numAccount);
//            }
//        }
//        if(user.getUserType()==3) {
//            if(accountDisplayFilter.getStatus() ==0){
//                accounts=accountRepository.findAllByZoneAndWithoutStatusAndNum(PageRequest.of(page, size, Sort.by(order)),user.getIdAgence(),numAccount);
//            } else{
//                accounts=accountRepository.findAllByZoneAndStatusAndNum(PageRequest.of(page, size, Sort.by(order)),user.getIdAgence(),status,numAccount);
//            }
//        }
//        if(user.getUserType()==4) {
//            if(accountDisplayFilter.getStatus() ==0){
//                accounts=accountRepository.findAllByRegionAndWithoutStatusAndNum(PageRequest.of(page, size, Sort.by(order)),user.getIdAgence(),numAccount);
//            } else{
//                accounts=accountRepository.findAllByRegionAndStatusAndNum(PageRequest.of(page, size, Sort.by(order)),user.getIdAgence(),status,numAccount);
//            }
//
//        }
        if(user.getUserType()==5) {
            if(accountDisplayFilter.getStatus() ==0){
                accounts=accountRepository.findAllByRegionwithoutstatus(PageRequest.of(page, size, Sort.by(order)),numAccount);
            } else{
                accounts=accountRepository.findAllByRegionwithstatus(PageRequest.of(page, size, Sort.by(order)),status,numAccount);
            }
        }
        else {
        	List<String> agencyCode=new ArrayList<String>();

        	  if(user.getUserType()==1) {
             	 Optional<AgenceAdministration> agence=  agenceAdministrationRepository.findByCodeAgence(user.getIdAgence());
                  if (agence.isPresent()) 
               	   agencyCode.add(agence.get().getInitial());

             }
        	  else if (user.getUserType()==3) {
       		   
       		   Optional<Zone> zone=zoneRepository.findByCodeZone(user.getIdAgence());
       		   if (zone.isPresent()) {
       			   
                      List<AgenceAdministration> agencies=  agenceAdministrationRepository.findByCodeZone(zone.get().getCodeZone());
                      for (AgenceAdministration agency : agencies) {
                   	   agencyCode.add(agency.getInitial());
                      }
  
       		   }
       		   //accounts=accountRepository.findAllByZone(user.getIdAgence()); 
       	   }
       	   else if (user.getUserType()==4) {
       		   Optional<Region> region=regionRepository.findByCodeRegion(user.getIdAgence());
       		   if (region.isPresent()) {
       			   List<Zone> zones=zoneRepository.findByCodeRegion(region.get().getCodeRegion());
                      for (Zone zone : zones) {
                          List<AgenceAdministration> agencies=  agenceAdministrationRepository.findByCodeZone(zone.getCodeZone());
                          for (AgenceAdministration agency : agencies) {
                       	   agencyCode.add(agency.getInitial());
                          }
                      }
       		   }	   
       	   }
        	  
        	  
        	  if(accountDisplayFilter.getStatus() ==0){
                  accounts=accountRepository.findAllByAgenceAndWithoutStatusAndNum(PageRequest.of(page, size, Sort.by(order)),agencyCode,numAccount);
              } else{
                  accounts=accountRepository.findAllByAgenceAndStatusAndNum(PageRequest.of(page, size, Sort.by(order)),agencyCode,status,numAccount);
              }
        	
        	
        }
        
        List<AccountDisplay> customerDisplays = new ArrayList<>();
        for (Account c : accounts
        ) {
            AccountStatus accountStatus = accountStatusRepository.findByAstCode(String.valueOf(c.getAstCode()));
            AccountDisplay accountDisplay = new AccountDisplay(c.getAccountCode(),c.getAccountNum(),
                    c.getAccountNumAttached(), c.getAccountAuthorize(), c.getAccountAvailable(),
                    c.getAccountBilling(),c.getAccountRevolvingLimit(),c.getAccountName(),c.getAstCode(),c.getCurrency(),
                    accountStatus.getAstLibelle(),c.getIdAgence(),c.getAccountExceeding(),c.getAccountBalance(),c.getPreauthAmount(),c.getRefundAmount());
            customerDisplays.add(accountDisplay);
            page2 = new PageImpl<>(customerDisplays,PageRequest.of(page, size, Sort.by(order)), accounts.getTotalElements());

        }
        return  page2;
    }

  @GetMapping("accountsByLib")
  public List<Account> getAllAccount() {
    List<Account> ListAccount = new ArrayList<Account>();
    if (accountRepository.findByAccountType(2).isPresent()) {
      ListAccount = accountRepository.findByAccountType(2).get();
    }

    return ListAccount;
  }
  
  @PutMapping("editPreauthAmount/{id}")
  public ResponseEntity<String> editPreauthAmount(@PathVariable(value = "id" ) Integer id, @RequestBody EditPreauthAmountRequest editPreauthAmountRequest ){
 
    Optional<Account> account= accountRepository.findByAccountCode(id);
   
    if (account.isPresent()) {
    	account.get().setPreauthAmount(new BigInteger(editPreauthAmountRequest.getAmount()));
    	accountRepository.save(account.get());
    	return new ResponseEntity<>(HttpStatus.OK);
    	
    }else return  new ResponseEntity<>(HttpStatus.NOT_FOUND);

  }
  
  
  @PutMapping("editRefundAmount/{id}")
  public ResponseEntity<String> editRefundAmount(@PathVariable(value = "id" ) Integer id, @RequestBody EditPreauthAmountRequest editPreauthAmountRequest ){
 
    Optional<Account> account= accountRepository.findByAccountCode(id);
   
    if (account.isPresent()) {
    	account.get().setRefundAmount(new BigInteger(editPreauthAmountRequest.getAmount()));
    	accountRepository.save(account.get());
    	return new ResponseEntity<>(HttpStatus.OK);
    	
    }else return  new ResponseEntity<>(HttpStatus.NOT_FOUND);

  }
  
  
  @PutMapping("editExceedingAmount/{id}")
  public ResponseEntity<String> editExceedingAmount(@PathVariable(value = "id" ) Integer id, @RequestBody EditPreauthAmountRequest editPreauthAmountRequest ){
 
    Optional<Account> account= accountRepository.findByAccountCode(id);
   
    if (account.isPresent()) {
    	account.get().setAccountExceeding(new BigInteger(editPreauthAmountRequest.getAmount()));
    	accountRepository.save(account.get());
    	return new ResponseEntity<>(HttpStatus.OK);
    	
    }else return  new ResponseEntity<>(HttpStatus.NOT_FOUND);

  }


  @GetMapping("/getAccount/{idAccount}")
  public ResponseEntity<Account> getEmployeeById(
      @PathVariable(value = "idAccount") String idAccount)
      throws ResourceNotFoundException {
    Account account = accountRepository.findByAccountCode(Integer.parseInt(idAccount))
        .orElseThrow(() ->
            new ResourceNotFoundException("Account not found for this id :: " + idAccount));

    return ResponseEntity.ok().body(account);
  }

  @GetMapping("/SaveAccount/{accountNumber}")
  public ResponseEntity<Account> addAccountPrep(
      @PathVariable(value = "accountNumber") String AccountNum) {
    int count = accountRepository.findPrep(AccountNum, 2);
    Account addAccount = new Account();
    if (count > 0) {
      List<Account> ListAccount = accountRepository.findAccount(AccountNum).get();
      Account acc = ListAccount.get(0);
      addAccount.setAccountAuthorize(acc.getAccountAuthorize());
      addAccount.setAccountAvailable(acc.getAccountAvailable());
      addAccount.setAccountBilling(acc.getAccountBilling());
      addAccount.setAccountName(acc.getAccountName());
      addAccount.setAccountNum(acc.getAccountNum());
      addAccount.setAccountNumAttached(acc.getAccountNumAttached());
      addAccount.setAccountRevolvingLimit(acc.getAccountRevolvingLimit());
      addAccount.setAccountType(acc.getAccountType());
      addAccount.setCurrency(acc.getCurrency());
      addAccount.setCustomerCode(acc.getCustomerCode());
      addAccount.setGlobalRiskMangCode(acc.getGlobalRiskMangCode());
      addAccount.setAccountNumAttached(AccountNum + String.valueOf(count + 1));
      addAccount.setCreationDate(new Date());
      
      accountRepository.save(addAccount);
    } else {
      Account acc = accountRepository.findByNumLibStatus(AccountNum);

      addAccount.setAccountAuthorize(acc.getAccountAuthorize());
      addAccount.setAccountAvailable(acc.getAccountAvailable());
      addAccount.setAccountBilling(acc.getAccountBilling());
      addAccount.setAccountName(acc.getAccountName());
      addAccount.setAccountNum(acc.getAccountNum());
      addAccount.setAccountRevolvingLimit(acc.getAccountRevolvingLimit());
      addAccount.setCurrency(acc.getCurrency());
      addAccount.setCustomerCode(acc.getCustomerCode());
      addAccount.setGlobalRiskMangCode(acc.getGlobalRiskMangCode());
      addAccount.setAccountNumAttached(AccountNum + String.valueOf(count + 1));      
      addAccount.setAccountNumAttached(AccountNum + "01");
      addAccount.setAccountType(3);
      addAccount.setCreationDate(new Date());
      accountRepository.save(addAccount);
    }


    return ResponseEntity.ok().body(addAccount);
  }

  @GetMapping("/getAccountPrepayer/{accountNumber}")
  public List<Account> getAcountPrepayer(@PathVariable(value = "accountNumber") String AccountNum) {
    System.out.println("account" + AccountNum);
    if (accountRepository.findAccount(AccountNum).isPresent()) {
      return accountRepository.findAccount(AccountNum).get();
    } else {
      return new ArrayList<>();
    }
  }

    @GetMapping("/getAllStatus")
    public List<AccountStatus> getStatusAll(){
     return  accountStatusRepository.findAll();
    }



  @PutMapping("activate/{id}")
  public ResponseEntity<Account> activate(@PathVariable(value = "id") String userId) {
    Account employee = accountRepository.findByAccountCode(Integer.parseInt(userId)).get();
    employee.setAstCode(1);

    accountRepository.save(employee);
    return ResponseEntity.ok(employee);
  }

  @PutMapping("blocked/{id}")
  public ResponseEntity<Account> bloqued(@PathVariable(value = "id") String userId) {
    Account employee = accountRepository.findByAccountCode(Integer.parseInt(userId)).get();
    employee.setAstCode(2);

    accountRepository.save(employee);
    return ResponseEntity.ok(employee);
  }

  @PutMapping("closed/{id}")
  public ResponseEntity<Account> closed(@PathVariable(value = "id") String id) {
    Account account = accountRepository.findByAccountCode(Integer.parseInt(id)).get();
    
	AgenceAdministration agence = agenceAdministrationRepository.findoneBycode(account.getIdAgence());


	Merchant m = merchantRepository.findByNumAccount(account.getAccountCode());
	if(m!=null) {
		deletedMerchantService.ResiliationMerchant("0"+account.getAccountNum());
	}
    account.setAstCode(9);
    String name = SecurityContextHolder.getContext().getAuthentication().getName();
	Optional<User> user = userRepository.findByUserNameOrUserEmail(name, name);
	if (!user.isPresent()) {
		throw new RuntimeException("Error saving status !");
	}
  
    
    
    List <Card> cards=cardRepository.findByAccCode(account.getAccountCode());
    for (Card card:cards ) {
    	card.setCardStatusCode(4);
    	cardRepository.save(card);
    	
    	CardHistory cardHistory = new CardHistory();
		cardHistory.setCardCode(card.getCardCode());
		cardHistory.setOperation("Changer le statut de la carte en Bloquée");
		cardHistory.setOperation_date(card.getModifDate());

		cardHistory.setEditedBy(user.get().getUserName());
		cardHistoryRepository.save(cardHistory);
    }

    accountRepository.save(account);
    return ResponseEntity.ok(account);
  }

  @PostMapping("riskManagement/{id}")
  public ResponseEntity<?> riskManagement(@RequestBody RiskManagementRequest rm,
      @PathVariable(value = "id") String userId) {

    Account account = accountRepository.findByAccountCode(Integer.parseInt(userId)).get();
//    if (Strings.isNullOrEmpty(account.getGlobalRiskMangCode())) {
    if (String.valueOf(account.getGlobalRiskMangCode()).equals("null") || account.getGlobalRiskMangCode()==null ) {
      Periodicity periodicity= periodicityRepository.findByLibelle(rm.getGlPeriodicityType());
      GlobalRiskManagement globalRiskManagement = new GlobalRiskManagement(rm.getGlMinAmount(),
          rm.getGlMaxAmount(), rm.getGlobalAmount(), rm.getGlobalNumber(), rm.getGlStartDate(),
          rm.getGlEndDate(), periodicity.getCode());
      GlobalRiskManagement globalRiskManagement1 = globalRepository.save(globalRiskManagement);

      if (rm.getDetailsRequestList()!=null)  {
        List<DetailsRequest> detailsRequests = rm.getDetailsRequestList();
        for (DetailsRequest dr : detailsRequests
        ) {
          List<String> strings = dr.getTransactionCode();
          for (String s : strings
          ) {
            EmvServiceValues serviceValues = emvServiceValuesRepository.findByLibelle(s);
            Periodicity periodicity1 = periodicityRepository
                .findByLibelle(dr.getDrTypePeriodicity());

            DetailRiskManagment detailRiskManagment = new DetailRiskManagment(dr.getDrMinAmount(),
                serviceValues.getCodeTransaction(), globalRiskManagement1.getGlobalRiskManagCode(),
                dr.getDrMaxAmount(),
                dr.getDrAmountLimit(), dr.getDrNumberLimit(), dr.getDrStartDate(),
                dr.getDrEndDate(),
                dr.getDrCurrencyAmount(), dr.getDrCurrencyNumber(), periodicity1.getCode());
            detailRepository.save(detailRiskManagment);
          }
        }
      }
      account.setGlobalRiskMangCode(String.valueOf(globalRiskManagement1.getGlobalRiskManagCode()));
      accountRepository.save(account);
      return ResponseEntity.ok().body(gson.toJson("RiskManagement added successfully!"));

    }
    GlobalRiskManagement globalRiskManagement = globalRepository
        .findById(Integer.parseInt(account.getGlobalRiskMangCode())).get();
    Periodicity periodicity = periodicityRepository
        .findByLibelle(rm.getGlPeriodicityType());
    globalRiskManagement.setGlEndDate(rm.getGlEndDate());
    globalRiskManagement.setGlCurrencyAmount(rm.getGlCurrencyAmount());
    globalRiskManagement.setGlCurrencyNumber(rm.getGlCurrencyNumber());
    globalRiskManagement.setGlStartDate(rm.getGlStartDate());
    globalRiskManagement.setGolbalAmount(rm.getGlobalAmount());
    globalRiskManagement.setMaxAmount(rm.getGlMaxAmount());
    globalRiskManagement.setMinAmount(rm.getGlMinAmount());
    globalRiskManagement.setGlobalNumber(rm.getGlobalNumber());
    globalRiskManagement.setPeriodicityType(periodicity.getCode());
    globalRepository.save(globalRiskManagement);

    if (detailRepository.findByGlobalCode(Integer.parseInt(account.getGlobalRiskMangCode()))
        .isEmpty()) {
      List<DetailsRequest> detailsRequests = rm.getDetailsRequestList();

      for (DetailsRequest dr : detailsRequests
      ) {
        List<String> strings = dr.getTransactionCode();
        for (String s : strings
        ) {
          EmvServiceValues serviceValues = emvServiceValuesRepository.findByLibelle(s);

          Periodicity periodicity1 = periodicityRepository
              .findByLibelle(dr.getDrTypePeriodicity());

          DetailRiskManagment detailRiskManagment = new DetailRiskManagment(dr.getDrMinAmount(),
              serviceValues.getCodeTransaction(), Integer.parseInt(account.getGlobalRiskMangCode()),
              dr.getDrMaxAmount(),
              dr.getDrAmountLimit(), dr.getDrNumberLimit(), dr.getDrStartDate(),
              dr.getDrEndDate(),
              dr.getDrCurrencyAmount(), dr.getDrCurrencyNumber(), periodicity1.getCode());
          detailRepository.save(detailRiskManagment);
        }

      }
      return ResponseEntity.ok().body(gson.toJson("RiskManagement updated successfully!"));




      }
      List<DetailRiskManagment> detailRiskManagments = detailRepository
          .findByGlobalCode(Integer.parseInt(account.getGlobalRiskMangCode()));
      for (DetailRiskManagment dr : detailRiskManagments
      ) {
        detailRepository.delete(dr);
      }
      List<DetailsRequest> detailsRequests = rm.getDetailsRequestList();

      for (DetailsRequest dr : detailsRequests
      ) {
        List<String> strings = dr.getTransactionCode();
        for (String s : strings
        ) {
          EmvServiceValues serviceValues = emvServiceValuesRepository.findByLibelle(s);

          Periodicity periodicity1 = periodicityRepository
              .findByLibelle(dr.getDrTypePeriodicity());

          DetailRiskManagment detailRiskManagment = new DetailRiskManagment(dr.getDrMinAmount(),
              serviceValues.getCodeTransaction(), Integer.parseInt(account.getGlobalRiskMangCode()),
              dr.getDrMaxAmount(),
              dr.getDrAmountLimit(), dr.getDrNumberLimit(), dr.getDrStartDate(),
              dr.getDrEndDate(),
              dr.getDrCurrencyAmount(), dr.getDrCurrencyNumber(), periodicity1.getCode());
          detailRepository.save(detailRiskManagment);
        }
      }

    return ResponseEntity.ok().body(gson.toJson("RiskManagement updated successfully!"));

  }


  @DeleteMapping("deleteRiskManagement/{id}")
  public ResponseEntity<String> deleteRiskManagement(@PathVariable(value = "id") Integer type) {
    Account account = accountRepository.findByAccountCode(type).get();
    GlobalRiskManagement globalRiskManagement = globalRepository
        .findById(Integer.parseInt(account.getGlobalRiskMangCode())).get();
    List<DetailRiskManagment> detailRiskManagments = detailRepository
        .findByGlobalCode(Integer.parseInt(account.getGlobalRiskMangCode()));
    for (DetailRiskManagment dr : detailRiskManagments
    ) {
      detailRepository.delete(dr);
    }
    account.setGlobalRiskMangCode(null);
    accountRepository.save(account);
    globalRepository.delete(globalRiskManagement);
    return ResponseEntity.ok().body(gson.toJson("GlobalRiskManagement deleted  successfully!"));

  }

  @DeleteMapping("delete=Details/{id}")
  public ResponseEntity<String> deleteDetails(@PathVariable(value = "id") Integer type) {
    DetailRiskManagment detailRiskManagment = detailRepository.findById(type).get();
    detailRepository.delete(detailRiskManagment);
    return ResponseEntity.ok().body(gson.toJson("DetailRiskManagement deleted  successfully!"));

  }


	@GetMapping("/getRiskManagement/{idAccount}")
	public ResponseEntity<RiskManegementDisplay> getRiskManagement(@PathVariable(value = "idAccount") String idAccount)
			throws ResourceNotFoundException {
		Account account = accountRepository.findByAccountCode(Integer.parseInt(idAccount))
				.orElseThrow(() -> new ResourceNotFoundException("Account not found for this id :: " + idAccount));
		if (Strings.isNullOrEmpty(account.getGlobalRiskMangCode())) {
			return ResponseEntity.ok().body(new RiskManegementDisplay());
		}
		if (account.getGlobalRiskMangCode() != null) {
			GlobalRiskManagement globalRiskManagement = globalRepository
					.findById(Integer.parseInt(account.getGlobalRiskMangCode())).get();
			Periodicity periodicity = periodicityRepository.findById(globalRiskManagement.getPeriodicityType()).get();

			RiskManegementDisplay riskManagementRequest = new RiskManegementDisplay(
					globalRiskManagement.getGlobalRiskManagCode(), globalRiskManagement.getMinAmount(),
					globalRiskManagement.getMaxAmount(), globalRiskManagement.getGolbalAmount(),
					globalRiskManagement.getGlobalNumber(), globalRiskManagement.getGlCurrencyAmount(),
					globalRiskManagement.getGlCurrencyNumber(), globalRiskManagement.getGlStartDate(),
					globalRiskManagement.getGlEndDate(), periodicity.getLibelle());
			List<DisplayDetailsManagement> detailsRequestList = new ArrayList<>();
			if (!detailRepository.findByGlobalCode(Integer.parseInt(account.getGlobalRiskMangCode())).isEmpty()) {
				List<DetailRiskManagment> detailRiskManagments = detailRepository
						.findByGlobalCode(Integer.parseInt(account.getGlobalRiskMangCode()));
				for (DetailRiskManagment dr : detailRiskManagments) {
					Periodicity periodicity1 = periodicityRepository.findById(dr.getTypePeriodicity()).get();

					DisplayDetailsManagement detailsRequest = new DisplayDetailsManagement(dr.getMinAmount(),
							dr.getTransactionCode(), dr.getMaxAmount(), dr.getDrAmountLimit(), dr.getDrNumberLimit(),
							dr.getDrStartDate(), dr.getDrEndDate(), dr.getDrCurrencyAmount(), dr.getDrCurrencyNumber(),
							periodicity1.getLibelle());

					detailsRequestList.add(detailsRequest);
				}
				riskManagementRequest.setDetailsRequestList(detailsRequestList);

			}
			return ResponseEntity.ok().body(riskManagementRequest);
		} else
			return ResponseEntity.ok().body(new RiskManegementDisplay());

	}

  @GetMapping("/getCustomer/{idAccount}")
  public ResponseEntity<Customer> getCustomer(
      @PathVariable(value = "idAccount") String idAccount)
      throws ResourceNotFoundException {

    Account account = accountRepository.findByAccountCode(Integer.parseInt(idAccount))
        .orElseThrow(() ->
            new ResourceNotFoundException("Account not found for this id :: " + idAccount));
    if(!customerRepository
        .findByCustomerCode(Integer.parseInt(account.getCustomerCode())).isPresent())
    {
      return ResponseEntity.ok().body(null);

    }
    Customer customer = customerRepository
        .findByCustomerCode(Integer.parseInt(account.getCustomerCode())).get();
    return ResponseEntity.ok().body(customer);
  }



  @GetMapping("/getListCard/{idAccount}")
  public List<Card> getListCard(
      @PathVariable(value = "idAccount") String idAccount)
      throws ResourceNotFoundException {
    Account account = accountRepository.findByAccountCode(Integer.parseInt(idAccount))
        .orElseThrow(() ->
            new ResourceNotFoundException("Account not found for this id :: " + idAccount));
   
    List<Card> cards = cardRepository.findByAccCode(account.getAccountCode());

    return cards;
  }

  @GetMapping("/getAccountByAccNum/{accNum}")
  public Account getAccountByAccNum(@PathVariable(value = "accNum") String accNum){
      try {
          return accountRepository.findByAccountNum(accNum);
      } catch (Exception e) {
          e.printStackTrace();
      }
      return accountRepository.findByAccountNum(accNum);

  }
}
