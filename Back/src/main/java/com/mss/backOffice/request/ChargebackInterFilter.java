package com.mss.backOffice.request;

public class ChargebackInterFilter {
	private String cardNumber;
	private String de42;
	private String date;
	private boolean inProgress;
	private boolean done;
	private boolean accepted;
	private boolean na;
	private boolean rejected;

	private String agencyCode;
	private String operationCode;
	private String accountNum;
	private String type;

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public boolean isRejected() {
		return rejected;
	}
	public void setRejected(boolean rejected) {
		this.rejected = rejected;
	}
	public String getCardNumber() {
		return cardNumber;
	}
	public void setCardNumber(String cardNumber) {
		this.cardNumber = cardNumber;
	}
	public String getDe42() {
		return de42;
	}
	public void setDe42(String de42) {
		this.de42 = de42;
	}
	public String getDate() {
		return date;
	}
	public void setDate(String date) {
		this.date = date;
	}
	public boolean isInProgress() {
		return inProgress;
	}
	public void setInProgress(boolean inProgress) {
		this.inProgress = inProgress;
	}
	public boolean isDone() {
		return done;
	}
	public void setDone(boolean done) {
		this.done = done;
	}
	public boolean isAccepted() {
		return accepted;
	}
	public void setAccepted(boolean accepted) {
		this.accepted = accepted;
	}
	public boolean isNa() {
		return na;
	}
	public void setNa(boolean na) {
		this.na = na;
	}

	public String getAgencyCode() {
		return agencyCode;
	}

	public void setAgencyCode(String agencyCode) {
		this.agencyCode = agencyCode;
	}
	public String getOperationCode() {
		return operationCode;
	}
	public void setOperationCode(String operationCode) {
		this.operationCode = operationCode;
	}
	public String getAccountNum() {
		return accountNum;
	}
	public void setAccountNum(String accountNum) {
		this.accountNum = accountNum;
	}
	
}
