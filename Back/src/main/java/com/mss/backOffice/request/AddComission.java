package com.mss.backOffice.request;

import java.util.HashSet;
import java.util.Set;

public class AddComission {
	 
	  private Integer transactionSource;
	  private String fixedCommission;
      private Integer programCode;
	  private String variableComission;

	  private String libelle;
	
	  private String currency;
	  private Set<String> emvServiceValues = new HashSet<>();
	  
	public Integer getProgramCode() {
		return programCode;
	}
	public void setProgramCode(Integer programCode) {
		this.programCode = programCode;
	}
	public Integer getTransactionSource() {
		return transactionSource;
	}
	public void setTransactionSource(Integer transactionSource) {
		this.transactionSource = transactionSource;
	}
	public String getFixedCommission() {
		return fixedCommission;
	}
	public void setFixedCommission(String fixedCommission) {
		this.fixedCommission = fixedCommission;
	}
	public String getVariableComission() {
		return variableComission;
	}
	public void setVariableComission(String variableComission) {
		this.variableComission = variableComission;
	}
	public String getLibelle() {
		return libelle;
	}
	public void setLibelle(String libelle) {
		this.libelle = libelle;
	}
	public String getCurrency() {
		return currency;
	}
	public void setCurrency(String currency) {
		this.currency = currency;
	}
	public Set<String> getEmvServiceValues() {
		return emvServiceValues;
	}
	public void setEmvServiceValues(Set<String> emvServiceValues) {
		this.emvServiceValues = emvServiceValues;
	}

	@Override
	public String toString() {
		return "AddComission{" +
				"transactionSource=" + transactionSource +
				", fixedCommission='" + fixedCommission + '\'' +
				", variableComission='" + variableComission + '\'' +
				", libelle='" + libelle + '\'' +
				", currency='" + currency + '\'' +
				", emvServiceValues=" + emvServiceValues +
				'}';
	}
}
