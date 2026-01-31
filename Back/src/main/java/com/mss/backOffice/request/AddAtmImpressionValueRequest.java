package com.mss.backOffice.request;

public class AddAtmImpressionValueRequest {
	
	private String staticText;
	private String variableText;
	private Integer lineNum;
	private String configId;
	private String transCode;
	
	
	public String getTransCode() {
		return transCode;
	}
	public void setTransCode(String transCode) {
		this.transCode = transCode;
	}
	public String getStaticText() {
		return staticText;
	}
	public void setStaticText(String staticText) {
		this.staticText = staticText;
	}
	public String getVariableText() {
		return variableText;
	}
	public void setVariableText(String variableText) {
		this.variableText = variableText;
	}
	public Integer getLineNum() {
		return lineNum;
	}
	public void setLineNum(Integer lineNum) {
		this.lineNum = lineNum;
	}
	public String getConfigId() {
		return configId;
	}
	public void setConfigId(String configId) {
		this.configId = configId;
	}
	
}
