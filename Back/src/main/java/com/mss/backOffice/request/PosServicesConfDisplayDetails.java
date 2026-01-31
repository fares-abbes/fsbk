package com.mss.backOffice.request;

import java.util.ArrayList;
import java.util.List;

public class PosServicesConfDisplayDetails {

	int psc_code;
	String psc_libelle;
    private List<PosServiceDetailsForServicesConf>services_list= new ArrayList<PosServiceDetailsForServicesConf>();
	
	public int getPsc_code() {
		return psc_code;
	}
	public void setPsc_code(int psc_code) {
		this.psc_code = psc_code;
	}
	public String getPsc_libelle() {
		return psc_libelle;
	}
	public void setPsc_libelle(String psc_libelle) {
		this.psc_libelle = psc_libelle;
	}
	public List<PosServiceDetailsForServicesConf> getServices_list() {
		return services_list;
	}
	public void setServices_list(List<PosServiceDetailsForServicesConf> services_list) {
		this.services_list = services_list;
	}

}
