package com.mss.backOffice.controller;

import com.google.common.base.Throwables;
import com.google.gson.Gson;
import com.mss.backOffice.exception.ResourceNotFoundException;
import com.mss.backOffice.request.AcceptCbEmisInter;
import com.mss.backOffice.request.ChargebackInterFilter;
import com.mss.backOffice.request.FiltredVisaOutgoingRecherche;
import com.mss.backOffice.services.ChargeBackService;
import com.mss.backOffice.services.ToolsService;
import com.mss.unified.entities.*;
import com.mss.unified.enumeration.TypeDossier;
import com.mss.unified.repositories.*;
import com.mss.unified.services.StringTransformationService;
import org.apache.commons.beanutils.PropertyUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.bind.annotation.*;

import javax.transaction.Transactional;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("chargeBackInternational/")
public class ChargeBackInternationalController {

	@Autowired
	VisaDisputeStatusAdviceRepository visaDisputeStatusAdviceRepository;

	@Autowired
	VisaIncomingRepository visaIncomingRepository;
	@Autowired
	private ChargeBackHistoryInterRepository chargeBackHistoryInterRepository;
	@Autowired
	private VisaSummaryRepository visaSummaryRepository;
	@Autowired
	private ChargeBackVisaOutgoingRepository chargeBackVisaOutgoingRepository;
	@Autowired
	public ChargebackVisaRepository chargebackVisaRepository;
	private static final Gson gson = new Gson();
	private static final Logger logger = LoggerFactory.getLogger(ChargeBackInternationalController.class);
	@Autowired
	private ChargebacksInternationalRepository chargebacksInternationalRepository;
	@Autowired
	private TransactionTemplate transactionTemplate;
	@Autowired
	private ChargeBackService chargeBackService;
	@Autowired
	private DualMessagePresentmentRepository dualMessagePresentmentRepository;

	@PutMapping("acceptedChargeBackVISAEmis")
	public ResponseEntity<String> acceptedChargeBackVISAEmis(@RequestBody List<AcceptCbEmisInter> visaAccepter)
			throws ResourceNotFoundException {
		List<ChargebacksInternational> visaList = new ArrayList<>();
		List<ChargeBackHistoryInternational> chargeBackHistoryList = new ArrayList<>();
		List<ChargebacksInternational> visaUpdate = new ArrayList<>();
		for (AcceptCbEmisInter visa : visaAccepter) {

			Optional<ChargebacksInternational> chargebackAccepted = chargebacksInternationalRepository
					.findById(visa.getCp_code());

			if (!chargebackAccepted.isPresent()) {
				throw new ResourceNotFoundException("VISA not found : " + visa.getCp_code());
			}
			chargebackAccepted.get().setStatusChargeback(3);
			chargebackAccepted.get().setAdditionalMessage(visa.getAdditionalMessage());
			chargebackAccepted.get().setTypeDossier(TypeDossier.EMIS);
			visaUpdate.add(chargebackAccepted.get());

			visaList.add(chargebackAccepted.get());

		}
		ToolsService.print("start proces", ToolsService.getLineNumber());
		try {
			// TODO: FIX this method
			chargeBackService.AppurementchargebackEmisVISA(visaList);
			chargebacksInternationalRepository.saveAll(visaUpdate);
			chargeBackHistoryList = copyChargebacksToHistory(visaUpdate);
			chargeBackHistoryInterRepository.saveAll(chargeBackHistoryList);
		} catch (Exception e) {
			return ResponseEntity.badRequest().body(gson.toJson("error 1"));
		}
		return ResponseEntity.ok().body(gson.toJson("ok"));
	}

