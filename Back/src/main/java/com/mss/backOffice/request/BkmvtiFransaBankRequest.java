package com.mss.backOffice.request;

import com.mss.unified.entities.BkmvtiFransaBank;

public class BkmvtiFransaBankRequest extends BkmvtiFransaBank {

	private String refDossierLocal;
	
	private String dcoStart;
	private String dvaStart;
	private String dcoEnd;
	private String dvaEnd;

	public String getDcoStart() {
		return dcoStart;
	}

	public void setDcoStart(String dcoStart) {
		this.dcoStart = dcoStart;
	}

	public String getDvaStart() {
		return dvaStart;
	}

	public void setDvaStart(String dvaStart) {
		this.dvaStart = dvaStart;
	}

	public String getDcoEnd() {
		return dcoEnd;
	}

	public void setDcoEnd(String dcoEnd) {
		this.dcoEnd = dcoEnd;
	}

	public String getDvaEnd() {
		return dvaEnd;
	}

	public void setDvaEnd(String dvaEnd) {
		this.dvaEnd = dvaEnd;
	}

	@Override
	public void setRefDossier(String refDossier) {
		this.refDossierLocal = refDossier;
	}

	@Override
	public String getRefDossier() {
		return refDossierLocal != null ? refDossierLocal : super.getRefDossier();
	}
	
	@Override
	public String toString() {
		return "BkmvtiFransaBankRequest [dcoStart=" + dcoStart + ", dvaStart=" + dvaStart + ", dcoEnd=" + dcoEnd
				+ ", dvaEnd=" + dvaEnd + ", getId()=" + getIdbkm() + ", getAgence()=" + getAgence() + ", getCodeDevice()="
				+ getCodeDevice() + ", getChapitreComptable()=" + getChapitreComptable() + ", getNumCompte()="
				+ getNumCompte() + ", getSuffixeCompte()=" + getSuffixeCompte() + ", getCodeOperation()="
				+ getCodeOperation() + ", getNumMouvement()=" + getNumMouvement() + ", getCodeRegroupement()="
				+ getCodeRegroupement() + ", getCodeUtilisateur()=" + getCodeUtilisateur() + ", getNumEvenement()="
				+ getNumEvenement() + ", getCleControleCompte()=" + getCleControleCompte() + ", getDateComptable()="
				+ getDateComptable() + ", getCodeService()=" + getCodeService() + ", getDateValeur()=" + getDateValeur()
				+ ", getMontant()=" + getMontant() + ", getSens()=" + getSens() + ", getLibelle()=" + getLibelle()
				+ ", getExonerationcommission()=" + getExonerationcommission() + ", getNumPiece()=" + getNumPiece()
				+ ", getReferanceLettrage()=" + getReferanceLettrage() + ", getCodeDesaccord1()=" + getCodeDesaccord1()
				+ ", getCodeDesaccord2()=" + getCodeDesaccord2() + ", getCodeDesaccord3()=" + getCodeDesaccord3()
				+ ", getCodeDesaccord4()=" + getCodeDesaccord4() + ", getCodeDesaccord5()=" + getCodeDesaccord5()
				+ ", getCodeUtilisateur_utf()=" + getCodeUtilisateur_utf() + ", getCodeutilisateurAutorise()="
				+ getCodeutilisateurAutorise() + ", getTauxChange()=" + getTauxChange() + ", getDateIndisponible()="
				+ getDateIndisponible() + ", getZoneutilise()=" + getZoneUtilise()
				+ ", getZoneutiliseespecifiquement()=" + getZoneutiliseespecifiquement()
				+ ", getNumCompteRattachement()=" + getNumCompteRattachement() + ", getSufficeCompteRattrachement()="
				+ getSufficeCompteRattrachement() + ", getZoneUtiliseSpecifiquement()=" + getZoneUtiliseSpecifiquement()
				+ ", getCalculmouvementInteragence()=" + getCalculmouvementInteragence() + ", getMouvementAgence()="
				+ getMouvementAgence() + ", getCodeMAJ()=" + getCodeMAJ() + ", getDateEcheance()=" + getDateEcheance()
				+ ", getCodeAgenceSaisie()=" + getCodeAgenceSaisie() + ", getAgenceEmettrice()=" + getAgenceEmettrice()
				+ ", getAgenceDestinatrice()=" + getAgenceDestinatrice() + ", getCodeDeviceOrigine()="
				+ getCodeDeviceOrigine() + ", getMontantOrigine()=" + getMontantOrigine() + ", getNumPiece1()="
				+ getNumPiece1() + ", getCodeID()=" + getCodeID() + ", getNumSequential()=" + getNumSequential()
				+ ", getCodeLangue()=" + getCodeLangue() + ", getLibelleMouvmeent()=" + getLibelleMouvmeent()
				+ ", getCodeModule()=" + getCodeModule() + ", getRefDossier()=" + getRefDossier()
				+ ", getRefAnalytique()=" + getRefAnalytique() + ", getLabelMouvmeent()=" + getLabelMouvmeent()
				+ ", getNatureTransaction()=" + getNatureTransaction() + ", getCodeEtat()=" + getCodeEtat()
				+ ", getCodeSchema()=" + getCodeSchema() + ", getCodeEtiquette()=" + getCodeEtiquette()
				+ ", getDestinationAnalytique()=" + getDestinationAnalytique() + ", getCodemouvementFusionne()="
				+ getCodemouvementFusionne() + ", getIdHeder()=" + getIdHeder() + ", getPieceComptable()="
				+ getPieceComptable() + ", getRefernceLettrage()=" + getRefernceLettrage() + "]";
	}

}
