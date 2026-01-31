package com.mss.backOffice.request;

public class AccountPosDetails {
	private String rso;
	private String nif;
	private String rc;
	private String nom;
	private String adresse;
	private String zipCode;
	private String commune;
	private String daira;
	private String wilaya;
	
	private String email;
	
	private String username;

	
	
	private String pays;
	private String prenom;
	private String tel;
	private String nomCompte;
	private String devise;
	private String agence;	
	private String statutCompte;
	private String typeIdentite;
	private String identite;
	private String statutClient;
	
	private String codeWilaya;
	
	
	
	
	public String getCodeWilaya() {
		return codeWilaya;
	}
	public void setCodeWilaya(String codeWilaya) {
		this.codeWilaya = codeWilaya;
	}
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public String getPays() {
		return pays;
	}
	public void setPays(String pays) {
		this.pays = pays;
	}
	public String getPrenom() {
		return prenom;
	}
	public void setPrenom(String prenom) {
		this.prenom = prenom;
	}
	public String getTel() {
		return tel;
	}
	public void setTel(String tel) {
		this.tel = tel;
	}
	public String getNomCompte() {
		return nomCompte;
	}
	public void setNomCompte(String nomCompte) {
		this.nomCompte = nomCompte;
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
	public String getRc() {
		return rc;
	}
	public void setRc(String rc) {
		this.rc = rc;
	}
	public String getNom() {
		return nom;
	}
	public void setNom(String nom) {
		this.nom = nom;
	}
	
	
	public String getAdresse() {
		return adresse;
	}
	public void setAdresse(String adresse) {
		this.adresse = adresse;
	}
	public String getWilaya() {
		return wilaya;
	}
	public void setWilaya(String wilaya) {
		this.wilaya = wilaya;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public String getZipCode() {
		return zipCode;
	}
	public void setZipCode(String zipCode) {
		this.zipCode = zipCode;
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
	@Override
	public String toString() {
		return "AccountPosDetails [rso=" + rso + ", nif=" + nif + ", rc=" + rc + ", nom=" + nom + ", addresse="
				+ adresse + ", zipCode=" + zipCode + ", commune=" + commune + ", daira=" + daira + ", wilaya=" + wilaya
				+ ", email=" + email + ", username=" + username + ", pays=" + pays + ", prenom=" + prenom + ", tel="
				+ tel + ", nomCompte=" + nomCompte + ", devise=" + devise + ", agence=" + agence + ", statutCompte="
				+ statutCompte + ", typeIdentite=" + typeIdentite + ", identite=" + identite + ", statutClient="
				+ statutClient + "]";
	}
	
	


}