	@PostMapping("getRepresentationVisaRecu")
	public Page<VisaDisputeStatusAdvice> getRepresentationVisaRecu(@RequestBody Map<String, String> filters,
			@RequestParam(name = "page", defaultValue = "0") int page,
			@RequestParam(name = "size", defaultValue = "10") int size, @RequestParam(name = "sortOn") String sortOn,
			@RequestParam(name = "dir") String dir) {
		Order order = null;
		Order order2 = null;
		if (dir.equals("desc")) {
			order = new Order(Sort.Direction.DESC, sortOn);
			order2 = new Order(Sort.Direction.DESC, "incomingCode");
		} else {
			order2 = new Order(Sort.Direction.ASC, "incomingCode");
			order = new Order(Sort.Direction.ASC, sortOn);
		}

		return visaDisputeStatusAdviceRepository.findRepresentationVisaRecu(
				PageRequest.of(page, size, Sort.by(order, order2)), filters.get("authorizationCode"),
				filters.get("acquirerReferenceNumber"));

	}

	public List<ChargeBackHistoryInternational> copyChargebacksToHistory(List<ChargebacksInternational> chargebacks) {
		try {
			// Optional delay if needed
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			String stackTrace = Throwables.getStackTraceAsString(e);
			logger.info("Exception during delay: {}", stackTrace);
		}

		// Get all chargebacks
		logger.info("Found {} chargebacks to copy to history", chargebacks.size());

		// Convert each to history record
		return chargebacks.stream().map(chargeback -> {
			ChargeBackHistoryInternational history = new ChargeBackHistoryInternational();
			try {
				// Manually copy all fields except the ones with type mismatches
				// Copy primitive types
				history.setCode(chargeback.getCode());
				history.setSummaryCodeVisa(chargeback.getSummaryCode());
				history.setSummaryCodeMCD(chargeback.getSummaryCode());
				history.setAmountTransaction(chargeback.getAmountTransaction());
				history.setSettlementAmount(chargeback.getSettlementAmount());
				history.setSourceAmount(chargeback.getSourceAmount());
				history.setStatusChargeback(chargeback.getStatusChargeback());

				// Copy String fields
				history.setPan(chargeback.getPan());
				history.setTransactionCurrency(chargeback.getTransactionCurrency());
				history.setSettlementCurrency(chargeback.getSettlementCurrency());
				history.setRrn(chargeback.getRrn());
				history.setArn(chargeback.getArn());
				history.setTransactionDate(chargeback.getTransactionDate());
				history.setAuthCode(chargeback.getAuthCode());
				history.setMerchantId(chargeback.getMerchantId());
				history.setMerchantName(chargeback.getMerchantName());
				history.setReasonCode(chargeback.getReasonCode());
				history.setMessageReject(chargeback.getMessageReject());

				// Copy Object fields
				history.setInterchange(chargeback.getInterchange());
				history.setChargeBackType(chargeback.getChargeBackType());
				history.setAdditionalMessage(chargeback.getAdditionalMessage());
				history.setTypeDossier(chargeback.getTypeDossier());

				// Handle type differences
				if (chargeback.getOperation() != null) {
					try {
						history.setOperation(Integer.parseInt(chargeback.getOperation()));
					} catch (NumberFormatException ex) {
						history.setOperation(0);
						logger.warn("Failed to parse operation value: {}", chargeback.getOperation());
					}
				}

				if (chargeback.getSourceCurrency() != null) {
					history.setSourceCurrency(chargeback.getSourceCurrency().doubleValue());
				}

				// Set treatment date to current date
				history.setTreatmentDate(new Date());

			} catch (Exception ex) {
				String stackTrace = Throwables.getStackTraceAsString(ex);
				logger.error("Exception during property copying: {}", stackTrace);
			}
			return history;
		}).collect(Collectors.toList());
	}

	@PostMapping("getRepresentationVisaRecuDocs")
	public List<VisaDisputeStatusAdvice> getRepresentationVisaRecuDocs(@RequestBody Map<String, String> filters) {
		return visaDisputeStatusAdviceRepository.findRepresentationVisaRecu(filters.get("authorizationCode"),
				filters.get("acquirerReferenceNumber"));

	}

