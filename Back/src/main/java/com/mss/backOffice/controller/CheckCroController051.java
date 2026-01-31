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
import java.util.stream.Stream;

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
import com.mss.unified.entities.UAP040IN;
import com.mss.unified.entities.UAP040INNotAccepted;
import com.mss.unified.entities.UAP050FransaBank;
import com.mss.unified.entities.UAP050IN;
import com.mss.unified.entities.UAP050INR;
import com.mss.unified.entities.UAP051IN;
import com.mss.unified.entities.UAP051FransaBank;
import com.mss.unified.entities.UAP051IN;
import com.mss.unified.entities.UAP051INNotAccepted;
import com.mss.unified.entities.UAP051INR;
import com.mss.unified.entities.User;
import com.mss.unified.entities.dayOperationReglement;
import com.mss.unified.entities.OpeningDay;
import com.mss.unified.repositories.OpeningDayRepository;
import com.mss.unified.repositories.BatchesFFCRepository;
import com.mss.unified.repositories.BkmvtiFransaBankRepository;
import com.mss.unified.repositories.CraRejetControlRepository;
import com.mss.unified.repositories.DayOperationFransaBankRepository;
import com.mss.unified.repositories.MvbkConfigRepository;
import com.mss.unified.repositories.MvbkSettlementRepository;
import com.mss.unified.repositories.UAP040INFransaBankRepository;
import com.mss.unified.repositories.UAP040InNotAcceptedRepository;
import com.mss.unified.repositories.UAP051INFransaBankRepository;
import com.mss.unified.repositories.UAP051INRFransaBankRepository;
import com.mss.unified.repositories.UAP051InNotAcceptedRepository;
import com.mss.unified.repositories.UAP051INFransaBankRepository;
import com.mss.unified.repositories.UserRepository;

