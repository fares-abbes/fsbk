package com.mss.backOffice.request;

import com.mss.unified.entities.FileContent;

public class FileContentRequest extends FileContent {
	private String dateTransactionStart;
	private String dateTransactionEnd;
	private String dateCompensationStart;
	private String dateCompensationEnd;

	public String getDateTransactionStart() {
		return dateTransactionStart;
	}

	public void setDateTransactionStart(String dateTransactionStart) {
		this.dateTransactionStart = dateTransactionStart;
	}

	public String getDateTransactionEnd() {
		return dateTransactionEnd;
	}

	public void setDateTransactionEnd(String dateTransactionEnd) {
		this.dateTransactionEnd = dateTransactionEnd;
	}

	public String getDateCompensationStart() {
		return dateCompensationStart;
	}

	public void setDateCompensationStart(String dateCompensationStart) {
		this.dateCompensationStart = dateCompensationStart;
	}

	public String getDateCompensationEnd() {
		return dateCompensationEnd;
	}

	public void setDateCompensationEnd(String dateCompensationEnd) {
		this.dateCompensationEnd = dateCompensationEnd;
	}

	@Override
	public String toString() {
		return "FileContentRequest [dateTransactionStart=" + dateTransactionStart + ", dateTransactionEnd="
				+ dateTransactionEnd + ", dateCompensationStart=" + dateCompensationStart + ", dateCompensationEnd="
				+ dateCompensationEnd + ", getValidCommission()=" + getValidCommission() + ", getId()=" + getId() + ", getDateCompensation()="
				+ getDateCompensation() + ", getCodeDebit()=" + getCodeDebit() + ", getCodeBin()=" + getCodeBin()
				+ ", getCodeBank()=" + getCodeBank() + ", getNumRIBEmetteur()=" + getNumRIBEmetteur()
				+ ", getNumCartePorteur()=" + getNumCartePorteur() + ", getCodeDebitCommercant()="
				+ getCodeDebitCommercant() + ", getNumRIBcommercant()=" + getNumRIBcommercant() + ", getBinAcquereur()="
				+ getBinAcquereur() + ", getCodeBankAcquereur()=" + getCodeBankAcquereur() + ", getCodeAgence()="
				+ getCodeAgence() + ", getIdTerminal()=" + getIdTerminal() + ", getIdCommercant()=" + getIdCommercant()
				+ ", getTypeTransaction()=" + getTypeTransaction() + ", getHeureTransaction()=" + getHeureTransaction()
				+ ", getMontantTransaction()=" + getMontantTransaction() + ", getNumFacture()=" + getNumFacture()
				+ ", getEmetteurFacture()=" + getEmetteurFacture() + ", getNumRefTransaction()="
				+ getNumRefTransaction() + ", getNumAutorisation()=" + getNumAutorisation() + ", getCodeDebitPorteur()="
				+ getCodeDebitPorteur() + ", getCommisionPorteur()=" + getCommisionPorteur()
				+ ", getCodeDebitCommisionCommercant()=" + getCodeDebitCommisionCommercant()
				+ ", getCommisionCommercant()=" + getCommisionCommercant() + ", getCommisionInterchange()="
				+ getCommisionInterchange() + ", getFraisOperateurTechnique()=" + getFraisOperateurTechnique()
				+ ", getAppCryptogram()=" + getAppCryptogram() + ", getCryptogramInfoData()=" + getCryptogramInfoData()
				+ ", getAtc()=" + getAtc() + ", getTerminalVerificationResult()=" + getTerminalVerificationResult()
				+ ", getLibelleCommercant()=" + getLibelleCommercant() + ", getRuf()=" + getRuf()
				+ ", getNumtransaction()=" + getNumtransaction() + ", getUdf1()=" + getUdf1() + ", getRufAcquereur()="
				+ getRufAcquereur() + ", getNumTransactionPaiementInternet()=" + getNumTransactionPaiementInternet()
				+ ", getTrackId()=" + getTrackId() + ", getIdOriginTransaction()=" + getIdOriginTransaction()
				+ ", getRufpaiement()=" + getRufpaiement() + ", getDateTransaction()=" + getDateTransaction()
				+ ", getIdHeder()=" + getIdHeder() + ", toString()=" + super.toString() + ", getClass()=" + getClass()
				+ ", hashCode()=" + hashCode() + "]";
	}

}
