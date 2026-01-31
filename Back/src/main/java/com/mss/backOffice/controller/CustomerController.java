package com.mss.backOffice.controller;

import com.google.common.base.Strings;
import com.google.gson.Gson;

import com.mss.unified.entities.Account;
import com.mss.unified.entities.AccountStatus;
import com.mss.unified.entities.AgenceAdministration;
import com.mss.unified.entities.Card;
import com.mss.unified.entities.Customer;
import com.mss.unified.entities.CustomerStatus;
import com.mss.unified.entities.DetailRiskManagment;
import com.mss.unified.entities.EmvServiceValues;
import com.mss.unified.entities.GlobalRiskManagement;
import com.mss.unified.entities.IdentificationType;
import com.mss.unified.entities.Periodicity;
import com.mss.unified.entities.Region;
import com.mss.unified.entities.User;
import com.mss.unified.entities.Zone;
import com.mss.backOffice.exception.ResourceNotFoundException;
import com.mss.unified.repositories.*;

import com.mss.backOffice.request.AccountDisplay;
import com.mss.backOffice.request.CardDisplay;
import com.mss.backOffice.request.CustomerDisplay;
import com.mss.backOffice.request.CustomersFilter;
import com.mss.backOffice.request.DetailsRequest;
import com.mss.backOffice.request.DisplayDetailsManagement;
import com.mss.backOffice.request.MerchantListDisplay;
import com.mss.backOffice.request.RiskManagementRequest;
import com.mss.backOffice.request.RiskManegementDisplay;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.validation.Valid;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
     
@RestController
@RequestMapping("customers")
public
class CustomerController {
  @Autowired
  UserRepository userRepository;


  @Autowired
  CustomerRepository customerRepository;
  @Autowired
  IdentificationTypeRepository identificationTypeRepository;
  @Autowired
  CustomerStatusRepository customerStatusRepository;
  private ModelMapper modelMapper;
  @Autowired
  AccountRepository accountRepository;
  @Autowired
  AccountStatusRepository accountStatusRepository;
  @Autowired
  GlobalRiskManagementRepository globalRepository;
  @Autowired
  DetailRiskManagmentRepository detailRepository;
  @Autowired
  PeriodicityTypeRepository periodicityRepository;
  @Autowired
  EmvServiceValuesRepository emvServiceValuesRepository;
  private static final Gson gson = new Gson();
  private static final Logger logger = LoggerFactory.getLogger(UserController.class);
  @Autowired
  private AgenceAdministrationRepository agenceAdministrationRepository;
  @Autowired
  private ZoneRepository zoneRepository;
  @Autowired
  private RegionRepository regionRepository;
  @PostMapping("addCustomer")
  public ResponseEntity<String> addCustomer(@Valid @RequestBody Customer customer) {
    logger.info(customer.toString());
    customerRepository.save(customer);

    return ResponseEntity.ok().body("Customer added successfully!");

  }


