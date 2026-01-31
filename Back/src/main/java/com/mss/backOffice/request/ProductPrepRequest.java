package com.mss.backOffice.request;

import java.util.HashSet;
import java.util.Set;

public class ProductPrepRequest {
    public String libelle;

    public int cpCommissionNew;

    public int cpCommissionRemplacement;

    public int cpCommissionPin;

    public int cpCommissionAnniversary;


    public int cpCommissionOpposition;

    private Set < String > programs= new HashSet <>();
    
    

	public ProductPrepRequest(String libelle, int cpCommissionNew, int cpCommissionRemplacement, int cpCommissionPin,
			int cpCommissionAnniversary, int cpCommissionOpposition) {
		super();
		this.libelle = libelle;
		this.cpCommissionNew = cpCommissionNew;
		this.cpCommissionRemplacement = cpCommissionRemplacement;
		this.cpCommissionPin = cpCommissionPin;
		this.cpCommissionAnniversary = cpCommissionAnniversary;
		this.cpCommissionOpposition = cpCommissionOpposition;
	}

	public String getLibelle() {
		return libelle;
	}

	public void setLibelle(String libelle) {
		this.libelle = libelle;
	}

	public int getCpCommissionNew() {
		return cpCommissionNew;
	}

	public void setCpCommissionNew(int cpCommissionNew) {
		this.cpCommissionNew = cpCommissionNew;
	}

	public int getCpCommissionRemplacement() {
		return cpCommissionRemplacement;
	}

	public void setCpCommissionRemplacement(int cpCommissionRemplacement) {
		this.cpCommissionRemplacement = cpCommissionRemplacement;
	}

	public int getCpCommissionPin() {
		return cpCommissionPin;
	}

	public void setCpCommissionPin(int cpCommissionPin) {
		this.cpCommissionPin = cpCommissionPin;
	}

	public int getCpCommissionAnniversary() {
		return cpCommissionAnniversary;
	}

	public void setCpCommissionAnniversary(int cpCommissionAnniversary) {
		this.cpCommissionAnniversary = cpCommissionAnniversary;
	}

	public int getCpCommissionOpposition() {
		return cpCommissionOpposition;
	}

	public void setCpCommissionOpposition(int cpCommissionOpposition) {
		this.cpCommissionOpposition = cpCommissionOpposition;
	}

	public Set<String> getPrograms() {
		return programs;
	}

	public void setPrograms(Set<String> programs) {
		this.programs = programs;
	}
    
    
}
