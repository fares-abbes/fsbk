package com.mss.backOffice.request;

import java.util.List;

public class CraDataList {
	
	private String typeCra;
	
	private long sumValidated;
	private long nbTotalValidated;

	private long sumRejected;
	private long nbTotalRejected;

	
	private List<CraData> craData;

	private String processingDateOpeningDay;
	


	public String getTypeCra() {
		return typeCra;
	}


	public String getProcessingDateOpeningDay() {
		return processingDateOpeningDay;
	}


	public void setProcessingDateOpeningDay(String processingDateOpeningDay) {
		this.processingDateOpeningDay = processingDateOpeningDay;
	}


	public void setTypeCra(String typeCra) {
		this.typeCra = typeCra;
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


	public List<CraData> getCraData() {
		return craData;
	}


	public void setCraData(List<CraData> craData) {
		this.craData = craData;
	}
	
	
	

}
