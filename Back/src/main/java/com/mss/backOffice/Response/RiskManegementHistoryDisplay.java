package com.mss.backOffice.Response;

import java.util.List;

public class RiskManegementHistoryDisplay {

	private long glMinAmount;
	private long glMaxAmount;
	private String globalAmount;
	private long globalNumber;
	private String currency;

	private String glPeriodicityType;
	private List<DisplayDetailsHistory> detailsRequestList;
	public long getGlMinAmount() {
		return glMinAmount;
	}
	public void setGlMinAmount(long glMinAmount) {
		this.glMinAmount = glMinAmount;
	}
	public long getGlMaxAmount() {
		return glMaxAmount;
	}
	public void setGlMaxAmount(long glMaxAmount) {
		this.glMaxAmount = glMaxAmount;
	}
	public String getGlobalAmount() {
		return globalAmount;
	}
	public void setGlobalAmount(String globalAmount) {
		this.globalAmount = globalAmount;
	}
	public long getGlobalNumber() {
		return globalNumber;
	}
	public void setGlobalNumber(long globalNumber) {
		this.globalNumber = globalNumber;
	}
	public String getGlPeriodicityType() {
		return glPeriodicityType;
	}
	public void setGlPeriodicityType(String glPeriodicityType) {
		this.glPeriodicityType = glPeriodicityType;
	}
	public List<DisplayDetailsHistory> getDetailsRequestList() {
		return detailsRequestList;
	}
	public void setDetailsRequestList(List<DisplayDetailsHistory> detailsRequestList) {
		this.detailsRequestList = detailsRequestList;
	}
	
	public String getCurrency() {
		return currency;
	}
	public void setCurrency(String currency) {
		this.currency = currency;
	}
	public RiskManegementHistoryDisplay(long glMinAmount, long glMaxAmount, String globalAmount, long globalNumber,
			String glPeriodicityType) {
		super();
		this.glMinAmount = glMinAmount;
		this.glMaxAmount = glMaxAmount;
		this.globalAmount = globalAmount;
		this.globalNumber = globalNumber;
		this.glPeriodicityType = glPeriodicityType;
		
	}

}
