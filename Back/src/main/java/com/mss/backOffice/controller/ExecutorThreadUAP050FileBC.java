package com.mss.backOffice.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Order;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.google.gson.Gson;
import com.mss.unified.entities.UAP040IN;
import com.mss.unified.entities.UAP040INHistory;
import com.mss.unified.entities.UAP050FransaBank;
import com.mss.unified.entities.UAP050FransaBankHistory;
import com.mss.unified.entities.UAP050IN;
import com.mss.unified.entities.UAP050INHistory;
import com.mss.unified.entities.UAP051FransaBank;
import com.mss.unified.entities.UAP051HistoryFransaBank;
import com.mss.unified.entities.UAP051IN;
import com.mss.unified.entities.UAP051INHistory;
import com.mss.unified.entities.UAPFransaBank;
import com.mss.unified.entities.UAPFransaBankHistory;
import com.mss.unified.entities.User;
import com.mss.unified.repositories.BatchesFFCRepository;
import com.mss.unified.repositories.BkmvtiFransaBankRepository;
import com.mss.unified.repositories.CodeBankBCRepository;
import com.mss.unified.repositories.DayOperationFransaBankRepository;
import com.mss.unified.repositories.MvbkConfigRepository;
import com.mss.unified.repositories.MvbkSettlementRepository;
import com.mss.unified.repositories.UAP040INFransaBankHistoryRepository;
import com.mss.unified.repositories.UAP040INFransaBankRepository;
import com.mss.unified.repositories.UAP050FransaBankHistoryRepository;
import com.mss.unified.repositories.UAP050FransaBankRepository;
import com.mss.unified.repositories.UAP050INFransaBankHistoryRepository;
import com.mss.unified.repositories.UAP050INFransaBankRepository;
import com.mss.unified.repositories.UAP051FransaBankHistoryRepository;
import com.mss.unified.repositories.UAP051FransaBankRepository;
import com.mss.unified.repositories.UAP051INFransaBankHistoryRepository;
import com.mss.unified.repositories.UAP051INFransaBankRepository;
import com.mss.unified.repositories.UAPFransaBankHistoryRepository;
import com.mss.unified.repositories.UAPFransaBankRepository;
import com.mss.unified.repositories.UserRepository;
import com.mss.unified.services.DynamicQueryService;

import org.springframework.data.jpa.domain.Specification;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("executorThreadUAP050FileBC")
public class ExecutorThreadUAP050FileBC {
	@Autowired
	DayOperationFransaBankRepository dayOperationFransaBankRepository;
	@Autowired
	CodeBankBCRepository codeBankBCRepository;
	@Autowired
	BatchesFFCRepository batchRepo;
	@Autowired
	ExecutorThreadUAP050INFileBC executorThreadUAP050INFileBC;
	private static final Logger logger = LoggerFactory.getLogger(ExecutorThreadUAP050FileBC.class);
	public static boolean catchException;

	@Autowired
	UAP050FransaBankRepository uAP050FransaBankRepository;
	@Autowired
	MvbkSettlementRepository mvbkSettlementRepository;
	@Autowired
	BkmvtiFransaBankRepository bkmvtiFransaBankRepository;
	@Autowired
	UAP050INFransaBankRepository uAP050INRepository;
	@Autowired
	UAP050FransaBankRepository uAP050OUTRepository;
	@Autowired
	UAPFransaBankRepository uAP040OUTRepository;
	@Autowired
	UAP040INFransaBankRepository uAP040INBankRepository;
	@Autowired
	DynamicQueryService dynamicQueryService;
	@Autowired
	UAP051FransaBankRepository uAP051FransaBankRepository;
	@Autowired
	UAP051INFransaBankRepository uAP051INRepository;
	@Autowired
	private UAP040INFransaBankHistoryRepository uAP040INFransaBankHistoryRepository;
	
	@Autowired
	private UAP050INFransaBankHistoryRepository uAP050INFransaBankHistoryRepository;
	@Autowired
	private UAP051INFransaBankHistoryRepository uAP051INFransaBankHistoryRepository;

	@Autowired
	private UAPFransaBankHistoryRepository  uAPFransaBankHistoryRepository;
	@Autowired
	UAP050FransaBankHistoryRepository uAP050FransaBankHistoryRepository;
	@Autowired
	UAP051FransaBankHistoryRepository uAP051FransaBankHistoryRepository;
	private static final Gson gson = new Gson();

	@Autowired
	public MvbkConfigRepository mvbkConfigR;
	@GetMapping("executeUAP050")
	public void parallelUAP(String name,String fileIntegration) throws ParseException {
		logger.info("executeUAP050");
		catchException = false;

		if (!catchException) {

			Runnable thread1 = new GenerateUAPFile050Thread(dayOperationFransaBankRepository, "1",
					uAP050FransaBankRepository, mvbkSettlementRepository, bkmvtiFransaBankRepository, name,fileIntegration,mvbkConfigR);
			Thread x = new Thread(thread1);
			x.start();
			doAfterEnd(x, name,fileIntegration);

		}

	}

	public void doAfterEnd(Thread x, String name,String fileIntegration) {
		// Wait for the threads to finish
		Thread waitThread = new Thread(() -> {
			try {
				x.join();
				executorThreadUAP050INFileBC.parallelUAP(name,fileIntegration);

			} catch (Exception e) {
				batchRepo.updateFinishBatch("Execute", 2, new Date());
				e.printStackTrace();
			}
		});
		waitThread.setName("status end ExecutorThreadUAP050FileBC");
		waitThread.start();
	}

