package com.mss.backOffice.request;

public class TpePendingDisplay {

  private Integer code;

  private String libelle;

  private String city;

  private String mccCode;
  private String mcc;
  private String country;


  private String zipCode;

  private String phone;


  private String type;
  private String typeLibelle;
  private String preAutorisation;
  private String motif;
  private String status;
  private String model;
private String address;

private String famillePosCode;
private String familyTPELabelle;

private int familleCode;

private String activite;



  public String getMcc() {
	return mcc;
}

public void setMcc(String mcc) {
	this.mcc = mcc;
}

public String getTypeLibelle() {
	return typeLibelle;
}

public void setTypeLibelle(String typeLibelle) {
	this.typeLibelle = typeLibelle;
}

public String getFamilyTPELabelle() {
	return familyTPELabelle;
}

public void setFamilyTPELabelle(String familyTPELabelle) {
	this.familyTPELabelle = familyTPELabelle;
}

public String getActivite() {
	return activite;
}

public void setActivite(String activite) {
	this.activite = activite;
}

public int getFamilleCode() {
	return familleCode;
}

public void setFamilleCode(int familleCode) {
	this.familleCode = familleCode;
}

public String getFamillePosCode() {
	return famillePosCode;
}

public void setFamillePosCode(String famillePosCode) {
	this.famillePosCode = famillePosCode;
}

public String getAddress() {
    return address;
  }

  public void setAddress(String address) {
    this.address = address;
  }

  public String getModel() {
    return model;
  }

  public void setModel(String model) {
    this.model = model;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public String getPreAutorisation() {
    return preAutorisation;
  }

  public String getMotif() {
    return motif;
  }

  public void setMotif(String motif) {
    this.motif = motif;
  }

  public void setPreAutorisation(String preAutorisation) {
    this.preAutorisation = preAutorisation;
  }

  public Integer getCode() {
    return code;
  }

  public void setCode(Integer code) {
    this.code = code;
  }

  public String getLibelle() {
    return libelle;
  }

  public void setLibelle(String libelle) {
    this.libelle = libelle;
  }

  public String getCity() {
    return city;
  }

  public void setCity(String city) {
    this.city = city;
  }

  public String getMccCode() {
    return mccCode;
  }

  public void setMccCode(String mccCode) {
    this.mccCode = mccCode;
  }

  public String getCountry() {
    return country;
  }

  public void setCountry(String country) {
    this.country = country;
  }

  public String getZipCode() {
    return zipCode;
  }

  public void setZipCode(String zipCode) {
    this.zipCode = zipCode;
  }

  public String getPhone() {
    return phone;
  }

  public void setPhone(String phone) {
    this.phone = phone;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

@Override
public String toString() {
	return "TpePendingDisplay [code=" + code + ", libelle=" + libelle + ", city=" + city + ", mccCode=" + mccCode
			+ ", country=" + country + ", zipCode=" + zipCode + ", phone=" + phone + ", type=" + type
			+ ", preAutorisation=" + preAutorisation + ", motif=" + motif + ", status=" + status + ", model=" + model
			+ ", address=" + address + ", famillePosCode=" + famillePosCode + ", familyTPELabelle=" + familyTPELabelle
			+ ", familleCode=" + familleCode + ", activite=" + activite + "]";
}



 
}