	@PostMapping("getRepresentationVisaEmisOutGoing")
	public Page<VisaOutgoingConfig> getRepresentationVisaEmisOutGoing(
			@RequestBody FiltredVisaOutgoingRecherche filtredVisaOutgoingRecherche,
			@RequestParam(name = "page", defaultValue = "0") int page,
			@RequestParam(name = "size", defaultValue = "10") int size, @RequestParam(name = "sortOn") String sortOn,
			@RequestParam(name = "dir") String dir) {

		Page<VisaOutgoingConfig> result = null;

		String pan = filtredVisaOutgoingRecherche.getPan() != null ? filtredVisaOutgoingRecherche.getPan().trim()
				: null;
		String refNumber = filtredVisaOutgoingRecherche.getRetrRefNumber() != null
				? filtredVisaOutgoingRecherche.getRetrRefNumber().trim()
				: null;
		String transactionDate = filtredVisaOutgoingRecherche.getTransactionDate() != null
				? filtredVisaOutgoingRecherche.getTransactionDate()
				: null;

		Optional<VisaSummary> summaryDate = visaSummaryRepository
				.findBydateCompensation(filtredVisaOutgoingRecherche.getDateCompensation());
		Integer summary = summaryDate.isPresent() ? summaryDate.get().getSummaryCode() : null;
		result = chargeBackVisaOutgoingRepository.getVisaOutgoingRepresentationEmis(PageRequest.of(page, size), summary,
				pan, refNumber, transactionDate);

		for (VisaOutgoingConfig element : result) {
			element.setCardNumber(StringTransformationService.getInstance().hideCardNumber(element.getCardNumber()));
		}
		return result;

	}

	@PostMapping("getRepresentationVisaEmisTc33")
	public Page<VisaDisputeStatusAdvice> getRepresentationVisaEmisTc33(@RequestBody Map<String, String> filters,
			@RequestParam(name = "page", defaultValue = "0") int page,
			@RequestParam(name = "size", defaultValue = "10") int size, @RequestParam(name = "sortOn") String sortOn,
			@RequestParam(name = "dir") String dir) {
		Order order = null;
		Order order2 = null;
		if (dir.equals("desc")) {
			order = new Order(Sort.Direction.DESC, sortOn);
			order2 = new Order(Sort.Direction.DESC, "incomingCode");
		} else {
			order2 = new Order(Sort.Direction.ASC, "incomingCode");
			order = new Order(Sort.Direction.ASC, sortOn);
		}

		return visaDisputeStatusAdviceRepository.findRepresentationVisaEmis(
				PageRequest.of(page, size, Sort.by(order, order2)), filters.get("authorizationCode"),
				filters.get("acquirerReferenceNumber"));

	}

	@PostMapping("getRepresentationVisaEmisTc33Docs")
	public List<VisaDisputeStatusAdvice> getRepresentationVisaEmisTc33Docs(@RequestBody Map<String, String> filters) {

		return visaDisputeStatusAdviceRepository.findRepresentationVisaEmis(filters.get("authorizationCode"),
				filters.get("acquirerReferenceNumber"));

	}

	@PostMapping("getCbVisaRecusTreated")
	public Page<VisaDisputeStatusAdvice> getCbVisaRecusTreated(@RequestBody Map<String, String> filters,
			@RequestParam(name = "page", defaultValue = "0") int page,
			@RequestParam(name = "size", defaultValue = "10") int size, @RequestParam(name = "sortOn") String sortOn,
			@RequestParam(name = "dir") String dir) {
		Order order = null;
		Order order2 = null;
		if (dir.equals("desc")) {
			order = new Order(Sort.Direction.DESC, sortOn);
			order2 = new Order(Sort.Direction.DESC, "incomingCode");
		} else {
			order2 = new Order(Sort.Direction.ASC, "incomingCode");
			order = new Order(Sort.Direction.ASC, sortOn);
		}
		List<Integer> status = filters.get("accepted") != null ? new ArrayList<>(Arrays.asList(3))
				: filters.get("rejected") != null ? new ArrayList<>(Arrays.asList(4))
						: new ArrayList<>(Arrays.asList(3, 4));

		return visaDisputeStatusAdviceRepository.findCbVisaRecuTreated(
				PageRequest.of(page, size, Sort.by(order, order2)), filters.get("authorizationCode"),
				filters.get("acquirerReferenceNumber"), status);

	}

