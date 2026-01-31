package com.mss.backOffice.controller;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import com.mss.unified.entities.*;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.data.domain.Sort.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;

import com.mss.backOffice.exception.ResourceNotFoundException;
import com.mss.backOffice.request.ChargebackInterFilter;
import com.mss.backOffice.request.RequestChargebackVisaI;
import com.mss.backOffice.request.SendMailChargebackRecuVISA;
import com.mss.backOffice.request.UserPrincipal;
import com.mss.backOffice.request.VisaIncomingFilter;
import com.mss.backOffice.services.*;
import com.mss.unified.enumeration.ChargeBackType;
import com.mss.unified.enumeration.TypeDossier;
import com.mss.unified.repositories.*;
import com.mss.unified.services.StringTransformationService;
import com.google.common.base.Throwables;
import com.google.gson.Gson;


@RestController
@RequestMapping("File")
public class ClearingController {

	
	@Autowired
	private VisaIncomingRepository visaIncomingRepository;
	@Autowired
	private VisaSummaryRepository visaSummaryRepository;
	@Autowired
	private TransactionCodeVisaRepository transactionCodeVisaRepository;
	@Autowired
	public TransactionAtmRepository transAtmRep;
	@Autowired
	public TransactionRepository transactionRepository;
	@Autowired
	public UserRepository userRepository;
	@Autowired
	private AgenceAdministrationRepository agenceRepository;
	@Autowired
	private ChargebacksInternationalRepository chargebacksInternationalRepository;
	@Autowired
	public StatusChargebackRepository statusChargebackRepo;
	@Autowired
	ChargeBackHistoryInterRepository chargeBackHistoryInterRepository;
	@Autowired
	public TypeOperationCompRepository typeOperationRepo;
	@Autowired
	public DualChargeBackRepository dualChargeBackRepo;
	@Autowired
	public DualMessagePresentmentRepository dualMessagePresentmentRepository;
	@Autowired
	public ChargebackBankRepository chargebackBankRepository;
	@Autowired
	public ChargebackVisaRepository chargebackVisaRepository;
	@Autowired
	public AgenceAdministrationRepository agenceAdministrationRepository;
	@Autowired
	public MerchantRepository merchantRepo;
	@Autowired
	HistoriqueSendMailChargeBackInterRepository historiqueSendMailChargeBackInterRepo;
	@Autowired
	public MyEmailService myEmailService;
	private static final Logger logger = LoggerFactory.getLogger(ClearingController.class);
	private final int statusChargebackEnvoyeSnt=6;
	private static final Gson gson = new Gson();
	private static final String  typeVISA= "VISA";
	private static final List<String>  CC_EMAILS= Arrays.asList("","");

	
	@PostMapping("AllVisaIncomingDocs")
	public List<VisaIncoming> allVisaIncomingDocs(@RequestBody VisaIncomingFilter filter) {

//		Optional<VisaSummary> optionalSummary = visaSummaryRepository.findBydateCompensation(filter.getDate());
//		if (!optionalSummary.isPresent()) {
//			return Collections.emptyList();
//		}
		String cardNumber = filter.getCardNumber() != null ? filter.getCardNumber().trim() : null;
		String arn = filter.getArn() != null ? filter.getArn().trim() : null;
		String transactionId = filter.getTransactionId() != null ? filter.getTransactionId().trim() : null;

//		Integer matching = null;
//		if ("MATCHING AVEC ATM".equals(filter.getMatching())) {
//			matching = 1;
//		} else if ("MATCHING AVEC POS".equals(filter.getMatching())) {
//			matching = 2;
//		} else if ("NOT MATCHING".equals(filter.getMatching())) {
//			matching = 3;
//		}

		if ("XX".equals(filter.getTransactionCode())) {
			filter.setTransactionCode(null);
		}
		String startDate = filter.getDate();

		String endDate = filter.getEndDate();
		if (endDate == null || endDate.isEmpty() || "null".equalsIgnoreCase(endDate)) {
			endDate = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
		}

		List<Integer> codes = visaSummaryRepository.getCodeBySummaryDateBetween(startDate, endDate);

		//int codeSummary = optionalSummary.get().getSummaryCode();
		return visaIncomingRepository.VisaIncomingWithInterval(cardNumber, arn, transactionId,
				filter.getTransactionCode(), codes);

	}
	