  @PostMapping("getAllCustomers")
	public Page<CustomerDisplay> getAllCustomers(@Valid @RequestBody CustomersFilter filter,
			@RequestParam(name = "page", defaultValue = "0") int page,
			@RequestParam(name = "size", defaultValue = "10") int size,
		    @RequestParam(name = "sortOn") String sortOn,
		    @RequestParam(name = "dir") String dir
			) {
		String name = SecurityContextHolder.getContext().getAuthentication().getName();
		User user = userRepository.findByUserNameOrUserEmail(name, name).get();
		List<Customer> listCustomer = new ArrayList<Customer>();
		// List<Account> accounts=new ArrayList<Account>();
		// List<String> listCustomerCode=new ArrayList<String>();
		Page<Account> accounts = null;

		Page<CustomerDisplay> page2 = null;
		//Page<Customer> all = null;
		String identity = "";
		//int statusCode = 0;
		Page<Customer> pageCustomers = null;
		List<CustomerDisplay> customerDisplays = new ArrayList<>();

//		if (filter.getStatus() != 0) {
//			statusCode = filter.getStatus();
//		}
		Order order=null;
	  	if (dir.equals("desc"))
	  		order = new Order(Sort.Direction.DESC, sortOn);
	  	else 
	  		order = new Order(Sort.Direction.ASC, sortOn);
	  	
	  	 if(user.getUserType()==5) {
	  		if (filter.getStatus() != 0)
				pageCustomers = customerRepository.findAllWithFilters(PageRequest.of(page, size, Sort.by(order)), filter.getIdentity(),
						filter.getStatus());
			else
				pageCustomers = customerRepository.findAllWithoutStatus(PageRequest.of(page, size, Sort.by(order)),
						filter.getIdentity());

	  	 }else {
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
   	   
	  		
	  	if (filter.getStatus() != 0)
			pageCustomers = customerRepository.findAllWithFilters(PageRequest.of(page, size, Sort.by(order)), filter.getIdentity(),
					filter.getStatus(),agencyCode);
		else
			pageCustomers = customerRepository.findAllWithoutStatus(PageRequest.of(page, size, Sort.by(order)),
					filter.getIdentity(),agencyCode);
	  	  
	  	 }
	  	
	  	
	  	 
	  	for (Customer c : pageCustomers) {
			CustomerStatus customerStatus = customerStatusRepository.findById(c.getCustomerStatusCode()).get();
			CustomerDisplay customerDisplay = new CustomerDisplay(c.getCustomerCode(), c.getCustomerIdentidy(),
					c.getGlobalRiskCode(), c.getCustomerStatusCode(), c.getCustomerIdentidy(), c.getCustomerName(),
					c.getCustomerAddress(), c.getCustomerGSM(), c.getCustomerCity(), c.getCustomerTown(),
					c.getCustomerPostal(), customerStatus.getLibelle());

			customerDisplays.add(customerDisplay);
		}
      page2 = new PageImpl<>(customerDisplays,PageRequest.of(page, size, Sort.by(order)), pageCustomers.getTotalElements());

		return page2;
	}

	@GetMapping("getAllCustomersAgence/{agence}")
  public List<CustomerDisplay> getAllCustomersAgence(@PathVariable(value = "agence") Integer agence) {
    List<Customer> listCustomer = customerRepository.findAll();
    List<CustomerDisplay> customerDisplays = new ArrayList<>();
    for (Customer c : listCustomer
    ) {
      CustomerStatus customerStatus = customerStatusRepository.findById(c.getCustomerStatusCode())
          .get();
      CustomerDisplay customerDisplay = new CustomerDisplay(c.getCustomerCode(),
          c.getCustomerIdentidy(),
              c.getGlobalRiskCode(),
              c.getCustomerStatusCode(),
          c.getCustomerIdentidy(),
              c.getCustomerName(),
              c.getCustomerAddress(),
              c.getCustomerGSM(),
          c.getCustomerCity(), c.getCustomerTown(), c.getCustomerPostal(),
          customerStatus.getLibelle());

      customerDisplays.add(customerDisplay);
    }
    return customerDisplays;
  }
  @GetMapping("getAllCustomersRegion")
  public List<CustomerDisplay> getAllCustomersRegion() {
    List<Customer> listCustomer = customerRepository.findAll();
    List<CustomerDisplay> customerDisplays = new ArrayList<>();
    for (Customer c : listCustomer
    ) {
      CustomerStatus customerStatus = customerStatusRepository.findById(c.getCustomerStatusCode())
          .get();
      CustomerDisplay customerDisplay = new CustomerDisplay(c.getCustomerCode(),
          c.getCustomerIdentidy(),
              c.getGlobalRiskCode(),
              c.getCustomerStatusCode(),
          c.getCustomerIdentidy(),
              c.getCustomerName(),
              c.getCustomerAddress(),
              c.getCustomerGSM(),
          c.getCustomerCity(), c.getCustomerTown(), c.getCustomerPostal(),
          customerStatus.getLibelle());

      customerDisplays.add(customerDisplay);
    }
    return customerDisplays;
  }

