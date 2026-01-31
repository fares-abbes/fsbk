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
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
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
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.google.common.base.Throwables;
import com.google.gson.Gson;
import com.mss.backOffice.request.AddFileRequest;
import com.mss.backOffice.request.ExtractDataDto;
import com.mss.backOffice.request.ReadFilesDto;
import com.mss.backOffice.request.UapEntity;
import com.mss.backOffice.request.UapIn;
import com.mss.backOffice.services.FormulaInterpreterService;
import com.mss.backOffice.services.PropertyService;
import com.mss.unified.dto.UapDetailsControl;
import com.mss.unified.entities.BatchesFC;
import com.mss.unified.entities.BkmvtiFransaBank;
import com.mss.unified.entities.CroControl;
import com.mss.unified.entities.DayOperationFransaBank;
import com.mss.unified.entities.MvbkConf;
import com.mss.unified.entities.MvbkSettlement;
import com.mss.unified.entities.UAP040IN;
import com.mss.unified.entities.UAP050FransaBank;
import com.mss.unified.entities.UAP050IN;
import com.mss.unified.entities.UAP051IN;
import com.mss.unified.entities.User;
import com.mss.unified.entities.dayOperationReglement;
import com.mss.unified.entities.OpeningDay;
import com.mss.unified.repositories.BatchesFFCRepository;
import com.mss.unified.repositories.BkmvtiFransaBankRepository;
import com.mss.unified.repositories.CardRepository;
import com.mss.unified.repositories.CodeBankBCRepository;
import com.mss.unified.repositories.CroControlRepository;
import com.mss.unified.repositories.DayOperationFransaBankRepository;
import com.mss.unified.repositories.FileTRepository;
import com.mss.unified.repositories.MotifRejetRepository;
import com.mss.unified.repositories.MvbkConfigRepository;
import com.mss.unified.repositories.MvbkSettlementRepository;
import com.mss.unified.repositories.OpposedCardRepository;
import com.mss.unified.repositories.UAP051INFransaBankRepository;
import com.mss.unified.repositories.UAP051INFransaBankHistoryRepository;
import com.mss.unified.repositories.UAP051INFransaBankRepository;
import com.mss.unified.repositories.UserRepository;
import com.mss.unified.repositories.dayOperationReglementRepository;
import com.mss.unified.repositories.OpeningDayRepository;

