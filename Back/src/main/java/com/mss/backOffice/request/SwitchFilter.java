package com.mss.backOffice.request;

import java.util.Date;

public class SwitchFilter {

	public String switchRRN;
	public String switchAcceptorMerchantCode;
	public String dateDebut;
	public String dateFin;
	public String pan;

	public String getSwitchRRN() {
		return switchRRN;
	}

	public void setSwitchRRN(String switchRRN) {
		this.switchRRN = switchRRN;
	}

	public String getSwitchAcceptorMerchantCode() {
		return switchAcceptorMerchantCode;
	}

	public void setSwitchAcceptorMerchantCode(String switchAcceptorMerchantCode) {
		this.switchAcceptorMerchantCode = switchAcceptorMerchantCode;
	}

	public String getPan() {
		return pan;
	}

	public void setPan(String pan) {
		this.pan = pan;
	}

	public String getDateDebut() {
		return dateDebut;
	}

	public void setDateDebut(String dateDebut) {
		this.dateDebut = dateDebut;
	}

	public String getDateFin() {
		return dateFin;
	}

	public void setDateFin(String dateFin) {
		this.dateFin = dateFin;
	}

	public SwitchFilter() {
	}

	@Override
	public String toString() {
		return "SwitchFilter{" + "switchRRN='" + switchRRN + '\'' + ", switchAcceptorMerchantCode='"
				+ switchAcceptorMerchantCode + '\'' + ", dateDebut=" + dateDebut + ", dateFin=" + dateFin + ", pan='"
				+ pan + '\'' + '}';
	}
}