	@Transactional
	@PutMapping("acceptedChargeBackVISARecuOutgoing")
	public ResponseEntity<String> acceptedChargeBackVISARecuOutgoing(
			@RequestBody List<ChargeBackVisaOutgoing> visaOutgoingConfig) {

		ToolsService.print("start proces Appurement", ToolsService.getLineNumber());
		try {
			if (!visaOutgoingConfig.isEmpty())
				//TODO : Fix this Method
				chargeBackService.AppurementchargebackRecuVisaOutgoingAtm(new ArrayList<>(), new ArrayList<>(),
						visaOutgoingConfig);
		} catch (Exception e) {
			return ResponseEntity.badRequest().body(gson.toJson("error Dans Appurement ChargeBackRecuOutgoing"));
		}

		return ResponseEntity.ok().body(gson.toJson("ok"));
	}

	@PostMapping("chargeBackRecuVISATraited")
	public Page<ChargebackVisa> chargeBackRecuVISATraited(@RequestBody ChargebackInterFilter filter,
			@RequestParam(name = "page", defaultValue = "0") int page,
			@RequestParam(name = "size", defaultValue = "10") int size, @RequestParam(name = "sortOn") String sortOn,
			@RequestParam(name = "dir") String dir) {

		Integer codeSummary = null;
		String cardNumber = filter.getCardNumber() != null ? filter.getCardNumber().trim() : null;
		String ref = filter.getDe42() != null ? filter.getDe42() : null;
		String date = filter.getDate() != null ? filter.getDate() : null;
		List<Integer> status = filter.isAccepted() ? new ArrayList<>(Arrays.asList(3))
				: filter.isRejected() ? new ArrayList<>(Arrays.asList(4)) : new ArrayList<>(Arrays.asList(3, 4));

		VisaSummary summary = visaSummaryRepository.findByDate(date);
		if (summary != null)
			codeSummary = summary.getSummaryCode();
		return chargebackVisaRepository.chargebackRecuVisaTreated(PageRequest.of(page, size), cardNumber, ref,
				codeSummary, status);
	}

	@PostMapping("findChargeBackVisaOutGoingTraited")
	public Page<VisaOutgoingConfig> filtredChargeBackVisaOutGoingTraited(
			@RequestBody FiltredVisaOutgoingRecherche filter, @RequestParam(name = "page", defaultValue = "0") int page,
			@RequestParam(name = "size", defaultValue = "10") int size, @RequestParam(name = "sortOn") String sortOn,
			@RequestParam(name = "dir") String dir) {

		Page<VisaOutgoingConfig> result = null;

		String pan = filter.getPan() != null ? filter.getPan().trim() : null;
		String refNumber = filter.getRetrRefNumber() != null ? filter.getRetrRefNumber().trim() : null;
		String transactionDate = filter.getTransactionDate() != null ? filter.getTransactionDate() : null;
		List<Integer> status = filter.isAccepted() ? new ArrayList<>(Arrays.asList(3))
				: filter.isRejected() ? new ArrayList<>(Arrays.asList(4)) : new ArrayList<>(Arrays.asList(3, 4));

		Optional<VisaSummary> summaryDate = visaSummaryRepository.findBydateCompensation(filter.getDateCompensation());
		Integer summary = summaryDate.isPresent() ? summaryDate.get().getSummaryCode() : null;
		result = chargeBackVisaOutgoingRepository.getVisaOutgoingTreated(PageRequest.of(page, size), summary, pan,
				refNumber, transactionDate, status);

		for (VisaOutgoingConfig element : result) {
			element.setCardNumber(StringTransformationService.getInstance().hideCardNumber(element.getCardNumber()));
		}
		return result;

	}
	
