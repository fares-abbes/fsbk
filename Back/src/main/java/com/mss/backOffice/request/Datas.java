package com.mss.backOffice.request;

public class Datas {

	private String identifier;

	private String compteDebit ;
	private String compteCredit;
	private String pieceBkm;
	public String getIdentifier() {
		return identifier;
	}
	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}
	public String getCompteDebit() {
		return compteDebit;
	}
	public void setCompteDebit(String compteDebit) {
		this.compteDebit = compteDebit;
	}
	public String getCompteCredit() {
		return compteCredit;
	}
	public void setCompteCredit(String compteCredit) {
		this.compteCredit = compteCredit;
	}
	public String getPieceBkm() {
		return pieceBkm;
	}
	public void setPieceBkm(String pieceBkm) {
		this.pieceBkm = pieceBkm;
	}
	public Datas(String identifier, String compteDebit, String compteCredit, String pieceBkm) {
		super();
		this.identifier = identifier;
		this.compteDebit = compteDebit;
		this.compteCredit = compteCredit;
		this.pieceBkm = pieceBkm;
	}
}