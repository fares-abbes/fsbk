package com.mss.backOffice.request;

import java.util.List;

public class CroDataList {
	
	private String typeCro;
	
	private long sumValidated;
	private long nbTotalValidated;

	private long sumRejected;
	private long nbTotalRejected;
	
	
	private long sumExtra;
	private long nbTotalExtra;
	
	private List<CroData> croData;
	
	
	public List<CroData> getCroData() {
		return croData;
	}
	public void setCroData(List<CroData> croData) {
		this.croData = croData;
	}
	public String getTypeCro() {
		return typeCro;
	}
	public void setTypeCro(String typeCro) {
		this.typeCro = typeCro;
	}
	public long getSumValidated() {
		return sumValidated;
	}
	public void setSumValidated(long sumValidated) {
		this.sumValidated = sumValidated;
	}
	public long getNbTotalValidated() {
		return nbTotalValidated;
	}
	public void setNbTotalValidated(long nbTotalValidated) {
		this.nbTotalValidated = nbTotalValidated;
	}
	public long getSumRejected() {
		return sumRejected;
	}
	public void setSumRejected(long sumRejected) {
		this.sumRejected = sumRejected;
	}
	public long getNbTotalRejected() {
		return nbTotalRejected;
	}
	public void setNbTotalRejected(long nbTotalRejected) {
		this.nbTotalRejected = nbTotalRejected;
	}
	public long getSumExtra() {
		return sumExtra;
	}
	public void setSumExtra(long sumExtra) {
		this.sumExtra = sumExtra;
	}
	public long getNbTotalExtra() {
		return nbTotalExtra;
	}
	public void setNbTotalExtra(long nbTotalExtra) {
		this.nbTotalExtra = nbTotalExtra;
	}
	
	

}