	@Transactional
	@PutMapping("acceptedChargeBackRecuVISARetrait")
	public ResponseEntity<String> acceptedChargeBackRecuVISARetrait(@RequestBody List<ChargebackVisa> chargebackVisa)
			throws InterruptedException, ResourceNotFoundException {
		List<ChargebackVisa> visaList = new ArrayList<>();
		List<ChargeBackHistoryInternational> chargeBackHistoryList = new ArrayList<>();
		List<ChargebackVisa> visaUpdate = new ArrayList<>();

		for (ChargebackVisa visa : chargebackVisa) {

			Optional<ChargebackVisa> chargebackAccepted = chargebackVisaRepository.findById(visa.getCode());
			if (!chargebackAccepted.isPresent()) {
				throw new ResourceNotFoundException("chargebackVisa not found : " + visa.getCode());
			}
			chargebackAccepted.get().setStatusChargeback(visa.getStatusChargeback());
			visaUpdate.add(chargebackAccepted.get());
			if (visa.getStatusChargeback() == 3)
				visaList.add(chargebackAccepted.get());
			ChargeBackHistoryInternational chargebackHistory = new ChargeBackHistoryInternational(
					chargebackAccepted.get());
			chargebackHistory.setStatusChargeback(visa.getStatusChargeback());
			chargebackHistory.setTypeDossier(TypeDossier.RECU);
			chargeBackHistoryList.add(chargebackHistory);
		}

		ToolsService.print("start proces", ToolsService.getLineNumber());
		try {
			if (!visaList.isEmpty())
				//TODO : fix this
				chargeBackService.AppurementchargebackRecuVisa(new ArrayList<>(), visaList, new ArrayList<>());
		} catch (Exception e) {
			return ResponseEntity.badRequest().body(gson.toJson("Erreur dans Appurement de chargeback"));
		}

		try {
			chargebackVisaRepository.saveAll(visaUpdate);
		} catch (Exception e) {
			return ResponseEntity.badRequest().body(gson.toJson("error 2"));
		}
		try {
			chargeBackHistoryInterRepository.saveAll(chargeBackHistoryList);
		} catch (Exception e) {
			return ResponseEntity.badRequest().body(gson.toJson("error 3"));
		}
		return ResponseEntity.ok().body(gson.toJson("ok"));

	}
	
	@Transactional
	@PutMapping("acceptedChargeBackVISARecuTc33")
	public ResponseEntity<String> acceptedChargeBackVISARecuTc33(
			@RequestBody List<VisaDisputeStatusAdvice> visaDisputeStatusAdvice)
			throws ResourceNotFoundException {
		List<VisaDisputeStatusAdvice> visaList = new ArrayList<>();
		List<ChargeBackHistoryInternational> chargeBackHistoryList = new ArrayList<>();
		List<VisaDisputeStatusAdvice> visaUpdate = new ArrayList<>();
		for (VisaDisputeStatusAdvice visa : visaDisputeStatusAdvice) {

			VisaDisputeStatusAdvice chargebackAccepted = visaDisputeStatusAdviceRepository
					.findById(visa.getIncomingCode()).orElseThrow(() -> new ResourceNotFoundException("VisaDisputeStatusAdvice not found : " + visa.getIncomingCode()));


			chargebackAccepted.setStatusChargeback(visa.getStatusChargeback());
			chargebackAccepted.setExcedentType( visa.getExcedentType() == null ? "ExcedentPhysique" : visa.getExcedentType());
			visaUpdate.add(chargebackAccepted);

			ChargeBackHistoryInternational chargebackHistory = new ChargeBackHistoryInternational(
					chargebackAccepted);
			if (visa.getStatusChargeback() == 3) {
				chargebackHistory.setStatusChargeback(3);
				chargebackHistory.setTypeDossier(TypeDossier.RECU);
				visaList.add(chargebackAccepted);
			} else {
				chargebackHistory.setStatusChargeback(4);
				chargebackHistory.setTypeDossier(TypeDossier.RECU);
			}
			chargeBackHistoryList.add(chargebackHistory);
		}

		ToolsService.print("start process", ToolsService.getLineNumber());
		try {
			if (!visaList.isEmpty()) {
				//TODO : fix this
				//executorThreadChargeback.AppurementchargebackRecuVisa(visaList, new ArrayList<>(), new ArrayList<>());
			}
			visaDisputeStatusAdviceRepository.saveAll(visaUpdate);
			chargeBackHistoryInterRepository.saveAll(chargeBackHistoryList);
		} catch (Exception e) {
			return ResponseEntity.badRequest().body(gson.toJson("error 1"));
		}

		return ResponseEntity.ok().body(gson.toJson("ok"));
	}
	
