package com.mss.backOffice.controller;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
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
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.beanutils.PropertyUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.google.common.base.Throwables;
import com.mss.backOffice.request.UapIn;
import com.mss.backOffice.services.FormulaInterpreterService;
import com.mss.backOffice.services.PropertyService;
import com.mss.unified.dto.UapDetailsControl;
import com.mss.unified.entities.BatchesFC;
import com.mss.unified.entities.BkmvtiFransaBank;
import com.mss.unified.entities.CraControl;
import com.mss.unified.entities.CraRejetControl;
import com.mss.unified.entities.DayOperationFransaBank;
import com.mss.unified.entities.MvbkConf;
import com.mss.unified.entities.MvbkSettlement;
import com.mss.unified.entities.UAP040IN;
import com.mss.unified.entities.UAP040INNotAccepted;
import com.mss.unified.entities.UAP040INR;
import com.mss.unified.entities.UAP050FransaBank;
import com.mss.unified.entities.UAP050FransaBankNotAccepted;
import com.mss.unified.entities.UAP050IN;
import com.mss.unified.entities.UAP051IN;
import com.mss.unified.entities.User;
import com.mss.unified.entities.OpeningDay;
import com.mss.unified.repositories.BatchesFFCRepository;
import com.mss.unified.repositories.BkmvtiFransaBankRepository;
import com.mss.unified.repositories.MvbkConfigRepository;
import com.mss.unified.repositories.MvbkSettlementRepository;
import com.mss.unified.repositories.UAP040INFransaBankRepository;
import com.mss.unified.repositories.UAP040INRFransaBankRepository;
import com.mss.unified.repositories.UserRepository;
import com.mss.unified.repositories.OpeningDayRepository;
import com.mss.unified.repositories.CraRejetControlRepository;
import com.mss.unified.repositories.UAP040InNotAcceptedRepository;
import com.mss.unified.repositories.UAP050INRFransaBankRepository;
import com.mss.unified.repositories.UAP051INRFransaBankRepository;

