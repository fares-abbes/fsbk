package com.mss.backOffice.request;

public class AddCommission {
  private Integer transactionSource;
  private String transactionType;
  private String fixedCommission;
  private String variableComission;
  private String libelle;
  private String currency;
  public Integer getTransactionSource() {
    return transactionSource;
  }

  public void setTransactionSource(Integer transactionSource) {
    this.transactionSource = transactionSource;
  }

  public String getTransactionType() {
    return transactionType;
  }

  public void setTransactionType(String transactionType) {
    this.transactionType = transactionType;
  }

  public String getFixedCommission() {
    return fixedCommission;
  }

  public void setFixedCommission(String fixedCommission) {
    this.fixedCommission = fixedCommission;
  }

  public String getLibelle() {
    return libelle;
  }

  public void setLibelle(String libelle) {
    this.libelle = libelle;
  }

  public String getVariableComission() {
    return variableComission;
  }

  public void setVariableComission(String variableComission) {
    this.variableComission = variableComission;
  }

  public String getCurrency() {
    return currency;
  }

  public void setCurrency(String currency) {
    this.currency = currency;
  }


  @Override
  public String toString() {
    return "AddCommission{" +
        "transactionSource=" + transactionSource +
        ", transactionType='" + transactionType + '\'' +
        ", fixedCommission='" + fixedCommission + '\'' +
        ", variableComission='" + variableComission + '\'' +
        ", libelle='" + libelle + '\'' +
        ", currency='" + currency + '\'' +
        '}';
  }
}
