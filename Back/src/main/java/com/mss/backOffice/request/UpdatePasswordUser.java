package com.mss.backOffice.request;

public class UpdatePasswordUser {

  private String oldPassword;
  private String newPassword;


  public String getOldPassword() {
    return oldPassword;
  }

  public void setOldPassword(String oldPassword) {
    this.oldPassword = oldPassword;
  }

  public String getNewPassword() {
    return newPassword;
  }

  public void setNewPassword(String newPassword) {
    this.newPassword = newPassword;
  }


  @Override
  public String toString() {
    return "UpdatePasswordUser{" +
        "oldPassword='" + oldPassword + '\'' +
        ", newPassword='" + newPassword + '\'' +
        '}';
  }
}
