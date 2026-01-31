package com.mss.backOffice.Response;

public class DisplayDetailsHistory {

  public long drMinAmount;
  public String transactionCode;
  public long drMaxAmount;
  public String drAmountLimit;
  public long drNumberLimit;
  public String drTypePeriodicity;


  public long getDrMinAmount() {
    return drMinAmount;
  }

  public void setDrMinAmount(long drMinAmount) {
    this.drMinAmount = drMinAmount;
  }

  public String getTransactionCode() {
    return transactionCode;
  }

  public void setTransactionCode(String transactionCode) {
    this.transactionCode = transactionCode;
  }

  public long getDrMaxAmount() {
    return drMaxAmount;
  }

  public void setDrMaxAmount(long drMaxAmount) {
    this.drMaxAmount = drMaxAmount;
  }

  public String getDrAmountLimit() {
    return drAmountLimit;
  }

  public void setDrAmountLimit(String drAmountLimit) {
    this.drAmountLimit = drAmountLimit;
  }

  public long getDrNumberLimit() {
    return drNumberLimit;
  }

  public void setDrNumberLimit(long drNumberLimit) {
    this.drNumberLimit = drNumberLimit;
  }

 

  public String getDrTypePeriodicity() {
    return drTypePeriodicity;
  }

  public void setDrTypePeriodicity(String drTypePeriodicity) {
    this.drTypePeriodicity = drTypePeriodicity;
  }

public DisplayDetailsHistory(long drMinAmount, String transactionCode, long drMaxAmount, String drAmountLimit,
		long drNumberLimit, String drTypePeriodicity) {
	super();
	this.drMinAmount = drMinAmount;
	this.transactionCode = transactionCode;
	this.drMaxAmount = drMaxAmount;
	this.drAmountLimit = drAmountLimit;
	this.drNumberLimit = drNumberLimit;
	this.drTypePeriodicity = drTypePeriodicity;
}

}
