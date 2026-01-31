package com.mss.backOffice.Response;

public class CurrencyDto {
	
	private String label;
	private String code;
	
	
	public CurrencyDto(String label, String code) {
		super();
		this.label = label;
		this.code = code;
	}
	public String getLabel() {
		return label;
	}
	public void setLabel(String label) {
		this.label = label;
	}
	public String getCode() {
		return code;
	}
	public void setCode(String code) {
		this.code = code;
	}
	

}
