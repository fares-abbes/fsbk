package com.mss.backOffice.controller;

import java.io.*;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.mss.backOffice.request.ExtractDataDto;
import com.mss.backOffice.request.ReadFilesDto;
import com.mss.backOffice.request.UapEntity;
import com.mss.unified.entities.*;
import com.mss.unified.repositories.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.google.common.base.Throwables;
import com.mss.backOffice.request.AddFileRequest;
import com.mss.backOffice.services.PropertyService;

import static com.mss.backOffice.controller.ExecutorMobileThreads.moveMatchedFiles;

@RestController
@RequestMapping("UAP070IN")
public class UAP070INController {
	
	private static final Logger logger = LoggerFactory.getLogger(UAP070INController.class);
	private static final String DONE_CRA = "DoneCRA";
	private static HashMap<String, String> rioList;
	private static HashMap<String, String> reglementList;
	private static List<UapEntity> duplicateElements;
	private static String codeBank = "035";
	private static int duplicatecount = 0;
	private static int max = 1;
	@Autowired
	BkmvtiFransaBankMobileRepository bkmvtiFransaBankMobileRepository;
	@Autowired
	OpeningDayMRepository openingDayMRepository;
	@Autowired
	OpeningDayRepository openedDayRepo;
	@Autowired
	UAP070INRepository uAP070INRepository;
	
	@Autowired
	UAP070INRepository uap070INRepository;
	
	@Autowired
	BatchesFFCRepository batchRepo;
	
	@Autowired
	PropertyService propertyService;
    @Autowired
    private FileHeaderMRepository fileHeaderMRepository;
    @Autowired
    private OpeningDayRepository openingDayRepository;

