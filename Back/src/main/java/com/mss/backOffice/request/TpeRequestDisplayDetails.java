package com.mss.backOffice.request;

import com.mss.unified.entities.CommissionTpe;
import com.mss.unified.entities.PendingTpe;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TpeRequestDisplayDetails {

  private int requestCode;
  private String accountNumber;

  private String userName;

  private String agence;

  private String nombreTPE;
  
  private String dateSaisie;

  private Date dateCreation;

  private Date dateDecision;

  //private String commissionNational;

  // private String commissionInterNational;

  private String city;

  private String country;

  private String codeZip;

  private String phone;


  private String status;
private Integer codeCommission;

  private String adresse;
  private String commissionTypeCode ;
  private String commissionInterNational;
  private String commissionNational;
  private Set<TpePendingDisplay> pedndingTpes = new HashSet<>();
  private Set<PendingTpe> pendingTpess = new HashSet<>();
  private List<CommissionTpe> commissionTpes;
  private List<CommissionTpe> commissionTpesInter;
  
 



  private Boolean offshore;
  
  
  private Integer codeCommissionh;

  private String commissionTypeCodeh ;
  private String commissionInterNationalh;
  private String commissionNationalh;

  private List<CommissionTpe> commissionTpesh;
  private List<CommissionTpe> commissionTpesInterh;
  
  
  private String nom;
 private String prenom;
 private String devise;
 private String nif;

 private String rso;
 private String rc;
 private String email;
 
 private String daira;
 
 private String commmune;
  
 private String revenue;

 private String siteWeb;
 
 private String nomC; 

 private String titleC;
 
 private String montantLoyer;
 private String montantLoyerh;

 
public String getMontantLoyerh() {
	return montantLoyerh;
}

public void setMontantLoyerh(String montantLoyerh) {
	this.montantLoyerh = montantLoyerh;
}

public String getMontantLoyer() {
			return montantLoyer;
		}

		public void setMontantLoyer(String montantLoyer) {
			this.montantLoyer = montantLoyer;
		}
 
public Set<PendingTpe> getPendingTpess() {
	return pendingTpess;
}

public void setPendingTpess(Set<PendingTpe> pendingTpess) {
	this.pendingTpess = pendingTpess;
}

public String getNomC() {
	return nomC;
}

public void setNomC(String nomC) {
	this.nomC = nomC;
}

public String getTitleC() {
	return titleC;
}

public void setTitleC(String titleC) {
	this.titleC = titleC;
}

public String getSiteWeb() {
	return siteWeb;
}

public void setSiteWeb(String siteWeb) {
	this.siteWeb = siteWeb;
}

public String getRevenue() {
	return revenue;
}

public void setRevenue(String revenue) {
	this.revenue = revenue;
}

public String getRc() {
	return rc;
}

public void setRc(String rc) {
	this.rc = rc;
}

public String getEmail() {
	return email;
}

public void setEmail(String email) {
	this.email = email;
}

public String getNom() {
	return nom;
}

public void setNom(String nom) {
	this.nom = nom;
}

public String getPrenom() {
	return prenom;
}

public void setPrenom(String prenom) {
	this.prenom = prenom;
}

public String getDevise() {
	return devise;
}

public void setDevise(String devise) {
	this.devise = devise;
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

public String getDateSaisie() {
	return dateSaisie;
}

public void setDateSaisie(String dateSaisie) {
	this.dateSaisie = dateSaisie;
}

public Integer getCodeCommissionh() {
	return codeCommissionh;
}

public void setCodeCommissionh(Integer codeCommissionh) {
	this.codeCommissionh = codeCommissionh;
}

public String getCommissionTypeCodeh() {
	return commissionTypeCodeh;
}

public void setCommissionTypeCodeh(String commissionTypeCodeh) {
	this.commissionTypeCodeh = commissionTypeCodeh;
}

public String getCommissionInterNationalh() {
	return commissionInterNationalh;
}

public void setCommissionInterNationalh(String commissionInterNationalh) {
	this.commissionInterNationalh = commissionInterNationalh;
}

public String getCommissionNationalh() {
	return commissionNationalh;
}

public void setCommissionNationalh(String commissionNationalh) {
	this.commissionNationalh = commissionNationalh;
}

public List<CommissionTpe> getCommissionTpesh() {
	return commissionTpesh;
}

public void setCommissionTpesh(List<CommissionTpe> commissionTpesh) {
	this.commissionTpesh = commissionTpesh;
}

public List<CommissionTpe> getCommissionTpesInterh() {
	return commissionTpesInterh;
}

public void setCommissionTpesInterh(List<CommissionTpe> commissionTpesInterh) {
	this.commissionTpesInterh = commissionTpesInterh;
}

public Integer getCodeCommission() {
	return codeCommission;
}

public void setCodeCommission(Integer codeCommission) {
	this.codeCommission = codeCommission;
}

public String getAdresse() {
    return adresse;
  }

  public void setAdresse(String adresse) {
    this.adresse = adresse;
  }

  public Set<TpePendingDisplay> getPedndingTpes() {
    return pedndingTpes;
  }

  public void setPedndingTpes(Set<TpePendingDisplay> pedndingTpes) {
    this.pedndingTpes = pedndingTpes;
  }

  public int getRequestCode() {
    return requestCode;
  }

  public void setRequestCode(int requestCode) {
    this.requestCode = requestCode;
  }

  public String getAccountNumber() {
    return accountNumber;
  }

  public void setAccountNumber(String accountNumber) {
    this.accountNumber = accountNumber;
  }

  public String getUserName() {
    return userName;
  }

  public void setUserName(String userName) {
    this.userName = userName;
  }

  public String getAgence() {
    return agence;
  }

  public void setAgence(String agence) {
    this.agence = agence;
  }

  public String getNombreTPE() {
    return nombreTPE;
  }

  public void setNombreTPE(String nombreTPE) {
    this.nombreTPE = nombreTPE;
  }

  public Date getDateCreation() {
    return dateCreation;
  }

  public void setDateCreation(Date dateCreation) {
    this.dateCreation = dateCreation;
  }

  public Date getDateDecision() {
    return dateDecision;
  }

  public void setDateDecision(Date dateDecision) {
    this.dateDecision = dateDecision;
  }

  /*public String getCommissionNational() {
    return commissionNational;
  }

  public void setCommissionNational(String commissionNational) {
    this.commissionNational = commissionNational;
  }

  public String getCommissionInterNational() {
    return commissionInterNational;
  }

  public void setCommissionInterNational(String commissionInterNational) {
    this.commissionInterNational = commissionInterNational;
  }*/

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


  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public String getCommissionTypeCode() {
    return commissionTypeCode;
  }

  public void setCommissionTypeCode(String commissionTypeCode) {
    this.commissionTypeCode = commissionTypeCode;
  }

  public Boolean getOffshore() {
    return offshore;
  }

  public void setOffshore(Boolean offshore) {
    this.offshore = offshore;
  }

  public List<CommissionTpe> getCommissionTpes() {
    return commissionTpes;
  }

  public void setCommissionTpes(List<CommissionTpe> commissionTpes) {
    this.commissionTpes = commissionTpes;
  }

  public List<CommissionTpe> getCommissionTpesInter() {
    return commissionTpesInter;
  }

  public void setCommissionTpesInter(List<CommissionTpe> commissionTpesInter) {
    this.commissionTpesInter = commissionTpesInter;
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

public String getDaira() {
	return daira;
}

public void setDaira(String daira) {
	this.daira = daira;
}

public String getCommmune() {
	return commmune;
}

public void setCommmune(String commmune) {
	this.commmune = commmune;
}









 
}
