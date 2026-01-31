package com.mss.backOffice.services;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.mss.unified.entities.BkmvtiFransaBank;
import com.mss.unified.entities.MvbkSettlement;
import com.mss.unified.entities.UAPFransaBank;
import com.mss.unified.entities.User;
import com.mss.unified.repositories.UserRepository;
@Service
public class BkmService {
	@Autowired
	UserRepository userRepository;
	
	public List<BkmvtiFransaBank> TestSigneWithoutDayOperation(UAPFransaBank uap, int methode, MvbkSettlement mvk,
			List<BkmvtiFransaBank> BkmvtiFransaBanks, int indexPieceComptable, int index2, String codeOperation) {
		BkmvtiFransaBank bkmvtiFransaBank = new BkmvtiFransaBank();

		if (AccountSigne(mvk.getAccount(), mvk.getSigne()) == 1
				|| AccountSigne(mvk.getAccount(), mvk.getSigne()) == 2
				|| AccountSigne(mvk.getAccount(), mvk.getSigne()) == 3) {
			bkmvtiFransaBank = TestAccountAndSigneWithoutSayOperation(uap, methode, index2, indexPieceComptable, 1,
					mvk.getAccount(), mvk.getSigne(), mvk, mvk.getAccount());
			BkmvtiFransaBanks.add(bkmvtiFransaBank);

		}

		return BkmvtiFransaBanks;
	}

	public BkmvtiFransaBank TestAccountAndSigneWithoutSayOperation(UAPFransaBank uap, int methode, int index2,
			int indexPieceComptable, int test, String account, String signe, MvbkSettlement mvk, String cle) {
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
		int lengthRefDossier = (uap.getNumtransaction() + uap.getDateTransaction()
				+ uap.getNumAutorisation().substring(lengthNumAuth - 6, lengthNumAuth)).length();

		bkmvtiFransaBank.setRefDossier(uap.getNumtransaction() + uap.getDateTransaction()
				+ uap.getNumAutorisation().substring(lengthNumAuth - 6, lengthNumAuth)
				+ getSpace(50 - lengthRefDossier));

		TestCodeAgenceWithoutDayOperation(uap, mvk.getCodeAgence(), mvk, bkmvtiFransaBank);

		getTransactionDate(uap.getDateTransaction(), bkmvtiFransaBank);

		int lengthReferanceLettrage = uap.getNumAutorisation().length();

		bkmvtiFransaBank.setReferanceLettrage(uap.getDateTransaction().substring(6, uap.getDateTransaction().length())
				+ uap.getNumAutorisation().substring(lengthReferanceLettrage - 6, lengthReferanceLettrage));
		
		getAmount(Float.parseFloat(uap.getMontantAComponser()), bkmvtiFransaBank);


		bkmvtiFransaBank.setCodeDeviceOrigine("208");
		bkmvtiFransaBank.setIdentification(mvk.getIdentification());
		setSameDataWithoutDayOperation(uap, methode, bkmvtiFransaBank, index2, indexPieceComptable);

		return bkmvtiFransaBank;

	}

