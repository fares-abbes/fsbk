package com.mss.backOffice.request;

import java.util.List;

public class TvrRequest {
  private String libelle;
  private List<Boolean> tvrValue;

  public String getLibelle() {
    return libelle;
  }

  public void setLibelle(String libelle) {
    this.libelle = libelle;
  }

  public List<Boolean> getTvrValue() {
    return tvrValue;
  }

  public void setTvrValue(List<Boolean> tvrValue) {
    this.tvrValue = tvrValue;
  }


  @Override
  public String toString() {
    return "TvrRequest{" +
        "libelle='" + libelle + '\'' +
        ", tvrValue=" + tvrValue +
        '}';
  }
}
