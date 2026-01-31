package com.mss.backOffice.request;

import java.util.Date;

public
class CardDisplay {

  private int cardCode;
  private Date expiryDate;
  private Date startDate;
  private Date preDate;
  private String cardNum;
  private String accCode;
  private String globalRiskCode;

  private Integer currencyCode;
  private String cardStatusCode;
  private String firtPositionCode;

  private String productCode;
  private String revProductCode;
  private String atc;

  private String program;

  private String rev_program;
  
  private Integer statusCode;
  private String perso;


  
  


public String getPerso() {
	return perso;
}

public void setPerso(String perso) {
	this.perso = perso;
}

public Integer getStatusCode() {
	return statusCode;
}

public void setStatusCode(Integer statusCode) {
	this.statusCode = statusCode;
}

public int getCardCode() {
    return cardCode;
  }

  public void setCardCode(int cardCode) {
    this.cardCode = cardCode;
  }

  public Date getExpiryDate() {
    return expiryDate;
  }

  public void setExpiryDate(Date expiryDate) {
    this.expiryDate = expiryDate;
  }

  public Date getStartDate() {
    return startDate;
  }

  public void setStartDate(Date startDate) {
    this.startDate = startDate;
  }

  public Date getPreDate() {
    return preDate;
  }

  public void setPreDate(Date preDate) {
    this.preDate = preDate;
  }

  public String getCardNum() {
    return cardNum;
  }

  public void setCardNum(String cardNum) {
    this.cardNum = cardNum;
  }

  public String getAccCode() {
    return accCode;
  }

  public void setAccCode(String accCode) {
    this.accCode = accCode;
  }

  public String getGlobalRiskCode() {
    return globalRiskCode;
  }

  public void setGlobalRiskCode(String globalRiskCode) {
    this.globalRiskCode = globalRiskCode;
  }

  public Integer getCurrencyCode() {
    return currencyCode;
  }

  public void setCurrencyCode(Integer currencyCode) {
    this.currencyCode = currencyCode;
  }

  public String getCardStatusCode() {
    return cardStatusCode;
  }

  public void setCardStatusCode(String cardStatusCode) {
    this.cardStatusCode = cardStatusCode;
  }

  public String getFirtPositionCode() {
    return firtPositionCode;
  }

  public void setFirtPositionCode(String firtPositionCode) {
    this.firtPositionCode = firtPositionCode;
  }

  public String getProductCode() {
    return productCode;
  }

  public void setProductCode(String productCode) {
    this.productCode = productCode;
  }

  public String getRevProductCode() {
    return revProductCode;
  }

  public void setRevProductCode(String revProductCode) {
    this.revProductCode = revProductCode;
  }

  public String getAtc() {
    return atc;
  }

  public void setAtc(String atc) {
    this.atc = atc;
  }

  public String getProgram() {
    return program;
  }

  public void setProgram(String program) {
    this.program = program;
  }

  public String getRev_program() {
    return rev_program;
  }

  public void setRev_program(String rev_program) {
    this.rev_program = rev_program;
  }

  @Override
  public String toString() {
    return "CardDisplay{" +
        "cardCode=" + cardCode +
        ", expiryDate=" + expiryDate +
        ", startDate=" + startDate +
        ", preDate=" + preDate +
        ", cardNum='" + cardNum + '\'' +
        ", accCode='" + accCode + '\'' +
        ", globalRiskCode='" + globalRiskCode + '\'' +
        ", currencyCode=" + currencyCode +
        ", cardStatusCode='" + cardStatusCode + '\'' +

        ", firtPositionCode=" + firtPositionCode +
        ", productCode='" + productCode + '\'' +
        ", revProductCode='" + revProductCode + '\'' +
        ", atc='" + atc + '\'' +
        ", program='" + program + '\'' +
        ", rev_program='" + rev_program + '\'' +
        '}';
  }
}
