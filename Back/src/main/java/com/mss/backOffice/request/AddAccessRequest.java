package com.mss.backOffice.request;

public class AddAccessRequest {

  private Integer vueCode;
  private String libelle;
  private Integer vueValue;
  private String roleName;

  public Integer getVueCode() {
    return vueCode;
  }

  public void setVueCode(Integer vueCode) {
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

  public String getRoleName() {
    return roleName;
  }

  public void setRoleName(String roleName) {
    this.roleName = roleName;
  }

  @Override
  public String toString() {
    return "AddAccessRequest{" +
        "vueCode=" + vueCode +
        ", libelle='" + libelle + '\'' +
        ", vueValue=" + vueValue +
        ", roleName='" + roleName + '\'' +
        '}';
  }
}
