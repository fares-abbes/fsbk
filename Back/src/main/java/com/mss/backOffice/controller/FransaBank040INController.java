package com.mss.backOffice.controller;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
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
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.hibernate.engine.jdbc.batch.spi.Batch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Period;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

import com.mss.backOffice.services.FormulaInterpreterService;
import com.mss.backOffice.services.PropertyService;
import com.google.common.base.Throwables;
import com.google.gson.Gson;
import com.mss.backOffice.request.AddFileRequest;
import com.mss.backOffice.request.ExtractDataDto;
import com.mss.backOffice.request.ReadFilesDto;
import com.mss.backOffice.request.UapEntity;
import com.mss.backOffice.request.UapIn;
import com.mss.unified.dto.UapDetailsControl;
import com.mss.unified.entities.BatchesFC;

import com.mss.unified.entities.BkmvtiFransaBank;
import com.mss.unified.entities.CroControl;
import com.mss.unified.entities.DayOperationFransaBank;
import com.mss.unified.entities.FileHeaderT;
import com.mss.unified.entities.MotifRejet;
import com.mss.unified.entities.MvbkConf;
import com.mss.unified.entities.MvbkSettlement;
import com.mss.unified.entities.UAP040IN;
import com.mss.unified.entities.UAP050FransaBank;
import com.mss.unified.entities.UAP050IN;
import com.mss.unified.entities.UAPFransaBank;
import com.mss.unified.entities.User;
import com.mss.unified.entities.dayOperationReglement;
import com.mss.unified.entities.OpeningDay;
import com.mss.unified.repositories.BatchesFFCRepository;
import com.mss.unified.repositories.BkmvtiFransaBankRepository;
import com.mss.unified.repositories.CardRepository;
import com.mss.unified.repositories.CodeBankBCRepository;
import com.mss.unified.repositories.DayOperationFransaBankRepository;
import com.mss.unified.repositories.FileTRepository;
import com.mss.unified.repositories.MotifRejetRepository;
import com.mss.unified.repositories.MvbkConfigRepository;
import com.mss.unified.repositories.MvbkSettlementRepository;
import com.mss.unified.repositories.OpposedCardRepository;
import com.mss.unified.repositories.UAP040INFransaBankHistoryRepository;
import com.mss.unified.repositories.UAP040INFransaBankRepository;
import com.mss.unified.repositories.UserRepository;
import com.mss.unified.repositories.dayOperationReglementRepository;
import com.mss.unified.repositories.OpeningDayRepository;
import com.mss.unified.repositories.CroControlRepository;

