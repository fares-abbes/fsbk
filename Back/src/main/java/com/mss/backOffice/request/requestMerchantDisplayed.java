package com.mss.backOffice.request;

import java.util.Date;


public class requestMerchantDisplayed {

private Integer merchantCode;

    private Integer merchantStatus;

    private String merchantId;

    private String merchantLibelle;

    private String city;

    private String country;

    private String codeZip;

    private String phone;
    private Date creationDate;

    private String numAccount;
    
    private String agence;
    
    private String address;
    
    private Boolean offshore;
    
    private String mcc;

	public Integer getMerchantCode() {
		return merchantCode;
	}

	public void setMerchantCode(Integer merchantCode) {
		this.merchantCode = merchantCode;
	}

	public Integer getMerchantStatus() {
		return merchantStatus;
	}

	public void setMerchantStatus(Integer merchantStatus) {
		this.merchantStatus = merchantStatus;
	}

	public String getMerchantId() {
		return merchantId;
	}

	public void setMerchantId(String merchantId) {
		this.merchantId = merchantId;
	}

	public String getMerchantLibelle() {
		return merchantLibelle;
	}

	public void setMerchantLibelle(String merchantLibelle) {
		this.merchantLibelle = merchantLibelle;
	}

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public String getCountry() {
		return country;
	}

	public void setCountry(String country) {
		this.country = country;
	}

	public String getCodeZip() {
		return codeZip;
	}

	public void setCodeZip(String codeZip) {
		this.codeZip = codeZip;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public Date getCreationDate() {
		return creationDate;
	}

	public void setCreationDate(Date creationDate) {
		this.creationDate = creationDate;
	}

	public String getNumAccount() {
		return numAccount;
	}

	public void setNumAccount(String numAccount) {
		this.numAccount = numAccount;
	}

	public String getAgence() {
		return agence;
	}

	public void setAgence(String agence) {
		this.agence = agence;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public Boolean getOffshore() {
		return offshore;
	}

	public void setOffshore(Boolean offshore) {
		this.offshore = offshore;
	}

	public String getMcc() {
		return mcc;
	}

	public void setMcc(String mcc) {
		this.mcc = mcc;
	}

	@Override
	public String toString() {
		return "requestMerchantDisplayed [merchantCode=" + merchantCode + ", merchantStatus=" + merchantStatus
				+ ", merchantId=" + merchantId + ", merchantLibelle=" + merchantLibelle + ", city=" + city
				+ ", country=" + country + ", codeZip=" + codeZip + ", phone=" + phone + ", creationDate="
				+ creationDate + ", numAccount=" + numAccount + ", agence=" + agence + ", address=" + address
				+ ", offshore=" + offshore + ", mcc=" + mcc + "]";
	}
    
    
	  
}
