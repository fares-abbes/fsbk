package com.mss.backOffice.controller;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.beanutils.PropertyUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.google.common.base.Throwables;
import com.google.gson.Gson;
import com.mss.backOffice.services.PropertyService;
import com.mss.unified.entities.BatchPorteur;
import com.mss.unified.entities.BkmHistory;
import com.mss.unified.entities.BkmvtiCardFransaBank;
import com.mss.unified.entities.BkmvtiCardHisotryFransaBank;
import com.mss.unified.entities.BkmvtiFransaBank;
import com.mss.unified.entities.DayOperationCardFransaBank;
import com.mss.unified.entities.DayOperationCardHistoryFransaBank;
import com.mss.unified.entities.MvbkSettlement;
import com.mss.unified.entities.User;
import com.mss.unified.repositories.BatchPorteurRepository;
import com.mss.unified.repositories.BkmvtiCardFransaBankRepository;
import com.mss.unified.repositories.BkmvtiCardHistoryFransaBankRepository;
import com.mss.unified.repositories.DayOperationCardFransaBankRepository;
import com.mss.unified.repositories.DayOperationCardHistoryFransaBankRepository;
import com.mss.unified.repositories.MvbkSettlementRepository;
import com.mss.unified.repositories.UserRepository;

@RestController
@RequestMapping("bkmCardFees")
public class BKMCardFeesController {
	@Autowired
	private DayOperationCardFransaBankRepository dayOperationCardFransaBankRepository;

	@Autowired
	private DayOperationCardHistoryFransaBankRepository dayOperationCardHistoryFransaBankRepository;
	@Autowired
	private MvbkSettlementRepository mvbkSettlementRepository;
	@Autowired
	private BkmvtiCardFransaBankRepository bkmvtiCardFransaBankRepository;
	@Autowired
	private BatchPorteurRepository batchPorteurRepository;
	@Autowired
	private UserRepository userRepository;
	@Autowired
	private BkmvtiCardHistoryFransaBankRepository bkmvtiCardHistoryFransaBankRepository;
	@Autowired
	private PropertyService propertyService;
	private static final Logger logger = LoggerFactory.getLogger(BKMCardFeesController.class);
	private static final Gson gson = new Gson();
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

