package com.mss.backOffice.controller;

import com.google.common.base.Throwables;
import com.google.gson.Gson;
import com.mss.backOffice.exception.ExceptionMethod;
import com.mss.backOffice.exception.ResourceNotFoundException;
import com.mss.backOffice.request.*;
import com.mss.backOffice.services.FormulaInterpreterService;
import com.mss.unified.entities.*;
import com.mss.unified.repositories.*;
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
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RestController
@RequestMapping("ChargeBacksController")
public class ChargeBacksController {


//	@GetMapping("/countDuplicate")
//	public ResponseEntity<?> countDuplicate() {
//		List<Integer> x = filecontentTRepository.countDuplicate();
//		Integer sum = x.stream().collect(Collectors.summingInt(Integer::intValue));
//		return ResponseEntity.ok().body(gson.toJson(String.valueOf(sum)));
//
//	}
@Autowired
FormulaInterpreterService fIPService;
private int recordNumber;
	public static Integer eve1 = 0;
	public static int eve = 0;
	public synchronized int getEveIndex() {
		return (++eve);
	}

	public static int getEveIndex1() {
		synchronized (eve1) {
			eve1 = eve1 + 1;
		}

		return eve1;
	}

	@Autowired
	OpeningDayRepository openedDayRepo;
	private static final Logger logger = LoggerFactory.getLogger(ChargeBacksController.class);
	@Autowired
	CodeBankBCRepository codeBankBCRepository;
	@Autowired
	MvbkConfigRepository mvbkConfigRepo;
    @Autowired
    private FILECONTENTRepository filecontentTRepository;
	@Autowired
    private UAP052INRepository uAP052INRepository;
	@Autowired
    private Gson gson;
    @Autowired
    private LogAtmRepository logAtmRepository;
    @Autowired
    private SwitchTransactionTRepository switchTransactionTRepository;
    @Autowired
    private DayOperationFransaBankRepository dayOperationFransaBankRepository;
    @Autowired
    private UAP052FransaBankRepository uap052FransaBankRepository;
    @Autowired
    private com.mss.backOffice.services.PropertyService propertyService;
    @Autowired
    private BkmvtiFransaBankRepository bkmvtiFransaBankRepository;
	@Autowired
	BatchesFFCRepository batchesFFCRepository;
	@GetMapping("/matchingCRA")
	public String matchingCRA(@RequestBody AddFileRequest addFileRequest) throws IOException {
		FileRequest.print("thread nb " + Thread.currentThread().getName(), FileRequest.getLineNumber());
		SecurityContextHolder.getContext().setAuthentication(OrshesterController.auth);

		String name = SecurityContextHolder.getContext().getAuthentication().getName();
		FileRequest.print(name, FileRequest.getLineNumber());
		try {
			if (openedDayRepo.findByStatus040().isPresent()) {
				OpeningDay openDay = openedDayRepo.findByStatus040().get();
				openDay.setLotIncrementNb(1);
				openDay.setLotIncrementNbCra(1);
				openedDayRepo.saveAndFlush(openDay);
				logger.info("matchingUAP052");
				BatchesFC batch = batchesFFCRepository.findByKey("CRAUAP52").get();
				batch.setBatchLastExcution(batch.getBatchEndDate());
				batch.setBatchStatus(0);
				batch.setBatchDate(new Date());
				batch.setError(null);
				batch.setErrorStackTrace(null);
				batchesFFCRepository.saveAndFlush(batch);
				int etat = validTransaction(addFileRequest, openDay.getFileIntegration());


				List<UAP052FransaBank> data = uap052FransaBankRepository
						.getListUAPByStatusTechniqueNV(openDay.getFileIntegration());
				if (data.size() > 0) {
					openedDayRepo.updateStatus040(openDay.getFileIntegration(), "doneSortCra");

					batchesFFCRepository.updateFinishBatch("CRAUAP52", 2, new Date());
					logger.info("end matching cra 040 with rejection");
				} else {
					OrshesterController.endedOk = true;
					openedDayRepo.updateStatus040(openDay.getFileIntegration(), "doneCra");
					batchesFFCRepository.updateFinishBatch("CRAUAP40", 1, new Date());
					logger.info("end matching cra 040");
				}
			}
			return "Done";
		} catch (Exception e) {
			String stackTrace = Throwables.getStackTraceAsString(e);
			logger.info("Exception is=>{}", stackTrace);
			if (stackTrace.length() > 4000)
				stackTrace = stackTrace.substring(0, 3999);
			String error = e.getMessage() == null ? e.toString() : e.getMessage();
			if (error.length() > 255)
				error = error.substring(0, 254);

			batchesFFCRepository.updateStatusAndErrorBatch("CRAUAP40", 2, error, new Date(), stackTrace);
			return "ERROR";
		}

	}

	private int validTransaction(AddFileRequest addFileRequest, String fileIntegration) {

		logger.info("******* begin validTransaction 052 **********");

		// String fileName = addFileRequest.getFilePath() + "/" +
		// addFileRequest.getFileName() + "."+"20"+addFileRequest.getFileDate()+".CRA";
		String fileName = addFileRequest.getFilePath() + "/" + addFileRequest.getFileName() + ".CRA";
		try {
			logger.info("file name cra 052=>{}", fileName);
//			FileRequest.print("uap 052 matching", FileRequest.getLineNumber());
			if (Files.exists(Paths.get(fileName))) {
				logger.info("file cra 052 exist");

				List<UAP052FransaBank> UAPFransaBankS = uap052FransaBankRepository.findByFileIntegration(fileIntegration);
				logger.info("UAPFransaBankS size =>{}", UAPFransaBankS.size());
				List<UAP052FransaBank> uAPFransaBank = new ArrayList<>();
				List<String> cras = new ArrayList<>();
				Stream<String> stream = extractData(addFileRequest);

//			Stream<String> stream = Files.lines(Paths.get(fileName), StandardCharsets.ISO_8859_1);

				cras = stream
						.filter(element -> element.startsWith("035")
								&& element.substring(element.length() - 3, element.length()).equals("000"))
						.map(String::toUpperCase).collect(Collectors.toList());
//				FileRequest.print("uap 040 matching cras size" + cras.size(), FileRequest.getLineNumber());

				String dateReg = "";
				if (cras.size() != 0) {
					dateReg = cras.get(0).substring(38, 46);
					logger.info("date reglement cra 052 =>{}", dateReg);


				}
				FileRequest.print("uap 052 matching", FileRequest.getLineNumber());

				Map<String, UAP052FransaBank> fileUap = new HashMap<>();

				UAPFransaBankS.forEach(u -> {

					String idChargeBack=u.getIdChargeback();
					String numAutorisationUap = u.getNumAutorisationOperationInitiale();

					fileUap.put( idChargeBack +numAutorisationUap , u);

				});
				FileRequest.print("uap 052 matching", FileRequest.getLineNumber());

				cras.forEach(e -> {
					String uapRIO= e.substring(0, 38);
				String dateReglement = e.substring(38, 46);
				
				// Parse UAP052 file format according to specification
				int offset = 46;
				
				// Code opération (3N)
				String codeOperation = e.substring(offset, offset + 3);
				offset += 3;
				
				// Référence de la banque du donneur d'ordre (18AN)
				String referenceBanqueDonneurOrdre = e.substring(offset, offset + 18);
				offset += 18;
				
				// Code Banque donneur d'ordre (3N)
				String codeBanqueDonneurOrdre = e.substring(offset, offset + 3);
				offset += 3;
				
				// Code Agence donneur d'ordre (5N)
				String codeAgenceDonneurOrdre = e.substring(offset, offset + 5);
				offset += 5;
				
				// Date du chargeback (8N) - AAAAMMJJ
				String dateChargeback = e.substring(offset, offset + 8);
				offset += 8;
				
				// Code Banque acquéreur (3N)
				String codeBanqueAcquereur = e.substring(offset, offset + 3);
				offset += 3;
				
				// Code Agence acquéreur (5N)
				String codeAgenceAcquereur = e.substring(offset, offset + 5);
				offset += 5;
				
				// Code motif du Chargeback (4N)
				String codeMotifChargeback = e.substring(offset, offset + 4);
				offset += 4;
				
				// Identifiant du Chargeback (16N)
				String identifiantChargeback = e.substring(offset, offset + 16);
				offset += 16;
				
				// Montant du chargeback (15N)
				String montantChargeback = e.substring(offset, offset + 15);
				offset += 15;
				
				// Sens du montant (1AN)
				String sensMontant = e.substring(offset, offset + 1);
				offset += 1;
				
				// RIB du porteur de la carte (20N)
				String ribPorteurCarte = e.substring(offset, offset + 20);
				offset += 20;
				
				// N° d'autorisation de l'opération initiale (15N)
				String numAutorisationOperationInitiale = e.substring(offset, offset + 15);
				offset += 15;
				
				// Date de l'opération initiale (8N) - AAAAMMJJ
				String dateOperationInitiale = e.substring(offset, offset + 8);
				offset += 8;
				
				// FILLER (526) - Zone libre
				String filler = e.substring(offset, Math.min(offset + 526, e.length()));

					String statustechnique = e.substring(Math.min(offset + 526, e.length())-3, Math.min(offset + 526, e.length()));


					if (fileUap.containsKey(identifiantChargeback + numAutorisationOperationInitiale)) {
					UAP052FransaBank uap = fileUap.get(identifiantChargeback + numAutorisationOperationInitiale);

						uap.setStatusTechnique(statustechnique);
						uap.setUapRio(uapRIO);
						uap.setDateReglement(dateReglement);
						uAPFransaBank.add(uap);

				}
			});


				uap052FransaBankRepository.saveAll(uAPFransaBank);
//				FileRequest.print("uap 040 matching", FileRequest.getLineNumber());


				logger.info("size valid Transaction 052=>{}", uAPFransaBank.size());
				return 1;

			}

			else {
				FileRequest.print(fileName, FileRequest.getLineNumber());
				batchesFFCRepository.updateStatusAndErrorBatch("CRAUAP52", 4, "Missing file!", new Date(), "");
				logger.info("****** missing file cra 040 ********");
				return 0;
			}
		} catch (Exception e) {
			String stackTrace = Throwables.getStackTraceAsString(e);
			logger.info("Exception is=>{}", stackTrace);
			FileRequest.print(fileName, FileRequest.getLineNumber());
			batchesFFCRepository.updateStatusAndErrorBatch("CRAUAP52", 2, "error!", new Date(), "");
			return 2;
		}
	}

