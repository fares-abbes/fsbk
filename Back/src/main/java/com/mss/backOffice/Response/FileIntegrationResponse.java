package com.mss.backOffice.Response;

public class FileIntegrationResponse {
	boolean status;
	int nbCancellation;
	int nbCreation;
	int nbModification;
	int nbRenewal;
	String message;
	
	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public boolean isStatus() {
		return status;
	}

	public void setStatus(boolean status) {
		this.status = status;
	}

	public int getNbCancellation() {
		return nbCancellation;
	}

	public void setNbCancellation(int nbCancellation) {
		this.nbCancellation = nbCancellation;
	}

	public int getNbCreation() {
		return nbCreation;
	}

	public void setNbCreation(int nbCreation) {
		this.nbCreation = nbCreation;
	}

	public int getNbModification() {
		return nbModification;
	}

	public void setNbModification(int nbModification) {
		this.nbModification = nbModification;
	}

	public int getNbRenewal() {
		return nbRenewal;
	}

	public void setNbRenewal(int nbRenewal) {
		this.nbRenewal = nbRenewal;
	}

}
