package com.mss.backOffice.request;


import java.util.HashSet;
import java.util.Set;

public
class ProductRequest {

    public String libelle;

    public int cpCommissionNew;

    public int cpCommissionRemplacement;

    public int cpCommissionPin;

    public int cpCommissionAnniversary;


    public int cpCommissionOpposition;

    private Set < String > programs= new HashSet <>();
    private Set < Integer > eligibleAccounts= new HashSet <>();
//    private Integer productNum;
    private Integer cpCommissionModifPlafond;
    private Integer cpCancelation;
    private Integer cpCapturedCard;
    private boolean autoValidation;
    
    private Integer cpCommissionCreation;
    private Integer  cpCommissionAnniversaryAndCreation;
    private String  cpTypeCommissionAnniversary;

    
    
    
    public Integer getCpCommissionCreation() {
		return cpCommissionCreation;
	}

	public void setCpCommissionCreation(Integer cpCommissionCreation) {
		this.cpCommissionCreation = cpCommissionCreation;
	}

	public Integer getCpCommissionAnniversaryAndCreation() {
		return cpCommissionAnniversaryAndCreation;
	}

	public void setCpCommissionAnniversaryAndCreation(Integer cpCommissionAnniversaryAndCreation) {
		this.cpCommissionAnniversaryAndCreation = cpCommissionAnniversaryAndCreation;
	}

	public String getCpTypeCommissionAnniversary() {
		return cpTypeCommissionAnniversary;
	}

	public void setCpTypeCommissionAnniversary(String cpTypeCommissionAnniversary) {
		this.cpTypeCommissionAnniversary = cpTypeCommissionAnniversary;
	}

	public Set<Integer> getEligibleAccounts() {
		return eligibleAccounts;
	}

	public void setEligibleAccounts(Set<Integer> eligibleAccounts) {
		this.eligibleAccounts = eligibleAccounts;
	}

	public boolean isAutoValidation() {
		return autoValidation;
	}

	public void setAutoValidation(boolean autoValidation) {
		this.autoValidation = autoValidation;
	}

	public Integer getCpCancelation() {
		return cpCancelation;
	}

	public void setCpCancelation(Integer cpCancelation) {
		this.cpCancelation = cpCancelation;
	}

	public Integer getCpCapturedCard() {
		return cpCapturedCard;
	}

	public void setCpCapturedCard(Integer cpCapturedCard) {
		this.cpCapturedCard = cpCapturedCard;
	}

	public Integer getCpCommissionModifPlafond() {
		return cpCommissionModifPlafond;
	}

	public void setCpCommissionModifPlafond(Integer cpCommissionModifPlafond) {
		this.cpCommissionModifPlafond = cpCommissionModifPlafond;
	}

	public
    String getLibelle() {
        return libelle;
    }

    public
    void setLibelle(String libelle) {
        this.libelle = libelle;
    }

    public
    int getCpCommissionNew() {
        return cpCommissionNew;
    }

    public
    void setCpCommissionNew(int cpCommissionNew) {
        this.cpCommissionNew = cpCommissionNew;
    }

    public
    int getCpCommissionRemplacement() {
        return cpCommissionRemplacement;
    }

    public
    void setCpCommissionRemplacement(int cpCommissionRemplacement) {
        this.cpCommissionRemplacement = cpCommissionRemplacement;
    }

    public
    int getCpCommissionPin() {
        return cpCommissionPin;
    }

    public
    void setCpCommissionPin(int cpCommissionPin) {
        this.cpCommissionPin = cpCommissionPin;
    }

    public
    int getCpCommissionAnniversary() {
        return cpCommissionAnniversary;
    }

    public
    void setCpCommissionAnniversary(int cpCommissionAnniversary) {
        this.cpCommissionAnniversary = cpCommissionAnniversary;
    }

    public
    int getCpCommissionOpposition() {
        return cpCommissionOpposition;
    }

    public
    void setCpCommissionOpposition(int cpCommissionOpposition) {
        this.cpCommissionOpposition = cpCommissionOpposition;
    }

    public
    Set < String > getPrograms() {
        return programs;
    }

    public
    void setPrograms(Set < String > programs) {
        this.programs = programs;
    }

	@Override
	public String toString() {
		return "ProductRequest [libelle=" + libelle + ", cpCommissionNew=" + cpCommissionNew
				+ ", cpCommissionRemplacement=" + cpCommissionRemplacement + ", cpCommissionPin=" + cpCommissionPin
				+ ", cpCommissionAnniversary=" + cpCommissionAnniversary + ", cpCommissionOpposition="
				+ cpCommissionOpposition + ", programs=" + programs
				+ ", cpCommissionModifPlafond=" + cpCommissionModifPlafond + "]";
	}


}
