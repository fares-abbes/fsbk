package com.mss.backOffice.request;

public class PosRequestLibelle {
	public String libelle;
	
	public String posNum;
	
	public String merchantCode;
	
	
	

	public String getMerchantCode() {
		return merchantCode;
	}

	public void setMerchantCode(String merchantCode) {
		this.merchantCode = merchantCode;
	}

	public String getLibelle() {
		return libelle;
	}

	public void setLibelle(String libelle) {
		this.libelle = libelle;
	}
	
	

	public String getPosNum() {
		return posNum;
	}

	public void setPosNum(String posNum) {
		this.posNum = posNum;
	}

	@Override
	public String toString() {
		return "PosRequestLibelle [libelle=" + libelle + "]";
	}
	
	

}