	@PostMapping("findUAP050")
	public Page<UAP050FransaBank> getUAP050(@RequestBody(required = false) Map<String, String> uap,
			@RequestParam(name = "page", defaultValue = "0") int page,
			@RequestParam(name = "size", defaultValue = "5", required = false) int size,
			@RequestParam(name = "sortOn", defaultValue = "code", required = false) String sortOn,
			@RequestParam(name = "dir", defaultValue = "asc", required = false) String dir) {

		List<Order> orders = new ArrayList<Order>();
		Order order1 = null;
		if (dir.equals("desc"))
			order1 = new Order(Sort.Direction.DESC, sortOn);
		else
			order1 = new Order(Sort.Direction.ASC, sortOn);
		orders.add(order1);
		if (uap.get("accepted")!=null) {
			if (uap.get("accepted").equals("1")) {
				
				return uAP050FransaBankRepository.filterAcceptedUap(PageRequest.of(page, size, Sort.by(orders)),
						uap.get("typeTransaction"), uap.get("typePaiement"), uap.get("numRIBcommercant"),
						uap.get("numContratAccepteur"), uap.get("codeBin"), uap.get("numTransaction"),
						uap.get("dateTransactionS"), uap.get("dateTransactionE"), uap.get("dateDebutValiditeCarteS"),
						uap.get("dateDebutValiditeCarteE"), uap.get("dateFinValiditeCarteS"),
						uap.get("dateFinValiditeCarteE"), uap.get("dateRemiseS"), uap.get("dateRemiseE"),
						uap.get("numAutorisation"));
			}
			
			if (uap.get("accepted").equals("2")) {
				
				return uAP050FransaBankRepository.filterRejectedUap(PageRequest.of(page, size, Sort.by(orders)),
						uap.get("typeTransaction"), uap.get("typePaiement"), uap.get("numRIBcommercant"),
						uap.get("numContratAccepteur"), uap.get("codeBin"), uap.get("numTransaction"),
						uap.get("dateTransactionS"), uap.get("dateTransactionE"), uap.get("dateDebutValiditeCarteS"),
						uap.get("dateDebutValiditeCarteE"), uap.get("dateFinValiditeCarteS"),
						uap.get("dateFinValiditeCarteE"), uap.get("dateRemiseS"), uap.get("dateRemiseE"),
						uap.get("numAutorisation"));
			}
			
			if (uap.get("accepted").equals("3")) {
				
				return uAP050FransaBankRepository.filterUapWithProblem(PageRequest.of(page, size, Sort.by(orders)),
						uap.get("typeTransaction"), uap.get("typePaiement"), uap.get("numRIBcommercant"),
						uap.get("numContratAccepteur"), uap.get("codeBin"), uap.get("numTransaction"),
						uap.get("dateTransactionS"), uap.get("dateTransactionE"), uap.get("dateDebutValiditeCarteS"),
						uap.get("dateDebutValiditeCarteE"), uap.get("dateFinValiditeCarteS"),
						uap.get("dateFinValiditeCarteE"), uap.get("dateRemiseS"), uap.get("dateRemiseE"),
						uap.get("numAutorisation"));
			}



			if (uap.get("accepted").equals("4")) {
	
				return uAP050FransaBankRepository.filterWaitingUap(PageRequest.of(page, size, Sort.by(orders)),
						uap.get("typeTransaction"), uap.get("typePaiement"), uap.get("numRIBcommercant"),
						uap.get("numContratAccepteur"), uap.get("codeBin"), uap.get("numTransaction"),
						uap.get("dateTransactionS"), uap.get("dateTransactionE"), uap.get("dateDebutValiditeCarteS"),
						uap.get("dateDebutValiditeCarteE"), uap.get("dateFinValiditeCarteS"),
						uap.get("dateFinValiditeCarteE"), uap.get("dateRemiseS"), uap.get("dateRemiseE"),
						uap.get("numAutorisation"));
			}

		} else {

			return uAP050FransaBankRepository.filter(PageRequest.of(page, size, Sort.by(orders)),
					uap.get("typeTransaction"), uap.get("typePaiement"), uap.get("numRIBcommercant"),
					uap.get("numContratAccepteur"), uap.get("codeBin"), uap.get("numTransaction"),
					uap.get("dateTransactionS"), uap.get("dateTransactionE"), uap.get("dateDebutValiditeCarteS"),
					uap.get("dateDebutValiditeCarteE"), uap.get("dateFinValiditeCarteS"),
					uap.get("dateFinValiditeCarteE"), uap.get("dateRemiseS"), uap.get("dateRemiseE"),
					uap.get("numAutorisation"));
		}
		return null;
	}
	
	
	
	
	@PostMapping("findUAP050Archive")
	public Page<UAP050FransaBankHistory> getUAP050Archive(@RequestBody(required = false) Map<String, String> uap,
			@RequestParam(name = "page", defaultValue = "0") int page,
			@RequestParam(name = "size", defaultValue = "5", required = false) int size,
			@RequestParam(name = "sortOn", defaultValue = "code", required = false) String sortOn,
			@RequestParam(name = "dir", defaultValue = "asc", required = false) String dir) {

		List<Order> orders = new ArrayList<Order>();
		Order order1 = null;
		if (dir.equals("desc"))
			order1 = new Order(Sort.Direction.DESC, sortOn);
		else
			order1 = new Order(Sort.Direction.ASC, sortOn);
		orders.add(order1);
		if (uap.get("accepted")!=null) {
			if (uap.get("accepted").equals("1")) {
				
				return uAP050FransaBankHistoryRepository.filterAcceptedUap(PageRequest.of(page, size, Sort.by(orders)),
						uap.get("typeTransaction"), uap.get("typePaiement"), uap.get("numRIBcommercant"),
						uap.get("numContratAccepteur"), uap.get("codeBin"), uap.get("numTransaction"),
						uap.get("dateTransactionS"), uap.get("dateTransactionE"), uap.get("dateDebutValiditeCarteS"),
						uap.get("dateDebutValiditeCarteE"), uap.get("dateFinValiditeCarteS"),
						uap.get("dateFinValiditeCarteE"), uap.get("dateRemiseS"), uap.get("dateRemiseE"),
						uap.get("numAutorisation"));
			}
			
			if (uap.get("accepted").equals("2")) {
				
				return uAP050FransaBankHistoryRepository.filterRejectedUap(PageRequest.of(page, size, Sort.by(orders)),
						uap.get("typeTransaction"), uap.get("typePaiement"), uap.get("numRIBcommercant"),
						uap.get("numContratAccepteur"), uap.get("codeBin"), uap.get("numTransaction"),
						uap.get("dateTransactionS"), uap.get("dateTransactionE"), uap.get("dateDebutValiditeCarteS"),
						uap.get("dateDebutValiditeCarteE"), uap.get("dateFinValiditeCarteS"),
						uap.get("dateFinValiditeCarteE"), uap.get("dateRemiseS"), uap.get("dateRemiseE"),
						uap.get("numAutorisation"));
			}
			
			if (uap.get("accepted").equals("3")) {
				
				return uAP050FransaBankHistoryRepository.filterUapWithProblem(PageRequest.of(page, size, Sort.by(orders)),
						uap.get("typeTransaction"), uap.get("typePaiement"), uap.get("numRIBcommercant"),
						uap.get("numContratAccepteur"), uap.get("codeBin"), uap.get("numTransaction"),
						uap.get("dateTransactionS"), uap.get("dateTransactionE"), uap.get("dateDebutValiditeCarteS"),
						uap.get("dateDebutValiditeCarteE"), uap.get("dateFinValiditeCarteS"),
						uap.get("dateFinValiditeCarteE"), uap.get("dateRemiseS"), uap.get("dateRemiseE"),
						uap.get("numAutorisation"));
			}



			if (uap.get("accepted").equals("4")) {
	
				return uAP050FransaBankHistoryRepository.filterWaitingUap(PageRequest.of(page, size, Sort.by(orders)),
						uap.get("typeTransaction"), uap.get("typePaiement"), uap.get("numRIBcommercant"),
						uap.get("numContratAccepteur"), uap.get("codeBin"), uap.get("numTransaction"),
						uap.get("dateTransactionS"), uap.get("dateTransactionE"), uap.get("dateDebutValiditeCarteS"),
						uap.get("dateDebutValiditeCarteE"), uap.get("dateFinValiditeCarteS"),
						uap.get("dateFinValiditeCarteE"), uap.get("dateRemiseS"), uap.get("dateRemiseE"),
						uap.get("numAutorisation"));
			}

		} else {

			return uAP050FransaBankHistoryRepository.filter(PageRequest.of(page, size, Sort.by(orders)),
					uap.get("typeTransaction"), uap.get("typePaiement"), uap.get("numRIBcommercant"),
					uap.get("numContratAccepteur"), uap.get("codeBin"), uap.get("numTransaction"),
					uap.get("dateTransactionS"), uap.get("dateTransactionE"), uap.get("dateDebutValiditeCarteS"),
					uap.get("dateDebutValiditeCarteE"), uap.get("dateFinValiditeCarteS"),
					uap.get("dateFinValiditeCarteE"), uap.get("dateRemiseS"), uap.get("dateRemiseE"),
					uap.get("numAutorisation"));
		}
		return null;
	}

