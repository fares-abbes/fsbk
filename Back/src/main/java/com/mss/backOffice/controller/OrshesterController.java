package com.mss.backOffice.controller;

import java.io.IOException;
import java.text.ParseException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import org.apache.commons.beanutils.PropertyUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.google.gson.Gson;
import com.mss.backOffice.request.AddFileRequest;
import com.mss.backOffice.services.FormulaInterpreterService;
import com.mss.unified.entities.BatchesFC;
import com.mss.unified.entities.DayOperationFransaBank;
import com.mss.unified.entities.MvbkConf;
import com.mss.unified.entities.UAP040IN;
import com.mss.unified.entities.UAP040INR;
import com.mss.unified.entities.UAP050FransaBank;
import com.mss.unified.entities.UAP050FransaBankHistory;
import com.mss.unified.entities.UAP050IN;
import com.mss.unified.entities.UAP050INR;
import com.mss.unified.entities.UAP050RFransaBank;
import com.mss.unified.entities.UAP051FransaBank;
import com.mss.unified.entities.UAP051IN;
import com.mss.unified.entities.UAP051INR;
import com.mss.unified.entities.UAP051RFransaBank;
import com.mss.unified.entities.UAPFransaBank;
import com.mss.unified.entities.UAPFransaBankR;
import com.mss.unified.entities.OpeningDay;
import com.mss.unified.repositories.BatchesFFCRepository;
import com.mss.unified.repositories.DayOperationFransaBankRepository;
import com.mss.unified.repositories.MvbkConfigRepository;
import com.mss.unified.repositories.UAP040INFransaBankRepository;
import com.mss.unified.repositories.UAP040INRFransaBankRepository;
import com.mss.unified.repositories.UAP050FransaBankRepository;
import com.mss.unified.repositories.UAP050INFransaBankRepository;
import com.mss.unified.repositories.UAP050INRFransaBankRepository;
import com.mss.unified.repositories.UAP050RFransaBankRepository;
import com.mss.unified.repositories.UAP051FransaBankRepository;
import com.mss.unified.repositories.UAP051INFransaBankRepository;
import com.mss.unified.repositories.UAP051INRFransaBankRepository;
import com.mss.unified.repositories.UAP051RFransaBankRepository;
import com.mss.unified.repositories.UAPFransaBankRRepository;
import com.mss.unified.repositories.UAPFransaBankRepository;
import com.mss.unified.repositories.OpeningDayRepository;

@RestController
@RequestMapping("orchester")
public class OrshesterController {
	@Autowired
	FransaBank040INController fsbk040In;
	@Autowired
	FransaBank050INController fsbk050In;
	@Autowired
	FransaBank051INController fsbk051In;
	@Autowired
	UAP040INFransaBankRepository uap040INR;
	@Autowired
	UAP050INFransaBankRepository uap050INR;
	@Autowired
	UAP051INFransaBankRepository uap051INR;
	@Autowired
	UAP040INRFransaBankRepository uap040INRR;
	@Autowired
	UAP050INRFransaBankRepository uap050INRR;
	@Autowired
	UAP051INRFransaBankRepository uap051INRR;
	@Autowired
	UAPFransaBankRRepository uap040RR;
	@Autowired
	UAP050RFransaBankRepository uap050RR;
	@Autowired
	UAP051RFransaBankRepository uap051RR;
	@Autowired
	UAPFransaBankRepository uap040R;
	@Autowired
	UAP050FransaBankRepository uap050R;
	@Autowired
	UAP051FransaBankRepository uap051R;

	@Autowired
	FransaBankController fsbk040;
	@Autowired
	FransaBank050Controller fsbk050;
	@Autowired
	FransaBank051Controller fsbk051;
	@Autowired
	@Lazy
	CheckCroController040 cc040;
	@Autowired
	@Lazy
	CheckCroController050 cc050;
	@Autowired
	@Lazy
	CheckCroController051 cc051;
	@Autowired
	OpeningDayRepository opdays;
	@Autowired
	FransaBank040Reglement reglement040;
	@Autowired
	FransaBank050Reglement reglement050;
	@Autowired
	FransaBank051Reglement reglement051;
	@Autowired
	BatchesFFCRepository batchesFFCRepository;
	@Autowired
	DownloadFileBc fbc;
	@Autowired
	FormulaInterpreterService fIPService;
	@Autowired
	DayOperationFransaBankRepository dayOperationFransaBankRepository;
	@Autowired
	public MvbkConfigRepository mvbkConfigR;
	private static final Logger logger = LoggerFactory.getLogger(OrshesterController.class);

