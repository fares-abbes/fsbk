package com.mss.backOffice.request;

import java.util.HashSet;
import java.util.Set;
import com.mss.unified.entities.Program;

public class RequestProductRev {

	  
	  public String libelle;


	 
	  public int cpCommissionNew;

	
	  public int cpCommissionRemplacement;

	 
	  public int cpCommissionPin;

	  
	  public int cpCommissionAnniversary;

	 
	  public int cpCommissionOpposition;
	 
	  private Set<String> programs = new HashSet<>();

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

	@Override
	public String toString() {
		return "RequestProductRev{" +
				"libelle='" + libelle + '\'' +
				", cpCommissionNew=" + cpCommissionNew +
				", cpCommissionRemplacement=" + cpCommissionRemplacement +
				", cpCommissionPin=" + cpCommissionPin +
				", cpCommissionAnniversary=" + cpCommissionAnniversary +
				", cpCommissionOpposition=" + cpCommissionOpposition +
				", programs=" + programs +
				'}';
	}
}
