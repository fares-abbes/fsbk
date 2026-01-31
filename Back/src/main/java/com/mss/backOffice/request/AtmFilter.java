package com.mss.backOffice.request;

public class AtmFilter {
	private String cardNumber;
	private String mti;
	private String terminalId;
	private String responseCode;
	public String getCardNumber() {
		return cardNumber;
	}
	public void setCardNumber(String cardNumber) {
		this.cardNumber = cardNumber;
	}
	public String getMti() {
		return mti;
	}
	public void setMti(String mti) {
		this.mti = mti;
	}

	public String getTerminalId() {
		return terminalId;
	}
	public void setTerminalId(String terminalId) {
		this.terminalId = terminalId;
	}
	public String getResponseCode() {
		return responseCode;
	}
	public void setResponseCode(String responseCode) {
		this.responseCode = responseCode;
	}


}