	public static Authentication auth;
	public static boolean endedOk;
	private static final Gson gson = new Gson();

	@GetMapping("/GenerateBKM")
	public ResponseEntity<?> GenerateBKM() {
		List<String> categories = mvbkConfigR.findDistinctByCategorie();
		List<String> reply = new ArrayList<String>();
		reply.add("identification;formula;identifications;result");

		for (String category : categories) {
			List<String> identifications = new ArrayList<String>();
			List<MvbkConf> config = mvbkConfigR.findByCategorie(Integer.valueOf(category));
			for (MvbkConf conf : config) {
				if (conf.getCodeSettlement() != null && "" != conf.getCodeSettlement().trim()) {
					identifications.addAll(fIPService.extractElementsFromFormula(conf.getCodeSettlement()));
				}
			}
			if (identifications.isEmpty()) {
				continue;
			}
			List<DayOperationFransaBank> days = dayOperationFransaBankRepository.findByIdentifications(identifications);
			HashMap<String, List<DayOperationFransaBank>> grouper = new HashMap();
			for (DayOperationFransaBank day : days) {
				String key = day.getNumAutorisation() + "_" + day.getNumRefTransaction() + "_"
						+ day.getNumCartePorteur();
				if (grouper.containsKey(key)) {
					List<DayOperationFransaBank> element = grouper.get(key);
					element.add(day);
				} else {
					List<DayOperationFransaBank> element = new ArrayList<DayOperationFransaBank>();
					element.add(day);
					grouper.put(key, element);
				}

			}
			for (MvbkConf conf : config) {
				if (conf.getCodeSettlement() != null && "" != conf.getCodeSettlement().trim()) {
					for (Map.Entry<String, List<DayOperationFransaBank>> entry : grouper.entrySet()) {
						Map<String, Float> data = new HashMap<>();
						for (DayOperationFransaBank el : entry.getValue()) {
							data.put(el.getIdenfication(), el.getMontantSettlement());
						}
						reply.add(conf.getIdentification() + ";" + conf.getCodeSettlement() + ";" + entry.getKey() + ";"
								+ fIPService.evaluateWithElements(conf.getCodeSettlement(), data));

					}
				}
			}

		}
		return ResponseEntity.ok().body(gson.toJson(reply));
	}

