package com.mss.backOffice.request;

import java.util.Date;

public class MerchantDisplay {

  private String nameMerchant, accountNumber, nbPOS;
  private Date creationDate;


  public String getNameMerchant() {
    return nameMerchant;
  }

  public void setNameMerchant(String nameMerchant) {
    this.nameMerchant = nameMerchant;
  }

  public String getAccountNumber() {
    return accountNumber;
  }

  public void setAccountNumber(String accountNumber) {
    this.accountNumber = accountNumber;
  }

  public Date getCreationDate() {
    return creationDate;
  }

  public void setCreationDate(Date creationDate) {
    this.creationDate = creationDate;
  }

  public String getNbPOS() {
    return nbPOS;
  }

  public void setNbPOS(String nbPOS) {
    this.nbPOS = nbPOS;
  }


  @Override
  public String toString() {
    return "MerchantDisplay{" +
        "nameMerchant='" + nameMerchant + '\'' +
        ", accountNumber='" + accountNumber + '\'' +
        ", nbPOS='" + nbPOS + '\'' +
        ", creationDate=" + creationDate +
        '}';
  }
}