	@PostMapping("AllVisaIncoming")
	public Page<VisaIncoming> allVisaIncoming(@RequestBody VisaIncomingFilter filter,
			@RequestParam(name = "page", defaultValue = "0") int page,
			@RequestParam(name = "size", defaultValue = "10") int size, @RequestParam(name = "sortOn") String sortOn,
			@RequestParam(name = "dir") String dir) {
		Sort.Direction direction = dir.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
		sortOn = sortOn != null ? sortOn.trim() : "incomingCode";
	    Sort sort = "incomingCode".equalsIgnoreCase(sortOn) 
	        ? Sort.by(new Order(direction, "incomingCode"))
	        : Sort.by(new Order(direction, sortOn), new Order(direction, "incomingCode"));

	    String cardNumber = filter.getCardNumber() != null ? filter.getCardNumber().trim() : null;
	    String arn = filter.getArn() != null ? filter.getArn().trim() : null;
	    String transactionId = filter.getTransactionId() != null ? filter.getTransactionId().trim() : null;
	    String transactionCode = "XX".equals(filter.getTransactionCode()) ? null : filter.getTransactionCode();

//	    Integer matching = null;
//	    if ("MATCHING AVEC ATM".equals(filter.getMatching())) {
//	        matching = 1;
//	    } else if ("MATCHING AVEC POS".equals(filter.getMatching())) {
//	        matching = 2;
//	    } else if ("NOT MATCHING".equals(filter.getMatching())) {
//	        matching = 3;
//	    }

	    String startDate = filter.getDate();
	    if (startDate == null || startDate.isEmpty()) {
	        return Page.empty();
	    }

	    String endDate = filter.getEndDate();
	    if (endDate == null || endDate.isEmpty() || "null".equalsIgnoreCase(endDate)) {
	        endDate = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
	    }

	    List<Integer> codes = visaSummaryRepository.getCodeBySummaryDateBetween(startDate, endDate);

	    if (!codes.isEmpty()) {
	        return visaIncomingRepository.visaIncoming(
	            PageRequest.of(page, size, sort),
	            cardNumber, arn, transactionId, transactionCode, codes
	        );
	    }

	    return Page.empty();
	}
	
	@GetMapping("allVisaSummary")
	public List<VisaSummary> getAllVisaSummary() {
		return visaSummaryRepository.findAll();

	}
	
	@GetMapping("getAllTransactionCodeVisa")
	public List<TransactionCodeVisa> getAllTransactionCodeVisa() {
		List<TransactionCodeVisa> listTransactionCodeVisa = transactionCodeVisaRepository.findAll();
		return listTransactionCodeVisa;

	}
	
	
	@GetMapping("GetAllValidTransactionFromVisaIncoming/{id}")
	public ResponseEntity<?> getAllValidTransactionFromVisaIncoming(@PathVariable int id) {
	    Optional<VisaIncoming> optionalVisaIncoming = visaIncomingRepository.findByIncomingCode(id);

	    if (!optionalVisaIncoming.isPresent()) {
	        return buildNotFoundResponse();
	    }

	    VisaIncoming visaIncoming = optionalVisaIncoming.get();
	    String matchingCode = visaIncoming.getMatchingCode();

	    if (matchingCode == null || matchingCode.length() < 2) {
	        return buildNotFoundResponse();
	    }

	    String type = matchingCode.substring(0, 1);
	    String codeStr = matchingCode.substring(1);

	    try {
	        int code = Integer.parseInt(codeStr);

	        switch(type) {
	            case "A":
	                TransactionValidAtm atmTransaction = transAtmRep.findByCode(code);
	                if (atmTransaction != null) {
	                    atmTransaction.setPan(StringTransformationService.getInstance().hideCardNumber(atmTransaction.getPan()));
	                    return ResponseEntity.ok(atmTransaction);
	                }
	                break;

	            case "P":
	                TransactionValidPos posTransaction = transactionRepository.findByCode(code);
	                if (posTransaction != null) {
	                    posTransaction.setPan(StringTransformationService.getInstance().hideCardNumber(posTransaction.getPan()));
	                    return ResponseEntity.ok(posTransaction);
	                }
	                break;
	                default:
	                	 return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid transaction type: " + type);
	        }

	    } catch (NumberFormatException e) {
	        logger.warn("Invalid code format: {} ", codeStr);
	        return buildNotFoundResponse();
	    }

	    return buildNotFoundResponse();
	}
	
