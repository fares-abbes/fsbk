package com.mss.backOffice.request;

public class FilterUsers {
    Long role;
    String userName;

    public Long getRole() {
        return role;
    }

    public void setRole(Long role) {
        this.role = role;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }
}
