package com.mss.backOffice.request;

public class CardComplaintRequest {
	
	private String incident;
	private String comment;
	private int cardCode;
	private String branch;
	public String getIncident() {
		return incident;
	}
	public void setIncident(String incident) {
		this.incident = incident;
	}
	public String getComment() {
		return comment;
	}
	public void setComment(String comment) {
		this.comment = comment;
	}
	public int getCardCode() {
		return cardCode;
	}
	public void setCardCode(int cardCode) {
		this.cardCode = cardCode;
	}
	public String getBranch() {
		return branch;
	}
	public void setBranch(String branch) {
		this.branch = branch;
	}


}
