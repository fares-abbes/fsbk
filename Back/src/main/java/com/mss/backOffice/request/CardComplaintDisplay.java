package com.mss.backOffice.request;

import java.util.List;

import com.mss.unified.entities.Card;
import com.mss.unified.entities.CardComplaint;
public class CardComplaintDisplay {
	
	private Card card;
	private List<CardComplaint> complaints;
	private boolean isOpenedComplaint;
	private CardComplaint openedComplaint;
	
	

	public boolean getIsOpenedComplaint() {
		return isOpenedComplaint;
	}
	public void setOpenedComplaint(boolean isOpenedComplaint) {
		this.isOpenedComplaint = isOpenedComplaint;
	}
	public CardComplaint getOpenedComplaint() {
		return openedComplaint;
	}
	public void setOpenedComplaint(CardComplaint openedComplaint) {
		this.openedComplaint = openedComplaint;
	}
	public Card getCard() {
		return card;
	}
	public void setCard(Card card) {
		this.card = card;
	}
	public List<CardComplaint> getComplaints() {
		return complaints;
	}
	public void setComplaints(List<CardComplaint> complaints) {
		this.complaints = complaints;
	}

}
