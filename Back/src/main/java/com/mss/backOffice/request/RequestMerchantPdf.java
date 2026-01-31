package com.mss.backOffice.request;

import java.util.Date;

public class RequestMerchantPdf {
	private String accountNumber;
	  private Date creationDate;
	private String etatMerchant;
	  private String idContrat;
	private String nameMerchant;
	private String nbPOS;
	
	
	public String getIdContrat() {
		return idContrat;
	}
	public void setIdContrat(String idContrat) {
		this.idContrat = idContrat;
	}
	public String getAccountNumber() {
		return accountNumber;
	}
	public void setAccountNumber(String accountNumber) {
		this.accountNumber = accountNumber;
	}

	

	public Date getCreationDate() {
		return creationDate;
	}
	public void setCreationDate(Date creationDate) {
		this.creationDate = creationDate;
	}
	public String getEtatMerchant() {
		return etatMerchant;
	}
	public void setEtatMerchant(String etatMerchant) {
		this.etatMerchant = etatMerchant;
	}

	public String getNameMerchant() {
		return nameMerchant;
	}
	public void setNameMerchant(String nameMerchant) {
		this.nameMerchant = nameMerchant;
	}
	public String getNbPOS() {
		return nbPOS;
	}
	public void setNbPOS(String nbPOS) {
		this.nbPOS = nbPOS;
	}
	

}
