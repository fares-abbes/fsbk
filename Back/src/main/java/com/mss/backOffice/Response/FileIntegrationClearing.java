package com.mss.backOffice.Response;

public class FileIntegrationClearing {
	boolean status;

	String message;
	StackTraceElement[] errorStack;
	String emplacement;
	String ligne;
	String stack;
	String nameComplet;
	String dateCompensation="";
	String nameCompletAmexOutgoing;
	String nomUnionPayAchat;
	public String getStack() {
		return stack;
	}

	public void setStack(String stack) {
		this.stack = stack;
	}

	public String getLigne() {
		return ligne;
	}

	public void setLigne(String ligne) {
		this.ligne = ligne;
	}

	public String getEmplacement() {
		return emplacement;
	}

	public void setEmplacement(String emplacement) {
		this.emplacement = emplacement;
	}

	public StackTraceElement[] getErrorStack() {
		return errorStack;
	}

	public void setErrorStack(StackTraceElement[] errorStack) {
		this.errorStack = errorStack;
	}

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

	public String getNameComplet() {
		return nameComplet;
	}

	public void setNameComplet(String nameComplet) {
		this.nameComplet = nameComplet;
	}

	public String getDateCompensation() {
		return dateCompensation;
	}

	public void setDateCompensation(String dateCompensation) {
		this.dateCompensation = dateCompensation;
	}

	public String getNameCompletAmexOutgoing() {
		return nameCompletAmexOutgoing;
	}

	public void setNameCompletAmexOutgoing(String nameCompletAmexOutgoing) {
		this.nameCompletAmexOutgoing = nameCompletAmexOutgoing;
	}

	public String getNomUnionPayAchat() {
		return nomUnionPayAchat;
	}

	public void setNomUnionPayAchat(String nomUnionPayAchat) {
		this.nomUnionPayAchat = nomUnionPayAchat;
	}

}
