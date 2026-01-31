package com.mss.backOffice.controller;

import com.google.common.base.Strings;
import com.google.common.base.Throwables;
import com.google.gson.Gson;
import com.mss.unified.entities.*;
import com.mss.backOffice.Response.DisplayDetailsHistory;
import com.mss.backOffice.Response.RiskManegementHistoryDisplay;
import com.mss.backOffice.exception.ResourceNotFoundException;
import com.mss.backOffice.message.response.ResetPinOfflineRequestsDisplay;
import com.mss.unified.repositories.*;
import com.mss.backOffice.request.AccountDisplay;
import com.mss.backOffice.request.ActionCardRequest;
import com.mss.backOffice.request.CardBalanceDisplay;
import com.mss.backOffice.request.CardDisplay;
import com.mss.backOffice.request.CardReeditionPinRequest;
import com.mss.backOffice.request.CardRenewelRequest;
import com.mss.backOffice.request.CustomerDisplay;
import com.mss.backOffice.request.DemandeActionsFilter;
import com.mss.backOffice.request.DetailsRequest;
import com.mss.backOffice.request.DisplayDetailsManagement;
import com.mss.backOffice.request.ReceivedCardsFromSmtFilter;
import com.mss.backOffice.request.ResetPinOfflineFilter;
import com.mss.backOffice.request.RiskManagementRequest;
import com.mss.backOffice.request.RiskManegementDisplay;

import java.math.BigInteger;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import javax.transaction.Transactional;

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
@RequestMapping("cardManagement")
public class CardController {

	@Autowired
	UserRepository userRepository;
	@Autowired
	private CardRepository cardRepository;
	@Autowired
	CustomerRepository customerRepository;
	@Autowired
	CustomerStatusRepository customerStatusRepository;
	@Autowired
	AccountRepository accountRepository;
	@Autowired
	IdentificationTypeRepository identificationTypeRepository;
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
	@Autowired
	TransactionRepo allocatedTransactionRepo;
	@Autowired
	DetailRiskManagmentRepository detailRMRepository;
	@Autowired
	ProgramReposiroty programReposiroty;
	@Autowired
	BinOnUsRepository binOnUsRepository;
	@Autowired
	ProductRepositpry productRepositpry;

	@Autowired
	CardHistoryRepository cardHistoryRepository;
	@Autowired
	GlobalRiskManagementRepository globalRiskManagementRepository;

	@Autowired
	DetailRiskManagmentRepository detailRiskManagmentRepository;
	@Autowired
	private ActionCardRepository actionCardRepository;
	@Autowired
	private ProgramReposiroty programRepository;
	@Autowired
	private StockBinRepository stockBinRepository;
	@Autowired
	private DemandeActionRepository demandeActionRepository;
	@Autowired
	private RiskManagementDemandeRepository riskManagementDemandeRepository;
	@Autowired
	private DetailRiskManagementDemandeRepository detailRiskManagementDemandeRepository;
	@Autowired
	private RiskHistoryRespository riskHistoryRespository;
	@Autowired
	private DetailRiskHistoryRepository detailRiskHistoryRepository;
	@Autowired
	private DemandeResetPinOfflineRepository demandeResetPinOfflineRepository;
	
	private static final Gson gson = new Gson();
	private static final Logger logger = LoggerFactory.getLogger(CardController.class);

	@GetMapping("/cardBalance/{pan}")
	public CardBalanceDisplay getCardBalance(@PathVariable(value = "pan") String pan) {
		CardBalanceDisplay cardBalanceDisplay = new CardBalanceDisplay();
		Optional<Card> card = cardRepository.findByCardNum(pan);
		if (card.isPresent()) {
			Optional<Account> account = accountRepository.findByAccountCode(card.get().getAccCode());
			Optional<GlobalRiskManagement> globalRiskManagement = globalRiskManagementRepository
					.findByGlobalRiskManagCode(Integer.parseInt(card.get().getGlobalRiskCode()));

			if (account.isPresent() && globalRiskManagement.isPresent()) {
				cardBalanceDisplay.setPan(pan);
				BigInteger withExceeding = account.get().getAccountAvailable().add(account.get().getAccountExceeding());
				logger.info("with exceeding => {}", withExceeding);
				BigInteger amountToSubstract = account.get().getAccountAuthorize();
				if (account.get().getPreauthAmount() != null) {
					amountToSubstract = amountToSubstract.add(account.get().getPreauthAmount());
				}

				if (account.get().getRefundAmount() != null) {
					amountToSubstract = amountToSubstract.add(account.get().getRefundAmount());
				}
				logger.info("amountToSubstract => {}", amountToSubstract);

				BigInteger diff = withExceeding.subtract(amountToSubstract);
				logger.info("diff is => {}", diff);

				long globalAmount = Long.parseLong(globalRiskManagement.get().getGolbalAmount());

				Long diffRisque = globalAmount - globalRiskManagement.get().getGlCurrencyAmount();

				logger.info("diffRisque is => {}", diffRisque);
				try {
					cardBalanceDisplay.setAmount(String.valueOf(Math.min(diff.longValue(), diffRisque)));

				} catch (Exception e) {

					cardBalanceDisplay.setAmount(String.valueOf(diffRisque));

				}
			}
		}
		return cardBalanceDisplay;
	}