	@PutMapping("matchingUAP070IN")
	public String matchingUAP070IN(@RequestBody AddFileRequest addFileRequest) throws IOException {
		
		logger.info("Starting UAP070IN matching process");
		
		try {
			// Check if there's an open day for processing
			if (openingDayMRepository.findByStatus070(DONE_CRA).isPresent()) {
				OpeningDayM openDay = openingDayMRepository.findByStatus070(DONE_CRA).get();
				openDay.setLotIncrementNb(1);
				openDay.setLotIncrementNbCra(1);
				openingDayMRepository.saveAndFlush(openDay);
				
				logger.info("matchingUAP070IN for file integration: {}", openDay.getFileIntegration());
				
				// Update batch status
				BatchesFC batch = batchRepo.findByKey("CRAUAP070IN").orElse(new BatchesFC());
				batch.setKey("CRAUAP070IN");
				batch.setBatchLastExcution(batch.getBatchEndDate());
				batch.setBatchStatus(0);
				batch.setBatchDate(new Date());
				batch.setError(null);
				batch.setErrorStackTrace(null);
				String craFilePaths = addFileRequest.getFilePath();
				batchRepo.saveAndFlush(batch);
				
				// Execute validation
				int etat = validTransactionUAP070IN(addFileRequest);

				if (etat == 1) {
					logger.info("UAP070IN matching completed successfully");
					List<String> listFiles = listAndFilterFiles(addFileRequest.getFilePath(), "070.DZD.CRO");
					List<Path> filePaths = listFiles.stream()
							.map(fileName -> Paths.get(addFileRequest.getFilePath(), fileName))
							.collect(Collectors.toList());
					moveMatchedFiles(filePaths, propertyService.getCompensationfilePathCro(), "CRO");

					return "Done";
				} else {
					logger.warn("UAP070IN matching completed with status: {}", etat);
					return "Completed with warnings";
				}
			}
			
			logger.warn("No open day found for UAP070IN matching");
			BatchesFC batcheError = batchRepo.findByKey("execStatusM").get();
			batcheError.setBatchNumber(2);
			batchRepo.save(batcheError);
			// Move matched CRA files to archive/processed folder

			return "No open day found";
			
		} catch (Exception e) {
			String stackTrace = Throwables.getStackTraceAsString(e);
			logger.error("Exception in UAP070IN matching: {}", stackTrace);
			
			if (stackTrace.length() > 4000) {
				stackTrace = stackTrace.substring(0, 3999);
			}
			
			String error = e.getMessage() == null ? e.toString() : e.getMessage();
			if (error.length() > 255) {
				error = error.substring(0, 254);
			}
			
			batchRepo.updateStatusAndErrorBatch("CRAUAP070IN", 2, error, new Date(), stackTrace);
			return "ERROR";
		}
	}
	public int validTransactionUAP070IN(AddFileRequest addFileRequest) throws IOException {
		try {
			String lastNumIndex = bkmvtiFransaBankMobileRepository.getLastNumIndex();
			max = (lastNumIndex == null) ? 1 : Integer.valueOf(lastNumIndex);
		} catch (Exception e) {
			max = 1; // Default value in case of an exception
			logger.error("An exception occurred while retrieving the last number index: {}", e.getMessage(), e);
		}
		List<UAP070IN> UAPFransaBankS = uAP070INRepository.findAll();

		List<String> list = new ArrayList<>();

		Map<String, String> fileUap = new HashMap<>();
		rioList = new HashMap<>();
		duplicateElements = new ArrayList<>();
		reglementList = new HashMap<>();
		ExtractDataDto data = extractData(addFileRequest);
		// list = extractData(addFileRequest);
		list = data.getLines();
		FileRequest.print(" " + list.size(), FileRequest.getLineNumber());

		for (String element : list) {
			String ligne = element.substring(46, element.length());
			String uapRIO = element.substring(0, 46);
			String numAutorisation = ligne.substring(63, 78); // '000K3HQMCRF5Z3H' - 16 chars
			String numRefTransaction = ligne.substring(109, 121); // '4YQXSUQXMVLY' - 12 chars
			logger.info(" " + numAutorisation );
			logger.info(" " + numRefTransaction );

			if (!fileUap.containsKey(numAutorisation + numRefTransaction)
//					&& uAP051INHR.findinHistorys(numAutorisationCro, PanCro, numTransaction).isEmpty()
			) {
				fileUap.put(numAutorisation + numRefTransaction, ligne);
				rioList.put(numAutorisation + numRefTransaction, element.substring(0, 38));
				reglementList.put(numAutorisation + numRefTransaction, element.substring(38, 46));

			} else {

				logger.info("" + fileUap.size() );
				logger.info("" + numAutorisation );
				logger.info("" + numRefTransaction );
				UapEntity el = new UapEntity();
				el.setData(ligne);
				el.setNumAutorisation(numAutorisation);
 				el.setNumTransaction(numRefTransaction);
				el.setRio(element.substring(0, 38));
				el.setReglement(element.substring(38, 46));
				duplicateElements.add(el);
			}

		}
		FileRequest.print(" " + duplicateElements.size(), FileRequest.getLineNumber());
		FileRequest.print(" " + fileUap.size(), FileRequest.getLineNumber());
		logger.info("length UAPFransaBankS =>{}", UAPFransaBankS.size());

		// trasform uap to hMap
		Map<String, UAP070IN> MapUap = new HashMap<>();
		for (UAP070IN u : UAPFransaBankS) {

			int lenNumAutorisationUap = u.getNumAutorisation().length();
			String numAutorisationUap = (u.getNumAutorisation().substring(lenNumAutorisationUap - 6,
					lenNumAutorisationUap)).trim();
			MapUap.put(numAutorisationUap + u.getNumRefTransaction(), u);
		}
		if (list.size() != (fileUap.size() + duplicateElements.size())) {
			FileRequest.print(" lists empty", FileRequest.getLineNumber());

			return -1;
		}
		logger.info("length fileUap =>{}", fileUap.size());

		logger.info("length Mapaps =>{}", MapUap.size());
		logger.info("length UAPFransaBankS =>{}", UAPFransaBankS.size());
		validAccept(MapUap, fileUap);

		uap070INRepository.saveAll(MapUap.values());


		/////////////////////////////////////


			moveFilesByStartingName(addFileRequest.getFilePath(), propertyService.getCompensationfilePathCro(), "070");
	return 1;
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
	public void validAccept(Map<String, UAP070IN> UAPFransaBankS, Map<String, String> list1) {
		for (Map.Entry<String, UAP070IN> entry : UAPFransaBankS.entrySet()) {
			UAP070IN u = entry.getValue();

			int lenNumAutorisationUap = u.getNumAutorisation().length();
			String numAutorisationUap = (u.getNumAutorisation().substring(lenNumAutorisationUap - 6,
					lenNumAutorisationUap)).trim();

			if (list1.containsKey(numAutorisationUap +  u.getNumRefTransaction())) {

				String e = list1.get(numAutorisationUap + u.getNumRefTransaction());
				String dateTransactionCro = e.substring(101, 101+8);
				String montantTransaction = e.substring(167, 167+15);
				String montantCompenser = e.substring(183, 183+15);

 				String MontantComission = e.substring(198, 198+7);
				u.setFileCompensationAmount(new BigDecimal(montantCompenser));
				u.setFileCommissionAmount(new BigDecimal(MontantComission));
				u.setFileTransactionAmount(new BigDecimal(montantTransaction));
				u.setUapRio(rioList.get(numAutorisationUap + u.getNumRefTransaction()));

				if ((u.getDateTransaction().trim()).equals(dateTransactionCro)
						&&  (u.getMontantCompensee()).equals(montantCompenser)
						&& (u.getMontantTransaction()).equals(montantTransaction)
						&& (u.getMontantCommission()).equals(MontantComission)){
						u.setAccepted("1");
					u.setDateReglement(reglementList.get(numAutorisationUap +  u.getNumRefTransaction()));

				} else {
					FileRequest.print(" set to null " + u.getNumAutorisation() + " " + u.getAccepted(),
							FileRequest.getLineNumber());
					u.setDateReglement(reglementList.get(numAutorisationUap +  u.getNumRefTransaction()));

					u.setAccepted("2");

				}
			} else {
				u.setAccepted("3");

			}
		}
	}


	public ExtractDataDto extractData(AddFileRequest addFileRequest) throws IOException {
		logger.info("extractData");
		ExtractDataDto extractedData = new ExtractDataDto();
		ArrayList<String> fileRows = new ArrayList<String>();
		List<String> filesNames = listAndFilterFiles(addFileRequest.getFilePath(), "070.DZD.CRO");
		FileRequest.print(filesNames.toString(), FileRequest.getLineNumber());
		FileRequest.print(addFileRequest.getFilePath(), FileRequest.getLineNumber());

		ReadFilesDto data = readFiles(filesNames, addFileRequest.getFilePath());

		fileRows = (ArrayList<String>) data.getFileContents();

		logger.info("extractData size = " + fileRows.size());
		extractedData.setLines(fileRows);
		extractedData.setMapContentByFile(data.getMapContentByFile());

		return extractedData;
	}


	public List<String> listAndFilterFiles(String folderPath, String filter) {
		List<String> filteredFiles = new ArrayList<>();
		
		File folder = new File(folderPath);
		
		if (folder.exists() && folder.isDirectory()) {
			File[] files = folder.listFiles();
			
			if (files != null) {
				for (File file : files) {
					if (file.isFile() && file.getName().contains(filter)) {
						filteredFiles.add(file.getName());
					}
				}
			}
		}
		
		return filteredFiles;
	}

	public ReadFilesDto readFiles(List<String> filteredFiles, String folderPath) {
		List<String> fileContents = new ArrayList<>();

		Map<String, List<String>> mapContentByFile = new HashMap<>();
		ReadFilesDto data = new ReadFilesDto();

		for (String fileName : filteredFiles) {
			String filePath = folderPath + File.separator + fileName;
			List<String> fileByFileContent = new ArrayList<>();

			try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
				StringBuilder content = new StringBuilder();
				String line;
				int i = 0;
				while ((line = reader.readLine()) != null) {
					if (i == 0) {
						i++;
					} else {
						fileContents.add(line);
						fileByFileContent.add(line);
						i++;
					}
				}
				mapContentByFile.put(fileName, fileByFileContent);

			} catch (IOException e) {
				// Handle the exception, e.g., log it or throw a custom exception
				e.printStackTrace();
			}
		}

		data.setFileContents(fileContents);
		data.setMapContentByFile(mapContentByFile);
		return data;
	}
}
