package com.mss.backOffice.request;

public class AtmLogDisplayFilter {
	private String gab;
	private String card;
	private String trx;
	private String date;
	private String rrn;
	private String isExtourne;
	
	public String getIsExtourne() {
		return isExtourne;
	}
	public void setIsExtourne(String isExtourne) {
		this.isExtourne = isExtourne;
	}
	public String getGab() {
		return gab;
	}
	public void setGab(String gab) {
		this.gab = gab;
	}
	public String getCard() {
		return card;
	}
	public void setCard(String card) {
		this.card = card;
	}
	public String getTrx() {
		return trx;
	}
	public void setTrx(String trx) {
		this.trx = trx;
	}
	public String getDate() {
		return date;
	}
	public void setDate(String date) {
		this.date = date;
	}
	public String getRrn() {
		return rrn;
	}
	public void setRrn(String rrn) {
		this.rrn = rrn;
	}
	

	@Override
	public String toString() {
		return "AtmLogDisplayFilter [gab=" + gab + ", card=" + card + ", trx=" + trx + ", date=" + date + ", rrn=" + rrn
				+ ", isExtourne=" + isExtourne + "]";
	}
	
	
}
//package com.mss.backOffice.request;
//
//public class AtmLogDisplayFilter {
//	private String atm;
//	private String card;
//	private String authCode;
//	private String dateTR;
//	
//	public String getAtm() {
//		return atm;
//	}
//	public void setAtm(String atm) {
//		this.atm = atm;
//	}
//	public String getCard() {
//		return card;
//	}
//	public void setCard(String card) {
//		this.card = card;
//	}
//	public String getAuthCode() {
//		return authCode;
//	}
//	public void setAuthCode(String authCode) {
//		this.authCode = authCode;
//	}
//	public String getDateTR() {
//		return dateTR;
//	}
//	public void setDateTR(String dateTR) {
//		this.dateTR = dateTR;
//	}
//	
//	
//	
//}
