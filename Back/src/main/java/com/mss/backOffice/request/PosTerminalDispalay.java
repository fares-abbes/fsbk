package com.mss.backOffice.request;

import java.util.Date;

import javax.persistence.Column;

public class PosTerminalDispalay {

  private int posCode;
 
  private String merchantCode;
  private String idContrat;

  private String mccCode;
  private String mcc;
  private String posNum;
  private String posLibelle;
  private String posLocation;
  private String city;
  private String state;
  private String country;
  private String phone;
  private String terminalOwner;
  private String serviceRepInfo;
  private String referralTel;
  private Date creationDate;
  private String adresse;
  private String posLimits;
  private String posAllowedTrans;
  private String posServices; 
  private String posBins;
  private String serialNum;
  private String status;
  private String statusSerial;
  private String statusSerialCode;
  
  private String statusUpdtae;
	private String statusSup;
	private String detailUpdate;
	private String detailSupp;
	
		private String commune;
	    
	    private String siteWeb;
	   	private String daira;

	   	private String codeZip;
	    private String terminalType;
	    private String terminalFamilly;
	    
		private String montantLoyer;

	    private int famillyPos;




public String getIdContrat() {
			return idContrat;
		}

		public void setIdContrat(String idContrat) {
			this.idContrat = idContrat;
		}

public int getFamillyPos() {
			return famillyPos;
		}

		public void setFamillyPos(int famillyPos) {
			this.famillyPos = famillyPos;
		}

public String getMontantLoyer() {
			return montantLoyer;
		}

		public void setMontantLoyer(String montantLoyer) {
			this.montantLoyer = montantLoyer;
		}

public String getTerminalType() {
			return terminalType;
		}

		public void setTerminalType(String terminalType) {
			this.terminalType = terminalType;
		}

		public String getTerminalFamilly() {
			return terminalFamilly;
		}

		public void setTerminalFamilly(String terminalFamilly) {
			this.terminalFamilly = terminalFamilly;
		}

public String getMcc() {
			return mcc;
		}

		public void setMcc(String mcc) {
			this.mcc = mcc;
		}

public String getCommune() {
			return commune;
		}

		public void setCommune(String commune) {
			this.commune = commune;
		}

		public String getSiteWeb() {
			return siteWeb;
		}

		public void setSiteWeb(String siteWeb) {
			this.siteWeb = siteWeb;
		}

		public String getDaira() {
			return daira;
		}

		public void setDaira(String daira) {
			this.daira = daira;
		}

		public String getCodeZip() {
			return codeZip;
		}

		public void setCodeZip(String codeZip) {
			this.codeZip = codeZip;
		}

public String getDetailSupp() {
		return detailSupp;
	}

	public void setDetailSupp(String detailSupp) {
		this.detailSupp = detailSupp;
	}

public String getDetailUpdate() {
		return detailUpdate;
	}

	public void setDetailUpdate(String detailUpdate) {
		this.detailUpdate = detailUpdate;
	}

public String getStatusUpdtae() {
		return statusUpdtae;
	}

	public void setStatusUpdtae(String statusUpdtae) {
		this.statusUpdtae = statusUpdtae;
	}

	public String getStatusSup() {
		return statusSup;
	}

	public void setStatusSup(String statusSup) {
		this.statusSup = statusSup;
	}

public String getStatusSerialCode() {
	return statusSerialCode;
}

public void setStatusSerialCode(String statusSerialCode) {
	this.statusSerialCode = statusSerialCode;
}

public void setStatus(String status) {
	this.status = status;
}

public int getPosCode() {
    return posCode;
  }

  public void setPosCode(int posCode) {
    this.posCode = posCode;
  }



  public String getMerchantCode() {
    return merchantCode;
  }

  public void setMerchantCode(String merchantCode) {
    this.merchantCode = merchantCode;
  }

  public String getMccCode() {
    return mccCode;
  }

  public void setMccCode(String mccCode) {
    this.mccCode = mccCode;
  }

  public String getPosNum() {
    return posNum;
  }

  public void setPosNum(String posNum) {
    this.posNum = posNum;
  }

  public String getPosLibelle() {
    return posLibelle;
  }

  public void setPosLibelle(String posLibelle) {
    this.posLibelle = posLibelle;
  }

  public String getPosLocation() {
    return posLocation;
  }

  public void setPosLocation(String posLocation) {
    this.posLocation = posLocation;
  }

