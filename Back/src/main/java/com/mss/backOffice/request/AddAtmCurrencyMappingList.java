package com.mss.backOffice.request;

import com.mss.unified.entities.AtmEmvCurr;
import java.util.List;

public class AddAtmCurrencyMappingList {
	
	
	private int ecmCode;

	private String ecmLibelle;
	
	private List<AtmEmvCurr> atmEmvCurr;

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

	public List<AtmEmvCurr> getAtmEmvCurr() {
		return atmEmvCurr;
	}

	public void setAtmEmvCurr(List<AtmEmvCurr> atmEmvCurr) {
		this.atmEmvCurr = atmEmvCurr;
	}
	
	
	
	
}
