package com.mss.backOffice.request;

public class DispalyVue {

  private String vueCode;
  private String libelle;
  private String groupe;
  private Integer vueValue;

  public Integer getVueValue() {
    return vueValue;
  }

  public void setVueValue(Integer vueValue) {
    this.vueValue = vueValue;
  }

  public DispalyVue() {
  }

  public DispalyVue(String vueCode, String libelle, String groupe,Integer vueValue) {
    this.vueCode = vueCode;
    this.libelle = libelle;
    this.groupe = groupe;
    this.vueValue=vueValue;
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

  public String getGroupe() {
    return groupe;
  }

  public void setGroupe(String groupe) {
    this.groupe = groupe;
  }

  @Override
  public String toString() {
    return "DispalyVue{" +
            "vueCode=" + vueCode +
            ", libelle='" + libelle + '\'' +
            ", groupe='" + groupe + '\'' +
            ", vueValue=" + vueValue +
            '}';
  }
}
