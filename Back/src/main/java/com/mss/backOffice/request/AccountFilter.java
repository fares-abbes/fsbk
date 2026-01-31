package com.mss.backOffice.request;

import java.util.Date;

public class AccountFilter {

  private String numAccount;
  private int status;
  private Date startDate;
  private Date endDate;

  @Override
  public String toString() {
    return "AccountFilter{" +
        "numAccount='" + numAccount + '\'' +
        ", status=" + status +
        ", startDate=" + startDate +
        ", endDate=" + endDate +
        '}';
  }
}
