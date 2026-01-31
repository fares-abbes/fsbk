package com.mss.backOffice.request;

import java.util.List;

public class AddVue {

  private String libelle;

  public String getLibelle() {
    return libelle;
  }

  public void setLibelle(String libelle) {
    this.libelle = libelle;
  }

  @Override
  public String toString() {
    return "AddVue{" +
        "libelle='" + libelle + '\'' +
        '}';
  }
}