@RestController
@RequestMapping("FransaBank051IN")
public  strictfp class FransaBank051INController {
	@Autowired
	CardRepository cardRepository;
	@Autowired
	OpeningDayRepository openedDayRepo;
	@Autowired
	dayOperationReglementRepository dayReglementRepo;

	@Autowired
	DayOperationFransaBankRepository dayRepo;
	@Autowired
	UAP051INFransaBankRepository uAP051INFransaBankRepository;
	@Autowired
	UAP051INFransaBankHistoryRepository uAP051INHR;
	@Autowired
	MotifRejetRepository mtvRejetRepository;
	@Autowired
	MvbkSettlementRepository mvbkSettlementRepository;
	@Autowired
	DayOperationFransaBankRepository dayOperationFransaBankRepository;
	@Autowired
	BkmvtiFransaBankRepository bkmvtiFransaBankRepository;
	private static List<UapEntity> duplicateElements;
	@Autowired
	OpposedCardRepository opCardBin;
	@Autowired
	BatchesFFCRepository batchesFFCRepository;
	@Autowired
	FileTRepository fileSummaryTRepository;
	@Autowired
	CodeBankBCRepository codeBankBCRepository;
	@Autowired
	BatchesFFCRepository batchRepo;

	@Autowired
	UserRepository userRepository;
	@Autowired
	PropertyService propertyService;
	@Autowired
	public MvbkConfigRepository mvbkConfigR;
	@Autowired
	FormulaInterpreterService fIPService;
	@Autowired
	private CroControlRepository croControlRepository;
	private static HashMap<String, String> reglementList;
	public static int max;
	private static final Logger logger = LoggerFactory.getLogger(FransaBank050INController.class);
	private static HashMap<String, String> rioList;

	public synchronized int getEveIndex() {

		return (++ExecutorThreadUAPINFileBC.eve);
	}

	public synchronized int getEveIndex1() {
		return (++ExecutorThreadUAPINFileBC.eve1);
	}

	private static final Gson gson = new Gson();
	private static final int inProgressBatch = 0;
	private static final int errorProgressBatch = 2;
	private static int duplicatecount = 0;

	public static String getLineNumber() {
		return Thread.currentThread().getStackTrace()[2].getClassName() + ": "
				+ String.valueOf(Thread.currentThread().getStackTrace()[2].getLineNumber());
	}

	private static String codeBank = "035";

	public static void print(String x, String y) {
		System.out.println(y + "  " + x + " at date " + LocalDateTime.now());
	}

	public int validTransaction(AddFileRequest addFileRequest) throws IOException {
        try {
            String lastNumIndex = bkmvtiFransaBankRepository.getLastNumIndex();
            max = (lastNumIndex == null) ? 1 : Integer.valueOf(lastNumIndex);
        } catch (Exception e) {
            max = 1; // Default value in case of an exception
            logger.error("An exception occurred while retrieving the last number index: {}", e.getMessage(), e);
        }
		List<UAP051IN> UAPFransaBankS = uAP051INFransaBankRepository.getListUAPIN();

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
			String numAutorisationCro = ligne.substring(305, 311);
			String PanCro = ligne.substring(202, 218);
			String numTransaction = ligne.substring(257, 269);
			FileRequest.print(" " + numAutorisationCro, FileRequest.getLineNumber());
			FileRequest.print(" " + PanCro, FileRequest.getLineNumber());
			FileRequest.print(" " + numTransaction, FileRequest.getLineNumber());

			if (!fileUap.containsKey(numAutorisationCro + PanCro + numTransaction) && uAP051INHR.findinHistorys(numAutorisationCro, PanCro, numTransaction).isEmpty()
					&& uAP051INFransaBankRepository.findinHistorys(numAutorisationCro, PanCro, numTransaction).isEmpty()) {
				fileUap.put(numAutorisationCro + PanCro + numTransaction, ligne);
				rioList.put(numAutorisationCro + PanCro + numTransaction, element.substring(0, 38));
				reglementList.put(numAutorisationCro + PanCro + numTransaction, element.substring(38, 46));

			} else {
				FileRequest.print("" + (uAP051INHR.findByRio(element.substring(0, 38)).isEmpty()),
						FileRequest.getLineNumber());
				FileRequest.print("" + fileUap.size(), FileRequest.getLineNumber());
				FileRequest.print("" + numAutorisationCro, FileRequest.getLineNumber());
				FileRequest.print("" + PanCro, FileRequest.getLineNumber());
				UapEntity el = new UapEntity();
				el.setData(ligne);
				el.setNumAutorisation(numAutorisationCro);
				el.setPanCro(PanCro);
				el.setNumTransaction(numTransaction);
				el.setRio(element.substring(0, 38));
				el.setReglement(element.substring(38, 46));
				duplicateElements.add(el);
			}

		}
		FileRequest.print(" " + duplicateElements.size(), FileRequest.getLineNumber());
		FileRequest.print(" " + fileUap.size(), FileRequest.getLineNumber());
		logger.info("length UAPFransaBankS =>{}", UAPFransaBankS.size());

		// trasform uap to hMap
		Map<String, UAP051IN> MapUap = new HashMap<>();
		for (UAP051IN u : UAPFransaBankS) {
			String PAN = u.getCodeBin().trim() + u.getTypeCarte().trim() + u.getNumSeq().trim() + u.getNumOrdre().trim()
					+ u.getCle().trim();
			int lenNumAutorisationUap = u.getNumAutorisation().length();
			String numAutorisationUap = (u.getNumAutorisation().substring(lenNumAutorisationUap - 6,
					lenNumAutorisationUap)).trim();
			MapUap.put(numAutorisationUap + PAN + u.getNumTransaction(), u);
		}
		if (list.size() != (fileUap.size() + duplicateElements.size())) {
			FileRequest.print(" lists empty", FileRequest.getLineNumber());

			return -1;
		}
		logger.info("length fileUap =>{}", fileUap.size());

		logger.info("length Mapaps =>{}", MapUap.size());
		logger.info("length UAPFransaBankS =>{}", UAPFransaBankS.size());
		validAccept(MapUap, fileUap);
		ValidExtraNotUapInFile(MapUap, fileUap, addFileRequest.getFileDate());
		if (duplicateElements != null && !duplicateElements.isEmpty()) {
			ExtraNotUapInFileDuplicate(MapUap, duplicateElements, addFileRequest.getFileDate());
		}

		validNotOk(MapUap, fileUap);
//				moveFilesByStartingName(addFileRequest.getFilePath(), propertyService.getCompensationfilePathCro(),
//						Name);
		uAP051INFransaBankRepository.saveAll(MapUap.values());

		/////////// CRO control/////////////

		saveCroControl(data.getMapContentByFile());

		/////////////////////////////////////

		if (MapUap.values().size() > 0) {
			Stream<UAP051IN> stream = MapUap.values().stream();
			List<UAP051IN> validlist = stream
					.filter(element -> element.getAccepted().equals("1") || element.getAccepted().equals("NOT OK"))
					.collect(Collectors.toList());
			moveFilesByStartingName(addFileRequest.getFilePath(), propertyService.getCompensationfilePathCro(), "051");
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
			croControl.setTypeCro("051");
			long sumFromFile = 0;
			int nbFromFile = 0;
			String dateReg = "";

			for (String element : fileContent) {
				String ligne = element.substring(46, element.length());
				String montantCompenser = ligne.substring(234, 249);

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
				List<UapDetailsControl> acceptedUpa51In = uAP051INFransaBankRepository.getAcceptedUap51In(dateReg);
				List<UapDetailsControl> rejectedUpa51In = uAP051INFransaBankRepository.getRejectedUap51In(dateReg);
				List<UapDetailsControl> extraUpa51In = uAP051INFransaBankRepository.getExtraUap51In(dateReg);

				long sumAcceptedUpa51In = 0;
				long sumRejectedUpa51In = 0;
				long sumExtraUpa51In = 0;

				for (UapDetailsControl el : acceptedUpa51In) {
					sumAcceptedUpa51In += Long.valueOf(el.getMontantAComponser().replace(".", ""));
				}

				for (UapDetailsControl el : rejectedUpa51In) {
					sumRejectedUpa51In += Long.valueOf(el.getMontantAComponser().replace(".", ""));
				}

				for (UapDetailsControl el : extraUpa51In) {
					sumExtraUpa51In += Long.valueOf(el.getMontantAComponser().replace(".", ""));
				}

				croControl.setSumValidated(sumAcceptedUpa51In);
				croControl.setNbTotalValidated(acceptedUpa51In.size());
				logger.info("SumValidated => {}", sumAcceptedUpa51In);
				logger.info("NbTotalValidated => {}", acceptedUpa51In.size());

				croControl.setSumRejected(sumRejectedUpa51In);
				croControl.setNbTotalRejected(rejectedUpa51In.size());
				logger.info("SumRejected => {}", sumRejectedUpa51In);
				logger.info("NbTotalRejected => {}", rejectedUpa51In.size());

				croControl.setSumExtra(sumExtraUpa51In);
				croControl.setNbTotalExtra(extraUpa51In.size());
				logger.info("SumExtra => {}", sumExtraUpa51In);
				logger.info("NbTotalExtra => {}", extraUpa51In.size());

			}

			croControlRepository.save(croControl);

		}

	}

	public ExtractDataDto extractData(AddFileRequest addFileRequest) throws IOException {
		logger.info("extractData");
		ExtractDataDto extractedData = new ExtractDataDto();
		ArrayList<String> fileRows = new ArrayList<String>();
		List<String> filesNames = listAndFilterFiles(addFileRequest.getFilePath(), "051.DZD.CRO");
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

	public void validAccept(Map<String, UAP051IN> UAPFransaBankS, Map<String, String> list1) {
		for (Map.Entry<String, UAP051IN> entry : UAPFransaBankS.entrySet()) {
			UAP051IN u = entry.getValue();
			String PAN = u.getCodeBin().trim() + u.getTypeCarte().trim() + u.getNumSeq().trim() + u.getNumOrdre().trim()
					+ u.getCle().trim();
			int lenNumAutorisationUap = u.getNumAutorisation().length();
			String numAutorisationUap = (u.getNumAutorisation().substring(lenNumAutorisationUap - 6,
					lenNumAutorisationUap)).trim();

			if (list1.containsKey(numAutorisationUap + PAN + u.getNumTransaction())) {

				String e = list1.get(numAutorisationUap + PAN + u.getNumTransaction());
				String dateTransactionCro = e.substring(269, 277);
				String MontantRetrait = e.substring(218, 233);
				String MontantComission = e.substring(250, 257);
				String MontantCompenser = e.substring(234, 249);
				u.setFileMontantAcompenser(MontantCompenser);
				u.setFileMontantcommission(MontantComission);
				u.setRio(rioList.get(numAutorisationUap + PAN + u.getNumTransaction()));
				FileRequest.print(MontantRetrait, FileRequest.getLineNumber());
				FileRequest.print(u.getMontantRetrait().trim(), FileRequest.getLineNumber());
				FileRequest.print(MontantCompenser, FileRequest.getLineNumber());
				FileRequest.print(u.getMontantAComponser().trim(), FileRequest.getLineNumber());
				FileRequest.print(MontantComission, FileRequest.getLineNumber());
				FileRequest.print(u.getMontantCommission().trim(), FileRequest.getLineNumber());
				FileRequest.print(dateTransactionCro, FileRequest.getLineNumber());
				FileRequest.print(u.getDateTransaction().trim(), FileRequest.getLineNumber());
				if ((u.getDateTransaction().trim()).equals(dateTransactionCro)
						&& (u.getMontantRetrait().trim()).equals(MontantRetrait)
						&& (u.getMontantCommission().trim()).equals(MontantComission)
						&& (u.getMontantAComponser().trim()).equals(MontantCompenser)

				) {
					if (opCardBin.findByCardNumber(PAN).isPresent()) {
						u.setAccepted("NOT OK");
						u.setFlag("20");
						u.setMotifRejet("008");
					} else if (calculeNumberOfDate(u.getDateTransaction()) > mtvRejetRepository.findByCode("505").get()
							.getDelais()) {
						u.setAccepted("NOT OK");
						u.setFlag("20");
						u.setMotifRejet("505");
					} else {

						u.setAccepted("1");
					}
					FileRequest.print(numAutorisationUap + PAN + u.getNumTransaction(), FileRequest.getLineNumber());
					FileRequest.print(reglementList.get(numAutorisationUap + PAN + u.getNumTransaction()), FileRequest.getLineNumber());

					u.setDateReglement(reglementList.get(numAutorisationUap + PAN + u.getNumTransaction()));

				} else {
					FileRequest.print(" set to null " + u.getNumAutorisation() + " " + u.getAccepted(),
							FileRequest.getLineNumber());
					u.setAccepted(null);

				}
			} else {
				u.setAccepted("3");

			}
		}
	}
 

	public void ValidExtraNotUapInFile(Map<String, UAP051IN> UAPFransaBankS, Map<String, String> list1,
			String fileDate) {
		for (Map.Entry m : list1.entrySet()) {
			if (!UAPFransaBankS.containsKey(m.getKey())) {
				String ligne = (String) m.getValue();
				UAP051IN uap51in = new UAP051IN();

				// ****set data
				uap51in.setFileIntegrationDate(fileDate);
				uap51in.setAccepted("2");
				uap51in.setTypeTransaction(ligne.substring(0, 3));
				uap51in.setTypePaiement(ligne.substring(3, 5));
				uap51in.setReferenceArchivage(ligne.substring(5, 23));
				uap51in.setIndicateur(ligne.substring(23, 24));
				uap51in.setNumRIBcommercant(ligne.substring(24, 44));
				uap51in.setPrefixeIBAN(ligne.substring(44, 48));
				uap51in.setLibelleCommercant(ligne.substring(48, 98));
				uap51in.setAdresseCommercant(ligne.substring(98, 168));
				uap51in.setTelephoneCommercant(ligne.substring(168, 178));
				uap51in.setNumContratAccepteur(ligne.substring(178, 193));
				uap51in.setCodeActivite(ligne.substring(193, 199));
				uap51in.setReserved(ligne.substring(199, 202));
				uap51in.setCodeBin(ligne.substring(202, 208));
				uap51in.setTypeCarte(ligne.substring(208, 210));
				uap51in.setNumSeq(ligne.substring(210, 216));
				uap51in.setNumOrdre(ligne.substring(216, 217));
				uap51in.setCle(ligne.substring(217, 218));
				uap51in.setMontantRetrait(ligne.substring(218, 233));
				uap51in.setCodeDebitCommercant(ligne.substring(233, 234));
				uap51in.setMontantAComponser(ligne.substring(234, 249));
				uap51in.setFileMontantAcompenser(ligne.substring(234, 249));
				uap51in.setCodeDebitPorteur(ligne.substring(249, 250));
				uap51in.setMontantCommission(ligne.substring(250, 257));
				uap51in.setFileMontantcommission(ligne.substring(250, 257));
				uap51in.setNumTransaction(ligne.substring(257, 269));
				uap51in.setDateTransaction(ligne.substring(269, 277));
				uap51in.setHeureTransaction(ligne.substring(277, 283));
				uap51in.setIdentifSystem(ligne.substring(283, 293));
				uap51in.setIdentifPointRetrait(ligne.substring(293, 303));
				uap51in.setModeLectureCarte(ligne.substring(303, 304));
				uap51in.setMethAuthPorteur(ligne.substring(304, 305));
				uap51in.setNumAutorisation(getZero(9) + ligne.substring(305, 311));
				uap51in.setDateDebutValiditeCarte(ligne.substring(311, 319));
				uap51in.setDateFinValiditeCarte(ligne.substring(319, 327));
				uap51in.setCryptogramData(ligne.substring(327, 328));
				uap51in.setAtc(ligne.substring(328, 330));
				uap51in.setTvr(ligne.substring(330, 335));
				uap51in.setDateRemise(ligne.substring(335, 343));
				uap51in.setRio(rioList.get(m.getKey()));
				uap51in.setCodeBankAcquereur(uap51in.getRio().substring(0, 3));
				try {
					String PAN = uap51in.getCodeBin().trim() + uap51in.getTypeCarte().trim()
							+ uap51in.getNumSeq().trim() + uap51in.getNumOrdre().trim() + uap51in.getCle().trim();
					uap51in.setCodeAgence(cardRepository.findByCardNum(PAN).get().getAgencyCode());
				} catch (Exception e) {

					uap51in.setCodeAgence("99999");
					FileRequest.print(e.getStackTrace().toString(), FileRequest.getLineNumber());
				}
				uap51in.setDateReglement(reglementList.get(m.getKey()));
				if (opCardBin
						.findByCardNumber(uap51in.getCodeBin().trim() + uap51in.getTypeCarte().trim()
								+ uap51in.getNumSeq().trim() + uap51in.getNumOrdre().trim() + uap51in.getCle().trim())
						.isPresent()) {
					uap51in.setAccepted("NOT OK");
					uap51in.setFlag("20");
					uap51in.setMotifRejet("008");
				} else if (calculeNumberOfDate(uap51in.getDateTransaction()) > mtvRejetRepository.findByCode("405")
						.get().getDelais()) {
					uap51in.setAccepted("2");
					uap51in.setFlag("20");
					uap51in.setMotifRejet("515");
				} else if (uAP051INFransaBankRepository.findByRio(uap51in.getRio()).isEmpty()
						&& uAP051INHR.findByRio(uap51in.getRio()).isEmpty()) {

					uap51in.setAccepted("2");
				} else {
					uap51in.setAccepted("2");
					uap51in.setFlag("20");
					uap51in.setMotifRejet("001");
				}
				uap51in.setMontantTransaction(Integer.parseInt(uap51in.getMontantRetrait()));
				uap51in.setMontantCommissionFSBKTTC(Integer.parseInt(uap51in.getMontantCommission()));
				if ("03".equals(uap51in.getTypePaiement()) || "04".equals(uap51in.getTypePaiement())) {
					uap51in.setPieceComptableBKM("RB" + "976" + uap51in.getRio().substring(uap51in.getRio().length()-6));

				} else {
					uap51in.setPieceComptableBKM("RB" + "169" +  uap51in.getRio().substring(uap51in.getRio().length()-6));

				}
				uap51in.setMontantCommissionFSBKTTC(Math.abs(Integer.valueOf(uap51in.getFileMontantAcompenser())
						- Integer.valueOf(uap51in.getMontantTransaction())));

				UAPFransaBankS.put(m.getKey().toString(), uap51in);
			}

		}

	}

	public void ExtraNotUapInFileDuplicate(Map<String, UAP051IN> UAPFransaBankS, List<UapEntity> elements,
			String fileDate) {
		int index = 0;
		for (UapEntity m : elements) {
			index++;
			String ligne = (String) m.getData();
			UAP051IN uap51in = new UAP051IN();
			logger.info("new pan" + m.getPanCro() + " num autorisation" + m.getNumAutorisation());

			// ****set data
			uap51in.setFileIntegrationDate(fileDate);
			uap51in.setAccepted("2");
			uap51in.setTypeTransaction(ligne.substring(0, 3));
			uap51in.setTypePaiement(ligne.substring(3, 5));
			uap51in.setReferenceArchivage(ligne.substring(5, 23));
			uap51in.setIndicateur(ligne.substring(23, 24));
			uap51in.setNumRIBcommercant(ligne.substring(24, 44));
			uap51in.setPrefixeIBAN(ligne.substring(44, 48));
			uap51in.setLibelleCommercant(ligne.substring(48, 98));
			uap51in.setAdresseCommercant(ligne.substring(98, 168));
			uap51in.setTelephoneCommercant(ligne.substring(168, 178));
			uap51in.setNumContratAccepteur(ligne.substring(178, 193));
			uap51in.setCodeActivite(ligne.substring(193, 199));
			uap51in.setReserved(ligne.substring(199, 202));
			uap51in.setCodeBin(ligne.substring(202, 208));
			uap51in.setTypeCarte(ligne.substring(208, 210));
			uap51in.setNumSeq(ligne.substring(210, 216));
			uap51in.setNumOrdre(ligne.substring(216, 217));
			uap51in.setCle(ligne.substring(217, 218));
			uap51in.setMontantRetrait(ligne.substring(218, 233));
			uap51in.setCodeDebitCommercant(ligne.substring(233, 234));
			uap51in.setMontantAComponser(ligne.substring(234, 249));
			uap51in.setCodeDebitPorteur(ligne.substring(249, 250));
			uap51in.setMontantCommission(ligne.substring(250, 257));
			uap51in.setNumTransaction(ligne.substring(257, 269));
			uap51in.setDateTransaction(ligne.substring(269, 277));
			uap51in.setHeureTransaction(ligne.substring(277, 283));
			uap51in.setIdentifSystem(ligne.substring(283, 293));
			uap51in.setIdentifPointRetrait(ligne.substring(293, 303));
			uap51in.setModeLectureCarte(ligne.substring(303, 304));
			uap51in.setMethAuthPorteur(ligne.substring(304, 305));
			uap51in.setNumAutorisation(getZero(9) + ligne.substring(305, 311));
			uap51in.setDateDebutValiditeCarte(ligne.substring(311, 319));
			uap51in.setDateFinValiditeCarte(ligne.substring(319, 327));
			uap51in.setCryptogramData(ligne.substring(327, 328));
			uap51in.setAtc(ligne.substring(328, 330));
			uap51in.setTvr(ligne.substring(330, 335));
			uap51in.setDateRemise(ligne.substring(335, 343));
			uap51in.setRio(m.getRio());
			uap51in.setDateReglement(m.getReglement());
			uap51in.setMotifRejet("001");
			uap51in.setFlag("20");
			uap51in.setCodeBankAcquereur(uap51in.getRio().substring(0, 3));
			uap51in.setMontantTransaction(Integer.parseInt(uap51in.getMontantRetrait()));
			uap51in.setMontantCommissionFSBKTTC(Integer.parseInt(uap51in.getMontantCommission()));
			try {
				String PAN = uap51in.getCodeBin().trim() + uap51in.getTypeCarte().trim() + uap51in.getNumSeq().trim()
						+ uap51in.getNumOrdre().trim() + uap51in.getCle().trim();
				uap51in.setCodeAgence(cardRepository.findByCardNum(PAN).get().getAgencyCode());
			} catch (Exception e) {

				uap51in.setCodeAgence("99999");
				FileRequest.print(e.getStackTrace().toString(), FileRequest.getLineNumber());
			}
			if ("03".equals(uap51in.getTypePaiement()) || "04".equals(uap51in.getTypePaiement())) {
				uap51in.setPieceComptableBKM( "RB" + "976" +   uap51in.getRio().substring(uap51in.getRio().length()-6));
			} else {
				uap51in.setPieceComptableBKM("RB" + "169" +  uap51in.getRio().substring(uap51in.getRio().length()-6));
			}
			uap51in.setFileMontantAcompenser(uap51in.getMontantAComponser());
			uap51in.setFileMontantcommission(uap51in.getMontantCommission());
			uap51in.setMontantCommissionFSBKTTC(Math.abs(Integer.valueOf(uap51in.getFileMontantAcompenser()) - Integer.valueOf(uap51in.getMontantTransaction())));
			UAPFransaBankS.put(m.getNumAutorisation() + m.getPanCro() + index, uap51in);
		}

	}

	public void validNotOk(Map<String, UAP051IN> UAPFransaBankS, Map<String, String> uapFile) {
		for (UAP051IN element : UAPFransaBankS.values()) {
			if (element.getAccepted() == null) {
				String PAN = element.getCodeBin().trim() + element.getTypeCarte().trim() + element.getNumSeq().trim()
						+ element.getNumOrdre().trim() + element.getCle().trim();
				int lenNumAutorisationUap = element.getNumAutorisation().length();
				String numAutorisationUap = (element.getNumAutorisation().substring(lenNumAutorisationUap - 6,
						lenNumAutorisationUap)).trim();

				String file = uapFile.get(numAutorisationUap + PAN + element.getNumTransaction());
				String montantCompenser = file.substring(234, 249);
				String montantCommission = file.substring(250, 257);
				element.setRio(rioList.get(numAutorisationUap + PAN + element.getNumTransaction()));

				element.setFileMontantAcompenser(montantCompenser);
				element.setFileMontantcommission(montantCommission);
				element.setDateReglement(reglementList.get(numAutorisationUap + PAN + element.getNumTransaction()));
				element.setDatabaseMntACompenser(element.getMontantAComponser());
				element.setDatabaseMntCommission(element.getMontantCommissionFSBKTTC() + "");
				element.setMontantAComponser(montantCompenser);
				element.setMontantCommissionFSBKTTC(Integer.valueOf(montantCommission));

				element.setAccepted("NOT OK");
				element.setFlag("20");
				element.setMotifRejet("002");
				element.setMontantCommissionFSBKTTC(Math.abs(Integer.valueOf(element.getFileMontantAcompenser())
						- Integer.valueOf(element.getMontantTransaction())));

			}
		}
	}

	@PutMapping("matchingUAP051IN")
	public String matchingUAP051(@RequestBody AddFileRequest addFileRequest) throws IOException {
		FileRequest.print("matchingUAP051IN", FileRequest.getLineNumber());

		BatchesFC batch = batchesFFCRepository.findByKey("CRAUAP51IN").get();
		batch.setBatchLastExcution(batch.getBatchEndDate());
		batch.setBatchStatus(0);
		batch.setBatchDate(new Date());
		batch.setError(null);
		batch.setErrorStackTrace(null);
		batchRepo.saveAndFlush(batch);
		SecurityContextHolder.getContext().setAuthentication(OrshesterController.auth);

		int etat = validTransaction(addFileRequest);
		if (etat == 1) {
			List<UAP051IN> ListUAPFransaBanksFiltred = uAP051INFransaBankRepository.getListUAPINByStatusAccepted();
			for (UAP051IN uap : ListUAPFransaBanksFiltred) {
				if (((uap.getTypeTransaction().equals("051") && uap.getTypePaiement().equals("01"))
						|| (uap.getTypeTransaction().equals("051") && uap.getTypePaiement().equals("05")))) {
					valid051WithoutDayOperation(uap);
					uap.setBkmGeneration("done");
				} else if (((uap.getTypeTransaction().equals("051") && uap.getTypePaiement().equals("04"))
						|| (uap.getTypeTransaction().equals("051") && uap.getTypePaiement().equals("03")))) {
					valid055WithoutDayOperation(uap);
					uap.setBkmGeneration("done");
				}
			}
			uAP051INFransaBankRepository.saveAll(ListUAPFransaBanksFiltred);

			Integer count = uAP051INFransaBankRepository.sizeNotMatched() == null ? 0
					: uAP051INFransaBankRepository.sizeNotMatched();
			FileRequest.print("" + count, FileRequest.getLineNumber());
			if (count == 0) {
				openedDayRepo.updateStatus051(addFileRequest.getFileDate(), "doneCro");
				batchRepo.updateFinishBatch("CRAUAP51IN", 1, new Date());
				rioList.clear();
				reglementList.clear();
				logger.info("end matchingUap40In");
			} else {
				Integer countAut = uAP051INFransaBankRepository.sizeNotMatchedAndEmptyFlag() == null ? 0
						: uAP051INFransaBankRepository.sizeNotMatchedAndEmptyFlag();
				FileRequest.print("countAut " + countAut + "count" + count, getLineNumber());

				if (countAut < count) {
					batchRepo.updateFinishBatch("CRAUAP51IN", 1, new Date());

				} else {
					openedDayRepo.updateStatus051(addFileRequest.getFileDate(), "doneSort");
					batchRepo.updateFinishBatch("CRAUAP51IN", 5, new Date());
					batchRepo.updateFinishBatch("SENDLOT", -1, new Date());
				}
				Optional<OpeningDay> d = openedDayRepo.findByStatus051("doneSort");
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
		FileRequest.print("matching done 051", FileRequest.getLineNumber());
		return "Done";
	}

	/////// ***** ADD J+1 REGLEMENT ***********//////////////////
	@PutMapping("reglement051In")
	public void reglement050In() throws IOException {

		BatchesFC batch = batchesFFCRepository.findByKey("GNR03").get();
		batch.setBatchLastExcution(batch.getBatchEndDate());
		batch.setBatchStatus(0);
		batch.setBatchDate(new Date());
		batch.setError(null);
		batch.setErrorStackTrace(null);
		batchRepo.save(batch);
		SimpleDateFormat newFormat = new SimpleDateFormat("yyyyMMdd");
		String today = newFormat.format(new Date());

		List<UAP051IN> ListUAPFransaBanksFiltred = uAP051INFransaBankRepository
				.getListUAPINReglementByStatusAccepted(today);
		List<UAP051IN> uaps = new ArrayList<UAP051IN>();

		for (UAP051IN uap : ListUAPFransaBanksFiltred) {

			if ((uap.getTypeTransaction().equals("051") && uap.getTypePaiement().equals("01")
					|| uap.getTypeTransaction().equals("051") && uap.getTypePaiement().equals("05"))) {
				valid051ReglementWithoutDayOperation(uap);

			}

			else if (((uap.getTypeTransaction().equals("051") && uap.getTypePaiement().equals("04"))
					|| (uap.getTypeTransaction().equals("051") && uap.getTypePaiement().equals("03")))) {
				valid055ReglementWithoutDayOperation(uap);

			}
			uap.setDone("done");
			uaps.add(uap);
		}
		uAP051INFransaBankRepository.saveAll(uaps);

		BatchesFC batch2 = batchesFFCRepository.findByKey("GNR03").get();
		batch2.setBatchLastExcution(batch.getBatchEndDate());
		batch2.setBatchStatus(1);
		batch2.setBatchDate(new Date());
		batch2.setError(null);
		batch2.setErrorStackTrace(null);
		batchRepo.save(batch2);

	}

	public void valid051WithoutDayOperation(UAP051IN uap) {
		List<MvbkSettlement> allMvbkSettelemnts = mvbkSettlementRepository
				.findByIdentificationOffUsAcqRembourssementTpeRCROIN();
		List<MvbkConf> allMvbkSettelemntsC = mvbkConfigR.findByCategorie(39);

		List<BkmvtiFransaBank> BkmvtiFransaBanks = new ArrayList<BkmvtiFransaBank>();

		int indexPieceComptable = getEveIndex1();
		for (MvbkConf mvk : allMvbkSettelemntsC) {
			int index2 = getEveIndex();
			BkmvtiFransaBanks = TestSigneWithoutDayOperation(uap, 13, mvk, BkmvtiFransaBanks, indexPieceComptable,
					index2, mvk.getCodeSettlement());

		}

		logger.info("size valid051 =>{}", BkmvtiFransaBanks.size());
		if (BkmvtiFransaBanks != null && !BkmvtiFransaBanks.isEmpty()) {
		    // Filter out null elements from the list
		    List<BkmvtiFransaBank> nonNullBkmvtiFransaBanks = BkmvtiFransaBanks.stream()
		            .filter(Objects::nonNull)
		            .collect(Collectors.toList());

		    if (!nonNullBkmvtiFransaBanks.isEmpty()) {
		        bkmvtiFransaBankRepository.saveAll(nonNullBkmvtiFransaBanks);
		    }
		    
		}	}

	public void valid051ReglementWithoutDayOperation(UAP051IN uap) {
		List<MvbkSettlement> allMvbkSettelemnts = mvbkSettlementRepository
				.findByIdentificationOffUsAcqRembourssementTpeReglementIN();
		List<MvbkConf> allMvbkSettelemntsC = mvbkConfigR.findByCategorie(45);

		List<BkmvtiFransaBank> BkmvtiFransaBanks = new ArrayList<BkmvtiFransaBank>();

		int indexPieceComptable = getEveIndex1();

		for (MvbkConf mvk : allMvbkSettelemntsC) {
			int index2 = getEveIndex();
			BkmvtiFransaBanks = TestSigneWithoutDayOperation(uap, 13, mvk, BkmvtiFransaBanks, indexPieceComptable,
					index2, mvk.getCodeSettlement());

		}

		logger.info("size valid051 =>{}", BkmvtiFransaBanks.size());
		if (BkmvtiFransaBanks != null && !BkmvtiFransaBanks.isEmpty()) {
		    // Filter out null elements from the list
		    List<BkmvtiFransaBank> nonNullBkmvtiFransaBanks = BkmvtiFransaBanks.stream()
		            .filter(Objects::nonNull)
		            .collect(Collectors.toList());

		    if (!nonNullBkmvtiFransaBanks.isEmpty()) {
		        bkmvtiFransaBankRepository.saveAll(nonNullBkmvtiFransaBanks);
		    }
		    
		}	}

	public void valid055WithoutDayOperation(UAP051IN uap) {
		List<MvbkSettlement> allMvbkSettelemnts = mvbkSettlementRepository
				.findByIdentificationOffUsAcqRembourssementTpeCRO();
		List<BkmvtiFransaBank> BkmvtiFransaBanks = new ArrayList<BkmvtiFransaBank>();
		List<MvbkConf> allMvbkSettelemntsC = mvbkConfigR.findByCategorie(61);

		int indexPieceComptable = getEveIndex1();

		for (MvbkConf mvk : allMvbkSettelemntsC) {
			int index2 = getEveIndex();
			BkmvtiFransaBanks = TestSigneWithoutDayOperation(uap, 23, mvk, BkmvtiFransaBanks, indexPieceComptable,
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

	public void valid055ReglementWithoutDayOperation(UAP051IN uap) {
		List<MvbkSettlement> allMvbkSettelemnts = mvbkSettlementRepository
				.findByIdentificationOffUsAcqRembourssementInternetInReglement();
		List<BkmvtiFransaBank> BkmvtiFransaBanks = new ArrayList<BkmvtiFransaBank>();
		List<MvbkConf> allMvbkSettelemntsC = mvbkConfigR.findByCategorie(71);

		int indexPieceComptable = getEveIndex1();

		for (MvbkConf mvk : allMvbkSettelemntsC) {
			int index2 = getEveIndex();
			BkmvtiFransaBanks = TestSigneWithoutDayOperation(uap, 23, mvk, BkmvtiFransaBanks, indexPieceComptable,
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

	public List<BkmvtiFransaBank> TestSigneWithoutDayOperation(UAP051IN uap, int methode, MvbkConf mvk,
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
		BkmvtiFransaBanks.add(bkmvtiFransaBank);

		return BkmvtiFransaBanks;
	}

	public BkmvtiFransaBank TestAccountAndSigneWithoutSayOperation(UAP051IN uap, int methode, int index2,
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
//		Gson gson = new Gson();
//		HashMap<String, Float> data = gson.fromJson(uap.getDatasJson(), FransaBankController.typeJsonData);
//
//		getAmount(fIPService.evaluateWithElements(mvk.getCodeSettlement(), data), bkmvtiFransaBank);



		
		HashMap<String, BigDecimal> data = new HashMap<String, BigDecimal>();
		BigDecimal mntRet= new BigDecimal( uap.getMontantTransaction());
		BigDecimal commConf= new BigDecimal( uap.getMontantCommissionFSBKTTC());
  
		data.put("MntRemb", mntRet);
		data.put("CommRemb", commConf);
        BigDecimal roundedValue = fIPService.evaluateWithElementswithBigDecimal(mvk.getCodeSettlement(), data).divide(new BigDecimal(100));

        DecimalFormat decimalFormat = new DecimalFormat("000000000000000000.00");
        String formattedValue = decimalFormat.format(roundedValue);

        // Replace the decimal point with a comma
        formattedValue = formattedValue.replace('.', ',');

        // save the formatted value
        bkmvtiFransaBank.setMontant(formattedValue);
		
		
		bkmvtiFransaBank.setCodeDeviceOrigine("208");
		bkmvtiFransaBank.setIdentification(mvk.getIdentification());
		bkmvtiFransaBank.setNumRefTransactions(uap.getNumTransaction());

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

	public BkmvtiFransaBank TestAccountLengthWithoutDayOperation(UAP051IN uap, int lengAccount, String Account,
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

	public BkmvtiFransaBank setSameDataWithoutDayOperation(UAP051IN uap, int methode, BkmvtiFransaBank bkmvtiFransaBank,
			int index2, int indexPieceComptable, MvbkConf mvb) {
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

		case 13:
			bkmvtiFransaBank.setPieceComptable(uap.getPieceComptableBKM());

			bkmvtiFransaBank.setNumEvenement(uap.getPieceComptableBKM()
					.substring(uap.getPieceComptableBKM().length() - 6, uap.getPieceComptableBKM().length()));

			break;

		case 23:

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

	public BkmvtiFransaBank TestCodeAgenceWithoutDayOperation(UAP051IN uap, String codeAgence, MvbkConf mvk,
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


	public static String repeat(String str, int count) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < count; i++) {
			sb.append(str);
		}
		return sb.toString();
	}

	
	public void writeFile(String fileName, String content, boolean allowtime) {

		File sourceDirectory = new File(propertyService.getNewFile());
		FileRequest.print(propertyService.getNewFile(), FileRequest.getLineNumber());
		if (!sourceDirectory.exists() || !sourceDirectory.isDirectory()) {
			FileRequest.print(
					"Source directory does not exist or is not a directory: " + sourceDirectory.getAbsolutePath(),
					FileRequest.getLineNumber());
			return;
		}
		if (allowtime) {
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
			String timestamp = LocalDateTime.now().format(formatter);
			fileName = fileName + "_" + timestamp;
		}
		File file = new File(sourceDirectory, fileName);
		try (FileWriter writer = new FileWriter(file)) {
			writer.write(content);
			FileRequest.print("File created successfully at: " + file.getAbsolutePath(), FileRequest.getLineNumber());
		} catch (IOException e) {
			FileRequest.print("Error while writing the file: " + e.getMessage(), FileRequest.getLineNumber());

		}
	}

}
