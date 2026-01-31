package com.mss.backOffice.request;

import com.mss.unified.entities.AtmFitsValue;
import java.util.List;


public class AddAtmFitApplication {
	
	 private int atmFitsApp;
	 
	 private String atmFitsAppLibelle;
	 
	private List<AtmFitsValue> atmFitsValue;

	public int getAtmFitsApp() {
		return atmFitsApp;
	}

	public void setAtmFitsApp(int atmFitsApp) {
		this.atmFitsApp = atmFitsApp;
	}

	public String getAtmFitsAppLibelle() {
		return atmFitsAppLibelle;
	}

	public void setAtmFitsAppLibelle(String atmFitsAppLibelle) {
		this.atmFitsAppLibelle = atmFitsAppLibelle;
	}

	public List<AtmFitsValue> getAtmFitsValue() {
		return atmFitsValue;
	}

	public void setAtmFitsValue(List<AtmFitsValue> atmFitsValue) {
		this.atmFitsValue = atmFitsValue;
	}
	 
	 

}