	private ResponseEntity<Map<String, String>> buildNotFoundResponse() {
	    Map<String, String> response = new HashMap<>();
	    response.put("Message", "Transaction code not found");
	    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
	}
	
	
	@PostMapping("ListAllChargeBackEmis")
	public Page<ChargebacksInternational> findAllChargeBacKEmi(@RequestBody ChargebackInterFilter filter,
			@RequestParam(name = "page", defaultValue = "0") int page,
			@RequestParam(name = "size", defaultValue = "10") int size, @RequestParam(name = "sortOn") String sortOn,
			@RequestParam(name = "dir") String dir) {
		try {
			ToolsService.print("here", ToolsService.getLineNumber());
			sortOn = sortOn.replace("de02", "pan");
			Sort.Direction direction = dir.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
			Sort sort = Sort.by(new Sort.Order(direction, sortOn), new Sort.Order(direction, "code"));

			String name = SecurityContextHolder.getContext().getAuthentication().getName();

			User user = userRepository.findByUserNameOrUserEmail(name, name)
					.orElseThrow(() -> new ResourceNotFoundException("User not found for this id :: " + name));

			boolean requeteInitial = user.getUserType() == 5;

			List<String> codeAgence = new ArrayList<>();

			if (!requeteInitial) {
				AgenceAdministration agenceTrouve = agenceRepository.findByCodeAgence(user.getIdAgence()).orElseThrow(
						() -> new ResourceNotFoundException("pas d'agence pour l'utilisateur " + user.getUserCode()));

				codeAgence.add(agenceTrouve.getInitial());
			}

			String cardNumber = StringUtils.trimToEmpty(filter.getCardNumber());
			String ref = StringUtils.trimToNull(filter.getDe42());

			Integer codeSummary = resolveSummaryCode(filter);
			if (filter.getDate() != null && codeSummary == null)
				return Page.empty();

			boolean nullaccepted = false;
			List<Integer> types = new ArrayList<Integer>();
			if (filter.isInProgress()) {
				types.add(2);
			}
			if (filter.isDone()) {
				types.add(1);
			}
			if (filter.isAccepted()) {
				types.add(3);
			}
			if (filter.isNa()) {
				types.add(10);
				nullaccepted = true;
			}
			if (!filter.isInProgress() && !filter.isDone() && !filter.isAccepted() && !filter.isNa()) {
				types.add(2);
				types.add(1);
				types.add(3);
				nullaccepted = true;
			}

			if (requeteInitial && filter.getAgencyCode() != null && !filter.getAgencyCode().isEmpty()) {
				codeAgence.add(filter.getAgencyCode());
			}
			if (codeAgence.isEmpty())
				codeAgence = null;
			
			 Page<ChargebacksInternational> resultPage = chargebacksInternationalRepository.getAllLChargeBackEmiByFilter(
					PageRequest.of(page, size, sort), cardNumber, ref, codeSummary, types, nullaccepted, codeAgence,
					filter.getType());
			ToolsService.print("here", ToolsService.getLineNumber());

			if (resultPage != null) {
				resultPage.forEach(el ->  el.setPan(ToolsService.maskCardNumber(el.getPan())));
			}

			return resultPage;
		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		}

	}
	private Integer resolveSummaryCode(ChargebackInterFilter filter) {
		if (filter.getDate() == null || filter.getType() == null)
			return null;

		String date = filter.getDate().trim();
		String type = filter.getType().trim().toUpperCase();
		switch (type) {
//		case "MCD":
//			return ipmClearingSummaryRepository.findByDate(date).map(IpmClearingSummary::getImpsCode).orElse(null);
		case "VISA":
			VisaSummary vs = visaSummaryRepository.findByDate(date);
			return vs != null ? vs.getSummaryCode() : null;
		default:
			return null;
		}
	}
	