	@GetMapping("/customer/{idCard}")
	public ResponseEntity<CustomerDisplay> getEmployeeById(@PathVariable(value = "idCard") Integer idCustomer)
			throws ResourceNotFoundException {
//		Customer c = new Customer();
		Card card = cardRepository.findByCardCode(idCustomer)
				.orElseThrow(() -> new ResourceNotFoundException("Customer not found for this id :: " + idCustomer));
		Account account = accountRepository.findByAccountCode(card.getAccCode()).get();
		Customer c = customerRepository.findByCustomerCode(Integer.parseInt(account.getCustomerCode())).get();
//		String name = SecurityContextHolder.getContext().getAuthentication().getName();
//		User ConnectUser = userRepository.findByUserNameOrUserEmail(name, name).get();
//		List<Account> accounts = new ArrayList<Account>();
//		Account account = new Account();
//
//		if (ConnectUser.getUserType() == 1) {
//			accounts = accountRepository.findAllByAgence(ConnectUser.getIdAgence());
//			for (Account acc : accounts) {
//				account = accountRepository.findByAccountCode(user.getAccCode()).get();
//				c = customerRepository.findByCustomerCode(Integer.parseInt(account.getCustomerCode())).get();
//			}
//		}
//		if (ConnectUser.getUserType() == 3) {
//			accounts = accountRepository.findAllByZone(ConnectUser.getIdAgence());
//			for (Account acc : accounts) {
//				account = accountRepository.findByAccountCode(user.getAccCode()).get();
//				c = customerRepository.findByCustomerCode(Integer.parseInt(account.getCustomerCode())).get();
//			}
//		}
//		if (ConnectUser.getUserType() == 4) {
//			accounts = accountRepository.findAllByRegion(ConnectUser.getIdAgence());
//			for (Account acc : accounts) {
//				account = accountRepository.findByAccountCode(user.getAccCode()).get();
//				c = customerRepository.findByCustomerCode(Integer.parseInt(account.getCustomerCode())).get();
//			}
//		}
//		if (ConnectUser.getUserType() == 5) {
//
//			account = accountRepository.findByAccountCode(user.getAccCode()).get();
//			c = customerRepository.findByCustomerCode(Integer.parseInt(account.getCustomerCode())).get();
//
//		}

		// Account account =
		// accountRepository.findByAccountCode(user.getAccCode()).get();
		/*
		 * Customer c = customerRepository.findByCustomerCode(Integer.parseInt(account.
		 * getCustomerCode())) .get();
		 */
		CustomerStatus customerStatus = customerStatusRepository.findById(c.getCustomerStatusCode()).get();

		IdentificationType identificationType = identificationTypeRepository
				.findByIdentificationTypeCode(c.getIdentityCode());

		CustomerDisplay customerDisplay = new CustomerDisplay(c.getCustomerCode(), "", c.getGlobalRiskCode(),
				c.getCustomerStatusCode(), c.getCustomerIdentidy(), c.getCustomerName(), c.getCustomerAddress(),
				c.getCustomerGSM(), c.getCustomerCity(), c.getCustomerTown(), c.getCustomerPostal(),
				customerStatus.getLibelle());
		customerDisplay.setRadical(c.getRadical());
		customerDisplay.setFirstName(c.getCustomerFirstName());
		customerDisplay.setLastName(c.getCustomerLastName());

		if (identificationType != null)
			customerDisplay.setIdentityCode(identificationType.getLibelle());
		logger.info(customerDisplay.toString());
		return ResponseEntity.ok().body(customerDisplay);
	}

	@GetMapping("/account/{idCard}")
	public ResponseEntity<AccountDisplay> getAccount(@PathVariable(value = "idCard") Integer idCustomer)
			throws ResourceNotFoundException {
		Card user = cardRepository.findByCardCode(idCustomer)
				.orElseThrow(() -> new ResourceNotFoundException("Customer not found for this id :: " + idCustomer));
		logger.info(user.toString());
		String name = SecurityContextHolder.getContext().getAuthentication().getName();
		User ConnectUser = userRepository.findByUserNameOrUserEmail(name, name).get();
		List<Account> accounts = new ArrayList<Account>();
		Account account = new Account();

		if (ConnectUser.getUserType() == 1) {
			accounts = accountRepository.findAllByAgence(ConnectUser.getIdAgence());
			for (Account acc : accounts) {
				account = accountRepository.findByAccountCode(user.getAccCode()).get();
			}
		}
		if (ConnectUser.getUserType() == 3) {
			accounts = accountRepository.findAllByZone(ConnectUser.getIdAgence());
			for (Account acc : accounts) {
				account = accountRepository.findByAccountCode(user.getAccCode()).get();
			}
		}
		if (ConnectUser.getUserType() == 4) {
			accounts = accountRepository.findAllByRegion(ConnectUser.getIdAgence());
			for (Account acc : accounts) {
				account = accountRepository.findByAccountCode(user.getAccCode()).get();
			}
		}
		if (ConnectUser.getUserType() == 5) {
			account = accountRepository.findByAccountCode(user.getAccCode()).get();

		}

		logger.info(account.toString());
		AccountStatus accountStatus = accountStatusRepository.findByAstCode(String.valueOf(account.getAstCode()));
		logger.info(accountStatus.toString());
		AccountDisplay customerDisplay = new AccountDisplay(account.getAccountCode(), account.getAccountNum(),
				account.getAccountNumAttached(), account.getAccountAuthorize(), account.getAccountAvailable(),
				account.getAccountBilling(), account.getAccountRevolvingLimit(), account.getAccountName(),
				account.getAstCode(), account.getCurrency(), accountStatus.getAstLibelle(), account.getIdAgence(),
				account.getAccountExceeding(), account.getAccountBalance(), account.getPreauthAmount(),
				account.getRefundAmount());
		logger.info(customerDisplay.toString());
		return ResponseEntity.ok().body(customerDisplay);
	}

