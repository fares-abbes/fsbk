package com.mss.backOffice.request;

public class OpposedCardsFilter {
	
	private String pan;
	private String motif;
	private String startDate;
	private String endDate;
	private String agencyCode;
	
	
	public String getAgencyCode() {
		return agencyCode;
	}
	public void setAgencyCode(String agencyCode) {
		this.agencyCode = agencyCode;
	}
	public String getStartDate() {
		return startDate;
	}
	public void setStartDate(String startDate) {
		this.startDate = startDate;
	}
	public String getEndDate() {
		return endDate;
	}
	public void setEndDate(String endDate) {
		this.endDate = endDate;
	}
	public String getPan() {
		return pan;
	}
	public void setPan(String pan) {
		this.pan = pan;
	}
	public String getMotif() {
		return motif;
	}
	public void setMotif(String motif) {
		this.motif = motif;
	}
	
	

}
