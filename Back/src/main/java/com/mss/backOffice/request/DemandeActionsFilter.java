package com.mss.backOffice.request;

public class DemandeActionsFilter {
	private String pan;
	private String actionStatus;
	private String actionType;
	private String branchCode;
	private int status;
	private String accountNumber ;
	
    
	public String getAccountNumber() {
		return accountNumber;
	}
	public void setAccountNumber(String accountNumber) {
		this.accountNumber = accountNumber;
	}
	public int getStatus() {
		return status;
	}
	public void setStatus(int status) {
		this.status = status;
	}
	public String getBranchCode() {
		return branchCode;
	}
	public void setBranchCode(String branchCode) {
		this.branchCode = branchCode;
	}
	public String getPan() {
		return pan;
	}
	public void setPan(String pan) {
		this.pan = pan;
	}
	public String getActionStatus() {
		return actionStatus;
	}
	public void setActionStatus(String actionStatus) {
		this.actionStatus = actionStatus;
	}
	public String getActionType() {
		return actionType;
	}
	public void setActionType(String actionType) {
		this.actionType = actionType;
	}


    
    
}
