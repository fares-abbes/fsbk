package com.mss.backOffice.controller;

import com.google.gson.Gson;
import com.mss.backOffice.services.PropertyService;
import com.mss.unified.entities.*;
import com.mss.unified.repositories.*;

import org.apache.commons.beanutils.PropertyUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@RestController

@RequestMapping("downloadFile")
public class DownloadFileBc {

	@Autowired
	BkmvtiFransaBankRepository bkmvtiFransaBankRepository;
	@Autowired
	PropertyService propertyService;
	private static final Gson gson = new Gson();
	@Autowired
	BkmHistoryRepository bkmHistoryRepo;
	@Autowired
	DayOperationFransaBankRepository dayOperationRepo;
	@Autowired
	HistoriqueDayFransaBankRepository historiqueRepo;
    @Autowired
    private BkmvtiMPFransaBankRepository bkmvtiMPFransaBankRepository;
    @Autowired
    private BkmvtiMPFransaBankHRepository bkmvtiMPFransaBankHRepository;
	@Autowired
	ExecutorMobileThreads execMobile;
    @Autowired
    private BkmvtiVisaRepository bkmvtiVisaRepository;

	public void moveFileWithTimestamp(String SOURCE_DIRECTORY, String TARGET_DIRECTORY, String fileName) {
		File sourceFile = new File(SOURCE_DIRECTORY + fileName);

		if (sourceFile.exists()) {
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
			String timestamp = LocalDateTime.now().format(formatter);
			String newFileName = fileName + "_" + timestamp;

			Path targetPath = Paths.get(TARGET_DIRECTORY + newFileName);

			try {
				Files.move(sourceFile.toPath(), targetPath);
				System.out.println("File moved successfully to: " + targetPath);
			} catch (IOException e) {
				System.err.println("Error while moving the file: " + e.getMessage());
			}
		} else {
			System.out.println("Source file does not exist: " + sourceFile.getAbsolutePath());
		}
	}

