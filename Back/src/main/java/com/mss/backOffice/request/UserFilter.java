package com.mss.backOffice.request;

public class UserFilter {

  private String username;
  private Boolean status;
  private String role;

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public Boolean getStatus() {
    return status;
  }

  public void setStatus(Boolean status) {
    this.status = status;
  }

  public String getRole() {
    return role;
  }

  public void setRole(String role) {
    this.role = role;
  }


  @Override
  public String toString() {
    return "UserFilter{" +
        "username='" + username + '\'' +
        ", status=" + status +
        ", role='" + role + '\'' +
        '}';
  }
}
