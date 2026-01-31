package com.mss.backOffice.request;

public class McdIncomingFilter {
	String cardNumber;
	String rrn;
	String merchantId;
	String operation;
	String matching;

	public String getMatching() {
		return matching;
	}

	public void setMatching(String matching) {
		this.matching = matching;
	}

	public String getCardNumber() {
		return cardNumber;
	}
	public void setCardNumber(String cardNumber) {
		this.cardNumber = cardNumber;
	}
	public String getRrn() {
		return rrn;
	}
	public void setRrn(String rrn) {
		this.rrn = rrn;
	}
	public String getMerchantId() {
		return merchantId;
	}
	public void setMerchantId(String merchantId) {
		this.merchantId = merchantId;
	}
	public String getOperation() {
		return operation;
	}
	public void setOperation(String operation) {
		this.operation = operation;
	}
	
}