	public void writeFile(String fileName, String content, boolean allowtime) {

		File sourceDirectory = new File(propertyService.getNewFile());

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

	@GetMapping(value = "writeInFileBC")
	public @ResponseBody ResponseEntity<String> writeInFile() {
		try {
			List<BkmvtiFransaBank> BkmvtiFransaBanks = bkmvtiFransaBankRepository.getListByEve();
			FileRequest.print(" " + BkmvtiFransaBanks.size(), FileRequest.getLineNumber());
			StringBuilder str = new StringBuilder("");
			int counter = 0;

			for (BkmvtiFransaBank bkm : BkmvtiFransaBanks) {
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
					str.append(getSpace(40) + "|");
				} else {
					str.append(bkm.getLibelle().substring(0, 40) + "|");
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
				FileRequest.print("no data  for  file", FileRequest.getLineNumber());
				return ResponseEntity.status(409).body(gson.toJson("Error"));
			} else {
				FileRequest.print("generating file", FileRequest.getLineNumber());

				writeFile("MVT_MON", data, true);
				SaveIntoHistorique();
				bkmvtiFransaBankRepository.deleteAll();
				FileRequest.print("deleting and archiving done", FileRequest.getLineNumber());
				deleteDayOperation();

				return ResponseEntity.status(200).body(gson.toJson("OK"));
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			FileRequest.print(ex.getLocalizedMessage(), FileRequest.getLineNumber());
			FileRequest.print(ex.getMessage(), FileRequest.getLineNumber());

			return null;
		}
	}
	@GetMapping(value = "writeInFileBCMP")
	public @ResponseBody ResponseEntity<String> writeInFileMP() {
		try {
			List<BkmvtiMPFransaBank> BkmvtiFransaBanks = bkmvtiMPFransaBankRepository.getListByEve();
			FileRequest.print(" " + BkmvtiFransaBanks.size(), FileRequest.getLineNumber());
			StringBuilder str = new StringBuilder("");
			int counter = 0;

			for (BkmvtiMPFransaBank bkm : BkmvtiFransaBanks) {
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
					str.append(getSpace(40) + "|");
				} else {
					str.append(bkm.getLibelle().substring(0, 40) + "|");
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
				FileRequest.print("no data  for  file", FileRequest.getLineNumber());
				return ResponseEntity.status(409).body(gson.toJson("Error"));
			} else {
				FileRequest.print("generating file", FileRequest.getLineNumber());

				writeFile("MVT_MON", data, true);
				SaveIntoHistoriqueMP();
				bkmvtiMPFransaBankRepository.deleteAll();
				execMobile.archiveDayOperationMP();

				FileRequest.print("deleting and archiving done", FileRequest.getLineNumber());

				return ResponseEntity.status(200).body(gson.toJson("OK"));
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			FileRequest.print(ex.getLocalizedMessage(), FileRequest.getLineNumber());
			FileRequest.print(ex.getMessage(), FileRequest.getLineNumber());

			return null;
		}
	}
	@GetMapping(value = "writeInFileVISA")
	public @ResponseBody ResponseEntity<String> writeInFileVISA() {
		try {
			List<BkmvtiVisa> BkmvtiFransaBanks = bkmvtiVisaRepository.getListByEve();
			FileRequest.print(" " + BkmvtiFransaBanks.size(), FileRequest.getLineNumber());
			StringBuilder str = new StringBuilder("");
			int counter = 0;

			for (BkmvtiVisa bkm : BkmvtiFransaBanks) {
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
					str.append(getSpace(40) + "|");
				} else {
					str.append(bkm.getLibelle().substring(0, 40) + "|");
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
				FileRequest.print("no data  for  file", FileRequest.getLineNumber());
				return ResponseEntity.status(409).body(gson.toJson("Error"));
			} else {
				FileRequest.print("generating file", FileRequest.getLineNumber());

				writeFile("MVT_VISA", data, true);
				//SaveIntoHistoriqueVisa();
				bkmvtiVisaRepository.deleteAll();
				FileRequest.print("deleting and archiving done", FileRequest.getLineNumber());
				//deleteDayOperationViSA();

				return ResponseEntity.status(200).body(gson.toJson("OK"));
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			FileRequest.print(ex.getLocalizedMessage(), FileRequest.getLineNumber());
			FileRequest.print(ex.getMessage(), FileRequest.getLineNumber());

			return null;
		}
	}

	public void deleteDayOperation() {
		// archive day operation
		List<DayOperationFransaBank> dayOperation = dayOperationRepo.findAll();

		List<HistoriqueDayFransaBank> B = dayOperation.stream().map(developer -> {
			HistoriqueDayFransaBank historique = new HistoriqueDayFransaBank();
			try {
				PropertyUtils.copyProperties(historique, developer);
			} catch (Exception ex) {
				ex.printStackTrace();
			}
			return historique;
		}).collect(Collectors.toList());

		historiqueRepo.saveAll(B);
		dayOperationRepo.deleteAll(dayOperation);
	}

	public void SaveIntoHistorique() {
		List<BkmvtiFransaBank> bkms = bkmvtiFransaBankRepository.findAll();
		List<BkmHistory> historyBkm = bkms.stream().map(developer -> {
			BkmHistory historiqueBkm = new BkmHistory();
			try {
				PropertyUtils.copyProperties(historiqueBkm, developer);
			} catch (Exception ex) {
				ex.printStackTrace();
			}
			return historiqueBkm;
		}).collect(Collectors.toList());

		bkmHistoryRepo.saveAll(historyBkm);
		bkmvtiFransaBankRepository.deleteAll();
	}
	public void SaveIntoHistoriqueMP() {
		List<BkmvtiMPFransaBank> bkms = bkmvtiMPFransaBankRepository.findAll();
		List<BkmvtiMPFransaBankH> historyBkm = bkms.stream().map(developer -> {
			BkmvtiMPFransaBankH historiqueBkm = new BkmvtiMPFransaBankH();
			try {
				PropertyUtils.copyProperties(historiqueBkm, developer);
			} catch (Exception ex) {
				ex.printStackTrace();
			}
			return historiqueBkm;
		}).collect(Collectors.toList());

		bkmvtiMPFransaBankHRepository.saveAll(historyBkm);
		bkmvtiFransaBankRepository.deleteAll();
	}

	public String getSpace(int count)

	{

		String Space = "";

		for (int i = 0; i < count; i++)

			Space += " ";

		return Space;

	}

}
