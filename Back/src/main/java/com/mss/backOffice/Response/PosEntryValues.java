package com.mss.backOffice.Response;

public class PosEntryValues {
	
	private String positionDescription;
	private String positionValue;
	private String positionValueDescription;
	
	
	
	
	
	public PosEntryValues(String positionDescription, String positionValue, String positionValueDescription) {
		super();
		this.positionDescription = positionDescription;
		this.positionValue = positionValue;
		this.positionValueDescription = positionValueDescription;
	}
	public String getPositionDescription() {
		return positionDescription;
	}
	public void setPositionDescription(String positionDescription) {
		this.positionDescription = positionDescription;
	}
	public String getPositionValue() {
		return positionValue;
	}
	public void setPositionValue(String positionValue) {
		this.positionValue = positionValue;
	}
	public String getPositionValueDescription() {
		return positionValueDescription;
	}
	public void setPositionValueDescription(String positionValueDescription) {
		this.positionValueDescription = positionValueDescription;
	}
	
	

}
