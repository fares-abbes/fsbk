package com.mss.backOffice.request;

import java.util.List;

public class AddAccess {

  private String roleName;
  private  List<VuePrivilege> vueAction;

  public String getRoleName() {
    return roleName;
  }

  public void setRoleName(String roleName) {
    this.roleName = roleName;
  }


  public List<VuePrivilege> getVueAction() {
    return vueAction;
  }

  public void setVueAction(List<VuePrivilege> vueAction) {
    this.vueAction = vueAction;
  }


  @Override
  public String toString() {
    return "AddAccess{" +
        "roleName='" + roleName + '\'' +
        ", vueAction=" + vueAction +
        '}';
  }
}
