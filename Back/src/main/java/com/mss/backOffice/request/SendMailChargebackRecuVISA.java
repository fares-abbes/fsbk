package com.mss.backOffice.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.mss.unified.entities.ChargebackVisa;

public class SendMailChargebackRecuVISA extends ChargebackVisa {
    private String message;

    @JsonProperty("code_motif")
    private String codeMotif;
    private String additionalMessage;

    public String getCodeMotif() {
        return codeMotif;
    }

    public void setCodeMotif(String codeMotif) {
        this.codeMotif = codeMotif;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getAdditionalMessage() {
        return additionalMessage;
    }

    public void setAdditionalMessage(String additionalMessage) {
        this.additionalMessage = additionalMessage;
    }
}
