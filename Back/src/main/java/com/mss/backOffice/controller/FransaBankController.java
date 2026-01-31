package com.mss.backOffice.controller;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.google.common.base.Throwables;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mss.backOffice.request.AddFileRequest;
import com.mss.backOffice.services.BkmService;
import com.mss.backOffice.services.FormulaInterpreterService;
import com.mss.backOffice.services.PropertyService;
import com.mss.unified.dto.UapDetailsControl;
import com.mss.unified.entities.BatchesFC;
import com.mss.unified.entities.BkmvtiFransaBank;
import com.mss.unified.entities.CraControl;
import com.mss.unified.entities.MvbkConf;
import com.mss.unified.entities.MvbkSettlement;
import com.mss.unified.entities.UAP040IN;
import com.mss.unified.entities.UAP050FransaBank;
import com.mss.unified.entities.UAP050IN;
import com.mss.unified.entities.UAP051FransaBank;
import com.mss.unified.entities.UAP051IN;
import com.mss.unified.entities.UAPFransaBank;
import com.mss.unified.entities.User;
import com.mss.unified.entities.OpeningDay;
import com.mss.unified.entities.UAP040FransaBankNotAccepted;
import com.mss.unified.repositories.BatchesFFCRepository;
import com.mss.unified.repositories.BkmvtiFransaBankRepository;
import com.mss.unified.repositories.CodeBankBCRepository;
import com.mss.unified.repositories.DayOperationFransaBankRepository;
import com.mss.unified.repositories.FileTRepository;
import com.mss.unified.repositories.MvbkConfigRepository;
import com.mss.unified.repositories.MvbkSettlementRepository;
import com.mss.unified.repositories.UAP040INFransaBankRepository;
import com.mss.unified.repositories.UAP050FransaBankRepository;
import com.mss.unified.repositories.UAP050INFransaBankRepository;
import com.mss.unified.repositories.UAP051FransaBankRepository;
import com.mss.unified.repositories.UAP051INFransaBankRepository;
import com.mss.unified.repositories.UAPFransaBankRepository;
import com.mss.unified.repositories.UserRepository;
import com.mss.unified.repositories.OpeningDayRepository;
import com.mss.unified.repositories.CraControlRepository;
import com.mss.unified.repositories.UAP040FransaBankNotAcceptedRepository;

import org.apache.commons.beanutils.PropertyUtils;


