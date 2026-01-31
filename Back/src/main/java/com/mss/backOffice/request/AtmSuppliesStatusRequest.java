package com.mss.backOffice.request;

public class AtmSuppliesStatusRequest {

	
	private String cardCaptureBin;
	private String cashHandlerRejectBin;
	private String depositBin;
	private String receiptPaper;
	private String journalPaper;
	private String nightSafe;
	private String cassette1;
	private String cassette2;
	private String cassette3;
	private String cassette4;
	
	private String statementPaper;
	private String statementRibbon;
	private String envelopeDispenser;
	public String getCardCaptureBin() {
		return cardCaptureBin;
	}
	public void setCardCaptureBin(String cardCaptureBin) {
		this.cardCaptureBin = cardCaptureBin;
	}
	public String getCashHandlerRejectBin() {
		return cashHandlerRejectBin;
	}
	public void setCashHandlerRejectBin(String cashHandlerRejectBin) {
		this.cashHandlerRejectBin = cashHandlerRejectBin;
	}
	public String getDepositBin() {
		return depositBin;
	}
	public void setDepositBin(String depositBin) {
		this.depositBin = depositBin;
	}
	public String getReceiptPaper() {
		return receiptPaper;
	}
	public void setReceiptPaper(String receiptPaper) {
		this.receiptPaper = receiptPaper;
	}
	public String getJournalPaper() {
		return journalPaper;
	}
	public void setJournalPaper(String journalPaper) {
		this.journalPaper = journalPaper;
	}
	public String getNightSafe() {
		return nightSafe;
	}
	public void setNightSafe(String nightSafe) {
		this.nightSafe = nightSafe;
	}
	public String getCassette1() {
		return cassette1;
	}
	public void setCassette1(String cassette1) {
		this.cassette1 = cassette1;
	}
	public String getCassette2() {
		return cassette2;
	}
	public void setCassette2(String cassette2) {
		this.cassette2 = cassette2;
	}
	public String getCassette3() {
		return cassette3;
	}
	public void setCassette3(String cassette3) {
		this.cassette3 = cassette3;
	}
	public String getCassette4() {
		return cassette4;
	}
	public void setCassette4(String cassette4) {
		this.cassette4 = cassette4;
	}
	public String getStatementPaper() {
		return statementPaper;
	}
	public void setStatementPaper(String statementPaper) {
		this.statementPaper = statementPaper;
	}
	public String getStatementRibbon() {
		return statementRibbon;
	}
	public void setStatementRibbon(String statementRibbon) {
		this.statementRibbon = statementRibbon;
	}
	public String getEnvelopeDispenser() {
		return envelopeDispenser;
	}
	public void setEnvelopeDispenser(String envelopeDispenser) {
		this.envelopeDispenser = envelopeDispenser;
	}
	@Override
	public String toString() {
		return "AtmSuppliesStatusRequest [cardCaptureBin=" + cardCaptureBin + ", cashHandlerRejectBin="
				+ cashHandlerRejectBin + ", depositBin=" + depositBin + ", receiptPaper=" + receiptPaper
				+ ", journalPaper=" + journalPaper + ", nightSafe=" + nightSafe + ", cassette1=" + cassette1
				+ ", cassette2=" + cassette2 + ", cassette3=" + cassette3 + ", cassette4=" + cassette4
				+ ", statementPaper=" + statementPaper + ", statementRibbon=" + statementRibbon + ", envelopeDispenser="
				+ envelopeDispenser + "]";
	}
	
	
	
}
