package com.mss.backOffice.request;

public class GenerateCardPrep {
	private int product;
	private int account;
	
	public GenerateCardPrep() {
		super();
		// TODO Auto-generated constructor stub
	}

	public int getProduct() {
		return product;
	}

	public void setProduct(int product) {
		this.product = product;
	}

	public int getAccount() {
		return account;
	}
	public void setAccount(int account) {
		this.account = account;
	}

	@Override
	public String toString() {
		return "GenerateCardPrep{" +
				"product=" + product +
				", account=" + account +
				'}';
	}
}
