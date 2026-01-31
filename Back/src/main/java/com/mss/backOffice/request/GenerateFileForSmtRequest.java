package com.mss.backOffice.request;

import java.util.List;

public class GenerateFileForSmtRequest {
	
	
	private List<Integer> cardCodes;
	private String operationType;
	
	
	

	public String getOperationType() {
		return operationType;
	}

	public void setOperationType(String operationType) {
		this.operationType = operationType;
	}

	public List<Integer> getCardCodes() {
		return cardCodes;
	}

	public void setCardCodes(List<Integer> cardCodes) {
		this.cardCodes = cardCodes;
	}
	

}