	@PostMapping("findUAP051")
	public Page<UAP051FransaBank> getUAP051(@RequestBody(required = false) Map<String, String> uap,
			@RequestParam(name = "page", defaultValue = "0") int page,
			@RequestParam(name = "size", defaultValue = "5", required = false) int size,
			@RequestParam(name = "sortOn", defaultValue = "code", required = false) String sortOn,
			@RequestParam(name = "dir", defaultValue = "asc", required = false) String dir) {

		List<Order> orders = new ArrayList<Order>();
		Order order1 = null;
		if (dir.equals("desc"))
			order1 = new Order(Sort.Direction.DESC, sortOn);
		else
			order1 = new Order(Sort.Direction.ASC, sortOn);
		orders.add(order1);
		if (uap.get("accepted")!=null) {
			
			if (uap.get("accepted").equals("1")) {
				
				return uAP051FransaBankRepository.filterAcceptedUap(PageRequest.of(page, size, Sort.by(orders)),
						
						uap.get("typeTransaction"), uap.get("typePaiement"), uap.get("numRIBcommercant"),
						uap.get("numContratAccepteur"), uap.get("codeBin"), uap.get("numTransaction"),
						uap.get("dateTransactionS"), uap.get("dateTransactionE"), uap.get("dateDebutValiditeCarteS"),
						uap.get("dateDebutValiditeCarteE"), uap.get("dateFinValiditeCarteS"),
						uap.get("dateFinValiditeCarteE"), uap.get("dateRemiseS"), uap.get("dateRemiseE"),
						uap.get("numAutorisation"));
			}
	if (uap.get("accepted").equals("2")) {
				
				return uAP051FransaBankRepository.filterRejectedUap(PageRequest.of(page, size, Sort.by(orders)),
						
						uap.get("typeTransaction"), uap.get("typePaiement"), uap.get("numRIBcommercant"),
						uap.get("numContratAccepteur"), uap.get("codeBin"), uap.get("numTransaction"),
						uap.get("dateTransactionS"), uap.get("dateTransactionE"), uap.get("dateDebutValiditeCarteS"),
						uap.get("dateDebutValiditeCarteE"), uap.get("dateFinValiditeCarteS"),
						uap.get("dateFinValiditeCarteE"), uap.get("dateRemiseS"), uap.get("dateRemiseE"),
						uap.get("numAutorisation"));
			}
	if (uap.get("accepted").equals("3")) {
		
		return uAP051FransaBankRepository.filterUapWithProblem(PageRequest.of(page, size, Sort.by(orders)),
				
				uap.get("typeTransaction"), uap.get("typePaiement"), uap.get("numRIBcommercant"),
				uap.get("numContratAccepteur"), uap.get("codeBin"), uap.get("numTransaction"),
				uap.get("dateTransactionS"), uap.get("dateTransactionE"), uap.get("dateDebutValiditeCarteS"),
				uap.get("dateDebutValiditeCarteE"), uap.get("dateFinValiditeCarteS"),
				uap.get("dateFinValiditeCarteE"), uap.get("dateRemiseS"), uap.get("dateRemiseE"),
				uap.get("numAutorisation"));
	}
	
	if (uap.get("accepted").equals("4")) {
		
		return uAP051FransaBankRepository.filterWaitingUap(PageRequest.of(page, size, Sort.by(orders)),
				
				uap.get("typeTransaction"), uap.get("typePaiement"), uap.get("numRIBcommercant"),
				uap.get("numContratAccepteur"), uap.get("codeBin"), uap.get("numTransaction"),
				uap.get("dateTransactionS"), uap.get("dateTransactionE"), uap.get("dateDebutValiditeCarteS"),
				uap.get("dateDebutValiditeCarteE"), uap.get("dateFinValiditeCarteS"),
				uap.get("dateFinValiditeCarteE"), uap.get("dateRemiseS"), uap.get("dateRemiseE"),
				uap.get("numAutorisation"));
	}
	

		} else {

			return uAP051FransaBankRepository.filter(PageRequest.of(page, size, Sort.by(orders)),
					uap.get("typeTransaction"), uap.get("typePaiement"), uap.get("numRIBcommercant"),
					uap.get("numContratAccepteur"), uap.get("codeBin"), uap.get("numTransaction"),
					uap.get("dateTransactionS"), uap.get("dateTransactionE"), uap.get("dateDebutValiditeCarteS"),
					uap.get("dateDebutValiditeCarteE"), uap.get("dateFinValiditeCarteS"),
					uap.get("dateFinValiditeCarteE"), uap.get("dateRemiseS"), uap.get("dateRemiseE"),
					uap.get("numAutorisation"));
		}
		
		return null;
	}
	
	
	
	@PostMapping("findUAP05Archive")
	public Page<UAP051HistoryFransaBank> getUAP051Archive(@RequestBody(required = false) Map<String, String> uap,
			@RequestParam(name = "page", defaultValue = "0") int page,
			@RequestParam(name = "size", defaultValue = "5", required = false) int size,
			@RequestParam(name = "sortOn", defaultValue = "code", required = false) String sortOn,
			@RequestParam(name = "dir", defaultValue = "asc", required = false) String dir) {

		List<Order> orders = new ArrayList<Order>();
		Order order1 = null;
		if (dir.equals("desc"))
			order1 = new Order(Sort.Direction.DESC, sortOn);
		else
			order1 = new Order(Sort.Direction.ASC, sortOn);
		orders.add(order1);
		if (uap.get("accepted")!=null) {
			
			if (uap.get("accepted").equals("1")) {
				
				return uAP051FransaBankHistoryRepository.filterAcceptedUap(PageRequest.of(page, size, Sort.by(orders)),
						
						uap.get("typeTransaction"), uap.get("typePaiement"), uap.get("numRIBcommercant"),
						uap.get("numContratAccepteur"), uap.get("codeBin"), uap.get("numTransaction"),
						uap.get("dateTransactionS"), uap.get("dateTransactionE"), uap.get("dateDebutValiditeCarteS"),
						uap.get("dateDebutValiditeCarteE"), uap.get("dateFinValiditeCarteS"),
						uap.get("dateFinValiditeCarteE"), uap.get("dateRemiseS"), uap.get("dateRemiseE"),
						uap.get("numAutorisation"));
			}
	if (uap.get("accepted").equals("2")) {
				
				return uAP051FransaBankHistoryRepository.filterRejectedUap(PageRequest.of(page, size, Sort.by(orders)),
						
						uap.get("typeTransaction"), uap.get("typePaiement"), uap.get("numRIBcommercant"),
						uap.get("numContratAccepteur"), uap.get("codeBin"), uap.get("numTransaction"),
						uap.get("dateTransactionS"), uap.get("dateTransactionE"), uap.get("dateDebutValiditeCarteS"),
						uap.get("dateDebutValiditeCarteE"), uap.get("dateFinValiditeCarteS"),
						uap.get("dateFinValiditeCarteE"), uap.get("dateRemiseS"), uap.get("dateRemiseE"),
						uap.get("numAutorisation"));
			}
	if (uap.get("accepted").equals("3")) {
		
		return uAP051FransaBankHistoryRepository.filterUapWithProblem(PageRequest.of(page, size, Sort.by(orders)),
				
				uap.get("typeTransaction"), uap.get("typePaiement"), uap.get("numRIBcommercant"),
				uap.get("numContratAccepteur"), uap.get("codeBin"), uap.get("numTransaction"),
				uap.get("dateTransactionS"), uap.get("dateTransactionE"), uap.get("dateDebutValiditeCarteS"),
				uap.get("dateDebutValiditeCarteE"), uap.get("dateFinValiditeCarteS"),
				uap.get("dateFinValiditeCarteE"), uap.get("dateRemiseS"), uap.get("dateRemiseE"),
				uap.get("numAutorisation"));
	}
	
	if (uap.get("accepted").equals("4")) {
		
		return uAP051FransaBankHistoryRepository.filterWaitingUap(PageRequest.of(page, size, Sort.by(orders)),
				
				uap.get("typeTransaction"), uap.get("typePaiement"), uap.get("numRIBcommercant"),
				uap.get("numContratAccepteur"), uap.get("codeBin"), uap.get("numTransaction"),
				uap.get("dateTransactionS"), uap.get("dateTransactionE"), uap.get("dateDebutValiditeCarteS"),
				uap.get("dateDebutValiditeCarteE"), uap.get("dateFinValiditeCarteS"),
				uap.get("dateFinValiditeCarteE"), uap.get("dateRemiseS"), uap.get("dateRemiseE"),
				uap.get("numAutorisation"));
	}
	

		} else {

			return uAP051FransaBankHistoryRepository.filter(PageRequest.of(page, size, Sort.by(orders)),
					uap.get("typeTransaction"), uap.get("typePaiement"), uap.get("numRIBcommercant"),
					uap.get("numContratAccepteur"), uap.get("codeBin"), uap.get("numTransaction"),
					uap.get("dateTransactionS"), uap.get("dateTransactionE"), uap.get("dateDebutValiditeCarteS"),
					uap.get("dateDebutValiditeCarteE"), uap.get("dateFinValiditeCarteS"),
					uap.get("dateFinValiditeCarteE"), uap.get("dateRemiseS"), uap.get("dateRemiseE"),
					uap.get("numAutorisation"));
		}
		
		return null;
	}

