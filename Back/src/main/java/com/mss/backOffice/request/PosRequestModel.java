package com.mss.backOffice.request;

public class PosRequestModel {
	public int codeModel;
	public String libelle;
	public String marque;
	public String codeType;
	public int getCodeModel() {
		return codeModel;
	}
	public void setCodeModel(int codeModel) {
		this.codeModel = codeModel;
	}
	public String getLibelle() {
		return libelle;
	}
	public void setLibelle(String libelle) {
		this.libelle = libelle;
	}
	public String getMarque() {
		return marque;
	}
	public void setMarque(String marque) {
		this.marque = marque;
	}
	public String getCodeType() {
		return codeType;
	}
	public void setCodeType(String codeType) {
		this.codeType = codeType;
	}
	@Override
	public String toString() {
		return "PosRequestModel [codeModel=" + codeModel + ", libelle=" + libelle + ", marque=" + marque + ", codeType="
				+ codeType + "]";
	}
	
	
	

}