	public void writeFile(String fileName, String content, boolean allowtime) {
//		try {
//			moveFileWithTimestamp(propertyService.getNewFile(), propertyService.getOldFile(), fileName);
//		} catch (Exception e1) {
//			// TODO Auto-generated catch block
//			e1.printStackTrace();
//		}
		File sourceDirectory = new File(propertyService.getNewFile());

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
			logger.info("File created successfully at: " + file.getAbsolutePath());

		} catch (IOException e) {
			logger.info("Error while writing the file: " + e.getMessage());

		}
	}

	@GetMapping(value = "generateBkmCard")
	public ResponseEntity<String> generateBkmCard() throws IOException {
		try {
			logger.info("*********begin generateBkmCard*********");

			BatchPorteur batch = batchPorteurRepository.findByKeys("BkmCard").get();

			batch.setBatchStatus(0);
			batch.setBatchDate(new Date());
			batch.setError(null);
			batch.setErrorStackTrace(null);
			batchPorteurRepository.save(batch);

			String name = SecurityContextHolder.getContext().getAuthentication().getName();
			Optional<User> user = userRepository.findByUserNameOrUserEmail(name, name);
			if (!user.isPresent()) {
				throw new RuntimeException("Error saving status !");
			}
			String userName = user.get().getUserName();

//			String userName =user.get().getUserName()+getSpace(10-user.get().getUserName().length());
//			if (userName.length()>10)
//				userName=userName.substring(0, 10);
			/**********************/
			mvbkOppositionGold(userName);
			mvbkOppositionClassic(userName);
			mvbkOppositionVisa(userName);

			mvbkRefabricationGold(userName);
			mvbkRefabricationClassic(userName);
			mvbkRefabricationVisa(userName);

			mvbkAnnulationGold(userName);
			mvbkAnnulationClassic(userName);
			mvbkAnnulationVisa(userName);

			mvbkReeditionPinGlod(userName);
			mvbkReeditionPinClassic(userName);
			mvbkReeditionPinVisa(userName);
			
			mvbkFabricationGold(userName);
			mvbkFabricationClassic(userName);
			mvbkFabricationVisa(userName);

			/**********************/

			List<BkmvtiCardFransaBank> BkmvtiFransaBanks = bkmvtiCardFransaBankRepository.getListByEve();

			StringBuilder str = new StringBuilder("");
			int counter = 0;
			for (BkmvtiCardFransaBank bkm : BkmvtiFransaBanks) {
				if (counter > 0)
					str.append(System.getProperty("line.separator"));

				if (bkm.getAgence() == null) {
					str.append(getSpace(5) + "|");
				} else {
					str.append(bkm.getAgence() + "|");
				}
				if (bkm.getCodeDevice() == null) {
					str.append(getSpace(3) + "|");
				} else {
					str.append(bkm.getCodeDevice() + "|");
				}
				if (bkm.getChapitreComptable() == null) {
					str.append(" " + "|");
				} else {
					str.append(bkm.getChapitreComptable() + "|");
				}
				if (bkm.getNumCompte() == null) {
					str.append(" " + "|");
				} else {
					str.append(bkm.getNumCompte() + "|");
				}
				if (bkm.getSuffixeCompte() == null) {
					str.append(getSpace(2) + "|");
				} else {
					str.append(bkm.getSuffixeCompte() + "|");
				}
				if (bkm.getCodeOperation() == null) {
					str.append(getSpace(3) + "|");
				} else {
					str.append(bkm.getCodeOperation() + "|");
				}
				if (bkm.getNumMouvement() == null) {
					str.append(getSpace(6) + "|");
				} else {
					str.append(bkm.getNumMouvement() + "|");
				}

				if (bkm.getCodeRegroupement() == null) {
					str.append(getSpace(3) + "|");
				} else {
					str.append(bkm.getCodeRegroupement() + "|");
				}
				if (bkm.getCodeUtilisateur() == null) {
					str.append(getSpace(10) + "|");
				} else {
					str.append(bkm.getCodeUtilisateur() + "|");
				}

				if (bkm.getNumEvenement() == null) {
					str.append(getSpace(6) + "|");
				} else {
					str.append(bkm.getNumEvenement() + "|");
				}
				if (bkm.getCleControleCompte() == null) {
					str.append(getSpace(2) + "|");
				} else {
					str.append(bkm.getCleControleCompte() + "|");
				}
				if (bkm.getDateComptable() == null) {
					str.append(getSpace(8) + "|");
				} else {
					str.append(bkm.getDateComptable() + "|");
				}
				if (bkm.getCodeService() == null) {
					str.append(getSpace(4) + "|");
				} else {
					str.append(bkm.getCodeService() + "|");
				}
				if (bkm.getDateValeur() == null) {
					str.append(getSpace(10) + "|");
				} else {
					str.append(bkm.getDateValeur() + "|");
				}
				if (bkm.getMontant() == null) {
					str.append(getSpace(19) + "|");
				} else {
					int lenght = bkm.getMontant().length();
					String Montant = bkm.getMontant().substring(2, lenght);
					str.append(Montant + "|");
				}
				if (bkm.getSens() == null) {
					str.append(getSpace(1) + "|");
				} else {
					str.append(bkm.getSens() + "|");
				}
				if (bkm.getLibelle() == null) {
					str.append(getSpace(30) + "|");
				} else {
					str.append(bkm.getLibelle() + "|");
				}
				if (bkm.getExonerationcommission() == null) {
					str.append(getSpace(1) + "|");
				} else {
					str.append(bkm.getExonerationcommission() + "|");
				}
				if (bkm.getPieceComptable() == null) {
					str.append(getSpace(11) + "|");
				} else {
					str.append(bkm.getPieceComptable() + "|");
				}

				if (bkm.getReferanceLettrage() == null) {
					str.append(getSpace(8) + "|");
				} else {
					str.append(bkm.getReferanceLettrage() + "|");
				}

				if (bkm.getCodeDesaccord1() == null) {
					str.append(getSpace(4) + "|");
				} else {
					str.append(bkm.getCodeDesaccord1() + "|");
				}
				if (bkm.getCodeDesaccord2() == null) {
					str.append(getSpace(4) + "|");
				} else {
					str.append(bkm.getCodeDesaccord2() + "|");
				}
				if (bkm.getCodeDesaccord3() == null) {
					str.append(getSpace(4) + "|");
				} else {
					str.append(bkm.getCodeDesaccord3() + "|");
				}
				if (bkm.getCodeDesaccord4() == null) {
					str.append(getSpace(4) + "|");
				} else {
					str.append(bkm.getCodeDesaccord4() + "|");
				}

				if (bkm.getCodeDesaccord5() == null) {
					str.append(getSpace(4) + "|");
				} else {
					str.append(bkm.getCodeDesaccord5() + "|");
				}

				if (bkm.getCodeUtilisateur_utf() == null) {
					str.append(getSpace(10) + "|");
				} else {
					str.append(bkm.getCodeUtilisateur_utf() + "|");
				}
				if (bkm.getCodeutilisateurAutorise() == null) {
					str.append(getSpace(10) + "|");
				} else {
					str.append(bkm.getCodeutilisateurAutorise() + "|");
				}
				if (bkm.getTauxChange() == null) {
					str.append(getSpace(15) + "|");
				} else {
					str.append(bkm.getTauxChange() + "|");
				}
				if (bkm.getDateIndisponible() == null) {
					str.append(getSpace(8) + "|");
				} else {
					str.append(bkm.getDateIndisponible() + "|");
				}
				if (bkm.getZoneUtilise() == null) {
					str.append(getSpace(1) + "|");
				} else {
					str.append(bkm.getZoneUtilise() + "|");
				}
				if (bkm.getZoneutiliseespecifiquement() == null) {
					str.append(getSpace(12) + "|");
				} else {
					str.append(bkm.getZoneutiliseespecifiquement() + "|");
				}
				if (bkm.getNumCompteRattachement() == null) {
					str.append(getSpace(11) + "|");
				} else {
					str.append(bkm.getNumCompteRattachement() + "|");
				}
				if (bkm.getSufficeCompteRattrachement() == null) {
					str.append(getSpace(2) + "|");
				} else {
					str.append(bkm.getSufficeCompteRattrachement() + "|");
				}

				if (bkm.getZoneutiliseespecifiquement() == null) {
					str.append(getSpace(1) + "|");
				} else {
					str.append(bkm.getZoneutiliseespecifiquement() + "|");
				}
				if (bkm.getCalculmouvementInteragence() == null) {
					str.append(getSpace(1) + "|");
				} else {
					str.append(bkm.getCalculmouvementInteragence() + "|");
				}
				if (bkm.getMouvementAgence() == null) {
					str.append(getSpace(1) + "|");
				} else {
					str.append(bkm.getMouvementAgence() + "|");
				}

				if (bkm.getCodeMAJ() == null) {
					str.append(getSpace(1) + "|");
				} else {
					str.append(bkm.getCodeMAJ() + "|");
				}
				if (bkm.getDateEcheance() == null) {
					str.append(getSpace(8) + "|");
				} else {
					str.append(bkm.getDateEcheance() + "|");
				}
				if (bkm.getCodeAgenceSaisie() == null) {
					str.append(getSpace(5) + "|");
				} else {
					str.append(bkm.getCodeAgenceSaisie() + "|");
				}

				if (bkm.getAgenceEmettrice() == null) {
					str.append(getSpace(5) + "|");
				} else {
					str.append(bkm.getAgenceEmettrice() + "|");
				}

				if (bkm.getAgenceDestinatrice() == null) {
					str.append(getSpace(5) + "|");
				} else {
					str.append(bkm.getAgenceDestinatrice() + "|");
				}

				if (bkm.getCodeDeviceOrigine() == null) {
					str.append(getSpace(3) + "|");
				} else {
					str.append(bkm.getCodeDeviceOrigine() + "|");
				}
				if (bkm.getMontantOrigine() == null) {
					str.append(getSpace(19) + "|");
				} else {
					str.append(bkm.getMontantOrigine() + "|");
				}

				if (bkm.getNumPiece1() == null) {
					str.append(getSpace(11) + "|");
				} else {
					str.append(bkm.getNumPiece1() + "|");
				}
				if (bkm.getCodeID() == null) {
					str.append(getSpace(6) + "|");
				} else {
					str.append(bkm.getCodeID() + "|");
				}

				if (bkm.getNumSequential() == null) {
					str.append(getSpace(6) + "|");
				} else {
					str.append(bkm.getNumSequential() + "|");
				}
				if (bkm.getCodeLangue() == null) {
					str.append(getSpace(3) + "|");
				} else {
					str.append(bkm.getCodeLangue() + "|");
				}
				if (bkm.getLibelleMouvmeent() == null) {
					str.append(getSpace(30) + "|");
				} else {
					str.append(bkm.getLibelleMouvmeent() + "|");
				}

				if (bkm.getCodeModule() == null) {
					str.append(getSpace(3) + "|");
				} else {
					str.append(bkm.getCodeModule() + "|");
				}
				if (bkm.getRefDossier() == null) {
					str.append(getSpace(50) + "|");
				} else {
					str.append(bkm.getRefDossier() + "|");
				}

				if (bkm.getRefAnalytique() == null) {
					str.append(getSpace(25) + "|");
				} else {
					str.append(bkm.getRefAnalytique() + "|");
				}
				if (bkm.getLibelleMouvmeent() == null) {
					str.append(getSpace(25) + "|");
				} else {
					str.append(bkm.getLibelleMouvmeent() + "|");
				}
				if (bkm.getNatureTransaction() == null) {
					str.append(getSpace(6) + "|");
				} else {
					str.append(bkm.getNatureTransaction() + "|");
				}
				if (bkm.getCodeEtat() == null) {
					str.append(getSpace(2) + "|");
				} else {
					str.append(bkm.getCodeEtat() + "|");
				}
				if (bkm.getCodeSchema() == null) {
					str.append(getSpace(10) + "|");
				} else {
					str.append(bkm.getCodeSchema() + "|");
				}
				if (bkm.getCodeEtiquette() == null) {
					str.append(getSpace(10) + "|");
				} else {
					str.append(bkm.getCodeEtiquette() + "|");
				}
				if (bkm.getDestinationAnalytique() == null) {
					str.append(getSpace(30) + "|");
				} else {
					str.append(bkm.getDestinationAnalytique() + "|");
				}
				if (bkm.getCodemouvementFusionne() == null) {
					str.append(getSpace(1) + "|");
				} else {
					str.append(bkm.getCodemouvementFusionne() + "|");
				}

				counter = counter + 1;

			}

			String data = str.toString();

			if (data == null) {

				batchPorteurRepository.updateStatusAndErrorBatch("BkmCard", 2, "ERROR", new Date(), "ERROR");
				logger.info("*********end generateBkmCard*********");
				return ResponseEntity.status(409).body(gson.toJson("Error"));

			} else {
				writeFile("MVT_MON_CARD", data, true);
				saveHistories(BkmvtiFransaBanks);

				batchPorteurRepository.updateFinishBatch("BkmCard", 1, new Date());
				logger.info("*********end generateBkmCard*********");
				return ResponseEntity.status(200).body(gson.toJson(data));
			}

		} catch (Exception exception) {
			String stackTrace = Throwables.getStackTraceAsString(exception);
			logger.info("Exception is=>{}", stackTrace);
			if (stackTrace.length() > 4000)
				stackTrace = stackTrace.substring(0, 3999);
			String error = exception.getMessage() == null ? exception.toString() : exception.getMessage();
			if (error.length() > 255)
				error = error.substring(0, 254);

			batchPorteurRepository.updateStatusAndErrorBatch("BkmCard", 2, error, new Date(), stackTrace);
			return null;
		}

	}

	// ***************Opposition Gold**********//
	private void mvbkOppositionGold(String name) {
		logger.info("begin mvbkOppositionGold");
		List<DayOperationCardFransaBank> dayOperationFransaBanks = dayOperationCardFransaBankRepository
				.getListOppositionGold();
		List<BkmvtiCardFransaBank> BkmvtiFransaBanks = new ArrayList<BkmvtiCardFransaBank>();
		List<MvbkSettlement> allMvbkSettelemnts = mvbkSettlementRepository.findByIdentificationOppositionGold();

		savebkm(dayOperationFransaBanks, allMvbkSettelemnts, BkmvtiFransaBanks, name, "opp");

		logger.info("end mvbkOppositionGold =>{}", BkmvtiFransaBanks.size());

	}

	// ***************Opposition classic**********//
	private void mvbkOppositionClassic(String name) {
		logger.info("begin mvbkOppositionClassic");
		List<DayOperationCardFransaBank> dayOperationFransaBanks = dayOperationCardFransaBankRepository
				.getListOppositionClassic();
		List<BkmvtiCardFransaBank> BkmvtiFransaBanks = new ArrayList<BkmvtiCardFransaBank>();
		List<MvbkSettlement> allMvbkSettelemnts = mvbkSettlementRepository.findByIdentificationOppositionClassic();

		savebkm(dayOperationFransaBanks, allMvbkSettelemnts, BkmvtiFransaBanks, name, "opp");

		logger.info("end mvbkOppositionClassic =>{}", BkmvtiFransaBanks.size());

	}
	
	// ***************Opposition Visa**********//
		private void mvbkOppositionVisa(String name) {
			logger.info("begin mvbkOppositionClassic");
			List<DayOperationCardFransaBank> dayOperationFransaBanks = dayOperationCardFransaBankRepository
					.getListOppositionVisa();
			List<BkmvtiCardFransaBank> BkmvtiFransaBanks = new ArrayList<BkmvtiCardFransaBank>();
			List<MvbkSettlement> allMvbkSettelemnts = mvbkSettlementRepository.findByIdentificationOppositionVisa();

			savebkm(dayOperationFransaBanks, allMvbkSettelemnts, BkmvtiFransaBanks, name, "opp");

			logger.info("end mvbkOppositionVisa =>{}", BkmvtiFransaBanks.size());

		}

	// ***************Refabrication Gold**********//
	private void mvbkRefabricationGold(String name) {
		logger.info("begin mvbkRefabricationGold");
		List<DayOperationCardFransaBank> dayOperationFransaBanks = dayOperationCardFransaBankRepository
				.getListRefabricationGold();
		List<BkmvtiCardFransaBank> BkmvtiFransaBanks = new ArrayList<BkmvtiCardFransaBank>();
		List<MvbkSettlement> allMvbkSettelemnts = mvbkSettlementRepository.findByIdentificationRefabricationGold();

		savebkm(dayOperationFransaBanks, allMvbkSettelemnts, BkmvtiFransaBanks, name, "refabrication");

		logger.info("end mvbkRefabricationGold =>{}", BkmvtiFransaBanks.size());

	}

	// ***************Refabrication Classic**********//
	private void mvbkRefabricationClassic(String name) {
		logger.info("begin mvbkRefabricationClassic");
		List<DayOperationCardFransaBank> dayOperationFransaBanks = dayOperationCardFransaBankRepository
				.getListRefabricationClassic();
		List<BkmvtiCardFransaBank> BkmvtiFransaBanks = new ArrayList<BkmvtiCardFransaBank>();
		List<MvbkSettlement> allMvbkSettelemnts = mvbkSettlementRepository.findByIdentificationRefabricationClassic();

		savebkm(dayOperationFransaBanks, allMvbkSettelemnts, BkmvtiFransaBanks, name, "refabrication");

		logger.info("end mvbkRefabricationClassic =>{}", BkmvtiFransaBanks.size());

	}
	
	// ***************Refabrication Visa**********//
		private void mvbkRefabricationVisa(String name) {
			logger.info("begin mvbkRefabricationVisa");
			List<DayOperationCardFransaBank> dayOperationFransaBanks = dayOperationCardFransaBankRepository
					.getListRefabricationVisa();
			List<BkmvtiCardFransaBank> BkmvtiFransaBanks = new ArrayList<BkmvtiCardFransaBank>();
			List<MvbkSettlement> allMvbkSettelemnts = mvbkSettlementRepository.findByIdentificationRefabricationVisa();

			savebkm(dayOperationFransaBanks, allMvbkSettelemnts, BkmvtiFransaBanks, name, "refabrication");

			logger.info("end mvbkRefabricationVisa =>{}", BkmvtiFransaBanks.size());

		}

	// ***************Fabrication Gold**********//
	private void mvbkFabricationGold(String name) {
		logger.info("begin mvbkFabricationGold");
		List<DayOperationCardFransaBank> dayOperationFransaBanks = dayOperationCardFransaBankRepository
				.getListFabricationGold();
		List<BkmvtiCardFransaBank> BkmvtiFransaBanks = new ArrayList<BkmvtiCardFransaBank>();
		List<MvbkSettlement> allMvbkSettelemnts = mvbkSettlementRepository.findByIdentificationFabricationGold();

		savebkm(dayOperationFransaBanks, allMvbkSettelemnts, BkmvtiFransaBanks, name, "fabrication");

		logger.info("end mvbkFabricationGold =>{}", BkmvtiFransaBanks.size());

	}

	// ***************Fabrication Classic**********//
	private void mvbkFabricationClassic(String name) {
		logger.info("begin mvbkFabricationclassic");
		List<DayOperationCardFransaBank> dayOperationFransaBanks = dayOperationCardFransaBankRepository
				.getListFabricationClassic();
		List<BkmvtiCardFransaBank> BkmvtiFransaBanks = new ArrayList<BkmvtiCardFransaBank>();
		List<MvbkSettlement> allMvbkSettelemnts = mvbkSettlementRepository.findByIdentificationFabricationClassic();

		savebkm(dayOperationFransaBanks, allMvbkSettelemnts, BkmvtiFransaBanks, name, "fabrication");

		logger.info("end mvbkFabricationclassic =>{}", BkmvtiFransaBanks.size());

	}
	
	// ***************Fabrication Visa**********//
		private void mvbkFabricationVisa(String name) {
			logger.info("begin mvbkFabricationVisa");
			List<DayOperationCardFransaBank> dayOperationFransaBanks = dayOperationCardFransaBankRepository
					.getListFabricationVisa();
			List<BkmvtiCardFransaBank> BkmvtiFransaBanks = new ArrayList<BkmvtiCardFransaBank>();
			List<MvbkSettlement> allMvbkSettelemnts = mvbkSettlementRepository.findByIdentificationFabricationVisa();

			savebkm(dayOperationFransaBanks, allMvbkSettelemnts, BkmvtiFransaBanks, name, "fabrication");

			logger.info("end mvbkFabricationVisa =>{}", BkmvtiFransaBanks.size());

		}


	// ***************reedition pin gold**********//
	private void mvbkReeditionPinGlod(String name) {
		logger.info("begin mvbkReeditionPinGlod");
		List<DayOperationCardFransaBank> dayOperationFransaBanks = dayOperationCardFransaBankRepository
				.getListReedtionPinGold();
		List<BkmvtiCardFransaBank> BkmvtiFransaBanks = new ArrayList<BkmvtiCardFransaBank>();
		List<MvbkSettlement> allMvbkSettelemnts = mvbkSettlementRepository.findByIdentificationReeditionPinGold();

		savebkm(dayOperationFransaBanks, allMvbkSettelemnts, BkmvtiFransaBanks, name, "reedition");

		logger.info("end mvbkReeditionPinGlod =>{}", BkmvtiFransaBanks.size());

	}

	// ***************reedition pin classic**********//
	private void mvbkReeditionPinClassic(String name) {
		logger.info("begin mvbkReeditionPinClassic");
		List<DayOperationCardFransaBank> dayOperationFransaBanks = dayOperationCardFransaBankRepository
				.getListReedtionPinClassic();
		List<BkmvtiCardFransaBank> BkmvtiFransaBanks = new ArrayList<BkmvtiCardFransaBank>();
		List<MvbkSettlement> allMvbkSettelemnts = mvbkSettlementRepository.findByIdentificationReeditionPinClassic();

		savebkm(dayOperationFransaBanks, allMvbkSettelemnts, BkmvtiFransaBanks, name, "reedition");

		logger.info("end mvbkReeditionPinClassic =>{}", BkmvtiFransaBanks.size());

	}
	
	// ***************reedition pin visa**********//
		private void mvbkReeditionPinVisa(String name) {
			logger.info("begin mvbkReeditionPinVisa");
			List<DayOperationCardFransaBank> dayOperationFransaBanks = dayOperationCardFransaBankRepository
					.getListReedtionPinVisa();
			List<BkmvtiCardFransaBank> BkmvtiFransaBanks = new ArrayList<BkmvtiCardFransaBank>();
			List<MvbkSettlement> allMvbkSettelemnts = mvbkSettlementRepository.findByIdentificationReeditionPinVisa();

			savebkm(dayOperationFransaBanks, allMvbkSettelemnts, BkmvtiFransaBanks, name, "reedition");

			logger.info("end mvbkReeditionPinVisa =>{}", BkmvtiFransaBanks.size());

		}

	// ***************annulation gold**********//
	private void mvbkAnnulationGold(String name) {
		logger.info("begin mvbkAnnulationGold");
		List<DayOperationCardFransaBank> dayOperationFransaBanks = dayOperationCardFransaBankRepository
				.getListAnnulationGold();
		List<BkmvtiCardFransaBank> BkmvtiFransaBanks = new ArrayList<BkmvtiCardFransaBank>();
		List<MvbkSettlement> allMvbkSettelemnts = mvbkSettlementRepository.findByIdentificationAnnulationGold();

		savebkm(dayOperationFransaBanks, allMvbkSettelemnts, BkmvtiFransaBanks, name, "annulation");

		logger.info("end mvbkAnnulationGold =>{}", BkmvtiFransaBanks.size());

	}

	// ***************annulation classic**********//
	private void mvbkAnnulationClassic(String name) {
		logger.info("begin mvbkAnnulationClassic");
		List<DayOperationCardFransaBank> dayOperationFransaBanks = dayOperationCardFransaBankRepository
				.getListAnnulationClassic();
		List<BkmvtiCardFransaBank> BkmvtiFransaBanks = new ArrayList<BkmvtiCardFransaBank>();
		List<MvbkSettlement> allMvbkSettelemnts = mvbkSettlementRepository.findByIdentificationAnnulationClassic();

		savebkm(dayOperationFransaBanks, allMvbkSettelemnts, BkmvtiFransaBanks, name, "annulation");

		logger.info("end mvbkAnnulationClassic =>{}", BkmvtiFransaBanks.size());

	}

	// ***************annulation visa**********//
	private void mvbkAnnulationVisa(String name) {
		logger.info("begin mvbkAnnulationVisa");
		List<DayOperationCardFransaBank> dayOperationFransaBanks = dayOperationCardFransaBankRepository
				.getListAnnulationVisa();
		List<BkmvtiCardFransaBank> BkmvtiFransaBanks = new ArrayList<BkmvtiCardFransaBank>();
		List<MvbkSettlement> allMvbkSettelemnts = mvbkSettlementRepository.findByIdentificationAnnulationVisa();

		savebkm(dayOperationFransaBanks, allMvbkSettelemnts, BkmvtiFransaBanks, name, "annulation");

		logger.info("end mvbkAnnulationVisa =>{}", BkmvtiFransaBanks.size());

	}

	private List<BkmvtiCardFransaBank> savebkm(List<DayOperationCardFransaBank> dayOperationFransaBanks,
			List<MvbkSettlement> allMvbkSettelemnts, List<BkmvtiCardFransaBank> BkmvtiFransaBanks, String name,
			String operation) {
		List<DayOperationCardFransaBank> daysToSave = new ArrayList<DayOperationCardFransaBank>();
		for (DayOperationCardFransaBank day : dayOperationFransaBanks) {
			int indexPieceComptable = getEveIndex1();

			List<DayOperationCardFransaBank> dayOperationFransaBanksByNumTransaction = dayOperationCardFransaBankRepository
					.getListByNumTransaction(day.getNumtransaction());

			for (MvbkSettlement mvk : allMvbkSettelemnts) {
				int index2 = getEveIndex();
				BkmvtiFransaBanks = TestSigne(operation, mvk, dayOperationFransaBanksByNumTransaction,
						BkmvtiFransaBanks, indexPieceComptable, index2, mvk.getCodeSettlement(), name);

			}

			for (DayOperationCardFransaBank dayCard : dayOperationFransaBanksByNumTransaction) {
				dayCard.setStatus("T");
				daysToSave.add(dayCard);
			}

		}

		bkmvtiCardFransaBankRepository.saveAll(BkmvtiFransaBanks);
		dayOperationCardFransaBankRepository.saveAll(daysToSave);
		return BkmvtiFransaBanks;
	}

	private void saveHistories(List<BkmvtiCardFransaBank> bkmvtiFransaBanks) {
		List<BkmvtiCardHisotryFransaBank> historyBkm = bkmvtiFransaBanks.stream().map(bkmCard -> {
			BkmvtiCardHisotryFransaBank historiqueBkm = new BkmvtiCardHisotryFransaBank();
			try {
				PropertyUtils.copyProperties(historiqueBkm, bkmCard);
			} catch (Exception ex) {
				ex.printStackTrace();
			}
			return historiqueBkm;
		}).collect(Collectors.toList());

		bkmvtiCardHistoryFransaBankRepository.saveAll(historyBkm);

		bkmvtiCardFransaBankRepository.deleteAll(bkmvtiFransaBanks);

		List<DayOperationCardFransaBank> treatedDays = dayOperationCardFransaBankRepository.getListTreatedDays();
		List<DayOperationCardHistoryFransaBank> historyDays = treatedDays.stream().map(dayCard -> {
			DayOperationCardHistoryFransaBank historyDay = new DayOperationCardHistoryFransaBank();
			try {
				PropertyUtils.copyProperties(historyDay, dayCard);
			} catch (Exception ex) {
				ex.printStackTrace();
			}
			return historyDay;
		}).collect(Collectors.toList());

		dayOperationCardHistoryFransaBankRepository.saveAll(historyDays);

		dayOperationCardFransaBankRepository.deleteAll(treatedDays);

	}

	public List<BkmvtiCardFransaBank> TestSigne(String operationCard, MvbkSettlement mvk,
			List<DayOperationCardFransaBank> dayOperationFransaBanksByNumTransaction,
			List<BkmvtiCardFransaBank> BkmvtiFransaBanks, int indexPieceComptable, int index2, String codeOperation,
			String name) {

		switch (mvk.getType()) {
		case "1":
//    	FileRequest.print("enterToMethode", FileRequest.getLineNumber());
			for (DayOperationCardFransaBank op : dayOperationFransaBanksByNumTransaction) {

				if (op.getIdenfication().equals(codeOperation)) {
					BkmvtiCardFransaBank bkmvtiFransaBank = new BkmvtiCardFransaBank();

					// ****Account porteur and signe C OR D****////
					if (AccountSigne(mvk.getAccount(), mvk.getSigne()) == 1) {
//                 	FileRequest.print("enterFirst", FileRequest.getLineNumber());
						bkmvtiFransaBank = TestAccountAndSigne(operationCard, index2, indexPieceComptable, 1,
								op.getCompteCredit(), op.getCompteDebit().length(), mvk.getSigne(), mvk, op,
								op.getCompteDebit(), name);
						BkmvtiFransaBanks.add(bkmvtiFransaBank);

					} else if (AccountSigne(mvk.getAccount(), mvk.getSigne()) == 2) {
//                	FileRequest.print("enterSecond", FileRequest.getLineNumber());
						bkmvtiFransaBank = TestAccountAndSigne(operationCard, index2, indexPieceComptable, 1,
								op.getCompteDebit(), op.getCompteDebit().length(), mvk.getSigne(), mvk, op,
								op.getCompteDebit(), name);
						BkmvtiFransaBanks.add(bkmvtiFransaBank);

					}

					// ****Account not porteur and signe C OR D****///
					else if (AccountSigne(mvk.getAccount(), mvk.getSigne()) == 3) {
//                	FileRequest.print("enterThreart", FileRequest.getLineNumber());
						bkmvtiFransaBank = TestAccountAndSigne(operationCard, index2, indexPieceComptable, 1,
								mvk.getAccount(), mvk.getAccount().length(), mvk.getSigne(), mvk, op, mvk.getAccount(),
								name);

						BkmvtiFransaBanks.add(bkmvtiFransaBank);
					}

				}
			}
			break;
		case "2":

			String code = codeOperation;
			String code1 = code.substring(0, 4);
			String code2 = code.substring(5, 9);
			String operation = code.substring(4, 5);
			float montant = 0;
			// *** same data ***//
			BkmvtiCardFransaBank bkmvtiFransaBank = new BkmvtiCardFransaBank();

			for (DayOperationCardFransaBank op : dayOperationFransaBanksByNumTransaction) {
				if (op.getIdenfication().equals(code1) || op.getIdenfication().equals(code2)) {
					if (operation.equals("+")) {
						montant = (montant + (op.getMontantSettlement()));
					} else if (operation.equals("-")) {
						montant = (float) ((Math.abs((op.getMontantSettlement()) - montant) * 100.0) / 100.0);
					}

					if (AccountSigne(mvk.getAccount(), mvk.getSigne()) == 1) {
						bkmvtiFransaBank = TestAccountAndSigne(operationCard, index2, indexPieceComptable, 1,
								op.getCompteCredit(), op.getCompteDebit().length(), mvk.getSigne(), mvk, op,
								op.getCompteDebit(), name);

						bkmvtiFransaBank.setMontant(getAmountFormat(Math.round(montant)));
					}

					else if (AccountSigne(mvk.getAccount(), mvk.getSigne()) == 2) {
						bkmvtiFransaBank = TestAccountAndSigne(operationCard, index2, indexPieceComptable, 1,
								op.getCompteDebit(), op.getCompteDebit().length(), mvk.getSigne(), mvk, op,
								op.getCompteDebit(), name);

						bkmvtiFransaBank.setMontant(getAmountFormat(Math.round(montant)));
					}

					else if (AccountSigne(mvk.getAccount(), mvk.getSigne()) == 3) {
						bkmvtiFransaBank = TestAccountAndSigne(operationCard, index2, indexPieceComptable, 1,
								mvk.getAccount(), op.getCompteDebit().length(), mvk.getSigne(), mvk, op,
								mvk.getAccount(), name);

						bkmvtiFransaBank.setMontant(getAmountFormat(Math.round(montant)));
					}
				}

			}
			BkmvtiFransaBanks.add(bkmvtiFransaBank);

			break;
		case "3":

			String codeSettlement = codeOperation;
			String codeSettlement1 = codeSettlement.substring(0, 4);
			String codeSettlement2 = codeSettlement.substring(5, 9);
			String codeSettlement3 = codeSettlement.substring(10, 14);
			String operationSettlement = codeSettlement.substring(4, 5);
			String operation2 = codeSettlement.substring(9, 10);
			float montantSettlement = 0;
			BkmvtiCardFransaBank bkmvtiFransaBank2 = new BkmvtiCardFransaBank();

			for (DayOperationCardFransaBank op : dayOperationFransaBanksByNumTransaction) {
				if (op.getIdenfication().equals(codeSettlement1) || op.getIdenfication().equals(codeSettlement2)
						|| op.getIdenfication().equals(codeSettlement3)) {
					if (operationSettlement.equals("+") && operation2.equals("+")) {
						montantSettlement = (montantSettlement + (op.getMontantSettlement()));
					} else if (operationSettlement.equals("-") && operation2.equals("-")) {
						montantSettlement = (float) ((Math.abs((op.getMontantSettlement()) - montantSettlement) * 100.0)
								/ 100.0);
					}

					if (AccountSigne(mvk.getAccount(), mvk.getSigne()) == 1) {
						bkmvtiFransaBank2 = TestAccountAndSigne(operationCard, index2, indexPieceComptable, 2,
								op.getCompteCredit(), op.getCompteDebit().length(), mvk.getSigne(), mvk, op,
								op.getCompteDebit(), name);
						bkmvtiFransaBank2.setMontant(getAmountFormat((Math.round(montantSettlement))));

					}

					else if (AccountSigne(mvk.getAccount(), mvk.getSigne()) == 2) {
						bkmvtiFransaBank2 = TestAccountAndSigne(operationCard, index2, indexPieceComptable, 2,
								op.getCompteDebit(), op.getCompteDebit().length(), mvk.getSigne(), mvk, op,
								op.getCompteDebit(), name);

						bkmvtiFransaBank2.setMontant(getAmountFormat(Math.round(montantSettlement)));
					}

					else if (AccountSigne(mvk.getAccount(), mvk.getSigne()) == 3) {
						bkmvtiFransaBank2 = TestAccountAndSigne(operationCard, index2, indexPieceComptable, 2,
								mvk.getAccount(), op.getCompteDebit().length(), mvk.getSigne(), mvk, op,
								mvk.getAccount(), name);

						bkmvtiFransaBank2.setMontant(getAmountFormat(Math.round(montantSettlement)));

					}
				}

			}
			BkmvtiFransaBanks.add(bkmvtiFransaBank2);

			break;
		default:

		}
		return BkmvtiFransaBanks;
	}

	public String getAmountFormat(float amount) {
		float m = Math.round(amount);
		int a = (int) m;

		String amountFormat = String.format("%018d,%02d", a / 100, a % 100);

		return amountFormat;
	}

	public int AccountSigne(String account, String signe) {
		int test = 0;
		if ((account.equals("PORTEUR") || (account.equals("COMMERCANT"))) && signe.equals("C")) {
			test = 1;
		} else if ((account.equals("PORTEUR") || (account.equals("COMMERCANT"))) && signe.equals("D")) {
			test = 2;
		} else if ((!account.equals("PORTEUR") && !account.equals("COMMERCANT"))
				&& (signe.equals("C") || signe.equals("D"))) {
			test = 3;
		}

		return test;

	}

	public BkmvtiCardFransaBank TestAccountAndSigne(String operation, int index2, int indexPieceComptable, int test,
			String account, int accountDebit, String signe, MvbkSettlement mvk, DayOperationCardFransaBank op,
			String cle, String name) {
		BkmvtiCardFransaBank bkmvtiFransaBank = new BkmvtiCardFransaBank();
		TestAccountLength(account.length(), accountDebit, account, mvk, bkmvtiFransaBank, op, cle);
		bkmvtiFransaBank.setCodeDevice("208");

		bkmvtiFransaBank.setCodeUtilisateur(name + getSpace(10 - name.length()));
		bkmvtiFransaBank.setCodeService("0000");
		bkmvtiFransaBank.setSens(mvk.getSigne());
		bkmvtiFransaBank.setExonerationcommission("O");
		int lengthNumPiece = op.getNumAutorisation().length();
		bkmvtiFransaBank.setNumPiece(op.getNumAutorisation().substring(lengthNumPiece - 11, lengthNumPiece));
		bkmvtiFransaBank.setTauxChange("1" + getSpace(6));
		bkmvtiFransaBank.setCalculmouvementInteragence("N");
		bkmvtiFransaBank.setMouvementAgence("N");
		//// add new fileds //////
		int lengthNumAuth = op.getNumAutorisation().length();
		int lengthRefDossier = (op.getNumRefTransaction() + op.getDateTransaction()
				+ op.getNumAutorisation().substring(lengthNumAuth - 6, lengthNumAuth)).length();
		bkmvtiFransaBank.setRefDossier(op.getNumRefTransaction() + op.getDateTransaction()
				+ op.getNumAutorisation().substring(lengthNumAuth - 6, lengthNumAuth)
				+ getSpace(50 - lengthRefDossier));

		// TestCodeAgence(methode, test, mvk.getCodeAgence(), mvk, op,
		// bkmvtiFransaBank);
		bkmvtiFransaBank.setAgence(op.getCodeAgence());
		getTransactionDate(op.getDateTransaction(), bkmvtiFransaBank);

		int lengthReferanceLettrage = op.getNumAutorisation().length();

		bkmvtiFransaBank.setReferanceLettrage(op.getDateTransaction().substring(6, op.getDateTransaction().length())
				+ op.getNumAutorisation().substring(lengthReferanceLettrage - 6, lengthReferanceLettrage));
		if (mvk.getType().equals("1")) {
			getAmount(op.getMontantSettlement(), bkmvtiFransaBank);
		}

		bkmvtiFransaBank.setCodeDeviceOrigine("208");
		bkmvtiFransaBank.setIdentification(mvk.getIdentification());
		setSameData(operation, bkmvtiFransaBank, index2, indexPieceComptable, op, mvk.getCodeOperation());

		return bkmvtiFransaBank;

	}

	public BkmvtiCardFransaBank getTransactionDate(String TransactionDate, BkmvtiCardFransaBank bkmvtiFransaBank) {
		String year = TransactionDate.substring(0, 4);
		String month = TransactionDate.substring(4, 6);
		String dayy = TransactionDate.substring(6);
		// bkmvtiFransaBank.setDateComptable(dayy + "/" + month + "/" + year);

		LocalDate date = LocalDate.now();
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
		bkmvtiFransaBank.setDateComptable(date.format(formatter));

		bkmvtiFransaBank.setDateValeur(dayy + "/" + month + "/" + year);
		return bkmvtiFransaBank;

	}

	public BkmvtiCardFransaBank getAmount(float amountSettlement, BkmvtiCardFransaBank bkmvtiFransaBank) {
		int amountRound = Math.round(amountSettlement);
		String amountString = String.valueOf(amountRound);
		int AmountLengh1 = amountString.length();
		String amount = "";
		if (AmountLengh1 > 2)
			amount = amountString.substring(0, AmountLengh1 - 2) + "," + amountString.substring(AmountLengh1 - 2);
		else
			amount = "0," + amountString;

		bkmvtiFransaBank.setMontant(getZero(20 - AmountLengh1) + amount);
		return bkmvtiFransaBank;
	}

	public String TestCompteCredit(String compteCredit, DayOperationCardFransaBank op) {
		String codeAgence = "";
		if (op.getCompteCredit().length() < 18) {
			codeAgence = op.getCodeAgence();

		} else {

			codeAgence = op.getCompteCredit().substring(3, 8);

		}
		return codeAgence;

	}

	public BkmvtiCardFransaBank TestCodeAgence(int methode, int test, String codeAgence, MvbkSettlement mvk,
			DayOperationCardFransaBank op, BkmvtiCardFransaBank bkmvtiFransaBank) {
		if (testMethode(methode) == 1) {

			if (test != 3 && test != 4) {
				if (codeAgence == null) {
//            		FileRequest.print("gootonull", FileRequest.getLineNumber());

					if (test == 1) {

						bkmvtiFransaBank.setAgence(TestCompteCredit(op.getCompteCredit(), op));
					} else {
//            		 FileRequest.print("goTo2", FileRequest.getLineNumber());
						bkmvtiFransaBank.setAgence(op.getCodeAgence());

					}
				} else if (mvk.getCodeAgence().equals("00002")) {
//            		FileRequest.print("goTo3", FileRequest.getLineNumber());
					bkmvtiFransaBank.setAgence(mvk.getCodeAgence());
				}

				else if (mvk.getCodeAgence().equals("MERCH")) {
//                	FileRequest.print("goTo4", FileRequest.getLineNumber());
					bkmvtiFransaBank.setAgence(op.getCodeAgence());
				} else if (mvk.getCodeAgence().equals("CARD") || mvk.getCodeAgence().equals("P")) {
//                	FileRequest.print("goTo5", FileRequest.getLineNumber());
					bkmvtiFransaBank.setAgence(op.getNumRIBEmetteur().substring(3, 8));
				}

				else if (mvk.getCodeAgence().equals("C")) {
//                	FileRequest.print("goTo6", FileRequest.getLineNumber());
					bkmvtiFransaBank.setAgence(op.getCodeAgence());

				}

			}

			else if (test == 3) {

//            	FileRequest.print(op.getNumRIBEmetteur()+ " "+op.getNumAutorisation(), FileRequest.getLineNumber());
				String code = op.getNumRIBEmetteur().substring(3, 8);
				bkmvtiFransaBank.setAgence(code);

			} else if (test == 4) {
				String code = op.getIdCommercant().substring(3, 8);
				bkmvtiFransaBank.setAgence(code);
			}

		} else if (testMethode(methode) == 2) {
			if (mvk.getCodeAgence() != null) {
				if (mvk.getCodeAgence().equals("00002")) {
//        		FileRequest.print("enter1", FileRequest.getLineNumber());
					bkmvtiFransaBank.setAgence(mvk.getCodeAgence());
				}
			} else {
				bkmvtiFransaBank.setAgence(op.getCodeAgence());
			}

		}

		return bkmvtiFransaBank;
	}

	public int testMethode(int method) {
		int acq = 0;
		if (method != 3 && method != 4 && method != 8 && method != 11 && method != 14) {
			acq = 1;
		} else if (method != 3 || method != 4 || method != 8 || method != 11 || method != 14) {
			acq = 2;
		}
		return acq;
	}

	public BkmvtiCardFransaBank TestAccountLength(int lengAccount, int lengAccountDebit, String Account,
			MvbkSettlement mvk, BkmvtiCardFransaBank bkmvtiFransaBank, DayOperationCardFransaBank op, String cle) {

		if (lengAccount > 18) {

			String credit = Account.substring(8, 18);
			String chapitreCompta = Account.substring(8, 14);
			bkmvtiFransaBank.setChapitreComptable(chapitreCompta + getZero(6 - chapitreCompta.length()));
			bkmvtiFransaBank.setNumCompte(credit);
			String index = cle.substring(cle.length() - 2, cle.length());
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
			bkmvtiFransaBank.setAgenceDestinatrice(op.getCodeAgence());
			bkmvtiFransaBank.setAgenceEmettrice(op.getCodeAgence());
			bkmvtiFransaBank.setCodeAgenceSaisie(op.getCodeAgence());
			bkmvtiFransaBank.setCodeID("S" + op.getCodeAgence());
		}

		else if (lengAccount < 6) {
			bkmvtiFransaBank.setNumCompte(Account + getZero(10 - (Account.length())));
			bkmvtiFransaBank.setChapitreComptable(Account + getZero(6 - Account.length()));
			bkmvtiFransaBank.setCleControleCompte(getSpace(2));
			bkmvtiFransaBank.setAgenceDestinatrice(op.getCodeAgence());
			bkmvtiFransaBank.setAgenceEmettrice(op.getCodeAgence());
			bkmvtiFransaBank.setCodeAgenceSaisie(op.getCodeAgence());
			bkmvtiFransaBank.setCodeID("S" + op.getCodeAgence());
		}

		else if (lengAccount == 10) {
			bkmvtiFransaBank.setNumCompte(Account);
			bkmvtiFransaBank.setChapitreComptable(Account.substring(0, 6));
			String index = cle.substring(cle.length() - 2, cle.length());
			bkmvtiFransaBank.setCleControleCompte(getSpace(2));
			bkmvtiFransaBank.setAgenceDestinatrice(op.getCodeAgence());
			bkmvtiFransaBank.setAgenceEmettrice(op.getCodeAgence());
			bkmvtiFransaBank.setCodeAgenceSaisie(op.getCodeAgence());
			bkmvtiFransaBank.setCodeID("S" + op.getCodeAgence());
		}

		else {
			bkmvtiFransaBank.setNumCompte(Account.substring(0, 10));
			bkmvtiFransaBank.setChapitreComptable(Account.substring(0, 6));
			bkmvtiFransaBank.setCleControleCompte(getSpace(2));
			bkmvtiFransaBank.setAgenceEmettrice(op.getCodeAgence());
			bkmvtiFransaBank.setCodeAgenceSaisie(op.getCodeAgence());
			bkmvtiFransaBank.setCodeID("S" + op.getCodeAgence());
		}

		return bkmvtiFransaBank;
	}

	public int getIndex(int indextoadd) {

		indextoadd += 1;

		return indextoadd;
	}

	public String getSpace(int count)

	{

		String Space = "";

		for (int i = 0; i < count; i++)

			Space += " ";

		return Space;

	}

	private BkmvtiCardFransaBank setSameData(String operation, BkmvtiCardFransaBank bkmvtiFransaBank, int index2,
			int indexPieceComptable, DayOperationCardFransaBank day, String codeOperation) {
		String lib = "";
		switch (operation) {

		// opposition
		case "opp":
			bkmvtiFransaBank.setPieceComptable("OP" + codeOperation + day.getPieceComptable());
			break;
		case "refabrication":
			bkmvtiFransaBank.setPieceComptable("RF" + codeOperation + day.getPieceComptable());
			break;
		case "fabrication":
			bkmvtiFransaBank.setPieceComptable("FS" + codeOperation + day.getPieceComptable());
			break;
		case "reedition":
			bkmvtiFransaBank.setPieceComptable("RD" + codeOperation + day.getPieceComptable());
			break;
		case "annulation":
			bkmvtiFransaBank.setPieceComptable("AN" + codeOperation + day.getPieceComptable());
			break;
		default:
			System.out.println("nothing");
		}
		bkmvtiFransaBank.setNumEvenement(String.format("%06d", indexPieceComptable));
		bkmvtiFransaBank.setCodeOperation(codeOperation);
		lib = day.getLibelleCommercant();
		bkmvtiFransaBank.setLibelle(lib + getSpace(40 - lib.length()));
		return bkmvtiFransaBank;
	}

	public String getZero(int count)

	{

		String zero = "";

		for (int i = 0; i < count; i++)

			zero += "0";

		return zero;

	}

}