  @PostMapping("getLisAccount/{idCustomer}")
  public Page<AccountDisplay> getLisAccount(
      @PathVariable(value = "idCustomer") String idCustomer,
      @RequestParam(name = "page", defaultValue = "0") int page,
		@RequestParam(name = "size", defaultValue = "10") int size
		  ) {
	String name = SecurityContextHolder.getContext().getAuthentication().getName();
	User user = userRepository.findByUserNameOrUserEmail(name, name).get();
	Page<Account> listCustomer=null; 
	List<Account> accounts=new ArrayList<Account>();
	if(user.getUserType()==1) {
		listCustomer=accountRepository.findAllByCustomerCodeAndIdAgence(PageRequest.of(page, size),idCustomer,user.getIdAgence()); 
	  }
	  if(user.getUserType()==3) {
		  accounts=accountRepository.findAllByZone(user.getIdAgence());
            /*List<AgenceAdministration> Agences=AgenceRepository.findByCodeZone(user.getIdAgence());
			for(AgenceAdministration agence:Agences) {
			listCustomer.addAll(accountRepository.findAllByCustomerCodeAndIdAgence(idCustomer,agence.getCodeAgence())); 			}
         */
		  for(Account acc:accounts) {
				listCustomer=accountRepository.findAllByCustomerCodeAndIdAgence(PageRequest.of(page, size),idCustomer,acc.getIdAgence()); 			
				}
  
		  }
  
	  if(user.getUserType()==4) {
		  accounts=accountRepository.findAllByRegion(user.getIdAgence());
		  for(Account acc:accounts) {
				listCustomer=accountRepository.findAllByCustomerCodeAndIdAgence(PageRequest.of(page, size),idCustomer,acc.getIdAgence()); 	
				}

		  }
	  if(user.getUserType()==5) {
		  
		listCustomer=accountRepository.findAllByCustomerCodeWithPagination(PageRequest.of(page, size),idCustomer);
				

		  }

		  /*List<Zone> zones=zoneRepository.findByCodeRegion(user.getIdAgence());
		  for(Zone z:zones) {
			  int codeZone=z.getCodeZone();
			  List<AgenceAdministration> agence=new ArrayList<AgenceAdministration>();

			  agence=AgenceRepository.findByCodeZone(codeZone);
			  for(AgenceAdministration ag:agence) {
				  listCustomer.addAll(accountRepository.findAllByCustomerCodeAndIdAgence(idCustomer,ag.getCodeAgence())); 
  
			  }*/
	
    
    List<AccountDisplay> customerDisplays = new ArrayList<>();
    for (Account c : listCustomer
    ) {
      AccountStatus accountStatus = accountStatusRepository
          .findByAstCode(String.valueOf(c.getAstCode()));
      
      AccountDisplay accountDisplay = new AccountDisplay(c.getAccountCode(), c.getAccountNum(),
          c.getAccountNumAttached(), c.getAccountAuthorize(), c.getAccountAvailable(),
          c.getAccountBilling(), c.getAccountRevolvingLimit(), c.getAccountName(), c.getAstCode(),
          c.getCurrency(), accountStatus.getAstLibelle(),c.getIdAgence(),c.getAccountExceeding(),c.getAccountBalance(),c.getPreauthAmount(),c.getRefundAmount());
      customerDisplays.add(accountDisplay);
    }
    Page<AccountDisplay> pageDisplay = new PageImpl<>(customerDisplays,PageRequest.of(page, size), listCustomer.getTotalElements());

    return pageDisplay;
  }


  @GetMapping("/customer/{idCustomer}")
  public ResponseEntity<Customer> getEmployeeById(
      @PathVariable(value = "idCustomer") String idCustomer)
      throws ResourceNotFoundException {
    Customer user =
        customerRepository.findByCustomerCode(Integer.parseInt(idCustomer))
            .orElseThrow(() ->
                new ResourceNotFoundException("Customer not found for this id :: " + idCustomer));
logger.info(user.toString());
    return ResponseEntity.ok().body(user);
  }


