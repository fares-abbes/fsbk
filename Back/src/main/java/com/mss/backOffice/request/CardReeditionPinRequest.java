package com.mss.backOffice.request;

public class CardReeditionPinRequest {
	
	private int cardCode;
	private String motif;


	public int getCardCode() {
		return cardCode;
	}
	public void setCardCode(int cardCode) {
		this.cardCode = cardCode;
	}
	public String getMotif() {
		return motif;
	}
	public void setMotif(String motif) {
		this.motif = motif;
	}
}
