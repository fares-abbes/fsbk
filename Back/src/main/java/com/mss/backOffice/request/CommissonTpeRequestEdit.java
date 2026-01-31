package com.mss.backOffice.request;

public class CommissonTpeRequestEdit {
    private String commissionNational;
    private String label;
    private String operateurMin;
    private String operateurMax;
    private String valeurComFixMin;
    //   private String valeurComFixMax;
    private String valeurComVariableMin;
    // private String valeurComVariableMax;
    private String commissionInterNational;
    private Integer merchantCode;
    //private commissionTpeId commissionTpeId;
    
    private String montantRefMin;
    private String montantRefMax;

    public String getCommissionNational() {
        return commissionNational;
    }

    public void setCommissionNational(String commissionNational) {
        this.commissionNational = commissionNational;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getOperateurMin() {
        return operateurMin;
    }

    public void setOperateurMin(String operateurMin) {
        this.operateurMin = operateurMin;
    }

    public String getOperateurMax() {
        return operateurMax;
    }

    public void setOperateurMax(String operateurMax) {
        this.operateurMax = operateurMax;
    }

    public String getValeurComFixMin() {
        return valeurComFixMin;
    }

    public void setValeurComFixMin(String valeurComFixMin) {
        this.valeurComFixMin = valeurComFixMin;
    }

    public String getValeurComVariableMin() {
        return valeurComVariableMin;
    }

    public void setValeurComVariableMin(String valeurComVariableMin) {
        this.valeurComVariableMin = valeurComVariableMin;
    }

    public String getCommissionInterNational() {
        return commissionInterNational;
    }

    public void setCommissionInterNational(String commissionInterNational) {
        this.commissionInterNational = commissionInterNational;
    }

    public Integer getMerchantCode() {
        return merchantCode;
    }

    public void setMerchantCode(Integer merchantCode) {
        this.merchantCode = merchantCode;
    }

	public String getMontantRefMin() {
		return montantRefMin;
	}

	public void setMontantRefMin(String montantRefMin) {
		this.montantRefMin = montantRefMin;
	}

	public String getMontantRefMax() {
		return montantRefMax;
	}

	public void setMontantRefMax(String montantRefMax) {
		this.montantRefMax = montantRefMax;
	}

	@Override
	public String toString() {
		return "CommissonTpeRequestEdit [commissionNational=" + commissionNational + ", label=" + label
				+ ", operateurMin=" + operateurMin + ", operateurMax=" + operateurMax + ", valeurComFixMin="
				+ valeurComFixMin + ", valeurComVariableMin=" + valeurComVariableMin + ", commissionInterNational="
				+ commissionInterNational + ", merchantCode=" + merchantCode + ", montantRefMin=" + montantRefMin
				+ ", montantRefMax=" + montantRefMax + "]";
	}

    

 
}