	@PostMapping("ListChargeBackEmisVISADocs")
	public List<VisaIncoming> findAllChargeBackEmiVISA(@RequestBody ChargebackInterFilter filter)
			throws ResourceNotFoundException {

		String name = SecurityContextHolder.getContext().getAuthentication().getName();

		User user = userRepository.findByUserNameOrUserEmail(name, name)
				.orElseThrow(() -> new ResourceNotFoundException("User not found for this id :: " + name));
		boolean requeteInitial = user.getUserType() == 5;

		List<String> codeAgence = new ArrayList<>();

		if (!requeteInitial) {
			AgenceAdministration agenceTrouve = agenceRepository.findByCodeAgence(user.getIdAgence()).orElseThrow(
					() -> new ResourceNotFoundException("pas d'agence pour l'utilisateur " + user.getUserCode()));
			codeAgence.add(agenceTrouve.getInitial());

		} else if (filter.getAgencyCode() != null && !filter.getAgencyCode().isEmpty()) {
			codeAgence.add(filter.getAgencyCode());
		}

		if (codeAgence.isEmpty())
			codeAgence = null;

		String cardNumber = StringUtils.trimToEmpty(filter.getCardNumber());
		String ref = StringUtils.trimToNull(filter.getDe42());

		Integer codeSummary = null;

		if (filter.getDate() != null && !filter.getDate().isEmpty()) {
			VisaSummary summary = visaSummaryRepository.findByDate(filter.getDate().trim());
			if (summary == null)
				return null;
			codeSummary = summary.getSummaryCode();
		}

		boolean nullaccepted = filter.isNa();
		List<String> types = new ArrayList<>();
		if (filter.isInProgress()) {
			types.add("2");
		}
		if (filter.isDone()) {
			types.add("1");
		}
		if (filter.isAccepted()) {
			types.add("3");
		}

		List<VisaIncoming> visaList = visaIncomingRepository.getAllLChargeBackEmiVIsaByAgency(cardNumber, ref,
				codeSummary, types, nullaccepted, codeAgence);

		if (visaList != null) {
			visaList.forEach(el -> el.setAccountNumber(ToolsService.maskCardNumber(el.getAccountNumber())));
		}
		return visaList;
	}
	
	@GetMapping("DisplayStatusChargeback")
	public List<StatusChargeback> displayStatusChargeback() {
		List<StatusChargeback> page1 = statusChargebackRepo.findAll();
		page1= page1.stream().filter(e->e.getStatusCode()!=statusChargebackEnvoyeSnt)
	            .collect(Collectors.toList());

		return page1;
	}
	
	@PostMapping("RequestChargebackVISA")
	public Page<VisaIncoming> requestChargebackVISA(@RequestBody ChargebackInterFilter filter,
			@RequestParam(name = "page", defaultValue = "0") int page,
			@RequestParam(name = "size", defaultValue = "10") int size, @RequestParam(name = "sortOn") String sortOn,
			@RequestParam(name = "dir") String dir) throws ResourceNotFoundException {

		Order order = new Order("desc".equalsIgnoreCase(dir) ? Sort.Direction.DESC : Sort.Direction.ASC, sortOn);

		String name = SecurityContextHolder.getContext().getAuthentication().getName();
		User user = userRepository.findByUserNameOrUserEmail(name, name)
				.orElseThrow(() -> new ResourceNotFoundException("User not found for this id :: " + name));

		boolean requeteInitial = user.getUserType() == 5;

		List<String> codeAgence = new ArrayList<>();

		if (!requeteInitial) {
			AgenceAdministration agenceTrouve = agenceRepository.findByCodeAgence(user.getIdAgence()).orElseThrow(
					() -> new ResourceNotFoundException("pas d'agence pour l'utilisateur " + user.getUserCode()));

			codeAgence.add(agenceTrouve.getInitial());
		}

		String cardNumber = StringUtils.trimToEmpty(filter.getCardNumber());
		String ref = StringUtils.trimToNull(filter.getDe42());
		String date = StringUtils.trimToNull(filter.getDate());

		Integer codeSummary = null;
		if (date != null) {
			VisaSummary summary = visaSummaryRepository.findByDate(date);
			if (summary == null)
				return null;
			codeSummary = summary.getSummaryCode();
		}

		if (requeteInitial && StringUtils.isNotBlank(filter.getAgencyCode())) {
			codeAgence.add(filter.getAgencyCode());
		}
		if (codeAgence.isEmpty())
			codeAgence = null;

		Page<VisaIncoming> pageResult = visaIncomingRepository.RequestChargebackVisaByAgencyCode(
				PageRequest.of(page, size, Sort.by(order)), ref, cardNumber, codeSummary, codeAgence);

		ToolsService.print("page size: " + pageResult.getSize(), ToolsService.getLineNumber());

		pageResult.forEach(el -> el.setAccountNumber(ToolsService.maskCardNumber(el.getAccountNumber())));

		return pageResult;

	}

