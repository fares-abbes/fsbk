package com.mss.backOffice.request;

public class PosServiceDetailsForServicesConf {
	private int ps_code;
	private String card_type;
	private String purchaise_floor_limit;
	private String cashadvance_floor_limit;
	private String mail_floor_limit;
	private String transaction_limit;
	private String transaction_profile;
	public int getPs_code() {
		return ps_code;
	}
	public void setPs_code(int ps_code) {
		this.ps_code = ps_code;
	}
	public String getCard_type() {
		return card_type;
	}
	public void setCard_type(String card_type) {
		this.card_type = card_type;
	}
	public String getPurchaise_floor_limit() {
		return purchaise_floor_limit;
	}
	public void setPurchaise_floor_limit(String purchaise_floor_limit) {
		this.purchaise_floor_limit = purchaise_floor_limit;
	}
	public String getCashadvance_floor_limit() {
		return cashadvance_floor_limit;
	}
	public void setCashadvance_floor_limit(String cashadvance_floor_limit) {
		this.cashadvance_floor_limit = cashadvance_floor_limit;
	}
	public String getMail_floor_limit() {
		return mail_floor_limit;
	}
	public void setMail_floor_limit(String mail_floor_limit) {
		this.mail_floor_limit = mail_floor_limit;
	}
	public String getTransaction_limit() {
		return transaction_limit;
	}
	public void setTransaction_limit(String transaction_limit) {
		this.transaction_limit = transaction_limit;
	}
	public String getTransaction_profile() {
		return transaction_profile;
	}
	public void setTransaction_profile(String transaction_profile) {
		this.transaction_profile = transaction_profile;
	}
	
	
}
