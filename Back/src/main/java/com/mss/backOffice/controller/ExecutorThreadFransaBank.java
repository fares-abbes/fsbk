package com.mss.backOffice.controller;

import com.google.common.base.Throwables;
import com.google.gson.Gson;
import com.mss.backOffice.request.UapIn;
import com.mss.backOffice.services.DayOperationCardSequenceService;
import com.mss.unified.entities.*;
import com.mss.unified.repositories.*;

import org.apache.any23.plugin.Author;
import org.apache.commons.beanutils.PropertyUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

@RestController
@RequestMapping("executorThreadFransaBank")
public class ExecutorThreadFransaBank {

	public static boolean stopThreads = false;

	@Autowired
	OpeningDayRepository OpeningDayRepo;

	@Autowired
	BkmvtiFransaBankRepository bkmRepo;
	@Autowired
	BkmHistoryRepository bkmHistoryRepo;
	@Autowired
	ExecutorBKMMouvement executorBKMMouvement;
	@Autowired
	public SettelementFransaBankRepository settlementRepo;
	@Autowired
	public CommissionFransaBankRepository commissionFransaRepo;

	@Autowired
	public TvaCommissionFransaBankRepository tvaCommissionRepo;
	@Autowired
	public BatchesFFCRepository batchesFFCRepo;
	@Autowired
	public DayOperationFransaBankRepository operationRepo;
	@Autowired
	public HistoriqueDayFransaBankRepository historiqueRepo;
	@Autowired
	public FILECONTENTRepository fileRepository;
	@Autowired
	public FileRepository tp_detail;
	@Autowired
	public SyntheseRepository syntheseRepository;
	@Autowired
	public SyntheseSummaryRepository summaryRepository;
	private static final Gson gson = new Gson();

	@Autowired
	TvaCommissionFransaBankRepository tvaRepo;
	@Autowired
	BatchesFFCRepository batchRepo;
	@Autowired
	CommissionAchatFransaBankRepository commissionAchatFransaBankRepository;
	@Autowired
	CommissionFransabankOnUsRepository commissionFransabankOnUsRepository;
	@Autowired
	PourcentageCommFSBKRepository pourcentageCommFSBKRepository;
	@Autowired
	CommissionFransaBankInternetRepository commissionFransaBankInternetRepository;

	@Autowired
	UAPFransaBankHistoryRepository uAPFransaBankHistoryRepository;

	@Autowired
	UAPFransaBankRepository UAPFransaBankRep;

	@Autowired
	InternationalProcessingExecutor internationalProcessingExecutor;
	@Autowired
	UAP050INFransaBankRepository UAP050INFransaBankRepo;
	@Autowired
	UAP051FransaBankRepository UAP051FransaBankRepo;
	@Autowired
	UAP051INFransaBankRepository UAP051InFransaBankRepo;

	@Autowired
	UAP051INFransaBankHistoryRepository uap051InHistory;
	@Autowired
	UAP051FransaBankHistoryRepository uap051History;
	@Autowired
	UAP050INFransaBankHistoryRepository UAP050INFransaBankHistoryRepo;

	@Autowired
	UAP050FransaBankRepository UAP050FransaBankRepo;

	@Autowired
	UAP050FransaBankHistoryRepository UAP050FransaBankHistoryRepo;

	@Autowired
	UAP040INFransaBankRepository UAP040INFransaBankRepo;

	@Autowired
	UAP040INFransaBankHistoryRepository UAP040INFransaBankHistoryRepo;
	@Autowired
	MargeCommissionRepository margeCommissionRepository;
	@Autowired
	UserRepository userRepository;
	@Autowired
	ExecutorSwitch execSwitch;
	@Autowired
	BankFransaRepository bankFransaRepository;
	@Autowired
	AtmTerminalRepository atmRepo;

	@Autowired
	CardRepository cardRepository;
	@Autowired
	MerchantRepository merchantRepository;
	@Autowired
	AccountRepository accountRepo;
	@Autowired
	ReglementGlobalController reglementGlobal;
	@Autowired
	FransaBank040Reglement reglement040;
	@Autowired
	FransaBank050Reglement reglement050;
	@Autowired
	FransaBank051Reglement reglement051;
	@Autowired
	FransaBank040INController reglement040In;
	@Autowired
	FransaBank050INController reglement050In;
	@Autowired
	FransaBank051INController reglement051In;
	@Autowired
	UAP040INFransaBankRepository uap040INRep;

	@Autowired
	UAP050INFransaBankRepository uap050INRep;
	@Autowired
	UAP051INFransaBankRepository uap051INRep;
	public static List<Integer> Test = new ArrayList<Integer>();
	public static List<String[]> debit;
	public static List<String[]> credit;
	public static List<CommissionFransaBank> commissionfransa;
	public static List<SettelementFransaBank> settlementfransa;
	public static List<TvaCommissionFransaBank> tvaCommission;
	public static List<AtmTerminal> atmTerminal;
	public static List<Account> account;
	private static final Logger logger = LoggerFactory.getLogger(ExecutorThreadFransaBank.class);
	private static final String SATATUS_VALID = "AP";
	public static boolean catchException;
	@Autowired
	CheckCroController040 chk040In;

	@Autowired
	CheckCroController050 chk050In;

	@Autowired
	CheckCroController051 chk051In;

	public static List<DayOperationFransaBank> daysOperationsAll = new ArrayList<DayOperationFransaBank>();
	public static List<FileContent> tpList = new ArrayList<FileContent>();
	public static List<FileContent> modifiedrows = new ArrayList<FileContent>();

	@Autowired
	DayOperationCardSequenceService dayService;
	@GetMapping("byPassCRO")
	@Transactional
	public void byPassCRO() throws IOException, ParseException, InterruptedException {
		batchesFFCRepo.updateFinishBatch("CRAUAP40IN", 0, new Date());
		batchesFFCRepo.updateFinishBatch("CRAUAP50IN", 0, new Date());
		batchesFFCRepo.updateFinishBatch("CRAUAP51IN", 0, new Date());
		BatchesFC execstatus = batchRepo.findByKey("execStatus").get();
		execstatus.setBatchNumber(0);
		batchRepo.save(execstatus);
		batchesFFCRepo.updateFinishBatch("TP", -1, new Date());
		batchesFFCRepo.updateFinishBatch("ByPassTP", -1, new Date());
		batchesFFCRepo.updateFinishBatch("CRAUAP40IN", 1, new Date());
		batchesFFCRepo.updateFinishBatch("CRAUAP50IN", 1, new Date());
		batchesFFCRepo.updateFinishBatch("CRAUAP51IN", 1, new Date());
	
	}

	@Autowired
	dayOperationReglementRepository dayReglementRepo;
	@Autowired
	FileUsedRepository fUR;
	@Autowired
	DownloadFileBc fbc;

	private final ReentrantLock lock = new ReentrantLock();

	@GetMapping("listFiles")
	public List<FileUsed> listFiles() {
		OpeningDay day = OpeningDayRepo.findAll().get(0);
		List<FileUsed> elements = fUR.findByFileIntegration(day.getFileIntegration());
		return elements;
	}

