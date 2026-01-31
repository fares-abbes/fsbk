package com.mss.backOffice.request;

import com.mss.unified.entities.CommissionTpe;
import com.mss.unified.entities.PendingTpe;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.Column;


public class TpeRequestDisplay {

  private int requestCode;
  private String accountNumber;

  private String userName;

  private String agence;

  private String nombreTPE;

  private Date dateCreation;

  private Date dateDecision;

  private String commissionNational;

  private String commissionInterNational;

  private String city;
  private String codeWilaya;

  private String country;

  private String codeZip;

  private String phone;

  private String label;
  private String status;
 private String agentName;

  private String adresse;

  private String commissionTypeCode ;

  private Boolean offshore;

private String montantRef;

private int statusVa;
  private List<CommissionTpeRequest> commissionTpes;
    private List<CommissionTpeRequest> commissionTpesInter;
  private Set<PendingTpe> pendingTpes = new HashSet<>();
  
  private String nom;
 private String prenom;
 private String devise;
 private String nif;

 private String rso;
 private String rc;
 
 private String email;

	private String commune;

	private String daira;

	private String revenue;
	  private String statuContrat;
	  
	  private String siteWeb;
	  

		private String numContrat;
	  
	    private String nomC;
	    

	    private String typeC;
	    

	    private String titleC;


	    private String raisonRejet;

	    private String montantLoyer;


	    
public String getMontantLoyer() {
			return montantLoyer;
		}

		public void setMontantLoyer(String montantLoyer) {
			this.montantLoyer = montantLoyer;
		}

public String getRaisonRejet() {
			return raisonRejet;
		}

		public void setRaisonRejet(String raisonRejet) {
			this.raisonRejet = raisonRejet;
		}

public int getRequestCode() {
			return requestCode;
		}

		public void setRequestCode(int requestCode) {
			this.requestCode = requestCode;
		}

public String getNumContrat() {
			return numContrat;
		}

		public void setNumContrat(String numContrat) {
			this.numContrat = numContrat;
		}

		public String getNomC() {
			return nomC;
		}

		public void setNomC(String nomC) {
			this.nomC = nomC;
		}

		public String getTypeC() {
			return typeC;
		}

		public void setTypeC(String typeC) {
			this.typeC = typeC;
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

public String getStatuContrat() {
		return statuContrat;
	}

	public void setStatuContrat(String statuContrat) {
		this.statuContrat = statuContrat;
	}

public String getCodeWilaya() {
		return codeWilaya;
	}

	public void setCodeWilaya(String codeWilaya) {
		this.codeWilaya = codeWilaya;
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

public String getRso() {
	return rso;
}

public void setRso(String rso) {
	this.rso = rso;
}

public String getNif() {
	return nif;
}

public void setNif(String nif) {
	this.nif = nif;
}

public String getDevise() {
	return devise;
}

public void setDevise(String devise) {
	this.devise = devise;
}

public int getStatusVa() {
	return statusVa;
}

public void setStatusVa(int statusVa) {
	this.statusVa = statusVa;
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

public String getAdresse() {
    return adresse;
  }

  public void setAdresse(String adresse) {
    this.adresse = adresse;
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

  public String getCommissionNational() {
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



  public String getAgentName() {
    return agentName;
  }

  public void setAgentName(String agentName) {
    this.agentName = agentName;
  }

  public Set<PendingTpe> getPendingTpes() {
    return pendingTpes;
  }

  public void setPendingTpes(Set<PendingTpe> pendingTpes) {
    this.pendingTpes = pendingTpes;
  }

  public String getMontantRef() {
        return montantRef;
    }

    public void setMontantRef(String montantRef) {
        this.montantRef = montantRef;
    }



  public List<CommissionTpeRequest> getCommissionTpes() {
    return commissionTpes;
  }

  public void setCommissionTpes(List<CommissionTpeRequest> commissionTpes) {
    this.commissionTpes = commissionTpes;
  }

  public List<CommissionTpeRequest> getCommissionTpesInter() {
    return commissionTpesInter;
  }

  public void setCommissionTpesInter(List<CommissionTpeRequest> commissionTpesInter) {
    this.commissionTpesInter = commissionTpesInter;
  }

  public String getLabel() {
    return label;
  }

  public void setLabel(String label) {
    this.label = label;
  }

  public Boolean getOffshore() {
    return offshore;
  }

  public void setOffshore(Boolean offshore) {
    this.offshore = offshore;
  }

@Override
public String toString() {
	return "TpeRequestDisplay [requestCode=" + requestCode + ", accountNumber=" + accountNumber + ", userName="
			+ userName + ", agence=" + agence + ", nombreTPE=" + nombreTPE + ", dateCreation=" + dateCreation
			+ ", dateDecision=" + dateDecision + ", commissionNational=" + commissionNational
			+ ", commissionInterNational=" + commissionInterNational + ", city=" + city + ", codeWilaya=" + codeWilaya
			+ ", country=" + country + ", codeZip=" + codeZip + ", phone=" + phone + ", label=" + label + ", status="
			+ status + ", agentName=" + agentName + ", adresse=" + adresse + ", commissionTypeCode="
			+ commissionTypeCode + ", offshore=" + offshore + ", montantRef=" + montantRef + ", statusVa=" + statusVa
			+ ", commissionTpes=" + commissionTpes + ", commissionTpesInter=" + commissionTpesInter + ", pendingTpes="
			+ pendingTpes + ", nom=" + nom + ", prenom=" + prenom + ", devise=" + devise + ", nif=" + nif + ", rso="
			+ rso + ", rc=" + rc + ", email=" + email + ", commune=" + commune + ", daira=" + daira + ", revenue="
			+ revenue + ", statuContrat=" + statuContrat + ", siteWeb=" + siteWeb + ", numContrat=" + numContrat
			+ ", nomC=" + nomC + ", typeC=" + typeC + ", titleC=" + titleC + ", raisonRejet=" + raisonRejet
			+ ", montantLoyer=" + montantLoyer + "]";
}




}
