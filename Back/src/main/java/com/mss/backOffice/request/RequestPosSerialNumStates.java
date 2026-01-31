package com.mss.backOffice.request;


public class RequestPosSerialNumStates {

	private String SerialNum;
	
	private String model;
	
	private String Status;

	
	private Integer StatusCode;
	private String dateSaisie;
	
	private String marque;
	
	private String libellePOS;
	
	private String PosNum;
	private Integer statuRemplacement;
	
	private String merchantCode;
	private String adresse;

	private String numSim;
	private String type;
	private int typeCode;
	

	

	public int getTypeCode() {
		return typeCode;
	}

	public void setTypeCode(int typeCode) {
		this.typeCode = typeCode;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getNumSim() {
		return numSim;
	}

	public void setNumSim(String numSim) {
		this.numSim = numSim;
	}

	public String getAdresse() {
		return adresse;
	}

	public void setAdresse(String adresse) {
		this.adresse = adresse;
	}

	public String getMerchantCode() {
		return merchantCode;
	}

	public void setMerchantCode(String merchantCode) {
		this.merchantCode = merchantCode;
	}

	public Integer getStatuRemplacement() {
		return statuRemplacement;
	}

	public void setStatuRemplacement(Integer statuRemplacement) {
		this.statuRemplacement = statuRemplacement;
	}

	public String getPosNum() {
		return PosNum;
	}

	public void setPosNum(String posNum) {
		PosNum = posNum;
	}

	public String getLibellePOS() {
		return libellePOS;
	}

	public void setLibellePOS(String libellePOS) {
		this.libellePOS = libellePOS;
	}

	public String getMarque() {
		return marque;
	}

	public void setMarque(String marque) {
		this.marque = marque;
	}

	public String getSerialNum() {
		return SerialNum;
	}

	public void setSerialNum(String serialNum) {
		SerialNum = serialNum;
	}

	public String getModel() {
		return model;
	}

	public void setModel(String model) {
		this.model = model;
	}

	public String getStatus() {
		return Status;
	}

	public void setStatus(String status) {
		Status = status;
	}
	
	

	

	public Integer getStatusCode() {
		return StatusCode;
	}

	public void setStatusCode(Integer statusCode) {
		StatusCode = statusCode;
	}

	public String getDateSaisie() {
		return dateSaisie;
	}

	public void setDateSaisie(String dateSaisie) {
		this.dateSaisie = dateSaisie;
	}

	@Override
	public String toString() {
		return "RequestPosSerialNumStates [SerialNum=" + SerialNum + ", model=" + model + ", Status=" + Status
				+ ", StatusCode=" + StatusCode + ", dateSaisie=" + dateSaisie + ", marque=" + marque + ", libellePOS="
				+ libellePOS + ", PosNum=" + PosNum + ", statuRemplacement=" + statuRemplacement + "]";
	}

	
	
	

	
	
	
	
}