	@PostMapping("RequestChargebackVISADocs")
	public List<VisaIncoming> requestChargebackVISA(@RequestBody ChargebackInterFilter filter)
			throws ResourceNotFoundException {

		String name = SecurityContextHolder.getContext().getAuthentication().getName();

		User user = userRepository.findByUserNameOrUserEmail(name, name)
				.orElseThrow(() -> new ResourceNotFoundException("User not found for this id :: " + name));
		boolean requeteInitial = user.getUserType() == 5;

		List<String> codeAgence = new ArrayList<>();

		if (!requeteInitial) {
			AgenceAdministration agenceTrouve = agenceRepository.findByCodeAgence(user.getIdAgence()).orElseThrow(
					() -> new ResourceNotFoundException("pas d'agence pour l'utilisateur " + user.getUserCode()));

			codeAgence.add(agenceTrouve.getInitial());
		}

		String cardNumber = StringUtils.trimToEmpty(filter.getCardNumber());
		String ref = StringUtils.trimToNull(filter.getDe42());
		String date = StringUtils.trimToNull(filter.getDate());

		Integer codeSummary = null;
		if (date != null) {
			VisaSummary summary = visaSummaryRepository.findByDate(date);
			if (summary == null)
				return null;
			codeSummary = summary.getSummaryCode();
		}

		if (requeteInitial && StringUtils.isNotBlank(filter.getAgencyCode())) {
			codeAgence.add(filter.getAgencyCode());
		}
		if (codeAgence.isEmpty())
			codeAgence = null;

		List<VisaIncoming> result = visaIncomingRepository.RequestChargebackVisaByAgencyCode(ref, cardNumber,
				codeSummary, codeAgence);

		if (result != null) {
			result.forEach(el -> el.setAccountNumber(ToolsService.maskCardNumber(el.getAccountNumber())));
		}

		return result;
	}
	
	@PutMapping("validateRequestChargebackVisa")
	public ResponseEntity<String> validateRequestChargebackVisa(
			@RequestBody List<RequestChargebackVisaI> rejectedList) {
		
		List<VisaIncoming> cpDetailsList = new ArrayList<>();
		List<ChargeBackHistoryInternational> chargeBackHistoryInternationals = new ArrayList<>();
		List<ChargebacksInternational> chargeBackInternationals = new ArrayList<>();
		rejectedList = rejectedList.stream().distinct().collect(Collectors.toList());

		for(RequestChargebackVisaI element : rejectedList) {
			Integer cp = element.getChargeback();

			Optional<VisaIncoming> visaOpt = visaIncomingRepository.findById(cp);
			if (!visaOpt.isPresent())
				continue;

			VisaIncoming visa = visaOpt.get();
			visa.setStatusChargeback("2");
			cpDetailsList.add(visa);

			String messageReason = element.getMessage().getMotif();

			Optional<VisaSummary> summaryOpt = visaSummaryRepository.findById(visa.getSummaryCode());
			if (!summaryOpt.isPresent())
				continue;
			VisaSummary summary = summaryOpt.get();
			String summaryYear = summary.getSummary_date().substring(2, 4);
			String transactionDate = summaryYear + visa.getPurchaseDate();

			ChargebacksInternational cb = new ChargebacksInternational(visa);
			//status 1 en cour de traitement
			//status 2  emis
			//status 3  accepter
			//status 4 rejeter
			cb.setStatusChargeback(2);
			cb.setAdditionalMessage(element.getAdditionalMessage());
			cb.setReasonCode(messageReason);
			cb.setChargeBackType(ChargeBackType.VISA);
			cb.setTypeDossier(TypeDossier.EMIS);
			cb.setTransactionDate(transactionDate);
			cb.setTreatmentDate(new Date());
			cb.setJourneeComptable(summary.getSummary_date());
			cb.setMontantOrigin(visa.getBaseAmount());
			cb.setCurrencyOrigin(visa.getBaseCurrency());
			cb.setArn(visa.getAcquirerReferenceNumber());
			chargeBackInternationals.add(cb);

			ChargeBackHistoryInternational history = new ChargeBackHistoryInternational(visa);
			history.setTransactionDate(transactionDate);
			history.setStatusChargeback(2);
			history.setAdditionalMessage(element.getAdditionalMessage());
			history.setChargeBackType(ChargeBackType.VISA);
			history.setTypeDossier(TypeDossier.EMIS);
			chargeBackHistoryInternationals.add(history);

		}
		ToolsService.print(cpDetailsList.size() + "", ToolsService.getLineNumber());
		visaIncomingRepository.saveAll(cpDetailsList);
		chargeBackHistoryInterRepository.saveAll(chargeBackHistoryInternationals);
		chargebacksInternationalRepository.saveAll(chargeBackInternationals);
		return ResponseEntity.ok().body(gson.toJson("ok"));

	}