	@GetMapping("/getRiskManagement/{idAccount}")
	public ResponseEntity<RiskManegementDisplay> getRiskManagement(@PathVariable(value = "idAccount") String idAccount)
			throws ResourceNotFoundException {
		Card card = cardRepository.findByCardCode(Integer.parseInt(idAccount))
				.orElseThrow(() -> new ResourceNotFoundException("Account not found for this id :: " + idAccount));
		logger.info(card.toString());
		if (Strings.isNullOrEmpty(String.valueOf(card.getGlobalRiskCode()))) {
			return ResponseEntity.ok().body(new RiskManegementDisplay());
		}

		GlobalRiskManagement globalRiskManagement = globalRepository
				.findById(Integer.parseInt(card.getGlobalRiskCode())).get();
		logger.info(globalRiskManagement.toString());
		Periodicity periodicity = periodicityRepository.findById(globalRiskManagement.getPeriodicityType()).get();
		logger.info(periodicity.toString());
		RiskManegementDisplay riskManagementRequest = new RiskManegementDisplay(
				globalRiskManagement.getGlobalRiskManagCode(), globalRiskManagement.getMinAmount(),
				globalRiskManagement.getMaxAmount(), globalRiskManagement.getGolbalAmount(),
				globalRiskManagement.getGlobalNumber(), globalRiskManagement.getGlCurrencyAmount(),
				globalRiskManagement.getGlCurrencyNumber(), globalRiskManagement.getGlStartDate(),
				globalRiskManagement.getGlEndDate(), periodicity.getLibelle());
		logger.info(riskManagementRequest.toString());
		Optional<Program> foundedProgram = programRepository
				.findByProgramCode(card.getProgrameId());
		if (foundedProgram.isPresent()) {
			riskManagementRequest.setCurrency(foundedProgram.get().getCurrency());
		}
		List<DisplayDetailsManagement> detailsRequestList = new ArrayList<>();
		List<DetailRiskManagment> detailRiskManagments = detailRepository
				.findByGlobalCode(Integer.parseInt(card.getGlobalRiskCode()));
		
		if (!detailRiskManagments.isEmpty()) {
			
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
	}

	@GetMapping("/getRiskManagementByAccount/{idAccount}")
	public ResponseEntity<RiskManegementDisplay> getRiskManagementByAccount(
			@PathVariable(value = "idAccount") String idAccount) throws ResourceNotFoundException {

		Account account = accountRepository.findByAccountCode(Integer.parseInt(idAccount)).get();

		GlobalRiskManagement globalRiskManagement = globalRepository
				.findById(Integer.parseInt(account.getGlobalRiskMangCode())).get();

		Periodicity periodicity = periodicityRepository.findById(globalRiskManagement.getPeriodicityType()).get();

		RiskManegementDisplay riskManagementRequest = new RiskManegementDisplay(
				globalRiskManagement.getGlobalRiskManagCode(), globalRiskManagement.getMinAmount(),
				globalRiskManagement.getMaxAmount(), globalRiskManagement.getGolbalAmount(),
				globalRiskManagement.getGlobalNumber(), globalRiskManagement.getGlCurrencyAmount(),
				globalRiskManagement.getGlCurrencyNumber(), globalRiskManagement.getGlStartDate(),
				globalRiskManagement.getGlEndDate(), periodicity.getLibelle());

		logger.info(riskManagementRequest.toString());

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
	}

	@GetMapping("/cardOperations/{id}")
	public List<CardHistory> getCardOpeationsHistory(@PathVariable(value = "id") Integer id) {
		return cardHistoryRepository.findByCardCode(id);
	}

	@PostMapping("riskManagement/{id}")
	public ResponseEntity<?> riskManagement(@RequestBody RiskManagementRequest rm,
			@PathVariable(value = "id") String userId) throws ResourceNotFoundException {
		logger.info(rm.toString());
		String name = SecurityContextHolder.getContext().getAuthentication().getName();
		Optional<User> user = userRepository.findByUserNameOrUserEmail(name, name);
		if (!user.isPresent()) {
			throw new RuntimeException("Error saving risk !");
		}
		Card account = cardRepository.findByCardCode(Integer.parseInt(userId))
				.orElseThrow(() -> new ResourceNotFoundException("Account not found for this id :: " + userId));
		logger.info(account.toString());
		if (String.valueOf(account.getGlobalRiskCode()).equals("null") || account.getGlobalRiskCode() == null) {
			logger.info("GlobalRiskCode is null or empty");
			Periodicity periodicity = periodicityRepository.findByLibelle(rm.getGlPeriodicityType());
			logger.info("periodicity => {}", periodicity.toString());
			GlobalRiskManagement globalRiskManagement = new GlobalRiskManagement(rm.getGlMinAmount(),
					rm.getGlMaxAmount(), rm.getGlobalAmount(), rm.getGlobalNumber(), rm.getGlStartDate(),
					rm.getGlEndDate(), periodicity.getCode());
			logger.info("globalRiskManagement => {}", globalRiskManagement.toString());
			GlobalRiskManagement globalRiskManagement1 = globalRepository.save(globalRiskManagement);
			logger.info(globalRiskManagement1.toString());
			if (rm.getDetailsRequestList() != null) {
				List<DetailsRequest> detailsRequests = rm.getDetailsRequestList();
				for (DetailsRequest dr : detailsRequests) {
					List<String> strings = dr.getTransactionCode();
					for (String s : strings) {
						EmvServiceValues serviceValues = emvServiceValuesRepository.findByLibelle(s);
						logger.info(serviceValues.toString());
						Periodicity periodicity1 = periodicityRepository.findByLibelle(dr.getDrTypePeriodicity());
						logger.info(periodicity1.toString());
						DetailRiskManagment detailRiskManagment = new DetailRiskManagment(dr.getDrMinAmount(),
								serviceValues.getCodeTransaction(), globalRiskManagement1.getGlobalRiskManagCode(),
								dr.getDrMaxAmount(), dr.getDrAmountLimit(), dr.getDrNumberLimit(), dr.getDrStartDate(),
								dr.getDrEndDate(), dr.getDrCurrencyAmount(), dr.getDrCurrencyNumber(),
								periodicity1.getCode());
						detailRepository.save(detailRiskManagment);
						logger.info(detailRiskManagment.toString());
					}
				}
			}
			account.setGlobalRiskCode(String.valueOf(globalRiskManagement1.getGlobalRiskManagCode()));

			logger.info(account.toString());
			return ResponseEntity.ok().body(gson.toJson("RiskManagement added successfully!"));

		}
		GlobalRiskManagement globalRiskManagement = globalRepository
				.findById(Integer.parseInt(account.getGlobalRiskCode())).get();
		Periodicity periodicity = periodicityRepository.findByLibelle(rm.getGlPeriodicityType());
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
		if (detailRepository.findByGlobalCode(Integer.parseInt(account.getGlobalRiskCode())).isEmpty()) {
			List<DetailsRequest> detailsRequests = rm.getDetailsRequestList();

			for (DetailsRequest dr : detailsRequests) {
				List<String> strings = dr.getTransactionCode();
				for (String s : strings) {
					EmvServiceValues serviceValues = emvServiceValuesRepository.findByLibelle(s);

					Periodicity periodicity1 = periodicityRepository.findByLibelle(dr.getDrTypePeriodicity());

					DetailRiskManagment detailRiskManagment = new DetailRiskManagment(dr.getDrMinAmount(),
							serviceValues.getCodeTransaction(), Integer.parseInt(account.getGlobalRiskCode()),
							dr.getDrMaxAmount(), dr.getDrAmountLimit(), dr.getDrNumberLimit(), dr.getDrStartDate(),
							dr.getDrEndDate(), dr.getDrCurrencyAmount(), dr.getDrCurrencyNumber(),
							periodicity1.getCode());
					detailRepository.save(detailRiskManagment);
				}

			}
			return ResponseEntity.ok().body(gson.toJson("RiskManagement updated successfully!"));

		}

		List<DetailRiskManagment> detailRiskManagments = detailRepository
				.findByGlobalCode(Integer.parseInt(account.getGlobalRiskCode()));
		for (DetailRiskManagment dr : detailRiskManagments) {
			detailRepository.delete(dr);
		}
		List<DetailsRequest> detailsRequests = rm.getDetailsRequestList();

		for (DetailsRequest dr : detailsRequests) {
			List<String> strings = dr.getTransactionCode();
			for (String s : strings) {
				EmvServiceValues serviceValues = emvServiceValuesRepository.findByLibelle(s);

				Periodicity periodicity1 = periodicityRepository.findByLibelle(dr.getDrTypePeriodicity());

				DetailRiskManagment detailRiskManagment = new DetailRiskManagment(dr.getDrMinAmount(),
						serviceValues.getCodeTransaction(), Integer.parseInt(account.getGlobalRiskCode()),
						dr.getDrMaxAmount(), dr.getDrAmountLimit(), dr.getDrNumberLimit(), dr.getDrStartDate(),
						dr.getDrEndDate(), dr.getDrCurrencyAmount(), dr.getDrCurrencyNumber(), periodicity1.getCode());
				detailRepository.save(detailRiskManagment);
			}
		}
		account.setModifDate(new Date());
		cardRepository.save(account);
		CardHistory cardHistory = new CardHistory();
		cardHistory.setCardCode(account.getCardCode());
		cardHistory.setOperation("Plafonds mis à jour");
		cardHistory.setOperation_date(account.getModifDate());
		cardHistory.setEditedBy(user.get().getUserName());
		cardHistoryRepository.save(cardHistory);
		return ResponseEntity.ok().body(gson.toJson("RiskManagement updated successfully!"));

	}

	@DeleteMapping("deleteRiskManagement/{id}")
	public ResponseEntity<String> deleteRiskManagement(@PathVariable(value = "id") Integer type) {
		String name = SecurityContextHolder.getContext().getAuthentication().getName();
		Optional<User> user = userRepository.findByUserNameOrUserEmail(name, name);
		if (!user.isPresent()) {
			throw new RuntimeException("Error saving risk !");
		}
		Card account = cardRepository.findByCardCode(type).get();
		GlobalRiskManagement globalRiskManagement = globalRepository
				.findById(Integer.parseInt(account.getGlobalRiskCode())).get();
		List<DetailRiskManagment> detailRiskManagments = detailRepository
				.findByGlobalCode(Integer.parseInt(account.getGlobalRiskCode()));
		for (DetailRiskManagment dr : detailRiskManagments) {
			detailRepository.delete(dr);
		}
		account.setGlobalRiskCode(null);
		account.setModifDate(new Date());

		cardRepository.save(account);
		globalRepository.delete(globalRiskManagement);
		logger.info(globalRiskManagement.toString());

		CardHistory cardHistory = new CardHistory();
		cardHistory.setCardCode(account.getCardCode());
		cardHistory.setOperation("Supprimer Plafond");
		cardHistory.setOperation_date(account.getModifDate());
		cardHistory.setEditedBy(user.get().getUserName());
		cardHistoryRepository.save(cardHistory);

		return ResponseEntity.ok().body(gson.toJson("GlobalRiskManagement deleted  successfully!"));

	}

	@GetMapping("getAvailableCardsByAccountNumber/{accountNumber}")
	public List<Card> getAvailableCardsByAccountNumber(@PathVariable(value = "accountNumber") String accountNumber) {
		try {
			return cardRepository.findByAccountNumber(accountNumber);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	@PostMapping("getReceivedCardsFromSmt")
	Page<CardDisplay> getReceivedCardsFromSmt(@RequestBody ReceivedCardsFromSmtFilter receivedCardsFromSmtFilter,
			@RequestParam(name = "page", defaultValue = "0") int page,
			@RequestParam(name = "size", defaultValue = "10") int size, @RequestParam(name = "sortOn") String sortOn,
			@RequestParam(name = "dir") String dir) {
		Order order = null;
		if (dir.equals("desc"))
			order = new Order(Sort.Direction.DESC, sortOn);
		else
			order = new Order(Sort.Direction.ASC, sortOn);

		Page<Card> all = cardRepository.getReceivedCardsFromSmt(PageRequest.of(page, size, Sort.by(order)),
				receivedCardsFromSmtFilter.getPan());
		List<CardDisplay> cardDisplays = new ArrayList<CardDisplay>();

		for (Card c : all) {
			CardDisplay cd = new CardDisplay();
			Account account = accountRepository.findByAccountCode(c.getAccCode()).get();

			cd.setCardNum(c.getCardNum());
			cd.setCardCode(c.getCardCode());
			cd.setAccCode(account.getAccountName());
			cardDisplays.add(cd);

		}
		return new PageImpl<>(cardDisplays, PageRequest.of(page, size), all.getTotalElements());

	}

	@PostMapping("addActionCard")
	public ResponseEntity<String> addActionCard(@RequestBody ActionCardRequest actionCardRequest) {
		try {

			List<ActionCard> actions = new ArrayList<ActionCard>();
			List<Card> cardsList = new ArrayList<Card>();
			List<StockBin> stocks = new ArrayList<StockBin>();
			List<CardHistory> cardHistories = new ArrayList<CardHistory>();
			String name = SecurityContextHolder.getContext().getAuthentication().getName();
			Optional<User> user = userRepository.findByUserNameOrUserEmail(name, name);
			if (!user.isPresent()) {
				throw new RuntimeException("Error saving action !");
			}

			for (Integer cardForRenewelCode : actionCardRequest.getCardsForRenewel()) {
				Optional<Card> card = cardRepository.findByCardCode(cardForRenewelCode);
				if (card.isPresent()) {
					if (card.get().getCardStatusCode() == 2) {
						if (!actionCardRepository.findByCardCode(card.get().getCardCode()).isPresent()) {
							if (card.get().getRandomPin() != null) {
								ActionCard actionCard = new ActionCard();
								actionCard.setCardCode(card.get().getCardCode());
								actionCard.setStatus(1);
								actionCard.setIsRenewel(1);
								actionCard.setIsReplacement(0);
								actionCard.setIsPinChange(0);
								actionCard.setRemplacementWithPin(false);
								Optional<Program> foundedProgram = programRepository
										.findByProgramCode(card.get().getProgrameId());
								if (foundedProgram.isPresent()) {
									LocalDate localDate = new Date().toInstant().atZone(ZoneId.systemDefault())
											.toLocalDate();
									LocalDate newLocalDate = localDate
											.plusYears(foundedProgram.get().getCprLifeCycle());
									actionCard.setCardNewExpDate(java.sql.Date.valueOf(newLocalDate));
									
									actionCard.setIsVisa(foundedProgram.get().getCurrency().equals("012")?0:1);
									actions.add(actionCard);

									CardHistory cardHistory = new CardHistory();
									cardHistory.setCardCode(card.get().getCardCode());
									cardHistory.setOperation("Demande de renouvellement");
									cardHistory.setOperation_date(new Date());
									cardHistory.setEditedBy(user.get().getUserName());
									cardHistories.add(cardHistory);

									StockBin stockbin = stockBinRepository
											.findBybinOnUsCode(foundedProgram.get().getBinOnUsCode());
									if (stockbin.getStockSatim() > 0) {

										stockbin.setStockReserve(stockbin.getStockReserve() + 1);
										stockbin.setStockSatim(stockbin.getStockSatim() - 1);

										stocks.add(stockbin);

									} else
										return ResponseEntity.badRequest().body("EMPTY STOCK");

								} else
									return ResponseEntity.badRequest().body("PROGRAM NOT ACTIVE");

							} else {
								ActionCard actionCard = new ActionCard();
								actionCard.setCardCode(card.get().getCardCode());
								actionCard.setIsRenewel(0);
								actionCard.setIsReplacement(1);
								actionCard.setIsPinChange(0);
								actionCard.setNameInCard("");
								actionCard.setRemplacementWithPin(true);
								actionCard.setReplacementOldCard("YES");
								actionCard.setRemplacementReason("006");

								Optional<Program> foundedProgram = programRepository
										.findByProgramCode(card.get().getProgrameId());
								if (foundedProgram.isPresent()) {
									LocalDate localDate = new Date().toInstant().atZone(ZoneId.systemDefault())
											.toLocalDate();
									LocalDate newLocalDate = localDate
											.plusYears(foundedProgram.get().getCprLifeCycle());
									actionCard.setCardNewExpDate(java.sql.Date.valueOf(newLocalDate));

									actionCard.setStatus(1);
									actionCard.setIsVisa(foundedProgram.get().getCurrency().equals("012")?0:1);

									actions.add(actionCard);

									CardHistory cardHistory = new CardHistory();
									cardHistory.setCardCode(card.get().getCardCode());

									cardHistory.setOperation("Creation action de remplacement (Ancienne carte)");
									cardHistory.setOperation_date(new Date());
									cardHistory.setEditedBy(user.get().getUserName());
									cardHistories.add(cardHistory);
									StockBin stockbin = stockBinRepository
											.findBybinOnUsCode(foundedProgram.get().getBinOnUsCode());
									if (stockbin.getStockSatim() > 0) {

										stockbin.setStockReserve(stockbin.getStockReserve() + 1);
										stockbin.setStockSatim(stockbin.getStockSatim() - 1);

										stocks.add(stockbin);

									} else {
										return ResponseEntity.badRequest().body(
												"Stock insuffisant pour la création d'une demande de remplacement (ancienne carte)");
									}
								} else
									return ResponseEntity.badRequest().body("PROGRAM NOT ACTIVE");

							}

						} else
							return ResponseEntity.badRequest().body("ACTION ALREADY EXIST");
					} else
						return ResponseEntity.badRequest().body("CARD NOT ACTIVE");
				} else
					return ResponseEntity.badRequest().body("CARD NOT FOUND");
			}

			for (CardReeditionPinRequest cardForPinChange : actionCardRequest.getCardsForPinChange()) {
				Optional<Card> card = cardRepository.findByCardCode(cardForPinChange.getCardCode());
				if (card.isPresent()) {
					if (card.get().getCardStatusCode() == 2) {
						if (!actionCardRepository.findByCardCode(card.get().getCardCode()).isPresent()) {
							if (card.get().getRandomPin() != null) {
								ActionCard actionCard = new ActionCard();
								actionCard.setCardCode(card.get().getCardCode());
								actionCard.setStatus(1);
								actionCard.setIsRenewel(0);
								actionCard.setIsReplacement(0);
								actionCard.setIsPinChange(1);
								actionCard.setRemplacementWithPin(false);
								actionCard.setReeditionPinReason(cardForPinChange.getMotif());
								Optional<Program> foundedProgram = programRepository
										.findByProgramCode(card.get().getProgrameId());
								if (foundedProgram.isPresent()) {
									actionCard.setIsVisa(foundedProgram.get().getCurrency().equals("012")?0:1);

								}
								
								actions.add(actionCard);
								CardHistory cardHistory = new CardHistory();
								cardHistory.setCardCode(card.get().getCardCode());
								if (actionCard.getReeditionPinReason().equals("001"))
									cardHistory.setOperation("Demande de réedition de PIN (Code oublié/perdu)");
								else
									cardHistory.setOperation("Demande de réedition de PIN (Code illisible)");
								// cardHistory.setOperation("Demande de réedition de PIN");
								cardHistory.setOperation_date(new Date());
								cardHistory.setEditedBy(user.get().getUserName());
								cardHistories.add(cardHistory);

							} else
								return ResponseEntity.badRequest().body("OERATION IMPOSSIBLE");

						} else
							return ResponseEntity.badRequest().body("ACTION ALREADY EXIST");
					} else
						return ResponseEntity.badRequest().body("CARD NOT ACTIVE");
				} else
					return ResponseEntity.badRequest().body("CARD NOT FOUND");
			}

			for (Integer cardForNonRenewelCode : actionCardRequest.getCardsForNonRenewel()) {
				Optional<Card> card = cardRepository.findByCardCode(cardForNonRenewelCode);
				if (card.isPresent()) {
					if (card.get().getCardStatusCode() == 2) {

						card.get().setPreDate(null);
						cardsList.add(card.get());

						CardHistory cardHistory = new CardHistory();
						cardHistory.setCardCode(card.get().getCardCode());
						cardHistory.setOperation("Demande de non renouvellement");
						cardHistory.setOperation_date(new Date());
						cardHistory.setEditedBy(user.get().getUserName());
						cardHistories.add(cardHistory);

					} else
						return ResponseEntity.badRequest().body("CARD NOT ACTIVE");
				} else
					return ResponseEntity.badRequest().body("CARD NOT FOUND");
			}

			for (CardRenewelRequest cardForReplacement : actionCardRequest.getCardsForReplacement()) {
				Optional<Card> card = cardRepository.findByCardCode(cardForReplacement.getCardCode());
				if (card.isPresent()) {
					if (card.get().getCardStatusCode() == 2) {
						if (!actionCardRepository.findByCardCode(card.get().getCardCode()).isPresent()) {
							if (card.get().getRandomPin() != null) {
								ActionCard actionCard = new ActionCard();
								
								Optional<Program> foundedProgram = programRepository
										.findByProgramCode(card.get().getProgrameId());
								if (foundedProgram.isPresent()) {
									StockBin stockbin = stockBinRepository
											.findBybinOnUsCode(foundedProgram.get().getBinOnUsCode());
									if (stockbin.getStockSatim() > 0) {

										stockbin.setStockReserve(stockbin.getStockReserve() + 1);
										stockbin.setStockSatim(stockbin.getStockSatim() - 1);

										stocks.add(stockbin);

									} else
										return ResponseEntity.badRequest().body("EMPTY STOCK");
								} else
									return ResponseEntity.badRequest().body("PROGRAM NOT ACTIVE");
								
								actionCard.setIsVisa(foundedProgram.get().getCurrency().equals("012")?0:1);

								actionCard.setCardCode(card.get().getCardCode());
								actionCard.setStatus(1);
								actionCard.setIsRenewel(0);
								actionCard.setIsReplacement(1);
								actionCard.setIsPinChange(0);
								actionCard.setRemplacementReason(cardForReplacement.getMotif());
								actionCard.setNameInCard(
										cardForReplacement.getNameInCard() != null ? cardForReplacement.getNameInCard()
												: "");

								actionCard.setRemplacementWithPin(
										cardForReplacement.getReplacementPin() == 1 ? true : false);

								actions.add(actionCard);

								CardHistory cardHistory = new CardHistory();
								cardHistory.setCardCode(card.get().getCardCode());
								String operation="Demande de remplacement ";
								if (actionCard.isRemplacementWithPin())
									operation=operation+"avec PIN ";
						
								//cardHistory.setOperation("Demande de remplacement (Carte mal embossée)");
								if (actionCard.getRemplacementReason().equals("005"))
									operation=operation+"(Carte mal embossée)";
									
								else
									operation=operation+"(Carte endommagée)";
								
								//	cardHistory.setOperation("Demande de remplacement (Carte endommagée)");
								cardHistory.setOperation(operation);
								cardHistory.setOperation_date(new Date());
								cardHistory.setEditedBy(user.get().getUserName());
								cardHistories.add(cardHistory);

								
							} 

						} else
							return ResponseEntity.badRequest().body("ACTION ALREADY EXIST");
					} else
						return ResponseEntity.badRequest().body("CARD NOT ACTIVE");
				} else
					return ResponseEntity.badRequest().body("CARD NOT FOUND");
			}

			actionCardRepository.saveAll(actions);
			cardHistoryRepository.saveAll(cardHistories);
			cardRepository.saveAll(cardsList);
			stockBinRepository.saveAll(stocks);
			return ResponseEntity.ok().body(gson.toJson("ACTIONS SAVED successfully "));
		} catch (Exception e) {

			String stackTrace = Throwables.getStackTraceAsString(e);
			logger.info("Exception is=>{}", stackTrace);
			return ResponseEntity.badRequest().body("OPERATION IMPOSSIBLE");
		}
	}

	@GetMapping("glByCardCode/{cardCode}")
	GlobalRiskManagement findByGlobalRiskManagCodeByCardCode(@PathVariable int cardCode) {
		try {
			return globalRepository.findByGlobalRiskManagCodeByCardCode(cardCode);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return globalRepository.findByGlobalRiskManagCodeByCardCode(cardCode);

	}

	@GetMapping("dtlByGlobalCode/{globalCode}")
	List<DetailRiskManagment> findByDetailRiskManagCodeByGlobalCode(@PathVariable int globalCode) {
		try {
			return detailRepository.findByGlobalCode(globalCode);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return detailRepository.findByGlobalCode(globalCode);

	}

	@PostMapping("demandeRiskManagement/{id}")
	public ResponseEntity<?> demandeRiskManagement(@RequestBody RiskManagementRequest rm,
			@PathVariable(value = "id") String cardId) throws ResourceNotFoundException {
		try {
		logger.info(rm.toString());	
		String name = SecurityContextHolder.getContext().getAuthentication().getName();
		Optional<User> user = userRepository.findByUserNameOrUserEmail(name, name);
		if (!user.isPresent()) {
			throw new RuntimeException("Error saving risk !");
		}
		Card card = cardRepository.findByCardCode(Integer.parseInt(cardId))
				.orElseThrow(() -> new ResourceNotFoundException("CARD not found for this id :: " + cardId));
		Optional<Account> account=accountRepository.findByAccountCode(card.getAccCode());

		Optional<DemandeAction> olddemandeAction=demandeActionRepository.findProcessingRequestByCardNum(card.getCardNum());
		
		if(olddemandeAction.isPresent()) 
			return ResponseEntity.ok().body(gson.toJson("this card has already a request to validate!"));
		
		DemandeAction demadeAction = new DemandeAction(card.getCardNum(),4,2,card.getAgencyCode(),user.get().getUserName(),account.get().getAccountNum());

		
		demadeAction =demandeActionRepository.save(demadeAction);
		Periodicity periodicity = periodicityRepository.findByLibelle(rm.getGlPeriodicityType());
		RiskManagementDemande riskManagementDemande = new RiskManagementDemande(demadeAction.getDemandeCode(),rm.getGlMinAmount(),
				rm.getGlMaxAmount(), rm.getGlobalAmount(), rm.getGlobalNumber(),rm.getGlCurrencyAmount(),rm.getGlCurrencyNumber(),
				rm.getGlStartDate(), rm.getGlEndDate(), periodicity.getCode()
				
		);

		
		RiskManagementDemande  riskManagementDemandeToSave= riskManagementDemandeRepository.save(riskManagementDemande);
		logger.info(riskManagementDemandeToSave.toString());

		List<DetailsRequest> detailsRequests = rm.getDetailsRequestList();

		for (DetailsRequest dr : detailsRequests) {
			List<String> strings = dr.getTransactionCode();
			for (String s : strings) {
				EmvServiceValues serviceValues = emvServiceValuesRepository.findByLibelle(s);
				Periodicity periodicity1 = periodicityRepository.findByLibelle(dr.getDrTypePeriodicity());
				DetailRiskManagementDemande detailRiskManagementDemande = new DetailRiskManagementDemande (riskManagementDemandeToSave.getGlobalId(),
						dr.getDrMinAmount(),
						serviceValues.getCodeTransaction(),
						dr.getDrMaxAmount(), dr.getDrAmountLimit(), dr.getDrNumberLimit(), dr.getDrStartDate(),
						dr.getDrEndDate(), dr.getDrCurrencyAmount(), dr.getDrCurrencyNumber(),
						periodicity1.getCode());
				
				detailRiskManagementDemandeRepository.save(detailRiskManagementDemande);
				logger.info(detailRiskManagementDemande.toString());
			}
		}
		
//	if (rm.dailyRisk) 
//		createDailyRiskRequest(rm,demadeAction);
//	
	
	CardHistory cardHistory= new CardHistory();
	cardHistory.setCardCode(card.getCardCode());
	cardHistory.setOperation("Demande de changement des plafonds");
	cardHistory.setOperation_date(card.getModifDate());
	cardHistory.setEditedBy(user.get().getUserName());
	cardHistoryRepository.save(cardHistory);
		return ResponseEntity.ok().body(gson.toJson("RiskManagement request added successfully!"));}
		catch(Exception e) {
			e.printStackTrace();
			return ResponseEntity.ok().body(gson.toJson("RiskManagement request not added !"));
		}

	}
	
	
	
	@PutMapping("validateRiskManagementAction/{id}")
	public ResponseEntity<?> riskManagement(@PathVariable(value = "id") String demandeId) throws ResourceNotFoundException {
		String name = SecurityContextHolder.getContext().getAuthentication().getName();
		Optional<User> user = userRepository.findByUserNameOrUserEmail(name, name);
		if (!user.isPresent()) {
			throw new RuntimeException("Error saving risk !");
		}
		
		DemandeAction demandeAction = demandeActionRepository.findById(Integer.parseInt(demandeId)).get();
		
		Card card = cardRepository.findByCardNum(demandeAction.getCardNum())
				.orElseThrow(() -> new ResourceNotFoundException("Card not found for this N° :: " + demandeAction.getCardNum()));
		logger.info(card.toString());
		
		card.setModifDate(new Date());
		
		RiskManagementDemande rMDemande = riskManagementDemandeRepository.findByDemandeID(demandeAction.getDemandeCode());
		
		
		GlobalRiskManagement globalRiskManagement = globalRepository
				.findById(Integer.parseInt(card.getGlobalRiskCode())).get();
		Periodicity periodicity = periodicityRepository.findByCode(rMDemande.getPeriodicityType());
		
		RiskHistory riskHistory=new RiskHistory();
		riskHistory.setCardCode(card.getCardCode());
		riskHistory.setChangeRequestId(rMDemande.getDemandeID());
		riskHistory.setOperationDate(new Date());
		riskHistory.setEditedBy(user.get().getUserName());
		
		riskHistory.setGolbalAmount(globalRiskManagement.getGolbalAmount());
		riskHistory.setGlobalNumber(globalRiskManagement.getGlobalNumber());
		riskHistory.setMaxAmount(globalRiskManagement.getMaxAmount());
		riskHistory.setMinAmount(globalRiskManagement.getMinAmount());
		riskHistory.setPeriodicityType(globalRiskManagement.getPeriodicityType());
		riskHistory=riskHistoryRespository.save(riskHistory);
		
		globalRiskManagement.setGlEndDate(rMDemande.getGlEndDate());
		
		globalRiskManagement.setGlStartDate(rMDemande.getGlStartDate());
		globalRiskManagement.setGolbalAmount(rMDemande.getGolbalAmount());
		globalRiskManagement.setMaxAmount(rMDemande.getMaxAmount());
		globalRiskManagement.setMinAmount(rMDemande.getMinAmount());
		globalRiskManagement.setGlobalNumber(rMDemande.getGlobalNumber());
		globalRiskManagement.setPeriodicityType(periodicity.getCode());
		globalRepository.save(globalRiskManagement);
		logger.info(globalRiskManagement.toString());
		


		List<DetailRiskManagment> detailRiskManagments = detailRepository
				.findByGlobalCode(Integer.parseInt(card.getGlobalRiskCode()));
		
		for (DetailRiskManagment dr : detailRiskManagments) {
			DetailRiskHistory detailHistory=new DetailRiskHistory();
			detailHistory.setGlobalhistoryCode(riskHistory.getHistoryCode());
			detailHistory.setDrAmountLimit(dr.getDrAmountLimit());
			detailHistory.setDrNumberLimit(dr.getDrNumberLimit());
			detailHistory.setMaxAmount(dr.getMaxAmount());
			detailHistory.setMinAmount(dr.getMinAmount());
			detailHistory.setTransactionCode(dr.getTransactionCode());
			detailHistory.setTypePeriodicity(dr.getTypePeriodicity());
			
			detailRiskHistoryRepository.save(detailHistory);
			detailRepository.delete(dr);
		}
		List<DetailRiskManagementDemande> detailsRequests = detailRiskManagementDemandeRepository.findByGlobalDemandeCode(rMDemande.getGlobalId());

		for (DetailRiskManagementDemande dr : detailsRequests) {
		
				EmvServiceValues serviceValues = emvServiceValuesRepository.findByCodeTransaction(dr.getTransactionCode()).get();
				
				Periodicity periodicity1 = periodicityRepository.findByCode(dr.getTypePeriodicity());
				
				Optional<DetailRiskManagment>  oldRisk = detailRiskManagments.stream()
					    .filter(detail -> dr.getTransactionCode().equals(detail.getTransactionCode()))
					    .findFirst();

				DetailRiskManagment detailRiskManagment = new DetailRiskManagment(dr.getMinAmount(),
						serviceValues.getCodeTransaction(), Integer.parseInt(card.getGlobalRiskCode()),
						dr.getMaxAmount(), dr.getDrAmountLimit(), dr.getDrNumberLimit(), dr.getDrStartDate(),
						dr.getDrEndDate(),oldRisk.get().getDrCurrencyAmount(), oldRisk.get().getDrCurrencyNumber(), periodicity1.getCode());
				
				
				detailRepository.save(detailRiskManagment);
			
		}
		
		CardHistory cardHistory= new CardHistory();
		cardHistory.setCardCode(card.getCardCode());
		cardHistory.setOperation("Demande de changement des plafonds approuvée");
		cardHistory.setOperation_date(card.getModifDate());
		cardHistory.setEditedBy(user.get().getUserName());
		cardHistoryRepository.save(cardHistory);

		
		demandeAction.setDemandeStatusCode(1);
		demandeActionRepository.save(demandeAction);
		cardRepository.save(card);
		return ResponseEntity.ok().body(gson.toJson("RiskManagement updated successfully!"));

	}
	
	@PostMapping("/getCeilingHistoriesList")
	public Page<RiskHistory> getCeilingHistoriesList(
			@RequestParam(value = "id") Integer id,
			@RequestParam(name = "page", defaultValue = "0") int page,
			@RequestParam(name = "size", defaultValue = "10") int size,
		    @RequestParam(name = "sortOn") String sortOn,
		    @RequestParam(name = "dir") String dir)
			{
				
				Order order=null;
			  	if (dir.equals("desc"))
			  		order = new Order(Sort.Direction.DESC, sortOn);
			  	else 
			  		order = new Order(Sort.Direction.ASC, sortOn);
			  	
		return riskHistoryRespository.findByCardCode(PageRequest.of(page, size, Sort.by(order)),id);

	}
	
	@GetMapping("/getCeilingHistory/{id}")
	public ResponseEntity<RiskManegementHistoryDisplay> getCeilingHistory(@PathVariable(value = "id") Integer id)
			throws ResourceNotFoundException {
		
		Optional<RiskHistory> riskHistory = riskHistoryRespository.findByHistoryCode(id);

		//RiskManagementDemande globalRiskManagementRequest = riskHistoryRespository.findByDemandeID(Integer.parseInt(idDemande));
	   if (riskHistory.isPresent()) {
		   Periodicity periodicity = periodicityRepository.findById(riskHistory.get().getPeriodicityType()).get();
		   RiskManegementHistoryDisplay riskManagementRequest = new RiskManegementHistoryDisplay(
					riskHistory.get().getMinAmount(),
					riskHistory.get().getMaxAmount(), riskHistory.get().getGolbalAmount(),
					riskHistory.get().getGlobalNumber(), periodicity.getLibelle()); 
		   Optional<Card> card = cardRepository.findByCardCode(riskHistory.get().getCardCode());
		   if (card.isPresent()) {
			   Optional<Program> foundedProgram = programRepository
						.findByProgramCode(card.get().getProgrameId());
				if (foundedProgram.isPresent()) {
					riskManagementRequest.setCurrency(foundedProgram.get().getCurrency());
				}
				
		   }
		   
			List<DisplayDetailsHistory> detailsRequestList = new ArrayList<>();
			List<DetailRiskHistory>  details=detailRiskHistoryRepository.findByGlobalhistoryCode(riskHistory.get().getHistoryCode()); 
			
				
				for (DetailRiskHistory dr : details) {
					Periodicity periodicity1 = periodicityRepository.findById(dr.getTypePeriodicity()).get();

					DisplayDetailsHistory detailsRequest = new DisplayDetailsHistory(dr.getMinAmount(),
							dr.getTransactionCode(), dr.getMaxAmount(), dr.getDrAmountLimit(), dr.getDrNumberLimit(),
							
							periodicity1.getLibelle());

					detailsRequestList.add(detailsRequest);
				}
				riskManagementRequest.setDetailsRequestList(detailsRequestList);
				
				return ResponseEntity.ok().body(riskManagementRequest);
	   }
	   return null;

		
		
	}


	@GetMapping("/getRiskManagementRequest/{idDemande}")
	public ResponseEntity<RiskManegementDisplay> getRiskManagementRequest(@PathVariable(value = "idDemande") String idDemande)
			throws ResourceNotFoundException {
		
		
	
		RiskManagementDemande globalRiskManagementRequest = riskManagementDemandeRepository.findByDemandeID(Integer.parseInt(idDemande));
				
		logger.info(globalRiskManagementRequest.toString());
		Periodicity periodicity = periodicityRepository.findById(globalRiskManagementRequest.getPeriodicityType()).get();
		logger.info(periodicity.toString());
		RiskManegementDisplay riskManagementRequest = new RiskManegementDisplay(
				globalRiskManagementRequest.getGlobalId(), globalRiskManagementRequest.getMinAmount(),
				globalRiskManagementRequest.getMaxAmount(), globalRiskManagementRequest.getGolbalAmount(),
				globalRiskManagementRequest.getGlobalNumber(), globalRiskManagementRequest.getGlCurrencyAmount(),
				globalRiskManagementRequest.getGlCurrencyNumber(), globalRiskManagementRequest.getGlStartDate(),
				globalRiskManagementRequest.getGlEndDate(), periodicity.getLibelle()
				);
		logger.info(riskManagementRequest.toString());
		
		Optional<DemandeAction> demandeAction=demandeActionRepository.findById((Integer.parseInt(idDemande)));
		if (demandeAction.isPresent()) {
			Card card = cardRepository.findByCardNum(demandeAction.get().getCardNum())
					.orElseThrow(() -> new ResourceNotFoundException("Card not found for this N° :: " + demandeAction.get().getCardNum()));
			logger.info(card.toString());	
			Optional<Program> foundedProgram = programRepository
					.findByProgramCode(card.getProgrameId());
			if (foundedProgram.isPresent()) {
				riskManagementRequest.setCurrency(foundedProgram.get().getCurrency());
			}
			
		}
		
		List<DisplayDetailsManagement> detailsRequestList = new ArrayList<>();
		List<DetailRiskManagementDemande> detailRiskManagments = detailRiskManagementDemandeRepository.findByGlobalDemandeCode(globalRiskManagementRequest.getGlobalId());

		
		if (!detailRiskManagments.isEmpty()) {
					
			for (DetailRiskManagementDemande dr : detailRiskManagments) {
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
	}
	
	@PostMapping("getResetPinOfflineRequests")
	public Page<ResetPinOfflineRequestsDisplay> getResetPinOfflineRequests(@RequestBody ResetPinOfflineFilter filter,@RequestParam(name = "page", defaultValue = "0") int page,
			@RequestParam(name = "size", defaultValue = "10") int size, @RequestParam(name = "sortOn") String sortOn,
			@RequestParam(name = "dir") String dir) {
		Order order = null;
		if (dir.equals("desc"))
			order = new Order(Sort.Direction.DESC, sortOn);
		else
			order = new Order(Sort.Direction.ASC, sortOn);
		
		Page<DemandeResetPinOffline> all = demandeResetPinOfflineRepository.findAll(PageRequest.of(page, size, Sort.by(order)),
				filter.getPan());
		
		
		List<ResetPinOfflineRequestsDisplay> requestsDisplays = new ArrayList<ResetPinOfflineRequestsDisplay>();
		 
		for (DemandeResetPinOffline c : all) {
			ResetPinOfflineRequestsDisplay cd = new ResetPinOfflineRequestsDisplay();

			cd.setCardNumber(c.getCardNumber());
			cd.setAddedBy(c.getAddedBy());
			cd.setCreatedAt(c.getCreatedAt());
			cd.setModifiedAt(c.getModifiedAt());
			cd.setMotif(c.getMotif());
			cd.setStatus(c.getStatus());
			requestsDisplays.add(cd);

		}
		return new PageImpl<>(requestsDisplays, PageRequest.of(page, size), all.getTotalElements());

		
	
	}
	
	@PostMapping("addResetPinOfflineRequest")
	public ResponseEntity<String> getResetPinOfflineRequests(@RequestBody ResetPinOfflineRequestsDisplay request) {
		
		 String name = SecurityContextHolder.getContext().getAuthentication().getName();
			Optional<User> user = userRepository.findByUserNameOrUserEmail(name, name);
			if (!user.isPresent()) {
				throw new RuntimeException("Error saving status !");
			}
			
			Optional<Card> card = cardRepository.findByCardNum(request.getCardNumber());
			if (card.isPresent()) {
				Optional<DemandeResetPinOffline> oldRequest =demandeResetPinOfflineRepository.findDemandeResetPinOffline(request.getCardNumber());
				
				if (oldRequest.isPresent()) {
					
					return ResponseEntity.badRequest().body("REQUEST ALREADY EXIST");
				}

				
				DemandeResetPinOffline newRequest = new DemandeResetPinOffline();
				newRequest.setCardNumber(request.getCardNumber());
				newRequest.setAddedBy(user.get().getUserName());
				
				newRequest.setMotif(request.getMotif());
				newRequest.setStatus(1);
				
				demandeResetPinOfflineRepository.save(newRequest);
				return ResponseEntity.ok().body(gson.toJson("Request SAVED successfully "));

				
			}else {
				return ResponseEntity.badRequest().body("CARD NOT FOUND");

			}

		
	
	}
}