@RestController
@RequestMapping("CheckCroController051")
public strictfp class CheckCroController051 {
	public static boolean allowMovementfiles;
	@Autowired
	DownloadFileBc fbc;
	@Autowired
	OrshesterController orc;
	@Autowired
	FormulaInterpreterService fIPService;
	@Autowired
	OpeningDayRepository odr;
	@Autowired
	OpeningDayRepository openedDayRepo;
	@Autowired
	UserRepository userRepository;
	private static String codeBank = "035";
	@Autowired
	MvbkSettlementRepository mvbkSettlementRepository;
	@Autowired
	UAP051INFransaBankRepository uAP051INFransaBankRepository;
	@Autowired
	UAP051INRFransaBankRepository uAP051INRFransaBankRepository;
	@Autowired
	BkmvtiFransaBankRepository bkmvtiFransaBankRepository;
	@Autowired
	BatchesFFCRepository batchRepo;
	@Autowired
	PropertyService propertyService;
	public static int max;
	private static final Logger logger = LoggerFactory.getLogger(CheckCroController040.class);

	private boolean allaccepted;
	@Autowired
	public MvbkConfigRepository mvbkConfigR;
	@Autowired
	DayOperationFransaBankRepository dayRepo;
	@Autowired
	public CraRejetControlRepository craRejetControlRepository;
	@Autowired
	public UAP051InNotAcceptedRepository uAP051InNotAcceptedRepository;

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

		
		HashMap<String, BigDecimal> data = new HashMap<String, BigDecimal>();
		BigDecimal mntRet= new BigDecimal( uap.getMontantTransaction());
		BigDecimal commConf= new BigDecimal( uap.getMontantCommissionFSBKTTC());
  
		data.put("MntRemb", mntRet);
		data.put("CommRembFSBK", commConf);
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

		return bkmvtiFransaBank;

	}



	public String getAmountFormat(float amount) {
		float m = Math.round(amount);
		int a = (int) m;

		String amountFormat = String.format("%018d,%02d", a / 100, a % 100);
		return amountFormat;
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

	public BkmvtiFransaBank setSameDataWithoutDayOperation(UAP051IN uap, int methode, BkmvtiFransaBank bkmvtiFransaBank,
			int index2, int indexPieceComptable, MvbkConf mvk) {
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

	private static <T> Stream<T> listToStream(List<T> list) {
		return list.stream();
	}


	public String TestCompteCredit(String compteCredit, DayOperationFransaBank op) {
		String codeAgence = "";
		if (op.getCompteCredit().length() < 18) {
			codeAgence = op.getCodeAgence();

		} else {

			codeAgence = op.getCompteCredit().substring(3, 8);

		}
		return codeAgence;

	}

	public BkmvtiFransaBank TestCodeAgence(int test, String codeAgence, MvbkConf mvk, DayOperationFransaBank op,
			BkmvtiFransaBank bkmvtiFransaBank) {
		if (test != 3 && test != 4) {
			if (codeAgence == null) {
				if (test == 1) {
					bkmvtiFransaBank.setAgence(TestCompteCredit(op.getCompteCredit(), op));
				} else {
					bkmvtiFransaBank.setAgence(op.getCodeAgence());

				}
			} else if (mvk.getCodeAgence().equals("00002")) {
				bkmvtiFransaBank.setAgence(mvk.getCodeAgence());
			}

			else if (mvk.getCodeAgence().equals("MERCH")) {
				bkmvtiFransaBank.setAgence(op.getCodeAgence());
			} else if (mvk.getCodeAgence().equals("CARD")) {
				bkmvtiFransaBank.setAgence(op.getNumRIBEmetteur().substring(3, 8));
			}
		}

		else if (test == 3) {
			String code = op.getNumRIBEmetteur().substring(3, 8);
			bkmvtiFransaBank.setAgence(code);
		} else if (test == 4) {
			String code = op.getIdCommercant().substring(3, 8);
			bkmvtiFransaBank.setAgence(code);
		}
		return bkmvtiFransaBank;
	}

	public BkmvtiFransaBank TestAccountLength(int lengAccount, int lengAccountDebit, String Account, MvbkConf mvk,
			BkmvtiFransaBank bkmvtiFransaBank, DayOperationFransaBank op, String cle) {

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


	public BkmvtiFransaBank setSameData(int methode, BkmvtiFransaBank bkmvtiFransaBank, int index2,
			int indexPieceComptable, DayOperationFransaBank op, MvbkConf mvk) {
		String lib = "";
		String cp = " ";
		String type = " ";
		bkmvtiFransaBank.setCodeOperation(mvk.getCodeOperation());
		lib = mvk.getLibelle_operation();
		bkmvtiFransaBank.setLibelle(lib + getSpace(40 - lib.length()));
		if (mvk.getLibGenerique() != null && mvk.getLibGenerique().trim() != "") {
			String libgenerique = mvk.getLibGenerique();
			libgenerique = libgenerique.replaceAll("dateTransation", op.getDateTransaction());
			String aut = op.getNumAutorisation();
			int lengthAuth = aut.length();
			libgenerique = libgenerique.replaceAll("numAutorisation", aut.substring(lengthAuth - 6));
			bkmvtiFransaBank.setLibelle(libgenerique + getSpace(40 - lib.length()));
		}

		switch (methode) {

		case 1:

			bkmvtiFransaBank.setPieceComptable(op.getPieceComptableBkm());

			bkmvtiFransaBank.setNumEvenement(op.getPieceComptableBkm().substring(op.getPieceComptableBkm().length() - 6,
					op.getPieceComptableBkm().length()));

			break;
		case 2:

			bkmvtiFransaBank.setPieceComptable(op.getPieceComptableBkm());

			bkmvtiFransaBank.setNumEvenement(op.getPieceComptableBkm().substring(op.getPieceComptableBkm().length() - 6,
					op.getPieceComptableBkm().length()));
			bkmvtiFransaBank.setNumEvenement(String.format("%06d", indexPieceComptable));

			break;
		case 3:

			bkmvtiFransaBank.setPieceComptable(op.getPieceComptableBkm());

			bkmvtiFransaBank.setNumEvenement(op.getPieceComptableBkm().substring(op.getPieceComptableBkm().length() - 6,
					op.getPieceComptableBkm().length()));

			break;

		case 4:

			bkmvtiFransaBank.setPieceComptable(op.getPieceComptableBkm());

			bkmvtiFransaBank.setNumEvenement(op.getPieceComptableBkm().substring(op.getPieceComptableBkm().length() - 6,
					op.getPieceComptableBkm().length()));
			bkmvtiFransaBank.setCodeOperation("170");

			break;

		case 5:

			bkmvtiFransaBank.setPieceComptable(op.getPieceComptableBkm());

			bkmvtiFransaBank.setNumEvenement(op.getPieceComptableBkm().substring(op.getPieceComptableBkm().length() - 6,
					op.getPieceComptableBkm().length()));

			break;
		case 6:

			bkmvtiFransaBank.setPieceComptable(op.getPieceComptableBkm());

			bkmvtiFransaBank.setNumEvenement(op.getPieceComptableBkm().substring(op.getPieceComptableBkm().length() - 6,
					op.getPieceComptableBkm().length()));

			break;

		case 7:

			bkmvtiFransaBank.setPieceComptable(op.getPieceComptableBkm());

			bkmvtiFransaBank.setNumEvenement(op.getPieceComptableBkm().substring(op.getPieceComptableBkm().length() - 6,
					op.getPieceComptableBkm().length()));

			break;
		case 8:

			bkmvtiFransaBank.setPieceComptable(op.getPieceComptableBkm());

			bkmvtiFransaBank.setNumEvenement(op.getPieceComptableBkm().substring(op.getPieceComptableBkm().length() - 6,
					op.getPieceComptableBkm().length()));

			break;
		case 9:

			bkmvtiFransaBank.setPieceComptable(op.getPieceComptableBkm());

			bkmvtiFransaBank.setNumEvenement(op.getPieceComptableBkm().substring(op.getPieceComptableBkm().length() - 6,
					op.getPieceComptableBkm().length()));

			break;
		case 10:

			bkmvtiFransaBank.setPieceComptable(op.getPieceComptableBkm());

			bkmvtiFransaBank.setNumEvenement(op.getPieceComptableBkm().substring(op.getPieceComptableBkm().length() - 6,
					op.getPieceComptableBkm().length()));

			break;
		case 11:

			bkmvtiFransaBank.setPieceComptable(op.getPieceComptableBkm());

			bkmvtiFransaBank.setNumEvenement(op.getPieceComptableBkm().substring(op.getPieceComptableBkm().length() - 6,
					op.getPieceComptableBkm().length()));

			break;
		case 12:

			bkmvtiFransaBank.setPieceComptable(op.getPieceComptableBkm());

			bkmvtiFransaBank.setNumEvenement(op.getPieceComptableBkm().substring(op.getPieceComptableBkm().length() - 6,
					op.getPieceComptableBkm().length()));

			break;
		case 13:

			bkmvtiFransaBank.setPieceComptable(op.getPieceComptableBkm());

			bkmvtiFransaBank.setNumEvenement(op.getPieceComptableBkm().substring(op.getPieceComptableBkm().length() - 6,
					op.getPieceComptableBkm().length()));

			break;
		case 14:

			bkmvtiFransaBank.setPieceComptable(op.getPieceComptableBkm());

			bkmvtiFransaBank.setNumEvenement(op.getPieceComptableBkm().substring(op.getPieceComptableBkm().length() - 6,
					op.getPieceComptableBkm().length()));

			break;
		///////////// NEW ***********////////////////////////

		case 15:

			bkmvtiFransaBank.setPieceComptable(op.getPieceComptableBkm());

			bkmvtiFransaBank.setNumEvenement(op.getPieceComptableBkm().substring(op.getPieceComptableBkm().length() - 6,
					op.getPieceComptableBkm().length()));

			break;

		case 16:

			bkmvtiFransaBank.setPieceComptable(op.getPieceComptableBkm());

			bkmvtiFransaBank.setNumEvenement(op.getPieceComptableBkm().substring(op.getPieceComptableBkm().length() - 6,
					op.getPieceComptableBkm().length()));

			break;

		case 17:

			bkmvtiFransaBank.setPieceComptable(op.getPieceComptableBkm());

			bkmvtiFransaBank.setNumEvenement(op.getPieceComptableBkm().substring(op.getPieceComptableBkm().length() - 6,
					op.getPieceComptableBkm().length()));

			break;

		case 18:

			bkmvtiFransaBank.setPieceComptable(op.getPieceComptableBkm());

			bkmvtiFransaBank.setNumEvenement(op.getPieceComptableBkm().substring(op.getPieceComptableBkm().length() - 6,
					op.getPieceComptableBkm().length()));

			break;
		case 19:

			bkmvtiFransaBank.setPieceComptable(op.getPieceComptableBkm());

			bkmvtiFransaBank.setNumEvenement(op.getPieceComptableBkm().substring(op.getPieceComptableBkm().length() - 6,
					op.getPieceComptableBkm().length()));

			break;
		case 20:

			bkmvtiFransaBank.setPieceComptable(op.getPieceComptableBkm());

			bkmvtiFransaBank.setNumEvenement(op.getPieceComptableBkm().substring(op.getPieceComptableBkm().length() - 6,
					op.getPieceComptableBkm().length()));

			break;

		case 21:
			bkmvtiFransaBank.setPieceComptable(op.getPieceComptableBkm());

			bkmvtiFransaBank.setNumEvenement(op.getPieceComptableBkm().substring(op.getPieceComptableBkm().length() - 6,
					op.getPieceComptableBkm().length()));

			break;

		case 22:
			bkmvtiFransaBank.setPieceComptable(op.getPieceComptableBkm());

			bkmvtiFransaBank.setNumEvenement(op.getPieceComptableBkm().substring(op.getPieceComptableBkm().length() - 6,
					op.getPieceComptableBkm().length()));

			break;
		case 23:
			bkmvtiFransaBank.setPieceComptable(op.getPieceComptableBkm());

			bkmvtiFransaBank.setNumEvenement(op.getPieceComptableBkm().substring(op.getPieceComptableBkm().length() - 6,
					op.getPieceComptableBkm().length()));

			break;
		case 24:
			bkmvtiFransaBank.setPieceComptable(op.getPieceComptableBkm());

			bkmvtiFransaBank.setNumEvenement(op.getPieceComptableBkm().substring(op.getPieceComptableBkm().length() - 6,
					op.getPieceComptableBkm().length()));

			break;
		case 25:
			bkmvtiFransaBank.setPieceComptable(op.getPieceComptableBkm());

			bkmvtiFransaBank.setNumEvenement(op.getPieceComptableBkm().substring(op.getPieceComptableBkm().length() - 6,
					op.getPieceComptableBkm().length()));

			break;
		default:
			System.out.println("nothing");
		}
		return bkmvtiFransaBank;
	}

	public BkmvtiFransaBank TestAccountLengthReg(int lengAccount, int lengAccountDebit, String Account, MvbkConf mvk,
			BkmvtiFransaBank bkmvtiFransaBank, dayOperationReglement op, String cle) {

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


	public String TestCompteCreditReg(String compteCredit, dayOperationReglement op) {
		String codeAgence = "";
		if (op.getCompteCredit().length() < 18) {
			codeAgence = op.getCodeAgence();

		} else {

			codeAgence = op.getCompteCredit().substring(3, 8);

		}
		return codeAgence;

	}

	public BkmvtiFransaBank setSameDataReg(int methode, BkmvtiFransaBank bkmvtiFransaBank, int index2,
			int indexPieceComptable, dayOperationReglement op, MvbkConf mvk) {
		String lib = "";
		bkmvtiFransaBank.setCodeOperation(mvk.getCodeOperation());
		lib = mvk.getLibelle_operation();
		bkmvtiFransaBank.setLibelle(lib + getSpace(40 - lib.length()));
		if (mvk.getLibGenerique() != null && mvk.getLibGenerique().trim() != "") {
			String libgenerique = mvk.getLibGenerique();
			libgenerique = libgenerique.replaceAll("dateTransation", op.getDateTransaction());
			String aut = op.getNumAutorisation();
			int lengthAuth = aut.length();
			libgenerique = libgenerique.replaceAll("numAutorisation", aut.substring(lengthAuth - 6));
			bkmvtiFransaBank.setLibelle(libgenerique + getSpace(40 - lib.length()));
		}
		switch (methode) {

		case 1:

			bkmvtiFransaBank.setPieceComptable(op.getPieceComptableBkm());

			bkmvtiFransaBank.setNumEvenement(op.getPieceComptableBkm().substring(op.getPieceComptableBkm().length() - 6,
					op.getPieceComptableBkm().length()));

			break;
		case 2:

			bkmvtiFransaBank.setPieceComptable(op.getPieceComptableBkm());

			bkmvtiFransaBank.setNumEvenement(op.getPieceComptableBkm().substring(op.getPieceComptableBkm().length() - 6,
					op.getPieceComptableBkm().length()));
			bkmvtiFransaBank.setNumEvenement(String.format("%06d", indexPieceComptable));

			break;
		case 3:

			bkmvtiFransaBank.setPieceComptable(op.getPieceComptableBkm());

			bkmvtiFransaBank.setNumEvenement(op.getPieceComptableBkm().substring(op.getPieceComptableBkm().length() - 6,
					op.getPieceComptableBkm().length()));

			break;

		case 4:

			bkmvtiFransaBank.setPieceComptable(op.getPieceComptableBkm());

			bkmvtiFransaBank.setNumEvenement(op.getPieceComptableBkm().substring(op.getPieceComptableBkm().length() - 6,
					op.getPieceComptableBkm().length()));

			break;

		case 5:

			bkmvtiFransaBank.setPieceComptable(op.getPieceComptableBkm());

			bkmvtiFransaBank.setNumEvenement(op.getPieceComptableBkm().substring(op.getPieceComptableBkm().length() - 6,
					op.getPieceComptableBkm().length()));

			break;
		case 6:

			bkmvtiFransaBank.setPieceComptable(op.getPieceComptableBkm());

			bkmvtiFransaBank.setNumEvenement(op.getPieceComptableBkm().substring(op.getPieceComptableBkm().length() - 6,
					op.getPieceComptableBkm().length()));

			break;

		case 7:

			bkmvtiFransaBank.setPieceComptable(op.getPieceComptableBkm());

			bkmvtiFransaBank.setNumEvenement(op.getPieceComptableBkm().substring(op.getPieceComptableBkm().length() - 6,
					op.getPieceComptableBkm().length()));

			break;
		case 8:

			bkmvtiFransaBank.setPieceComptable(op.getPieceComptableBkm());

			bkmvtiFransaBank.setNumEvenement(op.getPieceComptableBkm().substring(op.getPieceComptableBkm().length() - 6,
					op.getPieceComptableBkm().length()));

			break;
		case 9:

			bkmvtiFransaBank.setPieceComptable(op.getPieceComptableBkm());

			bkmvtiFransaBank.setNumEvenement(op.getPieceComptableBkm().substring(op.getPieceComptableBkm().length() - 6,
					op.getPieceComptableBkm().length()));

			break;
		case 10:

			bkmvtiFransaBank.setPieceComptable(op.getPieceComptableBkm());

			bkmvtiFransaBank.setNumEvenement(op.getPieceComptableBkm().substring(op.getPieceComptableBkm().length() - 6,
					op.getPieceComptableBkm().length()));

			break;
		case 11:

			bkmvtiFransaBank.setPieceComptable(op.getPieceComptableBkm());

			bkmvtiFransaBank.setNumEvenement(op.getPieceComptableBkm().substring(op.getPieceComptableBkm().length() - 6,
					op.getPieceComptableBkm().length()));

			break;
		case 12:

			bkmvtiFransaBank.setPieceComptable(op.getPieceComptableBkm());

			bkmvtiFransaBank.setNumEvenement(op.getPieceComptableBkm().substring(op.getPieceComptableBkm().length() - 6,
					op.getPieceComptableBkm().length()));

			break;
		case 13:

			bkmvtiFransaBank.setPieceComptable(op.getPieceComptableBkm());

			bkmvtiFransaBank.setNumEvenement(op.getPieceComptableBkm().substring(op.getPieceComptableBkm().length() - 6,
					op.getPieceComptableBkm().length()));

			break;
		case 14:

			bkmvtiFransaBank.setPieceComptable(op.getPieceComptableBkm());

			bkmvtiFransaBank.setNumEvenement(op.getPieceComptableBkm().substring(op.getPieceComptableBkm().length() - 6,
					op.getPieceComptableBkm().length()));

			break;
		///////////// NEW ***********////////////////////////

		case 15:

			bkmvtiFransaBank.setPieceComptable(op.getPieceComptableBkm());

			bkmvtiFransaBank.setNumEvenement(op.getPieceComptableBkm().substring(op.getPieceComptableBkm().length() - 6,
					op.getPieceComptableBkm().length()));

			break;

		case 16:

			bkmvtiFransaBank.setPieceComptable(op.getPieceComptableBkm());

			bkmvtiFransaBank.setNumEvenement(op.getPieceComptableBkm().substring(op.getPieceComptableBkm().length() - 6,
					op.getPieceComptableBkm().length()));

			break;

		case 17:

			bkmvtiFransaBank.setPieceComptable(op.getPieceComptableBkm());

			bkmvtiFransaBank.setNumEvenement(op.getPieceComptableBkm().substring(op.getPieceComptableBkm().length() - 6,
					op.getPieceComptableBkm().length()));

			break;

		case 18:

			bkmvtiFransaBank.setPieceComptable(op.getPieceComptableBkm());

			bkmvtiFransaBank.setNumEvenement(op.getPieceComptableBkm().substring(op.getPieceComptableBkm().length() - 6,
					op.getPieceComptableBkm().length()));

			break;

		case 19:

			bkmvtiFransaBank.setPieceComptable(op.getPieceComptableBkm());

			bkmvtiFransaBank.setNumEvenement(op.getPieceComptableBkm().substring(op.getPieceComptableBkm().length() - 6,
					op.getPieceComptableBkm().length()));

			break;
		case 20:

			bkmvtiFransaBank.setPieceComptable(op.getPieceComptableBkm());

			bkmvtiFransaBank.setNumEvenement(op.getPieceComptableBkm().substring(op.getPieceComptableBkm().length() - 6,
					op.getPieceComptableBkm().length()));

			break;

		case 21:

			bkmvtiFransaBank.setPieceComptable(op.getPieceComptableBkm());

			bkmvtiFransaBank.setNumEvenement(op.getPieceComptableBkm().substring(op.getPieceComptableBkm().length() - 6,
					op.getPieceComptableBkm().length()));

			break;

		case 22:

			bkmvtiFransaBank.setPieceComptable(op.getPieceComptableBkm());

			bkmvtiFransaBank.setNumEvenement(op.getPieceComptableBkm().substring(op.getPieceComptableBkm().length() - 6,
					op.getPieceComptableBkm().length()));

			break;
		case 23:

			bkmvtiFransaBank.setPieceComptable(op.getPieceComptableBkm());

			bkmvtiFransaBank.setNumEvenement(op.getPieceComptableBkm().substring(op.getPieceComptableBkm().length() - 6,
					op.getPieceComptableBkm().length()));

			break;
		case 24:

			bkmvtiFransaBank.setPieceComptable(op.getPieceComptableBkm());

			bkmvtiFransaBank.setNumEvenement(op.getPieceComptableBkm().substring(op.getPieceComptableBkm().length() - 6,
					op.getPieceComptableBkm().length()));

			break;
		case 25:

			bkmvtiFransaBank.setPieceComptable(op.getPieceComptableBkm());

			bkmvtiFransaBank.setNumEvenement(op.getPieceComptableBkm().substring(op.getPieceComptableBkm().length() - 6,
					op.getPieceComptableBkm().length()));

			break;
		default:
			System.out.println("nothing");
		}
		return bkmvtiFransaBank;
	}

	public BkmvtiFransaBank TestCodeAgenceReg(int test, String codeAgence, MvbkConf mvk, dayOperationReglement op,
			BkmvtiFransaBank bkmvtiFransaBank) {
		if (test != 3 && test != 4) {
			if (codeAgence == null) {
				if (test == 1) {
					bkmvtiFransaBank.setAgence(TestCompteCreditReg(op.getCompteCredit(), op));
				} else {
					bkmvtiFransaBank.setAgence(op.getCodeAgence());

				}
			} else if (mvk.getCodeAgence().equals("00002")) {
				bkmvtiFransaBank.setAgence(mvk.getCodeAgence());
			}

			else if (mvk.getCodeAgence().equals("MERCH")) {
				bkmvtiFransaBank.setAgence(op.getCodeAgence());
			} else if (mvk.getCodeAgence().equals("CARD")) {
				bkmvtiFransaBank.setAgence(op.getNumRIBEmetteur().substring(3, 8));
			}
		}

		else if (test == 3) {
			String code = op.getNumRIBEmetteur().substring(3, 8);
			bkmvtiFransaBank.setAgence(code);
		} else if (test == 4) {
			String code = op.getIdCommercant().substring(3, 8);
			bkmvtiFransaBank.setAgence(code);
		}
		return bkmvtiFransaBank;
	}

	public void Reject055(UAP051INR uap) {
		List<MvbkSettlement> allMvbkSettelemnts = mvbkSettlementRepository.Reject50inAchatExtra();
		List<MvbkConf> allMvbkSettelemntsC = mvbkConfigR.findByCategorie(554);

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

	public void Reject051Extra(UAP051INR uap) {
		List<MvbkSettlement> allMvbkSettelemnts = mvbkSettlementRepository
				.findByIdentificationOffUsAcqRembourssementTpeEXTRA();
		List<MvbkConf> allMvbkSettelemntsC = mvbkConfigR.findByCategorie(5546);

		List<BkmvtiFransaBank> bkms = new ArrayList<BkmvtiFransaBank>();
		int indexPieceComptable = ++max;
		for (MvbkConf mvk : allMvbkSettelemntsC) {

			bkms = getDataExtraUap(25, mvk, uap, bkms, indexPieceComptable);

		}

		bkmvtiFransaBankRepository.saveAll(bkms);
	}
	public void Reject051(UAP051INR uap) {
		List<MvbkSettlement> allMvbkSettelemnts = mvbkSettlementRepository.Reject50inAchatExtra();
		List<MvbkConf> allMvbkSettelemntsC = mvbkConfigR.findByCategorie(553);

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
	public void Reject055Extra(UAP051INR uap) {
		List<MvbkSettlement> allMvbkSettelemnts = mvbkSettlementRepository.Reject50inAchatExtra();
		List<MvbkConf> allMvbkSettelemntsC = mvbkConfigR.findByCategorie(5554);

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

//		List<MvbkSettlement> allMvbkSettelemnts = mvbkSettlementRepository.Reject51inRembInternetExtra();
//		List<MvbkConf> allMvbkSettelemntsC = mvbkConfigR.findByCategorie(5554);
//
//		List<BkmvtiFransaBank> bkms = new ArrayList<BkmvtiFransaBank>();
//		int indexPieceComptable = ++max;
//		for (MvbkConf mvk : allMvbkSettelemntsC) {
//
//			bkms = getDataExtraUap(25, mvk, uap, bkms, indexPieceComptable);
//
//		}
//
//		bkmvtiFransaBankRepository.saveAll(bkms);
//	}
	public List<BkmvtiFransaBank> TestSigneWithoutDayOperation(UAP051INR uap, int methode, MvbkConf mvk,
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

	public BkmvtiFransaBank TestAccountAndSigneWithoutSayOperation(UAP051INR uap, int methode, int index2,
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
		BigDecimal commConf= new BigDecimal( uap.getMontantCommissionFSBKTTC());
  
		data.put("MntRemb", mntRet);
		data.put("CommRembFSBK", commConf);
        BigDecimal roundedValue = fIPService.evaluateWithElementswithBigDecimal(mvk.getCodeSettlement(), data).divide(new BigDecimal(100));

        DecimalFormat decimalFormat = new DecimalFormat("000000000000000000.00");
        String formattedValue = decimalFormat.format(roundedValue);

        // Replace the decimal point with a comma
        formattedValue = formattedValue.replace('.', ',');

        // save the formatted value
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
	public List<BkmvtiFransaBank> getDataExtraUap(int methode, MvbkConf mvk, UAP051INR uap, List<BkmvtiFransaBank> bkms,
			int index) {
		BkmvtiFransaBank bkm = new BkmvtiFransaBank();

		bkm.setCodeDevice("208");
		String name = SecurityContextHolder.getContext().getAuthentication().getName();
		Optional<User> user = userRepository.findByUserNameOrUserEmail(name, name);
		if (!user.isPresent()) {
			throw new RuntimeException("Error saving status !");
		}

		bkm.setPieceComptable("RB" + mvk.getCodeOperation() + String.format("%06d", index));

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
		BigDecimal mntRet= new BigDecimal( uap.getMontantTransaction());
		BigDecimal commConf= new BigDecimal( uap.getMontantCommissionFSBKTTC());
  
		data.put("MntRemb", mntRet);
		data.put("CommRembFSBK", commConf);
        BigDecimal roundedValue = fIPService.evaluateWithElementswithBigDecimal(mvk.getCodeSettlement(), data).divide(new BigDecimal(100));

        DecimalFormat decimalFormat = new DecimalFormat("000000000000000000.00");
        String formattedValue = decimalFormat.format(roundedValue);

        // Replace the decimal point with a comma
        formattedValue = formattedValue.replace('.', ',');

        // save the formatted value
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
			UAP051INR uap) {
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

	@PutMapping("Updatemotif")
	public void Updatemotif(@RequestBody UAP051IN uap) throws IOException {
		UAP051IN el = uAP051INFransaBankRepository.getOne(uap.getCode());
		el.setMotifRejet(uap.getMotifRejet());
		uAP051INFransaBankRepository.save(el);
	}

	@PutMapping("updateElement")
	public void updateElement(@RequestBody UAP051IN uap) throws IOException {

		if (uap != null && uap.getNumTransaction() != null) {
			uAP051INFransaBankRepository.save(uap);
		}
	}

	public void validateALL() throws IOException {
		List<UAP051IN> elements = uAP051INFransaBankRepository.filterPending();
		if (elements != null && elements.size() > 0) {
			elements.forEach(x -> {
				x.setFlag(REJECTMODIFIED);
			});
			uAP051INFransaBankRepository.saveAll(elements);
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
		Optional<OpeningDay> d = odr.findByStatus051("doneSort");
		long sum = 0;
		if (d.isPresent()) {
			orc.copyUap051In();
			int i = 1;
			i = d.get().getLotIncrementNb();
			LocalDateTime currentDateTime = LocalDateTime.now();
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} // Define the desired date format
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
			String time051Ord = currentDateTime.format(formatter);
			time051Ord = time051Ord.substring(0, time051Ord.length() - 3) + "051";
			int numberOp = 0;
			List<UAP051INR> data = uAP051INRFransaBankRepository.findByFlag(REJECTPENDING);
			List<UAP051INR> data2 = uAP051INRFransaBankRepository.findByFlag(REJECTNOTACCEPTED);
			List<UAP051INR> data3 = uAP051INRFransaBankRepository.findByFlag(REJECTERROR);
			List<UAP051INR> data4 = uAP051INRFransaBankRepository.findByFlag(REJECTMODIFIED);
			data.addAll(data2);
			data.addAll(data3);
			data.addAll(data4);

			String enteteReel = "ELOT035000" + String.format("%03d", i) + "DZD" + String.format("%04d", data.size())
					+ repeat("0", 16) + repeat(" ", 28) + "\n";
			ArrayList<String> lignes = new ArrayList<String>();

			for (UAP051INR element : data) {
				sum += Long.valueOf(element.getMontantAComponser().replace(".", ""));
				String ligne = "151" + element.getMotifRejet() + element.getNumRIBcommercant().substring(3, 8)
						+ String.format("%13s", element.getCode()).replace(' ', '0') + codeBank
						+ element.getNumRIBcommercant().substring(3, 8) + element.getRio() + repeat(" ", 130);
				lignes.add(ligne);
			}

			writeFile("035.000." + String.format("%03d", i) + ".151.DZD.LOT", enteteReel + String.join("\n", lignes),
					false);

			String Entete = "ORD" + String.format("%03d", i) + "INLOT" + "000" + "001" + "151" + "DZD" + getSpace(41)
					+ "\n";

			writeFile("035." + String.format("%03d", i) + "." + time051Ord + ".ORD", Entete, false);
			if (lignes.size() > 0) {
				CraRejetControl craControl = new CraRejetControl("035.000." + String.format("%03d", i) + ".151.DZD.LOT",
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
		OpeningDay day = odr.findByStatus051("doneSort").get();
		day.setStatus051("doneCro");
		odr.save(day);
		batchRepo.updateFinishBatch("CRAUAP151IN", 1, new Date());
	}

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

	// used temporarly to force the rejection
	public void forceReject() {
		updateRejectionExtra(REJECTACCEPTED);
		updateRejectionNOTOK(REJECTACCEPTED);
	}

	@GetMapping("/integrateCra")
	public void handelRejectNotOK() {
		Optional<OpeningDay> opd = odr.findByStatus051("doneSort");

		if (opd.isPresent() && uAP051INRFransaBankRepository.findByFlag(REJECTPENDING).size() > 0) {

			List<UAP051INR> newList = uAP051INRFransaBankRepository.findByFlag(REJECTPENDING);
			BatchesFC batch = batchRepo.getOne(55);
			batch.setBatchDate(new Date());
			batch.setBatchStatus(0);
			List<String> filesNames = listAndFilterFiles(batch.getFileLocation(), "151");
			allaccepted = true;

			if (filesNames != null && filesNames.size() > 0) {
				// readfile
				rioIn = readFiles(filesNames, batch.getFileLocation());
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
				uAP051INRFransaBankRepository.saveAll(newList);
				updateRejectionExtra(REJECTACCEPTED);
				updateRejectionNOTOK(REJECTACCEPTED);

				try {
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

			if (allaccepted) {
				OpeningDay day = odr.findByStatus051("doneSort").get();
				day.setStatus051("doneCro");
				odr.save(day);

				batch.setBatchStatus(1);
				batch.setBatchEndDate(new Date());
				batchRepo.save(batch);
			} else {
				batch.setBatchStatus(4);
				batch.setBatchEndDate(new Date());
				batchRepo.save(batch);
			}
		}

	}

	private void saveCraControl(String fileIntegration) {
		List<CraRejetControl> craControlList = craRejetControlRepository.findByProcessingDateAndLotType(fileIntegration,
				"151");

		if (craControlList.size() > 0) {

			List<UapDetailsControl> acceptedUp = uAP051INRFransaBankRepository.getListUAP51AcceptedForControl();
			long sumAcceptedUp = 0;
			for (UapDetailsControl el : acceptedUp) {
				sumAcceptedUp += Long.valueOf(el.getMontantAComponser().replace(".", ""));

			}
//			
			long sumNotAccepted = 0;
			List<UAP051INR> notAccepted = uAP051INRFransaBankRepository.getListUAP51NotAcceptedForControl();
			List<UAP051INNotAccepted> notAcceptedList = new ArrayList<UAP051INNotAccepted>();
			for (UAP051INR el : notAccepted) {
				sumNotAccepted += Long.valueOf(el.getMontantAComponser().replace(".", ""));

				UAP051INNotAccepted notAcceptedUap51 = new UAP051INNotAccepted();
				try {
					PropertyUtils.copyProperties(notAcceptedUap51, el);
					notAcceptedUap51.setControlId(fileIntegration);
				} catch (Exception ex) {
					logger.info("Exception");
					logger.info(Throwables.getStackTraceAsString(ex));
				}
				notAcceptedList.add(notAcceptedUap51);

			}

			for (CraRejetControl craControl : craControlList) {
				logger.info("cra control is found");

				craControl.setSumAccepted(sumAcceptedUp);
				craControl.setNbAccepted(acceptedUp.size());

				craControl.setSumNotAccepted(sumNotAccepted);
				craControl.setNbNotAccepted(notAccepted.size());

				craRejetControlRepository.save(craControl);
			}
			uAP051InNotAcceptedRepository.saveAll(notAcceptedList);
		}
	}

	public void updateRejectionExtra(String flag) {
		List<UAP051INR> elements = uAP051INRFransaBankRepository.findByFlagAndAcceptedAndDateRegAfterToday(flag, "2");
		List<UAP051INR> upUpdated = new ArrayList<UAP051INR>();
		try {
	        try {
	            String lastNumIndex = bkmvtiFransaBankRepository.getLastNumIndex();
	            max = (lastNumIndex == null) ? 1 : Integer.valueOf(lastNumIndex);
	        } catch (Exception e) {
	            max = 1; // Default value in case of an exception
	            logger.error("An exception occurred while retrieving the last number index: {}", e.getMessage(), e);
	        }

			elements.forEach(element -> {

				if (((element.getTypeTransaction().equals("051") && element.getTypePaiement().equals("01"))
						|| (element.getTypeTransaction().equals("051") && element.getTypePaiement().equals("05"))
								&& element.getAccepted().equals("2"))) {
					//TPE
					Reject051Extra(element);
					element.setFlag(REJECTDONE);
					element.setBkmGeneration("done");

					upUpdated.add(element);

				} else if (((element.getTypeTransaction().equals("051") && element.getTypePaiement().equals("04"))
						|| (element.getTypeTransaction().equals("051") && element.getTypePaiement().equals("03"))
								&& element.getAccepted().equals("2"))) {
					//TVP
					Reject055Extra(element);
					element.setFlag(REJECTDONE);
					element.setBkmGeneration("done");

					upUpdated.add(element);

				}

			});
			uAP051INRFransaBankRepository.saveAll(upUpdated);
			BatchesFC batch = batchRepo.findByKey("CRAUAP51IN").get();

			batchRepo.updateFinishBatch("CRAUAP51IN", 1, new Date());

			// moveFilesByStartingName(batch.getFileLocation(),
			// propertyService.getCompensationfilePath(), batch.getFileName());

		} catch (Exception e) {
			String stackTrace = Throwables.getStackTraceAsString(e);
			logger.info("Exception is=>{}", stackTrace);
			if (stackTrace.length() > 4000)
				stackTrace = stackTrace.substring(0, 3999);
			String error = e.getMessage() == null ? e.toString() : e.getMessage();
			if (error.length() > 255)
				error = error.substring(0, 254);

			batchRepo.updateStatusAndErrorBatch("CRAUAP51IN", 2, error, new Date(), stackTrace);

		}
	}

	public void updateRejectionNOTOK(String flag) {
		List<UAP051INR> elements = uAP051INRFransaBankRepository.findByFlagAndAcceptedAndDateRegAfterToday(flag,
				"NOT OK");
		List<UAP051INR> upUpdated = new ArrayList<UAP051INR>();
		try {
			elements.forEach(element -> {

				if (((element.getTypeTransaction().equals("051") && element.getTypePaiement().equals("01"))
						|| (element.getTypeTransaction().equals("051") && element.getTypePaiement().equals("05"))
								&& element.getAccepted().equals("NOT OK"))) {
//					TPE
					Reject051(element);
					element.setFlag(REJECTDONE);
					element.setBkmGeneration("done");

					upUpdated.add(element);

				} else if (((element.getTypeTransaction().equals("051") && element.getTypePaiement().equals("04"))
						|| (element.getTypeTransaction().equals("051") && element.getTypePaiement().equals("03"))
								&& element.getAccepted().equals("NOT OK"))) {
//					TVP
					Reject055(element);
					element.setFlag(REJECTDONE);
					element.setBkmGeneration("done");

					upUpdated.add(element);

				}

			});
			uAP051INRFransaBankRepository.saveAll(upUpdated);
			BatchesFC batch = batchRepo.findByKey("CRAUAP51IN").get();

			batchRepo.updateFinishBatch("CRAUAP51IN", 1, new Date());
		} catch (Exception e) {
			String stackTrace = Throwables.getStackTraceAsString(e);
			logger.info("Exception is=>{}", stackTrace);
			if (stackTrace.length() > 4000)
				stackTrace = stackTrace.substring(0, 3999);
			String error = e.getMessage() == null ? e.toString() : e.getMessage();
			if (error.length() > 255)
				error = error.substring(0, 254);

			batchRepo.updateStatusAndErrorBatch("CRAUAP51IN", 2, error, new Date(), stackTrace);

		}
	}

	// ****************************Read reject CRA END***************************//
//	updateMissMatch040
	@PutMapping("updateMissMatch")
	public void HandleRejects(@RequestBody UapIn uaps) {
		Optional<OpeningDay> d = odr.findByStatus051("doneSort");
		FileRequest.print(uaps.toString(), FileRequest.getLineNumber());

		if (d.isPresent()) {

			if (uaps.getIdRejet() != null && uaps.getIdRejet().size() > 0) {
				List<Integer> uapIdReject = uaps.getIdRejet();
				List<UAP051IN> rejected = uapIdReject.stream().map(element -> {
					UAP051IN uap = uAP051INFransaBankRepository.findById(element).get();
					uap.setFlag(REJECTPENDING);
//					uap.setMotifRejet(uaps.getMotifRExtra());

					return uap;
				}).collect(Collectors.toList());
				uAP051INFransaBankRepository.saveAll(rejected);
			}
			if (uaps.getIdAcceptation() != null && uaps.getIdAcceptation().size() > 0) {
				List<Integer> uapIdAcc = uaps.getIdAcceptation();
				List<UAP051IN> accepted = uapIdAcc.stream().map(element -> {
					UAP051IN uap = uAP051INFransaBankRepository.findById(element).get();
					uap.setFlag("1");
					return uap;
				}).collect(Collectors.toList());

				uAP051INFransaBankRepository.saveAll(accepted);
			}
			if (uaps.getHandleAll() != null && uaps.getHandleAll().equals("accept")) {
				List<UAP051IN> list = uAP051INFransaBankRepository.findByMatchedAndEmptyFlag();

				list.forEach(ele -> {
					ele.setFlag("1");
				});
				uAP051INFransaBankRepository.saveAll(list);

			}

			if (uaps.getHandleAll() != null && uaps.getHandleAll().equals("reject")) {
				List<UAP051IN> list = uAP051INFransaBankRepository.findByMatchedAndEmptyFlag();
				list.forEach(ele -> {
					ele.setFlag(REJECTPENDING);
					ele.setMotifRejet(uaps.getMotifRExtra());
				});
				uAP051INFransaBankRepository.saveAll(list);

			}
			copyUap051In();
			int nbnotOK = uAP051INRFransaBankRepository.findByFlagAndAccepted(REJECTPENDING, "NOT OK").size();
			int nbnExtra = uAP051INRFransaBankRepository.findByFlagAndAccepted(REJECTPENDING, "2").size();

			if ((nbnotOK + nbnExtra) > 0) {
				generateRejectFile();
				batchRepo.updateFinishBatch("CRAUAP51IN", 1, new Date());
			} else {
				batchRepo.updateFinishBatch("CRAUAP151IN", 1, new Date());
				batchRepo.updateFinishBatch("CRAUAP51IN", 1, new Date());
			}

		}
	}

	public void copyUap051In() {
		List<UAP051IN> in = uAP051INFransaBankRepository.findByCopiedAndFlag("false", "20");
		List<UAP051INR> out = in.stream().map(developer -> {

			UAP051INR copiedUap = new UAP051INR();
			developer.setCopied("done");

			try {

				PropertyUtils.copyProperties(copiedUap, developer);

			} catch (Exception ex) {
				ex.printStackTrace();
			}
			copiedUap.setBkmGeneration(null);
			return copiedUap;

		}).collect(Collectors.toList());
		uAP051INRFransaBankRepository.saveAll(out);
		uAP051INFransaBankRepository.saveAll(in);
	}
	public BkmvtiFransaBank setSameDataWithoutDayOperation(UAP051INR uap, int methode,
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
	public BkmvtiFransaBank TestAccountLengthWithoutDayOperation(UAP051INR uap, int lengAccount, String Account,
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

	public BkmvtiFransaBank TestCodeAgenceWithoutDayOperation(UAP051INR uap, String codeAgence, MvbkConf mvk,
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

}