	@Async
	@GetMapping("/startgetCro")
//	@Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class, isolation = Isolation.READ_UNCOMMITTED)
	public ResponseEntity<?> startgetCro() {
		try {

			// Get the current date and time
			LocalDateTime currentDateTime = LocalDateTime.now();

			// Define the desired date format
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

			// Format the current date as a string

			FileRequest.print("calling", FileRequest.getLineNumber());
			AddFileRequest add040 = new AddFileRequest();
			AddFileRequest add050 = new AddFileRequest();
			AddFileRequest add051 = new AddFileRequest();
			FileRequest.print("starting", FileRequest.getLineNumber());

			OpeningDay ops = opdays.findAll().get(0);

			BatchesFC batch040 = batchesFFCRepository.findByKey("CRAUAP40IN").get();
			BatchesFC batch050 = batchesFFCRepository.findByKey("CRAUAP50IN").get();
			BatchesFC batch051 = batchesFFCRepository.findByKey("CRAUAP51IN").get();
			BatchesFC batchTp = batchesFFCRepository.findByKey("TP").get();
//			BatchesFC batchLOT = batchesFFCRepository.findByKey("SENDLOT").get();

			FileRequest.print(ops.toString(), FileRequest.getLineNumber());

			add040.setFileDate(ops.getFileIntegration());
			add040.setFileName(batch040.getFileName());
			add040.setFilePath(batch040.getFileLocation());

			add050.setFileDate(ops.getFileIntegration());
			add050.setFileName(batch050.getFileName());
			add050.setFilePath(batch050.getFileLocation());

			add051.setFileDate(ops.getFileIntegration());
			add051.setFileName(batch051.getFileName());
			add051.setFilePath(batch051.getFileLocation());
			ExecutorService executorService = Executors.newFixedThreadPool(10);
			auth = SecurityContextHolder.getContext().getAuthentication();
			CompletableFuture<String> result1 = executeEndpoint1(add040, executorService);
			CompletableFuture<String> result2 = executeEndpoint2(add050, executorService);
			CompletableFuture<String> result3 = executeEndpoint3(add051, executorService);
			// Wait for all CompletableFuture to complete
			CompletableFuture<Void> allOf = CompletableFuture.allOf(result1, result2, result3);
			allOf.join();
			// Combine the results
			FileRequest.print(ops.toString(), FileRequest.getLineNumber());
			batch040 = batchesFFCRepository.findByKey("CRAUAP40IN").get();
			batch050 = batchesFFCRepository.findByKey("CRAUAP50IN").get();
			batch051 = batchesFFCRepository.findByKey("CRAUAP51IN").get();

			FileRequest.print(ops.toString(), FileRequest.getLineNumber());

			
			if (batch040.getBatchStatus() == 4 && batch050.getBatchStatus() == 4 && batch051.getBatchStatus() == 4) {
				return ResponseEntity.ok().body(gson.toJson("missing files"));

			}
			if ((uap040INR.findByFlagAndAcceptedAndCopied(null, "2", "false") != null
					&& uap040INR.findByFlagAndAcceptedAndCopied(null, "2", "false").size() > 0)
					|| (uap040INR.findByFlagAndAcceptedAndCopied(null, "NOT OK", "false") != null
							&& uap040INR.findByFlagAndAcceptedAndCopied(null, "NOT OK", "false").size() > 0)) {
				FileRequest.print(" " + uap040INR.findByFlagAndAcceptedAndCopied(null, "2", "false").size(),
						FileRequest.getLineNumber());
				FileRequest.print(" " + uap040INR.findByFlagAndAcceptedAndCopied(null, "NOT OK", "false").size(),
						FileRequest.getLineNumber());

				ops.setStatus040("DoneSort");
				batchesFFCRepository.updateFinishBatch("CRAUAP40IN", 5, new Date());
				batchesFFCRepository.updateFinishBatch("SENDLOT", -1, new Date());
				batchesFFCRepository.updateFinishBatch("CRAUAP140IN", -1, new Date());

			} else if ((uap040INR.findByFlagAndAcceptedAndCopied("20", "2", "false") != null
					&& uap040INR.findByFlagAndAcceptedAndCopied("20", "2", "false").size() > 0)
					|| (uap040INR.findByFlagAndAcceptedAndCopied("20", "NOT OK", "false") != null
							&& uap040INR.findByFlagAndAcceptedAndCopied("20", "NOT OK", "false").size() > 0)) {
				FileRequest.print(" " + uap040INR.findByFlagAndAcceptedAndCopied("20", "2", "false").size(),
						FileRequest.getLineNumber());
				FileRequest.print(" " + uap040INR.findByFlagAndAcceptedAndCopied("20", "NOT OK", "false").size(),
						FileRequest.getLineNumber());

				ops.setStatus040("DoneSort");
				opdays.save(ops);

				cc040.generateRejectFile();
				batchTp.setBatchStatus(10);
				batchesFFCRepository.save(batchTp);
				Thread.sleep(1000);
				batchesFFCRepository.updateFinishBatch("SENDLOT", -1, new Date());
				batchesFFCRepository.updateFinishBatch("CRAUAP140IN", -1, new Date());
			} else {
				ops.setStatus040("DoneCro");
				batchesFFCRepository.updateFinishBatch("CRAUAP140IN", 1, new Date());
			}
			FileRequest.print(" " + uap050INR.findByFlagAndAcceptedAndCopied(null, "2", "false").size(),
					FileRequest.getLineNumber());
			FileRequest.print(" " + uap050INR.findByFlagAndAcceptedAndCopied(null, "NOT OK", "false").size(),
					FileRequest.getLineNumber());

			if ((uap050INR.findByFlagAndAcceptedAndCopied(null, "2", "false") != null
					&& uap050INR.findByFlagAndAcceptedAndCopied(null, "2", "false").size() > 0)
					|| (uap050INR.findByFlagAndAcceptedAndCopied(null, "NOT OK", "false") != null
							&& uap050INR.findByFlagAndAcceptedAndCopied(null, "NOT OK", "false").size() > 0)) {
				FileRequest.print(" " + uap050INR.findByFlagAndAcceptedAndCopied(null, "2", "false").size(),
						FileRequest.getLineNumber());
				FileRequest.print(" " + uap050INR.findByFlagAndAcceptedAndCopied(null, "NOT OK", "false").size(),
						FileRequest.getLineNumber());

				ops.setStatus050("DoneSort");
				batchesFFCRepository.updateFinishBatch("CRAUAP50IN", 5, new Date());
				batchesFFCRepository.updateFinishBatch("SENDLOT", -1, new Date());
				batchesFFCRepository.updateFinishBatch("CRAUAP150IN", -1, new Date());

			} else if ((uap050INR.findByFlagAndAcceptedAndCopied("20", "2", "false") != null
					&& uap050INR.findByFlagAndAcceptedAndCopied("20", "2", "false").size() > 0)
					|| (uap050INR.findByFlagAndAcceptedAndCopied("20", "NOT OK", "false") != null
							&& uap050INR.findByFlagAndAcceptedAndCopied("20", "NOT OK", "false").size() > 0)) {
				FileRequest.print(" " + uap050INR.findByFlagAndAcceptedAndCopied("20", "2", "false").size(),
						FileRequest.getLineNumber());
				FileRequest.print(" " + uap050INR.findByFlagAndAcceptedAndCopied("20", "NOT OK", "false").size(),
						FileRequest.getLineNumber());
				ops.setStatus050("DoneSort");
				opdays.save(ops);

				cc050.generateRejectFile();
				batchTp.setBatchStatus(10);
				batchesFFCRepository.save(batchTp);
				batchesFFCRepository.updateFinishBatch("SENDLOT", -1, new Date());
				Thread.sleep(1000);
				batchesFFCRepository.updateFinishBatch("CRAUAP150IN", -1, new Date());

			} else {
				ops.setStatus050("DoneCro");
				batchesFFCRepository.updateFinishBatch("CRAUAP150IN", 1, new Date());
			}
			if ((uap051INR.findByFlagAndAcceptedAndCopied(null, "2", "false") != null
					&& uap051INR.findByFlagAndAcceptedAndCopied(null, "2", "false").size() > 0)
					|| (uap051INR.findByFlagAndAcceptedAndCopied(null, "NOT OK", "false") != null
							&& uap051INR.findByFlagAndAcceptedAndCopied(null, "NOT OK", "false").size() > 0)) {
				FileRequest.print(" " + uap051INR.findByFlagAndAcceptedAndCopied(null, "2", "false").size(),
						FileRequest.getLineNumber());
				FileRequest.print(" " + uap051INR.findByFlagAndAcceptedAndCopied(null, "NOT OK", "false").size(),
						FileRequest.getLineNumber());

				ops.setStatus051("DoneSort");
				batchesFFCRepository.updateFinishBatch("CRAUAP51IN", 5, new Date());
				batchesFFCRepository.updateFinishBatch("SENDLOT", -1, new Date());
				batchesFFCRepository.updateFinishBatch("CRAUAP151IN", -1, new Date());

			} else if ((uap051INR.findByFlagAndAcceptedAndCopied("20", "2", "false") != null
					&& uap051INR.findByFlagAndAcceptedAndCopied("20", "2", "false").size() > 0)
					|| (uap051INR.findByFlagAndAcceptedAndCopied("20", "NOT OK", "false") != null
							&& uap051INR.findByFlagAndAcceptedAndCopied("20", "NOT OK", "false").size() > 0)) {
				FileRequest.print(" " + uap051INR.findByFlagAndAcceptedAndCopied("20", "2", "false").size(),
						FileRequest.getLineNumber());
				FileRequest.print(" " + uap051INR.findByFlagAndAcceptedAndCopied("20", "NOT OK", "false").size(),
						FileRequest.getLineNumber());

				ops.setStatus051("DoneSort");
				opdays.save(ops);

				cc051.generateRejectFile();
				batchTp.setBatchStatus(10);
				batchesFFCRepository.updateFinishBatch("CRAUAP51IN", 1, new Date());

				batchesFFCRepository.updateFinishBatch("TP", 10, new Date());
				batchesFFCRepository.updateFinishBatch("SENDLOT", -1, new Date());
				batchesFFCRepository.updateFinishBatch("CRAUAP151IN", -1, new Date());

			} else {
				ops.setStatus051("DoneCro");
				batchesFFCRepository.updateFinishBatch("CRAUAP151IN", 1, new Date());

			}
			opdays.save(ops);
			FileRequest.print(ops.toString(), FileRequest.getLineNumber());

			String combinedResult = null;

			try {
				copyUap040In();
				copyUap050In();
				copyUap051In();
				combinedResult = result1.get() + "\n" + result2.get() + "\n" + result3.get();
				
			} catch (InterruptedException | ExecutionException e) {
				logger.error(e.getMessage());
				logger.error(e.getCause().toString());
				batchesFFCRepository.updateFinishBatch("CRAUAP40IN", 2, new Date());
				batchesFFCRepository.updateFinishBatch("CRAUAP50IN", 2, new Date());
				batchesFFCRepository.updateFinishBatch("CRAUAP51IN", 2, new Date());

			}

			return ResponseEntity.ok().body(gson.toJson(combinedResult));
		} catch (Exception e) {
			batchesFFCRepository.updateFinishBatch("CRAUAP40IN", 2, new Date());
			batchesFFCRepository.updateFinishBatch("CRAUAP50IN", 2, new Date());
			batchesFFCRepository.updateFinishBatch("CRAUAP51IN", 2, new Date());
			return ResponseEntity.badRequest().body(gson.toJson("error"));
		}
	}

