package com.mss.backOffice.request;

import java.util.Date;
import java.util.List;

public class RiskManagementRequest {

  private Integer globalId;
  private long glMinAmount;
  private long glMaxAmount;
  private String globalAmount;
  private long globalNumber;
  private long glCurrencyAmount;
  private String glCurrencyNumber;
  private Date glStartDate;
  private Date glEndDate;
  private String glPeriodicityType;

  public Integer getGlobalId() {
    return globalId;
  }

  public void setGlobalId(Integer globalId) {
    this.globalId = globalId;
  }

  private List<DetailsRequest> detailsRequestList;

  public List<DetailsRequest> getDetailsRequestList() {
    return detailsRequestList;
  }

  public void setDetailsRequestList(
      List<DetailsRequest> detailsRequestList) {
    this.detailsRequestList = detailsRequestList;
  }

  public long getGlMinAmount() {
    return glMinAmount;
  }

  public void setGlMinAmount(long glMinAmount) {
    this.glMinAmount = glMinAmount;
  }

  public long getGlMaxAmount() {
    return glMaxAmount;
  }

  public void setGlMaxAmount(long glMaxAmount) {
    this.glMaxAmount = glMaxAmount;
  }

  public String getGlobalAmount() {
    return globalAmount;
  }

  public void setGlobalAmount(String globalAmount) {
    this.globalAmount = globalAmount;
  }

  public long getGlobalNumber() {
    return globalNumber;
  }

  public void setGlobalNumber(long globalNumber) {
    this.globalNumber = globalNumber;
  }

  public long getGlCurrencyAmount() {
    return glCurrencyAmount;
  }

  public void setGlCurrencyAmount(long glCurrencyAmount) {
    this.glCurrencyAmount = glCurrencyAmount;
  }

  public String getGlCurrencyNumber() {
    return glCurrencyNumber;
  }

  public void setGlCurrencyNumber(String glCurrencyNumber) {
    this.glCurrencyNumber = glCurrencyNumber;
  }

  public Date getGlStartDate() {
    return glStartDate;
  }

  public void setGlStartDate(Date glStartDate) {
    this.glStartDate = glStartDate;
  }

  public Date getGlEndDate() {
    return glEndDate;
  }

  public void setGlEndDate(Date glEndDate) {
    this.glEndDate = glEndDate;
  }

  public String getGlPeriodicityType() {
    return glPeriodicityType;
  }

  public void setGlPeriodicityType(String glPeriodicityType) {
    this.glPeriodicityType = glPeriodicityType;
  }

  public RiskManagementRequest() {
  }

  public RiskManagementRequest(Integer globalId, long glMinAmount, long glMaxAmount,
      String globalAmount, long globalNumber, long glCurrencyAmount,
      String glCurrencyNumber, Date glStartDate, Date glEndDate, String glPeriodicityType) {
    this.globalId = globalId;
    this.glMinAmount = glMinAmount;
    this.glMaxAmount = glMaxAmount;
    this.globalAmount = globalAmount;
    this.globalNumber = globalNumber;
    this.glCurrencyAmount = glCurrencyAmount;
    this.glCurrencyNumber = glCurrencyNumber;
    this.glStartDate = glStartDate;
    this.glEndDate = glEndDate;
    this.glPeriodicityType = glPeriodicityType;
  }

  @Override
  public String toString() {
    return "RiskManagementRequest{" +
        "globalId=" + globalId +
        ", glMinAmount=" + glMinAmount +
        ", glMaxAmount=" + glMaxAmount +
        ", globalAmount='" + globalAmount + '\'' +
        ", globalNumber=" + globalNumber +
        ", glCurrencyAmount=" + glCurrencyAmount +
        ", glCurrencyNumber='" + glCurrencyNumber + '\'' +
        ", glStartDate=" + glStartDate +
        ", glEndDate=" + glEndDate +
        ", glPeriodicityType='" + glPeriodicityType + '\'' +
        ", detailsRequestList=" + detailsRequestList +
        '}';
  }
}
