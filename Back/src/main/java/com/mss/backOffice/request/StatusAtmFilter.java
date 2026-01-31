package com.mss.backOffice.request;

public class StatusAtmFilter {

  private String statusNum;
  private String statusType;


  public String getStatusNum() {
    return statusNum;
  }

  public void setStatusNum(String statusNum) {
    this.statusNum = statusNum;
  }

  public String getStatusType() {
    return statusType;
  }

  public void setStatusType(String statusType) {
    this.statusType = statusType;
  }

  @Override
  public String toString() {
    return "StatusAtmFilter{" +
        "statusNum='" + statusNum + '\'' +
        ", statusType='" + statusType + '\'' +
        '}';
  }
}