	@GetMapping("DisplayOperationCode")
	public List<TypeOperationCompensation> displayOperationCode() {
		return typeOperationRepo.findAll();
	}

	@GetMapping("DisplayOperationCodeChargeBackHistory")
	public List<TypeOperationCompensation> displayOperationCodeChargeBackHistory() {
		return typeOperationRepo.findByOperationCode(Arrays.asList("05","08","05R","08R","06","15","17","18"));
	}

	@GetMapping("DualChargeBack")
	public Page<DualMessagePresentment> dualChargeBackMcd(@RequestParam(name = "page", defaultValue = "1") int page,
														  @RequestParam(name = "size", defaultValue = "10") int size,
														  @RequestParam(name = "type") String type) {
		
		return dualMessagePresentmentRepository.findByType(type, PageRequest.of(page, size));
	
	}
	
	@GetMapping("getTypes")
	public List<TypeOperationCompensation> getType(@RequestParam String category) {
		String operationCode1 = "";
		String operationCode2 = "";
		if (category.equals("chargeback")) {
			operationCode1 = "15";
			operationCode2 = "18";
		} else if (category.equals("request")) {
			operationCode1 = "01";
			operationCode2 = "04";
		}
	
		return typeOperationRepo.findTypes(operationCode1, operationCode2);
	}
	
	@GetMapping("BankName")
	public List<ChargebackBank> getBankNames() {
		return chargebackBankRepository.findAll();
	}
	
	@PutMapping("sendMailChargeBackRecuVisa")
	public ResponseEntity<?> sendMailChargeBackRecuVisa(@RequestBody SendMailChargebackRecuVISA rejected) {
		List<HistoriqueSendmailChargeBackInter> historiqueSendMailChargeBackInterList = new ArrayList<>();

		HashMap<String, String> agenceMails = new HashMap<>();
		Integer code = rejected.getCode();
		ChargebackVisa comp = chargebackVisaRepository.findById(code)
				.orElseThrow(() -> new RuntimeException("Chargeback VISA introuvable avec code: " + code));

		String summaryYear = visaSummaryRepository.findById(comp.getSummaryCode())
				.orElseThrow(() -> new RuntimeException("Résumé VISA introuvable")).getSummary_date().substring(2, 4);

		MessagePresentmentInter persentment = new MessagePresentmentInter();
		persentment.setMessage(rejected.getMessage());
		persentment.setTypeChargeback(typeVISA);
		persentment.setChargebackInter(code);
		comp.setMessageReject(String.valueOf(rejected.getCodeMotif()));
		Optional<Merchant> merchantOptional = merchantRepo.findByMerchantId(comp.getCardAcceptorId().trim());
		if (!merchantOptional.isPresent()) {
			throw new RuntimeException("Merchant introuvable pour ID: " + comp.getCardAcceptorId());
		}
		Merchant merchant = merchantOptional.get();
		String toAgence = agenceAdministrationRepository.findoneWithMerchant(merchant.getMerchantCode()).getEmail();

		HistoriqueSendmailChargeBackInter historiqueSendMailChargeBackInter = new HistoriqueSendmailChargeBackInter();

		String subject = "Information pour rejet " + comp.getAuthorisationCode();
		String message = buildMailVISA(comp, merchant, rejected, summaryYear);
		
		try {
			CC_EMAILS.add(getEmailUserConnected());
			sendEmailSafe(toAgence, subject, message, CC_EMAILS.toArray(new String[0]));
			agenceMails.put(comp.getCardAcceptorId(), toAgence);
			comp.setLastDateEnvoiMail(LocalDateTime.now());

		} catch (Exception e) {
			String stackTrace = Throwables.getStackTraceAsString(e);
			logger.error("Exception is=>{}", stackTrace);
			return ResponseEntity.badRequest().body(gson.toJson("Erreur lors de l’envoi du mail à l’agence."));
		}

			historiqueSendMailChargeBackInter = new HistoriqueSendmailChargeBackInter(comp.getCode(),
					LocalDateTime.now(), 3);
			historiqueSendMailChargeBackInter.setMailObject(subject);
			historiqueSendMailChargeBackInter.setMailContent(message);
			historiqueSendMailChargeBackInter.setAgencyMail(toAgence);
			historiqueSendMailChargeBackInter.setUserConnectedMail(getEmailUserConnected());
			historiqueSendMailChargeBackInter.setCcMails(CC_EMAILS);
		
		historiqueSendMailChargeBackInterList.add(historiqueSendMailChargeBackInter);
		List<HashMap<String, String>> listResult = new ArrayList<>();
		this.chargebackVisaRepository.save(comp);
		listResult.add(agenceMails);
		historiqueSendMailChargeBackInterRepo.saveAll(historiqueSendMailChargeBackInterList);
		return ResponseEntity.ok().body(listResult);
	}
	
