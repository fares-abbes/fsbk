package com.mss.backOffice.request;

public class TransactionReportingRequest {
	
	private String pan;
	private String amount;
	private String fiid;
	private String responseCode;
	private String merchantId;
	private String authCode;
	private String terminal;
	private String date;
	private String reversal;
	private String endDate;
	private String transactionCode;
	private String responseCodeIso;
	private String posEntry;
	private String switchRRN;
	private Integer chargebackStatus;

	public Integer getChargebackStatus() {
		return chargebackStatus;
	}

	public void setChargebackStatus(Integer chargebackStatus) {
		this.chargebackStatus = chargebackStatus;
	}

	public String getSwitchRRN() {
		return switchRRN;
	}

	public void setSwitchRRN(String switchRRN) {
		this.switchRRN = switchRRN;
	}

	public String getResponseCodeIso() {
		return responseCodeIso;
	}
	public void setResponseCodeIso(String responseCodeIso) {
		this.responseCodeIso = responseCodeIso;
	}
	public String getPosEntry() {
		return posEntry;
	}
	public void setPosEntry(String posEntry) {
		this.posEntry = posEntry;
	}
	public String getTransactionCode() {
		return transactionCode;
	}
	public void setTransactionCode(String transactionCode) {
		this.transactionCode = transactionCode;
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
	public String getAmount() {
		return amount;
	}
	public void setAmount(String amount) {
		this.amount = amount;
	}
	public String getFiid() {
		return fiid;
	}
	public void setFiid(String fiid) {
		this.fiid = fiid;
	}
	public String getResponseCode() {
		return responseCode;
	}
	public void setResponseCode(String responseCode) {
		this.responseCode = responseCode;
	}
	public String getMerchantId() {
		return merchantId;
	}
	public void setMerchantId(String merchantId) {
		this.merchantId = merchantId;
	}
	public String getAuthCode() {
		return authCode;
	}
	public void setAuthCode(String authCode) {
		this.authCode = authCode;
	}
	public String getTerminal() {
		return terminal;
	}
	public void setTerminal(String terminal) {
		this.terminal = terminal;
	}
	public String getDate() {
		return date;
	}
	public void setDate(String date) {
		this.date = date;
	}
	public String getReversal() {
		return reversal;
	}
	public void setReversal(String reversal) {
		this.reversal = reversal;
	}

	

}