  @PutMapping("/customer/{idCustomer}")
  public ResponseEntity<Customer> updateEmployee(
      @PathVariable(value = "idCustomer") String idCustomer,
      @Valid @RequestBody Customer employeeDetails) throws ResourceNotFoundException {
    logger.info(employeeDetails.toString());
    Customer customer = customerRepository.findByCustomerCode(Integer.parseInt(idCustomer))
        .orElseThrow(
            () -> new ResourceNotFoundException("Customer not found for this id :: " + idCustomer));
    modelMapper.map(employeeDetails, customer);

    final Customer updatedEmployee = customerRepository.save(customer);
    logger.info(updatedEmployee.toString());
    return ResponseEntity.ok(updatedEmployee);
  }

  @PostMapping("generateCard")
  public ResponseEntity<Card> generateCard(@Valid @RequestBody CardDisplay customer) {
    Card card = new Card();

    return ResponseEntity.ok(card);

  }

  @PostMapping("addIdentificationType")
  public ResponseEntity<String> addIdentificationType(
      @Valid @RequestBody IdentificationType customer) {
    logger.info(customer.toString());
    identificationTypeRepository.save(customer);

    return ResponseEntity.ok().body("IdentificationType added successfully!");

  }

  @PutMapping("activate/{id}")
  public ResponseEntity<Customer> activate(@PathVariable(value = "id") Integer userId) {
    Customer customer = customerRepository.findByCustomerCode(userId).get();
    customer.setCustomerStatusCode(1);

    customerRepository.save(customer);
    logger.info(customer.toString());
    return ResponseEntity.ok(customer);
  }
  
  


  @PutMapping("deactivate/{id}")
  public ResponseEntity<Customer> bloqued(@PathVariable(value = "id") Integer userId) {
    Customer customer = customerRepository.findByCustomerCode(userId).get();
    customer.setCustomerStatusCode(2);

    customerRepository.save(customer);
    logger.info(customer.toString());
    return ResponseEntity.ok(customer);
  }

  @GetMapping("getAllIdentificationTypes")
  public List<IdentificationType> getAllIdentificationTypes() {
    return identificationTypeRepository.findAll();
  }
  
  @GetMapping("getAllStatus")
  public List<CustomerStatus> getAllStatus() {
    return customerStatusRepository.findAll();
  }

  @GetMapping("/identificationType/{idIdentificationType}")
  public ResponseEntity<IdentificationType> getIdentificationType(
      @PathVariable(value = "idIdentificationType") String idIdentificationType) {
    IdentificationType user =
        identificationTypeRepository
            .findByIdentificationTypeCode(Integer.parseInt(idIdentificationType));

    return ResponseEntity.ok().body(user);
  }

