package com.mss.backOffice.request;

import java.io.Serializable;
import java.util.Date;

public class SwitchDisplay{

  private Integer switchCode;
  private String mti;
  private String pan;
  private String amount;
  private String currency;
  private String rrn;
  private Date requestDate;
  private Date responseDate;

  private String responseCode;
  private String authNumber;
  private String acceptorMerchantCode;
  private String arpc;
  private String ReplacementsAmount;
  private String PosEntryMode;
  private String tvr;
  private String cvr;

  private String processingCode;

  public String getProcessingCode() {
    return processingCode;
  }

  public void setProcessingCode(String processingCode) {
    this.processingCode = processingCode;
  }

  public SwitchDisplay(Integer switchCode, String mti, String pan, String amount,
      String currency, String appTransCounter, Date requestDate, String responseCode,
      Date responseDate,
      String authNumber, String acceptorMerchantCode, String arpc,
      String replacementsAmount, String posEntryMode, String tvr,String processingCode) {
    this.switchCode = switchCode;
    this.mti = mti;
    this.pan = pan;
    this.amount = amount;
    this.currency = currency;
    this.rrn = appTransCounter;
    this.requestDate = requestDate;
    this.responseCode = responseCode;
    this.responseDate = responseDate;
    this.authNumber = authNumber;
    this.acceptorMerchantCode = acceptorMerchantCode;
    this.arpc = arpc;
    this.ReplacementsAmount = replacementsAmount;
    this.PosEntryMode = posEntryMode;
    this.tvr = tvr;
    this.processingCode=processingCode;


  }



  public Integer getSwitchCode() {
    return switchCode;
  }

  public void setSwitchCode(Integer switchCode) {
    this.switchCode = switchCode;
  }

  public String getMti() {
    return mti;
  }

  public void setMti(String mti) {
    this.mti = mti;
  }

  public String getPan() {
    return pan;
  }

  public void setPan(String pan) {
    this.pan = pan;
  }

  public String getAmount() {
    return amount;
  }

  public void setAmount(String amount) {
    this.amount = amount;
  }

  public String getCurrency() {
    return currency;
  }

  public void setCurrency(String currency) {
    this.currency = currency;
  }

  public String getRrn() {
    return rrn;
  }

  public void setRrn(String rrn) {
    this.rrn = rrn;
  }


  public String getResponseCode() {
    return responseCode;
  }

  public void setResponseCode(String responseCode) {
    this.responseCode = responseCode;
  }


  public String getAuthNumber() {
    return authNumber;
  }

  public void setAuthNumber(String authNumber) {
    this.authNumber = authNumber;
  }

  public String getAcceptorMerchantCode() {
    return acceptorMerchantCode;
  }

  public void setAcceptorMerchantCode(String acceptorMerchantCode) {
    this.acceptorMerchantCode = acceptorMerchantCode;
  }

  public String getArpc() {
    return arpc;
  }

  public void setArpc(String arpc) {
    this.arpc = arpc;
  }

  public String getReplacementsAmount() {
    return ReplacementsAmount;
  }

  public void setReplacementsAmount(String replacementsAmount) {
    ReplacementsAmount = replacementsAmount;
  }

  public String getPosEntryMode() {
    return PosEntryMode;
  }

  public void setPosEntryMode(String posEntryMode) {
    PosEntryMode = posEntryMode;
  }

  public String getTvr() {
    return tvr;
  }

  public void setTvr(String tvr) {
    this.tvr = tvr;
  }

  public String getCvr() {
    return cvr;
  }

  public void setCvr(String cvr) {
    this.cvr = cvr;
  }

  public Date getRequestDate() {
    return requestDate;
  }

  public void setRequestDate(Date requestDate) {
    this.requestDate = requestDate;
  }

  public Date getResponseDate() {
    return responseDate;
  }

  public void setResponseDate(Date responseDate) {
    this.responseDate = responseDate;
  }


  @Override
  public String toString() {
    return "SwitchDisplay{" +
        "switchCode=" + switchCode +
        ", mti='" + mti + '\'' +
        ", pan='" + pan + '\'' +
        ", amount='" + amount + '\'' +
        ", currency='" + currency + '\'' +
        ", rrn='" + rrn + '\'' +
        ", requestDate=" + requestDate +
        ", responseDate=" + responseDate +
        ", responseCode='" + responseCode + '\'' +
        ", authNumber='" + authNumber + '\'' +
        ", acceptorMerchantCode='" + acceptorMerchantCode + '\'' +
        ", arpc='" + arpc + '\'' +
        ", ReplacementsAmount='" + ReplacementsAmount + '\'' +
        ", PosEntryMode='" + PosEntryMode + '\'' +
        ", tvr='" + tvr + '\'' +
        ", cvr='" + cvr + '\'' +
        ", processingCode='" + processingCode + '\'' +
        '}';
  }
}
