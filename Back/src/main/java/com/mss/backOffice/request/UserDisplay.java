package com.mss.backOffice.request;



import com.mss.unified.entities.Role;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

public class UserDisplay {

  private Integer userCode;
  private String userName;
  private String userEmail;
  private String firstName;
  private String lastName;
  private int attempt;
  private Date lastLogin;
  private String bankId;
  private Boolean isActivated = false;
  private Boolean status = false;
  private Set<Role> roles = new HashSet<>();
  private Integer type;
  private Integer idAgence;
 
  
  
  public Integer getIdAgence() {
	return idAgence;
}

public void setIdAgence(Integer idAgence) {
	this.idAgence = idAgence;
}


public Integer getType() {
	return type;
}

public void setType(Integer type) {
	this.type = type;
}

private Integer timeElapsed;
  
  public Integer getTimeElapsed() {
	return timeElapsed;
}

public void setTimeElapsed(Integer timeElapsed) {
	this.timeElapsed = timeElapsed;
}

public Integer getUserCode() {
    return userCode;
  }

  public void setUserCode(Integer userCode) {
    this.userCode = userCode;
  }

  public String getUserName() {
    return userName;
  }

  public void setUserName(String userName) {
    this.userName = userName;
  }

  public String getUserEmail() {
    return userEmail;
  }

  public void setUserEmail(String userEmail) {
    this.userEmail = userEmail;
  }

  public String getFirstName() {
    return firstName;
  }

  public void setFirstName(String firstName) {
    this.firstName = firstName;
  }

  public String getLastName() {
    return lastName;
  }

  public void setLastName(String lastName) {
    this.lastName = lastName;
  }

  public int getAttempt() {
    return attempt;
  }

  public void setAttempt(int attempt) {
    this.attempt = attempt;
  }

  public Date getLastLogin() {
    return lastLogin;
  }

  public void setLastLogin(Date lastLogin) {
    this.lastLogin = lastLogin;
  }

  public String getBankId() {
    return bankId;
  }

  public void setBankId(String bankId) {
    this.bankId = bankId;
  }

  public Boolean getActivated() {
    return isActivated;
  }

  public void setActivated(Boolean activated) {
    isActivated = activated;
  }

  public Boolean getStatus() {
    return status;
  }

  public void setStatus(Boolean status) {
    this.status = status;
  }

  public Set<Role> getRoles() {
    return roles;
  }

  public void setRoles(Set<Role> roles) {
    this.roles = roles;
  }

  public UserDisplay() {
  }


  public UserDisplay(Integer userCode, String userName, String userEmail, String firstName,
      String lastName, int attempt, Date lastLogin, Boolean isActivated,
      Boolean status, Set<Role> roles,Integer timeElapsed,Integer type,Integer idAgence) {
    this.userCode = userCode;
    this.userName = userName;
    this.userEmail = userEmail;
    this.firstName = firstName;
    this.lastName = lastName;
    this.attempt = attempt;
    this.lastLogin = lastLogin;
    this.isActivated = isActivated;
    this.status = status;
    this.roles = roles;
    this.timeElapsed=timeElapsed;
    this.type=type;
    this.idAgence=idAgence;

  }

  @Override
  public String toString() {
    return "UserDisplay{" +
        "userCode=" + userCode +
        ", userName='" + userName + '\'' +
        ", userEmail='" + userEmail + '\'' +
        ", firstName='" + firstName + '\'' +
        ", lastName='" + lastName + '\'' +
        ", attempt=" + attempt +
        ", lastLogin=" + lastLogin +
        ", bankId='" + bankId + '\'' +
        ", isActivated=" + isActivated +
        ", status=" + status +
        ", roles=" + roles +
        '}';
  }
}
