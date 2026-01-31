package com.mss.backOffice.controller;

import com.google.gson.Gson;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mss.backOffice.exception.ExceptionMethod;
import com.mss.backOffice.exception.ResourceNotFoundException;
import com.mss.backOffice.request.FileUAPRequest;
import com.mss.unified.entities.CodeBankBC;
import com.mss.unified.entities.CraControl;
import com.mss.unified.entities.UAP050FransaBank;
import com.mss.unified.entities.UAP050IN;
import com.mss.unified.entities.UAPFransaBank;
import com.mss.unified.entities.OpeningDay;
import com.mss.unified.repositories.CodeBankBCRepository;
import com.mss.unified.repositories.CraControlRepository;
import com.mss.unified.repositories.UAP050FransaBankRepository;
import com.mss.unified.repositories.UAP050INFransaBankRepository;
import com.mss.unified.repositories.UAPFransaBankRepository;
import com.mss.unified.repositories.OpeningDayRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("downloadUAP050File")
public class DownloadFileUAP_050_FC {
	@Autowired
	DownloadFileORD_UAP050 ord050;

	@Autowired
	UAP050FransaBankRepository uAP050FransaBankRepository;
	@Autowired
	CodeBankBCRepository codeBankBCRepository;
	@Autowired
	UAP050INFransaBankRepository uAP050INFransaBankRepository;

	public static Integer eve1 = 1;
	private static final Logger logger = LoggerFactory.getLogger(DownloadFileBc.class);
	@Autowired
	OpeningDayRepository opr;
	private static final Gson gson = new Gson();
	@Autowired
	DownloadFileBc write;
	@Autowired
	private CraControlRepository craControlRepository;