	public static String getEmailUserConnected() {
		UserPrincipal userPrincipal = (UserPrincipal) SecurityContextHolder.getContext().getAuthentication()
				.getPrincipal();
		String emailConnected = userPrincipal.getEmail();
		return emailConnected != null && !emailConnected.isEmpty() ? emailConnected : null;
	}

	private String buildMailVISA(ChargebackVisa comp, Merchant merchant, SendMailChargebackRecuVISA rejected, String summaryYear) {
		DecimalFormat df = new DecimalFormat("0.00");
		String date = new SimpleDateFormat("dd/MM/yy").format(new Date());
		return "<div>\n" + "        <h4>WIFAK BANK</h4>\n"
				+ "        <p style=\"color: #555; text-align: right;\">Tunis le : " + date + "</p>\n"
				+ "    </div>\n" + "\n" + "    <div>\n"
				+ "        <p><strong>Direction Monétique et Marketing</strong></p>\n"
				+ "        <p>Nous vous demandons de bien vouloir envoyer cette lettre à votre client dès réception</p>\n"
				+ "\n" + "        <div>\n" + "            <p><strong>Affiliation :</strong> "
				+ comp.getCardAcceptorId() + "</p>\n" + "            <p><strong>Client :</strong> "
				+ merchant.getMerchantLibelle() + "</p>\n"
				+ "            <p><strong>Objet :</strong> Information pour rejet</p>\n" + "        </div>\n"
				+ "\n"
				+ "        <p>Messieurs, Nous vous informons que la(es) transactions(s) décrite(s) ci-dessous fait l'objet d'un rejet reçu de la banque du porteur pour le motif suivant : <strong>"
				+ rejected.getMessage() + ": " + df.format(comp.getBaseAmount()) + " " + rejected.getAdditionalMessage()
				+ " </strong></p>\n" + "\n" + "        <div>\n"
				+ "            <p><strong>Date de la transaction :</strong>" + "20" + summaryYear + "-" + comp.getPurchaseDate().substring(0,2)
				+ "-" + comp.getPurchaseDate().substring(2)
				+ "</p>\n" + "            <p><strong>Date journée Comptable :</strong>"
				+ (comp.getProcessingDate() == null ? "" : comp.getProcessingDate()) + "</p>\n"
				+ "            <p><strong>Numéro de la Transaction :</strong></p>\n" + 
				"            <p><strong>Code Autorisation :</strong>" + comp.getAuthorisationCode()
				+ "</p>\n" + "            <p><strong>Montant de la Transaction :</strong>"
				+ (comp.getTransactionAmount() != null ? df.format(comp.getTransactionAmount()) : df.format(comp.getBaseAmount())) + "</p>\n" + "            <p><strong>Montant de Rejet :</strong> "
				+ df.format(comp.getBaseAmount()) + "</p>\n"
				+ "            <p><strong>Numéro de la Carte :</strong>"
				+ ToolsService.maskCardNumber(comp.getAccountNumber()) + "</p>\n" + "        </div>\n" + "\n"
				+ "        <p>Si le rejet est bien fondé nous serons dans l'obligation de débiter votre compte du montant du rejet.</p>\n"
				+ "\n"
				+ "        <p>Veuillez agréer, Messieurs, l'expression de nos salutations les meilleurs.</p>\n"
				+ "    </div>\n" + "\n" + "    <div>\n" + "        <p>BANQUE DE TUNISIE</p>\n" + "    </div>";
	}
	private boolean sendEmailSafe(String to, String subject, String content, String... cc) throws Exception {
	    try {
	        if (to != null && !to.trim().isEmpty()) {
	            myEmailService.sendOtpMessage(to.trim(), subject, content, cc);
	            return true;
	        }
	    } catch (Exception e) {
	        logger.error("Erreur lors de l'envoi de mail à {} : {}", to, Throwables.getStackTraceAsString(e));
	        if(cc.length == 0) {
	        	throw e;
	        }
	    }
	    return false;
	}
	