@RestController
@RequestMapping("FransaBank")
public  strictfp class FransaBankController {
	@Autowired
	FormulaInterpreterService fIPService;
	@Autowired
	OpeningDayRepository openedDayRepo;
	@Autowired
	DayOperationFransaBankRepository dayRepo;
	private static final Gson gson = new Gson();
	@Autowired
	UAPFransaBankRepository uAPFransaBankRepository;
	@Autowired
	UAP050FransaBankRepository uAP050FransaBankRepository;
	@Autowired
	MvbkSettlementRepository mvbkSettlementRepository;
	@Autowired
	DayOperationFransaBankRepository dayOperationFransaBankRepository;
	@Autowired
	BkmvtiFransaBankRepository bkmvtiFransaBankRepository;

	@Autowired
	BatchesFFCRepository batchesFFCRepository;
	@Autowired
	FileTRepository fileSummaryTRepository;
	@Autowired
	CodeBankBCRepository codeBankBCRepository;
	@Autowired
	BatchesFFCRepository batchRepo;
	@Autowired
	public MvbkConfigRepository mvbkConfigR;

	@Autowired
	UserRepository userRepository;
	@Autowired
	PropertyService propertyService;
	@Autowired
	UAP051FransaBankRepository uAP051FransaBankRepository;
	@Autowired
	UAP040INFransaBankRepository uAP040INFransaBankRepository;
	@Autowired
	UAP050INFransaBankRepository uAP050INFransaBankRepository;
	@Autowired
	UAP051INFransaBankRepository uAP051INFransaBankRepository;
	@Autowired
	BkmService bkmService;
	@Autowired
	private CraControlRepository craControlRepository;
	@Autowired
	private UAP040FransaBankNotAcceptedRepository UAP040NotAcceptedRepository;
	private static final Logger logger = LoggerFactory.getLogger(FransaBankController.class);
	public static final Type typeJsonData = new TypeToken<HashMap<String, Float>>() {
	}.getType();

	public synchronized int getEveIndex() {
		return (++ExecutorThreadUAPINFileBC.eve);
	}

	public synchronized int getEveIndex1() {
		return (++ExecutorThreadUAPINFileBC.eve1);
	}


	@GetMapping("findUap/{uap}/{fileDate}")
	public ResponseEntity<?> uapFinder(@PathVariable(value = "uap") String uap,
			@PathVariable(value = "fileDate") String fileDate) {
		boolean exists = true;
		if ("040".equals(uap)) {
			List<UAPFransaBank> ListUAPFransaBanksFiltred = uAPFransaBankRepository.findAll();
			if (ListUAPFransaBanksFiltred.size() == 0) {
				exists = false;
			}

		}
		if ("050".equals(uap)) {
			List<UAP050FransaBank> ListUAPFransaBanksFiltred = uAP050FransaBankRepository.findAll();
			if (ListUAPFransaBanksFiltred.size() == 0) {
				exists = false;
			}
		}
		if ("051".equals(uap)) {
			List<UAP051FransaBank> ListUAPFransaBanksFiltred = uAP051FransaBankRepository.findAll();
			if (ListUAPFransaBanksFiltred.size() == 0) {
				exists = false;
			}
		}
		if ("040IN".equals(uap)) {
			List<UAP040IN> ListUAPFransaBanksFiltred = uAP040INFransaBankRepository.findReglementByDate(fileDate);
			if (ListUAPFransaBanksFiltred.size() == 0) {
				exists = false;
			}
		}
		if ("050IN".equals(uap)) {
			List<UAP050IN> ListUAPFransaBanksFiltred = uAP050INFransaBankRepository.findReglementByDate(fileDate);
			if (ListUAPFransaBanksFiltred.size() == 0) {
				exists = false;
			}
		}
		if ("051IN".equals(uap)) {
			List<UAP051IN> ListUAPFransaBanksFiltred = uAP051INFransaBankRepository.findReglementByDate(fileDate);
			if (ListUAPFransaBanksFiltred.size() == 0) {
				exists = false;
			}

		}
 			return ResponseEntity.accepted().body(gson.toJson("OK"));
	 
	}

	@GetMapping("forceUap/{uap}")
	public void forceUap(@PathVariable(value = "uap") String uap) {
		if ("040".equals(uap)) {
			if (openedDayRepo.findByStatus040().isPresent()) {
				String fileDate = openedDayRepo.findByStatus040().get().getFileIntegration();
				openedDayRepo.updateStatus040(fileDate, "doneCra");
				batchRepo.updateFinishBatch("CRAUAP40", 1, new Date());
			}

		}
		if ("050".equals(uap)) {
			if (openedDayRepo.findByStatus050().isPresent()) {
				String fileDate = openedDayRepo.findByStatus050().get().getFileIntegration();
				openedDayRepo.updateStatus050(fileDate, "doneCra");
				batchRepo.updateFinishBatch("CRAUAP50", 1, new Date());
			}

		}
		if ("051".equals(uap)) {
			if (openedDayRepo.findByStatus051().isPresent()) {
				String fileDate = openedDayRepo.findByStatus051().get().getFileIntegration();
				openedDayRepo.updateStatus051(fileDate, "doneCra");
				batchRepo.updateFinishBatch("CRAUAP51", 1, new Date());
			}
		}
		if ("040IN".equals(uap)) {
			batchRepo.updateFinishBatch("CRAUAP40IN", 1, new Date());
			if (openedDayRepo.findByStatus040().isPresent()) {
				String fileDate = openedDayRepo.findByStatus040().get().getFileIntegration();
				openedDayRepo.updateStatus040(fileDate, "doneCro");
				batchRepo.updateFinishBatch("CRAUAP40IN", 1, new Date());
			}

		}
		if ("050IN".equals(uap)) {
			batchRepo.updateFinishBatch("CRAUAP50IN", 1, new Date());

			if (openedDayRepo.findByStatus050().isPresent()) {
				String fileDate = openedDayRepo.findByStatus050().get().getFileIntegration();
				openedDayRepo.updateStatus050(fileDate, "doneCro");
				batchRepo.updateFinishBatch("CRAUAP50IN", 1, new Date());
			}

		}
		if ("051IN".equals(uap)) {
			batchRepo.updateFinishBatch("CRAUAP51IN", 1, new Date());
			if (openedDayRepo.findByStatus051().isPresent()) {
				String fileDate = openedDayRepo.findByStatus051().get().getFileIntegration();
				openedDayRepo.updateStatus051(fileDate, "doneCro");
				batchRepo.updateFinishBatch("CRAUAP51IN", 1, new Date());
			}
		}

	}

	@PutMapping("matchingUAP040")
	public String Matching(@RequestBody AddFileRequest addFileRequest) throws IOException {

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
				logger.info("matchingUAP040");
				BatchesFC batch = batchesFFCRepository.findByKey("CRAUAP40").get();
				batch.setBatchLastExcution(batch.getBatchEndDate());
				batch.setBatchStatus(0);
				batch.setBatchDate(new Date());
				batch.setError(null);
				batch.setErrorStackTrace(null);
				batchRepo.saveAndFlush(batch);
				int etat = validTransaction(addFileRequest, openDay.getFileIntegration());

				if (etat == 1) {
					List<UAPFransaBank> ListUAPFransaBanksFiltred = uAPFransaBankRepository
							.getListUAPByStatusTechnique(openDay.getFileIntegration());
					for (UAPFransaBank uap : ListUAPFransaBanksFiltred) {
						if (uap.getTypeTransaction().equals("040") && uap.getTypeRetrait().equals("001")
								&& uap.getTagRetrait().equals("1")) {
							ValidRetraitWithDayOperation(uap);
						} else if (uap.getTypeTransaction().equals("040") && uap.getTypeRetrait().equals("001")
								&& uap.getTagRetrait().equals("2")) {
							ValidRetraitAlgeriePosteWithoutDayOperation(uap);
						} else if (uap.getTypeTransaction().equals("040") && uap.getTypeRetrait().equals("002")) {
							ValidConsultationSoldeWithoutDayOperation(uap);
						}

					}

//					moveFilesByStartingName(addFileRequest.getFilePath(), propertyService.getCompensationfilePath(),
//							addFileRequest.getFileName());

					List<UAPFransaBank> data = uAPFransaBankRepository
							.getListUAPByStatusTechniqueNV(openDay.getFileIntegration());
					if (data.size() > 0) {
						openedDayRepo.updateStatus040(openDay.getFileIntegration(), "doneSortCra");

						batchRepo.updateFinishBatch("CRAUAP40", 5, new Date());
						logger.info("end matching cra 040 with rejection");
					} else {
						OrshesterController.endedOk = true;
						openedDayRepo.updateStatus040(openDay.getFileIntegration(), "doneCra");
						batchRepo.updateFinishBatch("CRAUAP40", 1, new Date());
						logger.info("end matching cra 040");
					}
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

			batchRepo.updateStatusAndErrorBatch("CRAUAP40", 2, error, new Date(), stackTrace);
			return "ERROR";
		}
	}

	public int validTransaction(AddFileRequest addFileRequest, String fileIntegration) throws IOException {
		logger.info("******* begin validTransaction 040 **********");

		// String fileName = addFileRequest.getFilePath() + "/" +
		// addFileRequest.getFileName() + "."+"20"+addFileRequest.getFileDate()+".CRA";
		String fileName = addFileRequest.getFilePath() + "/" + addFileRequest.getFileName() + ".CRA";
		try {
			logger.info("file name cra 040=>{}", fileName);
//			FileRequest.print("uap 040 matching", FileRequest.getLineNumber());
			if (Files.exists(Paths.get(fileName))) {
				logger.info("file cra 040 exist");

				List<UAPFransaBank> UAPFransaBankS = uAPFransaBankRepository.findByFileIntegration(fileIntegration);
				logger.info("UAPFransaBankS size =>{}", UAPFransaBankS.size());
				List<UAPFransaBank> uAPFransaBank = new ArrayList<>();
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
					logger.info("date reglement cra 040 =>{}", dateReg);
					batchesFFCRepository.updateDateReg(dateReg, "GNR01");
					batchesFFCRepository.updateDateReg(dateReg, "CRAUAP40Reglement");
					logger.info("save date reglement in summary 040 =>{}", dateReg);

				}
				FileRequest.print("uap 040 matching", FileRequest.getLineNumber());

				Map<String, UAPFransaBank> fileUap = new HashMap<>();

				UAPFransaBankS.forEach(u -> {

					int lenNumAutorisationUap = u.getNumAutorisation().length();
					String PAN = u.getCodeBin().trim() + u.getTypeCarteDebit().trim() + u.getNumSeq().trim()
							+ u.getNumOrdre().trim() + u.getCle().trim();
					String numAutorisationUap = u.getNumAutorisation().substring(lenNumAutorisationUap - 6,
							lenNumAutorisationUap);

					fileUap.put(numAutorisationUap + PAN + u.getNumtransaction(), u);

				});
				FileRequest.print("uap 040 matching", FileRequest.getLineNumber());

				cras.forEach(e -> {
					String dateReglement = e.substring(38, 46);
					String CraBefore = e.substring(46, e.length());
					String numAutorisationCra = CraBefore.substring(134, 140);
					String dateTransactionCra = CraBefore.substring(98, 106);
					String numTransaction = CraBefore.substring(86, 86 + 12);

					String PanCRA = CraBefore.substring(32, 48);
					if (fileUap.containsKey(numAutorisationCra + PanCRA + numTransaction)) {
						UAPFransaBank uap = fileUap.get(numAutorisationCra + PanCRA + numTransaction);
						String montantRetrait = CraBefore.substring(63, 78);
						String montantComission = CraBefore.substring(79, 86);
						String montantCompenser = CraBefore.substring(48, 63);
						if ((uap.getDateTransaction().trim()).equals(dateTransactionCra)
								&& ((uap.getMontantRetrait().trim()).equals(montantRetrait))
								&& ((uap.getMontantAComponser().trim()).equals(montantCompenser))
								&& ((uap.getMontantCommission().trim()).equals(montantComission)))

						{
//							logger.info("valid transaction 040 with =>{}", numAutorisationCra + PanCRA);
							uap.setStatusTechnique("000");
							uap.setUapRio(e.substring(0, 38));
							uap.setDateReglement(dateReglement);
							uAPFransaBank.add(uap);
						}
					}

				});
				/*
				 * cras.forEach(e -> { String CraBefore = e.substring(46, e.length()); String
				 * status = CraBefore.substring(CraBefore.length() - 3, CraBefore.length());
				 * String dateTransactionCra = CraBefore.substring(98, 106); String
				 * numAutorisationCra = CraBefore.substring(134, 140); String montantRetrait =
				 * CraBefore.substring(63, 78); String PanCRA = CraBefore.substring(32, 48);
				 * String montantComission = CraBefore.substring(79, 86); String
				 * montantCompenser = CraBefore.substring(48, 63); if (status.equals("000")) {
				 * logger.info("status technique"); for (UAPFransaBank u : UAPFransaBankS) {
				 * logger.info("UAPFransaBankS"); int lenNumAutorisationUap =
				 * u.getNumAutorisation().length(); String PAN = u.getCodeBin().trim() +
				 * u.getTypeCarteDebit().trim() + u.getNumSeq().trim() + u.getNumOrdre().trim()
				 * + u.getCle().trim(); String numAutorisationUap =
				 * u.getNumAutorisation().substring(lenNumAutorisationUap - 6,
				 * lenNumAutorisationUap);
				 * logger.info("u.getDateTransaction()=>",u.getDateTransaction());
				 * logger.info("numAutorisationCra=>",numAutorisationCra);
				 * logger.info("u.getMontantRetrait()=>",u.getMontantRetrait());
				 * logger.info("PAN=>",PanCRA);
				 * logger.info("u.getMontantAComponser()=>",montantCompenser);
				 * logger.info("u.montantComission()=>",montantComission); if
				 * ((u.getDateTransaction().trim()).equals(dateTransactionCra) &&
				 * (numAutorisationUap).equals(numAutorisationCra) &&
				 * ((u.getMontantRetrait().trim()).equals(montantRetrait)) &&
				 * (PAN.equals(PanCRA) &&
				 * ((u.getMontantAComponser().trim()).equals(montantCompenser)) &&
				 * ((u.getMontantCommission().trim()).equals(montantComission))))
				 * 
				 * { logger.info("transaction"); u.setStatusTechnique("000");
				 * u.setFileIntegrationDate(addFileRequest.getFileDate());
				 * u.setUapRio(e.substring(0, 38)); uAPFransaBank.add(u); } }
				 * 
				 * }
				 * 
				 * });
				 */

				uAPFransaBankRepository.saveAll(uAPFransaBank);
//				FileRequest.print("uap 040 matching", FileRequest.getLineNumber());
				
				/////////// save cra LOT control /////////////
				saveCraControl(fileIntegration);
				///////////////////////////////////////////
				logger.info("size valid Transaction 040=>{}", uAPFransaBank.size());
				return 1;

			}

			else {
				FileRequest.print(fileName, FileRequest.getLineNumber());
				batchRepo.updateStatusAndErrorBatch("CRAUAP40", 4, "Missing file!", new Date(), "");
				logger.info("****** missing file cra 040 ********");
				return 0;
			}
		} catch (Exception e) {
			String stackTrace = Throwables.getStackTraceAsString(e);
			logger.info("Exception is=>{}", stackTrace);
			FileRequest.print(fileName, FileRequest.getLineNumber());
			batchRepo.updateStatusAndErrorBatch("CRAUAP40", 2, "error!", new Date(), "");
			return 2;
		}
	}
	private void saveCraControl(String fileIntegration) {
		List<CraControl> craControlList=craControlRepository.findByProcessingDateAndLotType(fileIntegration,"040");
		
		if (craControlList.size()>0) {
			
			List<UapDetailsControl>  acceptedUpa40= uAPFransaBankRepository.getListUAP40AcceptedForControl(fileIntegration);
			List<UAPFransaBank>  notAcceptedUpa40= uAPFransaBankRepository.getListUAP40NotAcceptedForControl(fileIntegration);
			
			long sumAcceptedUpa40=0;
			for (UapDetailsControl el : acceptedUpa40) {
	    		sumAcceptedUpa40+=
	    				Long.valueOf(el.getMontantAComponser().replace(".", "")) ;
	    		
	    		
			}
			
			
			long sumNotAcceptedUpa40=0;
			
			List<UAP040FransaBankNotAccepted> notAcceptedList= new ArrayList<UAP040FransaBankNotAccepted>();
			for (UAPFransaBank el : notAcceptedUpa40) {
				sumNotAcceptedUpa40+=
	    				Long.valueOf(el.getMontantAComponser().replace(".", "")) ;
				
				
				UAP040FransaBankNotAccepted notAccespted = new UAP040FransaBankNotAccepted();
				try {
					PropertyUtils.copyProperties(notAccespted, el);
				} catch (Exception ex) {
					logger.info("Exception");
					logger.info( Throwables.getStackTraceAsString(ex));
				}
				notAcceptedList.add(notAccespted);
	    		
			}
			
			
			for(CraControl craControl :craControlList) {
				
				//if (craControl.isPresent()) {
					logger.info("cra control is found");
					//List<UapDetailsControl>  acceptedUpa40= uAPFransaBankRepository.getListUAP40AcceptedForControl(fileIntegration);
					
					craControl.setSumAccepted(sumAcceptedUpa40);
					craControl.setNbAccepted(acceptedUpa40.size());
					
					
					craControl.setSumNotAccepted(sumNotAcceptedUpa40);
					craControl.setNbNotAccepted(notAcceptedUpa40.size());

					
					craControlRepository.save(craControl);
				//}
				
			}
			
			UAP040NotAcceptedRepository.saveAll(notAcceptedList);

			
		}

	}

	public Stream<String> extractData(AddFileRequest addFileRequest) throws IOException {
		logger.info("extractData");

		Stream<String> fileRows = null;
		List<String> filesNames = listAndFilterFiles(addFileRequest.getFilePath(), "040.DZD.CRA");
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

	public void ValidRetraitWithDayOperation(UAPFransaBank uap) {
//		logger.info("****** ValidRetrait cra 040 *******");

		List<MvbkSettlement> allMvbkSettelemnts = mvbkSettlementRepository.findByIdentificationCRARetrait();
		List<BkmvtiFransaBank> BkmvtiFransaBanks = new ArrayList<BkmvtiFransaBank>();
		List<MvbkConf> allMvbkSettelemntsC = mvbkConfigR.findByCategorie(21);

		int indexPieceComptable = getEveIndex1();
		for (MvbkConf mvk : allMvbkSettelemntsC) {
			int index2 = getEveIndex();
			BkmvtiFransaBanks = TestSigneWithoutDayOperation(uap, 3, mvk, BkmvtiFransaBanks, indexPieceComptable,
					index2, mvk.getCodeSettlement());

		}

		if (BkmvtiFransaBanks != null && !BkmvtiFransaBanks.isEmpty()) {
		    // Filter out null elements from the list
		    List<BkmvtiFransaBank> nonNullBkmvtiFransaBanks = BkmvtiFransaBanks.stream()
		            .filter(Objects::nonNull)
		            .collect(Collectors.toList());

		    if (!nonNullBkmvtiFransaBanks.isEmpty()) {
		        bkmvtiFransaBankRepository.saveAll(nonNullBkmvtiFransaBanks);
		    }
		    bkmvtiFransaBankRepository.saveAll(BkmvtiFransaBanks);
		}
	}

	public void ValidRetraitAlgeriePosteWithoutDayOperation(UAPFransaBank uap) {
//		logger.info("******** Valid RetraitAlgerie Poste cra 040********");

		List<MvbkSettlement> allMvbkSettelemntsALP = mvbkSettlementRepository.findByIdentificationCRARetraitALP();
		List<BkmvtiFransaBank> BkmvtiFransaBanksALP = new ArrayList<BkmvtiFransaBank>();
		List<MvbkConf> allMvbkSettelemntsC = mvbkConfigR.findByCategorie(49);

		int indexPieceComptable = getEveIndex1();

		for (MvbkConf mvk : allMvbkSettelemntsC) {
			int index2 = getEveIndex();
			BkmvtiFransaBanksALP = TestSigneWithoutDayOperation(uap, 15, mvk, BkmvtiFransaBanksALP, indexPieceComptable,
					index2, mvk.getCodeSettlement());

		}

		bkmvtiFransaBankRepository.saveAll(BkmvtiFransaBanksALP);

//		logger.info("end Valid Retrait AlgeriePoste cra 040=>{}", BkmvtiFransaBanksALP.size());

	}

	public void ValidConsultationSoldeWithoutDayOperation(UAPFransaBank uap) {
//		logger.info("***** ValidConsultationSolde cra 040******");
		List<MvbkSettlement> allMvbkSettelemnts = mvbkSettlementRepository.findByIdentificationCRAConsultationSolde();
		List<BkmvtiFransaBank> BkmvtiFransaBanksC = new ArrayList<BkmvtiFransaBank>();
		List<MvbkConf> allMvbkSettelemntsC = mvbkConfigR.findByCategorie(22);

		int indexPieceComptable = getEveIndex1();

		for (MvbkConf mvk : allMvbkSettelemntsC) {
			int index2 = getEveIndex();
			BkmvtiFransaBanksC = TestSigneWithoutDayOperation(uap, 11, mvk, BkmvtiFransaBanksC, indexPieceComptable,
					index2, mvk.getCodeSettlement());

		}

		bkmvtiFransaBankRepository.saveAll(BkmvtiFransaBanksC);

//		logger.info("end ValidConsultationSolde cra 040 =>{}", BkmvtiFransaBanksC.size());
	}

	public List<BkmvtiFransaBank> TestSigneWithoutDayOperation(UAPFransaBank uap, int methode, MvbkConf mvk,
			List<BkmvtiFransaBank> BkmvtiFransaBanks, int indexPieceComptable, int index2, String codeOperation) {
		BkmvtiFransaBank bkmvtiFransaBank = new BkmvtiFransaBank();

		if (AccountSigne(mvk.getAccount(), mvk.getSigne()) == 1 || AccountSigne(mvk.getAccount(), mvk.getSigne()) == 2
				|| AccountSigne(mvk.getAccount(), mvk.getSigne()) == 3) {
			bkmvtiFransaBank = TestAccountAndSigneWithoutSayOperation(uap, methode, index2, indexPieceComptable, 1,
					mvk.getAccount(), mvk.getSigne(), mvk, mvk.getAccount());
			BkmvtiFransaBanks.add(bkmvtiFransaBank);

		}

		return BkmvtiFransaBanks;
	}

	public BkmvtiFransaBank TestAccountAndSigneWithoutSayOperation(UAPFransaBank uap, int methode, int index2,
			int indexPieceComptable, int test, String account, String signe, MvbkConf mvk, String cle) {
		BkmvtiFransaBank bkmvtiFransaBank = new BkmvtiFransaBank();
		TestAccountLengthWithoutDayOperation(uap, account.length(), account, mvk, bkmvtiFransaBank, cle);
		bkmvtiFransaBank.setCodeDevice("208");
		String name = SecurityContextHolder.getContext().getAuthentication().getName();
		Optional<User> user = userRepository.findByUserNameOrUserEmail(name, name);
		if (!user.isPresent()) {
			throw new RuntimeException("Error saving status !");
		}
		String nameUser = user.get().getUserName();
		if (nameUser.length() > 10) {

			nameUser = nameUser.substring(0, 10);
		}
		bkmvtiFransaBank.setCodeUtilisateur(nameUser + getSpace(10 - user.get().getUserName().length()));
		// bkmvtiFransaBank.setCodeUtilisateur("UTI1"+getSpace(7));
		bkmvtiFransaBank.setCodeService("0000");
		bkmvtiFransaBank.setSens(mvk.getSigne());
		bkmvtiFransaBank.setExonerationcommission("O");
		int lengthNumPiece = uap.getNumAutorisation().length();
		bkmvtiFransaBank.setNumPiece(uap.getNumAutorisation().substring(lengthNumPiece - 11, lengthNumPiece));
		bkmvtiFransaBank.setTauxChange("1" + getSpace(6));
		bkmvtiFransaBank.setCalculmouvementInteragence("N");
		bkmvtiFransaBank.setMouvementAgence("N");
		int lengthNumAuth = uap.getNumAutorisation().length();
		int lengthRefDossier = (uap.getNumtransaction() + uap.getDateTransaction()
				+ uap.getNumAutorisation().substring(lengthNumAuth - 6, lengthNumAuth)).length();

		bkmvtiFransaBank.setRefDossier(uap.getNumtransaction() + uap.getDateTransaction()
				+ uap.getNumAutorisation().substring(lengthNumAuth - 6, lengthNumAuth)
				+ getSpace(50 - lengthRefDossier));

		TestCodeAgenceWithoutDayOperation(uap, mvk.getCodeAgence(), mvk, bkmvtiFransaBank);

		getTransactionDate(uap.getDateTransaction(), bkmvtiFransaBank);

		int lengthReferanceLettrage = uap.getNumAutorisation().length();

		bkmvtiFransaBank.setReferanceLettrage(uap.getDateTransaction().substring(6, uap.getDateTransaction().length())
				+ uap.getNumAutorisation().substring(lengthReferanceLettrage - 6, lengthReferanceLettrage));


		HashMap<String, BigDecimal> data = new HashMap<String, BigDecimal>();
		BigDecimal mntRet= new BigDecimal( uap.getMontantTranasction());
		BigDecimal commConf= new BigDecimal(  uap.getMontantCommissionFSBKHt());
		BigDecimal commTVA= new BigDecimal(  uap.getMontantCommissionTVA());
  
		data.put("MntRet", mntRet);
		data.put("commFSBKHT", commConf);
		data.put("commTVA", commTVA);
        BigDecimal roundedValue = fIPService.evaluateWithElementswithBigDecimal(mvk.getCodeSettlement(), data).divide(new BigDecimal(100));

        DecimalFormat decimalFormat = new DecimalFormat("000000000000000000.00");
        String formattedValue = decimalFormat.format(roundedValue);

        // Replace the decimal point with a comma
        formattedValue = formattedValue.replace('.', ',');

        // Print the formatted value
        bkmvtiFransaBank.setMontant(formattedValue);
        
		
		
		
		
		
		bkmvtiFransaBank.setCodeDeviceOrigine("208");
		bkmvtiFransaBank.setIdentification(mvk.getIdentification());
		bkmvtiFransaBank.setNumRefTransactions(uap.getNumtransaction());

		setSameDataWithoutDayOperation(uap, methode, bkmvtiFransaBank, index2, indexPieceComptable, mvk);
		try {

			if (Double.valueOf(bkmvtiFransaBank.getMontant().replace(",", ".")) <= 0) {
				throw new Exception("amount lower than expected");
			}
		} catch (Exception e) {
			return null;
		}
		return bkmvtiFransaBank;

	}

	public BkmvtiFransaBank TestAccountLengthWithoutDayOperation(UAPFransaBank uap, int lengAccount, String Account,
			MvbkConf mvk, BkmvtiFransaBank bkmvtiFransaBank, String cle) {

		if (lengAccount > 18) {

			String credit = Account.substring(8, 18);
			String chapitreCompta = Account.substring(8, 14);
			bkmvtiFransaBank.setChapitreComptable(chapitreCompta + getZero(6 - chapitreCompta.length()));
			bkmvtiFransaBank.setNumCompte(credit);
			bkmvtiFransaBank.setCleControleCompte(getSpace(2));
			String codeDes = Account.substring(3, 8);
			bkmvtiFransaBank.setAgenceDestinatrice(codeDes);
			bkmvtiFransaBank.setAgenceEmettrice(codeDes);
			bkmvtiFransaBank.setCodeAgenceSaisie(codeDes);
			String codeId = Account.substring(3, 8);
			bkmvtiFransaBank.setCodeID("S" + codeId);
		}

		else if (lengAccount >= 6 && lengAccount < 10) {
			bkmvtiFransaBank.setNumCompte(Account + getZero(10 - (Account.length())));
			bkmvtiFransaBank.setChapitreComptable(Account.substring(0, 6) + getZero(6 - Account.length()));

			bkmvtiFransaBank.setCleControleCompte(getSpace(2));
			bkmvtiFransaBank.setAgenceDestinatrice(uap.getCodeAgence());
			bkmvtiFransaBank.setAgenceEmettrice(uap.getCodeAgence());
			bkmvtiFransaBank.setCodeAgenceSaisie(uap.getCodeAgence());
			bkmvtiFransaBank.setCodeID("S" + uap.getCodeAgence());
		}

		else if (lengAccount < 6) {
			bkmvtiFransaBank.setNumCompte(Account + getZero(10 - (Account.length())));
			bkmvtiFransaBank.setChapitreComptable(Account + getZero(6 - Account.length()));
			bkmvtiFransaBank.setCleControleCompte(getSpace(2));
			bkmvtiFransaBank.setAgenceDestinatrice(uap.getCodeAgence());
			bkmvtiFransaBank.setAgenceEmettrice(uap.getCodeAgence());
			bkmvtiFransaBank.setCodeAgenceSaisie(uap.getCodeAgence());
			bkmvtiFransaBank.setCodeID("S" + uap.getCodeAgence());
		}

		else if (lengAccount == 10) {
			bkmvtiFransaBank.setNumCompte(Account);
			bkmvtiFransaBank.setChapitreComptable(Account.substring(0, 6));
			bkmvtiFransaBank.setCleControleCompte(getSpace(2));
			bkmvtiFransaBank.setAgenceDestinatrice(uap.getCodeAgence());
			bkmvtiFransaBank.setAgenceEmettrice(uap.getCodeAgence());
			bkmvtiFransaBank.setCodeAgenceSaisie(uap.getCodeAgence());
			bkmvtiFransaBank.setCodeID("S" + uap.getCodeAgence());
		}

		else {
			bkmvtiFransaBank.setNumCompte(Account.substring(0, 10));
			bkmvtiFransaBank.setChapitreComptable(Account.substring(0, 6));
			bkmvtiFransaBank.setCleControleCompte(getSpace(2));
			bkmvtiFransaBank.setAgenceEmettrice(uap.getCodeAgence());
			bkmvtiFransaBank.setCodeAgenceSaisie(uap.getCodeAgence());
			bkmvtiFransaBank.setCodeID("S" + uap.getCodeAgence());
		}

		return bkmvtiFransaBank;
	}

	public BkmvtiFransaBank setSameDataWithoutDayOperation(UAPFransaBank uap, int methode,
			BkmvtiFransaBank bkmvtiFransaBank, int index2, int indexPieceComptable, MvbkConf mvb) {
		try {
			String lib = "";
			bkmvtiFransaBank.setCodeOperation(mvb.getCodeOperation());
			lib = mvb.getLibelle_operation();
			bkmvtiFransaBank.setLibelle(lib + getSpace(40 - lib.length()));
			if (mvb.getLibGenerique() != null && mvb.getLibGenerique().trim() != "") {
				String libgenerique = mvb.getLibGenerique();
				libgenerique = libgenerique.replaceAll("dateTransation", uap.getDateTransaction());
				String aut = uap.getNumAutorisation();
				int lengthAuth = aut.length();
				libgenerique = libgenerique.replaceAll("numAutorisation", aut.substring(lengthAuth - 6));
				bkmvtiFransaBank.setLibelle(libgenerique + getSpace(40 - lib.length()));
			}
			switch (methode) {

			case 3:

				bkmvtiFransaBank.setPieceComptable(uap.getPieceComptableBKM());

				bkmvtiFransaBank.setNumEvenement(uap.getPieceComptableBKM()
						.substring(uap.getPieceComptableBKM().length() - 6, uap.getPieceComptableBKM().length()));
				break;

			case 11:

				bkmvtiFransaBank.setPieceComptable(uap.getPieceComptableBKM());

				bkmvtiFransaBank.setNumEvenement(uap.getPieceComptableBKM()
						.substring(uap.getPieceComptableBKM().length() - 6, uap.getPieceComptableBKM().length()));

				break;

			case 15:

				bkmvtiFransaBank.setPieceComptable(uap.getPieceComptableBKM());

				bkmvtiFransaBank.setNumEvenement(uap.getPieceComptableBKM()
						.substring(uap.getPieceComptableBKM().length() - 6, uap.getPieceComptableBKM().length()));
				break;

			default:
				bkmvtiFransaBank.setPieceComptable(uap.getPieceComptableBKM());
				bkmvtiFransaBank.setNumEvenement(uap.getPieceComptableBKM()
						.substring(uap.getPieceComptableBKM().length() - 6, uap.getPieceComptableBKM().length()));			}
			return bkmvtiFransaBank;
		} catch (Exception e) {
			FileRequest.print(uap.toString(), FileRequest.getLineNumber());
			return bkmvtiFransaBank;
		}
	}

	public BkmvtiFransaBank TestCodeAgenceWithoutDayOperation(UAPFransaBank uap, String codeAgence, MvbkConf mvk,
			BkmvtiFransaBank bkmvtiFransaBank) {
		if (mvk.getCodeAgence() != null) {
			if (mvk.getCodeAgence().equals("00002")) {
				bkmvtiFransaBank.setAgence(mvk.getCodeAgence());
			} else {
				bkmvtiFransaBank.setAgence(uap.getCodeAgence());
			}
		}

		else {
			bkmvtiFransaBank.setAgence(uap.getCodeAgence());
		}
		return bkmvtiFransaBank;
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

	public int AccountSigne(String account, String signe) {
		int test = 0;
		if ((account.equals("PORTEUR") || (account.equals("COMMERCANT")) || (account.equals("ATM")))
				&& signe.equals("C")) {
			test = 1;
		} else if ((account.equals("PORTEUR") || (account.equals("COMMERCANT")) || (account.equals("ATM")))
				&& signe.equals("D")) {
			test = 2;
		} else if ((!account.equals("PORTEUR") && !account.equals("COMMERCANT") && !account.equals("ATM"))
				&& (signe.equals("C") || signe.equals("D"))) {
			test = 3;
		}

		return test;
	}

	public BkmvtiFransaBank getTransactionDate(String TransactionDate, BkmvtiFransaBank bkmvtiFransaBank) {
		String year = TransactionDate.substring(0, 4);
		String month = TransactionDate.substring(4, 6);
		String dayy = TransactionDate.substring(6);
		LocalDate date = LocalDate.now();
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
		bkmvtiFransaBank.setDateComptable(date.format(formatter));

		bkmvtiFransaBank.setDateValeur(dayy + "/" + month + "/" + year);
		return bkmvtiFransaBank;

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

}