	public static void moveFilesByStartingName(String sourceDirectory, String destinationDirectory, String startingName)
			throws IOException {
		File sourceDir = new File(sourceDirectory);
		File[] files = sourceDir.listFiles((dir, name) -> name.startsWith(startingName));

		if (files != null) {

			LocalDateTime timer = LocalDateTime.now();
			String movingDate = "" + timer.getYear() + timer.getMonth() + timer.getDayOfMonth() + "_" + timer.getHour()
					+ timer.getMinute() + timer.getSecond();

			for (File file : files) {
				String originalFileName = file.getName();
				String newFileName = originalFileName + movingDate;

				Path sourcePath = file.toPath();
				Path destinationPath = new File(destinationDirectory, newFileName).toPath();

				Files.move(sourcePath, destinationPath, StandardCopyOption.REPLACE_EXISTING);
			}
		}
	}

	public Stream<String> extractData(AddFileRequest addFileRequest) throws IOException {
		logger.info("extractData");

		Stream<String> fileRows = null;
		List<String> filesNames = listAndFilterFiles(addFileRequest.getFilePath(), "052.DZD.CRA");
		FileRequest.print(filesNames.toString(), FileRequest.getLineNumber());
		FileRequest.print(addFileRequest.getFilePath(), FileRequest.getLineNumber());

		fileRows = readFiles(filesNames, addFileRequest.getFilePath());
		List<String> rows = fileRows.map(String::toUpperCase).collect(Collectors.toList());
		logger.info("extractData size = " + rows.size());
		for (String file : filesNames) {
			moveFilesByStartingName(addFileRequest.getFilePath(), propertyService.getCompensationfilePath(), file);
		}
		return rows.stream();
	}

	public List<String> listAndFilterFiles(String folderPath, String filter) {
		List<String> filteredFiles = new ArrayList<>();

		// Create a File object for the specified folder path
		File folder = new File(folderPath);

		// Check if the folder exists and is a directory
		if (folder.exists() && folder.isDirectory()) {
			// List all files in the folder
			File[] files = folder.listFiles();

			if (files != null) {
				for (File file : files) {
					// Check if the file matches the filter criteria
					if (file.isFile() && file.getName().contains(filter)) {
						// Add the file name to the list of filtered files
						filteredFiles.add(file.getName());
					}
				}
			}
		}

		return filteredFiles;
	}

