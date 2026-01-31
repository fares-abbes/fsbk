package com.mss.backOffice.request;

public class CardFilterDisplay {
    String pan;
    int status;
    String agencyCode;
    
    
    public String getAgencyCode() {
		return agencyCode;
	}

	public void setAgencyCode(String agencyCode) {
		this.agencyCode = agencyCode;
	}

	public String getPan() {
        return pan;
    }

    public void setPan(String pan) {
        this.pan = pan;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }
}