	@PostMapping("findChargeBackVisaOutGoing")
	public Page<VisaOutgoingConfig> filtredChargeBackVisaOutGoing(
			@RequestBody FiltredVisaOutgoingRecherche filtredVisaOutgoingRecherche,
			@RequestParam(name = "page", defaultValue = "0") int page,
			@RequestParam(name = "size", defaultValue = "10") int size, @RequestParam(name = "sortOn") String sortOn,
			@RequestParam(name = "dir") String dir) {

		Page<VisaOutgoingConfig> result = null;

		String pan = filtredVisaOutgoingRecherche.getPan() != null ? filtredVisaOutgoingRecherche.getPan().trim()
				: null;
		String refNumber = filtredVisaOutgoingRecherche.getRetrRefNumber() != null
				? filtredVisaOutgoingRecherche.getRetrRefNumber().trim()
				: null;
		String transactionDate = filtredVisaOutgoingRecherche.getTransactionDate() != null
				? filtredVisaOutgoingRecherche.getTransactionDate()
				: null;

		Optional<VisaSummary> summaryDate = visaSummaryRepository
				.findBydateCompensation(filtredVisaOutgoingRecherche.getDateCompensation());
		Integer summary = summaryDate.isPresent() ? summaryDate.get().getSummaryCode() : null;
		result = chargeBackVisaOutgoingRepository.getVisaOutgoing(PageRequest.of(page, size), summary, pan, refNumber,
				transactionDate);

		for (VisaOutgoingConfig element : result) {
			element.setCardNumber(StringTransformationService.getInstance().hideCardNumber(element.getCardNumber()));
		}
		return result;

	}

	@PostMapping("findChargeBackVisaOutGoingDoc")
	public List<VisaOutgoingConfig> filtredChargeBackVisaOutGoingDoc(
			@RequestBody FiltredVisaOutgoingRecherche filtredVisaOutgoingRecherche) {
		int summary = 0;
		Order order = null;

		String responseMessageType = null;
		String settlementDate = null;

		Integer matching = 0;
		List<VisaOutgoingConfig> result = null;

		String pan = filtredVisaOutgoingRecherche.getPan() != null ? filtredVisaOutgoingRecherche.getPan().trim()
				: null;
		String refNumber = filtredVisaOutgoingRecherche.getRetrRefNumber() != null
				? filtredVisaOutgoingRecherche.getRetrRefNumber().trim()
				: null;
		String transactionDate = filtredVisaOutgoingRecherche.getTransactionDate() != null
				? filtredVisaOutgoingRecherche.getTransactionDate()
				: null;

		if (filtredVisaOutgoingRecherche.getTransactionDate() != null) {
			transactionDate = filtredVisaOutgoingRecherche.getTransactionDate();
		}
		if (filtredVisaOutgoingRecherche.getResponseMessageType() != null) {
			responseMessageType = filtredVisaOutgoingRecherche.getResponseMessageType().trim();

		}
		if (filtredVisaOutgoingRecherche.getSettlementDate() != null) {
			settlementDate = filtredVisaOutgoingRecherche.getSettlementDate().trim();
		}

		if (filtredVisaOutgoingRecherche.getAtmCode() != null) {
			switch (filtredVisaOutgoingRecherche.getAtmCode()) {
			case "MATCHING AVEC ATM":
				matching = 1;
				break;
			case "MATCHING AVEC CP":
				matching = 3;
				break;
			case "NOT MATCHING VISA ATM":
				matching = 2;
				break;
			}

		}
		Optional<VisaSummary> summaryDate = visaSummaryRepository
				.findBydateCompensation(filtredVisaOutgoingRecherche.getDateCompensation());
		if (summaryDate.isPresent()) {
			summary = summary + summaryDate.get().getSummaryCode();

			for (VisaOutgoingConfig element : result) {
				element.setCardNumber(
						StringTransformationService.getInstance().hideCardNumber(element.getCardNumber()));
			}
			return result;
		} else {
			return null;
		}

	}

