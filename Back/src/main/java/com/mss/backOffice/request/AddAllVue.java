package com.mss.backOffice.request;

import java.util.List;

public class AddAllVue {
private String groupe;
  private List<String> vue;

  public List<String> getVue() {
    return vue;
  }

  public void setVue(List<String> vue) {
    this.vue = vue;
  }

  public String getGroupe() {
    return groupe;
  }

  public void setGroupe(String groupe) {
    this.groupe = groupe;
  }


  @Override
  public String toString() {
    return "AddAllVue{" +
        "groupe='" + groupe + '\'' +
        ", vue=" + vue +
        '}';
  }
}