	@PostMapping("findUAP050IN")
	public Page<UAP050IN> getUAP050IN(@RequestBody(required = false) Map<String, String> uap,
			@RequestParam(name = "page", defaultValue = "0") int page,
			@RequestParam(name = "size", defaultValue = "5", required = false) int size,
			@RequestParam(name = "sortOn", defaultValue = "code", required = false) String sortOn,
			@RequestParam(name = "dir", defaultValue = "asc", required = false) String dir) {

		List<Order> orders = new ArrayList<Order>();
		Order order1 = null;
		if (dir.equals("desc"))
			order1 = new Order(Sort.Direction.DESC, sortOn);
		else
			order1 = new Order(Sort.Direction.ASC, sortOn);
		orders.add(order1);
		if (size < 0) {

			return uAP050INRepository.filter(
					PageRequest.of(page, (int) uAP050FransaBankRepository.count(), Sort.by(orders)),
					uap.get("typeTransaction"), uap.get("typePaiement"), uap.get("numRIBcommercant"),
					uap.get("numContratAccepteur"), uap.get("codeBin"), uap.get("numTransaction"),
					uap.get("dateTransactionS"), uap.get("dateTransactionE"), uap.get("dateDebutValiditeCarteS"),
					uap.get("dateDebutValiditeCarteE"), uap.get("dateFinValiditeCarteS"),
					uap.get("dateFinValiditeCarteE"), uap.get("dateRemiseS"), uap.get("dateRemiseE"),
					uap.get("numAutorisation"));
		} else {

			return uAP050INRepository.filter(
					PageRequest.of(0, Long.valueOf(uAP050INRepository.count()).intValue(), Sort.by(orders)),
					uap.get("typeTransaction"), uap.get("typePaiement"), uap.get("numRIBcommercant"),
					uap.get("numContratAccepteur"), uap.get("codeBin"), uap.get("numTransaction"),
					uap.get("dateTransactionS"), uap.get("dateTransactionE"), uap.get("dateDebutValiditeCarteS"),
					uap.get("dateDebutValiditeCarteE"), uap.get("dateFinValiditeCarteS"),
					uap.get("dateFinValiditeCarteE"), uap.get("dateRemiseS"), uap.get("dateRemiseE"),
					uap.get("numAutorisation"));
		}
//					uAP050FransaBankRepository.findAll(PageRequest.of(page, size,Sort.by(orders)));

	}

	@PostMapping("findUAP051IN")
	public Page<UAP051IN> getUAP051IN(@RequestBody(required = false) Map<String, String> uap,
			@RequestParam(name = "page", defaultValue = "0") int page,
			@RequestParam(name = "size", defaultValue = "5", required = false) int size,
			@RequestParam(name = "sortOn", defaultValue = "code", required = false) String sortOn,
			@RequestParam(name = "dir", defaultValue = "asc", required = false) String dir) {

		List<Order> orders = new ArrayList<Order>();
		Order order1 = null;
		if (dir.equals("desc"))
			order1 = new Order(Sort.Direction.DESC, sortOn);
		else
			order1 = new Order(Sort.Direction.ASC, sortOn);
		orders.add(order1);
		if (size < 0) {

			return uAP051INRepository.filter(
					PageRequest.of(page, (int) uAP051FransaBankRepository.count(), Sort.by(orders)),
					uap.get("typeTransaction"), uap.get("typePaiement"), uap.get("numRIBcommercant"),
					uap.get("numContratAccepteur"), uap.get("codeBin"), uap.get("numTransaction"),
					uap.get("dateTransactionS"), uap.get("dateTransactionE"), uap.get("dateDebutValiditeCarteS"),
					uap.get("dateDebutValiditeCarteE"), uap.get("dateFinValiditeCarteS"),
					uap.get("dateFinValiditeCarteE"), uap.get("dateRemiseS"), uap.get("dateRemiseE"),
					uap.get("numAutorisation"));
		} else {

			return uAP051INRepository.filter(
					PageRequest.of(0, Long.valueOf(uAP051INRepository.count()).intValue(), Sort.by(orders)),
					uap.get("typeTransaction"), uap.get("typePaiement"), uap.get("numRIBcommercant"),
					uap.get("numContratAccepteur"), uap.get("codeBin"), uap.get("numTransaction"),
					uap.get("dateTransactionS"), uap.get("dateTransactionE"), uap.get("dateDebutValiditeCarteS"),
					uap.get("dateDebutValiditeCarteE"), uap.get("dateFinValiditeCarteS"),
					uap.get("dateFinValiditeCarteE"), uap.get("dateRemiseS"), uap.get("dateRemiseE"),
					uap.get("numAutorisation"));
		}
//					uAP050FransaBankRepository.findAll(PageRequest.of(page, size,Sort.by(orders)));

	}

