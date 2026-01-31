package com.mss.backOffice.request;

import javax.persistence.Column;

public class AddBank {

  private String tagBank;

  private String libelle;
  private String identificationNumber;

  public String getIdentificationNumber() {
    return identificationNumber;
  }

  public void setIdentificationNumber(String identificationNumber) {
    this.identificationNumber = identificationNumber;
  }

  public String getTagBank() {
    return tagBank;
  }

  public void setTagBank(String tagBank) {
    this.tagBank = tagBank;
  }

  public String getLibelle() {
    return libelle;
  }

  public void setLibelle(String libelle) {
    this.libelle = libelle;
  }


  @Override
  public String toString() {
    return "AddBank{" +
        "tagBank='" + tagBank + '\'' +
        ", libelle='" + libelle + '\'' +
        ", identificationNumber='" + identificationNumber + '\'' +
        '}';
  }
}