	public void lot050UAP050(String destinationBank, String nbreLigne, String totalAmountFinal,
			List<UAP050FransaBank> UAPFransaBankS, String seqNumber,String fileIntegrationDate) throws ExceptionMethod {
		StringBuilder str = new StringBuilder("");

		String Entete = "ELOT" + destinationBank + "000" + seqNumber + "DZD" + nbreLigne + totalAmountFinal
				+ getSpace(28);
		List<String> Dates = uAP050FransaBankRepository.findDate();
		// String Date = UAPDate.getDatetransaction();
		String Date = "";
		if (Dates.size() >= 1) {
			Date = Dates.get(0);
		} else
			Date = "";
		String fileName = destinationBank + "." + "000" + "." + seqNumber + "." + "050" + "." + "DZD" + "." + "LOT";
		str.append(Entete);
		str.append(System.getProperty("line.separator"));

		for (UAP050FransaBank UAP : UAPFransaBankS) {

			if (UAP.getTypeTransaction() == null) {
				throw new ExceptionMethod("Error");
			} else {
				str.append(UAP.getTypeTransaction());
			}

			if (UAP.getTypePaiement() == null) {
				str.append(getSpace(2));
			} else {
				str.append(UAP.getTypePaiement());
			}

			if (UAP.getReferenceArchivage() == null) {
				str.append(getSpace(18));
			} else {
				str.append(UAP.getReferenceArchivage());
			}
			if (UAP.getIndicateur() == null) {
				str.append(getSpace(1));
			} else {
				str.append(UAP.getIndicateur());
			}

			if (UAP.getNumRIBcommercant() == null) {
				str.append(getSpace(20));
			} else {
				str.append(UAP.getNumRIBcommercant());
			}

			if (UAP.getPrefixeIBAN() == null) {
				str.append(getSpace(4));
			} else {
				str.append(UAP.getPrefixeIBAN());
			}
			if (UAP.getLibelleCommercant() == null) {
				str.append(getSpace(50));
			} else {
				str.append(UAP.getLibelleCommercant());
			}
			if (UAP.getAdresseCommercant() == null) {
				str.append(getSpace(70));
			} else {
				if (UAP.getAdresseCommercant().length() > 70) {
					String adress = UAP.getAdresseCommercant().substring(0, 70);
					str.append(adress);
				} else {
					String adress = StringUtils.rightPad(UAP.getAdresseCommercant(), 70);
					str.append(adress);
				}

			}
			str.append(getSpace(10));

			if (UAP.getNumContratAccepteur() == null) {
				str.append(getSpace(15));
			} else {
				str.append(UAP.getNumContratAccepteur());
			}

			if (UAP.getCodeActivite() == null) {
				str.append(getSpace(6));
			} else {
				str.append(UAP.getCodeActivite());
			}
			if (UAP.getReserved() == null) {
				str.append(getSpace(3));
			} else {
				str.append(UAP.getReserved());
			}
			if (UAP.getCodeBin() == null) {
				str.append(getSpace(6));
			} else {
				str.append(UAP.getCodeBin());
			}
			if (UAP.getTypeCarte() == null) {
				str.append(getSpace(2));
			} else {
				str.append(UAP.getTypeCarte());
			}
			if (UAP.getNumSeq() == null) {
				str.append(getSpace(6));
			} else {
				str.append(UAP.getNumSeq());
			}
			if (UAP.getNumOrdre() == null) {
				str.append(getSpace(1));
			} else {
				str.append(UAP.getNumOrdre());
			}
			if (UAP.getCle() == null) {
				str.append(getSpace(1));
			} else {
				str.append(UAP.getCle());
			}
			if (UAP.getMontantRetrait() == null) {
				str.append(getSpace(15));
			} else {
				str.append(UAP.getMontantRetrait());
			}
			if (UAP.getCodeDebitCommercant() == null) {
				str.append(getSpace(1));
			} else {
				str.append(UAP.getCodeDebitCommercant());
			}
			if (UAP.getMontantAComponser() == null) {
				str.append(getSpace(15));
			} else {
				str.append(UAP.getMontantAComponser());
			}

			if (UAP.getCodeDebitPorteur() == null) {
				str.append(getSpace(1));
			} else {
				str.append(UAP.getCodeDebitPorteur());
			}

			if (UAP.getMontantCommission() == null) {
				str.append(getSpace(7));
			} else {
				str.append(UAP.getMontantCommission());
			}
			if (UAP.getNumTransaction() == null) {
				str.append(getSpace(12));
			} else {
				str.append(UAP.getNumTransaction());
			}
			if (UAP.getDateTransaction() == null) {
				str.append(getSpace(8));
			} else {
				str.append(UAP.getDateTransaction());
			}

			if (UAP.getHeureTransaction() == null) {
				str.append(getSpace(6));
			} else {
				str.append(UAP.getHeureTransaction());
			}

			if (UAP.getIdentifSystem() == null) {
				str.append(getSpace(10));
			} else {
				str.append(UAP.getIdentifSystem());
			}
			if (UAP.getIdentifPointRetrait() == null) {
				str.append(getSpace(10));
			} else {
				str.append(UAP.getIdentifPointRetrait());
			}
			if (UAP.getModeLectureCarte() == null) {
				str.append(getSpace(1));
			} else {
				str.append(UAP.getModeLectureCarte());
			}
			if (UAP.getMethAuthPorteur() == null) {
				str.append(getSpace(1));
			} else {
				str.append(UAP.getMethAuthPorteur());
			}
			if (UAP.getNumAutorisation() == null) {
				str.append(getSpace(6));
			} else {

				str.append(UAP.getNumAutorisation().substring(UAP.getNumAutorisation().length() - 6,
						UAP.getNumAutorisation().length()));

			}
			if (UAP.getDateDebutValiditeCarte() == null) {
				str.append(getSpace(8));
			} else {
				str.append(UAP.getDateDebutValiditeCarte());
			}
			if (UAP.getDateFinValiditeCarte() == null) {
				str.append(getSpace(8));
			} else {
				str.append(UAP.getDateFinValiditeCarte());
			}
			if (UAP.getCryptogramData() == null) {
				str.append(getSpace(1));
			} else {
				str.append(UAP.getCryptogramData());
			}
			if (UAP.getAtc() == null) {
				str.append(getSpace(2));
			} else {
				str.append(UAP.getAtc());
			}
			if (UAP.getTvr() == null) {
				str.append(getSpace(5));
			} else {
				str.append(UAP.getTvr());
			}
			if (UAP.getDateRemise() == null) {
				str.append(getSpace(8));
			} else {
				str.append(UAP.getDateRemise());
			}
			str.append(getSpace(307));
			str.append(System.getProperty("line.separator"));
		}
		
		CraControl craControl = new CraControl(
				fileName,
				"050",
				fileIntegrationDate,
				Long.parseLong(totalAmountFinal),
				Long.parseLong(nbreLigne)
				);
		craControlRepository.save(craControl);
		write.writeFile(fileName, str.toString(), false);

	}