	@GetMapping("ChargebackHistoryInter")
	public Page<ChargeBackHistoryInternational> findChargeBackHistoryPage(
			@RequestParam(name = "page", defaultValue = "0") int page,
			@RequestParam(name = "size", defaultValue = "10") int size, @RequestParam(name = "sortOn") String sortOn,
			@RequestParam(name = "dir") String dir, @RequestParam String rrn, @RequestParam String cardNum,
			@RequestParam String numAuto, @RequestParam String dateTran, @RequestParam String merchantId,
			@RequestParam Integer status, @RequestParam String type) {

		rrn = normalizeParam(rrn);
		cardNum = normalizeParam(cardNum);
		numAuto = normalizeParam(numAuto);
		dateTran = normalizeParam(dateTran);
		merchantId = normalizeParam(merchantId);

		ChargeBackType chargeBackType = null;

		//if (typeVISA.equals(type)) {
			chargeBackType = ChargeBackType.VISA;
//		} else if (typeMCD.equals(type)) {
//			chargeBackType = ChargeBackType.MCD;
//		}

		Sort.Direction direction = dir.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
		Sort sort = Sort.by(new Sort.Order(direction, sortOn));

		Page<ChargeBackHistoryInternational> page1 = null;
		page1 = chargeBackHistoryInterRepository.getHistory(PageRequest.of(page, size, sort), cardNum.trim(),
				numAuto.trim(), dateTran.trim(), merchantId, rrn, status, chargeBackType);

		page1.forEach(element -> {
			element.setPan(ToolsService.maskCardNumber(element.getPan()));
		});
		return page1;

	}
	private String normalizeParam(String param) {
	    return (param != null && !"null".equalsIgnoreCase(param)) ? param.trim() : "";
	}
	
	@GetMapping("getAllChargeBackStatus")
	public List<StatusChargeback> getAllChargeBackStatus(HttpServletRequest request) throws ResourceNotFoundException {
		String name = SecurityContextHolder.getContext().getAuthentication().getName();

		User user = userRepository.findByUserNameOrUserEmail(name, name)
				.orElseThrow(() -> new ResourceNotFoundException("User not found for this id :: " + name));
		boolean requeteInitial = user.getUserType() == 5;

		List<StatusChargeback> statut = this.statusChargebackRepo.findAll()
				.stream()
				.filter(e -> e.getStatusCode() != statusChargebackEnvoyeSnt)
				.collect(Collectors.toList());
		
		if (!requeteInitial) {
			statut = statut.stream().filter(e -> e.getStatusCode() != statusChargebackEnvoyeSnt)
					.collect(Collectors.toMap(StatusChargeback::getLibelleBanque, // Critère de filtrage
							Function.identity(), // Utiliser l'objet entier
							(existing, replacement) -> existing)) // Si doublon, garder le premier
					.values().stream().collect(Collectors.toList());
		}

		return statut;
	}
	
	
}
