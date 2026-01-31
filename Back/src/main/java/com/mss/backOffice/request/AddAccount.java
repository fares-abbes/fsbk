package com.mss.backOffice.request;

import java.util.Date;

import javax.persistence.Column;

public class AddAccount {
	
	  private String globalRiskMangCode;

	 
	  private String accountNum;


	
	  private int astCode;

	 
	  private String customerCode;
	
	  private String accountNumAttached;
	
	  private String accountName;


	  private int accountAvailable;

	  private int accountBilling;
	  private int accountAuthorize;
	  private int accountRevolvingLimit;
	  private String currency;

	  public int libelleBin;
	  private Date creationDate;
	  
	public String getGlobalRiskMangCode() {
		return globalRiskMangCode;
	}
	public void setGlobalRiskMangCode(String globalRiskMangCode) {
		this.globalRiskMangCode = globalRiskMangCode;
	}
	public String getAccountNum() {
		return accountNum;
	}
	public void setAccountNum(String accountNum) {
		this.accountNum = accountNum;
	}
	public int getAstCode() {
		return astCode;
	}
	public void setAstCode(int astCode) {
		this.astCode = astCode;
	}
	public String getCustomerCode() {
		return customerCode;
	}
	public void setCustomerCode(String customerCode) {
		this.customerCode = customerCode;
	}
	public String getAccountNumAttached() {
		return accountNumAttached;
	}
	public void setAccountNumAttached(String accountNumAttached) {
		this.accountNumAttached = accountNumAttached;
	}
	public String getAccountName() {
		return accountName;
	}
	public void setAccountName(String accountName) {
		this.accountName = accountName;
	}
	public int getAccountAvailable() {
		return accountAvailable;
	}
	public void setAccountAvailable(int accountAvailable) {
		this.accountAvailable = accountAvailable;
	}
	public int getAccountBilling() {
		return accountBilling;
	}
	public void setAccountBilling(int accountBilling) {
		this.accountBilling = accountBilling;
	}
	public int getAccountAuthorize() {
		return accountAuthorize;
	}
	public void setAccountAuthorize(int accountAuthorize) {
		this.accountAuthorize = accountAuthorize;
	}
	public int getAccountRevolvingLimit() {
		return accountRevolvingLimit;
	}
	public void setAccountRevolvingLimit(int accountRevolvingLimit) {
		this.accountRevolvingLimit = accountRevolvingLimit;
	}
	public String getCurrency() {
		return currency;
	}
	public void setCurrency(String currency) {
		this.currency = currency;
	}
	public int getLibelleBin() {
		return libelleBin;
	}
	public void setLibelleBin(int libelleBin) {
		this.libelleBin = libelleBin;
	}
	public Date getCreationDate() {
		return creationDate;
	}
	public void setCreationDate(Date creationDate) {
		this.creationDate = creationDate;
	}

	@Override
	public String toString() {
		return "AddAccount{" +
				"globalRiskMangCode='" + globalRiskMangCode + '\'' +
				", accountNum='" + accountNum + '\'' +
				", astCode=" + astCode +
				", customerCode='" + customerCode + '\'' +
				", accountNumAttached='" + accountNumAttached + '\'' +
				", accountName='" + accountName + '\'' +
				", accountAvailable=" + accountAvailable +
				", accountBilling=" + accountBilling +
				", accountAuthorize=" + accountAuthorize +
				", accountRevolvingLimit=" + accountRevolvingLimit +
				", currency='" + currency + '\'' +
				", libelleBin=" + libelleBin +
				", creationDate=" + creationDate +
				'}';
	}
}
