package com.mss.backOffice.controller;

import com.google.gson.Gson;
import com.mss.backOffice.exception.ExceptionMethod;
import com.mss.backOffice.exception.ResourceNotFoundException;
import com.mss.backOffice.request.FileUAPRequest;
import com.mss.unified.entities.BkmvtiFransaBank;
import com.mss.unified.entities.CodeBankBC;
import com.mss.unified.entities.CraControl;
import com.mss.unified.entities.UAP040IN;
import com.mss.unified.entities.UAPFransaBank;
import com.mss.unified.entities.OpeningDay;
import com.mss.unified.repositories.BkmvtiFransaBankRepository;
import com.mss.unified.repositories.CodeBankBCRepository;
import com.mss.unified.repositories.CraControlRepository;
import com.mss.unified.repositories.UAP040INFransaBankRepository;
import com.mss.unified.repositories.UAPFransaBankRepository;
import com.mss.unified.repositories.OpeningDayRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.Query;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("downloadUAPFile")
public class DownloadFileUAP_040_FC {
	@Autowired
	DownloadFileORD_UAP040 ord040;
	@Autowired
	UAPFransaBankRepository uAPFransaBankRepository;
	@Autowired
	CodeBankBCRepository codeBankBCRepository;
	@Autowired
	UAP040INFransaBankRepository uAP040INFransaBankRepository;
	private static final Gson gson = new Gson();
	public static Integer eve1 = 1;

	@Autowired
	OpeningDayRepository opr;
	@Autowired

	DownloadFileBc write;
	@Autowired
	private CraControlRepository craControlRepository;