	@PostMapping("findUAP050OUT")
	public Page<UAP050FransaBank> getUAP050OUT(@RequestBody(required = false) Map<String, String> uap,
			@RequestParam(name = "page", defaultValue = "0") int page,
			@RequestParam(name = "size", defaultValue = "5", required = false) int size,
			@RequestParam(name = "sortOn", defaultValue = "code", required = false) String sortOn,
			@RequestParam(name = "dir", defaultValue = "asc", required = false) String dir) {

		List<Order> orders = new ArrayList<Order>();
		Order order1 = null;
		if (dir.equals("desc"))
			order1 = new Order(Sort.Direction.DESC, sortOn);
		else
			order1 = new Order(Sort.Direction.ASC, sortOn);
		orders.add(order1);
		if (size < 0) {

			return uAP050OUTRepository.filter(
					PageRequest.of(page, (int) uAP050FransaBankRepository.count(), Sort.by(orders)),
					uap.get("typeTransaction"), uap.get("typePaiement"), uap.get("numRIBcommercant"),
					uap.get("numContratAccepteur"), uap.get("codeBin"), uap.get("numTransaction"),
					uap.get("dateTransactionS"), uap.get("dateTransactionE"), uap.get("dateDebutValiditeCarteS"),
					uap.get("dateDebutValiditeCarteE"), uap.get("dateFinValiditeCarteS"),
					uap.get("dateFinValiditeCarteE"), uap.get("dateRemiseS"), uap.get("dateRemiseE"),
					uap.get("numAutorisation"));
		} else {

			return uAP050OUTRepository.filter(PageRequest.of(page, size, Sort.by(orders)), uap.get("typeTransaction"),
					uap.get("typePaiement"), uap.get("numRIBcommercant"), uap.get("numContratAccepteur"),
					uap.get("codeBin"), uap.get("numTransaction"), uap.get("dateTransactionS"),
					uap.get("dateTransactionE"), uap.get("dateDebutValiditeCarteS"), uap.get("dateDebutValiditeCarteE"),
					uap.get("dateFinValiditeCarteS"), uap.get("dateFinValiditeCarteE"), uap.get("dateRemiseS"),
					uap.get("dateRemiseE"), uap.get("numAutorisation"));
		}
	}

	@PostMapping("findUAP040OUT")
	public Page<UAPFransaBank> getUAP040OUT(@RequestBody(required = false) Map<String, String> uap,
			@RequestParam(name = "page", defaultValue = "0") int page,
			@RequestParam(name = "size", defaultValue = "5", required = false) int size,
			@RequestParam(name = "sortOn", defaultValue = "code", required = false) String sortOn,
			@RequestParam(name = "dir", defaultValue = "asc", required = false) String dir) {

		List<Order> orders = new ArrayList<Order>();
		Order order1 = dir.equals("desc") 
			? new Order(Sort.Direction.DESC, sortOn) 
			: new Order(Sort.Direction.ASC, sortOn);
		orders.add(order1);

		String accepted = uap.get("accepted");
		Map<String, String> criteria = new java.util.HashMap<>(uap);
		criteria.remove("accepted");
		
		Specification<UAPFransaBank> spec = dynamicQueryService.buildSpecification(criteria, UAPFransaBank.class);
		spec = dynamicQueryService.addStatusTechniqueCondition(spec, accepted);

		return uAP040OUTRepository.findAll(spec, PageRequest.of(page, size, Sort.by(orders)));
	}
	
	
	
	@PostMapping("findUAP040OUTArchive")
	public Page<UAPFransaBankHistory> getUAP040OUTArchive(@RequestBody(required = false) Map<String, String> uap,
			@RequestParam(name = "page", defaultValue = "0") int page,
			@RequestParam(name = "size", defaultValue = "5", required = false) int size,
			@RequestParam(name = "sortOn", defaultValue = "code", required = false) String sortOn,
			@RequestParam(name = "dir", defaultValue = "asc", required = false) String dir) {

		List<Order> orders = new ArrayList<Order>();
		Order order1 = dir.equals("desc") 
			? new Order(Sort.Direction.DESC, sortOn) 
			: new Order(Sort.Direction.ASC, sortOn);
		orders.add(order1);

		String accepted = uap.get("accepted");
		Map<String, String> criteria = new java.util.HashMap<>(uap);
		criteria.remove("accepted");
		
		Specification<UAPFransaBankHistory> spec = dynamicQueryService.buildSpecification(criteria, UAPFransaBankHistory.class);
		spec = dynamicQueryService.addStatusTechniqueCondition(spec, accepted);

		return uAPFransaBankHistoryRepository.findAll(spec, PageRequest.of(page, size, Sort.by(orders)));
	}
	
	