	public Stream<String> readFiles(List<String> filteredFiles, String folderPath) {
		return filteredFiles.stream() // Stream the filtered file names
				.map(fileName -> Paths.get(folderPath, fileName)) // Map to file paths
				.flatMap(filePath -> {
					try {
						return Files.lines(filePath, StandardCharsets.ISO_8859_1); // Open each file as a stream
					} catch (IOException e) {
						throw new UncheckedIOException(e); // Wrap IOException as UncheckedIOException
					}
				});
	}
	@GetMapping("/matchingCROApi")
	public String matchingCROApi() throws IOException {
		AddFileRequest addFileRequest=new AddFileRequest();
		addFileRequest.setFileName("035.000.001.070.DZD");
		addFileRequest.setFilePath("C:\\home\\fsbkatm\\Documents\\COMPENSATION\\CRO");
		return matchingCRO(addFileRequest);
	}
	@GetMapping("/matchingCRO")
	public String matchingCRO(@RequestBody AddFileRequest addFileRequest) throws IOException {


		FileRequest.print("matchingUAP052IN", FileRequest.getLineNumber());
		BatchesFC batch = batchesFFCRepository.findByKey("CROUAP52").get();
		batch.setBatchLastExcution(batch.getBatchEndDate());
		batch.setBatchStatus(0);
		batch.setBatchDate(new Date());
		batch.setError(null);
		batch.setErrorStackTrace(null);
		batchesFFCRepository.saveAndFlush(batch);
		SecurityContextHolder.getContext().setAuthentication(OrshesterController.auth);

		try {
			int etat = validTransactionCRO(addFileRequest);



		} catch (Exception e) {
			String stackTrace = Throwables.getStackTraceAsString(e);
			logger.info("Exception is=>{}", stackTrace);
			if (stackTrace.length() > 4000)
				stackTrace = stackTrace.substring(0, 3999);
			String error = e.getMessage() == null ? e.toString() : e.getMessage();
			if (error.length() > 255)
				error = error.substring(0, 254);

			batchesFFCRepository.updateStatusAndErrorBatch("CROUAP52", 2, error, new Date(), stackTrace);
			return "error";
		}
		FileRequest.print("matching done 052", FileRequest.getLineNumber());

		return "done";
	}
	public int validTransactionCRO(AddFileRequest addFileRequest) throws IOException {

		logger.info("******* begin validTransaction 052 **********");

		// String fileName = addFileRequest.getFilePath() + "/" +
		// addFileRequest.getFileName() + "."+"20"+addFileRequest.getFileDate()+".CRA";
		String fileName = addFileRequest.getFilePath() + "/" + addFileRequest.getFileName() + ".CRO";
		try {
			logger.info("file name cra 052=>{}", fileName);
//			FileRequest.print("uap 052 matching", FileRequest.getLineNumber());
			if (Files.exists(Paths.get(fileName))) {
				logger.info("file cra 052 exist");

				List<UAP052IN> UAPFransaBankS = uAP052INRepository.findByDateReglementIsNull();
				logger.info("UAP052IN size =>{}", UAPFransaBankS.size());
				List<UAP052IN> uAPFransaBank = new ArrayList<>();
				List<String> cros = new ArrayList<>();
				Stream<String> stream = extractData(addFileRequest);

//			Stream<String> stream = Files.lines(Paths.get(fileName), StandardCharsets.ISO_8859_1);

				cros = stream
						.filter(element -> !element.startsWith("ECRO") )
						.map(String::toUpperCase).collect(Collectors.toList());
//				FileRequest.print("uap 040 matching cras size" + cras.size(), FileRequest.getLineNumber());

				String dateReg = "";
				if (cros.size() != 0) {
					dateReg = cros.get(0).substring(38, 46);
					logger.info("date reglement cra 052 =>{}", dateReg);


				}
				FileRequest.print("uap 052 matching", FileRequest.getLineNumber());

				Map<String, UAP052IN> fileUap = new HashMap<>();

				UAPFransaBankS.forEach(u -> {

					String idChargeBack=u.getIdChargeback();
					String numAutorisationUap = u.getNumAutorisationOperationInitiale();

					fileUap.put( idChargeBack +numAutorisationUap , u);

				});
				FileRequest.print("uap 052 matching", FileRequest.getLineNumber());

				cros.forEach(e -> {
					String uapRIO= e.substring(0, 38);
					String dateReglement = e.substring(38, 46);

					// Parse UAP052 file format according to specification
					int offset = 46;

					// Code opération (3N)
					String codeOperation = e.substring(offset, offset + 3);
					offset += 3;

					// Référence de la banque du donneur d'ordre (18AN)
					String referenceBanqueDonneurOrdre = e.substring(offset, offset + 18);
					offset += 18;

					// Code Banque donneur d'ordre (3N)
					String codeBanqueDonneurOrdre = e.substring(offset, offset + 3);
					offset += 3;

					// Code Agence donneur d'ordre (5N)
					String codeAgenceDonneurOrdre = e.substring(offset, offset + 5);
					offset += 5;

					// Date du chargeback (8N) - AAAAMMJJ
					String dateChargeback = e.substring(offset, offset + 8);
					offset += 8;

					// Code Banque acquéreur (3N)
					String codeBanqueAcquereur = e.substring(offset, offset + 3);
					offset += 3;

					// Code Agence acquéreur (5N)
					String codeAgenceAcquereur = e.substring(offset, offset + 5);
					offset += 5;

					// Code motif du Chargeback (4N)
					String codeMotifChargeback = e.substring(offset, offset + 4);
					offset += 4;

					// Identifiant du Chargeback (16N)
					String identifiantChargeback = e.substring(offset, offset + 16);
					offset += 16;

					// Montant du chargeback (15N)
					String montantChargeback = e.substring(offset, offset + 15);
					offset += 15;

					// Sens du montant (1AN)
					String sensMontant = e.substring(offset, offset + 1);
					offset += 1;

					// RIB du porteur de la carte (20N)
					String ribPorteurCarte = e.substring(offset, offset + 20);
					offset += 20;

					// N° d'autorisation de l'opération initiale (15N)
					String numAutorisationOperationInitiale = e.substring(offset, offset + 15);
					offset += 15;

					// Date de l'opération initiale (8N) - AAAAMMJJ
					String dateOperationInitiale = e.substring(offset, offset + 8);
					offset += 8;

					// FILLER (526) - Zone libre
					String filler = e.substring(offset, Math.min(offset + 526, e.length()));

					String statustechnique = e.substring(Math.min(offset + 526, e.length())-3, Math.min(offset + 526, e.length()));


					if (fileUap.containsKey(identifiantChargeback + numAutorisationOperationInitiale)) {
						UAP052IN uap = fileUap.get(identifiantChargeback + numAutorisationOperationInitiale);

 						uap.setUapRio(uapRIO);
						uap.setDateReglement(dateReglement);
						uAPFransaBank.add(uap);

					}
				});


				uAP052INRepository.saveAll(uAPFransaBank);
//				FileRequest.print("uap 040 matching", FileRequest.getLineNumber());


				logger.info("size valid Transaction 052=>{}", uAPFransaBank.size());
				return 1;

			}

			else {
				FileRequest.print(fileName, FileRequest.getLineNumber());
				batchesFFCRepository.updateStatusAndErrorBatch("CROUAP52", 4, "Missing file!", new Date(), "");
				logger.info("****** missing file cra 040 ********");
				return 0;
			}
		} catch (Exception e) {
			String stackTrace = Throwables.getStackTraceAsString(e);
			logger.info("Exception is=>{}", stackTrace);
			FileRequest.print(fileName, FileRequest.getLineNumber());
			batchesFFCRepository.updateStatusAndErrorBatch("CRAUAP52", 2, "error!", new Date(), "");
			return 2;
		}
	}

	@GetMapping("/GenerationRegCRACRO")
	public ResponseEntity<?> GenerationRegCRACRO(@RequestParam String type, @RequestParam(required = false) List<TransactionNational> transactionLists) throws Exception {
			DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.FRENCH);
			symbols.setDecimalSeparator('.');
			DecimalFormat decimalFormat = new DecimalFormat("0.###", symbols);
			logger.info("debut extraction credit debit account all settlements");
			
			// Calculate today's date in YYYYMMDD format for comparison
			String todayDate = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
			logger.info("Today's date for filtering: {}", todayDate);


			List<Integer> categories = mvbkConfigRepo.findDistinctByStatusLevel(type);
			logger.info("car size " + categories.size());


			recordNumber = 0;


			for (Integer categorie : categories) {
//				for (String categorie : Arrays.asList("24")) {

				System.out.println("categorie " + categorie);
				logger.info("categorie " + categorie);
				List<MvbkConf> listmvbkConf = mvbkConfigRepo.findByCategorie(categorie);



				List<MvbkConf> listMvbkConf = mvbkConfigRepo.findByCategorie(categorie);
			String origin =listMvbkConf.get(0).getTypeOp();
			List<TransactionNational> uaps = new ArrayList<>();
			if (transactionLists!= null){
				uaps=transactionLists;
			}else {
				// Filter based on origin: CHA/CHR
				if ("CHA".equals(origin)) {
					// For CHA: exclude transactionOrigine = '040'
					if (type.contains("UAPREG052IN")) {
						uaps = new ArrayList<>(uAP052INRepository.findByDateReglementIsNotNullAndDateReglementLessThanEqualAndTransactionOrigineNot(todayDate, "040"));
					}
					if (type.contains("UAPREG052FRANSABANK")) {
						uaps = new ArrayList<>(uap052FransaBankRepository.findByDateReglementIsNotNullAndDateReglementLessThanEqualAndTransactionOrigineNot(todayDate, "040"));
					}
					if (type.contains("UAP052IN")) {
						uaps = new ArrayList<>(uAP052INRepository.findByDateReglementIsNullAndTransactionOrigineNot("040"));
					}
					if (type.contains("UAP052FRANSABANK")) {
						uaps = new ArrayList<>(uap052FransaBankRepository.findByDateReglementIsNullAndTransactionOrigineNot("040"));
					}
				} else if ("CHR".equals(origin)) {
					// For CHR: only transactionOrigine = '040'
					if (type.contains("UAPREG052IN")) {
						uaps = new ArrayList<>(uAP052INRepository.findByDateReglementIsNotNullAndDateReglementLessThanEqualAndTransactionOrigine(todayDate, "040"));
					}
					if (type.contains("UAPREG052FRANSABANK")) {
						uaps = new ArrayList<>(uap052FransaBankRepository.findByDateReglementIsNotNullAndDateReglementLessThanEqualAndTransactionOrigine(todayDate, "040"));
					}
					if (type.contains("UAP052IN")) {
						uaps = new ArrayList<>(uAP052INRepository.findByDateReglementIsNullAndTransactionOrigine("040"));
					}
					if (type.contains("UAP052FRANSABANK")) {
						uaps = new ArrayList<>(uap052FransaBankRepository.findByDateReglementIsNullAndTransactionOrigine("040"));
					}
				} else {
					// Default: no filtering by transactionOrigine
					if (type.contains("UAPREG052IN")) {
						uaps = new ArrayList<>(uAP052INRepository.findByDateReglementIsNotNullAndDateReglementLessThanEqual(todayDate));
					}
					if (type.contains("UAPREG052FRANSABANK")) {
						uaps = new ArrayList<>(uap052FransaBankRepository.findByDateReglementIsNotNullAndDateReglementLessThanEqual(todayDate));
					}
					if (type.contains("UAP052IN")) {
						uaps = new ArrayList<>(uAP052INRepository.findByDateReglementIsNull());
					}
					if (type.contains("UAP052FRANSABANK")) {
						uaps = new ArrayList<>(uap052FransaBankRepository.findByDateReglementIsNull());
					}
				}
			}

			if (!uaps.isEmpty()) {
					generateBKMSUAP052(uaps, listMvbkConf);
				}


			}
			//bSCC.generate_file();