@RestController
@RequestMapping("CheckCroController040")
public strictfp class CheckCroController040 {
	public static String time040Ord;

	@Autowired
	OrshesterController orc;
	@Autowired
	FormulaInterpreterService fIPService;
	@Autowired
	OpeningDayRepository odr;
	@Autowired
	UserRepository userRepository;
	private static String codeBank = "035";
	@Autowired
	MvbkSettlementRepository mvbkSettlementRepository;
	@Autowired
	UAP040INFransaBankRepository uAP040INFransaBankRepository;
	@Autowired
	UAP050INRFransaBankRepository uAP050INRFransaBankRepository;
	@Autowired
	UAP051INRFransaBankRepository uAP051INRFransaBankRepository;
	@Autowired
	UAP040INRFransaBankRepository uAP040INRFransaBankRepository;
	@Autowired
	BkmvtiFransaBankRepository bkmvtiFransaBankRepository;
	@Autowired
	BatchesFFCRepository batchRepo;
	@Autowired
	PropertyService propertyService;
	@Autowired
	DownloadFileBc fbc;
	@Autowired
	public MvbkConfigRepository mvbkConfigR;
	@Autowired
	public CheckCroController050 ccc050;
	@Autowired
	public CraRejetControlRepository craRejetControlRepository;
	@Autowired
	public UAP040InNotAcceptedRepository uAP040InNotAcceptedRepository;
	public static int max;
	private boolean allaccepted;
	public static boolean allowMovementfiles;
	private static final Logger logger = LoggerFactory.getLogger(CheckCroController040.class);
	public static final String REJECTPENDING = "20";
	public static final String REJECTACCEPTED = "21";
	public static final String REJECTNOTACCEPTED = "22";
	public static final String REJECTMODIFIED = "23";
	public static final String REJECTERROR = "24";
	public static final String REJECTDONE = "25";

	public synchronized int getEveIndex() {
		return (++ExecutorThreadUAPINFileBC.eve);
	}

	public static HashMap<String, String> rioIn;

	public synchronized int getEveIndex1() {
		return (++ExecutorThreadUAPINFileBC.eve1);
	}

	// ************************REJECT NOT VALID
	// METHOD*****************************//
	public void RejectRetraitNotValid(UAP040INR uap) {
		List<MvbkSettlement> allMvbkSettelemnts = mvbkSettlementRepository.Reject40inRetraitExtra();
		List<MvbkConf> allMvbkSettelemntsC = mvbkConfigR.findByCategorie(548);
		List<BkmvtiFransaBank> BkmvtiFransaBanks = new ArrayList<BkmvtiFransaBank>();
		int indexPieceComptable = getEveIndex1();
		for (MvbkConf mvk : allMvbkSettelemntsC) {

			int index2 = getEveIndex();

			BkmvtiFransaBanks = TestSigneWithoutDayOperation(uap, 100, mvk, BkmvtiFransaBanks, indexPieceComptable,
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

	public void RejectAlgeriePosteNotValid(UAP040INR uap) {
		List<MvbkSettlement> allMvbkSettelemntsALP = mvbkSettlementRepository.Reject40inRetraitAlpExtra();
		List<MvbkConf> allMvbkSettelemntsC = mvbkConfigR.findByCategorie(549);

		List<BkmvtiFransaBank> BkmvtiFransaBanks = new ArrayList<BkmvtiFransaBank>();

		int indexPieceComptable = getEveIndex1();
		for (MvbkConf mvk : allMvbkSettelemntsC) {
			int index2 = getEveIndex();
			BkmvtiFransaBanks = TestSigneWithoutDayOperation(uap, 101, mvk, BkmvtiFransaBanks, indexPieceComptable,
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

	public void RejectConsultationSoldeNotValid(UAP040INR uap) {
		List<MvbkSettlement> allMvbkSettelemnts = mvbkSettlementRepository.Reject40inConsultationExtra();
		List<MvbkConf> allMvbkSettelemntsC = mvbkConfigR.findByCategorie(550);

		List<BkmvtiFransaBank> BkmvtiFransaBanks = new ArrayList<BkmvtiFransaBank>();
		int indexPieceComptable = getEveIndex1();
		for (MvbkConf mvk : allMvbkSettelemntsC) {
			int index2 = getEveIndex();
			BkmvtiFransaBanks = TestSigneWithoutDayOperation(uap, 102, mvk, BkmvtiFransaBanks, indexPieceComptable,
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
	// ************************END REJECT NOT VALID
	// METHOD*****************************//

	// ************************REJECT LIST NOT
	// VALID***************************************//
	@PutMapping("updateMissMatch")
	public void HandleRejects(@RequestBody UapIn uaps) {
		Optional<OpeningDay> d = odr.findByStatus040("doneSort");
		FileRequest.print(uaps.toString(), FileRequest.getLineNumber());

		if (d.isPresent()) {
			if (uaps.getIdRejet() != null && uaps.getIdRejet().size() > 0) {
				List<Integer> uapIdReject = uaps.getIdRejet();
				List<UAP040IN> rejected = uapIdReject.stream().map(element -> {
					UAP040IN uap = uAP040INFransaBankRepository.findById(element).get();
					uap.setFlag(REJECTPENDING);
//					uap.setMotifRejet(uaps.getMotifRExtra());
					return uap;
				}).collect(Collectors.toList());
				FileRequest.print("" + rejected.size(), FileRequest.getLineNumber());

				uAP040INFransaBankRepository.saveAll(rejected);
			}

			if (uaps.getIdAcceptation() != null && uaps.getIdAcceptation().size() > 0) {
				List<Integer> uapIdAcc = uaps.getIdAcceptation();
				List<UAP040IN> accepted = uapIdAcc.stream().map(element -> {
					UAP040IN uap = uAP040INFransaBankRepository.findById(element).get();
					uap.setFlag("1");
					return uap;
				}).collect(Collectors.toList());
				FileRequest.print("" + accepted.size(), FileRequest.getLineNumber());

				uAP040INFransaBankRepository.saveAll(accepted);
			}

			if (uaps.getHandleAll() != null && uaps.getHandleAll().equals("accept")) {
				List<UAP040IN> list = uAP040INFransaBankRepository.findByMatchedAndEmptyFlag();

				list.forEach(ele -> {
					ele.setFlag("1");
				});
				FileRequest.print("" + list.size(), FileRequest.getLineNumber());
				uAP040INFransaBankRepository.saveAll(list);
			}

			if (uaps.getHandleAll() != null && uaps.getHandleAll().equals("reject")) {
				List<UAP040IN> list = uAP040INFransaBankRepository.findByMatchedAndEmptyFlag();
				list.forEach(ele -> {
					ele.setFlag(REJECTPENDING);
					ele.setMotifRejet(uaps.getMotifRExtra());
				});
				FileRequest.print("" + list.size(), FileRequest.getLineNumber());
				uAP040INFransaBankRepository.saveAll(list);
			}

			copyUap040In();
			int nbnotOK = uAP040INRFransaBankRepository.findByFlagAndAccepted(REJECTPENDING, "NOT OK").size();
			int nbnExtra = uAP040INRFransaBankRepository.findByFlagAndAccepted(REJECTPENDING, "2").size();

			if ((nbnotOK + nbnExtra) > 0) {
				generateRejectFile();
				batchRepo.updateFinishBatch("CRAUAP40IN", 1, new Date());

			} else {
				batchRepo.updateFinishBatch("CRAUAP140IN", 1, new Date());
				batchRepo.updateFinishBatch("CRAUAP40IN", 1, new Date());
			}

		}

	}

	// ************************END ACCEPT NOT OK*********************************//

	// ************************REJECT EXTRA*************************************//
	public void RetraitRejectExtra(UAP040INR uap) {
		List<MvbkConf> allMvbkSettelemntsC = mvbkConfigR.findByCategorie(5548);
		List<BkmvtiFransaBank> bkms = new ArrayList<BkmvtiFransaBank>();
		int indexPieceComptable = ++max;
		for (MvbkConf mvk : allMvbkSettelemntsC) {
			bkms = getDataExtraUap(11, mvk, uap, bkms, indexPieceComptable);
		}
		bkmvtiFransaBankRepository.saveAll(bkms);
	}

	public void RetraitAlpRejectExtra(UAP040INR uap) {
		List<MvbkConf> allMvbkSettelemntsC = mvbkConfigR.findByCategorie(5549);
		List<BkmvtiFransaBank> bkms = new ArrayList<BkmvtiFransaBank>();
		int indexPieceComptable = ++max;
		uap.setMontantCommissionTTC(Integer.parseInt(uap.getFileMontantcommission()));
		uap.setMontantTransaction(Integer.parseInt(uap.getMontantRetrait()));

		for (MvbkConf mvk : allMvbkSettelemntsC) {

			bkms = getDataExtraUap(12, mvk, uap, bkms, indexPieceComptable);

		}

		bkmvtiFransaBankRepository.saveAll(bkms);
	}

	public void ConsultationRejectExtra(UAP040INR uap) {
		List<MvbkConf> allMvbkSettelemntsC = mvbkConfigR.findByCategorie(5550);

		List<BkmvtiFransaBank> bkms = new ArrayList<BkmvtiFransaBank>();
		int indexPieceComptable = ++max;

		for (MvbkConf mvk : allMvbkSettelemntsC) {

			bkms = getDataExtraUap(13, mvk, uap, bkms, indexPieceComptable);

		}

		bkmvtiFransaBankRepository.saveAll(bkms);
	}

	// ************************END ACCEPT EXTRA*********************************//
// ****************************Read reject CRA****************************** //
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

	public HashMap<String, String> readFiles(List<String> filteredFiles, String folderPath) {
		HashMap<String, String> fileContents = new HashMap<>();

		for (String fileName : filteredFiles) {
			String filePath = folderPath + File.separator + fileName;

			try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
				StringBuilder content = new StringBuilder();
				String line;
				int i = 0;
				while ((line = reader.readLine()) != null) {
					if (i == 0) {
						i++;
					} else {
						fileContents.put(line.substring(78, 116), line);
						i++;
					}
				}
			} catch (IOException e) {
				// Handle the exception, e.g., log it or throw a custom exception
				e.printStackTrace();
			}
		}

		return fileContents;
	}

	// used temporarly to force the rejection
	public void forceReject() {
		updateRejectionExtra(REJECTACCEPTED);
		updateRejectionNOTOK(REJECTACCEPTED);
	}

	@GetMapping("/integrateCra")
	public void handelRejectNotOK() {
		Optional<OpeningDay> opd = odr.findByStatus040("doneSort");
		if (opd.isPresent() && uAP040INRFransaBankRepository.findByFlag(REJECTPENDING).size() > 0) {
			List<UAP040INR> newList = uAP040INRFransaBankRepository.findByFlag(REJECTPENDING);
			BatchesFC batch = batchRepo.getOne(53);
			batch.setBatchDate(new Date());
			batch.setBatchStatus(0);
			allowMovementfiles = false;
			List<String> filesNames = listAndFilterFiles(batch.getFileLocation(), "140");
			allaccepted = true;

			if (filesNames != null && filesNames.size() > 0) {
				// readfile
				rioIn = readFiles(filesNames, batch.getFileLocation());
				FileRequest.print(rioIn.toString(), FileRequest.getLineNumber());
				// verif by rio
				newList.forEach(el -> {
					FileRequest.print(el.getRio(), FileRequest.getLineNumber());
					FileRequest.print(rioIn.get(el.getRio()), FileRequest.getLineNumber());

					if (rioIn.get(el.getRio()) != null && rioIn.get(el.getRio()).length() > 0) {
						String status = (String) rioIn.get(el.getRio()).substring(rioIn.get(el.getRio()).length() - 3,
								rioIn.get(el.getRio()).length());
						FileRequest.print(status, FileRequest.getLineNumber());
						if (!rioIn.get(el.getRio()).substring(38, 46).equals("00000000")) {
							el.setDateReg(rioIn.get(el.getRio()).substring(38, 46));
						}
						if (status == null) {
							el.setFlag(REJECTERROR);
							allaccepted = false;
						} else if (status.equals("000")) {
							el.setFlag(REJECTACCEPTED);
							allowMovementfiles = true;
						} else {
							el.setFlag(REJECTNOTACCEPTED);
							allaccepted = false;
							allowMovementfiles = true;

						}
					}
				});
				uAP040INRFransaBankRepository.saveAll(newList);
				updateRejectionExtra(REJECTACCEPTED);
				updateRejectionNOTOK(REJECTACCEPTED);

				try {
					if (allowMovementfiles) {
						for (String file : filesNames) {
							moveFilesByStartingName(batch.getFileLocation(), propertyService.getCompensationfilePath(),
									file);
						}
					}
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} else {

				newList.forEach(el -> {
					el.setFlag(REJECTERROR);
					allaccepted = false;
				});

			}
			/////////// save cra LOT control /////////////
			saveCraControl(opd.get().getFileIntegration());
			///////////////////////////////////////////
			ccc050.handelRejectNotOK();

			if (allaccepted) {
				OpeningDay day = odr.findByStatus040("doneSort").get();
				day.setStatus040("doneCro");
				odr.save(day);

				batch.setBatchStatus(1);
				batch.setBatchEndDate(new Date());
				batchRepo.save(batch);
			} else {
				batch.setBatchStatus(5);
				batch.setBatchEndDate(new Date());
				batchRepo.save(batch);

			}
		} else {
			ccc050.handelRejectNotOK();
		}

		if (uAP040INRFransaBankRepository.findByFlag(REJECTPENDING).size() == 0
				&& uAP040INRFransaBankRepository.findByFlag(REJECTERROR).size() == 0
				&& uAP050INRFransaBankRepository.findByFlag(REJECTPENDING).size() == 0
				&& uAP050INRFransaBankRepository.findByFlag(REJECTERROR).size() == 0
				&& uAP051INRFransaBankRepository.findByFlag(REJECTPENDING).size() == 0
				&& uAP050INRFransaBankRepository.findByFlag(REJECTERROR).size() == 0) {

			BatchesFC execstatus = batchRepo.findByKey("execStatus").get();
			execstatus.setBatchNumber(0);
			batchRepo.save(execstatus);
			batchRepo.updateFinishBatch("TP", -1, new Date());
			batchRepo.updateFinishBatch("CRAUAP140IN", 1, new Date());
			batchRepo.updateFinishBatch("CRAUAP150IN", 1, new Date());
			batchRepo.updateFinishBatch("CRAUAP151IN", 1, new Date());
//		fbc.writeInFile();

			batchRepo.updateFinishBatch("ByPassTP", -1, new Date());
		} else {

			if (uAP040INRFransaBankRepository.findByFlag(REJECTPENDING).size() > 0
					|| uAP040INRFransaBankRepository.findByFlag(REJECTERROR).size() > 0) {
				FileRequest.print("here", FileRequest.getLineNumber());
				batchRepo.updateFinishBatch("CRAUAP140IN", 2, new Date());

			}
			if (uAP050INRFransaBankRepository.findByFlag(REJECTPENDING).size() > 0
					|| uAP050INRFransaBankRepository.findByFlag(REJECTERROR).size() > 0) {
				FileRequest.print("here", FileRequest.getLineNumber());

				batchRepo.updateFinishBatch("CRAUAP150IN", 2, new Date());

			}
			if (uAP051INRFransaBankRepository.findByFlag(REJECTPENDING).size() > 0
					|| uAP051INRFransaBankRepository.findByFlag(REJECTERROR).size() > 0) {
				FileRequest.print("here", FileRequest.getLineNumber());

				batchRepo.updateFinishBatch("CRAUAP151IN", 2, new Date());

			}

		}
	}

	private void saveCraControl(String fileIntegration) {
		List<CraRejetControl> craControlList = craRejetControlRepository.findByProcessingDateAndLotType(fileIntegration,
				"140");

		if (craControlList.size() > 0) {

			List<UapDetailsControl> acceptedUp = uAP040INRFransaBankRepository.getListUAP40AcceptedForControl();
			long sumAcceptedUp = 0;
			for (UapDetailsControl el : acceptedUp) {
				sumAcceptedUp += Long.valueOf(el.getMontantAComponser().replace(".", ""));

			}
//			
			long sumNotAccepted = 0;
			List<UAP040INR> notAccepted = uAP040INRFransaBankRepository.getListUAP40NotAcceptedForControl();
			List<UAP040INNotAccepted> notAcceptedList = new ArrayList<UAP040INNotAccepted>();
			for (UAP040INR el : notAccepted) {
				sumNotAccepted += Long.valueOf(el.getMontantAComponser().replace(".", ""));

				UAP040INNotAccepted notAcceptedUap40 = new UAP040INNotAccepted();

				try {
					PropertyUtils.copyProperties(notAcceptedUap40, el);
					notAcceptedUap40.setControlId(fileIntegration);

				} catch (Exception ex) {
					logger.info("Exception");
					logger.info(Throwables.getStackTraceAsString(ex));
				}
				notAcceptedList.add(notAcceptedUap40);

			}

			for (CraRejetControl craControl : craControlList) {
				logger.info("cra control is found");

				craControl.setSumAccepted(sumAcceptedUp);
				craControl.setNbAccepted(acceptedUp.size());

				craControl.setSumNotAccepted(sumNotAccepted);
				craControl.setNbNotAccepted(notAccepted.size());

				craRejetControlRepository.save(craControl);
			}
			uAP040InNotAcceptedRepository.saveAll(notAcceptedList);
		}
	}

	public void updateRejectionExtra(String flag) {
		List<UAP040INR> elements = uAP040INRFransaBankRepository.findByFlagAndAcceptedAndDateRegAfterToday(flag, "2");
		List<UAP040INR> upUpdated = new ArrayList<UAP040INR>();
		try {
	        try {
	            String lastNumIndex = bkmvtiFransaBankRepository.getLastNumIndex();
	            max = (lastNumIndex == null) ? 1 : Integer.valueOf(lastNumIndex);
	        } catch (Exception e) {
	            max = 1; // Default value in case of an exception
	            logger.error("An exception occurred while retrieving the last number index: {}", e.getMessage(), e);
	        }
			elements.forEach(element -> {
				if (element.getTypeTransaction().equals("040") && element.getTypeRetrait().equals("001")
						&& element.getAccepted().equals("2")) {
					if (element.getCodeBankAcquereur().equals("007")) {
						RetraitAlpRejectExtra(element);
						element.setFlag(REJECTDONE);
						element.setBkmGeneration("done");
						upUpdated.add(element);
					} else {
						RetraitRejectExtra(element);
						element.setFlag(REJECTDONE);
						element.setBkmGeneration("done");
						upUpdated.add(element);
					}
				} else if (element.getTypeTransaction().equals("040") && element.getTypeRetrait().equals("002")
						&& element.getAccepted().equals("2")) {
					ConsultationRejectExtra(element);
					element.setFlag(REJECTDONE);
					element.setBkmGeneration("done");
					upUpdated.add(element);
				}

			});
			uAP040INRFransaBankRepository.saveAll(upUpdated);
		} catch (Exception e) {
			String stackTrace = Throwables.getStackTraceAsString(e);
			logger.info("Exception is=>{}", stackTrace);
			if (stackTrace.length() > 4000)
				stackTrace = stackTrace.substring(0, 3999);
			String error = e.getMessage() == null ? e.toString() : e.getMessage();
			if (error.length() > 255)
				error = error.substring(0, 254);
			batchRepo.updateStatusAndErrorBatch("CRAUAP40IN", 2, error, new Date(), stackTrace);
		}
	}

	public void updateRejectionNOTOK(String flag) {
		FileRequest.print("" + flag, FileRequest.getLineNumber());
		List<UAP040INR> elements = uAP040INRFransaBankRepository.findByFlagAndAcceptedAndDateRegAfterToday(flag,
				"NOT OK");
		FileRequest.print("" + elements.size(), FileRequest.getLineNumber());
		List<UAP040INR> upUpdated = new ArrayList<UAP040INR>();
		try {
	        try {
	            String lastNumIndex = bkmvtiFransaBankRepository.getLastNumIndex();
	            max = (lastNumIndex == null) ? 1 : Integer.valueOf(lastNumIndex);
	        } catch (Exception e) {
	            max = 1; // Default value in case of an exception
	            logger.error("An exception occurred while retrieving the last number index: {}", e.getMessage(), e);
	        }

			elements.forEach(element -> {
				if (element.getTypeTransaction().equals("040") && element.getTypeRetrait().equals("001")
						&& element.getAccepted().equals("NOT OK")) {
					element.setMontantCommissionTTC(Integer.parseInt(element.getFileMontantcommission()));
					element.setMontantTransaction(Integer.parseInt(element.getMontantRetrait()));

					if (element.getTagRetrait().equals("1")) {
						RejectRetraitNotValid(element);
					} else if (element.getTagRetrait().equals("2")) {
						RejectAlgeriePosteNotValid(element);
					}
					element.setBkmGeneration("done");
					element.setFlag(REJECTDONE);
					upUpdated.add(element);

				} else if (element.getTypeTransaction().equals("040") && element.getTypeRetrait().equals("002")
						&& element.getAccepted().equals("NOT OK")) {
					// somme cro row have the corrrect commission but the wrong amount to compensate
					// for
					element.setMontantCommissionTTC(Integer.parseInt(element.getFileMontantAcompenser()));
					element.setMontantTransaction(Integer.parseInt(element.getMontantRetrait()));

					RejectConsultationSoldeNotValid(element);
					element.setBkmGeneration("done");
					element.setFlag(REJECTDONE);
					upUpdated.add(element);

				}
			});

			uAP040INRFransaBankRepository.saveAll(upUpdated);

			BatchesFC batch = batchRepo.findByKey("CRAUAP40IN").get();
			batchRepo.updateFinishBatch("CRAUAP40IN", 1, new Date());

		} catch (Exception e) {
			String stackTrace = Throwables.getStackTraceAsString(e);
			logger.info("Exception is=>{}", stackTrace);
			if (stackTrace.length() > 4000)
				stackTrace = stackTrace.substring(0, 3999);
			String error = e.getMessage() == null ? e.toString() : e.getMessage();
			if (error.length() > 255)
				error = error.substring(0, 254);

			batchRepo.updateStatusAndErrorBatch("CRAUAP40IN", 2, error, new Date(), stackTrace);

		}
	}

	@GetMapping("validateModified/{forcemodify}")
	public void validateModified(@PathVariable String forcemodify) {
		if (forcemodify.equals("force")) {
			try {
				validateALL();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		updateRejectionExtra(REJECTMODIFIED);
		updateRejectionNOTOK(REJECTMODIFIED);
		ccc050.validateModified(forcemodify);
		OpeningDay day = odr.findByStatus040("doneSort").get();
		day.setStatus040("doneCro");
		odr.save(day);
		batchRepo.updateFinishBatch("CRAUAP140IN", 1, new Date());

	}

	// ****************************Read reject CRA END***************************//

	public List<BkmvtiFransaBank> TestSigneWithoutDayOperation(UAP040INR uap, int methode, MvbkConf mvk,
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

	public BkmvtiFransaBank TestAccountAndSigneWithoutSayOperation(UAP040INR uap, int methode, int index2,
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


		HashMap<String, BigDecimal> data = new HashMap<String, BigDecimal>();
		BigDecimal mntRet= new BigDecimal( uap.getMontantTransaction());
		BigDecimal commConf= new BigDecimal( uap.getMontantCommissionTTC());
  
		data.put("MntRet", mntRet);
		data.put("commConf", commConf);
        BigDecimal roundedValue = fIPService.evaluateWithElementswithBigDecimal(mvk.getCodeSettlement(), data).divide(new BigDecimal(100));

        DecimalFormat decimalFormat = new DecimalFormat("000000000000000000.00");
        String formattedValue = decimalFormat.format(roundedValue);

        // Replace the decimal point with a comma
        formattedValue = formattedValue.replace('.', ',');

        // Print the formatted value
        bkmvtiFransaBank.setMontant(formattedValue);
		try {

			if ( fIPService.evaluateWithElementswithBigDecimal(mvk.getCodeSettlement(), data).intValue() <= 0) {
				throw new Exception("amount lower than expected");
			}
		} catch (Exception e) {
			return null;
		}
		bkmvtiFransaBank.setCodeDeviceOrigine("208");
		bkmvtiFransaBank.setIdentification(mvk.getIdentification());
		bkmvtiFransaBank.setNumRefTransactions(uap.getNumTransaction());

		bkmvtiFransaBank.setNumRefTransactions(uap.getNumTransaction());
		setSameDataWithoutDayOperation(uap, methode, bkmvtiFransaBank, index2, indexPieceComptable, mvk);

		return bkmvtiFransaBank;

	}

	public BkmvtiFransaBank TestAccountLengthWithoutDayOperation(UAP040INR uap, int lengAccount, String Account,
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

	public BkmvtiFransaBank setSameDataWithoutDayOperation(UAP040INR uap, int methode,
			BkmvtiFransaBank bkmvtiFransaBank, int index2, int indexPieceComptable, MvbkConf mvk) {
		String lib = "";
		bkmvtiFransaBank.setCodeOperation(mvk.getCodeOperation());
		lib = mvk.getLibelle_operation();
		bkmvtiFransaBank.setLibelle(lib + getSpace(40 - lib.length()));

		if (mvk.getLibGenerique() != null && mvk.getLibGenerique().trim() != "") {
			String libgenerique = mvk.getLibGenerique();
			libgenerique = libgenerique.replaceAll("dateTransation", uap.getDateTransaction());
			String aut = uap.getNumAutorisation();
			int lengthAuth = aut.length();
			libgenerique = libgenerique.replaceAll("numAutorisation", aut.substring(lengthAuth - 6));
			bkmvtiFransaBank.setLibelle(libgenerique + getSpace(40 - lib.length()));
		}

		bkmvtiFransaBank.setPieceComptable(uap.getPieceComptableBKM());

		bkmvtiFransaBank.setNumEvenement(uap.getPieceComptableBKM().substring(uap.getPieceComptableBKM().length() - 6,
				uap.getPieceComptableBKM().length()));

		return bkmvtiFransaBank;
	}

	public BkmvtiFransaBank TestCodeAgenceWithoutDayOperation(UAP040INR uap, String codeAgence, MvbkConf mvk,
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

	public BkmvtiFransaBank TestCodeAgence(String codeAgence, MvbkConf mvk, DayOperationFransaBank op,
			BkmvtiFransaBank bkmvtiFransaBank) {
		if (mvk.getCodeAgence() != null) {
			if (mvk.getCodeAgence().equals("00002")) {
				bkmvtiFransaBank.setAgence(mvk.getCodeAgence());
			}
		} else {
			bkmvtiFransaBank.setAgence(op.getCodeAgence());
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

	public String getAmountFormat(float amount) {
		float m = Math.round(amount);
		int a = (int) m;

		String amountFormat = String.format("%018d,%02d", a / 100, a % 100);
		return amountFormat;
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

	public List<BkmvtiFransaBank> getDataExtraUap(int methode, MvbkConf mvk, UAP040INR uap, List<BkmvtiFransaBank> bkms,
			int index) {
		BkmvtiFransaBank bkm = new BkmvtiFransaBank();

		uap.setMontantCommissionTTC(Integer.parseInt(uap.getMontantCommission()));
		uap.setMontantTransaction(Integer.parseInt(uap.getMontantRetrait()));

		bkm.setCodeDevice("208");
		String name = SecurityContextHolder.getContext().getAuthentication().getName();
		Optional<User> user = userRepository.findByUserNameOrUserEmail(name, name);
		if (!user.isPresent()) {
			throw new RuntimeException("Error saving status !");
		}

		bkm.setPieceComptable("DB" + mvk.getCodeOperation() + String.format("%06d", index));

		bkm.setNumEvenement(String.format("%06d", index));

		bkm.setCodeUtilisateur(user.get().getUserName() + getSpace(10 - user.get().getUserName().length()));
		bkm.setCodeService("0000");
		bkm.setSens(mvk.getSigne());
		bkm.setExonerationcommission("O");
		int lengthNumPiece = uap.getNumAutorisation().length();
		bkm.setNumPiece(uap.getNumAutorisation().substring(lengthNumPiece - 11, lengthNumPiece));
		bkm.setTauxChange("1" + getSpace(6));
		bkm.setCalculmouvementInteragence("N");
		bkm.setMouvementAgence("N");
		int lengthNumAuth = uap.getNumAutorisation().length();
		int lengthRefDossier = (uap.getNumTransaction() + uap.getDateTransaction()
				+ uap.getNumAutorisation().substring(lengthNumAuth - 6, lengthNumAuth)).length();
		bkm.setRefDossier(uap.getNumTransaction() + uap.getDateTransaction()
				+ uap.getNumAutorisation().substring(lengthNumAuth - 6, lengthNumAuth)
				+ getSpace(50 - lengthRefDossier));
		int lengthReferanceLettrage = uap.getNumAutorisation().length();

		bkm.setReferanceLettrage(uap.getDateTransaction().substring(6, uap.getDateTransaction().length())
				+ uap.getNumAutorisation().substring(lengthReferanceLettrage - 6, lengthReferanceLettrage));

 
 
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

        // Print the formatted value
        bkm.setMontant(formattedValue);
		
 
		bkm.setCodeDeviceOrigine("208");
		bkm.setIdentification(mvk.getIdentification());
		bkm.setNumRefTransactions(uap.getNumTransaction());

		bkm.setAgence(mvk.getCodeAgence());
		getTransactionDate(uap.getDateTransaction(), bkm);
		getAccountExtra(mvk.getAccount(), bkm, mvk);
		setSameDataExtra(methode, bkm, mvk, uap);
		bkms.add(bkm);
		return bkms;
	}

	public BkmvtiFransaBank setSameDataExtra(int methode, BkmvtiFransaBank bkmvtiFransaBank, MvbkConf mvk,
			UAP040INR uap) {
		String lib = "";
		bkmvtiFransaBank.setCodeOperation(mvk.getCodeOperation());
		lib = mvk.getLibelle_operation();
		bkmvtiFransaBank.setLibelle(lib + getSpace(40 - lib.length()));
		if (mvk.getLibGenerique() != null && mvk.getLibGenerique().trim() != "") {
			String libgenerique = mvk.getLibGenerique();
			libgenerique = libgenerique.replaceAll("dateTransation", uap.getDateTransaction());
			String aut = uap.getNumAutorisation();
			int lengthAuth = aut.length();
			libgenerique = libgenerique.replaceAll("numAutorisation", aut.substring(lengthAuth - 6));
			bkmvtiFransaBank.setLibelle(libgenerique + getSpace(40 - lib.length()));
		}

		return bkmvtiFransaBank;

	}

	public String getAmountFormatExtra(String amount) {

		int a = Integer.parseInt(amount);

		String amountFormat = String.format("%018d,%02d", a / 100, a % 100);
		return amountFormat;
	}

	public BkmvtiFransaBank getAccountExtra(String account, BkmvtiFransaBank bkmvtiFransaBank, MvbkConf mvk) {
		if (account.length() > 18) {

			String credit = account.substring(8, 18);
			String chapitreCompta = account.substring(8, 14);
			bkmvtiFransaBank.setChapitreComptable(chapitreCompta + getZero(6 - chapitreCompta.length()));
			bkmvtiFransaBank.setNumCompte(credit);

			bkmvtiFransaBank.setCleControleCompte(getSpace(2));
			String codeDes = account.substring(3, 8);
			bkmvtiFransaBank.setAgenceDestinatrice(codeDes);
			bkmvtiFransaBank.setAgenceEmettrice(codeDes);
			bkmvtiFransaBank.setCodeAgenceSaisie(codeDes);
			String codeId = account.substring(3, 8);
			bkmvtiFransaBank.setCodeID("S" + codeId);
		}

		else if (account.length() >= 6 && account.length() < 10) {
			bkmvtiFransaBank.setNumCompte(account + getZero(10 - (account.length())));
			bkmvtiFransaBank.setChapitreComptable(account.substring(0, 6) + getZero(6 - account.length()));

			bkmvtiFransaBank.setCleControleCompte(getSpace(2));
			bkmvtiFransaBank.setAgenceDestinatrice(mvk.getCodeAgence());
			bkmvtiFransaBank.setAgenceEmettrice(mvk.getCodeAgence());
			bkmvtiFransaBank.setCodeAgenceSaisie(mvk.getCodeAgence());
			bkmvtiFransaBank.setCodeID("S" + mvk.getCodeAgence());
		}

		else if (account.length() < 6) {
			bkmvtiFransaBank.setNumCompte(account + getZero(10 - (account.length())));
			bkmvtiFransaBank.setChapitreComptable(account + getZero(6 - account.length()));
			bkmvtiFransaBank.setCleControleCompte(getSpace(2));
			bkmvtiFransaBank.setAgenceDestinatrice(mvk.getCodeAgence());
			bkmvtiFransaBank.setAgenceEmettrice(mvk.getCodeAgence());
			bkmvtiFransaBank.setCodeAgenceSaisie(mvk.getCodeAgence());
			bkmvtiFransaBank.setCodeID("S" + mvk.getCodeAgence());
		}

		else if (account.length() == 10) {
			bkmvtiFransaBank.setNumCompte(account);
			bkmvtiFransaBank.setChapitreComptable(account.substring(0, 6));

			bkmvtiFransaBank.setCleControleCompte(getSpace(2));
			bkmvtiFransaBank.setAgenceDestinatrice(mvk.getCodeAgence());
			bkmvtiFransaBank.setAgenceEmettrice(mvk.getCodeAgence());
			bkmvtiFransaBank.setCodeAgenceSaisie(mvk.getCodeAgence());
			bkmvtiFransaBank.setCodeID("S" + mvk.getCodeAgence());
		}

		else {
			bkmvtiFransaBank.setNumCompte(account.substring(0, 10));
			bkmvtiFransaBank.setChapitreComptable(account.substring(0, 6));
			bkmvtiFransaBank.setCleControleCompte(getSpace(2));
			bkmvtiFransaBank.setAgenceEmettrice(mvk.getCodeAgence());
			bkmvtiFransaBank.setCodeAgenceSaisie(mvk.getCodeAgence());
			bkmvtiFransaBank.setCodeID("S" + mvk.getCodeAgence());
		}
		return bkmvtiFransaBank;
	}

	@PutMapping("Updatemotif")
	public void Updatemotif(@RequestBody UAP040IN uap) throws IOException {
		UAP040IN el = uAP040INFransaBankRepository.getOne(uap.getCode());
		el.setMotifRejet(uap.getMotifRejet());
		uAP040INFransaBankRepository.save(el);
	}

	@PutMapping("updateElement")
	public void updateElement(@RequestBody UAP040IN uap) throws IOException {
		if (uap != null && uap.getNumTransaction() != null) {
			uAP040INFransaBankRepository.save(uap);
		}

	}

	public void validateALL() throws IOException {
		List<UAP040IN> elements = uAP040INFransaBankRepository.filterPending();
		if (elements != null && elements.size() > 0) {
			elements.forEach(x -> {
				x.setFlag(REJECTMODIFIED);
			});
			uAP040INFransaBankRepository.saveAll(elements);
		}
	}

	public static String repeat(String str, int count) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < count; i++) {
			sb.append(str);
		}
		return sb.toString();
	}

	@GetMapping("/generateRejectFile")
	public void generateRejectFile() {

		// Get the current date and time
		LocalDateTime currentDateTime = LocalDateTime.now();

		// Define the desired date format
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

		// Format the current date as a string
		time040Ord = currentDateTime.format(formatter);
		time040Ord = time040Ord.substring(0, time040Ord.length() - 3) + "040";

		long sum = 0;

		Optional<OpeningDay> d = odr.findByStatus040("doneSort");
		if (d.isPresent()) {
			orc.copyUap040In();
			batchRepo.updateFinishBatch("TP", 10, new Date());
			batchRepo.updateFinishBatch("SENDLOT", -1, new Date());
			int i = 1;
			i = d.get().getLotIncrementNb();

			int numberOp = 0;
//			String entete="ECRO035000001DZD"+String.format("%04d", numberOp)+"0".repeat(16)+" ".repeat(28);
			List<UAP040INR> data = uAP040INRFransaBankRepository.findByFlag(REJECTPENDING);
			List<UAP040INR> data2 = uAP040INRFransaBankRepository.findByFlag(REJECTNOTACCEPTED);
			List<UAP040INR> data3 = uAP040INRFransaBankRepository.findByFlag(REJECTERROR);
			List<UAP040INR> data4 = uAP040INRFransaBankRepository.findByFlag(REJECTMODIFIED);
			data.addAll(data2);
			data.addAll(data3);
			data.addAll(data4);
			String enteteReel = "ELOT035000" + String.format("%03d", i) + "DZD" + String.format("%04d", data.size())
					+ repeat("0", 16) + repeat(" ", 28) + "\n";
			ArrayList<String> lignes = new ArrayList<String>();
			FileRequest.print(data.size()+"", FileRequest.getLineNumber());
			for (UAP040INR element : data) {

				sum += Long.valueOf(element.getMontantAComponser().replace(".", ""));

				String ligne = "140" + element.getMotifRejet()
						+ (element.getCodeAgence().length() < 5
								? String.format("%5s", element.getCodeAgence()).replace(' ', '0')
								: element.getCodeAgence().substring(0, 5))
						+ String.format("%13s", element.getCode()).replace(' ', '0') + codeBank
						+ (element.getCodeAgence().length() < 5
								? String.format("%5s", element.getCodeAgence()).replace(' ', '0')
								: element.getCodeAgence().substring(0, 5))
						// + "002"// num seqeunce
						+ element.getRio() + repeat(" ", 130);
				lignes.add(ligne);

			}

			writeFile("035.000." + String.format("%03d", i) + ".140.DZD.LOT", enteteReel + String.join("\n", lignes),
					false);

			String Entete = "ORD" + String.format("%03d", i) + "INLOT" + "000" + "001" + "140" + "DZD" + getSpace(41)
					+ "\n";

			writeFile("035." + String.format("%03d", i) + "." + time040Ord + ".ORD", Entete, false);

			d.get().setLotIncrementNb(i);
			if (lignes.size() > 0) {
				CraRejetControl craControl = new CraRejetControl("035.000." + String.format("%03d", i) + ".140.DZD.LOT",
						"140", d.get().getFileIntegration(), sum, lignes.size());

				craRejetControlRepository.save(craControl);

			}

			OpeningDay el = d.get();
			el.setLotIncrementNb(i++);
			odr.save(el);

		}else {
		 FileRequest.print("no reject found",FileRequest.getLineNumber());
		}

		// ccc050.generateRejectFile();
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
			String movingDate = LocalDateTime.now().format(formatter);
			fileName = fileName + "_" + movingDate;
		}
		File file = new File(sourceDirectory, fileName);
		try (FileWriter writer = new FileWriter(file)) {
			writer.write(content);
			FileRequest.print("File created successfully at: " + file.getAbsolutePath(), FileRequest.getLineNumber());
		} catch (IOException e) {
			FileRequest.print("Error while writing the file: " + e.getMessage(), FileRequest.getLineNumber());

		}
	}

	public void copyUap040In() {
		List<UAP040IN> in = uAP040INFransaBankRepository.findByCopiedAndFlag("false", "20");
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
		uAP040INRFransaBankRepository.saveAll(out);
		uAP040INFransaBankRepository.saveAll(in);
	}
}
