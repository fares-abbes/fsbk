package com.mss.backOffice.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.mss.unified.entities.VisaOutGoingAtm;

public class SendMailChargebackRecuVISAOutgoing extends VisaOutGoingAtm {

    private String message;

    @JsonProperty("code_motif")
    private String codeMotif;

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
}
