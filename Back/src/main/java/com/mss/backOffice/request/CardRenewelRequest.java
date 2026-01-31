package com.mss.backOffice.request;

public class CardRenewelRequest {
	
	private int cardCode;
	private String motif;
	private int replacementPin;
	private String nameInCard;
	

	public String getNameInCard() {
		return nameInCard;
	}
	public void setNameInCard(String nameInCard) {
		this.nameInCard = nameInCard;
	}
	public int getReplacementPin() {
		return replacementPin;
	}
	public void setReplacementPin(int replacementPin) {
		this.replacementPin = replacementPin;
	}
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
