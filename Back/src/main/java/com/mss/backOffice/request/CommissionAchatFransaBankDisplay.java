package com.mss.backOffice.request;


public class CommissionAchatFransaBankDisplay {
	
	private Integer code;   
	
	private String valeurMin;    
	
	private String valeurMax;    
	
	private String valeurFix;    
	
	private String valeurVarivable;    
	
	private String cmi;    
	
	private String cpi;

	public Integer getCode() {
		return code;
	}

	public void setCode(Integer code) {
		this.code = code;
	}

	public String getValeurMin() {
		return valeurMin;
	}

	public void setValeurMin(String valeurMin) {
		this.valeurMin = valeurMin;
	}

	public String getValeurMax() {
		return valeurMax;
	}

	public void setValeurMax(String valeurMax) {
		this.valeurMax = valeurMax;
	}

	public String getValeurFix() {
		return valeurFix;
	}

	public void setValeurFix(String valeurFix) {
		this.valeurFix = valeurFix;
	}

	public String getValeurVarivable() {
		return valeurVarivable;
	}

	public void setValeurVarivable(String valeurVarivable) {
		this.valeurVarivable = valeurVarivable;
	}

	public String getCmi() {
		return cmi;
	}

	public void setCmi(String cmi) {
		this.cmi = cmi;
	}

	public String getCpi() {
		return cpi;
	}

	public void setCpi(String cpi) {
		this.cpi = cpi;
	}

	@Override
	public String toString() {
		return "CommissionAchatFransaBankDisplay [code=" + code + ", valeurMin=" + valeurMin + ", valeurMax="
				+ valeurMax + ", valeurFix=" + valeurFix + ", valeurVarivable=" + valeurVarivable + ", cmi=" + cmi
				+ ", cpi=" + cpi + "]";
	}
	
	

}
