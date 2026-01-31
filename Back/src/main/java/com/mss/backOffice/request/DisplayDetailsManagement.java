package com.mss.backOffice.request;

import java.util.Date;

public class DisplayDetailsManagement {

  public long drMinAmount;
  public String transactionCode;

  public long drMaxAmount;
  public String drAmountLimit;
  public long drNumberLimit;
  public Date drStartDate;
  public Date drEndDate;
  public long drCurrencyAmount;
  public long drCurrencyNumber;
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

  public Date getDrStartDate() {
    return drStartDate;
  }

  public void setDrStartDate(Date drStartDate) {
    this.drStartDate = drStartDate;
  }

  public Date getDrEndDate() {
    return drEndDate;
  }

  public void setDrEndDate(Date drEndDate) {
    this.drEndDate = drEndDate;
  }

  public long getDrCurrencyAmount() {
    return drCurrencyAmount;
  }

  public void setDrCurrencyAmount(long drCurrencyAmount) {
    this.drCurrencyAmount = drCurrencyAmount;
  }

  public long getDrCurrencyNumber() {
    return drCurrencyNumber;
  }

  public void setDrCurrencyNumber(long drCurrencyNumber) {
    this.drCurrencyNumber = drCurrencyNumber;
  }

  public String getDrTypePeriodicity() {
    return drTypePeriodicity;
  }

  public void setDrTypePeriodicity(String drTypePeriodicity) {
    this.drTypePeriodicity = drTypePeriodicity;
  }


  public DisplayDetailsManagement( long drMinAmount,
      String transactionCode, long drMaxAmount, String drAmountLimit, long drNumberLimit,
      Date drStartDate, Date drEndDate, long drCurrencyAmount, long drCurrencyNumber,
      String drTypePeriodicity) {
    this.drMinAmount = drMinAmount;
    this.transactionCode = transactionCode;
    this.drMaxAmount = drMaxAmount;
    this.drAmountLimit = drAmountLimit;
    this.drNumberLimit = drNumberLimit;
    this.drStartDate = drStartDate;
    this.drEndDate = drEndDate;
    this.drCurrencyAmount = drCurrencyAmount;
    this.drCurrencyNumber = drCurrencyNumber;
    this.drTypePeriodicity = drTypePeriodicity;
  }

  @Override
  public String toString() {
    return "DisplayDetailsManagement{" +
        "drMinAmount=" + drMinAmount +
        ", transactionCode='" + transactionCode + '\'' +
        ", drMaxAmount=" + drMaxAmount +
        ", drAmountLimit='" + drAmountLimit + '\'' +
        ", drNumberLimit=" + drNumberLimit +
        ", drStartDate=" + drStartDate +
        ", drEndDate=" + drEndDate +
        ", drCurrencyAmount=" + drCurrencyAmount +
        ", drCurrencyNumber=" + drCurrencyNumber +
        ", drTypePeriodicity='" + drTypePeriodicity + '\'' +
        '}';
  }
}
