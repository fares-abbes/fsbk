package com.mss.backOffice.controller;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
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
import com.mss.unified.entities.CraRejetControl;
import com.mss.unified.entities.DayOperationFransaBank;
import com.mss.unified.entities.MvbkConf;
import com.mss.unified.entities.MvbkSettlement;

import com.mss.unified.entities.UAP050IN;
import com.mss.unified.entities.UAP050INNotAccepted;
import com.mss.unified.entities.UAP050INR;
import com.mss.unified.entities.UAP050FransaBank;

import com.mss.unified.entities.UAP050IN;
import com.mss.unified.entities.UAP050INNotAccepted;
import com.mss.unified.entities.UAP051IN;
import com.mss.unified.entities.User;
import com.mss.unified.entities.OpeningDay;
import com.mss.unified.entities.UAP040IN;
import com.mss.unified.entities.UAP040INR;
import com.mss.unified.repositories.BatchesFFCRepository;
import com.mss.unified.repositories.BkmvtiFransaBankRepository;
import com.mss.unified.repositories.CraRejetControlRepository;
import com.mss.unified.repositories.MvbkConfigRepository;
import com.mss.unified.repositories.MvbkSettlementRepository;

import com.mss.unified.repositories.UAP050INFransaBankRepository;
import com.mss.unified.repositories.UAP050INRFransaBankRepository;
import com.mss.unified.repositories.UAP050InNotAcceptedRepository;
import com.mss.unified.repositories.UserRepository;
import com.mss.unified.repositories.OpeningDayRepository;

