package com.mss.backOffice.request;

import java.util.List;

public class AddAtmCurrencyMapping {
	
	private int    ecmCode;
	
	private String ecmLibelle;
	
	private List<AddCurremv> addCurremv;

	public int getEcmCode() {
		return ecmCode;
	}

	public void setEcmCode(int ecmCode) {
		this.ecmCode = ecmCode;
	}

	public String getEcmLibelle() {
		return ecmLibelle;
	}

	public void setEcmLibelle(String ecmLibelle) {
		this.ecmLibelle = ecmLibelle;
	}

	public List<AddCurremv> getAddCurremv() {
		return addCurremv;
	}

	public void setAddCurremv(List<AddCurremv> addCurremv) {
		this.addCurremv = addCurremv;
	}

	 
	
	

}