	@GetMapping("/getRiskManagement/{idAccount}")
	public ResponseEntity<RiskManegementDisplay> getRiskManagement(@PathVariable(value = "idAccount") String idAccount)
			throws ResourceNotFoundException {
		Customer account = customerRepository.findByCustomerCode(Integer.parseInt(idAccount))
				.orElseThrow(() -> new ResourceNotFoundException("Account not found for this id :: " + idAccount));
		if (Strings.isNullOrEmpty(String.valueOf(account.getGlobalRiskCode()))) {
			return ResponseEntity.ok().body(new RiskManegementDisplay());
		}
		if (account.getGlobalRiskCode() != null) {
			GlobalRiskManagement globalRiskManagement = globalRepository.findById(account.getGlobalRiskCode()).get();
			Periodicity periodicity = periodicityRepository.findById(globalRiskManagement.getPeriodicityType()).get();

			RiskManegementDisplay riskManagementRequest = new RiskManegementDisplay(
					globalRiskManagement.getGlobalRiskManagCode(), globalRiskManagement.getMinAmount(),
					globalRiskManagement.getMaxAmount(), globalRiskManagement.getGolbalAmount(),
					globalRiskManagement.getGlobalNumber(), globalRiskManagement.getGlCurrencyAmount(),
					globalRiskManagement.getGlCurrencyNumber(), globalRiskManagement.getGlStartDate(),
					globalRiskManagement.getGlEndDate(), periodicity.getLibelle());
			List<DisplayDetailsManagement> detailsRequestList = new ArrayList<>();
			if (!detailRepository.findByGlobalCode(account.getGlobalRiskCode()).isEmpty()) {
				List<DetailRiskManagment> detailRiskManagments = detailRepository
						.findByGlobalCode(account.getGlobalRiskCode());
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

  @PostMapping("riskManagement/{id}")
  public ResponseEntity<?> riskManagement(@RequestBody RiskManagementRequest rm,
      @PathVariable(value = "id") String userId) {
logger.info(rm.toString());
    Customer account = customerRepository.findByCustomerCode(Integer.parseInt(userId)).get();
    if (account.getGlobalRiskCode()==null) {
      Periodicity periodicity = periodicityRepository.findByLibelle(rm.getGlPeriodicityType());
      GlobalRiskManagement globalRiskManagement = new GlobalRiskManagement(rm.getGlMinAmount(),
          rm.getGlMaxAmount(), rm.getGlobalAmount(), rm.getGlobalNumber(),
          rm.getGlCurrencyAmount(), rm.getGlCurrencyNumber(), rm.getGlStartDate(),
          rm.getGlEndDate(), periodicity.getCode());
      GlobalRiskManagement globalRiskManagement1 = globalRepository.save(globalRiskManagement);
logger.info(globalRiskManagement1.toString());
logger.info(periodicity.toString());
      if (rm.getDetailsRequestList() != null) {
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
      account.setGlobalRiskCode(globalRiskManagement1.getGlobalRiskManagCode());
      customerRepository.save(account);
      return ResponseEntity.ok().body(gson.toJson("RiskManagement added successfully!"));

    }
    GlobalRiskManagement globalRiskManagement = globalRepository
        .findById(account.getGlobalRiskCode()).get();
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
logger.info(globalRiskManagement.toString());
    if (detailRepository.findByGlobalCode(account.getGlobalRiskCode())
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
              serviceValues.getCodeTransaction(), account.getGlobalRiskCode(),
              dr.getDrMaxAmount(),
              dr.getDrAmountLimit(), dr.getDrNumberLimit(), dr.getDrStartDate(),
              dr.getDrEndDate(),
              dr.getDrCurrencyAmount(), dr.getDrCurrencyNumber(), periodicity1.getCode());
          detailRepository.save(detailRiskManagment);
          logger.info(detailRiskManagment.toString());
        }

      }
      return ResponseEntity.ok().body(gson.toJson("RiskManagement updated successfully!"));


    }


    List<DetailRiskManagment> detailRiskManagments = detailRepository
        .findByGlobalCode(account.getGlobalRiskCode());
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
            serviceValues.getCodeTransaction(),account.getGlobalRiskCode(),
            dr.getDrMaxAmount(),
            dr.getDrAmountLimit(), dr.getDrNumberLimit(), dr.getDrStartDate(),
            dr.getDrEndDate(),
            dr.getDrCurrencyAmount(), dr.getDrCurrencyNumber(), periodicity1.getCode());
        detailRepository.save(detailRiskManagment);
        logger.info(detailRiskManagment.toString());
      }
    }

    return ResponseEntity.ok().body(gson.toJson("RiskManagement updated successfully!"));

  }

  @DeleteMapping("deleteRiskManagement/{id}")
  public ResponseEntity<String> deleteRiskManagement(@PathVariable(value = "id") Integer type) {
    Customer account = customerRepository.findByCustomerCode(type).get();
    GlobalRiskManagement globalRiskManagement = globalRepository
        .findById(account.getGlobalRiskCode()).get();
    List<DetailRiskManagment> detailRiskManagments = detailRepository
        .findByGlobalCode(account.getGlobalRiskCode());
    for (DetailRiskManagment dr : detailRiskManagments
    ) {
      detailRepository.delete(dr);
    }
    account.setGlobalRiskCode(null);
    customerRepository.save(account);
    globalRepository.delete(globalRiskManagement);
    return ResponseEntity.ok().body(gson.toJson("GlobalRiskManagement deleted  successfully!"));

  }
}