			logger.info(">>>>>>>>>>>>>>>>> Last Save DONE");
			logger.info("fin extraction credit debit account all settlements");
			return ResponseEntity.ok().body("Done");

		}
	public void generateBKMSUAP052(List<TransactionNational> uaps, List<MvbkConf> mvbks ) {
		logger.info("begin Generation");
		String name = ExecutorMobileThreads.name;
		List<BkmvtiFransaBank> BkmvtiFransaBanks = new ArrayList<BkmvtiFransaBank>();
		Map<String, List<DayOperationMP>> groupedByTransactionKey = new HashMap<>();

		for (TransactionNational uap : uaps) {

			int indexPieceComptable = getEveIndex1();
			for (MvbkConf mvk : mvbks) {
				int index2 = getEveIndex();
				BkmvtiFransaBanks = TestSigneUAP052(mvk, uap, BkmvtiFransaBanks,
						indexPieceComptable, index2);

			}

		}
		logger.info("end Generation =>{}", BkmvtiFransaBanks.size());
		if (BkmvtiFransaBanks != null && !BkmvtiFransaBanks.isEmpty()) {
			// Filter out null elements from the list
			List<BkmvtiFransaBank> nonNullBkmvtiFransaBanks = BkmvtiFransaBanks.stream()
					.filter(Objects::nonNull)
					.collect(Collectors.toList());

			if (!nonNullBkmvtiFransaBanks.isEmpty()) {
				bkmvtiFransaBankRepository.saveAll(nonNullBkmvtiFransaBanks);
			}

		}
	}
	public BkmvtiFransaBank TestAccountLengthBKM(MvbkConf mvk,
												   BkmvtiFransaBank BkmvtiFransaBank, TransactionNational op) {
		String accountConfig= mvk.getAccount();
		String account= "" ;
		if("PORTEUR".equals(accountConfig)){account=op.getNumRIBEmetteur();}
		else if("COMMERCANT".equals(accountConfig)){account=op.getNumRIBcommercant();}
		else{account=accountConfig;

		}
		int lengAccount=account.length();
		if (lengAccount > 18) {
			String credit = account.substring(8, 18);
			String chapitreCompta = account.substring(8, 14);
			BkmvtiFransaBank.setChapitreComptable(chapitreCompta + getZero(6 - chapitreCompta.length()));
			BkmvtiFransaBank.setNumCompte(credit);
			BkmvtiFransaBank.setCleControleCompte(getSpace(2));
			String codeDes = account.substring(3, 8);
			BkmvtiFransaBank.setAgenceDestinatrice(codeDes);
			BkmvtiFransaBank.setAgenceEmettrice(codeDes);
			BkmvtiFransaBank.setCodeAgenceSaisie(codeDes);
			String codeId = account.substring(3, 8);
			BkmvtiFransaBank.setCodeID("S" + codeId);
		}

		else if (lengAccount >= 6 && lengAccount < 10) {
			BkmvtiFransaBank.setNumCompte(account + getZero(10 - (account.length())));
			BkmvtiFransaBank.setChapitreComptable(account.substring(0, 6) + getZero(6 - account.length()));

			BkmvtiFransaBank.setCleControleCompte(getSpace(2));
			BkmvtiFransaBank.setAgenceDestinatrice(op.getCodeAgence());
			BkmvtiFransaBank.setAgenceEmettrice(op.getCodeAgence());
			BkmvtiFransaBank.setCodeAgenceSaisie(op.getCodeAgence());
			BkmvtiFransaBank.setCodeID("S" + op.getCodeAgence());
		}

		else if (lengAccount < 6) {
			BkmvtiFransaBank.setNumCompte(account + getZero(10 - (account.length())));
			BkmvtiFransaBank.setChapitreComptable(account + getZero(6 - account.length()));
			BkmvtiFransaBank.setCleControleCompte(getSpace(2));

		}

		else if (lengAccount == 10) {
			BkmvtiFransaBank.setNumCompte(account);
			BkmvtiFransaBank.setChapitreComptable(account.substring(0, 6));
			BkmvtiFransaBank.setCleControleCompte(getSpace(2));
			BkmvtiFransaBank.setAgenceDestinatrice(op.getCodeAgence());
			BkmvtiFransaBank.setAgenceEmettrice(op.getCodeAgence());
			BkmvtiFransaBank.setCodeAgenceSaisie(op.getCodeAgence());
			BkmvtiFransaBank.setCodeID("S" + op.getCodeAgence());
		}

		else {
			BkmvtiFransaBank.setNumCompte(account.substring(0, 10));
			BkmvtiFransaBank.setChapitreComptable(account.substring(0, 6));
			BkmvtiFransaBank.setCleControleCompte(getSpace(2));
			BkmvtiFransaBank.setAgenceEmettrice(op.getCodeAgence());
			BkmvtiFransaBank.setCodeAgenceSaisie(op.getCodeAgence());
			BkmvtiFransaBank.setCodeID("S" + op.getCodeAgence());
		}

		return BkmvtiFransaBank;
	}
	public String getZero(int count)

	{

		String zero = "";

		for (int i = 0; i < count; i++)

			zero += "0";

		return zero;

	}
	public String getSpace(int count)
	{

		String Space = "";

		for (int i = 0; i < count; i++)

			Space += " ";

		return Space;

	}
	public BkmvtiFransaBank TestCodeAgenceBKM( MvbkConf mvk,
											   TransactionNational op, BkmvtiFransaBank BkmvtiFransaBank) {

		if ("P".equals(mvk.getCodeAgence())) {

			BkmvtiFransaBank.setAgence(op.getNumRIBEmetteur().substring(3, 8));
			BkmvtiFransaBank.setAgenceEmettrice(BkmvtiFransaBank.getAgence());
			BkmvtiFransaBank.setAgenceDestinatrice(BkmvtiFransaBank.getAgence());
			BkmvtiFransaBank.setCodeAgenceSaisie(BkmvtiFransaBank.getAgence());
			BkmvtiFransaBank.setCodeID("S" + BkmvtiFransaBank.getAgence());
		} else if ("C".equals(mvk.getCodeAgence())) {

			BkmvtiFransaBank.setAgence(op.getCodeAgence());
			BkmvtiFransaBank.setAgenceEmettrice(BkmvtiFransaBank.getAgence());
			BkmvtiFransaBank.setAgenceDestinatrice(BkmvtiFransaBank.getAgence());
			BkmvtiFransaBank.setCodeAgenceSaisie(BkmvtiFransaBank.getAgence());
			BkmvtiFransaBank.setCodeID("S" + BkmvtiFransaBank.getAgence());
		} else {

			BkmvtiFransaBank.setAgence(mvk.getCodeAgence());
			BkmvtiFransaBank.setAgenceEmettrice(BkmvtiFransaBank.getAgence());
			BkmvtiFransaBank.setAgenceDestinatrice(BkmvtiFransaBank.getAgence());
			BkmvtiFransaBank.setCodeAgenceSaisie(BkmvtiFransaBank.getAgence());
			BkmvtiFransaBank.setCodeID("S" + BkmvtiFransaBank.getAgence());
		}

		return BkmvtiFransaBank;
	}
	public BkmvtiFransaBank getTransactionDate(String TransactionDate, BkmvtiFransaBank BkmvtiFransaBank) {
		String year = TransactionDate.substring(0, 4);
		String month = TransactionDate.substring(4, 6);
		String dayy = TransactionDate.substring(6);
		// BkmvtiFransaBank.setDateComptable(dayy + "/" + month + "/" + year);

		LocalDate date = LocalDate.now();
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
		BkmvtiFransaBank.setDateComptable(date.format(formatter));

		BkmvtiFransaBank.setDateValeur(dayy + "/" + month + "/" + year);
		return BkmvtiFransaBank;

	}

	public BkmvtiFransaBank TestAccountAndSigneBKM( int index2, int indexPieceComptable, MvbkConf mvk, TransactionNational op) {
		BkmvtiFransaBank BkmvtiFransaBank = new BkmvtiFransaBank();
		TestAccountLengthBKM(mvk, BkmvtiFransaBank, op);
		BkmvtiFransaBank.setCodeDevice("208");
		String  name = SecurityContextHolder.getContext().getAuthentication().getName();

		if (name.length() > 10) {
			name = name.substring(0, 10);
		}

		BkmvtiFransaBank.setCodeUtilisateur(name + getSpace(10 - name.length()));
		BkmvtiFransaBank.setCodeService("0000");
		BkmvtiFransaBank.setSens(mvk.getSigne());
		BkmvtiFransaBank.setExonerationcommission("O");
		int lengthNumPiece = op.getNumAutorisation().length();
		BkmvtiFransaBank.setNumPiece(op.getNumAutorisation().substring(lengthNumPiece - 10, lengthNumPiece));
		BkmvtiFransaBank.setTauxChange("1" + getSpace(6));
		BkmvtiFransaBank.setCalculmouvementInteragence("N");
		BkmvtiFransaBank.setMouvementAgence("N");
		//// add new fileds //////
		int lengthNumAuth = op.getNumAutorisation().length();
		int lengthRefDossier = (op.getNumTransaction() + op.getDateTransaction()
				+ op.getNumAutorisation().substring(lengthNumAuth - 6, lengthNumAuth)).length();
		BkmvtiFransaBank.setRefDossier(op.getNumTransaction() + op.getDateTransaction()
				+ op.getNumAutorisation().substring(lengthNumAuth - 6, lengthNumAuth)
				+ getSpace(50 - lengthRefDossier));

		TestCodeAgenceBKM(  mvk, op, BkmvtiFransaBank);

		getTransactionDate(op.getDateTransaction(), BkmvtiFransaBank);

		int lengthReferanceLettrage = op.getNumAutorisation().length();

		BkmvtiFransaBank.setReferanceLettrage(op.getDateTransaction().substring(6, op.getDateTransaction().length())
				+ op.getNumAutorisation().substring(lengthReferanceLettrage - 6, lengthReferanceLettrage));

		BkmvtiFransaBank.setCodeDeviceOrigine("208");
		BkmvtiFransaBank.setIdentification(mvk.getIdentification());
		BkmvtiFransaBank.setNumRefTransactions(op.getNumTransaction());
		setSameDataBKM(  BkmvtiFransaBank, index2, indexPieceComptable, op, mvk);

		return BkmvtiFransaBank;

	}

	public BkmvtiFransaBank setSameDataBKM(  BkmvtiFransaBank BkmvtiFransaBank, int index2,
											   int indexPieceComptable, TransactionNational op, MvbkConf mvk) {
		String lib = "";
		BkmvtiFransaBank.setCodeOperation(mvk.getCodeOperation());
		lib = mvk.getLibelle_operation();
		BkmvtiFransaBank.setLibelle(lib + getSpace(40 - lib.length()));
		if (mvk.getLibGenerique() != null && mvk.getLibGenerique().trim() != "") {
			String libgenerique = mvk.getLibGenerique();
			libgenerique = libgenerique.replaceAll("dateTransation", op.getDateTransaction());
			String aut = op.getNumAutorisation();
			int lengthAuth = aut.length();
			libgenerique = libgenerique.replaceAll("numAutorisation", aut.substring(lengthAuth - 6));
			BkmvtiFransaBank.setLibelle(libgenerique + getSpace(40 - lib.length()));
		}
		BkmvtiFransaBank
				.setPieceComptable( mvk.getTypeOp()+op.getTypeTransaction()+ String.format("%05d", indexPieceComptable));
		BkmvtiFransaBank.setNumEvenement(String.format("%06d", indexPieceComptable));
		return BkmvtiFransaBank;
	}

	public String getAmountFormat(double amount) {
		float m = Math.round(Math.abs(amount));
		int a = (int) m;

		String amountFormat = String.format("%018d,%02d", a / 100, a % 100);

		return amountFormat;
	}
	public List<BkmvtiFransaBank> TestSigneUAP052(MvbkConf mvk,
												  TransactionNational uap,
													List<BkmvtiFransaBank> BkmvtiFransaBanks, int indexPieceComptable, int index2 ) {
		BkmvtiFransaBank BkmvtiFransaBank2 = new BkmvtiFransaBank();
		Map<String, BigDecimal> data = new HashMap<>();
		Map<String, String> ComptC = new HashMap<>();
		Map<String, String> ComptD = new HashMap<>();
		data.put("montantTransaction", uap.getMontantTransaction());

//		data.put("montantInterchange", uap.getMontantInterchange());


		try {
			if(Double.valueOf( fIPService.evaluateWithElementswithBigDecimal(mvk.getCodeSettlement(), data).floatValue())<=0) {
				throw new Exception("amount lower than expected");
			}
//					String codeBank = op.getCodeBank();
//					String mvbaccount = mvk.getAccount().substring(0, mvk.getAccount().length() - 3) + codeBank;
			BkmvtiFransaBank2 = TestAccountAndSigneBKM( index2, indexPieceComptable, mvk,
					uap);
			BkmvtiFransaBank2.setMontant(getAmountFormat(fIPService.evaluateWithElementswithBigDecimal(mvk.getCodeSettlement(), data).doubleValue()));
			if ("AGENCE_C".equals(mvk.getEntity())) {
				BkmvtiFransaBank2.setNumCompte(
						BkmvtiFransaBank2.getNumCompte().substring(0, BkmvtiFransaBank2.getNumCompte().length() - 4)
								+ uap.getCodeAgence().substring(1, uap.getCodeAgence().length()));
			} else if ("AGENCE_P".equals(mvk.getEntity())) {
				BkmvtiFransaBank2.setNumCompte(
						BkmvtiFransaBank2.getNumCompte().substring(0, BkmvtiFransaBank2.getNumCompte().length() - 4)
								+ uap.getNumRIBEmetteur().substring(4, 8).length());
			}
			BkmvtiFransaBanks.add(BkmvtiFransaBank2);

			return BkmvtiFransaBanks;
		} catch (Exception e) {
			logger.error(e.getMessage());
			return BkmvtiFransaBanks;

		}
	}


