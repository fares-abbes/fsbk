package com.mss.backOffice.request;

import java.util.List;

public class ChangeAccountRequest {
	private String oldAccountNumber;
	private String accountNumber;
	private String currency;
	private Integer branchCode;
	private String accountName;
	private String radical;
	private String cardCode;
	private int allCards;
	private List<Integer> cards;
	
	
	public String getRadical() {
		return radical;
	}
	public void setRadical(String radical) {
		this.radical = radical;
	}
	public String getAccountName() {
		return accountName;
	}
	public void setAccountName(String accountName) {
		this.accountName = accountName;
	}
	public Integer getBranchCode() {
		return branchCode;
	}
	public void setBranchCode(Integer branchCode) {
		this.branchCode = branchCode;
	}
	public String getCurrency() {
		return currency;
	}
	public void setCurrency(String currency) {
		this.currency = currency;
	}
	public String getOldAccountNumber() {
		return oldAccountNumber;
	}
	public void setOldAccountNumber(String oldAccountNumber) {
		this.oldAccountNumber = oldAccountNumber;
	}
	public String getAccountNumber() {
		return accountNumber;
	}
	public void setAccountNumber(String accountNumber) {
		this.accountNumber = accountNumber;
	}
	public String getCardCode() {
		return cardCode;
	}
	public void setCardCode(String cardCode) {
		this.cardCode = cardCode;
	}
	public int getAllCards() {
		return allCards;
	}
	public void setAllCards(int allCards) {
		this.allCards = allCards;
	}
	public List<Integer> getCards() {
		return cards;
	}
	public void setCards(List<Integer> cards) {
		this.cards = cards;
	}
	

 
}