	public BkmvtiFransaBank TestAccountLengthWithoutDayOperation(UAPFransaBank uap, int lengAccount, String Account,
			MvbkSettlement mvk, BkmvtiFransaBank bkmvtiFransaBank, String cle) {

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

	public BkmvtiFransaBank setSameDataWithoutDayOperation(UAPFransaBank uap, int methode,
			BkmvtiFransaBank bkmvtiFransaBank, int index2, int indexPieceComptable) {
		String lib = "";
		switch (methode) {
		case 2:
			bkmvtiFransaBank.setPieceComptable(uap.getPieceComptableBKM());

			bkmvtiFransaBank.setNumEvenement(uap.getPieceComptableBKM()
					.substring(uap.getPieceComptableBKM().length() - 6, uap.getPieceComptableBKM().length()));
	
			bkmvtiFransaBank.setCodeOperation("162");
			lib = "RETRAIT PORTEUR FSBK/ATM CONF";
			bkmvtiFransaBank.setLibelle(lib + getSpace(40 - lib.length()));

			break;
		case 3:

			bkmvtiFransaBank.setPieceComptable(uap.getPieceComptableBKM());

			bkmvtiFransaBank.setNumEvenement(uap.getPieceComptableBKM()
					.substring(uap.getPieceComptableBKM().length() - 6, uap.getPieceComptableBKM().length()));
			bkmvtiFransaBank.setCodeOperation("170");
			lib = "RETRAIT PORTEUR CONF/ATM FSBK";
			bkmvtiFransaBank.setLibelle(lib + getSpace(40 - lib.length()));

			break;
		case 7:
			
			 bkmvtiFransaBank.setCodeOperation("164");
		     lib = "PAIEMENT PORTEUR FSBK/TPE CONF";
		     bkmvtiFransaBank.setLibelle(lib + getSpace(40 - lib.length()));
          
    break;
		case 8:

			bkmvtiFransaBank.setPieceComptable(uap.getPieceComptableBKM());

			bkmvtiFransaBank.setNumEvenement(uap.getPieceComptableBKM()
					.substring(uap.getPieceComptableBKM().length() - 6, uap.getPieceComptableBKM().length()));
			bkmvtiFransaBank.setCodeOperation("171");
			lib = "PAIEMENT PORTEUR CONF/TPE FSBK";
			bkmvtiFransaBank.setLibelle(lib + getSpace(40 - lib.length()));

			break;
	

		case 10:
			bkmvtiFransaBank.setPieceComptable(uap.getPieceComptableBKM());

			bkmvtiFransaBank.setNumEvenement(uap.getPieceComptableBKM()
					.substring(uap.getPieceComptableBKM().length() - 6, uap.getPieceComptableBKM().length()));
	
			bkmvtiFransaBank.setCodeOperation("106");
			lib = "CONSULTATION SOLDE PORTEUR FSBK/ATM CONF";
			bkmvtiFransaBank.setLibelle(lib + getSpace(40 - lib.length()));

			break;
		case 11:

			bkmvtiFransaBank.setPieceComptable(uap.getPieceComptableBKM());

			bkmvtiFransaBank.setNumEvenement(uap.getPieceComptableBKM()
					.substring(uap.getPieceComptableBKM().length() - 6, uap.getPieceComptableBKM().length()));

			bkmvtiFransaBank.setCodeOperation("107");
			lib = "CONSULTATION SOLDE PORTEUR CONF/ATM FSBK";
			bkmvtiFransaBank.setLibelle(lib + getSpace(40 - lib.length()));

			break;
		case 13:
			bkmvtiFransaBank.setPieceComptable(uap.getPieceComptableBKM());

			bkmvtiFransaBank.setNumEvenement(uap.getPieceComptableBKM()
					.substring(uap.getPieceComptableBKM().length() - 6, uap.getPieceComptableBKM().length()));

			bkmvtiFransaBank.setCodeOperation("169");
			lib = "REMBOURSSEMENT PORTEUR FSBK/TPE CONF";
			bkmvtiFransaBank.setLibelle(lib + getSpace(40 - lib.length()));

			break;
		case 14:

			bkmvtiFransaBank.setPieceComptable(uap.getPieceComptableBKM());

			bkmvtiFransaBank.setNumEvenement(uap.getPieceComptableBKM()
					.substring(uap.getPieceComptableBKM().length() - 6, uap.getPieceComptableBKM().length()));

			bkmvtiFransaBank.setCodeOperation("***");
			lib = "REMBOURSSEMENT PORTEUR CONF/TPE FSBK";
			bkmvtiFransaBank.setLibelle(lib + getSpace(40 - lib.length()));

			break;

		case 15:

			bkmvtiFransaBank.setPieceComptable(uap.getPieceComptableBKM());

			bkmvtiFransaBank.setNumEvenement(uap.getPieceComptableBKM()
					.substring(uap.getPieceComptableBKM().length() - 6, uap.getPieceComptableBKM().length()));
			bkmvtiFransaBank.setCodeOperation("170");
			lib = "RETRAIT PORTEUR ALP/ATM FSBK";
			bkmvtiFransaBank.setLibelle(lib + getSpace(40 - lib.length()));

			break;
		case 16:
			bkmvtiFransaBank.setPieceComptable(uap.getPieceComptableBKM());

			bkmvtiFransaBank.setNumEvenement(uap.getPieceComptableBKM()
					.substring(uap.getPieceComptableBKM().length() - 6, uap.getPieceComptableBKM().length()));
	
			bkmvtiFransaBank.setCodeOperation("170");
			lib = "RETRAIT PORTEUR FSBK/ATM ALP";
			bkmvtiFransaBank.setLibelle(lib + getSpace(40 - lib.length()));

			break;	
	  	case 18:
   		 bkmvtiFransaBank.setCodeOperation("961");
 		     lib = "PAIEMENT PORTEUR FSBK/TVP CONF";
		     bkmvtiFransaBank.setLibelle(lib + getSpace(40 - lib.length()));
            
   	break;
		
		case 19:

			bkmvtiFransaBank.setPieceComptable(uap.getPieceComptableBKM());

			bkmvtiFransaBank.setNumEvenement(uap.getPieceComptableBKM()
					.substring(uap.getPieceComptableBKM().length() - 6, uap.getPieceComptableBKM().length()));
			bkmvtiFransaBank.setCodeOperation("170");
			lib = "PAIEMENT PORTEUR CONF/TVP FSBK";
			bkmvtiFransaBank.setLibelle(lib + getSpace(40 - lib.length()));

			break;
		case 20:
   		 bkmvtiFransaBank.setCodeOperation("263");
 		     lib = "SORT PAIE FACTURE FSB/TPV CONF";
		     bkmvtiFransaBank.setLibelle(lib + getSpace(40 - lib.length()));
            
   	break;

		case 21:

			bkmvtiFransaBank.setPieceComptable(uap.getPieceComptableBKM());

			bkmvtiFransaBank.setNumEvenement(uap.getPieceComptableBKM()
					.substring(uap.getPieceComptableBKM().length() - 6, uap.getPieceComptableBKM().length()));
			bkmvtiFransaBank.setCodeOperation("170");
			lib = "PAIEMENT PORTEUR ALP/TVP FSBK";
			bkmvtiFransaBank.setLibelle(lib + getSpace(40 - lib.length()));

			break;
		case 23:

			bkmvtiFransaBank.setPieceComptable(uap.getPieceComptableBKM());

			bkmvtiFransaBank.setNumEvenement(uap.getPieceComptableBKM()
					.substring(uap.getPieceComptableBKM().length() - 6, uap.getPieceComptableBKM().length()));

			bkmvtiFransaBank.setCodeOperation("972");
			lib = "Remboursement porteur FSBK/ TVP CONF";
			bkmvtiFransaBank.setLibelle(lib + getSpace(40 - lib.length()));

			break;

		case 24:

			bkmvtiFransaBank.setPieceComptable(uap.getPieceComptableBKM());

			bkmvtiFransaBank.setNumEvenement(uap.getPieceComptableBKM()
					.substring(uap.getPieceComptableBKM().length() - 6, uap.getPieceComptableBKM().length()));
			bkmvtiFransaBank.setCodeOperation("170");
			lib = "Remboursement porteur CONF/ TVP FSBK";
			bkmvtiFransaBank.setLibelle(lib + getSpace(40 - lib.length()));

			break;
			
		default:
			System.out.println("nothing");
		
		}
		return bkmvtiFransaBank;
	}

	public BkmvtiFransaBank TestCodeAgenceWithoutDayOperation(UAPFransaBank uap, String codeAgence, MvbkSettlement mvk,
			BkmvtiFransaBank bkmvtiFransaBank) {
		if (mvk.getCodeAgence() != null) {
			if (mvk.getCodeAgence().equals("00002")) {
				bkmvtiFransaBank.setAgence(mvk.getCodeAgence());
			}
		}

		else {
			bkmvtiFransaBank.setAgence(uap.getCodeAgence());
		}
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

	public BkmvtiFransaBank getAmount(float amountSettlement, BkmvtiFransaBank bkmvtiFransaBank) {
		int amountRound = Math.round(amountSettlement);
		String amountString = String.valueOf(amountRound);
		int AmountLengh1 = amountString.length();
		String amount = amountString.substring(0, AmountLengh1 - 2) + "," + amountString.substring(AmountLengh1 - 2);

		bkmvtiFransaBank.setMontant(getZero(20 - AmountLengh1) + amount);
		return bkmvtiFransaBank;
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
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyMMdd");
			String movingDate = LocalDate.now().format(formatter);

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
