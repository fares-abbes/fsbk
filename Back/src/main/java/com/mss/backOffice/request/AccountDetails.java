package com.mss.backOffice.request;

public class AccountDetails {
	
	private String statutCompte;
	private String typeIdentite;
	private String identite;
	private String statutClient;
	private String nomCompte;
	private String nom;
	private String prenom;
	private String cite;
	private String pays;
	private String addresse;
	private String zipCode;
	private String tel;
	private String devise;
	private String agence;
	private String njf;
	private String dns;
	private String email;
	private String viln;
	private String radical;
	private String compteAttacheDZD;
	private String deviseCompteAttache;
	
	
	public String getRadical() {
		return radical;
	}
	public void setRadical(String radical) {
		this.radical = radical;
	}
	public String getViln() {
		return viln;
	}
	public void setViln(String viln) {
		this.viln = viln;
	}
	public String getNjf() {
		return njf;
	}
	public void setNjf(String njf) {
		this.njf = njf;
	}
	public String getDns() {
		return dns;
	}
	public void setDns(String dns) {
		this.dns = dns;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public String getStatutCompte() {
		return statutCompte;
	}
	public void setStatutCompte(String statutCompte) {
		this.statutCompte = statutCompte;
	}
	public String getTypeIdentite() {
		return typeIdentite;
	}
	public void setTypeIdentite(String typeIdentite) {
		this.typeIdentite = typeIdentite;
	}
	public String getIdentite() {
		return identite;
	}
	public void setIdentite(String identite) {
		this.identite = identite;
	}
	public String getStatutClient() {
		return statutClient;
	}
	public void setStatutClient(String statutClient) {
		this.statutClient = statutClient;
	}
	public String getNomCompte() {
		return nomCompte;
	}
	public void setNomCompte(String nomCompte) {
		this.nomCompte = nomCompte;
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
	public String getCite() {
		return cite;
	}
	public void setCite(String cite) {
		this.cite = cite;
	}
	public String getPays() {
		return pays;
	}
	public void setPays(String pays) {
		this.pays = pays;
	}
	public String getAddresse() {
		return addresse;
	}
	public void setAddresse(String addresse) {
		this.addresse = addresse;
	}
	public String getZipCode() {
		return zipCode;
	}
	public void setZipCode(String zipCode) {
		this.zipCode = zipCode;
	}
	public String getTel() {
		return tel;
	}
	public void setTel(String tel) {
		this.tel = tel;
	}
	public String getDevise() {
		return devise;
	}
	public void setDevise(String devise) {
		this.devise = devise;
	}
	public String getAgence() {
		return agence;
	}
	public void setAgence(String agence) {
		this.agence = agence;
	}
	
	public String getCompteAttacheDZD() {
		return compteAttacheDZD;
	}
	public void setCompteAttacheDZD(String compteAttacheDZD) {
		this.compteAttacheDZD = compteAttacheDZD;
	}
	public String getDeviseCompteAttache() {
		return deviseCompteAttache;
	}
	public void setDeviseCompteAttache(String deviseCompteAttache) {
		this.deviseCompteAttache = deviseCompteAttache;
	}
	@Override
	public String toString() {
		return "AccountDetails [statutCompte=" + statutCompte + ", typeIdentite=" + typeIdentite + ", identite="
				+ identite + ", statutClient=" + statutClient + ", nomCompte=" + nomCompte + ", nom=" + nom
				+ ", prenom=" + prenom + ", cite=" + cite + ", pays=" + pays + ", addresse=" + addresse + ", zipCode="
				+ zipCode + ", tel=" + tel + ", devise=" + devise + ", agence=" + agence + "]";
	}

	

}