@RestController
@RequestMapping("FransaBank040IN")
public strictfp class FransaBank040INController {
	@Autowired
	FormulaInterpreterService fIPService;
	@Autowired
	OpeningDayRepository openedDayRepo;
	@Autowired
	dayOperationReglementRepository dayReglementRepo;

	private static final Logger logger = LoggerFactory.getLogger(FransaBank040INController.class);
	@Autowired
	BkmvtiFransaBankRepository bkmRepo;
	@Autowired
	UAP040INFransaBankRepository uAP040INFransaBankRepository;
	@Autowired
	DayOperationFransaBankRepository dayOperationFransaBankRepository;
	@Autowired
	MvbkSettlementRepository mvbkSettlementRepository;
	@Autowired
	BkmvtiFransaBankRepository bkmvtiFransaBankRepository;
	@Autowired
	MotifRejetRepository mtvRejetRepository;
	@Autowired
	public MvbkConfigRepository mvbkConfigR;
	@Autowired
	CardRepository cardRepository;
	@Autowired
	BatchesFFCRepository batchesFFCRepository;
	@Autowired
	FileTRepository fileSummaryTRepository;
	@Autowired
	CodeBankBCRepository codeBankBCRepository;
	@Autowired
	BatchesFFCRepository batchRepo;
	@Autowired
	UAP040INFransaBankHistoryRepository uap040INHistory;

	@Autowired
	UserRepository userRepository;
	@Autowired
	PropertyService propertyService;
	@Autowired
	DayOperationFransaBankRepository dayRepo;
	@Autowired
	OpposedCardRepository opCardBin;
	@Autowired
	private CroControlRepository croControlRepository;
	private static HashMap<String, String> rioList;
	private static List<UapEntity> duplicateElements;
	private static HashMap<String, String> reglementList;

	public synchronized int getEveIndex() {
		return (++ExecutorThreadUAPINFileBC.eve);
	}

	public synchronized int getEveIndex1() {
		return (++ExecutorThreadUAPINFileBC.eve1);
	}

	public static String getLineNumber() {
		return Thread.currentThread().getStackTrace()[2].getClassName() + ": "
				+ String.valueOf(Thread.currentThread().getStackTrace()[2].getLineNumber());
	}

	@PutMapping("matchingUAP040IN")
	public String matchingUap40In(@RequestBody AddFileRequest addFileRequest) throws IOException {
		FileRequest.print("matchingUAP040IN", FileRequest.getLineNumber());
		BatchesFC batch = batchesFFCRepository.findByKey("CRAUAP40IN").get();
		batch.setBatchLastExcution(batch.getBatchEndDate());
		batch.setBatchStatus(0);
		batch.setBatchDate(new Date());
		batch.setError(null);
		batch.setErrorStackTrace(null);
		batchRepo.saveAndFlush(batch);
		SecurityContextHolder.getContext().setAuthentication(OrshesterController.auth);

		try {
			int etat = validTransaction(addFileRequest);

			if (etat == 1) {

				List<UAP040IN> ListUAPFransaBanksFiltred = uAP040INFransaBankRepository.getListUAPINByStatusAccepted();
				FileRequest.print(ListUAPFransaBanksFiltred.toString() + "", FileRequest.getLineNumber());

				for (UAP040IN uap : ListUAPFransaBanksFiltred) {
					if (uap.getTypeTransaction().equals("040") && uap.getTypeRetrait().equals("001")) {
						if (uap.getTagRetrait().equals("1")) {
							uap40InRetraitWithoutDayOperation(uap);
							uap.setBkmGeneration("done");
						} else if (uap.getTagRetrait().equals("2")) {
							uap40InAlgeriePosteWithoutDayOperation(uap);
							uap.setBkmGeneration("done");
						}

					} else if (uap.getTypeTransaction().equals("040") && uap.getTypeRetrait().equals("002")) {
						// somme cro row have the corrrect commission but the wrong amount to compensate
						// for

//						uap.setMontantCommissionTTC(Integer.valueOf(uap.getMontantAComponser()));
						uap40InConsultationWithoutDayOperation(uap);

						uap.setBkmGeneration("done");
					}
				}
				uAP040INFransaBankRepository.saveAll(ListUAPFransaBanksFiltred);
				Integer count = uAP040INFransaBankRepository.sizeNotMatched() == null ? 0
						: uAP040INFransaBankRepository.sizeNotMatched();
				if (count == 0) {
					batchRepo.updateFinishBatch("CRAUAP40IN", 1, new Date());
					openedDayRepo.updateStatus040(addFileRequest.getFileDate(), "doneCro");
					rioList.clear();
					reglementList.clear();
					logger.info("end matchingUap40In");
				} else {
					Integer countAut = uAP040INFransaBankRepository.sizeNotMatchedAndEmptyFlag() == null ? 0
							: uAP040INFransaBankRepository.sizeNotMatchedAndEmptyFlag();
					FileRequest.print("countAut " + countAut + "count" + count, getLineNumber());
					if (countAut < count) {
						batchRepo.updateFinishBatch("CRAUAP40IN", 1, new Date());

					} else {
						openedDayRepo.updateStatus040(addFileRequest.getFileDate(), "doneSort");
						batchRepo.updateFinishBatch("CRAUAP40IN", 5, new Date());
						batchRepo.updateFinishBatch("SENDLOT", -1, new Date());
					}
					FileRequest.print("addFileRequest.getFileDate() " + addFileRequest.getFileDate(), getLineNumber());

					Optional<OpeningDay> d = openedDayRepo.findByStatus040("doneSort");
					if (d.isPresent()) {
						OpeningDay day = d.get();
						FileRequest.print("day " + day, getLineNumber());

						day.setLotIncrementNb(1);
						openedDayRepo.saveAndFlush(day);
					}
					rioList.clear();
					reglementList.clear();

				}

			} else if (etat == -1) {
				batch.setBatchStatus(2);
				batch.setBatchEndDate(new Date());
				batch.setError("rows number is different");
				batch.setErrorStackTrace("rows number is different");
				batchRepo.saveAndFlush(batch);
			} else {
				batch.setBatchStatus(4);
				batch.setBatchEndDate(new Date());
				batch.setError("No file Present");
				batch.setErrorStackTrace("No file Present");
				batchRepo.saveAndFlush(batch);
			}

		} catch (Exception e) {
			String stackTrace = Throwables.getStackTraceAsString(e);
			logger.info("Exception is=>{}", stackTrace);
			if (stackTrace.length() > 4000)
				stackTrace = stackTrace.substring(0, 3999);
			String error = e.getMessage() == null ? e.toString() : e.getMessage();
			if (error.length() > 255)
				error = error.substring(0, 254);

			batchRepo.updateStatusAndErrorBatch("CRAUAP40IN", 2, error, new Date(), stackTrace);
			return "error";
		}
		FileRequest.print("matching done 040", FileRequest.getLineNumber());

		return "done";
	}

	public int validTransaction(AddFileRequest addFileRequest) throws IOException {
		logger.info("validTransaction");

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
			String numAutorisation = ligne.substring(134, 140);
			String PanCRo = ligne.substring(32, 48);
			String numTransaction = ligne.substring(86, 86 + 12);

//					fileUap.put(numAutorisation + PanCRo, ligne);
//					rioList.put(numAutorisation + PanCRo, element.substring(0, 38));
//					reglementList.put(numAutorisation + PanCRo, element.substring(38, 46));
			if (!fileUap.containsKey(numAutorisation + PanCRo + numTransaction) && uAP040INFransaBankRepository.findinHistorys(numAutorisation, PanCRo, numTransaction).isEmpty()
					&& uap040INHistory.findinHistorys(numAutorisation, PanCRo, numTransaction).isEmpty()) {
				fileUap.put(numAutorisation + PanCRo + numTransaction, ligne);
				rioList.put(numAutorisation + PanCRo + numTransaction, element.substring(0, 38));
				reglementList.put(numAutorisation + PanCRo + numTransaction, element.substring(38, 46));

			} else {

				FileRequest.print("" + fileUap.size(), FileRequest.getLineNumber());
				FileRequest.print("" + numAutorisation, FileRequest.getLineNumber());
				FileRequest.print("" + PanCRo, FileRequest.getLineNumber());
				UapEntity el = new UapEntity();
				el.setData(ligne);
				el.setNumAutorisation(numAutorisation);
				el.setPanCro(PanCRo);
				el.setNumTransaction(numTransaction);
				el.setRio(element.substring(0, 38));
				el.setReglement(element.substring(38, 46));
				duplicateElements.add(el);

			}
		}
		if (list.size() != (fileUap.size() + duplicateElements.size())) {

			return -1;
		}
		FileRequest.print(" " + duplicateElements.size(), FileRequest.getLineNumber());
		FileRequest.print(" " + fileUap.size(), FileRequest.getLineNumber());
		List<UAP040IN> UAPFransaBankS = uAP040INFransaBankRepository.getListUAPIN();
		logger.info("length UAPFransaBankS =>{}", UAPFransaBankS.size());

		Map<String, UAP040IN> MapUap = new HashMap<>();

		for (UAP040IN u : UAPFransaBankS) {
			int lenNumAutorisationUap = u.getNumAutorisation().length();
			String numAutorisationUap = u.getNumAutorisation()
					.substring(lenNumAutorisationUap - 6, lenNumAutorisationUap).trim();
			String PAN = u.getCodeBin().trim() + u.getTypeCarteDebit().trim() + u.getNumSeq().trim()
					+ u.getNumOrdre().trim() + u.getCle().trim();
			MapUap.put(numAutorisationUap + PAN + u.getNumTransaction(), u);
		}
		logger.info("length fileUap =>{}", fileUap.size());

		logger.info("length Mapaps =>{}", MapUap.size());
		logger.info("keys fileUap =>{}", fileUap.keySet().toString());

		logger.info("keys Mapaps =>{}", MapUap.keySet().toString());
		logger.info("length UAPFransaBankS =>{}", UAPFransaBankS.size());
		logger.info("list UAPFransaBankS =>{}", UAPFransaBankS.toString());
		logger.info("duplicate items list  UAPFransaBankS =>{}", duplicateElements.toString());
		validAccept(MapUap, fileUap);

		ValidExtraNotUapInFile(MapUap, fileUap, addFileRequest.getFileDate());
		if (duplicateElements != null && !duplicateElements.isEmpty()) {
			ExtraNotUapInFileDuplicate(MapUap, duplicateElements, addFileRequest.getFileDate());
		}
		validNotOk(MapUap, fileUap, addFileRequest.getFileDate());
		uAP040INFransaBankRepository.saveAll(MapUap.values());

		/////////// CRO control/////////////

		saveCroControl(data.getMapContentByFile());

		/////////////////////////////////////

		if (MapUap.values().size() > 0) {
			Stream<UAP040IN> stream = MapUap.values().stream();
			List<UAP040IN> validlist = stream
					.filter(element -> element.getAccepted().equals("1") || element.getAccepted().equals("NOT OK"))
					.collect(Collectors.toList());
			moveFilesByStartingName(addFileRequest.getFilePath(), propertyService.getCompensationfilePathCro(), "040");
			return validlist.size() > 0 ? 1 : 0;
		} else {
			return 0;
		}
	}

	private void saveCroControl(Map<String, List<String>> mapContentByFile) {

		for (String fileName : mapContentByFile.keySet()) {
			logger.info("fileName => {}", fileName);

			List<String> fileContent = mapContentByFile.get(fileName);

			CroControl croControl = new CroControl();
			croControl.setFileName(fileName);
			croControl.setProcessingDate(new Date());
			croControl.setTypeCro("040");
			long sumFromFile = 0;
			int nbFromFile = 0;
			String dateReg = "";

			for (String element : fileContent) {
				String ligne = element.substring(46, element.length());
				String montantCompenser = ligne.substring(48, 63);

				sumFromFile = sumFromFile + Long.parseLong(montantCompenser);
				nbFromFile++;
				if (dateReg.equals(""))
					dateReg = element.substring(38, 46);

			}

			logger.info("Date reglement => {}", dateReg);

			croControl.setSumFromFile(sumFromFile);
			logger.info("SumFromFile => {}", sumFromFile);

			croControl.setNbTotalFromFile(nbFromFile);
			logger.info("nbFromFile => {}", nbFromFile);

			if (!dateReg.equals("")) {
				croControl.setDateReg(dateReg);
				List<UapDetailsControl> acceptedUpa40In = uAP040INFransaBankRepository.getAcceptedUap40In(dateReg);
				List<UapDetailsControl> rejectedUpa40In = uAP040INFransaBankRepository.getRejectedUap40In(dateReg);
				List<UapDetailsControl> extraUpa40In = uAP040INFransaBankRepository.getExtraUap40In(dateReg);

				long sumAcceptedUpa40In = 0;
				long sumRejectedUpa40In = 0;
				long sumExtraUpa40In = 0;

				for (UapDetailsControl el : acceptedUpa40In) {
					sumAcceptedUpa40In += Long.valueOf(el.getMontantAComponser().replace(".", ""));
				}

				for (UapDetailsControl el : rejectedUpa40In) {
					sumRejectedUpa40In += Long.valueOf(el.getMontantAComponser().replace(".", ""));
				}

				for (UapDetailsControl el : extraUpa40In) {
					sumExtraUpa40In += Long.valueOf(el.getMontantAComponser().replace(".", ""));
				}

				croControl.setSumValidated(sumAcceptedUpa40In);
				croControl.setNbTotalValidated(acceptedUpa40In.size());
				logger.info("SumValidated => {}", sumAcceptedUpa40In);
				logger.info("NbTotalValidated => {}", acceptedUpa40In.size());

				croControl.setSumRejected(sumRejectedUpa40In);
				croControl.setNbTotalRejected(rejectedUpa40In.size());
				logger.info("SumRejected => {}", sumRejectedUpa40In);
				logger.info("NbTotalRejected => {}", rejectedUpa40In.size());

				croControl.setSumExtra(sumExtraUpa40In);
				croControl.setNbTotalExtra(extraUpa40In.size());
				logger.info("SumExtra => {}", sumExtraUpa40In);
				logger.info("NbTotalExtra => {}", extraUpa40In.size());

			}

			croControlRepository.save(croControl);

		}

	}

	public ExtractDataDto extractData(AddFileRequest addFileRequest) throws IOException {
		logger.info("extractData");
		ExtractDataDto extractedData = new ExtractDataDto();
		ArrayList<String> fileRows = new ArrayList<String>();
		List<String> filesNames = listAndFilterFiles(addFileRequest.getFilePath(), "040.DZD.CRO");
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

		// return fileContents;
		return data;
	}

	public long calculeNumberOfDate(String date) {

		// Define a DateTimeFormatter for parsing the input string
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");

		// Parse the input string to LocalDate
		LocalDate inputDate = LocalDate.parse(date, formatter);

		// Get the current system date as LocalDate
		LocalDate currentDate = LocalDate.now();

		// Calculate the difference in days
		return ChronoUnit.DAYS.between(inputDate, currentDate);

	}

	public void validAccept(Map<String, UAP040IN> UAPFransaBankS, Map<String, String> list1) {

		for (Map.Entry<String, UAP040IN> entry : UAPFransaBankS.entrySet()) {
			UAP040IN u = entry.getValue();
			int lenNumAutorisationUap = u.getNumAutorisation().length();
			String numAutorisationUap = (u.getNumAutorisation().substring(lenNumAutorisationUap - 6,
					lenNumAutorisationUap)).trim();
			FileRequest.print(numAutorisationUap, FileRequest.getLineNumber());
			String PAN = u.getCodeBin().trim() + u.getTypeCarteDebit().trim() + u.getNumSeq().trim()
					+ u.getNumOrdre().trim() + u.getCle().trim();
			String numTransaction = u.getNumTransaction();
			if (list1.containsKey(entry.getKey())) {
				FileRequest.print("accepted = " + numAutorisationUap, FileRequest.getLineNumber());
				FileRequest.print(" avant " + u.getNumAutorisation() + " " + u.getAccepted(),
						FileRequest.getLineNumber());
				String e = list1.get(numAutorisationUap + PAN + u.getNumTransaction());
				String dateTransaction = e.substring(98, 106);
				String montantRetrait = e.substring(63, 78);
				String montantComission = e.substring(79, 86);
				String montantCompenser = e.substring(48, 63);

				u.setRio(rioList.get(numAutorisationUap + PAN + u.getNumTransaction()));
				u.setFileMontantAcompenser(montantCompenser);
				u.setFileMontantcommission(montantComission);
				if (u.getDateTransaction().trim().equals(dateTransaction)
						&& (u.getMontantRetrait().trim()).equals(montantRetrait)
						&& (u.getMontantAComponser().trim()).equals(montantCompenser)
						&& (u.getMontantCommission().trim()).equals(montantComission)) {

					if (opCardBin.findByCardNumber(PAN).isPresent()) {
						u.setAccepted("NOT OK");
						u.setFlag("20");
						u.setMotifRejet("008");
					} else if (calculeNumberOfDate(u.getDateTransaction()) > mtvRejetRepository.findByCode("405").get()
							.getDelais()) {
						u.setAccepted("NOT OK");
						u.setFlag("20");
						u.setMotifRejet("405");
					} else {

						u.setAccepted("1");
					}
					u.setDateReg(reglementList.get(numAutorisationUap + PAN + u.getNumTransaction()));
					FileRequest.print(" 1 " + u.getNumAutorisation() + " " + u.getAccepted(),
							FileRequest.getLineNumber());

				} else {
					FileRequest.print(" set to null " + u.getNumAutorisation() + " " + u.getAccepted(),
							FileRequest.getLineNumber());
					u.setAccepted(null);

				}
				FileRequest.print(" 1 " + u.getNumAutorisation() + " " + u.getAccepted(), FileRequest.getLineNumber());

			} else {
				FileRequest.print("sattus 3 = " + numAutorisationUap, FileRequest.getLineNumber());

				u.setAccepted("3");
			}

		}
	}

	public void ValidExtraNotUapInFile(Map<String, UAP040IN> UAPFransaBankS, Map<String, String> list1,
			String fileDate) {
		for (Map.Entry m : list1.entrySet()) {
			logger.info("new=>{}", m.getKey());

			if (!UAPFransaBankS.containsKey(m.getKey())) {
				String ligne = (String) m.getValue();
				logger.info("new");
				UAP040IN uap40in = new UAP040IN();
				uap40in.setTypeTransaction(ligne.substring(0, 3));
				uap40in.setReferenceArchivage(ligne.substring(3, 21));
				uap40in.setCodeBankAcquereur(ligne.substring(21, 24));
				uap40in.setTypeRetrait(ligne.substring(29, 32));
				uap40in.setCodeBin(ligne.substring(32, 38));
				uap40in.setTypeCarteDebit(ligne.substring(38, 40));
				uap40in.setNumSeq(ligne.substring(40, 46));
				uap40in.setNumOrdre(ligne.substring(46, 47));
				uap40in.setCle(ligne.substring(47, 48));
				uap40in.setMontantAComponser(ligne.substring(48, 63));
				uap40in.setMontantRetrait(ligne.substring(63, 78));
				uap40in.setFileMontantAcompenser(ligne.substring(48, 63));
				uap40in.setFileMontantcommission(ligne.substring(79, 86));
				uap40in.setCodeDebitCommercant(ligne.substring(78, 79));
				uap40in.setMontantCommission(ligne.substring(79, 86));
				uap40in.setMontantTransaction(Integer.valueOf(uap40in.getMontantRetrait()));
				uap40in.setMontantCommissionTTC(Integer.valueOf(uap40in.getMontantCommission()));
				uap40in.setNumTransaction(ligne.substring(86, 98));
				uap40in.setDateTransaction(ligne.substring(98, 106));
				uap40in.setHeureTransaction(ligne.substring(106, 112));
				uap40in.setIdentifSystem(ligne.substring(112, 122));
				uap40in.setIdentifPointRetrait(ligne.substring(122, 132));
				uap40in.setModeLectureCarte(ligne.substring(132, 133));
				uap40in.setMethAuthPorteur(ligne.substring(133, 134));
				uap40in.setNumAutorisation(getZero(9) + ligne.substring(134, 140));
				uap40in.setDateDebutValiditeCarte(ligne.substring(140, 148));
				uap40in.setDateFinValiditeCarte(ligne.substring(148, 156));
				uap40in.setCryptogramData(ligne.substring(156, 157));

				uap40in.setAtc(ligne.substring(157, 159));
				uap40in.setTvr(ligne.substring(159, 164));
				uap40in.setFileIntegrationDate(fileDate);
				try {
					String PAN = uap40in.getCodeBin().trim() + uap40in.getTypeCarteDebit().trim()
							+ uap40in.getNumSeq().trim() + uap40in.getNumOrdre().trim() + uap40in.getCle().trim();
					uap40in.setCodeAgence(cardRepository.findByCardNum(PAN).get().getAgencyCode());
				} catch (Exception e) {

					uap40in.setCodeAgence(ligne.substring(24, 29));
					FileRequest.print(e.getStackTrace().toString(), FileRequest.getLineNumber());
				}
				if ("007".equals(uap40in.getCodeBankAcquereur())) {
					uap40in.setTagRetrait("2");
				} else {
					uap40in.setTagRetrait("1");

				}
				uap40in.setRio(rioList.get(m.getKey()));
				uap40in.setDateReg(reglementList.get(m.getKey()));
				if (opCardBin
						.findByCardNumber(uap40in.getCodeBin().trim() + uap40in.getTypeCarteDebit().trim()
								+ uap40in.getNumSeq().trim() + uap40in.getNumOrdre().trim() + uap40in.getCle().trim())
						.isPresent()) {
					uap40in.setAccepted("NOT OK");
					uap40in.setFlag("20");
					uap40in.setMotifRejet("008");
				} else if (calculeNumberOfDate(uap40in.getDateTransaction()) > mtvRejetRepository.findByCode("405")
						.get().getDelais()) {
					uap40in.setAccepted("2");
					uap40in.setFlag("20");
					uap40in.setMotifRejet("405");
				} else if (uap040INHistory.findByRio(uap40in.getRio()).isEmpty()
						&& uAP040INFransaBankRepository.findByRio(uap40in.getRio()).isEmpty()) {
					uap40in.setAccepted("2");
				} else {
					uap40in.setAccepted("2");
					uap40in.setFlag("20");
					uap40in.setMotifRejet("001");
				}
				if ("001".equals(uap40in.getTypeRetrait())) {
					uap40in.setPieceComptableBKM(
							"DB" + "162" + uap40in.getRio().substring(uap40in.getRio().length() - 6));
				} else {
					uap40in.setPieceComptableBKM(
							"DB" + "106" + uap40in.getRio().substring(uap40in.getRio().length() - 6));

				}
				uap40in.setMontantCommissionTTC(Math.abs(Integer.valueOf(uap40in.getFileMontantAcompenser())
						- Integer.valueOf(uap40in.getMontantTransaction())));
				UAPFransaBankS.put(m.getKey().toString(), uap40in);

			}
		}

	}

	public void ExtraNotUapInFileDuplicate(Map<String, UAP040IN> UAPFransaBankS, List<UapEntity> elements,
			String fileDate) {
		int index = 0;
		for (UapEntity m : elements) {
			index++;
			String ligne = (String) m.getData();
			logger.info("new pan" + m.getPanCro() + " num autorisation" + m.getNumAutorisation());
			UAP040IN uap40in = new UAP040IN();

			uap40in.setTypeTransaction(ligne.substring(0, 3));
			uap40in.setReferenceArchivage(ligne.substring(3, 21));
			uap40in.setCodeBankAcquereur(ligne.substring(21, 24));
//			uap40in.setCodeAgence(ligne.substring(24, 29));
			uap40in.setTypeRetrait(ligne.substring(29, 32));
			uap40in.setCodeBin(ligne.substring(32, 38));
			uap40in.setTypeCarteDebit(ligne.substring(38, 40));
			uap40in.setNumSeq(ligne.substring(40, 46));
			uap40in.setNumOrdre(ligne.substring(46, 47));
			uap40in.setCle(ligne.substring(47, 48));
			uap40in.setMontantAComponser(ligne.substring(48, 63));
			uap40in.setMontantRetrait(ligne.substring(63, 78));
			uap40in.setCodeDebitCommercant(ligne.substring(78, 79));
			uap40in.setMontantCommission(ligne.substring(79, 86));
			uap40in.setMontantTransaction(Integer.valueOf(uap40in.getMontantRetrait()));
			uap40in.setMontantCommissionTTC(Integer.valueOf(uap40in.getMontantCommission()));

			uap40in.setNumTransaction(ligne.substring(86, 98));
			uap40in.setDateTransaction(ligne.substring(98, 106));
			uap40in.setHeureTransaction(ligne.substring(106, 112));
			uap40in.setIdentifSystem(ligne.substring(112, 122));
			uap40in.setIdentifPointRetrait(ligne.substring(122, 132));
			uap40in.setModeLectureCarte(ligne.substring(132, 133));
			uap40in.setMethAuthPorteur(ligne.substring(133, 134));
			uap40in.setNumAutorisation(getZero(9) + ligne.substring(134, 140));
			uap40in.setDateDebutValiditeCarte(ligne.substring(140, 148));
			uap40in.setDateFinValiditeCarte(ligne.substring(148, 156));
			uap40in.setCryptogramData(ligne.substring(156, 157));
			uap40in.setAtc(ligne.substring(157, 159));
			uap40in.setTvr(ligne.substring(159, 164));
			uap40in.setFileIntegrationDate(fileDate);
			uap40in.setFileMontantAcompenser(uap40in.getMontantAComponser());
			uap40in.setFileMontantcommission(uap40in.getMontantCommission());
			uap40in.setRio(m.getRio());

			try {
				String PAN = uap40in.getCodeBin().trim() + uap40in.getTypeCarteDebit().trim()
						+ uap40in.getNumSeq().trim() + uap40in.getNumOrdre().trim() + uap40in.getCle().trim();
				uap40in.setCodeAgence(cardRepository.findByCardNum(PAN).get().getAgencyCode());
			} catch (Exception e) {

				uap40in.setCodeAgence("99999");
				FileRequest.print(e.getStackTrace().toString(), FileRequest.getLineNumber());
			}
			if ("007".equals(uap40in.getCodeBankAcquereur())) {
				uap40in.setTagRetrait("2");
			} else {
				uap40in.setTagRetrait("1");

			}
			if ("001".equals(uap40in.getTypeRetrait())) {
				uap40in.setPieceComptableBKM("DB" + "162"
						+ uap40in.getRio().substring(uap40in.getRio().length() - 6));
			} else {
				uap40in.setPieceComptableBKM("DB" + "106"
						+uap40in.getRio().substring(uap40in.getRio().length() - 6));

			}

			uap40in.setAccepted("2");
			uap40in.setDateReg(m.getReglement());
			uap40in.setMotifRejet("001");
			uap40in.setFlag("20");
			uap40in.setMontantCommissionTTC(Math.abs(Integer.valueOf(uap40in.getFileMontantAcompenser())
					- Integer.valueOf(uap40in.getMontantTransaction())));

			UAPFransaBankS.put(m.getNumAutorisation() + m.getPanCro() + index, uap40in);

		}

	}

	public void validNotOk(Map<String, UAP040IN> UAPFransaBankS, Map<String, String> uapFile, String fileDate) {
		for (UAP040IN element : UAPFransaBankS.values()) {
			if (element.getAccepted() == null) {
				String PAN = element.getCodeBin().trim() + element.getTypeCarteDebit().trim()
						+ element.getNumSeq().trim() + element.getNumOrdre().trim() + element.getCle().trim();
				int lenNumAutorisationUap = element.getNumAutorisation().length();
				String numAutorisationUap = (element.getNumAutorisation().substring(lenNumAutorisationUap - 6,
						lenNumAutorisationUap)).trim();
				String file = uapFile.get(numAutorisationUap + PAN + element.getNumTransaction());
				FileRequest.print(file, FileRequest.getLineNumber());
				String montantCompenser = file.substring(48, 63);
				String montantCommission = file.substring(79, 86);
				element.setRio(rioList.get(numAutorisationUap + PAN + element.getNumTransaction()));

				element.setFileMontantAcompenser(montantCompenser);
				element.setFileMontantcommission(montantCommission);
				element.setMontantRetrait(file.substring(63, 78));
				element.setMontantTransaction(Integer.valueOf(element.getMontantRetrait()));
				element.setMontantCommissionTTC(Integer.valueOf(element.getMontantCommission()));
				element.setDateReg(reglementList.get(numAutorisationUap + PAN + element.getNumTransaction()));

				element.setAccepted("NOT OK");
				element.setDatabaseMntACompenser(element.getMontantAComponser());
				element.setDatabaseMntCommission(element.getMontantCommissionTTC() + "");
				element.setMontantAComponser(montantCompenser);
				element.setMontantCommissionTTC(Integer.valueOf(montantCommission));
				element.setFlag("20");
				element.setMotifRejet("002");
				element.setMontantCommissionTTC(Math.abs(Integer.valueOf(element.getFileMontantAcompenser())
						- Integer.valueOf(element.getMontantTransaction())));

			}
		}
	}

	public void uap40InRetraitWithoutDayOperation(UAP040IN uap) {
		List<MvbkSettlement> allMvbkSettelemnts = mvbkSettlementRepository.findByIdentificationOffUsAcqRetraitRCROIN();
		List<MvbkConf> allMvbkSettelemntsC = mvbkConfigR.findByCategorie(37);
		List<BkmvtiFransaBank> BkmvtiFransaBanks = new ArrayList<BkmvtiFransaBank>();
		int indexPieceComptable = getEveIndex1();
		for (MvbkConf mvk : allMvbkSettelemntsC) {
			int index2 = getEveIndex();
			BkmvtiFransaBanks = TestSigneWithoutDayOperation(uap, 2, mvk, BkmvtiFransaBanks, indexPieceComptable,
					index2, mvk.getCodeSettlement());
		}
		FileRequest.print(BkmvtiFransaBanks.size()+"",FileRequest.getLineNumber());
 		if (BkmvtiFransaBanks != null && !BkmvtiFransaBanks.isEmpty()) {
		    // Filter out null elements from the list
		    List<BkmvtiFransaBank> nonNullBkmvtiFransaBanks = BkmvtiFransaBanks.stream()
		            .filter(Objects::nonNull)
		            .collect(Collectors.toList());

		    if (!nonNullBkmvtiFransaBanks.isEmpty()) {
		        bkmvtiFransaBankRepository.saveAll(nonNullBkmvtiFransaBanks);
		    }
		    
 		}	}

	public void uap40InAlgeriePosteWithoutDayOperation(UAP040IN uap) {
		List<MvbkSettlement> allMvbkSettelemntsALP = mvbkSettlementRepository
				.findByIdentificationOffUsAcqRetraitALPRCROIN();
		List<MvbkConf> allMvbkSettelemntsC = mvbkConfigR.findByCategorie(51);

		List<BkmvtiFransaBank> BkmvtiFransaBanks = new ArrayList<BkmvtiFransaBank>();
		int indexPieceComptable = getEveIndex1();
		for (MvbkConf mvk : allMvbkSettelemntsC) {
			int index2 = getEveIndex();
			BkmvtiFransaBanks = TestSigneWithoutDayOperation(uap, 16, mvk, BkmvtiFransaBanks, indexPieceComptable,
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
		    
		}
	}

	public void uap40InConsultationWithoutDayOperation(UAP040IN uap) {
		List<MvbkSettlement> allMvbkSettelemnts = mvbkSettlementRepository
				.findByIdentificationOffUsAcqConsultationSoldeRCROIN();
		List<MvbkConf> allMvbkSettelemntsC = mvbkConfigR.findByCategorie(41);

		List<BkmvtiFransaBank> BkmvtiFransaBanks = new ArrayList<BkmvtiFransaBank>();
		int indexPieceComptable = getEveIndex1();

		for (MvbkConf mvk : allMvbkSettelemntsC) {
			int index2 = getEveIndex();
			BkmvtiFransaBanks = TestSigneWithoutDayOperation(uap, 10, mvk, BkmvtiFransaBanks, indexPieceComptable,
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
		    
 		}
	}

	@PutMapping("generateReglement")
	public void generateNextDay() {
		BatchesFC batch = batchesFFCRepository.findByKey("GNR01").get();
		batch.setBatchLastExcution(batch.getBatchEndDate());
		batch.setBatchStatus(0);
		batch.setBatchDate(new Date());
		batch.setError(null);
		batch.setErrorStackTrace(null);
		batchRepo.save(batch);
		try {
			SimpleDateFormat newFormat = new SimpleDateFormat("yyyyMMdd");
			String today = newFormat.format(new Date());

			List<UAP040IN> ListUAPFransaBanksFiltred = uAP040INFransaBankRepository
					.getListUAPINByStatusAcceptedAndDateNew(today);
			FileRequest.print(ListUAPFransaBanksFiltred.toString(), FileRequest.getLineNumber());
			for (UAP040IN uap : ListUAPFransaBanksFiltred) {
				try {
					if (uap.getTypeTransaction().equals("040") && uap.getTypeRetrait().equals("001")) {

						if (uap.getTagRetrait().equals("1")) {
							uap40InRetraitRegWithoutDayOperation(uap);
						} else if (uap.getTagRetrait().equals("2")) {
							uap40InAlgeriePosteRegWithoutDayOperation(uap);
						}
					} else if (uap.getTypeTransaction().equals("040") && uap.getTypeRetrait().equals("002")) {

						uap40InConsultationRegWithoutDayOperation(uap);

					}
				} catch (Exception e) {
					logger.error("uap 040 IN is=>{}", uap);
					logger.error("Exception is=>{}", Throwables.getStackTraceAsString(e));

				}

			}

			batchesFFCRepository.updateFinishBatch("GNR01", 1, new Date());

		} catch (Exception e) {
			String stackTrace = Throwables.getStackTraceAsString(e);
			logger.info("Exception is=>{}", stackTrace);
			if (stackTrace.length() > 4000)
				stackTrace = stackTrace.substring(0, 3999);
			String error = e.getMessage() == null ? e.toString() : e.getMessage();
			if (error.length() > 255)
				error = error.substring(0, 254);

			batchRepo.updateStatusAndErrorBatch("GNR01", 2, error, new Date(), stackTrace);

		}

	}

	public void uap40InRetraitRegWithoutDayOperation(UAP040IN uap) {
		List<MvbkSettlement> allMvbkSettelemnts = mvbkSettlementRepository
				.findByIdentificationOffUsAcqRetraitReglementIN();

		List<MvbkConf> allMvbkSettelemntsC = mvbkConfigR.findByCategorie(42);

		List<BkmvtiFransaBank> BkmvtiFransaBanks = new ArrayList<BkmvtiFransaBank>();
		int indexPieceComptable = getEveIndex1();

		for (MvbkConf mvk : allMvbkSettelemntsC) {

			int index2 = getEveIndex();

			BkmvtiFransaBanks = TestSigneWithoutDayOperation(uap, 2, mvk, BkmvtiFransaBanks, indexPieceComptable,
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
		    
 		}	}

	public void uap40InAlgeriePosteRegWithoutDayOperation(UAP040IN uap) {
		List<MvbkSettlement> allMvbkSettelemntsALP = mvbkSettlementRepository
				.findByIdentificationOffUsRetraitAlPInReglement();
		List<MvbkConf> allMvbkSettelemntsC = mvbkConfigR.findByCategorie(75);

		List<BkmvtiFransaBank> BkmvtiFransaBanks = new ArrayList<BkmvtiFransaBank>();

		int indexPieceComptable = getEveIndex1();

		for (MvbkConf mvk : allMvbkSettelemntsC) {
			int index2 = getEveIndex();
			BkmvtiFransaBanks = TestSigneWithoutDayOperation(uap, 16, mvk, BkmvtiFransaBanks, indexPieceComptable,
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
		    
 		}
	}

	public void uap40InConsultationRegWithoutDayOperation(UAP040IN uap) {

		List<MvbkSettlement> allMvbkSettelemnts = mvbkSettlementRepository
				.findByIdentificationOffUsAcqCSoldeReglementIN();
		List<MvbkConf> allMvbkSettelemntsC = mvbkConfigR.findByCategorie(43);

		List<BkmvtiFransaBank> BkmvtiFransaBanks = new ArrayList<BkmvtiFransaBank>();

		int indexPieceComptable = getEveIndex1();

		for (MvbkConf mvk : allMvbkSettelemntsC) {
			int index2 = getEveIndex();
			BkmvtiFransaBanks = TestSigneWithoutDayOperation(uap, 10, mvk, BkmvtiFransaBanks, indexPieceComptable,
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
		    
 		}	}

	public List<BkmvtiFransaBank> TestSigneWithoutDayOperation(UAP040IN uap, int methode, MvbkConf mvk,
			List<BkmvtiFransaBank> BkmvtiFransaBanks, int indexPieceComptable, int index2, String codeOperation) {

		BkmvtiFransaBank bkmvtiFransaBank = new BkmvtiFransaBank();

		if ("BANK_ACQ".equals(mvk.getEntity())) {
			String codeBank = uap.getCodeBankAcquereur();
			String mvbaccount = mvk.getAccount().substring(0, mvk.getAccount().length() - 3) + codeBank;
			bkmvtiFransaBank = TestAccountAndSigneWithoutSayOperation(uap, methode, index2, indexPieceComptable, 1,
					mvbaccount, mvk.getSigne(), mvk, mvk.getAccount());
		} else {

			bkmvtiFransaBank = TestAccountAndSigneWithoutSayOperation(uap, methode, index2, indexPieceComptable, 1,
					mvk.getAccount(), mvk.getSigne(), mvk, mvk.getAccount());
		}
		
		FileRequest.print(uap.getReferenceArchivage()+"  "+uap.toString(), codeOperation);
		FileRequest.print(bkmvtiFransaBank.getIdentification()+"  "+bkmvtiFransaBank.getMontant(), codeOperation);
		BkmvtiFransaBanks.add(bkmvtiFransaBank);

		return BkmvtiFransaBanks;
	}

	public BkmvtiFransaBank TestAccountAndSigneWithoutSayOperation(UAP040IN uap, int methode, int index2,
			int indexPieceComptable, int test, String account, String signe, MvbkConf mvk, String cle) {
		BkmvtiFransaBank bkmvtiFransaBank = new BkmvtiFransaBank();
		TestAccountLengthWithoutDayOperation(uap, account.length(), account, bkmvtiFransaBank, cle);
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
		bkmvtiFransaBank.setCodeService("0000");
		bkmvtiFransaBank.setSens(mvk.getSigne());
		bkmvtiFransaBank.setExonerationcommission("O");
		int lengthNumPiece = uap.getNumAutorisation().length();
		bkmvtiFransaBank.setNumPiece(uap.getNumAutorisation().substring(lengthNumPiece - 11, lengthNumPiece));
		bkmvtiFransaBank.setTauxChange("1" + getSpace(6));
		bkmvtiFransaBank.setCalculmouvementInteragence("N");
		bkmvtiFransaBank.setMouvementAgence("N");
		int lengthNumAuth = uap.getNumAutorisation().length();
		int lengthRefDossier = (uap.getNumTransaction() + uap.getDateTransaction()
				+ uap.getNumAutorisation().substring(lengthNumAuth - 6, lengthNumAuth)).length();

		bkmvtiFransaBank.setRefDossier(uap.getNumTransaction() + uap.getDateTransaction()
				+ uap.getNumAutorisation().substring(lengthNumAuth - 6, lengthNumAuth)
				+ getSpace(50 - lengthRefDossier));

		TestCodeAgenceWithoutDayOperation(uap, mvk.getCodeAgence(), mvk, bkmvtiFransaBank);

		getTransactionDate(uap.getDateTransaction(), bkmvtiFransaBank);

		int lengthReferanceLettrage = uap.getNumAutorisation().length();

		bkmvtiFransaBank.setReferanceLettrage(uap.getDateTransaction().substring(6, uap.getDateTransaction().length())
				+ uap.getNumAutorisation().substring(lengthReferanceLettrage - 6, lengthReferanceLettrage));

//		getAmount(Float.parseFloat(uap.getMontantAComponser()), bkmvtiFransaBank);

//
		
		HashMap<String, BigDecimal> data = new HashMap<String, BigDecimal>();
		BigDecimal mntRet= new BigDecimal( uap.getMontantRetrait());
		BigDecimal commConf= new BigDecimal( uap.getMontantCommission());
  
		data.put("MntRet", mntRet);
		data.put("commConf", commConf);
        BigDecimal roundedValue = fIPService.evaluateWithElementswithBigDecimal(mvk.getCodeSettlement(), data).divide(new BigDecimal(100));

        DecimalFormat decimalFormat = new DecimalFormat("000000000000000000.00");
        String formattedValue = decimalFormat.format(roundedValue);

        // Replace the decimal point with a comma
        formattedValue = formattedValue.replace('.', ',');

        // save the formatted value
        bkmvtiFransaBank.setMontant(formattedValue);
		//
		bkmvtiFransaBank.setCodeDeviceOrigine("208");
		bkmvtiFransaBank.setIdentification(mvk.getIdentification());
		setSameDataWithoutDayOperation(uap, methode, bkmvtiFransaBank, index2, indexPieceComptable, mvk);
		try {
			FileRequest.print(bkmvtiFransaBank.getMontant(), FileRequest.getLineNumber());
			FileRequest.print(bkmvtiFransaBank.getIdentification(), FileRequest.getLineNumber());
			FileRequest.print(bkmvtiFransaBank.getNumRefTransactions(), FileRequest.getLineNumber());
			if (Double.valueOf(bkmvtiFransaBank.getMontant().replace(",", ".")) <= 0) {

				throw new Exception("amount lower than expected");
			}
		} catch (Exception e) {
			return null;
		}
		return bkmvtiFransaBank;

	}

	public BkmvtiFransaBank TestAccountLengthWithoutDayOperation(UAP040IN uap, int lengAccount, String Account,
			BkmvtiFransaBank bkmvtiFransaBank, String cle) {

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

	public BkmvtiFransaBank setSameDataWithoutDayOperation(UAP040IN uap, int methode, BkmvtiFransaBank bkmvtiFransaBank,
			int index2, int indexPieceComptable, MvbkConf mvb) {
		String lib = mvb.getLibelle_operation();
		bkmvtiFransaBank.setCodeOperation(mvb.getCodeOperation());
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

		case 2:
			bkmvtiFransaBank.setPieceComptable(uap.getPieceComptableBKM());
			bkmvtiFransaBank.setNumEvenement(uap.getPieceComptableBKM()
					.substring(uap.getPieceComptableBKM().length() - 6, uap.getPieceComptableBKM().length()));
			break;

		case 16:
			bkmvtiFransaBank.setPieceComptable(uap.getPieceComptableBKM());

			bkmvtiFransaBank.setNumEvenement(uap.getPieceComptableBKM()
					.substring(uap.getPieceComptableBKM().length() - 6, uap.getPieceComptableBKM().length()));
			break;
		case 10:
			bkmvtiFransaBank.setPieceComptable(uap.getPieceComptableBKM());

			bkmvtiFransaBank.setNumEvenement(uap.getPieceComptableBKM()
					.substring(uap.getPieceComptableBKM().length() - 6, uap.getPieceComptableBKM().length()));
			break;

		default:
			bkmvtiFransaBank.setPieceComptable(uap.getPieceComptableBKM());
			bkmvtiFransaBank.setNumEvenement(uap.getPieceComptableBKM()
					.substring(uap.getPieceComptableBKM().length() - 6, uap.getPieceComptableBKM().length()));
		}
		return bkmvtiFransaBank;
	}

	public BkmvtiFransaBank TestCodeAgenceWithoutDayOperation(UAP040IN uap, String codeAgence, MvbkConf mvk,
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

//	public BkmvtiFransaBank getAmount(float amountSettlement, BkmvtiFransaBank bkmvtiFransaBank) {
//		int amountRound = Math.round(amountSettlement);
//		String amountString = String.valueOf(amountRound);
//		int AmountLengh1 = amountString.length();
//		String amount = amountString.substring(0, AmountLengh1 - 2) + "," + amountString.substring(AmountLengh1 - 2);
//
//		bkmvtiFransaBank.setMontant(getZero(20 - AmountLengh1) + amount);
//		return bkmvtiFransaBank;
//	}

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

	public static void moveFilesByStartingName(String sourceDirectory, String destinationDirectory, String startingName)
			throws IOException {
		File sourceDir = new File(sourceDirectory);
		File[] files = sourceDir.listFiles((dir, name) -> name.contains(startingName));

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