	@PostMapping("findUAP040IN")
	public Page<UAP040IN> getUAP040IN(@RequestBody(required = false) Map<String, String> uap,
			@RequestParam(name = "page", defaultValue = "0") int page,
			@RequestParam(name = "size", defaultValue = "5", required = false) int size,
			@RequestParam(name = "sortOn", defaultValue = "code", required = false) String sortOn,
			@RequestParam(name = "dir", defaultValue = "asc", required = false) String dir) {

		List<Order> orders = new ArrayList<Order>();
		Order order1 = null;
		if (dir.equals("desc"))
			order1 = new Order(Sort.Direction.DESC, sortOn);
		else
			order1 = new Order(Sort.Direction.ASC, sortOn);
		orders.add(order1);
		if (size < 0) {

			return uAP040INBankRepository.filter(
					PageRequest.of(page, (int) uAP050FransaBankRepository.count(), Sort.by(orders)),
					uap.get("typeTransaction"), uap.get("typePaiement"), uap.get("numRIBcommercant"),
					uap.get("numContratAccepteur"), uap.get("codeBin"), uap.get("numTransaction"),
					uap.get("dateTransactionS"), uap.get("dateTransactionE"), uap.get("dateDebutValiditeCarteS"),
					uap.get("dateDebutValiditeCarteE"), uap.get("dateFinValiditeCarteS"),
					uap.get("dateFinValiditeCarteE"), uap.get("dateRemiseS"), uap.get("dateRemiseE"),
					uap.get("numAutorisation"));
		} else {

			return uAP040INBankRepository.filter(PageRequest.of(page, size, Sort.by(orders)),
					uap.get("typeTransaction"), uap.get("typePaiement"), uap.get("numRIBcommercant"),
					uap.get("numContratAccepteur"), uap.get("codeBin"), uap.get("numTransaction"),
					uap.get("dateTransactionS"), uap.get("dateTransactionE"), uap.get("dateDebutValiditeCarteS"),
					uap.get("dateDebutValiditeCarteE"), uap.get("dateFinValiditeCarteS"),
					uap.get("dateFinValiditeCarteE"), uap.get("dateRemiseS"), uap.get("dateRemiseE"),
					uap.get("numAutorisation"));
		}
	}

//	@PostMapping("findUAP040INEr")
//	public Page<UAP040IN> getUAP040INEr(@RequestBody(required = false) Map<String, String> uap,
//			@RequestParam(name = "page", defaultValue = "0") int page,
//			@RequestParam(name = "size", defaultValue = "5", required = false) int size,
//			@RequestParam(name = "sortOn", defaultValue = "code", required = false) String sortOn,
//			@RequestParam(name = "dir", defaultValue = "asc", required = false) String dir) {
//
//		List<Order> orders = new ArrayList<Order>();
//		Order order1 = null;
//		if (dir.equals("desc"))
//			order1 = new Order(Sort.Direction.DESC, sortOn);
//		else
//			order1 = new Order(Sort.Direction.ASC, sortOn);
//		orders.add(order1);
//
//
//
//		if (uap.get("accepted")!=null) {
//			if (uap.get("accepted").equals("4")) {
//
//				return uAP040INBankRepository.filterErNotYetMatched(PageRequest.of(page, size, Sort.by(orders)), uap.get("typeTransaction"),
//						uap.get("typePaiement"), uap.get("numRIBcommercant"), uap.get("numContratAccepteur"),
//						uap.get("codeBin"), uap.get("numTransaction"), uap.get("dateTransactionS"),
//						uap.get("dateTransactionE"), uap.get("dateDebutValiditeCarteS"), uap.get("dateDebutValiditeCarteE"),
//						uap.get("dateFinValiditeCarteS"), uap.get("dateFinValiditeCarteE"), uap.get("dateRemiseS"),
//						uap.get("dateRemiseE"), uap.get("numAutorisation"));
//
//			}else if (uap.get("accepted").equals("21") || uap.get("accepted").equals("22") || uap.get("accepted").equals("23")) {
//
//				if (uap.get("accepted").equals("21")) {
//					return uAP040INBankRepository.filterErExtra(PageRequest.of(page, size, Sort.by(orders)), uap.get("typeTransaction"),
//							uap.get("typePaiement"), uap.get("numRIBcommercant"), uap.get("numContratAccepteur"),
//							uap.get("codeBin"), uap.get("numTransaction"), uap.get("dateTransactionS"),
//							uap.get("dateTransactionE"), uap.get("dateDebutValiditeCarteS"), uap.get("dateDebutValiditeCarteE"),
//							uap.get("dateFinValiditeCarteS"), uap.get("dateFinValiditeCarteE"), uap.get("dateRemiseS"),
//							uap.get("dateRemiseE"), uap.get("numAutorisation"),"1");
//
//				}
//				if (uap.get("accepted").equals("22")) {
//					System.out.println("22");
//					return uAP040INBankRepository.filterErExtra(PageRequest.of(page, size, Sort.by(orders)), uap.get("typeTransaction"),
//							uap.get("typePaiement"), uap.get("numRIBcommercant"), uap.get("numContratAccepteur"),
//							uap.get("codeBin"), uap.get("numTransaction"), uap.get("dateTransactionS"),
//							uap.get("dateTransactionE"), uap.get("dateDebutValiditeCarteS"), uap.get("dateDebutValiditeCarteE"),
//							uap.get("dateFinValiditeCarteS"), uap.get("dateFinValiditeCarteE"), uap.get("dateRemiseS"),
//							uap.get("dateRemiseE"), uap.get("numAutorisation"),"2");
//
//				}
//
//				if (uap.get("accepted").equals("23")) {
//					return uAP040INBankRepository.filterErExtraWaiting(PageRequest.of(page, size, Sort.by(orders)), uap.get("typeTransaction"),
//							uap.get("typePaiement"), uap.get("numRIBcommercant"), uap.get("numContratAccepteur"),
//							uap.get("codeBin"), uap.get("numTransaction"), uap.get("dateTransactionS"),
//							uap.get("dateTransactionE"), uap.get("dateDebutValiditeCarteS"), uap.get("dateDebutValiditeCarteE"),
//							uap.get("dateFinValiditeCarteS"), uap.get("dateFinValiditeCarteE"), uap.get("dateRemiseS"),
//							uap.get("dateRemiseE"), uap.get("numAutorisation"));
//
//				}
//
//			} else {
//
//				return uAP040INBankRepository.filterErWithStatus(PageRequest.of(page, size, Sort.by(orders)), uap.get("typeTransaction"),
//						uap.get("typePaiement"), uap.get("numRIBcommercant"), uap.get("numContratAccepteur"),
//						uap.get("codeBin"), uap.get("numTransaction"), uap.get("dateTransactionS"),
//						uap.get("dateTransactionE"), uap.get("dateDebutValiditeCarteS"), uap.get("dateDebutValiditeCarteE"),
//						uap.get("dateFinValiditeCarteS"), uap.get("dateFinValiditeCarteE"), uap.get("dateRemiseS"),
//						uap.get("dateRemiseE"), uap.get("numAutorisation"),uap.get("accepted"));
//			}
//
//
//
//
//		}else {
//			return uAP040INBankRepository.filterErWithStatus(PageRequest.of(page, size, Sort.by(orders)), uap.get("typeTransaction"),
//					uap.get("typePaiement"), uap.get("numRIBcommercant"), uap.get("numContratAccepteur"),
//					uap.get("codeBin"), uap.get("numTransaction"), uap.get("dateTransactionS"),
//					uap.get("dateTransactionE"), uap.get("dateDebutValiditeCarteS"), uap.get("dateDebutValiditeCarteE"),
//					uap.get("dateFinValiditeCarteS"), uap.get("dateFinValiditeCarteE"), uap.get("dateRemiseS"),
//					uap.get("dateRemiseE"), uap.get("numAutorisation"),uap.get("accepted"));
//
//		}
//
//
//		return null;
//	}

	@PostMapping("findUAP040INEr")
	public Page<UAP040IN> getUAP040INEr(@RequestBody(required = false) Map<String, String> uap,
										@RequestParam(name = "page", defaultValue = "0") int page,
										@RequestParam(name = "size", defaultValue = "5", required = false) int size,
										@RequestParam(name = "sortOn", defaultValue = "code", required = false) String sortOn,
										@RequestParam(name = "dir", defaultValue = "asc", required = false) String dir) {


		List<Order> orders = new ArrayList<Order>();
		Order order1 = dir.equals("desc") 
			? new Order(Sort.Direction.DESC, sortOn) 
			: new Order(Sort.Direction.ASC, sortOn);
		orders.add(order1);

		String accepted = uap.get("accepted");
		Map<String, String> criteria = new java.util.HashMap<>(uap);
		criteria.remove("accepted");
		
		Specification<UAP040IN> spec = dynamicQueryService.buildSpecification(criteria, UAP040IN.class);
		spec = dynamicQueryService.addAcceptedCondition(spec, accepted);

		return uAP040INBankRepository.findAll(spec, PageRequest.of(page, size, Sort.by(orders)));
	}

	@PostMapping("findUAP040INErArchive")
	public Page<UAP040INHistory> getUAP040INErArchive(@RequestBody(required = false) Map<String, String> uap,
			@RequestParam(name = "page", defaultValue = "0") int page,
			@RequestParam(name = "size", defaultValue = "5", required = false) int size,
			@RequestParam(name = "sortOn", defaultValue = "code", required = false) String sortOn,
			@RequestParam(name = "dir", defaultValue = "asc", required = false) String dir) {

		List<Order> orders = new ArrayList<Order>();
		Order order1 = dir.equals("desc") 
			? new Order(Sort.Direction.DESC, sortOn) 
			: new Order(Sort.Direction.ASC, sortOn);
		orders.add(order1);

		String accepted = uap.get("accepted");
		Map<String, String> criteria = new java.util.HashMap<>(uap);
		criteria.remove("accepted");
		
		Specification<UAP040INHistory> spec = dynamicQueryService.buildSpecification(criteria, UAP040INHistory.class);
		spec = dynamicQueryService.addAcceptedCondition(spec, accepted);

		return uAP040INFransaBankHistoryRepository.findAll(spec, PageRequest.of(page, size, Sort.by(orders)));
	}
	