	@Async
	public CompletableFuture<String> executeEndpoint1(AddFileRequest adf, ExecutorService executorService) {

		FileRequest.print("strating", FileRequest.getLineNumber());
		CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> {
			// Call the fsbk040.Matching() method
			try {
				return fsbk040In.matchingUap40In(adf);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return "ERROR";
			}
		}, executorService);
		return future;

	}

	@Async
	public CompletableFuture<String> executeEndpoint2(AddFileRequest adf, ExecutorService executorService) {

		FileRequest.print("strating", FileRequest.getLineNumber());
		CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> {
			// Call the fsbk040.Matching() method
			try {
				return fsbk050In.matchingUAP050(adf);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return "ERROR";
			}
		}, executorService);
		return future;
	}

	@Async
	public CompletableFuture<String> executeEndpoint3(AddFileRequest adf, ExecutorService executorService) {

		FileRequest.print("strating", FileRequest.getLineNumber());
		CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> {
			// Call the fsbk040.Matching() method
			try {
				return fsbk051In.matchingUAP051(adf);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return "ERROR";
			}
		}, executorService);
		return future;

	}

	@GetMapping("/startgetCra")
	@Transactional
	public ResponseEntity<?> startgetCra() {
		endedOk = false;
		FileRequest.print("calling", FileRequest.getLineNumber());

		String name = SecurityContextHolder.getContext().getAuthentication().getName();
		FileRequest.print(name, FileRequest.getLineNumber());

		AddFileRequest add040 = new AddFileRequest();
		AddFileRequest add050 = new AddFileRequest();
		AddFileRequest add051 = new AddFileRequest();
		FileRequest.print("starting", FileRequest.getLineNumber());

		OpeningDay ops = opdays.findByStatus040().get();
		FileRequest.print(ops.toString(), FileRequest.getLineNumber());
		BatchesFC batch040 = batchesFFCRepository.findByKey("CRAUAP40").get();
		BatchesFC batch050 = batchesFFCRepository.findByKey("CRAUAP50").get();
		BatchesFC batch051 = batchesFFCRepository.findByKey("CRAUAP51").get();
		FileRequest.print(ops.toString(), FileRequest.getLineNumber());

		add040.setFileDate(ops.getFileIntegration());
		add040.setFileName(batch040.getFileName());
		add040.setFilePath(batch040.getFileLocation());

		add050.setFileDate(ops.getFileIntegration());
		add050.setFileName(batch050.getFileName());
		add050.setFilePath(batch050.getFileLocation());

		add051.setFileDate(ops.getFileIntegration());
		add051.setFileName(batch051.getFileName());
		add051.setFilePath(batch051.getFileLocation());
		ExecutorService executorService = Executors.newFixedThreadPool(10);
		auth = SecurityContextHolder.getContext().getAuthentication();

		CompletableFuture<String> result1 = executeEndpoint040(add040, executorService);
		CompletableFuture<String> result2 = executeEndpoint050(add050, executorService);
		CompletableFuture<String> result3 = executeEndpoint051(add051, executorService);
		// Wait for all CompletableFuture to complete
		CompletableFuture<Void> allOf = CompletableFuture.allOf(result1, result2, result3);
		allOf.join();

		// Combine the results
		String combinedResult = null;
		batch040 = batchesFFCRepository.findByKey("CRAUAP40").get();
		batch050 = batchesFFCRepository.findByKey("CRAUAP50").get();
		batch051 = batchesFFCRepository.findByKey("CRAUAP51").get();
		FileRequest.print("preparing writing files ", FileRequest.getLineNumber());
		try {
			combinedResult = result1.get() + "\n" + result2.get() + "\n" + result3.get();
			FileRequest.print("entring try block", FileRequest.getLineNumber());
			FileRequest.print(" " + batch040.getBatchStatus() + batch050.getBatchStatus() + batch051.getBatchStatus(),
					FileRequest.getLineNumber());
			if (endedOk) {
				FileRequest.print("preparing writing files ", FileRequest.getLineNumber());
				batch040.setBatchStatus(0);
				batchesFFCRepository.save(batch040);
				fbc.writeInFile();
				batch040.setBatchStatus(1);
				batchesFFCRepository.save(batch040);
				FileRequest.print(" writing files done", FileRequest.getLineNumber());

			} else {

				FileRequest.print(" not generated", FileRequest.getLineNumber());
			}
			FileRequest.print("preparing writing files ", FileRequest.getLineNumber());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			FileRequest.print(e.getCause().toString(), FileRequest.getLineNumber());
			FileRequest.print(e.getStackTrace().toString(), FileRequest.getLineNumber());
			FileRequest.print(e.getMessage(), FileRequest.getLineNumber());
			FileRequest.print(e.getLocalizedMessage(), FileRequest.getLineNumber());

		}

		return ResponseEntity.ok().body(gson.toJson(combinedResult));

	}

	@Async
	public CompletableFuture<String> executeEndpoint040(AddFileRequest adf, ExecutorService executorService) {

		FileRequest.print("strating", FileRequest.getLineNumber());
		CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> {
			// Call the fsbk040.Matching() method
			try {
				return fsbk040.Matching(adf);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return "ERROR";
			}
		}, executorService);
		return future;

	}

	@Async
	public CompletableFuture<String> executeEndpoint050(AddFileRequest adf, ExecutorService executorService) {
		FileRequest.print("strating", FileRequest.getLineNumber());
		CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> {
			// Call the fsbk040.Matching() method
			try {
				return fsbk050.Matching(adf);
			} catch (IOException e) {
//				 TODO Auto-generated catch block
				e.printStackTrace();
				return "ERROR";
			}
		}, executorService);
		return future;

	}

	@Async
	public CompletableFuture<String> executeEndpoint051(AddFileRequest adf, ExecutorService executorService) {
		FileRequest.print("strating", FileRequest.getLineNumber());
		CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> {
			// Call the fsbk040.Matching() method
			try {
				return fsbk051.Matching(adf);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return "ERROR";
			}
		}, executorService);
		return future;
	}


	public void copyUap040In() {
		List<UAP040IN> in = uap040INR.findByCopiedAndFlag("false", "20");
		List<UAP040INR> out = in.stream().map(developer -> {

			UAP040INR copiedUap = new UAP040INR();
			developer.setCopied("done");
			try {

				PropertyUtils.copyProperties(copiedUap, developer);

			} catch (Exception ex) {
				ex.printStackTrace();
			}
			copiedUap.setBkmGeneration(null);
			return copiedUap;

		}).collect(Collectors.toList());
		uap040INRR.saveAll(out);
		uap040INR.saveAll(in);
	}

	public void copyUap050In() {
		List<UAP050IN> in = uap050INR.findByCopiedAndFlag("false", "20");
		List<UAP050INR> out = in.stream().map(developer -> {

			UAP050INR copiedUap = new UAP050INR();
			developer.setCopied("done");

			try {

				PropertyUtils.copyProperties(copiedUap, developer);

			} catch (Exception ex) {
				ex.printStackTrace();
			}
			copiedUap.setBkmGeneration(null);
			return copiedUap;

		}).collect(Collectors.toList());
		uap050INRR.saveAll(out);
		uap050INR.saveAll(in);
	}

	public void copyUap051In() {
		List<UAP051IN> in = uap051INR.findByCopiedAndFlag("false", "20");
		List<UAP051INR> out = in.stream().map(developer -> {

			UAP051INR copiedUap = new UAP051INR();
			developer.setCopied("done");

			try {

				PropertyUtils.copyProperties(copiedUap, developer);

			} catch (Exception ex) {
				ex.printStackTrace();
			}
			copiedUap.setBkmGeneration(null);
			return copiedUap;

		}).collect(Collectors.toList());
		uap051INRR.saveAll(out);
		uap051INR.saveAll(in);
	}

}
