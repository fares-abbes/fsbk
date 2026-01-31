package com.mss.backOffice.request;

import java.util.List;

public class ActionCardRequest {
	
	private List<Integer> cardsForRenewel;
	private List<CardRenewelRequest> cardsForReplacement;
	private List<Integer> cardsForNonRenewel;
	private List<CardReeditionPinRequest> cardsForPinChange;
	
	

	public List<CardReeditionPinRequest> getCardsForPinChange() {
		return cardsForPinChange;
	}
	public void setCardsForPinChange(List<CardReeditionPinRequest> cardsForPinChange) {
		this.cardsForPinChange = cardsForPinChange;
	}
	public List<Integer> getCardsForRenewel() {
		return cardsForRenewel;
	}
	public void setCardsForRenewel(List<Integer> cardsForRenewel) {
		this.cardsForRenewel = cardsForRenewel;
	}
	public List<CardRenewelRequest> getCardsForReplacement() {
		return cardsForReplacement;
	}
	public void setCardsForReplacement(List<CardRenewelRequest> cardsForReplacement) {
		this.cardsForReplacement = cardsForReplacement;
	}
	public List<Integer> getCardsForNonRenewel() {
		return cardsForNonRenewel;
	}
	public void setCardsForNonRenewel(List<Integer> cardsForNonRenewel) {
		this.cardsForNonRenewel = cardsForNonRenewel;
	}
	
//	public List<Integer> getCardsForReplacement() {
//		return cardsForReplacement;
//	}
//	public void setCardsForReplacement(List<Integer> cardsForReplacement) {
//		this.cardsForReplacement = cardsForReplacement;
//	}

}
