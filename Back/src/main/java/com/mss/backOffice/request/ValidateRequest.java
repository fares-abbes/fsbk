package com.mss.backOffice.request;

import java.util.List;

public class ValidateRequest {

  private  Integer codeRequest;
  private List< ValidateTpeRequest > listIdTpe;
  private String commissionInterNational;
  private String commissionNational;
  private String city;
  private String country;
  private String codeZip;
  private String phone;
  private String adresse;

  public Integer getCodeRequest() {
    return codeRequest;
  }

  public void setCodeRequest(Integer codeRequest) {
    this.codeRequest = codeRequest;
  }

  public List<ValidateTpeRequest> getListIdTpe() {
    return listIdTpe;
  }

  public void setListIdTpe(List<ValidateTpeRequest> listIdTpe) {
    this.listIdTpe = listIdTpe;
  }


  public String getCommissionInterNational() {
	return commissionInterNational;
}

public void setCommissionInterNational(String commissionInterNational) {
	this.commissionInterNational = commissionInterNational;
}

public String getCommissionNational() {
	return commissionNational;
}

public void setCommissionNational(String commissionNational) {
	this.commissionNational = commissionNational;
}


public String getCity() {
	return city;
}

public void setCity(String city) {
	this.city = city;
}

public String getCountry() {
	return country;
}

public void setCountry(String country) {
	this.country = country;
}

public String getCodeZip() {
	return codeZip;
}

public void setCodeZip(String codeZip) {
	this.codeZip = codeZip;
}

public String getPhone() {
	return phone;
}

public void setPhone(String phone) {
	this.phone = phone;
}

public String getAdresse() {
	return adresse;
}

public void setAdresse(String adresse) {
	this.adresse = adresse;
}

@Override
public String toString() {
	return "ValidateRequest [codeRequest=" + codeRequest + ", listIdTpe=" + listIdTpe + ", commissionInterNational="
			+ commissionInterNational + ", commissionNational=" + commissionNational + ", city=" + city + ", country="
			+ country + ", codeZip=" + codeZip + ", phone=" + phone + ", adresse=" + adresse + "]";
}






}
