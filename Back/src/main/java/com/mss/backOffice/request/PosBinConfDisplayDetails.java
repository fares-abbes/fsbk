package com.mss.backOffice.request;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class PosBinConfDisplayDetails {
	
	private int pbc_code;
	private String pbc_libelle;
    private List<PosBinDetailsForBinConf>bin_list= new ArrayList<PosBinDetailsForBinConf>();
	public int getPbc_code() {
		return pbc_code;
	}
	public void setPbc_code(int pbc_code) {
		this.pbc_code = pbc_code;
	}
	public String getPbc_libelle() {
		return pbc_libelle;
	}
	public void setPbc_libelle(String pbc_libelle) {
		this.pbc_libelle = pbc_libelle;
	}
	public List<PosBinDetailsForBinConf> getBin_list() {
		return bin_list;
	}
	public void setBin_list(List<PosBinDetailsForBinConf> bin_list) {
		this.bin_list = bin_list;
	}
	
    

}