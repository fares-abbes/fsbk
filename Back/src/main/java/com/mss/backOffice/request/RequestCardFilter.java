package com.mss.backOffice.request;

import java.util.Date;

public class RequestCardFilter {
	private String accountNumber;
	private String  creationDate;
	private int status;
	public String Cin;
	private String branch;
	
	
	
	public String getBranch() {
		return branch;
	}
	public void setBranch(String branch) {
		this.branch = branch;
	}
	public String getCin() {
		return Cin;
	}
	public void setCin(String cin) {
		this.Cin = cin;
	}
	public int getStatus() {
		return status;
	}
	public void setStatus(int status) {
		this.status = status;
	}
	public String getAccountNumber() {
		return accountNumber;
	}
	public void setAccountNumber(String accountNumber) {
		this.accountNumber = accountNumber;
	}
	public String getCreationDate() {
		return creationDate;
	}
	public void setCreationDate(String creationDate) {
		this.creationDate = creationDate;
	}

	@Override
	public String toString() {
		return "RequestCardFilter{" +
				"accountNumber='" + accountNumber + '\'' +
				", creationDate='" + creationDate + '\'' +
				", status=" + status +
				", Cin='" + Cin + '\'' +
				'}';
	}
}
