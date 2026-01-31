package com.mss.backOffice.request;

import com.mss.unified.entities.DualChargeBackMessage;
import com.mss.unified.entities.VisaIncoming;

import java.util.Objects;

public class RequestChargebackVisaI {
	Integer chargeback;
	DualChargeBackMessage message;
	private String additionalMessage;

	
	
	public Integer getChargeback() {
		return chargeback;
	}
	public void setChargeback(Integer chargeback) {
		this.chargeback = chargeback;
	}
	public DualChargeBackMessage getMessage() {
		return message;
	}
	public void setMessage(DualChargeBackMessage message) {
		this.message = message;
	}

	public String getAdditionalMessage() {
		return additionalMessage;
	}

	public void setAdditionalMessage(String additionalMessage) {
		this.additionalMessage = additionalMessage;
	}

	@Override
	public boolean equals(Object o) {
		if (!(o instanceof RequestChargebackVisaI)) return false;
		RequestChargebackVisaI that = (RequestChargebackVisaI) o;
		return Objects.equals(chargeback, that.chargeback);
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(chargeback);
	}
}