	@GetMapping(value = "writeInFileUAP050BC")
	public @ResponseBody ResponseEntity<FileUAPRequest> writeInUAP050File()
			throws ResourceNotFoundException, ExceptionMethod, InterruptedException {
		CodeBankBC codeBankBC = codeBankBCRepository.findIdentifiant();
		FileUAPRequest fileRequest = new FileUAPRequest();
		String destinationBank = codeBankBC.getIdentifiant();
		OpeningDay op = opr.findForCra();

		List<UAP050FransaBank> UAPFransaBankS = uAP050FransaBankRepository.findReglementByDate(op.getFileIntegration());

		if (UAPFransaBankS.size() == 0) {
			return ResponseEntity.badRequest().body(null);

		}
		int sublistSize = 9900;
		// Divide the list into sublists
		FileRequest.print(" " + UAPFransaBankS.size(), FileRequest.getLineNumber());
		List<List<UAP050FransaBank>> sublists = divideList(UAPFransaBankS, sublistSize);
		FileRequest.print(" " + sublists.size(), FileRequest.getLineNumber());
		// Print the sublists
		for (int i = 0; i < sublists.size(); i++) {
			Thread.sleep(2000);

			BigDecimal totalAmount = new BigDecimal(0.0);
			for (UAP050FransaBank uap040 : sublists.get(i)) {
				try {
					totalAmount = totalAmount.add(new BigDecimal(uap040.getMontantAComponser()));
				} catch (Exception e) {
					FileRequest.print(""+uap040.getCode(), FileRequest.getLineNumber());
				}
			}
			String convertAmount = totalAmount.toString();
			String totalAmountFinal = getZero(16 - convertAmount.length()) + convertAmount;
			ord050.writeInORD050File(String.format("%03d", i + 1));
			lot050UAP050(destinationBank, String.format("%04d", sublists.get(i).size()), totalAmountFinal,
					sublists.get(i), String.format("%03d", i + 1),op.getFileIntegration());
		}

		return ResponseEntity.ok().body(fileRequest);
	}

	private static <T> List<List<T>> divideList(List<T> originalList, int sublistSize) {
		List<List<T>> sublists = new ArrayList<>();

		for (int i = 0; i < originalList.size(); i += sublistSize) {
			int endIndex = Math.min(i + sublistSize, originalList.size());
			sublists.add(originalList.subList(i, endIndex));
		}

		return sublists;
	}

	public static int generateSequenceNumber() {
		return ++eve1;
	}
	////////////////////// DOWNLOAD FILE UAP050 IN
	////////////////////// ***************/////////////////////////////////////

