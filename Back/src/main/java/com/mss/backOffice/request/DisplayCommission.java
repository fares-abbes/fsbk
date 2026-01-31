package com.mss.backOffice.request;

public class DisplayCommission {
  private String transactionSource;
  private String transactionType;
  private String fixedCommission;
  private String variableComission;


  public String getTransactionSource() {
    return transactionSource;
  }

  public void setTransactionSource(String transactionSource) {
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

  public String getVariableComission() {
    return variableComission;
  }

  public void setVariableComission(String variableComission) {
    this.variableComission = variableComission;
  }

  @Override
  public String toString() {
    return "DisplayCommission{" +
        "transactionSource='" + transactionSource + '\'' +
        ", transactionType='" + transactionType + '\'' +
        ", fixedCommission='" + fixedCommission + '\'' +
        ", variableComission='" + variableComission + '\'' +
        '}';
  }
}
