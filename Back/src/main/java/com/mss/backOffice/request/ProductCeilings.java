package com.mss.backOffice.request;


public class ProductCeilings {

	private int cprRiskAmountMin;
	private int cprRiskAmountMax;
	private int cprRiskAmount;
	private int glNumber;
	private int cprPurchaseMax;
    private int cprWithdrawalMax;
    private int cprEcommerceMax;

    
    
	public int getCprEcommerceMax() {
		return cprEcommerceMax;
	}
	public void setCprEcommerceMax(int cprEcommerceMax) {
		this.cprEcommerceMax = cprEcommerceMax;
	}
	public int getCprPurchaseMax() {
		return cprPurchaseMax;
	}
	public void setCprPurchaseMax(int cprPurchaseMax) {
		this.cprPurchaseMax = cprPurchaseMax;
	}
	public int getCprWithdrawalMax() {
		return cprWithdrawalMax;
	}
	public void setCprWithdrawalMax(int cprWithdrawalMax) {
		this.cprWithdrawalMax = cprWithdrawalMax;
	}
	public int getCprRiskAmountMin() {
		return cprRiskAmountMin;
	}
	public void setCprRiskAmountMin(int cprRiskAmountMin) {
		this.cprRiskAmountMin = cprRiskAmountMin;
	}
	public int getCprRiskAmountMax() {
		return cprRiskAmountMax;
	}
	public void setCprRiskAmountMax(int cprRiskAmountMax) {
		this.cprRiskAmountMax = cprRiskAmountMax;
	}
	public int getCprRiskAmount() {
		return cprRiskAmount;
	}
	public void setCprRiskAmount(int cprRiskAmount) {
		this.cprRiskAmount = cprRiskAmount;
	}
	public int getGlNumber() {
		return glNumber;
	}
	public void setGlNumber(int glNumber) {
		this.glNumber = glNumber;
	}
	
	
}
