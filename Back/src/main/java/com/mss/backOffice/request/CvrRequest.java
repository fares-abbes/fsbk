package com.mss.backOffice.request;

import java.util.List;

public class CvrRequest {

  private String libelle;
  private List<Boolean> cvrValue;


  public String getLibelle() {
    return libelle;
  }

  public void setLibelle(String libelle) {
    this.libelle = libelle;
  }

  public List<Boolean> getCvrValue() {
    return cvrValue;
  }

  public void setCvrValue(List<Boolean> cvrValue) {
    this.cvrValue = cvrValue;
  }


  @Override
  public String toString() {
    return "CvrRequest{" +
        "libelle='" + libelle + '\'' +
        ", cvrValue=" + cvrValue +
        '}';
  }
}