	@PostMapping("findUAP050INEr")
	public Page<UAP050IN> getUAP050INEr(@RequestBody(required = false) Map<String, String> uap,
			@RequestParam(name = "page", defaultValue = "0") int page,
			@RequestParam(name = "size", defaultValue = "5", required = false) int size,
			@RequestParam(name = "sortOn", defaultValue = "code", required = false) String sortOn,
			@RequestParam(name = "dir", defaultValue = "asc", required = false) String dir) {

		List<Order> orders = new ArrayList<Order>();
		Order order1 = dir.equals("desc") 
			? new Order(Sort.Direction.DESC, sortOn) 
			: new Order(Sort.Direction.ASC, sortOn);
		orders.add(order1);

		String accepted = uap.get("accepted");
		Map<String, String> criteria = new java.util.HashMap<>(uap);
		criteria.remove("accepted");
		
		Specification<UAP050IN> spec = dynamicQueryService.buildSpecification(criteria, UAP050IN.class);
		spec = dynamicQueryService.addAcceptedCondition(spec, accepted);

		return uAP050INRepository.findAll(spec, PageRequest.of(page, size, Sort.by(orders)));
	}
	
	
	@PostMapping("findUAP050INErArchive")
	public Page<UAP050INHistory> getUAP050INErArchive(@RequestBody(required = false) Map<String, String> uap,
			@RequestParam(name = "page", defaultValue = "0") int page,
			@RequestParam(name = "size", defaultValue = "5", required = false) int size,
			@RequestParam(name = "sortOn", defaultValue = "code", required = false) String sortOn,
			@RequestParam(name = "dir", defaultValue = "asc", required = false) String dir) {

		List<Order> orders = new ArrayList<Order>();
		Order order1 = dir.equals("desc") 
			? new Order(Sort.Direction.DESC, sortOn) 
			: new Order(Sort.Direction.ASC, sortOn);
		orders.add(order1);

		String accepted = uap.get("accepted");
		Map<String, String> criteria = new java.util.HashMap<>(uap);
		criteria.remove("accepted");
		
		Specification<UAP050INHistory> spec = dynamicQueryService.buildSpecification(criteria, UAP050INHistory.class);
		spec = dynamicQueryService.addAcceptedCondition(spec, accepted);

		return uAP050INFransaBankHistoryRepository.findAll(spec, PageRequest.of(page, size, Sort.by(orders)));
	}

	@PostMapping("findUAP051INEr")
	public Page<UAP051IN> getUAP051INEr(@RequestBody(required = false) Map<String, String> uap,
			@RequestParam(name = "page", defaultValue = "0") int page,
			@RequestParam(name = "size", defaultValue = "5", required = false) int size,
			@RequestParam(name = "sortOn", defaultValue = "code", required = false) String sortOn,
			@RequestParam(name = "dir", defaultValue = "asc", required = false) String dir) {

		List<Order> orders = new ArrayList<Order>();
		Order order1 = dir.equals("desc") 
			? new Order(Sort.Direction.DESC, sortOn) 
			: new Order(Sort.Direction.ASC, sortOn);
		orders.add(order1);

		String accepted = uap.get("accepted");
		Map<String, String> criteria = new java.util.HashMap<>(uap);
		criteria.remove("accepted");
		
		Specification<UAP051IN> spec = dynamicQueryService.buildSpecification(criteria, UAP051IN.class);
		spec = dynamicQueryService.addAcceptedCondition(spec, accepted);

		return uAP051INRepository.findAll(spec, PageRequest.of(page, size, Sort.by(orders)));
	}
	
	
	@PostMapping("findUAP051INErArchive")
	public Page<UAP051INHistory> getUAP051INErArchive(@RequestBody(required = false) Map<String, String> uap,
			@RequestParam(name = "page", defaultValue = "0") int page,
			@RequestParam(name = "size", defaultValue = "5", required = false) int size,
			@RequestParam(name = "sortOn", defaultValue = "code", required = false) String sortOn,
			@RequestParam(name = "dir", defaultValue = "asc", required = false) String dir) {

		List<Order> orders = new ArrayList<Order>();
		Order order1 = dir.equals("desc") 
			? new Order(Sort.Direction.DESC, sortOn) 
			: new Order(Sort.Direction.ASC, sortOn);
		orders.add(order1);

		String accepted = uap.get("accepted");
		Map<String, String> criteria = new java.util.HashMap<>(uap);
		criteria.remove("accepted");
		
		Specification<UAP051INHistory> spec = dynamicQueryService.buildSpecification(criteria, UAP051INHistory.class);
		spec = dynamicQueryService.addAcceptedCondition(spec, accepted);

		return uAP051INFransaBankHistoryRepository.findAll(spec, PageRequest.of(page, size, Sort.by(orders)));
	}

	@PostMapping("findUAP040INNotMatching")
	public Page<UAP040IN> getUAP040INNotMatching(@RequestBody(required = false) Map<String, String> uap,
			@RequestParam(name = "page", defaultValue = "0") int page,
			@RequestParam(name = "size", defaultValue = "5", required = false) int size,
			@RequestParam(name = "sortOn", defaultValue = "code", required = false) String sortOn,
			@RequestParam(name = "dir", defaultValue = "asc", required = false) String dir) {

		List<Order> orders = new ArrayList<Order>();
		Order order1 = null;
		if (dir.equals("desc"))
			order1 = new Order(Sort.Direction.DESC, sortOn);
		else
			order1 = new Order(Sort.Direction.ASC, sortOn);
		orders.add(order1);
		if (size < 0) {

			return uAP040INBankRepository.filterNotMatched(
					PageRequest.of(0, Long.valueOf(uAP040INBankRepository.count()).intValue(), Sort.by(orders)),
					uap.get("typeTransaction"), uap.get("typePaiement"), uap.get("numRIBcommercant"),
					uap.get("numContratAccepteur"), uap.get("codeBin"), uap.get("numTransaction"),
					uap.get("dateTransactionS"), uap.get("dateTransactionE"), uap.get("dateDebutValiditeCarteS"),
					uap.get("dateDebutValiditeCarteE"), uap.get("dateFinValiditeCarteS"),
					uap.get("dateFinValiditeCarteE"), uap.get("dateRemiseS"), uap.get("dateRemiseE"),
					uap.get("numAutorisation"), uap.get("accepted"));
		} else {

			return uAP040INBankRepository.filterNotMatched(PageRequest.of(page, size, Sort.by(orders)),
					uap.get("typeTransaction"), uap.get("typePaiement"), uap.get("numRIBcommercant"),
					uap.get("numContratAccepteur"), uap.get("codeBin"), uap.get("numTransaction"),
					uap.get("dateTransactionS"), uap.get("dateTransactionE"), uap.get("dateDebutValiditeCarteS"),
					uap.get("dateDebutValiditeCarteE"), uap.get("dateFinValiditeCarteS"),
					uap.get("dateFinValiditeCarteE"), uap.get("dateRemiseS"), uap.get("dateRemiseE"),
					uap.get("numAutorisation"), uap.get("accepted"));
		}
	}