	public void setValues() {

		// debit=settlementRepo.getListDebit();
		// credit=settlementRepo.getListCredit();
		commissionfransa = commissionFransaRepo.findAll();
		tvaCommission = tvaCommissionRepo.findAll();

	}

	@Async
	@GetMapping("ByPassTP")
	public void ByPassTP() throws ParseException {
		logger.info("ByPassTP");
		List<BatchesFC> batches = batchesFFCRepo.findAll();
		ExecutorThreadFransaBank.stopThreads=false;
		internationalProcessingExecutor.startAllStopOnError();

		BatchesFC execstatus = null;
		batches.forEach(v -> {
			if (v.getKey().equals("ByPassTP")) {
				LocalDateTime localDateTime = LocalDateTime.now();
				Date date = Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
				v.setBatchDate(date);
				v.setBatchLastExcution(v.getBatchEndDate());
				v.setBatchEndDate(null);
				v.setBatchStatus(0);
				v.setDateReg(null);
			} else if (v.getKey().equals("execStatus")) {
				v.setBatchNumber(1);
			} else {
				v.setBatchStatus(-1);
			}
		});
		execstatus = batchesFFCRepo.findByKey("execStatus").get();
		batchesFFCRepo.saveAll(batches);
		batchesFFCRepo.flush();
		batchesFFCRepo.updateFinishBatch("SENDLOT", 10, new Date());
		batchesFFCRepo.updateFinishBatch("SENDORD", 10, new Date());
		catchException = false;
		BatchesFC batch = null;

		batch = batchesFFCRepo.findByKey("ByPassTP").get();

		logger.info("save");
		if (!catchException) {
			FileRequest.print("here ", FileRequest.getLineNumber());

			commissionfransa = commissionFransaRepo.findAll();
			tvaCommission = tvaCommissionRepo.findAll();
			atmTerminal = atmRepo.findAll();
			account = accountRepo.findAll();
			FileRequest.print("here ", FileRequest.getLineNumber());
			try {
				// TODO HANDLE REJECT VERIFY BEFORE PROD
				FileRequest.print("here ", FileRequest.getLineNumber());
				// used temporarly to force the rejection
				handleReject();

			} catch (Exception e) {

				String stackTrace = Throwables.getStackTraceAsString(e);
				logger.info("Exception is=>{}", stackTrace);
				return;
			}
			try {
				FileRequest.print("here ", FileRequest.getLineNumber());
				generateReglement();
			} catch (Exception e) {

				String stackTrace = Throwables.getStackTraceAsString(e);
				logger.info("Exception is=>{}", stackTrace);
				return;
			}

			FileRequest.print("Loading  Card List", FileRequest.getLineNumber());
			List<Object[]> cards = cardRepository.findCardsAndAccounts();

			FileRequest.print("starting thread creation ", FileRequest.getLineNumber());
			String name = SecurityContextHolder.getContext().getAuthentication().getName();

			Optional<User> user = userRepository.findByUserNameOrUserEmail(name, name);
			if (!user.isPresent()) {
				throw new RuntimeException("Error saving status !");
			}
			FileRequest.print("delete op day", FileRequest.getLineNumber());

			boolean value = OpeningDayRepo.findByStatus040("doneCro").isPresent();
			if (value) {

				OpeningDay op = OpeningDayRepo.findByStatus040("doneCro").get();
				op.setStatus040("doneCra");
				op.setStatus050("doneCra");
				op.setStatus051("doneCra");
				OpeningDayRepo.save(op);
				OpeningDayRepo.flush();

			}
			FileRequest.print("done", FileRequest.getLineNumber());

			FileRequest.print("starting switch ", FileRequest.getLineNumber());
			// TODO decommenter preproduction
			execSwitch.dayOP();

		}
	}

