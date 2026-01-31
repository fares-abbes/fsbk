package com.mss.backOffice.request;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.mss.unified.entities.PendingTpe;

public class ValidateRequestEdit {
    private  Integer codeRequest;
    private List< ValidateTpeRequest > listIdTpe;
    private String commissionInterNational;
    private String commissionNational;
    private String city;
    private String country;
    private String codeZip;
    private String phone;
    private String adresse;
    private List<CommissonTpeRequestEdit> commissionTpes;
    private List<CommissonTpeRequestEdit> commissionTpesInter;
    private Set<PendingTpe> pendingTpes = new HashSet<>();
    private String commissionTypeCode ;
    private boolean offshore;
    
	private String commune;

	private String daira;

	private String revenue;
	  
	private String siteWeb;
	
	 private String nomC;
	    

	    private String typeC;
	    

	    private String titleC;
	    private String montantLoyer;

	    
	    
    
    public String getMontantLoyer() {
			return montantLoyer;
		}

		public void setMontantLoyer(String montantLoyer) {
			this.montantLoyer = montantLoyer;
		}

	public Set<PendingTpe> getPendingTpes() {
			return pendingTpes;
		}

		public void setPendingTpes(Set<PendingTpe> pendingTpes) {
			this.pendingTpes = pendingTpes;
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

	public String getRevenue() {
		return revenue;
	}

	public void setRevenue(String revenue) {
		this.revenue = revenue;
	}

	public String getSiteWeb() {
		return siteWeb;
	}

	public void setSiteWeb(String siteWeb) {
		this.siteWeb = siteWeb;
	}

	public boolean isOffshore() {
		return offshore;
	}

	public void setOffshore(boolean offshore) {
		this.offshore = offshore;
	}

	public String getCommissionTypeCode() {
		return commissionTypeCode;
	}

	public void setCommissionTypeCode(String commissionTypeCode) {
		this.commissionTypeCode = commissionTypeCode;
	}

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

    public List<CommissonTpeRequestEdit> getCommissionTpes() {
        return commissionTpes;
    }

    public void setCommissionTpes(List<CommissonTpeRequestEdit> commissionTpes) {
        this.commissionTpes = commissionTpes;
    }

    public List<CommissonTpeRequestEdit> getCommissionTpesInter() {
        return commissionTpesInter;
    }

    public void setCommissionTpesInter(List<CommissonTpeRequestEdit> commissionTpesInter) {
        this.commissionTpesInter = commissionTpesInter;
    }

	@Override
	public String toString() {
		return "ValidateRequestEdit [codeRequest=" + codeRequest + ", listIdTpe=" + listIdTpe
				+ ", commissionInterNational=" + commissionInterNational + ", commissionNational=" + commissionNational
				+ ", city=" + city + ", country=" + country + ", codeZip=" + codeZip + ", phone=" + phone + ", adresse="
				+ adresse + ", commissionTpes=" + commissionTpes + ", commissionTpesInter=" + commissionTpesInter
				+ ", pendingTpes=" + pendingTpes + ", commissionTypeCode=" + commissionTypeCode + ", offshore="
				+ offshore + ", commune=" + commune + ", daira=" + daira + ", revenue=" + revenue + ", siteWeb="
				+ siteWeb + ", nomC=" + nomC + ", typeC=" + typeC + ", titleC=" + titleC + ", montantLoyer="
				+ montantLoyer + "]";
	}


}
