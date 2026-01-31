package com.mss.backOffice.request;


import java.util.HashSet;
import java.util.Set;

public class UpdateTpeRequest {

  private String accountNumber;

  private String userName;

  private Integer agence;

  private String nombreTPE;


  private String commissionNational;

  private String commissionInterNational;

  private String city;

  private String country;

  private String codeZip;

  private String phone;
private String agentName;

  private String status;
  private String adresse;


  public String getAgentName() {
    return agentName;
  }

  public void setAgentName(String agentName) {
    this.agentName = agentName;
  }

  public String getAdresse() {
    return adresse;
  }

  public void setAdresse(String adresse) {
    this.adresse = adresse;
  }

  private Set<TpePendingDisplay> pedndingTpes = new HashSet<>();


  public String getAccountNumber() {
    return accountNumber;
  }

  public void setAccountNumber(String accountNumber) {
    this.accountNumber = accountNumber;
  }

  public String getUserName() {
    return userName;
  }

  public void setUserName(String userName) {
    this.userName = userName;
  }

  public Integer getAgence() {
    return agence;
  }

  public void setAgence(Integer agence) {
    this.agence = agence;
  }

  public String getNombreTPE() {
    return nombreTPE;
  }

  public void setNombreTPE(String nombreTPE) {
    this.nombreTPE = nombreTPE;
  }


  public String getCommissionNational() {
    return commissionNational;
  }

  public void setCommissionNational(String commissionNational) {
    this.commissionNational = commissionNational;
  }

  public String getCommissionInterNational() {
    return commissionInterNational;
  }

  public void setCommissionInterNational(String commissionInterNational) {
    this.commissionInterNational = commissionInterNational;
  }

  public String getCity() {
    return city;
  }

  public void setCity(String city) {
    this.city = city;
  }

  public String getCountry() {
    return country;
  }

  public void setCountry(String country) {
    this.country = country;
  }

  public String getCodeZip() {
    return codeZip;
  }

  public void setCodeZip(String codeZip) {
    this.codeZip = codeZip;
  }

  public String getPhone() {
    return phone;
  }

  public void setPhone(String phone) {
    this.phone = phone;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public Set<TpePendingDisplay> getPedndingTpes() {
    return pedndingTpes;
  }

  public void setPedndingTpes(Set<TpePendingDisplay> pedndingTpes) {
    this.pedndingTpes = pedndingTpes;
  }

  @Override
  public String toString() {
    return "UpdateTpeRequest{" +
        "accountNumber='" + accountNumber + '\'' +
        ", userName='" + userName + '\'' +
        ", agence=" + agence +
        ", nombreTPE='" + nombreTPE + '\'' +
        ", commissionNational='" + commissionNational + '\'' +
        ", commissionInterNational='" + commissionInterNational + '\'' +
        ", city='" + city + '\'' +
        ", country='" + country + '\'' +
        ", codeZip='" + codeZip + '\'' +
        ", phone='" + phone + '\'' +
        ", agentName='" + agentName + '\'' +
        ", status='" + status + '\'' +
        ", adresse='" + adresse + '\'' +
        ", pedndingTpes=" + pedndingTpes +
        '}';
  }
}
