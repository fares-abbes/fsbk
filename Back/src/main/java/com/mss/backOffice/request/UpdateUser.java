package com.mss.backOffice.request;

import java.util.Set;

public class UpdateUser {

  private String firstName;
  private String lastName;
  private String email;
  private Set<String> role;
  private Integer type;
  private Integer idAgence;
  private Integer idRegion;
  private Integer idZone;
  
  
  
  public Integer getIdAgence() {
	return idAgence;
}

public void setIdAgence(Integer idAgence) {
	this.idAgence = idAgence;
}

public Integer getIdRegion() {
	return idRegion;
}

public void setIdRegion(Integer idRegion) {
	this.idRegion = idRegion;
}

public Integer getIdZone() {
	return idZone;
}

public void setIdZone(Integer idZone) {
	this.idZone = idZone;
}

public Integer getType() {
	return type;
}

public void setType(Integer type) {
	this.type = type;
}

public Set<String> getRole() {
    return role;
  }

  public void setRole(Set<String> role) {
    this.role = role;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
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


  @Override
  public String toString() {
    return "UpdateUser{" +
        "firstName='" + firstName + '\'' +
        ", lastName='" + lastName + '\'' +
        ", email='" + email + '\'' +
        ", role=" + role +
        '}';
  }
}
