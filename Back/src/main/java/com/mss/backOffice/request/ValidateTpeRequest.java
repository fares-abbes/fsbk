package com.mss.backOffice.request;

public class ValidateTpeRequest {
	private Integer code;
	private boolean validate;
	private boolean reject;
	private String motif;
	private Integer model;
	private Integer posLimits;
	private Integer posServices;
	private Integer posBin;
	private Integer mccCode;
	
	

	public Integer getMccCode() {
		return mccCode;
	}

	public void setMccCode(Integer mccCode) {
		this.mccCode = mccCode;
	}

	public Integer getModel() {
		return model;
	}

	public void setModel(Integer model) {
		this.model = model;
	}

	public Integer getCode() {
		return code;
	}

	public void setCode(Integer code) {
		this.code = code;
	}

	public boolean isValidate() {
		return validate;
	}

	public void setValidate(boolean validate) {
		this.validate = validate;
	}

	public boolean isReject() {
		return reject;
	}

	public void setReject(boolean reject) {
		this.reject = reject;
	}

	public String getMotif() {
		return motif;
	}

	public void setMotif(String motif) {
		this.motif = motif;
	}

	public Integer getPosLimits() {
		return posLimits;
	}

	public void setPosLimits(Integer posLimits) {
		this.posLimits = posLimits;
	}

	public Integer getPosServices() {
		return posServices;
	}

	public void setPosServices(Integer posServices) {
		this.posServices = posServices;
	}

	public Integer getPosBin() {
		return posBin;
	}

	public void setPosBin(Integer posBin) {
		this.posBin = posBin;
	}

	@Override
	public String toString() {
		return "ValidateTpeRequest [code=" + code + ", validate=" + validate + ", reject=" + reject + ", motif=" + motif
				+ ", model=" + model + ", posLimits=" + posLimits + ", posServices=" + posServices + ", posBin="
				+ posBin + ", mccCode=" + mccCode + "]";
	}

	
	
}