	@Async
	@GetMapping("executeDayOp/{fileDate}")
	public void parallelDay(@PathVariable String fileDate) throws ParseException {
		logger.info("parallelDay");
		catchException = false;
		BatchesFC batch = null;
		ExecutorThreadFransaBank.stopThreads=false;

		internationalProcessingExecutor.startAllStopOnError();
		try {
			// batchesMPRepository.updateBatch("Execute", 0,new Date());
			batch = batchesFFCRepo.findByKey("Execute").get();
			logger.info("startingSave");
			batch.setBatchLastExcution(batch.getBatchEndDate());
			batch.setBatchStatus(0);
			batch.setBatchDate(new Date());
			batch.setError(null);
			batch.setErrorStackTrace(null);
			logger.info("startingSave" + batch);
			batchesFFCRepo.saveAndFlush(batch);
		} catch (Exception ex) {
			ex.printStackTrace();
			batchesFFCRepo.updateStatusAndErrorBatch("Execute", 2, ex.getMessage(), new Date(),
					ex.getStackTrace()[1].toString());
		}

		logger.info("save");
		if (!catchException) {
			FileRequest.print("here ", FileRequest.getLineNumber());

			commissionfransa = commissionFransaRepo.findAll();
			tvaCommission = tvaCommissionRepo.findAll();
			atmTerminal = atmRepo.findAll();
			account = accountRepo.findAll();
			FileRequest.print("here ", FileRequest.getLineNumber());
			try {
				// TODO HANDLE REJECT VERIFY BEFORE PROD
				FileRequest.print("here ", FileRequest.getLineNumber());
				// used temporarly to force the rejection
				handleReject();

			} catch (Exception e) {

				String stackTrace = Throwables.getStackTraceAsString(e);
				logger.info("Exception is=>{}", stackTrace);
				batchesFFCRepo.updateStatusAndErrorBatch("Execute", 2, e.getMessage(), new Date(),
						e.getStackTrace()[1].toString());
				return;
			}
			try {
				FileRequest.print("here ", FileRequest.getLineNumber());
				generateReglement();
			} catch (Exception e) {

				String stackTrace = Throwables.getStackTraceAsString(e);
				batchesFFCRepo.updateStatusAndErrorBatch("Execute", 2, e.getMessage(), new Date(),
						e.getStackTrace()[1].toString());
				logger.info("Exception is=>{}", stackTrace);
				return;
			}

			FileRequest.print("starting thread creation ", FileRequest.getLineNumber());
			String name = SecurityContextHolder.getContext().getAuthentication().getName();

			Optional<User> user = userRepository.findByUserNameOrUserEmail(name, name);
			if (!user.isPresent()) {

				throw new RuntimeException("Error saving status !");

			}
			FileRequest.print("delete op day", FileRequest.getLineNumber());

			OpeningDayRepo.deleteAll();

			FileRequest.print("done", FileRequest.getLineNumber());

			FileRequest.print("starting switch ", FileRequest.getLineNumber());
			// TODO decommenter preproduction
			try {
				execSwitch.dayOP();
				FileRequest.print("execSwitch.dayOP() ", FileRequest.getLineNumber());
				FileRequest.print("stopThreads "+stopThreads, FileRequest.getLineNumber());

				if (stopThreads) {
					throw new Exception("error in switch dayOp");

				}

			} catch (Exception e) {
				batchesFFCRepo.updateStatusAndErrorBatch("Execute", 2, e.getMessage(), new Date(),
						Throwables.getStackTraceAsString(e));
				String stackTrace = Throwables.getStackTraceAsString(e);
				logger.info("Exception is=>{}", stackTrace);
				return;
			}

			FileRequest.print("swith ended ", FileRequest.getLineNumber());

			ArrayList<Thread> threads = new ArrayList<Thread>();
			ArrayList<Runnable> runn = new ArrayList<Runnable>();
			FileRequest.print("start finding files", FileRequest.getLineNumber());

			List<FileHeader> fl = tp_detail.findByStatusFileIsNull();
			int size = 0;
			for (FileHeader header : fl) {
				FileRequest.print("------ " + header.getFileDate(), FileRequest.getLineNumber());
				FileRequest.print("------ " + stopThreads, FileRequest.getLineNumber());
				int starter = 0;
				if (stopThreads) {
					break;
				}
				for (int i = size; i < (size + 39); i++) {
					try {
						Runnable r = new DayOpFransaBankThread(merchantRepository, commissionFransaRepo, settlementRepo,
								operationRepo, fileRepository, Integer.toString(starter + 1), header.getFileDate(),
								tp_detail, tvaRepo, batchRepo, commissionAchatFransaBankRepository,
								commissionFransabankOnUsRepository, pourcentageCommFSBKRepository,
								commissionFransaBankInternetRepository, margeCommissionRepository, bankFransaRepository,
								atmTerminal, account);
						runn.add(r);
						Thread t = new Thread(r);
						t.setName("DayOPN: " + Integer.toString(i + 1));
						t.start();
						threads.add(t);
						starter++;
					} catch (Exception e) {
						stopThreads = true;
						throw e;
					}
					if (stopThreads) {
						break;
					}
				}

				size = +35;
				header.setStatusFile("integrated");
			}
			// Wait for the threads to finish
			tp_detail.saveAll(fl);
			Thread waitThread = new Thread(() -> {
				try {
					for (int i = 0; i < threads.size(); i++) {
						try {
							checkerrors();

							threads.get(i).join();
						} catch (InterruptedException e) {
							batchesFFCRepo.updateStatusAndErrorBatch("Execute", 2, e.getMessage(), new Date(),
									e.getStackTrace()[1].toString());
							stopThreads = true;
							throw new Exception("error in dayOp");
						}
					}
					FileRequest.print("Day Operation ended with sucess " + daysOperationsAll.size() + "size",
							FileRequest.getLineNumber());
					if (DayOpFransaBankThread.allowEdit) {
						fileRepository.saveAll(tpList);
						operationRepo.saveAll(daysOperationsAll);
						daysOperationsAll.clear();
						FileRequest.print("Day Operation clearing ended with sucess " + daysOperationsAll.size(),
								FileRequest.getLineNumber());
						String fileIntegration = openingDate();
						executorBKMMouvement.parallelGBanciare(user.get().getUserName(), fileIntegration);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			});
			waitThread.setName("replyStatus");
			waitThread.start();

		}
	}

	private void checkerrors() throws Exception {
		if (ExecutorThreadFransaBank.stopThreads) {
			throw new Exception("error in dayOp");
		}
	}

	@Transactional
	void generateReglement() throws IOException, ParseException {
		reglement040.validFile();
		reglement050.validFile();
		reglement051.validFile();

		reglement040In.generateNextDay();
		reglement050In.reglement050In();
		reglement051In.reglement050In();
		reglementGlobal.validFile();
	}

	@Transactional
	@GetMapping("generateReglementTest")
	void generateReglementTest() throws IOException, ParseException {
		reglement040.validFile();
		reglement050.validFile();
		reglement051.validFile();
		reglement040In.generateNextDay();
		reglement050In.reglement050In();
		reglement051In.reglement050In();
//		reglementGlobal.validFile();
	}

	@Transactional
	@GetMapping("generateReglementGTest")
	void generateReglementGTest() throws IOException, ParseException {

		reglementGlobal.validFile();
	}

	@Transactional
	@GetMapping("handleReject")
	void handleReject() {
		chk040In.forceReject();
		chk050In.forceReject();
		chk051In.forceReject();
//		chk040In.forceAccept();
//		chk050In.forceAccept();
//		chk051In.forceAccept();
//		if (batchesFFCRepo.findByKey("CANCELREJECTION").get().getBatchStatus()==-1) {
//			try {
//				uap040INRep.findByFlag(SATATUS_VALID);
//			} catch (IOException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//		}
	}

	public String openingDate() {
		OpeningDay opening = new OpeningDay();
		Date date = new Date();
		SimpleDateFormat formatter = new SimpleDateFormat("MMddyyyy");
		String fileIntegration = formatter.format(date);
		opening.setFileIntegration(fileIntegration);
		OpeningDayRepo.save(opening);
		return fileIntegration;
	}

	public BigDecimal checkElement(BigDecimal el) {
		return (el == null) ? new BigDecimal(0) : el.divide(new BigDecimal(100.00));
	}

	public void remplirSwith(List<Synthese> snList, String dateS, SyntheseSummary snt) {
		Map<String, BigDecimal> data = findbyList();
		// ONUS
		Synthese sn = new Synthese();

		sn.setIdHeader(new Integer(snt.getCode()));

		sn.setTransactionType("RetraitMAgence");
		sn.setNb(operationRepo.countByIdentification("S001"));
		sn.setOnUsOffUs("100");
		sn.setMontant(checkElement(data.get("S001")));
		sn.setProduit(
				checkElement(data.get("S003")).add(checkElement(data.get("S002"))).subtract(checkElement(sn.getCmi())));
		syntheseRepository.saveAndFlush(sn);
		snList.add(sn);

		// --
		sn = new Synthese();
		sn.setIdHeader(new Integer(snt.getCode()));

		sn.setTransactionType("RetraitAAgence");
		sn.setNb(operationRepo.countByIdentification("S004"));
		sn.setOnUsOffUs("100");

		sn.setMontant(checkElement(data.get("S004")));
		sn.setProduit(
				checkElement(data.get("S005")).add(checkElement(data.get("S006"))).subtract(checkElement(sn.getCmi())));
		syntheseRepository.saveAndFlush(sn);

		snList.add(sn);
		// AchatMAgence
		sn = new Synthese();
		sn.setIdHeader(new Integer(snt.getCode()));

		sn.setTransactionType("AchatMAgence");
		sn.setNb(operationRepo.countByIdentification("S012"));
		sn.setOnUsOffUs("100");

		sn.setMontant(checkElement(data.get("S012")));
		sn.setProduit(checkElement(data.get("S013")).add(checkElement(data.get("S014"))));
		syntheseRepository.saveAndFlush(sn);

		snList.add(sn);
		// AchatAAgence
		sn = new Synthese();
		sn.setIdHeader(new Integer(snt.getCode()));

		sn.setTransactionType("AchatAAgence");
		sn.setNb(operationRepo.countByIdentification("S015"));
		sn.setOnUsOffUs("100");

		sn.setMontant(checkElement(data.get("S015")));
		sn.setProduit(checkElement(data.get("S016")).add(checkElement(data.get("S017"))));

		syntheseRepository.saveAndFlush(sn);

		snList.add(sn);
		// internet mAgence
		sn = new Synthese();
		sn.setIdHeader(new Integer(snt.getCode()));

		sn.setTransactionType("InternetMAgence");
		sn.setNb(operationRepo.countByIdentification("S019"));
		sn.setOnUsOffUs("100");

		sn.setMontant(checkElement(data.get("S019")));
		sn.setProduit(checkElement(data.get("S020")).add(checkElement(data.get("S021"))));
		syntheseRepository.saveAndFlush(sn);

		snList.add(sn);
		// internet AAgence

		sn = new Synthese();
		sn.setIdHeader(new Integer(snt.getCode()));

		sn.setTransactionType("InternetAutreAgence");
		sn.setNb(operationRepo.countByIdentification("S022"));
		sn.setOnUsOffUs("100");
		sn.setMontant(checkElement(data.get("S022")));
		sn.setProduit(checkElement(data.get("S023")).add(checkElement(data.get("S024"))));
		syntheseRepository.saveAndFlush(sn);

		snList.add(sn);
		// R rtpe MMAgence

		sn = new Synthese();
		sn.setIdHeader(new Integer(snt.getCode()));

		sn.setTransactionType("RembourssementTPEMemeAgence");
		sn.setNb(operationRepo.countByIdentification("S026"));
		sn.setOnUsOffUs("100");

		sn.setMontant(checkElement(data.get("S026")));
		sn.setProduit(checkElement(data.get("S027")).add(checkElement(data.get("S028"))));
		syntheseRepository.saveAndFlush(sn);

		snList.add(sn);
		// R rtpe AAgence
		sn = new Synthese();
		sn.setIdHeader(new Integer(snt.getCode()));
		sn.setTransactionType("RembourssementTPEAutreAgence");
		sn.setNb(operationRepo.countByIdentification("S029"));
		sn.setOnUsOffUs("100");
		sn.setMontant(checkElement(data.get("S029")));
		sn.setProduit(checkElement(data.get("S030")).add(checkElement(data.get("S031"))));
		syntheseRepository.saveAndFlush(sn);

		snList.add(sn);
		// remboursement internet MM AGence

		sn = new Synthese();
		sn.setIdHeader(new Integer(snt.getCode()));
		sn.setTransactionType("remboursementInternetMA");
		sn.setNb(operationRepo.countByIdentification("S033"));
		sn.setOnUsOffUs("100");
		sn.setMontant(checkElement(data.get("S033")));
		sn.setProduit(checkElement(data.get("S034")).add(checkElement(data.get("S035"))));
		syntheseRepository.saveAndFlush(sn);

		snList.add(sn);
		// remboursement internet autre agences

		sn = new Synthese();
		sn.setIdHeader(new Integer(snt.getCode()));
		sn.setTransactionType("remboursementInternetAA");
		sn.setNb(operationRepo.countByIdentification("S036"));
		sn.setOnUsOffUs("100");
		sn.setMontant(checkElement(data.get("S036")));
		sn.setProduit(
				checkElement(data.get("S037")).add(checkElement(data.get("S038"))).subtract(checkElement(sn.getCmi())));
		syntheseRepository.saveAndFlush(sn);

		snList.add(sn);
		// Consultation solde MM AGence

		sn = new Synthese();
		sn.setIdHeader(new Integer(snt.getCode()));
		sn.setTransactionType("csMA");
		sn.setNb(operationRepo.countByIdentification("S008"));
		sn.setOnUsOffUs("100");
		sn.setMontant(checkElement(data.get("S008")));
		sn.setProduit(checkElement(data.get("S008")).add(checkElement(data.get("S009"))));
		syntheseRepository.saveAndFlush(sn);

		snList.add(sn);
		// Consultation solde autre agences

		sn = new Synthese();
		sn.setIdHeader(new Integer(snt.getCode()));
		sn.setTransactionType("csAA");
		sn.setNb(operationRepo.countByIdentification("S010"));
		sn.setOnUsOffUs("100");
		sn.setMontant(checkElement(data.get("S010")));
		sn.setProduit(checkElement(data.get("S010")).add(checkElement(data.get("S011"))));
		syntheseRepository.saveAndFlush(sn);

		snList.add(sn);
	}

	public void remplirExceptionBanque(List<Synthese> snts, String dateS, SyntheseSummary snt) {

		List<String> banquesCommissionList = commissionFransaRepo.findBankList();
		Map<String, Map<String, BigDecimal>> banksData = findbyListByBank(banquesCommissionList);

		FileRequest.print(banksData.toString(), FileRequest.getLineNumber());
		FileRequest.print(banquesCommissionList.size() + " ", FileRequest.getLineNumber());

		for (String bank : banquesCommissionList) {
			Map<String, BigDecimal> data = banksData.get(bank);
//					get liste des G dont la banque  est inthe range list
//					iterate through list des banques
			// FSBK / ALP
			// --FSBK/ALP emission
			Synthese sn = new Synthese();
			sn.setIdHeader(new Integer(snt.getCode()));
			sn.setBanqueException(bank);
			sn.setTransactionType("Retrait-emission");
			sn.setNb(operationRepo.countByIdentification("G159"));
			sn.setOnUsOffUs("002");
			sn.setMontant(checkElement(data.get("G159")));
			sn.setInterchange(checkElement(data.get("G166")).add(checkElement(data.get("G167"))));
			sn.setCmi(checkElement(data.get("G164")).add(checkElement(data.get("G165"))));
			sn.setProduit(checkElement(data.get("G160")).add(checkElement(data.get("G016")))
					.subtract(checkElement(data.get("G164"))).subtract(checkElement(data.get("G166")))
					.subtract(checkElement(data.get("G021"))).subtract(checkElement(data.get("G019"))));
			syntheseRepository.saveAndFlush(sn);

			snts.add(sn);

			// --FSBK/ALP emission
			sn = new Synthese();
			sn.setIdHeader(new Integer(snt.getCode()));
			sn.setBanqueException(bank);

			sn.setTransactionType("Retrait--reception");
			sn.setNb(operationRepo.countByIdentification("G170"));
			sn.setOnUsOffUs("002");

			sn.setMontant(checkElement(data.get("G170")));
			sn.setInterchange(checkElement(data.get("G171")).add(checkElement(data.get("G172"))));

			sn.setCpi(checkElement(data.get("G173")).add(checkElement(data.get("G174"))));
			sn.setCmi(checkElement(data.get("G176")).add(checkElement(data.get("G177"))));

			sn.setProduit(checkElement(data.get("G171")).add(checkElement(data.get("G172"))).subtract(sn.getCpi()));
			syntheseRepository.saveAndFlush(sn);
			snts.add(sn);

		}

		List<String> banquesCommissionInternetList = commissionFransaBankInternetRepository.findBankList();

		Map<String, Map<String, BigDecimal>> banquesInternetList = findbyListByBank(banquesCommissionList);
		for (String bank : banquesCommissionList) {
			Map<String, BigDecimal> data = banksData.get(bank);
			Synthese sn = new Synthese();
			sn.setIdHeader(new Integer(snt.getCode()));
			sn.setBanqueException(bank);

			sn.setTransactionType("Internet-emission");
			sn.setNb(operationRepo.countByIdentification("G186"));
			sn.setOnUsOffUs("002");

			sn.setMontant(checkElement(data.get("G186")));
			sn.setInterchange(checkElement(data.get("G187")).add(checkElement(data.get("G188"))));

			sn.setCmi(checkElement(data.get("G192")));
			sn.setProduit(checkElement(data.get("G187")).add(checkElement(data.get("G188")))
					.subtract(checkElement(data.get("G189"))).subtract(checkElement(data.get("G190"))));
			syntheseRepository.saveAndFlush(sn);

			snts.add(sn);

			sn = new Synthese();
			sn.setIdHeader(new Integer(snt.getCode()));
			sn.setBanqueException(bank);

			sn.setTransactionType("Internet-reception");
			sn.setNb(operationRepo.countByIdentification("G178"));
			sn.setOnUsOffUs("002");

			sn.setMontant(checkElement(data.get("G178")));
			sn.setProduit(checkElement(data.get("G179")).add(checkElement(data.get("G180")))
					.subtract(checkElement(data.get("G181"))).subtract(checkElement(data.get("G182")))
					.subtract(checkElement(data.get("G183"))).subtract(checkElement(data.get("G184"))));

			sn.setCmi(new BigDecimal("0"));
			sn.setCpi(checkElement(data.get("G181")).add(checkElement(data.get("G182"))));
			sn.setInterchange(checkElement(data.get("G183")).add(checkElement(data.get("G184"))));

			syntheseRepository.saveAndFlush(sn);

			snts.add(sn);
		}
	}

	@GetMapping("/getListExceptionBanks")
	public ResponseEntity<?> getListExceptionBanks() {
		List<String> banquesCommissionList = commissionFransaRepo.findBankList();
		List<String> banquesCommissionInternetList = commissionFransaBankInternetRepository.findBankList();
		banquesCommissionList.addAll(banquesCommissionInternetList);
		HashSet<String> banks = new HashSet<>(banquesCommissionList);
		return ResponseEntity.ok().body(gson.toJson(banks));

	}

	@Transactional
	@GetMapping("/remplirTableSynthese")
	public ResponseEntity<?> remplirTableSynthese(
			@RequestParam(name = "dateSynthese", required = false) String dateSynthese) {
		String dateS;
		if (dateSynthese == null) {
			DateTimeFormatter dtf = DateTimeFormatter.ofPattern("YYYY-MM-dd");
			LocalDateTime now = LocalDateTime.now();
			dateS = dtf.format(now);

		} else {
			dateS = dateSynthese;

		}

		List<Synthese> snList = new ArrayList<>();

		if (!summaryRepository.findByDateSynthese(dateS).isPresent()) {
			Map<String, BigDecimal> data = findbyList();
			FileRequest.print(data.toString(), FileRequest.getLineNumber());
			Synthese sn = new Synthese();
			SyntheseSummary snt = new SyntheseSummary();
			System.out.println(data.size());
			System.out.println(data);
			// operationRepo
			snt.setDateSynthese(dateS);
			snt = summaryRepository.saveAndFlush(snt);
			remplirSwith(snList, dateS, snt);

			// ONUS
			sn.setIdHeader(new Integer(snt.getCode()));

			sn.setTransactionType("RetraitMAgence");
			sn.setNb(operationRepo.countByIdentification("G001"));
			sn.setOnUsOffUs("000");
			sn.setMontant(checkElement(data.get("G001")));
			sn.setCmi(checkElement(data.get("G054")).add(checkElement(data.get("G055"))));
			sn.setProduit(checkElement(data.get("G003")).add(checkElement(data.get("G002")))
					.subtract(checkElement(sn.getCmi())));
			syntheseRepository.saveAndFlush(sn);
			snList.add(sn);

			// --
			sn = new Synthese();
			sn.setIdHeader(new Integer(snt.getCode()));

			sn.setTransactionType("RetraitAAgence");
			sn.setNb(operationRepo.countByIdentification("G004"));
			sn.setOnUsOffUs("000");

			sn.setMontant(checkElement(data.get("G004")));
			sn.setCmi(checkElement(data.get("G056")).add(checkElement(data.get("G057"))));
			sn.setProduit(checkElement(data.get("G005")).add(checkElement(data.get("G006")))
					.subtract(checkElement(sn.getCmi())));
			syntheseRepository.saveAndFlush(sn);

			snList.add(sn);
			// AchatMAgence
			sn = new Synthese();
			sn.setIdHeader(new Integer(snt.getCode()));

			sn.setTransactionType("AchatMAgence");
			sn.setNb(operationRepo.countByIdentification("G041"));
			sn.setOnUsOffUs("000");

			sn.setMontant(checkElement(data.get("G041")));
			sn.setCmi(checkElement(data.get("G059")).add(checkElement(data.get("G060"))));
			sn.setProduit(checkElement(data.get("G042")).add(checkElement(data.get("G043")))
					.subtract(checkElement(data.get("G060"))).subtract(checkElement(data.get("G059"))));
			syntheseRepository.saveAndFlush(sn);

			snList.add(sn);
			// AchatAAgence
			sn = new Synthese();
			sn.setIdHeader(new Integer(snt.getCode()));

			sn.setTransactionType("AchatAAgence");
			sn.setNb(operationRepo.countByIdentification("G044"));
			sn.setOnUsOffUs("000");

			sn.setMontant(checkElement(data.get("G044")));
			sn.setCmi(checkElement(data.get("G062")).add(checkElement(data.get("G061"))));
			sn.setProduit(checkElement(data.get("G046")).add(checkElement(data.get("G045")))
					.subtract(checkElement(data.get("G062"))).subtract(checkElement(data.get("G061"))));
			syntheseRepository.saveAndFlush(sn);

			snList.add(sn);
			// internet mAgence
			sn = new Synthese();
			sn.setIdHeader(new Integer(snt.getCode()));

			sn.setTransactionType("InternetMAgence");
			sn.setNb(operationRepo.countByIdentification("G081"));
			sn.setOnUsOffUs("000");

			sn.setMontant(checkElement(data.get("G081")));
			sn.setCmi(checkElement(data.get("G084")).add(checkElement(data.get("G085"))));
			sn.setProduit(checkElement(data.get("G082")).add(checkElement(data.get("G083")))
					.subtract(checkElement(sn.getCmi())));
			syntheseRepository.saveAndFlush(sn);

			snList.add(sn);
			// internet AAgence

			sn = new Synthese();
			sn.setIdHeader(new Integer(snt.getCode()));

			sn.setTransactionType("InternetAutreAgence");
			sn.setNb(operationRepo.countByIdentification("G087"));
			sn.setOnUsOffUs("000");
			sn.setMontant(checkElement(data.get("G087")));
			sn.setCmi(checkElement(data.get("G091")).add(checkElement(data.get("G092"))));
			sn.setProduit(checkElement(data.get("G088")).add(checkElement(data.get("G089")))
					.subtract(checkElement(sn.getCmi())));
			syntheseRepository.saveAndFlush(sn);

			snList.add(sn);
			// R rtpe MMAgence

			sn = new Synthese();
			sn.setIdHeader(new Integer(snt.getCode()));

			sn.setTransactionType("RembourssementTPEMemeAgence");
			sn.setNb(operationRepo.countByIdentification("G103"));
			sn.setOnUsOffUs("000");

			sn.setMontant(checkElement(data.get("G103")));
			sn.setCmi(checkElement(data.get("G106")).add(checkElement(data.get("G107"))));
			sn.setProduit(checkElement(data.get("G104")).add(checkElement(data.get("G105")))
					.subtract(checkElement(sn.getCmi())));
			syntheseRepository.saveAndFlush(sn);

			snList.add(sn);
			// R rtpe AAgence
			sn = new Synthese();
			sn.setIdHeader(new Integer(snt.getCode()));
			sn.setTransactionType("RembourssementTPEAutreAgence");
			sn.setNb(operationRepo.countByIdentification("G108"));
			sn.setOnUsOffUs("000");
			sn.setMontant(checkElement(data.get("G108")));
			sn.setCmi(checkElement(data.get("G106")).add(checkElement(data.get("G107"))));
			sn.setProduit(checkElement(data.get("G109")).add(checkElement(data.get("G110")))
					.subtract(checkElement(sn.getCmi())));
			syntheseRepository.saveAndFlush(sn);

			snList.add(sn);
			// remboursement internet MM AGence

			sn = new Synthese();
			sn.setIdHeader(new Integer(snt.getCode()));
			sn.setTransactionType("remboursementInternetMA");
			sn.setNb(operationRepo.countByIdentification("G148"));
			sn.setOnUsOffUs("000");
			sn.setMontant(checkElement(data.get("G148")));
			sn.setCmi(checkElement(data.get("G151")).add(checkElement(data.get("G152"))));
			sn.setProduit(checkElement(data.get("G149")).add(checkElement(data.get("G150")))
					.subtract(checkElement(sn.getCmi())));
			syntheseRepository.saveAndFlush(sn);

			snList.add(sn);
			// remboursement internet autre agences

			sn = new Synthese();
			sn.setIdHeader(new Integer(snt.getCode()));
			sn.setTransactionType("remboursementInternetAA");
			sn.setNb(operationRepo.countByIdentification("G153"));
			sn.setOnUsOffUs("000");
			sn.setMontant(checkElement(data.get("G153")));
			sn.setCmi(checkElement(data.get("G156")).add(checkElement(data.get("G157"))));
			sn.setProduit(checkElement(data.get("G154")).add(checkElement(data.get("G155")))
					.subtract(checkElement(sn.getCmi())));
			syntheseRepository.saveAndFlush(sn);

			snList.add(sn);
			// Consultation solde MM AGence

			sn = new Synthese();
			sn.setIdHeader(new Integer(snt.getCode()));
			sn.setTransactionType("csMA");
			sn.setNb(operationRepo.countByIdentification("G114"));
			sn.setOnUsOffUs("000");
			sn.setMontant(checkElement(data.get("G114")));
			sn.setCmi(checkElement(data.get("G116")).add(checkElement(data.get("G117"))));
			sn.setProduit(checkElement(data.get("G114")).add(checkElement(data.get("G115")))
					.subtract(checkElement(sn.getCmi())));
			syntheseRepository.saveAndFlush(sn);

			snList.add(sn);
			// Consultation solde autre agences

			sn = new Synthese();
			sn.setIdHeader(new Integer(snt.getCode()));
			sn.setTransactionType("csAA");
			sn.setNb(operationRepo.countByIdentification("G118"));
			sn.setOnUsOffUs("000");
			sn.setMontant(checkElement(data.get("G118")));
			sn.setCmi(checkElement(data.get("G120")).add(checkElement(data.get("G121"))));
			sn.setProduit(checkElement(data.get("G118")).add(checkElement(data.get("G119")))
					.subtract(checkElement(sn.getCmi())));
			syntheseRepository.saveAndFlush(sn);

			snList.add(sn);
			// OFFUS

			// --Autre banques emission
			sn = new Synthese();
			sn.setIdHeader(new Integer(snt.getCode()));

			sn.setTransactionType("RetraitAutreBanques/fsbk-emission");
			sn.setNb(operationRepo.countByIdentification("G014"));
			sn.setOnUsOffUs("001");

			sn.setMontant(checkElement(data.get("G014")));
			sn.setInterchange(checkElement(data.get("G018")).add(checkElement(data.get("G019"))));

			sn.setCmi(checkElement(data.get("G020")).add(checkElement(data.get("G021"))));
			sn.setProduit(checkElement(data.get("G015")).add(checkElement(data.get("G016")))
					.subtract(checkElement(sn.getCmi())).subtract(sn.getInterchange()));
			syntheseRepository.saveAndFlush(sn);

			snList.add(sn);

			sn = new Synthese();
			sn.setIdHeader(new Integer(snt.getCode()));

			sn.setTransactionType("AchatAutreBanques/fsbk-emission");
			sn.setNb(operationRepo.countByIdentification("G034"));
			sn.setOnUsOffUs("001");

			sn.setMontant(checkElement(data.get("G034")));
			sn.setInterchange(checkElement(data.get("G035")).add(checkElement(data.get("G036"))));

			sn.setCmi(checkElement(data.get("G037")).add(checkElement(data.get("G038"))));
			sn.setProduit(checkElement(data.get("G036"))
					.add(checkElement(data.get("G035")).subtract(checkElement(sn.getCmi()))));
			syntheseRepository.saveAndFlush(sn);

			snList.add(sn);

			// --Autre banques reception (Aquisition)
			sn = new Synthese();
			sn.setIdHeader(new Integer(snt.getCode()));

			sn.setTransactionType("RetraitAutreBanques/fsbk-reception");
			sn.setNb(operationRepo.countByIdentification("G008"));
			sn.setOnUsOffUs("001");

			sn.setMontant(checkElement(data.get("G008")));
			sn.setInterchange(checkElement(data.get("G009")).add(checkElement(data.get("G010"))));

			sn.setCpi(checkElement(data.get("G011")).add(checkElement(data.get("G012"))));
			sn.setProduit(checkElement(data.get("G009")).add(checkElement(data.get("G010"))).subtract(sn.getCpi()));
			syntheseRepository.saveAndFlush(sn);

			snList.add(sn);

			sn = new Synthese();
			sn.setIdHeader(new Integer(snt.getCode()));

			sn.setTransactionType("AchatAutreBanques/fsbk-reception");
			sn.setNb(operationRepo.countByIdentification("G026"));
			sn.setOnUsOffUs("001");

			sn.setMontant(checkElement(data.get("G026")));
			sn.setInterchange(checkElement(data.get("G031")).add(checkElement(data.get("G032"))));

			sn.setCpi(checkElement(data.get("G029")).add(checkElement(data.get("G030"))));
			sn.setProduit(checkElement(data.get("G027"))
					.add(checkElement(data.get("G028")).subtract(sn.getCpi()).subtract(sn.getInterchange())));
			syntheseRepository.saveAndFlush(sn);

			snList.add(sn);
			remplirExceptionBanque(snList, dateS, snt);
//old cs autre banques
//			sn = new Synthese();
//			sn.setIdHeader(new Integer(snt.getCode()));
//
//			sn.setTransactionType("CSAutreBanques/fsbk-emission");
//			sn.setNb(operationRepo.countByIdentification("G127"));
//			sn.setOnUsOffUs("001");
//
//			sn.setMontant(new BigDecimal(0));
//			sn.setInterchange(checkElement(data.get("G133")));
//
//			sn.setCmi(checkElement(data.get("G131")).add(checkElement(data.get("G132"))));
//			sn.setProduit(checkElement(data.get("G129")).add(checkElement(data.get("G130"))).subtract(checkElement(sn.getCmi())));
//			syntheseRepository.saveAndFlush(sn);
//
//			snList.add(sn);
//
//			sn = new Synthese();
//			sn.setIdHeader(new Integer(snt.getCode()));
//
//			sn.setTransactionType("CSAutreBanques/fsbk-reception");
//			sn.setNb(operationRepo.countByIdentification("G122"));
//			sn.setOnUsOffUs("001");
//
//			sn.setMontant(new BigDecimal(0));
//			sn.setInterchange(checkElement(data.get("G122")).add(checkElement(data.get("G123"))));
//
//			sn.setCpi(checkElement(data.get("G124")).add(checkElement(data.get("G125"))));
//			sn.setProduit(checkElement(data.get("G122")).add(checkElement(data.get("G123"))).subtract(sn.getCpi()));
//			syntheseRepository.saveAndFlush(sn);
//
//			snList.add(sn);

			// Internet emission
			sn = new Synthese();
			sn.setIdHeader(new Integer(snt.getCode()));

			sn.setTransactionType("Internet/FSBK-emission");
			sn.setNb(operationRepo.countByIdentification("G074"));
			sn.setOnUsOffUs("001");

			sn.setMontant(checkElement(data.get("G074")));
			sn.setInterchange(checkElement(data.get("G075")).add(checkElement(data.get("G076"))));
			sn.setCmi(checkElement(data.get("G077")).add(checkElement(data.get("G078"))));
			sn.setCpi(BigDecimal.ZERO);
			sn.setProduit(checkElement(data.get("G075")).add(checkElement(data.get("G076")))
					.subtract(checkElement(data.get("G077"))).subtract(checkElement(data.get("G078"))));
			syntheseRepository.saveAndFlush(sn);
			snList.add(sn);

			// Internet acquisition
			sn = new Synthese();
			sn.setIdHeader(new Integer(snt.getCode()));

			sn.setTransactionType("Internet-reception");
			sn.setNb(operationRepo.countByIdentification("G066"));
			sn.setOnUsOffUs("001");

			sn.setMontant(checkElement(data.get("G066")));
			sn.setInterchange(checkElement(data.get("G071")).add(checkElement(data.get("G072"))));

			sn.setCpi(checkElement(data.get("G069")).add(checkElement(data.get("G070"))));
			sn.setProduit(checkElement(data.get("G071")).add(checkElement(data.get("G072")))
					.subtract(checkElement(data.get("G069"))).subtract(checkElement(data.get("G070"))));
			syntheseRepository.saveAndFlush(sn);
			snList.add(sn);

			// Remboursement emission
			sn = new Synthese();
			sn.setIdHeader(new Integer(snt.getCode()));

			sn.setTransactionType("Remboursement-emission");
			sn.setNb(operationRepo.countByIdentification("G097"));
			sn.setOnUsOffUs("001");

			sn.setMontant(checkElement(data.get("G097")));
			sn.setInterchange(checkElement(data.get("G098")).add(checkElement(data.get("G099"))));
			sn.setCmi(checkElement(data.get("G100")).add(checkElement(data.get("G101"))));
			sn.setProduit(checkElement(data.get("G098")).add(checkElement(data.get("G099")))
					.subtract(checkElement(data.get("G100"))).subtract(checkElement(data.get("G101"))));
			syntheseRepository.saveAndFlush(sn);
			snList.add(sn);

			// Remboursement acquisition
			sn = new Synthese();
			sn.setIdHeader(new Integer(snt.getCode()));

			sn.setTransactionType("Remboursement-reception");
			sn.setNb(operationRepo.countByIdentification("G048"));
			sn.setOnUsOffUs("001");

			sn.setMontant(checkElement(data.get("G048")));
			sn.setInterchange(checkElement(data.get("G094")).add(checkElement(data.get("G095"))));
			sn.setCpi(checkElement(data.get("G051")).add(checkElement(data.get("G052"))));
			sn.setProduit(checkElement(data.get("G049")).add(checkElement(data.get("G050")))
					.subtract(checkElement(data.get("G051"))).subtract(checkElement(data.get("G052")))
					.subtract(checkElement(data.get("G094"))).subtract(checkElement(data.get("G095"))));
			syntheseRepository.saveAndFlush(sn);
			snList.add(sn);

			// Remboursement internet emission
			sn = new Synthese();
			sn.setIdHeader(new Integer(snt.getCode()));

			sn.setTransactionType("RemboursementInternetother-emission");
			sn.setNb(operationRepo.countByIdentification("G142"));
			sn.setOnUsOffUs("001");

			sn.setMontant(checkElement(data.get("G142")));
			sn.setInterchange(checkElement(data.get("G143")).add(checkElement(data.get("G144"))));
			sn.setCmi(checkElement(data.get("G145")).add(checkElement(data.get("G146"))));
			sn.setProduit(checkElement(data.get("G143")).add(checkElement(data.get("G144")))
					.subtract(checkElement(data.get("G145"))).subtract(checkElement(data.get("G146"))));
			syntheseRepository.saveAndFlush(sn);
			snList.add(sn);
			// Remboursement internet reception
			sn = new Synthese();
			sn.setIdHeader(new Integer(snt.getCode()));

			sn.setTransactionType("RemboursementInternetother-reception");
			sn.setNb(operationRepo.countByIdentification("G134"));
			sn.setOnUsOffUs("001");

			sn.setMontant(checkElement(data.get("G134")));
			sn.setInterchange(checkElement(data.get("G139")).add(checkElement(data.get("G140"))));
			sn.setCpi(checkElement(data.get("G137")).add(checkElement(data.get("G138"))));
			sn.setProduit(checkElement(data.get("G135")).add(checkElement(data.get("G136"))).subtract(sn.getCpi())
					.subtract(sn.getInterchange()));
			syntheseRepository.saveAndFlush(sn);
			snList.add(sn);
			// consultation solde emission
			sn = new Synthese();
			sn.setIdHeader(new Integer(snt.getCode()));

			sn.setTransactionType("CSOther-emission");
			sn.setNb(operationRepo.countByIdentification("G129"));
			sn.setOnUsOffUs("001");

			sn.setMontant(new BigDecimal(0));
			sn.setInterchange(checkElement(data.get("G129")).add(checkElement(data.get("G130"))));
			sn.setCmi(checkElement(data.get("G131")).add(checkElement(data.get("G132"))));
			sn.setProduit(checkElement(data.get("G129")).add(checkElement(data.get("G130")))
					.subtract(checkElement(data.get("G131"))).subtract(checkElement(data.get("G132"))));
			syntheseRepository.saveAndFlush(sn);
			snList.add(sn);

			// consultation solde reception
			sn = new Synthese();
			sn.setIdHeader(new Integer(snt.getCode()));

			sn.setTransactionType("CSOther-reception");
			sn.setNb(operationRepo.countByIdentification("G122"));
			sn.setOnUsOffUs("001");

			sn.setMontant(new BigDecimal(0));
			sn.setInterchange(checkElement(data.get("G122")).add(checkElement(data.get("G123"))));
			sn.setCpi(checkElement(data.get("G124")).add(checkElement(data.get("G125"))));
			sn.setProduit(checkElement(data.get("G122")).add(checkElement(data.get("G123")))
					.subtract(checkElement(data.get("G124"))).subtract(checkElement(data.get("G125"))));
			syntheseRepository.saveAndFlush(sn);
			snList.add(sn);

		}

		snList = syntheseRepository.findByIdHeader(summaryRepository.findByDateSynthese(dateS).get().getCode());
		return ResponseEntity.ok().body(gson.toJson(snList));

	}

	@GetMapping("/getSynthese")
	public ResponseEntity<?> getSyntheseOnUS(
			@RequestParam(name = "dateSynthese", required = false) String dateSynthese) {
		String dateS;

		if (dateSynthese == null) {

			String fileDate = operationRepo.findAll().get(0).getFileDate();

			if (operationRepo.findAll().isEmpty()) {
				DateTimeFormatter dtf = DateTimeFormatter.ofPattern("YYYY-MM-dd");
				LocalDateTime now = LocalDateTime.now();
				dateS = dtf.format(now);
				List<Synthese> el = syntheseRepository
						.findByIdHeader(summaryRepository.findByDateSynthese(dateS).get().getCode());
				return ResponseEntity.ok().body(gson.toJson(el));
			}

			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyMMdd");
			LocalDate localDate = LocalDate.parse(fileDate, formatter);
			// DateTimeFormatter outputFormatter =
			// DateTimeFormatter.ofPattern("MM/dd/YYYY");
			DateTimeFormatter dtf = DateTimeFormatter.ofPattern("YYYY-MM-dd");
			dateS = localDate.format(dtf);
			FileRequest.print(dateS, FileRequest.getLineNumber());
			if (summaryRepository.findByDateSynthese(dateS).isPresent()
					&& SATATUS_VALID.equals(summaryRepository.findByDateSynthese(dateS).get().getStatus())) {
				List<Synthese> el = syntheseRepository
						.findByIdHeader(summaryRepository.findByDateSynthese(dateS).get().getCode());
				return ResponseEntity.ok().body(gson.toJson(el));
			}
			if (summaryRepository.findByDateSynthese(dateS).isPresent()) {
				SyntheseSummary x = summaryRepository.findByDateSynthese(dateS).get();
				List<Synthese> el = syntheseRepository
						.findByIdHeader(summaryRepository.findByDateSynthese(dateS).get().getCode());
				FileRequest.print("deleting", FileRequest.getLineNumber());
				syntheseRepository.deleteAll(el);
				summaryRepository.delete(x);
			}

			FileRequest.print("deleting done", FileRequest.getLineNumber());
			dateSynthese = localDate.format(dtf);
			FileRequest.print("calling remplirTableSynthese date synthese " + dateSynthese,
					FileRequest.getLineNumber());
			remplirTableSynthese(dateSynthese);
		} else {
			dateS = dateSynthese;
		}
		List<Synthese> el = syntheseRepository
				.findByIdHeader(summaryRepository.findByDateSynthese(dateS).get().getCode());
		return ResponseEntity.ok().body(gson.toJson(el));
	}

	@GetMapping("/getSyntheseDate/{idHeader}")
	public ResponseEntity<?> getSyntheseDate(@PathVariable(value = "idHeader") int idHeader) {
		FileRequest.print("calling", FileRequest.getLineNumber());
		return ResponseEntity.ok().body(gson.toJson(summaryRepository.findById(idHeader).get()));
	}

	@GetMapping("/updateSynthesesymmary/{idHeader}")
	public ResponseEntity<?> validateSynthese(@PathVariable(value = "idHeader") int idHeader) {
		SyntheseSummary synthese = summaryRepository.findById(idHeader).get();
		synthese.setStatus(SATATUS_VALID);
		summaryRepository.saveAndFlush(synthese);
		return ResponseEntity.ok().body(gson.toJson("Done"));
	}

	@GetMapping("/getAllSyntheseDate/")
	public ResponseEntity<?> getAllSyntheseDate() {
		FileRequest.print("calling", FileRequest.getLineNumber());
		List<SyntheseSummary> ListSynthese = summaryRepository.findAll();
		List<String> data = new ArrayList<>();
		for (SyntheseSummary sn : ListSynthese) {
			data.add(sn.getDateSynthese());
		}
		return ResponseEntity.ok().body(gson.toJson(data));
	}

	public Map<String, BigDecimal> findbyList() {
		Map<String, BigDecimal> elements = new HashMap<>();

		try {
			List<Object[]> queryResult = operationRepo.findSumByFilter();
			System.out.println(queryResult.size());
			for (Object[] obj : queryResult) {

				if (obj[0] != null && obj[0] != null) {
					String ld = (String) obj[0];
					BigDecimal count = new BigDecimal((String) obj[1]);
					if (elements.get(ld) == null) {
						elements.put(ld, count);
					} else
						elements.put(ld, elements.get(ld).add(count));
				}
			}

		} catch (Exception e) {

			System.out.println(e);
		}
		return elements;

	}

	public Map<String, Map<String, BigDecimal>> findbyListByBank(List<String> banks) {
		Map<String, Map<String, BigDecimal>> banksData = new HashMap<>();

		for (String bank : banks) {
			Map<String, BigDecimal> elements = new HashMap<>();
			banksData.put(bank, elements);

		}
		try {
			List<Object[]> queryResult = operationRepo.findSumByFilterByBank(banks);

			for (Object[] obj : queryResult) {

				if (obj[0] != null && obj[0] != null) {
					String bank = (String) obj[0];
					Map<String, BigDecimal> element = banksData.get(bank);
					String identification = (String) obj[1];
					BigDecimal count = new BigDecimal((String) obj[2]);
					if (element.get(identification) == null) {
						element.put(identification, count);
					} else
						element.put(identification, element.get(identification).add(count));
				}
			}

		} catch (Exception e) {

			System.out.println(e);
		}
		return banksData;

	}

	@GetMapping("newCycleBatch")
	public ResponseEntity<?> newCycleBatch() {

		BatchesFC batchS = batchRepo.findByKey("execStatus").get();
		FileRequest.print("updateStatus", FileRequest.getLineNumber());
		batchS.setBatchStatus(0);
		batchS.setBatchLastExcution(null);
		batchS.setBatchNumber(0);
		batchS.setBatchDate(new Date());
		batchS.setError(null);
		batchS.setErrorStackTrace(null);
		batchRepo.save(batchS);
		return ResponseEntity.ok().body("ok");

	}
}