	@PostMapping("findUAP050INNotMatching")
	public Page<UAP050IN> getUAP050INNotMatching(@RequestBody(required = false) Map<String, String> uap,
			@RequestParam(name = "page", defaultValue = "0") int page,
			@RequestParam(name = "size", defaultValue = "5", required = false) int size,
			@RequestParam(name = "sortOn", defaultValue = "code", required = false) String sortOn,
			@RequestParam(name = "dir", defaultValue = "asc", required = false) String dir) {

		List<Order> orders = new ArrayList<Order>();
		Order order1 = null;
		if (dir.equals("desc"))
			order1 = new Order(Sort.Direction.DESC, sortOn);
		else
			order1 = new Order(Sort.Direction.ASC, sortOn);
		orders.add(order1);
		if (size < 0) {

			return uAP050INRepository.filterNotMatched( PageRequest.of(0, Long.valueOf(uAP050FransaBankRepository.count()).intValue(), Sort.by(orders)),
				 
					uap.get("typeTransaction"), uap.get("typePaiement"), uap.get("numRIBcommercant"),
					uap.get("numContratAccepteur"), uap.get("codeBin"), uap.get("numTransaction"),
					uap.get("dateTransactionS"), uap.get("dateTransactionE"), uap.get("dateDebutValiditeCarteS"),
					uap.get("dateDebutValiditeCarteE"), uap.get("dateFinValiditeCarteS"),
					uap.get("dateFinValiditeCarteE"), uap.get("dateRemiseS"), uap.get("dateRemiseE"),
					uap.get("numAutorisation"), uap.get("accepted"));
		} else {

			return uAP050INRepository.filterNotMatched(PageRequest.of(page, size, Sort.by(orders)),
					uap.get("typeTransaction"), uap.get("typePaiement"), uap.get("numRIBcommercant"),
					uap.get("numContratAccepteur"), uap.get("codeBin"), uap.get("numTransaction"),
					uap.get("dateTransactionS"), uap.get("dateTransactionE"), uap.get("dateDebutValiditeCarteS"),
					uap.get("dateDebutValiditeCarteE"), uap.get("dateFinValiditeCarteS"),
					uap.get("dateFinValiditeCarteE"), uap.get("dateRemiseS"), uap.get("dateRemiseE"),
					uap.get("numAutorisation"), uap.get("accepted"));
		}
//					uAP050FransaBankRepository.findAll(PageRequest.of(page, size,Sort.by(orders)));

	}

	@PostMapping("findUAP051INNotMatching")
	public Page<UAP051IN> getUAP051INNotMatching(@RequestBody(required = false) Map<String, String> uap,
			@RequestParam(name = "page", defaultValue = "0") int page,
			@RequestParam(name = "size", defaultValue = "5", required = false) int size,
			@RequestParam(name = "sortOn", defaultValue = "code", required = false) String sortOn,
			@RequestParam(name = "dir", defaultValue = "asc", required = false) String dir) {

		List<Order> orders = new ArrayList<Order>();
		Order order1 = null;
		if (dir.equals("desc"))
			order1 = new Order(Sort.Direction.DESC, sortOn);
		else
			order1 = new Order(Sort.Direction.ASC, sortOn);
		orders.add(order1);
		if (size < 0) {

			return uAP051INRepository.filterNotMatched(
					PageRequest.of(0, (int) uAP051FransaBankRepository.count(), Sort.by(orders)),
					uap.get("typeTransaction"), uap.get("typePaiement"), uap.get("numRIBcommercant"),
					uap.get("numContratAccepteur"), uap.get("codeBin"), uap.get("numTransaction"),
					uap.get("dateTransactionS"), uap.get("dateTransactionE"), uap.get("dateDebutValiditeCarteS"),
					uap.get("dateDebutValiditeCarteE"), uap.get("dateFinValiditeCarteS"),
					uap.get("dateFinValiditeCarteE"), uap.get("dateRemiseS"), uap.get("dateRemiseE"),
					uap.get("numAutorisation"), uap.get("accepted"));
		} else {

			return uAP051INRepository.filterNotMatched(PageRequest.of(page, size, Sort.by(orders)),
					uap.get("typeTransaction"), uap.get("typePaiement"), uap.get("numRIBcommercant"),
					uap.get("numContratAccepteur"), uap.get("codeBin"), uap.get("numTransaction"),
					uap.get("dateTransactionS"), uap.get("dateTransactionE"), uap.get("dateDebutValiditeCarteS"),
					uap.get("dateDebutValiditeCarteE"), uap.get("dateFinValiditeCarteS"),
					uap.get("dateFinValiditeCarteE"), uap.get("dateRemiseS"), uap.get("dateRemiseE"),
					uap.get("numAutorisation"), uap.get("accepted"));
		}
//					uAP050FransaBankRepository.findAll(PageRequest.of(page, size,Sort.by(orders)));

	}

	
	
	@PostMapping("findUAP040INCorrection")
	public Page<UAP040IN> getUAP040INCorrection( 
			@RequestParam(name = "page", defaultValue = "0") int page,
			@RequestParam(name = "size", defaultValue = "5", required = false) int size,
			@RequestParam(name = "sortOn", defaultValue = "code", required = false) String sortOn,
			@RequestParam(name = "dir", defaultValue = "asc", required = false) String dir) {

		List<Order> orders = new ArrayList<Order>();
		Order order1 = null;
		if (dir.equals("desc"))
			order1 = new Order(Sort.Direction.DESC, sortOn);
		else
			order1 = new Order(Sort.Direction.ASC, sortOn);
		orders.add(order1);
	 

			return uAP040INBankRepository.filterPending(PageRequest.of(page, size, Sort.by(orders)) );
	 
	}

	@PostMapping("findUAP050INCorrection")
	public Page<UAP050IN> getUAP050INCorrection( 
			@RequestParam(name = "page", defaultValue = "0") int page,
			@RequestParam(name = "size", defaultValue = "5", required = false) int size,
			@RequestParam(name = "sortOn", defaultValue = "code", required = false) String sortOn,
			@RequestParam(name = "dir", defaultValue = "asc", required = false) String dir) {

		List<Order> orders = new ArrayList<Order>();
		Order order1 = null;
		if (dir.equals("desc"))
			order1 = new Order(Sort.Direction.DESC, sortOn);
		else
			order1 = new Order(Sort.Direction.ASC, sortOn);
		orders.add(order1);
		if (size < 0) {

			return uAP050INRepository.filterPending(PageRequest.of(page, size, Sort.by(orders)) );
		} else {

			return uAP050INRepository.filterPending(PageRequest.of(page, size, Sort.by(orders)) );
		}
//					uAP050FransaBankRepository.findAll(PageRequest.of(page, size,Sort.by(orders)));

	}

	@PostMapping("findUAP051INCorrection")
	public Page<UAP051IN> getUAP051INCorrection( 
			@RequestParam(name = "page", defaultValue = "0") int page,
			@RequestParam(name = "size", defaultValue = "5", required = false) int size,
			@RequestParam(name = "sortOn", defaultValue = "code", required = false) String sortOn,
			@RequestParam(name = "dir", defaultValue = "asc", required = false) String dir) {

		List<Order> orders = new ArrayList<Order>();
		Order order1 = null;
		if (dir.equals("desc"))
			order1 = new Order(Sort.Direction.DESC, sortOn);
		else
			order1 = new Order(Sort.Direction.ASC, sortOn);
		orders.add(order1);
		if (size < 0) {

			return uAP051INRepository.filterPending(PageRequest.of(page, size, Sort.by(orders)) );
		} else {

			return uAP051INRepository.filterPending(PageRequest.of(page, size, Sort.by(orders)) );
		}
//					uAP050FransaBankRepository.findAll(PageRequest.of(page, size,Sort.by(orders)));

	}
	
}