	public void lotUAP040(String destinationBank, String nbreLigne, String totalAmountFinal,
			List<UAPFransaBank> UAPFransaBankS, String seqNumber,String fileIntegrationDate) throws ExceptionMethod {
		StringBuilder str = new StringBuilder("");
		String Entete = "ELOT" + destinationBank + "000" + seqNumber + "DZD" + nbreLigne + totalAmountFinal
				+ getSpace(28);
		List<String> Dates = uAPFransaBankRepository.findDate();
		String Date = "";
		if (Dates.size() >= 1) {
			Date = Dates.get(0);
		} else
			Date = "";
		// String Date = UAPDate.getDatetransaction();
		String fileName = destinationBank + "." + "000" + "." + seqNumber + "." + "040" + "." + "DZD" + "." + "LOT";
		str.append(Entete);
		str.append(System.getProperty("line.separator"));
		
		for (UAPFransaBank UAP : UAPFransaBankS) {

			if (UAP.getTypeTransaction() == null) {

				throw new ExceptionMethod("Error");

			} else {
				str.append(UAP.getTypeTransaction());
			}
			if (UAP.getReferenceArchivage() == null) {
				str.append(getSpace(18));
			} else {
				str.append(UAP.getReferenceArchivage());
			}
			if (UAP.getCodeBankAcquereur() == null) {

				str.append(getSpace(3));
			} else {
				str.append(UAP.getCodeBankAcquereur());
			}
			if (UAP.getCodeAgence() == null) {
				str.append(getSpace(5));
			} else {
				str.append(UAP.getCodeAgence());
			}
			if (UAP.getTypeRetrait() == null) {
				str.append(getSpace(3));
			} else {
				str.append(UAP.getTypeRetrait());
			}
			if (UAP.getCodeBin() == null) {
				str.append(getSpace(6));
			} else {
				str.append(UAP.getCodeBin());
			}
			if (UAP.getTypeCarteDebit() == null) {
				str.append(getSpace(2));
			} else {
				str.append(UAP.getTypeCarteDebit());
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
				str.append(1);
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

			if (UAP.getNumtransaction() == null) {
				str.append(getSpace(12));
			} else {
				str.append(UAP.getNumtransaction());
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
			str.append(getSpace(486));
			str.append(System.getProperty("line.separator"));
			
		}
		CraControl craControl = new CraControl(
				fileName,
				"040",
				fileIntegrationDate,
				Long.parseLong(totalAmountFinal),
				Long.parseLong(nbreLigne)
				);
//		craControl.setNbTotalInFile(Long.parseLong(nbreLigne));
//		craControl.setSumInFile(Long.parseLong(totalAmountFinal));
//		craControl.setLotFileName(fileName);
//		craControl.setLotType("040");
//		craControl.set
		craControlRepository.save(craControl);
		write.writeFile(fileName, str.toString(), false);

	}

	@GetMapping(value = "writeInFileUAPBC")
	public @ResponseBody ResponseEntity<String> writeInUAPFile()
			throws ResourceNotFoundException, ExceptionMethod, InterruptedException {
		CodeBankBC codeBankBC = codeBankBCRepository.findIdentifiant();
		String destinationBank = codeBankBC.getIdentifiant();
		OpeningDay op = opr.findForCra();

		List<UAPFransaBank> UAPFransaBankS = uAPFransaBankRepository.findReglementByDate(op.getFileIntegration());

		if (UAPFransaBankS.size() == 0) {
			return ResponseEntity.badRequest().body(null);

		}
		int sublistSize = 9900;
		// Divide the list into sublists
		List<List<UAPFransaBank>> sublists = divideList(UAPFransaBankS, sublistSize);

		// Print the sublists
		for (int i = 0; i < sublists.size(); i++) {
			Thread.sleep(2000);

			BigDecimal totalAmount = new BigDecimal(0.0);
			for (UAPFransaBank uap040 : sublists.get(i)) {
				totalAmount = totalAmount.add(new BigDecimal(uap040.getMontantAComponser()));
			}
			String convertAmount = totalAmount.toString();
			String totalAmountFinal = getZero(16 - convertAmount.length()) + convertAmount;
			ord040.writeInORD040File(String.format("%03d", i + 1));
			lotUAP040(destinationBank, String.format("%04d", sublists.get(i).size()), totalAmountFinal, sublists.get(i),
					String.format("%03d", i + 1),op.getFileIntegration());
		}

		/*
		 * if (fileRequest.getData()==null) { return
		 * ResponseEntity.status(409).body(gson.toJson("Error")); return
		 * ResponseEntity.ok().body(fileRequest); } else {
		 * 
		 * return ResponseEntity.status(200).body(gson.toJson(fileRequest.getData())); }
		 */

		return ResponseEntity.ok().body(gson.toJson("done"));
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

	///////////////////// DOWNLOAD FILE UAP40 IN
	///////////////////// *********/////////////////////////////

	@GetMapping(value = "writeInFileUAPBCIN")
	public @ResponseBody ResponseEntity<FileUAPRequest> writeInUAPINFile()
			throws ResourceNotFoundException, ExceptionMethod {
		CodeBankBC codeBankBC = codeBankBCRepository.findIdentifiant();
		FileUAPRequest fileRequest = new FileUAPRequest();
		String destinationBank = codeBankBC.getIdentifiant();

		List<UAP040IN> UAPFransaBankS = uAP040INFransaBankRepository.findAll();
		StringBuilder str = new StringBuilder("");
		// StringBuilder str1 = new StringBuilder("");
		Long totalAmount = uAP040INFransaBankRepository.getTotalAmountIN();
		String nbreOperation = String.valueOf(uAP040INFransaBankRepository.getNmbreOpreration());
		String Entete = "ELOT" + destinationBank + "000" + "006" + "DZD" + nbreOperation + totalAmount + getSpace(28);
		List<String> Dates = uAP040INFransaBankRepository.findDate();
		String Date = Dates.get(0);
		// String Date = UAPDate.getDatetransaction();
		String fileName = destinationBank + "." + "000" + "." + "006" + "." + "040" + "." + "DZD" + "." + "LOT" + "."
				+ Date;
		str.append(Entete);
		str.append(System.getProperty("line.separator"));
		if (UAPFransaBankS.size() == 0) {
			return ResponseEntity.badRequest().body(null);

		}
		for (UAP040IN UAP : UAPFransaBankS) {

			if (UAP.getTypeTransaction() == null) {
				throw new ExceptionMethod("Error");
			} else {
				str.append(UAP.getTypeTransaction());
			}
			if (UAP.getReferenceArchivage() == null) {
				str.append(getSpace(18));
			} else {
				str.append(UAP.getReferenceArchivage());
			}
			if (UAP.getCodeBankAcquereur() == null) {
				str.append(getSpace(3));
			} else {
				str.append(UAP.getCodeBankAcquereur());
			}
			if (UAP.getCodeAgence() == null) {
				str.append(getSpace(5));
			} else {
				str.append(UAP.getCodeAgence());
			}
			if (UAP.getTypeRetrait() == null) {
				str.append(getSpace(3));
			} else {
				str.append(UAP.getTypeRetrait());
			}
			if (UAP.getCodeBin() == null) {
				str.append(getSpace(6));
			} else {
				str.append(UAP.getCodeBin());
			}
			if (UAP.getTypeCarteDebit() == null) {
				str.append(getSpace(2));
			} else {
				str.append(UAP.getTypeCarteDebit());
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
				str.append(1);
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
			str.append(getSpace(486));
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
}
