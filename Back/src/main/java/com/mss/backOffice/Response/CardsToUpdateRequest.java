package com.mss.backOffice.Response;

import java.util.Date;

public class CardsToUpdateRequest {

	private int cardCode;
	private String cardNum;
	private Date expiryDate;
	private String product;
	private int productCode;
	
	
	public int getProductCode() {
		return productCode;
	}
	public void setProductCode(int productCode) {
		this.productCode = productCode;
	}
	public CardsToUpdateRequest(int cardCode, String cardNum, Date expiryDate, String product) {
		super();
		this.cardCode = cardCode;
		this.cardNum = cardNum;
		this.expiryDate = expiryDate;
		this.product = product;
	}
	public int getCardCode() {
		return cardCode;
	}
	public void setCardCode(int cardCode) {
		this.cardCode = cardCode;
	}
	public String getCardNum() {
		return cardNum;
	}
	public void setCardNum(String cardNum) {
		this.cardNum = cardNum;
	}
	public Date getExpiryDate() {
		return expiryDate;
	}
	public void setExpiryDate(Date expiryDate) {
		this.expiryDate = expiryDate;
	}
	public String getProduct() {
		return product;
	}
	public void setProduct(String product) {
		this.product = product;
	}
	
	
}