	@GetMapping(value = "writeInFileUAP050INBC")
	public @ResponseBody ResponseEntity<FileUAPRequest> writeInUAP050INFile()
			throws ResourceNotFoundException, ExceptionMethod {
		CodeBankBC codeBankBC = codeBankBCRepository.findIdentifiant();
		FileUAPRequest fileRequest = new FileUAPRequest();
		String destinationBank = codeBankBC.getIdentifiant();
		List<UAP050IN> UAPFransaBankS = uAP050INFransaBankRepository.findAll();
		StringBuilder str = new StringBuilder("");
		Long totalAmount = uAP050INFransaBankRepository.getTotalAmount();
		String nbreOperation = String.valueOf(uAP050INFransaBankRepository.getNmbreOpreration());
		String Entete = "ELOT" + destinationBank + "000" + "006" + "DZD" + nbreOperation + totalAmount + getSpace(28);
		List<String> Dates = uAP050INFransaBankRepository.findDate();
		// String Date = UAPDate.getDatetransaction();
		String Date = Dates.get(0);
		String fileName = destinationBank + "." + "000" + "." + "006" + "." + "050" + "." + "DZD" + "." + "LOT" + "."
				+ Date;
		str.append(Entete);
		str.append(System.getProperty("line.separator"));
		if (UAPFransaBankS.size() == 0) {
			return ResponseEntity.badRequest().body(null);

		}
		for (UAP050IN UAP : UAPFransaBankS) {

			if (UAP.getTypeTransaction() == null) {
				throw new ExceptionMethod("Error");
			} else {
				str.append(UAP.getTypeTransaction());
			}

			if (UAP.getTypePaiement() == null) {
				str.append(getSpace(2));
			} else {
				str.append(UAP.getTypePaiement());
			}

			if (UAP.getReferenceArchivage() == null) {
				str.append(getSpace(18));
			} else {
				str.append(UAP.getReferenceArchivage());
			}
			if (UAP.getIndicateur() == null) {
				str.append(getSpace(1));
			} else {
				str.append(UAP.getIndicateur());
			}
			if (UAP.getNumRIBcommercant() == null) {
				str.append(getSpace(20));
			} else {
				str.append(UAP.getNumRIBcommercant());
			}
			if (UAP.getPrefixeIBAN() == null) {
				str.append(getSpace(4));
			} else {
				str.append(UAP.getPrefixeIBAN());
			}
			if (UAP.getLibelleCommercant() == null) {
				str.append(getSpace(50));
			} else {
				str.append(UAP.getLibelleCommercant());
			}
			if (UAP.getAdresseCommercant() == null) {
				str.append(getSpace(70));
			} else {
				str.append(UAP.getAdresseCommercant());
			}
			if (UAP.getTelephoneCommercant() == null) {
				str.append(getSpace(10));
			} else {
				str.append(UAP.getTelephoneCommercant());
			}

			if (UAP.getNumContratAccepteur() == null) {
				str.append(getSpace(15));
			} else {
				str.append(UAP.getNumContratAccepteur());
			}
			if (UAP.getCodeActivite() == null) {
				str.append(getSpace(6));
			} else {
				str.append(UAP.getCodeActivite());
			}
			if (UAP.getReserved() == null) {
				str.append(getSpace(3));
			} else {
				str.append(UAP.getReserved());
			}
			if (UAP.getCodeBin() == null) {
				str.append(getSpace(6));
			} else {
				str.append(UAP.getCodeBin());
			}
			if (UAP.getTypeCarte() == null) {
				str.append(getSpace(2));
			} else {
				str.append(UAP.getTypeCarte());
			}
			if (UAP.getNumSeq() == null) {
				str.append(getSpace(6));
			} else {
				str.append(UAP.getNumSeq());
			}
			if (UAP.getNumOrdre() == null) {
				str.append(getSpace(1));
			} else {
				str.append(UAP.getNumOrdre());
			}
			if (UAP.getCle() == null) {
				str.append(getSpace(1));
			} else {
				str.append(UAP.getCle());
			}
			if (UAP.getMontantAComponser() == null) {
				str.append(getSpace(15));
			} else {
				str.append(UAP.getMontantAComponser());
			}
			if (UAP.getMontantRetrait() == null) {
				str.append(getSpace(15));
			} else {
				str.append(UAP.getMontantRetrait());
			}
			if (UAP.getCodeDebitPorteur() == null) {
				str.append(getSpace(1));
			} else {
				str.append(UAP.getCodeDebitPorteur());
			}
			if (UAP.getCodeDebitCommercant() == null) {
				str.append(getSpace(1));
			} else {
				str.append(UAP.getCodeDebitCommercant());
			}
			if (UAP.getMontantCommission() == null) {
				str.append(getSpace(7));
			} else {
				str.append(UAP.getMontantCommission());
			}
			if (UAP.getNumTransaction() == null) {
				str.append(getSpace(12));
			} else {
				str.append(UAP.getNumTransaction());
			}
			if (UAP.getDateTransaction() == null) {
				str.append(getSpace(8));
			} else {
				str.append(UAP.getDateTransaction());
			}

			if (UAP.getHeureTransaction() == null) {
				str.append(getSpace(6));
			} else {
				str.append(UAP.getHeureTransaction());
			}

			if (UAP.getIdentifSystem() == null) {
				str.append(getSpace(10));
			} else {
				str.append(UAP.getIdentifSystem());
			}
			if (UAP.getIdentifPointRetrait() == null) {
				str.append(getSpace(10));
			} else {
				str.append(UAP.getIdentifPointRetrait());
			}
			if (UAP.getModeLectureCarte() == null) {
				str.append(getSpace(1));
			} else {
				str.append(UAP.getModeLectureCarte());
			}
			if (UAP.getMethAuthPorteur() == null) {
				str.append(getSpace(1));
			} else {
				str.append(UAP.getMethAuthPorteur());
			}
			if (UAP.getNumAutorisation() == null) {
				str.append(getSpace(6));
			} else {
				str.append(UAP.getNumAutorisation());
			}
			if (UAP.getDateDebutValiditeCarte() == null) {
				str.append(getSpace(8));
			} else {
				str.append(UAP.getDateDebutValiditeCarte());
			}
			if (UAP.getDateFinValiditeCarte() == null) {
				str.append(getSpace(8));
			} else {
				str.append(UAP.getDateFinValiditeCarte());
			}
			if (UAP.getCryptogramData() == null) {
				str.append(getSpace(1));
			} else {
				str.append(UAP.getCryptogramData());
			}
			if (UAP.getAtc() == null) {
				str.append(getSpace(2));
			} else {
				str.append(UAP.getAtc());
			}
			if (UAP.getTvr() == null) {
				str.append(getSpace(5));
			} else {
				str.append(UAP.getTvr());
			}
			if (UAP.getDateRemise() == null) {
				str.append(getSpace(8));
			} else {
				str.append(UAP.getDateRemise());
			}
			str.append(getSpace(307));
			str.append(System.getProperty("line.separator"));
		}
		// String data = str.toString();
		fileRequest.setData(str.toString());
		fileRequest.setNameTitle(fileName);
		write.writeFile(fileName, str.toString(), false);

		/*
		 * if (fileRequest.getData()==null) { return
		 * ResponseEntity.status(409).body(gson.toJson("Error")); return
		 * ResponseEntity.ok().body(fileRequest); } else {
		 * 
		 * return ResponseEntity.status(200).body(gson.toJson(fileRequest.getData())); }
		 */
		return ResponseEntity.ok().body(fileRequest);
	}

	public String getSpace(int count)

	{

		String Space = "";

		for (int i = 0; i < count; i++)

			Space += " ";

		return Space;

	}

	public String getZero(int count)

	{

		String zero = "";

		for (int i = 0; i < count; i++)

			zero += "0";

		return zero;

	}
}