@RestController
@RequestMapping("CheckCroController050")
public strictfp class CheckCroController050 {
	public static boolean allowMovementfiles;
	@Autowired
	OrshesterController orc;
	@Autowired
	FormulaInterpreterService fIPService;
	@Autowired
	public MvbkConfigRepository mvbkConfigR;
	@Autowired
	OpeningDayRepository odr;
	@Autowired
	UserRepository userRepository;
	private static String codeBank = "035";
	@Autowired
	MvbkSettlementRepository mvbkSettlementRepository;
	@Autowired
	UAP050INFransaBankRepository uAP050INFransaBankRepository;
	@Autowired
	UAP050INRFransaBankRepository uAP050INRFransaBankRepository;
	@Autowired
	BkmvtiFransaBankRepository bkmvtiFransaBankRepository;
	@Autowired
	DownloadFileBc fbc;
	@Autowired
	BatchesFFCRepository batchRepo;
	@Autowired
	PropertyService propertyService;
	@Autowired
	public CheckCroController051 ccc51;
	@Autowired
	public CraRejetControlRepository craRejetControlRepository;
	@Autowired
	public UAP050InNotAcceptedRepository uAP050InNotAcceptedRepository;
	private boolean allaccepted;
	public static int max;
	public static final String REJECTPENDING = "20";
	public static final String REJECTACCEPTED = "21";
	public static final String REJECTNOTACCEPTED = "22";
	public static final String REJECTMODIFIED = "23";
	public static final String REJECTERROR = "24";
	public static final String REJECTDONE = "25";
	private static final Logger logger = LoggerFactory.getLogger(CheckCroController040.class);
	public static HashMap<String, String> rioIn;

	public synchronized int getEveIndex() {
		return (++ExecutorThreadUAPINFileBC.eve);
	}

	public synchronized int getEveIndex1() {
		return (++ExecutorThreadUAPINFileBC.eve1);
	}

	// *************************method*********************//

	@GetMapping("/generateRejectFile")
	public void generateRejectFile() {
		Optional<OpeningDay> d = odr.findByStatus050("doneSort");
		long sum = 0;
		if (d.isPresent()) {
			orc.copyUap050In();
			int i = 1;
			i = d.get().getLotIncrementNb();
			// Get the current date and time
			LocalDateTime currentDateTime = LocalDateTime.now();
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} // Define the desired date format
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
			String time050Ord = currentDateTime.format(formatter);
			time050Ord = time050Ord.substring(0, time050Ord.length() - 3) + "050";

			int numberOp = 0;
//	String entete="ECRO035000001DZD"+String.format("%04d", numberOp)+"0".repeat(16)+" ".repeat(28);
			List<UAP050INR> data = uAP050INRFransaBankRepository.findByFlag(REJECTPENDING);
			List<UAP050INR> data2 = uAP050INRFransaBankRepository.findByFlag(REJECTNOTACCEPTED);
			List<UAP050INR> data3 = uAP050INRFransaBankRepository.findByFlag(REJECTERROR);
			List<UAP050INR> data4 = uAP050INRFransaBankRepository.findByFlag(REJECTMODIFIED);
			data.addAll(data2);
			data.addAll(data3);
			data.addAll(data4);
			String enteteReel = "ELOT035000" + String.format("%03d", i) + "DZD" + String.format("%04d", data.size())
					+ repeat("0", 16) + repeat(" ", 28) + "\n";
			ArrayList<String> lignes = new ArrayList<String>();

			for (UAP050INR element : data) {
				sum += Long.valueOf(element.getMontantAComponser().replace(".", ""));

				String ligne = "150" + element.getMotifRejet()
						+ (element.getNumRIBcommercant() != null ? element.getNumRIBcommercant().substring(3, 8)
								: "00000")
						+ String.format("%13s", element.getCode()).replace(' ', '0') + codeBank
						+ (element.getNumRIBcommercant() != null ? element.getNumRIBcommercant().substring(3, 8)
								: "00000")
						+ element.getRio() + repeat(" ", 130);

				FileRequest.print(ligne, FileRequest.getLineNumber());

				lignes.add(ligne);

			}

			writeFile("035.000." + String.format("%03d", i) + ".150.DZD.LOT", enteteReel + String.join("\n", lignes),
					false);

			String Entete = "ORD" + String.format("%03d", i) + "INLOT" + "000" + "001" + "150" + "DZD" + getSpace(41)
					+ "\n";

			writeFile("035." + String.format("%03d", i) + "." + time050Ord + ".ORD", Entete, false);
			if (lignes.size() > 0) {
				CraRejetControl craControl = new CraRejetControl("035.000." + String.format("%03d", i) + ".150.DZD.LOT",
						"150", d.get().getFileIntegration(), sum, lignes.size());

				craRejetControlRepository.save(craControl);

			}
			batchRepo.updateFinishBatch("TP", 10, new Date());
			batchRepo.updateFinishBatch("SENDLOT", -1, new Date());
			OpeningDay el = d.get();
			el.setLotIncrementNb(i++);
			odr.save(el);

		}

	}

	public void writeFile(String fileName, String content, boolean allowtime) {
//		try {
//			moveFileWithTimestamp(propertyService.getNewFile(), propertyService.getOldFile(), fileName);
//		} catch (Exception e1) {
//			// TODO Auto-generated catch block
//			e1.printStackTrace();
//		}
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

	public static String repeat(String str, int count) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < count; i++) {
			sb.append(str);
		}
		return sb.toString();
	}

	public List<BkmvtiFransaBank> TestSigneWithoutDayOperation(UAP050INR uap, int methode, MvbkConf mvk,
			List<BkmvtiFransaBank> BkmvtiFransaBanks, int indexPieceComptable, int index2, String codeOperation) {

		BkmvtiFransaBank bkmvtiFransaBank = new BkmvtiFransaBank();
		uap.setMontantCommissionTTC(Integer.parseInt(uap.getFileMontantcommission()));
		uap.setMontantTransaction((Integer.parseInt(uap.getMontantRetrait())));

		if (AccountSigne(mvk.getAccount(), mvk.getSigne()) == 1 || AccountSigne(mvk.getAccount(), mvk.getSigne()) == 2
				|| AccountSigne(mvk.getAccount(), mvk.getSigne()) == 3) {
			bkmvtiFransaBank = TestAccountAndSigneWithoutSayOperation(uap, methode, index2, indexPieceComptable, 1,
					mvk.getAccount(), mvk.getSigne(), mvk, mvk.getAccount());
			BkmvtiFransaBanks.add(bkmvtiFransaBank);

		}

		return BkmvtiFransaBanks;
	}

	public BkmvtiFransaBank TestAccountAndSigneWithoutSayOperation(UAP050INR uap, int methode, int index2,
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
  
		data.put("MntTrans", mntRet);
		data.put("commFSBK", commConf);
        BigDecimal roundedValue = fIPService.evaluateWithElementswithBigDecimal(mvk.getCodeSettlement(), data).divide(new BigDecimal(100));

        DecimalFormat decimalFormat = new DecimalFormat("000000000000000000.00");
        String formattedValue = decimalFormat.format(roundedValue);

        // Replace the decimal point with a comma
        formattedValue = formattedValue.replace('.', ',');

        // Print the formatted value
        bkmvtiFransaBank.setMontant(formattedValue);
		try {

			if (Double.valueOf(bkmvtiFransaBank.getMontant().replace(",", ".")) <= 0) {
				throw new Exception("amount lower than expected");
			}
		} catch (Exception e) {
			return null;
		}
		bkmvtiFransaBank.setCodeDeviceOrigine("208");
		bkmvtiFransaBank.setIdentification(mvk.getIdentification());
		bkmvtiFransaBank.setNumRefTransactions(uap.getNumTransaction());

		setSameDataWithoutDayOperation(uap, methode, bkmvtiFransaBank, index2, indexPieceComptable, mvk);

		return bkmvtiFransaBank;

	}

	public BkmvtiFransaBank TestAccountLengthWithoutDayOperation(UAP050INR uap, int lengAccount, String Account,
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

	public BkmvtiFransaBank setSameDataWithoutDayOperation(UAP050INR uap, int methode,
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

	public BkmvtiFransaBank TestCodeAgenceWithoutDayOperation(UAP050INR uap, String codeAgence, MvbkConf mvk,
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

	public List<BkmvtiFransaBank> getDataExtraUap(int methode, MvbkConf mvk, UAP050INR uap, List<BkmvtiFransaBank> bkms,
			int index) {
		uap.setMontantCommissionTTC(Integer.parseInt(uap.getMontantCommission()));
		uap.setMontantTransaction(Integer.parseInt(uap.getMontantRetrait()));

		BkmvtiFransaBank bkm = new BkmvtiFransaBank();

		bkm.setCodeDevice("208");
		String name = SecurityContextHolder.getContext().getAuthentication().getName();
		Optional<User> user = userRepository.findByUserNameOrUserEmail(name, name);
		if (!user.isPresent()) {
			throw new RuntimeException("Error saving status !");
		}

		bkm.setPieceComptable("PA" + mvk.getCodeOperation() + String.format("%06d", index));

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

		bkm.setCodeDeviceOrigine("208");
		bkm.setIdentification(mvk.getIdentification());
		bkm.setNumRefTransactions(uap.getNumTransaction());

		bkm.setAgence(mvk.getCodeAgence());
		getTransactionDate(uap.getDateTransaction(), bkm);
		getAccountExtra(mvk.getAccount(), bkm, mvk);
		setSameDataExtra(methode, bkm, mvk, uap);
 
		
		HashMap<String, BigDecimal> data = new HashMap<String, BigDecimal>();
		BigDecimal mntRet= new BigDecimal( uap.getMontantRetrait());
		BigDecimal commConf= new BigDecimal( uap.getMontantCommission());
  
		data.put("MntTrans", mntRet);
		data.put("commFSBK", commConf);
        BigDecimal roundedValue = fIPService.evaluateWithElementswithBigDecimal(mvk.getCodeSettlement(), data).divide(new BigDecimal(100));

        DecimalFormat decimalFormat = new DecimalFormat("000000000000000000.00");
        String formattedValue = decimalFormat.format(roundedValue);

        // Replace the decimal point with a comma
        formattedValue = formattedValue.replace('.', ',');

        // Print the formatted value
        bkm.setMontant(formattedValue);
        

		bkms.add(bkm);
		return bkms;
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

	public BkmvtiFransaBank setSameDataExtra(int methode, BkmvtiFransaBank bkmvtiFransaBank, MvbkConf mvk,
			UAP050INR uap) {
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

	public void Reject050(UAP050INR uap) {
		List<MvbkSettlement> allMvbkSettelemnts = mvbkSettlementRepository.Reject50inAchatExtra();
		List<MvbkConf> allMvbkSettelemntsC = mvbkConfigR.findByCategorie(551);

		logger.info("allMvbkSettelemnts =>{}", allMvbkSettelemnts.size());

		List<BkmvtiFransaBank> BkmvtiFransaBanks = new ArrayList<BkmvtiFransaBank>();

		int indexPieceComptable = getEveIndex1();
		for (MvbkConf mvk : allMvbkSettelemntsC) {
			int index2 = getEveIndex();
			BkmvtiFransaBanks = TestSigneWithoutDayOperation(uap, 25, mvk, BkmvtiFransaBanks, indexPieceComptable,
					index2, mvk.getCodeSettlement());

		}

		logger.info("size valid050 =>{}", BkmvtiFransaBanks.size());
		if (BkmvtiFransaBanks != null && !BkmvtiFransaBanks.isEmpty()) {
		    // Filter out null elements from the list
		    List<BkmvtiFransaBank> nonNullBkmvtiFransaBanks = BkmvtiFransaBanks.stream()
		            .filter(Objects::nonNull)
		            .collect(Collectors.toList());

		    if (!nonNullBkmvtiFransaBanks.isEmpty()) {
		        bkmvtiFransaBankRepository.saveAll(nonNullBkmvtiFransaBanks);
		    }
		    
		}	}

	public void Reject052(UAP050INR uap) {
		List<MvbkSettlement> allMvbkSettelemnts = mvbkSettlementRepository.Reject50inAchatInternetExtra();
		List<MvbkConf> allMvbkSettelemntsC = mvbkConfigR.findByCategorie(552);

		List<BkmvtiFransaBank> BkmvtiFransaBanks = new ArrayList<BkmvtiFransaBank>();

		int indexPieceComptable = getEveIndex1();
		for (MvbkConf mvk : allMvbkSettelemntsC) {
			int index2 = getEveIndex();
			BkmvtiFransaBanks = TestSigneWithoutDayOperation(uap, 26, mvk, BkmvtiFransaBanks, indexPieceComptable,
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

	public void RejectExceptionnelMerchant052(UAP050INR uap) {
		List<MvbkSettlement> allMvbkSettelemnts = mvbkSettlementRepository.Reject50inAchatInternetExc();
		List<BkmvtiFransaBank> BkmvtiFransaBanks = new ArrayList<BkmvtiFransaBank>();
		List<MvbkConf> allMvbkSettelemntsC = mvbkConfigR.findByCategorie(555);

		int indexPieceComptable = getEveIndex1();
		for (MvbkConf mvk : allMvbkSettelemntsC) {
			int index2 = getEveIndex();
			BkmvtiFransaBanks = TestSigneWithoutDayOperation(uap, 26, mvk, BkmvtiFransaBanks, indexPieceComptable,
					index2, mvk.getCodeSettlement());

		}
		logger.info("size valid052 =>{}", BkmvtiFransaBanks.size());

		if (BkmvtiFransaBanks != null && !BkmvtiFransaBanks.isEmpty()) {
		    // Filter out null elements from the list
		    List<BkmvtiFransaBank> nonNullBkmvtiFransaBanks = BkmvtiFransaBanks.stream()
		            .filter(Objects::nonNull)
		            .collect(Collectors.toList());

		    if (!nonNullBkmvtiFransaBanks.isEmpty()) {
		        bkmvtiFransaBankRepository.saveAll(nonNullBkmvtiFransaBanks);
		    }
		    
		}	}

	public void Reject052AlgeriePoste(UAP050INR uap) {
		List<MvbkConf> allMvbkSettelemntsC = mvbkConfigR.findByCategorie(3552);

		List<BkmvtiFransaBank> BkmvtiFransaBanks = new ArrayList<BkmvtiFransaBank>();
		int indexPieceComptable = getEveIndex1();
		for (MvbkConf mvk : allMvbkSettelemntsC) {
			int index2 = getEveIndex();
			BkmvtiFransaBanks = TestSigneWithoutDayOperation(uap, 26, mvk, BkmvtiFransaBanks, indexPieceComptable,
					index2, mvk.getCodeSettlement());

		}

		logger.info("size valid052 =>{}", BkmvtiFransaBanks.size());

		if (BkmvtiFransaBanks != null && !BkmvtiFransaBanks.isEmpty()) {
		    // Filter out null elements from the list
		    List<BkmvtiFransaBank> nonNullBkmvtiFransaBanks = BkmvtiFransaBanks.stream()
		            .filter(Objects::nonNull)
		            .collect(Collectors.toList());

		    if (!nonNullBkmvtiFransaBanks.isEmpty()) {
		        bkmvtiFransaBankRepository.saveAll(nonNullBkmvtiFransaBanks);
		    }
		    
		}	}

	public void Reject052TPEExtra(UAP050INR uap) {
		List<MvbkConf> allMvbkSettelemntsC = mvbkConfigR.findByCategorie(5551);

		List<BkmvtiFransaBank> bkms = new ArrayList<BkmvtiFransaBank>();
		int indexPieceComptable = ++max;
		for (MvbkConf mvk : allMvbkSettelemntsC) {

			bkms = getDataExtraUap(19, mvk, uap, bkms, indexPieceComptable);

		}

		bkmvtiFransaBankRepository.saveAll(bkms);
	}

	public void Reject052InternetExtra(UAP050INR uap) {
		List<MvbkSettlement> allMvbkSettelemnts = mvbkSettlementRepository.Reject50inAchatExtra();
		List<MvbkConf> allMvbkSettelemntsC = mvbkConfigR.findByCategorie(1552);

		List<BkmvtiFransaBank> bkms = new ArrayList<BkmvtiFransaBank>();
		int indexPieceComptable = ++max;
		for (MvbkConf mvk : allMvbkSettelemntsC) {

			bkms = getDataExtraUap(19, mvk, uap, bkms, indexPieceComptable);

		}

		bkmvtiFransaBankRepository.saveAll(bkms);
	}

	public void RejectALPExtra(UAP050INR uap) {
		List<MvbkConf> allMvbkSettelemntsC = mvbkConfigR.findByCategorie(5552);

		List<BkmvtiFransaBank> bkms = new ArrayList<BkmvtiFransaBank>();
		int indexPieceComptable = ++max;
		for (MvbkConf mvk : allMvbkSettelemntsC) {

			bkms = getDataExtraUap(20, mvk, uap, bkms, indexPieceComptable);

		}

		bkmvtiFransaBankRepository.saveAll(bkms);
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
		ccc51.validateModified(forcemodify);
		OpeningDay day = odr.findByStatus050("doneSort").get();
		day.setStatus050("doneCro");
		odr.save(day);
		batchRepo.updateFinishBatch("CRAUAP150IN", 1, new Date());

	}

	// ******************end REJECT Extra**************************//
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
					}
				}
			} catch (IOException e) {
				// Handle the exception, e.g., log it or throw a custom exception
				e.printStackTrace();
			}
		}

		return fileContents;
	}

	public void forceReject() {
		updateRejectionExtra(REJECTACCEPTED);
		updateRejectionNOTOK(REJECTACCEPTED);
	}

	@GetMapping("/integrateCra")
	public void handelRejectNotOK() {

		Optional<OpeningDay> opd = odr.findByStatus050("doneSort");

		if (opd.isPresent() && uAP050INRFransaBankRepository.findByFlag(REJECTPENDING).size() > 0) {

			List<UAP050INR> newList = uAP050INRFransaBankRepository.findByFlag(REJECTPENDING);
			BatchesFC batch = batchRepo.getOne(54);
			batch.setBatchDate(new Date());
			batch.setBatchStatus(0);
			List<String> filesNames = listAndFilterFiles(batch.getFileLocation(), "150");
			if (filesNames != null && filesNames.size() > 0) {
				// readfile
				rioIn = readFiles(filesNames, batch.getFileLocation());
				allaccepted = true;
				// verif by rio
				newList.forEach(el -> {
					if (rioIn.get(el.getRio()) != null && rioIn.get(el.getRio()).length() > 0) {
						String status = (String) rioIn.get(el.getRio()).substring(rioIn.get(el.getRio()).length() - 3,
								rioIn.get(el.getRio()).length());
						FileRequest.print(status, FileRequest.getLineNumber());
						if (!rioIn.get(el.getRio()).substring(38, 46).equals("00000000")) {
							el.setDateReglement(rioIn.get(el.getRio()).substring(38, 46));
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
				uAP050INRFransaBankRepository.saveAll(newList);
				updateRejectionExtra(REJECTACCEPTED);
				updateRejectionNOTOK(REJECTACCEPTED);

				try {

					FileRequest.print(allowMovementfiles + "", FileRequest.getLineNumber());
					if (allowMovementfiles) {
						for (String file : filesNames) {
							CheckCroController040.moveFilesByStartingName(batch.getFileLocation(),
									propertyService.getCompensationfilePath(), file);
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
			ccc51.handelRejectNotOK();
			if (allaccepted) {
				OpeningDay day = odr.findByStatus050("doneSort").get();
				day.setStatus050("doneCro");
				odr.save(day);
				batch.setBatchStatus(1);
				batch.setBatchEndDate(new Date());
				batchRepo.save(batch);
			} else {
				batch.setBatchStatus(4);
				batch.setBatchEndDate(new Date());
				batchRepo.save(batch);

			}

		} else {
			ccc51.handelRejectNotOK();

		}
	}

	private void saveCraControl(String fileIntegration) {
		List<CraRejetControl> craControlList = craRejetControlRepository.findByProcessingDateAndLotType(fileIntegration,
				"150");

		if (craControlList.size() > 0) {

			List<UapDetailsControl> acceptedUp = uAP050INRFransaBankRepository.getListUAP50AcceptedForControl();
			long sumAcceptedUp = 0;
			for (UapDetailsControl el : acceptedUp) {
				sumAcceptedUp += Long.valueOf(el.getMontantAComponser().replace(".", ""));

			}
//			
			long sumNotAccepted = 0;
			List<UAP050INR> notAccepted = uAP050INRFransaBankRepository.getListUAP50NotAcceptedForControl();
			List<UAP050INNotAccepted> notAcceptedList = new ArrayList<UAP050INNotAccepted>();
			for (UAP050INR el : notAccepted) {
				sumNotAccepted += Long.valueOf(el.getMontantAComponser().replace(".", ""));

				UAP050INNotAccepted notAcceptedUap50 = new UAP050INNotAccepted();
				try {
					PropertyUtils.copyProperties(notAcceptedUap50, el);
					notAcceptedUap50.setControlId(fileIntegration);
				} catch (Exception ex) {
					logger.info("Exception");
					logger.info(Throwables.getStackTraceAsString(ex));
				}
				notAcceptedList.add(notAcceptedUap50);

			}

			for (CraRejetControl craControl : craControlList) {
				logger.info("cra control is found");

				craControl.setSumAccepted(sumAcceptedUp);
				craControl.setNbAccepted(acceptedUp.size());

				craControl.setSumNotAccepted(sumNotAccepted);
				craControl.setNbNotAccepted(notAccepted.size());

				craRejetControlRepository.save(craControl);
			}
			uAP050InNotAcceptedRepository.saveAll(notAcceptedList);
		}
	}

	public void updateRejectionExtra(String flag) {
		List<UAP050INR> elements = uAP050INRFransaBankRepository.findByFlagAndAcceptedAndDateRegAfterToday(flag, "2");
		FileRequest.print(elements.size() + "", FileRequest.getLineNumber());
		List<UAP050INR> upUpdated = new ArrayList<UAP050INR>();
		try {
	        try {
	            String lastNumIndex = bkmvtiFransaBankRepository.getLastNumIndex();
	            max = (lastNumIndex == null) ? 1 : Integer.valueOf(lastNumIndex);
	        } catch (Exception e) {
	            max = 1; // Default value in case of an exception
	            logger.error("An exception occurred while retrieving the last number index: {}", e.getMessage(), e);
	        }

			elements.forEach(element -> {

				if ((element.getTypeTransaction().equals("050") && element.getTypePaiement().equals("04")
						|| element.getTypeTransaction().equals("050") && element.getTypePaiement().equals("03"))
						&& element.getAccepted().equals("2")) {
					if (element.getCodeBankAcquereur().equals("007")) {
						RejectALPExtra(element);

					} else {
						Reject052InternetExtra(element);
					}
					element.setBkmGeneration("done");

					element.setFlag(REJECTDONE);

					upUpdated.add(element);
				} else if (element.getTypeTransaction().equals("050") && element.getTypePaiement().equals("01")
						&& element.getAccepted().equals("2")) {
					Reject052TPEExtra(element);
					element.setBkmGeneration("done");

					element.setFlag(REJECTDONE);
					upUpdated.add(element);
				}

			});
			uAP050INRFransaBankRepository.saveAll(upUpdated);
			BatchesFC batch = batchRepo.findByKey("CRAUAP50IN").get();

			batchRepo.updateFinishBatch("CRAUAP50IN", 1, new Date());

		} catch (Exception e) {
			String stackTrace = Throwables.getStackTraceAsString(e);
			logger.info("Exception is=>{}", stackTrace);
			if (stackTrace.length() > 4000)
				stackTrace = stackTrace.substring(0, 3999);
			String error = e.getMessage() == null ? e.toString() : e.getMessage();
			if (error.length() > 255)
				error = error.substring(0, 254);

			batchRepo.updateStatusAndErrorBatch("CRAUAP50IN", 2, error, new Date(), stackTrace);

		}

	}

	public void updateRejectionNOTOK(String flag) {
		List<UAP050INR> elements = uAP050INRFransaBankRepository.findByFlagAndAcceptedAndDateRegAfterToday(flag,
				"NOT OK");
		List<UAP050INR> upUpdated = new ArrayList<UAP050INR>();
		try {
	        try {
	            String lastNumIndex = bkmvtiFransaBankRepository.getLastNumIndex();
	            max = (lastNumIndex == null) ? 1 : Integer.valueOf(lastNumIndex);
	        } catch (Exception e) {
	            max = 1; // Default value in case of an exception
	            logger.error("An exception occurred while retrieving the last number index: {}", e.getMessage(), e);
	        }

			elements.forEach(element -> {
				if (element.getTypeTransaction().equals("050") && element.getTypePaiement().equals("01")
						&& element.getAccepted().equals("NOT OK")) {
					logger.info("not ok 050 + 01 not ok");

					Reject050(element);
					element.setFlag(REJECTDONE);
					element.setBkmGeneration("done");

					upUpdated.add(element);
				} else if ((element.getTypeTransaction().equals("050") && element.getTypePaiement().equals("04")
						|| element.getTypeTransaction().equals("050") && element.getTypePaiement().equals("03"))
						&& element.getAccepted().equals("NOT OK")) {
					logger.info("not ok 050 + 01 not ok");

					if (element.getTagPaiement().equals("1")) {
						Reject052(element);
					}
					if (element.getTagPaiement().equals("2")) {
						RejectExceptionnelMerchant052(element);
					}
					if (element.getTagPaiement().equals("3")) {
						Reject052AlgeriePoste(element);
					}

					element.setFlag(REJECTDONE);
					element.setBkmGeneration("done");

					upUpdated.add(element);

				}

			});

			uAP050INRFransaBankRepository.saveAll(upUpdated);

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

	@PutMapping("Updatemotif")
	public void Updatemotif(@RequestBody UAP050IN uap) throws IOException {
		UAP050IN el = uAP050INFransaBankRepository.getOne(uap.getCode());
		el.setMotifRejet(uap.getMotifRejet());
		uAP050INFransaBankRepository.save(el);
	}

	@PutMapping("updateElement")
	public void updateElement(@RequestBody UAP050IN uap) throws IOException {
		if (uap != null && uap.getNumTransaction() != null) {
			uAP050INFransaBankRepository.save(uap);
		}

	}

	public void validateALL() throws IOException {
		List<UAP050IN> elements = uAP050INFransaBankRepository.filterPending();
		if (elements != null && elements.size() > 0) {
			elements.forEach(x -> {
				x.setFlag(REJECTMODIFIED);
			});
			uAP050INFransaBankRepository.saveAll(elements);
		}
	}

	// ****************************Read reject CRA END***************************//
	@PutMapping("updateMissMatch")
	public void HandleRejects(@RequestBody UapIn uaps) {
		FileRequest.print("starting", FileRequest.getLineNumber());
		FileRequest.print(uaps.toString(), FileRequest.getLineNumber());

		Optional<OpeningDay> d = odr.findByStatus050("doneSort");

		if (d.isPresent()) {

			if (uaps.getIdRejet() != null && uaps.getIdRejet().size() > 0) {
				List<Integer> uapIdReject = uaps.getIdRejet();
				List<UAP050IN> rejected = uapIdReject.stream().map(element -> {
					UAP050IN uap = uAP050INFransaBankRepository.findById(element).get();
					uap.setFlag(REJECTPENDING);
//					uap.setMotifRejet(uaps.getMotifRExtra());

					return uap;
				}).collect(Collectors.toList());
				uAP050INFransaBankRepository.saveAll(rejected);
			}
			if (uaps.getIdAcceptation() != null && uaps.getIdAcceptation().size() > 0) {
				List<Integer> uapIdAcc = uaps.getIdAcceptation();
				List<UAP050IN> accepted = uapIdAcc.stream().map(element -> {
					UAP050IN uap = uAP050INFransaBankRepository.findById(element).get();
					uap.setFlag("1");
					return uap;
				}).collect(Collectors.toList());
				uAP050INFransaBankRepository.saveAll(accepted);
			}
			if (uaps.getHandleAll() != null && uaps.getHandleAll().equals("accept")) {
				List<UAP050IN> list = uAP050INFransaBankRepository.findByMatchedAndEmptyFlag();
				FileRequest.print(list.toString(), FileRequest.getLineNumber());
				list.forEach(ele -> {
					ele.setFlag("1");
				});
				uAP050INFransaBankRepository.saveAll(list);

			}

			if (uaps.getHandleAll() != null && uaps.getHandleAll().equals("reject")) {
				List<UAP050IN> list = uAP050INFransaBankRepository.findByMatchedAndEmptyFlag();
				FileRequest.print(list.toString(), FileRequest.getLineNumber());

				list.forEach(ele -> {
					ele.setFlag(REJECTPENDING);
					ele.setMotifRejet(uaps.getMotifRExtra());
				});
				uAP050INFransaBankRepository.saveAll(list);

			}
			BatchesFC batch = batchRepo.findByKey("CRAUAP50IN").get();

			copyUap050In();
			int nbnotOK = uAP050INRFransaBankRepository.findByFlagAndAccepted(REJECTPENDING, "NOT OK").size();
			int nbnExtra = uAP050INRFransaBankRepository.findByFlagAndAccepted(REJECTPENDING, "2").size();

			if ((nbnotOK + nbnExtra) > 0) {
				generateRejectFile();
				batchRepo.updateFinishBatch("CRAUAP50IN", 1, new Date());

			} else {
				batchRepo.updateFinishBatch("CRAUAP150IN", 1, new Date());
				batchRepo.updateFinishBatch("CRAUAP50IN", 1, new Date());
			}

		}
	}

	public void copyUap050In() {
		List<UAP050IN> in = uAP050INFransaBankRepository.findByCopiedAndFlag("false", "20");
		List<UAP050INR> out = in.stream().map(developer -> {

			UAP050INR copiedUap = new UAP050INR();
			developer.setCopied("done");

			try {

				PropertyUtils.copyProperties(copiedUap, developer);

			} catch (Exception ex) {
				ex.printStackTrace();
			}
			copiedUap.setBkmGeneration(null);
			return copiedUap;

		}).collect(Collectors.toList());
		uAP050INRFransaBankRepository.saveAll(out);
		uAP050INFransaBankRepository.saveAll(in);
	}

}
