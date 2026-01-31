package com.mss.backOffice.request;

public class VueValuePer {

  private String vueCode;
  private String libelle;
  private Integer vueValue;
  private String groupe;

  public String getGroupe() {
    return groupe;
  }

  public void setGroupe(String groupe) {
    this.groupe = groupe;
  }

  public String getVueCode() {
    return vueCode;
  }

  public void setVueCode(String vueCode) {
    this.vueCode = vueCode;
  }

  public String getLibelle() {
    return libelle;
  }

  public void setLibelle(String libelle) {
    this.libelle = libelle;
  }

  public Integer getVueValue() {
    return vueValue;
  }

  public void setVueValue(Integer vueValue) {
    this.vueValue = vueValue;
  }

  public VueValuePer() {
  }


  public VueValuePer(String vueCode, String libelle, Integer vueValue, String groupe) {
    this.vueCode = vueCode;
    this.libelle = libelle;
    this.vueValue = vueValue;
    this.groupe=groupe;
  }

  @Override
  public String toString() {
    return "VueValuePer{" +
            "vueCode=" + vueCode +
            ", libelle='" + libelle + '\'' +
            ", vueValue=" + vueValue +
            ", groupe='" + groupe + '\'' +
            '}';
  }
}
