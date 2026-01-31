package com.mss.backOffice.request;

public class VuePrivilege {

  private String idVue;
  private Integer privilege;


  public String getIdVue() {
    return idVue;
  }

  public Integer getPrivilege() {
    return privilege;
  }

  public void setPrivilege(Integer privilege) {
    this.privilege = privilege;
  }

  public void setIdVue(String idVue) {
    this.idVue = idVue;
  }


  public VuePrivilege(String idVue, Integer privilege) {
    this.idVue = idVue;
    this.privilege = privilege;
  }

  public VuePrivilege() {
  }

  @Override
  public String toString() {
    return "VuePrivilege{" +
            "idVue=" + idVue +
            ", privilege=" + privilege +
            '}';
  }
}