	@PostMapping("ChargeBackRecuInternationalVISA")
	public Page<ChargebackVisa> findAllChargeBacKRecuInternationalVISA(@RequestBody ChargebackInterFilter filter,
			@RequestParam(name = "page", defaultValue = "0") int page,
			@RequestParam(name = "size", defaultValue = "10") int size, @RequestParam(name = "sortOn") String sortOn,
			@RequestParam(name = "dir") String dir) {

		Integer codeSummary = null;
		String cardNumber = filter.getCardNumber() != null ? filter.getCardNumber().trim() : null;
		String ref = filter.getDe42() != null ? filter.getDe42() : null;
		String date = filter.getDate() != null ? filter.getDate() : null;

		VisaSummary summary = visaSummaryRepository.findByDate(date);
		if (summary != null)
			codeSummary = summary.getSummaryCode();
		return chargebackVisaRepository.chargebackRecuVisa(PageRequest.of(page, size), cardNumber, ref, codeSummary);

	}

	@PostMapping("getCbVisaRecuTc33")
	public Page<VisaDisputeStatusAdvice> getCbVisaRecus(@RequestBody Map<String, String> filters,
			@RequestParam(name = "page", defaultValue = "0") int page,
			@RequestParam(name = "size", defaultValue = "10") int size, @RequestParam(name = "sortOn") String sortOn,
			@RequestParam(name = "dir") String dir) {
		Order order = null;
		Order order2 = null;
		if (dir.equals("desc")) {
			order = new Order(Sort.Direction.DESC, sortOn);
			order2 = new Order(Sort.Direction.DESC, "incomingCode");
		} else {
			order2 = new Order(Sort.Direction.ASC, "incomingCode");
			order = new Order(Sort.Direction.ASC, sortOn);
		}

		return visaDisputeStatusAdviceRepository.findCbVisaRecu(PageRequest.of(page, size, Sort.by(order, order2)),
				filters.get("authorizationCode"), filters.get("acquirerReferenceNumber"));

	}
	
	@PostMapping("getCbVisaEmis")
	public Page<VisaDisputeStatusAdvice> getCbVisaEmis(@RequestBody Map<String, String> filters,
			@RequestParam(name = "page", defaultValue = "0") int page,
			@RequestParam(name = "size", defaultValue = "10") int size, @RequestParam(name = "sortOn") String sortOn,
			@RequestParam(name = "dir") String dir) {
		Order order = null;
		Order order2 = null;
		if (dir.equals("desc")) {
			order = new Order(Sort.Direction.DESC, sortOn);
			order2 = new Order(Sort.Direction.DESC, "incomingCode");
		} else {
			order2 = new Order(Sort.Direction.ASC, "incomingCode");
			order = new Order(Sort.Direction.ASC, sortOn);
		}

		return visaDisputeStatusAdviceRepository.findCbVisaEmis(PageRequest.of(page, size, Sort.by(order, order2)),
				filters.get("authorizationCode"), filters.get("acquirerReferenceNumber"));

	}

	@PostMapping("getCbVisaEmisDocs")
	public List<VisaDisputeStatusAdvice> getCbVisaEmisDocs(@RequestBody Map<String, String> filters) {

		return visaDisputeStatusAdviceRepository.findCbVisaEmis(filters.get("authorizationCode"), filters.get("acquirerReferenceNumber"));

	}
	

	@GetMapping("/reason-codes")
	public List<Map<String, String>> getReasonCodes() {
		List<DualMessagePresentment> list = dualMessagePresentmentRepository.findAll();
		return list.stream()
				.map(item -> {
					Map<String, String> map = new HashMap<>();
					map.put("motif", item.getMotif());
					map.put("messageReasonCode", item.getMessageReasonCode());
					return map;
				})
				.collect(Collectors.toList());
	}

}
