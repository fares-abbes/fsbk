package com.mss.backOffice.request;

import java.util.Date;

public class tpeRequestGenFichier {
	private String userName;
	private String accountNumber;
	  private String adresse;
	  private String nombreTPE;
	  private Date dateCreation;
	  
	  private String status;

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getAccountNumber() {
		return accountNumber;
	}

	public void setAccountNumber(String accountNumber) {
		this.accountNumber = accountNumber;
	}

	public String getAdresse() {
		return adresse;
	}

	public void setAdresse(String adresse) {
		this.adresse = adresse;
	}

	public String getNombreTPE() {
		return nombreTPE;
	}

	public void setNombreTPE(String nombreTPE) {
		this.nombreTPE = nombreTPE;
	}

	public Date getDateCreation() {
		return dateCreation;
	}

	public void setDateCreation(Date dateCreation) {
		this.dateCreation = dateCreation;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}
	  
	  
}