  public String getCity() {
    return city;
  }

  public void setCity(String city) {
    this.city = city;
  }

  public String getState() {
    return state;
  }

  public void setState(String state) {
    this.state = state;
  }

  public String getCountry() {
    return country;
  }

  public void setCountry(String country) {
    this.country = country;
  }

  public String getPhone() {
    return phone;
  }

  public void setPhone(String phone) {
    this.phone = phone;
  }

  public String getTerminalOwner() {
    return terminalOwner;
  }

  public void setTerminalOwner(String terminalOwner) {
    this.terminalOwner = terminalOwner;
  }

  public String getServiceRepInfo() {
    return serviceRepInfo;
  }

  public void setServiceRepInfo(String serviceRepInfo) {
    this.serviceRepInfo = serviceRepInfo;
  }

  public String getReferralTel() {
    return referralTel;
  }

  public void setReferralTel(String referralTel) {
    this.referralTel = referralTel;
  }

  public Date getCreationDate() {
    return creationDate;
  }

  public void setCreationDate(Date creationDate) {
    this.creationDate = creationDate;
  }

  public String getAdresse() {
    return adresse;
  }

  public void setAdresse(String adresse) {
    this.adresse = adresse;
  }

  public String getPosLimits() {
    return posLimits;
  }

  public void setPosLimits(String posLimits) {
    this.posLimits = posLimits;
  }

  public String getPosAllowedTrans() {
    return posAllowedTrans;
  }

  public void setPosAllowedTrans(String posAllowedTrans) {
    this.posAllowedTrans = posAllowedTrans;
  }

  public String getPosServices() {
	return posServices;
}

public void setPosServices(String posServices) {
	this.posServices = posServices;
}

public String getPosBins() {
	return posBins;
}

public void setPosBins(String posBins) {
	this.posBins = posBins;
}



public String getSerialNum() {
	return serialNum;
}

public void setSerialNum(String serialNum) {
	this.serialNum = serialNum;
}

public String getStatusSerial() {
	return statusSerial;
}

public void setStatusSerial(String statusSerial) {
	this.statusSerial = statusSerial;
}

public String getStatus() {
	return status;
}

public PosTerminalDispalay(int posCode, String merchantCode,
      String mccCode, String posNum, String posLibelle, String posLocation, String city,
      String state, String country, String phone, String terminalOwner,
      String serviceRepInfo, String referralTel, Date creationDate, String adresse,
      String posLimits, String posAllowedTrans,String posServices, String posBins,
      String status, String serialNum,String statusSerial,String statusSerialCode) {
    this.posCode = posCode;
    this.merchantCode = merchantCode;
    this.mccCode = mccCode;
    this.posNum = posNum;
    this.posLibelle = posLibelle;
    this.posLocation = posLocation;
    this.city = city;
    this.state = state;
    this.country = country;
    this.phone = phone;
    this.terminalOwner = terminalOwner;
    this.serviceRepInfo = serviceRepInfo;
    this.referralTel = referralTel;
    this.creationDate = creationDate;
    this.adresse = adresse;
    this.posLimits = posLimits;
    this.posAllowedTrans = posAllowedTrans;
    this.posServices=posServices;
    this.posBins=posBins;
    this.status =status;
    this.serialNum=serialNum;
    this.statusSerial =statusSerial;
    this.statusSerialCode=statusSerialCode;


  }


public PosTerminalDispalay(int posCode, String merchantCode,
	      String mccCode, String posNum, String posLibelle, String posLocation, String city,
	      String state, String country, String phone, String terminalOwner,
	      String serviceRepInfo, String referralTel, Date creationDate, String adresse,String posAllowedTrans) {
	    this.posCode = posCode;
	    this.merchantCode = merchantCode;
	    this.mccCode = mccCode;
	    this.posNum = posNum;
	    this.posLibelle = posLibelle;
	    this.posLocation = posLocation;
	    this.city = city;
	    this.state = state;
	    this.country = country;
	    this.phone = phone;
	    this.terminalOwner = terminalOwner;
	    this.serviceRepInfo = serviceRepInfo;
	    this.referralTel = referralTel;
	    this.creationDate = creationDate;
	    this.adresse = adresse;
	    this.posAllowedTrans = posAllowedTrans;
	  }

public PosTerminalDispalay() {
	super();
	// TODO Auto-generated constructor stub
}





}
