package com.mss.backOffice.request;

public class AcceptCbEmisInter {

	private Integer cp_code;
	private String compteBeneficiaire;
	private String additionalMessage;

	

	public Integer getCp_code() {
		return cp_code;
	}
	public void setCp_code(Integer cp_code) {
		this.cp_code = cp_code;
	}
	public String getCompteBeneficiaire() {
		return compteBeneficiaire;
	}
	public void setCompteBeneficiaire(String compteBeneficiaire) {
		this.compteBeneficiaire = compteBeneficiaire;
	}

    public String getAdditionalMessage() {
        return additionalMessage;
    }

    public void setAdditionalMessage(String additionalMessage) {
        this.additionalMessage = additionalMessage;
    }
}