//	@GetMapping("/GenerationRegCRO")
//	public ResponseEntity<?> GenerationRegCRO(){
//
//	}
	@GetMapping("/countDuplicate")
	public ResponseEntity<?> countDuplicate() {
		List<Integer> x = filecontentTRepository.countDuplicate();
		Integer sum = x.stream().collect(Collectors.summingInt(Integer::intValue));
		return ResponseEntity.ok().body(gson.toJson(String.valueOf(sum)));}
	@PostMapping("/setChStatusNational")
	public ResponseEntity<?> setChStatusNational(@RequestBody List<Integer> chargebackIds) {
		if (chargebackIds == null || chargebackIds.isEmpty()) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("No IDs provided");
		}

		List<FileContent> records = filecontentTRepository.findAllById(chargebackIds);
		if (records.isEmpty()) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No records found for provided IDs");
		}

		String today = LocalDate.now(ZoneId.systemDefault()).toString();
		for (FileContent fc : records) {
			fc.setChargebackStatus(com.mss.backOffice.enumType.ChargebackStatus.DONE.getCode());
			// Temporarily store the completion date in udf1 (can be moved to a dedicated field if available)
			fc.setUdf1(today);
		}

		filecontentTRepository.saveAll(records);
		return ResponseEntity.ok().build();
	}

	@GetMapping("/generateUap052out")
	public ResponseEntity<?> generateUap052out(String fileIntegration) {
		try {
			Logger logger = LoggerFactory.getLogger(ChargeBacksController.class);
			logger.info("Starting generateUap052IN method");

			// Get list of DayOperationFransaBank where identification is 'G205' or 'G204'
			List<String> identifications = Arrays.asList("G205", "G204");
			List<DayOperationFransaBank> dayOperations = dayOperationFransaBankRepository
					.findByIdenficationIn(identifications);

			logger.info("Found {} DayOperationFransaBank records with identifications G205 or G204",
					dayOperations.size());

			if (dayOperations.isEmpty()) {
				return ResponseEntity.ok().body(gson.toJson("No records found with identifications G205 or G204"));
			}

			// Generate list of UAP052FransaBank
			List<UAP052FransaBank> uap052List = new ArrayList<>();

			for (DayOperationFransaBank dayOp : dayOperations) {
				UAP052FransaBank uap052 = new UAP052FransaBank();

				try {
					// Copy properties from DayOperationFransaBank to UAP052FransaBank
					PropertyUtils.copyProperties(uap052, dayOp);
				} catch (Exception e) {
					logger.warn("PropertyUtils.copyProperties failed, using manual mapping: {}",
							e.getMessage());
					// Manual mapping for fields that match
					uap052.setTypeTransaction(dayOp.getTypeTransaction());
					uap052.setDateTransaction(dayOp.getDateTransaction());
					uap052.setHeureTransaction(dayOp.getHeureTransaction());
					uap052.setNumAutorisation(dayOp.getNumAutorisation());
					uap052.setNumTransaction(dayOp.getNumRefTransaction());
					uap052.setCodeAgence(dayOp.getCodeAgence());
					uap052.setNumTransaction(dayOp.getNumtransaction());
					uap052.setFileIntegrationDate(fileIntegration);

					uap052.setMontantTransaction( new BigDecimal(dayOp.getMontantTransaction()))  ;
					String refArchivage = dayOp.getRefernceLettrage();

					uap052.setReferenceArchivage(refArchivage);
				}

				// Find FileContent by numRefTransaction and typeOperation='080'
				if (dayOp.getNumRefTransaction() != null) {
					FileContent fileContent = filecontentTRepository
							.findByNumRefTransactionAndTypeTransaction(dayOp.getNumRefTransaction(), "080");

					if (fileContent != null) {
						logger.debug("Found FileContent for numRefTransaction: {}", dayOp.getNumRefTransaction());

						// Copy additional fields from FileContent
						String originalMontantTransaction = fileContent.getMontantTransaction();
						Integer originalIdTransactionOrigine = fileContent.getId();

						// Temporarily set problematic fields to null to avoid type conversion issues
						fileContent.setMontantTransaction(null);

						try {
							// Copy all other properties that are compatible
							PropertyUtils.copyProperties(uap052, fileContent);
						} catch (Exception e) {
							logger.warn("Failed to copy properties from FileContent: {}", e.getMessage());
						} finally {
							// Restore the original values
							fileContent.setMontantTransaction(originalMontantTransaction);
						}

						// Handle field conversions separately
						if (originalMontantTransaction != null) {
							try {
								uap052.setMontantTransaction( new BigDecimal(originalMontantTransaction))  ;
							} catch (NumberFormatException e) {
								logger.warn("Failed to parse montantTransaction: {}", originalMontantTransaction);
							}
						}

						// Set additional fields
						uap052.setTransactionOrigine(fileContent.getTransactionOrigine());
						uap052.setIdTransactionOrigine(originalIdTransactionOrigine);
					} else {
						logger.debug("No FileContent found for numRefTransaction: {} with typeOperation=080",
								dayOp.getNumRefTransaction());
					}
				}

				uap052List.add(uap052);
			}

			// Save to database
			List<UAP052FransaBank> savedRecords = uap052FransaBankRepository.saveAll(uap052List);

			logger.info("Successfully saved {} UAP052FransaBank records", savedRecords.size());

			return ResponseEntity.ok().body(gson.toJson(
					"Successfully generated and saved " + savedRecords.size() + " UAP052 records"));

		} catch (Exception e) {
			Logger logger = LoggerFactory.getLogger(ChargeBacksController.class);
			logger.error("Error in generateUap052IN: {}", e.getMessage(), e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(gson.toJson("Error: " + e.getMessage()));
		}
	}

	@GetMapping("/generateUap052IN")
	public ResponseEntity<?> generateUap052IN(String fileIntegration) {
		try {
			Logger logger = LoggerFactory.getLogger(ChargeBacksController.class);
			logger.info("Starting generateUap052IN method");
			
			// Get list of DayOperationFransaBank where identification is 'G205' or 'G204'
			List<String> identifications = Arrays.asList("G203", "G202");
			List<DayOperationFransaBank> dayOperations = dayOperationFransaBankRepository
					.findByIdenficationIn(identifications);
			
			logger.info("Found {} DayOperationFransaBank records with identifications G203 or G202",
					dayOperations.size());
			
			if (dayOperations.isEmpty()) {
				return ResponseEntity.ok().body(gson.toJson("No records found with identifications G203 or G202"));
			}
			
			// Generate list of UAP052FransaBank
			List<UAP052IN> uap052List = new ArrayList<>();
			
			for (DayOperationFransaBank dayOp : dayOperations) {
				UAP052IN uap052 = new UAP052IN();
				
				try {
					// Copy properties from DayOperationFransaBank to UAP052FransaBank
					PropertyUtils.copyProperties(uap052, dayOp);
				} catch (Exception e) {
					logger.warn("PropertyUtils.copyProperties failed, using manual mapping: {}", 
							e.getMessage());
					// Manual mapping for fields that match
					uap052.setTypeTransaction(dayOp.getTypeTransaction());
					uap052.setDateTransaction(dayOp.getDateTransaction());
					uap052.setHeureTransaction(dayOp.getHeureTransaction());
					uap052.setNumAutorisation(dayOp.getNumAutorisation());
					uap052.setNumTransaction(dayOp.getNumRefTransaction());
					uap052.setCodeAgence(dayOp.getCodeAgence());
					uap052.setNumTransaction(dayOp.getNumtransaction());
					uap052.setMontantTransaction( new BigDecimal(dayOp.getMontantTransaction()))  ;
					String refArchivage = dayOp.getRefernceLettrage();
					uap052.setFileIntegrationDate(fileIntegration);
					uap052.setReferenceArchivage(refArchivage);
 				}
				
				// Find FileContent by numRefTransaction and typeOperation='080'
				if (dayOp.getNumRefTransaction() != null) {
					FileContent fileContent = filecontentTRepository
							.findByNumRefTransactionAndTypeTransaction(dayOp.getNumRefTransaction(), "080");
					
					if (fileContent != null) {
						logger.debug("Found FileContent for numRefTransaction: {}", dayOp.getNumRefTransaction());
						
						// Copy additional fields from FileContent
						String originalMontantTransaction = fileContent.getMontantTransaction();
						Integer originalIdTransactionOrigine = fileContent.getId();
						
						// Temporarily set problematic fields to null to avoid type conversion issues
						fileContent.setMontantTransaction(null);
						
						try {
							// Copy all other properties that are compatible
							PropertyUtils.copyProperties(uap052, fileContent);
						} catch (Exception e) {
							logger.warn("Failed to copy properties from FileContent: {}", e.getMessage());
						} finally {
							// Restore the original values
							fileContent.setMontantTransaction(originalMontantTransaction);
						}
						
						// Handle field conversions separately
						if (originalMontantTransaction != null) {
							try {
								uap052.setMontantTransaction( new BigDecimal(originalMontantTransaction))  ;
							} catch (NumberFormatException e) {
								logger.warn("Failed to parse montantTransaction: {}", originalMontantTransaction);
							}
						}
						
						// Set additional fields
						uap052.setTransactionOrigine(fileContent.getTransactionOrigine());
						uap052.setIdTransactionOrigine(originalIdTransactionOrigine);
					} else {
						logger.debug("No FileContent found for numRefTransaction: {} with typeOperation=080", 
								dayOp.getNumRefTransaction());
					}
				}
				
				uap052List.add(uap052);
			}
			
			// Save to database
			List<UAP052IN> savedRecords = uAP052INRepository.saveAll(uap052List);
			
			logger.info("Successfully saved {} UAP052IN records", savedRecords.size());
			
			return ResponseEntity.ok().body(gson.toJson(
					"Successfully generated and saved " + savedRecords.size() + " UAP052 records"));
			
		} catch (Exception e) {
			Logger logger = LoggerFactory.getLogger(ChargeBacksController.class);
			logger.error("Error in generateUap052IN: {}", e.getMessage(), e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(gson.toJson("Error: " + e.getMessage()));
		}
	}

	@PostMapping("getSwitchTransaction")
	public Page<SwitchTransactionT> getAllTransactions(@RequestBody TransactionReportingRequest transReportingRequest,
																		 @RequestParam(name = "page", defaultValue = "0") int page,
																		 @RequestParam(name = "size", defaultValue = "10") int size,
																		 @RequestParam(name = "sortOn") String sortOn,
																		 @RequestParam(name = "dir") String dir) {

		Order order = null;
		if (dir.equals("desc"))
			order = new Order(Sort.Direction.DESC, sortOn);
		else
			order = new Order(Sort.Direction.ASC, sortOn);

		// Convert empty strings to null for proper filtering
		String pan = (transReportingRequest.getPan() != null && !transReportingRequest.getPan().trim().isEmpty()) 
				? transReportingRequest.getPan() : null;
		String merchantId = (transReportingRequest.getMerchantId() != null && !transReportingRequest.getMerchantId().trim().isEmpty()) 
				? transReportingRequest.getMerchantId() : null;
		String authCode = (transReportingRequest.getAuthCode() != null && !transReportingRequest.getAuthCode().trim().isEmpty()) 
				? transReportingRequest.getAuthCode() : null;
		String terminal = (transReportingRequest.getTerminal() != null && !transReportingRequest.getTerminal().trim().isEmpty()) 
				? transReportingRequest.getTerminal() : null;
		String startDate = (transReportingRequest.getDate() != null && !transReportingRequest.getDate().trim().isEmpty()) 
				? transReportingRequest.getDate() : null;
		String endDate = (transReportingRequest.getEndDate() != null && !transReportingRequest.getEndDate().trim().isEmpty()) 
				? transReportingRequest.getEndDate() : null;
		String switchRRN = (transReportingRequest.getSwitchRRN() != null && !transReportingRequest.getSwitchRRN().trim().isEmpty()) 
				? transReportingRequest.getSwitchRRN() : null;
		Integer chargebackStatus = transReportingRequest.getChargebackStatus();

		Page<SwitchTransactionT> trans = switchTransactionTRepository.getAllOnUsTransactions(
				PageRequest.of(page, size, Sort.by(order)),
				pan,
				merchantId,
				authCode,
				terminal,
				startDate,
				endDate,
				switchRRN,
				chargebackStatus);
		return trans;

	}

	@PostMapping("setChStatus")
	public ResponseEntity<?> setChargebackStatus(@RequestBody List<String> switchCodes) {
		List<SwitchTransactionT> transactions = switchTransactionTRepository.findAllBySwitchCodeIn(switchCodes);
		if (transactions.isEmpty()) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No matching transactions found");
		}
		for (SwitchTransactionT transaction : transactions) {
			transaction.setChargebackStatus(com.mss.backOffice.enumType.ChargebackStatus.PENDING.getCode());
		}
		switchTransactionTRepository.saveAll(transactions);
		return ResponseEntity.ok("Successfully updated " + transactions.size() + " records");
	}
	@GetMapping("/generate-lot")
	public ResponseEntity<String> generateCompensationFile(
			@RequestParam(name = "target", defaultValue = "CRA") String target) {
		try {
			String baseDir;
			baseDir=propertyService.getCompensationfilePathLOT();

			// Fetch list of UAP070IN records

			List<UAP052FransaBank> uap052List = uap052FransaBankRepository.findByDateReglementIsNullOrDateReglementEquals("");
			logger.info("Loaded {} UAP070IN records for file generation", uap052List.size());

			if (baseDir == null || baseDir.trim().isEmpty()) {
				return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
						.body("Destination path is not configured for target: " + target);
			}

			// Ensure directory exists
			Path dirPath = Paths.get(baseDir);
			Files.createDirectories(dirPath);

			// Build filename
			String timestamp = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss").format(LocalDateTime.now());
			String fileName = "LOT" + target.toUpperCase() + "_" + timestamp + ".txt";
			Path filePath = dirPath.resolve(fileName);
			List<TransactionNational> uaps= new ArrayList<>(uap052List);
			// Generate formatted content from UAP070IN records
			String content = generateUAP052File(uaps);

			Files.write(filePath, content.getBytes(StandardCharsets.UTF_8), StandardOpenOption.CREATE_NEW);
			String fileNameORD = "ORD" + target.toUpperCase() + "_" + timestamp + ".txt";

			Path filePathORD = dirPath.resolve(fileNameORD);

			writeInORD(filePathORD,"001");
			logger.info("Generated compensation file with {} records at: {}", uap052List.size(), filePath.toString());
			return ResponseEntity.ok(String.format("Generated file with %d UAP070IN records at: %s",
					uap052List.size(), filePath.toString()));
		} catch (Exception ex) {
			logger.error("Failed to generate compensation file", ex);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body("Failed to generate file: " + ex.getMessage());
		}
	}
	@GetMapping(value = "writeInORD")
	public @ResponseBody ResponseEntity<FileUAPRequest> writeInORD( Path filePath, String sequenceNumber)
			throws ResourceNotFoundException, ExceptionMethod, IOException {

		CodeBankBC codeBankBC = codeBankBCRepository.findIdentifiant();
		StringBuilder str = new StringBuilder("");
		FileUAPRequest fileRequest = new FileUAPRequest();
		String destinationBank = codeBankBC.getIdentifiant();
		String Entete = "ORD" + sequenceNumber + "INLOT" + "000" + sequenceNumber + "052" + "DZD" + getSpace(41) + "\n";
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
		String Date = dateFormat.format(new Date());
		String fileName = destinationBank + "." + sequenceNumber + "." + Date + "." + "ORD";
		str.append(Entete);
		fileRequest.setData(str.toString());
		fileRequest.setNameTitle(fileName);
		// Before line 813:
		if (Files.exists(filePath)) {
			Files.delete(filePath);
			logger.info("Deleted existing file: {}", filePath);
		}
		Files.write(filePath, str.toString().getBytes(StandardCharsets.UTF_8), StandardOpenOption.CREATE_NEW);

		return ResponseEntity.ok().body(fileRequest);
	}
 	public String generateUAP052File(List<TransactionNational> uap052List) {
		try {
			Logger logger = LoggerFactory.getLogger(ChargeBacksController.class);
			logger.info("Starting generateUAP052File method");

			// Retrieve all UAP052FransaBank records
 			CodeBankBC codeBankBC = codeBankBCRepository.findIdentifiant();
			String destinationBank = codeBankBC.getIdentifiant();
			BigDecimal totalAmountFinal = new BigDecimal(0);
			int nbreLigne = uap052List.size();
			String seqNumber = "001";

			
			logger.info("Found {} UAP052FransaBank records", uap052List.size());
			
			// Build file content according to Annexe 1.8 specification
			StringBuilder fileContent = new StringBuilder();
			int counter = 0;

			for (TransactionNational uap : uap052List) {

				
				// Code operation (3N) - always "052" for Chargeback
				fileContent.append(formatField("052", 3, false));
				
				// Reference de la banque du donneur d'ordre (18AN)
				fileContent.append(formatField(uap.getReferenceArchivage(), 18, false));
				
				// Code Banque donneur d'ordre (3N)
				fileContent.append(formatField(uap.getNumRIBEmetteur().substring(0,3), 3, false));
				
				// Code Agence donneur d'ordre (5N)
				fileContent.append(formatField(uap.getNumRIBEmetteur().substring(3, 8), 5, false));
				
				// Date du chargeback (8N) - format AAAAMMJJ (YYYYMMDD)
				fileContent.append(formatField(uap.getDateTransaction(), 8, false));
				
				// Code Banque acquereur (3N)
				fileContent.append(formatField(uap.getCodeBankAcquereur(), 3, false));
				
				// Code Agence acquereur (5N)
				fileContent.append(formatField(uap.getCodeAgence(), 5, false));
				
				// Code motif du Chargeback (4N)
				fileContent.append(formatField(uap.getCodeMotifChargeback(), 4, false));
				
				// Identifiant du Chargeback (16N)
				fileContent.append(formatField(uap.getIdChargeback(), 16, false));
				
				// Montant du chargeback (15N)
				fileContent.append(formatField(uap.getMontantTransaction().multiply(new BigDecimal(100)).toString(), 15, true));
				totalAmountFinal=totalAmountFinal.add(uap.getMontantTransaction());
				// Sens du montant (1AN) - D for debit
				fileContent.append(formatField("D", 1, false));
				
				// RIB du porteur de la carte (20N)
				fileContent.append(formatField(uap.getNumRIBEmetteur(), 20, false));
				
				// N° d'autorisation de l'opération initiale (15N)
				fileContent.append(formatField(uap.getNumAutorisationOperationInitiale(), 15, false));
				
				// Date de l'opération initiale (8N) - AAAAMMJJ
				fileContent.append(formatField(uap.getDateOperationInitiale(), 8, false));
				
				// FILLER (526) - Zone libre
				fileContent.append(formatField("", 526, false));
				fileContent.append(System.getProperty("line.separator"));

				counter++;
			}
			
			// Write to file
			String fileName = "035.000.001.052.DZD.LOT";
			// Build header line: ELOT + Bank ID + Padding + Seq + Currency + Line Count + Total Amount + Filler
			StringBuilder header = new StringBuilder();
			header.append("ELOT");                                                          // File type identifier
			header.append(destinationBank);                                                 // Destination bank code
			header.append("000");                                                           // Fixed padding
			header.append(seqNumber);                                                       // Sequence number
			header.append("DZD");                                                           // Currency code
			header.append(String.format("%04d", nbreLigne));                               // Number of lines (5 digits)
			header.append(String.format("%016d", totalAmountFinal.multiply(new BigDecimal(100)).intValue())); // Total amount in cents (15 digits)
			header.append(getSpace(28));
//			writeFile(fileName, fileContent.toString(), true);
			
			logger.info("Successfully generated UAP052 file with {} records", counter);

			return header.toString() + System.getProperty("line.separator") + fileContent.toString();
					
		} catch (Exception e) {
			Logger logger = LoggerFactory.getLogger(ChargeBacksController.class);
			logger.error("Error in generateUAP052File: {}", e.getMessage(), e);
			return null;
		}
	}
	
	/**
	 * Format field to specified length with padding
	 * @param value The value to format
	 * @param length The target length
	 * @param numeric If true, pad with zeros on left; if false, pad with spaces on right
	 * @return Formatted string
	 */
	private String formatField(String value, int length, boolean numeric) {
		if (value == null) {
			value = "";
		}
		
		if (numeric) {
			// Numeric field - pad with zeros on the left
			return String.format("%" + length + "s", value).replace(' ', '0');
		} else {
			// Alphanumeric field - pad with spaces on the right
			if (value.length() > length) {
				return value.substring(0, length);
			}
			return String.format("%-" + length + "s", value);
		}
	}
	
	/**
	 * Write content to file in the configured directory
	 * @param fileName Base file name
	 * @param content File content
	 * @param allowTimestamp If true, append timestamp to filename
	 */
	private void writeFile(String fileName, String content, boolean allowTimestamp) {
		java.io.File sourceDirectory = new java.io.File(propertyService.getNewFile());
		
		if (!sourceDirectory.exists() || !sourceDirectory.isDirectory()) {
			FileRequest.print(
					"Source directory does not exist or is not a directory: " + sourceDirectory.getAbsolutePath(),
					FileRequest.getLineNumber());
			return;
		}
		
		if (allowTimestamp) {
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
			String timestamp = LocalDateTime.now().format(formatter);
			fileName = fileName + "_" + timestamp;
		}
		
		java.io.File file = new java.io.File(sourceDirectory, fileName);
		try (java.io.FileWriter writer = new java.io.FileWriter(file)) {
			writer.write(content);
			FileRequest.print("File created successfully at: " + file.getAbsolutePath(), FileRequest.getLineNumber());
		} catch (IOException e) {
			FileRequest.print("Error while writing the file: " + e.getMessage(), FileRequest.getLineNumber());
		}
	}
	
	/**
	 * Build reference archivage from transaction reference and last 6 digits of authorization
	 * @param numRefTransaction Transaction reference number
	 * @param numAutorisation Authorization number
	 * @return Combined reference or empty string if inputs are invalid
	 */
	private String buildReferenceArchivage(String numRefTransaction, String numAutorisation) {
		if (numRefTransaction == null || numAutorisation == null) {
			return "";
		}
		
		if (numAutorisation.length() < 6) {
			// If authorization is shorter than 6 chars, use the whole string
			return numRefTransaction + numAutorisation;
		}
		
		// Concatenate transaction ref with last 6 characters of authorization
		return numRefTransaction + numAutorisation.substring(numAutorisation.length() - 6);
	}



}
