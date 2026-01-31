package com.mss.backOffice.request;

public class CardReportingRequest {
	private String name;
	private String pan;
	private String cardStatus;
	private String agencyCode;
	private String address;
	private String accountNum;
	private String startDate;
	private String modifDate;
	private String expiryDate;
	private String endDate;
	private String radical;
	private Boolean isFromMobile;

	
	
	public String getRadical() {
		return radical;
	}
	public void setRadical(String radical) {
		this.radical = radical;
	}
	public String getEndDate() {
		return endDate;
	}
	public void setEndDate(String endDate) {
		this.endDate = endDate;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getPan() {
		return pan;
	}
	public void setPan(String pan) {
		this.pan = pan;
	}
	public String getCardStatus() {
		return cardStatus;
	}
	public void setCardStatus(String cardStatus) {
		this.cardStatus = cardStatus;
	}
	public String getAgencyCode() {
		return agencyCode;
	}
	public void setAgencyCode(String agencyCode) {
		this.agencyCode = agencyCode;
	}
	public String getAddress() {
		return address;
	}
	public void setAddress(String address) {
		this.address = address;
	}
	public String getAccountNum() {
		return accountNum;
	}
	public void setAccountNum(String accountNum) {
		this.accountNum = accountNum;
	}
	public String getStartDate() {
		return startDate;
	}
	public void setStartDate(String startDate) {
		this.startDate = startDate;
	}
	public String getModifDate() {
		return modifDate;
	}
	public void setModifDate(String modifDate) {
		this.modifDate = modifDate;
	}
	public String getExpiryDate() {
		return expiryDate;
	}
	public void setExpiryDate(String expiryDate) {
		this.expiryDate = expiryDate;
	}
	
	public Boolean getIsFromMobile() {
		return isFromMobile;
	}
	public void setIsFromMobile(Boolean isFromMobile) {
		this.isFromMobile = isFromMobile;
	}
	@Override
	public String toString() {
		return "CardReportingRequest [name=" + name + ", pan=" + pan + ", cardStatus=" + cardStatus + ", agencyCode="
				+ agencyCode + ", address=" + address + ", accountNum=" + accountNum + ", startDate=" + startDate
				+ ", modifDate=" + modifDate + ", expiryDate=" + expiryDate + "]";
	}
	
	
	
	
}
