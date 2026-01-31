package com.mss.backOffice.Response;

import java.util.List;

import com.mss.unified.entities.Account;
import com.mss.unified.entities.Customer;

public class UpdateAccountDisplay {
	
	private Account account;
	private Customer customer;
	private List <CardsToUpdateRequest> card;
	
	public Account getAccount() {
		return account;
	}
	public void setAccount(Account account) {
		this.account = account;
	}
	public Customer getCustomer() {
		return customer;
	}
	public void setCustomer(Customer customer) {
		this.customer = customer;
	}
	public  List <CardsToUpdateRequest> getCard() {
		return card;
	}
	public void setCard( List <CardsToUpdateRequest> card) {
		this.card = card;
	}
	
	
}
