package com.mss.backOffice.request;

import java.util.Date;

import javax.persistence.Column;

public class MerchantListDisplay {

	


	private String agence;

  private Integer merchantCode;
  private String nameMerchant;
  private String accountNumber;
  private Date creationDate;
  private String comi;
  private String comn;
  private String nbPOS;
  private String merchantId;
  private Integer commissionType;
  
  private String adresse;
  private String city;
  
  private String phone;
  private String codeZip;
  
  private String country;
  
  private Integer merchantstatus;


  private String etatMerchant;
  
  
  private boolean offshore;
  
  private String email;

  private String commune;

  private String daira;
  
  
  private String nif;
  
  private String rso;
  
  private String rc;


  private String revenue;
  
  private String address;
  
  private String merchantLibelle;
  
  private String idContrat;
  
  private String detailResiliation;

  
  
  public String getDetailResiliation() {
	return detailResiliation;
}

public void setDetailResiliation(String detailResiliation) {
	this.detailResiliation = detailResiliation;
}

public String getIdContrat() {
	return idContrat;
}

public void setIdContrat(String idContrat) {
	this.idContrat = idContrat;
}

public String getAddress() {
	return address;
}

public void setAddress(String address) {
	this.address = address;
}

public String getMerchantLibelle() {
	return merchantLibelle;
}

public void setMerchantLibelle(String merchantLibelle) {
	this.merchantLibelle = merchantLibelle;
}

public String getAgence() {
	return agence;
}

public void setAgence(String agence) {
	this.agence = agence;
}

public String getNif() {
	return nif;
}

public void setNif(String nif) {
	this.nif = nif;
}

public String getRso() {
	return rso;
}

public void setRso(String rso) {
	this.rso = rso;
}

public String getRc() {
	return rc;
}

public void setRc(String rc) {
	this.rc = rc;
}

public String getRevenue() {
	return revenue;
}

public void setRevenue(String revenue) {
	this.revenue = revenue;
}

public String getEmail() {
	return email;
}

public void setEmail(String email) {
	this.email = email;
}

public String getCommune() {
	return commune;
}

public void setCommune(String commune) {
	this.commune = commune;
}

public String getDaira() {
	return daira;
}

public void setDaira(String daira) {
	this.daira = daira;
}

public Integer getMerchantstatus() {
	return merchantstatus;
}

public void setMerchantstatus(Integer merchantstatus) {
	this.merchantstatus = merchantstatus;
}

public String getAdresse() {
	return adresse;
}

public void setAdresse(String adresse) {
	this.adresse = adresse;
}

public String getCity() {
	return city;
}

public void setCity(String city) {
	this.city = city;
}

public String getPhone() {
	return phone;
}

public void setPhone(String phone) {
	this.phone = phone;
}

public String getCodeZip() {
	return codeZip;
}

public void setCodeZip(String codeZip) {
	this.codeZip = codeZip;
}

public String getCountry() {
	return country;
}

public void setCountry(String country) {
	this.country = country;
}

public Integer getCommissionType() {
	return commissionType;
}

public void setCommissionType(Integer commissionType) {
	this.commissionType = commissionType;
}



public boolean isOffshore() {
	return offshore;
}

public void setOffshore(boolean offshore) {
	this.offshore = offshore;
}

public String getMerchantId() {
    return merchantId;
  }

  public void setMerchantId(String merchantId) {
    this.merchantId = merchantId;
  }

  public Integer getMerchantCode() {
    return merchantCode;
  }

  public void setMerchantCode(Integer merchantCode) {
    this.merchantCode = merchantCode;
  }

  public String getNameMerchant() {
    return nameMerchant;
  }

  public void setNameMerchant(String nameMerchant) {
    this.nameMerchant = nameMerchant;
  }

  public String getAccountNumber() {
    return accountNumber;
  }

  public void setAccountNumber(String accountNumber) {
    this.accountNumber = accountNumber;
  }

  public Date getCreationDate() {
    return creationDate;
  }

  public void setCreationDate(Date creationDate) {
    this.creationDate = creationDate;
  }

  public String getComi() {
    return comi;
  }

  public void setComi(String comi) {
    this.comi = comi;
  }

  public String getComn() {
    return comn;
  }

  public void setComn(String comn) {
    this.comn = comn;
  }

  public String getNbPOS() {
    return nbPOS;
  }

  public void setNbPOS(String nbPOS) {
    this.nbPOS = nbPOS;
  }

  
  public String getEtatMerchant() {
	return etatMerchant;
}

public void setEtatMerchant(String etatMerchant) {
	this.etatMerchant = etatMerchant;
}

public MerchantListDisplay() {
  }

  public MerchantListDisplay(Integer merchantCode, String nameMerchant,
      String accountNumber, Date creationDate, String comi, String comn, String nbPOS,
      String merchantId,boolean offshore,Integer commissionType) {
    this.merchantCode = merchantCode;
    this.nameMerchant = nameMerchant;
    this.accountNumber = accountNumber;
    this.creationDate = creationDate;
    this.comi = comi;
    this.comn = comn;
    this.nbPOS = nbPOS;
    this.merchantId = merchantId;
    this.offshore=offshore;
    this.commissionType=commissionType;
  }

@Override
public String toString() {
	return "MerchantListDisplay [merchantCode=" + merchantCode + ", nameMerchant=" + nameMerchant + ", accountNumber="
			+ accountNumber + ", creationDate=" + creationDate + ", comi=" + comi + ", comn=" + comn + ", nbPOS="
			+ nbPOS + ", merchantId=" + merchantId + ", commissionType=" + commissionType + ", adresse=" + adresse
			+ ", city=" + city + ", phone=" + phone + ", codeZip=" + codeZip + ", country=" + country
			+ ", merchantstatus=" + merchantstatus + ", etatMerchant=" + etatMerchant + ", offshore=" + offshore
			+ ", email=" + email + ", commune=" + commune + ", daira=" + daira + ", nif=" + nif + ", rso=" + rso
			+ ", rc=" + rc + ", revenue=" + revenue + "]";
}










}
