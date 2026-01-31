package com.mss.backOffice.request;

public class ForgetPasswordRequest {
  private String userName;

  public String getUserName() {
    return userName;
  }

  public void setUserName(String userName) {
    this.userName = userName;
  }


  @Override
  public String toString() {
    return "ForgetPasswordRequest{" +
        "userName='" + userName + '\'' +
        '}';
  }
}
