package com.mss.backOffice.request;

import java.util.Set;

public class AddRole {
private String name;
  private Set<String> privilege;


  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public Set<String> getPrivilege() {
    return privilege;
  }

  public void setPrivilege(Set<String> privilege) {
    this.privilege = privilege;
  }


  @Override
  public String toString() {
    return "AddRole{" +
        "name='" + name + '\'' +
        ", privilege=" + privilege +
        '}';
  }
}
